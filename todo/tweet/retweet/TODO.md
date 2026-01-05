# TODO: Ретвитнуть с опциональным комментарием

## Meta
- project: twitter-tweet-api
- updated: 2025-01-27
- changelog: todo/tweet/retweet/CHANGELOG.md
- standards:
  - STANDART_CODE.md
  - STANDART_PROJECT.md
  - STANDART_TEST.md
  - STANDART_JAVADOC.md
  - STANDART_SWAGGER.md
  - STANDART_README.md
  - STANDART_POSTMAN.md

## Tasks

### Анализ и проектирование
- [x] (P1) [2025-01-27] #1: Анализ требований — Определить структуру Entity, DTO, бизнес-правила, SQL скрипт
  acceptance: "Понять вход/выход, non-functional requirements, определить затронутые стандарты, определить структуру данных"
  note: "Создан документ analysis-requirements.md с полным анализом требований. Определены структуры Entity, DTO, бизнес-правила, SQL скрипт, затронутые стандарты, вход/выход, non-functional requirements. Выявлен риск отсутствия поля retweets_count в таблице tweets."
- [x] (P1) [2025-01-27] #2: Проектирование API и контрактов — Определить контракт эндпоинта POST /api/v1/tweets/{tweetId}/retweet
  acceptance: "OpenAPI схема для эндпоинта, DTO структура, Entity структура, определение всех компонентов"
  note: "Создан документ design-api-contracts.md с полным проектированием API. Определены OpenAPI интерфейс RetweetApi с аннотациями, структура DTO (RetweetRequestDto, RetweetResponseDto) с @Schema, структура Entity Retweet с бизнес-методами, все компоненты (Repository, Mapper, Validator, Service, Controller), HTTP статус-коды, исключения, примеры для всех сценариев."

### Реализация инфраструктуры и конфигов
- [x] (P1) [2025-01-27] #3: SQL скрипт для таблицы tweet_retweets — Создать script_5_tweet_retweets.sql
  acceptance: "SQL скрипт создан с таблицей, уникальным ограничением на (tweet_id, user_id), foreign keys, индексами"
  note: "Создан SQL скрипт script_5_tweet_retweets.sql с таблицей tweet_retweets, полями (id, tweet_id, user_id, comment, created_at), уникальным ограничением на (tweet_id, user_id), foreign keys на tweets(id) и users(id), индексами для оптимизации. Примечание: требуется добавить поле retweets_count в таблицу tweets (отдельный скрипт или миграция)."
- [x] (P1) [2025-01-27] #4: Реализация Entity Retweet — Создать JPA сущность
  acceptance: "Entity создана с полями id, tweetId, userId, comment, createdAt, бизнес-методами isByUser(), isForTweet(), hasComment(), с учетом STANDART_CODE.md"
  note: "Создана JPA сущность Retweet в services/tweet-api/src/main/java/com/twitter/entity/Retweet.java с полями id, tweetId, userId, comment (nullable), createdAt, уникальным ограничением на (tweet_id, user_id), бизнес-методами isByUser(), isForTweet(), hasComment(). Entity соответствует стандартам проекта и структуре таблицы tweet_retweets."
- [x] (P1) [2025-01-27] #5: Реализация Repository RetweetRepository — Создать Spring Data JPA репозиторий
  acceptance: "Repository создан с Derived Query Methods (findByTweetIdAndUserId, existsByTweetIdAndUserId) без JavaDoc"
  note: "Создан Spring Data JPA репозиторий RetweetRepository в services/tweet-api/src/main/java/com/twitter/repository/RetweetRepository.java, расширяющий JpaRepository<Retweet, UUID>. Реализованы Derived Query Methods: findByTweetIdAndUserId(UUID, UUID) и existsByTweetIdAndUserId(UUID, UUID) без JavaDoc (согласно стандартам)."
- [x] (P1) [2025-01-27] #6: Реализация DTO для ретвита — Создать RetweetRequestDto и RetweetResponseDto
  acceptance: "DTO созданы как Records с валидацией (@NotNull для userId, @Size(max=280) для comment), @Schema аннотациями, размещены в правильных пакетах"
  note: "Созданы DTO: RetweetRequestDto (com.twitter.dto.request) с полями userId (@NotNull) и comment (@Size(max=280), nullable), RetweetResponseDto (com.twitter.dto.response) с полями id, tweetId, userId, comment (nullable), createdAt. Оба DTO созданы как Records с @Builder, полными @Schema аннотациями, JavaDoc и примерами."
