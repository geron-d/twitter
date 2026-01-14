# Проектирование интеграции с follower-api для создания follow-отношений

## Дата создания
2025-01-27

## Цель
Детально спроектировать интеграцию admin-script-api с follower-api для создания follow-отношений между пользователями.

---

## 1. Анализ существующих интеграций

### 1.1 Интеграция с users-api

**Структура:**
- `UsersApiClient` (Feign Client) → `UsersGateway` (Gateway) → `GenerateUsersAndTweetsServiceImpl` (Service)

**DTO:**
- Используются DTO из `com.twitter.common.dto`:
  - `UserRequestDto` (из common-lib)
  - `UserResponseDto` (из common-lib)

**Конфигурация:**
- URL: `${app.users-api.base-url:http://localhost:8081}`
- Path: `/api/v1/users`

### 1.2 Интеграция с tweet-api

**Структура:**
- `TweetsApiClient` (Feign Client) → `TweetsGateway` (Gateway) → `GenerateUsersAndTweetsServiceImpl` (Service)

**DTO:**
- Используются DTO из `com.twitter.common.dto`:
  - `CreateTweetRequestDto` (из common-lib)
  - `DeleteTweetRequestDto` (из common-lib)
  - `TweetResponseDto` (из common-lib)

**Конфигурация:**
- URL: `${app.tweet-api.base-url:http://localhost:8082}`
- Path: `/api/v1/tweets`

### 1.3 Выводы

**Паттерн интеграции:**
1. Feign Client интерфейс в пакете `com.twitter.client`
2. Gateway компонент в пакете `com.twitter.gateway`
3. DTO из common-lib (`com.twitter.common.dto`)

**Особенность для follower-api:**
- DTO для follow-отношений (`FollowRequestDto`, `FollowResponseDto`) находятся в service-specific пакете `com.twitter.dto` (follower-api), а не в common-lib
- Это требует принятия решения: использовать напрямую из follower-api или создать shared DTO в common-lib

---

## 2. Варианты использования DTO

### 2.1 Вариант 1: Использование DTO напрямую из follower-api

**Подход:**
- Импортировать `FollowRequestDto` и `FollowResponseDto` напрямую из пакета `com.twitter.dto.request` и `com.twitter.dto.response` (follower-api)
- Добавить зависимость на модуль `follower-api` в `build.gradle` admin-script-api

**Преимущества:**
- ✅ Не нужно дублировать DTO
- ✅ Автоматическая синхронизация при изменении DTO в follower-api
- ✅ Соответствует assumption из TODO_1.md: "DTO для follow-отношений можно использовать напрямую из follower-api"

**Недостатки:**
- ❌ Создаёт зависимость на уровне кода между admin-script-api и follower-api
- ❌ Нарушает принцип loose coupling (admin-script-api зависит от внутренней структуры follower-api)
- ❌ При изменении пакета DTO в follower-api потребуется обновление admin-script-api

**Реализация:**
```java
// В FollowApiClient
import com.twitter.dto.request.FollowRequestDto;  // из follower-api
import com.twitter.dto.response.FollowResponseDto;  // из follower-api
```

**Зависимость в build.gradle:**
```gradle
dependencies {
    implementation project(':services:follower-api')  // для доступа к DTO
}
```

### 2.2 Вариант 2: Создание shared DTO в common-lib

**Подход:**
- Создать `FollowRequestDto` и `FollowResponseDto` в `shared/common-lib/src/main/java/com/twitter/common/dto/`
- Обновить follower-api для использования shared DTO
- Использовать shared DTO в admin-script-api

**Преимущества:**
- ✅ Следует паттерну проекта (DTO из common-lib для межсервисной коммуникации)
- ✅ Соответствует STANDART_PROJECT.md: "Place DTOs in common-lib when used by multiple services"
- ✅ Лучшая архитектура (loose coupling)
- ✅ Единый источник истины для DTO

**Недостатки:**
- ❌ Требует рефакторинга follower-api (перемещение DTO в common-lib)
- ❌ Больше работы на этапе реализации
- ❌ Может затронуть другие части системы, использующие DTO из follower-api

**Реализация:**
```java
// В FollowApiClient
import com.twitter.common.dto.request.FollowRequestDto;  // из common-lib
import com.twitter.common.dto.response.FollowResponseDto;  // из common-lib
```

**Зависимость в build.gradle:**
```gradle
dependencies {
    implementation project(':shared:common-lib')  // уже есть
}
```

### 2.3 Рекомендация

**Выбранный вариант:** Вариант 1 (использование DTO напрямую из follower-api)

