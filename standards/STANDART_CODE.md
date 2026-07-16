# Code Standards for Twitter Microservices Project

## Overview

This document defines the coding standards and best practices for the Twitter microservices project. These standards are
based on the analysis of existing services (users-api, tweet-api, common-lib) and should be followed when writing new
code or modifying existing code.

**Technology Stack:**

- Java 24
- Spring Boot 3.5.5
- Gradle
- PostgreSQL / MongoDB (choose per service needs)
- MapStruct
- Lombok
- OpenAPI/Swagger

**Canonical code examples (avoid duplicating these snippets elsewhere in this document):**

- **DTO Records (request, response, validation usage)** — §3.1 and §6.5 (`@Valid` on controllers only).
- **OpenAPI `OpenApiConfig` bean** — §4.3.
- **API interface vs controller (layering)** — §5.2 (minimal); **full OpenAPI on interfaces** — §9.1.
- **Request logging (`@LoggableRequest`)** — §10.2.

---

## 1. General Principles

### 1.1 Language and Documentation

- **All code documentation must be written in English**
- Use clear, concise language
- Avoid technical jargon when possible
- Use present tense for descriptions

### 1.2 Code Style

- Follow Java naming conventions:
    - Classes: `PascalCase` (e.g., `UserController`)
    - Methods: `camelCase` (e.g., `getUserById`)
    - Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_RETRY_COUNT`)
    - Packages: `lowercase` (e.g., `com.twitter.service`)

### 1.3 Package Structure

Standard package structure for services:

```
com.twitter
├── Application.java              # Main application class
├── config/                       # Configuration classes
├── controller/                   # REST controllers
│   ├── [Entity]Api.java         # OpenAPI interface
│   └── [Entity]Controller.java  # Controller implementation
├── client/                       # External HTTP clients (Feign/REST)
├── dto/                          # Data Transfer Objects
│   ├── request/                  # Request DTOs
│   ├── response/                 # Response DTOs
│   └── filter/                   # Filter DTOs
├── entity/                        # JPA entities/MongoDB documets
├── exception/                    # Custom exceptions
│   └── handler/                  # Global exception handlers
├── gateway/                      # Gateway wrappers for external integrations
├── mapper/                        # MapStruct mappers
├── repository/                    # Spring Data repositories (JPA or MongoDB)
├── service/                       # Business logic
│   ├── [Entity]Service.java     # Service interface
│   └── [Entity]ServiceImpl.java # Service implementation
├── validation/                   # Validators
│   ├── [Entity]Validator.java   # Validator interface
│   └── [Entity]ValidatorImpl.java # Validator implementation
└── util/                          # Utility classes
```

---

## 2. Gradle Configuration

### 2.1 Single-Module Project Structure (Default)

By default, use a simple Gradle single-module project.

```
music-api/
├── build.gradle
├── settings.gradle
└── src/
    ├── main/
    └── test/
```

### 2.2 Example build.gradle for a regular project

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.5'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.music'
version = '0.0.1-SNAPSHOT'
description = 'Music application'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
    maven { url 'https://repo.spring.io/milestone' }
    maven { url 'https://repo.spring.io/snapshot' }
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'

    implementation 'io.swagger.core.v3:swagger-annotations:2.2.38'
    implementation('org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13') {
        exclude group: 'io.swagger.core.v3', module: 'swagger-annotations'
    }

    implementation 'io.micrometer:micrometer-tracing-bridge-otel'

    compileOnly 'org.projectlombok:lombok:1.18.38'
    annotationProcessor 'org.projectlombok:lombok:1.18.38'
    implementation 'org.mapstruct:mapstruct:1.6.3'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.6.3'
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'

    runtimeOnly 'org.postgresql:postgresql:42.7.7'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.junit.jupiter:junit-jupiter-api'
    testImplementation 'org.junit.jupiter:junit-jupiter-engine'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    testImplementation 'org.testcontainers:testcontainers:1.20.4'
    testImplementation 'org.testcontainers:junit-jupiter:1.20.4'
    testImplementation 'org.testcontainers:postgresql:1.20.4'
    testImplementation 'org.wiremock:wiremock-standalone:3.9.2'
}

compileJava {
    options.annotationProcessorPath = configurations.annotationProcessor
    options.compilerArgs += [
        '-Amapstruct.defaultComponentModel=spring',
        '-Amapstruct.unmappedTargetPolicy=IGNORE'
    ]
}

tasks.named('test') {
    useJUnitPlatform()
}
```

