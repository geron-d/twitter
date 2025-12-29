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
- [ ] (P1) #4: Реализация Gateway для follower-api — Создать FollowerGateway для абстракции работы с follower-api
  acceptance: "FollowerGateway создан с методом getFollowingUserIds, обработка ошибок с безопасными значениями, логирование операций"
- [ ] (P1) #5: Реализация Repository метода — Добавить метод для получения твитов по списку userIds
  acceptance: "Метод findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc добавлен в TweetRepository, использует Derived Query Method (без JavaDoc)"
- [ ] (P2) #6: Обновление конфигов — Проверить и обновить FeignConfig если нужно
  acceptance: "FeignConfig проверен, Feign клиенты включены для пакета com.twitter.client, конфигурация для follower-api добавлена если нужно"
- [ ] (P1) #6.1: Обновление application.yml — Добавить настройки для follower-api
  acceptance: "Добавлена секция app.follower-api.base-url в application.yml со значением http://localhost:8084 для локальной разработки"
- [ ] (P1) #6.2: Обновление application-docker.yml — Добавить настройки для follower-api в Docker окружении
  acceptance: "Добавлена секция app.follower-api.base-url в application-docker.yml со значением http://follower-api:8084 для Docker окружения"
- [ ] (P1) #6.3: Обновление docker-compose.yml — Добавить зависимость tweet-api от follower-api и переменную окружения
  acceptance: "Добавлена зависимость tweet-api от follower-api с condition: service_healthy, добавлена переменная окружения FOLLOWER_API_URL=http://follower-api:8084"

### Эндпоинт: GET /api/v1/tweets/timeline/{userId}
- [ ] (P1) #7: DTO для эндпоинта — Проверить необходимость специфичных DTO
  acceptance: "Определено что используется существующий TweetResponseDto, path и query параметры обрабатываются через Spring"
- [ ] (P1) #8: Mapper методы для эндпоинта — Проверить необходимость новых методов маппинга
  acceptance: "Определено что используются существующие методы TweetMapper, новых методов не требуется"
- [ ] (P1) #9: Validator методы для эндпоинта — Добавить метод validateForTimeline
  acceptance: "Метод validateForTimeline добавлен в TweetValidator интерфейс и реализацию, проверка существования пользователя через UserGateway"
- [ ] (P1) #10: Service методы для эндпоинта — Добавить метод getTimeline
  acceptance: "Метод getTimeline добавлен в TweetService интерфейс и реализацию, использует @Transactional(readOnly = true), интеграция с FollowerGateway"
- [ ] (P1) #11: Controller метод для эндпоинта — Добавить метод getTimeline в TweetApi и TweetController
  acceptance: "Метод добавлен в TweetApi интерфейс с OpenAPI аннотациями и в TweetController с @LoggableRequest, использует @PageableDefault"
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

