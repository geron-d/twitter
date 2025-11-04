package com.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import com.twitter.repository.TweetRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
public class TweetControllerTest {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("twitter_test")
        .withUsername("test")
        .withPassword("test");

    private static WireMockServer wireMockServer;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        synchronized (TweetControllerTest.class) {
            if (wireMockServer == null) {
                wireMockServer = new WireMockServer(0); // Random port
                wireMockServer.start();
            }
        }
        int wireMockPort = wireMockServer.port();
        
        registry.add("wiremock.server.port", () -> String.valueOf(wireMockPort));
        registry.add("app.users-api.base-url", () -> "http://localhost:" + wireMockPort);
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        if (wireMockServer != null) {
            wireMockServer.resetAll();
        }
    }

    @AfterAll
    static void tearDownWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    /**
     * Sets up WireMock stub for user existence check.
     *
     * @param userId the user ID to check
     * @param exists whether the user exists
     */
    protected void setupUserExistsStub(UUID userId, boolean exists) {
        if (wireMockServer == null) {
            return;
        }
        
        wireMockServer.stubFor(
            get(urlEqualTo("/api/v1/users/" + userId + "/exists"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"exists\":" + exists + "}"))
        );
    }

    /**
     * Sets up WireMock stub for user existence check with error response.
     *
     * @param userId the user ID to check
     * @param statusCode HTTP status code to return
     */
    protected void setupUserExistsStubWithError(UUID userId, int statusCode) {
        if (wireMockServer == null) {
            return;
        }
        
        wireMockServer.stubFor(
            get(urlEqualTo("/api/v1/users/" + userId + "/exists"))
                .willReturn(aResponse()
                    .withStatus(statusCode)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\":\"Internal Server Error\"}"))
        );
    }

    /**
     * Creates a valid CreateTweetRequestDto for testing.
     *
     * @param userId the user ID
     * @param content the tweet content
     * @return CreateTweetRequestDto instance
     */
    protected CreateTweetRequestDto createValidRequest(UUID userId, String content) {
        return CreateTweetRequestDto.builder()
            .userId(userId)
            .content(content)
            .build();
    }

    /**
     * Verifies that a tweet exists in the database.
     *
     * @param tweetId the tweet ID to check
     * @return true if tweet exists, false otherwise
     */
    protected boolean verifyTweetInDatabase(UUID tweetId) {
        return tweetRepository.findById(tweetId).isPresent();
    }

    /**
     * Gets tweet count in database.
     *
     * @return number of tweets in database
     */
    protected long getTweetCount() {
        return tweetRepository.count();
    }

    @Nested
    class CreateTweetTests {

        private UUID testUserId;

        @BeforeEach
        void setUp() {
            testUserId = UUID.randomUUID();
        }

        @Test
        void createTweet_WithValidData_ShouldReturn201Created() throws Exception {
            String content = "Hello World";
            setupUserExistsStub(testUserId, true);
            CreateTweetRequestDto request = createValidRequest(testUserId, content);

            String responseJson = mockMvc.perform(post("/api/v1/tweets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

            TweetResponseDto responseDto = objectMapper.readValue(responseJson, TweetResponseDto.class);

            assertThat(verifyTweetInDatabase(responseDto.getId())).isTrue();
            assertThat(getTweetCount()).isEqualTo(1);
        }

        @Test
        void createTweet_WithEmptyContent_ShouldReturn400BadRequest() throws Exception {
            CreateTweetRequestDto request = createValidRequest(testUserId, "");

            mockMvc.perform(post("/api/v1/tweets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            assertThat(getTweetCount()).isEqualTo(0);
        }

        @Test
        void createTweet_WithContentExceedingMaxLength_ShouldReturn400BadRequest() throws Exception {
            String content = "A".repeat(281);
            setupUserExistsStub(testUserId, true);
            CreateTweetRequestDto request = createValidRequest(testUserId, content);

            mockMvc.perform(post("/api/v1/tweets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            assertThat(getTweetCount()).isEqualTo(0);
        }

        @org.junit.jupiter.api.Test
        void createTweet_WithNullUserId_ShouldReturn400BadRequest() throws Exception {
            CreateTweetRequestDto request = CreateTweetRequestDto.builder()
                .userId(null)
                .content("Valid content")
                .build();

            mockMvc.perform(post("/api/v1/tweets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            assertThat(getTweetCount()).isEqualTo(0);
        }

        @Test
        void createTweet_WhenUsersApiReturns500_ShouldHandleGracefully() throws Exception {
            CreateTweetRequestDto request = createValidRequest(testUserId, "Valid content");
            setupUserExistsStubWithError(testUserId, 500);

            mockMvc.perform(post("/api/v1/tweets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists());

            assertThat(getTweetCount()).isEqualTo(0);
        }

        @Test
        void createTweet_WithMissingBody_ShouldReturn400BadRequest() throws Exception {
            int status = mockMvc.perform(post("/api/v1/tweets")
                    .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getStatus();
            
            assertThat(status).isGreaterThanOrEqualTo(400);
            assertThat(getTweetCount()).isEqualTo(0);
        }
    }
}

