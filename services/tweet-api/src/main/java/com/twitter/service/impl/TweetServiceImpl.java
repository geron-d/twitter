package com.twitter.service.impl;

import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import com.twitter.entity.Tweet;
import com.twitter.mapper.TweetMapper;
import com.twitter.repository.TweetRepository;
import com.twitter.service.TweetService;
import com.twitter.validation.TweetValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of TweetService interface.
 * <p>
 * Provides business logic for tweet operations including creation,
 * validation, and data transformation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TweetServiceImpl implements TweetService {

    private final TweetRepository tweetRepository;
    private final TweetMapper tweetMapper;
    private final TweetValidator tweetValidator;

    @Override
    @Transactional
    public TweetResponseDto createTweet(CreateTweetRequestDto requestDto) {
        tweetValidator.validateForCreate(requestDto);

        Tweet tweet = tweetMapper.toEntity(requestDto);
        Tweet savedTweet = tweetRepository.save(tweet);
        return tweetMapper.toResponseDto(savedTweet);
    }
}
