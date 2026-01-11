# Admin Script API Service

## Введение

**Admin Script API** — это микросервис для выполнения административных скриптов в системе Twitter, построенный на Java 24 и Spring Boot 3. Сервис предоставляет REST API для массовой генерации пользователей, follow-отношений и твитов с использованием библиотеки Datafaker для создания реалистичных тестовых данных. Сервис интегрируется с users-api, follower-api и tweet-api для создания сущностей в системе.

### Основные возможности:

- ✅ Генерация множественных пользователей с рандомными данными (login, email, firstName, lastName, password)
- ✅ Создание follow-отношений между пользователями (центральный пользователь фолловит половину остальных, половина остальных фолловят центрального)
- ✅ Создание твитов для каждого пользователя с рандомным контентом
- ✅ Удаление твитов у случайных пользователей для тестирования
- ✅ Создание лайков для случайных твитов (половина, треть, 1 пользователь)
- ✅ Создание ретвитов для случайных твитов (половина, треть, 1 пользователь)
- ✅ Подробная статистика выполнения скрипта (включая количество созданных лайков и ретвитов)
- ✅ Обработка частичных ошибок с graceful degradation
- ✅ Интеграция с users-api через Feign Client
- ✅ Интеграция с follower-api через Feign Client
- ✅ Интеграция с tweet-api через Feign Client
- ✅ Валидация параметров скрипта (Bean Validation и Business Rule Validation)
- ✅ Использование Datafaker для генерации реалистичных данных
- ✅ OpenAPI/Swagger документация
- ✅ Обработка ошибок по стандарту RFC 7807 Problem Details
- ✅ Логирование всех операций

## Архитектура

### Структура пакетов

```
com.twitter/
├── Application.java                    # Главный класс приложения
├── controller/
│   ├── AdminScriptApi.java            # OpenAPI интерфейс
│   └── AdminScriptController.java     # REST контроллер
├── dto/
│   ├── request/
│   │   └── BaseScriptRequestDto.java  # DTO для запроса скрипта
│   ├── response/
│   │   ├── BaseScriptResponseDto.java  # DTO для ответа скрипта
│   │   └── ScriptStatisticsDto.java               # DTO для статистики
│   └── external/
│       ├── UserRequestDto.java         # DTO для создания пользователя (users-api)
│       ├── UserResponseDto.java        # DTO ответа users-api
│       ├── CreateTweetRequestDto.java  # DTO для создания твита (tweet-api)
│       ├── DeleteTweetRequestDto.java  # DTO для удаления твита (tweet-api)
│       ├── LikeTweetRequestDto.java    # DTO для создания лайка (tweet-api)
│       ├── RetweetRequestDto.java     # DTO для создания ретвита (tweet-api)
│       ├── TweetResponseDto.java       # DTO ответа tweet-api
│       ├── LikeResponseDto.java       # DTO ответа лайка (tweet-api)
│       └── RetweetResponseDto.java    # DTO ответа ретвита (tweet-api)
├── client/
│   ├── UsersApiClient.java            # Feign клиент для users-api
│   ├── FollowApiClient.java           # Feign клиент для follower-api
│   └── TweetsApiClient.java          # Feign клиент для tweet-api
├── gateway/
│   ├── UsersGateway.java             # Gateway для users-api с обработкой ошибок
│   ├── FollowGateway.java            # Gateway для follower-api с обработкой ошибок
│   └── TweetsGateway.java            # Gateway для tweet-api с обработкой ошибок
├── service/
│   ├── GenerateUsersAndTweetsService.java      # Интерфейс сервиса
│   └── GenerateUsersAndTweetsServiceImpl.java  # Реализация сервиса
├── util/
│   └── RandomDataGenerator.java      # Генератор рандомных данных (Datafaker)
├── validation/
│   ├── GenerateUsersAndTweetsValidator.java      # Интерфейс валидатора
│   └── GenerateUsersAndTweetsValidatorImpl.java  # Реализация валидатора
└── config/
    ├── FeignConfig.java               # Конфигурация Feign
    └── OpenApiConfig.java             # Конфигурация OpenAPI
```

