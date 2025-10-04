package com.twitter.common.exception.validation;

import lombok.Getter;

/**
 * Exception thrown when format validation fails due to data structure or syntax errors.
 *
 * <p>
 * This exception is thrown when data does not conform to expected formats,
 * syntax rules, or technical constraints. It handles various types of format
 * validation failures including Bean Validation constraint violations and
 * JSON parsing errors.
 *
 * <p>The exception provides the following functionality:</p>
 * <ul>
 *   <li>Identifies the specific field that failed format validation</li>
 *   <li>Specifies the constraint that was violated</li>
 *   <li>Supports custom error messages for specific scenarios</li>
 *   <li>Provides factory methods for common validation errors</li>
 *   <li>Enables cause chaining for debugging complex validation failures</li>
 * </ul>
 *
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Email format validation using regex patterns</li>
 *   <li>JSON structure validation for API requests</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Bean Validation error
 * if (!isValidEmailFormat(email)) {
 *     throw FormatValidationException.beanValidationError(
 *         "email", "EMAIL_FORMAT", "Invalid email format: " + email
 *     );
 * }
 *
 * // JSON parsing error
 * try {
 *     JsonNode jsonNode = objectMapper.readTree(jsonString);
 * } catch (JsonProcessingException e) {
 *     throw FormatValidationException.jsonParsingError(e);
 * }
 *
 * }</pre>
 *
 * @author geron
 * @version 1.0
 */
@Getter
public class FormatValidationException extends ValidationException {

    /**
     * The name of the field that failed format validation.
     *
     * <p>
     * This field identifies which specific field (e.g., "email", "phone", "date")
     * contains the invalid format. It is used by the GlobalExceptionHandler to
     * provide detailed error information in the ProblemDetail response.
     */
    private final String fieldName;

    /**
     * The name of the constraint that was violated.
     *
     * <p>
     * This field specifies which validation constraint was violated
     * (e.g., "EMAIL_FORMAT", "PHONE_FORMAT", "DATE_FORMAT"). It helps
     * with debugging and provides context for the validation failure.
     */
    private final String constraintName;

    /**
     * Constructs a new format validation exception with field and constraint details.
     *
     * <p>
     * This constructor creates an exception with specific information about
     * which field failed format validation and what constraint was violated.
     * It provides the most detailed information for error handling and debugging.
     *
     * <p>Example:</p>
     * <pre>{@code
     * if (!isValidEmailFormat(email)) {
     *     throw new FormatValidationException(
     *         "email", "EMAIL_FORMAT", "Invalid email format: " + email
     *     );
     * }
     * }</pre>
     *
     * @param fieldName      the name of the field that failed validation
     * @param constraintName the name of the constraint that was violated
     * @param message        the error message describing the format violation
     */
    public FormatValidationException(String fieldName, String constraintName, String message) {
        super(message);
        this.fieldName = fieldName;
        this.constraintName = constraintName;
    }

    /**
     * Constructs a new format validation exception with a custom message.
     *
     * <p>
     * This constructor allows specifying a custom error message for specific
     * format validation scenarios. The fieldName and constraintName will be
     * set to null, indicating that specific field information is not available.
     *
     * <p>Example:</p>
     * <pre>{@code
     * throw new FormatValidationException(
     *     "The provided data does not match the expected format"
     * );
     * }</pre>
     *
     * @param message the custom error message describing the format violation
     */
    public FormatValidationException(String message) {
        super(message);
        this.fieldName = null;
        this.constraintName = null;
    }

    /**
     * Constructs a new format validation exception with a custom message and cause.
     *
     * <p>
     * This constructor allows wrapping another exception while providing context
     * about the format validation failure. This is useful when format validation
     * errors occur as a result of other exceptions (e.g., JSON parsing errors).
     *
     * <p>Example:</p>
     * <pre>{@code
     * try {
     *     JsonNode jsonNode = objectMapper.readTree(jsonString);
     * } catch (JsonProcessingException e) {
     *     throw new FormatValidationException(
     *         "Failed to parse JSON data", e
     *     );
     * }
     * }</pre>
     *
     * @param message the custom error message describing the format violation
     * @param cause   the underlying cause that led to this exception
     */
    public FormatValidationException(String message, Throwable cause) {
        super(message, cause);
        this.fieldName = null;
        this.constraintName = null;
    }

    /**
     * Returns the validation type for this exception.
     *
     * <p>
     * This method identifies this exception as a format validation error,
     * enabling the GlobalExceptionHandler to provide appropriate error handling
     * and response formatting.
     *
     * @return ValidationType.FORMAT indicating this is a format validation error
     */
    @Override
    public ValidationType getValidationType() {
        return ValidationType.FORMAT;
    }

    /**
     * Factory method for creating JSON parsing error exceptions.
     *
     * <p>
     * This factory method creates a FormatValidationException specifically
     * for JSON parsing errors. It wraps the original parsing exception and
     * provides a descriptive error message.
     *
     * <p>Example:</p>
     * <pre>{@code
     * try {
     *     JsonNode jsonNode = objectMapper.readTree(jsonString);
     * } catch (JsonProcessingException e) {
     *     throw FormatValidationException.jsonParsingError(e);
     * }
     * }</pre>
     *
     * @param cause the exception that caused the JSON parsing failure
     * @return a FormatValidationException with appropriate error message
     */
    public static FormatValidationException jsonParsingError(Throwable cause) {
        return new FormatValidationException("Error parsing JSON patch data: " + cause.getMessage(), cause);
    }

    /**
     * Factory method for creating Bean Validation error exceptions.
     *
     * <p>
     * This factory method creates a FormatValidationException specifically
     * for Bean Validation constraint violations. It provides detailed
     * information about the field and constraint that failed validation.
     *
     * <p>Example:</p>
     * <pre>{@code
     * if (!isValidEmailFormat(email)) {
     *     throw FormatValidationException.beanValidationError(
     *         "email", "EMAIL_FORMAT", "Invalid email format: " + email
     *     );
     * }
     * }</pre>
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
