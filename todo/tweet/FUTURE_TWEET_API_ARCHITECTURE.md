# Tweet API Service - Архитектура кэширования (Future)

## Meta
- **project**: twitter-tweet-api
- **document_type**: Caching Architecture Document
- **version**: 1.0
- **created_date**: 2025-01-27
- **status**: Future Implementation
- **analyst**: AI Assistant

## Executive Summary

Данный документ описывает архитектуру многоуровневой системы кэширования для Tweet API Service. Система кэширования спроектирована для обеспечения высокой производительности, масштабируемости и надежности при работе с большими объемами данных твитов, пользователей и социальных взаимодействий.

### Ключевые архитектурные решения кэширования
1. **Многоуровневая архитектура кэширования** с HTTP, Application, Database и CDN уровнями
2. **Redis кэширование** с JSON сериализацией и TTL
3. **Event-driven инвалидация** кэша по событиям
4. **Cache-Aside, Write-Through и Write-Behind паттерны**
5. **Стратегии кэширования** по типам данных (Static, Semi-Static, Dynamic, Real-time)
6. **Умное кэширование** с адаптивным TTL и инвалидацией
7. **Многоуровневое кэширование** (L1/L2 Cache) для оптимизации производительности

## 1. Архитектура системы кэширования

### 1.1 Многоуровневая архитектура кэширования

#### HTTP Cache
- **Cache-Control заголовки** для управления кэшированием на уровне браузера и прокси
- **ETag заголовки** для валидации кэшированного контента
- **Last-Modified заголовки** для временной валидации
- **Конфигурация**: `app.cache.http.enableETag`, `app.cache.http.enableLastModified`

#### Application Cache (Redis)
- **Redis кэширование** с JSON сериализацией и TTL
- **RedisTemplate** с Jackson2JsonRedisSerializer
- **CacheManager** с конфигурацией для разных типов данных
- **Конфигурация**: Redis host, port, password, database, timeout, connection pool

#### Database Cache
- **PostgreSQL query cache** и connection pooling
- **JPA/Hibernate second-level cache** для оптимизации запросов
- **Connection pooling** для управления соединениями с БД

#### CDN Cache (будущее)
- **Кэширование статического контента** на уровне CDN
- **Географическое распределение** кэша для глобальной доступности
- **Edge caching** для уменьшения нагрузки на origin серверы

### 1.2 Паттерны кэширования

#### Cache-Aside Pattern
- **Приложение управляет кэшем** напрямую
- **Lazy loading** данных в кэш при первом запросе
- **Инвалидация** при обновлении данных

#### Write-Through Pattern
- **Одновременная запись** в кэш и БД
- **Консистентность** данных между кэшем и БД
- **Производительность** записи может быть снижена

#### Write-Behind Pattern
- **Асинхронная запись** в БД
- **Высокая производительность** записи
- **Риск потери данных** при сбоях

### 1.3 Стратегии кэширования по типам данных

#### Static Data (Статические данные)
- **Редко изменяемые данные**: пользователи, настройки системы
- **Длительный TTL**: 600-3600 секунд
- **Высокий hit rate**: > 95%

#### Semi-Static Data (Полустатические данные)
- **Периодически изменяемые данные**: твиты, статистика
- **Средний TTL**: 60-300 секунд
- **Средний hit rate**: 70-90%

#### Dynamic Data (Динамические данные)
- **Часто изменяемые данные**: лайки, ретвиты, счетчики
- **Короткий TTL**: 10-60 секунд
- **Низкий hit rate**: 50-70%

#### Real-time Data (Данные реального времени)
- **Активность пользователей**: онлайн статус, текущие действия
- **Без кэширования** или очень короткий TTL (1-5 секунд)
- **Требует real-time обновлений**

## 2. Redis кэширование

### 2.1 Конфигурация Redis

#### RedisCacheConfig
```java
@Configuration
@EnableCaching
public class RedisCacheConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setDefaultSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        return template;
    }
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)))
            .entryTtl(Duration.ofSeconds(300));
            
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

#### Конфигурационные параметры
- **app.cache.redis.host**: localhost
- **app.cache.redis.port**: 6379
- **app.cache.redis.password**: пароль Redis
- **app.cache.redis.database**: 0
- **app.cache.redis.timeout**: 2000 мс
- **app.cache.redis.maxConnections**: 10
- **app.cache.redis.maxIdleConnections**: 5
- **app.cache.redis.minIdleConnections**: 1

### 2.2 Сервисы кэширования

#### TweetCacheService
```java
@Service
public class TweetCacheService {
    
