# TODO - План реализации сервиса Tweet API

## Meta
- project: twitter-tweet-api
- updated: 2025-01-27
- changelog: todo/CHANGELOG.md
- port: 8082
- database: PostgreSQL (схема: tweet_api)
- architecture: Микросервисы с Spring Boot 3.5.5

## Tasks

### Этап 1: Анализ и проектирование (P1)
- [X] (P1) #1: Анализ требований и архитектуры — Определить функциональные и нефункциональные требования для tweet-api
  acceptance: "Понять вход/выход, non-functional requirements, интеграция с users-api"
  estimated_time: "2 часа"
  dependencies: "Нет"
  
- [ ] (P1) #2: Проектирование модели данных — Спроектировать схему таблиц для твитов в PostgreSQL
  acceptance: "SQL схема таблиц tweets, likes, retweets с индексами и триггерами"
  estimated_time: "3 часа"
  dependencies: "#1"
  
- [ ] (P1) #3: Проектирование API контрактов — Определить REST endpoints и DTO структуры
  acceptance: "OpenAPI спецификация + JSON schema для всех endpoints"
  estimated_time: "2 часа"
  dependencies: "#2"

### Этап 2: Инфраструктура и базовая структура (P1)
- [ ] (P1) #4: Создание структуры проекта — Создать Gradle модуль tweet-api в services/
  acceptance: "build.gradle + базовая структура папок + Application.java + Dockerfile"
  estimated_time: "2 часа"
  dependencies: "#3"
  
- [ ] (P1) #5: Схема базы данных и миграции — Создать SQL миграции для таблиц твитов
  acceptance: "SQL миграции для tweets, tweet_likes, tweet_retweets с индексами"
  estimated_time: "3 часа"
  dependencies: "#4"
  
- [ ] (P1) #6: Конфигурация приложения — Создать application.yml и properties
  acceptance: "Полная конфигурация для порта 8082, базы данных, кэширования, мониторинга"
  estimated_time: "2 часа"
  dependencies: "#5"

### Этап 3: Модель данных и репозитории (P1)
- [ ] (P1) #7: Реализация JPA сущностей — Создать сущности Tweet, Like, Retweet
  acceptance: "Классы Entity с правильными аннотациями, связями и бизнес-методами"
  estimated_time: "4 часа"
  dependencies: "#6"
  
- [ ] (P1) #8: Слой репозиториев — Реализовать интерфейсы репозиториев с кастомными запросами
  acceptance: "TweetRepository, LikeRepository, RetweetRepository с кастомными методами"
  estimated_time: "3 часа"
  dependencies: "#7"
  
- [ ] (P1) #9: Оптимизация базы данных — Реализовать оптимизацию запросов и индексацию
  acceptance: "Оптимизированные запросы, правильные индексы, мониторинг производительности запросов"
  estimated_time: "2 часа"
  dependencies: "#8"

### Этап 4: DTO и маппинг (P1)
- [ ] (P1) #10: Request/Response DTO — Создать DTO классы с валидацией
  acceptance: "CreateTweetRequestDto, UpdateTweetRequestDto, TweetResponseDto и т.д."
  estimated_time: "3 часа"
  dependencies: "#9"
  
- [ ] (P1) #11: MapStruct мапперы — Реализовать мапперы для конвертации entity-DTO
  acceptance: "TweetMapper, LikeMapper, RetweetMapper с правильными маппингами"
  estimated_time: "2 часа"
  dependencies: "#10"
  
- [ ] (P1) #12: Кастомные валидаторы — Создать валидаторы бизнес-правил
  acceptance: "@UserExists, @NoSelfAction, @TweetExists валидаторы"
  estimated_time: "4 часа"
  dependencies: "#11"

### Этап 5: Сервисный слой (P1)
- [ ] (P1) #13: Интерфейс TweetService — Определить контракт сервиса
  acceptance: "Интерфейс TweetService со всеми бизнес-операциями"
  estimated_time: "1 час"
  dependencies: "#12"
  
- [ ] (P1) #14: TweetServiceImpl — Реализовать основную бизнес-логику
  acceptance: "CRUD операции, социальные действия, интеграция валидации"
  estimated_time: "6 часов"
  dependencies: "#13"
  
