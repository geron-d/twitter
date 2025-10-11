# Twitter Common Library

## Introduction

**Twitter Common Library** is a shared library for the Twitter project's microservices architecture, built on Java 24 and Spring Boot 3. The library provides common components that can be used across all microservices in the project to ensure consistency, code reuse, and standardization of error handling and logging.

### Library Purpose

- **HTTP Request Logging**: Automatic logging of incoming and outgoing HTTP requests with the ability to hide sensitive data
- **Global Exception Handling**: Centralized error handling with standardized response returns
- **Specialized Exceptions**: Predefined exceptions for business logic
- **Aspect-Oriented Programming**: Using AOP for cross-cutting functionality

## Architecture

### Package Structure

```
com.twitter.common/
├── aspect/                    # AOP Aspects
│   ├── LoggableRequest.java      # Logging annotation
│   └── LoggableRequestAspect.java # Logging aspect
├── enums/                     # Enumerations
│   ├── UserRole.java             # User roles (ADMIN, MODERATOR, USER)
│   └── UserStatus.java           # User statuses (ACTIVE, INACTIVE)
├── exception/                 # Exception handling
│   ├── GlobalExceptionHandler.java      # Global handler
│   └── validation/             # Validation exceptions
│       ├── ValidationException.java         # Base validation exception
│       ├── BusinessRuleValidationException.java # Business rules
│       ├── FormatValidationException.java      # Data format
│       ├── UniquenessValidationException.java  # Uniqueness
│       └── ValidationType.java                 # Validation types
├── config/                    # Configurations (empty)
└── util/                      # Utilities (empty)
```

### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Twitter Common Library                   │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │   Aspect Layer  │    │      Exception Layer            │ │
│  │                 │    │                                 │ │
│  │ @LoggableRequest│    │ GlobalExceptionHandler          │ │
│  │ LoggableRequest │    │ ValidationException             │ │
│  │ Aspect          │    │ BusinessRuleValidation         │ │
│  └─────────────────┘    │ FormatValidation               │ │
│           │              │ UniquenessValidation            │ │
│           │              └─────────────────────────────────┘ │
│           │                           │                     │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │   Domain Layer  │    │        Business Logic           │ │
│  │                 │    │                                 │ │
│  │ UserRole        │    │ Admin Management                │ │
│  │ UserStatus      │    │ Validation                      │ │
│  │ ValidationType  │    │ Error Handling                  │ │
│  └─────────────────┘    └─────────────────────────────────┘ │
│           │                           │                     │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │   HTTP Layer    │    │        Integration              │ │
│  │                 │    │                                 │ │
│  │ Request/Response│    │ Spring Boot Integration         │ │
│  │ Logging         │    │ AOP Integration                 │ │
│  │ Sensitive Data  │    │ Validation Integration          │ │
│  │ Hiding          │    │                                 │ │
│  └─────────────────┘    └─────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## API Documentation

### @LoggableRequest Annotation

**Purpose**: Annotation for controller methods that enables automatic logging of HTTP requests and responses.

**Parameters**:
- `printRequestBody()` (boolean, default: true) - enable/disable request body logging
- `hideFields()` (String[], default: {}) - array of fields to hide in logs

**Usage Example**:
```java
@RestController
public class UserController {
    
    @PostMapping("/users")
    @LoggableRequest(hideFields = {"password", "secretKey"})
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto userDto) {
        // user creation logic
    }
    
    @GetMapping("/users/{id}")
    @LoggableRequest(printRequestBody = false)
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        // user retrieval logic
    }
}
```

### Global Exception Handler

**Class**: `GlobalExceptionHandler`

**Handled Exceptions**:

| Exception Type | HTTP Status | Description |
|----------------|-------------|-------------|
| `ResponseStatusException` | From exception | Standard Spring exceptions |
| `RuntimeException` | 500 | Unexpected server errors |
| `ConstraintViolationException` | 400 | Validation errors |
| `ValidationException` | 400 | Base validation errors |
| `BusinessRuleValidationException` | 400 | Business rule violations |
| `FormatValidationException` | 400 | Data format errors |
| `UniquenessValidationException` | 409 | Uniqueness violations |

