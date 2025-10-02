package com.twitter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user update requests.
 * <p>
 * This record represents the data structure used for updating existing users
 * in the system. It includes validation constraints to ensure data
 * integrity and security requirements are met during updates.
 *
 * @author Twitter Team
 * @version 1.0
 */
public record UserUpdateDto(
    /**
     * Unique login name for user authentication.
     * <p>
     * This field must be unique across all users and is required for
     * authentication purposes. Must be between 3 and 50 characters.
     */
    @NotNull(message = "Login cannot be null")
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    String login,

    /**
     * User's first name.
     * <p>
     * This field stores the user's given name and is optional.
     * Used for personalization and display purposes.
     */
    String firstName,

    /**
     * User's last name.
     * <p>
     * This field stores the user's family name and is optional.
     * Used for personalization and display purposes.
     */
    String lastName,

    /**
     * Unique email address for the user.
     * <p>
     * This field must be unique across all users and is required.
     * Used for communication and account recovery purposes.
     * Must be a valid email format.
     */
    @Email(message = "Invalid email format")
    String email,

    /**
     * Password for user authentication.
     * <p>
     * This field contains the plain text password that will be hashed
     * and stored securely. Must be at least 8 characters long for
     * security requirements. Optional for updates.
     */
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password
) {
}
