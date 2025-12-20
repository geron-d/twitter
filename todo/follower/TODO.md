# TODO: Follower API Service Implementation

## Обзор
Реализация нового микросервиса follower-api для управления подписками пользователей на других пользователей в системе Twitter. Сервис будет предоставлять REST API для подписки/отписки, получения списков подписчиков и подписок, проверки статуса подписки и получения статистики.

## Задачи

### Анализ и проектирование

- [x] (P1) [2025-01-27 10:30] #1: Анализ требований и проектирование API - определить все эндпоинты, DTO структуру, Entity структуру, бизнес-правила
  - Зависимости: нет
  - Acceptance criteria:
    - Определены все эндпоинты (POST, DELETE, GET для подписок/отписок, списков, статуса, статистики)
    - Определена структура Entity Follow с уникальным ограничением
    - Определены все DTO (Request, Response, Filter)
    - Определены бизнес-правила (нельзя подписаться на себя, нельзя подписаться дважды)
    - Определена структура таблицы follows в БД
  - Выполнено: Создан документ ANALYSIS_DESIGN.md с полным проектированием API, включая все эндпоинты, DTO структуры, Entity структуру, бизнес-правила и схему БД. Документ содержит детальное описание 6 REST эндпоинтов, 7 DTO (Request, Response, Filter), структуру Entity Follow, 3 бизнес-правила и полную схему таблицы follows с индексами.

- [x] (P1) [2025-12-17 18:34] #2: Проектирование Docker конфигурации - определить структуру Dockerfile, application-docker.yml, конфигурацию в docker-compose.yml
  - Зависимости: #1
  - Acceptance criteria:
    - Определена структура Dockerfile (multi-stage build)
    - Определена конфигурация для Docker окружения (URL сервисов через имена)
    - Определена конфигурация в docker-compose.yml (зависимости, environment variables, healthcheck)
  - Выполнено: Создан документ DOCKER_DESIGN.md с полным проектированием Docker конфигурации для follower-api. Документ содержит детальное описание структуры Dockerfile (multi-stage build с gradle:jdk24 и eclipse-temurin:24-jre, порт 8084, healthcheck, non-root user), структуры application-docker.yml (URL users-api через имя сервиса Docker, профиль docker), и полной конфигурации в docker-compose.yml (зависимости от postgres и users-api, environment variables, healthcheck, volumes, network). Все проектирование соответствует паттернам существующих сервисов проекта.

### Инфраструктура

- [x] (P1) [2025-12-17 18:37] #3: Обновление settings.gradle - добавить include для follower-api
  - Зависимости: нет
  - Acceptance criteria:
    - Добавлена строка `include 'services:follower-api'` в settings.gradle
  - Выполнено: Добавлена строка `include 'services:follower-api'` в settings.gradle после других сервисов (users-api, tweet-api, admin-script-api). Модуль follower-api теперь включен в структуру проекта Gradle.

- [x] (P1) [2025-12-17 18:39] #4: Создание build.gradle для follower-api - настроить зависимости и аннотационные процессоры
  - Зависимости: #1
  - Acceptance criteria:
    - Создан build.gradle с зависимостями на shared:common-lib и shared:database
    - Настроены Spring Boot starters (web, data-jpa, validation, actuator)
    - Настроены OpenAPI/Swagger зависимости
    - Настроены Lombok и MapStruct с правильными annotation processors
    - Настроен compileJava с параметрами для MapStruct
  - Выполнено: Создан build.gradle для follower-api в services/follower-api/build.gradle. Настроены все зависимости: shared модули (common-lib, database), Spring Boot starters (web, data-jpa, validation, actuator), Spring Cloud OpenFeign для интеграции с users-api, OpenAPI/Swagger, Lombok и MapStruct с правильными annotation processors (включая lombok-mapstruct-binding), PostgreSQL driver, тестовые зависимости (включая WireMock для мокирования users-api). Настроен compileJava с параметрами для MapStruct (defaultComponentModel=spring, unmappedTargetPolicy=IGNORE). Настроен springBoot с mainClass.

