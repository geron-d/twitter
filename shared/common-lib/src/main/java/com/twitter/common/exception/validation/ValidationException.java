package com.twitter.common.exception.validation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Abstract base class for all validation exceptions in the Twitter system.
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
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "ValidationException",
    description = "Abstract base class for all validation exceptions in the Twitter system",
    example = """
        {
          "type": "https://example.com/errors/validation-error",
          "title": "Validation Error",
          "status": 400,
          "detail": "Validation failed: email must be a valid email address",
          "timestamp": "2025-01-27T16:30:00Z"
        }
        """
)
@Getter
public abstract class ValidationException extends RuntimeException {

    /**
     * Constructs a new validation exception with the specified detail message.
     * <p>
     * This constructor creates a validation exception with a custom error message
     * that describes the specific validation failure. The message should be clear
     * and informative to help developers understand what went wrong.
     *
     * @param message the detail message describing the validation error
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new validation exception with the specified detail message and cause.
     * <p>
     * This constructor allows wrapping another exception while providing context
     * about the validation failure. This is useful when validation errors occur
     * as a result of other exceptions (e.g., database errors during uniqueness checks).
     *
     * @param message the detail message describing the validation error
     * @param cause   the cause of the validation error
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns the type of validation that caused this exception.
     * <p>
     * This abstract method must be implemented by all concrete validation exception
     * classes. It provides a way to categorize validation errors and enables
     * the GlobalExceptionHandler to provide appropriate error responses based
     * on the validation type.
     *
     * @return the ValidationType that categorizes this validation error
     */
    public abstract ValidationType getValidationType();
}
