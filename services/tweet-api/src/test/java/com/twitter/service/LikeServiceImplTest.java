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

    @Nested
    class RemoveLikeTests {

        private UUID testTweetId;
        private UUID testUserId;
        private UUID testAuthorId;
        private LikeTweetRequestDto requestDto;
        private Like existingLike;
        private Tweet existingTweet;

        @BeforeEach
        void setUp() {
            testTweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            testAuthorId = UUID.fromString("333e4567-e89b-12d3-a456-426614174002");

            requestDto = LikeTweetRequestDto.builder()
                .userId(testUserId)
                .build();

            UUID likeId = UUID.fromString("987e6543-e21b-43d2-b654-321987654321");
            existingLike = Like.builder()
                .id(likeId)
                .tweetId(testTweetId)
                .userId(testUserId)
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();

            existingTweet = Tweet.builder()
                .id(testTweetId)
                .userId(testAuthorId)
                .content("Test tweet content")
                .likesCount(5)
                .createdAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .build();
        }

        @Test
        void removeLike_WithValidData_ShouldCompleteWithoutExceptions() {
            doNothing().when(likeValidator).validateForUnlike(testTweetId, requestDto);
            when(likeRepository.findByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(Optional.of(existingLike));
            doNothing().when(likeRepository).delete(existingLike);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class)))
                .thenAnswer(invocation -> invocation.<Tweet>getArgument(0));

            likeService.removeLike(testTweetId, requestDto);

            verify(likeValidator, times(1)).validateForUnlike(eq(testTweetId), eq(requestDto));
            verify(likeRepository, times(1)).findByTweetIdAndUserId(eq(testTweetId), eq(testUserId));
            verify(likeRepository, times(1)).delete(eq(existingLike));
            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(eq(testTweetId));
            verify(tweetRepository, times(1)).saveAndFlush(any(Tweet.class));
        }

        @Test
        void removeLike_WithValidData_ShouldCallEachDependencyExactlyOnce() {
            doNothing().when(likeValidator).validateForUnlike(testTweetId, requestDto);
            when(likeRepository.findByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(Optional.of(existingLike));
            doNothing().when(likeRepository).delete(existingLike);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class)))
                .thenAnswer(invocation -> invocation.<Tweet>getArgument(0));

            likeService.removeLike(testTweetId, requestDto);

            verify(likeValidator, times(1)).validateForUnlike(eq(testTweetId), eq(requestDto));
            verify(likeRepository, times(1)).findByTweetIdAndUserId(eq(testTweetId), eq(testUserId));
            verify(likeRepository, times(1)).delete(eq(existingLike));
            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(eq(testTweetId));
            verify(tweetRepository, times(1)).saveAndFlush(any(Tweet.class));
        }

        @Test
        void removeLike_WithValidData_ShouldDecrementLikesCount() {
            doNothing().when(likeValidator).validateForUnlike(testTweetId, requestDto);
            when(likeRepository.findByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(Optional.of(existingLike));
            doNothing().when(likeRepository).delete(existingLike);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class))).thenAnswer(invocation -> {
                Tweet tweet = invocation.getArgument(0);
                assertThat(tweet.getLikesCount()).isEqualTo(4);
                return tweet;
            });

            likeService.removeLike(testTweetId, requestDto);

            verify(tweetRepository, times(1)).saveAndFlush(argThat(tweet ->
                tweet.getLikesCount() == 4
            ));
        }

        @Test
        void removeLike_WhenValidationFails_ShouldThrowException() {
            doThrow(new com.twitter.common.exception.validation.BusinessRuleValidationException("LIKE_NOT_FOUND", "Like not found"))
                .when(likeValidator).validateForUnlike(testTweetId, requestDto);

            assertThatThrownBy(() -> likeService.removeLike(testTweetId, requestDto))
                .isInstanceOf(com.twitter.common.exception.validation.BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    com.twitter.common.exception.validation.BusinessRuleValidationException ex =
                        (com.twitter.common.exception.validation.BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("LIKE_NOT_FOUND");
                });

            verify(likeValidator, times(1)).validateForUnlike(eq(testTweetId), eq(requestDto));
            verify(likeRepository, never()).findByTweetIdAndUserId(any(), any());
            verify(likeRepository, never()).delete(any());
            verify(tweetRepository, never()).findByIdAndIsDeletedFalse(any());
            verify(tweetRepository, never()).saveAndFlush(any());
        }

        @Test
        void removeLike_WhenLikeNotFoundAfterValidation_ShouldThrowIllegalStateException() {
            doNothing().when(likeValidator).validateForUnlike(testTweetId, requestDto);
            when(likeRepository.findByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> likeService.removeLike(testTweetId, requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Like not found after validation");

            verify(likeValidator, times(1)).validateForUnlike(eq(testTweetId), eq(requestDto));
            verify(likeRepository, times(1)).findByTweetIdAndUserId(eq(testTweetId), eq(testUserId));
            verify(likeRepository, never()).delete(any());
            verify(tweetRepository, never()).findByIdAndIsDeletedFalse(any());
            verify(tweetRepository, never()).saveAndFlush(any());
        }

        @Test
        void removeLike_WhenTweetNotFoundAfterValidation_ShouldThrowIllegalStateException() {
            doNothing().when(likeValidator).validateForUnlike(testTweetId, requestDto);
            when(likeRepository.findByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(Optional.of(existingLike));
            doNothing().when(likeRepository).delete(existingLike);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> likeService.removeLike(testTweetId, requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tweet not found after validation");

            verify(likeValidator, times(1)).validateForUnlike(eq(testTweetId), eq(requestDto));
            verify(likeRepository, times(1)).findByTweetIdAndUserId(eq(testTweetId), eq(testUserId));
            verify(likeRepository, times(1)).delete(eq(existingLike));
            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(eq(testTweetId));
            verify(tweetRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    class GetLikesByTweetIdTests {

        private UUID testTweetId;
        private UUID testUserId1;
        private UUID testUserId2;
        private Pageable pageable;
        private Like like1;
        private Like like2;
        private LikeResponseDto responseDto1;
        private LikeResponseDto responseDto2;

        @BeforeEach
        void setUp() {
            testTweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            testUserId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            testUserId2 = UUID.fromString("234e5678-f90c-23e4-b567-537725285112");

            UUID likeId1 = UUID.fromString("987e6543-e21b-43d2-b654-321987654321");
            UUID likeId2 = UUID.fromString("876e5432-e10a-32c1-a543-210876543210");

            like1 = Like.builder()
                .id(likeId1)
                .tweetId(testTweetId)
                .userId(testUserId1)
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();

            like2 = Like.builder()
                .id(likeId2)
                .tweetId(testTweetId)
                .userId(testUserId2)
                .createdAt(LocalDateTime.of(2025, 1, 27, 14, 20, 0))
                .build();

            responseDto1 = LikeResponseDto.builder()
                .id(likeId1)
                .tweetId(testTweetId)
                .userId(testUserId1)
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();

            responseDto2 = LikeResponseDto.builder()
                .id(likeId2)
                .tweetId(testTweetId)
                .userId(testUserId2)
                .createdAt(LocalDateTime.of(2025, 1, 27, 14, 20, 0))
                .build();

            pageable = PageRequest.of(0, 20);
        }

        @Test
        void getLikesByTweetId_WhenLikesExist_ShouldReturnPageWithLikes() {
            List<Like> likes = List.of(like1, like2);
            Page<Like> likePage = new PageImpl<>(likes, pageable, 2);

            doNothing().when(likeValidator).validateTweetExists(testTweetId);
            when(likeRepository.findByTweetIdOrderByCreatedAtDesc(eq(testTweetId), eq(pageable)))
                .thenReturn(likePage);
            when(likeMapper.toLikeResponseDto(like1)).thenReturn(responseDto1);
            when(likeMapper.toLikeResponseDto(like2)).thenReturn(responseDto2);

            Page<LikeResponseDto> result = likeService.getLikesByTweetId(testTweetId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(responseDto1, responseDto2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getTotalPages()).isEqualTo(1);
        }

        @Test
        void getLikesByTweetId_WhenNoLikesExist_ShouldReturnEmptyPage() {
            Page<Like> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            doNothing().when(likeValidator).validateTweetExists(testTweetId);
            when(likeRepository.findByTweetIdOrderByCreatedAtDesc(eq(testTweetId), eq(pageable)))
                .thenReturn(emptyPage);

            Page<LikeResponseDto> result = likeService.getLikesByTweetId(testTweetId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getTotalPages()).isEqualTo(0);
        }

        @Test
        void getLikesByTweetId_WhenLikesExist_ShouldCallValidatorRepositoryAndMapper() {
            List<Like> likes = List.of(like1, like2);
            Page<Like> likePage = new PageImpl<>(likes, pageable, 2);

            doNothing().when(likeValidator).validateTweetExists(testTweetId);
            when(likeRepository.findByTweetIdOrderByCreatedAtDesc(eq(testTweetId), eq(pageable)))
                .thenReturn(likePage);
            when(likeMapper.toLikeResponseDto(like1)).thenReturn(responseDto1);
            when(likeMapper.toLikeResponseDto(like2)).thenReturn(responseDto2);

            likeService.getLikesByTweetId(testTweetId, pageable);

            verify(likeValidator, times(1)).validateTweetExists(eq(testTweetId));
            verify(likeRepository, times(1))
                .findByTweetIdOrderByCreatedAtDesc(eq(testTweetId), eq(pageable));
            verify(likeMapper, times(1)).toLikeResponseDto(eq(like1));
            verify(likeMapper, times(1)).toLikeResponseDto(eq(like2));
            verifyNoMoreInteractions(likeValidator, likeRepository, likeMapper);
        }

        @Test
        void getLikesByTweetId_WhenTweetNotFound_ShouldThrowBusinessRuleValidationException() {
            doThrow(new com.twitter.common.exception.validation.BusinessRuleValidationException("TWEET_NOT_FOUND", testTweetId))
                .when(likeValidator).validateTweetExists(testTweetId);

            assertThatThrownBy(() -> likeService.getLikesByTweetId(testTweetId, pageable))
                .isInstanceOf(com.twitter.common.exception.validation.BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    com.twitter.common.exception.validation.BusinessRuleValidationException ex =
                        (com.twitter.common.exception.validation.BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_NOT_FOUND");
                });

            verify(likeValidator, times(1)).validateTweetExists(eq(testTweetId));
            verify(likeRepository, never()).findByTweetIdOrderByCreatedAtDesc(any(), any());
            verify(likeMapper, never()).toLikeResponseDto(any());
        }

        @Test
        void getLikesByTweetId_WithPagination_ShouldReturnCorrectPage() {
            Pageable secondPage = PageRequest.of(1, 10);
            List<Like> likes = List.of(like1);
            Page<Like> likePage = new PageImpl<>(likes, secondPage, 11);

            doNothing().when(likeValidator).validateTweetExists(testTweetId);
            when(likeRepository.findByTweetIdOrderByCreatedAtDesc(eq(testTweetId), eq(secondPage)))
                .thenReturn(likePage);
            when(likeMapper.toLikeResponseDto(like1)).thenReturn(responseDto1);

            Page<LikeResponseDto> result = likeService.getLikesByTweetId(testTweetId, secondPage);

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
