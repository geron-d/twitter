# Проектирование системы валидации входных данных Tweet API

## Meta
- project: twitter-tweet-api
- design_date: 2025-01-27
- designer: AI Assistant
- version: 1.0
- status: completed
- step: 4.10

## Executive Summary

Данный документ содержит детальное проектирование системы валидации входных данных для сервиса Tweet API. Система спроектирована с учетом интеграции с существующими паттернами users-api, использования shared/common-lib компонентов и обеспечения высокого уровня безопасности.

## 1. Архитектурные принципы валидации

### 1.1 Многоуровневая валидация

#### Уровни валидации:
1. **DTO Level Validation** - Bean Validation аннотации на уровне DTO
2. **Service Level Validation** - Бизнес-правила и кастомная валидация
3. **Entity Level Validation** - Валидация на уровне JPA Entity
4. **Database Level Validation** - Constraints на уровне БД

#### Принципы:
- **Fail Fast** - валидация на самом раннем этапе
- **Separation of Concerns** - разделение технической и бизнес валидации
- **Consistency** - единообразные сообщения об ошибках
- **Security First** - приоритет безопасности над удобством

### 1.2 Интеграция с существующими паттернами

#### Использование shared/common-lib:
- **ValidationException** - базовый класс для всех ошибок валидации
- **ValidationType** - типизация ошибок валидации
- **GlobalExceptionHandler** - централизованная обработка ошибок
- **LoggableRequestAspect** - логирование валидационных ошибок

#### Следование паттернам users-api:
- **UserValidator** - паттерн централизованной валидации
- **Bean Validation** - использование Jakarta Validation
- **Custom Validators** - кастомные валидаторы для бизнес-правил

## 2. Bean Validation аннотации

### 2.1 Стандартные аннотации

#### CreateTweetRequest валидация:
```java
public record CreateTweetRequest(
    
    @NotBlank(message = "Tweet content cannot be blank")
    @Size(min = 1, max = 280, message = "Tweet content must be between 1 and 280 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\p{P}\\p{Z}]+$", 
             message = "Tweet content contains invalid characters")
    String content,
    
    @NotNull(message = "User ID cannot be null")
    @Valid
    UUID userId
) {}
```

#### UpdateTweetRequest валидация:
```java
public record UpdateTweetRequest(
    
    @NotBlank(message = "Tweet content cannot be blank")
    @Size(min = 1, max = 280, message = "Tweet content must be between 1 and 280 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\p{P}\\p{Z}]+$", 
             message = "Tweet content contains invalid characters")
    String content
) {}
```

#### Social Action Request валидация:
```java
public record LikeTweetRequest(
    
    @NotNull(message = "User ID cannot be null")
    @Valid
    UUID userId
) {}

public record RetweetRequest(
    
    @NotNull(message = "User ID cannot be null")
    @Valid
    UUID userId,
    
    @Size(max = 280, message = "Retweet comment cannot exceed 280 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\p{P}\\p{Z}]*$", 
             message = "Retweet comment contains invalid characters")
    String comment
) {}
```

### 2.2 Кастомные аннотации валидации

