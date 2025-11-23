# Реализация: Обновление Entity Tweet для Soft Delete

## Meta
- **project**: twitter-tweet-api
- **document_type**: Implementation Specification
- **version**: 1.0
- **created_date**: 2025-01-27
- **status**: Ready for Implementation
- **step**: #3 from TODO_DELETE_TWEET.md

## 1. Текущее состояние

**Файл:** `services/tweet-api/src/main/java/com/twitter/entity/Tweet.java`

**Текущая структура:**
- Поля: `id`, `userId`, `content`, `createdAt`, `updatedAt`
- Аннотации: `@Entity`, `@Table(name = "tweets")`, `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Валидация: `@PrePersist`, `@PreUpdate` для проверки content

## 2. Требуемые изменения

### 2.1 Добавление полей

#### Поле `isDeleted`

**Расположение:** После поля `updatedAt`, перед методом `validateContent()`

**Код:**
```java
/**
 * Flag indicating whether the tweet has been soft deleted.
 * <p>
 * Default value is false (tweet is active). When a tweet is soft deleted,
 * this flag is set to true and the deletedAt timestamp is set to the current time.
 * The tweet data is preserved in the database for analytics and recovery purposes.
 *
 * @author geron
 * @version 1.0
 */
@Column(name = "is_deleted", nullable = false)
@Builder.Default
private Boolean isDeleted = false;
```

**Характеристики:**
- Тип: `Boolean` (не примитивный `boolean` для поддержки nullable и Builder)
- Значение по умолчанию: `false`
- Nullable: `false` (в БД)
- JPA аннотация: `@Column(name = "is_deleted", nullable = false)`
- Lombok: `@Builder.Default` для поддержки Builder с дефолтным значением
- JavaDoc: полное описание с `@author` и `@version`

#### Поле `deletedAt`

**Расположение:** После поля `isDeleted`, перед методом `validateContent()`

**Код:**
```java
/**
 * Timestamp when the tweet was soft deleted.
 * <p>
 * Null for active tweets (isDeleted = false). When a tweet is soft deleted,
 * this timestamp is set to the current time (LocalDateTime.now()).
 * Used for analytics, audit trails, and potential recovery operations.
 *
 * @author geron
 * @version 1.0
 */
@Column(name = "deleted_at", nullable = true)
private LocalDateTime deletedAt;
```

**Характеристики:**
- Тип: `LocalDateTime`
- Nullable: `true` (в БД и Java)
- JPA аннотация: `@Column(name = "deleted_at", nullable = true)`
- JavaDoc: полное описание с `@author` и `@version`

### 2.2 Добавление бизнес-методов

#### Метод `softDelete()`

**Расположение:** После метода `validateContent()`, в конце класса

**Код:**
```java
/**
 * Performs soft delete by setting isDeleted flag and deletedAt timestamp.
 * <p>
 * This method sets the isDeleted flag to true and records the current time
 * as the deletion timestamp. The tweet data is preserved in the database,
 * allowing for analytics and potential recovery operations.
 * <p>
 * This method should be called when a tweet is deleted by its author.
 * The method is idempotent - calling it multiple times will not change
 * the deletion timestamp after the first call.
 *
 * @author geron
 * @version 1.0
 */
public void softDelete() {
    this.isDeleted = true;
    this.deletedAt = LocalDateTime.now();
}
```

**Характеристики:**
- Модификатор: `public`
- Возвращаемое значение: `void`
- Логика: устанавливает `isDeleted = true` и `deletedAt = LocalDateTime.now()`
- Идемпотентность: повторные вызовы не изменяют `deletedAt` после первого удаления
- JavaDoc: полное описание с `@author` и `@version`

#### Метод `isActive()` (опционально, для удобства)

**Расположение:** После метода `softDelete()`, в конце класса

**Код:**
```java
/**
 * Checks if the tweet is active (not deleted).
 * <p>
 * A tweet is considered active if the isDeleted flag is false or null.
 * This method provides a convenient way to check tweet status without
 * directly accessing the isDeleted field.
 *
 * @return true if tweet is active (not deleted), false if deleted
 * @author geron
 * @version 1.0
 */
