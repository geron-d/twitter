package com.twitter.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

/**
 * Data Transfer Object for retweeting a tweet.
 * <p>
 * The comment field is optional and can be null, but if provided,
 * it must be between 1 and 280 characters and cannot be an empty string.
 *
 * @param userId  the ID of the user who retweets the tweet
 * @param comment optional comment for the retweet (1-280 characters, can be null)
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "RetweetRequest",
    description = "Data structure for retweeting a tweet in the system. " +
        "The comment field is optional and can be null, but if provided, " +
        "it must be between 1 and 280 characters and cannot be an empty string.",
    example = """
        {
          "userId": "123e4567-e89b-12d3-a456-426614174000",
          "comment": "Great tweet!"
        }
        """
)
@Builder
public record RetweetRequestDto(
    @Schema(
        description = "The ID of the user who retweets the tweet",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "User ID cannot be null")
    UUID userId,

    @Schema(
        description = "Optional comment for the retweet (1-280 characters). " +
            "Can be null, but if provided, must not be empty string.",
        example = "Great tweet!",
        maxLength = 280,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 280, message = "Comment must not exceed 280 characters")
    String comment
) {
}
