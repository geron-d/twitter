package com.twitter.controller;

import com.twitter.common.dto.request.FollowRequestDto;
import com.twitter.common.dto.response.FollowResponseDto;
import com.twitter.dto.filter.FollowerFilter;
import com.twitter.dto.filter.FollowingFilter;
import com.twitter.dto.response.FollowStatsResponseDto;
import com.twitter.dto.response.FollowStatusResponseDto;
import com.twitter.dto.response.FollowerResponseDto;
import com.twitter.common.dto.response.FollowingResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

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

    /**
     * Removes a follow relationship between two users.
     * <p>
     * This method removes an existing follow relationship where one user (follower)
     * was following another user (following). It checks if the follow relationship
     * exists before attempting to delete it. If the relationship does not exist,
     * a 404 Not Found response is returned.
     *
     * @param followerId  the ID of the user who is following (the follower)
     * @param followingId the ID of the user being followed (the following)
     * @return ResponseEntity with HTTP 204 status if deletion is successful
     */
    @Operation(
        summary = "Delete follow relationship",
        description = "Removes an existing follow relationship between two users. " +
            "It checks if the follow relationship exists before attempting to delete it. " +
            "If the relationship does not exist, a 404 Not Found response is returned."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Follow relationship deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Follow relationship not found",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Follow Not Found",
                    summary = "Follow relationship does not exist",
                    value = """
                        {
                          "type": "https://example.com/errors/business-rule-validation",
                          "title": "Business Rule Validation Error",
                          "status": 404,
                          "detail": "Business rule 'FOLLOW_NOT_FOUND' violated for context: Follow relationship between followerId=123e4567-e89b-12d3-a456-426614174000 and followingId=987fcdeb-51a2-43d7-b123-426614174999 does not exist",
                          "ruleName": "FOLLOW_NOT_FOUND",
                          "context": "Follow relationship between followerId=123e4567-e89b-12d3-a456-426614174000 and followingId=987fcdeb-51a2-43d7-b123-426614174999 does not exist",
                          "timestamp": "2025-01-27T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - Invalid UUID format",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Invalid UUID Format Error",
                    summary = "Invalid UUID format for path parameters",
                    value = """
                        {
                          "type": "https://example.com/errors/validation-error",
                          "title": "Validation Error",
                          "status": 400,
                          "detail": "Invalid UUID format for followerId or followingId parameter",
                          "timestamp": "2025-01-27T10:30:00Z"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<Void> deleteFollow(
        @Parameter(
            description = "ID of the user who is following (the follower)",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        UUID followerId,
        @Parameter(
            description = "ID of the user being followed (the following)",
            required = true,
            example = "987fcdeb-51a2-43d7-b123-426614174999"
        )
        UUID followingId);

    /**
     * Retrieves a paginated list of followers for a specific user.
     * <p>
     * This method retrieves all users who follow the specified user, with optional
     * filtering by login name. The results are paginated and sorted by creation date
     * in descending order (newest first). User login information is retrieved from
     * the users-api service.
     *
     * @param userId   the ID of the user whose followers should be retrieved
     * @param filter   optional filter criteria for filtering followers by login (partial match)
     * @param pageable pagination parameters (page, size, sorting)
     * @return PagedModel containing paginated list of followers with metadata and HATEOAS links
     */
    @Operation(
        summary = "Get followers list",
        description = "Retrieves a paginated list of followers for a specific user. " +
            "Supports optional filtering by login name (partial match, case-insensitive). " +
            "Results are sorted by creation date in descending order (newest first). " +
            "User login information is retrieved from the users-api service."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Followers retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PagedModel.class),
                examples = @ExampleObject(
                    name = "Paginated Followers",
                    summary = "Example paginated followers response",
                    value = """
                        {
                          "content": [
                            {
                              "id": "123e4567-e89b-12d3-a456-426614174000",
                              "login": "john_doe",
                              "createdAt": "2025-01-20T15:30:00Z"
                            }
                          ],
                          "page": {
                            "size": 10,
                            "number": 0,
                            "totalElements": 1,
                            "totalPages": 1
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - Invalid UUID format",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Invalid UUID Format Error",
                    summary = "Invalid UUID format for userId parameter",
                    value = """
                        {
                          "type": "https://example.com/errors/validation-error",
                          "title": "Validation Error",
                          "status": 400,
                          "detail": "Invalid UUID format for userId parameter",
                          "timestamp": "2025-01-27T10:30:00Z"
                        }
                        """
                )
            )
        )
    })
    PagedModel<FollowerResponseDto> getFollowers(
        @Parameter(
            description = "Unique identifier of the user whose followers should be retrieved",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        UUID userId,
        @Parameter(description = "Filter criteria for filtering followers by login (partial match)")
        FollowerFilter filter,
        @Parameter(description = "Pagination parameters (page, size, sorting)")
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable);

    /**
     * Retrieves a paginated list of following for a specific user.
     * <p>
     * This method retrieves all users that the specified user is following, with optional
     * filtering by login name. The results are paginated and sorted by creation date
     * in descending order (newest first). User login information is retrieved from
     * the users-api service.
     *
     * @param userId   the ID of the user whose following should be retrieved
     * @param filter   optional filter criteria for filtering following by login (partial match)
     * @param pageable pagination parameters (page, size, sorting)
     * @return PagedModel containing paginated list of following with metadata and HATEOAS links
     */
    @Operation(
        summary = "Get following list",
        description = "Retrieves a paginated list of following for a specific user. " +
            "Supports optional filtering by login name (partial match, case-insensitive). " +
            "Results are sorted by creation date in descending order (newest first). " +
            "User login information is retrieved from the users-api service."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Following retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PagedModel.class),
                examples = @ExampleObject(
                    name = "Paginated Following",
                    summary = "Example paginated following response",
                    value = """
                        {
                          "content": [
                            {
                              "id": "987fcdeb-51a2-43d7-b123-426614174999",
                              "login": "jane_doe",
                              "createdAt": "2025-01-20T15:30:00Z"
                            }
                          ],
                          "page": {
                            "size": 10,
                            "number": 0,
                            "totalElements": 1,
                            "totalPages": 1
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - Invalid UUID format",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Invalid UUID Format Error",
                    summary = "Invalid UUID format for userId parameter",
                    value = """
                        {
                          "type": "https://example.com/errors/validation-error",
                          "title": "Validation Error",
                          "status": 400,
                          "detail": "Invalid UUID format for userId parameter",
                          "timestamp": "2025-01-27T10:30:00Z"
                        }
                        """
                )
            )
        )
    })
    PagedModel<FollowingResponseDto> getFollowing(
        @Parameter(
            description = "Unique identifier of the user whose following should be retrieved",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        UUID userId,
        @Parameter(description = "Filter criteria for filtering following by login (partial match)")
        FollowingFilter filter,
        @Parameter(description = "Pagination parameters (page, size, sorting)")
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable);

    /**
     * Retrieves the status of a follow relationship between two users.
     * <p>
     * This method checks if a follow relationship exists between the specified follower
     * and following users. If the relationship exists, it returns isFollowing=true and
     * the creation timestamp. If the relationship does not exist, it returns
     * isFollowing=false and createdAt=null.
     *
     * @param followerId  the ID of the user who is following (the follower)
     * @param followingId the ID of the user being followed (the following)
     * @return ResponseEntity containing the status of the follow relationship with HTTP 200 status
     */
    @Operation(
        summary = "Get follow relationship status",
        description = "Retrieves the status of a follow relationship between two users. " +
            "If the relationship exists, it returns isFollowing=true and the creation timestamp. " +
            "If the relationship does not exist, it returns isFollowing=false and createdAt=null."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Follow relationship status retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FollowStatusResponseDto.class),
                examples = {
                    @ExampleObject(
                        name = "Follow Relationship Exists",
                        summary = "Example when follow relationship exists",
                        value = """
                            {
                              "isFollowing": true,
                              "createdAt": "2025-01-20T15:30:00Z"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Follow Relationship Does Not Exist",
                        summary = "Example when follow relationship does not exist",
                        value = """
                            {
                              "isFollowing": false,
                              "createdAt": null
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - Invalid UUID format",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Invalid UUID Format Error",
                    summary = "Invalid UUID format for path parameters",
                    value = """
                        {
                          "type": "https://example.com/errors/validation-error",
                          "title": "Validation Error",
                          "status": 400,
                          "detail": "Invalid UUID format for followerId or followingId parameter",
                          "timestamp": "2025-01-27T10:30:00Z"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<FollowStatusResponseDto> getFollowStatus(
        @Parameter(
            description = "ID of the user who is following (the follower)",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        UUID followerId,
        @Parameter(
            description = "ID of the user being followed (the following)",
            required = true,
            example = "987fcdeb-51a2-43d7-b123-426614174999"
        )
        UUID followingId);

    /**
     * Retrieves follow statistics for a specific user.
     * <p>
     * This method calculates and returns the total number of followers (users following
     * this user) and the total number of following (users this user is following).
     *
     * @param userId the ID of the user whose statistics should be retrieved
     * @return ResponseEntity containing follow statistics with HTTP 200 status
     */
    @Operation(
        summary = "Get follow statistics",
        description = "Retrieves follow statistics for a specific user. " +
            "Returns the total number of followers (users following this user) and " +
            "the total number of following (users this user is following)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Follow statistics retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FollowStatsResponseDto.class),
                examples = @ExampleObject(
                    name = "Follow Statistics",
                    summary = "Example follow statistics response",
                    value = """
                        {
                          "followersCount": 150,
                          "followingCount": 75
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - Invalid UUID format",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Invalid UUID Format Error",
                    summary = "Invalid UUID format for userId parameter",
                    value = """
                        {
                          "type": "https://example.com/errors/validation-error",
                          "title": "Validation Error",
                          "status": 400,
                          "detail": "Invalid UUID format for userId parameter",
                          "timestamp": "2025-01-27T10:30:00Z"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<FollowStatsResponseDto> getFollowStats(
        @Parameter(
            description = "Unique identifier of the user whose statistics should be retrieved",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        UUID userId);
}
