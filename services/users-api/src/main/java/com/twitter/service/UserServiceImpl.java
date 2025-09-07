package com.twitter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.dto.UserPatchDto;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
import com.twitter.dto.filter.UserFilter;
import com.twitter.entity.User;
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
//    private final TracingUtil tracingUtil;

    @Override
    public Optional<UserResponseDto> getUserById(UUID id) {
//        log.debug("Looking up user by ID: {}", id);
//        return tracingUtil.executeInSpan("get-user-by-id", Map.of(
//            "user.id", id.toString(),
//            "operation", "GET_USER_BY_ID"
//        ), () -> {
//            tracingUtil.addEvent("database-query-started");
//            Optional<User> user = userRepository.findById(id);
//            tracingUtil.addEvent("database-query-completed");
//            tracingUtil.addTag("user.found", String.valueOf(user.isPresent()));
//
//            return user.map(userMapper::toUserResponseDto);
//        });
        return userRepository.findById(id).map(userMapper::toUserResponseDto);
    }

    @Override
    public Page<UserResponseDto> findAll(UserFilter userFilter, Pageable pageable) {
//        log.debug("Finding users with filter: {}", userFilter);
//        return tracingUtil.executeInSpan("find-all-users", Map.of(
//            "page.size", String.valueOf(pageable.getPageSize()),
//            "page.number", String.valueOf(pageable.getPageNumber()),
//            "operation", "FIND_ALL_USERS"
//        ), () -> {
//            tracingUtil.addEvent("database-query-started");
//            Page<User> users = userRepository.findAll(userFilter.toSpecification(), pageable);
//            tracingUtil.addEvent("database-query-completed");
//            tracingUtil.addTag("result.count", String.valueOf(users.getTotalElements()));
//
//            return users.map(userMapper::toUserResponseDto);
//        });
        return userRepository.findAll(userFilter.toSpecification(), pageable)
            .map(userMapper::toUserResponseDto);
    }

    @Override
    public UserResponseDto createUser(UserRequestDto userRequest) {
//        log.info("Creating new user with email: {}", userRequest.email());
//        return tracingUtil.executeInSpan("create-user", Map.of(
//            "user.email", userRequest.email(),
//            "user.login", userRequest.login(),
//            "operation", "CREATE_USER"
//        ), () -> {
//            tracingUtil.addEvent("user-creation-started");
//
//            User user = userMapper.toUser(userRequest);
//            user.setStatus(UserStatus.ACTIVE);
//
//            tracingUtil.addEvent("password-hashing-started");
//            setPassword(user, userRequest.password());
//            tracingUtil.addEvent("password-hashing-completed");
//
//            tracingUtil.addEvent("database-save-started");
//            User savedUser = userRepository.save(user);
//            tracingUtil.addEvent("database-save-completed");
//            tracingUtil.addTag("user.id", savedUser.getId().toString());
//
//            return userMapper.toUserResponseDto(savedUser);
//        });
        User user = userMapper.toUser(userRequest);
        user.setStatus(UserStatus.ACTIVE);

        setPassword(user, userRequest.password());

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    public Optional<UserResponseDto> updateUser(UUID id, UserRequestDto userDetails) {
        return userRepository.findById(id).map(user -> {
            userMapper.updateUserFromDto(userDetails, user);

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
            userMapper.updateUserFromPatchDto(userPatchDto, user);

            User updatedUser = userRepository.save(user);
            return userMapper.toUserResponseDto(updatedUser);
        });
    }

    @Override
    public Optional<UserResponseDto> inactivateUser(UUID id) {
        return userRepository.findById(id).map(user -> {
            user.setStatus(UserStatus.INACTIVE);
            User updatedUser = userRepository.save(user);
            return userMapper.toUserResponseDto(updatedUser);
        });
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
