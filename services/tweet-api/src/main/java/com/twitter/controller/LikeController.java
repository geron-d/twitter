package com.twitter.controller;

import com.twitter.common.aspect.LoggableRequest;
import com.twitter.dto.request.LikeTweetRequestDto;
import com.twitter.dto.response.LikeResponseDto;
import com.twitter.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for like management in Twitter microservices.
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tweets")
@RequiredArgsConstructor
public class LikeController implements LikeApi {

    private final LikeService likeService;

    /**
     * @see LikeApi#likeTweet
     */
    @LoggableRequest
    @PostMapping("/{tweetId}/like")
    @Override
    public ResponseEntity<LikeResponseDto> likeTweet(
        @PathVariable("tweetId") UUID tweetId,
        @RequestBody @Valid LikeTweetRequestDto likeTweetRequest) {
        LikeResponseDto createdLike = likeService.likeTweet(tweetId, likeTweetRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLike);
    }

    /**
     * @see LikeApi#removeLike
     */
    @LoggableRequest
    @DeleteMapping("/{tweetId}/like")
    @Override
    public ResponseEntity<Void> removeLike(
        @PathVariable("tweetId") UUID tweetId,
        @RequestBody @Valid LikeTweetRequestDto likeTweetRequest) {
        likeService.removeLike(tweetId, likeTweetRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

