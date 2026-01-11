package com.twitter.service;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.common.dto.request.LikeTweetRequestDto;
import com.twitter.common.dto.response.LikeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    /**
     * Removes a like from a tweet by deleting the like record.
     * <p>
     * This method performs the following operations:
     * 1. Validates the unlike request (tweet existence, user existence, like existence)
     * 2. Finds the like record in the database
     * 3. Deletes the like record
     * 4. Decrements the tweet's likes count
     * 5. Saves the updated tweet
     * <p>
     * The operation is atomic and executed within a transaction. If any step fails,
     * the entire operation is rolled back.
     *
     * @param tweetId    the unique identifier of the tweet to unlike
     * @param requestDto the unlike request containing userId
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, or like doesn't exist
     */
    void removeLike(UUID tweetId, LikeTweetRequestDto requestDto);

    /**
     * Retrieves a paginated list of likes for a specific tweet.
     * <p>
     * This method performs the following operations:
     * 1. Validates that the tweet exists and is not deleted
     * 2. Retrieves likes from the database with pagination support
     * 3. Converts Like entities to LikeResponseDto
     * 4. Returns a Page containing paginated likes with metadata
     * <p>
     * The method is read-only and transactional, ensuring data consistency. Likes are
     * sorted by creation date in descending order (newest first). If the tweet doesn't
     * exist or is deleted, a BusinessRuleValidationException is thrown. If there are no
     * likes for the tweet, an empty page is returned (not an error).
     *
     * @param tweetId  the unique identifier of the tweet whose likes to retrieve
     * @param pageable pagination parameters (page, size, sorting)
     * @return Page containing paginated list of likes with metadata
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, or tweet is deleted
     */
    Page<LikeResponseDto> getLikesByTweetId(UUID tweetId, Pageable pageable);
}