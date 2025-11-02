# Интеграционные тесты для метода createTweet

## Метод: TweetController.createTweet

**Эндпоинт**: `POST /api/v1/tweets`  
**Описание**: Создание нового твита с валидацией контента и проверкой существования пользователя через users-api.

---

## 1. Успешные сценарии (Happy Path)

### 1.1. Создание твита с валидными данными
- **Название**: `createTweet_WithValidData_ShouldReturn201Created`
- **Цель теста**: Проверить успешное создание твита с корректными входными данными
- **Ожидаемый результат**:
  - HTTP статус 201 (Created)
  - В теле ответа возвращается `TweetResponseDto` с корректными данными
  - Поле `id` заполнено (UUID)
  - Поля `userId` и `content` соответствуют запросу
  - Поля `createdAt` и `updatedAt` автоматически заполнены
  - Твит сохранен в базе данных
  - Выполнен HTTP-запрос к users-api для проверки существования пользователя

### 1.2. Создание твита с минимальной длиной контента (1 символ)
- **Название**: `createTweet_WithMinimalContent_ShouldReturn201Created`
- **Цель теста**: Проверить, что твит с минимально допустимой длиной контента (1 символ) успешно создается
- **Ожидаемый результат**:
  - HTTP статус 201 (Created)
  - Твит успешно создан и сохранен в БД

### 1.3. Создание твита с максимальной длиной контента (280 символов)
- **Название**: `createTweet_WithMaximumContent_ShouldReturn201Created`
- **Цель теста**: Проверить, что твит с максимально допустимой длиной контента (280 символов) успешно создается
- **Ожидаемый результат**:
  - HTTP статус 201 (Created)
  - Твит успешно создан с полным содержимым в 280 символов

### 1.4. Создание твита с контентом из специальных символов
- **Название**: `createTweet_WithSpecialCharacters_ShouldReturn201Created`
- **Цель теста**: Проверить корректную обработку специальных символов (эмодзи, Unicode, HTML-сущности) в контенте
- **Ожидаемый результат**:
  - HTTP статус 201 (Created)
  - Контент с специальными символами корректно сохранен

---

## 2. Граничные условия (Boundary Cases)

### 2.1. Попытка создания твита с пустым контентом
- **Название**: `createTweet_WithEmptyContent_ShouldReturn400BadRequest`
- **Цель теста**: Проверить валидацию пустого контента
- **Ожидаемый результат**:
  - HTTP статус 400 (Bad Request)
  - В ответе `ProblemDetail` с типом ошибки `FormatValidationException`
  - Сообщение об ошибке указывает на пустой контент
  - Твит не сохранен в БД

### 2.2. Попытка создания твита с контентом из одних пробелов
- **Название**: `createTweet_WithWhitespaceOnlyContent_ShouldReturn400BadRequest`
- **Цель теста**: Проверить валидацию контента, состоящего только из пробельных символов
- **Ожидаемый результат**:
  - HTTP статус 400 (Bad Request)
  - В ответе `ProblemDetail` с типом ошибки `FormatValidationException`
  - Сообщение об ошибке указывает на недопустимость пробельного контента
  - Твит не сохранен в БД

### 2.3. Попытка создания твита с контентом длиной 0 символов
- **Название**: `createTweet_WithZeroLengthContent_ShouldReturn400BadRequest`
- **Цель теста**: Проверить валидацию пустой строки
- **Ожидаемый результат**:
  - HTTP статус 400 (Bad Request)
  - В ответе `ProblemDetail` с валидационной ошибкой
  - Твит не сохранен в БД

### 2.4. Попытка создания твита с контентом длиной 281 символ
- **Название**: `createTweet_WithContentExceedingMaxLength_ShouldReturn400BadRequest`
- **Цель теста**: Проверить валидацию превышения максимальной длины контента
- **Ожидаемый результат**:
  - HTTP статус 400 (Bad Request)
  - В ответе `ProblemDetail` с валидационной ошибкой о превышении максимальной длины
  - Твит не сохранен в БД