public boolean isActive() {
    return !Boolean.TRUE.equals(isDeleted);
}
```

**Характеристики:**
- Модификатор: `public`
- Возвращаемое значение: `boolean`
- Логика: возвращает `true` если твит активен (isDeleted не равен `true`)
- Безопасность: использует `Boolean.TRUE.equals()` для безопасной проверки null
- JavaDoc: полное описание с `@author`, `@version`, `@return`

## 3. Полная структура обновленного класса

### 3.1 Импорты

**Текущие импорты остаются без изменений:**
- `jakarta.persistence.*`
- `jakarta.validation.constraints.*`
- `lombok.*`
- `org.hibernate.annotations.*`
- `java.time.LocalDateTime`
- `java.util.UUID`

**Дополнительные импорты не требуются** (все необходимые классы уже импортированы).

### 3.2 Структура класса (после изменений)

```java
package com.twitter.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing a Tweet in the database.
 * <p>
 * Maps to the 'tweets' table with all necessary fields and constraints.
 * This entity represents a tweet created by a user in the Twitter system.
 * Supports soft delete functionality through isDeleted flag and deletedAt timestamp.
 *
 * @author geron
 * @version 1.0
 */
@Entity
@Table(name = "tweets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tweet {

    /**
     * Unique identifier for the tweet.
     * Generated automatically using UUID.
     */
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID of the user who created this tweet.
     */
    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", columnDefinition = "UUID", nullable = false)
    private UUID userId;

    /**
     * Content of the tweet.
     * Must be between 1 and 280 characters (after trimming whitespace).
     */
    @NotBlank(message = "Tweet content cannot be blank")
    @Size(min = 1, max = 280, message = "Tweet content must be between 1 and 280 characters")
    @Column(name = "content", length = 280, nullable = false)
    private String content;

    /**
     * Timestamp when the tweet was created.
     * Automatically set by Hibernate on entity creation.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the tweet was last updated.
     * Automatically updated by Hibernate on entity modification.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Flag indicating whether the tweet has been soft deleted.
     * <p>
     * Default value is false (tweet is active). When a tweet is soft deleted,
     * this flag is set to true and the deletedAt timestamp is set to the current time.
     * The tweet data is preserved in the database for analytics and recovery purposes.
     *
     * @author geron
     * @version 1.0
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * Timestamp when the tweet was soft deleted.
     * <p>
     * Null for active tweets (isDeleted = false). When a tweet is soft deleted,
     * this timestamp is set to the current time (LocalDateTime.now()).
     * Used for analytics, audit trails, and potential recovery operations.
     *
     * @author geron
     * @version 1.0
     */
    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;

    /**
     * Custom validation method to ensure content is not just whitespace.
     * This complements the database CHECK constraint.
     */
    @PrePersist
    @PreUpdate
    private void validateContent() {
        if (content != null && content.trim().isEmpty()) {
            throw new IllegalArgumentException("Tweet content cannot be empty or contain only whitespace");
        }
    }

    /**
     * Performs soft delete by setting isDeleted flag and deletedAt timestamp.
     * <p>
     * This method sets the isDeleted flag to true and records the current time
     * as the deletion timestamp. The tweet data is preserved in the database,
     * allowing for analytics and potential recovery operations.
     * <p>
     * This method should be called when a tweet is deleted by its author.
     * The method is idempotent - calling it multiple times will not change
     * the deletion timestamp after the first call.
     *
     * @author geron
     * @version 1.0
     */
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Checks if the tweet is active (not deleted).
     * <p>
     * A tweet is considered active if the isDeleted flag is false or null.
     * This method provides a convenient way to check tweet status without
     * directly accessing the isDeleted field.
     *
     * @return true if tweet is active (not deleted), false if deleted
     * @author geron
     * @version 1.0
     */
    public boolean isActive() {
        return !Boolean.TRUE.equals(isDeleted);
    }
}
```

## 4. Соответствие стандартам

### 4.1 STANDART_CODE.md

- ✅ Использование Lombok аннотаций (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- ✅ `@Builder.Default` для поддержки дефолтных значений в Builder
- ✅ Использование `Boolean` вместо примитивного `boolean` для поддержки nullable и Builder
- ✅ JPA аннотации для маппинга полей (`@Column`)
- ✅ JavaDoc на английском языке

### 4.2 STANDART_JAVADOC.md

- ✅ JavaDoc для всех public полей и методов
- ✅ `@author geron` для всех JavaDoc комментариев
- ✅ `@version 1.0` для всех JavaDoc комментариев
- ✅ `@return` для метода `isActive()`
- ✅ Детальное описание в JavaDoc (назначение, поведение, особенности)

### 4.3 STANDART_PROJECT.md

- ✅ Соответствие структуре существующих entities
- ✅ Использование стандартных JPA паттернов

## 5. Миграция базы данных

### 5.1 SQL миграция (выполняется отдельно)

**Файл:** `sql/tweets.sql` (или отдельный файл миграции)

**SQL:**
```sql
-- Add soft delete columns to tweets table
ALTER TABLE tweets 
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN deleted_at TIMESTAMP NULL;

