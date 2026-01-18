package com.twitter.controller;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.common.dto.request.retweet.RetweetRequestDto;
import com.twitter.common.dto.response.retweet.RetweetResponseDto;
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
 * OpenAPI interface for Retweet Management API.
 *
 * @author geron
 * @version 1.0
 */
@Tag(name = "Retweet Management", description = "API for managing retweets in the Twitter system")
public interface RetweetApi {

    /**
     * Retweets a tweet by creating a retweet record with an optional comment.
     * <p>
     * This method creates a retweet record for a specific tweet. It performs validation
     * on the request data, checks if the tweet exists and is not deleted, verifies that
     * the user exists, prevents self-retweets (users cannot retweet their own tweets), and
     * ensures uniqueness (a user can only retweet a tweet once). The retweet operation is
     * atomic and updates the tweet's retweets count. An optional comment can be provided
     * (1-280 characters), but null comment is also allowed.
     *
     * @param tweetId         the unique identifier of the tweet to retweet (UUID format)
     * @param retweetRequest   DTO containing userId and optional comment for the retweet operation
     * @return ResponseEntity containing the created retweet data with HTTP 201 status
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, or self-retweet attempt
     * @throws UniquenessValidationException   if duplicate retweet attempt
     * @throws FormatValidationException       if comment validation fails (empty string or exceeds 280 characters)
     */
    @Operation(
        summary = "Retweet a tweet",
        description = "Retweets a tweet by creating a retweet record with an optional comment. " +
            "It performs validation on the request data, checks if the tweet exists and is not deleted, " +
            "verifies that the user exists, prevents self-retweets (users cannot retweet their own tweets), " +
            "and ensures uniqueness (a user can only retweet a tweet once). " +
            "The retweet operation is atomic and updates the tweet's retweets count. " +
            "An optional comment can be provided (1-280 characters), but null comment is also allowed."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Tweet retweeted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RetweetResponseDto.class)
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
            description = "Business rule violation or uniqueness violation",
            content = @Content(
                mediaType = "application/problem+json"
            )
        )
    })
    ResponseEntity<RetweetResponseDto> retweetTweet(
        @Parameter(
            description = "Unique identifier of the tweet to retweet",
            required = true,
            example = "223e4567-e89b-12d3-a456-426614174001"
        )
        UUID tweetId,
        @Parameter(description = "Retweet request containing userId and optional comment", required = true)
        RetweetRequestDto retweetRequest);

    /**
     * Removes a retweet from a tweet by deleting the retweet record.
     * <p>
     * This method removes a retweet record for a specific tweet. It performs validation
     * on the request data, checks if the tweet exists and is not deleted, verifies that
     * the user exists, and ensures that the retweet exists before removal. The retweet removal operation
     * is atomic and updates the tweet's retweets count by decrementing it.
     *
     * @param tweetId            the unique identifier of the tweet to remove retweet from (UUID format)
     * @param retweetRequest     DTO containing userId for the retweet removal operation
     * @return ResponseEntity with HTTP 204 No Content status (no response body)
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, or retweet doesn't exist
     */
    @Operation(
        summary = "Remove retweet from tweet",
        description = "Removes a retweet from a tweet by deleting the retweet record. " +
            "It performs validation on the request data, checks if the tweet exists and is not deleted, " +
            "verifies that the user exists, and ensures that the retweet exists before removal. " +
            "The retweet removal operation is atomic and updates the tweet's retweets count by decrementing it."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Retweet removed successfully"
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
    ResponseEntity<Void> removeRetweet(
        @Parameter(
            description = "Unique identifier of the tweet to remove retweet from",
            required = true,
            example = "223e4567-e89b-12d3-a456-426614174001"
        )
        UUID tweetId,
        @Parameter(description = "Retweet removal request containing userId", required = true)
        RetweetRequestDto retweetRequest);

    /**
     * Retrieves a paginated list of users who retweeted a specific tweet.
     * <p>
     * This endpoint retrieves all retweets for a specific tweet with pagination support.
     * Retweets are sorted by creation date in descending order (newest first). Supports
     * pagination with page, size, and sort parameters. If the tweet has no retweets,
     * an empty page is returned (not an error).
     *
     * @param tweetId  the unique identifier of the tweet whose retweets to retrieve
     * @param pageable pagination parameters (page, size, sorting)
     * @return PagedModel containing paginated list of retweets with metadata and HATEOAS links
     * @throws BusinessRuleValidationException if tweetId is null or tweet doesn't exist
     */
    @Operation(
        summary = "Get users who retweeted a tweet",
        description = "Retrieves a paginated list of users who retweeted a specific tweet. " +
            "Retweets are sorted by creation date in descending order (newest first). " +
            "Supports pagination with page, size, and sort parameters. " +
            "If the tweet has no retweets, an empty page is returned (not an error). " +
            "Default pagination: page=0, size=20, sort=createdAt,DESC."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tweet retweets retrieved successfully",
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
    PagedModel<RetweetResponseDto> getRetweetsByTweetId(
        @Parameter(
            description = "Unique identifier of the tweet",
            required = true,
            example = "223e4567-e89b-12d3-a456-426614174001"
        )
        UUID tweetId,
        @Parameter(description = "Pagination parameters (page, size, sorting)", required = false)
        Pageable pageable);
}