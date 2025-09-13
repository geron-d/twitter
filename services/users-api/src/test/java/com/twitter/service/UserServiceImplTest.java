package com.twitter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.dto.UserResponseDto;
import com.twitter.dto.filter.UserFilter;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        void getUserById_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
            UUID nonExistentUserId = UUID.fromString("999e4567-e89b-12d3-a456-426614174999");

            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

            Optional<UserResponseDto> result = userService.getUserById(nonExistentUserId);

            assertThat(result).isEmpty();

            verify(userRepository).findById(nonExistentUserId);
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        @SuppressWarnings("null")
        void getUserById_WhenIdIsNull_ShouldReturnEmptyOptional() {
            when(userRepository.findById(null)).thenReturn(Optional.empty());

            Optional<UserResponseDto> result = userService.getUserById(null);

            assertThat(result).isEmpty();

            verify(userRepository).findById(null);
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        void getUserById_ShouldCallRepositoryAndMapperWithCorrectParameters() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserResponseDto(testUser)).thenReturn(testUserResponseDto);

            userService.getUserById(testUserId);

            verify(userRepository).findById(testUserId);
            verify(userMapper).toUserResponseDto(testUser);
        }
    }

    @Nested
    @SuppressWarnings("unchecked")
    class FindAllTest {

        private User testUser1;
        private User testUser2;
        private UserResponseDto testUserResponseDto1;
        private UserResponseDto testUserResponseDto2;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
            UUID userId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID userId2 = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");

            testUser1 = new User()
                    .setId(userId1)
                    .setLogin("user1")
                    .setFirstName("John")
                    .setLastName("Doe")
                    .setEmail("john@example.com")
                    .setPasswordHash("hashedPassword1")
                    .setPasswordSalt("salt1")
                    .setStatus(UserStatus.ACTIVE)
                    .setRole(UserRole.USER);

            testUser2 = new User()
                    .setId(userId2)
                    .setLogin("user2")
                    .setFirstName("Jane")
                    .setLastName("Smith")
                    .setEmail("jane@example.com")
                    .setPasswordHash("hashedPassword2")
                    .setPasswordSalt("salt2")
                    .setStatus(UserStatus.ACTIVE)
                    .setRole(UserRole.ADMIN);

            testUserResponseDto1 = new UserResponseDto(
                    userId1,
                    "user1",
                    "John",
                    "Doe",
                    "john@example.com",
                    UserStatus.ACTIVE,
                    UserRole.USER
            );

            testUserResponseDto2 = new UserResponseDto(
                    userId2,
                    "user2",
                    "Jane",
                    "Smith",
                    "jane@example.com",
                    UserStatus.ACTIVE,
                    UserRole.ADMIN
            );

            pageable = PageRequest.of(0, 10);
        }

        @Test
        void findAll_WhenUsersExist_ShouldReturnPageWithUsers() {
            List<User> users = List.of(testUser1, testUser2);
            Page<User> userPage = new PageImpl<>(users, pageable, 2);
            UserFilter userFilter = new UserFilter(null, null, null, null, null);

            when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
            when(userMapper.toUserResponseDto(testUser1)).thenReturn(testUserResponseDto1);
            when(userMapper.toUserResponseDto(testUser2)).thenReturn(testUserResponseDto2);

            Page<UserResponseDto> result = userService.findAll(userFilter, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(testUserResponseDto1, testUserResponseDto2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(10);

            verify(userRepository).findAll(any(Specification.class), eq(pageable));
            verify(userMapper).toUserResponseDto(testUser1);
            verify(userMapper).toUserResponseDto(testUser2);
        }

        @Test
        void findAll_WhenNoUsersExist_ShouldReturnEmptyPage() {
            Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            UserFilter userFilter = new UserFilter(null, null, null, null, null);

            when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

            Page<UserResponseDto> result = userService.findAll(userFilter, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(userRepository).findAll(any(Specification.class), eq(pageable));
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        void findAll_WithFirstNameFilter_ShouldCallRepositoryWithCorrectSpecification() {
            UserFilter userFilter = new UserFilter("John", null, null, null, null);
            List<User> users = List.of(testUser1);
            Page<User> userPage = new PageImpl<>(users, pageable, 1);

            when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
            when(userMapper.toUserResponseDto(testUser1)).thenReturn(testUserResponseDto1);

            Page<UserResponseDto> result = userService.findAll(userFilter, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(userRepository).findAll(any(Specification.class), eq(pageable));
            verify(userMapper).toUserResponseDto(testUser1);
        }

        @Test
        void findAll_WithRoleFilter_ShouldCallRepositoryWithCorrectSpecification() {
            UserFilter userFilter = new UserFilter(null, null, null, null, UserRole.ADMIN);
            List<User> users = List.of(testUser2);
            Page<User> userPage = new PageImpl<>(users, pageable, 1);

            when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
            when(userMapper.toUserResponseDto(testUser2)).thenReturn(testUserResponseDto2);

            Page<UserResponseDto> result = userService.findAll(userFilter, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(userRepository).findAll(any(Specification.class), eq(pageable));
            verify(userMapper).toUserResponseDto(testUser2);
        }

        @Test
        void findAll_WithEmptyFilter_ShouldCallRepositoryWithEmptySpecification() {
            UserFilter userFilter = new UserFilter(null, null, null, null, null);
            List<User> users = List.of(testUser1, testUser2);
            Page<User> userPage = new PageImpl<>(users, pageable, 2);

            when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
            when(userMapper.toUserResponseDto(testUser1)).thenReturn(testUserResponseDto1);
            when(userMapper.toUserResponseDto(testUser2)).thenReturn(testUserResponseDto2);

            Page<UserResponseDto> result = userService.findAll(userFilter, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);

            verify(userRepository).findAll(any(Specification.class), eq(pageable));
            verify(userMapper).toUserResponseDto(testUser1);
            verify(userMapper).toUserResponseDto(testUser2);
        }

        @Test
        void findAll_WithDifferentPageable_ShouldCallRepositoryWithCorrectPageable() {
            Pageable customPageable = PageRequest.of(1, 5);
            UserFilter userFilter = new UserFilter(null, null, null, null, null);
            List<User> users = List.of(testUser1);
            Page<User> userPage = new PageImpl<>(users, customPageable, 1);

            when(userRepository.findAll(any(Specification.class), eq(customPageable))).thenReturn(userPage);
            when(userMapper.toUserResponseDto(testUser1)).thenReturn(testUserResponseDto1);

            Page<UserResponseDto> result = userService.findAll(userFilter, customPageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(5);

            verify(userRepository).findAll(any(Specification.class), eq(customPageable));
            verify(userMapper).toUserResponseDto(testUser1);
        }

        @Test
        void findAll_ShouldCallRepositoryWithCorrectParameters() {
            UserFilter userFilter = new UserFilter("John", "Doe", "john@example.com", "user1", UserRole.USER);
            List<User> users = List.of(testUser1);
            Page<User> userPage = new PageImpl<>(users, pageable, 1);

            when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
            when(userMapper.toUserResponseDto(testUser1)).thenReturn(testUserResponseDto1);

            userService.findAll(userFilter, pageable);

            verify(userRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        void findAll_ShouldMapAllUsersToResponseDtos() {
            List<User> users = List.of(testUser1, testUser2);
            Page<User> userPage = new PageImpl<>(users, pageable, 2);
            UserFilter userFilter = new UserFilter(null, null, null, null, null);

            when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
            when(userMapper.toUserResponseDto(testUser1)).thenReturn(testUserResponseDto1);
            when(userMapper.toUserResponseDto(testUser2)).thenReturn(testUserResponseDto2);

            userService.findAll(userFilter, pageable);

            verify(userMapper).toUserResponseDto(testUser1);
            verify(userMapper).toUserResponseDto(testUser2);
        }
    }
}