### 2.5. Попытка создания твита с null userId
- **Название**: `createTweet_WithNullUserId_ShouldReturn400BadRequest`
- **Цель теста**: Проверить валидацию отсутствия userId в запросе
- **Ожидаемый результат**:
  - HTTP статус 400 (Bad Request)
  - В ответе `ProblemDetail` с валидационной ошибкой о null userId
  - HTTP-запрос к users-api не выполнен
  - Твит не сохранен в БД

### 2.6. Попытка создания твита с несуществующим userId
- **Название**: `createTweet_WithNonExistentUserId_ShouldReturn409Conflict`
- **Цель теста**: Проверить обработку ситуации, когда пользователь с указанным userId не существует в users-api
- **Ожидаемый результат**:
  - HTTP статус 409 (Conflict)
  - В ответе `ProblemDetail` с типом ошибки `BusinessRuleValidationException`
  - Код ошибки `USER_NOT_EXISTS`
  - Выполнен HTTP-запрос к users-api
  - Твит не сохранен в БД

### 2.7. Попытка создания твита с контентом длиной 279 символов
- **Название**: `createTweet_WithContentOneCharBelowMax_ShouldReturn201Created`
- **Цель теста**: Проверить, что контент длиной на 1 символ меньше максимального допускается
- **Ожидаемый результат**:
  - HTTP статус 201 (Created)
  - Твит успешно создан

### 2.8. Попытка создания твита с контентом длиной 2 символа
- **Название**: `createTweet_WithContentOneCharAboveMin_ShouldReturn201Created`
- **Цель теста**: Проверить, что контент длиной на 1 символ больше минимального допускается
- **Ожидаемый результат**:
  - HTTP статус 201 (Created)
  - Твит успешно создан

---

## 3. Исключения и ошибки (Exceptional Cases)

### 3.1. Ошибка при запросе к users-api (500 Internal Server Error)
- **Название**: `createTweet_WhenUsersApiReturns500_ShouldHandleGracefully`
- **Цель теста**: Проверить обработку ситуации, когда users-api возвращает ошибку 500
- **Ожидаемый результат**:
  - HTTP статус 409 (Conflict) или 500 (Internal Server Error)
  - В ответе `ProblemDetail` с ошибкой бизнес-правила или внутренней ошибки
  - Твит не сохранен в БД

### 3.2. Таймаут при запросе к users-api
- **Название**: `createTweet_WhenUsersApiTimeout_ShouldHandleGracefully`
- **Цель теста**: Проверить обработку таймаута при запросе к users-api
- **Ожидаемый результат**:
  - HTTP статус 409 (Conflict) или 500 (Internal Server Error)
  - В ответе `ProblemDetail` с соответствующей ошибкой
  - Твит не сохранен в БД

### 3.3. Ошибка сети при запросе к users-api
- **Название**: `createTweet_WhenUsersApiNetworkError_ShouldHandleGracefully`
- **Цель теста**: Проверить обработку сетевой ошибки при подключении к users-api
- **Ожидаемый результат**:
  - HTTP статус 409 (Conflict) или 500 (Internal Server Error)
  - В ответе `ProblemDetail` с соответствующей ошибкой
  - Твит не сохранен в БД

### 3.4. Ошибка базы данных при сохранении твита
- **Название**: `createTweet_WhenDatabaseError_ShouldReturn500InternalServerError`
- **Цель теста**: Проверить обработку ошибки базы данных при сохранении (например, нарушение ограничений БД)
- **Ожидаемый результат**:
  - HTTP статус 500 (Internal Server Error)
  - В ответе `ProblemDetail` с ошибкой внутреннего сервера
  - Твит не сохранен в БД

### 3.5. Некорректный JSON в теле запроса
- **Название**: `createTweet_WithInvalidJson_ShouldReturn400BadRequest`
- **Цель теста**: Проверить обработку некорректного JSON в теле запроса
- **Ожидаемый результат**:
  - HTTP статус 400 (Bad Request)
  - В ответе `ProblemDetail` с ошибкой парсинга JSON

