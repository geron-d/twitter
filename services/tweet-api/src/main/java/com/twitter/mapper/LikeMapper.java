package com.twitter.mapper;

import com.twitter.common.dto.request.like.LikeTweetRequestDto;
import com.twitter.common.dto.response.like.LikeResponseDto;
import com.twitter.entity.Like;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

/**
 * MapStruct mapper for data transformation between Like entities and DTO objects.
 *
 * @author geron
 * @version 1.0
 */
@Mapper
public interface LikeMapper {

    /**
     * Converts LikeTweetRequestDto and tweetId to Like entity.
     *
     * @param requestDto DTO for the like
     * @param tweetId    the unique identifier of the tweet being liked
     * @return Like entity without service-managed fields (id, createdAt)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tweetId", source = "tweetId")
    @Mapping(target = "userId", source = "requestDto.userId")
    Like toLike(LikeTweetRequestDto requestDto, UUID tweetId);

    /**
     * Converts Like entity to LikeResponseDto.
     *
     * @param like Like entity
     * @return DTO containing like data for client response
     */
    LikeResponseDto toLikeResponseDto(Like like);
}