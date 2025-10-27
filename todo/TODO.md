# TODO: Create existsUser endpoint in users-api

## Meta
- project: twitter
- updated: 2025-01-27
- priority: P1
- type: feature
- changelog: todo/CHANGELOG.md

## Summary

Добавление оптимального endpoint `existsUser` в `users-api` для проверки существования пользователя. Цель — заменить неэффективный вызов `getUserById` в `tweet-api`, который возвращает полный объект пользователя и обрабатывает исключения, на легкий endpoint, возвращающий только boolean.

## Context Analysis

### Текущая ситуация

**Проблема:**
В `tweet-api/src/main/java/com/twitter/gateway/UserGateway.java` метод `existsUser()` использует тяжелый вызов:
```29:43:services/tweet-api/src/main/java/com/twitter/gateway/UserGateway.java
public boolean existsUser(UUID userId) {
    if (userId == null) {
        log.warn("Attempted to check existence of null user ID");
        return false;
    }
    
    try {
        usersApiClient.getUserById(userId);
        log.debug("User {} exists", userId);
        return true;
    } catch (Exception ex) {
        log.debug("User {} does not exist: {}", userId, ex.getMessage());
        return false;
    }
}
```

**Недостатки:**
1. Передача полного объекта User вместо boolean (лишняя нагрузка на сеть и сериализацию)
2. Обработка исключения как частого пути (антипаттерн)
3. Неявный способ передачи информации (404 = не существует)
4. Лишний маппинг в UserResponseDto

### Целевое состояние

Создать оптимизированный endpoint:
```
GET /api/v1/users/{userId}/exists
Response: { "exists": true/false }
```

Преимущества:
1. Явный контракт API
2. Минимальная передача данных
3. Высокая производительность
4. Чистый код без обработки исключений

## Tasks

### Phase 1: Analysis & Design (P1)

- [x] **(P1) #1: Проанализировать текущую архитектуру**  
  Выполнено: 2025-01-27  
  Описание: Изучить структуру users-api, понять взаимодействие с базой данных и особенности реализации.  
  Acceptance:
  - Изучена структура UserRepository и доступные методы
  - Определены шаблоны OpenAPI документации в проекте
  - Понятны зависимости между слоями (Controller -> Service -> Repository)
  
  **Результат анализа:**
  - UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User>
  - Доступен метод existsById(UUID id) из JpaRepository (уже есть из коробки)
  - OpenAPI настроен через @Operation, @ApiResponse аннотации в интерфейсах
  - Архитектура: Controller -> Service -> Repository (стандартный Spring Boot паттерн)
  - Используется слой валидации через UserValidator
  - MapStruct для маппинга DTO<->Entity (UserMapper)
  - Логирование через @LoggableRequest AOP аспект
  
- [x] **(P1) #2: Спроектировать API контракт**  
  Выполнено: 2025-01-27  
  Описание: Создать спецификацию endpoint existsUser с OpenAPI аннотациями.  
  Acceptance:
  - Определен HTTP метод (GET) и путь (/api/v1/users/{userId}/exists)
  - Спроектирован формат ответа (DTO с полем exists: boolean)
  - Добавлены OpenAPI аннотации с примерами и описаниями
  - Определены коды ответа (200 OK, 400 Bad Request)
  
  **Результат проектирования:**
  - Создана полная спецификация API контракта в `todo/tweet/exists-user-api-design.md`
  - Определен DTO: UserExistsResponseDto с полем boolean exists
  - HTTP метод: GET /api/v1/users/{userId}/exists
  - Коды ответа: 200 OK (всегда), 400 Bad Request (невалидный UUID)
  - Добавлены примеры OpenAPI аннотаций для UserApi интерфейса
  - Оценка производительности: 50-70% снижение времени ответа, 95%+ снижение payload
  - Определена стратегия интеграции с tweet-api (Feign Client)

