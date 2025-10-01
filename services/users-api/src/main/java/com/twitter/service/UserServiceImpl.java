package com.twitter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.dto.*;
import com.twitter.dto.filter.UserFilter;
import com.twitter.entity.User;
import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.ValidationException;
import com.twitter.mapper.UserMapper;
import com.twitter.repository.UserRepository;
import com.twitter.util.PasswordUtil;
import com.twitter.util.PatchDtoFactory;
import com.twitter.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the user management service.
 * <p>
 * This service provides business logic for CRUD operations with users,
 * including creation, updating, deactivation, and role management. It handles
 * data validation, password hashing, and business rule enforcement.
 *
 * @author Twitter Team
 * @version 1.0
 * @see UserService for the service interface
 * @see UserValidator for validation logic
 * @see UserMapper for data transformation
 * @see UserRepository for data access
 * @since 2025-01-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PatchDtoFactory patchDtoFactory;

    /**
     * Retrieves a user by their unique identifier.
     * <p>
     * This method performs a database lookup and returns the user data
     * if found. Returns an empty Optional if the user does not exist
     * or has been deactivated.
     *
     * @param id the unique identifier of the user
     * @return Optional containing user data or empty if not found
     * @see UserRepository#findById(Object) for data access
     * @see UserMapper#toUserResponseDto(User) for data transformation
     * @since 2025-01-27
     */
    @Override
    public Optional<UserResponseDto> getUserById(UUID id) {
        return userRepository.findById(id).map(userMapper::toUserResponseDto);
    }

    /**
     * Retrieves a paginated list of users with applied filters.
     * <p>
     * This method uses JPA specifications for dynamic filtering based on
     * the provided criteria. It supports filtering by name, role, and status
     * with full pagination support.
     *
     * @param userFilter filter criteria for user search
     * @param pageable   pagination parameters (page size, page number, sorting)
     * @return Page containing filtered users with pagination metadata
     * @see UserRepository#findAll(org.springframework.data.jpa.domain.Specification, Pageable) for data access
     * @see UserFilter#toSpecification() for filter specification
     * @since 2025-01-27
     */
    @Override
    public Page<UserResponseDto> findAll(UserFilter userFilter, Pageable pageable) {
        return userRepository.findAll(userFilter.toSpecification(), pageable)
            .map(userMapper::toUserResponseDto);
    }

    /**
     * Creates a new user in the system.
     * <p>
     * This method performs comprehensive data validation, sets the default
     * status to ACTIVE and role to USER, and securely hashes the password
     * using PBKDF2 with a random salt.
     *
     * @param userRequest DTO containing user data for creation
     * @return the created user data
     * @throws ValidationException        if data validation fails
     * @throws ResponseStatusException    if password hashing fails
     * @see UserValidator#validateForCreate(UserRequestDto) for validation logic
     * @see PasswordUtil for password hashing
     * @since 2025-01-27
     */
    @Override
    public UserResponseDto createUser(UserRequestDto userRequest) {
        userValidator.validateForCreate(userRequest);

        User user = userMapper.toUser(userRequest);
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(UserRole.USER);

        setPassword(user, userRequest.password());

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(savedUser);
    }

    /**
     * Updates an existing user's data.
     * <p>
     * This method performs data validation excluding the current user from
     * uniqueness checks. It updates the password only if provided in the
     * request and maintains data integrity throughout the process.
     *
     * @param id          the unique identifier of the user
     * @param userDetails DTO containing new user data
     * @return Optional containing updated user data or empty if user not found
     * @throws ValidationException     if data validation fails
     * @throws ResponseStatusException if password hashing fails
     * @see UserValidator#validateForUpdate(UUID, UserUpdateDto) for validation logic
     * @see UserMapper#updateUserFromUpdateDto(UserUpdateDto, User) for data mapping
     * @since 2025-01-27
     */
    @Override
    public Optional<UserResponseDto> updateUser(UUID id, UserUpdateDto userDetails) {
        return userRepository.findById(id).map(user -> {
            userValidator.validateForUpdate(id, userDetails);

            userMapper.updateUserFromUpdateDto(userDetails, user);

            if (userDetails.password() != null && !userDetails.password().isEmpty()) {
                setPassword(user, userDetails.password());
            }

            User updatedUser = userRepository.save(user);
            return userMapper.toUserResponseDto(updatedUser);
        });
    }

    /**
     * Performs a partial update of user data using JSON Patch.
     * <p>
     * This method performs two-stage validation: JSON structure validation
     * and business rule validation. It applies changes only to specified
     * fields while preserving other user data.
     *
     * @param id        the unique identifier of the user
     * @param patchNode JSON data for partial update
     * @return Optional containing updated user data or empty if user not found
     * @throws ValidationException if JSON structure or business rule validation fails
     * @see UserValidator#validateForPatch(UUID, JsonNode) for JSON validation
     * @see UserValidator#validateForPatchWithDto(UUID, UserPatchDto) for business validation
     * @since 2025-01-27
     */
    @Override
    public Optional<UserResponseDto> patchUser(UUID id, JsonNode patchNode) {
        return userRepository.findById(id).map(user -> {
            userValidator.validateForPatch(id, patchNode);

            UserPatchDto userPatchDto = userMapper.toUserPatchDto(user);
            userPatchDto = patchDtoFactory.createPatchDto(userPatchDto, patchNode);

            userValidator.validateForPatchWithDto(id, userPatchDto);

            userMapper.updateUserFromPatchDto(userPatchDto, user);

            User updatedUser = userRepository.save(user);
            return userMapper.toUserResponseDto(updatedUser);
        });
    }

    /**
     * Deactivates a user by setting their status to INACTIVE.
     * <p>
     * This method performs business rule validation to prevent deactivation
     * of the last active administrator. It logs successful deactivation
     * for audit purposes.
     *
     * @param id the unique identifier of the user
     * @return Optional containing deactivated user data or empty if user not found
     * @throws BusinessRuleValidationException if attempting to deactivate the last active administrator
     * @see UserValidator#validateAdminDeactivation(UUID) for business rule validation
     * @since 2025-01-27
     */
    @Override
    public Optional<UserResponseDto> inactivateUser(UUID id) {
        return userRepository.findById(id).map(user -> {
            userValidator.validateAdminDeactivation(id);

            user.setStatus(UserStatus.INACTIVE);
            User updatedUser = userRepository.save(user);
            log.info("User with ID {} has been successfully deactivated", id);
            return userMapper.toUserResponseDto(updatedUser);
        });
    }

    /**
     * Updates the role of a user in the system.
     * <p>
     * This method performs business rule validation to prevent role changes
     * for the last active administrator. It logs role changes with both
     * old and new role information for audit purposes.
     *
     * @param id         the unique identifier of the user
     * @param roleUpdate DTO containing the new user role
     * @return Optional containing updated user data or empty if user not found
     * @throws BusinessRuleValidationException if attempting to change the last active administrator's role
     * @see UserValidator#validateRoleChange(UUID, UserRole) for business rule validation
     * @since 2025-01-27
     */
    @Override
    public Optional<UserResponseDto> updateUserRole(UUID id, UserRoleUpdateDto roleUpdate) {
        return userRepository.findById(id).map(user -> {
            UserRole oldRole = user.getRole();
            UserRole newRole = roleUpdate.role();

            userValidator.validateRoleChange(id, newRole);

            user.setRole(newRole);
            User updatedUser = userRepository.save(user);

            log.info("User role updated for ID {}: {} -> {}", id, oldRole, newRole);
            return userMapper.toUserResponseDto(updatedUser);
        });
    }

    /**
     * Sets a hashed password for a user.
     * <p>
     * This private method generates a random salt and hashes the password
     * using PBKDF2 algorithm. It stores both the password hash and salt
     * in Base64 encoding for secure storage.
     *
     * @param user     the user to set the password for
     * @param password the password in plain text
     * @throws ResponseStatusException if salt generation or password hashing fails
     * @see PasswordUtil#getSalt() for salt generation
     * @see PasswordUtil#hashPassword(String, byte[]) for password hashing
     * @since 2025-01-27
     */
    private void setPassword(User user, String password) {
        try {
            byte[] salt = PasswordUtil.getSalt();
            String hashedPassword = PasswordUtil.hashPassword(password, salt);
            user.setPasswordHash(hashedPassword);
            user.setPasswordSalt(Base64.getEncoder().encodeToString(salt));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error hashing password: " + e.getMessage(), e);
        }
    }
}
