package com.twitter.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.common.dto.UserExistsResponseDto;
import com.twitter.common.dto.request.UserRequestDto;
import com.twitter.common.dto.response.UserResponseDto;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.ValidationException;
import com.twitter.dto.UserRoleUpdateDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.dto.filter.UserFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * OpenAPI interface for User Management API.
 * <p>
 * This interface contains all OpenAPI annotations for the User Management API endpoints.
 *
 * @author geron
 * @version 1.0
 */
@Tag(name = "User Management", description = "API for managing users in the Twitter system")
public interface UserApi {

    /**
     * Checks whether a user exists by their unique identifier.
     *
     * @param userId the unique identifier of the user to check
     * @return ResponseEntity containing UserExistsResponseDto with boolean exists field
     */
    @Operation(
        summary = "Check user existence",
        description = "Checks whether a user exists in the system by their unique identifier. " +
            "Returns true if user exists, false otherwise."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User existence check completed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserExistsResponseDto.class),
                examples = @ExampleObject(
                    name = "User Exists",
                    summary = "Example: user exists",
                    value = """
                        {
                          "exists": true
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "200",
            description = "User does not exist",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserExistsResponseDto.class),
                examples = @ExampleObject(
                    name = "User Does Not Exist",
                    summary = "Example: user does not exist",
                    value = """
                        {
                          "exists": false
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<UserExistsResponseDto> existsUser(
        @Parameter(
            description = "Unique identifier of the user to check",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        UUID userId);

    /**
     * Retrieves a user by their unique identifier.
     * <p>
     * Returns HTTP 404 if the user does not exist or has been deactivated.
     *
     * @param id the unique identifier of the user
     * @return ResponseEntity containing user data or 404 if not found
     */
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a specific user by their unique identifier. Returns 404 if user not found or deactivated."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDto.class),
                examples = @ExampleObject(
                    name = "User Response",
                    summary = "Example user data",
                    value = """
                        {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "login": "john_doe",
                          "firstName": "John",
                          "lastName": "Doe",
                          "email": "john.doe@example.com",
                          "status": "ACTIVE",
                          "role": "USER"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Not Found",
                    summary = "User not found error",
                    value = """
                        {
                          "type": "https://example.com/errors/not-found",
                          "title": "Not Found",
                          "status": 404,
                          "detail": "User with id '123e4567-e89b-12d3-a456-426614174000' not found",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<UserResponseDto> getUserById(
        @Parameter(description = "Unique identifier of the user", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id);

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
    @Operation(
        summary = "Get users with filtering and pagination",
        description = "Retrieves a paginated list of users with optional filtering by name, role, and status. Supports sorting and pagination."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PagedModel.class),
                examples = @ExampleObject(
                    name = "Paginated Users",
                    summary = "Example paginated response",
                    value = """
                        {
                          "content": [
                            {
                              "id": "123e4567-e89b-12d3-a456-426614174000",
                              "login": "john_doe",
                              "firstName": "John",
                              "lastName": "Doe",
                              "email": "john.doe@example.com",
                              "status": "ACTIVE",
                              "role": "USER"
                            }
                          ],
                          "page": {
                            "size": 10,
                            "number": 0,
                            "totalElements": 1,
                            "totalPages": 1
                          }
                        }
                        """
                )
            )
        )
    })
    PagedModel<UserResponseDto> findAll(
        @Parameter(description = "Filter criteria for user search")
        UserFilter userFilter,
        @Parameter(description = "Pagination parameters (page, size, sorting)")
        Pageable pageable);

    /**
     * Creates a new user in the system.
     * <p>
     * The system automatically sets the status to ACTIVE and role to USER.
     * The password is securely hashed using PBKDF2 algorithm with a random salt.
     *
     * @param userRequest user data for creation
     * @return the created user data
     * @throws ValidationException if validation fails or uniqueness conflict occurs
     */
    @Operation(
        summary = "Create new user",
        description = "Creates a new user in the system. Password is securely hashed. Status is set to ACTIVE and role to USER by default."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDto.class),
                examples = @ExampleObject(
                    name = "Created User",
                    summary = "Example created user",
                    value = """
                        {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "login": "jane_smith",
                          "firstName": "Jane",
                          "lastName": "Smith",
                          "email": "jane.smith@example.com",
                          "status": "ACTIVE",
                          "role": "USER"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    summary = "Validation failed",
                    value = """
                        {
                          "type": "https://example.com/errors/validation-error",
                          "title": "Validation Error",
                          "status": 400,
                          "detail": "Validation failed: email must be a valid email address",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Uniqueness conflict",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Conflict Error",
                    summary = "User already exists",
                    value = """
                        {
                          "type": "https://example.com/errors/uniqueness-validation",
                          "title": "Uniqueness Validation Error",
                          "status": 409,
                          "detail": "User with login 'jane_smith' already exists",
                          "fieldName": "login",
                          "fieldValue": "jane_smith",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        )
    })
    UserResponseDto createUser(
        @Parameter(description = "User data for creation", required = true)
        UserRequestDto userRequest);

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
    @Operation(
        summary = "Update user completely",
        description = "Performs a complete update of an existing user. Replaces all user fields with new values. Validates uniqueness and business rules."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDto.class),
                examples = @ExampleObject(
                    name = "Updated User",
                    summary = "Example updated user",
                    value = """
                        {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "login": "jane_smith_updated",
                          "firstName": "Jane",
                          "lastName": "Smith-Wilson",
                          "email": "jane.wilson@example.com",
                          "status": "ACTIVE",
                          "role": "USER"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Not Found",
                    summary = "User not found",
                    value = """
                        {
                          "type": "https://example.com/errors/not-found",
                          "title": "Not Found",
                          "status": 404,
                          "detail": "User with id '123e4567-e89b-12d3-a456-426614174000' not found",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    summary = "Validation failed",
                    value = """
                        {
                          "type": "https://example.com/errors/validation-error",
                          "title": "Validation Error",
                          "status": 400,
                          "detail": "Validation failed: login must be between 3 and 50 characters",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Uniqueness conflict",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Conflict Error",
                    summary = "User already exists",
                    value = """
                        {
                          "type": "https://example.com/errors/uniqueness-validation",
                          "title": "Uniqueness Validation Error",
                          "status": 409,
                          "detail": "User with email 'jane.wilson@example.com' already exists",
                          "fieldName": "email",
                          "fieldValue": "jane.wilson@example.com",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<UserResponseDto> updateUser(
        @Parameter(description = "Unique identifier of the user to update", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        @Parameter(description = "New user data for the update", required = true)
        UserUpdateDto userDetails);

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
    @Operation(
        summary = "Partially update user",
        description = "Performs a partial update of user data using JSON Patch. Allows updating only specified fields without modifying other data."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDto.class),
                examples = @ExampleObject(
                    name = "Patched User",
                    summary = "Example patched user",
                    value = """
                        {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "login": "jane_smith",
                          "firstName": "Jane",
                          "lastName": "Smith-Wilson",
                          "email": "jane.smith@example.com",
                          "status": "ACTIVE",
                          "role": "USER"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Not Found",
                    summary = "User not found",
                    value = """
                        {
                          "type": "https://example.com/errors/not-found",
                          "title": "Not Found",
                          "status": 404,
                          "detail": "User with id '123e4567-e89b-12d3-a456-426614174000' not found",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error or invalid JSON Patch",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    summary = "Validation failed",
                    value = """
                        {
                          "type": "https://example.com/errors/validation-error",
                          "title": "Validation Error",
                          "status": 400,
                          "detail": "Validation failed: login must be between 3 and 50 characters",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Uniqueness conflict",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Conflict Error",
                    summary = "User already exists",
                    value = """
                        {
                          "type": "https://example.com/errors/uniqueness-validation",
                          "title": "Uniqueness Validation Error",
                          "status": 409,
                          "detail": "User with login 'new_login' already exists",
                          "fieldName": "login",
                          "fieldValue": "new_login",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<UserResponseDto> patchUser(
        @Parameter(description = "Unique identifier of the user to update", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        @Parameter(description = "JSON patch data for partial update", required = true)
        JsonNode patchNode);

    /**
     * Deactivates a user by setting their status to INACTIVE.
     * <p>
     * It includes business rule validation to
     * prevent deactivation of the last active administrator.
     *
     * @param id the unique identifier of the user to deactivate
     * @return ResponseEntity containing updated user data or 404 if user not found
     * @throws BusinessRuleValidationException if attempting to deactivate the last administrator
     */
    @Operation(
        summary = "Deactivate user",
        description = "Deactivates a user by setting their status to INACTIVE. Includes business rule validation to prevent deactivation of the last active administrator."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User deactivated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDto.class),
                examples = @ExampleObject(
                    name = "Deactivated User",
                    summary = "Example deactivated user",
                    value = """
                        {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "login": "jane_smith",
                          "firstName": "Jane",
                          "lastName": "Smith",
                          "email": "jane.smith@example.com",
                          "status": "INACTIVE",
                          "role": "USER"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Not Found",
                    summary = "User not found",
                    value = """
                        {
                          "type": "https://example.com/errors/not-found",
                          "title": "Not Found",
                          "status": 404,
                          "detail": "User with id '123e4567-e89b-12d3-a456-426614174000' not found",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Business rule violation",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Business Rule Error",
                    summary = "Cannot deactivate last admin",
                    value = """
                        {
                          "type": "https://example.com/errors/business-rule-validation",
                          "title": "Business Rule Validation Error",
                          "status": 400,
                          "detail": "Business rule 'LAST_ADMIN_DEACTIVATION' violated for context: userId=123e4567-e89b-12d3-a456-426614174000",
                          "ruleName": "LAST_ADMIN_DEACTIVATION",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<UserResponseDto> inactivateUser(
        @Parameter(description = "Unique identifier of the user to deactivate", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id);

    /**
     * Updates the role of a user.
     * <p>
     * This endpoint changes the user's role while enforcing business rules
     * to prevent modification of the last active administrator's role.
     *
     * @param id         the unique identifier of the user
     * @param roleUpdate data containing the new role information
     * @return ResponseEntity containing updated user data or 404 if user not found
     * @throws BusinessRuleValidationException if attempting to change the last administrator's role
     */
    @Operation(
        summary = "Update user role",
        description = "Updates the role of a user while enforcing business rules to prevent modification of the last active administrator's role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User role updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDto.class),
                examples = @ExampleObject(
                    name = "Updated Role User",
                    summary = "Example user with updated role",
                    value = """
                        {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "login": "jane_smith",
                          "firstName": "Jane",
                          "lastName": "Smith",
                          "email": "jane.smith@example.com",
                          "status": "ACTIVE",
                          "role": "ADMIN"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Not Found",
                    summary = "User not found",
                    value = """
                        {
                          "type": "https://example.com/errors/not-found",
                          "title": "Not Found",
                          "status": 404,
                          "detail": "User with id '123e4567-e89b-12d3-a456-426614174000' not found",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Business rule violation or validation error",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Business Rule Error",
                    summary = "Cannot change last admin role",
                    value = """
                        {
                          "type": "https://example.com/errors/business-rule-validation",
                          "title": "Business Rule Validation Error",
                          "status": 400,
                          "detail": "Business rule 'LAST_ADMIN_ROLE_CHANGE' violated for context: userId=123e4567-e89b-12d3-a456-426614174000",
                          "ruleName": "LAST_ADMIN_ROLE_CHANGE",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<UserResponseDto> updateUserRole(
        @Parameter(description = "Unique identifier of the user", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        @Parameter(description = "Data containing the new role information", required = true)
        UserRoleUpdateDto roleUpdate);
}
