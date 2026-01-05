# Анализ требований: Ретвитнуть с опциональным комментарием

## Дата создания
2025-01-27

## Обзор
Данный документ содержит анализ требований для реализации функционала ретвита твита с опциональным комментарием. Функционал реализуется по аналогии с функционалом Like, но с добавлением опционального поля комментария.

## 1. Входные и выходные данные

### 1.1 Входные данные (Request)
- **Эндпоинт**: `POST /api/v1/tweets/{tweetId}/retweet`
- **Path параметр**: `tweetId` (UUID) - идентификатор твита для ретвита
- **Request Body**: `RetweetRequestDto`
  - `userId` (UUID, обязательное) - идентификатор пользователя, который делает ретвит
  - `comment` (String, опциональное) - комментарий к ретвиту (1-280 символов, null разрешен, пустая строка - нет)

### 1.2 Выходные данные (Response)
- **HTTP Status**: `201 Created` при успешном создании
- **Response Body**: `RetweetResponseDto`
  - `id` (UUID) - идентификатор ретвита
  - `tweetId` (UUID) - идентификатор оригинального твита
  - `userId` (UUID) - идентификатор пользователя, который сделал ретвит
  - `comment` (String, nullable) - комментарий к ретвиту
  - `createdAt` (LocalDateTime) - время создания ретвита

### 1.3 Ошибочные сценарии
- **400 Bad Request**: 
  - Невалидный формат данных (userId null, comment пустая строка, comment > 280 символов)
  - Невалидный формат UUID для tweetId
- **404 Not Found**: 
  - Твит не существует или удален
  - Пользователь не существует
- **409 Conflict**: 
  - Попытка ретвита собственного твита (self-retweet)
  - Дублирование ретвита (пользователь уже ретвитнул этот твит)

## 2. Структура Entity

### 2.1 Retweet Entity

**Расположение**: `services/tweet-api/src/main/java/com/twitter/entity/Retweet.java`

**Структура полей**:
- `id` (UUID) - первичный ключ, генерируется автоматически
- `tweetId` (UUID, NOT NULL) - идентификатор твита
- `userId` (UUID, NOT NULL) - идентификатор пользователя
- `comment` (String, nullable, max 280) - опциональный комментарий
- `createdAt` (LocalDateTime, NOT NULL) - время создания, устанавливается автоматически

**Ограничения**:
- Уникальное ограничение на паре `(tweet_id, user_id)` для предотвращения дублирования
- Foreign key на `tweets(id)` для `tweet_id`
- Foreign key на `users(id)` для `user_id` (через users-api)
- Индекс на `(tweet_id, user_id)` для оптимизации запросов

**Бизнес-методы**:
- `isByUser(UUID userId)` - проверка, что ретвит сделан указанным пользователем
- `isForTweet(UUID tweetId)` - проверка, что ретвит для указанного твита
- `hasComment()` - проверка наличия комментария (не null и не пустая строка)

**Аннотации**:
- `@Entity`, `@Table(name = "tweet_retweets")`
- `@UniqueConstraint` на паре `(tweet_id, user_id)`
- `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` (Lombok)
- `@CreationTimestamp` для `createdAt`
- `@NotNull` для обязательных полей
- `@Size(max = 280)` для `comment`

## 3. Структура DTO

### 3.1 RetweetRequestDto

**Расположение**: `services/tweet-api/src/main/java/com/twitter/dto/request/RetweetRequestDto.java`

**Структура**:
```java
public record RetweetRequestDto(
    @NotNull UUID userId,
    @Size(max = 280) String comment
)
```

**Валидация**:
- `userId`: `@NotNull` - обязательное поле
- `comment`: `@Size(max = 280)` - опциональное поле, максимум 280 символов
- `comment` может быть `null`, но не может быть пустой строкой (валидация в Validator)

**OpenAPI аннотации**:
- `@Schema` на уровне record и полей
- Примеры для всех сценариев

### 3.2 RetweetResponseDto

**Расположение**: `services/tweet-api/src/main/java/com/twitter/dto/response/RetweetResponseDto.java`

**Структура**:
```java
public record RetweetResponseDto(
    UUID id,
    UUID tweetId,
    UUID userId,
    String comment,  // nullable
    LocalDateTime createdAt
)
```

**OpenAPI аннотации**:
- `@Schema` на уровне record и полей
- `@JsonFormat` для `createdAt`

## 4. Бизнес-правила

### 4.1 Правила валидации

1. **Существование твита**: 
   - Твит должен существовать в базе данных
   - Твит не должен быть удален (`isDeleted = false`)

2. **Существование пользователя**: 
   - Пользователь должен существовать (проверка через `UserGateway.existsUser()`)

3. **Запрет self-retweet**: 
   - Пользователь не может ретвитнуть свой собственный твит
   - Проверка: `tweet.userId != requestDto.userId`

