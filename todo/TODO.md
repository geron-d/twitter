# TODO

## Meta
- project: Twitter Twitter microservice validation refactoring
- updated: 2025-01-27
- changelog: todo/CHANGELOG.md

## Tasks

### Analysis Phase

- [x] (P1) #1: Анализ текущей реализации TweetValidatorImpl — Изучение структуры и использования исключений  
  acceptance: "Понять текущую реализацию и используемые типы исключений"
  
- [x] (P1) #2: Анализ структуры исключений в UsersValidatorImpl — Сравнительный анализ паттернов валидации  
  acceptance: "Выявить паттерны использования специализированных исключений"
  
- [x] (P1) #3: Анализ GlobalExceptionHandler — Изучение обработки исключений  
  acceptance: "Понять как обрабатываются разные типы validation exceptions"

### Design Phase

- [ ] (P1) #4: Разработка стратегии рефакторинга — Определение типов исключений для разных валидаций  
  acceptance: "Определить какие ValidationException использовать для каждого типа ошибки"
  
  **Детали:**
  - `FormatValidationException` - для Bean Validation violations и пустого контента
  - `BusinessRuleValidationException` - для несуществующего пользователя
  - Удалить использование `ConstraintViolationException` и `IllegalArgumentException`

### Implementation Phase

- [ ] (P1) #5: Рефакторинг validateContent() — Замена ConstraintViolationException на FormatValidationException  
  acceptance: "validateContent() бросает FormatValidationException вместо ConstraintViolationException"
  
  **Изменения:**
  - Заменить `throw new ConstraintViolationException()` на `throw FormatValidationException.beanValidationError()`
  - Добавить детализацию по полям и constraints
  - Обновить импорты

- [ ] (P1) #6: Рефакторинг validateUserExists() — Замена IllegalArgumentException на BusinessRuleValidationException  
  acceptance: "validateUserExists() бросает BusinessRuleValidationException вместо IllegalArgumentException"
  
  **Изменения:**
  - Заменить `throw new IllegalArgumentException()` на `throw new BusinessRuleValidationException()`
  - Использовать корректные ruleName и context
  - Обновить документацию

- [ ] (P2) #7: Обновление интерфейса TweetValidator — Изменение сигнатур методов  
  acceptance: "TweetValidator.java отражает новые типы исключений в @throws"
  
  **Изменения:**
  - Обновить javadoc для методов
  - Изменить @throws ConstraintViolationException на соответствующие исключения
  - Заменить @throws RuntimeException на конкретные типы

### Testing Phase

- [ ] (P1) #8: Написание unit-тестов для TweetValidatorImpl — Проверка новых типов исключений  
  acceptance: "Все методы валидации покрыты тестами, проверяющими корректные типы исключений"
  
  **Сценарии:**
  - Bean validation violations → FormatValidationException
  - Пустой контент → FormatValidationException  
  - null userId → BusinessRuleValidationException
  - Несуществующий userId → BusinessRuleValidationException

- [ ] (P2) #9: Интеграционные тесты — Проверка обработки через GlobalExceptionHandler  
  acceptance: "Исключения корректно обрабатываются GlobalExceptionHandler и возвращают правильные HTTP статусы"
  
  **Сценарии:**
  - Проверить HTTP 400 для FormatValidationException
  - Проверить HTTP 409 для BusinessRuleValidationException
  - Проверить ProblemDetail структуру

### Documentation Phase

- [ ] (P3) #10: Обновление JavaDoc — Документация новых типов исключений  
  acceptance: "Все методы содержат актуальную документацию по типам исключений"
  
  **Обновить:**
  - TweetValidator.java
  - TweetValidatorImpl.java
  - Метод validateContent()
  - Метод validateUserExists()

## Assumptions

- GlobalExceptionHandler уже настроен и обрабатывает FormatValidationException и BusinessRuleValidationException
- Библиотека common-lib содержит все необходимые exception классы
- UserGateway.existsUser() корректно реализован и интегрирован
- Bean Validation аннотации на CreateTweetRequestDto валидны
- Текущая функциональность валидации не должна измениться, меняются только типы исключений

## Risks

- **Риск P1**: Изменение типов исключений может сломать существующие тесты
  - Митигация: Обновить все тесты после рефакторинга
  - Митигация: Использовать семантический поиск для поиска всех использований

- **Риск P2**: Несовместимость с текущей обработкой ошибок в TweetController
  - Митигация: Проверить, что @Valid на @RequestBody работает корректно с новой обработкой
  - Митигация: Проверить, что GlobalExceptionHandler корректно обрабатывает новые типы

- **Риск P3**: Непоследовательность в обработке ошибок между services
  - Митигация: Следовать паттернам из UserValidatorImpl
  - Митигация: Убедиться, что используются exception классы из common-lib

## Technical Details

### Mapping Validation Errors to Exceptions

| Validation Type | Current Exception | Target Exception | HTTP Status |
|----------------|-------------------|------------------|-------------|
| Bean Validation violations | ConstraintViolationException | FormatValidationException | 400 |
| Empty content check | ConstraintViolationException | FormatValidationException | 400 |
| Null userId | IllegalArgumentException | BusinessRuleValidationException | 409 |
| User not exists | IllegalArgumentException | BusinessRuleValidationException | 409 |

### Code Patterns

**Before (TweetValidatorImpl):**
```java
throw new ConstraintViolationException("Tweet creation validation failed", violations);
throw new IllegalArgumentException("User does not exist: " + userId);
```

**After (TweetValidatorImpl):**
```java
throw FormatValidationException.beanValidationError("content", "CONTENT_VALIDATION", 
    "Validation failed: " + violations.toString());
throw new BusinessRuleValidationException("USER_NOT_EXISTS", userId.toString());
```

## Notes

- Ссылка на архитектуру: todo/tweet/TWEET_API_ARCHITECTURE.md
- Ссылка на старый план: todo/tweet/TWEET_API_COMMON.md
- Ссылка на новый план: todo/tweet/TWEET_API_COMMON_2.md
- Параллельно выполняется задача: Реализация existsUser в users-api (todo/TODO_NEW.md)
- Проект использует Java 24
- Проект использует Spring Boot и Jakarta Validation

## Success Criteria

- ✅ TweetValidatorImpl использует только FormatValidationException и BusinessRuleValidationException
- ✅ TweetValidatorImpl не использует ConstraintViolationException и IllegalArgumentException
- ✅ Все тесты проходят
- ✅ API возвращает корректные HTTP статусы (400 для format, 409 для business rules)
- ✅ ProblemDetail структура соответствует пользовательским ожиданиям
- ✅ Код соответствует стандартам проекта (JavaDoc, error messages, logging)

## Next Steps

После завершения рефакторинга:
1. Провести code review
2. Обновить API документацию (Swagger/OpenAPI)
3. Обновить примеры использования в README
4. Добавить метрики для отслеживания типов validation errors

