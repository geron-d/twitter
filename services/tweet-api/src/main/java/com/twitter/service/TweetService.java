package com.twitter.service;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;

/**
 * Service interface for Tweet operations.
 * <p>
 * Defines the contract for tweet-related business logic,
 * including creation, validation, and data transformation.
 */
public interface TweetService {

    /**
     * Creates a new tweet from the provided request data.
     * <p>
     * This method performs the following operations:
     * 1. Validates the request data
     * 2. Checks if the user exists (via users-api integration)
     * 3. Converts the request DTO to Tweet entity
     * 4. Saves the tweet to the database
     * 5. Converts the saved entity to response DTO
     * 6. Returns the response DTO
     *
     * @param requestDto the tweet creation request containing content and userId
     * @return TweetResponseDto containing the created tweet data
     * @throws FormatValidationException       if content validation fails
     * @throws BusinessRuleValidationException if user doesn't exist
     */
    TweetResponseDto createTweet(CreateTweetRequestDto requestDto);
}
