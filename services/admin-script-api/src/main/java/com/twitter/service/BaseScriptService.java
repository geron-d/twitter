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
     * This method performs the following operations:
     * - Creates nUsers users with random data
     * - Creates nTweetsPerUser tweets for each successfully created user
     * - Validates that lUsersForDeletion does not exceed users with tweets
     * - Deletes one tweet from lUsersForDeletion random users
     * <p>
     * Partial errors are handled gracefully - errors are logged and added to the
     * response statistics, but execution continues. The method always returns a
     * response with statistics, even if some operations failed.
     *
     * @param requestDto DTO containing script parameters (nUsers, nTweetsPerUser, lUsersForDeletion)
     * @return BaseScriptResponseDto containing lists of created/deleted IDs and execution statistics
     */
    BaseScriptResponseDto executeScript(BaseScriptRequestDto requestDto);
}