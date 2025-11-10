package com.twitter.client;

import com.twitter.common.dto.UserExistsResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign Client for integration with Users API service.
 *
 * @author geron
 * @version 1.0
 */
@FeignClient(
    name = "users-api",
    url = "${app.users-api.base-url:http://localhost:8081}",
    path = "/api/v1/users"
)
public interface UsersApiClient {

    /**
     * Checks if a user exists by their identifier.
     *
     * @param userId the user identifier to check
     * @return UserExistsResponseDto containing the exists field of type boolean
     */
    @GetMapping("/{userId}/exists")
    UserExistsResponseDto existsUser(@PathVariable("userId") UUID userId);
}