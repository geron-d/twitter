# TODO: Реализация эндпоинта "Лайкнуть твит"

## Meta
- project: twitter-microservices
- updated: 2025-01-27
- changelog: todo/CHANGELOG.md
- standards:
  - STANDART_CODE.md
  - STANDART_PROJECT.md
  - STANDART_TEST.md
  - STANDART_JAVADOC.md
  - STANDART_SWAGGER.md
  - STANDART_README.md
  - STANDART_POSTMAN.md

## Tasks

### Анализ и проектирование
- [x] (P1) [2025-01-27] #1: Анализ требований — Определить входные/выходные данные, бизнес-правила, затронутые стандарты
  acceptance: "Понять вход/выход, бизнес-правила (уникальность, запрет самолайка), определить затронутые стандарты"
  note: "Выполнен полный анализ требований для эндпоинта POST /api/v1/tweets/{tweetId}/like. Определены входные/выходные данные (tweetId, userId, LikeResponseDto), бизнес-правила (уникальность, запрет самолайка, существование твита/пользователя, атомарность, обновление счетчика), затронутые стандарты (STANDART_CODE, STANDART_PROJECT, STANDART_TEST, STANDART_JAVADOC, STANDART_SWAGGER, STANDART_README, STANDART_POSTMAN). Определены компоненты для реализации (Entity Like, LikeRepository, DTO, Mapper, Validator, Service, Controller). Определены HTTP статус-коды (201, 400, 404, 409, 500). Создан документ: todo/tweet/done/like/analysis-requirements.md"
- [x] (P1) [2025-01-27] #2: Проектирование API и контрактов — Определить структуру DTO, Entity, валидацию
  acceptance: "OpenAPI схема для эндпоинта, DTO структура (LikeTweetRequestDto, LikeResponseDto), Entity структура (Like), определение общих и специфичных компонентов"
  note: "Выполнено проектирование API и контрактов для эндпоинта POST /api/v1/tweets/{tweetId}/like. Определена OpenAPI схема с полной документацией всех сценариев (201, 400, 404, 409). Определена структура DTO: LikeTweetRequestDto (Record с userId, @NotNull, @Schema), LikeResponseDto (Record с id, tweetId, userId, createdAt, @Schema). Определена структура Entity Like (UUID id, tweetId, userId, createdAt, уникальное ограничение на паре tweetId+userId, бизнес-методы isByUser, isForTweet). Определены общие компоненты (переиспользование UserGateway, TweetRepository, TweetValidator, TweetMapper) и специфичные компоненты (новые Entity, Repository, DTO, методы валидации/сервиса/контроллера). Определена структура базы данных (таблица tweet_likes, обновление таблицы tweets с likesCount). Создан документ: todo/tweet/done/like/design-api-contracts.md"

### Реализация инфраструктуры и конфигов
- [ ] (P1) #3: Реализация Entity Like — Создать JPA сущность Like с полями id, tweetId, userId, createdAt
  acceptance: "Entity Like создана с учетом STANDART_CODE.md (UUID id, уникальное ограничение на tweetId+userId, @CreationTimestamp)"
- [ ] (P1) #4: Реализация LikeRepository — Создать интерфейс с Derived Query Methods
  acceptance: "LikeRepository создан с методами findByTweetIdAndUserId, existsByTweetIdAndUserId (без JavaDoc для derived methods)"
- [ ] (P1) #5: Реализация общих DTO — Создать LikeTweetRequestDto и LikeResponseDto как Records
  acceptance: "DTO созданы как Records с валидацией (@NotNull для userId), размещены в правильных пакетах (dto/request, dto/response)"
- [ ] (P1) #6: Реализация Mapper интерфейса — Добавить методы маппинга для лайков
  acceptance: "Методы маппинга добавлены в TweetMapper или создан LikeMapper (toLike, toLikeResponseDto)"
- [ ] (P1) #7: Реализация Validator интерфейса — Добавить методы валидации для лайка
  acceptance: "Методы validateForLike добавлены в TweetValidator interface и implementation"
- [ ] (P1) #8: Реализация Service интерфейса — Добавить метод likeTweet в TweetService
  acceptance: "Метод likeTweet добавлен в TweetService interface и implementation, использует @Transactional"
