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
- [ ] (P1) #1: Анализ требований — Определить структуру полей soft delete, бизнес-правила удаления, валидацию прав доступа
  acceptance: "Понять вход/выход, определить затронутые стандарты, спроектировать структуру данных"
- [ ] (P1) #2: Проектирование API и контрактов — Определить структуру эндпоинта DELETE, HTTP статусы, валидацию
  acceptance: "OpenAPI схема, структура ответов, валидация прав доступа"

### Реализация кода
- [ ] (P1) #3: Обновление Entity Tweet — Добавить поля isDeleted и deletedAt, метод softDelete()
  acceptance: "Entity обновлена с полями isDeleted (Boolean, default false) и deletedAt (LocalDateTime, nullable), добавлен метод softDelete()"
- [ ] (P1) #4: Обновление Repository — Добавить методы для работы с soft delete
  acceptance: "Методы findByIdAndNotDeleted(), softDeleteById() добавлены в TweetRepository"
- [ ] (P1) #5: Обновление DTO — Добавить поля isDeleted и deletedAt в TweetResponseDto (опционально)
  acceptance: "TweetResponseDto обновлен с учетом soft delete полей (если требуется в ответах)"
- [ ] (P1) #6: Реализация Validator — Добавить метод validateForDelete в TweetValidator
  acceptance: "Метод validateForDelete добавлен в TweetValidator и TweetValidatorImpl, проверяет существование твита и права доступа"
- [ ] (P1) #7: Реализация Service — Добавить метод deleteTweet в TweetService
  acceptance: "Метод deleteTweet добавлен в TweetService и TweetServiceImpl, использует @Transactional, вызывает валидацию и soft delete"
- [ ] (P1) #8: Обновление getTweetById — Исключить удаленные твиты из результатов
  acceptance: "Метод getTweetById обновлен для использования findByIdAndNotDeleted() вместо findById()"
- [ ] (P1) #9: Реализация Controller — Добавить эндпоинт DELETE /api/v1/tweets/{tweetId}
  acceptance: "Метод deleteTweet добавлен в TweetController с @LoggableRequest, возвращает ResponseEntity.noContent()"

### Документация кода (JavaDoc)
- [ ] (P1) #10: JavaDoc для всех классов — Обновить JavaDoc для всех измененных классов
  acceptance: "Все public классы и методы имеют JavaDoc с @author geron, @version 1.0"
- [ ] (P1) #11: JavaDoc для DTO — Обновить JavaDoc для TweetResponseDto (если добавлены поля)
  acceptance: "TweetResponseDto имеет JavaDoc с @param для всех компонентов"

### Тестирование
- [ ] (P1) #12: Unit тесты для Service — Тесты для метода deleteTweet
  acceptance: "Все сценарии deleteTweet покрыты unit тестами: успешное удаление, твит не найден, доступ запрещен"
- [ ] (P1) #13: Unit тесты для Validator — Тесты для метода validateForDelete
  acceptance: "Все сценарии validateForDelete покрыты unit тестами: успешная валидация, твит не найден, доступ запрещен, твит уже удален"
- [ ] (P1) #14: Unit тесты для getTweetById — Тесты для проверки исключения удаленных твитов
  acceptance: "Тесты проверяют, что удаленные твиты не возвращаются в getTweetById"
- [ ] (P2) #15: Integration тесты для Controller — Тесты для эндпоинта DELETE
  acceptance: "Все сценарии DELETE эндпоинта покрыты integration тестами: 204 No Content, 404 Not Found, 409 Conflict (доступ запрещен)"

### Swagger/OpenAPI документация
- [ ] (P1) #16: OpenAPI interface (TweetApi.java) — Добавить метод deleteTweet с @Operation и @ApiResponses
  acceptance: "Метод deleteTweet добавлен в TweetApi с полной OpenAPI документацией: summary, description, @ApiResponses для 204, 404, 409, 400"
- [ ] (P1) #17: DTO Schema аннотации — Обновить @Schema для TweetResponseDto (если добавлены поля)
  acceptance: "TweetResponseDto имеет @Schema на уровне класса и полей с учетом STANDART_SWAGGER.md"

### Обновление README
- [ ] (P2) #18: Обновление README.md — Добавить описание эндпоинта DELETE в раздел REST API
  acceptance: "README обновлен с описанием DELETE /api/v1/tweets/{tweetId}, добавлено в таблицу эндпоинтов и детальное описание"

### Postman коллекции
- [ ] (P2) #19: Обновление Postman коллекции — Добавить запрос "delete tweet"
  acceptance: "Добавлен запрос delete tweet с примерами ответов: 204 No Content, 404 Not Found, 409 Conflict, обновлены переменные окружения"

### Проверка соответствия стандартам
- [ ] (P1) #20: Проверка соответствия стандартам — Проверить все стандарты проекта
  acceptance: "Все стандарты проверены, код соответствует требованиям STANDART_CODE.md, STANDART_PROJECT.md, STANDART_TEST.md, STANDART_JAVADOC.md, STANDART_SWAGGER.md, STANDART_README.md, STANDART_POSTMAN.md"

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
  - [Стандарты кода](../../standards/STANDART_CODE.md)
  - [Стандарты проекта](../../standards/STANDART_PROJECT.md)
  - [Стандарты тестирования](../../standards/STANDART_TEST.md)
  - [Стандарты JavaDoc](../../standards/STANDART_JAVADOC.md)
  - [Стандарты Swagger](../../standards/STANDART_SWAGGER.md)
  - [Стандарты README](../../standards/STANDART_README.md)
  - [Стандарты Postman](../../standards/STANDART_POSTMAN.md)

