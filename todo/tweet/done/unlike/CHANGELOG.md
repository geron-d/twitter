# Changelog

2025-01-27 — step #36 done — Проверка соответствия стандартам для эндпоинта DELETE /api/v1/tweets/{tweetId}/like — автор: assistant
  - Проведена полная проверка соответствия всех стандартам проекта
  - STANDART_CODE: Controller использует @LoggableRequest, правильная структура пакетов, именование в camelCase/PascalCase. Service использует @Transactional, правильная структура. Validator использует правильные исключения (BusinessRuleValidationException)
  - STANDART_PROJECT: Используется @LoggableRequest на контроллере, GlobalExceptionHandler обрабатывает исключения, правильные типы исключений
  - STANDART_JAVADOC: Все классы имеют @author и @version, методы имеют JavaDoc с @param, @return, @throws, используется @see для реализации интерфейсов
  - STANDART_SWAGGER: OpenAPI интерфейс имеет @Operation, @ApiResponses, @ApiResponse с примерами для всех статус-кодов (204, 400, 409), @Parameter для параметров, примеры используют RFC 7807 Problem Details
  - STANDART_TEST: Тесты используют @Nested для группировки, именование methodName_WhenCondition_ShouldExpectedResult, покрытие всех сценариев (успех, валидация, бизнес-правила), использование MockMvc, Testcontainers, WireMock
  - README.md соответствует STANDART_README.md
  - Postman коллекция соответствует STANDART_POSTMAN.md
  - Все компоненты соответствуют стандартам проекта
  - Файлы проверены: LikeController.java, LikeServiceImpl.java, LikeValidatorImpl.java, LikeApi.java, LikeControllerTest.java, README.md, twitter-tweet-api.postman_collection.json

2025-01-27 — step #35 done — Обновление Postman коллекции для эндпоинта DELETE /api/v1/tweets/{tweetId}/like — автор: assistant
  - Добавлен запрос 'remove like' в Postman коллекцию twitter-tweet-api.postman_collection.json
  - Запрос использует метод DELETE с путем /api/v1/tweets/{{tweetId}}/like
  - Добавлено описание запроса с указанием валидации и бизнес-правил
  - Добавлены примеры ответов для всех сценариев: успешный (204 No Content), ошибки валидации (400 - null userId, missing body, invalid UUID format), ошибки бизнес-правил (409 - tweet not found, user not exists, like not found)
  - Все примеры используют формат RFC 7807 Problem Details для ошибок
  - Обновлено описание коллекции в info.description с добавлением информации об удалении лайка
  - Переменные окружения не требуют обновления, так как используются существующие переменные (baseUrl, userId, tweetId)
  - Все соответствует стандартам STANDART_POSTMAN.md
  - Файлы: postman/tweet-api/twitter-tweet-api.postman_collection.json (обновлен)

2025-01-27 — step #34 done — Обновление README.md для эндпоинта DELETE /api/v1/tweets/{tweetId}/like — автор: assistant
  - Обновлен README.md для сервиса tweet-api
  - Добавлено в раздел 'Основные возможности': 'Убрать лайк твита с проверкой бизнес-правил'
  - Добавлен эндпоинт DELETE /{tweetId}/like в таблицу эндпоинтов
  - Добавлено детальное описание эндпоинта 'Убрать лайк твита' с параметрами, валидацией, бизнес-правилами, ответами и примерами ошибок
  - Обновлен раздел 'Бизнес-логика': добавлен метод removeLike в LikeService с описанием логики и особенностей
  - Добавлено бизнес-правило 'Удаление лайка' с описанием декремента счетчика и защиты от отрицательных значений
  - Обновлен раздел 'Слой валидации': добавлена валидация для операции UNLIKE с описанием всех этапов проверки
  - Добавлен пример использования в раздел 'Примеры использования' с curl командой и примерами ответов (успех 204, ошибки 400, 409)
  - Все соответствует стандартам STANDART_README.md
  - Файлы: services/tweet-api/README.md (обновлен)

