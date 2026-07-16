# Swagger/OpenAPI Documentation Standards

## Overview

This document defines standards and best practices for Swagger/OpenAPI documentation in this project’s Java
microservices (Spring Boot, SpringDoc). Wording may reference sample domains (users, tweets) for illustration; apply
the same rules to any service (for example Music API / Yandex integration).

**Technology Stack:**

- SpringDoc OpenAPI 3.x
- OpenAPI 3.0 Specification
- Java 24
- Spring Boot 3.5.5

---

## 1. General Principles

### 1.1 Documentation Language

- **All Swagger documentation must be written in English**
- Use clear, concise language
- Avoid technical jargon when possible
- Use present tense for descriptions

### 1.2 API Versioning

- Use semantic versioning for API versions (e.g., `1.0.0`)
- Version should be specified in `OpenApiConfig` class
- Version changes should follow semantic versioning rules:
    - **MAJOR**: Breaking changes
    - **MINOR**: New features (backward compatible)
    - **PATCH**: Bug fixes (backward compatible)

### 1.3 Documentation Structure

- Create separate OpenAPI interface (`*Api.java` or `*OpenApi.java`) for API documentation
- Implement the interface in the controller class
- Keep documentation annotations separate from business logic
- Document all endpoints, including error responses

---

## 2. OpenAPI Configuration

### 2.1 Configuration Class Structure

Every service must have an `OpenApiConfig` class in the `config` package:

```java
package com.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 *
 * @author [author]
 * @version 1.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures the OpenAPI specification for this service.
     *
     * @return configured OpenAPI instance
     */
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
            .info(new Info()
                .title("[Service Name] API")
                .description("""
                    REST API for [service description].
                    """)
                .version("1.0.0"))
            .servers(List.of(
                new Server()
                    .url("http://localhost:[port]")
                    .description("Local development server")
            ));
    }
}
```

### 2.2 Info Configuration Requirements

**Title:**

- Format: `"[Service Name] API"`

**Description:**

- Must include:
    - Brief overview of the API purpose
    - List of main capabilities (bullet points)
- Use triple-quoted strings (`"""`) for multi-line descriptions
- Keep descriptions concise but informative

**Version:**

- Use semantic versioning: `"1.0.0"`
- Update version when making API changes

### 2.3 Server Configuration

- Configure at least one server for local development
- Server URL should match the service port from `application.yml`
- Include description for each server environment
- Example fragment (continuation of the bean builder):

```java
            .servers(List.of(
                new Server()
                    .url("http://localhost:8081")
                    .description("Local development server")
            ));
```

---

## 3. API Interfaces

### 3.1 Interface Structure

Create a separate interface for OpenAPI annotations:

```java
package com.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * OpenAPI interface for [Entity] Management API.
 *
 * @author [author]
 * @version 1.0
 */
@Tag(name = "[Entity] Management", description = "API for managing [entities]")
public interface EntityApi {
    // Method signatures with OpenAPI annotations
}
```

### 3.2 Tag Annotation

- Apply `@Tag` at the interface level
- Use clear tag names (e.g., `"User Management"`, `"Tweet Management"`)
- Description should be concise

### 3.3 Controller Implementation

The controller should implement the API interface:

```java
@RestController
@RequestMapping("/api/v1/[entities]")
@RequiredArgsConstructor
public class EntityController implements EntityApi {

    private final EntityService service;

    @Override
    public ResponseEntity<EntityResponseDto> methodName(...) {
        // Implementation
    }
}
```

---

## 4. Operations

### 4.1 Operation Annotation

Every endpoint method must have `@Operation`:

```java
@Operation(
    summary = "[Brief summary of the operation]",
    description = "[Detailed description. " +
        "Include validation rules, business logic, and important notes.]"
)
```

### 4.2 Summary Requirements

- **Format:** Short, action-oriented phrase (3-7 words)
- **Examples:** `"Create new user"`, `"Get user by ID"`, `"Update user role"`
- Use present tense; be specific about the action

### 4.3 Description Requirements

- **Format:** Detailed explanation (2-4 sentences)
- **Must include:** what the operation does; key validation or constraints; business rules; integrations (if any)
- Use string concatenation for multi-line descriptions:

```java
description = "First sentence explaining behavior. " +
    "Second sentence with validation or integration details."
```

- **Canonical end-to-end example** (`@Operation`, `@ApiResponses`, `@Parameter`): see **§8.1**.

