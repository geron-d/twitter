package com.twitter.common.enums;

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
 * <ul>
 *   <li><strong>ACTIVE</strong> - User account is active and fully functional</li>
 *   <li><strong>INACTIVE</strong> - User account is deactivated and restricted</li>
 * </ul>
 *
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Account activation and deactivation management</li>
 *   <li>Access control based on account status</li>
 *   <li>User account lifecycle management</li>
 *   <li>Business rule validation for status transitions</li>
 * </ul>
 *
 * @author geron
 * @version 1.0
 */
public enum UserStatus {

    /**
     * Active user account status indicating full system access.
     *
     * <p>
     * Users with ACTIVE status have complete access to all system features
     * and can perform all operations allowed by their role. This is the
     * default status for newly registered users and represents the normal
     * operational state of a user account.
     *
     * <p>Capabilities include:</p>
     * <ul>
     *   <li>Full access to all system features</li>
     *   <li>Ability to create and manage content</li>
     *   <li>Participation in social interactions</li>
     *   <li>Access to user profile and settings</li>
     * </ul>
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
     *
     * <p>Restrictions include:</p>
     * <ul>
     *   <li>Limited or no access to system features</li>
     *   <li>Cannot create or modify content</li>
     *   <li>Cannot participate in social interactions</li>
     *   <li>May have limited profile access</li>
     * </ul>
     */
    INACTIVE
}
