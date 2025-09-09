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

@Slf4j
@Aspect
@Component
public class LoggableRequestAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

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
