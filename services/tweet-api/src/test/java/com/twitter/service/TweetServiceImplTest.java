package com.twitter.service;

import com.twitter.common.dto.request.tweet.CreateTweetRequestDto;
import com.twitter.common.dto.request.tweet.DeleteTweetRequestDto;
import com.twitter.common.dto.response.tweet.TweetResponseDto;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.dto.request.UpdateTweetRequestDto;
import com.twitter.entity.Tweet;
import com.twitter.gateway.FollowerGateway;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private FollowerGateway followerGateway;

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
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(foundTweet));
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
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.empty());

            Optional<TweetResponseDto> result = tweetService.getTweetById(testTweetId);

            assertThat(result).isEmpty();
        }

        @Test
        void getTweetById_WhenTweetExists_ShouldCallRepositoryAndMapper() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(foundTweet));
            when(tweetMapper.toResponseDto(foundTweet)).thenReturn(responseDto);

            tweetService.getTweetById(testTweetId);

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(eq(testTweetId));
            verify(tweetMapper, times(1)).toResponseDto(eq(foundTweet));
            verifyNoMoreInteractions(tweetRepository, tweetMapper);
        }

        @Test
        void getTweetById_WhenTweetDoesNotExist_ShouldCallRepositoryOnly() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.empty());

            tweetService.getTweetById(testTweetId);

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(eq(testTweetId));
            verifyNoInteractions(tweetMapper);
        }

        @Test
        void getTweetById_WhenTweetIsDeleted_ShouldReturnEmptyOptional() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.empty());

            Optional<TweetResponseDto> result = tweetService.getTweetById(testTweetId);

            assertThat(result).isEmpty();
            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(eq(testTweetId));
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

    @Nested
    class DeleteTweetTests {

        private UUID testTweetId;
        private DeleteTweetRequestDto deleteRequestDto;
        private Tweet existingTweet;

        @BeforeEach
        void setUp() {
            testTweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

            deleteRequestDto = DeleteTweetRequestDto.builder()
                .userId(testUserId)
                .build();

            existingTweet = Tweet.builder()
                .id(testTweetId)
                .userId(testUserId)
                .content("Tweet to be deleted")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .isDeleted(false)
                .build();
        }

        @Test
        void deleteTweet_WithValidData_ShouldPerformSoftDelete() {
            doNothing().when(tweetValidator).validateForDelete(testTweetId, deleteRequestDto);
            when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class))).thenAnswer(invocation -> invocation.<Tweet>getArgument(0));

            tweetService.deleteTweet(testTweetId, deleteRequestDto);

            assertThat(existingTweet.getIsDeleted()).isTrue();
            assertThat(existingTweet.getDeletedAt()).isNotNull();
        }

        @Test
        void deleteTweet_WithValidData_ShouldCallEachDependencyExactlyOnce() {
            doNothing().when(tweetValidator).validateForDelete(testTweetId, deleteRequestDto);
            when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class))).thenReturn(existingTweet);

            tweetService.deleteTweet(testTweetId, deleteRequestDto);

            verify(tweetValidator, times(1)).validateForDelete(eq(testTweetId), eq(deleteRequestDto));
            verify(tweetRepository, times(1)).findById(eq(testTweetId));
            verify(tweetRepository, times(1)).saveAndFlush(eq(existingTweet));
            assertThat(existingTweet.getIsDeleted()).isTrue();
            assertThat(existingTweet.getDeletedAt()).isNotNull();
        }

        @Test
        void deleteTweet_WhenTweetNotFound_ShouldThrowBusinessRuleValidationException() {
            BusinessRuleValidationException businessException = new BusinessRuleValidationException(
                "TWEET_NOT_FOUND",
                testTweetId
            );
            doThrow(businessException)
                .when(tweetValidator).validateForDelete(testTweetId, deleteRequestDto);

            assertThatThrownBy(() -> tweetService.deleteTweet(testTweetId, deleteRequestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .isEqualTo(businessException);

            verify(tweetValidator, times(1)).validateForDelete(eq(testTweetId), eq(deleteRequestDto));
            verify(tweetRepository, never()).findById(any());
            verify(tweetRepository, never()).saveAndFlush(any());
        }

        @Test
        void deleteTweet_WhenAccessDenied_ShouldThrowBusinessRuleValidationException() {
            BusinessRuleValidationException businessException = new BusinessRuleValidationException(
                "TWEET_ACCESS_DENIED",
                "Only the tweet author can delete their tweet"
            );
            doThrow(businessException)
                .when(tweetValidator).validateForDelete(testTweetId, deleteRequestDto);

            assertThatThrownBy(() -> tweetService.deleteTweet(testTweetId, deleteRequestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .isEqualTo(businessException);

            verify(tweetValidator, times(1)).validateForDelete(eq(testTweetId), eq(deleteRequestDto));
            verify(tweetRepository, never()).findById(any());
            verify(tweetRepository, never()).saveAndFlush(any());
        }

        @Test
        void deleteTweet_WhenTweetAlreadyDeleted_ShouldThrowBusinessRuleValidationException() {
            BusinessRuleValidationException businessException = new BusinessRuleValidationException(
                "TWEET_ALREADY_DELETED",
                testTweetId
            );
            doThrow(businessException)
                .when(tweetValidator).validateForDelete(testTweetId, deleteRequestDto);

            assertThatThrownBy(() -> tweetService.deleteTweet(testTweetId, deleteRequestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .isEqualTo(businessException);

            verify(tweetValidator, times(1)).validateForDelete(eq(testTweetId), eq(deleteRequestDto));
            verify(tweetRepository, never()).findById(any());
            verify(tweetRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    class GetUserTweetsTests {

        private UUID testUserId;
        private Pageable pageable;
        private Tweet tweet1;
        private Tweet tweet2;
        private TweetResponseDto responseDto1;
        private TweetResponseDto responseDto2;

        @BeforeEach
        void setUp() {
            testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

            UUID tweetId1 = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            UUID tweetId2 = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");

            tweet1 = Tweet.builder()
                .id(tweetId1)
                .userId(testUserId)
                .content("First tweet")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .isDeleted(false)
                .build();

            tweet2 = Tweet.builder()
                .id(tweetId2)
                .userId(testUserId)
                .content("Second tweet")
                .createdAt(LocalDateTime.of(2024, 1, 14, 9, 15, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 14, 9, 15, 0))
                .isDeleted(false)
                .build();

            responseDto1 = TweetResponseDto.builder()
                .id(tweetId1)
                .userId(testUserId)
                .content("First tweet")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .isDeleted(false)
                .deletedAt(null)
                .build();

            responseDto2 = TweetResponseDto.builder()
                .id(tweetId2)
                .userId(testUserId)
                .content("Second tweet")
                .createdAt(LocalDateTime.of(2024, 1, 14, 9, 15, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 14, 9, 15, 0))
                .isDeleted(false)
                .deletedAt(null)
                .build();

            pageable = PageRequest.of(0, 20);
        }

        @Test
        void getUserTweets_WhenTweetsExist_ShouldReturnPageWithTweets() {
            List<Tweet> tweets = List.of(tweet1, tweet2);
            Page<Tweet> tweetPage = new PageImpl<>(tweets, pageable, 2);

            when(tweetRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(eq(testUserId), eq(pageable)))
                .thenReturn(tweetPage);
            when(tweetMapper.toResponseDto(tweet1)).thenReturn(responseDto1);
            when(tweetMapper.toResponseDto(tweet2)).thenReturn(responseDto2);

            Page<TweetResponseDto> result = tweetService.getUserTweets(testUserId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(responseDto1, responseDto2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getTotalPages()).isEqualTo(1);
        }

        @Test
        void getUserTweets_WhenNoTweetsExist_ShouldReturnEmptyPage() {
            Page<Tweet> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(tweetRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(eq(testUserId), eq(pageable)))
                .thenReturn(emptyPage);

            Page<TweetResponseDto> result = tweetService.getUserTweets(testUserId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getTotalPages()).isEqualTo(0);
        }

        @Test
        void getUserTweets_WhenTweetsExist_ShouldCallRepositoryAndMapper() {
            List<Tweet> tweets = List.of(tweet1, tweet2);
            Page<Tweet> tweetPage = new PageImpl<>(tweets, pageable, 2);

            when(tweetRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(eq(testUserId), eq(pageable)))
                .thenReturn(tweetPage);
            when(tweetMapper.toResponseDto(tweet1)).thenReturn(responseDto1);
            when(tweetMapper.toResponseDto(tweet2)).thenReturn(responseDto2);

            tweetService.getUserTweets(testUserId, pageable);

            verify(tweetRepository, times(1))
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(eq(testUserId), eq(pageable));
            verify(tweetMapper, times(1)).toResponseDto(eq(tweet1));
            verify(tweetMapper, times(1)).toResponseDto(eq(tweet2));
            verifyNoMoreInteractions(tweetRepository, tweetMapper);
        }

        @Test
        void getUserTweets_WhenNoTweetsExist_ShouldCallRepositoryOnly() {
            Page<Tweet> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(tweetRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(eq(testUserId), eq(pageable)))
                .thenReturn(emptyPage);

            tweetService.getUserTweets(testUserId, pageable);

            verify(tweetRepository, times(1))
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(eq(testUserId), eq(pageable));
            verifyNoInteractions(tweetMapper);
        }
    }

    @Nested
    class GetTimelineTests {

        private UUID testUserId;
        private UUID followingUserId1;
        private UUID followingUserId2;
        private Pageable pageable;
        private Tweet tweet1;
        private Tweet tweet2;
        private TweetResponseDto responseDto1;
        private TweetResponseDto responseDto2;

        @BeforeEach
        void setUp() {
            testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            followingUserId1 = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            followingUserId2 = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");

            UUID tweetId1 = UUID.fromString("423e4567-e89b-12d3-a456-426614174003");
            UUID tweetId2 = UUID.fromString("523e4567-e89b-12d3-a456-426614174004");

            tweet1 = Tweet.builder()
                .id(tweetId1)
                .userId(followingUserId1)
                .content("Tweet from followed user 1")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .isDeleted(false)
                .build();

            tweet2 = Tweet.builder()
                .id(tweetId2)
                .userId(followingUserId2)
                .content("Tweet from followed user 2")
                .createdAt(LocalDateTime.of(2024, 1, 14, 9, 15, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 14, 9, 15, 0))
                .isDeleted(false)
                .build();

            responseDto1 = TweetResponseDto.builder()
                .id(tweetId1)
                .userId(followingUserId1)
                .content("Tweet from followed user 1")
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .isDeleted(false)
                .deletedAt(null)
                .build();

            responseDto2 = TweetResponseDto.builder()
                .id(tweetId2)
                .userId(followingUserId2)
                .content("Tweet from followed user 2")
                .createdAt(LocalDateTime.of(2024, 1, 14, 9, 15, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 14, 9, 15, 0))
                .isDeleted(false)
                .deletedAt(null)
                .build();

            pageable = PageRequest.of(0, 20);
        }

        @Test
        void getTimeline_WhenFollowingUsersHaveTweets_ShouldReturnPageWithTweets() {
            List<UUID> followingUserIds = List.of(followingUserId1, followingUserId2);
            List<Tweet> tweets = List.of(tweet1, tweet2);
            Page<Tweet> tweetPage = new PageImpl<>(tweets, pageable, 2);

            doNothing().when(tweetValidator).validateForTimeline(testUserId);
            when(followerGateway.getFollowingUserIds(testUserId)).thenReturn(followingUserIds);
            when(tweetRepository.findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc(eq(followingUserIds), eq(pageable)))
                .thenReturn(tweetPage);
            when(tweetMapper.toResponseDto(tweet1)).thenReturn(responseDto1);
            when(tweetMapper.toResponseDto(tweet2)).thenReturn(responseDto2);

            Page<TweetResponseDto> result = tweetService.getTimeline(testUserId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(responseDto1, responseDto2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getTotalPages()).isEqualTo(1);
        }

        @Test
        void getTimeline_WhenNoFollowingUsers_ShouldReturnEmptyPage() {
            List<UUID> emptyFollowingUserIds = List.of();

            doNothing().when(tweetValidator).validateForTimeline(testUserId);
            when(followerGateway.getFollowingUserIds(testUserId)).thenReturn(emptyFollowingUserIds);

            Page<TweetResponseDto> result = tweetService.getTimeline(testUserId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getTotalPages()).isEqualTo(0);
        }

        @Test
        void getTimeline_WhenFollowingUsersHaveNoTweets_ShouldReturnEmptyPage() {
            List<UUID> followingUserIds = List.of(followingUserId1, followingUserId2);
            Page<Tweet> emptyTweetPage = new PageImpl<>(List.of(), pageable, 0);

            doNothing().when(tweetValidator).validateForTimeline(testUserId);
            when(followerGateway.getFollowingUserIds(testUserId)).thenReturn(followingUserIds);
            when(tweetRepository.findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc(eq(followingUserIds), eq(pageable)))
                .thenReturn(emptyTweetPage);

            Page<TweetResponseDto> result = tweetService.getTimeline(testUserId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getTotalPages()).isEqualTo(0);
        }

        @Test
        void getTimeline_WhenFollowingUsersHaveTweets_ShouldCallEachDependencyExactlyOnce() {
            List<UUID> followingUserIds = List.of(followingUserId1, followingUserId2);
            List<Tweet> tweets = List.of(tweet1, tweet2);
            Page<Tweet> tweetPage = new PageImpl<>(tweets, pageable, 2);

            doNothing().when(tweetValidator).validateForTimeline(testUserId);
            when(followerGateway.getFollowingUserIds(testUserId)).thenReturn(followingUserIds);
            when(tweetRepository.findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc(eq(followingUserIds), eq(pageable)))
                .thenReturn(tweetPage);
            when(tweetMapper.toResponseDto(tweet1)).thenReturn(responseDto1);
            when(tweetMapper.toResponseDto(tweet2)).thenReturn(responseDto2);

            tweetService.getTimeline(testUserId, pageable);

            verify(tweetValidator, times(1)).validateForTimeline(eq(testUserId));
            verify(followerGateway, times(1)).getFollowingUserIds(eq(testUserId));
            verify(tweetRepository, times(1))
                .findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc(eq(followingUserIds), eq(pageable));
            verify(tweetMapper, times(1)).toResponseDto(eq(tweet1));
            verify(tweetMapper, times(1)).toResponseDto(eq(tweet2));
        }

        @Test
        void getTimeline_WhenValidationFails_ShouldThrowBusinessRuleValidationException() {
            BusinessRuleValidationException validationException = new BusinessRuleValidationException(
                "USER_NOT_EXISTS",
                testUserId
            );
            doThrow(validationException)
                .when(tweetValidator).validateForTimeline(testUserId);

            assertThatThrownBy(() -> tweetService.getTimeline(testUserId, pageable))
                .isInstanceOf(BusinessRuleValidationException.class)
                .isEqualTo(validationException);

            verify(tweetValidator, times(1)).validateForTimeline(eq(testUserId));
            verify(followerGateway, never()).getFollowingUserIds(any());
            verify(tweetRepository, never())
                .findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc(any(), any());
            verifyNoInteractions(tweetMapper);
        }
    }
}