package com.twitter.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception thrown when attempting to deactivate the last active administrator.
 *
 * <p>
 * This exception prevents the system from being left without any active
 * administrators, which could lead to system lockout scenarios. It is thrown
 * when a user management operation attempts to deactivate an administrator
 * account, but that account is the only remaining active administrator in the system.
 *
 * <p>The exception provides the following functionality:</p>
 * <ul>
 *   <li>Prevents system lockout by blocking last admin deactivation</li>
 *   <li>Returns HTTP 409 Conflict status code</li>
 *   <li>Provides customizable error messages</li>
 *   <li>Supports cause chaining for debugging</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Check if user is the last admin before deactivation
 * if (userService.isLastActiveAdmin(userId)) {
 *     throw new LastAdminDeactivationException(
 *         "Cannot deactivate user " + userId + " - they are the last active administrator"
 *     );
 * }
 *
 * // With cause chaining
 * try {
 *     userService.deactivateUser(userId);
 * } catch (DatabaseException e) {
 *     throw new LastAdminDeactivationException(
 *         "Failed to deactivate last admin due to database error", e
 *     );
 * }
 * }</pre>
 *
 * @author Twitter Team
 * @version 1.0
 * @see GlobalExceptionHandler#handleLastAdminDeactivationException(LastAdminDeactivationException)
 * @see ResponseStatusException for base functionality
 * @since 2025-01-27
 */
public class LastAdminDeactivationException extends ResponseStatusException {

    /**
     * Default error message for last admin deactivation attempts.
     * Used when no specific reason is provided to the constructor.
     */
    private static final String DEFAULT_MESSAGE = "Cannot deactivate the last active administrator";

    /**
     * Constructs a new LastAdminDeactivationException with the default message.
     *
     * <p>
     * This constructor creates an exception with a standard message indicating
     * that the last active administrator cannot be deactivated. It sets the
     * HTTP status to 409 Conflict.
     *
     * <p>Example:</p>
     * <pre>{@code
     * if (isLastAdmin(userId)) {
     *     throw new LastAdminDeactivationException();
     * }
     * }</pre>
     */
    public LastAdminDeactivationException() {
        super(HttpStatus.CONFLICT, DEFAULT_MESSAGE);
    }

    /**
     * Constructs a new LastAdminDeactivationException with a custom reason.
     *
     * <p>
     * This constructor allows specifying a custom error message that provides
     * more context about why the deactivation was blocked. The message should
     * clearly explain the business rule violation.
     *
     * <p>Example:</p>
     * <pre>{@code
     * throw new LastAdminDeactivationException(
     *     "User " + userId + " cannot be deactivated as they are the only remaining administrator"
     * );
     * }</pre>
     *
     * @param reason the detailed reason why the deactivation was blocked
     */
    public LastAdminDeactivationException(String reason) {
        super(HttpStatus.CONFLICT, reason);
    }

    /**
     * Constructs a new LastAdminDeactivationException with a custom reason and cause.
     *
     * <p>
     * This constructor allows specifying both a custom error message and the
     * underlying cause that led to the exception. This is useful for wrapping
     * other exceptions while providing context about the admin deactivation rule.
     *
     * <p>Example:</p>
     * <pre>{@code
     * try {
     *     userService.deactivateUser(userId);
     * } catch (DatabaseException e) {
     *     throw new LastAdminDeactivationException(
     *         "Failed to deactivate last admin due to database error", e
     *     );
     * }
     * }</pre>
     *
     * @param reason the detailed reason why the deactivation was blocked
     * @param cause  the underlying cause that led to this exception
     */
    public LastAdminDeactivationException(String reason, Throwable cause) {
        super(HttpStatus.CONFLICT, reason, cause);
    }
}
