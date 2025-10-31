package com.twitter.mapper;

import com.twitter.dto.request.CreateTweetRequestDto;
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
            assertEquals(tweetId, result.getId(), "ID should be mapped correctly");
            assertEquals(userId, result.getUserId(), "User ID should be mapped correctly");
            assertEquals(content, result.getContent(), "Content should be mapped correctly");
            assertEquals(createdAt, result.getCreatedAt(), "CreatedAt should be mapped correctly");
            assertEquals(updatedAt, result.getUpdatedAt(), "UpdatedAt should be mapped correctly");
        }
    }
}


