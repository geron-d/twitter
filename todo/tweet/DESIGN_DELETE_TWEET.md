# Проектирование API и контрактов: DELETE /api/v1/tweets/{tweetId}

## Meta
- **project**: twitter-tweet-api
- **document_type**: API Design Document
- **version**: 1.0
- **created_date**: 2025-01-27
- **status**: Completed
- **step**: #2 from TODO_DELETE_TWEET.md

## 1. Структура эндпоинта DELETE

### 1.1 HTTP метод и путь

**Эндпоинт:** `DELETE /api/v1/tweets/{tweetId}`

**HTTP метод:** `DELETE`

**Путь:** `/api/v1/tweets/{tweetId}`

**Версионирование:** `/api/v1/` (соответствует существующим эндпоинтам)

### 1.2 Параметры запроса

**Path параметры:**
- `tweetId` (UUID, required) - уникальный идентификатор твита для удаления
  - Тип: `UUID`
  - Формат: стандартный UUID (например, `123e4567-e89b-12d3-a456-426614174000`)
  - Валидация: автоматическая валидация Spring через `@PathVariable`

**Request Body:**
- `DeleteTweetRequestDto` (JSON, required) - DTO с userId для проверки прав доступа
  - Структура: `{ "userId": "uuid" }`
  - Валидация: Bean Validation через `@Valid`

### 1.3 Request DTO

**DeleteTweetRequestDto:**
```java
package com.twitter.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/**
 * Data Transfer Object for deleting a tweet.
 *
 * @param userId the ID of the user performing the delete (used for authorization check)
 * @author geron
 * @version 1.0
 */
@Schema(
    name = "DeleteTweetRequest",
    description = "Data structure for deleting tweets in the system",
    example = """
        {
          "userId": "123e4567-e89b-12d3-a456-426614174000"
        }
        """
)
@Builder
public record DeleteTweetRequestDto(
    @Schema(
        description = "The ID of the user performing the delete (used for authorization check)",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "User ID cannot be null")
    UUID userId
) {
}
```

**Обоснование:**
- Аналогично `UpdateTweetRequestDto`, userId передается в теле запроса для проверки прав доступа
- Простая структура: только userId, без дополнительных полей
- Bean Validation через `@NotNull` для обязательности поля

## 2. HTTP статусы и ответы

### 2.1 Успешный ответ

**HTTP статус:** `204 No Content`

**Тело ответа:** Отсутствует (пустое тело)

**Описание:** Твит успешно удален (soft delete). Операция выполнена без ошибок.

**Пример ответа:**
```
HTTP/1.1 204 No Content
Content-Length: 0
```

### 2.2 Ошибочные ответы

#### 2.2.1 404 Not Found

**HTTP статус:** `404 Not Found`

**Content-Type:** `application/problem+json`

**Причины:**
- Твит не найден по указанному `tweetId`
- Твит уже удален (soft delete)

**Пример ответа:**
```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 404,
  "detail": "Business rule 'TWEET_NOT_FOUND' violated for context: 123e4567-e89b-12d3-a456-426614174000",
  "ruleName": "TWEET_NOT_FOUND",
  "context": "123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2025-01-27T15:45:00Z"
}
```

**Или для уже удаленного твита:**
```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 404,
  "detail": "Business rule 'TWEET_ALREADY_DELETED' violated for context: 123e4567-e89b-12d3-a456-426614174000",
  "ruleName": "TWEET_ALREADY_DELETED",
  "context": "123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2025-01-27T15:45:00Z"
}
```

#### 2.2.2 409 Conflict

**HTTP статус:** `409 Conflict`

**Content-Type:** `application/problem+json`

**Причина:** Доступ запрещен - пользователь не является автором твита

**Пример ответа:**
```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 409,
  "detail": "Business rule 'TWEET_ACCESS_DENIED' violated for context: Only the tweet author can delete their tweet",
  "ruleName": "TWEET_ACCESS_DENIED",
  "context": "Only the tweet author can delete their tweet",
  "timestamp": "2025-01-27T15:45:00Z"
}
```

#### 2.2.3 400 Bad Request

**HTTP статус:** `400 Bad Request`

**Content-Type:** `application/problem+json`

**Причины:**
- Некорректный формат UUID для `tweetId`
- Отсутствует или некорректный `userId` в теле запроса
- Ошибки валидации Bean Validation

