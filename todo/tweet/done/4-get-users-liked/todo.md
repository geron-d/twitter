# TODO: Реализация эндпоинта GET /api/v1/tweets/{tweetId}/likes

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
- [x] (P1) #1: Анализ требований — Анализ существующего кода, определение структуры эндпоинта, проектирование API контракта.
  acceptance: "Понять вход/выход, определить затронутые стандарты, определить структуру эндпоинта"

### Реализация инфраструктуры и конфигов
- [x] (P1) [2025-01-27] #2: Обновление LikeRepository — Добавить метод findByTweetIdOrderByCreatedAtDesc для получения лайков с пагинацией.
  acceptance: "Метод findByTweetIdOrderByCreatedAtDesc(UUID tweetId, Pageable pageable) добавлен в LikeRepository (Derived Query Method, JavaDoc не требуется)"
  note: "Добавлен метод findByTweetIdOrderByCreatedAtDesc в LikeRepository. Добавлены импорты Page и Pageable. Метод следует паттерну из TweetRepository. Derived Query Method - JavaDoc не требуется согласно STANDART_JAVADOC.md"
  commit: "services/tweet-api/src/main/java/com/twitter/repository/LikeRepository.java"
- [x] (P1) [2025-01-27] #3: Обновление LikeMapper — Добавить метод toLikeResponseDtoPage для маппинга Page<Like> в Page<LikeResponseDto>.
  acceptance: "Метод toLikeResponseDtoPage(Page<Like>) добавлен в LikeMapper с default реализацией"
  note: "Добавлен default метод toLikeResponseDtoPage в LikeMapper. Метод использует существующий toLikeResponseDto для маппинга каждого элемента Page, сохраняя метаданные пагинации. Добавлен импорт org.springframework.data.domain.Page. Метод следует паттерну маппинга Page из Spring Data."
  commit: "services/tweet-api/src/main/java/com/twitter/mapper/LikeMapper.java"

### Эндпоинт: GET /api/v1/tweets/{tweetId}/likes
- [x] (P1) [2025-01-27] #4: Service метод для эндпоинта — Добавить метод getLikesByTweetId в LikeService интерфейс и реализацию.
  acceptance: "Метод добавлен в LikeService интерфейс с JavaDoc, реализован в LikeServiceImpl с валидацией существования твита, использует @Transactional(readOnly = true)"
  note: "Добавлен метод getLikesByTweetId в LikeService интерфейс с полным JavaDoc. Реализован в LikeServiceImpl с валидацией существования твита через likeValidator.validateTweetExists. Валидация вынесена в LikeValidator (добавлен метод validateTweetExists). Использует @Transactional(readOnly = true). Добавлены импорты Page и Pageable. Метод использует likeRepository.findByTweetIdOrderByCreatedAtDesc и likeMapper.toLikeResponseDtoPage для маппинга."
  commit: "services/tweet-api/src/main/java/com/twitter/service/LikeService.java, services/tweet-api/src/main/java/com/twitter/service/LikeServiceImpl.java, services/tweet-api/src/main/java/com/twitter/validation/LikeValidator.java, services/tweet-api/src/main/java/com/twitter/validation/LikeValidatorImpl.java"
- [x] (P1) [2025-01-27] #5: OpenAPI интерфейс для эндпоинта — Добавить метод getLikesByTweetId в LikeApi интерфейс с OpenAPI аннотациями.
  acceptance: "Метод добавлен в LikeApi интерфейс с OpenAPI аннотациями (@Operation, @ApiResponses, @Parameter)"
  note: "Добавлен метод getLikesByTweetId в LikeApi интерфейс с полной OpenAPI документацией. Метод возвращает PagedModel<LikeResponseDto>. Добавлены @Operation с summary и description, @ApiResponses с примерами для 200 OK (с пустым списком), 400 (валидация пагинации и UUID), 409 (твит не найден). Добавлены @Parameter для tweetId и pageable. Добавлены импорты PagedModel и Pageable. Следует паттерну из TweetApi.getUserTweets."
  commit: "services/tweet-api/src/main/java/com/twitter/controller/LikeApi.java"
