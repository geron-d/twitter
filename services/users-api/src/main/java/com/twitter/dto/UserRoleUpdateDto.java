package com.twitter.dto;

import com.twitter.common.enums.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for user role update requests.
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