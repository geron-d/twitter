package com.twitter.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Retweet data.
 *
 * @param id        unique identifier for the retweet
 * @param tweetId   ID of the tweet that was retweeted
 * @param userId    ID of the user who retweeted the tweet
 * @param comment   optional comment for the retweet (can be null)
 * @param createdAt timestamp when the retweet was created
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "RetweetResponse",
    description = "Retweet information returned by the API",
    example = """
        {
          "id": "987e6543-e21b-43d2-b654-321987654321",
          "tweetId": "223e4567-e89b-12d3-a456-426614174001",
          "userId": "123e4567-e89b-12d3-a456-426614174000",
          "comment": "Great tweet!",
          "createdAt": "2025-01-27T15:30:00Z"
        }
        """
)
@Builder
public record RetweetResponseDto(
    @Schema(
        description = "Unique identifier for the retweet",
        example = "987e6543-e21b-43d2-b654-321987654321",
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "ID of the tweet that was retweeted",
        example = "223e4567-e89b-12d3-a456-426614174001",
        format = "uuid"
    )
    UUID tweetId,

    @Schema(
        description = "ID of the user who retweeted the tweet",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid"
    )
    UUID userId,

    @Schema(
        description = "Optional comment for the retweet (can be null)",
        example = "Great tweet!",
        maxLength = 280,
        nullable = true
    )
    String comment,

    @Schema(
        description = "Timestamp when the retweet was created",
        example = "2025-01-27T15:30:00Z",
        format = "date-time"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime createdAt
) {
}
