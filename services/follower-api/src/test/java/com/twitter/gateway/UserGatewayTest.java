package com.twitter.gateway;

import com.twitter.client.UsersApiClient;
import com.twitter.common.dto.response.user.UserExistsResponseDto;
import com.twitter.common.dto.response.user.UserResponseDto;
import com.twitter.common.enums.user.UserRole;
import com.twitter.common.enums.user.UserStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserGatewayTest {

    @Mock
    private UsersApiClient usersApiClient;

    @InjectMocks
    private UserGateway userGateway;

    @Nested
    class ExistsUserTests {

        @Test
        void existsUser_WhenValidUserIdAndUserExists_ShouldReturnTrue() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UserExistsResponseDto response = new UserExistsResponseDto(true);

            when(usersApiClient.existsUser(validUserId)).thenReturn(response);

            boolean result = userGateway.existsUser(validUserId);

            assertThat(result).isTrue();
            verify(usersApiClient, times(1)).existsUser(eq(validUserId));
        }

        @Test
        void existsUser_WhenValidUserIdAndUserDoesNotExist_ShouldReturnFalse() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UserExistsResponseDto response = new UserExistsResponseDto(false);

            when(usersApiClient.existsUser(validUserId)).thenReturn(response);

            boolean result = userGateway.existsUser(validUserId);

            assertThat(result).isFalse();
            verify(usersApiClient, times(1)).existsUser(eq(validUserId));
        }

        @Test
        void existsUser_WhenUserIdIsNull_ShouldReturnFalseWithoutCallingClient() {
            UUID nullUserId = null;

            boolean result = userGateway.existsUser(nullUserId);

            assertThat(result).isFalse();
            verify(usersApiClient, never()).existsUser(any());
        }

        @Test
        void existsUser_WhenExceptionOccurs_ShouldReturnFalse() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            RuntimeException runtimeException = new RuntimeException("Service unavailable");

            when(usersApiClient.existsUser(validUserId)).thenThrow(runtimeException);

            boolean result = userGateway.existsUser(validUserId);

            assertThat(result).isFalse();
            verify(usersApiClient, times(1)).existsUser(eq(validUserId));
        }
    }

    @Nested
    class GetUserLoginTests {

        @Test
        void getUserLogin_WhenValidUserIdAndUserExists_ShouldReturnOptionalWithLogin() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            String expectedLogin = "john_doe";
            UserResponseDto userResponseDto = new UserResponseDto(
                validUserId,
                expectedLogin,
                "John",
                "Doe",
                "john.doe@example.com",
                UserStatus.ACTIVE,
                UserRole.USER,
                LocalDateTime.of(2025, 1, 27, 10, 30, 0)
            );
            ResponseEntity<UserResponseDto> response = ResponseEntity.ok(userResponseDto);

            when(usersApiClient.getUserById(validUserId)).thenReturn(response);

            Optional<String> result = userGateway.getUserLogin(validUserId);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedLogin);
            verify(usersApiClient, times(1)).getUserById(eq(validUserId));
        }

        @Test
        void getUserLogin_WhenValidUserIdAndUserDoesNotExist_ShouldReturnEmptyOptional() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            ResponseEntity<UserResponseDto> response = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            when(usersApiClient.getUserById(validUserId)).thenReturn(response);

            Optional<String> result = userGateway.getUserLogin(validUserId);

            assertThat(result).isEmpty();
            verify(usersApiClient, times(1)).getUserById(eq(validUserId));
        }

        @Test
        void getUserLogin_WhenResponseBodyIsNull_ShouldReturnEmptyOptional() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            ResponseEntity<UserResponseDto> response = ResponseEntity.ok(null);

            when(usersApiClient.getUserById(validUserId)).thenReturn(response);

            Optional<String> result = userGateway.getUserLogin(validUserId);

            assertThat(result).isEmpty();
            verify(usersApiClient, times(1)).getUserById(eq(validUserId));
        }

        @Test
        void getUserLogin_WhenUserIdIsNull_ShouldReturnEmptyOptionalWithoutCallingClient() {
            UUID nullUserId = null;

            Optional<String> result = userGateway.getUserLogin(nullUserId);

            assertThat(result).isEmpty();
            verify(usersApiClient, never()).getUserById(any());
        }

        @Test
        void getUserLogin_WhenExceptionOccurs_ShouldReturnEmptyOptional() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            RuntimeException runtimeException = new RuntimeException("Service unavailable");

            when(usersApiClient.getUserById(validUserId)).thenThrow(runtimeException);

            Optional<String> result = userGateway.getUserLogin(validUserId);

            assertThat(result).isEmpty();
            verify(usersApiClient, times(1)).getUserById(eq(validUserId));
        }
    }
}