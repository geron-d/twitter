# Project Standards for Twitter Microservices

## Overview

This document defines project-wide standards for using shared components from `common-lib` across Twitter microservices. These standards ensure consistency, code reuse, and proper organization of cross-service components.

**Key Principles:**
- Use `@LoggableRequest` for all controller method logging
- Rely on `GlobalExceptionHandler` for centralized error handling
- Extend validation exception hierarchy when needed
- Place shared DTOs in `common-lib/dto`
- Place shared enums in `common-lib/enums`

---

## 1. Request Logging with @LoggableRequest

### 1.1 When to Use

**Always use `@LoggableRequest` on controller methods** that handle HTTP requests. This annotation provides automatic request/response logging with configurable options.

### 1.2 Basic Usage

```java
@LoggableRequest
@GetMapping("/{id}")
public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
    return userService.getUserById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}
```

### 1.3 Hiding Sensitive Fields

**Always hide sensitive fields** like passwords, tokens, or personal information:

```java
@LoggableRequest(hideFields = {"password"})
@PostMapping
public UserResponseDto createUser(@RequestBody @Valid UserRequestDto userRequest) {
    return userService.createUser(userRequest);
}
```

**Common fields to hide:**
- `password` - User passwords
- `token` - Authentication tokens
- `secret` - API secrets
- `ssn` - Social Security Numbers
- `creditCard` - Credit card information

### 1.4 Disabling Request Body Logging

**Use `printRequestBody = false`** for:
- Large request bodies that would clutter logs
- Performance-critical endpoints
- Endpoints where body content is not relevant

```java
@LoggableRequest(printRequestBody = false)
@GetMapping("/{id}")
public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
    // Only method and URI will be logged
}
```

### 1.5 Best Practices

- **Always use** `@LoggableRequest` on public controller methods
- **Always hide** sensitive fields using `hideFields` parameter
- **Consider performance** for high-traffic endpoints
- **Use consistent** field names across services for hiding

---

## 2. Error Handling with GlobalExceptionHandler

### 2.1 Automatic Exception Handling

**The `GlobalExceptionHandler` is automatically active** in all services that include `common-lib`. No additional configuration is required.

### 2.2 Supported Exception Types

The handler processes the following exception types:

| Exception Type | HTTP Status | Use Case |
|----------------|-------------|----------|
| `ResponseStatusException` | From exception | Standard Spring HTTP exceptions |
| `RuntimeException` | 500 | Unexpected server errors |
| `ConstraintViolationException` | 400 | Bean validation errors |
| `UniquenessValidationException` | 409 | Duplicate data errors |
| `BusinessRuleValidationException` | 409 | Business logic violations |
| `FormatValidationException` | 400 | Data format errors |
| `ValidationException` | 400 | General validation errors |

### 2.3 Response Format

**All errors are returned in RFC 7807 ProblemDetail format:**

```json
{
  "type": "https://example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed: email must be a valid email address",
  "timestamp": "2025-01-27T15:30:00Z",
  "fieldName": "email",
  "constraintName": "EMAIL_FORMAT"
}
```

### 2.4 Best Practices

- **Don't create custom exception handlers** - use the provided hierarchy
- **Throw appropriate exception types** from the validation package
- **Let GlobalExceptionHandler** convert exceptions to ProblemDetail
- **Use specific exceptions** (UniquenessValidationException, etc.) instead of generic ones

---

## 3. Validation Exception Hierarchy

### 3.1 Exception Structure

The validation exception hierarchy is located in `com.twitter.common.exception.validation`:

```
ValidationException (abstract base)
├── UniquenessValidationException
├── BusinessRuleValidationException
└── FormatValidationException
```

### 3.2 When to Use Each Exception

**UniquenessValidationException:**
- Duplicate email addresses
- Duplicate login names
- Other uniqueness violations

```java
throw new UniquenessValidationException("email", email);
```

**BusinessRuleValidationException:**
- Business logic violations
- Domain-specific constraints
- Complex rule violations

```java
throw BusinessRuleValidationException.violation("USER_NOT_EXISTS", userId.toString());
```

**FormatValidationException:**
- Invalid email format
- Invalid date format
- Data structure violations

```java
throw FormatValidationException.invalidFormat("email", "EMAIL_FORMAT", email);
```

### 3.3 When to Add New Exception Types

**Add a new exception type when:**
1. The existing types don't cover your use case
2. You need specific error handling logic
3. The exception will be used across multiple services

**Steps to add a new exception:**
1. Extend `ValidationException` in `common-lib/src/main/java/com/twitter/common/exception/validation/`
2. Implement `getValidationType()` method
3. Add handler in `GlobalExceptionHandler`
4. Update `ValidationType` enum if needed
5. Document the new exception type

**Example:**

```java
public class AuthorizationValidationException extends ValidationException {
    private final String resource;
    private final String action;
    
    public AuthorizationValidationException(String resource, String action) {
        super("User is not authorized to perform " + action + " on " + resource);
        this.resource = resource;
        this.action = action;
    }
    
    @Override
    public ValidationType getValidationType() {
        return ValidationType.AUTHORIZATION;
    }
}
```

