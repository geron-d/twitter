package com.twitter.controller;

import com.twitter.dto.request.GenerateUsersAndTweetsRequestDto;
import com.twitter.dto.response.GenerateUsersAndTweetsResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * OpenAPI interface for Admin Script API.
 * <p>
 * This interface contains all OpenAPI annotations for the Admin Script API endpoints.
 *
 * @author geron
 * @version 1.0
 */
@Tag(name = "Admin Scripts", description = "API for executing administrative scripts in the Twitter system")
public interface AdminScriptApi {

    /**
     * Executes the administrative script to generate users, follow relationships and tweets.
     * <p>
     * This endpoint executes an administrative script that performs the following operations:
     * <ul>
     *   <li>Creates nUsers users with random data (login, email, firstName, lastName, password)</li>
     *   <li>Creates follow relationships between users (central user follows half of others, half of others follow central user)</li>
     *   <li>Creates nTweetsPerUser tweets for each successfully created user with random content</li>
     *   <li>Validates that lUsersForDeletion does not exceed the number of users with tweets</li>
     *   <li>Deletes one tweet from lUsersForDeletion random users (if lUsersForDeletion > 0)</li>
     * </ul>
     * <p>
     *
     * @param requestDto DTO containing script parameters (nUsers, nTweetsPerUser, lUsersForDeletion)
     * @return ResponseEntity containing GenerateUsersAndTweetsResponseDto with lists of IDs and execution statistics
     */
    @Operation(
        summary = "Generate users, follow relationships and tweets",
        description = "Executes an administrative script that creates multiple users with random data, " +
            "creates follow relationships between users (central user follows half of others, half of others follow central user), " +
            "adds tweets for each user, and deletes one tweet from a specified number of random users. " +
            "Parameters: nUsers (1-1000), nTweetsPerUser (1-100), " +
            "lUsersForDeletion (0+, must not exceed number of users with tweets)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Script executed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GenerateUsersAndTweetsResponseDto.class),
                examples = {
                    @ExampleObject(
                        name = "Successful Execution",
                        summary = "Example successful script execution",
                        value = """
                            {
                              "createdUsers": [
                                "123e4567-e89b-12d3-a456-426614174000",
                                "223e4567-e89b-12d3-a456-426614174001",
                                "323e4567-e89b-12d3-a456-426614174002"
                              ],
                              "createdFollows": [
                                "723e4567-e89b-12d3-a456-426614174010",
                                "823e4567-e89b-12d3-a456-426614174011"
                              ],
                              "createdTweets": [
                                "423e4567-e89b-12d3-a456-426614174003",
                                "523e4567-e89b-12d3-a456-426614174004",
                                "623e4567-e89b-12d3-a456-426614174005"
                              ],
                              "deletedTweets": [
                                "423e4567-e89b-12d3-a456-426614174003"
                              ],
                              "statistics": {
                                "totalUsersCreated": 3,
                                "totalFollowsCreated": 2,
                                "totalTweetsCreated": 15,
                                "totalTweetsDeleted": 1,
                                "usersWithTweets": 3,
                                "usersWithoutTweets": 0,
                                "executionTimeMs": 1234,
                                "errors": []
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Execution with Partial Errors",
                        summary = "Example execution with some errors in statistics",
                        value = """
                            {
                              "createdUsers": [
                                "123e4567-e89b-12d3-a456-426614174000",
                                "223e4567-e89b-12d3-a456-426614174001"
                              ],
                              "createdFollows": [],
                              "createdTweets": [
                                "423e4567-e89b-12d3-a456-426614174003",
                                "523e4567-e89b-12d3-a456-426614174004"
                              ],
                              "deletedTweets": [],
                              "statistics": {
                                "totalUsersCreated": 2,
                                "totalFollowsCreated": 0,
                                "totalTweetsCreated": 10,
                                "totalTweetsDeleted": 0,
                                "usersWithTweets": 2,
                                "usersWithoutTweets": 0,
                                "executionTimeMs": 856,
                                "errors": [
                                  "Validation failed: Business rule 'DELETION_COUNT_EXCEEDS_USERS_WITH_TWEETS' violated for context: Cannot delete tweets from 5 users: only 2 users have tweets"
                                ]
                              }
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/problem+json",
                examples = {
                    @ExampleObject(
                        name = "Request Validation Error",
                        summary = "Bean validation failed",
                        value = """
                            {
                              "type": "https://example.com/errors/validation-error",
                              "title": "Validation Error",
                              "status": 400,
                              "detail": "Validation failed: nUsers: Number of users must be at least 1",
                              "timestamp": "2025-01-27T15:30:00Z"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Business Rule Validation Error",
                        summary = "Business rule violation",
                        value = """
                            {
                              "type": "https://example.com/errors/business-rule-validation",
                              "title": "Business Rule Validation Error",
                              "status": 400,
                              "detail": "Business rule 'DELETION_COUNT_EXCEEDS_USERS_WITH_TWEETS' violated for context: Cannot delete tweets from 5 users: only 3 users have tweets",
                              "ruleName": "DELETION_COUNT_EXCEEDS_USERS_WITH_TWEETS",
                              "timestamp": "2025-01-27T15:30:00Z"
                            }
                            """
                    )
                }
            )
        )
    })
    ResponseEntity<GenerateUsersAndTweetsResponseDto> generateUsersAndTweets(
        @Parameter(
            description = "Script parameters: nUsers (1-1000), nTweetsPerUser (1-100), lUsersForDeletion (0+)",
            required = true
        )
        GenerateUsersAndTweetsRequestDto requestDto);
}