## REST API

### Базовый URL

```
http://localhost:8083/api/v1/admin-scripts
```

### Эндпоинты

| Метод | Путь | Описание | Параметры | Тело запроса | Ответ |
|-------|------|----------|-----------|--------------|-------|
| `POST` | `/generate-users-and-tweets` | Выполнить административный скрипт | - | `BaseScriptRequestDto` | `BaseScriptResponseDto` |

### Детальное описание эндпоинтов

#### 1. Выполнение административного скрипта

```http
POST /api/v1/admin-scripts/generate-users-and-tweets
Content-Type: application/json
```

**Параметры:**
- Нет

**Тело запроса:**
```json
{
  "nUsers": 10,
  "nTweetsPerUser": 5,
  "lUsersForDeletion": 3
}
```

**Валидация:**
- `nUsers` - обязательное поле, целое число от 1 до 1000
- `nTweetsPerUser` - обязательное поле, целое число от 1 до 100
- `lUsersForDeletion` - обязательное поле, целое число от 0 и выше, не должно превышать количество пользователей с твитами

**Ответы:**
- `200 OK` - скрипт выполнен (может содержать ошибки в statistics.errors)
- `400 Bad Request` - ошибка валидации параметров (Bean Validation или Business Rule Validation)

**Пример успешного ответа:**
```json
{
  "createdUsers": [
    "123e4567-e89b-12d3-a456-426614174000",
    "223e4567-e89b-12d3-a456-426614174001",
    "323e4567-e89b-12d3-a456-426614174002"
  ],
  "createdTweets": [
    "423e4567-e89b-12d3-a456-426614174003",
    "523e4567-e89b-12d3-a456-426614174004",
    "623e4567-e89b-12d3-a456-426614174005"
  ],
  "deletedTweets": [
    "423e4567-e89b-12d3-a456-426614174003"
  ],
  "statistics": {
    "totalUsersCreated": 3,
    "totalTweetsCreated": 15,
    "totalFollowsCreated": 2,
    "totalTweetsDeleted": 1,
    "usersWithTweets": 3,
    "usersWithoutTweets": 0,
    "totalLikesCreated": 5,
    "totalRetweetsCreated": 4,
    "executionTimeMs": 1234,
    "errors": []
  }
}
```

**Пример ответа с ошибками:**
```json
{
  "createdUsers": [
    "123e4567-e89b-12d3-a456-426614174000",
    "223e4567-e89b-12d3-a456-426614174001"
  ],
  "createdTweets": [
    "423e4567-e89b-12d3-a456-426614174003",
    "523e4567-e89b-12d3-a456-426614174004"
  ],
  "deletedTweets": [],
  "statistics": {
    "totalUsersCreated": 2,
    "totalTweetsCreated": 10,
    "totalFollowsCreated": 1,
    "totalTweetsDeleted": 0,
    "usersWithTweets": 2,
    "usersWithoutTweets": 0,
    "totalLikesCreated": 2,
    "totalRetweetsCreated": 1,
    "executionTimeMs": 856,
    "errors": [
      "Validation failed: Business rule 'DELETION_COUNT_EXCEEDS_USERS_WITH_TWEETS' violated for context: Cannot delete tweets from 5 users: only 2 users have tweets"
    ]
  }
}
```

## OpenAPI/Swagger Документация

### Обзор

Сервис включает полную OpenAPI 3.0 документацию, предоставляемую через SpringDoc OpenAPI. Документация содержит интерактивные возможности для тестирования API, детальные схемы данных и примеры запросов/ответов.

### Доступ к документации

