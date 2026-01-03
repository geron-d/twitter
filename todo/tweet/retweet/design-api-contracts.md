# Проектирование API и контрактов: Ретвитнуть с опциональным комментарием

## Дата создания
2025-01-27

## Обзор
Данный документ содержит проектирование API и контрактов для эндпоинта `POST /api/v1/tweets/{tweetId}/retweet`. Проектирование выполнено на основе анализа требований и стандартов проекта.

## 1. OpenAPI интерфейс RetweetApi

### 1.1 Структура интерфейса

**Расположение**: `services/tweet-api/src/main/java/com/twitter/controller/RetweetApi.java`

**Аннотации**:
- `@Tag(name = "Retweet Management", description = "API for managing retweets in the Twitter system")`

**Метод**: `retweetTweet`

### 1.2 Описание метода

**Сигнатура**:
```java
ResponseEntity<RetweetResponseDto> retweetTweet(
    @Parameter(...) UUID tweetId,
    @Parameter(...) RetweetRequestDto retweetRequest
);
```

**@Operation**:
- `summary`: "Retweet a tweet"
- `description`: "Retweets a tweet by creating a retweet record with an optional comment. " +
    "It performs validation on the request data, checks if the tweet exists and is not deleted, " +
    "verifies that the user exists, prevents self-retweets (users cannot retweet their own tweets), " +
    "and ensures uniqueness (a user can only retweet a tweet once). " +
    "The retweet operation is atomic and updates the tweet's retweets count. " +
    "An optional comment can be provided (1-280 characters), but null comment is also allowed."

### 1.3 API Responses

#### 1.3.1 201 Created - Успешное создание ретвита

```java
@ApiResponse(
    responseCode = "201",
    description = "Tweet retweeted successfully",
    content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = RetweetResponseDto.class),
        examples = @ExampleObject(
            name = "Created Retweet",
            summary = "Example created retweet with comment",
            value = """
                {
                  "id": "987e6543-e21b-43d2-b654-321987654321",
                  "tweetId": "223e4567-e89b-12d3-a456-426614174001",
                  "userId": "123e4567-e89b-12d3-a456-426614174000",
                  "comment": "Great tweet!",
                  "createdAt": "2025-01-27T15:30:00Z"
                }
                """
        )
    )
)
```

**Дополнительный пример без комментария**:
```java
@ExampleObject(
    name = "Created Retweet Without Comment",
    summary = "Example created retweet without comment",
    value = """
        {
          "id": "987e6543-e21b-43d2-b654-321987654321",
          "tweetId": "223e4567-e89b-12d3-a456-426614174001",
          "userId": "123e4567-e89b-12d3-a456-426614174000",
          "comment": null,
          "createdAt": "2025-01-27T15:30:00Z"
        }
        """
)
```

#### 1.3.2 400 Bad Request - Ошибка валидации

**Ошибка валидации userId**:
```java
@ApiResponse(
    responseCode = "400",
    description = "Validation error",
    content = @Content(
        mediaType = "application/problem+json",
        examples = @ExampleObject(
            name = "User ID Validation Error",
            summary = "User ID is null or invalid",
            value = """
                {
                  "type": "https://example.com/errors/validation-error",
                  "title": "Validation Error",
                  "status": 400,
                  "detail": "Validation failed: userId: User ID cannot be null",
                  "timestamp": "2025-01-27T15:30:00Z"
                }
                """
        )
    )
)
```

**Ошибка валидации комментария**:
```java
@ApiResponse(
    responseCode = "400",
    description = "Comment validation error",
    content = @Content(
        mediaType = "application/problem+json",
        examples = @ExampleObject(
            name = "Comment Validation Error",
            summary = "Comment is empty string or exceeds max length",
            value = """
                {
                  "type": "https://example.com/errors/format-validation",
                  "title": "Format Validation Error",
                  "status": 400,
                  "detail": "Comment must be between 1 and 280 characters if provided, or null. Empty string is not allowed.",
                  "fieldName": "comment",
                  "constraintName": "COMMENT_VALIDATION",
                  "timestamp": "2025-01-27T15:30:00Z"
                }
                """
        )
    )
)
```

