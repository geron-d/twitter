package com.twitter.mapper;

import com.twitter.common.dto.request.retweet.RetweetRequestDto;
import com.twitter.common.dto.response.retweet.RetweetResponseDto;
import com.twitter.entity.Retweet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RetweetMapperTest {

    private final RetweetMapper retweetMapper = Mappers.getMapper(RetweetMapper.class);

    @Nested
    class ToRetweetTests {

        @Test
        void toRetweet_WithValidData_ShouldMapCorrectly() {
            UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            RetweetRequestDto requestDto = RetweetRequestDto.builder()
                .userId(userId)
                .comment(null)
                .build();

            Retweet result = retweetMapper.toRetweet(requestDto, tweetId);

            assertThat(result).isNotNull();
            assertThat(result.getTweetId()).isEqualTo(tweetId);
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getComment()).isNull();
            assertThat(result.getId()).isNull();
            assertThat(result.getCreatedAt()).isNull();
        }

        @Test
        void toRetweet_WithValidDataAndComment_ShouldMapCorrectly() {
            UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            String comment = "Great tweet!";
            RetweetRequestDto requestDto = RetweetRequestDto.builder()
                .userId(userId)
                .comment(comment)
                .build();

            Retweet result = retweetMapper.toRetweet(requestDto, tweetId);

            assertThat(result).isNotNull();
            assertThat(result.getTweetId()).isEqualTo(tweetId);
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getComment()).isEqualTo(comment);
            assertThat(result.getId()).isNull();
            assertThat(result.getCreatedAt()).isNull();
        }

        @Test
        void toRetweet_ShouldIgnoreIdField() {
            UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            RetweetRequestDto requestDto = RetweetRequestDto.builder()
                .userId(userId)
                .comment(null)
                .build();

            Retweet result = retweetMapper.toRetweet(requestDto, tweetId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull();
        }

        @Test
        void toRetweet_ShouldIgnoreCreatedAtField() {
            UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            RetweetRequestDto requestDto = RetweetRequestDto.builder()
                .userId(userId)
                .comment(null)
                .build();

            Retweet result = retweetMapper.toRetweet(requestDto, tweetId);

            assertThat(result).isNotNull();
            assertThat(result.getCreatedAt()).isNull();
        }

        @Test
        void toRetweet_WithNullTweetId_ShouldMapWithNullTweetId() {
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            RetweetRequestDto requestDto = RetweetRequestDto.builder()
                .userId(userId)
                .comment(null)
                .build();

            Retweet result = retweetMapper.toRetweet(requestDto, null);

            assertThat(result).isNotNull();
            assertThat(result.getTweetId()).isNull();
            assertThat(result.getUserId()).isEqualTo(userId);
        }

        @Test
        void toRetweet_WithNullComment_ShouldMapWithNullComment() {
            UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            RetweetRequestDto requestDto = RetweetRequestDto.builder()
                .userId(userId)
                .comment(null)
                .build();

            Retweet result = retweetMapper.toRetweet(requestDto, tweetId);

            assertThat(result).isNotNull();
            assertThat(result.getComment()).isNull();
        }
    }

    @Nested
    class ToRetweetResponseDtoTests {

        @Test
        void toRetweetResponseDto_WithValidData_ShouldMapAllFieldsCorrectly() {
            UUID retweetId = UUID.fromString("987e6543-e21b-43d2-b654-321987654321");
            UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 27, 15, 30, 0);
            Retweet retweet = Retweet.builder()
                .id(retweetId)
                .tweetId(tweetId)
                .userId(userId)
                .comment(null)
                .createdAt(createdAt)
                .build();

            RetweetResponseDto result = retweetMapper.toRetweetResponseDto(retweet);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(retweetId);
            assertThat(result.tweetId()).isEqualTo(tweetId);
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.comment()).isNull();
            assertThat(result.createdAt()).isEqualTo(createdAt);
        }

        @Test
        void toRetweetResponseDto_WithValidDataAndComment_ShouldMapAllFieldsCorrectly() {
            UUID retweetId = UUID.fromString("987e6543-e21b-43d2-b654-321987654321");
            UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            String comment = "Great tweet!";
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 27, 15, 30, 0);
            Retweet retweet = Retweet.builder()
                .id(retweetId)
                .tweetId(tweetId)
                .userId(userId)
                .comment(comment)
                .createdAt(createdAt)
                .build();

            RetweetResponseDto result = retweetMapper.toRetweetResponseDto(retweet);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(retweetId);
            assertThat(result.tweetId()).isEqualTo(tweetId);
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.comment()).isEqualTo(comment);
            assertThat(result.createdAt()).isEqualTo(createdAt);
        }

        @Test
        void toRetweetResponseDto_WithNullInput_ShouldReturnNull() {
            RetweetResponseDto result = retweetMapper.toRetweetResponseDto(null);

            assertThat(result).isNull();
        }

        @Test
        void toRetweetResponseDto_WithNullFields_ShouldMapWithNullValues() {
            Retweet retweet = Retweet.builder()
                .id(null)
                .tweetId(null)
                .userId(null)
                .comment(null)
                .createdAt(null)
                .build();

            RetweetResponseDto result = retweetMapper.toRetweetResponseDto(retweet);

            assertThat(result).isNotNull();
            assertThat(result.id()).isNull();
            assertThat(result.tweetId()).isNull();
            assertThat(result.userId()).isNull();
            assertThat(result.comment()).isNull();
            assertThat(result.createdAt()).isNull();
        }
    }
}