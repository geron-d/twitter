package com.twitter.service;

import com.twitter.dto.request.RetweetRequestDto;
import com.twitter.dto.response.RetweetResponseDto;
import com.twitter.entity.Retweet;
import com.twitter.entity.Tweet;
import com.twitter.mapper.RetweetMapper;
import com.twitter.repository.RetweetRepository;
import com.twitter.repository.TweetRepository;
import com.twitter.validation.RetweetValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
