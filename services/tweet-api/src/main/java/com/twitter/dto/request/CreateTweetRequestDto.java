package com.twitter.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data Transfer Object for creating a new tweet.
 * Contains validation rules for tweet content and user identification.
 */
@Schema(
    name = "CreateTweetRequest",
    description = "Data structure for creating new tweets in the system",
    example = """
        {
          "content": "This is a sample tweet content",
          "userId": "123e4567-e89b-12d3-a456-426614174000"
        }
        """
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTweetRequestDto {

    /**
     * The content of the tweet.
     * Must be between 1 and 280 characters, cannot be blank.
     */
    @Schema(
        description = "The content of the tweet",
        example = "This is a sample tweet content",
        minLength = 1,
        maxLength = 280,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Tweet content cannot be empty")
    @Size(min = 1, max = 280, message = "Tweet content must be between 1 and 280 characters")
    private String content;

    /**
     * The ID of the user creating the tweet.
     * Must not be null and should reference an existing user.
     */
    @Schema(
        description = "The ID of the user creating the tweet",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "User ID cannot be null")
    private UUID userId;
}
