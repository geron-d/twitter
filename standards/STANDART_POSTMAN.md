# Стандарты написания Postman коллекций

## 1. Общая структура коллекций

### 1.1 Организация папок

Postman коллекции должны быть организованы в логические папки, соответствующие структуре API:

```
postman/
├── {api-name}/
│   ├── twitter-{api-name}.postman_collection.json
│   └── twitter-{api-name}.postman_environment.json
```

**Пример:**
```
postman/
├── users-api/
│   ├── twitter-users-api.postman_collection.json
│   └── twitter-users-api.postman_environment.json
├── tweet-api/
│   ├── twitter-tweet-api.postman_collection.json
│   └── twitter-tweet-api.postman_environment.json
```

### 1.2 Структура вложенности коллекции

Внутри коллекции должна быть папка с именем API (например, `users-api`, `tweets-api`), которая содержит все запросы:

```json
{
  "item": [
    {
      "name": "{api-name}-api",
      "description": "API for managing {resource} in the Twitter system",
      "item": [
        // запросы здесь
      ]
    }
  ]
}
```

### 1.3 Именование коллекций и запросов

**Коллекции:**
- Формат: `twitter-{api-name}.postman_collection.json`
- Пример: `twitter-users-api.postman_collection.json`

**Окружения:**
- Формат: `twitter-{api-name}.postman_environment.json`
- Имя окружения: `{api-name}-env` (например, `users-env`, `tweets-env`)

**Запросы:**
- Имена запросов в **lowercase с пробелами**
- Описательные и понятные названия
- Соответствуют действию, которое выполняет запрос

**Примеры имен запросов:**
- `get user by id`
- `check user exists`
- `get users with filtering`
- `create user`
- `update user (complete)`
- `update user role`
- `deactivate user`
- `create tweet`

## 2. Метаданные коллекции

### 2.1 Поля info

Каждая коллекция должна содержать секцию `info` с полным описанием:

```json
{
  "info": {
    "_postman_id": "twitter-{api-name}-collection",
    "name": "twitter",
    "description": "REST API for {resource} management in the Twitter microservices system.\n\nThis API provides comprehensive {resource} management capabilities including:\n- {capability 1}\n- {capability 2}\n- {capability 3}\n\n## Authentication\nCurrently, the API does not require authentication for basic operations.\nFuture versions will implement JWT-based authentication.\n\n## Rate Limiting\nAPI requests are subject to rate limiting to ensure system stability.\nPlease refer to response headers for current rate limit information.\n\n## Error Handling\nThe API uses standard HTTP status codes and follows RFC 7807 Problem Details\nfor error responses, providing detailed information about validation failures\nand business rule violations.",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    "_exporter_id": "twitter-{api-name}-api"
  }
}
```

**Обязательные поля:**
- `_postman_id`: уникальный идентификатор коллекции
- `name`: имя коллекции (обычно "twitter")
- `description`: подробное описание API с использованием Markdown
- `schema`: версия схемы Postman Collection
- `_exporter_id`: идентификатор экспортера

### 2.2 Описание API

Описание должно включать:
- Назначение API
- Основные возможности
- Информацию об аутентификации
- Информацию о rate limiting
- Информацию об обработке ошибок

## 3. Структура запросов

### 3.1 Именование запросов

**Правила именования:**
- Все в **lowercase**
- Использовать **пробелы** между словами
- Описательные названия, отражающие действие
- Для операций обновления указывать тип: `(complete)` для PUT, без указания для PATCH

**Примеры:**
- ✅ `get user by id`
- ✅ `create user`
- ✅ `update user (complete)`
- ✅ `update user role`
- ✅ `deactivate user`
- ❌ `GetUserById`
- ❌ `get-user-by-id`
- ❌ `GET_USER_BY_ID`

### 3.2 Описания запросов

Каждый запрос должен иметь поле `description`, которое:
- Объясняет назначение запроса
- Описывает основные действия
- Указывает на валидацию и бизнес-правила
- Упоминает интеграции с другими сервисами (если есть)

**Пример:**
```json
{
  "name": "create user",
  "request": {
    "description": "Creates a new user in the system. Password is securely hashed. Status is set to ACTIVE and role to USER by default."
  }
}
```

### 3.3 HTTP методы и соответствие REST

**Соответствие HTTP методов операциям:**

