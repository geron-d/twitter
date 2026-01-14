package com.twitter.controller;

import com.twitter.common.aspect.LoggableRequest;
import com.twitter.common.dto.request.RetweetRequestDto;
import com.twitter.common.dto.response.RetweetResponseDto;
import com.twitter.service.RetweetService;
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
 * REST controller for retweet management in Twitter microservices.
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tweets")
@RequiredArgsConstructor
public class RetweetController implements RetweetApi {

    private final RetweetService retweetService;

    /**
     * @see RetweetApi#retweetTweet
     */
    @LoggableRequest
    @PostMapping("/{tweetId}/retweet")
    @Override
    public ResponseEntity<RetweetResponseDto> retweetTweet(
        @PathVariable("tweetId") UUID tweetId,
        @RequestBody @Valid RetweetRequestDto retweetRequest) {
        RetweetResponseDto createdRetweet = retweetService.retweetTweet(tweetId, retweetRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRetweet);
    }

    /**
     * @see RetweetApi#removeRetweet
     */
    @LoggableRequest
    @DeleteMapping("/{tweetId}/retweet")
    @Override
    public ResponseEntity<Void> removeRetweet(
        @PathVariable("tweetId") UUID tweetId,
        @RequestBody @Valid RetweetRequestDto retweetRequest) {
        retweetService.removeRetweet(tweetId, retweetRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * @see RetweetApi#getRetweetsByTweetId
     */
    @LoggableRequest
    @GetMapping("/{tweetId}/retweets")
    @Override
    public PagedModel<RetweetResponseDto> getRetweetsByTweetId(
        @PathVariable("tweetId") UUID tweetId,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<RetweetResponseDto> retweets = retweetService.getRetweetsByTweetId(tweetId, pageable);
        return new PagedModel<>(retweets);
    }
}