#### Swagger UI
- **URL**: `http://localhost:8083/swagger-ui.html`
- **Описание**: Интерактивный интерфейс для изучения и тестирования API
- **Возможности**:
  - Просмотр всех эндпоинтов с детальным описанием
  - Интерактивное тестирование API (Try it out)
  - Просмотр схем данных и валидации
  - Примеры запросов и ответов

#### OpenAPI Specification
- **URL**: `http://localhost:8083/v3/api-docs`
- **Описание**: JSON спецификация OpenAPI 3.0
- **Использование**: Импорт в Postman, генерация клиентов, документация

#### Конфигурация
- **Класс**: `com.twitter.config.OpenApiConfig`
- **Версия API**: `1.0.0`
- **Title**: `Twitter Admin Script API`

### Особенности документации

- ✅ Полное описание всех эндпоинтов
- ✅ Примеры запросов и ответов (успешные и с ошибками)
- ✅ Документация всех возможных ошибок (400, 500)
- ✅ Схемы данных для всех DTO
- ✅ Валидационные правила для всех полей
- ✅ Описание бизнес-логики скрипта

## Бизнес-логика

### GenerateUsersAndTweetsService

Основной сервис для выполнения административного скрипта, реализующий следующие операции:

#### Методы сервиса:

1. **`executeScript(BaseScriptRequestDto requestDto)`**
   - Выполняет полный цикл административного скрипта
   - Возвращает `BaseScriptResponseDto` со списками ID и статистикой
   - Логика выполнения:
     - **Шаг 1:** Создание nUsers пользователей с рандомными данными через `RandomDataGenerator` и `UsersGateway`
     - **Шаг 1.5:** Создание follow-отношений между пользователями:
       - Выбор центрального пользователя (первый созданный пользователь)
       - Вычисление половины остальных пользователей (целочисленное деление)
       - Центральный пользователь фолловит половину остальных пользователей
       - Половина остальных пользователей фолловят центрального пользователя
       - Создание follow-отношений через `FollowGateway` с обработкой ошибок
     - **Шаг 2:** Создание nTweetsPerUser твитов для каждого успешно созданного пользователя через `RandomDataGenerator` и `TweetsGateway` (кэширование TweetResponseDto для последующего использования)
     - **Шаг 3:** Подсчёт пользователей с твитами и без твитов через `TweetsGateway.getUserTweets()`
     - **Шаг 4:** Валидация параметра lUsersForDeletion через `GenerateUsersAndTweetsValidator` (проверка, что lUsersForDeletion <= usersWithTweetsCount)
     - **Шаг 5:** Выбор l случайных пользователей с твитами и удаление по 1 твиту у каждого через `TweetsGateway.deleteTweet()`
     - **Шаг 6:** Создание лайков для случайного твита (половина пользователей, исключая автора твита) через `TweetsGateway.likeTweet()`
     - **Шаг 7:** Создание лайков для другого случайного твита (треть пользователей, исключая автора твита) через `TweetsGateway.likeTweet()`
     - **Шаг 8:** Создание одного лайка для другого случайного твита (1 пользователь, исключая автора твита) через `TweetsGateway.likeTweet()`
     - **Шаг 9:** Создание ретвитов для другого случайного твита (половина пользователей, исключая автора твита) через `TweetsGateway.retweetTweet()`
     - **Шаг 10:** Создание ретвитов для другого случайного твита (треть пользователей, исключая автора твита) через `TweetsGateway.retweetTweet()`
     - **Шаг 11:** Создание одного ретвита для другого случайного твита (1 пользователь, исключая автора твита) через `TweetsGateway.retweetTweet()`
     - **Шаг 12:** Сбор статистики (totalUsersCreated, totalFollowsCreated, totalTweetsCreated, totalTweetsDeleted, usersWithTweets, usersWithoutTweets, totalLikesCreated, totalRetweetsCreated, executionTimeMs, errors)

### Ключевые бизнес-правила:

1. **Обработка частичных ошибок:**
   - Ошибки при создании пользователей/твитов/удалении обрабатываются gracefully
   - Ошибки логируются и добавляются в `statistics.errors`
   - Выполнение продолжается для максимизации успешных операций

2. **Валидация параметров удаления:**
   - `lUsersForDeletion` не должно превышать количество пользователей с твитами
   - При нарушении правила ошибка добавляется в `statistics.errors`, удаление пропускается

3. **Генерация уникальных данных:**
   - Login и email генерируются с использованием timestamp/UUID для обеспечения уникальности
   - Все данные генерируются через библиотеку Datafaker

4. **Создание follow-отношений:**
   - Follow-отношения создаются после создания всех пользователей, но до создания твитов
   - Требуется минимум 2 пользователя для создания follow-отношений
   - Центральный пользователь (первый созданный) фолловит половину остальных
   - Половина остальных пользователей фолловят центрального пользователя
   - При ошибках создания follow-отношений выполнение продолжается, ошибки логируются и добавляются в statistics.errors

5. **Статистика выполнения:**
   - Подсчитывается время выполнения скрипта в миллисекундах
   - Собираются все ошибки, возникшие во время выполнения
   - Подсчитывается количество созданных follow-отношений (totalFollowsCreated)
   - Подсчитывается количество пользователей с твитами и без твитов
   - Подсчитывается количество созданных лайков (totalLikesCreated)
   - Подсчитывается количество созданных ретвитов (totalRetweetsCreated)

6. **Создание лайков и ретвитов (шаги 6-11):**
   - Для каждой операции выбирается случайный твит из созданных (6 разных твитов для 6 операций)
   - Пользователи выбираются случайно, исключая автора твита (для избежания self-like/self-retweet ошибок)
   - Используется `Collections.shuffle()` для случайного выбора твитов и пользователей
   - Ошибки (self-like, self-retweet, дубликаты) обрабатываются gracefully: логируются и добавляются в errors, выполнение продолжается
   - Требуется минимум 6 твитов и 2 пользователя для выполнения всех шагов
   - TweetResponseDto кэшируются при создании твитов для оптимизации (получение автора твита без дополнительных запросов)

## Слой валидации

### Архитектура валидации

Валидация в сервисе выполняется на двух уровнях:

1. **Bean Validation** (Jakarta Validation) - валидация на уровне DTO
2. **Business Rule Validation** - валидация бизнес-правил через `GenerateUsersAndTweetsValidator`

### GenerateUsersAndTweetsValidator

Валидатор для проверки бизнес-правил выполнения скрипта.

#### Методы валидатора:

1. **`validateDeletionCount(BaseScriptRequestDto requestDto, int usersWithTweetsCount)`**
   - Проверяет, что `lUsersForDeletion` не превышает количество пользователей с твитами
   - Выбрасывает `BusinessRuleValidationException` при нарушении правила
   - Обрабатывает случай `lUsersForDeletion = 0` (валидация проходит)

### Типы исключений валидации

#### 1. Bean Validation Errors

Используются для валидации параметров запроса на уровне DTO.

**HTTP статус:** `400 Bad Request`  
**Content-Type:** `application/problem+json`

**Пример ответа:**
```json
{
  "type": "https://example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed: nUsers: Number of users must be at least 1",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

#### 2. BusinessRuleValidationException

Используется для валидации бизнес-правил (например, превышение количества пользователей для удаления).

**HTTP статус:** `409 Conflict` (обрабатывается GlobalExceptionHandler)  
**Content-Type:** `application/problem+json`

**Пример ответа:**
```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 409,
  "detail": "Business rule 'DELETION_COUNT_EXCEEDS_USERS_WITH_TWEETS' violated for context: Cannot delete tweets from 5 users: only 3 users have tweets",
  "ruleName": "DELETION_COUNT_EXCEEDS_USERS_WITH_TWEETS",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

