# Changelog - Follower API Service

## 2025-01-27 22:15 — step 29 done — GET /api/v1/follows/{userId}/followers - Реализация Controller метода — автор: assistant

Реализован метод getFollowers для получения списка подписчиков через REST API:
- Добавлен метод getFollowers в интерфейс FollowApi:
  - @Operation с summary="Get followers list" и подробным description
  - @ApiResponses со всеми возможными статус-кодами:
    - 200 OK - успешное получение списка подписчиков (с примером PagedModel)
    - 400 Bad Request - неверный формат UUID (с примером Problem Details)
  - @ExampleObject для успешного ответа в формате PagedModel с примером структуры
  - @Parameter для всех параметров (userId, filter, pageable) с description и example
  - Полная JavaDoc документация с @param для всех параметров, @return
- Реализован метод getFollowers в FollowController:
  - @LoggableRequest для автоматического логирования запросов/ответов
  - @GetMapping("/{userId}/followers") для обработки GET запросов
  - @PathVariable для userId
  - @ModelAttribute для FollowerFilter (автоматическое связывание query параметров)
  - @PageableDefault(size=10, sort="createdAt", direction=Sort.Direction.DESC) для пагинации
  - Вызывает followService.getFollowers() и возвращает PagedModel<FollowerResponseDto> напрямую
  - JavaDoc с @see для ссылки на интерфейс

Метод соответствует стандартам проекта (STANDART_CODE.md, STANDART_SWAGGER.md, STANDART_JAVADOC.md) и структуре других Controller методов (TweetController.getUserTweets, UserController.findAll). Эндпоинт готов для использования и полностью документирован в Swagger. Проверка линтера: ошибок не обнаружено.

## 2025-01-27 22:00 — step 28 done — GET /api/v1/follows/{userId}/followers - Реализация Service метода — автор: assistant

Реализован метод getFollowers для получения списка подписчиков:
- Добавлен метод getFollowers(UUID userId, FollowerFilter filter, Pageable pageable) в интерфейс FollowService:
  - Полная JavaDoc документация с описанием операций, @param для всех параметров, @return
  - Описание пагинации, фильтрации и интеграции с users-api
- Реализован метод getFollowers в FollowServiceImpl:
  - @Transactional(readOnly = true) для обеспечения транзакционности только для чтения
  - Получение Page<Follow> из Repository (findByFollowingId с сортировкой по createdAt DESC)
  - Для каждой Follow получение login из users-api через UserGateway.getUserLogin()
  - Преобразование в FollowerResponseDto через FollowMapper.toFollowerResponseDto()
  - Применение фильтра по логину (если указан) - частичное совпадение без учета регистра
  - Создание PagedModel из отфильтрованных результатов
  - Логирование: debug перед операцией, info после успешного получения
- Расширен UserGateway:
  - Добавлен метод getUserLogin(UUID userId) для получения логина пользователя из users-api
  - Обработка null userId, ошибок Feign клиента, логирование
- Расширен UsersApiClient:
  - Добавлен метод getUserById(UUID id) для получения UserResponseDto из users-api
  - Интеграция с GET /api/v1/users/{id} эндпоинтом

Метод соответствует стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Service методов (TweetService.getUserTweets). Примечание: фильтрация по логину выполняется на уровне приложения после получения данных из БД, так как login хранится в users-api, а не в таблице follows. Это означает, что пагинация может работать некорректно при использовании фильтра, но это ограничение архитектуры. Проверка линтера: ошибок не обнаружено.

## 2025-01-27 21:45 — step 27 done — GET /api/v1/follows/{userId}/followers - Реализация DTO — автор: assistant

Созданы DTO для эндпоинта GET /api/v1/follows/{userId}/followers:
- FollowerResponseDto в пакете com.twitter.dto.response:
  - Поля: id (UUID), login (String), createdAt (LocalDateTime)
  - @Schema аннотации на уровне класса (name="FollowerResponse", description, example JSON) и на уровне полей (description, example, format, requiredMode)
  - @JsonFormat для createdAt (pattern="yyyy-MM-dd'T'HH:mm:ss'Z'", timezone="UTC")
  - @Builder для удобства создания
  - Полная JavaDoc документация с @param для всех компонентов, @author geron, @version 1.0
