# TODO: Получить твиты пользователя (с пагинацией)

## Meta
- project: twitter-tweet-api
- updated: 2025-01-27
- changelog: todo/tweet/CHANGELOG.md
- standards:
  - STANDART_CODE.md
  - STANDART_PROJECT.md
  - STANDART_TEST.md
  - STANDART_JAVADOC.md
  - STANDART_SWAGGER.md
  - STANDART_README.md
  - STANDART_POSTMAN.md

## Задача

Реализовать эндпоинт `GET /api/v1/tweets/user/{userId}` для получения твитов пользователя с поддержкой пагинации.

## Анализ задачи

### Назначение
Эндпоинт позволяет получить список твитов конкретного пользователя с поддержкой пагинации. Твиты должны быть отсортированы по дате создания в порядке убывания (новые первыми) и исключать удаленные твиты (soft delete).

### Входные данные
- `userId` (UUID) - идентификатор пользователя (path parameter)
- `page` (int, optional) - номер страницы (по умолчанию 0)
- `size` (int, optional) - размер страницы (по умолчанию 20, максимум 100)
- `sort` (String, optional) - параметры сортировки (по умолчанию createdAt DESC)

### Выходные данные
- `PagedModel<TweetResponseDto>` - пагинированный список твитов с метаданными пагинации

### Ключевые компоненты
1. **Repository Layer**: метод для получения твитов пользователя с пагинацией
2. **Service Layer**: бизнес-логика получения твитов пользователя
3. **Controller Layer**: REST эндпоинт с валидацией и обработкой ошибок
4. **DTO Layer**: использование существующего `TweetResponseDto`
5. **Mapper Layer**: использование существующего `TweetMapper`

### Затронутые стандарты
- **STANDART_CODE.md**: Java 24 Records, MapStruct, Lombok, Bean Validation
- **STANDART_PROJECT.md**: @LoggableRequest, исключения из common-lib
- **STANDART_TEST.md**: Unit и Integration тесты
- **STANDART_JAVADOC.md**: JavaDoc для всех public классов и методов
- **STANDART_SWAGGER.md**: OpenAPI документация для нового эндпоинта
- **STANDART_README.md**: обновление README с новым эндпоинтом
- **STANDART_POSTMAN.md**: добавление запроса в Postman коллекцию

### Зависимости
- Существующий `TweetRepository` - нужно добавить метод для получения твитов пользователя
- Существующий `TweetService` - нужно добавить метод для получения твитов пользователя
- Существующий `TweetController` - нужно добавить новый эндпоинт
- Существующий `TweetApi` - нужно добавить метод в OpenAPI интерфейс
- Существующий `TweetMapper` - используется для маппинга Entity → DTO
- Существующий `TweetResponseDto` - используется как тип ответа
- Spring Data JPA `Pageable` и `PagedModel` для пагинации

## Tasks

### Анализ и проектирование
- [ ] (P1) #1: Анализ требований — Определить входные/выходные данные, non-functional requirements, затронутые стандарты
  acceptance: "Понять вход/выход, non-functional requirements, определить затронутые стандарты"
  
- [ ] (P1) #2: Проектирование API и контрактов — Определить структуру эндпоинта, параметры пагинации, структуру ответа
  acceptance: "OpenAPI схема, DTO структура, параметры пагинации определены"

### Реализация кода
- [ ] (P1) #3: Добавление метода в TweetRepository — Добавить Derived Query Method для получения твитов пользователя с пагинацией
  acceptance: "Метод `Page<Tweet> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable)` создан в TweetRepository"
  
- [ ] (P1) #4: Добавление метода в TweetService интерфейс — Определить контракт метода получения твитов пользователя
  acceptance: "Метод `PagedModel<TweetResponseDto> getUserTweets(UUID userId, Pageable pageable)` добавлен в TweetService с JavaDoc"
  
- [ ] (P1) #5: Реализация метода в TweetServiceImpl — Реализовать бизнес-логику получения твитов пользователя
  acceptance: "Метод реализован с валидацией userId, получением твитов через Repository, маппингом и преобразованием в PagedModel"
  
- [ ] (P1) #6: Добавление метода в TweetApi интерфейс — Добавить OpenAPI аннотации для нового эндпоинта
  acceptance: "Метод добавлен в TweetApi с @Operation, @ApiResponses, @Parameter, @ExampleObject"
  
- [ ] (P1) #7: Реализация метода в TweetController — Реализовать REST эндпоинт
  acceptance: "Метод `@GetMapping("/user/{userId}")` реализован с @LoggableRequest, @PageableDefault, делегированием в TweetService"

