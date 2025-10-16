# Проектирование системы обработки ошибок Tweet API

## Meta
- project: twitter-tweet-api
- design_date: 2025-01-27
- designer: AI Assistant
- version: 1.0
- status: completed
- step: 4.12

## Executive Summary

Данный документ содержит детальное проектирование системы обработки ошибок для сервиса Tweet API. Система спроектирована с учетом интеграции с существующими паттернами users-api, использования shared/common-lib компонентов и обеспечения высокого уровня пользовательского опыта с понятными сообщениями об ошибках.

## 1. Архитектурные принципы обработки ошибок

### 1.1 Стандарты обработки ошибок

#### RFC 7807 Problem Details for HTTP APIs:
- **Стандартизированный формат** ответов об ошибках
- **Структурированная информация** о проблемах
- **Консистентность** между всеми сервисами
- **Расширяемость** для дополнительных полей

#### Принципы:
- **Fail Fast** - быстрая обработка ошибок
- **User-Friendly** - понятные сообщения для пользователей
- **Developer-Friendly** - детальная информация для разработчиков
- **Consistent** - единообразные ответы
- **Secure** - отсутствие утечки чувствительной информации

### 1.2 Интеграция с существующими паттернами

#### Использование shared/common-lib:
- **GlobalExceptionHandler** - централизованная обработка ошибок
- **ValidationException** - базовый класс для ошибок валидации
- **ProblemDetail** - стандартизированный формат ответов
- **LoggableRequestAspect** - логирование ошибок

#### Следование паттернам users-api:
- **Структура ответов** в формате ProblemDetail
- **HTTP статус коды** согласно REST стандартам
- **Типизированные исключения** с контекстом

## 2. Стандартные коды ошибок

### 2.1 Иерархия кодов ошибок

#### TweetErrorCode
```java
@Getter
@RequiredArgsConstructor
public enum TweetErrorCode {
    
    // Общие ошибки
    VALIDATION_ERROR("VALIDATION_ERROR", "Validation failed"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Internal server error"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "Service temporarily unavailable"),
    
    // Ошибки твитов
    TWEET_NOT_FOUND("TWEET_NOT_FOUND", "Tweet not found"),
    TWEET_ALREADY_DELETED("TWEET_ALREADY_DELETED", "Tweet has been deleted"),
    TWEET_ACCESS_DENIED("TWEET_ACCESS_DENIED", "Access denied to tweet"),
    TWEET_TOO_OLD("TWEET_TOO_OLD", "Tweet is too old for this operation"),
    TWEET_CONTENT_INVALID("TWEET_CONTENT_INVALID", "Tweet content is invalid"),
    TWEET_RATE_LIMIT_EXCEEDED("TWEET_RATE_LIMIT_EXCEEDED", "Tweet creation rate limit exceeded"),
    
    // Ошибки пользователей
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found"),
    USER_INACTIVE("USER_INACTIVE", "User account is inactive"),
    USER_BANNED("USER_BANNED", "User account is banned"),
    USER_SERVICE_UNAVAILABLE("USER_SERVICE_UNAVAILABLE", "User service is unavailable"),
    
    // Ошибки социальных действий
    LIKE_ALREADY_EXISTS("LIKE_ALREADY_EXISTS", "User has already liked this tweet"),
    LIKE_NOT_FOUND("LIKE_NOT_FOUND", "Like not found"),
    LIKE_SELF_NOT_ALLOWED("LIKE_SELF_NOT_ALLOWED", "Users cannot like their own tweets"),
    RETWEET_ALREADY_EXISTS("RETWEET_ALREADY_EXISTS", "User has already retweeted this tweet"),
    RETWEET_NOT_FOUND("RETWEET_NOT_FOUND", "Retweet not found"),
    RETWEET_SELF_NOT_ALLOWED("RETWEET_SELF_NOT_ALLOWED", "Users cannot retweet their own tweets"),
    SOCIAL_ACTION_RATE_LIMIT_EXCEEDED("SOCIAL_ACTION_RATE_LIMIT_EXCEEDED", "Social action rate limit exceeded"),
    
    // Ошибки пагинации
    INVALID_PAGE_NUMBER("INVALID_PAGE_NUMBER", "Invalid page number"),
    INVALID_PAGE_SIZE("INVALID_PAGE_SIZE", "Invalid page size"),
    PAGE_SIZE_TOO_LARGE("PAGE_SIZE_TOO_LARGE", "Page size exceeds maximum allowed"),
    
    // Ошибки безопасности
    SPAM_DETECTED("SPAM_DETECTED", "Spam content detected"),
    ABUSE_DETECTED("ABUSE_DETECTED", "Abusive behavior detected"),
    CONTENT_POLICY_VIOLATION("CONTENT_POLICY_VIOLATION", "Content violates policy"),
    
    // Ошибки интеграции
    INTEGRATION_ERROR("INTEGRATION_ERROR", "External service integration error"),
    CIRCUIT_BREAKER_OPEN("CIRCUIT_BREAKER_OPEN", "Circuit breaker is open"),
    TIMEOUT_ERROR("TIMEOUT_ERROR", "Request timeout"),
    
    // Ошибки авторизации
    UNAUTHORIZED("UNAUTHORIZED", "Authentication required"),
    FORBIDDEN("FORBIDDEN", "Insufficient permissions"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Authentication token expired"),
    TOKEN_INVALID("TOKEN_INVALID", "Invalid authentication token");
    
    private final String code;
    private final String message;
    
    /**
     * Gets the error code by string value
     *
     * @param code the error code string
     * @return the corresponding TweetErrorCode
     * @throws IllegalArgumentException if code is not found
     */
    public static TweetErrorCode fromCode(String code) {
        return Arrays.stream(values())
            .filter(errorCode -> errorCode.getCode().equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown error code: " + code));
    }
}
```

