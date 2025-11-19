# Проектирование PUT /api/v1/tweets/{tweetId}

## Дата: 2025-01-27
## Шаг: #2 - Проектирование API и контрактов

---

## 1. Структура UpdateTweetRequestDto

### 1.1 Поля DTO

```java
public record UpdateTweetRequestDto(
    String content,    // 1-280 символов, @NotBlank, @Size
    UUID userId        // @NotNull - для проверки прав автора
)
```

### 1.2 Обоснование структуры

**Поля:**
- `content` (String) - новый контент твита
  - Обязательное поле
  - Валидация: @NotBlank, @Size(min=1, max=280)
  - Аналогично CreateTweetRequestDto
  
- `userId` (UUID) - идентификатор пользователя, выполняющего обновление
  - Обязательное поле
  - Валидация: @NotNull
  - Используется для проверки прав автора (сравнение с userId твита)
  - Временное решение до внедрения системы аутентификации

**Отличия от CreateTweetRequestDto:**
- Та же структура полей (content, userId)
- Те же правила валидации
- userId используется для проверки прав, а не для создания

### 1.3 Bean Validation аннотации

```java
@NotBlank(message = "Tweet content cannot be empty")
@Size(min = 1, max = 280, message = "Tweet content must be between 1 and 280 characters")
String content

@NotNull(message = "User ID cannot be null")
UUID userId
```

### 1.4 OpenAPI Schema аннотации

```java
@Schema(
    name = "UpdateTweetRequest",
    description = "Data structure for updating existing tweets in the system",
    example = """
        {
          "content": "This is updated tweet content",
          "userId": "123e4567-e89b-12d3-a456-426614174000"
        }
        """
)
```

Для полей:
- `content`: description, example, minLength=1, maxLength=280, requiredMode=REQUIRED
- `userId`: description, example (UUID), format="uuid", requiredMode=REQUIRED

---

## 2. Правила валидации

### 2.1 Bean Validation (DTO уровень)

**Проверки на уровне DTO:**
1. `content` не может быть null или пустым (@NotBlank)
2. `content` должен быть от 1 до 280 символов (@Size)
3. `userId` не может быть null (@NotNull)

### 2.2 Бизнес-правила валидации (Service уровень)

**Метод: `TweetValidator#validateForUpdate(UUID tweetId, UpdateTweetRequestDto requestDto)`**

**Проверки:**
1. **Существование твита**
   - Твит должен существовать в БД
   - Твит не должен быть удален (soft delete)
   - Исключение: `BusinessRuleValidationException` с кодом "TWEET_NOT_FOUND"
   - HTTP статус: 404 Not Found

2. **Права автора**
   - `requestDto.userId()` должен совпадать с `tweet.userId`
   - Исключение: `BusinessRuleValidationException` с кодом "TWEET_ACCESS_DENIED"
   - HTTP статус: 403 Forbidden

3. **Ограничение времени обновления**
   - Разница между текущим временем и `tweet.createdAt` не должна превышать 7 дней
   - Исключение: `BusinessRuleValidationException` с кодом "TWEET_UPDATE_TIME_EXPIRED"
   - HTTP статус: 400 Bad Request

4. **Ограничение частоты обновлений**
   - Максимум 10 обновлений в час для одного твита
   - Требуется отслеживание количества обновлений (может потребоваться Redis/кэш)
   - Исключение: `BusinessRuleValidationException` с кодом "TWEET_UPDATE_RATE_LIMIT_EXCEEDED"
   - HTTP статус: 400 Bad Request

5. **Валидация контента**
   - Вызов `validateContent(UpdateTweetRequestDto)` - аналогично create
   - Проверка на пустоту после trim
   - Исключение: `FormatValidationException` с кодом "CONTENT_VALIDATION"
   - HTTP статус: 400 Bad Request

### 2.3 Порядок выполнения валидации

1. Bean Validation (автоматически при @Valid)
2. Существование твита
3. Права автора
4. Ограничение времени
5. Ограничение частоты
6. Валидация контента

**Принцип Fail Fast:** остановка при первой ошибке валидации.

---

## 3. HTTP статусы и сценарии

### 3.1 Успешные сценарии

**200 OK** - Твит успешно обновлен
- Условие: все валидации пройдены, твит обновлен
- Тело ответа: `TweetResponseDto` с обновленными данными
- `updatedAt` автоматически обновляется через `@UpdateTimestamp`

### 3.2 Ошибочные сценарии

