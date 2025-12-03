# README Standards for Twitter Microservices Project

## Overview

This document defines the standards and structure for README.md files across the Twitter microservices project.

**Key Principles:**
- README files should be written in Russian (русский язык)
- All README files must follow the standard structure defined in this document
- Documentation should be comprehensive but concise
- Include practical examples and code snippets
- Use consistent formatting and markdown syntax

**Technology Stack:**
- Java 24
- Spring Boot 3.5.5
- Gradle (Multi-module project)
- PostgreSQL
- MapStruct
- Lombok
- OpenAPI/Swagger

---

## 1. General Structure

### 1.1 Required Sections Order

Every README.md file must follow this structure (sections marked as optional can be omitted if not applicable):

1. **Title and Introduction** (обязательно)
2. **Основные возможности** (обязательно)
3. **Архитектура** (обязательно)
4. **REST API** (обязательно для API сервисов, опционально для библиотек)
5. **OpenAPI/Swagger Документация** (обязательно для API сервисов)
6. **Бизнес-логика** (обязательно)
7. **Слой валидации** (обязательно для сервисов с валидацией)
8. **Работа с базой данных** (обязательно для сервисов с БД)
9. **Интеграция** (опционально, только если есть интеграции)
10. **Примеры использования** (обязательно)
11. **Конфигурация** (обязательно)
12. **Запуск и развертывание** (обязательно)
13. **Безопасность** (обязательно)
14. **Тестирование** (обязательно)
15. **UML Диаграммы** (опционально, рекомендуется для сложных сервисов)

### 1.2 Title Format

```markdown
# [Service Name] Service
```

or

```markdown
# [Library Name] Library
```

**Examples:**
- `# Users API Service`
- `# Tweet API Service`
- `# Twitter Common Library`

---

## 2. Section Details

### 2.1 Введение (Introduction)

**Format:**
```markdown
## Введение

**[Service Name]** — это [тип компонента] для [назначение] в системе Twitter, построенный на Java 24 и Spring Boot 3. [Краткое описание функциональности].
```

**Requirements:**
- Must mention Java 24 and Spring Boot 3
- Must describe the purpose clearly
- Should be 2-3 sentences

**Example:**
```markdown
## Введение

**Users API** — это микросервис для управления пользователями в системе Twitter, построенный на Java 24 и Spring Boot 3. Сервис предоставляет REST API для создания, чтения, обновления и деактивации пользователей с поддержкой ролевой модели доступа.
```

### 2.2 Основные возможности

**Format:**
```markdown
### Основные возможности:

- ✅ [Feature 1]
- ✅ [Feature 2]
- ✅ [Feature 3]
```

**Requirements:**
- Use checkmark emoji (✅) for each feature
- List 5-10 key features
- Be specific and concise
- Use present tense

**Example:**
```markdown
### Основные возможности:

- ✅ CRUD операции для пользователей
- ✅ Ролевая модель (USER, ADMIN, MODERATOR)
- ✅ Безопасное хеширование паролей
- ✅ Валидация данных
- ✅ Пагинация и фильтрация
```

### 2.3 Архитектура

**Required Subsections:**
1. **Структура пакетов** - ASCII tree diagram
2. **Диаграмма компонентов** - ASCII diagram showing relationships

**Format for Package Structure:**
```markdown
### Структура пакетов

```
com.twitter/
├── Application.java              # Главный класс приложения
├── controller/
│   └── [Entity]Controller.java   # REST контроллер
├── service/
│   ├── [Entity]Service.java      # Интерфейс сервиса
│   └── [Entity]ServiceImpl.java # Реализация сервиса
└── ...
```
```

**Format for Component Diagram:**
```markdown
### Диаграмма компонентов

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Controller    │────│     Service     │────│   Repository    │
│                 │    │                 │    │                 │
│ - REST Endpoints│    │ - Business Logic│    │ - Data Access   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```
```

**Requirements:**
- Use ASCII art for diagrams
- Include comments in package structure
- Show relationships between components
- Use consistent box drawing characters (┌─┐│└┘)

### 2.4 REST API

**Required Subsections:**
1. **Базовый URL**
2. **Эндпоинты** (table format)
3. **Детальное описание эндпоинтов**

**Format for Base URL:**
```markdown
### Базовый URL

