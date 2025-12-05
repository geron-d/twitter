---
name: Admin Script API Service
overview: "Реализация отдельного микросервиса admin-script-api для выполнения административных скриптов: создание n пользователей с рандомными данными, добавление n твитов для каждого пользователя и удаление по 1 твиту у l случайных пользователей."
todos:
  - id: analysis
    content: Анализ требований и проектирование API - определить структуру DTO, параметры валидации, стратегию генерации данных с использованием Datafaker
    status: pending
  - id: gradle_setup
    content: Настройка Gradle модуля - добавить в settings.gradle, создать build.gradle с зависимостями (включая Datafaker)
    status: pending
  - id: dto
    content: Реализация DTO (Records) - GenerateUsersAndTweetsRequestDto и GenerateUsersAndTweetsResponseDto с валидацией и @Schema
    status: pending
    dependencies:
      - analysis
  - id: feign_clients
    content: Реализация Feign Clients - UsersApiClient и TweetsApiClient для интеграции с другими сервисами
    status: pending
    dependencies:
      - analysis
  - id: gateways
    content: Реализация Gateways - UsersGateway и TweetsGateway с обработкой ошибок
    status: pending
    dependencies:
      - feign_clients
  - id: random_generator
    content: Реализация RandomDataGenerator с использованием Datafaker - утилита для генерации рандомных данных пользователей и твитов
    status: pending
    dependencies:
      - analysis
      - gradle_setup
  - id: validator
    content: Реализация Validator - GenerateUsersAndTweetsValidator для валидации параметров скрипта
    status: pending
    dependencies:
      - dto
  - id: service
    content: Реализация Service - GenerateUsersAndTweetsService и GenerateUsersAndTweetsServiceImpl с бизнес-логикой выполнения скрипта
    status: pending
    dependencies:
      - dto
      - gateways
      - random_generator
      - validator
  - id: controller
    content: Реализация Controller - AdminScriptApi и AdminScriptController с REST endpoint для всех скриптов
    status: pending
    dependencies:
      - service
  - id: config
    content: Реализация Config - OpenApiConfig и FeignConfig для конфигурации сервиса
    status: pending
    dependencies:
      - controller
  - id: application_yml
    content: Создание application.yml - конфигурация порта, URLs других сервисов, Feign настройки
    status: pending
    dependencies:
      - config
  - id: javadoc
    content: JavaDoc документация - добавить JavaDoc для всех public классов и методов
    status: pending
    dependencies:
      - controller
      - service
      - validator
      - random_generator
  - id: unit_tests
    content: Unit тесты - GenerateUsersAndTweetsServiceImplTest, GenerateUsersAndTweetsValidatorImplTest, RandomDataGeneratorTest (с использованием Datafaker)
    status: pending
    dependencies:
      - service
      - validator
      - random_generator
  - id: integration_tests
    content: Integration тесты - GenerateUsersAndTweetsControllerTest с MockMvc и WireMock
    status: pending
    dependencies:
      - controller
      - unit_tests
  - id: swagger
    content: Swagger/OpenAPI документация - полная документация API с примерами
    status: pending
    dependencies:
      - controller
  - id: readme
    content: Создание README.md - документация сервиса на русском языке
    status: pending
    dependencies:
      - swagger
  - id: postman
    content: Postman коллекция - создание коллекции с запросами и примерами
    status: pending
    dependencies:
      - swagger
  - id: standards_check
    content: Проверка соответствия стандартам - финальная проверка всех стандартов проекта
    status: pending
    dependencies:
      - readme
      - postman
      - integration_tests
---

# План реализации сервиса admin-script-api

## Анализ задачи

### Назначение сервиса

Отдельный микросервис для выполнения административных скриптов, который:

1. Создает n пользователей с рандомными данными
2. Для каждого созданного пользователя создает n твитов с рандомным контентом
3. Удаляет по 1 твиту у l случайных пользователей

### Входные данные

- REST endpoint: `POST /api/v1/admin-scripts/generate-users-and-tweets`
- Request DTO с параметрами:
  - `nUsers` (Integer) - количество пользователей для создания
  - `nTweetsPerUser` (Integer) - количество твитов для каждого пользователя
  - `lUsersForDeletion` (Integer) - количество пользователей, у которых нужно удалить по 1 твиту

### Выходные данные

- Response DTO с результатами выполнения:
  - Список созданных пользователей (ID)
  - Список созданных твитов (ID)
  - Список удаленных твитов (ID)
  - Статистика выполнения

### Ключевые компоненты

1. **Controller** - REST endpoint для запуска скрипта
2. **Service** - бизнес-логика выполнения скрипта
3. **Feign Clients** - интеграция с users-api и tweet-api
4. **Gateways** - обертки для Feign клиентов
5. **DTO** - Request/Response для API
6. **Validator** - валидация параметров
7. **Util** - генерация рандомных данных с использованием **Datafaker**
8. **Config** - конфигурация OpenAPI и Feign

