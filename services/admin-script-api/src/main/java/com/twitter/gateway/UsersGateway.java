package com.twitter.gateway;

import com.twitter.client.UsersApiClient;
import com.twitter.common.dto.request.user.UserRequestDto;
import com.twitter.common.dto.response.user.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Gateway for integration with Users API service.
 *
 * @author geron
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UsersGateway {

    private final UsersApiClient usersApiClient;

    /**
     * Creates a new user in the users-api service.
     *
     * @param userRequest DTO containing user data for creation
     * @return UserResponseDto containing the created user information
     * @throws RuntimeException if the user creation fails (e.g., duplicate login/email, service unavailable)
     */
    public UserResponseDto createUser(UserRequestDto userRequest) {
        if (userRequest == null) {
            log.error("Attempted to create user with null request");
            throw new IllegalArgumentException("User request cannot be null");
        }

        try {
            UserResponseDto response = usersApiClient.createUser(userRequest);
            log.info("Successfully created user with ID: {} and login: {}", response.id(), response.login());
            return response;
        } catch (Exception ex) {
            log.error("Failed to create user with login: {}. Error: {}", userRequest.login(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to create user: " + ex.getMessage(), ex);
        }
    }
}

