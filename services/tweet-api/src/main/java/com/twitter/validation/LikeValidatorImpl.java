package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.common.dto.request.like.LikeTweetRequestDto;
import com.twitter.entity.Tweet;
import com.twitter.gateway.UserGateway;
import com.twitter.repository.LikeRepository;
import com.twitter.repository.TweetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementation of the like validator for Twitter system.
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikeValidatorImpl implements LikeValidator {

    private final TweetRepository tweetRepository;
    private final LikeRepository likeRepository;
    private final UserGateway userGateway;

    /**
     * @see LikeValidator#validateForLike
     */
    @Override
    public void validateForLike(UUID tweetId, LikeTweetRequestDto requestDto) {
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
            log.warn("Like request is null");
            throw new BusinessRuleValidationException("LIKE_REQUEST_NULL", "Like request cannot be null");
        }

        if (requestDto.userId() == null) {
            log.warn("User ID is null");
            throw new BusinessRuleValidationException("USER_ID_NULL", "User ID cannot be null");
        }

        validateUserExists(requestDto.userId());
        validateNoSelfLike(tweet, requestDto.userId());
        validateUniqueness(tweetId, requestDto.userId());
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
     * Validates that a user cannot like their own tweet.
     * <p>
     * This method checks if the user attempting to like the tweet is the same
     * as the tweet author, which is not allowed by business rules.
     *
     * @param tweet  the tweet being liked
     * @param userId the ID of the user attempting to like the tweet
     * @throws BusinessRuleValidationException if user is the tweet author
     */
    private void validateNoSelfLike(Tweet tweet, UUID userId) {
        if (tweet.getUserId().equals(userId)) {
            log.warn("User {} attempted to like their own tweet {}", userId, tweet.getId());
            throw new BusinessRuleValidationException("SELF_LIKE_NOT_ALLOWED", "User cannot like their own tweet");
        }
    }

    /**
     * Validates that the like does not already exist.
     * <p>
     * This method checks if a like already exists for the given tweet and user
     * using the repository.
     *
     * @param tweetId the ID of the tweet being liked
     * @param userId  the ID of the user attempting to like the tweet
     * @throws UniquenessValidationException if like already exists
     */
    private void validateUniqueness(UUID tweetId, UUID userId) {
        boolean likeExists = likeRepository.existsByTweetIdAndUserId(tweetId, userId);
        if (likeExists) {
            log.warn("Like already exists for tweet {} and user {}", tweetId, userId);
            throw new UniquenessValidationException("like", String.format("tweet %s and user %s", tweetId, userId));
        }
    }

    /**
     * @see LikeValidator#validateForUnlike
     */
    @Override
    public void validateForUnlike(UUID tweetId, LikeTweetRequestDto requestDto) {
        if (tweetId == null) {
            log.warn("Tweet ID is null");
            throw new BusinessRuleValidationException("TWEET_ID_NULL", "Tweet ID cannot be null");
        }

        tweetRepository.findByIdAndIsDeletedFalse(tweetId)
            .orElseThrow(() -> {
                log.warn("Tweet with ID {} not found or is deleted", tweetId);
                return new BusinessRuleValidationException("TWEET_NOT_FOUND", tweetId);
            });

        if (requestDto == null) {
            log.warn("Like request is null");
            throw new BusinessRuleValidationException("LIKE_REQUEST_NULL", "Like request cannot be null");
        }

        if (requestDto.userId() == null) {
            log.warn("User ID is null");
            throw new BusinessRuleValidationException("USER_ID_NULL", "User ID cannot be null");
        }

        validateUserExists(requestDto.userId());
        validateLikeExists(tweetId, requestDto.userId());
    }

    /**
     * Validates that the like exists for the given tweet and user.
     * <p>
     * This method checks if a like exists for the given tweet and user
     * using the repository. This validation is required before removing a like.
     *
     * @param tweetId the ID of the tweet being unliked
     * @param userId  the ID of the user attempting to unlike the tweet
     * @throws BusinessRuleValidationException if like doesn't exist
     */
    private void validateLikeExists(UUID tweetId, UUID userId) {
        boolean likeExists = likeRepository.existsByTweetIdAndUserId(tweetId, userId);
        if (!likeExists) {
            log.warn("Like does not exist for tweet {} and user {}", tweetId, userId);
            throw new BusinessRuleValidationException("LIKE_NOT_FOUND", String.format("Like not found for tweet %s and user %s", tweetId, userId));
        }
    }

    /**
     * @see LikeValidator#validateTweetExists
     */
    @Override
    public void validateTweetExists(UUID tweetId) {
        if (tweetId == null) {
            log.warn("Tweet ID is null");
            throw new BusinessRuleValidationException("TWEET_ID_NULL", "Tweet ID cannot be null");
        }

        tweetRepository.findByIdAndIsDeletedFalse(tweetId)
            .orElseThrow(() -> {
                log.warn("Tweet with ID {} not found or is deleted", tweetId);
                return new BusinessRuleValidationException("TWEET_NOT_FOUND", tweetId);
            });
    }
}