package com.twitter.controller;

import com.twitter.common.aspect.LoggableRequest;
import com.twitter.dto.request.FollowRequestDto;
import com.twitter.dto.response.FollowResponseDto;
import com.twitter.service.FollowService;
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
}

