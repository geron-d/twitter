# Users API Service

## Введение

**Users API** — это микросервис для управления пользователями в системе Twitter, построенный на Java 24 и Spring Boot 3. Сервис предоставляет REST API для создания, чтения, обновления и деактивации пользователей с поддержкой ролевой модели доступа.

### Основные возможности:
- ✅ CRUD операции для пользователей
- ✅ Ролевая модель (USER, ADMIN, MODERATOR)
- ✅ Безопасное хеширование паролей
- ✅ Валидация данных
- ✅ Пагинация и фильтрация
- ✅ Логирование запросов
- ✅ Защита от удаления последнего администратора
- ✅ OpenAPI/Swagger документация

## Архитектура

### Структура пакетов

```
com.twitter/
├── Application.java              # Главный класс приложения
├── controller/
│   └── UserController.java       # REST контроллер
├── dto/
│   ├── UserRequestDto.java       # DTO для создания пользователя
│   ├── UserResponseDto.java      # DTO для ответа
│   ├── UserUpdateDto.java        # DTO для обновления
│   ├── UserPatchDto.java         # DTO для частичного обновления
│   ├── UserRoleUpdateDto.java    # DTO для обновления роли
│   └── filter/
│       └── UserFilter.java       # Фильтр для поиска
├── entity/
│   └── User.java                 # JPA сущность
├── enums/
│   ├── UserRole.java             # Роли пользователей
│   └── UserStatus.java           # Статусы пользователей
├── exception/
│   └── validation/               # Исключения валидации
│       ├── ValidationException.java           # Базовое исключение
│       ├── UniquenessValidationException.java # Ошибки уникальности
│       ├── BusinessRuleValidationException.java # Бизнес-правила
│       └── FormatValidationException.java     # Формат данных
├── mapper/
│   └── UserMapper.java           # MapStruct маппер
├── repository/
│   └── UserRepository.java       # JPA репозиторий
├── service/
│   ├── UserService.java          # Интерфейс сервиса
│   └── UserServiceImpl.java      # Реализация сервиса
├── util/
│   ├── PasswordUtil.java         # Утилиты для работы с паролями
│   └── PatchDtoFactory.java      # Фабрика для PATCH операций
└── validation/
    ├── UserValidator.java         # Интерфейс валидатора
    └── UserValidatorImpl.java     # Реализация валидатора
```

### Диаграмма компонентов

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   UserController│────│   UserService   │────│  UserRepository │
│                 │    │                 │    │                 │
│ - REST Endpoints│    │ - Business Logic│    │ - Data Access   │
│ - Error Handling│    │ - Orchestration │    │ - Queries       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                       │
         │──────────────────────│                       │
         ▼                      ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   UserValidator │    │   UserMapper    │    │   PostgreSQL    │
│                 │    │                 │    │                 │
│ - Data Validation│   │ - Entity Mapping│    │ - Database      │
│ - Business Rules│    │ - DTO Conversion│    │ - Tables        │
│ - Uniqueness    │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌─────────────────┐
│   Validation    │    │      DTOs       │
│   Exceptions    │    │                 │
│                 │    │ - Request/Response│
│ - Uniqueness    │    │ - Validation    │
│ - Business Rules│    │ - Constraints   │
│ - Format Errors │    │                 │
└─────────────────┘    └─────────────────┘
```

## REST API

### Базовый URL
```
http://localhost:8080/api/v1/users
```

### Эндпоинты

| Метод | Путь | Описание | Параметры | Тело запроса | Ответ |
|-------|------|----------|-----------|--------------|-------|
| `GET` | `/{id}` | Получить пользователя по ID | `id` (UUID) | - | `UserResponseDto` |
| `GET` | `/` | Получить список пользователей | `UserFilter`, `Pageable` | - | `PagedModel<UserResponseDto>` |
| `POST` | `/` | Создать нового пользователя | - | `UserRequestDto` | `UserResponseDto` |
| `PUT` | `/{id}` | Полное обновление пользователя | `id` (UUID) | `UserUpdateDto` | `UserResponseDto` |
| `PATCH` | `/{id}` | Частичное обновление пользователя | `id` (UUID) | `JsonNode` | `UserResponseDto` |
| `PATCH` | `/{id}/inactivate` | Деактивировать пользователя | `id` (UUID) | - | `UserResponseDto` |
| `PATCH` | `/{id}/role` | Обновить роль пользователя | `id` (UUID) | `UserRoleUpdateDto` | `UserResponseDto` |

### Детальное описание эндпоинтов

#### 1. Получить пользователя по ID
```http
GET /api/v1/users/{id}
```

**Параметры:**
- `id` (UUID) - идентификатор пользователя

**Ответы:**
- `200 OK` - пользователь найден
- `404 Not Found` - пользователь не найден

**Пример ответа:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "login": "john_doe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "status": "ACTIVE",
  "role": "USER"
}
```

