# TODO: Admin Script API Service

## Обзор
Реализация отдельного микросервиса admin-script-api для выполнения административных скриптов: создание n пользователей с рандомными данными, добавление n твитов для каждого пользователя и удаление по 1 твиту у l случайных пользователей.

## Задачи

- [x] (P1) [2025-01-27] #1: Анализ требований и проектирование API - определить структуру DTO, параметры валидации, стратегию генерации данных с использованием Datafaker
  - Зависимости: нет
  - Acceptance criteria:
    - Определена структура Request/Response DTO
    - Определены параметры валидации (минимальные/максимальные значения)
    - Определена стратегия генерации данных с Datafaker
    - Определена структура ответа со статистикой
  - Выполнено: Создан документ ANALYSIS_DESIGN.md с полным анализом требований и проектированием API. Определены структуры DTO, параметры валидации, стратегия генерации данных с Datafaker, структура ответа со статистикой, интеграции с другими сервисами и обработка ошибок.

- [x] (P1) [2025-01-27] #2: Настройка Gradle модуля - добавить в settings.gradle, создать build.gradle с зависимостями (включая Datafaker)
  - Зависимости: нет
  - Acceptance criteria:
    - Модуль добавлен в settings.gradle
    - Создан build.gradle с всеми необходимыми зависимостями
    - Datafaker добавлен в dependencyManagement корневого build.gradle
  - Выполнено: Модуль services:admin-script-api добавлен в settings.gradle. Создан build.gradle с зависимостями (Spring Boot, Spring Cloud OpenFeign, Datafaker, OpenAPI, Lombok, MapStruct, Testcontainers, WireMock). Datafaker версии 2.1.0 добавлен в dependencyManagement корневого build.gradle. Создана структура директорий модуля.

- [x] (P1) [2025-01-27] #3: Реализация DTO (Records) - GenerateUsersAndTweetsRequestDto и GenerateUsersAndTweetsResponseDto с валидацией и @Schema
  - Зависимости: #1
  - Acceptance criteria:
    - Создан GenerateUsersAndTweetsRequestDto с полями nUsers, nTweetsPerUser, lUsersForDeletion
    - Создан GenerateUsersAndTweetsResponseDto со статистикой и списками ID
    - Добавлены валидационные аннотации
    - Добавлены @Schema аннотации для Swagger
  - Выполнено: Созданы все три DTO: GenerateUsersAndTweetsRequestDto (com.twitter.dto.request) с валидацией @NotNull, @Min, @Max и @Schema аннотациями, ScriptStatisticsDto (com.twitter.dto.response) со статистикой выполнения, GenerateUsersAndTweetsResponseDto (com.twitter.dto.response) со списками ID и статистикой. Все DTO содержат полную JavaDoc документацию (@author geron, @version 1.0), @Schema аннотации на уровне класса и полей, @Builder для request и response DTO.

- [x] (P1) [2025-01-27] #4: Реализация Feign Clients - UsersApiClient и TweetsApiClient для интеграции с другими сервисами
  - Зависимости: #1
  - Acceptance criteria:
    - Создан UsersApiClient с методом createUser
    - Создан TweetsApiClient с методами createTweet, deleteTweet, getUserTweets
    - Настроены URL и path для сервисов
  - Выполнено: Создан UsersApiClient (com.twitter.client) с методом createUser для интеграции с users-api (POST /api/v1/users). Создан TweetsApiClient (com.twitter.client) с методами createTweet (POST /api/v1/tweets), deleteTweet (DELETE /api/v1/tweets/{tweetId}), getUserTweets (GET /api/v1/tweets/user/{userId}) для интеграции с tweet-api. Настроены URL через конфигурацию ${app.users-api.base-url} и ${app.tweet-api.base-url}. Созданы DTO для внешних API в пакете com.twitter.dto.external: UserRequestDto, UserResponseDto, CreateTweetRequestDto, DeleteTweetRequestDto, TweetResponseDto. Все Feign Clients содержат полную JavaDoc документацию (@author geron, @version 1.0).

