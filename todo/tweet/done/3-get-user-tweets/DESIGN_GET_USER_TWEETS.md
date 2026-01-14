# Проектирование GET /api/v1/tweets/user/{userId}

## Дата: 2025-01-27
## Шаг: #2 - Проектирование API и контрактов

---

## 1. Структура эндпоинта

### 1.1 HTTP метод и путь

- **Метод:** GET
- **Путь:** `/api/v1/tweets/user/{userId}`
- **Content-Type:** не требуется (GET запрос)
- **Accept:** `application/json`

### 1.2 Параметры запроса

**Path параметры:**
- `userId` (UUID, обязательный) - идентификатор пользователя

**Query параметры (пагинация):**
- `page` (int, опциональный, по умолчанию 0) - номер страницы (начиная с 0)
- `size` (int, опциональный, по умолчанию 20, максимум 100) - размер страницы
- `sort` (String, опциональный, по умолчанию `createdAt,DESC`) - параметры сортировки

**Пример запроса:**
```
GET /api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=0&size=20&sort=createdAt,DESC
```

### 1.3 Обоснование структуры

**Почему `/user/{userId}` вместо `/users/{userId}/tweets`:**
- Следует существующему паттерну в архитектуре проекта
- Соответствует структуре из TWEET_API_ARCHITECTURE.md
- Консистентно с другими эндпоинтами (например, `/timeline/{userId}`)

**Параметры пагинации:**
- Используется стандартный Spring Data JPA `Pageable` интерфейс
- Поддерживает стандартные параметры: `page`, `size`, `sort`
- Автоматическая валидация через Spring (size ограничен максимумом 100)

---

## 2. Структура ответа

### 2.1 Тип ответа

**PagedModel<TweetResponseDto>** из Spring HATEOAS

### 2.2 Структура PagedModel

```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "userId": "987fcdeb-51a2-43d7-b123-426614174111",
      "content": "This is a sample tweet content",
      "createdAt": "2025-01-27T15:30:00Z",
      "updatedAt": "2025-01-27T15:30:00Z",
      "isDeleted": false,
      "deletedAt": null
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 150,
    "totalPages": 8
  },
  "_links": {
    "self": {
      "href": "/api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=0&size=20"
    },
    "first": {
      "href": "/api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=0&size=20"
    },
    "last": {
      "href": "/api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=7&size=20"
    },
    "next": {
      "href": "/api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=1&size=20"
    }
  }
}
```

### 2.3 Поля ответа

**content** (List<TweetResponseDto>):
- Массив твитов пользователя
- Отсортирован по `createdAt DESC` (новые первыми)
- Исключает удаленные твиты (isDeleted = false)

**page** (PaginationMetadata):
- `size` - размер страницы
- `number` - номер текущей страницы (0-based)
- `totalElements` - общее количество твитов пользователя
- `totalPages` - общее количество страниц

**_links** (HATEOAS links):
- `self` - ссылка на текущую страницу
- `first` - ссылка на первую страницу
- `last` - ссылка на последнюю страницу
- `next` - ссылка на следующую страницу (если есть)
- `prev` - ссылка на предыдущую страницу (если есть)

---

## 3. Правила валидации

### 3.1 Path параметры

**userId (UUID):**
- Обязательный параметр
- Валидация: автоматическая Spring (UUID format)
- Пример: `123e4567-e89b-12d3-a456-426614174000`
- Ошибка: 400 Bad Request при неверном формате UUID

### 3.2 Query параметры (пагинация)

**page (int):**
- Опциональный параметр
- По умолчанию: 0
- Минимум: 0
- Валидация: автоматическая Spring Data JPA
- Ошибка: 400 Bad Request при отрицательном значении

**size (int):**
- Опциональный параметр
- По умолчанию: 20
- Минимум: 1
- Максимум: 100 (ограничение из архитектуры)
- Валидация: автоматическая Spring Data JPA с @Max(100)
- Ошибка: 400 Bad Request при превышении максимума

