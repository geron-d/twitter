package com.twitter.service;

import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.dto.external.*;
import com.twitter.dto.request.GenerateUsersAndTweetsRequestDto;
import com.twitter.dto.response.GenerateUsersAndTweetsResponseDto;
import com.twitter.gateway.TweetsGateway;
import com.twitter.gateway.UsersGateway;
import com.twitter.util.RandomDataGenerator;
import com.twitter.validation.GenerateUsersAndTweetsValidator;
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
class GenerateUsersAndTweetsServiceImplTest {

    @Mock
    private UsersGateway usersGateway;

    @Mock
    private TweetsGateway tweetsGateway;

    @Mock
    private RandomDataGenerator randomDataGenerator;

    @Mock
    private GenerateUsersAndTweetsValidator validator;

    @InjectMocks
    private GenerateUsersAndTweetsServiceImpl service;

    @Nested
    class ExecuteScriptTests {

        private GenerateUsersAndTweetsRequestDto requestDto;
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

            requestDto = GenerateUsersAndTweetsRequestDto.builder()
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

            GenerateUsersAndTweetsResponseDto result = service.executeScript(requestDto);

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

            GenerateUsersAndTweetsResponseDto result = service.executeScript(requestDto);

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

            GenerateUsersAndTweetsRequestDto requestWithoutDeletion = GenerateUsersAndTweetsRequestDto.builder()
                .nUsers(1)
                .nTweetsPerUser(2)
                .lUsersForDeletion(0)
                .build();

            GenerateUsersAndTweetsResponseDto result = service.executeScript(requestWithoutDeletion);

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

            GenerateUsersAndTweetsRequestDto requestWithoutDeletion = GenerateUsersAndTweetsRequestDto.builder()
                .nUsers(1)
                .nTweetsPerUser(1)
                .lUsersForDeletion(0)
                .build();

            GenerateUsersAndTweetsResponseDto result = service.executeScript(requestWithoutDeletion);

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

            GenerateUsersAndTweetsRequestDto requestWithInvalidDeletion = GenerateUsersAndTweetsRequestDto.builder()
                .nUsers(1)
                .nTweetsPerUser(1)
                .lUsersForDeletion(2)
                .build();

            GenerateUsersAndTweetsResponseDto result = service.executeScript(requestWithInvalidDeletion);

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

            UserResponseDto userResponse1 = new UserResponseDto(
                userId1, "user1", "John", "Doe", "user1@test.com",
                UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now()
            );
            UserResponseDto userResponse2 = new UserResponseDto(
                userId2, "user2", "Jane", "Smith", "user2@test.com",
                UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now()
            );

            when(usersGateway.createUser(any(UserRequestDto.class)))
                .thenReturn(userResponse1, userResponse2);

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class)))
                .thenThrow(new RuntimeException("Tweet creation failed"));

            Page<TweetResponseDto> emptyTweetsPage = new PageImpl<>(
                List.of(), PageRequest.of(0, 1000), 0
            );

            when(tweetsGateway.getUserTweets(any(UUID.class), any(Pageable.class)))
                .thenReturn(emptyTweetsPage);

            doNothing().when(validator).validateDeletionCount(any(), eq(0));

            GenerateUsersAndTweetsRequestDto requestWithoutDeletion = GenerateUsersAndTweetsRequestDto.builder()
                .nUsers(2)
                .nTweetsPerUser(1)
                .lUsersForDeletion(0)
                .build();

            GenerateUsersAndTweetsResponseDto result = service.executeScript(requestWithoutDeletion);

            assertThat(result).isNotNull();
            assertThat(result.createdUsers()).hasSize(2);
            assertThat(result.createdTweets()).isEmpty();
            assertThat(result.statistics().usersWithTweets()).isEqualTo(0);
            assertThat(result.statistics().usersWithoutTweets()).isEqualTo(2);

            verify(usersGateway, times(2)).createUser(any(UserRequestDto.class));
        }

        @Test
        void executeScript_WhenDeletionFails_ShouldContinueAndAddError() {
            when(randomDataGenerator.generateLogin()).thenReturn("user1");
            when(randomDataGenerator.generateEmail()).thenReturn("user1@test.com");
            when(randomDataGenerator.generateFirstName()).thenReturn("John");
            when(randomDataGenerator.generateLastName()).thenReturn("Doe");
            when(randomDataGenerator.generatePassword()).thenReturn("password123");
            when(randomDataGenerator.generateTweetContent()).thenReturn("Tweet 1");

            UserResponseDto userResponse1 = new UserResponseDto(
                userId1, "user1", "John", "Doe", "user1@test.com",
                UserStatus.ACTIVE, UserRole.USER, LocalDateTime.now()
            );

            when(usersGateway.createUser(any(UserRequestDto.class))).thenReturn(userResponse1);

            TweetResponseDto tweetResponse1 = new TweetResponseDto(
                tweetId1, userId1, "Tweet 1", LocalDateTime.now(), LocalDateTime.now(), false, null
            );

            when(tweetsGateway.createTweet(any(CreateTweetRequestDto.class))).thenReturn(tweetResponse1);

            Page<TweetResponseDto> user1TweetsPage = new PageImpl<>(
                List.of(tweetResponse1), PageRequest.of(0, 1000), 1
            );

            when(tweetsGateway.getUserTweets(eq(userId1), any(Pageable.class)))
                .thenReturn(user1TweetsPage)
                .thenReturn(user1TweetsPage);

            doNothing().when(validator).validateDeletionCount(any(), eq(1));

            doThrow(new RuntimeException("Deletion failed"))
                .when(tweetsGateway).deleteTweet(any(UUID.class), any(DeleteTweetRequestDto.class));

            GenerateUsersAndTweetsResponseDto result = service.executeScript(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.deletedTweets()).isEmpty();
            assertThat(result.statistics().errors()).isNotEmpty();
            assertThat(result.statistics().errors().get(0)).contains("Failed to delete tweet");

            verify(tweetsGateway, times(1)).deleteTweet(any(UUID.class), any(DeleteTweetRequestDto.class));
        }
    }
}
