# Changelog - Twitter Tweet API

## 2025-01-27

### 15:46 — step 2 done — Создание структуры проекта tweet-api — автор: assistant
- Создана базовая структура проекта tweet-api в services/
- Добавлен build.gradle с необходимыми зависимостями
- Создан Application.java с аннотацией @SpringBootApplication
- Добавлен Dockerfile для контейнеризации (порт 8082)
- Создан application.yml с конфигурацией для порта 8082
- Добавлен .dockerignore для оптимизации Docker сборки
- Обновлен settings.gradle для включения нового модуля
- Проект успешно компилируется

### 15:50 — step 3 done — Схема базы данных для твитов — автор: assistant
- Создан sql/tweets.sql с миграцией для схемы tweet_api
- Создан sql/tweets_test.sql для тестовой схемы tweet_api_test
- Добавлены индексы для производительности (user_id+created_at, created_at, content search)
- Создан триггер для автоматического обновления updated_at
- Добавлены CHECK constraints для валидации контента
- Обновлен application.yml для использования схемы tweet_api
- Создан application-test.yml для тестовой конфигурации

### 15:52 — step 4 done — Конфигурация приложения — автор: assistant
- Проверена и подтверждена конфигурация application.yml для порта 8082
- Настроено подключение к PostgreSQL без отдельной схемы
- Добавлены настройки трейсинга и мониторинга
- Настроено логирование с трейсингом
- Создана тестовая конфигурация application-test.yml
- Проект успешно компилируется

### 15:54 — step 5 done — Реализация JPA Entity Tweet — автор: assistant
- Создана JPA Entity Tweet в пакете com.twitter.entity
- Добавлены все необходимые поля: id, userId, content, createdAt, updatedAt
- Настроен маппинг на таблицу tweets с правильными типами данных
- Добавлена валидация полей (@NotNull, @NotBlank, @Size)
- Использованы аннотации Lombok (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
- Добавлены Hibernate аннотации (@CreationTimestamp, @UpdateTimestamp)
- Реализована кастомная валидация контента в @PrePersist/@PreUpdate
- Проект успешно компилируется
