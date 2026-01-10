# TODO: Реализация эндпоинта GET /api/v1/tweets/{tweetId}/retweets

## Meta
- project: twitter-tweet-api
- updated: 2025-01-27
- changelog: todo/tweet/CHANGELOG.md
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
- [x] (P1) [2025-01-27] #1: Анализ требований — Анализ существующего кода GET /api/v1/tweets/{tweetId}/likes, определение структуры эндпоинта, проектирование API контракта.
  acceptance: "Понять вход/выход, определить затронутые стандарты, определить структуру эндпоинта"
  note: "Анализ выполнен при создании плана. Изучена реализация эндпоинта getLikesByTweetId, определены все компоненты и структура данных."

### Реализация инфраструктуры и конфигов
- [x] (P1) [2025-01-27] #2: Обновление RetweetRepository — Добавить метод findByTweetIdOrderByCreatedAtDesc для получения ретвитов с пагинацией.
  acceptance: "Метод findByTweetIdOrderByCreatedAtDesc(UUID tweetId, Pageable pageable) добавлен в RetweetRepository (Derived Query Method, JavaDoc не требуется)"
  note: "Добавлен метод findByTweetIdOrderByCreatedAtDesc в RetweetRepository. Добавлены импорты Page и Pageable. Метод следует паттерну из LikeRepository. Derived Query Method - JavaDoc не требуется согласно STANDART_JAVADOC.md"
  commit: "services/tweet-api/src/main/java/com/twitter/repository/RetweetRepository.java"
- [x] (P1) [2025-01-27] #3: Обновление RetweetValidator — Добавить метод validateTweetExists для валидации существования твита.
  acceptance: "Метод validateTweetExists(UUID tweetId) добавлен в RetweetValidator интерфейс и реализован в RetweetValidatorImpl"
  note: "Добавлен метод validateTweetExists в RetweetValidator интерфейс с полным JavaDoc. Реализован в RetweetValidatorImpl с проверкой на null и существование твита (не удален). Метод следует паттерну из LikeValidatorImpl.validateTweetExists."
  commit: "services/tweet-api/src/main/java/com/twitter/validation/RetweetValidator.java, services/tweet-api/src/main/java/com/twitter/validation/RetweetValidatorImpl.java"

### Эндпоинт: GET /api/v1/tweets/{tweetId}/retweets
- [x] (P1) [2025-01-27] #4: Service метод для эндпоинта — Добавить метод getRetweetsByTweetId в RetweetService интерфейс и реализацию.
  acceptance: "Метод добавлен в RetweetService интерфейс с JavaDoc, реализован в RetweetServiceImpl с валидацией существования твита, использует @Transactional(readOnly = true)"
  note: "Добавлен метод getRetweetsByTweetId в RetweetService интерфейс с полным JavaDoc. Реализован в RetweetServiceImpl с валидацией существования твита через retweetValidator.validateTweetExists. Использует @Transactional(readOnly = true). Добавлены импорты Page и Pageable. Метод использует retweetRepository.findByTweetIdOrderByCreatedAtDesc и retweetMapper.toRetweetResponseDto для маппинга."
  commit: "services/tweet-api/src/main/java/com/twitter/service/RetweetService.java, services/tweet-api/src/main/java/com/twitter/service/RetweetServiceImpl.java"
- [x] (P1) [2025-01-27] #5: OpenAPI интерфейс для эндпоинта — Добавить метод getRetweetsByTweetId в RetweetApi интерфейс с OpenAPI аннотациями.
  acceptance: "Метод добавлен в RetweetApi интерфейс с OpenAPI аннотациями (@Operation, @ApiResponses, @Parameter)"
  note: "Добавлен метод getRetweetsByTweetId в RetweetApi интерфейс с полной OpenAPI документацией. Метод возвращает PagedModel<RetweetResponseDto>. Добавлены @Operation с summary и description, @ApiResponses с примерами для 200 OK (Paginated Retweets и Empty Retweets List), 400 (Invalid Pagination Error и Invalid UUID Format Error), 409 (Tweet Not Found Error). Добавлены @Parameter для tweetId и pageable. Добавлены импорты PagedModel и Pageable. Следует паттерну из LikeApi.getLikesByTweetId."
  commit: "services/tweet-api/src/main/java/com/twitter/controller/RetweetApi.java"