---

## 5. API Responses (ApiResponses)

### 5.1 ApiResponses Annotation

Every operation must document possible response codes using `@ApiResponses`:

```java
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "[Success description]",
        content = @Content(...)
    ),
    @ApiResponse(
        responseCode = "400",
        description = "[Error description]",
        content = @Content(...)
    )
})
```

### 5.2 Success Responses

**200 OK:** GET, PUT, PATCH, DELETE — include `schema = @Schema(implementation = [ResponseDto].class)` where applicable.

**201 Created:** POST that creates a resource — include response schema.

```java
@ApiResponse(
    responseCode = "201",
    description = "Resource created successfully",
    content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = TweetResponseDto.class)
    )
)
```

### 5.3 Error Responses (RFC 7807)

**400 / 404 / 409** (and other errors): use `mediaType = "application/problem+json"` and document Problem Details.

Minimal shape:

```java
@ApiResponse(
    responseCode = "400",
    description = "Validation error",
    content = @Content(
        mediaType = "application/problem+json",
        schema = @Schema(implementation = ProblemDetail.class)
    )
)
```

When several error codes need **distinct example payloads** (different `type`, `title`, or custom properties), you may
add **`@ExampleObject`** on that `@ApiResponse` content. This is especially useful when the body is not fully captured
by a single shared DTO schema:

```java
@ApiResponse(
    responseCode = "401",
    description = "Unauthorized",
    content = @Content(
        mediaType = "application/problem+json",
        schema = @Schema(implementation = ProblemDetail.class),
        examples = @ExampleObject(
            name = "unauthorized",
            value = """
                {
                  "type": "https://example.com/problem/unauthorized",
                  "title": "Unauthorized",
                  "status": 401
                }
                """
        )
    )
)
```

### 5.4 Examples Policy (success vs errors)

- **`application/json` success bodies:** Prefer **`@Schema(implementation = YourResponseDto.class)`**. Put sample JSON
  in DTOs using class-level or field-level **`@Schema(example = "...")`** or text-block `example` on the record so the
  spec stays aligned with schemas.
- **`application/problem+json`:** Use **`@Schema(implementation = ProblemDetail.class)`** (or a dedicated problem DTO
  if you introduce one). Optionally add **`@ExampleObject`** per response to show realistic RFC 7807 payloads (see §5.3).
- **Do not** duplicate the same narrative in §9; for day-to-day rules, this subsection is authoritative.

---

## 6. Parameters

### 6.1 Parameter Annotation

All method parameters should be documented with `@Parameter`:

```java
@Parameter(
    description = "[Clear description of the parameter]",
    required = true,
    example = "[example value]"
)
```

### 6.2 Path Parameters

- Always `required = true`
- Include realistic `example` (UUID or ID)
- Description explains what the parameter identifies

```java
ResponseEntity<UserResponseDto> getUserById(
    @Parameter(
        description = "Unique identifier of the user",
        required = true,
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID id
);
```

### 6.3 Request Body Parameters

- `required = true` for POST/PUT when the body is mandatory
- Description explains what the body contains

```java
ResponseEntity<TweetResponseDto> createTweet(
    @Parameter(description = "Tweet data for creation", required = true)
    CreateTweetRequestDto createTweetRequest
);
```

### 6.4 Query Parameters

- Include `example` where helpful
- Set `required` appropriately for filters

```java
PagedModel<UserResponseDto> findAll(
    @Parameter(description = "Filter criteria for user search")
    UserFilter userFilter,
    @Parameter(description = "Pagination parameters (page, size, sorting)")
    Pageable pageable
);
```

---

## 7. DTO Documentation

### 7.1 Class-Level Schema Annotation

Every DTO must have `@Schema` at class level with `name`, `description`, and a realistic JSON `example` (text block).

**Field-level rules, enums, and sensitive data:** see §7.2–§7.5.

**Full record examples (request and response):** see **§7.6** (canonical).

### 7.2 Field-Level Schema Annotation

Every field in a DTO must have `@Schema` where it contributes to the public API contract:

```java
@Schema(
    description = "[Clear description of the field]",
    example = "[example value]",
    requiredMode = Schema.RequiredMode.REQUIRED,
    format = "uuid",
    minLength = 1,
    maxLength = 100,
    nullable = false
)
```

