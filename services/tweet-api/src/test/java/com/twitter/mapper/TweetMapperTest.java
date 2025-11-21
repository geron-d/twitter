package com.twitter.mapper;

import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.request.UpdateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import com.twitter.entity.Tweet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TweetMapperTest {

    private final TweetMapper mapper = Mappers.getMapper(TweetMapper.class);

    @Nested
    class ToEntity {

        @Test
        void mapsValidData_correctlyTransfersFieldsAndIgnoresTechnical() {
            UUID userId = UUID.randomUUID();
            CreateTweetRequestDto dto = CreateTweetRequestDto.builder()
                .content("hello world")
                .userId(userId)
                .build();

            Tweet entity = mapper.toEntity(dto);

            assertNotNull(entity);
            assertEquals("hello world", entity.getContent());
            assertEquals(userId, entity.getUserId());
            assertNull(entity.getId());
            assertNull(entity.getCreatedAt());
            assertNull(entity.getUpdatedAt());
        }
    }

    @Nested
    class ToResponseDtoTests {

        @Test
        void toResponseDto_WithValidData_ShouldMapAllFieldsCorrectly() {
            UUID tweetId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String content = "This is a valid tweet content";
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 15, 10, 35, 0);

            Tweet tweet = Tweet.builder()
                .id(tweetId)
                .userId(userId)
                .content(content)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

            TweetResponseDto result = mapper.toResponseDto(tweet);

            assertNotNull(result, "TweetResponseDto should not be null");
            assertEquals(tweetId, result.id(), "ID should be mapped correctly");
            assertEquals(userId, result.userId(), "User ID should be mapped correctly");
            assertEquals(content, result.content(), "Content should be mapped correctly");
            assertEquals(createdAt, result.createdAt(), "CreatedAt should be mapped correctly");
            assertEquals(updatedAt, result.updatedAt(), "UpdatedAt should be mapped correctly");
        }
    }

    @Nested
    class UpdateTweetFromUpdateDtoTests {

        @Test
        void updateTweetFromUpdateDto_WithValidData_ShouldUpdateContentOnly() {
            UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            String originalContent = "Original tweet content";
            String updatedContent = "Updated tweet content";
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 15, 10, 35, 0);

            Tweet tweet = Tweet.builder()
                .id(tweetId)
                .userId(userId)
                .content(originalContent)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

            UpdateTweetRequestDto updateDto = UpdateTweetRequestDto.builder()
                .content(updatedContent)
                .userId(userId)
                .build();

            mapper.updateTweetFromUpdateDto(updateDto, tweet);

            assertEquals(updatedContent, tweet.getContent(), "Content should be updated");
            assertEquals(tweetId, tweet.getId(), "ID should not be changed");
            assertEquals(userId, tweet.getUserId(), "User ID should not be changed");
            assertEquals(createdAt, tweet.getCreatedAt(), "CreatedAt should not be changed");
            assertEquals(updatedAt, tweet.getUpdatedAt(), "UpdatedAt should not be changed");
        }

        @Test
        void updateTweetFromUpdateDto_ShouldIgnoreSystemFields() {
            UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID differentUserId = UUID.fromString("333e4567-e89b-12d3-a456-426614174000");
            String originalContent = "Original content";
            String updatedContent = "Updated content";
            LocalDateTime originalCreatedAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            LocalDateTime originalUpdatedAt = LocalDateTime.of(2024, 1, 15, 10, 35, 0);

            Tweet tweet = Tweet.builder()
                .id(tweetId)
                .userId(userId)
                .content(originalContent)
                .createdAt(originalCreatedAt)
                .updatedAt(originalUpdatedAt)
                .build();

            UpdateTweetRequestDto updateDto = UpdateTweetRequestDto.builder()
                .content(updatedContent)
                .userId(differentUserId)
                .build();

            mapper.updateTweetFromUpdateDto(updateDto, tweet);

            assertEquals(updatedContent, tweet.getContent(), "Content should be updated");
            assertEquals(tweetId, tweet.getId(), "ID should remain unchanged");
            assertEquals(userId, tweet.getUserId(), "User ID should remain unchanged (ignored from DTO)");
            assertEquals(originalCreatedAt, tweet.getCreatedAt(), "CreatedAt should remain unchanged");
            assertEquals(originalUpdatedAt, tweet.getUpdatedAt(), "UpdatedAt should remain unchanged");
        }

        @Test
        void updateTweetFromUpdateDto_WhenUpdateDtoIsNull_ShouldNotChangeTweet() {
            UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            String originalContent = "Original content";
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 15, 10, 35, 0);

            Tweet tweet = Tweet.builder()
                .id(tweetId)
                .userId(userId)
                .content(originalContent)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

            mapper.updateTweetFromUpdateDto(null, tweet);

            assertEquals(originalContent, tweet.getContent(), "Content should not be changed");
            assertEquals(tweetId, tweet.getId(), "ID should not be changed");
            assertEquals(userId, tweet.getUserId(), "User ID should not be changed");
            assertEquals(createdAt, tweet.getCreatedAt(), "CreatedAt should not be changed");
            assertEquals(updatedAt, tweet.getUpdatedAt(), "UpdatedAt should not be changed");
        }
    }
}