- [x] (P1) [2025-01-27] #6: Controller реализация для эндпоинта — Реализовать метод getRetweetsByTweetId в RetweetController.
  acceptance: "Метод реализован в RetweetController с @LoggableRequest и @PageableDefault"
  note: "Добавлен метод getRetweetsByTweetId в RetweetController. Использует @LoggableRequest для логирования запросов. Использует @PageableDefault(size = 20, sort = \"createdAt\", direction = Sort.Direction.DESC) для пагинации по умолчанию. Путь: GET /api/v1/tweets/{tweetId}/retweets. Возвращает PagedModel<RetweetResponseDto>. Преобразует Page<RetweetResponseDto> в PagedModel через new PagedModel<>(retweets). Добавлены импорты Page, Pageable, Sort, PageableDefault, PagedModel. Следует паттерну из LikeController.getLikesByTweetId."
  commit: "services/tweet-api/src/main/java/com/twitter/controller/RetweetController.java"
- [x] (P1) [2025-01-27] #7: JavaDoc для эндпоинта — Добавить JavaDoc для всех новых методов.
  acceptance: "JavaDoc добавлен для всех методов эндпоинта с @author geron, @version 1.0, @param, @return, @throws"
  note: "Проверен JavaDoc для всех новых методов: RetweetValidator.validateTweetExists (полный JavaDoc с @param, @throws), RetweetValidatorImpl.validateTweetExists (JavaDoc с @see), RetweetService.getRetweetsByTweetId (полный JavaDoc с @param, @return, @throws), RetweetServiceImpl.getRetweetsByTweetId (JavaDoc с @see), RetweetApi.getRetweetsByTweetId (полный JavaDoc с @param, @return, @throws), RetweetController.getRetweetsByTweetId (JavaDoc с @see). Все классы имеют @author geron и @version 1.0. Все методы имеют JavaDoc согласно STANDART_JAVADOC.md. Примечание: метод findByTweetIdOrderByCreatedAtDesc в RetweetRepository - Derived Query Method, JavaDoc не требуется согласно стандартам."
  commit: "Проверка JavaDoc для всех методов"
- [x] (P1) [2025-01-27] #8: Unit тесты для эндпоинта — Написать Unit тесты для Service и Validator методов.
  acceptance: "Unit тесты для RetweetServiceImpl.getRetweetsByTweetId и RetweetValidatorImpl.validateTweetExists с учетом STANDART_TEST.md (успешный сценарий, твит не найден, пустой список, пагинация)"
  note: "Добавлены unit тесты для RetweetServiceImpl.getRetweetsByTweetId в RetweetServiceImplTest. Покрыты сценарии: успешный (ретвиты существуют), пустой список, твит не найден (BusinessRuleValidationException), пагинация (разные параметры). Добавлены unit тесты для RetweetValidatorImpl.validateTweetExists в RetweetValidatorImplTest. Покрыты сценарии: успешный (твит существует), tweetId null, твит не найден, твит удален. Тесты следуют паттерну methodName_WhenCondition_ShouldExpectedResult. Используются @Nested классы, моки для зависимостей, PageImpl для пагинации. Все тесты используют AssertJ для assertions и Mockito для моков."
  commit: "services/tweet-api/src/test/java/com/twitter/service/RetweetServiceImplTest.java, services/tweet-api/src/test/java/com/twitter/validation/RetweetValidatorImplTest.java"
