# TODO - План разработки сервиса Tweet API

## Метаданные
- проект: twitter-tweet-api
- обновлено: 2025-01-27
- журнал изменений: todo/CHANGELOG.md
- порт: 8082
- база данных: PostgreSQL (схема: tweet_api)
- архитектура: Микросервисы с Spring Boot 3.5.5

## Задачи

### Этап 1: Настройка инфраструктуры (P1)
- [ ] (P1) #1: Создание структуры проекта — Создать Gradle модуль tweet-api в services/
  критерии_приемки: "build.gradle + базовая структура папок + Application.java + Dockerfile"
  время_выполнения: "2 часа"
  зависимости: "Нет"
  
- [ ] (P1) #2: Схема базы данных и миграции — Создать SQL миграции для таблиц твитов
  критерии_приемки: "SQL миграции для tweets, tweet_likes, tweet_retweets с индексами"
  время_выполнения: "3 часа"
  зависимости: "#1"
  
- [ ] (P1) #3: Реализация JPA сущностей — Создать сущности Tweet, Like, Retweet
  критерии_приемки: "Классы Entity с правильными аннотациями, связями и бизнес-методами"
  время_выполнения: "4 часа"
  зависимости: "#2"
  
- [ ] (P1) #4: Слой репозиториев — Реализовать интерфейсы репозиториев с кастомными запросами
  критерии_приемки: "TweetRepository, LikeRepository, RetweetRepository с кастомными методами"
  время_выполнения: "3 часа"
  зависимости: "#3"

### Этап 2: Слой DTO и маппинга (P1)
- [ ] (P1) #5: Request/Response DTO — Создать DTO классы с валидацией
  критерии_приемки: "CreateTweetRequestDto, UpdateTweetRequestDto, TweetResponseDto и т.д."
  время_выполнения: "3 часа"
  зависимости: "#4"
  
- [ ] (P1) #6: MapStruct мапперы — Реализовать мапперы для конвертации entity-DTO
  критерии_приемки: "TweetMapper, LikeMapper, RetweetMapper с правильными маппингами"
  время_выполнения: "2 часа"
  зависимости: "#5"
  
- [ ] (P1) #7: Кастомные валидаторы — Создать валидаторы бизнес-правил
  критерии_приемки: "@UserExists, @NoSelfAction, @TweetExists валидаторы"
  время_выполнения: "4 часа"
  зависимости: "#6"

### Этап 3: Реализация сервисного слоя (P1)
- [ ] (P1) #8: Интерфейс TweetService — Определить контракт сервиса
  критерии_приемки: "Интерфейс TweetService со всеми бизнес-операциями"
  время_выполнения: "1 час"
  зависимости: "#7"
  
- [ ] (P1) #9: TweetServiceImpl — Реализовать основную бизнес-логику
  критерии_приемки: "CRUD операции, социальные действия, интеграция валидации"
  время_выполнения: "6 часов"
  зависимости: "#8"
  
- [ ] (P1) #10: Система бизнес-правил — Реализовать комплексные бизнес-правила
  критерии_приемки: "TweetBusinessRulesManager с правилами создания, обновления, социальных действий"
  время_выполнения: "5 часов"
  зависимости: "#9"
  
- [ ] (P1) #11: Валидация контента — Реализовать санитизацию контента и обнаружение спама
  критерии_приемки: "ContentValidationService с защитой от XSS и обнаружением спама"
  время_выполнения: "3 часа"
  зависимости: "#10"

### Этап 4: Слой интеграции (P1)
- [ ] (P1) #12: Клиент Users API — Реализовать HTTP клиент для интеграции с users-api
  критерии_приемки: "UsersApiClient с Circuit Breaker, Retry и Fallback стратегиями"
  время_выполнения: "4 часа"
  зависимости: "#11"
  
- [ ] (P1) #13: Система обработки ошибок — Реализовать комплексную обработку ошибок
  критерии_приемки: "TweetGlobalExceptionHandler с RFC 7807 Problem Details"
  время_выполнения: "3 часа"
  зависимости: "#12"
  
- [ ] (P1) #14: Система кэширования — Реализовать многоуровневое кэширование
  критерии_приемки: "Redis кэширование с TTL, инвалидацией и fallback стратегиями"
  время_выполнения: "4 часа"
  зависимости: "#13"

### Этап 5: Слой контроллеров (P1)
- [ ] (P1) #15: TweetController — Реализовать REST API эндпоинты
  критерии_приемки: "TweetController со всеми CRUD и социальными action эндпоинтами"
  время_выполнения: "4 часа"
  зависимости: "#14"
  
- [ ] (P1) #16: Конфигурация OpenAPI — Настроить Swagger UI и документацию API
  критерии_приемки: "Полная документация OpenAPI с примерами и схемами"
  время_выполнения: "2 часа"
  зависимости: "#15"
  
- [ ] (P1) #17: Система пагинации — Реализовать многостратегическую пагинацию
  критерии_приемки: "Offset-based, Cursor-based и Hybrid стратегии пагинации"
  время_выполнения: "3 часа"
  зависимости: "#16"

### Этап 6: Конфигурация и развертывание (P2)
- [ ] (P2) #18: Конфигурация приложения — Создать application.yml и properties
  критерии_приемки: "Полная конфигурация для порта 8082, базы данных, кэширования, мониторинга"
  время_выполнения: "2 часа"
  зависимости: "#17"
  
- [ ] (P2) #19: Конфигурация Docker — Обновить docker-compose.yml
  критерии_приемки: "Сервис tweet-api в compose с health checks и зависимостями"
  время_выполнения: "1 час"
  зависимости: "#18"
  
