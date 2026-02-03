package com.twitter.common.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Data Transfer Object for user creation requests.
 *
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

    @Schema(
        description = "User's first name (optional)",
        example = "Jane",
        maxLength = 100
    )
    String firstName,

    @Schema(
        description = "User's last name (optional)",
        example = "Smith",
        maxLength = 100
    )
    String lastName,

    @Schema(
        description = "Unique email address for the user",
        example = "jane.smith@example.com",
        format = "email",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    String email,

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

