package com.twitter.common.exception.validation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Exception thrown when uniqueness validation fails due to duplicate data.
 * <p>
 * This exception is thrown when attempting to create or update entities with
 * values that already exist in the system for fields that must be unique.
 * It provides detailed information about which field caused the conflict
 * and what value was duplicated, enabling precise error handling and user feedback.
 *
 * <p>The exception provides the following functionality:</p>
 * - Identifies the specific field that caused the uniqueness violation
 * - Provides the duplicate value that triggered the conflict
 * - Supports custom error messages for specific scenarios
 * - Enables cause chaining for debugging complex validation failures
 *
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "UniquenessValidationException",
    description = "Exception thrown when uniqueness validation fails due to duplicate data",
    example = """
        {
          "type": "https://example.com/errors/uniqueness-validation",
          "title": "Uniqueness Validation Error",
          "status": 409,
          "detail": "User with login 'jane_smith' already exists",
          "fieldName": "login",
          "fieldValue": "jane_smith",
          "timestamp": "2025-01-27T16:30:00Z"
        }
        """
)
@Getter
public class UniquenessValidationException extends ValidationException {

    /**
     * The name of the field that caused the uniqueness violation.
     * <p>
     * This field identifies which specific field (e.g., "email", "username")
     * contains the duplicate value. It is used by the GlobalExceptionHandler to
     * provide detailed error information in the ProblemDetail response.
     */
    @Schema(
        description = "The name of the field that caused the uniqueness violation",
        example = "login",
        nullable = true
    )
    private final String fieldName;

    /**
     * The duplicate value that caused the uniqueness violation.
     * <p>
     * This field contains the actual value that was found to be duplicate
     * in the system. It helps with debugging and provides context for
     * the validation failure.
     */
    @Schema(
        description = "The duplicate value that caused the uniqueness violation",
        example = "jane_smith",
        nullable = true
    )
    private final String fieldValue;

    /**
     * Constructs a new uniqueness validation exception with field details.
     * <p>
     * This constructor creates an exception with specific information about
     * which field caused the uniqueness violation and what value was duplicated.
     * It automatically generates a descriptive error message.
     *
     * @param fieldName  the name of the field that caused the conflict
     * @param fieldValue the duplicate value that triggered the conflict
     */
    public UniquenessValidationException(String fieldName, String fieldValue) {
        super(String.format("User with %s '%s' already exists", fieldName, fieldValue));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * Constructs a new uniqueness validation exception with a custom message.
     * <p>
     * This constructor allows specifying a custom error message for specific
     * uniqueness validation scenarios. The fieldName and fieldValue will be
     * set to null, indicating that specific field information is not available.
     *
     * @param message the custom error message describing the uniqueness violation
     */
    public UniquenessValidationException(String message) {
        super(message);
        this.fieldName = null;
        this.fieldValue = null;
    }

    /**
     * Constructs a new uniqueness validation exception with a custom message and cause.
     * <p>
     * This constructor allows wrapping another exception while providing context
     * about the uniqueness validation failure. This is useful when uniqueness
     * validation errors occur as a result of other exceptions (e.g., database
     * constraint violations).
     *
     * @param message the custom error message describing the uniqueness violation
     * @param cause   the underlying cause that led to this exception
     */
    public UniquenessValidationException(String message, Throwable cause) {
        super(message, cause);
        this.fieldName = null;
        this.fieldValue = null;
    }

    /**
     * Returns the validation type for this exception.
     * <p>
     * This method identifies this exception as a uniqueness validation error,
     * enabling the GlobalExceptionHandler to provide appropriate error handling
     * and response formatting.
     *
     * @return ValidationType.UNIQUENESS indicating this is a uniqueness validation error
     */
    @Override
    public ValidationType getValidationType() {
        return ValidationType.UNIQUENESS;
    }
}