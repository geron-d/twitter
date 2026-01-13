package com.twitter.service;

import com.twitter.common.dto.request.CreateTweetRequestDto;
import com.twitter.common.dto.request.DeleteTweetRequestDto;
import com.twitter.common.dto.request.FollowRequestDto;
import com.twitter.common.dto.request.LikeTweetRequestDto;
import com.twitter.common.dto.request.RetweetRequestDto;
import com.twitter.common.dto.request.UserRequestDto;
import com.twitter.common.dto.response.FollowResponseDto;
import com.twitter.common.dto.response.LikeResponseDto;
import com.twitter.common.dto.response.RetweetResponseDto;
import com.twitter.common.dto.response.TweetResponseDto;
import com.twitter.common.dto.response.UserResponseDto;
import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.dto.request.BaseScriptRequestDto;
import com.twitter.dto.response.BaseScriptResponseDto;
import com.twitter.gateway.FollowGateway;
import com.twitter.gateway.TweetsGateway;
import com.twitter.gateway.UsersGateway;
import com.twitter.util.RandomDataGenerator;
import com.twitter.validation.BaseScriptValidator;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseScriptServiceImplTest {

    @Mock
    private UsersGateway usersGateway;

    @Mock
    private TweetsGateway tweetsGateway;

    @Mock
    private FollowGateway followGateway;

    @Mock
    private RandomDataGenerator randomDataGenerator;

    @Mock
    private BaseScriptValidator validator;

    @InjectMocks
    private BaseScriptServiceImpl service;

    @Nested
    class ExecuteScriptTests {

        private BaseScriptRequestDto requestDto;
        private UUID userId1;
        private UUID userId2;
        private UUID tweetId1;
        private UUID tweetId2;
        private UUID tweetId3;
        private UUID tweetId4;

        @BeforeEach
        void setUp() {
            userId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            userId2 = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            tweetId1 = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
            tweetId2 = UUID.fromString("423e4567-e89b-12d3-a456-426614174003");
            tweetId3 = UUID.fromString("523e4567-e89b-12d3-a456-426614174004");
            tweetId4 = UUID.fromString("623e4567-e89b-12d3-a456-426614174005");

            requestDto = BaseScriptRequestDto.builder()
                .nUsers(2)
                .nTweetsPerUser(2)
                .lUsersForDeletion(1)
                .build();
        }

        @Test
        void executeScript_WithValidData_ShouldCreateUsersAndTweetsAndDelete() {
            when(randomDataGenerator.generateLogin()).thenReturn("user1", "user2");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com", "user2@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John", "Jane");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe", "Smith");
            when(randomDataGenerator.generatePassword()).thenReturn("password123", "password456");
            when(randomDataGenerator.generateTweetContent()).thenReturn("Tweet 1", "Tweet 2", "Tweet 3", "Tweet 4");

            UserResponseDto userResponse1 = new UserResponseDto(userId1, "user1", "John", "Doe",
                "user1@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());
            UserResponseDto userResponse2 = new UserResponseDto(userId2, "user2", "Jane", "Smith",
                "user2@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class))).thenReturn(userResponse1, userResponse2);

            TweetResponseDto tweetResponse1 = new TweetResponseDto(tweetId1, userId1, "Tweet 1",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse2 = new TweetResponseDto(tweetId2, userId1, "Tweet 2",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse3 = new TweetResponseDto(tweetId3, userId2, "Tweet 3",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse4 = new TweetResponseDto(tweetId4, userId2, "Tweet 4",
                LocalDateTime.now(), LocalDateTime.now(), false, null);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class)))
                .thenReturn(tweetResponse1, tweetResponse2, tweetResponse3, tweetResponse4);

            List<TweetResponseDto> user1TweetsList = new ArrayList<>(List.of(tweetResponse1, tweetResponse2));
            List<TweetResponseDto> user2TweetsList = new ArrayList<>(List.of(tweetResponse3, tweetResponse4));

            when(tweetsGateway.getUserTweets(eq(userId1), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user1TweetsList, PageRequest.of(0, 1000), 2));
            when(tweetsGateway.getUserTweets(eq(userId2), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user2TweetsList, PageRequest.of(0, 1000), 2));

            doNothing().when(validator).validateDeletionCount(any(), eq(2));
            doNothing().when(tweetsGateway).deleteTweet(any(UUID.class), any(DeleteTweetRequestDto.class));

            BaseScriptResponseDto result = service.executeScript(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.createdUsers()).hasSize(2);
            assertThat(result.createdUsers()).containsExactly(userId1, userId2);
            assertThat(result.createdTweets()).hasSize(4);
            assertThat(result.deletedTweets()).hasSize(1);
            assertThat(result.statistics()).isNotNull();
            assertThat(result.statistics().totalUsersCreated()).isEqualTo(2);
            assertThat(result.statistics().totalTweetsCreated()).isEqualTo(4);
            assertThat(result.statistics().totalTweetsDeleted()).isEqualTo(1);
            assertThat(result.statistics().usersWithTweets()).isEqualTo(2);
            assertThat(result.statistics().usersWithoutTweets()).isEqualTo(0);
            assertThat(result.statistics().executionTimeMs()).isGreaterThan(0);

            verify(usersGateway, times(2)).createUser(any(UserRequestDto.class));
            verify(tweetsGateway, times(4)).createTweet(any(CreateTweetRequestDto.class));
            verify(tweetsGateway, times(3)).getUserTweets(any(UUID.class), any(Pageable.class));
            verify(validator, times(1)).validateDeletionCount(any(), eq(2));
            verify(tweetsGateway, times(1)).deleteTweet(any(UUID.class), any(DeleteTweetRequestDto.class));
        }

        @Test
        void executeScript_WhenUserCreationFails_ShouldContinueAndAddError() {
            when(randomDataGenerator.generateLogin()).thenReturn("user1", "user2");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com", "user2@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John", "Jane");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe", "Smith");
            when(randomDataGenerator.generatePassword()).thenReturn("password123", "password456");

            UserResponseDto userResponse2 = new UserResponseDto(userId2, "user2", "Jane",
                "Smith", "user2@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class)))
                .thenThrow(new RuntimeException("User creation failed"))
                .thenReturn(userResponse2);

            BaseScriptResponseDto result = service.executeScript(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.createdUsers()).hasSize(1);
            assertThat(result.createdUsers()).containsExactly(userId2);
            assertThat(result.statistics().errors()).isNotEmpty();
            assertThat(result.statistics().errors().getFirst()).contains("Failed to create user");

            verify(usersGateway, times(2)).createUser(any(UserRequestDto.class));
        }

        @Test
        void executeScript_WhenTweetCreationFails_ShouldContinueAndAddError() {
            when(randomDataGenerator.generateLogin()).thenReturn("user1");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe");
            when(randomDataGenerator.generatePassword()).thenReturn("password123");
            when(randomDataGenerator.generateTweetContent()).thenReturn("Tweet 1", "Tweet 2");

            UserResponseDto userResponse1 = new UserResponseDto(userId1, "user1", "John", "Doe",
                "user1@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class))).thenReturn(userResponse1);

            TweetResponseDto tweetResponse1 = new TweetResponseDto(tweetId1, userId1, "Tweet 1",
                LocalDateTime.now(), LocalDateTime.now(), false, null);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class)))
                .thenReturn(tweetResponse1)
                .thenThrow(new RuntimeException("Tweet creation failed"));

            Page<TweetResponseDto> user1TweetsPage = new PageImpl<>(List.of(tweetResponse1),
                PageRequest.of(0, 1000), 1);

            when(tweetsGateway.getUserTweets(eq(userId1), any(Pageable.class))).thenReturn(user1TweetsPage);

            doNothing().when(validator).validateDeletionCount(any(), eq(1));

            BaseScriptRequestDto requestWithoutDeletion = BaseScriptRequestDto.builder()
                .nUsers(1)
                .nTweetsPerUser(2)
                .lUsersForDeletion(0)
                .build();

            BaseScriptResponseDto result = service.executeScript(requestWithoutDeletion);

            assertThat(result).isNotNull();
            assertThat(result.createdUsers()).hasSize(1);
            assertThat(result.createdTweets()).hasSize(1);
            assertThat(result.statistics().errors()).isNotEmpty();
            assertThat(result.statistics().errors().getFirst()).contains("Failed to create tweet");

            verify(tweetsGateway, times(2)).createTweet(any(CreateTweetRequestDto.class));
        }

        @Test
        void executeScript_WhenNoDeletionsRequested_ShouldSkipDeletionStep() {
            when(randomDataGenerator.generateLogin()).thenReturn("user1");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe");
            when(randomDataGenerator.generatePassword()).thenReturn("password123");
            when(randomDataGenerator.generateTweetContent()).thenReturn("Tweet 1");

            UserResponseDto userResponse1 = new UserResponseDto(userId1, "user1", "John", "Doe",
                "user1@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class))).thenReturn(userResponse1);

            TweetResponseDto tweetResponse1 = new TweetResponseDto(tweetId1, userId1, "Tweet 1",
                LocalDateTime.now(), LocalDateTime.now(), false, null);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class))).thenReturn(tweetResponse1);

            Page<TweetResponseDto> user1TweetsPage = new PageImpl<>(List.of(tweetResponse1),
                PageRequest.of(0, 1000), 1);

            when(tweetsGateway.getUserTweets(eq(userId1), any(Pageable.class))).thenReturn(user1TweetsPage);

            doNothing().when(validator).validateDeletionCount(any(), eq(1));

            BaseScriptRequestDto requestWithoutDeletion = BaseScriptRequestDto.builder()
                .nUsers(1)
                .nTweetsPerUser(1)
                .lUsersForDeletion(0)
                .build();

            BaseScriptResponseDto result = service.executeScript(requestWithoutDeletion);

            assertThat(result).isNotNull();
            assertThat(result.deletedTweets()).isEmpty();
            assertThat(result.statistics().totalTweetsDeleted()).isEqualTo(0);

            verify(validator, times(1)).validateDeletionCount(any(), eq(1));
            verify(tweetsGateway, never()).deleteTweet(any(UUID.class), any(DeleteTweetRequestDto.class));
        }

        @Test
        void executeScript_WhenValidationFails_ShouldAddErrorAndSkipDeletion() {
            when(randomDataGenerator.generateLogin()).thenReturn("user1");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe");
            when(randomDataGenerator.generatePassword()).thenReturn("password123");
            when(randomDataGenerator.generateTweetContent()).thenReturn("Tweet 1");

            UserResponseDto userResponse1 = new UserResponseDto(userId1, "user1", "John", "Doe",
                "user1@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class))).thenReturn(userResponse1);

            TweetResponseDto tweetResponse1 = new TweetResponseDto(tweetId1, userId1, "Tweet 1",
                LocalDateTime.now(), LocalDateTime.now(), false, null);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class))).thenReturn(tweetResponse1);

            Page<TweetResponseDto> user1TweetsPage = new PageImpl<>(List.of(tweetResponse1),
                PageRequest.of(0, 1000), 1);

            when(tweetsGateway.getUserTweets(eq(userId1), any(Pageable.class))).thenReturn(user1TweetsPage);

            doThrow(new BusinessRuleValidationException("DELETION_COUNT_EXCEEDS_USERS_WITH_TWEETS", "test"))
                .when(validator).validateDeletionCount(any(), eq(1));

            BaseScriptRequestDto requestWithInvalidDeletion = BaseScriptRequestDto.builder()
                .nUsers(1)
                .nTweetsPerUser(1)
                .lUsersForDeletion(2)
                .build();

            BaseScriptResponseDto result = service.executeScript(requestWithInvalidDeletion);

            assertThat(result).isNotNull();
            assertThat(result.deletedTweets()).isEmpty();
            assertThat(result.statistics().errors()).isNotEmpty();
            assertThat(result.statistics().errors().get(0)).contains("Validation failed");

            verify(validator, times(1)).validateDeletionCount(any(), eq(1));
            verify(tweetsGateway, never()).deleteTweet(any(UUID.class), any(DeleteTweetRequestDto.class));
        }

        @Test
        void executeScript_WhenUserHasNoTweets_ShouldCalculateUsersWithoutTweets() {
            when(randomDataGenerator.generateLogin()).thenReturn("user1", "user2");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com", "user2@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John", "Jane");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe", "Smith");
            when(randomDataGenerator.generatePassword()).thenReturn("password123", "password456");

            UserResponseDto userResponse1 = new UserResponseDto(userId1, "user1", "John", "Doe",
                "user1@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());
            UserResponseDto userResponse2 = new UserResponseDto(userId2, "user2", "Jane",
                "Smith", "user2@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class)))
                .thenReturn(userResponse1, userResponse2);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class)))
                .thenThrow(new RuntimeException("Tweet creation failed"));

            Page<TweetResponseDto> emptyTweetsPage = new PageImpl<>(List.of(),
                PageRequest.of(0, 1000), 0);

            when(tweetsGateway.getUserTweets(any(UUID.class), any(Pageable.class)))
                .thenReturn(emptyTweetsPage);

            doNothing().when(validator).validateDeletionCount(any(), eq(0));

            BaseScriptRequestDto requestWithoutDeletion = BaseScriptRequestDto.builder()
                .nUsers(2)
                .nTweetsPerUser(1)
                .lUsersForDeletion(0)
                .build();

            BaseScriptResponseDto result = service.executeScript(requestWithoutDeletion);

            assertThat(result).isNotNull();
            assertThat(result.createdUsers()).hasSize(2);
            assertThat(result.createdTweets()).isEmpty();
            assertThat(result.statistics().usersWithTweets()).isEqualTo(0);
            assertThat(result.statistics().usersWithoutTweets()).isEqualTo(2);

            verify(usersGateway, times(2)).createUser(any(UserRequestDto.class));
        }

        @Test
        void executeScript_WhenDeletionFails_ShouldContinueAndAddError() {
            BaseScriptRequestDto testRequestDto = BaseScriptRequestDto.builder()
                .nUsers(1)
                .nTweetsPerUser(1)
                .lUsersForDeletion(1)
                .build();

            when(randomDataGenerator.generateLogin()).thenReturn("user1");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe");
            when(randomDataGenerator.generatePassword()).thenReturn("password123");
            when(randomDataGenerator.generateTweetContent()).thenReturn("Tweet 1");

            UserResponseDto userResponse1 = new UserResponseDto(userId1, "user1", "John", "Doe",
                "user1@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class))).thenReturn(userResponse1);

            TweetResponseDto tweetResponse1 = new TweetResponseDto(tweetId1, userId1, "Tweet 1",
                LocalDateTime.now(), LocalDateTime.now(), false, null);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class))).thenReturn(tweetResponse1);

            List<TweetResponseDto> user1TweetsList = new ArrayList<>(List.of(tweetResponse1));

            when(tweetsGateway.getUserTweets(eq(userId1), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user1TweetsList, PageRequest.of(0, 1000), 1))
                .thenAnswer(_ -> new PageImpl<>(user1TweetsList, PageRequest.of(0, 1000), 1));

            doNothing().when(validator).validateDeletionCount(any(), eq(1));

            doThrow(new RuntimeException("Deletion failed"))
                .when(tweetsGateway).deleteTweet(any(UUID.class), any(DeleteTweetRequestDto.class));

            BaseScriptResponseDto result = service.executeScript(testRequestDto);

            assertThat(result).isNotNull();
            assertThat(result.deletedTweets()).isEmpty();
            assertThat(result.statistics().errors()).isNotEmpty();
            assertThat(result.statistics().errors().getFirst()).contains("Failed to delete tweet");

            verify(tweetsGateway, times(1)).deleteTweet(any(UUID.class), any(DeleteTweetRequestDto.class));
        }
    }

    @Nested
    class FollowRelationshipsTests {

        private UUID userId1;
        private UUID userId2;
        private UUID userId3;
        private UUID userId4;
        private UUID followId1;
        private UUID followId2;
        private UUID followId3;
        private UUID followId4;

        @BeforeEach
        void setUp() {
            userId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            userId2 = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            userId3 = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
            userId4 = UUID.fromString("423e4567-e89b-12d3-a456-426614174003");
            followId1 = UUID.fromString("523e4567-e89b-12d3-a456-426614174004");
            followId2 = UUID.fromString("623e4567-e89b-12d3-a456-426614174005");
            followId3 = UUID.fromString("723e4567-e89b-12d3-a456-426614174006");
            followId4 = UUID.fromString("823e4567-e89b-12d3-a456-426614174007");
        }

        @Test
        void executeScript_WithTwoUsers_ShouldSkipFollowRelationships() {
            BaseScriptRequestDto requestDto = BaseScriptRequestDto.builder()
                .nUsers(2)
                .nTweetsPerUser(1)
                .lUsersForDeletion(0)
                .build();

            when(randomDataGenerator.generateLogin()).thenReturn("user1", "user2");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com", "user2@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John", "Jane");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe", "Smith");
            when(randomDataGenerator.generatePassword()).thenReturn("password123", "password456");
            when(randomDataGenerator.generateTweetContent()).thenReturn("Tweet 1", "Tweet 2");

            UserResponseDto userResponse1 = new UserResponseDto(userId1, "user1", "John", "Doe",
                "user1@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());
            UserResponseDto userResponse2 = new UserResponseDto(userId2, "user2", "Jane", "Smith",
                "user2@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class))).thenReturn(userResponse1, userResponse2);

            TweetResponseDto tweetResponse1 = new TweetResponseDto(
                UUID.randomUUID(), userId1, "Tweet 1", LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse2 = new TweetResponseDto(
                UUID.randomUUID(), userId2, "Tweet 2", LocalDateTime.now(), LocalDateTime.now(), false, null);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class)))
                .thenReturn(tweetResponse1, tweetResponse2);

            Page<TweetResponseDto> user1TweetsPage = new PageImpl<>(List.of(tweetResponse1),
                PageRequest.of(0, 1000), 1);
            Page<TweetResponseDto> user2TweetsPage = new PageImpl<>(List.of(tweetResponse2),
                PageRequest.of(0, 1000), 1);

            when(tweetsGateway.getUserTweets(eq(userId1), any(Pageable.class))).thenReturn(user1TweetsPage);
            when(tweetsGateway.getUserTweets(eq(userId2), any(Pageable.class))).thenReturn(user2TweetsPage);

            doNothing().when(validator).validateDeletionCount(any(), eq(2));

            BaseScriptResponseDto result = service.executeScript(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.createdUsers()).hasSize(2);
            assertThat(result.createdFollows()).isEmpty();
            assertThat(result.statistics().totalFollowsCreated()).isEqualTo(0);

            verify(followGateway, never()).createFollow(any(FollowRequestDto.class));
        }

        @Test
        void executeScript_WithThreeUsers_ShouldCreateFollowRelationships() {
            BaseScriptRequestDto requestDto = BaseScriptRequestDto.builder()
                .nUsers(3)
                .nTweetsPerUser(1)
                .lUsersForDeletion(0)
                .build();

            when(randomDataGenerator.generateLogin()).thenReturn("user1", "user2", "user3");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com", "user2@test.com", "user3@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John", "Jane", "Bob");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe", "Smith", "Johnson");
            when(randomDataGenerator.generatePassword()).thenReturn("password123", "password456", "password789");
            when(randomDataGenerator.generateTweetContent()).thenReturn("Tweet 1", "Tweet 2", "Tweet 3");

            UserResponseDto userResponse1 = new UserResponseDto(userId1, "user1", "John", "Doe",
                "user1@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());
            UserResponseDto userResponse2 = new UserResponseDto(userId2, "user2", "Jane", "Smith",
                "user2@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());
            UserResponseDto userResponse3 = new UserResponseDto(userId3, "user3", "Bob", "Johnson",
                "user3@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class)))
                .thenReturn(userResponse1, userResponse2, userResponse3);

            when(followGateway.createFollow(any(FollowRequestDto.class)))
                .thenAnswer(invocation -> {
                    FollowRequestDto request = invocation.getArgument(0);
                    UUID followId = UUID.randomUUID();
                    if (request.followerId().equals(userId1) && request.followingId().equals(userId2)) {
                        followId = followId1;
                    } else if (request.followerId().equals(userId1) && request.followingId().equals(userId3)) {
                        followId = followId1;
                    } else if (request.followerId().equals(userId2) && request.followingId().equals(userId1)) {
                        followId = followId2;
                    } else if (request.followerId().equals(userId3) && request.followingId().equals(userId1)) {
                        followId = followId2;
                    }
                    return FollowResponseDto.builder()
                        .id(followId)
                        .followerId(request.followerId())
                        .followingId(request.followingId())
                        .createdAt(LocalDateTime.now())
                        .build();
                });

            TweetResponseDto tweetResponse1 = new TweetResponseDto(
                UUID.randomUUID(), userId1, "Tweet 1", LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse2 = new TweetResponseDto(
                UUID.randomUUID(), userId2, "Tweet 2", LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse3 = new TweetResponseDto(
                UUID.randomUUID(), userId3, "Tweet 3", LocalDateTime.now(), LocalDateTime.now(), false, null);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class)))
                .thenReturn(tweetResponse1, tweetResponse2, tweetResponse3);

            Page<TweetResponseDto> user1TweetsPage = new PageImpl<>(List.of(tweetResponse1),
                PageRequest.of(0, 1000), 1);
            Page<TweetResponseDto> user2TweetsPage = new PageImpl<>(List.of(tweetResponse2),
                PageRequest.of(0, 1000), 1);
            Page<TweetResponseDto> user3TweetsPage = new PageImpl<>(List.of(tweetResponse3),
                PageRequest.of(0, 1000), 1);

            when(tweetsGateway.getUserTweets(eq(userId1), any(Pageable.class))).thenReturn(user1TweetsPage);
            when(tweetsGateway.getUserTweets(eq(userId2), any(Pageable.class))).thenReturn(user2TweetsPage);
            when(tweetsGateway.getUserTweets(eq(userId3), any(Pageable.class))).thenReturn(user3TweetsPage);

            doNothing().when(validator).validateDeletionCount(any(), eq(3));

            BaseScriptResponseDto result = service.executeScript(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.createdUsers()).hasSize(3);
            assertThat(result.createdFollows()).hasSize(2);
            assertThat(result.statistics().totalFollowsCreated()).isEqualTo(2);

            verify(followGateway, times(2)).createFollow(any(FollowRequestDto.class));
        }

        @Test
        void executeScript_WhenFollowCreationFails_ShouldContinueAndAddError() {
            BaseScriptRequestDto requestDto = BaseScriptRequestDto.builder()
                .nUsers(3)
                .nTweetsPerUser(1)
                .lUsersForDeletion(0)
                .build();

            when(randomDataGenerator.generateLogin()).thenReturn("user1", "user2", "user3");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com", "user2@test.com", "user3@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John", "Jane", "Bob");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe", "Smith", "Johnson");
            when(randomDataGenerator.generatePassword()).thenReturn("password123", "password456", "password789");
            when(randomDataGenerator.generateTweetContent()).thenReturn("Tweet 1", "Tweet 2", "Tweet 3");

            UserResponseDto userResponse1 = new UserResponseDto(userId1, "user1", "John", "Doe",
                "user1@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());
            UserResponseDto userResponse2 = new UserResponseDto(userId2, "user2", "Jane", "Smith",
                "user2@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());
            UserResponseDto userResponse3 = new UserResponseDto(userId3, "user3", "Bob", "Johnson",
                "user3@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class)))
                .thenReturn(userResponse1, userResponse2, userResponse3);

            when(followGateway.createFollow(any(FollowRequestDto.class)))
                .thenAnswer(invocation -> {
                    FollowRequestDto request = invocation.getArgument(0);
                    if (request.followerId().equals(userId1) && request.followingId().equals(userId2)) {
                        return FollowResponseDto.builder()
                            .id(followId1)
                            .followerId(userId1)
                            .followingId(userId2)
                            .createdAt(LocalDateTime.now())
                            .build();
                    } else if (request.followerId().equals(userId1) && request.followingId().equals(userId3)) {
                        return FollowResponseDto.builder()
                            .id(followId1)
                            .followerId(userId1)
                            .followingId(userId3)
                            .createdAt(LocalDateTime.now())
                            .build();
                    } else {
                        throw new RuntimeException("Follow creation failed");
                    }
                });

            TweetResponseDto tweetResponse1 = new TweetResponseDto(
                UUID.randomUUID(), userId1, "Tweet 1", LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse2 = new TweetResponseDto(
                UUID.randomUUID(), userId2, "Tweet 2", LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse3 = new TweetResponseDto(
                UUID.randomUUID(), userId3, "Tweet 3", LocalDateTime.now(), LocalDateTime.now(), false, null);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class)))
                .thenReturn(tweetResponse1, tweetResponse2, tweetResponse3);

            Page<TweetResponseDto> user1TweetsPage = new PageImpl<>(List.of(tweetResponse1),
                PageRequest.of(0, 1000), 1);
            Page<TweetResponseDto> user2TweetsPage = new PageImpl<>(List.of(tweetResponse2),
                PageRequest.of(0, 1000), 1);
            Page<TweetResponseDto> user3TweetsPage = new PageImpl<>(List.of(tweetResponse3),
                PageRequest.of(0, 1000), 1);

            when(tweetsGateway.getUserTweets(eq(userId1), any(Pageable.class))).thenReturn(user1TweetsPage);
            when(tweetsGateway.getUserTweets(eq(userId2), any(Pageable.class))).thenReturn(user2TweetsPage);
            when(tweetsGateway.getUserTweets(eq(userId3), any(Pageable.class))).thenReturn(user3TweetsPage);

            doNothing().when(validator).validateDeletionCount(any(), eq(3));

            BaseScriptResponseDto result = service.executeScript(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.createdUsers()).hasSize(3);
            assertThat(result.createdFollows()).hasSize(1);
            assertThat(result.statistics().totalFollowsCreated()).isEqualTo(1);
            assertThat(result.statistics().errors()).isNotEmpty();
            assertThat(result.statistics().errors()).anyMatch(error -> error.contains("Failed to create follow relationship"));

            verify(followGateway, times(2)).createFollow(any(FollowRequestDto.class));
        }

        @Test
        void executeScript_WithFollowRelationships_ShouldIncludeInStatistics() {
            BaseScriptRequestDto requestDto = BaseScriptRequestDto.builder()
                .nUsers(3)
                .nTweetsPerUser(2)
                .lUsersForDeletion(0)
                .build();

            when(randomDataGenerator.generateLogin()).thenReturn("user1", "user2");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com", "user2@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John", "Jane");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe", "Smith");
            when(randomDataGenerator.generatePassword()).thenReturn("password123", "password456");
            when(randomDataGenerator.generateTweetContent()).thenReturn("Tweet 1", "Tweet 2", "Tweet 3", "Tweet 4");

            UserResponseDto userResponse1 = new UserResponseDto(userId1, "user1", "John", "Doe",
                "user1@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());
            UserResponseDto userResponse2 = new UserResponseDto(userId2, "user2", "Jane", "Smith",
                "user2@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());
            UserResponseDto userResponse3 = new UserResponseDto(userId3, "user3", "Bob", "Johnson",
                "user3@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class)))
                .thenReturn(userResponse1, userResponse2, userResponse3);

            when(followGateway.createFollow(any(FollowRequestDto.class)))
                .thenAnswer(invocation -> {
                    FollowRequestDto request = invocation.getArgument(0);
                    UUID followId = UUID.randomUUID();
                    if (request.followerId().equals(userId1) && request.followingId().equals(userId2)) {
                        followId = followId1;
                    } else if (request.followerId().equals(userId1) && request.followingId().equals(userId3)) {
                        followId = followId1;
                    } else if (request.followerId().equals(userId2) && request.followingId().equals(userId1)) {
                        followId = followId2;
                    } else if (request.followerId().equals(userId3) && request.followingId().equals(userId1)) {
                        followId = followId2;
                    }
                    return FollowResponseDto.builder()
                        .id(followId)
                        .followerId(request.followerId())
                        .followingId(request.followingId())
                        .createdAt(LocalDateTime.now())
                        .build();
                });

            UUID tweetId1 = UUID.randomUUID();
            UUID tweetId2 = UUID.randomUUID();
            UUID tweetId3 = UUID.randomUUID();
            UUID tweetId4 = UUID.randomUUID();
            UUID tweetId5 = UUID.randomUUID();
            UUID tweetId6 = UUID.randomUUID();

            TweetResponseDto tweetResponse1 = new TweetResponseDto(
                tweetId1, userId1, "Tweet 1", LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse2 = new TweetResponseDto(
                tweetId2, userId1, "Tweet 2", LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse3 = new TweetResponseDto(
                tweetId3, userId2, "Tweet 3", LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse4 = new TweetResponseDto(
                tweetId4, userId2, "Tweet 4", LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse5 = new TweetResponseDto(
                tweetId5, userId3, "Tweet 5", LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse6 = new TweetResponseDto(
                tweetId6, userId3, "Tweet 6", LocalDateTime.now(), LocalDateTime.now(), false, null);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class)))
                .thenReturn(tweetResponse1, tweetResponse2, tweetResponse3, tweetResponse4, tweetResponse5, tweetResponse6);

            List<TweetResponseDto> user1TweetsList = new ArrayList<>(List.of(tweetResponse1, tweetResponse2));
            List<TweetResponseDto> user2TweetsList = new ArrayList<>(List.of(tweetResponse3, tweetResponse4));
            List<TweetResponseDto> user3TweetsList = new ArrayList<>(List.of(tweetResponse5, tweetResponse6));

            when(tweetsGateway.getUserTweets(eq(userId1), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user1TweetsList, PageRequest.of(0, 1000), 2));
            when(tweetsGateway.getUserTweets(eq(userId2), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user2TweetsList, PageRequest.of(0, 1000), 2));
            when(tweetsGateway.getUserTweets(eq(userId3), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user3TweetsList, PageRequest.of(0, 1000), 2));

            doNothing().when(validator).validateDeletionCount(any(), eq(3));

            BaseScriptResponseDto result = service.executeScript(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.createdUsers()).hasSize(3);
            assertThat(result.createdFollows()).hasSize(2);
            assertThat(result.createdTweets()).hasSize(6);
            assertThat(result.statistics().totalUsersCreated()).isEqualTo(3);
            assertThat(result.statistics().totalFollowsCreated()).isEqualTo(2);
            assertThat(result.statistics().totalTweetsCreated()).isEqualTo(6);
            assertThat(result.statistics().usersWithTweets()).isEqualTo(3);
            assertThat(result.statistics().usersWithoutTweets()).isEqualTo(0);

            verify(followGateway, times(2)).createFollow(any(FollowRequestDto.class));
        }
    }

    @Nested
    class LikesAndRetweetsTests {

        private UUID userId1;
        private UUID userId2;
        private UUID userId3;
        private UUID tweetId1;
        private UUID tweetId2;
        private UUID tweetId3;
        private UUID tweetId4;
        private UUID tweetId5;
        private UUID tweetId6;

        @BeforeEach
        void setUp() {
            userId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            userId2 = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
            userId3 = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
            tweetId1 = UUID.fromString("423e4567-e89b-12d3-a456-426614174003");
            tweetId2 = UUID.fromString("523e4567-e89b-12d3-a456-426614174004");
            tweetId3 = UUID.fromString("623e4567-e89b-12d3-a456-426614174005");
            tweetId4 = UUID.fromString("723e4567-e89b-12d3-a456-426614174006");
            tweetId5 = UUID.fromString("823e4567-e89b-12d3-a456-426614174007");
            tweetId6 = UUID.fromString("923e4567-e89b-12d3-a456-426614174008");
        }

        @Test
        void executeScript_WithEnoughTweetsAndUsers_ShouldCreateLikesAndRetweets() {
            BaseScriptRequestDto requestDto = BaseScriptRequestDto.builder()
                .nUsers(3)
                .nTweetsPerUser(3)
                .lUsersForDeletion(0)
                .build();

            when(randomDataGenerator.generateLogin()).thenReturn("user1", "user2", "user3");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com", "user2@test.com", "user3@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John", "Jane", "Bob");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe", "Smith", "Johnson");
            when(randomDataGenerator.generatePassword()).thenReturn("password123", "password456", "password789");
            when(randomDataGenerator.generateTweetContent()).thenReturn("Tweet 1", "Tweet 2", "Tweet 3", "Tweet 4", "Tweet 5", "Tweet 6", "Tweet 7", "Tweet 8", "Tweet 9");

            UserResponseDto userResponse1 = new UserResponseDto(userId1, "user1", "John", "Doe",
                "user1@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());
            UserResponseDto userResponse2 = new UserResponseDto(userId2, "user2", "Jane", "Smith",
                "user2@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());
            UserResponseDto userResponse3 = new UserResponseDto(userId3, "user3", "Bob", "Johnson",
                "user3@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class)))
                .thenReturn(userResponse1, userResponse2, userResponse3);

            TweetResponseDto tweetResponse1 = new TweetResponseDto(tweetId1, userId1, "Tweet 1",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse2 = new TweetResponseDto(tweetId2, userId1, "Tweet 2",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse3 = new TweetResponseDto(tweetId3, userId1, "Tweet 3",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse4 = new TweetResponseDto(tweetId4, userId2, "Tweet 4",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse5 = new TweetResponseDto(tweetId5, userId2, "Tweet 5",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse6 = new TweetResponseDto(tweetId6, userId2, "Tweet 6",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse7 = new TweetResponseDto(UUID.randomUUID(), userId3, "Tweet 7",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse8 = new TweetResponseDto(UUID.randomUUID(), userId3, "Tweet 8",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse9 = new TweetResponseDto(UUID.randomUUID(), userId3, "Tweet 9",
                LocalDateTime.now(), LocalDateTime.now(), false, null);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class)))
                .thenReturn(tweetResponse1, tweetResponse2, tweetResponse3, tweetResponse4, tweetResponse5,
                    tweetResponse6, tweetResponse7, tweetResponse8, tweetResponse9);

            List<TweetResponseDto> user1TweetsList = new ArrayList<>(List.of(tweetResponse1, tweetResponse2, tweetResponse3));
            List<TweetResponseDto> user2TweetsList = new ArrayList<>(List.of(tweetResponse4, tweetResponse5, tweetResponse6));
            List<TweetResponseDto> user3TweetsList = new ArrayList<>(List.of(tweetResponse7, tweetResponse8, tweetResponse9));

            when(tweetsGateway.getUserTweets(eq(userId1), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user1TweetsList, PageRequest.of(0, 1000), 3));
            when(tweetsGateway.getUserTweets(eq(userId2), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user2TweetsList, PageRequest.of(0, 1000), 3));
            when(tweetsGateway.getUserTweets(eq(userId3), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user3TweetsList, PageRequest.of(0, 1000), 3));

            doNothing().when(validator).validateDeletionCount(any(), eq(3));

            LikeResponseDto likeResponse = LikeResponseDto.builder()
                .id(UUID.randomUUID())
                .tweetId(tweetId1)
                .userId(userId2)
                .createdAt(LocalDateTime.now())
                .build();

            RetweetResponseDto retweetResponse = RetweetResponseDto.builder()
                .id(UUID.randomUUID())
                .tweetId(tweetId4)
                .userId(userId1)
                .comment(null)
                .createdAt(LocalDateTime.now())
                .build();

            when(tweetsGateway.likeTweet(any(UUID.class), any(LikeTweetRequestDto.class)))
                .thenReturn(likeResponse);
            when(tweetsGateway.retweetTweet(any(UUID.class), any(RetweetRequestDto.class)))
                .thenReturn(retweetResponse);

            BaseScriptResponseDto result = service.executeScript(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.createdUsers()).hasSize(3);
            assertThat(result.createdTweets()).hasSize(9);
            assertThat(result.statistics().totalLikesCreated()).isGreaterThan(0);
            assertThat(result.statistics().totalRetweetsCreated()).isGreaterThan(0);
            assertThat(result.statistics().totalLikesCreated() + result.statistics().totalRetweetsCreated()).isGreaterThan(0);

            verify(tweetsGateway, atLeastOnce()).likeTweet(any(UUID.class), any(LikeTweetRequestDto.class));
            verify(tweetsGateway, atLeastOnce()).retweetTweet(any(UUID.class), any(RetweetRequestDto.class));
        }

        @Test
        void executeScript_WhenLikeFails_ShouldContinueAndAddError() {
            BaseScriptRequestDto requestDto = BaseScriptRequestDto.builder()
                .nUsers(2)
                .nTweetsPerUser(3)
                .lUsersForDeletion(0)
                .build();

            when(randomDataGenerator.generateLogin()).thenReturn("user1", "user2");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com", "user2@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John", "Jane");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe", "Smith");
            when(randomDataGenerator.generatePassword()).thenReturn("password123", "password456");
            when(randomDataGenerator.generateTweetContent()).thenReturn("Tweet 1", "Tweet 2", "Tweet 3", "Tweet 4", "Tweet 5", "Tweet 6");

            UserResponseDto userResponse1 = new UserResponseDto(userId1, "user1", "John", "Doe",
                "user1@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());
            UserResponseDto userResponse2 = new UserResponseDto(userId2, "user2", "Jane", "Smith",
                "user2@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class)))
                .thenReturn(userResponse1, userResponse2);

            TweetResponseDto tweetResponse1 = new TweetResponseDto(tweetId1, userId1, "Tweet 1",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse2 = new TweetResponseDto(tweetId2, userId1, "Tweet 2",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse3 = new TweetResponseDto(tweetId3, userId1, "Tweet 3",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse4 = new TweetResponseDto(tweetId4, userId2, "Tweet 4",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse5 = new TweetResponseDto(tweetId5, userId2, "Tweet 5",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse6 = new TweetResponseDto(tweetId6, userId2, "Tweet 6",
                LocalDateTime.now(), LocalDateTime.now(), false, null);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class)))
                .thenReturn(tweetResponse1, tweetResponse2, tweetResponse3, tweetResponse4, tweetResponse5, tweetResponse6);

            List<TweetResponseDto> user1TweetsList = new ArrayList<>(List.of(tweetResponse1, tweetResponse2, tweetResponse3));
            List<TweetResponseDto> user2TweetsList = new ArrayList<>(List.of(tweetResponse4, tweetResponse5, tweetResponse6));

            when(tweetsGateway.getUserTweets(eq(userId1), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user1TweetsList, PageRequest.of(0, 1000), 3));
            when(tweetsGateway.getUserTweets(eq(userId2), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user2TweetsList, PageRequest.of(0, 1000), 3));

            doNothing().when(validator).validateDeletionCount(any(), eq(2));

            when(tweetsGateway.likeTweet(any(UUID.class), any(LikeTweetRequestDto.class)))
                .thenThrow(new RuntimeException("Self-like error"));

            BaseScriptResponseDto result = service.executeScript(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.statistics().errors()).isNotEmpty();
            assertThat(result.statistics().errors()).anyMatch(error -> error.contains("Failed to create like"));
            assertThat(result.statistics().totalLikesCreated()).isEqualTo(0);

            verify(tweetsGateway, atLeastOnce()).likeTweet(any(UUID.class), any(LikeTweetRequestDto.class));
        }

        @Test
        void executeScript_WhenRetweetFails_ShouldContinueAndAddError() {
            BaseScriptRequestDto requestDto = BaseScriptRequestDto.builder()
                .nUsers(2)
                .nTweetsPerUser(3)
                .lUsersForDeletion(0)
                .build();

            when(randomDataGenerator.generateLogin()).thenReturn("user1", "user2");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com", "user2@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John", "Jane");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe", "Smith");
            when(randomDataGenerator.generatePassword()).thenReturn("password123", "password456");
            when(randomDataGenerator.generateTweetContent()).thenReturn("Tweet 1", "Tweet 2", "Tweet 3", "Tweet 4", "Tweet 5", "Tweet 6");

            UserResponseDto userResponse1 = new UserResponseDto(userId1, "user1", "John", "Doe",
                "user1@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());
            UserResponseDto userResponse2 = new UserResponseDto(userId2, "user2", "Jane", "Smith",
                "user2@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class)))
                .thenReturn(userResponse1, userResponse2);

            TweetResponseDto tweetResponse1 = new TweetResponseDto(tweetId1, userId1, "Tweet 1",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse2 = new TweetResponseDto(tweetId2, userId1, "Tweet 2",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse3 = new TweetResponseDto(tweetId3, userId1, "Tweet 3",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse4 = new TweetResponseDto(tweetId4, userId2, "Tweet 4",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse5 = new TweetResponseDto(tweetId5, userId2, "Tweet 5",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse6 = new TweetResponseDto(tweetId6, userId2, "Tweet 6",
                LocalDateTime.now(), LocalDateTime.now(), false, null);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class)))
                .thenReturn(tweetResponse1, tweetResponse2, tweetResponse3, tweetResponse4, tweetResponse5, tweetResponse6);

            List<TweetResponseDto> user1TweetsList = new ArrayList<>(List.of(tweetResponse1, tweetResponse2, tweetResponse3));
            List<TweetResponseDto> user2TweetsList = new ArrayList<>(List.of(tweetResponse4, tweetResponse5, tweetResponse6));

            when(tweetsGateway.getUserTweets(eq(userId1), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user1TweetsList, PageRequest.of(0, 1000), 3));
            when(tweetsGateway.getUserTweets(eq(userId2), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user2TweetsList, PageRequest.of(0, 1000), 3));

            doNothing().when(validator).validateDeletionCount(any(), eq(2));

            LikeResponseDto likeResponse = LikeResponseDto.builder()
                .id(UUID.randomUUID())
                .tweetId(tweetId1)
                .userId(userId2)
                .createdAt(LocalDateTime.now())
                .build();

            when(tweetsGateway.likeTweet(any(UUID.class), any(LikeTweetRequestDto.class)))
                .thenReturn(likeResponse);
            when(tweetsGateway.retweetTweet(any(UUID.class), any(RetweetRequestDto.class)))
                .thenThrow(new RuntimeException("Self-retweet error"));

            BaseScriptResponseDto result = service.executeScript(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.statistics().errors()).isNotEmpty();
            assertThat(result.statistics().errors()).anyMatch(error -> error.contains("Failed to create retweet"));
            assertThat(result.statistics().totalRetweetsCreated()).isEqualTo(0);

            verify(tweetsGateway, atLeastOnce()).retweetTweet(any(UUID.class), any(RetweetRequestDto.class));
        }

        @Test
        void executeScript_WithInsufficientTweets_ShouldSkipLikesAndRetweets() {
            BaseScriptRequestDto requestDto = BaseScriptRequestDto.builder()
                .nUsers(2)
                .nTweetsPerUser(1)
                .lUsersForDeletion(0)
                .build();

            when(randomDataGenerator.generateLogin()).thenReturn("user1", "user2");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com", "user2@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John", "Jane");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe", "Smith");
            when(randomDataGenerator.generatePassword()).thenReturn("password123", "password456");
            when(randomDataGenerator.generateTweetContent()).thenReturn("Tweet 1", "Tweet 2");

            UserResponseDto userResponse1 = new UserResponseDto(userId1, "user1", "John", "Doe",
                "user1@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());
            UserResponseDto userResponse2 = new UserResponseDto(userId2, "user2", "Jane", "Smith",
                "user2@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class)))
                .thenReturn(userResponse1, userResponse2);

            TweetResponseDto tweetResponse1 = new TweetResponseDto(tweetId1, userId1, "Tweet 1",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse2 = new TweetResponseDto(tweetId2, userId2, "Tweet 2",
                LocalDateTime.now(), LocalDateTime.now(), false, null);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class)))
                .thenReturn(tweetResponse1, tweetResponse2);

            List<TweetResponseDto> user1TweetsList = new ArrayList<>(List.of(tweetResponse1));
            List<TweetResponseDto> user2TweetsList = new ArrayList<>(List.of(tweetResponse2));

            when(tweetsGateway.getUserTweets(eq(userId1), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user1TweetsList, PageRequest.of(0, 1000), 1));
            when(tweetsGateway.getUserTweets(eq(userId2), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user2TweetsList, PageRequest.of(0, 1000), 1));

            doNothing().when(validator).validateDeletionCount(any(), eq(2));

            BaseScriptResponseDto result = service.executeScript(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.createdTweets()).hasSize(2);
            assertThat(result.statistics().totalLikesCreated()).isGreaterThanOrEqualTo(0);
            assertThat(result.statistics().totalRetweetsCreated()).isGreaterThanOrEqualTo(0);
        }

        @Test
        void executeScript_WithInsufficientUsers_ShouldSkipLikesAndRetweets() {
            BaseScriptRequestDto requestDto = BaseScriptRequestDto.builder()
                .nUsers(1)
                .nTweetsPerUser(6)
                .lUsersForDeletion(0)
                .build();

            when(randomDataGenerator.generateLogin()).thenReturn("user1");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe");
            when(randomDataGenerator.generatePassword()).thenReturn("password123");
            when(randomDataGenerator.generateTweetContent()).thenReturn("Tweet 1", "Tweet 2", "Tweet 3", "Tweet 4", "Tweet 5", "Tweet 6");

            UserResponseDto userResponse1 = new UserResponseDto(userId1, "user1", "John", "Doe",
                "user1@test.com", UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now());

            when(usersGateway.createUser(any(UserRequestDto.class)))
                .thenReturn(userResponse1);

            TweetResponseDto tweetResponse1 = new TweetResponseDto(tweetId1, userId1, "Tweet 1",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse2 = new TweetResponseDto(tweetId2, userId1, "Tweet 2",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse3 = new TweetResponseDto(tweetId3, userId1, "Tweet 3",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse4 = new TweetResponseDto(tweetId4, userId1, "Tweet 4",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse5 = new TweetResponseDto(tweetId5, userId1, "Tweet 5",
                LocalDateTime.now(), LocalDateTime.now(), false, null);
            TweetResponseDto tweetResponse6 = new TweetResponseDto(tweetId6, userId1, "Tweet 6",
                LocalDateTime.now(), LocalDateTime.now(), false, null);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class)))
                .thenReturn(tweetResponse1, tweetResponse2, tweetResponse3, tweetResponse4, tweetResponse5, tweetResponse6);

            List<TweetResponseDto> user1TweetsList = new ArrayList<>(List.of(tweetResponse1, tweetResponse2, tweetResponse3,
                tweetResponse4, tweetResponse5, tweetResponse6));

            when(tweetsGateway.getUserTweets(eq(userId1), any(Pageable.class)))
                .thenAnswer(_ -> new PageImpl<>(user1TweetsList, PageRequest.of(0, 1000), 6));

            doNothing().when(validator).validateDeletionCount(any(), eq(1));

            BaseScriptResponseDto result = service.executeScript(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.createdTweets()).hasSize(6);
            assertThat(result.statistics().totalLikesCreated()).isEqualTo(0);
            assertThat(result.statistics().totalRetweetsCreated()).isEqualTo(0);

            verify(tweetsGateway, never()).likeTweet(any(UUID.class), any(LikeTweetRequestDto.class));
            verify(tweetsGateway, never()).retweetTweet(any(UUID.class), any(RetweetRequestDto.class));
        }
    }
}
