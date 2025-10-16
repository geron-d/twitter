# Проектирование модели данных Tweet API Service

## Meta
- project: twitter-tweet-api
- design_date: 2025-01-27
- designer: AI Assistant
- version: 1.0
- status: completed
- database: PostgreSQL 15+
- port: 8082

## Executive Summary

Данный документ содержит детальное проектирование модели данных для сервиса Tweet API. Модель спроектирована с учетом высоких требований к производительности (1000 RPS чтение, 100 RPS запись), масштабируемости и интеграции с существующим сервисом users-api.

## 1. Архитектурные принципы

### 1.1 Основные принципы проектирования

#### Производительность:
- **Оптимизированные индексы** для частых запросов
- **Составные индексы** для сложных запросов (user_id + created_at)
- **Партиционирование** по дате для больших объемов данных
- **Soft delete** для сохранения истории без потери производительности

#### Масштабируемость:
- **UUID** для всех идентификаторов (поддержка распределенных систем)
- **Нормализованная структура** для минимизации дублирования
- **Подготовка к шардингу** по user_id
- **Горизонтальное масштабирование** через stateless архитектуру

#### Целостность данных:
- **Foreign key constraints** для ссылочной целостности
- **Unique constraints** для предотвращения дублирования
- **Check constraints** для валидации бизнес-правил
- **NOT NULL constraints** для обязательных полей

#### Аудит и мониторинг:
- **Временные метки** для всех операций (created_at, updated_at)
- **Soft delete** с отметкой времени удаления
- **Audit trail** для отслеживания изменений
- **Метрики производительности** через индексы

## 2. Структура таблиц

### 2.1 Основная таблица tweets

```sql
-- Таблица твитов - основная сущность системы
CREATE TABLE tweets (
    -- Уникальный идентификатор твита
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Ссылка на пользователя (автора твита)
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Содержимое твита (максимум 280 символов)
    content VARCHAR(280) NOT NULL CHECK (LENGTH(TRIM(content)) > 0),
    
    -- Временные метки
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Soft delete поля
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    -- Статистика твита (для оптимизации запросов)
    likes_count INTEGER NOT NULL DEFAULT 0 CHECK (likes_count >= 0),
    retweets_count INTEGER NOT NULL DEFAULT 0 CHECK (retweets_count >= 0),
    replies_count INTEGER NOT NULL DEFAULT 0 CHECK (replies_count >= 0),
    
    -- Информация о последнем обновлении статистики
    stats_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Ограничения
    CONSTRAINT tweets_content_not_empty CHECK (LENGTH(TRIM(content)) > 0),
    CONSTRAINT tweets_content_max_length CHECK (LENGTH(content) <= 280),
    CONSTRAINT tweets_deleted_consistency CHECK (
        (is_deleted = FALSE AND deleted_at IS NULL) OR 
        (is_deleted = TRUE AND deleted_at IS NOT NULL)
    )
);

-- Комментарии к полям
COMMENT ON TABLE tweets IS 'Основная таблица для хранения твитов';
COMMENT ON COLUMN tweets.id IS 'Уникальный идентификатор твита (UUID)';
COMMENT ON COLUMN tweets.user_id IS 'Идентификатор автора твита, ссылка на users.id';
COMMENT ON COLUMN tweets.content IS 'Содержимое твита, максимум 280 символов';
COMMENT ON COLUMN tweets.metadata IS 'Дополнительные метаданные твита в формате JSON';
COMMENT ON COLUMN tweets.created_at IS 'Время создания твита с часовым поясом';
COMMENT ON COLUMN tweets.updated_at IS 'Время последнего обновления твита';
COMMENT ON COLUMN tweets.is_deleted IS 'Флаг мягкого удаления твита';
COMMENT ON COLUMN tweets.deleted_at IS 'Время мягкого удаления твита';
COMMENT ON COLUMN tweets.likes_count IS 'Количество лайков (дениormalized для производительности)';
COMMENT ON COLUMN tweets.retweets_count IS 'Количество ретвитов (дениormalized для производительности)';
COMMENT ON COLUMN tweets.replies_count IS 'Количество ответов (дениormalized для производительности)';
COMMENT ON COLUMN tweets.stats_updated_at IS 'Время последнего обновления статистики';
```

