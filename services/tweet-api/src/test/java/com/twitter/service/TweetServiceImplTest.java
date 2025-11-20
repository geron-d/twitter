package com.twitter.service;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.request.UpdateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import com.twitter.entity.Tweet;
import com.twitter.mapper.TweetMapper;
import com.twitter.repository.TweetRepository;
import com.twitter.validation.TweetValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
            assertThat(result.id()).isEqualTo(savedTweet.getId());
            assertThat(result.userId()).isEqualTo(testUserId);
            assertThat(result.content()).isEqualTo("Hello World");
            assertThat(result.createdAt()).isEqualTo(savedTweet.getCreatedAt());
            assertThat(result.updatedAt()).isEqualTo(savedTweet.getUpdatedAt());
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

    @Nested
    class GetTweetByIdTests {

        private UUID testTweetId;
        private UUID testUserId;
        private Tweet foundTweet;
        private TweetResponseDto responseDto;

        @BeforeEach
        void setUp() {
            testTweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

            foundTweet = Tweet.builder()
                .id(testTweetId)
                .userId(testUserId)
                .content("Test tweet content")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build();

            responseDto = TweetResponseDto.builder()
                .id(testTweetId)
                .userId(testUserId)
                .content("Test tweet content")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build();
        }

        @Test
        void getTweetById_WhenTweetExists_ShouldReturnOptionalWithTweetResponseDto() {
            when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(foundTweet));
            when(tweetMapper.toResponseDto(foundTweet)).thenReturn(responseDto);

            Optional<TweetResponseDto> result = tweetService.getTweetById(testTweetId);

            assertThat(result).isPresent();
            assertThat(result.get()).isNotNull();
            assertThat(result.get().id()).isEqualTo(testTweetId);
            assertThat(result.get().userId()).isEqualTo(testUserId);
            assertThat(result.get().content()).isEqualTo("Test tweet content");
            assertThat(result.get().createdAt()).isEqualTo(foundTweet.getCreatedAt());
            assertThat(result.get().updatedAt()).isEqualTo(foundTweet.getUpdatedAt());
        }

        @Test
        void getTweetById_WhenTweetDoesNotExist_ShouldReturnEmptyOptional() {
            when(tweetRepository.findById(testTweetId)).thenReturn(Optional.empty());

            Optional<TweetResponseDto> result = tweetService.getTweetById(testTweetId);

            assertThat(result).isEmpty();
        }

        @Test
        void getTweetById_WhenTweetExists_ShouldCallRepositoryAndMapper() {
            when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(foundTweet));
            when(tweetMapper.toResponseDto(foundTweet)).thenReturn(responseDto);

            tweetService.getTweetById(testTweetId);

            verify(tweetRepository, times(1)).findById(eq(testTweetId));
            verify(tweetMapper, times(1)).toResponseDto(eq(foundTweet));
            verifyNoMoreInteractions(tweetRepository, tweetMapper);
        }

        @Test
        void getTweetById_WhenTweetDoesNotExist_ShouldCallRepositoryOnly() {
            when(tweetRepository.findById(testTweetId)).thenReturn(Optional.empty());

            tweetService.getTweetById(testTweetId);

            verify(tweetRepository, times(1)).findById(eq(testTweetId));
            verifyNoInteractions(tweetMapper);
        }
    }

    @Nested
    class UpdateTweetTests {

        private UUID testTweetId;
        private UUID testUserId;
        private UpdateTweetRequestDto updateRequestDto;
        private Tweet existingTweet;
        private Tweet updatedTweet;
        private TweetResponseDto responseDto;

        @BeforeEach
        void setUp() {
            testTweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

            updateRequestDto = UpdateTweetRequestDto.builder()
                .content("Updated tweet content")
                .userId(testUserId)
                .build();

            existingTweet = Tweet.builder()
                .id(testTweetId)
                .userId(testUserId)
                .content("Original tweet content")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build();

            updatedTweet = Tweet.builder()
                .id(testTweetId)
                .userId(testUserId)
                .content("Updated tweet content")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 16, 11, 45, 0))
                .build();

            responseDto = TweetResponseDto.builder()
                .id(testTweetId)
                .userId(testUserId)
                .content("Updated tweet content")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 16, 11, 45, 0))
                .build();
        }

        @Test
        void updateTweet_WithValidData_ShouldReturnTweetResponseDto() {
            doNothing().when(tweetValidator).validateForUpdate(testTweetId, updateRequestDto);
            when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(existingTweet));
            doNothing().when(tweetMapper).updateTweetFromUpdateDto(updateRequestDto, existingTweet);
            when(tweetRepository.saveAndFlush(existingTweet)).thenReturn(updatedTweet);
            when(tweetMapper.toResponseDto(updatedTweet)).thenReturn(responseDto);

            TweetResponseDto result = tweetService.updateTweet(testTweetId, updateRequestDto);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(testTweetId);
            assertThat(result.userId()).isEqualTo(testUserId);
            assertThat(result.content()).isEqualTo("Updated tweet content");
            assertThat(result.createdAt()).isEqualTo(existingTweet.getCreatedAt());
            assertThat(result.updatedAt()).isEqualTo(updatedTweet.getUpdatedAt());
        }

        @Test
        void updateTweet_WithValidData_ShouldCallEachDependencyExactlyOnce() {
            doNothing().when(tweetValidator).validateForUpdate(testTweetId, updateRequestDto);
            when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(existingTweet));
            doNothing().when(tweetMapper).updateTweetFromUpdateDto(updateRequestDto, existingTweet);
            when(tweetRepository.saveAndFlush(existingTweet)).thenReturn(updatedTweet);
            when(tweetMapper.toResponseDto(updatedTweet)).thenReturn(responseDto);

            tweetService.updateTweet(testTweetId, updateRequestDto);

            verify(tweetValidator, times(1)).validateForUpdate(eq(testTweetId), eq(updateRequestDto));
            verify(tweetRepository, times(1)).findById(eq(testTweetId));
            verify(tweetMapper, times(1)).updateTweetFromUpdateDto(eq(updateRequestDto), eq(existingTweet));
            verify(tweetRepository, times(1)).saveAndFlush(eq(existingTweet));
            verify(tweetMapper, times(1)).toResponseDto(eq(updatedTweet));
        }

        @Test
        void updateTweet_WhenValidationFails_ShouldThrowFormatValidationException() {
            FormatValidationException validationException = new FormatValidationException(
                "content",
                "CONTENT_VALIDATION",
                "Tweet content cannot be empty"
            );
            doThrow(validationException)
                .when(tweetValidator).validateForUpdate(testTweetId, updateRequestDto);

            assertThatThrownBy(() -> tweetService.updateTweet(testTweetId, updateRequestDto))
                .isInstanceOf(FormatValidationException.class)
                .isEqualTo(validationException);

            verify(tweetValidator, times(1)).validateForUpdate(eq(testTweetId), eq(updateRequestDto));
            verify(tweetRepository, never()).findById(any());
            verify(tweetMapper, never()).updateTweetFromUpdateDto(any(), any());
            verify(tweetRepository, never()).saveAndFlush(any());
            verify(tweetMapper, never()).toResponseDto(any());
        }

        @Test
        void updateTweet_WhenBusinessRuleViolation_ShouldThrowBusinessRuleValidationException() {
            BusinessRuleValidationException businessException = new BusinessRuleValidationException(
                "TWEET_NOT_FOUND",
                "tweetId=" + testTweetId
            );
            doThrow(businessException)
                .when(tweetValidator).validateForUpdate(testTweetId, updateRequestDto);

            assertThatThrownBy(() -> tweetService.updateTweet(testTweetId, updateRequestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .isEqualTo(businessException);

            verify(tweetValidator, times(1)).validateForUpdate(eq(testTweetId), eq(updateRequestDto));
            verify(tweetRepository, never()).findById(any());
            verify(tweetMapper, never()).updateTweetFromUpdateDto(any(), any());
            verify(tweetRepository, never()).saveAndFlush(any());
            verify(tweetMapper, never()).toResponseDto(any());
        }
    }
}