**sort (String):**
- Опциональный параметр
- По умолчанию: `createdAt,DESC`
- Формат: `fieldName,DIRECTION` (например, `createdAt,DESC`, `createdAt,ASC`)
- Валидация: автоматическая Spring Data JPA
- Ошибка: 400 Bad Request при неверном формате

### 3.3 Бизнес-правила валидации (Service уровень)

**Метод: `TweetService#getUserTweets(UUID userId, Pageable pageable)`**

**Проверки:**
1. **Валидация userId**
   - userId не может быть null
   - userId должен быть валидным UUID
   - Исключение: `FormatValidationException` с кодом "USER_ID_VALIDATION"
   - HTTP статус: 400 Bad Request

2. **Существование пользователя (опционально)**
   - Пользователь должен существовать в users-api
   - Проверка через интеграцию с users-api (если требуется)
   - Исключение: `BusinessRuleValidationException` с кодом "USER_NOT_EXISTS"
   - HTTP статус: 404 Not Found
   - **Примечание:** В текущей реализации может быть пропущено, если не требуется строгая проверка

3. **Пагинация**
   - Автоматическая валидация через Spring Data JPA
   - size ограничен максимумом 100
   - page не может быть отрицательным

---

## 4. HTTP статусы и сценарии

### 4.1 Успешные сценарии

**200 OK** - Твиты пользователя успешно получены
- Условие: запрос валиден, твиты найдены (даже если список пустой)
- Тело ответа: `PagedModel<TweetResponseDto>` с метаданными пагинации
- Пустой список: возвращается `content: []` с корректными метаданными пагинации

### 4.2 Ошибочные сценарии

**400 Bad Request** - Ошибки валидации
- **Сценарий 1:** Неверный формат UUID для userId
  - Исключение: `MethodArgumentTypeMismatchException` или `IllegalArgumentException`
  - Обработчик: `GlobalExceptionHandler`
  - Формат: ProblemDetail (RFC 7807)
  
- **Сценарий 2:** Неверные параметры пагинации (size > 100, page < 0)
  - Исключение: `MethodArgumentNotValidException` или `ConstraintViolationException`
  - Обработчик: `GlobalExceptionHandler`
  - Формат: ProblemDetail

- **Сценарий 3:** Неверный формат sort параметра
  - Исключение: `IllegalArgumentException`
  - Обработчик: `GlobalExceptionHandler`
  - Формат: ProblemDetail

**404 Not Found** - Пользователь не найден (опционально)
- Условие: пользователь не существует в users-api (если проверка включена)
- Исключение: `BusinessRuleValidationException` с кодом "USER_NOT_EXISTS"
- Обработчик: `GlobalExceptionHandler`
- Формат: ProblemDetail

**500 Internal Server Error** - Внутренняя ошибка сервера
- Условие: неожиданная ошибка при получении твитов
- Исключение: `RuntimeException`
- Обработчик: `GlobalExceptionHandler`
- Формат: ProblemDetail

---

## 5. Контракт метода getUserTweets в TweetApi

### 5.1 Сигнатура метода

```java
@Operation(
    summary = "Get user tweets with pagination",
    description = "Retrieves a paginated list of tweets for a specific user. " +
        "Tweets are sorted by creation date in descending order (newest first). " +
        "Deleted tweets (soft delete) are excluded from the results. " +
        "Supports pagination with page, size, and sort parameters."
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "User tweets retrieved successfully", ...),
    @ApiResponse(responseCode = "400", description = "Validation error", ...),
    @ApiResponse(responseCode = "404", description = "User not found", ...)
})
PagedModel<TweetResponseDto> getUserTweets(
    @Parameter(
        description = "Unique identifier of the user",
        required = true,
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID userId,
    
    @Parameter(
        description = "Pagination parameters (page, size, sorting)",
        required = false
    )
    Pageable pageable
);
```

### 5.2 HTTP метод и путь в Controller

```java
@LoggableRequest
@GetMapping("/user/{userId}")
@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
@Override
public PagedModel<TweetResponseDto> getUserTweets(
    @PathVariable("userId") UUID userId,
    Pageable pageable
) {
    Page<TweetResponseDto> tweets = tweetService.getUserTweets(userId, pageable);
    return new PagedModel<>(tweets);
}
```