### Документация кода (JavaDoc)
- [ ] (P1) #8: JavaDoc для Service метода — Добавить JavaDoc для метода getUserTweets в TweetService
  acceptance: "JavaDoc добавлен с @author geron, @version 1.0, @param, @return, @throws"
  
- [ ] (P1) #9: JavaDoc для Controller метода — Добавить JavaDoc для метода getUserTweets в TweetController
  acceptance: "JavaDoc добавлен с @see для ссылки на TweetApi#getUserTweets"

### Тестирование
- [ ] (P1) #10: Unit тесты для TweetServiceImpl.getUserTweets — Покрыть метод unit тестами
  acceptance: "Unit тесты созданы для всех сценариев: успешное получение, пользователь не найден, пустой список, пагинация"
  
- [ ] (P2) #11: Integration тесты для TweetController.getUserTweets — Покрыть эндпоинт integration тестами
  acceptance: "Integration тесты созданы для всех статус-кодов (200, 400, 404), проверка пагинации, валидации"

### Swagger/OpenAPI документация
- [ ] (P1) #12: OpenAPI аннотации для getUserTweets — Добавить полную OpenAPI документацию
  acceptance: "Полная OpenAPI документация с @Operation, @ApiResponses, @Parameter, @ExampleObject для всех сценариев"

### Обновление README
- [ ] (P2) #13: Обновление README.md — Добавить новый эндпоинт в документацию
  acceptance: "README обновлен: добавлен в таблицу эндпоинтов, детальное описание, примеры, обновлен раздел бизнес-логики"

### Postman коллекции
- [ ] (P2) #14: Обновление Postman коллекции — Добавить запрос для нового эндпоинта
  acceptance: "Запрос 'get user tweets' добавлен в коллекцию с примерами ответов для всех сценариев (200, 400, 404)"

### Проверка соответствия стандартам
- [ ] (P1) #15: Проверка соответствия стандартам — Проверить соответствие всем стандартам проекта
  acceptance: "Все стандарты проверены, код соответствует требованиям STANDART_CODE.md, STANDART_PROJECT.md, STANDART_TEST.md, STANDART_JAVADOC.md, STANDART_SWAGGER.md, STANDART_README.md, STANDART_POSTMAN.md"

## Assumptions
- Пользователь должен существовать в users-api для получения его твитов
- Пагинация использует offset-based подход (page, size) через Spring Data JPA Pageable
- Твиты сортируются по createdAt DESC (новые первыми)
- Удаленные твиты (isDeleted = true) исключаются из результатов
- Максимальный размер страницы: 100 элементов (как указано в архитектуре)
- По умолчанию: page = 0, size = 20

## Risks
- **Производительность при большом количестве твитов**: 
  - Митигация: использование существующего индекса `idx_tweets_user_id_created_at`
- **Интеграция с users-api**: 
  - Митигация: использование существующей интеграции с обработкой ошибок
- **Валидация параметров пагинации**:
  - Митигация: Spring Data JPA автоматически валидирует Pageable параметры

## Metrics & Success Criteria
- Все тесты проходят (unit и integration)
- Покрытие кода > 80% для новых методов
- Время ответа API < 200ms для операций чтения
- Полная OpenAPI документация с примерами
- README обновлен с новым эндпоинтом
- Postman коллекция содержит запрос с примерами
- Соответствие всем стандартам проекта

## Notes
- Использовать существующий `TweetMapper` для маппинга Entity → DTO
- Использовать существующий `TweetResponseDto` как тип ответа
- Следовать паттернам из существующих эндпоинтов (getTweetById)
- Использовать `PagedModel` из Spring HATEOAS для пагинированных ответов
- Ссылки на стандарты:
  - [STANDART_CODE.md](../../standards/STANDART_CODE.md)
  - [STANDART_PROJECT.md](../../standards/STANDART_PROJECT.md)
  - [STANDART_TEST.md](../../standards/STANDART_TEST.md)
  - [STANDART_JAVADOC.md](../../standards/STANDART_JAVADOC.md)
  - [STANDART_SWAGGER.md](../../standards/STANDART_SWAGGER.md)
  - [STANDART_README.md](../../standards/STANDART_README.md)
  - [STANDART_POSTMAN.md](../../standards/STANDART_POSTMAN.md)
- Ссылки на архитектуру:
  - [TWEET_API_ARCHITECTURE.md](TWEET_API_ARCHITECTURE.md)
  - [TWEET_API_COMMON_2.md](TWEET_API_COMMON_2.md)