- [x] (P2) [2025-01-27] #9: Integration тесты для эндпоинта — Написать Integration тесты для контроллера.
  acceptance: "Integration тесты для RetweetController.getRetweetsByTweetId с MockMvc, все статус-коды проверены (200 OK, 400 Bad Request, 409 Conflict, пагинация)"
  note: "Добавлены integration тесты для RetweetController.getRetweetsByTweetId в RetweetControllerTest. Покрыты сценарии: 200 OK (ретвиты существуют), 200 OK (пустой список), 409 Conflict (твит не найден), 400 Bad Request (неверный формат UUID), пагинация (разные параметры), пагинация по умолчанию, сортировка по createdAt DESC. Тесты используют MockMvc, проверяют структуру ответа (content, page), статус-коды и метаданные пагинации. Добавлен импорт get метода. Следует паттерну из LikeControllerTest.getLikesByTweetId."
  commit: "services/tweet-api/src/test/java/com/twitter/controller/RetweetControllerTest.java"
- [x] (P1) [2025-01-27] #10: Swagger документация для эндпоинта — Убедиться, что OpenAPI документация полная.
  acceptance: "OpenAPI документация для эндпоинта полная с @ExampleObject для всех сценариев (200 OK, 400 Bad Request, 409 Conflict)"
  note: "Проверена OpenAPI документация для getRetweetsByTweetId в RetweetApi. Документация полная и включает: @Operation с summary и description, @ApiResponses с примерами для 200 OK (Paginated Retweets и Empty Retweets List), 400 Bad Request (Invalid Pagination Error и Invalid UUID Format Error), 409 Conflict (Tweet Not Found Error). Все примеры соответствуют реальным ответам API. Документация следует паттерну из LikeApi.getLikesByTweetId. Примечание: используется 409 Conflict вместо 404 Not Found для BusinessRuleValidationException, что соответствует стандартам проекта."
  commit: "Проверка OpenAPI документации (изменений не требуется)"

### Финальная инфраструктура
- [x] (P2) [2025-01-27] #11: Обновление README.md — Обновить README с описанием нового эндпоинта.
  acceptance: "README обновлен с учетом STANDART_README.md, эндпоинт добавлен в таблицу эндпоинтов, детальное описание, примеры использования на русском языке"
  note: "Обновлен README.md для tweet-api: добавлен пункт 'Получить пользователей, ретвитнувших твит с пагинацией' в раздел 'Основные возможности', добавлена строка в таблицу эндпоинтов (GET /{tweetId}/retweets), добавлен раздел '10. Получить пользователей, ретвитнувших твит' с детальным описанием (параметры пути и запроса, валидация, бизнес-правила, ответы, примеры JSON ответов, примеры использования с curl), добавлен раздел 'Получить пользователей, ретвитнувших твит' в 'Примеры использования' с примерами curl запросов и JSON ответов для всех сценариев (200 OK с ретвитами, 200 OK без ретвитов, 409 Conflict, 400 Bad Request). Все описания на русском языке, следуют паттерну из раздела 'Получить пользователей, лайкнувших твит'."
  commit: "services/tweet-api/README.md"
- [x] (P2) [2025-01-27] #12: Обновление Postman коллекции — Добавить запрос в Postman коллекцию.
  acceptance: "Добавлен запрос get retweets by tweet id с примерами ответов для всех эндпоинтов (200 OK, 400 Bad Request, 409 Conflict), обновлены переменные окружения"
  note: "Добавлен запрос 'get retweets by tweet id' в Postman коллекцию twitter-tweet-api.postman_collection.json. Запрос включает: метод GET, URL {{baseUrl}}/api/v1/tweets/{{tweetId}}/retweets с параметрами пагинации (page, size, sort), описание эндпоинта, примеры ответов для всех сценариев: 'retweets retrieved' (200 OK с ретвитами, включая comment), 'empty retweets list' (200 OK без ретвитов), 'tweet not found error' (409 Conflict), 'invalid uuid format error' (400 Bad Request), 'invalid pagination parameters error' (400 Bad Request). Обновлено описание коллекции, добавлен пункт 'Get users who retweeted a tweet with pagination'. Все примеры ответов соответствуют реальным ответам API. Структура запроса следует паттерну из 'get likes by tweet id'."
  commit: "postman/tweet-api/twitter-tweet-api.postman_collection.json"