#### 2. Получить список пользователей
```http
GET /api/v1/users?firstNameContains=John&role=USER&page=0&size=10&sort=login,asc
```

**Параметры запроса:**
- `firstNameContains` (String, optional) - фильтр по имени
- `lastNameContains` (String, optional) - фильтр по фамилии
- `email` (String, optional) - фильтр по email
- `login` (String, optional) - фильтр по логину
- `role` (UserRole, optional) - фильтр по роли
- `page` (int, default: 0) - номер страницы
- `size` (int, default: 10) - размер страницы (максимум: 100)
- `sort` (String, optional) - сортировка

**Ограничения пагинации:**
- Дефолтный размер страницы: 10 элементов
- Максимальный размер страницы: 100 элементов
- При превышении максимального размера автоматически применяется лимит

**Структура ответа PagedModel:**
- `content` - массив объектов `UserResponseDto`
- `page` - метаданные пагинации:
  - `size` - размер страницы
  - `number` - номер страницы (начиная с 0)
  - `totalElements` - общее количество элементов
  - `totalPages` - общее количество страниц

**Пример ответа:**
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "login": "john_doe",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "status": "ACTIVE",
      "role": "USER"
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

#### 3. Создать пользователя
```http
POST /api/v1/users
Content-Type: application/json
```

**Тело запроса:**
```json
{
  "login": "jane_smith",
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@example.com",
  "password": "securePassword123"
}
```

**Валидация:**
- `login` - обязательное, 3-50 символов
- `email` - обязательное, валидный email
- `password` - обязательное, минимум 8 символов

**Ответы:**
- `200 OK` - пользователь создан
- `400 Bad Request` - ошибка валидации
- `409 Conflict` - пользователь с таким логином/email уже существует

#### 4. Обновить пользователя (PUT)
```http
PUT /api/v1/users/{id}
Content-Type: application/json
```

**Тело запроса:**
```json
{
  "login": "jane_smith_updated",
  "firstName": "Jane",
  "lastName": "Smith-Wilson",
  "email": "jane.wilson@example.com",
  "password": "newSecurePassword123"
}
```

**Ответы:**
- `200 OK` - пользователь обновлен
- `404 Not Found` - пользователь не найден
- `400 Bad Request` - ошибка валидации
- `409 Conflict` - конфликт уникальности

#### 5. Частичное обновление пользователя (PATCH)
```http
PATCH /api/v1/users/{id}
Content-Type: application/json
```

