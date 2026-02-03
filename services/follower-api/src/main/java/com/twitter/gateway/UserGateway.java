package com.twitter.gateway;

import com.twitter.client.UsersApiClient;
import com.twitter.common.dto.response.user.UserExistsResponseDto;
import com.twitter.common.dto.response.user.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Gateway for integration with Users API service.
 *
 * @author geron
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserGateway {

    private final UsersApiClient usersApiClient;

    /**
     * Checks if a user exists by their identifier.
     *
     * @param userId the user identifier to check
     * @return true if the user exists, false otherwise (including when userId is null or service is unavailable)
     */
    public boolean existsUser(UUID userId) {
        if (userId == null) {
            log.warn("Attempted to check existence of null user ID");
            return false;
        }

        try {
            UserExistsResponseDto response = usersApiClient.existsUser(userId);
            boolean exists = response.exists();
            log.debug("User {} exists: {}", userId, exists);
            return exists;
        } catch (Exception ex) {
            log.debug("User {} does not exist: {}", userId, ex.getMessage());
            return false;
        }
    }

    /**
     * Retrieves user login by user identifier.
     *
     * @param userId the user identifier
     * @return Optional containing user login if user exists, empty otherwise
     */
    public Optional<String> getUserLogin(UUID userId) {
        if (userId == null) {
            log.warn("Attempted to get login for null user ID");
            return Optional.empty();
        }

        try {
            ResponseEntity<UserResponseDto> response = usersApiClient.getUserById(userId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String login = response.getBody().login();
                log.debug("Retrieved login for user {}: {}", userId, login);
                return Optional.of(login);
            } else {
                log.debug("User {} not found or response body is null", userId);
                return Optional.empty();
            }
        } catch (Exception ex) {
            log.debug("Failed to get login for user {}: {}", userId, ex.getMessage());
            return Optional.empty();
        }
    }
}
