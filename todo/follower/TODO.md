# TODO: Follower API Service Implementation

## Обзор
Реализация нового микросервиса follower-api для управления подписками пользователей на других пользователей в системе Twitter. Сервис будет предоставлять REST API для подписки/отписки, получения списков подписчиков и подписок, проверки статуса подписки и получения статистики.

## Задачи

### Анализ и проектирование

- [ ] (P1) #1: Анализ требований и проектирование API - определить все эндпоинты, DTO структуру, Entity структуру, бизнес-правила
  - Зависимости: нет
  - Acceptance criteria:
    - Определены все эндпоинты (POST, DELETE, GET для подписок/отписок, списков, статуса, статистики)
    - Определена структура Entity Follow с уникальным ограничением
    - Определены все DTO (Request, Response, Filter)
    - Определены бизнес-правила (нельзя подписаться на себя, нельзя подписаться дважды)
    - Определена структура таблицы follows в БД

- [ ] (P1) #2: Проектирование Docker конфигурации - определить структуру Dockerfile, application-docker.yml, конфигурацию в docker-compose.yml
  - Зависимости: #1
  - Acceptance criteria:
    - Определена структура Dockerfile (multi-stage build)
    - Определена конфигурация для Docker окружения (URL сервисов через имена)
    - Определена конфигурация в docker-compose.yml (зависимости, environment variables, healthcheck)

### Реализация кода

- [ ] (P1) #3: Обновление settings.gradle - добавить include для follower-api
  - Зависимости: нет
  - Acceptance criteria:
    - Добавлена строка `include 'services:follower-api'` в settings.gradle

- [ ] (P1) #4: Создание build.gradle для follower-api - настроить зависимости и аннотационные процессоры
  - Зависимости: #1
  - Acceptance criteria:
    - Создан build.gradle с зависимостями на shared:common-lib и shared:database
    - Настроены Spring Boot starters (web, data-jpa, validation, actuator)
    - Настроены OpenAPI/Swagger зависимости
    - Настроены Lombok и MapStruct с правильными annotation processors
    - Настроен compileJava с параметрами для MapStruct

- [ ] (P1) #5: Создание SQL скрипта - создать sql/follows.sql для таблицы follows
  - Зависимости: #1
  - Acceptance criteria:
    - Создан SQL скрипт с CREATE TABLE follows
    - Определены все поля (id, follower_id, following_id, created_at)
    - Добавлено UNIQUE ограничение на (follower_id, following_id)
    - Добавлено CHECK ограничение (follower_id != following_id)

- [ ] (P1) #6: Реализация Entity Follow - создать JPA сущность с уникальным ограничением на (follower_id, following_id)
  - Зависимости: #1, #5
  - Acceptance criteria:
    - Создана Entity Follow с полями id, followerId, followingId, createdAt
    - Использованы правильные JPA аннотации (@Entity, @Table, @Id, @Column)
    - Добавлено уникальное ограничение через @Table(uniqueConstraints)
    - Использован @CreationTimestamp для createdAt
    - Добавлена полная JavaDoc документация

- [ ] (P1) #7: Реализация DTO (Records) - создать все Request, Response, Filter DTO с валидацией
  - Зависимости: #1
  - Acceptance criteria:
    - Создан FollowRequestDto (followerId, followingId) с валидацией
    - Создан FollowResponseDto с информацией о подписке
    - Создан FollowerResponseDto (id, login, createdAt) для списка подписчиков
    - Создан FollowingResponseDto (id, login, createdAt) для списка подписок
    - Создан FollowStatusResponseDto (isFollowing, createdAt)
    - Создан FollowStatsResponseDto (followersCount, followingCount)
    - Создан FollowerFilter для фильтрации подписчиков
    - Создан FollowingFilter для фильтрации подписок
    - Все DTO используют Records (Java 24)
    - Все DTO имеют валидационные аннотации
    - Все DTO имеют @Schema аннотации для Swagger

- [ ] (P1) #8: Реализация Gateway для users-api - создать UsersApiClient (Feign) и UserGateway
  - Зависимости: #1, #4
  - Acceptance criteria:
    - Создан UsersApiClient с методом existsUser для проверки существования пользователя
    - Настроен Feign Client с правильным URL и path
    - Создан UserGateway с методом existsUser для обёртки вызовов
    - Добавлена обработка ошибок в UserGateway
    - Добавлено логирование