| HTTP метод | Операция | Пример |
|------------|----------|--------|
| `GET` | Получение данных | `get user by id`, `get users with filtering` |
| `POST` | Создание ресурса | `create user`, `create tweet` |
| `PUT` | Полное обновление | `update user (complete)` |
| `PATCH` | Частичное обновление | `update user role`, `deactivate user` |
| `DELETE` | Удаление ресурса | `delete user` (если применимо) |

## 4. URL и переменные

### 4.1 Базовый URL

**Всегда использовать переменную `{{baseUrl}}`** для базового URL:

```json
{
  "url": {
    "raw": "{{baseUrl}}/api/v1/users",
    "host": ["{{baseUrl}}"],
    "path": ["api", "v1", "users"]
  }
}
```

### 4.2 Структура путей

Пути должны соответствовать структуре контроллера:

**Формат:** `/api/v1/{resource}`

**Примеры:**
- `/api/v1/users`
- `/api/v1/users/{userId}`
- `/api/v1/users/{userId}/exists`
- `/api/v1/users/{userId}/role`
- `/api/v1/users/{userId}/inactivate`
- `/api/v1/tweets`

### 4.3 Path параметры

Path параметры должны использовать переменные окружения:

```json
{
  "url": {
    "raw": "{{baseUrl}}/api/v1/users/{{userId}}",
    "path": ["api", "v1", "users", "{{userId}}"]
  }
}
```

**Именование переменных:**
- `{{userId}}` - для идентификатора пользователя
- `{{tweetId}}` - для идентификатора твита
- `{{id}}` - для общего идентификатора ресурса

### 4.4 Query параметры

Query параметры должны:
- Иметь описания
- Использовать переменные окружения для сложных значений (JSON)
- Быть явно указаны в секции `query`

**Пример:**
```json
{
  "url": {
    "raw": "{{baseUrl}}/api/v1/users?userFilter={{userFilter}}&pageable={{pageable}}",
    "query": [
      {
        "key": "userFilter",
        "value": "{{userFilter}}",
        "description": "Filter criteria for user search"
      },
      {
        "key": "pageable",
        "value": "{{pageable}}",
        "description": "Pagination parameters (page, size, sorting)"
      }
    ]
  }
}
```

## 5. Заголовки (Headers)

### 5.1 Обязательные заголовки

**Для всех запросов:**
- `Accept: application/json`

**Для запросов с телом (POST, PUT, PATCH):**
- `Content-Type: application/json`
- `Accept: application/json`

**Пример:**
```json
{
  "header": [
    {
      "key": "Content-Type",
      "value": "application/json",
      "type": "text"
    },
    {
      "key": "Accept",
      "value": "application/json",
      "type": "text"
    }
  ]
}
```

### 5.2 Условные заголовки

**Authorization (для будущего использования):**
```json
{
  "key": "Authorization",
  "value": "Bearer {{auth_token}}",
  "type": "text",
  "disabled": true
}
```

Заголовок должен быть отключен (`disabled: true`), если аутентификация еще не реализована.

## 6. Тела запросов (Request Body)

### 6.1 Формат JSON

Все тела запросов должны быть в формате JSON с использованием `raw` mode:

```json
{
  "body": {
    "mode": "raw",
    "raw": "{\n  \"login\": \"jane_smith\",\n  \"email\": \"jane.smith@example.com\",\n  \"firstName\": \"Jane\",\n  \"lastName\": \"Smith\",\n  \"password\": \"securePassword123\"\n}",
    "options": {
      "raw": {
        "language": "json"
      }
    }
  }
}
```

### 6.2 Использование переменных окружения

Тела запросов должны использовать переменные окружения для тестовых данных:

**Пример:**
```json
{
  "body": {
    "mode": "raw",
    "raw": "{\n  \"content\": \"{{testTweet_content}}\",\n  \"userId\": \"{{userId}}\"\n}",
    "options": {
      "raw": {
        "language": "json"
      }
    }
  }
}
```

### 6.3 Соответствие DTO

Тела запросов должны точно соответствовать DTO из контроллера:

- `UserRequestDto` для создания пользователя
- `UserUpdateDto` для полного обновления
- `UserRoleUpdateDto` для обновления роли
- `JsonNode` для частичного обновления (PATCH)

## 7. Примеры ответов (Responses)

### 7.1 Обязательные примеры

Каждый запрос должен содержать примеры ответов для всех возможных сценариев:

**Успешные ответы:**
- `200 OK` - успешное выполнение
- `201 Created` - ресурс создан
- `204 No Content` - успешное выполнение без тела ответа

