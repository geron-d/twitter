package com.twitter.common.aspect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Aspect for automatic HTTP request/response logging.
 * <p>
 * This aspect provides comprehensive logging functionality across multiple
 * methods and classes. It uses Spring AOP to intercept method calls annotated
 * with @LoggableRequest and logs detailed information about HTTP requests and
 * responses, including headers, body content, and response status codes.
 *
 * <p>The aspect performs the following operations:</p>
 * - Intercepts method calls annotated with @LoggableRequest
 * - Extracts HTTP request information from the current request context
 * - Logs request details including method, URI, headers, and body
 * - Executes the original method
 * - Logs response details including status code and body information
 * - Supports hiding sensitive fields in request bodies
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@Aspect
@Component
public class LoggableRequestAspect {

    public LoggableRequestAspect() {
        // Default constructor - Spring will handle initialization
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Advice method that intercepts and logs HTTP request/response details.
     * <p>
     * This method is executed around methods annotated with @LoggableRequest
     * and provides comprehensive logging functionality. It extracts request
     * information from the current HTTP context, logs the request details,
     * executes the original method, and then logs the response information.
     *
     * <p>The logging process includes:</p>
     * - Extracting HTTP request from Spring's RequestContextHolder
     * - Logging request details (method, URI, headers, body)
     * - Executing the original method via ProceedingJoinPoint
     * - Logging response details (status code, body information)
     *
     * @param proceedingJoinPoint the AOP join point containing method information
     * @return the result of the original method execution
     * @throws Throwable if the original method throws an exception
     */
    @Around("@annotation(com.twitter.common.aspect.LoggableRequest))")
    public Object log(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
            .currentRequestAttributes())
            .getRequest();
        logRequestDetails(request, proceedingJoinPoint);
        Object value = proceedingJoinPoint.proceed();
        logResponseDetails(request, value);
        return value;
    }

    /**
     * Logs detailed information about the HTTP request.
     * <p>
     * This method extracts and logs comprehensive request information including
     * HTTP method, URI, headers, and body content. It respects the configuration
     * from the @LoggableRequest annotation to determine what information should
     * be logged and which fields should be hidden for security purposes.
     *
     * <p>The logging behavior is controlled by:</p>
     * - {@code printRequestBody} - whether to log the request body
     * - {@code hideFields} - array of field names to hide in the body
     *
     * <p>Log format examples:</p>
     * <pre>
     * ### REQUEST POST /api/users ,Headers: Content-Type: application/json; Accept: ** , Body: {"name":"John","email":"john@example.com"}
     * ### REQUEST GET /api/users/123
     * </pre>
     *
     * @param request             the HTTP servlet request
     * @param proceedingJoinPoint the AOP join point containing method information
     */
    private void logRequestDetails(HttpServletRequest request, ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        LoggableRequest annotation = methodSignature.getMethod()
            .getAnnotation(LoggableRequest.class);
        boolean printDetails = annotation.printRequestBody();
        String[] hideFields = annotation.hideFields();

        String method = request.getMethod();
        String uri = request.getRequestURI();

        if (printDetails) {
            String headers = Collections.list(request.getHeaderNames())
                .stream()
                .map(header -> header + ": " + request.getHeader(header))
                .collect(Collectors.joining("; "));

            Object requestBody = proceedingJoinPoint.getArgs().length > 0
                ? proceedingJoinPoint.getArgs()[0]
                : "{}";

            if (hideFields.length > 0) {
                requestBody = hideSensitiveFields(requestBody, hideFields);
            }

            if (!headers.isEmpty()) {
                log.info("### REQUEST {} {} ,Headers: {} , Body: {}", method, uri, headers, requestBody);
            } else {
                log.info("### REQUEST {} {} , Body: {}", method, uri, requestBody);
            }
        } else {
            log.info("### REQUEST {} {}", method, uri);
        }
    }

