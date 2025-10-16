# Проектирование системы пагинации Tweet API

## Meta
- project: twitter-tweet-api
- design_date: 2025-01-27
- designer: AI Assistant
- version: 1.0
- status: completed
- step: 4.14

## Executive Summary

Данный документ содержит детальное проектирование системы пагинации для сервиса Tweet API. Система спроектирована с учетом высоких требований к производительности, интеграции с существующими паттернами users-api и обеспечения эффективной обработки больших объемов данных.

## 1. Архитектурные принципы пагинации

### 1.1 Стратегии пагинации

#### Offset-based Pagination (OFFSET):
- **Принцип**: Использование OFFSET и LIMIT в SQL запросах
- **Преимущества**: Простота реализации, поддержка произвольного доступа к страницам
- **Недостатки**: Производительность ухудшается на больших OFFSET
- **Применение**: Статические списки, небольшие объемы данных

#### Cursor-based Pagination (CURSOR):
- **Принцип**: Использование курсора (обычно ID или timestamp) для навигации
- **Преимущества**: Консистентность данных, высокая производительность
- **Недостатки**: Сложность реализации, невозможность произвольного доступа
- **Применение**: Динамические списки, большие объемы данных

#### Hybrid Pagination (HYBRID):
- **Принцип**: Комбинация offset-based и cursor-based подходов
- **Преимущества**: Гибкость выбора стратегии по типу данных
- **Недостатки**: Сложность реализации и поддержки
- **Применение**: Разные типы запросов в одном API

### 1.2 Классификация данных по стратегиям

#### Статические данные (OFFSET):
- Список пользователей
- Административные списки
- Справочные данные

#### Динамические данные (CURSOR):
- Лента новостей (timeline)
- Твиты пользователя
- Лайки и ретвиты

#### Смешанные данные (HYBRID):
- Поиск по твитам
- Фильтрованные списки

## 2. Offset-based пагинация

### 2.1 Offset Pagination Service

