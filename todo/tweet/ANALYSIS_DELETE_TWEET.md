# Анализ требований: Реализация мягкого удаления твита (Soft Delete)

## Meta
- **project**: twitter-tweet-api
- **document_type**: Requirements Analysis
- **version**: 1.0
- **created_date**: 2025-01-27
- **status**: Completed
- **step**: #1 from TODO_DELETE_TWEET.md

## 1. Входные и выходные данные

### 1.1 Входные данные

**Эндпоинт:** `DELETE /api/v1/tweets/{tweetId}`

**Параметры:**
- `tweetId` (UUID, path parameter) - уникальный идентификатор твита для удаления
- `userId` (UUID, request body или header) - идентификатор пользователя, выполняющего удаление

**Примечание:** Анализ существующего кода показывает, что для операций обновления используется `userId` в теле запроса (`UpdateTweetRequestDto`). Для удаления может использоваться аналогичный подход или передача через заголовки/контекст безопасности.

### 1.2 Выходные данные

**Успешный ответ:**
- HTTP статус: `204 No Content` (без тела ответа)
- Согласно стандартам REST API и архитектурному документу

**Ошибочные ответы:**
- `404 Not Found` - твит не найден или уже удален
- `409 Conflict` - доступ запрещен (пользователь не является автором твита)
- `400 Bad Request` - ошибки валидации (некорректный tweetId)

## 2. Структура полей Soft Delete

### 2.1 Поля Entity

**Требуется добавить в `Tweet` entity:**

1. **`isDeleted`** (Boolean)
   - Тип: `Boolean` (не примитивный `boolean` для поддержки nullable)
   - Значение по умолчанию: `false`
   - Nullable: `false`
   - Назначение: флаг, указывающий, удален ли твит
   - JPA аннотация: `@Column(name = "is_deleted", nullable = false)`
   - Значение по умолчанию в БД: `DEFAULT false`

2. **`deletedAt`** (LocalDateTime)
   - Тип: `LocalDateTime`
   - Nullable: `true`
   - Назначение: временная метка удаления твита
   - JPA аннотация: `@Column(name = "deleted_at", nullable = true)`
   - Значение: устанавливается при soft delete, `null` для активных твитов

### 2.2 Бизнес-метод Entity

**Метод `softDelete()`:**
```java
public void softDelete() {
    this.isDeleted = true;
    this.deletedAt = LocalDateTime.now();
}
```

**Метод `isActive()` (опционально, для удобства):**
```java
public boolean isActive() {
    return !Boolean.TRUE.equals(isDeleted);
}
```

### 2.3 Структура базы данных

**Миграция SQL (будет выполнена отдельно):**
```sql
ALTER TABLE tweets 
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN deleted_at TIMESTAMP NULL;

CREATE INDEX IF NOT EXISTS idx_tweets_is_deleted 
ON tweets(is_deleted) WHERE is_deleted = false;
```

**Примечание:** Индекс с условием `WHERE is_deleted = false` оптимизирует запросы для активных твитов.

## 3. Бизнес-правила удаления

### 3.1 Основные правила

1. **Только автор может удалить твит**
   - Проверка: `tweet.getUserId().equals(requestUserId)`
   - Исключение: `BusinessRuleValidationException` с кодом `TWEET_ACCESS_DENIED`
   - HTTP статус: `409 Conflict`

2. **Твит должен существовать**
   - Проверка: твит найден в БД по `tweetId`
   - Исключение: `BusinessRuleValidationException` с кодом `TWEET_NOT_FOUND`
   - HTTP статус: `404 Not Found`

3. **Твит не должен быть уже удален**
   - Проверка: `!Boolean.TRUE.equals(tweet.getIsDeleted())`
   - Исключение: `BusinessRuleValidationException` с кодом `TWEET_ALREADY_DELETED`
   - HTTP статус: `404 Not Found` (или `409 Conflict`)

4. **Soft delete сохраняет данные**
   - Статистика (лайки, ретвиты) сохраняется
   - Связанные записи (likes, retweets) не удаляются
   - Данные доступны для аналитики и восстановления

5. **Временная метка удаления**
   - `deletedAt` устанавливается в момент удаления
   - Используется `LocalDateTime.now()` для установки текущего времени

### 3.2 Правила валидации

**Валидация входных данных:**
- `tweetId` не должен быть `null`
- `tweetId` должен быть валидным UUID
- `userId` не должен быть `null` (если передается в запросе)

