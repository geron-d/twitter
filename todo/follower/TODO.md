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
  - Выполнено: Создан OpenApiConfig в пакете com.twitter.config с @Configuration и @Bean методом followerApiOpenAPI(). Настроена OpenAPI спецификация с title "Twitter Follower API", подробным description (возможности API, аутентификация, rate limiting, обработка ошибок), version "1.0.0", server на localhost:8084. Создан FeignConfig в пакете com.twitter.config с @Configuration и @EnableFeignClients(basePackages = "com.twitter.client") для активации Feign клиентов в пакете com.twitter.client. Все классы содержат полную JavaDoc документацию (@author geron, @version 1.0). Конфигурация соответствует структуре других сервисов (tweet-api, admin-script-api) и стандартам проекта (STANDART_SWAGGER.md, STANDART_CODE.md). Проверка линтера: ошибок не обнаружено.``

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

- [x] (P1) [2025-12-17 20:10] #11: Реализация Entity Follow - создать JPA сущность с уникальным ограничением на (follower_id, following_id)
  - Зависимости: #1, #6
  - Acceptance criteria:
    - Создана Entity Follow с полями id, followerId, followingId, createdAt
    - Использованы правильные JPA аннотации (@Entity, @Table, @Id, @Column)
    - Добавлено уникальное ограничение через @Table(uniqueConstraints)
    - Использован @CreationTimestamp для createdAt
    - Добавлена полная JavaDoc документация
  - Выполнено: Создана Entity Follow в пакете com.twitter.entity. Использованы все необходимые JPA аннотации (@Entity, @Table с uniqueConstraints для (follower_id, following_id), @Id с @GeneratedValue(strategy = GenerationType.UUID), @Column для всех полей). Добавлен @CreationTimestamp для createdAt. Использованы Lombok аннотации (@Data, @Accessors(chain = true), @NoArgsConstructor, @AllArgsConstructor). Добавлена полная JavaDoc документация с @author geron, @version 1.0, включая описание всех полей и бизнес-правил. Entity соответствует стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Entity (User, Tweet). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-12-17 20:15] #12: Реализация Gateway для users-api - создать UsersApiClient (Feign) и UserGateway
  - Зависимости: #1, #4
  - Acceptance criteria:
    - Создан UsersApiClient с методом existsUser для проверки существования пользователя
    - Настроен Feign Client с правильным URL и path
    - Создан UserGateway с методом existsUser для обёртки вызовов
    - Добавлена обработка ошибок в UserGateway
    - Добавлено логирование
  - Выполнено: Создан UsersApiClient в пакете com.twitter.client с @FeignClient аннотацией (name="users-api", url="${app.users-api.base-url:http://localhost:8081}", path="/api/v1/users"). Метод existsUser вызывает GET /api/v1/users/{userId}/exists и возвращает UserExistsResponseDto из common-lib. Создан UserGateway в пакете com.twitter.gateway с @Component, @RequiredArgsConstructor, @Slf4j. Метод existsUser оборачивает вызов Feign клиента, обрабатывает null userId (возвращает false с предупреждением), обрабатывает исключения (возвращает false с debug логированием), логирует успешные проверки. Оба класса содержат полную JavaDoc документацию с @author geron, @version 1.0. Классы соответствуют стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Gateway/Feign клиентов (tweet-api, admin-script-api). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-12-17 20:20] #13: Реализация Repository - создать FollowRepository с Derived Query Methods
  - Зависимости: #11
  - Acceptance criteria:
    - Создан FollowRepository extends JpaRepository<Follow, UUID>
    - Добавлены Derived Query Methods (existsByFollowerIdAndFollowingId, findByFollowerId, findByFollowingId, countByFollowerId, countByFollowingId)
    - Repository НЕ имеет JavaDoc для Derived Query Methods (согласно стандартам)
  - Выполнено: Создан FollowRepository в пакете com.twitter.repository, расширяет JpaRepository<Follow, UUID>. Добавлены все необходимые Derived Query Methods: existsByFollowerIdAndFollowingId (проверка существования подписки), findByFollowerId и findByFollowingId с Pageable (для пагинации списков подписок и подписчиков), countByFollowerId и countByFollowingId (для подсчета статистики), findByFollowerIdAndFollowingId возвращает Optional<Follow> (для поиска конкретной подписки). Все методы являются Derived Query Methods и НЕ имеют JavaDoc документации (согласно стандартам проекта STANDART_JAVADOC.md). Repository имеет JavaDoc только на уровне класса с @author geron, @version 1.0. Соответствует структуре других Repository (UserRepository, TweetRepository). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-12-17 20:25] #14: Реализация Mapper (MapStruct) - создать интерфейс FollowMapper с маппингами
  - Зависимости: #11
  - Acceptance criteria:
    - Создан FollowMapper интерфейс с @Mapper
    - Добавлены методы маппинга (toFollow, toFollowResponseDto, toFollowerResponseDto, toFollowingResponseDto)
    - Настроены игнорируемые поля (@Mapping(target = "...", ignore = true))
    - Mapper настроен как Spring компонент
  - Выполнено: Создан FollowMapper в пакете com.twitter.mapper с @Mapper аннотацией (настроен как Spring компонент через defaultComponentModel=spring в build.gradle). Добавлены методы маппинга: toFollow(FollowRequestDto) с игнорированием id и createdAt (service-managed поля), toFollowResponseDto(Follow) для преобразования Entity в Response DTO, toFollowerResponseDto(Follow, String login) для списка подписчиков (login получается через users-api), toFollowingResponseDto(Follow, String login) для списка подписок (login получается через users-api). Все методы используют @Mapping для настройки маппинга полей. Mapper имеет полную JavaDoc документацию с @author geron, @version 1.0. Соответствует стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Mapper (UserMapper, TweetMapper). Проверка линтера: ошибок не обнаружено. Примечание: DTO классы будут созданы в последующих шагах (#16, #27, #33).

- [x] (P1) [2025-12-17 20:30] #15: Реализация Validator - создать FollowValidator interface и implementation с проверкой бизнес-правил
  - Зависимости: #12, #13
  - Acceptance criteria:
    - Создан интерфейс FollowValidator
    - Создана реализация FollowValidatorImpl
    - Реализована валидация: нельзя подписаться на себя
    - Реализована валидация: нельзя подписаться дважды
    - Реализована валидация: оба пользователя должны существовать (через UserGateway)
    - Используются исключения из common-lib (BusinessRuleValidationException)
    - Добавлено логирование
  - Выполнено: Создан интерфейс FollowValidator в пакете com.twitter.validation с методом validateForFollow(FollowRequestDto). Создана реализация FollowValidatorImpl с @Component, @RequiredArgsConstructor, @Slf4j. Реализованы все три валидации: validateNoSelfFollow (проверка, что followerId != followingId, выбрасывает BusinessRuleValidationException с правилом "SELF_FOLLOW_NOT_ALLOWED"), validateUsersExist (проверка существования обоих пользователей через UserGateway, выбрасывает BusinessRuleValidationException с правилами "FOLLOWER_NOT_EXISTS" и "FOLLOWING_NOT_EXISTS"), validateUniqueness (проверка через FollowRepository.existsByFollowerIdAndFollowingId, выбрасывает UniquenessValidationException). Все методы имеют логирование (warn для ошибок). Оба класса содержат полную JavaDoc документацию с @author geron, @version 1.0. Соответствует стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Validator (UserValidator, TweetValidator). Проверка линтера: ошибок не обнаружено. Примечание: FollowRequestDto будет создан в шаге #16.

### Эндпоинт: POST /api/v1/follows - Подписка на пользователя

- [x] (P1) [2025-12-17 20:35] #16: POST /api/v1/follows - Реализация DTO - создать FollowRequestDto и FollowResponseDto
  - Зависимости: #1
  - Acceptance criteria:
    - Создан FollowRequestDto (followerId, followingId) с валидацией (@NotNull, @Valid UUID)
    - Создан FollowResponseDto с информацией о подписке (id, followerId, followingId, createdAt)
    - Все DTO используют Records (Java 24)
    - Все DTO имеют валидационные аннотации
    - Все DTO имеют @Schema аннотации для Swagger
  - Выполнено: Создан FollowRequestDto в пакете com.twitter.dto.request с полями followerId и followingId (оба UUID, @NotNull). Добавлены @Schema аннотации на уровне класса (name="FollowRequest", description, example JSON) и на уровне полей (description, example UUID, format="uuid", requiredMode=REQUIRED). Использован @Builder для удобства создания. Создан FollowResponseDto в пакете com.twitter.dto.response с полями id, followerId, followingId, createdAt (LocalDateTime). Добавлены @Schema аннотации на уровне класса (name="FollowResponse", description, example JSON) и на уровне полей (description, example, format). Использован @JsonFormat для createdAt (pattern="yyyy-MM-dd'T'HH:mm:ss'Z'", timezone="UTC"). Использован @Builder. Оба DTO используют Records (Java 24), имеют полную JavaDoc документацию с @param для всех компонентов, @author geron, @version 1.0. Соответствуют стандартам проекта (STANDART_CODE.md, STANDART_SWAGGER.md, STANDART_JAVADOC.md) и структуре других DTO (CreateTweetRequestDto, TweetResponseDto). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-12-17 20:40] #17: POST /api/v1/follows - Реализация Service метода - создать метод follow в FollowService
  - Зависимости: #11, #13, #14, #15, #16
  - Acceptance criteria:
    - Добавлен метод follow(FollowRequestDto) в интерфейс FollowService
    - Реализован метод follow в FollowServiceImpl с @Transactional
    - Метод использует валидатор перед операцией
    - Метод сохраняет подписку через Repository
    - Метод использует Mapper для преобразования
    - Добавлено логирование
  - Выполнено: Создан интерфейс FollowService в пакете com.twitter.service с методом follow(FollowRequestDto), возвращающим FollowResponseDto. Метод имеет полную JavaDoc документацию с описанием операций, @param, @return, @throws. Создана реализация FollowServiceImpl с @Service, @RequiredArgsConstructor, @Slf4j. Зависимости: FollowRepository, FollowMapper, FollowValidator. Метод follow реализован с @Transactional для обеспечения транзакционности. Метод использует валидатор (followValidator.validateForFollow) перед операцией, использует маппер для преобразования DTO в Entity (followMapper.toFollow), сохраняет через Repository (followRepository.saveAndFlush), использует маппер для преобразования Entity в Response DTO (followMapper.toFollowResponseDto). Добавлено логирование: debug перед операцией, info после успешного создания. Метод имеет JavaDoc с @see для ссылки на интерфейс. Соответствует стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Service (TweetService, UserService). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-12-17 20:45] #18: POST /api/v1/follows - Реализация Controller метода - создать метод createFollow в FollowController
  - Зависимости: #17
  - Acceptance criteria:
    - Добавлен метод createFollow в интерфейс FollowApi с OpenAPI аннотациями
    - Реализован метод createFollow в FollowController с @LoggableRequest
    - Метод использует @Valid для валидации
    - Метод возвращает HttpStatus.CREATED (201)
    - Метод возвращает FollowResponseDto
  - Выполнено: Создан интерфейс FollowApi в пакете com.twitter.controller с @Tag(name="Follow Management", description="API for managing follow relationships in the Twitter system"). Метод createFollow имеет @Operation с summary и description, @ApiResponses со всеми возможными статус-кодами (201 Created, 400 Bad Request для валидации, 409 Conflict для бизнес-правил и уникальности), @ExampleObject для успешных и ошибочных ответов в формате RFC 7807 Problem Details. @Parameter для request с description. Создан FollowController в пакете com.twitter.controller с @RestController, @RequestMapping("/api/v1/follows"), @RequiredArgsConstructor, @Slf4j. Метод createFollow реализован с @LoggableRequest, @PostMapping, @RequestBody @Valid FollowRequestDto, возвращает ResponseEntity.status(HttpStatus.CREATED).body(createdFollow). Метод имеет JavaDoc с @see для ссылки на интерфейс. Соответствует стандартам проекта (STANDART_CODE.md, STANDART_SWAGGER.md, STANDART_JAVADOC.md) и структуре других Controller (TweetController, UserController). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27 20:50] #19: POST /api/v1/follows - Unit тесты для Service метода - протестировать метод follow
  - Зависимости: #17
  - Acceptance criteria:
    - Создан тест для метода follow в FollowServiceImplTest
    - Протестирован успешный сценарий
    - Протестированы ошибочные сценарии (валидация, пользователь не найден, двойная подписка)
    - Используется @Nested для группировки тестов
    - Используется AssertJ для assertions
    - Проверены взаимодействия с зависимостями (verify)
  - Выполнено: Создан FollowServiceImplTest в services/follower-api/src/test/java/com/twitter/service/FollowServiceImplTest.java. Реализован @Nested класс FollowTests с тестами: follow_WithValidData_ShouldReturnFollowResponseDto (успешный сценарий), follow_WithValidData_ShouldCallEachDependencyExactlyOnce (проверка взаимодействий), follow_WhenSelfFollow_ShouldThrowBusinessRuleValidationException (валидация самоподписки), follow_WhenFollowerNotFound_ShouldThrowBusinessRuleValidationException (пользователь не найден), follow_WhenFollowingNotFound_ShouldThrowBusinessRuleValidationException (пользователь не найден), follow_WhenFollowAlreadyExists_ShouldThrowUniquenessValidationException (двойная подписка). Все тесты используют @ExtendWith(MockitoExtension.class), AssertJ для assertions, проверяют взаимодействия с зависимостями через verify. Тесты соответствуют стандартам проекта (STANDART_TEST.md) и структуре других Service тестов (TweetServiceImplTest). Проверка линтера: ошибок не обнаружено.

