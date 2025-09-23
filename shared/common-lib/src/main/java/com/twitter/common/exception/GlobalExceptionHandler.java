package com.twitter.common.exception;

import com.twitter.exception.validation.BusinessRuleValidationException;
import com.twitter.exception.validation.FormatValidationException;
import com.twitter.exception.validation.UniquenessValidationException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
     * Обработка ошибок валидации
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
     * Обработка ошибок уникальности (дублирование логина/email)
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
     * Обработка ошибок бизнес-правил (деактивация последнего админа, смена роли)
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
     * Обработка ошибок формата данных (Bean Validation, JSON parsing)
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
     * Обработка попытки деактивации последнего администратора
     */
    @ExceptionHandler(LastAdminDeactivationException.class)
    public ProblemDetail handleLastAdminDeactivationException(LastAdminDeactivationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getReason() != null ? ex.getReason() : "Cannot deactivate the last active administrator"
        );
        problemDetail.setTitle("Last Admin Deactivation Error");
        problemDetail.setType(URI.create("https://example.com/errors/last-admin-deactivation"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