### 2.2 HTTP статус коды

#### MappingErrorCodes
```java
@Component
@RequiredArgsConstructor
public class ErrorCodeMapping {
    
    /**
     * Maps error codes to HTTP status codes
     *
     * @param errorCode the error code
     * @return the corresponding HTTP status
     */
    public HttpStatus getHttpStatus(TweetErrorCode errorCode) {
        return switch (errorCode) {
            // 400 Bad Request
            case VALIDATION_ERROR, TWEET_CONTENT_INVALID, INVALID_PAGE_NUMBER, 
                 INVALID_PAGE_SIZE, PAGE_SIZE_TOO_LARGE -> HttpStatus.BAD_REQUEST;
            
            // 401 Unauthorized
            case UNAUTHORIZED, TOKEN_EXPIRED, TOKEN_INVALID -> HttpStatus.UNAUTHORIZED;
            
            // 403 Forbidden
            case FORBIDDEN, TWEET_ACCESS_DENIED, USER_BANNED, 
                 LIKE_SELF_NOT_ALLOWED, RETWEET_SELF_NOT_ALLOWED -> HttpStatus.FORBIDDEN;
            
            // 404 Not Found
            case TWEET_NOT_FOUND, USER_NOT_FOUND, LIKE_NOT_FOUND, RETWEET_NOT_FOUND -> HttpStatus.NOT_FOUND;
            
            // 409 Conflict
            case LIKE_ALREADY_EXISTS, RETWEET_ALREADY_EXISTS, TWEET_ALREADY_DELETED -> HttpStatus.CONFLICT;
            
            // 422 Unprocessable Entity
            case TWEET_TOO_OLD, USER_INACTIVE, SPAM_DETECTED, ABUSE_DETECTED, 
                 CONTENT_POLICY_VIOLATION -> HttpStatus.UNPROCESSABLE_ENTITY;
            
            // 429 Too Many Requests
            case TWEET_RATE_LIMIT_EXCEEDED, SOCIAL_ACTION_RATE_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            
            // 503 Service Unavailable
            case SERVICE_UNAVAILABLE, USER_SERVICE_UNAVAILABLE, INTEGRATION_ERROR, 
                 CIRCUIT_BREAKER_OPEN, TIMEOUT_ERROR -> HttpStatus.SERVICE_UNAVAILABLE;
            
            // 500 Internal Server Error
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
    
    /**
     * Gets the error type URI for ProblemDetail
     *
     * @param errorCode the error code
     * @return the error type URI
     */
    public URI getErrorTypeUri(TweetErrorCode errorCode) {
        String baseUri = "https://api.twitter.com/errors/";
        return URI.create(baseUri + errorCode.getCode().toLowerCase().replace("_", "-"));
    }
}
```

## 3. Структура error response

### 3.1 Базовые структуры ответов

#### TweetErrorResponse
```java
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TweetErrorResponse {
    
    /**
     * Error type URI (RFC 7807)
     */
    private URI type;
    
    /**
     * Error title
     */
    private String title;
    
    /**
     * HTTP status code
     */
    private int status;
    
    /**
     * Error detail message
     */
    private String detail;
    
    /**
     * Error code
     */
    private String code;
    
    /**
     * Additional error context
     */
    private Map<String, Object> context;
    
    /**
     * Request timestamp
     */
    private Instant timestamp;
    
    /**
     * Request ID for tracing
     */
    private String requestId;
    
    /**
     * Instance URI for specific error occurrence
     */
    private URI instance;
    
    /**
     * Creates a standardized error response
     *
     * @param errorCode the error code
     * @param detail the error detail
     * @param context additional context
     * @return TweetErrorResponse
     */
    public static TweetErrorResponse of(TweetErrorCode errorCode, String detail, Map<String, Object> context) {
        return TweetErrorResponse.builder()
            .type(URI.create("https://api.twitter.com/errors/" + errorCode.getCode().toLowerCase().replace("_", "-")))
            .title(errorCode.getMessage())
            .status(getHttpStatus(errorCode).value())
            .detail(detail)
            .code(errorCode.getCode())
            .context(context != null ? context : Collections.emptyMap())
            .timestamp(Instant.now())
            .build();
    }
    
    /**
     * Creates a simple error response
     *
     * @param errorCode the error code
     * @param detail the error detail
     * @return TweetErrorResponse
     */
    public static TweetErrorResponse of(TweetErrorCode errorCode, String detail) {
        return of(errorCode, detail, null);
    }
    
    private static HttpStatus getHttpStatus(TweetErrorCode errorCode) {
        // Implementation would use ErrorCodeMapping
        return HttpStatus.BAD_REQUEST; // Default
    }
}
```

