package com.twitter.mapper;


import com.twitter.dto.UserPatchDto;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct маппер для преобразования между сущностями User и DTO объектами.
 * Обеспечивает автоматическое преобразование данных между слоями приложения
 * с настройкой игнорирования служебных полей.
 * 
 * @author Twitter Team
 * @version 1.0
 */
@Mapper
public interface UserMapper {

    /**
     * Преобразует DTO запроса в сущность User.
     * Игнорирует поле passwordHash, так как пароль обрабатывается отдельно
     * с хешированием и генерацией соли.
     * 
     * @param userRequestDto DTO с данными для создания пользователя
     * @return сущность User без хеша пароля
     */
    @Mapping(target = "passwordHash", ignore = true)
    User toUser(UserRequestDto userRequestDto);

    /**
     * Преобразует сущность User в DTO ответа.
     * Автоматически исключает чувствительные поля (passwordHash, passwordSalt)
     * для безопасности при передаче данных клиенту.
     * 
     * @param user сущность пользователя из базы данных
     * @return DTO с данными пользователя для ответа клиенту
     */
    UserResponseDto toUserResponseDto(User user);

    /**
     * Преобразует сущность User в DTO для PATCH операций.
     * Создает промежуточный объект для применения JSON Patch изменений
     * с сохранением текущих значений полей пользователя.
     * 
     * @param user сущность пользователя из базы данных
     * @return DTO для применения PATCH изменений
     */
    UserPatchDto toUserPatchDto(User user);

    /**
     * Обновляет сущность User данными из DTO PATCH операций.
     * Применяет только измененные поля к существующей сущности,
     * сохраняя неизменные поля без модификации.
     * 
     * @param userPatchDto DTO с данными для частичного обновления
     * @param user целевая сущность User для обновления
     */
    void updateUserFromPatchDto(UserPatchDto userPatchDto, @MappingTarget User user);

    /**
     * Обновляет сущность User данными из DTO полного обновления.
     * Игнорирует служебные поля: id, passwordHash, passwordSalt, status, role.
     * Эти поля управляются отдельно через бизнес-логику сервиса.
     * 
     * @param userUpdateDto DTO с данными для полного обновления
     * @param user целевая сущность User для обновления
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "passwordSalt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateUserFromUpdateDto(UserUpdateDto userUpdateDto, @MappingTarget User user);
}