- [ ] (P1) #9: Реализация Repository - создать FollowRepository с Derived Query Methods
  - Зависимости: #6
  - Acceptance criteria:
    - Создан FollowRepository extends JpaRepository<Follow, UUID>
    - Добавлены Derived Query Methods (existsByFollowerIdAndFollowingId, findByFollowerId, findByFollowingId, countByFollowerId, countByFollowingId)
    - Repository НЕ имеет JavaDoc для Derived Query Methods (согласно стандартам)

- [ ] (P1) #10: Реализация Mapper (MapStruct) - создать интерфейс FollowMapper с маппингами
  - Зависимости: #6, #7
  - Acceptance criteria:
    - Создан FollowMapper интерфейс с @Mapper
    - Добавлены методы маппинга (toFollow, toFollowResponseDto, toFollowerResponseDto, toFollowingResponseDto)
    - Настроены игнорируемые поля (@Mapping(target = "...", ignore = true))
    - Mapper настроен как Spring компонент

- [ ] (P1) #11: Реализация Validator - создать FollowValidator interface и implementation с проверкой бизнес-правил
  - Зависимости: #7, #8, #9
  - Acceptance criteria:
    - Создан интерфейс FollowValidator
    - Создана реализация FollowValidatorImpl
    - Реализована валидация: нельзя подписаться на себя
    - Реализована валидация: нельзя подписаться дважды
    - Реализована валидация: оба пользователя должны существовать (через UserGateway)
    - Используются исключения из common-lib (BusinessRuleValidationException)
    - Добавлено логирование

- [ ] (P1) #12: Реализация Service - создать FollowService interface и implementation с бизнес-логикой
  - Зависимости: #7, #9, #10, #11
  - Acceptance criteria:
    - Создан интерфейс FollowService
    - Создана реализация FollowServiceImpl
    - Реализован метод follow (подписка) с @Transactional
    - Реализован метод unfollow (отписка) с @Transactional
    - Реализован метод getFollowers (получение списка подписчиков) с пагинацией
    - Реализован метод getFollowing (получение списка подписок) с пагинацией
    - Реализован метод getFollowStatus (проверка статуса подписки)
    - Реализован метод getFollowStats (получение статистики)
    - Все методы используют валидатор перед операциями
    - Добавлено логирование

- [ ] (P1) #13: Реализация Controller - создать FollowApi (OpenAPI) и FollowController с @LoggableRequest
  - Зависимости: #12
  - Acceptance criteria:
    - Создан интерфейс FollowApi с @Tag и OpenAPI аннотациями
    - Создан FollowController implements FollowApi
    - Реализован POST /api/v1/follows с @LoggableRequest
    - Реализован DELETE /api/v1/follows/{followerId}/{followingId} с @LoggableRequest
    - Реализован GET /api/v1/follows/{userId}/followers с @LoggableRequest
    - Реализован GET /api/v1/follows/{userId}/following с @LoggableRequest
    - Реализован GET /api/v1/follows/{followerId}/{followingId}/status с @LoggableRequest
    - Реализован GET /api/v1/follows/{userId}/stats с @LoggableRequest
    - Все методы используют @Valid для валидации
    - Все методы возвращают правильные HTTP статус-коды

- [ ] (P1) #14: Реализация Config - создать OpenApiConfig и FeignConfig
  - Зависимости: #1, #4
  - Acceptance criteria:
    - Создан OpenApiConfig с @Configuration
    - Создан Bean followerApiOpenAPI() с настройкой Info, Servers
    - Создан FeignConfig с @Configuration и @EnableFeignClients
    - Настроен basePackages для Feign клиентов

- [ ] (P1) #15: Создание application.yml - настроить порт 8084, подключение к БД, Feign клиент
  - Зависимости: #1, #4
  - Acceptance criteria:
    - Создан application.yml с server.port=8084
    - Настроено подключение к PostgreSQL
    - Настроен users-api URL для Feign клиента
    - Настроены SpringDoc OpenAPI параметры
    - Настроены management endpoints
    - Настроено логирование

- [ ] (P1) #16: Создание application-docker.yml - настроить конфигурацию для Docker (URL users-api через имя сервиса)
  - Зависимости: #1, #15
  - Acceptance criteria:
    - Создан application-docker.yml
    - Настроен users-api URL: http://users-api:8081 (через имя сервиса Docker)
    - Настроен database URL: jdbc:postgresql://postgres:5432/twitter (через имя сервиса Docker)
    - Настроен profile: docker

- [ ] (P1) #17: Создание Dockerfile - multi-stage build с Gradle и JRE, порт 8084, healthcheck
  - Зависимости: #4
  - Acceptance criteria:
    - Создан Dockerfile с multi-stage build
    - Stage 1: gradle:jdk24 для сборки
    - Stage 2: eclipse-temurin:24-jre для runtime
    - Настроен порт 8084
    - Добавлен healthcheck на /actuator/health
    - Настроены JVM опции для контейнера
    - Используется non-root user (appuser)
    - Копируется правильный JAR из build/libs

