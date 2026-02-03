package com.twitter.validation;

import com.twitter.common.dto.request.retweet.RetweetRequestDto;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;

import java.util.UUID;

/**
 * Interface for retweet validation in Twitter system.
 *
 * @author geron
 * @version 1.0
 */
public interface RetweetValidator {

    /**
     * Performs complete validation for tweet retweet operation.
     * <p>
     * This method validates retweet data including:
     * - Existence of the tweet (tweetId must not be null and tweet must exist and not be deleted)
     * - Existence of the user (userId must not be null and user must exist)
     * - Self-retweet check (user cannot retweet their own tweet)
     * - Duplicate retweet check (user cannot retweet the same tweet twice)
     * - Comment validation (if comment is not null, it must not be empty string and must not exceed 280 characters)
     *
     * @param tweetId    the unique identifier of the tweet to retweet
     * @param requestDto DTO for the retweet
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, or self-retweet attempt
     * @throws UniquenessValidationException   if duplicate retweet attempt
     * @throws FormatValidationException       if comment validation fails (empty string or exceeds 280 characters)
     */
    void validateForRetweet(UUID tweetId, RetweetRequestDto requestDto);

    /**
     * Performs complete validation for tweet retweet removal operation.
     * <p>
     * This method validates retweet removal data including:
     * - Existence of the tweet (tweetId must not be null and tweet must exist and not be deleted)
     * - Existence of the user (userId must not be null and user must exist)
     * - Existence of the retweet (retweet must exist for the given tweet and user)
     *
     * @param tweetId    the unique identifier of the tweet to remove retweet from
     * @param requestDto DTO for the retweet removal
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, or retweet doesn't exist
     */
    void validateForRemoveRetweet(UUID tweetId, RetweetRequestDto requestDto);

    /**
     * Validates that a tweet exists and is not deleted.
     * <p>
     * This method checks if the tweet with the given ID exists in the database
     * and is not soft deleted. This validation is used for read operations like
     * retrieving retweets for a tweet.
     *
     * @param tweetId the unique identifier of the tweet to validate
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, or tweet is deleted
     */
    void validateTweetExists(UUID tweetId);
}