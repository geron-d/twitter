package com.twitter.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
     * Получает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return данные пользователя
     * @throws org.springframework.web.client.HttpClientErrorException.NotFound если пользователь не найден
     */
    @GetMapping("/{userId}")
    Object getUserById(@PathVariable("userId") UUID userId);
}