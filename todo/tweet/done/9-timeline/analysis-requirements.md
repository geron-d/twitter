# Анализ требований: Эндпоинт получения ленты новостей

## Meta
- **Шаг**: #1
- **Дата**: 2025-01-27
- **Статус**: Завершено
- **Связанные задачи**: TODO.md #1

## 1. Входные данные

### 1.1 Path параметры
- **userId** (UUID, обязательный) - идентификатор пользователя, для которого запрашивается лента новостей
  - Валидация: формат UUID, не null
  - Пример: `123e4567-e89b-12d3-a456-426614174000`

### 1.2 Query параметры (пагинация)
- **page** (Integer, опциональный, по умолчанию 0) - номер страницы (начиная с 0)
  - Валидация: >= 0
  - Пример: `?page=0`
  
- **size** (Integer, опциональный, по умолчанию 20) - размер страницы
  - Валидация: 1-100 (максимум 100 элементов на страницу согласно стандартам)
  - Пример: `?size=20`
  
- **sort** (String, опциональный, по умолчанию "createdAt,DESC") - сортировка
  - Валидация: допустимые поля для сортировки
  - Пример: `?sort=createdAt,DESC`

### 1.3 HTTP метод и путь
- **Метод**: GET
- **Путь**: `/api/v1/tweets/timeline/{userId}`
- **Полный пример**: `GET /api/v1/tweets/timeline/123e4567-e89b-12d3-a456-426614174000?page=0&size=20`

## 2. Выходные данные

### 2.1 Успешный ответ (200 OK)
- **Тип**: `Page<TweetResponseDto>` или `PagedModel<TweetResponseDto>`
- **Структура**: Пагинированный список твитов с метаданными

**TweetResponseDto** (существующий DTO):
```json
{
  "id": "uuid",
  "userId": "uuid",
  "content": "string (1-280 символов)",
  "createdAt": "2025-01-27T10:30:00Z",
  "updatedAt": "2025-01-27T10:30:00Z",
  "isDeleted": false,
  "stats": {
    "likesCount": 0,
    "retweetsCount": 0,
    "repliesCount": 0
  }
}
```

**Метаданные пагинации**:
```json
{
  "content": [...],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 150,
    "totalPages": 8,
    "first": true,
    "last": false
  }
}
```

### 2.2 Пустая лента (200 OK)
- Если пользователь не имеет подписок или подписанные пользователи не имеют твитов
- Возвращается пустая страница с метаданными:
  - `totalElements: 0`
  - `totalPages: 0`
  - `content: []`

### 2.3 Ошибки
- **400 Bad Request**: Невалидный UUID, некорректные параметры пагинации
- **404 Not Found**: Пользователь не существует (опционально, может быть 400)
- **500 Internal Server Error**: Внутренняя ошибка сервера
- **503 Service Unavailable**: follower-api недоступен (если используется Circuit Breaker)

## 3. Бизнес-правила

### 3.1 Основные правила
1. **Лента новостей содержит твиты от пользователей, на которых подписан запрашивающий пользователь**
   - Получение списка подписок через follower-api
   - Получение твитов от всех подписанных пользователей
   - Исключение удаленных твитов (isDeleted = false)

2. **Сортировка по умолчанию**
   - По дате создания (createdAt) в порядке убывания (DESC)
   - Новые твиты первыми

3. **Пагинация**
   - Offset-based пагинация (page, size)
   - Максимальный размер страницы: 100 элементов
   - По умолчанию: page=0, size=20

4. **Валидация пользователя**
   - Пользователь должен существовать в системе (проверка через UserGateway)
   - Невалидный UUID возвращает 400 Bad Request

5. **Обработка пустых результатов**
   - Если пользователь не имеет подписок → пустая страница (200 OK)
   - Если подписанные пользователи не имеют твитов → пустая страница (200 OK)
   - Это не ошибка, а нормальное состояние

### 3.2 Правила фильтрации
- Исключаются удаленные твиты (isDeleted = false)
- Включаются только твиты от подписанных пользователей
- Не включаются собственные твиты пользователя (только от подписок)

### 3.3 Правила производительности
- Использование IN запроса для получения твитов по списку userIds
- Ограничение количества userIds в одном запросе (если нужно, батчинг)
- Кэширование списка подписок (опционально, для будущей оптимизации)

## 4. Зависимости от follower-api

