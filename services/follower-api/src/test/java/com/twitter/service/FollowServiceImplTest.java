package com.twitter.service;

import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.filter.FollowerFilter;
import com.twitter.dto.filter.FollowingFilter;
import com.twitter.common.dto.request.follow.FollowRequestDto;
import com.twitter.common.dto.response.follow.FollowResponseDto;
import com.twitter.dto.response.FollowStatsResponseDto;
import com.twitter.dto.response.FollowStatusResponseDto;
import com.twitter.dto.response.FollowerResponseDto;
import com.twitter.common.dto.response.follow.FollowingResponseDto;
import com.twitter.entity.Follow;
import com.twitter.gateway.UserGateway;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    @Mock
    private UserGateway userGateway;

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

    @Nested
    class UnfollowTests {

        private UUID testFollowerId;
        private UUID testFollowingId;
        private Follow existingFollow;

        @BeforeEach
        void setUp() {
            testFollowerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            testFollowingId = UUID.fromString("987fcdeb-51a2-43d7-b123-426614174999");

            UUID followId = UUID.fromString("456e7890-e89b-12d3-a456-426614174111");
            existingFollow = Follow.builder()
                .id(followId)
                .followerId(testFollowerId)
                .followingId(testFollowingId)
                .createdAt(LocalDateTime.of(2025, 1, 27, 10, 30, 0))
                .build();
        }

        @Test
        void unfollow_WithValidData_ShouldDeleteFollow() {
            when(followRepository.findByFollowerIdAndFollowingId(testFollowerId, testFollowingId))
                .thenReturn(Optional.of(existingFollow));
            doNothing().when(followRepository).delete(existingFollow);

            followService.unfollow(testFollowerId, testFollowingId);

            verify(followRepository, times(1))
                .findByFollowerIdAndFollowingId(eq(testFollowerId), eq(testFollowingId));
            verify(followRepository, times(1)).delete(eq(existingFollow));
        }

        @Test
        void unfollow_WhenFollowNotFound_ShouldThrowResponseStatusException() {
            when(followRepository.findByFollowerIdAndFollowingId(testFollowerId, testFollowingId))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> followService.unfollow(testFollowerId, testFollowingId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex = (ResponseStatusException) exception;
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getReason()).contains("Follow relationship between");
                    assertThat(ex.getReason()).contains(testFollowerId.toString());
                    assertThat(ex.getReason()).contains(testFollowingId.toString());
                });

            verify(followRepository, times(1))
                .findByFollowerIdAndFollowingId(eq(testFollowerId), eq(testFollowingId));
            verify(followRepository, never()).delete(any());
        }
    }

    @Nested
    class GetFollowersTests {

        private UUID testUserId;
        private UUID testFollowerId1;
        private UUID testFollowerId2;
        private Follow follow1;
        private Follow follow2;
        private FollowerResponseDto followerResponseDto1;
        private FollowerResponseDto followerResponseDto2;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
            testUserId = UUID.fromString("111e4567-e89b-12d3-a456-426614174000");
            testFollowerId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            testFollowerId2 = UUID.fromString("222e4567-e89b-12d3-a456-426614174000");

            UUID followId1 = UUID.fromString("456e7890-e89b-12d3-a456-426614174111");
            follow1 = Follow.builder()
                .id(followId1)
                .followerId(testFollowerId1)
                .followingId(testUserId)
                .createdAt(LocalDateTime.of(2025, 1, 20, 15, 30, 0))
                .build();

            UUID followId2 = UUID.fromString("789e0123-e89b-12d3-a456-426614174222");
            follow2 = Follow.builder()
                .id(followId2)
                .followerId(testFollowerId2)
                .followingId(testUserId)
                .createdAt(LocalDateTime.of(2025, 1, 25, 12, 0, 0))
                .build();

            followerResponseDto1 = FollowerResponseDto.builder()
                .id(testFollowerId1)
                .login("john_doe")
                .createdAt(LocalDateTime.of(2025, 1, 20, 15, 30, 0))
                .build();

            followerResponseDto2 = FollowerResponseDto.builder()
                .id(testFollowerId2)
                .login("jane_smith")
                .createdAt(LocalDateTime.of(2025, 1, 25, 12, 0, 0))
                .build();

            pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        @Test
        void getFollowers_WithValidData_ShouldReturnPagedModelWithFollowers() {
            List<Follow> follows = List.of(follow1, follow2);
            Page<Follow> followsPage = new PageImpl<>(follows, pageable, 2);

            when(followRepository.findByFollowingId(eq(testUserId), any(Pageable.class)))
                .thenReturn(followsPage);
            when(userGateway.getUserLogin(testFollowerId1)).thenReturn(Optional.of("john_doe"));
            when(userGateway.getUserLogin(testFollowerId2)).thenReturn(Optional.of("jane_smith"));
            when(followMapper.toFollowerResponseDto(follow1, "john_doe"))
                .thenReturn(followerResponseDto1);
            when(followMapper.toFollowerResponseDto(follow2, "jane_smith"))
                .thenReturn(followerResponseDto2);

            PagedModel<FollowerResponseDto> result = followService.getFollowers(
                testUserId, null, pageable);
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(followerResponseDto1, followerResponseDto2);
            assertThat(result.getMetadata().totalElements()).isEqualTo(2);
            assertThat(result.getMetadata().number()).isEqualTo(0);
            assertThat(result.getMetadata().size()).isEqualTo(10);
        }

        @Test
        void getFollowers_WithValidData_ShouldCallEachDependencyExactlyOnce() {
            List<Follow> follows = List.of(follow1);
            Page<Follow> followsPage = new PageImpl<>(follows, pageable, 1);

            when(followRepository.findByFollowingId(eq(testUserId), any(Pageable.class)))
                .thenReturn(followsPage);
            when(userGateway.getUserLogin(testFollowerId1)).thenReturn(Optional.of("john_doe"));
            when(followMapper.toFollowerResponseDto(follow1, "john_doe"))
                .thenReturn(followerResponseDto1);

            followService.getFollowers(testUserId, null, pageable);

            verify(followRepository, times(1))
                .findByFollowingId(eq(testUserId), any(Pageable.class));
            verify(userGateway, times(1)).getUserLogin(eq(testFollowerId1));
            verify(followMapper, times(1))
                .toFollowerResponseDto(eq(follow1), eq("john_doe"));
        }

        @Test
        void getFollowers_WhenNoFollowersExist_ShouldReturnEmptyPagedModel() {
            Page<Follow> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(followRepository.findByFollowingId(eq(testUserId), any(Pageable.class)))
                .thenReturn(emptyPage);

            PagedModel<FollowerResponseDto> result = followService.getFollowers(
                testUserId, null, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getMetadata().totalElements()).isEqualTo(0);

            verify(followRepository, times(1))
                .findByFollowingId(eq(testUserId), any(Pageable.class));
            verify(userGateway, never()).getUserLogin(any());
            verify(followMapper, never()).toFollowerResponseDto(any(), any());
        }

        @Test
        void getFollowers_WithLoginFilter_ShouldFilterByLogin() {
            List<Follow> follows = List.of(follow1, follow2);
            Page<Follow> followsPage = new PageImpl<>(follows, pageable, 2);
            FollowerFilter filter = new FollowerFilter("john");

            when(followRepository.findByFollowingId(eq(testUserId), any(Pageable.class)))
                .thenReturn(followsPage);
            when(userGateway.getUserLogin(testFollowerId1)).thenReturn(Optional.of("john_doe"));
            when(userGateway.getUserLogin(testFollowerId2)).thenReturn(Optional.of("jane_smith"));
            when(followMapper.toFollowerResponseDto(follow1, "john_doe"))
                .thenReturn(followerResponseDto1);
            when(followMapper.toFollowerResponseDto(follow2, "jane_smith"))
                .thenReturn(followerResponseDto2);

            PagedModel<FollowerResponseDto> result = followService.getFollowers(
                testUserId, filter, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).containsExactly(followerResponseDto1);
            assertThat(result.getContent().get(0).login()).isEqualTo("john_doe");
        }

        @Test
        void getFollowers_WithLoginFilter_ShouldFilterCaseInsensitively() {
            List<Follow> follows = List.of(follow1, follow2);
            Page<Follow> followsPage = new PageImpl<>(follows, pageable, 2);
            FollowerFilter filter = new FollowerFilter("JOHN");

            when(followRepository.findByFollowingId(eq(testUserId), any(Pageable.class)))
                .thenReturn(followsPage);
            when(userGateway.getUserLogin(testFollowerId1)).thenReturn(Optional.of("john_doe"));
            when(userGateway.getUserLogin(testFollowerId2)).thenReturn(Optional.of("jane_smith"));
            when(followMapper.toFollowerResponseDto(follow1, "john_doe"))
                .thenReturn(followerResponseDto1);
            when(followMapper.toFollowerResponseDto(follow2, "jane_smith"))
                .thenReturn(followerResponseDto2);

            PagedModel<FollowerResponseDto> result = followService.getFollowers(
                testUserId, filter, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).containsExactly(followerResponseDto1);
        }

        @Test
        void getFollowers_WhenUserLoginNotFound_ShouldUseUnknownLogin() {
            List<Follow> follows = List.of(follow1);
            Page<Follow> followsPage = new PageImpl<>(follows, pageable, 1);
            FollowerResponseDto followerWithUnknownLogin = FollowerResponseDto.builder()
                .id(testFollowerId1)
                .login("unknown")
                .createdAt(LocalDateTime.of(2025, 1, 20, 15, 30, 0))
                .build();

            when(followRepository.findByFollowingId(eq(testUserId), any(Pageable.class)))
                .thenReturn(followsPage);
            when(userGateway.getUserLogin(testFollowerId1)).thenReturn(Optional.empty());
            when(followMapper.toFollowerResponseDto(follow1, "unknown"))
                .thenReturn(followerWithUnknownLogin);

            PagedModel<FollowerResponseDto> result = followService.getFollowers(
                testUserId, null, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).login()).isEqualTo("unknown");

            verify(userGateway, times(1)).getUserLogin(eq(testFollowerId1));
            verify(followMapper, times(1))
                .toFollowerResponseDto(eq(follow1), eq("unknown"));
        }

        @Test
        void getFollowers_WhenPageableNotSorted_ShouldAddDefaultSorting() {
            Pageable unsortedPageable = PageRequest.of(0, 10);
            List<Follow> follows = List.of(follow1);
            Page<Follow> followsPage = new PageImpl<>(follows, unsortedPageable, 1);

            when(followRepository.findByFollowingId(eq(testUserId), any(Pageable.class)))
                .thenReturn(followsPage);
            when(userGateway.getUserLogin(testFollowerId1)).thenReturn(Optional.of("john_doe"));
            when(followMapper.toFollowerResponseDto(follow1, "john_doe"))
                .thenReturn(followerResponseDto1);

            followService.getFollowers(testUserId, null, unsortedPageable);

            verify(followRepository, times(1)).findByFollowingId(
                eq(testUserId),
                argThat(pageable -> pageable.getSort().isSorted() &&
                    pageable.getSort().getOrderFor("createdAt") != null &&
                    pageable.getSort().getOrderFor("createdAt").getDirection() == Sort.Direction.DESC)
            );
        }
    }

    @Nested
    class GetFollowingTests {

        private UUID testUserId;
        private UUID testFollowingId1;
        private UUID testFollowingId2;
        private Follow follow1;
        private Follow follow2;
        private FollowingResponseDto followingResponseDto1;
        private FollowingResponseDto followingResponseDto2;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
            testUserId = UUID.fromString("111e4567-e89b-12d3-a456-426614174000");
            testFollowingId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            testFollowingId2 = UUID.fromString("222e4567-e89b-12d3-a456-426614174000");

            UUID followId1 = UUID.fromString("456e7890-e89b-12d3-a456-426614174111");
            follow1 = Follow.builder()
                .id(followId1)
                .followerId(testUserId)
                .followingId(testFollowingId1)
                .createdAt(LocalDateTime.of(2025, 1, 20, 15, 30, 0))
                .build();

            UUID followId2 = UUID.fromString("789e0123-e89b-12d3-a456-426614174222");
            follow2 = Follow.builder()
                .id(followId2)
                .followerId(testUserId)
                .followingId(testFollowingId2)
                .createdAt(LocalDateTime.of(2025, 1, 25, 12, 0, 0))
                .build();

            followingResponseDto1 = FollowingResponseDto.builder()
                .id(testFollowingId1)
                .login("jane_doe")
                .createdAt(LocalDateTime.of(2025, 1, 20, 15, 30, 0))
                .build();

            followingResponseDto2 = FollowingResponseDto.builder()
                .id(testFollowingId2)
                .login("john_smith")
                .createdAt(LocalDateTime.of(2025, 1, 25, 12, 0, 0))
                .build();

            pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        @Test
        void getFollowing_WithValidData_ShouldReturnPagedModelWithFollowing() {
            List<Follow> follows = List.of(follow1, follow2);
            Page<Follow> followsPage = new PageImpl<>(follows, pageable, 2);

            when(followRepository.findByFollowerId(eq(testUserId), any(Pageable.class)))
                .thenReturn(followsPage);
            when(userGateway.getUserLogin(testFollowingId1)).thenReturn(Optional.of("jane_doe"));
            when(userGateway.getUserLogin(testFollowingId2)).thenReturn(Optional.of("john_smith"));
            when(followMapper.toFollowingResponseDto(follow1, "jane_doe"))
                .thenReturn(followingResponseDto1);
            when(followMapper.toFollowingResponseDto(follow2, "john_smith"))
                .thenReturn(followingResponseDto2);

            PagedModel<FollowingResponseDto> result = followService.getFollowing(
                testUserId, null, pageable);
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(followingResponseDto1, followingResponseDto2);
            assertThat(result.getMetadata().totalElements()).isEqualTo(2);
            assertThat(result.getMetadata().number()).isEqualTo(0);
            assertThat(result.getMetadata().size()).isEqualTo(10);
        }

        @Test
        void getFollowing_WithValidData_ShouldCallEachDependencyExactlyOnce() {
            List<Follow> follows = List.of(follow1);
            Page<Follow> followsPage = new PageImpl<>(follows, pageable, 1);

            when(followRepository.findByFollowerId(eq(testUserId), any(Pageable.class)))
                .thenReturn(followsPage);
            when(userGateway.getUserLogin(testFollowingId1)).thenReturn(Optional.of("jane_doe"));
            when(followMapper.toFollowingResponseDto(follow1, "jane_doe"))
                .thenReturn(followingResponseDto1);

            followService.getFollowing(testUserId, null, pageable);

            verify(followRepository, times(1))
                .findByFollowerId(eq(testUserId), any(Pageable.class));
            verify(userGateway, times(1)).getUserLogin(eq(testFollowingId1));
            verify(followMapper, times(1))
                .toFollowingResponseDto(eq(follow1), eq("jane_doe"));
        }

        @Test
        void getFollowing_WhenNoFollowingExist_ShouldReturnEmptyPagedModel() {
            Page<Follow> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(followRepository.findByFollowerId(eq(testUserId), any(Pageable.class)))
                .thenReturn(emptyPage);

            PagedModel<FollowingResponseDto> result = followService.getFollowing(
                testUserId, null, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getMetadata().totalElements()).isEqualTo(0);

            verify(followRepository, times(1))
                .findByFollowerId(eq(testUserId), any(Pageable.class));
            verify(userGateway, never()).getUserLogin(any());
            verify(followMapper, never()).toFollowingResponseDto(any(), any());
        }

        @Test
        void getFollowing_WithLoginFilter_ShouldFilterByLogin() {
            List<Follow> follows = List.of(follow1, follow2);
            Page<Follow> followsPage = new PageImpl<>(follows, pageable, 2);
            FollowingFilter filter = new FollowingFilter("jane");

            when(followRepository.findByFollowerId(eq(testUserId), any(Pageable.class)))
                .thenReturn(followsPage);
            when(userGateway.getUserLogin(testFollowingId1)).thenReturn(Optional.of("jane_doe"));
            when(userGateway.getUserLogin(testFollowingId2)).thenReturn(Optional.of("john_smith"));
            when(followMapper.toFollowingResponseDto(follow1, "jane_doe"))
                .thenReturn(followingResponseDto1);
            when(followMapper.toFollowingResponseDto(follow2, "john_smith"))
                .thenReturn(followingResponseDto2);

            PagedModel<FollowingResponseDto> result = followService.getFollowing(
                testUserId, filter, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).containsExactly(followingResponseDto1);
            assertThat(result.getContent().get(0).login()).isEqualTo("jane_doe");
        }

        @Test
        void getFollowing_WithLoginFilter_ShouldFilterCaseInsensitively() {
            List<Follow> follows = List.of(follow1, follow2);
            Page<Follow> followsPage = new PageImpl<>(follows, pageable, 2);
            FollowingFilter filter = new FollowingFilter("JANE");

            when(followRepository.findByFollowerId(eq(testUserId), any(Pageable.class)))
                .thenReturn(followsPage);
            when(userGateway.getUserLogin(testFollowingId1)).thenReturn(Optional.of("jane_doe"));
            when(userGateway.getUserLogin(testFollowingId2)).thenReturn(Optional.of("john_smith"));
            when(followMapper.toFollowingResponseDto(follow1, "jane_doe"))
                .thenReturn(followingResponseDto1);
            when(followMapper.toFollowingResponseDto(follow2, "john_smith"))
                .thenReturn(followingResponseDto2);

            PagedModel<FollowingResponseDto> result = followService.getFollowing(
                testUserId, filter, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).containsExactly(followingResponseDto1);
        }

        @Test
        void getFollowing_WhenUserLoginNotFound_ShouldUseUnknownLogin() {
            List<Follow> follows = List.of(follow1);
            Page<Follow> followsPage = new PageImpl<>(follows, pageable, 1);
            FollowingResponseDto followingWithUnknownLogin = FollowingResponseDto.builder()
                .id(testFollowingId1)
                .login("unknown")
                .createdAt(LocalDateTime.of(2025, 1, 20, 15, 30, 0))
                .build();

            when(followRepository.findByFollowerId(eq(testUserId), any(Pageable.class)))
                .thenReturn(followsPage);
            when(userGateway.getUserLogin(testFollowingId1)).thenReturn(Optional.empty());
            when(followMapper.toFollowingResponseDto(follow1, "unknown"))
                .thenReturn(followingWithUnknownLogin);

            PagedModel<FollowingResponseDto> result = followService.getFollowing(
                testUserId, null, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).login()).isEqualTo("unknown");

            verify(userGateway, times(1)).getUserLogin(eq(testFollowingId1));
            verify(followMapper, times(1))
                .toFollowingResponseDto(eq(follow1), eq("unknown"));
        }

        @Test
        void getFollowing_WithPagination_ShouldUseCorrectPageable() {
            Pageable customPageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
            List<Follow> follows = List.of(follow1);
            Page<Follow> followsPage = new PageImpl<>(follows, customPageable, 1);

            when(followRepository.findByFollowerId(eq(testUserId), any(Pageable.class)))
                .thenReturn(followsPage);
            when(userGateway.getUserLogin(testFollowingId1)).thenReturn(Optional.of("jane_doe"));
            when(followMapper.toFollowingResponseDto(follow1, "jane_doe"))
                .thenReturn(followingResponseDto1);

            followService.getFollowing(testUserId, null, customPageable);

            verify(followRepository, times(1))
                .findByFollowerId(eq(testUserId), eq(customPageable));
        }

        @Test
        void getFollowing_WhenPageableNotSorted_ShouldAddDefaultSorting() {
            Pageable unsortedPageable = PageRequest.of(0, 10);
            List<Follow> follows = List.of(follow1);
            Page<Follow> followsPage = new PageImpl<>(follows, unsortedPageable, 1);

            when(followRepository.findByFollowerId(eq(testUserId), any(Pageable.class)))
                .thenReturn(followsPage);
            when(userGateway.getUserLogin(testFollowingId1)).thenReturn(Optional.of("jane_doe"));
            when(followMapper.toFollowingResponseDto(follow1, "jane_doe"))
                .thenReturn(followingResponseDto1);

            followService.getFollowing(testUserId, null, unsortedPageable);

            verify(followRepository, times(1)).findByFollowerId(
                eq(testUserId),
                argThat(pageable -> pageable.getSort().isSorted() &&
                    pageable.getSort().getOrderFor("createdAt") != null &&
                    pageable.getSort().getOrderFor("createdAt").getDirection() == Sort.Direction.DESC)
            );
        }
    }

    @Nested
    class GetFollowStatusTests {

        private UUID testFollowerId;
        private UUID testFollowingId;
        private Follow existingFollow;
        private FollowStatusResponseDto statusResponseDto;

        @BeforeEach
        void setUp() {
            testFollowerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            testFollowingId = UUID.fromString("987fcdeb-51a2-43d7-b123-426614174999");

            UUID followId = UUID.fromString("456e7890-e89b-12d3-a456-426614174111");
            existingFollow = Follow.builder()
                .id(followId)
                .followerId(testFollowerId)
                .followingId(testFollowingId)
                .createdAt(LocalDateTime.of(2025, 1, 20, 15, 30, 0))
                .build();

            statusResponseDto = FollowStatusResponseDto.builder()
                .isFollowing(true)
                .createdAt(LocalDateTime.of(2025, 1, 20, 15, 30, 0))
                .build();
        }

        @Test
        void getFollowStatus_WhenFollowExists_ShouldReturnFollowStatusResponseDto() {
            when(followRepository.findByFollowerIdAndFollowingId(testFollowerId, testFollowingId))
                .thenReturn(Optional.of(existingFollow));
            when(followMapper.toFollowStatusResponseDto(existingFollow))
                .thenReturn(statusResponseDto);

            FollowStatusResponseDto result = followService.getFollowStatus(testFollowerId, testFollowingId);

            assertThat(result).isNotNull();
            assertThat(result.isFollowing()).isTrue();
            assertThat(result.createdAt()).isEqualTo(existingFollow.getCreatedAt());
        }

        @Test
        void getFollowStatus_WhenFollowDoesNotExist_ShouldReturnFollowStatusResponseDtoWithFalse() {
            when(followRepository.findByFollowerIdAndFollowingId(testFollowerId, testFollowingId))
                .thenReturn(Optional.empty());

            FollowStatusResponseDto result = followService.getFollowStatus(testFollowerId, testFollowingId);

            assertThat(result).isNotNull();
            assertThat(result.isFollowing()).isFalse();
            assertThat(result.createdAt()).isNull();
        }

        @Test
        void getFollowStatus_WithValidData_ShouldCallEachDependencyExactlyOnce() {
            when(followRepository.findByFollowerIdAndFollowingId(testFollowerId, testFollowingId))
                .thenReturn(Optional.of(existingFollow));
            when(followMapper.toFollowStatusResponseDto(existingFollow))
                .thenReturn(statusResponseDto);

            followService.getFollowStatus(testFollowerId, testFollowingId);

            verify(followRepository, times(1))
                .findByFollowerIdAndFollowingId(eq(testFollowerId), eq(testFollowingId));
            verify(followMapper, times(1))
                .toFollowStatusResponseDto(eq(existingFollow));
        }

        @Test
        void getFollowStatus_WhenFollowDoesNotExist_ShouldNotCallMapper() {
            when(followRepository.findByFollowerIdAndFollowingId(testFollowerId, testFollowingId))
                .thenReturn(Optional.empty());

            followService.getFollowStatus(testFollowerId, testFollowingId);

            verify(followRepository, times(1))
                .findByFollowerIdAndFollowingId(eq(testFollowerId), eq(testFollowingId));
            verify(followMapper, never()).toFollowStatusResponseDto(any());
        }
    }

    @Nested
    class GetFollowStatsTests {

        private UUID testUserId;
        private FollowStatsResponseDto statsResponseDto;

        @BeforeEach
        void setUp() {
            testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

            statsResponseDto = FollowStatsResponseDto.builder()
                .followersCount(150L)
                .followingCount(75L)
                .build();
        }

        @Test
        void getFollowStats_WithValidData_ShouldReturnFollowStatsResponseDto() {
            long followersCount = 150L;
            long followingCount = 75L;

            when(followRepository.countByFollowingId(testUserId))
                .thenReturn(followersCount);
            when(followRepository.countByFollowerId(testUserId))
                .thenReturn(followingCount);
            when(followMapper.toFollowStatsResponseDto(followersCount, followingCount))
                .thenReturn(statsResponseDto);

            FollowStatsResponseDto result = followService.getFollowStats(testUserId);

            assertThat(result).isNotNull();
            assertThat(result.followersCount()).isEqualTo(followersCount);
            assertThat(result.followingCount()).isEqualTo(followingCount);
        }

        @Test
        void getFollowStats_WithZeroCounts_ShouldReturnFollowStatsResponseDtoWithZeros() {
            long followersCount = 0L;
            long followingCount = 0L;

            FollowStatsResponseDto zeroStats = FollowStatsResponseDto.builder()
                .followersCount(0L)
                .followingCount(0L)
                .build();

            when(followRepository.countByFollowingId(testUserId))
                .thenReturn(followersCount);
            when(followRepository.countByFollowerId(testUserId))
                .thenReturn(followingCount);
            when(followMapper.toFollowStatsResponseDto(followersCount, followingCount))
                .thenReturn(zeroStats);

            FollowStatsResponseDto result = followService.getFollowStats(testUserId);

            assertThat(result).isNotNull();
            assertThat(result.followersCount()).isEqualTo(0L);
            assertThat(result.followingCount()).isEqualTo(0L);
        }

        @Test
        void getFollowStats_WithValidData_ShouldCallEachDependencyExactlyOnce() {
            long followersCount = 150L;
            long followingCount = 75L;

            when(followRepository.countByFollowingId(testUserId))
                .thenReturn(followersCount);
            when(followRepository.countByFollowerId(testUserId))
                .thenReturn(followingCount);
            when(followMapper.toFollowStatsResponseDto(followersCount, followingCount))
                .thenReturn(statsResponseDto);

            followService.getFollowStats(testUserId);

            verify(followRepository, times(1))
                .countByFollowingId(eq(testUserId));
            verify(followRepository, times(1))
                .countByFollowerId(eq(testUserId));
            verify(followMapper, times(1))
                .toFollowStatsResponseDto(eq(followersCount), eq(followingCount));
        }
    }
}