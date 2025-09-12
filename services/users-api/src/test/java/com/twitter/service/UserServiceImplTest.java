package com.twitter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.dto.UserResponseDto;
import com.twitter.entity.User;
import com.twitter.enums.UserRole;
import com.twitter.enums.UserStatus;
import com.twitter.mapper.UserMapper;
import com.twitter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("getUserById method tests")
    class GetUserByIdTest {

        private User testUser;
        private UserResponseDto testUserResponseDto;
        private UUID testUserId;

        @BeforeEach
        void setUp() {
            testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            
            testUser = new User()
                    .setId(testUserId)
                    .setLogin("testuser")
                    .setFirstName("Test")
                    .setLastName("User")
                    .setEmail("test@example.com")
                    .setPasswordHash("hashedPassword")
                    .setPasswordSalt("salt")
                    .setStatus(UserStatus.ACTIVE)
                    .setRole(UserRole.USER);

            testUserResponseDto = new UserResponseDto(
                    testUserId,
                    "testuser",
                    "Test",
                    "User",
                    "test@example.com",
                    UserStatus.ACTIVE,
                    UserRole.USER
            );
        }

        @Test
        @DisplayName("Should return user when user exists with given ID")
        void getUserById_WhenUserExists_ShouldReturnUser() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserResponseDto(testUser)).thenReturn(testUserResponseDto);

            Optional<UserResponseDto> result = userService.getUserById(testUserId);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testUserResponseDto);

            verify(userRepository).findById(testUserId);
            verify(userMapper).toUserResponseDto(testUser);
        }

        @Test
        @DisplayName("Should return empty Optional when user does not exist")
        void getUserById_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
            UUID nonExistentUserId = UUID.fromString("999e4567-e89b-12d3-a456-426614174999");

            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

            Optional<UserResponseDto> result = userService.getUserById(nonExistentUserId);

            assertThat(result).isEmpty();

            verify(userRepository).findById(nonExistentUserId);
            verify(userMapper, org.mockito.Mockito.never()).toUserResponseDto(any());
        }

        @Test
        @DisplayName("Should return empty Optional when ID is null")
        @SuppressWarnings("null")
        void getUserById_WhenIdIsNull_ShouldReturnEmptyOptional() {
            when(userRepository.findById(null)).thenReturn(Optional.empty());

            Optional<UserResponseDto> result = userService.getUserById(null);

            assertThat(result).isEmpty();

            verify(userRepository).findById(null);
            verify(userMapper, org.mockito.Mockito.never()).toUserResponseDto(any());
        }

        @Test
        @DisplayName("Should call repository and mapper with correct parameters")
        void getUserById_ShouldCallRepositoryAndMapperWithCorrectParameters() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserResponseDto(testUser)).thenReturn(testUserResponseDto);

            userService.getUserById(testUserId);

            verify(userRepository).findById(testUserId);
            verify(userMapper).toUserResponseDto(testUser);
        }
    }
}
