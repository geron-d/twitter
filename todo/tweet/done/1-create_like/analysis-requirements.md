# Анализ требований: Эндпоинт "Лайкнуть твит"

## Meta
- **Дата создания**: 2025-01-27
- **Эндпоинт**: `POST /api/v1/tweets/{tweetId}/like`
- **HTTP метод**: POST
- **Приоритет**: P1

## 1. Входные данные

### 1.1 Path параметры
- **tweetId** (UUID, обязательный)
  - Уникальный идентификатор твита, который нужно лайкнуть
  - Формат: UUID
  - Валидация: должен быть валидным UUID, твит должен существовать и не быть удаленным

### 1.2 Request Body
- **LikeTweetRequestDto** (JSON, обязательный)
  - **userId** (UUID, обязательный)
    - Уникальный идентификатор пользователя, который лайкает твит
    - Формат: UUID
    - Валидация: должен быть валидным UUID, пользователь должен существовать в системе

**Пример запроса:**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}
```

## 2. Выходные данные

### 2.1 Успешный ответ (201 Created)
- **LikeResponseDto** (JSON)
  - **id** (UUID) - уникальный идентификатор созданного лайка
  - **tweetId** (UUID) - идентификатор твита
  - **userId** (UUID) - идентификатор пользователя, который лайкнул
  - **createdAt** (LocalDateTime) - время создания лайка

**Пример успешного ответа:**
```json
{
  "id": "987e6543-e21b-43d2-b654-321987654321",
  "tweetId": "223e4567-e89b-12d3-a456-426614174001",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "createdAt": "2025-01-27T15:30:00Z"
}
```

### 2.2 Ошибки
- **400 Bad Request**: Невалидный UUID, некорректные данные в request body
- **404 Not Found**: Твит не найден или удален
- **409 Conflict**: Лайк уже существует (дублирование), попытка самолайка
- **500 Internal Server Error**: Внутренняя ошибка сервера

## 3. Бизнес-правила

### 3.1 Основные правила
1. **Уникальность лайка**
   - Один пользователь может лайкнуть твит только один раз
   - Обеспечивается уникальным ограничением в БД на паре (tweetId, userId)
   - При попытке повторного лайка возвращается ошибка 409 Conflict

2. **Запрет самолайка**
   - Пользователь не может лайкнуть свой собственный твит
   - Проверка: userId из request body != userId твита
   - При попытке самолайка возвращается ошибка 409 Conflict

3. **Существование твита**
   - Твит должен существовать в системе
   - Твит не должен быть удаленным (isDeleted = false)
   - При отсутствии твита возвращается ошибка 404 Not Found

4. **Существование пользователя**
   - Пользователь должен существовать в системе
   - Проверка через интеграцию с users-api (UserGateway)
   - При отсутствии пользователя возвращается ошибка 400 Bad Request

5. **Атомарность операции**
   - Операция должна быть транзакционной (@Transactional)
   - Уровень изоляции: SERIALIZABLE (для предотвращения дублирования)
   - При ошибке все изменения откатываются

6. **Обновление счетчика**
   - При создании лайка обновляется счетчик `likesCount` в таблице `tweets`
   - Счетчик увеличивается на 1
   - Обновление выполняется синхронно в той же транзакции

### 3.2 Правила валидации
- **Валидация tweetId**: должен быть валидным UUID, не null
- **Валидация userId**: должен быть валидным UUID, не null
- **Валидация существования твита**: твит должен существовать и не быть удаленным
- **Валидация существования пользователя**: пользователь должен существовать (через users-api)
- **Валидация самолайка**: userId из request != userId твита
- **Валидация дублирования**: лайк с такой парой (tweetId, userId) не должен существовать

### 3.3 Правила производительности
- Использование уникального ограничения в БД для предотвращения дублирования
- Транзакция с уровнем изоляции SERIALIZABLE для предотвращения race conditions
- Денормализация счетчика likesCount для быстрых запросов статистики

## 4. Затронутые стандарты

### 4.1 STANDART_CODE.md
- ✅ Использование Records для DTO (LikeTweetRequestDto, LikeResponseDto)
- ✅ Слоистая архитектура: Controller → Service → Repository
- ✅ Использование MapStruct для маппинга Entity ↔ DTO
- ✅ Bean Validation для валидации входных данных (@NotNull для userId)
- ✅ JavaDoc для всех публичных методов
- ✅ Использование @LoggableRequest на контроллере
- ✅ Использование @RequiredArgsConstructor, @Slf4j
- ✅ UUID для идентификаторов
- ✅ @CreationTimestamp для createdAt

### 4.2 STANDART_PROJECT.md
- ✅ Использование @LoggableRequest для логирования запросов
- ✅ Обработка ошибок через GlobalExceptionHandler
- ✅ Использование ValidationException иерархии (BusinessRuleValidationException, FormatValidationException)
- ✅ Размещение shared DTOs в common-lib (если используются несколькими сервисами)

### 4.3 STANDART_TEST.md
- ✅ Unit тесты для Service, Validator, Mapper
- ✅ Integration тесты для Controller
- ✅ Именование: `methodName_WhenCondition_ShouldExpectedResult`
- ✅ Использование @Nested для группировки тестов
- ✅ Использование AssertJ для assertions
- ✅ Паттерн AAA (Arrange-Act-Assert)

### 4.4 STANDART_JAVADOC.md
- ✅ JavaDoc для всех публичных методов с @author geron, @version 1.0
- ✅ @param, @return, @throws для всех методов
- ✅ Использование @see для ссылок на интерфейсы
- ✅ JavaDoc для DTO Records с @param для всех компонентов

### 4.5 STANDART_SWAGGER.md
- ✅ OpenAPI аннотации в TweetApi интерфейсе
- ✅ @Operation, @ApiResponses, @ExampleObject для документации
- ✅ @Schema аннотации на DTO
- ✅ RFC 7807 Problem Details для ошибок

### 4.6 STANDART_README.md
- ✅ Обновление README на русском языке
- ✅ Добавление эндпоинта в таблицу REST API
- ✅ Детальное описание эндпоинта с примерами
- ✅ Обновление раздела "Примеры использования"

### 4.7 STANDART_POSTMAN.md
- ✅ Добавление запроса "like tweet" в коллекцию
- ✅ Примеры ответов для всех сценариев (201, 400, 404, 409)
- ✅ Использование переменных {{baseUrl}}, {{tweetId}}, {{userId}}

## 5. Компоненты для реализации

### 5.1 Новые компоненты
1. **Entity: Like**
   - Поля: id (UUID), tweetId (UUID), userId (UUID), createdAt (LocalDateTime)
   - Уникальное ограничение на паре (tweetId, userId)
   - Индексы для производительности

2. **Repository: LikeRepository**
   - Методы: findByTweetIdAndUserId, existsByTweetIdAndUserId
   - Derived Query Methods (без JavaDoc)

3. **DTO: LikeTweetRequestDto**
   - Record с полем userId (UUID, @NotNull)
   - Bean Validation аннотации
   - @Schema аннотации для Swagger

4. **DTO: LikeResponseDto**
   - Record с полями: id, tweetId, userId, createdAt
   - @Schema аннотации для Swagger

5. **Mapper: методы для лайков**
   - toLike(LikeTweetRequestDto, UUID tweetId) → Like
   - toLikeResponseDto(Like) → LikeResponseDto
   - В TweetMapper или новый LikeMapper

6. **Validator: методы валидации лайка**
   - validateForLike(UUID tweetId, LikeTweetRequestDto requestDto)
   - Проверки: существование твита, пользователя, самолайк, дублирование

7. **Service: метод likeTweet**
   - likeTweet(UUID tweetId, LikeTweetRequestDto requestDto) → LikeResponseDto
   - @Transactional с уровнем изоляции SERIALIZABLE
   - Вызов валидации, создание лайка, обновление счетчика

8. **Controller: метод likeTweet**
   - POST /api/v1/tweets/{tweetId}/like
   - @LoggableRequest
   - OpenAPI аннотации в TweetApi

### 5.2 Обновляемые компоненты
1. **Entity: Tweet**
   - Добавить поле likesCount (Integer, default 0)
   - Добавить метод incrementLikesCount()

2. **Repository: TweetRepository**
   - Возможно, добавить метод для обновления счетчика (или использовать Entity метод)

## 6. Зависимости

### 6.1 Внутренние зависимости
- **TweetRepository**: для проверки существования твита
- **UserGateway**: для проверки существования пользователя (интеграция с users-api)
- **TweetMapper**: для маппинга Entity ↔ DTO

### 6.2 Внешние зависимости
- **users-api**: проверка существования пользователя (через UserGateway)
- **PostgreSQL**: хранение данных (таблицы tweets, tweet_likes)

## 7. HTTP статус-коды

| Статус | Описание | Условие |
|--------|----------|---------|
| 201 Created | Лайк успешно создан | Все валидации пройдены, лайк создан |
| 400 Bad Request | Ошибка валидации | Невалидный UUID, некорректные данные |
| 404 Not Found | Твит не найден | Твит не существует или удален |
| 409 Conflict | Конфликт | Лайк уже существует, попытка самолайка |
| 500 Internal Server Error | Внутренняя ошибка | Неожиданная ошибка сервера |

## 8. Примеры запросов и ответов

### 8.1 Успешный лайк (201 Created)
**Запрос:**
```http
POST /api/v1/tweets/223e4567-e89b-12d3-a456-426614174001/like
Content-Type: application/json

