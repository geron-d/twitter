package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.request.LikeTweetRequestDto;

import java.util.UUID;

/**
 * Interface for like validation in Twitter system.
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

    /**
     * Performs complete validation for tweet unlike operation.
     * <p>
     * This method validates unlike data including:
     * <ul>
     *   <li>Existence of the tweet (tweetId must not be null and tweet must exist and not be deleted)</li>
     *   <li>Existence of the user (userId must not be null and user must exist)</li>
     *   <li>Existence of the like (like must exist for the given tweet and user)</li>
     * </ul>
     *
     * @param tweetId    the unique identifier of the tweet to unlike
     * @param requestDto DTO containing userId for the unlike
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, or like doesn't exist
     */
    void validateForUnlike(UUID tweetId, LikeTweetRequestDto requestDto);

    /**
     * Validates that a tweet exists and is not deleted.
     * <p>
     * This method checks if the tweet with the given ID exists in the database
     * and is not soft deleted. This validation is used for read operations like
     * retrieving likes for a tweet.
     *
     * @param tweetId the unique identifier of the tweet to validate
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, or tweet is deleted
     */
    void validateTweetExists(UUID tweetId);
}
