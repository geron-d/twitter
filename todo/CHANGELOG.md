# Changelog

## 2025-01-27

### tweet-api: Timeline endpoint implementation

- **2025-01-27** — step #1 done — Анализ требований для эндпоинта получения ленты новостей — автор: assistant
  - Выполнен полный анализ требований
  - Определены входные/выходные данные, бизнес-правила, зависимости от follower-api
  - Определены затронутые стандарты проекта
  - Определен список всех эндпоинтов tweet-api
  - Спроектирована интеграция с follower-api через Feign Client и Gateway паттерн
  - Создан документ: `todo/tweet/done/analysis-requirements.md`

- **2025-01-27** — step #2 done — Проектирование API и контрактов для эндпоинта получения ленты новостей — автор: assistant
  - Определена OpenAPI схема для эндпоинта getTimeline с полной документацией
  - Определена структура ответа (использование существующего TweetResponseDto)
  - Определен контракт с follower-api (эндпоинт, структура запроса/ответа, обработка ошибок)
  - Определены общие компоненты (переиспользование) и специфичные компоненты (новые)
  - Создан документ: `todo/tweet/done/design-api-contracts.md`

- **2025-01-27** — step #3 done — Реализация Feign клиента для follower-api — автор: assistant
  - Создан FollowerApiClient с методом getFollowing
  - Использует @FeignClient с конфигурацией name='follower-api', url из application.yml
  - Метод использует @SpringQueryMap для передачи Pageable параметров
  - Возвращает PagedModel<FollowingResponseDto>
  - Создан FollowingResponseDto в common-lib для межсервисной коммуникации
  - Файлы: `services/tweet-api/src/main/java/com/twitter/client/FollowerApiClient.java`, `shared/common-lib/src/main/java/com/twitter/common/dto/response/FollowingResponseDto.java`

- **2025-01-27** — step #4 done — Реализация Gateway для follower-api — автор: assistant
  - Создан FollowerGateway с методом getFollowingUserIds
  - Реализовано получение всех подписок через пагинацию (размер страницы 100)
  - Обработка ошибок с возвратом пустого списка (graceful degradation)
  - Логирование операций на уровнях debug и info
  - Следует паттерну UserGateway для консистентности
  - Файл: `services/tweet-api/src/main/java/com/twitter/gateway/FollowerGateway.java`

- **2025-01-27** — step #5 done — Реализация Repository метода — автор: assistant
  - Добавлен метод findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc в TweetRepository
  - Использует Derived Query Method Spring Data JPA для выполнения IN запроса по списку userIds
  - Возвращает Page<Tweet> с пагинацией и сортировкой по createdAt DESC
  - Исключает удаленные твиты (isDeleted = false)
  - Без JavaDoc согласно стандартам проекта
  - Файл: `services/tweet-api/src/main/java/com/twitter/repository/TweetRepository.java`

- **2025-01-27** — step #6.1 done — Обновление application.yml — автор: assistant
  - Добавлена секция app.follower-api.base-url в application.yml
  - Значение: http://localhost:8084 для локальной разработки
  - Настройка следует паттерну app.users-api.base-url
  - Файл: `services/tweet-api/src/main/resources/application.yml`

- **2025-01-27** — step #6.2 done — Обновление application-docker.yml — автор: assistant
  - Добавлена секция app.follower-api.base-url в application-docker.yml
  - Значение: http://follower-api:8084 для Docker окружения
  - Настройка следует паттерну app.users-api.base-url
  - Используется имя сервиса Docker (follower-api) вместо localhost
  - Файл: `services/tweet-api/src/main/resources/application-docker.yml`

- **2025-01-27** — step #6.3 done — Обновление docker-compose.yml — автор: assistant
  - Добавлена зависимость tweet-api от follower-api с condition: service_healthy
  - Добавлена переменная окружения FOLLOWER_API_URL=http://follower-api:8084
  - Конфигурация следует паттерну зависимости от users-api
  - Соответствует стандартам STANDART_DOCKER.md
  - Файл: `docker-compose.yml`

