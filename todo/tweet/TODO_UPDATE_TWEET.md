# TODO: Реализация PUT /api/v1/tweets/{tweetId}

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
- [x] (P1) #1: Анализ требований — Изучить архитектуру, существующий код и стандарты
  acceptance: "Понять вход/выход, бизнес-правила, определить затронутые стандарты"
  metadata: priority=P1, done=2025-01-27T15:30, note="Проанализированы архитектура, существующий код (TweetController, TweetService, TweetValidator, TweetMapper, CreateTweetRequestDto), стандарты проекта (CODE, TEST, JAVADOC, SWAGGER, README, POSTMAN). Определены вход/выход, бизнес-правила обновления твитов, затронутые стандарты и архитектурные компоненты."
- [x] (P1) #2: Проектирование API и контрактов — Определить структуру DTO и валидацию
  acceptance: "UpdateTweetRequestDto структура, правила валидации, HTTP статусы"
  metadata: priority=P1, done=2025-01-27T15:45, note="Спроектирована структура UpdateTweetRequestDto (content, userId), определены правила валидации (Bean Validation + бизнес-правила), HTTP статусы для всех сценариев (200, 400, 403, 404, 500), контракт метода updateTweet в TweetApi. Создан документ DESIGN_UPDATE_TWEET.md с полным проектированием."

### Реализация кода
- [x] (P1) #3: Реализация UpdateTweetRequestDto — Создать DTO Record с валидацией
  acceptance: "DTO создан как Record с @NotBlank, @Size, @Schema аннотациями, размещен в dto/request/"
  metadata: priority=P1, done=2025-01-27T16:00, note="Создан UpdateTweetRequestDto.java в dto/request/ с полями content (String, @NotBlank, @Size) и userId (UUID, @NotNull). Добавлены @Schema аннотации для OpenAPI, @Builder для совместимости, полная JavaDoc документация. Файл соответствует стандартам проекта."
- [x] (P1) #4: Реализация метода маппинга в TweetMapper — Добавить updateTweetFromUpdateDto
  acceptance: "Метод создан с @MappingTarget, игнорирует системные поля (id, createdAt, userId)"
  metadata: priority=P1, done=2025-01-27T16:15, note="Добавлен метод updateTweetFromUpdateDto в TweetMapper с @MappingTarget. Игнорируются системные поля: id, createdAt, updatedAt, userId. Обновляется только поле content. Добавлена полная JavaDoc документация. Импортирован UpdateTweetRequestDto и @MappingTarget."
- [x] (P1) #5: Реализация validateForUpdate в TweetValidator — Добавить валидацию обновления
  acceptance: "Метод проверяет существование твита, права автора, время обновления, частоту, контент"
  metadata: priority=P1, done=2025-01-27T16:30, note="Добавлен метод validateForUpdate в интерфейс TweetValidator и реализация в TweetValidatorImpl. Реализованы все проверки: существование твита, права автора, время обновления (7 дней), частота обновлений (упрощенная проверка - 6 минут между обновлениями), валидация контента. Добавлены приватные методы validateTweetOwnership, validateUpdateTimeLimit, validateUpdateFrequency, validateContent. Добавлена полная JavaDoc документация. Добавлена зависимость TweetRepository."
- [x] (P1) #6: Реализация updateTweet в TweetService — Добавить бизнес-логику обновления
  acceptance: "Метод использует @Transactional, вызывает валидатор, обновляет через mapper, сохраняет"
  metadata: priority=P1, done=2025-01-27T16:45, note="Добавлен метод updateTweet в интерфейс TweetService и реализация в TweetServiceImpl. Метод использует @Transactional, вызывает tweetValidator.validateForUpdate(), получает твит из репозитория, обновляет через tweetMapper.updateTweetFromUpdateDto(), сохраняет через tweetRepository.saveAndFlush(), преобразует в TweetResponseDto. Добавлена полная JavaDoc документация в интерфейсе и реализация с @see."
- [ ] (P1) #7: Реализация updateTweet в TweetController — Добавить REST эндпоинт
  acceptance: "Метод с @LoggableRequest, @Valid, @PathVariable, возвращает ResponseEntity.ok()"
- [ ] (P1) #8: Реализация updateTweet в TweetApi — Добавить OpenAPI интерфейс
  acceptance: "Метод с @Operation, @ApiResponses, @Parameter, примерами ответов"

### Документация кода (JavaDoc)
- [ ] (P1) #9: JavaDoc для UpdateTweetRequestDto — Документировать DTO Record
  acceptance: "JavaDoc с @param для всех компонентов, @author geron, @version 1.0"