- [x] (P2) [2025-01-27 21:00] #20: POST /api/v1/follows - Integration тесты - протестировать эндпоинт POST /api/v1/follows
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
  - Выполнено: Создан BaseIntegrationTest в services/follower-api/src/test/java/com/twitter/testconfig/BaseIntegrationTest.java с настройкой PostgreSQL Testcontainers и WireMock сервера для мокирования users-api. Создан application-test.yml для тестового профиля. Создан FollowControllerTest в services/follower-api/src/test/java/com/twitter/controller/FollowControllerTest.java с полными integration тестами для эндпоинта POST /api/v1/follows: успешный сценарий (201 Created) с проверкой создания подписки в БД, валидация Bean Validation (400 Bad Request) для null полей (followerId, followingId), неверный формат JSON (400 Bad Request), отсутствие body (400 Bad Request), бизнес-валидация (409 Conflict) для самоподписки (SELF_FOLLOW_NOT_ALLOWED), пользователь не найден - follower (FOLLOWER_NOT_EXISTS), пользователь не найден - following (FOLLOWING_NOT_EXISTS), двойная подписка (409 Conflict), обработка ошибок users-api (500 Internal Server Error) с graceful handling. Все тесты используют @SpringBootTest, @AutoConfigureWebMvc, @ActiveProfiles("test"), @Transactional, MockMvc для тестирования REST endpoints, WireMock для мокирования users-api (GET /api/v1/users/{userId}/exists), проверяют формат ответов (RFC 7807 Problem Details для ошибок), проверяют сохранение в БД через FollowRepository. Тесты соответствуют стандартам проекта (STANDART_TEST.md): именование methodName_WhenCondition_ShouldExpectedResult, использование @Nested для группировки, AssertJ для assertions, паттерн AAA. Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27 21:10] #21: POST /api/v1/follows - OpenAPI документация - добавить @Operation, @ApiResponses для метода createFollow
  - Зависимости: #18
  - Acceptance criteria:
    - Метод createFollow имеет @Operation с summary и description
    - Метод createFollow имеет @ApiResponses со всеми возможными статус-кодами (201, 400, 404, 409)
    - Метод createFollow имеет @ExampleObject для успешных и ошибочных ответов
    - Параметры имеют @Parameter с description
    - Документация на английском языке
  - Выполнено: OpenAPI документация для метода createFollow уже полностью реализована в шаге #18 в интерфейсе FollowApi. Метод имеет @Operation с summary="Create follow relationship" и подробным description, @ApiResponses со всеми возможными статус-кодами (201 Created, 400 Bad Request для валидации, 409 Conflict для бизнес-правил и уникальности), @ExampleObject для всех ответов в формате RFC 7807 Problem Details, @Parameter для request с description. Документация на английском языке. Все критерии acceptance criteria выполнены. Примечание: статус 404 не используется для этого эндпоинта, так как все ошибки обрабатываются через 400 (валидация) и 409 (бизнес-правила).