**Тело запроса:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith-Wilson"
}
```

**Ответы:**
- `200 OK` - пользователь обновлен
- `404 Not Found` - пользователь не найден
- `400 Bad Request` - ошибка валидации или некорректный JSON Patch
- `409 Conflict` - конфликт уникальности логина/email

#### 6. Деактивировать пользователя
```http
PATCH /api/v1/users/{id}/inactivate
```

**Ответы:**
- `200 OK` - пользователь деактивирован
- `404 Not Found` - пользователь не найден
- `400 Bad Request` - попытка деактивировать последнего администратора

#### 7. Обновить роль пользователя
```http
PATCH /api/v1/users/{id}/role
Content-Type: application/json
```

**Тело запроса:**
```json
{
  "role": "ADMIN"
}
```

**Ответы:**
- `200 OK` - роль обновлена
- `404 Not Found` - пользователь не найден
- `400 Bad Request` - попытка изменить роль последнего администратора

## OpenAPI/Swagger Документация

### Обзор

Сервис включает полную OpenAPI 3.0 документацию, предоставляемую через SpringDoc OpenAPI. Документация содержит интерактивные возможности для тестирования API, детальные схемы данных и примеры запросов/ответов.

### Доступ к документации

#### Swagger UI
- **URL**: `http://localhost:8080/swagger-ui.html`
- **Описание**: Интерактивный интерфейс для изучения и тестирования API
- **Возможности**:
  - Просмотр всех эндпоинтов с детальным описанием
  - Интерактивное тестирование API (Try it out)
  - Просмотр схем данных и валидации
  - Примеры запросов и ответов
  - Автогенерация клиентского кода

#### OpenAPI Спецификация
- **URL**: `http://localhost:8080/v3/api-docs`
- **Описание**: JSON спецификация OpenAPI 3.0
- **Использование**:
  - Генерация клиентских SDK
  - Импорт в инструменты тестирования API (Postman, Insomnia)
  - Интеграция с CI/CD пайплайнами
  - Валидация API контрактов

#### Конфигурация Swagger
- **URL**: `http://localhost:8080/v3/api-docs/swagger-config`
- **Описание**: Конфигурация Swagger UI

### Особенности документации

#### Интерактивные возможности
- **Try it out**: Тестирование всех эндпоинтов прямо в браузере
- **Request Builder**: Визуальный интерфейс для создания запросов
- **Response Viewer**: Отображение ответов с форматированием
- **Schema Explorer**: Просмотр и понимание моделей данных

#### Полное покрытие API
- **Все эндпоинты**: Документация всех 7 API эндпоинтов
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
open http://localhost:8080/swagger-ui.html
```

#### Генерация клиентского кода
```bash
# Использование OpenAPI Generator
openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g java \
  -o ./generated-client

# Генерация TypeScript клиента
openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
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
- **Пароли**: Поля паролей помечены как чувствительные и скрыты в примерах
- **Аутентификация**: Будущие версии будут включать JWT аутентификацию
- **Rate Limiting**: Ограничения API документированы в заголовках ответов
- **Приватность данных**: Обработка персональной информации следует руководящим принципам приватности

### Устранение неполадок

#### Частые проблемы

1. **Swagger UI не загружается**
   - Проверьте, что зависимость SpringDoc правильно добавлена
   - Убедитесь, что приложение запущено на правильном порту
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

### UserService

Основной сервис для работы с пользователями, реализующий следующие операции:

#### Методы сервиса:

1. **`getUserById(UUID id)`**
   - Получает пользователя по идентификатору
   - Возвращает `Optional<UserResponseDto>`
   - Логика: поиск в репозитории и маппинг в DTO

2. **`findAll(UserFilter userFilter, Pageable pageable)`**
   - Получает список пользователей с фильтрацией и пагинацией
   - Возвращает `Page<UserResponseDto>`
   - Логика: построение спецификации из фильтра и маппинг результатов

3. **`createUser(UserRequestDto userRequest)`**
   - Создает нового пользователя
   - Возвращает `UserResponseDto`
   - Логика:
     - Валидация уникальности логина и email
     - Маппинг DTO в сущность
     - Установка статуса ACTIVE и роли USER
     - Хеширование пароля
     - Сохранение в БД

4. **`updateUser(UUID id, UserUpdateDto userDetails)`**
   - Полное обновление пользователя
   - Возвращает `Optional<UserResponseDto>`
   - Логика:
     - Поиск пользователя по ID
     - Валидация уникальности (исключая текущего пользователя)
     - Обновление полей через маппер
     - Хеширование нового пароля (если указан)
     - Сохранение изменений

