# Проектирование API и контрактов: Эндпоинт "Лайкнуть твит"

## Meta
- **Шаг**: #2
- **Дата**: 2025-01-27
- **Статус**: Завершено
- **Связанные задачи**: TODO.md #2
- **Предыдущий шаг**: analysis-requirements.md (#1)

## 1. OpenAPI схема для эндпоинта

### 1.1 Метод и путь
- **HTTP метод**: POST
- **Путь**: `/api/v1/tweets/{tweetId}/like`
- **Полный путь**: `POST /api/v1/tweets/{tweetId}/like`
- **Content-Type**: `application/json`
- **Response Content-Type**: `application/json` (201), `application/problem+json` (ошибки)

### 1.2 OpenAPI аннотации для TweetApi интерфейса

```java
/**
 * Likes a tweet by creating a like record.
 * <p>
 * This endpoint allows a user to like a tweet. It performs validation including
 * checking if the tweet exists, if the user exists, preventing self-likes, and
 * preventing duplicate likes. The operation is transactional and atomic.
 * Upon successful creation, the tweet's likes count is incremented.
 *
 * @param tweetId           the unique identifier of the tweet to like
 * @param likeTweetRequest  DTO containing userId of the user who likes the tweet
 * @return ResponseEntity containing the created like data with HTTP 201 status
 * @throws BusinessRuleValidationException if tweet doesn't exist, user doesn't exist, self-like attempt, or duplicate like
 */
@Operation(
    summary = "Like a tweet",
    description = "Likes a tweet by creating a like record. " +
        "It performs validation including checking if the tweet exists and is not deleted, " +
        "if the user exists via users-api integration, preventing self-likes (user cannot like their own tweet), " +
        "and preventing duplicate likes (one user can like a tweet only once). " +
        "The operation is transactional and atomic. Upon successful creation, the tweet's likes count is incremented. " +
        "Returns 201 Created with the created like data."
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "201",
        description = "Tweet liked successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = LikeResponseDto.class),
            examples = @ExampleObject(
                name = "Created Like",
                summary = "Example created like response",
                value = """
                    {
                      "id": "987e6543-e21b-43d2-b654-321987654321",
                      "tweetId": "223e4567-e89b-12d3-a456-426614174001",
                      "userId": "123e4567-e89b-12d3-a456-426614174000",
                      "createdAt": "2025-01-27T15:30:00Z"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(
            mediaType = "application/problem+json",
            examples = @ExampleObject(
                name = "Invalid UUID",
                summary = "Invalid UUID format",
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
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Business rule violation - user not found",
        content = @Content(
            mediaType = "application/problem+json",
            examples = @ExampleObject(
                name = "User Not Found",
                summary = "User does not exist",
                value = """
                    {
                      "type": "https://example.com/errors/business-rule-validation",
                      "title": "Business Rule Validation Error",
                      "status": 400,
                      "detail": "Business rule 'USER_NOT_EXISTS' violated for context: 123e4567-e89b-12d3-a456-426614174000",
                      "ruleName": "USER_NOT_EXISTS",
                      "timestamp": "2025-01-27T15:30:00Z"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "Tweet not found",
        content = @Content(
            mediaType = "application/problem+json",
            examples = @ExampleObject(
                name = "Tweet Not Found",
                summary = "Tweet does not exist or is deleted",
                value = """
                    {
                      "type": "https://example.com/errors/business-rule-validation",
                      "title": "Business Rule Validation Error",
                      "status": 404,
                      "detail": "Business rule 'TWEET_NOT_FOUND' violated for context: 223e4567-e89b-12d3-a456-426614174001",
                      "ruleName": "TWEET_NOT_FOUND",
                      "timestamp": "2025-01-27T15:30:00Z"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "409",
        description = "Conflict - like already exists",
        content = @Content(
            mediaType = "application/problem+json",
            examples = @ExampleObject(
                name = "Like Already Exists",
                summary = "Duplicate like attempt",
                value = """
                    {
                      "type": "https://example.com/errors/uniqueness-validation",
                      "title": "Uniqueness Validation Error",
                      "status": 409,
                      "detail": "Like already exists for tweet 223e4567-e89b-12d3-a456-426614174001 and user 123e4567-e89b-12d3-a456-426614174000",
                      "fieldName": "like",
                      "timestamp": "2025-01-27T15:30:00Z"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "409",
        description = "Conflict - self-like not allowed",
        content = @Content(
            mediaType = "application/problem+json",
            examples = @ExampleObject(
                name = "Self-Like Not Allowed",
                summary = "User cannot like their own tweet",
                value = """
                    {
                      "type": "https://example.com/errors/business-rule-validation",
                      "title": "Business Rule Validation Error",
                      "status": 409,
                      "detail": "Business rule 'SELF_LIKE_NOT_ALLOWED' violated for context: User cannot like their own tweet",
                      "ruleName": "SELF_LIKE_NOT_ALLOWED",
                      "timestamp": "2025-01-27T15:30:00Z"
                    }
                    """
            )
        )
    )
})
ResponseEntity<LikeResponseDto> likeTweet(
    @Parameter(
        description = "Unique identifier of the tweet to like",
        required = true,
        example = "223e4567-e89b-12d3-a456-426614174001"
    )
    UUID tweetId,
    
    @Parameter(description = "Like data containing userId", required = true)
    LikeTweetRequestDto likeTweetRequest
);
```

## 2. Структура DTO

### 2.1 LikeTweetRequestDto (Request DTO)

**Расположение**: `services/tweet-api/src/main/java/com/twitter/dto/request/LikeTweetRequestDto.java`

**Структура**:
```java
package com.twitter.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/**
 * Data Transfer Object for liking a tweet.
 * <p>
 * This record represents the data structure used for liking a tweet in the system.
 * It contains the userId of the user who wants to like the tweet.
 *
 * @param userId the ID of the user who likes the tweet
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "LikeTweetRequest",
    description = "Data structure for liking a tweet in the system",
    example = """
        {
          "userId": "123e4567-e89b-12d3-a456-426614174000"
        }
        """
)
@Builder
public record LikeTweetRequestDto(
    @Schema(
        description = "The ID of the user who likes the tweet",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "User ID cannot be null")
    UUID userId
) {
}
```

**Особенности**:
- Record (Java 24 feature)
- Bean Validation: @NotNull для userId
- @Schema аннотации для Swagger документации
- @Builder для удобного создания (Lombok)
- Размещение: `dto/request/` (service-specific, не в common-lib)

### 2.2 LikeResponseDto (Response DTO)

**Расположение**: `services/tweet-api/src/main/java/com/twitter/dto/response/LikeResponseDto.java`

**Структура**:
```java
package com.twitter.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Like data.
 * <p>
 * This record contains all relevant like data including identifier, tweet reference,
 * user reference, and timestamp for creation. It is used to return like data from
 * the API endpoints.
 *
 * @param id        unique identifier for the like
 * @param tweetId   ID of the tweet that was liked
 * @param userId    ID of the user who liked the tweet
 * @param createdAt timestamp when the like was created
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "LikeResponse",
    description = "Like information returned by the API",
    example = """
        {
          "id": "987e6543-e21b-43d2-b654-321987654321",
          "tweetId": "223e4567-e89b-12d3-a456-426614174001",
          "userId": "123e4567-e89b-12d3-a456-426614174000",
          "createdAt": "2025-01-27T15:30:00Z"
        }
        """
)
@Builder
public record LikeResponseDto(
    @Schema(
        description = "Unique identifier for the like",
        example = "987e6543-e21b-43d2-b654-321987654321",
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "ID of the tweet that was liked",
        example = "223e4567-e89b-12d3-a456-426614174001",
        format = "uuid"
    )
    UUID tweetId,

    @Schema(
        description = "ID of the user who liked the tweet",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid"
    )
    UUID userId,

    @Schema(
        description = "Timestamp when the like was created",
        example = "2025-01-27T15:30:00Z",
        format = "date-time"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime createdAt
) {
}
```

**Особенности**:
- Record (Java 24 feature)
- @Schema аннотации для Swagger документации
- @JsonFormat для форматирования даты
- @Builder для удобного создания (Lombok)
- Размещение: `dto/response/` (service-specific, не в common-lib)

## 3. Структура Entity

### 3.1 Like Entity

**Расположение**: `services/tweet-api/src/main/java/com/twitter/entity/Like.java`

**Структура**:
```java
package com.twitter.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing a Like in the database.
 * <p>
 * Maps to the 'tweet_likes' table with all necessary fields and constraints.
 * This entity represents a like created by a user for a tweet in the Twitter system.
 * Enforces uniqueness constraint on the pair (tweetId, userId) to prevent duplicate likes.
 *
 * @author geron
 * @version 1.0
 */
@Entity
@Table(
    name = "tweet_likes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tweet_likes_tweet_user", columnNames = {"tweet_id", "user_id"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Like {

    /**
     * Unique identifier for the like.
     * Generated automatically using UUID.
     */
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID of the tweet that was liked.
     */
    @NotNull(message = "Tweet ID cannot be null")
    @Column(name = "tweet_id", columnDefinition = "UUID", nullable = false)
    private UUID tweetId;

    /**
     * ID of the user who created this like.
     */
    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", columnDefinition = "UUID", nullable = false)
    private UUID userId;

    /**
     * Timestamp when the like was created.
     * Automatically set by Hibernate on entity creation.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Checks if the like is by a specific user.
     * <p>
     * This method returns true if the like was created by the specified user.
     *
     * @param userId the user ID to check
     * @return true if the like is by the specified user, false otherwise
     */
    public boolean isByUser(UUID userId) {
        return this.userId != null && this.userId.equals(userId);
    }

    /**
     * Checks if the like is for a specific tweet.
     * <p>
     * This method returns true if the like is for the specified tweet.
     *
     * @param tweetId the tweet ID to check
     * @return true if the like is for the specified tweet, false otherwise
     */
    public boolean isForTweet(UUID tweetId) {
        return this.tweetId != null && this.tweetId.equals(tweetId);
    }
}
```

**Особенности**:
- UUID id с автогенерацией
- Уникальное ограничение на паре (tweetId, userId) через @UniqueConstraint
- @CreationTimestamp для createdAt
- Бизнес-методы: isByUser(), isForTweet()
- Lombok аннотации: @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
- Индексы будут созданы через SQL миграции (idx_tweet_likes_tweet_id, idx_tweet_likes_user_id)

## 4. Определение общих и специфичных компонентов

### 4.1 Общие компоненты (переиспользование)

1. **UserGateway**
   - Используется для проверки существования пользователя
   - Уже реализован в `services/tweet-api/src/main/java/com/twitter/gateway/UserGateway.java`
   - Метод: `existsUser(UUID userId)`

2. **TweetRepository**
   - Используется для проверки существования твита
   - Уже реализован в `services/tweet-api/src/main/java/com/twitter/repository/TweetRepository.java`
   - Метод: `findByIdAndIsDeletedFalse(UUID tweetId)`

3. **TweetMapper**
   - Может быть расширен методами для лайков или создан отдельный LikeMapper
   - Решение: добавить методы в TweetMapper для консистентности

4. **TweetValidator**
   - Будет расширен методом `validateForLike`
   - Уже реализован в `services/tweet-api/src/main/java/com/twitter/validation/TweetValidator.java`

5. **GlobalExceptionHandler**
   - Обработка ошибок через существующий handler из common-lib
   - Использование ValidationException иерархии

6. **Tweet Entity**
   - Будет обновлена добавлением поля `likesCount` и метода `incrementLikesCount()`
   - Уже существует в `services/tweet-api/src/main/java/com/twitter/entity/Tweet.java`

### 4.2 Специфичные компоненты (новые)

1. **Entity: Like**
   - Новая сущность для хранения лайков
   - Расположение: `services/tweet-api/src/main/java/com/twitter/entity/Like.java`

2. **Repository: LikeRepository**
   - Новый репозиторий для работы с лайками
   - Расположение: `services/tweet-api/src/main/java/com/twitter/repository/LikeRepository.java`
   - Методы: `findByTweetIdAndUserId`, `existsByTweetIdAndUserId`

3. **DTO: LikeTweetRequestDto**
   - Новый Request DTO для лайка
   - Расположение: `services/tweet-api/src/main/java/com/twitter/dto/request/LikeTweetRequestDto.java`

4. **DTO: LikeResponseDto**
   - Новый Response DTO для лайка
   - Расположение: `services/tweet-api/src/main/java/com/twitter/dto/response/LikeResponseDto.java`

5. **Mapper: методы для лайков**
   - Новые методы в TweetMapper: `toLike`, `toLikeResponseDto`
   - Расположение: `services/tweet-api/src/main/java/com/twitter/mapper/TweetMapper.java`

6. **Validator: метод validateForLike**
   - Новый метод в TweetValidator
   - Расположение: `services/tweet-api/src/main/java/com/twitter/validation/TweetValidator.java` и `TweetValidatorImpl.java`

7. **Service: метод likeTweet**
   - Новый метод в TweetService
   - Расположение: `services/tweet-api/src/main/java/com/twitter/service/TweetService.java` и `TweetServiceImpl.java`

8. **Controller: метод likeTweet**
   - Новый метод в TweetApi и TweetController
   - Расположение: `services/tweet-api/src/main/java/com/twitter/controller/TweetApi.java` и `TweetController.java`

## 5. Структура базы данных

### 5.1 Таблица tweet_likes

**SQL структура** (из архитектуры):
```sql
CREATE TABLE tweet_api.tweet_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tweet_id UUID NOT NULL REFERENCES tweet_api.tweets(id),
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tweet_id, user_id)
);

-- Индексы для производительности
CREATE INDEX idx_tweet_likes_tweet_id ON tweet_api.tweet_likes(tweet_id);
CREATE INDEX idx_tweet_likes_user_id ON tweet_api.tweet_likes(user_id);
```

**Особенности**:
- Уникальное ограничение на паре (tweet_id, user_id) для предотвращения дублирования
- Foreign key на tweets(id) для ссылочной целостности
- Индексы для оптимизации запросов по tweet_id и user_id
- Триггер для автоматического обновления счетчика likesCount в таблице tweets

### 5.2 Обновление таблицы tweets

**Добавление поля likesCount**:
```sql
ALTER TABLE tweet_api.tweets 
ADD COLUMN IF NOT EXISTS likes_count INTEGER DEFAULT 0;

-- Индекс для сортировки по популярности
CREATE INDEX IF NOT EXISTS idx_tweets_likes_count 
ON tweet_api.tweets(likes_count DESC);
```

## 6. Маппинг Entity ↔ DTO

### 6.1 Методы маппинга в TweetMapper

**Добавить методы**:
```java
/**
 * Converts LikeTweetRequestDto and tweetId to Like entity.
 *
 * @param requestDto DTO containing userId for the like
 * @param tweetId    the unique identifier of the tweet being liked
 * @return Like entity without service-managed fields (id, createdAt)
 */
@Mapping(target = "id", ignore = true)
@Mapping(target = "createdAt", ignore = true)
Like toLike(LikeTweetRequestDto requestDto, UUID tweetId);

/**
 * Converts Like entity to LikeResponseDto.
 *
 * @param like Like entity from database
 * @return DTO containing like data for client response
 */
LikeResponseDto toLikeResponseDto(Like like);
```

**Особенности**:
- Игнорирование служебных полей (id, createdAt) при создании
- Автоматический маппинг полей с одинаковыми именами
- tweetId передается отдельным параметром (не из DTO)

## 7. Валидация

### 7.1 Метод validateForLike в TweetValidator

**Интерфейс**:
```java
/**
 * Performs complete validation for tweet like operation.
 * <p>
 * This method validates like data including:
 * <ul>
 *   <li>Existence of the tweet (tweetId must not be null and tweet must exist and not be deleted)</li>
 *   <li>Existence of the user (userId must not be null and user must exist)</li>
 *   <li>Self-like check (user cannot like their own tweet)</li>
 *   <li>Duplicate like check (user cannot like the same tweet twice)</li>
 * </ul>
 *
 * @param tweetId    the unique identifier of the tweet to like
 * @param requestDto DTO containing userId for the like
 * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, self-like attempt, or duplicate like
 */
void validateForLike(UUID tweetId, LikeTweetRequestDto requestDto);
```

**Реализация валидации**:
1. Проверка tweetId: не null
2. Проверка существования твита: `tweetRepository.findByIdAndIsDeletedFalse(tweetId)`
3. Проверка userId: не null
4. Проверка существования пользователя: `userGateway.existsUser(userId)`
5. Проверка самолайка: `tweet.getUserId().equals(requestDto.userId())`
6. Проверка дублирования: `likeRepository.existsByTweetIdAndUserId(tweetId, requestDto.userId())`

## 8. Service метод

### 8.1 Метод likeTweet в TweetService

**Интерфейс**:
```java
/**
 * Likes a tweet by creating a like record.
 * <p>
 * This method performs the following operations:
 * 1. Validates the like request (tweet existence, user existence, self-like check, duplicate check)
 * 2. Creates a Like entity from the request DTO
 * 3. Saves the like to the database
 * 4. Increments the tweet's likes count
 * 5. Converts the saved entity to response DTO
 * 6. Returns the response DTO
 * <p>
 * The method is transactional with SERIALIZABLE isolation level to prevent race conditions
 * and duplicate likes. Only one like per user per tweet is allowed.
 *
 * @param tweetId    the unique identifier of the tweet to like
 * @param requestDto the like request containing userId
 * @return LikeResponseDto containing the created like data
 * @throws BusinessRuleValidationException if tweet doesn't exist, user doesn't exist, self-like attempt, or duplicate like
 */
@Transactional(isolation = Isolation.SERIALIZABLE)
LikeResponseDto likeTweet(UUID tweetId, LikeTweetRequestDto requestDto);
```

**Особенности**:
- @Transactional с уровнем изоляции SERIALIZABLE
- Вызов валидации через TweetValidator
- Создание Like entity через Mapper
- Сохранение в БД через LikeRepository
- Обновление счетчика через Tweet.incrementLikesCount()
- Маппинг в DTO через Mapper

## 9. Controller метод

### 9.1 Метод likeTweet в TweetApi и TweetController

**TweetApi интерфейс**:
```java
ResponseEntity<LikeResponseDto> likeTweet(
    @Parameter(...) UUID tweetId,
    @Parameter(...) LikeTweetRequestDto likeTweetRequest
);
```

**TweetController реализация**:
```java
@LoggableRequest
@PostMapping("/{tweetId}/like")
@Override
public ResponseEntity<LikeResponseDto> likeTweet(
    @PathVariable("tweetId") UUID tweetId,
    @RequestBody @Valid LikeTweetRequestDto likeTweetRequest
) {
    LikeResponseDto createdLike = tweetService.likeTweet(tweetId, likeTweetRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdLike);
}
```

**Особенности**:
- @LoggableRequest для логирования запросов
- @Valid для валидации request body
- HTTP статус 201 Created
- Path variable для tweetId
- Request body для userId

## 10. Итоговая структура компонентов

### 10.1 Новые файлы
1. `services/tweet-api/src/main/java/com/twitter/entity/Like.java`
2. `services/tweet-api/src/main/java/com/twitter/repository/LikeRepository.java`
3. `services/tweet-api/src/main/java/com/twitter/dto/request/LikeTweetRequestDto.java`
4. `services/tweet-api/src/main/java/com/twitter/dto/response/LikeResponseDto.java`

### 10.2 Обновляемые файлы
1. `services/tweet-api/src/main/java/com/twitter/entity/Tweet.java` - добавить likesCount
2. `services/tweet-api/src/main/java/com/twitter/mapper/TweetMapper.java` - добавить методы для лайков
3. `services/tweet-api/src/main/java/com/twitter/validation/TweetValidator.java` - добавить validateForLike
4. `services/tweet-api/src/main/java/com/twitter/validation/TweetValidatorImpl.java` - реализовать validateForLike
5. `services/tweet-api/src/main/java/com/twitter/service/TweetService.java` - добавить likeTweet
6. `services/tweet-api/src/main/java/com/twitter/service/TweetServiceImpl.java` - реализовать likeTweet
7. `services/tweet-api/src/main/java/com/twitter/controller/TweetApi.java` - добавить likeTweet с OpenAPI аннотациями
8. `services/tweet-api/src/main/java/com/twitter/controller/TweetController.java` - реализовать likeTweet

### 10.3 SQL миграции
1. Создание таблицы `tweet_likes` (если еще не создана)
2. Добавление поля `likes_count` в таблицу `tweets` (если еще не добавлено)
3. Создание индексов для производительности
4. Создание триггера для обновления счетчика (если еще не создан)

## 11. Следующие шаги

1. Реализация Entity Like (#3)
2. Реализация LikeRepository (#4)
3. Реализация DTO (#5)
4. Реализация Mapper методов (#6)