**Ошибка формата UUID**:
```java
@ApiResponse(
    responseCode = "400",
    description = "Invalid UUID format",
    content = @Content(
        mediaType = "application/problem+json",
        examples = @ExampleObject(
            name = "Invalid UUID Format Error",
            summary = "Invalid tweet ID format",
            value = """
                {
                  "type": "https://example.com/errors/validation-error",
                  "title": "Validation Error",
                  "status": 400,
                  "detail": "Invalid UUID format for tweetId parameter",
                  "timestamp": "2025-01-27T15:30:00Z"
                }
                """
        )
    )
)
```

#### 1.3.3 404 Not Found - Ресурс не найден

**Твит не найден**:
```java
@ApiResponse(
    responseCode = "404",
    description = "Tweet not found",
    content = @Content(
        mediaType = "application/problem+json",
        examples = @ExampleObject(
            name = "Tweet Not Found Error",
            summary = "Tweet does not exist or is deleted",
            value = """
                {
                  "type": "https://example.com/errors/business-rule-validation",
                  "title": "Business Rule Validation Error",
                  "status": 404,
                  "detail": "Business rule 'TWEET_NOT_FOUND' violated for context: 223e4567-e89b-12d3-a456-426614174001",
                  "ruleName": "TWEET_NOT_FOUND",
                  "context": "223e4567-e89b-12d3-a456-426614174001",
                  "timestamp": "2025-01-27T15:30:00Z"
                }
                """
        )
    )
)
```

**Пользователь не найден**:
```java
@ApiResponse(
    responseCode = "404",
    description = "User not found",
    content = @Content(
        mediaType = "application/problem+json",
        examples = @ExampleObject(
            name = "User Not Found Error",
            summary = "User does not exist",
            value = """
                {
                  "type": "https://example.com/errors/business-rule-validation",
                  "title": "Business Rule Validation Error",
                  "status": 404,
                  "detail": "Business rule 'USER_NOT_EXISTS' violated for context: 123e4567-e89b-12d3-a456-426614174000",
                  "ruleName": "USER_NOT_EXISTS",
                  "context": "123e4567-e89b-12d3-a456-426614174000",
                  "timestamp": "2025-01-27T15:30:00Z"
                }
                """
        )
    )
)
```

#### 1.3.4 409 Conflict - Нарушение бизнес-правил

**Self-retweet запрещен**:
```java
@ApiResponse(
    responseCode = "409",
    description = "Business rule violation - self-retweet",
    content = @Content(
        mediaType = "application/problem+json",
        examples = @ExampleObject(
            name = "Self-Retweet Error",
            summary = "User cannot retweet their own tweet",
            value = """
                {
                  "type": "https://example.com/errors/business-rule-validation",
                  "title": "Business Rule Validation Error",
                  "status": 409,
                  "detail": "Business rule 'SELF_RETWEET_NOT_ALLOWED' violated for context: Users cannot retweet their own tweets",
                  "ruleName": "SELF_RETWEET_NOT_ALLOWED",
                  "context": "Users cannot retweet their own tweets",
                  "timestamp": "2025-01-27T15:30:00Z"
                }
                """
        )
    )
)
```

**Дублирование ретвита**:
```java
@ApiResponse(
    responseCode = "409",
    description = "Uniqueness violation - duplicate retweet",
    content = @Content(
        mediaType = "application/problem+json",
        examples = @ExampleObject(
            name = "Duplicate Retweet Error",
            summary = "User already retweeted this tweet",
            value = """
                {
                  "type": "https://example.com/errors/uniqueness-validation",
                  "title": "Uniqueness Validation Error",
                  "status": 409,
                  "detail": "A retweet already exists for tweet 223e4567-e89b-12d3-a456-426614174001 and user 123e4567-e89b-12d3-a456-426614174000",
                  "fieldName": "retweet",
                  "fieldValue": "tweet 223e4567-e89b-12d3-a456-426614174001 and user 123e4567-e89b-12d3-a456-426614174000",
                  "timestamp": "2025-01-27T15:30:00Z"
                }
                """
        )
    )
)
```