### 3.6. Отсутствие тела запроса
- **Название**: `createTweet_WithMissingBody_ShouldReturn400BadRequest`
- **Цель теста**: Проверить обработку запроса без тела
- **Ожидаемый результат**:
  - HTTP статус 400 (Bad Request)
  - В ответе `ProblemDetail` с ошибкой о недостающих данных

### 3.7. Неверный формат userId (не UUID)
- **Название**: `createTweet_WithInvalidUserIdFormat_ShouldReturn400BadRequest`
- **Цель теста**: Проверить обработку userId в неверном формате (не UUID)
- **Ожидаемый результат**:
  - HTTP статус 400 (Bad Request)
  - В ответе `ProblemDetail` с ошибкой валидации формата

### 3.8. Users-api возвращает 404 для существующего пользователя (неожиданное поведение)
- **Название**: `createTweet_WhenUsersApiReturns404_ShouldHandleGracefully`
- **Цель теста**: Проверить обработку ситуации, когда users-api возвращает 404, хотя пользователь должен существовать
- **Ожидаемый результат**:
  - HTTP статус 409 (Conflict)
  - В ответе `ProblemDetail` с типом ошибки `BusinessRuleValidationException`
  - Код ошибки `USER_NOT_EXISTS`
  - Твит не сохранен в БД

### 3.9. Users-api возвращает некорректный ответ (не JSON)
- **Название**: `createTweet_WhenUsersApiReturnsInvalidResponse_ShouldHandleGracefully`
- **Цель теста**: Проверить обработку ситуации, когда users-api возвращает некорректный формат ответа
- **Ожидаемый результат**:
  - HTTP статус 409 (Conflict) или 500 (Internal Server Error)
  - В ответе `ProblemDetail` с соответствующей ошибкой
  - Твит не сохранен в БД

### 3.10. Одновременное создание нескольких твитов одним пользователем
- **Название**: `createTweet_MultipleTweetsBySameUser_ShouldCreateAllSuccessfully`
- **Цель теста**: Проверить, что один пользователь может создавать несколько твитов подряд
- **Ожидаемый результат**:
  - Все запросы возвращают HTTP статус 201 (Created)
  - Все твиты сохранены в БД с уникальными ID
  - Каждый твит имеет корректные временные метки

---

## Пошаговый план написания интеграционных тестов

### Шаг 1: Подготовка зависимостей и конфигурации

1. **Добавить зависимости в `build.gradle`**:
   - Убедиться, что присутствуют:
     - `spring-boot-starter-test`
     - `org.testcontainers:junit-jupiter`
     - `org.testcontainers:postgresql`
     - `com.github.tomakehurst:wiremock-jre8` (для эмуляции users-api)
     - `org.springframework.cloud:spring-cloud-starter-contract-stub-runner` (альтернатива WireMock)

2. **Создать тестовый профиль конфигурации**:
   - Файл `application-integration-test.yml` в `src/test/resources/`
   - Настройка порта WireMock для users-api
   - Отключение автоматической миграции БД (если используется)
   - Настройка логирования для тестов

### Шаг 2: Настройка тестового окружения

1. **Создать базовый класс для интеграционных тестов**:
   - Класс `TweetControllerIntegrationTestBase`
   - Аннотации: `@SpringBootTest`, `@AutoConfigureMockMvc`, `@Testcontainers`
   - Настройка Testcontainers для PostgreSQL
   - Настройка WireMock для users-api
   - Методы для очистки БД между тестами (`@BeforeEach`, `@AfterEach`)

2. **Конфигурация Testcontainers**:
   - Использование `@Container` для PostgreSQL контейнера
   - Автоматический выбор свободных портов
   - Настройка подключения к БД через JDBC URL из контейнера
   - Автоматический запуск/остановка контейнера

3. **Конфигурация WireMock**:
   - Создание стабов для эндпоинта `/api/v1/users/{userId}/exists`
   - Настройка базового URL users-api для тестов
   - Использование `@WireMockTest` или ручная настройка через `WireMockServer`

### Шаг 3: Создание тестового класса