### 2.3 Multi-Module Project Structure (When Needed)

Use Gradle multi-module only when a service boundary or shared library cannot be maintained effectively in a single
module.

```
twitter/
├── build.gradle
├── settings.gradle
├── services/
│   ├── users-api/
│   └── tweet-api/
└── shared/
    ├── common-lib/
    └── database/
```

### 2.4 When to switch to multi-module

Switch from single-module to multi-module when one or more criteria are met:

- Multiple deployable services must be built from one repository
- Shared code (DTOs, clients, utility libraries) needs independent versioning boundaries
- Build performance and ownership improve from module isolation
- Team structure requires explicit module contracts and dependency control

### 2.5 Key Gradle Practices

- **Use BOM (Bill of Materials)** for dependency version management
- **Configure annotation processors** explicitly for Lombok and MapStruct
- **Use Java toolchain** for consistent Java version across modules
- **Apply dependency management** at project level (single-module) or subproject level (multi-module)

---

## 3. Java 24 Features

### 3.1 Records for DTOs

**Always use Records for DTOs** instead of classes:

```java
/**
 * Data Transfer Object for user creation requests.
 *
 * @param login     unique login name for user authentication
 * @param firstName user's first name
 * @param lastName  user's last name
 * @param email     user's email address
 * @param password  user's password (will be hashed)
 * @author geron
 * @version 1.0
 */
@Schema(name = "UserRequest", description = "Data structure for creating new users")
public record UserRequestDto(
        @NotBlank(message = "Login cannot be blank")
        @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
        String login,

        String firstName,

        String lastName,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
    ) {
}
```

**Response DTO example:**

```java
public record UserResponseDto(
    UUID id,
    String login,
    String firstName,
    String lastName,
    String email,
    UserStatus status,
    UserRole role,
    LocalDateTime createdAt
) {
}
```

**Benefits:**

- Immutability by default
- Concise syntax
- Automatic equals/hashCode/toString
- Perfect for DTOs

### 3.2 Text Blocks

**Use text blocks for multi-line strings** (Java 15+):

```java
@Schema(
    name = "UserRequest",
    description = """
        Data structure for creating new users in the system.
        This DTO includes validation constraints to ensure data
        integrity and security requirements are met.
        """,
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
```

### 3.3 Pattern Matching

Use pattern matching where appropriate (Java 21+):

```java
// Pattern matching for instanceof
if(response instanceof
ResponseEntity<?> responseEntity){
    // Use responseEntity directly
    }
```

### 3.4 Sealed Classes (if applicable)

Use sealed classes for restricted inheritance hierarchies:

```java
public sealed class ValidationException
    permits UniquenessValidationException,
    BusinessRuleValidationException,
    FormatValidationException {
    // ...
}
```

---

## 4. Spring Boot 3.5.5 Practices

### 4.1 Application Structure

**Main Application Class:**

