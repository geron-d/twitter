package com.twitter.gateway;

import com.twitter.client.UsersApiClient;
import com.twitter.common.dto.UserExistsResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
     * @return true if the user exists, false otherwise
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
}
