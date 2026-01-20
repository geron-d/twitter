package com.twitter.service;

import com.twitter.common.dto.request.follow.FollowRequestDto;
import com.twitter.common.dto.response.follow.FollowResponseDto;
import com.twitter.common.dto.response.follow.FollowingResponseDto;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.filter.FollowerFilter;
import com.twitter.dto.filter.FollowingFilter;
import com.twitter.dto.response.FollowStatsResponseDto;
import com.twitter.dto.response.FollowStatusResponseDto;
import com.twitter.dto.response.FollowerResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Service interface for follow relationship management in Twitter microservices.
 * <p>
 * This interface defines the contract for follow relationship management services.
 *
 * @author geron
 * @version 1.0
 */
public interface FollowService {

    /**
     * Creates a new follow relationship from the provided request data.
     *
     * @param request the follow relationship creation request
     * @return FollowResponseDto containing the created follow relationship data
     * @throws BusinessRuleValidationException if users cannot follow themselves or users don't exist
     * @throws UniquenessValidationException   if follow relationship already exists
     */
    FollowResponseDto follow(FollowRequestDto request);

    /**
     * Removes a follow relationship between two users.
     *
     * @param followerId  the ID of the user who is following (the follower)
     * @param followingId the ID of the user being followed (the following)
     * @throws ResponseStatusException if the follow relationship does not exist (404 Not Found)
     */
    void unfollow(UUID followerId, UUID followingId);

    /**
     * Retrieves a paginated list of followers for a specific user.
     * <p>
     * The method retrieves all users who follow the specified user, with optional
     * filtering by login name. The results are paginated and sorted by creation date
     * in descending order (newest first).
     *
     * @param userId   the ID of the user whose followers should be retrieved
     * @param filter   optional filter criteria for filtering followers by login (partial match)
     * @param pageable pagination parameters (page, size, sorting)
     * @return PagedModel containing paginated list of followers
     */
    PagedModel<FollowerResponseDto> getFollowers(UUID userId, FollowerFilter filter, Pageable pageable);

    /**
     * Retrieves a paginated list of following for a specific user.
     * <p>
     * The method retrieves all users that the specified user is following, with optional
     * filtering by login name. The results are paginated and sorted by creation date
     * in descending order (newest first).
     *
     * @param userId   the ID of the user whose following should be retrieved
     * @param filter   optional filter criteria for filtering following by login (partial match)
     * @param pageable pagination parameters (page, size, sorting)
     * @return PagedModel containing paginated list of following
     */
    PagedModel<FollowingResponseDto> getFollowing(UUID userId, FollowingFilter filter, Pageable pageable);

    /**
     * Retrieves the status of a follow relationship between two users.
     *
     * @param followerId  the ID of the user who is following (the follower)
     * @param followingId the ID of the user being followed (the following)
     * @return FollowStatusResponseDto containing the status of the follow relationship
     */
    FollowStatusResponseDto getFollowStatus(UUID followerId, UUID followingId);

    /**
     * Retrieves follow statistics for a specific user.
     * <p>
     * The method calculates and returns the total number of followers (users following
     * this user) and the total number of following (users this user is following).
     *
     * @param userId the ID of the user whose statistics should be retrieved
     * @return FollowStatsResponseDto containing followers count and following count
     */
    FollowStatsResponseDto getFollowStats(UUID userId);
}