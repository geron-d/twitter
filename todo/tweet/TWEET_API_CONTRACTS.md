# Проектирование API контрактов Tweet API Service

## Meta
- project: twitter-tweet-api
- design_date: 2025-01-27
- designer: AI Assistant
- version: 1.0
- status: in_progress
- port: 8082
- base_url: http://localhost:8082/api/v1

## Executive Summary

Данный документ содержит детальное проектирование API контрактов для сервиса Tweet API. Контракты спроектированы с учетом высоких требований к производительности, интеграции с users-api и будущего масштабирования системы.

## 1. Архитектурные принципы API

### 1.1 REST API Design Principles

#### Основные принципы:
- **RESTful архитектура** с четким разделением ресурсов
- **HTTP методы** соответствуют операциям (GET, POST, PUT, DELETE)
- **Статус коды** отражают результат операции
- **Версионирование** через URL path (/api/v1/)
- **Консистентность** в именовании и структуре

#### Соглашения по именованию:
- **Ресурсы**: множественное число (tweets, likes, retweets)
- **Идентификаторы**: UUID в path параметрах
- **Поля**: camelCase в JSON, snake_case в БД
- **Временные метки**: ISO 8601 формат

### 1.2 Структура ответов

#### Стандартная структура успешного ответа:
```json
{
  "data": { ... },
  "meta": {
    "timestamp": "2025-01-27T10:30:00Z",
    "requestId": "req-12345678-1234-1234-1234-123456789abc"
  }
}
```

#### Стандартная структура ошибки:
```json
{
  "error": {
    "code": "TWEET_NOT_FOUND",
    "message": "Tweet with id '12345678-1234-1234-1234-123456789abc' not found",
    "details": {
      "field": "tweetId",
      "value": "12345678-1234-1234-1234-123456789abc"
    }
  },
  "meta": {
    "timestamp": "2025-01-27T10:30:00Z",
    "requestId": "req-12345678-1234-1234-1234-123456789abc"
  }
}
```

## 2. REST Endpoints Design

### 2.1 Основные операции с твитами

#### POST /api/v1/tweets - Создание твита
```yaml
summary: Create a new tweet
description: Creates a new tweet for the authenticated user
operationId: createTweet
tags:
  - Tweets
requestBody:
  required: true
  content:
    application/json:
      schema:
        $ref: '#/components/schemas/CreateTweetRequest'
responses:
  '201':
    description: Tweet created successfully
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/TweetResponse'
  '400':
    description: Bad request - validation errors
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/ValidationErrorResponse'
  '401':
    description: Unauthorized
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/ErrorResponse'
  '404':
    description: User not found
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/ErrorResponse'
```

#### GET /api/v1/tweets/{tweetId} - Получение твита
```yaml
summary: Get tweet by ID
description: Retrieves a specific tweet by its ID
operationId: getTweetById
tags:
  - Tweets
parameters:
  - name: tweetId
    in: path
    required: true
    schema:
      type: string
      format: uuid
    description: Unique identifier of the tweet
responses:
  '200':
    description: Tweet retrieved successfully
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/TweetResponse'
  '404':
    description: Tweet not found
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/ErrorResponse'
```

#### PUT /api/v1/tweets/{tweetId} - Обновление твита
```yaml
summary: Update tweet
description: Updates an existing tweet (only by the author)
operationId: updateTweet
tags:
  - Tweets
parameters:
  - name: tweetId
    in: path
    required: true
    schema:
      type: string
      format: uuid
requestBody:
  required: true
  content:
    application/json:
      schema:
        $ref: '#/components/schemas/UpdateTweetRequest'
responses:
  '200':
    description: Tweet updated successfully
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/TweetResponse'
  '400':
    description: Bad request - validation errors
  '403':
    description: Forbidden - not the author
  '404':
    description: Tweet not found
```

#### DELETE /api/v1/tweets/{tweetId} - Удаление твита
```yaml
summary: Delete tweet
description: Soft deletes a tweet (only by the author)
operationId: deleteTweet
tags:
  - Tweets
parameters:
  - name: tweetId
    in: path
    required: true
    schema:
      type: string
      format: uuid
responses:
  '204':
    description: Tweet deleted successfully
  '403':
    description: Forbidden - not the author
  '404':
    description: Tweet not found
```

### 2.2 Операции с пользователями

