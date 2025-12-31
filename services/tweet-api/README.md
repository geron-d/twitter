# Tweet API Service

## Введение

**Tweet API** — это микросервис для управления твитами в системе Twitter, построенный на Java 24 и Spring Boot 3. Сервис
предоставляет REST API для создания твитов с валидацией контента и интеграцией с users-api для проверки существования
пользователей.

### Основные возможности:

- ✅ Создание твитов с валидацией контента
- ✅ Получение твита по уникальному идентификатору
- ✅ Получение твитов пользователя с пагинацией
- ✅ Получение ленты новостей (timeline) с пагинацией
- ✅ Обновление твитов с проверкой прав автора
- ✅ Удаление твитов (soft delete) с проверкой прав автора
- ✅ Интеграция с users-api для проверки существования пользователей
- ✅ Интеграция с follower-api для получения списка подписок
- ✅ Валидация данных (длина контента 1-280 символов)
- ✅ OpenAPI/Swagger документация
- ✅ Обработка ошибок по стандарту RFC 7807 Problem Details
- ✅ Логирование запросов
- ✅ Автоматическое управление временными метками

## Архитектура

### Структура пакетов

```
com.twitter/
├── Application.java              # Главный класс приложения
├── controller/
│   ├── TweetApi.java            # OpenAPI интерфейс
│   └── TweetController.java     # REST контроллер
├── dto/
│   ├── request/
│   │   ├── CreateTweetRequestDto.java  # DTO для создания твита
│   │   └── UpdateTweetRequestDto.java   # DTO для обновления твита
│   └── response/
│       └── TweetResponseDto.java       # DTO для ответа
├── entity/
│   └── Tweet.java               # JPA сущность
├── gateway/
│   ├── UserGateway.java        # Gateway для интеграции с users-api
│   └── FollowerGateway.java   # Gateway для интеграции с follower-api
├── client/
│   ├── UsersApiClient.java     # Feign клиент для users-api
│   └── FollowerApiClient.java # Feign клиент для follower-api
├── mapper/
│   └── TweetMapper.java        # MapStruct маппер
├── repository/
│   └── TweetRepository.java    # JPA репозиторий
├── service/
│   ├── TweetService.java       # Интерфейс сервиса
│   └── TweetServiceImpl.java   # Реализация сервиса
├── validation/
│   ├── TweetValidator.java     # Интерфейс валидатора
│   └── TweetValidatorImpl.java # Реализация валидатора
└── config/
    ├── FeignConfig.java        # Конфигурация Feign
    └── OpenApiConfig.java      # Конфигурация OpenAPI
```

## REST API

### Базовый URL

```
http://localhost:8082/api/v1/tweets
```

### Эндпоинты

| Метод    | Путь                  | Описание                      | Тело запроса            | Ответ                        |
|----------|-----------------------|-------------------------------|-------------------------|------------------------------|
| `POST`   | `/`                   | Создать новый твит            | `CreateTweetRequestDto` | `TweetResponseDto`           |
| `GET`    | `/{tweetId}`          | Получить твит по ID            | -                       | `TweetResponseDto`           |
| `GET`    | `/user/{userId}`      | Получить твиты пользователя   | -                       | `PagedModel<TweetResponseDto>`|
| `GET`    | `/timeline/{userId}`  | Получить ленту новостей        | -                       | `PagedModel<TweetResponseDto>`|
| `PUT`    | `/{tweetId}`          | Обновить твит                 | `UpdateTweetRequestDto` | `TweetResponseDto`           |
| `DELETE` | `/{tweetId}`          | Удалить твит (soft delete)    | `DeleteTweetRequestDto` | -                            |

### Детальное описание эндпоинтов

#### 1. Создать твит

```http
POST /api/v1/tweets
Content-Type: application/json
```

**Тело запроса:**

```json
{
  "content": "This is my first tweet!",
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Валидация:**

- `content` - обязательное, 1-280 символов, не может быть пустым или только пробелами
- `userId` - обязательное, UUID существующего пользователя

**Ответы:**

- `201 Created` - твит успешно создан
- `400 Bad Request` - ошибка валидации контента или пользователь не существует
- `400 Bad Request` - нарушение ограничений Bean Validation

**Пример успешного ответа:**

```json
{
  "id": "987e6543-e21b-43d2-b654-321987654321",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "content": "This is my first tweet!",
  "createdAt": "2025-01-27T15:30:00Z",
  "updatedAt": "2025-01-27T15:30:00Z"
}
```

**Пример ошибки валидации контента (400 Bad Request):**

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

**Пример ошибки несуществующего пользователя (400 Bad Request):**

```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 400,
  "detail": "Business rule 'USER_NOT_EXISTS' violated for context: 123e4567-e89b-12d3-a456-426614174000",
  "ruleName": "USER_NOT_EXISTS",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

#### 2. Получить твит по ID

```http
GET /api/v1/tweets/{tweetId}
```

**Параметры пути:**

- `tweetId` - обязательный, UUID существующего твита

**Валидация:**