    @CachePut(value = "tweets", key = "#tweet.id")
    public Tweet cacheTweet(Tweet tweet) {
        return tweet;
    }
    
    @Cacheable(value = "tweets", key = "#tweetId")
    public Optional<Tweet> getCachedTweet(UUID tweetId) {
        return Optional.empty();
    }
    
    @CachePut(value = "userTweets", key = "#userId + '_' + #page + '_' + #size")
    public Page<Tweet> cacheUserTweets(UUID userId, int page, int size, Page<Tweet> tweets) {
        return tweets;
    }
    
    @Cacheable(value = "userTweets", key = "#userId + '_' + #page + '_' + #size")
    public Optional<Page<Tweet>> getCachedUserTweets(UUID userId, int page, int size) {
        return Optional.empty();
    }
    
    @CacheEvict(value = "tweets", key = "#tweetId")
    public void invalidateTweet(UUID tweetId) {
        // Инвалидация кэша твита
    }
    
    @CacheEvict(value = "userTweets", allEntries = true)
    public void invalidateUserTweets(UUID userId) {
        // Инвалидация кэша твитов пользователя
    }
}
```

#### UserProfileCacheService
```java
@Service
public class UserProfileCacheService {
    
    @CachePut(value = "userProfiles", key = "#userProfile.id")
    public UserProfile cacheUserProfile(UserProfile userProfile) {
        return userProfile;
    }
    
    @Cacheable(value = "userProfiles", key = "#userId")
    public Optional<UserProfile> getCachedUserProfile(UUID userId) {
        return Optional.empty();
    }
    
    @CachePut(value = "userExists", key = "#userId")
    public Boolean cacheUserExists(UUID userId, Boolean exists) {
        return exists;
    }
    
    @Cacheable(value = "userExists", key = "#userId")
    public Optional<Boolean> getCachedUserExists(UUID userId) {
        return Optional.empty();
    }
    
    @CacheEvict(value = "userProfiles", key = "#userId")
    public void invalidateUserProfile(UUID userId) {
        // Инвалидация кэша профиля
    }
    
    @CacheEvict(value = {"userProfiles", "userExists"}, allEntries = true)
    public void invalidateAllUserCaches(UUID userId) {
        // Инвалидация всех кэшей пользователя
    }
}
```

### 2.3 TTL конфигурация

#### Время жизни кэша по типам данных
- **app.cache.ttl.tweetCacheSeconds**: 300 секунд (5 минут) - кэш твитов
- **app.cache.ttl.userProfileCacheSeconds**: 600 секунд (10 минут) - кэш профилей
- **app.cache.ttl.timelineCacheSeconds**: 30 секунд - кэш лент новостей
- **app.cache.ttl.statisticsCacheSeconds**: 60 секунд - кэш статистики
- **app.cache.ttl.userTweetsCacheSeconds**: 120 секунд (2 минуты) - кэш твитов пользователя
- **app.cache.ttl.userExistsCacheSeconds**: 300 секунд (5 минут) - кэш существования пользователя

## 3. Event-driven инвалидация кэша

### 3.1 Cache Events

#### События для инвалидации кэша
```java
// События твитов
public class TweetCreatedEvent extends ApplicationEvent {
    private final UUID tweetId;
    private final UUID userId;
}

public class TweetUpdatedEvent extends ApplicationEvent {
    private final UUID tweetId;
    private final UUID userId;
}

public class TweetDeletedEvent extends ApplicationEvent {
    private final UUID tweetId;
    private final UUID userId;
}

public class TweetLikedEvent extends ApplicationEvent {
    private final UUID tweetId;
    private final UUID userId;
}

public class TweetRetweetedEvent extends ApplicationEvent {
    private final UUID tweetId;
    private final UUID userId;
}

// События пользователей
public class UserProfileUpdatedEvent extends ApplicationEvent {
    private final UUID userId;
}

public class UserDeactivatedEvent extends ApplicationEvent {
    private final UUID userId;
}
```

### 3.2 CacheInvalidationService

```java
@Service
public class CacheInvalidationService {
    
    @EventListener
    public void handleTweetCreated(TweetCreatedEvent event) {
        invalidateUserTweets(event.getUserId());
        invalidateTimeline(event.getUserId());
    }
    
