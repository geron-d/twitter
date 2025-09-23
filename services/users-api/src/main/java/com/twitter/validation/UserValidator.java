package com.twitter.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.dto.UserPatchDto;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.enums.UserRole;

import java.util.UUID;

/**
 * Интерфейс для валидации пользователей в системе Twitter.
 * Централизует всю логику валидации, вынесенную из UserServiceImpl.
 * 
 * @author Twitter Team
 * @version 1.0
 */
public interface UserValidator {
    
    /**
     * Полная валидация для создания пользователя.
     * Включает проверку уникальности login/email и формат данных.
     * 
     * @param userRequest DTO с данными для создания пользователя
     * @throws ValidationException при нарушении валидации
     */
    void validateForCreate(UserRequestDto userRequest);
    
    /**
     * Валидация для обновления пользователя.
     * Включает проверку уникальности login/email с исключением текущего пользователя.
     * 
     * @param userId ID пользователя для обновления
     * @param userUpdate DTO с данными для обновления
     * @throws ValidationException при нарушении валидации
     */
    void validateForUpdate(UUID userId, UserUpdateDto userUpdate);
    
    /**
     * Валидация для PATCH операций.
     * Включает проверку JSON структуры, Bean Validation и уникальности.
     * 
     * @param userId ID пользователя для патча
     * @param patchNode JSON данные для патча
     * @throws ValidationException при нарушении валидации
     */
    void validateForPatch(UUID userId, JsonNode patchNode);
    
    /**
     * Проверка уникальности логина и email пользователя.
     * 
     * @param login логин для проверки (может быть null)
     * @param email email для проверки (может быть null)
     * @param excludeUserId ID пользователя для исключения из проверки (при обновлении)
     * @throws UniquenessValidationException при конфликте уникальности
     */
    void validateUniqueness(String login, String email, UUID excludeUserId);
    
    /**
     * Проверка возможности деактивации пользователя.
     * Предотвращает деактивацию последнего активного администратора.
     * 
     * @param userId ID пользователя для деактивации
     * @throws BusinessRuleValidationException при нарушении бизнес-правил
     */
    void validateAdminDeactivation(UUID userId);
    
    /**
     * Проверка возможности смены роли пользователя.
     * Предотвращает смену роли последнего активного администратора.
     * 
     * @param userId ID пользователя
     * @param newRole новая роль пользователя
     * @throws BusinessRuleValidationException при нарушении бизнес-правил
     */
    void validateRoleChange(UUID userId, UserRole newRole);
    
    /**
     * Валидация JSON структуры патча.
     * Проверяет корректность JSON и возможность применения к DTO.
     * 
     * @param patchNode JSON данные для патча
     * @throws FormatValidationException при ошибке формата JSON
     */
    void validatePatchData(JsonNode patchNode);
    
    /**
     * Bean Validation для DTO патча.
     * Применяет аннотации валидации к объекту UserPatchDto.
     * 
     * @param patchDto DTO для валидации
     * @throws FormatValidationException при нарушении ограничений валидации
     */
    void validatePatchConstraints(UserPatchDto patchDto);
}
