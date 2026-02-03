package com.twitter.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for follower information in follower lists.
 *
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "FollowerResponse",
    description = "Follower information returned in follower lists",
    example = """
        {
          "id": "123e4567-e89b-12d3-a456-426614174000",
          "login": "john_doe",
          "createdAt": "2025-01-20T15:30:00Z"
        }
        """
)
@Builder
public record FollowerResponseDto(
    @Schema(
        description = "Unique identifier of the follower user",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "Login name of the follower user",
        example = "john_doe",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String login,

    @Schema(
        description = "Timestamp when the follow relationship was created",
        example = "2025-01-20T15:30:00Z",
        format = "date-time"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime createdAt
) {
}
