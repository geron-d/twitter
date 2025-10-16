# Проектирование системы кэширования Tweet API

## Meta
- project: twitter-tweet-api
- design_date: 2025-01-27
- designer: AI Assistant
- version: 1.0
- status: completed
- step: 4.13

## Executive Summary

Данный документ содержит детальное проектирование системы кэширования для сервиса Tweet API. Система спроектирована с учетом высоких требований к производительности, интеграции с существующими паттернами и обеспечения масштабируемости для обработки больших объемов данных.

## 1. Архитектурные принципы кэширования

### 1.1 Многоуровневая архитектура кэширования

#### Уровни кэширования:
1. **HTTP Cache** - кэширование на уровне HTTP заголовков
2. **Application Cache** - кэширование на уровне приложения (Redis)
3. **Database Cache** - кэширование на уровне БД (PostgreSQL)
4. **CDN Cache** - кэширование на уровне CDN (будущее)

#### Принципы:
- **Cache-Aside Pattern** - приложение управляет кэшем
- **Write-Through Pattern** - запись в кэш и БД одновременно
- **Write-Behind Pattern** - асинхронная запись в БД
- **TTL-based Expiration** - время жизни кэша
- **Event-driven Invalidation** - инвалидация по событиям

### 1.2 Стратегии кэширования по типам данных

#### Классификация данных:
- **Static Data** - редко изменяемые данные (пользователи, настройки)
- **Semi-Static Data** - периодически изменяемые данные (твиты, статистика)
- **Dynamic Data** - часто изменяемые данные (лайки, ретвиты)
- **Real-time Data** - данные в реальном времени (активность пользователей)

## 2. HTTP кэширование

### 2.1 HTTP Cache Headers

