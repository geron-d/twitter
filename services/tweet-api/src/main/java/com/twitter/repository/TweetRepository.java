package com.twitter.repository;

import com.twitter.entity.Tweet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Tweet entity.
 * Provides CRUD operations and custom query methods for tweets.
 */
@Repository
public interface TweetRepository extends JpaRepository<Tweet, UUID> {

    /**
     * Find all tweets by a specific user, ordered by creation date (newest first).
     *
     * @param userId the ID of the user
     * @return list of tweets by the user
     */
    List<Tweet> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find all tweets by a specific user with pagination, ordered by creation date (newest first).
     *
     * @param userId   the ID of the user
     * @param pageable pagination information
     * @return page of tweets by the user
     */
    Page<Tweet> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find all tweets ordered by creation date (newest first).
     * Used for global timeline/feed.
     *
     * @param pageable pagination information
     * @return page of tweets ordered by creation date
     */
    Page<Tweet> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find tweets created after a specific timestamp.
     * Useful for real-time updates and notifications.
     *
     * @param timestamp the timestamp to filter by
     * @return list of tweets created after the timestamp
     */
    List<Tweet> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime timestamp);

    /**
     * Count tweets by a specific user.
     *
     * @param userId the ID of the user
     * @return number of tweets by the user
     */
    long countByUserId(UUID userId);

    /**
     * Find tweets containing specific text in content (case-insensitive).
     * Uses LIKE query for partial matching.
     *
     * @param content  the text to search for
     * @param pageable pagination information
     * @return page of tweets containing the text
     */
    @Query("SELECT t FROM Tweet t WHERE LOWER(t.content) LIKE LOWER(CONCAT('%', :content, '%')) ORDER BY t.createdAt DESC")
    Page<Tweet> findByContentContainingIgnoreCase(@Param("content") String content, Pageable pageable);

    /**
     * Find recent tweets by multiple users (for following/followers timeline).
     *
     * @param userIds  list of user IDs
     * @param pageable pagination information
     * @return page of tweets by the specified users
     */
    @Query("SELECT t FROM Tweet t WHERE t.userId IN :userIds ORDER BY t.createdAt DESC")
    Page<Tweet> findByUserIdInOrderByCreatedAtDesc(@Param("userIds") List<UUID> userIds, Pageable pageable);

    /**
     * Find tweets created between two timestamps.
     * Useful for analytics and reporting.
     *
     * @param startTime start timestamp
     * @param endTime   end timestamp
     * @param pageable  pagination information
     * @return page of tweets created in the time range
     */
    @Query("SELECT t FROM Tweet t WHERE t.createdAt BETWEEN :startTime AND :endTime ORDER BY t.createdAt DESC")
    Page<Tweet> findByCreatedAtBetweenOrderByCreatedAtDesc(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable);

    /**
     * Delete all tweets by a specific user.
     * Used when a user account is deleted.
     *
     * @param userId the ID of the user
     * @return number of deleted tweets
     */
    long deleteByUserId(UUID userId);
}
