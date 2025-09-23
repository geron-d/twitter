package com.twitter.validation;

import lombok.Getter;

/**
 * Базовое исключение для всех типов валидации в системе Twitter.
 * Предоставляет общую структуру для всех ошибок валидации.
 * 
 * @author Twitter Team
 * @version 1.0
 */
@Getter
public abstract class ValidationException extends RuntimeException {
    
    /**
     * Конструктор с сообщением об ошибке.
     * 
     * @param message сообщение об ошибке валидации
     */
    public ValidationException(String message) {
        super(message);
    }
    
    /**
     * Конструктор с сообщением и причиной ошибки.
     * 
     * @param message сообщение об ошибке валидации
     * @param cause причина ошибки
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Возвращает тип валидации, который вызвал ошибку.
     * 
     * @return тип валидации
     */
    public abstract ValidationType getValidationType();
}
