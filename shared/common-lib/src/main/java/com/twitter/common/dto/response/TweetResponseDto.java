package com.twitter.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Tweet data.
 * <p>
 * This record contains all relevant tweet data including identifier, user reference,
 * content, timestamps for creation and updates, and soft delete information.
 * It is used to return tweet data from the API endpoints.
 *
 * @param id        unique identifier for the tweet
 * @param userId    ID of the user who created this tweet
 * @param content   content of the tweet
 * @param createdAt timestamp when the tweet was created
 * @param updatedAt timestamp when the tweet was last updated
 * @param isDeleted flag indicating whether the tweet has been soft deleted (nullable, typically false for active tweets)
 * @param deletedAt timestamp when the tweet was soft deleted (nullable, null for active tweets)
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
          "updatedAt": "2025-01-21T20:30:00Z",
          "isDeleted": false,
          "deletedAt": null
        }
        """
)
@Builder
public record TweetResponseDto(
    @Schema(
        description = "Unique identifier for the tweet",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "ID of the user who created this tweet (references users-api service)",
        example = "987fcdeb-51a2-43d7-b123-426614174111",
        format = "uuid"
    )
    UUID userId,

    @Schema(
        description = "Content of the tweet",
        example = "This is a sample tweet content",
        maxLength = 280
    )
    String content,

    @Schema(
        description = "Timestamp when the tweet was created",
        example = "2025-01-21T20:30:00Z",
        format = "date-time"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime createdAt,

    @Schema(
        description = "Timestamp when the tweet was last updated",
        example = "2025-01-21T20:30:00Z",
        format = "date-time"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime updatedAt,

    @Schema(
        description = "Flag indicating whether the tweet has been soft deleted. Typically false for active tweets returned by the API.",
        example = "false"
    )
    Boolean isDeleted,

    @Schema(
        description = "Timestamp when the tweet was soft deleted. Null for active tweets.",
        example = "2025-01-27T16:00:00Z",
        format = "date-time",
        nullable = true
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime deletedAt
) {
}