- [ ] (P1) #15: Система бизнес-правил — Реализовать комплексные бизнес-правила
  acceptance: "TweetBusinessRulesManager с правилами создания, обновления, социальных действий"
  estimated_time: "5 часов"
  dependencies: "#14"
  
- [ ] (P1) #16: Валидация контента — Реализовать санитизацию контента и обнаружение спама
  acceptance: "ContentValidationService с защитой от XSS и обнаружением спама"
  estimated_time: "3 часа"
  dependencies: "#15"

### Этап 6: Интеграция и обработка ошибок (P1)
- [ ] (P1) #17: Клиент Users API — Реализовать HTTP клиент для интеграции с users-api
  acceptance: "UsersApiClient с Circuit Breaker, Retry и Fallback стратегиями"
  estimated_time: "4 часа"
  dependencies: "#16"
  
- [ ] (P1) #18: Система обработки ошибок — Реализовать комплексную обработку ошибок
  acceptance: "TweetGlobalExceptionHandler с RFC 7807 Problem Details"
  estimated_time: "3 часа"
  dependencies: "#17"
  
- [ ] (P1) #19: Система кэширования — Реализовать многоуровневое кэширование
  acceptance: "Redis кэширование с TTL, инвалидацией и fallback стратегиями"
  estimated_time: "4 часа"
  dependencies: "#18"

### Этап 7: REST API контроллеры (P1)
- [ ] (P1) #20: TweetController — Реализовать REST API эндпоинты
  acceptance: "TweetController со всеми CRUD и социальными action эндпоинтами"
  estimated_time: "4 часа"
  dependencies: "#19"
  
- [ ] (P1) #21: Конфигурация OpenAPI — Настроить Swagger UI и документацию API
  acceptance: "Полная документация OpenAPI с примерами и схемами"
  estimated_time: "2 часа"
  dependencies: "#20"
  
- [ ] (P1) #22: Система пагинации — Реализовать многостратегическую пагинацию
  acceptance: "Offset-based, Cursor-based и Hybrid стратегии пагинации"
  estimated_time: "3 часа"
  dependencies: "#21"

### Этап 8: Конфигурация и развертывание (P2)
- [ ] (P2) #23: Конфигурация Docker — Обновить docker-compose.yml
  acceptance: "Сервис tweet-api в compose с health checks и зависимостями"
  estimated_time: "1 час"
  dependencies: "#22"
  
- [ ] (P2) #24: Настройка мониторинга — Настроить Actuator эндпоинты и метрики
  acceptance: "Health checks, метрики, трейсинг и интеграция с Prometheus"
  estimated_time: "2 часа"
  dependencies: "#23"

### Этап 9: Тестирование (P2)
- [ ] (P2) #25: Unit тесты — Создать комплексные unit тесты
  acceptance: "Покрытие кода >80%, тесты для всех публичных методов и граничных случаев"
  estimated_time: "8 часов"
  dependencies: "#24"
  
- [ ] (P2) #26: Интеграционные тесты — Создать интеграционные тесты с TestContainers
  acceptance: "Тесты с реальной PostgreSQL, тестирование API эндпоинтов, транзакции БД"
  estimated_time: "6 часов"
  dependencies: "#25"
  
- [ ] (P2) #27: Contract тесты — Создать WireMock тесты для интеграции с users-api
  acceptance: "Contract тестирование с Pact, проверка совместимости API"
  estimated_time: "4 часа"
  dependencies: "#26"

### Этап 10: Производительность и оптимизация (P2)
  
- [ ] (P2) #29: Оптимизация кэша — Настроить стратегии кэширования
  acceptance: "Hit rate кэша >80%, правильный TTL, эффективная инвалидация"
  estimated_time: "2 часа"
  dependencies: "#28"

### Этап 11: Документация и финализация (P3)
- [ ] (P3) #30: Документация API — Завершить документацию OpenAPI/Swagger
  acceptance: "Полная документация API с примерами, кодами ошибок и руководствами по использованию"
  estimated_time: "3 часа"
  dependencies: "#29"
  
- [ ] (P3) #31: Техническая документация — Создать README и архитектурную документацию
  acceptance: "README.md с инструкциями по настройке, диаграммами архитектуры, руководством по развертыванию"
  estimated_time: "2 часа"
  dependencies: "#30"
  
