# TODO: Реализация эндпоинта "Убрать лайк"

## Meta
- project: twitter-microservices
- updated: 2025-01-27
- changelog: todo/CHANGELOG.md
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
- [X] (P1) #22: Анализ требований — Определить входные/выходные данные, бизнес-правила, затронутые стандарты
  acceptance: "Понять вход/выход (tweetId, userId, 204 No Content), бизнес-правила (существование лайка, обновление счетчика), определить затронутые стандарты"
- [x] (P1) #23: Проектирование API и контрактов — Определить структуру валидации и бизнес-логики
  acceptance: "OpenAPI схема для DELETE эндпоинта, определение методов валидации (validateForUnlike), определение методов сервиса (removeLike), переиспользование существующих DTO"
  note: "Выполнено проектирование API и контрактов. Определена OpenAPI схема для DELETE эндпоинта с полной документацией всех статус-кодов и примеров. Определена структура метода removeLike в LikeService и LikeServiceImpl. Определена структура метода decrementLikesCount в Entity Tweet. Подтверждено переиспользование LikeTweetRequestDto. Создан документ api_design.md с детальным проектированием. Выполнено: 2025-01-27"

### Реализация инфраструктуры и конфигов
- [x] (P1) #24: Инфраструктура уже существует — Entity, Repository, DTO, Mapper
  acceptance: "Все компоненты инфраструктуры уже созданы для POST /api/v1/tweets/{tweetId}/like, переиспользуются для DELETE"
  note: "Entity Like, LikeRepository, LikeTweetRequestDto, LikeMapper, LikeService, LikeController, LikeApi уже существуют. Не требуется создание новой инфраструктуры, только расширение существующих интерфейсов и реализаций."

### Эндпоинт: DELETE /api/v1/tweets/{tweetId}/like
- [x] (P1) #25: DTO для эндпоинта — Переиспользование существующего LikeTweetRequestDto
  acceptance: "Используется существующий LikeTweetRequestDto (не требуется создание нового DTO)"
  note: "DELETE операция использует тот же DTO, что и POST (содержит userId). Не требуется создание нового DTO."
- [x] (P1) #26: Mapper методы для эндпоинта — Не требуется
  acceptance: "DELETE операция не требует маппинга (возвращает 204 No Content без тела ответа)"
  note: "DELETE операция не требует маппинга, так как возвращает 204 No Content без тела ответа."
- [x] (P1) #27: Validator методы для эндпоинта — Добавить метод validateForUnlike
  acceptance: "Метод validateForUnlike добавлен в LikeValidator interface и implementation (проверка существования твита, пользователя, лайка)"
  note: "Метод validateForUnlike добавлен в LikeValidator интерфейс и LikeValidatorImpl реализацию. Валидация включает проверку tweetId, существования твита, requestDto, userId, существования пользователя и существования лайка. Добавлен приватный метод validateLikeExists для проверки существования лайка перед удалением. Выполнено: 2026-01-02 00:41"
- [x] (P1) #28: Service методы для эндпоинта — Добавить метод removeLike
  acceptance: "Метод removeLike добавлен в LikeService interface и implementation, использует @Transactional, удаляет лайк, обновляет счетчик"
  note: "Добавлен метод removeLike() в интерфейс LikeService с полной JavaDoc документацией. Реализован метод removeLike() в LikeServiceImpl с использованием @Transactional для атомарности операции. Добавлен метод decrementLikesCount() в Entity Tweet с защитой от отрицательных значений. Все методы соответствуют стандартам проекта. Выполнено: 2025-01-27"
- [x] (P1) #29: Controller метод для эндпоинта — Добавить метод removeLike в LikeApi и LikeController
  acceptance: "Метод removeLike добавлен в LikeApi интерфейс с OpenAPI аннотациями и в LikeController с @LoggableRequest, возвращает 204 No Content"
  note: "Добавлен метод removeLike() в интерфейс LikeApi с полной OpenAPI документацией (все статус-коды 204, 400, 404 с примерами). Реализован метод removeLike() в LikeController с @LoggableRequest и @DeleteMapping. Метод возвращает 204 No Content. Все соответствует стандартам проекта (STANDART_CODE, STANDART_SWAGGER). Выполнено: 2025-01-27"
- [ ] (P1) #30: JavaDoc для эндпоинта — Добавить JavaDoc для всех методов
  acceptance: "JavaDoc добавлен для всех методов эндпоинта с @author geron, @version 1.0, @param, @return, @throws"
- [ ] (P1) #31: Unit тесты для эндпоинта — Создать unit тесты для Service и Validator
  acceptance: "Unit тесты для Service и Validator методов с учетом STANDART_TEST.md (naming pattern, @Nested, AssertJ)"
- [ ] (P2) #32: Integration тесты для эндпоинта — Создать integration тесты с MockMvc
  acceptance: "Integration тесты для эндпоинта с MockMvc, все статус-коды проверены (204, 400, 404, 409)"
- [ ] (P1) #33: Swagger документация для эндпоинта — Добавить OpenAPI аннотации
  acceptance: "OpenAPI документация для эндпоинта полная с @ExampleObject для всех сценариев (успех 204, ошибки валидации, конфликты)"

### Финальная инфраструктура
- [ ] (P2) #34: Обновление README.md — Обновить документацию сервиса
  acceptance: "README обновлен с учетом STANDART_README.md, эндпоинт DELETE документирован в разделе REST API и Примеры использования"
- [ ] (P2) #35: Обновление Postman коллекции — Добавить запрос в коллекцию
  acceptance: "Добавлен запрос 'remove like' с примерами ответов для всех сценариев (204, 400, 404, 409), обновлены переменные окружения"
- [ ] (P1) #36: Проверка соответствия стандартам — Проверить все стандарты
  acceptance: "Все стандарты проверены, код соответствует требованиям (STANDART_CODE, STANDART_PROJECT, STANDART_TEST, STANDART_JAVADOC, STANDART_SWAGGER)"

## Assumptions
- Используется существующий DTO `LikeTweetRequestDto` для DELETE операции (содержит userId)
- DELETE операция возвращает 204 No Content без тела ответа
- Удаление лайка должно быть атомарным (транзакция)
- Счетчик лайков твита обновляется автоматически при удалении (decrementLikesCount)
- Валидация проверяет существование лайка перед удалением
- Пользователь может убрать только свой лайк (проверка через userId в request body)

## Risks
- **Технические риски**: 
  - Race condition при одновременном удалении лайка несколькими запросами (митигация: транзакция с правильным уровнем изоляции)
  - Несогласованность счетчика лайков при ошибках (митигация: транзакция, проверка в тестах)
- **Организационные риски**: 
  - Недостаточное покрытие тестами (митигация: обязательные unit и integration тесты)
- **Зависимости**: 
  - Интеграция с users-api для проверки существования пользователя (митигация: Circuit Breaker, fallback стратегии)

## Metrics & Success Criteria
- Все unit тесты проходят (покрытие > 80% для новых методов)
- Все integration тесты проходят
- Эндпоинт возвращает правильные HTTP статус-коды (204, 400, 404, 409)
- Счетчик лайков корректно обновляется в БД (decrementLikesCount)
- OpenAPI документация полная и корректная
- Postman коллекция обновлена с примерами
- README обновлен на русском языке
- Код соответствует всем стандартам проекта

