package com.twitter.dto;

import com.twitter.common.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for user role update requests.
 * <p>
 * It includes validation constraints to ensure data
 * integrity and business rule compliance during role changes.
 *
 * @param role new role to assign to the user
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "UserRoleUpdate",
    description = "Data structure for updating user roles in the system",
    example = """
        {
          "role": "ADMIN"
        }
        """
)
public record UserRoleUpdateDto(
    /**
     * New role to assign to the user.
     * <p>
     * Must be a valid UserRole enum value and cannot be null.
     * Business rules may prevent certain role changes.
     */
    @Schema(
        description = "New role to assign to the user",
        example = "ADMIN",
        implementation = UserRole.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Role cannot be null")
    UserRole role
) {
}