- [x] (P1) [2025-12-17 19:15] #5: Создание application.yml - настроить порт 8084, подключение к БД, Feign клиент
  - Зависимости: #1, #4
  - Acceptance criteria:
    - Создан application.yml с server.port=8084
    - Настроено подключение к PostgreSQL
    - Настроен users-api URL для Feign клиента
    - Настроены SpringDoc OpenAPI параметры
    - Настроены management endpoints
    - Настроено логирование
  - Выполнено: Создан application.yml для follower-api в services/follower-api/src/main/resources/application.yml. Настроены все параметры: server.port=8084, spring.application.name=follower-api, подключение к PostgreSQL (jdbc:postgresql://localhost:5432/twitter), Feign клиент для users-api (http://localhost:8081), SpringDoc OpenAPI параметры (path, swagger-ui настройки), management endpoints (health, info, metrics, tracing), логирование (уровни DEBUG для com.twitter, паттерны для console и file). Конфигурация соответствует структуре других сервисов (tweet-api, admin-script-api) и включает пагинацию (default-page-size=10, max-page-size=100).

- [x] (P1) [2025-12-17 19:20] #6: Создание SQL скрипта - создать sql/follows.sql для таблицы follows
  - Зависимости: #1
  - Acceptance criteria:
    - Создан SQL скрипт с CREATE TABLE follows
    - Определены все поля (id, follower_id, following_id, created_at)
    - Добавлено UNIQUE ограничение на (follower_id, following_id)
    - Добавлено CHECK ограничение (follower_id != following_id)
  - Выполнено: Создан SQL скрипт sql/follows.sql для таблицы follows. Определены все поля: id (UUID PRIMARY KEY), follower_id (UUID NOT NULL), following_id (UUID NOT NULL), created_at (TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP). Добавлены FOREIGN KEY ограничения на users(id) для обоих полей (follows_follower_fk, follows_following_fk). Добавлено UNIQUE ограничение на (follower_id, following_id) (follows_unique_follower_following). Добавлено CHECK ограничение (follower_id != following_id) (follows_check_no_self_follow). Созданы индексы для оптимизации запросов: idx_follows_follower_id, idx_follows_following_id, idx_follows_created_at. Скрипт соответствует структуре из ANALYSIS_DESIGN.md и стилю существующих SQL скриптов проекта.

- [x] (P1) [2025-12-17 19:30] #7: Реализация Config - создать OpenApiConfig и FeignConfig
  - Зависимости: #1, #4
  - Acceptance criteria:
    - Создан OpenApiConfig с @Configuration
    - Создан Bean followerApiOpenAPI() с настройкой Info, Servers
    - Создан FeignConfig с @Configuration и @EnableFeignClients
    - Настроен basePackages для Feign клиентов
  - Выполнено: Создан OpenApiConfig в пакете com.twitter.config с @Configuration и @Bean методом followerApiOpenAPI(). Настроена OpenAPI спецификация с title "Twitter Follower API", подробным description (возможности API, аутентификация, rate limiting, обработка ошибок), version "1.0.0", server на localhost:8084. Создан FeignConfig в пакете com.twitter.config с @Configuration и @EnableFeignClients(basePackages = "com.twitter.client") для активации Feign клиентов в пакете com.twitter.client. Все классы содержат полную JavaDoc документацию (@author geron, @version 1.0). Конфигурация соответствует структуре других сервисов (tweet-api, admin-script-api) и стандартам проекта (STANDART_SWAGGER.md, STANDART_CODE.md). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-12-17 19:40] #8: Создание application-docker.yml - настроить конфигурацию для Docker (URL users-api через имя сервиса)
  - Зависимости: #1, #5
  - Acceptance criteria:
    - Создан application-docker.yml
    - Настроен users-api URL: http://users-api:8081 (через имя сервиса Docker)
    - Настроен database URL: jdbc:postgresql://postgres:5432/twitter (через имя сервиса Docker)
    - Настроен profile: docker
  - Выполнено: Создан application-docker.yml для follower-api в services/follower-api/src/main/resources/application-docker.yml. Настроен users-api URL: http://users-api:8081 (через имя сервиса Docker вместо localhost). Конфигурация базы данных будет передаваться через environment variables в docker-compose.yml (SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/twitter), что соответствует паттерну других сервисов. Профиль docker будет активирован через environment variable SPRING_PROFILES_ACTIVE=docker в docker-compose.yml. Конфигурация соответствует структуре других сервисов (tweet-api, admin-script-api) и проектированию из DOCKER_DESIGN.md.

- [x] (P1) [2025-12-17 19:50] #9: Создание Dockerfile - multi-stage build с Gradle и JRE, порт 8084, healthcheck
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
  - Выполнено: Создан Dockerfile для follower-api в services/follower-api/Dockerfile с multi-stage build. Stage 1 использует gradle:jdk24 для сборки приложения (копирование Gradle файлов, загрузка зависимостей, сборка через ./gradlew :services:follower-api:build). Stage 2 использует eclipse-temurin:24-jre для runtime (установка curl для healthcheck, создание non-root пользователя appuser, копирование JAR из build stage, создание директории для логов). Настроен порт 8084 (EXPOSE 8084). Добавлен healthcheck на /actuator/health с интервалом 30s, таймаутом 3s, start-period 60s, retries 3. Настроены JVM опции для контейнера (Xms512m, Xmx1024m, UseG1GC, UseContainerSupport). Dockerfile соответствует структуре других сервисов (users-api, tweet-api, admin-script-api) и проектированию из DOCKER_DESIGN.md.

