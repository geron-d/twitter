# Tweet API Service - Архитектурный документ

## Meta
- **project**: twitter-tweet-api
- **document_type**: Architecture Document
- **version**: 1.0
- **created_date**: 2025-01-27
- **status**: Draft
- **analyst**: AI Assistant

## Executive Summary

Tweet API Service является ключевым компонентом Twitter-подобной платформы, обеспечивающим функциональность создания, управления и взаимодействия с твитами. Сервис построен на основе микросервисной архитектуры с использованием Spring Boot и PostgreSQL, обеспечивая высокую производительность, надежность и масштабируемость.

## 1. Обзор системы

### 1.1 Назначение сервиса
Tweet API Service предоставляет REST API для:
- Создания, чтения, обновления и удаления твитов
- Социальных взаимодействий (лайки, ретвиты)
- Получения твитов пользователей и ленты новостей
- Управления списками взаимодействий

### 1.2 Основные входные и выходные данные

#### Входные данные:
- **Создание твита**: content (max 280 символов), user_id
- **Обновление твита**: tweet_id, новый content
- **Социальные действия**: tweet_id, user_id
- **Запросы данных**: user_id, pagination параметры

#### Выходные данные:
- **Твиты**: полная информация с метаданными
- **Списки твитов**: пагинированные результаты
- **Статистика**: количество лайков, ретвитов
- **Статусы операций**: HTTP статусы и сообщения об ошибках

## 2. Анализ функциональных требований

### 2.1 Основные операции с твитами

#### CRUD операции:
- **Создание твита** (POST /api/v1/tweets)
  - Входные данные: content (max 280 символов), user_id
  - Выходные данные: созданный твит с ID, временными метками
  - Валидация: длина контента, существование пользователя
  
- **Получение твита** (GET /api/v1/tweets/{tweetId})
  - Входные данные: tweet_id
  - Выходные данные: полная информация о твите
  - Обработка: проверка существования, soft delete
  
- **Обновление твита** (PUT /api/v1/tweets/{tweetId})
  - Входные данные: tweet_id, новый content
  - Выходные данные: обновленный твит
  - Ограничения: только автор может редактировать
  
- **Удаление твита** (DELETE /api/v1/tweets/{tweetId})
  - Входные данные: tweet_id
  - Выходные данные: статус операции
  - Реализация: soft delete с отметкой времени

#### Социальные функции:
- **Лайк/анлайк твита** (POST/DELETE /api/v1/tweets/{tweetId}/like)
  - Уникальность: один пользователь = один лайк
  - Атомарность: транзакционное выполнение
  
- **Ретвит/анретвит** (POST/DELETE /api/v1/tweets/{tweetId}/retweet)
  - Аналогично лайкам с уникальностью
  - Возможность добавления комментария к ретвиту

#### Получение данных:
- **Твиты пользователя** (GET /api/v1/tweets/user/{userId})
  - Пагинация: offset/limit параметры
  - Сортировка: по дате создания (DESC)
  - Фильтрация: исключение удаленных твитов
  
- **Лента новостей** (GET /api/v1/tweets/timeline/{userId})
  - Сложность: требует интеграции с follow-service
  - Оптимизация: кэширование, индексы
  
- **Списки взаимодействий** (GET /api/v1/tweets/{tweetId}/likes|retweets)
  - Пагинация для больших списков
  - Информация о пользователях через users-api

### 2.2 Бизнес-правила и ограничения

#### Ограничения контента:
- Максимальная длина твита: 280 символов
- Поддержка Unicode символов
- Запрет на пустой контент
- Валидация на XSS и вредоносный контент

#### Правила доступа:
- Только автор может редактировать/удалять твит
- Все пользователи могут читать публичные твиты
- Проверка существования пользователя через users-api

#### Ограничения производительности:
- Максимум 1 лайк/ретвит на пользователя на твит
- Атомарные операции для социальных функций
- Batch операции для множественных запросов

## 3. Архитектурные компоненты

### 3.1 Слоистая архитектура

#### Controller Layer
- **REST API endpoints** с OpenAPI документацией и Swagger UI
- **RESTful архитектура** с четким разделением ресурсов и HTTP методов
- **TweetController** с реализацией TweetApi интерфейса
- **Валидация входных данных** через Bean Validation и кастомные валидаторы
- **Обработка HTTP статусов** и стандартизированных ошибок
- **Request/Response mapping** через стандартизированные DTO структуры
- **Пагинация** с offset-based стратегией (максимум 100 элементов на страницу)
- **Автоматическое логирование** через @LoggableRequest из shared-lib
- **Централизованная обработка ошибок** через GlobalExceptionHandler

#### Service Layer
- **Бизнес-логика** и правила домена
- **TweetService** интерфейс с TweetServiceImpl реализацией
- **Транзакционность** через @Transactional с правильными уровнями изоляции
- **Интеграция с внешними сервисами** через Circuit Breaker и Fallback
- **Валидация бизнес-правил** перед операциями
- **Кэширование** для оптимизации производительности
- **Типизированные исключения** для обработки ошибок
- **Координация операций** между различными компонентами
- **Обработка ошибок** и исключений

#### Repository Layer
- **Доступ к данным** через Spring Data JPA
- **TweetRepository, LikeRepository, RetweetRepository** интерфейсы
- **JPA Entities** с оптимизированными индексами и валидацией
- **Кастомные запросы** для сложных операций через @Query
- **Specification паттерн** для динамических запросов
- **Batch операции** для оптимизации производительности
- **Soft delete** поддержка с временными метками
- **Денормализация** статистики для быстрых запросов

