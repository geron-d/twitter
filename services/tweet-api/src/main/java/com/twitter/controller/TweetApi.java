package com.twitter.controller;

import com.twitter.common.dto.request.tweet.CreateTweetRequestDto;
import com.twitter.common.dto.request.tweet.DeleteTweetRequestDto;
import com.twitter.common.dto.response.tweet.TweetResponseDto;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.dto.request.UpdateTweetRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * OpenAPI interface for Tweet Management API.
 *
 * @author geron
 * @version 1.0
 */
@Tag(name = "Tweet Management", description = "API for managing tweets in the Twitter system")
public interface TweetApi {

    /**
     * Creates a new tweet in the system.
     * <p>
     * This method creates a new tweet with the provided content and user ID.
     * It performs validation on the request data, checks if the user exists via
     * users-api integration, and saves the tweet to the database. The tweet content
     * must be between 1 and 280 characters and cannot be empty or only whitespace.
     *
     * @param createTweetRequest DTO containing tweet data for creation (content and userId)
     * @return ResponseEntity containing the created tweet data with HTTP 201 status
     * @throws ConstraintViolationException if content validation fails
     * @throws IllegalArgumentException     if user doesn't exist
     */
    @Operation(
        summary = "Create new tweet",
        description = "Creates a new tweet with the provided content and user ID. " +
            "It performs validation on the request data, checks if the user exists via " +
            "users-api integration, and saves the tweet to the database. " +
            "The tweet content must be between 1 and 280 characters and cannot be empty or only whitespace."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Tweet created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TweetResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/problem+json"
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Business rule violation",
            content = @Content(
                mediaType = "application/problem+json"
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Constraint violation error",
            content = @Content(
                mediaType = "application/problem+json"
            )
        )
    })
    ResponseEntity<TweetResponseDto> createTweet(
        @Parameter(description = "Tweet data for creation", required = true)
        CreateTweetRequestDto createTweetRequest);

    /**
     * Retrieves a tweet by its unique identifier.
     * <p>
     * Returns 200 OK with tweet data if found, or 404 Not Found if the tweet
     * does not exist. The tweetId must be a valid UUID format.
     *
     * @param tweetId the unique identifier of the tweet (UUID format)
     * @return ResponseEntity containing tweet data with HTTP 200 status if found,
     * or HTTP 404 status if not found
     */
    @Operation(
        summary = "Get tweet by ID",
        description = "Retrieves a tweet from the database by its unique identifier. " +
            "Returns 200 OK with tweet data if the tweet exists, or 404 Not Found " +
            "if the tweet does not exist. The tweetId must be a valid UUID format."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tweet found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TweetResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tweet not found",
            content = @Content(
                mediaType = "application/problem+json"
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid UUID format",
            content = @Content(
                mediaType = "application/problem+json"
            )
        )
    })
    ResponseEntity<TweetResponseDto> getTweetById(
        @Parameter(
            description = "Unique identifier of the tweet",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        UUID tweetId);

    /**
     * Updates an existing tweet with new content.
     * <p>
     * Only the tweet author can update their tweet. The method performs validation on the request data,
     * checks tweet existence, verifies authorization, and updates the tweet content.
     * The tweet content must be between 1 and 280 characters and cannot be empty or only whitespace.
     *
     * @param tweetId            the unique identifier of the tweet to update (UUID format)
     * @param updateTweetRequest DTO containing tweet data for update (content and userId)
     * @return ResponseEntity containing the updated tweet data with HTTP 200 status
     * @throws ConstraintViolationException if content validation fails
     * @throws IllegalArgumentException     if tweet doesn't exist or access denied
     */
    @Operation(
        summary = "Update existing tweet",
        description = "Only the tweet author can update their tweet. " +
            "The method performs validation on the request data, checks tweet existence, " +
            "verifies authorization, and updates the tweet content. " +
            "The tweet content must be between 1 and 280 characters and cannot be empty or only whitespace."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tweet updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TweetResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/problem+json"
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Constraint violation error",
            content = @Content(
                mediaType = "application/problem+json"
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Business rule violation - Access denied",
            content = @Content(
                mediaType = "application/problem+json"
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Business rule violation - Tweet not found",
            content = @Content(
                mediaType = "application/problem+json"
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid UUID format",
            content = @Content(
                mediaType = "application/problem+json"
            )
        )
    })
    ResponseEntity<TweetResponseDto> updateTweet(
        @Parameter(
            description = "Unique identifier of the tweet to update",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        UUID tweetId,
        @Parameter(description = "Tweet data for update", required = true)
        UpdateTweetRequestDto updateTweetRequest);

    /**
     * Deletes a tweet by performing soft delete.
     * <p>
     * Only the tweet author can delete their tweet. The method performs validation
     * on the request data, checks tweet existence, verifies authorization, and
     * performs soft delete by setting isDeleted flag and deletedAt timestamp.
     *
     * @param tweetId            the unique identifier of the tweet to delete (UUID format)
     * @param deleteTweetRequest DTO containing userId for authorization check
     * @return ResponseEntity with HTTP 204 status if deletion is successful
     * @throws BusinessRuleValidationException if tweet doesn't exist, is already deleted, or access denied
     */
    @Operation(
        summary = "Delete tweet",
        description = "Deletes a tweet by performing soft delete. " +
            "Only the tweet author can delete their tweet. " +
            "The method performs validation on the request data, checks tweet existence, " +
            "verifies authorization, and performs soft delete by setting isDeleted flag " +
            "and deletedAt timestamp. The tweet data is preserved in the database " +
            "for analytics and recovery purposes."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Tweet deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tweet not found or already deleted",
            content = @Content(
                mediaType = "application/problem+json"
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Business rule violation - Access denied",
            content = @Content(
                mediaType = "application/problem+json"
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/problem+json"
            )
        )
    })
    ResponseEntity<Void> deleteTweet(
        @Parameter(
            description = "Unique identifier of the tweet to delete",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        UUID tweetId,
        @Parameter(description = "Tweet deletion request with userId for authorization", required = true)
        DeleteTweetRequestDto deleteTweetRequest);

    /**
     * Retrieves a paginated list of tweets for a specific user.
     * <p>
     * This endpoint retrieves all tweets created by a specific user with pagination support.
     * Tweets are sorted by creation date in descending order (newest first). Deleted tweets
     * (soft delete) are automatically excluded from the results. Supports pagination with
     * page, size, and sort parameters.
     *
     * @param userId   the unique identifier of the user whose tweets to retrieve
     * @param pageable pagination parameters (page, size, sorting)
     * @return PagedModel containing paginated list of tweets with metadata and HATEOAS links
     */
    @Operation(
        summary = "Get user tweets with pagination",
        description = "Retrieves a paginated list of tweets for a specific user. " +
            "Tweets are sorted by creation date in descending order (newest first). " +
            "Deleted tweets (soft delete) are excluded from the results. " +
            "Supports pagination with page, size, and sort parameters. " +
            "Default pagination: page=0, size=20, sort=createdAt,DESC."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User tweets retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PagedModel.class)
            )
        )
    })
    PagedModel<TweetResponseDto> getUserTweets(
        @Parameter(
            description = "Unique identifier of the user",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        UUID userId,
        @Parameter(description = "Pagination parameters (page, size, sorting)", required = false)
        Pageable pageable);

    /**
     * Retrieves a paginated timeline (news feed) of tweets for a specific user.
     * <p>
     * This endpoint retrieves tweets from all users that the specified user is following.
     * The timeline includes tweets from all followed users, sorted by creation date in
     * descending order (newest first). Deleted tweets (soft delete) are automatically
     * excluded from the results. Supports pagination with page, size, and sort parameters.
     * If the user has no following relationships, an empty page is returned (not an error).
     * Integration with follower-api is used to retrieve the list of followed users.
     *
     * @param userId   the unique identifier of the user whose timeline to retrieve
     * @param pageable pagination parameters (page, size, sorting)
     * @return PagedModel containing paginated list of tweets with metadata and HATEOAS links
     * @throws BusinessRuleValidationException if userId is null or user doesn't exist
     * @throws ConstraintViolationException   if pagination parameters are invalid
     */
    @Operation(
        summary = "Get user timeline with pagination",
        description = "Retrieves a paginated timeline (news feed) of tweets for a specific user. " +
            "The timeline includes tweets from all users that the specified user is following. " +
            "Tweets are sorted by creation date in descending order (newest first). " +
            "Deleted tweets (soft delete) are excluded from the results. " +
            "Supports pagination with page, size, and sort parameters. " +
            "If the user has no following relationships, an empty page is returned (not an error). " +
            "Integration with follower-api is used to retrieve the list of followed users. " +
            "Default pagination: page=0, size=20, sort=createdAt,DESC."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Timeline retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PagedModel.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid UUID format for userId",
            content = @Content(
                mediaType = "application/problem+json"
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "User does not exist",
            content = @Content(
                mediaType = "application/problem+json"
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid pagination parameters",
            content = @Content(
                mediaType = "application/problem+json"
            )
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Follower API service unavailable",
            content = @Content(
                mediaType = "application/problem+json"
            )
        )
    })
    PagedModel<TweetResponseDto> getTimeline(
        @Parameter(
            description = "Unique identifier of the user whose timeline to retrieve",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        UUID userId,
        @Parameter(description = "Pagination parameters (page, size, sorting)", required = false)
        Pageable pageable);
}