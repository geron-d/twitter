# Анализ и проектирование логики создания follow-отношений в Admin Script

## Дата создания
2025-01-27

## Цель
Добавить логику создания follow-отношений между пользователями в административный скрипт генерации пользователей и твитов.

---

## 1. Анализ требований

### 1.1 Входные данные

**Источник данных:**
- Список успешно созданных пользователей (`List<UUID> createdUsers`) из шага 1 (создание пользователей)
- Минимум 1 пользователь должен быть создан для создания follow-отношений

**Ограничения:**
- Если создано менее 2 пользователей, follow-отношения не создаются (недостаточно пользователей для создания отношений)
- Если создано ровно 1 пользователь, follow-отношения не создаются (нет других пользователей для создания отношений)

### 1.2 Выходные данные

**Результаты выполнения:**
- Список ID успешно созданных follow-отношений (`List<UUID> createdFollows`)
- Статистика: общее количество созданных follow-отношений (`int totalFollowsCreated`)
- Ошибки: список ошибок при создании follow-отношений (добавляются в `statistics.errors`)

**Структура ответа:**
- `GenerateUsersAndTweetsResponseDto.createdFollows` - новый список UUID
- `ScriptStatisticsDto.totalFollowsCreated` - новое поле int

### 1.3 Логика выбора центрального пользователя

**Правило:**
- Первый созданный пользователь (первый элемент в списке `createdUsers`) выбирается как "центральный" пользователь
- Центральный пользователь - это пользователь, который:
  - Фолловит половину остальных пользователей (является follower)
  - Фолловится половиной остальных пользователей (является following)

**Обоснование:**
- Простота реализации (первый элемент списка)
- Предсказуемость (всегда один и тот же пользователь при одинаковых входных данных)
- Соответствует assumption из TODO_1.md: "Первый созданный пользователь выбирается как 'центральный'"

### 1.4 Логика вычисления половины

**Правило:**
- Используется целочисленное деление (округление вниз)
- Формула: `halfCount = (createdUsers.size() - 1) / 2`
  - `-1` потому что центральный пользователь исключается из подсчёта
  - Деление на 2 с округлением вниз

**Примеры:**
- 1 пользователь: `halfCount = (1 - 1) / 2 = 0` → follow-отношения не создаются
- 2 пользователя: `halfCount = (2 - 1) / 2 = 0` → follow-отношения не создаются
- 3 пользователя: `halfCount = (3 - 1) / 2 = 1` → создаётся 1 follow-отношение в каждую сторону (всего 2)
- 4 пользователя: `halfCount = (4 - 1) / 2 = 1` → создаётся 1 follow-отношение в каждую сторону (всего 2)
- 5 пользователей: `halfCount = (5 - 1) / 2 = 2` → создаётся 2 follow-отношения в каждую сторону (всего 4)
- 10 пользователей: `halfCount = (10 - 1) / 2 = 4` → создаётся 4 follow-отношения в каждую сторону (всего 8)

**Визуализация для 5 пользователей:**
```
Пользователи: [U1, U2, U3, U4, U5]
Центральный: U1
halfCount = 2

U1 фолловит: U2, U3 (2 пользователя из 4)
U2, U3 фолловят: U1 (2 пользователя из 4 фолловят U1)

Создано follow-отношений:
- U1 → U2
- U1 → U3
- U2 → U1
- U3 → U1
Всего: 4 follow-отношения
```

### 1.5 Логика создания follow-отношений

**Шаг 1: Подготовка данных**
1. Проверка: если `createdUsers.size() < 2`, пропустить создание follow-отношений
2. Выбор центрального пользователя: `centralUser = createdUsers.get(0)`
3. Создание списка остальных пользователей: `otherUsers = createdUsers.subList(1, createdUsers.size())`
4. Вычисление половины: `halfCount = (createdUsers.size() - 1) / 2`
5. Проверка: если `halfCount == 0`, пропустить создание follow-отношений

**Шаг 2: Создание follow-отношений "центральный → остальные"**
1. Перемешивание списка остальных пользователей: `Collections.shuffle(otherUsers)`
2. Выбор первых `halfCount` пользователей: `usersToFollow = otherUsers.subList(0, halfCount)`
3. Для каждого пользователя из `usersToFollow`:
   - Создание `FollowRequestDto(followerId=centralUser, followingId=user)`
   - Вызов `followGateway.createFollow(request)`
   - При успехе: добавление ID follow-отношения в `createdFollows`
   - При ошибке: логирование ошибки, добавление в `errors`, продолжение выполнения

