package com.twitter.gateway;

import java.util.UUID;

import com.twitter.common.dto.UserExistsResponseDto;
import org.springframework.stereotype.Component;

import com.twitter.client.UsersApiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Gateway для работы с Users API.
 * Предоставляет высокоуровневые методы для проверки существования пользователей.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserGateway {
    
    private final UsersApiClient usersApiClient;
    
    /**
     * Проверяет существование пользователя по его идентификатору.
     * 
     * @param userId идентификатор пользователя для проверки
     * @return true, если пользователь существует, false в противном случае
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
