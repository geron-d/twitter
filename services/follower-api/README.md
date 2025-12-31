# Follower API Service

## Введение

**Follower API** — это микросервис для управления отношениями подписки (follow relationships) между пользователями в системе Twitter, построенный на Java 24 и Spring Boot 3. Сервис предоставляет REST API для создания и удаления подписок, получения списков подписчиков и подписок с пагинацией и фильтрацией, проверки статуса подписки и получения статистики подписок.

### Основные возможности:

- ✅ Создание и удаление отношений подписки между пользователями
- ✅ Получение списка подписчиков пользователя с пагинацией и фильтрацией
- ✅ Получение списка подписок пользователя с пагинацией и фильтрацией
- ✅ Проверка статуса подписки между двумя пользователями
- ✅ Получение статистики подписок (количество подписчиков и подписок)
- ✅ Интеграция с users-api для проверки существования пользователей и получения логинов
- ✅ Валидация данных и бизнес-правил (запрет самоподписки, проверка уникальности)
- ✅ OpenAPI/Swagger документация
- ✅ Обработка ошибок по стандарту RFC 7807 Problem Details
- ✅ Логирование запросов

## Архитектура

### Структура пакетов

```
com.twitter/
├── Application.java              # Главный класс приложения
├── client/
│   └── UsersApiClient.java       # Feign клиент для интеграции с users-api
├── config/
│   ├── FeignConfig.java          # Конфигурация Feign клиентов
│   └── OpenApiConfig.java        # Конфигурация OpenAPI/Swagger
├── controller/
│   ├── FollowApi.java            # OpenAPI интерфейс
│   └── FollowController.java     # REST контроллер
├── dto/
│   ├── filter/
│   │   ├── FollowerFilter.java   # Фильтр для поиска подписчиков
│   │   └── FollowingFilter.java  # Фильтр для поиска подписок
│   ├── request/
│   │   └── FollowRequestDto.java # DTO для создания подписки
│   └── response/
│       ├── FollowResponseDto.java        # DTO для ответа с данными подписки
│       ├── FollowerResponseDto.java      # DTO для информации о подписчике
│       ├── FollowingResponseDto.java     # DTO для информации о подписке
│       ├── FollowStatusResponseDto.java   # DTO для статуса подписки
│       └── FollowStatsResponseDto.java   # DTO для статистики подписок
├── entity/
│   └── Follow.java               # JPA сущность отношения подписки
├── gateway/
│   └── UserGateway.java         # Gateway для работы с users-api
├── mapper/
│   └── FollowMapper.java        # MapStruct маппер
├── repository/
│   └── FollowRepository.java    # JPA репозиторий
├── service/
│   ├── FollowService.java       # Интерфейс сервиса
│   └── FollowServiceImpl.java   # Реализация сервиса
└── validation/
    ├── FollowValidator.java     # Интерфейс валидатора
    └── FollowValidatorImpl.java  # Реализация валидатора
```

## REST API

### Базовый URL

```
http://localhost:8084/api/v1/follows
```

### Эндпоинты

| Метод | Путь | Описание | Параметры | Тело запроса | Ответ |
|-------|------|----------|-----------|--------------|-------|
| `POST` | `/` | Создать отношение подписки | - | `FollowRequestDto` | `FollowResponseDto` |
| `DELETE` | `/{followerId}/{followingId}` | Удалить отношение подписки | `followerId`, `followingId` (UUID) | - | `204 No Content` |
| `GET` | `/{userId}/followers` | Получить список подписчиков | `userId` (UUID), `FollowerFilter`, `Pageable` | - | `PagedModel<FollowerResponseDto>` |
| `GET` | `/{userId}/following` | Получить список подписок | `userId` (UUID), `FollowingFilter`, `Pageable` | - | `PagedModel<FollowingResponseDto>` |
| `GET` | `/{followerId}/{followingId}/status` | Проверить статус подписки | `followerId`, `followingId` (UUID) | - | `FollowStatusResponseDto` |
| `GET` | `/{userId}/stats` | Получить статистику подписок | `userId` (UUID) | - | `FollowStatsResponseDto` |