#### CacheControlService
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheControlService {
    
    private final CacheControlProperties properties;
    
    /**
     * Sets appropriate cache headers for tweet responses
     *
     * @param response the HTTP response
     * @param cacheType the type of data being cached
     */
    public void setCacheHeaders(HttpServletResponse response, CacheType cacheType) {
        CacheControl cacheControl = getCacheControl(cacheType);
        response.setHeader("Cache-Control", cacheControl.getHeaderValue());
        
        if (cacheControl.getMaxAge() > 0) {
            response.setHeader("Expires", getExpiresHeader(cacheControl.getMaxAge()));
        }
        
        if (cacheType == CacheType.TWEET_CONTENT) {
            response.setHeader("ETag", generateETag());
        }
    }
    
    /**
     * Sets cache headers for user timeline responses
     *
     * @param response the HTTP response
     * @param userId the user ID
     * @param lastModified the last modified timestamp
     */
    public void setTimelineCacheHeaders(HttpServletResponse response, UUID userId, LocalDateTime lastModified) {
        CacheControl cacheControl = CacheControl.maxAge(Duration.ofSeconds(properties.getTimelineCacheSeconds()));
        response.setHeader("Cache-Control", cacheControl.getHeaderValue());
        response.setHeader("Last-Modified", formatLastModified(lastModified));
        response.setHeader("ETag", generateTimelineETag(userId, lastModified));
    }
    
    /**
     * Sets cache headers for user profile responses
     *
     * @param response the HTTP response
     * @param userId the user ID
     * @param lastModified the last modified timestamp
     */
    public void setUserProfileCacheHeaders(HttpServletResponse response, UUID userId, LocalDateTime lastModified) {
        CacheControl cacheControl = CacheControl.maxAge(Duration.ofSeconds(properties.getUserProfileCacheSeconds()));
        response.setHeader("Cache-Control", cacheControl.getHeaderValue());
        response.setHeader("Last-Modified", formatLastModified(lastModified));
        response.setHeader("ETag", generateUserETag(userId, lastModified));
    }
    
    /**
     * Sets no-cache headers for dynamic content
     *
     * @param response the HTTP response
     */
    public void setNoCacheHeaders(HttpServletResponse response) {
        CacheControl cacheControl = CacheControl.noCache().noStore().mustRevalidate();
        response.setHeader("Cache-Control", cacheControl.getHeaderValue());
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }
    
    private CacheControl getCacheControl(CacheType cacheType) {
        return switch (cacheType) {
            case TWEET_CONTENT -> CacheControl.maxAge(Duration.ofSeconds(properties.getTweetCacheSeconds()));
            case USER_PROFILE -> CacheControl.maxAge(Duration.ofSeconds(properties.getUserProfileCacheSeconds()));
            case TIMELINE -> CacheControl.maxAge(Duration.ofSeconds(properties.getTimelineCacheSeconds()));
            case STATISTICS -> CacheControl.maxAge(Duration.ofSeconds(properties.getStatisticsCacheSeconds()));
            case DYNAMIC_CONTENT -> CacheControl.noCache().noStore().mustRevalidate();
        };
    }
    
    private String getExpiresHeader(long maxAgeSeconds) {
        Instant expires = Instant.now().plusSeconds(maxAgeSeconds);
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(expires.atZone(ZoneOffset.UTC));
    }
    
    private String formatLastModified(LocalDateTime lastModified) {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(lastModified.atZone(ZoneOffset.UTC));
    }
    
    private String generateETag() {
        return "\"" + UUID.randomUUID().toString() + "\"";
    }
    
    private String generateTimelineETag(UUID userId, LocalDateTime lastModified) {
        String content = userId.toString() + lastModified.toString();
        String hash = DigestUtils.md5Hex(content);
        return "\"" + hash + "\"";
    }
    
    private String generateUserETag(UUID userId, LocalDateTime lastModified) {
        String content = userId.toString() + lastModified.toString();
        String hash = DigestUtils.md5Hex(content);
        return "\"" + hash + "\"";
    }
}
```

### 2.2 HTTP Cache Interceptor

#### HttpCacheInterceptor
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class HttpCacheInterceptor implements HandlerInterceptor {
    
    private final CacheControlService cacheControlService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Check for conditional requests
        if (isConditionalRequest(request)) {
            return handleConditionalRequest(request, response);
        }
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // Set cache headers based on request path
        String requestPath = request.getRequestURI();
        CacheType cacheType = determineCacheType(requestPath, request.getMethod());
        
        if (cacheType != CacheType.DYNAMIC_CONTENT) {
            cacheControlService.setCacheHeaders(response, cacheType);
        } else {
            cacheControlService.setNoCacheHeaders(response);
        }
    }
    
    private boolean isConditionalRequest(HttpServletRequest request) {
        return request.getHeader("If-None-Match") != null || 
               request.getHeader("If-Modified-Since") != null;
    }
    
    private boolean handleConditionalRequest(HttpServletRequest request, HttpServletResponse response) {
        String ifNoneMatch = request.getHeader("If-None-Match");
        String ifModifiedSince = request.getHeader("If-Modified-Since");
        
        // Check ETag
        if (ifNoneMatch != null) {
            String currentETag = getCurrentETag(request);
            if (ifNoneMatch.equals(currentETag)) {
                response.setStatus(HttpStatus.NOT_MODIFIED.value());
                return false;
            }
        }
        
        // Check Last-Modified
        if (ifModifiedSince != null) {
            LocalDateTime lastModified = getLastModified(request);
            if (lastModified != null && isNotModifiedSince(lastModified, ifModifiedSince)) {
                response.setStatus(HttpStatus.NOT_MODIFIED.value());
                return false;
            }
        }
        
        return true;
    }
    
    private CacheType determineCacheType(String requestPath, String method) {
        if (!"GET".equals(method)) {
            return CacheType.DYNAMIC_CONTENT;
        }
        
        if (requestPath.matches("/api/v1/tweets/\\w+")) {
            return CacheType.TWEET_CONTENT;
        }
        
        if (requestPath.matches("/api/v1/tweets/user/\\w+")) {
            return CacheType.TIMELINE;
        }
        
        if (requestPath.matches("/api/v1/tweets/timeline/\\w+")) {
            return CacheType.TIMELINE;
        }
        
        if (requestPath.matches("/api/v1/tweets/\\w+/likes")) {
            return CacheType.STATISTICS;
        }
        
        return CacheType.DYNAMIC_CONTENT;
    }
    
    private String getCurrentETag(HttpServletRequest request) {
        // Implementation would extract ETag from current resource
        return null;
    }
    
    private LocalDateTime getLastModified(HttpServletRequest request) {
        // Implementation would get last modified time from current resource
        return null;
    }
    
    private boolean isNotModifiedSince(LocalDateTime lastModified, String ifModifiedSince) {
        try {
            LocalDateTime ifModifiedSinceTime = LocalDateTime.parse(ifModifiedSince, DateTimeFormatter.RFC_1123_DATE_TIME);
            return !lastModified.isAfter(ifModifiedSinceTime);
        } catch (Exception e) {
            return false;
        }
    }
}
```

## 3. Application-level кэширование

### 3.1 Redis Cache Configuration

#### RedisCacheConfig
```java
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisCacheConfig {
    
    private final RedisConnectionFactory redisConnectionFactory;
    private final CacheProperties cacheProperties;
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        
        // JSON serialization
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(objectMapper);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory)
            .cacheDefaults(cacheConfiguration());
        
        // Configure specific caches
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Tweet cache configuration
        cacheConfigurations.put("tweets", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(cacheProperties.getTweetCacheSeconds()))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));
        
        // User profile cache configuration
        cacheConfigurations.put("user-profiles", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(cacheProperties.getUserProfileCacheSeconds()))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));
        
        // Timeline cache configuration
        cacheConfigurations.put("timelines", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(cacheProperties.getTimelineCacheSeconds()))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));
        
        // Statistics cache configuration
        cacheConfigurations.put("statistics", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(cacheProperties.getStatisticsCacheSeconds()))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));
        
        builder.withInitialCacheConfigurations(cacheConfigurations);
        
        return builder.build();
    }
    
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
```

### 3.2 Tweet Cache Service