#### Проверка существования пользователя:
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserExistsValidator.class)
@Documented
public @interface UserExists {
    String message() default "User does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

#### Проверка на самолайк/саморетвит:
```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoSelfActionValidator.class)
@Documented
public @interface NoSelfAction {
    String message() default "User cannot perform action on their own tweet";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

#### Проверка существования твита:
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TweetExistsValidator.class)
@Documented
public @interface TweetExists {
    String message() default "Tweet does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

#### Проверка прав доступа к твиту:
```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TweetAccessValidator.class)
@Documented
public @interface TweetAccess {
    String message() default "User does not have access to this tweet";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

## 3. Кастомные валидаторы

### 3.1 UserExistsValidator

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class UserExistsValidator implements ConstraintValidator<UserExists, UUID> {
    
    private final UserServiceClient userServiceClient;
    
    @Override
    public void initialize(UserExists constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(UUID userId, ConstraintValidatorContext context) {
        if (userId == null) {
            return true; // Let @NotNull handle null validation
        }
        
        try {
            boolean exists = userServiceClient.userExists(userId);
            if (!exists) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "User with ID '" + userId + "' does not exist"
                ).addConstraintViolation();
            }
            return exists;
        } catch (Exception e) {
            log.warn("Failed to validate user existence for ID: {}", userId, e);
            // In case of service unavailability, assume user exists for graceful degradation
            return true;
        }
    }
}
```

### 3.2 NoSelfActionValidator

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class NoSelfActionValidator implements ConstraintValidator<NoSelfAction, Object> {
    
    private final TweetService tweetService;
    
    @Override
    public void initialize(NoSelfAction constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(Object request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        
        try {
            UUID tweetId = extractTweetId(request);
            UUID userId = extractUserId(request);
            
            if (tweetId == null || userId == null) {
                return true; // Let other validators handle null values
            }
            
            Tweet tweet = tweetService.findById(tweetId);
            if (tweet == null) {
                return true; // Let TweetExistsValidator handle this
            }
            
            boolean isSelfAction = tweet.getUserId().equals(userId);
            if (isSelfAction) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "User cannot perform action on their own tweet"
                ).addConstraintViolation();
            }
            
            return !isSelfAction;
        } catch (Exception e) {
            log.warn("Failed to validate self-action for request: {}", request, e);
            return true; // Allow in case of errors
        }
    }
    
    private UUID extractTweetId(Object request) {
        // Extract tweetId from request based on request type
        if (request instanceof LikeTweetRequest) {
            return getTweetIdFromContext(); // From path parameter
        }
        // Similar for other request types
        return null;
    }
    
    private UUID extractUserId(Object request) {
        if (request instanceof LikeTweetRequest likeRequest) {
            return likeRequest.userId();
        }
        if (request instanceof RetweetRequest retweetRequest) {
            return retweetRequest.userId();
        }
        return null;
    }
}
```

### 3.3 TweetExistsValidator

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class TweetExistsValidator implements ConstraintValidator<TweetExists, UUID> {
    
    private final TweetRepository tweetRepository;
    
    @Override
    public void initialize(TweetExists constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(UUID tweetId, ConstraintValidatorContext context) {
        if (tweetId == null) {
            return true; // Let @NotNull handle null validation
        }
        
        try {
            boolean exists = tweetRepository.existsByIdAndIsDeletedFalse(tweetId);
            if (!exists) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Tweet with ID '" + tweetId + "' does not exist or has been deleted"
                ).addConstraintViolation();
            }
            return exists;
        } catch (Exception e) {
            log.warn("Failed to validate tweet existence for ID: {}", tweetId, e);
            return false; // Fail validation in case of database errors
        }
    }
}
```

### 3.4 TweetAccessValidator

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class TweetAccessValidator implements ConstraintValidator<TweetAccess, Object> {
    
    private final TweetService tweetService;
    
    @Override
    public void initialize(TweetAccess constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(Object request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        
        try {
            UUID tweetId = extractTweetId(request);
            UUID userId = extractUserId(request);
            
            if (tweetId == null || userId == null) {
                return true; // Let other validators handle null values
            }
            
            Tweet tweet = tweetService.findById(tweetId);
            if (tweet == null) {
                return true; // Let TweetExistsValidator handle this
            }
            
            // Check if user is the author (for update/delete operations)
            boolean hasAccess = tweet.getUserId().equals(userId);
            if (!hasAccess) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "User does not have access to modify this tweet"
                ).addConstraintViolation();
            }
            
            return hasAccess;
        } catch (Exception e) {
            log.warn("Failed to validate tweet access for request: {}", request, e);
            return false; // Fail validation in case of errors
        }
    }
    
    private UUID extractTweetId(Object request) {
        // Extract tweetId from request based on request type
        return getTweetIdFromContext(); // From path parameter
    }
    
    private UUID extractUserId(Object request) {
        if (request instanceof UpdateTweetRequest) {
            return getUserIdFromContext(); // From authentication context
        }
        return null;
    }
}
```

## 4. Валидация на уровне Entity

### 4.1 Tweet Entity валидация

