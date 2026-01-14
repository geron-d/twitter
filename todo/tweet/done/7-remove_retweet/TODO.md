# TODO: Реализация эндпоинта "Убрать ретвит"

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
- [x] (P1) #1: Анализ требований — Определить входные/выходные данные, бизнес-правила, затронутые стандарты
  acceptance: "Понять вход/выход (tweetId, userId, 204 No Content), бизнес-правила (существование ретвита, обновление счетчика), определить затронутые стандарты"
- [x] (P1) #2: Проектирование API и контрактов — Определить структуру валидации и бизнес-логики
  acceptance: "OpenAPI схема для DELETE эндпоинта, определение методов валидации (validateForRemoveRetweet), определение методов сервиса (removeRetweet), переиспользование существующих DTO"

### Реализация инфраструктуры и конфигов
- [x] (P1) #3: Инфраструктура уже существует — Entity, Repository, DTO, Mapper
  acceptance: "Все компоненты инфраструктуры уже созданы для POST /api/v1/tweets/{tweetId}/retweet, переиспользуются для DELETE"
  note: "Entity Retweet, RetweetRepository, RetweetRequestDto, RetweetMapper, RetweetService, RetweetController, RetweetApi уже существуют. Не требуется создание новой инфраструктуры, только расширение существующих интерфейсов и реализаций."

### Эндпоинт: DELETE /api/v1/tweets/{tweetId}/retweet
- [x] (P1) #4: DTO для эндпоинта — Переиспользование существующего RetweetRequestDto
  acceptance: "Используется существующий RetweetRequestDto (не требуется создание нового DTO)"
  note: "DELETE операция использует тот же DTO, что и POST (содержит userId). Не требуется создание нового DTO."
- [x] (P1) #5: Mapper методы для эндпоинта — Не требуется
  acceptance: "DELETE операция не требует маппинга (возвращает 204 No Content без тела ответа)"
  note: "DELETE операция не требует маппинга, так как возвращает 204 No Content без тела ответа."
- [x] (P1) #6: [2025-01-27 12:00] Validator методы для эндпоинта — Добавить метод validateForRemoveRetweet
  acceptance: "Метод validateForRemoveRetweet добавлен в RetweetValidator interface и implementation (проверка существования твита, пользователя, ретвита)"
  note: "Реализован метод validateForRemoveRetweet() в интерфейсе RetweetValidator с полной JavaDoc документацией. Реализован метод validateForRemoveRetweet() в RetweetValidatorImpl с проверками: tweetId на null, существование твита (не удален), requestDto на null, userId на null, существование пользователя через validateUserExists(), существование ретвита через новый приватный метод validateRetweetExists(). Добавлен приватный метод validateRetweetExists() с использованием retweetRepository.existsByTweetIdAndUserId(). Все исключения используют BusinessRuleValidationException из common-lib. Реализация следует паттерну validateForUnlike из LikeValidatorImpl. Код проверен линтером, ошибок нет. Выполнено: 2025-01-27"
- [x] (P1) #7: [2025-01-27 12:15] Service методы для эндпоинта — Добавить метод removeRetweet
  acceptance: "Метод removeRetweet добавлен в RetweetService interface и implementation, использует @Transactional, удаляет ретвит, обновляет счетчик"
  note: "Реализован метод removeRetweet() в интерфейсе RetweetService с полной JavaDoc документацией. Реализован метод removeRetweet() в RetweetServiceImpl с использованием @Transactional для атомарности операции. Метод выполняет: валидацию через retweetValidator.validateForRemoveRetweet(), поиск ретвита через retweetRepository.findByTweetIdAndUserId(), удаление ретвита через retweetRepository.delete(), получение твита и декремент счетчика через tweet.decrementRetweetsCount(), сохранение обновленного твита. Добавлен метод decrementRetweetsCount() в Tweet entity по аналогии с decrementLikesCount() для защиты от отрицательных значений. Код проверен линтером, ошибок нет. Выполнено: 2025-01-27"
