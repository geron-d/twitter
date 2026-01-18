package com.twitter.common.enums.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of validation types used in Twitter's validation framework.
 * <p>
 * This enum categorizes different types of validation errors that can occur
 * in the Twitter system, helping to provide appropriate error handling and
 * user feedback. It serves as a classification system for validation exceptions
 * and enables the GlobalExceptionHandler to provide specific error responses
 * based on the validation type.
 *
 * <p>The validation types cover the main categories of validation failures:</p>
 * - <strong>Data Integrity</strong> - Ensuring data uniqueness and consistency
 * - <strong>Business Logic</strong> - Enforcing domain-specific rules and constraints
 * - <strong>Technical Format</strong> - Validating data structure and syntax
 *
 * @author geron
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
public enum ValidationType {

    /**
     * Uniqueness validation for preventing duplicate data.
     * <p>
     * This validation type is used when checking for duplicate values in critical
     * fields such as email addresses, usernames, or other unique identifiers.
     * It ensures data integrity by preventing conflicts that could arise from
     * duplicate entries in the system.
     */
    UNIQUENESS("Uniqueness validation"),

    /**
     * Business rule validation for enforcing domain-specific constraints.
     * <p>
     * This validation type is used when enforcing business logic rules that
     * are specific to the Twitter domain. These rules go beyond technical
     * validation and implement complex business constraints that ensure
     * the system operates according to business requirements.
     */
    BUSINESS_RULE("Business rule validation"),

    /**
     * Format validation for data structure and syntax checking.
     * <p>
     * This validation type is used when validating the technical format and
     * structure of data. It ensures that data conforms to expected formats,
     * syntax rules, and technical constraints before processing.
     */
    FORMAT("Format validation");

    /**
     * Human-readable description of the validation type.
     */
    private final String description;
}
