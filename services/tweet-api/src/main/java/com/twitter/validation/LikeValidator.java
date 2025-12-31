package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.request.LikeTweetRequestDto;

import java.util.UUID;

/**
 * Interface for like validation in Twitter system.
 * <p>
 * This interface centralizes all validation logic for like operations,
 * ensuring business rules are enforced before creating likes.
 *
 * @author geron
 * @version 1.0
 */
public interface LikeValidator {

    /**
     * Performs complete validation for tweet like operation.
     * <p>
     * This method validates like data including:
     * <ul>
     *   <li>Existence of the tweet (tweetId must not be null and tweet must exist and not be deleted)</li>
     *   <li>Existence of the user (userId must not be null and user must exist)</li>
     *   <li>Self-like check (user cannot like their own tweet)</li>
     *   <li>Duplicate like check (user cannot like the same tweet twice)</li>
     * </ul>
     *
     * @param tweetId    the unique identifier of the tweet to like
     * @param requestDto DTO containing userId for the like
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, or self-like attempt
     * @throws UniquenessValidationException   if duplicate like attempt
     */
    void validateForLike(UUID tweetId, LikeTweetRequestDto requestDto);
}