**Архитектурное решение:**
- TweetService возвращает `Page<TweetResponseDto>` (Spring Data тип)
- TweetController преобразует `Page` → `PagedModel` (HATEOAS тип)
- Разделение ответственности: Service работает с данными, Controller добавляет HATEOAS links

### 5.3 Параметры

1. **userId** (path variable, UUID)
   - Обязательный параметр
   - Валидация: автоматическая Spring (UUID format)
   - Пример: `123e4567-e89b-12d3-a456-426614174000`

2. **pageable** (query parameters, Pageable)
   - Опциональный параметр
   - Валидация: автоматическая Spring Data JPA
   - Дефолтные значения: page=0, size=20, sort=createdAt,DESC

---

## 6. Примеры запросов/ответов

### 6.1 Успешный запрос (первая страница)

**Запрос:**
```http
GET /api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=0&size=20
Accept: application/json
```

**Ответ (200 OK):**
```json
{
  "content": [
    {
      "id": "111e4567-e89b-12d3-a456-426614174000",
      "userId": "123e4567-e89b-12d3-a456-426614174000",
      "content": "This is my latest tweet!",
      "createdAt": "2025-01-27T15:30:00Z",
      "updatedAt": "2025-01-27T15:30:00Z",
      "isDeleted": false,
      "deletedAt": null
    },
    {
      "id": "222e4567-e89b-12d3-a456-426614174000",
      "userId": "123e4567-e89b-12d3-a456-426614174000",
      "content": "Another tweet from yesterday",
      "createdAt": "2025-01-26T10:15:00Z",
      "updatedAt": "2025-01-26T10:15:00Z",
      "isDeleted": false,
      "deletedAt": null
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 150,
    "totalPages": 8
  },
  "_links": {
    "self": {
      "href": "/api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=0&size=20"
    },
    "first": {
      "href": "/api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=0&size=20"
    },
    "last": {
      "href": "/api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=7&size=20"
    },
    "next": {
      "href": "/api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=1&size=20"
    }
  }
}
```

### 6.2 Успешный запрос (пустой список)

**Запрос:**
```http
GET /api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=0&size=20
```

**Ответ (200 OK):**
```json
{
  "content": [],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 0,
    "totalPages": 0
  },
  "_links": {
    "self": {
      "href": "/api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=0&size=20"
    },
    "first": {
      "href": "/api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=0&size=20"
    },
    "last": {
      "href": "/api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=0&size=20"
    }
  }
}
```

### 6.3 Ошибка валидации (неверный UUID)

**Запрос:**
```http
GET /api/v1/tweets/user/invalid-uuid?page=0&size=20
```