```java
@Entity
@Table(name = "tweets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tweet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID cannot be null")
    private UUID userId;
    
    @Column(name = "content", nullable = false, length = 280)
    @NotBlank(message = "Tweet content cannot be blank")
    @Size(min = 1, max = 280, message = "Tweet content must be between 1 and 280 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\p{P}\\p{Z}]+$", 
             message = "Tweet content contains invalid characters")
    private String content;
    
    @Column(name = "created_at", nullable = false)
    @NotNull(message = "Created at timestamp cannot be null")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_deleted", nullable = false)
    @NotNull(message = "Is deleted flag cannot be null")
    private Boolean isDeleted = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 4.2 Like Entity валидация

```java
@Entity
@Table(name = "likes", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tweet_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tweet_id", nullable = false)
    @NotNull(message = "Tweet ID cannot be null")
    private UUID tweetId;
    
    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID cannot be null")
    private UUID userId;
    
    @Column(name = "created_at", nullable = false)
    @NotNull(message = "Created at timestamp cannot be null")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

### 4.3 Retweet Entity валидация

```java
@Entity
@Table(name = "retweets", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tweet_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Retweet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tweet_id", nullable = false)
    @NotNull(message = "Tweet ID cannot be null")
    private UUID tweetId;
    
    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID cannot be null")
    private UUID userId;
    
    @Column(name = "comment", length = 280)
    @Size(max = 280, message = "Retweet comment cannot exceed 280 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\p{P}\\p{Z}]*$", 
             message = "Retweet comment contains invalid characters")
    private String comment;
    
    @Column(name = "created_at", nullable = false)
    @NotNull(message = "Created at timestamp cannot be null")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

## 5. Санитизация контента

### 5.1 ContentSanitizer

```java
@Component
@Slf4j
public class ContentSanitizer {
    
    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("(?i)<script[^>]*>.*?</script>");
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("(?i)javascript:");
    private static final Pattern SPAM_PATTERN = Pattern.compile("(?i)(spam|scam|free money|click here)");
    
    /**
     * Sanitizes tweet content by removing potentially dangerous elements
     * and normalizing text.
     *
     * @param content the raw tweet content
     * @return sanitized content safe for storage and display
     */
    public String sanitizeTweetContent(String content) {
        if (content == null) {
            return null;
        }
        
        String sanitized = content.trim();
        
        // Remove HTML tags
        sanitized = HTML_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove script tags
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove javascript: protocols
        sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Normalize whitespace
        sanitized = sanitized.replaceAll("\\s+", " ");
        
        // Check for spam patterns
        if (SPAM_PATTERN.matcher(sanitized).find()) {
            log.warn("Potential spam content detected: {}", sanitized);
            // Could implement additional spam detection logic here
        }
        
        return sanitized.trim();
    }
    
    /**
     * Validates that content contains only allowed characters
     *
     * @param content the content to validate
     * @return true if content is valid, false otherwise
     */
    public boolean isValidContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        // Check for valid Unicode characters (letters, numbers, punctuation, spaces)
        return content.matches("^[\\p{L}\\p{N}\\p{P}\\p{Z}]+$");
    }
    
    /**
     * Escapes HTML special characters for safe display
     *
     * @param content the content to escape
     * @return HTML-escaped content
     */
    public String escapeHtml(String content) {
        if (content == null) {
            return null;
        }
        
        return content
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
```

### 5.2 ContentValidationService

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentValidationService {
    
    private final ContentSanitizer contentSanitizer;
    
    /**
     * Validates and sanitizes tweet content
     *
     * @param content the raw content
     * @return validation result with sanitized content
     */
    public ContentValidationResult validateTweetContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return ContentValidationResult.error("Tweet content cannot be empty");
        }
        
        if (content.length() > 280) {
            return ContentValidationResult.error("Tweet content cannot exceed 280 characters");
        }
        
        if (!contentSanitizer.isValidContent(content)) {
            return ContentValidationResult.error("Tweet content contains invalid characters");
        }
        
        String sanitizedContent = contentSanitizer.sanitizeTweetContent(content);
        
        if (sanitizedContent.length() != content.length()) {
            log.info("Content was sanitized: original length={}, sanitized length={}", 
                    content.length(), sanitizedContent.length());
        }
        
        return ContentValidationResult.success(sanitizedContent);
    }
    
    /**
     * Validates retweet comment
     *
     * @param comment the comment to validate
     * @return validation result with sanitized comment
     */
    public ContentValidationResult validateRetweetComment(String comment) {
        if (comment == null) {
            return ContentValidationResult.success(null);
        }
        
        if (comment.length() > 280) {
            return ContentValidationResult.error("Retweet comment cannot exceed 280 characters");
        }
        
        if (!contentSanitizer.isValidContent(comment)) {
            return ContentValidationResult.error("Retweet comment contains invalid characters");
        }
        
        String sanitizedComment = contentSanitizer.sanitizeTweetContent(comment);
        return ContentValidationResult.success(sanitizedComment);
    }
}
```

### 5.3 ContentValidationResult

```java
@Getter
@AllArgsConstructor
public class ContentValidationResult {
    
    private final boolean valid;
    private final String content;
    private final String errorMessage;
    
    public static ContentValidationResult success(String content) {
        return new ContentValidationResult(true, content, null);
    }
    
    public static ContentValidationResult error(String errorMessage) {
        return new ContentValidationResult(false, null, errorMessage);
    }
}
```

## 6. Централизованная валидация

### 6.1 TweetValidator

```java
public interface TweetValidator {
    
    /**
     * Validates tweet creation request
     *
     * @param request the create tweet request
     * @throws ValidationException if validation fails
     */
    void validateForCreate(CreateTweetRequest request);
    
    /**
     * Validates tweet update request
     *
     * @param tweetId the tweet ID
     * @param request the update tweet request
     * @throws ValidationException if validation fails
     */
    void validateForUpdate(UUID tweetId, UpdateTweetRequest request);
    
    /**
     * Validates tweet deletion request
     *
     * @param tweetId the tweet ID
     * @param userId the user ID
     * @throws ValidationException if validation fails
     */
    void validateForDelete(UUID tweetId, UUID userId);
    
    /**
     * Validates like tweet request
     *
     * @param tweetId the tweet ID
     * @param request the like tweet request
     * @throws ValidationException if validation fails
     */
    void validateForLike(UUID tweetId, LikeTweetRequest request);
    
    /**
     * Validates retweet request
     *
     * @param tweetId the tweet ID
     * @param request the retweet request
     * @throws ValidationException if validation fails
     */
    void validateForRetweet(UUID tweetId, RetweetRequest request);
}
```

### 6.2 TweetValidatorImpl

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class TweetValidatorImpl implements TweetValidator {
    
    private final TweetRepository tweetRepository;
    private final UserServiceClient userServiceClient;
    private final ContentValidationService contentValidationService;
    private final Validator validator;
    
    @Override
    public void validateForCreate(CreateTweetRequest request) {
        // Bean validation
        Set<ConstraintViolation<CreateTweetRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new FormatValidationException(
                "CREATE_TWEET_VALIDATION",
                "Validation failed for create tweet request: " + 
                violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "))
            );
        }
        
        // Content validation
        ContentValidationResult contentResult = contentValidationService.validateTweetContent(request.content());
        if (!contentResult.isValid()) {
            throw new FormatValidationException(
                "CONTENT_VALIDATION",
                contentResult.getErrorMessage()
            );
        }
        
        // User existence validation
        try {
            boolean userExists = userServiceClient.userExists(request.userId());
            if (!userExists) {
                throw new UniquenessValidationException(
                    "userId", 
                    request.userId().toString(), 
                    "User does not exist"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to validate user existence for ID: {}", request.userId(), e);
            throw new BusinessRuleValidationException(
                "USER_SERVICE_UNAVAILABLE",
                "Unable to validate user existence"
            );
        }
    }
    
    @Override
    public void validateForUpdate(UUID tweetId, UpdateTweetRequest request) {
        // Bean validation
        Set<ConstraintViolation<UpdateTweetRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new FormatValidationException(
                "UPDATE_TWEET_VALIDATION",
                "Validation failed for update tweet request: " + 
                violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "))
            );
        }
        
        // Content validation
        ContentValidationResult contentResult = contentValidationService.validateTweetContent(request.content());
        if (!contentResult.isValid()) {
            throw new FormatValidationException(
                "CONTENT_VALIDATION",
                contentResult.getErrorMessage()
            );
        }
        
        // Tweet existence and access validation
        Tweet tweet = tweetRepository.findByIdAndIsDeletedFalse(tweetId)
            .orElseThrow(() -> new UniquenessValidationException(
                "tweetId", 
                tweetId.toString(), 
                "Tweet not found or has been deleted"
            ));
        
        // Additional business rule validation can be added here
        // For example, checking if tweet is too old to be updated
        if (tweet.getCreatedAt().isBefore(LocalDateTime.now().minusDays(7))) {
            throw new BusinessRuleValidationException(
                "TWEET_TOO_OLD",
                "Tweet is too old to be updated"
            );
        }
    }
    
    @Override
    public void validateForDelete(UUID tweetId, UUID userId) {
        Tweet tweet = tweetRepository.findByIdAndIsDeletedFalse(tweetId)
            .orElseThrow(() -> new UniquenessValidationException(
                "tweetId", 
                tweetId.toString(), 
                "Tweet not found or has been deleted"
            ));
        
        if (!tweet.getUserId().equals(userId)) {
            throw new BusinessRuleValidationException(
                "TWEET_ACCESS_DENIED",
                "User does not have permission to delete this tweet"
            );
        }
    }
    
    @Override
    public void validateForLike(UUID tweetId, LikeTweetRequest request) {
        // Bean validation
        Set<ConstraintViolation<LikeTweetRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new FormatValidationException(
                "LIKE_TWEET_VALIDATION",
                "Validation failed for like tweet request: " + 
                violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "))
            );
        }
        
        // Tweet existence validation
        Tweet tweet = tweetRepository.findByIdAndIsDeletedFalse(tweetId)
            .orElseThrow(() -> new UniquenessValidationException(
                "tweetId", 
                tweetId.toString(), 
                "Tweet not found or has been deleted"
            ));
        
        // Self-like validation
        if (tweet.getUserId().equals(request.userId())) {
            throw new BusinessRuleValidationException(
                "SELF_ACTION_NOT_ALLOWED",
                "User cannot like their own tweet"
            );
        }
        
        // Duplicate like validation
        boolean alreadyLiked = tweetRepository.existsLikeByTweetIdAndUserId(tweetId, request.userId());
        if (alreadyLiked) {
            throw new UniquenessValidationException(
                "like", 
                tweetId + ":" + request.userId(), 
                "User has already liked this tweet"
            );
        }
    }
    
    @Override
    public void validateForRetweet(UUID tweetId, RetweetRequest request) {
        // Bean validation
        Set<ConstraintViolation<RetweetRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new FormatValidationException(
                "RETWEET_VALIDATION",
                "Validation failed for retweet request: " + 
                violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "))
            );
        }
        
        // Comment validation if present
        if (request.comment() != null) {
            ContentValidationResult commentResult = contentValidationService.validateRetweetComment(request.comment());
            if (!commentResult.isValid()) {
                throw new FormatValidationException(
                    "COMMENT_VALIDATION",
                    commentResult.getErrorMessage()
                );
            }
        }
        
        // Tweet existence validation
        Tweet tweet = tweetRepository.findByIdAndIsDeletedFalse(tweetId)
            .orElseThrow(() -> new UniquenessValidationException(
                "tweetId", 
                tweetId.toString(), 
                "Tweet not found or has been deleted"
            ));
        
        // Self-retweet validation
        if (tweet.getUserId().equals(request.userId())) {
            throw new BusinessRuleValidationException(
                "SELF_ACTION_NOT_ALLOWED",
                "User cannot retweet their own tweet"
            );
        }
        
        // Duplicate retweet validation
        boolean alreadyRetweeted = tweetRepository.existsRetweetByTweetIdAndUserId(tweetId, request.userId());
        if (alreadyRetweeted) {
            throw new UniquenessValidationException(
                "retweet", 
                tweetId + ":" + request.userId(), 
                "User has already retweeted this tweet"
            );
        }
    }
}
```

## 7. Обработка ошибок валидации

### 7.1 Специализированные исключения

#### TweetValidationException
```java
public class TweetValidationException extends ValidationException {
    
