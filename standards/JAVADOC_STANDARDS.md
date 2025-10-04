# JavaDoc Documentation Standards for Twitter Common-Lib

## Overview
This document defines the JavaDoc documentation standards for the Twitter Common-Lib project. All public APIs must be documented according to these standards to ensure consistency and maintainability.

## General Principles

### Language
- All documentation must be written in **English**
- Use clear, concise language
- Avoid technical jargon when possible
- Use present tense for descriptions

### Structure
- Start with a brief description of the element
- Follow with detailed information
- Use standard JavaDoc tags consistently
- Include examples for complex methods

## Documentation Standards by Element Type

### 1. Classes and Interfaces

#### Required Tags
- `@author` - Author name (geron)
- `@version` - Version number

#### Template
```java
/**
 * Brief description of the class purpose and functionality.
 * 
 * Detailed description explaining the class responsibilities,
 * key features, and usage context within the Twitter ecosystem.
 * 
 * @author geron
 * @version 1.0
 */
public class ExampleClass {
    // implementation
}
```

#### Example
```java
/**
 * Global exception handler for centralized error processing in Twitter microservices.
 * 
 * This handler provides unified error response formatting using Spring's ProblemDetail
 * and handles various types of validation and business rule exceptions. It ensures
 * consistent error responses across all Twitter services.
 * 
 * @author geron
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    // implementation
}
```

### 2. Methods

#### Required Tags
- `@param` - For each parameter (if any)
- `@return` - For return value description
- `@throws` - For each exception that can be thrown

#### Template
```java
/**
 * Brief description of what the method does.
 * 
 * Detailed description of the method's behavior, including
 * any side effects, performance considerations, or important notes.
 * 
 * @param paramName description of the parameter
 * @return description of the return value
 * @throws ExceptionType description of when this exception is thrown
 */
public ReturnType methodName(ParamType paramName) throws ExceptionType {
    // implementation
}
```

#### Example
```java
/**
 * Logs HTTP request details including headers and body.
 * 
 * This method extracts request information and logs it according to the
 * LoggableRequest annotation configuration. Sensitive fields can be hidden
 * using the hideFields parameter.
 * 
 * @param request the HTTP servlet request
 * @param proceedingJoinPoint the AOP join point containing method information
 * @throws IllegalArgumentException if request is null
 */
private void logRequestDetails(HttpServletRequest request, ProceedingJoinPoint proceedingJoinPoint) {
    // implementation
}
```

### 3. Annotations

#### Required Tags
- `@author` - Author name

#### Template
```java
/**
 * Brief description of the annotation's purpose.
 * 
 * Detailed description of when and how to use this annotation,
 * including examples and best practices.
 * 
 * @author geron
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExampleAnnotation {
    // annotation elements
}
```

#### Example
```java
/**
 * Annotation for automatic HTTP request/response logging.
 * 
 * This annotation enables automatic logging of HTTP requests and responses
 * for methods annotated with it. It provides configuration options for
 * controlling what information is logged and which fields should be hidden.
 * 
 * @author geron
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoggableRequest {
    // annotation elements
}
```

### 4. Enums

#### Required Documentation
- Class-level documentation
- Documentation for each enum constant

#### Template
```java
/**
 * Brief description of the enum's purpose.
 * 
 * Detailed description of the enum's usage context and
 * the meaning of its values.
 * 
 * @author geron
 * @version 1.0
 */
public enum ExampleEnum {
    
    /**
     * Description of the first enum value.
     */
    FIRST_VALUE,
    
    /**
     * Description of the second enum value.
     */
    SECOND_VALUE
}
```

#### Example
```java
/**
 * Enumeration of validation types used in Twitter's validation framework.
 * 
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

### 5. Exceptions

#### Required Tags
- `@author` - Author name

#### Template
```java
/**
 * Exception thrown when [specific condition] occurs.
 * 
 * Detailed description of the exception, including when it's thrown,
 * what causes it, and how to handle it properly.
 * 
 * @author geron
 */
public class ExampleException extends RuntimeException {
    // implementation
}
```

#### Example
```java
/**
 * Exception thrown when attempting to deactivate the last active administrator.
 * 
 * This exception prevents the system from being left without any active
 * administrators, which could lead to system lockout scenarios.
 * 
 * @author geron
 */
public class BusinessRuleValidationException extends RuntimeException {
    // implementation
}
```

### 6. JPA Repositories

#### Documentation Rules for Derived Query Methods

Spring Data JPA automatically generates implementation for Derived Query Methods based on method names. These methods have obvious functionality from their names and do not require JavaDoc documentation.

**Key Principles:**
- Derived Query Methods are self-documenting through their naming convention
- Adding JavaDoc to these methods creates redundancy and maintenance overhead
- Focus documentation efforts on custom methods and complex business logic
- Use clear, descriptive method names instead of relying on documentation

#### Methods That Do NOT Require Documentation

The following method patterns are considered Derived Query Methods and should NOT be documented:

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
 * 
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
    
    // Custom methods - JavaDoc required
    /**
     * Custom method description.
     * 
     * @param param description
     * @return description
     */
    CustomType customMethod(ParamType param);
}
```

#### Example

