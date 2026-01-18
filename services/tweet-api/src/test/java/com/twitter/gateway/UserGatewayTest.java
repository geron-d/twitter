package com.twitter.gateway;

import com.twitter.client.UsersApiClient;
import com.twitter.common.dto.response.user.UserExistsResponseDto;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
