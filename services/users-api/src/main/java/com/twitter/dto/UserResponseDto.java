package com.twitter.dto;

import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;

import java.util.UUID;

/**
 * Data Transfer Object for user response data.
 * <p>
 * This record represents the data structure used for returning user information
 * to clients. It excludes sensitive fields like password hash and salt for
 * security purposes while providing all necessary user data for display.
 *
 * @author geron
 * @version 1.0
 */
public record UserResponseDto(
    /**
     * Unique identifier for the user.
     * <p>
     * This field contains the UUID that uniquely identifies the user
     * in the system and is used for API operations.
     */
    UUID id,

    /**
     * User's login name.
     * <p>
     * This field contains the unique login name used for authentication
     * and display purposes.
     */
    String login,

    /**
     * User's first name.
     * <p>
     * This field contains the user's given name and may be null
     * if not provided during registration.
     */
    String firstName,

    /**
     * User's last name.
     * <p>
     * This field contains the user's family name and may be null
     * if not provided during registration.
     */
    String lastName,

    /**
     * User's email address.
     * <p>
     * This field contains the unique email address used for
     * communication and account recovery.
     */
    String email,

    /**
     * Current status of the user account.
     * <p>
     * This field indicates whether the user account is active or inactive
     * and affects the user's ability to access the system.
     */
    UserStatus status,

    /**
     * Role assigned to the user.
     * <p>
     * This field determines the user's permissions and access level
     * within the system.
     */
    UserRole role
) {
}