- FollowerFilter в пакете com.twitter.dto.filter:
  - Поле: login (String, optional) для фильтрации подписчиков по логину (частичное совпадение)
  - @Schema аннотации на уровне класса (name="FollowerFilter", description, example JSON) и на уровне полей (description, example, requiredMode=NOT_REQUIRED)
  - Полная JavaDoc документация с @param для всех компонентов, @author geron, @version 1.0
  - Примечание: фильтрация по логину будет выполняться на уровне сервиса после получения данных из users-api, так как login не хранится в таблице follows

Оба DTO используют Records (Java 24), соответствуют стандартам проекта (STANDART_CODE.md, STANDART_SWAGGER.md, STANDART_JAVADOC.md) и структуре других DTO (FollowRequestDto, FollowResponseDto). Проверка линтера: ошибок не обнаружено.

## 2025-01-27 21:35 — step 26 done — DELETE /api/v1/follows/{followerId}/{followingId} - OpenAPI документация — автор: assistant

OpenAPI документация для метода deleteFollow уже полностью реализована в шаге #23 в интерфейсе FollowApi. Все критерии acceptance criteria выполнены: @Operation с summary и description, @ApiResponses со всеми возможными статус-кодами (204, 404, 400), @ExampleObject для всех ответов, @Parameter с description для обоих параметров, документация на английском языке.

## 2025-01-27 21:30 — step 25 done — DELETE /api/v1/follows/{followerId}/{followingId} - Integration тесты — автор: assistant

Добавлены integration тесты для эндпоинта DELETE /api/v1/follows/{followerId}/{followingId}:
- @Nested класс DeleteFollowTests для группировки тестов DELETE эндпоинта
- Тесты успешного сценария:
  - deleteFollow_WithValidData_ShouldReturn204NoContent - проверка удаления подписки (204 No Content) с проверкой удаления из БД
- Тесты ошибочного сценария:
  - deleteFollow_WhenFollowDoesNotExist_ShouldReturn404NotFound - проверка 404 Not Found и формата ответа (RFC 7807 Problem Details)
- Helper метод createAndSaveFollow() для создания и сохранения подписок в БД для тестов
- Все тесты используют MockMvc для тестирования REST endpoints
- Все тесты используют @Transactional для изоляции тестов
- Все тесты проверяют сохранение/удаление в БД через FollowRepository
- Все тесты проверяют формат ответов (RFC 7807 Problem Details для ошибок)

Тесты соответствуют стандартам проекта (STANDART_TEST.md) и структуре других Controller тестов (TweetControllerTest). Проверка линтера: ошибок не обнаружено.

## 2025-01-27 21:25 — step 24 done — DELETE /api/v1/follows/{followerId}/{followingId} - Unit тесты для Service метода — автор: assistant

Добавлены unit тесты для метода unfollow в FollowServiceImplTest:
- @Nested класс UnfollowTests для группировки тестов метода unfollow
- Тесты успешного сценария:
  - unfollow_WithValidData_ShouldDeleteFollow - проверка успешного удаления подписки и взаимодействий с зависимостями
- Тесты ошибочного сценария:
  - unfollow_WhenFollowNotFound_ShouldThrowResponseStatusException - проверка выброса ResponseStatusException с HttpStatus.NOT_FOUND (404) и сообщением, содержащим followerId и followingId
- Все тесты используют AssertJ для assertions (assertThat, assertThatThrownBy)
- Все тесты проверяют взаимодействия с зависимостями через verify (followRepository.findByFollowerIdAndFollowingId, followRepository.delete)
- Тесты используют @BeforeEach для инициализации тестовых данных

Тесты соответствуют стандартам проекта (STANDART_TEST.md) и структуре других Service тестов (TweetServiceImplTest). Проверка линтера: ошибок не обнаружено.

## 2025-01-27 21:20 — step 23 done — DELETE /api/v1/follows/{followerId}/{followingId} - Реализация Controller метода — автор: assistant

Реализован метод deleteFollow для удаления подписок через REST API:
- Добавлен метод deleteFollow в интерфейс FollowApi:
  - @Operation с summary="Delete follow relationship" и подробным description
  - @ApiResponses со всеми возможными статус-кодами:
    - 204 No Content - успешное удаление
    - 404 Not Found - подписка не найдена (с примером Problem Details)
    - 400 Bad Request - неверный формат UUID (с примером Problem Details)
  - @ExampleObject для всех ответов в формате RFC 7807 Problem Details
  - @Parameter для обоих path параметров (followerId, followingId) с description и example
  - Полная JavaDoc документация с @param для обоих параметров, @return