- [ ] (P3) #32: Валидация развертывания — Финальное развертывание и валидация
  acceptance: "Сервис работает на порту 8082, все эндпоинты функционируют, интеграция работает"
  estimated_time: "2 часа"
  dependencies: "#31"

## Assumptions
- База данных PostgreSQL с отдельной схемой tweet_api
- Порт 8082 для сервиса tweet-api (следующий после users-api 8081)
- Интеграция с users-api через HTTP REST API
- Использование существующей shared/common-lib для логирования и обработки ошибок
- Следование архитектурным паттернам из users-api для консистентности
- Максимальная длина твита: 280 символов
- Поддержка лайков и ретвитов с базовой функциональностью
- Использование Spring Boot 3.5.5 и Java 24
- Паттерн Circuit Breaker для интеграции с внешними сервисами
- Многоуровневая стратегия кэширования (HTTP, Application, Database)
- Комплексная система валидации и бизнес-правил
- Soft delete для твитов для сохранения истории
- UUID идентификаторы для поддержки распределенных систем

## Technical Requirements

### Функциональные требования
- **Управление твитами:**
  - Создать твит (POST /api/v1/tweets)
  - Получить твит по ID (GET /api/v1/tweets/{tweetId})
  - Обновить твит (PUT /api/v1/tweets/{tweetId})
  - Удалить твит (DELETE /api/v1/tweets/{tweetId})
  - Получить твиты пользователя (GET /api/v1/tweets/user/{userId})
  - Получить ленту новостей (GET /api/v1/tweets/timeline/{userId})

- **Социальные взаимодействия:**
  - Лайкнуть твит (POST /api/v1/tweets/{tweetId}/like)
  - Убрать лайк (DELETE /api/v1/tweets/{tweetId}/like)
  - Ретвитнуть (POST /api/v1/tweets/{tweetId}/retweet)
  - Убрать ретвит (DELETE /api/v1/tweets/{tweetId}/retweet)
  - Получить список лайков (GET /api/v1/tweets/{tweetId}/likes)
  - Получить список ретвитов (GET /api/v1/tweets/{tweetId}/retweets)

### Нефункциональные требования
- **Производительность:**
  - Время ответа API < 200ms для операций чтения
  - Время ответа API < 500ms для операций записи
  - Поддержка до 1000 RPS для операций чтения
  - Поддержка до 100 RPS для операций записи
  - Время выполнения запросов к БД < 100ms
  - Hit rate кэша > 80% для часто используемых данных

- **Надежность:**
  - Доступность 99.9%
  - Частота ошибок < 0.1% для всех операций
  - Время восстановления < 5 минут после сбоя
  - Консистентность данных 100% для критических операций

- **Безопасность:**
  - Валидация и санитизация входных данных
  - Защита от XSS для контента
  - Ограничение частоты запросов для API эндпоинтов
  - Интеграция аутентификации (будущее)

