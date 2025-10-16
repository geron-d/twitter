# Анализ архитектуры users-api для проектирования tweet-api

## Meta
- project: twitter-tweet-api
- analysis_date: 2025-01-27
- analyst: AI Assistant
- version: 1.0
- status: completed
- source: users-api service analysis

## Executive Summary

Данный документ содержит детальный анализ архитектуры существующего сервиса users-api для обеспечения консистентности при проектировании tweet-api. Анализ охватывает все слои приложения, паттерны проектирования, конфигурацию и общие компоненты.

## 1. Анализ структуры пакетов

### 1.1 Общая структура пакетов

```
com.twitter/
├── Application.java                 # Главный класс приложения
├── config/
│   └── OpenApiConfig.java         # Конфигурация OpenAPI/Swagger
├── controller/
│   ├── UserApi.java               # OpenAPI интерфейс
│   └── UserController.java        # REST контроллер
├── dto/
│   ├── UserRequestDto.java        # DTO для создания пользователя
│   ├── UserResponseDto.java       # DTO для ответа
│   ├── UserUpdateDto.java         # DTO для обновления
│   ├── UserPatchDto.java          # DTO для PATCH операций
│   ├── UserRoleUpdateDto.java     # DTO для обновления роли
│   ├── error/                     # DTO для ошибок
│   └── filter/                    # DTO для фильтрации
├── entity/
│   └── User.java                  # JPA сущность
├── mapper/
│   └── UserMapper.java            # MapStruct маппер
├── repository/
│   └── UserRepository.java        # Spring Data JPA репозиторий
├── service/
│   ├── UserService.java           # Интерфейс сервиса
│   └── UserServiceImpl.java       # Реализация сервиса
├── util/
│   ├── PasswordUtil.java          # Утилиты для паролей
│   └── PatchDtoFactory.java       # Фабрика для PATCH операций
└── validation/
    ├── UserValidator.java         # Интерфейс валидатора
    └── UserValidatorImpl.java     # Реализация валидатора
```

### 1.2 Принципы организации пакетов

- **Слоистая архитектура**: четкое разделение на controller, service, repository, dto, entity
- **Инверсия зависимостей**: интерфейсы в отдельных файлах от реализаций
- **Разделение ответственности**: каждый пакет имеет четко определенную роль
- **Консистентность**: единообразное именование и структура

## 2. Анализ слоев приложения

### 2.1 Controller Layer

#### Паттерны и принципы:
- **RESTful API**: использование стандартных HTTP методов
- **OpenAPI интеграция**: аннотации @Schema для документации
- **Валидация**: @Valid для автоматической валидации DTO
- **Логирование**: @LoggableRequest для автоматического логирования
- **Обработка ошибок**: возврат ResponseEntity с соответствующими статусами

#### Ключевые особенности:
```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApi {
    
    @LoggableRequest
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") UUID id) {
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

#### Паттерны обработки ошибок:
- Использование Optional для обработки отсутствующих данных
- Возврат ResponseEntity.notFound() для 404 ошибок
- Делегирование бизнес-логики в сервисный слой

### 2.2 Service Layer

#### Паттерны и принципы:
- **Интерфейс-реализация**: четкое разделение контракта и реализации
- **Транзакционность**: использование @Transactional (неявно через Spring Data)
- **Валидация**: двухуровневая валидация (DTO + бизнес-правила)
- **Маппинг**: использование MapStruct для преобразования данных
- **Обработка ошибок**: кастомные исключения для разных типов ошибок

#### Ключевые особенности:
```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    
    @Override
    public UserResponseDto createUser(UserRequestDto userRequest) {
        userValidator.validateForCreate(userRequest);
        User user = userMapper.toUser(userRequest);
        // Бизнес-логика
        User savedUser = userRepository.saveAndFlush(user);
        return userMapper.toUserResponseDto(savedUser);
    }
}
```

#### Паттерны валидации:
- **Двухуровневая валидация**: DTO валидация + бизнес-правила
- **Кастомные валидаторы**: UserValidator для сложной бизнес-логики
- **Исключения**: ValidationException, BusinessRuleValidationException

### 2.3 Repository Layer

#### Паттерны и принципы:
- **Spring Data JPA**: использование интерфейсов с автоматической реализацией
- **JpaSpecificationExecutor**: для динамических запросов
- **Кастомные методы**: existsBy*, countBy* для проверок
- **Пагинация**: встроенная поддержка через Pageable

#### Ключевые особенности:
```java
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    
    long countByRoleAndStatus(UserRole role, UserStatus status);
    boolean existsByLogin(String login);
    boolean existsByEmail(String email);
    boolean existsByLoginAndIdNot(String login, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);
}
```

#### Паттерны запросов:
- **Query Methods**: автоматическая генерация запросов по именам методов
- **Specifications**: для сложных динамических запросов
- **Exists проверки**: для валидации уникальности

### 2.4 Entity Layer

#### Паттерны и принципы:
- **JPA аннотации**: @Entity, @Table, @Id, @Column
- **Lombok**: @Data, @NoArgsConstructor, @AllArgsConstructor
- **UUID**: использование UUID для первичных ключей
- **Аудит**: @CreationTimestamp для автоматических временных меток
- **Enums**: @Enumerated(EnumType.STRING) для перечислений

#### Ключевые особенности:
```java
@Entity
@Table(name = "users")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

