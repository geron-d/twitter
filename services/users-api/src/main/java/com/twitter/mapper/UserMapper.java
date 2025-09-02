package com.twitter.mapper;


import com.twitter.dto.UserPatchDto;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
import com.twitter.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true)
    User toUser(UserRequestDto userRequestDto);

    UserResponseDto toUserResponseDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    void updateUserFromDto(UserRequestDto userRequestDto, @MappingTarget User user);

    UserPatchDto toUserPatchDto(User user);

    void updateUserFromPatchDto(UserPatchDto userPatchDto, @MappingTarget User user);
}