#### TweetCacheService
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class TweetCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties cacheProperties;
    
    private static final String TWEET_KEY_PREFIX = "tweet:";
    private static final String USER_TWEETS_KEY_PREFIX = "user_tweets:";
    private static final String TIMELINE_KEY_PREFIX = "timeline:";
    private static final String STATISTICS_KEY_PREFIX = "stats:";
    
    /**
     * Caches a tweet
     *
     * @param tweet the tweet to cache
     */
    @CachePut(value = "tweets", key = "#tweet.id")
    public Tweet cacheTweet(Tweet tweet) {
        log.debug("Caching tweet: {}", tweet.getId());
        return tweet;
    }
    
    /**
     * Gets a cached tweet
     *
     * @param tweetId the tweet ID
     * @return cached tweet or null
     */
    @Cacheable(value = "tweets", key = "#tweetId")
    public Tweet getCachedTweet(UUID tweetId) {
        log.debug("Cache miss for tweet: {}", tweetId);
        return null;
    }
    
    /**
     * Caches user tweets with pagination
     *
     * @param userId the user ID
     * @param page the page number
     * @param tweets the tweets to cache
     */
    public void cacheUserTweets(UUID userId, int page, List<Tweet> tweets) {
        String key = USER_TWEETS_KEY_PREFIX + userId + ":" + page;
        redisTemplate.opsForValue().set(key, tweets, Duration.ofSeconds(cacheProperties.getUserTweetsCacheSeconds()));
        log.debug("Cached {} tweets for user {} page {}", tweets.size(), userId, page);
    }
    
    /**
     * Gets cached user tweets
     *
     * @param userId the user ID
     * @param page the page number
     * @return cached tweets or null
     */
    @SuppressWarnings("unchecked")
    public List<Tweet> getCachedUserTweets(UUID userId, int page) {
        String key = USER_TWEETS_KEY_PREFIX + userId + ":" + page;
        List<Tweet> tweets = (List<Tweet>) redisTemplate.opsForValue().get(key);
        if (tweets != null) {
            log.debug("Cache hit for user {} page {}", userId, page);
        } else {
            log.debug("Cache miss for user {} page {}", userId, page);
        }
        return tweets;
    }
    
    /**
     * Caches user timeline
     *
     * @param userId the user ID
     * @param page the page number
     * @param tweets the timeline tweets
     */
    public void cacheTimeline(UUID userId, int page, List<Tweet> tweets) {
        String key = TIMELINE_KEY_PREFIX + userId + ":" + page;
        redisTemplate.opsForValue().set(key, tweets, Duration.ofSeconds(cacheProperties.getTimelineCacheSeconds()));
        log.debug("Cached {} timeline tweets for user {} page {}", tweets.size(), userId, page);
    }
    
    /**
     * Gets cached timeline
     *
     * @param userId the user ID
     * @param page the page number
     * @return cached timeline or null
     */
    @SuppressWarnings("unchecked")
    public List<Tweet> getCachedTimeline(UUID userId, int page) {
        String key = TIMELINE_KEY_PREFIX + userId + ":" + page;
        List<Tweet> tweets = (List<Tweet>) redisTemplate.opsForValue().get(key);
        if (tweets != null) {
            log.debug("Cache hit for timeline user {} page {}", userId, page);
        } else {
            log.debug("Cache miss for timeline user {} page {}", userId, page);
        }
        return tweets;
    }
    
    /**
     * Caches tweet statistics
     *
     * @param tweetId the tweet ID
     * @param stats the statistics
     */
    public void cacheTweetStatistics(UUID tweetId, TweetStats stats) {
        String key = STATISTICS_KEY_PREFIX + tweetId;
        redisTemplate.opsForValue().set(key, stats, Duration.ofSeconds(cacheProperties.getStatisticsCacheSeconds()));
        log.debug("Cached statistics for tweet: {}", tweetId);
    }
    
    /**
     * Gets cached tweet statistics
     *
     * @param tweetId the tweet ID
     * @return cached statistics or null
     */
    public TweetStats getCachedTweetStatistics(UUID tweetId) {
        String key = STATISTICS_KEY_PREFIX + tweetId;
        TweetStats stats = (TweetStats) redisTemplate.opsForValue().get(key);
        if (stats != null) {
            log.debug("Cache hit for statistics tweet: {}", tweetId);
        } else {
            log.debug("Cache miss for statistics tweet: {}", tweetId);
        }
        return stats;
    }
    
    /**
     * Invalidates tweet cache
     *
     * @param tweetId the tweet ID
     */
    @CacheEvict(value = "tweets", key = "#tweetId")
    public void invalidateTweet(UUID tweetId) {
        log.debug("Invalidated cache for tweet: {}", tweetId);
        
        // Also invalidate related caches
        invalidateTweetStatistics(tweetId);
        invalidateRelatedCaches(tweetId);
    }
    
    /**
     * Invalidates user tweets cache
     *
     * @param userId the user ID
     */
    public void invalidateUserTweets(UUID userId) {
        String pattern = USER_TWEETS_KEY_PREFIX + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Invalidated {} user tweet cache entries for user: {}", keys.size(), userId);
        }
    }
    
    /**
     * Invalidates timeline cache
     *
     * @param userId the user ID
     */
    public void invalidateTimeline(UUID userId) {
        String pattern = TIMELINE_KEY_PREFIX + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Invalidated {} timeline cache entries for user: {}", keys.size(), userId);
        }
    }
    
    /**
     * Invalidates tweet statistics cache
     *
     * @param tweetId the tweet ID
     */
    public void invalidateTweetStatistics(UUID tweetId) {
        String key = STATISTICS_KEY_PREFIX + tweetId;
        redisTemplate.delete(key);
        log.debug("Invalidated statistics cache for tweet: {}", tweetId);
    }
    
    private void invalidateRelatedCaches(UUID tweetId) {
        // Invalidate caches that might contain this tweet
        // This is a simplified implementation
        log.debug("Invalidating related caches for tweet: {}", tweetId);
    }
}
```

### 3.3 User Profile Cache Service

#### UserProfileCacheService
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties cacheProperties;
    
    private static final String USER_PROFILE_KEY_PREFIX = "user_profile:";
    private static final String USER_EXISTS_KEY_PREFIX = "user_exists:";
    
    /**
     * Caches user profile information
     *
     * @param userId the user ID
     * @param userInfo the user information
     */
    @CachePut(value = "user-profiles", key = "#userId")
    public UserInfo cacheUserProfile(UUID userId, UserInfo userInfo) {
        log.debug("Caching user profile: {}", userId);
        return userInfo;
    }
    
    /**
     * Gets cached user profile
     *
     * @param userId the user ID
     * @return cached user profile or null
     */
    @Cacheable(value = "user-profiles", key = "#userId")
    public UserInfo getCachedUserProfile(UUID userId) {
        log.debug("Cache miss for user profile: {}", userId);
        return null;
    }
    
    /**
     * Caches user existence check
     *
     * @param userId the user ID
     * @param exists whether user exists
     */
    public void cacheUserExists(UUID userId, boolean exists) {
        String key = USER_EXISTS_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, exists, Duration.ofSeconds(cacheProperties.getUserExistsCacheSeconds()));
        log.debug("Cached user existence: {} = {}", userId, exists);
    }
    
    /**
     * Gets cached user existence
     *
     * @param userId the user ID
     * @return cached existence or null
     */
    public Boolean getCachedUserExists(UUID userId) {
        String key = USER_EXISTS_KEY_PREFIX + userId;
        Boolean exists = (Boolean) redisTemplate.opsForValue().get(key);
        if (exists != null) {
            log.debug("Cache hit for user existence: {} = {}", userId, exists);
        } else {
            log.debug("Cache miss for user existence: {}", userId);
        }
        return exists;
    }
    
    /**
     * Invalidates user profile cache
     *
     * @param userId the user ID
     */
    @CacheEvict(value = "user-profiles", key = "#userId")
    public void invalidateUserProfile(UUID userId) {
        log.debug("Invalidated cache for user profile: {}", userId);
        
        // Also invalidate user existence cache
        String existsKey = USER_EXISTS_KEY_PREFIX + userId;
        redisTemplate.delete(existsKey);
    }
    
    /**
     * Invalidates all user-related caches
     *
     * @param userId the user ID
     */
    public void invalidateAllUserCaches(UUID userId) {
        invalidateUserProfile(userId);
        
        // Invalidate user tweets and timeline caches
        String userTweetsPattern = "user_tweets:" + userId + ":*";
        String timelinePattern = "timeline:" + userId + ":*";
        
        Set<String> keys = new HashSet<>();
        keys.addAll(redisTemplate.keys(userTweetsPattern));
        keys.addAll(redisTemplate.keys(timelinePattern));
        
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Invalidated {} user-related cache entries for user: {}", keys.size(), userId);
        }
    }
}
```

