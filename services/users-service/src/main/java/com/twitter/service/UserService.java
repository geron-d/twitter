package com.twitter.service;

import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
import com.twitter.entity.User;
import com.twitter.mapper.UserMapper;
import com.twitter.repository.UserRepository;
import com.twitter.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserResponseDto createUser(UserRequestDto userRequest) {
        User user = userMapper.toUser(userRequest);

        setPassword(user, userRequest.password());

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(savedUser);
    }

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

    public Optional<UserResponseDto> getUserById(UUID id) {
        return userRepository.findById(id).map(userMapper::toUserResponseDto);
    }

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