#### OffsetPaginationService
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class OffsetPaginationService {
    
    private final TweetRepository tweetRepository;
    private final PaginationProperties paginationProperties;
    
    /**
     * Gets paginated tweets using offset-based pagination
     *
     * @param page the page number (0-based)
     * @param size the page size
     * @param sort the sorting criteria
     * @return paginated tweets
     */
    public Page<Tweet> getTweets(int page, int size, Sort sort) {
        // Validate and adjust page size
        int adjustedSize = adjustPageSize(size);
        
        // Create pageable
        Pageable pageable = PageRequest.of(page, adjustedSize, sort);
        
        // Execute query
        Page<Tweet> result = tweetRepository.findAllByIsDeletedFalse(pageable);
        
        log.debug("Retrieved {} tweets for page {} with size {}", 
            result.getContent().size(), page, adjustedSize);
        
        return result;
    }
    
    /**
     * Gets paginated tweets by user using offset-based pagination
     *
     * @param userId the user ID
     * @param page the page number (0-based)
     * @param size the page size
     * @param sort the sorting criteria
     * @return paginated tweets
     */
    public Page<Tweet> getTweetsByUser(UUID userId, int page, int size, Sort sort) {
        int adjustedSize = adjustPageSize(size);
        Pageable pageable = PageRequest.of(page, adjustedSize, sort);
        
        Page<Tweet> result = tweetRepository.findByUserIdAndIsDeletedFalse(userId, pageable);
        
        log.debug("Retrieved {} tweets for user {} page {} with size {}", 
            result.getContent().size(), userId, page, adjustedSize);
        
        return result;
    }
    
    /**
     * Gets paginated tweets with filtering using offset-based pagination
     *
     * @param filter the filter criteria
     * @param page the page number (0-based)
     * @param size the page size
     * @param sort the sorting criteria
     * @return paginated tweets
     */
    public Page<Tweet> getTweetsWithFilter(TweetFilter filter, int page, int size, Sort sort) {
        int adjustedSize = adjustPageSize(size);
        Pageable pageable = PageRequest.of(page, adjustedSize, sort);
        
        Page<Tweet> result = tweetRepository.findAll(filter.toSpecification(), pageable);
        
        log.debug("Retrieved {} filtered tweets for page {} with size {}", 
            result.getContent().size(), page, adjustedSize);
        
        return result;
    }
    
    /**
     * Adjusts page size to be within allowed limits
     *
     * @param requestedSize the requested page size
     * @return adjusted page size
     */
    private int adjustPageSize(int requestedSize) {
        int maxSize = paginationProperties.getMaxPageSize();
        int defaultSize = paginationProperties.getDefaultPageSize();
        
        if (requestedSize <= 0) {
            return defaultSize;
        }
        
        return Math.min(requestedSize, maxSize);
    }
}
```

### 2.2 Offset Pagination Response

#### OffsetPaginationResponse
```java
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OffsetPaginationResponse<T> {
    
    /**
     * The content of the current page
     */
    private List<T> content;
    
    /**
     * Pagination metadata
     */
    private OffsetPaginationMetadata page;
    
    /**
     * Creates an offset pagination response from Spring Page
     *
     * @param page the Spring Page
     * @return OffsetPaginationResponse
     */
    public static <T> OffsetPaginationResponse<T> of(Page<T> page) {
        OffsetPaginationMetadata metadata = OffsetPaginationMetadata.builder()
            .size(page.getSize())
            .number(page.getNumber())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .numberOfElements(page.getNumberOfElements())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
        
        return OffsetPaginationResponse.<T>builder()
            .content(page.getContent())
            .page(metadata)
            .build();
    }
}
```

#### OffsetPaginationMetadata
```java
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OffsetPaginationMetadata {
    
    /**
     * Size of the page
     */
    private int size;
    
    /**
     * Number of the page (0-based)
     */
    private int number;
    
    /**
     * Total number of elements
     */
    private long totalElements;
    
    /**
     * Total number of pages
     */
    private int totalPages;
    
    /**
     * Whether this is the first page
     */
    private boolean first;
    
    /**
     * Whether this is the last page
     */
    private boolean last;
    
    /**
     * Number of elements in this page
     */
    private int numberOfElements;
    
    /**
     * Whether there is a next page
     */
    private boolean hasNext;
    
    /**
     * Whether there is a previous page
     */
    private boolean hasPrevious;
}
```

## 3. Cursor-based пагинация

### 3.1 Cursor Pagination Service

#### CursorPaginationService
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CursorPaginationService {
    
    private final TweetRepository tweetRepository;
    private final PaginationProperties paginationProperties;
    
    /**
     * Gets paginated tweets using cursor-based pagination
     *
     * @param cursor the cursor (tweet ID or timestamp)
     * @param size the page size
     * @param direction the pagination direction
     * @return cursor paginated tweets
     */
    public CursorPaginationResponse<Tweet> getTweets(String cursor, int size, PaginationDirection direction) {
        int adjustedSize = adjustPageSize(size);
        
        List<Tweet> tweets;
        String nextCursor = null;
        String previousCursor = null;
        
        if (direction == PaginationDirection.FORWARD) {
            tweets = getTweetsForward(cursor, adjustedSize);
            nextCursor = getNextCursor(tweets, adjustedSize);
        } else if (direction == PaginationDirection.BACKWARD) {
            tweets = getTweetsBackward(cursor, adjustedSize);
            previousCursor = getPreviousCursor(tweets, adjustedSize);
        } else {
            // Initial request
            tweets = getTweetsInitial(adjustedSize);
            nextCursor = getNextCursor(tweets, adjustedSize);
        }
        
        CursorPaginationMetadata metadata = CursorPaginationMetadata.builder()
            .size(adjustedSize)
            .hasNext(nextCursor != null)
            .hasPrevious(previousCursor != null)
            .nextCursor(nextCursor)
            .previousCursor(previousCursor)
            .build();
        
        return CursorPaginationResponse.<Tweet>builder()
            .content(tweets)
            .page(metadata)
            .build();
    }
    
    /**
     * Gets paginated tweets by user using cursor-based pagination
     *
     * @param userId the user ID
     * @param cursor the cursor
     * @param size the page size
     * @param direction the pagination direction
     * @return cursor paginated tweets
     */
    public CursorPaginationResponse<Tweet> getTweetsByUser(UUID userId, String cursor, int size, PaginationDirection direction) {
        int adjustedSize = adjustPageSize(size);
        
        List<Tweet> tweets;
        String nextCursor = null;
        String previousCursor = null;
        
        if (direction == PaginationDirection.FORWARD) {
            tweets = getTweetsByUserForward(userId, cursor, adjustedSize);
            nextCursor = getNextCursor(tweets, adjustedSize);
        } else if (direction == PaginationDirection.BACKWARD) {
            tweets = getTweetsByUserBackward(userId, cursor, adjustedSize);
            previousCursor = getPreviousCursor(tweets, adjustedSize);
        } else {
            tweets = getTweetsByUserInitial(userId, adjustedSize);
            nextCursor = getNextCursor(tweets, adjustedSize);
        }
        
        CursorPaginationMetadata metadata = CursorPaginationMetadata.builder()
            .size(adjustedSize)
            .hasNext(nextCursor != null)
            .hasPrevious(previousCursor != null)
            .nextCursor(nextCursor)
            .previousCursor(previousCursor)
            .build();
        
        return CursorPaginationResponse.<Tweet>builder()
            .content(tweets)
            .page(metadata)
            .build();
    }
    
    /**
     * Gets paginated timeline using cursor-based pagination
     *
     * @param userId the user ID
     * @param cursor the cursor
     * @param size the page size
     * @param direction the pagination direction
     * @return cursor paginated timeline
     */
    public CursorPaginationResponse<Tweet> getTimeline(UUID userId, String cursor, int size, PaginationDirection direction) {
        int adjustedSize = adjustPageSize(size);
        
        List<Tweet> tweets;
        String nextCursor = null;
        String previousCursor = null;
        
        if (direction == PaginationDirection.FORWARD) {
            tweets = getTimelineForward(userId, cursor, adjustedSize);
            nextCursor = getNextCursor(tweets, adjustedSize);
        } else if (direction == PaginationDirection.BACKWARD) {
            tweets = getTimelineBackward(userId, cursor, adjustedSize);
            previousCursor = getPreviousCursor(tweets, adjustedSize);
        } else {
            tweets = getTimelineInitial(userId, adjustedSize);
            nextCursor = getNextCursor(tweets, adjustedSize);
        }
        
        CursorPaginationMetadata metadata = CursorPaginationMetadata.builder()
            .size(adjustedSize)
            .hasNext(nextCursor != null)
            .hasPrevious(previousCursor != null)
            .nextCursor(nextCursor)
            .previousCursor(previousCursor)
            .build();
        
        return CursorPaginationResponse.<Tweet>builder()
            .content(tweets)
            .page(metadata)
            .build();
    }
    
    private List<Tweet> getTweetsForward(String cursor, int size) {
        if (cursor == null) {
            return getTweetsInitial(size);
        }
        
        UUID cursorId = parseCursor(cursor);
        return tweetRepository.findByIdLessThanAndIsDeletedFalseOrderByIdDesc(cursorId, PageRequest.of(0, size));
    }
    
    private List<Tweet> getTweetsBackward(String cursor, int size) {
        if (cursor == null) {
            return getTweetsInitial(size);
        }
        
        UUID cursorId = parseCursor(cursor);
        return tweetRepository.findByIdGreaterThanAndIsDeletedFalseOrderByIdAsc(cursorId, PageRequest.of(0, size));
    }
    
    private List<Tweet> getTweetsInitial(int size) {
        return tweetRepository.findByIsDeletedFalseOrderByIdDesc(PageRequest.of(0, size));
    }
    
    private List<Tweet> getTweetsByUserForward(UUID userId, String cursor, int size) {
        if (cursor == null) {
            return getTweetsByUserInitial(userId, size);
        }
        
        UUID cursorId = parseCursor(cursor);
        return tweetRepository.findByUserIdAndIdLessThanAndIsDeletedFalseOrderByIdDesc(userId, cursorId, PageRequest.of(0, size));
    }
    
    private List<Tweet> getTweetsByUserBackward(UUID userId, String cursor, int size) {
        if (cursor == null) {
            return getTweetsByUserInitial(userId, size);
        }
        
        UUID cursorId = parseCursor(cursor);
        return tweetRepository.findByUserIdAndIdGreaterThanAndIsDeletedFalseOrderByIdAsc(userId, cursorId, PageRequest.of(0, size));
    }
    
    private List<Tweet> getTweetsByUserInitial(UUID userId, int size) {
        return tweetRepository.findByUserIdAndIsDeletedFalseOrderByIdDesc(userId, PageRequest.of(0, size));
    }
    
    private List<Tweet> getTimelineForward(UUID userId, String cursor, int size) {
        if (cursor == null) {
            return getTimelineInitial(userId, size);
        }
        
        UUID cursorId = parseCursor(cursor);
        return tweetRepository.findTimelineForward(userId, cursorId, PageRequest.of(0, size));
    }
    
    private List<Tweet> getTimelineBackward(UUID userId, String cursor, int size) {
        if (cursor == null) {
            return getTimelineInitial(userId, size);
        }
        
        UUID cursorId = parseCursor(cursor);
        return tweetRepository.findTimelineBackward(userId, cursorId, PageRequest.of(0, size));
    }
    
    private List<Tweet> getTimelineInitial(UUID userId, int size) {
        return tweetRepository.findTimelineInitial(userId, PageRequest.of(0, size));
    }
    
    private String getNextCursor(List<Tweet> tweets, int requestedSize) {
        if (tweets.size() < requestedSize) {
            return null; // No more pages
        }
        
        Tweet lastTweet = tweets.get(tweets.size() - 1);
        return encodeCursor(lastTweet.getId());
    }
    
    private String getPreviousCursor(List<Tweet> tweets, int requestedSize) {
        if (tweets.size() < requestedSize) {
            return null; // No more pages
        }
        
        Tweet firstTweet = tweets.get(0);
        return encodeCursor(firstTweet.getId());
    }
    
    private String encodeCursor(UUID id) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(id.toString().getBytes());
    }
    
    private UUID parseCursor(String cursor) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor));
            return UUID.fromString(decoded);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor format: " + cursor);
        }
    }
    
    private int adjustPageSize(int requestedSize) {
        int maxSize = paginationProperties.getMaxPageSize();
        int defaultSize = paginationProperties.getDefaultPageSize();
        
        if (requestedSize <= 0) {
            return defaultSize;
        }
        
        return Math.min(requestedSize, maxSize);
    }
}
```

