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

### 15:56 — step 6 done — Создание TweetRepository — автор: assistant
- Создан TweetRepository интерфейс в пакете com.twitter.repository
- Расширяет JpaRepository<Tweet, UUID> для базовых CRUD операций
- Добавлены кастомные методы для поиска твитов по пользователю
- Реализована пагинация для всех основных запросов
- Добавлен поиск по содержимому с игнорированием регистра
- Реализованы методы для работы с временными диапазонами
- Добавлены методы для подсчёта твитов и массового удаления
- Проект успешно компилируется

### 16:00 — step 7 done — Создание CreateTweetRequestDto — автор: assistant
- Создан CreateTweetRequestDto в пакете com.twitter.dto.request
- Добавлена валидация @NotBlank для поля content
- Реализована валидация @Size(min=1, max=280) для длины контента
- Добавлена валидация @NotNull для поля userId
- Использованы Lombok аннотации (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
- Добавлена подробная JavaDoc документация
- Проект успешно компилируется

### 16:02 — step 8 done — Создание TweetResponseDto — автор: assistant
- Создан TweetResponseDto в пакете com.twitter.dto.response
- Добавлены все необходимые поля: id, userId, content, createdAt, updatedAt
- Использованы правильные типы данных (UUID, String, LocalDateTime)
- Добавлено JSON форматирование дат с @JsonFormat
- Использованы Lombok аннотации (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
- Добавлена подробная JavaDoc документация
- Проект успешно компилируется

### 16:04 — step 9 done — Создание MapStruct маппера — автор: assistant
- Создан TweetMapper интерфейс в пакете com.twitter.mapper
- Настроен MapStruct с Spring component model
- Реализованы методы для конвертации:
  - CreateTweetRequestDto → Tweet (toEntity)
  - Tweet → TweetResponseDto (toResponseDto)
  - Update операции (updateEntity)
  - Tweet → CreateTweetRequestDto (toRequestDto)
- MapStruct успешно сгенерировал реализацию TweetMapperImpl
- Проект успешно компилируется

### 16:06 — step 10 done — Создание интерфейса TweetService — автор: assistant
- Создан интерфейс TweetService в пакете com.twitter.service
- Определён метод createTweet(CreateTweetRequestDto) → TweetResponseDto
- Добавлена подробная JavaDoc документация с описанием процесса
- Описаны возможные исключения (ConstraintViolationException, UserNotFoundException, TweetCreationException)
- Проект успешно компилируется

### 16:08 — step 11 done — Реализация TweetServiceImpl — автор: assistant
- Создан TweetServiceImpl в пакете com.twitter.service.impl
- Реализован полный цикл создания твита:
  - Валидация входных данных с Bean Validation
  - Проверка существования пользователя (placeholder)
  - Конвертация DTO → Entity → Response DTO
  - Сохранение в базу данных
- Добавлены Spring аннотации (@Service, @Transactional, @RequiredArgsConstructor)
- Реализовано логирование с SLF4J (@Slf4j)
- Добавлена обработка исключений и транзакционность
- Проект успешно компилируется