- [x] (P1) [2025-12-17 20:00] #10: Обновление docker-compose.yml - добавить сервис follower-api с зависимостями от postgres и users-api
  - Зависимости: #9, #8
  - Acceptance criteria:
    - Добавлен сервис follower-api в docker-compose.yml
    - Настроен порт 8084:8084
    - Добавлены зависимости: postgres (service_healthy), users-api (service_healthy)
    - Настроены environment variables (SPRING_PROFILES_ACTIVE, SPRING_DATASOURCE_URL, USERS_API_URL, SPRING_JPA_HIBERNATE_DDL_AUTO)
    - Добавлен healthcheck
    - Настроены volumes для логов
    - Добавлен в network twitter-network
  - Выполнено: Добавлен сервис follower-api в docker-compose.yml после admin-script-api. Настроен build context (.), dockerfile (services/follower-api/Dockerfile), container_name (twitter-follower-api), порт 8084:8084. Добавлены зависимости: postgres (service_healthy), users-api (service_healthy). Настроены environment variables: SPRING_PROFILES_ACTIVE=docker, SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/twitter, SPRING_DATASOURCE_USERNAME=user, SPRING_DATASOURCE_PASSWORD=password, SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver, SPRING_JPA_HIBERNATE_DDL_AUTO=validate, SPRING_JPA_SHOW_SQL=false, LOGGING_LEVEL_COM_TWITTER=DEBUG, LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG. Добавлен healthcheck на /actuator/health (interval=30s, timeout=10s, retries=3, start_period=60s). Настроены volumes для логов (./logs:/app/logs). Добавлен в network twitter-network. Настроен restart: unless-stopped. Конфигурация соответствует структуре других сервисов и проектированию из DOCKER_DESIGN.md.

### Общие слои (используются всеми эндпоинтами)

- [ ] (P1) #11: Реализация Entity Follow - создать JPA сущность с уникальным ограничением на (follower_id, following_id)
  - Зависимости: #1, #6
  - Acceptance criteria:
    - Создана Entity Follow с полями id, followerId, followingId, createdAt
    - Использованы правильные JPA аннотации (@Entity, @Table, @Id, @Column)
    - Добавлено уникальное ограничение через @Table(uniqueConstraints)
    - Использован @CreationTimestamp для createdAt
    - Добавлена полная JavaDoc документация

- [ ] (P1) #12: Реализация Gateway для users-api - создать UsersApiClient (Feign) и UserGateway
  - Зависимости: #1, #4
  - Acceptance criteria:
    - Создан UsersApiClient с методом existsUser для проверки существования пользователя
    - Настроен Feign Client с правильным URL и path
    - Создан UserGateway с методом existsUser для обёртки вызовов
    - Добавлена обработка ошибок в UserGateway
    - Добавлено логирование

- [ ] (P1) #13: Реализация Repository - создать FollowRepository с Derived Query Methods
  - Зависимости: #11
  - Acceptance criteria:
    - Создан FollowRepository extends JpaRepository<Follow, UUID>
    - Добавлены Derived Query Methods (existsByFollowerIdAndFollowingId, findByFollowerId, findByFollowingId, countByFollowerId, countByFollowingId)
    - Repository НЕ имеет JavaDoc для Derived Query Methods (согласно стандартам)

- [ ] (P1) #14: Реализация Mapper (MapStruct) - создать интерфейс FollowMapper с маппингами
  - Зависимости: #11
  - Acceptance criteria:
    - Создан FollowMapper интерфейс с @Mapper
    - Добавлены методы маппинга (toFollow, toFollowResponseDto, toFollowerResponseDto, toFollowingResponseDto)
    - Настроены игнорируемые поля (@Mapping(target = "...", ignore = true))
    - Mapper настроен как Spring компонент

- [ ] (P1) #15: Реализация Validator - создать FollowValidator interface и implementation с проверкой бизнес-правил
  - Зависимости: #12, #13
  - Acceptance criteria:
    - Создан интерфейс FollowValidator
    - Создана реализация FollowValidatorImpl
    - Реализована валидация: нельзя подписаться на себя
    - Реализована валидация: нельзя подписаться дважды
    - Реализована валидация: оба пользователя должны существовать (через UserGateway)
    - Используются исключения из common-lib (BusinessRuleValidationException)
    - Добавлено логирование