{
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Ответ:**
```json
{
  "id": "987e6543-e21b-43d2-b654-321987654321",
  "tweetId": "223e4567-e89b-12d3-a456-426614174001",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "createdAt": "2025-01-27T15:30:00Z"
}
```

### 8.2 Ошибка: Твит не найден (404 Not Found)
**Ответ:**
```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 404,
  "detail": "Business rule 'TWEET_NOT_FOUND' violated for context: 223e4567-e89b-12d3-a456-426614174001",
  "ruleName": "TWEET_NOT_FOUND",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

### 8.3 Ошибка: Лайк уже существует (409 Conflict)
**Ответ:**
```json
{
  "type": "https://example.com/errors/uniqueness-validation",
  "title": "Uniqueness Validation Error",
  "status": 409,
  "detail": "Like already exists for tweet 223e4567-e89b-12d3-a456-426614174001 and user 123e4567-e89b-12d3-a456-426614174000",
  "fieldName": "like",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

### 8.4 Ошибка: Попытка самолайка (409 Conflict)
**Ответ:**
```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 409,
  "detail": "Business rule 'SELF_LIKE_NOT_ALLOWED' violated for context: User cannot like their own tweet",
  "ruleName": "SELF_LIKE_NOT_ALLOWED",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

## 9. Предположения

1. Пользователь идентифицируется через `userId` в request body (не через аутентификацию)
2. Твит должен существовать и не быть удаленным (soft delete)
3. Уникальность лайка обеспечивается на уровне БД (UNIQUE constraint на tweetId+userId)
4. Счетчик `likesCount` обновляется синхронно при создании лайка
5. При попытке повторного лайка возвращается ошибка 409 Conflict
6. Интеграция с users-api доступна через UserGateway (уже реализована)

## 10. Риски и митигация

### 10.1 Технические риски
- **Race condition при одновременных лайках**
  - Митигация: уникальное ограничение в БД, транзакция с уровнем изоляции SERIALIZABLE
  - Мониторинг: логирование конфликтов

- **Производительность при обновлении счетчика**
  - Митигация: денормализация для быстрых запросов, обновление в той же транзакции
  - Мониторинг: время выполнения запросов

### 10.2 Зависимости
- **Интеграция с users-api**
  - Митигация: Circuit Breaker, fallback стратегии (уже реализовано в UserGateway)
  - Мониторинг: доступность users-api, время ответа

## 11. Критерии успеха

- ✅ Все тесты проходят (unit и integration)
- ✅ Покрытие кода > 80% для новых компонентов
- ✅ Эндпоинт возвращает правильные HTTP статус-коды (201, 400, 404, 409)
- ✅ Валидация работает корректно (самолайк, дублирование, несуществующий твит)
- ✅ OpenAPI документация полная и корректная
- ✅ README обновлен с примерами использования
- ✅ Postman коллекция обновлена с примерами

## 12. Следующие шаги

1. Проектирование API и контрактов (#2)
2. Реализация Entity Like (#3)
3. Реализация LikeRepository (#4)
4. Реализация DTO (#5)