**Шаг 3: Создание follow-отношений "остальные → центральный"**
1. Перемешивание списка остальных пользователей: `Collections.shuffle(otherUsers)` (новое перемешивание)
2. Выбор первых `halfCount` пользователей: `usersToFollowBack = otherUsers.subList(0, halfCount)`
3. Для каждого пользователя из `usersToFollowBack`:
   - Создание `FollowRequestDto(followerId=user, followingId=centralUser)`
   - Вызов `followGateway.createFollow(request)`
   - При успехе: добавление ID follow-отношения в `createdFollows`
   - При ошибке: логирование ошибки, добавление в `errors`, продолжение выполнения

**Примечания:**
- Перемешивание выполняется дважды (для разнообразия выбора пользователей)
- Один и тот же пользователь может быть выбран в обоих направлениях (это допустимо)
- Ошибки при создании одного follow-отношения не останавливают создание остальных

### 1.6 Обработка ошибок

**Типы ошибок:**
1. **Ошибки валидации** (400 Bad Request):
   - Невалидные UUID
   - Попытка создать дублирующее follow-отношение
   - Обработка: логирование (ERROR), добавление в `errors`, продолжение выполнения

2. **Ошибки внешнего сервиса** (500 Internal Server Error, timeout):
   - follower-api недоступен
   - Таймаут запроса
   - Обработка: логирование (ERROR), добавление в `errors`, продолжение выполнения

3. **Критические ошибки** (не ожидаются):
   - Если все follow-отношения не удалось создать, это не критично - скрипт продолжает выполнение

**Стратегия обработки:**
- Graceful degradation: частичные ошибки не останавливают выполнение скрипта
- Логирование всех ошибок с уровнем ERROR
- Добавление всех ошибок в список `statistics.errors` с понятными сообщениями
- Формат сообщения об ошибке: `"Failed to create follow relationship {followerId} -> {followingId}: {errorMessage}"`

### 1.7 Позиция в общем потоке выполнения

**Текущий порядок шагов:**
1. Step 1: Создание пользователей
2. Step 2: Создание твитов
3. Step 3: Подсчёт пользователей с твитами
4. Step 4: Валидация
5. Step 5: Удаление твитов
6. Step 6: Сбор статистики

**Новый порядок шагов:**
1. Step 1: Создание пользователей
2. **Step 1.5: Создание follow-отношений** ← новый шаг
3. Step 2: Создание твитов
4. Step 3: Подсчёт пользователей с твитами
5. Step 4: Валидация
6. Step 5: Удаление твитов
7. Step 6: Сбор статистики

**Обоснование:**
- Follow-отношения создаются после создания всех пользователей (нужны ID пользователей)
- Follow-отношения создаются до создания твитов (соответствует assumption из TODO_1.md)
- Это логично, так как follow-отношения - это отношения между пользователями, а не между пользователями и твитами

---

## 2. Проектирование интеграции с follower-api

### 2.1 Структура Feign Client

**Класс:** `FollowApiClient`
**Пакет:** `com.twitter.client`
**Аннотации:** `@FeignClient`

**Конфигурация:**
```java
@FeignClient(
    name = "follower-api",
    url = "${app.follower-api.base-url:http://localhost:8084}",
    path = "/api/v1/follows"
)
```

**Метод:**
```java
@PostMapping
FollowResponseDto createFollow(@RequestBody @Valid FollowRequestDto request);
```

**Используемые DTO:**
- `FollowRequestDto` - из пакета `com.twitter.dto.request` (follower-api)
- `FollowResponseDto` - из пакета `com.twitter.dto.response` (follower-api)

**Примечание:**
- DTO используются напрямую из follower-api (соответствует assumption из TODO_1.md)
- Если это недопустимо, потребуется создание shared DTO в common-lib

### 2.2 Структура Gateway

**Класс:** `FollowGateway`
**Пакет:** `com.twitter.gateway`
**Аннотации:** `@Component`, `@RequiredArgsConstructor`, `@Slf4j`