Use `Schema.RequiredMode.NOT_REQUIRED`, `nullable = true`, and omit optional attributes when not applicable.

### 7.3 Field Documentation Requirements

- **Description:** what the field represents; for optional fields, note “(optional)”
- **Required:** `requiredMode = Schema.RequiredMode.REQUIRED` vs `NOT_REQUIRED`
- **Format:** `uuid`, `email`, `date-time`, etc.
- **Length:** `minLength` / `maxLength` aligned with Bean Validation (`@Size`, etc.)

### 7.4 Sensitive Data Handling

```java
@Schema(
    description = "Password for user authentication (will be securely hashed)",
    example = "securePassword123",
    minLength = 8,
    requiredMode = Schema.RequiredMode.REQUIRED,
    accessMode = Schema.AccessMode.WRITE_ONLY
)
String password;
```

- `WRITE_ONLY`: request schemas only
- `READ_ONLY`: response schemas only

### 7.5 Enum Fields

```java
@Schema(
    description = "Current status of the user account",
    example = "ACTIVE",
    implementation = UserStatus.class
)
UserStatus status;
```

### 7.6 Complete DTO Example

**Request DTO:**

```java
@Schema(
    name = "UserRequest",
    description = "Data structure for creating new users in the system",
    example = """
        {
          "login": "jane_smith",
          "firstName": "Jane",
          "lastName": "Smith",
          "email": "jane.smith@example.com",
          "password": "securePassword123"
        }
        """
)
public record UserRequestDto(
    @Schema(
        description = "Unique login name for user authentication",
        example = "jane_smith",
        minLength = 3,
        maxLength = 50,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Login cannot be blank")
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    String login,

    @Schema(
        description = "User's first name (optional)",
        example = "Jane",
        maxLength = 100
    )
    String firstName,

    @Schema(
        description = "Unique email address for the user",
        example = "jane.smith@example.com",
        format = "email",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    String email,

    @Schema(
        description = "Password for user authentication (will be securely hashed)",
        example = "securePassword123",
        minLength = 8,
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.WRITE_ONLY
    )
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password
) {
}
```

**Response DTO:**

```java
@Schema(
    name = "UserResponse",
    description = "User information returned by the API (excludes sensitive data like password)",
    example = """
        {
          "id": "123e4567-e89b-12d3-a456-426614174000",
          "login": "jane_smith",
          "firstName": "Jane",
          "lastName": "Smith",
          "email": "jane.smith@example.com",
          "status": "ACTIVE",
          "role": "USER",
          "createdAt": "2026-01-21T20:30:00"
        }
        """
)
public record UserResponseDto(
    @Schema(
        description = "Unique identifier for the user",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "User's unique login name",
        example = "jane_smith",
        maxLength = 50
    )
    String login,

    @Schema(
        description = "User's first name (optional)",
        example = "Jane",
        maxLength = 100,
        nullable = true
    )
    String firstName,

    @Schema(
        description = "User's unique email address",
        example = "jane.smith@example.com",
        format = "email"
    )
    String email,

    @Schema(
        description = "Current status of the user account",
        example = "ACTIVE",
        implementation = UserStatus.class
    )
    UserStatus status,

    @Schema(
        description = "Role assigned to the user",
        example = "USER",
        implementation = UserRole.class
    )
    UserRole role,

    @Schema(
        description = "Date and time when the user account was created",
        example = "2026-01-21T20:30:00",
        format = "date-time"
    )
    LocalDateTime createdAt
) {
}
```

---

## 8. Code Examples

**§8.1 is the canonical end-to-end reference** for `@Operation`, `@ApiResponses`, and `@Parameter` together. Sections
4–6 define the same rules without repeating the full listing.

### 8.1 Complete API Interface Example

```java
package com.twitter.controller;

import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * OpenAPI interface for Tweet Management API.
 *
 * @author geron
 * @version 1.0
 */
@Tag(name = "Tweet Management", description = "API for managing tweets in the Twitter system")
public interface TweetApi {

    /**
     * Creates a new tweet in the system.
     *
     * @param createTweetRequest DTO containing tweet data for creation (content and userId)
     * @return ResponseEntity containing the created tweet data with HTTP 201 status
     */
    @Operation(
        summary = "Create new tweet",
        description = "Creates a new tweet with the provided content and user ID. " +
            "It performs validation on the request data, checks if the user exists via " +
            "users-api integration, and saves the tweet to the database. " +
            "The tweet content must be between 1 and 280 characters and cannot be empty or only whitespace."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Tweet created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TweetResponseDto.class)
            )
        )
    })
    ResponseEntity<TweetResponseDto> createTweet(
        @Parameter(description = "Tweet data for creation", required = true)
        CreateTweetRequestDto createTweetRequest
    );
}
```

