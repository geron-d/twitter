package com.twitter.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.common.aspect.LoggableRequest;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.ValidationException;
import com.twitter.common.dto.UserExistsResponseDto;
import com.twitter.dto.*;
import com.twitter.dto.filter.UserFilter;
import com.twitter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for user management in Twitter microservices.
 * <p>
 * This controller provides a complete set of CRUD operations for user management
 * with support for filtering, pagination, and role-based access control. It handles
 * HTTP requests and delegates business logic to the UserService layer.
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    /**
     * Checks whether a user exists by their unique identifier.
     *
     * @param userId the unique identifier of the user to check
     * @return ResponseEntity containing UserExistsResponseDto with boolean exists field
     */
    @LoggableRequest
    @GetMapping("/{userId}/exists")
    @Override
    public ResponseEntity<UserExistsResponseDto> existsUser(@PathVariable("userId") UUID userId) {
        boolean exists = userService.existsById(userId);
        return ResponseEntity.ok(new UserExistsResponseDto(exists));
    }

    /**
     * Retrieves a user by their unique identifier.
     * <p>
     * This endpoint performs a database lookup and returns the user data
     * if found. Returns HTTP 404 if the user does not exist or has been
     * deactivated.
     *
     * @param id the unique identifier of the user
     * @return ResponseEntity containing user data or 404 if not found
     */
    @LoggableRequest
    @GetMapping("/{id}")
    @Override
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") UUID id) {
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a paginated list of users with optional filtering.
     * <p>
     * This endpoint supports filtering by first name, last name, role, and status.
     * It returns a paginated response with metadata about the total number of
     * records and pagination information.
     *
     * @param userFilter filter criteria for user search (name, role, status)
     * @param pageable   pagination parameters (page, size, sorting)
     * @return PagedModel containing filtered list of users with pagination metadata
     */
    @LoggableRequest
    @GetMapping
    @Override
    public PagedModel<UserResponseDto> findAll(@ModelAttribute UserFilter userFilter,
                                               @PageableDefault(size = 10) Pageable pageable) {
        Page<UserResponseDto> users = userService.findAll(userFilter, pageable);
        return new PagedModel<>(users);
    }

    /**
     * Creates a new user in the system.
     * <p>
     * This endpoint creates a new user with the provided data. The system
     * automatically sets the status to ACTIVE and role to USER. The password
     * is securely hashed using PBKDF2 algorithm with a random salt.
     *
     * @param userRequest user data for creation
     * @return the created user data
     * @throws ValidationException if validation fails or uniqueness conflict occurs
     */
    @LoggableRequest(hideFields = {"password"})
    @PostMapping
    @Override
    public UserResponseDto createUser(@RequestBody @Valid UserRequestDto userRequest) {
        return userService.createUser(userRequest);
    }

    /**
     * Performs a complete update of an existing user.
     * <p>
     * This endpoint replaces all user fields with the new values provided
     * in the request body. It performs validation and uniqueness checks
     * before updating the user record.
     *
     * @param id          the unique identifier of the user to update
     * @param userDetails new user data for the update
     * @return ResponseEntity containing updated user data or 404 if user not found
     * @throws ValidationException if validation fails or uniqueness conflict occurs
     */
    @LoggableRequest(hideFields = {"password"})
    @PutMapping("/{id}")
    @Override
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable("id") UUID id, @RequestBody @Valid UserUpdateDto userDetails) {
        return userService.updateUser(id, userDetails)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Performs a partial update of user data using JSON Patch.
     * <p>
     * This endpoint allows updating only specified fields without modifying
     * other user data. It uses JSON Patch format to apply changes selectively
     * and performs validation on the patched data.
     *
     * @param id        the unique identifier of the user to update
     * @param patchNode JSON patch data for partial update
     * @return ResponseEntity containing updated user data or 404 if user not found
     * @throws ValidationException if validation fails or JSON format is invalid
     */
    @LoggableRequest
    @PatchMapping("/{id}")
    @Override
    public ResponseEntity<UserResponseDto> patchUser(@PathVariable("id") UUID id, @RequestBody JsonNode patchNode) {
        return userService.patchUser(id, patchNode)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deactivates a user by setting their status to INACTIVE.
     * <p>
     * This endpoint deactivates a user account, preventing them from
     * accessing the system. It includes business rule validation to
     * prevent deactivation of the last active administrator.
     *
     * @param id the unique identifier of the user to deactivate
     * @return ResponseEntity containing updated user data or 404 if user not found
     * @throws BusinessRuleValidationException if attempting to deactivate the last administrator
     */
    @LoggableRequest
    @PatchMapping("/{id}/inactivate")
    @Override
    public ResponseEntity<UserResponseDto> inactivateUser(@PathVariable("id") UUID id) {
        return userService.inactivateUser(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates the role of a user.
     * <p>
     * This endpoint changes the user's role while enforcing business rules
     * to prevent modification of the last active administrator's role.
     * It validates the new role and applies appropriate permissions.
     *
     * @param id         the unique identifier of the user
     * @param roleUpdate data containing the new role information
     * @return ResponseEntity containing updated user data or 404 if user not found
     * @throws BusinessRuleValidationException if attempting to change the last administrator's role
     */
    @LoggableRequest
    @PatchMapping("/{id}/role")
    @Override
    public ResponseEntity<UserResponseDto> updateUserRole(@PathVariable("id") UUID id,
                                                          @RequestBody @Valid UserRoleUpdateDto roleUpdate) {
        return userService.updateUserRole(id, roleUpdate)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