**Response Format** (ProblemDetail):
```json
{
  "type": "https://example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed: ...",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Specialized Exceptions

**Base Class**: `ValidationException`

**Exception Hierarchy**:

```
ValidationException (abstract)
├── BusinessRuleValidationException
├── FormatValidationException
└── UniquenessValidationException
```

**BusinessRuleValidationException**:
- **Purpose**: Business rule violations in the system
- **Examples**: Attempting to deactivate the last admin, changing the last admin's role
- **Factory Methods**: `lastAdminDeactivation()`, `lastAdminRoleChange()`

**FormatValidationException**:
- **Purpose**: Data format errors
- **Examples**: Invalid email format, incorrect date format
- **Factory Methods**: `invalidFormat()`, `invalidEmailFormat()`

**UniquenessValidationException**:
- **Purpose**: Data uniqueness violations
- **Examples**: Duplicate email, duplicate login
- **Factory Methods**: `duplicateEmail()`, `duplicateLogin()`

**ValidationType enum**:
- `BUSINESS_RULE` - Business rules
- `FORMAT` - Data format
- `UNIQUENESS` - Uniqueness
- `CUSTOM` - Custom rules

## Business Logic

### Enumerations

**UserRole** - User roles:
- `ADMIN` - System administrator
- `MODERATOR` - Content moderator  
- `USER` - Regular user

**UserStatus** - User statuses:
- `ACTIVE` - Active user
- `INACTIVE` - Inactive user

**ValidationType** - Validation types:
- `BUSINESS_RULE` - Business rules
- `FORMAT` - Data format
- `UNIQUENESS` - Uniqueness
- `CUSTOM` - Custom rules

### Request Logging

**Component**: `LoggableRequestAspect`

**Functionality**:
1. **Method Interception**: Automatically intercepts methods annotated with `@LoggableRequest`
2. **Request Logging**: Records HTTP method, URI, headers, and request body
3. **Sensitive Data Hiding**: Masks specified fields with "***" value
4. **Response Logging**: Records response status and data size

**Algorithm**:
```
1. Get HTTP request from context
2. Extract @LoggableRequest annotation parameters
3. Log request details (method, URI, headers, body)
4. Hide sensitive fields if necessary
5. Execute original method
6. Log response details (status, data size)
7. Return result
```

### Exception Handling

**Component**: `GlobalExceptionHandler`

**Handling Strategy**:
1. **Response Standardization**: All errors returned in RFC 7807 format (ProblemDetail)
2. **Logging**: Automatic logging of all exceptions
3. **Security**: Hiding internal details from client
4. **Tracing**: Adding timestamps for debugging

## Usage Examples

### 1. Basic Controller Logging

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PostMapping
    @LoggableRequest
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserRequestDto userDto) {
        User user = userService.createUser(userDto);
        return ResponseEntity.ok(userMapper.toResponseDto(user));
    }
    
    @GetMapping("/{id}")
    @LoggableRequest(printRequestBody = false)
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(userMapper.toResponseDto(user));
    }
}
```

### 2. Logging with Sensitive Data Hiding

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/login")
    @LoggableRequest(hideFields = {"password", "token"})
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto loginDto) {
        // authentication logic
    }
    
    @PostMapping("/register")
    @LoggableRequest(hideFields = {"password", "confirmPassword"})
    public ResponseEntity<UserResponseDto> register(@RequestBody RegisterRequestDto registerDto) {
        // registration logic
    }
}
```

### 3. Business Exception Handling

```java
@Service
public class AdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    public void deactivateAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
        
        if (admin.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not an admin");
        }
        
        long activeAdminCount = userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
        
        if (activeAdminCount <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Cannot deactivate the last active administrator. System requires at least one active admin."
            );
        }
        
        admin.setStatus(UserStatus.INACTIVE);
        userRepository.save(admin);
    }
}
```

### 4. Validation with Error Handling

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PutMapping("/{id}")
    @LoggableRequest
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateDto updateDto) {
        
        try {
            User user = userService.updateUser(id, updateDto);
            return ResponseEntity.ok(userMapper.toResponseDto(user));
        } catch (ConstraintViolationException e) {
            // Automatically handled by GlobalExceptionHandler
            throw e;
        }
    }
}
```

