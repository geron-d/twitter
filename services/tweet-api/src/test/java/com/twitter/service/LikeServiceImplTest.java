package com.twitter.service;

import com.twitter.dto.request.LikeTweetRequestDto;
import com.twitter.dto.response.LikeResponseDto;
import com.twitter.entity.Like;
import com.twitter.entity.Tweet;
import com.twitter.mapper.LikeMapper;
import com.twitter.repository.LikeRepository;
import com.twitter.repository.TweetRepository;
import com.twitter.validation.LikeValidator;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private LikeMapper likeMapper;

    @Mock
    private LikeValidator likeValidator;

    @Mock
    private TweetRepository tweetRepository;

    @InjectMocks
    private LikeServiceImpl likeService;

    @Nested
    class LikeTweetTests {

        private UUID testTweetId;
        private UUID testUserId;
        private UUID testAuthorId;
        private LikeTweetRequestDto requestDto;
        private Like mappedLike;
        private Like savedLike;
        private Tweet existingTweet;
        private LikeResponseDto responseDto;

        @BeforeEach
        void setUp() {
            testTweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            testAuthorId = UUID.fromString("333e4567-e89b-12d3-a456-426614174002");

            requestDto = LikeTweetRequestDto.builder()
                .userId(testUserId)
                .build();

            mappedLike = Like.builder()
                .tweetId(testTweetId)
                .userId(testUserId)
                .build();

            UUID likeId = UUID.fromString("987e6543-e21b-43d2-b654-321987654321");
            savedLike = Like.builder()
                .id(likeId)
                .tweetId(testTweetId)
                .userId(testUserId)
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();

            existingTweet = Tweet.builder()
                .id(testTweetId)
                .userId(testAuthorId)
                .content("Test tweet content")
                .likesCount(0)
                .createdAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .build();

            responseDto = LikeResponseDto.builder()
                .id(likeId)
                .tweetId(testTweetId)
                .userId(testUserId)
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();
        }

        @Test
        void likeTweet_WithValidData_ShouldReturnLikeResponseDto() {
            doNothing().when(likeValidator).validateForLike(testTweetId, requestDto);
            when(likeMapper.toLike(requestDto, testTweetId)).thenReturn(mappedLike);
            when(likeRepository.saveAndFlush(mappedLike)).thenReturn(savedLike);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class)))
                .thenAnswer(invocation -> invocation.<Tweet>getArgument(0));
            when(likeMapper.toLikeResponseDto(savedLike)).thenReturn(responseDto);

            LikeResponseDto result = likeService.likeTweet(testTweetId, requestDto);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(savedLike.getId());
            assertThat(result.tweetId()).isEqualTo(testTweetId);
            assertThat(result.userId()).isEqualTo(testUserId);
            assertThat(result.createdAt()).isEqualTo(savedLike.getCreatedAt());
        }

        @Test
        void likeTweet_WithValidData_ShouldCallEachDependencyExactlyOnce() {
            doNothing().when(likeValidator).validateForLike(testTweetId, requestDto);
            when(likeMapper.toLike(requestDto, testTweetId)).thenReturn(mappedLike);
            when(likeRepository.saveAndFlush(mappedLike)).thenReturn(savedLike);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class))).thenAnswer(invocation -> {
                Tweet tweet = invocation.getArgument(0);
                return tweet;
            });
            when(likeMapper.toLikeResponseDto(savedLike)).thenReturn(responseDto);

            likeService.likeTweet(testTweetId, requestDto);

            verify(likeValidator, times(1)).validateForLike(eq(testTweetId), eq(requestDto));
            verify(likeMapper, times(1)).toLike(eq(requestDto), eq(testTweetId));
            verify(likeRepository, times(1)).saveAndFlush(eq(mappedLike));
            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(eq(testTweetId));
            verify(tweetRepository, times(1)).saveAndFlush(any(Tweet.class));
            verify(likeMapper, times(1)).toLikeResponseDto(eq(savedLike));
        }

        @Test
        void likeTweet_WithValidData_ShouldIncrementLikesCount() {
            doNothing().when(likeValidator).validateForLike(testTweetId, requestDto);
            when(likeMapper.toLike(requestDto, testTweetId)).thenReturn(mappedLike);
            when(likeRepository.saveAndFlush(mappedLike)).thenReturn(savedLike);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class))).thenAnswer(invocation -> {
                Tweet tweet = invocation.getArgument(0);
                assertThat(tweet.getLikesCount()).isEqualTo(1);
                return tweet;
            });
            when(likeMapper.toLikeResponseDto(savedLike)).thenReturn(responseDto);

            likeService.likeTweet(testTweetId, requestDto);

            verify(tweetRepository, times(1)).saveAndFlush(argThat(tweet ->
                tweet.getLikesCount() == 1
            ));
        }

        @Test
        void likeTweet_WhenValidationFails_ShouldThrowException() {
            doThrow(new com.twitter.common.exception.validation.BusinessRuleValidationException("TWEET_NOT_FOUND", testTweetId))
                .when(likeValidator).validateForLike(testTweetId, requestDto);

            assertThatThrownBy(() -> likeService.likeTweet(testTweetId, requestDto))
                .isInstanceOf(com.twitter.common.exception.validation.BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    com.twitter.common.exception.validation.BusinessRuleValidationException ex =
                        (com.twitter.common.exception.validation.BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_NOT_FOUND");
                });

            verify(likeValidator, times(1)).validateForLike(eq(testTweetId), eq(requestDto));
            verify(likeMapper, never()).toLike(any(), any());
            verify(likeRepository, never()).saveAndFlush(any());
            verify(tweetRepository, never()).findByIdAndIsDeletedFalse(any());
        }

        @Test
        void likeTweet_WhenTweetNotFoundAfterValidation_ShouldThrowIllegalStateException() {
            doNothing().when(likeValidator).validateForLike(testTweetId, requestDto);
            when(likeMapper.toLike(requestDto, testTweetId)).thenReturn(mappedLike);
            when(likeRepository.saveAndFlush(mappedLike)).thenReturn(savedLike);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> likeService.likeTweet(testTweetId, requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tweet not found after validation");

            verify(likeValidator, times(1)).validateForLike(eq(testTweetId), eq(requestDto));
            verify(likeMapper, times(1)).toLike(eq(requestDto), eq(testTweetId));
            verify(likeRepository, times(1)).saveAndFlush(eq(mappedLike));
            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(eq(testTweetId));
            verify(tweetRepository, never()).saveAndFlush(any());
            verify(likeMapper, never()).toLikeResponseDto(any());
        }

        @Test
        void likeTweet_WhenUniquenessValidationFails_ShouldThrowUniquenessValidationException() {
            doThrow(new com.twitter.common.exception.validation.UniquenessValidationException("like", "tweet " + testTweetId + " and user " + testUserId))
                .when(likeValidator).validateForLike(testTweetId, requestDto);

            assertThatThrownBy(() -> likeService.likeTweet(testTweetId, requestDto))
                .isInstanceOf(com.twitter.common.exception.validation.UniquenessValidationException.class);

            verify(likeValidator, times(1)).validateForLike(eq(testTweetId), eq(requestDto));
            verify(likeMapper, never()).toLike(any(), any());
            verify(likeRepository, never()).saveAndFlush(any());
            verify(tweetRepository, never()).findByIdAndIsDeletedFalse(any());
        }
    }
}

