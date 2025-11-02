package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.gateway.UserGateway;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TweetValidatorImplTest {

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private UserGateway userGateway;

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
            // Arrange
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            String validContent = "This is a valid tweet content";
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content(validContent)
                .userId(validUserId)
                .build();

            // Act & Assert
            assertThatCode(() -> tweetValidator.validateContent(requestDto))
                .doesNotThrowAnyException();

            verify(validator, times(1)).validate(requestDto);
        }

        @Test
        void validateContent_WhenContentIsNull_ShouldThrowFormatValidationException() {
            // Arrange
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content(null)
                .userId(validUserId)
                .build();

            // Act & Assert
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
            // Arrange
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content("")
                .userId(validUserId)
                .build();

            // Act & Assert
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
            // Arrange
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            String content281Chars = "a".repeat(281);
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content(content281Chars)
                .userId(validUserId)
                .build();

            // Act & Assert
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
            // Arrange
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content(null)
                .userId(null)
                .build();

            // Act & Assert
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
            // Act & Assert
            assertThatThrownBy(() -> tweetValidator.validateContent(null))
                .isInstanceOfAny(
                    FormatValidationException.class,
                    NullPointerException.class,
                    IllegalArgumentException.class
                );
        }

        @Test
        void validateContent_WhenValidatorReturnsViolations_ShouldFormatErrorMessageCorrectly() {
            // Arrange
            UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
                .content("")
                .userId(validUserId)
                .build();

            // Act & Assert
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
}

