package com.twitter.repository;

import com.twitter.entity.Tweet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for tweet data access operations.
 *
 * @author geron
 * @version 1.0
 */
@Repository
public interface TweetRepository extends JpaRepository<Tweet, UUID> {

    Optional<Tweet> findByIdAndIsDeletedFalse(UUID id);

    Page<Tweet> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Tweet> findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc(List<UUID> userIds, Pageable pageable);

    /**
     * Performs soft delete on a tweet by setting isDeleted flag and deletedAt timestamp.
     *
     * @param id        the unique identifier of the tweet to soft delete
     * @param deletedAt the timestamp when the tweet was deleted
     */
    @Modifying
    @Query("UPDATE Tweet t SET t.isDeleted = true, t.deletedAt = :deletedAt WHERE t.id = :id")
    void softDeleteById(@Param("id") UUID id, @Param("deletedAt") LocalDateTime deletedAt);
}
