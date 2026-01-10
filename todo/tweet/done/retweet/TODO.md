# TODO: Ретвитнуть с опциональным комментарием

## Meta
- project: twitter-tweet-api
- updated: 2025-01-27
- changelog: todo/tweet/retweet/CHANGELOG.md
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
- [x] (P1) [2025-01-27] #1: Анализ требований — Определить структуру Entity, DTO, бизнес-правила, SQL скрипт
  acceptance: "Понять вход/выход, non-functional requirements, определить затронутые стандарты, определить структуру данных"
  note: "Создан документ analysis-requirements.md с полным анализом требований. Определены структуры Entity, DTO, бизнес-правила, SQL скрипт, затронутые стандарты, вход/выход, non-functional requirements. Выявлен риск отсутствия поля retweets_count в таблице tweets."
- [x] (P1) [2025-01-27] #2: Проектирование API и контрактов — Определить контракт эндпоинта POST /api/v1/tweets/{tweetId}/retweet
  acceptance: "OpenAPI схема для эндпоинта, DTO структура, Entity структура, определение всех компонентов"
  note: "Создан документ design-api-contracts.md с полным проектированием API. Определены OpenAPI интерфейс RetweetApi с аннотациями, структура DTO (RetweetRequestDto, RetweetResponseDto) с @Schema, структура Entity Retweet с бизнес-методами, все компоненты (Repository, Mapper, Validator, Service, Controller), HTTP статус-коды, исключения, примеры для всех сценариев."

### Реализация инфраструктуры и конфигов
- [x] (P1) [2025-01-27] #3: SQL скрипт для таблицы tweet_retweets — Создать script_5_tweet_retweets.sql
  acceptance: "SQL скрипт создан с таблицей, уникальным ограничением на (tweet_id, user_id), foreign keys, индексами"
  note: "Создан SQL скрипт script_5_tweet_retweets.sql с таблицей tweet_retweets, полями (id, tweet_id, user_id, comment, created_at), уникальным ограничением на (tweet_id, user_id), foreign keys на tweets(id) и users(id), индексами для оптимизации. Примечание: требуется добавить поле retweets_count в таблицу tweets (отдельный скрипт или миграция)."
- [x] (P1) [2025-01-27] #4: Реализация Entity Retweet — Создать JPA сущность
  acceptance: "Entity создана с полями id, tweetId, userId, comment, createdAt, бизнес-методами isByUser(), isForTweet(), hasComment(), с учетом STANDART_CODE.md"
  note: "Создана JPA сущность Retweet в services/tweet-api/src/main/java/com/twitter/entity/Retweet.java с полями id, tweetId, userId, comment (nullable), createdAt, уникальным ограничением на (tweet_id, user_id), бизнес-методами isByUser(), isForTweet(), hasComment(). Entity соответствует стандартам проекта и структуре таблицы tweet_retweets."
- [x] (P1) [2025-01-27] #5: Реализация Repository RetweetRepository — Создать Spring Data JPA репозиторий
  acceptance: "Repository создан с Derived Query Methods (findByTweetIdAndUserId, existsByTweetIdAndUserId) без JavaDoc"
  note: "Создан Spring Data JPA репозиторий RetweetRepository в services/tweet-api/src/main/java/com/twitter/repository/RetweetRepository.java, расширяющий JpaRepository<Retweet, UUID>. Реализованы Derived Query Methods: findByTweetIdAndUserId(UUID, UUID) и existsByTweetIdAndUserId(UUID, UUID) без JavaDoc (согласно стандартам)."
- [x] (P1) [2025-01-27] #6: Реализация DTO для ретвита — Создать RetweetRequestDto и RetweetResponseDto
  acceptance: "DTO созданы как Records с валидацией (@NotNull для userId, @Size(max=280) для comment), @Schema аннотациями, размещены в правильных пакетах"
  note: "Созданы DTO: RetweetRequestDto (com.twitter.dto.request) с полями userId (@NotNull) и comment (@Size(max=280), nullable), RetweetResponseDto (com.twitter.dto.response) с полями id, tweetId, userId, comment (nullable), createdAt. Оба DTO созданы как Records с @Builder, полными @Schema аннотациями, JavaDoc и примерами."
