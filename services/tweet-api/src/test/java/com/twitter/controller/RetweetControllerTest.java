package com.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.common.dto.request.retweet.RetweetRequestDto;
import com.twitter.common.dto.response.retweet.RetweetResponseDto;
import com.twitter.entity.Retweet;
import com.twitter.entity.Tweet;
import com.twitter.repository.RetweetRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class RetweetControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private RetweetRepository retweetRepository;

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

    /**
     * Creates and saves a retweet in the database for testing.
     *
     * @param tweetId the tweet ID
     * @param userId  the user ID
     * @param comment optional comment for the retweet
     * @return the saved Retweet entity
     */
    protected Retweet createAndSaveRetweet(UUID tweetId, UUID userId, String comment) {
        Retweet retweet = Retweet.builder()
            .tweetId(tweetId)
            .userId(userId)
            .comment(comment)
            .build();
        return retweetRepository.saveAndFlush(retweet);
    }

    @Nested
    class RetweetTweetTests {

        private UUID testUserId;
        private UUID testTweetId;
        private UUID differentUserId;

        @BeforeEach
        void setUp() {
            testUserId = UUID.randomUUID();
            differentUserId = UUID.randomUUID();
        }

        @Test
        void retweetTweet_WithValidDataWithoutComment_ShouldReturn201Created() throws Exception {
            String content = "Test tweet for retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(differentUserId, true);

            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(differentUserId)
                .comment(null)
                .build();

            String responseJson = mockMvc.perform(post("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tweetId").value(testTweetId.toString()))
                .andExpect(jsonPath("$.userId").value(differentUserId.toString()))
                .andExpect(jsonPath("$.comment").isEmpty())
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

            RetweetResponseDto responseDto = objectMapper.readValue(responseJson, RetweetResponseDto.class);
            assertThat(responseDto.id()).isNotNull();
            assertThat(responseDto.tweetId()).isEqualTo(testTweetId);
            assertThat(responseDto.userId()).isEqualTo(differentUserId);
            assertThat(responseDto.comment()).isNull();
            assertThat(responseDto.createdAt()).isNotNull();

            Retweet savedRetweet = retweetRepository.findById(responseDto.id()).orElseThrow();
            assertThat(savedRetweet.getTweetId()).isEqualTo(testTweetId);
            assertThat(savedRetweet.getUserId()).isEqualTo(differentUserId);
            assertThat(savedRetweet.getComment()).isNull();

            Tweet updatedTweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(updatedTweet.getRetweetsCount()).isEqualTo(1);
        }

        @Test
        void retweetTweet_WithValidDataWithComment_ShouldReturn201Created() throws Exception {
            String content = "Test tweet for retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(differentUserId, true);

            String comment = "Great tweet!";
            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(differentUserId)
                .comment(comment)
                .build();

            String responseJson = mockMvc.perform(post("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tweetId").value(testTweetId.toString()))
                .andExpect(jsonPath("$.userId").value(differentUserId.toString()))
                .andExpect(jsonPath("$.comment").value(comment))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

            RetweetResponseDto responseDto = objectMapper.readValue(responseJson, RetweetResponseDto.class);
            assertThat(responseDto.id()).isNotNull();
            assertThat(responseDto.tweetId()).isEqualTo(testTweetId);
            assertThat(responseDto.userId()).isEqualTo(differentUserId);
            assertThat(responseDto.comment()).isEqualTo(comment);
            assertThat(responseDto.createdAt()).isNotNull();

            Retweet savedRetweet = retweetRepository.findById(responseDto.id()).orElseThrow();
            assertThat(savedRetweet.getTweetId()).isEqualTo(testTweetId);
            assertThat(savedRetweet.getUserId()).isEqualTo(differentUserId);
            assertThat(savedRetweet.getComment()).isEqualTo(comment);

            Tweet updatedTweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(updatedTweet.getRetweetsCount()).isEqualTo(1);
        }

        @Test
        void retweetTweet_WithNullUserId_ShouldReturn400BadRequest() throws Exception {
            String content = "Test tweet for retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(null)
                .comment(null)
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            assertThat(retweetRepository.count()).isEqualTo(0);
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(0);
        }

        @Test
        void retweetTweet_WithMissingBody_ShouldReturn400BadRequest() throws Exception {
            String content = "Test tweet for retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            int status = mockMvc.perform(post("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getStatus();

            assertThat(status).isGreaterThanOrEqualTo(400);
            assertThat(retweetRepository.count()).isEqualTo(0);
        }

        @Test
        void retweetTweet_WithEmptyComment_ShouldReturn400BadRequest() throws Exception {
            String content = "Test tweet for retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(differentUserId, true);

            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(differentUserId)
                .comment("")
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            assertThat(retweetRepository.count()).isEqualTo(0);
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(0);
        }

        @Test
        void retweetTweet_WithCommentExceedingMaxLength_ShouldReturn400BadRequest() throws Exception {
            String content = "Test tweet for retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(differentUserId, true);

            String longComment = "A".repeat(281);
            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(differentUserId)
                .comment(longComment)
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            assertThat(retweetRepository.count()).isEqualTo(0);
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(0);
        }

        @Test
        void retweetTweet_WhenTweetDoesNotExist_ShouldReturn409Conflict() throws Exception {
            testTweetId = UUID.randomUUID();
            setupUserExistsStub(differentUserId, true);

            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(differentUserId)
                .comment(null)
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("TWEET_NOT_FOUND"));

            assertThat(retweetRepository.count()).isEqualTo(0);
        }

        @Test
        void retweetTweet_WhenUserDoesNotExist_ShouldReturn409Conflict() throws Exception {
            String content = "Test tweet for retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(differentUserId, false);

            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(differentUserId)
                .comment(null)
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("USER_NOT_EXISTS"));

            assertThat(retweetRepository.count()).isEqualTo(0);
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(0);
        }

        @Test
        void retweetTweet_WhenSelfRetweet_ShouldReturn409Conflict() throws Exception {
            String content = "Test tweet for retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(testUserId, true);

            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(testUserId)
                .comment(null)
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("SELF_RETWEET_NOT_ALLOWED"));

            assertThat(retweetRepository.count()).isEqualTo(0);
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(0);
        }

        @Test
        void retweetTweet_WhenDuplicateRetweet_ShouldReturn409Conflict() throws Exception {
            String content = "Test tweet for retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(differentUserId, true);

            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(differentUserId)
                .comment(null)
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            assertThat(retweetRepository.count()).isEqualTo(1);
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(1);

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.fieldName").value("retweet"))
                .andExpect(jsonPath("$.fieldValue").exists());

            assertThat(retweetRepository.count()).isEqualTo(1);
            tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(1);
        }

        @Test
        void retweetTweet_WhenUsersApiReturns500_ShouldReturn409Conflict() throws Exception {
            String content = "Test tweet for retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStubWithError(differentUserId, 500);

            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(differentUserId)
                .comment(null)
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists());

            assertThat(retweetRepository.count()).isEqualTo(0);
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(0);
        }

        @Test
        void retweetTweet_ShouldIncrementRetweetsCount() throws Exception {
            String content = "Test tweet for retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();
            UUID user3 = UUID.randomUUID();
            setupUserExistsStub(user1, true);
            setupUserExistsStub(user2, true);
            setupUserExistsStub(user3, true);

            RetweetRequestDto request1 = RetweetRequestDto.builder()
                .userId(user1)
                .comment(null)
                .build();
            RetweetRequestDto request2 = RetweetRequestDto.builder()
                .userId(user2)
                .comment("Nice!")
                .build();
            RetweetRequestDto request3 = RetweetRequestDto.builder()
                .userId(user3)
                .comment("Great tweet!")
                .build();

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(1);

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

            tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(2);

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isCreated());

            tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(3);
            assertThat(retweetRepository.count()).isEqualTo(3);
        }
    }

    @Nested
    class RemoveRetweetTests {

        private UUID testUserId;
        private UUID testTweetId;
        private UUID differentUserId;

        @BeforeEach
        void setUp() {
            testUserId = UUID.randomUUID();
            differentUserId = UUID.randomUUID();
        }

        @Test
        void removeRetweet_WithValidData_ShouldReturn204NoContent() throws Exception {
            String content = "Test tweet for remove retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(differentUserId, true);

            Retweet savedRetweet = createAndSaveRetweet(testTweetId, differentUserId, null);
            savedTweet.setRetweetsCount(1);
            tweetRepository.saveAndFlush(savedTweet);

            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(differentUserId)
                .comment(null)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

            assertThat(retweetRepository.findById(savedRetweet.getId())).isEmpty();
            Tweet updatedTweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(updatedTweet.getRetweetsCount()).isEqualTo(0);
        }

        @Test
        void removeRetweet_WithNullUserId_ShouldReturn400BadRequest() throws Exception {
            String content = "Test tweet for remove retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            Retweet savedRetweet = createAndSaveRetweet(testTweetId, differentUserId, null);
            savedTweet.setRetweetsCount(1);
            tweetRepository.saveAndFlush(savedTweet);

            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(null)
                .comment(null)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            assertThat(retweetRepository.findById(savedRetweet.getId())).isPresent();
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(1);
        }

        @Test
        void removeRetweet_WithMissingBody_ShouldReturn400BadRequest() throws Exception {
            String content = "Test tweet for remove retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            Retweet savedRetweet = createAndSaveRetweet(testTweetId, differentUserId, null);
            savedTweet.setRetweetsCount(1);
            tweetRepository.saveAndFlush(savedTweet);

            int status = mockMvc.perform(delete("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getStatus();

            assertThat(status).isGreaterThanOrEqualTo(400);
            assertThat(retweetRepository.findById(savedRetweet.getId())).isPresent();
        }

        @Test
        void removeRetweet_WhenTweetDoesNotExist_ShouldReturn409Conflict() throws Exception {
            testTweetId = UUID.randomUUID();
            setupUserExistsStub(differentUserId, true);

            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(differentUserId)
                .comment(null)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("TWEET_NOT_FOUND"));

            assertThat(retweetRepository.count()).isEqualTo(0);
        }

        @Test
        void removeRetweet_WhenUserDoesNotExist_ShouldReturn409Conflict() throws Exception {
            String content = "Test tweet for remove retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(differentUserId, false);

            Retweet savedRetweet = createAndSaveRetweet(testTweetId, differentUserId, null);
            savedTweet.setRetweetsCount(1);
            tweetRepository.saveAndFlush(savedTweet);

            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(differentUserId)
                .comment(null)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("USER_NOT_EXISTS"));

            assertThat(retweetRepository.findById(savedRetweet.getId())).isPresent();
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(1);
        }

        @Test
        void removeRetweet_WhenRetweetDoesNotExist_ShouldReturn409Conflict() throws Exception {
            String content = "Test tweet for remove retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(differentUserId, true);

            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(differentUserId)
                .comment(null)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("RETWEET_NOT_FOUND"));

            assertThat(retweetRepository.count()).isEqualTo(0);
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(0);
        }

        @Test
        void removeRetweet_ShouldDecrementRetweetsCount() throws Exception {
            String content = "Test tweet for remove retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();
            UUID user3 = UUID.randomUUID();
            setupUserExistsStub(user1, true);
            setupUserExistsStub(user2, true);
            setupUserExistsStub(user3, true);

            Retweet retweet1 = createAndSaveRetweet(testTweetId, user1, null);
            Retweet retweet2 = createAndSaveRetweet(testTweetId, user2, "Nice!");
            Retweet retweet3 = createAndSaveRetweet(testTweetId, user3, "Great tweet!");
            savedTweet.setRetweetsCount(3);
            tweetRepository.saveAndFlush(savedTweet);

            RetweetRequestDto request1 = RetweetRequestDto.builder()
                .userId(user1)
                .comment(null)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isNoContent());

            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(2);
            assertThat(retweetRepository.findById(retweet1.getId())).isEmpty();

            RetweetRequestDto request2 = RetweetRequestDto.builder()
                .userId(user2)
                .comment(null)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isNoContent());

            tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(1);
            assertThat(retweetRepository.findById(retweet2.getId())).isEmpty();

            RetweetRequestDto request3 = RetweetRequestDto.builder()
                .userId(user3)
                .comment(null)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isNoContent());

            tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(0);
            assertThat(retweetRepository.findById(retweet3.getId())).isEmpty();
            assertThat(retweetRepository.count()).isEqualTo(0);
        }

        @Test
        void removeRetweet_WhenUsersApiReturns500_ShouldReturn409Conflict() throws Exception {
            String content = "Test tweet for remove retweet";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStubWithError(differentUserId, 500);

            Retweet savedRetweet = createAndSaveRetweet(testTweetId, differentUserId, null);
            savedTweet.setRetweetsCount(1);
            tweetRepository.saveAndFlush(savedTweet);

            RetweetRequestDto request = RetweetRequestDto.builder()
                .userId(differentUserId)
                .comment(null)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/retweet", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists());

            assertThat(retweetRepository.findById(savedRetweet.getId())).isPresent();
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getRetweetsCount()).isEqualTo(1);
        }
    }

    @Nested
    class GetRetweetsByTweetIdTests {

        private UUID testUserId;
        private UUID testTweetId;
        private UUID retweetUserId1;
        private UUID retweetUserId2;
        private UUID retweetUserId3;

        @BeforeEach
        void setUp() {
            testUserId = UUID.randomUUID();
            retweetUserId1 = UUID.randomUUID();
            retweetUserId2 = UUID.randomUUID();
            retweetUserId3 = UUID.randomUUID();
        }

        @Test
        void getRetweetsByTweetId_WhenRetweetsExist_ShouldReturn200Ok() throws Exception {
            String content = "Test tweet for retweets";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            createAndSaveRetweet(testTweetId, retweetUserId1, "Great tweet!");
            createAndSaveRetweet(testTweetId, retweetUserId2, null);
            createAndSaveRetweet(testTweetId, retweetUserId3, "Nice!");

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/retweets", testTweetId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].tweetId").value(testTweetId.toString()))
                .andExpect(jsonPath("$.content[0].userId").exists())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].createdAt").exists())
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(1));
        }

        @Test
        void getRetweetsByTweetId_WhenNoRetweetsExist_ShouldReturn200OkWithEmptyList() throws Exception {
            String content = "Test tweet without retweets";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/retweets", testTweetId))
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
        void getRetweetsByTweetId_WhenTweetDoesNotExist_ShouldReturn409Conflict() throws Exception {
            testTweetId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/retweets", testTweetId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("TWEET_NOT_FOUND"));
        }

        @Test
        void getRetweetsByTweetId_WithInvalidUuid_ShouldReturn400BadRequest() throws Exception {
            mockMvc.perform(get("/api/v1/tweets/{tweetId}/retweets", "invalid-uuid"))
                .andExpect(status().isBadRequest());
        }

        @Test
        void getRetweetsByTweetId_WithPagination_ShouldReturnCorrectPage() throws Exception {
            String content = "Test tweet for pagination";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            for (int i = 1; i <= 25; i++) {
                createAndSaveRetweet(testTweetId, UUID.randomUUID(), null);
            }

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/retweets", testTweetId)
                    .param("page", "0")
                    .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(25))
                .andExpect(jsonPath("$.page.totalPages").value(3));

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/retweets", testTweetId)
                    .param("page", "1")
                    .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(25))
                .andExpect(jsonPath("$.page.totalPages").value(3));

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/retweets", testTweetId)
                    .param("page", "2")
                    .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.number").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(25))
                .andExpect(jsonPath("$.page.totalPages").value(3));
        }

        @Test
        void getRetweetsByTweetId_WithDefaultPagination_ShouldUseDefaultValues() throws Exception {
            String content = "Test tweet for default pagination";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            for (int i = 1; i <= 5; i++) {
                createAndSaveRetweet(testTweetId, UUID.randomUUID(), null);
            }

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/retweets", testTweetId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(5))
                .andExpect(jsonPath("$.page.totalPages").value(1));
        }

        @Test
        void getRetweetsByTweetId_ShouldReturnRetweetsSortedByCreatedAtDesc() throws Exception {
            String content = "Test tweet for sorting";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            Retweet retweet1 = createAndSaveRetweet(testTweetId, retweetUserId1, "First");
            Thread.sleep(10);
            Retweet retweet2 = createAndSaveRetweet(testTweetId, retweetUserId2, "Second");
            Thread.sleep(10);
            Retweet retweet3 = createAndSaveRetweet(testTweetId, retweetUserId3, "Third");

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/retweets", testTweetId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].id").value(retweet3.getId().toString()))
                .andExpect(jsonPath("$.content[1].id").value(retweet2.getId().toString()))
                .andExpect(jsonPath("$.content[2].id").value(retweet1.getId().toString()));
        }
    }
}