package com.twitter.service;

import com.twitter.common.dto.request.retweet.RetweetRequestDto;
import com.twitter.common.dto.response.retweet.RetweetResponseDto;
import com.twitter.entity.Retweet;
import com.twitter.entity.Tweet;
import com.twitter.mapper.RetweetMapper;
import com.twitter.repository.RetweetRepository;
import com.twitter.repository.TweetRepository;
import com.twitter.validation.RetweetValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of the retweet management service.
 * <p>
 * This service provides business logic for retweet operations, including validation
 * and data transformation. It handles data validation, user existence checks
 * via users-api integration, and business rule enforcement for retweets.
 *
 * @author geron
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RetweetServiceImpl implements RetweetService {

    private final RetweetRepository retweetRepository;
    private final RetweetMapper retweetMapper;
    private final RetweetValidator retweetValidator;
    private final TweetRepository tweetRepository;

    /**
     * @see RetweetService#retweetTweet
     */
    @Override
    @Transactional
    public RetweetResponseDto retweetTweet(UUID tweetId, RetweetRequestDto requestDto) {
        retweetValidator.validateForRetweet(tweetId, requestDto);

        Retweet retweet = retweetMapper.toRetweet(requestDto, tweetId);
        Retweet savedRetweet = retweetRepository.saveAndFlush(retweet);

        Tweet tweet = tweetRepository.findByIdAndIsDeletedFalse(tweetId)
            .orElseThrow(() -> new IllegalStateException("Tweet not found after validation"));
        tweet.incrementRetweetsCount();
        tweetRepository.saveAndFlush(tweet);

        return retweetMapper.toRetweetResponseDto(savedRetweet);
    }

    /**
     * @see RetweetService#removeRetweet
     */
    @Override
    @Transactional
    public void removeRetweet(UUID tweetId, RetweetRequestDto requestDto) {
        retweetValidator.validateForRemoveRetweet(tweetId, requestDto);

        Retweet retweet = retweetRepository.findByTweetIdAndUserId(tweetId, requestDto.userId())
            .orElseThrow(() -> new IllegalStateException("Retweet not found after validation"));
        retweetRepository.delete(retweet);

        Tweet tweet = tweetRepository.findByIdAndIsDeletedFalse(tweetId)
            .orElseThrow(() -> new IllegalStateException("Tweet not found after validation"));
        tweet.decrementRetweetsCount();
        tweetRepository.saveAndFlush(tweet);
    }

    /**
     * @see RetweetService#getRetweetsByTweetId
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RetweetResponseDto> getRetweetsByTweetId(UUID tweetId, Pageable pageable) {
        retweetValidator.validateTweetExists(tweetId);

        log.debug("Retrieving retweets for tweet {} with pagination: page={}, size={}", tweetId, pageable.getPageNumber(), pageable.getPageSize());
        Page<Retweet> retweets = retweetRepository.findByTweetIdOrderByCreatedAtDesc(tweetId, pageable);
        return retweets.map(retweetMapper::toRetweetResponseDto);
    }
}