#### GET /api/v1/tweets/user/{userId} - Твиты пользователя
```yaml
summary: Get user tweets
description: Retrieves tweets by a specific user with pagination
operationId: getUserTweets
tags:
  - Tweets
parameters:
  - name: userId
    in: path
    required: true
    schema:
      type: string
      format: uuid
  - name: page
    in: query
    schema:
      type: integer
      minimum: 0
      default: 0
  - name: size
    in: query
    schema:
      type: integer
      minimum: 1
      maximum: 100
      default: 20
responses:
  '200':
    description: User tweets retrieved successfully
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/TweetListResponse'
  '404':
    description: User not found
```

#### GET /api/v1/tweets/timeline/{userId} - Лента новостей
```yaml
summary: Get user timeline
description: Retrieves timeline for a specific user
operationId: getUserTimeline
tags:
  - Tweets
parameters:
  - name: userId
    in: path
    required: true
    schema:
      type: string
      format: uuid
  - name: page
    in: query
    schema:
      type: integer
      minimum: 0
      default: 0
  - name: size
    in: query
    schema:
      type: integer
      minimum: 1
      maximum: 100
      default: 20
responses:
  '200':
    description: Timeline retrieved successfully
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/TweetListResponse'
  '404':
    description: User not found
```

### 2.3 Социальные функции

#### POST /api/v1/tweets/{tweetId}/like - Лайк твита
```yaml
summary: Like a tweet
description: Adds a like to a specific tweet
operationId: likeTweet
tags:
  - Social Actions
parameters:
  - name: tweetId
    in: path
    required: true
    schema:
      type: string
      format: uuid
requestBody:
  required: true
  content:
    application/json:
      schema:
        $ref: '#/components/schemas/LikeTweetRequest'
responses:
  '201':
    description: Tweet liked successfully
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/LikeResponse'
  '400':
    description: Bad request - already liked or self-like
  '404':
    description: Tweet not found
```

#### DELETE /api/v1/tweets/{tweetId}/like - Убрать лайк
```yaml
summary: Unlike a tweet
description: Removes a like from a specific tweet
operationId: unlikeTweet
tags:
  - Social Actions
parameters:
  - name: tweetId
    in: path
    required: true
    schema:
      type: string
      format: uuid
requestBody:
  required: true
  content:
    application/json:
      schema:
        $ref: '#/components/schemas/UnlikeTweetRequest'
responses:
  '204':
    description: Like removed successfully
  '404':
    description: Tweet not found or like not found
```

#### POST /api/v1/tweets/{tweetId}/retweet - Ретвит
```yaml
summary: Retweet a tweet
description: Creates a retweet of a specific tweet
operationId: retweetTweet
tags:
  - Social Actions
parameters:
  - name: tweetId
    in: path
    required: true
    schema:
      type: string
      format: uuid
requestBody:
  required: true
  content:
    application/json:
      schema:
        $ref: '#/components/schemas/RetweetRequest'
responses:
  '201':
    description: Tweet retweeted successfully
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/RetweetResponse'
  '400':
    description: Bad request - already retweeted or self-retweet
  '404':
    description: Tweet not found
```

#### DELETE /api/v1/tweets/{tweetId}/retweet - Убрать ретвит
```yaml
summary: Remove retweet
description: Removes a retweet from a specific tweet
operationId: removeRetweet
tags:
  - Social Actions
parameters:
  - name: tweetId
    in: path
    required: true
    schema:
      type: string
      format: uuid
requestBody:
  required: true
  content:
    application/json:
      schema:
        $ref: '#/components/schemas/RemoveRetweetRequest'
responses:
  '204':
    description: Retweet removed successfully
  '404':
    description: Tweet not found or retweet not found
```

### 2.4 Получение статистики и списков

#### GET /api/v1/tweets/{tweetId}/likes - Кто лайкнул
```yaml
summary: Get tweet likes
description: Retrieves list of users who liked a specific tweet
operationId: getTweetLikes
tags:
  - Social Actions
parameters:
  - name: tweetId
    in: path
    required: true
    schema:
      type: string
      format: uuid
  - name: page
    in: query
    schema:
      type: integer
      minimum: 0
      default: 0
  - name: size
    in: query
    schema:
      type: integer
      minimum: 1
      maximum: 100
      default: 20
responses:
  '200':
    description: Likes retrieved successfully
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/LikeListResponse'
  '404':
    description: Tweet not found
```

