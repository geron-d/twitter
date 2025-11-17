# TODO: Реализация GET /api/v1/tweets/{tweetId}

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
- [x] (P1) [2025-01-27] #1: Анализ требований — Проанализировать существующий код, определить необходимые изменения
  acceptance: "Понят вход/выход эндпоинта, определены затронутые компоненты, определены стандарты проекта, определена структура ответов (200, 404, 400)"
  note: "Анализ выполнен при создании плана. Определены все необходимые компоненты и стандарты."
- [x] (P1) [2025-01-27] #2: Проектирование API и контрактов — Спроектировать структуру метода в Service и Controller
  acceptance: "Определен контракт метода getTweetById в TweetService, определена структура ответа (Optional<TweetResponseDto>), определены HTTP статус коды (200, 404), определена обработка ошибок"
  note: "Проектирование выполнено при создании плана. Определен контракт: Optional<TweetResponseDto> getTweetById(UUID tweetId)."

### Реализация кода
- [x] (P1) [2025-01-27] #3: Реализация метода в Repository — Проверить доступность findById (используется стандартный из JpaRepository)
  acceptance: "Подтверждено, что JpaRepository.findById доступен, дополнительный метод не требуется"
  note: "Проверено: TweetRepository extends JpaRepository<Tweet, UUID>, метод findById доступен автоматически. Дополнительный метод не требуется."
- [x] (P1) [2025-01-27] #4: Реализация метода в Service интерфейсе — Добавить метод getTweetById в TweetService интерфейс
  acceptance: "Метод определен в TweetService, имеет JavaDoc с @author geron, @version 1.0, возвращает Optional<TweetResponseDto>, имеет @param и @return в JavaDoc"
  note: "Добавлен метод getTweetById в TweetService с полным JavaDoc. Метод возвращает Optional<TweetResponseDto>. Файл: services/tweet-api/src/main/java/com/twitter/service/TweetService.java"
- [x] (P1) [2025-01-27] #5: Реализация метода в Service implementation — Реализовать getTweetById в TweetServiceImpl
  acceptance: "Метод реализован с @Transactional(readOnly = true), использует tweetRepository.findById, использует tweetMapper.toResponseDto, возвращает Optional<TweetResponseDto>, имеет @see TweetService#getTweetById в JavaDoc"
  note: "Добавлена реализация getTweetById в TweetServiceImpl. Метод использует @Transactional(readOnly = true), вызывает tweetRepository.findById и преобразует через tweetMapper.toResponseDto. Файл: services/tweet-api/src/main/java/com/twitter/service/TweetServiceImpl.java"
- [x] (P1) [2025-01-27] #6: Реализация метода в TweetApi интерфейсе — Добавить метод getTweetById в TweetApi с OpenAPI аннотациями
  acceptance: "Метод определен в TweetApi, имеет @Operation с summary и description, имеет @ApiResponses со статусами 200, 404, 400, имеет @Parameter для tweetId, имеет @ExampleObject для успешного ответа и ошибки 404"
  note: "Добавлен метод getTweetById в TweetApi с полной OpenAPI документацией. Включены @Operation, @ApiResponses (200, 404, 400), @Parameter с example, @ExampleObject для всех сценариев. Все ошибки используют RFC 7807 Problem Details. Файл: services/tweet-api/src/main/java/com/twitter/controller/TweetApi.java"
- [x] (P1) [2025-01-27] #7: Реализация метода в TweetController — Реализовать GET эндпоинт в TweetController
  acceptance: "Метод реализован с @GetMapping(\"/{tweetId}\"), использует @LoggableRequest, использует @PathVariable для tweetId, возвращает ResponseEntity<TweetResponseDto>, возвращает 200 OK при наличии твита, возвращает 404 Not Found при отсутствии твита, имеет @see TweetApi#getTweetById в JavaDoc"
  note: "Добавлен метод getTweetById в TweetController. Использует @GetMapping(\"/{tweetId}\"), @LoggableRequest, @PathVariable, Optional pattern для обработки результата (200 OK или 404 Not Found). Файл: services/tweet-api/src/main/java/com/twitter/controller/TweetController.java"

### Документация кода (JavaDoc)
- [ ] (P1) #8: JavaDoc для Service методов — Добавить/проверить JavaDoc для всех методов Service
  acceptance: "TweetService.getTweetById имеет полный JavaDoc, TweetServiceImpl.getTweetById имеет @see ссылку, все JavaDoc на английском языке, все JavaDoc содержат @author geron, @version 1.0"
- [ ] (P1) #9: JavaDoc для Controller методов — Добавить/проверить JavaDoc для метода контроллера
  acceptance: "TweetController.getTweetById имеет @see ссылку, все JavaDoc соответствуют стандартам"