## 4. Инвалидация кэша

### 4.1 Cache Invalidation Service

#### CacheInvalidationService
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidationService {
    
    private final TweetCacheService tweetCacheService;
    private final UserProfileCacheService userProfileCacheService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Invalidates caches when a tweet is created
     *
     * @param tweet the created tweet
     */
    @EventListener
    public void handleTweetCreated(TweetCreatedEvent event) {
        Tweet tweet = event.getTweet();
        
        // Invalidate user tweets cache
        tweetCacheService.invalidateUserTweets(tweet.getUserId());
        
        // Invalidate timeline caches for followers
        invalidateFollowerTimelines(tweet.getUserId());
        
        log.info("Invalidated caches for tweet creation: {}", tweet.getId());
    }
    
    /**
     * Invalidates caches when a tweet is updated
     *
     * @param tweet the updated tweet
     */
    @EventListener
    public void handleTweetUpdated(TweetUpdatedEvent event) {
        Tweet tweet = event.getTweet();
        
        // Invalidate tweet cache
        tweetCacheService.invalidateTweet(tweet.getId());
        
        // Invalidate user tweets cache
        tweetCacheService.invalidateUserTweets(tweet.getUserId());
        
        log.info("Invalidated caches for tweet update: {}", tweet.getId());
    }
    
    /**
     * Invalidates caches when a tweet is deleted
     *
     * @param tweetId the deleted tweet ID
     */
    @EventListener
    public void handleTweetDeleted(TweetDeletedEvent event) {
        UUID tweetId = event.getTweetId();
        UUID userId = event.getUserId();
        
        // Invalidate tweet cache
        tweetCacheService.invalidateTweet(tweetId);
        
        // Invalidate user tweets cache
        tweetCacheService.invalidateUserTweets(userId);
        
        // Invalidate statistics cache
        tweetCacheService.invalidateTweetStatistics(tweetId);
        
        log.info("Invalidated caches for tweet deletion: {}", tweetId);
    }
    
    /**
     * Invalidates caches when a tweet is liked
     *
     * @param tweetId the liked tweet ID
     */
    @EventListener
    public void handleTweetLiked(TweetLikedEvent event) {
        UUID tweetId = event.getTweetId();
        
        // Invalidate statistics cache
        tweetCacheService.invalidateTweetStatistics(tweetId);
        
        log.debug("Invalidated statistics cache for tweet like: {}", tweetId);
    }
    
    /**
     * Invalidates caches when a tweet is retweeted
     *
     * @param tweetId the retweeted tweet ID
     */
    @EventListener
    public void handleTweetRetweeted(TweetRetweetedEvent event) {
        UUID tweetId = event.getTweetId();
        
        // Invalidate statistics cache
        tweetCacheService.invalidateTweetStatistics(tweetId);
        
        log.debug("Invalidated statistics cache for tweet retweet: {}", tweetId);
    }
    
    /**
     * Invalidates caches when user profile is updated
     *
     * @param userId the user ID
     */
    @EventListener
    public void handleUserProfileUpdated(UserProfileUpdatedEvent event) {
        UUID userId = event.getUserId();
        
        // Invalidate user profile cache
        userProfileCacheService.invalidateUserProfile(userId);
        
        log.info("Invalidated caches for user profile update: {}", userId);
    }
    
    /**
     * Invalidates caches when user is deactivated
     *
     * @param userId the user ID
     */
    @EventListener
    public void handleUserDeactivated(UserDeactivatedEvent event) {
        UUID userId = event.getUserId();
        
        // Invalidate all user-related caches
        userProfileCacheService.invalidateAllUserCaches(userId);
        
        log.info("Invalidated all caches for user deactivation: {}", userId);
    }
    
    /**
     * Invalidates timeline caches for followers
     *
     * @param userId the user ID
     */
    private void invalidateFollowerTimelines(UUID userId) {
        // This would typically involve getting followers from follow-service
        // For now, we'll implement a simplified version
        
        // Invalidate timeline caches for users who might be following this user
        String pattern = "timeline:*";
        Set<String> keys = redisTemplate.keys(pattern);
        
        if (keys != null && !keys.isEmpty()) {
            // In a real implementation, we would filter by actual followers
            // For now, we'll invalidate a subset of timeline caches
            List<String> keysToInvalidate = keys.stream()
                .limit(100) // Limit to prevent performance issues
                .collect(Collectors.toList());
            
            redisTemplate.delete(keysToInvalidate);
            log.debug("Invalidated {} timeline caches for followers of user: {}", keysToInvalidate.size(), userId);
        }
    }
    
    /**
     * Invalidates all caches (for maintenance)
     */
    public void invalidateAllCaches() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Invalidated all caches: {} entries", keys.size());
        }
    }
    
    /**
     * Invalidates caches by pattern
     *
     * @param pattern the key pattern
     */
    public void invalidateCachesByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Invalidated {} cache entries matching pattern: {}", keys.size(), pattern);
        }
    }
}
```

### 4.2 Cache Events

#### Tweet Events
```java
@Getter
@AllArgsConstructor
public class TweetCreatedEvent {
    private final Tweet tweet;
    private final Instant timestamp;
}