**Пример ответа (некорректный UUID):**
```json
{
  "type": "https://example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Invalid UUID format for tweetId parameter",
  "timestamp": "2025-01-27T15:45:00Z"
}
```

**Пример ответа (отсутствует userId):**
```json
{
  "type": "https://example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed: userId: User ID cannot be null",
  "timestamp": "2025-01-27T15:45:00Z"
}
```

## 3. OpenAPI документация

### 3.1 Метод в TweetApi интерфейсе

```java
/**
 * Deletes a tweet by performing soft delete.
 * <p>
 * Only the tweet author can delete their tweet. The method performs validation
 * on the request data, checks tweet existence, verifies authorization, and
 * performs soft delete by setting isDeleted flag and deletedAt timestamp.
 * The tweet data is preserved in the database for analytics and recovery purposes.
 *
 * @param tweetId            the unique identifier of the tweet to delete (UUID format)
 * @param deleteTweetRequest DTO containing userId for authorization check
 * @return ResponseEntity with HTTP 204 status if deletion is successful
 * @throws BusinessRuleValidationException if tweet doesn't exist, is already deleted, or access denied
 */
@Operation(
    summary = "Delete tweet",
    description = "Deletes a tweet by performing soft delete. " +
        "Only the tweet author can delete their tweet. " +
        "The method performs validation on the request data, checks tweet existence, " +
        "verifies authorization, and performs soft delete by setting isDeleted flag " +
        "and deletedAt timestamp. The tweet data is preserved in the database " +
        "for analytics and recovery purposes."
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "204",
        description = "Tweet deleted successfully"
    ),
    @ApiResponse(
        responseCode = "404",
        description = "Tweet not found or already deleted",
        content = @Content(
            mediaType = "application/problem+json",
            examples = {
                @ExampleObject(
                    name = "Tweet Not Found",
                    summary = "Tweet does not exist",
                    value = """
                        {
                          "type": "https://example.com/errors/business-rule-validation",
                          "title": "Business Rule Validation Error",
                          "status": 404,
                          "detail": "Business rule 'TWEET_NOT_FOUND' violated for context: 123e4567-e89b-12d3-a456-426614174000",
                          "ruleName": "TWEET_NOT_FOUND",
                          "context": "123e4567-e89b-12d3-a456-426614174000",
                          "timestamp": "2025-01-27T15:45:00Z"
                        }
                        """
                ),
                @ExampleObject(
                    name = "Tweet Already Deleted",
                    summary = "Tweet is already soft deleted",
                    value = """
                        {
                          "type": "https://example.com/errors/business-rule-validation",
                          "title": "Business Rule Validation Error",
                          "status": 404,
                          "detail": "Business rule 'TWEET_ALREADY_DELETED' violated for context: 123e4567-e89b-12d3-a456-426614174000",
                          "ruleName": "TWEET_ALREADY_DELETED",
                          "context": "123e4567-e89b-12d3-a456-426614174000",
                          "timestamp": "2025-01-27T15:45:00Z"
                        }
                        """
                )
            }
        )
    ),
    @ApiResponse(
        responseCode = "409",
        description = "Business rule violation - Access denied",
        content = @Content(
            mediaType = "application/problem+json",
            examples = @ExampleObject(
                name = "Access Denied Error",
                summary = "User is not the tweet author",
                value = """
                    {
                      "type": "https://example.com/errors/business-rule-validation",
                      "title": "Business Rule Validation Error",
                      "status": 409,
                      "detail": "Business rule 'TWEET_ACCESS_DENIED' violated for context: Only the tweet author can delete their tweet",
                      "ruleName": "TWEET_ACCESS_DENIED",
                      "context": "Only the tweet author can delete their tweet",
                      "timestamp": "2025-01-27T15:45:00Z"
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
            examples = {
                @ExampleObject(
                    name = "Invalid UUID Format Error",
                    summary = "Invalid tweet ID format",
                    value = """
                        {
                          "type": "https://example.com/errors/validation-error",
                          "title": "Validation Error",
                          "status": 400,
                          "detail": "Invalid UUID format for tweetId parameter",
                          "timestamp": "2025-01-27T15:45:00Z"
                        }
                        """
                ),
                @ExampleObject(
                    name = "User ID Validation Error",
                    summary = "User ID is null or invalid",
                    value = """
                        {
                          "type": "https://example.com/errors/validation-error",
                          "title": "Validation Error",
                          "status": 400,
                          "detail": "Validation failed: userId: User ID cannot be null",
                          "timestamp": "2025-01-27T15:45:00Z"
                        }
                        """
                )
            }
        )
    )
})
ResponseEntity<Void> deleteTweet(
    @Parameter(
        description = "Unique identifier of the tweet to delete",
        required = true,
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID tweetId,
    @Parameter(description = "Tweet deletion request with userId for authorization", required = true)
    DeleteTweetRequestDto deleteTweetRequest);
```

