# Changelog - Реализация эндпоинта GET /api/v1/tweets/{tweetId}/likes

## 2025-01-27

### Step #2: Обновление LikeRepository
- **Время:** 2025-01-27
- **Автор:** assistant
- **Описание:** Добавлен метод findByTweetIdOrderByCreatedAtDesc в LikeRepository для получения лайков с пагинацией
- **Изменения:**
  - Добавлен метод `Page<Like> findByTweetIdOrderByCreatedAtDesc(UUID tweetId, Pageable pageable)` в LikeRepository
  - Добавлены импорты `org.springframework.data.domain.Page` и `org.springframework.data.domain.Pageable`
  - Метод следует паттерну Derived Query Methods из Spring Data JPA
  - JavaDoc не требуется согласно STANDART_JAVADOC.md (Derived Query Methods являются self-documenting)
- **Файлы:**
  - `services/tweet-api/src/main/java/com/twitter/repository/LikeRepository.java`

### Step #3: Обновление LikeMapper
- **Время:** 2025-01-27
- **Автор:** assistant
- **Описание:** Добавлен метод toLikeResponseDtoPage в LikeMapper для маппинга Page<Like> в Page<LikeResponseDto>
- **Изменения:**
  - Добавлен default метод `Page<LikeResponseDto> toLikeResponseDtoPage(Page<Like> likes)` в LikeMapper
  - Метод использует существующий `toLikeResponseDto` для маппинга каждого элемента Page
  - Сохраняет метаданные пагинации (totalElements, totalPages, number, size)
  - Добавлен импорт `org.springframework.data.domain.Page`
  - Добавлен JavaDoc для метода согласно STANDART_JAVADOC.md
- **Файлы:**
  - `services/tweet-api/src/main/java/com/twitter/mapper/LikeMapper.java`

### Step #4: Service метод для эндпоинта
- **Время:** 2025-01-27
- **Автор:** assistant
- **Описание:** Добавлен метод getLikesByTweetId в LikeService интерфейс и реализация в LikeServiceImpl
- **Изменения:**
  - Добавлен метод `Page<LikeResponseDto> getLikesByTweetId(UUID tweetId, Pageable pageable)` в LikeService интерфейс
  - Добавлен полный JavaDoc для метода в интерфейсе согласно STANDART_JAVADOC.md
  - Реализован метод в LikeServiceImpl с валидацией существования твита через LikeValidator
  - Использует `@Transactional(readOnly = true)` для read-only операции
  - Валидация существования твита вынесена в LikeValidator.validateTweetExists
  - Использует `likeRepository.findByTweetIdOrderByCreatedAtDesc` для получения лайков
  - Использует `likeMapper.toLikeResponseDtoPage` для маппинга
  - Добавлены импорты `Page`, `Pageable`
  - Добавлено логирование для отладки
  - Добавлен метод `validateTweetExists` в LikeValidator интерфейс и реализация в LikeValidatorImpl
  - Удален неиспользуемый импорт `BusinessRuleValidationException` из LikeServiceImpl
- **Файлы:**
  - `services/tweet-api/src/main/java/com/twitter/service/LikeService.java`
  - `services/tweet-api/src/main/java/com/twitter/service/LikeServiceImpl.java`
  - `services/tweet-api/src/main/java/com/twitter/validation/LikeValidator.java`
  - `services/tweet-api/src/main/java/com/twitter/validation/LikeValidatorImpl.java`

### Step #5: OpenAPI интерфейс для эндпоинта
- **Время:** 2025-01-27
- **Автор:** assistant
- **Описание:** Добавлен метод getLikesByTweetId в LikeApi интерфейс с полной OpenAPI документацией
- **Изменения:**
  - Добавлен метод `PagedModel<LikeResponseDto> getLikesByTweetId(UUID tweetId, Pageable pageable)` в LikeApi интерфейс
  - Добавлен полный JavaDoc для метода согласно STANDART_JAVADOC.md
  - Добавлена аннотация `@Operation` с summary и description
  - Добавлена аннотация `@ApiResponses` с примерами для всех сценариев:
    - 200 OK: успешный ответ с пагинированным списком лайков (включая пример пустого списка)
    - 400 Bad Request: ошибки валидации (неверные параметры пагинации, неверный формат UUID)
    - 409 Conflict: бизнес-правила (твит не найден)
  - Добавлены аннотации `@Parameter` для tweetId и pageable с описаниями и примерами
  - Добавлены импорты `PagedModel` и `Pageable` из Spring Data Web
  - Следует паттерну из TweetApi.getUserTweets для пагинированных GET эндпоинтов
