# Трейсинг логов в Spring Boot 3+

Этот документ описывает реализацию трейсинга логов в микросервисе users-api.

## Обзор

Реализован полноценный трейсинг с использованием:
- **Spring Cloud Sleuth** - для автоматического трейсинга HTTP запросов
- **OpenTelemetry** - современный стандарт трейсинга
- **Zipkin** - для визуализации трейсов
- **Micrometer** - для метрик и мониторинга
- **Logback** - для структурированного логирования

## Компоненты

### 1. TracingConfig
Конфигурация OpenTelemetry с экспортом в Zipkin:
- Настройка TracerProvider
- Интеграция с Micrometer
- Автоматическое создание span'ов

### 2. LoggingConfig
Конфигурация структурированного логирования:
- JSON формат для продакшена
- Консольный вывод для разработки
- Ротация файлов логов
- Интеграция с MDC

### 3. TracingFilter
Фильтр для автоматического добавления trace ID:
- Извлечение trace ID из заголовков
- Добавление в MDC для логирования
- Передача в ответе для других сервисов

### 4. TracingUtil
Утилитный класс для работы с трейсингом:
- Создание span'ов
- Добавление тегов и событий
- Выполнение операций в span'ах
- Работа с MDC

## Настройка

### 1. Запуск Zipkin
```bash
# Запуск Zipkin через Docker Compose
docker-compose -f compose/zipkin.yaml up -d

# Или напрямую
docker run -d -p 9411:9411 openzipkin/zipkin
```

### 2. Конфигурация application.yml
```yaml
spring:
  sleuth:
    zipkin:
      base-url: http://localhost:9411
    sampler:
      probability: 1.0  # 100% для разработки
  web:
    client:
      enabled: true
    servlet:
      enabled: true

management:
  tracing:
    sampling:
      probability: 1.0
    zipkin:
      endpoint: http://localhost:9411/api/v2/spans
```

## Использование

### 1. Автоматический трейсинг
Все HTTP запросы автоматически получают trace ID и span ID:

```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        // Trace ID автоматически добавляется в логи
        log.info("Getting user: {}", id);
        return userService.getUserById(id);
    }
}
```

### 2. Ручное создание span'ов
```java
@Service
public class UserService {
    
    @Autowired
    private TracingUtil tracingUtil;
    
    public User createUser(UserRequest request) {
        return tracingUtil.executeInSpan("create-user", Map.of(
            "user.email", request.getEmail(),
            "operation", "CREATE_USER"
        ), () -> {
            tracingUtil.addEvent("user-creation-started");
            
            User user = userRepository.save(convertToEntity(request));
            
            tracingUtil.addEvent("user-creation-completed");
            tracingUtil.addTag("user.id", user.getId().toString());
            
            return user;
        });
    }
}
```

### 3. Работа с MDC
```java
// Получение текущего trace ID
String traceId = tracingUtil.getCurrentTraceId();

// Добавление пользовательского контекста
tracingUtil.addUserContext("user123", "john_doe");

// Очистка контекста
tracingUtil.clearUserContext();
```

## Логирование

### Структура логов
```json
{
  "@timestamp": "2024-01-15T10:30:00.000Z",
  "level": "INFO",
  "logger": "com.twitter.controller.UserController",
  "message": "Getting user by ID: 123e4567-e89b-12d3-a456-426614174000",
  "thread": "http-nio-8080-exec-1",
  "mdc": {
    "traceId": "abc123def456",
    "spanId": "def456ghi789",
    "requestId": "req123456789",
    "userId": "user123"
  },
  "service": "users-api",
  "traceId": "abc123def456",
  "spanId": "def456ghi789",
  "parentSpanId": "parent123",
  "sampled": "true",
  "userId": "user123",
  "requestId": "req123456789"
}
```

### Уровни логирования
- **DEBUG** - детальная информация для разработки
- **INFO** - общая информация о работе приложения
- **WARN** - предупреждения
- **ERROR** - ошибки с полным контекстом

## Мониторинг

### 1. Zipkin UI
- URL: http://localhost:9411
- Просмотр трейсов в реальном времени
- Анализ производительности
- Поиск по trace ID

### 2. Actuator Endpoints
- `/actuator/health` - состояние приложения
- `/actuator/metrics` - метрики
- `/actuator/tracing` - информация о трейсинге

### 3. Логи
- Консольный вывод (разработка)
- Файловые логи с ротацией (продакшен)
- JSON формат для ELK Stack

## Лучшие практики

### 1. Именование span'ов
- Используйте описательные имена
- Включайте операцию и ресурс
- Примеры: `get-user-by-id`, `create-user`, `validate-email`

### 2. Теги
- Добавляйте релевантные теги
- Используйте стандартные ключи
- Примеры: `user.id`, `operation`, `response.status`

### 3. События
- Логируйте важные этапы
- Используйте консистентные имена
- Примеры: `user-lookup-started`, `validation-completed`

### 4. Обработка ошибок
```java
try {
    return tracingUtil.executeInSpan("operation", () -> {
        // выполнение операции
    });
} catch (Exception e) {
    tracingUtil.addTag("error", "true");
    tracingUtil.addTag("error.message", e.getMessage());
    throw e;
}
```

## Производительность

### 1. Sampling
- Разработка: 100% (probability: 1.0)
- Продакшен: 10% (probability: 0.1)

### 2. Асинхронная отправка
- Трейсы отправляются асинхронно
- Не блокируют основной поток
- Batch обработка для эффективности

### 3. Локальное хранение
- Используйте локальное хранилище для Zipkin
- В продакшене настройте внешнее хранилище

## Troubleshooting

### 1. Трейсы не отображаются в Zipkin
- Проверьте подключение к Zipkin
- Убедитесь, что sampling включен
- Проверьте логи на ошибки

### 2. Trace ID не передается между сервисами
- Убедитесь, что TracingFilter настроен
- Проверьте заголовки HTTP запросов
- Включите логирование на уровне DEBUG

### 3. Производительность
- Настройте sampling для продакшена
- Используйте асинхронную отправку
- Мониторьте использование памяти

