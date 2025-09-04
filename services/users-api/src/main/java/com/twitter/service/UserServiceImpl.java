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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

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
                throw new RuntimeException("Error patching user", e);
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
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
