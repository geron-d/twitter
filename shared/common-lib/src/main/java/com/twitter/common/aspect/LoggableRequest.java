package com.twitter.common.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for automatic HTTP request/response logging.
 * <p>
 * This annotation enables automatic logging of HTTP requests and responses
 * for methods annotated with it. It provides configuration options for
 * controlling what information is logged and which fields should be hidden
 * for security purposes. The logging is performed by the LoggableRequestAspect
 * which intercepts method calls and logs request details including headers,
 * body content, and response information.
 *
 * @author geron
 * @version 1.0
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
     * - Large request bodies that would clutter logs
     * - Sensitive endpoints where body content should not be logged
     * - Performance-critical endpoints where logging overhead should be minimized
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
     * @return array of field names to hide in the logged output
     */
    String[] hideFields() default {};

}