**Примечание:** В текущей реализации `BusinessRuleValidationException` обрабатывается в сервисе и добавляется в `statistics.errors`, поэтому возвращается `200 OK` с ошибками в статистике.

### Валидация по операциям

#### Выполнение скрипта (POST /generate-users-and-tweets)

**Bean Validation:**
- `nUsers` - обязательное поле, @NotNull, @Min(1), @Max(1000)
- `nTweetsPerUser` - обязательное поле, @NotNull, @Min(1), @Max(100)
- `lUsersForDeletion` - обязательное поле, @NotNull, @Min(0)

**Business Rule Validation:**
- `lUsersForDeletion` <= количество пользователей с твитами (после создания твитов)

## Интеграция

### Архитектура интеграции

Admin Script API интегрируется с тремя другими микросервисами через Feign Clients:
- **users-api** (порт 8081) - для создания пользователей
- **follower-api** (порт 8084) - для создания follow-отношений
- **tweet-api** (порт 8082) - для создания, получения и удаления твитов

### Компоненты интеграции

#### 1. UsersApiClient

Feign клиент для интеграции с users-api.

**Конфигурация:**
- URL: `${app.users-api.base-url:http://localhost:8081}`
- Path: `/api/v1/users`

**Методы:**
- `createUser(UserRequestDto userRequest) -> UserResponseDto` - создание пользователя

**Использование:**
- Вызывается через `UsersGateway` для обработки ошибок и логирования

#### 2. FollowApiClient

Feign клиент для интеграции с follower-api.

**Конфигурация:**
- URL: `${app.follower-api.base-url:http://localhost:8084}`
- Path: `/api/v1/follows`

**Методы:**
- `createFollow(FollowRequestDto followRequest) -> FollowResponseDto` - создание follow-отношения

**Использование:**
- Вызывается через `FollowGateway` для обработки ошибок и логирования

#### 3. TweetsApiClient

Feign клиент для интеграции с tweet-api.

**Конфигурация:**
- URL: `${app.tweet-api.base-url:http://localhost:8082}`
- Path: `/api/v1/tweets`

**Методы:**
- `createTweet(CreateTweetRequestDto createTweetRequest) -> TweetResponseDto` - создание твита
- `deleteTweet(UUID tweetId, DeleteTweetRequestDto deleteTweetRequest) -> void` - удаление твита
- `getUserTweets(UUID userId, Pageable pageable) -> Page<TweetResponseDto>` - получение твитов пользователя
- `likeTweet(UUID tweetId, LikeTweetRequestDto likeTweetRequest) -> LikeResponseDto` - создание лайка
- `retweetTweet(UUID tweetId, RetweetRequestDto retweetRequest) -> RetweetResponseDto` - создание ретвита

**Использование:**
- Вызывается через `TweetsGateway` для обработки ошибок и логирования

#### 4. UsersGateway

Gateway для обёртки вызовов `UsersApiClient` с обработкой ошибок.

**Функциональность:**
- Валидация входных параметров (null checks)
- Обработка исключений через try-catch с логированием
- Пробрасывание исключений дальше для обработки в Service слое

#### 5. FollowGateway

Gateway для обёртки вызовов `FollowApiClient` с обработкой ошибок.

**Функциональность:**
- Валидация входных параметров (null checks)
- Обработка исключений через try-catch с логированием
- Пробрасывание исключений дальше для обработки в Service слое

#### 6. TweetsGateway

Gateway для обёртки вызовов `TweetsApiClient` с обработкой ошибок.

**Функциональность:**
- Валидация входных параметров (null checks)
- Обработка исключений через try-catch с логированием
- Пробрасывание исключений дальше для обработки в Service слое

