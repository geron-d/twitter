package com.twitter.repository;

import com.twitter.entity.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for Tweet entity.
 * Provides CRUD operations and custom query methods for tweets.
 */
@Repository
public interface TweetRepository extends JpaRepository<Tweet, UUID> {
}
