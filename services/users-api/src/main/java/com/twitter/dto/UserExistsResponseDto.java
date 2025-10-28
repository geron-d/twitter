package com.twitter.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for user existence check response.
 * <p>
 * This record represents the response from checking whether a user exists
 * in the system. It provides a boolean value indicating the user's existence
 * without exposing any sensitive user data.
 */
@Schema(
    name = "UserExistsResponse",
    description = "Response indicating whether a user exists in the system",
    example = "{\"exists\": true}"
)
public record UserExistsResponseDto(
    /**
     * Indicates whether the user exists in the system.
     * <p>
     * This field is set to true if a user with the specified ID exists
     * and is found in the database, false otherwise.
     */
    @Schema(
        description = "Indicates whether the user exists in the system",
        example = "true",
        required = true
    )
    boolean exists
) {
}


