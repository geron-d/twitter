package com.twitter.mapper;

import com.twitter.dto.request.FollowRequestDto;
import com.twitter.dto.response.FollowResponseDto;
import com.twitter.dto.response.FollowStatsResponseDto;
import com.twitter.dto.response.FollowStatusResponseDto;
import com.twitter.dto.response.FollowerResponseDto;
import com.twitter.dto.response.FollowingResponseDto;
import com.twitter.entity.Follow;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FollowMapperTest {

    private final FollowMapper followMapper = Mappers.getMapper(FollowMapper.class);

    @Nested
    class ToFollowTests {

        @Test
        void toFollow_WithValidData_ShouldMapCorrectly() {
            UUID followerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID followingId = UUID.fromString("987fcdeb-51a2-43d7-b123-426614174999");
            FollowRequestDto dto = FollowRequestDto.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();

            Follow result = followMapper.toFollow(dto);

            assertThat(result).isNotNull();
            assertThat(result.getFollowerId()).isEqualTo(followerId);
            assertThat(result.getFollowingId()).isEqualTo(followingId);
            assertThat(result.getId()).isNull();
            assertThat(result.getCreatedAt()).isNull();
        }

        @Test
        void toFollow_ShouldIgnoreIdField() {
            UUID followerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID followingId = UUID.fromString("987fcdeb-51a2-43d7-b123-426614174999");
            FollowRequestDto dto = FollowRequestDto.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();

            Follow result = followMapper.toFollow(dto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull();
        }

        @Test
        void toFollow_ShouldIgnoreCreatedAtField() {
            UUID followerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID followingId = UUID.fromString("987fcdeb-51a2-43d7-b123-426614174999");
            FollowRequestDto dto = FollowRequestDto.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();

            Follow result = followMapper.toFollow(dto);

            assertThat(result).isNotNull();
            assertThat(result.getCreatedAt()).isNull();
        }

        @Test
        void toFollow_WithNullInput_ShouldReturnNull() {
            Follow result = followMapper.toFollow(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    class ToFollowResponseDtoTests {

        @Test
        void toFollowResponseDto_WithValidData_ShouldMapAllFieldsCorrectly() {
            UUID followId = UUID.fromString("456e7890-e89b-12d3-a456-426614174111");
            UUID followerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID followingId = UUID.fromString("987fcdeb-51a2-43d7-b123-426614174999");
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 27, 10, 30, 0);
            Follow follow = Follow.builder()
                .id(followId)
                .followerId(followerId)
                .followingId(followingId)
                .createdAt(createdAt)
                .build();

            FollowResponseDto result = followMapper.toFollowResponseDto(follow);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(followId);
            assertThat(result.followerId()).isEqualTo(followerId);
            assertThat(result.followingId()).isEqualTo(followingId);
            assertThat(result.createdAt()).isEqualTo(createdAt);
        }

        @Test
        void toFollowResponseDto_WithNullInput_ShouldReturnNull() {
            FollowResponseDto result = followMapper.toFollowResponseDto(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    class ToFollowerResponseDtoTests {

        @Test
        void toFollowerResponseDto_WithValidData_ShouldMapAllFieldsCorrectly() {
            UUID followId = UUID.fromString("456e7890-e89b-12d3-a456-426614174111");
            UUID followerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID followingId = UUID.fromString("987fcdeb-51a2-43d7-b123-426614174999");
            String login = "john_doe";
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 27, 10, 30, 0);
            Follow follow = Follow.builder()
                .id(followId)
                .followerId(followerId)
                .followingId(followingId)
                .createdAt(createdAt)
                .build();

            FollowerResponseDto result = followMapper.toFollowerResponseDto(follow, login);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(followerId);
            assertThat(result.login()).isEqualTo(login);
            assertThat(result.createdAt()).isEqualTo(createdAt);
        }

    }

    @Nested
    class ToFollowingResponseDtoTests {

        @Test
        void toFollowingResponseDto_WithValidData_ShouldMapAllFieldsCorrectly() {
            UUID followId = UUID.fromString("456e7890-e89b-12d3-a456-426614174111");
            UUID followerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID followingId = UUID.fromString("987fcdeb-51a2-43d7-b123-426614174999");
            String login = "jane_doe";
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 27, 10, 30, 0);
            Follow follow = Follow.builder()
                .id(followId)
                .followerId(followerId)
                .followingId(followingId)
                .createdAt(createdAt)
                .build();

            FollowingResponseDto result = followMapper.toFollowingResponseDto(follow, login);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(followingId);
            assertThat(result.login()).isEqualTo(login);
            assertThat(result.createdAt()).isEqualTo(createdAt);
        }

    }

    @Nested
    class ToFollowStatusResponseDtoTests {

        @Test
        void toFollowStatusResponseDto_WithValidData_ShouldMapAllFieldsCorrectly() {
            UUID followId = UUID.fromString("456e7890-e89b-12d3-a456-426614174111");
            UUID followerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID followingId = UUID.fromString("987fcdeb-51a2-43d7-b123-426614174999");
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 27, 10, 30, 0);
            Follow follow = Follow.builder()
                .id(followId)
                .followerId(followerId)
                .followingId(followingId)
                .createdAt(createdAt)
                .build();

            FollowStatusResponseDto result = followMapper.toFollowStatusResponseDto(follow);

            assertThat(result).isNotNull();
            assertThat(result.isFollowing()).isTrue();
            assertThat(result.createdAt()).isEqualTo(createdAt);
        }

        @Test
        void toFollowStatusResponseDto_ShouldSetIsFollowingToTrue() {
            UUID followId = UUID.fromString("456e7890-e89b-12d3-a456-426614174111");
            UUID followerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID followingId = UUID.fromString("987fcdeb-51a2-43d7-b123-426614174999");
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 27, 10, 30, 0);
            Follow follow = Follow.builder()
                .id(followId)
                .followerId(followerId)
                .followingId(followingId)
                .createdAt(createdAt)
                .build();

            FollowStatusResponseDto result = followMapper.toFollowStatusResponseDto(follow);

            assertThat(result).isNotNull();
            assertThat(result.isFollowing()).isTrue();
        }

        @Test
        void toFollowStatusResponseDto_WithNullInput_ShouldReturnNull() {
            FollowStatusResponseDto result = followMapper.toFollowStatusResponseDto(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    class ToFollowStatsResponseDtoTests {

        @Test
        void toFollowStatsResponseDto_WithValidData_ShouldMapAllFieldsCorrectly() {
            long followersCount = 150L;
            long followingCount = 75L;

            FollowStatsResponseDto result = followMapper.toFollowStatsResponseDto(followersCount, followingCount);

            assertThat(result).isNotNull();
            assertThat(result.followersCount()).isEqualTo(followersCount);
            assertThat(result.followingCount()).isEqualTo(followingCount);
        }

        @Test
        void toFollowStatsResponseDto_WithZeroCounts_ShouldMapCorrectly() {
            long followersCount = 0L;
            long followingCount = 0L;

            FollowStatsResponseDto result = followMapper.toFollowStatsResponseDto(followersCount, followingCount);

            assertThat(result).isNotNull();
            assertThat(result.followersCount()).isEqualTo(0L);
            assertThat(result.followingCount()).isEqualTo(0L);
        }

        @Test
        void toFollowStatsResponseDto_WithLargeCounts_ShouldMapCorrectly() {
            long followersCount = 1000000L;
            long followingCount = 500000L;

            FollowStatsResponseDto result = followMapper.toFollowStatsResponseDto(followersCount, followingCount);

            assertThat(result).isNotNull();
            assertThat(result.followersCount()).isEqualTo(followersCount);
            assertThat(result.followingCount()).isEqualTo(followingCount);
        }
    }
}