**Ответ (400 Bad Request):**
```json
{
  "type": "https://example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Invalid UUID format for userId parameter",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

### 6.4 Ошибка валидации (size превышает максимум)

**Запрос:**
```http
GET /api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=0&size=200
```

**Ответ (400 Bad Request):**
```json
{
  "type": "https://example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Page size must not be greater than 100",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

### 6.5 Пользователь не найден (опционально)

**Запрос:**
```http
GET /api/v1/tweets/user/999e9999-e99b-99d9-a999-999999999999?page=0&size=20
```

**Ответ (404 Not Found):**
```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 404,
  "detail": "Business rule 'USER_NOT_EXISTS' violated for context: 999e9999-e99b-99d9-a999-999999999999",
  "ruleName": "USER_NOT_EXISTS",
  "context": "999e9999-e99b-99d9-a999-999999999999",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

---

## 7. Интеграция с существующими компонентами

### 7.1 TweetRepository

**Новый метод:**
```java
Page<Tweet> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(
    UUID userId, 
    Pageable pageable
);
```

**Обоснование:**
- Derived Query Method из Spring Data JPA
- Фильтрация по `userId` и `isDeleted = false`
- Сортировка по `createdAt DESC` (новые первыми)
- Поддержка пагинации через `Pageable`

**Использование индекса:**
- Используется существующий индекс `idx_tweets_user_id_created_at` для оптимизации запроса

### 7.2 TweetMapper

**Использование существующего метода:**
```java
TweetResponseDto toResponseDto(Tweet tweet);
```

**Для пагинации:**
- MapStruct автоматически обрабатывает маппинг `Page<Tweet>` → `Page<TweetResponseDto>`
- Или создание `PagedModel` из `Page<TweetResponseDto>` в Service Layer

### 7.3 TweetService

**Новый метод в интерфейсе:**
```java
Page<TweetResponseDto> getUserTweets(UUID userId, Pageable pageable);
```

**Реализация:**
- @Transactional(readOnly = true) аннотация
- Валидация userId (если требуется)
- Вызов `tweetRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId, pageable)`
- Маппинг `Page<Tweet>` → `Page<TweetResponseDto>` через TweetMapper
- Возврат `Page<TweetResponseDto>` с метаданными пагинации

**Архитектурное решение:**
- Service Layer возвращает `Page<TweetResponseDto>` (Spring Data тип)
- Controller Layer преобразует `Page` → `PagedModel` (HATEOAS тип)
- Разделение ответственности: Service работает с данными, Controller добавляет HATEOAS links

### 7.4 TweetController

**Новый метод:**
```java
@LoggableRequest
@GetMapping("/user/{userId}")
@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
@Override
public PagedModel<TweetResponseDto> getUserTweets(
    @PathVariable("userId") UUID userId,
    Pageable pageable
) {
    Page<TweetResponseDto> tweets = tweetService.getUserTweets(userId, pageable);
    return new PagedModel<>(tweets);
}
```

**Особенности:**
- @LoggableRequest для автоматического логирования
- @PageableDefault для установки дефолтных значений пагинации
- Получение `Page<TweetResponseDto>` из TweetService
- Преобразование `Page` → `PagedModel` в контроллере (добавление HATEOAS links)

### 7.5 TweetApi

**Новый метод в интерфейсе:**
- Полная OpenAPI документация с @Operation, @ApiResponses
- Примеры для всех статус-кодов (200, 400, 404)
- @Parameter аннотации для параметров
- Примеры ответов с PagedModel структурой

---

## 8. Предположения и ограничения

### 8.1 Предположения

1. Пользователь может существовать или не существовать в users-api (проверка опциональна)
2. Пагинация использует offset-based подход (page, size) через Spring Data JPA Pageable
3. Твиты сортируются по `createdAt DESC` (новые первыми) по умолчанию
4. Удаленные твиты (isDeleted = true) исключаются из результатов автоматически через Repository метод
5. Максимальный размер страницы: 100 элементов (как указано в архитектуре)
6. По умолчанию: page = 0, size = 20, sort = createdAt,DESC
7. Используется существующий индекс `idx_tweets_user_id_created_at` для оптимизации запросов

### 8.2 Ограничения

1. Производительность может снижаться при большом количестве твитов пользователя (митигация: использование индекса)
2. Проверка существования пользователя может быть опциональной (не требуется строгая валидация)
3. Пагинация ограничена максимумом 100 элементов на страницу

---

## 9. Критерии приемки

- [x] Структура эндпоинта определена (GET /api/v1/tweets/user/{userId})
- [x] Параметры пагинации определены (page, size, sort)
- [x] Структура ответа определена (PagedModel<TweetResponseDto>)
- [x] Правила валидации определены (path параметры, query параметры, бизнес-правила)
- [x] HTTP статусы определены для всех сценариев (200, 400, 404, 500)
- [x] Контракт метода getUserTweets в TweetApi спроектирован
- [x] Интеграция с существующими компонентами определена
- [x] Примеры запросов/ответов подготовлены
- [x] OpenAPI схема спроектирована

---

## 10. Следующие шаги

После завершения проектирования:
1. Реализация метода в TweetRepository (#3)
2. Реализация метода в TweetService интерфейс (#4)
3. Реализация метода в TweetServiceImpl (#5)
4. Реализация метода в TweetApi интерфейс (#6)
5. Реализация метода в TweetController (#7)

