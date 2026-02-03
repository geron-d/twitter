package com.twitter.controller;

import com.twitter.dto.request.BaseScriptRequestDto;
import com.twitter.dto.response.BaseScriptResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * OpenAPI interface for Admin Script API.
 *
 * @author geron
 * @version 1.0
 */
@Tag(name = "Admin Scripts", description = "API for executing administrative scripts in the Twitter system")
public interface AdminScriptApi {

    @Operation(
        summary = "Execute base script",
        description = "Executes the base administrative script that creates multiple users with random data, " +
            "creates follow relationships between users (central user follows half of others, half of others follow central user), " +
            "adds tweets for each user, and deletes one tweet from a specified number of random users. " +
            "Parameters: nUsers (1-1000), nTweetsPerUser (1-100), " +
            "lUsersForDeletion (0+, must not exceed number of users with tweets)."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Script executed successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BaseScriptResponseDto.class)
        )
    )
    ResponseEntity<BaseScriptResponseDto> baseScript(
        @Parameter(
            description = "Script parameters: nUsers (1-1000), nTweetsPerUser (1-100), lUsersForDeletion (0+)",
            required = true
        )
        BaseScriptRequestDto requestDto);
}