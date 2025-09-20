# Users API Service

## Введение

**Users API** — это микросервис для управления пользователями в системе Twitter, построенный на Java 24 и Spring Boot 3. Сервис предоставляет REST API для создания, чтения, обновления и деактивации пользователей с поддержкой ролевой модели доступа.

### Основные возможности:
- ✅ CRUD операции для пользователей
- ✅ Ролевая модель (USER, ADMIN, MODERATOR)
- ✅ Безопасное хеширование паролей
- ✅ Валидация данных
- ✅ Пагинация и фильтрация
- ✅ Логирование запросов
- ✅ Защита от удаления последнего администратора

## Архитектура

### Структура пакетов

```
com.twitter/
├── Application.java              # Главный класс приложения
├── controller/
│   └── UserController.java       # REST контроллер
├── dto/
│   ├── UserRequestDto.java       # DTO для создания пользователя
│   ├── UserResponseDto.java      # DTO для ответа
│   ├── UserUpdateDto.java        # DTO для обновления
│   ├── UserPatchDto.java         # DTO для частичного обновления
│   ├── UserRoleUpdateDto.java    # DTO для обновления роли
│   └── filter/
│       └── UserFilter.java       # Фильтр для поиска
├── entity/
│   └── User.java                 # JPA сущность
├── enums/
│   ├── UserRole.java             # Роли пользователей
│   └── UserStatus.java           # Статусы пользователей
├── mapper/
│   └── UserMapper.java           # MapStruct маппер
├── repository/
│   └── UserRepository.java       # JPA репозиторий
├── service/
│   ├── UserService.java          # Интерфейс сервиса
│   └── UserServiceImpl.java      # Реализация сервиса
└── util/
    └── PasswordUtil.java         # Утилиты для работы с паролями
```

### Диаграмма компонентов

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   UserController│────│   UserService   │────│  UserRepository │
│                 │    │                 │    │                 │
│ - REST Endpoints│    │ - Business Logic│    │ - Data Access   │
│ - Validation    │    │ - Validation    │    │ - Queries       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│      DTOs       │    │   UserMapper    │    │   PostgreSQL    │
│                 │    │                 │    │                 │
│ - Request/Response│   │ - Entity Mapping│   │ - Database      │
│ - Validation    │    │ - DTO Conversion│    │ - Tables        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## REST API

### Базовый URL
```
http://localhost:8080/api/v1/users
```

### Эндпоинты

| Метод | Путь | Описание | Параметры | Тело запроса | Ответ |
|-------|------|----------|-----------|--------------|-------|
| `GET` | `/{id}` | Получить пользователя по ID | `id` (UUID) | - | `UserResponseDto` |
| `GET` | `/` | Получить список пользователей | `UserFilter`, `Pageable` | - | `PagedModel<UserResponseDto>` |
| `POST` | `/` | Создать нового пользователя | - | `UserRequestDto` | `UserResponseDto` |
| `PUT` | `/{id}` | Полное обновление пользователя | `id` (UUID) | `UserUpdateDto` | `UserResponseDto` |
| `PATCH` | `/{id}` | Частичное обновление пользователя | `id` (UUID) | `JsonNode` | `UserResponseDto` |
| `PATCH` | `/{id}/inactivate` | Деактивировать пользователя | `id` (UUID) | - | `UserResponseDto` |
| `PATCH` | `/{id}/role` | Обновить роль пользователя | `id` (UUID) | `UserRoleUpdateDto` | `UserResponseDto` |

### Детальное описание эндпоинтов

#### 1. Получить пользователя по ID
```http
GET /api/v1/users/{id}
```

**Параметры:**
- `id` (UUID) - идентификатор пользователя

**Ответы:**
- `200 OK` - пользователь найден
- `404 Not Found` - пользователь не найден

