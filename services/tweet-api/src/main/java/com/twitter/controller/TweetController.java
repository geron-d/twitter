package com.twitter.controller;

import com.twitter.common.aspect.LoggableRequest;
import com.twitter.common.dto.request.CreateTweetRequestDto;
import com.twitter.common.dto.request.DeleteTweetRequestDto;
import com.twitter.common.dto.response.TweetResponseDto;
import com.twitter.dto.request.UpdateTweetRequestDto;
import com.twitter.service.TweetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for tweet management in Twitter microservices.
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tweets")
@RequiredArgsConstructor
public class TweetController implements TweetApi {

    private final TweetService tweetService;

    /**
     * @see TweetApi#createTweet
     */
    @LoggableRequest
    @PostMapping
    @Override
    public ResponseEntity<TweetResponseDto> createTweet(
        @RequestBody @Valid CreateTweetRequestDto createTweetRequest) {
        TweetResponseDto createdTweet = tweetService.createTweet(createTweetRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTweet);
    }

    /**
     * @see TweetApi#getTweetById
     */
    @LoggableRequest
    @GetMapping("/{tweetId}")
    @Override
    public ResponseEntity<TweetResponseDto> getTweetById(
        @PathVariable("tweetId") UUID tweetId) {
        return tweetService.getTweetById(tweetId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @see TweetApi#updateTweet
     */
    @LoggableRequest
    @PutMapping("/{tweetId}")
    @Override
    public ResponseEntity<TweetResponseDto> updateTweet(
        @PathVariable("tweetId") UUID tweetId,
        @RequestBody @Valid UpdateTweetRequestDto updateTweetRequest) {
        TweetResponseDto updatedTweet = tweetService.updateTweet(tweetId, updateTweetRequest);
        return ResponseEntity.ok(updatedTweet);
    }

    /**
     * @see TweetApi#deleteTweet
     */
    @LoggableRequest
    @DeleteMapping("/{tweetId}")
    @Override
    public ResponseEntity<Void> deleteTweet(
        @PathVariable("tweetId") UUID tweetId,
        @RequestBody @Valid DeleteTweetRequestDto deleteTweetRequest) {
        tweetService.deleteTweet(tweetId, deleteTweetRequest);
        return ResponseEntity.noContent().build();
    }

    /**
     * @see TweetApi#getUserTweets
     */
    @LoggableRequest
    @GetMapping("/user/{userId}")
    @Override
    public PagedModel<TweetResponseDto> getUserTweets(
        @PathVariable("userId") UUID userId,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TweetResponseDto> tweets = tweetService.getUserTweets(userId, pageable);
        return new PagedModel<>(tweets);
    }
}