- [x] (P1) [2025-01-27] #13: Проверка соответствия стандартам — Проверить соответствие всем стандартам проекта.
  acceptance: "Все стандарты проверены, код соответствует требованиям (CODE, TEST, JAVADOC, SWAGGER, POSTMAN, README)"
  note: "Проведена финальная проверка соответствия всем стандартам проекта. Результаты: STANDART_CODE.md - ✅ Layered Architecture соблюдена (Repository → Service → Controller), API Interface Separation соблюдена (RetweetApi отдельно от RetweetController), Service Interface Pattern соблюдена (RetweetService интерфейс + RetweetServiceImpl), @Transactional(readOnly = true) для read операций, @LoggableRequest для контроллера, @PageableDefault для пагинации. STANDART_TEST.md - ✅ Именование тестов: methodName_WhenCondition_ShouldExpectedResult, Unit тесты с моками (RetweetServiceImplTest, RetweetValidatorImplTest), Integration тесты с MockMvc (RetweetControllerTest), @Nested классы используются. STANDART_JAVADOC.md - ✅ @author geron и @version 1.0 для всех классов, @param, @return, @throws для всех методов, @see для реализаций интерфейсов. STANDART_SWAGGER.md - ✅ @Operation, @ApiResponses, @Parameter в RetweetApi, @ExampleObject для всех сценариев (200 OK, 400 Bad Request, 409 Conflict), отдельный интерфейс RetweetApi. STANDART_README.md - ✅ Эндпоинт добавлен в таблицу REST API, детальное описание с примерами, примеры использования на русском языке. STANDART_POSTMAN.md - ✅ Запрос 'get retweets by tweet id' добавлен, примеры ответов для всех сценариев (200 OK, 400 Bad Request, 409 Conflict), описание коллекции обновлено. Все стандарты соблюдены."
  commit: "Проверка соответствия стандартам (изменений не требуется)"

## Assumptions
- Используется существующий RetweetResponseDto - дополнительных DTO не требуется
- PagedModel из Spring Data Web уже используется в проекте
- TweetRepository существует и может использоваться для валидации существования твита
- Индекс на tweet_id в таблице tweet_retweets уже существует
- Пагинация по умолчанию: page=0, size=20, sort=createdAt,DESC
- Эндпоинт следует паттернам существующего эндпоинта GET /api/v1/tweets/{tweetId}/likes

## Risks
- **Технические риски:**
  - Производительность при большом количестве ретвитов: митигация - использование пагинации и индексов БД
  - Отсутствие индекса на tweet_id: митигация - проверить наличие индекса, добавить если нужно
  
- **Организационные риски:**
  - Зависимость от TweetRepository: риск минимален, репозиторий уже существует
  - Необходимость консистентности с существующим эндпоинтом likes: митигация - использовать как референсную реализацию

## Metrics & Success Criteria
- Все тесты проходят (unit и integration)
- Покрытие кода > 80% для новых методов
- Время ответа < 200ms для типичного запроса
- Соответствие всем стандартам проекта
- Полная документация (OpenAPI, README, Postman)
- Эндпоинт возвращает пагинированный список ретвитов
- Поддерживается пагинация с параметрами page, size, sort
- Ретвиты сортируются по createdAt DESC (новые первыми)
- При отсутствии твита возвращается 409 Conflict
- При отсутствии ретвитов возвращается пустой список (200 OK)

## Notes
- Эндпоинт следует паттернам существующих GET эндпоинтов с пагинацией (getUserTweets, getTimeline, getLikesByTweetId)
- Используется PagedModel для пагинации (как в TweetController и LikeController)
- Валидация существования твита выполняется в Service слое через RetweetValidator
- Derived Query Methods в Repository не требуют JavaDoc согласно STANDART_JAVADOC.md
- Все JavaDoc на английском языке
- Все README на русском языке
- Все Swagger документация на английском языке
- Эндпоинт аналогичен GET /api/v1/tweets/{tweetId}/likes и должен следовать той же структуре
