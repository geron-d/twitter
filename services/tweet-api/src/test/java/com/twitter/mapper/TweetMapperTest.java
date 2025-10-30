package com.twitter.mapper;

import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.entity.Tweet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

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
}