#### Паттерны безопасности:
- **Неизменяемые поля**: updatable = false для id и createdAt
- **Обязательные поля**: nullable = false для критических полей
- **Уникальность**: unique = true для логина и email

### 2.5 DTO/Mapper Layer

#### Паттерны и принципы:
- **Record DTOs**: использование Java records для неизменяемых DTO
- **MapStruct**: автоматическая генерация мапперов
- **Валидация**: Bean Validation аннотации в DTO
- **OpenAPI**: @Schema аннотации для документации
- **Безопасность**: исключение чувствительных полей из Response DTO

#### Ключевые особенности:
```java
@Schema(name = "UserRequest", description = "Data structure for creating new users")
public record UserRequestDto(
    @NotBlank(message = "Login cannot be blank")
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    String login,
    
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    String email
) {}
```

#### Паттерны маппинга:
```java
@Mapper
public interface UserMapper {
    
    @Mapping(target = "passwordHash", ignore = true)
    User toUser(UserRequestDto userRequestDto);
    
    UserResponseDto toUserResponseDto(User user);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    void updateUserFromUpdateDto(UserUpdateDto userUpdateDto, @MappingTarget User user);
}
```

## 3. Анализ конфигурации

### 3.1 Spring Boot конфигурация

#### application.yml структура:
```yaml
server:
  port: 8081

spring:
  application:
    name: users-api
  datasource:
    url: jdbc:postgresql://localhost:5432/twitter
    username: user
    password: password
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: none
  data:
    web:
      pageable:
        default-page-size: 10
        max-page-size: 100
```

#### Ключевые настройки:
- **Порт**: 8081 для users-api
- **База данных**: PostgreSQL с явным указанием драйвера
- **JPA**: отключение автоматического создания схемы (ddl-auto: none)
- **Пагинация**: настройка размеров страниц

### 3.2 OpenAPI/Swagger конфигурация

#### Структура конфигурации:
```java
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI usersApiOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Twitter Users API")
                .description("REST API for user management...")
                .version("1.0.0"))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8081")
                    .description("Local development server")
            ));
    }
}
```

#### Ключевые особенности:
- **Детальное описание**: comprehensive API documentation
- **Серверы**: конфигурация для разных окружений
- **Версионирование**: четкое указание версии API

### 3.3 Мониторинг и логирование

#### Actuator конфигурация:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,tracing
  endpoint:
    health:
      show-details: always
  tracing:
    sampling:
      probability: 1.0
```

#### Логирование:
```yaml
logging:
  level:
    com.twitter: DEBUG
    org.springframework.web: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} ${spring.application.name:} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n"
```

## 4. Анализ shared/common-lib компонентов

### 4.1 LoggableRequestAspect

#### Функциональность:
- **Автоматическое логирование**: HTTP запросов и ответов
- **Скрытие чувствительных данных**: hideFields параметр
- **AOP интеграция**: @Around advice для методов с @LoggableRequest
- **Структурированное логирование**: детальная информация о запросах

#### Ключевые особенности:
```java
@Around("@annotation(com.twitter.common.aspect.LoggableRequest))")
public Object log(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
        .currentRequestAttributes()).getRequest();
    logRequestDetails(request, proceedingJoinPoint);
    Object value = proceedingJoinPoint.proceed();
    logResponseDetails(request, value);
    return value;
}
```

#### Паттерны использования:
- **Аннотация**: @LoggableRequest на методах контроллера
- **Конфигурация**: hideFields = {"password"} для скрытия чувствительных данных
- **Контекст**: использование RequestContextHolder для получения HTTP контекста

### 4.2 Система исключений

#### Иерархия исключений:
```
ValidationException (базовое)
├── BusinessRuleValidationException
├── FormatValidationException
└── UniquenessValidationException
```

#### Паттерны обработки:
- **Типизированные исключения**: разные типы для разных ошибок
- **ValidationType enum**: классификация типов валидации
- **GlobalExceptionHandler**: централизованная обработка исключений

### 4.3 Enums

#### Структура:
```java
public enum UserRole {
    USER, ADMIN, MODERATOR
}

