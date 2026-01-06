package com.twitter.controller;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.request.RetweetRequestDto;
import com.twitter.dto.response.RetweetResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
                schema = @Schema(implementation = RetweetResponseDto.class),
                examples = {
                    @ExampleObject(
                        name = "Created Retweet With Comment",
                        summary = "Example created retweet with comment",
                        value = """
                            {
                              "id": "987e6543-e21b-43d2-b654-321987654321",
                              "tweetId": "223e4567-e89b-12d3-a456-426614174001",
                              "userId": "123e4567-e89b-12d3-a456-426614174000",
                              "comment": "Great tweet!",
                              "createdAt": "2025-01-27T15:30:00Z"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Created Retweet Without Comment",
                        summary = "Example created retweet without comment",
                        value = """
                            {
                              "id": "987e6543-e21b-43d2-b654-321987654321",
                              "tweetId": "223e4567-e89b-12d3-a456-426614174001",
                              "userId": "123e4567-e89b-12d3-a456-426614174000",
                              "comment": null,
                              "createdAt": "2025-01-27T15:30:00Z"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/problem+json",
                examples = {
                    @ExampleObject(
                        name = "User ID Validation Error",
                        summary = "User ID is null or invalid",
                        value = """
                            {
                              "type": "https://example.com/errors/validation-error",
                              "title": "Validation Error",
                              "status": 400,
                              "detail": "Validation failed: userId: User ID cannot be null",
                              "timestamp": "2025-01-27T15:30:00Z"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Comment Validation Error",
                        summary = "Comment is empty string or exceeds max length",
                        value = """
                            {
                              "type": "https://example.com/errors/format-validation",
                              "title": "Format Validation Error",
                              "status": 400,
                              "detail": "Comment cannot be empty string. Use null if no comment is provided.",
                              "fieldName": "comment",
                              "constraintName": "NOT_EMPTY",
                              "timestamp": "2025-01-27T15:30:00Z"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Invalid UUID Format Error",
                        summary = "Invalid tweet ID format",
                        value = """
                            {
                              "type": "https://example.com/errors/validation-error",
                              "title": "Validation Error",
                              "status": 400,
                              "detail": "Invalid UUID format for tweetId parameter",
                              "timestamp": "2025-01-27T15:30:00Z"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Business rule violation or uniqueness violation",
            content = @Content(
                mediaType = "application/problem+json",
                examples = {
                    @ExampleObject(
                        name = "Tweet Not Found Error",
                        summary = "Tweet does not exist or is deleted",
                        value = """
                            {
                              "type": "https://example.com/errors/business-rule-validation",
                              "title": "Business Rule Validation Error",
                              "status": 409,
                              "detail": "Business rule 'TWEET_NOT_FOUND' violated for context: 223e4567-e89b-12d3-a456-426614174001",
                              "ruleName": "TWEET_NOT_FOUND",
                              "context": "223e4567-e89b-12d3-a456-426614174001",
                              "timestamp": "2025-01-27T15:30:00Z"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "User Not Found Error",
                        summary = "User does not exist",
                        value = """
                            {
                              "type": "https://example.com/errors/business-rule-validation",
                              "title": "Business Rule Validation Error",
                              "status": 409,
                              "detail": "Business rule 'USER_NOT_EXISTS' violated for context: 123e4567-e89b-12d3-a456-426614174000",
                              "ruleName": "USER_NOT_EXISTS",
                              "context": "123e4567-e89b-12d3-a456-426614174000",
                              "timestamp": "2025-01-27T15:30:00Z"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Self-Retweet Error",
                        summary = "User cannot retweet their own tweet",
                        value = """
                            {
                              "type": "https://example.com/errors/business-rule-validation",
                              "title": "Business Rule Validation Error",
                              "status": 409,
                              "detail": "Business rule 'SELF_RETWEET_NOT_ALLOWED' violated for context: Users cannot retweet their own tweets",
                              "ruleName": "SELF_RETWEET_NOT_ALLOWED",
                              "context": "Users cannot retweet their own tweets",
                              "timestamp": "2025-01-27T15:30:00Z"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Duplicate Retweet Error",
                        summary = "User already retweeted this tweet",
                        value = """
                            {
                              "type": "https://example.com/errors/uniqueness-validation",
                              "title": "Uniqueness Validation Error",
                              "status": 409,
                              "detail": "A retweet already exists for tweet 223e4567-e89b-12d3-a456-426614174001 and user 123e4567-e89b-12d3-a456-426614174000",
                              "fieldName": "retweet",
                              "fieldValue": "tweet 223e4567-e89b-12d3-a456-426614174001 and user 123e4567-e89b-12d3-a456-426614174000",
                              "timestamp": "2025-01-27T15:30:00Z"
                            }
                            """
                    )
                }
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
}