package com.twitter.service;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.request.FollowRequestDto;
import com.twitter.dto.response.FollowResponseDto;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Service interface for follow relationship management in Twitter microservices.
 * <p>
 * This interface defines the contract for follow relationship management services,
 * providing business logic for follow operations, including creation, validation,
 * and data transformation.
 *
 * @author geron
 * @version 1.0
 */
public interface FollowService {

    /**
     * Creates a new follow relationship from the provided request data.
     * <p>
     * The method is transactional, ensuring data consistency. Business rules are enforced:
     * users cannot follow themselves, follow relationships must be unique, and both users
     * must exist in the system.
     *
     * @param request the follow relationship creation request containing followerId and followingId
     * @return FollowResponseDto containing the created follow relationship data
     * @throws BusinessRuleValidationException if users cannot follow themselves or users don't exist
     * @throws UniquenessValidationException   if follow relationship already exists
     */
    FollowResponseDto follow(FollowRequestDto request);

    /**
     * Removes a follow relationship between two users.
     * <p>
     * The method is transactional, ensuring data consistency. It checks if the follow
     * relationship exists before attempting to delete it. If the relationship does not
     * exist, a ResponseStatusException with HTTP 404 status is thrown.
     *
     * @param followerId  the ID of the user who is following (the follower)
     * @param followingId the ID of the user being followed (the following)
     * @throws ResponseStatusException if the follow relationship does not exist (404 Not Found)
     */
    void unfollow(UUID followerId, UUID followingId);
}