**400 Bad Request** - Ошибки валидации
- **Сценарий 1:** Ошибка Bean Validation (content пустой, слишком длинный, userId null)
  - Исключение: `ConstraintViolationException` или `MethodArgumentNotValidException`
  - Обработчик: `GlobalExceptionHandler`
  - Формат: ProblemDetail (RFC 7807)
  
- **Сценарий 2:** Ошибка валидации контента (пустой после trim)
  - Исключение: `FormatValidationException` с кодом "CONTENT_VALIDATION"
  - Обработчик: `GlobalExceptionHandler`
  - Формат: ProblemDetail
  
- **Сценарий 3:** Превышено время обновления (более 7 дней)
  - Исключение: `BusinessRuleValidationException` с кодом "TWEET_UPDATE_TIME_EXPIRED"
  - Обработчик: `GlobalExceptionHandler`
  - Формат: ProblemDetail
  
- **Сценарий 4:** Превышена частота обновлений (более 10 в час)
  - Исключение: `BusinessRuleValidationException` с кодом "TWEET_UPDATE_RATE_LIMIT_EXCEEDED"
  - Обработчик: `GlobalExceptionHandler`
  - Формат: ProblemDetail

**403 Forbidden** - Недостаточно прав
- Условие: `requestDto.userId()` не совпадает с `tweet.userId`
- Исключение: `BusinessRuleValidationException` с кодом "TWEET_ACCESS_DENIED"
- Обработчик: `GlobalExceptionHandler`
- Формат: ProblemDetail

**404 Not Found** - Твит не найден
- Условие: твит не существует или удален (soft delete)
- Исключение: `BusinessRuleValidationException` с кодом "TWEET_NOT_FOUND"
- Обработчик: `GlobalExceptionHandler`
- Формат: ProblemDetail

**500 Internal Server Error** - Внутренняя ошибка сервера
- Условие: неожиданная ошибка при обновлении
- Исключение: `RuntimeException`
- Обработчик: `GlobalExceptionHandler`
- Формат: ProblemDetail

---

## 4. Контракт метода updateTweet в TweetApi

### 4.1 Сигнатура метода

```java
@Operation(
    summary = "Update existing tweet",
    description = "Updates an existing tweet with new content. " +
        "Only the tweet author can update their tweet. " +
        "The tweet must not be older than 7 days and update frequency " +
        "must not exceed 10 updates per hour. " +
        "The content must be between 1 and 280 characters."
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Tweet updated successfully", ...),
    @ApiResponse(responseCode = "400", description = "Validation error", ...),
    @ApiResponse(responseCode = "403", description = "Access denied", ...),
    @ApiResponse(responseCode = "404", description = "Tweet not found", ...)
})
ResponseEntity<TweetResponseDto> updateTweet(
    @Parameter(description = "Unique identifier of the tweet", required = true, example = "...")
    UUID tweetId,
    
    @Parameter(description = "Tweet data for update", required = true)
    UpdateTweetRequestDto updateTweetRequest
);
```

### 4.2 HTTP метод и путь

- **Метод:** PUT
- **Путь:** `/api/v1/tweets/{tweetId}`
- **Content-Type:** `application/json`
- **Accept:** `application/json`

### 4.3 Параметры

1. **tweetId** (path variable, UUID)
   - Обязательный параметр
   - Валидация: автоматическая Spring (UUID format)
   - Пример: `123e4567-e89b-12d3-a456-426614174000`

2. **updateTweetRequest** (request body, UpdateTweetRequestDto)
   - Обязательный параметр
   - Валидация: @Valid аннотация
   - Content-Type: application/json

### 4.4 Примеры запросов/ответов

**Успешный запрос:**
```http
PUT /api/v1/tweets/123e4567-e89b-12d3-a456-426614174000
Content-Type: application/json

{
  "content": "This is updated tweet content",
  "userId": "987e6543-e21b-43d2-b654-321987654321"
}
```