### 1.4 Параметры метода

**tweetId (Path Parameter)**:
```java
@Parameter(
    description = "Unique identifier of the tweet to retweet",
    required = true,
    example = "223e4567-e89b-12d3-a456-426614174001"
)
UUID tweetId
```

**retweetRequest (Request Body)**:
```java
@Parameter(
    description = "Retweet request containing userId and optional comment",
    required = true
)
RetweetRequestDto retweetRequest
```

## 2. DTO структура

### 2.1 RetweetRequestDto

**Расположение**: `services/tweet-api/src/main/java/com/twitter/dto/request/RetweetRequestDto.java`

**Структура**:
```java
@Schema(
    name = "RetweetRequest",
    description = "Data structure for retweeting a tweet in the system. " +
        "The comment field is optional and can be null, but if provided, " +
        "it must be between 1 and 280 characters and cannot be an empty string.",
    example = """
        {
          "userId": "123e4567-e89b-12d3-a456-426614174000",
          "comment": "Great tweet!"
        }
        """
)
@Builder
public record RetweetRequestDto(
    @Schema(
        description = "The ID of the user who retweets the tweet",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "User ID cannot be null")
    UUID userId,
    
    @Schema(
        description = "Optional comment for the retweet (1-280 characters). " +
            "Can be null, but if provided, must not be empty string.",
        example = "Great tweet!",
        maxLength = 280,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 280, message = "Comment must not exceed 280 characters")
    String comment
) {
}
```

**Валидация**:
- `userId`: `@NotNull` - обязательное поле
- `comment`: `@Size(max = 280)` - опциональное поле, максимум 280 символов
- Дополнительная валидация в `RetweetValidator`: проверка, что `comment` не пустая строка (если не null)

**Примеры**:
- С комментарием: `{"userId": "...", "comment": "Great tweet!"}`
- Без комментария: `{"userId": "...", "comment": null}`

### 2.2 RetweetResponseDto

**Расположение**: `services/tweet-api/src/main/java/com/twitter/dto/response/RetweetResponseDto.java`

**Структура**:
```java
@Schema(
    name = "RetweetResponse",
    description = "Retweet information returned by the API",
    example = """
        {
          "id": "987e6543-e21b-43d2-b654-321987654321",
          "tweetId": "223e4567-e89b-12d3-a456-426614174001",
          "userId": "123e4567-e89b-12d3-a456-426614174000",
          "comment": "Great tweet!",
          "createdAt": "2025-01-27T15:30:00Z"
        }
        """
)
@Builder
public record RetweetResponseDto(
    @Schema(
        description = "Unique identifier for the retweet",
        example = "987e6543-e21b-43d2-b654-321987654321",
        format = "uuid"
    )
    UUID id,
    
    @Schema(
        description = "ID of the tweet that was retweeted",
        example = "223e4567-e89b-12d3-a456-426614174001",
        format = "uuid"
    )
    UUID tweetId,
    
    @Schema(
        description = "ID of the user who retweeted the tweet",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid"
    )
    UUID userId,
    
    @Schema(
        description = "Optional comment for the retweet (can be null)",
        example = "Great tweet!",
        maxLength = 280,
        nullable = true
    )
    String comment,
    
    @Schema(
        description = "Timestamp when the retweet was created",
        example = "2025-01-27T15:30:00Z",
        format = "date-time"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime createdAt
) {
}
```

## 3. Entity структура

### 3.1 Retweet Entity

**Расположение**: `services/tweet-api/src/main/java/com/twitter/entity/Retweet.java`