### 3.2 Cursor Pagination Response

#### CursorPaginationResponse
```java
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CursorPaginationResponse<T> {
    
    /**
     * The content of the current page
     */
    private List<T> content;
    
    /**
     * Cursor pagination metadata
     */
    private CursorPaginationMetadata page;
}
```

#### CursorPaginationMetadata
```java
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CursorPaginationMetadata {
    
    /**
     * Size of the page
     */
    private int size;
    
    /**
     * Whether there is a next page
     */
    private boolean hasNext;
    
    /**
     * Whether there is a previous page
     */
    private boolean hasPrevious;
    
    /**
     * Cursor for the next page
     */
    private String nextCursor;
    
    /**
     * Cursor for the previous page
     */
    private String previousCursor;
}
```

## 4. Hybrid пагинация

### 4.1 Hybrid Pagination Service

#### HybridPaginationService
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class HybridPaginationService {
    
    private final OffsetPaginationService offsetPaginationService;
    private final CursorPaginationService cursorPaginationService;
    private final PaginationProperties paginationProperties;
    
    /**
     * Gets paginated tweets using hybrid pagination strategy
     *
     * @param request the pagination request
     * @return paginated tweets
     */
    public PaginationResponse<Tweet> getTweets(PaginationRequest request) {
        PaginationStrategy strategy = determineStrategy(request);
        
        return switch (strategy) {
            case OFFSET -> convertToGenericResponse(
                offsetPaginationService.getTweets(request.getPage(), request.getSize(), request.getSort())
            );
            case CURSOR -> convertToGenericResponse(
                cursorPaginationService.getTweets(request.getCursor(), request.getSize(), request.getDirection())
            );
            case HYBRID -> handleHybridPagination(request);
        };
    }
    
    /**
     * Gets paginated tweets by user using hybrid pagination strategy
     *
     * @param userId the user ID
     * @param request the pagination request
     * @return paginated tweets
     */
    public PaginationResponse<Tweet> getTweetsByUser(UUID userId, PaginationRequest request) {
        PaginationStrategy strategy = determineStrategy(request);
        
        return switch (strategy) {
            case OFFSET -> convertToGenericResponse(
                offsetPaginationService.getTweetsByUser(userId, request.getPage(), request.getSize(), request.getSort())
            );
            case CURSOR -> convertToGenericResponse(
                cursorPaginationService.getTweetsByUser(userId, request.getCursor(), request.getSize(), request.getDirection())
            );
            case HYBRID -> handleHybridPaginationByUser(userId, request);
        };
    }
    
    /**
     * Gets paginated timeline using hybrid pagination strategy
     *
     * @param userId the user ID
     * @param request the pagination request
     * @return paginated timeline
     */
    public PaginationResponse<Tweet> getTimeline(UUID userId, PaginationRequest request) {
        // Timeline always uses cursor-based pagination for consistency
        CursorPaginationResponse<Tweet> cursorResponse = cursorPaginationService.getTimeline(
            userId, request.getCursor(), request.getSize(), request.getDirection()
        );
        
        return convertToGenericResponse(cursorResponse);
    }
    
    /**
     * Determines the best pagination strategy based on request parameters
     *
     * @param request the pagination request
     * @return the pagination strategy
     */
    private PaginationStrategy determineStrategy(PaginationRequest request) {
        // If cursor is provided, use cursor-based pagination
        if (request.getCursor() != null) {
            return PaginationStrategy.CURSOR;
        }
        
        // If page is provided and it's a small page number, use offset-based
        if (request.getPage() != null && request.getPage() < paginationProperties.getOffsetThreshold()) {
            return PaginationStrategy.OFFSET;
        }
        
        // For large page numbers, use cursor-based pagination
        if (request.getPage() != null && request.getPage() >= paginationProperties.getOffsetThreshold()) {
            return PaginationStrategy.CURSOR;
        }
        
        // Default to cursor-based for better performance
        return PaginationStrategy.CURSOR;
    }
    
    /**
     * Handles hybrid pagination by combining both strategies
     *
     * @param request the pagination request
     * @return paginated tweets
     */
    private PaginationResponse<Tweet> handleHybridPagination(PaginationRequest request) {
        // For hybrid pagination, we can use offset-based for small page numbers
        // and cursor-based for larger page numbers
        if (request.getPage() != null && request.getPage() < paginationProperties.getOffsetThreshold()) {
            Page<Tweet> offsetResult = offsetPaginationService.getTweets(
                request.getPage(), request.getSize(), request.getSort()
            );
            return convertToGenericResponse(offsetResult);
        } else {
            CursorPaginationResponse<Tweet> cursorResult = cursorPaginationService.getTweets(
                request.getCursor(), request.getSize(), request.getDirection()
            );
            return convertToGenericResponse(cursorResult);
        }
    }
    
    /**
     * Handles hybrid pagination by user
     *
     * @param userId the user ID
     * @param request the pagination request
     * @return paginated tweets
     */
    private PaginationResponse<Tweet> handleHybridPaginationByUser(UUID userId, PaginationRequest request) {
        if (request.getPage() != null && request.getPage() < paginationProperties.getOffsetThreshold()) {
            Page<Tweet> offsetResult = offsetPaginationService.getTweetsByUser(
                userId, request.getPage(), request.getSize(), request.getSort()
            );
            return convertToGenericResponse(offsetResult);
        } else {
            CursorPaginationResponse<Tweet> cursorResult = cursorPaginationService.getTweetsByUser(
                userId, request.getCursor(), request.getSize(), request.getDirection()
            );
            return convertToGenericResponse(cursorResult);
        }
    }
    
    /**
     * Converts offset pagination response to generic response
     *
     * @param page the Spring Page
     * @return generic pagination response
     */
    private PaginationResponse<Tweet> convertToGenericResponse(Page<Tweet> page) {
        PaginationMetadata metadata = PaginationMetadata.builder()
            .strategy(PaginationStrategy.OFFSET)
            .size(page.getSize())
            .number(page.getNumber())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
        
        return PaginationResponse.<Tweet>builder()
            .content(page.getContent())
            .page(metadata)
            .build();
    }
    
    /**
     * Converts cursor pagination response to generic response
     *
     * @param response the cursor pagination response
     * @return generic pagination response
     */
    private PaginationResponse<Tweet> convertToGenericResponse(CursorPaginationResponse<Tweet> response) {
        PaginationMetadata metadata = PaginationMetadata.builder()
            .strategy(PaginationStrategy.CURSOR)
            .size(response.getPage().getSize())
            .hasNext(response.getPage().isHasNext())
            .hasPrevious(response.getPage().isHasPrevious())
            .nextCursor(response.getPage().getNextCursor())
            .previousCursor(response.getPage().getPreviousCursor())
            .build();
        
        return PaginationResponse.<Tweet>builder()
            .content(response.getContent())
            .page(metadata)
            .build();
    }
}
```

### 4.2 Generic Pagination Response

#### PaginationResponse
```java
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginationResponse<T> {
    
    /**
     * The content of the current page
     */
    private List<T> content;
    
    /**
     * Generic pagination metadata
     */
    private PaginationMetadata page;
}
```

#### PaginationMetadata
```java
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginationMetadata {
    
    /**
     * The pagination strategy used
     */
    private PaginationStrategy strategy;
    
    /**
     * Size of the page
     */
    private int size;
    
    /**
     * Page number (for offset-based pagination)
     */
    private Integer number;
    
    /**
     * Total number of elements (for offset-based pagination)
     */
    private Long totalElements;
    
    /**
     * Total number of pages (for offset-based pagination)
     */
    private Integer totalPages;
    
    /**
     * Whether this is the first page (for offset-based pagination)
     */
    private Boolean first;
    
    /**
     * Whether this is the last page (for offset-based pagination)
     */
    private Boolean last;
    
    /**
     * Whether there is a next page
     */
    private boolean hasNext;
    
    /**
     * Whether there is a previous page
     */
    private boolean hasPrevious;
    
    /**
     * Cursor for the next page (for cursor-based pagination)
     */
    private String nextCursor;
    
    /**
     * Cursor for the previous page (for cursor-based pagination)
     */
    private String previousCursor;
}
```

## 5. Оптимизированные запросы

### 5.1 Custom Repository Queries

#### TweetRepository
```java
@Repository
public interface TweetRepository extends JpaRepository<Tweet, UUID>, JpaSpecificationExecutor<Tweet> {
    