- [ ] (P2) #20: Настройка мониторинга — Настроить Actuator эндпоинты и метрики
  критерии_приемки: "Health checks, метрики, трейсинг и интеграция с Prometheus"
  время_выполнения: "2 часа"
  зависимости: "#19"

### Этап 7: Реализация тестирования (P2)
- [ ] (P2) #21: Unit тесты — Создать комплексные unit тесты
  критерии_приемки: "Покрытие кода >80%, тесты для всех публичных методов и граничных случаев"
  время_выполнения: "8 часов"
  зависимости: "#20"
  
- [ ] (P2) #22: Интеграционные тесты — Создать интеграционные тесты с TestContainers
  критерии_приемки: "Тесты с реальной PostgreSQL, тестирование API эндпоинтов, транзакции БД"
  время_выполнения: "6 часов"
  зависимости: "#21"
  
- [ ] (P2) #23: Contract тесты — Создать WireMock тесты для интеграции с users-api
  критерии_приемки: "Contract тестирование с Pact, проверка совместимости API"
  время_выполнения: "4 часа"
  зависимости: "#22"

### Этап 8: Производительность и оптимизация (P2)
- [ ] (P2) #24: Оптимизация базы данных — Реализовать оптимизацию запросов и индексацию
  критерии_приемки: "Оптимизированные запросы, правильные индексы, мониторинг производительности запросов"
  время_выполнения: "3 часа"
  зависимости: "#23"
  
- [ ] (P2) #25: Нагрузочное тестирование — Реализовать тестирование производительности
  критерии_приемки: "Нагрузочные тесты для 1000 RPS чтения, 100 RPS записи, время ответа < 200ms"
  время_выполнения: "4 часа"
  зависимости: "#24"
  
- [ ] (P2) #26: Оптимизация кэша — Настроить стратегии кэширования
  критерии_приемки: "Hit rate кэша >80%, правильный TTL, эффективная инвалидация"
  время_выполнения: "2 часа"
  зависимости: "#25"

### Этап 9: Документация и финализация (P3)
- [ ] (P3) #27: Документация API — Завершить документацию OpenAPI/Swagger
  критерии_приемки: "Полная документация API с примерами, кодами ошибок и руководствами по использованию"
  время_выполнения: "3 часа"
  зависимости: "#26"
  
- [ ] (P3) #28: Техническая документация — Создать README и архитектурную документацию
  критерии_приемки: "README.md с инструкциями по настройке, диаграммами архитектуры, руководством по развертыванию"
  время_выполнения: "2 часа"
  зависимости: "#27"
  
- [ ] (P3) #29: Валидация развертывания — Финальное развертывание и валидация
  критерии_приемки: "Сервис работает на порту 8082, все эндпоинты функционируют, интеграция работает"
  время_выполнения: "2 часа"
  зависимости: "#28"

## Предположения
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

## Технические требования

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

## Проектирование схемы базы данных
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

## Проектирование API эндпоинтов
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
DELETE /api/v1/tweets/{tweetId}/like     # Убрать лайк
POST   /api/v1/tweets/{tweetId}/retweet  # Ретвитнуть с опциональным комментарием
DELETE /api/v1/tweets/{tweetId}/retweet  # Убрать ретвит

# Списки социальных действий
GET    /api/v1/tweets/{tweetId}/likes    # Получить пользователей, лайкнувших твит
GET    /api/v1/tweets/{tweetId}/retweets # Получить пользователей, ретвитнувших твит
```

## Точки интеграции
- **users-api (Порт 8081):** Валидация пользователей, информация профиля
- **follow-service (Будущее):** Отношения подписок пользователей для ленты новостей
- **timeline-service (Будущее):** Кэшированная генерация ленты новостей
- **notification-service (Будущее):** Уведомления в реальном времени для взаимодействий

## Риски и митигация

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

## Критерии успеха
- Сервис успешно работает на порту 8082
- Все API эндпоинты отвечают корректно с правильными HTTP статус кодами
- Интеграция с users-api функционирует без ошибок
- Покрытие тестами > 80% для всех компонентов
- Время ответа API соответствует требованиям производительности
- Полная документация OpenAPI с примерами
- Docker контейнеризация работает корректно
- Health checks и эндпоинты мониторинга функциональны

## Архитектурные паттерны
- **Repository Pattern:** Абстракция доступа к данным
- **DTO Pattern:** Изоляция контракта API
- **Service Layer Pattern:** Инкапсуляция бизнес-логики
- **Circuit Breaker Pattern:** Устойчивость внешних сервисов
- **Cache-Aside Pattern:** Оптимизация производительности
- **Factory Pattern:** Абстракция создания объектов

## Технологический стек
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

## Примечания
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

## Оценка времени
- **Этап 1-2 (Инфраструктура и DTO):** 2-3 дня
- **Этап 3-4 (Сервис и интеграция):** 3-4 дня  
- **Этап 5 (Контроллер):** 1-2 дня
- **Этап 6 (Конфигурация):** 1 день
- **Этап 7 (Тестирование):** 2-3 дня
- **Этап 8 (Оптимизация):** 1-2 дня
- **Этап 9 (Документация):** 1 день

**Общее оценочное время:** 11-16 дней

## Следующие шаги
1. Начать с Этапа 1: Создание структуры проекта и базовой конфигурации
2. Реализовать схему базы данных и JPA сущности
3. Построить сервисный слой с бизнес-логикой
4. Создать REST API эндпоинты
5. Добавить комплексное тестирование
6. Развернуть и валидировать функциональность
