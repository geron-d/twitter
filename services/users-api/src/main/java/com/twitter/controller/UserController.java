package com.twitter.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.common.aspect.LoggableRequest;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
import com.twitter.dto.UserRoleUpdateDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.dto.filter.UserFilter;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.ValidationException;
import com.twitter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST контроллер для управления пользователями в системе Twitter.
 * Предоставляет полный набор CRUD операций с поддержкой фильтрации, пагинации и ролевой модели.
 * 
 * @author Twitter Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Получает пользователя по идентификатору.
     * 
     * @param id UUID идентификатор пользователя
     * @return ResponseEntity с данными пользователя или 404 если не найден
     */
    @LoggableRequest
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") UUID id) {
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Получает список пользователей с фильтрацией и пагинацией.
     * Поддерживает фильтрацию по имени, фамилии, роли и статусу.
     * 
     * @param userFilter фильтр для поиска пользователей
     * @param pageable параметры пагинации (страница, размер, сортировка)
     * @return PagedModel с отфильтрованным списком пользователей
     */
    @LoggableRequest
    @GetMapping
    public PagedModel<UserResponseDto> findAll(@ModelAttribute UserFilter userFilter, Pageable pageable) {
        Page<UserResponseDto> users = userService.findAll(userFilter, pageable);
        return new PagedModel<>(users);
    }

    /**
     * Создает нового пользователя в системе.
     * Автоматически устанавливает статус ACTIVE и роль USER.
     * Пароль хешируется с использованием PBKDF2.
     * 
     * @param userRequest данные для создания пользователя
     * @return данные созданного пользователя
     * @throws ValidationException при нарушении валидации или конфликте уникальности
     */
    @LoggableRequest(hideFields = {"password"})
    @PostMapping
    public UserResponseDto createUser(@RequestBody @Valid UserRequestDto userRequest) {
        return userService.createUser(userRequest);
    }

    /**
     * Полностью обновляет данные существующего пользователя.
     * Заменяет все поля пользователя на новые значения.
     * 
     * @param id UUID идентификатор пользователя для обновления
     * @param userDetails новые данные пользователя
     * @return ResponseEntity с обновленными данными или 404 если пользователь не найден
     * @throws ValidationException при нарушении валидации или конфликте уникальности
     */
    @LoggableRequest(hideFields = {"password"})
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable("id") UUID id, @RequestBody @Valid UserUpdateDto userDetails) {
        return userService.updateUser(id, userDetails)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Частично обновляет данные пользователя с использованием JSON Patch.
     * Позволяет обновлять только указанные поля без изменения остальных.
     * 
     * @param id UUID идентификатор пользователя для обновления
     * @param patchNode JSON данные для частичного обновления
     * @return ResponseEntity с обновленными данными или 404 если пользователь не найден
     * @throws ValidationException при нарушении валидации или некорректном JSON
     */
    @LoggableRequest
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> patchUser(@PathVariable("id") UUID id, @RequestBody JsonNode patchNode) {
        return userService.patchUser(id, patchNode)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Деактивирует пользователя, устанавливая статус INACTIVE.
     * Предотвращает деактивацию последнего активного администратора.
     * 
     * @param id UUID идентификатор пользователя для деактивации
     * @return ResponseEntity с обновленными данными или 404 если пользователь не найден
     * @throws BusinessRuleValidationException при попытке деактивации последнего администратора
     */
    @LoggableRequest
    @PatchMapping("/{id}/inactivate")
    public ResponseEntity<UserResponseDto> inactivateUser(@PathVariable("id") UUID id) {
        return userService.inactivateUser(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Обновляет роль пользователя.
     * Предотвращает изменение роли последнего активного администратора.
     * 
     * @param id UUID идентификатор пользователя
     * @param roleUpdate данные для обновления роли
     * @return ResponseEntity с обновленными данными или 404 если пользователь не найден
     * @throws BusinessRuleValidationException при попытке изменения роли последнего администратора
     */
    @LoggableRequest
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponseDto> updateUserRole(@PathVariable("id") UUID id, 
                                                         @RequestBody @Valid UserRoleUpdateDto roleUpdate) {
        return userService.updateUserRole(id, roleUpdate)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