**Пример ответа:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "login": "john_doe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "status": "ACTIVE",
  "role": "USER"
}
```

#### 2. Получить список пользователей
```http
GET /api/v1/users?firstNameContains=John&role=USER&page=0&size=10&sort=login,asc
```

**Параметры запроса:**
- `firstNameContains` (String, optional) - фильтр по имени
- `lastNameContains` (String, optional) - фильтр по фамилии
- `email` (String, optional) - фильтр по email
- `login` (String, optional) - фильтр по логину
- `role` (UserRole, optional) - фильтр по роли
- `page` (int, default: 0) - номер страницы
- `size` (int, default: 20) - размер страницы
- `sort` (String, optional) - сортировка

**Структура ответа PagedModel:**
- `content` - массив объектов `UserResponseDto`
- `page` - метаданные пагинации:
  - `size` - размер страницы
  - `number` - номер страницы (начиная с 0)
  - `totalElements` - общее количество элементов
  - `totalPages` - общее количество страниц

**Пример ответа:**
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "login": "john_doe",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "status": "ACTIVE",
      "role": "USER"
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

#### 3. Создать пользователя
```http
POST /api/v1/users
Content-Type: application/json
```

**Тело запроса:**
```json
{
  "login": "jane_smith",
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@example.com",
  "password": "securePassword123"
}
```

**Валидация:**
- `login` - обязательное, 3-50 символов
- `email` - обязательное, валидный email
- `password` - обязательное, минимум 8 символов

**Ответы:**
- `200 OK` - пользователь создан
- `400 Bad Request` - ошибка валидации
- `409 Conflict` - пользователь с таким логином/email уже существует

#### 4. Обновить пользователя (PUT)
```http
PUT /api/v1/users/{id}
Content-Type: application/json
```

**Тело запроса:**
```json
{
  "login": "jane_smith_updated",
  "firstName": "Jane",
  "lastName": "Smith-Wilson",
  "email": "jane.wilson@example.com",
  "password": "newSecurePassword123"
}
```

**Ответы:**
- `200 OK` - пользователь обновлен
- `404 Not Found` - пользователь не найден
- `400 Bad Request` - ошибка валидации
- `409 Conflict` - конфликт уникальности

#### 5. Частичное обновление пользователя (PATCH)
```http
PATCH /api/v1/users/{id}
Content-Type: application/json
```

**Тело запроса:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith-Wilson"
}
```

#### 6. Деактивировать пользователя
```http
PATCH /api/v1/users/{id}/inactivate
```

**Ответы:**
- `200 OK` - пользователь деактивирован
- `404 Not Found` - пользователь не найден
- `400 Bad Request` - попытка деактивировать последнего администратора

#### 7. Обновить роль пользователя
```http
PATCH /api/v1/users/{id}/role
Content-Type: application/json
```

**Тело запроса:**
```json
{
  "role": "ADMIN"
}
```

**Ответы:**
- `200 OK` - роль обновлена
- `404 Not Found` - пользователь не найден
- `400 Bad Request` - попытка изменить роль последнего администратора

## Бизнес-логика

### UserService

Основной сервис для работы с пользователями, реализующий следующие операции:

#### Методы сервиса:

1. **`getUserById(UUID id)`**
   - Получает пользователя по идентификатору
   - Возвращает `Optional<UserResponseDto>`
   - Логика: поиск в репозитории и маппинг в DTO

2. **`findAll(UserFilter userFilter, Pageable pageable)`**
   - Получает список пользователей с фильтрацией и пагинацией
   - Возвращает `Page<UserResponseDto>`
   - Логика: построение спецификации из фильтра и маппинг результатов

3. **`createUser(UserRequestDto userRequest)`**
   - Создает нового пользователя
   - Возвращает `UserResponseDto`
   - Логика:
     - Валидация уникальности логина и email
     - Маппинг DTO в сущность
     - Установка статуса ACTIVE и роли USER
     - Хеширование пароля
     - Сохранение в БД

4. **`updateUser(UUID id, UserUpdateDto userDetails)`**
   - Полное обновление пользователя
   - Возвращает `Optional<UserResponseDto>`
   - Логика:
     - Поиск пользователя по ID
     - Валидация уникальности (исключая текущего пользователя)
     - Обновление полей через маппер
     - Хеширование нового пароля (если указан)
     - Сохранение изменений

5. **`patchUser(UUID id, JsonNode patchNode)`**
   - Частичное обновление пользователя
   - Возвращает `Optional<UserResponseDto>`
   - Логика:
     - Поиск пользователя по ID
     - Применение JSON Patch к DTO
     - Валидация результата
     - Проверка уникальности
     - Обновление сущности

6. **`inactivateUser(UUID id)`**
   - Деактивация пользователя
   - Возвращает `Optional<UserResponseDto>`
   - Логика:
     - Проверка, что это не последний активный администратор
     - Установка статуса INACTIVE
     - Логирование операции

7. **`updateUserRole(UUID id, UserRoleUpdateDto roleUpdate)`**
   - Обновление роли пользователя
   - Возвращает `Optional<UserResponseDto>`
   - Логика:
     - Проверка, что нельзя изменить роль последнего администратора
     - Обновление роли
     - Логирование изменения