    public TweetValidationException(String message) {
        super(message);
    }
    
    public TweetValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public ValidationType getValidationType() {
        return ValidationType.BUSINESS_RULE;
    }
}
```

#### ContentValidationException
```java
public class ContentValidationException extends ValidationException {
    
    public ContentValidationException(String message) {
        super(message);
    }
    
    public ContentValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public ValidationType getValidationType() {
        return ValidationType.FORMAT;
    }
}
```

### 7.2 Глобальная обработка ошибок

```java
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class TweetValidationExceptionHandler {
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex) {
        
        List<ValidationError> errors = ex.getConstraintViolations().stream()
            .map(violation -> new ValidationError(
                violation.getPropertyPath().toString(),
                violation.getMessage(),
                violation.getInvalidValue() != null ? violation.getInvalidValue().toString() : null
            ))
            .collect(Collectors.toList());
        
        ValidationErrorResponse response = ValidationErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message("Validation failed")
            .details(errors)
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> new ValidationError(
                error.getField(),
                error.getDefaultMessage(),
                error.getRejectedValue() != null ? error.getRejectedValue().toString() : null
            ))
            .collect(Collectors.toList());
        
        ValidationErrorResponse response = ValidationErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message("Validation failed")
            .details(errors)
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
}
```

## 8. Конфигурация валидации

### 8.1 ValidationConfig

```java
@Configuration
@EnableConfigurationProperties(ValidationProperties.class)
public class ValidationConfig {
    
