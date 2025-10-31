# Тестовые сценарии для метода `validateForCreate` класса `TweetValidatorImpl`

Метод: `validateForCreate(CreateTweetRequestDto requestDto)` (строки 43-46)

## Описание метода

Метод выполняет полную валидацию для создания твита:
1. Вызывает `validateContent(requestDto)` для валидации контента через Bean Validation и дополнительные правила
2. Вызывает `validateUserExists(requestDto.getUserId())` для проверки существования пользователя через UserGateway

Зависимости:
- `Validator` (Jakarta Bean Validation) - для валидации DTO
- `UserGateway` - для проверки существования пользователя

## Структура тестов

Все тесты должны быть сгруппированы в один `@Nested` класс `ValidateForCreateTests` внутри основного тестового класса `TweetValidatorImplTest`.

## Тестовые сценарии

### Успешные сценарии

1. **Успешное выполнение при валидных данных**
   - DTO содержит валидный контент (1-280 символов, не пустой, не только пробелы)
   - userId не null и соответствует существующему пользователю
   - Ожидается: метод завершается без исключений

### Граничные значения

4. **Бросается FormatValidationException при content длиной 0 символов**
   - content пустая строка ""
   - userId валидный и существует
   - Ожидается: выбрасывается FormatValidationException с кодом "CONTENT_VALIDATION" или "EMPTY_CONTENT"

5. **Бросается FormatValidationException при content длиной 281 символ (превышение максимума)**
   - content содержит 281 символ
   - userId валидный и существует
   - Ожидается: выбрасывается FormatValidationException с кодом "CONTENT_VALIDATION"

6. **Бросается FormatValidationException при content состоящей только из пробелов**
   - content содержит только пробелы (например, "   ")
   - userId валидный и существует
   - Ожидается: выбрасывается FormatValidationException с кодом "EMPTY_CONTENT"

7. **Бросается FormatValidationException при content = null**
   - content равен null
   - userId валидный и существует
   - Ожидается: выбрасывается FormatValidationException с кодом "CONTENT_VALIDATION"

8. **Бросается BusinessRuleValidationException при userId = null**
   - content валидный
   - userId равен null
   - Ожидается: выбрасывается BusinessRuleValidationException с кодом "USER_ID_NULL"

9. **Бросается BusinessRuleValidationException при несуществующем userId**
   - content валидный
   - userId не null, но пользователь не существует (userGateway.existsUser возвращает false)
   - Ожидается: выбрасывается BusinessRuleValidationException с кодом "USER_NOT_EXISTS"

### Ошибочные сценарии и исключения

10. **Бросается FormatValidationException при нескольких нарушениях Bean Validation**
    - DTO содержит множественные нарушения (например, content = null и userId = null)
    - Ожидается: выбрасывается FormatValidationException с кодом "CONTENT_VALIDATION" и сообщением, содержащим все нарушения

11. **Бросается FormatValidationException при requestDto = null**
    - requestDto равен null
    - Ожидается: выбрасывается FormatValidationException или NullPointerException (в зависимости от реализации validateContent)

12. **Бросается FormatValidationException при нарушении Bean Validation аннотаций (@NotBlank, @Size)**
    - content нарушает аннотации (например, пустая строка при @NotBlank)
    - userId валидный
    - Ожидается: выбрасывается FormatValidationException с кодом "CONTENT_VALIDATION"

## Примечания по реализации

- Использовать `@ExtendWith(MockitoExtension.class)` для интеграции Mockito
- Мокировать зависимости: `Validator validator` и `UserGateway userGateway`
- Использовать `@InjectMocks` для создания экземпляра `TweetValidatorImpl`
- Создавать валидные и невалидные экземпляры `CreateTweetRequestDto` с помощью builder или конструктора
- Использовать AssertJ для assertions
- Проверять тип исключения, код ошибки и сообщение
- Использовать `verify()` для проверки взаимодействий с моками
- Структурировать тесты по формату AAA (Arrange - Act - Assert)

