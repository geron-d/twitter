# TODO: Централизация версий зависимостей через dependencyManagement

## Meta
- project: Twitter Microservices Monorepo
- updated: 2025-01-27
- changelog: todo/CHANGELOG.md
- priority: P1 (Critical Infrastructure)

## Tasks

### Done
- [x] (P1) #1: Анализ текущей структуры зависимостей — Проанализированы build.gradle файлы корневого проекта, users-api и common-lib
  acceptance: "Выявлены дублирующиеся версии зависимостей в подпроектах"

### In Progress
- [ ] (P1) #2: Анализ существующего dependencyManagement — Определение стратегии использования Spring Boot BOM
  acceptance: "Определена оптимальная стратегия централизации через dependencyManagement"

### To Do
- [ ] (P1) #3: Расширение dependencyManagement в корневом build.gradle — Добавление версий для внешних зависимостей
  acceptance: "Добавлены версии для Lombok, MapStruct, Swagger, PostgreSQL в dependencyManagement"

- [ ] (P1) #4: Обновление users-api/build.gradle — Удаление хардкодных версий
  acceptance: "Все хардкодные версии удалены, зависимости управляются через dependencyManagement"

- [ ] (P1) #5: Обновление common-lib/build.gradle — Удаление хардкодных версий
  acceptance: "Все хардкодные версии удалены, зависимости управляются через dependencyManagement"

- [ ] (P2) #6: Валидация сборки — Проверка корректности сборки всех модулей
  acceptance: "Все модули собираются без ошибок"

- [ ] (P2) #7: Обновление документации — Обновление README файлов с новой структурой
  acceptance: "Документация отражает новую структуру управления зависимостями"

- [ ] (P3) #8: Создание скрипта проверки версий — Автоматическая проверка консистентности версий
  acceptance: "Скрипт проверяет отсутствие хардкодных версий в подпроектах"

## Assumptions
- Проект использует Gradle с Spring Boot 3.5.5
- Все подпроекты уже используют dependencyManagement через Spring Boot BOM
- Необходимо расширить существующий dependencyManagement для внешних зависимостей
- Lombok, MapStruct, Swagger и PostgreSQL драйвер не управляются Spring Boot BOM
- TestContainers уже управляется через testcontainers-bom

## Risks & Mitigation
- **Риск**: Конфликт версий между dependencyManagement и хардкодными версиями
  - **Митигация**: Полное удаление хардкодных версий из подпроектов

- **Риск**: Нарушение сборки при изменении dependencyManagement
  - **Митигация**: Поэтапное внедрение с тестированием на каждом шаге

- **Риск**: Несовместимость версий с Spring Boot BOM
  - **Митигация**: Использование совместимых версий зависимостей

## Dependencies Analysis
### Зависимости, требующие централизации через dependencyManagement:
- `swagger-annotations: 2.2.38` (users-api, common-lib)
- `springdoc-openapi-starter-webmvc-ui: 2.8.13` (users-api, common-lib)
- `lombok: 1.18.38` (users-api, common-lib)
- `mapstruct: 1.6.3` (users-api, common-lib)
- `mapstruct-processor: 1.6.3` (users-api, common-lib)
- `lombok-mapstruct-binding: 0.2.0` (users-api, common-lib)
- `postgresql: 42.7.7` (users-api)

### Зависимости, уже управляемые Spring Boot BOM:
- Все spring-boot-starter-* зависимости
- micrometer-tracing-bridge-otel
- spring-boot-starter-test
- junit-jupiter-* (частично)

## Implementation Strategy
1. **Расширение dependencyManagement** - добавление версий для внешних зависимостей в корневой build.gradle
2. **Удаление хардкодных версий** - полное удаление версий из подпроектов
3. **Валидация на каждом шаге** - проверка сборки после каждого изменения
4. **Документирование процесса** - создание руководства по обновлению версий

## Success Criteria
- Все версии зависимостей управляются через dependencyManagement
- Отсутствуют хардкодные версии в подпроектах
- Все модули собираются успешно
- Процесс обновления зависимостей упрощен и документирован

## Notes
- Использовать существующий dependencyManagement блок в subprojects
- Добавить версии для внешних зависимостей в dependencyManagement
- Полностью удалить хардкодные версии из подпроектов
- Обеспечить совместимость с Spring Boot BOM
- Подготовить rollback план на случай проблем
