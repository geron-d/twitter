package com.twitter.common.exception.validation;

import com.twitter.common.enums.validation.ValidationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Exception thrown when format validation fails due to data structure or syntax errors.
 * <p>
 * This exception is thrown when data does not conform to expected formats,
 * syntax rules, or technical constraints. It handles various types of format
 * validation failures including Bean Validation constraint violations and
 * JSON parsing errors.
 *
 * <p>The exception provides the following functionality:</p>
 * - Identifies the specific field that failed format validation
 * - Specifies the constraint that was violated
 * - Supports custom error messages for specific scenarios
 * - Provides factory methods for common validation errors
 * - Enables cause chaining for debugging complex validation failures
 *
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "FormatValidationException",
    description = "Exception thrown when format validation fails due to data structure or syntax errors",
    example = """
        {
          "type": "https://example.com/errors/format-validation",
          "title": "Format Validation Error",
          "status": 400,
          "detail": "Invalid email format: invalid-email",
          "fieldName": "email",
          "constraintName": "EMAIL_FORMAT",
          "timestamp": "2025-01-27T16:30:00Z"
        }
        """
)
@Getter
public class FormatValidationException extends ValidationException {

    @Schema(
        description = "The name of the field that failed format validation",
        example = "email",
        nullable = true
    )
    private final String fieldName;

    @Schema(
        description = "The name of the constraint that was violated",
        example = "EMAIL_FORMAT",
        nullable = true
    )
    private final String constraintName;

    public FormatValidationException(String fieldName, String constraintName, String message) {
        super(message);
        this.fieldName = fieldName;
        this.constraintName = constraintName;
    }

    public FormatValidationException(String message) {
        super(message);
        this.fieldName = null;
        this.constraintName = null;
    }

    public FormatValidationException(String message, Throwable cause) {
        super(message, cause);
        this.fieldName = null;
        this.constraintName = null;
    }

    /**
     * @see ValidationException#getValidationType
     */
    @Override
    public ValidationType getValidationType() {
        return ValidationType.FORMAT;
    }

    /**
     * Factory method for creating JSON parsing error exceptions.
     * <p>
     * This factory method creates a FormatValidationException specifically
     * for JSON parsing errors. It wraps the original parsing exception and
     * provides a descriptive error message.
     *
     * @param cause the exception that caused the JSON parsing failure
     * @return a FormatValidationException with appropriate error message
     */
    public static FormatValidationException jsonParsingError(Throwable cause) {
        return new FormatValidationException("Error parsing JSON patch data: " + cause.getMessage(), cause);
    }

    /**
     * Factory method for creating Bean Validation error exceptions.
     * <p>
     * This factory method creates a FormatValidationException specifically
     * for Bean Validation constraint violations. It provides detailed
     * information about the field and constraint that failed validation.
     *
     * @param fieldName      the name of the field that failed validation
     * @param constraintName the name of the constraint that was violated
     * @param message        the error message describing the validation failure
     * @return a FormatValidationException with field and constraint details
     */
    public static FormatValidationException beanValidationError(String fieldName, String constraintName, String message) {
        return new FormatValidationException(fieldName, constraintName, message);
    }
}