package com.twitter.service;

import com.twitter.common.dto.request.LikeTweetRequestDto;
import com.twitter.common.dto.response.LikeResponseDto;
import com.twitter.entity.Like;
import com.twitter.entity.Tweet;
import com.twitter.mapper.LikeMapper;
import com.twitter.repository.LikeRepository;
import com.twitter.repository.TweetRepository;
import com.twitter.validation.LikeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of the like management service.
 * <p>
 * This service provides business logic for like operations, including validation
 * and data transformation. It handles data validation, user existence checks
 * via users-api integration, and business rule enforcement for likes.
 *
 * @author geron
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final LikeMapper likeMapper;
    private final LikeValidator likeValidator;
    private final TweetRepository tweetRepository;

    /**
     * @see LikeService#likeTweet
     */
    @Override
    @Transactional
    public LikeResponseDto likeTweet(UUID tweetId, LikeTweetRequestDto requestDto) {
        likeValidator.validateForLike(tweetId, requestDto);

        Like like = likeMapper.toLike(requestDto, tweetId);
        Like savedLike = likeRepository.saveAndFlush(like);

        Tweet tweet = tweetRepository.findByIdAndIsDeletedFalse(tweetId)
            .orElseThrow(() -> new IllegalStateException("Tweet not found after validation"));
        tweet.incrementLikesCount();
        tweetRepository.saveAndFlush(tweet);

        return likeMapper.toLikeResponseDto(savedLike);
    }

    /**
     * @see LikeService#removeLike
     */
    @Override
    @Transactional
    public void removeLike(UUID tweetId, LikeTweetRequestDto requestDto) {
        likeValidator.validateForUnlike(tweetId, requestDto);

        Like like = likeRepository.findByTweetIdAndUserId(tweetId, requestDto.userId())
            .orElseThrow(() -> new IllegalStateException("Like not found after validation"));
        likeRepository.delete(like);

        Tweet tweet = tweetRepository.findByIdAndIsDeletedFalse(tweetId)
            .orElseThrow(() -> new IllegalStateException("Tweet not found after validation"));
        tweet.decrementLikesCount();
        tweetRepository.saveAndFlush(tweet);
    }

    /**
     * @see LikeService#getLikesByTweetId
     */
    @Override
    @Transactional(readOnly = true)
    public Page<LikeResponseDto> getLikesByTweetId(UUID tweetId, Pageable pageable) {
        likeValidator.validateTweetExists(tweetId);

        log.debug("Retrieving likes for tweet {} with pagination: page={}, size={}", tweetId, pageable.getPageNumber(), pageable.getPageSize());
        Page<Like> likes = likeRepository.findByTweetIdOrderByCreatedAtDesc(tweetId, pageable);
        return likes.map(likeMapper::toLikeResponseDto);
    }
}