package com.twitter.mapper;

import com.twitter.common.dto.request.tweet.CreateTweetRequestDto;
import com.twitter.common.dto.response.tweet.TweetResponseDto;
import com.twitter.dto.request.UpdateTweetRequestDto;
import com.twitter.entity.Tweet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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

    /**
     * Updates Tweet entity with data from UpdateTweetRequestDto.
     *
     * @param updateDto DTO containing data for tweet update
     * @param tweet     target Tweet entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "userId", ignore = true)
    void updateTweetFromUpdateDto(UpdateTweetRequestDto updateDto, @MappingTarget Tweet tweet);
}