### 3.2 Бизнес-логика и транзакции

#### Основные операции с твитами
- **Создание твита**: валидация входных данных, проверка пользователя через users-api, проверка статуса пользователя, создание и сохранение твита
- **Получение твита**: поиск по ID с исключением удаленных твитов (@Transactional(readOnly = true))
- **Обновление твита**: проверка прав автора, валидация обновления, обновление данных и сохранение
- **Удаление твита**: soft delete с сохранением статистики

#### Социальные функции
- **Лайк твита**: проверка существования твита и пользователя, проверка на самолайк, проверка дублирования, создание лайка, обновление счетчика
- **Убрать лайк**: поиск лайка, удаление, обновление счетчика
- **Ретвит**: проверка существования твита и пользователя, проверка на саморетвит, проверка дублирования, создание ретвита, обновление счетчика

#### Получение данных
- **Твиты пользователя**: проверка существования пользователя, получение твитов с пагинацией, преобразование в DTO
- **Лента новостей**: проверка существования пользователя, получение подписок через follow-service, получение твитов с пагинацией

#### Уровни изоляции транзакций
- **READ_COMMITTED** для операций чтения (@Transactional(readOnly = true))
- **REPEATABLE_READ** для операций создания (@Transactional)
- **SERIALIZABLE** для социальных функций (предотвращение дублирования)

### 3.3 JPA Entities и структура данных

#### Tweet Entity
- **UUID идентификатор** с автогенерацией
- **userId, content** основные поля твита
- **createdAt, updatedAt** временные метки
- **isDeleted, deletedAt** поддержка soft delete
- **likesCount, retweetsCount, repliesCount** денормализованные счетчики
- **statsUpdatedAt** метка обновления статистики
- **Бизнес-методы**: isActive(), softDelete(), incrementLikesCount(), decrementLikesCount()

#### Like Entity
- **UUID идентификатор** с автогенерацией
- **tweetId, userId** связь с твитом и пользователем
- **createdAt** временная метка создания
- **Уникальное ограничение** на пару (tweetId, userId)
- **Бизнес-методы**: isByUser(), isForTweet()

#### Retweet Entity
- **UUID идентификатор** с автогенерацией
- **tweetId, userId** связь с твитом и пользователем
- **comment** опциональный комментарий к ретвиту
- **createdAt** временная метка создания
- **Уникальное ограничение** на пару (tweetId, userId)
- **Бизнес-методы**: isByUser(), isForTweet(), hasComment()

#### Индексы для производительности
- **idx_tweets_user_id_created_at** для твитов пользователя
- **idx_tweets_created_at** для временной сортировки
- **idx_tweets_is_deleted** для фильтрации удаленных
- **idx_tweets_likes_count** для сортировки по популярности
- **idx_likes_tweet_id, idx_likes_user_id** для лайков
- **idx_retweets_tweet_id, idx_retweets_user_id** для ретвитов

#### DTO/Mapper Layer
- **Стандартизированные DTO структуры** для Request/Response
- **Record-based DTOs** для неизменяемости и краткости кода
- **MapStruct** для автоматического маппинга между слоями
- **TweetMapper, LikeMapper, RetweetMapper** интерфейсы
- **Comprehensive validation** через Bean Validation и кастомные валидаторы
- **OpenAPI аннотации** для автоматической документации API
- **Группы валидации** для разных операций (Create, Update, Patch)
- **Типизированные Error DTOs** для консистентной обработки ошибок
- **Стандартизированные ответы** с metadata и graceful error handling
- **API versioning** через URL path (/api/v1/)

### 3.4 Repository интерфейсы и кастомные запросы

#### TweetRepository
- **findByIdAndNotDeleted()** - поиск активного твита по ID
- **findByUserIdAndNotDeletedOrderByCreatedAtDesc()** - твиты пользователя с пагинацией
- **findByUserIdInAndNotDeletedOrderByCreatedAtDesc()** - твиты множественных пользователей для ленты
- **incrementLikesCount(), decrementLikesCount()** - обновление счетчиков лайков
- **incrementRetweetsCount(), decrementRetweetsCount()** - обновление счетчиков ретвитов
- **countByUserIdAndNotDeleted()** - подсчет твитов пользователя
- **findTopTweetsByEngagement()** - топ твиты по популярности
- **findByContentContainingAndNotDeleted()** - поиск по содержимому
- **softDeleteById(), softDeleteByUserId()** - массовое soft delete

#### LikeRepository
- **findByTweetIdAndUserId()** - поиск лайка пользователя для твита
- **findByTweetIdOrderByCreatedAtDesc()** - лайки твита с пагинацией
- **findByUserIdOrderByCreatedAtDesc()** - лайки пользователя с пагинацией
- **countByTweetId(), countByUserId()** - подсчет лайков
- **existsByTweetIdAndUserId()** - проверка существования лайка
- **deleteByTweetId(), deleteByUserId()** - массовое удаление лайков
- **findUserIdsByTweetId()** - пользователи, лайкнувшие твит

#### RetweetRepository
- **findByTweetIdAndUserId()** - поиск ретвита пользователя для твита
- **findByTweetIdOrderByCreatedAtDesc()** - ретвиты твита с пагинацией
- **findByUserIdOrderByCreatedAtDesc()** - ретвиты пользователя с пагинацией
- **countByTweetId(), countByUserId()** - подсчет ретвитов
- **existsByTweetIdAndUserId()** - проверка существования ретвита
- **findByTweetIdWithComments()** - ретвиты с комментариями
- **deleteByTweetId(), deleteByUserId()** - массовое удаление ретвитов
- **findUserIdsByTweetId()** - пользователи, ретвитнувшие твит