- [ ] (P1) #18: Обновление docker-compose.yml - добавить сервис follower-api с зависимостями от postgres и users-api
  - Зависимости: #17, #16
  - Acceptance criteria:
    - Добавлен сервис follower-api в docker-compose.yml
    - Настроен порт 8084:8084
    - Добавлены зависимости: postgres (service_healthy), users-api (service_healthy)
    - Настроены environment variables (SPRING_PROFILES_ACTIVE, SPRING_DATASOURCE_URL, USERS_API_URL, SPRING_JPA_HIBERNATE_DDL_AUTO)
    - Добавлен healthcheck
    - Настроены volumes для логов
    - Добавлен в network twitter-network

### Документация кода (JavaDoc)

- [ ] (P1) #19: JavaDoc для всех классов - добавить JavaDoc с @author geron, @version 1.0 для всех public классов и методов
  - Зависимости: #6, #7, #8, #9, #10, #11, #12, #13, #14
  - Acceptance criteria:
    - Все public классы имеют JavaDoc с @author geron, @version 1.0
    - Все public методы имеют JavaDoc с @param, @return, @throws
    - JavaDoc на английском языке
    - DTO Records имеют JavaDoc с @param для всех компонентов
    - Repository Derived Query Methods НЕ имеют JavaDoc (согласно стандартам)

### Тестирование

- [ ] (P1) #20: Unit тесты для Service - покрыть все методы FollowServiceImpl тестами
  - Зависимости: #12
  - Acceptance criteria:
    - Создан FollowServiceImplTest с @ExtendWith(MockitoExtension.class)
    - Протестированы все методы (follow, unfollow, getFollowers, getFollowing, getFollowStatus, getFollowStats)
    - Протестированы успешные сценарии
    - Протестированы ошибочные сценарии (валидация, пользователь не найден)
    - Используется @Nested для группировки тестов
    - Используется AssertJ для assertions
    - Проверены взаимодействия с зависимостями (verify)
    - Покрытие > 80%

- [ ] (P1) #21: Unit тесты для Validator - покрыть все методы FollowValidatorImpl тестами
  - Зависимости: #11
  - Acceptance criteria:
    - Создан FollowValidatorImplTest с @ExtendWith(MockitoExtension.class)
    - Протестированы все валидационные методы
    - Протестированы успешные сценарии
    - Протестированы ошибочные сценарии (подписка на себя, двойная подписка, пользователь не существует)
    - Используется @Nested для группировки тестов
    - Используется AssertJ для assertions
    - Проверены выбросы исключений

- [ ] (P1) #22: Unit тесты для Mapper - протестировать FollowMapper с реальным маппером
  - Зависимости: #10
  - Acceptance criteria:
    - Создан FollowMapperTest
    - Используется реальный маппер (Mappers.getMapper)
    - Протестированы все методы маппинга
    - Проверены игнорируемые поля
    - Используется AssertJ для assertions

- [ ] (P1) #23: Unit тесты для Gateway - протестировать UserGateway
  - Зависимости: #8
  - Acceptance criteria:
    - Создан UserGatewayTest с @ExtendWith(MockitoExtension.class)
    - Протестирован метод existsUser
    - Протестированы успешные сценарии
    - Протестированы ошибочные сценарии (ошибка Feign клиента)
    - Используется AssertJ для assertions

- [ ] (P2) #24: Integration тесты для Controller - покрыть все эндпоинты тестами с MockMvc, проверить все статус-коды
  - Зависимости: #13
  - Acceptance criteria:
    - Создан FollowControllerTest с @SpringBootTest, @AutoConfigureWebMvc
    - Протестированы все эндпоинты (POST, DELETE, GET)
    - Протестированы все статус-коды (200, 201, 400, 404, 409)
    - Использован MockMvc для тестирования REST endpoints
    - Использован WireMock для мокирования users-api
    - Использован @Transactional для изоляции тестов
    - Проверена валидация запросов
    - Проверен формат ответов

### Swagger/OpenAPI документация

- [ ] (P1) #25: OpenAPI interface (FollowApi.java) - добавить @Tag, @Operation, @ApiResponses, @Parameter для всех методов
  - Зависимости: #13
  - Acceptance criteria:
    - Все методы имеют @Operation с summary и description
    - Все методы имеют @ApiResponses со всеми возможными статус-кодами
    - Все методы имеют @ExampleObject для успешных и ошибочных ответов
    - Все параметры имеют @Parameter с description
    - Документация на английском языке