#### GET /api/v1/tweets/{tweetId}/retweets - Кто ретвитнул
```yaml
summary: Get tweet retweets
description: Retrieves list of users who retweeted a specific tweet
operationId: getTweetRetweets
tags:
  - Social Actions
parameters:
  - name: tweetId
    in: path
    required: true
    schema:
      type: string
      format: uuid
  - name: page
    in: query
    schema:
      type: integer
      minimum: 0
      default: 0
  - name: size
    in: query
    schema:
      type: integer
      minimum: 1
      maximum: 100
      default: 20
responses:
  '200':
    description: Retweets retrieved successfully
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/RetweetListResponse'
  '404':
    description: Tweet not found
```

## 3. DTO Structures Design

### 3.1 Request DTOs

#### CreateTweetRequest
```yaml
type: object
required:
  - content
  - userId
properties:
  content:
    type: string
    minLength: 1
    maxLength: 280
    description: Tweet content
    example: "Hello Twitter! This is my first tweet."
  userId:
    type: string
    format: uuid
    description: ID of the user creating the tweet
    example: "12345678-1234-1234-1234-123456789abc"
```

#### UpdateTweetRequest
```yaml
type: object
required:
  - content
properties:
  content:
    type: string
    minLength: 1
    maxLength: 280
    description: Updated tweet content
    example: "Updated tweet content"
```

#### LikeTweetRequest
```yaml
type: object
required:
  - userId
properties:
  userId:
    type: string
    format: uuid
    description: ID of the user liking the tweet
    example: "12345678-1234-1234-1234-123456789abc"
```

#### UnlikeTweetRequest
```yaml
type: object
required:
  - userId
properties:
  userId:
    type: string
    format: uuid
    description: ID of the user removing the like
    example: "12345678-1234-1234-1234-123456789abc"
```

#### RetweetRequest
```yaml
type: object
required:
  - userId
properties:
  userId:
    type: string
    format: uuid
    description: ID of the user retweeting
    example: "12345678-1234-1234-1234-123456789abc"
  comment:
    type: string
    maxLength: 280
    description: Optional comment for the retweet
    example: "Great tweet!"
```

#### RemoveRetweetRequest
```yaml
type: object
required:
  - userId
properties:
  userId:
    type: string
    format: uuid
    description: ID of the user removing the retweet
    example: "12345678-1234-1234-1234-123456789abc"
```

### 3.2 Response DTOs

#### TweetResponse
```yaml
type: object
properties:
  id:
    type: string
    format: uuid
    description: Unique identifier of the tweet
    example: "12345678-1234-1234-1234-123456789abc"
  userId:
    type: string
    format: uuid
    description: ID of the tweet author
    example: "12345678-1234-1234-1234-123456789abc"
  content:
    type: string
    description: Tweet content
    example: "Hello Twitter! This is my first tweet."
  createdAt:
    type: string
    format: date-time
    description: Tweet creation timestamp
    example: "2025-01-27T10:30:00Z"
  updatedAt:
    type: string
    format: date-time
    description: Tweet last update timestamp
    example: "2025-01-27T10:30:00Z"
  isDeleted:
    type: boolean
    description: Soft delete flag
    example: false
  deletedAt:
    type: string
    format: date-time
    nullable: true
    description: Tweet deletion timestamp
    example: null
  stats:
    $ref: '#/components/schemas/TweetStats'
  author:
    $ref: '#/components/schemas/UserInfo'
```

#### TweetStats
```yaml
type: object
properties:
  likesCount:
    type: integer
    minimum: 0
    description: Number of likes
    example: 42
  retweetsCount:
    type: integer
    minimum: 0
    description: Number of retweets
    example: 15
  repliesCount:
    type: integer
    minimum: 0
    description: Number of replies
    example: 8
  statsUpdatedAt:
    type: string
    format: date-time
    description: Last stats update timestamp
    example: "2025-01-27T10:30:00Z"
```

#### UserInfo
```yaml
type: object
properties:
  id:
    type: string
    format: uuid
    description: User ID
    example: "12345678-1234-1234-1234-123456789abc"
  login:
    type: string
    description: User login
    example: "john_doe"
  firstName:
    type: string
    description: User first name
    example: "John"
  lastName:
    type: string
    description: User last name
    example: "Doe"
```

#### TweetListResponse
```yaml
type: object
properties:
  tweets:
    type: array
    items:
      $ref: '#/components/schemas/TweetResponse'
  pagination:
    $ref: '#/components/schemas/PaginationInfo'
```

