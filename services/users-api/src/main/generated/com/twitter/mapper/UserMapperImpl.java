package com.twitter.mapper;

import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
import com.twitter.dto.UserPatchDto;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.entity.User;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-14T15:43:00+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toUser(UserRequestDto userRequestDto) {
        if ( userRequestDto == null ) {
            return null;
        }

        User user = new User();

        user.setLogin( userRequestDto.login() );
        user.setFirstName( userRequestDto.firstName() );
        user.setLastName( userRequestDto.lastName() );
        user.setEmail( userRequestDto.email() );

        return user;
    }

    @Override
    public UserResponseDto toUserResponseDto(User user) {
        if ( user == null ) {
            return null;
        }

        UUID id = null;
        String login = null;
        String firstName = null;
        String lastName = null;
        String email = null;
        UserStatus status = null;
        UserRole role = null;
        LocalDateTime createdAt = null;

        id = user.getId();
        login = user.getLogin();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        email = user.getEmail();
        status = user.getStatus();
        role = user.getRole();
        createdAt = user.getCreatedAt();

        UserResponseDto userResponseDto = new UserResponseDto( id, login, firstName, lastName, email, status, role, createdAt );

        return userResponseDto;
    }

    @Override
    public UserPatchDto toUserPatchDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserPatchDto userPatchDto = new UserPatchDto();

        userPatchDto.setLogin( user.getLogin() );
        userPatchDto.setFirstName( user.getFirstName() );
        userPatchDto.setLastName( user.getLastName() );
        userPatchDto.setEmail( user.getEmail() );

        return userPatchDto;
    }

    @Override
    public void updateUserFromPatchDto(UserPatchDto userPatchDto, User user) {
        if ( userPatchDto == null ) {
            return;
        }

        user.setLogin( userPatchDto.getLogin() );
        user.setFirstName( userPatchDto.getFirstName() );
        user.setLastName( userPatchDto.getLastName() );
        user.setEmail( userPatchDto.getEmail() );
    }

    @Override
    public void updateUserFromUpdateDto(UserUpdateDto userUpdateDto, User user) {
        if ( userUpdateDto == null ) {
            return;
        }

        user.setLogin( userUpdateDto.login() );
        user.setFirstName( userUpdateDto.firstName() );
        user.setLastName( userUpdateDto.lastName() );
        user.setEmail( userUpdateDto.email() );
    }
}
