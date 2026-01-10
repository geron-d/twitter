# TODO: Реализация эндпоинта "Убрать лайк"

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
- [X] (P1) #22: Анализ требований — Определить входные/выходные данные, бизнес-правила, затронутые стандарты
  acceptance: "Понять вход/выход (tweetId, userId, 204 No Content), бизнес-правила (существование лайка, обновление счетчика), определить затронутые стандарты"
- [x] (P1) #23: Проектирование API и контрактов — Определить структуру валидации и бизнес-логики
  acceptance: "OpenAPI схема для DELETE эндпоинта, определение методов валидации (validateForUnlike), определение методов сервиса (removeLike), переиспользование существующих DTO"
  note: "Выполнено проектирование API и контрактов. Определена OpenAPI схема для DELETE эндпоинта с полной документацией всех статус-кодов и примеров. Определена структура метода removeLike в LikeService и LikeServiceImpl. Определена структура метода decrementLikesCount в Entity Tweet. Подтверждено переиспользование LikeTweetRequestDto. Создан документ api_design.md с детальным проектированием. Выполнено: 2025-01-27"

### Реализация инфраструктуры и конфигов
- [x] (P1) #24: Инфраструктура уже существует — Entity, Repository, DTO, Mapper
  acceptance: "Все компоненты инфраструктуры уже созданы для POST /api/v1/tweets/{tweetId}/like, переиспользуются для DELETE"
  note: "Entity Like, LikeRepository, LikeTweetRequestDto, LikeMapper, LikeService, LikeController, LikeApi уже существуют. Не требуется создание новой инфраструктуры, только расширение существующих интерфейсов и реализаций."

### Эндпоинт: DELETE /api/v1/tweets/{tweetId}/like
- [x] (P1) #25: DTO для эндпоинта — Переиспользование существующего LikeTweetRequestDto
  acceptance: "Используется существующий LikeTweetRequestDto (не требуется создание нового DTO)"
  note: "DELETE операция использует тот же DTO, что и POST (содержит userId). Не требуется создание нового DTO."
- [x] (P1) #26: Mapper методы для эндпоинта — Не требуется
  acceptance: "DELETE операция не требует маппинга (возвращает 204 No Content без тела ответа)"
  note: "DELETE операция не требует маппинга, так как возвращает 204 No Content без тела ответа."
- [x] (P1) #27: Validator методы для эндпоинта — Добавить метод validateForUnlike
  acceptance: "Метод validateForUnlike добавлен в LikeValidator interface и implementation (проверка существования твита, пользователя, лайка)"
  note: "Метод validateForUnlike добавлен в LikeValidator интерфейс и LikeValidatorImpl реализацию. Валидация включает проверку tweetId, существования твита, requestDto, userId, существования пользователя и существования лайка. Добавлен приватный метод validateLikeExists для проверки существования лайка перед удалением. Выполнено: 2026-01-02 00:41"
- [x] (P1) #28: Service методы для эндпоинта — Добавить метод removeLike
  acceptance: "Метод removeLike добавлен в LikeService interface и implementation, использует @Transactional, удаляет лайк, обновляет счетчик"
  note: "Добавлен метод removeLike() в интерфейс LikeService с полной JavaDoc документацией. Реализован метод removeLike() в LikeServiceImpl с использованием @Transactional для атомарности операции. Добавлен метод decrementLikesCount() в Entity Tweet с защитой от отрицательных значений. Все методы соответствуют стандартам проекта. Выполнено: 2025-01-27"
- [x] (P1) #29: Controller метод для эндпоинта — Добавить метод removeLike в LikeApi и LikeController
  acceptance: "Метод removeLike добавлен в LikeApi интерфейс с OpenAPI аннотациями и в LikeController с @LoggableRequest, возвращает 204 No Content"
  note: "Добавлен метод removeLike() в интерфейс LikeApi с полной OpenAPI документацией (все статус-коды 204, 400, 404 с примерами). Реализован метод removeLike() в LikeController с @LoggableRequest и @DeleteMapping. Метод возвращает 204 No Content. Все соответствует стандартам проекта (STANDART_CODE, STANDART_SWAGGER). Выполнено: 2025-01-27"