#### Specification паттерн для динамических запросов
- **TweetSpecification** класс с методами для динамических запросов
- **hasUserId(), isNotDeleted()** - базовые фильтры
- **hasContentContaining()** - поиск по содержимому
- **createdAfter(), createdBefore()** - фильтрация по времени
- **hasMinLikesCount(), hasMinRetweetsCount()** - фильтрация по статистике
- **orderByCreatedAtDesc(), orderByEngagementDesc()** - сортировка

### 3.5 Request/Response DTOs и структура данных

#### Request DTOs
- **CreateTweetRequestDto** - content (1-280 символов), userId с валидацией @NotBlank, @Size, @Pattern
- **UpdateTweetRequestDto** - content (1-280 символов), userId с аналогичной валидацией
- **LikeTweetRequestDto** - userId для лайка твита
- **RetweetRequestDto** - userId и опциональный comment (до 280 символов)

#### Response DTOs
- **TweetResponseDto** - id, userId, content, createdAt, updatedAt, isDeleted, stats (TweetStatsDto)
- **TweetStatsDto** - likesCount, retweetsCount, repliesCount с валидацией @Min(0)
- **LikeResponseDto** - id, tweetId, userId, createdAt
- **RetweetResponseDto** - id, tweetId, userId, comment, createdAt

#### Error DTOs
- **ErrorResponseDto** - error (ErrorInfoDto), meta (ResponseMetaDto)
- **ErrorInfoDto** - code, message, details (Map<String, Object>)
- **ValidationErrorResponseDto** - error с деталями валидации, meta
- **ResponseMetaDto** - timestamp, requestId для трассировки

#### OpenAPI документация
- **@Schema аннотации** для всех DTO с примерами и описаниями
- **requiredMode** для указания обязательных полей
- **format** для типов данных (uuid, date-time)
- **minLength, maxLength** для ограничений длины
- **example** для примеров значений

### 3.6 MapStruct мапперы и преобразование данных

#### TweetMapper
- **toTweet(CreateTweetRequestDto)** - создание Entity из Request DTO с игнорированием системных полей
- **toTweetResponseDto(Tweet)** - преобразование Entity в Response DTO с вложенной статистикой
- **updateTweetFromUpdateDto(UpdateTweetRequestDto, @MappingTarget Tweet)** - обновление Entity из Update DTO
- **toTweetStats(Tweet)** - кастомный метод для преобразования статистики
- **toTweetResponseDtoList(List<Tweet>)** - маппинг списков твитов
- **toTweetResponseDtoPage(Page<Tweet>)** - маппинг пагинированных результатов

#### LikeMapper
- **toLike(LikeTweetRequestDto, UUID tweetId)** - создание Like Entity с tweetId
- **toLikeResponseDto(Like)** - преобразование Like Entity в Response DTO
- **toLikeWithTweetId(LikeTweetRequestDto, UUID tweetId)** - кастомный маппер с tweetId
- **toLikeResponseDtoList(List<Like>)** - маппинг списков лайков
- **toLikeResponseDtoPage(Page<Like>)** - маппинг пагинированных лайков

#### RetweetMapper
- **toRetweet(RetweetRequestDto, UUID tweetId)** - создание Retweet Entity с tweetId
- **toRetweetResponseDto(Retweet)** - преобразование Retweet Entity в Response DTO
- **toRetweetWithTweetId(RetweetRequestDto, UUID tweetId)** - кастомный маппер с tweetId и comment
- **toRetweetResponseDtoList(List<Retweet>)** - маппинг списков ретвитов
- **toRetweetResponseDtoPage(Page<Retweet>)** - маппинг пагинированных ретвитов

#### Конфигурация MapStruct
- **@Mapper(componentModel = "spring")** для интеграции с Spring
- **@Mapping(target = "field", ignore = true)** для игнорирования полей при маппинге
- **@MappingTarget** для обновления существующих объектов
- **@Named** для кастомных методов маппинга
- **default методы** для сложной логики маппинга

### 3.7 Валидация DTO и кастомные валидаторы

#### Bean Validation аннотации
- **@NotBlank** для проверки непустых строк
- **@Size(min, max)** для ограничения длины строк
- **@Pattern** для проверки регулярных выражений
- **@NotNull** для проверки непустых значений
- **@Min(value)** для проверки минимальных значений
- **@Valid** для каскадной валидации вложенных объектов

#### Кастомные валидаторы
- **@UserExists** - проверка существования пользователя через UsersApiClient
- **UserExistsValidator** - реализация валидатора с fallback при ошибках API
- **@NoSelfAction** - проверка на самодействия (лайк/ретвит собственного твита)
- **NoSelfActionValidator** - реализация с рефлексией для разных DTO типов

#### Группы валидации
- **ValidationGroups.Create** - для операций создания
- **ValidationGroups.Update** - для операций обновления
- **ValidationGroups.Patch** - для операций частичного обновления
- **Использование групп** в @Validated аннотациях контроллеров

#### Обработка ошибок валидации
- **MethodArgumentNotValidException** - обработка ошибок валидации в контроллерах
- **ValidationErrorResponseDto** - структурированный ответ с деталями ошибок
- **FieldError** - информация о конкретных полях с ошибками
- **BindingResult** - результат валидации с детальной информацией

