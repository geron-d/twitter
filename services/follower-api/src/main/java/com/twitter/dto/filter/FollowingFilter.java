package com.twitter.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Filter DTO for following search and filtering operations.
 * <p>
 * This record provides a convenient way to specify filtering criteria for
 * following queries. It supports partial matching for login field. The filter
 * is used to filter following by login name when retrieving following lists.
 * <p>
 * Note: Filtering by login is performed at the service layer after retrieving
 * user data from users-api, as login information is not stored in the follows table.
 *
 * @param login partial match for following login name (can be null)
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "FollowingFilter",
    description = "Filter criteria for following search and filtering operations",
    example = """
        {
          "login": "jane"
        }
        """
)
public record FollowingFilter(
    @Schema(
        description = "Partial match filter for following login name (case-insensitive)",
        example = "jane",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String login
) {
}
