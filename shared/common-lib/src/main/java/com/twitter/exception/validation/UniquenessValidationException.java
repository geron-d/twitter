package com.twitter.exception.validation;

import lombok.Getter;

/**
 * Исключение для ошибок валидации уникальности данных пользователя.
 * Выбрасывается при попытке создать или обновить пользователя с уже существующими
 * login или email в системе.
 * 
 * @author Twitter Team
 * @version 1.0
 */
@Getter
public class UniquenessValidationException extends ValidationException {
    
    private final String fieldName;
    private final String fieldValue;
    
    /**
     * Конструктор с указанием поля и значения, вызвавшего конфликт.
     * 
     * @param fieldName название поля (login или email)
     * @param fieldValue значение поля, вызвавшее конфликт
     */
    public UniquenessValidationException(String fieldName, String fieldValue) {
        super(String.format("User with %s '%s' already exists", fieldName, fieldValue));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
    
    /**
     * Конструктор с кастомным сообщением.
     * 
     * @param message сообщение об ошибке
     */
    public UniquenessValidationException(String message) {
        super(message);
        this.fieldName = null;
        this.fieldValue = null;
    }
    
    /**
     * Конструктор с сообщением и причиной ошибки.
     * 
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     */
    public UniquenessValidationException(String message, Throwable cause) {
        super(message, cause);
        this.fieldName = null;
        this.fieldValue = null;
    }
    
    @Override
    public ValidationType getValidationType() {
        return ValidationType.UNIQUENESS;
    }
}
