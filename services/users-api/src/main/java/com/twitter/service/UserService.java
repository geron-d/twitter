package com.twitter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.ValidationException;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
import com.twitter.dto.UserRoleUpdateDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.dto.filter.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for user management in Twitter microservices.
 * <p>
 * This interface defines the contract for user management services, providing
 * business logic for CRUD operations with users, including validation,
 * password hashing, and business rule enforcement.
 *
 * @author geron
 * @version 1.0
 */
public interface UserService {

    /**
     * Retrieves a user by their unique identifier.
     * <p>
     * Returns an empty Optional if the user does not exist
     * or has been deactivated.
     *
     * @param id the unique identifier of the user
     * @return Optional containing user data or empty if not found
     */
    Optional<UserResponseDto> getUserById(UUID id);

    /**
     * Retrieves a paginated list of users with applied filters.
     * <p>
     * This method supports filtering by various criteria including name,
     * role, and status. It provides full pagination support with sorting
     * capabilities for efficient data retrieval.
     *
     * @param userFilter filter criteria for user search (name, role, status)
     * @param pageable   pagination parameters (page, size, sorting)
     * @return Page containing filtered list of users with pagination metadata
     */
    Page<UserResponseDto> findAll(UserFilter userFilter, Pageable pageable);

    /**
     * Creates a new user in the system.
     * <p>
     * This method creates a new user with the provided data. The system
     * automatically sets the status to ACTIVE and role to USER. The password
     * is securely hashed using PBKDF2 algorithm with a random salt.
     *
     * @param userRequest user data for creation
     * @return the created user data
     * @throws ValidationException if validation fails or uniqueness conflict occurs
     */
    UserResponseDto createUser(UserRequestDto userRequest);

    /**
     * Performs a complete update of an existing user.
     * <p>
     * This method replaces all user fields with new values from the provided DTO.
     * It includes validation for uniqueness and business rules to ensure
     * data integrity throughout the update process.
     *
     * @param id          the unique identifier of the user to update
     * @param userDetails new user data for the update
     * @return Optional containing updated user data or empty if user not found
     * @throws ValidationException if validation fails or uniqueness conflict occurs
     */
    Optional<UserResponseDto> updateUser(UUID id, UserUpdateDto userDetails);

    /**
     * Performs a partial update of user data using JSON Patch.
     * <p>
     * This method allows updating only specified fields without modifying
     * other user data. It includes validation for JSON structure and business
     * rules to ensure data integrity during partial updates.
     *
     * @param id        the unique identifier of the user to update
     * @param patchNode JSON data for partial update
     * @return Optional containing updated user data or empty if user not found
     * @throws ValidationException if JSON structure or business rule validation fails
     */
    Optional<UserResponseDto> patchUser(UUID id, JsonNode patchNode);

    /**
     * Deactivates a user by setting their status to INACTIVE.
     * <p>
     * This method deactivates a user account while enforcing business rules
     * to prevent deactivation of the last active administrator in the system.
     * It ensures system maintainability by keeping at least one active administrator.
     *
     * @param id the unique identifier of the user to deactivate
     * @return Optional containing updated user data or empty if user not found
     * @throws BusinessRuleValidationException if attempting to deactivate the last administrator
     */
    Optional<UserResponseDto> inactivateUser(UUID id);

    /**
     * Updates the role of a user in the system.
     * <p>
     * This method changes the user's role while enforcing business rules
     * to prevent modification of the last active administrator's role.
     * It supports various roles including USER, ADMIN, and MODERATOR.
     *
     * @param id         the unique identifier of the user
     * @param roleUpdate data containing the new user role
     * @return Optional containing updated user data or empty if user not found
     * @throws BusinessRuleValidationException if attempting to change the last administrator's role
     */
    Optional<UserResponseDto> updateUserRole(UUID id, UserRoleUpdateDto roleUpdate);

    /**
     * Checks whether a user exists in the system by their unique identifier.
     *
     * @param id the unique identifier of the user to check
     * @return true if the user exists, false otherwise
     */
    boolean existsById(UUID id);
}
