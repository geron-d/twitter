# Twitter Common Library

## Введение

**Twitter Common Library** — это общая библиотека для микросервисной архитектуры проекта Twitter, построенная на Java 24 и Spring Boot 3. Библиотека предоставляет общие компоненты, которые могут быть использованы во всех микросервисах проекта для обеспечения единообразия, переиспользования кода и стандартизации обработки ошибок и логирования.

### Назначение библиотеки

- **Логирование HTTP запросов**: Автоматическое логирование входящих и исходящих HTTP запросов с возможностью скрытия чувствительных данных
- **Глобальная обработка исключений**: Централизованная обработка ошибок с возвратом стандартизированных ответов
- **Специализированные исключения**: Предопределенные исключения для бизнес-логики
- **Аспектно-ориентированное программирование**: Использование AOP для сквозной функциональности

## Архитектура

### Структура пакетов

```
com.twitter.common/
├── aspect/                    # Аспекты для AOP
│   ├── LoggableRequest.java      # Аннотация для логирования
│   └── LoggableRequestAspect.java # Аспект логирования
├── enums/                     # Перечисления
│   ├── UserRole.java             # Роли пользователей (ADMIN, MODERATOR, USER)
│   └── UserStatus.java           # Статусы пользователей (ACTIVE, INACTIVE)
├── exception/                 # Обработка исключений
│   ├── GlobalExceptionHandler.java      # Глобальный обработчик
│   └── validation/             # Исключения валидации
│       ├── ValidationException.java         # Базовое исключение валидации
│       ├── BusinessRuleValidationException.java # Бизнес-правила
│       ├── FormatValidationException.java      # Формат данных
│       ├── UniquenessValidationException.java  # Уникальность
│       └── ValidationType.java                 # Типы валидации
├── config/                    # Конфигурации (пустой)
└── util/                      # Утилиты (пустой)
```

### Диаграмма компонентов

```
┌─────────────────────────────────────────────────────────────┐
│                    Twitter Common Library                   │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │   Aspect Layer  │    │      Exception Layer            │ │
│  │                 │    │                                 │ │
│  │ @LoggableRequest│    │ GlobalExceptionHandler          │ │
│  │ LoggableRequest │    │ ValidationException             │ │
│  │ Aspect          │    │ BusinessRuleValidation         │ │
│  └─────────────────┘    │ FormatValidation               │ │
│           │              │ UniquenessValidation            │ │
│           │              └─────────────────────────────────┘ │
│           │                           │                     │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │   Domain Layer  │    │        Business Logic           │ │
│  │                 │    │                                 │ │
│  │ UserRole        │    │ Admin Management                │ │
│  │ UserStatus      │    │ Validation                      │ │
│  │ ValidationType  │    │ Error Handling                  │ │
│  └─────────────────┘    └─────────────────────────────────┘ │
│           │                           │                     │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │   HTTP Layer    │    │        Integration              │ │
│  │                 │    │                                 │ │
│  │ Request/Response│    │ Spring Boot Integration         │ │
│  │ Logging         │    │ AOP Integration                 │ │
│  │ Sensitive Data  │    │ Validation Integration          │ │
│  │ Hiding          │    │                                 │ │
│  └─────────────────┘    └─────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## API Документация

### Аннотация @LoggableRequest

**Назначение**: Аннотация для методов контроллеров, которая включает автоматическое логирование HTTP запросов и ответов.

**Параметры**:
- `printRequestBody()` (boolean, default: true) - включить/выключить логирование тела запроса
- `hideFields()` (String[], default: {}) - массив полей для скрытия в логах

**Пример использования**:
```java
@RestController
public class UserController {
    
