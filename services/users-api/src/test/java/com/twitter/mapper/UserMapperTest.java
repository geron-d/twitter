package com.twitter.mapper;

import com.twitter.dto.UserRequestDto;
import com.twitter.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    private final UserMapper userMapper= Mappers.getMapper(UserMapper.class);

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
}
