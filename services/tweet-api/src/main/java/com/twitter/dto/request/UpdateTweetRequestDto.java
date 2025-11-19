package com.twitter.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

/**
 * Data Transfer Object for updating an existing tweet.
 *
 * @param content the updated content of the tweet (1-280 characters)
 * @param userId  the ID of the user performing the update (used for authorization check)
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "UpdateTweetRequest",
    description = "Data structure for updating existing tweets in the system",
    example = """
        {
          "content": "This is updated tweet content",
          "userId": "123e4567-e89b-12d3-a456-426614174000"
        }
        """
)
@Builder
public record UpdateTweetRequestDto(
    @Schema(
        description = "The updated content of the tweet",
        example = "This is updated tweet content",
        minLength = 1,
        maxLength = 280,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Tweet content cannot be empty")
    @Size(min = 1, max = 280, message = "Tweet content must be between 1 and 280 characters")
    String content,

    @Schema(
        description = "The ID of the user performing the update (used for authorization check)",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "User ID cannot be null")
    UUID userId
) {
}

