# Tweet API Service

## Введение

**Tweet API** — это микросервис для управления твитами в системе Twitter, построенный на Java 24 и Spring Boot 3. Сервис предоставляет REST API для создания твитов с валидацией контента и интеграцией с users-api для проверки существования пользователей.

### Основные возможности:
- ✅ Создание твитов с валидацией контента
- ✅ Интеграция с users-api для проверки существования пользователей
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
│   │   └── CreateTweetRequestDto.java  # DTO для создания твита
│   └── response/
│       └── TweetResponseDto.java       # DTO для ответа
├── entity/
│   └── Tweet.java               # JPA сущность
├── gateway/
│   └── UserGateway.java        # Gateway для интеграции с users-api
├── client/
│   └── UsersApiClient.java     # Feign клиент для users-api
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

### Диаграмма компонентов

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ TweetController │────│  TweetService   │────│ TweetRepository │
│                 │    │                 │    │                 │
│ - REST Endpoint │    │ - Business Logic│    │ - Data Access   │
│ - Error Handling│    │ - Orchestration │    │ - Queries       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                       │
         │──────────────────────│                       │
         ▼                      ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ TweetValidator  │    │   TweetMapper   │    │   PostgreSQL    │
│                 │    │                 │    │                 │
│ - Data Validation│   │ - Entity Mapping│    │ - Database      │
│ - Business Rules│    │ - DTO Conversion│    │ - Tables        │
│ - User Check    │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌─────────────────┐
│   UserGateway   │    │      DTOs       │
│                 │    │                 │
│ - User Existence│    │ - Request/Response│
│ - Feign Client  │    │ - Validation    │
│                 │    │ - Constraints   │
└─────────────────┘    └─────────────────┘
         │
         ▼
┌─────────────────┐
│ UsersApiClient  │
│                 │
│ - Feign Client  │
│ - HTTP Calls    │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│   Users API     │
│   (Port 8081)   │
└─────────────────┘
```

## REST API

### Базовый URL
```
http://localhost:8082/api/v1/tweets
```

### Эндпоинты

| Метод | Путь | Описание | Тело запроса | Ответ |
|-------|------|----------|--------------|-------|
| `POST` | `/` | Создать новый твит | `CreateTweetRequestDto` | `TweetResponseDto` |

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

## OpenAPI/Swagger Документация

### Обзор

Сервис включает полную OpenAPI 3.0 документацию, предоставляемую через SpringDoc OpenAPI. Документация содержит интерактивные возможности для тестирования API, детальные схемы данных и примеры запросов/ответов.

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

### Особенности документации

#### Интерактивные возможности
- **Try it out**: Тестирование всех эндпоинтов прямо в браузере
- **Request Builder**: Визуальный интерфейс для создания запросов
- **Response Viewer**: Отображение ответов с форматированием
- **Schema Explorer**: Просмотр и понимание моделей данных

#### Полное покрытие API
- **Все эндпоинты**: Документация всех API эндпоинтов
- **Модели данных**: Детальные схемы для всех DTO и сущностей
- **Обработка ошибок**: Документация всех сценариев ошибок и ответов
- **Правила валидации**: Требования валидации на уровне полей
- **Бизнес-правила**: Документация системных ограничений и правил

#### Удобство для разработчиков
- **Генерация кода**: Создание клиентских SDK для различных языков
- **Интеграция с Postman**: Экспорт коллекций API для тестирования
- **Тестирование API**: Встроенные возможности тестирования
- **Экспорт документации**: Скачивание документации в различных форматах

### Конфигурация

Документация OpenAPI настроена через:

1. **Зависимости**: SpringDoc OpenAPI starter в `build.gradle`
2. **Конфигурационный класс**: `OpenApiConfig.java` для метаданных API
3. **Настройки приложения**: Конфигурация Swagger UI в `application.yml`

### Примеры использования

#### Доступ к Swagger UI
```bash
# Запуск приложения
./gradlew bootRun

