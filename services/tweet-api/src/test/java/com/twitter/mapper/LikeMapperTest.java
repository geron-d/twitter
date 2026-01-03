package com.twitter.mapper;

import com.twitter.dto.request.LikeTweetRequestDto;
import com.twitter.dto.response.LikeResponseDto;
import com.twitter.entity.Like;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LikeMapperTest {

    private final LikeMapper likeMapper = Mappers.getMapper(LikeMapper.class);

    @Nested
    class ToLikeTests {

        @Test
        void toLike_WithValidData_ShouldMapCorrectly() {
            UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            LikeTweetRequestDto requestDto = LikeTweetRequestDto.builder()
                .userId(userId)
                .build();

            Like result = likeMapper.toLike(requestDto, tweetId);

            assertThat(result).isNotNull();
            assertThat(result.getTweetId()).isEqualTo(tweetId);
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getId()).isNull();
            assertThat(result.getCreatedAt()).isNull();
        }

        @Test
        void toLike_ShouldIgnoreIdField() {
            UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            LikeTweetRequestDto requestDto = LikeTweetRequestDto.builder()
                .userId(userId)
                .build();

            Like result = likeMapper.toLike(requestDto, tweetId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull();
        }

        @Test
        void toLike_ShouldIgnoreCreatedAtField() {
            UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            LikeTweetRequestDto requestDto = LikeTweetRequestDto.builder()
                .userId(userId)
                .build();

            Like result = likeMapper.toLike(requestDto, tweetId);

            assertThat(result).isNotNull();
            assertThat(result.getCreatedAt()).isNull();
        }

        @Test
        void toLike_WithNullTweetId_ShouldMapWithNullTweetId() {
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            LikeTweetRequestDto requestDto = LikeTweetRequestDto.builder()
                .userId(userId)
                .build();

            Like result = likeMapper.toLike(requestDto, null);

            assertThat(result).isNotNull();
            assertThat(result.getTweetId()).isNull();
            assertThat(result.getUserId()).isEqualTo(userId);
        }
    }

    @Nested
    class ToLikeResponseDtoTests {

        @Test
        void toLikeResponseDto_WithValidData_ShouldMapAllFieldsCorrectly() {
            UUID likeId = UUID.fromString("987e6543-e21b-43d2-b654-321987654321");
            UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 27, 15, 30, 0);
            Like like = Like.builder()
                .id(likeId)
                .tweetId(tweetId)
                .userId(userId)
                .createdAt(createdAt)
                .build();

            LikeResponseDto result = likeMapper.toLikeResponseDto(like);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(likeId);
            assertThat(result.tweetId()).isEqualTo(tweetId);
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.createdAt()).isEqualTo(createdAt);
        }

        @Test
        void toLikeResponseDto_WithNullInput_ShouldReturnNull() {
            LikeResponseDto result = likeMapper.toLikeResponseDto(null);

            assertThat(result).isNull();
        }

        @Test
        void toLikeResponseDto_WithNullFields_ShouldMapWithNullValues() {
            Like like = Like.builder()
                .id(null)
                .tweetId(null)
                .userId(null)
                .createdAt(null)
                .build();

            LikeResponseDto result = likeMapper.toLikeResponseDto(like);

            assertThat(result).isNotNull();
            assertThat(result.id()).isNull();
            assertThat(result.tweetId()).isNull();
            assertThat(result.userId()).isNull();
            assertThat(result.createdAt()).isNull();
        }
    }
}