### Ключевые бизнес-правила:

1. **Уникальность данных:**
   - Логин должен быть уникальным
   - Email должен быть уникальным
   - При обновлении исключается текущий пользователь

2. **Защита администраторов:**
   - Нельзя деактивировать последнего активного администратора
   - Нельзя изменить роль последнего активного администратора

3. **Безопасность паролей:**
   - Пароли хешируются с использованием PBKDF2
   - Используется криптографически безопасная соль
   - Пароли никогда не возвращаются в ответах

4. **Валидация данных:**
   - Все входящие данные валидируются
   - Используется Jakarta Validation
   - Кастомная валидация для бизнес-правил

## Работа с базой данных

### Сущность User

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String login;
    
    private String firstName;
    private String lastName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Column(nullable = false)
    private String passwordSalt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
}
```

### Таблица users

| Поле | Тип | Ограничения | Описание |
|------|-----|-------------|----------|
| `id` | UUID | PRIMARY KEY, NOT NULL | Уникальный идентификатор |
| `login` | VARCHAR | UNIQUE, NOT NULL | Логин пользователя |
| `first_name` | VARCHAR | NULL | Имя |
| `last_name` | VARCHAR | NULL | Фамилия |
| `email` | VARCHAR | UNIQUE, NOT NULL | Email адрес |
| `password_hash` | VARCHAR | NOT NULL | Хеш пароля |
| `password_salt` | VARCHAR | NOT NULL | Соль для хеширования |
| `status` | VARCHAR | NOT NULL | Статус (ACTIVE/INACTIVE) |
| `role` | VARCHAR | NOT NULL | Роль (USER/ADMIN/MODERATOR) |

### UserRepository

Интерфейс репозитория расширяет `JpaRepository` и `JpaSpecificationExecutor`:

```java
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    long countByRoleAndStatus(UserRole role, UserStatus status);
    boolean existsByLogin(String login);
    boolean existsByEmail(String email);
    boolean existsByLoginAndIdNot(String login, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);
}
```

### Спецификации для фильтрации

Фильтрация реализована через Spring Data JPA Specifications:

```java
public record UserFilter(String firstNameContains, String lastNameContains, 
                        String email, String login, UserRole role) {
    public Specification<User> toSpecification() {
        return firstNameContainsSpec()
            .and(lastNameContainsSpec())
            .and(emailSpec())
            .and(loginSpec())
            .and(roleSpec());
    }
}
```

## Примеры использования

### Создание пользователя

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "login": "newuser",
    "firstName": "New",
    "lastName": "User",
    "email": "newuser@example.com",
    "password": "securePassword123"
  }'
```

### Получение пользователей с фильтрацией

```bash
curl "http://localhost:8080/api/v1/users?firstNameContains=John&role=USER&page=0&size=10"
```

### Обновление пользователя

```bash
curl -X PUT http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Content-Type: application/json" \
  -d '{
    "login": "updateduser",
    "firstName": "Updated",
    "lastName": "User",
    "email": "updated@example.com",
    "password": "newPassword123"
  }'
```

### Частичное обновление

```bash
curl -X PATCH http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "New Name"
  }'
```

### Деактивация пользователя

```bash
curl -X PATCH http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000/inactivate
```

### Обновление роли

```bash
curl -X PATCH http://localhost:8080/api/v1/users/123e4567-e89b-12d3-a456-426614174000/role \
  -H "Content-Type: application/json" \
  -d '{
    "role": "ADMIN"
  }'
```

## UML Диаграммы

### Диаграмма классов

