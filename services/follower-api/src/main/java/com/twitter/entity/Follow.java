package com.twitter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing a Follow relationship in the database.
 * <p>
 * Maps to the 'follows' table with all necessary fields and constraints.
 * This entity represents a follow relationship between two users in the Twitter system,
 * where one user (follower) follows another user (following).
 * <p>
 * The entity enforces business rules through database constraints:
 * - Unique constraint on (follower_id, following_id) prevents duplicate follows
 * - Check constraint ensures follower_id != following_id (users cannot follow themselves)
 *
 * @author geron
 * @version 1.0
 */
@Entity
@Table(
    name = "follows",
    uniqueConstraints = @UniqueConstraint(
        name = "follows_unique_follower_following",
        columnNames = {"follower_id", "following_id"}
    )
)
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Follow {

    /**
     * Unique identifier for the follow relationship.
     * <p>
     * This field serves as the primary key and is automatically generated
     * using UUID. It cannot be updated after creation and is required.
     */
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID of the user who is following (the follower).
     * <p>
     * This field references the 'users' table and represents the user
     * who initiated the follow relationship. It is required and cannot be null.
     */
    @Column(name = "follower_id", columnDefinition = "UUID", nullable = false)
    private UUID followerId;

    /**
     * ID of the user who is being followed (the following).
     * <p>
     * This field references the 'users' table and represents the user
     * who is being followed. It is required and cannot be null.
     * The database constraint ensures that follower_id != following_id.
     */
    @Column(name = "following_id", columnDefinition = "UUID", nullable = false)
    private UUID followingId;

    /**
     * Timestamp when the follow relationship was created.
     * <p>
     * This field is automatically set by Hibernate when the entity is persisted.
     * It cannot be updated after creation and is required.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}