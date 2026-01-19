package com.twitter.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Response DTO for follow statistics.
 *
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "FollowStatsResponse",
    description = "Follow statistics information returned by the API",
    example = """
        {
          "followersCount": 150,
          "followingCount": 75
        }
        """
)
@Builder
public record FollowStatsResponseDto(
    @Schema(
        description = "Total number of users following this user",
        example = "150",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    long followersCount,

    @Schema(
        description = "Total number of users this user is following",
        example = "75",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    long followingCount
) {
}
