package com.twitter.mapper;

import com.twitter.dto.UserPatchDto;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.entity.User;
import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Nested
    class ToUserTest {

        @Test
        void toUser_WithValidData_ShouldMapCorrectly() {
            UserRequestDto userRequestDto = new UserRequestDto(
                "testuser",
                "John",
                "Doe",
                "john.doe@example.com",
                "password123"
            );

            User result = userMapper.toUser(userRequestDto);

            assertThat(result).isNotNull();
            assertThat(result.getLogin()).isEqualTo("testuser");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(result.getPasswordHash()).isNull();
            assertThat(result.getPasswordSalt()).isNull();
            assertThat(result.getStatus()).isNull();
            assertThat(result.getRole()).isNull();
            assertThat(result.getId()).isNull();
        }

        @Test
        void toUser_WithMinimalData_ShouldMapCorrectly() {
            UserRequestDto userRequestDto = new UserRequestDto(
                "minuser",
                null,
                null,
                "min@example.com",
                "password123"
            );

            User result = userMapper.toUser(userRequestDto);

            assertThat(result).isNotNull();
            assertThat(result.getLogin()).isEqualTo("minuser");
            assertThat(result.getFirstName()).isNull();
            assertThat(result.getLastName()).isNull();
            assertThat(result.getEmail()).isEqualTo("min@example.com");
            assertThat(result.getPasswordHash()).isNull();
            assertThat(result.getPasswordSalt()).isNull();
            assertThat(result.getStatus()).isNull();
            assertThat(result.getRole()).isNull();
            assertThat(result.getId()).isNull();
        }

        @Test
        void toUser_WithNullInput_ShouldReturnNull() {
            User result = userMapper.toUser(null);

            assertThat(result).isNull();
        }

        @Test
        void toUser_ShouldIgnorePasswordHashField() {
            UserRequestDto userRequestDto = new UserRequestDto(
                "testuser",
                "Test",
                "User",
                "test@example.com",
                "password123"
            );

            User result = userMapper.toUser(userRequestDto);

            assertThat(result).isNotNull();
            assertThat(result.getPasswordHash()).isNull();
        }

        @Test
        void toUser_ShouldNotSetId() {
            UserRequestDto userRequestDto = new UserRequestDto(
                "testuser",
                "Test",
                "User",
                "test@example.com",
                "password123"
            );

            User result = userMapper.toUser(userRequestDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull();
        }

        @Test
        void toUser_ShouldNotSetStatus() {
            UserRequestDto userRequestDto = new UserRequestDto(
                "testuser",
                "Test",
                "User",
                "test@example.com",
                "password123"
            );

            User result = userMapper.toUser(userRequestDto);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isNull();
        }

        @Test
        void toUser_ShouldNotSetRole() {
            UserRequestDto userRequestDto = new UserRequestDto(
                "testuser",
                "Test",
                "User",
                "test@example.com",
                "password123"
            );

            User result = userMapper.toUser(userRequestDto);

            assertThat(result).isNotNull();
            assertThat(result.getRole()).isNull();
        }

        @Test
        void toUser_ShouldNotSetPasswordSalt() {
            UserRequestDto userRequestDto = new UserRequestDto(
                "testuser",
                "Test",
                "User",
                "test@example.com",
                "password123"
            );

            User result = userMapper.toUser(userRequestDto);

            assertThat(result).isNotNull();
            assertThat(result.getPasswordSalt()).isNull();
        }
    }

    @Nested
    class ToUserResponseDtoTest {

        @Test
        void toUserResponseDto_WithValidData_ShouldMapCorrectly() {
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 21, 15, 30, 0);
            User user = new User()
                .setId(userId)
                .setLogin("testuser")
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail("john.doe@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER)
                .setCreatedAt(createdAt);

            UserResponseDto result = userMapper.toUserResponseDto(user);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(userId);
            assertThat(result.login()).isEqualTo("testuser");
            assertThat(result.firstName()).isEqualTo("John");
            assertThat(result.lastName()).isEqualTo("Doe");
            assertThat(result.email()).isEqualTo("john.doe@example.com");
            assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
            assertThat(result.role()).isEqualTo(UserRole.USER);
            assertThat(result.createdAt()).isEqualTo(createdAt);
        }

        @Test
        void toUserResponseDto_WithMinimalData_ShouldMapCorrectly() {
            UUID userId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 21, 16, 45, 30);
            User user = new User()
                .setId(userId)
                .setLogin("minuser")
                .setEmail("min@example.com")
                .setStatus(UserStatus.INACTIVE)
                .setRole(UserRole.MODERATOR)
                .setCreatedAt(createdAt);

            UserResponseDto result = userMapper.toUserResponseDto(user);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(userId);
            assertThat(result.login()).isEqualTo("minuser");
            assertThat(result.firstName()).isNull();
            assertThat(result.lastName()).isNull();
            assertThat(result.email()).isEqualTo("min@example.com");
            assertThat(result.status()).isEqualTo(UserStatus.INACTIVE);
            assertThat(result.role()).isEqualTo(UserRole.MODERATOR);
            assertThat(result.createdAt()).isEqualTo(createdAt);
        }

        @Test
        void toUserResponseDto_WithNullInput_ShouldReturnNull() {
            UserResponseDto result = userMapper.toUserResponseDto(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    class ToUserPatchDtoTest {

        @Test
        void toUserPatchDto_WithValidData_ShouldMapCorrectly() {
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            User user = new User()
                .setId(userId)
                .setLogin("testuser")
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail("john.doe@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            UserPatchDto result = userMapper.toUserPatchDto(user);

            assertThat(result).isNotNull();
            assertThat(result.getLogin()).isEqualTo("testuser");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        }

        @Test
        void toUserPatchDto_WithMinimalData_ShouldMapCorrectly() {
            UUID userId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            User user = new User()
                .setId(userId)
                .setLogin("minuser")
                .setEmail("min@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.INACTIVE)
                .setRole(UserRole.MODERATOR);

            UserPatchDto result = userMapper.toUserPatchDto(user);

            assertThat(result).isNotNull();
            assertThat(result.getLogin()).isEqualTo("minuser");
            assertThat(result.getFirstName()).isNull();
            assertThat(result.getLastName()).isNull();
            assertThat(result.getEmail()).isEqualTo("min@example.com");
        }

        @Test
        void toUserPatchDto_WithNullInput_ShouldReturnNull() {
            UserPatchDto result = userMapper.toUserPatchDto(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    class UpdateUserFromPatchDtoTest {

        @Test
        void updateUserFromPatchDto_WithAllFields_ShouldUpdateAllFields() {
            UserPatchDto userPatchDto = new UserPatchDto();
            userPatchDto.setLogin("newlogin");
            userPatchDto.setFirstName("NewFirst");
            userPatchDto.setLastName("NewLast");
            userPatchDto.setEmail("new@example.com");

            LocalDateTime originalCreatedAt = LocalDateTime.of(2025, 1, 21, 15, 30, 0);
            User user = new User()
                .setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .setLogin("oldlogin")
                .setFirstName("OldFirst")
                .setLastName("OldLast")
                .setEmail("old@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER)
                .setCreatedAt(originalCreatedAt);

            userMapper.updateUserFromPatchDto(userPatchDto, user);

            assertThat(user.getLogin()).isEqualTo("newlogin");
            assertThat(user.getFirstName()).isEqualTo("NewFirst");
            assertThat(user.getLastName()).isEqualTo("NewLast");
            assertThat(user.getEmail()).isEqualTo("new@example.com");
            assertThat(user.getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(user.getPasswordHash()).isEqualTo("hashedPassword");
            assertThat(user.getPasswordSalt()).isEqualTo("salt");
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.getRole()).isEqualTo(UserRole.USER);
            assertThat(user.getCreatedAt()).isEqualTo(originalCreatedAt); // createdAt не должно изменяться
        }

        @Test
        void updateUserFromPatchDto_WithNullFields_ShouldSetNullFields() {
            UserPatchDto userPatchDto = new UserPatchDto();
            userPatchDto.setLogin("newlogin");
            userPatchDto.setFirstName(null);
            userPatchDto.setLastName(null);
            userPatchDto.setEmail("new@example.com");

            User user = new User()
                .setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .setLogin("oldlogin")
                .setFirstName("OldFirst")
                .setLastName("OldLast")
                .setEmail("old@example.com")
                .setPasswordHash("hashedPassword")
                .setPasswordSalt("salt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            userMapper.updateUserFromPatchDto(userPatchDto, user);

            assertThat(user.getLogin()).isEqualTo("newlogin");
            assertThat(user.getFirstName()).isNull();
            assertThat(user.getLastName()).isNull();
            assertThat(user.getEmail()).isEqualTo("new@example.com");
        }
    }

    @Nested
    class UpdateUserFromUpdateDtoTest {

        @Test
        void updateUserFromUpdateDto_WithAllFields_ShouldUpdateAllFields() {
            UserUpdateDto userUpdateDto = new UserUpdateDto(
                "newlogin",
                "NewFirst",
                "NewLast",
                "new@example.com",
                "newPassword123"
            );

            LocalDateTime originalCreatedAt = LocalDateTime.of(2025, 1, 21, 15, 30, 0);
            User user = new User()
                .setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .setLogin("oldlogin")
                .setFirstName("OldFirst")
                .setLastName("OldLast")
                .setEmail("old@example.com")
                .setPasswordHash("oldHashedPassword")
                .setPasswordSalt("oldSalt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER)
                .setCreatedAt(originalCreatedAt);

            userMapper.updateUserFromUpdateDto(userUpdateDto, user);

            assertThat(user.getLogin()).isEqualTo("newlogin");
            assertThat(user.getFirstName()).isEqualTo("NewFirst");
            assertThat(user.getLastName()).isEqualTo("NewLast");
            assertThat(user.getEmail()).isEqualTo("new@example.com");
            assertThat(user.getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(user.getPasswordHash()).isEqualTo("oldHashedPassword");
            assertThat(user.getPasswordSalt()).isEqualTo("oldSalt");
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.getRole()).isEqualTo(UserRole.USER);
            assertThat(user.getCreatedAt()).isEqualTo(originalCreatedAt); // createdAt не должно изменяться
        }

        @Test
        void updateUserFromUpdateDto_WithNullFields_ShouldSetNullFields() {
            UserUpdateDto userUpdateDto = new UserUpdateDto(
                "newlogin",
                null,
                null,
                "new@example.com",
                "newPassword123"
            );

            User user = new User()
                .setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .setLogin("oldlogin")
                .setFirstName("OldFirst")
                .setLastName("OldLast")
                .setEmail("old@example.com")
                .setPasswordHash("oldHashedPassword")
                .setPasswordSalt("oldSalt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            userMapper.updateUserFromUpdateDto(userUpdateDto, user);

            assertThat(user.getLogin()).isEqualTo("newlogin");
            assertThat(user.getFirstName()).isNull();
            assertThat(user.getLastName()).isNull();
            assertThat(user.getEmail()).isEqualTo("new@example.com");
            assertThat(user.getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(user.getPasswordHash()).isEqualTo("oldHashedPassword");
            assertThat(user.getPasswordSalt()).isEqualTo("oldSalt");
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.getRole()).isEqualTo(UserRole.USER);
        }

        @Test
        void updateUserFromUpdateDto_WithMinimalData_ShouldUpdateOnlyProvidedFields() {
            UserUpdateDto userUpdateDto = new UserUpdateDto(
                "minlogin",
                "MinFirst",
                "MinLast",
                "min@example.com",
                "minPassword123"
            );

            User user = new User()
                .setId(UUID.fromString("223e4567-e89b-12d3-a456-426614174001"))
                .setLogin("oldlogin")
                .setFirstName("OldFirst")
                .setLastName("OldLast")
                .setEmail("old@example.com")
                .setPasswordHash("oldHashedPassword")
                .setPasswordSalt("oldSalt")
                .setStatus(UserStatus.INACTIVE)
                .setRole(UserRole.MODERATOR);

            userMapper.updateUserFromUpdateDto(userUpdateDto, user);

            assertThat(user.getLogin()).isEqualTo("minlogin");
            assertThat(user.getFirstName()).isEqualTo("MinFirst");
            assertThat(user.getLastName()).isEqualTo("MinLast");
            assertThat(user.getEmail()).isEqualTo("min@example.com");
            assertThat(user.getId()).isEqualTo(UUID.fromString("223e4567-e89b-12d3-a456-426614174001"));
            assertThat(user.getPasswordHash()).isEqualTo("oldHashedPassword");
            assertThat(user.getPasswordSalt()).isEqualTo("oldSalt");
            assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
            assertThat(user.getRole()).isEqualTo(UserRole.MODERATOR);
        }

        @Test
        void updateUserFromUpdateDto_ShouldIgnoreIdField() {
            UserUpdateDto userUpdateDto = new UserUpdateDto(
                "newlogin",
                "NewFirst",
                "NewLast",
                "new@example.com",
                "newPassword123"
            );

            UUID originalId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            User user = new User()
                .setId(originalId)
                .setLogin("oldlogin")
                .setFirstName("OldFirst")
                .setLastName("OldLast")
                .setEmail("old@example.com")
                .setPasswordHash("oldHashedPassword")
                .setPasswordSalt("oldSalt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            userMapper.updateUserFromUpdateDto(userUpdateDto, user);

            assertThat(user.getId()).isEqualTo(originalId);
        }

        @Test
        void updateUserFromUpdateDto_ShouldIgnorePasswordHashField() {
            UserUpdateDto userUpdateDto = new UserUpdateDto(
                "newlogin",
                "NewFirst",
                "NewLast",
                "new@example.com",
                "newPassword123"
            );

            String originalPasswordHash = "oldHashedPassword";
            User user = new User()
                .setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .setLogin("oldlogin")
                .setFirstName("OldFirst")
                .setLastName("OldLast")
                .setEmail("old@example.com")
                .setPasswordHash(originalPasswordHash)
                .setPasswordSalt("oldSalt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            userMapper.updateUserFromUpdateDto(userUpdateDto, user);

            assertThat(user.getPasswordHash()).isEqualTo(originalPasswordHash);
        }

        @Test
        void updateUserFromUpdateDto_ShouldIgnorePasswordSaltField() {
            UserUpdateDto userUpdateDto = new UserUpdateDto(
                "newlogin",
                "NewFirst",
                "NewLast",
                "new@example.com",
                "newPassword123"
            );

            String originalPasswordSalt = "oldSalt";
            User user = new User()
                .setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .setLogin("oldlogin")
                .setFirstName("OldFirst")
                .setLastName("OldLast")
                .setEmail("old@example.com")
                .setPasswordHash("oldHashedPassword")
                .setPasswordSalt(originalPasswordSalt)
                .setStatus(UserStatus.ACTIVE)
                .setRole(UserRole.USER);

            userMapper.updateUserFromUpdateDto(userUpdateDto, user);

            assertThat(user.getPasswordSalt()).isEqualTo(originalPasswordSalt);
        }

        @Test
        void updateUserFromUpdateDto_ShouldIgnoreStatusField() {
            UserUpdateDto userUpdateDto = new UserUpdateDto(
                "newlogin",
                "NewFirst",
                "NewLast",
                "new@example.com",
                "newPassword123"
            );

            UserStatus originalStatus = UserStatus.INACTIVE;
            User user = new User()
                .setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .setLogin("oldlogin")
                .setFirstName("OldFirst")
                .setLastName("OldLast")
                .setEmail("old@example.com")
                .setPasswordHash("oldHashedPassword")
                .setPasswordSalt("oldSalt")
                .setStatus(originalStatus)
                .setRole(UserRole.USER);

            userMapper.updateUserFromUpdateDto(userUpdateDto, user);

            assertThat(user.getStatus()).isEqualTo(originalStatus);
        }

        @Test
        void updateUserFromUpdateDto_ShouldIgnoreRoleField() {
            UserUpdateDto userUpdateDto = new UserUpdateDto(
                "newlogin",
                "NewFirst",
                "NewLast",
                "new@example.com",
                "newPassword123"
            );

            UserRole originalRole = UserRole.ADMIN;
            User user = new User()
                .setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .setLogin("oldlogin")
                .setFirstName("OldFirst")
                .setLastName("OldLast")
                .setEmail("old@example.com")
                .setPasswordHash("oldHashedPassword")
                .setPasswordSalt("oldSalt")
                .setStatus(UserStatus.ACTIVE)
                .setRole(originalRole);

            userMapper.updateUserFromUpdateDto(userUpdateDto, user);

            assertThat(user.getRole()).isEqualTo(originalRole);
        }
    }
}