**Обоснование:**
1. Соответствует assumption из TODO_1.md
2. Меньше работы на этапе реализации (не требуется рефакторинг follower-api)
3. Автоматическая синхронизация при изменении DTO
4. В проекте уже есть примеры зависимости между сервисами (admin-script-api зависит от common-lib, который используется всеми сервисами)

**Примечание:**
- Если в будущем потребуется использовать эти DTO в других сервисах, можно будет выполнить рефакторинг и переместить их в common-lib
- На данном этапе использование напрямую из follower-api является приемлемым решением

---

## 3. Детальное проектирование FollowApiClient

### 3.1 Структура класса

**Класс:** `FollowApiClient`
**Пакет:** `com.twitter.client`
**Тип:** Интерфейс (Feign Client)

**Аннотации:**
```java
@FeignClient(
    name = "follower-api",
    url = "${app.follower-api.base-url:http://localhost:8084}",
    path = "/api/v1/follows"
)
```

**Зависимости:**
- Spring Cloud OpenFeign (уже в зависимостях проекта)

### 3.2 Методы

**Метод createFollow:**
```java
/**
 * Creates a new follow relationship in the follower-api service.
 * <p>
 * This method creates a follow relationship between two users. The follower
 * user will be following the following user. Both user IDs must be valid UUIDs
 * and the users must exist in the system.
 *
 * @param request DTO containing followerId and followingId for the relationship
 * @return FollowResponseDto containing the created follow relationship information including ID
 */
@PostMapping
FollowResponseDto createFollow(@RequestBody @Valid FollowRequestDto request);
```

**Параметры:**
- `request` - `FollowRequestDto` с полями:
  - `followerId` (UUID) - ID пользователя, который фолловит
  - `followingId` (UUID) - ID пользователя, на которого фолловят

**Возвращаемое значение:**
- `FollowResponseDto` с полями:
  - `id` (UUID) - ID созданного follow-отношения
  - `followerId` (UUID) - ID пользователя, который фолловит
  - `followingId` (UUID) - ID пользователя, на которого фолловят
  - `createdAt` (LocalDateTime) - время создания

**HTTP метод:** POST
**Endpoint:** `/api/v1/follows` (базовый path из @FeignClient)

**Обработка ошибок:**
- Feign Client автоматически пробрасывает исключения при ошибках HTTP
- Обработка ошибок выполняется в Gateway слое

### 3.3 JavaDoc документация

**Требования:**
- Класс-уровневая JavaDoc с @author geron, @version 1.0
- Метод-уровневая JavaDoc с @param, @return
- Описание назначения и использования

**Пример:**
```java
/**
 * Feign Client for integration with Follower API service.
 * <p>
 * This client provides methods for creating and managing follow relationships
 * between users in the Twitter system. It communicates with the follower-api
 * service using HTTP REST API.
 *
 * @author geron
 * @version 1.0
 */
@FeignClient(...)
public interface FollowApiClient {
    // методы
}
```

---

## 4. Детальное проектирование FollowGateway

### 4.1 Структура класса

**Класс:** `FollowGateway`
**Пакет:** `com.twitter.gateway`
**Тип:** Компонент Spring

**Аннотации:**
```java
@Component
@RequiredArgsConstructor
@Slf4j
```

**Зависимости:**
- `FollowApiClient followApiClient` (final, через конструктор)

### 4.2 Методы

**Метод createFollow:**
```java
/**
 * Creates a new follow relationship in the follower-api service.
 * <p>
 * This method creates a follow relationship between two users through the
 * follower-api service. It handles validation, error handling, and logging.
 * If the creation fails, a RuntimeException is thrown with a descriptive message.
 *
 * @param request DTO containing followerId and followingId for the relationship
 * @return FollowResponseDto containing the created follow relationship information including ID
 * @throws IllegalArgumentException if the request is null or contains null fields
 * @throws RuntimeException if the follow relationship creation fails (e.g., service unavailable, validation error)
 */
public FollowResponseDto createFollow(FollowRequestDto request) {
    // Реализация
}
```

### 4.3 Логика обработки

**Шаг 1: Валидация входных параметров**
```java
if (request == null) {
    log.error("Attempted to create follow relationship with null request");
    throw new IllegalArgumentException("Follow request cannot be null");
}

if (request.followerId() == null || request.followingId() == null) {
    log.error("Attempted to create follow relationship with null user IDs");
    throw new IllegalArgumentException("Follower ID and Following ID cannot be null");
}
```

