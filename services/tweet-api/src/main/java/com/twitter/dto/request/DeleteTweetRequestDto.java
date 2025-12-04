package com.twitter.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/**
 * Data Transfer Object for deleting a tweet.
 *
 * @param userId the ID of the user performing the delete (used for authorization check)
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "DeleteTweetRequest",
    description = "Data structure for deleting tweets in the system",
    example = """
        {
          "userId": "123e4567-e89b-12d3-a456-426614174000"
        }
        """
)
@Builder
public record DeleteTweetRequestDto(
    @Schema(
        description = "The ID of the user performing the delete (used for authorization check)",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "User ID cannot be null")
    UUID userId
) {
}


