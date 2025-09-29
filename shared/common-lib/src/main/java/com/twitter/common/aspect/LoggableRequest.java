package com.twitter.common.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for automatic HTTP request/response logging in Twitter microservices.
 * <p>
 * This annotation enables automatic logging of HTTP requests and responses
 * for methods annotated with it. It provides configuration options for
 * controlling what information is logged and which fields should be hidden
 * for security purposes. The logging is performed by the LoggableRequestAspect
 * which intercepts method calls and logs request details including headers,
 * body content, and response information.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @LoggableRequest
 * public ResponseEntity<User> createUser(@RequestBody User user) {
 *     return userService.createUser(user);
 * }
 *
 * @LoggableRequest(printRequestBody = false, hideFields = {"password", "ssn"})
 * public ResponseEntity<User> updateUser(@RequestBody User user) {
 *     return userService.updateUser(user);
 * }
 * }</pre>
 *
 * @author Twitter Team
 * @version 1.0
 * @see LoggableRequestAspect for the implementation that handles the logging
 * @since 2025-01-27
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoggableRequest {

    /**
     * Controls whether the request body should be logged.
     * <p>
     * When set to {@code true} (default), the complete request body will be
     * logged including all fields. When set to {@code false}, only the request
     * method and URI will be logged without the body content.
     *
     * <p>Use {@code false} for:</p>
     * <ul>
     *   <li>Large request bodies that would clutter logs</li>
     *   <li>Sensitive endpoints where body content should not be logged</li>
     *   <li>Performance-critical endpoints where logging overhead should be minimized</li>
     * </ul>
     *
     * @return {@code true} if request body should be logged, {@code false} otherwise
     */
    boolean printRequestBody() default true;

    /**
     * Array of field names that should be hidden in the logged request body.
     * <p>
     * This parameter allows you to specify sensitive fields that should be
     * replaced with "***" in the logged output. The hiding is performed
     * recursively on nested objects and arrays. Field names are case-sensitive
     * and should match exactly with the JSON property names.
     *
     * <p>Common fields to hide:</p>
     * <ul>
     *   <li>{@code "password"} - User passwords</li>
     *   <li>{@code "ssn"} - Social Security Numbers</li>
     *   <li>{@code "creditCard"} - Credit card information</li>
     *   <li>{@code "token"} - Authentication tokens</li>
     *   <li>{@code "secret"} - API secrets</li>
     * </ul>
     *
     * <p>Example:</p>
     * <pre>{@code
     * @LoggableRequest(hideFields = {"password", "ssn", "creditCard"})
     * public ResponseEntity<User> createUser(@RequestBody User user) {
     *     // password, ssn, and creditCard fields will be hidden in logs
     * }
     * }</pre>
     *
     * @return array of field names to hide in the logged output
     */
    String[] hideFields() default {};

}