**Успешный ответ (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "userId": "987e6543-e21b-43d2-b654-321987654321",
  "content": "This is updated tweet content",
  "createdAt": "2025-01-20T10:30:00Z",
  "updatedAt": "2025-01-27T15:30:00Z"
}
```

**Ошибка валидации (400 Bad Request):**
```json
{
  "type": "https://example.com/errors/format-validation",
  "title": "Format Validation Error",
  "status": 400,
  "detail": "Tweet content must be between 1 and 280 characters",
  "fieldName": "content",
  "constraintName": "CONTENT_VALIDATION",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

**Ошибка доступа (403 Forbidden):**
```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 403,
  "detail": "Business rule 'TWEET_ACCESS_DENIED' violated",
  "ruleName": "TWEET_ACCESS_DENIED",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

**Твит не найден (404 Not Found):**
```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 404,
  "detail": "Business rule 'TWEET_NOT_FOUND' violated for context: 123e4567-e89b-12d3-a456-426614174000",
  "ruleName": "TWEET_NOT_FOUND",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

---

## 5. Интеграция с существующими компонентами

### 5.1 TweetMapper

**Новый метод:**
```java
@Mapping(target = "id", ignore = true)
@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "updatedAt", ignore = true)
@Mapping(target = "userId", ignore = true)
void updateTweetFromUpdateDto(UpdateTweetRequestDto updateDto, @MappingTarget Tweet tweet);
```

**Игнорируемые поля:**
- `id` - системное поле, не изменяется
- `createdAt` - системное поле, не изменяется
- `updatedAt` - обновляется автоматически через @UpdateTimestamp
- `userId` - системное поле, не изменяется (проверяется в валидаторе)

**Обновляемые поля:**
- `content` - обновляется из `updateDto.content()`

### 5.2 TweetValidator

**Новый метод в интерфейсе:**
```java
void validateForUpdate(UUID tweetId, UpdateTweetRequestDto requestDto);
```

**Реализация:**
- Проверка существования твита
- Проверка прав автора
- Проверка времени обновления (7 дней)
- Проверка частоты обновлений (10 в час)
- Валидация контента

### 5.3 TweetService

**Новый метод в интерфейсе:**
```java
TweetResponseDto updateTweet(UUID tweetId, UpdateTweetRequestDto requestDto);
```

**Реализация:**
- @Transactional аннотация
- Вызов `tweetValidator.validateForUpdate(tweetId, requestDto)`
- Получение твита из репозитория
- Вызов `tweetMapper.updateTweetFromUpdateDto(requestDto, tweet)`
- Сохранение через `tweetRepository.saveAndFlush(tweet)`
- Преобразование в `TweetResponseDto` через `tweetMapper.toResponseDto(tweet)`
- Возврат `TweetResponseDto`

### 5.4 TweetController

**Новый метод:**
```java
@LoggableRequest
@PutMapping("/{tweetId}")
@Override
public ResponseEntity<TweetResponseDto> updateTweet(
    @PathVariable("tweetId") UUID tweetId,
    @RequestBody @Valid UpdateTweetRequestDto updateTweetRequest) {
    TweetResponseDto updatedTweet = tweetService.updateTweet(tweetId, updateTweetRequest);
    return ResponseEntity.ok(updatedTweet);
}
```

### 5.5 TweetApi

**Новый метод в интерфейсе:**
- Полная OpenAPI документация с @Operation, @ApiResponses
- Примеры для всех статус-кодов (200, 400, 403, 404)
- @Parameter аннотации для параметров

---

## 6. Предположения и ограничения

### 6.1 Предположения

1. Проверка прав автора выполняется через сравнение `userId` из запроса с `userId` из твита (временное решение до внедрения аутентификации)

2. Ограничение времени обновления (7 дней) реализуется на уровне валидатора через расчет разницы между `createdAt` и текущим временем

3. Ограничение частоты обновлений (10 в час) реализуется на уровне валидатора (может потребоваться Redis/кэш для отслеживания)

4. `updatedAt` обновляется автоматически через JPA `@UpdateTimestamp`

5. Используется существующий `GlobalExceptionHandler` для обработки ошибок

### 6.2 Ограничения

1. Отслеживание частоты обновлений может потребовать дополнительной инфраструктуры (Redis)

2. Проверка прав автора через `userId` в запросе не является безопасной (временное решение)

3. В будущем потребуется интеграция с системой аутентификации для проверки прав

---

## 7. Критерии приемки

- [x] UpdateTweetRequestDto структура определена (content, userId)
- [x] Правила валидации определены (Bean Validation + бизнес-правила)
- [x] HTTP статусы определены для всех сценариев (200, 400, 403, 404, 500)
- [x] Контракт метода updateTweet в TweetApi спроектирован
- [x] Интеграция с существующими компонентами определена
- [x] Примеры запросов/ответов подготовлены

---

## 8. Следующие шаги

После завершения проектирования:
1. Реализация UpdateTweetRequestDto (#3)
2. Реализация метода маппинга в TweetMapper (#4)
3. Реализация validateForUpdate в TweetValidator (#5)
4. Реализация updateTweet в TweetService (#6)
5. Реализация updateTweet в TweetController (#7)
6. Реализация updateTweet в TweetApi (#8)

