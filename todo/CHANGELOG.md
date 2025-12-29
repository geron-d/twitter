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

- **2025-01-27** — step #4 done — Реализация Gateway для follower-api — автор: assistant
  - Создан FollowerGateway с методом getFollowingUserIds
  - Реализовано получение всех подписок через пагинацию (размер страницы 100)
  - Обработка ошибок с возвратом пустого списка (graceful degradation)
  - Логирование операций на уровнях debug и info
  - Следует паттерну UserGateway для консистентности
  - Файл: `services/tweet-api/src/main/java/com/twitter/gateway/FollowerGateway.java`

- **2025-01-27** — step #5 done — Реализация Repository метода — автор: assistant
  - Добавлен метод findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc в TweetRepository
  - Использует Derived Query Method Spring Data JPA для выполнения IN запроса по списку userIds
  - Возвращает Page<Tweet> с пагинацией и сортировкой по createdAt DESC
  - Исключает удаленные твиты (isDeleted = false)
  - Без JavaDoc согласно стандартам проекта
  - Файл: `services/tweet-api/src/main/java/com/twitter/repository/TweetRepository.java`

- **2025-01-27** — step #6.1 done — Обновление application.yml — автор: assistant
  - Добавлена секция app.follower-api.base-url в application.yml
  - Значение: http://localhost:8084 для локальной разработки
  - Настройка следует паттерну app.users-api.base-url
  - Файл: `services/tweet-api/src/main/resources/application.yml`

- **2025-01-27** — step #6.2 done — Обновление application-docker.yml — автор: assistant
  - Добавлена секция app.follower-api.base-url в application-docker.yml
  - Значение: http://follower-api:8084 для Docker окружения
  - Настройка следует паттерну app.users-api.base-url
  - Используется имя сервиса Docker (follower-api) вместо localhost
  - Файл: `services/tweet-api/src/main/resources/application-docker.yml`

- **2025-01-27** — step #6.3 done — Обновление docker-compose.yml — автор: assistant
  - Добавлена зависимость tweet-api от follower-api с condition: service_healthy
  - Добавлена переменная окружения FOLLOWER_API_URL=http://follower-api:8084
  - Конфигурация следует паттерну зависимости от users-api
  - Соответствует стандартам STANDART_DOCKER.md
  - Файл: `docker-compose.yml`

