# Инструкция по применению изменений для шага #3

## Описание изменений
Добавлен ext блок в корневой build.gradle для централизованного управления версиями зависимостей.

## Файлы для изменения
- `build.gradle` (корневой файл проекта)

## Применение патча
Используйте файл `todo/patch_step3_build_gradle.diff` для применения изменений:

```bash
# Применить патч
git apply todo/patch_step3_build_gradle.diff

# Или вручную скопировать содержимое из todo/build_gradle_updated.gradle
```

## Структура ext блока
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
}
```

## Проверка корректности
После применения изменений выполните:

```bash
# Проверить синтаксис Gradle
./gradlew help

# Проверить доступность переменных
./gradlew properties | grep -E "(swagger|lombok|mapstruct|postgresql)"
```

## Совместимость
- Сохранена совместимость с существующим dependencyManagement
- Spring Boot BOM продолжает управлять транзитивными зависимостями
- Структура проекта не изменена

## Следующие шаги
После применения этого изменения можно переходить к шагу #4 - обновлению users-api/build.gradle
