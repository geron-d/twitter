package com.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.request.UpdateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import com.twitter.entity.Tweet;
import com.twitter.repository.TweetRepository;
import com.twitter.testconfig.BaseIntegrationTest;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class TweetControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
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

    /**
     * Creates and saves a tweet in the database for testing.
     *
     * @param userId  the user ID
     * @param content the tweet content
     * @return the saved Tweet entity
     */
    protected Tweet createAndSaveTweet(UUID userId, String content) {
        Tweet tweet = Tweet.builder()
            .userId(userId)
            .content(content)
            .build();
        return tweetRepository.saveAndFlush(tweet);
    }

    /**
     * Creates a valid UpdateTweetRequestDto for testing.
     *
     * @param userId  the user ID
     * @param content the updated tweet content
     * @return UpdateTweetRequestDto instance
     */
    protected UpdateTweetRequestDto createUpdateRequest(UUID userId, String content) {
        return UpdateTweetRequestDto.builder()
            .userId(userId)
            .content(content)
            .build();
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

            assertThat(verifyTweetInDatabase(responseDto.id())).isTrue();
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

        @Test
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

    @Nested
    class GetTweetByIdTests {

        private UUID testUserId;
        private UUID testTweetId;

        @BeforeEach
        void setUp() {
            testUserId = UUID.randomUUID();
        }

        @Test
        void getTweetById_WhenTweetExists_ShouldReturn200Ok() throws Exception {
            String content = "Test tweet content";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            mockMvc.perform(get("/api/v1/tweets/{tweetId}", testTweetId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testTweetId.toString()))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        void getTweetById_WhenTweetExists_ShouldReturnCorrectTweetData() throws Exception {
            String content = "Another test tweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            String responseJson = mockMvc.perform(get("/api/v1/tweets/{tweetId}", testTweetId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

            TweetResponseDto responseDto = objectMapper.readValue(responseJson, TweetResponseDto.class);
            assertThat(responseDto.id()).isEqualTo(testTweetId);
            assertThat(responseDto.userId()).isEqualTo(testUserId);
            assertThat(responseDto.content()).isEqualTo(content);
            assertThat(responseDto.createdAt()).isNotNull();
            assertThat(responseDto.updatedAt()).isNotNull();
        }

        @Test
        void getTweetById_WhenTweetDoesNotExist_ShouldReturn404NotFound() throws Exception {
            testTweetId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/tweets/{tweetId}", testTweetId))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateTweetTests {

        private UUID testUserId;
        private UUID testTweetId;

        @BeforeEach
        void setUp() {
            testUserId = UUID.randomUUID();
            String originalContent = "Original tweet content";
            Tweet existingTweet = createAndSaveTweet(testUserId, originalContent);
            testTweetId = existingTweet.getId();
        }

        @Test
        void updateTweet_WithValidData_ShouldReturn200Ok() throws Exception {
            String updatedContent = "Updated tweet content";
            UpdateTweetRequestDto request = createUpdateRequest(testUserId, updatedContent);

            String responseJson = mockMvc.perform(put("/api/v1/tweets/{tweetId}", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testTweetId.toString()))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.content").value(updatedContent))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

            TweetResponseDto responseDto = objectMapper.readValue(responseJson, TweetResponseDto.class);
            assertThat(responseDto.id()).isEqualTo(testTweetId);
            assertThat(responseDto.userId()).isEqualTo(testUserId);
            assertThat(responseDto.content()).isEqualTo(updatedContent);
            assertThat(responseDto.createdAt()).isNotNull();
            assertThat(responseDto.updatedAt()).isNotNull();

            Tweet updatedTweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(updatedTweet.getContent()).isEqualTo(updatedContent);
            assertThat(updatedTweet.getId()).isEqualTo(testTweetId);
            assertThat(updatedTweet.getUserId()).isEqualTo(testUserId);
        }

        @Test
        void updateTweet_WithEmptyContent_ShouldReturn400BadRequest() throws Exception {
            UpdateTweetRequestDto request = createUpdateRequest(testUserId, "");

            mockMvc.perform(put("/api/v1/tweets/{tweetId}", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getContent()).isEqualTo("Original tweet content");
        }

        @Test
        void updateTweet_WithContentExceedingMaxLength_ShouldReturn400BadRequest() throws Exception {
            String longContent = "A".repeat(281);
            UpdateTweetRequestDto request = createUpdateRequest(testUserId, longContent);

            mockMvc.perform(put("/api/v1/tweets/{tweetId}", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getContent()).isEqualTo("Original tweet content");
        }

        @Test
        void updateTweet_WithNullUserId_ShouldReturn400BadRequest() throws Exception {
            UpdateTweetRequestDto request = UpdateTweetRequestDto.builder()
                .userId(null)
                .content("Valid content")
                .build();

            mockMvc.perform(put("/api/v1/tweets/{tweetId}", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getContent()).isEqualTo("Original tweet content");
        }

        @Test
        void updateTweet_WhenTweetDoesNotExist_ShouldReturn409Conflict() throws Exception {
            UUID nonExistentTweetId = UUID.randomUUID();
            UpdateTweetRequestDto request = createUpdateRequest(testUserId, "Updated content");

            mockMvc.perform(put("/api/v1/tweets/{tweetId}", nonExistentTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("TWEET_NOT_FOUND"));
        }

        @Test
        void updateTweet_WhenUserIsNotAuthor_ShouldReturn409Conflict() throws Exception {
            UUID differentUserId = UUID.randomUUID();
            UpdateTweetRequestDto request = createUpdateRequest(differentUserId, "Updated content");

            mockMvc.perform(put("/api/v1/tweets/{tweetId}", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("TWEET_ACCESS_DENIED"));

            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getContent()).isEqualTo("Original tweet content");
        }

        @Test
        void updateTweet_WithMissingBody_ShouldReturn400BadRequest() throws Exception {
            int status = mockMvc.perform(put("/api/v1/tweets/{tweetId}", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getStatus();

            assertThat(status).isGreaterThanOrEqualTo(400);
        }
    }
}

