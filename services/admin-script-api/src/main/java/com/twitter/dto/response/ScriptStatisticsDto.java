package com.twitter.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Data Transfer Object for script execution statistics.
 *
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "ScriptStatistics",
    description = "Detailed statistics about administrative script execution",
    example = """
        {
          "totalUsersCreated": 10,
          "totalTweetsCreated": 50,
          "totalFollowsCreated": 5,
          "totalTweetsDeleted": 3,
          "usersWithTweets": 10,
          "usersWithoutTweets": 0,
          "totalLikesCreated": 15,
          "totalRetweetsCreated": 12,
          "executionTimeMs": 1234,
          "errors": []
        }
        """
)
public record ScriptStatisticsDto(

    @Schema(
        description = "Total number of successfully created users",
        example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer totalUsersCreated,

    @Schema(
        description = "Total number of successfully created tweets",
        example = "50",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer totalTweetsCreated,

    @Schema(
        description = "Total number of successfully created follow relationships",
        example = "5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer totalFollowsCreated,

    @Schema(
        description = "Total number of successfully deleted tweets",
        example = "3",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer totalTweetsDeleted,

    @Schema(
        description = "Number of users who have at least one tweet",
        example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer usersWithTweets,

    @Schema(
        description = "Number of users who do not have any tweets",
        example = "0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer usersWithoutTweets,

    @Schema(
        description = "Total number of successfully created likes",
        example = "15",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer totalLikesCreated,

    @Schema(
        description = "Total number of successfully created retweets",
        example = "12",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer totalRetweetsCreated,

    @Schema(
        description = "Script execution time in milliseconds",
        example = "1234",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Long executionTimeMs,

    @Schema(
        description = "List of error messages if any errors occurred during execution (empty if no errors)",
        example = "[]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<String> errors
) {
}

