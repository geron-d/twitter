# JavaDoc Documentation Standards for Twitter Microservices Project

## Overview

This document defines the JavaDoc documentation standards for the Twitter microservices project.

**Scope:**
- All public classes, interfaces, and methods
- DTOs (Records)
- Repository interfaces
- Annotations
- Enums
- Exceptions
- Controllers and Services

**Technology Stack:**
- Java 24
- Spring Boot 3.5.5
- Gradle (Multi-module project)

---

## General Principles

### Language

- **All documentation must be written in English**
- Use clear, concise language
- Avoid technical jargon when possible
- Use present tense for descriptions
- Use imperative mood for instructions

### Structure

- Start with a brief description of the element (first sentence)
- Follow with detailed information using `<p>` tags
- Use standard JavaDoc tags consistently
- Use lists (`<ul>`, `<ol>`) for multiple items or steps

### Required Tags

All public classes, interfaces, and methods must include:

- `@author geron` - Author information (always use "geron")
- `@version 1.0` - Version number (use 1.0 for initial documentation)

### Formatting Guidelines

- Use `<p>` tags to separate paragraphs
- Use `<pre>{@code ... }</pre>` for multi-line code examples
- Use `{@code code}` for inline code references
- Use `<ul>` and `<li>` for unordered lists
- Use `<ol>` and `<li>` for ordered lists
- Use `{@link ClassName}` or `{@link ClassName#method}` for cross-references

---

## Documentation Standards by Element Type

### 1. Classes and Interfaces

#### Required Tags
- `@author geron`
- `@version 1.0`

#### Template
```java
/**
 * Brief description of the class purpose and functionality.
 * <p>
 * Detailed description explaining the class responsibilities,
 * key features, and usage context within the Twitter ecosystem.
 * Include information about when to use this class and how it
 * integrates with other components.
 *
 * @author geron
 * @version 1.0
 */
public class ExampleClass {
    // implementation
}
```

#### Examples from Project

**Service Interface:**
```java
/**
 * Service interface for user management in Twitter microservices.
 * <p>
 * This interface defines the contract for user management services, providing
 * business logic for CRUD operations with users, including validation,
 * password hashing, and business rule enforcement.
 *
 * @author geron
 * @version 1.0
 */
public interface UserService {
    // methods
}
```

**Service Implementation:**
```java
/**
 * Implementation of the user management service.
 * <p>
 * This service provides business logic for CRUD operations with users,
 * including creation, updating, deactivation, and role management. It handles
 * data validation, password hashing, and business rule enforcement.
 *
 * @author geron
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    // implementation
}
```

**Aspect Class:**
```java
/**
 * Aspect for automatic HTTP request/response logging in Twitter microservices.
 * <p>
 * This aspect provides comprehensive logging functionality across multiple
 * methods and classes. It uses Spring AOP to intercept method calls annotated
 * with @LoggableRequest and logs detailed information about HTTP requests and
 * responses, including headers, body content, and response status codes.
 *
 * @author geron
 * @version 1.0
 */
@Aspect
@Component
public class LoggableRequestAspect {
    // implementation
}
```

**OpenAPI Interface:**
```java
/**
 * OpenAPI interface for Tweet Management API.
 *
 * @author geron
 * @version 1.0
 */
@Tag(name = "Tweet Management", description = "API for managing tweets in the Twitter system")
public interface TweetApi {
    // methods
}
```

---

### 2. Methods

#### Required Tags
- `@param` - For each parameter (if any)
- `@return` - For return value description
- `@throws` - For each exception that can be thrown

#### Template for Public Methods
```java
/**
 * Brief description of what the method does.
 * <p>
 * Detailed description of the method's behavior, including
 * any side effects, performance considerations, or important notes.
 * Include information about the method's role in the overall
 * system architecture.
 *
 * @param paramName description of the parameter and its constraints
 * @param anotherParam description of another parameter
 * @return description of the return value and its possible states
 * @throws ExceptionType description of when this exception is thrown
 * @throws AnotherException description of another possible exception
 */
public ReturnType methodName(ParamType paramName, AnotherType anotherParam) 
        throws ExceptionType, AnotherException {
    // implementation
}
```

