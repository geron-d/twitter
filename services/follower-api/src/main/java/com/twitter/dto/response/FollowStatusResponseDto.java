package com.twitter.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Response DTO for follow relationship status.
 * <p>
 * This record contains the status of a follow relationship including whether
 * the relationship exists and when it was created (if it exists).
 *
 * @param isFollowing flag indicating whether the follow relationship exists
 * @param createdAt   timestamp when the follow relationship was created (null if relationship does not exist)
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "FollowStatusResponse",
    description = "Follow relationship status information returned by the API",
    example = """
        {
          "isFollowing": true,
          "createdAt": "2025-01-20T15:30:00Z"
        }
        """
)
@Builder
public record FollowStatusResponseDto(
    @Schema(
        description = "Flag indicating whether the follow relationship exists",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    boolean isFollowing,

    @Schema(
        description = "Timestamp when the follow relationship was created (null if relationship does not exist)",
        example = "2025-01-20T15:30:00Z",
        format = "date-time",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime createdAt
) {
}

