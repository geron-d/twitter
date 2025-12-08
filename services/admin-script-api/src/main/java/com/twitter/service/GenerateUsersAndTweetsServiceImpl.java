package com.twitter.service;

import com.twitter.dto.external.*;
import com.twitter.dto.request.GenerateUsersAndTweetsRequestDto;
import com.twitter.dto.response.GenerateUsersAndTweetsResponseDto;
import com.twitter.dto.response.ScriptStatisticsDto;
import com.twitter.gateway.TweetsGateway;
import com.twitter.gateway.UsersGateway;
import com.twitter.util.RandomDataGenerator;
import com.twitter.validation.GenerateUsersAndTweetsValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the service for executing the administrative script to generate users and tweets.
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateUsersAndTweetsServiceImpl implements GenerateUsersAndTweetsService {

    private final UsersGateway usersGateway;
    private final TweetsGateway tweetsGateway;
    private final RandomDataGenerator randomDataGenerator;
    private final GenerateUsersAndTweetsValidator validator;

    /**
     * @see GenerateUsersAndTweetsService#executeScript
     */
    @Override
    public GenerateUsersAndTweetsResponseDto executeScript(GenerateUsersAndTweetsRequestDto requestDto) {
        long startTime = System.currentTimeMillis();
        log.info("Starting script execution: nUsers={}, nTweetsPerUser={}, lUsersForDeletion={}",
            requestDto.nUsers(), requestDto.nTweetsPerUser(), requestDto.lUsersForDeletion());

        List<UUID> createdUsers = new ArrayList<>();
        List<UUID> createdTweets = new ArrayList<>();
        List<UUID> deletedTweets = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Step 1: Create users
        log.info("Step 1: Creating {} users", requestDto.nUsers());
        for (int i = 0; i < requestDto.nUsers(); i++) {
            try {
                UserRequestDto userRequest = UserRequestDto.builder()
                    .login(randomDataGenerator.generateLogin())
                    .email(randomDataGenerator.generateEmail())
                    .firstName(randomDataGenerator.generateFirstName())
                    .lastName(randomDataGenerator.generateLastName())
                    .password(randomDataGenerator.generatePassword())
                    .build();

                UserResponseDto userResponse = usersGateway.createUser(userRequest);
                createdUsers.add(userResponse.id());
                log.debug("Created user {}/{}: {}", i + 1, requestDto.nUsers(), userResponse.id());
            } catch (Exception ex) {
                String errorMsg = String.format("Failed to create user %d/%d: %s", i + 1, requestDto.nUsers(), ex.getMessage());
                log.error(errorMsg, ex);
                errors.add(errorMsg);
            }
        }
        log.info("Step 1 completed: {} users created successfully out of {} requested", createdUsers.size(), requestDto.nUsers());

        // Step 2: Create tweets for each user
        log.info("Step 2: Creating {} tweets for each of {} users", requestDto.nTweetsPerUser(), createdUsers.size());
        for (UUID userId : createdUsers) {
            for (int i = 0; i < requestDto.nTweetsPerUser(); i++) {
                try {
                    CreateTweetRequestDto tweetRequest = CreateTweetRequestDto.builder()
                        .content(randomDataGenerator.generateTweetContent())
                        .userId(userId)
                        .build();

                    TweetResponseDto tweetResponse = tweetsGateway.createTweet(tweetRequest);
                    createdTweets.add(tweetResponse.id());
                    log.debug("Created tweet {}/{} for user {}", i + 1, requestDto.nTweetsPerUser(), userId);
                } catch (Exception ex) {
                    String errorMsg = String.format("Failed to create tweet %d/%d for user %s: %s",
                        i + 1, requestDto.nTweetsPerUser(), userId, ex.getMessage());
                    log.error(errorMsg, ex);
                    errors.add(errorMsg);
                }
            }
        }
        log.info("Step 2 completed: {} tweets created successfully", createdTweets.size());

        // Step 3: Calculate users with tweets
        log.info("Step 3: Calculating users with tweets");
        List<UUID> usersWithTweets = new ArrayList<>();
        for (UUID userId : createdUsers) {
            try {
                Pageable pageable = PageRequest.of(0, 1000); // Large page size to get all tweets
                Page<TweetResponseDto> userTweets = tweetsGateway.getUserTweets(userId, pageable);
                if (userTweets.getTotalElements() > 0) {
                    usersWithTweets.add(userId);
                }
            } catch (Exception ex) {
                String errorMsg = String.format("Failed to get tweets for user %s: %s", userId, ex.getMessage());
                log.error(errorMsg, ex);
                errors.add(errorMsg);
            }
        }
        int usersWithTweetsCount = usersWithTweets.size();
        int usersWithoutTweetsCount = createdUsers.size() - usersWithTweetsCount;
        log.info("Step 3 completed: {} users with tweets, {} users without tweets",
            usersWithTweetsCount, usersWithoutTweetsCount);

        // Step 4: Validate deletion count
        log.info("Step 4: Validating deletion count");
        try {
            validator.validateDeletionCount(requestDto, usersWithTweetsCount);
            log.info("Step 4 completed: Validation passed");
        } catch (Exception ex) {
            String errorMsg = String.format("Validation failed: %s", ex.getMessage());
            log.error(errorMsg, ex);
            errors.add(errorMsg);
            // Continue execution even if validation fails (will skip deletion step)
        }

        // Step 5: Delete tweets from random users
        if (requestDto.lUsersForDeletion() > 0 && usersWithTweetsCount > 0) {
            log.info("Step 5: Deleting one tweet from {} random users", requestDto.lUsersForDeletion());
            List<UUID> usersToDeleteFrom = new ArrayList<>(usersWithTweets);
            Collections.shuffle(usersToDeleteFrom);
            int usersToProcess = Math.min(requestDto.lUsersForDeletion(), usersToDeleteFrom.size());

            for (int i = 0; i < usersToProcess; i++) {
                UUID userId = usersToDeleteFrom.get(i);
                try {
                    Pageable pageable = PageRequest.of(0, 1000);
                    Page<TweetResponseDto> userTweets = tweetsGateway.getUserTweets(userId, pageable);
                    List<TweetResponseDto> tweetsList = userTweets.getContent();

                    if (!tweetsList.isEmpty()) {
                        // Select random tweet
                        Collections.shuffle(tweetsList);
                        TweetResponseDto tweetToDelete = tweetsList.get(0);

                        DeleteTweetRequestDto deleteRequest = DeleteTweetRequestDto.builder()
                            .userId(userId)
                            .build();

                        tweetsGateway.deleteTweet(tweetToDelete.id(), deleteRequest);
                        deletedTweets.add(tweetToDelete.id());
                        log.debug("Deleted tweet {} for user {}", tweetToDelete.id(), userId);
                    } else {
                        String errorMsg = String.format("User %s has no tweets to delete", userId);
                        log.warn(errorMsg);
                        errors.add(errorMsg);
                    }
                } catch (Exception ex) {
                    String errorMsg = String.format("Failed to delete tweet for user %s: %s", userId, ex.getMessage());
                    log.error(errorMsg, ex);
                    errors.add(errorMsg);
                }
            }
            log.info("Step 5 completed: {} tweets deleted successfully", deletedTweets.size());
        } else {
            log.info("Step 5 skipped: No deletions requested or no users with tweets");
        }

        // Step 6: Calculate execution time and build response
        long endTime = System.currentTimeMillis();
        long executionTimeMs = endTime - startTime;

        ScriptStatisticsDto statistics = new ScriptStatisticsDto(createdUsers.size(), createdTweets.size(),
            deletedTweets.size(), usersWithTweetsCount, usersWithoutTweetsCount, executionTimeMs, errors);

        GenerateUsersAndTweetsResponseDto response = GenerateUsersAndTweetsResponseDto.builder()
            .createdUsers(createdUsers)
            .createdTweets(createdTweets)
            .deletedTweets(deletedTweets)
            .statistics(statistics)
            .build();

        log.info("Script execution completed in {} ms. Created: {} users, {} tweets. Deleted: {} tweets. Errors: {}",
            executionTimeMs, createdUsers.size(), createdTweets.size(), deletedTweets.size(), errors.size());

        return response;
    }
}