## Usage Scenarios

### Scenario 1: User Creation with Logging

**Description**: Creating a new user with full request and response logging.

**Steps**:
1. Client sends POST request to `/api/users`
2. `LoggableRequestAspect` intercepts the request
3. Request details are logged (method, URI, headers, body)
4. User creation business logic is executed
5. Response status and data size are logged
6. Response is returned to client

**Log**:
```
### REQUEST POST /api/users ,Headers: Content-Type: application/json; Accept: application/json , Body: {"username":"john_doe","email":"john@example.com","password":"***"}
### RESPONSE POST /api/users , status: 201
```

### Scenario 2: Validation Error

**Description**: Handling validation error with standardized response return.

**Steps**:
1. Client sends incorrect data
2. Spring Validation throws `ConstraintViolationException`
3. `GlobalExceptionHandler` intercepts the exception
4. `ProblemDetail` is created with error details
5. HTTP 400 with JSON response is returned

**Response**:
```json
{
  "type": "https://example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed: email must be a valid email address",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Scenario 3: Attempting to Deactivate Last Admin

**Description**: Preventing deactivation of the last active administrator.

**Steps**:
1. Client requests admin deactivation
2. Service checks the number of active admins
3. If admin is the last one, `ResponseStatusException` with HTTP 409 is thrown
4. `GlobalExceptionHandler` handles the exception
5. HTTP 409 with conflict description is returned

**Response**:
```json
{
  "type": "https://example.com/errors/last-admin-deactivation",
  "title": "Last Admin Deactivation Error",
  "status": 409,
  "detail": "Cannot deactivate the last active administrator",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Dependencies

### Core Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| `spring-boot-starter` | 3.x | Core Spring Boot starter |
| `spring-boot-starter-aop` | 3.x | Aspect-oriented programming |
| `spring-boot-starter-validation` | 3.x | Data validation |
| `spring-boot-starter-web` | 3.x | Web applications |
| `lombok` | 1.18.38 | Code generation |
| `mapstruct` | 1.6.3 | Object mapping |

### Dependency Management

The library uses **centralized version management** through `dependencyManagement` in the root `build.gradle`.

**Usage Principles**:
- All dependency versions are managed from the root `build.gradle`
- Only dependency names are specified in the library's `build.gradle` without versions
- When adding new dependencies, version is added to the root `dependencyManagement`

### Requirements

- **Java**: 24
- **Spring Boot**: 3.x
- **Maven/Gradle**: Modern versions

## Configuration

### Logging Configuration

To enable detailed response logging, add to `application.yml`:

```yaml
logging:
  level:
    com.twitter.common.aspect: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### AOP Configuration

Ensure AOP is enabled in your application

## Testing

The library includes comprehensive unit tests for all components:

- **LoggableRequestAspectTest**: Testing logging aspect
- **SuccessfulScenarios**: Successful scenarios
- **BoundaryScenarios**: Boundary cases
- **ExceptionScenarios**: Exception handling
- **AdditionalScenarios**: Additional tests

### Running Tests

```bash
./gradlew test
```

## Usage Recommendations

### 1. Logging

- Use `@LoggableRequest` on all public controller methods
- Specify `hideFields` for fields with sensitive data
- Disable `printRequestBody` for GET requests without body

### 2. Exception Handling

- Use `ResponseStatusException` for standard HTTP errors
- Apply `BusinessRuleValidationException` for admin business logic
- Add custom exceptions extending `ResponseStatusException`

### 3. Security

- Always hide passwords, tokens, and other sensitive data
- Don't log full request bodies in production
- Use appropriate logging levels