    @PostMapping("/users")
    @LoggableRequest(hideFields = {"password", "secretKey"})
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto userDto) {
        // логика создания пользователя
    }
    
    @GetMapping("/users/{id}")
    @LoggableRequest(printRequestBody = false)
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        // логика получения пользователя
    }
}
```

### Глобальный обработчик исключений

**Класс**: `GlobalExceptionHandler`

**Обрабатываемые исключения**:

| Тип исключения | HTTP статус | Описание |
|----------------|-------------|----------|
| `ResponseStatusException` | Из исключения | Стандартные Spring исключения |
| `RuntimeException` | 500 | Неожиданные ошибки сервера |
| `ConstraintViolationException` | 400 | Ошибки валидации |
| `ValidationException` | 400 | Базовые ошибки валидации |
| `BusinessRuleValidationException` | 400 | Нарушение бизнес-правил |
| `FormatValidationException` | 400 | Ошибки формата данных |
| `UniquenessValidationException` | 409 | Нарушение уникальности |

**Формат ответа** (ProblemDetail):
```json
{
  "type": "https://example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed: ...",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Специализированные исключения

### Специализированные исключения

**Базовый класс**: `ValidationException`

**Иерархия исключений**:

```
ValidationException (abstract)
├── BusinessRuleValidationException
├── FormatValidationException
└── UniquenessValidationException
```

**BusinessRuleValidationException**:
- **Назначение**: Нарушение бизнес-правил системы
- **Примеры**: Попытка деактивации последнего админа, изменение роли последнего админа
- **Factory методы**: `lastAdminDeactivation()`, `lastAdminRoleChange()`

**FormatValidationException**:
- **Назначение**: Ошибки формата данных
- **Примеры**: Некорректный формат email, неверный формат даты
- **Factory методы**: `invalidFormat()`, `invalidEmailFormat()`

**UniquenessValidationException**:
- **Назначение**: Нарушение уникальности данных
- **Примеры**: Дублирование email, дублирование логина
- **Factory методы**: `duplicateEmail()`, `duplicateLogin()`

**ValidationType enum**:
- `BUSINESS_RULE` - Бизнес-правила
- `FORMAT` - Формат данных
- `UNIQUENESS` - Уникальность
- `CUSTOM` - Пользовательские правила

## Бизнес-логика

### Перечисления (Enums)

**UserRole** - Роли пользователей:
- `ADMIN` - Администратор системы
- `MODERATOR` - Модератор контента  
- `USER` - Обычный пользователь

**UserStatus** - Статусы пользователей:
- `ACTIVE` - Активный пользователь
- `INACTIVE` - Неактивный пользователь

**ValidationType** - Типы валидации:
- `BUSINESS_RULE` - Бизнес-правила
- `FORMAT` - Формат данных
- `UNIQUENESS` - Уникальность
- `CUSTOM` - Пользовательские правила

### Логирование запросов

**Компонент**: `LoggableRequestAspect`

**Функциональность**:
1. **Перехват методов**: Автоматически перехватывает методы, помеченные аннотацией `@LoggableRequest`
2. **Логирование запроса**: Записывает HTTP метод, URI, заголовки и тело запроса
3. **Скрытие чувствительных данных**: Маскирует указанные поля значением "***"
4. **Логирование ответа**: Записывает статус ответа и размер данных

**Алгоритм работы**:
```
1. Получить HTTP запрос из контекста
2. Извлечь параметры аннотации @LoggableRequest
3. Записать детали запроса (метод, URI, заголовки, тело)
4. При необходимости скрыть чувствительные поля
5. Выполнить оригинальный метод
6. Записать детали ответа (статус, размер данных)
7. Вернуть результат
```

### Обработка исключений

**Компонент**: `GlobalExceptionHandler`

**Стратегия обработки**:
1. **Стандартизация ответов**: Все ошибки возвращаются в формате RFC 7807 (ProblemDetail)
2. **Логирование**: Автоматическое логирование всех исключений
3. **Безопасность**: Скрытие внутренних деталей от клиента
4. **Трассировка**: Добавление временных меток для отладки

## Примеры использования

### 1. Базовое логирование контроллера

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PostMapping
    @LoggableRequest
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserRequestDto userDto) {
        User user = userService.createUser(userDto);
        return ResponseEntity.ok(userMapper.toResponseDto(user));
    }
    
    @GetMapping("/{id}")
    @LoggableRequest(printRequestBody = false)
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(userMapper.toResponseDto(user));
    }
}
```

### 2. Логирование с скрытием чувствительных данных

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/login")
    @LoggableRequest(hideFields = {"password", "token"})
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto loginDto) {
        // логика аутентификации
    }
    
    @PostMapping("/register")
    @LoggableRequest(hideFields = {"password", "confirmPassword"})
    public ResponseEntity<UserResponseDto> register(@RequestBody RegisterRequestDto registerDto) {
        // логика регистрации
    }
}
```

### 3. Обработка бизнес-исключений

```java
@Service
public class AdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    public void deactivateAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
        
        if (admin.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not an admin");
        }
        
        long activeAdminCount = userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
        
        if (activeAdminCount <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Cannot deactivate the last active administrator. System requires at least one active admin."
            );
        }
        
        admin.setStatus(UserStatus.INACTIVE);
        userRepository.save(admin);
    }
}
```