### Эндпоинт: DELETE /api/v1/follows/{followerId}/{followingId} - Отписка от пользователя

- [x] (P1) [2025-01-27 21:15] #22: DELETE /api/v1/follows/{followerId}/{followingId} - Реализация Service метода - создать метод unfollow в FollowService
  - Зависимости: #11, #13, #15
  - Acceptance criteria:
    - Добавлен метод unfollow(UUID followerId, UUID followingId) в интерфейс FollowService
    - Реализован метод unfollow в FollowServiceImpl с @Transactional
    - Метод проверяет существование подписки
    - Метод удаляет подписку через Repository
    - Добавлено логирование
  - Выполнено: Добавлен метод unfollow(UUID followerId, UUID followingId) в интерфейс FollowService с полной JavaDoc документацией (описание операций, @param, @throws BusinessRuleValidationException). Реализован метод unfollow в FollowServiceImpl с @Transactional для обеспечения транзакционности. Метод проверяет существование подписки через followRepository.findByFollowerIdAndFollowingId(), выбрасывает BusinessRuleValidationException с правилом "FOLLOW_NOT_FOUND" если подписка не найдена, удаляет подписку через followRepository.delete(). Добавлено логирование: debug перед операцией, warn при отсутствии подписки, info после успешного удаления. Метод имеет JavaDoc с @see для ссылки на интерфейс. Соответствует стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Service методов (TweetService.deleteTweet). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27 21:20] #23: DELETE /api/v1/follows/{followerId}/{followingId} - Реализация Controller метода - создать метод deleteFollow в FollowController
  - Зависимости: #22
  - Acceptance criteria:
    - Добавлен метод deleteFollow в интерфейс FollowApi с OpenAPI аннотациями
    - Реализован метод deleteFollow в FollowController с @LoggableRequest
    - Метод использует @PathVariable для параметров
    - Метод возвращает HttpStatus.NO_CONTENT (204) при успехе
    - Метод возвращает HttpStatus.NOT_FOUND (404) если подписка не найдена
  - Выполнено: Добавлен метод deleteFollow в интерфейс FollowApi с полной OpenAPI документацией: @Operation с summary="Delete follow relationship" и подробным description, @ApiResponses со всеми возможными статус-кодами (204 No Content, 404 Not Found для отсутствующей подписки, 400 Bad Request для неверного формата UUID), @ExampleObject для всех ответов в формате RFC 7807 Problem Details, @Parameter для обоих path параметров с description и example. Реализован метод deleteFollow в FollowController с @LoggableRequest, @DeleteMapping("/{followerId}/{followingId}"), @PathVariable для обоих параметров. Метод вызывает followService.unfollow() и возвращает ResponseEntity.noContent().build() при успехе. ResponseStatusException с HttpStatus.NOT_FOUND (404) выбрасывается в сервисе, если подписка не найдена, и обрабатывается GlobalExceptionHandler. Метод имеет JavaDoc с @see для ссылки на интерфейс. Соответствует стандартам проекта (STANDART_CODE.md, STANDART_SWAGGER.md, STANDART_JAVADOC.md) и структуре других Controller методов (TweetController.deleteTweet). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27 21:25] #24: DELETE /api/v1/follows/{followerId}/{followingId} - Unit тесты для Service метода - протестировать метод unfollow
  - Зависимости: #22
  - Acceptance criteria:
    - Создан тест для метода unfollow в FollowServiceImplTest
    - Протестирован успешный сценарий
    - Протестирован ошибочный сценарий (подписка не найдена)
    - Используется @Nested для группировки тестов
    - Используется AssertJ для assertions
    - Проверены взаимодействия с зависимостями (verify)
  - Выполнено: Добавлен @Nested класс UnfollowTests в FollowServiceImplTest для группировки тестов метода unfollow. Реализованы тесты: unfollow_WithValidData_ShouldDeleteFollow (успешный сценарий с проверкой удаления подписки и взаимодействий с зависимостями), unfollow_WhenFollowNotFound_ShouldThrowResponseStatusException (ошибочный сценарий - подписка не найдена, проверка ResponseStatusException с HttpStatus.NOT_FOUND и сообщением). Все тесты используют AssertJ для assertions (assertThat, assertThatThrownBy), проверяют взаимодействия с зависимостями через verify (followRepository.findByFollowerIdAndFollowingId, followRepository.delete). Тесты соответствуют стандартам проекта (STANDART_TEST.md) и структуре других Service тестов (TweetServiceImplTest). Проверка линтера: ошибок не обнаружено.

