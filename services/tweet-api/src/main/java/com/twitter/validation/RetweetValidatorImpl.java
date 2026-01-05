package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.request.RetweetRequestDto;
import com.twitter.entity.Tweet;
import com.twitter.gateway.UserGateway;
import com.twitter.repository.RetweetRepository;
import com.twitter.repository.TweetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementation of the retweet validator for Twitter system.
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetweetValidatorImpl implements RetweetValidator {

    private final TweetRepository tweetRepository;
    private final RetweetRepository retweetRepository;
    private final UserGateway userGateway;

    /**
     * @see RetweetValidator#validateForRetweet
     */
    @Override
    public void validateForRetweet(UUID tweetId, RetweetRequestDto requestDto) {
        if (tweetId == null) {
            log.warn("Tweet ID is null");
            throw new BusinessRuleValidationException("TWEET_ID_NULL", "Tweet ID cannot be null");
        }

        Tweet tweet = tweetRepository.findByIdAndIsDeletedFalse(tweetId)
            .orElseThrow(() -> {
                log.warn("Tweet with ID {} not found or is deleted", tweetId);
                return new BusinessRuleValidationException("TWEET_NOT_FOUND", tweetId);
            });

        if (requestDto == null) {
            log.warn("Retweet request is null");
            throw new BusinessRuleValidationException("RETWEET_REQUEST_NULL", "Retweet request cannot be null");
        }

        if (requestDto.userId() == null) {
            log.warn("User ID is null");
            throw new BusinessRuleValidationException("USER_ID_NULL", "User ID cannot be null");
        }

        validateUserExists(requestDto.userId());
        validateNoSelfRetweet(tweet, requestDto.userId());
        validateUniqueness(tweetId, requestDto.userId());
        validateComment(requestDto.comment());
    }

    /**
     * Validates that the user exists in the system.
     *
     * @param userId the user ID to validate
     * @throws BusinessRuleValidationException if user doesn't exist
     */
    private void validateUserExists(UUID userId) {
        boolean userExists = userGateway.existsUser(userId);
        if (!userExists) {
            log.warn("User with ID {} does not exist", userId);
            throw new BusinessRuleValidationException("USER_NOT_EXISTS", userId);
        }
    }

    /**
     * Validates that a user cannot retweet their own tweet.
     * <p>
     * This method checks if the user attempting to retweet the tweet is the same
     * as the tweet author, which is not allowed by business rules.
     *
     * @param tweet  the tweet being retweeted
     * @param userId the ID of the user attempting to retweet the tweet
     * @throws BusinessRuleValidationException if user is the tweet author
     */
    private void validateNoSelfRetweet(Tweet tweet, UUID userId) {
        if (tweet.getUserId().equals(userId)) {
            log.warn("User {} attempted to retweet their own tweet {}", userId, tweet.getId());
            throw new BusinessRuleValidationException("SELF_RETWEET_NOT_ALLOWED", "User cannot retweet their own tweet");
        }
    }

    /**
     * Validates that the retweet does not already exist.
     * <p>
     * This method checks if a retweet already exists for the given tweet and user
     * using the repository.
     *
     * @param tweetId the ID of the tweet being retweeted
     * @param userId  the ID of the user attempting to retweet the tweet
     * @throws UniquenessValidationException if retweet already exists
     */
    private void validateUniqueness(UUID tweetId, UUID userId) {
        boolean retweetExists = retweetRepository.existsByTweetIdAndUserId(tweetId, userId);
        if (retweetExists) {
            log.warn("Retweet already exists for tweet {} and user {}", tweetId, userId);
            throw new UniquenessValidationException("retweet", String.format("tweet %s and user %s", tweetId, userId));
        }
    }

    /**
     * Validates the comment field of the retweet request.
     * <p>
     * This method validates that if a comment is provided (not null), it must:
     * <ul>
     *   <li>Not be an empty string (after trimming whitespace)</li>
     *   <li>Not exceed 280 characters (this is also checked by @Size annotation, but validated here for consistency)</li>
     * </ul>
     * Null comments are allowed and will pass validation.
     *
     * @param comment the comment to validate (can be null)
     * @throws FormatValidationException if comment is not null but is empty string or exceeds 280 characters
     */
    private void validateComment(String comment) {
        if (comment == null) {
            return;
        }

        String trimmedComment = comment.trim();
        if (trimmedComment.isEmpty()) {
            log.warn("Comment cannot be empty string (null is allowed)");
            throw new FormatValidationException("comment", "NOT_EMPTY", "Comment cannot be empty string. Use null if no comment is provided.");
        }

        if (trimmedComment.length() > 280) {
            log.warn("Comment exceeds maximum length of 280 characters: {}", trimmedComment.length());
            throw new FormatValidationException("comment", "MAX_LENGTH", String.format("Comment must not exceed 280 characters, but was %d characters", trimmedComment.length()));
        }
    }
}
