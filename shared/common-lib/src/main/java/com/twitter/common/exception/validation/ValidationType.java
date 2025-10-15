package com.twitter.common.exception.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of validation types used in Twitter's validation framework.
 *
 * <p>
 * This enum categorizes different types of validation errors that can occur
 * in the Twitter system, helping to provide appropriate error handling and
 * user feedback. It serves as a classification system for validation exceptions
 * and enables the GlobalExceptionHandler to provide specific error responses
 * based on the validation type.
 *
 * <p>The validation types cover the main categories of validation failures:</p>
 * <ul>
 *   <li><strong>Data Integrity</strong> - Ensuring data uniqueness and consistency</li>
 *   <li><strong>Business Logic</strong> - Enforcing domain-specific rules and constraints</li>
 *   <li><strong>Technical Format</strong> - Validating data structure and syntax</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // In validation exception classes
 * @Override
 * public ValidationType getValidationType() {
 *     return ValidationType.UNIQUENESS;
 * }
 *
 * // In error handling
 * switch (validationException.getValidationType()) {
 *     case UNIQUENESS:
 *         return handleUniquenessError(validationException);
 *     case BUSINESS_RULE:
 *         return handleBusinessRuleError(validationException);
 *     case FORMAT:
 *         return handleFormatError(validationException);
 * }
 *
 * // In logging and monitoring
 * log.warn("Validation failed: type={}, message={}",
 *     exception.getValidationType(), exception.getMessage());
 * }</pre>
 *
 * @author geron
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
public enum ValidationType {

    /**
     * Uniqueness validation for preventing duplicate data.
     *
     * <p>
     * This validation type is used when checking for duplicate values in critical
     * fields such as email addresses, usernames, or other unique identifiers.
     * It ensures data integrity by preventing conflicts that could arise from
     * duplicate entries in the system.
     *
     * <p>Common use cases:</p>
     * <ul>
     *   <li>Email address uniqueness during user registration</li>
     *   <li>Username uniqueness in user profiles</li>
     * </ul>
     *
     * <p>Example:</p>
     * <pre>{@code
     * if (userRepository.existsByEmail(email)) {
     *     throw new UniquenessValidationException("email", email, "Email already exists");
     * }
     * }</pre>
     */
    UNIQUENESS("Uniqueness validation"),

    /**
     * Business rule validation for enforcing domain-specific constraints.
     *
     * <p>
     * This validation type is used when enforcing business logic rules that
     * are specific to the Twitter domain. These rules go beyond technical
     * validation and implement complex business constraints that ensure
     * the system operates according to business requirements.
     *
     * <p>Common use cases:</p>
     * <ul>
     *   <li>Preventing deactivation of the last administrator</li>
     *   <li>Validating user account status transitions</li>
     * </ul>
     *
     * <p>Example:</p>
     * <pre>{@code
     * if (isLastActiveAdmin(userId)) {
     *     throw new BusinessRuleValidationException(
     *         "LAST_ADMIN_PROTECTION",
     *         "Cannot deactivate the last active administrator"
     *     );
     * }
     * }</pre>
     */
    BUSINESS_RULE("Business rule validation"),

    /**
     * Format validation for data structure and syntax checking.
     *
     * <p>
     * This validation type is used when validating the technical format and
     * structure of data. It ensures that data conforms to expected formats,
     * syntax rules, and technical constraints before processing.
     *
     * <p>Common use cases:</p>
     * <ul>
     *   <li>Email format validation using regex patterns</li>
     *   <li>JSON structure validation for API requests</li>
     * </ul>
     *
     * <p>Example:</p>
     * <pre>{@code
     * if (!isValidEmailFormat(email)) {
     *     throw new FormatValidationException(
     *         "email",
     *         "EMAIL_FORMAT",
     *         "Invalid email format: " + email
     *     );
     * }
     * }</pre>
     */
    FORMAT("Format validation");

    /**
     * Human-readable description of the validation type.
     *
     * <p>
     * This field provides a descriptive string that explains what type
     * of validation this enum value represents. It is useful for logging,
     * debugging, and generating user-friendly error messages.
     *
     * @return the description of this validation type
     */
    private final String description;
}
