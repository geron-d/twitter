package com.twitter.controller;

import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.request.UpdateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolationException;
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
                schema = @Schema(implementation = TweetResponseDto.class),
                examples = @ExampleObject(
                    name = "Created Tweet",
                    summary = "Example created tweet",
                    value = """
                        {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "userId": "987e6543-e21b-43d2-b654-321987654321",
                          "content": "This is my first tweet!",
                          "createdAt": "2025-01-27T15:30:00Z",
                          "updatedAt": "2025-01-27T15:30:00Z"
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
                    name = "Format Validation Error",
                    summary = "Content validation failed",
                    value = """
                        {
                          "type": "https://example.com/errors/format-validation",
                          "title": "Format Validation Error",
                          "status": 400,
                          "detail": "Tweet content must be between 1 and 280 characters",
                          "fieldName": "content",
                          "constraintName": "CONTENT_VALIDATION",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Business rule violation",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "User Not Found Error",
                    summary = "User does not exist",
                    value = """
                        {
                          "type": "https://example.com/errors/business-rule-validation",
                          "title": "Business Rule Validation Error",
                          "status": 400,
                          "detail": "Business rule 'USER_NOT_EXISTS' violated for context: 987e6543-e21b-43d2-b654-321987654321",
                          "ruleName": "USER_NOT_EXISTS",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Constraint violation error",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Constraint Violation Error",
                    summary = "Bean validation failed",
                    value = """
                        {
                          "type": "https://example.com/errors/validation-error",
                          "title": "Validation Error",
                          "status": 400,
                          "detail": "Validation failed: content: Tweet content cannot be empty",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
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
     *         or HTTP 404 status if not found
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
                schema = @Schema(implementation = TweetResponseDto.class),
                examples = @ExampleObject(
                    name = "Tweet Response",
                    summary = "Example tweet data",
                    value = """
                        {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "userId": "987e6543-e21b-43d2-b654-321987654321",
                          "content": "This is my first tweet!",
                          "createdAt": "2025-01-27T15:30:00Z",
                          "updatedAt": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tweet not found",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Tweet Not Found Error",
                    summary = "Tweet does not exist",
                    value = """
                        {
                          "type": "https://example.com/errors/not-found",
                          "title": "Tweet Not Found",
                          "status": 404,
                          "detail": "Tweet with ID '123e4567-e89b-12d3-a456-426614174000' not found",
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
                schema = @Schema(implementation = TweetResponseDto.class),
                examples = @ExampleObject(
                    name = "Updated Tweet",
                    summary = "Example updated tweet",
                    value = """
                        {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "userId": "987e6543-e21b-43d2-b654-321987654321",
                          "content": "This is updated tweet content",
                          "createdAt": "2025-01-20T10:30:00Z",
                          "updatedAt": "2025-01-27T15:30:00Z"
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
                    name = "Format Validation Error",
                    summary = "Content validation failed",
                    value = """
                        {
                          "type": "https://example.com/errors/format-validation",
                          "title": "Format Validation Error",
                          "status": 400,
                          "detail": "Tweet content must be between 1 and 280 characters",
                          "fieldName": "content",
                          "constraintName": "CONTENT_VALIDATION",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Constraint violation error",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Constraint Violation Error",
                    summary = "Bean validation failed",
                    value = """
                        {
                          "type": "https://example.com/errors/validation-error",
                          "title": "Validation Error",
                          "status": 400,
                          "detail": "Validation failed: content: Tweet content cannot be empty",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Business rule violation - Access denied",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Access Denied Error",
                    summary = "User is not the tweet author",
                    value = """
                        {
                          "type": "https://example.com/errors/business-rule-validation",
                          "title": "Business Rule Validation Error",
                          "status": 409,
                          "detail": "Business rule 'TWEET_ACCESS_DENIED' violated for context: Only the tweet author can update their tweet",
                          "ruleName": "TWEET_ACCESS_DENIED",
                          "context": "Only the tweet author can update their tweet",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Business rule violation - Tweet not found",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Tweet Not Found Error",
                    summary = "Tweet does not exist",
                    value = """
                        {
                          "type": "https://example.com/errors/business-rule-validation",
                          "title": "Business Rule Validation Error",
                          "status": 409,
                          "detail": "Business rule 'TWEET_NOT_FOUND' violated for context: 123e4567-e89b-12d3-a456-426614174000",
                          "ruleName": "TWEET_NOT_FOUND",
                          "context": "123e4567-e89b-12d3-a456-426614174000",
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
    ResponseEntity<TweetResponseDto> updateTweet(
        @Parameter(
            description = "Unique identifier of the tweet to update",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        UUID tweetId,
        @Parameter(description = "Tweet data for update", required = true)
        UpdateTweetRequestDto updateTweetRequest);
}