- [x] (P1) #8: [2025-01-27 12:30] Controller метод для эндпоинта — Добавить метод removeRetweet в RetweetApi и RetweetController
  acceptance: "Метод removeRetweet добавлен в RetweetApi интерфейс с OpenAPI аннотациями и в RetweetController с @LoggableRequest, возвращает 204 No Content"
  note: "Реализован метод removeRetweet() в интерфейсе RetweetApi с полной OpenAPI документацией: @Operation с summary и description, @ApiResponses для всех статус-кодов (204, 400, 409) с @ExampleObject для всех сценариев ошибок (Validation error, Invalid UUID format, Tweet Not Found, User Not Found, Retweet Not Found), @Parameter для всех параметров с описаниями и примерами. Реализован метод removeRetweet() в RetweetController с @DeleteMapping(\"/{tweetId}/retweet\"), @LoggableRequest, @Valid для валидации request body, ResponseEntity<Void> с HttpStatus.NO_CONTENT. Код проверен линтером, ошибок нет. Выполнено: 2025-01-27"
- [x] (P1) #9: [2025-01-27 12:30] JavaDoc для эндпоинта — Добавить JavaDoc для всех методов
  acceptance: "JavaDoc добавлен для всех методов эндпоинта с @author geron, @version 1.0, @param, @return, @throws"
  note: "JavaDoc добавлен для всех методов эндпоинта DELETE /api/v1/tweets/{tweetId}/retweet. Обновлены методы: RetweetServiceImpl.removeRetweet(), RetweetController.removeRetweet(), RetweetValidatorImpl.validateForRemoveRetweet(), RetweetApi.removeRetweet(). Все методы содержат полную документацию с @param, @return, @throws согласно STANDART_JAVADOC.md. JavaDoc на английском языке. Выполнено: 2025-01-27"
- [x] (P1) #10: [2025-01-27 12:45] Unit тесты для эндпоинта — Создать unit тесты для Service и Validator
  acceptance: "Unit тесты для Service и Validator методов с учетом STANDART_TEST.md (naming pattern, @Nested, AssertJ)"
  note: "Созданы unit тесты для всех методов эндпоинта DELETE /api/v1/tweets/{tweetId}/retweet. RetweetServiceImplTest: добавлен @Nested класс RemoveRetweetTests с 6 тестами для метода removeRetweet (успешный сценарий, проверка вызовов зависимостей, проверка декремента счетчика, ошибки валидации, ретвит не найден после валидации, твит не найден после валидации). RetweetValidatorImplTest: добавлен @Nested класс ValidateForRemoveRetweetTests с 7 тестами для метода validateForRemoveRetweet (успешный сценарий, tweetId null, твит не найден, requestDto null, userId null, пользователь не существует, ретвит не существует). Все тесты следуют стандартам STANDART_TEST.md: паттерн именования methodName_WhenCondition_ShouldExpectedResult, использование @Nested для группировки, AssertJ для assertions, Mockito для моков, паттерн AAA (Arrange-Act-Assert). Код проверен линтером, ошибок нет. Выполнено: 2025-01-27"
- [x] (P2) #11: [2025-01-27 13:00] Integration тесты для эндпоинта — Создать integration тесты с MockMvc
  acceptance: "Integration тесты для эндпоинта с MockMvc, все статус-коды проверены (204, 400, 404)"
  note: "Созданы integration тесты для эндпоинта DELETE /api/v1/tweets/{tweetId}/retweet в RetweetControllerTest. Добавлен @Nested класс RemoveRetweetTests с 8 тестами: removeRetweet_WithValidData_ShouldReturn204NoContent (успешный сценарий с проверкой удаления ретвита и декремента счетчика), removeRetweet_WithNullUserId_ShouldReturn400BadRequest (валидация null userId), removeRetweet_WithMissingBody_ShouldReturn400BadRequest (валидация отсутствующего body), removeRetweet_WhenTweetDoesNotExist_ShouldReturn409Conflict (твит не найден), removeRetweet_WhenUserDoesNotExist_ShouldReturn409Conflict (пользователь не существует), removeRetweet_WhenRetweetDoesNotExist_ShouldReturn409Conflict (ретвит не найден), removeRetweet_ShouldDecrementRetweetsCount (проверка декремента счетчика при множественных удалениях), removeRetweet_WhenUsersApiReturns500_ShouldReturn409Conflict (ошибка users-api). Все тесты используют MockMvc, проверяют HTTP статус-коды (204, 400, 409), проверяют состояние БД после операций, используют WireMock для мокирования users-api. Тесты следуют паттерну RemoveLikeTests из LikeControllerTest. Код проверен линтером, ошибок нет. Выполнено: 2025-01-27"