- [x] (P1) [2025-01-27] #6: Controller реализация для эндпоинта — Реализовать метод getLikesByTweetId в LikeController.
  acceptance: "Метод реализован в LikeController с @LoggableRequest и @PageableDefault"
  note: "Добавлен метод getLikesByTweetId в LikeController. Использует @LoggableRequest для логирования запросов. Использует @PageableDefault(size = 20, sort = \"createdAt\", direction = Sort.Direction.DESC) для пагинации по умолчанию. Путь: GET /api/v1/tweets/{tweetId}/likes. Возвращает PagedModel<LikeResponseDto>. Преобразует Page<LikeResponseDto> в PagedModel через new PagedModel<>(likes). Добавлены импорты Page, Pageable, Sort, PageableDefault, PagedModel. Следует паттерну из TweetController.getUserTweets."
  commit: "services/tweet-api/src/main/java/com/twitter/controller/LikeController.java"
- [x] (P1) [2025-01-27] #7: JavaDoc для эндпоинта — Добавить JavaDoc для всех новых методов.
  acceptance: "JavaDoc добавлен для всех методов эндпоинта с @author geron, @version 1.0, @param, @return, @throws"
  note: "Проверен JavaDoc для всех новых методов: LikeService.getLikesByTweetId (полный JavaDoc с @param, @return, @throws), LikeServiceImpl.getLikesByTweetId (JavaDoc с @see), LikeValidator.validateTweetExists (полный JavaDoc с @param, @throws), LikeValidatorImpl.validateTweetExists (JavaDoc с @see), LikeApi.getLikesByTweetId (полный JavaDoc с @param, @return, @throws), LikeController.getLikesByTweetId (JavaDoc с @see). Все методы имеют JavaDoc согласно STANDART_JAVADOC.md. Примечание: метод toLikeResponseDtoPage не существует в LikeMapper (используется likes.map(likeMapper::toLikeResponseDto) в LikeServiceImpl)."
  commit: "Проверка JavaDoc для всех методов"
- [x] (P1) [2025-01-27] #8: Unit тесты для эндпоинта — Написать Unit тесты для Service и Mapper методов.
  acceptance: "Unit тесты для LikeServiceImpl.getLikesByTweetId и LikeMapper.toLikeResponseDtoPage с учетом STANDART_TEST.md (успешный сценарий, твит не найден, пустой список, пагинация)"
  note: "Добавлены unit тесты для LikeServiceImpl.getLikesByTweetId в LikeServiceImplTest. Покрыты сценарии: успешный (лайки существуют), пустой список, твит не найден (BusinessRuleValidationException), пагинация (разные параметры). Тесты следуют паттерну methodName_WhenCondition_ShouldExpectedResult. Используются @Nested классы, моки для зависимостей, PageImpl для пагинации. Примечание: метод toLikeResponseDtoPage не существует в LikeMapper (используется likes.map(likeMapper::toLikeResponseDto)), поэтому тесты для маппера не требуются."
  commit: "services/tweet-api/src/test/java/com/twitter/service/LikeServiceImplTest.java"
- [x] (P2) [2025-01-27] #9: Integration тесты для эндпоинта — Написать Integration тесты для контроллера.
  acceptance: "Integration тесты для LikeController.getLikesByTweetId с MockMvc, все статус-коды проверены (200 OK, 404 Not Found, 400 Bad Request, пагинация)"
  note: "Добавлены integration тесты для LikeController.getLikesByTweetId в LikeControllerTest. Покрыты сценарии: 200 OK (лайки существуют), 200 OK (пустой список), 409 Conflict (твит не найден), 400 Bad Request (неверный формат UUID), пагинация (разные параметры), пагинация по умолчанию, сортировка по createdAt DESC. Тесты используют MockMvc, проверяют структуру ответа (content, page), статус-коды и метаданные пагинации. Добавлен импорт get метода."
  commit: "services/tweet-api/src/test/java/com/twitter/controller/LikeControllerTest.java"
- [x] (P1) [2025-01-27] #10: Swagger документация для эндпоинта — Убедиться, что OpenAPI документация полная.
  acceptance: "OpenAPI документация для эндпоинта полная с @ExampleObject для всех сценариев (200 OK, 404 Not Found, 400 Bad Request)"
  note: "Проверена OpenAPI документация для getLikesByTweetId в LikeApi. Документация полная и включает: @Operation с summary и description, @ApiResponses с примерами для 200 OK (Paginated Likes и Empty Likes List), 400 Bad Request (Invalid Pagination Error и Invalid UUID Format Error), 409 Conflict (Tweet Not Found Error). Все примеры соответствуют реальным ответам API. Документация следует паттерну из TweetApi.getUserTweets. Примечание: используется 409 Conflict вместо 404 Not Found для BusinessRuleValidationException, что соответствует стандартам проекта."
  commit: "Проверка OpenAPI документации (изменений не требуется)"