**Зависимости:**
- `FollowApiClient followApiClient` (final, через конструктор)

**Метод:**
```java
public FollowResponseDto createFollow(FollowRequestDto request) {
    // Валидация входных параметров (null checks)
    // Вызов followApiClient.createFollow(request)
    // Обработка ошибок (try-catch)
    // Логирование (info для успеха, error для ошибок)
    // Пробрасывание исключений дальше для обработки в Service слое
}
```

**Обработка ошибок:**
- Валидация: проверка на null для request и его полей
- Логирование: INFO для успешных операций, ERROR для ошибок
- Исключения: пробрасывание RuntimeException с понятным сообщением

**Паттерн:**
- Следует паттерну Gateway, как в `UsersGateway` и `TweetsGateway`
- Абстракция над Feign Client
- Централизованная обработка ошибок и логирование

### 2.3 Конфигурация application.yml

**Новая настройка:**
```yaml
app:
  follower-api:
    base-url: http://localhost:8084
```

**Для Docker окружения (application-docker.yml):**
```yaml
app:
  follower-api:
    base-url: http://follower-api:8084
```

**Примечание:**
- Порт 8084 выбран по аналогии с другими сервисами (users-api: 8081, tweet-api: 8082, admin-script-api: 8083)
- URL настраивается через properties для гибкости конфигурации

---

## 3. Затронутые стандарты проекта

### 3.1 STANDART_CODE.md

**Требования:**
- ✅ Использование Records для DTO (уже используется в существующих DTO)
- ✅ Использование Lombok (@RequiredArgsConstructor, @Slf4j)
- ✅ Правильная структура пакетов (client, gateway)
- ✅ JavaDoc на английском языке
- ✅ Правильные naming conventions (PascalCase для классов, camelCase для методов)

**Применение:**
- `FollowApiClient` - интерфейс Feign Client, следует паттерну из `UsersApiClient`
- `FollowGateway` - компонент Gateway, следует паттерну из `UsersGateway`
- Все классы должны иметь JavaDoc с @author geron, @version 1.0

### 3.2 STANDART_PROJECT.md

**Требования:**
- ✅ Использование Gateway паттерна для интеграции с внешними сервисами
- ✅ Использование Feign Clients для HTTP-коммуникации
- ✅ Обработка ошибок через try-catch с логированием
- ✅ Валидация входных параметров

**Применение:**
- `FollowGateway` использует Gateway паттерн
- `FollowApiClient` использует Feign Client
- Обработка ошибок в Gateway с логированием и пробрасыванием исключений

### 3.3 STANDART_JAVADOC.md

**Требования:**
- ✅ Все public классы и методы должны иметь JavaDoc
- ✅ Использование @author geron, @version 1.0
- ✅ @param для всех параметров
- ✅ @return для возвращаемых значений
- ✅ @throws для исключений

**Применение:**
- `FollowApiClient` - JavaDoc для класса и метода
- `FollowGateway` - JavaDoc для класса и метода с полным описанием
- Обновление JavaDoc в `GenerateUsersAndTweetsServiceImpl` для нового шага
- Обновление JavaDoc в DTO для новых полей

### 3.4 STANDART_TEST.md

**Требования:**
- ✅ Unit тесты для всех новых классов
- ✅ Использование @ExtendWith(MockitoExtension.class)
- ✅ Использование AssertJ для assertions
- ✅ Паттерн AAA (Arrange-Act-Assert)
- ✅ Использование @Nested для группировки тестов

**Применение:**
- `FollowGatewayTest` - unit тесты для FollowGateway
- Обновление `GenerateUsersAndTweetsServiceImplTest` - тесты для нового шага
- Тесты должны покрывать: успешные сценарии, обработку ошибок, граничные случаи

### 3.5 STANDART_README.md

**Требования:**
- ✅ Обновление README.md с описанием новой функциональности
- ✅ Обновление раздела "Бизнес-логика" с описанием нового шага
- ✅ Обновление раздела "Интеграция" с информацией об интеграции с follower-api
- ✅ Обновление примеров использования с новыми полями в ответе

**Применение:**
- Добавление описания Step 1.5 в раздел "Бизнес-логика"
- Добавление информации о FollowApiClient и FollowGateway в раздел "Интеграция"
- Обновление примеров ответов с полем `createdFollows`

---

