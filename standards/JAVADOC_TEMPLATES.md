# JavaDoc Templates for Twitter Common-Lib

## Overview
This document provides ready-to-use JavaDoc templates for all types of elements in the Twitter Common-Lib project. These templates follow the established documentation standards and can be copied directly into the code.

## Template Usage Instructions

1. **Copy the appropriate template** for your element type
2. **Replace placeholder text** with actual descriptions
3. **Remove unused tags** (e.g., remove `@throws` if method doesn't throw exceptions)
4. **Add specific details** relevant to your implementation
5. **Verify all placeholders** are replaced before committing

## 1. Class Templates

### Basic Class Template
```java
/**
 * Brief description of the class purpose and functionality.
 * 
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

### Service Class Template
```java
/**
 * Service class for [specific functionality] in Twitter microservices.
 * 
 * This service provides [detailed functionality description] and handles
 * [specific responsibilities]. It integrates with [related components]
 * and follows Twitter's [specific patterns/standards].
 * 
 * @author geron
 * @version 1.0
 */
@Service
public class ExampleService {
    // implementation
}
```

### Configuration Class Template
```java
/**
 * Configuration class for [specific configuration area].
 * 
 * This configuration class defines beans and properties for
 * [specific functionality]. It provides default values and
 * allows customization through application properties.
 * 
 * @author geron
 * @version 1.0
 */
@Configuration
public class ExampleConfiguration {
    // implementation
}
```

### Exception Class Template
```java
/**
 * Exception thrown when [specific condition] occurs.
 * 
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

## 2. Method Templates

### Public Method Template
```java
/**
 * Brief description of what the method does.
 * 
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

### Private Method Template
```java
/**
 * Helper method for [specific functionality].
 * 
 * This private method [detailed description of what it does].
 * It is used internally by [calling methods] and should not
 * be called directly from outside the class.
 * 
 * @param paramName description of the parameter
 * @return description of the return value
 * @throws ExceptionType description of when this exception is thrown
 */
private ReturnType helperMethod(ParamType paramName) throws ExceptionType {
    // implementation
}
```

### Static Method Template
```java
/**
 * Utility method for [specific functionality].
 * 
 * This static method provides [detailed description] and can be
 * called without instantiating the class. It is thread-safe and
 * performs [specific operations].
 * 
 * @param paramName description of the parameter
 * @return description of the return value
 * @throws ExceptionType description of when this exception is thrown
 */
public static ReturnType utilityMethod(ParamType paramName) throws ExceptionType {
    // implementation
}
```

### Constructor Template
```java
/**
 * Constructs a new [ClassName] with the specified parameters.
 * 
 * This constructor initializes the instance with the provided
 * values and performs [specific initialization steps].
 * 
 * @param paramName description of the parameter
 * @param anotherParam description of another parameter
 * @throws IllegalArgumentException if any parameter is invalid
 * @throws ExceptionType if initialization fails
 */
public ClassName(ParamType paramName, AnotherType anotherParam) 
        throws IllegalArgumentException, ExceptionType {
    // implementation
}
```

### Method with Code Example Template
```java
/**
 * Brief description of what the method does.
 * 
 * Detailed description of the method's behavior and usage.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * ExampleClass example = new ExampleClass();
 * ReturnType result = example.methodName(paramValue);
 * }</pre>
 * 
 * @param paramName description of the parameter
 * @return description of the return value
 * @throws ExceptionType description of when this exception is thrown
 */
public ReturnType methodName(ParamType paramName) throws ExceptionType {
    // implementation
}
```

## 3. Annotation Templates

### Basic Annotation Template
```java
/**
 * Annotation for [specific functionality].
 * 
 * This annotation [detailed description of what it does and when to use it].
 * It provides configuration options for [specific behavior] and can be
 * applied to [target elements].
 * 
 * @author geron
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExampleAnnotation {
    
    /**
     * Description of the annotation parameter.
     * 
     * @return description of what this parameter controls
     */
    boolean parameterName() default true;
}
```

### Annotation with Multiple Parameters Template
```java
/**
 * Annotation for [specific functionality] with multiple configuration options.
 * 
 * This annotation provides comprehensive configuration for [specific behavior].
 * It allows fine-tuning of [specific aspects] and supports [specific features].
 * 
 * @author geron
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComplexAnnotation {
    
    /**
     * Description of the first parameter.
     * 
     * @return description of what this parameter controls
     */
    boolean firstParameter() default true;
    
    /**
     * Description of the second parameter.
     * 
     * @return description of what this parameter controls
     */
    String[] secondParameter() default {};
    
    /**
     * Description of the third parameter.
     * 
     * @return description of what this parameter controls
     */
    int thirdParameter() default 0;
}
```

## 4. Enum Templates

### Basic Enum Template
```java
/**
 * Enumeration of [specific concept] used in Twitter's [specific area].
 * 
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
    SECOND_VALUE,
    
    /**
     * Description of the third enum value.
     * Used when [specific condition] occurs.
     */
    THIRD_VALUE
}
```

### Enum with Fields Template
```java
/**
 * Enumeration of [specific concept] with associated data.
 * 
 * This enum defines [specific concept] values along with their
 * associated [specific data]. Each value provides [specific functionality]
 * through its associated fields and methods.
 * 
 * @author geron
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
public enum ExampleEnumWithFields {
    
    /**
     * Description of the first enum value.
     * Associated with [specific data/behavior].
     */
    FIRST_VALUE("first", "First Description"),
    
    /**
     * Description of the second enum value.
     * Associated with [specific data/behavior].
     */
    SECOND_VALUE("second", "Second Description");
    
    private final String code;
    private final String description;
}
```

## 5. Field Templates

### Public Field Template
```java
/**
 * Description of what this field represents.
 * 
 * This field stores [detailed description] and is used for
 * [specific purposes]. It should be [specific constraints/notes].
 */
