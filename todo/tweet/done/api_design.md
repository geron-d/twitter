# Проектирование API и контрактов для эндпоинта DELETE /api/v1/tweets/{tweetId}/like

**Дата:** 2025-01-27  
**Шаг:** #23  
**Статус:** Выполнено

## 1. OpenAPI схема для DELETE эндпоинта

### 1.1 Интерфейс LikeApi

Добавить метод в интерфейс `LikeApi`:

```java
/**
 * Removes a like from a tweet by deleting the like record.
 * <p>
 * This method removes a like record for a specific tweet. It performs validation
 * on the request data, checks if the tweet exists and is not deleted, verifies that
 * the user exists, and ensures that the like exists before removal. The unlike operation
 * is atomic and updates the tweet's likes count by decrementing it.
 *
 * @param tweetId            the unique identifier of the tweet to unlike (UUID format)
 * @param likeTweetRequest   DTO containing userId for the unlike operation
 * @return ResponseEntity with HTTP 204 No Content status (no response body)
 * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, or like doesn't exist
 */
@Operation(
    summary = "Remove like from tweet",
    description = "Removes a like from a tweet by deleting the like record. " +
        "It performs validation on the request data, checks if the tweet exists and is not deleted, " +
        "verifies that the user exists, and ensures that the like exists before removal. " +
        "The unlike operation is atomic and updates the tweet's likes count by decrementing it."
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "204",
        description = "Like removed successfully"
    ),
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
    ),
    @ApiResponse(
        responseCode = "404",
        description = "Resource not found",
        content = @Content(
            mediaType = "application/problem+json",
            examples = {
                @ExampleObject(
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
                ),
                @ExampleObject(
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
                ),
                @ExampleObject(
                    name = "Like Not Found Error",
                    summary = "Like does not exist",
                    value = """
                        {
                          "type": "https://example.com/errors/business-rule-validation",
                          "title": "Business Rule Validation Error",
                          "status": 404,
                          "detail": "Business rule 'LIKE_NOT_FOUND' violated for context: Like not found for tweet 223e4567-e89b-12d3-a456-426614174001 and user 123e4567-e89b-12d3-a456-426614174000",
                          "ruleName": "LIKE_NOT_FOUND",
                          "context": "Like not found for tweet 223e4567-e89b-12d3-a456-426614174001 and user 123e4567-e89b-12d3-a456-426614174000",
                          "timestamp": "2025-01-27T15:30:00Z"
                        }
                        """
                )
            }
        )
    ),
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
})
ResponseEntity<Void> removeLike(
    @Parameter(
        description = "Unique identifier of the tweet to unlike",
        required = true,
        example = "223e4567-e89b-12d3-a456-426614174001"
    )
    UUID tweetId,
    @Parameter(description = "Unlike request containing userId", required = true)
    LikeTweetRequestDto likeTweetRequest
);
```

### 1.2 Контроллер LikeController

Добавить реализацию метода в `LikeController`:

```java
/**
 * @see LikeApi#removeLike
 */
@LoggableRequest
@DeleteMapping("/{tweetId}/like")
@Override
public ResponseEntity<Void> removeLike(
    @PathVariable("tweetId") UUID tweetId,
    @RequestBody @Valid LikeTweetRequestDto likeTweetRequest) {
    likeService.removeLike(tweetId, likeTweetRequest);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
}
```

## 2. Структура валидации

### 2.1 Метод validateForUnlike