- **2025-01-27** — step #6 done — Обновление конфигов (FeignConfig) — автор: assistant
  - Проверен FeignConfig на корректность конфигурации
  - Подтверждено, что @EnableFeignClients(basePackages = "com.twitter.client") автоматически включает FollowerApiClient
  - Общие настройки Feign из application.yml (таймауты, logger-level) применяются ко всем клиентам
  - Дополнительных изменений в FeignConfig не требуется
  - Файл: `services/tweet-api/src/main/java/com/twitter/config/FeignConfig.java`

- **2025-01-27** — step #7 done — Проверка необходимости специфичных DTO для эндпоинта — автор: assistant
  - Проверена необходимость специфичных DTO для эндпоинта getTimeline
  - Определено: используется существующий TweetResponseDto из shared/common-lib (аналогично getUserTweets)
  - Path параметр userId обрабатывается через @PathVariable UUID (стандартный Spring механизм)
  - Query параметры (page, size, sort) обрабатываются через Spring Pageable с @PageableDefault
  - Ответ - PagedModel<TweetResponseDto> (аналогично getUserTweets)
  - Новых DTO не требуется, следует паттерну getUserTweets для консистентности

- **2025-01-27** — step #8 done — Проверка необходимости новых методов маппинга для эндпоинта — автор: assistant
  - Проверена необходимость новых методов маппинга для эндпоинта getTimeline
  - Определено: используется существующий метод TweetMapper.toResponseDto(Tweet tweet) (аналогично getUserTweets)
  - Паттерн использования: tweetRepository.findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc(userIds, pageable).map(tweetMapper::toResponseDto)
  - Spring Data Page поддерживает метод .map() для преобразования элементов
  - Оба метода (getUserTweets и getTimeline) возвращают Page<Tweet> и используют один и тот же метод маппера
  - Новых методов маппинга не требуется

- **2025-01-27** — step #9 done — Добавление метода validateForTimeline в TweetValidator — автор: assistant
  - Добавлен метод validateForTimeline(UUID userId) в TweetValidator интерфейс
  - Реализован метод в TweetValidatorImpl с использованием validateUserExists
  - Метод проверяет существование пользователя через UserGateway.existsUser
  - Следует паттерну других методов валидации (validateForCreate)
  - Метод проверяет, что userId не null и пользователь существует в системе
  - Файлы: `services/tweet-api/src/main/java/com/twitter/validation/TweetValidator.java`, `services/tweet-api/src/main/java/com/twitter/validation/TweetValidatorImpl.java`

- **2025-01-27** — step #10 done — Добавление метода getTimeline в TweetService — автор: assistant
  - Добавлен метод getTimeline(UUID userId, Pageable pageable) в TweetService интерфейс
  - Реализован метод в TweetServiceImpl с полной JavaDoc документацией
  - Метод использует @Transactional(readOnly = true) для оптимизации производительности
  - Реализована интеграция с FollowerGateway для получения списка подписок
  - Если список подписок пустой, возвращается пустая страница (не ошибка)
  - Используется Repository метод findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc для получения твитов
  - Маппинг выполняется через tweetMapper.toResponseDto
  - Добавлен FollowerGateway в зависимости TweetServiceImpl
  - Файлы: `services/tweet-api/src/main/java/com/twitter/service/TweetService.java`, `services/tweet-api/src/main/java/com/twitter/service/TweetServiceImpl.java`

- **2025-01-27** — step #11 done — Добавление метода getTimeline в TweetApi и TweetController — автор: assistant
  - Добавлен метод getTimeline в TweetApi интерфейс с полной OpenAPI документацией
  - OpenAPI аннотации включают @Operation с описанием, @ApiResponses с примерами для всех сценариев
  - Примеры ответов: успешный ответ с твитами, пустая лента (нет подписок), пустая лента (нет твитов)
  - Примеры ошибок: невалидный UUID, пользователь не существует, невалидные параметры пагинации, недоступность follower-api
  - Реализован метод в TweetController с @LoggableRequest для логирования запросов
  - Используется @GetMapping("/timeline/{userId}") для маппинга пути
  - Используется @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) для пагинации по умолчанию
  - Метод вызывает tweetService.getTimeline и возвращает PagedModel<TweetResponseDto>
  - Следует паттерну getUserTweets для консистентности
  - Файлы: `services/tweet-api/src/main/java/com/twitter/controller/TweetApi.java`, `services/tweet-api/src/main/java/com/twitter/controller/TweetController.java`