```
┌─────────────────────────────────────────────────────────────┐
│                        UserController                       │
├─────────────────────────────────────────────────────────────┤
│ - userService: UserService                                  │
├─────────────────────────────────────────────────────────────┤
│ + getUserById(id: UUID): ResponseEntity<UserResponseDto>   │
│ + findAll(filter: UserFilter, pageable: Pageable):         │
│   PagedModel<UserResponseDto>                              │
│ + createUser(request: UserRequestDto): UserResponseDto     │
│ + updateUser(id: UUID, details: UserUpdateDto):            │
│   ResponseEntity<UserResponseDto>                          │
│ + patchUser(id: UUID, patch: JsonNode):                    │
│   ResponseEntity<UserResponseDto>                          │
│ + inactivateUser(id: UUID): ResponseEntity<UserResponseDto>│
│ + updateUserRole(id: UUID, role: UserRoleUpdateDto):       │
│   ResponseEntity<UserResponseDto>                          │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ uses
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                        UserService                          │
├─────────────────────────────────────────────────────────────┤
│ + getUserById(id: UUID): Optional<UserResponseDto>         │
│ + findAll(filter: UserFilter, pageable: Pageable):         │
│   Page<UserResponseDto>                                     │
│ + createUser(request: UserRequestDto): UserResponseDto     │
│ + updateUser(id: UUID, details: UserUpdateDto):            │
│   Optional<UserResponseDto>                                │
│ + patchUser(id: UUID, patch: JsonNode):                    │
│   Optional<UserResponseDto>                                │
│ + inactivateUser(id: UUID): Optional<UserResponseDto>      │
│ + updateUserRole(id: UUID, role: UserRoleUpdateDto):       │
│   Optional<UserResponseDto>                                │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ implements
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      UserServiceImpl                        │
├─────────────────────────────────────────────────────────────┤
│ - objectMapper: ObjectMapper                                │
│ - userMapper: UserMapper                                    │
│ - userRepository: UserRepository                            │
│ - validator: Validator                                      │
├─────────────────────────────────────────────────────────────┤
│ + getUserById(id: UUID): Optional<UserResponseDto>         │
│ + findAll(filter: UserFilter, pageable: Pageable):         │
│   Page<UserResponseDto>                                     │
│ + createUser(request: UserRequestDto): UserResponseDto     │
│ + updateUser(id: UUID, details: UserUpdateDto):            │
│   Optional<UserResponseDto>                                │
│ + patchUser(id: UUID, patch: JsonNode):                    │
│   Optional<UserResponseDto>                                │
│ + inactivateUser(id: UUID): Optional<UserResponseDto>      │
│ + updateUserRole(id: UUID, role: UserRoleUpdateDto):       │
│   Optional<UserResponseDto>                                │
│ - validateUserUniqueness(login: String, email: String,     │
│   excludeUserId: UUID): void                               │
│ - setPassword(user: User, password: String): void          │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ uses
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      UserRepository                         │
├─────────────────────────────────────────────────────────────┤
│ + countByRoleAndStatus(role: UserRole, status: UserStatus):│
│   long                                                      │
│ + existsByLogin(login: String): boolean                    │
│ + existsByEmail(email: String): boolean                    │
│ + existsByLoginAndIdNot(login: String, id: UUID): boolean  │
│ + existsByEmailAndIdNot(email: String, id: UUID): boolean  │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ extends
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    JpaRepository<User, UUID>                │
│                    JpaSpecificationExecutor<User>           │
└─────────────────────────────────────────────────────────────┘
```

## Конфигурация

### Зависимости

Основные зависимости проекта:

- **Spring Boot 3.x** - основной фреймворк
- **Spring Data JPA** - работа с базой данных
- **Spring Web** - REST API
- **Spring Validation** - валидация данных
- **MapStruct** - маппинг объектов
- **Lombok** - генерация кода
- **PostgreSQL** - база данных
- **Micrometer Tracing** - трейсинг

## Запуск и развертывание

### Локальный запуск

1. Убедитесь, что PostgreSQL запущен на порту 5432
2. Создайте базу данных `twitter`
3. Запустите приложение:

```bash
./gradlew bootRun
```

### Docker

```bash
docker build -t users-api .
docker run -p 8080:8080 users-api
```

### Мониторинг

Приложение предоставляет следующие эндпоинты мониторинга:

- `/actuator/health` - состояние здоровья
- `/actuator/info` - информация о приложении
- `/actuator/metrics` - метрики
- `/actuator/tracing` - трейсинг

## Безопасность

### Хеширование паролей

- Алгоритм: PBKDF2WithHmacSHA256
- Итерации: 10,000
- Длина ключа: 256 бит
- Размер соли: 16 байт

### Валидация

- Все входящие данные валидируются
- Используется Jakarta Validation
- Кастомная валидация для бизнес-правил

### Логирование

- Все запросы логируются через `@LoggableRequest`
- Пароли скрываются в логах
- Подробное логирование операций

## Тестирование

Проект включает:

- Unit тесты для всех компонентов
- Integration тесты с TestContainers
- Тесты валидации
- Тесты безопасности паролей

Запуск тестов:

```bash
./gradlew test
```