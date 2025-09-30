package com.twitter.common.exception.validation;

import lombok.Getter;

/**
 * Abstract base class for all validation exceptions in the Twitter system.
 *
 * <p>
 * This abstract class provides a common structure and interface for all validation
 * errors in the Twitter microservices architecture. It establishes a unified
 * exception hierarchy that allows for consistent error handling and categorization
 * of different types of validation failures.
 *
 * <p>The validation architecture follows these principles:</p>
 * <ul>
 *   <li><strong>Type Safety</strong> - Each validation exception has a specific type</li>
 *   <li><strong>Consistency</strong> - All validation errors follow the same structure</li>
 *   <li><strong>Extensibility</strong> - New validation types can be easily added</li>
 *   <li><strong>Centralized Handling</strong> - GlobalExceptionHandler processes all types</li>
 * </ul>
 *
 * <p>The validation exception hierarchy:</p>
 * <pre>
 * ValidationException (abstract)
 * ├── UniquenessValidationException
 * ├── BusinessRuleValidationException
 * └── FormatValidationException
 * </pre>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Custom validation exception
 * public class CustomValidationException extends ValidationException {
 *     public CustomValidationException(String message) {
 *         super(message);
 *     }
 *
 *     @Override
 *     public ValidationType getValidationType() {
 *         return ValidationType.CUSTOM;
 *     }
 * }
 *
 * // Throwing validation exceptions
 * if (isDuplicateEmail(email)) {
 *     throw new UniquenessValidationException("email", email, "Email already exists");
 * }
 *
 * if (violatesBusinessRule(user)) {
 *     throw new BusinessRuleValidationException("LAST_ADMIN", "Cannot deactivate last admin");
 * }
 * }</pre>
 *
 * @author Twitter Team
 * @version 1.0
 * @see ValidationType for validation type enumeration
 * @see UniquenessValidationException for uniqueness validation errors
 * @see BusinessRuleValidationException for business rule violations
 * @see FormatValidationException for data format errors
 * @since 2025-01-27
 */
@Getter
public abstract class ValidationException extends RuntimeException {

    /**
     * Constructs a new validation exception with the specified detail message.
     *
     * <p>
     * This constructor creates a validation exception with a custom error message
     * that describes the specific validation failure. The message should be clear
     * and informative to help developers understand what went wrong.
     *
     * <p>Example:</p>
     * <pre>{@code
     * throw new UniquenessValidationException("email", email, "Email address already exists");
     * }</pre>
     *
     * @param message the detail message describing the validation error
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new validation exception with the specified detail message and cause.
     *
     * <p>
     * This constructor allows wrapping another exception while providing context
     * about the validation failure. This is useful when validation errors occur
     * as a result of other exceptions (e.g., database errors during uniqueness checks).
     *
     * <p>Example:</p>
     * <pre>{@code
     * try {
     *     userRepository.findByEmail(email);
     * } catch (DatabaseException e) {
     *     throw new UniquenessValidationException(
     *         "email", email, "Failed to check email uniqueness", e
     *     );
     * }
     * }</pre>
     *
     * @param message the detail message describing the validation error
     * @param cause   the cause of the validation error
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns the type of validation that caused this exception.
     *
     * <p>
     * This abstract method must be implemented by all concrete validation exception
     * classes. It provides a way to categorize validation errors and enables
     * the GlobalExceptionHandler to provide appropriate error responses based
     * on the validation type.
     *
     * <p>The validation types include:</p>
     * <ul>
     *   <li>{@code UNIQUENESS} - Duplicate data validation</li>
     *   <li>{@code BUSINESS_RULE} - Business logic validation</li>
     *   <li>{@code FORMAT} - Data format validation</li>
     * </ul>
     *
     * <p>Example implementation:</p>
     * <pre>{@code
     * @Override
     * public ValidationType getValidationType() {
     *     return ValidationType.UNIQUENESS;
     * }
     * }</pre>
     *
     * @return the ValidationType that categorizes this validation error
     * @see ValidationType for available validation types
     */
    public abstract ValidationType getValidationType();
}
