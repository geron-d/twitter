package com.twitter.validation;

import com.twitter.common.dto.request.tweet.CreateTweetRequestDto;
import com.twitter.common.dto.request.tweet.DeleteTweetRequestDto;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.dto.request.UpdateTweetRequestDto;

import java.util.UUID;

/**
 * Interface for tweet validation in Twitter system.
 * <p>
 * This interface centralizes all validation logic extracted from TweetServiceImpl.
 *
 * @author geron
 * @version 1.0
 */
public interface TweetValidator {

    /**
     * Performs complete validation for tweet creation.
     * <p>
     * This method validates tweet data for creation including content validation
     * and user existence checks. It ensures data integrity and business rules compliance.
     *
     * @param requestDto DTO containing tweet data for creation
     * @throws FormatValidationException       if content validation fails
     * @throws BusinessRuleValidationException if user doesn't exist
     */
    void validateForCreate(CreateTweetRequestDto requestDto);

    /**
     * Validates tweet content using Bean Validation and custom rules.
     * <p>
     * This method applies Bean Validation annotations to the request DTO
     * and performs additional custom validation for content rules.
     *
     * @param requestDto DTO containing tweet data to validate
     * @throws FormatValidationException if content validation fails
     */
    void validateContent(CreateTweetRequestDto requestDto);

    /**
     * Validates that the user exists in the system.
     * <p>
     * This method checks if the provided user ID corresponds to an existing user.
     * It will be integrated with users-api service for actual user validation.
     *
     * @param userId the user ID to validate
     * @throws BusinessRuleValidationException if user doesn't exist or userId is null
     */
    void validateUserExists(UUID userId);

    /**
     * Performs complete validation for tweet update.
     * <p>
     * This method validates tweet data for update including:
     * - Existence of the tweet (tweetId must not be null and tweet must exist)
     * - Authorization check (only tweet author can update their tweet)
     * - Content validation (Bean Validation and custom rules)
     *
     * @param tweetId    the unique identifier of the tweet to update
     * @param requestDto DTO containing tweet data for update
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, or access denied (user is not the tweet author)
     * @throws FormatValidationException       if content validation fails (empty, whitespace-only, or constraint violations)
     */
    void validateForUpdate(UUID tweetId, UpdateTweetRequestDto requestDto);

    /**
     * Performs complete validation for tweet deletion.
     * <p>
     * This method validates tweet data for deletion including:
     * - Existence of the tweet (tweetId must not be null and tweet must exist)
     * - State check (tweet must not be already deleted)
     * - Authorization check (only tweet author can delete their tweet)
     *
     * @param tweetId    the unique identifier of the tweet to delete
     * @param requestDto DTO containing userId for authorization check
     * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, is already deleted, or access denied
     */
    void validateForDelete(UUID tweetId, DeleteTweetRequestDto requestDto);

    /**
     * Performs validation for timeline retrieval.
     * <p>
     * This method validates that the user exists in the system before retrieving their timeline.
     * It ensures that the userId is not null and corresponds to an existing user.
     *
     * @param userId the unique identifier of the user whose timeline to retrieve
     * @throws BusinessRuleValidationException if userId is null or user doesn't exist
     */
    void validateForTimeline(UUID userId);
}