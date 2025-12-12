# Анализ требований и проектирование API для admin-script-api

**Дата выполнения:** 2025-01-27  
**Шаг:** #1 - Анализ требований и проектирование API

## 1. Структура Request DTO

### GenerateUsersAndTweetsRequestDto

**Назначение:** DTO для запроса на выполнение административного скрипта генерации пользователей и твитов.

**Поля:**
- `nUsers` (Integer) - количество пользователей для создания
  - Валидация: `@NotNull`, `@Min(1)`, `@Max(1000)` (разумный лимит для массовых операций)
  - @Schema: description, example, minimum, maximum, requiredMode
  
- `nTweetsPerUser` (Integer) - количество твитов для каждого пользователя
  - Валидация: `@NotNull`, `@Min(1)`, `@Max(100)` (разумный лимит на пользователя)
  - @Schema: description, example, minimum, maximum, requiredMode
  
- `lUsersForDeletion` (Integer) - количество пользователей, у которых нужно удалить по 1 твиту
  - Валидация: `@NotNull`, `@Min(0)` (0 допустимо, если не нужно удалять)
  - Бизнес-валидация: l <= количество пользователей с твитами (проверка в Validator)
  - @Schema: description, example, minimum, requiredMode

**Структура:**
```java
@Schema(name = "GenerateUsersAndTweetsRequest", ...)
@Builder
public record GenerateUsersAndTweetsRequestDto(
    @Schema(...) @NotNull @Min(1) @Max(1000) Integer nUsers,
    @Schema(...) @NotNull @Min(1) @Max(100) Integer nTweetsPerUser,
    @Schema(...) @NotNull @Min(0) Integer lUsersForDeletion
) {}
```

## 2. Структура Response DTO

### GenerateUsersAndTweetsResponseDto

**Назначение:** DTO для ответа с результатами выполнения административного скрипта.

**Поля:**
- `createdUsers` (List<UUID>) - список ID созданных пользователей
  - @Schema: description, example
  
- `createdTweets` (List<UUID>) - список ID созданных твитов
  - @Schema: description, example
  
- `deletedTweets` (List<UUID>) - список ID удаленных твитов
  - @Schema: description, example
  
- `statistics` (ScriptStatisticsDto) - статистика выполнения скрипта
  - Вложенный record для группировки статистики
  - @Schema: description

**Вложенный ScriptStatisticsDto:**
- `totalUsersCreated` (Integer) - общее количество созданных пользователей
- `totalTweetsCreated` (Integer) - общее количество созданных твитов
- `totalTweetsDeleted` (Integer) - общее количество удаленных твитов
- `usersWithTweets` (Integer) - количество пользователей, у которых есть твиты
- `usersWithoutTweets` (Integer) - количество пользователей без твитов (для информации)
- `executionTimeMs` (Long) - время выполнения скрипта в миллисекундах
- `errors` (List<String>) - список ошибок, если были (опционально)

**Структура:**
```java
@Schema(name = "GenerateUsersAndTweetsResponse", ...)
@Builder
public record GenerateUsersAndTweetsResponseDto(
    @Schema(...) List<UUID> createdUsers,
    @Schema(...) List<UUID> createdTweets,
    @Schema(...) List<UUID> deletedTweets,
    @Schema(...) ScriptStatisticsDto statistics
) {}

@Schema(name = "ScriptStatistics", ...)
public record ScriptStatisticsDto(
    @Schema(...) Integer totalUsersCreated,
    @Schema(...) Integer totalTweetsCreated,
    @Schema(...) Integer totalTweetsDeleted,
    @Schema(...) Integer usersWithTweets,
    @Schema(...) Integer usersWithoutTweets,
    @Schema(...) Long executionTimeMs,
    @Schema(...) List<String> errors
) {}
```

## 3. Параметры валидации

### Bean Validation (на уровне DTO)
- `nUsers`: @NotNull, @Min(1), @Max(1000)
- `nTweetsPerUser`: @NotNull, @Min(1), @Max(100)
- `lUsersForDeletion`: @NotNull, @Min(0)

### Бизнес-валидация (в GenerateUsersAndTweetsValidator)
- `lUsersForDeletion <= количество пользователей с твитами` - проверка после создания твитов
- Если l > количество пользователей с твитами, выбрасывать ValidationException с понятным сообщением

### Сообщения об ошибках валидации
- Использовать стандартные сообщения из Jakarta Validation
- Для бизнес-валидации: "Cannot delete tweets from {l} users: only {actualCount} users have tweets"

