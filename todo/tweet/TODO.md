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
- [x] (P1) [2025-01-27] #3: Реализация Entity Like — Создать JPA сущность Like с полями id, tweetId, userId, createdAt
  acceptance: "Entity Like создана с учетом STANDART_CODE.md (UUID id, уникальное ограничение на tweetId+userId, @CreationTimestamp)"
  note: "Создана JPA Entity Like в services/tweet-api/src/main/java/com/twitter/entity/Like.java. Реализованы все поля (UUID id с автогенерацией, UUID tweetId, UUID userId, LocalDateTime createdAt с @CreationTimestamp). Добавлено уникальное ограничение на паре (tweetId, userId) через @UniqueConstraint. Реализованы бизнес-методы isByUser() и isForTweet(). Использованы Lombok аннотации (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor). Добавлена полная JavaDoc документация согласно STANDART_JAVADOC.md. Entity соответствует стандартам STANDART_CODE.md."
- [x] (P1) [2025-01-27] #4: Реализация LikeRepository — Создать интерфейс с Derived Query Methods
  acceptance: "LikeRepository создан с методами findByTweetIdAndUserId, existsByTweetIdAndUserId (без JavaDoc для derived methods)"
  note: "Создан LikeRepository в services/tweet-api/src/main/java/com/twitter/repository/LikeRepository.java. Реализованы методы: Optional<Like> findByTweetIdAndUserId(UUID tweetId, UUID userId) и boolean existsByTweetIdAndUserId(UUID tweetId, UUID userId). Методы являются Derived Query Methods Spring Data JPA, поэтому JavaDoc для них не добавлен (согласно STANDART_CODE.md). Добавлен JavaDoc для интерфейса (@author geron, @version 1.0). Repository расширяет JpaRepository<Like, UUID> и помечен аннотацией @Repository."
- [x] (P1) [2025-01-27] #5: Реализация общих DTO — Создать LikeTweetRequestDto и LikeResponseDto как Records
  acceptance: "DTO созданы как Records с валидацией (@NotNull для userId), размещены в правильных пакетах (dto/request, dto/response)"
  note: "Созданы DTO: LikeTweetRequestDto в services/tweet-api/src/main/java/com/twitter/dto/request/LikeTweetRequestDto.java и LikeResponseDto в services/tweet-api/src/main/java/com/twitter/dto/response/LikeResponseDto.java. Оба DTO реализованы как Records (Java 24 feature). LikeTweetRequestDto содержит UUID userId с валидацией @NotNull и @Schema аннотациями. LikeResponseDto содержит UUID id, UUID tweetId, UUID userId, LocalDateTime createdAt с @JsonFormat для форматирования даты. Оба DTO имеют @Builder аннотацию (Lombok), полную JavaDoc документацию (@author geron, @version 1.0) и @Schema аннотации для Swagger документации. DTO соответствуют стандартам STANDART_CODE.md (Records для DTO, Bean Validation, OpenAPI аннотации)."
- [x] (P1) [2025-01-27] #6: Реализация Mapper интерфейса — Добавить методы маппинга для лайков
  acceptance: "Методы маппинга добавлены в TweetMapper или создан LikeMapper (toLike, toLikeResponseDto)"
  note: "Создан отдельный LikeMapper в services/tweet-api/src/main/java/com/twitter/mapper/LikeMapper.java с методами: Like toLike(LikeTweetRequestDto requestDto, UUID tweetId) и LikeResponseDto toLikeResponseDto(Like like). Метод toLike принимает два параметра (requestDto и tweetId) и маппит их в Like entity с игнорированием служебных полей (id, createdAt). Используются @Mapping аннотации для явного указания источников (tweetId из параметра, userId из requestDto.userId). Метод toLikeResponseDto выполняет простой маппинг всех полей из Like entity в LikeResponseDto. LikeMapper имеет полную JavaDoc документацию (@author geron, @version 1.0, @param, @return). Создание отдельного маппера соответствует принципу разделения ответственности (Single Responsibility Principle) и паттерну, используемому в проекте (аналогично FollowMapper в follower-api). Методы соответствуют стандартам STANDART_CODE.md (MapStruct для маппинга, игнорирование служебных полей)."
- [x] (P1) [2025-01-27] #7: Реализация Validator интерфейса — Добавить методы валидации для лайка
  acceptance: "Методы validateForLike добавлены в TweetValidator interface и implementation"
  note: "Создан отдельный LikeValidator в services/tweet-api/src/main/java/com/twitter/validation/LikeValidator.java и LikeValidatorImpl в services/tweet-api/src/main/java/com/twitter/validation/LikeValidatorImpl.java. Метод validateForLike(UUID tweetId, LikeTweetRequestDto requestDto) выполняет полную валидацию для операции лайка: проверка tweetId (не null), проверка существования твита (findByIdAndIsDeletedFalse), проверка requestDto (не null), проверка userId (не null), проверка существования пользователя (userGateway.existsUser), проверка самолайка (validateNoSelfLike), проверка дублирования (validateUniqueness). Для дублирования используется UniquenessValidationException (409 Conflict), для остальных ошибок - BusinessRuleValidationException. LikeValidatorImpl имеет зависимости: TweetRepository, LikeRepository, UserGateway. Валидация разделена на приватные методы (validateUserExists, validateNoSelfLike, validateUniqueness) для лучшей читаемости. Создание отдельного валидатора соответствует принципу разделения ответственности (Single Responsibility Principle) и паттерну, используемому в проекте (аналогично FollowValidator в follower-api). Метод validateForLike удален из TweetValidator и TweetValidatorImpl. LikeValidator имеет полную JavaDoc документацию (@author geron, @version 1.0, @param, @throws). Валидатор соответствует стандартам STANDART_CODE.md (валидация через отдельный компонент, использование исключений из common-lib, логирование через @Slf4j)."
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