## Database Schema Design
```sql
-- Схема Tweet API
CREATE SCHEMA IF NOT EXISTS tweet_api;

-- Основная таблица твитов
CREATE TABLE tweet_api.tweets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    content VARCHAR(280) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    likes_count INTEGER DEFAULT 0,
    retweets_count INTEGER DEFAULT 0,
    replies_count INTEGER DEFAULT 0,
    stats_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица лайков твитов
CREATE TABLE tweet_api.tweet_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tweet_id UUID NOT NULL REFERENCES tweet_api.tweets(id),
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tweet_id, user_id)
);

-- Таблица ретвитов твитов
CREATE TABLE tweet_api.tweet_retweets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tweet_id UUID NOT NULL REFERENCES tweet_api.tweets(id),
    user_id UUID NOT NULL,
    comment VARCHAR(280),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tweet_id, user_id)
);

-- Индексы для производительности
CREATE INDEX idx_tweets_user_id_created_at ON tweet_api.tweets(user_id, created_at DESC);
CREATE INDEX idx_tweets_created_at ON tweet_api.tweets(created_at DESC);
CREATE INDEX idx_tweets_is_deleted ON tweet_api.tweets(is_deleted);
CREATE INDEX idx_tweets_likes_count ON tweet_api.tweets(likes_count DESC);
CREATE INDEX idx_tweet_likes_tweet_id ON tweet_api.tweet_likes(tweet_id);
CREATE INDEX idx_tweet_likes_user_id ON tweet_api.tweet_likes(user_id);
CREATE INDEX idx_tweet_retweets_tweet_id ON tweet_api.tweet_retweets(tweet_id);
CREATE INDEX idx_tweet_retweets_user_id ON tweet_api.tweet_retweets(user_id);

-- Триггеры для поддержания счетчиков
CREATE OR REPLACE FUNCTION tweet_api.update_tweet_stats()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF TG_TABLE_NAME = 'tweet_likes' THEN
            UPDATE tweet_api.tweets SET likes_count = likes_count + 1, stats_updated_at = CURRENT_TIMESTAMP WHERE id = NEW.tweet_id;
        ELSIF TG_TABLE_NAME = 'tweet_retweets' THEN
            UPDATE tweet_api.tweets SET retweets_count = retweets_count + 1, stats_updated_at = CURRENT_TIMESTAMP WHERE id = NEW.tweet_id;
        END IF;
    ELSIF TG_OP = 'DELETE' THEN
        IF TG_TABLE_NAME = 'tweet_likes' THEN
            UPDATE tweet_api.tweets SET likes_count = likes_count - 1, stats_updated_at = CURRENT_TIMESTAMP WHERE id = OLD.tweet_id;
        ELSIF TG_TABLE_NAME = 'tweet_retweets' THEN
            UPDATE tweet_api.tweets SET retweets_count = retweets_count - 1, stats_updated_at = CURRENT_TIMESTAMP WHERE id = OLD.tweet_id;
        END IF;
    END IF;
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_likes_count
    AFTER INSERT OR DELETE ON tweet_api.tweet_likes
    FOR EACH ROW EXECUTE FUNCTION tweet_api.update_tweet_stats();

CREATE TRIGGER trigger_update_retweets_count
    AFTER INSERT OR DELETE ON tweet_api.tweet_retweets
    FOR EACH ROW EXECUTE FUNCTION tweet_api.update_tweet_stats();
```

## API Endpoints Design
```
# Управление твитами
POST   /api/v1/tweets                    # Создать твит
GET    /api/v1/tweets/{tweetId}          # Получить твит по ID
PUT    /api/v1/tweets/{tweetId}          # Обновить твит
DELETE /api/v1/tweets/{tweetId}          # Удалить твит (soft delete)

# Контент пользователя
GET    /api/v1/tweets/user/{userId}      # Получить твиты пользователя (с пагинацией)
GET    /api/v1/tweets/timeline/{userId}  # Получить ленту новостей пользователя (с пагинацией)

# Социальные взаимодействия
POST   /api/v1/tweets/{tweetId}/like     # Лайкнуть твит
DELETE /api/v1/tweets/{tweetId}/like      # Убрать лайк
POST   /api/v1/tweets/{tweetId}/retweet   # Ретвитнуть с опциональным комментарием
DELETE /api/v1/tweets/{tweetId}/retweet   # Убрать ретвит

# Списки социальных действий
GET    /api/v1/tweets/{tweetId}/likes     # Получить пользователей, лайкнувших твит
GET    /api/v1/tweets/{tweetId}/retweets  # Получить пользователей, ретвитнувших твит
```

## Integration Points
- **users-api (Порт 8081):** Валидация пользователей, информация профиля
- **follow-service (Будущее):** Отношения подписок пользователей для ленты новостей
- **timeline-service (Будущее):** Кэшированная генерация ленты новостей
- **notification-service (Будущее):** Уведомления в реальном времени для взаимодействий

## Risks and Mitigation

### Технические риски
- **Высокая нагрузка на чтение ленты новостей**
  - Митигация: Redis кэширование, оптимизированная пагинация, read replicas БД
  - Мониторинг: RPS, время ответа, hit rate кэша

- **Производительность БД с большим объемом твитов**
  - Митигация: Партиционирование таблиц, архивирование старых твитов, оптимизированные индексы
  - Мониторинг: Время выполнения запросов, размер БД, использование индексов