@Getter
@AllArgsConstructor
public class TweetUpdatedEvent {
    private final Tweet tweet;
    private final Instant timestamp;
}

@Getter
@AllArgsConstructor
public class TweetDeletedEvent {
    private final UUID tweetId;
    private final UUID userId;
    private final Instant timestamp;
}

@Getter
@AllArgsConstructor
public class TweetLikedEvent {
    private final UUID tweetId;
    private final UUID userId;
    private final Instant timestamp;
}

@Getter
@AllArgsConstructor
public class TweetRetweetedEvent {
    private final UUID tweetId;
    private final UUID userId;
    private final Instant timestamp;
}
```

#### User Events
```java
@Getter
@AllArgsConstructor
public class UserProfileUpdatedEvent {
    private final UUID userId;
    private final Instant timestamp;
}

@Getter
@AllArgsConstructor
public class UserDeactivatedEvent {
    private final UUID userId;
    private final Instant timestamp;
}
```

## 5. Кэширование статистики

### 5.1 Statistics Cache Service

#### StatisticsCacheService
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties cacheProperties;
    
    private static final String LIKES_COUNT_KEY_PREFIX = "likes_count:";
    private static final String RETWEETS_COUNT_KEY_PREFIX = "retweets_count:";
    private static final String REPLIES_COUNT_KEY_PREFIX = "replies_count:";
    
    /**
     * Caches tweet likes count
     *
     * @param tweetId the tweet ID
     * @param count the likes count
     */
    public void cacheLikesCount(UUID tweetId, long count) {
        String key = LIKES_COUNT_KEY_PREFIX + tweetId;
        redisTemplate.opsForValue().set(key, count, Duration.ofSeconds(cacheProperties.getStatisticsCacheSeconds()));
        log.debug("Cached likes count for tweet {}: {}", tweetId, count);
    }
    
    /**
     * Gets cached likes count
     *
     * @param tweetId the tweet ID
     * @return cached count or null
     */
    public Long getCachedLikesCount(UUID tweetId) {
        String key = LIKES_COUNT_KEY_PREFIX + tweetId;
        Long count = (Long) redisTemplate.opsForValue().get(key);
        if (count != null) {
            log.debug("Cache hit for likes count tweet {}: {}", tweetId, count);
        } else {
            log.debug("Cache miss for likes count tweet: {}", tweetId);
        }
        return count;
    }
    
    /**
     * Increments cached likes count
     *
     * @param tweetId the tweet ID
     * @return the new count
     */
    public long incrementLikesCount(UUID tweetId) {
        String key = LIKES_COUNT_KEY_PREFIX + tweetId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null) {
            redisTemplate.expire(key, Duration.ofSeconds(cacheProperties.getStatisticsCacheSeconds()));
            log.debug("Incremented likes count for tweet {}: {}", tweetId, count);
        }
        return count != null ? count : 0;
    }
    
    /**
     * Decrements cached likes count
     *
     * @param tweetId the tweet ID
     * @return the new count
     */
    public long decrementLikesCount(UUID tweetId) {
        String key = LIKES_COUNT_KEY_PREFIX + tweetId;
        Long count = redisTemplate.opsForValue().decrement(key);
        if (count != null) {
            redisTemplate.expire(key, Duration.ofSeconds(cacheProperties.getStatisticsCacheSeconds()));
            log.debug("Decremented likes count for tweet {}: {}", tweetId, count);
        }
        return count != null ? count : 0;
    }
    
    /**
     * Caches tweet retweets count
     *
     * @param tweetId the tweet ID
     * @param count the retweets count
     */
    public void cacheRetweetsCount(UUID tweetId, long count) {
        String key = RETWEETS_COUNT_KEY_PREFIX + tweetId;
        redisTemplate.opsForValue().set(key, count, Duration.ofSeconds(cacheProperties.getStatisticsCacheSeconds()));
        log.debug("Cached retweets count for tweet {}: {}", tweetId, count);
    }
    
    /**
     * Gets cached retweets count
     *
     * @param tweetId the tweet ID
     * @return cached count or null
     */
    public Long getCachedRetweetsCount(UUID tweetId) {
        String key = RETWEETS_COUNT_KEY_PREFIX + tweetId;
        Long count = (Long) redisTemplate.opsForValue().get(key);
        if (count != null) {
            log.debug("Cache hit for retweets count tweet {}: {}", tweetId, count);
        } else {
            log.debug("Cache miss for retweets count tweet: {}", tweetId);
        }
        return count;
    }
    
    /**
     * Increments cached retweets count
     *
     * @param tweetId the tweet ID
     * @return the new count
     */
    public long incrementRetweetsCount(UUID tweetId) {
        String key = RETWEETS_COUNT_KEY_PREFIX + tweetId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null) {
            redisTemplate.expire(key, Duration.ofSeconds(cacheProperties.getStatisticsCacheSeconds()));
            log.debug("Incremented retweets count for tweet {}: {}", tweetId, count);
        }
        return count != null ? count : 0;
    }
    
    /**
     * Decrements cached retweets count
     *
     * @param tweetId the tweet ID
     * @return the new count
     */
    public long decrementRetweetsCount(UUID tweetId) {
        String key = RETWEETS_COUNT_KEY_PREFIX + tweetId;
        Long count = redisTemplate.opsForValue().decrement(key);
        if (count != null) {
            redisTemplate.expire(key, Duration.ofSeconds(cacheProperties.getStatisticsCacheSeconds()));
            log.debug("Decremented retweets count for tweet {}: {}", tweetId, count);
        }
        return count != null ? count : 0;
    }
    
    /**
     * Invalidates all statistics caches for a tweet
     *
     * @param tweetId the tweet ID
     */
    public void invalidateTweetStatistics(UUID tweetId) {
        List<String> keys = List.of(
            LIKES_COUNT_KEY_PREFIX + tweetId,
            RETWEETS_COUNT_KEY_PREFIX + tweetId,
            REPLIES_COUNT_KEY_PREFIX + tweetId
        );
        
        redisTemplate.delete(keys);
        log.debug("Invalidated statistics caches for tweet: {}", tweetId);
    }
}
```