**Шаг 2: Вызов Feign Client**
```java
try {
    FollowResponseDto response = followApiClient.createFollow(request);
    log.info("Successfully created follow relationship: {} -> {} (ID: {})", 
        request.followerId(), request.followingId(), response.id());
    return response;
} catch (Exception ex) {
    log.error("Failed to create follow relationship {} -> {}. Error: {}", 
        request.followerId(), request.followingId(), ex.getMessage(), ex);
    throw new RuntimeException("Failed to create follow relationship: " + ex.getMessage(), ex);
}
```

**Обработка ошибок:**
- Валидация: IllegalArgumentException для null значений
- HTTP ошибки: RuntimeException с понятным сообщением
- Логирование: ERROR для ошибок, INFO для успешных операций

### 4.4 JavaDoc документация

**Требования:**
- Класс-уровневая JavaDoc с @author geron, @version 1.0
- Метод-уровневая JavaDoc с @param, @return, @throws
- Описание паттерна Gateway и обработки ошибок

**Пример:**
```java
/**
 * Gateway for integration with Follower API service.
 * <p>
 * This gateway provides an abstraction layer over the Feign Client for
 * creating follow relationships. It handles validation, error handling,
 * and logging, following the Gateway pattern used throughout the project.
 *
 * @author geron
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FollowGateway {
    // методы
}
```

---

## 5. Конфигурация application.yml

### 5.1 Локальное окружение (application.yml)

**Новая настройка:**
```yaml
app:
  follower-api:
    base-url: http://localhost:8084
```

**Размещение:**
- В секции `app:` вместе с другими настройками сервисов
- После `app.tweet-api.base-url`

**Полная секция:**
```yaml
app:
  users-api:
    base-url: http://localhost:8081
  tweet-api:
    base-url: http://localhost:8082
  follower-api:
    base-url: http://localhost:8084
```

### 5.2 Docker окружение (application-docker.yml)

**Новая настройка:**
```yaml
app:
  follower-api:
    base-url: http://follower-api:8084
```

**Размещение:**
- В секции `app:` вместе с другими настройками сервисов
- Использование Docker hostname `follower-api` вместо `localhost`

**Полная секция:**
```yaml
app:
  users-api:
    base-url: http://users-api:8081
  tweet-api:
    base-url: http://tweet-api:8082
  follower-api:
    base-url: http://follower-api:8084
```

### 5.3 Обоснование порта

**Порт 8084:**
- users-api: 8081
- tweet-api: 8082
- admin-script-api: 8083
- follower-api: 8084 (следующий свободный порт)

**Примечание:**
- Порт должен соответствовать реальному порту follower-api
- Если follower-api использует другой порт, настройку нужно будет скорректировать

---

## 6. Зависимости в build.gradle

### 6.1 Текущие зависимости

**Существующие зависимости admin-script-api:**
```gradle
dependencies {
    implementation project(':shared:common-lib')
    implementation project(':shared:database')
    // Spring Boot starters
    // Feign, OpenAPI, Lombok, etc.
}
```

### 6.2 Новая зависимость (Вариант 1)

**Добавление зависимости на follower-api:**
```gradle
dependencies {
    implementation project(':shared:common-lib')
    implementation project(':shared:database')
    implementation project(':services:follower-api')  // ← новая зависимость для доступа к DTO
    // остальные зависимости
}
```

**Обоснование:**
- Необходима для импорта `FollowRequestDto` и `FollowResponseDto` из пакета `com.twitter.dto`
- Создаёт зависимость на уровне кода, но это приемлемо согласно assumption

**Альтернатива (Вариант 2):**
- Если DTO будут перемещены в common-lib, эта зависимость не нужна
- Достаточно существующей зависимости `project(':shared:common-lib')`

---

## 7. Сравнение с существующими интеграциями

### 7.1 Сравнительная таблица

| Аспект | users-api | tweet-api | follower-api |
|--------|-----------|-----------|--------------|
| **Feign Client** | `UsersApiClient` | `TweetsApiClient` | `FollowApiClient` |
| **Gateway** | `UsersGateway` | `TweetsGateway` | `FollowGateway` |
| **DTO источник** | common-lib | common-lib | follower-api (service-specific) |
| **URL настройка** | `app.users-api.base-url` | `app.tweet-api.base-url` | `app.follower-api.base-url` |
| **Path** | `/api/v1/users` | `/api/v1/tweets` | `/api/v1/follows` |
| **Методы** | `createUser` | `createTweet`, `deleteTweet`, `getUserTweets` | `createFollow` |

### 7.2 Выводы

