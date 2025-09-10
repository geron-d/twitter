# TODO: Создание UserUpdateDto для обновления пользователей

## Цель
Создать кастомное DTO для обновления пользователя в users-api и обновить все связанные компоненты.

## Анализ текущего состояния
- `UserController.updateUser()` использует `UserRequestDto` для обновления пользователя
- `UserRequestDto` содержит все поля включая обязательные (`login`, `email`, `password`)
- `UserPatchDto` уже существует для частичного обновления (PATCH операций)
- `UserMapper` имеет методы для работы с `UserRequestDto` и `UserPatchDto`

## Проблема
- `UserRequestDto` предназначен для создания пользователей и содержит обязательные поля
- Для обновления пользователя нужен отдельный DTO, который не требует обязательных полей
- Текущий `updateUser` метод использует `UserRequestDto`, что неоптимально

## План выполнения

### Шаг 1: Создание UserUpdateDto
- Создать новый DTO `UserUpdateDto` для операций обновления
- Поля должны быть опциональными (все nullable)
- Добавить валидацию только для заполненных полей
- Исключить поля, которые не должны обновляться (id, status, role)

**Структура UserUpdateDto:**
```java
public record UserUpdateDto(
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    String login,
    
    String firstName,
    
    String lastName,
    
    @Email(message = "Invalid email format")
    String email,
    
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password
) {}
```

### Шаг 2: Обновление UserMapper
- Добавить методы для работы с `UserUpdateDto`
- `toUserUpdateDto(User user)` - для преобразования User в UserUpdateDto
- `updateUserFromUpdateDto(UserUpdateDto dto, @MappingTarget User user)` - для обновления User из UserUpdateDto

### Шаг 3: Обновление UserService
- Изменить сигнатуру метода `updateUser` для использования `UserUpdateDto`
- Обновить реализацию в `UserServiceImpl`

### Шаг 4: Обновление UserController
- Изменить метод `updateUser` для использования `UserUpdateDto`
- Обновить импорты

### Шаг 5: Проверка совместимости
- Убедиться, что все изменения корректно интегрированы
- Проверить, что валидация работает правильно

## Файлы для изменения
1. `services/users-api/src/main/java/com/twitter/dto/UserUpdateDto.java` (новый)
2. `services/users-api/src/main/java/com/twitter/mapper/UserMapper.java`
3. `services/users-api/src/main/java/com/twitter/service/UserService.java`
4. `services/users-api/src/main/java/com/twitter/service/UserServiceImpl.java`
5. `services/users-api/src/main/java/com/twitter/controller/UserController.java`

## Ключевые особенности UserUpdateDto
- Все поля nullable (опциональные)
- Валидация применяется только к заполненным полям
- Исключены системные поля (id, status, role, passwordHash, passwordSalt)
- Совместимость с существующей логикой обновления пароля

## Статус
- [ ] Шаг 1: Создание UserUpdateDto
- [ ] Шаг 2: Обновление UserMapper
- [ ] Шаг 3: Обновление UserService
- [ ] Шаг 4: Обновление UserController
- [ ] Шаг 5: Проверка совместимости
