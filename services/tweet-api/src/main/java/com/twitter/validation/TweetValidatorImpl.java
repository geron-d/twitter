package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.request.DeleteTweetRequestDto;
import com.twitter.dto.request.UpdateTweetRequestDto;
import com.twitter.entity.Tweet;
import com.twitter.gateway.UserGateway;
import com.twitter.repository.TweetRepository;
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
    private final TweetRepository tweetRepository;

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

        validateContentString(requestDto.content());
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

    /**
     * @see TweetValidator#validateForUpdate
     */
    @Override
    public void validateForUpdate(UUID tweetId, UpdateTweetRequestDto requestDto) {
        if (tweetId == null) {
            log.warn("Tweet ID is null");
            throw new BusinessRuleValidationException("TWEET_ID_NULL", "Tweet ID cannot be null");
        }

        Tweet tweet = tweetRepository.findById(tweetId)
            .orElseThrow(() -> {
                log.warn("Tweet with ID {} not found", tweetId);
                return new BusinessRuleValidationException("TWEET_NOT_FOUND", tweetId);
            });

        validateTweetOwnership(tweet, requestDto.userId());
        validateContent(requestDto);
    }

    /**
     * Validates tweet content using Bean Validation and custom rules.
     *
     * @param requestDto DTO containing tweet data to validate
     * @throws FormatValidationException if content validation fails
     */
    private void validateContent(UpdateTweetRequestDto requestDto) {
        Set<ConstraintViolation<UpdateTweetRequestDto>> violations = validator.validate(requestDto);

        if (!violations.isEmpty()) {
            log.warn("Validation violations found: {}", violations);
            String errorMessage = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
            throw FormatValidationException.beanValidationError("content", "CONTENT_VALIDATION", errorMessage);
        }

        validateContentString(requestDto.content());
    }

    /**
     * Validates that the user performing the update is the tweet author.
     *
     * @param tweet   the tweet to validate
     * @param userId  the user ID performing the update
     * @throws BusinessRuleValidationException if user is not the tweet author
     */
    private void validateTweetOwnership(Tweet tweet, UUID userId) {
        if (!tweet.getUserId().equals(userId)) {
            log.warn("User {} attempted to update tweet {} owned by user {}", userId, tweet.getId(), tweet.getUserId());
            throw new BusinessRuleValidationException("TWEET_ACCESS_DENIED", "Only the tweet author can update their tweet");
        }
    }

    /**
     * @see TweetValidator#validateForDelete
     */
    @Override
    public void validateForDelete(UUID tweetId, DeleteTweetRequestDto requestDto) {
        if (tweetId == null) {
            log.warn("Tweet ID is null");
            throw new BusinessRuleValidationException("TWEET_ID_NULL", "Tweet ID cannot be null");
        }

        Tweet tweet = tweetRepository.findById(tweetId)
            .orElseThrow(() -> {
                log.warn("Tweet with ID {} not found", tweetId);
                return new BusinessRuleValidationException("TWEET_NOT_FOUND", tweetId);
            });

        if (Boolean.TRUE.equals(tweet.getIsDeleted())) {
            log.warn("Tweet with ID {} is already deleted", tweetId);
            throw new BusinessRuleValidationException("TWEET_ALREADY_DELETED", tweetId);
        }

        validateTweetOwnership(tweet, requestDto.userId());
    }

    /**
     * Validates tweet content string using custom rules.
     * <p>
     * This method is reused for both CreateTweetRequestDto and UpdateTweetRequestDto
     * to ensure consistent content validation logic.
     *
     * @param content the tweet content to validate
     * @throws FormatValidationException if content is empty or contains only whitespace
     */
    private void validateContentString(String content) {
        if (content != null && content.trim().isEmpty()) {
            log.warn("Tweet content is empty or contains only whitespace");
            throw new FormatValidationException("content", "EMPTY_CONTENT", "Tweet content cannot be empty");
        }
    }
}