### 3.2 Специализированные структуры ответов

#### ValidationErrorResponse
```java
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResponse extends TweetErrorResponse {
    
    /**
     * List of validation errors
     */
    private List<FieldError> errors;
    
    /**
     * Validation error for a specific field
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
        private String code;
    }
    
    /**
     * Creates a validation error response
     *
     * @param errors list of field errors
     * @return ValidationErrorResponse
     */
    public static ValidationErrorResponse of(List<FieldError> errors) {
        return ValidationErrorResponse.builder()
            .type(URI.create("https://api.twitter.com/errors/validation-error"))
            .title("Validation Error")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail("Validation failed")
            .code(TweetErrorCode.VALIDATION_ERROR.getCode())
            .errors(errors)
            .timestamp(Instant.now())
            .build();
    }
}
```

#### BusinessRuleErrorResponse
```java
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessRuleErrorResponse extends TweetErrorResponse {
    
    /**
     * Business rule name that was violated
     */
    private String ruleName;
    
    /**
     * Rule violation context
     */
    private Map<String, Object> ruleContext;
    
    /**
     * Suggested action to resolve the issue
     */
    private String suggestedAction;
    
    /**
     * Creates a business rule error response
     *
     * @param ruleName the violated rule name
     * @param detail the error detail
     * @param context the rule context
     * @return BusinessRuleErrorResponse
     */
    public static BusinessRuleErrorResponse of(String ruleName, String detail, Map<String, Object> context) {
        return BusinessRuleErrorResponse.builder()
            .type(URI.create("https://api.twitter.com/errors/business-rule-violation"))
            .title("Business Rule Violation")
            .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .detail(detail)
            .code(TweetErrorCode.VALIDATION_ERROR.getCode())
            .ruleName(ruleName)
            .ruleContext(context != null ? context : Collections.emptyMap())
            .timestamp(Instant.now())
            .build();
    }
}
```

## 4. Специализированные исключения

### 4.1 Tweet-specific исключения

#### TweetNotFoundException
```java
public class TweetNotFoundException extends TweetException {
    
    public TweetNotFoundException(UUID tweetId) {
        super(TweetErrorCode.TWEET_NOT_FOUND, 
            "Tweet with ID '" + tweetId + "' not found");
        addContext("tweetId", tweetId);
    }
    
    public TweetNotFoundException(UUID tweetId, String reason) {
        super(TweetErrorCode.TWEET_NOT_FOUND, 
            "Tweet with ID '" + tweetId + "' not found: " + reason);
        addContext("tweetId", tweetId);
        addContext("reason", reason);
    }
}
```

#### TweetAccessDeniedException
```java
public class TweetAccessDeniedException extends TweetException {
    
    public TweetAccessDeniedException(UUID tweetId, UUID userId) {
        super(TweetErrorCode.TWEET_ACCESS_DENIED, 
            "User '" + userId + "' does not have access to tweet '" + tweetId + "'");
        addContext("tweetId", tweetId);
        addContext("userId", userId);
    }
    
    public TweetAccessDeniedException(UUID tweetId, UUID userId, String reason) {
        super(TweetErrorCode.TWEET_ACCESS_DENIED, 
            "User '" + userId + "' does not have access to tweet '" + tweetId + "': " + reason);
        addContext("tweetId", tweetId);
        addContext("userId", userId);
        addContext("reason", reason);
    }
}
```

#### TweetRateLimitExceededException
```java
public class TweetRateLimitExceededException extends TweetException {
    
    public TweetRateLimitExceededException(UUID userId, int limit, Duration resetTime) {
        super(TweetErrorCode.TWEET_RATE_LIMIT_EXCEEDED, 
            "Tweet creation rate limit exceeded for user '" + userId + "'");
        addContext("userId", userId);
        addContext("limit", limit);
        addContext("resetTime", resetTime.toSeconds());
        addContext("retryAfter", resetTime.toSeconds());
    }
}
```

#### SpamDetectedException
```java
public class SpamDetectedException extends TweetException {
    
    public SpamDetectedException(String reason) {
        super(TweetErrorCode.SPAM_DETECTED, 
            "Spam content detected: " + reason);
        addContext("reason", reason);
        addContext("detectionType", "AUTOMATED");
    }
    
    public SpamDetectedException(String reason, List<String> patterns) {
        super(TweetErrorCode.SPAM_DETECTED, 
            "Spam content detected: " + reason);
        addContext("reason", reason);
        addContext("patterns", patterns);
        addContext("detectionType", "PATTERN_BASED");
    }
}
```

### 4.2 Базовый класс TweetException