### Тестирование
- [ ] (P1) #10: Unit тесты для Service — Создать unit тесты для TweetServiceImpl.getTweetById
  acceptance: "Тесты используют @ExtendWith(MockitoExtension.class), используют именование methodName_WhenCondition_ShouldExpectedResult, используют @Nested для группировки, используют AssertJ для assertions, следуют паттерну AAA, покрыты сценарии: твит найден, твит не найден, проверены взаимодействия с зависимостями (verify)"
- [ ] (P1) #11: Integration тесты для Controller — Создать integration тесты для GET /api/v1/tweets/{tweetId}
  acceptance: "Тесты используют @SpringBootTest, @AutoConfigureWebMvc, используют MockMvc для тестирования REST endpoints, используют @Transactional для изоляции, тестированы статус-коды: 200, 404, проверен формат ответов, проверена валидация UUID"

### Swagger/OpenAPI документация
- [ ] (P1) #12: OpenAPI interface (*Api.java) — Добавить метод getTweetById в TweetApi с полной OpenAPI документацией
  acceptance: "Метод имеет @Tag (уже есть на уровне интерфейса), имеет @Operation с summary и description, имеет @ApiResponses со всеми статус-кодами (200, 404, 400), имеет @Parameter для tweetId с description и example, имеет @ExampleObject для успешного ответа (200), имеет @ExampleObject для ошибки 404, все примеры используют RFC 7807 Problem Details для ошибок"
- [ ] (P1) #13: DTO Schema аннотации — Проверить @Schema аннотации в TweetResponseDto
  acceptance: "TweetResponseDto имеет @Schema на уровне класса, все поля TweetResponseDto имеют @Schema аннотации, все @Schema содержат description, example, format где нужно"

### Обновление README
- [ ] (P2) #14: Обновление README.md — Обновить README.md с информацией о новом эндпоинте
  acceptance: "Обновлен раздел \"REST API\" с новым эндпоинтом, добавлено детальное описание эндпоинта GET /api/v1/tweets/{tweetId}, добавлен пример использования в разделе \"Примеры использования\", все описания на русском языке"

### Postman коллекции
- [ ] (P2) #15: Обновление Postman коллекции — Добавить запрос get tweet by id в Postman коллекцию
  acceptance: "Добавлен запрос с именем \"get tweet by id\" (lowercase с пробелами), запрос использует переменную {{baseUrl}}, запрос использует переменную {{tweetId}} для path параметра, запрос имеет описание, добавлены примеры ответов: 200 OK, 404 Not Found, используется правильный Content-Type (application/json для успеха, application/problem+json для ошибок), ошибки следуют RFC 7807 Problem Details"

### Проверка соответствия стандартам
- [ ] (P1) #16: Проверка соответствия стандартам — Проверить соответствие всех изменений стандартам проекта
  acceptance: "Проверено соответствие STANDART_CODE.md, проверено соответствие STANDART_PROJECT.md, проверено соответствие STANDART_TEST.md, проверено соответствие STANDART_JAVADOC.md, проверено соответствие STANDART_SWAGGER.md, проверено соответствие STANDART_README.md, проверено соответствие STANDART_POSTMAN.md"

## Assumptions
- Tweet entity не имеет поля isDeleted, поэтому используется стандартный findById из JpaRepository
- TweetResponseDto уже существует и может быть использован без изменений
- TweetMapper.toResponseDto уже реализован и работает корректно
- GlobalExceptionHandler из common-lib обрабатывает исключения автоматически
- UUID валидация выполняется Spring автоматически через @PathVariable

## Risks
- **Технический риск**: Если в будущем будет добавлен soft delete, потребуется обновление метода для использования findByIdAndNotDeleted
- **Технический риск**: Нужно убедиться, что Optional правильно обрабатывается и возвращается 404
- **Организационный риск**: Необходимо синхронизировать изменения в Service, Controller, тестах и документации

## Metrics & Success Criteria
- Покрытие тестами: > 80% для новых методов
- Соответствие стандартам: 100% соответствие всем стандартам проекта
- Функциональность: Эндпоинт возвращает 200 OK при наличии твита, 404 Not Found при отсутствии
- Производительность: Время ответа < 200ms для операций чтения
- Документация: Полная OpenAPI документация с примерами
- Тесты: Все unit и integration тесты проходят

## Notes
- Ссылка на архитектуру: `todo/tweet/TWEET_API_ARCHITECTURE.md`
- Ссылка на общий план: `todo/tweet/TWEET_API_COMMON_2.md`
- Существующий код: `services/tweet-api/src/main/java/com/twitter/`
- Тесты: `services/tweet-api/src/test/java/com/twitter/`
- Стандарты: `standards/STANDART_*.md`