    @EventListener
    public void handleTweetUpdated(TweetUpdatedEvent event) {
        invalidateTweet(event.getTweetId());
        invalidateUserTweets(event.getUserId());
    }
    
    @EventListener
    public void handleTweetDeleted(TweetDeletedEvent event) {
        invalidateTweet(event.getTweetId());
        invalidateUserTweets(event.getUserId());
        invalidateTimeline(event.getUserId());
    }
    
    @EventListener
    public void handleTweetLiked(TweetLikedEvent event) {
        invalidateTweetStatistics(event.getTweetId());
    }
    
    @EventListener
    public void handleTweetRetweeted(TweetRetweetedEvent event) {
        invalidateTweetStatistics(event.getTweetId());
    }
    
    @EventListener
    public void handleUserProfileUpdated(UserProfileUpdatedEvent event) {
        invalidateUserProfile(event.getUserId());
    }
    
    @EventListener
    public void handleUserDeactivated(UserDeactivatedEvent event) {
        invalidateAllUserCaches(event.getUserId());
        invalidateFollowerTimelines(event.getUserId());
    }
    
    public void invalidateFollowerTimelines(UUID userId) {
        // Инвалидация кэша лент подписчиков
    }
    
    public void invalidateAllCaches() {
        // Инвалидация всех кэшей (для обслуживания)
    }
    
    public void invalidateCachesByPattern(String pattern) {
        // Инвалидация кэшей по паттерну
    }
}
```

## 4. Структура пакетов системы кэширования

### 4.1 Организация пакетов
```
cache/
├── http/           # HTTP кэширование
├── redis/          # Redis кэширование
├── service/        # Сервисы кэширования
├── event/          # События кэширования
├── metrics/        # Метрики кэширования
├── config/         # Конфигурация кэширования
└── health/         # Проверка здоровья кэша
```

### 4.2 Детальная структура

#### cache/http/
- **HttpCacheConfig** - конфигурация HTTP кэширования
- **HttpCacheService** - сервис для управления HTTP заголовками
- **ETagService** - генерация и валидация ETag заголовков
- **LastModifiedService** - управление Last-Modified заголовками

#### cache/redis/
- **RedisCacheConfig** - конфигурация Redis
- **RedisTemplateConfig** - конфигурация RedisTemplate
- **CacheManagerConfig** - конфигурация CacheManager
- **RedisConnectionConfig** - конфигурация соединений с Redis

#### cache/service/
- **TweetCacheService** - кэширование твитов
- **UserProfileCacheService** - кэширование профилей пользователей
- **TimelineCacheService** - кэширование лент новостей
- **StatisticsCacheService** - кэширование статистики
- **CacheInvalidationService** - инвалидация кэша

#### cache/event/
- **TweetCreatedEvent** - событие создания твита
- **TweetUpdatedEvent** - событие обновления твита
- **TweetDeletedEvent** - событие удаления твита
- **TweetLikedEvent** - событие лайка твита
- **TweetRetweetedEvent** - событие ретвита твита
- **UserProfileUpdatedEvent** - событие обновления профиля
- **UserDeactivatedEvent** - событие деактивации пользователя

#### cache/metrics/
- **CacheMetricsService** - метрики кэширования
- **CacheHitRateMetrics** - метрики hit rate
- **CacheSizeMetrics** - метрики размера кэша
- **CacheEvictionMetrics** - метрики вытеснения из кэша

#### cache/config/
- **CacheProperties** - свойства кэширования
- **RedisProperties** - свойства Redis
- **HttpCacheProperties** - свойства HTTP кэширования

#### cache/health/
- **CacheHealthIndicator** - проверка здоровья кэша
- **RedisHealthIndicator** - проверка здоровья Redis
- **CacheHealthService** - сервис проверки здоровья

## 5. Расширенное кэширование

### 5.1 Многоуровневое кэширование (L1/L2 Cache)

#### MultiLevelUserCacheService
```java
@Service
public class MultiLevelUserCacheService {
    
    // L1 Cache (In-Memory) - быстрый доступ
    private final Cache<UUID, UserProfile> l1Cache;
    
    // L2 Cache (Redis) - распределенный кэш
    private final RedisTemplate<String, Object> redisTemplate;
    
