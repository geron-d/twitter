package com.twitter.mapper;

import com.twitter.common.dto.request.user.UserRequestDto;
import com.twitter.common.dto.response.user.UserResponseDto;
import com.twitter.dto.UserPatchDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for data transformation between User entities and DTO objects.
 *
 * @author geron
 * @version 1.0
 */
@Mapper
public interface UserMapper {

    /**
     * Converts UserRequestDto to User entity.
     * <p>
     * This method transforms request DTO data into a User entity, ignoring
     * the passwordHash field.
     *
     * @param userRequestDto DTO containing user data for creation
     * @return User entity
     */
    @Mapping(target = "passwordHash", ignore = true)
    User toUser(UserRequestDto userRequestDto);

    /**
     * Converts User entity to UserResponseDto.
     *
     * @param user User entity from database
     * @return DTO containing user data for client response
     */
    UserResponseDto toUserResponseDto(User user);

    /**
     * Converts User entity to UserPatchDto for PATCH operations.
     *
     * @param user User entity from database
     * @return DTO for applying PATCH changes
     */
    UserPatchDto toUserPatchDto(User user);

    /**
     * Updates User entity with data from UserPatchDto.
     * <p>
     * This method applies only changed fields to the existing entity,
     * preserving unchanged fields without modification for partial updates.
     *
     * @param userPatchDto DTO containing data for partial update
     * @param user         target User entity to update
     */
    void updateUserFromPatchDto(UserPatchDto userPatchDto, @MappingTarget User user);

    /**
     * Updates User entity with data from UserUpdateDto.
     * <p>
     * This method performs complete entity updates while ignoring service fields:
     * id, passwordHash, passwordSalt, status, role.
     *
     * @param userUpdateDto DTO containing data for complete update
     * @param user          target User entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "passwordSalt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateUserFromUpdateDto(UserUpdateDto userUpdateDto, @MappingTarget User user);
}