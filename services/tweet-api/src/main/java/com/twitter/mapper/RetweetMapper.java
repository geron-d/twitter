package com.twitter.mapper;

import com.twitter.common.dto.request.retweet.RetweetRequestDto;
import com.twitter.common.dto.response.retweet.RetweetResponseDto;
import com.twitter.entity.Retweet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

/**
 * MapStruct mapper for data transformation between Retweet entities and DTO objects.
 *
 * @author geron
 * @version 1.0
 */
@Mapper
public interface RetweetMapper {

    /**
     * Converts RetweetRequestDto and tweetId to Retweet entity.
     *
     * @param requestDto DTO containing userId and optional comment for the retweet
     * @param tweetId    the unique identifier of the tweet being retweeted
     * @return Retweet entity without service-managed fields (id, createdAt)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tweetId", source = "tweetId")
    @Mapping(target = "userId", source = "requestDto.userId")
    @Mapping(target = "comment", source = "requestDto.comment")
    Retweet toRetweet(RetweetRequestDto requestDto, UUID tweetId);

    /**
     * Converts Retweet entity to RetweetResponseDto.
     *
     * @param retweet Retweet entity from database
     * @return DTO containing retweet data for client response
     */
    RetweetResponseDto toRetweetResponseDto(Retweet retweet);
}