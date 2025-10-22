# TODO - Реализация POST /api/v1/tweets (Создание твита)

## Meta
- project: twitter-tweet-api
- feature: POST /api/v1/tweets - Создание твита
- updated: 2025-01-27
- port: 8082
- database: PostgreSQL (схема: tweet_api)

## Tasks

### Этап 1: Анализ и проектирование (P1)
- [X] (P1) #1: Анализ требований для POST /api/v1/tweets — Определить входные/выходные данные и бизнес-правила
  acceptance: "Понять структуру запроса, валидацию, интеграцию с users-api"
  estimated_time: "30 минут"
  dependencies: "Нет"

### Этап 2: Инфраструктура проекта (P1)
- [x] (P1) [2025-01-27 15:46] #2: Создание структуры проекта tweet-api — Создать Gradle модуль в services/
  acceptance: "build.gradle + базовая структура папок + Application.java + Dockerfile"
  estimated_time: "1 час"
  dependencies: "#1"
  metadata: priority=P1, done=2025-01-27T15:46, note="Создана структура проекта, build.gradle, Application.java, Dockerfile, application.yml, .dockerignore. Проект компилируется успешно."
  
- [x] (P1) [2025-01-27 15:50] #3: Схема базы данных для твитов — Создать SQL миграцию для таблицы tweets
  acceptance: "SQL миграция для таблицы tweets с индексами"
  estimated_time: "1 час"
  dependencies: "#2"
  metadata: priority=P1, done=2025-01-27T15:50, note="Созданы SQL миграции для схемы tweet_api и tweet_api_test, добавлены индексы, триггеры, обновлена конфигурация БД."
  
- [x] (P1) [2025-01-27 15:52] #4: Конфигурация приложения — Создать application.yml для порта 8082
  acceptance: "Конфигурация порта, базы данных, подключение к PostgreSQL"
  estimated_time: "30 минут"
  dependencies: "#3"
  metadata: priority=P1, done=2025-01-27T15:52, note="Конфигурация application.yml и application-test.yml настроена для порта 8082, PostgreSQL, трейсинга и логирования."

### Этап 3: Модель данных (P1)
- [x] (P1) [2025-01-27 15:54] #5: Реализация JPA Entity Tweet — Создать сущность Tweet
  acceptance: "Класс Tweet с полями id, userId, content, createdAt, updatedAt"
  estimated_time: "1 час"
  dependencies: "#4"
  metadata: priority=P1, done=2025-01-27T15:54, note="Создана JPA Entity Tweet с валидацией, маппингом на таблицу tweets, аннотациями Lombok и Hibernate."
  
- [x] (P1) [2025-01-27 15:56] #6: Создание TweetRepository — Реализовать репозиторий для твитов
  acceptance: "TweetRepository с методом save() и кастомными запросами"
  estimated_time: "30 минут"
  dependencies: "#5"
  metadata: priority=P1, done=2025-01-27T15:56, note="Создан TweetRepository с JpaRepository, кастомными методами для поиска по пользователю, пагинации, поиска по контенту."

### Этап 4: DTO и маппинг (P1)
- [x] (P1) [2025-01-27 16:00] #7: Request DTO для создания твита — Создать CreateTweetRequestDto
  acceptance: "DTO с полями content (1-280 символов), userId с валидацией"
  estimated_time: "30 минут"
  dependencies: "#6"
  metadata: priority=P1, done=2025-01-27T16:00, note="Создан CreateTweetRequestDto с валидацией @NotBlank, @Size для content и @NotNull для userId."
  
- [x] (P1) [2025-01-27 16:02] #8: Response DTO для твита — Создать TweetResponseDto
  acceptance: "DTO с полями id, userId, content, createdAt, updatedAt"
  estimated_time: "30 минут"
  dependencies: "#7"
  metadata: priority=P1, done=2025-01-27T16:02, note="Создан TweetResponseDto с полями id, userId, content, createdAt, updatedAt, JSON форматированием дат."
  
- [ ] (P1) #9: MapStruct маппер — Реализовать TweetMapper
  acceptance: "Маппер для конвертации CreateTweetRequestDto → Tweet → TweetResponseDto"
  estimated_time: "30 минут"
  dependencies: "#8"

### Этап 5: Сервисный слой (P1)
- [ ] (P1) #10: Интерфейс TweetService — Определить контракт для создания твита
  acceptance: "Метод createTweet(CreateTweetRequestDto) возвращает TweetResponseDto"
  estimated_time: "15 минут"
  dependencies: "#9"
  
- [ ] (P1) #11: TweetServiceImpl — Реализовать бизнес-логику создания твита
  acceptance: "Валидация контента, проверка пользователя, сохранение твита"
  estimated_time: "1 час"
  dependencies: "#10"

### Этап 6: Интеграция с users-api (P1)
- [ ] (P1) #12: Клиент Users API — Реализовать HTTP клиент для проверки пользователя
  acceptance: "UsersApiClient с методом existsUser(userId)"
  estimated_time: "1 час"
  dependencies: "#11"
  
