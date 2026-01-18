package com.twitter.common.enums.user;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumeration of user account statuses in the Twitter system.
 *
 * <p>
 * This enum defines the different states that a user account can be in within
 * the Twitter system. The status determines the user's ability to access
 * system features and perform various operations. Status transitions are
 * controlled by business rules and administrative actions.
 *
 * <p>The available statuses:</p>
 * - <strong>ACTIVE</strong> - User account is active and fully functional
 * - <strong>INACTIVE</strong> - User account is deactivated and restricted
 *
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "UserStatus",
    description = "Enumeration of user account statuses in the Twitter system",
    example = "ACTIVE"
)
public enum UserStatus {

    /**
     * Active user account status indicating full system access.
     *
     * <p>
     * Users with ACTIVE status have complete access to all system features
     * and can perform all operations allowed by their role. This is the
     * default status for newly registered users and represents the normal
     * operational state of a user account.
     */
    ACTIVE,

    /**
     * Inactive user account status indicating restricted system access.
     *
     * <p>
     * Users with INACTIVE status have limited or no access to system features.
     * This status is typically set by administrators for account deactivation
     * or by the system for temporary restrictions. Inactive users cannot
     * perform most operations until their status is changed back to ACTIVE.
     */
    INACTIVE
}
