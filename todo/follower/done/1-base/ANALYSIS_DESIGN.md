# Follower API Service - Analysis and Design

## Дата создания
2025-01-27

## Обзор
Документ содержит полное проектирование микросервиса follower-api для управления подписками пользователей в системе Twitter.

---

## 1. REST API Endpoints

### 1.1 POST /api/v1/follows
**Описание:** Создание новой подписки (follow relationship)

**HTTP Method:** POST

**Request Body:**
```json
{
  "followerId": "123e4567-e89b-12d3-a456-426614174000",
  "followingId": "987fcdeb-51a2-43d7-b123-426614174999"
}
```

**Response:**
- **201 Created** - Подписка успешно создана
  ```json
  {
    "id": "456e7890-e89b-12d3-a456-426614174111",
    "followerId": "123e4567-e89b-12d3-a456-426614174000",
    "followingId": "987fcdeb-51a2-43d7-b123-426614174999",
    "createdAt": "2025-01-27T10:30:00"
  }
  ```
- **400 Bad Request** - Ошибка валидации (неверный формат UUID, пустые поля)
- **409 Conflict** - Нарушение бизнес-правил (подписка на себя, двойная подписка, пользователь не существует)

**Бизнес-логика:**
- Валидация существования обоих пользователей через users-api
- Проверка, что followerId != followingId
- Проверка, что подписка еще не существует

---

### 1.2 DELETE /api/v1/follows/{followerId}/{followingId}
**Описание:** Удаление подписки (unfollow)

**HTTP Method:** DELETE

**Path Parameters:**
- `followerId` (UUID, required) - ID пользователя, который отписывается
- `followingId` (UUID, required) - ID пользователя, от которого отписываются

**Response:**
- **200 OK** - Подписка успешно удалена
- **404 Not Found** - Подписка не найдена

**Бизнес-логика:**
- Если подписка не существует, возвращается 404

---

### 1.3 GET /api/v1/follows/{userId}/followers
**Описание:** Получение списка подписчиков пользователя

**HTTP Method:** GET

**Path Parameters:**
- `userId` (UUID, required) - ID пользователя, чьих подписчиков нужно получить

**Query Parameters:**
- `page` (int, optional, default=0) - Номер страницы (для пагинации)
- `size` (int, optional, default=10) - Размер страницы
- `login` (String, optional) - Фильтр по логину подписчика (частичное совпадение)

**Response:**
- **200 OK** - Список подписчиков
  ```json
  {
    "content": [
      {
        "id": "123e4567-e89b-12d3-a456-426614174000",
        "login": "john_doe",
        "createdAt": "2025-01-20T15:30:00"
      }
    ],
    "page": {
      "number": 0,
      "size": 10,
      "totalElements": 1,
      "totalPages": 1
    }
  }
  ```

**Бизнес-логика:**
- Пагинация обязательна
- Фильтрация по логину (частичное совпадение)
- Сортировка по дате создания (createdAt DESC)

---

### 1.4 GET /api/v1/follows/{userId}/following
**Описание:** Получение списка подписок пользователя

**HTTP Method:** GET

**Path Parameters:**
- `userId` (UUID, required) - ID пользователя, чьи подписки нужно получить

**Query Parameters:**
- `page` (int, optional, default=0) - Номер страницы (для пагинации)
- `size` (int, optional, default=10) - Размер страницы
- `login` (String, optional) - Фильтр по логину подписки (частичное совпадение)

**Response:**
- **200 OK** - Список подписок
  ```json
  {
    "content": [
      {
        "id": "987fcdeb-51a2-43d7-b123-426614174999",
        "login": "jane_smith",
        "createdAt": "2025-01-25T12:00:00"
      }
    ],
    "page": {
      "number": 0,
      "size": 10,
      "totalElements": 1,
      "totalPages": 1
    }
  }
  ```

**Бизнес-логика:**
- Пагинация обязательна
- Фильтрация по логину (частичное совпадение)
- Сортировка по дате создания (createdAt DESC)

---

### 1.5 GET /api/v1/follows/{followerId}/{followingId}/status
**Описание:** Проверка статуса подписки

**HTTP Method:** GET

**Path Parameters:**
- `followerId` (UUID, required) - ID пользователя, который подписан
- `followingId` (UUID, required) - ID пользователя, на которого подписан