#### PaginationInfo
```yaml
type: object
properties:
  page:
    type: integer
    minimum: 0
    description: Current page number
    example: 0
  size:
    type: integer
    minimum: 1
    description: Page size
    example: 20
  totalElements:
    type: integer
    minimum: 0
    description: Total number of elements
    example: 150
  totalPages:
    type: integer
    minimum: 0
    description: Total number of pages
    example: 8
  hasNext:
    type: boolean
    description: Whether there is a next page
    example: true
  hasPrevious:
    type: boolean
    description: Whether there is a previous page
    example: false
```

#### LikeResponse
```yaml
type: object
properties:
  id:
    type: string
    format: uuid
    description: Like ID
    example: "12345678-1234-1234-1234-123456789abc"
  tweetId:
    type: string
    format: uuid
    description: Tweet ID
    example: "12345678-1234-1234-1234-123456789abc"
  userId:
    type: string
    format: uuid
    description: User ID
    example: "12345678-1234-1234-1234-123456789abc"
  createdAt:
    type: string
    format: date-time
    description: Like creation timestamp
    example: "2025-01-27T10:30:00Z"
```

#### RetweetResponse
```yaml
type: object
properties:
  id:
    type: string
    format: uuid
    description: Retweet ID
    example: "12345678-1234-1234-1234-123456789abc"
  tweetId:
    type: string
    format: uuid
    description: Original tweet ID
    example: "12345678-1234-1234-1234-123456789abc"
  userId:
    type: string
    format: uuid
    description: User ID who retweeted
    example: "12345678-1234-1234-1234-123456789abc"
  comment:
    type: string
    nullable: true
    description: Retweet comment
    example: "Great tweet!"
  createdAt:
    type: string
    format: date-time
    description: Retweet creation timestamp
    example: "2025-01-27T10:30:00Z"
```

#### LikeListResponse
```yaml
type: object
properties:
  likes:
    type: array
    items:
      $ref: '#/components/schemas/LikeResponse'
  pagination:
    $ref: '#/components/schemas/PaginationInfo'
```

#### RetweetListResponse
```yaml
type: object
properties:
  retweets:
    type: array
    items:
      $ref: '#/components/schemas/RetweetResponse'
  pagination:
    $ref: '#/components/schemas/PaginationInfo'
```

### 3.3 Error DTOs

#### ErrorResponse
```yaml
type: object
properties:
  error:
    $ref: '#/components/schemas/ErrorDetails'
  meta:
    $ref: '#/components/schemas/ResponseMeta'
```

#### ErrorDetails
```yaml
type: object
properties:
  code:
    type: string
    description: Error code
    example: "TWEET_NOT_FOUND"
  message:
    type: string
    description: Human-readable error message
    example: "Tweet with id '12345678-1234-1234-1234-123456789abc' not found"
  details:
    type: object
    description: Additional error details
    properties:
      field:
        type: string
        description: Field name that caused the error
        example: "tweetId"
      value:
        type: string
        description: Value that caused the error
        example: "12345678-1234-1234-1234-123456789abc"
```

#### ValidationErrorResponse
```yaml
type: object
properties:
  error:
    type: object
    properties:
      code:
        type: string
        example: "VALIDATION_ERROR"
      message:
        type: string
        example: "Validation failed"
      details:
        type: array
        items:
          $ref: '#/components/schemas/ValidationError'
  meta:
    $ref: '#/components/schemas/ResponseMeta'
```

#### ValidationError
```yaml
type: object
properties:
  field:
    type: string
    description: Field name
    example: "content"
  message:
    type: string
    description: Validation error message
    example: "Tweet content cannot be empty"
  rejectedValue:
    type: string
    description: Rejected value
    example: ""
```

#### ResponseMeta
```yaml
type: object
properties:
  timestamp:
    type: string
    format: date-time
    description: Response timestamp
    example: "2025-01-27T10:30:00Z"
  requestId:
    type: string
    description: Unique request identifier
    example: "req-12345678-1234-1234-1234-123456789abc"
```

## 4. Валидация и бизнес-правила

### 4.1 Валидация входных данных

#### Bean Validation аннотации:
```java
// CreateTweetRequest
@NotBlank(message = "Tweet content cannot be blank")
@Size(min = 1, max = 280, message = "Tweet content must be between 1 and 280 characters")
private String content;

@NotNull(message = "User ID cannot be null")
@Valid
private UUID userId;

// UpdateTweetRequest
@NotBlank(message = "Tweet content cannot be blank")
@Size(min = 1, max = 280, message = "Tweet content must be between 1 and 280 characters")
private String content;
```

