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
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TweetValidatorImpl implements TweetValidator {

    private final Validator validator;
    private final UserGateway userGateway;

    /**
     * @see TweetValidator#validateForCreate
     */
    @Override
    public void validateForCreate(CreateTweetRequestDto requestDto) {
        validateContent(requestDto);
        validateUserExists(requestDto.userId());
    }

    /**
     * @see TweetValidator#validateContent
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

        if (requestDto.content() != null && requestDto.content().trim().isEmpty()) {
            log.warn("Tweet content is empty or contains only whitespace");
            throw new FormatValidationException("content", "EMPTY_CONTENT", "Tweet content cannot be empty");
        }
    }

    /**
     * @see TweetValidator#validateUserExists
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