- [x] (P1) [2026-01-05 16:40] #7: Реализация Mapper интерфейса RetweetMapper — Создать MapStruct интерфейс
  acceptance: "Mapper интерфейс создан с методами toRetweet(RetweetRequestDto, UUID tweetId) и toRetweetResponseDto(Retweet)"
  note: "Создан MapStruct интерфейс RetweetMapper в services/tweet-api/src/main/java/com/twitter/mapper/RetweetMapper.java с методами toRetweet(RetweetRequestDto, UUID tweetId) и toRetweetResponseDto(Retweet). Метод toRetweet игнорирует service-managed поля (id, createdAt), маппит tweetId из параметра, userId и comment из requestDto. Метод toRetweetResponseDto преобразует Retweet entity в RetweetResponseDto. Интерфейс создан по аналогии с LikeMapper, соответствует стандартам проекта."
- [x] (P1) [2026-01-05 16:40] #8: Реализация Validator интерфейса RetweetValidator — Создать интерфейс
  acceptance: "Validator interface создан с методом validateForRetweet(UUID tweetId, RetweetRequestDto requestDto)"
  note: "Создан интерфейс RetweetValidator в services/tweet-api/src/main/java/com/twitter/validation/RetweetValidator.java с методом validateForRetweet(UUID tweetId, RetweetRequestDto requestDto). Метод документирован с описанием всех проверок: существование твита, существование пользователя, запрет self-retweet, проверка уникальности, валидация комментария. Определены исключения: BusinessRuleValidationException, UniquenessValidationException, FormatValidationException. Интерфейс создан по аналогии с LikeValidator, соответствует стандартам проекта."
- [x] (P1) [2026-01-05 16:40] #9: Реализация Service интерфейса RetweetService — Создать интерфейс
  acceptance: "Service interface создан с методом retweetTweet(UUID tweetId, RetweetRequestDto requestDto)"
  note: "Создан интерфейс RetweetService в services/tweet-api/src/main/java/com/twitter/service/RetweetService.java с методом retweetTweet(UUID tweetId, RetweetRequestDto requestDto). Метод документирован с описанием всех шагов бизнес-логики: валидация, создание Entity, сохранение в БД, обновление счетчика retweetsCount, маппинг в Response DTO. Определены исключения: BusinessRuleValidationException, UniquenessValidationException, FormatValidationException. Интерфейс создан по аналогии с LikeService, соответствует стандартам проекта."

### Эндпоинт: POST /api/v1/tweets/{tweetId}/retweet
- [x] (P1) [2026-01-05 16:40] #10: Validator реализация RetweetValidatorImpl — Реализовать все проверки валидации
  acceptance: "Методы валидации реализованы в RetweetValidatorImpl для эндпоинта: существование твита, существование пользователя, запрет self-retweet, проверка дублирования, валидация комментария"
  note: "Создана реализация RetweetValidatorImpl в services/tweet-api/src/main/java/com/twitter/validation/RetweetValidatorImpl.java. Реализован метод validateForRetweet с проверками: tweetId не null, существование твита (не удален), requestDto не null, userId не null, существование пользователя через UserGateway, запрет self-retweet, проверка уникальности ретвита, валидация комментария (null разрешен, но если не null - не пустая строка и не более 280 символов). Добавлены приватные методы: validateUserExists, validateNoSelfRetweet, validateUniqueness, validateComment. Все методы документированы JavaDoc. Реализация создана по аналогии с LikeValidatorImpl, соответствует стандартам проекта."
- [x] (P1) [2026-01-05 16:40] #11: Service реализация RetweetServiceImpl — Реализовать бизнес-логику
  acceptance: "Методы добавлены в RetweetServiceImpl для эндпоинта, используют @Transactional, вызывают валидацию, обновляют счетчик retweetsCount"
  note: "Создана реализация RetweetServiceImpl в services/tweet-api/src/main/java/com/twitter/service/RetweetServiceImpl.java. Реализован метод retweetTweet с бизнес-логикой: валидация через RetweetValidator, маппинг DTO в Entity через RetweetMapper, сохранение ретвита в БД, обновление счетчика retweetsCount в Tweet entity через incrementRetweetsCount(), маппинг Entity в Response DTO, возврат Response DTO. Метод использует @Transactional для атомарности операции. Добавлен полный JavaDoc. Также добавлено поле retweetsCount и метод incrementRetweetsCount() в Tweet entity для поддержки функционала. Реализация создана по аналогии с LikeServiceImpl, соответствует стандартам проекта."
