package com.twitter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
import com.twitter.dto.UserRoleUpdateDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.dto.filter.UserFilter;
import com.twitter.exception.validation.BusinessRuleValidationException;
import com.twitter.exception.validation.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Интерфейс сервиса для управления пользователями в системе Twitter.
 * Предоставляет бизнес-логику для CRUD операций с пользователями, включая валидацию,
 * хеширование паролей и соблюдение бизнес-правил.
 * 
 * @author Twitter Team
 * @version 1.0
 */
public interface UserService {

    /**
     * Получает пользователя по идентификатору.
     * Выполняет поиск в базе данных и возвращает данные пользователя.
     *
     * @param id UUID идентификатор пользователя
     * @return Optional с данными пользователя или пустой Optional если пользователь не найден
     */
    Optional<UserResponseDto> getUserById(UUID id);

    /**
     * Получает список пользователей с фильтрацией и пагинацией.
     * Поддерживает фильтрацию по различным критериям и сортировку результатов.
     *
     * @param userFilter фильтр для поиска пользователей (имя, фамилия, роль, статус)
     * @param pageable параметры пагинации (страница, размер, сортировка)
     * @return Page с отфильтрованным списком пользователей
     */
    Page<UserResponseDto> findAll(UserFilter userFilter, Pageable pageable);

    /**
     * Создает нового пользователя в системе.
     * Автоматически устанавливает статус ACTIVE и роль USER.
     * Пароль хешируется с использованием PBKDF2 с солью.
     *
     * @param userRequest данные для создания пользователя
     * @return данные созданного пользователя
     * @throws ValidationException при нарушении валидации или конфликте уникальности
     */
    UserResponseDto createUser(UserRequestDto userRequest);

    /**
     * Полностью обновляет данные существующего пользователя.
     * Заменяет все поля пользователя на новые значения из DTO.
     * Включает валидацию уникальности и бизнес-правил.
     *
     * @param id UUID идентификатор пользователя для обновления
     * @param userDetails новые данные пользователя
     * @return Optional с обновленными данными пользователя или пустой Optional если пользователь не найден
     * @throws ValidationException при нарушении валидации или конфликте уникальности
     */
    Optional<UserResponseDto> updateUser(UUID id, UserUpdateDto userDetails);

    /**
     * Частично обновляет данные пользователя с использованием JSON Patch.
     * Позволяет обновлять только указанные поля без изменения остальных.
     * Включает валидацию JSON структуры и бизнес-правил.
     *
     * @param id UUID идентификатор пользователя для обновления
     * @param patchNode JSON данные для частичного обновления
     * @return обновленный пользователь или пустой Optional если пользователь не найден
     */
    Optional<UserResponseDto> patchUser(UUID id, JsonNode patchNode);

    /**
     * Деактивирует пользователя, устанавливая статус INACTIVE.
     * Предотвращает деактивацию последнего активного администратора в системе.
     *
     * @param id UUID идентификатор пользователя для деактивации
     * @return Optional с обновленными данными пользователя или пустой Optional если пользователь не найден
     * @throws BusinessRuleValidationException при попытке деактивации последнего администратора
     */
    Optional<UserResponseDto> inactivateUser(UUID id);

    /**
     * Обновляет роль пользователя в системе.
     * Предотвращает изменение роли последнего активного администратора.
     * Поддерживает роли: USER, ADMIN, MODERATOR.
     *
     * @param id UUID идентификатор пользователя
     * @param roleUpdate данные для обновления роли пользователя
     * @return Optional с обновленными данными пользователя или пустой Optional если пользователь не найден
     * @throws BusinessRuleValidationException при попытке изменения роли последнего администратора
     */
    Optional<UserResponseDto> updateUserRole(UUID id, UserRoleUpdateDto roleUpdate);
}
