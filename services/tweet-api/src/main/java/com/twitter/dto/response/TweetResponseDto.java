package com.twitter.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Tweet data.
 * Used to return tweet information in API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TweetResponseDto {

    /**
     * Unique identifier for the tweet.
     * Generated automatically when tweet is created.
     */
    private UUID id;

    /**
     * ID of the user who created this tweet.
     * References the user from users-api service.
     */
    private UUID userId;

    /**
     * Content of the tweet.
     * Maximum 280 characters as per Twitter standards.
     */
    private String content;

    /**
     * Timestamp when the tweet was created.
     * Formatted as ISO 8601 string in UTC timezone.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime createdAt;

    /**
     * Timestamp when the tweet was last updated.
     * Formatted as ISO 8601 string in UTC timezone.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;
}
