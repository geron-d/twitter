package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.request.RetweetRequestDto;

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
     * <ul>
     *   <li>Existence of the tweet (tweetId must not be null and tweet must exist and not be deleted)</li>
     *   <li>Existence of the user (userId must not be null and user must exist)</li>
     *   <li>Self-retweet check (user cannot retweet their own tweet)</li>
     *   <li>Duplicate retweet check (user cannot retweet the same tweet twice)</li>
     *   <li>Comment validation (if comment is not null, it must not be empty string and must not exceed 280 characters)</li>
     * </ul>
     *
     * @param tweetId    the unique identifier of the tweet to retweet
     * @param requestDto DTO containing userId and optional comment for the retweet
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, or self-retweet attempt
     * @throws UniquenessValidationException   if duplicate retweet attempt
     * @throws FormatValidationException       if comment validation fails (empty string or exceeds 280 characters)
     */
    void validateForRetweet(UUID tweetId, RetweetRequestDto requestDto);
}
