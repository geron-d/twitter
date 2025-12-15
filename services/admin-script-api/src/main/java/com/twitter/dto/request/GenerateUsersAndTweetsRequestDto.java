package com.twitter.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * Data Transfer Object for administrative script request to generate users and tweets.
 * <p>
 * This record represents the data structure used for executing the administrative
 * script that creates multiple users with random data, adds tweets for each user,
 * and deletes one tweet from a specified number of random users.
 *
 * @param nUsers            number of users to create
 * @param nTweetsPerUser    number of tweets to create for each user
 * @param lUsersForDeletion number of users from which to delete one tweet each
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "GenerateUsersAndTweetsRequest",
    description = "Data structure for administrative script request to generate users and tweets",
    example = """
        {
          "nUsers": 10,
          "nTweetsPerUser": 5,
          "lUsersForDeletion": 3
        }
        """
)
@Builder
public record GenerateUsersAndTweetsRequestDto(

    /**
     * Number of users to create.
     * <p>
     * This field specifies how many users should be created with random data.
     * Must be between 1 and 1000 to prevent excessive load on the system.
     */
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

    /**
     * Number of tweets to create for each user.
     * <p>
     * This field specifies how many tweets should be created for each successfully
     * created user. Must be between 1 and 100 to prevent excessive load.
     */
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

    /**
     * Number of users from which to delete one tweet each.
     * <p>
     * This field specifies how many random users should be selected for tweet deletion.
     * Must be at least 0 (0 means no deletions). The actual number of deletions may be
     * less if there are not enough users with tweets. Business validation ensures that
     * this value does not exceed the number of users with tweets.
     */
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