## 6. Мониторинг и метрики кэширования

### 6.1 Cache Metrics Service

#### CacheMetricsService
```java
@Service
@RequiredArgsConstructor
public class CacheMetricsService {
    
    private final MeterRegistry meterRegistry;
    
    /**
     * Records cache hit
     *
     * @param cacheName the cache name
     * @param key the cache key
     */
    public void recordCacheHit(String cacheName, String key) {
        Counter.builder("cache.hits")
            .tag("cache", cacheName)
            .tag("key_type", getKeyType(key))
            .register(meterRegistry)
            .increment();
    }
    
    /**
     * Records cache miss
     *
     * @param cacheName the cache name
     * @param key the cache key
     */
    public void recordCacheMiss(String cacheName, String key) {
        Counter.builder("cache.misses")
            .tag("cache", cacheName)
            .tag("key_type", getKeyType(key))
            .register(meterRegistry)
            .increment();
    }
    
    /**
     * Records cache eviction
     *
     * @param cacheName the cache name
     * @param reason the eviction reason
     */
    public void recordCacheEviction(String cacheName, String reason) {
        Counter.builder("cache.evictions")
            .tag("cache", cacheName)
            .tag("reason", reason)
            .register(meterRegistry)
            .increment();
    }
    
    /**
     * Records cache operation duration
     *
     * @param cacheName the cache name
     * @param operation the operation type
     * @param duration the operation duration
     */
    public void recordCacheOperationDuration(String cacheName, String operation, Duration duration) {
        Timer.builder("cache.operation.duration")
            .tag("cache", cacheName)
            .tag("operation", operation)
            .register(meterRegistry)
            .record(duration);
    }
    
    /**
     * Records cache size
     *
     * @param cacheName the cache name
     * @param size the cache size
     */
    public void recordCacheSize(String cacheName, long size) {
        Gauge.builder("cache.size")
            .tag("cache", cacheName)
            .register(meterRegistry, () -> size);
    }
    
    private String getKeyType(String key) {
        if (key.startsWith("tweet:")) {
            return "tweet";
        } else if (key.startsWith("user_profile:")) {
            return "user_profile";
        } else if (key.startsWith("timeline:")) {
            return "timeline";
        } else if (key.startsWith("stats:")) {
            return "statistics";
        } else {
            return "other";
        }
    }
}
```

