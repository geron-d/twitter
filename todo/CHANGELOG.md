# Changelog

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