### Зависимости

- **users-api** - для создания пользователей (POST /api/v1/users)
- **tweet-api** - для создания твитов (POST /api/v1/tweets) и удаления (DELETE /api/v1/tweets/{id})
- **common-lib** - для логирования, обработки исключений
- **Spring Cloud OpenFeign** - для межсервисной коммуникации
- **Datafaker** (net.datafaker:datafaker:2.1.0) - для генерации фейковых данных

### Риски и узкие места

1. **Производительность**: массовое создание пользователей и твитов может быть медленным

   - Решение: использовать batch операции, если доступны, или параллельную обработку

2. **Уникальность данных**: генерация уникальных login и email

   - Решение: использовать Datafaker для генерации данных + timestamp/UUID для обеспечения уникальности

3. **Обработка ошибок**: частичный успех при создании

   - Решение: возвращать детальную статистику успешных/неуспешных операций

4. **Удаление твитов**: нужно убедиться, что у пользователя есть твиты для удаления

   - Решение: проверять наличие твитов перед удалением

### Затронутые стандарты

- **STANDART_CODE.md**: Java 24, Records для DTO, MapStruct, Lombok, layered architecture
- **STANDART_PROJECT.md**: @LoggableRequest, исключения из common-lib, Feign Clients
- **STANDART_TEST.md**: Unit тесты для Service, Validator, Integration тесты для Controller
- **STANDART_JAVADOC.md**: JavaDoc для всех public классов и методов
- **STANDART_SWAGGER.md**: OpenAPI документация для REST endpoint
- **STANDART_README.md**: README на русском языке
- **STANDART_POSTMAN.md**: Postman коллекция для нового endpoint

## План реализации

### 1. Анализ и проектирование

#### 1.1 Анализ требований

- Определить структуру Request/Response DTO
- Определить параметры валидации (минимальные/максимальные значения для n, l)
- Определить стратегию генерации рандомных данных с использованием **Datafaker**
- Определить структуру ответа со статистикой

#### 1.2 Проектирование API

- REST endpoint: `POST /api/v1/admin-scripts/generate-users-and-tweets`
- Request DTO: `GenerateUsersAndTweetsRequestDto` с полями nUsers, nTweetsPerUser, lUsersForDeletion
- Response DTO: `GenerateUsersAndTweetsResponseDto` со статистикой и списками ID
- **Примечание**: Названия специфичны для данного скрипта, что позволит в будущем добавлять другие скрипты с собственными DTO и эндпоинтами

#### 1.3 Проектирование интеграций

- Feign Client для users-api: `POST /api/v1/users` (создание пользователя)
- Feign Client для tweet-api: `POST /api/v1/tweets` (создание твита), `DELETE /api/v1/tweets/{id}` (удаление твита)
- Gateway компоненты для обработки ошибок

### 2. Реализация кода

#### 2.1 Структура модуля

Создать новый модуль `services/admin-script-api` со структурой:

```
services/admin-script-api/
├── build.gradle
├── Dockerfile
├── README.md
└── src/
    ├── main/
    │   ├── java/com/twitter/
    │   │   ├── Application.java
    │   │   ├── config/
    │   │   │   ├── OpenApiConfig.java
    │   │   │   └── FeignConfig.java
    │   │   ├── controller/
    │   │   │   ├── AdminScriptApi.java
    │   │   │   └── AdminScriptController.java
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   └── GenerateUsersAndTweetsRequestDto.java
    │   │   │   └── response/
    │   │   │       └── GenerateUsersAndTweetsResponseDto.java
    │   │   ├── service/
    │   │   │   ├── GenerateUsersAndTweetsService.java
    │   │   │   └── GenerateUsersAndTweetsServiceImpl.java
    │   │   ├── client/
    │   │   │   ├── UsersApiClient.java
    │   │   │   └── TweetsApiClient.java
    │   │   ├── gateway/
    │   │   │   ├── UsersGateway.java
    │   │   │   └── TweetsGateway.java
    │   │   ├── validation/
    │   │   │   ├── GenerateUsersAndTweetsValidator.java
    │   │   │   └── GenerateUsersAndTweetsValidatorImpl.java
    │   │   └── util/
    │   │       └── RandomDataGenerator.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/twitter/
            ├── controller/
            │   └── AdminScriptControllerTest.java
            ├── service/
            │   └── GenerateUsersAndTweetsServiceImplTest.java
            ├── validation/
            │   └── GenerateUsersAndTweetsValidatorImplTest.java
            └── util/
                └── RandomDataGeneratorTest.java
```

#### 2.2 Gradle конфигурация

