package com.twitter.gateway;

import com.twitter.client.TweetsApiClient;
import com.twitter.common.dto.request.like.LikeTweetRequestDto;
import com.twitter.common.dto.request.retweet.RetweetRequestDto;
import com.twitter.common.dto.response.like.LikeResponseDto;
import com.twitter.common.dto.response.retweet.RetweetResponseDto;
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
class TweetsGatewayTest {

    @Mock
    private TweetsApiClient tweetsApiClient;

    @InjectMocks
    private TweetsGateway tweetsGateway;

    private UUID tweetId;
    private UUID userId;
    private LikeTweetRequestDto validLikeRequest;
    private LikeResponseDto validLikeResponse;
    private RetweetRequestDto validRetweetRequest;
    private RetweetResponseDto validRetweetResponse;

    @BeforeEach
    void setUp() {
        tweetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        userId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");

        validLikeRequest = LikeTweetRequestDto.builder()
            .userId(userId)
            .build();

        UUID likeId = UUID.fromString("456e7890-e89b-12d3-a456-426614174111");
        validLikeResponse = LikeResponseDto.builder()
            .id(likeId)
            .tweetId(tweetId)
            .userId(userId)
            .createdAt(LocalDateTime.of(2025, 1, 27, 10, 30, 0))
            .build();

        validRetweetRequest = RetweetRequestDto.builder()
            .userId(userId)
            .comment(null)
            .build();

        UUID retweetId = UUID.fromString("789e0123-e89b-12d3-a456-426614174222");
        validRetweetResponse = RetweetResponseDto.builder()
            .id(retweetId)
            .tweetId(tweetId)
            .userId(userId)
            .comment(null)
            .createdAt(LocalDateTime.of(2025, 1, 27, 10, 30, 0))
            .build();
    }

    @Nested
    class LikeTweetTests {

        @Test
        void likeTweet_WithValidRequest_ShouldReturnLikeResponseDto() {
            when(tweetsApiClient.likeTweet(tweetId, validLikeRequest)).thenReturn(validLikeResponse);

            LikeResponseDto result = tweetsGateway.likeTweet(tweetId, validLikeRequest);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(validLikeResponse.id());
            assertThat(result.tweetId()).isEqualTo(tweetId);
            assertThat(result.userId()).isEqualTo(userId);
            verify(tweetsApiClient, times(1)).likeTweet(eq(tweetId), eq(validLikeRequest));
        }

        @Test
        void likeTweet_WithValidRequest_ShouldLogSuccess() {
            when(tweetsApiClient.likeTweet(tweetId, validLikeRequest)).thenReturn(validLikeResponse);

            tweetsGateway.likeTweet(tweetId, validLikeRequest);

            verify(tweetsApiClient, times(1)).likeTweet(eq(tweetId), eq(validLikeRequest));
        }

