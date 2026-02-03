package com.twitter.common.exception.validation;

import com.twitter.common.enums.validation.ValidationType;
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

    @Schema(
        description = "The name of the field that caused the uniqueness violation",
        example = "login",
        nullable = true
    )
    private final String fieldName;

    @Schema(
        description = "The duplicate value that caused the uniqueness violation",
        example = "jane_smith",
        nullable = true
    )
    private final String fieldValue;

    public UniquenessValidationException(String fieldName, String fieldValue) {
        super(String.format("User with %s '%s' already exists", fieldName, fieldValue));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public UniquenessValidationException(String message) {
        super(message);
        this.fieldName = null;
        this.fieldValue = null;
    }

    public UniquenessValidationException(String message, Throwable cause) {
        super(message, cause);
        this.fieldName = null;
        this.fieldValue = null;
    }

    /**
     * @see ValidationException#getValidationType
     */
    @Override
    public ValidationType getValidationType() {
        return ValidationType.UNIQUENESS;
    }
}