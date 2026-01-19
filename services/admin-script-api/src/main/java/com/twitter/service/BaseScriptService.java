package com.twitter.service;

import com.twitter.dto.request.BaseScriptRequestDto;
import com.twitter.dto.response.BaseScriptResponseDto;

/**
 * Service interface for executing the base administrative script.
 *
 * @author geron
 * @version 1.0
 */
public interface BaseScriptService {

    /**
     * Executes the administrative script to generate users and tweets.
     * <p>
     * This method executes a comprehensive administrative script that performs the following steps:
     * 1. Step 1: Create users - Generates the specified number of users with random data
     * 2. Step 1.5: Create follow relationships - Creates follow relationships between users:
     * - Selects the first created user as the central user
     * - The central user follows half of the remaining users
     * - Half of the remaining users follow the central user
     * - Uses integer division (rounding down) to calculate half count
     * - Requires at least 2 users to create follow relationships
     * 3. Step 2: Create tweets - Creates tweets for each successfully created user (caches TweetResponseDto for later use)
     * 4. Step 3: Calculate users with tweets - Determines which users have tweets
     * 5. Step 4: Validate deletion count - Validates business rules for tweet deletion
     * 6. Step 5: Delete tweets - Deletes one tweet from random users (if validation passes)
     * 7. Step 6: Create likes (half of users) - Creates likes for a random tweet:
     * - Selects a random tweet from created tweets (using Collections.shuffle())
     * - Gets the tweet author from cached TweetResponseDto
     * - Selects half of available users, excluding the tweet author
     * - Creates likes for each selected user
     * - Handles errors gracefully (self-like, duplicates): logs and adds to errors, continues execution
     * 8. Step 7: Create likes (third of users) - Creates likes for a different random tweet:
     * - Selects a different random tweet (not used in step 6)
     * - Gets the tweet author from cached TweetResponseDto
     * - Selects third of available users, excluding the tweet author
     * - Creates likes for each selected user
     * - Handles errors gracefully: logs and adds to errors, continues execution
     * 9. Step 8: Create likes (1 user) - Creates one like for a different random tweet:
     * - Selects a different random tweet (not used in steps 6-7)
     * - Gets the tweet author from cached TweetResponseDto
     * - Selects 1 available user, excluding the tweet author
     * - Creates one like for the selected user
     * - Handles errors gracefully: logs and adds to errors, continues execution
     * 10. Step 9: Create retweets (half of users) - Creates retweets for a different random tweet:
     * - Selects a different random tweet (not used in steps 6-8)
     * - Gets the tweet author from cached TweetResponseDto
     * - Selects half of available users, excluding the tweet author
     * - Creates retweets for each selected user (with comment = null)
     * - Handles errors gracefully (self-retweet, duplicates): logs and adds to errors, continues execution
     * 11. Step 10: Create retweets (third of users) - Creates retweets for a different random tweet:
     * - Selects a different random tweet (not used in steps 6-9)
     * - Gets the tweet author from cached TweetResponseDto
     * - Selects third of available users, excluding the tweet author
     * - Creates retweets for each selected user (with comment = null)
     * - Handles errors gracefully: logs and adds to errors, continues execution
     * 12. Step 11: Create retweets (1 user) - Creates one retweet for a different random tweet:
     * - Selects a different random tweet (not used in steps 6-10)
     * - Gets the tweet author from cached TweetResponseDto
     * - Selects 1 available user, excluding the tweet author
     * - Creates one retweet for the selected user (with comment = null)
     * - Handles errors gracefully: logs and adds to errors, continues execution
     * 13. Step 12: Build response - Collects statistics and builds the response DTO
     *
     * @param requestDto DTO containing script parameters
     * @return BaseScriptResponseDto
     */
    BaseScriptResponseDto executeScript(BaseScriptRequestDto requestDto);
}