    // Offset-based pagination queries
    Page<Tweet> findAllByIsDeletedFalse(Pageable pageable);
    
    Page<Tweet> findByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);
    
    // Cursor-based pagination queries
    List<Tweet> findByIdLessThanAndIsDeletedFalseOrderByIdDesc(UUID id, Pageable pageable);
    
    List<Tweet> findByIdGreaterThanAndIsDeletedFalseOrderByIdAsc(UUID id, Pageable pageable);
    
    List<Tweet> findByUserIdAndIdLessThanAndIsDeletedFalseOrderByIdDesc(UUID userId, UUID id, Pageable pageable);
    
    List<Tweet> findByUserIdAndIdGreaterThanAndIsDeletedFalseOrderByIdAsc(UUID userId, UUID id, Pageable pageable);
    
    // Timeline queries (simplified - would need follow-service integration)
    @Query("SELECT t FROM Tweet t WHERE t.userId IN " +
           "(SELECT f.followingId FROM Follow f WHERE f.followerId = :userId) " +
           "AND t.isDeleted = false ORDER BY t.createdAt DESC")
    List<Tweet> findTimelineInitial(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT t FROM Tweet t WHERE t.userId IN " +
           "(SELECT f.followingId FROM Follow f WHERE f.followerId = :userId) " +
           "AND t.id < :cursorId AND t.isDeleted = false ORDER BY t.createdAt DESC")
    List<Tweet> findTimelineForward(@Param("userId") UUID userId, @Param("cursorId") UUID cursorId, Pageable pageable);
    
    @Query("SELECT t FROM Tweet t WHERE t.userId IN " +
           "(SELECT f.followingId FROM Follow f WHERE f.followerId = :userId) " +
           "AND t.id > :cursorId AND t.isDeleted = false ORDER BY t.createdAt ASC")
    List<Tweet> findTimelineBackward(@Param("userId") UUID userId, @Param("cursorId") UUID cursorId, Pageable pageable);
    
    // Optimized queries with projections
    @Query("SELECT new com.twitter.dto.TweetSummaryDto(t.id, t.content, t.createdAt, t.userId) " +
           "FROM Tweet t WHERE t.isDeleted = false ORDER BY t.createdAt DESC")
    List<TweetSummaryDto> findTweetSummaries(Pageable pageable);
    
    @Query("SELECT new com.twitter.dto.TweetSummaryDto(t.id, t.content, t.createdAt, t.userId) " +
           "FROM Tweet t WHERE t.userId = :userId AND t.isDeleted = false ORDER BY t.createdAt DESC")
    List<TweetSummaryDto> findTweetSummariesByUser(@Param("userId") UUID userId, Pageable pageable);
    
    // Count queries for statistics
    long countByUserIdAndIsDeletedFalse(UUID userId);
    
    long countByIsDeletedFalse();
    
    // Exists queries for validation
    boolean existsByIdAndIsDeletedFalse(UUID id);
    
    boolean existsByUserIdAndIsDeletedFalse(UUID userId);
}
```

### 5.2 Query Optimization Service

#### QueryOptimizationService
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class QueryOptimizationService {
    
    private final TweetRepository tweetRepository;
    private final PaginationProperties paginationProperties;
    
    /**
     * Optimizes queries based on data size and access patterns
     *
     * @param queryType the type of query
     * @param parameters the query parameters
     * @return optimized query result
     */
    public <T> T executeOptimizedQuery(QueryType queryType, Map<String, Object> parameters) {
        QueryOptimizationStrategy strategy = determineOptimizationStrategy(queryType, parameters);
        
        return switch (strategy) {
            case USE_PROJECTION -> executeWithProjection(queryType, parameters);
            case USE_INDEX_HINT -> executeWithIndexHint(queryType, parameters);
            case USE_BATCH_LOADING -> executeWithBatchLoading(queryType, parameters);
            case USE_CACHE -> executeWithCache(queryType, parameters);
            case STANDARD -> executeStandard(queryType, parameters);
        };
    }
    
    /**
     * Determines the best optimization strategy for the query
     *
     * @param queryType the query type
     * @param parameters the query parameters
     * @return optimization strategy
     */
    private QueryOptimizationStrategy determineOptimizationStrategy(QueryType queryType, Map<String, Object> parameters) {
        // Analyze query parameters to determine optimization strategy
        int estimatedResultSize = estimateResultSize(queryType, parameters);
        
        if (estimatedResultSize > paginationProperties.getProjectionThreshold()) {
            return QueryOptimizationStrategy.USE_PROJECTION;
        }
        
        if (queryType == QueryType.TIMELINE) {
            return QueryOptimizationStrategy.USE_INDEX_HINT;
        }
        
        if (queryType == QueryType.USER_TWEETS && estimatedResultSize > paginationProperties.getBatchThreshold()) {
            return QueryOptimizationStrategy.USE_BATCH_LOADING;
        }
        
        if (queryType == QueryType.POPULAR_TWEETS) {
            return QueryOptimizationStrategy.USE_CACHE;
        }
        
        return QueryOptimizationStrategy.STANDARD;
    }
    
    /**
     * Executes query with projection to reduce data transfer
     *
     * @param queryType the query type
     * @param parameters the query parameters
     * @return query result
     */
    @SuppressWarnings("unchecked")
    private <T> T executeWithProjection(QueryType queryType, Map<String, Object> parameters) {
        Pageable pageable = (Pageable) parameters.get("pageable");
        
        return switch (queryType) {
            case TWEETS -> (T) tweetRepository.findTweetSummaries(pageable);
            case USER_TWEETS -> {
                UUID userId = (UUID) parameters.get("userId");
                yield (T) tweetRepository.findTweetSummariesByUser(userId, pageable);
            }
            default -> executeStandard(queryType, parameters);
        };
    }
    
    /**
     * Executes query with index hints for better performance
     *
     * @param queryType the query type
     * @param parameters the query parameters
     * @return query result
     */
    private <T> T executeWithIndexHint(QueryType queryType, Map<String, Object> parameters) {
        // Implementation would use @QueryHints for index hints
        return executeStandard(queryType, parameters);
    }
    
    /**
     * Executes query with batch loading for large datasets
     *
     * @param queryType the query type
     * @param parameters the query parameters
     * @return query result
     */
    private <T> T executeWithBatchLoading(QueryType queryType, Map<String, Object> parameters) {
        // Implementation would use batch loading strategies
        return executeStandard(queryType, parameters);
    }
    
    /**
     * Executes query with cache for frequently accessed data
     *
     * @param queryType the query type
     * @param parameters the query parameters
     * @return query result
     */
    private <T> T executeWithCache(QueryType queryType, Map<String, Object> parameters) {
        // Implementation would use cache for popular tweets
        return executeStandard(queryType, parameters);
    }
    
    /**
     * Executes standard query without optimization
     *
     * @param queryType the query type
     * @param parameters the query parameters
     * @return query result
     */
    private <T> T executeStandard(QueryType queryType, Map<String, Object> parameters) {
        // Standard query execution
        return null; // Placeholder
    }
    
    /**
     * Estimates the result size for optimization decisions
     *
     * @param queryType the query type
     * @param parameters the query parameters
     * @return estimated result size
     */
    private int estimateResultSize(QueryType queryType, Map<String, Object> parameters) {
        return switch (queryType) {
            case TWEETS -> 1000; // Estimated
            case USER_TWEETS -> 100; // Estimated
            case TIMELINE -> 500; // Estimated
            case POPULAR_TWEETS -> 50; // Estimated
        };
    }
}
```