### 8.2 Complete Controller Implementation Example

```java
package com.twitter.controller;

import com.twitter.common.aspect.LoggableRequest;
import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
import com.twitter.service.TweetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for tweet management.
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tweets")
@RequiredArgsConstructor
public class TweetController implements TweetApi {

    private final TweetService tweetService;

    /**
     * @see TweetApi#createTweet
     */
    @LoggableRequest
    @PostMapping
    @Override
    public ResponseEntity<TweetResponseDto> createTweet(
        @RequestBody @Valid CreateTweetRequestDto createTweetRequest) {
        TweetResponseDto createdTweet = tweetService.createTweet(createTweetRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTweet);
    }
}
```

---

## 9. Best Practices

### 9.1 Consistency

- Same naming patterns, description tone, and UUID/date style across endpoints
- Error responses follow RFC 7807 Problem Details

### 9.2 Completeness

- Every public endpoint, parameter, and DTO used in the API surface has OpenAPI coverage

### 9.3 Examples

- Follow **§5.4** for where examples live (`@Schema` on DTOs vs `@ExampleObject` on problem responses)
- Keep examples realistic and aligned with runtime behavior

### 9.4 Validation Alignment

- `@Schema` constraints mirror Bean Validation; document formats and nullability explicitly

### 9.5 Security

- `WRITE_ONLY` for secrets; descriptions stay API-centric, not implementation dumps
- Mention authentication expectations in service-level OpenAPI description when relevant

### 9.6 Maintainability

- Update docs with API changes; verify examples in reviews; bump API version on breaking changes

---

## 10. Common Patterns

### 10.1 CRUD Operations

**Create (POST):** summary `"Create new [entity]"` — `201 Created` with body.

**Read (GET):** `"Get [entity] by ID"` or list with filters — `200 OK`.

**Update (PUT):** `"Update [entity] completely"` — `200 OK`.

**Partial update (PATCH):** `"Partially update [entity]"` — `200 OK`.

**Delete / deactivate:** `"Deactivate [entity]"` / `"Delete [entity]"` — `200 OK` or `204 No Content`.

### 10.2 Pagination

- Return type `PagedModel<[Entity]ResponseDto>` where applicable
- Document `Pageable` (see §6.4)
- Response example shape:

```json
{
  "content": [],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 100,
    "totalPages": 10
  }
}
```

### 10.3 Filtering

- Filter DTO with `@Schema` and example
- `@Parameter` description: `"Filter criteria for [entity] search"`

---

## 11. Checklist

Use when adding or changing an endpoint:

- [ ] **§2** — `OpenApiConfig`: title, description, version, servers match deployment
- [ ] **§3** — `*Api` / `*OpenApi` interface with `@Tag`; controller implements it
- [ ] **§4** — `@Operation` with summary and full description
- [ ] **§5** — `@ApiResponses` for all status codes; success uses DTO schema; errors use `problem+json` per §5.3–§5.4
- [ ] **§6** — `@Parameter` on path, query, and documented body parameters
- [ ] **§7** — DTO class- and field-level `@Schema`; enums and sensitive fields handled per §7.4–§7.5

---

## 12. References

### Official Documentation

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI 3.0 Specification](https://swagger.io/specification/)
- [RFC 7807 Problem Details for HTTP APIs](https://tools.ietf.org/html/rfc7807)

### Project Examples

**This repository (Music API):**

- `src/main/java/com/music/controller/YandexUserOpenApi.java` — OpenAPI interface with success schema and multiple
  `problem+json` responses with `@ExampleObject`
- `src/main/java/com/music/dto/YandexUserInfo.java` — response DTO with `@Schema`

**Illustrative multi-service layout (other codebases):**

- `services/users-api/.../OpenApiConfig.java`, `UserApi.java`, `UserRequestDto.java`, `UserResponseDto.java`
- `services/tweet-api/.../OpenApiConfig.java`, `TweetApi.java`, `CreateTweetRequestDto.java`, `TweetResponseDto.java`

---

*Last updated: 2026-04-04*
*Version: 1.1*