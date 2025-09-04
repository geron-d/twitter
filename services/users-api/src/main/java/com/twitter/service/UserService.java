package com.twitter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
import com.twitter.dto.filter.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    /**
     * Получает пользователя по идентификатору
     *
     * @param id идентификатор пользователя
     * @return пользователь или пустой Optional если не найден
     */
    Optional<UserResponseDto> getUserById(UUID id);

    /**
     * Получает всех пользователей с пагинацией
     *
     * @param pageable параметры пагинации
     * @return страница пользователей
     */
    Page<UserResponseDto> findAll(UserFilter userFilter, Pageable pageable);

    /**
     * Создает нового пользователя
     *
     * @param userRequest данные для создания пользователя
     * @return созданный пользователь
     */
    UserResponseDto createUser(UserRequestDto userRequest);

    /**
     * Обновляет существующего пользователя
     *
     * @param id          идентификатор пользователя
     * @param userDetails новые данные пользователя
     * @return обновленный пользователь или пустой Optional если пользователь не найден
     */
    Optional<UserResponseDto> updateUser(UUID id, UserRequestDto userDetails);

    Optional<UserResponseDto> patchUser(UUID id, JsonNode patchNode);

    /**
     * Деактивирует пользователя
     *
     * @param id идентификатор пользователя
     * @return обновленный пользователь или пустой Optional если пользователь не найден
     */
    Optional<UserResponseDto> inactivateUser(UUID id);
}