- [x] (P1) [2026-01-05 16:40] #7: Реализация Mapper интерфейса RetweetMapper — Создать MapStruct интерфейс
  acceptance: "Mapper интерфейс создан с методами toRetweet(RetweetRequestDto, UUID tweetId) и toRetweetResponseDto(Retweet)"
  note: "Создан MapStruct интерфейс RetweetMapper в services/tweet-api/src/main/java/com/twitter/mapper/RetweetMapper.java с методами toRetweet(RetweetRequestDto, UUID tweetId) и toRetweetResponseDto(Retweet). Метод toRetweet игнорирует service-managed поля (id, createdAt), маппит tweetId из параметра, userId и comment из requestDto. Метод toRetweetResponseDto преобразует Retweet entity в RetweetResponseDto. Интерфейс создан по аналогии с LikeMapper, соответствует стандартам проекта."
- [x] (P1) [2026-01-05 16:40] #8: Реализация Validator интерфейса RetweetValidator — Создать интерфейс
  acceptance: "Validator interface создан с методом validateForRetweet(UUID tweetId, RetweetRequestDto requestDto)"
  note: "Создан интерфейс RetweetValidator в services/tweet-api/src/main/java/com/twitter/validation/RetweetValidator.java с методом validateForRetweet(UUID tweetId, RetweetRequestDto requestDto). Метод документирован с описанием всех проверок: существование твита, существование пользователя, запрет self-retweet, проверка уникальности, валидация комментария. Определены исключения: BusinessRuleValidationException, UniquenessValidationException, FormatValidationException. Интерфейс создан по аналогии с LikeValidator, соответствует стандартам проекта."
- [x] (P1) [2026-01-05 16:40] #9: Реализация Service интерфейса RetweetService — Создать интерфейс
  acceptance: "Service interface создан с методом retweetTweet(UUID tweetId, RetweetRequestDto requestDto)"
  note: "Создан интерфейс RetweetService в services/tweet-api/src/main/java/com/twitter/service/RetweetService.java с методом retweetTweet(UUID tweetId, RetweetRequestDto requestDto). Метод документирован с описанием всех шагов бизнес-логики: валидация, создание Entity, сохранение в БД, обновление счетчика retweetsCount, маппинг в Response DTO. Определены исключения: BusinessRuleValidationException, UniquenessValidationException, FormatValidationException. Интерфейс создан по аналогии с LikeService, соответствует стандартам проекта."

### Эндпоинт: POST /api/v1/tweets/{tweetId}/retweet
- [x] (P1) [2026-01-05 16:40] #10: Validator реализация RetweetValidatorImpl — Реализовать все проверки валидации
  acceptance: "Методы валидации реализованы в RetweetValidatorImpl для эндпоинта: существование твита, существование пользователя, запрет self-retweet, проверка дублирования, валидация комментария"
  note: "Создана реализация RetweetValidatorImpl в services/tweet-api/src/main/java/com/twitter/validation/RetweetValidatorImpl.java. Реализован метод validateForRetweet с проверками: tweetId не null, существование твита (не удален), requestDto не null, userId не null, существование пользователя через UserGateway, запрет self-retweet, проверка уникальности ретвита, валидация комментария (null разрешен, но если не null - не пустая строка и не более 280 символов). Добавлены приватные методы: validateUserExists, validateNoSelfRetweet, validateUniqueness, validateComment. Все методы документированы JavaDoc. Реализация создана по аналогии с LikeValidatorImpl, соответствует стандартам проекта."
- [ ] (P1) #11: Service реализация RetweetServiceImpl — Реализовать бизнес-логику
  acceptance: "Методы добавлены в RetweetServiceImpl для эндпоинта, используют @Transactional, вызывают валидацию, обновляют счетчик retweetsCount"
- [ ] (P1) #12: OpenAPI интерфейс RetweetApi — Создать интерфейс с аннотациями
  acceptance: "Метод добавлен в RetweetApi интерфейс с @Operation, @ApiResponses, @ExampleObject для всех сценариев (201, 400, 404, 409)"
