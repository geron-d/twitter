package com.twitter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user creation requests.
 * <p>
 * This record represents the data structure used for creating new users
 * in the system. It includes validation constraints to ensure data
 * integrity and security requirements are met.
 *
 * @param login     unique login name for user authentication
 * @param firstName user's first name
 * @param lastName  user's last name
 * @param email     user's email address
 * @param password  user's password (will be hashed)
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "UserRequest",
    description = "Data structure for creating new users in the system",
    example = """
        {
          "login": "jane_smith",
          "firstName": "Jane",
          "lastName": "Smith",
          "email": "jane.smith@example.com",
          "password": "securePassword123"
        }
        """
)
public record UserRequestDto(

    /**
     * Unique login name for user authentication.
     * <p>
     * This field must be unique across all users and is required for
     * authentication purposes. Must be between 3 and 50 characters.
     */
    @Schema(
        description = "Unique login name for user authentication",
        example = "jane_smith",
        minLength = 3,
        maxLength = 50,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Login cannot be blank")
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    String login,

    /**
     * User's first name.
     */
    @Schema(
        description = "User's first name (optional)",
        example = "Jane",
        maxLength = 100
    )
    String firstName,

    /**
     * User's last name.
     */
    @Schema(
        description = "User's last name (optional)",
        example = "Smith",
        maxLength = 100
    )
    String lastName,

    /**
     * Unique email address for the user.
     * <p>
     * This field must be unique across all users and is required.
     * Must be a valid email format.
     */
    @Schema(
        description = "Unique email address for the user",
        example = "jane.smith@example.com",
        format = "email",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    String email,

    /**
     * Password for user authentication.
     * <p>
     * This field contains the plain text password that will be hashed
     * and stored securely. Must be at least 8 characters long for
     * security requirements.
     */
    @Schema(
        description = "Password for user authentication (will be securely hashed)",
        example = "securePassword123",
        minLength = 8,
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.WRITE_ONLY
    )
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password
) {
}
