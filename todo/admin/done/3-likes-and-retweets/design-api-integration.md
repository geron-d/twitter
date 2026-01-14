# Проектирование API интеграции для лайков и ретвитов

## Дата создания
2025-01-27

## Цель
Спроектировать структуру Feign клиентов, Gateway методов и логику выбора твитов и пользователей для добавления функциональности лайков и ретвитов в административный скрипт.

---

## 1. Структура Feign клиентов (TweetsApiClient)

### 1.1 Метод likeTweet

**Эндпоинт в tweet-api:**
- `POST /api/v1/tweets/{tweetId}/like`
- Request Body: `LikeTweetRequestDto` (содержит `userId`)
- Response: `LikeResponseDto` (HTTP 201 Created)
- Ошибки: HTTP 409 Conflict (self-like, дубликат)

**Проектируемый метод в TweetsApiClient:**
```java
/**
 * Likes a tweet in the tweet-api service.
 * <p>
 * This method creates a like record for a specific tweet. The user cannot like their own tweet
 * (self-like), and duplicate likes are prevented by a unique constraint.
 *
 * @param tweetId the unique identifier of the tweet to like
 * @param likeTweetRequest DTO containing userId for the like operation
 * @return LikeResponseDto containing the created like information including ID
 */
@PostMapping("/{tweetId}/like")
LikeResponseDto likeTweet(
    @PathVariable("tweetId") UUID tweetId,
    @RequestBody LikeTweetRequestDto likeTweetRequest
);
```

**Импорты:**
- `com.twitter.common.dto.request.LikeTweetRequestDto` (после перемещения в common-lib)
- `com.twitter.dto.response.LikeResponseDto` (из tweet-api, возможно потребуется переместить в common-lib)

### 1.2 Метод retweetTweet

**Эндпоинт в tweet-api:**
- `POST /api/v1/tweets/{tweetId}/retweet`
- Request Body: `RetweetRequestDto` (содержит `userId`, опционально `comment`)
- Response: `RetweetResponseDto` (HTTP 201 Created)
- Ошибки: HTTP 409 Conflict (self-retweet, дубликат)

**Проектируемый метод в TweetsApiClient:**
```java
/**
 * Retweets a tweet in the tweet-api service.
 * <p>
 * This method creates a retweet record for a specific tweet. The user cannot retweet their own tweet
 * (self-retweet), and duplicate retweets are prevented by a unique constraint. An optional comment
 * can be provided (1-280 characters).
 *
 * @param tweetId the unique identifier of the tweet to retweet
 * @param retweetRequest DTO containing userId and optional comment for the retweet operation
 * @return RetweetResponseDto containing the created retweet information including ID
 */
@PostMapping("/{tweetId}/retweet")
RetweetResponseDto retweetTweet(
    @PathVariable("tweetId") UUID tweetId,
    @RequestBody RetweetRequestDto retweetRequest
);
```

**Импорты:**
- `com.twitter.common.dto.request.RetweetRequestDto` (после перемещения в common-lib)
- `com.twitter.dto.response.RetweetResponseDto` (из tweet-api, возможно потребуется переместить в common-lib)

---

## 2. Структура Gateway методов (TweetsGateway)

### 2.1 Метод likeTweet

**Проектируемый метод:**
```java
/**
 * Likes a tweet in the tweet-api service.
 * <p>
 * This method performs validation, calls the Feign client, and handles errors gracefully.
 * Errors (self-like, duplicate like) are logged but do not throw exceptions to allow
 * script execution to continue.
 *
 * @param tweetId the unique identifier of the tweet to like
 * @param likeTweetRequest DTO containing userId for the like operation
 * @return LikeResponseDto containing the created like information including ID
 * @throws IllegalArgumentException if tweetId or likeTweetRequest is null
 * @throws RuntimeException if the like operation fails (e.g., service unavailable, network error)
 */
public LikeResponseDto likeTweet(UUID tweetId, LikeTweetRequestDto likeTweetRequest) {
    // Валидация входных параметров
    // Вызов Feign клиента
    // Логирование успеха/ошибки
    // Обработка исключений (409 для self-like/дубликата не должно прерывать выполнение)
}
```

**Особенности обработки ошибок:**
- HTTP 409 (Conflict) для self-like и дубликатов: логируется, но не пробрасывается как исключение (для graceful обработки в сервисе)
- Другие ошибки (500, 404, network): пробрасываются как RuntimeException
- Null-валидация входных параметров

### 2.2 Метод retweetTweet