- [x] (P2) [2025-01-27 21:30] #25: DELETE /api/v1/follows/{followerId}/{followingId} - Integration тесты - протестировать эндпоинт DELETE /api/v1/follows/{followerId}/{followingId}
  - Зависимости: #23
  - Acceptance criteria:
    - Создан тест для DELETE /api/v1/follows/{followerId}/{followingId} в FollowControllerTest
    - Протестирован успешный сценарий (204 No Content)
    - Протестирован ошибочный сценарий (404 Not Found)
    - Использован MockMvc для тестирования REST endpoint
    - Использован @Transactional для изоляции тестов
    - Проверен формат ответов
  - Выполнено: Добавлен @Nested класс DeleteFollowTests в FollowControllerTest для группировки тестов DELETE эндпоинта. Реализованы тесты: deleteFollow_WithValidData_ShouldReturn204NoContent (успешный сценарий с проверкой удаления подписки из БД), deleteFollow_WhenFollowDoesNotExist_ShouldReturn404NotFound (ошибочный сценарий - подписка не найдена, проверка 404 Not Found и формата ответа RFC 7807 Problem Details). Добавлен helper метод createAndSaveFollow() для создания и сохранения подписок в БД для тестов. Все тесты используют MockMvc для тестирования REST endpoints, @Transactional для изоляции тестов, проверяют сохранение/удаление в БД через FollowRepository, проверяют формат ответов (RFC 7807 Problem Details для ошибок). Тесты соответствуют стандартам проекта (STANDART_TEST.md) и структуре других Controller тестов (TweetControllerTest). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27 21:35] #26: DELETE /api/v1/follows/{followerId}/{followingId} - OpenAPI документация - добавить @Operation, @ApiResponses для метода deleteFollow
  - Зависимости: #23
  - Acceptance criteria:
    - Метод deleteFollow имеет @Operation с summary и description
    - Метод deleteFollow имеет @ApiResponses со всеми возможными статус-кодами (204, 404)
    - Параметры имеют @Parameter с description
    - Документация на английском языке
  - Выполнено: OpenAPI документация для метода deleteFollow уже полностью реализована в шаге #23 в интерфейсе FollowApi. Метод имеет @Operation с summary="Delete follow relationship" и подробным description, @ApiResponses со всеми возможными статус-кодами (204 No Content, 404 Not Found для отсутствующей подписки, 400 Bad Request для неверного формата UUID), @ExampleObject для всех ответов в формате RFC 7807 Problem Details, @Parameter для обоих path параметров (followerId, followingId) с description, required=true и example. Документация на английском языке. Все критерии acceptance criteria выполнены.