### Эндпоинт: POST /api/v1/follows - Подписка на пользователя

- [ ] (P1) #16: POST /api/v1/follows - Реализация DTO - создать FollowRequestDto и FollowResponseDto
  - Зависимости: #1
  - Acceptance criteria:
    - Создан FollowRequestDto (followerId, followingId) с валидацией (@NotNull, @Valid UUID)
    - Создан FollowResponseDto с информацией о подписке (id, followerId, followingId, createdAt)
    - Все DTO используют Records (Java 24)
    - Все DTO имеют валидационные аннотации
    - Все DTO имеют @Schema аннотации для Swagger

- [ ] (P1) #17: POST /api/v1/follows - Реализация Service метода - создать метод follow в FollowService
  - Зависимости: #11, #13, #14, #15, #16
  - Acceptance criteria:
    - Добавлен метод follow(FollowRequestDto) в интерфейс FollowService
    - Реализован метод follow в FollowServiceImpl с @Transactional
    - Метод использует валидатор перед операцией
    - Метод сохраняет подписку через Repository
    - Метод использует Mapper для преобразования
    - Добавлено логирование

- [ ] (P1) #18: POST /api/v1/follows - Реализация Controller метода - создать метод createFollow в FollowController
  - Зависимости: #17
  - Acceptance criteria:
    - Добавлен метод createFollow в интерфейс FollowApi с OpenAPI аннотациями
    - Реализован метод createFollow в FollowController с @LoggableRequest
    - Метод использует @Valid для валидации
    - Метод возвращает HttpStatus.CREATED (201)
    - Метод возвращает FollowResponseDto

- [ ] (P1) #19: POST /api/v1/follows - Unit тесты для Service метода - протестировать метод follow
  - Зависимости: #17
  - Acceptance criteria:
    - Создан тест для метода follow в FollowServiceImplTest
    - Протестирован успешный сценарий
    - Протестированы ошибочные сценарии (валидация, пользователь не найден, двойная подписка)
    - Используется @Nested для группировки тестов
    - Используется AssertJ для assertions
    - Проверены взаимодействия с зависимостями (verify)

- [ ] (P2) #20: POST /api/v1/follows - Integration тесты - протестировать эндпоинт POST /api/v1/follows
  - Зависимости: #18
  - Acceptance criteria:
    - Создан тест для POST /api/v1/follows в FollowControllerTest
    - Протестирован успешный сценарий (201 Created)
    - Протестированы ошибочные сценарии (400 Bad Request, 404 Not Found, 409 Conflict)
    - Использован MockMvc для тестирования REST endpoint
    - Использован WireMock для мокирования users-api
    - Использован @Transactional для изоляции тестов
    - Проверена валидация запросов
    - Проверен формат ответов

- [ ] (P1) #21: POST /api/v1/follows - OpenAPI документация - добавить @Operation, @ApiResponses для метода createFollow
  - Зависимости: #18
  - Acceptance criteria:
    - Метод createFollow имеет @Operation с summary и description
    - Метод createFollow имеет @ApiResponses со всеми возможными статус-кодами (201, 400, 404, 409)
    - Метод createFollow имеет @ExampleObject для успешных и ошибочных ответов
    - Параметры имеют @Parameter с description
    - Документация на английском языке

### Эндпоинт: DELETE /api/v1/follows/{followerId}/{followingId} - Отписка от пользователя

- [ ] (P1) #22: DELETE /api/v1/follows/{followerId}/{followingId} - Реализация Service метода - создать метод unfollow в FollowService
  - Зависимости: #11, #13, #15
  - Acceptance criteria:
    - Добавлен метод unfollow(UUID followerId, UUID followingId) в интерфейс FollowService
    - Реализован метод unfollow в FollowServiceImpl с @Transactional
    - Метод проверяет существование подписки
    - Метод удаляет подписку через Repository
    - Добавлено логирование

- [ ] (P1) #23: DELETE /api/v1/follows/{followerId}/{followingId} - Реализация Controller метода - создать метод deleteFollow в FollowController
  - Зависимости: #22
  - Acceptance criteria:
    - Добавлен метод deleteFollow в интерфейс FollowApi с OpenAPI аннотациями
    - Реализован метод deleteFollow в FollowController с @LoggableRequest
    - Метод использует @PathVariable для параметров
    - Метод возвращает HttpStatus.NO_CONTENT (204) при успехе
    - Метод возвращает HttpStatus.NOT_FOUND (404) если подписка не найдена

