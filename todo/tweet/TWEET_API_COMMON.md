# TODO - Tweet API Service Development

## Meta
- project: twitter-tweet-api
- updated: 2025-01-27
- changelog: todo/CHANGELOG.md
- port: 8082
- database: PostgreSQL

## Tasks

### Analysis Phase (P1)
- [X] (P1) #1: Анализ требований и архитектуры — Определить функциональные и нефункциональные требования для tweet-api
  acceptance: "Понять вход/выход, non-functional requirements, интеграция с users-api"
- [X] (P1) #2: Проектирование модели данных — Спроектировать схему таблиц для твитов в PostgreSQL
  acceptance: "SQL схема таблиц tweets, likes, retweets с индексами"
- [X] (P1) #3: Проектирование API контрактов — Определить REST endpoints и DTO структуры
  acceptance: "OpenAPI спецификация + JSON schema для всех endpoints"

### Design Phase (P2)
- [ ] (P2) #4: Архитектурное проектирование сервиса — Спроектировать слои приложения (Controller, Service, Repository)
  acceptance: "Диаграмма архитектуры + описание взаимодействия слоев"
- [ ] (P2) #5: Проектирование интеграции с users-api — Определить способы взаимодействия между сервисами
  acceptance: "Схема интеграции + план обработки ошибок"
- [ ] (P2) #6: Проектирование системы валидации — Определить бизнес-правила и ограничения
  acceptance: "Список валидаций + обработка ошибок"

### Implementation Phase (P1)
- [ ] (P1) #7: Создание структуры проекта — Создать Gradle модуль tweet-api в services/
  acceptance: "build.gradle + базовая структура папок + Application.java"
- [ ] (P1) #8: Реализация модели данных — Создать JPA entities для Tweet, Like, Retweet
  acceptance: "Entity классы + SQL миграции + репозитории"
- [ ] (P1) #9: Реализация DTO и мапперов — Создать DTO классы и MapStruct мапперы
  acceptance: "Request/Response DTO + мапперы + валидация"
- [ ] (P1) #10: Реализация сервисного слоя — Создать бизнес-логику для операций с твитами
  acceptance: "TweetService + валидация + обработка ошибок"
- [ ] (P1) #11: Реализация контроллерного слоя — Создать REST API endpoints
  acceptance: "TweetController + OpenAPI аннотации + обработка HTTP статусов"
- [ ] (P1) #12: Интеграция с users-api — Реализовать проверку существования пользователей
  acceptance: "HTTP клиент + обработка ошибок + fallback стратегии"

### Configuration Phase (P2)
- [ ] (P2) #13: Настройка конфигурации — Создать application.yml и Docker конфигурацию
  acceptance: "Конфигурация порта 8082 + подключение к БД + Dockerfile"
- [ ] (P2) #14: Настройка мониторинга и логирования — Интегрировать с существующей системой мониторинга
  acceptance: "Actuator endpoints + логирование + метрики"
- [ ] (P2) #15: Обновление Docker Compose — Добавить tweet-api в docker-compose.yml
  acceptance: "Сервис tweet-api в compose + health checks + зависимости"

### Testing Phase (P2)
- [ ] (P2) #16: Unit тестирование — Создать unit тесты для всех компонентов
  acceptance: "Покрытие >80% + тесты для всех публичных методов"
- [ ] (P2) #17: Integration тестирование — Создать интеграционные тесты с TestContainers
  acceptance: "Тесты с реальной БД + тесты API endpoints"
- [ ] (P2) #18: Contract тестирование — Создать тесты контрактов с users-api
  acceptance: "WireMock тесты + проверка совместимости API"

### Documentation Phase (P3)
- [ ] (P3) #19: API документация — Создать OpenAPI/Swagger документацию
  acceptance: "Полная документация API + примеры запросов/ответов"
- [ ] (P3) #20: Техническая документация — Создать README и архитектурную документацию
  acceptance: "README.md + диаграммы + инструкции по запуску"

## Assumptions
- Используем PostgreSQL как основную БД (как в users-api)
- Порт 8082 для tweet-api сервиса
- Интеграция с users-api через HTTP REST API
- Используем существующую shared/common-lib для логирования и обработки ошибок
- Следуем архитектурным паттернам из users-api
- Максимальная длина твита: 280 символов
- Поддержка лайков и ретвитов на базовом уровне
- Используем Spring Boot 3.5.5 и Java 24