- Добавить модуль в `settings.gradle`
- Создать `build.gradle` с зависимостями:
  - common-lib, database (shared модули)
  - Spring Boot Web, Validation, Actuator
  - Spring Cloud OpenFeign
  - OpenAPI/Swagger
  - Lombok, MapStruct
  - **Datafaker** (net.datafaker:datafaker:2.1.0) - для генерации фейковых данных
  - PostgreSQL (runtime)
  - Test dependencies (JUnit, Mockito, AssertJ, Testcontainers, WireMock)

**Пример зависимости Datafaker в build.gradle:**
```gradle
dependencies {
    // Datafaker для генерации фейковых данных
    implementation 'net.datafaker:datafaker:2.1.0'
    // ... другие зависимости
}
```

#### 2.3 DTO (Records)

- **GenerateUsersAndTweetsRequestDto**: nUsers, nTweetsPerUser, lUsersForDeletion с валидацией
- **GenerateUsersAndTweetsResponseDto**: статистика (createdUsers, createdTweets, deletedTweets), списки ID
- **Примечание**: Названия DTO специфичны для данного скрипта, что позволит в будущем добавлять другие скрипты с собственными DTO

#### 2.4 Feign Clients

- **UsersApiClient**: метод createUser(UserRequestDto)
- **TweetsApiClient**: методы createTweet(CreateTweetRequestDto), deleteTweet(UUID tweetId), getUserTweets(UUID userId)

#### 2.5 Gateways

- **UsersGateway**: обертка для UsersApiClient с обработкой ошибок
- **TweetsGateway**: обертка для TweetsApiClient с обработкой ошибок

#### 2.6 Service

- **GenerateUsersAndTweetsService**: интерфейс с методом executeScript(GenerateUsersAndTweetsRequestDto)
- **GenerateUsersAndTweetsServiceImpl**: реализация логики:

  1. Валидация параметров
  2. Создание n пользователей с рандомными данными (используя RandomDataGenerator с Datafaker)
  3. Для каждого пользователя создание n твитов (используя RandomDataGenerator с Datafaker)
  4. Выбор l случайных пользователей и удаление по 1 твиту у каждого
  5. Формирование ответа со статистикой

#### 2.7 Validator

- **GenerateUsersAndTweetsValidator**: интерфейс для валидации параметров
- **GenerateUsersAndTweetsValidatorImpl**: реализация валидации параметров (n > 0, l > 0, l <= количество пользователей с твитами)

#### 2.8 Util (RandomDataGenerator с Datafaker)

- **RandomDataGenerator**: утилита для генерации рандомных данных с использованием **Datafaker**
  
  **Методы для генерации:**
  - `generateLogin()` - рандомный login с использованием `faker.internet().username()` + timestamp/UUID для уникальности
  - `generateEmail()` - рандомный email с использованием `faker.internet().emailAddress()` + timestamp для уникальности
  - `generateFirstName()` - рандомное имя с использованием `faker.name().firstName()`
  - `generateLastName()` - рандомная фамилия с использованием `faker.name().lastName()`
  - `generatePassword()` - рандомный пароль с использованием `faker.internet().password()` (минимум 8 символов)
  - `generateTweetContent()` - рандомный контент твита (1-280 символов) с использованием `faker.lorem().sentence()` или `faker.lorem().paragraph()` с обрезкой до 280 символов

  **Пример реализации:**
  ```java
  @Component
  @RequiredArgsConstructor
  public class RandomDataGenerator {
      private final Faker faker = new Faker();
      
      public String generateLogin() {
          return faker.internet().username() + "_" + System.currentTimeMillis();
      }
      
      public String generateEmail() {
          return faker.internet().emailAddress().replace("@", "_" + System.currentTimeMillis() + "@");
      }
      
      public String generateFirstName() {
          return faker.name().firstName();
      }
      
      public String generateLastName() {
          return faker.name().lastName();
      }
      
      public String generatePassword() {
          return faker.internet().password(8, 20, true, true, true);
      }
      
      public String generateTweetContent() {
          String content = faker.lorem().sentence();
          if (content.length() > 280) {
              content = content.substring(0, 277) + "...";
          }
          return content;
      }
  }
  ```

#### 2.9 Controller

- **AdminScriptApi**: OpenAPI интерфейс с аннотациями для всех административных скриптов
- **AdminScriptController**: REST контроллер с @LoggableRequest, реализующий AdminScriptApi
- **Примечание**: Контроллер общий для всех административных скриптов. Каждый скрипт имеет свой эндпоинт (например, `/api/v1/admin-scripts/generate-users-and-tweets`), свой сервис и свои DTO, но все вызываются через один контроллер

#### 2.10 Config

- **OpenApiConfig**: конфигурация OpenAPI для admin-script-api
- **FeignConfig**: @EnableFeignClients конфигурация

