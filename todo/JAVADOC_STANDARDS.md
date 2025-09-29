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
- `@author` - Author name (Twitter Team)
- `@version` - Version number
- `@since` - Version when the class was introduced

#### Template
```java
/**
 * Brief description of the class purpose and functionality.
 * 
 * Detailed description explaining the class responsibilities,
 * key features, and usage context within the Twitter ecosystem.
 * 
 * @author Twitter Team
 * @version 1.0
 * @since 2025-01-27
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
 * @author Twitter Team
 * @version 1.0
 * @since 2025-01-27
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
- `@see` - For related methods/classes (when applicable)

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
 * @see RelatedClass#relatedMethod()
 * @since 2025-01-27
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
 * @see LoggableRequest#hideFields()
 * @since 2025-01-27
 */
private void logRequestDetails(HttpServletRequest request, ProceedingJoinPoint proceedingJoinPoint) {
    // implementation
}
```

### 3. Annotations

#### Required Tags
- `@author` - Author name
- `@since` - Version when introduced
- `@see` - Related classes or documentation

#### Template
```java
/**
 * Brief description of the annotation's purpose.
 * 
 * Detailed description of when and how to use this annotation,
 * including examples and best practices.
 * 
 * @author Twitter Team
 * @since 2025-01-27
 * @see RelatedClass
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
 * @author Twitter Team
 * @since 2025-01-27
 * @see LoggableRequestAspect
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
 * @author Twitter Team
 * @version 1.0
 * @since 2025-01-27
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
 * @author Twitter Team
 * @version 1.0
 * @since 2025-01-27
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
- `@since` - Version when introduced
- `@see` - Related classes or documentation

#### Template
```java
/**
 * Exception thrown when [specific condition] occurs.
 * 
 * Detailed description of the exception, including when it's thrown,
 * what causes it, and how to handle it properly.
 * 
 * @author Twitter Team
 * @since 2025-01-27
 * @see RelatedClass
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
 * @author Twitter Team
 * @since 2025-01-27
 * @see GlobalExceptionHandler#handleLastAdminDeactivationException(LastAdminDeactivationException)
 */
public class LastAdminDeactivationException extends RuntimeException {
    // implementation
}
```

## Standard JavaDoc Tags

### Core Tags
- `@param` - Parameter description
- `@return` - Return value description
- `@throws` / `@exception` - Exception description
- `@see` - Reference to related documentation
- `@since` - Version when introduced
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
 * @see ValidationType for validation categories
 * @see GlobalExceptionHandler for error handling
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

- **v1.0** (2025-01-27): Initial version with comprehensive standards for all element types
