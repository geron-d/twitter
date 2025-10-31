package com.twitter.service;

import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import com.twitter.entity.Tweet;
import com.twitter.mapper.TweetMapper;
import com.twitter.repository.TweetRepository;
import com.twitter.validation.TweetValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TweetServiceImplTest {

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private TweetMapper tweetMapper;

    @Mock
    private TweetValidator tweetValidator;

    @InjectMocks
    private TweetServiceImpl tweetService;

    @Nested
    class CreateTweetTests {

        private CreateTweetRequestDto validRequestDto;
        private Tweet mappedTweet;
        private Tweet savedTweet;
        private TweetResponseDto responseDto;
        private UUID testUserId;

        @BeforeEach
        void setUp() {
            testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

            validRequestDto = CreateTweetRequestDto.builder()
                .content("Hello World")
                .userId(testUserId)
                .build();

            mappedTweet = Tweet.builder()
                .userId(testUserId)
                .content("Hello World")
                .build();

            UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            savedTweet = Tweet.builder()
                .id(tweetId)
                .userId(testUserId)
                .content("Hello World")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build();

            responseDto = TweetResponseDto.builder()
                .id(tweetId)
                .userId(testUserId)
                .content("Hello World")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build();
        }

        @Test
        void createTweet_WithValidData_ShouldReturnTweetResponseDto() {
            doNothing().when(tweetValidator).validateForCreate(validRequestDto);
            when(tweetMapper.toEntity(validRequestDto)).thenReturn(mappedTweet);
            when(tweetRepository.saveAndFlush(mappedTweet)).thenReturn(savedTweet);
            when(tweetMapper.toResponseDto(savedTweet)).thenReturn(responseDto);

            TweetResponseDto result = tweetService.createTweet(validRequestDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(savedTweet.getId());
            assertThat(result.getUserId()).isEqualTo(testUserId);
            assertThat(result.getContent()).isEqualTo("Hello World");
            assertThat(result.getCreatedAt()).isEqualTo(savedTweet.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(savedTweet.getUpdatedAt());
        }

        @Test
        void createTweet_WithValidData_ShouldCallEachDependencyExactlyOnce() {
            doNothing().when(tweetValidator).validateForCreate(validRequestDto);
            when(tweetMapper.toEntity(validRequestDto)).thenReturn(mappedTweet);
            when(tweetRepository.saveAndFlush(mappedTweet)).thenReturn(savedTweet);
            when(tweetMapper.toResponseDto(savedTweet)).thenReturn(responseDto);

            tweetService.createTweet(validRequestDto);

            verify(tweetValidator, times(1)).validateForCreate(eq(validRequestDto));
            verify(tweetMapper, times(1)).toEntity(eq(validRequestDto));
            verify(tweetRepository, times(1)).saveAndFlush(eq(mappedTweet));
            verify(tweetMapper, times(1)).toResponseDto(eq(savedTweet));
        }
    }
}