### 4. Валидация с обработкой ошибок

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PutMapping("/{id}")
    @LoggableRequest
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateDto updateDto) {
        
        try {
            User user = userService.updateUser(id, updateDto);
            return ResponseEntity.ok(userMapper.toResponseDto(user));
        } catch (ConstraintViolationException e) {
            // Автоматически обрабатывается GlobalExceptionHandler
            throw e;
        }
    }
}
```

## Сценарии использования

### Сценарий 1: Создание пользователя с логированием

**Описание**: Создание нового пользователя с полным логированием запроса и ответа.

**Шаги**:
1. Клиент отправляет POST запрос на `/api/users`
2. `LoggableRequestAspect` перехватывает запрос
3. Логируется метод, URI, заголовки и тело запроса
4. Выполняется бизнес-логика создания пользователя
5. Логируется статус ответа и размер данных
6. Возвращается ответ клиенту

**Лог**:
```
### REQUEST POST /api/users ,Headers: Content-Type: application/json; Accept: application/json , Body: {"username":"john_doe","email":"john@example.com","password":"***"}
### RESPONSE POST /api/users , status: 201
```

### Сценарий 2: Ошибка валидации

**Описание**: Обработка ошибки валидации с возвратом стандартизированного ответа.

**Шаги**:
1. Клиент отправляет некорректные данные
2. Spring Validation выбрасывает `ConstraintViolationException`
3. `GlobalExceptionHandler` перехватывает исключение
4. Создается `ProblemDetail` с деталями ошибки
5. Возвращается HTTP 400 с JSON ответом

**Ответ**:
```json
{
  "type": "https://example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed: email must be a valid email address",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Сценарий 3: Попытка деактивации последнего админа

**Описание**: Предотвращение деактивации последнего активного администратора.

**Шаги**:
1. Клиент запрашивает деактивацию администратора
2. Сервис проверяет количество активных админов
3. Если админ последний, выбрасывается `ResponseStatusException` с HTTP 409
4. `GlobalExceptionHandler` обрабатывает исключение
5. Возвращается HTTP 409 с описанием конфликта

**Ответ**:
```json
{
  "type": "https://example.com/errors/last-admin-deactivation",
  "title": "Last Admin Deactivation Error",
  "status": 409,
  "detail": "Cannot deactivate the last active administrator",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Зависимости

### Основные зависимости

| Зависимость | Версия | Назначение |
|-------------|--------|------------|
| `spring-boot-starter` | 3.x | Основной Spring Boot starter |
| `spring-boot-starter-aop` | 3.x | Аспектно-ориентированное программирование |
| `spring-boot-starter-validation` | 3.x | Валидация данных |
| `spring-boot-starter-web` | 3.x | Web приложения |
| `lombok` | 1.18.38 | Генерация кода |
| `mapstruct` | 1.6.3 | Маппинг объектов |

### Требования

- **Java**: 24
- **Spring Boot**: 3.x
- **Maven/Gradle**: Современные версии

## Конфигурация

### Настройка логирования

Для включения детального логирования ответов добавьте в `application.yml`:

```yaml
logging:
  level:
    com.twitter.common.aspect: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Настройка AOP

Убедитесь, что AOP включен в вашем приложении

## Тестирование

Библиотека включает комплексные unit-тесты для всех компонентов:

- **LoggableRequestAspectTest**: Тестирование аспекта логирования
- **SuccessfulScenarios**: Успешные сценарии
- **BoundaryScenarios**: Граничные случаи
- **ExceptionScenarios**: Обработка исключений
- **AdditionalScenarios**: Дополнительные тесты

### Запуск тестов

```bash
./gradlew test
```

## Рекомендации по использованию

### 1. Логирование

- Используйте `@LoggableRequest` на всех публичных методах контроллеров
- Указывайте `hideFields` для полей с чувствительными данными
- Отключайте `printRequestBody` для GET запросов без тела

### 2. Обработка исключений

- Используйте `ResponseStatusException` для стандартных HTTP ошибок
- Применяйте `BusinessRuleValidationException` для бизнес-логики админов
- Добавляйте кастомные исключения, расширяющие `ResponseStatusException`

### 3. Безопасность

- Всегда скрывайте пароли, токены и другие чувствительные данные
- Не логируйте полные тела запросов в production
- Используйте соответствующие уровни логирования
