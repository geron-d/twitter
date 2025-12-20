# Changelog - Follower API Service

## 2025-12-17 20:10 — step 11 done — Реализация Entity Follow — автор: assistant

Создана Entity Follow в пакете com.twitter.entity для представления подписок пользователей:
- Поля: id (UUID, Primary Key), followerId (UUID, NOT NULL), followingId (UUID, NOT NULL), createdAt (LocalDateTime, NOT NULL)
- JPA аннотации: @Entity, @Table с uniqueConstraints для (follower_id, following_id), @Id с @GeneratedValue(strategy = GenerationType.UUID), @Column для всех полей
- Lombok аннотации: @Data, @Accessors(chain = true), @NoArgsConstructor, @AllArgsConstructor
- @CreationTimestamp для автоматической установки createdAt при создании
- Полная JavaDoc документация с @author geron, @version 1.0, включая описание всех полей и бизнес-правил

Entity соответствует стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Entity (User, Tweet). Уникальное ограничение на уровне БД предотвращает двойные подписки, а CHECK ограничение (на уровне БД) предотвращает подписку на себя. Проверка линтера: ошибок не обнаружено.

## 2025-12-17 20:00 — step 10 done — Обновление docker-compose.yml — автор: assistant

Добавлен сервис follower-api в docker-compose.yml после admin-script-api. Настроена полная конфигурация:
- Build: context (.), dockerfile (services/follower-api/Dockerfile)
- Container name: twitter-follower-api
- Порт: 8084:8084
- Зависимости: postgres (service_healthy), users-api (service_healthy)
- Environment variables:
  - SPRING_PROFILES_ACTIVE=docker
  - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/twitter
  - SPRING_DATASOURCE_USERNAME=user
  - SPRING_DATASOURCE_PASSWORD=password
  - SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
  - SPRING_JPA_HIBERNATE_DDL_AUTO=validate
  - SPRING_JPA_SHOW_SQL=false
  - LOGGING_LEVEL_COM_TWITTER=DEBUG
  - LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG
- Healthcheck: /actuator/health (interval=30s, timeout=10s, retries=3, start_period=60s)
- Volumes: ./logs:/app/logs
- Network: twitter-network
- Restart: unless-stopped

Конфигурация соответствует структуре других сервисов (users-api, tweet-api, admin-script-api) и проектированию из DOCKER_DESIGN.md. Сервис готов для развертывания через docker-compose up.

## 2025-12-17 19:50 — step 9 done — Создание Dockerfile — автор: assistant

Создан Dockerfile для follower-api в services/follower-api/Dockerfile с multi-stage build:
- Stage 1 (build): использует gradle:jdk24 для сборки приложения
  - Копирование Gradle файлов для кэширования
  - Загрузка зависимостей (./gradlew dependencies)
  - Сборка приложения (./gradlew :services:follower-api:build -x test --no-daemon --parallel --build-cache)
- Stage 2 (runtime): использует eclipse-temurin:24-jre для runtime
  - Установка curl для healthcheck
  - Создание non-root пользователя appuser для безопасности
  - Копирование JAR файла из build stage
  - Создание директории для логов
  - Переключение на non-root пользователя
  - Настройка порта 8084 (EXPOSE 8084)
  - Настройка JVM опций (Xms512m, Xmx1024m, UseG1GC, UseContainerSupport)
  - Настройка healthcheck на /actuator/health (interval=30s, timeout=3s, start-period=60s, retries=3)
  - Запуск приложения через ENTRYPOINT

Dockerfile соответствует структуре других сервисов (users-api, tweet-api, admin-script-api) и проектированию из DOCKER_DESIGN.md. Готов для использования в docker-compose.yml.

## 2025-12-17 19:40 — step 8 done — Создание application-docker.yml — автор: assistant

Создан application-docker.yml для follower-api в services/follower-api/src/main/resources/application-docker.yml. Настроена конфигурация для Docker окружения:
- users-api URL: http://users-api:8081 (через имя сервиса Docker вместо localhost)
- Конфигурация базы данных будет передаваться через environment variables в docker-compose.yml (SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/twitter), что соответствует паттерну других сервисов
- Профиль docker будет активирован через environment variable SPRING_PROFILES_ACTIVE=docker в docker-compose.yml

Конфигурация соответствует структуре других сервисов (tweet-api, admin-script-api) и проектированию из DOCKER_DESIGN.md. Файл готов для использования в Docker окружении.

## 2025-12-17 19:30 — step 7 done — Реализация Config — автор: assistant

Созданы конфигурационные классы для follower-api:
- OpenApiConfig в пакете com.twitter.config:
  - @Configuration для Spring конфигурации
  - @Bean метод followerApiOpenAPI() для создания OpenAPI спецификации
  - Настройка Info с title "Twitter Follower API", подробным description (возможности API, аутентификация, rate limiting, обработка ошибок), version "1.0.0"
  - Настройка Server с url "http://localhost:8084" и description "Local development server"
  - Полная JavaDoc документация (@author geron, @version 1.0)