#### TweetException
```java
@Getter
public abstract class TweetException extends RuntimeException {
    
    private final TweetErrorCode errorCode;
    private final Map<String, Object> context;
    
    protected TweetException(TweetErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
    }
    
    protected TweetException(TweetErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
    }
    
    /**
     * Adds context information to the exception
     *
     * @param key the context key
     * @param value the context value
     * @return this exception for chaining
     */
    protected TweetException addContext(String key, Object value) {
        context.put(key, value);
        return this;
    }
    
    /**
     * Gets the HTTP status code for this exception
     *
     * @return HTTP status code
     */
    public HttpStatus getHttpStatus() {
        return ErrorCodeMapping.getHttpStatus(errorCode);
    }
    
    /**
     * Gets the error type URI for ProblemDetail
     *
     * @return error type URI
     */
    public URI getErrorTypeUri() {
        return ErrorCodeMapping.getErrorTypeUri(errorCode);
    }
    
    /**
     * Converts this exception to TweetErrorResponse
     *
     * @return TweetErrorResponse
     */
    public TweetErrorResponse toErrorResponse() {
        return TweetErrorResponse.of(errorCode, getMessage(), context);
    }
}
```

## 5. Глобальная обработка ошибок

### 5.1 TweetGlobalExceptionHandler

```java
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class TweetGlobalExceptionHandler {
    
    private final ErrorCodeMapping errorCodeMapping;
    private final ErrorLocalizationService localizationService;
    private final ErrorMetricsService metricsService;
    
    /**
     * Handles Tweet-specific exceptions
     *
     * @param ex the TweetException
     * @param request the HTTP request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(TweetException.class)
    public ResponseEntity<TweetErrorResponse> handleTweetException(
            TweetException ex, 
            HttpServletRequest request) {
        
        logError(ex, request);
        metricsService.recordError(ex.getErrorCode());
        
        TweetErrorResponse response = ex.toErrorResponse();
        response.setRequestId(getRequestId(request));
        response.setInstance(URI.create(request.getRequestURI()));
        
        // Localize error message if needed
        String localizedMessage = localizationService.localizeMessage(
            ex.getErrorCode().getCode(), 
            getLocale(request)
        );
        if (localizedMessage != null) {
            response.setDetail(localizedMessage);
        }
        
        return ResponseEntity
            .status(ex.getHttpStatus())
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(response);
    }
    
    /**
     * Handles validation exceptions
     *
     * @param ex the ConstraintViolationException
     * @param request the HTTP request
     * @return ResponseEntity with validation error response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, 
            HttpServletRequest request) {
        
        logError(ex, request);
        metricsService.recordError(TweetErrorCode.VALIDATION_ERROR);
        
        List<ValidationErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations().stream()
            .map(violation -> ValidationErrorResponse.FieldError.builder()
                .field(violation.getPropertyPath().toString())
                .message(violation.getMessage())
                .rejectedValue(violation.getInvalidValue())
                .code(violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName())
                .build())
            .collect(Collectors.toList());
        
        ValidationErrorResponse response = ValidationErrorResponse.of(fieldErrors);
        response.setRequestId(getRequestId(request));
        response.setInstance(URI.create(request.getRequestURI()));
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(response);
    }
    
    /**
     * Handles method argument validation exceptions
     *
     * @param ex the MethodArgumentNotValidException
     * @param request the HTTP request
     * @return ResponseEntity with validation error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) {
        
        logError(ex, request);
        metricsService.recordError(TweetErrorCode.VALIDATION_ERROR);
        
        List<ValidationErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> ValidationErrorResponse.FieldError.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .rejectedValue(error.getRejectedValue())
                .code(error.getCode())
                .build())
            .collect(Collectors.toList());
        
        ValidationErrorResponse response = ValidationErrorResponse.of(fieldErrors);
        response.setRequestId(getRequestId(request));
        response.setInstance(URI.create(request.getRequestURI()));
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(response);
    }
    
    /**
     * Handles business rule validation exceptions
     *
     * @param ex the BusinessRuleValidationException
     * @param request the HTTP request
     * @return ResponseEntity with business rule error response
     */
    @ExceptionHandler(BusinessRuleValidationException.class)
    public ResponseEntity<BusinessRuleErrorResponse> handleBusinessRuleValidation(
            BusinessRuleValidationException ex, 
            HttpServletRequest request) {
        
        logError(ex, request);
        metricsService.recordError(TweetErrorCode.VALIDATION_ERROR);
        
        BusinessRuleErrorResponse response = BusinessRuleErrorResponse.of(
            ex.getRuleName(),
            ex.getMessage(),
            ex.getContext()
        );
        response.setRequestId(getRequestId(request));
        response.setInstance(URI.create(request.getRequestURI()));
        
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(response);
    }
    
    /**
     * Handles integration exceptions
     *
     * @param ex the IntegrationException
     * @param request the HTTP request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<TweetErrorResponse> handleIntegrationException(
            IntegrationException ex, 
            HttpServletRequest request) {
        
        logError(ex, request);
        metricsService.recordError(TweetErrorCode.INTEGRATION_ERROR);
        
        TweetErrorResponse response = TweetErrorResponse.of(
            TweetErrorCode.INTEGRATION_ERROR,
            "External service integration failed: " + ex.getMessage()
        );
        response.setRequestId(getRequestId(request));
        response.setInstance(URI.create(request.getRequestURI()));
        
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(response);
    }
    
    /**
     * Handles circuit breaker exceptions
     *
     * @param ex the CircuitBreakerOpenException
     * @param request the HTTP request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(CircuitBreakerOpenException.class)
    public ResponseEntity<TweetErrorResponse> handleCircuitBreakerOpen(
            CircuitBreakerOpenException ex, 
            HttpServletRequest request) {
        
        logError(ex, request);
        metricsService.recordError(TweetErrorCode.CIRCUIT_BREAKER_OPEN);
        
        TweetErrorResponse response = TweetErrorResponse.of(
            TweetErrorCode.CIRCUIT_BREAKER_OPEN,
            "Service temporarily unavailable due to high error rate"
        );
        response.setRequestId(getRequestId(request));
        response.setInstance(URI.create(request.getRequestURI()));
        response.addContext("retryAfter", ex.getRetryAfter().toSeconds());
        
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .header("Retry-After", String.valueOf(ex.getRetryAfter().toSeconds()))
            .body(response);
    }
    
    /**
     * Handles timeout exceptions
     *
     * @param ex the TimeoutException
     * @param request the HTTP request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<TweetErrorResponse> handleTimeout(
            TimeoutException ex, 
            HttpServletRequest request) {
        
        logError(ex, request);
        metricsService.recordError(TweetErrorCode.TIMEOUT_ERROR);
        
        TweetErrorResponse response = TweetErrorResponse.of(
            TweetErrorCode.TIMEOUT_ERROR,
            "Request timeout: " + ex.getMessage()
        );
        response.setRequestId(getRequestId(request));
        response.setInstance(URI.create(request.getRequestURI()));
        
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(response);
    }
    
    /**
     * Handles unexpected runtime exceptions
     *
     * @param ex the RuntimeException
     * @param request the HTTP request
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<TweetErrorResponse> handleRuntimeException(
            RuntimeException ex, 
            HttpServletRequest request) {
        
        logError(ex, request);
        metricsService.recordError(TweetErrorCode.INTERNAL_SERVER_ERROR);
        
        TweetErrorResponse response = TweetErrorResponse.of(
            TweetErrorCode.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
        );
        response.setRequestId(getRequestId(request));
        response.setInstance(URI.create(request.getRequestURI()));
        
        // Don't expose internal error details in production
        if (isDevelopmentEnvironment()) {
            response.addContext("exception", ex.getClass().getSimpleName());
            response.addContext("message", ex.getMessage());
        }
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(response);
    }
    
    private void logError(Exception ex, HttpServletRequest request) {
        log.error("Error processing request {} {}: {}", 
            request.getMethod(), 
            request.getRequestURI(), 
            ex.getMessage(), 
            ex);
    }
    
    private String getRequestId(HttpServletRequest request) {
        return request.getHeader("X-Request-ID");
    }
    
    private Locale getLocale(HttpServletRequest request) {
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null) {
            return Locale.forLanguageTag(acceptLanguage.split(",")[0]);
        }
        return Locale.getDefault();
    }
    
    private boolean isDevelopmentEnvironment() {
        return "development".equals(System.getProperty("spring.profiles.active"));
    }
}
```