### 3.5 Оптимизация и производительность

#### Batch операции для оптимизации
- **batchUpdateLikesCount()** - массовое обновление счетчиков лайков
- **batchSoftDelete()** - массовое soft delete твитов
- **@Modifying** аннотации для bulk операций
- **EntityManager** для нативных запросов

#### Конфигурация JPA/Hibernate
- **batch_size: 20** для batch операций
- **order_inserts: true** для оптимизации вставок
- **order_updates: true** для оптимизации обновлений
- **batch_versioned_data: true** для версионированных данных
- **open-in-view: false** для предотвращения LazyInitializationException

#### Мониторинг производительности
- **format_sql: true** для форматирования SQL запросов
- **show_sql: false** в production для производительности
- **Индексы** для оптимизации частых запросов
- **Статистика** использования индексов через PostgreSQL
- **Стандартизированные ответы** с метаданными (timestamp, requestId)
- **Graceful error handling** с детальными кодами ошибок
- **Versioning** для обратной совместимости API

### 3.2 Модель данных

#### Архитектурные принципы проектирования
- **UUID идентификаторы** для поддержки распределенных систем
- **Soft delete** с временными метками для сохранения истории
- **Дениormalized счетчики** для оптимизации частых запросов статистики
- **Составные индексы** для ускорения сложных запросов
- **Автоматические триггеры** для поддержания целостности данных

#### Основные сущности
- **tweets** - основная таблица твитов с метаданными и статистикой
- **tweet_likes** - таблица лайков с уникальностью на пользователя
- **tweet_retweets** - таблица ретвитов с возможностью комментариев
- **tweet_replies** - таблица ответов на твиты

#### Стратегия индексации
- **Составные индексы** для основных запросов (user_id + created_at)
- **Full-text search** индексы для поиска по содержимому
- **Специализированные индексы** для аналитических запросов
- **Партиционированные индексы** для больших объемов данных

#### Автоматизация и целостность
- **Триггеры** для автоматического обновления счетчиков лайков/ретвитов/ответов
- **Представления** для оптимизированных запросов и консистентности данных
- **Ограничения** для валидации бизнес-правил на уровне БД
- **Cascade удаление** для поддержания ссылочной целостности

### 3.3 REST API Design

#### Архитектурные принципы API
- **RESTful архитектура** с четким разделением ресурсов
- **HTTP методы** соответствуют операциям (GET, POST, PUT, DELETE)
- **Статус коды** отражают результат операции
- **Версионирование** через URL path (/api/v1/)
- **Консистентность** в именовании и структуре

#### Структура API endpoints

**Основные операции с твитами:**
- `POST /api/v1/tweets` - Создание твита (HttpStatus.CREATED, @Valid валидация)
- `GET /api/v1/tweets/{tweetId}` - Получение твита по ID (Optional pattern, 404 при отсутствии)
- `PUT /api/v1/tweets/{tweetId}` - Обновление твита (только автор, авторизация в Service Layer)
- `DELETE /api/v1/tweets/{tweetId}` - Удаление твита (soft delete, ResponseEntity.noContent())

**Операции с пользователями:**
- `GET /api/v1/tweets/user/{userId}` - Твиты пользователя с пагинацией (@PageableDefault, PagedModel)
- `GET /api/v1/tweets/timeline/{userId}` - Лента новостей пользователя (сложная бизнес-логика в Service Layer)

**Социальные функции:**
- `POST /api/v1/tweets/{tweetId}/like` - Лайк твита (вложенные ресурсы, проверка дублирования)
- `DELETE /api/v1/tweets/{tweetId}/like` - Убрать лайк (ResponseEntity.noContent())
- `POST /api/v1/tweets/{tweetId}/retweet` - Ретвит с комментарием (HttpStatus.CREATED)
- `DELETE /api/v1/tweets/{tweetId}/retweet` - Убрать ретвит (ResponseEntity.noContent())

**Получение статистики и списков:**
- `GET /api/v1/tweets/{tweetId}/likes` - Список пользователей, лайкнувших твит (PagedModel)
- `GET /api/v1/tweets/{tweetId}/retweets` - Список пользователей, ретвитнувших твит (PagedModel)

#### Реализация контроллеров
- **TweetController** implements TweetApi интерфейс
- **@RestController** с @RequestMapping("/api/v1/tweets")
- **@RequiredArgsConstructor** для автоматической инъекции зависимостей
- **@LoggableRequest** на всех методах для автоматического логирования
- **Делегирование бизнес-логики** в TweetService
- **Optional pattern** для обработки отсутствующих данных

#### Параметры пагинации
- **page**: номер страницы (начиная с 0, по умолчанию 0)
- **size**: размер страницы (1-100, по умолчанию 20)
- **Пример**: `?page=0&size=20`

#### HTTP статус коды
- **200 OK**: Успешное получение данных
- **201 Created**: Успешное создание ресурса (твит, лайк, ретвит)
- **204 No Content**: Успешное удаление без возврата данных
- **400 Bad Request**: Ошибки валидации или некорректные данные
- **401 Unauthorized**: Отсутствие аутентификации
- **403 Forbidden**: Недостаточно прав (не автор твита)
- **404 Not Found**: Ресурс не найден (твит, пользователь)
- **409 Conflict**: Конфликт состояния (дублирование лайка/ретвита)
- **500 Internal Server Error**: Внутренняя ошибка сервера
- **503 Service Unavailable**: Внешний сервис недоступен

