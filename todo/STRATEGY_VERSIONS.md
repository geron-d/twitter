# Стратегия централизации версий зависимостей

## Анализ текущего состояния

### Дублирующиеся зависимости
Следующие зависимости имеют одинаковые версии в `users-api` и `common-lib`:

| Зависимость | Версия | Модули |
|-------------|--------|---------|
| swagger-annotations | 2.2.38 | users-api, common-lib |
| springdoc-openapi-starter-webmvc-ui | 2.8.13 | users-api, common-lib |
| lombok | 1.18.38 | users-api, common-lib |
| mapstruct | 1.6.3 | users-api, common-lib |
| mapstruct-processor | 1.6.3 | users-api, common-lib |
| lombok-mapstruct-binding | 0.2.0 | users-api, common-lib |

### Уникальные зависимости
- `postgresql: 42.7.7` - только в users-api
- `micrometer-tracing-bridge-otel` - только в users-api (версия управляется Spring Boot)

## Рекомендуемая структура ext блока

```gradle
ext {
    // OpenAPI/Swagger
    swaggerAnnotationsVersion = '2.2.38'
    springdocOpenapiVersion = '2.8.13'
    
    // Code Generation
    lombokVersion = '1.18.38'
    mapstructVersion = '1.6.3'
    lombokMapstructBindingVersion = '0.2.0'
    
    // Database
    postgresqlVersion = '42.7.7'
    
    // Testing (если потребуется централизация)
    junitJupiterVersion = '5.10.1'
    testcontainersVersion = '1.21.3'
}
```

## Стратегия миграции

### Этап 1: Подготовка корневого build.gradle
- Добавить ext блок с версиями
- Сохранить существующий dependencyManagement
- Обеспечить совместимость с Spring Boot BOM

### Этап 2: Обновление подпроектов
- Заменить хардкодные версии на переменные из ext
- Сохранить exclude блоки для конфликтующих зависимостей
- Проверить корректность сборки

### Этап 3: Валидация
- Сборка всех модулей
- Проверка отсутствия конфликтов версий
- Тестирование функциональности

## Преимущества централизации

1. **Консистентность**: Все модули используют одинаковые версии
2. **Упрощение обновлений**: Изменение версии в одном месте
3. **Снижение ошибок**: Меньше шансов на конфликты версий
4. **Улучшение maintainability**: Четкая структура управления зависимостями

## Риски и митигация

### Риск: Нарушение сборки
**Митигация**: Поэтапное внедрение с тестированием

### Риск: Конфликты с Spring Boot BOM
**Митигация**: Использование dependencyManagement для управления транзитивными зависимостями

### Риск: Усложнение процесса
**Митигация**: Документирование процесса и создание скриптов проверки

## Следующие шаги

1. Реализовать ext блок в корневом build.gradle
2. Обновить users-api/build.gradle
3. Обновить common-lib/build.gradle
4. Провести валидацию сборки
5. Обновить документацию
