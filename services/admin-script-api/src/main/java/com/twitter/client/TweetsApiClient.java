package com.twitter.client;

import com.twitter.common.dto.request.tweet.CreateTweetRequestDto;
import com.twitter.common.dto.request.tweet.DeleteTweetRequestDto;
import com.twitter.common.dto.request.LikeTweetRequestDto;
import com.twitter.common.dto.request.RetweetRequestDto;
import com.twitter.common.dto.response.LikeResponseDto;
import com.twitter.common.dto.response.RetweetResponseDto;
import com.twitter.common.dto.response.TweetResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Feign Client for integration with Tweets API service.
 *
 * @author geron
 * @version 1.0
 */
@FeignClient(
    name = "tweet-api",
    url = "${app.tweet-api.base-url:http://localhost:8082}",
    path = "/api/v1/tweets"
)
public interface TweetsApiClient {

    /**
     * Creates a new tweet in the tweet-api service.
     * <p>
     * The tweet content must be between 1 and 280 characters.
     *
     * @param createTweetRequest DTO containing tweet data for creation (content and userId)
     * @return TweetResponseDto containing the created tweet information including ID
     */
    @PostMapping
    TweetResponseDto createTweet(@RequestBody CreateTweetRequestDto createTweetRequest);

    /**
     * Deletes a tweet by performing soft delete in the tweet-api service.
     *
     * @param tweetId            the unique identifier of the tweet to delete
     * @param deleteTweetRequest DTO containing userId for authorization check
     */
    @DeleteMapping("/{tweetId}")
    void deleteTweet(
        @PathVariable("tweetId") UUID tweetId,
        @RequestBody DeleteTweetRequestDto deleteTweetRequest
    );

    /**
     * Retrieves a paginated list of tweets for a specific user.
     *
     * @param userId   the unique identifier of the user whose tweets to retrieve
     * @param pageable pagination parameters (page, size, sorting). Use large page size to get all tweets
     * @return Page containing paginated list of tweets with metadata
     */
    @GetMapping("/user/{userId}")
    Page<TweetResponseDto> getUserTweets(
        @PathVariable("userId") UUID userId,
        @SpringQueryMap Pageable pageable
    );

    /**
     * Likes a tweet in the tweet-api service.
     *
     * @param tweetId          the unique identifier of the tweet to like
     * @param likeTweetRequest DTO containing userId for the like operation
     * @return LikeResponseDto containing the created like information including ID, tweetId, userId, and createdAt
     */
    @PostMapping("/{tweetId}/like")
    LikeResponseDto likeTweet(
        @PathVariable("tweetId") UUID tweetId,
        @RequestBody LikeTweetRequestDto likeTweetRequest
    );

    /**
     * Retweets a tweet in the tweet-api service.
     *
     * @param tweetId         the unique identifier of the tweet to retweet
     * @param retweetRequest  DTO containing userId and optional comment for the retweet operation
     * @return RetweetResponseDto containing the created retweet information including ID, tweetId, userId, comment, and createdAt
     */
    @PostMapping("/{tweetId}/retweet")
    RetweetResponseDto retweetTweet(
        @PathVariable("tweetId") UUID tweetId,
        @RequestBody RetweetRequestDto retweetRequest
    );
}
