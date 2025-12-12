package com.twitter.service;

import com.twitter.common.dto.request.CreateTweetRequestDto;
import com.twitter.common.dto.request.DeleteTweetRequestDto;
import com.twitter.common.dto.response.TweetResponseDto;
import com.twitter.dto.request.UpdateTweetRequestDto;
import com.twitter.entity.Tweet;
import com.twitter.mapper.TweetMapper;
import com.twitter.repository.TweetRepository;
import com.twitter.validation.TweetValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the tweet management service.
 * <p>
 * This service provides business logic for tweet operations, including creation,
 * validation, and data transformation. It handles data validation, user existence
 * checks via users-api integration, and business rule enforcement.
 *
 * @author geron
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TweetServiceImpl implements TweetService {

    private final TweetRepository tweetRepository;
    private final TweetMapper tweetMapper;
    private final TweetValidator tweetValidator;

    /**
     * @see TweetService#createTweet
     */
    @Override
    @Transactional
    public TweetResponseDto createTweet(CreateTweetRequestDto requestDto) {
        tweetValidator.validateForCreate(requestDto);

        Tweet tweet = tweetMapper.toEntity(requestDto);
        Tweet savedTweet = tweetRepository.saveAndFlush(tweet);
        return tweetMapper.toResponseDto(savedTweet);
    }

    /**
     * @see TweetService#getTweetById
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<TweetResponseDto> getTweetById(UUID tweetId) {
        return tweetRepository.findByIdAndIsDeletedFalse(tweetId)
            .map(tweetMapper::toResponseDto);
    }

    /**
     * @see TweetService#updateTweet
     */
    @Override
    @Transactional
    public TweetResponseDto updateTweet(UUID tweetId, UpdateTweetRequestDto requestDto) {
        tweetValidator.validateForUpdate(tweetId, requestDto);

        Tweet tweet = tweetRepository.findById(tweetId)
            .orElseThrow(() -> new IllegalStateException("Tweet not found after validation"));

        tweetMapper.updateTweetFromUpdateDto(requestDto, tweet);
        Tweet updatedTweet = tweetRepository.saveAndFlush(tweet);
        return tweetMapper.toResponseDto(updatedTweet);
    }

    /**
     * @see TweetService#deleteTweet
     */
    @Override
    @Transactional
    public void deleteTweet(UUID tweetId, DeleteTweetRequestDto requestDto) {
        tweetValidator.validateForDelete(tweetId, requestDto);

        Tweet tweet = tweetRepository.findById(tweetId)
            .orElseThrow(() -> new IllegalStateException("Tweet not found after validation"));

        tweet.softDelete();
        tweetRepository.saveAndFlush(tweet);
    }

    /**
     * @see TweetService#getUserTweets
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TweetResponseDto> getUserTweets(UUID userId, Pageable pageable) {
        return tweetRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId, pageable)
            .map(tweetMapper::toResponseDto);
    }
}