1. **Структура класса `TweetControllerIntegrationTest`**:
   ```java
   @SpringBootTest
   @AutoConfigureMockMvc
   @Testcontainers
   class TweetControllerIntegrationTest {
       // Конфигурация Testcontainers и WireMock
       // Методы для подготовки данных
       // @Nested класс CreateTweetTests
   }
   ```

2. **Использование `MockMvc` или `TestRestTemplate`**:
   - Рекомендуется `MockMvc` для unit-подобных тестов
   - Или `TestRestTemplate`/`WebTestClient` для более реальных HTTP-запросов

### Шаг 4: Реализация тестовых сценариев

1. **Паттерн AAA для каждого теста**:
   - **Arrange**: Подготовка данных (создание запроса, настройка WireMock стабов, очистка БД)
   - **Act**: Выполнение HTTP-запроса через `MockMvc` или `TestRestTemplate`
   - **Assert**: Проверка HTTP статуса, тела ответа, состояния БД

2. **Утилитные методы для подготовки данных**:
   - `createValidCreateTweetRequest()` - создание валидного запроса
   - `setupUserExistsStub(UUID userId, boolean exists)` - настройка WireMock стаба
   - `verifyTweetInDatabase(UUID tweetId)` - проверка наличия твита в БД
   - `cleanDatabase()` - очистка БД

3. **Использование AssertJ для проверок**:
   - Проверка HTTP статуса: `assertThat(response.getStatus()).isEqualTo(201)`
   - Проверка тела ответа: `assertThat(responseDto.getId()).isNotNull()`
   - Проверка БД: прямое обращение к `TweetRepository` или через SQL запросы

### Шаг 5: Обработка исключительных ситуаций

1. **Настройка WireMock для различных сценариев**:
   - Успешный ответ (200 OK) с `exists: true/false`
   - Ошибка 500 Internal Server Error
   - Таймаут (медленный ответ или отсутствие ответа)
   - Некорректный JSON ответ
   - Ошибка 404 Not Found

2. **Эмуляция ошибок БД**:
   - Использование специальных данных, вызывающих ошибки БД
   - Или временное отключение БД для проверки обработки ошибок подключения

### Шаг 6: Оптимизация и улучшения

1. **Использование `@Sql` для подготовки данных**:
   - Скрипты для создания тестовых пользователей
   - Скрипты для очистки данных после тестов

2. **Параметризованные тесты**:
   - Использование `@ParameterizedTest` для тестирования граничных значений длины контента

3. **Проверка логирования**:
   - Использование `OutputCaptureExtension` для проверки логов (опционально)

4. **Проверка метрик и трейсинга**:
   - Проверка корректности работы `@LoggableRequest` аспекта
   - Проверка создания traceId для запросов (если используется)

### Шаг 7: Документация и поддержка

1. **Добавление JavaDoc комментариев**:
   - Описание назначения каждого теста
   - Объяснение используемых стабов и моков

2. **Создание README для тестов**:
   - Инструкции по запуску интеграционных тестов
   - Описание необходимых зависимостей
   - Примеры запуска через Gradle

3. **Интеграция в CI/CD**:
   - Настройка запуска тестов в pipeline
   - Учет времени выполнения (Testcontainers может быть медленным)

### Примерная структура файлов

```
services/tweet-api/src/test/java/com/twitter/controller/
├── TweetControllerIntegrationTest.java
└── base/
    └── TweetControllerIntegrationTestBase.java

services/tweet-api/src/test/resources/
├── application-integration-test.yml
└── sql/
    ├── clean-tweets.sql
    └── setup-test-users.sql
```

### Ключевые моменты реализации

1. **Изоляция тестов**: Каждый тест должен быть независимым и не зависеть от порядка выполнения
2. **Производительность**: Использование `@Testcontainers` с переиспользованием контейнеров где возможно
3. **Читаемость**: Четкое разделение на Arrange-Act-Assert, использование понятных имен методов
4. **Полнота покрытия**: Все сценарии из списка должны быть реализованы
5. **Соответствие стандартам**: Следование JavaDoc стандартам проекта, использование существующих паттернов