- [ ] (P1) #24: DELETE /api/v1/follows/{followerId}/{followingId} - Unit тесты для Service метода - протестировать метод unfollow
  - Зависимости: #22
  - Acceptance criteria:
    - Создан тест для метода unfollow в FollowServiceImplTest
    - Протестирован успешный сценарий
    - Протестирован ошибочный сценарий (подписка не найдена)
    - Используется @Nested для группировки тестов
    - Используется AssertJ для assertions
    - Проверены взаимодействия с зависимостями (verify)

- [ ] (P2) #25: DELETE /api/v1/follows/{followerId}/{followingId} - Integration тесты - протестировать эндпоинт DELETE /api/v1/follows/{followerId}/{followingId}
  - Зависимости: #23
  - Acceptance criteria:
    - Создан тест для DELETE /api/v1/follows/{followerId}/{followingId} в FollowControllerTest
    - Протестирован успешный сценарий (204 No Content)
    - Протестирован ошибочный сценарий (404 Not Found)
    - Использован MockMvc для тестирования REST endpoint
    - Использован @Transactional для изоляции тестов
    - Проверен формат ответов

- [ ] (P1) #26: DELETE /api/v1/follows/{followerId}/{followingId} - OpenAPI документация - добавить @Operation, @ApiResponses для метода deleteFollow
  - Зависимости: #23
  - Acceptance criteria:
    - Метод deleteFollow имеет @Operation с summary и description
    - Метод deleteFollow имеет @ApiResponses со всеми возможными статус-кодами (204, 404)
    - Параметры имеют @Parameter с description
    - Документация на английском языке

### Эндпоинт: GET /api/v1/follows/{userId}/followers - Получение списка подписчиков

- [ ] (P1) #27: GET /api/v1/follows/{userId}/followers - Реализация DTO - создать FollowerResponseDto и FollowerFilter
  - Зависимости: #1
  - Acceptance criteria:
    - Создан FollowerResponseDto (id, login, createdAt) для списка подписчиков
    - Создан FollowerFilter для фильтрации подписчиков
    - Все DTO используют Records (Java 24)
    - Все DTO имеют @Schema аннотации для Swagger

- [ ] (P1) #28: GET /api/v1/follows/{userId}/followers - Реализация Service метода - создать метод getFollowers в FollowService
  - Зависимости: #11, #13, #14, #27
  - Acceptance criteria:
    - Добавлен метод getFollowers(UUID userId, FollowerFilter filter, Pageable pageable) в интерфейс FollowService
    - Реализован метод getFollowers в FollowServiceImpl
    - Метод использует пагинацию
    - Метод возвращает PagedModel<FollowerResponseDto>
    - Метод использует Mapper для преобразования
    - Добавлено логирование

- [ ] (P1) #29: GET /api/v1/follows/{userId}/followers - Реализация Controller метода - создать метод getFollowers в FollowController
  - Зависимости: #28
  - Acceptance criteria:
    - Добавлен метод getFollowers в интерфейс FollowApi с OpenAPI аннотациями
    - Реализован метод getFollowers в FollowController с @LoggableRequest
    - Метод использует @PathVariable для userId
    - Метод использует @PageableDefault для пагинации
    - Метод возвращает HttpStatus.OK (200)
    - Метод возвращает PagedModel<FollowerResponseDto>

- [ ] (P1) #30: GET /api/v1/follows/{userId}/followers - Unit тесты для Service метода - протестировать метод getFollowers
  - Зависимости: #28
  - Acceptance criteria:
    - Создан тест для метода getFollowers в FollowServiceImplTest
    - Протестирован успешный сценарий с пагинацией
    - Протестирован сценарий с фильтрацией
    - Используется @Nested для группировки тестов
    - Используется AssertJ для assertions
    - Проверены взаимодействия с зависимостями (verify)

- [ ] (P2) #31: GET /api/v1/follows/{userId}/followers - Integration тесты - протестировать эндпоинт GET /api/v1/follows/{userId}/followers
  - Зависимости: #29
  - Acceptance criteria:
    - Создан тест для GET /api/v1/follows/{userId}/followers в FollowControllerTest
    - Протестирован успешный сценарий (200 OK) с пагинацией
    - Протестирован ошибочный сценарий (404 Not Found)
    - Использован MockMvc для тестирования REST endpoint
    - Использован @Transactional для изоляции тестов
    - Проверен формат ответов (PagedModel)

- [ ] (P1) #32: GET /api/v1/follows/{userId}/followers - OpenAPI документация - добавить @Operation, @ApiResponses для метода getFollowers
  - Зависимости: #29
  - Acceptance criteria:
    - Метод getFollowers имеет @Operation с summary и description
    - Метод getFollowers имеет @ApiResponses со всеми возможными статус-кодами (200, 404)
    - Параметры имеют @Parameter с description
    - Документация на английском языке