#### Examples from Project

**Service Interface Method:**
```java
/**
 * Retrieves a user by their unique identifier.
 * <p>
 * Returns an empty Optional if the user does not exist
 * or has been deactivated.
 *
 * @param id the unique identifier of the user
 * @return Optional containing user data or empty if not found
 */
Optional<UserResponseDto> getUserById(UUID id);
```

**Service Method with Business Logic:**
```java
/**
 * Creates a new user in the system.
 * <p>
 * This method creates a new user with the provided data. The system
 * automatically sets the status to ACTIVE and role to USER. The password
 * is securely hashed using PBKDF2 algorithm with a random salt.
 *
 * @param userRequest user data for creation
 * @return the created user data
 * @throws ValidationException if validation fails or uniqueness conflict occurs
 */
UserResponseDto createUser(UserRequestDto userRequest);
```

**Method with Multiple Operations:**
```java
/**
 * Creates a new tweet from the provided request data.
 * <p>
 * This method performs the following operations:
 * 1. Validates the request data
 * 2. Checks if the user exists (via users-api integration)
 * 3. Converts the request DTO to Tweet entity
 * 4. Saves the tweet to the database
 * 5. Converts the saved entity to response DTO
 * 6. Returns the response DTO
 *
 * @param requestDto the tweet creation request containing content and userId
 * @return TweetResponseDto containing the created tweet data
 * @throws FormatValidationException       if content validation fails
 * @throws BusinessRuleValidationException if user doesn't exist
 */
TweetResponseDto createTweet(CreateTweetRequestDto requestDto);
```

**Private Helper Method:**
```java
/**
 * Sets a hashed password for a user.
 * <p>
 * This private method generates a random salt and hashes the password
 * using PBKDF2 algorithm. It stores both the password hash and salt
 * in Base64 encoding for secure storage.
 *
 * @param user     the user to set the password for
 * @param password the password in plain text
 * @throws ResponseStatusException if salt generation or password hashing fails
 */
private void setPassword(User user, String password) {
    // implementation
}
```

**Method with Code Example:**
```java
/**
 * Logs detailed information about the HTTP request.
 * <p>
 * This method extracts and logs comprehensive request information including
 * HTTP method, URI, headers, and body content. It respects the configuration
 * from the @LoggableRequest annotation to determine what information should
 * be logged and which fields should be hidden for security purposes.
 *
 * <p>Log format examples:</p>
 * <pre>
 * ### REQUEST POST /api/users ,Headers: Content-Type: application/json; Accept: ** , Body: {"name":"John","email":"john@example.com"}
 * ### REQUEST GET /api/users/123
 * </pre>
 *
 * @param request             the HTTP servlet request
 * @param proceedingJoinPoint the AOP join point containing method information
 */
private void logRequestDetails(HttpServletRequest request, ProceedingJoinPoint proceedingJoinPoint) {
    // implementation
}
```

#### Implementation Methods Using @see

When implementing interface methods, use `@see` to reference the interface method:

```java
/**
 * @see UserService#getUserById
 */
@Override
public Optional<UserResponseDto> getUserById(UUID id) {
    return userRepository.findById(id).map(userMapper::toUserResponseDto);
}
```

**Note:** Implementation methods that simply delegate to the interface do not need full JavaDoc if they use `@see`. However, if the implementation adds significant logic or behavior, provide full documentation.

---

### 3. Records (DTOs)

#### Required Tags
- `@param` - For each record component parameter
- `@author geron`
- `@version 1.0`