- [ ] (P1) #13: Валидатор пользователя — Создать кастомный валидатор @UserExists
  acceptance: "Валидатор проверяет существование пользователя через users-api"
  estimated_time: "45 минут"
  dependencies: "#12"

### Этап 7: REST API контроллер (P1)
- [ ] (P1) #14: TweetController — Реализовать POST /api/v1/tweets эндпоинт
  acceptance: "Контроллер принимает CreateTweetRequestDto, возвращает TweetResponseDto"
  estimated_time: "45 минут"
  dependencies: "#13"
  
- [ ] (P1) #15: Обработка ошибок — Реализовать обработку ошибок валидации
  acceptance: "Обработка ошибок валидации с детальными сообщениями"
  estimated_time: "30 минут"
  dependencies: "#14"

### Этап 8: Конфигурация и тестирование (P2)
- [ ] (P2) #16: Обновление Docker Compose — Добавить tweet-api в docker-compose.yml
  acceptance: "Сервис tweet-api в compose с зависимостью от users-api"
  estimated_time: "15 минут"
  dependencies: "#15"
  
- [ ] (P2) #17: Unit тесты — Создать тесты для TweetService и TweetController
  acceptance: "Тесты для создания твита, валидации, обработки ошибок"
  estimated_time: "1 час"
  dependencies: "#16"
  
- [ ] (P2) #18: Интеграционные тесты — Создать тесты с реальной БД
  acceptance: "Тесты POST /api/v1/tweets с TestContainers"
  estimated_time: "45 минут"
  dependencies: "#17"

## Assumptions
- Используем PostgreSQL с отдельной схемой tweet_api
- Порт 8082 для tweet-api сервиса
- Интеграция с users-api (порт 8081) для валидации пользователей
- Максимальная длина твита: 280 символов
- Используем Spring Boot 3.5.5 и Java 24
- Следуем архитектурным паттернам из users-api

## Technical Requirements

### Функциональные требования для POST /api/v1/tweets
- **Входные данные:**
  - content (String, 1-280 символов) - содержимое твита
  - userId (UUID) - идентификатор пользователя-автора

- **Выходные данные:**
  - id (UUID) - идентификатор созданного твита
  - userId (UUID) - идентификатор автора
  - content (String) - содержимое твита
  - createdAt (Timestamp) - время создания
  - updatedAt (Timestamp) - время последнего обновления

- **Бизнес-правила:**
  - Контент не может быть пустым или состоять только из пробелов
  - Максимальная длина контента: 280 символов
  - Пользователь должен существовать в users-api
  - Твит создается с текущим временем

### Нефункциональные требования
- Время ответа API < 500ms для создания твита
- Обработка ошибок валидации с детальными сообщениями
- Интеграция с users-api с обработкой ошибок

## Database Schema (упрощенная)
```sql
-- Схема Tweet API
CREATE SCHEMA IF NOT EXISTS tweet_api;

-- Таблица твитов (упрощенная версия)
CREATE TABLE tweet_api.tweets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    content VARCHAR(280) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индекс для производительности
CREATE INDEX idx_tweets_user_id_created_at ON tweet_api.tweets(user_id, created_at DESC);
```

## API Contract
```yaml
POST /api/v1/tweets
Content-Type: application/json

Request Body:
{
  "content": "Hello, Twitter!",
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}

Response (201 Created):
{
  "id": "987fcdeb-51a2-43d7-8f9e-123456789abc",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "content": "Hello, Twitter!",
  "createdAt": "2025-01-27T10:30:00Z",
  "updatedAt": "2025-01-27T10:30:00Z"
}

Error Response (400 Bad Request):
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": {
      "content": "Content cannot be empty",
      "userId": "User does not exist"
    }
  }
}
```

## Integration Points
- **users-api (Порт 8081):** Проверка существования пользователя при создании твита

## Success Criteria
- POST /api/v1/tweets эндпоинт работает корректно
- Валидация входных данных функционирует
- Интеграция с users-api для проверки пользователя работает
- Обработка ошибок возвращает детальные сообщения
- Тесты покрывают основную функциональность

## Time Estimation
- **Этап 1-2 (Анализ и инфраструктура):** 2 часа
- **Этап 3-4 (Модель данных и DTO):** 2 часа  
- **Этап 5-6 (Сервис и интеграция):** 2.5 часа
- **Этап 7 (Контроллер):** 1.5 часа
- **Этап 8 (Тестирование):** 2 часа

**Общее время:** 10 часов (1-2 дня)

## Next Steps
1. Начать с создания структуры проекта tweet-api
2. Реализовать схему базы данных и JPA сущность
3. Создать DTO и маппер
4. Реализовать сервисный слой
5. Создать REST API контроллер
6. Добавить интеграцию с users-api
7. Настроить тестирование

## Priority Focus
- **P1 (Critical):** Основная функциональность создания твита
- **P1 (Critical):** Валидация входных данных
- **P1 (Critical):** Интеграция с users-api
- **P2 (Important):** Тестирование и обработка ошибок
