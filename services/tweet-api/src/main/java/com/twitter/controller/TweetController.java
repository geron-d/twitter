package com.twitter.controller;

import com.twitter.common.aspect.LoggableRequest;
import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import com.twitter.service.TweetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