## 4. Детальное проектирование логики

### 4.1 Алгоритм создания follow-отношений

```java
// Псевдокод
List<UUID> createdFollows = new ArrayList<>();
int totalFollowsCreated = 0;

// Проверка: минимум 2 пользователя
if (createdUsers.size() < 2) {
    log.info("Skipping follow relationships creation: insufficient users (need at least 2, got {})", createdUsers.size());
    return; // Пропустить создание follow-отношений
}

// Выбор центрального пользователя
UUID centralUser = createdUsers.get(0);
List<UUID> otherUsers = new ArrayList<>(createdUsers.subList(1, createdUsers.size()));

// Вычисление половины
int halfCount = (createdUsers.size() - 1) / 2;

// Проверка: halfCount должен быть > 0
if (halfCount == 0) {
    log.info("Skipping follow relationships creation: halfCount is 0 (only 1-2 users)");
    return; // Пропустить создание follow-отношений
}

log.info("Step 1.5: Creating follow relationships. Central user: {}, Other users: {}, Half count: {}", 
    centralUser, otherUsers.size(), halfCount);

// Шаг 1: Центральный фолловит половину остальных
Collections.shuffle(otherUsers);
List<UUID> usersToFollow = otherUsers.subList(0, halfCount);

for (UUID userToFollow : usersToFollow) {
    try {
        FollowRequestDto request = FollowRequestDto.builder()
            .followerId(centralUser)
            .followingId(userToFollow)
            .build();
        
        FollowResponseDto response = followGateway.createFollow(request);
        createdFollows.add(response.id());
        totalFollowsCreated++;
        log.debug("Created follow relationship: {} -> {}", centralUser, userToFollow);
    } catch (Exception ex) {
        String errorMsg = String.format("Failed to create follow relationship %s -> %s: %s", 
            centralUser, userToFollow, ex.getMessage());
        log.error(errorMsg, ex);
        errors.add(errorMsg);
    }
}

// Шаг 2: Половина остальных фолловят центрального
Collections.shuffle(otherUsers); // Новое перемешивание
List<UUID> usersToFollowBack = otherUsers.subList(0, halfCount);

for (UUID userToFollowBack : usersToFollowBack) {
    try {
        FollowRequestDto request = FollowRequestDto.builder()
            .followerId(userToFollowBack)
            .followingId(centralUser)
            .build();
        
        FollowResponseDto response = followGateway.createFollow(request);
        createdFollows.add(response.id());
        totalFollowsCreated++;
        log.debug("Created follow relationship: {} -> {}", userToFollowBack, centralUser);
    } catch (Exception ex) {
        String errorMsg = String.format("Failed to create follow relationship %s -> %s: %s", 
            userToFollowBack, centralUser, ex.getMessage());
        log.error(errorMsg, ex);
        errors.add(errorMsg);
    }
}

log.info("Step 1.5 completed: {} follow relationships created successfully out of {} attempted", 
    totalFollowsCreated, halfCount * 2);
```

### 4.2 Обновление DTO

**GenerateUsersAndTweetsResponseDto:**
```java
public record GenerateUsersAndTweetsResponseDto(
    List<UUID> createdUsers,
    List<UUID> createdFollows,  // ← новое поле
    List<UUID> createdTweets,
    List<UUID> deletedTweets,
    ScriptStatisticsDto statistics
) {
    // @Schema аннотации обновлены
    // JavaDoc обновлен
}
```

**ScriptStatisticsDto:**
```java
public record ScriptStatisticsDto(
    Integer totalUsersCreated,
    Integer totalFollowsCreated,  // ← новое поле
    Integer totalTweetsCreated,
    Integer totalTweetsDeleted,
    Integer usersWithTweets,
    Integer usersWithoutTweets,
    Long executionTimeMs,
    List<String> errors
) {
    // @Schema аннотации обновлены
    // JavaDoc обновлен
}
```

### 4.3 Обновление Service Implementation

**GenerateUsersAndTweetsServiceImpl:**
- Добавление зависимости: `private final FollowGateway followGateway;`
- Добавление нового шага Step 1.5 после Step 1
- Обновление создания `ScriptStatisticsDto` с новым полем `totalFollowsCreated`
- Обновление создания `GenerateUsersAndTweetsResponseDto` с новым полем `createdFollows`

