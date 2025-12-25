package com.twitter.client;

import com.twitter.common.dto.request.FollowRequestDto;
import com.twitter.common.dto.response.FollowResponseDto;
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
     * <p>
     * This method creates a follow relationship between two users. The follower
     * user will be following the following user. Both user IDs must be valid UUIDs
     * and the users must exist in the system.
     *
     * @param request DTO containing followerId and followingId for the relationship
     * @return FollowResponseDto containing the created follow relationship information including ID
     */
    @PostMapping
    FollowResponseDto createFollow(@RequestBody FollowRequestDto request);
}

