package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.request.RetweetRequestDto;
import com.twitter.entity.Tweet;
import com.twitter.gateway.UserGateway;
import com.twitter.repository.RetweetRepository;
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
class RetweetValidatorImplTest {

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private RetweetRepository retweetRepository;

    @Mock
    private UserGateway userGateway;

    @InjectMocks
    private RetweetValidatorImpl retweetValidator;

    @Nested
    class ValidateForRetweetTests {

        private UUID testTweetId;
        private UUID testUserId;
        private UUID testAuthorId;
        private RetweetRequestDto requestDto;
        private RetweetRequestDto requestDtoWithComment;
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

            requestDtoWithComment = RetweetRequestDto.builder()
                .userId(testUserId)
                .comment("Great tweet!")
                .build();

            existingTweet = Tweet.builder()
                .id(testTweetId)
                .userId(testAuthorId)
                .content("Test tweet content")
                .retweetsCount(0)
                .createdAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .isDeleted(false)
                .build();
        }

        @Test
        void validateForRetweet_WhenValidData_ShouldCompleteWithoutExceptions() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testUserId)).thenReturn(true);
            when(retweetRepository.existsByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(false);

            assertThatCode(() -> retweetValidator.validateForRetweet(testTweetId, requestDto))
                .doesNotThrowAnyException();

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testUserId);
            verify(retweetRepository, times(1)).existsByTweetIdAndUserId(testTweetId, testUserId);
        }

        @Test
        void validateForRetweet_WhenValidDataWithComment_ShouldCompleteWithoutExceptions() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testUserId)).thenReturn(true);
            when(retweetRepository.existsByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(false);

            assertThatCode(() -> retweetValidator.validateForRetweet(testTweetId, requestDtoWithComment))
                .doesNotThrowAnyException();

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testUserId);
            verify(retweetRepository, times(1)).existsByTweetIdAndUserId(testTweetId, testUserId);
        }

        @Test
        void validateForRetweet_WhenTweetIdIsNull_ShouldThrowBusinessRuleValidationException() {
            assertThatThrownBy(() -> retweetValidator.validateForRetweet(null, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_ID_NULL");
                });

            verify(tweetRepository, never()).findByIdAndIsDeletedFalse(any());
            verify(userGateway, never()).existsUser(any());
            verify(retweetRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForRetweet_WhenTweetNotFound_ShouldThrowBusinessRuleValidationException() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> retweetValidator.validateForRetweet(testTweetId, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_NOT_FOUND");
                    assertThat(ex.getContext()).isEqualTo(testTweetId);
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, never()).existsUser(any());
            verify(retweetRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForRetweet_WhenRequestDtoIsNull_ShouldThrowBusinessRuleValidationException() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));

            assertThatThrownBy(() -> retweetValidator.validateForRetweet(testTweetId, null))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("RETWEET_REQUEST_NULL");
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, never()).existsUser(any());
            verify(retweetRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForRetweet_WhenUserIdIsNull_ShouldThrowBusinessRuleValidationException() {
            RetweetRequestDto nullUserIdRequest = RetweetRequestDto.builder()
                .userId(null)
                .comment(null)
                .build();

            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));

            assertThatThrownBy(() -> retweetValidator.validateForRetweet(testTweetId, nullUserIdRequest))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("USER_ID_NULL");
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, never()).existsUser(any());
            verify(retweetRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForRetweet_WhenUserDoesNotExist_ShouldThrowBusinessRuleValidationException() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testUserId)).thenReturn(false);

            assertThatThrownBy(() -> retweetValidator.validateForRetweet(testTweetId, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("USER_NOT_EXISTS");
                    assertThat(ex.getContext()).isEqualTo(testUserId);
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testUserId);
            verify(retweetRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForRetweet_WhenSelfRetweet_ShouldThrowBusinessRuleValidationException() {
            RetweetRequestDto selfRetweetRequest = RetweetRequestDto.builder()
                .userId(testAuthorId)
                .comment(null)
                .build();

            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testAuthorId)).thenReturn(true);

            assertThatThrownBy(() -> retweetValidator.validateForRetweet(testTweetId, selfRetweetRequest))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("SELF_RETWEET_NOT_ALLOWED");
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testAuthorId);
            verify(retweetRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForRetweet_WhenDuplicateRetweet_ShouldThrowUniquenessValidationException() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testUserId)).thenReturn(true);
            when(retweetRepository.existsByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(true);

            assertThatThrownBy(() -> retweetValidator.validateForRetweet(testTweetId, requestDto))
                .isInstanceOf(UniquenessValidationException.class)
                .satisfies(exception -> {
                    UniquenessValidationException ex = (UniquenessValidationException) exception;
                    assertThat(ex.getFieldName()).isEqualTo("retweet");
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testUserId);
            verify(retweetRepository, times(1)).existsByTweetIdAndUserId(testTweetId, testUserId);
        }

        @Test
        void validateForRetweet_WhenCommentIsEmptyString_ShouldThrowFormatValidationException() {
            RetweetRequestDto emptyCommentRequest = RetweetRequestDto.builder()
                .userId(testUserId)
                .comment("")
                .build();

            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testUserId)).thenReturn(true);
            when(retweetRepository.existsByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(false);

            assertThatThrownBy(() -> retweetValidator.validateForRetweet(testTweetId, emptyCommentRequest))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getFieldName()).isEqualTo("comment");
                    assertThat(ex.getConstraintName()).isEqualTo("NOT_EMPTY");
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testUserId);
            verify(retweetRepository, times(1)).existsByTweetIdAndUserId(testTweetId, testUserId);
        }

        @Test
        void validateForRetweet_WhenCommentIsWhitespaceOnly_ShouldThrowFormatValidationException() {
            RetweetRequestDto whitespaceCommentRequest = RetweetRequestDto.builder()
                .userId(testUserId)
                .comment("   ")
                .build();

            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testUserId)).thenReturn(true);
            when(retweetRepository.existsByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(false);

            assertThatThrownBy(() -> retweetValidator.validateForRetweet(testTweetId, whitespaceCommentRequest))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getFieldName()).isEqualTo("comment");
                    assertThat(ex.getConstraintName()).isEqualTo("NOT_EMPTY");
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testUserId);
            verify(retweetRepository, times(1)).existsByTweetIdAndUserId(testTweetId, testUserId);
        }

        @Test
        void validateForRetweet_WhenCommentExceedsMaxLength_ShouldThrowFormatValidationException() {
            String longComment = "A".repeat(281);
            RetweetRequestDto longCommentRequest = RetweetRequestDto.builder()
                .userId(testUserId)
                .comment(longComment)
                .build();

            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testUserId)).thenReturn(true);
            when(retweetRepository.existsByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(false);

            assertThatThrownBy(() -> retweetValidator.validateForRetweet(testTweetId, longCommentRequest))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getFieldName()).isEqualTo("comment");
                    assertThat(ex.getConstraintName()).isEqualTo("MAX_LENGTH");
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testUserId);
            verify(retweetRepository, times(1)).existsByTweetIdAndUserId(testTweetId, testUserId);
        }

        @Test
        void validateForRetweet_WhenCommentIsExactlyMaxLength_ShouldCompleteWithoutExceptions() {
            String maxLengthComment = "A".repeat(280);
            RetweetRequestDto maxLengthCommentRequest = RetweetRequestDto.builder()
                .userId(testUserId)
                .comment(maxLengthComment)
                .build();

            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testUserId)).thenReturn(true);
            when(retweetRepository.existsByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(false);

            assertThatCode(() -> retweetValidator.validateForRetweet(testTweetId, maxLengthCommentRequest))
                .doesNotThrowAnyException();

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testUserId);
            verify(retweetRepository, times(1)).existsByTweetIdAndUserId(testTweetId, testUserId);
        }
    }

    @Nested
    class ValidateForRemoveRetweetTests {

        private UUID testTweetId;
        private UUID testUserId;
        private UUID testAuthorId;
        private RetweetRequestDto requestDto;
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

            existingTweet = Tweet.builder()
                .id(testTweetId)
                .userId(testAuthorId)
                .content("Test tweet content")
                .retweetsCount(5)
                .createdAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .isDeleted(false)
                .build();
        }

        @Test
        void validateForRemoveRetweet_WhenValidData_ShouldCompleteWithoutExceptions() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testUserId)).thenReturn(true);
            when(retweetRepository.existsByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(true);

            assertThatCode(() -> retweetValidator.validateForRemoveRetweet(testTweetId, requestDto))
                .doesNotThrowAnyException();

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testUserId);
            verify(retweetRepository, times(1)).existsByTweetIdAndUserId(testTweetId, testUserId);
        }

        @Test
        void validateForRemoveRetweet_WhenTweetIdIsNull_ShouldThrowBusinessRuleValidationException() {
            assertThatThrownBy(() -> retweetValidator.validateForRemoveRetweet(null, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_ID_NULL");
                });

            verify(tweetRepository, never()).findByIdAndIsDeletedFalse(any());
            verify(userGateway, never()).existsUser(any());
            verify(retweetRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForRemoveRetweet_WhenTweetNotFound_ShouldThrowBusinessRuleValidationException() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> retweetValidator.validateForRemoveRetweet(testTweetId, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_NOT_FOUND");
                    assertThat(ex.getContext()).isEqualTo(testTweetId);
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, never()).existsUser(any());
            verify(retweetRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForRemoveRetweet_WhenRequestDtoIsNull_ShouldThrowBusinessRuleValidationException() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));

            assertThatThrownBy(() -> retweetValidator.validateForRemoveRetweet(testTweetId, null))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("RETWEET_REQUEST_NULL");
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, never()).existsUser(any());
            verify(retweetRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForRemoveRetweet_WhenUserIdIsNull_ShouldThrowBusinessRuleValidationException() {
            RetweetRequestDto nullUserIdRequest = RetweetRequestDto.builder()
                .userId(null)
                .comment(null)
                .build();

            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));

            assertThatThrownBy(() -> retweetValidator.validateForRemoveRetweet(testTweetId, nullUserIdRequest))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("USER_ID_NULL");
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, never()).existsUser(any());
            verify(retweetRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForRemoveRetweet_WhenUserDoesNotExist_ShouldThrowBusinessRuleValidationException() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testUserId)).thenReturn(false);

            assertThatThrownBy(() -> retweetValidator.validateForRemoveRetweet(testTweetId, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("USER_NOT_EXISTS");
                    assertThat(ex.getContext()).isEqualTo(testUserId);
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testUserId);
            verify(retweetRepository, never()).existsByTweetIdAndUserId(any(), any());
        }

        @Test
        void validateForRemoveRetweet_WhenRetweetDoesNotExist_ShouldThrowBusinessRuleValidationException() {
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(userGateway.existsUser(testUserId)).thenReturn(true);
            when(retweetRepository.existsByTweetIdAndUserId(testTweetId, testUserId)).thenReturn(false);

            assertThatThrownBy(() -> retweetValidator.validateForRemoveRetweet(testTweetId, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("RETWEET_NOT_FOUND");
                });

            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(testTweetId);
            verify(userGateway, times(1)).existsUser(testUserId);
            verify(retweetRepository, times(1)).existsByTweetIdAndUserId(testTweetId, testUserId);
        }
    }
}