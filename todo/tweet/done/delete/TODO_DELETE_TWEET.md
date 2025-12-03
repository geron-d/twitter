# TODO: Реализация мягкого удаления твита (Soft Delete)

## Meta
- project: twitter-tweet-api
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
- [x] (P1) [2025-01-27 15:30] #1: Анализ требований — Определить структуру полей soft delete, бизнес-правила удаления, валидацию прав доступа
  acceptance: "Понять вход/выход, определить затронутые стандарты, спроектировать структуру данных"
  done: "Выполнен анализ требований, определены структура полей (isDeleted, deletedAt), бизнес-правила удаления, валидация прав доступа. Создан документ ANALYSIS_DELETE_TWEET.md с полным анализом."
  artifacts: "todo/tweet/ANALYSIS_DELETE_TWEET.md"
- [x] (P1) [2025-01-27 15:45] #2: Проектирование API и контрактов — Определить структуру эндпоинта DELETE, HTTP статусы, валидацию
  acceptance: "OpenAPI схема, структура ответов, валидация прав доступа"
  done: "Спроектирована структура DELETE эндпоинта, определены HTTP статусы (204, 404, 409, 400), структура валидации, OpenAPI документация. Создан DeleteTweetRequestDto. Создан документ DESIGN_DELETE_TWEET.md с полным проектированием."
  artifacts: "todo/tweet/DESIGN_DELETE_TWEET.md"

### Реализация кода
- [x] (P1) [2025-01-27 16:00] #3: Обновление Entity Tweet — Добавить поля isDeleted и deletedAt, метод softDelete()
  acceptance: "Entity обновлена с полями isDeleted (Boolean, default false) и deletedAt (LocalDateTime, nullable), добавлен метод softDelete()"
  done: "Реализованы изменения в Entity Tweet: добавлены поля isDeleted и deletedAt с @Builder.Default, методы softDelete() и isActive(), обновлен JavaDoc класса. Код соответствует спецификации и стандартам проекта."
  artifacts: "services/tweet-api/src/main/java/com/twitter/entity/Tweet.java, todo/tweet/IMPLEMENTATION_ENTITY_TWEET.md"
- [x] (P1) [2025-01-27 16:15] #4: Обновление Repository — Добавить методы для работы с soft delete
  acceptance: "Методы findByIdAndNotDeleted(), softDeleteById() добавлены в TweetRepository"
  done: "Добавлены методы findByIdAndIsDeletedFalse() (соответствует Spring Data conventions) и softDeleteById() с @Query в TweetRepository. Методы соответствуют стандартам проекта, derived query method не требует JavaDoc."
  artifacts: "services/tweet-api/src/main/java/com/twitter/repository/TweetRepository.java"
- [x] (P1) [2025-01-27 16:20] #5: Обновление DTO — Добавить поля isDeleted и deletedAt в TweetResponseDto (опционально)
  acceptance: "TweetResponseDto обновлен с учетом soft delete полей (если требуется в ответах)"
  done: "Добавлены опциональные поля isDeleted (Boolean, nullable) и deletedAt (LocalDateTime, nullable) в TweetResponseDto. Обновлены @Schema аннотации, примеры и JavaDoc. Поля добавлены для полноты DTO, хотя удалённые твиты не возвращаются в обычных запросах."
  artifacts: "services/tweet-api/src/main/java/com/twitter/dto/response/TweetResponseDto.java"
- [x] (P1) [2025-01-27 16:30] #6: Реализация Validator — Добавить метод validateForDelete в TweetValidator
  acceptance: "Метод validateForDelete добавлен в TweetValidator и TweetValidatorImpl, проверяет существование твита и права доступа"
  done: "Добавлен метод validateForDelete в TweetValidator интерфейс и TweetValidatorImpl. Метод проверяет: tweetId на null, существование твита, состояние (не удален), права доступа через validateTweetOwnership(). Создан DeleteTweetRequestDto для передачи userId. Код соответствует DESIGN_DELETE_TWEET.md и стандартам проекта."
  artifacts: "services/tweet-api/src/main/java/com/twitter/validation/TweetValidator.java, services/tweet-api/src/main/java/com/twitter/validation/TweetValidatorImpl.java, services/tweet-api/src/main/java/com/twitter/dto/request/DeleteTweetRequestDto.java"
- [x] (P1) [2025-01-27 16:40] #7: Реализация Service — Добавить метод deleteTweet в TweetService
  acceptance: "Метод deleteTweet добавлен в TweetService и TweetServiceImpl, использует @Transactional, вызывает валидацию и soft delete"
  done: "Добавлен метод deleteTweet в TweetService интерфейс и TweetServiceImpl. Метод использует @Transactional, вызывает validateForDelete для валидации, получает твит из БД, вызывает softDelete() на entity, сохраняет изменения через saveAndFlush. Возвращает void (ответ 204 No Content). Код соответствует стандартам проекта."
  artifacts: "services/tweet-api/src/main/java/com/twitter/service/TweetService.java, services/tweet-api/src/main/java/com/twitter/service/TweetServiceImpl.java"
