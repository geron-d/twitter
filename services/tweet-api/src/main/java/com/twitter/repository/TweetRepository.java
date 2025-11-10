package com.twitter.repository;

import com.twitter.entity.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for tweet data access operations.
 *
 * @author geron
 * @version 1.0
 */
@Repository
public interface TweetRepository extends JpaRepository<Tweet, UUID> {
}
