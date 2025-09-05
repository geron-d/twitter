package com.twitter.common.aspect;

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
        boolean printDetails = methodSignature.getMethod()
            .getAnnotation(LoggableRequest.class)
            .printRequestBody();

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
}
