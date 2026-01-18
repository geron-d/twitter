package com.twitter.common.dto.request.follow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/**
 * Data Transfer Object for creating a new follow relationship.
 *
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "FollowRequest",
    description = "Data structure for creating new follow relationships between users",
    example = """
        {
          "followerId": "123e4567-e89b-12d3-a456-426614174000",
          "followingId": "987fcdeb-51a2-43d7-b123-426614174999"
        }
        """
)
@Builder
public record FollowRequestDto(
    @Schema(
        description = "ID of the user who is following (the follower)",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Follower ID cannot be null")
    UUID followerId,

    @Schema(
        description = "ID of the user being followed (the following)",
        example = "987fcdeb-51a2-43d7-b123-426614174999",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Following ID cannot be null")
    UUID followingId
) {
}
