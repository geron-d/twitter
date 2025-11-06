package com.twitter.service;

import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import com.twitter.entity.Tweet;
import com.twitter.mapper.TweetMapper;
import com.twitter.repository.TweetRepository;
import com.twitter.validation.TweetValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * Creates a new tweet from the provided request data.
     * <p>
     * This method performs the following validations:
     * 1. Validates the request data using TweetValidator
     * 2. Checks if the user exists via users-api integration
     *
     * @param requestDto the tweet creation request containing content and userId
     * @return TweetResponseDto containing the created tweet data
     * @throws com.twitter.common.exception.validation.FormatValidationException if content validation fails
     * @throws com.twitter.common.exception.validation.BusinessRuleValidationException if user doesn't exist
     */
    @Override
    @Transactional
    public TweetResponseDto createTweet(CreateTweetRequestDto requestDto) {
        tweetValidator.validateForCreate(requestDto);

        Tweet tweet = tweetMapper.toEntity(requestDto);
        Tweet savedTweet = tweetRepository.saveAndFlush(tweet);
        return tweetMapper.toResponseDto(savedTweet);
    }
}
