package com.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.common.dto.request.LikeTweetRequestDto;
import com.twitter.common.dto.response.LikeResponseDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    /**
     * Creates and saves a like in the database for testing.
     *
     * @param tweetId the tweet ID
     * @param userId  the user ID
     * @return the saved Like entity
     */
    protected Like createAndSaveLike(UUID tweetId, UUID userId) {
        Like like = Like.builder()
            .tweetId(tweetId)
            .userId(userId)
            .build();
        return likeRepository.saveAndFlush(like);
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

            String responseJson = mockMvc.perform(post("/api/v1/tweets/{tweetId}/like", testTweetId)
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

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/like", testTweetId)
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

            int status = mockMvc.perform(post("/api/v1/tweets/{tweetId}/like", testTweetId)
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

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/like", testTweetId)
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

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/like", testTweetId)
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

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/like", testTweetId)
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

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/like", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            assertThat(likeRepository.count()).isEqualTo(1);
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(1);

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/like", testTweetId)
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

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/like", testTweetId)
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

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/like", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(1);

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/like", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

            tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(2);

            mockMvc.perform(post("/api/v1/tweets/{tweetId}/like", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isCreated());

            tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(3);
            assertThat(likeRepository.count()).isEqualTo(3);
        }
    }

    @Nested
    class RemoveLikeTests {

        private UUID testUserId;
        private UUID testTweetId;
        private UUID differentUserId;

        @BeforeEach
        void setUp() {
            testUserId = UUID.randomUUID();
            differentUserId = UUID.randomUUID();
        }

        @Test
        void removeLike_WithValidData_ShouldReturn204NoContent() throws Exception {
            String content = "Test tweet for unlike";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(differentUserId, true);

            Like savedLike = createAndSaveLike(testTweetId, differentUserId);
            savedTweet.setLikesCount(1);
            tweetRepository.saveAndFlush(savedTweet);

            LikeTweetRequestDto request = LikeTweetRequestDto.builder()
                .userId(differentUserId)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/like", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

            assertThat(likeRepository.findById(savedLike.getId())).isEmpty();
            Tweet updatedTweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(updatedTweet.getLikesCount()).isEqualTo(0);
        }

        @Test
        void removeLike_WithNullUserId_ShouldReturn400BadRequest() throws Exception {
            String content = "Test tweet for unlike";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            Like savedLike = createAndSaveLike(testTweetId, differentUserId);
            savedTweet.setLikesCount(1);
            tweetRepository.saveAndFlush(savedTweet);

            LikeTweetRequestDto request = LikeTweetRequestDto.builder()
                .userId(null)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/like", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            assertThat(likeRepository.findById(savedLike.getId())).isPresent();
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(1);
        }

        @Test
        void removeLike_WithMissingBody_ShouldReturn400BadRequest() throws Exception {
            String content = "Test tweet for unlike";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            Like savedLike = createAndSaveLike(testTweetId, differentUserId);
            savedTweet.setLikesCount(1);
            tweetRepository.saveAndFlush(savedTweet);

            int status = mockMvc.perform(delete("/api/v1/tweets/{tweetId}/like", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getStatus();

            assertThat(status).isGreaterThanOrEqualTo(400);
            assertThat(likeRepository.findById(savedLike.getId())).isPresent();
        }

        @Test
        void removeLike_WhenTweetDoesNotExist_ShouldReturn409Conflict() throws Exception {
            testTweetId = UUID.randomUUID();
            setupUserExistsStub(differentUserId, true);

            LikeTweetRequestDto request = LikeTweetRequestDto.builder()
                .userId(differentUserId)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/like", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("TWEET_NOT_FOUND"));

            assertThat(likeRepository.count()).isEqualTo(0);
        }

        @Test
        void removeLike_WhenUserDoesNotExist_ShouldReturn409Conflict() throws Exception {
            String content = "Test tweet for unlike";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(differentUserId, false);

            Like savedLike = createAndSaveLike(testTweetId, differentUserId);
            savedTweet.setLikesCount(1);
            tweetRepository.saveAndFlush(savedTweet);

            LikeTweetRequestDto request = LikeTweetRequestDto.builder()
                .userId(differentUserId)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/like", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("USER_NOT_EXISTS"));

            assertThat(likeRepository.findById(savedLike.getId())).isPresent();
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(1);
        }

        @Test
        void removeLike_WhenLikeDoesNotExist_ShouldReturn409Conflict() throws Exception {
            String content = "Test tweet for unlike";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStub(differentUserId, true);

            LikeTweetRequestDto request = LikeTweetRequestDto.builder()
                .userId(differentUserId)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/like", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("LIKE_NOT_FOUND"));

            assertThat(likeRepository.count()).isEqualTo(0);
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(0);
        }

        @Test
        void removeLike_ShouldDecrementLikesCount() throws Exception {
            String content = "Test tweet for unlike";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();
            UUID user3 = UUID.randomUUID();
            setupUserExistsStub(user1, true);
            setupUserExistsStub(user2, true);
            setupUserExistsStub(user3, true);

            Like like1 = createAndSaveLike(testTweetId, user1);
            Like like2 = createAndSaveLike(testTweetId, user2);
            Like like3 = createAndSaveLike(testTweetId, user3);
            savedTweet.setLikesCount(3);
            tweetRepository.saveAndFlush(savedTweet);

            LikeTweetRequestDto request1 = LikeTweetRequestDto.builder()
                .userId(user1)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/like", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isNoContent());

            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(2);
            assertThat(likeRepository.findById(like1.getId())).isEmpty();

            LikeTweetRequestDto request2 = LikeTweetRequestDto.builder()
                .userId(user2)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/like", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isNoContent());

            tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(1);
            assertThat(likeRepository.findById(like2.getId())).isEmpty();

            LikeTweetRequestDto request3 = LikeTweetRequestDto.builder()
                .userId(user3)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/like", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isNoContent());

            tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(0);
            assertThat(likeRepository.findById(like3.getId())).isEmpty();
            assertThat(likeRepository.count()).isEqualTo(0);
        }

        @Test
        void removeLike_WhenUsersApiReturns500_ShouldReturn409Conflict() throws Exception {
            String content = "Test tweet for unlike";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();
            setupUserExistsStubWithError(differentUserId, 500);

            Like savedLike = createAndSaveLike(testTweetId, differentUserId);
            savedTweet.setLikesCount(1);
            tweetRepository.saveAndFlush(savedTweet);

            LikeTweetRequestDto request = LikeTweetRequestDto.builder()
                .userId(differentUserId)
                .build();

            mockMvc.perform(delete("/api/v1/tweets/{tweetId}/like", testTweetId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists());

            assertThat(likeRepository.findById(savedLike.getId())).isPresent();
            Tweet tweet = tweetRepository.findById(testTweetId).orElseThrow();
            assertThat(tweet.getLikesCount()).isEqualTo(1);
        }
    }

    @Nested
    class GetLikesByTweetIdTests {

        private UUID testUserId;
        private UUID testTweetId;
        private UUID likeUserId1;
        private UUID likeUserId2;
        private UUID likeUserId3;

        @BeforeEach
        void setUp() {
            testUserId = UUID.randomUUID();
            likeUserId1 = UUID.randomUUID();
            likeUserId2 = UUID.randomUUID();
            likeUserId3 = UUID.randomUUID();
        }

        @Test
        void getLikesByTweetId_WhenLikesExist_ShouldReturn200Ok() throws Exception {
            String content = "Test tweet for likes";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            createAndSaveLike(testTweetId, likeUserId1);
            createAndSaveLike(testTweetId, likeUserId2);
            createAndSaveLike(testTweetId, likeUserId3);

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/likes", testTweetId))
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
        void getLikesByTweetId_WhenNoLikesExist_ShouldReturn200OkWithEmptyList() throws Exception {
            String content = "Test tweet without likes";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/likes", testTweetId))
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
        void getLikesByTweetId_WhenTweetDoesNotExist_ShouldReturn409Conflict() throws Exception {
            testTweetId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/likes", testTweetId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.ruleName").value("TWEET_NOT_FOUND"));
        }

        @Test
        void getLikesByTweetId_WithInvalidUuid_ShouldReturn400BadRequest() throws Exception {
            mockMvc.perform(get("/api/v1/tweets/{tweetId}/likes", "invalid-uuid"))
                .andExpect(status().isBadRequest());
        }

        @Test
        void getLikesByTweetId_WithPagination_ShouldReturnCorrectPage() throws Exception {
            String content = "Test tweet for pagination";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            for (int i = 1; i <= 25; i++) {
                createAndSaveLike(testTweetId, UUID.randomUUID());
            }

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/likes", testTweetId)
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

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/likes", testTweetId)
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

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/likes", testTweetId)
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
        void getLikesByTweetId_WithDefaultPagination_ShouldUseDefaultValues() throws Exception {
            String content = "Test tweet for default pagination";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            for (int i = 1; i <= 5; i++) {
                createAndSaveLike(testTweetId, UUID.randomUUID());
            }

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/likes", testTweetId))
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
        void getLikesByTweetId_ShouldReturnLikesSortedByCreatedAtDesc() throws Exception {
            String content = "Test tweet for sorting";
            Tweet savedTweet = createAndSaveTweet(testUserId, content);
            testTweetId = savedTweet.getId();

            Like like1 = createAndSaveLike(testTweetId, likeUserId1);
            Thread.sleep(10);
            Like like2 = createAndSaveLike(testTweetId, likeUserId2);
            Thread.sleep(10);
            Like like3 = createAndSaveLike(testTweetId, likeUserId3);

            mockMvc.perform(get("/api/v1/tweets/{tweetId}/likes", testTweetId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].id").value(like3.getId().toString()))
                .andExpect(jsonPath("$.content[1].id").value(like2.getId().toString()))
                .andExpect(jsonPath("$.content[2].id").value(like1.getId().toString()));
        }
    }
}