#### Template
```java
/**
 * Data Transfer Object for [specific purpose].
 * <p>
 * This record represents the data structure used for [specific purpose]
 * in the system. It includes validation constraints to ensure data
 * integrity and security requirements are met.
 *
 * @param fieldName     description of the field
 * @param anotherField  description of another field
 * @author geron
 * @version 1.0
 */
@Schema(name = "ExampleRequest", description = "Data structure for...")
public record ExampleDto(
    /**
     * Description of what this field represents.
     * <p>
     * Additional details about constraints, format, or usage.
     */
    @Schema(description = "...", example = "...")
    @NotBlank(message = "...")
    String fieldName,
    
    // other fields
) {
}
```

#### Examples from Project

**Request DTO:**
```java
/**
 * Data Transfer Object for user creation requests.
 * <p>
 * This record represents the data structure used for creating new users
 * in the system. It includes validation constraints to ensure data
 * integrity and security requirements are met.
 *
 * @param login     unique login name for user authentication
 * @param firstName user's first name
 * @param lastName  user's last name
 * @param email     user's email address
 * @param password  user's password (will be hashed)
 * @author geron
 * @version 1.0
 */
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
    /**
     * Unique login name for user authentication.
     * <p>
     * This field must be unique across all users and is required for
     * authentication purposes. Must be between 3 and 50 characters.
     */
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
    
    /**
     * User's first name.
     */
    @Schema(
        description = "User's first name (optional)",
        example = "Jane",
        maxLength = 100
    )
    String firstName,
    
    // other fields...
) {
}
```

**Simpler DTO:**
```java
/**
 * Data Transfer Object for creating a new tweet.
 * <p>
 * This DTO contains validation rules for tweet content and user identification.
 *
 * @author geron
 * @version 1.0
 */
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
@Builder
public record CreateTweetRequestDto(
    @Schema(
        description = "The content of the tweet",
        example = "This is a sample tweet content",
        minLength = 1,
        maxLength = 280,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Tweet content cannot be empty")
    @Size(min = 1, max = 280, message = "Tweet content must be between 1 and 280 characters")
    String content,
    
    @Schema(
        description = "The ID of the user creating the tweet",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "User ID cannot be null")
    UUID userId
) {
}
```

**Key Points for DTOs:**
- Document the record itself with `@param` tags for all components
- Optionally document individual fields with JavaDoc comments
- Use `@Schema` annotations for OpenAPI documentation
- Include validation constraints in JavaDoc when relevant

---

### 4. Repository Interfaces

#### Documentation Rules for Derived Query Methods

Spring Data JPA automatically generates implementation for Derived Query Methods based on method names. These methods have obvious functionality from their names and **do NOT require JavaDoc documentation**.

**Key Principles:**
- Derived Query Methods are self-documenting through their naming convention
- Adding JavaDoc to these methods creates redundancy and maintenance overhead
- Focus documentation efforts on custom methods and complex business logic
- Use clear, descriptive method names instead of relying on documentation

#### Methods That Do NOT Require Documentation

The following method patterns are considered Derived Query Methods and should **NOT** be documented:

- `findBy*` - Find entities by property (e.g., `findByLogin`, `findByEmail`)
- `countBy*` - Count entities by property (e.g., `countByRole`, `countByStatus`)
- `existsBy*` - Check if entities exist by property (e.g., `existsByLogin`, `existsByEmail`)
- `deleteBy*` - Delete entities by property (e.g., `deleteByStatus`, `deleteByRole`)
- `*By*And*` - Methods with multiple conditions (e.g., `existsByLoginAndIdNot`)
- `*By*Or*` - Methods with OR conditions (e.g., `findByLoginOrEmail`)
- `*By*In*` - Methods with IN conditions (e.g., `findByIdIn`)
- `*By*Between*` - Methods with BETWEEN conditions (e.g., `findByCreatedAtBetween`)
- `*By*LessThan*` - Methods with comparison conditions (e.g., `findByAgeLessThan`)
- `*By*GreaterThan*` - Methods with comparison conditions (e.g., `findByAgeGreaterThan`)

#### Methods That DO Require Documentation

The following methods should always be documented:

- Custom query methods using `@Query` annotation
- Methods with complex business logic
- Methods that perform non-standard operations
- Methods that have side effects beyond simple CRUD operations
- Methods that require specific parameter validation or constraints

#### Template for Repository Interface

```java
/**
 * Repository interface for [entity] data access operations.
 * <p>
 * This repository provides data access methods for [Entity] entities.
 * It extends JpaRepository and JpaSpecificationExecutor for standard
 * CRUD operations and dynamic query capabilities.
 *
 * @author geron
 * @version 1.0
 */
public interface [Entity]Repository extends JpaRepository<[Entity], [ID]>, JpaSpecificationExecutor<[Entity]> {

    // Derived Query Methods - NO JavaDoc required
    long countByRoleAndStatus(UserRole role, UserStatus status);
    boolean existsByLogin(String login);
    boolean existsByEmail(String email);
    boolean existsByLoginAndIdNot(String login, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);
    
    // Custom methods - JavaDoc required
    /**
     * Custom method description.
     * <p>
     * Detailed description of what this custom method does and why it's needed.
     *
     * @param param description of the parameter
     * @return description of the return value
     * @throws ExceptionType description of when this exception is thrown
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status")
    List<User> findUsersByComplexCriteria(@Param("role") UserRole role, 
                                         @Param("status") UserStatus status);
}
```

#### Examples from Project

**Simple Repository (no custom methods):**
```java
/**
 * Repository interface for user data access operations.
 *
 * @author geron
 * @version 1.0
 */
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    long countByRoleAndStatus(UserRole role, UserStatus status);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    boolean existsByLoginAndIdNot(String login, UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);
}
```

**Repository with Custom Query:**
```java
/**
 * Repository interface for tweet data access operations.
 *
 * @author geron
 * @version 1.0
 */
@Repository
public interface TweetRepository extends JpaRepository<Tweet, UUID> {
    
    /**
     * Finds tweets by user ID with pagination support.
     * <p>
     * This method retrieves all tweets created by a specific user,
     * ordered by creation date in descending order (newest first).
     *
     * @param userId the unique identifier of the user
     * @param pageable pagination parameters
     * @return Page containing tweets for the specified user
     */
    @Query("SELECT t FROM Tweet t WHERE t.userId = :userId ORDER BY t.createdAt DESC")
    Page<Tweet> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);
}
```

---

### 5. Annotations

#### Required Tags
- `@author geron`

#### Template
```java
/**
 * Annotation for [specific functionality].
 * <p>
 * This annotation [detailed description of what it does and when to use it].
 * It provides configuration options for [specific behavior] and can be
 * applied to [target elements].
 *
 * @author geron
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExampleAnnotation {
    
    /**
     * Description of the annotation parameter.
     * <p>
     * Additional details about what this parameter controls and its default behavior.
     *
     * @return description of what this parameter controls
     */
    boolean parameterName() default true;
}
```

#### Example from Project