```
http://localhost:[PORT]/api/v1/[resource]
```
```

**Format for Endpoints Table:**
```markdown
| Метод | Путь | Описание | Параметры | Тело запроса | Ответ |
|-------|------|----------|-----------|--------------|-------|
| `GET` | `/{id}` | Получить по ID | `id` (UUID) | - | `ResponseDto` |
| `POST` | `/` | Создать | - | `RequestDto` | `ResponseDto` |
```

**Format for Detailed Endpoint Description:**
```markdown
#### 1. [Operation Name]

```http
[METHOD] /api/v1/[path]
Content-Type: application/json
```

**Параметры:**
- `param` (Type) - описание

**Тело запроса:**
```json
{
  "field": "value"
}
```

**Валидация:**
- `field` - требования

**Ответы:**
- `200 OK` - успех
- `400 Bad Request` - ошибка валидации

**Пример ответа:**
```json
{
  "id": "...",
  "field": "value"
}
```
```

### 2.5 OpenAPI/Swagger Документация

**Required Subsections:**
1. **Обзор**
2. **Доступ к документации** (Swagger UI, OpenAPI Spec, Config)
3. **Особенности документации**
4. **Конфигурация**
5. **Примеры использования**
6. **Безопасность документации** (optional)
7. **Устранение неполадок** (optional)

**Format:**
```markdown
## OpenAPI/Swagger Документация

### Обзор

Сервис включает полную OpenAPI 3.0 документацию, предоставляемую через SpringDoc OpenAPI. Документация содержит интерактивные возможности для тестирования API, детальные схемы данных и примеры запросов/ответов.

### Доступ к документации

#### Swagger UI
- **URL**: `http://localhost:[PORT]/swagger-ui.html`
- **Описание**: Интерактивный интерфейс для изучения и тестирования API
- **Возможности**:
  - Просмотр всех эндпоинтов с детальным описанием
  - Интерактивное тестирование API (Try it out)
  - Просмотр схем данных и валидации
```

### 2.6 Бизнес-логика

**Required Subsections:**
1. **[Entity]Service** - описание сервиса
2. **Методы сервиса** - список методов с описанием
3. **Ключевые бизнес-правила** - нумерованный список

**Format:**
```markdown
## Бизнес-логика

### [Entity]Service

Основной сервис для работы с [entities], реализующий следующие операции:

#### Методы сервиса:

1. **`methodName(params)`**
   - Краткое описание
   - Возвращает `ReturnType`
   - Логика:
     - Шаг 1
     - Шаг 2

### Ключевые бизнес-правила:

1. **Правило 1:**
   - Описание

2. **Правило 2:**
   - Описание
```

### 2.7 Слой валидации

**Required Subsections:**
1. **Архитектура валидации**
2. **[Entity]Validator** - описание валидатора
3. **Типы исключений валидации** - описание каждого типа
4. **Валидация по операциям** - описание для каждой операции

**Format for Exception Types:**
```markdown
#### 1. [ExceptionName]

Используется для [назначение].

**HTTP статус:** `[STATUS]`  
**Content-Type:** `application/problem+json`

**Пример ответа:**
```json
{
  "type": "...",
  "title": "...",
  "status": [STATUS],
  "detail": "...",
  "timestamp": "..."
}
```
```

### 2.8 Работа с базой данных

**Required Subsections:**
1. **Сущность [Entity]** (optional, if using JPA)
2. **Таблица [table_name]** - таблица с полями
3. **[Entity]Repository** - описание репозитория
4. **Спецификации для фильтрации** (optional)

**Format for Table:**
```markdown
### Таблица [table_name]

