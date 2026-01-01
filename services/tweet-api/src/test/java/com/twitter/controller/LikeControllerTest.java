package com.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.dto.request.LikeTweetRequestDto;
import com.twitter.dto.response.LikeResponseDto;
import com.twitter.entity.Like;
import com.twitter.entity.Tweet;
import com.twitter.repository.LikeRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class LikeControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
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

    @Nested
    class LikeTweetTests {

        private UUID testUserId;
        private UUID testTweetId;
        private UUID differentUserId;

        @BeforeEach
        void setUp() {
            testUserId = UUID.randomUUID();
            differentUserId = UUID.randomUUID();
        }

        @Test
        void likeTweet_WithValidData_ShouldReturn201Created() throws Exception {
            String content = "Test tweet for like";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(differentUserId, true);

            LikeTweetRequestDto request = LikeTweetRequestDto.builder()
                .userId(differentUserId)
                .build();

            String responseJson = mockMvc.perform(post("/api/v1/tweets/{tweetId}/likes", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tweetId").value(testTweetId.toString()))
                .andExpect(jsonPath("$.userId").value(differentUserId.toString()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

            LikeResponseDto responseDto = objectMapper.readValue(responseJson, LikeResponseDto.class);
            assertThat(responseDto.id()).isNotNull();
            assertThat(responseDto.tweetId()).isEqualTo(testTweetId);
            assertThat(responseDto.userId()).isEqualTo(differentUserId);
            assertThat(responseDto.createdAt()).isNotNull();

            Like savedLike = likeRepository.findById(responseDto.id()).orElseThrow();
            assertThat(savedLike.getTweetId()).isEqualTo(testTweetId);
            assertThat(savedLike.getUserId()).isEqualTo(differentUserId);

            Tweet updatedTweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(updatedTweet.getLikesCount()).isEqualTo(1);
        }

        @Test
        void likeTweet_WithNullUserId_ShouldReturn400BadRequest() throws Exception {
            String content = "Test tweet for like";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            LikeTweetRequestDto request = LikeTweetRequestDto.builder()
                .userId(null)
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/likes", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            assertThat(likeRepository.count()).isEqualTo(0);
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(0);
        }

        @Test
        void likeTweet_WithMissingBody_ShouldReturn400BadRequest() throws Exception {
            String content = "Test tweet for like";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            int status = mockMvc.perform(post("/api/v1/tweets/{tweetId}/likes", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getStatus();

            assertThat(status).isGreaterThanOrEqualTo(400);
            assertThat(likeRepository.count()).isEqualTo(0);
        }

        @Test
        void likeTweet_WhenTweetDoesNotExist_ShouldReturn409Conflict() throws Exception {
            testTweetId = UUID.randomUUID();
            setupUserExistsStub(differentUserId, true);

            LikeTweetRequestDto request = LikeTweetRequestDto.builder()
                .userId(differentUserId)
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/likes", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("TWEET_NOT_FOUND"));

            assertThat(likeRepository.count()).isEqualTo(0);
        }

        @Test
        void likeTweet_WhenUserDoesNotExist_ShouldReturn409Conflict() throws Exception {
            String content = "Test tweet for like";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(differentUserId, false);

            LikeTweetRequestDto request = LikeTweetRequestDto.builder()
                .userId(differentUserId)
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/likes", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("USER_NOT_EXISTS"));

            assertThat(likeRepository.count()).isEqualTo(0);
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(0);
        }

        @Test
        void likeTweet_WhenSelfLike_ShouldReturn409Conflict() throws Exception {
            String content = "Test tweet for like";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(testUserId, true);

            LikeTweetRequestDto request = LikeTweetRequestDto.builder()
                .userId(testUserId)
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/likes", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("SELF_LIKE_NOT_ALLOWED"));

            assertThat(likeRepository.count()).isEqualTo(0);
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(0);
        }

        @Test
        void likeTweet_WhenDuplicateLike_ShouldReturn409Conflict() throws Exception {
            String content = "Test tweet for like";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(differentUserId, true);

            LikeTweetRequestDto request = LikeTweetRequestDto.builder()
                .userId(differentUserId)
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/likes", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            assertThat(likeRepository.count()).isEqualTo(1);
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(1);

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/likes", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.fieldName").value("like"))
                .andExpect(jsonPath("$.fieldValue").exists());

            assertThat(likeRepository.count()).isEqualTo(1);
            tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(1);
        }

        @Test
        void likeTweet_WhenUsersApiReturns500_ShouldReturn409Conflict() throws Exception {
            String content = "Test tweet for like";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStubWithError(differentUserId, 500);

            LikeTweetRequestDto request = LikeTweetRequestDto.builder()
                .userId(differentUserId)
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/likes", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists());

            assertThat(likeRepository.count()).isEqualTo(0);
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(0);
        }

        @Test
        void likeTweet_ShouldIncrementLikesCount() throws Exception {
            String content = "Test tweet for like";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();
            UUID user3 = UUID.randomUUID();
            setupUserExistsStub(user1, true);
            setupUserExistsStub(user2, true);
            setupUserExistsStub(user3, true);

            LikeTweetRequestDto request1 = LikeTweetRequestDto.builder()
                .userId(user1)
                .build();
            LikeTweetRequestDto request2 = LikeTweetRequestDto.builder()
                .userId(user2)
                .build();
            LikeTweetRequestDto request3 = LikeTweetRequestDto.builder()
                .userId(user3)
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/likes", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(1);

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/likes", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

            tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(2);

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/likes", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isCreated());

            tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(3);
            assertThat(likeRepository.count()).isEqualTo(3);
        }
    }
}

