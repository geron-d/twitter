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

- [x] (P1) [2025-01-21 14:45] #3: Определение типов валидаций — Категоризация всех валидаций по типам (уникальность, бизнес-правила, формат).  
  acceptance: "Список всех валидаций с их типами и контекстом использования"
  note: "Проанализированы все валидации в UserServiceImpl и DTO, создана детальная категоризация"

- [x] (P1) [2025-01-21 15:00] #4: Создание интерфейса UserValidator — Определение контракта для валидации пользователей.  
  acceptance: "Интерфейс с методами для каждого типа валидации"
  note: "Создан полный интерфейс UserValidator с методами для всех типов валидаций и детальной документацией"

- [x] (P1) [2025-01-21 15:15] #5: Проектирование исключений валидации — Создание специализированных исключений для разных типов ошибок.  
  acceptance: "Иерархия исключений: ValidationException -> UniquenessValidationException, BusinessRuleValidationException"
  note: "Создана полная иерархия исключений с ValidationException, UniquenessValidationException, BusinessRuleValidationException, FormatValidationException. Обновлено с использованием Lombok @Getter. ValidationType вынесен в отдельный enum класс. Исправлен конфликт @AllArgsConstructor в enum"

### To Do

- [x] (P1) [2025-01-21 15:30] #6: Создание UserValidatorImpl — Реализация основного класса валидации.  
  acceptance: "Класс с методами validateForCreate, validateForUpdate, validateForPatch, validateBusinessRules"
  note: "Создан UserValidatorImpl с полной реализацией всех методов интерфейса UserValidator. Перенесена логика валидации из UserServiceImpl с использованием новых исключений. Рефакторинг: разделен validateForPatch на два метода, создан PatchDtoFactory в пакете util. Перемещены исключения в пакет exception.validation"

- [x] (P2) [2025-01-21 15:30] #9: Создание валидатора для JSON патчей — Интеграция PATCH валидации в UserValidator.  
  acceptance: "Методы validateForPatch, validatePatchData, validatePatchConstraints в UserValidator"
  note: "Реализовано в UserValidatorImpl: validateForPatch(), validatePatchData(), validatePatchConstraints()"

- [ ] (P2) #10: Интеграция с существующими DTO — Обеспечение совместимости с текущими аннотациями валидации.  
  acceptance: "Сохранение работы @Valid аннотаций в контроллерах"

#### Рефакторинг сервиса
- [x] (P1) [2025-01-21 17:00] #11: Рефакторинг UserServiceImpl — Замена прямых валидаций на вызовы UserValidator.  
  acceptance: "Удаление приватных методов валидации, добавление зависимостей на UserValidator"
  note: "Полностью рефакторирован UserServiceImpl: заменены все прямые валидации на вызовы UserValidator. Удален метод validateUserUniqueness. Убраны неиспользуемые поля objectMapper и validator. Все методы теперь используют новый слой валидации."

- [x] (P2) [2025-01-21 18:00] #12: Обновление обработки ошибок — Адаптация исключений под новый слой валидации.  
  acceptance: "Обработка новых типов исключений в GlobalExceptionHandler"
  note: "Добавлен импорт ValidationException и обработчик для базового класса. Все исключения валидации теперь корректно обрабатываются в GlobalExceptionHandler. Тесты UserServiceImplTest прошли успешно."

#### Тестирование
- [x] (P1) [2025-01-21 18:30] #13: Создание unit-тестов для UserValidator — Покрытие всех сценариев валидации.  
  acceptance: "Тесты для всех методов валидации с позитивными и негативными сценариями"
  note: "Создан comprehensive набор unit-тестов UserValidatorImplTest с 8 вложенными классами тестов, покрывающими все методы валидации. Включены позитивные и негативные сценарии для всех методов: validateForCreate, validateForUpdate, validateForPatch, validateForPatchWithDto, validateUniqueness, validateAdminDeactivation, validateRoleChange, validatePatchData, validatePatchConstraints. Все тесты прошли успешно."

- [x] (P2) [2025-01-21 19:00] #14: Обновление существующих тестов — Адаптация тестов UserServiceImpl под новую архитектуру.  
  acceptance: "Все существующие тесты проходят с новой реализацией"
  note: "Обновлены тесты UserServiceImplTest: добавлены проверки verify(userValidator) для всех методов валидации. Исправлены тесты updateUser, inactivateUser, updateUserRole для корректной проверки вызовов UserValidator. Все тесты проходят успешно."

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

