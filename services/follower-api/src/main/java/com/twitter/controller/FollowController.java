package com.twitter.controller;

import com.twitter.common.aspect.LoggableRequest;
import com.twitter.dto.filter.FollowerFilter;
import com.twitter.dto.filter.FollowingFilter;
import com.twitter.common.dto.request.follow.FollowRequestDto;
import com.twitter.common.dto.response.follow.FollowResponseDto;
import com.twitter.dto.response.FollowStatsResponseDto;
import com.twitter.dto.response.FollowStatusResponseDto;
import com.twitter.dto.response.FollowerResponseDto;
import com.twitter.common.dto.response.follow.FollowingResponseDto;
import com.twitter.service.FollowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for follow relationship management in Twitter microservices.
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
public class FollowController implements FollowApi {

    private final FollowService followService;

    /**
     * @see FollowApi#createFollow
     */
    @LoggableRequest
    @PostMapping
    @Override
    public ResponseEntity<FollowResponseDto> createFollow(@RequestBody @Valid FollowRequestDto request) {
        FollowResponseDto createdFollow = followService.follow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFollow);
    }

    /**
     * @see FollowApi#deleteFollow
     */
    @LoggableRequest
    @DeleteMapping("/{followerId}/{followingId}")
    @Override
    public ResponseEntity<Void> deleteFollow(
        @PathVariable("followerId") UUID followerId,
        @PathVariable("followingId") UUID followingId) {
        followService.unfollow(followerId, followingId);
        return ResponseEntity.noContent().build();
    }

    /**
     * @see FollowApi#getFollowers
     */
    @LoggableRequest
    @GetMapping("/{userId}/followers")
    @Override
    public PagedModel<FollowerResponseDto> getFollowers(
        @PathVariable("userId") UUID userId,
        @ModelAttribute FollowerFilter filter,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return followService.getFollowers(userId, filter, pageable);
    }

    /**
     * @see FollowApi#getFollowing
     */
    @LoggableRequest
    @GetMapping("/{userId}/following")
    @Override
    public PagedModel<FollowingResponseDto> getFollowing(
        @PathVariable("userId") UUID userId,
        @ModelAttribute FollowingFilter filter,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return followService.getFollowing(userId, filter, pageable);
    }

    /**
     * @see FollowApi#getFollowStatus
     */
    @LoggableRequest
    @GetMapping("/{followerId}/{followingId}/status")
    @Override
    public ResponseEntity<FollowStatusResponseDto> getFollowStatus(
        @PathVariable("followerId") UUID followerId,
        @PathVariable("followingId") UUID followingId) {
        FollowStatusResponseDto status = followService.getFollowStatus(followerId, followingId);
        return ResponseEntity.ok(status);
    }

    /**
     * @see FollowApi#getFollowStats
     */
    @LoggableRequest
    @GetMapping("/{userId}/stats")
    @Override
    public ResponseEntity<FollowStatsResponseDto> getFollowStats(
        @PathVariable("userId") UUID userId) {
        FollowStatsResponseDto stats = followService.getFollowStats(userId);
        return ResponseEntity.ok(stats);
    }
}