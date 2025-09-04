package com.twitter.dto;

import java.util.UUID;

public record UserResponseDto(
    UUID id,
    String login,
    String firstName,
    String lastName,
    String email
) {}