5. **`patchUser(UUID id, JsonNode patchNode)`**
   - Частичное обновление пользователя
   - Возвращает `Optional<UserResponseDto>`
   - Логика:
     - Поиск пользователя по ID
     - Применение JSON Patch к DTO
     - Валидация результата
     - Проверка уникальности
     - Обновление сущности

6. **`inactivateUser(UUID id)`**
   - Деактивация пользователя
   - Возвращает `Optional<UserResponseDto>`
   - Логика:
     - Проверка, что это не последний активный администратор
     - Установка статуса INACTIVE
     - Логирование операции

7. **`updateUserRole(UUID id, UserRoleUpdateDto roleUpdate)`**
   - Обновление роли пользователя
   - Возвращает `Optional<UserResponseDto>`
   - Логика:
     - Проверка, что нельзя изменить роль последнего администратора
     - Обновление роли
     - Логирование изменения

### Ключевые бизнес-правила:

1. **Уникальность данных:**
   - Логин должен быть уникальным
   - Email должен быть уникальным
   - При обновлении исключается текущий пользователь

2. **Защита администраторов:**
   - Нельзя деактивировать последнего активного администратора
   - Нельзя изменить роль последнего активного администратора

3. **Безопасность паролей:**
   - Пароли хешируются с использованием PBKDF2
   - Используется криптографически безопасная соль
   - Пароли никогда не возвращаются в ответах

4. **Валидация данных:**
   - Все входящие данные валидируются
   - Используется Jakarta Validation
   - Кастомная валидация для бизнес-правил

## Слой валидации

### Архитектура валидации

Сервис использует централизованный слой валидации через `UserValidator`, который обеспечивает:

- **Единообразную валидацию** для всех операций с пользователями
- **Разделение ответственности** между бизнес-логикой и валидацией
- **Типизированные исключения** для различных видов ошибок
- **Интеграцию с Jakarta Validation** для проверки формата данных

### UserValidator

Интерфейс `UserValidator` определяет методы валидации для всех операций с пользователями. Он включает методы для валидации создания, обновления, частичного обновления, проверки уникальности, деактивации администраторов и смены ролей.

### Типы исключений валидации

#### 1. ValidationException (базовое исключение)
Базовое абстрактное исключение для всех типов ошибок валидации. Содержит информацию о типе валидации и контексте ошибки.

#### 2. UniquenessValidationException
Используется для ошибок уникальности логина и email. Содержит информацию о поле, которое нарушает уникальность, и его значении.

**HTTP статус:** `409 Conflict`  
**Content-Type:** `application/problem+json`

**Пример ответа:**
```json
{
  "title": "Uniqueness Validation Error",
  "detail": "User with login 'testuser' already exists",
  "fieldName": "login",
  "fieldValue": "testuser",
  "validationType": "UNIQUENESS",
  "timestamp": "2025-01-21T19:30:00Z"
}
```

#### 3. BusinessRuleValidationException
Используется для нарушений бизнес-правил, таких как попытка деактивировать последнего администратора или изменить его роль. Содержит название нарушенного правила.

**HTTP статус:** `409 Conflict`  
**Content-Type:** `application/problem+json`

**Пример ответа:**
```json
{
  "title": "Business Rule Validation Error",
  "detail": "Business rule 'LAST_ADMIN_DEACTIVATION' violated for context: userId=123e4567-e89b-12d3-a456-426614174000",
  "ruleName": "LAST_ADMIN_DEACTIVATION",
  "validationType": "BUSINESS_RULE",
  "timestamp": "2025-01-21T19:30:00Z"
}
```

#### 4. FormatValidationException
Используется для ошибок формата данных, таких как некорректная длина логина, невалидный email или другие нарушения ограничений Jakarta Validation.

**HTTP статус:** `400 Bad Request`  
**Content-Type:** `application/problem+json`

