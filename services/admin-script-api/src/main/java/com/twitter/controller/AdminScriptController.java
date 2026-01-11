package com.twitter.controller;

import com.twitter.common.aspect.LoggableRequest;
import com.twitter.dto.request.BaseScriptRequestDto;
import com.twitter.dto.response.BaseScriptResponseDto;
import com.twitter.service.GenerateUsersAndTweetsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for administrative scripts in Twitter microservices.
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin-scripts")
@RequiredArgsConstructor
public class AdminScriptController implements AdminScriptApi {

    private final GenerateUsersAndTweetsService generateUsersAndTweetsService;

    /**
     * @see AdminScriptApi#generateUsersAndTweets
     */
    @LoggableRequest
    @PostMapping("/generate-users-and-tweets")
    @Override
    public ResponseEntity<BaseScriptResponseDto> generateUsersAndTweets(
        @RequestBody @Valid BaseScriptRequestDto requestDto) {
        BaseScriptResponseDto response = generateUsersAndTweetsService.executeScript(requestDto);
        return ResponseEntity.ok(response);
    }
}