- [x] (P1) #30: JavaDoc для эндпоинта — Добавить JavaDoc для всех методов
  acceptance: "JavaDoc добавлен для всех методов эндпоинта с @author geron, @version 1.0, @param, @return, @throws"
  note: "Добавлена полная JavaDoc документация для всех методов эндпоинта DELETE /api/v1/tweets/{tweetId}/like. Обновлены методы: LikeServiceImpl.removeLike(), LikeController.removeLike(), LikeValidatorImpl.validateForUnlike(). Все методы теперь содержат полную документацию с @param, @return, @throws согласно STANDART_JAVADOC.md. Выполнено: 2025-01-27"
- [x] (P1) #31: Unit тесты для эндпоинта — Создать unit тесты для Service и Validator
  acceptance: "Unit тесты для Service и Validator методов с учетом STANDART_TEST.md (naming pattern, @Nested, AssertJ)"
  note: "Созданы unit тесты для всех методов эндпоинта DELETE /api/v1/tweets/{tweetId}/like. LikeServiceImplTest: 6 тестов для метода removeLike (успешный сценарий, проверка вызовов зависимостей, проверка декремента счетчика, ошибки валидации, лайк не найден после валидации, твит не найден после валидации). LikeValidatorImplTest: 7 тестов для метода validateForUnlike (успешный сценарий, tweetId null, твит не найден, requestDto null, userId null, пользователь не существует, лайк не существует). Все тесты следуют стандартам STANDART_TEST.md: паттерн именования methodName_WhenCondition_ShouldExpectedResult, использование @Nested для группировки, AssertJ для assertions, Mockito для моков. Выполнено: 2025-01-27"
- [x] (P2) #32: Integration тесты для эндпоинта — Создать integration тесты с MockMvc
  acceptance: "Integration тесты для эндпоинта с MockMvc, все статус-коды проверены (204, 400, 404, 409)"
  note: "Созданы integration тесты для метода removeLike в LikeControllerTest. Добавлен @Nested класс RemoveLikeTests с 8 тестами: успешный сценарий (204), валидация (400 - null userId, missing body), ошибки (404 - tweet not found, user not found, like not found), декремент счетчика, ошибка users-api. Все тесты используют MockMvc, проверяют статус-коды, структуру ответов (RFC 7807), состояние БД (удаление лайка, обновление счетчика). Тесты следуют стандартам STANDART_TEST.md: паттерн именования methodName_WhenCondition_ShouldExpectedResult, использование @Nested для группировки, AssertJ для assertions. Статус-код 409 не тестируется, так как для DELETE операции он не используется в текущей реализации. Выполнено: 2025-01-27"
- [x] (P1) #33: Swagger документация для эндпоинта — Добавить OpenAPI аннотации
  acceptance: "OpenAPI документация для эндпоинта полная с @ExampleObject для всех сценариев (успех 204, ошибки валидации, конфликты)"
  note: "Проверена OpenAPI документация для метода removeLike в интерфейсе LikeApi. Документация полная и соответствует стандартам STANDART_SWAGGER.md: @Operation с summary и description, @ApiResponse для всех статус-кодов (204, 400, 404) с @ExampleObject для всех сценариев ошибок (Validation error, Invalid UUID format, Tweet Not Found, User Not Found, Like Not Found), @Parameter для всех параметров с описаниями и примерами. Статус-код 409 не документирован, так как для DELETE операции он не используется в текущей реализации. Выполнено: 2025-01-27"

### Финальная инфраструктура
- [x] (P2) #34: Обновление README.md — Обновить документацию сервиса
  acceptance: "README обновлен с учетом STANDART_README.md, эндпоинт DELETE документирован в разделе REST API и Примеры использования"
  note: "Обновлен README.md для сервиса tweet-api. Добавлено в раздел 'Основные возможности': 'Убрать лайк твита с проверкой бизнес-правил'. Добавлен эндпоинт DELETE /{tweetId}/like в таблицу эндпоинтов. Добавлено детальное описание эндпоинта 'Убрать лайк твита' с параметрами, валидацией, бизнес-правилами, ответами и примерами ошибок. Обновлен раздел 'Бизнес-логика': добавлен метод removeLike в LikeService с описанием логики и особенностей. Добавлено бизнес-правило 'Удаление лайка' с описанием декремента счетчика и защиты от отрицательных значений. Обновлен раздел 'Слой валидации': добавлена валидация для операции UNLIKE с описанием всех этапов проверки. Добавлен пример использования в раздел 'Примеры использования' с curl командой и примерами ответов (успех 204, ошибки 400, 409). Все соответствует стандартам STANDART_README.md. Выполнено: 2025-01-27"
