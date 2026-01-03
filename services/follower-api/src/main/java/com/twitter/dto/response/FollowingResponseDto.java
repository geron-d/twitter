package com.twitter.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for following information in following lists.
 * <p>
 * This record contains following information including identifier, login,
 * and creation timestamp.
 *
 * @param id        unique identifier of the following user
 * @param login     login name of the following user
 * @param createdAt timestamp when the follow relationship was created
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "FollowingResponse",
    description = "Following information returned in following lists",
    example = """
        {
          "id": "123e4567-e89b-12d3-a456-426614174000",
          "login": "jane_doe",
          "createdAt": "2025-01-20T15:30:00Z"
        }
        """
)
@Builder
public record FollowingResponseDto(
    @Schema(
        description = "Unique identifier of the following user",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "Login name of the following user",
        example = "jane_doe",
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
