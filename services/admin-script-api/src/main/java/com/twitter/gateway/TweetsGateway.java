package com.twitter.gateway;

import com.twitter.client.TweetsApiClient;
import com.twitter.common.dto.request.CreateTweetRequestDto;
import com.twitter.common.dto.request.DeleteTweetRequestDto;
import com.twitter.common.dto.request.LikeTweetRequestDto;
import com.twitter.common.dto.request.RetweetRequestDto;
import com.twitter.common.dto.response.LikeResponseDto;
import com.twitter.common.dto.response.RetweetResponseDto;
import com.twitter.common.dto.response.TweetResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Gateway for integration with Tweets API service.
 *
 * @author geron
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TweetsGateway {

    private final TweetsApiClient tweetsApiClient;

    /**
     * Creates a new tweet in the tweet-api service.
     *
     * @param createTweetRequest DTO containing tweet data for creation (content and userId)
     * @return TweetResponseDto containing the created tweet information including ID
     * @throws RuntimeException if the tweet creation fails (e.g., invalid content, user not found, service unavailable)
     */
    public TweetResponseDto createTweet(CreateTweetRequestDto createTweetRequest) {
        if (createTweetRequest == null) {
            log.error("Attempted to create tweet with null request");
            throw new IllegalArgumentException("Create tweet request cannot be null");
        }

        try {
            TweetResponseDto response = tweetsApiClient.createTweet(createTweetRequest);
            log.info("Successfully created tweet with ID: {} for user: {}", response.id(), response.userId());
            return response;
        } catch (Exception ex) {
            log.error("Failed to create tweet for user: {}. Error: {}", createTweetRequest.userId(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to create tweet: " + ex.getMessage(), ex);
        }
    }

    /**
     * Deletes a tweet by performing soft delete in the tweet-api service.
     *
     * @param tweetId            the unique identifier of the tweet to delete
     * @param deleteTweetRequest DTO containing userId for authorization check
     * @throws RuntimeException if the tweet deletion fails (e.g., tweet not found, unauthorized, service unavailable)
     */
    public void deleteTweet(UUID tweetId, DeleteTweetRequestDto deleteTweetRequest) {
        if (tweetId == null) {
            log.error("Attempted to delete tweet with null tweet ID");
            throw new IllegalArgumentException("Tweet ID cannot be null");
        }
        if (deleteTweetRequest == null) {
            log.error("Attempted to delete tweet with null delete request");
            throw new IllegalArgumentException("Delete tweet request cannot be null");
        }

        try {
            tweetsApiClient.deleteTweet(tweetId, deleteTweetRequest);
            log.info("Successfully deleted tweet with ID: {} for user: {}", tweetId, deleteTweetRequest.userId());
        } catch (Exception ex) {
            log.error("Failed to delete tweet with ID: {} for user: {}. Error: {}",
                tweetId, deleteTweetRequest.userId(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to delete tweet: " + ex.getMessage(), ex);
        }
    }

    /**
     * Retrieves a paginated list of tweets for a specific user.
     *
     * @param userId   the unique identifier of the user whose tweets to retrieve
     * @param pageable pagination parameters (page, size, sorting). Use large page size to get all tweets
     * @return Page containing paginated list of tweets with metadata
     * @throws RuntimeException if the tweet retrieval fails (e.g., user not found, service unavailable)
     */
    public Page<TweetResponseDto> getUserTweets(UUID userId, Pageable pageable) {
        if (userId == null) {
            log.error("Attempted to get tweets for null user ID");
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (pageable == null) {
            log.error("Attempted to get tweets with null pageable");
            throw new IllegalArgumentException("Pageable cannot be null");
        }

        try {
            Page<TweetResponseDto> response = tweetsApiClient.getUserTweets(userId, pageable);
            log.info("Successfully retrieved {} tweets for user: {}", response.getTotalElements(), userId);
            return response;
        } catch (Exception ex) {
            log.error("Failed to retrieve tweets for user: {}. Error: {}", userId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to retrieve user tweets: " + ex.getMessage(), ex);
        }
    }

    /**
     * Likes a tweet in the tweet-api service.
     *
     * @param tweetId          the unique identifier of the tweet to like
     * @param likeTweetRequest DTO containing userId for the like operation
     * @return LikeResponseDto containing the created like information including ID, tweetId, userId, and createdAt
     * @throws IllegalArgumentException if tweetId or likeTweetRequest is null
     * @throws RuntimeException if the like operation fails (e.g., service unavailable, network error)
     */
    public LikeResponseDto likeTweet(UUID tweetId, LikeTweetRequestDto likeTweetRequest) {
        if (tweetId == null) {
            log.error("Attempted to like tweet with null tweet ID");
            throw new IllegalArgumentException("Tweet ID cannot be null");
        }
        if (likeTweetRequest == null) {
            log.error("Attempted to like tweet with null like request");
            throw new IllegalArgumentException("Like tweet request cannot be null");
        }

        try {
            LikeResponseDto response = tweetsApiClient.likeTweet(tweetId, likeTweetRequest);
            log.info("Successfully created like for tweet {} by user {}", tweetId, likeTweetRequest.userId());
            return response;
        } catch (Exception ex) {
            log.error("Failed to create like for tweet {} by user {}. Error: {}",
                tweetId, likeTweetRequest.userId(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to create like: " + ex.getMessage(), ex);
        }
    }

    /**
     * Retweets a tweet in the tweet-api service.
     *
     * @param tweetId         the unique identifier of the tweet to retweet
     * @param retweetRequest  DTO containing userId and optional comment for the retweet operation
     * @return RetweetResponseDto containing the created retweet information including ID, tweetId, userId, comment, and createdAt
     * @throws IllegalArgumentException if tweetId or retweetRequest is null
     * @throws RuntimeException if the retweet operation fails (e.g., service unavailable, network error)
     */
    public RetweetResponseDto retweetTweet(UUID tweetId, RetweetRequestDto retweetRequest) {
        if (tweetId == null) {
            log.error("Attempted to retweet tweet with null tweet ID");
            throw new IllegalArgumentException("Tweet ID cannot be null");
        }
        if (retweetRequest == null) {
            log.error("Attempted to retweet tweet with null retweet request");
            throw new IllegalArgumentException("Retweet request cannot be null");
        }

        try {
            RetweetResponseDto response = tweetsApiClient.retweetTweet(tweetId, retweetRequest);
            log.info("Successfully created retweet for tweet {} by user {}", tweetId, retweetRequest.userId());
            return response;
        } catch (Exception ex) {
            log.error("Failed to create retweet for tweet {} by user {}. Error: {}",
                tweetId, retweetRequest.userId(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to create retweet: " + ex.getMessage(), ex);
        }
    }
}