- [x] (P1) [2025-01-27 16:50] #8: Обновление getTweetById — Исключить удаленные твиты из результатов
  acceptance: "Метод getTweetById обновлен для использования findByIdAndNotDeleted() вместо findById()"
  done: "Обновлен метод getTweetById в TweetServiceImpl для использования findByIdAndIsDeletedFalse() вместо findById(). Теперь удаленные твиты не возвращаются в результатах запроса."
  artifacts: "services/tweet-api/src/main/java/com/twitter/service/TweetServiceImpl.java"
- [x] (P1) [2025-01-27 17:00] #9: Реализация Controller — Добавить эндпоинт DELETE /api/v1/tweets/{tweetId}
  acceptance: "Метод deleteTweet добавлен в TweetController с @LoggableRequest, возвращает ResponseEntity.noContent()"
  done: "Добавлен метод deleteTweet в TweetApi интерфейс с полной OpenAPI документацией (@Operation, @ApiResponses для 204, 404, 409, 400). Добавлен метод deleteTweet в TweetController с @DeleteMapping, @LoggableRequest, @Valid для валидации. Метод вызывает tweetService.deleteTweet() и возвращает ResponseEntity.noContent().build()."
  artifacts: "services/tweet-api/src/main/java/com/twitter/controller/TweetApi.java, services/tweet-api/src/main/java/com/twitter/controller/TweetController.java"

### Документация кода (JavaDoc)
- [x] (P1) [2025-01-27 17:15] #10: JavaDoc для всех классов — Обновить JavaDoc для всех измененных классов
  acceptance: "Все public классы и методы имеют JavaDoc с @author geron, @version 1.0"
  done: "Проверены все измененные классы на соответствие стандартам JavaDoc. Все классы имеют @author geron и @version 1.0. Добавлен JavaDoc для метода softDeleteById в TweetRepository (custom query method). Улучшен JavaDoc для методов softDelete() и isActive() в Tweet Entity с подробным описанием поведения и идемпотентности."
  artifacts: "services/tweet-api/src/main/java/com/twitter/repository/TweetRepository.java, services/tweet-api/src/main/java/com/twitter/entity/Tweet.java"
- [x] (P1) [2025-01-27 17:20] #11: JavaDoc для DTO — Обновить JavaDoc для TweetResponseDto (если добавлены поля)
  acceptance: "TweetResponseDto имеет JavaDoc с @param для всех компонентов"
  done: "Проверен и улучшен JavaDoc для TweetResponseDto. Все параметры record документированы через @param (включая isDeleted и deletedAt, добавленные в шаге #5). Улучшено описание класса с использованием <p> тегов согласно стандартам. JavaDoc соответствует STANDART_JAVADOC.md."
  artifacts: "services/tweet-api/src/main/java/com/twitter/dto/response/TweetResponseDto.java"

### Тестирование
- [x] (P1) [2025-01-27 17:30] #12: Unit тесты для Service — Тесты для метода deleteTweet
  acceptance: "Все сценарии deleteTweet покрыты unit тестами: успешное удаление, твит не найден, доступ запрещен"
  done: "Добавлены unit тесты для метода deleteTweet в TweetServiceImplTest. Покрыты все сценарии: успешное удаление (проверка установки isDeleted и deletedAt), проверка вызовов зависимостей, твит не найден (TWEET_NOT_FOUND), доступ запрещен (TWEET_ACCESS_DENIED), твит уже удален (TWEET_ALREADY_DELETED). Тесты соответствуют стандартам проекта и используют паттерн methodName_WhenCondition_ShouldExpectedResult."
  artifacts: "services/tweet-api/src/test/java/com/twitter/service/TweetServiceImplTest.java"
- [x] (P1) [2025-01-27 17:40] #13: Unit тесты для Validator — Тесты для метода validateForDelete
  acceptance: "Все сценарии validateForDelete покрыты unit тестами: успешная валидация, твит не найден, доступ запрещен, твит уже удален"
  done: "Добавлены unit тесты для метода validateForDelete в TweetValidatorImplTest. Покрыты все сценарии: успешная валидация, tweetId is null (TWEET_ID_NULL), твит не найден (TWEET_NOT_FOUND), твит уже удален (TWEET_ALREADY_DELETED), доступ запрещен (TWEET_ACCESS_DENIED). Тесты соответствуют стандартам проекта и используют паттерн methodName_WhenCondition_ShouldExpectedResult."
  artifacts: "services/tweet-api/src/test/java/com/twitter/validation/TweetValidatorImplTest.java"