**Response:**
- **200 OK** - Статус подписки
  ```json
  {
    "isFollowing": true,
    "createdAt": "2025-01-20T15:30:00"
  }
  ```
  или
  ```json
  {
    "isFollowing": false,
    "createdAt": null
  }
  ```

**Бизнес-логика:**
- Если подписка существует, возвращается isFollowing=true и createdAt
- Если подписка не существует, возвращается isFollowing=false и createdAt=null

---

### 1.6 GET /api/v1/follows/{userId}/stats
**Описание:** Получение статистики подписок пользователя

**HTTP Method:** GET

**Path Parameters:**
- `userId` (UUID, required) - ID пользователя, чью статистику нужно получить

**Response:**
- **200 OK** - Статистика подписок
  ```json
  {
    "followersCount": 150,
    "followingCount": 75
  }
  ```

**Бизнес-логика:**
- Подсчет количества подписчиков (followersCount)
- Подсчет количества подписок (followingCount)

---

## 2. Entity Structure

### 2.1 Entity: Follow

**Package:** `com.twitter.entity`

**Table Name:** `follows`

**Fields:**
- `id` (UUID, Primary Key) - Уникальный идентификатор подписки
- `followerId` (UUID, NOT NULL) - ID пользователя, который подписывается
- `followingId` (UUID, NOT NULL) - ID пользователя, на которого подписываются
- `createdAt` (LocalDateTime, NOT NULL) - Дата и время создания подписки

**Constraints:**
- UNIQUE (follower_id, following_id) - Нельзя подписаться дважды
- CHECK (follower_id != following_id) - Нельзя подписаться на себя

**JPA Annotations:**
- `@Entity`
- `@Table(name = "follows", uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}))`
- `@Id` с `@GeneratedValue(strategy = GenerationType.UUID)`
- `@Column` для всех полей
- `@CreationTimestamp` для createdAt

**Lombok Annotations:**
- `@Data`
- `@NoArgsConstructor`
- `@AllArgsConstructor`
- `@Accessors(chain = true)`

---

## 3. DTO Structure

### 3.1 Request DTOs

#### FollowRequestDto
**Package:** `com.twitter.dto.request`

**Fields:**
- `followerId` (UUID, @NotNull) - ID пользователя, который подписывается
- `followingId` (UUID, @NotNull) - ID пользователя, на которого подписываются

**Validation:**
- `@NotNull` для обоих полей
- `@Schema` аннотации для Swagger

---

### 3.2 Response DTOs

#### FollowResponseDto
**Package:** `com.twitter.dto.response`

**Fields:**
- `id` (UUID) - ID подписки
- `followerId` (UUID) - ID пользователя, который подписался
- `followingId` (UUID) - ID пользователя, на которого подписались
- `createdAt` (LocalDateTime) - Дата создания подписки

#### FollowerResponseDto
**Package:** `com.twitter.dto.response`

**Fields:**
- `id` (UUID) - ID подписчика
- `login` (String) - Логин подписчика
- `createdAt` (LocalDateTime) - Дата создания подписки

**Использование:** Для списка подписчиков (GET /followers)

#### FollowingResponseDto
**Package:** `com.twitter.dto.response`

**Fields:**
- `id` (UUID) - ID пользователя, на которого подписан
- `login` (String) - Логин пользователя, на которого подписан
- `createdAt` (LocalDateTime) - Дата создания подписки

**Использование:** Для списка подписок (GET /following)

#### FollowStatusResponseDto
**Package:** `com.twitter.dto.response`

**Fields:**
- `isFollowing` (boolean) - Флаг, указывающий, существует ли подписка
- `createdAt` (LocalDateTime, nullable) - Дата создания подписки (null, если подписки нет)

**Использование:** Для проверки статуса подписки (GET /status)

#### FollowStatsResponseDto
**Package:** `com.twitter.dto.response`

**Fields:**
- `followersCount` (long) - Количество подписчиков
- `followingCount` (long) - Количество подписок

**Использование:** Для статистики подписок (GET /stats)

---

### 3.3 Filter DTOs

#### FollowerFilter
**Package:** `com.twitter.dto.filter`

**Fields:**
- `login` (String, optional) - Фильтр по логину подписчика (частичное совпадение)

**Методы:**
- `toSpecification()` - Преобразование в JPA Specification для фильтрации

**Использование:** Для фильтрации списка подписчиков

#### FollowingFilter
**Package:** `com.twitter.dto.filter`

