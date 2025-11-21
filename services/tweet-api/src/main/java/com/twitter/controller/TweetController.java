package com.twitter.controller;

import com.twitter.common.aspect.LoggableRequest;
import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.request.UpdateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import com.twitter.service.TweetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