```java

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 4.2 Dependency Injection

**Always use constructor injection with Lombok:**

```java

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final UserValidator userValidator;

    // Methods...
}
```

**Benefits:**

- Immutable dependencies
- No need for @Autowired
- Clear dependencies
- Easy testing

### 4.3 Configuration Classes

**Use @Configuration for configuration:**

```java
/**
 * Configuration class for OpenAPI/Swagger documentation.
 *
 * @author geron
 * @version 1.0
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI usersApiOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Twitter Users API")
                .description("REST API for user management")
                .version("1.0.0"))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8081")
                    .description("Local development server")
            ));
    }
}
```

### 4.4 Actuator and Monitoring

- Include `spring-boot-starter-actuator` for health checks
- Use Micrometer for metrics and tracing
- Configure appropriate endpoints in `application.yml`

---

## 5. Architectural Patterns

### 5.1 Layered Architecture

**Standard layers (top to bottom):**

1. **Controller Layer** - HTTP request/response handling
2. **Service Layer** - Business logic
3. **Repository Layer** - Data access
4. **Entity Layer** - Domain models

### 5.2 API Interface Separation

**Separate OpenAPI interface from controller implementation:**

```java
// UserApi.java - OpenAPI interface with annotations
@Tag(name = "User Management", description = "API for managing users")
public interface UserApi {

    @Operation(summary = "Get user by ID", description = "...")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found")
    })
    ResponseEntity<UserResponseDto> getUserById(
        @Parameter(description = "Unique identifier of the user", required = true)
        UUID id
    );
}

// UserController.java - Implementation
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") UUID id) {
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

Add **`@LoggableRequest`** on controller methods when you need request logging (see **§10.2**); keep OpenAPI details on the interface (**§9.1**) or on DTOs (**§9.2**).

**Benefits:**

- Clean separation of concerns
- OpenAPI annotations don't clutter controller
- Easy to maintain API documentation

### 5.3 Service Interface Pattern

**Always define service interface and implementation:**

```java
// UserService.java - Interface
public interface UserService {
    Optional<UserResponseDto> getUserById(UUID id);

    Page<UserResponseDto> findAll(UserFilter userFilter, Pageable pageable);

    UserResponseDto createUser(UserRequestDto userRequest);
    // ...
}

// UserServiceImpl.java - Implementation
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    // Implementation...
}
```

### 5.4 Gateway Pattern for Inter-Service Communication

**Use Gateway pattern for external service calls:**

```java
// UsersApiClient.java - Feign Client
@FeignClient(
    name = "users-api",
    url = "${app.users-api.base-url:http://localhost:8081}",
    path = "/api/v1/users"
)
public interface UsersApiClient {
    @GetMapping("/{userId}/exists")
    UserExistsResponseDto existsUser(@PathVariable("userId") UUID userId);
}

// UserGateway.java - Gateway wrapper
@Component
@RequiredArgsConstructor
@Slf4j
public class UserGateway {

    private final UsersApiClient usersApiClient;

    public boolean existsUser(UUID userId) {
        if (userId == null) {
            log.warn("Attempted to check existence of null user ID");
            return false;
        }

        try {
            UserExistsResponseDto response = usersApiClient.existsUser(userId);
            return response.exists();
        } catch (Exception ex) {
            log.debug("User {} does not exist: {}", userId, ex.getMessage());
            return false;
        }
    }
}
```

**Benefits:**

- Abstraction over HTTP client
- Error handling in one place
- Easy to mock in tests
- Can add retry logic, circuit breakers, etc.

### 5.5 Feign Client Configuration

**Enable Feign Clients:**

```java

@Configuration
@EnableFeignClients(basePackages = "com.twitter.client")
public class FeignConfig {
}
```

### 5.6 Data Storage Strategy

**Select one primary data storage per service based on business requirements:**

- Use **PostgreSQL** when service needs ACID transactions, relational modeling, joins, and strict consistency
- Use **MongoDB** when service needs flexible schema, document aggregation, and rapid model evolution
- Avoid mixing PostgreSQL and MongoDB in the same service unless there is a clear architectural justification
- Document storage choice in service README and keep only relevant dependencies/repositories

**Storage decision matrix:**

| Criterion              | PostgreSQL               | MongoDB                                |
|------------------------|--------------------------|----------------------------------------|
| Data model             | Relational               | Document-oriented                      |
| Schema evolution       | Controlled migrations    | Flexible schema                        |
| Transaction complexity | Strong                   | Moderate to strong (depends on design) |
| Typical fit            | Core transactional flows | Content, feed, event-like data         |

---

## 6. DTO and Persistence Models

### 6.1 DTOs as Records

Use Java Records for all DTOs. Full request and response examples are in **§3.1 Records for DTOs**; Bean Validation on DTO fields and `@Valid` on controllers are covered in **§6.5 Bean Validation**.

### 6.2 JPA Entities (PostgreSQL option)

**Use Lombok annotations for entities:**

```java

@Entity
@Table(name = "users")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "login", unique = true, nullable = false)
    private String login;

    // Use @CreationTimestamp for audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

**Key practices:**

- Use `@Data` for getters/setters
- Use `@Accessors(chain = true)` for fluent API
- Use `@CreationTimestamp` and `@UpdateTimestamp` for audit fields
- Always specify `@Table(name = "...")` explicitly

### 6.3 MapStruct Mappers

**Use MapStruct for entity-DTO conversion:**

```java

@Mapper
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true)
    User toUser(UserRequestDto userRequestDto);

    UserResponseDto toUserResponseDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "passwordSalt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateUserFromUpdateDto(UserUpdateDto userUpdateDto, @MappingTarget User user);
}
```

**Key practices:**

- Use `@Mapper` interface (not abstract class)
- Ignore service-managed fields (id, timestamps, etc.)
- Use `@MappingTarget` for update operations
- Configure MapStruct annotation processor options in `build.gradle` as shown in **§2.2** (`compileJava` / `options.compilerArgs`).

### 6.4 MongoDB Documents (MongoDB option)

**Use @Document for MongoDB persistence models:**

```java

@Document(collection = "users")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String login;

    @Indexed(unique = true)
    private String email;

    @CreatedDate
    private Instant createdAt;
}
```

**Key practices:**

- Use `@Document(collection = "...")` explicitly
- Define indexes with `@Indexed` and use compound indexes when required by query patterns
- Use TTL indexes only for truly expiring data (sessions, temporary tokens, etc.)

### 6.5 Bean Validation

**Use Bean Validation annotations on DTO fields** (see **§3.1** for a full example). **In controllers, use `@Valid`** to trigger validation:

```java
@PostMapping
public UserResponseDto createUser(@RequestBody @Valid UserRequestDto userRequest) {
    return userService.createUser(userRequest);
}
```

---

## 7. Exception Handling

### 7.1 Global Exception Handler

**Use @RestControllerAdvice for centralized exception handling:**

```java

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(ResponseStatusException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            ex.getStatusCode(),
            ex.getReason() != null ? ex.getReason() : "Request failed"
        );
        problemDetail.setTitle(HttpStatus.valueOf(ex.getStatusCode().value()).getReasonPhrase());
        problemDetail.setType(URI.create("https://example.com/errors/request-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(UniquenessValidationException.class)
    public ProblemDetail handleUniquenessValidationException(UniquenessValidationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Uniqueness Validation Error");
        problemDetail.setType(URI.create("https://example.com/errors/uniqueness-validation"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("fieldName", ex.getFieldName());
        problemDetail.setProperty("fieldValue", ex.getFieldValue());
        return problemDetail;
    }

    // Other exception handlers...
}
```

### 7.2 Custom Exception Hierarchy

**Create specific exception types:**

```java
// Base exception
public class ValidationException extends RuntimeException {
    private final ValidationType validationType;
    // ...
}

// Specific exceptions
public class UniquenessValidationException extends ValidationException {
    private final String fieldName;
    private final String fieldValue;
    // ...
}

public class BusinessRuleValidationException extends ValidationException {
    private final String ruleName;
    private final String context;
    // ...
}

public class FormatValidationException extends ValidationException {
    private final String fieldName;
    private final String constraintName;
    // ...
}
```

### 7.3 HTTP Status Codes

**Standard status code usage:**

- `200 OK` - Successful GET, PUT, PATCH
- `201 Created` - Successful POST
- `400 Bad Request` - Validation errors, format errors
- `404 Not Found` - Resource not found
- `409 Conflict` - Uniqueness violations, business rule violations
- `500 Internal Server Error` - Unexpected server errors

### 7.4 ProblemDetail (RFC 7807)

**Always use ProblemDetail for error responses:**

- Follows RFC 7807 standard
- Provides structured error information
- Includes type, title, status, detail, and custom properties

---

## 8. Validation

### 8.1 Validation Layers

**Three-layer validation approach:**

1. **Bean Validation** - On DTOs (automatic)
2. **Custom Validators** - Business logic validation
3. **Repository Validation** - Uniqueness checks

### 8.2 Custom Validator Pattern

**Separate validator interface and implementation:**

```java
// UserValidator.java - Interface
public interface UserValidator {
    void validateForCreate(UserRequestDto userRequest);

    void validateForUpdate(UUID userId, UserUpdateDto userUpdate);

    void validateUniqueness(String login, String email, UUID excludeUserId);

    void validateAdminDeactivation(UUID userId);
    // ...
}

// UserValidatorImpl.java - Implementation
@Slf4j
@Component
@RequiredArgsConstructor
public class UserValidatorImpl implements UserValidator {

    private final UserRepository userRepository;
    private final Validator validator; // Bean Validation validator

    @Override
    public void validateForCreate(UserRequestDto userRequest) {
        validateUniqueness(userRequest.login(), userRequest.email(), null);
    }

    @Override
    public void validateUniqueness(String login, String email, UUID excludeUserId) {
        if (!ObjectUtils.isEmpty(login)) {
            boolean loginExists = excludeUserId != null
                ? userRepository.existsByLoginAndIdNot(login, excludeUserId)
                : userRepository.existsByLogin(login);

            if (loginExists) {
                log.warn("Uniqueness validation failed: login '{}' already exists", login);
                throw new UniquenessValidationException("login", login);
            }
        }
        // Similar for email...
    }
}
```

### 8.3 Validation in Service Layer

**Call validators in service methods:**

```java

@Override
public UserResponseDto createUser(UserRequestDto userRequest) {
    userValidator.validateForCreate(userRequest);

    User user = userMapper.toUser(userRequest);
    // ... rest of logic
}
```

---

## 9. OpenAPI/Swagger

### 9.1 API Interface Documentation

**Document API interfaces with OpenAPI annotations. Use `@ApiResponse` only for the valid `200` case and do not provide examples for other status codes:**

```java

@Tag(name = "User Management", description = "API for managing users")
public interface UserApi {

    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a specific user by their unique identifier. Returns 404 if user not found."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDto.class),
                examples = @ExampleObject(
                    name = "User Response",
                    value = """
                        {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "login": "john_doe",
                          "firstName": "John",
                          "lastName": "Doe",
                          "email": "john.doe@example.com",
                          "status": "ACTIVE",
                          "role": "USER"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<UserResponseDto> getUserById(
        @Parameter(description = "Unique identifier of the user", required = true)
        UUID id
    );
}
```

### 9.2 DTO Documentation

**Document DTOs with `@Schema`** at class and field level. The Record shape and validation constraints are defined in **§3.1**; add `@Schema` for OpenAPI titles, descriptions, examples, and required mode:

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
    @Schema(description = "Unique login name", example = "jane_smith", requiredMode = Schema.RequiredMode.REQUIRED)
    String login
    // ... remaining components per §3.1
) {
}
```

### 9.3 OpenAPI Configuration

Define the `OpenAPI` bean in a `@Configuration` class as shown in **§4.3 Configuration Classes** (`OpenApiConfig`). Do not duplicate that snippet here.

---

## 10. Logging

### 10.1 SLF4J with Lombok

**Use @Slf4j annotation:**

```java

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    public Optional<UserResponseDto> inactivateUser(UUID id) {
        return userRepository.findById(id).map(user -> {
            userValidator.validateAdminDeactivation(id);
            user.setStatus(UserStatus.INACTIVE);
            User updatedUser = userRepository.save(user);
            log.info("User with ID {} has been successfully deactivated", id);
            return userMapper.toUserResponseDto(updatedUser);
        });
    }
}
```

### 10.2 AOP for Request Logging

**Use `@LoggableRequest` on controller methods** for automatic request logging. Method signatures and mappings match **§5.2** (API interface separation) and **§9.1** (OpenAPI on the interface); only the logging aspect is shown here:

```java
@LoggableRequest
@Override
public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") UUID id) {
    // Same implementation as in §5.2
}
```

For **`createUser`**, add **`@LoggableRequest(hideFields = {"password"})`** above the same **`@PostMapping`** method that uses **`@Valid`** as in **§6.5** (hides sensitive fields from logs).

### 10.3 Log Levels

**Use appropriate log levels:**

- `ERROR` - System errors, exceptions
- `WARN` - Validation failures, business rule violations
- `INFO` - Important business events, request/response logging
- `DEBUG` - Detailed debugging information

---

## 11. Testing

### 11.1 Test Structure

**Mirror main source structure:**

```
src/
├── main/
│   └── java/com/twitter/
└── test/
    └── java/com/twitter/
```

### 11.2 JUnit 5

**Use JUnit 5 for all tests:**

```java

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldGetUserById() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User();
        UserResponseDto dto = new UserResponseDto(...);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(user)).thenReturn(dto);

        // When
        Optional<UserResponseDto> result = userService.getUserById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
    }
}
```

### 11.3 Testcontainers for Integration Tests

**Use Testcontainers for integration tests with selected storage:**

```java

@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        // Test implementation
    }
}
```

```java

@SpringBootTest
@Testcontainers
class UserDocumentRepositoryIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @Autowired
    private UserDocumentRepository userDocumentRepository;

    @Test
    void shouldSaveAndFindDocument() {
        // Test implementation
    }
}
```

### 11.4 Test Dependencies

**Required test dependencies:**

```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.junit.jupiter:junit-jupiter-api'
testImplementation 'org.junit.jupiter:junit-jupiter-engine'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
testImplementation 'org.testcontainers:junit-jupiter'
testImplementation 'org.testcontainers:postgresql'
testImplementation 'org.testcontainers:mongodb'
```

---

## 12. Repositories

### 12.1 Spring Data JPA

**Extend JpaRepository and JpaSpecificationExecutor:**

```java
/**
 * Repository interface for user data access operations.
 *
 * @author geron
 * @version 1.0
 */
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    // Derived Query Methods - NO JavaDoc required
    long countByRoleAndStatus(UserRole role, UserStatus status);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    boolean existsByLoginAndIdNot(String login, UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);
}
```

### 12.2 Spring Data MongoDB

**Extend MongoRepository for document persistence:**

```java
public interface UserDocumentRepository extends MongoRepository<UserDocument, String> {

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    Optional<UserDocument> findByLogin(String login);
}
```

### 12.3 Derived Query Methods

**Key points:**

- **DO NOT** add JavaDoc to derived query methods (self-documenting)
- Use clear, descriptive method names following Spring Data conventions
- Methods like `findBy*`, `existsBy*`, `countBy*` are obvious from naming

### 12.4 JPA Specifications

**Use Specifications for dynamic queries:**

```java
// Filter DTO
public record UserFilter(String firstNameContains, String lastNameContains, UserRole role) {

