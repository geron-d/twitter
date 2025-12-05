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

- [ ] (P1) #2: Настройка Gradle модуля - добавить в settings.gradle, создать build.gradle с зависимостями (включая Datafaker)
  - Зависимости: нет
  - Acceptance criteria:
    - Модуль добавлен в settings.gradle
    - Создан build.gradle с всеми необходимыми зависимостями
    - Datafaker добавлен в dependencyManagement корневого build.gradle

- [ ] (P1) #3: Реализация DTO (Records) - GenerateUsersAndTweetsRequestDto и GenerateUsersAndTweetsResponseDto с валидацией и @Schema
  - Зависимости: #1
  - Acceptance criteria:
    - Создан GenerateUsersAndTweetsRequestDto с полями nUsers, nTweetsPerUser, lUsersForDeletion
    - Создан GenerateUsersAndTweetsResponseDto со статистикой и списками ID
    - Добавлены валидационные аннотации
    - Добавлены @Schema аннотации для Swagger

- [ ] (P1) #4: Реализация Feign Clients - UsersApiClient и TweetsApiClient для интеграции с другими сервисами
  - Зависимости: #1
  - Acceptance criteria:
    - Создан UsersApiClient с методом createUser
    - Создан TweetsApiClient с методами createTweet, deleteTweet, getUserTweets
    - Настроены URL и path для сервисов

- [ ] (P1) #5: Реализация Gateways - UsersGateway и TweetsGateway с обработкой ошибок
  - Зависимости: #4
  - Acceptance criteria:
    - Создан UsersGateway с обработкой ошибок
    - Создан TweetsGateway с обработкой ошибок
    - Добавлено логирование

- [ ] (P1) #6: Реализация RandomDataGenerator с использованием Datafaker - утилита для генерации рандомных данных пользователей и твитов
  - Зависимости: #1, #2
  - Acceptance criteria:
    - Создан RandomDataGenerator с методами генерации данных
    - Используется Datafaker для генерации
    - Обеспечена уникальность login и email через timestamp/UUID

- [ ] (P1) #7: Реализация Validator - GenerateUsersAndTweetsValidator для валидации параметров скрипта
  - Зависимости: #3
  - Acceptance criteria:
    - Создан интерфейс GenerateUsersAndTweetsValidator
    - Создана реализация GenerateUsersAndTweetsValidatorImpl
    - Валидация параметров (n > 0, l > 0, l <= количество пользователей с твитами)

- [ ] (P1) #8: Реализация Service - GenerateUsersAndTweetsService и GenerateUsersAndTweetsServiceImpl с бизнес-логикой выполнения скрипта
  - Зависимости: #3, #5, #6, #7
  - Acceptance criteria:
    - Создан интерфейс GenerateUsersAndTweetsService
    - Создана реализация GenerateUsersAndTweetsServiceImpl
    - Реализована логика создания пользователей, твитов и удаления

- [ ] (P1) #9: Реализация Controller - AdminScriptApi и AdminScriptController с REST endpoint для всех скриптов
  - Зависимости: #8
  - Acceptance criteria:
    - Создан AdminScriptApi с OpenAPI аннотациями
    - Создан AdminScriptController с @LoggableRequest
    - Реализован endpoint POST /api/v1/admin-scripts/generate-users-and-tweets

- [ ] (P1) #10: Реализация Config - OpenApiConfig и FeignConfig для конфигурации сервиса
  - Зависимости: #9
  - Acceptance criteria:
    - Создан OpenApiConfig
    - Создан FeignConfig с @EnableFeignClients

- [ ] (P1) #11: Создание application.yml - конфигурация порта, URLs других сервисов, Feign настройки
  - Зависимости: #10
  - Acceptance criteria:
    - Создан application.yml
    - Настроен порт 8083
    - Настроены URLs для users-api и tweet-api
    - Настроены Feign настройки

- [ ] (P2) #12: JavaDoc документация - добавить JavaDoc для всех public классов и методов
  - Зависимости: #9, #8, #7, #6
  - Acceptance criteria:
    - JavaDoc для всех public классов
    - JavaDoc для всех public методов
    - @author geron, @version 1.0
    - @param, @return, @throws для всех методов

- [ ] (P2) #13: Unit тесты - GenerateUsersAndTweetsServiceImplTest, GenerateUsersAndTweetsValidatorImplTest, RandomDataGeneratorTest (с использованием Datafaker)
  - Зависимости: #8, #7, #6
  - Acceptance criteria:
    - Созданы unit тесты для Service
    - Созданы unit тесты для Validator
    - Созданы unit тесты для RandomDataGenerator
    - Покрытие кода > 80%

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