- Категоризация валидаций (Step #3):
  
  **1. ВАЛИДАЦИЯ УНИКАЛЬНОСТИ (UniquenessValidation)**
  - validateUserUniqueness() - проверка уникальности login/email
    * Контекст: createUser(), updateUser(), patchUser()
    * Тип ошибки: ResponseStatusException(HttpStatus.CONFLICT)
    * Логика: проверка через userRepository.existsByLogin/existsByEmail
  
  **2. БИЗНЕС-ПРАВИЛА (BusinessRuleValidation)**
  - Проверка последнего админа в inactivateUser()
    * Контекст: деактивация пользователя с ролью ADMIN
    * Тип ошибки: LastAdminDeactivationException
    * Логика: countByRoleAndStatus(ADMIN, ACTIVE) <= 1
  - Проверка последнего админа в updateUserRole()
    * Контекст: смена роли с ADMIN на другую
    * Тип ошибки: LastAdminDeactivationException
    * Логика: countByRoleAndStatus(ADMIN, ACTIVE) <= 1
  
  **3. ФОРМАТ ДАННЫХ (FormatValidation)**
  - Bean Validation в UserRequestDto:
    * @NotBlank, @Size(min=3,max=50) для login
    * @NotBlank, @Email для email
    * @NotBlank, @Size(min=8) для password
  - Bean Validation в UserUpdateDto:
    * @NotNull, @Size(min=3,max=50) для login
    * @Email для email
    * @Size(min=8) для password
  - Bean Validation в UserPatchDto:
    * @Size(min=3,max=50) для login
    * @Email для email
  - JSON патч валидация в patchUser():
    * Проверка структуры JSON через ObjectMapper
    * Bean Validation через Validator.validate()
    * Тип ошибки: ResponseStatusException(HttpStatus.BAD_REQUEST)
  
  **4. ОБРАБОТКА ОШИБОК (ErrorHandling)**
  - PasswordUtil ошибки в setPassword():
    * NoSuchAlgorithmException, InvalidKeySpecException
    * Тип ошибки: ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)

- Определение интерфейса UserValidator (Step #4):
```java
public interface UserValidator {
    
    // === ОСНОВНЫЕ МЕТОДЫ ВАЛИДАЦИИ ===
    
    /**
     * Полная валидация для создания пользователя
     * @param userRequest DTO с данными для создания
     * @throws ValidationException при нарушении валидации
     */
    void validateForCreate(UserRequestDto userRequest);
    
    /**
     * Валидация для обновления пользователя
     * @param userId ID пользователя для обновления
     * @param userUpdate DTO с данными для обновления
     * @throws ValidationException при нарушении валидации
     */
    void validateForUpdate(UUID userId, UserUpdateDto userUpdate);
    
    /**
     * Валидация для PATCH операций
     * @param userId ID пользователя для патча
     * @param patchNode JSON данные для патча
     * @throws ValidationException при нарушении валидации
     */
    void validateForPatch(UUID userId, JsonNode patchNode);
    
    // === ВАЛИДАЦИЯ УНИКАЛЬНОСТИ ===
    
    /**
     * Проверка уникальности логина и email
     * @param login логин для проверки
     * @param email email для проверки
     * @param excludeUserId ID пользователя для исключения (при обновлении)
     * @throws UniquenessValidationException при конфликте уникальности
     */
    void validateUniqueness(String login, String email, UUID excludeUserId);
    
    // === БИЗНЕС-ПРАВИЛА ===
    
    /**
     * Проверка возможности деактивации пользователя
     * @param userId ID пользователя для деактивации
     * @throws BusinessRuleValidationException при нарушении бизнес-правил
     */
    void validateAdminDeactivation(UUID userId);
    
    /**
     * Проверка возможности смены роли пользователя
     * @param userId ID пользователя
     * @param newRole новая роль
     * @throws BusinessRuleValidationException при нарушении бизнес-правил
     */
    void validateRoleChange(UUID userId, UserRole newRole);
    
    // === ВАЛИДАЦИЯ ФОРМАТА ДАННЫХ ===
    
    /**
     * Валидация JSON структуры патча
     * @param patchNode JSON данные
     * @throws FormatValidationException при ошибке формата
     */
    void validatePatchData(JsonNode patchNode);
    
    /**
     * Bean Validation для DTO патча
     * @param patchDto DTO для валидации
     * @throws FormatValidationException при нарушении ограничений
     */
    void validatePatchConstraints(UserPatchDto patchDto);
}
```

- Схема вызовов методов UserValidator:
  createUser() -> validateForCreate() -> validateUniqueness()
  updateUser() -> validateForUpdate() -> validateUniqueness()
  patchUser() -> validateForPatch() -> validatePatchData() + validatePatchConstraints() + validateUniqueness()
  inactivateUser() -> validateAdminDeactivation()
  updateUserRole() -> validateRoleChange()

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
