package com.twitter.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Data Transfer Object for user creation requests (external API).
 * <p>
 * This record represents the data structure used for creating new users
 * in the users-api service via Feign Client.
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
@Builder
public record UserRequestDto(

    /**
     * Unique login name for user authentication.
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