### 3. Документация кода (JavaDoc)

- JavaDoc для всех public классов и методов
- @author geron, @version 1.0
- @param, @return, @throws для всех методов
- JavaDoc для DTO Records с @param для всех компонентов
- В JavaDoc для RandomDataGenerator указать использование Datafaker

### 4. Тестирование

#### 4.1 Unit тесты

- **GenerateUsersAndTweetsServiceImplTest**: тесты для всех сценариев выполнения скрипта
- **GenerateUsersAndTweetsValidatorImplTest**: тесты валидации параметров
- **RandomDataGeneratorTest**: тесты генерации рандомных данных с использованием Datafaker
  - Проверка генерации уникальных login и email
  - Проверка валидности email формата
  - Проверка длины пароля (минимум 8 символов)
  - Проверка длины контента твита (1-280 символов)

#### 4.2 Integration тесты

- **AdminScriptControllerTest**: тесты REST endpoint `/api/v1/admin-scripts/generate-users-and-tweets` с MockMvc
- Использование WireMock для мокирования users-api и tweet-api
- Тестирование всех статус-кодов (200, 400, 500)

### 5. Swagger/OpenAPI документация

- OpenAPI interface (AdminScriptApi) с @Tag, @Operation, @ApiResponses для эндпоинта `/api/v1/admin-scripts/generate-users-and-tweets`
- @Schema аннотации для всех DTO (GenerateUsersAndTweetsRequestDto, GenerateUsersAndTweetsResponseDto)
- Примеры запросов и ответов
- Документация всех возможных ошибок
- **Примечание**: В будущем в AdminScriptApi можно будет добавлять новые методы для других скриптов

### 6. Обновление README

- Создать README.md для admin-script-api на русском языке
- Описать назначение сервиса
- Документировать REST API
- Описать интеграции с users-api и tweet-api
- Упомянуть использование Datafaker для генерации данных
- Примеры использования

### 7. Postman коллекции

- Создать коллекцию `postman/admin-script-api/`
- Добавить запрос "generate users and tweets" для эндпоинта `POST /api/v1/admin-scripts/generate-users-and-tweets`
- Примеры для успешного выполнения и ошибок

### 8. Проверка соответствия стандартам

- Проверить соответствие всем стандартам проекта
- Убедиться в правильности структуры пакетов
- Проверить использование Java 24 features
- Проверить соответствие архитектурным паттернам

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

## Риски

1. **Производительность**: массовые операции могут быть медленными

   - Митигация: добавить логирование прогресса, рассмотреть асинхронную обработку в будущем

2. **Уникальность данных**: возможны коллизии при генерации

   - Митигация: использовать Datafaker для генерации качественных данных + timestamp/UUID для обеспечения уникальности login и email

3. **Частичные ошибки**: некоторые операции могут не выполниться

   - Митигация: возвращать детальную статистику успешных/неуспешных операций

4. **Зависимость от других сервисов**: users-api и tweet-api должны быть доступны

   - Митигация: обработка ошибок через Gateways, возврат понятных сообщений об ошибках

5. **Зависимость от Datafaker**: библиотека должна быть доступна

   - Митигация: использовать стабильную версию Datafaker 2.1.0, добавить в dependencyManagement корневого build.gradle

## Метрики успешности

1. Все тесты проходят (unit и integration)
2. Покрытие кода > 80%
3. Соответствие всем стандартам проекта
4. Swagger документация полная и корректная
5. README содержит всю необходимую информацию
6. Postman коллекция работает корректно
7. Скрипт успешно создает пользователей, твиты и удаляет твиты
8. Генерация данных с использованием Datafaker работает корректно и обеспечивает уникальность

## Примечания

- Сервис использует ту же архитектуру, что и users-api и tweet-api
- Интеграция через Feign Clients аналогична интеграции tweet-api с users-api
- **Генерация рандомных данных выполняется с использованием библиотеки Datafaker** - современной библиотеки для генерации фейковых данных, которая поддерживает множество локалей и обеспечивает качественную генерацию имен, email, текста и других данных
- Для обеспечения уникальности login и email используется комбинация Datafaker + timestamp/UUID
- Обработка ошибок должна быть детальной для администраторов
- Datafaker версии 2.1.0 должна быть добавлена в dependencyManagement корневого build.gradle для централизованного управления версиями
- **Архитектура для масштабируемости**: 
  - **Общий контроллер**: `AdminScriptController` и `AdminScriptApi` используются для всех административных скриптов
  - **Специфичные компоненты**: Каждый скрипт имеет свой сервис, DTO и валидатор с специфичными названиями (например, `GenerateUsersAndTweetsService`, `GenerateUsersAndTweetsRequestDto`)
  - **Специфичные эндпоинты**: Каждый скрипт имеет свой эндпоинт в рамках одного контроллера
