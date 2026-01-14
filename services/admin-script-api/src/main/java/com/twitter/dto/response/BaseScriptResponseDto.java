package com.twitter.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for base administrative script response with execution results.
 * <p>
 * This record represents the data structure returned after executing the base administrative
 * script. It contains lists of created and deleted entity IDs, as well as detailed
 * statistics about the script execution.
 *
 * @param createdUsers   list of IDs of successfully created users
 * @param createdFollows list of IDs of successfully created follow relationships
 * @param createdTweets  list of IDs of successfully created tweets
 * @param deletedTweets  list of IDs of successfully deleted tweets
 * @param statistics      detailed statistics about script execution
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "BaseScriptResponse",
    description = "Response data structure for base administrative script execution results",
    example = """
        {
          "createdUsers": [
            "123e4567-e89b-12d3-a456-426614174000",
            "223e4567-e89b-12d3-a456-426614174001"
          ],
          "createdFollows": [
            "456e7890-e89b-12d3-a456-426614174111",
            "567e8901-e89b-12d3-a456-426614174222"
          ],
          "createdTweets": [
            "323e4567-e89b-12d3-a456-426614174002",
            "423e4567-e89b-12d3-a456-426614174003"
          ],
          "deletedTweets": [
            "523e4567-e89b-12d3-a456-426614174004"
          ],
          "statistics": {
            "totalUsersCreated": 10,
            "totalTweetsCreated": 50,
            "totalTweetsDeleted": 3,
            "usersWithTweets": 10,
            "usersWithoutTweets": 0,
            "executionTimeMs": 1234,
            "errors": []
          }
        }
        """
)
@Builder
public record BaseScriptResponseDto(

    /**
     * List of IDs of successfully created users.
     * <p>
     * This list contains the UUIDs of all users that were successfully created
     * during script execution.
     */
    @Schema(
        description = "List of IDs of successfully created users",
        example = "[\"123e4567-e89b-12d3-a456-426614174000\", \"223e4567-e89b-12d3-a456-426614174001\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<UUID> createdUsers,

    /**
     * List of IDs of successfully created follow relationships.
     * <p>
     * This list contains the UUIDs of all follow relationships that were successfully created
     * during script execution (Step 1.5). The list will be empty if fewer than
     * 2 users were created, or if all follow relationship creation attempts failed.
     */
    @Schema(
        description = "List of IDs of successfully created follow relationships",
        example = "[\"456e7890-e89b-12d3-a456-426614174111\", \"567e8901-e89b-12d3-a456-426614174222\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<UUID> createdFollows,

    /**
     * List of IDs of successfully created tweets.
     * <p>
     * This list contains the UUIDs of all tweets that were successfully created
     * during script execution.
     */
    @Schema(
        description = "List of IDs of successfully created tweets",
        example = "[\"323e4567-e89b-12d3-a456-426614174002\", \"423e4567-e89b-12d3-a456-426614174003\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<UUID> createdTweets,

    /**
     * List of IDs of successfully deleted tweets.
     * <p>
     * This list contains the UUIDs of all tweets that were successfully deleted
     * during script execution.
     */
    @Schema(
        description = "List of IDs of successfully deleted tweets",
        example = "[\"523e4567-e89b-12d3-a456-426614174004\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<UUID> deletedTweets,

    /**
     * Detailed statistics about script execution.
     * <p>
     * This field contains comprehensive statistics including counts of created
     * and deleted entities, execution time, and any errors that occurred.
     */
    @Schema(
        description = "Detailed statistics about script execution",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    ScriptStatisticsDto statistics
) {
}