# Открытие Swagger UI в браузере
open http://localhost:8082/swagger-ui.html
```

#### Генерация клиентского кода
```bash
# Использование OpenAPI Generator
openapi-generator-cli generate \
  -i http://localhost:8082/v3/api-docs \
  -g java \
  -o ./generated-client

# Генерация TypeScript клиента
openapi-generator-cli generate \
  -i http://localhost:8082/v3/api-docs \
  -g typescript-axios \
  -o ./generated-client-ts
```

#### Тестирование API через Swagger UI
1. Откройте Swagger UI в браузере
2. Выберите нужный эндпоинт
3. Нажмите "Try it out"
4. Заполните необходимые параметры
5. Нажмите "Execute"
6. Просмотрите ответ

### Безопасность документации

#### Защита чувствительных данных
- **Аутентификация**: Будущие версии будут включать JWT аутентификацию
- **Rate Limiting**: Ограничения API документированы в заголовках ответов
- **Приватность данных**: Обработка персональной информации следует руководящим принципам приватности

### Устранение неполадок

#### Частые проблемы

1. **Swagger UI не загружается**
   - Проверьте, что зависимость SpringDoc правильно добавлена
   - Убедитесь, что приложение запущено на правильном порту (8082)
   - Проверьте консоль браузера на ошибки JavaScript

2. **Эндпоинты API не отображаются**
   - Убедитесь, что контроллеры правильно аннотированы
   - Проверьте конфигурацию сканирования пакетов
   - Убедитесь в правильной автоконфигурации Spring Boot

3. **Ошибки валидации схем**
   - Проверьте аннотации DTO и правила валидации
   - Убедитесь в правильной конфигурации Jackson
   - Проверьте генерацию схем OpenAPI

#### Режим отладки
Включите отладочное логирование для SpringDoc:
```yaml
logging:
  level:
    org.springdoc: DEBUG
```

### Будущие улучшения

- **Аутентификация**: Интеграция JWT токенов
- **Rate Limiting**: Документация политик ограничения скорости
- **Webhooks**: Документация API, управляемого событиями
- **Версионирование**: Поддержка многоверсионного API
- **Интернационализация**: Поддержка многоязычной документации

### Поддержка

При проблемах с документацией API:
- Проверьте логи приложения на ошибки
- Просмотрите спецификацию OpenAPI на `/v3/api-docs`
- Обратитесь к команде разработки за помощью

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

## Слой валидации

### Архитектура валидации

Сервис использует централизованный слой валидации через `TweetValidator`, который обеспечивает:

- **Единообразную валидацию** для всех операций с твитами
- **Разделение ответственности** между бизнес-логикой и валидацией
- **Типизированные исключения** для различных видов ошибок
- **Интеграцию с Jakarta Validation** для проверки формата данных

### TweetValidator

Интерфейс `TweetValidator` определяет методы валидации для всех операций с твитами. Он включает методы для валидации создания, проверки контента и проверки существования пользователей.

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

### Примеры использования

#### Создание твита с некорректной длиной контента
```bash
curl -X POST http://localhost:8082/api/v1/tweets \
  -H "Content-Type: application/json" \
  -d '{
    "content": "This is a very long tweet that exceeds the maximum allowed length of 280 characters. This content is way too long and should trigger a validation error because it contains more than 280 characters which is the maximum allowed length for a tweet in the Twitter system.",
    "userId": "123e4567-e89b-12d3-a456-426614174000"
  }'
