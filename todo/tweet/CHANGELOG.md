# Changelog

## 2025-01-27

### 13:30 — step #1 done — Анализ требований для GET /api/v1/tweets/user/{userId} — автор: assistant

Проанализированы требования для эндпоинта получения твитов пользователя с пагинацией:
- Определены входные данные: userId (UUID, path parameter), page (int, optional, default 0), size (int, optional, default 20, max 100), sort (String, optional, default createdAt DESC)
- Определены выходные данные: PagedModel<TweetResponseDto> с метаданными пагинации
- Добавлен раздел non-functional requirements:
  - Производительность: время ответа < 200ms, пропускная способность до 1000 RPS, время выполнения запросов к БД < 100ms
  - Надежность: доступность 99.9%, error rate < 0.1%, recovery time < 5 минут
  - Масштабируемость: поддержка пагинации, эффективная работа с большими объемами данных
  - Безопасность: валидация входных данных, защита от SQL injection, rate limiting
- Определены затронутые стандарты проекта:
  - STANDART_CODE.md (Java 24 Records, MapStruct, Lombok, Bean Validation)
  - STANDART_PROJECT.md (@LoggableRequest, исключения из common-lib)
  - STANDART_TEST.md (Unit и Integration тесты)
  - STANDART_JAVADOC.md (JavaDoc для всех public классов и методов)
  - STANDART_SWAGGER.md (OpenAPI документация)
  - STANDART_README.md (обновление README)
  - STANDART_POSTMAN.md (добавление запроса в Postman коллекцию)
- Определены ключевые компоненты: Repository Layer, Service Layer, Controller Layer, DTO Layer, Mapper Layer
- Определены зависимости: TweetRepository, TweetService, TweetController, TweetApi, TweetMapper, TweetResponseDto, Spring Data JPA Pageable и PagedModel

### 13:45 — step #2 done — Проектирование API и контрактов для GET /api/v1/tweets/user/{userId} — автор: assistant

Спроектирована структура эндпоинта и контракты для получения твитов пользователя с пагинацией:
- Структура эндпоинта: GET /api/v1/tweets/user/{userId} с параметрами пагинации (page, size, sort)
- Параметры пагинации: page (int, default 0), size (int, default 20, max 100), sort (String, default createdAt,DESC)
- Структура ответа: PagedModel<TweetResponseDto> с метаданными пагинации и HATEOAS links
- Правила валидации: валидация UUID для userId, валидация параметров пагинации через Spring Data JPA
- HTTP статусы: 200 OK (успешное получение, включая пустой список), 400 Bad Request (ошибки валидации), 404 Not Found (пользователь не найден, опционально), 500 Internal Server Error
- Контракт метода getUserTweets в TweetApi: полная OpenAPI документация с @Operation, @ApiResponses, @Parameter, примерами для всех статус-кодов
- Интеграция с существующими компонентами:
  - TweetRepository: метод findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc (Derived Query Method)
  - TweetMapper: использование существующего метода toResponseDto
  - TweetService: метод getUserTweets с @Transactional(readOnly = true)
  - TweetController: метод с @LoggableRequest и @PageableDefault
- Примеры запросов/ответов: успешный запрос с данными, пустой список, ошибки валидации
- Создан документ DESIGN_GET_USER_TWEETS.md с детальным проектированием

### 14:00 — step #3 done — Добавление метода в TweetRepository для получения твитов пользователя — автор: assistant

Добавлен Derived Query Method в TweetRepository:
- Метод: `Page<Tweet> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable)`
- Импорты: добавлены `org.springframework.data.domain.Page` и `org.springframework.data.domain.Pageable`
- Функциональность:
  - Фильтрация по userId (UUID)
  - Исключение удаленных твитов (isDeleted = false)
  - Сортировка по createdAt в порядке убывания (новые первыми)
  - Поддержка пагинации через Pageable
- Использование индекса: метод использует существующий индекс `idx_tweets_user_id_created_at` для оптимизации запросов
- Соответствие стандартам: Derived Query Method без JavaDoc (согласно STANDART_CODE.md, раздел 12.2 - self-documenting методы)
- Файл: `services/tweet-api/src/main/java/com/twitter/repository/TweetRepository.java`

### 14:15 — step #4 done — Добавление метода в TweetService интерфейс для получения твитов пользователя — автор: assistant

Добавлен метод getUserTweets в TweetService интерфейс:
- Метод: `Page<TweetResponseDto> getUserTweets(UUID userId, Pageable pageable)`
- Импорты: добавлены `org.springframework.data.domain.Page` и `org.springframework.data.domain.Pageable`
- JavaDoc документация:
  - Описание метода с перечислением всех операций (6 шагов)
  - @param для userId и pageable с описаниями
  - @return с описанием возвращаемого Page с метаданными пагинации
  - @throws для FormatValidationException и BusinessRuleValidationException
  - @author geron и @version 1.0
- Функциональность:
  - Валидация userId
  - Получение твитов через Repository с пагинацией
  - Фильтрация удаленных твитов (isDeleted = false)
  - Сортировка по createdAt DESC (новые первыми)
  - Маппинг Entity → DTO
  - Возврат Page с метаданными пагинации