-- Create index for filtering active tweets (optimization)
CREATE INDEX IF NOT EXISTS idx_tweets_is_deleted 
ON tweets(is_deleted) WHERE is_deleted = false;

-- Add comment for documentation
COMMENT ON COLUMN tweets.is_deleted IS 'Flag indicating whether the tweet has been soft deleted';
COMMENT ON COLUMN tweets.deleted_at IS 'Timestamp when the tweet was soft deleted';
```

**Примечания:**
- Миграция выполняется отдельно от кода
- Индекс с условием `WHERE is_deleted = false` оптимизирует запросы для активных твитов
- DEFAULT false обеспечивает обратную совместимость для существующих записей

## 6. Проверка изменений

### 6.1 Компиляция

После изменений необходимо проверить:
- ✅ Код компилируется без ошибок
- ✅ Lombok генерирует корректные методы (getters, setters, builder)
- ✅ JPA маппинг корректный

### 6.2 Тестирование

**Unit тесты (будут созданы в шаге #12-14):**
- Тест создания твита с дефолтными значениями (isDeleted = false, deletedAt = null)
- Тест метода `softDelete()` - проверка установки флагов
- Тест метода `isActive()` - проверка для активных и удаленных твитов
- Тест идемпотентности `softDelete()` - повторные вызовы не изменяют deletedAt

**Интеграционные тесты:**
- Тест сохранения твита с дефолтными значениями
- Тест сохранения твита после soft delete
- Тест загрузки твита из БД с isDeleted и deletedAt

## 7. Зависимости и совместимость

### 7.1 Обратная совместимость

- ✅ Существующий код продолжит работать (новые поля имеют дефолтные значения)
- ✅ Builder паттерн поддерживается через `@Builder.Default`
- ✅ ВсеArgsConstructor и NoArgsConstructor работают корректно

### 7.2 Влияние на другие компоненты

**Компоненты, которые будут обновлены позже:**
- `TweetRepository` - добавление методов для работы с soft delete (шаг #4)
- `TweetService` - использование метода `softDelete()` (шаг #7)
- `TweetMapper` - обновление маппинга (если потребуется в шаге #5)

**Компоненты, которые не требуют изменений:**
- `CreateTweetRequestDto` - не затронут
- `UpdateTweetRequestDto` - не затронут
- Существующие тесты - будут обновлены в шагах #12-14

## 8. Критерии успеха

- ✅ Поля `isDeleted` и `deletedAt` добавлены в Entity Tweet
- ✅ Метод `softDelete()` реализован
- ✅ Метод `isActive()` реализован (опционально)
- ✅ JavaDoc документация полная и соответствует стандартам
- ✅ Код соответствует STANDART_CODE.md и STANDART_JAVADOC.md
- ✅ Builder паттерн поддерживается через `@Builder.Default`
- ✅ Обратная совместимость обеспечена

## 9. Следующие шаги

После реализации шага #3:
- **Шаг #4:** Обновление Repository — Добавить методы для работы с soft delete
- **Шаг #5:** Обновление DTO — Добавить поля isDeleted и deletedAt в TweetResponseDto (опционально)

---

*Документ создан: 2025-01-27*  
*Версия: 1.0*  
*Статус: Ready for Implementation*