| Поле | Тип | Ограничения | Описание |
|------|-----|-------------|----------|
| `id` | UUID | PRIMARY KEY, NOT NULL | Уникальный идентификатор |
| `field` | VARCHAR | NOT NULL | Описание |
```

### 2.9 Интеграция

**Required Subsections:**
1. **Архитектура интеграции**
2. **Компоненты интеграции**
3. **Процесс [operation]**
4. **Обработка ошибок**

**Format:**
```markdown
## Интеграция с [service-name]

### Архитектура интеграции

[Service] интегрируется с [Other Service] через [method] для [purpose].

### Компоненты интеграции

#### 1. [ComponentName]

Описание компонента

**Конфигурация:**
- Параметр 1: значение
- Параметр 2: значение
```

### 2.10 Примеры использования

**Format:**
```markdown
## Примеры использования

### [Operation Name]

```bash
curl -X [METHOD] http://localhost:[PORT]/api/v1/[path] \
  -H "Content-Type: application/json" \
  -d '{
    "field": "value"
  }'
```

**Ответ ([STATUS]):**

```json
{
  "field": "value"
}
```
```

**Requirements:**
- Use curl commands for HTTP examples
- Include request and response examples
- Show both success and error cases when relevant
- Format JSON properly

### 2.11 Конфигурация

**Required Subsections:**
1. **Зависимости** - список основных зависимостей
2. **Управление зависимостями** - описание системы управления версиями

**Format:**
```markdown
## Конфигурация

### Зависимости

Основные зависимости проекта:

- **Spring Boot 3.x** - основной фреймворк
- **Spring Data JPA** - работа с базой данных
- **Spring Web** - REST API
- **SpringDoc OpenAPI** - документация API и Swagger UI
- **MapStruct** - маппинг объектов
- **Lombok** - генерация кода
- **PostgreSQL** - база данных

### Управление зависимостями

Сервис использует **централизованное управление версиями** через `dependencyManagement` в корневом `build.gradle`.

**Важно**: При добавлении новых зависимостей в `build.gradle` сервиса **НЕ указывайте версии** - они автоматически резолвятся через `dependencyManagement`.
```

### 2.12 Запуск и развертывание

**Required Subsections:**
1. **Локальный запуск** - пошаговая инструкция
2. **Docker** (optional) - команды Docker
3. **Мониторинг** (optional) - эндпоинты мониторинга

**Format:**
```markdown
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
docker build -t [service-name] .
docker run -p [PORT]:[PORT] [service-name]
```

### Мониторинг

Приложение предоставляет следующие эндпоинты мониторинга:

- `/actuator/health` - состояние здоровья
- `/actuator/info` - информация о приложении
- `/swagger-ui.html` - интерактивная документация API
```

### 2.13 Безопасность

**Required Subsections:**
1. **Хеширование паролей** (if applicable)
2. **Валидация**
3. **Логирование**

**Format:**
```markdown
## Безопасность

### Хеширование паролей

- Алгоритм: [ALGORITHM]
- Итерации: [NUMBER]
- Длина ключа: [BITS] бит
- Размер соли: [BYTES] байт

### Валидация

- Все входящие данные валидируются
- Используется Jakarta Validation
- Кастомная валидация для бизнес-правил

### Логирование

- Все запросы логируются через `@LoggableRequest`
- Пароли скрываются в логах
- Подробное логирование операций
```

### 2.14 Тестирование

**Format:**
```markdown
## Тестирование

Проект включает:

- **Unit тесты** для всех компонентов
- **Integration тесты** с TestContainers
- **Тесты валидации** с покрытием всех сценариев
- **Тесты безопасности** (if applicable)

Запуск тестов:

```bash
./gradlew test
```

### Покрытие тестами

- `[TestClass]` - описание
- `[TestClass]` - описание
```

### 2.15 UML Диаграммы (Optional)

**Format:**
```markdown
## UML Диаграммы

### Диаграмма классов

```
[ASCII diagram showing class relationships]
```
```

---

## 3. Formatting Standards

### 3.1 Markdown Syntax