**Порядок валидации:**
1. Проверка `tweetId` на null
2. Поиск твита в БД
3. Проверка существования твита
4. Проверка, что твит не удален
5. Проверка прав доступа (авторство)

## 4. Валидация прав доступа

### 4.1 Механизм валидации

**Аналогично `validateForUpdate`:**
- Использование метода `validateTweetOwnership()` из `TweetValidatorImpl`
- Сравнение `tweet.getUserId()` с `requestUserId`
- Выброс `BusinessRuleValidationException` при несовпадении

**Метод валидации:**
```java
void validateForDelete(UUID tweetId, UUID userId) {
    // 1. Проверка tweetId на null
    // 2. Поиск твита в БД
    // 3. Проверка существования твита
    // 4. Проверка, что твит не удален
    // 5. Проверка прав доступа через validateTweetOwnership()
}
```

### 4.2 Исключения

**Типы исключений:**
- `BusinessRuleValidationException` - для бизнес-правил (не найден, уже удален, доступ запрещен)
- `FormatValidationException` - для ошибок формата (некорректный UUID)

**Коды ошибок:**
- `TWEET_NOT_FOUND` - твит не найден
- `TWEET_ALREADY_DELETED` - твит уже удален
- `TWEET_ACCESS_DENIED` - доступ запрещен (не автор)
- `TWEET_ID_NULL` - tweetId равен null

## 5. Затронутые стандарты

### 5.1 Стандарты кода (STANDART_CODE.md)

- **Java 24 Features**: использование Records для DTO (если потребуется)
- **Spring Boot 3.5.5 Practices**: @Transactional для service методов
- **Architectural Patterns**: Service Layer Pattern, Repository Pattern
- **Exception Handling**: использование ProblemDetail (RFC 7807)
- **Logging**: @LoggableRequest на контроллере, SLF4J с Lombok

### 5.2 Стандарты проекта (STANDART_PROJECT.md)

- **@LoggableRequest**: использование на методе контроллера
- **GlobalExceptionHandler**: централизованная обработка ошибок
- **Validation Exception Hierarchy**: использование BusinessRuleValidationException

### 5.3 Стандарты тестирования (STANDART_TEST.md)

- **Unit тесты**: для TweetService, TweetValidator
- **Integration тесты**: для TweetController
- **Паттерн именования**: `methodName_WhenCondition_ShouldExpectedResult`
- **Покрытие**: успешные и ошибочные сценарии

### 5.4 Стандарты JavaDoc (STANDART_JAVADOC.md)

- **@author geron**: для всех public классов и методов
- **@version 1.0**: для всех public классов и методов
- **@param**: для всех параметров
- **@return**: для возвращаемых значений
- **@throws**: для исключений

### 5.5 Стандарты Swagger (STANDART_SWAGGER.md)

- **@Operation**: для описания операции
- **@ApiResponses**: для описания возможных ответов (204, 404, 409, 400)
- **@Parameter**: для описания параметров

### 5.6 Стандарты README (STANDART_README.md)

- Обновление раздела REST API с описанием DELETE эндпоинта
- Добавление в таблицу эндпоинтов
- Детальное описание с примерами

### 5.7 Стандарты Postman (STANDART_POSTMAN.md)

- Добавление запроса "delete tweet"
- Примеры ответов: 204, 404, 409
- Обновление переменных окружения

## 6. Спроектированная структура данных

### 6.1 Обновленная Entity Tweet

```java
@Entity
@Table(name = "tweets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tweet {
    // ... существующие поля ...
    
    /**
     * Flag indicating whether the tweet has been soft deleted.
     * Default value is false (tweet is active).
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
    
    /**
     * Timestamp when the tweet was soft deleted.
     * Null for active tweets, set to current time when deleted.
     */
    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;
    
    /**
     * Performs soft delete by setting isDeleted flag and deletedAt timestamp.
     */
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
    
    /**
     * Checks if the tweet is active (not deleted).
     *
     * @return true if tweet is active, false if deleted
     */
    public boolean isActive() {
        return !Boolean.TRUE.equals(isDeleted);
    }
}
```

### 6.2 Обновленный Repository