**Пример ответа:**
```json
{
  "title": "Format Validation Error",
  "detail": "Validation failed for field 'login': size must be between 3 and 50",
  "validationType": "FORMAT",
  "timestamp": "2025-01-21T19:30:00Z"
}
```

### Валидация по операциям

#### Создание пользователя (CREATE)
Проверяется уникальность логина и email среди всех существующих пользователей. При обнаружении дублирования выбрасывается `UniquenessValidationException`.

#### Обновление пользователя (UPDATE)
Проверяется уникальность логина и email среди всех пользователей, исключая текущего обновляемого пользователя. Это позволяет пользователю сохранить свои текущие данные при обновлении других полей.

#### Частичное обновление (PATCH)
Выполняется двухэтапная валидация: сначала проверяется структура JSON Patch, затем применяется Bean Validation к результирующему DTO. Дополнительно проверяется уникальность только тех полей, которые изменяются.

#### Деактивация пользователя (INACTIVATE)
Проверяется бизнес-правило: нельзя деактивировать последнего активного администратора в системе. При попытке деактивации последнего админа выбрасывается `BusinessRuleValidationException`.

#### Смена роли (ROLE_CHANGE)
Проверяется бизнес-правило: нельзя изменить роль последнего активного администратора на любую другую роль. Это гарантирует наличие хотя бы одного администратора в системе.

### Примеры использования

#### Создание пользователя с дублирующимся логином
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "login": "existinguser",
    "email": "new@example.com",
    "password": "password123"
  }'
```

**Ответ (409 Conflict):**
```json
{
  "title": "Uniqueness Validation Error",
  "detail": "User with login 'existinguser' already exists",
  "fieldName": "login",
  "fieldValue": "existinguser",
  "validationType": "UNIQUENESS",
  "timestamp": "2025-01-21T19:30:00Z"
}
```

#### Попытка деактивировать последнего админа
```bash
curl -X PATCH http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000/inactivate
```

**Ответ (409 Conflict):**
```json
{
  "title": "Business Rule Validation Error",
  "detail": "Business rule 'LAST_ADMIN_DEACTIVATION' violated for context: userId=123e4567-e89b-12d3-a456-426614174000",
  "ruleName": "LAST_ADMIN_DEACTIVATION",
  "validationType": "BUSINESS_RULE",
  "timestamp": "2025-01-21T19:30:00Z"
}
```

#### PATCH с некорректным форматом данных
```bash
curl -X PATCH http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Content-Type: application/json" \
  -d '{"login": "ab"}'
```

**Ответ (400 Bad Request):**
```json
{
  "title": "Format Validation Error",
  "detail": "Validation failed for field 'login': size must be between 3 and 50",
  "validationType": "FORMAT",
  "timestamp": "2025-01-21T19:30:00Z"
}
```

## Работа с базой данных

### Сущность User

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String login;
    
    private String firstName;
    private String lastName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Column(nullable = false)
    private String passwordSalt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
}
```

### Таблица users

| Поле | Тип | Ограничения | Описание |
|------|-----|-------------|----------|
| `id` | UUID | PRIMARY KEY, NOT NULL | Уникальный идентификатор |
| `login` | VARCHAR | UNIQUE, NOT NULL | Логин пользователя |
| `first_name` | VARCHAR | NULL | Имя |
| `last_name` | VARCHAR | NULL | Фамилия |
| `email` | VARCHAR | UNIQUE, NOT NULL | Email адрес |
| `password_hash` | VARCHAR | NOT NULL | Хеш пароля |
| `password_salt` | VARCHAR | NOT NULL | Соль для хеширования |
| `status` | VARCHAR | NOT NULL | Статус (ACTIVE/INACTIVE) |
| `role` | VARCHAR | NOT NULL | Роль (USER/ADMIN/MODERATOR) |

### UserRepository

Интерфейс репозитория расширяет `JpaRepository` и `JpaSpecificationExecutor`:

```java
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    long countByRoleAndStatus(UserRole role, UserStatus status);
    boolean existsByLogin(String login);
    boolean existsByEmail(String email);
    boolean existsByLoginAndIdNot(String login, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);
}
```

### Спецификации для фильтрации

Фильтрация реализована через Spring Data JPA Specifications:

```java
public record UserFilter(String firstNameContains, String lastNameContains, 
                        String email, String login, UserRole role) {
    public Specification<User> toSpecification() {
        return firstNameContainsSpec()
            .and(lastNameContainsSpec())
            .and(emailSpec())
            .and(loginSpec())
            .and(roleSpec());
    }
}
```

## Примеры использования

### Создание пользователя

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "login": "newuser",
    "firstName": "New",
    "lastName": "User",
    "email": "newuser@example.com",
    "password": "securePassword123"
  }'
```

### Получение пользователей с фильтрацией

```bash
curl "http://localhost:8080/api/v1/users?firstNameContains=John&role=USER&page=0&size=10"
```

### Обновление пользователя

```bash
curl -X PUT http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Content-Type: application/json" \
  -d '{
    "login": "updateduser",
    "firstName": "Updated",
    "lastName": "User",
    "email": "updated@example.com",
    "password": "newPassword123"
  }'
```

### Частичное обновление

```bash
curl -X PATCH http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "New Name"
  }'
```

### Деактивация пользователя

```bash
curl -X PATCH http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000/inactivate
```

### Обновление роли

```bash
curl -X PATCH http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000/role \
  -H "Content-Type: application/json" \
  -d '{
    "role": "ADMIN"
  }'