- [ ] (P2) #9: Обновление Tweet Entity — Добавить поле likesCount и метод incrementLikesCount
  acceptance: "Tweet Entity обновлена с полем likesCount (Integer, default 0) и методом incrementLikesCount()"

### Эндпоинт: POST /api/v1/tweets/{tweetId}/like
- [ ] (P1) #10: DTO для эндпоинта — Создать LikeTweetRequestDto и LikeResponseDto
  acceptance: "Request/Response DTO созданы как Records с валидацией и @Schema аннотациями"
- [ ] (P1) #11: Mapper методы для эндпоинта — Добавить методы маппинга в Mapper
  acceptance: "Методы маппинга добавлены в Mapper интерфейс для лайка (toLike, toLikeResponseDto)"
- [ ] (P1) #12: Validator методы для эндпоинта — Добавить валидацию лайка
  acceptance: "Методы валидации добавлены в Validator interface и implementation (проверка существования твита, пользователя, самолайка, дублирования)"
- [ ] (P1) #13: Service методы для эндпоинта — Реализовать бизнес-логику лайка
  acceptance: "Метод likeTweet добавлен в Service interface и implementation, использует @Transactional, вызывает валидацию, создает лайк, обновляет счетчик"
- [ ] (P1) #14: Controller метод для эндпоинта — Добавить метод в TweetApi и TweetController
  acceptance: "Метод добавлен в TweetApi интерфейс с OpenAPI аннотациями и в TweetController с @LoggableRequest"
- [ ] (P1) #15: JavaDoc для эндпоинта — Добавить JavaDoc для всех методов
  acceptance: "JavaDoc добавлен для всех методов эндпоинта с @author geron, @version 1.0, @param, @return, @throws"
- [ ] (P1) #16: Unit тесты для эндпоинта — Создать unit тесты для Service, Validator, Mapper
  acceptance: "Unit тесты для Service, Validator, Mapper методов с учетом STANDART_TEST.md (naming pattern, @Nested, AssertJ)"
- [ ] (P2) #17: Integration тесты для эндпоинта — Создать integration тесты с MockMvc
  acceptance: "Integration тесты для эндпоинта с MockMvc, все статус-коды проверены (201, 400, 404, 409)"
- [ ] (P1) #18: Swagger документация для эндпоинта — Добавить OpenAPI аннотации
  acceptance: "OpenAPI документация для эндпоинта полная с @ExampleObject для всех сценариев (успех, ошибки валидации, конфликты)"

### Финальная инфраструктура
- [ ] (P2) #19: Обновление README.md — Обновить документацию сервиса
  acceptance: "README обновлен с учетом STANDART_README.md, эндпоинт документирован в разделе REST API и Примеры использования"
- [ ] (P2) #20: Обновление Postman коллекции — Добавить запрос в коллекцию
  acceptance: "Добавлен запрос 'like tweet' с примерами ответов для всех сценариев (201, 400, 404, 409), обновлены переменные окружения"
- [ ] (P1) #21: Проверка соответствия стандартам — Проверить все стандарты
  acceptance: "Все стандарты проверены, код соответствует требованиям (STANDART_CODE, STANDART_PROJECT, STANDART_TEST, STANDART_JAVADOC, STANDART_SWAGGER)"

## Assumptions
- Пользователь идентифицируется через `userId` в request body (не через аутентификацию)
- Твит должен существовать и не быть удаленным (soft delete)
- Уникальность лайка обеспечивается на уровне БД (UNIQUE constraint на tweetId+userId)
- Счетчик `likesCount` обновляется синхронно при создании лайка
- При попытке повторного лайка возвращается ошибка 409 Conflict

## Risks
- **Технические риски**: Race condition при одновременных лайках (митигация: уникальное ограничение в БД, транзакция)
- **Производительность**: Обновление счетчика при каждом лайке (митигация: денормализация для быстрых запросов)
- **Зависимости**: Интеграция с users-api для проверки существования пользователя (митигация: Circuit Breaker, fallback)

## Metrics & Success Criteria
- Все тесты проходят (unit и integration)
- Покрытие кода > 80% для новых компонентов
- Эндпоинт возвращает правильные HTTP статус-коды
- Валидация работает корректно (самолайк, дублирование, несуществующий твит)
- OpenAPI документация полная и корректная
- README обновлен с примерами использования

