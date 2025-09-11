package com.twitter.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.common.aspect.LoggableRequest;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.dto.filter.UserFilter;
import com.twitter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @LoggableRequest
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") UUID id) {
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @LoggableRequest
    @GetMapping
    public PagedModel<UserResponseDto> findAll(@ModelAttribute UserFilter userFilter, Pageable pageable) {
        Page<UserResponseDto> users = userService.findAll(userFilter, pageable);
        return new PagedModel<>(users);
    }

    @LoggableRequest(hideFields = {"password"})
    @PostMapping
    public UserResponseDto createUser(@RequestBody @Valid UserRequestDto userRequest) {
        return userService.createUser(userRequest);
    }

    @LoggableRequest(hideFields = {"password"})
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable("id") UUID id, @RequestBody @Valid UserUpdateDto userDetails) {
        return userService.updateUser(id, userDetails)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @LoggableRequest
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> patchUser(@PathVariable("id") UUID id, @RequestBody JsonNode patchNode) {
        return userService.patchUser(id, patchNode)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @LoggableRequest
    @PatchMapping("/{id}/inactivate")
    public ResponseEntity<UserResponseDto> inactivateUser(@PathVariable("id") UUID id) {
        return userService.inactivateUser(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