---

## 5. Примеры сценариев

### 5.1 Сценарий 1: 3 пользователя

**Входные данные:**
- `createdUsers = [U1, U2, U3]`

**Выполнение:**
- Центральный: U1
- Остальные: [U2, U3]
- halfCount = (3 - 1) / 2 = 1

**Создание follow-отношений:**
1. U1 → U2 (или U1 → U3, в зависимости от shuffle)
2. U2 → U1 (или U3 → U1, в зависимости от shuffle)

**Результат:**
- `createdFollows = [F1, F2]` (2 follow-отношения)
- `totalFollowsCreated = 2`

### 5.2 Сценарий 2: 5 пользователей

**Входные данные:**
- `createdUsers = [U1, U2, U3, U4, U5]`

**Выполнение:**
- Центральный: U1
- Остальные: [U2, U3, U4, U5]
- halfCount = (5 - 1) / 2 = 2

**Создание follow-отношений:**
1. U1 → U2, U1 → U3 (или другие 2, в зависимости от shuffle)
2. U2 → U1, U3 → U1 (или другие 2, в зависимости от shuffle)

**Результат:**
- `createdFollows = [F1, F2, F3, F4]` (4 follow-отношения)
- `totalFollowsCreated = 4`

### 5.3 Сценарий 3: 1 пользователь

**Входные данные:**
- `createdUsers = [U1]`

**Выполнение:**
- Проверка: `createdUsers.size() < 2` → true
- Пропуск создания follow-отношений

**Результат:**
- `createdFollows = []` (пустой список)
- `totalFollowsCreated = 0`

### 5.4 Сценарий 4: Ошибка при создании follow-отношения

**Входные данные:**
- `createdUsers = [U1, U2, U3, U4, U5]`

**Выполнение:**
- Центральный: U1
- halfCount = 2
- При создании U1 → U2: успех
- При создании U1 → U3: ошибка (например, follower-api недоступен)
- При создании U2 → U1: успех
- При создании U3 → U1: успех

**Результат:**
- `createdFollows = [F1, F2, F3]` (3 follow-отношения)
- `totalFollowsCreated = 3`
- `errors = ["Failed to create follow relationship U1 -> U3: Connection timeout"]`

---

## 6. Выводы и рекомендации

### 6.1 Выводы

1. **Логика создания follow-отношений:**
   - Простая и понятная логика выбора центрального пользователя (первый в списке)
   - Предсказуемое вычисление половины (целочисленное деление)
   - Graceful обработка ошибок (частичные ошибки не останавливают выполнение)

2. **Интеграция с follower-api:**
   - Использование существующих DTO из follower-api (FollowRequestDto, FollowResponseDto)
   - Следование паттерну Gateway для абстракции над Feign Client
   - Централизованная обработка ошибок и логирование

3. **Стандарты проекта:**
   - Все стандарты проекта соблюдены
   - Следование существующим паттернам (UsersGateway, TweetsGateway)
   - Полная JavaDoc документация для всех новых классов

### 6.2 Рекомендации

1. **Тестирование:**
   - Unit тесты для FollowGateway (успешные сценарии, обработка ошибок, null checks)
   - Обновление тестов для GenerateUsersAndTweetsServiceImpl (различные количества пользователей, обработка ошибок)

2. **Документация:**
   - Обновление README.md с описанием нового шага
   - Обновление примеров использования с новыми полями в ответе

3. **Мониторинг:**
   - Логирование всех операций создания follow-отношений
   - Отслеживание ошибок через statistics.errors

---

## 7. Следующие шаги

1. ✅ Шаг #1: Анализ требований и проектирование (выполнено)
2. ⏭️ Шаг #2: Проектирование интеграции с follower-api (следующий шаг)
3. ⏭️ Шаг #3: Создание FollowApiClient
4. ⏭️ Шаг #4: Создание FollowGateway
5. ⏭️ Шаг #5: Обновление GenerateUsersAndTweetsResponseDto
6. ⏭️ Шаг #6: Обновление ScriptStatisticsDto
7. ⏭️ Шаг #7: Обновление GenerateUsersAndTweetsServiceImpl
8. ⏭️ Шаг #8: Обновление application.yml

---

**Автор:** assistant  
**Дата:** 2025-01-27  
**Версия:** 1.0