## 4. Стратегия генерации данных с использованием Datafaker

### RandomDataGenerator - методы генерации

**Зависимость:** `net.datafaker:datafaker:2.1.0`

**Методы:**

1. **generateLogin()** -> String
   - Использовать: `faker.internet().username()`
   - Обеспечить уникальность: добавить `_` + `System.currentTimeMillis()` + `_` + `UUID.randomUUID().toString().substring(0, 8)`
   - Пример: `john_doe_1738001234567_a1b2c3d4`
   - Длина: до 100 символов (ограничение из UserRequestDto: 3-50, но с timestamp может быть больше)

2. **generateEmail()** -> String
   - Использовать: `faker.internet().emailAddress()`
   - Обеспечить уникальность: заменить `@` на `_` + `System.currentTimeMillis()` + `@`
   - Пример: `john.doe_1738001234567@example.com`
   - Валидация: должен соответствовать формату email

3. **generateFirstName()** -> String
   - Использовать: `faker.name().firstName()`
   - Может быть null (опциональное поле в UserRequestDto)

4. **generateLastName()** -> String
   - Использовать: `faker.name().lastName()`
   - Может быть null (опциональное поле в UserRequestDto)

5. **generatePassword()** -> String
   - Использовать: `faker.internet().password(8, 20, true, true, true)`
   - Параметры: minLength=8, maxLength=20, includeDigit=true, includeLower=true, includeUpper=true
   - Минимум 8 символов (требование из UserRequestDto)

6. **generateTweetContent()** -> String
   - Использовать: `faker.lorem().sentence()` или `faker.lorem().paragraph()`
   - Обрезать до 280 символов (максимальная длина твита)
   - Если длина > 280: `content.substring(0, 277) + "..."`
   - Минимум 1 символ (требование из CreateTweetRequestDto)