- `tweetId` - обязательный, должен быть валидным UUID форматом

**Ответы:**

- `200 OK` - твит успешно найден и возвращен
- `404 Not Found` - твит с указанным ID не найден

**Пример успешного ответа (200 OK):**

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "userId": "987e6543-e21b-43d2-b654-321987654321",
  "content": "This is my first tweet!",
  "createdAt": "2025-01-27T15:30:00Z",
  "updatedAt": "2025-01-27T15:30:00Z"
}
```

**Пример ошибки твит не найден (404 Not Found):**

```json
{
  "type": "https://example.com/errors/not-found",
  "title": "Tweet Not Found",
  "status": 404,
  "detail": "Tweet with ID '123e4567-e89b-12d3-a456-426614174000' not found",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

#### 3. Обновить твит

```http
PUT /api/v1/tweets/{tweetId}
Content-Type: application/json
```

**Параметры пути:**

- `tweetId` - обязательный, UUID существующего твита

**Тело запроса:**

```json
{
  "content": "This is updated tweet content",
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Валидация:**

- `content` - обязательное, 1-280 символов, не может быть пустым или только пробелами
- `userId` - обязательное, UUID автора твита (используется для проверки прав)
- `tweetId` - обязательный, должен быть валидным UUID форматом

**Бизнес-правила:**

- Только автор твита может обновлять свой твит
- Твит должен существовать в системе
- Контент должен соответствовать правилам валидации

**Ответы:**

- `200 OK` - твит успешно обновлен
- `400 Bad Request` - ошибка валидации контента или нарушение ограничений Bean Validation
- `409 Conflict` - твит не найден или доступ запрещен (пользователь не является автором)

**Пример успешного ответа (200 OK):**

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "userId": "987e6543-e21b-43d2-b654-321987654321",
  "content": "This is updated tweet content",
  "createdAt": "2025-01-27T15:30:00Z",
  "updatedAt": "2025-01-27T16:45:00Z"
}
```

**Пример ошибки валидации контента (400 Bad Request):**

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

**Пример ошибки доступа запрещен (409 Conflict):**

```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 409,
  "detail": "Business rule 'TWEET_ACCESS_DENIED' violated for context: Only the tweet author can update their tweet",
  "ruleName": "TWEET_ACCESS_DENIED",
  "context": "Only the tweet author can update their tweet",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

**Пример ошибки твит не найден (409 Conflict):**

```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 409,
  "detail": "Business rule 'TWEET_NOT_FOUND' violated for context: 123e4567-e89b-12d3-a456-426614174000",
  "ruleName": "TWEET_NOT_FOUND",
  "context": "123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

#### 4. Получить твиты пользователя

```http
GET /api/v1/tweets/user/{userId}?page=0&size=20&sort=createdAt,DESC
```

**Параметры пути:**

- `userId` - обязательный, UUID пользователя, чьи твиты нужно получить

**Параметры запроса (query parameters):**

- `page` - необязательный, номер страницы (по умолчанию 0)
- `size` - необязательный, размер страницы (по умолчанию 20, максимум 100)
- `sort` - необязательный, параметры сортировки (по умолчанию `createdAt,DESC`)

**Валидация:**

- `userId` - обязательный, должен быть валидным UUID форматом
- `page` - должен быть >= 0
- `size` - должен быть > 0 и <= 100
- `sort` - должен быть валидным форматом сортировки Spring Data JPA

**Бизнес-правила:**

- Твиты сортируются по дате создания в порядке убывания (новые первыми)
- Удаленные твиты (soft delete) исключаются из результатов
- Поддерживается пагинация для работы с большими объемами данных

**Ответы:**

- `200 OK` - твиты успешно получены (может быть пустой список)
- `400 Bad Request` - ошибка валидации (некорректный UUID, неверные параметры пагинации)

**Пример успешного ответа (200 OK) с твитами:**

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
  }
}
```

**Пример успешного ответа (200 OK) с пустым списком:**

```json
{
  "content": [],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

#### 5. Получить ленту новостей (timeline)

```http
GET /api/v1/tweets/timeline/{userId}?page=0&size=20&sort=createdAt,DESC
```

**Параметры пути:**

- `userId` - обязательный, UUID пользователя, чьи твиты нужно получить

**Параметры запроса (query parameters):**

- `page` - необязательный, номер страницы (по умолчанию 0)
- `size` - необязательный, размер страницы (по умолчанию 20, максимум 100)
- `sort` - необязательный, параметры сортировки (по умолчанию `createdAt,DESC`)

**Валидация:**

- `userId` - обязательный, должен быть валидным UUID форматом
- `page` - должен быть >= 0
- `size` - должен быть > 0 и <= 100
- `sort` - должен быть валидным форматом сортировки Spring Data JPA

**Бизнес-правила:**

- Твиты сортируются по дате создания в порядке убывания (новые первыми)
- Удаленные твиты (soft delete) исключаются из результатов
- Поддерживается пагинация для работы с большими объемами данных

**Ответы:**

- `200 OK` - твиты успешно получены (может быть пустой список)
- `400 Bad Request` - ошибка валидации (некорректный UUID, неверные параметры пагинации)

**Пример успешного ответа (200 OK) с твитами:**

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
  }
}
```

**Пример успешного ответа (200 OK) с пустым списком:**

```json
{
  "content": [],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

**Параметры пути:**

- `userId` - обязательный, UUID пользователя, чью ленту новостей нужно получить

**Параметры запроса (query parameters):**

- `page` - необязательный, номер страницы (по умолчанию 0)
- `size` - необязательный, размер страницы (по умолчанию 20, максимум 100)
- `sort` - необязательный, параметры сортировки (по умолчанию `createdAt,DESC`)

**Валидация:**

- `userId` - обязательный, должен быть валидным UUID форматом и существовать в системе
- `page` - должен быть >= 0
- `size` - должен быть > 0 и <= 100
- `sort` - должен быть валидным форматом сортировки Spring Data JPA

**Бизнес-правила:**

- Лента новостей содержит твиты от всех пользователей, на которых подписан указанный пользователь
- Твиты сортируются по дате создания в порядке убывания (новые первыми)
- Удаленные твиты (soft delete) исключаются из результатов
- Если пользователь не имеет подписок, возвращается пустая страница (не ошибка)
- Если подписанные пользователи не имеют твитов, возвращается пустая страница (не ошибка)
- Поддерживается пагинация для работы с большими объемами данных
- Интеграция с follower-api используется для получения списка подписок

**Ответы:**

- `200 OK` - лента новостей успешно получена (может быть пустой список)
- `400 Bad Request` - ошибка валидации (некорректный UUID, неверные параметры пагинации, пользователь не существует)
- `503 Service Unavailable` - follower-api недоступен (graceful degradation - возвращается пустая страница)

**Пример успешного ответа (200 OK) с твитами:**

```json
{
  "content": [
    {
      "id": "111e4567-e89b-12d3-a456-426614174000",
      "userId": "222e4567-e89b-12d3-a456-426614174111",
      "content": "This is a tweet from a followed user!",
      "createdAt": "2025-01-27T15:30:00Z",
      "updatedAt": "2025-01-27T15:30:00Z",
      "isDeleted": false,
      "deletedAt": null
    },
    {
      "id": "333e4567-e89b-12d3-a456-426614174222",
      "userId": "444e4567-e89b-12d3-a456-426614174333",
      "content": "Another tweet from another followed user",
      "createdAt": "2025-01-27T14:20:00Z",
      "updatedAt": "2025-01-27T14:20:00Z",
      "isDeleted": false,
      "deletedAt": null
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 150,
    "totalPages": 8,
    "first": true,
    "last": false
  }
}
```

**Пример успешного ответа (200 OK) с пустой лентой (нет подписок):**

```json
{
  "content": [],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true
  }
}
```

**Пример успешного ответа (200 OK) с пустой лентой (нет твитов):**

```json
{
  "content": [],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true
  }
}
```

**Пример ошибки пользователь не существует (400 Bad Request):**

```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 400,
  "detail": "Business rule 'USER_NOT_EXISTS' violated for context: 123e4567-e89b-12d3-a456-426614174000",
  "ruleName": "USER_NOT_EXISTS",
  "context": "123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

**Пример ошибки невалидный UUID (400 Bad Request):**

```json
{
  "type": "https://example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Invalid UUID format for userId parameter",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

#### 6. Удалить твит

```http
DELETE /api/v1/tweets/{tweetId}
Content-Type: application/json
```

**Параметры пути:**

- `tweetId` - обязательный, UUID существующего твита

**Тело запроса:**

```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Валидация:**

- `userId` - обязательное, UUID автора твита (используется для проверки прав)
- `tweetId` - обязательный, должен быть валидным UUID форматом

**Бизнес-правила:**

- Только автор твита может удалить свой твит
- Твит должен существовать в системе и не быть уже удаленным
- Выполняется soft delete (мягкое удаление) - данные сохраняются в БД

**Ответы:**

- `204 No Content` - твит успешно удален (без тела ответа)
- `400 Bad Request` - ошибка валидации (некорректный UUID, отсутствует userId)
- `404 Not Found` - твит не найден или уже удален
- `409 Conflict` - доступ запрещен (пользователь не является автором)

**Пример успешного ответа (204 No Content):**

Ответ не содержит тела, только HTTP статус 204.

**Пример ошибки валидации userId (400 Bad Request):**

```json
{
  "type": "https://example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed: userId: User ID cannot be null",
  "timestamp": "2025-01-27T15:45:00Z"
}
```

**Пример ошибки твит не найден (404 Not Found):**

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

**Пример ошибки твит уже удален (404 Not Found):**

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

**Пример ошибки доступа запрещен (409 Conflict):**

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

## OpenAPI/Swagger Документация

### Обзор

Сервис включает полную OpenAPI 3.0 документацию, предоставляемую через SpringDoc OpenAPI. Документация содержит
интерактивные возможности для тестирования API, детальные схемы данных и примеры запросов/ответов.

### Доступ к документации

#### Swagger UI

- **URL**: `http://localhost:8082/swagger-ui.html`
- **Описание**: Интерактивный интерфейс для изучения и тестирования API
- **Возможности**:
    - Просмотр всех эндпоинтов с детальным описанием
    - Интерактивное тестирование API (Try it out)
    - Просмотр схем данных и валидации
    - Примеры запросов и ответов
    - Автогенерация клиентского кода

#### OpenAPI Спецификация

- **URL**: `http://localhost:8082/v3/api-docs`
- **Описание**: JSON спецификация OpenAPI 3.0
- **Использование**:
    - Генерация клиентских SDK
    - Импорт в инструменты тестирования API (Postman, Insomnia)
    - Интеграция с CI/CD пайплайнами
    - Валидация API контрактов

#### Конфигурация Swagger

- **URL**: `http://localhost:8082/v3/api-docs/swagger-config`
- **Описание**: Конфигурация Swagger UI

### Примеры использования

#### Доступ к Swagger UI

```bash
# Запуск приложения
./gradlew bootRun

# Открытие Swagger UI в браузере
open http://localhost:8082/swagger-ui.html
```

## Бизнес-логика

### TweetService

Основной сервис для работы с твитами, реализующий следующие операции:

#### Методы сервиса:

1. **`createTweet(CreateTweetRequestDto requestDto)`**
    - Создает новый твит
    - Возвращает `TweetResponseDto`
    - Логика:
        - Валидация контента (длина, не пустой)
        - Проверка существования пользователя через users-api
        - Маппинг DTO в сущность
        - Сохранение в БД
        - Маппинг сущности в DTO ответа

2. **`getTweetById(UUID tweetId)`**
    - Получает твит по уникальному идентификатору
    - Возвращает `Optional<TweetResponseDto>`
    - Логика:
        - Поиск твита в БД по UUID
        - Маппинг сущности в DTO ответа
        - Возвращает `Optional.empty()` если твит не найден

3. **`updateTweet(UUID tweetId, UpdateTweetRequestDto requestDto)`**
    - Обновляет существующий твит
    - Возвращает `TweetResponseDto`
    - Логика:
        - Валидация запроса (существование твита, права автора, контент)
        - Получение твита из БД
        - Обновление контента через маппер
        - Сохранение в БД
        - Маппинг сущности в DTO ответа

4. **`getUserTweets(UUID userId, Pageable pageable)`**
    - Получает пагинированный список твитов пользователя
    - Возвращает `Page<TweetResponseDto>`
    - Логика:
        - Получение твитов из БД по userId с фильтрацией (isDeleted = false)
        - Сортировка по createdAt DESC (новые первыми)
        - Применение пагинации (page, size, sort)
        - Маппинг сущностей в DTO ответа
        - Возврат Page с метаданными пагинации

5. **`getTimeline(UUID userId, Pageable pageable)`**
    - Получает пагинированную ленту новостей (timeline) для пользователя
    - Возвращает `Page<TweetResponseDto>`
    - Логика:
        - Валидация существования пользователя через UserGateway
        - Получение списка подписок через FollowerGateway (интеграция с follower-api)
        - Если список подписок пустой, возвращается пустая страница (не ошибка)
        - Получение твитов из БД по списку userIds (IN запрос) с фильтрацией (isDeleted = false)
        - Сортировка по createdAt DESC (новые первыми)
        - Применение пагинации (page, size, sort)
        - Маппинг сущностей в DTO ответа
        - Возврат Page с метаданными пагинации
    - Особенности:
        - Интеграция с follower-api для получения списка подписок
        - Graceful degradation: при недоступности follower-api возвращается пустая страница
        - Поддержка пагинации для работы с большими объемами данных

6. **`deleteTweet(UUID tweetId, DeleteTweetRequestDto requestDto)`**
    - Удаляет твит (soft delete)
    - Возвращает `void` (ответ 204 No Content)
    - Логика:
        - Валидация запроса (существование твита, права автора, состояние твита)
        - Получение твита из БД
        - Вызов метода `softDelete()` на сущности (устанавливает isDeleted = true и deletedAt = текущее время)
        - Сохранение изменений в БД
        - Возврат без тела ответа

### Ключевые бизнес-правила:

1. **Валидация контента:**
    - Контент должен быть от 1 до 280 символов
    - Контент не может быть пустым или содержать только пробелы
    - Проверка выполняется на уровне Bean Validation и кастомной валидации

2. **Проверка пользователя:**
    - Пользователь должен существовать в системе users-api
    - Проверка выполняется через интеграцию с users-api
    - При отсутствии пользователя выбрасывается `BusinessRuleValidationException`

3. **Временные метки:**
    - `createdAt` устанавливается автоматически при создании
    - `updatedAt` обновляется автоматически при изменении
    - Управление выполняется Hibernate через `@CreationTimestamp` и `@UpdateTimestamp`

4. **Валидация данных:**
    - Все входящие данные валидируются
    - Используется Jakarta Validation
    - Кастомная валидация для бизнес-правил

5. **Удаление твитов (Soft Delete):**
    - Только автор твита может удалить свой твит
    - Выполняется soft delete - данные сохраняются в БД
    - Устанавливается флаг `isDeleted = true` и временная метка `deletedAt`
    - Удаленные твиты не возвращаются в обычных запросах (getTweetById, getUserTweets)
    - Статистика (лайки, ретвиты) сохраняется при удалении

6. **Получение твитов пользователя:**
    - Поддержка пагинации для работы с большими объемами данных
    - Сортировка по дате создания в порядке убывания (новые первыми)
    - Автоматическое исключение удаленных твитов (isDeleted = false)
    - Дефолтные значения пагинации: page=0, size=20, sort=createdAt,DESC
    - Максимальный размер страницы: 100 элементов

7. **Получение ленты новостей (timeline):**
    - Лента содержит твиты от всех пользователей, на которых подписан указанный пользователь
    - Интеграция с follower-api для получения списка подписок
    - Поддержка пагинации для работы с большими объемами данных
    - Сортировка по дате создания в порядке убывания (новые первыми)
    - Автоматическое исключение удаленных твитов (isDeleted = false)
    - Если пользователь не имеет подписок, возвращается пустая страница (не ошибка)
    - Если подписанные пользователи не имеют твитов, возвращается пустая страница (не ошибка)
    - Graceful degradation: при недоступности follower-api возвращается пустая страница
    - Дефолтные значения пагинации: page=0, size=20, sort=createdAt,DESC
    - Максимальный размер страницы: 100 элементов

## Слой валидации

### Архитектура валидации

Сервис использует централизованный слой валидации через `TweetValidator`, который обеспечивает:

- **Единообразную валидацию** для всех операций с твитами
- **Разделение ответственности** между бизнес-логикой и валидацией
- **Типизированные исключения** для различных видов ошибок
- **Интеграцию с Jakarta Validation** для проверки формата данных

### TweetValidator

Интерфейс `TweetValidator` определяет методы валидации для всех операций с твитами. Он включает методы для валидации
создания, проверки контента и проверки существования пользователей.

### Типы исключений валидации

#### 1. FormatValidationException

Используется для ошибок формата данных, таких как некорректная длина контента или пустой контент.

**HTTP статус:** `400 Bad Request`  
**Content-Type:** `application/problem+json`

**Пример ответа:**

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

#### 2. BusinessRuleValidationException

Используется для нарушений бизнес-правил, таких как попытка создать твит от несуществующего пользователя.

**HTTP статус:** `400 Bad Request`  
**Content-Type:** `application/problem+json`

**Пример ответа:**

```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 400,
  "detail": "Business rule 'USER_NOT_EXISTS' violated for context: 123e4567-e89b-12d3-a456-426614174000",
  "ruleName": "USER_NOT_EXISTS",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

### Валидация по операциям

#### Создание твита (CREATE)

Выполняется двухэтапная валидация:

1. **Валидация контента:**
    - Проверка Bean Validation аннотаций (`@NotBlank`, `@Size`)
    - Проверка, что контент не пустой после trim
    - При ошибках выбрасывается `FormatValidationException`

2. **Проверка пользователя:**
    - Проверка, что `userId` не равен `null`
    - Вызов `UserGateway.existsUser()` для проверки существования
    - При отсутствии пользователя выбрасывается `BusinessRuleValidationException`

#### Обновление твита (UPDATE)

Выполняется многоэтапная валидация:

1. **Проверка существования твита:**
    - Проверка, что `tweetId` не равен `null`
    - Поиск твита в БД по UUID
    - При отсутствии твита выбрасывается `BusinessRuleValidationException` с правилом `TWEET_NOT_FOUND`

2. **Проверка прав автора:**
    - Сравнение `userId` из запроса с `userId` твита
    - При несовпадении выбрасывается `BusinessRuleValidationException` с правилом `TWEET_ACCESS_DENIED`

3. **Валидация контента:**
    - Проверка Bean Validation аннотаций (`@NotBlank`, `@Size`)
    - Проверка, что контент не пустой после trim
    - При ошибках выбрасывается `FormatValidationException`

#### Удаление твита (DELETE)

Выполняется многоэтапная валидация:

1. **Проверка tweetId:**
    - Проверка, что `tweetId` не равен `null`
    - При отсутствии выбрасывается `BusinessRuleValidationException` с правилом `TWEET_ID_NULL`

2. **Проверка существования твита:**
    - Поиск твита в БД по UUID
    - При отсутствии твита выбрасывается `BusinessRuleValidationException` с правилом `TWEET_NOT_FOUND`

3. **Проверка состояния твита:**
    - Проверка, что твит не был уже удален (`isDeleted = false`)
    - При попытке удалить уже удаленный твит выбрасывается `BusinessRuleValidationException` с правилом `TWEET_ALREADY_DELETED`

4. **Проверка прав автора:**
    - Сравнение `userId` из запроса с `userId` твита
    - При несовпадении выбрасывается `BusinessRuleValidationException` с правилом `TWEET_ACCESS_DENIED`

## Работа с базой данных

### Таблица tweets

| Поле         | Тип          | Ограничения           | Описание                              |
|--------------|--------------|-----------------------|---------------------------------------|
| `id`         | UUID         | PRIMARY KEY, NOT NULL | Уникальный идентификатор              |
| `user_id`    | UUID         | NOT NULL              | ID пользователя (ссылка на users-api) |
| `content`    | VARCHAR(280) | NOT NULL              | Содержимое твита                      |
| `created_at` | TIMESTAMP    | NOT NULL              | Время создания                        |
| `updated_at` | TIMESTAMP    | NOT NULL              | Время последнего обновления           |
| `is_deleted` | BOOLEAN      | NOT NULL, DEFAULT false | Флаг мягкого удаления                |
| `deleted_at` | TIMESTAMP    | NULL                  | Время мягкого удаления                |

### Ограничения базы данных

1. **CHECK constraint для длины контента:**
   ```sql
   CONSTRAINT chk_content_length CHECK (LENGTH(TRIM(content)) > 0),
   CONSTRAINT chk_content_max_length CHECK (LENGTH(content) <= 280)
   ```

2. **Индексы для оптимизации:**
    - `idx_tweets_user_id_created_at` - для запросов по пользователю с сортировкой по дате
    - `idx_tweets_created_at_desc` - для хронологических запросов ленты

3. **Автоматическое обновление updated_at:**
    - Триггер `update_tweets_updated_at` автоматически обновляет `updated_at` при изменении записи

4. **Soft Delete (мягкое удаление):**
    - Поле `is_deleted` используется для пометки удаленных твитов (по умолчанию `false`)
    - Поле `deleted_at` хранит временную метку удаления (NULL для активных твитов)
    - Удаленные твиты не возвращаются в обычных запросах (используется `findByIdAndIsDeletedFalse()`)
    - Индекс `idx_tweets_is_deleted` для оптимизации запросов активных твитов

## Интеграция с другими сервисами

### Интеграция с users-api

#### Архитектура интеграции

Tweet API интегрируется с Users API через Feign Client для проверки существования пользователей перед созданием твитов.

### Компоненты интеграции

#### 1. UsersApiClient (Feign Client)

Интерфейс Feign Client для вызова users-api

**Конфигурация:**

- Базовый URL: `http://localhost:8081` (настраивается через `app.users-api.base-url`)
- Путь: `/api/v1/users`
- Эндпоинт: `GET /{userId}/exists`

#### 2. UserGateway

Gateway компонент для абстракции работы с users-api

**Особенности:**

- Обрабатывает `null` userId
- Обрабатывает исключения при вызове users-api
- Логирует операции для отладки

### Процесс проверки пользователя

1. **Валидация userId:**
    - Проверка, что `userId` не равен `null`
    - Если `null`, выбрасывается `BusinessRuleValidationException`

2. **Вызов users-api:**
    - `TweetValidator` вызывает `UserGateway.existsUser(userId)`
    - `UserGateway` вызывает `UsersApiClient.existsUser(userId)`
    - Feign выполняет HTTP GET запрос к `http://localhost:8081/api/v1/users/{userId}/exists`

3. **Обработка ответа:**
    - Если пользователь существует (`exists: true`), валидация проходит
    - Если пользователь не существует (`exists: false`), выбрасывается `BusinessRuleValidationException`
    - При ошибке сети или таймауте, считается что пользователь не существует

### Обработка ошибок

#### Сценарии ошибок:

1. **Пользователь не существует:**
    - HTTP 200 OK с `{"exists": false}`
    - Выбрасывается `BusinessRuleValidationException` с правилом `USER_NOT_EXISTS`

2. **Ошибка сети:**
    - Feign выбрасывает исключение
    - `UserGateway` обрабатывает и возвращает `false`
    - Выбрасывается `BusinessRuleValidationException`

3. **Таймаут:**
    - Feign выбрасывает `FeignException`
    - `UserGateway` обрабатывает и возвращает `false`
    - Выбрасывается `BusinessRuleValidationException`

### Интеграция с follower-api

#### Архитектура интеграции

Tweet API интегрируется с Follower API через Feign Client для получения списка подписок пользователя при построении ленты новостей (timeline).

#### Компоненты интеграции

##### 1. FollowerApiClient (Feign Client)

Интерфейс Feign Client для вызова follower-api

**Конфигурация:**

- Базовый URL: `http://localhost:8084` (настраивается через `app.follower-api.base-url`)
- Путь: `/api/v1/follows`
- Эндпоинт: `GET /{userId}/following`

**Метод:**

- `PagedModel<FollowingResponseDto> getFollowing(UUID userId, Pageable pageable)` - получает пагинированный список подписок пользователя

##### 2. FollowerGateway

Gateway компонент для абстракции работы с follower-api

**Особенности:**

- Обрабатывает `null` userId (возвращает пустой список)
- Обрабатывает исключения при вызове follower-api (graceful degradation)
- Логирует операции для отладки
- Получает все подписки через пагинацию (размер страницы 100)

**Метод:**

- `List<UUID> getFollowingUserIds(UUID userId)` - получает список идентификаторов пользователей, на которых подписан указанный пользователь

#### Процесс получения ленты новостей

1. **Валидация userId:**
    - Проверка, что `userId` не равен `null`
    - Проверка существования пользователя через `UserGateway.existsUser()`
    - Если пользователь не существует, выбрасывается `BusinessRuleValidationException`

2. **Получение списка подписок:**
    - `TweetService` вызывает `FollowerGateway.getFollowingUserIds(userId)`
    - `FollowerGateway` делает пагинированные запросы к follower-api до получения всех подписок
    - Используется размер страницы 100 для минимизации количества запросов
    - Если список подписок пустой, возвращается пустая страница (не ошибка)

3. **Получение твитов:**
    - Используется Repository метод `findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc` для получения твитов по списку userIds
    - Применяется пагинация и сортировка по createdAt DESC
    - Маппинг сущностей в DTO ответа
    - Возврат Page с метаданными пагинации

#### Обработка ошибок

##### Сценарии ошибок:

1. **Пользователь не имеет подписок:**
    - HTTP 200 OK с пустым списком подписок
    - Возвращается пустая страница твитов (не ошибка)

2. **Ошибка сети:**
    - Feign выбрасывает исключение
    - `FollowerGateway` обрабатывает и возвращает пустой список (graceful degradation)
    - Возвращается пустая страница твитов (не ошибка)

3. **Таймаут:**
    - Feign выбрасывает `FeignException`
    - `FollowerGateway` обрабатывает и возвращает пустой список (graceful degradation)
    - Возвращается пустая страница твитов (не ошибка)

4. **Follower-api недоступен (503 Service Unavailable):**
    - `FollowerGateway` обрабатывает и возвращает пустой список (graceful degradation)
    - Возвращается пустая страница твитов (не ошибка)
    - Логируется предупреждение для мониторинга

**Логирование:**

- Все вызовы к follower-api логируются на уровне DEBUG
- Ошибки логируются с предупреждением
- Успешные операции логируются на уровне INFO

## Примеры использования

### Создание твита

```bash
curl -X POST http://localhost:8082/api/v1/tweets \
  -H "Content-Type: application/json" \
  -d '{
    "content": "This is my first tweet!",
    "userId": "123e4567-e89b-12d3-a456-426614174000"
  }'
```

**Ответ (201 Created):**

```json
{
  "id": "987e6543-e21b-43d2-b654-321987654321",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "content": "This is my first tweet!",
  "createdAt": "2025-01-27T15:30:00Z",
  "updatedAt": "2025-01-27T15:30:00Z"
}
```

### Получение твита по ID

```bash
curl -X GET http://localhost:8082/api/v1/tweets/123e4567-e89b-12d3-a456-426614174000
```

**Ответ (200 OK):**

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "userId": "987e6543-e21b-43d2-b654-321987654321",
  "content": "This is my first tweet!",
  "createdAt": "2025-01-27T15:30:00Z",
  "updatedAt": "2025-01-27T15:30:00Z"
}
```

**Ответ при отсутствии твита (404 Not Found):**

```json
{
  "type": "https://example.com/errors/not-found",
  "title": "Tweet Not Found",
  "status": 404,
  "detail": "Tweet with ID '123e4567-e89b-12d3-a456-426614174000' not found",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

### Получение твитов пользователя

```bash
# Получить первую страницу (20 твитов по умолчанию)
curl -X GET "http://localhost:8082/api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000"

# Получить вторую страницу с размером 10
curl -X GET "http://localhost:8082/api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?page=1&size=10"

# Получить с кастомной сортировкой
curl -X GET "http://localhost:8082/api/v1/tweets/user/123e4567-e89b-12d3-a456-426614174000?sort=createdAt,ASC"
```

**Ответ (200 OK) с твитами:**

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
  }
}
```

**Ответ (200 OK) с пустым списком:**

```json
{
  "content": [],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

### Получение ленты новостей (timeline)

```bash
# Получить первую страницу ленты новостей (20 твитов по умолчанию)
curl -X GET "http://localhost:8082/api/v1/tweets/timeline/123e4567-e89b-12d3-a456-426614174000"

# Получить вторую страницу с размером 10
curl -X GET "http://localhost:8082/api/v1/tweets/timeline/123e4567-e89b-12d3-a456-426614174000?page=1&size=10"

# Получить с кастомной сортировкой
curl -X GET "http://localhost:8082/api/v1/tweets/timeline/123e4567-e89b-12d3-a456-426614174000?sort=createdAt,ASC"
```

**Ответ (200 OK) с твитами из ленты:**

```json
{
  "content": [
    {
      "id": "111e4567-e89b-12d3-a456-426614174000",
      "userId": "222e4567-e89b-12d3-a456-426614174111",
      "content": "This is a tweet from a followed user!",
      "createdAt": "2025-01-27T15:30:00Z",
      "updatedAt": "2025-01-27T15:30:00Z",
      "isDeleted": false,
      "deletedAt": null
    },
    {
      "id": "333e4567-e89b-12d3-a456-426614174222",
      "userId": "444e4567-e89b-12d3-a456-426614174333",
      "content": "Another tweet from another followed user",
      "createdAt": "2025-01-27T14:20:00Z",
      "updatedAt": "2025-01-27T14:20:00Z",
      "isDeleted": false,
      "deletedAt": null
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 150,
    "totalPages": 8,
    "first": true,
    "last": false
  }
}
```

**Ответ (200 OK) с пустой лентой (нет подписок):**

```json
{
  "content": [],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true
  }
}
```

**Ответ (200 OK) с пустой лентой (нет твитов):**

```json
{
  "content": [],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true
  }
}
```

**Ответ при ошибке пользователь не существует (400 Bad Request):**

```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 400,
  "detail": "Business rule 'USER_NOT_EXISTS' violated for context: 123e4567-e89b-12d3-a456-426614174000",
  "ruleName": "USER_NOT_EXISTS",
  "context": "123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

### Обновление твита

```bash
curl -X PUT http://localhost:8082/api/v1/tweets/123e4567-e89b-12d3-a456-426614174000 \
  -H "Content-Type: application/json" \
  -d '{
    "content": "This is updated tweet content",
    "userId": "987e6543-e21b-43d2-b654-321987654321"
  }'