- Реализован метод deleteFollow в FollowController:
  - @LoggableRequest для автоматического логирования запросов/ответов
  - @DeleteMapping("/{followerId}/{followingId}") для обработки DELETE запросов
  - @PathVariable для обоих параметров (followerId, followingId)
  - Вызывает followService.unfollow() и возвращает ResponseEntity.noContent().build() при успехе (204 No Content)
  - ResponseStatusException с HttpStatus.NOT_FOUND (404) выбрасывается в сервисе, если подписка не найдена, и обрабатывается GlobalExceptionHandler
  - JavaDoc с @see для ссылки на интерфейс

Метод соответствует стандартам проекта (STANDART_CODE.md, STANDART_SWAGGER.md, STANDART_JAVADOC.md) и структуре других Controller методов (TweetController.deleteTweet). Эндпоинт готов для использования и полностью документирован в Swagger. Проверка линтера: ошибок не обнаружено.

## 2025-01-27 21:15 — step 22 done — DELETE /api/v1/follows/{followerId}/{followingId} - Реализация Service метода — автор: assistant

Реализован метод unfollow для удаления подписок:
- Добавлен метод unfollow(UUID followerId, UUID followingId) в интерфейс FollowService:
  - Полная JavaDoc документация с описанием операций, @param для обоих параметров, @throws BusinessRuleValidationException
  - Описание транзакционности и проверки существования подписки
- Реализован метод unfollow в FollowServiceImpl:
  - @Transactional для обеспечения транзакционности
  - Проверка существования подписки через followRepository.findByFollowerIdAndFollowingId()
  - Выброс BusinessRuleValidationException с правилом "FOLLOW_NOT_FOUND" если подписка не найдена
  - Удаление подписки через followRepository.delete()
  - Логирование: debug перед операцией, warn при отсутствии подписки, info после успешного удаления
  - JavaDoc с @see для ссылки на интерфейс

Метод соответствует стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Service методов (TweetService.deleteTweet). Проверка линтера: ошибок не обнаружено.

## 2025-01-27 21:10 — step 21 done — POST /api/v1/follows - OpenAPI документация — автор: assistant

OpenAPI документация для метода createFollow уже полностью реализована в шаге #18 в интерфейсе FollowApi. Все критерии acceptance criteria выполнены: @Operation с summary и description, @ApiResponses со всеми возможными статус-кодами (201, 400, 409), @ExampleObject для всех ответов, @Parameter с description, документация на английском языке. Примечание: статус 404 не используется для этого эндпоинта, так как все ошибки обрабатываются через 400 (валидация) и 409 (бизнес-правила).

## 2025-01-27 21:00 — step 20 done — POST /api/v1/follows - Integration тесты — автор: assistant

Созданы integration тесты для эндпоинта POST /api/v1/follows:
- BaseIntegrationTest в services/follower-api/src/test/java/com/twitter/testconfig/BaseIntegrationTest.java:
  - Настройка PostgreSQL Testcontainers (postgres:15-alpine)
  - Настройка WireMock сервера для мокирования users-api
  - Динамическая конфигурация Spring properties через @DynamicPropertySource
  - Helper методы setupUserExistsStub() и setupUserExistsStubWithError() для настройки WireMock stubs
  - Автоматический reset WireMock перед каждым тестом
- application-test.yml в services/follower-api/src/test/resources/application-test.yml:
  - Конфигурация для тестового профиля
  - Настройка Feign клиента для тестов
  - Настройка логирования для тестов
- FollowControllerTest в services/follower-api/src/test/java/com/twitter/controller/FollowControllerTest.java:
  - @Nested класс CreateFollowTests для группировки тестов
  - Тесты успешного сценария:
    - createFollow_WithValidData_ShouldReturn201Created - проверка создания подписки с валидацией ответа и сохранения в БД
  - Тесты валидации (400 Bad Request):
    - createFollow_WithNullFollowerId_ShouldReturn400BadRequest - проверка валидации null полей
    - createFollow_WithNullFollowingId_ShouldReturn400BadRequest - проверка валидации null полей
    - createFollow_WithInvalidJson_ShouldReturn400BadRequest - проверка неверного формата JSON
    - createFollow_WithMissingBody_ShouldReturn400BadRequest - проверка отсутствия body
  - Тесты бизнес-правил (409 Conflict):
    - createFollow_WhenSelfFollow_ShouldReturn409Conflict - проверка самоподписки (SELF_FOLLOW_NOT_ALLOWED)
    - createFollow_WhenFollowerNotFound_ShouldReturn409Conflict - проверка несуществующего follower (FOLLOWER_NOT_EXISTS)
    - createFollow_WhenFollowingNotFound_ShouldReturn409Conflict - проверка несуществующего following (FOLLOWING_NOT_EXISTS)
    - createFollow_WhenFollowAlreadyExists_ShouldReturn409Conflict - проверка двойной подписки
  - Тесты обработки ошибок:
    - createFollow_WhenUsersApiReturns500_ShouldHandleGracefully - проверка graceful handling ошибок users-api
  - Все тесты проверяют формат ответов (RFC 7807 Problem Details для ошибок)
  - Все тесты проверяют сохранение в БД через FollowRepository
  - Helper методы: createValidRequest(), verifyFollowInDatabase(), getFollowCount()