## 6. Обработка больших объемов данных

### 6.1 Large Dataset Handler

#### LargeDatasetHandler
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class LargeDatasetHandler {
    
    private final TweetRepository tweetRepository;
    private final PaginationProperties paginationProperties;
    
    /**
     * Handles large dataset queries with streaming
     *
     * @param queryType the query type
     * @param parameters the query parameters
     * @return streaming result
     */
    public Stream<Tweet> streamLargeDataset(QueryType queryType, Map<String, Object> parameters) {
        int batchSize = paginationProperties.getStreamingBatchSize();
        int offset = 0;
        
        return Stream.iterate(
            tweetRepository.findBatch(queryType, parameters, offset, batchSize),
            tweets -> !tweets.isEmpty(),
            tweets -> {
                offset += batchSize;
                return tweetRepository.findBatch(queryType, parameters, offset, batchSize);
            }
        ).flatMap(List::stream);
    }
    
    /**
     * Handles large dataset queries with parallel processing
     *
     * @param queryType the query type
     * @param parameters the query parameters
     * @return parallel stream result
     */
    public Stream<Tweet> parallelLargeDataset(QueryType queryType, Map<String, Object> parameters) {
        int totalCount = getTotalCount(queryType, parameters);
        int batchSize = paginationProperties.getParallelBatchSize();
        int numberOfBatches = (totalCount + batchSize - 1) / batchSize;
        
        return IntStream.range(0, numberOfBatches)
            .parallel()
            .mapToObj(batchNumber -> {
                int offset = batchNumber * batchSize;
                return tweetRepository.findBatch(queryType, parameters, offset, batchSize);
            })
            .flatMap(List::stream);
    }
    
    /**
     * Handles large dataset queries with pagination chunks
     *
     * @param queryType the query type
     * @param parameters the query parameters
     * @param chunkSize the chunk size
     * @return chunked results
     */
    public List<List<Tweet>> chunkLargeDataset(QueryType queryType, Map<String, Object> parameters, int chunkSize) {
        int totalCount = getTotalCount(queryType, parameters);
        List<List<Tweet>> chunks = new ArrayList<>();
        
        for (int offset = 0; offset < totalCount; offset += chunkSize) {
            List<Tweet> chunk = tweetRepository.findBatch(queryType, parameters, offset, chunkSize);
            chunks.add(chunk);
            
            if (chunk.size() < chunkSize) {
                break; // Last chunk
            }
        }
        
        return chunks;
    }
    
    /**
     * Gets total count for large dataset queries
     *
     * @param queryType the query type
     * @param parameters the query parameters
     * @return total count
     */
    private int getTotalCount(QueryType queryType, Map<String, Object> parameters) {
        return switch (queryType) {
            case TWEETS -> (int) tweetRepository.countByIsDeletedFalse();
            case USER_TWEETS -> {
                UUID userId = (UUID) parameters.get("userId");
                yield (int) tweetRepository.countByUserIdAndIsDeletedFalse(userId);
            }
            default -> 0;
        };
    }
}
```

### 6.2 Database Indexing Strategy

#### DatabaseIndexingStrategy
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseIndexingStrategy {
    
    /**
     * Creates optimized indexes for pagination queries
     */
    @PostConstruct
    public void createOptimizedIndexes() {
        // These would be created via migration scripts
        log.info("Creating optimized indexes for pagination queries");
        
        // Composite index for user tweets with pagination
        // CREATE INDEX idx_tweets_user_created_id ON tweets(user_id, created_at DESC, id DESC) WHERE is_deleted = false;
        
        // Composite index for timeline queries
        // CREATE INDEX idx_tweets_timeline ON tweets(created_at DESC, id DESC) WHERE is_deleted = false;
        
        // Index for cursor-based pagination
        // CREATE INDEX idx_tweets_id_desc ON tweets(id DESC) WHERE is_deleted = false;
        
        // Partial index for active tweets only
        // CREATE INDEX idx_tweets_active ON tweets(created_at DESC) WHERE is_deleted = false;
    }
    
    /**
     * Analyzes query performance and suggests index optimizations
     *
     * @param queryType the query type
     * @param executionTime the query execution time
     */
    public void analyzeQueryPerformance(QueryType queryType, Duration executionTime) {
        if (executionTime.toMillis() > 1000) { // More than 1 second
            log.warn("Slow query detected for type {}: {}ms", queryType, executionTime.toMillis());
            
            // Suggest index optimizations
            suggestIndexOptimizations(queryType);
        }
    }
    
    /**
     * Suggests index optimizations for slow queries
     *
     * @param queryType the query type
     */
    private void suggestIndexOptimizations(QueryType queryType) {
        switch (queryType) {
            case USER_TWEETS -> log.info("Consider creating composite index on (user_id, created_at DESC, id DESC)");
            case TIMELINE -> log.info("Consider creating composite index on (created_at DESC, id DESC)");
            case TWEETS -> log.info("Consider creating index on (created_at DESC)");
        }
    }
}
```

