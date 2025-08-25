package com.twitter.dto;

import java.util.UUID;

public record UserResponseDto(
    UUID id,
    String username,
    String firstName,
    String secondName,
    String email
) {}
