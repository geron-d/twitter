package com.twitter.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.common.aspect.LoggableRequest;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserResponseDto;
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
//    private final TracingUtil tracingUtil;

    @LoggableRequest
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") UUID id) {
//        log.info("Getting user by ID: {}", id);
//
//        return tracingUtil.executeInSpan("get-user-by-id", Map.of(
//            "user.id", id.toString(),
//            "operation", "GET_USER"
//        ), () -> {
//            tracingUtil.addEvent("user-lookup-started");
//
//            ResponseEntity<UserResponseDto> result = userService.getUserById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//
//            tracingUtil.addEvent("user-lookup-completed");
//            tracingUtil.addTag("response.status", String.valueOf(result.getStatusCode().value()));
//
//            return result;
//        });
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @LoggableRequest
    @GetMapping
    public PagedModel<UserResponseDto> findAll(@ModelAttribute UserFilter userFilter, Pageable pageable) {
//        log.info("Finding users with filter: {} and pageable: {}", userFilter, pageable);
//
//        return tracingUtil.executeInSpan("find-all-users", Map.of(
//            "page.size", String.valueOf(pageable.getPageSize()),
//            "page.number", String.valueOf(pageable.getPageNumber()),
//            "operation", "FIND_USERS"
//        ), () -> {
//            tracingUtil.addEvent("user-search-started");
//
//            Page<UserResponseDto> users = userService.findAll(userFilter, pageable);
//
//            tracingUtil.addEvent("user-search-completed");
//            tracingUtil.addTag("result.count", String.valueOf(users.getTotalElements()));
//
//            return users;
//        });
        Page<UserResponseDto> users = userService.findAll(userFilter, pageable);
        return new PagedModel<>(users);
    }

    @LoggableRequest
    @PostMapping
    public UserResponseDto createUser(@RequestBody @Valid UserRequestDto userRequest) {
//        log.info("Creating new user with email: {}", userRequest.email());
//
//        return tracingUtil.executeInSpan("create-user", Map.of(
//            "user.email", userRequest.email(),
//            "user.login", userRequest.login(),
//            "operation", "CREATE_USER"
//        ), () -> {
//            tracingUtil.addEvent("user-creation-started");
//
//            UserResponseDto result = userService.createUser(userRequest);
//
//            tracingUtil.addEvent("user-creation-completed");
//            tracingUtil.addTag("user.id", result.id().toString());
//
//            return result;
//        });
        return userService.createUser(userRequest);
    }

    @LoggableRequest
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable("id") UUID id, @RequestBody @Valid UserRequestDto userDetails) {
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