- [x] (P1) [2026-01-05 16:40] #12: OpenAPI интерфейс RetweetApi — Создать интерфейс с аннотациями
  acceptance: "Метод добавлен в RetweetApi интерфейс с @Operation, @ApiResponses, @ExampleObject для всех сценариев (201, 400, 404, 409)"
  note: "Создан интерфейс RetweetApi в services/tweet-api/src/main/java/com/twitter/controller/RetweetApi.java с методом retweetTweet. Добавлены полные OpenAPI аннотации: @Tag для Retweet Management, @Operation с описанием, @ApiResponses с примерами для всех статус-кодов: 201 (с комментарием и без комментария), 400 (валидация userId, комментария, UUID), 404 (твит не найден, пользователь не найден), 409 (self-retweet, дублирование). Все примеры используют RFC 7807 Problem Details формат. Добавлен полный JavaDoc для интерфейса и метода. Интерфейс создан по аналогии с LikeApi, соответствует стандартам проекта и требованиям из design-api-contracts.md."
- [x] (P1) [2026-01-05 16:40] #13: Controller RetweetController — Реализовать REST эндпоинт
  acceptance: "Метод добавлен в RetweetController с @PostMapping("/{tweetId}/retweet"), @LoggableRequest, @Valid, HttpStatus.CREATED"
  note: "Создан контроллер RetweetController в services/tweet-api/src/main/java/com/twitter/controller/RetweetController.java. Контроллер реализует интерфейс RetweetApi. Реализован метод retweetTweet с аннотациями: @LoggableRequest для логирования запросов, @PostMapping(\"/{tweetId}/retweet\") для маппинга эндпоинта, @PathVariable для tweetId, @RequestBody @Valid для retweetRequest, @Override для реализации интерфейса. Метод возвращает ResponseEntity.status(HttpStatus.CREATED).body(createdRetweet). Добавлен полный JavaDoc с @see ссылкой на интерфейс. Использованы аннотации: @Slf4j, @RestController, @RequestMapping(\"/api/v1/tweets\"), @RequiredArgsConstructor. Контроллер создан по аналогии с LikeController, соответствует стандартам проекта."
- [x] (P1) [2026-01-05 16:50] #14: JavaDoc для эндпоинта — Добавить JavaDoc для всех методов
  acceptance: "JavaDoc добавлен для всех методов эндпоинта с @author geron, @version 1.0, @param, @return, @throws"
  note: "Добавлен полный JavaDoc для метода retweetTweet в RetweetController с @author geron, @version 1.0, @param для всех параметров (tweetId, retweetRequest), @return с описанием возвращаемого значения, @throws для всех исключений (BusinessRuleValidationException, UniquenessValidationException, FormatValidationException). JavaDoc включает подробное описание метода, его поведения и всех возможных исключений. Также добавлена ссылка @see на интерфейс RetweetApi. JavaDoc соответствует стандартам проекта (STANDART_JAVADOC.md)."
- [x] (P1) [2026-01-05 17:00] #15: Unit тесты для эндпоинта — Создать тесты для Service, Validator, Mapper
  acceptance: "Unit тесты для RetweetServiceImpl, RetweetValidatorImpl, RetweetMapper с учетом STANDART_TEST.md, все успешные и ошибочные сценарии"
  note: "Созданы unit тесты для всех компонентов: RetweetServiceImplTest (тесты для retweetTweet: успешные сценарии с комментарием и без, проверка вызовов зависимостей, проверка инкремента счетчика, ошибочные сценарии), RetweetValidatorImplTest (тесты для validateForRetweet: успешные сценарии, проверка всех валидаций - null tweetId, tweet не найден, null requestDto, null userId, пользователь не существует, self-retweet, дублирование, валидация комментария - пустая строка, только пробелы, превышение длины, точная длина), RetweetMapperTest (тесты для toRetweet и toRetweetResponseDto: успешные сценарии с комментарием и без, проверка игнорирования полей id и createdAt, проверка null значений). Все тесты следуют стандартам проекта (STANDART_TEST.md), используют @Nested для группировки, паттерн methodName_WhenCondition_ShouldExpectedResult, AssertJ для assertions, Mockito для моков. Покрыты все успешные и ошибочные сценарии."
