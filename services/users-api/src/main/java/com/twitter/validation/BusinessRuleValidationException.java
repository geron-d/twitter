package com.twitter.validation;

import com.twitter.enums.UserRole;
import lombok.Getter;

/**
 * Исключение для ошибок валидации бизнес-правил системы.
 * Выбрасывается при нарушении бизнес-логики, например, при попытке
 * деактивации последнего администратора или нарушении других системных ограничений.
 * 
 * @author Twitter Team
 * @version 1.0
 */
@Getter
public class BusinessRuleValidationException extends ValidationException {
    
    private final String ruleName;
    private final Object context;
    
    /**
     * Конструктор с указанием названия правила и контекста.
     * 
     * @param ruleName название нарушенного бизнес-правила
     * @param context контекст нарушения (например, ID пользователя)
     */
    public BusinessRuleValidationException(String ruleName, Object context) {
        super(String.format("Business rule '%s' violated for context: %s", ruleName, context));
        this.ruleName = ruleName;
        this.context = context;
    }
    
    /**
     * Конструктор с кастомным сообщением.
     * 
     * @param message сообщение об ошибке
     */
    public BusinessRuleValidationException(String message) {
        super(message);
        this.ruleName = null;
        this.context = null;
    }
    
    /**
     * Конструктор с сообщением и причиной ошибки.
     * 
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     */
    public BusinessRuleValidationException(String message, Throwable cause) {
        super(message, cause);
        this.ruleName = null;
        this.context = null;
    }
    
    @Override
    public ValidationType getValidationType() {
        return ValidationType.BUSINESS_RULE;
    }
    
    /**
     * Создает исключение для попытки деактивации последнего администратора.
     * 
     * @param userId ID пользователя-администратора
     * @return исключение с соответствующим сообщением
     */
    public static BusinessRuleValidationException lastAdminDeactivation(Object userId) {
        return new BusinessRuleValidationException("LAST_ADMIN_DEACTIVATION", userId);
    }
    
    /**
     * Создает исключение для попытки смены роли последнего администратора.
     * 
     * @param userId ID пользователя-администратора
     * @param newRole новая роль
     * @return исключение с соответствующим сообщением
     */
    public static BusinessRuleValidationException lastAdminRoleChange(Object userId, UserRole newRole) {
        return new BusinessRuleValidationException("LAST_ADMIN_ROLE_CHANGE", 
            String.format("userId=%s, newRole=%s", userId, newRole));
    }
}
