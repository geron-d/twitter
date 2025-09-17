package com.twitter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.common.exception.LastAdminDeactivationException;
import com.twitter.dto.UserPatchDto;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
import com.twitter.dto.UserRoleUpdateDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.dto.filter.UserFilter;
import com.twitter.entity.User;
import com.twitter.enums.UserRole;
import com.twitter.enums.UserStatus;
import com.twitter.mapper.UserMapper;
import com.twitter.repository.UserRepository;
import com.twitter.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public Optional<UserResponseDto> getUserById(UUID id) {
        return userRepository.findById(id).map(userMapper::toUserResponseDto);
    }

    @Override
    public Page<UserResponseDto> findAll(UserFilter userFilter, Pageable pageable) {
        return userRepository.findAll(userFilter.toSpecification(), pageable)
            .map(userMapper::toUserResponseDto);
    }

    @Override
    public UserResponseDto createUser(UserRequestDto userRequest) {
        validateUserUniqueness(userRequest.login(), userRequest.email(), null);
        
        User user = userMapper.toUser(userRequest);
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(UserRole.USER);

        setPassword(user, userRequest.password());

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    public Optional<UserResponseDto> updateUser(UUID id, UserUpdateDto userDetails) {
        return userRepository.findById(id).map(user -> {
            validateUserUniqueness(userDetails.login(), userDetails.email(), id);
            
            userMapper.updateUserFromUpdateDto(userDetails, user);

            if (userDetails.password() != null && !userDetails.password().isEmpty()) {
                setPassword(user, userDetails.password());
            }

            User updatedUser = userRepository.save(user);
            return userMapper.toUserResponseDto(updatedUser);
        });
    }

    @Override
    public Optional<UserResponseDto> patchUser(UUID id, JsonNode patchNode) {
        return userRepository.findById(id).map(user -> {
            UserPatchDto userPatchDto = userMapper.toUserPatchDto(user);

            try {
                objectMapper.readerForUpdating(userPatchDto).readValue(patchNode);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error patching user: " + e.getMessage(), e);
            }
            
            validateUserUniqueness(userPatchDto.getLogin(), userPatchDto.getEmail(), id);
            userMapper.updateUserFromPatchDto(userPatchDto, user);

            User updatedUser = userRepository.save(user);
            return userMapper.toUserResponseDto(updatedUser);
        });
    }

    @Override
    public Optional<UserResponseDto> inactivateUser(UUID id) {
        return userRepository.findById(id).map(user -> {
            if (user.getRole() == UserRole.ADMIN) {
                long activeAdminCount = userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
                if (activeAdminCount <= 1) {
                    log.warn("Attempt to deactivate the last active administrator with ID: {}", id);
                    throw new LastAdminDeactivationException("Cannot deactivate the last active administrator");
                }
            }
            
            user.setStatus(UserStatus.INACTIVE);
            User updatedUser = userRepository.save(user);
            log.info("User with ID {} has been successfully deactivated", id);
            return userMapper.toUserResponseDto(updatedUser);
        });
    }

    @Override
    public Optional<UserResponseDto> updateUserRole(UUID id, UserRoleUpdateDto roleUpdate) {
        return userRepository.findById(id).map(user -> {
            UserRole oldRole = user.getRole();
            UserRole newRole = roleUpdate.role();
            
            // Проверяем, что нельзя удалить последнего активного админа
            if (oldRole == UserRole.ADMIN && newRole != UserRole.ADMIN) {
                long activeAdminCount = userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
                if (activeAdminCount <= 1) {
                    log.warn("Attempt to change role of the last active administrator with ID: {} from {} to {}", 
                            id, oldRole, newRole);
                    throw new LastAdminDeactivationException("Cannot change role of the last active administrator");
                }
            }
            
            user.setRole(newRole);
            User updatedUser = userRepository.save(user);
            
            log.info("User role updated for ID {}: {} -> {}", id, oldRole, newRole);
            return userMapper.toUserResponseDto(updatedUser);
        });
    }

    /**
     * Проверяет уникальность логина и email пользователя
     *
     * @param login         логин для проверки
     * @param email         email для проверки
     * @param excludeUserId ID пользователя, который исключается из проверки (для случаев обновления)
     */
    private void validateUserUniqueness(String login, String email, UUID excludeUserId) {
        if (!ObjectUtils.isEmpty(login)) {
            boolean loginExists = excludeUserId != null 
                ? userRepository.existsByLoginAndIdNot(login, excludeUserId)
                : userRepository.existsByLogin(login);
            
            if (loginExists) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User with login '" + login + "' already exists");
            }
        }

        if (!ObjectUtils.isEmpty(email)) {
            boolean emailExists = excludeUserId != null 
                ? userRepository.existsByEmailAndIdNot(email, excludeUserId)
                : userRepository.existsByEmail(email);
            
            if (emailExists) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User with email '" + email + "' already exists");
            }
        }
    }

    /**
     * Устанавливает хешированный пароль для пользователя
     *
     * @param user     пользователь
     * @param password пароль в открытом виде
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