    /**
     * Logs detailed information about the HTTP response.
     * <p>
     * This method logs response information including HTTP status code and
     * body details. The level of detail depends on the current log level:
     * - DEBUG level: logs complete response body
     * - INFO level: logs status code and collection/map sizes for structured data
     *
     * <p>The method handles different response types:</p>
     * - ResponseEntity - logs status code and body information
     * - Collection - logs status code and collection size
     * - Map - logs status code and map size
     * - Other types - logs status code only
     *
     * <p>Log format examples:</p>
     * <pre>
     * ### RESPONSE POST /api/users , status: 201
     * ### RESPONSE GET /api/users , status: 200 , collection size 5
     * ### RESPONSE PUT /api/users/123 , status: 200 , map size 3
     * </pre>
     *
     * @param request  the HTTP servlet request
     * @param response the response object returned by the method
     */
    private void logResponseDetails(HttpServletRequest request, Object response) {
        Optional<?> body = Optional.ofNullable(response);
        if (body.isEmpty() || !(body.get() instanceof ResponseEntity)) {
            log.info("### RESPONSE {}, {} ", request.getMethod(), request.getRequestURI());
            return;
        }

        ResponseEntity<?> responseEntity = (ResponseEntity<?>) response;
        if (log.isDebugEnabled()) {
            log.debug("### RESPONSE {} {}, status: {}, {}", request.getMethod(), request.getRequestURI(),
                responseEntity.getStatusCode().value(), responseEntity.getBody());
        } else {
            if (body.get() instanceof Collection) {
                log.info("### RESPONSE {} {} , status: {} , collection size {}", request.getMethod(),
                    request.getRequestURI(), responseEntity.getStatusCode(), ((Collection<?>) body.get()).size());
            } else if (body.get() instanceof Map) {
                log.info("### RESPONSE {} {} , status: {} , map size {}", request.getMethod(), request.getRequestURI(),
                    responseEntity.getStatusCode().value(), ((Map<?, ?>) body.get()).size());
            } else {
                log.info("### RESPONSE {} {} , status: {}", request.getMethod(), request.getRequestURI(),
                    responseEntity.getStatusCode().value());
            }
        }
    }

    /**
     * Hides sensitive fields in the request body by replacing their values with "***".
     * <p>
     * This method converts the request body to JSON, recursively hides specified
     * fields, and returns the modified JSON structure. It handles nested objects
     * and arrays by recursively processing all levels of the JSON structure.
     *
     * <p>The hiding algorithm:</p>
     * 1. Serialize the request body to JSON string
     * 2. Parse the JSON string into a JsonNode tree
     * 3. Recursively traverse the tree and hide specified fields
     * 4. Return the modified JsonNode
     *
     * <p>If JSON processing fails, the original object is returned with a warning log.</p>
     *
     * @param requestBody the original request body object
     * @param hideFields  array of field names to hide
     * @return the request body with sensitive fields hidden, or original object if processing fails
     */
    private Object hideSensitiveFields(Object requestBody, String[] hideFields) {
        try {
            String jsonString = objectMapper.writeValueAsString(requestBody);
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            hideFieldsInJsonNode(jsonNode, hideFields);

            return jsonNode;
        } catch (Exception e) {
            log.warn("Failed to hide sensitive fields, returning original object: {}", e.getMessage());
            return requestBody;
        }
    }

    /**
     * Recursively hides specified fields in a JSON node structure.
     * <p>
     * This method implements the core algorithm for hiding sensitive fields
     * in JSON structures. It handles both objects and arrays by recursively
     * processing all nested elements.
     *
     * <p>The algorithm works as follows:</p>
     * 1. If the node is an object:
     * - Check each field name against the hideFields array
     * - Replace matching field values with "***"
     * - Recursively process nested objects and arrays
     * 2. If the node is an array:
     * - Recursively process each array element
     *
     * <p>Example transformation:</p>
     * <pre>
     * Input:  {"name":"John","password":"secret123","profile":{"email":"john@example.com","ssn":"123-45-6789"}}
     * Fields: ["password","ssn"]
     * Output: {"name":"John","password":"***","profile":{"email":"john@example.com","ssn":"***"}}
     * </pre>
     *
     * @param node       the JSON node to process
     * @param hideFields array of field names to hide
     */
    private void hideFieldsInJsonNode(JsonNode node, String[] hideFields) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            for (String fieldName : hideFields) {
                if (objectNode.has(fieldName)) {
                    objectNode.put(fieldName, "***");
                }
            }

            objectNode.fieldNames().forEachRemaining(fieldName -> {
                JsonNode fieldValue = objectNode.get(fieldName);
                if (fieldValue.isObject() || fieldValue.isArray()) {
                    hideFieldsInJsonNode(fieldValue, hideFields);
                }
            });
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                hideFieldsInJsonNode(arrayElement, hideFields);
            }
        }
    }
}