#### Стандартизированные ответы
- **Успешные ответы**: структура с data и meta полями
- **Ошибки**: детальные коды ошибок с контекстом
- **Пагинация**: стандартная структура с метаданными
- **Метаданные**: timestamp, requestId для трассировки

#### OpenAPI интеграция
- **TweetApi интерфейс** с @Operation и @ApiResponses аннотациями
- **DTO с @Schema аннотациями** для детальной документации
- **TweetApiOpenApiConfig** для конфигурации OpenAPI
- **Swagger UI** с настройками отображения и фильтрации
- **SpringDoc OpenAPI** для автоматической генерации документации
- **Примеры запросов/ответов** в @Schema аннотациях

#### Основные DTO структуры

**Request DTOs:**
- `CreateTweetRequest`: content (1-280 символов), userId
- `UpdateTweetRequest`: content (1-280 символов)
- `LikeTweetRequest`: userId
- `RetweetRequest`: userId, comment (опционально, до 280 символов)

**Response DTOs:**
- `TweetResponse`: полная информация о твите с статистикой и автором
- `TweetListResponse`: список твитов с пагинацией
- `LikeResponse`: информация о лайке
- `RetweetResponse`: информация о ретвите
- `PaginationInfo`: метаданные пагинации (page, size, totalElements, totalPages)

**Error DTOs:**
- `ErrorResponse`: стандартная структура ошибки с кодом и сообщением
- `ValidationErrorResponse`: детали ошибок валидации
- `ResponseMeta`: метаданные ответа (timestamp, requestId)

#### Валидация и бизнес-правила

**Правила создания твита:**
- Контент не может быть пустым или состоять только из пробелов
- Максимальная длина контента: 280 символов
- Пользователь должен существовать в users-api
- Твит не может быть создан от имени несуществующего пользователя

**Правила обновления твита:**
- Только автор может обновлять твит
- Удаленные твиты нельзя обновлять
- Новый контент должен соответствовать правилам валидации

**Правила удаления твита:**
- Только автор может удалять твит
- Удаление выполняется как soft delete
- Статистика (лайки, ретвиты) сохраняется

**Правила социальных действий:**
- Пользователь не может лайкнуть/ретвитнуть свой твит
- Один пользователь может лайкнуть твит только один раз
- Один пользователь может ретвитнуть твит только один раз
- При удалении твита все связанные действия помечаются как неактивные

**Технические правила валидации:**
- **Bean Validation** для базовой валидации полей (@NotBlank, @Size, @Pattern)
- **Кастомные валидаторы** для бизнес-правил (@UserExists, @NoSelfAction)
- **Группы валидации** для разных операций (@Validated(CreateGroup.class))
- **Валидация UUID** через @NotNull и @Valid аннотации
- **Обработка ошибок валидации** через @ExceptionHandler(MethodArgumentNotValidException.class)

#### Стандартные коды ошибок
- **VALIDATION_ERROR**: Ошибки валидации входных данных
- **INTERNAL_SERVER_ERROR**: Внутренняя ошибка сервера
- **TWEET_NOT_FOUND**: Твит не найден
- **TWEET_ALREADY_DELETED**: Твит уже удален
- **TWEET_ACCESS_DENIED**: Недостаточно прав для операции с твитом
- **USER_NOT_FOUND**: Пользователь не найден
- **USER_SERVICE_UNAVAILABLE**: Сервис пользователей недоступен
- **LIKE_ALREADY_EXISTS**: Лайк уже существует
- **LIKE_NOT_FOUND**: Лайк не найден
- **RETWEET_ALREADY_EXISTS**: Ретвит уже существует
- **RETWEET_NOT_FOUND**: Ретвит не найден
- **SELF_ACTION_NOT_ALLOWED**: Самодействие не разрешено
- **INVALID_PAGE_NUMBER**: Некорректный номер страницы
- **INVALID_PAGE_SIZE**: Некорректный размер страницы

### 3.4 Архитектурные паттерны

#### Repository Pattern
- Абстракция доступа к данным
- Единообразный интерфейс для разных источников данных
- Легкое тестирование через моки

#### DTO Pattern
- Изоляция внутренней модели от внешнего API
- Контроль над передаваемыми данными
- Версионирование API без изменения внутренней модели

#### Service Layer Pattern
- Инкапсуляция бизнес-логики
- Транзакционность операций
- Переиспользование логики между контроллерами

#### Dependency Injection
- Слабая связанность компонентов
- Легкое тестирование и мокирование
- Конфигурируемость через Spring

## 4. Технологический стек

### 4.1 Основные технологии
- **Spring Boot 3.5.5**: основной фреймворк
- **Java 24**: язык программирования
- **PostgreSQL**: реляционная база данных
- **JPA/Hibernate**: ORM для работы с БД

### 4.2 Дополнительные библиотеки
- **MapStruct**: маппинг объектов
- **Spring Web**: REST API
- **Spring Data JPA**: репозитории
- **Spring Validation**: валидация данных
- **Spring Actuator**: мониторинг и метрики

### 4.3 Инфраструктура
- **Docker**: контейнеризация
- **Docker Compose**: оркестрация сервисов
- **Gradle**: сборка проекта
- **JUnit 5**: unit тестирование
- **TestContainers**: интеграционное тестирование

### 4.4 Shared/Common-lib компоненты

#### LoggableRequestAspect
- **Автоматическое логирование** HTTP запросов и ответов через AOP
- **Скрытие чувствительных данных** через hideFields параметр
- **AOP интеграция** с @LoggableRequest аннотацией
- **Структурированное логирование** с детальной информацией
- **Конфигурируемость** через аннотацию параметры

