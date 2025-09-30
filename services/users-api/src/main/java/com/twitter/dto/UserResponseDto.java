package com.twitter.dto;

import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
import java.util.UUID;

public record UserResponseDto(
    UUID id,
    String login,
    String firstName,
    String lastName,
    String email,
    UserStatus status,
    UserRole role
) {}