public enum UserStatus {
    ACTIVE, INACTIVE
}
```

#### Паттерны использования:
- **@Enumerated(EnumType.STRING)**: сохранение в БД как строки
- **Валидация**: использование в бизнес-правилах
- **API**: экспозиция через DTO

## 5. Паттерны валидации

### 5.1 Bean Validation

#### Аннотации в DTO:
```java
@NotBlank(message = "Login cannot be blank")
@Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
String login;

@NotBlank(message = "Email cannot be blank")
@Email(message = "Invalid email format")
String email;
```

#### Паттерны:
- **Сообщения об ошибках**: кастомные сообщения для каждой аннотации
- **Комбинирование**: использование нескольких аннотаций на одном поле
- **Группы валидации**: возможность группировки валидаций

### 5.2 Бизнес-валидация

#### UserValidator паттерны:
```java
public interface UserValidator {
    void validateForCreate(UserRequestDto userRequest);
    void validateForUpdate(UUID id, UserUpdateDto userDetails);
    void validateForPatch(UUID id, JsonNode patchNode);
    void validateAdminDeactivation(UUID id);
    void validateRoleChange(UUID id, UserRole newRole);
}
```

#### Ключевые особенности:
- **Контекстная валидация**: разные методы для разных операций
- **Бизнес-правила**: проверка администраторов, уникальности
- **Исключения**: выбрасывание типизированных исключений

## 6. Паттерны обработки ошибок

### 6.1 HTTP статус коды

#### Используемые статусы:
- **200 OK**: успешное получение данных
- **201 Created**: успешное создание (неявно через ResponseEntity)
- **404 Not Found**: ресурс не найден
- **400 Bad Request**: ошибки валидации
- **500 Internal Server Error**: внутренние ошибки

#### Паттерны возврата:
```java
return userService.getUserById(id)
    .map(ResponseEntity::ok)
    .orElse(ResponseEntity.notFound().build());
```

### 6.2 Исключения

#### Типы исключений:
- **ValidationException**: общие ошибки валидации
- **BusinessRuleValidationException**: нарушение бизнес-правил
- **UniquenessValidationException**: нарушение уникальности
- **FormatValidationException**: ошибки формата данных

## 7. Рекомендации для tweet-api

### 7.1 Архитектурные принципы

#### Следовать существующим паттернам:
1. **Слоистая архитектура**: controller → service → repository → entity
2. **Интерфейс-реализация**: четкое разделение контрактов и реализаций
3. **DTO паттерн**: использование records для DTO
4. **MapStruct маппинг**: автоматическая генерация мапперов
5. **Валидация**: двухуровневая (DTO + бизнес-правила)

### 7.2 Конфигурация

#### Настройки для tweet-api:
- **Порт**: 8082 (следующий доступный)
- **База данных**: та же PostgreSQL с отдельной схемой
- **OpenAPI**: аналогичная конфигурация с описанием tweet API
- **Мониторинг**: те же actuator endpoints

### 7.3 Интеграция с shared/common-lib

#### Использовать существующие компоненты:
- **LoggableRequestAspect**: для логирования HTTP запросов
- **Система исключений**: ValidationException и подклассы
- **Enums**: создать TweetStatus, TweetType если необходимо
- **GlobalExceptionHandler**: расширить для tweet-specific ошибок

### 7.4 Специфичные для tweet-api решения

#### Дополнительные компоненты:
- **TweetValidator**: для бизнес-правил твитов
- **ContentSanitizer**: для очистки контента от XSS
- **RateLimiter**: для ограничения создания твитов
- **CacheService**: для кэширования популярных твитов

## 8. Заключение

### 8.1 Ключевые выводы

1. **Консистентность**: users-api демонстрирует зрелые архитектурные паттерны
2. **Масштабируемость**: архитектура готова к расширению
3. **Безопасность**: продуманная система валидации и обработки ошибок
4. **Мониторинг**: комплексная система логирования и метрик

### 8.2 Готовность к реализации

Архитектура users-api предоставляет отличную основу для проектирования tweet-api. Все необходимые паттерны, компоненты и конфигурации уже протестированы и готовы к использованию.

### 8.3 Следующие шаги

1. **Адаптация паттернов**: применение изученных паттернов к tweet-api
2. **Расширение shared/common-lib**: добавление tweet-specific компонентов
3. **Интеграция**: проектирование взаимодействия между сервисами
4. **Тестирование**: использование существующих подходов к тестированию

---

*Документ создан: 2025-01-27*  
*Версия: 1.0*  
*Статус: Completed*
