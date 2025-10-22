package com.twitter.validation;

import com.twitter.dto.request.CreateTweetRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * Implementation of the tweet validator for Twitter system.
 * <p>
 * This validator centralizes all validation logic extracted from TweetServiceImpl.
 * It provides comprehensive validation for tweet data including content validation,
 * user existence checks, and business rule validation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TweetValidatorImpl implements TweetValidator {

    private final Validator validator;

    /**
     * Performs complete validation for tweet creation.
     * <p>
     * This method validates tweet data for creation including content validation
     * and user existence checks. It ensures data integrity and business rules compliance.
     *
     * @param requestDto DTO containing tweet data for creation
     * @throws ConstraintViolationException if validation fails
     */
    @Override
    public void validateForCreate(CreateTweetRequestDto requestDto) {
        validateContent(requestDto);
        validateUserExists(requestDto.getUserId());
    }

    /**
     * Validates tweet content using Bean Validation and custom rules.
     * <p>
     * This method applies Bean Validation annotations to the request DTO
     * and performs additional custom validation for content rules.
     *
     * @param requestDto DTO containing tweet data to validate
     * @throws ConstraintViolationException if content validation fails
     */
    @Override
    public void validateContent(CreateTweetRequestDto requestDto) {
        Set<ConstraintViolation<CreateTweetRequestDto>> violations = validator.validate(requestDto);

        if (!violations.isEmpty()) {
            log.warn("Validation violations found: {}", violations);
            throw new ConstraintViolationException("Tweet creation validation failed", violations);
        }

        if (requestDto.getContent() != null && requestDto.getContent().trim().isEmpty()) {
            log.warn("Tweet content is empty or contains only whitespace");
            throw new ConstraintViolationException("Tweet content cannot be empty", Set.of());
        }
    }

    /**
     * Validates that the user exists in the system.
     * <p>
     * This method checks if the provided user ID corresponds to an existing user.
     * It will be integrated with users-api service for actual user validation.
     *
     * @param userId the user ID to validate
     * @throws RuntimeException if user doesn't exist
     */
    @Override
    public void validateUserExists(UUID userId) {
        // TODO: Replace with actual users-api integration
        // For now, we'll just log and assume user exists
        log.debug("Validating user existence for ID: {}", userId);

        // Placeholder validation - in real implementation this would call users-api
        if (userId == null) {
            throw new RuntimeException("User ID cannot be null");
        }

        log.debug("User validation passed for ID: {}", userId);
    }
}
