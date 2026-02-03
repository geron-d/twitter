package com.twitter.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for base administrative script response with execution results.
 *
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

    @Schema(
        description = "List of IDs of successfully created users",
        example = "[\"123e4567-e89b-12d3-a456-426614174000\", \"223e4567-e89b-12d3-a456-426614174001\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<UUID> createdUsers,

    @Schema(
        description = "List of IDs of successfully created follow relationships",
        example = "[\"456e7890-e89b-12d3-a456-426614174111\", \"567e8901-e89b-12d3-a456-426614174222\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<UUID> createdFollows,

    @Schema(
        description = "List of IDs of successfully created tweets",
        example = "[\"323e4567-e89b-12d3-a456-426614174002\", \"423e4567-e89b-12d3-a456-426614174003\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<UUID> createdTweets,

    @Schema(
        description = "List of IDs of successfully deleted tweets",
        example = "[\"523e4567-e89b-12d3-a456-426614174004\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<UUID> deletedTweets,

    @Schema(
        description = "Detailed statistics about script execution",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    ScriptStatisticsDto statistics
) {
}