## 6. Локализация сообщений об ошибках

### 6.1 ErrorLocalizationService

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorLocalizationService {
    
    private final MessageSource messageSource;
    private final ErrorLocalizationProperties properties;
    
    /**
     * Localizes an error message for the given error code and locale
     *
     * @param errorCode the error code
     * @param locale the target locale
     * @return localized message or null if not found
     */
    public String localizeMessage(String errorCode, Locale locale) {
        if (!properties.isEnabled()) {
            return null;
        }
        
        try {
            String messageKey = "error." + errorCode.toLowerCase().replace("_", ".");
            return messageSource.getMessage(messageKey, null, locale);
        } catch (NoSuchMessageException e) {
            log.debug("No localized message found for error code: {} and locale: {}", errorCode, locale);
            return null;
        }
    }
    
    /**
     * Localizes an error message with parameters
     *
     * @param errorCode the error code
     * @param locale the target locale
     * @param parameters the message parameters
     * @return localized message or null if not found
     */
    public String localizeMessage(String errorCode, Locale locale, Object... parameters) {
        if (!properties.isEnabled()) {
            return null;
        }
        
        try {
            String messageKey = "error." + errorCode.toLowerCase().replace("_", ".");
            return messageSource.getMessage(messageKey, parameters, locale);
        } catch (NoSuchMessageException e) {
            log.debug("No localized message found for error code: {} and locale: {}", errorCode, locale);
            return null;
        }
    }
    
    /**
     * Gets all supported locales
     *
     * @return list of supported locales
     */
    public List<Locale> getSupportedLocales() {
        return properties.getSupportedLocales();
    }
}
```

### 6.2 Файлы локализации

#### messages_en.properties
```properties
# General errors
error.validation.error=Validation failed
error.internal.server.error=Internal server error
error.service.unavailable=Service temporarily unavailable