## 7. Мониторинг и метрики пагинации

### 7.1 Pagination Metrics Service

#### PaginationMetricsService
```java
@Service
@RequiredArgsConstructor
public class PaginationMetricsService {
    
    private final MeterRegistry meterRegistry;
    
    /**
     * Records pagination request metrics
     *
     * @param strategy the pagination strategy
     * @param queryType the query type
     * @param pageSize the page size
     * @param executionTime the execution time
     */
    public void recordPaginationRequest(PaginationStrategy strategy, QueryType queryType, int pageSize, Duration executionTime) {
        Timer.builder("pagination.request.duration")
            .tag("strategy", strategy.name())
            .tag("query_type", queryType.name())
            .tag("page_size", String.valueOf(pageSize))
            .register(meterRegistry)
            .record(executionTime);
        
        Counter.builder("pagination.request.count")
            .tag("strategy", strategy.name())
            .tag("query_type", queryType.name())
            .register(meterRegistry)
            .increment();
    }
    
    /**
     * Records pagination performance metrics
     *
     * @param strategy the pagination strategy
     * @param queryType the query type
     * @param resultSize the result size
     * @param totalElements the total elements
     */
    public void recordPaginationPerformance(PaginationStrategy strategy, QueryType queryType, int resultSize, long totalElements) {
        Gauge.builder("pagination.result.size")
            .tag("strategy", strategy.name())
            .tag("query_type", queryType.name())
            .register(meterRegistry, () -> resultSize);
        
        Gauge.builder("pagination.total.elements")
            .tag("strategy", strategy.name())
            .tag("query_type", queryType.name())
            .register(meterRegistry, () -> totalElements);
    }
    
    /**
     * Records pagination error metrics
     *
     * @param strategy the pagination strategy
     * @param queryType the query type
     * @param errorType the error type
     */
    public void recordPaginationError(PaginationStrategy strategy, QueryType queryType, String errorType) {
        Counter.builder("pagination.error.count")
            .tag("strategy", strategy.name())
            .tag("query_type", queryType.name())
            .tag("error_type", errorType)
            .register(meterRegistry)
            .increment();
    }
}
```