### 2.2 Таблица лайков

```sql
-- Таблица лайков твитов
CREATE TABLE tweet_likes (
    -- Уникальный идентификатор лайка
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Ссылка на твит
    tweet_id UUID NOT NULL REFERENCES tweets(id) ON DELETE CASCADE,
    
    -- Ссылка на пользователя, который лайкнул
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Время создания лайка
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Уникальность: один пользователь может лайкнуть твит только один раз
    CONSTRAINT tweet_likes_unique_user_tweet UNIQUE (tweet_id, user_id)
);

-- Комментарии к полям
COMMENT ON TABLE tweet_likes IS 'Таблица для хранения лайков твитов';
COMMENT ON COLUMN tweet_likes.id IS 'Уникальный идентификатор лайка';
COMMENT ON COLUMN tweet_likes.tweet_id IS 'Идентификатор твита, ссылка на tweets.id';
COMMENT ON COLUMN tweet_likes.user_id IS 'Идентификатор пользователя, ссылка на users.id';
COMMENT ON COLUMN tweet_likes.created_at IS 'Время создания лайка';
```

### 2.3 Таблица ретвитов

```sql
-- Таблица ретвитов
CREATE TABLE tweet_retweets (
    -- Уникальный идентификатор ретвита
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Ссылка на оригинальный твит
    tweet_id UUID NOT NULL REFERENCES tweets(id) ON DELETE CASCADE,
    
    -- Ссылка на пользователя, который ретвитнул
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Комментарий к ретвиту (опционально)
    comment VARCHAR(280) CHECK (LENGTH(TRIM(comment)) > 0),
    
    -- Время создания ретвита
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Уникальность: один пользователь может ретвитнуть твит только один раз
    CONSTRAINT tweet_retweets_unique_user_tweet UNIQUE (tweet_id, user_id),
    
    -- Ограничения
    CONSTRAINT tweet_retweets_comment_max_length CHECK (LENGTH(comment) <= 280)
);

-- Комментарии к полям
COMMENT ON TABLE tweet_retweets IS 'Таблица для хранения ретвитов';
COMMENT ON COLUMN tweet_retweets.id IS 'Уникальный идентификатор ретвита';
COMMENT ON COLUMN tweet_retweets.tweet_id IS 'Идентификатор оригинального твита';
COMMENT ON COLUMN tweet_retweets.user_id IS 'Идентификатор пользователя, который ретвитнул';
COMMENT ON COLUMN tweet_retweets.comment IS 'Комментарий к ретвиту, максимум 280 символов';
COMMENT ON COLUMN tweet_retweets.created_at IS 'Время создания ретвита';
```

### 2.4 Таблица ответов (replies)

```sql
-- Таблица ответов на твиты
CREATE TABLE tweet_replies (
    -- Уникальный идентификатор ответа
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Ссылка на оригинальный твит
    parent_tweet_id UUID NOT NULL REFERENCES tweets(id) ON DELETE CASCADE,
    
    -- Ссылка на ответ (тоже твит)
    reply_tweet_id UUID NOT NULL REFERENCES tweets(id) ON DELETE CASCADE,
    
    -- Время создания ответа
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Уникальность: один твит может быть ответом только на один твит
    CONSTRAINT tweet_replies_unique_reply UNIQUE (reply_tweet_id),
    
    -- Ограничения
    CONSTRAINT tweet_replies_no_self_reply CHECK (parent_tweet_id != reply_tweet_id)
);

-- Комментарии к полям
COMMENT ON TABLE tweet_replies IS 'Таблица для хранения ответов на твиты';
COMMENT ON COLUMN tweet_replies.id IS 'Уникальный идентификатор ответа';
COMMENT ON COLUMN tweet_replies.parent_tweet_id IS 'Идентификатор оригинального твита';
COMMENT ON COLUMN tweet_replies.reply_tweet_id IS 'Идентификатор твита-ответа';
COMMENT ON COLUMN tweet_replies.created_at IS 'Время создания ответа';
```

## 3. Стратегия индексации

### 3.1 Основные индексы для производительности