- [x] (P1) #12: [2025-01-27 12:30] Swagger документация для эндпоинта — Добавить OpenAPI аннотации
  acceptance: "OpenAPI документация для эндпоинта полная с @ExampleObject для всех сценариев (успех 204, ошибки валидации, ошибки бизнес-правил)"
  note: "Проверена OpenAPI документация для метода removeRetweet в интерфейсе RetweetApi. Документация полная и соответствует стандартам STANDART_SWAGGER.md: @Operation с summary и description, @ApiResponse для всех статус-кодов (204, 400, 409) с @ExampleObject для всех сценариев ошибок (Validation error, Invalid UUID format, Tweet Not Found, User Not Found, Retweet Not Found), @Parameter для всех параметров с описаниями и примерами. Все примеры используют формат RFC 7807 Problem Details для ошибок. Выполнено: 2025-01-27"

### Финальная инфраструктура
- [x] (P2) #13: [2025-01-27 13:15] Обновление README.md — Обновить документацию сервиса
  acceptance: "README обновлен с учетом STANDART_README.md, эндпоинт DELETE документирован в разделе REST API и Примеры использования"
  note: "Обновлен README.md для сервиса tweet-api. Добавлено: в раздел 'Основные возможности' - 'Убрать ретвит твита', в таблицу эндпоинтов - 'DELETE /{tweetId}/retweet', новый раздел '10. Убрать ретвит твита' с полным описанием эндпоинта (параметры, валидация, бизнес-правила, ответы, примеры ошибок), в раздел 'Бизнес-логика' - метод removeRetweet() в RetweetService с описанием логики и особенностей, в раздел 'Ключевые бизнес-правила для ретвитов' - пункт '7. Удаление ретвита' с описанием логики декремента счетчика, в раздел 'Слой валидации' - подраздел 'Убрать ретвит твита (REMOVE_RETWEET)' с описанием всех этапов валидации, в раздел 'Примеры использования' - пример curl запроса для removeRetweet с примерами ответов для всех сценариев (204, 409). Вся документация на русском языке, соответствует стандартам STANDART_README.md. Выполнено: 2025-01-27"
- [x] (P2) #14: [2025-01-27 13:30] Обновление Postman коллекции — Добавить запрос в коллекцию
  acceptance: "Добавлен запрос 'remove retweet' с примерами ответов для всех сценариев (204, 400, 404), обновлены переменные окружения"
  note: "Обновлена Postman коллекция twitter-tweet-api.postman_collection.json. Добавлен запрос 'remove retweet' (DELETE /api/v1/tweets/{{tweetId}}/retweet) с полным описанием, использованием переменных {{baseUrl}}, {{tweetId}}, {{userId}}, заголовками Content-Type и Accept, телом запроса с RetweetRequestDto. Добавлены примеры ответов для всех сценариев: retweet removed (204 No Content), validation error - null userId (400 Bad Request), validation error - missing body (400 Bad Request), tweet not found error (409 Conflict), user not exists error (409 Conflict), retweet not found error (409 Conflict), invalid uuid format error (400 Bad Request). Все примеры ответов следуют RFC 7807 Problem Details для ошибок. Обновлено описание коллекции в секции info для упоминания функциональности удаления ретвита. Запрос следует структуре 'remove like' и стандартам STANDART_POSTMAN.md. Выполнено: 2025-01-27"
