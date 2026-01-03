package com.twitter.controller;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.request.LikeTweetRequestDto;
import com.twitter.dto.response.LikeResponseDto;
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
                schema = @Schema(implementation = LikeResponseDto.class),
                examples = @ExampleObject(
                    name = "Created Like",
                    summary = "Example created like",
                    value = """
                        {
                          "id": "987e6543-e21b-43d2-b654-321987654321",
                          "tweetId": "223e4567-e89b-12d3-a456-426614174001",
                          "userId": "123e4567-e89b-12d3-a456-426614174000",
                          "createdAt": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
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
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Business rule violation",
            content = @Content(
                mediaType = "application/problem+json",
                examples = {
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
                        name = "Self-Like Error",
                        summary = "User cannot like their own tweet",
                        value = """
                            {
                              "type": "https://example.com/errors/business-rule-validation",
                              "title": "Business Rule Validation Error",
                              "status": 409,
                              "detail": "Business rule 'SELF_LIKE_NOT_ALLOWED' violated for context: Users cannot like their own tweets",
                              "ruleName": "SELF_LIKE_NOT_ALLOWED",
                              "context": "Users cannot like their own tweets",
                              "timestamp": "2025-01-27T15:30:00Z"
                            }
                            """
                    ),
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
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Uniqueness violation - duplicate like",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Duplicate Like Error",
                    summary = "User already liked this tweet",
                    value = """
                        {
                          "type": "https://example.com/errors/uniqueness-validation",
                          "title": "Uniqueness Validation Error",
                          "status": 409,
                          "detail": "A like already exists for tweet 223e4567-e89b-12d3-a456-426614174001 and user 123e4567-e89b-12d3-a456-426614174000",
                          "fieldName": "like",
                          "fieldValue": "tweet 223e4567-e89b-12d3-a456-426614174001 and user 123e4567-e89b-12d3-a456-426614174000",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid UUID format",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
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
                mediaType = "application/problem+json",
                examples = @ExampleObject(
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
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Business rule violation",
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
                        name = "Like Not Found Error",
                        summary = "Like does not exist",
                        value = """
                            {
                              "type": "https://example.com/errors/business-rule-validation",
                              "title": "Business Rule Validation Error",
                              "status": 409,
                              "detail": "Business rule 'LIKE_NOT_FOUND' violated for context: Like not found for tweet 223e4567-e89b-12d3-a456-426614174001 and user 123e4567-e89b-12d3-a456-426614174000",
                              "ruleName": "LIKE_NOT_FOUND",
                              "context": "Like not found for tweet 223e4567-e89b-12d3-a456-426614174001 and user 123e4567-e89b-12d3-a456-426614174000",
                              "timestamp": "2025-01-27T15:30:00Z"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid UUID format",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
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
}