```

**Ответ (400 Bad Request):**
```json
{
  "type": "https://example.com/errors/format-validation",
  "title": "Format Validation Error",
  "status": 400,
  "detail": "content: Tweet content must be between 1 and 280 characters",
  "fieldName": "content",
  "constraintName": "CONTENT_VALIDATION",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

#### Создание твита от несуществующего пользователя
```bash
curl -X POST http://localhost:8082/api/v1/tweets \
  -H "Content-Type: application/json" \
  -d '{
    "content": "This is a valid tweet",
    "userId": "00000000-0000-0000-0000-000000000000"
  }'
```

**Ответ (400 Bad Request):**
```json
{
  "type": "https://example.com/errors/business-rule-validation",
  "title": "Business Rule Validation Error",
  "status": 400,
  "detail": "Business rule 'USER_NOT_EXISTS' violated for context: 00000000-0000-0000-0000-000000000000",
  "ruleName": "USER_NOT_EXISTS",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

#### Создание твита с пустым контентом
```bash
curl -X POST http://localhost:8082/api/v1/tweets \
  -H "Content-Type: application/json" \
  -d '{
    "content": "   ",
    "userId": "123e4567-e89b-12d3-a456-426614174000"
  }'
```

**Ответ (400 Bad Request):**
```json
{
  "type": "https://example.com/errors/format-validation",
  "title": "Format Validation Error",
  "status": 400,
  "detail": "Tweet content cannot be empty",
  "fieldName": "content",
  "constraintName": "EMPTY_CONTENT",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

## Работа с базой данных

### Сущность Tweet

```java
@Entity
@Table(name = "tweets")
public class Tweet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @NotBlank
    @Size(min = 1, max = 280)
    @Column(name = "content", length = 280, nullable = false)
    private String content;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

### Таблица tweets

| Поле | Тип | Ограничения | Описание |
|------|-----|-------------|----------|
| `id` | UUID | PRIMARY KEY, NOT NULL | Уникальный идентификатор |
| `user_id` | UUID | NOT NULL | ID пользователя (ссылка на users-api) |
| `content` | VARCHAR(280) | NOT NULL | Содержимое твита |
| `created_at` | TIMESTAMP | NOT NULL | Время создания |
| `updated_at` | TIMESTAMP | NOT NULL | Время последнего обновления |

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

### TweetRepository

Интерфейс репозитория расширяет `JpaRepository`:

```java
@Repository
public interface TweetRepository extends JpaRepository<Tweet, UUID> {
}
```

## Интеграция с users-api

### Архитектура интеграции

Tweet API интегрируется с Users API через Feign Client для проверки существования пользователей перед созданием твитов.

### Компоненты интеграции

#### 1. UsersApiClient (Feign Client)

Интерфейс Feign Client для вызова users-api:

```java
@FeignClient(
    name = "users-api",
    url = "${app.users-api.base-url:http://localhost:8081}",
    path = "/api/v1/users"
)
public interface UsersApiClient {
    @GetMapping("/{userId}/exists")
    UserExistsResponseDto existsUser(@PathVariable("userId") UUID userId);
}
```

**Конфигурация:**
- Базовый URL: `http://localhost:8081` (настраивается через `app.users-api.base-url`)
- Путь: `/api/v1/users`
- Эндпоинт: `GET /{userId}/exists`

#### 2. UserGateway

Gateway компонент для абстракции работы с users-api:

```java
@Component
public class UserGateway {
    private final UsersApiClient usersApiClient;
    
    public boolean existsUser(UUID userId) {
        if (userId == null) {
            return false;
        }
        try {
            UserExistsResponseDto response = usersApiClient.existsUser(userId);
            return response.exists();
        } catch (Exception ex) {
            return false;
        }
    }
}
```

**Особенности:**
- Обрабатывает `null` userId
- Обрабатывает исключения при вызове users-api
- Логирует операции для отладки

#### 3. Конфигурация Feign

Настройки Feign Client в `application.yml`:

```yaml
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

**Параметры:**
- `connect-timeout`: 2000ms - таймаут подключения
- `read-timeout`: 5000ms - таймаут чтения
- `logger-level`: basic - уровень логирования

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

### Конфигурация

Настройка базового URL users-api в `application.yml`:

```yaml
app:
  users-api:
    base-url: http://localhost:8081
```

Для разных окружений можно переопределить через переменные окружения или профили Spring.

### Мониторинг интеграции

Логирование интеграции:
- Уровень DEBUG для успешных проверок
- Уровень WARN для несуществующих пользователей
- Уровень ERROR для сетевых ошибок

Пример логов:
```
DEBUG UserGateway - User 123e4567-e89b-12d3-a456-426614174000 exists: true
WARN TweetValidatorImpl - User with ID 00000000-0000-0000-0000-000000000000 does not exist
```

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

### Создание твита с максимальной длиной

```bash
curl -X POST http://localhost:8082/api/v1/tweets \
  -H "Content-Type: application/json" \
  -d '{
    "content": "This is a tweet with exactly 280 characters. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
    "userId": "123e4567-e89b-12d3-a456-426614174000"
  }'
