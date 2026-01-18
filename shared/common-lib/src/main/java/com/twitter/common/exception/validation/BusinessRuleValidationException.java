package com.twitter.common.exception.validation;

import com.twitter.common.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Exception thrown when business rule validation fails due to domain-specific constraint violations.
 * <p>
 * This exception is thrown when business logic rules that are specific to the Twitter
 * domain are violated. These rules go beyond technical validation and implement
 * complex business constraints that ensure the system operates according to
 * business requirements and maintains data integrity.
 *
 * <p>The exception provides the following functionality:</p>
 * - Identifies the specific business rule that was violated
 * - Provides context about the circumstances of the violation
 * - Supports custom error messages for specific scenarios
 * - Provides factory methods for common business rule violations
 * - Enables cause chaining for debugging complex validation failures
 *
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "BusinessRuleValidationException",
    description = "Exception thrown when business rule validation fails due to domain-specific constraint violations",
    example = """
        {
          "type": "https://example.com/errors/business-rule-validation",
          "title": "Business Rule Validation Error",
          "status": 400,
          "detail": "Business rule 'LAST_ADMIN_DEACTIVATION' violated for context: userId=123e4567-e89b-12d3-a456-426614174000",
          "ruleName": "LAST_ADMIN_DEACTIVATION",
          "timestamp": "2025-01-27T16:30:00Z"
        }
        """
)
@Getter
public class BusinessRuleValidationException extends ValidationException {

    /**
     * The name of the business rule that was violated.
     * <p>
     * This field identifies which specific business rule (e.g., "LAST_ADMIN_DEACTIVATION",
     * "ROLE_CHANGE_RESTRICTION") was violated. It is used by the GlobalExceptionHandler
     * to provide detailed error information in the ProblemDetail response.
     */
    @Schema(
        description = "The name of the business rule that was violated",
        example = "LAST_ADMIN_DEACTIVATION",
        nullable = true
    )
    private final String ruleName;

    /**
     * The context in which the business rule violation occurred.
     * <p>
     * This field provides additional context about the circumstances of the
     * violation (e.g., user ID, entity state, operation details). It helps
     * with debugging and provides context for the validation failure.
     */
    @Schema(
        description = "The context in which the business rule violation occurred",
        example = "userId=123e4567-e89b-12d3-a456-426614174000",
        nullable = true
    )
    private final Object context;

    /**
     * Constructs a new business rule validation exception with rule and context details.
     * <p>
     * This constructor creates an exception with specific information about
     * which business rule was violated and the context in which it occurred.
     * It automatically generates a descriptive error message.
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
     * <p>
     * This constructor allows specifying a custom error message for specific
     * business rule validation scenarios. The ruleName and context will be
     * set to null, indicating that specific rule information is not available.
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
     * <p>
     * This constructor allows wrapping another exception while providing context
     * about the business rule validation failure. This is useful when business rule
     * validation errors occur as a result of other exceptions.
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
     * <p>
     * This factory method creates a BusinessRuleValidationException specifically
     * for attempts to deactivate the last active administrator in the system.
     * This prevents the system from being left without any administrative access.
     *
     * @param userId the ID of the administrator being deactivated
     * @return a BusinessRuleValidationException with appropriate error message
     */
    public static BusinessRuleValidationException lastAdminDeactivation(Object userId) {
        return new BusinessRuleValidationException("LAST_ADMIN_DEACTIVATION", userId);
    }

    /**
     * Factory method for creating last admin role change error exceptions.
     * <p>
     * This factory method creates a BusinessRuleValidationException specifically
     * for attempts to change the role of the last active administrator to a
     * non-admin role. This prevents the system from being left without any
     * administrative access.
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