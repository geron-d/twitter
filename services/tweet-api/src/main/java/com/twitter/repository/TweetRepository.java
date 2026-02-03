package com.twitter.repository;

import com.twitter.entity.Tweet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, UUID> {

    Optional<Tweet> findByIdAndIsDeletedFalse(UUID id);

    Page<Tweet> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Tweet> findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc(List<UUID> userIds, Pageable pageable);
}