package com.twitter.common.dto.response;

import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for user response data.
 * <p>
 * This record represents the data structure used for returning user information
 * to clients. It excludes sensitive fields like password hash and salt for
 * security purposes while providing all necessary user data for display.
 *
 * @param id        unique identifier for the user
 * @param login     user's login name
 * @param firstName user's first name
 * @param lastName  user's last name
 * @param email     user's email address
 * @param status    current status of the user account
 * @param role      user's role in the system
 * @param createdAt date and time when the user account was created
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
    /**
     * Unique identifier for the user.
     * <p>
     * This field contains the UUID that uniquely identifies the user
     * in the system .
     */
    @Schema(
        description = "Unique identifier for the user",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid"
    )
    UUID id,

    /**
     * User's login name.
     */
    @Schema(
        description = "User's unique login name",
        example = "jane_smith",
        maxLength = 50
    )
    String login,

    /**
     * User's first name.
     * <p>
     * May be null if not provided during registration.
     */
    @Schema(
        description = "User's first name (optional)",
        example = "Jane",
        maxLength = 100,
        nullable = true
    )
    String firstName,

    /**
     * User's last name.
     * <p>
     * May be null if not provided during registration.
     */
    @Schema(
        description = "User's last name (optional)",
        example = "Smith",
        maxLength = 100,
        nullable = true
    )
    String lastName,

    /**
     * User's email address.
     */
    @Schema(
        description = "User's unique email address",
        example = "jane.smith@example.com",
        format = "email"
    )
    String email,

    /**
     * Current status of the user account.
     * <p>
     * Affects the user's ability to access the system.
     */
    @Schema(
        description = "Current status of the user account",
        example = "ACTIVE",
        implementation = UserStatus.class
    )
    UserStatus status,

    /**
     * Role assigned to the user.
     * <p>
     * This field determines the user's permissions and access level
     * within the system.
     */
    @Schema(
        description = "Role assigned to the user",
        example = "USER",
        implementation = UserRole.class
    )
    UserRole role,

    /**
     * Date and time when the user account was created.
     */
    @Schema(
        description = "Date and time when the user account was created",
        example = "2025-01-21T20:30:00",
        format = "date-time"
    )
    LocalDateTime createdAt
) {
}