**Методы:**
- `createTweet(CreateTweetRequestDto createTweetRequest) -> TweetResponseDto` - создание твита
- `deleteTweet(UUID tweetId, DeleteTweetRequestDto deleteTweetRequest) -> void` - удаление твита
- `getUserTweets(UUID userId, Pageable pageable) -> Page<TweetResponseDto>` - получение твитов пользователя
- `likeTweet(UUID tweetId, LikeTweetRequestDto likeTweetRequest) -> LikeResponseDto` - создание лайка
- `retweetTweet(UUID tweetId, RetweetRequestDto retweetRequest) -> RetweetResponseDto` - создание ретвита

**Обработка ошибок:**
- Все исключения логируются с детальной информацией
- Исключения пробрасываются как RuntimeException для обработки в Service слое
- HTTP 409 ошибки (self-like, self-retweet, дубликаты) должны обрабатываться gracefully в Service слое

### Процесс выполнения скрипта

1. **Создание пользователей:**
   - Для каждого пользователя генерируются рандомные данные через `RandomDataGenerator`
   - Вызывается `UsersGateway.createUser()` для создания пользователя в users-api
   - Ошибки обрабатываются gracefully (логируются и добавляются в errors)

2. **Создание follow-отношений:**
   - Выбирается центральный пользователь (первый созданный пользователь)
   - Вычисляется половина остальных пользователей (целочисленное деление)
   - Центральный пользователь фолловит половину остальных через `FollowGateway.createFollow()`
   - Половина остальных пользователей фолловят центрального через `FollowGateway.createFollow()`
   - Ошибки обрабатываются gracefully (логируются и добавляются в errors)

3. **Создание твитов:**
   - Для каждого успешно созданного пользователя создаётся nTweetsPerUser твитов
   - Генерируется рандомный контент через `RandomDataGenerator.generateTweetContent()`
   - Вызывается `TweetsGateway.createTweet()` для создания твита в tweet-api
   - Ошибки обрабатываются gracefully

4. **Получение твитов пользователей:**
   - Для каждого пользователя вызывается `TweetsGateway.getUserTweets()` для подсчёта твитов
   - Используется для определения usersWithTweets и usersWithoutTweets

5. **Удаление твитов:**
   - Выбираются l случайных пользователей с твитами
   - Для каждого пользователя получаются твиты через `TweetsGateway.getUserTweets()`
   - Выбирается случайный твит и удаляется через `TweetsGateway.deleteTweet()`
   - Ошибки обрабатываются gracefully

6. **Создание лайков (шаги 6-8):**
   - Выбирается случайный твит из созданных (разные твиты для каждой операции)
   - Получается автор твита из кэша TweetResponseDto
   - Выбираются пользователи случайно, исключая автора твита
   - Создаются лайки через `TweetsGateway.likeTweet()` для выбранных пользователей
   - Ошибки (self-like, дубликаты) обрабатываются gracefully

7. **Создание ретвитов (шаги 9-11):**
   - Выбирается случайный твит из созданных (разные твиты для каждой операции, не использованные в шагах 6-8)
   - Получается автор твита из кэша TweetResponseDto
   - Выбираются пользователи случайно, исключая автора твита
   - Создаются ретвиты через `TweetsGateway.retweetTweet()` для выбранных пользователей (comment = null)
   - Ошибки (self-retweet, дубликаты) обрабатываются gracefully

### Обработка ошибок

- **Частичные ошибки:** Ошибки при создании/удалении отдельных сущностей обрабатываются gracefully, логируются и добавляются в `statistics.errors`
- **Критические ошибки:** Ошибки валидации параметров возвращают `400 Bad Request` через GlobalExceptionHandler
- **Ошибки внешних сервисов:** Ошибки users-api, follower-api и tweet-api обрабатываются в Gateway слое и пробрасываются дальше для обработки в Service

## Примеры использования

### Выполнение скрипта с успешным результатом

```bash
curl -X POST http://localhost:8083/api/v1/admin-scripts/generate-users-and-tweets \
  -H "Content-Type: application/json" \
  -d '{
    "nUsers": 5,
    "nTweetsPerUser": 3,
    "lUsersForDeletion": 2
  }'
```