- **Сбои интеграции с users-api**
  - Митигация: Circuit breaker, retry механизмы, fallback стратегии
  - Мониторинг: Доступность API, время ответа, частота ошибок

- **Использование памяти с кэшированием**
  - Митигация: TTL-based истечение, лимиты размера кэша, мониторинг
  - Мониторинг: Использование памяти, hit rate кэша, частота вытеснения

### Бизнес-риски
- **Потеря или коррупция данных**
  - Митигация: Репликация БД, автоматические бэкапы, ACID транзакции
  - Мониторинг: Проверки целостности данных, успешность бэкапов

- **Уязвимости безопасности**
  - Митигация: Валидация входных данных, защита от XSS, ограничение частоты запросов
  - Мониторинг: Алерты безопасности, паттерны подозрительной активности

## Success Criteria
- Сервис успешно работает на порту 8082
- Все API эндпоинты отвечают корректно с правильными HTTP статус кодами
- Интеграция с users-api функционирует без ошибок
- Покрытие тестами > 80% для всех компонентов
- Время ответа API соответствует требованиям производительности
- Полная документация OpenAPI с примерами
- Docker контейнеризация работает корректно
- Health checks и эндпоинты мониторинга функциональны

## Architectural Patterns
- **Repository Pattern:** Абстракция доступа к данным
- **DTO Pattern:** Изоляция контракта API
- **Service Layer Pattern:** Инкапсуляция бизнес-логики
- **Circuit Breaker Pattern:** Устойчивость внешних сервисов
- **Cache-Aside Pattern:** Оптимизация производительности
- **Factory Pattern:** Абстракция создания объектов

## Technology Stack
- **Backend:** Java 24, Spring Boot 3.5.5
- **База данных:** PostgreSQL 15
- **ORM:** JPA/Hibernate
- **Маппинг:** MapStruct
- **Документация:** SpringDoc OpenAPI
- **Кэширование:** Redis
- **Устойчивость:** Resilience4j (Circuit Breaker, Retry)
- **Тестирование:** JUnit 5, TestContainers, WireMock
- **Мониторинг:** Micrometer, Prometheus
- **Контейнеризация:** Docker, Docker Compose

## Notes
- Следовать паттернам users-api для консистентности между сервисами
- Использовать UUID для всех идентификаторов для поддержки распределенных систем
- Реализовать soft delete для твитов для сохранения истории взаимодействий
- Добавить аудит поля (created_at, updated_at) ко всем сущностям
- Подготовить архитектуру для будущей интеграции с Kafka
- Планировать миграцию на микросервисную архитектуру с service discovery
- Реализовать комплексное логирование с traceId/spanId
- Использовать компоненты shared/common-lib для консистентности
- Следовать RFC 7807 Problem Details для ответов об ошибках
- Реализовать graceful degradation для сбоев внешних сервисов

## Time Estimation
- **Этап 1-2 (Анализ и инфраструктура):** 1-2 дня
- **Этап 3-4 (Модель данных и DTO):** 2-3 дня  
- **Этап 5-6 (Сервис и интеграция):** 3-4 дня
- **Этап 7 (Контроллер):** 1-2 дня
- **Этап 8 (Конфигурация):** 1 день
- **Этап 9 (Тестирование):** 2-3 дня
- **Этап 10 (Оптимизация):** 1-2 дня
- **Этап 11 (Документация):** 1 день

**Общее оценочное время:** 12-18 дней

## Next Steps
1. Начать с Этапа 1: Анализ требований и проектирование архитектуры
2. Создать структуру проекта и базовую конфигурацию
3. Реализовать схему базы данных и JPA сущности
4. Построить сервисный слой с бизнес-логикой
5. Создать REST API эндпоинты
6. Добавить комплексное тестирование
7. Развернуть и валидировать функциональность

## Priority Focus Areas
1. **P1 (Critical):** Основная функциональность CRUD операций с твитами
2. **P1 (Critical):** Интеграция с users-api для валидации пользователей
3. **P1 (Critical):** Система валидации и бизнес-правил
4. **P2 (Important):** Производительность и кэширование
5. **P2 (Important):** Комплексное тестирование
6. **P3 (Nice to have):** Расширенная документация и оптимизация
