package com.twitter.controller;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.common.dto.request.like.LikeTweetRequestDto;
import com.twitter.common.dto.response.like.LikeResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * OpenAPI interface for Like Management API.
 *
 * @author geron
 * @version 1.0
 */
@Tag(name = "Like Management", description = "API for managing likes in the Twitter system")
public interface LikeApi {

    /**
     * Likes a tweet by creating a like record.
     * <p>
     * This method creates a like record for a specific tweet. It performs validation
     * on the request data, checks if the tweet exists and is not deleted, verifies that
     * the user exists, prevents self-likes (users cannot like their own tweets), and
     * ensures uniqueness (a user can only like a tweet once). The like operation is
     * atomic and updates the tweet's likes count.
     *
     * @param tweetId            the unique identifier of the tweet to like (UUID format)
     * @param likeTweetRequest   DTO containing userId for the like operation
     * @return ResponseEntity containing the created like data with HTTP 201 status
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, or self-like attempt
     * @throws UniquenessValidationException   if duplicate like attempt
     */
    @Operation(
        summary = "Like a tweet",
        description = "Likes a tweet by creating a like record. " +
            "It performs validation on the request data, checks if the tweet exists and is not deleted, " +
            "verifies that the user exists, prevents self-likes (users cannot like their own tweets), " +
            "and ensures uniqueness (a user can only like a tweet once). " +
            "The like operation is atomic and updates the tweet's likes count."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Tweet liked successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LikeResponseDto.class)
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
            responseCode = "409",
            description = "Business rule violation",
            content = @Content(
                mediaType = "application/problem+json"
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Uniqueness violation - duplicate like",
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
    ResponseEntity<LikeResponseDto> likeTweet(
        @Parameter(
            description = "Unique identifier of the tweet to like",
            required = true,
            example = "223e4567-e89b-12d3-a456-426614174001"
        )
        UUID tweetId,
        @Parameter(description = "Like request containing userId", required = true)
        LikeTweetRequestDto likeTweetRequest);

    /**
     * Removes a like from a tweet by deleting the like record.
     * <p>
     * This method removes a like record for a specific tweet. It performs validation
     * on the request data, checks if the tweet exists and is not deleted, verifies that
     * the user exists, and ensures that the like exists before removal. The unlike operation
     * is atomic and updates the tweet's likes count by decrementing it.
     *
     * @param tweetId            the unique identifier of the tweet to unlike (UUID format)
     * @param likeTweetRequest   DTO containing userId for the unlike operation
     * @return ResponseEntity with HTTP 204 No Content status (no response body)
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, or like doesn't exist
     */
    @Operation(
        summary = "Remove like from tweet",
        description = "Removes a like from a tweet by deleting the like record. " +
            "It performs validation on the request data, checks if the tweet exists and is not deleted, " +
            "verifies that the user exists, and ensures that the like exists before removal. " +
            "The unlike operation is atomic and updates the tweet's likes count by decrementing it."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Like removed successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/problem+json"
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Business rule violation",
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
    ResponseEntity<Void> removeLike(
        @Parameter(
            description = "Unique identifier of the tweet to unlike",
            required = true,
            example = "223e4567-e89b-12d3-a456-426614174001"
        )
        UUID tweetId,
        @Parameter(description = "Unlike request containing userId", required = true)
        LikeTweetRequestDto likeTweetRequest);

    /**
     * Retrieves a paginated list of users who liked a specific tweet.
     * <p>
     * This endpoint retrieves all likes for a specific tweet with pagination support.
     * Likes are sorted by creation date in descending order (newest first). Supports
     * pagination with page, size, and sort parameters. If the tweet has no likes,
     * an empty page is returned (not an error).
     *
     * @param tweetId  the unique identifier of the tweet whose likes to retrieve
     * @param pageable pagination parameters (page, size, sorting)
     * @return PagedModel containing paginated list of likes with metadata and HATEOAS links
     * @throws BusinessRuleValidationException if tweetId is null or tweet doesn't exist
     */
    @Operation(
        summary = "Get users who liked a tweet",
        description = "Retrieves a paginated list of users who liked a specific tweet. " +
            "Likes are sorted by creation date in descending order (newest first). " +
            "Supports pagination with page, size, and sort parameters. " +
            "If the tweet has no likes, an empty page is returned (not an error). " +
            "Default pagination: page=0, size=20, sort=createdAt,DESC."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tweet likes retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PagedModel.class)
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
            responseCode = "409",
            description = "Business rule violation",
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
    PagedModel<LikeResponseDto> getLikesByTweetId(
        @Parameter(
            description = "Unique identifier of the tweet",
            required = true,
            example = "223e4567-e89b-12d3-a456-426614174001"
        )
        UUID tweetId,
        @Parameter(description = "Pagination parameters (page, size, sorting)", required = false)
        Pageable pageable);
}