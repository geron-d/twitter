package com.twitter.service;

import com.twitter.dto.request.RetweetRequestDto;
import com.twitter.dto.response.RetweetResponseDto;
import com.twitter.entity.Retweet;
import com.twitter.entity.Tweet;
import com.twitter.mapper.RetweetMapper;
import com.twitter.repository.RetweetRepository;
import com.twitter.repository.TweetRepository;
import com.twitter.validation.RetweetValidator;
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
class RetweetServiceImplTest {

    @Mock
    private RetweetRepository retweetRepository;

    @Mock
    private RetweetMapper retweetMapper;

    @Mock
    private RetweetValidator retweetValidator;

    @Mock
    private TweetRepository tweetRepository;

    @InjectMocks
    private RetweetServiceImpl retweetService;

    @Nested
    class RetweetTweetTests {

        private UUID testTweetId;
        private UUID testUserId;
        private UUID testAuthorId;
        private RetweetRequestDto requestDto;
        private RetweetRequestDto requestDtoWithComment;
        private Retweet mappedRetweet;
        private Retweet savedRetweet;
        private Tweet existingTweet;
        private RetweetResponseDto responseDto;
        private RetweetResponseDto responseDtoWithComment;

        @BeforeEach
        void setUp() {
            testTweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            testAuthorId = UUID.fromString("333e4567-e89b-12d3-a456-426614174002");

            requestDto = RetweetRequestDto.builder()
                .userId(testUserId)
                .comment(null)
                .build();

            requestDtoWithComment = RetweetRequestDto.builder()
                .userId(testUserId)
                .comment("Great tweet!")
                .build();

            mappedRetweet = Retweet.builder()
                .tweetId(testTweetId)
                .userId(testUserId)
                .comment(null)
                .build();

            UUID retweetId = UUID.fromString("987e6543-e21b-43d2-b654-321987654321");
            savedRetweet = Retweet.builder()
                .id(retweetId)
                .tweetId(testTweetId)
                .userId(testUserId)
                .comment(null)
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();

            existingTweet = Tweet.builder()
                .id(testTweetId)
                .userId(testAuthorId)
                .content("Test tweet content")
                .retweetsCount(0)
                .createdAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .build();

            responseDto = RetweetResponseDto.builder()
                .id(retweetId)
                .tweetId(testTweetId)
                .userId(testUserId)
                .comment(null)
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();

            responseDtoWithComment = RetweetResponseDto.builder()
                .id(retweetId)
                .tweetId(testTweetId)
                .userId(testUserId)
                .comment("Great tweet!")
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();
        }

