package com.twitter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateDto(
    @NotNull(message = "Login cannot be null")
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    String login,
    
    String firstName,
    
    String lastName,
    
    @Email(message = "Invalid email format")
    String email,
    
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password
) {
}
