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
- [ ] (P1) #7: JavaDoc для эндпоинта — Добавить JavaDoc для всех новых методов.
  acceptance: "JavaDoc добавлен для всех методов эндпоинта с @author geron, @version 1.0, @param, @return, @throws"
- [ ] (P1) #8: Unit тесты для эндпоинта — Написать Unit тесты для Service и Validator методов.
  acceptance: "Unit тесты для RetweetServiceImpl.getRetweetsByTweetId и RetweetValidatorImpl.validateTweetExists с учетом STANDART_TEST.md (успешный сценарий, твит не найден, пустой список, пагинация)"
- [ ] (P2) #9: Integration тесты для эндпоинта — Написать Integration тесты для контроллера.
  acceptance: "Integration тесты для RetweetController.getRetweetsByTweetId с MockMvc, все статус-коды проверены (200 OK, 400 Bad Request, 409 Conflict, пагинация)"
- [ ] (P1) #10: Swagger документация для эндпоинта — Убедиться, что OpenAPI документация полная.
  acceptance: "OpenAPI документация для эндпоинта полная с @ExampleObject для всех сценариев (200 OK, 400 Bad Request, 409 Conflict)"

### Финальная инфраструктура
- [ ] (P2) #11: Обновление README.md — Обновить README с описанием нового эндпоинта.
  acceptance: "README обновлен с учетом STANDART_README.md, эндпоинт добавлен в таблицу эндпоинтов, детальное описание, примеры использования на русском языке"
- [ ] (P2) #12: Обновление Postman коллекции — Добавить запрос в Postman коллекцию.
  acceptance: "Добавлен запрос get retweets by tweet id с примерами ответов для всех эндпоинтов (200 OK, 400 Bad Request, 409 Conflict), обновлены переменные окружения"
- [ ] (P1) #13: Проверка соответствия стандартам — Проверить соответствие всем стандартам проекта.
  acceptance: "Все стандарты проверены, код соответствует требованиям (CODE, TEST, JAVADOC, SWAGGER, POSTMAN, README)"

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