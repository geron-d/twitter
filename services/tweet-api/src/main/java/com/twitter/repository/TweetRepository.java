package com.twitter.repository;

import com.twitter.entity.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Modifying
    @Query("UPDATE Tweet t SET t.isDeleted = true, t.deletedAt = :deletedAt WHERE t.id = :id")
    void softDeleteById(@Param("id") UUID id, @Param("deletedAt") LocalDateTime deletedAt);
}