### Эндпоинт: GET /api/v1/follows/{userId}/following - Получение списка подписок

- [ ] (P1) #33: GET /api/v1/follows/{userId}/following - Реализация DTO - создать FollowingResponseDto и FollowingFilter
  - Зависимости: #1
  - Acceptance criteria:
    - Создан FollowingResponseDto (id, login, createdAt) для списка подписок
    - Создан FollowingFilter для фильтрации подписок
    - Все DTO используют Records (Java 24)
    - Все DTO имеют @Schema аннотации для Swagger

- [ ] (P1) #34: GET /api/v1/follows/{userId}/following - Реализация Service метода - создать метод getFollowing в FollowService
  - Зависимости: #11, #13, #14, #33
  - Acceptance criteria:
    - Добавлен метод getFollowing(UUID userId, FollowingFilter filter, Pageable pageable) в интерфейс FollowService
    - Реализован метод getFollowing в FollowServiceImpl
    - Метод использует пагинацию
    - Метод возвращает PagedModel<FollowingResponseDto>
    - Метод использует Mapper для преобразования
    - Добавлено логирование

- [ ] (P1) #35: GET /api/v1/follows/{userId}/following - Реализация Controller метода - создать метод getFollowing в FollowController
  - Зависимости: #34
  - Acceptance criteria:
    - Добавлен метод getFollowing в интерфейс FollowApi с OpenAPI аннотациями
    - Реализован метод getFollowing в FollowController с @LoggableRequest
    - Метод использует @PathVariable для userId
    - Метод использует @PageableDefault для пагинации
    - Метод возвращает HttpStatus.OK (200)
    - Метод возвращает PagedModel<FollowingResponseDto>

- [ ] (P1) #36: GET /api/v1/follows/{userId}/following - Unit тесты для Service метода - протестировать метод getFollowing
  - Зависимости: #34
  - Acceptance criteria:
    - Создан тест для метода getFollowing в FollowServiceImplTest
    - Протестирован успешный сценарий с пагинацией
    - Протестирован сценарий с фильтрацией
    - Используется @Nested для группировки тестов
    - Используется AssertJ для assertions
    - Проверены взаимодействия с зависимостями (verify)

- [ ] (P2) #37: GET /api/v1/follows/{userId}/following - Integration тесты - протестировать эндпоинт GET /api/v1/follows/{userId}/following
  - Зависимости: #35
  - Acceptance criteria:
    - Создан тест для GET /api/v1/follows/{userId}/following в FollowControllerTest
    - Протестирован успешный сценарий (200 OK) с пагинацией
    - Протестирован ошибочный сценарий (404 Not Found)
    - Использован MockMvc для тестирования REST endpoint
    - Использован @Transactional для изоляции тестов
    - Проверен формат ответов (PagedModel)

- [ ] (P1) #38: GET /api/v1/follows/{userId}/following - OpenAPI документация - добавить @Operation, @ApiResponses для метода getFollowing
  - Зависимости: #35
  - Acceptance criteria:
    - Метод getFollowing имеет @Operation с summary и description
    - Метод getFollowing имеет @ApiResponses со всеми возможными статус-кодами (200, 404)
    - Параметры имеют @Parameter с description
    - Документация на английском языке

### Эндпоинт: GET /api/v1/follows/{followerId}/{followingId}/status - Проверка статуса подписки

- [ ] (P1) #39: GET /api/v1/follows/{followerId}/{followingId}/status - Реализация DTO - создать FollowStatusResponseDto
  - Зависимости: #1
  - Acceptance criteria:
    - Создан FollowStatusResponseDto (isFollowing, createdAt)
    - DTO использует Records (Java 24)
    - DTO имеет @Schema аннотации для Swagger

- [ ] (P1) #40: GET /api/v1/follows/{followerId}/{followingId}/status - Реализация Service метода - создать метод getFollowStatus в FollowService
  - Зависимости: #11, #13, #14, #39
  - Acceptance criteria:
    - Добавлен метод getFollowStatus(UUID followerId, UUID followingId) в интерфейс FollowService
    - Реализован метод getFollowStatus в FollowServiceImpl
    - Метод проверяет существование подписки
    - Метод возвращает FollowStatusResponseDto
    - Метод использует Mapper для преобразования
    - Добавлено логирование

- [ ] (P1) #41: GET /api/v1/follows/{followerId}/{followingId}/status - Реализация Controller метода - создать метод getFollowStatus в FollowController
  - Зависимости: #40
  - Acceptance criteria:
    - Добавлен метод getFollowStatus в интерфейс FollowApi с OpenAPI аннотациями
    - Реализован метод getFollowStatus в FollowController с @LoggableRequest
    - Метод использует @PathVariable для параметров
    - Метод возвращает HttpStatus.OK (200)
    - Метод возвращает FollowStatusResponseDto

