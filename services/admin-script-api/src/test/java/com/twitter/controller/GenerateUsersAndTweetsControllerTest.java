package com.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
import com.twitter.dto.external.CreateTweetRequestDto;
import com.twitter.dto.external.TweetResponseDto;
import com.twitter.dto.external.UserRequestDto;
import com.twitter.dto.external.UserResponseDto;
import com.twitter.dto.request.GenerateUsersAndTweetsRequestDto;
import com.twitter.dto.response.GenerateUsersAndTweetsResponseDto;
import com.twitter.testconfig.BaseIntegrationTest;
import com.twitter.testconfig.WireMockStubHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class GenerateUsersAndTweetsControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        wireMockServer = getWireMockServer();
    }


    /**
     * Creates a valid GenerateUsersAndTweetsRequestDto for testing.
     *
     * @param nUsers            number of users to create
     * @param nTweetsPerUser    number of tweets per user
     * @param lUsersForDeletion number of users for deletion
     * @return GenerateUsersAndTweetsRequestDto instance
     */
    private GenerateUsersAndTweetsRequestDto createValidRequest(Integer nUsers, Integer nTweetsPerUser, Integer lUsersForDeletion) {
        return GenerateUsersAndTweetsRequestDto.builder()
            .nUsers(nUsers)
            .nTweetsPerUser(nTweetsPerUser)
            .lUsersForDeletion(lUsersForDeletion)
            .build();
    }

    @Nested
    class GenerateUsersAndTweetsTests {

        @Test
        void generateUsersAndTweets_WithValidData_ShouldReturn200Ok() throws Exception {
            int nUsers = 2;
            int nTweetsPerUser = 3;
            int lUsersForDeletion = 1;

            GenerateUsersAndTweetsRequestDto request = createValidRequest(nUsers, nTweetsPerUser, lUsersForDeletion);

            List<UUID> userIds = new ArrayList<>();
            for (int i = 0; i < nUsers; i++) {
                UUID userId = UUID.randomUUID();
                userIds.add(userId);

                UserRequestDto userRequest = UserRequestDto.builder()
                    .login("user" + i)
                    .firstName("First" + i)
                    .lastName("Last" + i)
                    .email("user" + i + "@example.com")
                    .password("password123")
                    .build();

                UserResponseDto userResponse = new UserResponseDto(
                    userId,
                    "user" + i,
                    "First" + i,
                    "Last" + i,
                    "user" + i + "@example.com",
                    UserStatus.ACTIVE,
                    UserRole.USER,
                    LocalDateTime.now()
                );

                WireMockStubHelper.setupCreateUserStub(wireMockServer, objectMapper, userRequest, userResponse);
            }

            List<UUID> tweetIds = new ArrayList<>();
            for (UUID userId : userIds) {
                List<TweetResponseDto> userTweets = new ArrayList<>();
                for (int j = 0; j < nTweetsPerUser; j++) {
                    UUID tweetId = UUID.randomUUID();
                    tweetIds.add(tweetId);

                    CreateTweetRequestDto createTweetRequest = CreateTweetRequestDto.builder()
                        .userId(userId)
                        .content("Tweet content " + j)
                        .build();

                    TweetResponseDto tweetResponse = new TweetResponseDto(
                        tweetId,
                        userId,
                        "Tweet content " + j,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        false,
                        null
                    );

                    WireMockStubHelper.setupCreateTweetStub(wireMockServer, objectMapper, createTweetRequest, tweetResponse);
                    userTweets.add(tweetResponse);
                }

                WireMockStubHelper.setupGetUserTweetsStub(wireMockServer, objectMapper, userId, userTweets);
            }

            if (!tweetIds.isEmpty()) {
                WireMockStubHelper.setupDeleteTweetStub(wireMockServer, tweetIds.getFirst());
            }

            String responseJson = mockMvc.perform(post("/api/v1/admin-scripts/generate-users-and-tweets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.createdUsers").isArray())
                .andExpect(jsonPath("$.createdTweets").isArray())
                .andExpect(jsonPath("$.deletedTweets").isArray())
                .andExpect(jsonPath("$.statistics").exists())
                .andExpect(jsonPath("$.statistics.totalUsersCreated").value(nUsers))
                .andExpect(jsonPath("$.statistics.totalTweetsCreated").value(nUsers * nTweetsPerUser))
                .andReturn()
                .getResponse()
                .getContentAsString();

            GenerateUsersAndTweetsResponseDto response = objectMapper.readValue(responseJson, GenerateUsersAndTweetsResponseDto.class);
            assertThat(response.createdUsers()).hasSize(nUsers);
            assertThat(response.createdTweets()).hasSize(nUsers * nTweetsPerUser);
            assertThat(response.statistics().totalUsersCreated()).isEqualTo(nUsers);
            assertThat(response.statistics().totalTweetsCreated()).isEqualTo(nUsers * nTweetsPerUser);
        }

        @Test
        void generateUsersAndTweets_WithNullNUsers_ShouldReturn400BadRequest() throws Exception {
            GenerateUsersAndTweetsRequestDto request = GenerateUsersAndTweetsRequestDto.builder()
                .nUsers(null)
                .nTweetsPerUser(5)
                .lUsersForDeletion(0)
                .build();

            mockMvc.perform(post("/api/v1/admin-scripts/generate-users-and-tweets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        void generateUsersAndTweets_WithNUsersExceedingMax_ShouldReturn400BadRequest() throws Exception {
            GenerateUsersAndTweetsRequestDto request = createValidRequest(1001, 5, 0);

            mockMvc.perform(post("/api/v1/admin-scripts/generate-users-and-tweets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        void generateUsersAndTweets_WithNTweetsPerUserLessThanOne_ShouldReturn400BadRequest() throws Exception {
            GenerateUsersAndTweetsRequestDto request = createValidRequest(5, 0, 0);

            mockMvc.perform(post("/api/v1/admin-scripts/generate-users-and-tweets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        void generateUsersAndTweets_WithNTweetsPerUserExceedingMax_ShouldReturn400BadRequest() throws Exception {
            GenerateUsersAndTweetsRequestDto request = createValidRequest(5, 101, 0);

            mockMvc.perform(post("/api/v1/admin-scripts/generate-users-and-tweets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        void generateUsersAndTweets_WithLUsersForDeletionNegative_ShouldReturn400BadRequest() throws Exception {
            GenerateUsersAndTweetsRequestDto request = createValidRequest(5, 5, -1);

            mockMvc.perform(post("/api/v1/admin-scripts/generate-users-and-tweets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        void generateUsersAndTweets_WithLUsersForDeletionExceedingUsersWithTweets_ShouldReturn400BadRequest() throws Exception {
            int nUsers = 2;
            int nTweetsPerUser = 3;
            int lUsersForDeletion = 5; // Exceeds nUsers

            GenerateUsersAndTweetsRequestDto request = createValidRequest(nUsers, nTweetsPerUser, lUsersForDeletion);

            List<UUID> userIds = new ArrayList<>();
            for (int i = 0; i < nUsers; i++) {
                UUID userId = UUID.randomUUID();
                userIds.add(userId);

                UserRequestDto userRequest = UserRequestDto.builder()
                    .login("user" + i)
                    .firstName("First" + i)
                    .lastName("Last" + i)
                    .email("user" + i + "@example.com")
                    .password("password123")
                    .build();

                UserResponseDto userResponse = new UserResponseDto(
                    userId,
                    "user" + i,
                    "First" + i,
                    "Last" + i,
                    "user" + i + "@example.com",
                    UserStatus.ACTIVE,
                    UserRole.USER,
                    LocalDateTime.now()
                );

                WireMockStubHelper.setupCreateUserStub(wireMockServer, objectMapper, userRequest, userResponse);
            }

            for (UUID userId : userIds) {
                List<TweetResponseDto> userTweets = new ArrayList<>();
                for (int j = 0; j < nTweetsPerUser; j++) {
                    UUID tweetId = UUID.randomUUID();

                    CreateTweetRequestDto createTweetRequest = CreateTweetRequestDto.builder()
                        .userId(userId)
                        .content("Tweet content " + j)
                        .build();

                    TweetResponseDto tweetResponse = new TweetResponseDto(
                        tweetId,
                        userId,
                        "Tweet content " + j,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        false,
                        null
                    );

                    WireMockStubHelper.setupCreateTweetStub(wireMockServer, objectMapper, createTweetRequest, tweetResponse);
                    userTweets.add(tweetResponse);
                }

                WireMockStubHelper.setupGetUserTweetsStub(wireMockServer, objectMapper, userId, userTweets);
            }

            String responseJson = mockMvc.perform(post("/api/v1/admin-scripts/generate-users-and-tweets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statistics").exists())
                .andExpect(jsonPath("$.statistics.errors").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

            GenerateUsersAndTweetsResponseDto response = objectMapper.readValue(responseJson, GenerateUsersAndTweetsResponseDto.class);
            assertThat(response.statistics().errors()).isNotEmpty();
            assertThat(response.statistics().errors().getFirst()).contains("DELETION_COUNT_EXCEEDS_USERS_WITH_TWEETS");
            assertThat(response.statistics().totalTweetsDeleted()).isEqualTo(0);
        }

        @Test
        void generateUsersAndTweets_WhenUsersApiReturns500_ShouldHandleGracefully() throws Exception {
            int nUsers = 2;
            int nTweetsPerUser = 3;
            int lUsersForDeletion = 0;

            GenerateUsersAndTweetsRequestDto request = createValidRequest(nUsers, nTweetsPerUser, lUsersForDeletion);

            WireMockStubHelper.setupCreateUserStubWithError(wireMockServer, 500);

            String responseJson = mockMvc.perform(post("/api/v1/admin-scripts/generate-users-and-tweets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Service handles errors gracefully
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

            GenerateUsersAndTweetsResponseDto response = objectMapper.readValue(responseJson, GenerateUsersAndTweetsResponseDto.class);
            assertThat(response.statistics().totalUsersCreated()).isEqualTo(0);
            assertThat(response.statistics().errors()).isNotEmpty();
        }

        @Test
        void generateUsersAndTweets_WhenTweetsApiReturns500_ShouldHandleGracefully() throws Exception {
            int nUsers = 1;
            int nTweetsPerUser = 3;
            int lUsersForDeletion = 0;

            GenerateUsersAndTweetsRequestDto request = createValidRequest(nUsers, nTweetsPerUser, lUsersForDeletion);

            UUID userId = UUID.randomUUID();
            UserRequestDto userRequest = UserRequestDto.builder()
                .login("user1")
                .firstName("First1")
                .lastName("Last1")
                .email("user1@example.com")
                .password("password123")
                .build();

            UserResponseDto userResponse = new UserResponseDto(
                userId,
                "user1",
                "First1",
                "Last1",
                "user1@example.com",
                UserStatus.ACTIVE,
                UserRole.USER,
                LocalDateTime.now()
            );

            WireMockStubHelper.setupCreateUserStub(wireMockServer, objectMapper, userRequest, userResponse);

            WireMockStubHelper.setupCreateTweetStubWithError(wireMockServer, 500);

            String responseJson = mockMvc.perform(post("/api/v1/admin-scripts/generate-users-and-tweets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Service handles errors gracefully
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

            GenerateUsersAndTweetsResponseDto response = objectMapper.readValue(responseJson, GenerateUsersAndTweetsResponseDto.class);
            assertThat(response.statistics().totalUsersCreated()).isEqualTo(1);
            assertThat(response.statistics().totalTweetsCreated()).isEqualTo(0);
            assertThat(response.statistics().errors()).isNotEmpty();
        }
    }
}

