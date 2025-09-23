package com.twitter.exception.validation;

import lombok.Getter;

/**
 * Исключение для ошибок валидации формата данных.
 * Выбрасывается при нарушении ограничений Bean Validation или
 * ошибках парсинга JSON данных.
 * 
 * @author Twitter Team
 * @version 1.0
 */
@Getter
public class FormatValidationException extends ValidationException {
    
    private final String fieldName;
    private final String constraintName;
    
    /**
     * Конструктор с указанием поля и ограничения.
     * 
     * @param fieldName название поля с ошибкой
     * @param constraintName название нарушенного ограничения
     * @param message сообщение об ошибке
     */
    public FormatValidationException(String fieldName, String constraintName, String message) {
        super(message);
        this.fieldName = fieldName;
        this.constraintName = constraintName;
    }
    
    /**
     * Конструктор с кастомным сообщением.
     * 
     * @param message сообщение об ошибке
     */
    public FormatValidationException(String message) {
        super(message);
        this.fieldName = null;
        this.constraintName = null;
    }
    
    /**
     * Конструктор с сообщением и причиной ошибки.
     * 
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     */
    public FormatValidationException(String message, Throwable cause) {
        super(message, cause);
        this.fieldName = null;
        this.constraintName = null;
    }
    
    @Override
    public ValidationType getValidationType() {
        return ValidationType.FORMAT;
    }
    
    /**
     * Создает исключение для ошибки парсинга JSON.
     * 
     * @param cause причина ошибки парсинга
     * @return исключение с соответствующим сообщением
     */
    public static FormatValidationException jsonParsingError(Throwable cause) {
        return new FormatValidationException("Error parsing JSON patch data: " + cause.getMessage(), cause);
    }
    
    /**
     * Создает исключение для ошибки Bean Validation.
     * 
     * @param fieldName название поля
     * @param constraintName название ограничения
     * @param message сообщение об ошибке
     * @return исключение с соответствующим сообщением
     */
    public static FormatValidationException beanValidationError(String fieldName, String constraintName, String message) {
        return new FormatValidationException(fieldName, constraintName, message);
    }
}
