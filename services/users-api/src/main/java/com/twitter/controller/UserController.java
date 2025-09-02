package com.twitter.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.UserFilter;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
import com.twitter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public PagedModel<UserResponseDto> findAll(@ModelAttribute UserFilter userFilter, Pageable pageable) {
        Page<UserResponseDto> users = userService.findAll(userFilter, pageable);
        return new PagedModel<>(users);
    }

    @PostMapping
    public UserResponseDto createUser(@RequestBody @Valid UserRequestDto userRequest) {
        return userService.createUser(userRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable UUID id, @RequestBody @Valid UserRequestDto userDetails) {
        return userService.updateUser(id, userDetails)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> patchUser(@PathVariable UUID id, @RequestBody JsonNode patchNode) {
        return userService.patchUser(id, patchNode)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
