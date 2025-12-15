package com.twitter.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Data Transfer Object for script execution statistics.
 * <p>
 * This record represents detailed statistics about the execution of an administrative
 * script, including counts of created and deleted entities, execution time, and any
 * errors that occurred during execution.
 *
 * @param totalUsersCreated  total number of successfully created users
 * @param totalTweetsCreated total number of successfully created tweets
 * @param totalTweetsDeleted  total number of successfully deleted tweets
 * @param usersWithTweets     number of users who have tweets
 * @param usersWithoutTweets  number of users who do not have tweets
 * @param executionTimeMs     script execution time in milliseconds
 * @param errors              list of error messages if any errors occurred during execution
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
          "totalTweetsDeleted": 3,
          "usersWithTweets": 10,
          "usersWithoutTweets": 0,
          "executionTimeMs": 1234,
          "errors": []
        }
        """
)
public record ScriptStatisticsDto(

    /**
     * Total number of successfully created users.
     */
    @Schema(
        description = "Total number of successfully created users",
        example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer totalUsersCreated,

    /**
     * Total number of successfully created tweets.
     */
    @Schema(
        description = "Total number of successfully created tweets",
        example = "50",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer totalTweetsCreated,

    /**
     * Total number of successfully deleted tweets.
     */
    @Schema(
        description = "Total number of successfully deleted tweets",
        example = "3",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer totalTweetsDeleted,

    /**
     * Number of users who have at least one tweet.
     */
    @Schema(
        description = "Number of users who have at least one tweet",
        example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer usersWithTweets,

    /**
     * Number of users who do not have any tweets.
     */
    @Schema(
        description = "Number of users who do not have any tweets",
        example = "0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer usersWithoutTweets,

    /**
     * Script execution time in milliseconds.
     */
    @Schema(
        description = "Script execution time in milliseconds",
        example = "1234",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Long executionTimeMs,

    /**
     * List of error messages if any errors occurred during execution.
     * <p>
     * This list contains error messages for partial failures (e.g., when some
     * users or tweets failed to be created). If the list is empty, it means
     * all operations completed successfully.
     */
    @Schema(
        description = "List of error messages if any errors occurred during execution (empty if no errors)",
        example = "[]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<String> errors
) {
}
