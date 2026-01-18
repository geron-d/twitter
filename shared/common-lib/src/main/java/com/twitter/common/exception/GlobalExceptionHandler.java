package com.twitter.common.exception;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.common.exception.validation.ValidationException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;

/**
 * Global exception handler for centralized error processing in Twitter microservices.
 * <p>
 * This handler provides unified error response formatting using Spring's ProblemDetail
 * and handles various types of validation and business rule exceptions. It ensures
 * consistent error responses across all Twitter services by converting exceptions
 * into standardized HTTP error responses with detailed problem information.
 *
 * <p>The handler processes the following exception types:</p>
 * - ResponseStatusException - HTTP status exceptions
 * - RuntimeException - General runtime errors
 * - ConstraintViolationException - Bean validation errors
 * - MethodArgumentTypeMismatchException - Invalid path variable format (e.g., invalid UUID)
 * - UniquenessValidationException - Duplicate data errors
 * - BusinessRuleValidationException - Business logic violations
 * - FormatValidationException - Data format errors
 * - ValidationException - General validation errors
 *
 * <p>All responses follow RFC 7807 Problem Details for HTTP APIs standard.</p>
 *
 * @author geron
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Default constructor for GlobalExceptionHandler.
     * <p>
     * This constructor creates a new instance of GlobalExceptionHandler.
     * The handler is automatically registered with Spring's exception handling
     * mechanism through the @RestControllerAdvice annotation.
     * </p>
     */
    public GlobalExceptionHandler() {
        // Default constructor - Spring will handle initialization
    }

    /**
     * Handles Spring's ResponseStatusException and converts it to ProblemDetail.
     *
     * <p>Response format:</p>
     * <pre>
     * {
     *   "type": "https://example.com/errors/request-error",
     *   "title": "Bad Request",
     *   "status": 400,
     *   "detail": "Invalid request parameters",
     *   "timestamp": "2025-01-27T15:30:00Z"
     * }
     * </pre>
     *
     * @param ex the ResponseStatusException that was thrown
     * @return ProblemDetail containing standardized error information
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String reason = ex.getReason();
        String detail = reason != null ? reason : "Request failed";

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            ex.getStatusCode(),
            detail
        );
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setType(URI.create("https://example.com/errors/request-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles general RuntimeException instances and converts them to ProblemDetail.
     *
     * <p>Response format:</p>
     * <pre>
     * {
     *   "type": "https://example.com/errors/internal-error",
     *   "title": "Internal Server Error",
     *   "status": 500,
     *   "detail": "Unexpected server error",
     *   "timestamp": "2025-01-27T15:30:00Z"
     * }
     * </pre>
     *
     * @param ex the RuntimeException that was thrown
     * @return ProblemDetail containing standardized error information
     */
    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getMessage() != null ? ex.getMessage() : "Unexpected server error"
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://example.com/errors/internal-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles Bean Validation constraint violations and converts them to ProblemDetail.
     *
     * <p>Response format:</p>
     * <pre>
     * {
     *   "type": "https://example.com/errors/validation-error",
     *   "title": "Validation Error",
     *   "status": 400,
     *   "detail": "Validation failed: email must be a valid email address",
     *   "timestamp": "2025-01-27T15:30:00Z"
     * }
     * </pre>
     *
     * @param ex the ConstraintViolationException that was thrown
     * @return ProblemDetail containing validation error information
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleValidationException(ConstraintViolationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Validation failed: " + ex.getMessage()
        );
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("https://example.com/errors/validation-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles uniqueness validation exceptions and converts them to ProblemDetail.
     *
     * <p>Response format:</p>
     * <pre>
     * {
     *   "type": "https://example.com/errors/uniqueness-validation",
     *   "title": "Uniqueness Validation Error",
     *   "status": 409,
     *   "detail": "Email address already exists",
     *   "timestamp": "2025-01-27T15:30:00Z",
     *   "fieldName": "email",
     *   "fieldValue": "john@example.com"
     * }
     * </pre>
     *
     * @param ex the UniquenessValidationException that was thrown
     * @return ProblemDetail containing uniqueness validation error information
     */
    @ExceptionHandler(UniquenessValidationException.class)
    public ProblemDetail handleUniquenessValidationException(UniquenessValidationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Uniqueness Validation Error");
        problemDetail.setType(URI.create("https://example.com/errors/uniqueness-validation"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("fieldName", ex.getFieldName());
        problemDetail.setProperty("fieldValue", ex.getFieldValue());
        return problemDetail;
    }

    /**
     * Handles business rule validation exceptions and converts them to ProblemDetail.
     *
     * <p>Response format:</p>
     * <pre>
     * {
     *   "type": "https://example.com/errors/business-rule-validation",
     *   "title": "Business Rule Validation Error",
     *   "status": 409,
     *   "detail": "Cannot deactivate the last active administrator",
     *   "timestamp": "2025-01-27T15:30:00Z",
     *   "ruleName": "LAST_ADMIN_PROTECTION",
     *   "context": "User ID: 123, Role: ADMIN"
     * }
     * </pre>
     *
     * @param ex the BusinessRuleValidationException that was thrown
     * @return ProblemDetail containing business rule validation error information
     */
    @ExceptionHandler(BusinessRuleValidationException.class)
    public ProblemDetail handleBusinessRuleValidationException(BusinessRuleValidationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Business Rule Validation Error");
        problemDetail.setType(URI.create("https://example.com/errors/business-rule-validation"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("ruleName", ex.getRuleName());
        problemDetail.setProperty("context", ex.getContext());
        return problemDetail;
    }

    /**
     * Handles format validation exceptions and converts them to ProblemDetail.
     *
     * <p>Response format:</p>
     * <pre>
     * {
     *   "type": "https://example.com/errors/format-validation",
     *   "title": "Format Validation Error",
     *   "status": 400,
     *   "detail": "Invalid email format",
     *   "timestamp": "2025-01-27T15:30:00Z",
     *   "fieldName": "email",
     *   "constraintName": "EMAIL_FORMAT"
     * }
     * </pre>
     *
     * @param ex the FormatValidationException that was thrown
     * @return ProblemDetail containing format validation error information
     */
    @ExceptionHandler(FormatValidationException.class)
    public ProblemDetail handleFormatValidationException(FormatValidationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Format Validation Error");
        problemDetail.setType(URI.create("https://example.com/errors/format-validation"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("fieldName", ex.getFieldName());
        problemDetail.setProperty("constraintName", ex.getConstraintName());
        return problemDetail;
    }

    /**
     * Handles general validation exceptions and converts them to ProblemDetail.
     *
     * <p>Response format:</p>
     * <pre>
     * {
     *   "type": "https://example.com/errors/validation-error",
     *   "title": "Validation Error",
     *   "status": 400,
     *   "detail": "Validation failed",
     *   "timestamp": "2025-01-27T15:30:00Z",
     *   "validationType": "UNIQUENESS"
     * }
     * </pre>
     *
     * @param ex the ValidationException that was thrown
     * @return ProblemDetail containing general validation error information
     */
    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidationException(ValidationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("https://example.com/errors/validation-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("validationType", ex.getValidationType().name());
        return problemDetail;
    }

    /**
     * Handles method argument type mismatch exceptions (e.g., invalid UUID format in path variables).
     *
     * <p>Response format:</p>
     * <pre>
     * {
     *   "type": "https://example.com/errors/validation-error",
     *   "title": "Validation Error",
     *   "status": 400,
     *   "detail": "Invalid UUID format for userId parameter",
     *   "timestamp": "2025-01-27T15:30:00Z"
     * }
     * </pre>
     *
     * @param ex the MethodArgumentTypeMismatchException that was thrown
     * @return ProblemDetail containing validation error information
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String parameterName = ex.getName();
        Class<?> requiredTypeClass = ex.getRequiredType();
        String requiredType = requiredTypeClass != null ? requiredTypeClass.getSimpleName() : "unknown";
        String detail = String.format("Invalid %s format for %s parameter", requiredType, parameterName);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            detail
        );
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("https://example.com/errors/validation-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("parameterName", parameterName);
        problemDetail.setProperty("requiredType", requiredType);
        return problemDetail;
    }
}