### 6.2 Cache Health Check

#### CacheHealthIndicator
```java
@Component
@RequiredArgsConstructor
public class CacheHealthIndicator implements HealthIndicator {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public Health health() {
        try {
            // Test Redis connection
            String testKey = "health_check:" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, "test", Duration.ofSeconds(10));
            String value = (String) redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);
            
            if ("test".equals(value)) {
                return Health.up()
                    .withDetail("redis", "Available")
                    .withDetail("connection", "OK")
                    .build();
            } else {
                return Health.down()
                    .withDetail("redis", "Unavailable")
                    .withDetail("connection", "Failed")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("redis", "Unavailable")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## 7. Конфигурация кэширования

### 7.1 Cache Properties

#### CacheProperties
```java
@ConfigurationProperties(prefix = "app.cache")
@Data
public class CacheProperties {
    
    /**
     * Enable caching
     */
    private boolean enabled = true;
    
    /**
     * Cache TTL settings
     */
    private TTL ttl = new TTL();
    
    /**
     * Redis settings
     */
    private Redis redis = new Redis();
    
    /**
     * HTTP cache settings
     */
    private Http http = new Http();
    
    @Data
    public static class TTL {
        private int tweetCacheSeconds = 300; // 5 minutes
        private int userProfileCacheSeconds = 600; // 10 minutes
        private int timelineCacheSeconds = 30; // 30 seconds
        private int statisticsCacheSeconds = 60; // 1 minute
        private int userTweetsCacheSeconds = 120; // 2 minutes
        private int userExistsCacheSeconds = 300; // 5 minutes
    }
    
    @Data
    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private String password;
        private int database = 0;
        private int timeout = 2000;
        private int maxConnections = 10;
        private int maxIdleConnections = 5;
        private int minIdleConnections = 1;
    }
    
    @Data
    public static class Http {
        private int tweetCacheSeconds = 300;
        private int userProfileCacheSeconds = 600;
        private int timelineCacheSeconds = 30;
        private int statisticsCacheSeconds = 60;
        private boolean enableETag = true;
        private boolean enableLastModified = true;
    }
}
```

### 7.2 Cache Configuration

#### CacheConfiguration
```java
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
@RequiredArgsConstructor
public class CacheConfiguration {
    
    private final CacheProperties cacheProperties;
    
    @Bean
    @ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true", matchIfMissing = true)
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(cacheProperties.getRedis().getHost());
        config.setPort(cacheProperties.getRedis().getPort());
        config.setDatabase(cacheProperties.getRedis().getDatabase());
        
