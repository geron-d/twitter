package com.twitter.controller;

import com.twitter.dto.request.FollowRequestDto;
import com.twitter.dto.response.FollowResponseDto;
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
 * OpenAPI interface for Follow Management API.
 *
 * @author geron
 * @version 1.0
 */
@Tag(name = "Follow Management", description = "API for managing follow relationships in the Twitter system")
public interface FollowApi {

    /**
     * Creates a new follow relationship between two users.
     * <p>
     * This method creates a follow relationship where one user (follower) follows
     * another user (following). It performs validation on the request data, checks
     * if both users exist via users-api integration, and saves the follow relationship
     * to the database. Business rules are enforced: users cannot follow themselves,
     * follow relationships must be unique, and both users must exist in the system.
     *
     * @param request DTO containing follow relationship data for creation (followerId and followingId)
     * @return ResponseEntity containing the created follow relationship data with HTTP 201 status
     */
    @Operation(
        summary = "Create follow relationship",
        description = "Creates a new follow relationship between two users. " +
            "It performs validation on the request data, checks if both users exist via " +
            "users-api integration, and saves the follow relationship to the database. " +
            "Business rules are enforced: users cannot follow themselves, follow relationships " +
            "must be unique, and both users must exist in the system."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Follow relationship created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FollowResponseDto.class),
                examples = @ExampleObject(
                    name = "Created Follow Relationship",
                    summary = "Example created follow relationship",
                    value = """
                        {
                          "id": "456e7890-e89b-12d3-a456-426614174111",
                          "followerId": "123e4567-e89b-12d3-a456-426614174000",
                          "followingId": "987fcdeb-51a2-43d7-b123-426614174999",
                          "createdAt": "2025-01-27T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - Invalid request format",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    summary = "Invalid UUID format or null fields",
                    value = """
                        {
                          "type": "https://example.com/errors/validation-error",
                          "title": "Validation Error",
                          "status": 400,
                          "detail": "Validation failed: followerId: Follower ID cannot be null",
                          "timestamp": "2025-01-27T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Business rule violation - Self follow not allowed",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Self Follow Error",
                    summary = "User attempted to follow themselves",
                    value = """
                        {
                          "type": "https://example.com/errors/business-rule-validation",
                          "title": "Business Rule Validation Error",
                          "status": 409,
                          "detail": "Business rule 'SELF_FOLLOW_NOT_ALLOWED' violated for context: User cannot follow themselves (userId=123e4567-e89b-12d3-a456-426614174000)",
                          "ruleName": "SELF_FOLLOW_NOT_ALLOWED",
                          "context": "User cannot follow themselves (userId=123e4567-e89b-12d3-a456-426614174000)",
                          "timestamp": "2025-01-27T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Uniqueness violation - Follow relationship already exists",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Uniqueness Error",
                    summary = "Follow relationship already exists",
                    value = """
                        {
                          "type": "https://example.com/errors/uniqueness-validation",
                          "title": "Uniqueness Validation Error",
                          "status": 409,
                          "detail": "Follow relationship already exists",
                          "timestamp": "2025-01-27T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Business rule violation - User does not exist",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "User Not Found Error",
                    summary = "User does not exist",
                    value = """
                        {
                          "type": "https://example.com/errors/business-rule-validation",
                          "title": "Business Rule Validation Error",
                          "status": 409,
                          "detail": "Business rule 'FOLLOWER_NOT_EXISTS' violated for context: 123e4567-e89b-12d3-a456-426614174000",
                          "ruleName": "FOLLOWER_NOT_EXISTS",
                          "context": "123e4567-e89b-12d3-a456-426614174000",
                          "timestamp": "2025-01-27T10:30:00Z"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<FollowResponseDto> createFollow(
        @Parameter(description = "Follow relationship data for creation", required = true)
        FollowRequestDto request);
}