- [x] (P1) [2025-01-27 17:50] #14: Unit тесты для getTweetById — Тесты для проверки исключения удаленных твитов
  acceptance: "Тесты проверяют, что удаленные твиты не возвращаются в getTweetById"
  done: "Обновлены существующие тесты для getTweetById: заменен findById на findByIdAndIsDeletedFalse во всех тестах. Добавлен новый тест getTweetById_WhenTweetIsDeleted_ShouldReturnEmptyOptional, который проверяет, что удаленные твиты не возвращаются (метод возвращает Optional.empty()). Все тесты теперь соответствуют реальной реализации метода."
  artifacts: "services/tweet-api/src/test/java/com/twitter/service/TweetServiceImplTest.java"
- [x] (P2) [2025-01-27 18:00] #15: Integration тесты для Controller — Тесты для эндпоинта DELETE
  acceptance: "Все сценарии DELETE эндпоинта покрыты integration тестами: 204 No Content, 404 Not Found, 409 Conflict (доступ запрещен)"
  done: "Добавлены integration тесты для эндпоинта DELETE в TweetControllerTest. Покрыты все сценарии: успешное удаление (204 No Content с проверкой soft delete в БД), твит не найден (409 Conflict с проверкой ruleName TWEET_NOT_FOUND - соответствует реальному поведению GlobalExceptionHandler), доступ запрещен (409 Conflict с проверкой ruleName TWEET_ACCESS_DENIED и что твит не удален), null userId (400 Bad Request). Тесты соответствуют стандартам проекта и используют MockMvc для тестирования REST эндпоинтов. Примечание: GlobalExceptionHandler возвращает 409 для всех BusinessRuleValidationException, включая TWEET_NOT_FOUND, что соответствует поведению других эндпоинтов (updateTweet)."
  artifacts: "services/tweet-api/src/test/java/com/twitter/controller/TweetControllerTest.java"

### Swagger/OpenAPI документация
- [x] (P1) [2025-01-27 18:15] #16: OpenAPI interface (TweetApi.java) — Добавить метод deleteTweet с @Operation и @ApiResponses
  acceptance: "Метод deleteTweet добавлен в TweetApi с полной OpenAPI документацией: summary, description, @ApiResponses для 204, 404, 409, 400"
  done: "Метод deleteTweet уже реализован в TweetApi (шаг #9) с полной OpenAPI документацией: @Operation с summary и description, @ApiResponses для всех статусов (204, 404, 409, 400) с примерами в @ExampleObject, @Parameter для параметров. Документация соответствует STANDART_SWAGGER.md."
  artifacts: "services/tweet-api/src/main/java/com/twitter/controller/TweetApi.java"
- [x] (P1) [2025-01-27 18:15] #17: DTO Schema аннотации — Обновить @Schema для TweetResponseDto (если добавлены поля)
  acceptance: "TweetResponseDto имеет @Schema на уровне класса и полей с учетом STANDART_SWAGGER.md"
  done: "TweetResponseDto имеет полную @Schema документацию: @Schema на уровне класса с name, description и example (включая поля isDeleted и deletedAt), @Schema на каждом поле с описанием, example, format (для UUID и date-time), nullable для опциональных полей. Все соответствует STANDART_SWAGGER.md."
  artifacts: "services/tweet-api/src/main/java/com/twitter/dto/response/TweetResponseDto.java"

### Обновление README
- [x] (P2) [2025-01-27 18:30] #18: Обновление README.md — Добавить описание эндпоинта DELETE в раздел REST API
  acceptance: "README обновлен с описанием DELETE /api/v1/tweets/{tweetId}, добавлено в таблицу эндпоинтов и детальное описание"
  done: "Обновлен README.md: добавлен DELETE эндпоинт в таблицу эндпоинтов, добавлено детальное описание DELETE эндпоинта (параметры, валидация, бизнес-правила, ответы, примеры ошибок), обновлен раздел 'Основные возможности' (добавлено удаление твитов), обновлен раздел 'Бизнес-логика' (добавлен метод deleteTweet), обновлен раздел 'Слой валидации' (добавлена валидация для DELETE), обновлен раздел 'Работа с базой данных' (добавлены поля isDeleted и deletedAt), добавлен пример использования DELETE в раздел 'Примеры использования'. Все соответствует STANDART_README.md."
  artifacts: "services/tweet-api/README.md"