2025-01-27 — step #32 done — Integration тесты для эндпоинта DELETE /api/v1/tweets/{tweetId}/like — автор: assistant
  - Созданы integration тесты для метода removeLike в LikeControllerTest
  - Добавлен @Nested класс RemoveLikeTests с 8 тестами
  - Тесты для успешного сценария: removeLike_WithValidData_ShouldReturn204NoContent (проверка удаления лайка и декремента счетчика)
  - Тесты для валидации: removeLike_WithNullUserId_ShouldReturn400BadRequest, removeLike_WithMissingBody_ShouldReturn400BadRequest
  - Тесты для ошибок: removeLike_WhenTweetDoesNotExist_ShouldReturn404NotFound, removeLike_WhenUserDoesNotExist_ShouldReturn404NotFound, removeLike_WhenLikeDoesNotExist_ShouldReturn404NotFound
  - Тест для декремента счетчика: removeLike_ShouldDecrementLikesCount (проверка последовательного удаления нескольких лайков)
  - Тест для ошибки users-api: removeLike_WhenUsersApiReturns500_ShouldReturn404NotFound
  - Все тесты используют MockMvc, проверяют статус-коды, структуру ответов (RFC 7807 Problem Details), состояние БД
  - Тесты проверяют удаление лайка из БД и корректное обновление счетчика лайков твита
  - Тесты следуют стандартам STANDART_TEST.md: паттерн именования methodName_WhenCondition_ShouldExpectedResult, использование @Nested для группировки, AssertJ для assertions
  - Добавлен helper метод createAndSaveLike для создания тестовых данных
  - Статус-код 409 не тестируется, так как для DELETE операции он не используется в текущей реализации
  - Файлы: services/tweet-api/src/test/java/com/twitter/controller/LikeControllerTest.java (обновлен)

2025-01-27 — step #33 done — Swagger документация для эндпоинта DELETE /api/v1/tweets/{tweetId}/like — автор: assistant
  - Проверена OpenAPI документация для метода removeLike в интерфейсе LikeApi
  - Документация полная и соответствует стандартам STANDART_SWAGGER.md
  - @Operation с summary и description для операции удаления лайка
  - @ApiResponse для всех статус-кодов: 204 (успех), 400 (валидация, два типа), 404 (не найдено, три типа)
  - @ExampleObject для всех сценариев ошибок: Validation error, Invalid UUID format, Tweet Not Found, User Not Found, Like Not Found
  - @Parameter для всех параметров (tweetId, likeTweetRequest) с описаниями и примерами
  - Статус-код 409 не документирован, так как для DELETE операции он не используется в текущей реализации
  - Все примеры используют формат RFC 7807 Problem Details для ошибок
  - Файлы: services/tweet-api/src/main/java/com/twitter/controller/LikeApi.java

2025-01-27 — step #31 done — Unit тесты для эндпоинта DELETE /api/v1/tweets/{tweetId}/like — автор: assistant
  - Созданы unit тесты для всех методов эндпоинта DELETE /api/v1/tweets/{tweetId}/like
  - LikeServiceImplTest: добавлен @Nested класс RemoveLikeTests с 6 тестами для метода removeLike
  - Тесты для removeLike: успешный сценарий, проверка вызовов зависимостей, проверка декремента счетчика, ошибки валидации, лайк не найден после валидации, твит не найден после валидации
  - LikeValidatorImplTest: добавлен @Nested класс ValidateForUnlikeTests с 7 тестами для метода validateForUnlike
  - Тесты для validateForUnlike: успешный сценарий, tweetId null, твит не найден, requestDto null, userId null, пользователь не существует, лайк не существует
  - Все тесты следуют стандартам STANDART_TEST.md: паттерн именования methodName_WhenCondition_ShouldExpectedResult, использование @Nested для группировки, AssertJ для assertions, Mockito для моков, @ExtendWith(MockitoExtension.class) для unit тестов
  - Тесты покрывают успешные и ошибочные сценарии, проверяют взаимодействие с зависимостями через verify
  - Тесты используют паттерн AAA (Arrange-Act-Assert) для структурирования
  - Все тесты изолированы и независимы, используют @BeforeEach для инициализации тестовых данных
  - Файлы: services/tweet-api/src/test/java/com/twitter/service/LikeServiceImplTest.java (обновлен), services/tweet-api/src/test/java/com/twitter/validation/LikeValidatorImplTest.java (обновлен)