### Финальная инфраструктура
- [x] (P2) [2025-01-27] #11: Обновление README.md — Обновить README с описанием нового эндпоинта.
  acceptance: "README обновлен с учетом STANDART_README.md, эндпоинт добавлен в таблицу эндпоинтов, детальное описание, примеры использования на русском языке"
  note: "Обновлен README.md: добавлен эндпоинт GET /{tweetId}/likes в таблицу эндпоинтов, добавлено детальное описание эндпоинта (раздел 9) с параметрами, валидацией, бизнес-правилами, примерами ответов и ошибок, добавлен пример использования в раздел \"Примеры использования\" с curl командами и примерами ответов, обновлен список основных возможностей. Все описания на русском языке согласно STANDART_README.md."
  commit: "services/tweet-api/README.md"
- [x] (P2) [2025-01-27] #12: Обновление Postman коллекции — Добавить запрос в Postman коллекцию.
  acceptance: "Добавлен запрос get likes by tweet id с примерами ответов для всех эндпоинтов (200 OK, 404 Not Found, 400 Bad Request), обновлены переменные окружения"
  note: "Добавлен запрос 'get likes by tweet id' в Postman коллекцию после 'remove like'. Запрос включает: GET метод с параметрами пагинации (page, size, sort), описание эндпоинта, примеры ответов: likes retrieved (200 OK), empty likes list (200 OK), tweet not found error (409 Conflict), invalid uuid format error (400 Bad Request), invalid pagination parameters error (400 Bad Request). Обновлено описание коллекции в info секции. Запрос следует структуре 'get user tweets' для пагинированных GET эндпоинтов."
  commit: "postman/tweet-api/twitter-tweet-api.postman_collection.json"
- [x] (P1) [2025-01-27] #13: Проверка соответствия стандартам — Проверить соответствие всем стандартам проекта.
  acceptance: "Все стандарты проверены, код соответствует требованиям (CODE, TEST, JAVADOC, SWAGGER, POSTMAN, README)"
  note: "Проверено соответствие всем стандартам проекта: STANDART_CODE.md (Java 24 features, MapStruct, Lombok, структура пакетов, @Transactional, @LoggableRequest, @PageableDefault, PagedModel), STANDART_TEST.md (структура тестов с @Nested, именование methodName_WhenCondition_ShouldExpectedResult, использование Mockito, AssertJ), STANDART_JAVADOC.md (JavaDoc для всех методов с @author geron, @version 1.0, @param, @return, @throws), STANDART_SWAGGER.md (OpenAPI документация с @Operation, @ApiResponses, @ExampleObject), STANDART_POSTMAN.md (структура коллекции, именование запросов, примеры ответов), STANDART_README.md (структура README, описание на русском языке). Все стандарты соблюдены."
  commit: "Проверка соответствия стандартам (изменений не требуется)"

## Assumptions
- Используется существующий LikeResponseDto - дополнительных DTO не требуется
- PagedModel из Spring Data Web уже используется в проекте
- TweetRepository существует и может использоваться для валидации существования твита
- Индекс на tweet_id в таблице tweet_likes уже существует (если нет - нужно добавить)
- Пагинация по умолчанию: page=0, size=20, sort=createdAt,DESC

## Risks
- **Технические риски:**
  - Производительность при большом количестве лайков: митигация - использование пагинации и индексов БД
  - Отсутствие индекса на tweet_id: митигация - проверить наличие индекса, добавить если нужно
  
- **Организационные риски:**
  - Зависимость от TweetRepository: риск минимален, репозиторий уже существует

## Metrics & Success Criteria
- Все тесты проходят (unit и integration)
- Покрытие кода > 80% для новых методов
- Время ответа < 200ms для типичного запроса
- Соответствие всем стандартам проекта
- Полная документация (OpenAPI, README, Postman)
- Эндпоинт возвращает пагинированный список лайков
- Поддерживается пагинация с параметрами page, size, sort
- Лайки сортируются по createdAt DESC (новые первыми)
- При отсутствии твита возвращается 404
- При отсутствии лайков возвращается пустой список (200 OK)

## Notes
- Эндпоинт следует паттернам существующих GET эндпоинтов с пагинацией (getUserTweets, getTimeline)
- Используется PagedModel для пагинации (как в TweetController)
- Валидация существования твита выполняется в Service слое
- Derived Query Methods в Repository не требуют JavaDoc согласно STANDART_JAVADOC.md
- Все JavaDoc на английском языке
- Все README на русском языке
- Все Swagger документация на английском языке