        if (cacheProperties.getRedis().getPassword() != null) {
            config.setPassword(cacheProperties.getRedis().getPassword());
        }
        
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(cacheProperties.getRedis().getTimeout()))
            .poolConfig(createPoolConfig())
            .build();
        
        return new LettuceConnectionFactory(config, clientConfig);
    }
    
    private GenericObjectPoolConfig<?> createPoolConfig() {
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(cacheProperties.getRedis().getMaxConnections());
        poolConfig.setMaxIdle(cacheProperties.getRedis().getMaxIdleConnections());
        poolConfig.setMinIdle(cacheProperties.getRedis().getMinIdleConnections());
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        return poolConfig;
    }
}
```

## 8. Тестирование кэширования

### 8.1 Unit тесты кэширования

```java
@ExtendWith(MockitoExtension.class)
class TweetCacheServiceTest {
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private CacheProperties cacheProperties;
    
    @InjectMocks
    private TweetCacheService tweetCacheService;
    
    @Test
    void cacheTweet_ShouldStoreTweetInCache() {
        // Given
        Tweet tweet = createTestTweet();
        when(cacheProperties.getTweetCacheSeconds()).thenReturn(300);
        
        // When
        Tweet result = tweetCacheService.cacheTweet(tweet);
        
        // Then
        assertThat(result).isEqualTo(tweet);
        verify(redisTemplate).opsForValue();
    }
    
    @Test
    void getCachedTweet_CacheHit_ShouldReturnTweet() {
        // Given
        UUID tweetId = UUID.randomUUID();
        Tweet expectedTweet = createTestTweet();
        when(redisTemplate.opsForValue().get("tweet:" + tweetId)).thenReturn(expectedTweet);
        
        // When
        Tweet result = tweetCacheService.getCachedTweet(tweetId);
        
        // Then
        assertThat(result).isEqualTo(expectedTweet);
    }
    
    @Test
    void getCachedTweet_CacheMiss_ShouldReturnNull() {
        // Given
        UUID tweetId = UUID.randomUUID();
        when(redisTemplate.opsForValue().get("tweet:" + tweetId)).thenReturn(null);
        
        // When
        Tweet result = tweetCacheService.getCachedTweet(tweetId);
        
        // Then
        assertThat(result).isNull();
    }
    
    private Tweet createTestTweet() {
        return Tweet.builder()
            .id(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .content("Test tweet content")
            .createdAt(LocalDateTime.now())
            .build();
    }
}
```

### 8.2 Integration тесты

```java
@SpringBootTest
@Testcontainers
class CacheIntegrationTest {
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @Autowired
    private TweetCacheService tweetCacheService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Test
    void cacheTweet_WithRedis_ShouldWork() {
        // Given
        Tweet tweet = createTestTweet();
        
        // When
        tweetCacheService.cacheTweet(tweet);
        Tweet cachedTweet = tweetCacheService.getCachedTweet(tweet.getId());
        
        // Then
        assertThat(cachedTweet).isNotNull();
        assertThat(cachedTweet.getId()).isEqualTo(tweet.getId());
    }
    
    @Test
    void invalidateTweet_ShouldRemoveFromCache() {
        // Given
        Tweet tweet = createTestTweet();
        tweetCacheService.cacheTweet(tweet);
        
        // When
        tweetCacheService.invalidateTweet(tweet.getId());
        Tweet cachedTweet = tweetCacheService.getCachedTweet(tweet.getId());
        
        // Then
        assertThat(cachedTweet).isNull();
    }
    
    private Tweet createTestTweet() {
        return Tweet.builder()
            .id(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .content("Test tweet content")
            .createdAt(LocalDateTime.now())
            .build();
    }
}
```

## 9. Заключение

### 9.1 Ключевые архитектурные решения

1. **Многоуровневая архитектура кэширования** - HTTP, Application, Database уровни
2. **Стратегии кэширования по типам данных** - Static, Semi-Static, Dynamic, Real-time
3. **Event-driven инвалидация** - автоматическая инвалидация по событиям
4. **HTTP кэширование** - с ETag, Last-Modified и Cache-Control заголовками
5. **Redis кэширование** - с JSON сериализацией и TTL
6. **Мониторинг и метрики** - отслеживание hit/miss ratio и производительности

### 9.2 Готовность к реализации

- ✅ **HTTP кэширование** с Cache-Control заголовками и ETag
- ✅ **Application-level кэширование** с Redis и Spring Cache
- ✅ **Специализированные cache services** для твитов, пользователей и статистики
- ✅ **Event-driven инвалидация** с автоматической очисткой кэша
- ✅ **Мониторинг и метрики** с Prometheus и health checks
- ✅ **Конфигурация** через CacheProperties
- ✅ **Тестирование** unit и integration тестов

### 9.3 Критерии успешности

- ✅ **Полное покрытие кэширования** для всех операций чтения
- ✅ **Соответствие требованиям производительности** с TTL и стратегиями
- ✅ **Интеграция с существующими паттернами** Spring Cache и Redis
- ✅ **Производительность** с мониторингом hit/miss ratio
- ✅ **Тестируемость** с unit и integration тестами
- ✅ **Масштабируемость** с поддержкой кластеризации Redis

---

*Документ создан: 2025-01-27*  
*Версия: 1.0*  
*Статус: Completed*