    public Optional<UserProfile> getUserProfile(UUID userId) {
        // Проверяем L1 Cache
        UserProfile profile = l1Cache.getIfPresent(userId);
        if (profile != null) {
            return Optional.of(profile);
        }
        
        // Проверяем L2 Cache (Redis)
        profile = (UserProfile) redisTemplate.opsForValue().get("user:" + userId);
        if (profile != null) {
            // Загружаем в L1 Cache
            l1Cache.put(userId, profile);
            return Optional.of(profile);
        }
        
        return Optional.empty();
    }
    
    public void cacheUserProfile(UUID userId, UserProfile profile) {
        // Кэшируем в L1 Cache
        l1Cache.put(userId, profile);
        
        // Кэшируем в L2 Cache (Redis)
        redisTemplate.opsForValue().set("user:" + userId, profile, Duration.ofMinutes(10));
    }
}
```

### 5.2 Умное кэширование

#### SmartUserCacheService
```java
@Service
public class SmartUserCacheService {
    
    public void cacheUserProfile(UUID userId, UserProfile profile) {
        // Адаптивный TTL на основе типа пользователя
        Duration ttl = calculateAdaptiveTTL(profile);
        
        // Кэширование с адаптивным TTL
        redisTemplate.opsForValue().set("user:" + userId, profile, ttl);
    }
    
    private Duration calculateAdaptiveTTL(UserProfile profile) {
        // Администраторы кэшируются дольше
        if (profile.getRole() == UserRole.ADMIN) {
            return Duration.ofHours(1);
        }
        
        // Модераторы кэшируются среднее время
        if (profile.getRole() == UserRole.MODERATOR) {
            return Duration.ofMinutes(30);
        }
        
        // Обычные пользователи кэшируются короткое время
        return Duration.ofMinutes(10);
    }
    
    public Optional<UserProfile> getCachedUserProfile(UUID userId) {
        UserProfile profile = (UserProfile) redisTemplate.opsForValue().get("user:" + userId);
        
        if (profile != null) {
            // Проверяем актуальность данных
            if (isStaleData(profile)) {
                // Используем устаревшие данные как fallback
                return Optional.of(profile);
            }
            return Optional.of(profile);
        }
        
        return Optional.empty();
    }
    
    private boolean isStaleData(UserProfile profile) {
        // Проверяем, не устарели ли данные
        return profile.getLastUpdated().isBefore(Instant.now().minus(Duration.ofMinutes(5)));
    }
}
```

### 5.3 Структура кэшированных данных

#### CachedUserData
```java
public class CachedUserData {
    private final UserProfile userProfile;
    private final Instant cachedAt;
    private final Duration ttl;
    private final CacheLevel level;
    
    public boolean isExpired() {
        return Instant.now().isAfter(cachedAt.plus(ttl));
    }
    
    public boolean isStale() {
        return Instant.now().isAfter(cachedAt.plus(ttl.dividedBy(2)));
    }
}

public enum CacheLevel {
    L1_MEMORY,      // In-memory кэш
    L2_REDIS,      // Redis кэш
    L3_DATABASE    // Database кэш
}
```

## 6. Мониторинг и метрики кэширования

### 6.1 Cache Metrics Service

```java
@Service
public class CacheMetricsService {
    
    private final MeterRegistry meterRegistry;
    private final Timer cacheHitTimer;
    private final Timer cacheMissTimer;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Gauge cacheSizeGauge;
    
    public void recordCacheHit(String cacheName, String key) {
        cacheHitCounter.increment(Tags.of("cache", cacheName, "key", key));
        cacheHitTimer.record(Duration.ofNanos(System.nanoTime()));
    }
    
    public void recordCacheMiss(String cacheName, String key) {
        cacheMissCounter.increment(Tags.of("cache", cacheName, "key", key));
        cacheMissTimer.record(Duration.ofNanos(System.nanoTime()));
    }
    
    public void recordCacheSize(String cacheName, long size) {
        cacheSizeGauge.set(Tags.of("cache", cacheName), size);
    }
    
    public double getCacheHitRate(String cacheName) {
        long hits = cacheHitCounter.count(Tags.of("cache", cacheName));
        long misses = cacheMissCounter.count(Tags.of("cache", cacheName));
        return hits / (double) (hits + misses);
    }
}
```

### 6.2 Health Check для кэша

#### CacheHealthIndicator
```java
@Component
public class CacheHealthIndicator implements HealthIndicator {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheMetricsService cacheMetricsService;
    
