package com.twitter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.common.dto.request.UserRequestDto;
import com.twitter.common.dto.response.UserResponseDto;
import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
import com.twitter.dto.UserPatchDto;
import com.twitter.dto.UserRoleUpdateDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.dto.filter.UserFilter;
import com.twitter.entity.User;
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
 * Implementation of the user management service.
 * <p>
 * This service provides business logic for CRUD operations with users,
 * including creation, updating, deactivation, and role management. It handles
 * data validation, password hashing, and business rule enforcement.
 *
 * @author geron
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
     * @see UserService#getUserById
     */
    @Override
    public Optional<UserResponseDto> getUserById(UUID id) {
        return userRepository.findById(id).map(userMapper::toUserResponseDto);
    }

    /**
     * @see UserService#findAll
     */
    @Override
    public Page<UserResponseDto> findAll(UserFilter userFilter, Pageable pageable) {
        return userRepository.findAll(userFilter.toSpecification(), pageable)
            .map(userMapper::toUserResponseDto);
    }

    /**
     * @see UserService#createUser
     */
    @Override
    public UserResponseDto createUser(UserRequestDto userRequest) {
        userValidator.validateForCreate(userRequest);

        User user = userMapper.toUser(userRequest);
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(UserRole.USER);

        setPassword(user, userRequest.password());

        User savedUser = userRepository.saveAndFlush(user);
        return userMapper.toUserResponseDto(savedUser);
    }

    /**
     * @see UserService#updateUser
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
     * @see UserService#patchUser
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
     * @see UserService#inactivateUser
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
     * @see UserService#updateUserRole
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
     * @see UserService#existsById
     */
    @Override
    public boolean existsById(UUID id) {
        if (id == null) {
            return false;
        }

        return userRepository.existsById(id);
    }

    /**
     * Sets a hashed password for a user.
     * <p>
     * This private method generates a random salt and hashes the password
     * using PBKDF2 algorithm. It stores both the password hash and salt
     * in Base64 encoding for secure storage.
     *
     * @param user     the user to set the password for
     * @param password the password in plain text
     * @throws ResponseStatusException if salt generation or password hashing fails
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