- FeignConfig в пакете com.twitter.config:
  - @Configuration для Spring конфигурации
  - @EnableFeignClients(basePackages = "com.twitter.client") для активации Feign клиентов
  - Сканирование пакета com.twitter.client для поиска Feign Client интерфейсов
  - Полная JavaDoc документация (@author geron, @version 1.0)

Все классы соответствуют стандартам проекта (STANDART_SWAGGER.md, STANDART_CODE.md) и структуре других сервисов (tweet-api, admin-script-api). Проверка линтера: ошибок не обнаружено.

## 2025-12-17 19:20 — step 6 done — Создание SQL скрипта — автор: assistant

Создан SQL скрипт sql/follows.sql для таблицы follows. Скрипт включает:
- CREATE TABLE follows с полями: id (UUID PRIMARY KEY), follower_id (UUID NOT NULL), following_id (UUID NOT NULL), created_at (TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)
- FOREIGN KEY ограничения на users(id) для обоих полей (follows_follower_fk, follows_following_fk)
- UNIQUE ограничение на (follower_id, following_id) (follows_unique_follower_following) - предотвращает двойные подписки
- CHECK ограничение (follower_id != following_id) (follows_check_no_self_follow) - предотвращает подписку на себя
- Индексы для оптимизации запросов: idx_follows_follower_id, idx_follows_following_id, idx_follows_created_at

Скрипт соответствует структуре из ANALYSIS_DESIGN.md и стилю существующих SQL скриптов проекта (users.sql, tweets.sql).

## 2025-12-17 19:15 — step 5 done — Создание application.yml — автор: assistant

Создан application.yml для follower-api в services/follower-api/src/main/resources/application.yml. Настроены все параметры:
- server.port=8084
- spring.application.name=follower-api
- Подключение к PostgreSQL (jdbc:postgresql://localhost:5432/twitter)
- Feign клиент для users-api (http://localhost:8081) с таймаутами (connect-timeout=2000, read-timeout=5000)
- SpringDoc OpenAPI параметры (path /v3/api-docs, swagger-ui настройки)
- Management endpoints (health, info, metrics, tracing)
- Логирование (уровни DEBUG для com.twitter, паттерны для console и file)
- Пагинация (default-page-size=10, max-page-size=100)
- Jackson настройки для дат (write-dates-as-timestamps=false, time-zone=UTC)
- JPA настройки (ddl-auto=validate)

Конфигурация соответствует структуре других сервисов (tweet-api, admin-script-api) и готова для использования в локальной разработке.

## 2025-12-17 18:39 — step 4 done — Создание build.gradle для follower-api — автор: assistant

Создан build.gradle для follower-api в services/follower-api/build.gradle. Настроены все зависимости: shared модули (common-lib, database), Spring Boot starters (web, data-jpa, validation, actuator), Spring Cloud OpenFeign для интеграции с users-api, OpenAPI/Swagger, Lombok и MapStruct с правильными annotation processors (включая lombok-mapstruct-binding), PostgreSQL driver, тестовые зависимости (включая WireMock для мокирования users-api). Настроен compileJava с параметрами для MapStruct (defaultComponentModel=spring, unmappedTargetPolicy=IGNORE). Настроен springBoot с mainClass.

## 2025-12-17 18:37 — step 3 done — Обновление settings.gradle — автор: assistant

Добавлена строка `include 'services:follower-api'` в settings.gradle. Модуль follower-api теперь включен в структуру проекта Gradle и будет доступен для сборки.

## 2025-12-17 18:34 — step 2 done — Проектирование Docker конфигурации — автор: assistant

Создан документ DOCKER_DESIGN.md с полным проектированием Docker конфигурации для follower-api:
- Определена структура Dockerfile с multi-stage build (gradle:jdk24 для сборки, eclipse-temurin:24-jre для runtime)
- Определена конфигурация application-docker.yml (URL users-api через имя сервиса Docker http://users-api:8081, профиль docker)
- Определена полная конфигурация в docker-compose.yml (зависимости от postgres и users-api с условием service_healthy, environment variables, healthcheck на порту 8084, volumes для логов, network twitter-network)
- Проектирование соответствует паттернам существующих сервисов (users-api, tweet-api, admin-script-api)
- Учтены требования безопасности (non-root user, минимальный runtime образ)
- Определены JVM опции для контейнера (Xms512m, Xmx1024m, G1GC, UseContainerSupport)

## 2025-01-27 10:30 — step 1 done — Анализ требований и проектирование API — автор: assistant

Создан документ ANALYSIS_DESIGN.md с полным проектированием микросервиса follower-api:
- Определены все 6 REST эндпоинтов (POST, DELETE, GET для подписок/отписок, списков, статуса, статистики)
- Определена структура Entity Follow с уникальным ограничением на (follower_id, following_id)
- Определены все DTO (7 DTO: FollowRequestDto, FollowResponseDto, FollowerResponseDto, FollowingResponseDto, FollowStatusResponseDto, FollowStatsResponseDto, FollowerFilter, FollowingFilter)
- Определены 3 бизнес-правила (нельзя подписаться на себя, нельзя подписаться дважды, оба пользователя должны существовать)
- Определена структура таблицы follows в БД с индексами и ограничениями
- Описана интеграция с users-api через Feign Client
- Определена структура всех слоев (Service, Repository, Validator, Mapper, Controller)