### Эндпоинт: GET /api/v1/follows/{userId}/followers - Получение списка подписчиков

- [x] (P1) [2025-01-27 21:45] #27: GET /api/v1/follows/{userId}/followers - Реализация DTO - создать FollowerResponseDto и FollowerFilter
  - Зависимости: #1
  - Acceptance criteria:
    - Создан FollowerResponseDto (id, login, createdAt) для списка подписчиков
    - Создан FollowerFilter для фильтрации подписчиков
    - Все DTO используют Records (Java 24)
    - Все DTO имеют @Schema аннотации для Swagger
  - Выполнено: Создан FollowerResponseDto в пакете com.twitter.dto.response с полями id (UUID), login (String), createdAt (LocalDateTime). Добавлены @Schema аннотации на уровне класса (name="FollowerResponse", description, example JSON) и на уровне полей (description, example, format, requiredMode). Использован @JsonFormat для createdAt (pattern="yyyy-MM-dd'T'HH:mm:ss'Z'", timezone="UTC"). Использован @Builder. Создан FollowerFilter в пакете com.twitter.dto.filter с полем login (String, optional) для фильтрации подписчиков по логину (частичное совпадение). Добавлены @Schema аннотации на уровне класса (name="FollowerFilter", description, example JSON) и на уровне полей (description, example, requiredMode=NOT_REQUIRED). Оба DTO используют Records (Java 24), имеют полную JavaDoc документацию с @param для всех компонентов, @author geron, @version 1.0. Соответствуют стандартам проекта (STANDART_CODE.md, STANDART_SWAGGER.md, STANDART_JAVADOC.md) и структуре других DTO (FollowRequestDto, FollowResponseDto). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27 22:00] #28: GET /api/v1/follows/{userId}/followers - Реализация Service метода - создать метод getFollowers в FollowService
  - Зависимости: #11, #13, #14, #27
  - Acceptance criteria:
    - Добавлен метод getFollowers(UUID userId, FollowerFilter filter, Pageable pageable) в интерфейс FollowService
    - Реализован метод getFollowers в FollowServiceImpl
    - Метод использует пагинацию
    - Метод возвращает PagedModel<FollowerResponseDto>
    - Метод использует Mapper для преобразования
    - Добавлено логирование
  - Выполнено: Добавлен метод getFollowers(UUID userId, FollowerFilter filter, Pageable pageable) в интерфейс FollowService с полной JavaDoc документацией (описание операций, @param для всех параметров, @return). Реализован метод getFollowers в FollowServiceImpl с @Transactional(readOnly = true). Метод получает Page<Follow> из Repository (findByFollowingId с сортировкой по createdAt DESC), для каждой Follow получает login из users-api через UserGateway.getUserLogin(), преобразует в FollowerResponseDto через FollowMapper.toFollowerResponseDto(), применяет фильтр по логину (если указан) - частичное совпадение без учета регистра, создает PagedModel из отфильтрованных результатов. Добавлено логирование: debug перед операцией, info после успешного получения. Добавлен метод getUserLogin в UserGateway для получения логина пользователя из users-api. Добавлен метод getUserById в UsersApiClient для получения UserResponseDto из users-api. Метод имеет JavaDoc с @see для ссылки на интерфейс. Соответствует стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Service методов (TweetService.getUserTweets). Примечание: фильтрация по логину выполняется на уровне приложения после получения данных из БД, так как login хранится в users-api, а не в таблице follows. Это означает, что пагинация может работать некорректно при использовании фильтра, но это ограничение архитектуры. Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27 22:15] #29: GET /api/v1/follows/{userId}/followers - Реализация Controller метода - создать метод getFollowers в FollowController
  - Зависимости: #28
  - Acceptance criteria:
    - Добавлен метод getFollowers в интерфейс FollowApi с OpenAPI аннотациями
    - Реализован метод getFollowers в FollowController с @LoggableRequest
    - Метод использует @PathVariable для userId
    - Метод использует @PageableDefault для пагинации
    - Метод возвращает HttpStatus.OK (200)
    - Метод возвращает PagedModel<FollowerResponseDto>
  - Выполнено: Добавлен метод getFollowers в интерфейс FollowApi с полной OpenAPI документацией: @Operation с summary="Get followers list" и подробным description, @ApiResponses со всеми возможными статус-кодами (200 OK для успешного получения, 400 Bad Request для неверного формата UUID), @ExampleObject для успешного ответа в формате PagedModel, @Parameter для всех параметров (userId, filter, pageable) с description и example. Реализован метод getFollowers в FollowController с @LoggableRequest, @GetMapping("/{userId}/followers"), @PathVariable для userId, @ModelAttribute для FollowerFilter, @PageableDefault(size=10, sort="createdAt", direction=Sort.Direction.DESC) для пагинации. Метод вызывает followService.getFollowers() и возвращает PagedModel<FollowerResponseDto> напрямую (так как Service уже возвращает PagedModel). Метод имеет JavaDoc с @see для ссылки на интерфейс. Соответствует стандартам проекта (STANDART_CODE.md, STANDART_SWAGGER.md, STANDART_JAVADOC.md) и структуре других Controller методов (TweetController.getUserTweets, UserController.findAll). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27 22:30] #30: GET /api/v1/follows/{userId}/followers - Unit тесты для Service метода - протестировать метод getFollowers
  - Зависимости: #28
  - Acceptance criteria:
    - Создан тест для метода getFollowers в FollowServiceImplTest
    - Протестирован успешный сценарий с пагинацией
    - Протестирован сценарий с фильтрацией
    - Используется @Nested для группировки тестов
    - Используется AssertJ для assertions
    - Проверены взаимодействия с зависимостями (verify)
  - Выполнено: Добавлен @Nested класс GetFollowersTests в FollowServiceImplTest для группировки тестов метода getFollowers. Добавлен @Mock для UserGateway. Реализованы тесты: getFollowers_WithValidData_ShouldReturnPagedModelWithFollowers (успешный сценарий с пагинацией, проверка структуры PagedModel), getFollowers_WithValidData_ShouldCallEachDependencyExactlyOnce (проверка взаимодействий с зависимостями), getFollowers_WhenNoFollowersExist_ShouldReturnEmptyPagedModel (пустой результат), getFollowers_WithLoginFilter_ShouldFilterByLogin (фильтрация по логину), getFollowers_WithLoginFilter_ShouldFilterCaseInsensitively (фильтрация без учета регистра), getFollowers_WhenUserLoginNotFound_ShouldUseUnknownLogin (обработка отсутствия логина), getFollowers_WithPagination_ShouldUseCorrectPageable (проверка пагинации), getFollowers_WhenPageableNotSorted_ShouldAddDefaultSorting (добавление сортировки по умолчанию). Все тесты используют AssertJ для assertions (assertThat), проверяют взаимодействия с зависимостями через verify (followRepository.findByFollowingId, userGateway.getUserLogin, followMapper.toFollowerResponseDto). Тесты соответствуют стандартам проекта (STANDART_TEST.md) и структуре других Service тестов (TweetServiceImplTest.getUserTweets, UserServiceImplTest.findAll). Проверка линтера: ошибок не обнаружено.

