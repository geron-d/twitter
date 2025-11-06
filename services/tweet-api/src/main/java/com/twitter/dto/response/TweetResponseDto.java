package com.twitter.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Tweet data.
 * It contains all relevant tweet data including identifier, user reference,
 * content, and timestamps for creation and updates.
 *
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "TweetResponse",
    description = "Tweet information returned by the API",
    example = """
        {
          "id": "123e4567-e89b-12d3-a456-426614174000",
          "userId": "987fcdeb-51a2-43d7-b123-426614174111",
          "content": "This is a sample tweet content",
          "createdAt": "2025-01-21T20:30:00Z",
          "updatedAt": "2025-01-21T20:30:00Z"
        }
        """
)
@Builder
public record TweetResponseDto(
    /**
     * Unique identifier for the tweet.
     * Generated automatically when tweet is created.
     */
    @Schema(
        description = "Unique identifier for the tweet",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid"
    )
    UUID id,

    /**
     * ID of the user who created this tweet.
     * References the user from users-api service.
     */
    @Schema(
        description = "ID of the user who created this tweet (references users-api service)",
        example = "987fcdeb-51a2-43d7-b123-426614174111",
        format = "uuid"
    )
    UUID userId,

    /**
     * Content of the tweet.
     * Maximum 280 characters as per Twitter standards.
     */
    @Schema(
        description = "Content of the tweet",
        example = "This is a sample tweet content",
        maxLength = 280
    )
    String content,

    /**
     * Timestamp when the tweet was created.
     * Formatted as ISO 8601 string in UTC timezone.
     */
    @Schema(
        description = "Timestamp when the tweet was created",
        example = "2025-01-21T20:30:00Z",
        format = "date-time"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime createdAt,

    /**
     * Timestamp when the tweet was last updated.
     * Formatted as ISO 8601 string in UTC timezone.
     */
    @Schema(
        description = "Timestamp when the tweet was last updated",
        example = "2025-01-21T20:30:00Z",
        format = "date-time"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime updatedAt
) {
}
