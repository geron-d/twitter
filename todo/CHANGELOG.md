# Changelog

## 2025-01-27

### 17:00 — step 7 done — Добавление аннотаций к перечислениям — автор: assistant

**Изменения:**
- Добавлены аннотации @Schema к enum классам UserRole и UserStatus
- Добавлены описания для каждого значения enum
- Добавлены примеры использования в Swagger UI
- Сохранена существующая документация JavaDoc

**Артефакты:**
- `shared/common-lib/src/main/java/com/twitter/common/enums/UserRole.java` - обновлен
- `shared/common-lib/src/main/java/com/twitter/common/enums/UserStatus.java` - обновлен

**Покрытие enum значений:**
- **UserRole**: ADMIN, MODERATOR, USER
- **UserStatus**: ACTIVE, INACTIVE

**Особенности:**
- Каждое значение enum имеет детальное описание
- Примеры использования для каждого значения
- Сохранена иерархия ролей (USER → MODERATOR → ADMIN)
- Описаны права доступа для каждой роли
- Описаны ограничения для каждого статуса

**Статус:** Готово к тестированию в Swagger UI

### 16:45 — step 6 done — Создание схем для обработки ошибок (исправлено) — автор: assistant

**Изменения:**
- Добавлена зависимость SpringDoc OpenAPI в common-lib модуль
- Добавлены аннотации @Schema к классам исключений в shared модуле
- Удалены временные DTO файлы из users-api модуля
- Схемы соответствуют RFC 7807 Problem Details for HTTP APIs

**Артефакты:**
- `shared/common-lib/build.gradle` - добавлена зависимость SpringDoc OpenAPI
- `shared/common-lib/src/main/java/com/twitter/common/exception/validation/ValidationException.java` - обновлен
- `shared/common-lib/src/main/java/com/twitter/common/exception/validation/UniquenessValidationException.java` - обновлен
- `shared/common-lib/src/main/java/com/twitter/common/exception/validation/BusinessRuleValidationException.java` - обновлен
- `shared/common-lib/src/main/java/com/twitter/common/exception/validation/FormatValidationException.java` - обновлен

**Покрытие ошибок:**
- ValidationException - базовая схема для всех ошибок валидации
- UniquenessValidationException - ошибки дублирования данных (409)
- BusinessRuleValidationException - ошибки бизнес-правил (400)
- FormatValidationException - ошибки формата данных (400)

**Особенности:**
- Схемы соответствуют RFC 7807 стандарту
- Все поля имеют описания и примеры
- Поддержка дополнительных полей для специфичных ошибок
- Timestamp для отслеживания времени ошибок
- Nullable поля для опциональных данных
- Централизованное расположение в shared модуле

**Статус:** Готово к тестированию в Swagger UI после сборки проекта

### 16:30 — step 6 done — Создание схем для обработки ошибок — автор: assistant

**Изменения:**
- Созданы DTO схемы для ошибок в users-api модуле
- Схемы соответствуют RFC 7807 Problem Details for HTTP APIs
- Добавлены детальные примеры для всех типов ошибок
- Схемы интегрированы с GlobalExceptionHandler

**Артефакты:**
- `services/users-api/src/main/java/com/twitter/dto/error/ProblemDetailDto.java` - создан
- `services/users-api/src/main/java/com/twitter/dto/error/UniquenessValidationErrorDto.java` - создан
- `services/users-api/src/main/java/com/twitter/dto/error/BusinessRuleValidationErrorDto.java` - создан
- `services/users-api/src/main/java/com/twitter/dto/error/FormatValidationErrorDto.java` - создан

**Покрытие ошибок:**
- ProblemDetailDto - базовая схема для всех ошибок
- UniquenessValidationErrorDto - ошибки дублирования данных (409)
- BusinessRuleValidationErrorDto - ошибки бизнес-правил (400)
- FormatValidationErrorDto - ошибки формата данных (400)

**Особенности:**
- Схемы соответствуют RFC 7807 стандарту
- Все поля имеют описания и примеры
- Поддержка дополнительных полей для специфичных ошибок
- Timestamp для отслеживания времени ошибок
- Nullable поля для опциональных данных

**Статус:** Готово к тестированию в Swagger UI

### 16:15 — step 5 done — Создание схем для DTO классов — автор: assistant