## 8. Конфигурация пагинации

### 8.1 Pagination Properties

#### PaginationProperties
```java
@ConfigurationProperties(prefix = "app.pagination")
@Data
public class PaginationProperties {
    
    /**
     * Enable pagination features
     */
    private boolean enabled = true;
    
    /**
     * Default pagination settings
     */
    private Defaults defaults = new Defaults();
    
    /**
     * Pagination limits
     */
    private Limits limits = new Limits();
    
    /**
     * Optimization settings
     */
    private Optimization optimization = new Optimization();
    
    /**
     * Performance settings
     */
    private Performance performance = new Performance();
    
    @Data
    public static class Defaults {
        private int defaultPageSize = 20;
        private PaginationStrategy defaultStrategy = PaginationStrategy.CURSOR;
        private Sort defaultSort = Sort.by(Sort.Direction.DESC, "createdAt");
    }
    
    @Data
    public static class Limits {
        private int maxPageSize = 100;
        private int minPageSize = 1;
        private int offsetThreshold = 1000; // Switch to cursor-based after this page
    }
    
    @Data
    public static class Optimization {
        private int projectionThreshold = 1000; // Use projection for results larger than this
        private int batchThreshold = 500; // Use batch loading for results larger than this
        private boolean enableQueryOptimization = true;
        private boolean enableIndexHints = true;
    }
    
    @Data
    public static class Performance {
        private int streamingBatchSize = 1000;
        private int parallelBatchSize = 500;
        private int maxConcurrentQueries = 10;
        private Duration queryTimeout = Duration.ofSeconds(30);
    }
}
```

