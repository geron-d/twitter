package com.twitter.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.common.exception.validation.ValidationException;
import com.twitter.dto.UserPatchDto;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.entity.User;
import com.twitter.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the user validator for Twitter system.
 * <p>
 * This validator centralizes all validation logic extracted from UserServiceImpl.
 * It provides comprehensive validation for user data including uniqueness checks,
 * business rule validation, and format validation for JSON Patch operations.
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserValidatorImpl implements UserValidator {

    private final UserRepository userRepository;
    private final Validator validator;

    /**
     * Performs complete validation for user creation.
     * <p>
     * This method validates user data for creation including uniqueness checks
     * for login and email fields. It ensures no duplicate users exist in the system.
     *
     * @param userRequest DTO containing user data for creation
     * @throws ValidationException if validation fails
     */
    @Override
    public void validateForCreate(UserRequestDto userRequest) {
        validateUniqueness(userRequest.login(), userRequest.email(), null);
    }

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
     */
    @Override
    public void validateForUpdate(UUID userId, UserUpdateDto userUpdate) {
        validateUniqueness(userUpdate.login(), userUpdate.email(), userId);
    }

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
     */
    @Override
    public void validateForPatch(UUID userId, JsonNode patchNode) {
        validatePatchData(patchNode);
    }

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
     */
    @Override
    public void validateForPatchWithDto(UUID userId, UserPatchDto patchDto) {
        validatePatchConstraints(patchDto);
        validateUniqueness(patchDto.getLogin(), patchDto.getEmail(), userId);
    }

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
     */
    @Override
    public void validateUniqueness(String login, String email, UUID excludeUserId) {
        if (!ObjectUtils.isEmpty(login)) {
            boolean loginExists = excludeUserId != null
                ? userRepository.existsByLoginAndIdNot(login, excludeUserId)
                : userRepository.existsByLogin(login);

            if (loginExists) {
                log.warn("Uniqueness validation failed: login '{}' already exists", login);
                throw new UniquenessValidationException("login", login);
            }
        }

        if (!ObjectUtils.isEmpty(email)) {
            boolean emailExists = excludeUserId != null
                ? userRepository.existsByEmailAndIdNot(email, excludeUserId)
                : userRepository.existsByEmail(email);

            if (emailExists) {
                log.warn("Uniqueness validation failed: email '{}' already exists", email);
                throw new UniquenessValidationException("email", email);
            }
        }
    }

    /**
     * Validates the possibility of user deactivation.
     * <p>
     * This method enforces business rules to prevent deactivation of the last
     * active administrator in the system. It ensures system maintainability
     * by keeping at least one active administrator available.
     *
     * @param userId the ID of the user to be deactivated
     * @throws BusinessRuleValidationException if business rules are violated
     */
    @Override
    public void validateAdminDeactivation(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessRuleValidationException("USER_NOT_FOUND", userId));

        if (user.getRole() == UserRole.ADMIN) {
            long activeAdminCount = userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
            if (activeAdminCount <= 1) {
                log.warn("Business rule validation failed: attempt to deactivate last admin with ID: {}", userId);
                throw BusinessRuleValidationException.lastAdminDeactivation(userId);
            }
        }
    }

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
     */
    @Override
    public void validateRoleChange(UUID userId, UserRole newRole) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessRuleValidationException("USER_NOT_FOUND", userId));

        UserRole oldRole = user.getRole();

        if (oldRole == UserRole.ADMIN && newRole != UserRole.ADMIN) {
            long activeAdminCount = userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
            if (activeAdminCount <= 1) {
                log.warn("Business rule validation failed: attempt to change role of last admin with ID: {} from {} to {}",
                    userId, oldRole, newRole);
                throw BusinessRuleValidationException.lastAdminRoleChange(userId, newRole);
            }
        }
    }

    /**
     * Validates JSON structure of patch data.
     * <p>
     * This method validates the JSON structure to ensure it can be properly
     * applied to the target DTO. It checks for null values and ensures the
     * patch data is a valid JSON object.
     *
     * @param patchNode JSON data for the patch operation
     * @throws FormatValidationException if JSON format is invalid
     */
    @Override
    public void validatePatchData(JsonNode patchNode) {
        if (patchNode == null || patchNode.isNull()) {
            throw new FormatValidationException("Patch data cannot be null");
        }

        if (!patchNode.isObject()) {
            throw new FormatValidationException("Patch data must be a JSON object");
        }
    }

    /**
     * Performs Bean Validation on patch DTO.
     * <p>
     * This method applies Bean Validation annotations to the UserPatchDto object
     * and collects all constraint violations. It provides detailed error messages
     * for validation failures to help with debugging.
     *
     * @param patchDto DTO to validate
     * @throws FormatValidationException if validation constraints are violated
     */
    @Override
    public void validatePatchConstraints(UserPatchDto patchDto) {
        Set<ConstraintViolation<UserPatchDto>> violations = validator.validate(patchDto);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

            log.warn("Patch constraints validation failed: {}", errorMessage);
            throw new FormatValidationException("Validation failed: " + errorMessage);
        }
    }
}
