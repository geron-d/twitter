package com.twitter.dto.request;

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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTweetRequestDto {

    /**
     * The content of the tweet.
     * Must be between 1 and 280 characters, cannot be blank.
     */
    @NotBlank(message = "Tweet content cannot be empty")
    @Size(min = 1, max = 280, message = "Tweet content must be between 1 and 280 characters")
    private String content;

    /**
     * The ID of the user creating the tweet.
     * Must not be null and should reference an existing user.
     */
    @NotNull(message = "User ID cannot be null")
    private UUID userId;
}
