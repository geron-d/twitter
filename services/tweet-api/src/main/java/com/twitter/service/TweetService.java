package com.twitter.service;

import com.twitter.common.dto.request.CreateTweetRequestDto;
import com.twitter.common.dto.request.DeleteTweetRequestDto;
import com.twitter.common.dto.response.TweetResponseDto;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.dto.request.UpdateTweetRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    /**
     * Updates an existing tweet with new content.
     * <p>
     * This method performs the following operations:
     * 1. Validates the update request (tweet existence, authorization, content validation)
     * 2. Retrieves the tweet from the database
     * 3. Updates the tweet entity with new content using mapper (preserves system fields)
     * 4. Saves the updated tweet to the database
     * 5. Converts the saved entity to response DTO
     * 6. Returns the response DTO
     * <p>
     * The method is transactional, ensuring data consistency. Only the tweet author
     * can update their tweet. The tweet content must be between 1 and 280 characters.
     *
     * @param tweetId    the unique identifier of the tweet to update
     * @param requestDto the tweet update request containing new content and userId
     * @return TweetResponseDto containing the updated tweet data with updated timestamp
     * @throws FormatValidationException       if content validation fails (empty, whitespace-only, or constraint violations)
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, or access denied (user is not the tweet author)
     */
    TweetResponseDto updateTweet(UUID tweetId, UpdateTweetRequestDto requestDto);

    /**
     * Deletes a tweet by performing soft delete.
     * <p>
     * This method performs the following operations:
     * 1. Validates the delete request (tweet existence, state check, authorization)
     * 2. Retrieves the tweet from the database
     * 3. Performs soft delete by setting isDeleted flag and deletedAt timestamp
     * 4. Saves the updated tweet to the database
     * <p>
     * The method is transactional, ensuring data consistency. Only the tweet author
     * can delete their tweet. The tweet data is preserved in the database for analytics
     * and recovery purposes. Returns void as the response is 204 No Content.
     *
     * @param tweetId    the unique identifier of the tweet to delete
     * @param requestDto the tweet deletion request containing userId for authorization check
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, is already deleted, or access denied
     */
    void deleteTweet(UUID tweetId, DeleteTweetRequestDto requestDto);

    /**
     * Retrieves a paginated list of tweets for a specific user.
     * <p>
     * The method is read-only and transactional, ensuring data consistency. Tweets are
     * sorted by creation date in descending order (newest first). Deleted tweets are
     * automatically excluded from the results. The Page object contains pagination
     * metadata (totalElements, totalPages, number, size) that can be used by the controller
     * to create HATEOAS links.
     *
     * @param userId   the unique identifier of the user whose tweets to retrieve
     * @param pageable pagination parameters (page, size, sorting)
     * @return Page containing paginated list of tweets with metadata
     * @throws FormatValidationException       if userId is null or invalid
     * @throws BusinessRuleValidationException if user doesn't exist (optional validation)
     */
    Page<TweetResponseDto> getUserTweets(UUID userId, Pageable pageable);
}
