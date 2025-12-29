# TODO: Реализация эндпоинта получения ленты новостей

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
  - STANDART_DOCKER.md

## Tasks

### Анализ и проектирование
- [x] (P1) [2025-01-27] #1: Анализ требований — Определить входные/выходные данные, бизнес-правила, зависимости от follower-api
  acceptance: "Понять вход/выход, определить затронутые стандарты, определить список всех эндпоинтов, спроектировать интеграцию с follower-api"
  note: "Выполнен полный анализ требований. Определены входные/выходные данные, бизнес-правила, зависимости от follower-api, затронутые стандарты, список эндпоинтов. Спроектирована интеграция с follower-api через Feign Client и Gateway паттерн. Создан документ: done/analysis-requirements.md"
- [x] (P1) [2025-01-27] #2: Проектирование API и контрактов — Определить структуру эндпоинта и интеграцию с follower-api
  acceptance: "OpenAPI схема для эндпоинта, определение структуры ответа, определение контракта с follower-api, определение общих и специфичных компонентов"
  note: "Выполнено проектирование API и контрактов. Определена OpenAPI схема для эндпоинта getTimeline с полной документацией всех сценариев (успех, пустая лента, ошибки). Определена структура ответа (использование существующего TweetResponseDto). Определен контракт с follower-api (эндпоинт, структура запроса/ответа, обработка ошибок). Определены общие компоненты (переиспользование) и специфичные компоненты (новые). Создан документ: done/design-api-contracts.md"

### Реализация инфраструктуры и конфигов
- [x] (P1) [2025-01-27] #3: Реализация Feign клиента для follower-api — Создать FollowerApiClient для интеграции с follower-api
  acceptance: "FollowerApiClient создан с методом getFollowing, использует @FeignClient с правильной конфигурацией, обработка ошибок"
  note: "Создан FollowerApiClient с методом getFollowing. Использует @FeignClient с конфигурацией name='follower-api', url из application.yml, path='/api/v1/follows'. Метод использует @SpringQueryMap для передачи Pageable параметров. Возвращает PagedModel<FollowingResponseDto>. Создан FollowingResponseDto в common-lib для межсервисной коммуникации. Файлы: services/tweet-api/src/main/java/com/twitter/client/FollowerApiClient.java, shared/common-lib/src/main/java/com/twitter/common/dto/response/FollowingResponseDto.java"
- [x] (P1) [2025-01-27] #4: Реализация Gateway для follower-api — Создать FollowerGateway для абстракции работы с follower-api
  acceptance: "FollowerGateway создан с методом getFollowingUserIds, обработка ошибок с безопасными значениями, логирование операций"
  note: "Создан FollowerGateway с методом getFollowingUserIds. Реализовано получение всех подписок через пагинацию (размер страницы 100). Обработка ошибок с возвратом пустого списка (graceful degradation). Логирование операций на уровнях debug и info. Следует паттерну UserGateway. Файл: services/tweet-api/src/main/java/com/twitter/gateway/FollowerGateway.java"
- [x] (P1) [2025-01-27] #5: Реализация Repository метода — Добавить метод для получения твитов по списку userIds
  acceptance: "Метод findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc добавлен в TweetRepository, использует Derived Query Method (без JavaDoc)"
  note: "Добавлен метод findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc в TweetRepository. Использует Derived Query Method Spring Data JPA для выполнения IN запроса по списку userIds. Метод возвращает Page<Tweet> с пагинацией и сортировкой по createdAt DESC. Исключает удаленные твиты (isDeleted = false). Без JavaDoc согласно стандартам проекта. Файл: services/tweet-api/src/main/java/com/twitter/repository/TweetRepository.java"
- [x] (P2) [2025-01-27] #6: Обновление конфигов — Проверить и обновить FeignConfig если нужно
  acceptance: "FeignConfig проверен, Feign клиенты включены для пакета com.twitter.client, конфигурация для follower-api добавлена если нужно"
  note: "Проверен FeignConfig. Конфигурация корректна: @EnableFeignClients(basePackages = \"com.twitter.client\") автоматически включает FollowerApiClient. Общие настройки Feign из application.yml (таймауты, logger-level) применяются ко всем клиентам. Дополнительных изменений не требуется. Файл: services/tweet-api/src/main/java/com/twitter/config/FeignConfig.java"
- [x] (P1) [2025-01-27] #6.1: Обновление application.yml — Добавить настройки для follower-api
  acceptance: "Добавлена секция app.follower-api.base-url в application.yml со значением http://localhost:8084 для локальной разработки"
  note: "Добавлена секция app.follower-api.base-url в application.yml со значением http://localhost:8084 для локальной разработки. Настройка следует паттерну app.users-api.base-url. Файл: services/tweet-api/src/main/resources/application.yml"