- [x] **(P1) #3: Определить оптимизацию на уровне БД**  
  Выполнено: 2025-01-27  
  Описание: Выбрать эффективный способ проверки существования без загрузки данных.  
  Acceptance:
  - Выбран метод `existsById()` из JpaRepository (уже доступен)
  - Оценена производительность vs `findById().isPresent()`
  - Учтен вариант с добавлением кастомного SQL запроса если нужно
  
  **Результат:**
  - Используем `userRepository.existsById(id)` - встроенный метод JpaRepository
  - Генерируемый SQL: `SELECT 1 FROM users WHERE id = ? LIMIT 1` (оптимально)
  - Производительность: не загружает полную entity, только проверяет наличие
  - Кастомный SQL не требуется - стандартный метод достаточно эффективен

### Phase 2: Implementation in users-api (P1)

- [x] **(P1) #4: Создать DTO для ответа**  
  Выполнено: 2025-01-27  
  Описание: Создать UserExistsResponseDto в пакете dto.  
  Acceptance:
  - Создан класс UserExistsResponseDto
  - Добавлено поле boolean exists
  - Добавлен Javadoc с описанием назначения DTO
  - Используется @Schema аннотации для OpenAPI
  
  **Результат:**
  - Создан файл: `services/users-api/src/main/java/com/twitter/dto/UserExistsResponseDto.java`
  - Использован record тип (современный подход Java)
  - Добавлен полный JavaDoc согласно стандартам проекта
  - Настроены @Schema аннотации для OpenAPI документации
  - DTO минималистичный и оптимизированный для передачи только boolean значения

- [x] **(P1) #5: Добавить метод в UserService интерфейс**  
  Выполнено: 2025-01-27  
  Описание: Добавить метод `boolean existsById(UUID id)` в `services/users-api/.../UserService.java`.  
  Acceptance:
  - Добавлен метод в интерфейс с документацией
  - Указано возвращаемое значение boolean
  - Добавлен Javadoc с описанием поведения
  
  **Результат:**
  - Добавлен метод boolean existsById(UUID id) в интерфейс UserService
  - Добавлен полный Javadoc с описанием функциональности
  - Указано, что метод выполняет эффективную проверку без загрузки entity
  - Описано использование repository's existsById с оптимизированным SQL

- [x] **(P1) #6: Реализовать метод в UserServiceImpl**  
  Выполнено: 2025-01-27  
  Описание: Реализовать логику в `services/users-api/.../UserServiceImpl.java`.  
  Acceptance:
  - Использован `userRepository.existsById(id)` для эффективной проверки
  - Добавлена обработка edge cases (null, etc.)
  - Добавлено логирование операций
  - Обеспечена обработка возможных исключений
  
  **Результат:**
  - Добавлена реализация метода existsById в UserServiceImpl
  - Использован userRepository.existsById(id) для оптимальной производительности
  - Добавлена проверка на null с обработкой edge cases
  - Добавлено логирование для debugging и мониторинга
  - Метод использует @Override аннотацию

- [ ] **(P1) #7: Добавить метод в UserApi интерфейс**  
  Описание: Добавить endpoint в OpenAPI интерфейс с аннотациями.  
  Статус: To Do  
  Acceptance:
  - Добавлен метод в UserApi интерфейс
  - Настроены OpenAPI аннотации (@Operation, @ApiResponses)
  - Добавлены примеры запросов и ответов
  - Указаны параметры и коды ответа

- [ ] **(P1) #8: Реализовать endpoint в UserController**  
  Описание: Добавить обработчик GET запроса в контроллер.  
  Статус: To Do  
  Acceptance:
  - Добавлен метод в UserController
  - Использован сервисный слой для получения данных
  - Настроен маппинг ответа в DTO
  - Добавлен @LoggableRequest для логирования

### Phase 3: Integration in tweet-api (P2)

