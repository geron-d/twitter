package com.twitter.mapper;

import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import com.twitter.entity.Tweet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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

    /**
     * Updates existing Tweet entity with data from CreateTweetRequestDto.
     * Used for updating existing tweets.
     *
     * @param requestDto the request DTO with updated data
     * @param tweet      the existing tweet entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CreateTweetRequestDto requestDto, @MappingTarget Tweet tweet);

    /**
     * Maps Tweet entity to CreateTweetRequestDto.
     * Used for converting existing tweet back to request format.
     *
     * @param tweet the tweet entity
     * @return CreateTweetRequestDto
     */
    CreateTweetRequestDto toRequestDto(Tweet tweet);
}