**Структура полей**:
```java
@Entity
@Table(
    name = "tweet_retweets",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_tweet_retweets_tweet_user",
            columnNames = {"tweet_id", "user_id"}
        )
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Retweet {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull(message = "Tweet ID cannot be null")
    @Column(name = "tweet_id", columnDefinition = "UUID", nullable = false)
    private UUID tweetId;
    
    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", columnDefinition = "UUID", nullable = false)
    private UUID userId;
    
    @Size(max = 280, message = "Comment must not exceed 280 characters")
    @Column(name = "comment", length = 280, nullable = true)
    private String comment;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Бизнес-методы
    public boolean isByUser(UUID userId) {
        return this.userId != null && this.userId.equals(userId);
    }
    
    public boolean isForTweet(UUID tweetId) {
        return this.tweetId != null && this.tweetId.equals(tweetId);
    }
    
    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }
}
```

**Ограничения**:
- Уникальное ограничение на паре `(tweet_id, user_id)`
- Foreign key на `tweets(id)` для `tweet_id`
- Foreign key на `users(id)` для `user_id` (через users-api)
- Индексы на `tweet_id`, `user_id`, и `(tweet_id, user_id)`

## 4. Компоненты для реализации

### 4.1 Repository

**Расположение**: `services/tweet-api/src/main/java/com/twitter/repository/RetweetRepository.java`

**Методы**:
```java
@Repository
public interface RetweetRepository extends JpaRepository<Retweet, UUID> {
    
    Optional<Retweet> findByTweetIdAndUserId(UUID tweetId, UUID userId);
    
    boolean existsByTweetIdAndUserId(UUID tweetId, UUID userId);
}
```

**Примечание**: Derived Query Methods без JavaDoc (согласно стандартам).

### 4.2 Mapper

**Расположение**: `services/tweet-api/src/main/java/com/twitter/mapper/RetweetMapper.java`

**Методы**:
```java
@Mapper
public interface RetweetMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tweetId", source = "tweetId")
    @Mapping(target = "userId", source = "requestDto.userId")
    @Mapping(target = "comment", source = "requestDto.comment")
    Retweet toRetweet(RetweetRequestDto requestDto, UUID tweetId);
    
    RetweetResponseDto toRetweetResponseDto(Retweet retweet);
}
```

### 4.3 Validator

**Расположение**: 
- Интерфейс: `services/tweet-api/src/main/java/com/twitter/validation/RetweetValidator.java`
- Реализация: `services/tweet-api/src/main/java/com/twitter/validation/RetweetValidatorImpl.java`

**Метод интерфейса**:
```java
void validateForRetweet(UUID tweetId, RetweetRequestDto requestDto);
```

**Проверки в реализации**:
1. Проверка, что `tweetId` не null
2. Проверка существования твита (не удален)
3. Проверка, что `requestDto` не null
4. Проверка, что `userId` не null
5. Проверка существования пользователя через `UserGateway`
6. Проверка запрета self-retweet
7. Проверка уникальности ретвита
8. Валидация комментария (если не null, то не пустая строка и не более 280 символов)

### 4.4 Service

**Расположение**:
- Интерфейс: `services/tweet-api/src/main/java/com/twitter/service/RetweetService.java`
- Реализация: `services/tweet-api/src/main/java/com/twitter/service/RetweetServiceImpl.java`

**Метод интерфейса**:
```java
RetweetResponseDto retweetTweet(UUID tweetId, RetweetRequestDto requestDto);
```

**Логика реализации**:
1. Вызов валидации через `RetweetValidator.validateForRetweet()`
2. Маппинг DTO в Entity через `RetweetMapper.toRetweet()`
3. Сохранение в БД через `RetweetRepository.saveAndFlush()`
4. Обновление счетчика `retweetsCount` в `Tweet` entity
5. Маппинг Entity в Response DTO через `RetweetMapper.toRetweetResponseDto()`
6. Возврат Response DTO

**Транзакционность**: `@Transactional` на методе сервиса