### 3.2 Реализация в TweetController

```java
/**
 * @see TweetApi#deleteTweet
 */
@LoggableRequest
@DeleteMapping("/{tweetId}")
@Override
public ResponseEntity<Void> deleteTweet(
    @PathVariable("tweetId") UUID tweetId,
    @RequestBody @Valid DeleteTweetRequestDto deleteTweetRequest) {
    tweetService.deleteTweet(tweetId, deleteTweetRequest);
    return ResponseEntity.noContent().build();
}
```

## 4. Валидация прав доступа

### 4.1 Структура валидации

**Порядок валидации:**
1. **Bean Validation** - автоматическая валидация `DeleteTweetRequestDto` через `@Valid`
   - Проверка: `userId` не должен быть `null`
   - Исключение: `ConstraintViolationException` → HTTP 400

2. **Валидация tweetId** - проверка формата UUID
   - Проверка: автоматическая валидация Spring через `@PathVariable`
   - Исключение: `MethodArgumentTypeMismatchException` → HTTP 400

3. **Валидация существования твита** - проверка в БД
   - Проверка: твит найден по `tweetId`
   - Исключение: `BusinessRuleValidationException` с кодом `TWEET_NOT_FOUND` → HTTP 404

4. **Валидация состояния твита** - проверка, что твит не удален
   - Проверка: `!Boolean.TRUE.equals(tweet.getIsDeleted())`
   - Исключение: `BusinessRuleValidationException` с кодом `TWEET_ALREADY_DELETED` → HTTP 404

5. **Валидация прав доступа** - проверка авторства
   - Проверка: `tweet.getUserId().equals(deleteTweetRequest.userId())`
   - Исключение: `BusinessRuleValidationException` с кодом `TWEET_ACCESS_DENIED` → HTTP 409

### 4.2 Метод валидации в TweetValidator

```java
/**
 * Performs complete validation for tweet deletion.
 * <p>
 * This method validates tweet data for deletion including:
 * <ul>
 *   <li>Existence of the tweet (tweetId must not be null and tweet must exist)</li>
 *   <li>State check (tweet must not be already deleted)</li>
 *   <li>Authorization check (only tweet author can delete their tweet)</li>
 * </ul>
 *
 * @param tweetId    the unique identifier of the tweet to delete
 * @param requestDto DTO containing userId for authorization check
 * @throws BusinessRuleValidationException if tweetId is null, tweet doesn't exist, is already deleted, or access denied
 */
void validateForDelete(UUID tweetId, DeleteTweetRequestDto requestDto);
```

### 4.3 Реализация валидации в TweetValidatorImpl

```java
@Override
public void validateForDelete(UUID tweetId, DeleteTweetRequestDto requestDto) {
    if (tweetId == null) {
        log.warn("Tweet ID is null");
        throw new BusinessRuleValidationException("TWEET_ID_NULL", "Tweet ID cannot be null");
    }

    Tweet tweet = tweetRepository.findById(tweetId)
        .orElseThrow(() -> {
            log.warn("Tweet with ID {} not found", tweetId);
            return new BusinessRuleValidationException("TWEET_NOT_FOUND", tweetId);
        });

    if (Boolean.TRUE.equals(tweet.getIsDeleted())) {
        log.warn("Tweet with ID {} is already deleted", tweetId);
        throw new BusinessRuleValidationException("TWEET_ALREADY_DELETED", tweetId);
    }

    validateTweetOwnership(tweet, requestDto.userId());
}
```

## 5. Структура ответов

### 5.1 Успешный ответ (204 No Content)

**Особенности:**
- Пустое тело ответа (без JSON)
- HTTP статус 204
- Content-Length: 0

