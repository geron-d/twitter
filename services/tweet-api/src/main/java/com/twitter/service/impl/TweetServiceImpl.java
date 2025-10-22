package com.twitter.service.impl;

import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import com.twitter.entity.Tweet;
import com.twitter.mapper.TweetMapper;
import com.twitter.repository.TweetRepository;
import com.twitter.service.TweetService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Implementation of TweetService interface.
 * <p>
 * Provides business logic for tweet operations including creation,
 * validation, and data transformation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TweetServiceImpl implements TweetService {

    private final TweetRepository tweetRepository;
    private final TweetMapper tweetMapper;
    private final Validator validator;

    @Override
    @Transactional
    public TweetResponseDto createTweet(CreateTweetRequestDto requestDto) {
        validateRequest(requestDto);
        validateUserExists(requestDto.getUserId());

        Tweet tweet = tweetMapper.toEntity(requestDto);
        Tweet savedTweet = tweetRepository.save(tweet);

        TweetResponseDto responseDto = tweetMapper.toResponseDto(savedTweet);
        return responseDto;
    }

    /**
     * Validates the request DTO using Bean Validation.
     *
     * @param requestDto the request to validate
     * @throws ConstraintViolationException if validation fails
     */
    private void validateRequest(CreateTweetRequestDto requestDto) {
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
     * Validates that the user exists.
     * <p>
     * This is a placeholder method that will be replaced with actual
     * users-api integration in the next steps.
     *
     * @param userId the user ID to validate
     * @throws RuntimeException if user doesn't exist
     */
    private void validateUserExists(java.util.UUID userId) {
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
