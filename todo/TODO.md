# TODO

## Meta
- project: twitter-users-api-validation-layer
- updated: 2025-01-21
- changelog: todo/CHANGELOG.md

## Tasks

### Done
- [x] (P1) #1: Анализ текущего состояния валидации в UserServiceImpl — Изучение существующих валидаций и их распределения по методам.  
  acceptance: "Понять все типы валидаций: уникальность, бизнес-правила, Bean Validation"

### In Progress

### Done
- [x] (P1) [2025-01-21 14:30] #2: Проектирование архитектуры слоя валидации — Создание интерфейсов и классов для централизованной валидации.  
  acceptance: "Создать UserValidator интерфейс и его реализацию с четким разделением ответственности"
  note: "Спроектирована упрощенная архитектура с единым UserValidator (объединенный с PatchValidator) и иерархией исключений"

### To Do

#### Анализ и проектирование
- [ ] (P1) #3: Определение типов валидаций — Категоризация всех валидаций по типам (уникальность, бизнес-правила, формат).  
  acceptance: "Список всех валидаций с их типами и контекстом использования"

- [ ] (P1) #4: Создание интерфейса UserValidator — Определение контракта для валидации пользователей.  
  acceptance: "Интерфейс с методами для каждого типа валидации"

- [ ] (P1) #5: Проектирование исключений валидации — Создание специализированных исключений для разных типов ошибок.  
  acceptance: "Иерархия исключений: ValidationException -> UniquenessValidationException, BusinessRuleValidationException"

#### Реализация
- [ ] (P1) #6: Создание UserValidatorImpl — Реализация основного класса валидации.  
  acceptance: "Класс с методами validateForCreate, validateForUpdate, validateForPatch, validateBusinessRules"

- [ ] (P1) #7: Вынос валидации уникальности — Перенос логики validateUserUniqueness в отдельный метод.  
  acceptance: "Метод validateUniqueness в UserValidatorImpl"

- [ ] (P1) #8: Вынос бизнес-валидаций — Перенос проверок последнего админа в валидатор.  
  acceptance: "Методы validateAdminDeactivation, validateRoleChange в UserValidatorImpl"

- [ ] (P2) #9: Создание валидатора для JSON патчей — Интеграция PATCH валидации в UserValidator.  
  acceptance: "Методы validateForPatch, validatePatchData, validatePatchConstraints в UserValidator"

- [ ] (P2) #10: Интеграция с существующими DTO — Обеспечение совместимости с текущими аннотациями валидации.  
  acceptance: "Сохранение работы @Valid аннотаций в контроллерах"

#### Рефакторинг сервиса
- [ ] (P1) #11: Рефакторинг UserServiceImpl — Замена прямых валидаций на вызовы UserValidator.  
  acceptance: "Удаление приватных методов валидации, добавление зависимостей на UserValidator"

- [ ] (P2) #12: Обновление обработки ошибок — Адаптация исключений под новый слой валидации.  
  acceptance: "Обработка новых типов исключений в GlobalExceptionHandler"

#### Тестирование
- [ ] (P1) #13: Создание unit-тестов для UserValidator — Покрытие всех сценариев валидации.  
  acceptance: "Тесты для всех методов валидации с позитивными и негативными сценариями"

- [ ] (P2) #14: Обновление существующих тестов — Адаптация тестов UserServiceImpl под новую архитектуру.  
  acceptance: "Все существующие тесты проходят с новой реализацией"

- [ ] (P2) #15: Интеграционные тесты — Тестирование полного flow с новым слоем валидации.  
  acceptance: "Тесты контроллеров с проверкой валидации на всех уровнях"

#### Документация и финализация
- [ ] (P3) #16: Обновление документации — Описание нового слоя валидации в README.  
  acceptance: "Документация по использованию UserValidator и новых исключений"

- [ ] (P3) #17: Создание примеров использования — Примеры кода для разработчиков.  
  acceptance: "Примеры валидации в разных сценариях"

## Assumptions
- Существующие Bean Validation аннотации (@Valid, @NotBlank, @Email) остаются без изменений
- GlobalExceptionHandler может быть расширен для обработки новых типов исключений
- Производительность не должна ухудшиться после рефакторинга
- Обратная совместимость API должна быть сохранена
- Java 24 и Spring Boot 3.x остаются целевыми версиями

## Notes
- Текущие валидации в UserServiceImpl:
  - validateUserUniqueness() - проверка уникальности login/email
  - Проверка последнего админа в inactivateUser() и updateUserRole()
  - Bean Validation через @Valid в patchUser()
  - Обработка JSON патчей с валидацией через Validator

- Архитектура слоя валидации (Step #2 - обновлено):
```
┌─────────────────────────────────────────────────────────────┐
│                    UserServiceImpl                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   createUser()  │  │   updateUser()  │  │ patchUser() │ │
│  └─────────┬───────┘  └─────────┬───────┘  └─────┬───────┘ │
└────────────┼─────────────────────┼─────────────────┼─────────┘
             │                     │                 │
             ▼                     ▼                 ▼
┌─────────────────────────────────────────────────────────────┐
│                  Validation Layer                          │
│                    UserValidator                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │ validateForCreate│  │validateForUpdate│  │validatePatch│ │
│  │ validateUniqueness│ │ validateUniqueness│ │validateJson │ │
│  │ validateBusiness │ │ validateBusiness │ │validateConstraints│ │
│  │ validateAdminDeactivation│ │ validateRoleChange │ │             │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│                Exception Hierarchy                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │ValidationException│  │UniquenessValEx │  │BusinessRule │ │
│  │   (base class)   │  │                 │  │ValidationEx │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

- Ключевые компоненты для реализации:
  - UserValidator интерфейс (объединенный с PatchValidator)
  - UserValidatorImpl класс
  - ValidationException иерархия
  - Интеграция с существующим GlobalExceptionHandler

- Детальный дизайн интерфейсов (Step #2 - обновлено):
  UserValidator (объединенный):
    - validateForCreate(UserRequestDto) - полная валидация для создания
    - validateForUpdate(UUID, UserUpdateDto) - валидация для обновления  
    - validateForPatch(UUID, JsonNode patchNode) - валидация для PATCH операций
    - validateUniqueness(String login, String email, UUID excludeId) - уникальность
    - validateBusinessRules(User user, Operation operation) - бизнес-правила
    - validateAdminDeactivation(UUID userId) - проверка последнего админа
    - validateRoleChange(UUID userId, UserRole newRole) - смена роли
    - validatePatchData(JsonNode patchNode) - валидация JSON структуры патча
    - validatePatchConstraints(UserPatchDto dto) - Bean Validation для патча

  ValidationException иерархия:
    - ValidationException (базовый класс)
      - UniquenessValidationException (конфликты уникальности)
      - BusinessRuleValidationException (нарушение бизнес-правил)
      - FormatValidationException (ошибки формата данных)

- Риски:
  - Нарушение существующих тестов при рефакторинге
  - Снижение производительности из-за дополнительных слоев
  - Сложность миграции существующей логики
  - Потенциальные проблемы с транзакциями при валидации

- Зависимости:
  - Spring Boot Validation
  - Существующий UserRepository
  - GlobalExceptionHandler из common-lib
  - Jackson ObjectMapper для JSON патчей
