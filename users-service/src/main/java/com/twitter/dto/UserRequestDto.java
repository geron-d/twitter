package com.twitter.dto;

public record UserRequestDto(
    String username,
    String firstName,
    String secondName,
    String email,
    String password
) {}
