package com.twitter.validation;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.request.FollowRequestDto;
import com.twitter.gateway.UserGateway;
import com.twitter.repository.FollowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FollowValidatorImpl}.
 *
 * @author geron
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class FollowValidatorImplTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserGateway userGateway;

    @InjectMocks
    private FollowValidatorImpl followValidator;

    private UUID testFollowerId;
    private UUID testFollowingId;
    private FollowRequestDto validRequestDto;

    @BeforeEach
    void setUp() {
        testFollowerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        testFollowingId = UUID.fromString("987fcdeb-51a2-43d7-b123-426614174999");

        validRequestDto = FollowRequestDto.builder()
            .followerId(testFollowerId)
            .followingId(testFollowingId)
            .build();
    }

    @Nested
    class ValidateForFollowTests {

        @Test
        void validateForFollow_WhenValidData_ShouldCompleteWithoutExceptions() {
            when(userGateway.existsUser(testFollowerId)).thenReturn(true);
            when(userGateway.existsUser(testFollowingId)).thenReturn(true);
            when(followRepository.existsByFollowerIdAndFollowingId(testFollowerId, testFollowingId))
                .thenReturn(false);

            assertThatCode(() -> followValidator.validateForFollow(validRequestDto))
                .doesNotThrowAnyException();

            verify(userGateway, times(1)).existsUser(testFollowerId);
            verify(userGateway, times(1)).existsUser(testFollowingId);
            verify(followRepository, times(1))
                .existsByFollowerIdAndFollowingId(testFollowerId, testFollowingId);
        }

        @Test
        void validateForFollow_WhenRequestIsNull_ShouldThrowBusinessRuleValidationException() {
            assertThatThrownBy(() -> followValidator.validateForFollow(null))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("FOLLOW_REQUEST_NULL");
                    assertThat(ex.getContext()).isEqualTo("Follow request cannot be null");
                });

            verify(userGateway, never()).existsUser(any());
            verify(followRepository, never()).existsByFollowerIdAndFollowingId(any(), any());
        }

        @Test
        void validateForFollow_WhenFollowerIdIsNull_ShouldThrowBusinessRuleValidationException() {
            FollowRequestDto requestWithNullFollowerId = FollowRequestDto.builder()
                .followerId(null)
                .followingId(testFollowingId)
                .build();

            assertThatThrownBy(() -> followValidator.validateForFollow(requestWithNullFollowerId))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("FOLLOWER_ID_NULL");
                    assertThat(ex.getContext()).isEqualTo("Follower ID cannot be null");
                });

            verify(userGateway, never()).existsUser(any());
            verify(followRepository, never()).existsByFollowerIdAndFollowingId(any(), any());
        }

        @Test
        void validateForFollow_WhenFollowingIdIsNull_ShouldThrowBusinessRuleValidationException() {
            FollowRequestDto requestWithNullFollowingId = FollowRequestDto.builder()
                .followerId(testFollowerId)
                .followingId(null)
                .build();

            assertThatThrownBy(() -> followValidator.validateForFollow(requestWithNullFollowingId))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("FOLLOWING_ID_NULL");
                    assertThat(ex.getContext()).isEqualTo("Following ID cannot be null");
                });

            verify(userGateway, never()).existsUser(any());
            verify(followRepository, never()).existsByFollowerIdAndFollowingId(any(), any());
        }

        @Test
        void validateForFollow_WhenSelfFollow_ShouldThrowBusinessRuleValidationException() {
            FollowRequestDto selfFollowRequest = FollowRequestDto.builder()
                .followerId(testFollowerId)
                .followingId(testFollowerId)
                .build();

            assertThatThrownBy(() -> followValidator.validateForFollow(selfFollowRequest))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("SELF_FOLLOW_NOT_ALLOWED");
                    String context = ex.getContext() != null ? ex.getContext().toString() : "";
                    assertThat(context).contains("User cannot follow themselves");
                    assertThat(context).contains(testFollowerId.toString());
                });

            verify(userGateway, never()).existsUser(any());
            verify(followRepository, never()).existsByFollowerIdAndFollowingId(any(), any());
        }

        @Test
        void validateForFollow_WhenFollowerDoesNotExist_ShouldThrowBusinessRuleValidationException() {
            when(userGateway.existsUser(testFollowerId)).thenReturn(false);

            assertThatThrownBy(() -> followValidator.validateForFollow(validRequestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("FOLLOWER_NOT_EXISTS");
                    assertThat(ex.getContext()).isEqualTo(testFollowerId);
                });

            verify(userGateway, times(1)).existsUser(testFollowerId);
            verify(userGateway, never()).existsUser(testFollowingId);
            verify(followRepository, never()).existsByFollowerIdAndFollowingId(any(), any());
        }

        @Test
        void validateForFollow_WhenFollowingDoesNotExist_ShouldThrowBusinessRuleValidationException() {
            when(userGateway.existsUser(testFollowerId)).thenReturn(true);
            when(userGateway.existsUser(testFollowingId)).thenReturn(false);

            assertThatThrownBy(() -> followValidator.validateForFollow(validRequestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    BusinessRuleValidationException ex = (BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("FOLLOWING_NOT_EXISTS");
                    assertThat(ex.getContext()).isEqualTo(testFollowingId);
                });

            verify(userGateway, times(1)).existsUser(testFollowerId);
            verify(userGateway, times(1)).existsUser(testFollowingId);
            verify(followRepository, never()).existsByFollowerIdAndFollowingId(any(), any());
        }

        @Test
        void validateForFollow_WhenFollowAlreadyExists_ShouldThrowUniquenessValidationException() {
            when(userGateway.existsUser(testFollowerId)).thenReturn(true);
            when(userGateway.existsUser(testFollowingId)).thenReturn(true);
            when(followRepository.existsByFollowerIdAndFollowingId(testFollowerId, testFollowingId))
                .thenReturn(true);

            assertThatThrownBy(() -> followValidator.validateForFollow(validRequestDto))
                .isInstanceOf(UniquenessValidationException.class)
                .satisfies(exception -> {
                    UniquenessValidationException ex = (UniquenessValidationException) exception;
                    assertThat(ex.getMessage()).contains("Follow relationship already exists");
                });

            verify(userGateway, times(1)).existsUser(testFollowerId);
            verify(userGateway, times(1)).existsUser(testFollowingId);
            verify(followRepository, times(1))
                .existsByFollowerIdAndFollowingId(testFollowerId, testFollowingId);
        }
    }
}