- Use `#` for main title (level 1)
- Use `##` for major sections (level 2)
- Use `###` for subsections (level 3)
- Use `####` for sub-subsections (level 4)
- Use `**bold**` for emphasis on important terms
- Use `` `code` `` for inline code
- Use ```code blocks``` for multi-line code
- Use `-` for unordered lists
- Use `1.` for ordered lists

### 3.2 Code Blocks

**For HTTP requests:**
```markdown
```http
GET /api/v1/users/{id}
```
```

**For JSON:**
```markdown
```json
{
  "field": "value"
}
```
```

**For Bash commands:**
```markdown
```bash
./gradlew bootRun
```
```

**For Java code:**
```markdown
```java
public class Example {
    // code
}
```
```

**For SQL:**
```markdown
```sql
SELECT * FROM users;
```
```

**For YAML:**
```markdown
```yaml
key: value
```
```

### 3.3 Tables

Always include header row and align columns:
```markdown
| Column 1 | Column 2 | Column 3 |
|----------|----------|----------|
| Value 1  | Value 2  | Value 3  |
```

### 3.4 Lists

**Unordered lists:**
```markdown
- Item 1
- Item 2
  - Sub-item 2.1
  - Sub-item 2.2
```

**Ordered lists:**
```markdown
1. First step
2. Second step
3. Third step
```

### 3.5 Emphasis

- Use `**bold**` for important terms, section names, HTTP methods
- Use `` `code` `` for:
  - Class names
  - Method names
  - Variable names
  - File paths
  - URLs
  - Configuration values

---

## 4. Language and Style

### 4.1 Language

- **All README files must be written in Russian (русский язык)**
- Use formal but clear language
- Avoid slang and colloquialisms
- Use present tense for descriptions
- Use imperative mood for instructions

### 4.2 Terminology

**Consistent terms:**
- "микросервис" (not "сервис" when referring to microservice)
- "REST API" (not "RESTful API")
- "эндпоинт" (not "endpoint")
- "валидация" (not "проверка")
- "сущность" (for JPA entities)
- "DTO" (not "Data Transfer Object" in Russian text)

### 4.3 Examples

**Good:**
```markdown
Сервис предоставляет REST API для управления пользователями.
```

**Bad:**
```markdown
Сервис предоставляет RESTful API для управления юзерами.
```

---

## 5. Service-Specific Requirements

### 5.1 API Services (users-api, tweet-api, etc.)

**Must include:**
- REST API section with full endpoint documentation
- OpenAPI/Swagger documentation section
- Database section
- Validation section
- Integration section (if applicable)
- Security section with password hashing (if applicable)

### 5.2 Libraries (common-lib, etc.)

**Must include:**
- API Documentation section (instead of REST API)
- Usage examples
- Configuration section
- Recommendations section
- May omit: Database, OpenAPI/Swagger sections

### 5.3 Shared Components

**Must include:**
- Clear description of shared functionality
- Usage examples for each component
- Integration instructions
- Dependencies section

---

## 6. Quality Checklist

Before finalizing a README, ensure:

- [ ] All required sections are present
- [ ] All code examples are tested and working
- [ ] All URLs and ports are correct
- [ ] All table formatting is correct
- [ ] All code blocks have proper language tags
- [ ] All diagrams render correctly
- [ ] No broken links
- [ ] Consistent terminology throughout
- [ ] Proper Russian grammar and spelling
- [ ] Examples are practical and relevant
- [ ] Security considerations are documented
- [ ] Testing information is included

---

## 7. Examples Reference

For reference implementations, see:
- `services/users-api/README.md` - Full API service example
- `services/tweet-api/README.md` - API service with integration example
- `shared/common-lib/README.md` - Library documentation example

---

## 8. Updates and Maintenance

- README files should be updated when:
  - New features are added
  - API changes occur
  - Dependencies change
  - Architecture changes
  - Configuration changes

- Review README files during code reviews
- Keep examples up-to-date with code changes
- Update version numbers and ports when they change