**Ошибки:**
- `400 Bad Request` - ошибки валидации и бизнес-правил
- `404 Not Found` - ресурс не найден
- `409 Conflict` - конфликт (например, дублирование уникальных полей)

### 7.2 Структура примеров ответов

Каждый пример должен содержать:

```json
{
  "name": "descriptive name",
  "originalRequest": {
    // копия исходного запроса
  },
  "status": "Status Text",
  "code": 200,
  "_postman_previewlanguage": "json",
  "header": [
    {
      "key": "Content-Type",
      "value": "application/json"
    }
  ],
  "cookie": [],
  "body": "{\n  // JSON тело ответа\n}"
}
```

### 7.3 Именование примеров

**Формат:** `{scenario description}`

**Примеры:**
- `user found`
- `user not found`
- `user created`
- `validation error`
- `conflict error`
- `business rule error`
- `user deactivated`

### 7.4 Content-Type для разных типов ответов

**Успешные ответы:**
```json
{
  "key": "Content-Type",
  "value": "application/json"
}
```

**Ошибки (RFC 7807 Problem Details):**
```json
{
  "key": "Content-Type",
  "value": "application/problem+json"
}
```

### 7.5 Формат ошибок

Все ошибки должны следовать RFC 7807 Problem Details:

```json
{
  "type": "https://example.com/errors/{error-type}",
  "title": "Error Title",
  "status": 400,
  "detail": "Detailed error message",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

**Дополнительные поля для специфичных ошибок:**
- `fieldName` - для ошибок валидации полей
- `fieldValue` - значение поля, вызвавшего ошибку
- `ruleName` - для ошибок бизнес-правил
- `constraintName` - для ошибок ограничений

**Примеры типов ошибок:**
- `validation-error` - ошибки валидации
- `format-validation` - ошибки формата
- `business-rule-validation` - ошибки бизнес-правил
- `uniqueness-validation` - ошибки уникальности
- `not-found` - ресурс не найден

## 8. Переменные окружения

### 8.1 Структура файла окружения

```json
{
  "id": "twitter-{api-name}-env",
  "name": "{api-name}-env",
  "values": [
    // переменные здесь
  ],
  "_postman_variable_scope": "environment",
  "_postman_exported_at": "2025-01-27T19:45:00.000Z",
  "_postman_exported_using": "Postman/10.0.0"
}
```

### 8.2 Обязательные переменные

**baseUrl:**
```json
{
  "key": "baseUrl",
  "value": "http://localhost:{port}",
  "type": "default",
  "enabled": true
}
```

**Идентификаторы ресурсов:**
```json
{
  "key": "userId",
  "value": "123e4567-e89b-12d3-a456-426614174000",
  "type": "default",
  "enabled": true
}
```

### 8.3 Тестовые данные

Переменные для тестовых данных должны использовать динамические значения Postman:

**Примеры:**
```json
{
  "key": "testUser_login",
  "value": "test_user_{{$timestamp}}",
  "type": "default",
  "enabled": true
},
{
  "key": "testUser_email",
  "value": "test{{$timestamp}}@example.com",
  "type": "default",
  "enabled": true
}
```

**Доступные динамические переменные:**
- `{{$timestamp}}` - текущая временная метка
- `{{$randomUUID}}` - случайный UUID
- `{{$randomEmail}}` - случайный email
- `{{$randomFullName}}` - случайное полное имя

### 8.4 Секретные переменные

Секретные переменные (токены, ключи API) должны иметь `type: "secret"` и быть отключены по умолчанию:

```json
{
  "key": "auth_token",
  "value": "",
  "type": "secret",
  "enabled": false
},
{
  "key": "api_key",
  "value": "",
  "type": "secret",
  "enabled": false
}
```

### 8.5 Переменные коллекции

Коллекция также может содержать переменные уровня коллекции:

```json
{
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8081",
      "type": "string"
    },
    {
      "key": "userId",
      "value": "123e4567-e89b-12d3-a456-426614174000",
      "type": "string"
    }
  ]
}
```

**Приоритет переменных:**
1. Переменные окружения (наивысший приоритет)
2. Переменные коллекции
3. Глобальные переменные

## 9. Организация файлов

### 9.1 Структура папок

```
postman/
├── {api-name}/
│   ├── twitter-{api-name}.postman_collection.json
│   └── twitter-{api-name}.postman_environment.json
├── POSTMAN_GUIDE.md
└── STANDART_POSTMAN.md
```

### 9.2 Именование файлов

**Коллекции:**
- Формат: `twitter-{api-name}.postman_collection.json`
- Пример: `twitter-users-api.postman_collection.json`

**Окружения:**
- Формат: `twitter-{api-name}.postman_environment.json`
- Пример: `twitter-users-api.postman_environment.json`

### 9.3 Связь коллекций и окружений

- Каждая коллекция должна иметь соответствующее окружение
- Имя окружения должно быть понятным: `{api-name}-env`
- Окружение должно содержать все необходимые переменные для коллекции

## 10. Соответствие контроллерам

### 10.1 Маппинг методов контроллера на запросы

Каждый метод контроллера должен иметь соответствующий запрос в Postman коллекции:

| Метод контроллера | HTTP метод | Имя запроса | Путь |
|-------------------|------------|-------------|------|
| `existsUser` | `GET` | `check user exists` | `/api/v1/users/{userId}/exists` |
| `getUserById` | `GET` | `get user by id` | `/api/v1/users/{id}` |
| `findAll` | `GET` | `get users with filtering` | `/api/v1/users` |
| `createUser` | `POST` | `create user` | `/api/v1/users` |
| `updateUser` | `PUT` | `update user (complete)` | `/api/v1/users/{id}` |
| `patchUser` | `PATCH` | `patch user` | `/api/v1/users/{id}` |
| `inactivateUser` | `PATCH` | `deactivate user` | `/api/v1/users/{id}/inactivate` |
| `updateUserRole` | `PATCH` | `update user role` | `/api/v1/users/{id}/role` |

### 10.2 Соответствие DTO

Тела запросов должны точно соответствовать DTO из контроллера:

**UserRequestDto:**
```json
{
  "login": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "password": "string"
}
```

**UserUpdateDto:**
```json
{
  "login": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "password": "string"
}
```

**UserRoleUpdateDto:**
```json
{
  "role": "USER|ADMIN|MODERATOR"
}
```

### 10.3 Соответствие статус-кодам

Статус-коды в примерах ответов должны соответствовать возвращаемым значениям контроллера:

- `ResponseEntity.ok()` → `200 OK`
- `ResponseEntity.notFound()` → `404 Not Found`
- `ResponseEntity.status(HttpStatus.CREATED)` → `201 Created`
- Ошибки валидации → `400 Bad Request`
- Конфликты → `409 Conflict`

## 11. Дополнительные рекомендации

### 11.1 Порядок запросов

Рекомендуемый порядок запросов в коллекции:
1. GET запросы (чтение данных)
2. POST запросы (создание)
3. PUT запросы (полное обновление)
4. PATCH запросы (частичное обновление)
5. DELETE запросы (удаление, если применимо)

### 11.2 Группировка по функциональности

Запросы могут быть сгруппированы в подпапки по функциональности:
- User Management
- Authentication
- Tweet Management
- и т.д.

### 11.3 Документация

Каждая коллекция должна быть самодостаточной и содержать:
- Полное описание в `info.description`
- Описания для каждого запроса
- Описания для query параметров
- Примеры для всех сценариев использования

### 11.4 Тестирование

При создании коллекции убедитесь, что:
- Все запросы работают с указанными переменными окружения
- Примеры ответов соответствуют реальным ответам API
- Все возможные сценарии покрыты примерами
- Ошибки документированы с правильными статус-кодами

## 12. Чек-лист создания коллекции

Перед финализацией коллекции проверьте:

- [ ] Коллекция имеет правильное имя и структуру папок
- [ ] Все метаданные (`info`) заполнены корректно
- [ ] Все запросы имеют описательные имена в lowercase с пробелами
- [ ] Каждый запрос имеет описание
- [ ] URL используют переменную `{{baseUrl}}`
- [ ] Path параметры используют переменные окружения
- [ ] Query параметры имеют описания
- [ ] Заголовки установлены корректно (Content-Type, Accept)
- [ ] Тела запросов соответствуют DTO
- [ ] Каждый запрос имеет примеры для всех сценариев
- [ ] Примеры ответов имеют правильные Content-Type
- [ ] Ошибки следуют RFC 7807 Problem Details
- [ ] Окружение содержит все необходимые переменные
- [ ] Секретные переменные имеют `type: "secret"`
- [ ] Файлы названы согласно стандарту
- [ ] Все методы контроллера покрыты запросами

---

**Версия документа:** 1.0  
**Дата создания:** 2025-01-27  
**Основано на анализе:** UserController.java, users-api и tweet-api коллекции