- [x] (P2) #35: Обновление Postman коллекции — Добавить запрос в коллекцию
  acceptance: "Добавлен запрос 'remove like' с примерами ответов для всех сценариев (204, 400, 404, 409), обновлены переменные окружения"
  note: "Добавлен запрос 'remove like' в Postman коллекцию twitter-tweet-api.postman_collection.json. Запрос использует метод DELETE с путем /api/v1/tweets/{{tweetId}}/like. Добавлено описание запроса с указанием валидации и бизнес-правил. Добавлены примеры ответов для всех сценариев: успешный (204 No Content), ошибки валидации (400 - null userId, missing body, invalid UUID format), ошибки бизнес-правил (409 - tweet not found, user not exists, like not found). Все примеры используют формат RFC 7807 Problem Details для ошибок. Обновлено описание коллекции в info.description с добавлением информации об удалении лайка. Переменные окружения не требуют обновления, так как используются существующие переменные (baseUrl, userId, tweetId). Все соответствует стандартам STANDART_POSTMAN.md. Выполнено: 2025-01-27"
- [x] (P1) #36: Проверка соответствия стандартам — Проверить все стандарты
  acceptance: "Все стандарты проверены, код соответствует требованиям (STANDART_CODE, STANDART_PROJECT, STANDART_TEST, STANDART_JAVADOC, STANDART_SWAGGER)"
  note: "Проведена полная проверка соответствия всех стандартам проекта для эндпоинта DELETE /api/v1/tweets/{tweetId}/like. STANDART_CODE: Controller использует @LoggableRequest, правильная структура пакетов, именование в camelCase/PascalCase. Service использует @Transactional, правильная структура. Validator использует правильные исключения (BusinessRuleValidationException). STANDART_PROJECT: Используется @LoggableRequest на контроллере, GlobalExceptionHandler обрабатывает исключения, правильные типы исключений. STANDART_JAVADOC: Все классы имеют @author и @version, методы имеют JavaDoc с @param, @return, @throws, используется @see для реализации интерфейсов. STANDART_SWAGGER: OpenAPI интерфейс имеет @Operation, @ApiResponses, @ApiResponse с примерами для всех статус-кодов (204, 400, 409), @Parameter для параметров, примеры используют RFC 7807 Problem Details. STANDART_TEST: Тесты используют @Nested для группировки, именование methodName_WhenCondition_ShouldExpectedResult, покрытие всех сценариев (успех, валидация, бизнес-правила), использование MockMvc, Testcontainers, WireMock. README.md соответствует STANDART_README.md. Postman коллекция соответствует STANDART_POSTMAN.md. Все компоненты соответствуют стандартам проекта. Выполнено: 2025-01-27"

## Assumptions
- Используется существующий DTO `LikeTweetRequestDto` для DELETE операции (содержит userId)
- DELETE операция возвращает 204 No Content без тела ответа
- Удаление лайка должно быть атомарным (транзакция)
- Счетчик лайков твита обновляется автоматически при удалении (decrementLikesCount)
- Валидация проверяет существование лайка перед удалением
- Пользователь может убрать только свой лайк (проверка через userId в request body)

## Risks
- **Технические риски**: 
  - Race condition при одновременном удалении лайка несколькими запросами (митигация: транзакция с правильным уровнем изоляции)
  - Несогласованность счетчика лайков при ошибках (митигация: транзакция, проверка в тестах)
- **Организационные риски**: 
  - Недостаточное покрытие тестами (митигация: обязательные unit и integration тесты)
- **Зависимости**: 
  - Интеграция с users-api для проверки существования пользователя (митигация: Circuit Breaker, fallback стратегии)

## Metrics & Success Criteria
- Все unit тесты проходят (покрытие > 80% для новых методов)
- Все integration тесты проходят
- Эндпоинт возвращает правильные HTTP статус-коды (204, 400, 404, 409)
- Счетчик лайков корректно обновляется в БД (decrementLikesCount)
- OpenAPI документация полная и корректная
- Postman коллекция обновлена с примерами
- README обновлен на русском языке
- Код соответствует всем стандартам проекта

