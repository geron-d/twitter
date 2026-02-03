package com.twitter.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.common.aspect.LoggableRequest;
import com.twitter.common.dto.request.user.UserRequestDto;
import com.twitter.common.dto.response.user.UserExistsResponseDto;
import com.twitter.common.dto.response.user.UserResponseDto;
import com.twitter.dto.UserRoleUpdateDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.dto.filter.UserFilter;
import com.twitter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for user management in Twitter microservices.
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    /**
     * @see UserApi#existsUser
     */
    @LoggableRequest
    @GetMapping("/{userId}/exists")
    @Override
    public ResponseEntity<UserExistsResponseDto> existsUser(@PathVariable("userId") UUID userId) {
        boolean exists = userService.existsById(userId);
        return ResponseEntity.ok(new UserExistsResponseDto(exists));
    }

    /**
     * @see UserApi#getUserById
     */
    @LoggableRequest
    @GetMapping("/{id}")
    @Override
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") UUID id) {
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @see UserApi#findAll
     */
    @LoggableRequest
    @GetMapping
    @Override
    public PagedModel<UserResponseDto> findAll(@ModelAttribute UserFilter userFilter,
                                               @PageableDefault Pageable pageable) {
        Page<UserResponseDto> users = userService.findAll(userFilter, pageable);
        return new PagedModel<>(users);
    }

    /**
     * @see UserApi#createUser
     */
    @LoggableRequest(hideFields = {"password"})
    @PostMapping
    @Override
    public UserResponseDto createUser(@RequestBody @Valid UserRequestDto userRequest) {
        return userService.createUser(userRequest);
    }

    /**
     * @see UserApi#updateUser
     */
    @LoggableRequest(hideFields = {"password"})
    @PutMapping("/{id}")
    @Override
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable("id") UUID id, @RequestBody @Valid UserUpdateDto userDetails) {
        return userService.updateUser(id, userDetails)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @see UserApi#patchUser
     */
    @LoggableRequest
    @PatchMapping("/{id}")
    @Override
    public ResponseEntity<UserResponseDto> patchUser(@PathVariable("id") UUID id, @RequestBody JsonNode patchNode) {
        return userService.patchUser(id, patchNode)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @see UserApi#inactivateUser
     */
    @LoggableRequest
    @PatchMapping("/{id}/inactivate")
    @Override
    public ResponseEntity<UserResponseDto> inactivateUser(@PathVariable("id") UUID id) {
        return userService.inactivateUser(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @see UserApi#updateUserRole
     */
    @LoggableRequest
    @PatchMapping("/{id}/role")
    @Override
    public ResponseEntity<UserResponseDto> updateUserRole(@PathVariable("id") UUID id,
                                                          @RequestBody @Valid UserRoleUpdateDto roleUpdate) {
        return userService.updateUserRole(id, roleUpdate)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}