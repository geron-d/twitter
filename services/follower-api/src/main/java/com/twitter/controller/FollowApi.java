package com.twitter.controller;

import com.twitter.common.dto.request.follow.FollowRequestDto;
import com.twitter.common.dto.response.follow.FollowResponseDto;
import com.twitter.common.dto.response.follow.FollowingResponseDto;
import com.twitter.dto.filter.FollowerFilter;
import com.twitter.dto.filter.FollowingFilter;
import com.twitter.dto.response.FollowStatsResponseDto;
import com.twitter.dto.response.FollowStatusResponseDto;
import com.twitter.dto.response.FollowerResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(
        summary = "Create follow relationship",
        description = "Creates a new follow relationship between two users. " +
            "It performs validation on the request data, checks if both users exist via " +
            "users-api integration, and saves the follow relationship to the database. " +
            "Business rules are enforced: users cannot follow themselves, follow relationships " +
            "must be unique, and both users must exist in the system."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Follow relationship created successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = FollowResponseDto.class)
        )
    )
    ResponseEntity<FollowResponseDto> createFollow(
        @Parameter(description = "Follow relationship data for creation", required = true)
        FollowRequestDto request);

    @Operation(
        summary = "Delete follow relationship",
        description = "Removes an existing follow relationship between two users. " +
            "It checks if the follow relationship exists before attempting to delete it. " +
            "If the relationship does not exist, a 404 Not Found response is returned."
    )
    @ApiResponse(
        responseCode = "204",
        description = "Follow relationship deleted successfully"
    )
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

    @Operation(
        summary = "Get followers list",
        description = "Retrieves a paginated list of followers for a specific user. " +
            "Supports optional filtering by login name (partial match, case-insensitive). " +
            "Results are sorted by creation date in descending order (newest first). " +
            "User login information is retrieved from the users-api service."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Followers retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = PagedModel.class)
        )
    )
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
        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable);

    @Operation(
        summary = "Get following list",
        description = "Retrieves a paginated list of following for a specific user. " +
            "Supports optional filtering by login name (partial match, case-insensitive). " +
            "Results are sorted by creation date in descending order (newest first). " +
            "User login information is retrieved from the users-api service."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Following retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = PagedModel.class)
        )
    )
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
        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable);

    @Operation(
        summary = "Get follow relationship status",
        description = "Retrieves the status of a follow relationship between two users. " +
            "If the relationship exists, it returns isFollowing=true and the creation timestamp. " +
            "If the relationship does not exist, it returns isFollowing=false and createdAt=null."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Follow relationship status retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = FollowStatusResponseDto.class)
        )
    )
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

    @Operation(
        summary = "Get follow statistics",
        description = "Retrieves follow statistics for a specific user. " +
            "Returns the total number of followers (users following this user) and " +
            "the total number of following (users this user is following)."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Follow statistics retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = FollowStatsResponseDto.class)
        )
    )
    ResponseEntity<FollowStatsResponseDto> getFollowStats(
        @Parameter(
            description = "Unique identifier of the user whose statistics should be retrieved",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        UUID userId);
}