- [x] (P1) [2025-01-27] #6.2: Обновление application-docker.yml — Добавить настройки для follower-api в Docker окружении
  acceptance: "Добавлена секция app.follower-api.base-url в application-docker.yml со значением http://follower-api:8084 для Docker окружения"
  note: "Добавлена секция app.follower-api.base-url в application-docker.yml со значением http://follower-api:8084 для Docker окружения. Настройка следует паттерну app.users-api.base-url. Используется имя сервиса Docker (follower-api) вместо localhost. Файл: services/tweet-api/src/main/resources/application-docker.yml"
- [x] (P1) [2025-01-27] #6.3: Обновление docker-compose.yml — Добавить зависимость tweet-api от follower-api и переменную окружения
  acceptance: "Добавлена зависимость tweet-api от follower-api с condition: service_healthy, добавлена переменная окружения FOLLOWER_API_URL=http://follower-api:8084"
  note: "Добавлена зависимость tweet-api от follower-api с condition: service_healthy в секции depends_on. Добавлена переменная окружения FOLLOWER_API_URL=http://follower-api:8084 в секции environment. Конфигурация следует паттерну зависимости от users-api и соответствует стандартам STANDART_DOCKER.md. Файл: docker-compose.yml"

### Эндпоинт: GET /api/v1/tweets/timeline/{userId}
- [x] (P1) [2025-01-27] #7: DTO для эндпоинта — Проверить необходимость специфичных DTO
  acceptance: "Определено что используется существующий TweetResponseDto, path и query параметры обрабатываются через Spring"
  note: "Проверена необходимость специфичных DTO для эндпоинта getTimeline. Определено: используется существующий TweetResponseDto из shared/common-lib (аналогично getUserTweets). Path параметр userId обрабатывается через @PathVariable UUID. Query параметры (page, size, sort) обрабатываются через Spring Pageable с @PageableDefault. Ответ - PagedModel<TweetResponseDto>. Новых DTO не требуется. Следует паттерну getUserTweets для консистентности."
- [x] (P1) [2025-01-27] #8: Mapper методы для эндпоинта — Проверить необходимость новых методов маппинга
  acceptance: "Определено что используются существующие методы TweetMapper, новых методов не требуется"
  note: "Проверена необходимость новых методов маппинга для эндпоинта getTimeline. Определено: используется существующий метод TweetMapper.toResponseDto(Tweet tweet) (аналогично getUserTweets). Паттерн использования: tweetRepository.findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc(userIds, pageable).map(tweetMapper::toResponseDto). Spring Data Page поддерживает метод .map() для преобразования элементов. Оба метода (getUserTweets и getTimeline) возвращают Page<Tweet> и используют один и тот же метод маппера. Новых методов маппинга не требуется."
- [x] (P1) [2025-01-27] #9: Validator методы для эндпоинта — Добавить метод validateForTimeline
  acceptance: "Метод validateForTimeline добавлен в TweetValidator интерфейс и реализацию, проверка существования пользователя через UserGateway"
  note: "Добавлен метод validateForTimeline(UUID userId) в TweetValidator интерфейс и TweetValidatorImpl реализацию. Метод выполняет валидацию существования пользователя через validateUserExists, который использует UserGateway.existsUser. Следует паттерну других методов валидации (validateForCreate). Метод проверяет, что userId не null и пользователь существует в системе. Файлы: services/tweet-api/src/main/java/com/twitter/validation/TweetValidator.java, services/tweet-api/src/main/java/com/twitter/validation/TweetValidatorImpl.java"
- [x] (P1) [2025-01-27] #10: Service методы для эндпоинта — Добавить метод getTimeline
  acceptance: "Метод getTimeline добавлен в TweetService интерфейс и реализацию, использует @Transactional(readOnly = true), интеграция с FollowerGateway"
  note: "Добавлен метод getTimeline(UUID userId, Pageable pageable) в TweetService интерфейс и TweetServiceImpl реализацию. Метод использует @Transactional(readOnly = true) для оптимизации производительности. Реализована интеграция с FollowerGateway для получения списка подписок. Если список подписок пустой, возвращается пустая страница. Используется Repository метод findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc для получения твитов. Маппинг выполняется через tweetMapper.toResponseDto. Добавлен FollowerGateway в зависимости TweetServiceImpl. Файлы: services/tweet-api/src/main/java/com/twitter/service/TweetService.java, services/tweet-api/src/main/java/com/twitter/service/TweetServiceImpl.java"
- [x] (P1) [2025-01-27] #11: Controller метод для эндпоинта — Добавить метод getTimeline в TweetApi и TweetController
  acceptance: "Метод добавлен в TweetApi интерфейс с OpenAPI аннотациями и в TweetController с @LoggableRequest, использует @PageableDefault"
  note: "Добавлен метод getTimeline в TweetApi интерфейс с полной OpenAPI документацией (@Operation, @ApiResponses с примерами для всех сценариев: успех, пустая лента, ошибки). Реализован метод в TweetController с @LoggableRequest для логирования запросов, @GetMapping(\"/timeline/{userId}\") для маппинга пути, @PageableDefault(size = 20, sort = \"createdAt\", direction = Sort.Direction.DESC) для пагинации по умолчанию. Метод вызывает tweetService.getTimeline и возвращает PagedModel<TweetResponseDto>. Следует паттерну getUserTweets для консистентности. Файлы: services/tweet-api/src/main/java/com/twitter/controller/TweetApi.java, services/tweet-api/src/main/java/com/twitter/controller/TweetController.java"
