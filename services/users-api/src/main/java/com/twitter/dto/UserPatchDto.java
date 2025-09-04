package com.twitter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserPatchDto {

    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    private String login;

    private String firstName;

    private String lastName;

    @Email(message = "Invalid email format")
    private String email;
}