```java
/**
 * Annotation for automatic HTTP request/response logging in Twitter microservices.
 * <p>
 * This annotation enables automatic logging of HTTP requests and responses
 * for methods annotated with it. It provides configuration options for
 * controlling what information is logged and which fields should be hidden
 * for security purposes. The logging is performed by the LoggableRequestAspect
 * which intercepts method calls and logs request details including headers,
 * body content, and response information.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @LoggableRequest
 * public ResponseEntity<User> createUser(@RequestBody User user) {
 *     return userService.createUser(user);
 * }
 *
 * @LoggableRequest(printRequestBody = false, hideFields = {"password", "ssn"})
 * public ResponseEntity<User> updateUser(@RequestBody User user) {
 *     return userService.updateUser(user);
 * }
 * }</pre>
 *
 * @author geron
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoggableRequest {

    /**
     * Controls whether the request body should be logged.
     * <p>
     * When set to {@code true} (default), the complete request body will be
     * logged including all fields. When set to {@code false}, only the request
     * method and URI will be logged without the body content.
     *
     * <p>Use {@code false} for:</p>
     * <ul>
     *   <li>Large request bodies that would clutter logs</li>
     *   <li>Sensitive endpoints where body content should not be logged</li>
     *   <li>Performance-critical endpoints where logging overhead should be minimized</li>
     * </ul>
     *
     * @return {@code true} if request body should be logged, {@code false} otherwise
     */
    boolean printRequestBody() default true;

    /**
     * Array of field names that should be hidden in the logged request body.
     * <p>
     * This parameter allows you to specify sensitive fields that should be
     * replaced with "***" in the logged output. The hiding is performed
     * recursively on nested objects and arrays. Field names are case-sensitive
     * and should match exactly with the JSON property names.
     *
     * <p>Common fields to hide:</p>
     * <ul>
     *   <li>{@code "password"} - User passwords</li>
     *   <li>{@code "ssn"} - Social Security Numbers</li>
     *   <li>{@code "creditCard"} - Credit card information</li>
     *   <li>{@code "token"} - Authentication tokens</li>
     *   <li>{@code "secret"} - API secrets</li>
     * </ul>
     *
     * <p>Example:</p>
     * <pre>{@code
     * @LoggableRequest(hideFields = {"password", "ssn", "creditCard"})
     * public ResponseEntity<User> createUser(@RequestBody User user) {
     *     // password, ssn, and creditCard fields will be hidden in logs
     * }
     * }</pre>
     *
     * @return array of field names to hide in the logged output
     */
    String[] hideFields() default {};
}
```

---

### 6. Enums

#### Required Documentation
- Class-level documentation with `@author` and `@version`
- Documentation for each enum constant

#### Template
```java
/**
 * Enumeration of [specific concept] used in Twitter's [specific area].
 * <p>
 * This enum defines the possible values for [specific concept] and
 * provides a type-safe way to represent [specific states/options].
 * It is used throughout the Twitter system for [specific purposes].
 *
 * @author geron
 * @version 1.0
 */
public enum ExampleEnum {
    
    /**
     * Description of the first enum value.
     * Used when [specific condition] occurs.
     */
    FIRST_VALUE,
    
    /**
     * Description of the second enum value.
     * Used when [specific condition] occurs.
     */
    SECOND_VALUE
}
```

#### Example
```java
/**
 * Enumeration of validation types used in Twitter's validation framework.
 * <p>
 * This enum categorizes different types of validation errors that can occur
 * in the Twitter system, helping to provide appropriate error handling and
 * user feedback.
 *
 * @author geron
 * @version 1.0
 */
public enum ValidationType {
    
    /**
     * Uniqueness validation for preventing duplicate data.
     * Used for login, email, and other unique identifiers.
     */
    UNIQUENESS,
    
    /**
     * Business rule validation for enforcing domain-specific constraints.
     * Used for complex business logic validation.
     */
    BUSINESS_RULE,
    
    /**
     * Format validation for data structure and syntax checking.
     * Used for JSON parsing, Bean Validation, and format constraints.
     */
    FORMAT
}
```

---

### 7. Exceptions

#### Required Tags
- `@author geron`

#### Template
```java
/**
 * Exception thrown when [specific condition] occurs.
 * <p>
 * This exception is thrown when [detailed description of when
 * and why the exception occurs]. It provides additional context
 * through [specific methods/properties] to help with error handling.
 *
 * @author geron
 * @version 1.0
 */
public class ExampleException extends RuntimeException {
    // implementation
}
```

#### Example
```java
/**
 * Exception thrown when attempting to deactivate the last active administrator.
 * <p>
 * This exception prevents the system from being left without any active
 * administrators, which could lead to system lockout scenarios.
 *
 * @author geron
 * @version 1.0
 */
public class BusinessRuleValidationException extends ValidationException {
    // implementation
}
```

---

## Formatting Guidelines

### Paragraphs

Use `<p>` tags to separate paragraphs:

```java
/**
 * Brief description.
 * <p>
 * First detailed paragraph with additional information.
 * <p>
 * Second detailed paragraph with more context.
 */
```

### Code Examples

Use `<pre>{@code ... }</pre>` for multi-line code examples:

```java
/**
 * Method description.
 * <p>
 * Example usage:
 * <pre>{@code
 * ExampleClass example = new ExampleClass();
 * ReturnType result = example.methodName(paramValue);
 * }</pre>
 */
```

Use `{@code code}` for inline code references:

```java
/**
 * When set to {@code true}, the feature is enabled.
 * Use {@code false} to disable the feature.
 */
```

### Lists

Use `<ul>` and `<li>` for unordered lists:

```java
/**
 * <p>The handler processes the following exception types:</p>
 * <ul>
 *   <li>ResponseStatusException - HTTP status exceptions</li>
 *   <li>RuntimeException - General runtime errors</li>
 *   <li>ConstraintViolationException - Bean validation errors</li>
 * </ul>
 */
```

Use `<ol>` and `<li>` for ordered lists:

```java
/**
 * <p>This method performs the following operations:</p>
 * <ol>
 *   <li>Validates the request data</li>
 *   <li>Checks if the user exists</li>
 *   <li>Saves the entity to the database</li>
 * </ol>
 */
```

### Cross-References

Use `{@link ClassName}` for class references:

```java
/**
 * Uses {@link UserService} to retrieve user information.
 */
```

Use `{@link ClassName#method}` for method references:

```java
/**
 * @see UserService#getUserById
 */
```

---

## Best Practices

### 1. Completeness

- Document all public classes and interfaces
- Document all public methods
- Document all public fields (if any)
- Document all constructors (if custom)
- Document all enum constants

### 2. Clarity

- Write clear, concise descriptions
- Use complete sentences
- Start with capital letter, end with period
- Avoid ambiguous terms
- Provide context when necessary

### 3. Accuracy

- Ensure descriptions match actual behavior
- Keep documentation in sync with code changes
- Update documentation when refactoring
- Verify examples work correctly

### 4. Consistency

- Use the same style and format throughout
- Use consistent terminology across the project
- Follow naming conventions
- Use standard JavaDoc tags correctly

### 5. Examples

- Include code examples for complex methods
- Ensure examples are syntactically correct
- Use realistic examples from the project context
- Update examples when API changes

### 6. Implementation Methods

- Use `@see` for simple delegating implementations
- Provide full documentation for implementations with significant logic
- Document any additional behavior beyond the interface contract

### 7. Repository Methods

- **DO NOT** document Derived Query Methods
- **DO** document custom query methods with `@Query`
- **DO** document complex business logic methods
- Use clear, descriptive method names

---

## Quality Checklist

Before submitting code, ensure:

### Completeness
- [ ] All public classes have class-level JavaDoc
- [ ] All public interfaces have interface-level JavaDoc
- [ ] All public methods have method-level JavaDoc
- [ ] All public fields have field-level JavaDoc (if any)
- [ ] All constructors have JavaDoc (if custom)
- [ ] All enum constants have JavaDoc
- [ ] All DTO records have record-level JavaDoc with `@param` tags
- [ ] All annotation elements have JavaDoc

### Accuracy
- [ ] Descriptions match actual behavior
- [ ] Parameter descriptions are accurate
- [ ] Return value descriptions are correct
- [ ] Exception descriptions are accurate
- [ ] Examples work as described
- [ ] Cross-references point to existing classes/methods

### Clarity
- [ ] Language is clear and concise
- [ ] Technical terms are explained when necessary
- [ ] Examples are helpful and relevant
- [ ] Cross-references are meaningful
- [ ] No spelling or grammar errors

### Standards Compliance
- [ ] Uses standard JavaDoc tags correctly
- [ ] Follows Oracle JavaDoc conventions
- [ ] HTML formatting is valid
- [ ] Links work correctly
- [ ] Includes `@author geron` and `@version 1.0` where required
- [ ] Uses `<p>` tags for paragraph separation
- [ ] Uses proper code formatting tags