```sql
-- Индексы для таблицы tweets

-- Индекс для поиска твитов пользователя (основной запрос)
CREATE INDEX idx_tweets_user_id_created_at ON tweets(user_id, created_at DESC) 
WHERE is_deleted = FALSE;

-- Индекс для ленты новостей (timeline)
CREATE INDEX idx_tweets_created_at_active ON tweets(created_at DESC) 
WHERE is_deleted = FALSE;

-- Индекс для поиска по содержимому (full-text search)
CREATE INDEX idx_tweets_content_gin ON tweets USING gin(to_tsvector('english', content))
WHERE is_deleted = FALSE;

-- Индекс для обновления статистики
CREATE INDEX idx_tweets_stats_updated_at ON tweets(stats_updated_at);

-- Индекс для поиска удаленных твитов (для админских операций)
CREATE INDEX idx_tweets_deleted_at ON tweets(deleted_at) 
WHERE is_deleted = TRUE;

-- Индексы для таблицы tweet_likes

-- Индекс для поиска лайков твита
CREATE INDEX idx_tweet_likes_tweet_id ON tweet_likes(tweet_id);

-- Индекс для поиска лайков пользователя
CREATE INDEX idx_tweet_likes_user_id ON tweet_likes(user_id);

-- Составной индекс для проверки существования лайка
CREATE INDEX idx_tweet_likes_tweet_user ON tweet_likes(tweet_id, user_id);

-- Индекс для временных запросов
CREATE INDEX idx_tweet_likes_created_at ON tweet_likes(created_at DESC);

-- Индексы для таблицы tweet_retweets

-- Индекс для поиска ретвитов твита
CREATE INDEX idx_tweet_retweets_tweet_id ON tweet_retweets(tweet_id);

-- Индекс для поиска ретвитов пользователя
CREATE INDEX idx_tweet_retweets_user_id ON tweet_retweets(user_id);

-- Составной индекс для проверки существования ретвита
CREATE INDEX idx_tweet_retweets_tweet_user ON tweet_retweets(tweet_id, user_id);

-- Индекс для временных запросов
CREATE INDEX idx_tweet_retweets_created_at ON tweet_retweets(created_at DESC);

-- Индексы для таблицы tweet_replies

-- Индекс для поиска ответов на твит
CREATE INDEX idx_tweet_replies_parent_tweet_id ON tweet_replies(parent_tweet_id);

-- Индекс для поиска ответов пользователя
CREATE INDEX idx_tweet_replies_reply_tweet_id ON tweet_replies(reply_tweet_id);

-- Индекс для временных запросов
CREATE INDEX idx_tweet_replies_created_at ON tweet_replies(created_at DESC);
```

### 3.2 Специализированные индексы

```sql
-- Партиционированные индексы для больших объемов данных
-- (будут созданы при необходимости масштабирования)

-- Партиционирование по дате (пример для будущего использования)
-- CREATE TABLE tweets_y2025 PARTITION OF tweets
-- FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');

-- Индексы для аналитических запросов
CREATE INDEX idx_tweets_user_stats ON tweets(user_id, likes_count DESC, retweets_count DESC)
WHERE is_deleted = FALSE;

-- Индекс для популярных твитов
CREATE INDEX idx_tweets_popularity ON tweets((likes_count + retweets_count) DESC, created_at DESC)
WHERE is_deleted = FALSE AND created_at > CURRENT_TIMESTAMP - INTERVAL '7 days';
```

## 4. Триггеры и функции

### 4.1 Автоматическое обновление статистики

