package com.twitter.service;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for tweet management in Twitter microservices.
 * <p>
 * This interface defines the contract for tweet management services, providing
 * business logic for tweet operations, including creation, validation,
 * and data transformation.
 *
 * @author geron
 * @version 1.0
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

    /**
     * Retrieves a tweet by its unique identifier.
     * <p>
     * This method retrieves a tweet from the database by its UUID identifier.
     * Returns an empty Optional if the tweet does not exist.
     *
     * @param tweetId the unique identifier of the tweet
     * @return Optional containing tweet data or empty if not found
     */
    Optional<TweetResponseDto> getTweetById(UUID tweetId);
}