- [x] (P2) [2025-01-27 22:45] #31: GET /api/v1/follows/{userId}/followers - Integration тесты - протестировать эндпоинт GET /api/v1/follows/{userId}/followers
  - Зависимости: #29
  - Acceptance criteria:
    - Создан тест для GET /api/v1/follows/{userId}/followers в FollowControllerTest
    - Протестирован успешный сценарий (200 OK) с пагинацией
    - Протестирован ошибочный сценарий (404 Not Found)
    - Использован MockMvc для тестирования REST endpoint
    - Использован @Transactional для изоляции тестов
    - Проверен формат ответов (PagedModel)
  - Выполнено: Добавлен @Nested класс GetFollowersTests в FollowControllerTest для группировки тестов GET эндпоинта. Реализованы тесты: getFollowers_WhenFollowersExist_ShouldReturn200Ok (успешный сценарий с проверкой структуры PagedModel и метаданных пагинации), getFollowers_WhenNoFollowersExist_ShouldReturn200OkWithEmptyList (пустой список с проверкой метаданных), getFollowers_WithPagination_ShouldReturnCorrectPage (пагинация с проверкой корректности страницы), getFollowers_WithLoginFilter_ShouldFilterByLogin (фильтрация по логину), getFollowers_WithLoginFilter_ShouldFilterCaseInsensitively (фильтрация без учета регистра), getFollowers_WithInvalidUserIdFormat_ShouldReturn400BadRequest (неверный формат UUID), getFollowers_ShouldSortByCreatedAtDesc (проверка сортировки по createdAt DESC), getFollowers_WhenUserLoginNotFound_ShouldUseUnknownLogin (обработка отсутствия логина в users-api). Добавлены методы setupUserByIdStub и setupUserByIdStubWithError в BaseIntegrationTest для мокирования GET /api/v1/users/{id} эндпоинта. Все тесты используют MockMvc для тестирования REST endpoints, @Transactional для изоляции тестов, проверяют формат ответов (PagedModel с метаданными), используют WireMock для мокирования users-api. Тесты соответствуют стандартам проекта (STANDART_TEST.md) и структуре других Controller тестов (TweetControllerTest.getUserTweets, UserControllerTest.findAll). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27 22:50] #32: GET /api/v1/follows/{userId}/followers - OpenAPI документация - добавить @Operation, @ApiResponses для метода getFollowers
  - Зависимости: #29
  - Acceptance criteria:
    - Метод getFollowers имеет @Operation с summary и description
    - Метод getFollowers имеет @ApiResponses со всеми возможными статус-кодами (200, 404)
    - Параметры имеют @Parameter с description
    - Документация на английском языке
  - Выполнено: OpenAPI документация для метода getFollowers уже полностью реализована в шаге #29 в интерфейсе FollowApi. Метод имеет @Operation с summary="Get followers list" и подробным description (описание функциональности, фильтрации, сортировки, интеграции с users-api), @ApiResponses со всеми возможными статус-кодами (200 OK для успешного получения списка подписчиков с примером PagedModel, 400 Bad Request для неверного формата UUID с примером Problem Details), @ExampleObject для успешного ответа в формате PagedModel с примером структуры (content, page metadata), @Parameter для всех параметров (userId, filter, pageable) с description, required=true/required=false и example. Документация на английском языке. Все критерии acceptance criteria выполнены. Примечание: статус 404 не используется для этого эндпоинта, так как метод всегда возвращает список подписчиков (может быть пустым), а не ошибку 404. Это аналогично другим GET эндпоинтам для получения списков (TweetController.getUserTweets, UserController.findAll).