#### Система исключений
- **ValidationException** (абстрактное базовое исключение)
- **UniquenessValidationException** для проверки уникальности данных
- **BusinessRuleValidationException** для нарушений бизнес-правил
- **FormatValidationException** для ошибок формата данных
- **ValidationType enum** для классификации типов валидации
- **GlobalExceptionHandler** для централизованной обработки ошибок

#### GlobalExceptionHandler
- **RFC 7807 Problem Details** стандарт для ответов об ошибках
- **Централизованная обработка** всех типов исключений
- **Консистентные ответы** для всех сервисов
- **Автоматическое логирование** ошибок
- **HTTP статус коды**: 400 Bad Request, 409 Conflict, 500 Internal Server Error

#### Enums и утилиты
- **UserRole** (ADMIN, MODERATOR, USER) для авторизации
- **UserStatus** (ACTIVE, INACTIVE) для управления состоянием
- **PasswordUtil** для работы с паролями
- **PatchDtoFactory** для PATCH операций

#### Паттерны использования
- **@LoggableRequest** на методах контроллера для логирования
- **Типизированные исключения** для разных типов ошибок
- **@Enumerated(EnumType.STRING)** для сохранения enums в БД
- **RequestContextHolder** для получения HTTP контекста
- **Factory методы** для создания типичных исключений

## 5. Интеграции с внешними сервисами

### 5.1 Интеграция с users-api

#### Архитектура users-api
- **Слоистая архитектура**: controller → service → repository → entity
- **Порт**: 8081, база данных PostgreSQL
- **OpenAPI/Swagger** конфигурация с детальной документацией
- **Actuator endpoints** для мониторинга (health, info, metrics, tracing)
- **Структурированное логирование** с traceId/spanId для трассировки

#### API контракты интеграции:
- **GET /api/v1/users/{userId}/exists** - проверка существования пользователя
- **GET /api/v1/users/{userId}** - получение информации о пользователе
- **Стандартизированные ответы** с детальными кодами ошибок
- **Contract testing** с Pact для обеспечения совместимости

#### Текущие интеграции:
- **Проверка существования пользователей** при создании твита, лайке/ретвите
- **Получение информации о пользователях** для отображения в списках твитов
- **Валидация пользователей** для всех операций через кастомные валидаторы

#### Архитектурные решения:
- **HTTP REST API** для синхронной интеграции
- **Circuit Breaker Pattern** для защиты от сбоев внешних сервисов
- **Retry механизмы** с exponential backoff
- **Timeout настройки** для предотвращения зависания
- **Fallback стратегии** для graceful degradation

#### Обработка ошибок интеграции:
- **Детальные коды ошибок** (USER_NOT_FOUND, USER_SERVICE_UNAVAILABLE)
- **Кэширование** информации о пользователях на 10 минут
- **Graceful degradation** с ограниченной функциональностью
- **Monitoring и alerting** для проблем интеграции
- **Structured logging** для трассировки запросов

#### Консистентность архитектуры:
- **Следование паттернам users-api** для единообразия
- **Использование shared/common-lib** компонентов
- **Аналогичная структура пакетов** и слоев
- **Совместимые конфигурации** Spring Boot и OpenAPI

#### Интеграция с users-api через Service Layer
- **UsersApiClient** интерфейс для взаимодействия с users-api
- **Circuit Breaker** для защиты от сбоев внешних сервисов
- **Fallback стратегии** для graceful degradation при недоступности users-api
- **Кэширование** информации о пользователях для оптимизации производительности
- **Типизированные исключения** для обработки ошибок интеграции

#### Circuit Breaker конфигурация
- **failure-rate-threshold**: 50% для перехода в открытое состояние
- **wait-duration-in-open-state**: 30 секунд перед попыткой восстановления
- **sliding-window-size**: 10 запросов для анализа
- **minimum-number-of-calls**: 5 минимальных вызовов для анализа
- **permitted-number-of-calls-in-half-open-state**: 3 разрешенных вызова в полуоткрытом состоянии

### 5.2 Будущие интеграции

#### follow-service (планируется):
- **Получение списка подписок** для ленты новостей
- **Обновление лент** при изменении подписок
- **Асинхронная обработка** через message queues

#### timeline-service (планируется):
- **Кэширование лент** для быстрого доступа
- **Асинхронное обновление** при создании твитов
- **Personalization** на основе поведения пользователя

## 6. Нефункциональные требования

### 6.1 Производительность
- **Время ответа**: < 200ms для чтения, < 500ms для записи
- **Пропускная способность**: 1000 RPS на чтение, 100 RPS на запись
- **Database performance**: Query execution time < 100ms
- **Cache hit rate**: > 80% для часто запрашиваемых данных

### 6.2 Надежность и доступность
- **Доступность**: 99.9% uptime
- **Error rate**: < 0.1% для всех операций
- **Recovery time**: < 5 минут для восстановления после сбоя
- **Data consistency**: 100% для критических операций

## 7. Риски и митигация

### 7.1 Технические риски

#### Производительность базы данных
- **Риск**: Медленные запросы при росте объема данных
- **Митигация**: Составные индексы, партиционирование таблиц, архивирование старых данных
- **Мониторинг**: Время выполнения запросов, использование индексов, размер БД

#### Высокая нагрузка на чтение
- **Риск**: Перегрузка сервера при запросах ленты новостей
- **Митигация**: Redis кэширование, CDN, оптимизированная пагинация, read replicas
- **Мониторинг**: RPS, время ответа, hit rate кэша, использование CPU

