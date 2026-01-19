package com.twitter.mapper;

import com.twitter.common.dto.request.follow.FollowRequestDto;
import com.twitter.common.dto.response.follow.FollowResponseDto;
import com.twitter.common.dto.response.follow.FollowingResponseDto;
import com.twitter.dto.response.FollowStatsResponseDto;
import com.twitter.dto.response.FollowStatusResponseDto;
import com.twitter.dto.response.FollowerResponseDto;
import com.twitter.entity.Follow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for data transformation between Follow entities and DTO objects.
 *
 * @author geron
 * @version 1.0
 */
@Mapper
public interface FollowMapper {

    /**
     * Converts FollowRequestDto to Follow entity.
     *
     * @param dto DTO containing follow relationship data for creation
     * @return Follow entity without service-managed fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Follow toFollow(FollowRequestDto dto);

    /**
     * Converts Follow entity to FollowResponseDto.
     *
     * @param follow Follow entity from database
     * @return DTO containing follow relationship data for client response
     */
    FollowResponseDto toFollowResponseDto(Follow follow);

    /**
     * Converts Follow entity and user login to FollowerResponseDto.
     *
     * @param follow Follow entity from database
     * @param login  login of the follower user
     * @return DTO containing follower information
     */
    @Mapping(target = "id", source = "follow.followerId")
    @Mapping(target = "login", source = "login")
    @Mapping(target = "createdAt", source = "follow.createdAt")
    FollowerResponseDto toFollowerResponseDto(Follow follow, String login);

    /**
     * Converts Follow entity and user login to FollowingResponseDto.
     *
     * @param follow Follow entity from database
     * @param login  login of the following user
     * @return DTO containing following information
     */
    @Mapping(target = "id", source = "follow.followingId")
    @Mapping(target = "login", source = "login")
    @Mapping(target = "createdAt", source = "follow.createdAt")
    FollowingResponseDto toFollowingResponseDto(Follow follow, String login);

    /**
     * Converts Follow entity to FollowStatusResponseDto.
     *
     * @param follow Follow entity from database
     * @return DTO containing follow relationship status
     */
    @Mapping(target = "isFollowing", constant = "true")
    @Mapping(target = "createdAt", source = "follow.createdAt")
    FollowStatusResponseDto toFollowStatusResponseDto(Follow follow);

    /**
     * Converts follower and following counts to FollowStatsResponseDto.
     *
     * @param followersCount total number of users following this user
     * @param followingCount total number of users this user is following
     * @return DTO containing follow statistics
     */
    @Mapping(target = "followersCount", source = "followersCount")
    @Mapping(target = "followingCount", source = "followingCount")
    FollowStatsResponseDto toFollowStatsResponseDto(long followersCount, long followingCount);
}