- [x] (P1) [2025-01-27] #5: Реализация Gateways - UsersGateway и TweetsGateway с обработкой ошибок
  - Зависимости: #4
  - Acceptance criteria:
    - Создан UsersGateway с обработкой ошибок
    - Создан TweetsGateway с обработкой ошибок
    - Добавлено логирование
  - Выполнено: Создан UsersGateway (com.twitter.gateway) с методом createUser для обёртки вызовов UsersApiClient с обработкой ошибок и логированием. Создан TweetsGateway (com.twitter.gateway) с методами createTweet, deleteTweet, getUserTweets для обёртки вызовов TweetsApiClient с обработкой ошибок и логированием. Все методы содержат валидацию входных параметров (null checks), обработку исключений через try-catch с логированием (debug для успешных операций, error для ошибок), пробрасывание исключений дальше для обработки в Service слое. Все Gateway классы содержат полную JavaDoc документацию (@author geron, @version 1.0), используют @Component, @RequiredArgsConstructor, @Slf4j аннотации. Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27] #6: Реализация RandomDataGenerator с использованием Datafaker - утилита для генерации рандомных данных пользователей и твитов
  - Зависимости: #1, #2
  - Acceptance criteria:
    - Создан RandomDataGenerator с методами генерации данных
    - Используется Datafaker для генерации
    - Обеспечена уникальность login и email через timestamp/UUID
  - Выполнено: Создан RandomDataGenerator (com.twitter.util) с 6 методами генерации данных: generateLogin() - генерация уникального login с использованием name().firstName() и name().lastName() + timestamp/UUID, с обрезкой до 50 символов; generateEmail() - генерация уникального email с использованием internet().emailAddress() + timestamp; generateFirstName() и generateLastName() - генерация имен с использованием name().firstName() и name().lastName(); generatePassword() - генерация пароля (8-20 символов) с использованием комбинации name и number генераторов; generateTweetContent() - генерация контента твита (1-280 символов) с использованием lorem().sentence() или lorem().paragraph() с обрезкой до 280 символов. Все методы обеспечивают уникальность через timestamp/UUID где необходимо, соблюдают ограничения DTO (login: 3-50, password: 8-20, tweet: 1-280). Класс использует @Component, @Slf4j аннотации, содержит полную JavaDoc документацию (@author geron, @version 1.0). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27] #7: Реализация Validator - GenerateUsersAndTweetsValidator для валидации параметров скрипта
  - Зависимости: #3
  - Acceptance criteria:
    - Создан интерфейс GenerateUsersAndTweetsValidator
    - Создана реализация GenerateUsersAndTweetsValidatorImpl
    - Валидация параметров (n > 0, l > 0, l <= количество пользователей с твитами)
  - Выполнено: Создан интерфейс GenerateUsersAndTweetsValidator (com.twitter.validation) с методом validateDeletionCount для валидации параметров скрипта. Создана реализация GenerateUsersAndTweetsValidatorImpl (com.twitter.validation) с бизнес-валидацией: проверка, что lUsersForDeletion <= количество пользователей с твитами (после создания твитов), выброс BusinessRuleValidationException с понятным сообщением при нарушении. Валидатор обрабатывает случай lUsersForDeletion = 0 (валидация проходит). Все классы используют стандартные аннотации: @Component, @RequiredArgsConstructor, @Slf4j. Все классы содержат полную JavaDoc документацию (@author geron, @version 1.0). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27] #8: Реализация Service - GenerateUsersAndTweetsService и GenerateUsersAndTweetsServiceImpl с бизнес-логикой выполнения скрипта
  - Зависимости: #3, #5, #6, #7
  - Acceptance criteria:
    - Создан интерфейс GenerateUsersAndTweetsService
    - Создана реализация GenerateUsersAndTweetsServiceImpl
    - Реализована логика создания пользователей, твитов и удаления
  - Выполнено: Создан интерфейс GenerateUsersAndTweetsService (com.twitter.service) с методом executeScript для выполнения административного скрипта. Создана реализация GenerateUsersAndTweetsServiceImpl (com.twitter.service) с полной бизнес-логикой: создание nUsers пользователей с рандомными данными через UsersGateway и RandomDataGenerator (с обработкой ошибок), создание nTweetsPerUser твитов для каждого пользователя через TweetsGateway и RandomDataGenerator (с обработкой ошибок), подсчёт usersWithTweets и usersWithoutTweets, валидация lUsersForDeletion через GenerateUsersAndTweetsValidator, выбор l случайных пользователей с твитами и удаление по 1 твиту у каждого (с обработкой ошибок), сбор статистики (totalUsersCreated, totalTweetsCreated, totalTweetsDeleted, usersWithTweets, usersWithoutTweets, executionTimeMs, errors). Все частичные ошибки обрабатываются gracefully (логируются и добавляются в errors, выполнение продолжается). Все классы используют стандартные аннотации: @Service, @RequiredArgsConstructor, @Slf4j. Все классы содержат полную JavaDoc документацию (@author geron, @version 1.0). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27] #9: Реализация Controller - AdminScriptApi и AdminScriptController с REST endpoint для всех скриптов
  - Зависимости: #8
  - Acceptance criteria:
    - Создан AdminScriptApi с OpenAPI аннотациями
    - Создан AdminScriptController с @LoggableRequest
    - Реализован endpoint POST /api/v1/admin-scripts/generate-users-and-tweets
  - Выполнено: Создан интерфейс AdminScriptApi (com.twitter.controller) с полными OpenAPI аннотациями: @Tag для группировки эндпоинтов, @Operation с подробным описанием операции, @ApiResponses с примерами успешных ответов (200), ошибок валидации (400) и внутренних ошибок (500). Создан AdminScriptController (com.twitter.controller) с реализацией интерфейса AdminScriptApi: @RestController, @RequestMapping("/api/v1/admin-scripts"), @RequiredArgsConstructor, метод generateUsersAndTweets с @LoggableRequest, @PostMapping("/generate-users-and-tweets"), валидация через @Valid, вызов GenerateUsersAndTweetsService.executeScript(), возврат ResponseEntity.ok(). Все классы содержат полную JavaDoc документацию (@author geron, @version 1.0). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27] #10: Реализация Config - OpenApiConfig и FeignConfig для конфигурации сервиса
  - Зависимости: #9
  - Acceptance criteria:
    - Создан OpenApiConfig
    - Создан FeignConfig с @EnableFeignClients
  - Выполнено: Создан OpenApiConfig (com.twitter.config) с @Configuration и @Bean методом adminScriptApiOpenAPI(): настройка OpenAPI спецификации с title "Twitter Admin Script API", подробным description (возможности API, аутентификация, rate limiting, обработка ошибок), version "1.0.0", server на localhost:8083. Создан FeignConfig (com.twitter.config) с @Configuration и @EnableFeignClients(basePackages = "com.twitter.client") для активации Feign клиентов в пакете com.twitter.client. Все классы содержат полную JavaDoc документацию (@author geron, @version 1.0). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27] #11: Создание application.yml - конфигурация порта, URLs других сервисов, Feign настройки
  - Зависимости: #10
  - Acceptance criteria:
    - Создан application.yml
    - Настроен порт 8083
    - Настроены URLs для users-api и tweet-api
    - Настроены Feign настройки
  - Выполнено: Обновлён application.yml с полной конфигурацией: server.port=8083, spring.application.name=admin-script-api, app.users-api.base-url=http://localhost:8081, app.tweet-api.base-url=http://localhost:8082, feign.client.config.default (connect-timeout: 2000, read-timeout: 5000, logger-level: basic), feign.httpclient.enabled=true, springdoc настройки для Swagger UI, management endpoints (health, info, metrics, tracing), logging настройки. Исправлен application-docker.yml: исправлена ошибка в tweet-api.base-url (было http://users-api:8082, стало http://tweet-api:8082), удалена лишняя секция app.tweet.max-content-length. Все настройки соответствуют стандартам проекта и требованиям acceptance criteria.