# Tweet errors
error.tweet.not.found=Tweet not found
error.tweet.already.deleted=Tweet has been deleted
error.tweet.access.denied=Access denied to tweet
error.tweet.too.old=Tweet is too old for this operation
error.tweet.content.invalid=Tweet content is invalid
error.tweet.rate.limit.exceeded=Tweet creation rate limit exceeded

# User errors
error.user.not.found=User not found
error.user.inactive=User account is inactive
error.user.banned=User account is banned
error.user.service.unavailable=User service is unavailable

# Social action errors
error.like.already.exists=User has already liked this tweet
error.like.not.found=Like not found
error.like.self.not.allowed=Users cannot like their own tweets
error.retweet.already.exists=User has already retweeted this tweet
error.retweet.not.found=Retweet not found
error.retweet.self.not.allowed=Users cannot retweet their own tweets
error.social.action.rate.limit.exceeded=Social action rate limit exceeded

# Security errors
error.spam.detected=Spam content detected
error.abuse.detected=Abusive behavior detected
error.content.policy.violation=Content violates policy

# Authorization errors
error.unauthorized=Authentication required
error.forbidden=Insufficient permissions
error.token.expired=Authentication token expired
error.token.invalid=Invalid authentication token
```

#### messages_ru.properties
```properties
# Общие ошибки
error.validation.error=Ошибка валидации
error.internal.server.error=Внутренняя ошибка сервера
error.service.unavailable=Сервис временно недоступен

# Ошибки твитов
error.tweet.not.found=Твит не найден
error.tweet.already.deleted=Твит был удален
error.tweet.access.denied=Доступ к твиту запрещен
error.tweet.too.old=Твит слишком старый для этой операции
error.tweet.content.invalid=Содержимое твита недопустимо
error.tweet.rate.limit.exceeded=Превышен лимит создания твитов

# Ошибки пользователей
error.user.not.found=Пользователь не найден
error.user.inactive=Аккаунт пользователя неактивен
error.user.banned=Аккаунт пользователя заблокирован
error.user.service.unavailable=Сервис пользователей недоступен

# Ошибки социальных действий
error.like.already.exists=Пользователь уже лайкнул этот твит
error.like.not.found=Лайк не найден
error.like.self.not.allowed=Пользователи не могут лайкать свои твиты
error.retweet.already.exists=Пользователь уже ретвитнул этот твит
error.retweet.not.found=Ретвит не найден
error.retweet.self.not.allowed=Пользователи не могут ретвитить свои твиты
error.social.action.rate.limit.exceeded=Превышен лимит социальных действий

# Ошибки безопасности
error.spam.detected=Обнаружен спам
error.abuse.detected=Обнаружено злоупотребление
error.content.policy.violation=Контент нарушает политику

# Ошибки авторизации
error.unauthorized=Требуется аутентификация
error.forbidden=Недостаточно прав
error.token.expired=Токен аутентификации истек
error.token.invalid=Недействительный токен аутентификации
```

## 7. Логирование ошибок

### 7.1 ErrorLoggingService

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorLoggingService {
    
    private final ErrorLoggingProperties properties;
    
    /**
     * Logs an error with structured information
     *
     * @param ex the exception
     * @param request the HTTP request
     * @param errorCode the error code
     */
    public void logError(Exception ex, HttpServletRequest request, TweetErrorCode errorCode) {
        if (!shouldLogError(errorCode)) {
            return;
        }
        
        ErrorLogEntry logEntry = ErrorLogEntry.builder()
            .timestamp(Instant.now())
            .errorCode(errorCode.getCode())
            .errorMessage(ex.getMessage())
            .exceptionType(ex.getClass().getSimpleName())
            .requestMethod(request.getMethod())
            .requestUri(request.getRequestURI())
            .requestId(getRequestId(request))
            .userId(getUserId(request))
            .userAgent(request.getHeader("User-Agent"))
            .clientIp(getClientIp(request))
            .stackTrace(getStackTrace(ex))
            .context(getErrorContext(ex))
            .build();
        
        logStructuredError(logEntry);
    }
    
    /**
     * Logs a structured error entry
     *
     * @param logEntry the error log entry
     */
    private void logStructuredError(ErrorLogEntry logEntry) {
        if (properties.isJsonFormat()) {
            log.error("Error occurred: {}", toJson(logEntry));
        } else {
            log.error("Error occurred: code={}, message={}, request={} {}, user={}, ip={}", 
                logEntry.getErrorCode(),
                logEntry.getErrorMessage(),
                logEntry.getRequestMethod(),
                logEntry.getRequestUri(),
                logEntry.getUserId(),
                logEntry.getClientIp(),
                logEntry.getExceptionType());
        }
    }
    
    /**
     * Determines if an error should be logged based on its severity
     *
     * @param errorCode the error code
     * @return true if error should be logged
     */
    private boolean shouldLogError(TweetErrorCode errorCode) {
        return switch (errorCode) {
            case INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE, INTEGRATION_ERROR -> true;
            case VALIDATION_ERROR, TWEET_NOT_FOUND, USER_NOT_FOUND -> properties.isLogValidationErrors();
            case SPAM_DETECTED, ABUSE_DETECTED -> properties.isLogSecurityErrors();
            default -> properties.isLogAllErrors();
        };
    }
    
    private String getRequestId(HttpServletRequest request) {
        return request.getHeader("X-Request-ID");
    }
    
    private String getUserId(HttpServletRequest request) {
        // Extract user ID from JWT token or session
        return request.getHeader("X-User-ID");
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    private String getStackTrace(Exception ex) {
        if (properties.isIncludeStackTrace()) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            return sw.toString();
        }
        return null;
    }
    
    private Map<String, Object> getErrorContext(Exception ex) {
        if (ex instanceof TweetException tweetEx) {
            return tweetEx.getContext();
        }
        return Collections.emptyMap();
    }
    
    private String toJson(ErrorLogEntry logEntry) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(logEntry);
        } catch (Exception e) {
            log.warn("Failed to serialize error log entry to JSON", e);
            return logEntry.toString();
        }
    }
}
```

