package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.dto.request.BaseScriptRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implementation of the validator for base script.
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BaseScriptValidatorImpl implements BaseScriptValidator {

    /**
     * @see BaseScriptValidator#validateDeletionCount
     */
    @Override
    public void validateDeletionCount(BaseScriptRequestDto requestDto, int usersWithTweetsCount) {
        if (requestDto == null) {
            log.error("Request DTO is null");
            throw new IllegalArgumentException("Request DTO cannot be null");
        }

        int lUsersForDeletion = requestDto.lUsersForDeletion();

        if (lUsersForDeletion == 0) {
            log.debug("No deletions requested (lUsersForDeletion = 0), validation passed");
            return;
        }

        if (lUsersForDeletion > usersWithTweetsCount) {
            String errorMessage = String.format("Cannot delete tweets from %d users: only %d users have tweets",
                lUsersForDeletion, usersWithTweetsCount);
            throw new BusinessRuleValidationException("DELETION_COUNT_EXCEEDS_USERS_WITH_TWEETS", errorMessage);
        }

        log.debug("Deletion count validation passed: {} <= {}", lUsersForDeletion, usersWithTweetsCount);
    }
}