**Методы для работы с soft delete:**
```java
@Repository
public interface TweetRepository extends JpaRepository<Tweet, UUID> {
    /**
     * Finds tweet by ID excluding deleted tweets.
     */
    Optional<Tweet> findByIdAndIsDeletedFalse(UUID id);
    
    /**
     * Performs soft delete by ID.
     */
    @Modifying
    @Query("UPDATE Tweet t SET t.isDeleted = true, t.deletedAt = :deletedAt WHERE t.id = :id")
    void softDeleteById(@Param("id") UUID id, @Param("deletedAt") LocalDateTime deletedAt);
}
```

### 6.3 Обновленный DTO (опционально)

**TweetResponseDto может включать поля soft delete (если требуется в ответах):**
```java
@Builder
public record TweetResponseDto(
    // ... существующие поля ...
    
    @Schema(description = "Flag indicating if tweet is deleted", example = "false")
    Boolean isDeleted,
    
    @Schema(description = "Timestamp when tweet was deleted", example = "2025-01-27T15:30:00Z")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    LocalDateTime deletedAt
) {
}
```

**Примечание:** Согласно архитектурному документу, удаленные твиты не должны возвращаться в обычных запросах, поэтому эти поля могут быть опциональными.

## 7. Затронутые компоненты

### 7.1 Компоненты, требующие изменений

1. **Tweet Entity** - добавление полей isDeleted и deletedAt, метод softDelete()
2. **TweetRepository** - методы findByIdAndIsDeletedFalse(), softDeleteById()
3. **TweetValidator** - метод validateForDelete()
4. **TweetValidatorImpl** - реализация validateForDelete()
5. **TweetService** - метод deleteTweet()
6. **TweetServiceImpl** - реализация deleteTweet()
7. **TweetController** - эндпоинт DELETE /api/v1/tweets/{tweetId}
8. **TweetApi** - OpenAPI интерфейс с методом deleteTweet()
9. **TweetMapper** - обновление маппинга (если добавляются поля в DTO)
10. **TweetResponseDto** - опционально добавление полей isDeleted и deletedAt
11. **getTweetById** - обновление для использования findByIdAndIsDeletedFalse()

### 7.2 Компоненты, не требующие изменений

- **CreateTweetRequestDto** - не затронут
- **UpdateTweetRequestDto** - не затронут
- **Like/Retweet entities** - не затронуты (статистика сохраняется)

## 8. Предположения (Assumptions)

1. **Soft delete означает установку флага isDeleted = true и установку deletedAt = текущее время** - подтверждено
2. **Удаленные твиты не должны возвращаться в обычных запросах (getTweetById)** - подтверждено
3. **Статистика (лайки, ретвиты) сохраняется при soft delete** - подтверждено
4. **Только автор твита может удалить свой твит (аналогично updateTweet)** - подтверждено
5. **HTTP статус 204 No Content для успешного удаления (без тела ответа)** - подтверждено
6. **Валидация прав доступа аналогична updateTweet (сравнение userId)** - подтверждено
7. **Миграция БД будет выполнена отдельно для добавления полей isDeleted и deletedAt** - подтверждено

## 9. Риски и митигация

### 9.1 Технические риски

1. **Необходимость миграции БД**
   - Риск: изменение схемы БД требует миграции
   - Митигация: миграция будет выполнена отдельно, добавление полей с DEFAULT значениями безопасно

2. **Обновление существующих запросов**
   - Риск: необходимо обновить все методы Repository для учета soft delete
   - Митигация: создание метода findByIdAndIsDeletedFalse() и обновление getTweetById()

3. **Производительность при фильтрации**
   - Риск: фильтрация удаленных твитов может замедлить запросы
   - Митигация: индекс на is_deleted с условием WHERE is_deleted = false

### 9.2 Организационные риски

1. **Изменения в других частях системы**
   - Риск: другие сервисы могут использовать TweetRepository
   - Митигация: обратная совместимость через сохранение существующих методов

## 10. Критерии успеха

- ✅ Структура полей soft delete определена (isDeleted, deletedAt)
- ✅ Бизнес-правила удаления определены
- ✅ Валидация прав доступа спроектирована
- ✅ Входные и выходные данные определены
- ✅ Затронутые стандарты идентифицированы
- ✅ Структура данных спроектирована
- ✅ Затронутые компоненты идентифицированы

## 11. Следующие шаги

Следующий шаг: **#2: Проектирование API и контрактов** - Определить структуру эндпоинта DELETE, HTTP статусы, валидацию

---

*Документ создан: 2025-01-27*  
*Версия: 1.0*  
*Статус: Completed*