### 3.4 Best Practices

- **Prefer existing exceptions** over creating new ones
- **Use factory methods** when available (e.g., `BusinessRuleValidationException.violation()`)
- **Provide clear error messages** in exception constructors
- **Include context information** (field names, values, etc.)

---

## 4. Shared DTOs in common-lib/dto

### 4.1 When to Place DTOs in common-lib

**Place DTOs in `common-lib/src/main/java/com/twitter/common/dto/` when:**
- The DTO is used by **multiple services**
- The DTO represents **inter-service communication** (e.g., Feign clients)
- The DTO is part of a **shared contract** between services

### 4.2 Examples of Shared DTOs

**UserExistsResponseDto** - Used by:
- `users-api` - Returns user existence status
- `tweet-api` - Checks if user exists before creating tweet

```java
// In common-lib/dto/UserExistsResponseDto.java
public record UserExistsResponseDto(boolean exists) {
}
```

### 4.3 When NOT to Place DTOs in common-lib

**Keep DTOs in service-specific packages when:**
- The DTO is only used within a single service
- The DTO is specific to one service's domain
- The DTO contains service-specific fields

**Example - Service-specific DTO:**

```java
// In services/users-api/src/main/java/com/twitter/dto/UserRequestDto.java
public record UserRequestDto(
    String login,
    String email,
    String password
) {
    // Only used in users-api
}
```

### 4.4 Best Practices

- **Evaluate usage** before moving DTOs to common-lib
- **Use Records** for all DTOs (Java 24 feature)
- **Add OpenAPI annotations** (`@Schema`) for documentation
- **Keep DTOs immutable** - no setters, use records
- **Document shared DTOs** with JavaDoc

---

## 5. Shared Enums in common-lib/enums

### 5.1 When to Place Enums in common-lib

**Place enums in `common-lib/src/main/java/com/twitter/common/enums/` when:**
- The enum is used by **multiple services**
- The enum represents **shared domain concepts**
- The enum values are part of **inter-service contracts**

### 5.2 Examples of Shared Enums

**UserRole** - Used by:
- `users-api` - User role management
- `tweet-api` - Authorization checks
- Other services - Role-based access control

**UserStatus** - Used by:
- `users-api` - User status management
- `tweet-api` - User existence validation
- Other services - User state checks

```java
// In common-lib/enums/UserRole.java
public enum UserRole {
    ADMIN,
    MODERATOR,
    USER
}
```

### 5.3 When NOT to Place Enums in common-lib

**Keep enums in service-specific packages when:**
- The enum is only used within a single service
- The enum represents service-specific concepts
- The enum values are internal to one service

**Example - Service-specific enum:**

```java
// In services/tweet-api/src/main/java/com/twitter/enums/TweetType.java
public enum TweetType {
    ORIGINAL,
    REPLY,
    RETWEET
    // Only used in tweet-api
}
```

### 5.4 Best Practices

- **Use enums for fixed sets of values** - don't use strings
- **Add OpenAPI annotations** (`@Schema`) for documentation
- **Document enum values** with JavaDoc
- **Use `@Enumerated(EnumType.STRING)`** in JPA entities
- **Consider extensibility** when designing shared enums

---

## 6. Integration Checklist

When creating a new service or adding functionality:

- [ ] All controller methods use `@LoggableRequest`
- [ ] Sensitive fields are hidden in `@LoggableRequest(hideFields = {...})`
- [ ] Validation exceptions extend the common hierarchy
- [ ] Shared DTOs are placed in `common-lib/dto`
- [ ] Shared enums are placed in `common-lib/enums`
- [ ] Service-specific DTOs stay in service packages
- [ ] Service-specific enums stay in service packages
- [ ] Exceptions are thrown with appropriate types
- [ ] Error responses follow ProblemDetail format (automatic)

---

## 7. Migration Guide

### 7.1 Moving DTOs to common-lib

1. Identify DTOs used by multiple services
2. Move DTO to `common-lib/src/main/java/com/twitter/common/dto/`
3. Update imports in all services
4. Ensure DTO follows Record pattern
5. Add OpenAPI annotations if missing

### 7.2 Moving Enums to common-lib

1. Identify enums used by multiple services
2. Move enum to `common-lib/src/main/java/com/twitter/common/enums/`
3. Update imports in all services
4. Update JPA entities to use new package
5. Add OpenAPI annotations if missing

### 7.3 Adding New Validation Exceptions

1. Create new exception class extending `ValidationException`
2. Implement `getValidationType()` method
3. Add handler method in `GlobalExceptionHandler`
4. Update `ValidationType` enum if needed
5. Document the new exception type

---

## 8. Version History

- **v1.0** (2025-01-27): Initial version based on common-lib analysis

---

## References

- [Code Standards](./STANDART_CODE.md)
- [common-lib README](../shared/common-lib/README.md)
- [GlobalExceptionHandler](../shared/common-lib/src/main/java/com/twitter/common/exception/GlobalExceptionHandler.java)
- [LoggableRequest](../shared/common-lib/src/main/java/com/twitter/common/aspect/LoggableRequest.java)



