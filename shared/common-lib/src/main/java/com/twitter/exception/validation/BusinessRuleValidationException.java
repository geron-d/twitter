package com.twitter.exception.validation;

import com.twitter.common.exception.GlobalExceptionHandler;
import com.twitter.enums.UserRole;
import lombok.Getter;

/**
 * Exception thrown when business rule validation fails due to domain-specific constraint violations.
 * 
 * <p>
 * This exception is thrown when business logic rules that are specific to the Twitter
 * domain are violated. These rules go beyond technical validation and implement
 * complex business constraints that ensure the system operates according to
 * business requirements and maintains data integrity.
 * 
 * <p>The exception provides the following functionality:</p>
 * <ul>
 *   <li>Identifies the specific business rule that was violated</li>
 *   <li>Provides context about the circumstances of the violation</li>
 *   <li>Supports custom error messages for specific scenarios</li>
 *   <li>Provides factory methods for common business rule violations</li>
 *   <li>Enables cause chaining for debugging complex validation failures</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Preventing deactivation of the last active administrator</li>
 *   <li>Validating user account status transitions</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * // Last admin deactivation check
 * if (isLastActiveAdmin(userId)) {
 *     throw BusinessRuleValidationException.lastAdminDeactivation(userId);
 * }
 * 
 * // Last admin role change check
 * if (isLastActiveAdmin(userId) && newRole != UserRole.ADMIN) {
 *     throw BusinessRuleValidationException.lastAdminRoleChange(userId, newRole);
 * }
 * 
 * // Custom business rule validation
 * if (violatesCustomBusinessRule(entity)) {
 *     throw new BusinessRuleValidationException(
 *         "CUSTOM_RULE", "Entity violates custom business constraint"
 *     );
 * }
 * }</pre>
 * 
 * @author Twitter Team
 * @version 1.0
 * @since 2025-01-27
 * @see ValidationException for the base validation exception class
 * @see ValidationType#BUSINESS_RULE for the validation type
 * @see GlobalExceptionHandler#handleBusinessRuleValidationException(BusinessRuleValidationException)
 */
@Getter
public class BusinessRuleValidationException extends ValidationException {
    
    /**
     * The name of the business rule that was violated.
     * 
     * <p>
     * This field identifies which specific business rule (e.g., "LAST_ADMIN_DEACTIVATION",
     * "ROLE_CHANGE_RESTRICTION") was violated. It is used by the GlobalExceptionHandler
     * to provide detailed error information in the ProblemDetail response.
     */
    private final String ruleName;
    
    /**
     * The context in which the business rule violation occurred.
     * 
     * <p>
     * This field provides additional context about the circumstances of the
     * violation (e.g., user ID, entity state, operation details). It helps
     * with debugging and provides context for the validation failure.
     */
    private final Object context;
    
    /**
     * Constructs a new business rule validation exception with rule and context details.
     * 
     * <p>
     * This constructor creates an exception with specific information about
     * which business rule was violated and the context in which it occurred.
     * It automatically generates a descriptive error message.
     * 
     * <p>Example:</p>
     * <pre>{@code
     * if (violatesBusinessRule(entity)) {
     *     throw new BusinessRuleValidationException(
     *         "CUSTOM_RULE", "Entity violates custom business constraint"
     *     );
     * }
     * }</pre>
     * 
     * @param ruleName the name of the business rule that was violated
     * @param context  the context in which the violation occurred
     */
    public BusinessRuleValidationException(String ruleName, Object context) {
        super(String.format("Business rule '%s' violated for context: %s", ruleName, context));
        this.ruleName = ruleName;
        this.context = context;
    }
    
    /**
     * Constructs a new business rule validation exception with a custom message.
     * 
     * <p>
     * This constructor allows specifying a custom error message for specific
     * business rule validation scenarios. The ruleName and context will be
     * set to null, indicating that specific rule information is not available.
     * 
     * <p>Example:</p>
     * <pre>{@code
     * throw new BusinessRuleValidationException(
     *     "The requested operation violates system business rules"
     * );
     * }</pre>
     * 
     * @param message the custom error message describing the business rule violation
     */
    public BusinessRuleValidationException(String message) {
        super(message);
        this.ruleName = null;
        this.context = null;
    }
    
    /**
     * Constructs a new business rule validation exception with a custom message and cause.
     * 
     * <p>
     * This constructor allows wrapping another exception while providing context
     * about the business rule validation failure. This is useful when business rule
     * validation errors occur as a result of other exceptions.
     * 
     * <p>Example:</p>
     * <pre>{@code
     * try {
     *     performBusinessOperation(entity);
     * } catch (DatabaseException e) {
     *     throw new BusinessRuleValidationException(
     *         "Failed to validate business rule due to database error", e
     *     );
     * }
     * }</pre>
     * 
     * @param message the custom error message describing the business rule violation
     * @param cause   the underlying cause that led to this exception
     */
    public BusinessRuleValidationException(String message, Throwable cause) {
        super(message, cause);
        this.ruleName = null;
        this.context = null;
    }
    
    /**
     * Returns the validation type for this exception.
     * 
     * <p>
     * This method identifies this exception as a business rule validation error,
     * enabling the GlobalExceptionHandler to provide appropriate error handling
     * and response formatting.
     * 
     * @return ValidationType.BUSINESS_RULE indicating this is a business rule validation error
     */
    @Override
    public ValidationType getValidationType() {
        return ValidationType.BUSINESS_RULE;
    }
    
    /**
     * Factory method for creating last admin deactivation error exceptions.
     * 
     * <p>
     * This factory method creates a BusinessRuleValidationException specifically
     * for attempts to deactivate the last active administrator in the system.
     * This prevents the system from being left without any administrative access.
     * 
     * <p>Example:</p>
     * <pre>{@code
     * if (isLastActiveAdmin(userId)) {
     *     throw BusinessRuleValidationException.lastAdminDeactivation(userId);
     * }
     * }</pre>
     * 
     * @param userId the ID of the administrator being deactivated
     * @return a BusinessRuleValidationException with appropriate error message
     */
    public static BusinessRuleValidationException lastAdminDeactivation(Object userId) {
        return new BusinessRuleValidationException("LAST_ADMIN_DEACTIVATION", userId);
    }
    
    /**
     * Factory method for creating last admin role change error exceptions.
     * 
     * <p>
     * This factory method creates a BusinessRuleValidationException specifically
     * for attempts to change the role of the last active administrator to a
     * non-admin role. This prevents the system from being left without any
     * administrative access.
     * 
     * <p>Example:</p>
     * <pre>{@code
     * if (isLastActiveAdmin(userId) && newRole != UserRole.ADMIN) {
     *     throw BusinessRuleValidationException.lastAdminRoleChange(userId, newRole);
     * }
     * }</pre>
     * 
     * @param userId  the ID of the administrator whose role is being changed
     * @param newRole the new role being assigned
     * @return a BusinessRuleValidationException with appropriate error message
     */
    public static BusinessRuleValidationException lastAdminRoleChange(Object userId, UserRole newRole) {
        return new BusinessRuleValidationException("LAST_ADMIN_ROLE_CHANGE", 
            String.format("userId=%s, newRole=%s", userId, newRole));
    }
}
