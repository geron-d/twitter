# Проектирование API и контрактов: Эндпоинт получения ленты новостей

## Meta
- **Шаг**: #2
- **Дата**: 2025-01-27
- **Статус**: Завершено
- **Связанные задачи**: TODO.md #2
- **Предыдущий шаг**: analysis-requirements.md (#1)

## 1. OpenAPI схема для эндпоинта

### 1.1 Метод и путь
- **HTTP метод**: GET
- **Путь**: `/api/v1/tweets/timeline/{userId}`
- **Полный путь**: `GET /api/v1/tweets/timeline/{userId}?page=0&size=20&sort=createdAt,DESC`

### 1.2 OpenAPI аннотации для TweetApi интерфейса

```java
/**
 * Retrieves a paginated timeline (news feed) of tweets for a specific user.
 * <p>
 * This endpoint retrieves tweets from all users that the specified user is following.
 * The timeline includes tweets from all followed users, sorted by creation date in
 * descending order (newest first). Deleted tweets (soft delete) are automatically
 * excluded from the results. Supports pagination with page, size, and sort parameters.
 * If the user has no following relationships, an empty page is returned (not an error).
 * Integration with follower-api is used to retrieve the list of followed users.
 *
 * @param userId   the unique identifier of the user whose timeline to retrieve
 * @param pageable pagination parameters (page, size, sorting)
 * @return PagedModel containing paginated list of tweets with metadata and HATEOAS links
 */
@Operation(
    summary = "Get user timeline with pagination",
    description = "Retrieves a paginated timeline (news feed) of tweets for a specific user. " +
        "The timeline includes tweets from all users that the specified user is following. " +
        "Tweets are sorted by creation date in descending order (newest first). " +
        "Deleted tweets (soft delete) are excluded from the results. " +
        "Supports pagination with page, size, and sort parameters. " +
        "If the user has no following relationships, an empty page is returned (not an error). " +
        "Integration with follower-api is used to retrieve the list of followed users. " +
        "Default pagination: page=0, size=20, sort=createdAt,DESC."
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Timeline retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = PagedModel.class),
            examples = {
                @ExampleObject(
                    name = "Timeline with Tweets",
                    summary = "Example timeline response with tweets from followed users",
                    value = """
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
                        """
                ),
                @ExampleObject(
                    name = "Empty Timeline",
                    summary = "Example response when user has no following relationships",
                    value = """
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
                        """
                ),
                @ExampleObject(
                    name = "Empty Timeline - No Tweets",
                    summary = "Example response when followed users have no tweets",
                    value = """
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
                        """
                )
            }
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid UUID format for userId",
        content = @Content(
            mediaType = "application/problem+json",
            examples = @ExampleObject(
                name = "Invalid UUID Format Error",
                summary = "Invalid user ID format",
                value = """
                    {
                      "type": "https://example.com/errors/validation-error",
                      "title": "Validation Error",
                      "status": 400,
                      "detail": "Invalid UUID format for userId parameter",
                      "timestamp": "2025-01-27T15:30:00Z"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "User does not exist",
        content = @Content(
            mediaType = "application/problem+json",
            examples = @ExampleObject(
                name = "User Not Found Error",
                summary = "User does not exist",
                value = """
                    {
                      "type": "https://example.com/errors/business-rule-validation",
                      "title": "Business Rule Validation Error",
                      "status": 400,
                      "detail": "Business rule 'USER_NOT_EXISTS' violated for context: 123e4567-e89b-12d3-a456-426614174000",
                      "ruleName": "USER_NOT_EXISTS",
                      "context": "123e4567-e89b-12d3-a456-426614174000",
                      "timestamp": "2025-01-27T15:30:00Z"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid pagination parameters",
        content = @Content(
            mediaType = "application/problem+json",
            examples = @ExampleObject(
                name = "Invalid Pagination Error",
                summary = "Invalid page or size parameters",
                value = """
                    {
                      "type": "https://example.com/errors/validation-error",
                      "title": "Validation Error",
                      "status": 400,
                      "detail": "Invalid pagination parameters: page must be >= 0, size must be between 1 and 100",
                      "timestamp": "2025-01-27T15:30:00Z"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "503",
        description = "Follower API service unavailable",
        content = @Content(
            mediaType = "application/problem+json",
            examples = @ExampleObject(
                name = "Service Unavailable Error",
                summary = "Follower API is unavailable",
                value = """
                    {
                      "type": "https://example.com/errors/service-unavailable",
                      "title": "Service Unavailable",
                      "status": 503,
                      "detail": "Follower API service is currently unavailable. Please try again later.",
                      "timestamp": "2025-01-27T15:30:00Z"
                    }
                    """
            )
        )
    )
})
PagedModel<TweetResponseDto> getTimeline(
    @Parameter(
        description = "Unique identifier of the user whose timeline to retrieve",
        required = true,
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID userId,
    @Parameter(description = "Pagination parameters (page, size, sorting)", required = false)
    Pageable pageable);
```

### 1.3 Реализация в TweetController

```java
/**
 * @see TweetApi#getTimeline
 */
@LoggableRequest
@GetMapping("/timeline/{userId}")
@Override
public PagedModel<TweetResponseDto> getTimeline(
    @PathVariable("userId") UUID userId,
    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<TweetResponseDto> timeline = tweetService.getTimeline(userId, pageable);
    return new PagedModel<>(timeline);
}
```

## 2. Определение структуры ответа

### 2.1 Использование существующего TweetResponseDto

**Решение**: Использовать существующий `TweetResponseDto` из `shared/common-lib`.

**Обоснование**:
- TweetResponseDto уже содержит все необходимые поля для отображения твита
- Структура ответа идентична эндпоинту `getUserTweets`
- Нет необходимости создавать специфичный DTO для timeline
- Соответствует принципу переиспользования компонентов

**Структура TweetResponseDto**:
```java
public record TweetResponseDto(
    UUID id,                    // ID твита
    UUID userId,                // ID пользователя, создавшего твит
    String content,             // Содержимое твита (1-280 символов)
    LocalDateTime createdAt,   // Дата создания
    LocalDateTime updatedAt,    // Дата обновления
    Boolean isDeleted,          // Флаг удаления
    LocalDateTime deletedAt     // Дата удаления (nullable)
)
```

### 2.2 Структура пагинированного ответа

**Тип возвращаемого значения**: `PagedModel<TweetResponseDto>`

**Структура ответа**:
```json
{
  "content": [
    {
      "id": "uuid",
      "userId": "uuid",
      "content": "string",
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
    "totalPages": 8,
    "first": true,
    "last": false
  },
  "_links": {
    "self": { "href": "/api/v1/tweets/timeline/{userId}?page=0&size=20" },
    "next": { "href": "/api/v1/tweets/timeline/{userId}?page=1&size=20" },
    "prev": { "href": null },
    "first": { "href": "/api/v1/tweets/timeline/{userId}?page=0&size=20" },
    "last": { "href": "/api/v1/tweets/timeline/{userId}?page=7&size=20" }
  }
}
```

### 2.3 Обработка пустых результатов

**Сценарий 1**: Пользователь не имеет подписок
- **HTTP статус**: 200 OK
- **Ответ**: Пустая страница с `totalElements: 0`, `totalPages: 0`, `content: []`
- **Это не ошибка**, а нормальное состояние

**Сценарий 2**: Подписанные пользователи не имеют твитов
- **HTTP статус**: 200 OK
- **Ответ**: Пустая страница с `totalElements: 0`, `totalPages: 0`, `content: []`
- **Это не ошибка**, а нормальное состояние

## 3. Определение контракта с follower-api

### 3.1 Эндпоинт follower-api

**Метод**: GET  
**Путь**: `/api/v1/follows/{userId}/following`  
**Базовый URL**: 
- Локально: `http://localhost:8084`
- Docker: `http://follower-api:8084`

### 3.2 Структура запроса

**Path параметры**:
- `userId` (UUID, обязательный) - идентификатор пользователя, для которого запрашивается список подписок

**Query параметры** (пагинация):
- `page` (Integer, опциональный, по умолчанию 0)
- `size` (Integer, опциональный, по умолчанию 10)
- `sort` (String, опциональный, по умолчанию "createdAt,DESC")

**Пример запроса**:
```
GET /api/v1/follows/123e4567-e89b-12d3-a456-426614174000/following?page=0&size=100
```

### 3.3 Структура ответа follower-api

**Тип ответа**: `PagedModel<FollowingResponseDto>`

**FollowingResponseDto**:
```java
public record FollowingResponseDto(
    UUID id,                    // followingId - идентификатор пользователя, на которого подписан
    String login,               // логин пользователя, на которого подписан
    LocalDateTime createdAt     // дата создания подписки
)
```

**Важно**: Поле `id` в `FollowingResponseDto` содержит `followingId` - идентификатор пользователя, на которого подписан запрашивающий пользователь.

**Пример ответа**:
```json
{
  "content": [
    {
      "id": "222e4567-e89b-12d3-a456-426614174111",
      "login": "jane_doe",
      "createdAt": "2025-01-20T15:30:00Z"
    },
    {
      "id": "333e4567-e89b-12d3-a456-426614174222",
      "login": "john_smith",
      "createdAt": "2025-01-19T10:20:00Z"
    }
  ],
  "page": {
    "size": 100,
    "number": 0,
    "totalElements": 50,
    "totalPages": 1
  }
}
```

### 3.4 Обработка ошибок follower-api

**200 OK**: Успешный ответ, обрабатываем список подписок

**404 Not Found**: Пользователь не найден в follower-api
- **Действие в tweet-api**: Возвращаем 400 Bad Request с сообщением "User does not exist"
- **Обоснование**: Если пользователь не найден в follower-api, он не существует в системе

**500/503**: Внутренняя ошибка follower-api или сервис недоступен
- **Действие в tweet-api**: 
  - Вариант 1: Возвращаем пустую страницу (graceful degradation)
  - Вариант 2: Возвращаем 503 Service Unavailable
- **Рекомендация**: Circuit Breaker с fallback на пустую страницу для graceful degradation

**Timeout**: Таймаут запроса к follower-api
- **Действие в tweet-api**: Возвращаем пустую страницу или 503 Service Unavailable
- **Рекомендация**: Circuit Breaker с fallback

### 3.5 Получение всех подписок

**Проблема**: follower-api возвращает пагинированные результаты, но для построения полной ленты нужно получить все подписки.

**Решение**:
1. Делать запросы с пагинацией до получения всех подписок
2. Использовать параметр `size=100` (или максимально допустимый) для минимизации количества запросов
3. Обрабатывать все страницы до получения полного списка

**Алгоритм**:
```java
List<UUID> getAllFollowingUserIds(UUID userId) {
    List<UUID> allFollowingIds = new ArrayList<>();
    int page = 0;
    int size = 100; // Максимальный размер страницы
    
    while (true) {
        PagedModel<FollowingResponseDto> pageResult = followerApiClient.getFollowing(userId, PageRequest.of(page, size));
        List<UUID> pageFollowingIds = pageResult.getContent().stream()
            .map(FollowingResponseDto::id)
            .toList();
        allFollowingIds.addAll(pageFollowingIds);
        
        if (pageResult.getPage().isLast()) {
            break;
        }
        page++;
    }
    
    return allFollowingIds;
}
```

**Ограничение**: Если у пользователя очень много подписок (например, 1000+), это может привести к множественным запросам. Для оптимизации можно:
- Кэшировать список подписок
- Использовать батчинг для получения твитов
- Ограничить максимальное количество подписок для обработки

## 4. Определение общих и специфичных компонентов

### 4.1 Общие компоненты (переиспользование)

#### 4.1.1 DTO
- **TweetResponseDto** - используется без изменений
  - Расположение: `shared/common-lib/src/main/java/com/twitter/common/dto/response/TweetResponseDto.java`
  - Используется в: `getUserTweets`, `getTimeline`, `getTweetById`, и других эндпоинтах

#### 4.1.2 Mapper
- **TweetMapper** - используется существующий метод `toResponseDto`
  - Расположение: `services/tweet-api/src/main/java/com/twitter/mapper/TweetMapper.java`
  - Метод: `TweetResponseDto toResponseDto(Tweet tweet)`
  - Новых методов маппинга не требуется

#### 4.1.3 Repository
- **TweetRepository** - будет добавлен новый метод (шаг #5)
  - Метод: `findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc(List<UUID> userIds, Pageable pageable)`
  - Использует Derived Query Method (без JavaDoc согласно стандартам)

#### 4.1.4 Gateway паттерн
- **UserGateway** - используется для валидации существования пользователя
  - Расположение: `services/tweet-api/src/main/java/com/twitter/gateway/UserGateway.java`
  - Метод: `boolean existsUser(UUID userId)`

#### 4.1.5 Validator
- **TweetValidator** - будет добавлен новый метод `validateForTimeline` (шаг #9)
  - Расположение: `services/tweet-api/src/main/java/com/twitter/validation/TweetValidator.java`
  - Использует `UserGateway.existsUser` для проверки существования пользователя

#### 4.1.6 Service
- **TweetService** - будет добавлен новый метод `getTimeline` (шаг #10)
  - Расположение: `services/tweet-api/src/main/java/com/twitter/service/TweetService.java`
  - Использует `@Transactional(readOnly = true)` как и `getUserTweets`

#### 4.1.7 Controller
- **TweetController** - будет добавлен новый метод `getTimeline` (шаг #11)
  - Расположение: `services/tweet-api/src/main/java/com/twitter/controller/TweetController.java`
  - Использует `@LoggableRequest` как и другие методы
  - Использует `@PageableDefault` для пагинации

### 4.2 Специфичные компоненты (новые)

#### 4.2.1 Feign Client
- **FollowerApiClient** - новый Feign клиент для интеграции с follower-api
  - Расположение: `services/tweet-api/src/main/java/com/twitter/client/FollowerApiClient.java`
  - Метод: `PagedModel<FollowingResponseDto> getFollowing(UUID userId, Pageable pageable)`
  - Конфигурация: `@FeignClient(name = "follower-api", url = "${app.follower-api.base-url}")`

#### 4.2.2 Gateway
- **FollowerGateway** - новый Gateway для абстракции работы с follower-api
  - Расположение: `services/tweet-api/src/main/java/com/twitter/gateway/FollowerGateway.java`
  - Метод: `List<UUID> getFollowingUserIds(UUID userId)`
  - Обработка ошибок с безопасными значениями (пустой список при ошибке)
  - Логирование операций

#### 4.2.3 Конфигурация
- **application.yml** - добавление настроек для follower-api
  - Секция: `app.follower-api.base-url: http://localhost:8084`

- **application-docker.yml** - добавление настроек для Docker окружения
  - Секция: `app.follower-api.base-url: http://follower-api:8084`

- **docker-compose.yml** - добавление зависимости tweet-api от follower-api
  - Зависимость: `depends_on: follower-api` с `condition: service_healthy`
  - Переменная окружения: `FOLLOWER_API_URL=http://follower-api:8084`

### 4.3 Сравнительная таблица компонентов

| Компонент | getUserTweets | getTimeline | Переиспользование |
|-----------|---------------|-------------|-------------------|
| **DTO** | TweetResponseDto | TweetResponseDto | ✅ Да |
| **Mapper** | TweetMapper.toResponseDto | TweetMapper.toResponseDto | ✅ Да |
| **Repository** | findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc | findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc | ⚠️ Новый метод |
| **Validator** | validateUserExists (через UserGateway) | validateForTimeline (через UserGateway) | ⚠️ Новый метод |
| **Service** | getUserTweets | getTimeline | ⚠️ Новый метод |
| **Controller** | getUserTweets | getTimeline | ⚠️ Новый метод |
| **Gateway** | UserGateway | FollowerGateway | ❌ Новый компонент |
| **Feign Client** | UsersApiClient | FollowerApiClient | ❌ Новый компонент |
| **Конфигурация** | app.users-api | app.follower-api | ⚠️ Новые настройки |

## 5. Выводы и рекомендации

### 5.1 Ключевые решения
1. ✅ Использовать существующий `TweetResponseDto` (не создавать новый)
2. ✅ Использовать существующий `TweetMapper.toResponseDto` (новых методов не требуется)
3. ✅ Следовать паттернам из `getUserTweets` для консистентности
4. ✅ Использовать Gateway паттерн для абстракции follower-api (аналогично UserGateway)
5. ✅ Обрабатывать пустые результаты как нормальное состояние (200 OK, не ошибка)

### 5.2 Архитектурные принципы
- **Переиспользование**: Максимальное использование существующих компонентов
- **Консистентность**: Следование паттернам существующих эндпоинтов
- **Абстракция**: Gateway паттерн для изоляции интеграции с внешними сервисами
- **Graceful degradation**: Возврат пустой страницы при ошибках follower-api

### 5.3 Следующие шаги
1. Реализация Feign клиента для follower-api (шаг #3)
2. Реализация Gateway для follower-api (шаг #4)
3. Реализация Repository метода (шаг #5)
4. Обновление конфигураций (шаги #6.1, #6.2, #6.3)

## 6. Ссылки

- [TODO.md](../TODO.md)
- [analysis-requirements.md](./analysis-requirements.md) - предыдущий шаг
- [TWEET_API_ARCHITECTURE.md](../TWEET_API_ARCHITECTURE.md)
- [STANDART_SWAGGER.md](../../../standards/STANDART_SWAGGER.md)
- [TweetApi.java](../../../services/tweet-api/src/main/java/com/twitter/controller/TweetApi.java)
- [TweetController.java](../../../services/tweet-api/src/main/java/com/twitter/controller/TweetController.java)
- [Follower API README](../../../services/follower-api/README.md)
