package com.twitter.testconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.twitter.common.dto.request.CreateTweetRequestDto;
import com.twitter.common.dto.request.FollowRequestDto;
import com.twitter.common.dto.request.LikeTweetRequestDto;
import com.twitter.common.dto.request.RetweetRequestDto;
import com.twitter.common.dto.request.UserRequestDto;
import com.twitter.common.dto.response.FollowResponseDto;
import com.twitter.common.dto.response.LikeResponseDto;
import com.twitter.common.dto.response.RetweetResponseDto;
import com.twitter.common.dto.response.TweetResponseDto;
import com.twitter.common.dto.response.UserResponseDto;
import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Builder class for setting up WireMock stubs in BaseScriptControllerTest.
 * <p>
 * This class provides methods to create test data (DTOs) and configure WireMock stubs
 * for testing the base-script endpoint. It encapsulates the repetitive
 * logic of creating users and tweets test data and setting up corresponding stubs.
 * <p>
 * The builder uses instance-based approach for better flexibility and testability.
 *
 * @author geron
 * @version 1.0
 */
public final class BaseScriptTestStubBuilder {

    private final WireMockServer wireMockServer;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new instance of the builder.
     *
     * @param wireMockServer the WireMock server instance for setting up stubs
     * @param objectMapper   the ObjectMapper for JSON serialization
     */
    public BaseScriptTestStubBuilder(WireMockServer wireMockServer, ObjectMapper objectMapper) {
        this.wireMockServer = wireMockServer;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a UserRequestDto for testing.
     *
     * @param index the index of the user (used for generating unique data)
     * @return UserRequestDto instance with test data
     */
    private UserRequestDto createUserRequest(int index) {
        return UserRequestDto.builder()
            .login("user" + index)
            .firstName("First" + index)
            .lastName("Last" + index)
            .email("user" + index + "@example.com")
            .password("password123")
            .build();
    }

    /**
     * Creates a UserResponseDto for testing.
     *
     * @param userId the user ID
     * @param index  the index of the user (used for generating unique data)
     * @return UserResponseDto instance with test data
     */
    private UserResponseDto createUserResponse(UUID userId, int index) {
        return new UserResponseDto(
            userId,
            "user" + index,
            "First" + index,
            "Last" + index,
            "user" + index + "@example.com",
            UserStatus.ACTIVE,
            UserRole.USER,
            LocalDateTime.now()
        );
    }

    /**
     * Creates a CreateTweetRequestDto for testing.
     *
     * @param userId     the user ID who owns the tweet
     * @param tweetIndex the index of the tweet (used for generating unique content)
     * @return CreateTweetRequestDto instance with test data
     */
    private CreateTweetRequestDto createTweetRequest(UUID userId, int tweetIndex) {
        return CreateTweetRequestDto.builder()
            .userId(userId)
            .content("Tweet content " + tweetIndex)
            .build();
    }

    /**
     * Creates a TweetResponseDto for testing.
     *
     * @param tweetId    the tweet ID
     * @param userId     the user ID who owns the tweet
     * @param tweetIndex the index of the tweet (used for generating unique content)
     * @return TweetResponseDto instance with test data
     */
    private TweetResponseDto createTweetResponse(UUID tweetId, UUID userId, int tweetIndex) {
        return new TweetResponseDto(
            tweetId,
            userId,
            "Tweet content " + tweetIndex,
            LocalDateTime.now(),
            LocalDateTime.now(),
            false,
            null
        );
    }

    /**
     * Sets up WireMock stubs for creating N users.
     * <p>
     * This method creates test data for N users and configures WireMock stubs
     * for the POST /api/v1/users endpoint for each user.
     *
     * @param nUsers the number of users to create
     * @return list of generated user IDs
     */
    public List<UUID> setupUsersStubs(int nUsers) {
        List<UUID> userIds = new ArrayList<>();
        for (int i = 0; i < nUsers; i++) {
            UUID userId = UUID.randomUUID();
            userIds.add(userId);

            UserRequestDto userRequest = createUserRequest(i);
            UserResponseDto userResponse = createUserResponse(userId, i);

            WireMockStubHelper.setupCreateUserStub(wireMockServer, objectMapper, userRequest, userResponse);
        }
        return userIds;
    }

    /**
     * Sets up WireMock stubs for creating N tweets for each user.
     * <p>
     * This method creates test data for N tweets per user and configures WireMock stubs
     * for the POST /api/v1/tweets endpoint for each tweet.
     *
     * @param userIds        list of user IDs to create tweets for
     * @param nTweetsPerUser number of tweets to create per user
     * @return map of user ID to list of tweet IDs for that user
     */
    public Map<UUID, List<UUID>> setupTweetsStubs(List<UUID> userIds, int nTweetsPerUser) {
        Map<UUID, List<UUID>> userTweetsMap = new HashMap<>();
        for (UUID userId : userIds) {
            List<UUID> tweetIds = new ArrayList<>();
            for (int j = 0; j < nTweetsPerUser; j++) {
                UUID tweetId = UUID.randomUUID();
                tweetIds.add(tweetId);

                CreateTweetRequestDto createTweetRequest = createTweetRequest(userId, j);
                TweetResponseDto tweetResponse = createTweetResponse(tweetId, userId, j);

                WireMockStubHelper.setupCreateTweetStub(wireMockServer, objectMapper, createTweetRequest, tweetResponse);
            }
            userTweetsMap.put(userId, tweetIds);
        }
        return userTweetsMap;
    }

    /**
     * Sets up WireMock stubs for getting user tweets.
     * <p>
     * This method configures WireMock stubs for the GET /api/v1/tweets/user/{userId}
     * endpoint for each user, returning the tweets that were created for that user.
     *
     * @param userTweetsMap map of user ID to list of tweet IDs for that user
     */
    public void setupGetUserTweetsStubs(Map<UUID, List<UUID>> userTweetsMap) {
        for (Map.Entry<UUID, List<UUID>> entry : userTweetsMap.entrySet()) {
            UUID userId = entry.getKey();
            List<UUID> tweetIds = entry.getValue();
            List<TweetResponseDto> userTweets = new ArrayList<>();
            for (int j = 0; j < tweetIds.size(); j++) {
                UUID tweetId = tweetIds.get(j);
                TweetResponseDto tweetResponse = createTweetResponse(tweetId, userId, j);
                userTweets.add(tweetResponse);
            }
            WireMockStubHelper.setupGetUserTweetsStub(wireMockServer, objectMapper, userId, userTweets);
        }
    }

    /**
     * Sets up WireMock stub for deleting a tweet.
     * <p>
     * This method configures a WireMock stub for the DELETE /api/v1/tweets/{tweetId}
     * endpoint.
     *
     * @param tweetId the tweet ID to delete
     */
    public void setupDeleteTweetStub(UUID tweetId) {
        WireMockStubHelper.setupDeleteTweetStub(wireMockServer, tweetId);
    }

    /**
     * Sets up WireMock stub for user creation error.
     * <p>
     * This method configures a WireMock stub for the POST /api/v1/users endpoint
     * that returns an error response with the specified HTTP status code.
     *
     * @param statusCode the HTTP status code to return
     */
    public void setupUserCreationError(int statusCode) {
        WireMockStubHelper.setupCreateUserStubWithError(wireMockServer, statusCode);
    }

    /**
     * Sets up WireMock stub for tweet creation error.
     * <p>
     * This method configures a WireMock stub for the POST /api/v1/tweets endpoint
     * that returns an error response with the specified HTTP status code.
     *
     * @param statusCode the HTTP status code to return
     */
    public void setupTweetCreationError(int statusCode) {
        WireMockStubHelper.setupCreateTweetStubWithError(wireMockServer, statusCode);
    }

    /**
     * Creates a FollowRequestDto for testing.
     *
     * @param followerId  the ID of the user who is following
     * @param followingId the ID of the user being followed
     * @return FollowRequestDto instance with test data
     */
    private FollowRequestDto createFollowRequest(UUID followerId, UUID followingId) {
        return FollowRequestDto.builder()
            .followerId(followerId)
            .followingId(followingId)
            .build();
    }

    /**
     * Creates a FollowResponseDto for testing.
     *
     * @param followId    the follow relationship ID
     * @param followerId  the ID of the user who is following
     * @param followingId the ID of the user being followed
     * @return FollowResponseDto instance with test data
     */
    private FollowResponseDto createFollowResponse(UUID followId, UUID followerId, UUID followingId) {
        return FollowResponseDto.builder()
            .id(followId)
            .followerId(followerId)
            .followingId(followingId)
            .createdAt(LocalDateTime.now())
            .build();
    }

    /**
     * Sets up WireMock stubs for creating follow relationships.
     * <p>
     * This method creates test data for follow relationships and configures WireMock stubs
     * for the POST /api/v1/follows endpoint for each follow relationship.
     * <p>
     * The method creates follow relationships according to the logic:
     * - Central user (first user) follows half of the remaining users
     * - Half of the remaining users follow the central user
     * - Uses integer division (rounding down) to calculate half count
     * <p>
     * Since the actual service uses Collections.shuffle() which makes the order unpredictable,
     * this method creates stubs for all possible combinations that could be created.
     *
     * @param userIds list of user IDs to create follow relationships for
     * @return list of generated follow IDs
     */
    public List<UUID> setupFollowsStubs(List<UUID> userIds) {
        List<UUID> followIds = new ArrayList<>();
        if (userIds.size() < 2) {
            return followIds;
        }

        UUID centralUser = userIds.get(0);
        List<UUID> otherUsers = new ArrayList<>(userIds.subList(1, userIds.size()));
        int halfCount = (userIds.size() - 1) / 2;

        if (halfCount > 0) {
            // Create stubs for all possible combinations that could be created
            // Since Collections.shuffle() makes the order unpredictable, we need to create
            // stubs for all possible combinations that the service might create.
            // Central user can follow any of the other users
            for (UUID otherUser : otherUsers) {
                UUID followId = UUID.randomUUID();
                followIds.add(followId);

                FollowRequestDto followRequest = createFollowRequest(centralUser, otherUser);
                FollowResponseDto followResponse = createFollowResponse(followId, centralUser, otherUser);

                WireMockStubHelper.setupCreateFollowStub(wireMockServer, objectMapper, followRequest, followResponse);
            }

            // Any of the other users can follow central user
            for (UUID otherUser : otherUsers) {
                UUID followId = UUID.randomUUID();
                followIds.add(followId);

                FollowRequestDto followRequest = createFollowRequest(otherUser, centralUser);
                FollowResponseDto followResponse = createFollowResponse(followId, otherUser, centralUser);

                WireMockStubHelper.setupCreateFollowStub(wireMockServer, objectMapper, followRequest, followResponse);
            }
        }

        return followIds;
    }

    /**
     * Sets up WireMock stub for follow creation error.
     * <p>
     * This method configures a WireMock stub for the POST /api/v1/follows endpoint
     * that returns an error response with the specified HTTP status code.
     *
     * @param statusCode the HTTP status code to return
     */
    public void setupFollowCreationError(int statusCode) {
        WireMockStubHelper.setupCreateFollowStubWithError(wireMockServer, statusCode);
    }

    /**
     * Sets up WireMock stubs for creating likes.
     * <p>
     * This method creates test data for likes and configures WireMock stubs
     * for the POST /api/v1/tweets/{tweetId}/like endpoint.
     * <p>
     * Since the actual service uses Collections.shuffle() which makes the order unpredictable,
     * this method creates stubs for all possible combinations that could be created.
     * <p>
     * For each tweet, it creates stubs for all users (except the tweet author) to like it.
     *
     * @param userTweetsMap map of user ID to list of tweet IDs for that user
     * @param userIds       list of all user IDs
     */
    public void setupLikesStubs(Map<UUID, List<UUID>> userTweetsMap, List<UUID> userIds) {
        Map<UUID, UUID> tweetAuthorMap = new HashMap<>();
        for (Map.Entry<UUID, List<UUID>> entry : userTweetsMap.entrySet()) {
            UUID authorId = entry.getKey();
            for (UUID tweetId : entry.getValue()) {
                tweetAuthorMap.put(tweetId, authorId);
            }
        }
        
        for (Map.Entry<UUID, UUID> entry : tweetAuthorMap.entrySet()) {
            UUID tweetId = entry.getKey();
            UUID authorId = entry.getValue();

            for (UUID userId : userIds) {
                if (userId.equals(authorId)) {
                    continue;
                }
                
                LikeTweetRequestDto likeRequest = LikeTweetRequestDto.builder()
                    .userId(userId)
                    .build();
                LikeResponseDto likeResponse = LikeResponseDto.builder()
                    .id(UUID.randomUUID())
                    .tweetId(tweetId)
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .build();
                WireMockStubHelper.setupLikeTweetStub(wireMockServer, objectMapper, tweetId, likeRequest, likeResponse);
            }
        }
    }

    /**
     * Sets up WireMock stubs for creating retweets.
     * <p>
     * This method creates test data for retweets and configures WireMock stubs
     * for the POST /api/v1/tweets/{tweetId}/retweet endpoint.
     * <p>
     * Since the actual service uses Collections.shuffle() which makes the order unpredictable,
     * this method creates stubs for all possible combinations that could be created.
     * <p>
     * For each tweet, it creates stubs for all users (except the tweet author) to retweet it.
     *
     * @param userTweetsMap map of user ID to list of tweet IDs for that user
     * @param userIds       list of all user IDs
     */
    public void setupRetweetsStubs(Map<UUID, List<UUID>> userTweetsMap, List<UUID> userIds) {
        Map<UUID, UUID> tweetAuthorMap = new HashMap<>();
        for (Map.Entry<UUID, List<UUID>> entry : userTweetsMap.entrySet()) {
            UUID authorId = entry.getKey();
            for (UUID tweetId : entry.getValue()) {
                tweetAuthorMap.put(tweetId, authorId);
            }
        }
        
        for (Map.Entry<UUID, UUID> entry : tweetAuthorMap.entrySet()) {
            UUID tweetId = entry.getKey();
            UUID authorId = entry.getValue();

            for (UUID userId : userIds) {
                if (userId.equals(authorId)) {
                    continue;
                }
                
                RetweetRequestDto retweetRequest = RetweetRequestDto.builder()
                    .userId(userId)
                    .comment(null)
                    .build();
                RetweetResponseDto retweetResponse = RetweetResponseDto.builder()
                    .id(UUID.randomUUID())
                    .tweetId(tweetId)
                    .userId(userId)
                    .comment(null)
                    .createdAt(LocalDateTime.now())
                    .build();
                WireMockStubHelper.setupRetweetTweetStub(wireMockServer, objectMapper, tweetId, retweetRequest, retweetResponse);
            }
        }
    }

    /**
     * Sets up WireMock stubs for creating likes and retweets.
     * <p>
     * This method creates test data for likes and retweets and configures WireMock stubs
     * for the POST /api/v1/tweets/{tweetId}/like and POST /api/v1/tweets/{tweetId}/retweet endpoints.
     * <p>
     * Since the actual service uses Collections.shuffle() which makes the order unpredictable,
     * this method creates stubs for all possible combinations that could be created.
     * <p>
     * For each tweet, it creates stubs for all users (except the tweet author) to like/retweet it.
     *
     * @param userTweetsMap map of user ID to list of tweet IDs for that user
     * @param userIds       list of all user IDs
     */
    public void setupLikesAndRetweetsStubs(Map<UUID, List<UUID>> userTweetsMap, List<UUID> userIds) {
        Map<UUID, UUID> tweetAuthorMap = new HashMap<>();
        for (Map.Entry<UUID, List<UUID>> entry : userTweetsMap.entrySet()) {
            UUID authorId = entry.getKey();
            for (UUID tweetId : entry.getValue()) {
                tweetAuthorMap.put(tweetId, authorId);
            }
        }
        
        for (Map.Entry<UUID, UUID> entry : tweetAuthorMap.entrySet()) {
            UUID tweetId = entry.getKey();
            UUID authorId = entry.getValue();

            for (UUID userId : userIds) {
                if (userId.equals(authorId)) {
                    continue;
                }
                
                LikeTweetRequestDto likeRequest = LikeTweetRequestDto.builder()
                    .userId(userId)
                    .build();
                LikeResponseDto likeResponse = LikeResponseDto.builder()
                    .id(UUID.randomUUID())
                    .tweetId(tweetId)
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .build();
                WireMockStubHelper.setupLikeTweetStub(wireMockServer, objectMapper, tweetId, likeRequest, likeResponse);
                
                RetweetRequestDto retweetRequest = RetweetRequestDto.builder()
                    .userId(userId)
                    .comment(null)
                    .build();
                RetweetResponseDto retweetResponse = RetweetResponseDto.builder()
                    .id(UUID.randomUUID())
                    .tweetId(tweetId)
                    .userId(userId)
                    .comment(null)
                    .createdAt(LocalDateTime.now())
                    .build();
                WireMockStubHelper.setupRetweetTweetStub(wireMockServer, objectMapper, tweetId, retweetRequest, retweetResponse);
            }
        }
    }

    /**
     * Sets up WireMock stub for like creation error.
     * <p>
     * This method configures a WireMock stub for the POST /api/v1/tweets/{tweetId}/like endpoint
     * that returns an error response with the specified HTTP status code.
     *
     * @param tweetId    the tweet ID to like
     * @param statusCode the HTTP status code to return
     */
    public void setupLikeCreationError(UUID tweetId, int statusCode) {
        WireMockStubHelper.setupLikeTweetStubWithError(wireMockServer, tweetId, statusCode);
    }

    /**
     * Sets up WireMock stub for retweet creation error.
     * <p>
     * This method configures a WireMock stub for the POST /api/v1/tweets/{tweetId}/retweet endpoint
     * that returns an error response with the specified HTTP status code.
     *
     * @param tweetId    the tweet ID to retweet
     * @param statusCode the HTTP status code to return
     */
    public void setupRetweetCreationError(UUID tweetId, int statusCode) {
        WireMockStubHelper.setupRetweetTweetStubWithError(wireMockServer, tweetId, statusCode);
    }

    /**
     * Sets up WireMock stub for retweet creation error for all tweets.
     * <p>
     * This method configures a WireMock stub for the POST /api/v1/tweets/{tweetId}/retweet endpoint
     * that returns an error response with the specified HTTP status code for all retweet requests.
     *
     * @param statusCode the HTTP status code to return
     */
    public void setupRetweetCreationErrorForAll(int statusCode) {
        WireMockStubHelper.setupRetweetTweetStubWithError(wireMockServer, null, statusCode);
    }

    /**
     * Sets up all WireMock stubs for a full scenario of generating users and tweets.
     * <p>
     * This method sets up stubs for:
     * <ul>
     *   <li>Creating N users</li>
     *   <li>Creating follow relationships between users</li>
     *   <li>Creating N tweets per user</li>
     *   <li>Getting tweets for each user</li>
     *   <li>Deleting the first N tweets (if nTweetsToDelete > 0)</li>
     *   <li>Creating likes and retweets (if enough tweets and users exist)</li>
     * </ul>
     *
     * @param nUsers          number of users to create
     * @param nTweetsPerUser  number of tweets per user
     * @param nTweetsToDelete number of tweets to delete (deletes first N tweets from the list)
     * @return TestStubData containing the generated user IDs, follow IDs, and tweet IDs
     */
    public TestStubData setupFullScenario(int nUsers, int nTweetsPerUser, int nTweetsToDelete) {
        List<UUID> userIds = setupUsersStubs(nUsers);
        List<UUID> followIds = setupFollowsStubs(userIds);
        Map<UUID, List<UUID>> userTweetsMap = setupTweetsStubs(userIds, nTweetsPerUser);
        setupGetUserTweetsStubs(userTweetsMap);

        // Collect all tweet IDs in order
        List<UUID> allTweetIds = new ArrayList<>();
        for (List<UUID> tweetIds : userTweetsMap.values()) {
            allTweetIds.addAll(tweetIds);
        }

        // Setup delete stubs for the first N tweets
        for (int i = 0; i < nTweetsToDelete && i < allTweetIds.size(); i++) {
            setupDeleteTweetStub(allTweetIds.get(i));
        }

        // Setup likes and retweets stubs (if enough tweets and users exist)
        if (allTweetIds.size() >= 6 && userIds.size() >= 2) {
            setupLikesAndRetweetsStubs(userTweetsMap, userIds);
        }

        return new TestStubData(userIds, followIds, allTweetIds);
    }

    /**
     * Record containing test stub data (user IDs, follow IDs, and tweet IDs).
     * <p>
     * This record is used to return the generated IDs from setup methods
     * so that tests can access them if needed.
     *
     * @param userIds  list of generated user IDs
     * @param followIds list of generated follow IDs
     * @param tweetIds list of generated tweet IDs
     * @author geron
     * @version 1.0
     */
    public record TestStubData(
        List<UUID> userIds,
        List<UUID> followIds,
        List<UUID> tweetIds
    ) {
        /**
         * Constructor for backward compatibility (followIds will be empty list).
         *
         * @param userIds  list of generated user IDs
         * @param tweetIds list of generated tweet IDs
         */
        public TestStubData(List<UUID> userIds, List<UUID> tweetIds) {
            this(userIds, new ArrayList<>(), tweetIds);
        }
    }
}