```

## UML Диаграммы

### Диаграмма классов

```
┌─────────────────────────────────────────────────────────────┐
│                        UserController                       │
├─────────────────────────────────────────────────────────────┤
│ - userService: UserService                                  │
├─────────────────────────────────────────────────────────────┤
│ + getUserById(id: UUID): ResponseEntity<UserResponseDto>   │
│ + findAll(filter: UserFilter, pageable: Pageable):         │
│   PagedModel<UserResponseDto>                              │
│ + createUser(request: UserRequestDto): UserResponseDto     │
│ + updateUser(id: UUID, details: UserUpdateDto):            │
│   ResponseEntity<UserResponseDto>                          │
│ + patchUser(id: UUID, patch: JsonNode):                    │
│   ResponseEntity<UserResponseDto>                          │
│ + inactivateUser(id: UUID): ResponseEntity<UserResponseDto>│
│ + updateUserRole(id: UUID, role: UserRoleUpdateDto):       │
│   ResponseEntity<UserResponseDto>                          │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ uses
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                        UserService                          │
├─────────────────────────────────────────────────────────────┤
│ + getUserById(id: UUID): Optional<UserResponseDto>         │
│ + findAll(filter: UserFilter, pageable: Pageable):         │
│   Page<UserResponseDto>                                     │
│ + createUser(request: UserRequestDto): UserResponseDto     │
│ + updateUser(id: UUID, details: UserUpdateDto):            │
│   Optional<UserResponseDto>                                │
│ + patchUser(id: UUID, patch: JsonNode):                    │
│   Optional<UserResponseDto>                                │
│ + inactivateUser(id: UUID): Optional<UserResponseDto>      │
│ + updateUserRole(id: UUID, role: UserRoleUpdateDto):       │
│   Optional<UserResponseDto>                                │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ implements
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      UserServiceImpl                        │
├─────────────────────────────────────────────────────────────┤
│ - objectMapper: ObjectMapper                                │
│ - userMapper: UserMapper                                    │
│ - userRepository: UserRepository                            │
│ - userValidator: UserValidator                             │
│ - patchDtoFactory: PatchDtoFactory                         │
├─────────────────────────────────────────────────────────────┤
│ + getUserById(id: UUID): Optional<UserResponseDto>         │
│ + findAll(filter: UserFilter, pageable: Pageable):         │
│   Page<UserResponseDto>                                     │
│ + createUser(request: UserRequestDto): UserResponseDto      │
│ + updateUser(id: UUID, details: UserUpdateDto):            │
│   Optional<UserResponseDto>                                │
│ + patchUser(id: UUID, patch: JsonNode):                    │
│   Optional<UserResponseDto>                                │
│ + inactivateUser(id: UUID): Optional<UserResponseDto>      │
│ + updateUserRole(id: UUID, role: UserRoleUpdateDto):       │
│   Optional<UserResponseDto>                                │
│ - setPassword(user: User, password: String): void          │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ uses
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      UserValidator                           │
├─────────────────────────────────────────────────────────────┤
│ + validateForCreate(request: UserRequestDto): void         │
│ + validateForUpdate(id: UUID, update: UserUpdateDto): void │
│ + validateForPatch(id: UUID, patch: JsonNode): void         │
│ + validateForPatchWithDto(id: UUID, patch: UserPatchDto):   │
│   void                                                      │
│ + validateUniqueness(login: String, email: String,         │
│   excludeUserId: UUID): void                               │
│ + validateAdminDeactivation(id: UUID): void                │
│ + validateRoleChange(id: UUID, newRole: UserRole): void    │
│ + validatePatchData(patch: JsonNode): void                 │
│ + validatePatchConstraints(patch: UserPatchDto): void       │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ uses
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      UserRepository                         │
├─────────────────────────────────────────────────────────────┤
│ + countByRoleAndStatus(role: UserRole, status: UserStatus):│
│   long                                                      │
│ + existsByLogin(login: String): boolean                    │
│ + existsByEmail(email: String): boolean                    │
│ + existsByLoginAndIdNot(login: String, id: UUID): boolean  │
│ + existsByEmailAndIdNot(email: String, id: UUID): boolean  │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ extends
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    JpaRepository<User, UUID>                │
│                    JpaSpecificationExecutor<User>           │
└─────────────────────────────────────────────────────────────┘
```

## Конфигурация

### Зависимости

Основные зависимости проекта:

- **Spring Boot 3.x** - основной фреймворк
- **Spring Data JPA** - работа с базой данных
- **Spring Web** - REST API
- **Spring Validation** - валидация данных
- **SpringDoc OpenAPI** - документация API и Swagger UI
- **MapStruct** - маппинг объектов
- **Lombok** - генерация кода
- **PostgreSQL** - база данных
- **Micrometer Tracing** - трейсинг

## Запуск и развертывание

### Локальный запуск

1. Убедитесь, что PostgreSQL запущен на порту 5432
2. Создайте базу данных `twitter`
3. Запустите приложение:

```bash
./gradlew bootRun
```

### Docker

```bash
docker build -t users-api .
docker run -p 8080:8080 users-api
```

### Мониторинг

Приложение предоставляет следующие эндпоинты мониторинга:

- `/actuator/health` - состояние здоровья
- `/actuator/info` - информация о приложении
- `/actuator/metrics` - метрики
- `/actuator/tracing` - трейсинг
- `/swagger-ui.html` - интерактивная документация API
- `/v3/api-docs` - OpenAPI спецификация

## Безопасность

### Хеширование паролей

- Алгоритм: PBKDF2WithHmacSHA256
- Итерации: 10,000
- Длина ключа: 256 бит
- Размер соли: 16 байт

### Валидация

- Все входящие данные валидируются
- Используется Jakarta Validation
- Кастомная валидация для бизнес-правил

### Логирование

- Все запросы логируются через `@LoggableRequest`
- Пароли скрываются в логах
- Подробное логирование операций

## Тестирование

Проект включает:

- **Unit тесты** для всех компонентов
- **Integration тесты** с TestContainers
- **Тесты валидации** с покрытием всех сценариев
- **Тесты безопасности** паролей
- **Тесты исключений** валидации

Запуск тестов:

```bash
./gradlew test
```