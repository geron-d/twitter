package com.twitter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
 * @author geron
 * @version 1.0
 * @param login unique login name for user authentication
 * @param firstName user's first name
 * @param lastName user's last name
 * @param email user's email address
 * @param password user's password (will be hashed)
 */
@Schema(
    name = "UserUpdate",
    description = "Data structure for updating existing users in the system",
    example = """
        {
          "login": "jane_smith_updated",
          "firstName": "Jane",
          "lastName": "Smith-Wilson",
          "email": "jane.wilson@example.com",
          "password": "newSecurePassword123"
        }
        """
)
public record UserUpdateDto(
    /**
     * Unique login name for user authentication.
     * <p>
     * This field must be unique across all users and is required for
     * authentication purposes. Must be between 3 and 50 characters.
     */
    @Schema(
        description = "Unique login name for user authentication",
        example = "jane_smith_updated",
        minLength = 3,
        maxLength = 50,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Login cannot be null")
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    String login,

    /**
     * User's first name.
     * <p>
     * This field stores the user's given name and is optional.
     * Used for personalization and display purposes.
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
     * This field stores the user's family name and is optional.
     * Used for personalization and display purposes.
     */
    @Schema(
        description = "User's last name (optional)",
        example = "Smith-Wilson",
        maxLength = 100,
        nullable = true
    )
    String lastName,

    /**
     * Unique email address for the user.
     * <p>
     * This field must be unique across all users and is required.
     * Used for communication and account recovery purposes.
     * Must be a valid email format.
     */
    @Schema(
        description = "Unique email address for the user",
        example = "jane.wilson@example.com",
        format = "email",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Email(message = "Invalid email format")
    String email,

    /**
     * Password for user authentication.
     * <p>
     * This field contains the plain text password that will be hashed
     * and stored securely. Must be at least 8 characters long for
     * security requirements. Optional for updates.
     */
    @Schema(
        description = "Password for user authentication (will be securely hashed, optional for updates)",
        example = "newSecurePassword123",
        minLength = 8,
        nullable = true,
        accessMode = Schema.AccessMode.WRITE_ONLY
    )
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password
) {
}
