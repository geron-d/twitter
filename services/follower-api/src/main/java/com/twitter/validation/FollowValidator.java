package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.request.FollowRequestDto;

/**
 * Interface for follow relationship validation in Twitter system.
 * <p>
 * This interface centralizes all validation logic for follow relationships,
 * ensuring business rules are enforced before creating follow relationships.
 *
 * @author geron
 * @version 1.0
 */
public interface FollowValidator {

    /**
     * Performs complete validation for follow relationship creation.
     * <p>
     * This method validates follow relationship data for creation including:
     * <ul>
     *   <li>Self-follow check (users cannot follow themselves)</li>
     *   <li>Uniqueness check (follow relationship must not already exist)</li>
     *   <li>User existence check (both follower and following users must exist)</li>
     * </ul>
     *
     * @param request DTO containing follow relationship data for creation
     * @throws BusinessRuleValidationException if users cannot follow themselves or users don't exist
     * @throws UniquenessValidationException   if follow relationship already exists
     */
    void validateForFollow(FollowRequestDto request);
}

