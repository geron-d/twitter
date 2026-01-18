package com.twitter.common.dto.request.like;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/**
 * Data Transfer Object for liking a tweet.
 *
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "LikeTweetRequest",
    description = "Data structure for liking a tweet in the system",
    example = """
        {
          "userId": "123e4567-e89b-12d3-a456-426614174000"
        }
        """
)
@Builder
public record LikeTweetRequestDto(
    @Schema(
        description = "The ID of the user who likes the tweet",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "User ID cannot be null")
    UUID userId
) {
}
