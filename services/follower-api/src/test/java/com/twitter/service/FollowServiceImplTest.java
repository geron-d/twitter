package com.twitter.service;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.request.FollowRequestDto;
import com.twitter.dto.response.FollowResponseDto;
import com.twitter.entity.Follow;
import com.twitter.mapper.FollowMapper;
import com.twitter.repository.FollowRepository;
import com.twitter.validation.FollowValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private FollowMapper followMapper;

    @Mock
    private FollowValidator followValidator;

    @InjectMocks
    private FollowServiceImpl followService;

    @Nested
    class FollowTests {

        private FollowRequestDto validRequestDto;
        private Follow mappedFollow;
        private Follow savedFollow;
        private FollowResponseDto responseDto;
        private UUID testFollowerId;
        private UUID testFollowingId;

        @BeforeEach
        void setUp() {
            testFollowerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            testFollowingId = UUID.fromString("987fcdeb-51a2-43d7-b123-426614174999");

            validRequestDto = FollowRequestDto.builder()
                .followerId(testFollowerId)
                .followingId(testFollowingId)
                .build();

            mappedFollow = Follow.builder()
                .followerId(testFollowerId)
                .followingId(testFollowingId)
                .build();

            UUID followId = UUID.fromString("456e7890-e89b-12d3-a456-426614174111");
            savedFollow = Follow.builder()
                .id(followId)
                .followerId(testFollowerId)
                .followingId(testFollowingId)
                .createdAt(LocalDateTime.of(2025, 1, 27, 10, 30, 0))
                .build();

            responseDto = FollowResponseDto.builder()
                .id(followId)
                .followerId(testFollowerId)
                .followingId(testFollowingId)
                .createdAt(LocalDateTime.of(2025, 1, 27, 10, 30, 0))
                .build();
        }

        @Test
        void follow_WithValidData_ShouldReturnFollowResponseDto() {
            doNothing().when(followValidator).validateForFollow(validRequestDto);
            when(followMapper.toFollow(validRequestDto)).thenReturn(mappedFollow);
            when(followRepository.saveAndFlush(mappedFollow)).thenReturn(savedFollow);
            when(followMapper.toFollowResponseDto(savedFollow)).thenReturn(responseDto);

            FollowResponseDto result = followService.follow(validRequestDto);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(savedFollow.getId());
            assertThat(result.followerId()).isEqualTo(testFollowerId);
            assertThat(result.followingId()).isEqualTo(testFollowingId);
            assertThat(result.createdAt()).isEqualTo(savedFollow.getCreatedAt());
        }

        @Test
        void follow_WithValidData_ShouldCallEachDependencyExactlyOnce() {
            doNothing().when(followValidator).validateForFollow(validRequestDto);
            when(followMapper.toFollow(validRequestDto)).thenReturn(mappedFollow);
            when(followRepository.saveAndFlush(mappedFollow)).thenReturn(savedFollow);
            when(followMapper.toFollowResponseDto(savedFollow)).thenReturn(responseDto);

            followService.follow(validRequestDto);

            verify(followValidator, times(1)).validateForFollow(eq(validRequestDto));
            verify(followMapper, times(1)).toFollow(eq(validRequestDto));
            verify(followRepository, times(1)).saveAndFlush(eq(mappedFollow));
            verify(followMapper, times(1)).toFollowResponseDto(eq(savedFollow));
        }

        @Test
        void follow_WhenSelfFollow_ShouldThrowBusinessRuleValidationException() {
            BusinessRuleValidationException validationException = new BusinessRuleValidationException(
                "SELF_FOLLOW_NOT_ALLOWED",
                String.format("User cannot follow themselves (userId=%s)", testFollowerId)
            );
            doThrow(validationException)
                .when(followValidator).validateForFollow(validRequestDto);

            assertThatThrownBy(() -> followService.follow(validRequestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .isEqualTo(validationException);

            verify(followValidator, times(1)).validateForFollow(eq(validRequestDto));
            verify(followMapper, never()).toFollow(any());
            verify(followRepository, never()).saveAndFlush(any());
            verify(followMapper, never()).toFollowResponseDto(any());
        }

        @Test
        void follow_WhenFollowerNotFound_ShouldThrowBusinessRuleValidationException() {
            BusinessRuleValidationException businessException = new BusinessRuleValidationException(
                "FOLLOWER_NOT_EXISTS",
                testFollowerId
            );
            doThrow(businessException)
                .when(followValidator).validateForFollow(validRequestDto);

            assertThatThrownBy(() -> followService.follow(validRequestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .isEqualTo(businessException);

            verify(followValidator, times(1)).validateForFollow(eq(validRequestDto));
            verify(followMapper, never()).toFollow(any());
            verify(followRepository, never()).saveAndFlush(any());
            verify(followMapper, never()).toFollowResponseDto(any());
        }

        @Test
        void follow_WhenFollowingNotFound_ShouldThrowBusinessRuleValidationException() {
            BusinessRuleValidationException businessException = new BusinessRuleValidationException(
                "FOLLOWING_NOT_EXISTS",
                testFollowingId
            );
            doThrow(businessException)
                .when(followValidator).validateForFollow(validRequestDto);

            assertThatThrownBy(() -> followService.follow(validRequestDto))
                .isInstanceOf(BusinessRuleValidationException.class)
                .isEqualTo(businessException);

            verify(followValidator, times(1)).validateForFollow(eq(validRequestDto));
            verify(followMapper, never()).toFollow(any());
            verify(followRepository, never()).saveAndFlush(any());
            verify(followMapper, never()).toFollowResponseDto(any());
        }

        @Test
        void follow_WhenFollowAlreadyExists_ShouldThrowUniquenessValidationException() {
            UniquenessValidationException uniquenessException = new UniquenessValidationException(
                "Follow relationship already exists"
            );
            doThrow(uniquenessException)
                .when(followValidator).validateForFollow(validRequestDto);

            assertThatThrownBy(() -> followService.follow(validRequestDto))
                .isInstanceOf(UniquenessValidationException.class)
                .isEqualTo(uniquenessException);

            verify(followValidator, times(1)).validateForFollow(eq(validRequestDto));
            verify(followMapper, never()).toFollow(any());
            verify(followRepository, never()).saveAndFlush(any());
            verify(followMapper, never()).toFollowResponseDto(any());
        }
    }
}

