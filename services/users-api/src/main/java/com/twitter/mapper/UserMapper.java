package com.twitter.mapper;

import com.twitter.dto.UserPatchDto;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for data transformation between User entities and DTO objects.
 * <p>
 * This mapper provides automatic data transformation between application layers
 * with configuration for ignoring service fields and ensuring data security.
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
     * the passwordHash field as password processing is handled separately
     * with hashing and salt generation.
     *
     * @param userRequestDto DTO containing user data for creation
     * @return User entity without password hash
     */
    @Mapping(target = "passwordHash", ignore = true)
    User toUser(UserRequestDto userRequestDto);

    /**
     * Converts User entity to UserResponseDto.
     * <p>
     * This method transforms User entity data into a response DTO, automatically
     * excluding sensitive fields (passwordHash, passwordSalt) for security
     * when transmitting data to clients.
     *
     * @param user User entity from database
     * @return DTO containing user data for client response
     */
    UserResponseDto toUserResponseDto(User user);

    /**
     * Converts User entity to UserPatchDto for PATCH operations.
     * <p>
     * This method creates an intermediate object for applying JSON Patch changes
     * while preserving current user field values for partial updates.
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
     * id, passwordHash, passwordSalt, status, role. These fields are managed
     * separately through service business logic.
     *
     * @param userUpdateDto DTO containing data for complete update
     * @param user           target User entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "passwordSalt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateUserFromUpdateDto(UserUpdateDto userUpdateDto, @MappingTarget User user);
}