    public Specification<User> toSpecification() {
        return firstNameContainsSpec()
            .and(lastNameContainsSpec())
            .and(roleSpec());
    }

    private Specification<User> firstNameContainsSpec() {
        return ((root, _, cb) -> StringUtils.hasText(firstNameContains)
            ? cb.like(root.get("firstName"), "%" + firstNameContains + "%")
            : null);
    }
    // ...
}

// Usage in service
Page<UserResponseDto> findAll(UserFilter userFilter, Pageable pageable) {
    return userRepository.findAll(userFilter.toSpecification(), pageable)
        .map(userMapper::toUserResponseDto);
}
```

### 12.5 Custom Query Methods

**Document custom methods with @Query:**

```java

@Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status")
List<User> findActiveUsersByRole(@Param("role") UserRole role, @Param("status") UserStatus status);
```

---

## 13. JavaDoc Standards

### 13.1 Reference to Standards

**Follow the established JavaDoc standards:**

- See `standards/STANDART_JAVADOC.md` for detailed standards

### 13.2 Required Tags

**All public classes and methods must have:**

- `@author geron` - Author information
- `@version 1.0` - Version number
- `@param` - For each parameter
- `@return` - For return value
- `@throws` - For exceptions

### 13.3 Documentation Structure

**Standard structure:**

```java
/**
 * Brief description of the class/method purpose.
 * <p>
 * Detailed description explaining responsibilities,
 * key features, and usage context.
 * <p>
 * Additional paragraphs for complex explanations.
 *
 * @param paramName description of the parameter
 * @return description of the return value
 * @throws ExceptionType description of when this exception is thrown
 * @author geron
 * @version 1.0
 */
