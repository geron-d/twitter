package com.twitter.controller;

import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * OpenAPI interface for Tweet Management API.
 * <p>
 * This interface contains all OpenAPI annotations for the Tweet Management API endpoints.
 * It provides a centralized location for API documentation and can be implemented
 * by controllers to ensure consistent API documentation across the application.
 *
 * @author geron
 * @version 1.0
 */
@Tag(name = "Tweet Management", description = "API for managing tweets in the Twitter system")
public interface TweetApi {

    /**
     * Creates a new tweet in the system.
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
}