### 4.1 Эндпоинт follower-api
- **Метод**: GET
- **Путь**: `/api/v1/follows/{userId}/following`
- **Порт**: 8084
- **Базовый URL**: `http://localhost:8084` (локально) или `http://follower-api:8084` (Docker)

### 4.2 Контракт API
**Запрос**:
- Path параметр: `userId` (UUID)

**Ответ**:
- Тип: `PagedModel<FollowingResponseDto>`
- Структура `FollowingResponseDto`:
  ```json
  {
    "id": "uuid",  // followingId - идентификатор пользователя, на которого подписан
    "login": "string",
    "createdAt": "2025-01-27T10:30:00Z"
  }
  ```

**Важно**: Поле `id` в `FollowingResponseDto` содержит `followingId` - идентификатор пользователя, на которого подписан запрашивающий пользователь.

### 4.3 Обработка ошибок follower-api
- **200 OK**: Успешный ответ, обрабатываем список подписок
- **404 Not Found**: Пользователь не найден → возвращаем 400 Bad Request в tweet-api
- **500/503**: Внутренняя ошибка follower-api → 
  - Вариант 1: Возвращаем пустую страницу (graceful degradation)
  - Вариант 2: Возвращаем 503 Service Unavailable (требует уточнения)
  - Рекомендация: Circuit Breaker с fallback на пустую страницу

### 4.4 Пагинация follower-api
- follower-api поддерживает пагинацию
- Для получения всех подписок может потребоваться несколько запросов
- **Важно**: Нужно получить все подписки (не только первую страницу) для построения полной ленты
- **Решение**: Получать все подписки с пагинацией или использовать параметр для получения всех (если доступен)

## 5. Затронутые стандарты

### 5.1 STANDART_CODE.md
- Использование Records для DTO (TweetResponseDto уже существует)
- Слоистая архитектура: Controller → Service → Repository
- Использование MapStruct для маппинга
- Bean Validation для валидации входных данных
- JavaDoc для всех публичных методов
- Использование @LoggableRequest на контроллере
- Gateway паттерн для интеграции с follower-api

### 5.2 STANDART_PROJECT.md
- Использование @LoggableRequest для логирования запросов
- Обработка ошибок через GlobalExceptionHandler
- Использование ValidationException иерархии
- Gateway паттерн для абстракции внешних сервисов

### 5.3 STANDART_JAVADOC.md
- JavaDoc для всех публичных методов с @author geron, @version 1.0
- @param, @return, @throws для всех методов
- Использование @see для ссылок на интерфейсы

### 5.4 STANDART_SWAGGER.md
- OpenAPI аннотации в TweetApi интерфейсе
- @Operation, @ApiResponses, @ExampleObject для документации
- @Schema аннотации на DTO

### 5.5 STANDART_TEST.md
- Unit тесты для Service и Validator
- Integration тесты для Controller
- Именование: `methodName_WhenCondition_ShouldExpectedResult`
- Использование MockMvc для integration тестов
- WireMock для мокирования follower-api

### 5.6 STANDART_DOCKER.md
- Зависимость tweet-api от follower-api с health check
- Переменные окружения для конфигурации
- Health conditions для зависимостей

## 6. Список всех эндпоинтов tweet-api

### 6.1 Существующие эндпоинты
1. `POST /api/v1/tweets` - Создание твита
2. `GET /api/v1/tweets/{tweetId}` - Получение твита по ID
3. `PUT /api/v1/tweets/{tweetId}` - Обновление твита
4. `DELETE /api/v1/tweets/{tweetId}` - Удаление твита
5. `GET /api/v1/tweets/user/{userId}` - Получение твитов пользователя (аналог для timeline)

### 6.2 Новый эндпоинт
6. `GET /api/v1/tweets/timeline/{userId}` - Получение ленты новостей пользователя

## 7. Проектирование интеграции с follower-api

### 7.1 Архитектура интеграции

**Слои интеграции**:
1. **Feign Client** (`FollowerApiClient`) - HTTP клиент для вызова follower-api
2. **Gateway** (`FollowerGateway`) - Абстракция над Feign клиентом с обработкой ошибок
3. **Service** (`TweetService`) - Использование Gateway для получения списка подписок

### 7.2 FollowerApiClient (Feign Client)

**Расположение**: `services/tweet-api/src/main/java/com/twitter/client/FollowerApiClient.java`

