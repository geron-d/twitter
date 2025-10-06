# Changelog

## 2025-01-27

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