#### Проблемы интеграции
- **Риск**: Каскадные сбои при недоступности users-api
- **Митигация**: Circuit breaker pattern, retry механизмы, fallback стратегии
- **Мониторинг**: Доступность внешних сервисов, время ответа, количество ошибок

### 7.2 Бизнес-риски

#### Потеря данных
- **Риск**: Коррупция или потеря данных при сбоях
- **Митигация**: Database replication, automated backups, ACID транзакции
- **Мониторинг**: Целостность данных, успешность бэкапов, replication lag

#### Безопасность
- **Риск**: Несанкционированный доступ к данным
- **Митигация**: Strong authentication, input validation, rate limiting
- **Мониторинг**: Попытки несанкционированного доступа, подозрительная активность

## 8. Критерии успешности и метрики

### 8.1 Технические метрики
- **Response time**: < 200ms для чтения, < 500ms для записи
- **Throughput**: 1000 RPS для чтения, 100 RPS для записи
- **Availability**: 99.9% uptime
- **Error rate**: < 0.1% для всех операций

### 8.2 Бизнес-метрики
- **API success rate**: > 99.9%
- **User satisfaction**: Measured through feedback
- **Feature adoption**: Usage of new features
- **Performance perception**: User-reported performance

### 8.3 Операционные метрики
- **Deployment frequency**: Weekly releases
- **Lead time**: < 1 day from commit to production
- **Mean time to recovery**: < 30 minutes
- **Change failure rate**: < 5%

## 9. Миграции и развертывание

### 9.1 Стратегия миграций
- **Создание схемы** tweet_api в PostgreSQL
- **Поэтапное развертывание** таблиц, индексов и триггеров
- **Версионирование миграций** для отслеживания изменений
- **Rollback стратегии** для безопасного отката изменений

## 10. Заключение

### 10.1 Ключевые архитектурные решения
1. **Микросервисная архитектура** с четким разделением ответственности
2. **RESTful API** с стандартизированными DTO и обработкой ошибок
3. **UUID идентификаторы** для поддержки распределенных систем
4. **Soft delete** для сохранения истории без потери производительности
5. **Дениormalized счетчики** для оптимизации частых запросов
6. **Автоматические триггеры** для поддержания целостности данных
7. **Comprehensive validation** с Bean Validation и кастомными валидаторами
8. **Circuit breaker pattern** для надежной интеграции с внешними сервисами

### 10.2 Консистентность с users-api

#### Архитектурные принципы
- **Слоистая архитектура**: controller → service → repository → entity
- **Интерфейс-реализация** паттерн для всех слоев
- **DTO паттерн** с использованием Java records
- **MapStruct маппинг** для автоматической генерации мапперов
- **Двухуровневая валидация** (DTO + бизнес-правила)

#### Конфигурация и настройки
- **Порт**: 8082 (следующий после users-api 8081)
- **База данных**: та же PostgreSQL с отдельной схемой tweet_api
- **OpenAPI конфигурация** аналогичная users-api
- **Actuator endpoints** для мониторинга (health, info, metrics, tracing)
- **Структурированное логирование** с traceId/spanId

#### Использование shared/common-lib
- **LoggableRequestAspect** для автоматического логирования HTTP запросов
- **Система исключений** ValidationException и подклассы
- **GlobalExceptionHandler** расширенный для tweet-specific ошибок
- **Enums** для TweetStatus, TweetType если необходимо

#### Интеграция shared-lib в tweet-api
- **Зависимости**: `implementation project(':shared:common-lib')` в build.gradle
- **Конфигурация**: `@Import(GlobalExceptionHandler.class)` в TweetApiConfig
- **Логирование**: `@LoggableRequest(hideFields = {"password", "token"})` на контроллерах
- **Валидация**: использование UniquenessValidationException для проверки уникальности
- **Бизнес-правила**: BusinessRuleValidationException для tweet-specific правил

#### Tweet-specific расширения
- **TweetValidationException** extends ValidationException для специфичных случаев
- **ContentValidationException** extends FormatValidationException для валидации контента
- **TweetStatus enum** (ACTIVE, DELETED, HIDDEN) для управления состоянием твитов
- **TweetType enum** (ORIGINAL, RETWEET, REPLY) для классификации типов твитов

#### Обработка ошибок в Service Layer
- **TweetNotFoundException** для отсутствующих твитов
- **LikeNotFoundException** для отсутствующих лайков
- **RetweetNotFoundException** для отсутствующих ретвитов
- **UsersApiException** для ошибок интеграции с users-api
- **TweetApiExceptionHandler** расширяет GlobalExceptionHandler для tweet-specific ошибок
- **RFC 7807 Problem Details** для стандартизированных ответов об ошибках

#### Архитектурные преимущества shared-lib
- **Консистентность**: единообразные паттерны логирования и обработки ошибок
- **Масштабируемость**: AOP-based компоненты с минимальным overhead
- **Безопасность**: автоматическое скрытие чувствительных данных в логах
- **Расширяемость**: возможность добавления tweet-specific компонентов
- **RFC 7807 соответствие**: стандартизированные ответы об ошибках

