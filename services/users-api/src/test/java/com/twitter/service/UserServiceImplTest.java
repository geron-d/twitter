package com.twitter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.twitter.dto.*;
import com.twitter.dto.filter.UserFilter;
import com.twitter.entity.User;
import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.mapper.UserMapper;
import com.twitter.repository.UserRepository;
import com.twitter.util.PatchDtoFactory;
import com.twitter.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserValidator userValidator;

    @Mock
    private PatchDtoFactory patchDtoFactory;

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
                UserRole.USER,
                LocalDateTime.now()
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
                UserRole.USER,
                LocalDateTime.now()
            );

            testUserResponseDto2 = new UserResponseDto(
                userId2,
                "user2",
                "Jane",
                "Smith",
                "jane@example.com",
                UserStatus.ACTIVE,
                UserRole.ADMIN,
                LocalDateTime.now()
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

    @Nested
    class CreateUserTest {

        private UserRequestDto testUserRequestDto;
        private User testUser;
        private User savedUser;
        private UserResponseDto testUserResponseDto;

        @BeforeEach
        void setUp() {
            testUserRequestDto = new UserRequestDto(
                "testuser",
                "Test",
                "User",
                "test@example.com",
                "password123"
            );

            testUser = new User()
                .setLogin("testuser")
                .setFirstName("Test")
                .setLastName("User")
                .setEmail("test@example.com");

            savedUser = new User()
                .setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .setLogin("testuser")
                .setFirstName("Test")
                .setLastName("User")
                .setEmail("test@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            testUserResponseDto = new UserResponseDto(
                savedUser.getId(),
                "testuser",
                "Test",
                "User",
                "test@example.com",
                UserStatus.ACTIVE,
                UserRole.USER,
                LocalDateTime.now()
            );
        }

        @Test
        void createUser_WithValidData_ShouldCreateAndReturnUser() {
            when(userMapper.toUser(testUserRequestDto)).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.toUserResponseDto(savedUser)).thenReturn(testUserResponseDto);

            UserResponseDto result = userService.createUser(testUserRequestDto);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testUserResponseDto);
            assertThat(result.id()).isEqualTo(savedUser.getId());
            assertThat(result.login()).isEqualTo("testuser");
            assertThat(result.firstName()).isEqualTo("Test");
            assertThat(result.lastName()).isEqualTo("User");
            assertThat(result.email()).isEqualTo("test@example.com");
            assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
            assertThat(result.role()).isEqualTo(UserRole.USER);

            verify(userValidator).validateForCreate(testUserRequestDto);
            verify(userMapper).toUser(testUserRequestDto);
            verify(userRepository).save(any(User.class));
            verify(userMapper).toUserResponseDto(savedUser);
        }

        @Test
        void createUser_ShouldSetStatusToActive() {
            when(userMapper.toUser(testUserRequestDto)).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
                return savedUser;
            });
            when(userMapper.toUserResponseDto(savedUser)).thenReturn(testUserResponseDto);

            userService.createUser(testUserRequestDto);

            verify(userValidator).validateForCreate(testUserRequestDto);
            verify(userMapper).toUser(testUserRequestDto);
            verify(userRepository).save(any(User.class));
            verify(userMapper).toUserResponseDto(savedUser);
        }

        @Test
        void createUser_ShouldSetRoleToUser() {
            when(userMapper.toUser(testUserRequestDto)).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                assertThat(user.getRole()).isEqualTo(UserRole.USER);
                return savedUser;
            });
            when(userMapper.toUserResponseDto(savedUser)).thenReturn(testUserResponseDto);

            userService.createUser(testUserRequestDto);

            verify(userValidator).validateForCreate(testUserRequestDto);
            verify(userMapper).toUser(testUserRequestDto);
            verify(userRepository).save(any(User.class));
            verify(userMapper).toUserResponseDto(savedUser);
        }

        @Test
        void createUser_ShouldHashPassword() {
            when(userMapper.toUser(testUserRequestDto)).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                assertThat(user.getPasswordHash()).isNotNull();
                assertThat(user.getPasswordSalt()).isNotNull();
                assertThat(user.getPasswordHash()).isNotEqualTo("password123");
                return savedUser;
            });
            when(userMapper.toUserResponseDto(savedUser)).thenReturn(testUserResponseDto);

            userService.createUser(testUserRequestDto);

            verify(userValidator).validateForCreate(testUserRequestDto);
            verify(userMapper).toUser(testUserRequestDto);
            verify(userRepository).save(any(User.class));
            verify(userMapper).toUserResponseDto(savedUser);
        }

        @Test
        void createUser_WithMinimalData_ShouldCreateUser() {
            UserRequestDto minimalRequest = new UserRequestDto(
                "minuser",
                null,
                null,
                "min@example.com",
                "password123"
            );

            User minimalUser = new User()
                .setLogin("minuser")
                .setEmail("min@example.com");

            User savedMinimalUser = new User()
                .setId(UUID.fromString("223e4567-e89b-12d3-a456-426614174001"))
                .setLogin("minuser")
                .setEmail("min@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            UserResponseDto minimalResponse = new UserResponseDto(
                savedMinimalUser.getId(),
                "minuser",
                null,
                null,
                "min@example.com",
                UserStatus.ACTIVE,
                UserRole.USER,
                LocalDateTime.now()
            );

            when(userMapper.toUser(minimalRequest)).thenReturn(minimalUser);
            when(userRepository.save(any(User.class))).thenReturn(savedMinimalUser);
            when(userMapper.toUserResponseDto(savedMinimalUser)).thenReturn(minimalResponse);

            UserResponseDto result = userService.createUser(minimalRequest);

            assertThat(result).isNotNull();
            assertThat(result.login()).isEqualTo("minuser");
            assertThat(result.firstName()).isNull();
            assertThat(result.lastName()).isNull();
            assertThat(result.email()).isEqualTo("min@example.com");
            assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
            assertThat(result.role()).isEqualTo(UserRole.USER);

            verify(userValidator).validateForCreate(minimalRequest);
            verify(userMapper).toUser(minimalRequest);
            verify(userRepository).save(any(User.class));
            verify(userMapper).toUserResponseDto(savedMinimalUser);
        }

        @Test
        void createUser_WhenLoginExists_ShouldThrowUniquenessValidationException() {
            doThrow(new UniquenessValidationException("login", "testuser"))
                .when(userValidator).validateForCreate(testUserRequestDto);

            assertThatThrownBy(() -> userService.createUser(testUserRequestDto))
                .isInstanceOf(UniquenessValidationException.class)
                .hasMessageContaining("User with login 'testuser' already exists");

            verify(userValidator).validateForCreate(testUserRequestDto);
            verify(userMapper, never()).toUser(any());
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        void createUser_WhenEmailExists_ShouldThrowUniquenessValidationException() {
            doThrow(new UniquenessValidationException("email", "test@example.com"))
                .when(userValidator).validateForCreate(testUserRequestDto);

            assertThatThrownBy(() -> userService.createUser(testUserRequestDto))
                .isInstanceOf(UniquenessValidationException.class)
                .hasMessageContaining("User with email 'test@example.com' already exists");

            verify(userValidator).validateForCreate(testUserRequestDto);
            verify(userMapper, never()).toUser(any());
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserResponseDto(any());
        }
    }

    @Nested
    class UpdateUserTest {

        private User testUser;
        private User updatedUser;
        private UserResponseDto testUserResponseDto;
        private UserUpdateDto testUserUpdateDto;
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
                .setPasswordHash("oldHashedPassword")
                .setPasswordSalt("oldSalt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            updatedUser = new User()
                .setId(testUserId)
                .setLogin("updateduser")
                .setFirstName("Updated")
                .setLastName("User")
                .setEmail("updated@example.com")
                .setPasswordHash("newHashedPassword")
                .setPasswordSalt("newSalt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            testUserUpdateDto = new UserUpdateDto(
                "updateduser",
                "Updated",
                "User",
                "updated@example.com",
                "newPassword123"
            );

            testUserResponseDto = new UserResponseDto(
                testUserId,
                "updateduser",
                "Updated",
                "User",
                "updated@example.com",
                UserStatus.ACTIVE,
                UserRole.USER,
                LocalDateTime.now()
            );
        }

        @Test
        void updateUser_WhenUserExists_ShouldUpdateAndReturnUser() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);
            when(userMapper.toUserResponseDto(updatedUser)).thenReturn(testUserResponseDto);

            Optional<UserResponseDto> result = userService.updateUser(testUserId, testUserUpdateDto);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testUserResponseDto);
            assertThat(result.get().id()).isEqualTo(testUserId);
            assertThat(result.get().login()).isEqualTo("updateduser");
            assertThat(result.get().firstName()).isEqualTo("Updated");
            assertThat(result.get().lastName()).isEqualTo("User");
            assertThat(result.get().email()).isEqualTo("updated@example.com");

            verify(userValidator).validateForUpdate(testUserId, testUserUpdateDto);
            verify(userRepository).findById(testUserId);
            verify(userMapper).updateUserFromUpdateDto(testUserUpdateDto, testUser);
            verify(userRepository).save(testUser);
            verify(userMapper).toUserResponseDto(updatedUser);
        }

        @Test
        void updateUser_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
            UUID nonExistentUserId = UUID.fromString("999e4567-e89b-12d3-a456-426614174999");

            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

            Optional<UserResponseDto> result = userService.updateUser(nonExistentUserId, testUserUpdateDto);

            assertThat(result).isEmpty();

            verify(userRepository).findById(nonExistentUserId);
            verify(userMapper, never()).updateUserFromUpdateDto(any(), any());
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        void updateUser_WhenUserUpdateDtoIsNull_ShouldThrowException() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> userService.updateUser(testUserId, null))
                .isInstanceOf(NullPointerException.class);

            verify(userRepository).findById(testUserId);
        }

        @Test
        void updateUser_WithMinimalUpdateData_ShouldUpdateUser() {
            UserUpdateDto minimalUpdateDto = new UserUpdateDto(
                "newlogin",
                null,
                null,
                "newemail@example.com",
                null
            );

            User minimalUpdatedUser = new User()
                .setId(testUserId)
                .setLogin("newlogin")
                .setFirstName("Test")
                .setLastName("User")
                .setEmail("newemail@example.com")
                .setPasswordHash("oldHashedPassword")
                .setPasswordSalt("oldSalt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            UserResponseDto minimalResponseDto = new UserResponseDto(
                testUserId,
                "newlogin",
                "Test",
                "User",
                "newemail@example.com",
                UserStatus.ACTIVE,
                UserRole.USER,
                LocalDateTime.now()
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(minimalUpdatedUser);
            when(userMapper.toUserResponseDto(minimalUpdatedUser)).thenReturn(minimalResponseDto);

            Optional<UserResponseDto> result = userService.updateUser(testUserId, minimalUpdateDto);

            assertThat(result).isPresent();
            assertThat(result.get().login()).isEqualTo("newlogin");
            assertThat(result.get().firstName()).isEqualTo("Test");
            assertThat(result.get().lastName()).isEqualTo("User");
            assertThat(result.get().email()).isEqualTo("newemail@example.com");

            verify(userRepository).findById(testUserId);
            verify(userMapper).updateUserFromUpdateDto(minimalUpdateDto, testUser);
            verify(userRepository).save(testUser);
            verify(userMapper).toUserResponseDto(minimalUpdatedUser);
        }

        @Test
        void updateUser_WithValidPassword_ShouldHashPassword() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                assertThat(user.getPasswordHash()).isNotNull();
                assertThat(user.getPasswordSalt()).isNotNull();
                assertThat(user.getPasswordHash()).isNotEqualTo("oldHashedPassword");
                assertThat(user.getPasswordHash()).isNotEqualTo("newPassword123");
                return updatedUser;
            });
            when(userMapper.toUserResponseDto(updatedUser)).thenReturn(testUserResponseDto);

            Optional<UserResponseDto> result = userService.updateUser(testUserId, testUserUpdateDto);

            assertThat(result).isPresent();
            verify(userRepository).findById(testUserId);
            verify(userMapper).updateUserFromUpdateDto(testUserUpdateDto, testUser);
            verify(userRepository).save(testUser);
            verify(userMapper).toUserResponseDto(updatedUser);
        }

        @Test
        void updateUser_WithExistingLogin_ShouldThrowUniquenessValidationException() {
            UserUpdateDto updateDtoWithExistingLogin = new UserUpdateDto(
                "existinguser", // логин, который уже существует
                "Updated",
                "User",
                "updated@example.com",
                "newPassword123"
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            doThrow(new UniquenessValidationException("login", "existinguser"))
                .when(userValidator).validateForUpdate(testUserId, updateDtoWithExistingLogin);

            assertThatThrownBy(() -> userService.updateUser(testUserId, updateDtoWithExistingLogin))
                .isInstanceOf(UniquenessValidationException.class)
                .hasMessageContaining("User with login 'existinguser' already exists");

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateForUpdate(testUserId, updateDtoWithExistingLogin);
            verify(userMapper, never()).updateUserFromUpdateDto(any(), any());
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        void updateUser_WithExistingEmail_ShouldThrowUniquenessValidationException() {
            UserUpdateDto updateDtoWithExistingEmail = new UserUpdateDto(
                "updateduser",
                "Updated",
                "User",
                "existing@example.com", // email, который уже существует
                "newPassword123"
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            doThrow(new UniquenessValidationException("email", "existing@example.com"))
                .when(userValidator).validateForUpdate(testUserId, updateDtoWithExistingEmail);

            assertThatThrownBy(() -> userService.updateUser(testUserId, updateDtoWithExistingEmail))
                .isInstanceOf(UniquenessValidationException.class)
                .hasMessageContaining("User with email 'existing@example.com' already exists");

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateForUpdate(testUserId, updateDtoWithExistingEmail);
            verify(userMapper, never()).updateUserFromUpdateDto(any(), any());
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        void updateUser_WithSameLoginAndEmail_ShouldUpdateUser() {
            UserUpdateDto updateDtoWithSameData = new UserUpdateDto(
                "testuser", // тот же логин
                "Updated",
                "User",
                "test@example.com", // тот же email
                "newPassword123"
            );

            User updatedUserWithSameData = new User()
                .setId(testUserId)
                .setLogin("testuser")
                .setFirstName("Updated")
                .setLastName("User")
                .setEmail("test@example.com")
                .setPasswordHash("newHashedPassword")
                .setPasswordSalt("newSalt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            UserResponseDto responseDtoWithSameData = new UserResponseDto(
                testUserId,
                "testuser",
                "Updated",
                "User",
                "test@example.com",
                UserStatus.ACTIVE,
                UserRole.USER,
                LocalDateTime.now()
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedUserWithSameData);
            when(userMapper.toUserResponseDto(updatedUserWithSameData)).thenReturn(responseDtoWithSameData);

            Optional<UserResponseDto> result = userService.updateUser(testUserId, updateDtoWithSameData);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(responseDtoWithSameData);
            assertThat(result.get().login()).isEqualTo("testuser");
            assertThat(result.get().email()).isEqualTo("test@example.com");
            assertThat(result.get().firstName()).isEqualTo("Updated");

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateForUpdate(testUserId, updateDtoWithSameData);
            verify(userMapper).updateUserFromUpdateDto(updateDtoWithSameData, testUser);
            verify(userRepository).save(testUser);
            verify(userMapper).toUserResponseDto(updatedUserWithSameData);
        }
    }

    @Nested
    class PatchUserTest {

        private User testUser;
        private User updatedUser;
        private UserResponseDto testUserResponseDto;
        private UserPatchDto testUserPatchDto;
        private JsonNode testJsonNode;
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

            updatedUser = new User()
                .setId(testUserId)
                .setLogin("patcheduser")
                .setFirstName("Patched")
                .setLastName("User")
                .setEmail("patched@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            testUserPatchDto = new UserPatchDto();
            testUserPatchDto.setLogin("patcheduser");
            testUserPatchDto.setFirstName("Patched");
            testUserPatchDto.setLastName("User");
            testUserPatchDto.setEmail("patched@example.com");

            testUserResponseDto = new UserResponseDto(
                testUserId,
                "patcheduser",
                "Patched",
                "User",
                "patched@example.com",
                UserStatus.ACTIVE,
                UserRole.USER,
                LocalDateTime.now()
            );

            testJsonNode = JsonNodeFactory.instance.objectNode()
                .put("login", "patcheduser")
                .put("firstName", "Patched")
                .put("lastName", "User")
                .put("email", "patched@example.com");
        }

        @Test
        void patchUser_WhenUserExists_ShouldPatchAndReturnUser() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserPatchDto(testUser)).thenReturn(testUserPatchDto);
            when(patchDtoFactory.createPatchDto(any(UserPatchDto.class), eq(testJsonNode))).thenReturn(testUserPatchDto);
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);
            when(userMapper.toUserResponseDto(updatedUser)).thenReturn(testUserResponseDto);

            Optional<UserResponseDto> result = userService.patchUser(testUserId, testJsonNode);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testUserResponseDto);
            assertThat(result.get().id()).isEqualTo(testUserId);
            assertThat(result.get().login()).isEqualTo("patcheduser");
            assertThat(result.get().firstName()).isEqualTo("Patched");
            assertThat(result.get().lastName()).isEqualTo("User");
            assertThat(result.get().email()).isEqualTo("patched@example.com");

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateForPatch(testUserId, testJsonNode);
            verify(userMapper).toUserPatchDto(testUser);
            verify(patchDtoFactory).createPatchDto(any(UserPatchDto.class), eq(testJsonNode));
            verify(userValidator).validateForPatchWithDto(testUserId, testUserPatchDto);
            verify(userMapper).updateUserFromPatchDto(testUserPatchDto, testUser);
            verify(userRepository).save(testUser);
            verify(userMapper).toUserResponseDto(updatedUser);
        }

        @Test
        void patchUser_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
            UUID nonExistentUserId = UUID.fromString("999e4567-e89b-12d3-a456-426614174999");

            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

            Optional<UserResponseDto> result = userService.patchUser(nonExistentUserId, testJsonNode);

            assertThat(result).isEmpty();

            verify(userRepository).findById(nonExistentUserId);
            verify(userMapper, never()).toUserPatchDto(any());
            verify(userMapper, never()).updateUserFromPatchDto(any(), any());
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        void patchUser_WithPartialData_ShouldPatchOnlyProvidedFields() {
            JsonNode partialJsonNode = JsonNodeFactory.instance.objectNode()
                .put("firstName", "NewName")
                .put("email", "newemail@example.com");

            UserPatchDto partialPatchDto = new UserPatchDto();
            partialPatchDto.setFirstName("NewName");
            partialPatchDto.setEmail("newemail@example.com");

            User partiallyUpdatedUser = new User()
                .setId(testUserId)
                .setLogin("testuser")
                .setFirstName("NewName")
                .setLastName("User")
                .setEmail("newemail@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            UserResponseDto partialResponseDto = new UserResponseDto(
                testUserId,
                "testuser",
                "NewName",
                "User",
                "newemail@example.com",
                UserStatus.ACTIVE,
                UserRole.USER,
                LocalDateTime.now()
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserPatchDto(testUser)).thenReturn(partialPatchDto);
            when(patchDtoFactory.createPatchDto(any(UserPatchDto.class), eq(partialJsonNode))).thenReturn(partialPatchDto);
            when(userRepository.save(any(User.class))).thenReturn(partiallyUpdatedUser);
            when(userMapper.toUserResponseDto(partiallyUpdatedUser)).thenReturn(partialResponseDto);

            Optional<UserResponseDto> result = userService.patchUser(testUserId, partialJsonNode);

            assertThat(result).isPresent();
            assertThat(result.get().login()).isEqualTo("testuser");
            assertThat(result.get().firstName()).isEqualTo("NewName");
            assertThat(result.get().lastName()).isEqualTo("User");
            assertThat(result.get().email()).isEqualTo("newemail@example.com");

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateForPatch(testUserId, partialJsonNode);
            verify(userMapper).toUserPatchDto(testUser);
            verify(patchDtoFactory).createPatchDto(any(UserPatchDto.class), eq(partialJsonNode));
            verify(userValidator).validateForPatchWithDto(testUserId, partialPatchDto);
            verify(userMapper).updateUserFromPatchDto(partialPatchDto, testUser);
            verify(userRepository).save(testUser);
            verify(userMapper).toUserResponseDto(partiallyUpdatedUser);
        }

        @Test
        void patchUser_WithEmptyJson_ShouldNotChangeUser() {
            JsonNode emptyJsonNode = JsonNodeFactory.instance.objectNode();

            UserPatchDto emptyPatchDto = new UserPatchDto();

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserPatchDto(testUser)).thenReturn(emptyPatchDto);
            when(patchDtoFactory.createPatchDto(any(UserPatchDto.class), eq(emptyJsonNode))).thenReturn(emptyPatchDto);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toUserResponseDto(testUser)).thenReturn(testUserResponseDto);

            Optional<UserResponseDto> result = userService.patchUser(testUserId, emptyJsonNode);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testUserResponseDto);

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateForPatch(testUserId, emptyJsonNode);
            verify(userMapper).toUserPatchDto(testUser);
            verify(patchDtoFactory).createPatchDto(any(UserPatchDto.class), eq(emptyJsonNode));
            verify(userValidator).validateForPatchWithDto(testUserId, emptyPatchDto);
            verify(userMapper).updateUserFromPatchDto(emptyPatchDto, testUser);
            verify(userRepository).save(testUser);
            verify(userMapper).toUserResponseDto(testUser);
        }

        @Test
        void patchUser_WithNullJsonNode_ShouldThrowFormatValidationException() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            doThrow(FormatValidationException.jsonParsingError(new IllegalArgumentException("argument \"content\" is null")))
                .when(userValidator).validateForPatch(testUserId, null);

            assertThatThrownBy(() -> userService.patchUser(testUserId, null))
                .isInstanceOf(FormatValidationException.class)
                .hasMessageContaining("Error parsing JSON patch data");

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateForPatch(testUserId, null);
        }

        @Test
        void patchUser_WithSingleFieldUpdate_ShouldUpdateOnlyThatField() {
            JsonNode singleFieldJsonNode = JsonNodeFactory.instance.objectNode()
                .put("login", "newlogin");

            UserPatchDto singleFieldPatchDto = new UserPatchDto();
            singleFieldPatchDto.setLogin("newlogin");

            User singleFieldUpdatedUser = new User()
                .setId(testUserId)
                .setLogin("newlogin")
                .setFirstName("Test")
                .setLastName("User")
                .setEmail("test@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            UserResponseDto singleFieldResponseDto = new UserResponseDto(
                testUserId,
                "newlogin",
                "Test",
                "User",
                "test@example.com",
                UserStatus.ACTIVE,
                UserRole.USER,
                LocalDateTime.now()
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserPatchDto(testUser)).thenReturn(singleFieldPatchDto);
            when(patchDtoFactory.createPatchDto(any(UserPatchDto.class), eq(singleFieldJsonNode))).thenReturn(singleFieldPatchDto);
            when(userRepository.save(any(User.class))).thenReturn(singleFieldUpdatedUser);
            when(userMapper.toUserResponseDto(singleFieldUpdatedUser)).thenReturn(singleFieldResponseDto);

            Optional<UserResponseDto> result = userService.patchUser(testUserId, singleFieldJsonNode);

            assertThat(result).isPresent();
            assertThat(result.get().login()).isEqualTo("newlogin");
            assertThat(result.get().firstName()).isEqualTo("Test");
            assertThat(result.get().lastName()).isEqualTo("User");
            assertThat(result.get().email()).isEqualTo("test@example.com");

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateForPatch(testUserId, singleFieldJsonNode);
            verify(userMapper).toUserPatchDto(testUser);
            verify(patchDtoFactory).createPatchDto(any(UserPatchDto.class), eq(singleFieldJsonNode));
            verify(userValidator).validateForPatchWithDto(testUserId, singleFieldPatchDto);
            verify(userMapper).updateUserFromPatchDto(singleFieldPatchDto, testUser);
            verify(userRepository).save(testUser);
            verify(userMapper).toUserResponseDto(singleFieldUpdatedUser);
        }

        @Test
        void patchUser_WithExistingLogin_ShouldThrowUniquenessValidationException() {
            JsonNode jsonNodeWithExistingLogin = JsonNodeFactory.instance.objectNode()
                .put("login", "existinguser");

            UserPatchDto patchDtoWithExistingLogin = new UserPatchDto();
            patchDtoWithExistingLogin.setLogin("existinguser");

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserPatchDto(testUser)).thenReturn(patchDtoWithExistingLogin);
            when(patchDtoFactory.createPatchDto(any(UserPatchDto.class), eq(jsonNodeWithExistingLogin))).thenReturn(patchDtoWithExistingLogin);
            doThrow(new UniquenessValidationException("login", "existinguser"))
                .when(userValidator).validateForPatchWithDto(testUserId, patchDtoWithExistingLogin);

            assertThatThrownBy(() -> userService.patchUser(testUserId, jsonNodeWithExistingLogin))
                .isInstanceOf(UniquenessValidationException.class)
                .hasMessageContaining("User with login 'existinguser' already exists");

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateForPatch(testUserId, jsonNodeWithExistingLogin);
            verify(userMapper).toUserPatchDto(testUser);
            verify(patchDtoFactory).createPatchDto(any(UserPatchDto.class), eq(jsonNodeWithExistingLogin));
            verify(userValidator).validateForPatchWithDto(testUserId, patchDtoWithExistingLogin);
            verify(userMapper, never()).updateUserFromPatchDto(any(), any());
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        void patchUser_WithExistingEmail_ShouldThrowUniquenessValidationException() {
            JsonNode jsonNodeWithExistingEmail = JsonNodeFactory.instance.objectNode()
                .put("email", "existing@example.com");

            UserPatchDto patchDtoWithExistingEmail = new UserPatchDto();
            patchDtoWithExistingEmail.setLogin("testuser"); // устанавливаем логин из текущего пользователя
            patchDtoWithExistingEmail.setEmail("existing@example.com");

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserPatchDto(testUser)).thenReturn(patchDtoWithExistingEmail);
            when(patchDtoFactory.createPatchDto(any(UserPatchDto.class), eq(jsonNodeWithExistingEmail))).thenReturn(patchDtoWithExistingEmail);
            doThrow(new UniquenessValidationException("email", "existing@example.com"))
                .when(userValidator).validateForPatchWithDto(testUserId, patchDtoWithExistingEmail);

            assertThatThrownBy(() -> userService.patchUser(testUserId, jsonNodeWithExistingEmail))
                .isInstanceOf(UniquenessValidationException.class)
                .hasMessageContaining("User with email 'existing@example.com' already exists");

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateForPatch(testUserId, jsonNodeWithExistingEmail);
            verify(userMapper).toUserPatchDto(testUser);
            verify(patchDtoFactory).createPatchDto(any(UserPatchDto.class), eq(jsonNodeWithExistingEmail));
            verify(userValidator).validateForPatchWithDto(testUserId, patchDtoWithExistingEmail);
            verify(userMapper, never()).updateUserFromPatchDto(any(), any());
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        void patchUser_WithSameLoginAndEmail_ShouldPatchUser() {
            JsonNode jsonNodeWithSameData = JsonNodeFactory.instance.objectNode()
                .put("login", "testuser")
                .put("email", "test@example.com");

            UserPatchDto patchDtoWithSameData = new UserPatchDto();
            patchDtoWithSameData.setLogin("testuser");
            patchDtoWithSameData.setEmail("test@example.com");

            User patchedUserWithSameData = new User()
                .setId(testUserId)
                .setLogin("testuser")
                .setFirstName("Test")
                .setLastName("User")
                .setEmail("test@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            UserResponseDto responseDtoWithSameData = new UserResponseDto(
                testUserId,
                "testuser",
                "Test",
                "User",
                "test@example.com",
                UserStatus.ACTIVE,
                UserRole.USER,
                LocalDateTime.now()
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserPatchDto(testUser)).thenReturn(patchDtoWithSameData);
            when(patchDtoFactory.createPatchDto(any(UserPatchDto.class), eq(jsonNodeWithSameData))).thenReturn(patchDtoWithSameData);
            when(userRepository.save(any(User.class))).thenReturn(patchedUserWithSameData);
            when(userMapper.toUserResponseDto(patchedUserWithSameData)).thenReturn(responseDtoWithSameData);

            Optional<UserResponseDto> result = userService.patchUser(testUserId, jsonNodeWithSameData);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(responseDtoWithSameData);
            assertThat(result.get().login()).isEqualTo("testuser");
            assertThat(result.get().email()).isEqualTo("test@example.com");

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateForPatch(testUserId, jsonNodeWithSameData);
            verify(userMapper).toUserPatchDto(testUser);
            verify(patchDtoFactory).createPatchDto(any(UserPatchDto.class), eq(jsonNodeWithSameData));
            verify(userValidator).validateForPatchWithDto(testUserId, patchDtoWithSameData);
            verify(userMapper).updateUserFromPatchDto(patchDtoWithSameData, testUser);
            verify(userRepository).save(testUser);
            verify(userMapper).toUserResponseDto(patchedUserWithSameData);
        }

        @Test
        void patchUser_WithInvalidLoginTooShort_ShouldThrowFormatValidationException() {
            JsonNode invalidLoginJsonNode = JsonNodeFactory.instance.objectNode()
                .put("login", "ab");

            UserPatchDto invalidPatchDto = new UserPatchDto();
            invalidPatchDto.setLogin("ab");
            invalidPatchDto.setEmail("test@example.com");

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserPatchDto(testUser)).thenReturn(invalidPatchDto);
            when(patchDtoFactory.createPatchDto(any(UserPatchDto.class), eq(invalidLoginJsonNode))).thenReturn(invalidPatchDto);
            doThrow(FormatValidationException.beanValidationError("login", "Size", "Login must be between 3 and 50 characters"))
                .when(userValidator).validateForPatchWithDto(testUserId, invalidPatchDto);

            assertThatThrownBy(() -> userService.patchUser(testUserId, invalidLoginJsonNode))
                .isInstanceOf(FormatValidationException.class)
                .hasMessageContaining("Login must be between 3 and 50 characters");

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateForPatch(testUserId, invalidLoginJsonNode);
            verify(userMapper).toUserPatchDto(testUser);
            verify(patchDtoFactory).createPatchDto(any(UserPatchDto.class), eq(invalidLoginJsonNode));
            verify(userValidator).validateForPatchWithDto(testUserId, invalidPatchDto);
            verify(userMapper, never()).updateUserFromPatchDto(any(), any());
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        void patchUser_WithInvalidLoginTooLong_ShouldThrowFormatValidationException() {
            String longLogin = "a".repeat(51);
            JsonNode invalidLoginJsonNode = JsonNodeFactory.instance.objectNode()
                .put("login", longLogin);

            UserPatchDto invalidPatchDto = new UserPatchDto();
            invalidPatchDto.setLogin(longLogin);
            invalidPatchDto.setEmail("test@example.com");

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserPatchDto(testUser)).thenReturn(invalidPatchDto);
            when(patchDtoFactory.createPatchDto(any(UserPatchDto.class), eq(invalidLoginJsonNode))).thenReturn(invalidPatchDto);
            doThrow(FormatValidationException.beanValidationError("login", "Size", "Login must be between 3 and 50 characters"))
                .when(userValidator).validateForPatchWithDto(testUserId, invalidPatchDto);

            assertThatThrownBy(() -> userService.patchUser(testUserId, invalidLoginJsonNode))
                .isInstanceOf(FormatValidationException.class)
                .hasMessageContaining("Login must be between 3 and 50 characters");

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateForPatch(testUserId, invalidLoginJsonNode);
            verify(userMapper).toUserPatchDto(testUser);
            verify(patchDtoFactory).createPatchDto(any(UserPatchDto.class), eq(invalidLoginJsonNode));
            verify(userValidator).validateForPatchWithDto(testUserId, invalidPatchDto);
            verify(userMapper, never()).updateUserFromPatchDto(any(), any());
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        void patchUser_WithInvalidEmail_ShouldThrowFormatValidationException() {
            JsonNode invalidEmailJsonNode = JsonNodeFactory.instance.objectNode()
                .put("email", "invalid-email");

            UserPatchDto invalidPatchDto = new UserPatchDto();
            invalidPatchDto.setLogin("testuser");
            invalidPatchDto.setEmail("invalid-email");

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userMapper.toUserPatchDto(testUser)).thenReturn(invalidPatchDto);
            when(patchDtoFactory.createPatchDto(any(UserPatchDto.class), eq(invalidEmailJsonNode))).thenReturn(invalidPatchDto);
            doThrow(FormatValidationException.beanValidationError("email", "Email", "Invalid email format"))
                .when(userValidator).validateForPatchWithDto(testUserId, invalidPatchDto);

            assertThatThrownBy(() -> userService.patchUser(testUserId, invalidEmailJsonNode))
                .isInstanceOf(FormatValidationException.class)
                .hasMessageContaining("Invalid email format");

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateForPatch(testUserId, invalidEmailJsonNode);
            verify(userMapper).toUserPatchDto(testUser);
            verify(patchDtoFactory).createPatchDto(any(UserPatchDto.class), eq(invalidEmailJsonNode));
            verify(userValidator).validateForPatchWithDto(testUserId, invalidPatchDto);
            verify(userMapper, never()).updateUserFromPatchDto(any(), any());
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserResponseDto(any());
        }
    }

    @Nested
    class InactivateUserTest {

        private User testUser;
        private User inactivatedUser;
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

            inactivatedUser = new User()
                .setId(testUserId)
                .setLogin("testuser")
                .setFirstName("Test")
                .setLastName("User")
                .setEmail("test@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.INACTIVE)
                .setRole(UserRole.USER);

            testUserResponseDto = new UserResponseDto(
                testUserId,
                "testuser",
                "Test",
                "User",
                "test@example.com",
                UserStatus.INACTIVE,
                UserRole.USER,
                LocalDateTime.now()
            );
        }

        @Test
        void inactivateUser_WhenUserExistsAndIsNotAdmin_ShouldInactivateAndReturnUser() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(inactivatedUser);
            when(userMapper.toUserResponseDto(inactivatedUser)).thenReturn(testUserResponseDto);

            Optional<UserResponseDto> result = userService.inactivateUser(testUserId);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testUserResponseDto);
            assertThat(result.get().status()).isEqualTo(UserStatus.INACTIVE);

            verify(userRepository).findById(testUserId);
            verify(userRepository).save(testUser);
            verify(userMapper).toUserResponseDto(inactivatedUser);
        }

        @Test
        void inactivateUser_WhenUserExistsAndIsAdminWithOtherActiveAdmins_ShouldInactivateAndReturnUser() {
            User adminUser = new User()
                .setId(testUserId)
                .setLogin("adminuser")
                .setFirstName("Admin")
                .setLastName("User")
                .setEmail("admin@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.ADMIN);

            User inactivatedAdminUser = new User()
                .setId(testUserId)
                .setLogin("adminuser")
                .setFirstName("Admin")
                .setLastName("User")
                .setEmail("admin@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.INACTIVE)
                .setRole(UserRole.ADMIN);

            UserResponseDto adminResponseDto = new UserResponseDto(
                testUserId,
                "adminuser",
                "Admin",
                "User",
                "admin@example.com",
                UserStatus.INACTIVE,
                UserRole.ADMIN,
                LocalDateTime.now()
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(adminUser));
            when(userRepository.save(any(User.class))).thenReturn(inactivatedAdminUser);
            when(userMapper.toUserResponseDto(inactivatedAdminUser)).thenReturn(adminResponseDto);

            Optional<UserResponseDto> result = userService.inactivateUser(testUserId);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(adminResponseDto);
            assertThat(result.get().status()).isEqualTo(UserStatus.INACTIVE);

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateAdminDeactivation(testUserId);
            verify(userRepository).save(adminUser);
            verify(userMapper).toUserResponseDto(inactivatedAdminUser);
        }

        @Test
        void inactivateUser_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
            UUID nonExistentUserId = UUID.fromString("999e4567-e89b-12d3-a456-426614174999");

            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

            Optional<UserResponseDto> result = userService.inactivateUser(nonExistentUserId);

            assertThat(result).isEmpty();

            verify(userRepository).findById(nonExistentUserId);
            verify(userRepository, never()).countByRoleAndStatus(any(), any());
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        void inactivateUser_WhenUserIsLastActiveAdmin_ShouldThrowBusinessRuleValidationException() {
            User adminUser = new User()
                .setId(testUserId)
                .setLogin("adminuser")
                .setFirstName("Admin")
                .setLastName("User")
                .setEmail("admin@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.ADMIN);

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(adminUser));
            doThrow(BusinessRuleValidationException.lastAdminDeactivation(testUserId))
                .when(userValidator).validateAdminDeactivation(testUserId);

            assertThatThrownBy(() -> userService.inactivateUser(testUserId))
                .isInstanceOf(BusinessRuleValidationException.class)
                .hasMessageContaining("Business rule 'LAST_ADMIN_DEACTIVATION' violated");

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateAdminDeactivation(testUserId);
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        void inactivateUser_WhenUserIsAlreadyInactive_ShouldInactivateAndReturnUser() {
            User inactiveUser = new User()
                .setId(testUserId)
                .setLogin("testuser")
                .setFirstName("Test")
                .setLastName("User")
                .setEmail("test@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.INACTIVE)
                .setRole(UserRole.USER);

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(inactiveUser));
            when(userRepository.save(any(User.class))).thenReturn(inactiveUser);
            when(userMapper.toUserResponseDto(inactiveUser)).thenReturn(testUserResponseDto);

            Optional<UserResponseDto> result = userService.inactivateUser(testUserId);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testUserResponseDto);
            assertThat(result.get().status()).isEqualTo(UserStatus.INACTIVE);

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateAdminDeactivation(testUserId);
            verify(userRepository).save(inactiveUser);
            verify(userMapper).toUserResponseDto(inactiveUser);
        }

        @Test
        void inactivateUser_WhenUserIsModerator_ShouldInactivateAndReturnUser() {
            User moderatorUser = new User()
                .setId(testUserId)
                .setLogin("moduser")
                .setFirstName("Moderator")
                .setLastName("User")
                .setEmail("mod@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.MODERATOR);

            User inactivatedModeratorUser = new User()
                .setId(testUserId)
                .setLogin("moduser")
                .setFirstName("Moderator")
                .setLastName("User")
                .setEmail("mod@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.INACTIVE)
                .setRole(UserRole.MODERATOR);

            UserResponseDto moderatorResponseDto = new UserResponseDto(
                testUserId,
                "moduser",
                "Moderator",
                "User",
                "mod@example.com",
                UserStatus.INACTIVE,
                UserRole.MODERATOR,
                LocalDateTime.now()
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(moderatorUser));
            when(userRepository.save(any(User.class))).thenReturn(inactivatedModeratorUser);
            when(userMapper.toUserResponseDto(inactivatedModeratorUser)).thenReturn(moderatorResponseDto);

            Optional<UserResponseDto> result = userService.inactivateUser(testUserId);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(moderatorResponseDto);
            assertThat(result.get().status()).isEqualTo(UserStatus.INACTIVE);

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateAdminDeactivation(testUserId);
            verify(userRepository, never()).countByRoleAndStatus(any(), any());
            verify(userRepository).save(moderatorUser);
            verify(userMapper).toUserResponseDto(inactivatedModeratorUser);
        }
    }

    @Nested
    class UpdateUserRoleTest {

        private User testUser;
        private User updatedUser;
        private UserResponseDto testUserResponseDto;
        private UserRoleUpdateDto testRoleUpdateDto;
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

            updatedUser = new User()
                .setId(testUserId)
                .setLogin("testuser")
                .setFirstName("Test")
                .setLastName("User")
                .setEmail("test@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.MODERATOR);

            testRoleUpdateDto = new UserRoleUpdateDto(UserRole.MODERATOR);

            testUserResponseDto = new UserResponseDto(
                testUserId,
                "testuser",
                "Test",
                "User",
                "test@example.com",
                UserStatus.ACTIVE,
                UserRole.MODERATOR,
                LocalDateTime.now()
            );
        }

        @Test
        void updateUserRole_WhenUserExistsAndIsNotAdmin_ShouldUpdateRoleAndReturnUser() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);
            when(userMapper.toUserResponseDto(updatedUser)).thenReturn(testUserResponseDto);

            Optional<UserResponseDto> result = userService.updateUserRole(testUserId, testRoleUpdateDto);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testUserResponseDto);
            assertThat(result.get().role()).isEqualTo(UserRole.MODERATOR);

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateRoleChange(testUserId, UserRole.MODERATOR);
            verify(userRepository).save(testUser);
            verify(userMapper).toUserResponseDto(updatedUser);
        }

        @Test
        void updateUserRole_WhenUserExistsAndIsAdminWithOtherActiveAdmins_ShouldUpdateRoleAndReturnUser() {
            User adminUser = new User()
                .setId(testUserId)
                .setLogin("adminuser")
                .setFirstName("Admin")
                .setLastName("User")
                .setEmail("admin@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.ADMIN);

            User updatedAdminUser = new User()
                .setId(testUserId)
                .setLogin("adminuser")
                .setFirstName("Admin")
                .setLastName("User")
                .setEmail("admin@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.MODERATOR);

            UserResponseDto adminResponseDto = new UserResponseDto(
                testUserId,
                "adminuser",
                "Admin",
                "User",
                "admin@example.com",
                UserStatus.ACTIVE,
                UserRole.MODERATOR,
                LocalDateTime.now()
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(adminUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedAdminUser);
            when(userMapper.toUserResponseDto(updatedAdminUser)).thenReturn(adminResponseDto);

            Optional<UserResponseDto> result = userService.updateUserRole(testUserId, testRoleUpdateDto);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(adminResponseDto);
            assertThat(result.get().role()).isEqualTo(UserRole.MODERATOR);

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateRoleChange(testUserId, UserRole.MODERATOR);
            verify(userRepository).save(adminUser);
            verify(userMapper).toUserResponseDto(updatedAdminUser);
        }

        @Test
        void updateUserRole_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
            UUID nonExistentUserId = UUID.fromString("999e4567-e89b-12d3-a456-426614174999");

            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

            Optional<UserResponseDto> result = userService.updateUserRole(nonExistentUserId, testRoleUpdateDto);

            assertThat(result).isEmpty();

            verify(userRepository).findById(nonExistentUserId);
            verify(userRepository, never()).countByRoleAndStatus(any(), any());
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        void updateUserRole_WhenUserIsLastActiveAdmin_ShouldThrowBusinessRuleValidationException() {
            User adminUser = new User()
                .setId(testUserId)
                .setLogin("adminuser")
                .setFirstName("Admin")
                .setLastName("User")
                .setEmail("admin@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.ADMIN);

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(adminUser));
            doThrow(BusinessRuleValidationException.lastAdminRoleChange(testUserId, UserRole.MODERATOR))
                .when(userValidator).validateRoleChange(testUserId, UserRole.MODERATOR);

            assertThatThrownBy(() -> userService.updateUserRole(testUserId, testRoleUpdateDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .hasMessageContaining("Business rule 'LAST_ADMIN_ROLE_CHANGE' violated");

            verify(userRepository).findById(testUserId);
            verify(userValidator).validateRoleChange(testUserId, UserRole.MODERATOR);
            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toUserResponseDto(any());
        }

        @Test
        void updateUserRole_WhenChangingUserToAdmin_ShouldUpdateRoleAndReturnUser() {
            UserRoleUpdateDto userToAdminDto = new UserRoleUpdateDto(UserRole.ADMIN);

            User updatedToAdminUser = new User()
                .setId(testUserId)
                .setLogin("testuser")
                .setFirstName("Test")
                .setLastName("User")
                .setEmail("test@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.ADMIN);

            UserResponseDto adminResponseDto = new UserResponseDto(
                testUserId,
                "testuser",
                "Test",
                "User",
                "test@example.com",
                UserStatus.ACTIVE,
                UserRole.ADMIN,
                LocalDateTime.now()
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedToAdminUser);
            when(userMapper.toUserResponseDto(updatedToAdminUser)).thenReturn(adminResponseDto);

            Optional<UserResponseDto> result = userService.updateUserRole(testUserId, userToAdminDto);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(adminResponseDto);
            assertThat(result.get().role()).isEqualTo(UserRole.ADMIN);

            verify(userRepository).findById(testUserId);
            verify(userRepository, never()).countByRoleAndStatus(any(), any());
            verify(userRepository).save(testUser);
            verify(userMapper).toUserResponseDto(updatedToAdminUser);
        }

        @Test
        void updateUserRole_WhenChangingModeratorToUser_ShouldUpdateRoleAndReturnUser() {
            User moderatorUser = new User()
                .setId(testUserId)
                .setLogin("moduser")
                .setFirstName("Moderator")
                .setLastName("User")
                .setEmail("mod@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.MODERATOR);

            UserRoleUpdateDto moderatorToUserDto = new UserRoleUpdateDto(UserRole.USER);

            User updatedToUser = new User()
                .setId(testUserId)
                .setLogin("moduser")
                .setFirstName("Moderator")
                .setLastName("User")
                .setEmail("mod@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            UserResponseDto userResponseDto = new UserResponseDto(
                testUserId,
                "moduser",
                "Moderator",
                "User",
                "mod@example.com",
                UserStatus.ACTIVE,
                UserRole.USER,
                LocalDateTime.now()
            );

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(moderatorUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedToUser);
            when(userMapper.toUserResponseDto(updatedToUser)).thenReturn(userResponseDto);

            Optional<UserResponseDto> result = userService.updateUserRole(testUserId, moderatorToUserDto);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(userResponseDto);
            assertThat(result.get().role()).isEqualTo(UserRole.USER);

            verify(userRepository).findById(testUserId);
            verify(userRepository, never()).countByRoleAndStatus(any(), any());
            verify(userRepository).save(moderatorUser);
            verify(userMapper).toUserResponseDto(updatedToUser);
        }
    }
}