### Детальное описание эндпоинтов

#### 1. Создать отношение подписки

```http
POST /api/v1/follows
Content-Type: application/json
```

**Тело запроса:**
```json
{
  "followerId": "123e4567-e89b-12d3-a456-426614174000",
  "followingId": "987fcdeb-51a2-43d7-b123-426614174999"
}
```

**Валидация:**
- `followerId` - обязательное поле, UUID формат
- `followingId` - обязательное поле, UUID формат
- Пользователь не может подписаться на самого себя
- Отношение подписки должно быть уникальным
- Оба пользователя должны существовать в системе

**Ответы:**
- `201 Created` - подписка создана успешно
- `400 Bad Request` - ошибка валидации (неверный формат UUID, null поля)
- `409 Conflict` - нарушение бизнес-правил (самоподписка, пользователь не существует, подписка уже существует)

**Пример ответа (201 Created):**
```json
{
  "id": "456e7890-e89b-12d3-a456-426614174111",
  "followerId": "123e4567-e89b-12d3-a456-426614174000",
  "followingId": "987fcdeb-51a2-43d7-b123-426614174999",
  "createdAt": "2025-01-27T10:30:00Z"
}
```

**Пример ответа (409 Conflict - самоподписка):**
```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 409,
  "detail": "Business rule 'SELF_FOLLOW_NOT_ALLOWED' violated for context: User cannot follow themselves (userId=123e4567-e89b-12d3-a456-426614174000)",
  "ruleName": "SELF_FOLLOW_NOT_ALLOWED",
  "context": "User cannot follow themselves (userId=123e4567-e89b-12d3-a456-426614174000)",
  "timestamp": "2025-01-27T10:30:00Z"
}
```

#### 2. Удалить отношение подписки

```http
DELETE /api/v1/follows/{followerId}/{followingId}
```

**Параметры:**
- `followerId` (UUID) - идентификатор пользователя, который подписывается
- `followingId` (UUID) - идентификатор пользователя, на которого подписываются

**Ответы:**
- `204 No Content` - подписка удалена успешно
- `404 Not Found` - отношение подписки не найдено
- `400 Bad Request` - неверный формат UUID

**Пример ответа (404 Not Found):**
```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 404,
  "detail": "Follow relationship between followerId=123e4567-e89b-12d3-a456-426614174000 and followingId=987fcdeb-51a2-43d7-b123-426614174999 does not exist",
  "ruleName": "FOLLOW_NOT_FOUND",
  "context": "Follow relationship between followerId=123e4567-e89b-12d3-a456-426614174000 and followingId=987fcdeb-51a2-43d7-b123-426614174999 does not exist",
  "timestamp": "2025-01-27T10:30:00Z"
}
```

#### 3. Получить список подписчиков

```http
GET /api/v1/follows/{userId}/followers?login=john&page=0&size=10&sort=createdAt,desc
```

**Параметры:**
- `userId` (UUID, path) - идентификатор пользователя, чьих подписчиков нужно получить
- `login` (String, query, optional) - фильтр по логину подписчика (частичное совпадение, без учета регистра)
- `page` (int, query, default: 0) - номер страницы
- `size` (int, query, default: 10) - размер страницы (максимум: 100)
- `sort` (String, query, default: createdAt,desc) - сортировка

**Ограничения пагинации:**
- Дефолтный размер страницы: 10 элементов
- Максимальный размер страницы: 100 элементов
- Дефолтная сортировка: по дате создания (новые сначала)

**Ответы:**
- `200 OK` - список подписчиков получен успешно
- `400 Bad Request` - неверный формат UUID