## Technical Requirements
- **Функциональные требования:**
  - Создание твитов (POST /api/v1/tweets)
  - Получение твитов пользователя (GET /api/v1/tweets/user/{userId})
  - Получение конкретного твита (GET /api/v1/tweets/{tweetId})
  - Обновление твита (PUT /api/v1/tweets/{tweetId})
  - Удаление твита (DELETE /api/v1/tweets/{tweetId})
  - Лайк твита (POST /api/v1/tweets/{tweetId}/like)
  - Убрать лайк (DELETE /api/v1/tweets/{tweetId}/like)
  - Ретвит (POST /api/v1/tweets/{tweetId}/retweet)
  - Получение ленты новостей (GET /api/v1/tweets/timeline/{userId})

- **Нефункциональные требования:**
  - Время ответа API < 200ms для чтения
  - Время ответа API < 500ms для записи
  - Доступность 99.9%
  - Поддержка до 1000 RPS на чтение
  - Поддержка до 100 RPS на запись

## Database Schema Design
```sql
-- Таблица твитов
CREATE TABLE tweets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    content VARCHAR(280) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP
);

-- Таблица лайков
CREATE TABLE tweet_likes (
    id UUID PRIMARY KEY,
    tweet_id UUID NOT NULL REFERENCES tweets(id),
    user_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tweet_id, user_id)
);

-- Таблица ретвитов
CREATE TABLE tweet_retweets (
    id UUID PRIMARY KEY,
    tweet_id UUID NOT NULL REFERENCES tweets(id),
    user_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tweet_id, user_id)
);

-- Индексы для производительности
CREATE INDEX idx_tweets_user_id ON tweets(user_id);
CREATE INDEX idx_tweets_created_at ON tweets(created_at DESC);
CREATE INDEX idx_tweets_user_created ON tweets(user_id, created_at DESC);
CREATE INDEX idx_tweet_likes_tweet_id ON tweet_likes(tweet_id);
CREATE INDEX idx_tweet_retweets_tweet_id ON tweet_retweets(tweet_id);
```

## API Endpoints Design
```
POST   /api/v1/tweets                    # Создать твит
GET    /api/v1/tweets/{tweetId}          # Получить твит
PUT    /api/v1/tweets/{tweetId}          # Обновить твит
DELETE /api/v1/tweets/{tweetId}          # Удалить твит
GET    /api/v1/tweets/user/{userId}      # Твиты пользователя
GET    /api/v1/tweets/timeline/{userId}  # Лента новостей
POST   /api/v1/tweets/{tweetId}/like     # Лайкнуть твит
DELETE /api/v1/tweets/{tweetId}/like     # Убрать лайк
POST   /api/v1/tweets/{tweetId}/retweet  # Ретвитнуть
DELETE /api/v1/tweets/{tweetId}/retweet  # Убрать ретвит
GET    /api/v1/tweets/{tweetId}/likes    # Кто лайкнул
GET    /api/v1/tweets/{tweetId}/retweets # Кто ретвитнул
```

## Integration Points
- **users-api**: Проверка существования пользователей при создании твитов
- **follow-service** (будущий): Получение списка подписок для ленты новостей
- **timeline-service** (будущий): Асинхронное обновление лент при создании твитов

## Risks and Mitigation
- **Риск**: Высокая нагрузка на чтение ленты новостей
  **Митигация**: Кэширование популярных твитов, пагинация, индексы БД

- **Риск**: Проблемы с интеграцией users-api
  **Митигация**: Circuit breaker, retry механизмы, fallback стратегии

- **Риск**: Производительность БД при большом количестве твитов
  **Митигация**: Партиционирование по дате, архивирование старых твитов

## Success Criteria
- Сервис запускается на порту 8082
- Все API endpoints работают корректно
- Интеграция с users-api функционирует
- Покрытие тестами >80%
- Время ответа соответствует требованиям
- Документация API полная и актуальная

## Notes
- Следуем паттернам из users-api для консистентности
- Используем UUID для всех идентификаторов
- Реализуем soft delete для твитов
- Добавляем аудит полей (created_at, updated_at)
- Подготавливаем архитектуру для будущего добавления Kafka
- Планируем миграцию на микросервисную архитектуру с service discovery