    @Bean
    public Validator validator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }
    
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = 
            new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/validation");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600);
        return messageSource;
    }
    
    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.setValidationMessageSource(messageSource());
        return factoryBean;
    }
}
```

### 8.2 ValidationProperties

```java
@ConfigurationProperties(prefix = "app.validation")
@Data
public class ValidationProperties {
    
    /**
     * Maximum tweet content length
     */
    private int maxTweetLength = 280;
    
    /**
     * Maximum retweet comment length
     */
    private int maxCommentLength = 280;
    
    /**
     * Enable content sanitization
     */
    private boolean enableSanitization = true;
    
    /**
     * Enable spam detection
     */
    private boolean enableSpamDetection = true;
    
    /**
     * Maximum age for tweet updates (in days)
     */
    private int maxUpdateAgeDays = 7;
    
    /**
     * Enable duplicate action prevention
     */
    private boolean enableDuplicatePrevention = true;
}
```

## 9. Тестирование валидации

### 9.1 Unit тесты валидаторов

```java
@ExtendWith(MockitoExtension.class)
class TweetValidatorImplTest {
    
    @Mock
    private TweetRepository tweetRepository;
    
    @Mock
    private UserServiceClient userServiceClient;
    
    @Mock
    private ContentValidationService contentValidationService;
    