- [ ] (P1) #42: GET /api/v1/follows/{followerId}/{followingId}/status - Unit тесты для Service метода - протестировать метод getFollowStatus
  - Зависимости: #40
  - Acceptance criteria:
    - Создан тест для метода getFollowStatus в FollowServiceImplTest
    - Протестирован успешный сценарий (подписка существует)
    - Протестирован сценарий (подписка не существует)
    - Используется @Nested для группировки тестов
    - Используется AssertJ для assertions
    - Проверены взаимодействия с зависимостями (verify)

- [ ] (P2) #43: GET /api/v1/follows/{followerId}/{followingId}/status - Integration тесты - протестировать эндпоинт GET /api/v1/follows/{followerId}/{followingId}/status
  - Зависимости: #41
  - Acceptance criteria:
    - Создан тест для GET /api/v1/follows/{followerId}/{followingId}/status в FollowControllerTest
    - Протестирован успешный сценарий (200 OK)
    - Использован MockMvc для тестирования REST endpoint
    - Использован @Transactional для изоляции тестов
    - Проверен формат ответов

- [ ] (P1) #44: GET /api/v1/follows/{followerId}/{followingId}/status - OpenAPI документация - добавить @Operation, @ApiResponses для метода getFollowStatus
  - Зависимости: #41
  - Acceptance criteria:
    - Метод getFollowStatus имеет @Operation с summary и description
    - Метод getFollowStatus имеет @ApiResponses со всеми возможными статус-кодами (200)
    - Параметры имеют @Parameter с description
    - Документация на английском языке

### Эндпоинт: GET /api/v1/follows/{userId}/stats - Получение статистики подписок

- [ ] (P1) #45: GET /api/v1/follows/{userId}/stats - Реализация DTO - создать FollowStatsResponseDto
  - Зависимости: #1
  - Acceptance criteria:
    - Создан FollowStatsResponseDto (followersCount, followingCount)
    - DTO использует Records (Java 24)
    - DTO имеет @Schema аннотации для Swagger

- [ ] (P1) #46: GET /api/v1/follows/{userId}/stats - Реализация Service метода - создать метод getFollowStats в FollowService
  - Зависимости: #11, #13, #14, #45
  - Acceptance criteria:
    - Добавлен метод getFollowStats(UUID userId) в интерфейс FollowService
    - Реализован метод getFollowStats в FollowServiceImpl
    - Метод использует Repository для подсчета
    - Метод возвращает FollowStatsResponseDto
    - Метод использует Mapper для преобразования
    - Добавлено логирование

- [ ] (P1) #47: GET /api/v1/follows/{userId}/stats - Реализация Controller метода - создать метод getFollowStats в FollowController
  - Зависимости: #46
  - Acceptance criteria:
    - Добавлен метод getFollowStats в интерфейс FollowApi с OpenAPI аннотациями
    - Реализован метод getFollowStats в FollowController с @LoggableRequest
    - Метод использует @PathVariable для userId
    - Метод возвращает HttpStatus.OK (200)
    - Метод возвращает FollowStatsResponseDto

- [ ] (P1) #48: GET /api/v1/follows/{userId}/stats - Unit тесты для Service метода - протестировать метод getFollowStats
  - Зависимости: #46
  - Acceptance criteria:
    - Создан тест для метода getFollowStats в FollowServiceImplTest
    - Протестирован успешный сценарий
    - Используется @Nested для группировки тестов
    - Используется AssertJ для assertions
    - Проверены взаимодействия с зависимостями (verify)

- [ ] (P2) #49: GET /api/v1/follows/{userId}/stats - Integration тесты - протестировать эндпоинт GET /api/v1/follows/{userId}/stats
  - Зависимости: #47
  - Acceptance criteria:
    - Создан тест для GET /api/v1/follows/{userId}/stats в FollowControllerTest
    - Протестирован успешный сценарий (200 OK)
    - Использован MockMvc для тестирования REST endpoint
    - Использован @Transactional для изоляции тестов
    - Проверен формат ответов

- [ ] (P1) #50: GET /api/v1/follows/{userId}/stats - OpenAPI документация - добавить @Operation, @ApiResponses для метода getFollowStats
  - Зависимости: #47
  - Acceptance criteria:
    - Метод getFollowStats имеет @Operation с summary и description
    - Метод getFollowStats имеет @ApiResponses со всеми возможными статус-кодами (200)
    - Параметры имеют @Parameter с description
    - Документация на английском языке