4. **Уникальность ретвита**: 
   - Пользователь может ретвитнуть твит только один раз
   - Проверка через `RetweetRepository.existsByTweetIdAndUserId()`
   - Обеспечивается уникальным ограничением в БД

5. **Валидация комментария**: 
   - Если `comment` указан (не null), он должен быть не пустой строкой после trim
   - Максимальная длина комментария: 280 символов
   - `null` разрешен (опциональный комментарий)
   - Пустая строка `""` не разрешена

### 4.2 Бизнес-логика

1. **Создание ретвита**:
   - Валидация всех правил через `RetweetValidator`
   - Создание `Retweet` entity через `RetweetMapper`
   - Сохранение в БД через `RetweetRepository`
   - Обновление счетчика `retweetsCount` в таблице `tweets` (инкремент на 1)
   - Возврат `RetweetResponseDto`

2. **Обновление счетчика**:
   - После создания ретвита необходимо обновить счетчик `retweetsCount` в таблице `tweets`
   - Использовать метод `incrementRetweetsCount()` в `Tweet` entity (если будет добавлен)
   - Или добавить метод `incrementRetweetsCount()` аналогично `incrementLikesCount()`

## 5. SQL скрипт

### 5.1 Структура таблицы

**Файл**: `sql/script_5_tweet_retweets.sql`

**Таблица**: `tweet_retweets`