- [x] (P1) #15: [2025-01-27 13:45] Проверка соответствия стандартам — Проверить все стандарты
  acceptance: "Все стандарты проверены, код соответствует требованиям (STANDART_CODE, STANDART_PROJECT, STANDART_TEST, STANDART_JAVADOC, STANDART_SWAGGER)"
  note: "Проведена полная проверка соответствия всех стандартов проекта. STANDART_CODE.md: ✅ Использование Records для DTOs (RetweetRequestDto), ✅ Lombok аннотации (@Slf4j, @RequiredArgsConstructor, @Component, @Service), ✅ @Transactional для service методов (removeRetweet), ✅ Правильная структура пакетов (controller, service, validation, repository), ✅ Использование MapStruct (RetweetMapper), ✅ Bean Validation (@Valid, @NotNull), ✅ Правильная обработка исключений (BusinessRuleValidationException из common-lib), ✅ Использование Gateway паттерна (UserGateway). STANDART_PROJECT.md: ✅ @LoggableRequest на всех controller методах (removeRetweet), ✅ Использование исключений из common-lib (BusinessRuleValidationException), ✅ Правильная структура Gateway для межсервисного взаимодействия. STANDART_JAVADOC.md: ✅ Все публичные классы и методы имеют JavaDoc (RetweetValidator, RetweetValidatorImpl, RetweetService, RetweetServiceImpl, RetweetApi, RetweetController), ✅ Использование @author geron и @version 1.0, ✅ Полная документация с @param, @return, @throws, ✅ Использование @see для реализации интерфейсов. STANDART_TEST.md: ✅ Использование паттерна methodName_WhenCondition_ShouldExpectedResult, ✅ Использование @Nested для группировки тестов (RemoveRetweetTests, ValidateForRemoveRetweetTests), ✅ Использование AssertJ для assertions, ✅ Использование Mockito для моков, ✅ Паттерн AAA (Arrange-Act-Assert), ✅ Покрытие всех сценариев (успешные и ошибочные). STANDART_SWAGGER.md: ✅ Полная OpenAPI документация с @Operation, @ApiResponses, ✅ Использование @ExampleObject для всех сценариев (204, 400, 409), ✅ Использование @Parameter для всех параметров, ✅ Правильный формат ошибок (RFC 7807 Problem Details), ✅ Правильные Content-Type (application/problem+json для ошибок). Все стандарты соблюдены. Выполнено: 2025-01-27"

## Assumptions
- Используется существующий DTO `RetweetRequestDto` для DELETE операции (содержит userId)
- DELETE операция возвращает 204 No Content без тела ответа
- Удаление ретвита должно быть атомарным (транзакция)
- Счетчик ретвитов твита обновляется автоматически при удалении (decrementRetweetsCount)
- Валидация проверяет существование ретвита перед удалением
- Пользователь может убрать только свой ретвит (проверка через userId в request body)
- RetweetRepository уже имеет метод `findByTweetIdAndUserId()` для поиска ретвита
- Tweet entity имеет метод `decrementRetweetsCount()` для декремента счетчика (добавлен в шаге #7)

## Risks
- **Технические риски**: 
  - Race condition при одновременном удалении ретвита несколькими запросами (митигация: транзакция с правильным уровнем изоляции)
  - Несогласованность счетчика ретвитов при ошибках (митигация: транзакция, проверка в тестах)
  - Неправильная обработка случая, когда ретвит уже удален (митигация: валидация существования ретвита перед удалением)
- **Организационные риски**: 
  - Недостаточное покрытие тестами (митигация: обязательные unit и integration тесты)
- **Зависимости**: 
  - Интеграция с users-api для проверки существования пользователя (митигация: Circuit Breaker, fallback стратегии)

## Metrics & Success Criteria
- Все unit тесты проходят (покрытие > 80% для новых методов)
- Все integration тесты проходят
- Эндпоинт возвращает правильные HTTP статус-коды (204, 400, 404)
- Счетчик ретвитов корректно обновляется в БД (decrementRetweetsCount)
- OpenAPI документация полная и корректная
- Postman коллекция обновлена с примерами
- README обновлен на русском языке
- Код соответствует всем стандартам проекта
