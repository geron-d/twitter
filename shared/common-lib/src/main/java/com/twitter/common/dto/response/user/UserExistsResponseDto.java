package com.twitter.common.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for user existence check response.
 *
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "UserExistsResponse",
    description = "Response indicating whether a user exists in the system",
    example = "{\"exists\": true}"
)
public record UserExistsResponseDto(
    @Schema(
        description = "Indicates whether the user exists in the system",
        example = "true"
    )
    boolean exists
) {
}