**Fields:**
- `login` (String, optional) - Фильтр по логину подписки (частичное совпадение)

**Методы:**
- `toSpecification()` - Преобразование в JPA Specification для фильтрации

**Использование:** Для фильтрации списка подписок

---

## 4. Business Rules

### 4.1 Правило 1: Нельзя подписаться на себя
**Описание:** Пользователь не может подписаться сам на себя.

**Валидация:**
- Проверка в `FollowValidator.validateForFollow()`
- Использование `BusinessRuleValidationException` при нарушении
- HTTP Status: 409 Conflict

**Сообщение об ошибке:**
```
"User cannot follow themselves"
```

---

### 4.2 Правило 2: Нельзя подписаться дважды
**Описание:** Пользователь не может подписаться на другого пользователя, если подписка уже существует.

**Валидация:**
- Проверка через `FollowRepository.existsByFollowerIdAndFollowingId()`
- Использование `UniquenessValidationException` при нарушении
- HTTP Status: 409 Conflict

**Сообщение об ошибке:**
```
"Follow relationship already exists"
```

---

### 4.3 Правило 3: Оба пользователя должны существовать
**Описание:** Перед созданием подписки необходимо проверить, что оба пользователя (follower и following) существуют в системе.

**Валидация:**
- Проверка через `UserGateway.existsUser()` для обоих пользователей
- Использование `BusinessRuleValidationException` при нарушении
- HTTP Status: 409 Conflict

**Сообщение об ошибке:**
```
"User with ID {userId} does not exist"
```

---

## 5. Database Schema

### 5.1 Table: follows

**SQL Script Location:** `sql/follows.sql`

**DDL:**
```sql
CREATE TABLE IF NOT EXISTS follows (
    id UUID PRIMARY KEY,
    follower_id UUID NOT NULL,
    following_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT follows_follower_fk FOREIGN KEY (follower_id) REFERENCES users(id),
    CONSTRAINT follows_following_fk FOREIGN KEY (following_id) REFERENCES users(id),
    CONSTRAINT follows_unique_follower_following UNIQUE (follower_id, following_id),
    CONSTRAINT follows_check_no_self_follow CHECK (follower_id != following_id)
);

-- Индексы для оптимизации запросов
CREATE INDEX IF NOT EXISTS idx_follows_follower_id ON follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_following_id ON follows(following_id);
CREATE INDEX IF NOT EXISTS idx_follows_created_at ON follows(created_at);
```