**Ответ (200 OK):**

```json
{
  "createdUsers": [
    "123e4567-e89b-12d3-a456-426614174000",
    "223e4567-e89b-12d3-a456-426614174001",
    "323e4567-e89b-12d3-a456-426614174002",
    "423e4567-e89b-12d3-a456-426614174003",
    "523e4567-e89b-12d3-a456-426614174004"
  ],
  "createdFollows": [
    "623e4567-e89b-12d3-a456-426614174010",
    "723e4567-e89b-12d3-a456-426614174011"
  ],
  "createdTweets": [
    "623e4567-e89b-12d3-a456-426614174005",
    "723e4567-e89b-12d3-a456-426614174006",
    "823e4567-e89b-12d3-a456-426614174007"
  ],
  "deletedTweets": [
    "623e4567-e89b-12d3-a456-426614174005",
    "723e4567-e89b-12d3-a456-426614174006"
  ],
  "statistics": {
    "totalUsersCreated": 5,
    "totalFollowsCreated": 2,
    "totalTweetsCreated": 15,
    "totalTweetsDeleted": 2,
    "usersWithTweets": 5,
    "usersWithoutTweets": 0,
    "totalLikesCreated": 8,
    "totalRetweetsCreated": 6,
    "executionTimeMs": 2345,
    "errors": []
  }
}
```

### Выполнение скрипта с ошибкой валидации

```bash
curl -X POST http://localhost:8083/api/v1/admin-scripts/generate-users-and-tweets \
  -H "Content-Type: application/json" \
  -d '{
    "nUsers": 0,
    "nTweetsPerUser": 5,
    "lUsersForDeletion": 0
  }'
```

**Ответ (400 Bad Request):**

```json
{
  "type": "https://example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed: nUsers: Number of users must be at least 1",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

### Выполнение скрипта с частичными ошибками

```bash
curl -X POST http://localhost:8083/api/v1/admin-scripts/generate-users-and-tweets \
  -H "Content-Type: application/json" \
  -d '{
    "nUsers": 2,
    "nTweetsPerUser": 3,
    "lUsersForDeletion": 5
  }'
```

**Ответ (200 OK с ошибками в статистике):**

```json
{
  "createdUsers": [
    "123e4567-e89b-12d3-a456-426614174000",
    "223e4567-e89b-12d3-a456-426614174001"
  ],
  "createdTweets": [
    "323e4567-e89b-12d3-a456-426614174002",
    "423e4567-e89b-12d3-a456-426614174003"
  ],
  "deletedTweets": [],
  "statistics": {
    "totalUsersCreated": 2,
    "totalTweetsCreated": 6,
    "totalTweetsDeleted": 0,
    "usersWithTweets": 2,
    "usersWithoutTweets": 0,
    "executionTimeMs": 856,
    "errors": [
      "Validation failed: Business rule 'DELETION_COUNT_EXCEEDS_USERS_WITH_TWEETS' violated for context: Cannot delete tweets from 5 users: only 2 users have tweets"
    ]
  }
}
```

## Конфигурация

### Зависимости

Основные зависимости проекта:

- **Spring Boot 3.x** - основной фреймворк
- **Spring Web** - REST API
- **Spring Cloud OpenFeign** - интеграция с другими сервисами
- **SpringDoc OpenAPI** - документация API и Swagger UI
- **Datafaker 2.1.0** - генерация рандомных данных
- **Lombok** - генерация кода
- **Jakarta Validation** - валидация данных
- **Testcontainers** - интеграционные тесты
- **WireMock** - мокирование внешних сервисов в тестах

### Управление зависимостями

Сервис использует **централизованное управление версиями** через `dependencyManagement` в корневом `build.gradle`.

**Важно**: При добавлении новых зависимостей в `build.gradle` сервиса **НЕ указывайте версии** - они автоматически резолвятся через `dependencyManagement`.

### Конфигурация приложения

Основные параметры конфигурации в `application.yml`:

```yaml
server:
  port: 8083