**Статус:** ✅ Уже реализован (шаг #27)

**Интерфейс:** `LikeValidator.validateForUnlike(UUID tweetId, LikeTweetRequestDto requestDto)`

**Реализация:** `LikeValidatorImpl.validateForUnlike()`

**Проверки:**
1. `tweetId != null` → `BusinessRuleValidationException("TWEET_ID_NULL")`
2. Твит существует и не удален → `BusinessRuleValidationException("TWEET_NOT_FOUND")`
3. `requestDto != null` → `BusinessRuleValidationException("LIKE_REQUEST_NULL")`
4. `requestDto.userId() != null` → `BusinessRuleValidationException("USER_ID_NULL")`
5. Пользователь существует → `BusinessRuleValidationException("USER_NOT_EXISTS")`
6. Лайк существует → `BusinessRuleValidationException("LIKE_NOT_FOUND")`

**Используемые методы:**
- `tweetRepository.findByIdAndIsDeletedFalse(tweetId)`
- `userGateway.existsUser(userId)`
- `likeRepository.existsByTweetIdAndUserId(tweetId, userId)`

## 3. Структура бизнес-логики

### 3.1 Метод removeLike в LikeService

**Интерфейс:** `LikeService.removeLike(UUID tweetId, LikeTweetRequestDto requestDto)`

**Сигнатура:**
```java
/**
 * Removes a like from a tweet by deleting the like record.
 * <p>
 * This method performs the following operations:
 * 1. Validates the unlike request (tweet existence, user existence, like existence)
 * 2. Finds the like record in the database
 * 3. Deletes the like record
 * 4. Decrements the tweet's likes count
 * 5. Saves the updated tweet
 * <p>
 * The operation is atomic and executed within a transaction. If any step fails,
 * the entire operation is rolled back.
 *
 * @param tweetId    the unique identifier of the tweet to unlike
 * @param requestDto the unlike request containing userId
 * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, user doesn't exist, or like doesn't exist
 */
void removeLike(UUID tweetId, LikeTweetRequestDto requestDto);
```

**Реализация в LikeServiceImpl:**
```java
/**
 * @see LikeService#removeLike
 */
@Override
@Transactional
public void removeLike(UUID tweetId, LikeTweetRequestDto requestDto) {
    // 1. Валидация
    likeValidator.validateForUnlike(tweetId, requestDto);
    
    // 2. Поиск лайка
    Like like = likeRepository.findByTweetIdAndUserId(tweetId, requestDto.userId())
        .orElseThrow(() -> new IllegalStateException("Like not found after validation"));
    
    // 3. Удаление лайка
    likeRepository.delete(like);
    
    // 4. Получение твита и обновление счетчика
    Tweet tweet = tweetRepository.findByIdAndIsDeletedFalse(tweetId)
        .orElseThrow(() -> new IllegalStateException("Tweet not found after validation"));
    tweet.decrementLikesCount();
    
    // 5. Сохранение твита
    tweetRepository.saveAndFlush(tweet);
}
```

**Особенности:**
- Использование `@Transactional` для атомарности
- Использование `saveAndFlush()` для немедленного сохранения
- Обработка `IllegalStateException` для случаев, когда данные не найдены после валидации

### 3.2 Метод decrementLikesCount в Entity Tweet

**Сигнатура:**
```java
/**
 * Decrements the likes count for this tweet.
 * <p>
 * This method atomically decrements the likesCount field by 1.
 * It should be called when a like is removed from this tweet.
 * The method handles null values and prevents negative counts by setting the counter to 0
 * if it would become negative.
 */
public void decrementLikesCount() {
    if (this.likesCount == null || this.likesCount <= 0) {
        this.likesCount = 0;
    } else {
        this.likesCount--;
    }
}
```

**Особенности:**
- Защита от отрицательных значений
- Обработка null значений
- Атомарная операция (выполняется в транзакции)

## 4. Переиспользование существующих DTO

### 4.1 LikeTweetRequestDto

**Статус:** ✅ Переиспользуется

**Структура:**
```java
@Schema(
    name = "LikeTweetRequest",
    description = "Data structure for liking/unliking a tweet in the system",
    example = """
        {
          "userId": "123e4567-e89b-12d3-a456-426614174000"
        }
        """
)
@Builder
public record LikeTweetRequestDto(
    @Schema(
        description = "The ID of the user who likes/unlikes the tweet",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "User ID cannot be null")
    UUID userId
) {
}
```

**Использование:**
- POST `/api/v1/tweets/{tweetId}/likes` - для создания лайка
- DELETE `/api/v1/tweets/{tweetId}/like` - для удаления лайка

**Примечание:** DTO используется для обеих операций, что обеспечивает консистентность API.

## 5. Контракты и соглашения

### 5.1 HTTP статус-коды

| Код | Описание | Условие |
|-----|----------|---------|
| 204 | No Content | Лайк успешно удален |
| 400 | Bad Request | Ошибка валидации (невалидный UUID, null значения) |
| 404 | Not Found | Твит не найден, пользователь не найден, лайк не найден |
| 409 | Conflict | Не используется для DELETE операции |

### 5.2 Формат ошибок

Все ошибки возвращаются в формате RFC 7807 Problem Details:

```json
{
  "type": "https://example.com/errors/[error-type]",
  "title": "[Error Title]",
  "status": [HTTP_STATUS_CODE],
  "detail": "[Detailed error message]",
  "timestamp": "2025-01-27T15:30:00Z",
  "[additional fields]": "[additional values]"
}
```

### 5.3 Исключения

| Исключение | HTTP код | Условие |
|------------|---------|---------|
| `BusinessRuleValidationException("TWEET_ID_NULL")` | 400 | tweetId равен null |
| `BusinessRuleValidationException("TWEET_NOT_FOUND")` | 404 | Твит не найден или удален |
| `BusinessRuleValidationException("LIKE_REQUEST_NULL")` | 400 | requestDto равен null |
| `BusinessRuleValidationException("USER_ID_NULL")` | 400 | userId равен null |
| `BusinessRuleValidationException("USER_NOT_EXISTS")` | 404 | Пользователь не существует |
| `BusinessRuleValidationException("LIKE_NOT_FOUND")` | 404 | Лайк не существует |

## 6. Последовательность операций

### 6.1 Успешный сценарий

```
1. Controller получает запрос DELETE /api/v1/tweets/{tweetId}/like
2. Controller вызывает likeService.removeLike(tweetId, requestDto)
3. Service вызывает likeValidator.validateForUnlike(tweetId, requestDto)
   ├─ 3.1. Проверка tweetId != null
   ├─ 3.2. Проверка существования твита
   ├─ 3.3. Проверка requestDto != null
   ├─ 3.4. Проверка userId != null
   ├─ 3.5. Проверка существования пользователя
   └─ 3.6. Проверка существования лайка
4. Service находит лайк через likeRepository.findByTweetIdAndUserId()
5. Service удаляет лайк через likeRepository.delete(like)
6. Service получает твит через tweetRepository.findByIdAndIsDeletedFalse()
7. Service вызывает tweet.decrementLikesCount()
8. Service сохраняет твит через tweetRepository.saveAndFlush(tweet)
9. Controller возвращает ResponseEntity.status(HttpStatus.NO_CONTENT).build()
```

### 6.2 Ошибочный сценарий (лайк не найден)

```
1. Controller получает запрос DELETE /api/v1/tweets/{tweetId}/like
2. Controller вызывает likeService.removeLike(tweetId, requestDto)
3. Service вызывает likeValidator.validateForUnlike(tweetId, requestDto)
   └─ 3.6. Проверка существования лайка → FAIL
4. Validator выбрасывает BusinessRuleValidationException("LIKE_NOT_FOUND")
5. GlobalExceptionHandler обрабатывает исключение
6. Controller возвращает 404 Not Found с Problem Details
```

## 7. Зависимости и интеграции

### 7.1 Внутренние зависимости

- **LikeRepository:**
  - `findByTweetIdAndUserId(UUID tweetId, UUID userId)` - поиск лайка
  - `delete(Like like)` - удаление лайка
  - `existsByTweetIdAndUserId(UUID tweetId, UUID userId)` - проверка существования

- **TweetRepository:**
  - `findByIdAndIsDeletedFalse(UUID tweetId)` - получение твита
  - `saveAndFlush(Tweet tweet)` - сохранение твита с обновлением счетчика

- **LikeValidator:**
  - `validateForUnlike(UUID tweetId, LikeTweetRequestDto requestDto)` - валидация

- **UserGateway:**
  - `existsUser(UUID userId)` - проверка существования пользователя (используется в валидаторе)

### 7.2 Внешние зависимости

- **users-api:** для проверки существования пользователя через UserGateway

## 8. Требования к реализации

### 8.1 Обязательные компоненты

- ✅ Метод `validateForUnlike()` - уже реализован
- ⚠️ Метод `removeLike()` в интерфейсе `LikeService`
- ⚠️ Метод `removeLike()` в реализации `LikeServiceImpl`
- ⚠️ Метод `decrementLikesCount()` в Entity `Tweet`
- ⚠️ Метод `removeLike()` в интерфейсе `LikeApi` с OpenAPI аннотациями
- ⚠️ Метод `removeLike()` в контроллере `LikeController`

### 8.2 Требования к тестам

- Unit тесты для `LikeServiceImpl.removeLike()`
- Unit тесты для `LikeValidatorImpl.validateForUnlike()` (уже должны быть)
- Integration тесты для `LikeController.removeLike()` с MockMvc
- Проверка всех HTTP статус-кодов (204, 400, 404)

### 8.3 Требования к документации

- JavaDoc для всех новых методов
- OpenAPI аннотации с примерами для всех сценариев
- Обновление README.md
- Обновление Postman коллекции

## 9. Выводы

### 9.1 Готовые компоненты

- ✅ DTO (`LikeTweetRequestDto`) - переиспользуется
- ✅ Validator (`validateForUnlike`) - уже реализован
- ✅ Repository методы - все необходимые методы существуют
- ✅ Exception handling - через `GlobalExceptionHandler`

### 9.2 Требуемые изменения

- ⚠️ Добавить метод `decrementLikesCount()` в Entity `Tweet`
- ⚠️ Добавить метод `removeLike()` в интерфейс `LikeService`
- ⚠️ Реализовать метод `removeLike()` в `LikeServiceImpl`
- ⚠️ Добавить метод `removeLike()` в интерфейс `LikeApi` с OpenAPI аннотациями
- ⚠️ Реализовать метод `removeLike()` в `LikeController`
- ⚠️ Добавить JavaDoc для всех новых методов
- ⚠️ Создать unit тесты для Service
- ⚠️ Создать integration тесты для Controller
- ⚠️ Обновить Swagger документацию
- ⚠️ Обновить README.md
- ⚠️ Обновить Postman коллекцию

### 9.3 Соответствие стандартам

Все проектирование соответствует стандартам проекта:
- ✅ STANDART_CODE.md - архитектура, паттерны, обработка ошибок
- ✅ STANDART_PROJECT.md - использование общих компонентов
- ✅ STANDART_TEST.md - структура и именование тестов
- ✅ STANDART_JAVADOC.md - документация всех публичных методов
- ✅ STANDART_SWAGGER.md - полная OpenAPI документация

