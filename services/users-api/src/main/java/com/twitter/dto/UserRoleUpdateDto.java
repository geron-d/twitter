package com.twitter.dto;

import com.twitter.common.enums.UserRole;
import jakarta.validation.constraints.NotNull;

/**
 * DTO для обновления роли пользователя
 */
public record UserRoleUpdateDto(
    @NotNull(message = "Role cannot be null")
    UserRole role
) {
}
