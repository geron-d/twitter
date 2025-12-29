package com.twitter.gateway;

import com.twitter.client.FollowerApiClient;
import com.twitter.common.dto.response.FollowingResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Gateway for integration with Follower API service.
 *
 * @author geron
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FollowerGateway {

    private final FollowerApiClient followerApiClient;

    /**
     * Retrieves a list of user identifiers that the specified user is following.
     * <p>
     * This method retrieves all following relationships for the given user by
     * making paginated requests to the follower-api until all following users
     * are retrieved. If the follower-api is unavailable or returns an error,
     * an empty list is returned (graceful degradation).
     * <p>
     * The method uses pagination with a page size of 100 to minimize the number
     * of requests needed to retrieve all following relationships.
     *
     * @param userId the unique identifier of the user whose following list to retrieve
     * @return list of user identifiers (followingIds) that the user is following.
     *         Returns an empty list if the user has no following relationships,
     *         if the user ID is null, or if an error occurs while communicating with follower-api
     */
    public List<UUID> getFollowingUserIds(UUID userId) {
        if (userId == null) {
            log.warn("Attempted to retrieve following list for null user ID");
            return new ArrayList<>();
        }

        try {
            log.debug("Retrieving following list for user: userId={}", userId);
            List<UUID> allFollowingIds = new ArrayList<>();
            int page = 0;
            int size = 100; // Maximum page size to minimize requests
            int pageFollowingIdsSize;

            do {
                Pageable pageable = PageRequest.of(page, size);
                PagedModel<FollowingResponseDto> pageResult = followerApiClient.getFollowing(userId, pageable);

                List<UUID> pageFollowingIds = pageResult.getContent().stream()
                    .map(FollowingResponseDto::id)
                    .toList();
                pageFollowingIdsSize = pageFollowingIds.size();
                allFollowingIds.addAll(pageFollowingIds);

                log.debug("Retrieved {} following users from page {} for user: userId={}, total so far: {}",
                    pageFollowingIdsSize, page, userId, allFollowingIds.size());

                page++;
            } while (pageFollowingIdsSize >= size);

            log.info("Retrieved {} total following users for user: userId={}", allFollowingIds.size(), userId);
            return allFollowingIds;
        } catch (Exception ex) {
            log.warn("Failed to retrieve following list for user: userId={}, error: {}",
                userId, ex.getMessage(), ex);
            return new ArrayList<>();
        }
    }
}

