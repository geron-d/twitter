package com.twitter.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing a Like in the database.
 * <p>
 * Enforces uniqueness constraint on the pair (tweetId, userId) to prevent duplicate likes.
 *
 * @author geron
 * @version 1.0
 */
@Entity
@Table(
    name = "tweet_likes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tweet_likes_tweet_user", columnNames = {"tweet_id", "user_id"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Like {

    /**
     * Unique identifier for the like.
     * Generated automatically using UUID.
     */
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID of the tweet that was liked.
     */
    @NotNull(message = "Tweet ID cannot be null")
    @Column(name = "tweet_id", columnDefinition = "UUID", nullable = false)
    private UUID tweetId;

    /**
     * ID of the user who created this like.
     */
    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", columnDefinition = "UUID", nullable = false)
    private UUID userId;

    /**
     * Timestamp when the like was created.
     * Automatically set by Hibernate on entity creation.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Checks if the like is by a specific user.
     * <p>
     * This method returns true if the like was created by the specified user.
     *
     * @param userId the user ID to check
     * @return true if the like is by the specified user, false otherwise
     */
    public boolean isByUser(UUID userId) {
        return this.userId != null && this.userId.equals(userId);
    }

    /**
     * Checks if the like is for a specific tweet.
     * <p>
     * This method returns true if the like is for the specified tweet.
     *
     * @param tweetId the tweet ID to check
     * @return true if the like is for the specified tweet, false otherwise
     */
    public boolean isForTweet(UUID tweetId) {
        return this.tweetId != null && this.tweetId.equals(tweetId);
    }
}
