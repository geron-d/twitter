package com.twitter.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

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
 * - <strong>USER</strong> - Basic user with standard permissions
 * - <strong>MODERATOR</strong> - Content moderation capabilities
 * - <strong>ADMIN</strong> - Full system administration access
 *
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "UserRole",
    description = "Enumeration of user roles in the Twitter system with hierarchical access control",
    example = "USER"
)
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
     * - All USER and MODERATOR permissions
     * - User account management (create, update, deactivate)
     * - System configuration and settings
     * - Access to administrative dashboards
     * - Role assignment and permission management
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
     * - All USER permissions
     * - Content moderation (flag, hide, delete inappropriate content)
     * - User account management (limited scope)
     * - Access to moderation tools and reports
     * - User support and assistance
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
     * - Create and manage personal tweets
     * - Follow and unfollow other users
     * - Update personal profile information
     * - Access public content and feeds
     */
    USER
}