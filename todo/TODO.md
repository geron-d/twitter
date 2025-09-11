# TODO: Добавление эндпоинта для обновления роли пользователя

## Задача
В users-api добавить эндпоинт для обновления роли пользователя

## Анализ текущего кода

### Структура проекта:
- **UserController** - REST эндпоинты для работы с пользователями
- **UserService** - интерфейс с методами для бизнес-логики  
- **UserServiceImpl** - реализация сервиса с логикой обновления пользователей
- **User** - сущность с полем `role` типа `UserRole` (USER, ADMIN, MODERATOR)
- **UserUpdateDto** - DTO для обновления пользователя (НЕ содержит поле role)
- **UserPatchDto** - DTO для частичного обновления через PATCH

### Текущие возможности обновления роли:
- ✅ Через PATCH эндпоинт можно обновить роль (UserPatchDto содержит все поля)
- ❌ Через PUT эндпоинт НЕЛЬЗЯ обновить роль (UserUpdateDto не содержит поле role)

## План выполнения

### Шаг 1: Создать DTO для обновления роли
- [ ] Создать `UserRoleUpdateDto` с полем `role` типа `UserRole`
- [ ] Добавить валидацию для роли (@NotNull, @Valid)

### Шаг 2: Добавить метод в UserService
- [ ] Добавить метод `updateUserRole(UUID id, UserRoleUpdateDto roleUpdate)` в интерфейс `UserService`
- [ ] Добавить реализацию в `UserServiceImpl` с проверкой на последнего админа
- [ ] Добавить логирование изменений роли

### Шаг 3: Добавить эндпоинт в UserController
- [ ] Создать PATCH эндпоинт `/{id}/role` для обновления роли пользователя
- [ ] Добавить валидацию и обработку ошибок
- [ ] Добавить аннотацию @LoggableRequest

### Шаг 4: Добавить проверки безопасности
- [ ] Проверить, что нельзя удалить последнего активного админа
- [ ] Добавить проверку прав доступа (только админы могут менять роли)
- [ ] Добавить логирование попыток изменения роли

### Шаг 5: Обновить UserUpdateDto (опционально)
- [ ] Добавить поле `role` в `UserUpdateDto` для полноценного обновления через PUT
- [ ] Обновить маппер для обработки нового поля

## Рекомендации

**Рекомендация:** Лучше создать отдельный эндпоинт для обновления роли, так как это критически важная операция, которая требует специальных проверок безопасности.

## Файлы для изменения

1. `services/users-api/src/main/java/com/twitter/dto/UserRoleUpdateDto.java` (новый файл)
2. `services/users-api/src/main/java/com/twitter/service/UserService.java`
3. `services/users-api/src/main/java/com/twitter/service/UserServiceImpl.java`
4. `services/users-api/src/main/java/com/twitter/controller/UserController.java`
5. `services/users-api/src/main/java/com/twitter/dto/UserUpdateDto.java` (опционально)
6. `services/users-api/src/main/java/com/twitter/mapper/UserMapper.java` (опционально)

## Статус
- [ ] Шаг 1: Создание DTO
- [ ] Шаг 2: Добавление метода в сервис
- [ ] Шаг 3: Добавление эндпоинта
- [ ] Шаг 4: Проверки безопасности
- [ ] Шаг 5: Обновление UserUpdateDto (опционально)