#### Кастомные валидаторы:
```java
// Проверка существования пользователя
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserExistsValidator.class)
public @interface UserExists {
    String message() default "User does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Проверка на самолайк/саморетвит
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoSelfActionValidator.class)
public @interface NoSelfAction {
    String message() default "User cannot perform action on their own tweet";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### 4.2 Бизнес-правила

#### Правила создания твита:
- Контент не может быть пустым или состоять только из пробелов
- Максимальная длина контента: 280 символов
- Пользователь должен существовать в users-api
- Твит не может быть создан от имени несуществующего пользователя

#### Правила обновления твита:
- Только автор может обновлять твит
- Удаленные твиты нельзя обновлять
- Новый контент должен соответствовать правилам валидации

#### Правила удаления твита:
- Только автор может удалять твит
- Удаление выполняется как soft delete
- Статистика (лайки, ретвиты) сохраняется

#### Правила социальных действий:
- Пользователь не может лайкнуть/ретвитнуть свой твит
- Один пользователь может лайкнуть твит только один раз
- Один пользователь может ретвитнуть твит только один раз
- При удалении твита все связанные действия помечаются как неактивные

### 4.3 Обработка ошибок

#### Стандартные коды ошибок:
```java
public enum ErrorCode {
    // Общие ошибки
    VALIDATION_ERROR("VALIDATION_ERROR"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR"),
    
    // Ошибки твитов
    TWEET_NOT_FOUND("TWEET_NOT_FOUND"),
    TWEET_ALREADY_DELETED("TWEET_ALREADY_DELETED"),
    TWEET_ACCESS_DENIED("TWEET_ACCESS_DENIED"),
    
    // Ошибки пользователей
    USER_NOT_FOUND("USER_NOT_FOUND"),
    USER_SERVICE_UNAVAILABLE("USER_SERVICE_UNAVAILABLE"),
    
    // Ошибки социальных действий
    LIKE_ALREADY_EXISTS("LIKE_ALREADY_EXISTS"),
    LIKE_NOT_FOUND("LIKE_NOT_FOUND"),
    RETWEET_ALREADY_EXISTS("RETWEET_ALREADY_EXISTS"),
    RETWEET_NOT_FOUND("RETWEET_NOT_FOUND"),
    SELF_ACTION_NOT_ALLOWED("SELF_ACTION_NOT_ALLOWED"),
    
    // Ошибки пагинации
    INVALID_PAGE_NUMBER("INVALID_PAGE_NUMBER"),
    INVALID_PAGE_SIZE("INVALID_PAGE_SIZE")
}
```

#### HTTP статус коды:
- **200 OK**: Успешное получение данных
- **201 Created**: Успешное создание ресурса
- **204 No Content**: Успешное удаление без возврата данных
- **400 Bad Request**: Ошибки валидации или некорректные данные
- **401 Unauthorized**: Отсутствие аутентификации
- **403 Forbidden**: Недостаточно прав для выполнения операции
- **404 Not Found**: Ресурс не найден
- **409 Conflict**: Конфликт состояния (например, дублирование)
- **500 Internal Server Error**: Внутренняя ошибка сервера
- **503 Service Unavailable**: Внешний сервис недоступен

## 5. Интеграционные контракты

### 5.1 Интеграция с users-api

#### Проверка существования пользователя:
```yaml
GET /api/v1/users/{userId}/exists
responses:
  '200':
    description: User exists
    content:
      application/json:
        schema:
          type: object
          properties:
            exists:
              type: boolean
              example: true
  '404':
    description: User not found
```

#### Получение информации о пользователе:
```yaml
GET /api/v1/users/{userId}
responses:
  '200':
    description: User information
    content:
      application/json:
        schema:
          type: object
          properties:
            id:
              type: string
              format: uuid
            login:
              type: string
            firstName:
              type: string
            lastName:
              type: string
```

#### Обработка ошибок интеграции:
```java
// Circuit breaker для users-api
@Component
public class UserServiceClient {
    
    @CircuitBreaker(name = "user-service", fallbackMethod = "fallbackUserExists")
    public boolean userExists(UUID userId) {
        // HTTP call to users-api
    }
    
