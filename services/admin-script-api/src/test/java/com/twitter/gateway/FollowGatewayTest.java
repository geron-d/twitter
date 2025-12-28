package com.twitter.gateway;

import com.twitter.client.FollowApiClient;
import com.twitter.common.dto.request.FollowRequestDto;
import com.twitter.common.dto.response.FollowResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowGatewayTest {

    @Mock
    private FollowApiClient followApiClient;

    @InjectMocks
    private FollowGateway followGateway;

    private UUID followerId;
    private UUID followingId;
    private FollowRequestDto validRequest;
    private FollowResponseDto validResponse;

    @BeforeEach
    void setUp() {
        followerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        followingId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
        
        validRequest = FollowRequestDto.builder()
            .followerId(followerId)
            .followingId(followingId)
            .build();

        UUID followId = UUID.fromString("456e7890-e89b-12d3-a456-426614174111");
        validResponse = FollowResponseDto.builder()
            .id(followId)
            .followerId(followerId)
            .followingId(followingId)
            .createdAt(LocalDateTime.of(2025, 1, 27, 10, 30, 0))
            .build();
    }

    @Nested
    class CreateFollowTests {

        @Test
        void createFollow_WithValidRequest_ShouldReturnFollowResponseDto() {
            when(followApiClient.createFollow(validRequest)).thenReturn(validResponse);

            FollowResponseDto result = followGateway.createFollow(validRequest);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(validResponse.id());
            assertThat(result.followerId()).isEqualTo(followerId);
            assertThat(result.followingId()).isEqualTo(followingId);
            verify(followApiClient, times(1)).createFollow(eq(validRequest));
        }

        @Test
        void createFollow_WithValidRequest_ShouldLogSuccess() {
            when(followApiClient.createFollow(validRequest)).thenReturn(validResponse);

            followGateway.createFollow(validRequest);

            verify(followApiClient, times(1)).createFollow(eq(validRequest));
        }

        @Test
        void createFollow_WhenRequestIsNull_ShouldThrowIllegalArgumentException() {
            assertThatThrownBy(() -> followGateway.createFollow(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Follow request cannot be null");

            verify(followApiClient, never()).createFollow(any());
        }

        @Test
        void createFollow_WhenFollowerIdIsNull_ShouldThrowIllegalArgumentException() {
            FollowRequestDto requestWithNullFollowerId = FollowRequestDto.builder()
                .followerId(null)
                .followingId(followingId)
                .build();

            assertThatThrownBy(() -> followGateway.createFollow(requestWithNullFollowerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Follower ID and Following ID cannot be null");

            verify(followApiClient, never()).createFollow(any());
        }

        @Test
        void createFollow_WhenFollowingIdIsNull_ShouldThrowIllegalArgumentException() {
            FollowRequestDto requestWithNullFollowingId = FollowRequestDto.builder()
                .followerId(followerId)
                .followingId(null)
                .build();

            assertThatThrownBy(() -> followGateway.createFollow(requestWithNullFollowingId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Follower ID and Following ID cannot be null");

            verify(followApiClient, never()).createFollow(any());
        }

        @Test
        void createFollow_WhenBothIdsAreNull_ShouldThrowIllegalArgumentException() {
            FollowRequestDto requestWithNullIds = FollowRequestDto.builder()
                .followerId(null)
                .followingId(null)
                .build();

            assertThatThrownBy(() -> followGateway.createFollow(requestWithNullIds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Follower ID and Following ID cannot be null");

            verify(followApiClient, never()).createFollow(any());
        }

        @Test
        void createFollow_WhenFeignClientThrowsRuntimeException_ShouldThrowRuntimeException() {
            RuntimeException feignException = new RuntimeException("Service unavailable");
            when(followApiClient.createFollow(validRequest)).thenThrow(feignException);

            assertThatThrownBy(() -> followGateway.createFollow(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create follow relationship")
                .hasMessageContaining("Service unavailable")
                .hasCause(feignException);

            verify(followApiClient, times(1)).createFollow(eq(validRequest));
        }

        @Test
        void createFollow_WhenFeignClientThrowsIllegalArgumentException_ShouldThrowRuntimeException() {
            IllegalArgumentException validationException = new IllegalArgumentException("Invalid user IDs");
            when(followApiClient.createFollow(validRequest)).thenThrow(validationException);

            assertThatThrownBy(() -> followGateway.createFollow(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create follow relationship")
                .hasMessageContaining("Invalid user IDs")
                .hasCause(validationException);

            verify(followApiClient, times(1)).createFollow(eq(validRequest));
        }

        @Test
        void createFollow_WhenFeignClientThrowsGenericException_ShouldWrapInRuntimeException() {
            RuntimeException genericException = new RuntimeException("Connection timeout");
            when(followApiClient.createFollow(validRequest)).thenThrow(genericException);

            assertThatThrownBy(() -> followGateway.createFollow(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create follow relationship")
                .hasMessageContaining("Connection timeout")
                .hasCause(genericException);

            verify(followApiClient, times(1)).createFollow(eq(validRequest));
        }
    }
}