- [ ] (P1) #13: Controller RetweetController — Реализовать REST эндпоинт
  acceptance: "Метод добавлен в RetweetController с @PostMapping("/{tweetId}/retweet"), @LoggableRequest, @Valid, HttpStatus.CREATED"
- [ ] (P1) #14: JavaDoc для эндпоинта — Добавить JavaDoc для всех методов
  acceptance: "JavaDoc добавлен для всех методов эндпоинта с @author geron, @version 1.0, @param, @return, @throws"
- [ ] (P1) #15: Unit тесты для эндпоинта — Создать тесты для Service, Validator, Mapper
  acceptance: "Unit тесты для RetweetServiceImpl, RetweetValidatorImpl, RetweetMapper с учетом STANDART_TEST.md, все успешные и ошибочные сценарии"
- [ ] (P2) #16: Integration тесты для эндпоинта — Создать тесты с MockMvc
  acceptance: "Integration тесты для RetweetController с MockMvc, все статус-коды проверены (201, 400, 404, 409)"
- [ ] (P1) #17: Swagger документация для эндпоинта — Убедиться в полноте документации
  acceptance: "OpenAPI документация для эндпоинта полная с @ExampleObject для всех сценариев, все DTO имеют @Schema аннотации"

### Финальная инфраструктура
- [ ] (P2) #18: Обновление README.md — Обновить документацию
  acceptance: "README обновлен с учетом STANDART_README.md, все новые эндпоинты документированы на русском языке"
- [ ] (P2) #19: Обновление Postman коллекции — Добавить запрос retweet tweet
  acceptance: "Добавлен запрос retweet tweet с примерами ответов для всех эндпоинтов, обновлены переменные окружения"
- [ ] (P1) #20: Проверка соответствия стандартам — Проверить все стандарты
  acceptance: "Все стандарты проверены, код соответствует требованиям STANDART_CODE.md, STANDART_PROJECT.md, STANDART_TEST.md, STANDART_JAVADOC.md, STANDART_SWAGGER.md, STANDART_README.md, STANDART_POSTMAN.md"

## Assumptions
- Таблица tweets уже существует с полем retweets_count
- Пользователи проверяются через UserGateway (интеграция с users-api)
- Твиты проверяются через TweetRepository
- SQL скрипт будет выполнен до запуска приложения
- Комментарий опционален, но если указан, должен быть валидным (1-280 символов)
- Null комментарий разрешен, пустая строка - нет

## Risks
- **Технические**: Необходимость обновления счетчика retweetsCount в таблице tweets (нужно проверить, есть ли метод incrementRetweetsCount в TweetRepository)
- **Бизнес-логика**: Валидация комментария должна учитывать, что null и пустая строка - разные случаи (null разрешен, пустая строка - нет)
- **Производительность**: Уникальное ограничение на паре (tweet_id, user_id) должно быть проиндексировано

## Metrics & Success Criteria
- Все unit тесты проходят
- Все integration тесты проходят
- Покрытие кода > 80% для новых компонентов
- Swagger документация полная и корректная
- Postman коллекция обновлена с примерами
- README обновлен на русском языке
- Код соответствует всем стандартам проекта

## Notes
- Реализация по аналогии с функционалом Like (LikeController, LikeService, LikeValidator)
- Эндпоинт: POST /api/v1/tweets/{tweetId}/retweet
- Опциональный комментарий до 280 символов
- Бизнес-правила: запрет self-retweet, уникальность на паре (tweetId, userId), валидация комментария
- Ссылки на стандарты:
  - [STANDART_CODE.md](../../standards/STANDART_CODE.md)
  - [STANDART_PROJECT.md](../../standards/STANDART_PROJECT.md)
  - [STANDART_TEST.md](../../standards/STANDART_TEST.md)
  - [STANDART_JAVADOC.md](../../standards/STANDART_JAVADOC.md)
  - [STANDART_SWAGGER.md](../../standards/STANDART_SWAGGER.md)
  - [STANDART_README.md](../../standards/STANDART_README.md)
  - [STANDART_POSTMAN.md](../../standards/STANDART_POSTMAN.md)