package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.dto.request.GenerateUsersAndTweetsRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GenerateUsersAndTweetsValidatorImplTest {

    @InjectMocks
    private GenerateUsersAndTweetsValidatorImpl validator;

    @Nested
    class ValidateDeletionCountTests {

        private GenerateUsersAndTweetsRequestDto requestDto;

        @BeforeEach
        void setUp() {
            requestDto = GenerateUsersAndTweetsRequestDto.builder()
                .nUsers(10)
                .nTweetsPerUser(5)
                .lUsersForDeletion(3)
                .build();
        }

        @Test
        void validateDeletionCount_WhenLUsersForDeletionIsZero_ShouldCompleteWithoutExceptions() {
            GenerateUsersAndTweetsRequestDto requestWithZeroDeletions = GenerateUsersAndTweetsRequestDto.builder()
                .nUsers(10)
                .nTweetsPerUser(5)
                .lUsersForDeletion(0)
                .build();

            assertThatCode(() -> validator.validateDeletionCount(requestWithZeroDeletions, 10))
                .doesNotThrowAnyException();
        }

        @Test
        void validateDeletionCount_WhenLUsersForDeletionEqualsUsersWithTweets_ShouldCompleteWithoutExceptions() {
            assertThatCode(() -> validator.validateDeletionCount(requestDto, 3))
                .doesNotThrowAnyException();
        }

        @Test
        void validateDeletionCount_WhenLUsersForDeletionLessThanUsersWithTweets_ShouldCompleteWithoutExceptions() {
            assertThatCode(() -> validator.validateDeletionCount(requestDto, 10))
                .doesNotThrowAnyException();
        }

        @Test
        void validateDeletionCount_WhenLUsersForDeletionGreaterThanUsersWithTweets_ShouldThrowBusinessRuleValidationException() {
            assertThatThrownBy(() -> validator.validateDeletionCount(requestDto, 2))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("DELETION_COUNT_EXCEEDS_USERS_WITH_TWEETS");
                    assertThat(ex.getMessage()).contains("Cannot delete tweets from 3 users: only 2 users have tweets");
                });
        }

        @Test
        void validateDeletionCount_WhenRequestDtoIsNull_ShouldThrowIllegalArgumentException() {
            assertThatThrownBy(() -> validator.validateDeletionCount(null, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Request DTO cannot be null");
        }

        @Test
        void validateDeletionCount_WhenUsersWithTweetsIsZeroAndLUsersForDeletionIsZero_ShouldCompleteWithoutExceptions() {
            GenerateUsersAndTweetsRequestDto requestWithZeroDeletions = GenerateUsersAndTweetsRequestDto.builder()
                .nUsers(10)
                .nTweetsPerUser(5)
                .lUsersForDeletion(0)
                .build();

            assertThatCode(() -> validator.validateDeletionCount(requestWithZeroDeletions, 0))
                .doesNotThrowAnyException();
        }

        @Test
        void validateDeletionCount_WhenUsersWithTweetsIsZeroAndLUsersForDeletionIsGreaterThanZero_ShouldThrowBusinessRuleValidationException() {
            assertThatThrownBy(() -> validator.validateDeletionCount(requestDto, 0))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("DELETION_COUNT_EXCEEDS_USERS_WITH_TWEETS");
                    assertThat(ex.getMessage()).contains("Cannot delete tweets from 3 users: only 0 users have tweets");
                });
        }

        @Test
        void validateDeletionCount_WhenLUsersForDeletionIsOneAndUsersWithTweetsIsOne_ShouldCompleteWithoutExceptions() {
            GenerateUsersAndTweetsRequestDto requestWithOneDeletion = GenerateUsersAndTweetsRequestDto.builder()
                .nUsers(10)
                .nTweetsPerUser(5)
                .lUsersForDeletion(1)
                .build();

            assertThatCode(() -> validator.validateDeletionCount(requestWithOneDeletion, 1))
                .doesNotThrowAnyException();
        }

        @Test
        void validateDeletionCount_WhenLUsersForDeletionIsLarge_ShouldThrowBusinessRuleValidationException() {
            GenerateUsersAndTweetsRequestDto requestWithLargeDeletion = GenerateUsersAndTweetsRequestDto.builder()
                .nUsers(100)
                .nTweetsPerUser(10)
                .lUsersForDeletion(50)
                .build();

            assertThatThrownBy(() -> validator.validateDeletionCount(requestWithLargeDeletion, 10))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("DELETION_COUNT_EXCEEDS_USERS_WITH_TWEETS");
                    assertThat(ex.getMessage()).contains("Cannot delete tweets from 50 users: only 10 users have tweets");
                });
        }
    }
}
