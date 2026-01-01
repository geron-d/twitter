package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.request.LikeTweetRequestDto;
import com.twitter.entity.Tweet;
import com.twitter.gateway.UserGateway;
import com.twitter.repository.LikeRepository;
import com.twitter.repository.TweetRepository;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeValidatorImplTest {

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserGateway userGateway;

    @InjectMocks
    private LikeValidatorImpl likeValidator;

    @Nested
    class ValidateForLikeTests {

        private UUID testTweetId;
        private UUID testUserId;
        private UUID testAuthorId;
        private LikeTweetRequestDto requestDto;
        private Tweet existingTweet;

        @BeforeEach
        void setUp() {
            testTweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            testAuthorId = UUID.fromString("333e4567-e89b-12d3-a456-426614174002");

            requestDto = LikeTweetRequestDto.builder()
                .userId(testUserId)
                .build();

            existingTweet = Tweet.builder()
                .id(testTweetId)
                .userId(testAuthorId)
                .content("Test tweet content")
                .likesCount(0)
                .createdAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .isDeleted(false)
                .build();
        }

        @Test
        void validateForLike_WhenValidData_ShouldCompleteWithoutExceptions() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testUserId)).thenReturn(true);
            when(likeRepository.existsByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(false);

            assertThatCode(() -> likeValidator.validateForLike(testTweetId, requestDto))
                .doesNotThrowAnyException();

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testUserId);
            verify(likeRepository, times(1)).existsByTweetIdAndUserId(testTweetId, testUserId);
        }

        @Test
        void validateForLike_WhenTweetIdIsNull_ShouldThrowBusinessRuleValidationException() {
            assertThatThrownBy(() -> likeValidator.validateForLike(null, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_ID_NULL");
                });

            verify(tweetRepository, never()).findByIdAndIsDeletedFalse(any());
            verify(userGateway, never()).existsUser(any());
            verify(likeRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForLike_WhenTweetNotFound_ShouldThrowBusinessRuleValidationException() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> likeValidator.validateForLike(testTweetId, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_NOT_FOUND");
                    assertThat(ex.getContext()).isEqualTo(testTweetId);
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, never()).existsUser(any());
            verify(likeRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForLike_WhenRequestDtoIsNull_ShouldThrowBusinessRuleValidationException() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));

            assertThatThrownBy(() -> likeValidator.validateForLike(testTweetId, null))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("LIKE_REQUEST_NULL");
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, never()).existsUser(any());
            verify(likeRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForLike_WhenUserIdIsNull_ShouldThrowBusinessRuleValidationException() {
            LikeTweetRequestDto nullUserIdRequest = LikeTweetRequestDto.builder()
                .userId(null)
                .build();

            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));

            assertThatThrownBy(() -> likeValidator.validateForLike(testTweetId, nullUserIdRequest))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("USER_ID_NULL");
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, never()).existsUser(any());
            verify(likeRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForLike_WhenUserDoesNotExist_ShouldThrowBusinessRuleValidationException() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testUserId)).thenReturn(false);

            assertThatThrownBy(() -> likeValidator.validateForLike(testTweetId, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("USER_NOT_EXISTS");
                    assertThat(ex.getContext()).isEqualTo(testUserId);
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testUserId);
            verify(likeRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForLike_WhenSelfLike_ShouldThrowBusinessRuleValidationException() {
            LikeTweetRequestDto selfLikeRequest = LikeTweetRequestDto.builder()
                .userId(testAuthorId)
                .build();

            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testAuthorId)).thenReturn(true);

            assertThatThrownBy(() -> likeValidator.validateForLike(testTweetId, selfLikeRequest))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("SELF_LIKE_NOT_ALLOWED");
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testAuthorId);
            verify(likeRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForLike_WhenDuplicateLike_ShouldThrowUniquenessValidationException() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testUserId)).thenReturn(true);
            when(likeRepository.existsByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(true);

            assertThatThrownBy(() -> likeValidator.validateForLike(testTweetId, requestDto))
                .isInstanceOf(UniquenessValidationException.class)
                .satisfies(exception -> {
                    UniquenessValidationException ex = (UniquenessValidationException) exception;
                    assertThat(ex.getFieldName()).isEqualTo("like");
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testUserId);
            verify(likeRepository, times(1)).existsByTweetIdAndUserId(testTweetId, testUserId);
        }
    }
}

