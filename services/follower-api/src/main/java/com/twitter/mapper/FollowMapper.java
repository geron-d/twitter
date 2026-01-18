package com.twitter.mapper;

import com.twitter.common.dto.request.follow.FollowRequestDto;
import com.twitter.common.dto.response.follow.FollowResponseDto;
import com.twitter.dto.response.FollowStatsResponseDto;
import com.twitter.dto.response.FollowStatusResponseDto;
import com.twitter.dto.response.FollowerResponseDto;
import com.twitter.common.dto.response.follow.FollowingResponseDto;
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
     * <p>
     * This method transforms request DTO data into a Follow entity, ignoring
     * service-managed fields (id, createdAt) which are set automatically
     * by the service layer and database.
     *
     * @param dto DTO containing follow relationship data for creation
     * @return Follow entity without service-managed fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Follow toFollow(FollowRequestDto dto);

    /**
     * Converts Follow entity to FollowResponseDto.
     * <p>
     * This method transforms a Follow entity into a response DTO containing
     * all follow relationship information including id, followerId, followingId,
     * and createdAt timestamp.
     *
     * @param follow Follow entity from database
     * @return DTO containing follow relationship data for client response
     */
    FollowResponseDto toFollowResponseDto(Follow follow);

    /**
     * Converts Follow entity and user login to FollowerResponseDto.
     * <p>
     * This method transforms a Follow entity into a FollowerResponseDto for
     * displaying follower information in lists. The login parameter should be
     * obtained from the users-api service.
     *
     * @param follow Follow entity from database
     * @param login  login of the follower user (obtained from users-api)
     * @return DTO containing follower information (id, login, createdAt)
     */
    @Mapping(target = "id", source = "follow.followerId")
    @Mapping(target = "login", source = "login")
    @Mapping(target = "createdAt", source = "follow.createdAt")
    FollowerResponseDto toFollowerResponseDto(Follow follow, String login);

    /**
     * Converts Follow entity and user login to FollowingResponseDto.
     * <p>
     * This method transforms a Follow entity into a FollowingResponseDto for
     * displaying following information in lists. The login parameter should be
     * obtained from the users-api service.
     *
     * @param follow Follow entity from database
     * @param login  login of the following user (obtained from users-api)
     * @return DTO containing following information (id, login, createdAt)
     */
    @Mapping(target = "id", source = "follow.followingId")
    @Mapping(target = "login", source = "login")
    @Mapping(target = "createdAt", source = "follow.createdAt")
    FollowingResponseDto toFollowingResponseDto(Follow follow, String login);

    /**
     * Converts Follow entity to FollowStatusResponseDto.
     * <p>
     * This method transforms a Follow entity into a FollowStatusResponseDto for
     * displaying follow relationship status. The method sets isFollowing=true
     * and includes the creation timestamp.
     *
     * @param follow Follow entity from database
     * @return DTO containing follow relationship status (isFollowing=true, createdAt)
     */
    @Mapping(target = "isFollowing", constant = "true")
    @Mapping(target = "createdAt", source = "follow.createdAt")
    FollowStatusResponseDto toFollowStatusResponseDto(Follow follow);

    /**
     * Converts follower and following counts to FollowStatsResponseDto.
     * <p>
     * This method transforms count values into a FollowStatsResponseDto for
     * displaying follow statistics. The counts are obtained from database
     * queries using countByFollowingId and countByFollowerId.
     *
     * @param followersCount total number of users following this user
     * @param followingCount total number of users this user is following
     * @return DTO containing follow statistics (followersCount, followingCount)
     */
    @Mapping(target = "followersCount", source = "followersCount")
    @Mapping(target = "followingCount", source = "followingCount")
    FollowStatsResponseDto toFollowStatsResponseDto(long followersCount, long followingCount);
}