- [ ] **(P2) #9: Добавить метод existsUser в UsersApiClient**  
  Описание: Расширить Feign клиент в tweet-api.  
  Статус: To Do  
  Acceptance:
  - Добавлен метод в UsersApiClient интерфейс
  - Настроен правильный путь /api/v1/users/{userId}/exists
  - Добавлены необходимые аннотации Feign
  - Возвращает только boolean без DTO

- [ ] **(P2) #10: Обновить UserGateway в tweet-api**  
  Описание: Изменить метод existsUser для использования нового endpoint.  
  Статус: To Do  
  Acceptance:
  - Заменен вызов getUserById на existsUser
  - Убрана обработка исключений из try-catch блока
  - Сохранена проверка на null
  - Улучшено логирование

### Phase 4: Testing & Documentation (P2)

- [ ] **(P2) #11: Написать unit тесты для UserServiceImpl.existsById**  
  Описание: Создать тесты для нового метода сервиса.  
  Статус: To Do  
  Acceptance:
  - Тест на существующего пользователя возвращает true
  - Тест на несуществующего пользователя возвращает false
  - Тест на null userId обрабатывается корректно
  - Покрытие кода > 90%

- [ ] **(P2) #12: Написать integration тесты для нового endpoint**  
  Описание: Создать тесты для UserController.  
  Статус: To Do  
  Acceptance:
  - HTTP тест с реальной базой данных
  - Проверка статус кода 200
  - Проверка корректности JSON ответа
  - Проверка логирования запросов

- [ ] **(P2) #13: Обновить документацию API**  
  Описание: Добавить описание нового endpoint в документацию.  
  Статус: To Do  
  Acceptance:
  - Swagger UI отображает новый endpoint
  - OpenAPI spec содержит корректные метаданные
  - Добавлены примеры использования в README

- [ ] **(P2) #14: Добавить Postman тесты**  
  Описание: Создать запросы для ручного тестирования.  
  Статус: To Do  
  Acceptance:
  - Добавлен запрос в Postman коллекцию
  - Настроена переменная окружения
  - Тестированы успешные и негативные сценарии

### Phase 5: Performance & Monitoring (P3)

- [ ] **(P3) #15: Провести нагрузочное тестирование**  
  Описание: Сравнить производительность getUserById vs existsUser.  
  Статус: To Do  
  Acceptance:
  - Измерено время выполнения обоих методов
  - Сравнен объем передачи данных по сети
  - Проверено влияние на базу данных
  - Документирован выигрыш в производительности

- [ ] **(P3) #16: Добавить метрики в Micrometer**  
  Описание: Инструментировать endpoint для мониторинга.  
  Статус: To Do  
  Acceptance:
  - Метрика количества вызовов existsUser
  - Метрика времени выполнения
  - Метрика количества успешных/неуспешных проверок
  - Интеграция с Prometheus

### Phase 6: Deployment & Rollout (P1)

- [ ] **(P1) #17: Локальное тестирование**  
  Описание: Проверить работу на локальной среде.  
  Статус: To Do  
  Acceptance:
  - Оба сервиса запускаются без ошибок
  - API тестируется через Postman/Swagger
  - Логи не содержат ошибок
  - Все unit и integration тесты проходят

- [ ] **(P1) #18: Обновить версию API**  
  Описание: Обновить версионирование API при необходимости.  
  Статус: To Do  
  Acceptance:
  - Определено необходимость versioning
  - Обновлен API version если требуется
  - Обеспечена обратная совместимость

- [ ] **(P1) #19: Собрать и протестировать Docker образы**  
  Описание: Собрать образы для деплоя.  
  Статус: To Do  
  Acceptance:
  - Образ users-api собран успешно
  - Образ tweet-api собран успешно
  - Приложение запускается в Docker контейнерах
  - Взаимодействие между сервисами работает

- [ ] **(P1) #20: Деплой в тестовую среду**  
  Описание: Развернуть изменения в тестовом окружении.  
  Статус: To Do  
  Acceptance:
  - Деплой выполнен без ошибок
  - Проведено smoke тестирование
  - Проверена работа в production-like окружении
  - Метрики в норме