- [ ] (P1) #12: JavaDoc для эндпоинта — Добавить JavaDoc для всех новых методов
  acceptance: "JavaDoc добавлен для всех методов эндпоинта с @author geron, @version 1.0, @param, @return, @throws"
- [ ] (P1) #13: Unit тесты для эндпоинта — Написать unit тесты для Service и Validator
  acceptance: "Unit тесты для Service и Validator методов эндпоинта с учетом STANDART_TEST.md, именование methodName_WhenCondition_ShouldExpectedResult"
- [ ] (P2) #14: Integration тесты для эндпоинта — Написать integration тесты для контроллера
  acceptance: "Integration тесты для эндпоинта с MockMvc, WireMock для follower-api, все статус-коды проверены (200, 400)"
- [ ] (P1) #15: Swagger документация для эндпоинта — Добавить полную OpenAPI документацию
  acceptance: "OpenAPI документация для эндпоинта полная с @ExampleObject для всех сценариев (успех, пустая лента, ошибки)"

### Финальная инфраструктура
- [ ] (P2) #16: Обновление README.md — Добавить описание эндпоинта и интеграции с follower-api
  acceptance: "README обновлен с учетом STANDART_README.md, эндпоинт документирован, добавлены примеры использования, описание интеграции с follower-api"
- [ ] (P2) #17: Обновление Postman коллекции — Добавить запрос get timeline
  acceptance: "Добавлен запрос get timeline с примерами ответов для всех эндпоинтов, обновлены переменные окружения, использованы переменные {{baseUrl}} и {{userId}}"
- [ ] (P1) #18: Проверка соответствия стандартам — Проверить соответствие всем стандартам проекта
  acceptance: "Все стандарты проверены, код соответствует требованиям STANDART_CODE.md, STANDART_PROJECT.md, STANDART_TEST.md, STANDART_JAVADOC.md, STANDART_SWAGGER.md, STANDART_README.md, STANDART_POSTMAN.md, STANDART_DOCKER.md"
- [ ] (P1) #19: Проверка Docker конфигурации — Проверить соответствие docker-compose.yml стандартам
  acceptance: "docker-compose.yml проверен на соответствие STANDART_DOCKER.md (зависимости с health conditions, переменные окружения, health checks, networks)"

## Assumptions
- Follower-api доступен на порту 8084 и предоставляет эндпоинт GET /api/v1/follows/{userId}/following
- Эндпоинт follower-api возвращает PagedModel<FollowingResponseDto> с полем id (followingId)
- Если пользователь не имеет подписок, возвращается пустая страница (не ошибка)
- Если подписанные пользователи не имеют твитов, возвращается пустая страница
- Пагинация работает аналогично эндпоинту getUserTweets
- Сортировка по умолчанию: createdAt DESC (новые твиты первыми)

## Risks
- **Производительность при большом количестве подписок**: Использование IN запроса с ограничением количества userIds, кэширование списка подписок
- **Недоступность follower-api**: Circuit Breaker, fallback стратегия (возврат пустой страницы или ошибки)
- **Производительность запроса с большим списком userIds**: Ограничение количества userIds в IN запросе, батчинг если нужно
- **Зависимость от follower-api**: Договоренность об API контракте, версионирование API

## Metrics & Success Criteria
- Эндпоинт возвращает 200 OK с пагинированным списком твитов
- Эндпоинт корректно обрабатывает пустую ленту (пользователь без подписок)
- Эндпоинт валидирует userId и возвращает 400 при невалидном UUID
- Эндпоинт возвращает 400 при несуществующем пользователе
- Пагинация работает корректно (page, size, sort)
- Интеграция с follower-api работает через Feign клиент
- Все тесты проходят (unit и integration)
- Покрытие кода > 80% для новых методов
- OpenAPI документация полная и корректная
- Postman коллекция обновлена с примерами
- Время ответа < 500ms для ленты с < 100 подписок
- Время ответа < 1000ms для ленты с < 500 подписок

## Notes
- Эндпоинт должен быть реализован согласно существующим паттернам в tweet-api
- Использовать существующие компоненты (UserGateway, TweetMapper) где возможно
- Следовать архитектурным принципам из TWEET_API_ARCHITECTURE.md
- Интеграция с follower-api аналогична интеграции с users-api
- При ошибке follower-api можно вернуть пустую страницу или ошибку (требует уточнения)
- **Важно**: Необходимо обновить конфигурационные файлы (application.yml, application-docker.yml, docker-compose.yml) для интеграции с follower-api
- В docker-compose.yml нужно добавить зависимость tweet-api от follower-api с health check
- Ссылки на стандарты: standards/STANDART_*.md
- Ссылки на архитектуру: todo/tweet/TWEET_API_ARCHITECTURE.md

