package com.twitter.service;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.request.LikeTweetRequestDto;
import com.twitter.dto.response.LikeResponseDto;

import java.util.UUID;

/**
 * Service interface for like management in Twitter microservices.
 * <p>
 * This interface defines the contract for like management services, providing
 * business logic for like operations, including validation and data transformation.
 *
 * @author geron
 * @version 1.0
 */
public interface LikeService {

    /**
     * Likes a tweet by creating a like record.
     * <p>
     * This method performs the following operations:
     * 1. Validates the like request (tweet existence, user existence, self-like check, uniqueness)
     * 2. Creates a Like entity from the request data
     * 3. Saves the like to the database
     * 4. Converts the saved entity to response DTO
     * 5. Returns the response DTO
     * <p>
     * A user cannot like their own tweet, and duplicate likes are prevented by a unique constraint in the database. 
     * The tweet must exist and not be deleted for the like operation to succeed.
     *
     * @param tweetId    the unique identifier of the tweet to like
     * @param requestDto the like request containing userId
     * @return LikeResponseDto containing the created like data
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, or self-like attempt
     * @throws UniquenessValidationException   if duplicate like attempt
     */
    LikeResponseDto likeTweet(UUID tweetId, LikeTweetRequestDto requestDto);
}

