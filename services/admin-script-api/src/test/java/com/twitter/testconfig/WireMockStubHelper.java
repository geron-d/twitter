package com.twitter.testconfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.twitter.common.dto.request.CreateTweetRequestDto;
import com.twitter.common.dto.request.FollowRequestDto;
import com.twitter.common.dto.request.UserRequestDto;
import com.twitter.common.dto.response.FollowResponseDto;
import com.twitter.common.dto.response.TweetResponseDto;
import com.twitter.common.dto.response.UserResponseDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Utility class for setting up WireMock stubs for external API calls in integration tests.
 * <p>
 * This class provides static helper methods for configuring WireMock stubs
 * for users-api and tweet-api services used in admin-script-api tests.
 * <p>
 * All methods are static and require WireMockServer and ObjectMapper instances
 * to be passed as parameters for flexibility and testability.
 *
 * @author geron
 * @version 1.0
 */
public final class WireMockStubHelper {

    private WireMockStubHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Sets up WireMock stub for creating a user via users-api.
     * <p>
     * This method configures a stub for POST /api/v1/users endpoint that returns
     * the provided user response with HTTP status 201 Created.
     *
     * @param wireMockServer the WireMock server instance
     * @param objectMapper   the ObjectMapper for JSON serialization
     * @param userRequest    the user request DTO (not used in stub matching, but kept for consistency)
     * @param userResponse   the user response DTO to return
     * @throws RuntimeException if JSON serialization fails
     */
    public static void setupCreateUserStub(
        WireMockServer wireMockServer,
        ObjectMapper objectMapper,
        UserRequestDto userRequest,
        UserResponseDto userResponse) {
        if (wireMockServer == null) {
            return;
        }

        try {
            String responseBody = objectMapper.writeValueAsString(userResponse);

            wireMockServer.stubFor(
                post(urlEqualTo("/api/v1/users"))
                    .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody))
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to setup create user stub", e);
        }
    }

    /**
     * Sets up WireMock stub for creating a user with error response via users-api.
     * <p>
     * This method configures a stub for POST /api/v1/users endpoint that returns
     * an error response with the specified HTTP status code.
     *
     * @param wireMockServer the WireMock server instance
     * @param statusCode     the HTTP status code to return
     */
    public static void setupCreateUserStubWithError(
        WireMockServer wireMockServer,
        int statusCode) {
        if (wireMockServer == null) {
            return;
        }

        wireMockServer.stubFor(
            post(urlEqualTo("/api/v1/users"))
                .willReturn(aResponse()
                    .withStatus(statusCode)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\":\"Internal Server Error\"}"))
        );
    }

    /**
     * Sets up WireMock stub for creating a tweet via tweet-api.
     * <p>
     * This method configures a stub for POST /api/v1/tweets endpoint that matches
     * the request by userId and returns the provided tweet response with HTTP status 201 Created.
     *
     * @param wireMockServer     the WireMock server instance
     * @param objectMapper       the ObjectMapper for JSON serialization
     * @param createTweetRequest the tweet request DTO (used for matching userId in request body)
     * @param tweetResponse      the tweet response DTO to return
     * @throws RuntimeException if JSON serialization fails
     */
    public static void setupCreateTweetStub(
        WireMockServer wireMockServer,
        ObjectMapper objectMapper,
        CreateTweetRequestDto createTweetRequest,
        TweetResponseDto tweetResponse) {
        if (wireMockServer == null) {
            return;
        }

        try {
            String responseBody = objectMapper.writeValueAsString(tweetResponse);

            wireMockServer.stubFor(
                post(urlEqualTo("/api/v1/tweets"))
                    .withRequestBody(matchingJsonPath("$.userId", equalTo(createTweetRequest.userId().toString())))
                    .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody))
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to setup create tweet stub", e);
        }
    }

    /**
     * Sets up WireMock stub for creating a tweet with error response via tweet-api.
     * <p>
     * This method configures a stub for POST /api/v1/tweets endpoint that returns
     * an error response with the specified HTTP status code.
     *
     * @param wireMockServer the WireMock server instance
     * @param statusCode     the HTTP status code to return
     */
    public static void setupCreateTweetStubWithError(
        WireMockServer wireMockServer,
        int statusCode) {
        if (wireMockServer == null) {
            return;
        }

        wireMockServer.stubFor(
            post(urlEqualTo("/api/v1/tweets"))
                .willReturn(aResponse()
                    .withStatus(statusCode)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\":\"Internal Server Error\"}"))
        );
    }

    /**
     * Sets up WireMock stub for getting user tweets via tweet-api.
     * <p>
     * This method configures a stub for GET /api/v1/tweets/user/{userId} endpoint
     * that returns a paginated response containing the provided list of tweets.
     *
     * @param wireMockServer the WireMock server instance
     * @param objectMapper   the ObjectMapper for JSON serialization
     * @param userId         the user ID to get tweets for
     * @param tweets         list of tweets to return in the response
     * @throws RuntimeException if JSON serialization fails
     */
    public static void setupGetUserTweetsStub(
        WireMockServer wireMockServer,
        ObjectMapper objectMapper,
        UUID userId,
        List<TweetResponseDto> tweets) {
        if (wireMockServer == null) {
            return;
        }

        try {
            PageImpl<TweetResponseDto> page = new PageImpl<>(tweets, PageRequest.of(0, 20), tweets.size());
            String responseBody = objectMapper.writeValueAsString(page);

            wireMockServer.stubFor(
                get(urlPathEqualTo("/api/v1/tweets/user/" + userId))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody))
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to setup getUserTweets stub", e);
        }
    }

    /**
     * Sets up WireMock stub for deleting a tweet via tweet-api.
     * <p>
     * This method configures a stub for DELETE /api/v1/tweets/{tweetId} endpoint
     * that returns HTTP status 204 No Content.
     *
     * @param wireMockServer the WireMock server instance
     * @param tweetId        the tweet ID to delete
     */
    public static void setupDeleteTweetStub(
        WireMockServer wireMockServer,
        UUID tweetId) {
        if (wireMockServer == null) {
            return;
        }

        wireMockServer.stubFor(
            delete(urlEqualTo("/api/v1/tweets/" + tweetId))
                .willReturn(aResponse()
                    .withStatus(204))
        );
    }

    /**
     * Sets up WireMock stub for creating a follow relationship via follower-api.
     * <p>
     * This method configures a stub for POST /api/v1/follows endpoint that matches
     * the request by followerId and followingId and returns the provided follow response
     * with HTTP status 201 Created.
     *
     * @param wireMockServer   the WireMock server instance
     * @param objectMapper     the ObjectMapper for JSON serialization
     * @param followRequest    the follow request DTO (used for matching followerId and followingId in request body)
     * @param followResponse   the follow response DTO to return
     * @throws RuntimeException if JSON serialization fails
     */
    public static void setupCreateFollowStub(
        WireMockServer wireMockServer,
        ObjectMapper objectMapper,
        FollowRequestDto followRequest,
        FollowResponseDto followResponse) {
        if (wireMockServer == null) {
            return;
        }

        try {
            String responseBody = objectMapper.writeValueAsString(followResponse);
            String followerIdStr = followRequest.followerId().toString();
            String followingIdStr = followRequest.followingId().toString();

            wireMockServer.stubFor(
                post(urlPathEqualTo("/api/v1/follows"))
                    .withRequestBody(matchingJsonPath("$.followerId", equalTo(followerIdStr)))
                    .withRequestBody(matchingJsonPath("$.followingId", equalTo(followingIdStr)))
                    .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody))
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to setup create follow stub", e);
        }
    }

    /**
     * Sets up WireMock stub for creating a follow relationship with error response via follower-api.
     * <p>
     * This method configures a stub for POST /api/v1/follows endpoint that returns
     * an error response with the specified HTTP status code.
     *
     * @param wireMockServer the WireMock server instance
     * @param statusCode     the HTTP status code to return
     */
    public static void setupCreateFollowStubWithError(
        WireMockServer wireMockServer,
        int statusCode) {
        if (wireMockServer == null) {
            return;
        }

        wireMockServer.stubFor(
            post(urlEqualTo("/api/v1/follows"))
                .willReturn(aResponse()
                    .withStatus(statusCode)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\":\"Internal Server Error\"}"))
        );
    }
}