spring:
  application:
    name: admin-script-api

app:
  users-api:
    base-url: http://localhost:8081
  follower-api:
    base-url: http://localhost:8084
  tweet-api:
    base-url: http://localhost:8082

feign:
  client:
    config:
      default:
        connect-timeout: 2000
        read-timeout: 5000
        logger-level: basic
  httpclient:
    enabled: true
```

## Запуск и развертывание

### Локальный запуск

1. Убедитесь, что PostgreSQL запущен на порту 5432
2. Убедитесь, что users-api запущен на порту 8081
3. Убедитесь, что follower-api запущен на порту 8084
4. Убедитесь, что tweet-api запущен на порту 8082
5. Создайте базу данных `twitter` (если требуется)
6. Запустите приложение:

```bash
./gradlew :services:admin-script-api:bootRun
```

Или из корневой директории проекта:

```bash
./gradlew bootRun --args='--spring.profiles.active=default'
```

### Docker

```bash
docker build -t admin-script-api .
docker run -p 8083:8083 admin-script-api
```

### Мониторинг

Приложение предоставляет следующие эндпоинты мониторинга:

- `/actuator/health` - состояние здоровья
- `/actuator/info` - информация о приложении
- `/actuator/metrics` - метрики приложения
- `/actuator/tracing` - трассировка запросов
- `/swagger-ui.html` - интерактивная документация API

## Безопасность

### Валидация

- Все входящие данные валидируются через Jakarta Validation
- Кастомная валидация для бизнес-правил через `GenerateUsersAndTweetsValidator`
- Валидация параметров скрипта (nUsers, nTweetsPerUser, lUsersForDeletion)

### Логирование

- Все запросы логируются через `@LoggableRequest`
- Подробное логирование всех операций скрипта
- Логирование ошибок с уровнем ERROR

### Обработка ошибок

- Частичные ошибки обрабатываются gracefully
- Критические ошибки возвращаются через GlobalExceptionHandler
- Все ошибки документируются в статистике выполнения

## Тестирование

Проект включает:

- **Unit тесты** для всех компонентов:
  - `RandomDataGeneratorTest` - тесты генератора данных
  - `GenerateUsersAndTweetsValidatorImplTest` - тесты валидатора
  - `GenerateUsersAndTweetsServiceImplTest` - тесты сервиса
- **Integration тесты** с MockMvc и WireMock:
  - `GenerateUsersAndTweetsControllerTest` - тесты контроллера с полным Spring контекстом

Запуск тестов:

```bash
./gradlew :services:admin-script-api:test
```

### Покрытие тестами

- `RandomDataGeneratorTest` - тесты всех методов генерации данных, проверка уникальности и ограничений
- `GenerateUsersAndTweetsValidatorImplTest` - тесты валидации параметров удаления
- `GenerateUsersAndTweetsServiceImplTest` - тесты полного цикла выполнения скрипта
- `GenerateUsersAndTweetsControllerTest` - тесты REST эндпоинта с мокированием внешних сервисов

### Использование Datafaker

Сервис использует библиотеку **Datafaker 2.1.0** для генерации реалистичных тестовых данных:

- **Генерация login:** `name().firstName()` + `name().lastName()` + timestamp/UUID
- **Генерация email:** `internet().emailAddress()` + timestamp
- **Генерация имени/фамилии:** `name().firstName()` и `name().lastName()`
- **Генерация пароля:** комбинация name и number генераторов (8-20 символов)
- **Генерация контента твита:** `lorem().sentence()` или `lorem().paragraph()` (1-280 символов)

Все данные генерируются с соблюдением ограничений DTO и обеспечивают уникальность через timestamp/UUID где необходимо.