## Assumptions

### Технические
- JpaRepository.existsById() достаточен для эффективной проверки
- OpenAPI аннотации уже настроены в проекте
- Feign Client корректно работает между сервисами
- Database connection pool настроен адекватно

### Архитектурные
- API останется REST-based синхронное взаимодействие
- Версионирование API не требуется (breaking change отсутствует)
- Обратная совместимость гарантирована
- Shared libraries не требуют изменений

### Организационные
- Разработчик имеет доступ ко всем сервисам
- CI/CD пайплайн настроен и работает
- Есть доступ к тестовой среде для деплоя
- Регрессионное тестирование существующих endpoint'ов не требуется

### Производительность
- Уменьшение времени ответа на 40-60%
- Снижение нагрузки на сеть на 90%+
- Нет негативного влияния на производительность БД
- TTL кэширования не требуется на данном этапе

## Potential Risks & Mitigations

### Технические риски

**Риск 1: Ретроградация существующих интеграций**
- Вероятность: Low
- Описание: Другие сервисы могут вызывать getUserById напрямую
- Митигация: Новый endpoint - дополнительный, не ломает существующий API
- Ответственный: Developer
- Статус: Acceptable

**Риск 2: Проблемы с транзакционностью**
- Вероятность: Low
- Описание: existsById может быть не atomic для распределенных транзакций
- Митигация: Использовать existsById, который оптимизирован на уровне JPA/Hibernate
- Ответственный: Developer
- Статус: Monitor

**Риск 3: Рост количества HTTP запросов**
- Вероятность: Medium
- Описание: endpoints могут вызываться чаще из-за упрощения
- Митигация: Добавить мониторинг вызовов, рассмотреть кэширование при необходимости
- Ответственный: DevOps
- Статус: Mitigate

### Архитектурные риски

**Риск 4: Скрытые зависимости между сервисами**
- Вероятность: Low
- Описание: tight coupling между tweet-api и users-api через лишние вызовы
- Митигация: Использовать async события (Kafka) в будущем
- Ответственный: Architect
- Статус: Future Work

**Риск 5: Недостаточная валидация данных**
- Вероятность: Very Low
- Описание: users-api не проверяет активность пользователя (ACTIVE vs INACTIVE)
- Митигация: Реализовать бизнес-логику проверки статуса в existsUser
- Ответственный: Developer
- Статус: Consider

### Организационные риски

**Риск 6: Отсутствие тестового покрытия**
- Вероятность: Medium
- Описание: Регрессия при изменении кода
- Митигация: Написать unit и integration тесты перед кодом
- Ответственный: QA + Developer
- Статус: Mitigate

**Риск 7: Недостаточная документация**
- Вероятность: Low
- Описание: Сложность поддержки кода
- Митигация: Добавить подробный Javadoc, обновить README
- Ответственный: Developer
- Статус: Mitigate

## Metrics & Success Criteria

### Метрики производительности
- **Целевой показатель**: Сокращение времени ответа на 50%
- **Текущий baseline**: ~50-100ms (getUserById)
- **Целевой**: ~20-40ms (existsUser)
- **Метод измерения**: Измерение P95 latency через Micrometer

### Метрики качества
- **Целевой показатель**: Покрытие тестами > 90%
- **Инструменты**: JaCoCo + JUnit 5
- **Автоматизация**: CI пайплайн проверяет порог

### Метрики использования
- **Целевой показатель**: Adoption rate > 80% в течение 1 месяца
- **Метод измерения**: Подсчет вызовов через Micrometer metrics
- **Отслеживание**: Grafana dashboard

### Критерии успешности
1. ✅ Новый endpoint работает корректно
2. ✅ Тесты проходят (unit + integration)
3. ✅ Производительность улучшена минимум на 40%
4. ✅ Backward compatibility сохранена
5. ✅ Документация обновлена
6. ✅ Деплой в тестовую среду успешен
7. ✅ Нет регрессий существующих функциональностей