```

### 13.4 Cross-References

**Use @see for interface implementations:**

```java
/**
 * @see UserApi#getUserById
 */
@Override
public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") UUID id) {
    // Implementation
}
```

---

## 14. Additional Best Practices

### 14.1 Transaction Management

**Use @Transactional for service methods that modify data:**

```java

@Override
@Transactional
public TweetResponseDto createTweet(CreateTweetRequestDto requestDto) {
    tweetValidator.validateForCreate(requestDto);
    Tweet tweet = tweetMapper.toEntity(requestDto);
    Tweet savedTweet = tweetRepository.saveAndFlush(tweet);
    return tweetMapper.toResponseDto(savedTweet);
}
```

### 14.2 Optional Usage

**Use Optional for methods that may return null:**

```java

@Override
public Optional<UserResponseDto> getUserById(UUID id) {
    return userRepository.findById(id)
        .map(userMapper::toUserResponseDto);
}
```

### 14.3 Pagination

**Use Spring Data Pageable for pagination:**

```java

@GetMapping
public PagedModel<UserResponseDto> findAll(
    @ModelAttribute UserFilter userFilter,
    @PageableDefault(size = 10) Pageable pageable
) {
    Page<UserResponseDto> users = userService.findAll(userFilter, pageable);
    return new PagedModel<>(users);
}
```

### 14.4 UUID for IDs

**Always use UUID for entity IDs:**

```java