### Repository-Specific
- [ ] Repository interface is documented
- [ ] Derived Query Methods are **NOT** documented
- [ ] Custom query methods are documented
- [ ] Complex business logic methods are documented

### DTO-Specific
- [ ] Record has class-level JavaDoc with `@param` tags
- [ ] Important fields have individual JavaDoc comments
- [ ] Validation constraints are mentioned when relevant

---

## Common Patterns and Examples

### Pattern 1: Service Interface and Implementation

**Interface:**
```java
/**
 * Service interface for user management in Twitter microservices.
 * <p>
 * This interface defines the contract for user management services, providing
 * business logic for CRUD operations with users, including validation,
 * password hashing, and business rule enforcement.
 *
 * @author geron
 * @version 1.0
 */
public interface UserService {
    
    /**
     * Retrieves a user by their unique identifier.
     * <p>
     * Returns an empty Optional if the user does not exist
     * or has been deactivated.
     *
     * @param id the unique identifier of the user
     * @return Optional containing user data or empty if not found
     */
    Optional<UserResponseDto> getUserById(UUID id);
}
```

**Implementation:**
```java
/**
 * Implementation of the user management service.
 * <p>
 * This service provides business logic for CRUD operations with users,
 * including creation, updating, deactivation, and role management. It handles
 * data validation, password hashing, and business rule enforcement.
 *
 * @author geron
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    /**
     * @see UserService#getUserById
     */
    @Override
    public Optional<UserResponseDto> getUserById(UUID id) {
        return userRepository.findById(id).map(userMapper::toUserResponseDto);
    }
}
```

### Pattern 2: DTO Record with Field Documentation

```java
/**
 * Data Transfer Object for user creation requests.
 * <p>
 * This record represents the data structure used for creating new users
 * in the system. It includes validation constraints to ensure data
 * integrity and security requirements are met.
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
    /**
     * Unique login name for user authentication.
     * <p>
     * This field must be unique across all users and is required for
     * authentication purposes. Must be between 3 and 50 characters.
     */
    @Schema(description = "Unique login name for user authentication", example = "jane_smith")
    @NotBlank(message = "Login cannot be blank")
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    String login,
    
    // other fields...
) {
}
```

### Pattern 3: Repository with Derived Query Methods

```java
/**
 * Repository interface for user data access operations.
 * <p>
 * This repository provides data access methods for User entities.
 * It extends JpaRepository and JpaSpecificationExecutor for standard
 * CRUD operations and dynamic query capabilities.
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

---

## Tools and Validation

### JavaDoc Generation

```bash
# Generate JavaDoc for a specific module
./gradlew :shared:common-lib:javadoc

# Generate JavaDoc for all modules
./gradlew javadoc

# Generate JavaDoc JAR
./gradlew :shared:common-lib:javadocJar
```

### Validation Commands

```bash
# Check JavaDoc syntax (without compilation)
./gradlew :shared:common-lib:javadoc -x compileJava

# Generate HTML documentation
./gradlew :shared:common-lib:javadocJar
```

### IDE Integration

Most modern IDEs (IntelliJ IDEA, Eclipse) can:
- Validate JavaDoc syntax in real-time
- Generate JavaDoc stubs automatically
- Show JavaDoc tooltips on hover
- Check for missing JavaDoc on public APIs

---

## Version History

- **v1.0** (2025-01-27): Initial version based on comprehensive analysis of `shared` and `services` modules

---

## References

- [Oracle JavaDoc Guide](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html)
- [JavaDoc Tags Reference](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javadoc.html#CHDJGIED)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Project Code Standards](./STANDART_CODE.md)
- [Project JavaDoc Standards (Common-Lib)](./JAVADOC_STANDARDS.md)
- [Project JavaDoc Templates](./JAVADOC_TEMPLATES.md)