**Поля**:
- `id` UUID PRIMARY KEY DEFAULT gen_random_uuid()
- `tweet_id` UUID NOT NULL
- `user_id` UUID NOT NULL
- `comment` VARCHAR(280) NULL
- `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

**Ограничения**:
- `CONSTRAINT tweet_retweets_tweet_fk FOREIGN KEY (tweet_id) REFERENCES tweets(id)`
- `CONSTRAINT tweet_retweets_user_fk FOREIGN KEY (user_id) REFERENCES users(id)`
- `CONSTRAINT uk_tweet_retweets_tweet_user UNIQUE (tweet_id, user_id)`

**Индексы**:
- Индекс на `(tweet_id, user_id)` для оптимизации проверки уникальности
- Индекс на `tweet_id` для быстрого поиска всех ретвитов твита
- Индекс на `user_id` для быстрого поиска всех ретвитов пользователя

### 5.2 Обновление таблицы tweets

**Требуется добавить поле** `retweets_count` в таблицу `tweets`:
- `retweets_count INTEGER NOT NULL DEFAULT 0`
- Это поле будет обновляться при создании/удалении ретвитов

**Примечание**: Это изменение должно быть выполнено до реализации функционала ретвита.

## 6. Затронутые стандарты

### 6.1 STANDART_CODE.md
- Использование Records для DTO
- Использование Lombok для Entity
- Использование MapStruct для маппинга
- Использование Bean Validation
- Структура пакетов
- Naming conventions

### 6.2 STANDART_PROJECT.md
- Использование `@LoggableRequest` для логирования запросов
- Использование `GlobalExceptionHandler` для обработки ошибок
- Использование `UserGateway` для проверки пользователей
- Использование исключений из `common-lib`

### 6.3 STANDART_TEST.md
- Unit тесты для Service, Validator, Mapper
- Integration тесты для Controller
- Использование Mockito для моков
- Покрытие всех сценариев (успешных и ошибочных)

### 6.4 STANDART_JAVADOC.md
- JavaDoc для всех публичных классов и методов
- `@author geron`, `@version 1.0`
- `@param`, `@return`, `@throws`
- `@see` для реализации интерфейсов

### 6.5 STANDART_SWAGGER.md
- OpenAPI аннотации в `RetweetApi` интерфейсе
- `@Operation`, `@ApiResponses`, `@ExampleObject`
- `@Schema` аннотации для DTO
- Примеры для всех статус-кодов

### 6.6 STANDART_README.md
- Обновление README.md с описанием нового эндпоинта
- Документация на русском языке
- Примеры запросов и ответов

### 6.7 STANDART_POSTMAN.md
- Добавление запроса в Postman коллекцию
- Примеры для всех сценариев
- Обновление переменных окружения

## 7. Non-functional requirements

### 7.1 Производительность
- Уникальное ограничение на `(tweet_id, user_id)` должно быть проиндексировано
- Индексы на `tweet_id` и `user_id` для быстрого поиска
- Денормализация счетчика `retweetsCount` в таблице `tweets` для оптимизации чтения

### 7.2 Безопасность
- Валидация всех входных данных
- Проверка существования пользователя через users-api
- Защита от дублирования через уникальное ограничение в БД

### 7.3 Надежность
- Транзакционность операций (`@Transactional`)
- Обработка всех исключений через `GlobalExceptionHandler`
- Логирование всех операций через `@LoggableRequest`

### 7.4 Масштабируемость
- Использование UUID для идентификаторов
- Индексы для оптимизации запросов
- Денормализация счетчиков для уменьшения нагрузки на БД

## 8. Компоненты для реализации

### 8.1 Entity
- `Retweet.java` - JPA сущность

### 8.2 Repository
- `RetweetRepository.java` - Spring Data JPA репозиторий
  - `findByTweetIdAndUserId(UUID tweetId, UUID userId)`
  - `existsByTweetIdAndUserId(UUID tweetId, UUID userId)`

### 8.3 DTO
- `RetweetRequestDto.java` - Request DTO
- `RetweetResponseDto.java` - Response DTO

### 8.4 Mapper
- `RetweetMapper.java` - MapStruct интерфейс
  - `toRetweet(RetweetRequestDto, UUID tweetId)`
  - `toRetweetResponseDto(Retweet)`

### 8.5 Validator
- `RetweetValidator.java` - интерфейс валидатора
- `RetweetValidatorImpl.java` - реализация валидатора
  - `validateForRetweet(UUID tweetId, RetweetRequestDto requestDto)`

### 8.6 Service
- `RetweetService.java` - интерфейс сервиса
- `RetweetServiceImpl.java` - реализация сервиса
  - `retweetTweet(UUID tweetId, RetweetRequestDto requestDto)`

### 8.7 Controller
- `RetweetApi.java` - OpenAPI интерфейс
- `RetweetController.java` - REST контроллер
  - `POST /api/v1/tweets/{tweetId}/retweet`

### 8.8 Tests
- `RetweetServiceImplTest.java` - Unit тесты для сервиса
- `RetweetValidatorImplTest.java` - Unit тесты для валидатора
- `RetweetMapperTest.java` - Unit тесты для маппера
- `RetweetControllerTest.java` - Integration тесты для контроллера

## 9. Зависимости

### 9.1 Существующие компоненты
- `TweetRepository` - для проверки существования твита
- `Tweet` entity - для обновления счетчика `retweetsCount`
- `UserGateway` - для проверки существования пользователя
- `GlobalExceptionHandler` - для обработки ошибок
- `@LoggableRequest` - для логирования запросов

### 9.2 Новые компоненты
- `Retweet` entity
- `RetweetRepository`
- `RetweetRequestDto`, `RetweetResponseDto`
- `RetweetMapper`
- `RetweetValidator`, `RetweetValidatorImpl`
- `RetweetService`, `RetweetServiceImpl`
- `RetweetApi`, `RetweetController`

## 10. Риски и предположения

### 10.1 Риски

1. **Отсутствие поля `retweets_count` в таблице `tweets`**:
   - **Решение**: Добавить поле `retweets_count` в таблицу `tweets` перед реализацией функционала
   - **Действие**: Создать миграцию SQL для добавления поля

2. **Валидация комментария**:
   - **Проблема**: Различие между `null` и пустой строкой
   - **Решение**: Валидация в `RetweetValidator` - `null` разрешен, пустая строка - нет

3. **Производительность уникального ограничения**:
   - **Решение**: Создать индекс на `(tweet_id, user_id)` для оптимизации проверки

### 10.2 Предположения

1. Таблица `tweets` будет обновлена с полем `retweets_count` до начала реализации
2. Метод `incrementRetweetsCount()` будет добавлен в `Tweet` entity (аналогично `incrementLikesCount()`)
3. Пользователи проверяются через `UserGateway` (интеграция с users-api)
4. SQL скрипт будет выполнен до запуска приложения
5. Комментарий опционален, но если указан, должен быть валидным (1-280 символов)
6. `null` комментарий разрешен, пустая строка - нет

## 11. Критерии приемки

1. ✅ Entity `Retweet` создана с правильными полями и ограничениями
2. ✅ DTO `RetweetRequestDto` и `RetweetResponseDto` созданы как Records с валидацией
3. ✅ SQL скрипт `script_5_tweet_retweets.sql` создан с таблицей, ограничениями и индексами
4. ✅ Бизнес-правила определены и задокументированы
5. ✅ Все затронутые стандарты определены
6. ✅ Входные и выходные данные определены
7. ✅ Non-functional requirements определены
8. ✅ Все компоненты для реализации определены
9. ✅ Риски и предположения задокументированы

## 12. Следующие шаги

1. Проектирование API и контрактов (#2)
2. Создание SQL скрипта (#3)
3. Реализация Entity (#4)
4. Реализация Repository (#5)
5. Реализация DTO (#6)
6. Реализация Mapper (#7)
7. Реализация Validator (#8)
8. Реализация Service (#9)
9. Реализация Controller (#10-14)
10. Тестирование (#15-16)
11. Документация (#17-19)
12. Проверка стандартов (#20)