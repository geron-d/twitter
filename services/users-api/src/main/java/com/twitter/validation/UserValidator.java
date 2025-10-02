package com.twitter.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.dto.UserPatchDto;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.common.enums.UserRole;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.common.exception.validation.ValidationException;

import java.util.UUID;

/**
 * Interface for user validation in Twitter system.
 * <p>
 * This interface centralizes all validation logic extracted from UserServiceImpl.
 * It provides comprehensive validation for user data including uniqueness checks,
 * business rule validation, and format validation for JSON Patch operations.
 *
 * @author Twitter Team
 * @version 1.0
 * @see UserValidatorImpl for the default implementation
 * @see ValidationException for validation error handling
 * @see BusinessRuleValidationException for business rule violations
 * @since 2025-01-27
 */
public interface UserValidator {
    
    /**
     * Performs complete validation for user creation.
     * <p>
     * This method validates user data for creation including uniqueness checks
     * for login and email fields. It ensures no duplicate users exist in the system.
     *
     * @param userRequest DTO containing user data for creation
     * @throws ValidationException if validation fails
     * @see UserValidatorImpl#validateForCreate(UserRequestDto) for implementation details
     * @since 2025-01-27
     */
    void validateForCreate(UserRequestDto userRequest);
    
    /**
     * Performs validation for user update operations.
     * <p>
     * This method validates user data for updates including uniqueness checks
     * with exclusion of the current user from uniqueness validation to allow
     * updates without changing login or email.
     *
     * @param userId     the ID of the user being updated
     * @param userUpdate DTO containing updated user data
     * @throws ValidationException if validation fails
     * @see UserValidatorImpl#validateForUpdate(UUID, UserUpdateDto) for implementation details
     * @since 2025-01-27
     */
    void validateForUpdate(UUID userId, UserUpdateDto userUpdate);
    
    /**
     * Performs validation for JSON Patch operations.
     * <p>
     * This method validates only the JSON structure of patch data to ensure
     * it can be properly applied to the target DTO. Business rule validation
     * is performed separately after patch application.
     *
     * @param userId    the ID of the user being patched
     * @param patchNode JSON data for the patch operation
     * @throws ValidationException if JSON structure validation fails
     * @see UserValidatorImpl#validateForPatch(UUID, JsonNode) for implementation details
     * @since 2025-01-27
     */
    void validateForPatch(UUID userId, JsonNode patchNode);
    
    /**
     * Performs validation for PATCH data with a prepared DTO.
     * <p>
     * This method validates the patched DTO using Bean Validation annotations
     * and performs uniqueness checks with exclusion of the current user.
     * It ensures data integrity after patch application.
     *
     * @param userId   the ID of the user being patched
     * @param patchDto prepared DTO for validation
     * @throws ValidationException if validation fails
     * @see UserValidatorImpl#validateForPatchWithDto(UUID, UserPatchDto) for implementation details
     * @since 2025-01-27
     */
    void validateForPatchWithDto(UUID userId, UserPatchDto patchDto);
    
    /**
     * Validates uniqueness of user login and email.
     * <p>
     * This method checks if the provided login or email already exists in the system.
     * It supports exclusion of a specific user ID for update operations to allow
     * users to keep their existing login or email unchanged.
     *
     * @param login         the login to validate (can be null)
     * @param email         the email to validate (can be null)
     * @param excludeUserId the user ID to exclude from uniqueness check (for updates)
     * @throws UniquenessValidationException if uniqueness conflict is detected
     * @see UserValidatorImpl#validateUniqueness(String, String, UUID) for implementation details
     * @since 2025-01-27
     */
    void validateUniqueness(String login, String email, UUID excludeUserId);
    
    /**
     * Validates the possibility of user deactivation.
     * <p>
     * This method enforces business rules to prevent deactivation of the last
     * active administrator in the system. It ensures system maintainability
     * by keeping at least one active administrator available.
     *
     * @param userId the ID of the user to be deactivated
     * @throws BusinessRuleValidationException if business rules are violated
     * @see UserValidatorImpl#validateAdminDeactivation(UUID) for implementation details
     * @since 2025-01-27
     */
    void validateAdminDeactivation(UUID userId);
    
    /**
     * Validates the possibility of user role change.
     * <p>
     * This method enforces business rules to prevent role changes for the last
     * active administrator. It ensures system maintainability by preventing
     * scenarios where no active administrators remain in the system.
     *
     * @param userId  the ID of the user
     * @param newRole the new role for the user
     * @throws BusinessRuleValidationException if business rules are violated
     * @see UserValidatorImpl#validateRoleChange(UUID, UserRole) for implementation details
     * @since 2025-01-27
     */
    void validateRoleChange(UUID userId, UserRole newRole);
    
    /**
     * Validates JSON structure of patch data.
     * <p>
     * This method validates the JSON structure to ensure it can be properly
     * applied to the target DTO. It checks for null values and ensures the
     * patch data is a valid JSON object.
     *
     * @param patchNode JSON data for the patch operation
     * @throws FormatValidationException if JSON format is invalid
     * @see UserValidatorImpl#validatePatchData(JsonNode) for implementation details
     * @since 2025-01-27
     */
    void validatePatchData(JsonNode patchNode);
    
    /**
     * Performs Bean Validation on patch DTO.
     * <p>
     * This method applies Bean Validation annotations to the UserPatchDto object
     * and collects all constraint violations. It provides detailed error messages
     * for validation failures to help with debugging.
     *
     * @param patchDto DTO to validate
     * @throws FormatValidationException if validation constraints are violated
     * @see UserValidatorImpl#validatePatchConstraints(UserPatchDto) for implementation details
     * @since 2025-01-27
     */
    void validatePatchConstraints(UserPatchDto patchDto);
}