- **2025-01-27** — step #12 done — JavaDoc для эндпоинта — автор: assistant
  - Проверена JavaDoc документация для всех методов эндпоинта getTimeline
  - Все методы имеют полную JavaDoc документацию согласно STANDART_JAVADOC.md
  - TweetApi#getTimeline: добавлены @throws теги для BusinessRuleValidationException и ConstraintViolationException
  - TweetService#getTimeline: имеет @param, @return, @throws
  - TweetValidator#validateForTimeline: имеет @param, @throws
  - TweetServiceImpl#getTimeline: имеет @see (соответствует стандартам для простых реализаций)
  - TweetController#getTimeline: имеет @see (соответствует стандартам для простых реализаций)
  - TweetValidatorImpl#validateForTimeline: имеет @see (соответствует стандартам для простых реализаций)
  - FollowerGateway#getFollowingUserIds: имеет @param, @return
  - FollowerApiClient#getFollowing: имеет @param, @return
  - Все методы соответствуют стандартам STANDART_JAVADOC.md
  - Файл: `services/tweet-api/src/main/java/com/twitter/controller/TweetApi.java`

- **2025-01-27** — step #13 done — Unit тесты для эндпоинта — автор: assistant
  - Добавлены unit тесты для метода getTimeline в TweetServiceImplTest (6 тестов)
  - Тесты для getTimeline: успешный сценарий с твитами, пустая страница (нет подписок), пустая страница (нет твитов), проверка вызовов зависимостей, проверка отсутствия вызовов при пустых подписках, проверка валидации
  - Добавлены unit тесты для метода validateForTimeline в TweetValidatorImplTest (3 теста)
  - Тесты для validateForTimeline: успешный сценарий (пользователь существует), userId is null, пользователь не существует
  - Все тесты следуют стандартам STANDART_TEST.md
  - Используется паттерн именования methodName_WhenCondition_ShouldExpectedResult
  - Используется @Nested для группировки тестов
  - Проверяются успешные и ошибочные сценарии
  - Проверяется взаимодействие с зависимостями через verify
  - Добавлен мок для FollowerGateway в TweetServiceImplTest
  - Все тесты успешно проходят
  - Файлы: `services/tweet-api/src/test/java/com/twitter/service/TweetServiceImplTest.java`, `services/tweet-api/src/test/java/com/twitter/validation/TweetValidatorImplTest.java`

- **2025-01-27** — step #14 done — Integration тесты для эндпоинта — автор: assistant
  - Добавлены integration тесты для эндпоинта getTimeline в TweetControllerTest (9 тестов)
  - Тесты используют MockMvc и WireMock для мокирования follower-api и users-api
  - Добавлены helper методы в BaseIntegrationTest для настройки follower-api stubs
  - Helper методы: setupFollowingStub, setupFollowingStubEmpty, setupFollowingStubWithError
  - Обновлена конфигурация BaseIntegrationTest для добавления app.follower-api.base-url
  - Тесты проверяют: успешный сценарий с твитами (200 OK), пустая лента когда нет подписок (200 OK), пустая лента когда нет твитов (200 OK), невалидный UUID (400 Bad Request), пользователь не существует (409 Conflict, так как BusinessRuleValidationException возвращает 409), недоступность follower-api (graceful degradation, 200 OK с пустым списком), исключение удаленных твитов, сортировка по createdAt DESC
  - Все тесты следуют стандартам STANDART_TEST.md
  - Используется @Nested для группировки тестов
  - Файлы: `services/tweet-api/src/test/java/com/twitter/controller/TweetControllerTest.java`, `services/tweet-api/src/test/java/com/twitter/testconfig/BaseIntegrationTest.java`