**Изменения:**
- Добавлены аннотации @Schema ко всем DTO классам
- Добавлены детальные описания полей с примерами
- Настроена валидация через OpenAPI схемы
- Добавлены примеры JSON для всех DTO

**Артефакты:**
- `services/users-api/src/main/java/com/twitter/dto/UserRequestDto.java` - обновлен
- `services/users-api/src/main/java/com/twitter/dto/UserResponseDto.java` - обновлен  
- `services/users-api/src/main/java/com/twitter/dto/UserUpdateDto.java` - обновлен
- `services/users-api/src/main/java/com/twitter/dto/UserRoleUpdateDto.java` - обновлен
- `services/users-api/src/main/java/com/twitter/dto/filter/UserFilter.java` - обновлен

**Покрытие DTO:**
- UserRequestDto - схема для создания пользователей с валидацией
- UserResponseDto - схема для ответов API (без пароля)
- UserUpdateDto - схема для обновления пользователей
- UserRoleUpdateDto - схема для изменения ролей
- UserFilter - схема для фильтрации пользователей

**Особенности:**
- Пароли помечены как WRITE_ONLY для безопасности
- UUID поля имеют правильный формат
- Email поля имеют валидацию формата
- Enum поля ссылаются на соответствующие классы
- Все поля имеют примеры и описания

**Статус:** Готово к тестированию в Swagger UI

### 16:00 — step 4 done — Рефакторинг OpenAPI аннотаций в интерфейс — автор: assistant

**Изменения:**
- Создан интерфейс `UserApi` с полным набором OpenAPI аннотаций
- Все аннотации @Operation, @ApiResponse, @Parameter, @ExampleObject перенесены в интерфейс
- UserController теперь реализует интерфейс UserApi
- Убраны все OpenAPI импорты из UserController
- Добавлены аннотации @Override для всех методов контроллера

**Артефакты:**
- `services/users-api/src/main/java/com/twitter/controller/UserApi.java` - создан
- `services/users-api/src/main/java/com/twitter/controller/UserController.java` - рефакторинг

**Преимущества:**
- Разделение ответственности: интерфейс для документации, контроллер для реализации
- Переиспользование: интерфейс можно использовать для генерации клиентского кода
- Чистота кода: контроллер стал более читаемым без аннотаций
- Централизация: вся документация API в одном месте

**Статус:** Готово к тестированию в Swagger UI

### 15:45 — step 4 done — Добавление OpenAPI аннотаций к UserController — автор: assistant

**Изменения:**
- Добавлены импорты OpenAPI аннотаций (io.swagger.v3.oas.annotations.*)
- Добавлена аннотация @Tag к классу UserController для группировки операций
- Добавлены детальные аннотации @Operation для всех 7 эндпоинтов
- Добавлены аннотации @ApiResponses с примерами для всех возможных ответов (200, 400, 404, 409)
- Добавлены аннотации @Parameter для всех параметров запроса
- Добавлены примеры запросов и ответов через @ExampleObject

**Артефакты:**
- `services/users-api/src/main/java/com/twitter/controller/UserController.java` - полностью обновлен

**Покрытие:**
- GET /{id} - получение пользователя по ID
- GET / - получение списка пользователей с фильтрацией и пагинацией  
- POST / - создание нового пользователя
- PUT /{id} - полное обновление пользователя
- PATCH /{id} - частичное обновление пользователя
- PATCH /{id}/inactivate - деактивация пользователя
- PATCH /{id}/role - обновление роли пользователя

**Статус:** Готово к тестированию в Swagger UI

### 15:30 — step 3 done — Настройка SpringDoc OpenAPI конфигурации — автор: assistant

**Изменения:**
- Добавлена зависимость `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0` в `build.gradle`
- Создан конфигурационный класс `OpenApiConfig.java` с базовой настройкой OpenAPI
- Обновлен `application.yml` с настройками Swagger UI
- Создан `SWAGGER_README.md` с инструкциями по использованию

**Артефакты:**
- `services/users-api/build.gradle` - обновлен
- `services/users-api/src/main/java/com/twitter/config/OpenApiConfig.java` - создан
- `services/users-api/src/main/resources/application.yml` - обновлен
- `services/users-api/SWAGGER_README.md` - создан

**Статус:** Готово к тестированию после сборки проекта

