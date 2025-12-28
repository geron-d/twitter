package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.common.dto.request.FollowRequestDto;
import com.twitter.gateway.UserGateway;
import com.twitter.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementation of the follow relationship validator for Twitter system.
 * <p>
 * This validator centralizes all validation logic for follow relationships,
 * ensuring business rules are enforced before creating follow relationships.
 * It validates self-follow prevention, uniqueness, and user existence.
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FollowValidatorImpl implements FollowValidator {

    private final FollowRepository followRepository;
    private final UserGateway userGateway;

    /**
     * @see FollowValidator#validateForFollow
     */
    @Override
    public void validateForFollow(FollowRequestDto request) {
        if (request == null) {
            log.warn("Follow request is null");
            throw new BusinessRuleValidationException("FOLLOW_REQUEST_NULL", "Follow request cannot be null");
        }

        UUID followerId = request.followerId();
        UUID followingId = request.followingId();

        validateNoSelfFollow(followerId, followingId);
        validateUsersExist(followerId, followingId);
        validateUniqueness(followerId, followingId);
    }

    /**
     * Validates that a user cannot follow themselves.
     * <p>
     * This method checks if followerId equals followingId, which would indicate
     * an attempt to follow oneself. This is not allowed by business rules.
     *
     * @param followerId  the ID of the user who is following
     * @param followingId the ID of the user being followed
     * @throws BusinessRuleValidationException if followerId equals followingId
     */
    private void validateNoSelfFollow(UUID followerId, UUID followingId) {
        if (followerId != null && followerId.equals(followingId)) {
            log.warn("User {} attempted to follow themselves", followerId);
            throw new BusinessRuleValidationException("SELF_FOLLOW_NOT_ALLOWED",
                String.format("User cannot follow themselves (userId=%s)", followerId));
        }
    }

    /**
     * Validates that both users exist in the system.
     *
     * @param followerId  the ID of the user who is following
     * @param followingId the ID of the user being followed
     * @throws BusinessRuleValidationException if either user does not exist
     */
    private void validateUsersExist(UUID followerId, UUID followingId) {
        if (followerId == null) {
            log.warn("Follower ID is null");
            throw new BusinessRuleValidationException("FOLLOWER_ID_NULL", "Follower ID cannot be null");
        }

        if (followingId == null) {
            log.warn("Following ID is null");
            throw new BusinessRuleValidationException("FOLLOWING_ID_NULL", "Following ID cannot be null");
        }

        boolean followerExists = userGateway.existsUser(followerId);
        if (!followerExists) {
            log.warn("Follower with ID {} does not exist", followerId);
            throw new BusinessRuleValidationException("FOLLOWER_NOT_EXISTS", followerId);
        }

        boolean followingExists = userGateway.existsUser(followingId);
        if (!followingExists) {
            log.warn("Following user with ID {} does not exist", followingId);
            throw new BusinessRuleValidationException("FOLLOWING_NOT_EXISTS", followingId);
        }
    }

    /**
     * Validates that the follow relationship does not already exist.
     * <p>
     * This method checks if a follow relationship already exists between
     * the follower and following users using the repository.
     *
     * @param followerId  the ID of the user who is following
     * @param followingId the ID of the user being followed
     * @throws UniquenessValidationException if follow relationship already exists
     */
    private void validateUniqueness(UUID followerId, UUID followingId) {
        boolean exists = followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
        if (exists) {
            log.warn("Follow relationship already exists between follower {} and following {}", followerId, followingId);
            throw new UniquenessValidationException("Follow relationship already exists");
        }
    }
}