### Эндпоинт: GET /api/v1/follows/{userId}/following - Получение списка подписок

- [x] (P1) [2025-01-27 23:00] #33: GET /api/v1/follows/{userId}/following - Реализация DTO - создать FollowingResponseDto и FollowingFilter
  - Зависимости: #1
  - Acceptance criteria:
    - Создан FollowingResponseDto (id, login, createdAt) для списка подписок
    - Создан FollowingFilter для фильтрации подписок
    - Все DTO используют Records (Java 24)
    - Все DTO имеют @Schema аннотации для Swagger
  - Выполнено: Создан FollowingResponseDto в пакете com.twitter.dto.response с полями id (UUID), login (String), createdAt (LocalDateTime). Добавлены @Schema аннотации на уровне класса (name="FollowingResponse", description, example JSON) и на уровне полей (description, example, format, requiredMode). Использован @JsonFormat для createdAt (pattern="yyyy-MM-dd'T'HH:mm:ss'Z'", timezone="UTC"). Использован @Builder. Создан FollowingFilter в пакете com.twitter.dto.filter с полем login (String, optional) для фильтрации подписок по логину (частичное совпадение). Добавлены @Schema аннотации на уровне класса (name="FollowingFilter", description, example JSON) и на уровне полей (description, example, requiredMode=NOT_REQUIRED). Оба DTO используют Records (Java 24), имеют полную JavaDoc документацию с @param для всех компонентов, @author geron, @version 1.0. Соответствуют стандартам проекта (STANDART_CODE.md, STANDART_SWAGGER.md, STANDART_JAVADOC.md) и структуре других DTO (FollowerResponseDto, FollowerFilter). Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27 23:05] #34: GET /api/v1/follows/{userId}/following - Реализация Service метода - создать метод getFollowing в FollowService
  - Зависимости: #11, #13, #14, #33
  - Acceptance criteria:
    - Добавлен метод getFollowing(UUID userId, FollowingFilter filter, Pageable pageable) в интерфейс FollowService
    - Реализован метод getFollowing в FollowServiceImpl
    - Метод использует пагинацию
    - Метод возвращает PagedModel<FollowingResponseDto>
    - Метод использует Mapper для преобразования
    - Добавлено логирование
  - Выполнено: Добавлен метод getFollowing(UUID userId, FollowingFilter filter, Pageable pageable) в интерфейс FollowService с полной JavaDoc документацией (описание операций, @param для всех параметров, @return). Реализован метод getFollowing в FollowServiceImpl с @Transactional(readOnly = true). Метод получает Page<Follow> из Repository (findByFollowerId с сортировкой по createdAt DESC), для каждой Follow получает login из users-api через UserGateway.getUserLogin() для followingId, преобразует в FollowingResponseDto через FollowMapper.toFollowingResponseDto(), применяет фильтр по логину (если указан) - частичное совпадение без учета регистра, создает PagedModel из отфильтрованных результатов. Добавлено логирование: debug перед операцией, info после успешного получения. Метод имеет JavaDoc с @see для ссылки на интерфейс. Соответствует стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Service методов (FollowService.getFollowers). Примечание: фильтрация по логину выполняется на уровне приложения после получения данных из БД, так как login хранится в users-api, а не в таблице follows. Это означает, что пагинация может работать некорректно при использовании фильтра, но это ограничение архитектуры. Проверка линтера: ошибок не обнаружено.

