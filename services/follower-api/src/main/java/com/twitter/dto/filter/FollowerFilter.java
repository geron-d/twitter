package com.twitter.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Filter DTO for follower search and filtering operations.
 * <p>
 * This record provides a convenient way to specify filtering criteria for
 * follower queries. It supports partial matching for login field. The filter
 * is used to filter followers by login name when retrieving follower lists.
 * <p>
 * Note: Filtering by login is performed at the service layer after retrieving
 * user data from users-api, as login information is not stored in the follows table.
 *
 * @param login partial match for follower login name (can be null)
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "FollowerFilter",
    description = "Filter criteria for follower search and filtering operations",
    example = """
        {
          "login": "john"
        }
        """
)
public record FollowerFilter(
    @Schema(
        description = "Partial match filter for follower login name (case-insensitive)",
        example = "john",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String login
) {
}