- [x] (P2) [2026-01-05 17:10] #16: Integration тесты для эндпоинта — Создать тесты с MockMvc
  acceptance: "Integration тесты для RetweetController с MockMvc, все статус-коды проверены (201, 400, 404, 409)"
  note: "Создан интеграционный тест RetweetControllerTest в services/tweet-api/src/test/java/com/twitter/controller/RetweetControllerTest.java. Реализованы тесты для всех статус-кодов: 201 (успешные сценарии с комментарием и без), 400 (валидация: null userId, отсутствие тела запроса, пустой комментарий, превышение максимальной длины комментария, комментарий точной максимальной длины), 409 (твит не найден, пользователь не найден, self-retweet запрещен, дублирование ретвита, ошибка users-api). Добавлены тесты для проверки инкремента счетчика retweetsCount. Все тесты используют @Nested для группировки, паттерн methodName_WhenCondition_ShouldExpectedResult, BaseIntegrationTest для настройки окружения, MockMvc для тестирования REST эндпоинтов, WireMock для мокирования users-api. Тесты следуют стандартам проекта (STANDART_TEST.md) и созданы по аналогии с LikeControllerTest."
- [x] (P1) [2026-01-05 17:20] #17: Swagger документация для эндпоинта — Убедиться в полноте документации
  acceptance: "OpenAPI документация для эндпоинта полная с @ExampleObject для всех сценариев, все DTO имеют @Schema аннотации"
  note: "Проверена и исправлена Swagger документация для эндпоинта POST /api/v1/tweets/{tweetId}/retweet. Исправлены статус-коды в RetweetApi: все BusinessRuleValidationException (TWEET_NOT_FOUND, USER_NOT_EXISTS, SELF_RETWEET_NOT_ALLOWED) теперь документированы с правильным статус-кодом 409 (вместо 404), что соответствует реальному поведению GlobalExceptionHandler. Все @ExampleObject присутствуют для всех сценариев: 201 (с комментарием и без), 400 (валидация userId, комментария, UUID), 409 (твит не найден, пользователь не найден, self-retweet, дублирование). Все DTO (RetweetRequestDto, RetweetResponseDto) имеют полные @Schema аннотации на уровне класса и полей. Документация соответствует стандартам проекта (STANDART_SWAGGER.md)."

### Финальная инфраструктура
- [x] (P2) [2026-01-05 17:30] #18: Обновление README.md — Обновить документацию
  acceptance: "README обновлен с учетом STANDART_README.md, все новые эндпоинты документированы на русском языке"
  note: "Обновлен README.md для tweet-api с информацией о новом эндпоинте POST /api/v1/tweets/{tweetId}/retweet. Добавлено в основные возможности: ретвит твитов с опциональным комментарием. Обновлена структура пакетов: добавлены RetweetApi, RetweetController, RetweetService, RetweetValidator, RetweetRepository, RetweetMapper, RetweetRequestDto, RetweetResponseDto, Retweet entity. Добавлен эндпоинт в таблицу эндпоинтов. Добавлено детальное описание эндпоинта ретвита с валидацией, бизнес-правилами, примерами успешных и ошибочных ответов. Добавлено описание RetweetService в раздел Бизнес-логика с методами и ключевыми бизнес-правилами. Добавлено описание RetweetValidator в раздел Слой валидации с многоэтапной валидацией. Добавлена информация о таблице tweet_retweets в раздел Работа с базой данных с полями, ограничениями, индексами. Обновлена таблица tweets: добавлено поле retweets_count. Добавлен пример использования ретвита в раздел Примеры использования с curl командами (с комментарием и без). Документация соответствует стандартам проекта (STANDART_README.md) и написана на русском языке."
- [x] (P2) [2026-01-05 17:40] #19: Обновление Postman коллекции — Добавить запрос retweet tweet
  acceptance: "Добавлен запрос retweet tweet с примерами ответов для всех эндпоинтов, обновлены переменные окружения"
  note: "Добавлен запрос 'retweet tweet' в Postman коллекцию twitter-tweet-api.postman_collection.json после запроса 'remove like'. Запрос включает: метод POST, путь /api/v1/tweets/{{tweetId}}/retweet, тело запроса с userId и опциональным comment (null по умолчанию), полное описание с валидацией и бизнес-правилами. Добавлены примеры ответов для всех сценариев: 201 Created (с комментарием и без), 400 Bad Request (null userId, missing body, empty comment, comment too long, invalid UUID), 409 Conflict (tweet not found, user not exists, self-retweet, duplicate retweet). Обновлено описание коллекции: добавлена информация о ретвитах в список возможностей API. Все примеры ответов используют правильные Content-Type (application/json для успешных, application/problem+json для ошибок) и следуют RFC 7807 Problem Details формату. Запрос соответствует стандартам проекта (STANDART_POSTMAN.md) и создан по аналогии с запросом 'like tweet'."
