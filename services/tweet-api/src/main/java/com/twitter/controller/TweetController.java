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
 * <p>
 * This controller provides CRUD operations for managing tweets with support for
 * validation and integration with users-api service. It handles HTTP requests
 * and delegates business logic to the TweetService layer.
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
     * Creates a new tweet in the system.
     * <p>
     * This endpoint creates a new tweet with the provided content and user ID.
     * It performs validation on the request data, checks if the user exists via
     * users-api integration, and saves the tweet to the database. The tweet content
     * must be between 1 and 280 characters and cannot be empty or only whitespace.
     *
     * @param createTweetRequest DTO containing tweet data (content and userId)
     * @return ResponseEntity containing the created tweet data with HTTP 201 status
     * @throws jakarta.validation.ConstraintViolationException if content validation fails
     * @throws IllegalArgumentException                        if user doesn't exist
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

