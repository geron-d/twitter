package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.dto.request.BaseScriptRequestDto;

/**
 * Interface for validating parameters of the base script.
 *
 * @author geron
 * @version 1.0
 */
public interface BaseScriptValidator {

    /**
     * Validates that the number of users for deletion does not exceed
     * the number of users who have tweets.
     *
     * @param requestDto           DTO containing script parameters (nUsers, nTweetsPerUser, lUsersForDeletion)
     * @param usersWithTweetsCount actual number of users who have tweets (after creation)
     * @throws BusinessRuleValidationException if lUsersForDeletion > usersWithTweetsCount
     */
    void validateDeletionCount(BaseScriptRequestDto requestDto, int usersWithTweetsCount);
}
