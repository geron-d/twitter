package com.twitter.controller;

import com.twitter.common.aspect.LoggableRequest;
import com.twitter.dto.request.LikeTweetRequestDto;
import com.twitter.dto.response.LikeResponseDto;
import com.twitter.service.LikeService;
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

    /**
     * @see LikeApi#getLikesByTweetId
     */
    @LoggableRequest
    @GetMapping("/{tweetId}/likes")
    @Override
    public PagedModel<LikeResponseDto> getLikesByTweetId(
        @PathVariable("tweetId") UUID tweetId,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<LikeResponseDto> likes = likeService.getLikesByTweetId(tweetId, pageable);
        return new PagedModel<>(likes);
    }
}