    @Mock
    private Validator validator;
    
    @InjectMocks
    private TweetValidatorImpl tweetValidator;
    
    @Test
    void validateForCreate_ValidRequest_ShouldNotThrow() {
        // Given
        CreateTweetRequest request = new CreateTweetRequest(
            "Valid tweet content", 
            UUID.randomUUID()
        );
        
        when(validator.validate(request)).thenReturn(Collections.emptySet());
        when(contentValidationService.validateTweetContent("Valid tweet content"))
            .thenReturn(ContentValidationResult.success("Valid tweet content"));
        when(userServiceClient.userExists(request.userId())).thenReturn(true);
        
        // When & Then
        assertDoesNotThrow(() -> tweetValidator.validateForCreate(request));
    }
    
    @Test
    void validateForCreate_InvalidContent_ShouldThrow() {
        // Given
        CreateTweetRequest request = new CreateTweetRequest(
            "", 
            UUID.randomUUID()
        );
        
        when(validator.validate(request)).thenReturn(Collections.emptySet());
        when(contentValidationService.validateTweetContent(""))
            .thenReturn(ContentValidationResult.error("Tweet content cannot be empty"));
        
        // When & Then
        assertThrows(FormatValidationException.class, 
            () -> tweetValidator.validateForCreate(request));
    }
    
