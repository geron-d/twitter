package com.twitter.service;

import com.twitter.UserFilter;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    /**
     * Создает нового пользователя
     * @param userRequest данные для создания пользователя
     * @return созданный пользователь
     */
    UserResponseDto createUser(UserRequestDto userRequest);

    /**
     * Обновляет существующего пользователя
     * @param id идентификатор пользователя
     * @param userDetails новые данные пользователя
     * @return обновленный пользователь или пустой Optional если пользователь не найден
     */
    Optional<UserResponseDto> updateUser(UUID id, UserRequestDto userDetails);

    /**
     * Получает пользователя по идентификатору
     * @param id идентификатор пользователя
     * @return пользователь или пустой Optional если не найден
     */
    Optional<UserResponseDto> getUserById(UUID id);

    /**
     * Получает всех пользователей с пагинацией
     * @param pageable параметры пагинации
     * @return страница пользователей
     */
    Page<UserResponseDto> findAll(UserFilter userFilter, Pageable pageable);
}
