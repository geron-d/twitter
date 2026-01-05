package com.twitter.service;

import com.twitter.dto.request.RetweetRequestDto;
import com.twitter.dto.response.RetweetResponseDto;
import com.twitter.entity.Retweet;
import com.twitter.entity.Tweet;
import com.twitter.mapper.RetweetMapper;
import com.twitter.repository.RetweetRepository;
import com.twitter.repository.TweetRepository;
import com.twitter.validation.RetweetValidator;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetweetServiceImplTest {

    @Mock
    private RetweetRepository retweetRepository;

    @Mock
    private RetweetMapper retweetMapper;

    @Mock
    private RetweetValidator retweetValidator;

    @Mock
    private TweetRepository tweetRepository;

    @InjectMocks
    private RetweetServiceImpl retweetService;

    @Nested
    class RetweetTweetTests {

        private UUID testTweetId;
        private UUID testUserId;
        private UUID testAuthorId;
        private RetweetRequestDto requestDto;
        private RetweetRequestDto requestDtoWithComment;
        private Retweet mappedRetweet;
        private Retweet savedRetweet;
        private Tweet existingTweet;
        private RetweetResponseDto responseDto;
        private RetweetResponseDto responseDtoWithComment;

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

            mappedRetweet = Retweet.builder()
                .tweetId(testTweetId)
                .userId(testUserId)
                .comment(null)
                .build();

            UUID retweetId = UUID.fromString("987e6543-e21b-43d2-b654-321987654321");
            savedRetweet = Retweet.builder()
                .id(retweetId)
                .tweetId(testTweetId)
                .userId(testUserId)
                .comment(null)
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();

            existingTweet = Tweet.builder()
                .id(testTweetId)
                .userId(testAuthorId)
                .content("Test tweet content")
                .retweetsCount(0)
                .createdAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 27, 10, 0, 0))
                .build();

            responseDto = RetweetResponseDto.builder()
                .id(retweetId)
                .tweetId(testTweetId)
                .userId(testUserId)
                .comment(null)
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();

            responseDtoWithComment = RetweetResponseDto.builder()
                .id(retweetId)
                .tweetId(testTweetId)
                .userId(testUserId)
                .comment("Great tweet!")
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();
        }

        @Test
        void retweetTweet_WithValidData_ShouldReturnRetweetResponseDto() {
            doNothing().when(retweetValidator).validateForRetweet(testTweetId, requestDto);
            when(retweetMapper.toRetweet(requestDto, testTweetId)).thenReturn(mappedRetweet);
            when(retweetRepository.saveAndFlush(mappedRetweet)).thenReturn(savedRetweet);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class)))
                .thenAnswer(invocation -> invocation.<Tweet>getArgument(0));
            when(retweetMapper.toRetweetResponseDto(savedRetweet)).thenReturn(responseDto);

            RetweetResponseDto result = retweetService.retweetTweet(testTweetId, requestDto);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(savedRetweet.getId());
            assertThat(result.tweetId()).isEqualTo(testTweetId);
            assertThat(result.userId()).isEqualTo(testUserId);
            assertThat(result.comment()).isNull();
            assertThat(result.createdAt()).isEqualTo(savedRetweet.getCreatedAt());
        }

        @Test
        void retweetTweet_WithValidDataAndComment_ShouldReturnRetweetResponseDtoWithComment() {
            Retweet mappedRetweetWithComment = Retweet.builder()
                .tweetId(testTweetId)
                .userId(testUserId)
                .comment("Great tweet!")
                .build();

            Retweet savedRetweetWithComment = Retweet.builder()
                .id(savedRetweet.getId())
                .tweetId(testTweetId)
                .userId(testUserId)
                .comment("Great tweet!")
                .createdAt(LocalDateTime.of(2025, 1, 27, 15, 30, 0))
                .build();

            doNothing().when(retweetValidator).validateForRetweet(testTweetId, requestDtoWithComment);
            when(retweetMapper.toRetweet(requestDtoWithComment, testTweetId)).thenReturn(mappedRetweetWithComment);
            when(retweetRepository.saveAndFlush(mappedRetweetWithComment)).thenReturn(savedRetweetWithComment);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class)))
                .thenAnswer(invocation -> invocation.<Tweet>getArgument(0));
            when(retweetMapper.toRetweetResponseDto(savedRetweetWithComment)).thenReturn(responseDtoWithComment);

            RetweetResponseDto result = retweetService.retweetTweet(testTweetId, requestDtoWithComment);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(savedRetweetWithComment.getId());
            assertThat(result.tweetId()).isEqualTo(testTweetId);
            assertThat(result.userId()).isEqualTo(testUserId);
            assertThat(result.comment()).isEqualTo("Great tweet!");
            assertThat(result.createdAt()).isEqualTo(savedRetweetWithComment.getCreatedAt());
        }

        @Test
        void retweetTweet_WithValidData_ShouldCallEachDependencyExactlyOnce() {
            doNothing().when(retweetValidator).validateForRetweet(testTweetId, requestDto);
            when(retweetMapper.toRetweet(requestDto, testTweetId)).thenReturn(mappedRetweet);
            when(retweetRepository.saveAndFlush(mappedRetweet)).thenReturn(savedRetweet);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class)))
                .thenAnswer(invocation -> invocation.<Tweet>getArgument(0));
            when(retweetMapper.toRetweetResponseDto(savedRetweet)).thenReturn(responseDto);

            retweetService.retweetTweet(testTweetId, requestDto);

            verify(retweetValidator, times(1)).validateForRetweet(eq(testTweetId), eq(requestDto));
            verify(retweetMapper, times(1)).toRetweet(eq(requestDto), eq(testTweetId));
            verify(retweetRepository, times(1)).saveAndFlush(eq(mappedRetweet));
            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(eq(testTweetId));
            verify(tweetRepository, times(1)).saveAndFlush(any(Tweet.class));
            verify(retweetMapper, times(1)).toRetweetResponseDto(eq(savedRetweet));
        }

        @Test
        void retweetTweet_WithValidData_ShouldIncrementRetweetsCount() {
            doNothing().when(retweetValidator).validateForRetweet(testTweetId, requestDto);
            when(retweetMapper.toRetweet(requestDto, testTweetId)).thenReturn(mappedRetweet);
            when(retweetRepository.saveAndFlush(mappedRetweet)).thenReturn(savedRetweet);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.of(existingTweet));
            when(tweetRepository.saveAndFlush(any(Tweet.class))).thenAnswer(invocation -> {
                Tweet tweet = invocation.getArgument(0);
                assertThat(tweet.getRetweetsCount()).isEqualTo(1);
                return tweet;
            });
            when(retweetMapper.toRetweetResponseDto(savedRetweet)).thenReturn(responseDto);

            retweetService.retweetTweet(testTweetId, requestDto);

            verify(tweetRepository, times(1)).saveAndFlush(argThat(tweet ->
                tweet.getRetweetsCount() == 1
            ));
        }

        @Test
        void retweetTweet_WhenValidationFails_ShouldThrowException() {
            doThrow(new com.twitter.common.exception.validation.BusinessRuleValidationException("TWEET_NOT_FOUND", testTweetId))
                .when(retweetValidator).validateForRetweet(testTweetId, requestDto);

            assertThatThrownBy(() -> retweetService.retweetTweet(testTweetId, requestDto))
                .isInstanceOf(com.twitter.common.exception.validation.BusinessRuleValidationException.class)
                .satisfies(exception -> {
                    com.twitter.common.exception.validation.BusinessRuleValidationException ex =
                        (com.twitter.common.exception.validation.BusinessRuleValidationException) exception;
                    assertThat(ex.getRuleName()).isEqualTo("TWEET_NOT_FOUND");
                });

            verify(retweetValidator, times(1)).validateForRetweet(eq(testTweetId), eq(requestDto));
            verify(retweetMapper, never()).toRetweet(any(), any());
            verify(retweetRepository, never()).saveAndFlush(any());
            verify(tweetRepository, never()).findByIdAndIsDeletedFalse(any());
        }

        @Test
        void retweetTweet_WhenTweetNotFoundAfterValidation_ShouldThrowIllegalStateException() {
            doNothing().when(retweetValidator).validateForRetweet(testTweetId, requestDto);
            when(retweetMapper.toRetweet(requestDto, testTweetId)).thenReturn(mappedRetweet);
            when(retweetRepository.saveAndFlush(mappedRetweet)).thenReturn(savedRetweet);
            when(tweetRepository.findByIdAndIsDeletedFalse(testTweetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> retweetService.retweetTweet(testTweetId, requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tweet not found after validation");

            verify(retweetValidator, times(1)).validateForRetweet(eq(testTweetId), eq(requestDto));
            verify(retweetMapper, times(1)).toRetweet(eq(requestDto), eq(testTweetId));
            verify(retweetRepository, times(1)).saveAndFlush(eq(mappedRetweet));
            verify(tweetRepository, times(1)).findByIdAndIsDeletedFalse(eq(testTweetId));
            verify(tweetRepository, never()).saveAndFlush(any());
            verify(retweetMapper, never()).toRetweetResponseDto(any());
        }

        @Test
        void retweetTweet_WhenUniquenessValidationFails_ShouldThrowUniquenessValidationException() {
            doThrow(new com.twitter.common.exception.validation.UniquenessValidationException("retweet", "tweet " + testTweetId + " and user " + testUserId))
                .when(retweetValidator).validateForRetweet(testTweetId, requestDto);

            assertThatThrownBy(() -> retweetService.retweetTweet(testTweetId, requestDto))
                .isInstanceOf(com.twitter.common.exception.validation.UniquenessValidationException.class);

            verify(retweetValidator, times(1)).validateForRetweet(eq(testTweetId), eq(requestDto));
            verify(retweetMapper, never()).toRetweet(any(), any());
            verify(retweetRepository, never()).saveAndFlush(any());
            verify(tweetRepository, never()).findByIdAndIsDeletedFalse(any());
        }
    }
}