```

**Ответ (200 OK):**

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "userId": "987e6543-e21b-43d2-b654-321987654321",
  "content": "This is updated tweet content",
  "createdAt": "2025-01-27T15:30:00Z",
  "updatedAt": "2025-01-27T16:45:00Z"
}
```

**Ответ при отсутствии прав (409 Conflict):**

```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 409,
  "detail": "Business rule 'TWEET_ACCESS_DENIED' violated for context: Only the tweet author can update their tweet",
  "ruleName": "TWEET_ACCESS_DENIED",
  "context": "Only the tweet author can update their tweet",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

### Удаление твита

```bash
curl -X DELETE http://localhost:8082/api/v1/tweets/123e4567-e89b-12d3-a456-426614174000 \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "987e6543-e21b-43d2-b654-321987654321"
  }'
```

**Ответ (204 No Content):**

Ответ не содержит тела, только HTTP статус 204.

**Ответ при отсутствии прав (409 Conflict):**

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

**Ответ при отсутствии твита (404 Not Found):**

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

## Конфигурация

### Зависимости

Основные зависимости проекта:

- **Spring Boot 3.x** - основной фреймворк
- **Spring Data JPA** - работа с базой данных
- **Spring Web** - REST API
- **Spring Validation** - валидация данных
- **Spring Cloud OpenFeign** - интеграция с users-api
- **SpringDoc OpenAPI** - документация API и Swagger UI
- **MapStruct** - маппинг объектов
- **Lombok** - генерация кода
- **PostgreSQL** - база данных
- **Micrometer Tracing** - трейсинг

## Запуск и развертывание

### Локальный запуск

1. Убедитесь, что PostgreSQL запущен на порту 5432
2. Убедитесь, что users-api запущен на порту 8081
3. Создайте базу данных `twitter` (если еще не создана)
4. Выполните SQL скрипт для создания таблицы `tweets` (см. `sql/tweets.sql`)
5. Запустите приложение:

```bash
./gradlew bootRun
```

### Docker

```bash
docker build -t tweet-api .
docker run -p 8082:8082 tweet-api
```

### Требования к окружению

- **Java 24** - версия Java для сборки и запуска
- **PostgreSQL** - база данных (порт 5432)
- **Users API** - должен быть запущен на порту 8081 для проверки существования пользователей
- **Follower API** - должен быть запущен на порту 8084 для получения списка подписок (опционально, при недоступности возвращается пустая лента)

## Безопасность

### Валидация

- Все входящие данные валидируются
- Используется Jakarta Validation
- Кастомная валидация для бизнес-правил
- Проверка существования пользователей через users-api

### Логирование

- Все запросы логируются через `@LoggableRequest`
- Подробное логирование операций валидации
- Логирование интеграции с users-api

### Обработка ошибок

- Стандартизированные ответы об ошибках по RFC 7807 Problem Details
- Типизированные исключения для различных сценариев
- Детальная информация об ошибках валидации

## Тестирование

Проект включает:

- **Unit тесты** для всех компонентов
- **Integration тесты** с TestContainers
- **Тесты валидации** с покрытием всех сценариев
- **Тесты интеграции** с users-api через WireMock
- **Тесты исключений** валидации

Запуск тестов:

```bash
./gradlew test
```

### Покрытие тестами

- `TweetControllerTest` - тесты контроллера
- `TweetServiceImplTest` - тесты сервиса
- `TweetValidatorImplTest` - тесты валидатора
- `TweetMapperTest` - тесты маппера
- `UserGatewayTest` - тесты интеграции с users-api
- `BaseIntegrationTest` - базовые интеграционные тесты

