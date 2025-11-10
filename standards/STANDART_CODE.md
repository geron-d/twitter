# Code Standards for Twitter Microservices Project

## Overview

This document defines the coding standards and best practices for the Twitter microservices project. These standards are based on the analysis of existing services (users-api, tweet-api, common-lib) and should be followed when writing new code or modifying existing code.

**Technology Stack:**
- Java 24
- Spring Boot 3.5.5
- Gradle (Multi-module project)
- PostgreSQL
- MapStruct
- Lombok
- OpenAPI/Swagger

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
├── dto/                          # Data Transfer Objects
│   ├── request/                  # Request DTOs
│   ├── response/                 # Response DTOs
│   └── filter/                   # Filter DTOs
├── entity/                        # JPA entities
├── mapper/                        # MapStruct mappers
├── repository/                    # JPA repositories
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

### 2.1 Multi-Module Project Structure

The project uses a Gradle multi-module structure:

```
twitter/
├── build.gradle                  # Root build file
├── settings.gradle               # Module definitions
├── services/
│   ├── users-api/
│   └── tweet-api/
└── shared/
    ├── common-lib/
    └── database/
```

### 2.2 Root build.gradle

```gradle
plugins {
    id 'java-library'
    id 'org.springframework.boot' version '3.5.5' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}

group = 'com.twitter'
version = '0.0.1-SNAPSHOT'

allprojects {
    apply plugin: 'java'
    
    group = rootProject.group
    version = rootProject.version
    
    java {
        sourceCompatibility = '24'
        targetCompatibility = '24'
        toolchain {
            languageVersion = JavaLanguageVersion.of(24)
        }
    }
    
    repositories {
        mavenCentral()
        maven { url 'https://repo.spring.io/milestone' }
        maven { url 'https://repo.spring.io/snapshot' }
    }
}

subprojects {
    apply plugin: 'io.spring.dependency-management'
    
    dependencyManagement {
        imports {
            mavenBom "org.springframework.boot:spring-boot-dependencies:3.5.5"
            mavenBom "org.springframework.cloud:spring-cloud-dependencies:2025.0.0"
            mavenBom "org.testcontainers:testcontainers-bom:1.21.3"
        }
        
        dependencies {
            // Common dependencies managed here
        }
    }
}
```

### 2.3 Service Module build.gradle

```gradle
plugins {
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
    id 'java'
}

dependencies {
    // Shared modules
    implementation project(':shared:common-lib')
    implementation project(':shared:database')
    
    // Spring Boot starters
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    
    // Annotation processors
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    implementation 'org.mapstruct:mapstruct'
    annotationProcessor 'org.mapstruct:mapstruct-processor'
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding'
}

compileJava {
    options.annotationProcessorPath = configurations.annotationProcessor
    options.compilerArgs += [
        '-Amapstruct.defaultComponentModel=spring',
        '-Amapstruct.unmappedTargetPolicy=IGNORE'
    ]
}

springBoot {
    mainClass = 'com.twitter.Application'
}
```

### 2.4 Key Gradle Practices

- **Use BOM (Bill of Materials)** for dependency version management
- **Configure annotation processors** explicitly for Lombok and MapStruct
- **Use Java toolchain** for consistent Java version across modules
- **Apply dependency management** at subproject level

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
if (response instanceof ResponseEntity<?> responseEntity) {
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
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
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
    
    @LoggableRequest
    @GetMapping("/{id}")
    @Override
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") UUID id) {
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

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

---

## 6. DTO and Entity

### 6.1 DTOs as Records

**Always use Records for DTOs:**

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

### 6.2 JPA Entities

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
- Configure in `build.gradle`:
  ```gradle
  options.compilerArgs += [
      '-Amapstruct.defaultComponentModel=spring',
      '-Amapstruct.unmappedTargetPolicy=IGNORE'
  ]
  ```

### 6.4 Bean Validation

**Use Bean Validation annotations on DTOs:**

```java
public record UserRequestDto(
    @NotBlank(message = "Login cannot be blank")
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    String login,
    
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password
) {
}
```

**In controllers, use @Valid:**

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

**Document API interfaces with OpenAPI annotations:**

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
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = "application/problem+json")
        )
    })
    ResponseEntity<UserResponseDto> getUserById(
        @Parameter(description = "Unique identifier of the user", required = true)
        UUID id
    );
}
```

### 9.2 DTO Documentation

**Document DTOs with @Schema:**

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
    String login,
    // ...
) {
}
```

### 9.3 OpenAPI Configuration

**Configure OpenAPI in @Configuration class:**

```java
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

**Use @LoggableRequest annotation for automatic logging:**

```java
@LoggableRequest
@GetMapping("/{id}")
@Override
public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") UUID id) {
    return userService.getUserById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}

// Hide sensitive fields
@LoggableRequest(hideFields = {"password"})
@PostMapping
public UserResponseDto createUser(@RequestBody @Valid UserRequestDto userRequest) {
    return userService.createUser(userRequest);
}
```

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

**Use Testcontainers for database integration tests:**

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

### 11.4 Test Dependencies

**Required test dependencies:**

```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.junit.jupiter:junit-jupiter-api'
testImplementation 'org.junit.jupiter:junit-jupiter-engine'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
testImplementation 'org.testcontainers:junit-jupiter'
testImplementation 'org.testcontainers:postgresql'
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

### 12.2 Derived Query Methods

**Key points:**

- **DO NOT** add JavaDoc to derived query methods (self-documenting)
- Use clear, descriptive method names following Spring Data conventions
- Methods like `findBy*`, `existsBy*`, `countBy*` are obvious from naming

### 12.3 JPA Specifications

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

### 12.4 Custom Query Methods

**Document custom methods with @Query:**

```java
@Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status")
List<User> findActiveUsersByRole(@Param("role") UserRole role, @Param("status") UserStatus status);
```

---

## 13. JavaDoc Standards

### 13.1 Reference to Standards

**Follow the established JavaDoc standards:**

- See `standards/JAVADOC_STANDARDS.md` for detailed standards
- See `standards/JAVADOC_TEMPLATES.md` for templates

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
- [ ] No hardcoded values (use constants or properties)
- [ ] Error messages are clear and helpful

---

## 16. Version History

- **v1.0** (2025-01-27): Initial version based on analysis of users-api, tweet-api, and common-lib

---

## References

- [JavaDoc Standards](./JAVADOC_STANDARDS.md)
- [JavaDoc Templates](./JAVADOC_TEMPLATES.md)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MapStruct Documentation](https://mapstruct.org/)
- [Lombok Documentation](https://projectlombok.org/)

