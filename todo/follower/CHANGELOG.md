# Changelog - Follower API Service

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