- **Файлы:**
  - `services/tweet-api/src/main/java/com/twitter/controller/LikeApi.java`

### Step #6: Controller реализация для эндпоинта
- **Время:** 2025-01-27
- **Автор:** assistant
- **Описание:** Реализован метод getLikesByTweetId в LikeController
- **Изменения:**
  - Добавлен метод `PagedModel<LikeResponseDto> getLikesByTweetId(UUID tweetId, Pageable pageable)` в LikeController
  - Использует аннотацию `@LoggableRequest` для логирования запросов
  - Использует аннотацию `@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)` для пагинации по умолчанию
  - Путь эндпоинта: `GET /api/v1/tweets/{tweetId}/likes`
  - Возвращает `PagedModel<LikeResponseDto>` с метаданными пагинации и HATEOAS ссылками
  - Преобразует `Page<LikeResponseDto>` в `PagedModel` через `new PagedModel<>(likes)`
  - Добавлены импорты `Page`, `Pageable`, `Sort`, `PageableDefault`, `PagedModel`
  - Следует паттерну из TweetController.getUserTweets для пагинированных GET эндпоинтов
  - Добавлен JavaDoc с ссылкой на LikeApi#getLikesByTweetId
- **Файлы:**
  - `services/tweet-api/src/main/java/com/twitter/controller/LikeController.java`

### Step #7: JavaDoc для эндпоинта
- **Время:** 2025-01-27
- **Автор:** assistant
- **Описание:** Проверен JavaDoc для всех новых методов эндпоинта
- **Изменения:**
  - Проверен JavaDoc для всех методов согласно STANDART_JAVADOC.md
  - LikeService.getLikesByTweetId: полный JavaDoc с @param, @return, @throws
  - LikeServiceImpl.getLikesByTweetId: JavaDoc с @see LikeService#getLikesByTweetId
  - LikeValidator.validateTweetExists: полный JavaDoc с @param, @throws
  - LikeValidatorImpl.validateTweetExists: JavaDoc с @see LikeValidator#validateTweetExists
  - LikeApi.getLikesByTweetId: полный JavaDoc с @param, @return, @throws
  - LikeController.getLikesByTweetId: JavaDoc с @see LikeApi#getLikesByTweetId
  - Все методы имеют JavaDoc с @author geron, @version 1.0
  - Все методы соответствуют стандартам проекта
- **Примечание:**
  - Метод toLikeResponseDtoPage не существует в LikeMapper (в LikeServiceImpl используется `likes.map(likeMapper::toLikeResponseDto)`)
- **Файлы:**
  - Проверены все файлы с новыми методами (изменений не требуется)

### Step #8: Unit тесты для эндпоинта
- **Время:** 2025-01-27
- **Автор:** assistant
- **Описание:** Добавлены unit тесты для LikeServiceImpl.getLikesByTweetId
- **Изменения:**
  - Добавлен вложенный класс `GetLikesByTweetIdTests` в LikeServiceImplTest
  - Добавлены тесты для всех сценариев:
    - `getLikesByTweetId_WhenLikesExist_ShouldReturnPageWithLikes` - успешный сценарий с лайками
    - `getLikesByTweetId_WhenNoLikesExist_ShouldReturnEmptyPage` - пустой список лайков
    - `getLikesByTweetId_WhenLikesExist_ShouldCallValidatorRepositoryAndMapper` - проверка вызовов зависимостей
    - `getLikesByTweetId_WhenNoLikesExist_ShouldCallValidatorAndRepositoryOnly` - проверка вызовов при пустом списке
    - `getLikesByTweetId_WhenTweetNotFound_ShouldThrowBusinessRuleValidationException` - твит не найден
    - `getLikesByTweetId_WithPagination_ShouldReturnCorrectPage` - проверка пагинации
  - Тесты следуют паттерну `methodName_WhenCondition_ShouldExpectedResult` согласно STANDART_TEST.md
  - Используются моки для зависимостей (LikeValidator, LikeRepository, LikeMapper)
  - Используется `PageImpl` для создания Page объектов
  - Добавлены импорты `Page`, `PageImpl`, `PageRequest`, `Pageable`, `List`
  - Все тесты используют AssertJ для проверок
  - Покрыты все основные сценарии использования метода
