package com.twitter.service;

import com.twitter.dto.filter.FollowerFilter;
import com.twitter.dto.request.FollowRequestDto;
import com.twitter.dto.response.FollowResponseDto;
import com.twitter.dto.response.FollowerResponseDto;
import com.twitter.entity.Follow;
import com.twitter.gateway.UserGateway;
import com.twitter.mapper.FollowMapper;
import com.twitter.repository.FollowRepository;
import com.twitter.validation.FollowValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the follow relationship management service.
 * <p>
 * This service provides business logic for follow relationship operations, including
 * creation, validation, and data transformation. It handles data validation, user
 * existence checks via users-api integration, and business rule enforcement.
 *
 * @author geron
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final FollowMapper followMapper;
    private final FollowValidator followValidator;
    private final UserGateway userGateway;

    /**
     * @see FollowService#follow
     */
    @Override
    @Transactional
    public FollowResponseDto follow(FollowRequestDto request) {
        log.debug("Creating follow relationship: followerId={}, followingId={}",
            request.followerId(), request.followingId());

        followValidator.validateForFollow(request);

        Follow follow = followMapper.toFollow(request);
        Follow savedFollow = followRepository.saveAndFlush(follow);

        log.info("Successfully created follow relationship: id={}, followerId={}, followingId={}",
            savedFollow.getId(), savedFollow.getFollowerId(), savedFollow.getFollowingId());

        return followMapper.toFollowResponseDto(savedFollow);
    }

    /**
     * @see FollowService#unfollow
     */
    @Override
    @Transactional
    public void unfollow(UUID followerId, UUID followingId) {
        log.debug("Removing follow relationship: followerId={}, followingId={}", followerId, followingId);

        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
            .orElseThrow(() -> {
                log.warn("Follow relationship not found: followerId={}, followingId={}", followerId, followingId);
                return new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Follow relationship between followerId=%s and followingId=%s does not exist",
                        followerId, followingId)
                );
            });

        followRepository.delete(follow);

        log.info("Successfully removed follow relationship: id={}, followerId={}, followingId={}",
            follow.getId(), followerId, followingId);
    }

    /**
     * @see FollowService#getFollowers
     */
    @Override
    @Transactional(readOnly = true)
    public PagedModel<FollowerResponseDto> getFollowers(UUID userId, FollowerFilter filter, Pageable pageable) {
        log.debug("Retrieving followers for user: userId={}, filter={}, page={}, size={}",
            userId, filter, pageable.getPageNumber(), pageable.getPageSize());

        Pageable sortedPageable = pageable.getSort().isSorted()
            ? pageable
            : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Follow> followsPage = followRepository.findByFollowingId(userId, sortedPageable);

        List<FollowerResponseDto> followers = followsPage.getContent().stream()
            .map(follow -> {
                String login = userGateway.getUserLogin(follow.getFollowerId())
                    .orElse("unknown");
                return followMapper.toFollowerResponseDto(follow, login);
            })
            .filter(follower -> {
                if (filter != null && StringUtils.hasText(filter.login())) {
                    String filterLogin = filter.login().toLowerCase();
                    String followerLogin = follower.login() != null ? follower.login().toLowerCase() : "";
                    return followerLogin.contains(filterLogin);
                }
                return true;
            })
            .collect(Collectors.toList());

        Page<FollowerResponseDto> filteredPage = new PageImpl<>(followers, followsPage.getPageable(),
            followsPage.getTotalElements());

        log.info("Retrieved {} followers for user: userId={}, totalElements={}",
            followers.size(), userId, filteredPage.getTotalElements());

        return new PagedModel<>(filteredPage);
    }
}