**Проектируемый метод:**
```java
/**
 * Retweets a tweet in the tweet-api service.
 * <p>
 * This method performs validation, calls the Feign client, and handles errors gracefully.
 * Errors (self-retweet, duplicate retweet) are logged but do not throw exceptions to allow
 * script execution to continue.
 *
 * @param tweetId the unique identifier of the tweet to retweet
 * @param retweetRequest DTO containing userId and optional comment for the retweet operation
 * @return RetweetResponseDto containing the created retweet information including ID
 * @throws IllegalArgumentException if tweetId or retweetRequest is null
 * @throws RuntimeException if the retweet operation fails (e.g., service unavailable, network error)
 */
public RetweetResponseDto retweetTweet(UUID tweetId, RetweetRequestDto retweetRequest) {
    // Валидация входных параметров
    // Вызов Feign клиента
    // Логирование успеха/ошибки
    // Обработка исключений (409 для self-retweet/дубликата не должно прерывать выполнение)
}
```

**Особенности обработки ошибок:**
- HTTP 409 (Conflict) для self-retweet и дубликатов: логируется, но не пробрасывается как исключение
- Другие ошибки: пробрасываются как RuntimeException
- Null-валидация входных параметров

---

## 3. Логика выбора твитов и пользователей

### 3.1 Общая стратегия

**Требования:**
- Для каждой операции выбираются разные твиты (6 разных твитов для 6 операций)
- Для каждой операции выбираются разные пользователи (исключая автора твита)
- Использовать `Collections.shuffle()` для случайного выбора
- Обработка ошибок: self-like, self-retweet, дубликаты логируются и добавляются в errors, но выполнение продолжается

### 3.2 Шаги 6-11 в GenerateUsersAndTweetsServiceImpl

#### Шаг 6: Создание лайков (половина пользователей)

**Логика:**
1. Выбрать один случайный твит из `createdTweets` (используя `Collections.shuffle()`)
2. Получить автора этого твита из `TweetResponseDto.userId()`
3. Выбрать половину пользователей из `createdUsers`, исключая автора твита
4. Для каждого выбранного пользователя:
   - Создать `LikeTweetRequestDto` с `userId`
   - Вызвать `tweetsGateway.likeTweet(tweetId, likeTweetRequest)`
   - При успехе: увеличить счетчик `totalLikesCreated`
   - При ошибке (409, self-like, дубликат): добавить в `errors`, продолжить выполнение
   - При критической ошибке: добавить в `errors`, продолжить выполнение

**Код-структура:**
```java
// Step 6: Create likes (half of users)
log.info("Step 6: Creating likes for half of users");
int totalLikesCreated = 0;
if (createdTweets.size() >= 1 && createdUsers.size() >= 2) {
    // Выбрать случайный твит
    List<UUID> availableTweets = new ArrayList<>(createdTweets);
    Collections.shuffle(availableTweets);
    UUID selectedTweetId = availableTweets.get(0);
    
    // Получить автора твита (нужно получить TweetResponseDto)
    // TODO: Получить TweetResponseDto для selectedTweetId (через getUserTweets или кэш)
    UUID tweetAuthorId = ...; // Получить из TweetResponseDto
    
    // Выбрать пользователей (исключая автора)
    List<UUID> availableUsers = new ArrayList<>(createdUsers);
    availableUsers.remove(tweetAuthorId);
    Collections.shuffle(availableUsers);
    int halfCount = availableUsers.size() / 2;
    List<UUID> usersToLike = availableUsers.subList(0, halfCount);
    
    // Создать лайки
    for (UUID userId : usersToLike) {
        try {
            LikeTweetRequestDto likeRequest = LikeTweetRequestDto.builder()
                .userId(userId)
                .build();
            tweetsGateway.likeTweet(selectedTweetId, likeRequest);
            totalLikesCreated++;
        } catch (Exception ex) {
            // Обработка ошибок
        }
    }
}
```

#### Шаг 7: Создание лайков (треть пользователей)

**Логика:**
- Аналогично шагу 6, но выбирается треть пользователей
- Выбирается другой твит (не использованный в шаге 6)

#### Шаг 8: Создание лайков (1 пользователь)

**Логика:**
- Аналогично шагу 6, но выбирается 1 пользователь
- Выбирается другой твит (не использованный в шагах 6-7)

#### Шаг 9: Создание ретвитов (половина пользователей)

**Логика:**
- Аналогично шагу 6, но для ретвитов
- Используется `RetweetRequestDto` (можно передать `comment = null`)
- Выбирается другой твит (не использованный в шагах 6-8)

#### Шаг 10: Создание ретвитов (треть пользователей)

**Логика:**
- Аналогично шагу 9, но выбирается треть пользователей
- Выбирается другой твит (не использованный в шагах 6-9)

#### Шаг 11: Создание ретвитов (1 пользователь)

**Логика:**
- Аналогично шагу 9, но выбирается 1 пользователь
- Выбирается другой твит (не использованный в шагах 6-10)

### 3.3 Проблема получения автора твита

**Проблема:**
- В `createdTweets` хранятся только UUID твитов
- Для исключения автора твита нужно знать `userId` автора
- `getUserTweets()` возвращает все твиты пользователя, но не гарантирует, что нужный твит будет в списке

