package com.twitter.common.enums;

/**
 * Enumeration of user roles in the Twitter system.
 *
 * <p>
 * This enum defines the different roles that users can have within the Twitter
 * system, each with specific permissions and responsibilities. The role system
 * implements a hierarchical access control model where higher roles inherit
 * permissions from lower roles.
 *
 * <p>The role hierarchy (from lowest to highest):</p>
 * <ul>
 *   <li><strong>USER</strong> - Basic user with standard permissions</li>
 *   <li><strong>MODERATOR</strong> - Content moderation capabilities</li>
 *   <li><strong>ADMIN</strong> - Full system administration access</li>
 * </ul>
 *
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Access control and permission management</li>
 *   <li>Content moderation and user management</li>
 *   <li>System administration and configuration</li>
 *   <li>Business rule validation (e.g., last admin protection)</li>
 * </ul>
 *
 * @author Twitter Team
 * @version 1.0
 * @since 2025-01-27
 */
public enum UserRole {

    /**
     * Administrator role with full system access and management capabilities.
     *
     * <p>
     * Users with the ADMIN role have complete access to all system functionality
     * including user management, system configuration, and administrative
     * operations. This role is typically reserved for system administrators
     * and requires special protection against accidental deactivation.
     *
     * <p>Permissions include:</p>
     * <ul>
     *   <li>All USER and MODERATOR permissions</li>
     *   <li>User account management (create, update, deactivate)</li>
     *   <li>System configuration and settings</li>
     *   <li>Access to administrative dashboards</li>
     *   <li>Role assignment and permission management</li>
     * </ul>
     *
     * <p>Example usage:</p>
     */
    ADMIN,

    /**
     * Moderator role with content moderation and user management capabilities.
     *
     * <p>
     * Users with the MODERATOR role have enhanced permissions for content
     * moderation and user management. They can perform moderation tasks
     * while maintaining some administrative capabilities, but with
     * restrictions compared to full administrators.
     *
     * <p>Permissions include:</p>
     * <ul>
     *   <li>All USER permissions</li>
     *   <li>Content moderation (flag, hide, delete inappropriate content)</li>
     *   <li>User account management (limited scope)</li>
     *   <li>Access to moderation tools and reports</li>
     *   <li>User support and assistance</li>
     * </ul>
     *
     * <p>Example usage:</p>
     */
    MODERATOR,

    /**
     * Standard user role with basic system permissions.
     *
     * <p>
     * Users with the USER role have access to standard Twitter functionality
     * including posting tweets, following other users, and basic profile
     * management. This is the default role assigned to new users.
     *
     * <p>Permissions include:</p>
     * <ul>
     *   <li>Create and manage personal tweets</li>
     *   <li>Follow and unfollow other users</li>
     *   <li>Update personal profile information</li>
     *   <li>Access public content and feeds</li>
     * </ul>
     */
    USER
}