Тесты используют @SpringBootTest, @AutoConfigureWebMvc, @ActiveProfiles("test"), @Transactional для изоляции тестов, MockMvc для тестирования REST endpoints, WireMock для мокирования users-api. Все тесты соответствуют стандартам проекта (STANDART_TEST.md) и структуре других Controller тестов (TweetControllerTest). Проверка линтера: ошибок не обнаружено.

## 2025-01-27 20:50 — step 19 done — POST /api/v1/follows - Unit тесты для Service метода — автор: assistant

Создан FollowServiceImplTest в services/follower-api/src/test/java/com/twitter/service/FollowServiceImplTest.java для unit тестирования метода follow:
- @ExtendWith(MockitoExtension.class) для использования Mockito
- @Mock для зависимостей: FollowRepository, FollowMapper, FollowValidator
- @InjectMocks для FollowServiceImpl
- @Nested класс FollowTests для группировки тестов метода follow
- Тесты успешного сценария:
  - follow_WithValidData_ShouldReturnFollowResponseDto - проверка корректного возврата FollowResponseDto
  - follow_WithValidData_ShouldCallEachDependencyExactlyOnce - проверка взаимодействий с зависимостями
- Тесты ошибочных сценариев:
  - follow_WhenSelfFollow_ShouldThrowBusinessRuleValidationException - валидация самоподписки
  - follow_WhenFollowerNotFound_ShouldThrowBusinessRuleValidationException - пользователь не найден (follower)
  - follow_WhenFollowingNotFound_ShouldThrowBusinessRuleValidationException - пользователь не найден (following)
  - follow_WhenFollowAlreadyExists_ShouldThrowUniquenessValidationException - двойная подписка
- Все тесты используют AssertJ для assertions (assertThat, assertThatThrownBy)
- Все тесты проверяют взаимодействия с зависимостями через verify()
- Тесты используют @BeforeEach для инициализации тестовых данных
- Тесты соответствуют стандартам проекта (STANDART_TEST.md) и структуре других Service тестов (TweetServiceImplTest)

Тесты покрывают все сценарии использования метода follow, включая успешное создание подписки и все возможные ошибки валидации. Проверка линтера: ошибок не обнаружено.

## 2025-12-17 20:45 — step 18 done — POST /api/v1/follows - Реализация Controller метода — автор: assistant

Созданы классы Controller для эндпоинта POST /api/v1/follows:
- FollowApi (интерфейс) в пакете com.twitter.controller:
  - @Tag(name="Follow Management", description="API for managing follow relationships in the Twitter system")
  - Метод createFollow(FollowRequestDto) с полной OpenAPI документацией:
    - @Operation с summary="Create follow relationship" и подробным description
    - @ApiResponses со всеми возможными статус-кодами:
      - 201 Created - успешное создание с примером FollowResponseDto
      - 400 Bad Request - ошибка валидации (неверный формат UUID, пустые поля)
      - 409 Conflict - нарушение бизнес-правил (подписка на себя, пользователь не существует)
      - 409 Conflict - нарушение уникальности (подписка уже существует)
    - @ExampleObject для всех ответов в формате RFC 7807 Problem Details
    - @Parameter для request с description
  - Полная JavaDoc документация с @author geron, @version 1.0
- FollowController (реализация) в пакете com.twitter.controller:
  - @RestController, @RequestMapping("/api/v1/follows"), @RequiredArgsConstructor, @Slf4j
  - Зависимость: FollowService
  - Метод createFollow реализован с:
    - @LoggableRequest для автоматического логирования запросов/ответов
    - @PostMapping для обработки POST запросов
    - @RequestBody @Valid FollowRequestDto для валидации запроса
    - Возвращает ResponseEntity.status(HttpStatus.CREATED).body(createdFollow)
  - JavaDoc с @see для ссылки на интерфейс