    public boolean fallbackUserExists(UUID userId, Exception ex) {
        // Fallback logic - assume user exists for graceful degradation
        return true;
    }
}
```

### 5.2 Будущие интеграции

#### follow-service (планируется):
```yaml
GET /api/v1/follows/{userId}/following
description: Get list of users that the user follows
responses:
  '200':
    description: Following list
    content:
      application/json:
        schema:
          type: object
          properties:
            following:
              type: array
              items:
                type: string
                format: uuid
```

#### timeline-service (планируется):
```yaml
POST /api/v1/timeline/{userId}/update
description: Update user timeline with new tweet
requestBody:
  required: true
  content:
    application/json:
      schema:
        type: object
        properties:
          tweetId:
            type: string
            format: uuid
          authorId:
            type: string
            format: uuid
```

## 6. Производительность и масштабируемость

### 6.1 Пагинация

#### Стратегия пагинации:
- **Offset-based pagination** для простых случаев
- **Cursor-based pagination** для больших объемов данных
- **Максимальный размер страницы**: 100 элементов
- **По умолчанию**: 20 элементов на страницу

#### Примеры запросов:
```
GET /api/v1/tweets/user/{userId}?page=0&size=20
GET /api/v1/tweets/timeline/{userId}?page=0&size=50
GET /api/v1/tweets/{tweetId}/likes?page=0&size=20
```

### 6.2 Кэширование

#### Стратегия кэширования:
- **Популярные твиты**: кэш на 5 минут
- **Статистика твитов**: кэш на 1 минуту
- **Информация о пользователях**: кэш на 10 минут
- **Лента новостей**: кэш на 30 секунд

#### HTTP заголовки для кэширования:
```http
Cache-Control: public, max-age=300
ETag: "tweet-12345678-1234-1234-1234-123456789abc-v1"
Last-Modified: Mon, 27 Jan 2025 10:30:00 GMT
```

### 6.3 Rate Limiting

#### Ограничения по запросам:
- **Чтение**: 1000 RPS на пользователя
- **Запись**: 100 RPS на пользователя
- **Создание твитов**: 10 RPS на пользователя
- **Социальные действия**: 50 RPS на пользователя

#### HTTP заголовки для rate limiting:
```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1643284800
```

## 7. Безопасность

### 7.1 Аутентификация и авторизация

#### JWT токены:
```yaml
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Проверка прав доступа:
- **Создание твита**: аутентифицированный пользователь
- **Обновление/удаление**: только автор твита
- **Чтение**: публичный доступ (кроме приватных твитов)
- **Социальные действия**: аутентифицированный пользователь

### 7.2 Валидация входных данных

#### Защита от атак:
- **XSS защита**: экранирование HTML символов
- **SQL injection**: использование JPA/Hibernate
- **CSRF защита**: проверка Origin заголовков
- **Input validation**: строгая валидация всех входных данных

#### Санитизация контента:
```java
@Component
public class ContentSanitizer {
    
    public String sanitizeTweetContent(String content) {
        // Remove potentially dangerous HTML/JavaScript
        // Validate Unicode characters
        // Check for spam patterns
        return sanitizedContent;
    }
}
```

## 8. Мониторинг и логирование

### 8.1 Метрики API

#### Ключевые метрики:
- **Response time**: время ответа по endpoint'ам
- **Throughput**: количество запросов в секунду
- **Error rate**: процент ошибок по типам
- **Availability**: доступность сервиса

#### Prometheus метрики:
```java
@RestController
public class TweetController {
    
    @Timed(name = "tweet.create", description = "Time taken to create tweet")
    @PostMapping("/api/v1/tweets")
    public ResponseEntity<TweetResponse> createTweet(@Valid @RequestBody CreateTweetRequest request) {
        // Implementation
    }
}
```

### 8.2 Логирование

#### Структурированные логи:
```json
{
  "timestamp": "2025-01-27T10:30:00Z",
  "level": "INFO",
  "service": "tweet-api",
  "requestId": "req-12345678-1234-1234-1234-123456789abc",
  "userId": "12345678-1234-1234-1234-123456789abc",
  "action": "CREATE_TWEET",
  "tweetId": "12345678-1234-1234-1234-123456789abc",
  "duration": 150,
  "status": "SUCCESS"
}
```

#### Уровни логирования:
- **DEBUG**: детальная отладочная информация
- **INFO**: общая информация о работе сервиса
- **WARN**: предупреждения о потенциальных проблемах
- **ERROR**: ошибки, требующие внимания
- **FATAL**: критические ошибки, останавливающие сервис

## 9. Тестирование контрактов

### 9.1 Contract Testing

#### Pact тесты для users-api:
```java
@ExtendWith(PactConsumerTestExt.class)
public class UserServiceContractTest {
    
    @Pact(consumer = "tweet-api", provider = "users-api")
    public RequestResponsePact userExistsPact(PactDslWithProvider builder) {
        return builder
            .given("user exists")
            .uponReceiving("a request to check user existence")
            .path("/api/v1/users/12345678-1234-1234-1234-123456789abc/exists")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(new PactDslJsonBody()
                .booleanType("exists", true))
            .toPact();
    }
    
    @Test
    @PactTestFor(pactMethod = "userExistsPact")
    void testUserExists(MockServer mockServer) {
        // Test implementation
    }
}
```

### 9.2 API Testing

#### Тесты с TestContainers:
```java
@SpringBootTest
@Testcontainers
class TweetApiIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("tweet_test")
            .withUsername("test")
            .withPassword("test");
    
    @Test
    void shouldCreateTweetSuccessfully() {
        // Integration test implementation
    }
}
```

## 10. Документация и примеры

### 10.1 OpenAPI спецификация

#### Полная спецификация:
```yaml
openapi: 3.0.3
info:
  title: Tweet API
  description: REST API for managing tweets and social interactions
  version: 1.0.0
  contact:
    name: Twitter Team
    email: api@twitter.com
servers:
  - url: http://localhost:8082/api/v1
    description: Development server
  - url: https://api.twitter.com/api/v1
    description: Production server
```

#### Swagger UI конфигурация:
```java
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.twitter.controller"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(apiInfo());
    }
}
```

### 10.2 Примеры использования

#### Создание твита:
```bash
curl -X POST http://localhost:8082/api/v1/tweets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-jwt-token" \
  -d '{
    "content": "Hello Twitter! This is my first tweet.",
    "userId": "12345678-1234-1234-1234-123456789abc"
  }'
```

#### Получение твитов пользователя:
```bash
curl -X GET "http://localhost:8082/api/v1/tweets/user/12345678-1234-1234-1234-123456789abc?page=0&size=20" \
  -H "Authorization: Bearer your-jwt-token"
```

#### Лайк твита:
```bash
curl -X POST http://localhost:8082/api/v1/tweets/12345678-1234-1234-1234-123456789abc/like \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-jwt-token" \
  -d '{
    "userId": "87654321-4321-4321-4321-210987654321"
  }'
```

## 11. Следующие шаги

### 11.1 Немедленные действия

1. **Создание OpenAPI спецификации** в формате YAML
2. **Реализация DTO классов** с валидацией
3. **Настройка Swagger UI** для документации
4. **Создание базовых тестов** для контрактов

### 11.2 Среднесрочные задачи

1. **Реализация контроллеров** на основе контрактов
2. **Настройка интеграции** с users-api
3. **Создание comprehensive test suite**
4. **Настройка мониторинга** и логирования

### 11.3 Долгосрочные цели

1. **Оптимизация производительности** на основе метрик
2. **Подготовка к масштабированию** с учетом роста нагрузки
3. **Интеграция с будущими сервисами** (follow-service, timeline-service)
4. **Continuous improvement** API на основе обратной связи

## 12. Заключение

### 12.1 Ключевые архитектурные решения

1. **RESTful API** с четким разделением ресурсов
2. **Стандартизированные DTO** для консистентности
3. **Comprehensive validation** для безопасности
4. **Graceful error handling** для лучшего UX
5. **Pagination support** для масштабируемости
6. **Integration contracts** для надежности

### 12.2 Готовность к реализации

- **Детальные контракты** для всех endpoints
- **Четкие DTO структуры** для Request/Response
- **Правила валидации** и обработки ошибок
- **Интеграционные контракты** с users-api
- **Стратегии производительности** и масштабирования

### 12.3 Критерии успешности

- ✅ **OpenAPI спецификация** для всех endpoints
- ✅ **JSON schema** для всех DTO структур
- ✅ **Валидация** и обработка ошибок
- ✅ **Интеграционные контракты** с users-api
- ✅ **Документация** и примеры использования

---

*Документ создан: 2025-01-27*  
*Версия: 1.0*  
*Статус: Completed*