    @Test
    void validateForLike_SelfLike_ShouldThrow() {
        // Given
        UUID tweetId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LikeTweetRequest request = new LikeTweetRequest(userId);
        
        Tweet tweet = Tweet.builder()
            .id(tweetId)
            .userId(userId)
            .content("Test tweet")
            .build();
        
        when(validator.validate(request)).thenReturn(Collections.emptySet());
        when(tweetRepository.findByIdAndIsDeletedFalse(tweetId))
            .thenReturn(Optional.of(tweet));
        
        // When & Then
        assertThrows(BusinessRuleValidationException.class, 
            () -> tweetValidator.validateForLike(tweetId, request));
    }
}
```

### 9.2 Integration тесты

```java
@SpringBootTest
@Testcontainers
class TweetValidationIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("tweet_test")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private TweetValidator tweetValidator;
    
    @Autowired
    private TweetRepository tweetRepository;
    
    @Test
    void validateForCreate_WithDatabase_ShouldWork() {
        // Given
        UUID userId = UUID.randomUUID();
        CreateTweetRequest request = new CreateTweetRequest(
            "Integration test tweet", 
            userId
        );
        
        // When & Then
        assertDoesNotThrow(() -> tweetValidator.validateForCreate(request));
    }
}
```

## 10. Мониторинг и метрики

### 10.1 Validation Metrics

```java
@Component
@RequiredArgsConstructor
public class ValidationMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public void recordValidationSuccess(String validatorType) {
        Counter.builder("validation.success")
            .tag("validator", validatorType)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordValidationFailure(String validatorType, String errorType) {
        Counter.builder("validation.failure")
            .tag("validator", validatorType)
            .tag("error", errorType)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordValidationDuration(String validatorType, Duration duration) {
        Timer.builder("validation.duration")
            .tag("validator", validatorType)
            .register(meterRegistry)
            .record(duration);
    }
}
```

### 10.2 Validation Aspect

```java
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationMetricsAspect {
    
    private final ValidationMetrics validationMetrics;
    
    @Around("@annotation(Validated)")
    public Object measureValidation(ProceedingJoinPoint joinPoint) throws Throwable {
        String validatorType = joinPoint.getTarget().getClass().getSimpleName();
        Timer.Sample sample = Timer.start();
        
        try {
            Object result = joinPoint.proceed();
            validationMetrics.recordValidationSuccess(validatorType);
            return result;
        } catch (ValidationException e) {
            validationMetrics.recordValidationFailure(validatorType, e.getClass().getSimpleName());
            throw e;
        } finally {
            sample.stop(Timer.builder("validation.duration")
                .tag("validator", validatorType)
                .register(validationMetrics.getMeterRegistry()));
        }
    }
}
```

## 11. Заключение

### 11.1 Ключевые архитектурные решения

1. **Многоуровневая валидация** - валидация на DTO, Service, Entity и Database уровнях
2. **Централизованная валидация** - единый TweetValidator для всех операций
3. **Интеграция с shared/common-lib** - использование существующих паттернов
4. **Санитизация контента** - защита от XSS и других атак
5. **Comprehensive error handling** - типизированные исключения и обработка ошибок
6. **Мониторинг и метрики** - отслеживание производительности валидации

### 11.2 Готовность к реализации

- ✅ **Bean Validation аннотации** для всех DTO
- ✅ **Кастомные валидаторы** для бизнес-правил
- ✅ **Entity валидация** с JPA constraints
- ✅ **Санитизация контента** для безопасности
- ✅ **Централизованная валидация** через TweetValidator
- ✅ **Обработка ошибок** с типизированными исключениями
- ✅ **Конфигурация** и настройки валидации
- ✅ **Тестирование** unit и integration тестов
- ✅ **Мониторинг** и метрики валидации

### 11.3 Критерии успешности

- ✅ **Полное покрытие валидации** всех входных данных
- ✅ **Соответствие стандартам безопасности** с санитизацией
- ✅ **Интеграция с существующими паттернами** users-api и shared/common-lib
- ✅ **Comprehensive error handling** с понятными сообщениями
- ✅ **Производительность** с мониторингом и метриками
- ✅ **Тестируемость** с unit и integration тестами

---

*Документ создан: 2025-01-27*  
*Версия: 1.0*  
*Статус: Completed*