**Fields:**
- `id` (UUID, PRIMARY KEY) - Уникальный идентификатор подписки
- `follower_id` (UUID, NOT NULL) - ID пользователя, который подписывается
- `following_id` (UUID, NOT NULL) - ID пользователя, на которого подписываются
- `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP) - Дата и время создания подписки

**Constraints:**
- `follows_unique_follower_following` - UNIQUE (follower_id, following_id)
- `follows_check_no_self_follow` - CHECK (follower_id != following_id)
- `follows_follower_fk` - FOREIGN KEY на users(id)
- `follows_following_fk` - FOREIGN KEY на users(id)

**Indexes:**
- `idx_follows_follower_id` - Для быстрого поиска подписчиков
- `idx_follows_following_id` - Для быстрого поиска подписок
- `idx_follows_created_at` - Для сортировки по дате

---

## 6. Integration Points

### 6.1 Users API Integration

**Purpose:** Проверка существования пользователей перед созданием подписки

**Feign Client:** `UsersApiClient`
- Method: `existsUser(UUID userId)`
- Endpoint: `GET /api/v1/users/{userId}/exists`
- Response: `UserExistsResponseDto` (from common-lib)

**Gateway:** `UserGateway`
- Method: `existsUser(UUID userId) -> boolean`
- Error Handling: Логирование ошибок, возврат false при недоступности сервиса

---

## 7. Service Layer Structure

### 7.1 FollowService Interface

**Methods:**
- `FollowResponseDto follow(FollowRequestDto request)` - Создание подписки
- `void unfollow(UUID followerId, UUID followingId)` - Удаление подписки
- `Page<FollowerResponseDto> getFollowers(UUID userId, FollowerFilter filter, Pageable pageable)` - Получение списка подписчиков
- `Page<FollowingResponseDto> getFollowing(UUID userId, FollowingFilter filter, Pageable pageable)` - Получение списка подписок
- `FollowStatusResponseDto getFollowStatus(UUID followerId, UUID followingId)` - Проверка статуса подписки
- `FollowStatsResponseDto getFollowStats(UUID userId)` - Получение статистики

**Transactions:**
- `@Transactional` для методов `follow()` и `unfollow()`

---

## 8. Repository Layer Structure

### 8.1 FollowRepository Interface

**Extends:** `JpaRepository<Follow, UUID>`

**Derived Query Methods:**
- `boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId)` - Проверка существования подписки
- `Page<Follow> findByFollowingId(UUID followingId, Pageable pageable)` - Поиск подписчиков
- `Page<Follow> findByFollowerId(UUID followerId, Pageable pageable)` - Поиск подписок
- `long countByFollowingId(UUID followingId)` - Подсчет подписчиков
- `long countByFollowerId(UUID followerId)` - Подсчет подписок
- `Optional<Follow> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId)` - Поиск конкретной подписки

**Note:** Derived Query Methods НЕ имеют JavaDoc (согласно стандартам проекта)

---

## 9. Validation Layer Structure

### 9.1 FollowValidator Interface

**Methods:**
- `void validateForFollow(FollowRequestDto request)` - Валидация перед созданием подписки

**Validation Logic:**
1. Проверка, что followerId != followingId
2. Проверка существования обоих пользователей через UserGateway
3. Проверка, что подписка еще не существует

**Exceptions:**
- `BusinessRuleValidationException` - При нарушении бизнес-правил
- `UniquenessValidationException` - При попытке двойной подписки

---

## 10. Mapper Layer Structure

### 10.1 FollowMapper Interface

**MapStruct Configuration:**
- `@Mapper(componentModel = "spring")`

**Methods:**
- `Follow toFollow(FollowRequestDto dto)` - Преобразование DTO в Entity
- `FollowResponseDto toFollowResponseDto(Follow follow)` - Преобразование Entity в Response DTO
- `FollowerResponseDto toFollowerResponseDto(Follow follow, User user)` - Преобразование для списка подписчиков
- `FollowingResponseDto toFollowingResponseDto(Follow follow, User user)` - Преобразование для списка подписок

**Note:** Для FollowerResponseDto и FollowingResponseDto потребуется дополнительная информация о пользователе (login), которая будет получена через users-api или через JOIN запрос.

---

## 11. Assumptions

1. Сервис будет работать на порту 8084
2. Сервис будет использовать ту же базу данных PostgreSQL, что и другие сервисы
3. Интеграция с users-api будет через Feign Client
4. Пользователи уже существуют в системе (создаются через users-api)
5. Сервис не требует аутентификации (как и другие сервисы в проекте)
6. Таблица follows будет создана через SQL скрипт в sql/follows.sql
7. Сервис будет разворачиваться через docker-compose.yml вместе с другими сервисами
8. Healthcheck будет доступен на /actuator/health
9. Все эндпоинты будут доступны через /api/v1/follows
10. Пагинация будет использоваться для списков подписчиков/подписок

---

## 12. Open Questions / Risks

### 12.1 Получение информации о пользователях для списков
**Вопрос:** Как получать login пользователей для FollowerResponseDto и FollowingResponseDto?

**Варианты:**
1. JOIN запрос с таблицей users (если есть доступ к БД users-api)
2. Вызов users-api для каждого пользователя (неэффективно)
3. Batch запрос к users-api (если есть такой эндпоинт)
4. Кэширование информации о пользователях

**Решение:** Использовать JOIN запрос, если users-api использует ту же БД. Если нет - создать batch эндпоинт в users-api или использовать кэширование.

### 12.2 Производительность при большом количестве подписок
**Риск:** При большом количестве подписок запросы на получение списков могут быть медленными.

**Решение:** 
- Использование пагинации
- Индексы в БД (уже определены)
- Возможно, кэширование статистики

---

## 13. Next Steps

После завершения проектирования:
1. Создание SQL скрипта (шаг #5)
2. Реализация Entity (шаг #6)
3. Реализация DTO (шаг #7)
4. Реализация Repository (шаг #9)
5. Реализация Service (шаг #12)
6. Реализация Controller (шаг #13)

---

## 14. References

- [Code Standards](../../../../standards/STANDART_CODE.md)
- [Project Standards](../../../../standards/STANDART_PROJECT.md)
- [Users API README](../../../../services/users-api/README.md)
- [Tweet API README](../../../../services/tweet-api/README.md)