#### Структура пакетов Controller Layer
- **TweetController.java** - основной контроллер с реализацией TweetApi
- **TweetApi.java** - OpenAPI интерфейс с аннотациями
- **dto/request/** - Request DTOs (CreateTweetRequestDto, UpdateTweetRequestDto, LikeTweetRequestDto)
- **dto/response/** - Response DTOs (TweetResponseDto, LikeResponseDto, RetweetResponseDto)
- **dto/error/** - Error DTOs (ErrorResponseDto, ValidationErrorResponseDto)
- **validation/** - Кастомные валидаторы (UserExistsValidator, NoSelfActionValidator)

#### Зависимости Controller Layer
- **shared/common-lib** - LoggableRequestAspect, система исключений
- **spring-boot-starter-web** - REST API функциональность
- **spring-boot-starter-validation** - Bean Validation
- **springdoc-openapi-starter-webmvc-ui** - OpenAPI/Swagger UI
- **mapstruct** - автоматическая генерация мапперов

#### Структура пакетов Service Layer
- **TweetService.java** - интерфейс сервиса
- **TweetServiceImpl.java** - реализация сервиса
- **client/** - клиенты для внешних сервисов (UsersApiClient, UsersApiClientImpl, UsersApiFallbackService)
- **exception/** - tweet-specific исключения (TweetNotFoundException, LikeNotFoundException, RetweetNotFoundException, UsersApiException)
- **config/** - конфигурации (CircuitBreakerConfig, CacheConfig)

#### Зависимости Service Layer
- **shared/common-lib** - система исключений, валидация
- **spring-boot-starter-data-jpa** - JPA/Hibernate функциональность
- **spring-boot-starter-cache** - кэширование
- **resilience4j-spring-boot2** - Circuit Breaker
- **resilience4j-circuitbreaker** - Circuit Breaker функциональность
- **mapstruct** - автоматическая генерация мапперов

#### Структура пакетов Repository Layer
- **TweetRepository.java** - основной репозиторий для твитов
- **LikeRepository.java** - репозиторий для лайков
- **RetweetRepository.java** - репозиторий для ретвитов
- **impl/TweetRepositoryImpl.java** - кастомная реализация с batch операциями
- **specification/TweetSpecification.java** - спецификации для динамических запросов
- **entity/** - JPA Entities (Tweet.java, Like.java, Retweet.java)

#### Зависимости Repository Layer
- **spring-boot-starter-data-jpa** - Spring Data JPA функциональность
- **postgresql** - PostgreSQL драйвер
- **hibernate-core** - Hibernate ORM
- **hibernate-validator** - Bean Validation
- **spring-boot-starter-validation** - Spring Validation

#### Структура пакетов DTO/Mapper Layer
- **dto/request/** - Request DTOs (CreateTweetRequestDto, UpdateTweetRequestDto, LikeTweetRequestDto, RetweetRequestDto)
- **dto/response/** - Response DTOs (TweetResponseDto, TweetStatsDto, LikeResponseDto, RetweetResponseDto)
- **dto/error/** - Error DTOs (ErrorResponseDto, ErrorInfoDto, ValidationErrorResponseDto, ResponseMetaDto)
- **dto/validation/** - Валидация (ValidationGroups, UserExists, UserExistsValidator, NoSelfAction, NoSelfActionValidator)
- **mapper/** - MapStruct мапперы (TweetMapper, LikeMapper, RetweetMapper)

#### Зависимости DTO/Mapper Layer
- **spring-boot-starter-validation** - Bean Validation функциональность
- **mapstruct** - MapStruct для автоматического маппинга
- **swagger-annotations** - OpenAPI/Swagger аннотации
- **mapstruct-processor** - процессор для генерации мапперов
- **spring-boot-configuration-processor** - процессор конфигурации Spring Boot

### 10.3 Следующие шаги
1. **Реализация миграций** для создания схемы в БД
2. **Создание JPA entities** на основе архитектурной модели
3. **Реализация DTO классов** с валидацией на основе API контрактов
4. **Создание REST контроллеров** с OpenAPI документацией
5. **Настройка интеграции** с users-api и contract testing
6. **Реализация мониторинга** и structured logging
7. **Тестирование производительности** с реальными данными
8. **Адаптация паттернов** users-api для tweet-api
9. **Интеграция shared/common-lib** компонентов в tweet-api
10. **Настройка логирования** с @LoggableRequest на контроллерах
11. **Расширение системы исключений** для tweet-specific случаев
12. **Создание tweet-specific enums** (TweetStatus, TweetType)
13. **Реализация TweetController** с TweetApi интерфейсом
14. **Настройка OpenAPI конфигурации** и Swagger UI
15. **Создание кастомных валидаторов** (@UserExists, @NoSelfAction)
16. **Настройка обработки ошибок** в контроллерах
17. **Реализация TweetService** с бизнес-логикой
18. **Настройка Circuit Breaker** и кэширования
19. **Создание UsersApiClient** для интеграции с users-api
20. **Реализация tweet-specific исключений** и обработчиков ошибок
21. **Создание JPA Entities** (Tweet, Like, Retweet) с индексами
22. **Реализация Repository интерфейсов** с кастомными запросами
23. **Создание TweetSpecification** для динамических запросов
24. **Настройка batch операций** и оптимизации производительности
25. **Создание миграций** для схемы базы данных
26. **Создание Request/Response DTOs** с OpenAPI аннотациями
27. **Реализация MapStruct мапперов** (TweetMapper, LikeMapper, RetweetMapper)
28. **Создание кастомных валидаторов** (@UserExists, @NoSelfAction)
29. **Настройка групп валидации** для разных операций
30. **Создание Error DTOs** для обработки ошибок

---

*Документ создан: 2025-01-27*  
*Версия: 1.0*  
*Статус: Draft*