**Обоснование:**
- Соответствует RESTful принципам для DELETE операций
- Экономия трафика (нет необходимости возвращать данные)
- Стандартная практика для успешного удаления

### 5.2 Ошибочные ответы (RFC 7807 Problem Details)

**Формат:** `application/problem+json`

**Структура:**
- `type` - URI типа ошибки
- `title` - краткое описание ошибки
- `status` - HTTP статус код
- `detail` - детальное описание ошибки
- `ruleName` - код бизнес-правила (для BusinessRuleValidationException)
- `context` - контекст ошибки
- `timestamp` - временная метка ошибки

**Обоснование:**
- Соответствие стандарту RFC 7807
- Консистентность с существующими эндпоинтами
- Детальная информация для клиентов API

## 6. Соответствие стандартам

### 6.1 STANDART_CODE.md

- ✅ Использование Records для DTO (`DeleteTweetRequestDto`)
- ✅ Bean Validation аннотации (`@NotNull`)
- ✅ Lombok `@Builder` для совместимости
- ✅ Java 24 features (Records, Text Blocks)

### 6.2 STANDART_PROJECT.md

- ✅ `@LoggableRequest` на методе контроллера
- ✅ Использование `GlobalExceptionHandler` для обработки ошибок
- ✅ `BusinessRuleValidationException` для бизнес-правил

### 6.3 STANDART_SWAGGER.md

- ✅ `@Operation` с summary и description
- ✅ `@ApiResponses` для всех возможных ответов
- ✅ `@Parameter` для описания параметров
- ✅ Примеры ответов в `@ExampleObject`
- ✅ Использование `@Schema` для DTO

### 6.4 STANDART_JAVADOC.md

- ✅ JavaDoc для всех public методов
- ✅ `@author geron`
- ✅ `@version 1.0`
- ✅ `@param` для всех параметров
- ✅ `@throws` для исключений

## 7. Примеры использования

### 7.1 Успешное удаление

**Запрос:**
```http
DELETE /api/v1/tweets/123e4567-e89b-12d3-a456-426614174000
Content-Type: application/json

{
  "userId": "987e6543-e21b-43d2-b654-321987654321"
}
```

**Ответ:**
```http
HTTP/1.1 204 No Content
Content-Length: 0
```

### 7.2 Твит не найден

**Запрос:**
```http
DELETE /api/v1/tweets/00000000-0000-0000-0000-000000000000
Content-Type: application/json

{
  "userId": "987e6543-e21b-43d2-b654-321987654321"
}
```

**Ответ:**
```http
HTTP/1.1 404 Not Found
Content-Type: application/problem+json

{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 404,
  "detail": "Business rule 'TWEET_NOT_FOUND' violated for context: 00000000-0000-0000-0000-000000000000",
  "ruleName": "TWEET_NOT_FOUND",
  "context": "00000000-0000-0000-0000-000000000000",
  "timestamp": "2025-01-27T15:45:00Z"
}
```

### 7.3 Доступ запрещен

**Запрос:**
```http
DELETE /api/v1/tweets/123e4567-e89b-12d3-a456-426614174000
Content-Type: application/json

{
  "userId": "11111111-1111-1111-1111-111111111111"
}
```

**Ответ:**
```http
HTTP/1.1 409 Conflict
Content-Type: application/problem+json

{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 409,
  "detail": "Business rule 'TWEET_ACCESS_DENIED' violated for context: Only the tweet author can delete their tweet",
  "ruleName": "TWEET_ACCESS_DENIED",
  "context": "Only the tweet author can delete their tweet",
  "timestamp": "2025-01-27T15:45:00Z"
}
```

## 8. Критерии успеха

- ✅ Структура эндпоинта DELETE определена
- ✅ HTTP статусы определены (204, 404, 409, 400)
- ✅ Структура валидации спроектирована
- ✅ OpenAPI документация спроектирована
- ✅ Request DTO спроектирован (DeleteTweetRequestDto)
- ✅ Примеры использования подготовлены
- ✅ Соответствие стандартам проверено

## 9. Следующие шаги

Следующий шаг: **#3: Обновление Entity Tweet** - Добавить поля isDeleted и deletedAt, метод softDelete()

---

*Документ создан: 2025-01-27*  
*Версия: 1.0*  
*Статус: Completed*

