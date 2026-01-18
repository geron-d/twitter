package com.twitter.common.exception.validation;

import com.twitter.common.enums.validation.ValidationType;
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
    
    public ValidationException(String message) {
        super(message);
    }
    
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