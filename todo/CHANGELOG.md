# Changelog

## 2025-01-27

### tweet-api: Timeline endpoint implementation

- **2025-01-27** — step #1 done — Анализ требований для эндпоинта получения ленты новостей — автор: assistant
  - Выполнен полный анализ требований
  - Определены входные/выходные данные, бизнес-правила, зависимости от follower-api
  - Определены затронутые стандарты проекта
  - Определен список всех эндпоинтов tweet-api
  - Спроектирована интеграция с follower-api через Feign Client и Gateway паттерн
  - Создан документ: `todo/tweet/done/analysis-requirements.md`

- **2025-01-27** — step #2 done — Проектирование API и контрактов для эндпоинта получения ленты новостей — автор: assistant
  - Определена OpenAPI схема для эндпоинта getTimeline с полной документацией
  - Определена структура ответа (использование существующего TweetResponseDto)
  - Определен контракт с follower-api (эндпоинт, структура запроса/ответа, обработка ошибок)
  - Определены общие компоненты (переиспользование) и специфичные компоненты (новые)
  - Создан документ: `todo/tweet/done/design-api-contracts.md`

- **2025-01-27** — step #3 done — Реализация Feign клиента для follower-api — автор: assistant
  - Создан FollowerApiClient с методом getFollowing
  - Использует @FeignClient с конфигурацией name='follower-api', url из application.yml
  - Метод использует @SpringQueryMap для передачи Pageable параметров
  - Возвращает PagedModel<FollowingResponseDto>
  - Создан FollowingResponseDto в common-lib для межсервисной коммуникации
  - Файлы: `services/tweet-api/src/main/java/com/twitter/client/FollowerApiClient.java`, `shared/common-lib/src/main/java/com/twitter/common/dto/response/FollowingResponseDto.java`

