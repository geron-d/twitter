package com.twitter.client;

import com.twitter.common.dto.UserExistsResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign Client для интеграции с Users API.
 * Предоставляет методы для HTTP запросов к users-api сервису.
 */
@FeignClient(
    name = "users-api",
    url = "${app.users-api.base-url:http://localhost:8081}",
    path = "/api/v1/users"
)
public interface UsersApiClient {

    /**
     * Проверяет существование пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя для проверки
     * @return UserExistsResponseDto с полем exists типа boolean
     */
    @GetMapping("/{userId}/exists")
    UserExistsResponseDto existsUser(@PathVariable("userId") UUID userId);
}