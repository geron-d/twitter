package com.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.common.dto.request.tweet.CreateTweetRequestDto;
import com.twitter.common.dto.request.tweet.DeleteTweetRequestDto;
import com.twitter.common.dto.response.tweet.TweetResponseDto;
import com.twitter.dto.request.UpdateTweetRequestDto;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
     * @param userId  the user ID
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

        @Test
        void getTweetById_WhenTweetIsDeleted_ShouldReturn404NotFound() throws Exception {
            String content = "Tweet to be deleted";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            mockMvc.perform(get("/api/v1/tweets/{tweetId}", testTweetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTweetId.toString()));

            DeleteTweetRequestDto deleteRequest = DeleteTweetRequestDto.builder()
                .userId(testUserId)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isNoContent());

            Tweet deletedTweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(deletedTweet.getIsDeleted()).isTrue();
            assertThat(deletedTweet.getDeletedAt()).isNotNull();

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

    @Nested
    class DeleteTweetTests {

        private UUID testUserId;
        private UUID testTweetId;

        @BeforeEach
        void setUp() {
            testUserId = UUID.randomUUID();
        }

        @Test
        void deleteTweet_WithValidData_ShouldReturn204NoContent() throws Exception {
            String content = "Tweet to be deleted";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            DeleteTweetRequestDto request = DeleteTweetRequestDto.builder()
                .userId(testUserId)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

            Tweet deletedTweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(deletedTweet.getIsDeleted()).isTrue();
            assertThat(deletedTweet.getDeletedAt()).isNotNull();
        }

        @Test
        void deleteTweet_WhenTweetDoesNotExist_ShouldReturn409Conflict() throws Exception {
            testTweetId = UUID.randomUUID();
            DeleteTweetRequestDto request = DeleteTweetRequestDto.builder()
                .userId(testUserId)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("TWEET_NOT_FOUND"));
        }

        @Test
        void deleteTweet_WhenUserIsNotAuthor_ShouldReturn409Conflict() throws Exception {
            String content = "Tweet to be deleted";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            UUID differentUserId = UUID.randomUUID();
            DeleteTweetRequestDto request = DeleteTweetRequestDto.builder()
                .userId(differentUserId)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("TWEET_ACCESS_DENIED"));

            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getIsDeleted()).isFalse();
            assertThat(tweet.getDeletedAt()).isNull();
        }

        @Test
        void deleteTweet_WithNullUserId_ShouldReturn400BadRequest() throws Exception {
            String content = "Tweet to be deleted";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            DeleteTweetRequestDto request = DeleteTweetRequestDto.builder()
                .userId(null)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getIsDeleted()).isFalse();
        }
    }

    @Nested
    class GetUserTweetsTests {

        private UUID testUserId;

        @BeforeEach
        void setUp() {
            testUserId = UUID.randomUUID();
        }

        @Test
        void getUserTweets_WhenTweetsExist_ShouldReturn200Ok() throws Exception {
            createAndSaveTweet(testUserId, "First tweet");
            createAndSaveTweet(testUserId, "Second tweet");
            createAndSaveTweet(testUserId, "Third tweet");

            mockMvc.perform(get("/api/v1/tweets/user/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.content[0].content").exists())
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(1));
        }

        @Test
        void getUserTweets_WhenNoTweetsExist_ShouldReturn200OkWithEmptyList() throws Exception {
            mockMvc.perform(get("/api/v1/tweets/user/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(0));
        }

        @Test
        void getUserTweets_ShouldExcludeDeletedTweets() throws Exception {
            Tweet tweet1 = createAndSaveTweet(testUserId, "Active tweet");
            Tweet tweet2 = createAndSaveTweet(testUserId, "Tweet to be deleted");

            DeleteTweetRequestDto deleteRequest = DeleteTweetRequestDto.builder()
                .userId(testUserId)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}", tweet2.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/tweets/user/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(tweet1.getId().toString()))
                .andExpect(jsonPath("$.page.totalElements").value(1));
        }

        @Test
        void getUserTweets_ShouldSortByCreatedAtDesc() throws Exception {
            Tweet tweet1 = createAndSaveTweet(testUserId, "First tweet");
            Thread.sleep(10); // Ensure different timestamps
            Tweet tweet2 = createAndSaveTweet(testUserId, "Second tweet");
            Thread.sleep(10);
            Tweet tweet3 = createAndSaveTweet(testUserId, "Third tweet");

            String responseJson = mockMvc.perform(get("/api/v1/tweets/user/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

            // Parse response to verify order
            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseJson);
            var content = jsonNode.get("content");
            assertThat(content.isArray()).isTrue();
            assertThat(content.size()).isEqualTo(3);

            // First tweet should be the newest (third tweet)
            assertThat(content.get(0).get("id").asText()).isEqualTo(tweet3.getId().toString());
            assertThat(content.get(0).get("content").asText()).isEqualTo("Third tweet");

            // Second tweet should be the middle one
            assertThat(content.get(1).get("id").asText()).isEqualTo(tweet2.getId().toString());
            assertThat(content.get(1).get("content").asText()).isEqualTo("Second tweet");

            // Last tweet should be the oldest (first tweet)
            assertThat(content.get(2).get("id").asText()).isEqualTo(tweet1.getId().toString());
            assertThat(content.get(2).get("content").asText()).isEqualTo("First tweet");
        }

        @Test
        void getUserTweets_WithDefaultPagination_ShouldUseDefaultValues() throws Exception {
            for (int i = 1; i <= 5; i++) {
                createAndSaveTweet(testUserId, "Tweet " + i);
            }

            mockMvc.perform(get("/api/v1/tweets/user/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(5))
                .andExpect(jsonPath("$.page.totalPages").value(1));
        }
    }

    @Nested
    class GetTimelineTests {

        private UUID testUserId;
        private UUID followingUserId1;
        private UUID followingUserId2;

        @BeforeEach
        void setUp() {
            testUserId = UUID.randomUUID();
            followingUserId1 = UUID.randomUUID();
            followingUserId2 = UUID.randomUUID();
        }

        @Test
        void getTimeline_WhenFollowingUsersHaveTweets_ShouldReturn200Ok() throws Exception {
            setupUserExistsStub(testUserId, true);
            setupFollowingStub(testUserId, List.of(followingUserId1, followingUserId2), 0, 100);

            Tweet tweet1 = createAndSaveTweet(followingUserId1, "Tweet from followed user 1");
            Tweet tweet2 = createAndSaveTweet(followingUserId2, "Tweet from followed user 2");

            mockMvc.perform(get("/api/v1/tweets/timeline/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].userId").exists())
                .andExpect(jsonPath("$.content[0].content").exists())
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(1));
        }

        @Test
        void getTimeline_WhenNoFollowingUsers_ShouldReturn200OkWithEmptyList() throws Exception {
            setupUserExistsStub(testUserId, true);
            setupFollowingStubEmpty(testUserId);

            mockMvc.perform(get("/api/v1/tweets/timeline/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(0));
        }

        @Test
        void getTimeline_WhenFollowingUsersHaveNoTweets_ShouldReturn200OkWithEmptyList() throws Exception {
            setupUserExistsStub(testUserId, true);
            setupFollowingStub(testUserId, List.of(followingUserId1, followingUserId2), 0, 100);

            mockMvc.perform(get("/api/v1/tweets/timeline/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(0));
        }

        @Test
        void getTimeline_WithInvalidUserId_ShouldReturn400BadRequest() throws Exception {
            String invalidUserId = "invalid-uuid";

            mockMvc.perform(get("/api/v1/tweets/timeline/{userId}", invalidUserId))
                .andExpect(status().isBadRequest());
        }

        @Test
        void getTimeline_WhenUserDoesNotExist_ShouldReturn409Conflict() throws Exception {
            setupUserExistsStub(testUserId, false);

            mockMvc.perform(get("/api/v1/tweets/timeline/{userId}", testUserId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("USER_NOT_EXISTS"));
        }

        @Test
        void getTimeline_WhenFollowerApiReturns500_ShouldReturnEmptyPage() throws Exception {
            setupUserExistsStub(testUserId, true);
            setupFollowingStubWithError(testUserId, 500);

            mockMvc.perform(get("/api/v1/tweets/timeline/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(0));
        }

        @Test
        void getTimeline_ShouldExcludeDeletedTweets() throws Exception {
            setupUserExistsStub(testUserId, true);
            setupFollowingStub(testUserId, List.of(followingUserId1), 0, 100);

            Tweet activeTweet = createAndSaveTweet(followingUserId1, "Active tweet");
            Tweet tweetToDelete = createAndSaveTweet(followingUserId1, "Tweet to be deleted");

            DeleteTweetRequestDto deleteRequest = DeleteTweetRequestDto.builder()
                .userId(followingUserId1)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}", tweetToDelete.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/tweets/timeline/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(activeTweet.getId().toString()))
                .andExpect(jsonPath("$.page.totalElements").value(1));
        }

        @Test
        void getTimeline_ShouldSortByCreatedAtDesc() throws Exception {
            setupUserExistsStub(testUserId, true);
            setupFollowingStub(testUserId, List.of(followingUserId1), 0, 100);

            Tweet tweet1 = createAndSaveTweet(followingUserId1, "First tweet");
            Thread.sleep(10);
            Tweet tweet2 = createAndSaveTweet(followingUserId1, "Second tweet");
            Thread.sleep(10);
            Tweet tweet3 = createAndSaveTweet(followingUserId1, "Third tweet");

            String responseJson = mockMvc.perform(get("/api/v1/tweets/timeline/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseJson);
            var content = jsonNode.get("content");
            assertThat(content.isArray()).isTrue();
            assertThat(content.size()).isEqualTo(3);

            assertThat(content.get(0).get("id").asText()).isEqualTo(tweet3.getId().toString());
            assertThat(content.get(0).get("content").asText()).isEqualTo("Third tweet");

            assertThat(content.get(1).get("id").asText()).isEqualTo(tweet2.getId().toString());
            assertThat(content.get(1).get("content").asText()).isEqualTo("Second tweet");

            assertThat(content.get(2).get("id").asText()).isEqualTo(tweet1.getId().toString());
            assertThat(content.get(2).get("content").asText()).isEqualTo("First tweet");
        }
    }
}