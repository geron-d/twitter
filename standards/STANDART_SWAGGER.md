# Swagger/OpenAPI Documentation Standards

## Overview

This document defines the standards and best practices for writing Swagger/OpenAPI documentation in the Twitter microservices project.

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

- Create separate OpenAPI interface (`*Api.java`) for API documentation
- Implement the interface in the controller class
- Keep documentation annotations separate from business logic
- Document all endpoints, including error responses

---

## 2. OpenAPI Configuration

### 2.1 Configuration Class Structure

Every service must have an `OpenApiConfig` class in the `config` package:

```java
package com.twitter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 * <p>
 * This configuration class sets up the OpenAPI documentation for the [Service Name] API service.
 *
 * @author [author]
 * @version 1.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures the OpenAPI specification for the [Service Name] API.
     *
     * @return configured OpenAPI instance
     */
    @Bean
    public OpenAPI [serviceName]ApiOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("[Service Name] API")
                .description("""
                    REST API for [service description] in the Twitter microservices system.
                    
                    This API provides comprehensive [capability description] including:
                    - [Feature 1]
                    - [Feature 2]
                    - [Feature 3]
                    
                    ## Authentication
                    Currently, the API does not require authentication for basic operations.
                    Future versions will implement JWT-based authentication.
                    
                    ## Rate Limiting
                    API requests are subject to rate limiting to ensure system stability.
                    Please refer to response headers for current rate limit information.
                    
                    ## Error Handling
                    The API uses standard HTTP status codes and follows RFC 7807 Problem Details
                    for error responses, providing detailed information about validation failures
                    and business rule violations.
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
- Example: `"Twitter Users API"`, `"Twitter Tweet API"`

**Description:**
- Must include:
  - Brief overview of the API purpose
  - List of main capabilities (bullet points)
  - Authentication section (current state and future plans)
  - Rate limiting information
  - Error handling approach (RFC 7807 Problem Details)
- Use triple-quoted strings (`"""`) for multi-line descriptions
- Keep descriptions concise but informative

**Version:**
- Use semantic versioning: `"1.0.0"`
- Update version when making API changes

### 2.3 Server Configuration

- Configure at least one server for local development
- Server URL should match the service port from `application.yml`
- Include description for each server environment
- Example:
  ```java
  .servers(List.of(
      new Server()
          .url("http://localhost:8081")
          .description("Local development server")
  ))
  ```

---

## 3. API Interfaces

### 3.1 Interface Structure

Create a separate interface for OpenAPI annotations:

```java
package com.twitter.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
// ... other imports

/**
 * OpenAPI interface for [Entity] Management API.
 * <p>
 * This interface contains all OpenAPI annotations for the [Entity] Management API endpoints.
 *
 * @author [author]
 * @version 1.0
 */
@Tag(name = "[Entity] Management", description = "API for managing [entities] in the Twitter system")
public interface [Entity]Api {
    // Method signatures with OpenAPI annotations
}
```

### 3.2 Tag Annotation

**Usage:**
- Apply `@Tag` annotation at the interface level
- Use singular form for entity name: `"User Management"`, `"Tweet Management"`
- Description should be concise: `"API for managing [entities] in the Twitter system"`

**Example:**
```java
@Tag(name = "User Management", description = "API for managing users in the Twitter system")
public interface UserApi {
    // ...
}
```

### 3.3 Controller Implementation

The controller should implement the API interface:

```java
@RestController
@RequestMapping("/api/v1/[entities]")
@RequiredArgsConstructor
public class [Entity]Controller implements [Entity]Api {
    
    private final [Entity]Service service;
    
    @Override
    public ResponseEntity<[Entity]ResponseDto> [methodName](...) {
        // Implementation
    }
}
```

---

## 4. Operations (Operations)

### 4.1 Operation Annotation

Every endpoint method must have `@Operation` annotation:

```java
@Operation(
    summary = "[Brief summary of the operation]",
    description = "[Detailed description of what the operation does. " +
        "Include validation rules, business logic, and important notes.]"
)
```

### 4.2 Summary Requirements

- **Format:** Short, action-oriented phrase (3-7 words)
- **Examples:**
  - `"Create new user"`
  - `"Get user by ID"`
  - `"Update user role"`
  - `"Deactivate user"`
- Use present tense
- Be specific about the action

### 4.3 Description Requirements

- **Format:** Detailed explanation (2-4 sentences)
- **Must include:**
  - What the operation does
  - Key validation rules or constraints
  - Business logic or rules that apply
  - Integration with other services (if applicable)
- Use concatenation for multi-line descriptions:
  ```java
  description = "Creates a new tweet with the provided content and user ID. " +
      "It performs validation on the request data, checks if the user exists via " +
      "users-api integration, and saves the tweet to the database. " +
      "The tweet content must be between 1 and 280 characters and cannot be empty or only whitespace."
  ```

### 4.4 Complete Example

```java
@Operation(
    summary = "Create new tweet",
    description = "Creates a new tweet with the provided content and user ID. " +
        "It performs validation on the request data, checks if the user exists via " +
        "users-api integration, and saves the tweet to the database. " +
        "The tweet content must be between 1 and 280 characters and cannot be empty or only whitespace."
)
```

---

## 5. API Responses (ApiResponses)

### 5.1 ApiResponses Annotation

Every operation must document all possible response codes using `@ApiResponses`:

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
    ),
    // ... more responses
})
```

### 5.2 Success Responses

**200 OK:**
- Use for successful GET, PUT, PATCH, DELETE operations
- Must include:
  - `schema = @Schema(implementation = [ResponseDto].class)`
  - `@ExampleObject` with realistic example data

**201 Created:**
- Use for successful POST operations that create resources
- Must include response schema and example

**Example:**
```java
@ApiResponse(
    responseCode = "201",
    description = "Tweet created successfully",
    content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = TweetResponseDto.class),
        examples = @ExampleObject(
            name = "Created Tweet",
            summary = "Example created tweet",
            value = """
                {
                  "id": "123e4567-e89b-12d3-a456-426614174000",
                  "userId": "987e6543-e21b-43d2-b654-321987654321",
                  "content": "This is my first tweet!",
                  "createdAt": "2025-01-27T15:30:00Z",
                  "updatedAt": "2025-01-27T15:30:00Z"
                }
                """
        )
    )
)
```

### 5.3 Error Responses

**400 Bad Request:**
- Use for validation errors, constraint violations, business rule violations
- Must include `@ExampleObject` with RFC 7807 Problem Details format
- Document different error types separately if they return different structures

**404 Not Found:**
- Use when resource is not found
- Include Problem Details example

**409 Conflict:**
- Use for uniqueness violations
- Include field name and value in example

**Example Error Response:**
```java
@ApiResponse(
    responseCode = "400",
    description = "Validation error",
    content = @Content(
        mediaType = "application/problem+json",
        examples = @ExampleObject(
            name = "Format Validation Error",
            summary = "Content validation failed",
            value = """
                {
                  "type": "https://example.com/errors/format-validation",
                  "title": "Format Validation Error",
                  "status": 400,
                  "detail": "Tweet content must be between 1 and 280 characters",
                  "fieldName": "content",
                  "constraintName": "CONTENT_VALIDATION",
                  "timestamp": "2025-01-27T15:30:00Z"
                }
                """
        )
    )
)
```

### 5.4 ExampleObject Requirements

- **Name:** Descriptive name for the example (e.g., `"Created Tweet"`, `"Validation Error"`)
- **Summary:** Brief summary (optional but recommended)
- **Value:** Valid JSON string using triple quotes
- Use realistic UUIDs, dates, and data
- Match the actual response structure exactly

### 5.5 Multiple Error Types

If an endpoint can return multiple types of 400 errors, document each separately:

```java
@ApiResponse(
    responseCode = "400",
    description = "Validation error",
    content = @Content(...)
),
@ApiResponse(
    responseCode = "400",
    description = "Business rule violation",
    content = @Content(...)
),
@ApiResponse(
    responseCode = "400",
    description = "Constraint violation error",
    content = @Content(...)
)
```

---

## 6. Parameters

### 6.1 Parameter Annotation

All method parameters must be documented with `@Parameter`:

```java
@Parameter(
    description = "[Clear description of the parameter]",
    required = true,  // or false
    example = "[example value]"  // for path/query parameters
)
```

### 6.2 Path Parameters

**Requirements:**
- Always `required = true`
- Include `example` with realistic UUID or ID value
- Description should explain what the parameter identifies

**Example:**
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

**Requirements:**
- Use `@Parameter` for DTO parameters
- `required = true` for POST/PUT operations
- Description should explain what data the body contains

**Example:**
```java
ResponseEntity<TweetResponseDto> createTweet(
    @Parameter(description = "Tweet data for creation", required = true)
    CreateTweetRequestDto createTweetRequest
);
```

### 6.4 Query Parameters

**Requirements:**
- Include `example` value
- Set `required` appropriately (usually `false` for optional filters)
- Description should explain filtering behavior

**Example:**
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

Every DTO must have `@Schema` annotation at the class level:

```java
@Schema(
    name = "[DTO Name]",
    description = "[Clear description of what this DTO represents]",
    example = """
        {
          "field1": "value1",
          "field2": "value2"
        }
        """
)
public record [DtoName](...) {
}
```

**Requirements:**
- **Name:** Use descriptive name (e.g., `"UserRequest"`, `"TweetResponse"`)
- **Description:** Explain the purpose and when it's used
- **Example:** Complete JSON example with realistic values

**Example:**
```java
@Schema(
    name = "CreateTweetRequest",
    description = "Data structure for creating new tweets in the system",
    example = """
        {
          "content": "This is a sample tweet content",
          "userId": "123e4567-e89b-12d3-a456-426614174000"
        }
        """
)
public record CreateTweetRequestDto(...) {
}
```

### 7.2 Field-Level Schema Annotation

Every field in a DTO must have `@Schema` annotation:

```java
@Schema(
    description = "[Clear description of the field]",
    example = "[example value]",
    requiredMode = Schema.RequiredMode.REQUIRED,  // or NOT_REQUIRED
    format = "[format]",  // e.g., "uuid", "email", "date-time"
    minLength = [number],  // for strings
    maxLength = [number],  // for strings
    nullable = true  // if field can be null
)
```

### 7.3 Field Documentation Requirements

**Description:**
- Explain what the field represents
- Include constraints or special behavior
- For optional fields, mention "(optional)" in description

**Example:**
```java
@Schema(
    description = "User's first name (optional)",
    example = "Jane",
    maxLength = 100,
    nullable = true
)
String firstName;
```

**Required Fields:**
- Use `requiredMode = Schema.RequiredMode.REQUIRED` for mandatory fields
- Use `requiredMode = Schema.RequiredMode.NOT_REQUIRED` for optional fields

**Format:**
- Use appropriate formats:
  - `format = "uuid"` for UUID fields
  - `format = "email"` for email fields
  - `format = "date-time"` for LocalDateTime fields

**Length Constraints:**
- Use `minLength` and `maxLength` for string fields
- Match validation constraints from `@Size` annotations

**Nullable Fields:**
- Set `nullable = true` for fields that can be null
- Omit or set `nullable = false` for required fields

### 7.4 Sensitive Data Handling

For sensitive fields (passwords, tokens), use `accessMode`:

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

**Access Modes:**
- `Schema.AccessMode.WRITE_ONLY`: Field appears only in request schemas
- `Schema.AccessMode.READ_ONLY`: Field appears only in response schemas
- Default: Field appears in both request and response schemas

### 7.5 Enum Fields

For enum fields, use `implementation`:

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
          "createdAt": "2025-01-21T20:30:00"
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
        example = "2025-01-21T20:30:00",
        format = "date-time"
    )
    LocalDateTime createdAt
) {
}
```

---

## 8. Code Examples

### 8.1 Complete API Interface Example

```java
package com.twitter.controller;

import com.twitter.dto.request.CreateTweetRequestDto;
import com.twitter.dto.response.TweetResponseDto;
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
 * OpenAPI interface for Tweet Management API.
 *
 * @author geron
 * @version 1.0
 */
@Tag(name = "Tweet Management", description = "API for managing tweets in the Twitter system")
public interface TweetApi {

    /**
     * Creates a new tweet in the system.
     * <p>
     * This method creates a new tweet with the provided content and user ID.
     * It performs validation on the request data, checks if the user exists via
     * users-api integration, and saves the tweet to the database. The tweet content
     * must be between 1 and 280 characters and cannot be empty or only whitespace.
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
                schema = @Schema(implementation = TweetResponseDto.class),
                examples = @ExampleObject(
                    name = "Created Tweet",
                    summary = "Example created tweet",
                    value = """
                        {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "userId": "987e6543-e21b-43d2-b654-321987654321",
                          "content": "This is my first tweet!",
                          "createdAt": "2025-01-27T15:30:00Z",
                          "updatedAt": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "Format Validation Error",
                    summary = "Content validation failed",
                    value = """
                        {
                          "type": "https://example.com/errors/format-validation",
                          "title": "Format Validation Error",
                          "status": 400,
                          "detail": "Tweet content must be between 1 and 280 characters",
                          "fieldName": "content",
                          "constraintName": "CONTENT_VALIDATION",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Business rule violation",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    name = "User Not Found Error",
                    summary = "User does not exist",
                    value = """
                        {
                          "type": "https://example.com/errors/business-rule-validation",
                          "title": "Business Rule Validation Error",
                          "status": 400,
                          "detail": "Business rule 'USER_NOT_EXISTS' violated for context: 987e6543-e21b-43d2-b654-321987654321",
                          "ruleName": "USER_NOT_EXISTS",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
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
 * REST controller for tweet management in Twitter microservices.
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

- **Use consistent naming:** Follow the same naming patterns across all APIs
- **Consistent descriptions:** Use similar structure and tone for similar operations
- **Consistent examples:** Use similar UUIDs and data formats across examples
- **Consistent error formats:** All error responses should follow RFC 7807 Problem Details

### 9.2 Completeness

- **Document all endpoints:** Every public endpoint must have OpenAPI documentation
- **Document all responses:** Include all possible HTTP status codes
- **Document all parameters:** Every parameter must have `@Parameter` annotation
- **Document all DTOs:** Every DTO must have `@Schema` annotation at class and field levels

### 9.3 Examples

- **Provide examples for all responses:** Both success and error responses should have examples
- **Use realistic data:** Examples should use realistic UUIDs, dates, and values
- **Match actual structure:** Examples must match the actual response structure exactly
- **Include multiple error examples:** If multiple error types exist, provide examples for each

### 9.4 Error Documentation

- **Document all error scenarios:** Include validation errors, business rule violations, not found errors
- **Use Problem Details format:** All error responses should follow RFC 7807
- **Include error context:** Error examples should include relevant field names, constraint names, rule names
- **Separate error types:** Document different error types as separate `@ApiResponse` entries

### 9.5 Validation Alignment

- **Match validation constraints:** `@Schema` annotations should match `@Size`, `@Min`, `@Max` validation constraints
- **Document required fields:** Use `requiredMode` to indicate required vs optional fields
- **Document formats:** Use `format` for UUIDs, emails, dates, etc.
- **Document nullable fields:** Use `nullable = true` for optional fields

### 9.6 Security

- **Hide sensitive data:** Use `accessMode = Schema.AccessMode.WRITE_ONLY` for passwords
- **Don't expose internal details:** Keep descriptions focused on API behavior, not implementation
- **Document authentication:** Include authentication information in API description (even if not yet implemented)

### 9.7 Maintainability

- **Keep documentation up to date:** Update documentation when API changes
- **Review documentation:** Include Swagger documentation in code reviews
- **Test examples:** Verify that examples in documentation match actual API behavior
- **Version documentation:** Update API version when making breaking changes

---

## 10. Common Patterns

### 10.1 CRUD Operations

**Create (POST):**
- Summary: `"Create new [entity]"`
- Response: `201 Created` with created entity
- Errors: `400` (validation), `409` (conflict if applicable)

**Read (GET):**
- Summary: `"Get [entity] by ID"` or `"Get [entities] with filtering"`
- Response: `200 OK` with entity or list
- Errors: `404` (not found)

**Update (PUT):**
- Summary: `"Update [entity] completely"`
- Response: `200 OK` with updated entity
- Errors: `400` (validation), `404` (not found), `409` (conflict if applicable)

**Partial Update (PATCH):**
- Summary: `"Partially update [entity]"`
- Response: `200 OK` with updated entity
- Errors: `400` (validation), `404` (not found), `409` (conflict if applicable)

**Delete/Deactivate:**
- Summary: `"Deactivate [entity]"` or `"Delete [entity]"`
- Response: `200 OK` with updated entity (if soft delete) or `204 No Content`
- Errors: `400` (business rule violation), `404` (not found)

### 10.2 Pagination

For paginated endpoints:
- Use `PagedModel<[Entity]ResponseDto>` as return type
- Document `Pageable` parameter with description: `"Pagination parameters (page, size, sorting)"`
- Include pagination metadata in response example:
  ```json
  {
    "content": [...],
    "page": {
      "size": 10,
      "number": 0,
      "totalElements": 100,
      "totalPages": 10
    }
  }
  ```

### 10.3 Filtering

For filtered endpoints:
- Create a separate Filter DTO with `@Schema` annotation
- Document filter DTO with example showing all filter fields
- Use `@Parameter` for filter parameter with description: `"Filter criteria for [entity] search"`

---

## 11. Checklist

Use this checklist when documenting a new API endpoint:

### OpenAPI Configuration
- [ ] `OpenApiConfig` class exists with proper `Info` configuration
- [ ] API title follows naming convention
- [ ] Description includes capabilities, authentication, rate limiting, error handling
- [ ] Version is set correctly
- [ ] Server configuration matches service port

### API Interface
- [ ] Separate `*Api` interface created with `@Tag` annotation
- [ ] Tag name follows convention (e.g., "User Management")
- [ ] Controller implements the interface

### Operation Documentation
- [ ] `@Operation` annotation with `summary` and `description`
- [ ] Summary is concise and action-oriented
- [ ] Description includes validation rules and business logic

### Response Documentation
- [ ] `@ApiResponses` annotation with all possible status codes
- [ ] Success response (200/201) with `@Schema` and `@ExampleObject`
- [ ] Error responses (400, 404, 409, etc.) with Problem Details examples
- [ ] All examples use realistic data

### Parameter Documentation
- [ ] All parameters have `@Parameter` annotation
- [ ] Path parameters include `example` value
- [ ] Request body parameters have clear descriptions
- [ ] Query parameters documented appropriately

### DTO Documentation
- [ ] Class-level `@Schema` with `name`, `description`, and `example`
- [ ] All fields have field-level `@Schema` annotations
- [ ] Required fields use `requiredMode = Schema.RequiredMode.REQUIRED`
- [ ] Optional fields use `nullable = true` or `requiredMode = Schema.RequiredMode.NOT_REQUIRED`
- [ ] Format specified for UUIDs, emails, dates
- [ ] Length constraints match validation annotations
- [ ] Sensitive fields use `accessMode = Schema.AccessMode.WRITE_ONLY`
- [ ] Enum fields use `implementation` attribute

---

## 12. References

### Official Documentation
- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI 3.0 Specification](https://swagger.io/specification/)
- [RFC 7807 Problem Details for HTTP APIs](https://tools.ietf.org/html/rfc7807)

### Project Examples
- `services/users-api/src/main/java/com/twitter/config/OpenApiConfig.java`
- `services/tweet-api/src/main/java/com/twitter/config/OpenApiConfig.java`
- `services/users-api/src/main/java/com/twitter/controller/UserApi.java`
- `services/tweet-api/src/main/java/com/twitter/controller/TweetApi.java`
- `services/users-api/src/main/java/com/twitter/dto/UserRequestDto.java`
- `services/users-api/src/main/java/com/twitter/dto/UserResponseDto.java`
- `services/tweet-api/src/main/java/com/twitter/dto/request/CreateTweetRequestDto.java`
- `services/tweet-api/src/main/java/com/twitter/dto/response/TweetResponseDto.java`

---

*Last updated: 2025-01-27*
*Version: 1.0*



