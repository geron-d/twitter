package com.twitter.client;

import com.twitter.common.dto.response.FollowingResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

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
public interface FollowerApiClient {

    /**
     * Retrieves a paginated list of users that the specified user is following.
     *
     * @param userId   the unique identifier of the user whose following list to retrieve
     * @param pageable pagination parameters (page, size, sorting)
     * @return PagedModel containing paginated list of FollowingResponseDto with metadata
     */
    @GetMapping("/{userId}/following")
    Page<FollowingResponseDto> getFollowing(
        @PathVariable("userId") UUID userId,
        @SpringQueryMap Pageable pageable
    );
}


