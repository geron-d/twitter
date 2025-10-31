package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.gateway.UserGateway;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final UserGateway userGateway;

    /**
     * Performs complete validation for tweet creation.
     * <p>
     * This method validates tweet data for creation including content validation
     * and user existence checks. It ensures data integrity and business rules compliance.
     *
     * @param requestDto DTO containing tweet data for creation
     * @throws FormatValidationException if content validation fails
     * @throws BusinessRuleValidationException if user doesn't exist
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
     * @throws FormatValidationException if content validation fails
     */
    @Override
    public void validateContent(CreateTweetRequestDto requestDto) {
        Set<ConstraintViolation<CreateTweetRequestDto>> violations = validator.validate(requestDto);

        if (!violations.isEmpty()) {
            log.warn("Validation violations found: {}", violations);
            String errorMessage = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
            throw FormatValidationException.beanValidationError("content", "CONTENT_VALIDATION", errorMessage);
        }

        if (requestDto.getContent() != null && requestDto.getContent().trim().isEmpty()) {
            log.warn("Tweet content is empty or contains only whitespace");
            throw new FormatValidationException("content", "EMPTY_CONTENT", "Tweet content cannot be empty");
        }
    }

    /**
     * Validates that the user exists in the system.
     * <p>
     * This method checks if the provided user ID corresponds to an existing user.
     * It integrates with users-api service through UserGateway for actual user validation.
     *
     * @param userId the user ID to validate
     * @throws BusinessRuleValidationException if user doesn't exist or userId is null
     */
    @Override
    public void validateUserExists(UUID userId) {
        if (userId == null) {
            log.warn("User ID is null");
            throw new BusinessRuleValidationException("USER_ID_NULL", "User ID cannot be null");
        }

        boolean userExists = userGateway.existsUser(userId);
        if (!userExists) {
            log.warn("User with ID {} does not exist", userId);
            throw new BusinessRuleValidationException("USER_NOT_EXISTS", userId);
        }
    }
}