- [ ] (P1) #26: DTO Schema аннотации - добавить @Schema на уровне класса и полей для всех DTO
  - Зависимости: #7
  - Acceptance criteria:
    - Все DTO имеют @Schema на уровне класса (name, description, example)
    - Все поля DTO имеют @Schema (description, example, requiredMode, format, minLength, maxLength)
    - Примеры используют реалистичные UUID и данные

- [ ] (P2) #27: Обновление OpenApiConfig - настроить Info, Servers для follower-api
  - Зависимости: #14
  - Acceptance criteria:
    - OpenApiConfig содержит правильный title "Twitter Follower API"
    - OpenApiConfig содержит подробное description
    - OpenApiConfig содержит server на localhost:8084
    - Version установлена в "1.0.0"

### Обновление README

- [ ] (P2) #28: Обновление README.md - создать полную документацию на русском языке согласно STANDART_README.md
  - Зависимости: #13, #12, #11
  - Acceptance criteria:
    - Создан README.md на русском языке
    - Включены все обязательные секции (Введение, Основные возможности, Архитектура, REST API, OpenAPI/Swagger, Бизнес-логика, Слой валидации, Работа с базой данных, Интеграция, Примеры использования, Конфигурация, Запуск и развертывание, Безопасность, Тестирование)
    - Документированы все эндпоинты с примерами
    - Описана интеграция с users-api
    - Описана структура таблицы follows
    - Включены примеры curl команд

### Postman коллекции

- [ ] (P2) #29: Обновление Postman коллекции - создать коллекцию с всеми запросами, примерами ответов, переменными окружения
  - Зависимости: #13
  - Acceptance criteria:
    - Создана Postman коллекция twitter-follower-api.postman_collection.json
    - Создано окружение twitter-follower-api.postman_environment.json
    - Добавлены все запросы (lowercase с пробелами)
    - Используется переменная {{baseUrl}}
    - Используются переменные окружения для path параметров
    - Добавлены примеры ответов для всех сценариев (200, 201, 400, 404, 409)
    - Ошибки в формате RFC 7807 Problem Details
    - Правильные Content-Type (application/json, application/problem+json)

### Проверка соответствия стандартам

- [ ] (P1) #30: Проверка соответствия стандартам - проверить соответствие всем стандартам проекта
  - Зависимости: #19, #20, #21, #22, #23, #24, #25, #26, #28, #29, #17, #18
  - Acceptance criteria:
    - Проверено соответствие STANDART_CODE.md
    - Проверено соответствие STANDART_PROJECT.md
    - Проверено соответствие STANDART_TEST.md
    - Проверено соответствие STANDART_JAVADOC.md
    - Проверено соответствие STANDART_SWAGGER.md
    - Проверено соответствие STANDART_README.md
    - Проверено соответствие STANDART_POSTMAN.md
    - Сервис успешно разворачивается через docker-compose up
    - Healthcheck работает корректно
    - Интеграция с users-api через Docker network функционирует

## Предположения

1. Сервис будет работать на порту 8084
2. Сервис будет использовать ту же базу данных PostgreSQL, что и другие сервисы
3. Интеграция с users-api будет через Feign Client
4. Пользователи уже существуют в системе (создаются через users-api)
5. Сервис не требует аутентификации (как и другие сервисы в проекте)
6. Таблица follows будет создана через SQL скрипт в sql/follows.sql
7. Сервис будет разворачиваться через docker-compose.yml вместе с другими сервисами
8. Healthcheck будет доступен на /actuator/health
9. Все эндпоинты будут доступны через /api/v1/follows
10. Пагинация будет использоваться для списков подписчиков/подписок

## Риски

1. **Производительность при большом количестве подписок**: При большом количестве подписок запросы на получение списков могут быть медленными. Решение: использование пагинации и индексов в БД.
2. **Интеграция с users-api может быть недоступна**: Если users-api недоступен, валидация существования пользователей не будет работать. Решение: обработка ошибок в UserGateway, возврат понятных ошибок.
3. **Циклические зависимости**: Пользователи могут подписываться друг на друга, что не является проблемой, но нужно учесть при проектировании.

## Метрики успешности

- Все тесты проходят (unit и integration)
- Покрытие кода > 80%
- Все эндпоинты документированы в Swagger
- Postman коллекция содержит все запросы с примерами
- README содержит полную документацию на русском языке
- Код соответствует всем стандартам проекта
- Сервис успешно разворачивается через `docker-compose up`
- Healthcheck работает корректно
- Интеграция с users-api через Docker network функционирует

