package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.dto.request.CreateTweetRequestDto;

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
}