```sql
-- Функция для обновления счетчиков лайков
CREATE OR REPLACE FUNCTION update_tweet_likes_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE tweets 
        SET likes_count = likes_count + 1,
            stats_updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.tweet_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE tweets 
        SET likes_count = GREATEST(likes_count - 1, 0),
            stats_updated_at = CURRENT_TIMESTAMP
        WHERE id = OLD.tweet_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Триггер для автоматического обновления счетчика лайков
CREATE TRIGGER trigger_update_tweet_likes_count
    AFTER INSERT OR DELETE ON tweet_likes
    FOR EACH ROW EXECUTE FUNCTION update_tweet_likes_count();

-- Функция для обновления счетчиков ретвитов
CREATE OR REPLACE FUNCTION update_tweet_retweets_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE tweets 
        SET retweets_count = retweets_count + 1,
            stats_updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.tweet_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE tweets 
        SET retweets_count = GREATEST(retweets_count - 1, 0),
            stats_updated_at = CURRENT_TIMESTAMP
        WHERE id = OLD.tweet_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Триггер для автоматического обновления счетчика ретвитов
CREATE TRIGGER trigger_update_tweet_retweets_count
    AFTER INSERT OR DELETE ON tweet_retweets
    FOR EACH ROW EXECUTE FUNCTION update_tweet_retweets_count();

-- Функция для обновления счетчиков ответов
CREATE OR REPLACE FUNCTION update_tweet_replies_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE tweets 
        SET replies_count = replies_count + 1,
            stats_updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.parent_tweet_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE tweets 
        SET replies_count = GREATEST(replies_count - 1, 0),
            stats_updated_at = CURRENT_TIMESTAMP
        WHERE id = OLD.parent_tweet_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Триггер для автоматического обновления счетчика ответов
CREATE TRIGGER trigger_update_tweet_replies_count
    AFTER INSERT OR DELETE ON tweet_replies
    FOR EACH ROW EXECUTE FUNCTION update_tweet_replies_count();
```

### 4.2 Автоматическое обновление временных меток

```sql
-- Функция для автоматического обновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Триггер для автоматического обновления updated_at в tweets
CREATE TRIGGER trigger_tweets_updated_at
    BEFORE UPDATE ON tweets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

## 5. Представления (Views) для оптимизации запросов

### 5.1 Представление для активных твитов

```sql
-- Представление для активных (не удаленных) твитов
CREATE VIEW active_tweets AS
SELECT 
    t.id,
    t.user_id,
    t.content,
    t.metadata,
    t.created_at,
    t.updated_at,
    t.likes_count,
    t.retweets_count,
    t.replies_count,
    t.stats_updated_at,
    u.login as author_login,
    u.first_name as author_first_name,
    u.last_name as author_last_name
FROM tweets t
JOIN users u ON t.user_id = u.id
WHERE t.is_deleted = FALSE;

COMMENT ON VIEW active_tweets IS 'Представление активных твитов с информацией об авторах';
```

### 5.2 Представление для статистики твитов

```sql
-- Представление для статистики твитов
CREATE VIEW tweet_stats AS
SELECT 
    t.id as tweet_id,
    t.user_id,
    t.content,
    t.created_at,
    t.likes_count,
    t.retweets_count,
    t.replies_count,
    (t.likes_count + t.retweets_count + t.replies_count) as total_engagement,
    CASE 
        WHEN t.created_at > CURRENT_TIMESTAMP - INTERVAL '1 hour' THEN 'recent'
        WHEN t.created_at > CURRENT_TIMESTAMP - INTERVAL '24 hours' THEN 'today'
        WHEN t.created_at > CURRENT_TIMESTAMP - INTERVAL '7 days' THEN 'week'
        ELSE 'older'
    END as age_category
FROM tweets t
WHERE t.is_deleted = FALSE;

COMMENT ON VIEW tweet_stats IS 'Представление статистики твитов с категоризацией по возрасту';
```

## 6. Ограничения и валидация

### 6.1 Бизнес-правила

```sql
-- Дополнительные ограничения для бизнес-логики

-- Ограничение на максимальное количество твитов в день (защита от спама)
-- (реализуется на уровне приложения, но можно добавить триггер)

-- Ограничение на длину содержимого (уже есть в CHECK constraint)
-- Ограничение на уникальность лайков/ретвитов (уже есть в UNIQUE constraint)

-- Проверка целостности данных при удалении пользователя
-- (уже есть ON DELETE CASCADE)

-- Проверка на самолайк (пользователь не может лайкнуть свой твит)
-- (реализуется на уровне приложения)
```

### 6.2 Валидация данных

```sql
-- Дополнительные CHECK constraints для валидации

-- Проверка на пустое содержимое (уже есть)
-- Проверка на максимальную длину (уже есть)
-- Проверка на положительные счетчики (уже есть)