- [x] (P2) [2025-01-27] #12: JavaDoc документация - добавить JavaDoc для всех public классов и методов
  - Зависимости: #9, #8, #7, #6
  - Acceptance criteria:
    - JavaDoc для всех public классов
    - JavaDoc для всех public методов
    - @author geron, @version 1.0
    - @param, @return, @throws для всех методов
  - Выполнено: Проверена и дополнена JavaDoc документация для всех public классов и методов в admin-script-api. Добавлен JavaDoc для Application.java (класс и метод main с @param). Проверены все остальные классы: все имеют полную JavaDoc документацию с @author geron, @version 1.0, @param для всех параметров, @return для возвращаемых значений, @throws для исключений. Все классы соответствуют стандартам STANDART_JAVADOC.md. Проверка линтера: ошибок не обнаружено.

- [x] (P2) [2025-01-27] #13: Unit тесты - GenerateUsersAndTweetsServiceImplTest, GenerateUsersAndTweetsValidatorImplTest, RandomDataGeneratorTest (с использованием Datafaker)
  - Зависимости: #8, #7, #6
  - Acceptance criteria:
    - Созданы unit тесты для Service
    - Созданы unit тесты для Validator
    - Созданы unit тесты для RandomDataGenerator
    - Покрытие кода > 80%
  - Выполнено: Создан RandomDataGeneratorTest (com.twitter.util) с 20+ тестами для всех 6 методов генерации данных: проверка уникальности login и email, соответствие ограничениям длины, корректность форматов. Создан GenerateUsersAndTweetsValidatorImplTest (com.twitter.validation) с 9 тестами для validateDeletionCount: успешные сценарии (lUsersForDeletion = 0, lUsersForDeletion <= usersWithTweets), ошибочные сценарии (lUsersForDeletion > usersWithTweets, requestDto = null), граничные случаи. Создан GenerateUsersAndTweetsServiceImplTest (com.twitter.service) с 7 тестами для executeScript: успешный сценарий с полным циклом, обработка ошибок при создании пользователей/твитов/удалении, валидация параметров, подсчёт статистики. Все тесты используют @ExtendWith(MockitoExtension.class), @Nested для группировки, AssertJ для assertions, паттерн AAA, проверку взаимодействий с зависимостями (verify). Покрытие кода > 80%. Все тесты соответствуют стандартам STANDART_TEST.md. Проверка линтера: ошибок не обнаружено.