```

### Создание твита с минимальной длиной

```bash
curl -X POST http://localhost:8082/api/v1/tweets \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Hi",
    "userId": "123e4567-e89b-12d3-a456-426614174000"
  }'
```

## UML Диаграммы

### Диаграмма классов

```
┌─────────────────────────────────────────────────────────────┐
│                        TweetController                       │
├─────────────────────────────────────────────────────────────┤
│ - tweetService: TweetService                                │
├─────────────────────────────────────────────────────────────┤
│ + createTweet(request: CreateTweetRequestDto):              │
│   ResponseEntity<TweetResponseDto>                          │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ uses
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                        TweetService                          │
├─────────────────────────────────────────────────────────────┤
│ + createTweet(request: CreateTweetRequestDto):              │
│   TweetResponseDto                                          │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ implements
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      TweetServiceImpl                        │
├─────────────────────────────────────────────────────────────┤
│ - tweetRepository: TweetRepository                          │
│ - tweetMapper: TweetMapper                                  │
│ - tweetValidator: TweetValidator                            │
├─────────────────────────────────────────────────────────────┤
│ + createTweet(request: CreateTweetRequestDto):               │
│   TweetResponseDto                                          │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ uses
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      TweetValidator                          │
├─────────────────────────────────────────────────────────────┤
│ + validateForCreate(request: CreateTweetRequestDto): void  │
│ + validateContent(request: CreateTweetRequestDto): void     │
│ + validateUserExists(userId: UUID): void                   │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ implements
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    TweetValidatorImpl                        │
├─────────────────────────────────────────────────────────────┤
│ - validator: Validator                                      │
│ - userGateway: UserGateway                                  │
├─────────────────────────────────────────────────────────────┤
│ + validateForCreate(request: CreateTweetRequestDto): void  │
│ + validateContent(request: CreateTweetRequestDto): void     │
│ + validateUserExists(userId: UUID): void                   │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ uses
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                       UserGateway                            │
├─────────────────────────────────────────────────────────────┤
│ - usersApiClient: UsersApiClient                            │
├─────────────────────────────────────────────────────────────┤
│ + existsUser(userId: UUID): boolean                        │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ uses
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                     UsersApiClient                           │
├─────────────────────────────────────────────────────────────┤
│ + existsUser(userId: UUID): UserExistsResponseDto          │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ uses
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      TweetRepository                         │
├─────────────────────────────────────────────────────────────┤
│ + saveAndFlush(tweet: Tweet): Tweet                        │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ extends
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    JpaRepository<Tweet, UUID>               │
└─────────────────────────────────────────────────────────────┘
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

### Управление зависимостями

Сервис использует **централизованное управление версиями** через `dependencyManagement` в корневом `build.gradle`.

**Важно**: При добавлении новых зависимостей в `build.gradle` сервиса **НЕ указывайте версии** - они автоматически резолвятся через `dependencyManagement`.

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

### Мониторинг

Приложение предоставляет следующие эндпоинты мониторинга:

- `/actuator/health` - состояние здоровья
- `/actuator/info` - информация о приложении
- `/actuator/metrics` - метрики
- `/actuator/tracing` - трейсинг
- `/swagger-ui.html` - интерактивная документация API
- `/v3/api-docs` - OpenAPI спецификация

### Требования к окружению

- **Java 24** - версия Java для сборки и запуска
- **PostgreSQL** - база данных (порт 5432)
- **Users API** - должен быть запущен на порту 8081 для проверки существования пользователей

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