@Id
@GeneratedValue(generator = "UUID")
@Column(name = "id", updatable = false, nullable = false)
private UUID id;
```

### 14.5 Enums

**Use enums for fixed sets of values:**

```java
public enum UserRole {
    ADMIN,
    MODERATOR,
    USER
}

// In entity
@Enumerated(EnumType.STRING)
@Column(name = "role", nullable = false)
private UserRole role;
```

---

## 15. Code Quality Checklist

Before submitting code, ensure:

- [ ] All public classes and methods have JavaDoc
- [ ] Code follows naming conventions
- [ ] DTOs are Records
- [ ] Entities use Lombok annotations
- [ ] Services have interfaces and implementations
- [ ] Validators are separated from services
- [ ] Exceptions use ProblemDetail format
- [ ] OpenAPI annotations are complete
- [ ] Logging is appropriate
- [ ] Tests are written for new functionality
- [ ] Selected storage (PostgreSQL or MongoDB) is explicitly documented for the service
- [ ] Only storage-relevant dependencies, repositories, and tests are included
- [ ] No hardcoded values (use constants or properties)
- [ ] Error messages are clear and helpful

---

## 16. Version History

- **v1.0** (2025-01-27): Initial version based on analysis of users-api, tweet-api, and common-lib

---

## References

- [JavaDoc Standards](./STANDART_JAVADOC.md) (templates and tag rules)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MapStruct Documentation](https://mapstruct.org/)
- [Lombok Documentation](https://projectlombok.org/)

