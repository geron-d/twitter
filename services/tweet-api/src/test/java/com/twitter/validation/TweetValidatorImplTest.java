package com.twitter.validation;

import com.twitter.common.dto.request.tweet.CreateTweetRequestDto;
import com.twitter.common.dto.request.tweet.DeleteTweetRequestDto;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.dto.request.UpdateTweetRequestDto;
import com.twitter.entity.Tweet;
import com.twitter.gateway.UserGateway;
import com.twitter.repository.TweetRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TweetValidatorImplTest {

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private UserGateway userGateway;

    @Mock
    private TweetRepository tweetRepository;

    @InjectMocks
    private TweetValidatorImpl tweetValidator;

    @Nested
    class ValidateForCreateTests {

        @Test
        void validateForCreate_WhenValidData_ShouldCompleteWithoutExceptions() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            String validContent = "This is a valid tweet content";
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content(validContent)
                .userId(validUserId)
                .build();

            when(userGateway.existsUser(validUserId)).thenReturn(true);

            tweetValidator.validateForCreate(requestDto);

            verify(userGateway, times(1)).existsUser(validUserId);
        }

        @Test
        void validateForCreate_WhenContentIsEmptyString_ShouldThrowFormatValidationException() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content("")
                .userId(validUserId)
                .build();

            assertThatThrownBy(() -> tweetValidator.validateForCreate(requestDto))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getConstraintName()).isEqualTo("CONTENT_VALIDATION");
                });

            verify(userGateway, never()).existsUser(any());
        }

        @Test
        void validateForCreate_WhenContentExceedsMaxLength_ShouldThrowFormatValidationException() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            String content281Chars = "a".repeat(281);
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content(content281Chars)
                .userId(validUserId)
                .build();

            assertThatThrownBy(() -> tweetValidator.validateForCreate(requestDto))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getConstraintName()).isEqualTo("CONTENT_VALIDATION");
                });

            verify(userGateway, never()).existsUser(any());
        }

        @Test
        void validateForCreate_WhenContentIsNull_ShouldThrowFormatValidationException() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content(null)
                .userId(validUserId)
                .build();

            assertThatThrownBy(() -> tweetValidator.validateForCreate(requestDto))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getConstraintName()).isEqualTo("CONTENT_VALIDATION");
                });

            verify(userGateway, never()).existsUser(any());
        }

        @Test
        void validateForCreate_WhenUserIdIsNull_ShouldThrowBusinessRuleValidationException() {
            String validContent = "This is a valid tweet content";
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content(validContent)
                .userId(null)
                .build();

            assertThatThrownBy(() -> tweetValidator.validateForCreate(requestDto))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getConstraintName()).isEqualTo("CONTENT_VALIDATION");
                    assertThat(ex.getMessage()).contains("userId");
                });

            verify(userGateway, never()).existsUser(any());
        }

        @Test
        void validateForCreate_WhenUserDoesNotExist_ShouldThrowBusinessRuleValidationException() {
            UUID nonExistentUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            String validContent = "This is a valid tweet content";
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content(validContent)
                .userId(nonExistentUserId)
                .build();

            when(userGateway.existsUser(nonExistentUserId)).thenReturn(false);

            assertThatThrownBy(() -> tweetValidator.validateForCreate(requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("USER_NOT_EXISTS");
                    assertThat(ex.getContext()).isEqualTo(nonExistentUserId);
                });

            verify(userGateway, times(1)).existsUser(nonExistentUserId);
        }

        @Test
        void validateForCreate_WhenMultipleValidationViolations_ShouldThrowFormatValidationExceptionWithAllViolations() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content(null)
                .userId(null)
                .build();

            assertThatThrownBy(() -> tweetValidator.validateForCreate(requestDto))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getConstraintName()).isEqualTo("CONTENT_VALIDATION");
                    assertThat(ex.getMessage()).contains("content");
                    assertThat(ex.getMessage()).contains("userId");
                });

            verify(userGateway, never()).existsUser(any());
        }

        @Test
        void validateForCreate_WhenRequestDtoIsNull_ShouldThrowException() {
            assertThatThrownBy(() -> tweetValidator.validateForCreate(null))
                .isInstanceOfAny(
                    FormatValidationException.class,
                    NullPointerException.class,
                    IllegalArgumentException.class
                );

            verify(userGateway, never()).existsUser(any());
        }

        @Test
        void validateForCreate_WhenBeanValidationAnnotationsViolated_ShouldThrowFormatValidationException() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content("")
                .userId(validUserId)
                .build();

            assertThatThrownBy(() -> tweetValidator.validateForCreate(requestDto))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getConstraintName()).isEqualTo("CONTENT_VALIDATION");
                    assertThat(ex.getFieldName()).isEqualTo("content");
                });

            verify(userGateway, never()).existsUser(any());
        }
    }

    @Nested
    class ValidateContentTests {

        @Test
        void validateContent_WhenValidData_ShouldCompleteWithoutExceptions() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            String validContent = "This is a valid tweet content";
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content(validContent)
                .userId(validUserId)
                .build();

            assertThatCode(() -> tweetValidator.validateContent(requestDto))
                .doesNotThrowAnyException();

            verify(validator, times(1)).validate(requestDto);
        }

        @Test
        void validateContent_WhenContentIsNull_ShouldThrowFormatValidationException() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content(null)
                .userId(validUserId)
                .build();

            assertThatThrownBy(() -> tweetValidator.validateContent(requestDto))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getFieldName()).isEqualTo("content");
                    assertThat(ex.getConstraintName()).isEqualTo("CONTENT_VALIDATION");
                    assertThat(ex.getMessage()).contains("content");
                });

            verify(validator, times(1)).validate(requestDto);
        }

        @Test
        void validateContent_WhenContentIsEmptyString_ShouldThrowFormatValidationException() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content("")
                .userId(validUserId)
                .build();

            assertThatThrownBy(() -> tweetValidator.validateContent(requestDto))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getFieldName()).isEqualTo("content");
                    assertThat(ex.getConstraintName()).isEqualTo("CONTENT_VALIDATION");
                });

            verify(validator, times(1)).validate(requestDto);
        }

        @Test
        void validateContent_WhenContentExceedsMaxLength_ShouldThrowFormatValidationException() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            String content281Chars = "a".repeat(281);
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content(content281Chars)
                .userId(validUserId)
                .build();

            assertThatThrownBy(() -> tweetValidator.validateContent(requestDto))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getFieldName()).isEqualTo("content");
                    assertThat(ex.getConstraintName()).isEqualTo("CONTENT_VALIDATION");
                    assertThat(ex.getMessage()).contains("content");
                });

            verify(validator, times(1)).validate(requestDto);
        }

        @Test
        void validateContent_WhenMultipleValidationViolations_ShouldThrowFormatValidationExceptionWithAllViolations() {
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content(null)
                .userId(null)
                .build();

            assertThatThrownBy(() -> tweetValidator.validateContent(requestDto))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getFieldName()).isEqualTo("content");
                    assertThat(ex.getConstraintName()).isEqualTo("CONTENT_VALIDATION");
                    assertThat(ex.getMessage()).contains("content");
                    assertThat(ex.getMessage()).contains("userId");
                });

            verify(validator, times(1)).validate(requestDto);
        }

        @Test
        void validateContent_WhenRequestDtoIsNull_ShouldThrowException() {
            assertThatThrownBy(() -> tweetValidator.validateContent(null))
                .isInstanceOfAny(
                    FormatValidationException.class,
                    NullPointerException.class,
                    IllegalArgumentException.class
                );
        }

        @Test
        void validateContent_WhenValidatorReturnsViolations_ShouldFormatErrorMessageCorrectly() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content("")
                .userId(validUserId)
                .build();

            assertThatThrownBy(() -> tweetValidator.validateContent(requestDto))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    String errorMessage = ex.getMessage();
                    assertThat(errorMessage).contains("content");
                    assertThat(errorMessage.split(", ").length).isGreaterThanOrEqualTo(1);
                });

            verify(validator, times(1)).validate(requestDto);
        }
    }

    @Nested
    class ValidateUserExistsTests {

        @Test
        void validateUserExists_WhenValidUserIdAndUserExists_ShouldCompleteWithoutExceptions() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            when(userGateway.existsUser(validUserId)).thenReturn(true);

            assertThatCode(() -> tweetValidator.validateUserExists(validUserId))
                .doesNotThrowAnyException();

            verify(userGateway, times(1)).existsUser(validUserId);
        }

        @Test
        void validateUserExists_WhenUserIdIsNull_ShouldThrowBusinessRuleValidationException() {
            assertThatThrownBy(() -> tweetValidator.validateUserExists(null))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("USER_ID_NULL");
                    assertThat(ex.getContext()).isEqualTo("User ID cannot be null");
                });

            verify(userGateway, never()).existsUser(any());
        }

        @Test
        void validateUserExists_WhenUserDoesNotExist_ShouldThrowBusinessRuleValidationException() {
            UUID nonExistentUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            when(userGateway.existsUser(nonExistentUserId)).thenReturn(false);

            assertThatThrownBy(() -> tweetValidator.validateUserExists(nonExistentUserId))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("USER_NOT_EXISTS");
                    assertThat(ex.getContext()).isEqualTo(nonExistentUserId);
                });

            verify(userGateway, times(1)).existsUser(nonExistentUserId);
        }
    }

    @Nested
    class ValidateForUpdateTests {

        @Test
        void validateForUpdate_WhenValidData_ShouldCompleteWithoutExceptions() {
            UUID tweetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID authorUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
            String validContent = "This is updated tweet content";
            UpdateTweetRequestDto requestDto = UpdateTweetRequestDto.builder()
                .content(validContent)
                .userId(authorUserId)
                .build();

            Tweet existingTweet = Tweet.builder()
                .id(tweetId)
                .userId(authorUserId)
                .content("Original content")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            when(tweetRepository.findById(tweetId)).thenReturn(Optional.of(existingTweet));

            assertThatCode(() -> tweetValidator.validateForUpdate(tweetId, requestDto))
                .doesNotThrowAnyException();

            verify(tweetRepository, times(1)).findById(tweetId);
            verify(validator, times(1)).validate(requestDto);
        }

        @Test
        void validateForUpdate_WhenTweetIdIsNull_ShouldThrowBusinessRuleValidationException() {
            UUID authorUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
            String validContent = "This is updated tweet content";
            UpdateTweetRequestDto requestDto = UpdateTweetRequestDto.builder()
                .content(validContent)
                .userId(authorUserId)
                .build();

            assertThatThrownBy(() -> tweetValidator.validateForUpdate(null, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_ID_NULL");
                });

            verify(tweetRepository, never()).findById(any());
            verify(validator, never()).validate(any());
        }

        @Test
        void validateForUpdate_WhenTweetNotFound_ShouldThrowBusinessRuleValidationException() {
            UUID tweetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID authorUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
            String validContent = "This is updated tweet content";
            UpdateTweetRequestDto requestDto = UpdateTweetRequestDto.builder()
                .content(validContent)
                .userId(authorUserId)
                .build();

            when(tweetRepository.findById(tweetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tweetValidator.validateForUpdate(tweetId, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_NOT_FOUND");
                    assertThat(ex.getContext()).isEqualTo(tweetId);
                });

            verify(tweetRepository, times(1)).findById(tweetId);
            verify(validator, never()).validate(any());
        }

        @Test
        void validateForUpdate_WhenUserIsNotAuthor_ShouldThrowBusinessRuleValidationException() {
            UUID tweetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID authorUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
            UUID differentUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
            String validContent = "This is updated tweet content";
            UpdateTweetRequestDto requestDto = UpdateTweetRequestDto.builder()
                .content(validContent)
                .userId(differentUserId)
                .build();

            Tweet existingTweet = Tweet.builder()
                .id(tweetId)
                .userId(authorUserId)
                .content("Original content")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            when(tweetRepository.findById(tweetId)).thenReturn(Optional.of(existingTweet));

            assertThatThrownBy(() -> tweetValidator.validateForUpdate(tweetId, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_ACCESS_DENIED");
                    assertThat(ex.getContext()).isEqualTo("Only the tweet author can update their tweet");
                });

            verify(tweetRepository, times(1)).findById(tweetId);
            verify(validator, never()).validate(any());
        }

        @Test
        void validateForUpdate_WhenContentIsEmpty_ShouldThrowFormatValidationException() {
            UUID tweetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID authorUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
            UpdateTweetRequestDto requestDto = UpdateTweetRequestDto.builder()
                .content("")
                .userId(authorUserId)
                .build();

            Tweet existingTweet = Tweet.builder()
                .id(tweetId)
                .userId(authorUserId)
                .content("Original content")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            when(tweetRepository.findById(tweetId)).thenReturn(Optional.of(existingTweet));

            assertThatThrownBy(() -> tweetValidator.validateForUpdate(tweetId, requestDto))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getConstraintName()).isEqualTo("CONTENT_VALIDATION");
                    assertThat(ex.getFieldName()).isEqualTo("content");
                });

            verify(tweetRepository, times(1)).findById(tweetId);
            verify(validator, times(1)).validate(requestDto);
        }

        @Test
        void validateForUpdate_WhenContentIsNull_ShouldThrowFormatValidationException() {
            UUID tweetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID authorUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
            UpdateTweetRequestDto requestDto = UpdateTweetRequestDto.builder()
                .content(null)
                .userId(authorUserId)
                .build();

            Tweet existingTweet = Tweet.builder()
                .id(tweetId)
                .userId(authorUserId)
                .content("Original content")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            when(tweetRepository.findById(tweetId)).thenReturn(Optional.of(existingTweet));

            assertThatThrownBy(() -> tweetValidator.validateForUpdate(tweetId, requestDto))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getConstraintName()).isEqualTo("CONTENT_VALIDATION");
                    assertThat(ex.getFieldName()).isEqualTo("content");
                });

            verify(tweetRepository, times(1)).findById(tweetId);
            verify(validator, times(1)).validate(requestDto);
        }

        @Test
        void validateForUpdate_WhenContentExceedsMaxLength_ShouldThrowFormatValidationException() {
            UUID tweetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID authorUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
            String content281Chars = "a".repeat(281);
            UpdateTweetRequestDto requestDto = UpdateTweetRequestDto.builder()
                .content(content281Chars)
                .userId(authorUserId)
                .build();

            Tweet existingTweet = Tweet.builder()
                .id(tweetId)
                .userId(authorUserId)
                .content("Original content")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            when(tweetRepository.findById(tweetId)).thenReturn(Optional.of(existingTweet));

            assertThatThrownBy(() -> tweetValidator.validateForUpdate(tweetId, requestDto))
                .isInstanceOf(FormatValidationException.class)
                .satisfies(exception -> {
                    FormatValidationException ex = (FormatValidationException) exception;
                    assertThat(ex.getConstraintName()).isEqualTo("CONTENT_VALIDATION");
                    assertThat(ex.getFieldName()).isEqualTo("content");
                });

            verify(tweetRepository, times(1)).findById(tweetId);
            verify(validator, times(1)).validate(requestDto);
        }

        @Test
        void validateForUpdate_WhenUserIdIsNull_ShouldThrowBusinessRuleValidationException() {
            UUID tweetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID authorUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
            String validContent = "This is updated tweet content";
            UpdateTweetRequestDto requestDto = UpdateTweetRequestDto.builder()
                .content(validContent)
                .userId(null)
                .build();

            Tweet existingTweet = Tweet.builder()
                .id(tweetId)
                .userId(authorUserId)
                .content("Original content")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            when(tweetRepository.findById(tweetId)).thenReturn(Optional.of(existingTweet));

            assertThatThrownBy(() -> tweetValidator.validateForUpdate(tweetId, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_ACCESS_DENIED");
                    assertThat(ex.getContext()).isEqualTo("Only the tweet author can update their tweet");
                });

            verify(tweetRepository, times(1)).findById(tweetId);
            verify(validator, never()).validate(any());
        }
    }

    @Nested
    class ValidateForDeleteTests {

        @Test
        void validateForDelete_WhenValidData_ShouldCompleteWithoutExceptions() {
            UUID tweetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID authorUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
            DeleteTweetRequestDto requestDto = DeleteTweetRequestDto.builder()
                .userId(authorUserId)
                .build();

            Tweet existingTweet = Tweet.builder()
                .id(tweetId)
                .userId(authorUserId)
                .content("Tweet to be deleted")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

            when(tweetRepository.findById(tweetId)).thenReturn(Optional.of(existingTweet));

            assertThatCode(() -> tweetValidator.validateForDelete(tweetId, requestDto))
                .doesNotThrowAnyException();

            verify(tweetRepository, times(1)).findById(tweetId);
        }

        @Test
        void validateForDelete_WhenTweetIdIsNull_ShouldThrowBusinessRuleValidationException() {
            UUID authorUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
            DeleteTweetRequestDto requestDto = DeleteTweetRequestDto.builder()
                .userId(authorUserId)
                .build();

            assertThatThrownBy(() -> tweetValidator.validateForDelete(null, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_ID_NULL");
                    assertThat(ex.getContext()).isEqualTo("Tweet ID cannot be null");
                });

            verify(tweetRepository, never()).findById(any());
        }

        @Test
        void validateForDelete_WhenTweetNotFound_ShouldThrowBusinessRuleValidationException() {
            UUID tweetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID authorUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
            DeleteTweetRequestDto requestDto = DeleteTweetRequestDto.builder()
                .userId(authorUserId)
                .build();

            when(tweetRepository.findById(tweetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tweetValidator.validateForDelete(tweetId, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_NOT_FOUND");
                    assertThat(ex.getContext()).isEqualTo(tweetId);
                });

            verify(tweetRepository, times(1)).findById(tweetId);
        }

        @Test
        void validateForDelete_WhenTweetAlreadyDeleted_ShouldThrowBusinessRuleValidationException() {
            UUID tweetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID authorUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
            DeleteTweetRequestDto requestDto = DeleteTweetRequestDto.builder()
                .userId(authorUserId)
                .build();

            Tweet deletedTweet = Tweet.builder()
                .id(tweetId)
                .userId(authorUserId)
                .content("Already deleted tweet")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(true)
                .deletedAt(LocalDateTime.now())
                .build();

            when(tweetRepository.findById(tweetId)).thenReturn(Optional.of(deletedTweet));

            assertThatThrownBy(() -> tweetValidator.validateForDelete(tweetId, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_ALREADY_DELETED");
                    assertThat(ex.getContext()).isEqualTo(tweetId);
                });

            verify(tweetRepository, times(1)).findById(tweetId);
        }

        @Test
        void validateForDelete_WhenUserIsNotAuthor_ShouldThrowBusinessRuleValidationException() {
            UUID tweetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UUID authorUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
            UUID differentUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
            DeleteTweetRequestDto requestDto = DeleteTweetRequestDto.builder()
                .userId(differentUserId)
                .build();

            Tweet existingTweet = Tweet.builder()
                .id(tweetId)
                .userId(authorUserId)
                .content("Tweet to be deleted")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

            when(tweetRepository.findById(tweetId)).thenReturn(Optional.of(existingTweet));

            assertThatThrownBy(() -> tweetValidator.validateForDelete(tweetId, requestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_ACCESS_DENIED");
                    assertThat(ex.getContext()).isEqualTo("Only the tweet author can update their tweet");
                });

            verify(tweetRepository, times(1)).findById(tweetId);
        }
    }

    @Nested
    class ValidateForTimelineTests {

        @Test
        void validateForTimeline_WhenValidUserIdAndUserExists_ShouldCompleteWithoutExceptions() {
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            when(userGateway.existsUser(validUserId)).thenReturn(true);

            assertThatCode(() -> tweetValidator.validateForTimeline(validUserId))
                .doesNotThrowAnyException();

            verify(userGateway, times(1)).existsUser(validUserId);
        }

        @Test
        void validateForTimeline_WhenUserIdIsNull_ShouldThrowBusinessRuleValidationException() {
            assertThatThrownBy(() -> tweetValidator.validateForTimeline(null))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("USER_ID_NULL");
                    assertThat(ex.getContext()).isEqualTo("User ID cannot be null");
                });

            verify(userGateway, never()).existsUser(any());
        }

        @Test
        void validateForTimeline_WhenUserDoesNotExist_ShouldThrowBusinessRuleValidationException() {
            UUID nonExistentUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            when(userGateway.existsUser(nonExistentUserId)).thenReturn(false);

            assertThatThrownBy(() -> tweetValidator.validateForTimeline(nonExistentUserId))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("USER_NOT_EXISTS");
                    assertThat(ex.getContext()).isEqualTo(nonExistentUserId);
                });

            verify(userGateway, times(1)).existsUser(nonExistentUserId);
        }
    }
}