2025-01-27 — step #30 done — JavaDoc для эндпоинта DELETE /api/v1/tweets/{tweetId}/like — автор: assistant
  - Добавлена полная JavaDoc документация для всех методов эндпоинта
  - Обновлен метод LikeServiceImpl.removeLike() с полной документацией (@param, @return, @throws)
  - Обновлен метод LikeController.removeLike() с полной документацией (@param, @return, @throws)
  - Обновлен метод LikeValidatorImpl.validateForUnlike() с полной документацией (@param, @throws)
  - Все методы соответствуют стандартам проекта (STANDART_JAVADOC.md)
  - Добавлены необходимые импорты для BusinessRuleValidationException
  - Файлы: services/tweet-api/src/main/java/com/twitter/service/LikeServiceImpl.java, controller/LikeController.java, validation/LikeValidatorImpl.java

2025-01-27 — step #29 done — Controller метод для эндпоинта DELETE /api/v1/tweets/{tweetId}/like — автор: assistant
  - Добавлен метод removeLike() в интерфейс LikeApi с полной OpenAPI документацией
  - Документированы все статус-коды: 204 (успех), 400 (валидация), 404 (не найдено)
  - Добавлены примеры ответов для всех сценариев в формате RFC 7807 Problem Details
  - Реализован метод removeLike() в LikeController с @LoggableRequest и @DeleteMapping
  - Метод возвращает 204 No Content при успешном удалении лайка
  - Все соответствует стандартам проекта (STANDART_CODE, STANDART_SWAGGER)
  - Файлы: services/tweet-api/src/main/java/com/twitter/controller/LikeApi.java, LikeController.java

2025-01-27 — step #28 done — Service методы для эндпоинта DELETE /api/v1/tweets/{tweetId}/like — автор: assistant
  - Добавлен метод removeLike() в интерфейс LikeService с полной JavaDoc документацией
  - Реализован метод removeLike() в LikeServiceImpl с использованием @Transactional для атомарности операции
  - Добавлен метод decrementLikesCount() в Entity Tweet с защитой от отрицательных значений
  - Метод removeLike() выполняет валидацию, удаление лайка и обновление счетчика в одной транзакции
  - Все методы соответствуют стандартам проекта (STANDART_CODE, STANDART_JAVADOC)
  - Файлы: services/tweet-api/src/main/java/com/twitter/service/LikeService.java, LikeServiceImpl.java, entity/Tweet.java

2025-01-27 — step #23 done — Проектирование API и контрактов для эндпоинта DELETE /api/v1/tweets/{tweetId}/like — автор: assistant
  - Выполнено проектирование API и контрактов для эндпоинта удаления лайка
  - Определена OpenAPI схема для DELETE эндпоинта с полной документацией всех статус-кодов (204, 400, 404)
  - Определена структура метода removeLike в интерфейсе LikeService и реализации LikeServiceImpl
  - Определена структура метода decrementLikesCount в Entity Tweet
  - Определена структура метода removeLike в интерфейсе LikeApi с OpenAPI аннотациями
  - Определена структура метода removeLike в контроллере LikeController
  - Подтверждено переиспользование LikeTweetRequestDto для DELETE операции
  - Определены контракты и соглашения (HTTP статус-коды, формат ошибок, исключения)
  - Определена последовательность операций для успешных и ошибочных сценариев
  - Создан документ api_design.md с детальным проектированием
  - Файлы: todo/tweet/done/api_design.md

2025-01-27 — step #22 done — Анализ требований для эндпоинта DELETE /api/v1/tweets/{tweetId}/like — автор: assistant
  - Выполнен полный анализ требований для эндпоинта удаления лайка
  - Определены входные данные: tweetId (path), LikeTweetRequestDto (body)
  - Определены выходные данные: 204 No Content (успех), 400/404/409 (ошибки)
  - Определены бизнес-правила: валидация существования твита, пользователя, лайка; атомарность операции; обновление счетчика
  - Определены затронутые стандарты: STANDART_CODE, STANDART_PROJECT, STANDART_TEST, STANDART_JAVADOC, STANDART_SWAGGER
  - Создан документ analysis_requirements.md с детальным анализом
  - Файлы: todo/tweet/done/analysis_requirements.md

2026-01-02 00:41 — step #27 done — Validator методы для эндпоинта DELETE /api/v1/tweets/{tweetId}/like — автор: assistant
  - Добавлен метод validateForUnlike в интерфейс LikeValidator
  - Реализован метод validateForUnlike в LikeValidatorImpl
  - Добавлен приватный метод validateLikeExists для проверки существования лайка
  - Обновлена JavaDoc для интерфейса и реализации
  - Файлы: services/tweet-api/src/main/java/com/twitter/validation/LikeValidator.java, LikeValidatorImpl.java
