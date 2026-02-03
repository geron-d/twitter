package com.twitter.common.dto.response.user;

import com.twitter.common.enums.user.UserRole;
import com.twitter.common.enums.user.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for user response data.
 *
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "UserResponse",
    description = "User information returned by the API (excludes sensitive data like password)",
    example = """
        {
          "id": "123e4567-e89b-12d3-a456-426614174000",
          "login": "jane_smith",
          "firstName": "Jane",
          "lastName": "Smith",
          "email": "jane.smith@example.com",
          "status": "ACTIVE",
          "role": "USER",
          "createdAt": "2025-01-21T20:30:00"
        }
        """
)
public record UserResponseDto(
    @Schema(
        description = "Unique identifier for the user",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "User's unique login name",
        example = "jane_smith",
        maxLength = 50
    )
    String login,

    @Schema(
        description = "User's first name (optional)",
        example = "Jane",
        maxLength = 100,
        nullable = true
    )
    String firstName,

    @Schema(
        description = "User's last name (optional)",
        example = "Smith",
        maxLength = 100,
        nullable = true
    )
    String lastName,

    @Schema(
        description = "User's unique email address",
        example = "jane.smith@example.com",
        format = "email"
    )
    String email,

    @Schema(
        description = "Current status of the user account",
        example = "ACTIVE",
        implementation = UserStatus.class
    )
    UserStatus status,

    @Schema(
        description = "Role assigned to the user",
        example = "USER",
        implementation = UserRole.class
    )
    UserRole role,

    @Schema(
        description = "Date and time when the user account was created",
        example = "2025-01-21T20:30:00",
        format = "date-time"
    )
    LocalDateTime createdAt
) {
}
