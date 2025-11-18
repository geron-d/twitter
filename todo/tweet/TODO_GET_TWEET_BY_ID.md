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
- [x] (P1) [2025-01-27] #8: JavaDoc для Service методов — Добавить/проверить JavaDoc для всех методов Service
  acceptance: "TweetService.getTweetById имеет полный JavaDoc, TweetServiceImpl.getTweetById имеет @see ссылку, все JavaDoc на английском языке, все JavaDoc содержат @author geron, @version 1.0"
  note: "Проверено: TweetService.getTweetById имеет полный JavaDoc с @param и @return. TweetServiceImpl.getTweetById имеет @see TweetService#getTweetById. Классы имеют @author geron и @version 1.0. Все JavaDoc на английском языке."
- [x] (P1) [2025-01-27] #9: JavaDoc для Controller методов — Добавить/проверить JavaDoc для метода контроллера
  acceptance: "TweetController.getTweetById имеет @see ссылку, все JavaDoc соответствуют стандартам"
  note: "Проверено: TweetController.getTweetById имеет @see TweetApi#getTweetById. Класс TweetController имеет @author geron и @version 1.0. Все JavaDoc соответствуют стандартам."

### Тестирование
- [x] (P1) [2025-01-27] #10: Unit тесты для Service — Создать unit тесты для TweetServiceImpl.getTweetById
  acceptance: "Тесты используют @ExtendWith(MockitoExtension.class), используют именование methodName_WhenCondition_ShouldExpectedResult, используют @Nested для группировки, используют AssertJ для assertions, следуют паттерну AAA, покрыты сценарии: твит найден, твит не найден, проверены взаимодействия с зависимостями (verify)"
  note: "Добавлены unit тесты для getTweetById в TweetServiceImplTest. Создан @Nested класс GetTweetByIdTests с 4 тестами: твит найден, твит не найден, проверка взаимодействий при наличии твита, проверка взаимодействий при отсутствии твита. Все тесты используют паттерн AAA, AssertJ, verify для проверки взаимодействий. Файл: services/tweet-api/src/test/java/com/twitter/service/TweetServiceImplTest.java"
- [x] (P1) [2025-01-27] #11: Integration тесты для Controller — Создать integration тесты для GET /api/v1/tweets/{tweetId}
  acceptance: "Тесты используют @SpringBootTest, @AutoConfigureWebMvc, используют MockMvc для тестирования REST endpoints, используют @Transactional для изоляции, тестированы статус-коды: 200, 404, проверен формат ответов, проверена валидация UUID"
  note: "Добавлены integration тесты для getTweetById в TweetControllerTest. Создан @Nested класс GetTweetByIdTests с 4 тестами: 200 OK при наличии твита, проверка корректности данных, 404 Not Found при отсутствии твита, 400 Bad Request при невалидном UUID. Добавлен helper метод createAndSaveTweet. Файл: services/tweet-api/src/test/java/com/twitter/controller/TweetControllerTest.java"

### Swagger/OpenAPI документация
- [x] (P1) [2025-01-27] #12: OpenAPI interface (*Api.java) — Добавить метод getTweetById в TweetApi с полной OpenAPI документацией
  acceptance: "Метод имеет @Tag (уже есть на уровне интерфейса), имеет @Operation с summary и description, имеет @ApiResponses со всеми статус-кодами (200, 404, 400), имеет @Parameter для tweetId с description и example, имеет @ExampleObject для успешного ответа (200), имеет @ExampleObject для ошибки 404, все примеры используют RFC 7807 Problem Details для ошибок"
  note: "Метод getTweetById уже был добавлен в TweetApi в задаче #6 с полной OpenAPI документацией. Файл: services/tweet-api/src/main/java/com/twitter/controller/TweetApi.java"
- [x] (P1) [2025-01-27] #13: DTO Schema аннотации — Проверить @Schema аннотации в TweetResponseDto
  acceptance: "TweetResponseDto имеет @Schema на уровне класса, все поля TweetResponseDto имеют @Schema аннотации, все @Schema содержат description, example, format где нужно"
  note: "TweetResponseDto полностью соответствует стандартам проекта. Имеет @Schema на уровне класса с name, description, example. Все поля имеют @Schema аннотации с description, example, format (uuid для UUID, date-time для LocalDateTime), maxLength для content. Файл: services/tweet-api/src/main/java/com/twitter/dto/response/TweetResponseDto.java"

### Обновление README
- [x] (P2) [2025-01-27] #14: Обновление README.md — Обновить README.md с информацией о новом эндпоинте
  acceptance: "Обновлен раздел \"REST API\" с новым эндпоинтом, добавлено детальное описание эндпоинта GET /api/v1/tweets/{tweetId}, добавлен пример использования в разделе \"Примеры использования\", все описания на русском языке"
  note: "Обновлен README.md: добавлена возможность получения твита в раздел \"Основные возможности\", добавлен GET /{tweetId} в таблицу эндпоинтов, добавлено детальное описание эндпоинта с примерами ответов (200, 404, 400), добавлен метод getTweetById в описание TweetService, добавлен пример использования с curl. Файл: services/tweet-api/README.md"

### Postman коллекции
- [x] (P2) [2025-01-27] #15: Обновление Postman коллекции — Добавить запрос get tweet by id в Postman коллекцию
  acceptance: "Добавлен запрос с именем \"get tweet by id\" (lowercase с пробелами), запрос использует переменную {{baseUrl}}, запрос использует переменную {{tweetId}} для path параметра, запрос имеет описание, добавлены примеры ответов: 200 OK, 404 Not Found, используется правильный Content-Type (application/json для успеха, application/problem+json для ошибок), ошибки следуют RFC 7807 Problem Details"
  note: "Добавлен запрос \"get tweet by id\" в Postman коллекцию. Запрос использует GET метод, переменную {{baseUrl}} для базового URL, переменную {{tweetId}} для path параметра. Добавлено описание запроса. Добавлены примеры ответов: 200 OK (tweet found) с Content-Type application/json, 404 Not Found (tweet not found) с Content-Type application/problem+json и RFC 7807 Problem Details. Добавлена переменная tweetId в коллекцию и в файл окружения. Файлы: postman/tweet-api/twitter-tweet-api.postman_collection.json, postman/tweet-api/twitter-tweet-api.postman_environment.json"

### Проверка соответствия стандартам
- [x] (P1) [2025-01-27] #16: Проверка соответствия стандартам — Проверить соответствие всех изменений стандартам проекта
  acceptance: "Проверено соответствие STANDART_CODE.md, проверено соответствие STANDART_PROJECT.md, проверено соответствие STANDART_TEST.md, проверено соответствие STANDART_JAVADOC.md, проверено соответствие STANDART_SWAGGER.md, проверено соответствие STANDART_README.md, проверено соответствие STANDART_POSTMAN.md"
  note: "Проведена финальная проверка всех изменений на соответствие стандартам проекта. Все изменения соответствуют стандартам: код использует @LoggableRequest, @Transactional(readOnly = true) для read операций, тесты используют правильное именование (methodName_WhenCondition_ShouldExpectedResult), @Nested для группировки, AssertJ для assertions, паттерн AAA, JavaDoc присутствует на всех методах, OpenAPI документация полная с примерами (200, 404, 400), README обновлен на русском языке, Postman коллекция соответствует стандартам (lowercase с пробелами, переменные, примеры ответов, RFC 7807)."

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

