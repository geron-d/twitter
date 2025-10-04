package com.twitter.dto;

import com.twitter.common.enums.UserRole;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for user role update requests.
 * <p>
 * This record represents the data structure used for updating user roles
 * in the system. It includes validation constraints to ensure data
 * integrity and business rule compliance during role changes.
 *
 * @author geron
 * @version 1.0
 */
public record UserRoleUpdateDto(
    /**
     * New role to assign to the user.
     * <p>
     * This field contains the role that will be assigned to the user.
     * Must be a valid UserRole enum value and cannot be null.
     * Business rules may prevent certain role changes.
     */
    @NotNull(message = "Role cannot be null")
    UserRole role
) {
}
