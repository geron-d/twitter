package com.twitter.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Like data.
 *
 * @param id        unique identifier for the like
 * @param tweetId   ID of the tweet that was liked
 * @param userId    ID of the user who liked the tweet
 * @param createdAt timestamp when the like was created
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "LikeResponse",
    description = "Like information returned by the API",
    example = """
        {
          "id": "987e6543-e21b-43d2-b654-321987654321",
          "tweetId": "223e4567-e89b-12d3-a456-426614174001",
          "userId": "123e4567-e89b-12d3-a456-426614174000",
          "createdAt": "2025-01-27T15:30:00Z"
        }
        """
)
@Builder
public record LikeResponseDto(
    @Schema(
        description = "Unique identifier for the like",
        example = "987e6543-e21b-43d2-b654-321987654321",
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "ID of the tweet that was liked",
        example = "223e4567-e89b-12d3-a456-426614174001",
        format = "uuid"
    )
    UUID tweetId,

    @Schema(
        description = "ID of the user who liked the tweet",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid"
    )
    UUID userId,

    @Schema(
        description = "Timestamp when the like was created",
        example = "2025-01-27T15:30:00Z",
        format = "date-time"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime createdAt
) {
}
