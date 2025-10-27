package com.twitter.mapper;

import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import com.twitter.entity.Tweet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between Tweet DTOs and Entity.
 */
@Mapper
public interface TweetMapper {

    /**
     * Maps CreateTweetRequestDto to Tweet entity.
     * Used when creating a new tweet from API request.
     *
     * @param requestDto the request DTO containing tweet data
     * @return Tweet entity ready for persistence
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Tweet toEntity(CreateTweetRequestDto requestDto);

    /**
     * Maps Tweet entity to TweetResponseDto.
     * Used when returning tweet data in API responses.
     *
     * @param tweet the tweet entity from database
     * @return TweetResponseDto for API response
     */
    TweetResponseDto toResponseDto(Tweet tweet);
}
