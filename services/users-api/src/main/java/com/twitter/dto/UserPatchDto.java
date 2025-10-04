package com.twitter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Data Transfer Object for user PATCH operations.
 * <p>
 * This class represents the data structure used for partial user updates
 * via JSON Patch operations. It includes validation constraints to ensure
 * data integrity while allowing selective field updates.
 *
 * @author geron
 * @version 1.0
 */
@Data
@RequiredArgsConstructor
public class UserPatchDto {

    /**
     * Unique login name for user authentication.
     * <p>
     * This field must be unique across all users and is optional for updates.
     * Must be between 3 and 50 characters if provided.
     */
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    private String login;

    /**
     * User's first name.
     * <p>
     * This field stores the user's given name and is optional.
     * Used for personalization and display purposes.
     */
    private String firstName;

    /**
     * User's last name.
     * <p>
     * This field stores the user's family name and is optional.
     * Used for personalization and display purposes.
     */
    private String lastName;

    /**
     * Unique email address for the user.
     * <p>
     * This field must be unique across all users and is optional for updates.
     * Used for communication and account recovery purposes.
     * Must be a valid email format if provided.
     */
    @Email(message = "Invalid email format")
    private String email;
}
