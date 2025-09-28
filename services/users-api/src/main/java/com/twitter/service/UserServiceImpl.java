package com.twitter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.dto.*;
import com.twitter.dto.filter.UserFilter;
import com.twitter.entity.User;
import com.twitter.enums.UserRole;
import com.twitter.enums.UserStatus;
import com.twitter.exception.validation.BusinessRuleValidationException;
import com.twitter.exception.validation.ValidationException;
import com.twitter.mapper.UserMapper;
import com.twitter.repository.UserRepository;
import com.twitter.util.PasswordUtil;
import com.twitter.util.PatchDtoFactory;
import com.twitter.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса управления пользователями.
 * Предоставляет бизнес-логику для CRUD операций с пользователями,
 * включая создание, обновление, деактивацию и управление ролями.
 * 
 * @author Twitter Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PatchDtoFactory patchDtoFactory;

    /**
     * Получает пользователя по уникальному идентификатору.
     * Возвращает Optional.empty() если пользователь не найден.
     * 
     * @param id уникальный идентификатор пользователя
     * @return Optional с данными пользователя или пустой Optional
     */
    @Override
    public Optional<UserResponseDto> getUserById(UUID id) {
        return userRepository.findById(id).map(userMapper::toUserResponseDto);
    }

    /**
     * Получает список пользователей с применением фильтров и пагинации.
     * Использует спецификации для динамической фильтрации по критериям.
     * 
     * @param userFilter фильтры для поиска пользователей
     * @param pageable параметры пагинации (размер страницы, номер страницы, сортировка)
     * @return страница с отфильтрованными пользователями
     */
    @Override
    public Page<UserResponseDto> findAll(UserFilter userFilter, Pageable pageable) {
        return userRepository.findAll(userFilter.toSpecification(), pageable)
            .map(userMapper::toUserResponseDto);
    }

    /**
     * Создает нового пользователя в системе.
     * Выполняет валидацию данных, устанавливает статус ACTIVE и роль USER,
     * хеширует пароль с использованием соли.
     * 
     * @param userRequest DTO с данными для создания пользователя
     * @return данные созданного пользователя
     * @throws ValidationException при нарушении валидации данных
     * @throws ResponseStatusException при ошибке хеширования пароля
     */
    @Override
    public UserResponseDto createUser(UserRequestDto userRequest) {
        userValidator.validateForCreate(userRequest);

        User user = userMapper.toUser(userRequest);
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(UserRole.USER);

        setPassword(user, userRequest.password());

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(savedUser);
    }

    /**
     * Обновляет данные существующего пользователя.
     * Выполняет валидацию данных с исключением текущего пользователя из проверки уникальности.
     * Обновляет пароль только если он указан в запросе.
     * 
     * @param id уникальный идентификатор пользователя
     * @param userDetails DTO с новыми данными пользователя
     * @return Optional с обновленными данными пользователя или пустой Optional если пользователь не найден
     * @throws ValidationException при нарушении валидации данных
     * @throws ResponseStatusException при ошибке хеширования пароля
     */
    @Override
    public Optional<UserResponseDto> updateUser(UUID id, UserUpdateDto userDetails) {
        return userRepository.findById(id).map(user -> {
            userValidator.validateForUpdate(id, userDetails);

            userMapper.updateUserFromUpdateDto(userDetails, user);

            if (userDetails.password() != null && !userDetails.password().isEmpty()) {
                setPassword(user, userDetails.password());
            }

            User updatedUser = userRepository.save(user);
            return userMapper.toUserResponseDto(updatedUser);
        });
    }

    /**
     * Частично обновляет данные пользователя с использованием JSON Patch.
     * Выполняет двухэтапную валидацию: структуры JSON и бизнес-правил.
     * Применяет изменения только к указанным полям.
     * 
     * @param id уникальный идентификатор пользователя
     * @param patchNode JSON данные для частичного обновления
     * @return Optional с обновленными данными пользователя или пустой Optional если пользователь не найден
     * @throws ValidationException при нарушении валидации JSON структуры или бизнес-правил
     */
    @Override
    public Optional<UserResponseDto> patchUser(UUID id, JsonNode patchNode) {
        return userRepository.findById(id).map(user -> {
            userValidator.validateForPatch(id, patchNode);

            UserPatchDto userPatchDto = userMapper.toUserPatchDto(user);
            userPatchDto = patchDtoFactory.createPatchDto(userPatchDto, patchNode);

            userValidator.validateForPatchWithDto(id, userPatchDto);

            userMapper.updateUserFromPatchDto(userPatchDto, user);

            User updatedUser = userRepository.save(user);
            return userMapper.toUserResponseDto(updatedUser);
        });
    }

    /**
     * Деактивирует пользователя, устанавливая статус INACTIVE.
     * Выполняет проверку бизнес-правил для предотвращения деактивации последнего администратора.
     * Логирует успешную деактивацию.
     * 
     * @param id уникальный идентификатор пользователя
     * @return Optional с данными деактивированного пользователя или пустой Optional если пользователь не найден
     * @throws BusinessRuleValidationException при попытке деактивации последнего активного администратора
     */
    @Override
    public Optional<UserResponseDto> inactivateUser(UUID id) {
        return userRepository.findById(id).map(user -> {
            userValidator.validateAdminDeactivation(id);

            user.setStatus(UserStatus.INACTIVE);
            User updatedUser = userRepository.save(user);
            log.info("User with ID {} has been successfully deactivated", id);
            return userMapper.toUserResponseDto(updatedUser);
        });
    }

    /**
     * Обновляет роль пользователя в системе.
     * Выполняет проверку бизнес-правил для предотвращения смены роли последнего администратора.
     * Логирует изменение роли с указанием старой и новой роли.
     * 
     * @param id уникальный идентификатор пользователя
     * @param roleUpdate DTO с новой ролью пользователя
     * @return Optional с обновленными данными пользователя или пустой Optional если пользователь не найден
     * @throws BusinessRuleValidationException при попытке смены роли последнего активного администратора
     */
    @Override
    public Optional<UserResponseDto> updateUserRole(UUID id, UserRoleUpdateDto roleUpdate) {
        return userRepository.findById(id).map(user -> {
            UserRole oldRole = user.getRole();
            UserRole newRole = roleUpdate.role();

            userValidator.validateRoleChange(id, newRole);

            user.setRole(newRole);
            User updatedUser = userRepository.save(user);

            log.info("User role updated for ID {}: {} -> {}", id, oldRole, newRole);
            return userMapper.toUserResponseDto(updatedUser);
        });
    }

    /**
     * Устанавливает хешированный пароль для пользователя.
     * Генерирует случайную соль и хеширует пароль с использованием PBKDF2.
     * Сохраняет хеш пароля и соль в Base64 кодировке.
     * 
     * @param user пользователь для установки пароля
     * @param password пароль в открытом виде
     * @throws ResponseStatusException при ошибке генерации соли или хеширования пароля
     */
    private void setPassword(User user, String password) {
        try {
            byte[] salt = PasswordUtil.getSalt();
            String hashedPassword = PasswordUtil.hashPassword(password, salt);
            user.setPasswordHash(hashedPassword);
            user.setPasswordSalt(Base64.getEncoder().encodeToString(salt));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error hashing password: " + e.getMessage(), e);
        }
    }
}