- Архитектурное решение:
  - Service Layer возвращает `Page<TweetResponseDto>` (Spring Data тип)
  - Controller Layer преобразует `Page` → `PagedModel` (HATEOAS тип)
  - Разделение ответственности: Service работает с данными, Controller добавляет HATEOAS links
- Соответствие стандартам: полная JavaDoc документация согласно STANDART_JAVADOC.md
- Файл: `services/tweet-api/src/main/java/com/twitter/service/TweetService.java`

### 14:30 — step #5 done — Реализация метода getUserTweets в TweetServiceImpl — автор: assistant

Реализован метод getUserTweets в TweetServiceImpl:
- Метод: `Page<TweetResponseDto> getUserTweets(UUID userId, Pageable pageable)`
- Импорты: добавлены `org.springframework.data.domain.Page` и `org.springframework.data.domain.Pageable`
- Реализация:
  - @Transactional(readOnly = true) для read-only транзакции
  - Вызов `tweetRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId, pageable)`
  - Маппинг `Page<Tweet>` → `Page<TweetResponseDto>` через `.map(tweetMapper::toResponseDto)`
  - Возврат `Page<TweetResponseDto>` с метаданными пагинации
- JavaDoc: используется @see TweetService#getUserTweets согласно стандартам
- Функциональность:
  - Автоматическая фильтрация удаленных твитов (isDeleted = false) через Repository метод
  - Автоматическая сортировка по createdAt DESC (новые первыми) через Repository метод
  - Сохранение метаданных пагинации (totalElements, totalPages, number, size) через Page.map()
- Соответствие стандартам: использование стандартного подхода Spring Data с Page.map() (STANDART_CODE.md)
- Файл: `services/tweet-api/src/main/java/com/twitter/service/TweetServiceImpl.java`

### 14:45 — step #6 done — Добавление метода getUserTweets в TweetApi интерфейс — автор: assistant

Добавлен метод getUserTweets в TweetApi интерфейс с полной OpenAPI документацией:
- Метод: `PagedModel<TweetResponseDto> getUserTweets(UUID userId, Pageable pageable)`
- Импорты: добавлены `org.springframework.data.domain.Pageable` и `org.springframework.data.web.PagedModel`
- JavaDoc документация:
  - Описание метода с указанием функциональности
  - @param для userId и pageable с описаниями
  - @return с описанием возвращаемого PagedModel
- OpenAPI документация:
  - @Operation с summary "Get user tweets with pagination" и подробным description
  - @ApiResponses для всех статус-кодов:
    - 200 OK - успешное получение с примером PagedModel (2 твита, метаданные пагинации)
    - 400 Bad Request - 2 типа ошибок (invalid UUID format, invalid pagination parameters)
    - 404 Not Found - пользователь не найден (USER_NOT_EXISTS)
  - @Parameter для обоих параметров:
    - userId с description, required=true, example UUID
    - pageable с description "Pagination parameters (page, size, sorting)", required=false
  - Примеры ответов для всех сценариев в формате RFC 7807 Problem Details для ошибок
- Соответствие стандартам: полная OpenAPI документация согласно STANDART_SWAGGER.md
- Консистентность: следует паттернам из UserApi.findAll() для пагинированных эндпоинтов
- Файл: `services/tweet-api/src/main/java/com/twitter/controller/TweetApi.java`

### 15:00 — step #7 done — Реализация метода getUserTweets в TweetController — автор: assistant

Реализован метод getUserTweets в TweetController:
- Метод: `PagedModel<TweetResponseDto> getUserTweets(UUID userId, Pageable pageable)`
- Импорты: добавлены `org.springframework.data.domain.Page`, `org.springframework.data.domain.Pageable`, `org.springframework.data.domain.Sort`, `org.springframework.data.web.PageableDefault`, `org.springframework.data.web.PagedModel`
- Аннотации:
  - @LoggableRequest для автоматического логирования запросов
  - @GetMapping("/user/{userId}") для HTTP GET запроса
  - @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) на параметре pageable
  - @Override для реализации метода из TweetApi интерфейса
- Реализация:
  - Получение `Page<TweetResponseDto>` из TweetService через `tweetService.getUserTweets(userId, pageable)`
  - Преобразование `Page` → `PagedModel` через `new PagedModel<>(tweets)`
  - Возврат `PagedModel<TweetResponseDto>` с метаданными пагинации и HATEOAS links
- JavaDoc: используется @see TweetApi#getUserTweets согласно стандартам
- Функциональность:
  - Дефолтные значения пагинации: page=0, size=20, sort=createdAt,DESC
  - Автоматическое добавление HATEOAS links (self, first, last, next, prev) через PagedModel
  - Сохранение метаданных пагинации (totalElements, totalPages, number, size)
- Архитектурное решение:
  - Service Layer возвращает `Page<TweetResponseDto>` (Spring Data тип)
  - Controller Layer преобразует `Page` → `PagedModel` (HATEOAS тип)
  - Разделение ответственности: Service работает с данными, Controller добавляет HATEOAS links
- Соответствие стандартам: использование @LoggableRequest, @PageableDefault, разделение ответственности (STANDART_CODE.md, STANDART_PROJECT.md)
- Консистентность: следует паттернам из UserController.findAll() для пагинированных эндпоинтов
- Файл: `services/tweet-api/src/main/java/com/twitter/controller/TweetController.java`