**Пример ответа:**
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "login": "john_doe",
      "createdAt": "2025-01-20T15:30:00Z"
    },
    {
      "id": "987fcdeb-51a2-43d7-b123-426614174999",
      "login": "jane_smith",
      "createdAt": "2025-01-19T10:20:00Z"
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

**Особенности:**
- Фильтрация по логину выполняется на уровне сервиса после получения данных из users-api
- Логины подписчиков получаются из users-api через `UserGateway`
- Если логин пользователя не найден, используется значение "unknown"

#### 4. Получить список подписок

```http
GET /api/v1/follows/{userId}/following?login=jane&page=0&size=10&sort=createdAt,desc
```

**Параметры:**
- `userId` (UUID, path) - идентификатор пользователя, чьи подписки нужно получить
- `login` (String, query, optional) - фильтр по логину подписки (частичное совпадение, без учета регистра)
- `page` (int, query, default: 0) - номер страницы
- `size` (int, query, default: 10) - размер страницы (максимум: 100)
- `sort` (String, query, default: createdAt,desc) - сортировка

**Ответы:**
- `200 OK` - список подписок получен успешно
- `400 Bad Request` - неверный формат UUID

**Пример ответа:**
```json
{
  "content": [
    {
      "id": "987fcdeb-51a2-43d7-b123-426614174999",
      "login": "jane_doe",
      "createdAt": "2025-01-20T15:30:00Z"
    },
    {
      "id": "456e7890-e89b-12d3-a456-426614174111",
      "login": "bob_wilson",
      "createdAt": "2025-01-19T10:20:00Z"
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

#### 5. Проверить статус подписки

```http
GET /api/v1/follows/{followerId}/{followingId}/status
```

**Параметры:**
- `followerId` (UUID) - идентификатор пользователя, который подписывается
- `followingId` (UUID) - идентификатор пользователя, на которого подписываются

**Ответы:**
- `200 OK` - статус подписки получен успешно
- `400 Bad Request` - неверный формат UUID

**Пример ответа (подписка существует):**
```json
{
  "isFollowing": true,
  "createdAt": "2025-01-20T15:30:00Z"
}
```

**Пример ответа (подписка не существует):**
```json
{
  "isFollowing": false,
  "createdAt": null
}
```

#### 6. Получить статистику подписок

```http
GET /api/v1/follows/{userId}/stats
```

**Параметры:**
- `userId` (UUID) - идентификатор пользователя, чью статистику нужно получить

**Ответы:**
- `200 OK` - статистика получена успешно
- `400 Bad Request` - неверный формат UUID

**Пример ответа:**
```json
{
  "followersCount": 150,
  "followingCount": 75
}
```

## OpenAPI/Swagger Документация

### Обзор

Сервис включает полную OpenAPI 3.0 документацию, предоставляемую через SpringDoc OpenAPI. Документация содержит интерактивные возможности для тестирования API, детальные схемы данных и примеры запросов/ответов.

### Доступ к документации

#### Swagger UI
- **URL**: `http://localhost:8084/swagger-ui.html`
- **Описание**: Интерактивный интерфейс для изучения и тестирования API
- **Возможности**:
  - Просмотр всех эндпоинтов с детальным описанием
  - Интерактивное тестирование API (Try it out)
  - Просмотр схем данных и валидации
  - Просмотр примеров запросов и ответов
  - Просмотр кодов ошибок и их описаний

#### OpenAPI Specification
- **URL**: `http://localhost:8084/v3/api-docs`
- **Описание**: JSON спецификация OpenAPI 3.0
- **Использование**: Для генерации клиентских библиотек и интеграции с инструментами

#### Swagger Config
- **URL**: `http://localhost:8084/v3/api-docs/swagger-config`
- **Описание**: Конфигурация Swagger UI

### Конфигурация

Конфигурация OpenAPI находится в `OpenApiConfig.java`:

```java
@Bean
public OpenAPI followerApiOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Twitter Follower API")
            .description("...")
            .version("1.0.0"))
        .servers(List.of(
            new Server()
                .url("http://localhost:8084")
                .description("Local development server")
        ));
}
```

## Бизнес-логика

### FollowService

Основной сервис для работы с отношениями подписки, реализующий следующие операции:

#### Методы сервиса:

1. **`follow(FollowRequestDto request)`**
   - Создает новое отношение подписки между двумя пользователями
   - Возвращает `FollowResponseDto` с данными созданной подписки
   - Логика:
     - Валидация запроса через `FollowValidator`
     - Преобразование DTO в сущность через `FollowMapper`
     - Сохранение в базу данных через `FollowRepository`
     - Преобразование сущности в DTO для ответа
   - Транзакционность: `@Transactional`

2. **`unfollow(UUID followerId, UUID followingId)`**
   - Удаляет отношение подписки между двумя пользователями
   - Логика:
     - Поиск отношения подписки в базе данных
     - Если не найдено - выбрасывается `ResponseStatusException` с HTTP 404
     - Удаление отношения из базы данных
   - Транзакционность: `@Transactional`

3. **`getFollowers(UUID userId, FollowerFilter filter, Pageable pageable)`**
   - Получает список подписчиков пользователя с пагинацией и фильтрацией
   - Возвращает `PagedModel<FollowerResponseDto>`
   - Логика:
     - Получение страницы отношений подписки из базы данных
     - Для каждого отношения получение логина подписчика из users-api
     - Преобразование в DTO через `FollowMapper`
     - Фильтрация по логину (если указан фильтр)
     - Возврат пагинированного результата
   - Транзакционность: `@Transactional(readOnly = true)`

4. **`getFollowing(UUID userId, FollowingFilter filter, Pageable pageable)`**
   - Получает список подписок пользователя с пагинацией и фильтрацией
   - Возвращает `PagedModel<FollowingResponseDto>`
   - Логика аналогична `getFollowers`, но использует `followerId` вместо `followingId`

5. **`getFollowStatus(UUID followerId, UUID followingId)`**
   - Проверяет статус подписки между двумя пользователями
   - Возвращает `FollowStatusResponseDto`
   - Логика:
     - Поиск отношения подписки в базе данных
     - Если найдено - возвращает `isFollowing=true` и `createdAt`
     - Если не найдено - возвращает `isFollowing=false` и `createdAt=null`
   - Транзакционность: `@Transactional(readOnly = true)`

6. **`getFollowStats(UUID userId)`**
   - Получает статистику подписок пользователя
   - Возвращает `FollowStatsResponseDto` с количеством подписчиков и подписок
   - Логика:
     - Подсчет подписчиков через `countByFollowingId`
     - Подсчет подписок через `countByFollowerId`
     - Преобразование в DTO через `FollowMapper`
   - Транзакционность: `@Transactional(readOnly = true)`

### Ключевые бизнес-правила:

1. **Запрет самоподписки:**
   - Пользователь не может подписаться на самого себя
   - Проверка выполняется в `FollowValidator.validateNoSelfFollow`
   - При нарушении выбрасывается `BusinessRuleValidationException` с правилом "SELF_FOLLOW_NOT_ALLOWED"

2. **Уникальность подписки:**
   - Отношение подписки должно быть уникальным (пара `followerId`, `followingId`)
   - Проверка выполняется в `FollowValidator.validateUniqueness`
   - При нарушении выбрасывается `UniquenessValidationException`

3. **Существование пользователей:**
   - Оба пользователя (follower и following) должны существовать в системе
   - Проверка выполняется через `UserGateway.existsUser` с интеграцией в users-api
   - При нарушении выбрасывается `BusinessRuleValidationException` с правилом "FOLLOWER_NOT_EXISTS" или "FOLLOWING_NOT_EXISTS"

4. **Автоматическое управление временными метками:**
   - Поле `createdAt` устанавливается автоматически при создании через `@CreationTimestamp`
   - Поле не может быть обновлено после создания (`updatable = false`)

## Слой валидации

### Архитектура валидации

Валидация в сервисе выполняется на нескольких уровнях:

1. **Bean Validation (Jakarta Validation)** - валидация формата данных на уровне DTO
2. **Business Rule Validation** - валидация бизнес-правил через `FollowValidator`
3. **Database Constraints** - валидация на уровне базы данных

### FollowValidator

Валидатор централизует всю логику валидации для отношений подписки.

#### Методы валидатора:

1. **`validateForFollow(FollowRequestDto request)`**
   - Основной метод валидации для создания подписки
   - Выполняет все проверки:
     - Проверка на null запроса и полей
     - Проверка запрета самоподписки
     - Проверка существования пользователей
     - Проверка уникальности подписки

2. **`validateNoSelfFollow(UUID followerId, UUID followingId)`** (private)
   - Проверяет, что пользователь не пытается подписаться на самого себя
   - Выбрасывает `BusinessRuleValidationException` с правилом "SELF_FOLLOW_NOT_ALLOWED"

3. **`validateUsersExist(UUID followerId, UUID followingId)`** (private)
   - Проверяет существование обоих пользователей через `UserGateway`
   - Выбрасывает `BusinessRuleValidationException` с правилом "FOLLOWER_ID_NULL", "FOLLOWING_ID_NULL", "FOLLOWER_NOT_EXISTS" или "FOLLOWING_NOT_EXISTS"

4. **`validateUniqueness(UUID followerId, UUID followingId)`** (private)
   - Проверяет уникальность отношения подписки через `FollowRepository`
   - Выбрасывает `UniquenessValidationException` при дублировании

### Типы исключений валидации

#### 1. BusinessRuleValidationException

Используется для нарушений бизнес-правил.

**HTTP статус:** `409 Conflict`  
**Content-Type:** `application/problem+json`

**Пример ответа (самоподписка):**
```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 409,
  "detail": "Business rule 'SELF_FOLLOW_NOT_ALLOWED' violated for context: User cannot follow themselves (userId=123e4567-e89b-12d3-a456-426614174000)",
  "ruleName": "SELF_FOLLOW_NOT_ALLOWED",
  "context": "User cannot follow themselves (userId=123e4567-e89b-12d3-a456-426614174000)",
  "timestamp": "2025-01-27T10:30:00Z"
}
```

**Пример ответа (пользователь не существует):**
```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 409,
  "detail": "Business rule 'FOLLOWER_NOT_EXISTS' violated for context: 123e4567-e89b-12d3-a456-426614174000",
  "ruleName": "FOLLOWER_NOT_EXISTS",
  "context": "123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2025-01-27T10:30:00Z"
}
```

#### 2. UniquenessValidationException

Используется для нарушений уникальности.

**HTTP статус:** `409 Conflict`  
**Content-Type:** `application/problem+json`

**Пример ответа:**
```json
{
  "type": "https://example.com/errors/uniqueness-validation",
  "title": "Uniqueness Validation Error",
  "status": 409,
  "detail": "Follow relationship already exists",
  "timestamp": "2025-01-27T10:30:00Z"
}
```

#### 3. Format Validation (Jakarta Validation)

Используется для ошибок формата данных.

**HTTP статус:** `400 Bad Request`  
**Content-Type:** `application/problem+json`

**Пример ответа:**
```json
{
  "type": "https://example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed: followerId: Follower ID cannot be null",
  "timestamp": "2025-01-27T10:30:00Z"
}
```

### Валидация по операциям

#### Создание подписки (POST /api/v1/follows)

1. **Bean Validation:**
   - `followerId` - `@NotNull`
   - `followingId` - `@NotNull`

2. **Business Rule Validation:**
   - Проверка на null запроса
   - Проверка запрета самоподписки
   - Проверка существования пользователей
   - Проверка уникальности подписки

#### Удаление подписки (DELETE /api/v1/follows/{followerId}/{followingId})

- Проверка существования отношения подписки (404 если не найдено)

## Работа с базой данных

### Сущность Follow

JPA сущность, представляющая отношение подписки в базе данных.

**Аннотации:**
- `@Entity` - JPA сущность
- `@Table` - таблица `follows` с уникальным ограничением на `(follower_id, following_id)`
- `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` - Lombok аннотации

**Поля:**
- `id` (UUID) - первичный ключ, генерируется автоматически
- `followerId` (UUID) - идентификатор пользователя, который подписывается
- `followingId` (UUID) - идентификатор пользователя, на которого подписываются
- `createdAt` (LocalDateTime) - время создания, устанавливается автоматически через `@CreationTimestamp`

### Таблица follows

| Поле | Тип | Ограничения | Описание |
|------|-----|-------------|----------|
| `id` | UUID | PRIMARY KEY, NOT NULL | Уникальный идентификатор отношения подписки |
| `follower_id` | UUID | NOT NULL, FOREIGN KEY (users.id) | Идентификатор пользователя, который подписывается |
| `following_id` | UUID | NOT NULL, FOREIGN KEY (users.id) | Идентификатор пользователя, на которого подписываются |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Время создания отношения подписки |

**Ограничения базы данных:**
- `follows_unique_follower_following` - уникальное ограничение на пару `(follower_id, following_id)`
- `follows_check_no_self_follow` - проверочное ограничение: `follower_id != following_id`
- `follows_follower_fk` - внешний ключ на таблицу `users`
- `follows_following_fk` - внешний ключ на таблицу `users`

**Индексы:**
- `idx_follows_follower_id` - индекс для запросов по подписчику
- `idx_follows_following_id` - индекс для запросов по подписке
- `idx_follows_created_at` - индекс для сортировки по дате создания

### FollowRepository

JPA репозиторий для работы с сущностью `Follow`.

**Методы репозитория:**

1. **`existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId)`**
   - Проверяет существование отношения подписки
   - Используется для валидации уникальности

2. **`findByFollowerId(UUID followerId, Pageable pageable)`**
   - Находит все подписки пользователя (кого он подписывается)
   - Используется в `getFollowing`

3. **`findByFollowingId(UUID followingId, Pageable pageable)`**
   - Находит всех подписчиков пользователя (кто на него подписан)
   - Используется в `getFollowers`

4. **`countByFollowerId(UUID followerId)`**
   - Подсчитывает количество подписок пользователя
   - Используется в `getFollowStats`

5. **`countByFollowingId(UUID followingId)`**
   - Подсчитывает количество подписчиков пользователя
   - Используется в `getFollowStats`

6. **`findByFollowerIdAndFollowingId(UUID followerId, UUID followingId)`**
   - Находит конкретное отношение подписки
   - Используется в `getFollowStatus` и `unfollow`

## Интеграция с users-api

### Архитектура интеграции

Follower API интегрируется с Users API через Feign Client для проверки существования пользователей и получения их логинов. Интеграция необходима, так как информация о пользователях хранится в отдельном сервисе.

### Компоненты интеграции

#### 1. UsersApiClient

Feign клиент для HTTP вызовов к users-api.

**Конфигурация:**
- Имя клиента: `users-api`
- Базовый URL: `http://localhost:8081` (настраивается через `app.users-api.base-url`)
- Путь: `/api/v1/users`

**Методы:**
- `existsUser(UUID userId)` - проверка существования пользователя
- `getUserById(UUID id)` - получение данных пользователя по ID

#### 2. UserGateway

Gateway компонент, абстрагирующий работу с users-api и предоставляющий удобный интерфейс для сервисного слоя.

**Методы:**
- `existsUser(UUID userId)` - проверка существования пользователя, возвращает `boolean`
- `getUserLogin(UUID userId)` - получение логина пользователя, возвращает `Optional<String>`

**Обработка ошибок:**
- При ошибках Feign клиента методы возвращают безопасные значения (`false` или `Optional.empty()`)
- Ошибки логируются на уровне DEBUG

### Процесс создания подписки

1. **Валидация запроса** - проверка формата данных через Bean Validation
2. **Проверка бизнес-правил** - через `FollowValidator`:
   - Проверка запрета самоподписки
   - Проверка существования пользователей через `UserGateway.existsUser` → `UsersApiClient.existsUser` → users-api
   - Проверка уникальности подписки
3. **Создание подписки** - сохранение в базу данных
4. **Возврат результата** - преобразование в DTO и возврат клиенту

### Процесс получения списка подписчиков/подписок

1. **Получение данных из БД** - запрос к `FollowRepository` с пагинацией
2. **Получение логинов** - для каждого отношения подписки:
   - Вызов `UserGateway.getUserLogin` → `UsersApiClient.getUserById` → users-api
   - Если логин не найден, используется значение "unknown"
3. **Преобразование в DTO** - через `FollowMapper`
4. **Фильтрация** - по логину (если указан фильтр)
5. **Возврат результата** - пагинированный список DTO

### Обработка ошибок

**Сценарии ошибок:**
- users-api недоступен - методы `UserGateway` возвращают безопасные значения, операция продолжается
- Пользователь не найден - `existsUser` возвращает `false`, выбрасывается `BusinessRuleValidationException`
- Ошибка сети - логируется на уровне DEBUG, возвращается безопасное значение

**Логирование:**
- Все вызовы к users-api логируются на уровне DEBUG
- Ошибки логируются с предупреждением

## Примеры использования

### Создание подписки

```bash
curl -X POST http://localhost:8084/api/v1/follows \
  -H "Content-Type: application/json" \
  -d '{
    "followerId": "123e4567-e89b-12d3-a456-426614174000",
    "followingId": "987fcdeb-51a2-43d7-b123-426614174999"
  }'
```

**Ответ (201 Created):**

```json
{
  "id": "456e7890-e89b-12d3-a456-426614174111",
  "followerId": "123e4567-e89b-12d3-a456-426614174000",
  "followingId": "987fcdeb-51a2-43d7-b123-426614174999",
  "createdAt": "2025-01-27T10:30:00Z"
}
```

### Удаление подписки

```bash
curl -X DELETE http://localhost:8084/api/v1/follows/123e4567-e89b-12d3-a456-426614174000/987fcdeb-51a2-43d7-b123-426614174999
```

**Ответ (204 No Content):**

```
(пустое тело ответа)
```

### Получение списка подписчиков

```bash
curl -X GET "http://localhost:8084/api/v1/follows/123e4567-e89b-12d3-a456-426614174000/followers?login=john&page=0&size=10&sort=createdAt,desc"
```

**Ответ (200 OK):**

```json
{
  "content": [
    {
      "id": "987fcdeb-51a2-43d7-b123-426614174999",
      "login": "john_doe",
      "createdAt": "2025-01-20T15:30:00Z"
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### Получение списка подписок

```bash
curl -X GET "http://localhost:8084/api/v1/follows/123e4567-e89b-12d3-a456-426614174000/following?login=jane&page=0&size=10"
```

**Ответ (200 OK):**

```json
{
  "content": [
    {
      "id": "456e7890-e89b-12d3-a456-426614174111",
      "login": "jane_smith",
      "createdAt": "2025-01-19T10:20:00Z"
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### Проверка статуса подписки

```bash
curl -X GET http://localhost:8084/api/v1/follows/123e4567-e89b-12d3-a456-426614174000/987fcdeb-51a2-43d7-b123-426614174999/status
```

**Ответ (200 OK - подписка существует):**

```json
{
  "isFollowing": true,
  "createdAt": "2025-01-20T15:30:00Z"
}
```

**Ответ (200 OK - подписка не существует):**

```json
{
  "isFollowing": false,
  "createdAt": null
}
```

### Получение статистики подписок

```bash
curl -X GET http://localhost:8084/api/v1/follows/123e4567-e89b-12d3-a456-426614174000/stats
```

**Ответ (200 OK):**

```json
{
  "followersCount": 150,
  "followingCount": 75
}
```

## Конфигурация

### Зависимости

Основные зависимости проекта:

- **Spring Boot 3.x** - основной фреймворк
- **Spring Data JPA** - работа с базой данных
- **Spring Web** - REST API
- **Spring Cloud OpenFeign** - интеграция с другими микросервисами
- **SpringDoc OpenAPI** - документация API и Swagger UI
- **MapStruct** - маппинг объектов
- **Lombok** - генерация кода
- **PostgreSQL** - база данных
- **Jakarta Validation** - валидация данных

### Управление зависимостями

Сервис использует **централизованное управление версиями** через `dependencyManagement` в корневом `build.gradle`.

**Важно**: При добавлении новых зависимостей в `build.gradle` сервиса **НЕ указывайте версии** - они автоматически резолвятся через `dependencyManagement`.

### Конфигурационные файлы

- `application.yml` - основная конфигурация приложения
- `application-docker.yml` - конфигурация для Docker окружения
- `application-test.yml` - конфигурация для тестов

### Основные настройки

**Порт сервиса:** `8084`

**База данных:**
- URL: `jdbc:postgresql://localhost:5432/twitter`
- Драйвер: `org.postgresql.Driver`

**Интеграция с users-api:**
- Базовый URL: `http://localhost:8081` (настраивается через `app.users-api.base-url`)

**Feign клиент:**
- Connect timeout: 2000ms
- Read timeout: 5000ms
- Logger level: basic

## Запуск и развертывание

### Локальный запуск

1. Убедитесь, что PostgreSQL запущен на порту 5432
2. Создайте базу данных `twitter` (если еще не создана)
3. Выполните SQL скрипты для создания таблиц (см. `sql/script_3_follows.sql`)
4. Убедитесь, что users-api запущен на порту 8081 (для интеграции)
5. Запустите приложение:

```bash
./gradlew :services:follower-api:bootRun
```

Или из корня проекта:

```bash
./gradlew bootRun --args='--spring.profiles.active=default'
```

### Docker

```bash
# Сборка образа
docker build -t follower-api services/follower-api

# Запуск контейнера
docker run -p 8084:8084 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/twitter \
  -e SPRING_DATASOURCE_USERNAME=user \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e APP_USERS_API_BASE_URL=http://host.docker.internal:8081 \
  follower-api
```

### Мониторинг

Приложение предоставляет следующие эндпоинты мониторинга:

- `/actuator/health` - состояние здоровья приложения
- `/actuator/info` - информация о приложении
- `/actuator/metrics` - метрики приложения
- `/actuator/tracing` - трассировка запросов
- `/swagger-ui.html` - интерактивная документация API
- `/v3/api-docs` - OpenAPI спецификация

## Безопасность

### Валидация

- Все входящие данные валидируются через Jakarta Validation
- Кастомная валидация для бизнес-правил через `FollowValidator`
- Валидация на уровне базы данных через constraints

### Логирование

- Все запросы логируются через `@LoggableRequest` аспект
- Подробное логирование операций на уровне DEBUG
- Логирование ошибок интеграции с users-api

### Защита от ошибок

- Обработка ошибок Feign клиента с безопасными значениями по умолчанию
- Валидация всех входных параметров перед обработкой
- Транзакционность операций для обеспечения целостности данных

## Тестирование

Проект включает:

- **Unit тесты** для всех компонентов:
  - `UserGatewayTest` - тесты Gateway для интеграции с users-api
  - `FollowMapperTest` - тесты маппера с реальным MapStruct маппером
  - `FollowValidatorImplTest` - тесты валидатора со всеми сценариями
- **Integration тесты** (при необходимости)
- **Тесты валидации** с покрытием всех сценариев

Запуск тестов:

```bash
./gradlew :services:follower-api:test
```

### Покрытие тестами

- `UserGatewayTest` - тестирование методов `existsUser` и `getUserLogin` с различными сценариями
- `FollowMapperTest` - тестирование всех методов маппинга между сущностями и DTO
- `FollowValidatorImplTest` - тестирование всех правил валидации:
  - Проверка null значений
  - Проверка запрета самоподписки
  - Проверка существования пользователей
  - Проверка уникальности подписки

