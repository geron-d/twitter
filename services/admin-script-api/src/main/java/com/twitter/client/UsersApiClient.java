package com.twitter.client;

import com.twitter.common.dto.request.UserRequestDto;
import com.twitter.common.dto.response.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
     * Creates a new user in the users-api service.
     *
     * @param userRequest DTO containing user data for creation
     * @return UserResponseDto containing the created user information including ID
     */
    @PostMapping
    UserResponseDto createUser(@RequestBody UserRequestDto userRequest);
}