- **Примечание:**
  - Метод toLikeResponseDtoPage не существует в LikeMapper, поэтому тесты для маппера не требуются
- **Файлы:**
  - `services/tweet-api/src/test/java/com/twitter/service/LikeServiceImplTest.java`

### Step #9: Integration тесты для эндпоинта
- **Время:** 2025-01-27
- **Автор:** assistant
- **Описание:** Добавлены integration тесты для LikeController.getLikesByTweetId
- **Изменения:**
  - Добавлен вложенный класс `GetLikesByTweetIdTests` в LikeControllerTest
  - Добавлены тесты для всех сценариев:
    - `getLikesByTweetId_WhenLikesExist_ShouldReturn200Ok` - успешный сценарий с лайками (200 OK)
    - `getLikesByTweetId_WhenNoLikesExist_ShouldReturn200OkWithEmptyList` - пустой список лайков (200 OK)
    - `getLikesByTweetId_WhenTweetDoesNotExist_ShouldReturn409Conflict` - твит не найден (409 Conflict)
    - `getLikesByTweetId_WithInvalidUuid_ShouldReturn400BadRequest` - неверный формат UUID (400 Bad Request)
    - `getLikesByTweetId_WithPagination_ShouldReturnCorrectPage` - проверка пагинации с разными параметрами
    - `getLikesByTweetId_WithDefaultPagination_ShouldUseDefaultValues` - проверка пагинации по умолчанию
    - `getLikesByTweetId_ShouldReturnLikesSortedByCreatedAtDesc` - проверка сортировки по createdAt DESC
  - Тесты используют MockMvc для выполнения HTTP запросов
  - Проверяют структуру ответа (content, page), статус-коды и метаданные пагинации
  - Используют jsonPath для проверки JSON ответа
  - Тесты следуют паттерну `methodName_WhenCondition_ShouldExpectedResult` согласно STANDART_TEST.md
  - Добавлен импорт `get` метода из MockMvcRequestBuilders
  - Все тесты используют @SpringBootTest, @AutoConfigureWebMvc, @ActiveProfiles("test"), @Transactional
  - Покрыты все основные сценарии использования эндпоинта
- **Файлы:**
  - `services/tweet-api/src/test/java/com/twitter/controller/LikeControllerTest.java`

### Step #10: Swagger документация для эндпоинта
- **Время:** 2025-01-27
- **Автор:** assistant
- **Описание:** Проверена OpenAPI документация для getLikesByTweetId
- **Изменения:**
  - Проверена OpenAPI документация для getLikesByTweetId в LikeApi интерфейсе
  - Документация полная и включает все необходимые элементы:
    - `@Operation` с summary и description
    - `@ApiResponses` с примерами для всех сценариев:
      - 200 OK: два примера (Paginated Likes и Empty Likes List)
      - 400 Bad Request: два примера (Invalid Pagination Error и Invalid UUID Format Error)
      - 409 Conflict: пример (Tweet Not Found Error)
    - `@Parameter` для tweetId и pageable с описаниями и примерами
  - Все примеры соответствуют реальным ответам API
  - Документация следует паттерну из TweetApi.getUserTweets для пагинированных GET эндпоинтов
  - Все примеры используют правильный формат JSON согласно RFC 7807 Problem Details для ошибок
  - Документация соответствует стандартам проекта (STANDART_SWAGGER.md)
- **Примечание:**
  - Используется 409 Conflict вместо 404 Not Found для BusinessRuleValidationException, что соответствует стандартам проекта (бизнес-правила возвращают 409)
- **Файлы:**
  - Проверена документация в `services/tweet-api/src/main/java/com/twitter/controller/LikeApi.java` (изменений не требуется)