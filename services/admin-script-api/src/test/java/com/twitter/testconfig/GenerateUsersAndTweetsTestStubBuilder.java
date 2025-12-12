package com.twitter.testconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.twitter.common.dto.request.CreateTweetRequestDto;
import com.twitter.common.dto.request.UserRequestDto;
import com.twitter.common.dto.response.TweetResponseDto;
import com.twitter.common.dto.response.UserResponseDto;
import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Builder class for setting up WireMock stubs in GenerateUsersAndTweetsControllerTest.
 * <p>
 * This class provides methods to create test data (DTOs) and configure WireMock stubs
 * for testing the generate-users-and-tweets endpoint. It encapsulates the repetitive
 * logic of creating users and tweets test data and setting up corresponding stubs.
 * <p>
 * The builder uses instance-based approach for better flexibility and testability.
 *
 * @author geron
 * @version 1.0
 */
public final class GenerateUsersAndTweetsTestStubBuilder {

    private final WireMockServer wireMockServer;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new instance of the builder.
     *
     * @param wireMockServer the WireMock server instance for setting up stubs
     * @param objectMapper   the ObjectMapper for JSON serialization
     */
    public GenerateUsersAndTweetsTestStubBuilder(WireMockServer wireMockServer, ObjectMapper objectMapper) {
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
     * Sets up all WireMock stubs for a full scenario of generating users and tweets.
     * <p>
     * This method sets up stubs for:
     * <ul>
     *   <li>Creating N users</li>
     *   <li>Creating N tweets per user</li>
     *   <li>Getting tweets for each user</li>
     *   <li>Deleting the first N tweets (if nTweetsToDelete > 0)</li>
     * </ul>
     *
     * @param nUsers          number of users to create
     * @param nTweetsPerUser  number of tweets per user
     * @param nTweetsToDelete number of tweets to delete (deletes first N tweets from the list)
     * @return TestStubData containing the generated user IDs and tweet IDs
     */
    public TestStubData setupFullScenario(int nUsers, int nTweetsPerUser, int nTweetsToDelete) {
        List<UUID> userIds = setupUsersStubs(nUsers);
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

        return new TestStubData(userIds, allTweetIds);
    }

    /**
     * Record containing test stub data (user IDs and tweet IDs).
     * <p>
     * This record is used to return the generated IDs from setup methods
     * so that tests can access them if needed.
     *
     * @param userIds  list of generated user IDs
     * @param tweetIds list of generated tweet IDs
     * @author geron
     * @version 1.0
     */
    public record TestStubData(
        List<UUID> userIds,
        List<UUID> tweetIds
    ) {
    }
}