### 7.2 ErrorLogEntry

```java
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorLogEntry {
    
    private Instant timestamp;
    private String errorCode;
    private String errorMessage;
    private String exceptionType;
    private String requestMethod;
    private String requestUri;
    private String requestId;
    private String userId;
    private String userAgent;
    private String clientIp;
    private String stackTrace;
    private Map<String, Object> context;
}
```

## 8. Мониторинг и метрики ошибок

### 8.1 ErrorMetricsService

```java
@Service
@RequiredArgsConstructor
public class ErrorMetricsService {
    
    private final MeterRegistry meterRegistry;
    
    /**
     * Records an error occurrence
     *
     * @param errorCode the error code
     */
    public void recordError(TweetErrorCode errorCode) {
        Counter.builder("tweet.errors.total")
            .tag("error_code", errorCode.getCode())
            .tag("error_type", getErrorType(errorCode))
            .register(meterRegistry)
            .increment();
    }
    
    /**
     * Records an error with additional tags
     *
     * @param errorCode the error code
     * @param tags additional tags
     */
    public void recordError(TweetErrorCode errorCode, Map<String, String> tags) {
        Counter.Builder<Counter> builder = Counter.builder("tweet.errors.total")
            .tag("error_code", errorCode.getCode())
            .tag("error_type", getErrorType(errorCode));
        
        tags.forEach(builder::tag);
        builder.register(meterRegistry).increment();
    }
    
    /**
     * Records error response time
     *
     * @param errorCode the error code
     * @param duration the response duration
     */
    public void recordErrorResponseTime(TweetErrorCode errorCode, Duration duration) {
        Timer.builder("tweet.errors.response_time")
            .tag("error_code", errorCode.getCode())
            .register(meterRegistry)
            .record(duration);
    }
    
    /**
     * Records error rate by user
     *
     * @param userId the user ID
     * @param errorCode the error code
     */
    public void recordUserError(UUID userId, TweetErrorCode errorCode) {
        Counter.builder("tweet.errors.by_user")
            .tag("user_id", userId.toString())
            .tag("error_code", errorCode.getCode())
            .register(meterRegistry)
            .increment();
    }
    
    /**
     * Records error rate by endpoint
     *
     * @param endpoint the endpoint
     * @param errorCode the error code
     */
    public void recordEndpointError(String endpoint, TweetErrorCode errorCode) {
        Counter.builder("tweet.errors.by_endpoint")
            .tag("endpoint", endpoint)
            .tag("error_code", errorCode.getCode())
            .register(meterRegistry)
            .increment();
    }
    
    private String getErrorType(TweetErrorCode errorCode) {
        return switch (errorCode) {
            case VALIDATION_ERROR, TWEET_CONTENT_INVALID -> "validation";
            case TWEET_NOT_FOUND, USER_NOT_FOUND -> "not_found";
            case TWEET_ACCESS_DENIED, FORBIDDEN -> "access_denied";
            case SPAM_DETECTED, ABUSE_DETECTED -> "security";
            case INTEGRATION_ERROR, SERVICE_UNAVAILABLE -> "integration";
            case INTERNAL_SERVER_ERROR -> "internal";
            default -> "other";
        };
    }
}
```

## 9. Конфигурация обработки ошибок

### 9.1 ErrorHandlingProperties

```java
@ConfigurationProperties(prefix = "app.error-handling")
@Data
public class ErrorHandlingProperties {
    
    /**
     * Enable error handling features
     */
    private boolean enabled = true;
    
    /**
     * Include stack traces in error responses
     */
    private boolean includeStackTrace = false;
    
    /**
     * Include exception details in error responses
     */
    private boolean includeExceptionDetails = false;
    
    /**
     * Default error message for internal errors
     */
    private String defaultInternalErrorMessage = "An unexpected error occurred";
    
    /**
     * Error response format
     */
    private ErrorResponseFormat responseFormat = ErrorResponseFormat.PROBLEM_DETAIL;
    
    /**
     * Localization settings
     */
    private Localization localization = new Localization();
    
    /**
     * Logging settings
     */
    private Logging logging = new Logging();
    
    /**
     * Metrics settings
     */
    private Metrics metrics = new Metrics();
    
    @Data
    public static class Localization {
        private boolean enabled = true;
        private List<Locale> supportedLocales = List.of(Locale.ENGLISH, Locale.forLanguageTag("ru"));
        private Locale defaultLocale = Locale.ENGLISH;
    }
    
    @Data
    public static class Logging {
        private boolean enabled = true;
        private boolean jsonFormat = true;
        private boolean includeStackTrace = true;
        private boolean logValidationErrors = false;
        private boolean logSecurityErrors = true;
        private boolean logAllErrors = false;
    }
    
    @Data
    public static class Metrics {
        private boolean enabled = true;
        private boolean recordUserErrors = true;
        private boolean recordEndpointErrors = true;
        private boolean recordResponseTimes = true;
    }
    
    public enum ErrorResponseFormat {
        PROBLEM_DETAIL,
        CUSTOM,
        SIMPLE
    }
}
```