- [ ] (P1) #10: JavaDoc для методов валидации — Документировать validateForUpdate
  acceptance: "JavaDoc с @param, @throws, описанием бизнес-правил"
- [ ] (P1) #11: JavaDoc для методов сервиса — Документировать updateTweet
  acceptance: "JavaDoc с @param, @return, @throws, описанием логики"
- [ ] (P1) #12: JavaDoc для методов контроллера — Документировать updateTweet
  acceptance: "JavaDoc с @see TweetApi#updateTweet для реализации"

### Тестирование
- [ ] (P1) #13: Unit тесты для TweetValidator — Тесты validateForUpdate
  acceptance: "Тесты для всех сценариев: успех, твит не найден, нет прав, превышено время, превышена частота"
- [ ] (P1) #14: Unit тесты для TweetService — Тесты updateTweet
  acceptance: "Тесты успешного обновления, ошибок валидации, проверка взаимодействий с зависимостями"
- [ ] (P1) #15: Unit тесты для TweetMapper — Тесты updateTweetFromUpdateDto
  acceptance: "Тесты маппинга с реальным маппером, проверка игнорируемых полей"
- [ ] (P2) #16: Integration тесты для TweetController — Тесты REST эндпоинта
  acceptance: "Тесты с MockMvc для всех статус-кодов (200, 400, 404, 403), проверка валидации"

### Swagger/OpenAPI документация
- [ ] (P1) #17: OpenAPI метод updateTweet в TweetApi — Добавить @Operation и @ApiResponses
  acceptance: "Полная документация с summary, description, примерами для 200, 400, 404, 403"
- [ ] (P1) #18: DTO Schema аннотации для UpdateTweetRequestDto — Добавить @Schema
  acceptance: "Класс и поля имеют @Schema с description, example, requiredMode, minLength, maxLength"

### Обновление README
- [ ] (P2) #19: Обновление README.md — Добавить описание PUT эндпоинта
  acceptance: "Обновлен раздел REST API с новым эндпоинтом, добавлен пример использования"

### Postman коллекции
- [ ] (P2) #20: Обновление Postman коллекции — Добавить запрос update tweet
  acceptance: "Добавлен запрос 'update tweet (complete)' с примерами для всех сценариев (200, 400, 404, 403)"

### Проверка соответствия стандартам
- [ ] (P1) #21: Проверка соответствия стандартам — Финальная проверка всех стандартов
  acceptance: "Все стандарты проверены, код соответствует требованиям"

## Assumptions
- Предполагается, что проверка прав автора будет выполняться через сравнение userId из твита с userId из запроса (пока нет аутентификации)
- Предполагается, что ограничение времени обновления (7 дней) и частоты (10 в час) будут реализованы на уровне валидатора
- Предполагается, что updatedAt будет обновляться автоматически через JPA @UpdateTimestamp
- Предполагается использование существующего GlobalExceptionHandler для обработки ошибок

## Risks
- **Технические риски:**
  - Необходимость проверки прав автора без системы аутентификации (временное решение через userId в запросе)
  - Отслеживание частоты обновлений может потребовать Redis или кэширование
  - Проверка времени обновления требует расчета разницы между createdAt и текущим временем
  
- **Организационные риски:**
  - Бизнес-правила могут измениться (время обновления, частота)
  - В будущем потребуется интеграция с системой аутентификации для проверки прав

## Metrics & Success Criteria
- Все тесты проходят (unit и integration)
- Покрытие кода > 80% для новых методов
- Все статус-коды протестированы (200, 400, 404, 403)
- Swagger документация полная с примерами
- README обновлен на русском языке
- Postman коллекция содержит все сценарии
- Код соответствует всем стандартам проекта

## Notes
- Ссылки на стандарты:
  - [STANDART_CODE.md](../../standards/STANDART_CODE.md)
  - [STANDART_PROJECT.md](../../standards/STANDART_PROJECT.md)
  - [STANDART_TEST.md](../../standards/STANDART_TEST.md)
  - [STANDART_JAVADOC.md](../../standards/STANDART_JAVADOC.md)
  - [STANDART_SWAGGER.md](../../standards/STANDART_SWAGGER.md)
  - [STANDART_README.md](../../standards/STANDART_README.md)
  - [STANDART_POSTMAN.md](../../standards/STANDART_POSTMAN.md)
- Архитектурный документ: [TWEET_API_ARCHITECTURE.md](./TWEET_API_ARCHITECTURE.md)
- Общий план сервиса: [TWEET_API_COMMON_2.md](./TWEET_API_COMMON_2.md)