public static final String FIELD_NAME = "value";
```

### Private Field Template
```java
/**
 * Internal field for [specific functionality].
 * 
 * This private field stores [detailed description] and is used
 * internally by [specific methods]. It should not be accessed
 * directly from outside the class.
 */
private final FieldType fieldName;
```

### Configuration Field Template
```java
/**
 * Configuration property for [specific functionality].
 * 
 * This field is populated from application properties and controls
 * [specific behavior]. The default value is [default value] and
 * can be overridden through [specific configuration method].
 */
@Value("${app.specific.property:defaultValue}")
private String configProperty;
```

## 6. Interface Templates

### Basic Interface Template
```java
/**
 * Interface defining the contract for [specific functionality].
 * 
 * This interface specifies the methods that must be implemented
 * by classes providing [specific functionality]. It ensures
 * consistent behavior across different implementations.
 * 
 * @author geron
 * @version 1.0
 */
public interface ExampleInterface {
    
    /**
     * Description of what this method should do.
     * 
     * @param paramName description of the parameter
     * @return description of the return value
     * @throws ExceptionType description of when this exception is thrown
     */
    ReturnType methodName(ParamType paramName) throws ExceptionType;
}
```

### Service Interface Template
```java
/**
 * Service interface for [specific business functionality].
 * 
 * This interface defines the contract for services that handle
 * [specific business operations]. Implementations should provide
 * [specific guarantees] and follow [specific patterns].
 * 
 * @author geron
 * @version 1.0
 */
public interface ExampleService {
    
    /**
     * Performs [specific business operation].
     * 
     * @param paramName description of the parameter
     * @return description of the return value
     * @throws BusinessException if the operation fails
     */
    ReturnType performOperation(ParamType paramName) throws BusinessException;
}
```

## 7. Specialized Templates

### Aspect Template
```java
/**
 * Aspect for [specific cross-cutting concern].
 * 
 * This aspect provides [specific functionality] across multiple
 * methods and classes. It uses [specific AOP technique] to
 * [specific behavior description].
 * 
 * @author geron
 * @version 1.0
 */
@Aspect
@Component
public class ExampleAspect {
    
