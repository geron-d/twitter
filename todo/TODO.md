# TODO: Интеграционные тесты для метода createUser

## Описание задачи
Написать интеграционные тестовые сценарии для метода `createUser` из `UserController.java` в классе `UserControllerTest.java`.

## Технические требования
- **Язык**: Java 24
- **Фреймворк**: Spring Boot 3
- **Тестирование**: JUnit 5, Spring Boot Test, Testcontainers
- **База данных**: PostgreSQL через Testcontainers
- **Эмуляция внешних зависимостей**: WireMock для HTTP-зависимостей
- **Принцип тестирования**: AAA (Arrange–Act–Assert)
- **Структура**: Все тесты для одного метода объединены в один `@Nested` класс

## Целевой метод
```java
@PostMapping
public UserResponseDto createUser(@RequestBody @Valid UserRequestDto userRequest) {
    return userService.createUser(userRequest);
}
```

## Список тестовых сценариев

### @Nested класс: CreateUserIntegrationTests

#### 🟢 УСПЕШНЫЕ СЦЕНАРИИ

1. **createUser_WithValidMinimalData_ShouldCreateUserWith200Ok**
   - Создание пользователя с минимально необходимыми полями (login, email, password)
   - Проверка: статус 200, корректные данные в ответе, статус ACTIVE, роль USER

2. **createUser_WithValidFullData_ShouldCreateUserWith200Ok**
   - Создание пользователя со всеми полями (login, firstName, lastName, email, password)
   - Проверка: статус 200, все поля корректно сохранены

#### 🔴 ИСКЛЮЧИТЕЛЬНЫЕ СЦЕНАРИИ

##### Ошибки валидации (400 Bad Request) - 8 тестов

1. **createUser_WithBlankLogin_ShouldReturn400BadRequest**
    - Создание пользователя с пустым login
    - Проверка: статус 400, сообщение об ошибке валидации

2. **createUser_WithTooShortLogin_ShouldReturn400BadRequest**
    - Создание пользователя с логином длиной менее 3 символов
    - Проверка: статус 400, сообщение об ошибке валидации

3. **createUser_WithTooLongLogin_ShouldReturn400BadRequest**
    - Создание пользователя с логином длиной более 50 символов
    - Проверка: статус 400, сообщение об ошибке валидации

4. **createUser_WithBlankEmail_ShouldReturn400BadRequest**
    - Создание пользователя с пустым email
    - Проверка: статус 400, сообщение об ошибке валидации

5. **createUser_WithInvalidEmailFormat_ShouldReturn400BadRequest**
    - Создание пользователя с некорректным форматом email (без @, без домена, etc.)
    - Проверка: статус 400, сообщение об ошибке валидации

6. **createUser_WithBlankPassword_ShouldReturn400BadRequest**
    - Создание пользователя с пустым паролем
    - Проверка: статус 400, сообщение об ошибке валидации

7. **createUser_WithTooShortPassword_ShouldReturn400BadRequest**
    - Создание пользователя с паролем длиной менее 8 символов
    - Проверка: статус 400, сообщение об ошибке валидации

8. **createUser_WithMultipleValidationErrors_ShouldReturn400BadRequest**
    - Создание пользователя с несколькими ошибками валидации одновременно
    - Проверка: статус 400, все ошибки валидации в ответе

##### Ошибки уникальности (409 Conflict) - 3 теста

1. **createUser_WithDuplicateLogin_ShouldReturn409Conflict**
    - Создание пользователя с уже существующим login
    - Проверка: статус 409, сообщение о конфликте

2. **createUser_WithDuplicateEmail_ShouldReturn409Conflict**
    - Создание пользователя с уже существующим email
    - Проверка: статус 409, сообщение о конфликте


## Структура реализации
- Все тесты будут добавлены в существующий класс `UserControllerTest.java`
- Создан новый `@Nested` класс `CreateUserIntegrationTests`
- Каждый тест следует принципу **AAA (Arrange-Act-Assert)**
- Используется существующая инфраструктура с Testcontainers и PostgreSQL
- Применяется MockMvc для HTTP-запросов

## Следующие шаги
1. Реализовать тесты для успешных сценариев
2. Реализовать тесты для исключительных ситуаций
3. Проверить покрытие кода и качество тестов
4. Запустить все тесты и убедиться в их прохождении