### 4.5 Controller

**Расположение**: `services/tweet-api/src/main/java/com/twitter/controller/RetweetController.java`

**Структура**:
```java
@Slf4j
@RestController
@RequestMapping("/api/v1/tweets")
@RequiredArgsConstructor
public class RetweetController implements RetweetApi {
    
    private final RetweetService retweetService;
    
    @LoggableRequest
    @PostMapping("/{tweetId}/retweet")
    @Override
    public ResponseEntity<RetweetResponseDto> retweetTweet(
        @PathVariable("tweetId") UUID tweetId,
        @RequestBody @Valid RetweetRequestDto retweetRequest) {
        RetweetResponseDto createdRetweet = retweetService.retweetTweet(tweetId, retweetRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRetweet);
    }
}
```

## 5. HTTP статус-коды

| Статус | Описание | Когда возвращается |
|--------|----------|-------------------|
| 201 Created | Ретвит успешно создан | Успешное создание ретвита |
| 400 Bad Request | Ошибка валидации | Невалидный формат данных, пустая строка в comment, comment > 280 символов |
| 404 Not Found | Ресурс не найден | Твит не существует или удален, пользователь не существует |
| 409 Conflict | Нарушение бизнес-правил | Self-retweet, дублирование ретвита |

## 6. Исключения

### 6.1 BusinessRuleValidationException

**Используется для**:
- `TWEET_ID_NULL` - tweetId равен null
- `TWEET_NOT_FOUND` - твит не существует или удален
- `RETWEET_REQUEST_NULL` - requestDto равен null
- `USER_ID_NULL` - userId равен null
- `USER_NOT_EXISTS` - пользователь не существует
- `SELF_RETWEET_NOT_ALLOWED` - попытка ретвита собственного твита
- `COMMENT_VALIDATION` - комментарий пустая строка (если не null)

### 6.2 UniquenessValidationException

**Используется для**:
- `retweet` - дублирование ретвита (пользователь уже ретвитнул этот твит)

### 6.3 FormatValidationException

**Используется для**:
- `COMMENT_VALIDATION` - комментарий превышает 280 символов

## 7. Интеграция с существующими компонентами

### 7.1 TweetRepository

**Использование**:
- `findByIdAndIsDeletedFalse(UUID tweetId)` - проверка существования твита
- `saveAndFlush(Tweet tweet)` - обновление счетчика `retweetsCount`

### 7.2 Tweet Entity

**Требуется добавить**:
- Поле `retweetsCount` (если отсутствует)
- Метод `incrementRetweetsCount()` (аналогично `incrementLikesCount()`)

### 7.3 UserGateway

**Использование**:
- `existsUser(UUID userId)` - проверка существования пользователя

### 7.4 GlobalExceptionHandler

**Обработка**:
- Автоматическая обработка всех исключений через `GlobalExceptionHandler`
- Преобразование в RFC 7807 Problem Details формат

## 8. Критерии приемки

1. ✅ OpenAPI интерфейс `RetweetApi` спроектирован с полными аннотациями
2. ✅ DTO структура определена с `@Schema` аннотациями
3. ✅ Entity структура определена с бизнес-методами
4. ✅ Все компоненты определены (Repository, Mapper, Validator, Service, Controller)
5. ✅ HTTP статус-коды определены для всех сценариев
6. ✅ Исключения определены с правильными типами
7. ✅ Интеграция с существующими компонентами определена
8. ✅ Примеры для всех сценариев (успешных и ошибочных) определены

## 9. Следующие шаги

1. Создание SQL скрипта (#3)
2. Реализация Entity (#4)
3. Реализация Repository (#5)
4. Реализация DTO (#6)
5. Реализация Mapper (#7)
6. Реализация Validator (#8)
7. Реализация Service (#9)
8. Реализация Controller (#10-14)
9. Тестирование (#15-16)
10. Документация (#17-19)
11. Проверка стандартов (#20)

