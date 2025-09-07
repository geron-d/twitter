# Быстрый старт с трейсингом логов

## Запуск системы

### 1. Запуск Zipkin
```bash
# Запуск Zipkin через Docker Compose
docker-compose -f compose/zipkin.yaml up -d

# Проверка, что Zipkin запущен
curl http://localhost:9411/health
```

### 2. Запуск приложения
```bash
# Переход в директорию сервиса
cd services/users-api

# Запуск приложения
./gradlew bootRun
```

### 3. Проверка трейсинга

#### Отправка тестового запроса
```bash
# Создание пользователя
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "login": "testuser",
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "password": "password123"
  }'

# Получение пользователя
curl http://localhost:8080/api/v1/users/{id}

# Поиск пользователей
curl "http://localhost:8080/api/v1/users?page=0&size=10"
```

#### Просмотр трейсов в Zipkin
1. Откройте http://localhost:9411 в браузере
2. Нажмите "Run Query" для просмотра всех трейсов
3. Кликните на трейс для детального просмотра

#### Просмотр логов
```bash
# Логи в консоли (разработка)
tail -f logs/application.log

# Логи в JSON формате (продакшен)
tail -f logs/application.log | jq .
```

## Структура трейсинга

### Автоматические компоненты
- **TracingFilter** - добавляет trace ID в каждый HTTP запрос
- **Spring Cloud Sleuth** - автоматически создает span'ы для HTTP запросов
- **MDC** - передает контекст через все логи

### Ручные компоненты
- **TracingUtil** - утилиты для создания span'ов и работы с MDC
- **Логирование** - структурированные логи с trace ID

### Примеры использования

#### В контроллере
```java
@GetMapping("/{id}")
public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
    log.info("Getting user by ID: {}", id);
    
    return tracingUtil.executeInSpan("get-user-by-id", Map.of(
        "user.id", id.toString(),
        "operation", "GET_USER"
    ), () -> {
        tracingUtil.addEvent("user-lookup-started");
        
        ResponseEntity<UserResponseDto> result = userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
        
        tracingUtil.addEvent("user-lookup-completed");
        tracingUtil.addTag("response.status", String.valueOf(result.getStatusCode().value()));
        
        return result;
    });
}
```

#### В сервисе
```java
public UserResponseDto createUser(UserRequestDto userRequest) {
    log.info("Creating new user with email: {}", userRequest.email());
    return tracingUtil.executeInSpan("create-user", Map.of(
        "user.email", userRequest.email(),
        "user.login", userRequest.login(),
        "operation", "CREATE_USER"
    ), () -> {
        tracingUtil.addEvent("user-creation-started");
        
        User user = userMapper.toUser(userRequest);
        user.setStatus(UserStatus.ACTIVE);

        tracingUtil.addEvent("password-hashing-started");
        setPassword(user, userRequest.password());
        tracingUtil.addEvent("password-hashing-completed");

        tracingUtil.addEvent("database-save-started");
        User savedUser = userRepository.save(user);
        tracingUtil.addEvent("database-save-completed");
        tracingUtil.addTag("user.id", savedUser.getId().toString());
        
        return userMapper.toUserResponseDto(savedUser);
    });
}
```

## Мониторинг

### Endpoints
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Tracing**: http://localhost:8080/actuator/tracing

### Логи
- **Консоль**: структурированные логи с trace ID
- **Файлы**: `logs/application.log` с ротацией
- **Ошибки**: `logs/error.log` отдельно

### Zipkin
- **UI**: http://localhost:9411
- **API**: http://localhost:9411/api/v2/spans

## Настройка

### Изменение уровня логирования
```yaml
logging:
  level:
    com.twitter: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
```

### Изменение sampling
```yaml
spring:
  sleuth:
    sampler:
      probability: 0.1  # 10% для продакшена
```

### Изменение Zipkin endpoint
```yaml
spring:
  sleuth:
    zipkin:
      base-url: http://your-zipkin-server:9411
```

## Troubleshooting

### Проблема: Трейсы не отображаются в Zipkin
**Решение**: Проверьте, что Zipkin запущен и доступен по адресу http://localhost:9411

### Проблема: Trace ID не передается между запросами
**Решение**: Убедитесь, что TracingFilter настроен и Spring Cloud Sleuth включен

### Проблема: Логи не содержат trace ID
**Решение**: Проверьте настройки logback-spring.xml и убедитесь, что MDC настроен правильно

### Проблема: Производительность страдает
**Решение**: Уменьшите sampling до 0.1 (10%) для продакшена

