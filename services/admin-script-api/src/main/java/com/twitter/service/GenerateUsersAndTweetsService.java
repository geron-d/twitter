package com.twitter.service;

import com.twitter.dto.request.GenerateUsersAndTweetsRequestDto;
import com.twitter.dto.response.GenerateUsersAndTweetsResponseDto;

/**
 * Service interface for executing the administrative script to generate users and tweets.
 *
 * @author geron
 * @version 1.0
 */
public interface GenerateUsersAndTweetsService {

    /**
     * Executes the administrative script to generate users and tweets.
     * <p>
     * This method performs the following operations:
     * <ul>
     *   <li>Creates nUsers users with random data</li>
     *   <li>Creates nTweetsPerUser tweets for each successfully created user</li>
     *   <li>Validates that lUsersForDeletion does not exceed users with tweets</li>
     *   <li>Deletes one tweet from lUsersForDeletion random users</li>
     * </ul>
     * <p>
     * Partial errors are handled gracefully - errors are logged and added to the
     * response statistics, but execution continues. The method always returns a
     * response with statistics, even if some operations failed.
     *
     * @param requestDto DTO containing script parameters (nUsers, nTweetsPerUser, lUsersForDeletion)
     * @return GenerateUsersAndTweetsResponseDto containing lists of created/deleted IDs and execution statistics
     */
    GenerateUsersAndTweetsResponseDto executeScript(GenerateUsersAndTweetsRequestDto requestDto);
}