    @Override
    public Health health() {
        try {
            // Проверяем соединение с Redis
            redisTemplate.opsForValue().get("health:check");
            
            // Проверяем метрики кэша
            double hitRate = cacheMetricsService.getCacheHitRate("tweets");
            
            if (hitRate < 0.5) {
                return Health.down()
                    .withDetail("hitRate", hitRate)
                    .withDetail("message", "Cache hit rate is too low")
                    .build();
            }
            
            return Health.up()
                .withDetail("hitRate", hitRate)
                .withDetail("message", "Cache is healthy")
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## 7. Конфигурация системы кэширования

### 7.1 Основные настройки

#### application.yml
```yaml
app:
  cache:
    enabled: true
    ttl:
      tweetCacheSeconds: 300
      userProfileCacheSeconds: 600
      timelineCacheSeconds: 30
      statisticsCacheSeconds: 60
      userTweetsCacheSeconds: 120
      userExistsCacheSeconds: 300
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0
      timeout: 2000
      maxConnections: 10
      maxIdleConnections: 5
      minIdleConnections: 1
    http:
      enableETag: true
      enableLastModified: true

spring:
  cache:
    type: redis
    redis:
      time-to-live: 300000
      cache-null-values: false
      enable-statistics: true
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 10
          max-idle: 5
          min-idle: 1
```

### 7.2 Конфигурация через Properties

#### CacheProperties
```java
@ConfigurationProperties(prefix = "app.cache")
@Data
public class CacheProperties {
    private boolean enabled = true;
    private Ttl ttl = new Ttl();
    private Redis redis = new Redis();
    private Http http = new Http();
    
    @Data
    public static class Ttl {
        private int tweetCacheSeconds = 300;
        private int userProfileCacheSeconds = 600;
        private int timelineCacheSeconds = 30;
        private int statisticsCacheSeconds = 60;
        private int userTweetsCacheSeconds = 120;
        private int userExistsCacheSeconds = 300;
    }
    
    @Data
    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private String password = "";
        private int database = 0;
        private int timeout = 2000;
        private int maxConnections = 10;
        private int maxIdleConnections = 5;
        private int minIdleConnections = 1;
    }
    
    @Data
    public static class Http {
        private boolean enableETag = true;
        private boolean enableLastModified = true;
    }
}
```

## 8. Производительность и оптимизация

### 8.1 Критерии производительности

#### Целевые метрики
- **Cache hit rate**: > 80% для часто запрашиваемых данных
- **Response time**: < 50ms для кэшированных запросов
- **Cache size**: оптимальное использование памяти
- **Eviction rate**: минимальное количество вытеснений

#### Оптимизация запросов
- **Batch loading** для множественных запросов
- **Projection** для уменьшения передачи данных
- **Index hints** для оптимизации запросов к БД
- **Connection pooling** для управления соединениями

### 8.2 Мониторинг производительности

#### Ключевые метрики
- **Cache hit/miss ratio** по типам данных
- **Cache size** и использование памяти
- **Eviction statistics** и причины вытеснения
- **Response time** для кэшированных и некэшированных запросов
- **Redis connection pool** статистика

#### Алерты и уведомления
- **Low hit rate** (< 70%) для критических кэшей
- **High eviction rate** (> 10% в минуту)
- **Redis connection issues** или таймауты
- **Memory usage** превышение лимитов

## 9. Безопасность кэширования

### 9.1 Защита данных

#### Шифрование
- **TLS/SSL** для соединений с Redis
- **Шифрование данных** в кэше для чувствительной информации
- **Access control** для доступа к Redis

#### Изоляция данных
- **Namespace** для разделения данных разных сервисов
- **TTL** для автоматического удаления данных
- **Eviction policies** для управления памятью

### 9.2 Аудит и логирование

#### Логирование доступа к кэшу
- **Cache access logs** для аудита
- **Sensitive data masking** в логах
- **Performance metrics** для мониторинга

## 10. Оптимизация запросов и пагинация

### 10.1 Оптимизация запросов к базе данных

#### Составные индексы
- **Индексы для основных запросов** (user_id + created_at)
- **Full-text search индексы** для поиска по содержимому
- **Специализированные индексы** для аналитических запросов
- **Партиционированные индексы** для больших объемов данных
- **Query optimization** с использованием EXPLAIN ANALYZE
- **Batch операции** для оптимизации множественных запросов
- **Connection pooling** для управления соединениями с БД

### 10.2 Конфигурация системы пагинации

#### Многостратегическая пагинация
- **Offset-based Pagination** - для статических списков с произвольным доступом к страницам
- **Cursor-based Pagination** - для динамических данных с высокой производительностью и консистентностью
- **Hybrid Pagination** - комбинация подходов с автоматическим выбором стратегии

#### Классификация данных по стратегиям
- **Статические данные (OFFSET)** - список пользователей, административные списки, справочные данные
- **Динамические данные (CURSOR)** - лента новостей, твиты пользователя, лайки и ретвиты
- **Смешанные данные (HYBRID)** - поиск по твитам, фильтрованные списки

### 10.3 Offset-based пагинация

#### OffsetPaginationService
- **getTweets** - получение твитов с пагинацией (page, size, sort)
- **getTweetsByUser** - получение твитов пользователя с пагинацией
- **getTweetsWithFilter** - получение твитов с фильтрацией и пагинацией
- **adjustPageSize** - корректировка размера страницы в пределах лимитов
- **OffsetPaginationResponse** - ответ с метаданными пагинации
- **OffsetPaginationMetadata** - метаданные (size, number, totalElements, totalPages, first, last, hasNext, hasPrevious)

### 10.4 Cursor-based пагинация

#### CursorPaginationService
- **getTweets** - получение твитов с курсором (cursor, size, direction)
- **getTweetsByUser** - получение твитов пользователя с курсором
- **getTimeline** - получение ленты новостей с курсором
- **getTweetsForward** - получение твитов вперед от курсора
- **getTweetsBackward** - получение твитов назад от курсора
- **getTweetsInitial** - получение начальных твитов
- **getNextCursor** - получение курсора для следующей страницы
- **getPreviousCursor** - получение курсора для предыдущей страницы
- **encodeCursor** - кодирование курсора в Base64
- **parseCursor** - парсинг курсора из Base64
- **CursorPaginationResponse** - ответ с метаданными курсорной пагинации
- **CursorPaginationMetadata** - метаданные (size, hasNext, hasPrevious, nextCursor, previousCursor)

### 10.5 Оптимизированные запросы

#### Custom Repository Queries
- **findAllByIsDeletedFalse** - получение всех твитов с пагинацией
- **findByUserIdAndIsDeletedFalse** - получение твитов пользователя с пагинацией
- **findByIdLessThanAndIsDeletedFalseOrderByIdDesc** - cursor-based запрос вперед
- **findByIdGreaterThanAndIsDeletedFalseOrderByIdAsc** - cursor-based запрос назад
- **findByUserIdAndIdLessThanAndIsDeletedFalseOrderByIdDesc** - cursor-based запрос пользователя вперед
- **findByUserIdAndIdGreaterThanAndIsDeletedFalseOrderByIdAsc** - cursor-based запрос пользователя назад
- **findTimelineInitial** - получение начальной ленты новостей
- **findTimelineForward** - получение ленты новостей вперед
- **findTimelineBackward** - получение ленты новостей назад
- **findTweetSummaries** - получение кратких данных твитов с проекцией
- **findTweetSummariesByUser** - получение кратких данных твитов пользователя
- **countByUserIdAndIsDeletedFalse** - подсчет твитов пользователя
- **countByIsDeletedFalse** - подсчет всех твитов
- **existsByIdAndIsDeletedFalse** - проверка существования твита
- **existsByUserIdAndIsDeletedFalse** - проверка существования твитов пользователя

### 10.6 Query Optimization Service

#### QueryOptimizationService
- **executeOptimizedQuery** - выполнение оптимизированного запроса
- **determineOptimizationStrategy** - определение стратегии оптимизации
- **executeWithProjection** - выполнение с проекцией для уменьшения передачи данных
- **executeWithIndexHint** - выполнение с подсказками индексов
- **executeWithBatchLoading** - выполнение с batch loading для больших наборов данных
- **executeWithCache** - выполнение с кэшированием для часто используемых данных
- **executeStandard** - стандартное выполнение без оптимизации
- **estimateResultSize** - оценка размера результата для принятия решений об оптимизации
- **QueryOptimizationStrategy** - enum стратегий (USE_PROJECTION, USE_INDEX_HINT, USE_BATCH_LOADING, USE_CACHE, STANDARD)
- **QueryType** - enum типов запросов (TWEETS, USER_TWEETS, TIMELINE, POPULAR_TWEETS)

### 10.7 Обработка больших объемов данных

#### LargeDatasetHandler
- **streamLargeDataset** - обработка с streaming для больших наборов данных
- **parallelLargeDataset** - обработка с параллельным выполнением
- **chunkLargeDataset** - обработка с разбиением на чанки
- **getTotalCount** - получение общего количества для больших наборов данных
- **Stream.iterate** - итерация по батчам данных
- **IntStream.range().parallel()** - параллельная обработка батчей
- **streamingBatchSize** - размер батча для streaming
- **parallelBatchSize** - размер батча для параллельной обработки

### 10.8 Мониторинг и метрики пагинации

#### PaginationMetricsService
- **recordPaginationRequest** - запись метрик запроса пагинации (strategy, queryType, pageSize, executionTime)
- **recordPaginationPerformance** - запись метрик производительности (strategy, queryType, resultSize, totalElements)
- **recordPaginationError** - запись метрик ошибок пагинации (strategy, queryType, errorType)
- **Timer** - измерение времени выполнения запросов пагинации
- **Counter** - подсчет количества запросов пагинации
- **Gauge** - измерение размера результата и общего количества элементов
- **Tags** - теги для метрик (strategy, query_type, page_size, error_type)

### 10.9 Конфигурация пагинации

#### PaginationProperties
```yaml
pagination:
  enabled: true
  defaults:
    defaultPageSize: 20
    defaultStrategy: CURSOR
    defaultSort: createdAt DESC
  limits:
    maxPageSize: 100
    minPageSize: 1
    offsetThreshold: 1000
  optimization:
    projectionThreshold: 1000
    batchThreshold: 500
    enableQueryOptimization: true
    enableIndexHints: true
  performance:
    streamingBatchSize: 1000
    parallelBatchSize: 500
    maxConcurrentQueries: 10
    queryTimeout: 30
```

### 10.10 Структура пакетов

#### Пакеты системы пагинации
```
pagination/
├── offset/          # Offset-based пагинация
├── cursor/          # Cursor-based пагинация
├── hybrid/          # Hybrid пагинация
├── optimization/    # Оптимизация запросов
├── handler/         # Обработка больших объемов данных
├── metrics/         # Метрики пагинации
├── config/          # Конфигурация пагинации
└── response/        # Ответы пагинации
```

### 10.11 Конфигурация JPA/Hibernate

#### Hibernate оптимизации
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
        batch_versioned_data: true
    open-in-view: false
```

### 10.12 Мониторинг производительности

#### SQL Мониторинг
- **format_sql: true** для форматирования SQL запросов
- **show_sql: false** в production для производительности
- **Индексы** для оптимизации частых запросов
- **Статистика** использования индексов через PostgreSQL
- **Стандартизированные ответы** с метаданными (timestamp, requestId)
- **Graceful error handling** с детальными кодами ошибок
- **Versioning** для обратной совместимости API

#### Метрики производительности
- **Response time** для всех типов запросов
- **Throughput** (запросов в секунду)
- **Database connection pool** статистика
- **Query execution time** по типам запросов
- **Index usage statistics** для оптимизации

## 11. Планы развития

### 11.1 Краткосрочные цели (1-3 месяца)
1. **Реализация базового Redis кэширования** для твитов и пользователей
2. **Настройка TTL** и базовой инвалидации кэша
3. **Интеграция с Spring Cache** и аннотациями
4. **Базовый мониторинг** и метрики кэша
5. **Реализация Offset-based пагинации** для простых запросов
6. **Настройка базовых индексов** для оптимизации запросов

### 11.2 Среднесрочные цели (3-6 месяцев)
1. **Event-driven инвалидация** кэша
2. **Многоуровневое кэширование** (L1/L2)
3. **Умное кэширование** с адаптивным TTL
4. **Расширенный мониторинг** и алерты
5. **Cursor-based пагинация** для динамических данных
6. **Query Optimization Service** с автоматическим выбором стратегии
7. **Batch операции** для больших наборов данных

### 11.3 Долгосрочные цели (6-12 месяцев)
1. **CDN интеграция** для статического контента
2. **Distributed caching** для кластерных развертываний
3. **Machine learning** для оптимизации кэширования
4. **Advanced analytics** для анализа паттернов использования
5. **Hybrid пагинация** с автоматическим выбором стратегии
6. **Партиционирование таблиц** для масштабирования
7. **Read replicas** для распределения нагрузки чтения

---

*Документ создан: 2025-01-27*  
*Версия: 1.0*  
*Статус: Future Implementation*
