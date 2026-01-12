package com.twitter.controller;

import com.twitter.common.aspect.LoggableRequest;
import com.twitter.dto.request.BaseScriptRequestDto;
import com.twitter.dto.response.BaseScriptResponseDto;
import com.twitter.service.BaseScriptService;
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

    private final BaseScriptService baseScriptService;

    /**
     * @see AdminScriptApi#baseScript
     */
    @LoggableRequest
    @PostMapping("/base-script")
    @Override
    public ResponseEntity<BaseScriptResponseDto> baseScript(
        @RequestBody @Valid BaseScriptRequestDto requestDto) {
        BaseScriptResponseDto response = baseScriptService.executeScript(requestDto);
        return ResponseEntity.ok(response);
    }
}
