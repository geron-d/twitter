package com.twitter.gateway;

import com.twitter.client.FollowApiClient;
import com.twitter.common.dto.request.follow.FollowRequestDto;
import com.twitter.common.dto.response.follow.FollowResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Gateway for integration with Follower API service.
 *
 * @author geron
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FollowGateway {

    private final FollowApiClient followApiClient;

    /**
     * Creates a new follow relationship in the follower-api service.
     *
     * @param request DTO containing followerId and followingId for the relationship
     * @return FollowResponseDto containing the created follow relationship information
     * @throws IllegalArgumentException if the request is null or contains null fields
     * @throws RuntimeException         if the follow relationship creation fails (e.g., service unavailable, validation error)
     */
    public FollowResponseDto createFollow(FollowRequestDto request) {
        if (request == null) {
            log.error("Attempted to create follow relationship with null request");
            throw new IllegalArgumentException("Follow request cannot be null");
        }

        if (request.followerId() == null || request.followingId() == null) {
            log.error("Attempted to create follow relationship with null user IDs");
            throw new IllegalArgumentException("Follower ID and Following ID cannot be null");
        }

        try {
            FollowResponseDto response = followApiClient.createFollow(request);
            log.info("Successfully created follow relationship: {} -> {} (ID: {})",
                request.followerId(), request.followingId(), response.id());
            return response;
        } catch (Exception ex) {
            log.error("Failed to create follow relationship {} -> {}. Error: {}",
                request.followerId(), request.followingId(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to create follow relationship: " + ex.getMessage(), ex);
        }
    }
}