package com.twitter.repository;

import com.twitter.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {

    Optional<Like> findByTweetIdAndUserId(UUID tweetId, UUID userId);

    boolean existsByTweetIdAndUserId(UUID tweetId, UUID userId);
}
