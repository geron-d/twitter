package com.twitter.repository;

import com.twitter.entity.Retweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RetweetRepository extends JpaRepository<Retweet, UUID> {

    Optional<Retweet> findByTweetIdAndUserId(UUID tweetId, UUID userId);

    boolean existsByTweetIdAndUserId(UUID tweetId, UUID userId);
}
