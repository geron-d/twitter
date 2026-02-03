package com.twitter.client;

import com.twitter.common.dto.request.follow.FollowRequestDto;
import com.twitter.common.dto.response.follow.FollowResponseDto;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign Client for integration with Follower API service.
 *
 * @author geron
 * @version 1.0
 */
@FeignClient(
    name = "follower-api",
    url = "${app.follower-api.base-url:http://localhost:8084}",
    path = "/api/v1/follows"
)
public interface FollowApiClient {

    /**
     * Creates a new follow relationship in the follower-api service.
     *
     * @param request DTO containing followerId and followingId for the relationship
     * @return FollowResponseDto containing the created follow relationship information
     * @throws FeignException if the HTTP request fails (e.g., service unavailable, validation error)
     */
    @PostMapping
    FollowResponseDto createFollow(@RequestBody FollowRequestDto request);
}