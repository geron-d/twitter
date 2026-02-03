package com.twitter.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * Data Transfer Object for base administrative script request.
 *
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "BaseScriptRequest",
    description = "Data structure for base administrative script request",
    example = """
        {
          "nUsers": 10,
          "nTweetsPerUser": 5,
          "lUsersForDeletion": 3
        }
        """
)
@Builder
public record BaseScriptRequestDto(

    @Schema(
        description = "Number of users to create with random data",
        example = "10",
        minimum = "1",
        maximum = "1000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Number of users cannot be null")
    @Min(value = 1, message = "Number of users must be at least 1")
    @Max(value = 1000, message = "Number of users cannot exceed 1000")
    Integer nUsers,

    @Schema(
        description = "Number of tweets to create for each user",
        example = "5",
        minimum = "1",
        maximum = "100",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Number of tweets per user cannot be null")
    @Min(value = 1, message = "Number of tweets per user must be at least 1")
    @Max(value = 100, message = "Number of tweets per user cannot exceed 100")
    Integer nTweetsPerUser,

    @Schema(
        description = "Number of users from which to delete one tweet each (0 means no deletions)",
        example = "3",
        minimum = "0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Number of users for deletion cannot be null")
    @Min(value = 0, message = "Number of users for deletion cannot be negative")
    Integer lUsersForDeletion
) {
}
