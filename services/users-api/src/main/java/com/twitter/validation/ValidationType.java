package com.twitter.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Перечисление типов валидации для категоризации ошибок в системе Twitter.
 * Используется для классификации различных типов валидации и соответствующих исключений.
 * 
 * @author Twitter Team
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
public enum ValidationType {
    
    /**
     * Валидация уникальности данных (login, email).
     * Проверяет отсутствие дублирования критичных полей в системе.
     */
    UNIQUENESS("Uniqueness validation"),
    
    /**
     * Валидация бизнес-правил системы.
     * Проверяет соблюдение корпоративных правил и ограничений.
     */
    BUSINESS_RULE("Business rule validation"),
    
    /**
     * Валидация формата данных.
     * Проверяет соответствие данных техническим ограничениям и форматам.
     */
    FORMAT("Format validation");
    
    private final String description;

}