## Alternative Approaches

### Альтернатива 1: Batch exists check
**Идея**: Endpoint для проверки множества пользователей за раз  
**Преимущества**: 
- Меньше HTTP запросов
- Batch processing оптимизация

**Недостатки**: 
- Усложнение API
- Вероятно преждевременная оптимизация

**Решение**: Reject (не приоритетно на данном этапе)

### Альтернатива 2: Кэширование на клиенте
**Идея**: Кэшировать результаты вызовов в памяти  
**Преимущества**: 
- Резкое снижение вызовов к users-api
- Улучшение latency

**Недостатки**: 
- Неактуальные данные при удалении пользователя
- Сложность invalidation стратегии
- Memory overhead

**Решение**: Reject (вероятно избыточно на данном этапе)

### Альтернатива 3: GraphQL вместо REST
**Идея**: Использовать GraphQL для гибкости запросов  
**Преимущества**: 
- Возможность запрашивать только нужные поля
- Множественные запросы в одном вызове

**Недостатки**: 
- Радикальное изменение архитектуры
- Высокая сложность внедрения
- Текущий стек на REST

**Решение**: Reject (не в scope текущей задачи)

### Альтернатива 4: Event-driven проверка
**Идея**: Использовать Apache Kafka для async проверки  
**Преимущества**: 
- Decoupling сервисов
- Масштабируемость

**Недостатки**: 
- Высокая сложность внедрения
- Необходимы изменения в текущей архитектуре
- Eventual consistency вопросы

**Решение**: Defer (будущая эволюция архитектуры)

## Notes

- **Ссылки на архитектуру**: 
  - Архитектура tweet-api: `todo/tweet/TWEET_API_ARCHITECTURE.md`
  - Общий план: `todo/tweet/TWEET_API_COMMON.md`

- **Стандарты кода**: 
  - Javadoc стандарты: `standards/JAVADOC_STANDARDS.md`
  - Javadoc templates: `standards/JAVADOC_TEMPLATES.md`

- **Похожие задачи**: 
  - Tweet API общий план: `todo/tweet/TWEET_API_COMMON_2.md`
  - Tweet API NEW: `todo/tweet/TWEET_API_COMMON_3.md`

- **Технологии**: 
  - Java 24
  - Spring Boot 3.5.5
  - Spring Cloud OpenFeign
  - PostgreSQL 15

- **Зависимости**: 
  - `shared/common-lib` - общая библиотека с исключениями и AOP
  - `shared/database` - общие компоненты БД

- **Важные контексты**:
  - Проект использует монорепозиторий
  - Централизованное управление зависимостями
  - MapStruct для маппинга объектов
  - TestContainers для интеграционных тестов

## Timeline

- **Анализ и проектирование**: 2-3 часа
- **Реализация в users-api**: 4-6 часов
- **Интеграция в tweet-api**: 2-3 часа
- **Тестирование**: 3-4 часа
- **Документация**: 1-2 часа
- **Деплой**: 2-3 часа

**Итого**: ~14-21 час

## Dependencies

### Внешние зависимости
- Spring Boot 3.5.5
- Spring Data JPA
- Spring Cloud OpenFeign

### Внутренние зависимости
- shared/common-lib (исключения, логирование)
- shared/database (если потребуется)

### Блокировки
- Нет блокирующих зависимостей от других задач

## Team & Stakeholders

- **Developer**: Реализация кода
- **QA**: Тестирование (unit + integration)
- **DevOps**: Настройка CI/CD и деплой
- **Product**: Принятие решения о приоритетности

## References

- Spring Data JPA existsById: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods
- Feign Client: https://cloud.spring.io/spring-cloud-openfeign/reference/html/
- OpenAPI 3: https://swagger.io/specification/
- Javadoc Standards: `/standards/JAVADOC_STANDARDS.md`