### Дополнительные тесты и документация

- [ ] (P1) #51: Unit тесты для Gateway - протестировать UserGateway
  - Зависимости: #12
  - Acceptance criteria:
    - Создан UserGatewayTest с @ExtendWith(MockitoExtension.class)
    - Протестирован метод existsUser
    - Протестированы успешные сценарии
    - Протестированы ошибочные сценарии (ошибка Feign клиента)
    - Используется AssertJ для assertions

- [ ] (P1) #52: Unit тесты для Mapper - протестировать FollowMapper с реальным маппером
  - Зависимости: #14
  - Acceptance criteria:
    - Создан FollowMapperTest
    - Используется реальный маппер (Mappers.getMapper)
    - Протестированы все методы маппинга
    - Проверены игнорируемые поля
    - Используется AssertJ для assertions

- [ ] (P1) #53: Unit тесты для Validator - протестировать FollowValidatorImpl
  - Зависимости: #15
  - Acceptance criteria:
    - Создан FollowValidatorImplTest с @ExtendWith(MockitoExtension.class)
    - Протестированы все валидационные методы
    - Протестированы успешные сценарии
    - Протестированы ошибочные сценарии (подписка на себя, двойная подписка, пользователь не существует)
    - Используется @Nested для группировки тестов
    - Используется AssertJ для assertions
    - Проверены выбросы исключений

- [ ] (P1) #54: JavaDoc для всех классов - добавить JavaDoc с @author geron, @version 1.0 для всех public классов и методов
  - Зависимости: #11, #12, #13, #14, #15, #16, #17, #18, #22, #23, #27, #28, #29, #33, #34, #35, #39, #40, #41, #45, #46, #47, #7
  - Acceptance criteria:
    - Все public классы имеют JavaDoc с @author geron, @version 1.0
    - Все public методы имеют JavaDoc с @param, @return, @throws
    - JavaDoc на английском языке
    - DTO Records имеют JavaDoc с @param для всех компонентов
    - Repository Derived Query Methods НЕ имеют JavaDoc (согласно стандартам)

- [ ] (P1) #55: DTO Schema аннотации - добавить @Schema на уровне класса и полей для всех DTO
  - Зависимости: #16, #27, #33, #39, #45
  - Acceptance criteria:
    - Все DTO имеют @Schema на уровне класса (name, description, example)
    - Все поля DTO имеют @Schema (description, example, requiredMode, format, minLength, maxLength)
    - Примеры используют реалистичные UUID и данные

- [ ] (P2) #56: Обновление OpenApiConfig - настроить Info, Servers для follower-api
  - Зависимости: #7
  - Acceptance criteria:
    - OpenApiConfig содержит правильный title "Twitter Follower API"
    - OpenApiConfig содержит подробное description
    - OpenApiConfig содержит server на localhost:8084
    - Version установлена в "1.0.0"

- [ ] (P2) #57: Обновление README.md - создать полную документацию на русском языке согласно STANDART_README.md
  - Зависимости: #18, #23, #29, #35, #41, #47
  - Acceptance criteria:
    - Создан README.md на русском языке
    - Включены все обязательные секции (Введение, Основные возможности, Архитектура, REST API, OpenAPI/Swagger, Бизнес-логика, Слой валидации, Работа с базой данных, Интеграция, Примеры использования, Конфигурация, Запуск и развертывание, Безопасность, Тестирование)
    - Документированы все эндпоинты с примерами
    - Описана интеграция с users-api
    - Описана структура таблицы follows
    - Включены примеры curl команд

- [ ] (P2) #58: Обновление Postman коллекции - создать коллекцию с всеми запросами, примерами ответов, переменными окружения
  - Зависимости: #18, #23, #29, #35, #41, #47
  - Acceptance criteria:
    - Создана Postman коллекция twitter-follower-api.postman_collection.json
    - Создано окружение twitter-follower-api.postman_environment.json
    - Добавлены все запросы (lowercase с пробелами)
    - Используется переменная {{baseUrl}}
    - Используются переменные окружения для path параметров
    - Добавлены примеры ответов для всех сценариев (200, 201, 204, 400, 404, 409)
    - Ошибки в формате RFC 7807 Problem Details
    - Правильные Content-Type (application/json, application/problem+json)

- [ ] (P1) #59: Проверка соответствия стандартам - проверить соответствие всем стандартам проекта
  - Зависимости: #54, #19, #20, #24, #25, #30, #31, #36, #37, #42, #43, #48, #49, #51, #52, #53, #55, #56, #57, #58, #9, #10
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
