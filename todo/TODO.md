# TODO: Запрет деактивации последнего администратора

## Задача
В users-api в методе `inactivateUser` в `UserController.java` необходимо запретить деактивировать последнего администратора.

## Анализ текущей архитектуры
- **UserRole**: USER, ADMIN, MODERATOR
- **UserStatus**: ACTIVE, INACTIVE
- **User**: entity с полями role и status
- **UserRepository**: базовый JpaRepository без кастомных методов
- **UserServiceImpl.inactivateUser()**: просто меняет статус на INACTIVE без проверок
- **GlobalExceptionHandler**: обрабатывает ResponseStatusException

## План выполнения

### Шаг 1: Добавить метод в UserRepository
- [ ] Создать метод `countByRoleAndStatus(UserRole role, UserStatus status)` в `UserRepository.java`
- [ ] Этот метод будет использоваться для подсчета активных администраторов

### Шаг 2: Создать кастомное исключение
- [ ] Создать `LastAdminDeactivationException` в `shared/common-lib/src/main/java/com/twitter/common/exception/`
- [ ] Наследовать от `ResponseStatusException` с HTTP 409 Conflict
- [ ] Добавить понятное сообщение об ошибке

### Шаг 3: Обновить GlobalExceptionHandler
- [ ] Добавить обработку `LastAdminDeactivationException` в `GlobalExceptionHandler.java`
- [ ] Обеспечить корректный HTTP ответ с описанием ошибки

### Шаг 4: Модифицировать UserServiceImpl
- [ ] Обновить метод `inactivateUser(UUID id)` в `UserServiceImpl.java`
- [ ] Добавить проверку: если пользователь ADMIN и это последний активный админ - выбросить исключение
- [ ] Проверку делать перед изменением статуса на INACTIVE
- [ ] Добавить логирование попытки деактивации последнего админа

### Шаг 5: Тестирование
- [ ] Протестировать деактивацию обычных пользователей (должно работать)
- [ ] Протестировать деактивацию админов, когда есть другие активные админы (должно работать)
- [ ] Протестировать попытку деактивации последнего админа (должна выбрасываться ошибка 409)

## Технические детали

### HTTP статус код
- **409 Conflict** - подходящий статус для бизнес-ограничений

### Логика проверки
```java
// Псевдокод
if (user.getRole() == UserRole.ADMIN) {
    long activeAdminCount = userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
    if (activeAdminCount <= 1) {
        throw new LastAdminDeactivationException("Cannot deactivate the last active administrator");
    }
}
```

### Файлы для изменения
1. `services/users-api/src/main/java/com/twitter/repository/UserRepository.java`
2. `shared/common-lib/src/main/java/com/twitter/common/exception/LastAdminDeactivationException.java` (новый)
3. `shared/common-lib/src/main/java/com/twitter/common/exception/GlobalExceptionHandler.java`
4. `services/users-api/src/main/java/com/twitter/service/UserServiceImpl.java`

## Ожидаемый результат
- Последний активный администратор не может быть деактивирован
- При попытке деактивации возвращается HTTP 409 с понятным сообщением
- Обычные пользователи и админы (когда есть другие активные админы) деактивируются как обычно
- Логируется попытка деактивации последнего админа