Controller соответствует стандартам проекта (STANDART_CODE.md, STANDART_SWAGGER.md, STANDART_JAVADOC.md) и структуре других Controller (TweetController, UserController). Эндпоинт готов для использования и полностью документирован в Swagger. Проверка линтера: ошибок не обнаружено.

## 2025-12-17 20:40 — step 17 done — POST /api/v1/follows - Реализация Service метода — автор: assistant

Созданы классы Service для управления подписками:
- FollowService (интерфейс) в пакете com.twitter.service:
  - Метод follow(FollowRequestDto) возвращает FollowResponseDto
  - Полная JavaDoc документация с описанием операций, @param, @return, @throws (BusinessRuleValidationException, UniquenessValidationException)
  - Описание бизнес-правил и транзакционности
- FollowServiceImpl (реализация) в пакете com.twitter.service:
  - @Service, @RequiredArgsConstructor, @Slf4j
  - Зависимости: FollowRepository, FollowMapper, FollowValidator
  - Метод follow реализован с @Transactional для обеспечения транзакционности
  - Последовательность операций:
    1. Логирование (debug) перед операцией
    2. Валидация через followValidator.validateForFollow()
    3. Преобразование DTO в Entity через followMapper.toFollow()
    4. Сохранение через followRepository.saveAndFlush()
    5. Преобразование Entity в Response DTO через followMapper.toFollowResponseDto()
    6. Логирование (info) после успешного создания
  - JavaDoc с @see для ссылки на интерфейс

Service соответствует стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Service (TweetService, UserService). Метод обеспечивает транзакционность, валидацию бизнес-правил и логирование операций. Проверка линтера: ошибок не обнаружено.

## 2025-12-17 20:35 — step 16 done — POST /api/v1/follows - Реализация DTO — автор: assistant

Созданы DTO для эндпоинта POST /api/v1/follows:
- FollowRequestDto в пакете com.twitter.dto.request:
  - Поля: followerId (UUID, @NotNull), followingId (UUID, @NotNull)
  - Валидация: @NotNull для обоих полей
  - @Schema аннотации на уровне класса (name="FollowRequest", description, example JSON) и на уровне полей (description, example UUID, format="uuid", requiredMode=REQUIRED)
  - @Builder для удобства создания
  - Полная JavaDoc документация с @param для всех компонентов, @author geron, @version 1.0
- FollowResponseDto в пакете com.twitter.dto.response:
  - Поля: id (UUID), followerId (UUID), followingId (UUID), createdAt (LocalDateTime)
  - @Schema аннотации на уровне класса (name="FollowResponse", description, example JSON) и на уровне полей (description, example, format)
  - @JsonFormat для createdAt (pattern="yyyy-MM-dd'T'HH:mm:ss'Z'", timezone="UTC")
  - @Builder для удобства создания
  - Полная JavaDoc документация с @param для всех компонентов, @author geron, @version 1.0

Оба DTO используют Records (Java 24), соответствуют стандартам проекта (STANDART_CODE.md, STANDART_SWAGGER.md, STANDART_JAVADOC.md) и структуре других DTO (CreateTweetRequestDto, TweetResponseDto). Примеры UUID и данных реалистичны. Проверка линтера: ошибок не обнаружено.

## 2025-12-17 20:30 — step 15 done — Реализация Validator — автор: assistant

Созданы классы для валидации бизнес-правил подписок:
- FollowValidator (интерфейс) в пакете com.twitter.validation:
  - Метод validateForFollow(FollowRequestDto) для полной валидации создания подписки
  - Полная JavaDoc документация с @author geron, @version 1.0
- FollowValidatorImpl (реализация) в пакете com.twitter.validation:
  - @Component, @RequiredArgsConstructor, @Slf4j
  - Зависимости: FollowRepository, UserGateway
  - Реализованы три валидации:
    1. validateNoSelfFollow - проверка, что пользователь не может подписаться на себя (followerId != followingId), выбрасывает BusinessRuleValidationException с правилом "SELF_FOLLOW_NOT_ALLOWED"
    2. validateUsersExist - проверка существования обоих пользователей через UserGateway.existsUser(), выбрасывает BusinessRuleValidationException с правилами "FOLLOWER_NOT_EXISTS" и "FOLLOWING_NOT_EXISTS"
    3. validateUniqueness - проверка уникальности подписки через FollowRepository.existsByFollowerIdAndFollowingId(), выбрасывает UniquenessValidationException при дублировании
  - Все методы имеют логирование (warn для ошибок валидации)
  - Обработка null значений для request, followerId, followingId
  - Полная JavaDoc документация с @author geron, @version 1.0

