package com.twitter.service;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.request.RetweetRequestDto;
import com.twitter.dto.response.RetweetResponseDto;

import java.util.UUID;

/**
 * Service interface for retweet management in Twitter microservices.
 * <p>
 * This interface defines the contract for retweet management services, providing
 * business logic for retweet operations, including validation and data transformation.
 *
 * @author geron
 * @version 1.0
 */
public interface RetweetService {

    /**
     * Retweets a tweet by creating a retweet record with an optional comment.
     * <p>
     * This method performs the following operations:
     * 1. Validates the retweet request (tweet existence, user existence, self-retweet check, uniqueness, comment validation)
     * 2. Creates a Retweet entity from the request data
     * 3. Saves the retweet to the database
     * 4. Updates the tweet's retweets count
     * 5. Converts the saved entity to response DTO
     * 6. Returns the response DTO
     * <p>
     * A user cannot retweet their own tweet, and duplicate retweets are prevented by a unique constraint in the database.
     * The tweet must exist and not be deleted for the retweet operation to succeed.
     * An optional comment can be provided (1-280 characters), but null comment is also allowed.
     * The operation is atomic and executed within a transaction. If any step fails,
     * the entire operation is rolled back.
     *
     * @param tweetId    the unique identifier of the tweet to retweet
     * @param requestDto the retweet request containing userId and optional comment
     * @return RetweetResponseDto containing the created retweet data
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, or self-retweet attempt
     * @throws UniquenessValidationException   if duplicate retweet attempt
     * @throws FormatValidationException       if comment validation fails (empty string or exceeds 280 characters)
     */
    RetweetResponseDto retweetTweet(UUID tweetId, RetweetRequestDto requestDto);

    /**
     * Removes a retweet from a tweet by deleting the retweet record.
     * <p>
     * This method performs the following operations:
     * 1. Validates the retweet removal request (tweet existence, user existence, retweet existence)
     * 2. Finds the retweet record in the database
     * 3. Deletes the retweet record
     * 4. Decrements the tweet's retweets count
     * 5. Saves the updated tweet
     * <p>
     * The operation is atomic and executed within a transaction. If any step fails,
     * the entire operation is rolled back.
     *
     * @param tweetId    the unique identifier of the tweet to remove retweet from
     * @param requestDto the retweet removal request containing userId
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, or retweet doesn't exist
     */
    void removeRetweet(UUID tweetId, RetweetRequestDto requestDto);
}