package com.twitter.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing a Retweet in the database.
 * <p>
 * Enforces uniqueness constraint on the pair (tweetId, userId) to prevent duplicate retweets.
 * Supports optional comment field (nullable, max 280 characters).
 *
 * @author geron
 * @version 1.0
 */
@Entity
@Table(
    name = "tweet_retweets",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tweet_retweets_tweet_user", columnNames = {"tweet_id", "user_id"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Retweet {

    /**
     * Unique identifier for the retweet.
     * Generated automatically using UUID.
     */
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID of the tweet that was retweeted.
     */
    @NotNull(message = "Tweet ID cannot be null")
    @Column(name = "tweet_id", columnDefinition = "UUID", nullable = false)
    private UUID tweetId;

    /**
     * ID of the user who created this retweet.
     */
    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", columnDefinition = "UUID", nullable = false)
    private UUID userId;

    /**
     * Optional comment for the retweet.
     * Can be null, but if provided, must not exceed 280 characters and cannot be an empty string.
     */
    @Size(max = 280, message = "Comment must not exceed 280 characters")
    @Column(name = "comment", length = 280)
    private String comment;

    /**
     * Timestamp when the retweet was created.
     * Automatically set by Hibernate on entity creation.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Checks if the retweet has a comment.
     *
     * @return true if the retweet has a non-empty comment, false otherwise
     */
    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }
}