**Структура**:
```java
@FeignClient(
    name = "follower-api",
    url = "${app.follower-api.base-url:http://localhost:8084}",
    path = "/api/v1/follows"
)
public interface FollowerApiClient {
    @GetMapping("/{userId}/following")
    PagedModel<FollowingResponseDto> getFollowing(
        @PathVariable("userId") UUID userId,
        @SpringQueryMap Pageable pageable
    );
}
```

**Особенности**:
- Использование `@SpringQueryMap` для передачи Pageable параметров
- Возвращает `PagedModel<FollowingResponseDto>` (Spring HATEOAS)
- Обработка ошибок через Feign ErrorDecoder (опционально)

### 7.3 FollowerGateway

**Расположение**: `services/tweet-api/src/main/java/com/twitter/gateway/FollowerGateway.java`

**Структура**:
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class FollowerGateway {
    private final FollowerApiClient followerApiClient;
    
    /**
     * Получает список идентификаторов пользователей, на которых подписан указанный пользователь.
     * 
     * @param userId идентификатор пользователя
     * @return список идентификаторов подписок (followingId)
     */
    public List<UUID> getFollowingUserIds(UUID userId) {
        // Реализация с обработкой ошибок
        // Получение всех подписок с пагинацией
        // Извлечение только id (followingId) из FollowingResponseDto
    }
}
```

**Обработка ошибок**:
- При ошибке follower-api возвращать пустой список (graceful degradation)
- Логирование ошибок
- Обработка null значений

**Получение всех подписок**:
- Если follower-api поддерживает получение всех подписок за один запрос - использовать это
- Если нет - делать несколько запросов с пагинацией до получения всех подписок
- Ограничение на максимальное количество подписок (для производительности)

### 7.4 Интеграция в TweetService

**Метод getTimeline**:
```java
@Transactional(readOnly = true)
public Page<TweetResponseDto> getTimeline(UUID userId, Pageable pageable) {
    // 1. Валидация userId через TweetValidator
    // 2. Получение списка подписок через FollowerGateway
    // 3. Если список пуст - возврат пустой страницы
    // 4. Получение твитов через TweetRepository.findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc
    // 5. Маппинг в TweetResponseDto
    // 6. Возврат пагинированного результата
}
```

### 7.5 Конфигурация

**application.yml**:
```yaml
app:
  follower-api:
    base-url: http://localhost:8084
```

**application-docker.yml**:
```yaml
app:
  follower-api:
    base-url: http://follower-api:8084
```

**docker-compose.yml**:
- Добавить зависимость `tweet-api` от `follower-api`
- Добавить health check condition
- Добавить переменную окружения `FOLLOWER_API_URL`

### 7.6 Обработка больших списков подписок

**Проблема**: Если у пользователя много подписок (например, 1000+), IN запрос может быть неэффективным.

**Решения**:
1. Ограничение количества userIds в одном запросе (батчинг)
2. Использование нескольких запросов к БД с разбиением списка
3. Кэширование списка подписок (для будущей оптимизации)

**Рекомендация**: Начать с простого IN запроса, добавить батчинг при необходимости.

## 8. Выводы и рекомендации

### 8.1 Ключевые решения
1. Использовать существующий `TweetResponseDto` (не создавать новый)
2. Использовать Gateway паттерн для абстракции follower-api
3. При ошибке follower-api возвращать пустую страницу (graceful degradation)
4. Получать все подписки для построения полной ленты
5. Использовать IN запрос для получения твитов по списку userIds

### 8.2 Риски и митигация
- **Производительность**: Мониторинг времени ответа, оптимизация при необходимости
- **Недоступность follower-api**: Circuit Breaker с fallback на пустую страницу
- **Большие списки подписок**: Батчинг запросов к БД

### 8.3 Следующие шаги
1. Проектирование API и контрактов (шаг #2)
2. Реализация Feign клиента (шаг #3)
3. Реализация Gateway (шаг #4)
4. Реализация Repository метода (шаг #5)

## 9. Ссылки

- [TODO.md](TODO.md)
- [TWEET_API_ARCHITECTURE.md](../0-base/TWEET_API_ARCHITECTURE.md)
- [STANDART_CODE.md](../../../../standards/STANDART_CODE.md)
- [STANDART_PROJECT.md](../../../../standards/STANDART_PROJECT.md)
- [Follower API README](../../../../services/follower-api/README.md)
