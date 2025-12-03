package com.twitter.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing a Tweet in the database.
 * <p>
 * Maps to the 'tweets' table with all necessary fields and constraints.
 * This entity represents a tweet created by a user in the Twitter system.
 * Supports soft delete functionality through isDeleted flag and deletedAt timestamp.
 *
 * @author geron
 * @version 1.0
 */
@Entity
@Table(name = "tweets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tweet {

    /**
     * Unique identifier for the tweet.
     * Generated automatically using UUID.
     */
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID of the user who created this tweet.
     */
    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", columnDefinition = "UUID", nullable = false)
    private UUID userId;

    /**
     * Content of the tweet.
     * Must be between 1 and 280 characters (after trimming whitespace).
     */
    @NotBlank(message = "Tweet content cannot be blank")
    @Size(min = 1, max = 280, message = "Tweet content must be between 1 and 280 characters")
    @Column(name = "content", length = 280, nullable = false)
    private String content;

    /**
     * Timestamp when the tweet was created.
     * Automatically set by Hibernate on entity creation.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the tweet was last updated.
     * Automatically updated by Hibernate on entity modification.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Flag indicating whether the tweet has been soft deleted.
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * Timestamp when the tweet was soft deleted.
     */
    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;

    /**
     * Custom validation method to ensure content is not just whitespace.
     * This complements the database CHECK constraint.
     */
    @PrePersist
    @PreUpdate
    private void validateContent() {
        if (content != null && content.trim().isEmpty()) {
            throw new IllegalArgumentException("Tweet content cannot be empty or contain only whitespace");
        }
    }

    /**
     * Performs soft delete by setting isDeleted flag and deletedAt timestamp.
     * <p>
     * This method marks the tweet as deleted without removing it from the database.
     * It sets the isDeleted flag to true and records the current timestamp in deletedAt.
     * <p>
     * After soft delete, the tweet will not be returned by standard query methods
     * that filter out deleted tweets (e.g., findByIdAndIsDeletedFalse).
     */
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Checks if the tweet is active (not deleted).
     * <p>
     * This method returns true if the tweet has not been soft deleted, false otherwise.
     * A tweet is considered active when isDeleted is false or null.
     *
     * @return true if the tweet is active (not deleted), false otherwise
     */
    public boolean isActive() {
        return !Boolean.TRUE.equals(isDeleted);
    }
}