        @Test
        void likeTweet_WhenTweetIdIsNull_ShouldThrowIllegalArgumentException() {
            assertThatThrownBy(() -> tweetsGateway.likeTweet(null, validLikeRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tweet ID cannot be null");

            verify(tweetsApiClient, never()).likeTweet(any(), any());
        }

        @Test
        void likeTweet_WhenRequestIsNull_ShouldThrowIllegalArgumentException() {
            assertThatThrownBy(() -> tweetsGateway.likeTweet(tweetId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Like tweet request cannot be null");

            verify(tweetsApiClient, never()).likeTweet(any(), any());
        }

        @Test
        void likeTweet_WhenFeignClientThrowsRuntimeException_ShouldThrowRuntimeException() {
            RuntimeException feignException = new RuntimeException("Service unavailable");
            when(tweetsApiClient.likeTweet(tweetId, validLikeRequest)).thenThrow(feignException);

            assertThatThrownBy(() -> tweetsGateway.likeTweet(tweetId, validLikeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create like")
                .hasMessageContaining("Service unavailable")
                .hasCause(feignException);

            verify(tweetsApiClient, times(1)).likeTweet(eq(tweetId), eq(validLikeRequest));
        }

        @Test
        void likeTweet_WhenFeignClientThrowsIllegalArgumentException_ShouldThrowRuntimeException() {
            IllegalArgumentException validationException = new IllegalArgumentException("Invalid tweet ID");
            when(tweetsApiClient.likeTweet(tweetId, validLikeRequest)).thenThrow(validationException);

            assertThatThrownBy(() -> tweetsGateway.likeTweet(tweetId, validLikeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create like")
                .hasMessageContaining("Invalid tweet ID")
                .hasCause(validationException);

            verify(tweetsApiClient, times(1)).likeTweet(eq(tweetId), eq(validLikeRequest));
        }

        @Test
        void likeTweet_WhenFeignClientThrowsGenericException_ShouldWrapInRuntimeException() {
            RuntimeException genericException = new RuntimeException("Connection timeout");
            when(tweetsApiClient.likeTweet(tweetId, validLikeRequest)).thenThrow(genericException);

            assertThatThrownBy(() -> tweetsGateway.likeTweet(tweetId, validLikeRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create like")
                .hasMessageContaining("Connection timeout")
                .hasCause(genericException);

            verify(tweetsApiClient, times(1)).likeTweet(eq(tweetId), eq(validLikeRequest));
        }
    }

    @Nested
    class RetweetTweetTests {

        @Test
        void retweetTweet_WithValidRequest_ShouldReturnRetweetResponseDto() {
            when(tweetsApiClient.retweetTweet(tweetId, validRetweetRequest)).thenReturn(validRetweetResponse);

            RetweetResponseDto result = tweetsGateway.retweetTweet(tweetId, validRetweetRequest);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(validRetweetResponse.id());
            assertThat(result.tweetId()).isEqualTo(tweetId);
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.comment()).isNull();
            verify(tweetsApiClient, times(1)).retweetTweet(eq(tweetId), eq(validRetweetRequest));
        }

        @Test
        void retweetTweet_WithValidRequestAndComment_ShouldReturnRetweetResponseDto() {
            RetweetRequestDto requestWithComment = RetweetRequestDto.builder()
                .userId(userId)
                .comment("Great tweet!")
                .build();

            RetweetResponseDto responseWithComment = RetweetResponseDto.builder()
                .id(validRetweetResponse.id())
                .tweetId(tweetId)
                .userId(userId)
                .comment("Great tweet!")
                .createdAt(LocalDateTime.of(2025, 1, 27, 10, 30, 0))
                .build();

            when(tweetsApiClient.retweetTweet(tweetId, requestWithComment)).thenReturn(responseWithComment);

            RetweetResponseDto result = tweetsGateway.retweetTweet(tweetId, requestWithComment);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(responseWithComment.id());
            assertThat(result.tweetId()).isEqualTo(tweetId);
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.comment()).isEqualTo("Great tweet!");
            verify(tweetsApiClient, times(1)).retweetTweet(eq(tweetId), eq(requestWithComment));
        }

        @Test
        void retweetTweet_WithValidRequest_ShouldLogSuccess() {
            when(tweetsApiClient.retweetTweet(tweetId, validRetweetRequest)).thenReturn(validRetweetResponse);

            tweetsGateway.retweetTweet(tweetId, validRetweetRequest);

            verify(tweetsApiClient, times(1)).retweetTweet(eq(tweetId), eq(validRetweetRequest));
        }

        @Test
        void retweetTweet_WhenTweetIdIsNull_ShouldThrowIllegalArgumentException() {
            assertThatThrownBy(() -> tweetsGateway.retweetTweet(null, validRetweetRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tweet ID cannot be null");

            verify(tweetsApiClient, never()).retweetTweet(any(), any());
        }

        @Test
        void retweetTweet_WhenRequestIsNull_ShouldThrowIllegalArgumentException() {
            assertThatThrownBy(() -> tweetsGateway.retweetTweet(tweetId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Retweet request cannot be null");

            verify(tweetsApiClient, never()).retweetTweet(any(), any());
        }

        @Test
        void retweetTweet_WhenFeignClientThrowsRuntimeException_ShouldThrowRuntimeException() {
            RuntimeException feignException = new RuntimeException("Service unavailable");
            when(tweetsApiClient.retweetTweet(tweetId, validRetweetRequest)).thenThrow(feignException);

            assertThatThrownBy(() -> tweetsGateway.retweetTweet(tweetId, validRetweetRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create retweet")
                .hasMessageContaining("Service unavailable")
                .hasCause(feignException);

            verify(tweetsApiClient, times(1)).retweetTweet(eq(tweetId), eq(validRetweetRequest));
        }

        @Test
        void retweetTweet_WhenFeignClientThrowsIllegalArgumentException_ShouldThrowRuntimeException() {
            IllegalArgumentException validationException = new IllegalArgumentException("Invalid tweet ID");
            when(tweetsApiClient.retweetTweet(tweetId, validRetweetRequest)).thenThrow(validationException);

            assertThatThrownBy(() -> tweetsGateway.retweetTweet(tweetId, validRetweetRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create retweet")
                .hasMessageContaining("Invalid tweet ID")
                .hasCause(validationException);

            verify(tweetsApiClient, times(1)).retweetTweet(eq(tweetId), eq(validRetweetRequest));
        }

        @Test
        void retweetTweet_WhenFeignClientThrowsGenericException_ShouldWrapInRuntimeException() {
            RuntimeException genericException = new RuntimeException("Connection timeout");
            when(tweetsApiClient.retweetTweet(tweetId, validRetweetRequest)).thenThrow(genericException);

            assertThatThrownBy(() -> tweetsGateway.retweetTweet(tweetId, validRetweetRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create retweet")
                .hasMessageContaining("Connection timeout")
                .hasCause(genericException);

            verify(tweetsApiClient, times(1)).retweetTweet(eq(tweetId), eq(validRetweetRequest));
        }
    }
}