### Реализация RandomDataGenerator

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class RandomDataGenerator {
    private final Faker faker = new Faker();
    
    public String generateLogin() {
        String baseLogin = faker.internet().username();
        String uniqueSuffix = "_" + System.currentTimeMillis() + "_" + 
            UUID.randomUUID().toString().substring(0, 8);
        return baseLogin + uniqueSuffix;
    }
    
    public String generateEmail() {
        String baseEmail = faker.internet().emailAddress();
        String uniquePart = "_" + System.currentTimeMillis();
        return baseEmail.replace("@", uniquePart + "@");
    }
    
    // ... остальные методы
}
```

## 5. Структура ответа со статистикой

### ScriptStatisticsDto - детальная статистика

**Поля:**
- `totalUsersCreated` - фактическое количество созданных пользователей (может быть меньше nUsers при ошибках)
- `totalTweetsCreated` - фактическое количество созданных твитов (может быть меньше nUsers * nTweetsPerUser при ошибках)
- `totalTweetsDeleted` - фактическое количество удаленных твитов (может быть меньше lUsersForDeletion)
- `usersWithTweets` - количество пользователей, у которых есть твиты (для валидации lUsersForDeletion)
- `usersWithoutTweets` - количество пользователей без твитов (информационное поле)
- `executionTimeMs` - время выполнения скрипта в миллисекундах
- `errors` - список ошибок (если были частичные ошибки, но скрипт выполнился)

### Логика формирования статистики

1. **Создание пользователей:**
   - Создавать пользователей последовательно
   - При ошибке создания пользователя: логировать ошибку, добавлять в `errors`, продолжать со следующим
   - Учитывать только успешно созданных пользователей в `totalUsersCreated`

2. **Создание твитов:**
   - Для каждого успешно созданного пользователя создавать nTweetsPerUser твитов
   - При ошибке создания твита: логировать ошибку, добавлять в `errors`, продолжать
   - Учитывать только успешно созданные твиты в `totalTweetsCreated`
   - Подсчитывать `usersWithTweets` и `usersWithoutTweets`

3. **Удаление твитов:**
   - Выбрать l случайных пользователей из списка пользователей с твитами
   - Для каждого пользователя: получить список твитов, выбрать случайный твит, удалить
   - При ошибке удаления: логировать ошибку, добавлять в `errors`, продолжать
   - Учитывать только успешно удаленные твиты в `totalTweetsDeleted`

4. **Время выполнения:**
   - Засечь время в начале выполнения скрипта
   - Засечь время в конце выполнения скрипта
   - Вычислить разницу в миллисекундах

## 6. Интеграции с другими сервисами

### UsersApiClient

**Методы:**
- `createUser(UserRequestDto userRequest) -> UserResponseDto`
  - POST /api/v1/users
  - Использовать для создания пользователей с рандомными данными

**Структура UserRequestDto (из users-api):**
- login (String, 3-50 символов, обязательное)
- firstName (String, опциональное)
- lastName (String, опциональное)
- email (String, email формат, обязательное)
- password (String, минимум 8 символов, обязательное)

### TweetsApiClient

**Методы:**
- `createTweet(CreateTweetRequestDto tweetRequest) -> TweetResponseDto`
  - POST /api/v1/tweets
  - Использовать для создания твитов с рандомным контентом
  
- `deleteTweet(UUID tweetId) -> void`
  - DELETE /api/v1/tweets/{tweetId}
  - Использовать для удаления твитов
  
- `getUserTweets(UUID userId) -> List<TweetResponseDto>`
  - GET /api/v1/tweets/user/{userId}
  - Использовать для получения списка твитов пользователя перед удалением

**Структура CreateTweetRequestDto (из tweet-api):**
- content (String, 1-280 символов, обязательное)
- userId (UUID, обязательное)

## 7. Обработка ошибок

### Стратегия обработки ошибок

1. **Частичные ошибки:**
   - При ошибке создания одного пользователя: логировать, добавлять в `errors`, продолжать
   - При ошибке создания одного твита: логировать, добавлять в `errors`, продолжать
   - При ошибке удаления одного твита: логировать, добавлять в `errors`, продолжать
   - Возвращать частичный результат со статистикой

2. **Критические ошибки:**
   - При ошибке валидации параметров: выбрасывать ValidationException (400 Bad Request)
   - При недоступности внешних сервисов: выбрасывать ServiceUnavailableException (503 Service Unavailable)
   - При внутренних ошибках: выбрасывать InternalServerException (500 Internal Server Error)

3. **Логирование:**
   - Использовать @LoggableRequest на контроллере
   - Логировать все ошибки с уровнем ERROR
   - Логировать прогресс выполнения скрипта с уровнем INFO

## 8. REST Endpoint

### POST /api/v1/admin-scripts/generate-users-and-tweets

**Контроллер:** AdminScriptController  
**OpenAPI интерфейс:** AdminScriptApi

**Request:**
- Content-Type: application/json
- Body: GenerateUsersAndTweetsRequestDto

**Response:**
- 200 OK: GenerateUsersAndTweetsResponseDto (успешное выполнение, даже с частичными ошибками)
- 400 Bad Request: ValidationException (ошибки валидации параметров)
- 503 Service Unavailable: ServiceUnavailableException (недоступность внешних сервисов)
- 500 Internal Server Error: InternalServerException (внутренние ошибки)

**OpenAPI документация:**
- @Tag(name = "Admin Scripts", description = "Administrative scripts for system management")
- @Operation(summary = "Generate users and tweets", description = "...")
- @ApiResponses для всех возможных статус-кодов

## 9. Предположения и ограничения

### Предположения
1. Скрипт выполняется синхронно (может занять время при больших значениях n)
2. Уникальность login и email обеспечивается через timestamp + UUID
3. Если у пользователя нет твитов, он пропускается при удалении
4. Сервис не требует аутентификации (как и другие сервисы)
5. Порт для admin-script-api: 8083

### Ограничения
1. Максимальное количество пользователей за один запрос: 1000
2. Максимальное количество твитов на пользователя: 100
3. Время выполнения может быть долгим при больших значениях n
4. Нет поддержки отмены выполнения скрипта (требуется дождаться завершения)

## 10. Следующие шаги

После завершения анализа:
1. Настройка Gradle модуля (#2)
2. Реализация DTO (#3)
3. Реализация Feign Clients (#4)
4. Реализация Gateways (#5)
5. Реализация RandomDataGenerator (#6)
6. Реализация Validator (#7)
7. Реализация Service (#8)
8. Реализация Controller (#9)

## Заключение

Анализ требований и проектирование API завершено. Определены:
- ✅ Структура Request/Response DTO
- ✅ Параметры валидации (Bean Validation + бизнес-валидация)
- ✅ Стратегия генерации данных с Datafaker
- ✅ Структура ответа со статистикой
- ✅ Интеграции с другими сервисами
- ✅ Обработка ошибок
- ✅ REST Endpoint спецификация

Готово к реализации следующих шагов.