-- Проверка на корректность временных меток
ALTER TABLE tweets ADD CONSTRAINT tweets_timestamps_valid 
CHECK (created_at <= updated_at);

-- Проверка на корректность времени удаления
ALTER TABLE tweets ADD CONSTRAINT tweets_deletion_timestamp_valid 
CHECK (deleted_at IS NULL OR deleted_at >= created_at);
```

## 7. Стратегия масштабирования

### 7.1 Партиционирование

```sql
-- Пример партиционирования по дате (для будущего использования)
-- CREATE TABLE tweets_partitioned (
--     LIKE tweets INCLUDING ALL
-- ) PARTITION BY RANGE (created_at);

-- Создание партиций по месяцам
-- CREATE TABLE tweets_2025_01 PARTITION OF tweets_partitioned
-- FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

-- CREATE TABLE tweets_2025_02 PARTITION OF tweets_partitioned
-- FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');
```

### 7.2 Архивирование

```sql
-- Таблица для архивирования старых твитов
CREATE TABLE tweets_archive (
    LIKE tweets INCLUDING ALL
);

-- Функция для архивирования старых твитов
CREATE OR REPLACE FUNCTION archive_old_tweets(archive_date DATE)
RETURNS INTEGER AS $$
DECLARE
    archived_count INTEGER;
BEGIN
    -- Перемещение старых твитов в архив
    WITH archived AS (
        DELETE FROM tweets 
        WHERE created_at < archive_date
        RETURNING *
    )
    INSERT INTO tweets_archive SELECT * FROM archived;
    
    GET DIAGNOSTICS archived_count = ROW_COUNT;
    RETURN archived_count;
END;
$$ LANGUAGE plpgsql;
```

## 8. Мониторинг и метрики

### 8.1 Представления для мониторинга

```sql
-- Представление для мониторинга производительности
CREATE VIEW tweet_performance_metrics AS
SELECT 
    DATE_TRUNC('hour', created_at) as hour,
    COUNT(*) as tweets_count,
    AVG(likes_count) as avg_likes,
    AVG(retweets_count) as avg_retweets,
    AVG(replies_count) as avg_replies,
    MAX(likes_count) as max_likes,
    MAX(retweets_count) as max_retweets
FROM tweets
WHERE is_deleted = FALSE
GROUP BY DATE_TRUNC('hour', created_at)
ORDER BY hour DESC;

COMMENT ON VIEW tweet_performance_metrics IS 'Метрики производительности твитов по часам';

-- Представление для мониторинга активности пользователей
CREATE VIEW user_activity_metrics AS
SELECT 
    user_id,
    COUNT(*) as total_tweets,
    COUNT(CASE WHEN created_at > CURRENT_TIMESTAMP - INTERVAL '24 hours' THEN 1 END) as tweets_last_24h,
    COUNT(CASE WHEN created_at > CURRENT_TIMESTAMP - INTERVAL '7 days' THEN 1 END) as tweets_last_7d,
    SUM(likes_count) as total_likes_received,
    SUM(retweets_count) as total_retweets_received,
    AVG(likes_count) as avg_likes_per_tweet,
    MAX(likes_count) as max_likes_single_tweet
FROM tweets
WHERE is_deleted = FALSE
GROUP BY user_id
ORDER BY total_tweets DESC;

COMMENT ON VIEW user_activity_metrics IS 'Метрики активности пользователей';
```

## 9. Миграции и версионирование

### 9.1 Скрипт создания схемы

```sql
-- Создание схемы tweet_api
CREATE SCHEMA IF NOT EXISTS tweet_api;

-- Установка поиска в схему tweet_api
SET search_path TO tweet_api, public;

-- Создание всех таблиц, индексов, триггеров и представлений
-- (весь код выше должен быть выполнен в правильном порядке)
```

### 9.2 Скрипт миграции данных

```sql
-- Пример миграции данных (если нужно перенести существующие данные)
-- INSERT INTO tweet_api.tweets (id, user_id, content, created_at, updated_at)
-- SELECT id, user_id, content, created_at, updated_at
-- FROM old_tweets_table
-- WHERE is_deleted = FALSE;
```

## 10. Тестирование производительности

### 10.1 Тестовые данные

```sql
-- Функция для генерации тестовых данных
CREATE OR REPLACE FUNCTION generate_test_tweets(count INTEGER)
RETURNS VOID AS $$
DECLARE
    i INTEGER;
    user_ids UUID[];
    random_user_id UUID;
    random_content TEXT;