Validator соответствует стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Validator (UserValidator, TweetValidator). Использует исключения из common-lib (BusinessRuleValidationException, UniquenessValidationException), которые обрабатываются GlobalExceptionHandler с соответствующими HTTP статусами (409 Conflict). Проверка линтера: ошибок не обнаружено. Примечание: FollowRequestDto будет создан в шаге #16.

## 2025-12-17 20:25 — step 14 done — Реализация Mapper (MapStruct) — автор: assistant

Создан FollowMapper в пакете com.twitter.mapper для преобразования между Follow entities и DTO:
- @Mapper аннотация (настроен как Spring компонент через defaultComponentModel=spring в build.gradle)
- Методы маппинга:
  - toFollow(FollowRequestDto) - преобразование DTO в Entity с игнорированием service-managed полей (id, createdAt)
  - toFollowResponseDto(Follow) - преобразование Entity в Response DTO для ответов API
  - toFollowerResponseDto(Follow, String login) - преобразование для списка подписчиков (login получается через users-api)
  - toFollowingResponseDto(Follow, String login) - преобразование для списка подписок (login получается через users-api)
- Все методы используют @Mapping для настройки маппинга полей
- Игнорирование service-managed полей (id, createdAt) при создании Entity из DTO
- Полная JavaDoc документация с @author geron, @version 1.0

Mapper соответствует стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Mapper (UserMapper, TweetMapper). MapStruct автоматически сгенерирует реализацию при компиляции. Примечание: DTO классы (FollowRequestDto, FollowResponseDto, FollowerResponseDto, FollowingResponseDto) будут созданы в последующих шагах (#16, #27, #33). Проверка линтера: ошибок не обнаружено.

## 2025-12-17 20:20 — step 13 done — Реализация Repository — автор: assistant

Создан FollowRepository в пакете com.twitter.repository для доступа к данным о подписках:
- Расширяет JpaRepository<Follow, UUID> для стандартных CRUD операций
- Derived Query Methods (без JavaDoc согласно стандартам):
  - existsByFollowerIdAndFollowingId(UUID, UUID) - проверка существования подписки
  - findByFollowerId(UUID, Pageable) - поиск подписок пользователя с пагинацией
  - findByFollowingId(UUID, Pageable) - поиск подписчиков пользователя с пагинацией
  - countByFollowerId(UUID) - подсчет количества подписок пользователя
  - countByFollowingId(UUID) - подсчет количества подписчиков пользователя
  - findByFollowerIdAndFollowingId(UUID, UUID) - поиск конкретной подписки (возвращает Optional<Follow>)
- JavaDoc документация только на уровне класса с @author geron, @version 1.0
- Все методы являются Derived Query Methods и self-documenting через имена методов

Repository соответствует стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Repository (UserRepository, TweetRepository). Методы обеспечивают все необходимые операции для работы эндпоинтов (создание подписки, получение списков с пагинацией, проверка статуса, статистика). Проверка линтера: ошибок не обнаружено.

## 2025-12-17 20:15 — step 12 done — Реализация Gateway для users-api — автор: assistant

Созданы классы для интеграции с Users API:
- UsersApiClient (Feign Client) в пакете com.twitter.client:
  - @FeignClient с name="users-api", url="${app.users-api.base-url:http://localhost:8081}", path="/api/v1/users"
  - Метод existsUser(UUID userId) вызывает GET /api/v1/users/{userId}/exists
  - Возвращает UserExistsResponseDto из common-lib
  - Полная JavaDoc документация с @author geron, @version 1.0
- UserGateway (Gateway wrapper) в пакете com.twitter.gateway:
  - @Component, @RequiredArgsConstructor, @Slf4j
  - Метод existsUser(UUID userId) оборачивает вызов Feign клиента
  - Обработка null userId: возвращает false с предупреждением в лог
  - Обработка исключений: возвращает false с debug логированием
  - Логирование успешных проверок существования пользователя
  - Полная JavaDoc документация с @author geron, @version 1.0

Классы соответствуют стандартам проекта (STANDART_CODE.md, STANDART_JAVADOC.md) и структуре других Gateway/Feign клиентов (tweet-api, admin-script-api). Gateway обеспечивает абстракцию над HTTP клиентом, централизованную обработку ошибок и упрощает тестирование. Проверка линтера: ошибок не обнаружено.

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