**Решения:**
1. **Кэширование TweetResponseDto:** Сохранять `TweetResponseDto` при создании твита в Map `Map<UUID, TweetResponseDto>`
2. **Вызов getUserTweets:** Для каждого твита вызывать `getUserTweets()` и искать нужный твит (неэффективно)
3. **Хранение авторов:** Сохранять `Map<UUID, UUID>` (tweetId -> userId) при создании твитов

**Рекомендуемое решение:**
Использовать кэширование `TweetResponseDto` при создании твитов:
```java
Map<UUID, TweetResponseDto> tweetsCache = new HashMap<>();
// При создании твита:
TweetResponseDto tweetResponse = tweetsGateway.createTweet(tweetRequest);
createdTweets.add(tweetResponse.id());
tweetsCache.put(tweetResponse.id(), tweetResponse);
```

### 3.4 Обработка ошибок

**Типы ошибок:**
1. **Self-like / Self-retweet (409):** Логируется, добавляется в `errors`, выполнение продолжается
2. **Дубликат (409):** Логируется, добавляется в `errors`, выполнение продолжается
3. **Критические ошибки (500, 404, network):** Логируется, добавляется в `errors`, выполнение продолжается

**Структура обработки:**
```java
try {
    tweetsGateway.likeTweet(tweetId, likeRequest);
    totalLikesCreated++;
    log.debug("Successfully created like for tweet {} by user {}", tweetId, userId);
} catch (RuntimeException ex) {
    String errorMsg = String.format("Failed to create like for tweet %s by user %s: %s",
        tweetId, userId, ex.getMessage());
    log.warn(errorMsg);
    errors.add(errorMsg);
    // Продолжить выполнение
}
```

---

## 4. Обновление статистики

### 4.1 ScriptStatisticsDto

**Новые поля:**
- `Integer totalLikesCreated` - общее количество созданных лайков
- `Integer totalRetweetsCreated` - общее количество созданных ретвитов

**Обновление конструктора:**
```java
ScriptStatisticsDto statistics = new ScriptStatisticsDto(
    createdUsers.size(),
    createdTweets.size(),
    totalFollowsCreated,
    deletedTweets.size(),
    usersWithTweetsCount,
    usersWithoutTweetsCount,
    totalLikesCreated,      // новое поле
    totalRetweetsCreated,    // новое поле
    executionTimeMs,
    errors
);
```

---

## 5. Зависимости и импорты

### 5.1 Необходимые DTO (после перемещения в common-lib)

- `com.twitter.common.dto.request.LikeTweetRequestDto`
- `com.twitter.common.dto.request.RetweetRequestDto`

### 5.2 Response DTO (требуется решение)

**Варианты:**
1. Переместить `LikeResponseDto` и `RetweetResponseDto` в common-lib (рекомендуется)
2. Создать копии в admin-script-api (не рекомендуется из-за дублирования)
3. Использовать только для логирования, не возвращать из Gateway (упрощение)

**Рекомендация:** Переместить Response DTO в common-lib для консистентности архитектуры.

---

## 6. Edge cases и валидация

### 6.1 Edge cases

1. **Мало твитов:** Если `createdTweets.size() < 6`, использовать доступные твиты (возможно повторное использование)
2. **Мало пользователей:** Если после исключения автора твита остается мало пользователей, использовать доступных
3. **Нет доступных пользователей:** Пропустить операцию, добавить предупреждение в логи

### 6.2 Валидация

- Проверка наличия созданных твитов перед операциями
- Проверка наличия доступных пользователей (исключая автора)
- Обработка случая, когда все пользователи - авторы выбранных твитов

---

## 7. Резюме

### 7.1 Структура Feign клиентов

- Добавить `likeTweet(UUID tweetId, LikeTweetRequestDto request)` в `TweetsApiClient`
- Добавить `retweetTweet(UUID tweetId, RetweetRequestDto request)` в `TweetsApiClient`

### 7.2 Структура Gateway

- Добавить `likeTweet(UUID tweetId, LikeTweetRequestDto request)` в `TweetsGateway`
- Добавить `retweetTweet(UUID tweetId, RetweetRequestDto request)` в `TweetsGateway`
- Обработка ошибок: 409 не прерывает выполнение, другие ошибки пробрасываются

### 7.3 Логика выбора

- 6 разных твитов для 6 операций (использовать `Collections.shuffle()`)
- Пользователи выбираются случайно, исключая автора твита
- Кэширование `TweetResponseDto` для получения автора твита
- Graceful обработка ошибок (логирование + добавление в errors)

### 7.4 Статистика

- Добавить `totalLikesCreated` и `totalRetweetsCreated` в `ScriptStatisticsDto`
- Обновить конструктор и JavaDoc

---

## 8. Следующие шаги

1. Переместить DTO в common-lib (шаг #1 - выполнен)
2. Реализовать методы в TweetsApiClient (шаг #3)
3. Реализовать методы в TweetsGateway (шаг #4)
4. Обновить ScriptStatisticsDto (шаг #5)
5. Реализовать логику в GenerateUsersAndTweetsServiceImpl (шаг #6)