        @Test
        void retweetTweet_WithValidData_ShouldReturnRetweetResponseDto() {
            doNothing().when(retweetValidator).validateForRetweet(testTweetId, requestDto);
            when(retweetMapper.toRetweet(requestDto, testTweetId)).thenReturn(mappedRetweet);
            when(retweetRepository.saveAndFlush(mappedRetweet)).thenReturn(savedRetweet);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class)))
                .thenAnswer(invocation -> invocation.<Tweet>getArgument(0));
            when(retweetMapper.toRetweetResponseDto(savedRetweet)).thenReturn(responseDto);

            RetweetResponseDto result = retweetService.retweetTweet(testTweetId, requestDto);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(savedRetweet.getId());
            assertThat(result.tweetId()).isEqualTo(testTweetId);
            assertThat(result.userId()).isEqualTo(testUserId);
            assertThat(result.comment()).isNull();
            assertThat(result.createdAt()).isEqualTo(savedRetweet.getCreatedAt());
        }

        @Test
        void retweetTweet_WithValidDataAndComment_ShouldReturnRetweetResponseDtoWithComment() {
            Retweet mappedRetweetWithComment = Retweet.builder()
                .tweetId(testTweetId)
                .userId(testUserId)
                .comment("Great tweet!")
                .build();

            Retweet savedRetweetWithComment = Retweet.builder()
                .id(savedRetweet.getId())
                .tweetId(testTweetId)
                .userId(testUserId)
                .comment("Great tweet!")
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();

            doNothing().when(retweetValidator).validateForRetweet(testTweetId, requestDtoWithComment);
            when(retweetMapper.toRetweet(requestDtoWithComment, testTweetId)).thenReturn(mappedRetweetWithComment);
            when(retweetRepository.saveAndFlush(mappedRetweetWithComment)).thenReturn(savedRetweetWithComment);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class)))
                .thenAnswer(invocation -> invocation.<Tweet>getArgument(0));
            when(retweetMapper.toRetweetResponseDto(savedRetweetWithComment)).thenReturn(responseDtoWithComment);

            RetweetResponseDto result = retweetService.retweetTweet(testTweetId, requestDtoWithComment);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(savedRetweetWithComment.getId());
            assertThat(result.tweetId()).isEqualTo(testTweetId);
            assertThat(result.userId()).isEqualTo(testUserId);
            assertThat(result.comment()).isEqualTo("Great tweet!");
            assertThat(result.createdAt()).isEqualTo(savedRetweetWithComment.getCreatedAt());
        }

        @Test
        void retweetTweet_WithValidData_ShouldCallEachDependencyExactlyOnce() {
            doNothing().when(retweetValidator).validateForRetweet(testTweetId, requestDto);
            when(retweetMapper.toRetweet(requestDto, testTweetId)).thenReturn(mappedRetweet);
            when(retweetRepository.saveAndFlush(mappedRetweet)).thenReturn(savedRetweet);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class)))
                .thenAnswer(invocation -> invocation.<Tweet>getArgument(0));
            when(retweetMapper.toRetweetResponseDto(savedRetweet)).thenReturn(responseDto);

            retweetService.retweetTweet(testTweetId, requestDto);

            verify(retweetValidator, times(1)).validateForRetweet(eq(testTweetId), eq(requestDto));
            verify(retweetMapper, times(1)).toRetweet(eq(requestDto), eq(testTweetId));
            verify(retweetRepository, times(1)).saveAndFlush(eq(mappedRetweet));
            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(eq(testTweetId));
            verify(tweetRepository, times(1)).saveAndFlush(any(Tweet.class));
            verify(retweetMapper, times(1)).toRetweetResponseDto(eq(savedRetweet));
        }

        @Test
        void retweetTweet_WithValidData_ShouldIncrementRetweetsCount() {
            doNothing().when(retweetValidator).validateForRetweet(testTweetId, requestDto);
            when(retweetMapper.toRetweet(requestDto, testTweetId)).thenReturn(mappedRetweet);
            when(retweetRepository.saveAndFlush(mappedRetweet)).thenReturn(savedRetweet);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class))).thenAnswer(invocation -> {
                Tweet tweet = invocation.getArgument(0);
                assertThat(tweet.getRetweetsCount()).isEqualTo(1);
                return tweet;
            });
            when(retweetMapper.toRetweetResponseDto(savedRetweet)).thenReturn(responseDto);

            retweetService.retweetTweet(testTweetId, requestDto);

            verify(tweetRepository, times(1)).saveAndFlush(argThat(tweet ->
                tweet.getRetweetsCount() == 1
            ));
        }

        @Test
        void retweetTweet_WhenValidationFails_ShouldThrowException() {
            doThrow(new com.twitter.common.exception.validation.BusinessRuleValidationException("TWEET_NOT_FOUND", testTweetId))
                .when(retweetValidator).validateForRetweet(testTweetId, requestDto);

            assertThatThrownBy(() -> retweetService.retweetTweet(testTweetId, requestDto))
                .isInstanceOf(com.twitter.common.exception.validation.BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    com.twitter.common.exception.validation.BusinessRuleValidationException ex =
                        (com.twitter.common.exception.validation.BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_NOT_FOUND");
                });

            verify(retweetValidator, times(1)).validateForRetweet(eq(testTweetId), eq(requestDto));
            verify(retweetMapper, never()).toRetweet(any(), any());
            verify(retweetRepository, never()).saveAndFlush(any());
            verify(tweetRepository, never()).findByIdAndIsDeletedFalse(any());
        }

        @Test
        void retweetTweet_WhenTweetNotFoundAfterValidation_ShouldThrowIllegalStateException() {
            doNothing().when(retweetValidator).validateForRetweet(testTweetId, requestDto);
            when(retweetMapper.toRetweet(requestDto, testTweetId)).thenReturn(mappedRetweet);
            when(retweetRepository.saveAndFlush(mappedRetweet)).thenReturn(savedRetweet);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> retweetService.retweetTweet(testTweetId, requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tweet not found after validation");

            verify(retweetValidator, times(1)).validateForRetweet(eq(testTweetId), eq(requestDto));
            verify(retweetMapper, times(1)).toRetweet(eq(requestDto), eq(testTweetId));
            verify(retweetRepository, times(1)).saveAndFlush(eq(mappedRetweet));
            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(eq(testTweetId));
            verify(tweetRepository, never()).saveAndFlush(any());
            verify(retweetMapper, never()).toRetweetResponseDto(any());
        }

        @Test
        void retweetTweet_WhenUniquenessValidationFails_ShouldThrowUniquenessValidationException() {
            doThrow(new com.twitter.common.exception.validation.UniquenessValidationException("retweet", "tweet " + testTweetId + " and user " + testUserId))
                .when(retweetValidator).validateForRetweet(testTweetId, requestDto);

            assertThatThrownBy(() -> retweetService.retweetTweet(testTweetId, requestDto))
                .isInstanceOf(com.twitter.common.exception.validation.UniquenessValidationException.class);

            verify(retweetValidator, times(1)).validateForRetweet(eq(testTweetId), eq(requestDto));
            verify(retweetMapper, never()).toRetweet(any(), any());
            verify(retweetRepository, never()).saveAndFlush(any());
            verify(tweetRepository, never()).findByIdAndIsDeletedFalse(any());
        }
    }

    @Nested
    class RemoveRetweetTests {

        private UUID testTweetId;
        private UUID testUserId;
        private UUID testAuthorId;
        private RetweetRequestDto requestDto;
        private Retweet existingRetweet;
        private Tweet existingTweet;

        @BeforeEach
        void setUp() {
            testTweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            testAuthorId = UUID.fromString("333e4567-e89b-12d3-a456-426614174002");

            requestDto = RetweetRequestDto.builder()
                .userId(testUserId)
                .comment(null)
                .build();

            UUID retweetId = UUID.fromString("987e6543-e21b-43d2-b654-321987654321");
            existingRetweet = Retweet.builder()
                .id(retweetId)
                .tweetId(testTweetId)
                .userId(testUserId)
                .comment(null)
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();

            existingTweet = Tweet.builder()
                .id(testTweetId)
                .userId(testAuthorId)
                .content("Test tweet content")
                .retweetsCount(5)
                .createdAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .build();
        }

        @Test
        void removeRetweet_WithValidData_ShouldCompleteWithoutExceptions() {
            doNothing().when(retweetValidator).validateForRemoveRetweet(testTweetId, requestDto);
            when(retweetRepository.findByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(Optional.of(existingRetweet));
            doNothing().when(retweetRepository).delete(existingRetweet);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class)))
                .thenAnswer(invocation -> invocation.<Tweet>getArgument(0));

            retweetService.removeRetweet(testTweetId, requestDto);

            verify(retweetValidator, times(1)).validateForRemoveRetweet(eq(testTweetId), eq(requestDto));
            verify(retweetRepository, times(1)).findByTweetIdAndUserId(eq(testTweetId), eq(testUserId));
            verify(retweetRepository, times(1)).delete(eq(existingRetweet));
            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(eq(testTweetId));
            verify(tweetRepository, times(1)).saveAndFlush(any(Tweet.class));
        }

        @Test
        void removeRetweet_WithValidData_ShouldCallEachDependencyExactlyOnce() {
            doNothing().when(retweetValidator).validateForRemoveRetweet(testTweetId, requestDto);
            when(retweetRepository.findByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(Optional.of(existingRetweet));
            doNothing().when(retweetRepository).delete(existingRetweet);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class)))
                .thenAnswer(invocation -> invocation.<Tweet>getArgument(0));

            retweetService.removeRetweet(testTweetId, requestDto);

            verify(retweetValidator, times(1)).validateForRemoveRetweet(eq(testTweetId), eq(requestDto));
            verify(retweetRepository, times(1)).findByTweetIdAndUserId(eq(testTweetId), eq(testUserId));
            verify(retweetRepository, times(1)).delete(eq(existingRetweet));
            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(eq(testTweetId));
            verify(tweetRepository, times(1)).saveAndFlush(any(Tweet.class));
        }

        @Test
        void removeRetweet_WithValidData_ShouldDecrementRetweetsCount() {
            doNothing().when(retweetValidator).validateForRemoveRetweet(testTweetId, requestDto);
            when(retweetRepository.findByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(Optional.of(existingRetweet));
            doNothing().when(retweetRepository).delete(existingRetweet);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class))).thenAnswer(invocation -> {
                Tweet tweet = invocation.getArgument(0);
                assertThat(tweet.getRetweetsCount()).isEqualTo(4);
                return tweet;
            });

            retweetService.removeRetweet(testTweetId, requestDto);

            verify(tweetRepository, times(1)).saveAndFlush(argThat(tweet ->
                tweet.getRetweetsCount() == 4
            ));
        }

        @Test
        void removeRetweet_WhenValidationFails_ShouldThrowException() {
            doThrow(new com.twitter.common.exception.validation.BusinessRuleValidationException("RETWEET_NOT_FOUND", "Retweet not found"))
                .when(retweetValidator).validateForRemoveRetweet(testTweetId, requestDto);

            assertThatThrownBy(() -> retweetService.removeRetweet(testTweetId, requestDto))
                .isInstanceOf(com.twitter.common.exception.validation.BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    com.twitter.common.exception.validation.BusinessRuleValidationException ex =
                        (com.twitter.common.exception.validation.BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("RETWEET_NOT_FOUND");
                });

            verify(retweetValidator, times(1)).validateForRemoveRetweet(eq(testTweetId), eq(requestDto));
            verify(retweetRepository, never()).findByTweetIdAndUserId(any(), any());
            verify(retweetRepository, never()).delete(any());
            verify(tweetRepository, never()).findByIdAndIsDeletedFalse(any());
            verify(tweetRepository, never()).saveAndFlush(any());
        }

        @Test
        void removeRetweet_WhenRetweetNotFoundAfterValidation_ShouldThrowIllegalStateException() {
            doNothing().when(retweetValidator).validateForRemoveRetweet(testTweetId, requestDto);
            when(retweetRepository.findByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> retweetService.removeRetweet(testTweetId, requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Retweet not found after validation");

            verify(retweetValidator, times(1)).validateForRemoveRetweet(eq(testTweetId), eq(requestDto));
            verify(retweetRepository, times(1)).findByTweetIdAndUserId(eq(testTweetId), eq(testUserId));
            verify(retweetRepository, never()).delete(any());
            verify(tweetRepository, never()).findByIdAndIsDeletedFalse(any());
            verify(tweetRepository, never()).saveAndFlush(any());
        }

        @Test
        void removeRetweet_WhenTweetNotFoundAfterValidation_ShouldThrowIllegalStateException() {
            doNothing().when(retweetValidator).validateForRemoveRetweet(testTweetId, requestDto);
            when(retweetRepository.findByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(Optional.of(existingRetweet));
            doNothing().when(retweetRepository).delete(existingRetweet);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> retweetService.removeRetweet(testTweetId, requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tweet not found after validation");

            verify(retweetValidator, times(1)).validateForRemoveRetweet(eq(testTweetId), eq(requestDto));
            verify(retweetRepository, times(1)).findByTweetIdAndUserId(eq(testTweetId), eq(testUserId));
            verify(retweetRepository, times(1)).delete(eq(existingRetweet));
            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(eq(testTweetId));
            verify(tweetRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    class GetRetweetsByTweetIdTests {

        private UUID testTweetId;
        private UUID testUserId1;
        private UUID testUserId2;
        private Retweet retweet1;
        private Retweet retweet2;
        private RetweetResponseDto responseDto1;
        private RetweetResponseDto responseDto2;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
            testTweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            testUserId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            testUserId2 = UUID.fromString("234e5678-f90c-23e4-b567-537725285112");

            UUID retweetId1 = UUID.fromString("987e6543-e21b-43d2-b654-321987654321");
            retweet1 = Retweet.builder()
                .id(retweetId1)
                .tweetId(testTweetId)
                .userId(testUserId1)
                .comment("Great tweet!")
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();

            UUID retweetId2 = UUID.fromString("876e5432-e10a-32c1-a543-210876543210");
            retweet2 = Retweet.builder()
                .id(retweetId2)
                .tweetId(testTweetId)
                .userId(testUserId2)
                .comment(null)
                .createdAt(LocalDateTime.of(2025, 1, 27, 14, 20, 0))
                .build();

            responseDto1 = RetweetResponseDto.builder()
                .id(retweetId1)
                .tweetId(testTweetId)
                .userId(testUserId1)
                .comment("Great tweet!")
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();

            responseDto2 = RetweetResponseDto.builder()
                .id(retweetId2)
                .tweetId(testTweetId)
                .userId(testUserId2)
                .comment(null)
                .createdAt(LocalDateTime.of(2025, 1, 27, 14, 20, 0))
                .build();

            pageable = PageRequest.of(0, 20);
        }

        @Test
        void getRetweetsByTweetId_WhenRetweetsExist_ShouldReturnPageWithRetweets() {
            List<Retweet> retweets = List.of(retweet1, retweet2);
            Page<Retweet> retweetPage = new PageImpl<>(retweets, pageable, 2);

            doNothing().when(retweetValidator).validateTweetExists(testTweetId);
            when(retweetRepository.findByTweetIdOrderByCreatedAtDesc(eq(testTweetId), eq(pageable)))
                .thenReturn(retweetPage);
            when(retweetMapper.toRetweetResponseDto(retweet1)).thenReturn(responseDto1);
            when(retweetMapper.toRetweetResponseDto(retweet2)).thenReturn(responseDto2);

            Page<RetweetResponseDto> result = retweetService.getRetweetsByTweetId(testTweetId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(responseDto1, responseDto2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getTotalPages()).isEqualTo(1);
        }

        @Test
        void getRetweetsByTweetId_WhenNoRetweetsExist_ShouldReturnEmptyPage() {
            Page<Retweet> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            doNothing().when(retweetValidator).validateTweetExists(testTweetId);
            when(retweetRepository.findByTweetIdOrderByCreatedAtDesc(eq(testTweetId), eq(pageable)))
                .thenReturn(emptyPage);

            Page<RetweetResponseDto> result = retweetService.getRetweetsByTweetId(testTweetId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getTotalPages()).isEqualTo(0);
        }

        @Test
        void getRetweetsByTweetId_WhenRetweetsExist_ShouldCallValidatorRepositoryAndMapper() {
            List<Retweet> retweets = List.of(retweet1, retweet2);
            Page<Retweet> retweetPage = new PageImpl<>(retweets, pageable, 2);

            doNothing().when(retweetValidator).validateTweetExists(testTweetId);
            when(retweetRepository.findByTweetIdOrderByCreatedAtDesc(eq(testTweetId), eq(pageable)))
                .thenReturn(retweetPage);
            when(retweetMapper.toRetweetResponseDto(retweet1)).thenReturn(responseDto1);
            when(retweetMapper.toRetweetResponseDto(retweet2)).thenReturn(responseDto2);

            retweetService.getRetweetsByTweetId(testTweetId, pageable);

            verify(retweetValidator, times(1)).validateTweetExists(eq(testTweetId));
            verify(retweetRepository, times(1))
                .findByTweetIdOrderByCreatedAtDesc(eq(testTweetId), eq(pageable));
            verify(retweetMapper, times(1)).toRetweetResponseDto(eq(retweet1));
            verify(retweetMapper, times(1)).toRetweetResponseDto(eq(retweet2));
            verifyNoMoreInteractions(retweetValidator, retweetRepository, retweetMapper);
        }

        @Test
        void getRetweetsByTweetId_WhenTweetNotFound_ShouldThrowBusinessRuleValidationException() {
            doThrow(new com.twitter.common.exception.validation.BusinessRuleValidationException("TWEET_NOT_FOUND", testTweetId))
                .when(retweetValidator).validateTweetExists(testTweetId);

            assertThatThrownBy(() -> retweetService.getRetweetsByTweetId(testTweetId, pageable))
                .isInstanceOf(com.twitter.common.exception.validation.BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    com.twitter.common.exception.validation.BusinessRuleValidationException ex =
                        (com.twitter.common.exception.validation.BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_NOT_FOUND");
                });

            verify(retweetValidator, times(1)).validateTweetExists(eq(testTweetId));
            verify(retweetRepository, never()).findByTweetIdOrderByCreatedAtDesc(any(), any());
            verify(retweetMapper, never()).toRetweetResponseDto(any());
        }

        @Test
        void getRetweetsByTweetId_WithPagination_ShouldReturnCorrectPage() {
            Pageable secondPage = PageRequest.of(1, 10);
            List<Retweet> retweets = List.of(retweet1);
            Page<Retweet> retweetPage = new PageImpl<>(retweets, secondPage, 11);

            doNothing().when(retweetValidator).validateTweetExists(testTweetId);
            when(retweetRepository.findByTweetIdOrderByCreatedAtDesc(eq(testTweetId), eq(secondPage)))
                .thenReturn(retweetPage);
            when(retweetMapper.toRetweetResponseDto(retweet1)).thenReturn(responseDto1);

            Page<RetweetResponseDto> result = retweetService.getRetweetsByTweetId(testTweetId, secondPage);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).containsExactly(responseDto1);
            assertThat(result.getTotalElements()).isEqualTo(11);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getTotalPages()).isEqualTo(2);
        }
    }
}