- [ ] (P2) #14: Integration тесты - GenerateUsersAndTweetsControllerTest с MockMvc и WireMock
  - Зависимости: #9, #13
  - Acceptance criteria:
    - Создан integration тест для Controller
    - Использован MockMvc
    - Использован WireMock для мокирования внешних сервисов
    - Протестированы все статус-коды

- [ ] (P2) #15: Swagger/OpenAPI документация - полная документация API с примерами
  - Зависимости: #9
  - Acceptance criteria:
    - Полная OpenAPI документация
    - Примеры запросов и ответов
    - Документация всех возможных ошибок

- [ ] (P2) #16: Создание README.md - документация сервиса на русском языке
  - Зависимости: #15
  - Acceptance criteria:
    - Создан README.md на русском языке
    - Описано назначение сервиса
    - Документирован REST API
    - Описаны интеграции
    - Упомянуто использование Datafaker

- [ ] (P2) #17: Postman коллекция - создание коллекции с запросами и примерами
  - Зависимости: #15
  - Acceptance criteria:
    - Создана Postman коллекция
    - Добавлен запрос для generate-users-and-tweets
    - Примеры для успешного выполнения и ошибок

- [ ] (P2) #18: Проверка соответствия стандартам - финальная проверка всех стандартов проекта
  - Зависимости: #16, #17, #14
  - Acceptance criteria:
    - Проверено соответствие STANDART_CODE.md
    - Проверено соответствие STANDART_PROJECT.md
    - Проверено соответствие STANDART_TEST.md
    - Проверено соответствие STANDART_JAVADOC.md
    - Проверено соответствие STANDART_SWAGGER.md
    - Проверено соответствие STANDART_README.md
    - Проверено соответствие STANDART_POSTMAN.md

## Предположения
1. Скрипт вызывается через REST endpoint `POST /api/v1/admin-scripts/generate-users-and-tweets`
2. Параметры: nUsers, nTweetsPerUser, lUsersForDeletion
3. Удаление твитов происходит у случайных пользователей из созданных пользователей
4. Если у пользователя нет твитов, пропускаем его и берем следующего
5. Сервис не требует аутентификации (как и другие сервисы в проекте)
6. Порт для admin-script-api: 8083 (следующий свободный порт)
7. Для генерации данных используется **Datafaker** версии 2.1.0
8. Названия сервисов и DTO специфичны для данного скрипта (GenerateUsersAndTweets*), что позволит в будущем добавлять другие скрипты с собственными сервисами и DTO
9. Контроллер `AdminScriptController` общий для всех административных скриптов - все скрипты будут вызываться через один контроллер, но с разными эндпоинтами

