package com.twitter.mapper;

import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import com.twitter.entity.Tweet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for data transformation between Tweet entities and DTO objects.
 *
 * @author geron
 * @version 1.0
 */
@Mapper
public interface TweetMapper {

    /**
     * Converts CreateTweetRequestDto to Tweet entity.
     *
     * @param requestDto DTO containing tweet data for creation
     * @return Tweet entity without service-managed fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Tweet toEntity(CreateTweetRequestDto requestDto);

    /**
     * Converts Tweet entity to TweetResponseDto.
     *
     * @param tweet Tweet entity from database
     * @return DTO containing tweet data for client response
     */
    TweetResponseDto toResponseDto(Tweet tweet);
}