**Сходства:**
- ✅ Паттерн Feign Client → Gateway → Service
- ✅ Обработка ошибок в Gateway слое
- ✅ Логирование операций
- ✅ Валидация входных параметров

**Отличия:**
- ⚠️ DTO из service-specific пакета (follower-api) вместо common-lib
- ⚠️ Требуется зависимость на модуль follower-api

**Рекомендация:**
- Следовать существующему паттерну интеграции
- Использовать DTO напрямую из follower-api (согласно assumption)
- В будущем рассмотреть перемещение DTO в common-lib для консистентности

---

## 8. Примеры использования

### 8.1 Пример создания follow-отношения

**В Service слое:**
```java
// В GenerateUsersAndTweetsServiceImpl
FollowRequestDto request = FollowRequestDto.builder()
    .followerId(centralUser)
    .followingId(userToFollow)
    .build();

FollowResponseDto response = followGateway.createFollow(request);
createdFollows.add(response.id());
```

**Логи:**
```
INFO  - Successfully created follow relationship: 123e4567-e89b-12d3-a456-426614174000 -> 223e4567-e89b-12d3-a456-426614174001 (ID: 456e7890-e89b-12d3-a456-426614174111)
```

### 8.2 Пример обработки ошибки

**В Service слое:**
```java
try {
    FollowResponseDto response = followGateway.createFollow(request);
    createdFollows.add(response.id());
} catch (Exception ex) {
    String errorMsg = String.format("Failed to create follow relationship %s -> %s: %s", 
        request.followerId(), request.followingId(), ex.getMessage());
    log.error(errorMsg, ex);
    errors.add(errorMsg);
}
```

**Логи:**
```
ERROR - Failed to create follow relationship 123e4567-e89b-12d3-a456-426614174000 -> 223e4567-e89b-12d3-a456-426614174001. Error: Connection timeout
```

---

## 9. Тестирование интеграции

### 9.1 Unit тесты для FollowGateway

**Тесты:**
- ✅ Успешное создание follow-отношения
- ✅ Обработка ошибок (HTTP ошибки, timeout)
- ✅ Валидация null request
- ✅ Валидация null полей в request

**Моки:**
- `@Mock FollowApiClient` - мокирование Feign Client
- `@InjectMocks FollowGateway` - тестируемый класс

### 9.2 Integration тесты

**Тесты:**
- ✅ Успешное создание follow-отношения через WireMock
- ✅ Обработка ошибок follower-api (500, timeout)
- ✅ Проверка корректности HTTP запросов

**WireMock stubs:**
- POST `/api/v1/follows` - успешное создание (201 Created)
- POST `/api/v1/follows` - ошибка сервера (500 Internal Server Error)

---

## 10. Выводы и рекомендации

### 10.1 Выводы

1. **Интеграция с follower-api:**
   - Следует паттерну существующих интеграций (Feign Client → Gateway → Service)
   - Использует DTO напрямую из follower-api (согласно assumption)
   - Требует добавления зависимости на модуль follower-api

2. **Структура компонентов:**
   - `FollowApiClient` - Feign Client интерфейс
   - `FollowGateway` - Gateway компонент с обработкой ошибок
   - Конфигурация через `application.yml`

3. **Обработка ошибок:**
   - Валидация в Gateway слое
   - Логирование всех операций
   - Пробрасывание исключений для обработки в Service слое

### 10.2 Рекомендации

1. **Реализация:**
   - Создать `FollowApiClient` с методом `createFollow`
   - Создать `FollowGateway` с обработкой ошибок
   - Добавить настройку `app.follower-api.base-url` в `application.yml`
   - Добавить зависимость на `follower-api` в `build.gradle`

2. **Тестирование:**
   - Unit тесты для `FollowGateway`
   - Integration тесты с WireMock

3. **Документация:**
   - Полная JavaDoc для всех классов и методов
   - Обновление README.md с описанием интеграции

---

## 11. Следующие шаги

1. ✅ Шаг #2: Проектирование интеграции с follower-api (выполнено)
2. ⏭️ Шаг #3: Создание FollowApiClient (Feign Client)
3. ⏭️ Шаг #4: Создание FollowGateway
4. ⏭️ Шаг #5: Обновление GenerateUsersAndTweetsResponseDto
5. ⏭️ Шаг #6: Обновление ScriptStatisticsDto
6. ⏭️ Шаг #7: Обновление GenerateUsersAndTweetsServiceImpl
7. ⏭️ Шаг #8: Обновление application.yml

---

**Автор:** assistant  
**Дата:** 2025-01-27  
**Версия:** 1.0
