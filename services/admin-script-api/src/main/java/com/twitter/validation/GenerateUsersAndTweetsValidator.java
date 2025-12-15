package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.dto.request.GenerateUsersAndTweetsRequestDto;

/**
 * Interface for validating parameters of the generate users and tweets script.
 *
 * @author geron
 * @version 1.0
 */
public interface GenerateUsersAndTweetsValidator {

    /**
     * Validates that the number of users for deletion does not exceed
     * the number of users who have tweets.
     *
     * @param requestDto           DTO containing script parameters (nUsers, nTweetsPerUser, lUsersForDeletion)
     * @param usersWithTweetsCount actual number of users who have tweets (after creation)
     * @throws BusinessRuleValidationException if lUsersForDeletion > usersWithTweetsCount
     */
    void validateDeletionCount(GenerateUsersAndTweetsRequestDto requestDto, int usersWithTweetsCount);
}