```java
/**
 * Repository interface for user data access operations.
 * 
 * This repository provides data access methods for User entities.
 * It extends JpaRepository and JpaSpecificationExecutor for standard
 * CRUD operations and dynamic query capabilities.
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

#### Best Practices for Repository Documentation

1. **Repository Interface Documentation:**
   - Always document the repository interface itself
   - Explain the entity type and primary key type
   - Mention extended interfaces (JpaRepository, JpaSpecificationExecutor)
   - Describe any special capabilities or constraints

2. **Method Naming Conventions:**
   - Use clear, descriptive method names
   - Follow Spring Data JPA naming conventions
   - Avoid abbreviations that might be unclear
   - Use consistent terminology across the project

3. **Custom Method Documentation:**
   - Document all custom methods with `@Query` annotations
   - Explain complex business logic
   - Document parameter constraints and validation rules
   - Describe return value semantics and possible states

4. **Exception Handling:**
   - Document methods that can throw specific exceptions
   - Explain when and why exceptions are thrown
   - Provide guidance on exception handling

#### Common Patterns and Examples

**Good Repository Interface:**
```java
/**
 * Repository interface for user data access operations.
 * 
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
    
    // Custom methods - JavaDoc required
    /**
     * Finds users by complex criteria with custom query.
     * 
     * This method performs a complex search using multiple criteria
     * and returns users matching all specified conditions.
     * 
     * @param criteria the search criteria
     * @return list of users matching the criteria
     * @throws IllegalArgumentException if criteria is null
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status AND u.createdAt > :date")
    List<User> findUsersByComplexCriteria(@Param("role") UserRole role, 
                                         @Param("status") UserStatus status, 
                                         @Param("date") LocalDateTime date);
}
```

**Anti-pattern - Over-documentation:**
```java
// DON'T DO THIS - Derived Query Methods don't need documentation
/**
 * Checks if a user exists by login.
 * 
 * @param login the login to check
 * @return true if user exists, false otherwise
 */
boolean existsByLogin(String login);
```

## Standard JavaDoc Tags

### Core Tags
- `@param` - Parameter description
- `@return` - Return value description
- `@throws` / `@exception` - Exception description
- `@author` - Author information
- `@version` - Version number
- `@deprecated` - Mark as deprecated

### Additional Tags
- `@apiNote` - API usage notes
- `@implSpec` - Implementation specification
- `@implNote` - Implementation notes
- `@serial` - Serialization information
- `@serialData` - Serialization data
- `@serialField` - Serialization field

## Code Examples

### Inline Code Examples
```java
/**
 * Calculates the user's activity score.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * UserActivityCalculator calculator = new UserActivityCalculator();
 * int score = calculator.calculateScore(userId, startDate, endDate);
 * }</pre>
 * 
 * @param userId the user identifier
 * @param startDate the start date for calculation
 * @param endDate the end date for calculation
 * @return the calculated activity score
 */
public int calculateScore(Long userId, LocalDate startDate, LocalDate endDate) {
    // implementation
}
```

### Cross-References
```java
/**
 * Validates user data according to Twitter's business rules.
 * 
 * @param user the user to validate
 * @throws ValidationException if validation fails
 */
public void validateUser(User user) throws ValidationException {
    // implementation
}
```

## Quality Checklist

### Completeness
- [ ] All public classes have class-level JavaDoc
- [ ] All public methods have method-level JavaDoc
- [ ] All public fields have field-level JavaDoc
- [ ] All constructors have JavaDoc
- [ ] All enum constants have JavaDoc

### Accuracy
- [ ] Descriptions match actual behavior
- [ ] Parameter descriptions are accurate
- [ ] Return value descriptions are correct
- [ ] Exception descriptions are accurate
- [ ] Examples work as described

### Clarity
- [ ] Language is clear and concise
- [ ] Technical terms are explained
- [ ] Examples are helpful
- [ ] Cross-references are meaningful

### Standards Compliance
- [ ] Uses standard JavaDoc tags correctly
- [ ] Follows Oracle JavaDoc conventions
- [ ] HTML formatting is valid
- [ ] Links work correctly

## Tools and Validation

### JavaDoc Generation
```bash
# Generate JavaDoc for common-lib module
./gradlew :shared:common-lib:javadoc

# Generate JavaDoc for all modules
./gradlew javadoc
```

### Validation Commands
```bash
# Check JavaDoc syntax
./gradlew :shared:common-lib:javadoc -x compileJava

# Generate HTML documentation
./gradlew :shared:common-lib:javadocJar
```

## Best Practices

1. **Keep it Simple**: Write clear, concise descriptions
2. **Be Consistent**: Use the same style and format throughout
3. **Include Examples**: Provide code examples for complex methods
4. **Cross-Reference**: Link related classes and methods
5. **Update Regularly**: Keep documentation in sync with code changes
6. **Test Examples**: Ensure all code examples work correctly
7. **Use HTML Carefully**: Only use basic HTML tags for formatting

## Version History

- **v2.0** (2025-01-27): Updated standards - removed @since and @see tags, changed @author to "geron", added JPA repository rules
- **v1.0** (2025-01-27): Initial version with comprehensive standards for all element types
