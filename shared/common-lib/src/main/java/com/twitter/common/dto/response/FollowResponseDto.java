package com.twitter.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for follow relationship data.
 * <p>
 * This record contains all relevant follow relationship data including identifier,
 * follower and following user references, and creation timestamp. It is used to
 * return follow relationship data from the API endpoints.
 *
 * @param id          unique identifier for the follow relationship
 * @param followerId  ID of the user who is following (the follower)
 * @param followingId ID of the user being followed (the following)
 * @param createdAt   timestamp when the follow relationship was created
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "FollowResponse",
    description = "Follow relationship information returned by the API",
    example = """
        {
          "id": "456e7890-e89b-12d3-a456-426614174111",
          "followerId": "123e4567-e89b-12d3-a456-426614174000",
          "followingId": "987fcdeb-51a2-43d7-b123-426614174999",
          "createdAt": "2025-01-27T10:30:00Z"
        }
        """
)
@Builder
public record FollowResponseDto(
    @Schema(
        description = "Unique identifier for the follow relationship",
        example = "456e7890-e89b-12d3-a456-426614174111",
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "ID of the user who is following (the follower)",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid"
    )
    UUID followerId,

    @Schema(
        description = "ID of the user being followed (the following)",
        example = "987fcdeb-51a2-43d7-b123-426614174999",
        format = "uuid"
    )
    UUID followingId,

    @Schema(
        description = "Timestamp when the follow relationship was created",
        example = "2025-01-27T10:30:00Z",
        format = "date-time"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime createdAt
) {
}
