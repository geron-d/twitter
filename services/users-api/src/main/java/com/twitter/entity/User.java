package com.twitter.entity;

import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User entity representing a user account in the Twitter system.
 * <p>
 * This entity maps to the 'users' table in the database and contains all
 * user-related data including authentication credentials, personal information,
 * and system status.
 *
 * @author geron
 * @version 1.0
 */
@Entity
@Table(name = "users")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Unique identifier for the user.
     * <p>
     * This field serves as the primary key and is automatically generated
     * using UUID. It cannot be updated after creation and is required.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Unique login name for user authentication.
     * <p>
     * This field must be unique across all users and is required for
     * authentication purposes. It serves as the username for login.
     */
    @Column(name = "login", unique = true, nullable = false)
    private String login;

    /**
     * User's first name.
     * <p>
     * This field stores the user's given name and is optional.
     * Used for personalization and display purposes.
     */
    @Column(name = "first_name")
    private String firstName;

    /**
     * User's last name.
     * <p>
     * This field stores the user's family name and is optional.
     * Used for personalization and display purposes.
     */
    @Column(name = "last_name")
    private String lastName;

    /**
     * Unique email address for the user.
     * <p>
     * This field must be unique across all users and is required.
     * Used for communication and account recovery purposes.
     */
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    /**
     * Hashed password for user authentication.
     * <p>
     * This field stores the PBKDF2 hashed password and is required.
     * The password is never stored in plain text for security reasons.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Salt used for password hashing.
     * <p>
     * This field stores the Base64-encoded salt used during password
     * hashing with PBKDF2 algorithm. It is required for password verification.
     */
    @Column(name = "password_salt", nullable = false)
    private String passwordSalt;

    /**
     * Current status of the user account.
     * <p>
     * This field indicates whether the user account is active or inactive.
     * It is required and affects the user's ability to access the system.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    /**
     * Role assigned to the user.
     * <p>
     * This field determines the user's permissions and access level
     * within the system. It is required and affects system functionality.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    /**
     * Date and time when the user account was created.
     * <p>
     * This field is automatically set when the user is first saved to the database
     * and cannot be updated afterwards. It provides audit information about
     * when the user account was originally created in the system.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