## 10. Тестирование обработки ошибок

### 10.1 Unit тесты обработки ошибок

```java
@ExtendWith(MockitoExtension.class)
class TweetGlobalExceptionHandlerTest {
    
    @Mock
    private ErrorCodeMapping errorCodeMapping;
    
    @Mock
    private ErrorLocalizationService localizationService;
    
    @Mock
    private ErrorMetricsService metricsService;
    
    @InjectMocks
    private TweetGlobalExceptionHandler exceptionHandler;
    
    @Test
    void handleTweetException_ShouldReturnProperResponse() {
        // Given
        UUID tweetId = UUID.randomUUID();
        TweetNotFoundException ex = new TweetNotFoundException(tweetId);
        HttpServletRequest request = createMockRequest();
        
        when(errorCodeMapping.getHttpStatus(TweetErrorCode.TWEET_NOT_FOUND))
            .thenReturn(HttpStatus.NOT_FOUND);
        
        // When
        ResponseEntity<TweetErrorResponse> response = exceptionHandler.handleTweetException(ex, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo(TweetErrorCode.TWEET_NOT_FOUND.getCode());
        assertThat(response.getBody().getContext()).containsEntry("tweetId", tweetId);
    }
    
    @Test
    void handleValidationException_ShouldReturnValidationResponse() {
        // Given
        ConstraintViolationException ex = createConstraintViolationException();
        HttpServletRequest request = createMockRequest();
        
        // When
        ResponseEntity<ValidationErrorResponse> response = 
            exceptionHandler.handleConstraintViolation(ex, request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrors()).isNotEmpty();
    }
    
    private HttpServletRequest createMockRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/tweets");
        when(request.getHeader("X-Request-ID")).thenReturn("req-123");
        return request;
    }
    
    private ConstraintViolationException createConstraintViolationException() {
        // Create a mock constraint violation exception
        return mock(ConstraintViolationException.class);
    }
}
```

### 10.2 Integration тесты

```java
@SpringBootTest
@Testcontainers
class ErrorHandlingIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void createTweet_WithInvalidData_ShouldReturnValidationError() {
        // Given
        CreateTweetRequest request = new CreateTweetRequest("", UUID.randomUUID());
        
        // When
        ResponseEntity<ValidationErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/tweets", 
            request, 
            ValidationErrorResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrors()).isNotEmpty();
    }
    
    @Test
    void getTweet_WithNonExistentId_ShouldReturnNotFoundError() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        
        // When
        ResponseEntity<TweetErrorResponse> response = restTemplate.getForEntity(
            "/api/v1/tweets/" + nonExistentId, 
            TweetErrorResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo(TweetErrorCode.TWEET_NOT_FOUND.getCode());
    }
}
```

## 11. Заключение

### 11.1 Ключевые архитектурные решения

1. **Стандартизированная обработка ошибок** - следование RFC 7807 Problem Details
2. **Типизированные исключения** - TweetException с контекстом и метаданными
3. **Централизованная обработка** - TweetGlobalExceptionHandler для всех ошибок
4. **Локализация сообщений** - поддержка множественных языков
5. **Структурированное логирование** - с контекстом и метриками
6. **Мониторинг и метрики** - отслеживание ошибок и производительности

### 11.2 Готовность к реализации

- ✅ **Стандартные коды ошибок** для всех операций
- ✅ **Структуры error response** в формате ProblemDetail
- ✅ **Специализированные исключения** для tweet-specific ошибок
- ✅ **Глобальная обработка ошибок** через TweetGlobalExceptionHandler
- ✅ **Локализация сообщений** с поддержкой множественных языков
- ✅ **Логирование ошибок** с структурированной информацией
- ✅ **Мониторинг и метрики** для отслеживания ошибок
- ✅ **Конфигурация** через ErrorHandlingProperties
- ✅ **Тестирование** unit и integration тестов

### 11.3 Критерии успешности

- ✅ **Полное покрытие обработки ошибок** для всех операций
- ✅ **Соответствие стандартам REST API** с RFC 7807
- ✅ **Интеграция с существующими паттернами** users-api и shared/common-lib
- ✅ **Пользовательский опыт** с понятными сообщениями об ошибках
- ✅ **Производительность** с мониторингом и метриками
- ✅ **Тестируемость** с unit и integration тестами

---

*Документ создан: 2025-01-27*  
*Версия: 1.0*  
*Статус: Completed*