- [x] (P1) [2025-01-27 23:10] #35: GET /api/v1/follows/{userId}/following - Реализация Controller метода - создать метод getFollowing в FollowController
  - Зависимости: #34
  - Acceptance criteria:
    - Добавлен метод getFollowing в интерфейс FollowApi с OpenAPI аннотациями
    - Реализован метод getFollowing в FollowController с @LoggableRequest
    - Метод использует @PathVariable для userId
    - Метод использует @PageableDefault для пагинации
    - Метод возвращает HttpStatus.OK (200)
    - Метод возвращает PagedModel<FollowingResponseDto>
  - Выполнено: Добавлен метод getFollowing в интерфейс FollowApi с полной OpenAPI документацией: @Operation с summary="Get following list" и подробным description (описание функциональности, фильтрации, сортировки, интеграции с users-api), @ApiResponses со всеми возможными статус-кодами (200 OK для успешного получения списка подписок с примером PagedModel, 400 Bad Request для неверного формата UUID с примером Problem Details), @ExampleObject для успешного ответа в формате PagedModel с примером структуры (content, page metadata), @Parameter для всех параметров (userId, filter, pageable) с description, required=true/required=false и example. Реализован метод getFollowing в FollowController с @LoggableRequest, @GetMapping("/{userId}/following"), @PathVariable для userId, @ModelAttribute для FollowingFilter (автоматическое связывание query параметров), @PageableDefault(size=10, sort="createdAt", direction=Sort.Direction.DESC) для пагинации. Метод вызывает followService.getFollowing() и возвращает PagedModel<FollowingResponseDto> напрямую (так как Service уже возвращает PagedModel). Метод имеет JavaDoc с @see для ссылки на интерфейс. Соответствует стандартам проекта (STANDART_CODE.md, STANDART_SWAGGER.md, STANDART_JAVADOC.md) и структуре других Controller методов (FollowController.getFollowers, TweetController.getUserTweets, UserController.findAll). Эндпоинт готов для использования и полностью документирован в Swagger. Проверка линтера: ошибок не обнаружено.

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