    /**
     * Advice method that [specific functionality].
     * 
     * This method is executed [specific timing] and provides
     * [specific functionality] for methods annotated with
     * [specific annotation].
     * 
     * @param joinPoint the AOP join point
     * @return the result of the original method execution
     * @throws Throwable if the original method throws an exception
     */
    @Around("@annotation(RelatedAnnotation)")
    public Object adviceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // implementation
    }
}
```

### Exception Handler Template
```java
/**
 * Exception handler for [specific exception type].
 * 
 * This method handles [specific exception type] and converts it
 * to an appropriate HTTP response. It provides [specific error
 * information] and follows Twitter's error response format.
 * 
 * @param ex the exception that was thrown
 * @return ProblemDetail containing error information
 */
@ExceptionHandler(SpecificException.class)
public ProblemDetail handleSpecificException(SpecificException ex) {
    // implementation
}
```

### Validation Exception Template
```java
/**
 * Exception thrown when [specific validation] fails.
 * 
 * This exception is thrown when [detailed description of validation
 * failure]. It provides additional context through [specific methods]
 * to help with error resolution.
 * 
 * @author geron
 * @version 1.0
 */
public class SpecificValidationException extends ValidationException {
    
    /**
     * Constructs a new validation exception with the specified message.
     * 
     * @param message the detail message
     */
    public SpecificValidationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new validation exception with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public SpecificValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Returns the type of validation that failed.
     * 
     * @return the validation type
     */
    @Override
    public ValidationType getValidationType() {
        return ValidationType.SPECIFIC_TYPE;
    }
}
```

## 8. Template Placeholders

### Common Placeholders to Replace

- `[specific functionality]` - Replace with actual functionality description
- `[detailed description]` - Replace with specific implementation details
- `[specific condition]` - Replace with actual conditions
- `[specific purposes]` - Replace with actual use cases
- `[specific constraints/notes]` - Replace with actual constraints
- `[specific methods]` - Replace with actual method names
- `[specific parameters]` - Replace with actual parameter descriptions
- `[specific behavior]` - Replace with actual behavior description
- `[specific timing]` - Replace with actual execution timing
- `[specific annotation]` - Replace with actual annotation name

### Example of Placeholder Replacement

**Before (template):**
```java
/**
 * Service class for [specific functionality] in Twitter microservices.
 */
```

**After (actual usage):**
```java
/**
 * Service class for user authentication in Twitter microservices.
 */
```

## 9. Quality Checklist for Templates

### Before Using a Template
- [ ] All placeholders have been replaced with actual values
- [ ] Descriptions are accurate and specific to the implementation
- [ ] All unused tags have been removed
- [ ] Cross-references point to existing classes/methods
- [ ] Examples are correct and tested
- [ ] Language is clear and professional

### After Using a Template
- [ ] JavaDoc generates without warnings
- [ ] All public APIs are documented
- [ ] Descriptions match actual behavior
- [ ] Examples work as described
- [ ] Cross-references are valid

## 10. Template Customization Guidelines

1. **Keep it Relevant**: Only include information that adds value
2. **Be Specific**: Avoid generic descriptions
3. **Include Context**: Explain how the element fits into the larger system
4. **Provide Examples**: Include code examples for complex methods
5. **Cross-Reference**: Link related classes and methods
6. **Stay Consistent**: Use the same style and format throughout
7. **Update Regularly**: Keep documentation in sync with code changes

```

## 8. JPA Repository Templates

### Basic Repository Template

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
    boolean existsByLoginAndIdNot(String login, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);
    
    // Custom methods - JavaDoc required
    /**
     * Custom method description.
     * 
     * @param param description of the parameter
     * @return description of the return value
     * @throws ExceptionType description of when this exception is thrown
     */
    CustomType customMethod(ParamType param);
}
```

### User Repository Template

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

## Version History

- **v2.0** (2025-01-27): Updated templates - removed @since and @see tags, changed @author to "geron", added JPA repository templates
- **v1.0** (2025-01-27): Initial version with comprehensive templates for all element types