- [x] (P1) [2026-01-05 17:50] #20: Проверка соответствия стандартам — Проверить все стандарты
  acceptance: "Все стандарты проверены, код соответствует требованиям STANDART_CODE.md, STANDART_PROJECT.md, STANDART_TEST.md, STANDART_JAVADOC.md, STANDART_SWAGGER.md, STANDART_README.md, STANDART_POSTMAN.md"
  note: "Проведена полная проверка соответствия всех стандартов проекта для функционала ретвита. Проверены все компоненты: Entity (Retweet), Repository (RetweetRepository), DTO (RetweetRequestDto, RetweetResponseDto), Mapper (RetweetMapper), Validator (RetweetValidator, RetweetValidatorImpl), Service (RetweetService, RetweetServiceImpl), Controller (RetweetController, RetweetApi), тесты (RetweetServiceImplTest, RetweetValidatorImplTest, RetweetMapperTest, RetweetControllerTest). Все компоненты соответствуют стандартам: STANDART_CODE.md (Records для DTO, Lombok, MapStruct, валидация, структура пакетов), STANDART_PROJECT.md (@LoggableRequest, правильные исключения из common-lib), STANDART_TEST.md (структура тестов, именование methodName_WhenCondition_ShouldExpectedResult, @Nested, AssertJ, Mockito), STANDART_JAVADOC.md (полный JavaDoc для всех публичных классов и методов, @author geron, @version 1.0, отсутствие JavaDoc для Derived Query Methods в Repository), STANDART_SWAGGER.md (полные OpenAPI аннотации, @ExampleObject для всех сценариев, @Schema для DTO), STANDART_README.md (обновлен в шаге #18), STANDART_POSTMAN.md (обновлен в шаге #19). Все проверки пройдены успешно."

## Assumptions
- Таблица tweets уже существует с полем retweets_count
- Пользователи проверяются через UserGateway (интеграция с users-api)
- Твиты проверяются через TweetRepository
- SQL скрипт будет выполнен до запуска приложения
- Комментарий опционален, но если указан, должен быть валидным (1-280 символов)
- Null комментарий разрешен, пустая строка - нет

## Risks
- **Технические**: Необходимость обновления счетчика retweetsCount в таблице tweets (нужно проверить, есть ли метод incrementRetweetsCount в TweetRepository)
- **Бизнес-логика**: Валидация комментария должна учитывать, что null и пустая строка - разные случаи (null разрешен, пустая строка - нет)
- **Производительность**: Уникальное ограничение на паре (tweet_id, user_id) должно быть проиндексировано

## Metrics & Success Criteria
- Все unit тесты проходят
- Все integration тесты проходят
- Покрытие кода > 80% для новых компонентов
- Swagger документация полная и корректная
- Postman коллекция обновлена с примерами
- README обновлен на русском языке
- Код соответствует всем стандартам проекта

## Notes
- Реализация по аналогии с функционалом Like (LikeController, LikeService, LikeValidator)
- Эндпоинт: POST /api/v1/tweets/{tweetId}/retweet
- Опциональный комментарий до 280 символов
- Бизнес-правила: запрет self-retweet, уникальность на паре (tweetId, userId), валидация комментария
- Ссылки на стандарты:
  - [STANDART_CODE.md](../../standards/STANDART_CODE.md)
  - [STANDART_PROJECT.md](../../standards/STANDART_PROJECT.md)
  - [STANDART_TEST.md](../../standards/STANDART_TEST.md)
  - [STANDART_JAVADOC.md](../../standards/STANDART_JAVADOC.md)
  - [STANDART_SWAGGER.md](../../standards/STANDART_SWAGGER.md)
  - [STANDART_README.md](../../standards/STANDART_README.md)
  - [STANDART_POSTMAN.md](../../standards/STANDART_POSTMAN.md)