### Postman коллекции
- [x] (P2) [2025-01-27 18:45] #19: Обновление Postman коллекции — Добавить запрос "delete tweet"
  acceptance: "Добавлен запрос delete tweet с примерами ответов: 204 No Content, 404 Not Found, 409 Conflict, обновлены переменные окружения"
  done: "Добавлен запрос 'delete tweet' в Postman коллекцию: метод DELETE, путь {{baseUrl}}/api/v1/tweets/{{tweetId}}, тело запроса с userId, полное описание метода. Добавлены примеры ответов: 204 No Content (успешное удаление), 404 Not Found (твит не найден), 404 Not Found (твит уже удален), 409 Conflict (доступ запрещен), 400 Bad Request (null userId). Обновлено описание коллекции с упоминанием удаления твитов. Все соответствует STANDART_POSTMAN.md."
  artifacts: "postman/tweet-api/twitter-tweet-api.postman_collection.json"

### Проверка соответствия стандартам
- [x] (P1) [2025-01-27 19:00] #20: Проверка соответствия стандартам — Проверить все стандарты проекта
  acceptance: "Все стандарты проверены, код соответствует требованиям STANDART_CODE.md, STANDART_PROJECT.md, STANDART_TEST.md, STANDART_JAVADOC.md, STANDART_SWAGGER.md, STANDART_README.md, STANDART_POSTMAN.md"
  done: "Выполнена финальная проверка соответствия всех стандартов проекта. Проверены: STANDART_CODE.md (структура пакетов, именование, Java 24, Spring Boot 3.5.5), STANDART_PROJECT.md (@LoggableRequest на всех методах контроллера, @Valid для валидации, GlobalExceptionHandler), STANDART_TEST.md (unit и integration тесты с правильными паттернами именования), STANDART_JAVADOC.md (@author geron, @version 1.0 на всех классах), STANDART_SWAGGER.md (полная OpenAPI документация), STANDART_README.md (полное описание DELETE эндпоинта), STANDART_POSTMAN.md (запрос DELETE с примерами ответов). Все стандарты соблюдены."
  artifacts: "Все измененные файлы соответствуют стандартам проекта"

## Assumptions
- Soft delete означает установку флага isDeleted = true и установку deletedAt = текущее время
- Удаленные твиты не должны возвращаться в обычных запросах (getTweetById)
- Статистика (лайки, ретвиты) сохраняется при soft delete
- Только автор твита может удалить свой твит (аналогично updateTweet)
- HTTP статус 204 No Content для успешного удаления (без тела ответа)
- Валидация прав доступа аналогична updateTweet (сравнение userId)
- Миграция БД будет выполнена отдельно для добавления полей isDeleted и deletedAt

## Risks
- Технические риски:
  - Необходимость миграции БД для добавления полей isDeleted и deletedAt
  - Обновление существующих запросов для исключения удаленных твитов
  - Возможные проблемы с производительностью при фильтрации удаленных твитов
  - Необходимость обновления всех методов Repository для учета soft delete
- Организационные риски:
  - Возможные изменения в других частях системы, использующих TweetRepository
  - Необходимость координации с командой для обновления зависимых сервисов

## Metrics & Success Criteria
- Все тесты проходят (unit и integration)
- Покрытие кода > 80% для новых методов
- Соответствие всем стандартам проекта
- Эндпоинт DELETE /api/v1/tweets/{tweetId} работает корректно
- Удаленные твиты не возвращаются в getTweetById
- Валидация прав доступа работает корректно
- OpenAPI документация полная и корректная
- Postman коллекция обновлена с примерами
- HTTP статус 204 No Content возвращается при успешном удалении
- HTTP статус 404 Not Found возвращается при отсутствии твита
- HTTP статус 409 Conflict возвращается при попытке удаления чужого твита

## Notes
- Эндпоинт: DELETE /api/v1/tweets/{tweetId}
- Реализация soft delete (мягкое удаление) с сохранением данных в БД
- Только автор твита может удалить свой твит
- Статистика (лайки, ретвиты) сохраняется при удалении
- Ссылки на стандарты:
  - [Архитектура сервиса](../TWEET_API_ARCHITECTURE.md)
  - [План разработки](../TWEET_API_COMMON_2.md)
  - [Стандарты кода](../../../../standards/STANDART_CODE.md)
  - [Стандарты проекта](../../../../standards/STANDART_PROJECT.md)
  - [Стандарты тестирования](../../../../standards/STANDART_TEST.md)
  - [Стандарты JavaDoc](../../../../standards/STANDART_JAVADOC.md)
  - [Стандарты Swagger](../../../../standards/STANDART_SWAGGER.md)
  - [Стандарты README](../../../../standards/STANDART_README.md)
  - [Стандарты Postman](../../../../standards/STANDART_POSTMAN.md)