BEGIN
    -- Получение списка пользователей
    SELECT ARRAY_AGG(id) INTO user_ids FROM users LIMIT 100;
    
    -- Генерация тестовых твитов
    FOR i IN 1..count LOOP
        random_user_id := user_ids[1 + (random() * (array_length(user_ids, 1) - 1))::int];
        random_content := 'Test tweet content ' || i || ' - ' || md5(random()::text);
        
        INSERT INTO tweets (user_id, content)
        VALUES (random_user_id, random_content);
    END LOOP;
END;
$$ LANGUAGE plpgsql;
```

### 10.2 Запросы для тестирования производительности

```sql
-- Тестовые запросы для проверки производительности

-- 1. Получение твитов пользователя (основной запрос)
EXPLAIN ANALYZE
SELECT * FROM active_tweets 
WHERE user_id = 'some-uuid' 
ORDER BY created_at DESC 
LIMIT 20;

-- 2. Получение ленты новостей
EXPLAIN ANALYZE
SELECT * FROM active_tweets 
ORDER BY created_at DESC 
LIMIT 50;

-- 3. Поиск по содержимому
EXPLAIN ANALYZE
SELECT * FROM tweets 
WHERE to_tsvector('english', content) @@ plainto_tsquery('english', 'search term')
AND is_deleted = FALSE
ORDER BY created_at DESC
LIMIT 20;

-- 4. Получение статистики твита
EXPLAIN ANALYZE
SELECT * FROM tweet_stats 
WHERE tweet_id = 'some-uuid';
```

## 11. Рекомендации по оптимизации

### 11.1 Настройки PostgreSQL

```sql
-- Рекомендуемые настройки PostgreSQL для tweet_api

-- Увеличение shared_buffers для кэширования
-- shared_buffers = 256MB (или 25% от RAM)

-- Настройка work_mem для сложных запросов
-- work_mem = 4MB

-- Настройка maintenance_work_mem для индексов
-- maintenance_work_mem = 64MB

-- Включение автоматической очистки
-- autovacuum = on
-- autovacuum_max_workers = 3

-- Настройка логирования медленных запросов
-- log_min_duration_statement = 1000
-- log_statement = 'mod'
```

### 11.2 Мониторинг и алертинг

```sql
-- Запросы для мониторинга

-- Размер таблиц
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables 
WHERE schemaname = 'tweet_api'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Использование индексов
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes 
WHERE schemaname = 'tweet_api'
ORDER BY idx_scan DESC;

-- Медленные запросы
SELECT 
    query,
    calls,
    total_time,
    mean_time,
    rows
FROM pg_stat_statements 
WHERE query LIKE '%tweets%'
ORDER BY mean_time DESC
LIMIT 10;
```

## 12. Заключение

### 12.1 Ключевые архитектурные решения

1. **UUID идентификаторы** - обеспечивают уникальность в распределенной системе
2. **Soft delete** - сохраняет историю данных без потери производительности
3. **Дениormalized счетчики** - оптимизируют частые запросы статистики
4. **Составные индексы** - ускоряют сложные запросы (user_id + created_at)
5. **Автоматические триггеры** - поддерживают целостность счетчиков
6. **Представления** - упрощают сложные запросы и обеспечивают консистентность

### 12.2 Готовность к масштабированию

- **Партиционирование** по дате для больших объемов данных
- **Архивирование** старых твитов для поддержания производительности
- **Горизонтальное масштабирование** через stateless архитектуру
- **Мониторинг** производительности и использования ресурсов

### 12.3 Следующие шаги

1. **Реализация миграций** для создания схемы в БД
2. **Создание JPA entities** на основе данной модели
3. **Настройка мониторинга** для отслеживания производительности
4. **Тестирование производительности** с реальными данными
5. **Оптимизация** на основе результатов тестирования

---

*Документ создан: 2025-01-27*  
*Версия: 1.0*  
*Статус: Completed*