## 9. Тестирование пагинации

### 9.1 Unit тесты пагинации

```java
@ExtendWith(MockitoExtension.class)
class OffsetPaginationServiceTest {
    
    @Mock
    private TweetRepository tweetRepository;
    
    @Mock
    private PaginationProperties paginationProperties;
    
    @InjectMocks
    private OffsetPaginationService offsetPaginationService;
    
    @Test
    void getTweets_ValidRequest_ShouldReturnPaginatedTweets() {
        // Given
        int page = 0;
        int size = 20;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        
        Page<Tweet> mockPage = createMockPage();
        when(tweetRepository.findAllByIsDeletedFalse(any(Pageable.class))).thenReturn(mockPage);
        when(paginationProperties.getMaxPageSize()).thenReturn(100);
        
        // When
        Page<Tweet> result = offsetPaginationService.getTweets(page, size, sort);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(20);
    }
    
    @Test
    void getTweets_ExceedsMaxSize_ShouldAdjustPageSize() {
        // Given
        int page = 0;
        int size = 150; // Exceeds max size
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        
        Page<Tweet> mockPage = createMockPage();
        when(tweetRepository.findAllByIsDeletedFalse(any(Pageable.class))).thenReturn(mockPage);
        when(paginationProperties.getMaxPageSize()).thenReturn(100);
        
        // When
        Page<Tweet> result = offsetPaginationService.getTweets(page, size, sort);
        
        // Then
        assertThat(result.getSize()).isEqualTo(100); // Should be adjusted to max size
    }
    
    private Page<Tweet> createMockPage() {
        List<Tweet> tweets = List.of(
            createTestTweet(),
            createTestTweet()
        );
        
        return new PageImpl<>(tweets, PageRequest.of(0, 20), 2);
    }
    
    private Tweet createTestTweet() {
        return Tweet.builder()
            .id(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .content("Test tweet")
            .createdAt(LocalDateTime.now())
            .build();
    }
}
```

### 9.2 Integration тесты

```java
@SpringBootTest
@Testcontainers
class PaginationIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("tweet_test")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private OffsetPaginationService offsetPaginationService;
    
    @Autowired
    private CursorPaginationService cursorPaginationService;
    
    @Test
    void offsetPagination_WithDatabase_ShouldWork() {
        // Given
        int page = 0;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        
        // When
        Page<Tweet> result = offsetPaginationService.getTweets(page, size, sort);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotNull();
    }
    
    @Test
    void cursorPagination_WithDatabase_ShouldWork() {
        // Given
        String cursor = null;
        int size = 10;
        PaginationDirection direction = PaginationDirection.INITIAL;
        
        // When
        CursorPaginationResponse<Tweet> result = cursorPaginationService.getTweets(cursor, size, direction);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotNull();
        assertThat(result.getPage()).isNotNull();
    }
}
```

## 10. Заключение

### 10.1 Ключевые архитектурные решения

1. **Многостратегическая пагинация** - Offset-based, Cursor-based и Hybrid подходы
2. **Оптимизированные запросы** - с проекциями, индексами и batch loading
3. **Обработка больших объемов данных** - с streaming и parallel processing
4. **Производительность** - с мониторингом и метриками
5. **Гибкость** - с автоматическим выбором стратегии
6. **Масштабируемость** - с поддержкой больших объемов данных

### 10.2 Готовность к реализации

- ✅ **Offset-based пагинация** для статических списков
- ✅ **Cursor-based пагинация** для динамических данных
- ✅ **Hybrid пагинация** с автоматическим выбором стратегии
- ✅ **Оптимизированные запросы** с проекциями и индексами
- ✅ **Обработка больших объемов данных** с streaming
- ✅ **Мониторинг и метрики** с Prometheus
- ✅ **Конфигурация** через PaginationProperties
- ✅ **Тестирование** unit и integration тестов

### 10.3 Критерии успешности

- ✅ **Полное покрытие пагинации** для всех операций чтения
- ✅ **Соответствие требованиям производительности** с оптимизированными запросами
- ✅ **Интеграция с существующими паттернами** users-api
- ✅ **Производительность** с мониторингом и метриками
- ✅ **Тестируемость** с unit и integration тестами
- ✅ **Масштабируемость** с поддержкой больших объемов данных

---

*Документ создан: 2025-01-27*  
*Версия: 1.0*  
*Статус: Completed*
