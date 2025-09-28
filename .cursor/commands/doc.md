Цель: подготовить подробную, структурированную документацию для сервиса на Java 24 + Spring Boot 3 и сохранить её в файл README.md в корне репозитория.

ВАЖНО: думай очень глубоко и многослойно. На каждом шаге задавай себе "почему?" и "а что если?".
Используй техники: 5 Whys, FMEA (failure modes), threat modelling, и runbook-thinking (как восстановить систему в проде).
Не пиши код — только анализ, описания, примеры запросов/ответов и текстовые диаграммы.

1) Входные предпосылки (если ты агент, выполняющий на репозитории):
    - Возьми: src/main/java, src/main/resources, pom.xml или build.gradle, Dockerfile, kubernetes manifests, tests.
    - Если есть OpenAPI / swagger.yaml — используй как исходник.
    - Иначе — ищи @RestController, @Controller, @Service, @Repository, @Entity, application.yml/properties.

2) Формат итогового README.md (обязательный):
    - Введение: назначение сервиса, контекст, владельцы, краткое summary (1–3 абзаца).
    - Архитектура: список компонентов, их роли, зависимости, текстовые диаграммы (классов и последовательности).
    - API: таблица всех эндпоинтов (см. шаблон ниже).
    - Бизнес-логика: по каждому сервису — назначение, ключевые методы, поведение, side-effects.
    - Работа с БД: сущности, таблицы, связи, индексы, репозитории.
    - Интеграции: внешние системы, контракты, retry/backoff, timeouts.
    - Нефункциональные требования: SLA, latency, throughput, RPO/RTO.
    - Безопасность: auth/authz, уязвимости, шифрование, секреты.
    - Observability: метрики, трассировка, логирование, алерты.
    - Развертывание: Docker, k8s, env vars, конфиги, CI/CD.
    - Тестирование: какие тесты есть, как запускать (unit/integration), покрытия.
    - Оценка рисков (FMEA): список рисков с приоритетом/вероятностью/mitigation.
    - Migration / Rollback план.
    - Definition of Done (конкретные acceptance-criteria).
    - Примеры использования: cURL/HTTP request-response примеры.
    - Assumptions & Unknowns: что не было найдено/недостаточно информации.
    - Appendix: полезные команды для разработчика, ссылки, TODO.

3) Шаблон описания компонента (обязателен — одинаковый для всех классов/контроллеров/репозиториев):
    - Name: полное имя класса (FQN) и путь файла.
    - Type: Controller / Service / Repository / Entity / DTO / Config / Util.
    - Responsibility: 1–2 предложения.
    - Public API: для каждого публичного метода — `signature (без кода)`: purpose, inputs, outputs, side-effects, expected exceptions, transactional/caching behaviour.
    - Dependencies: internal / external (other services, DB, queues).
    - Tests: unit/integration tests presence (путь к тестам).
    - Risks / Notes / TODO (P1/P2/P3).
    - Example usage snippet (HTTP request / response or sequence of calls) — текстовый пример, не исполняемый код.

4) Шаблон описания REST эндпоинта (таблица или повторяемая карточка):
    - Endpoint: `GET|POST|PUT|DELETE` path
    - Auth: none / JWT / OAuth2 / API Key
    - Params: path/query/header (name: type — required/optional — description)
    - Request body: JSON schema summary (fields + types + required)
    - Response: status codes (200, 201, 4xx, 5xx) + schema + example (JSON)
    - Errors: possible error responses and causes
    - Idempotency / Caching / Pagination / Rate limits
    - Performance expectations (p95 latency target)
    - Example cURL request + response (JSON) — коротко

5) Работа с БД — шаблон для каждой сущности:
    - Entity: имя (FQN), таблица
    - Fields: name: type, nullable, constraints, default
    - PK, FK, unique constraints, indexes
    - Relations: OneToMany, ManyToOne, join tables
    - Migrations: где находятся (Liquibase/Flyway) и последний migration file
    - Repositories: тип (Spring Data JpaRepository), кастомные запросы (JPQL/SQL) — что делают

6) UML / Диаграммы (текстовые):
    - Предложи PlantUML-версию диаграмм (класс/sequence) как опцию (если запрет на "код" — используешь ASCII-диаграммы).
    - Дай краткую инструкцию: «если хочешь, вставь этот PlantUML в онлайн-рендерер».

7) Глубокий анализ (обязательно — думай очень сильно):
    - Failure modes: перечисли возможные отказные сценарии (DB outage, downstream SLA degrade, message loss) + вероятность + влияние + mitigation.
    - Concurrency & Consistency: возможные гонки, транзакции, изоляция, stale reads, idempotency.
    - Security threats: injection, auth bypass, sensitive data leak — предложи mitigations.
    - Performance hotspots: потенциальные узкие места, N+1 queries, heavy joins, long-running transactions, connection pool.
    - Operational readiness: backup, restore, schema migration plan, runbook шаги.
    - Backward compatibility: старые клиенты / schema-evolution проблемы.

8) Риски и приоритеты
    - Для каждого найденного риска укажи: severity (P1 — критично, P2 — важно, P3 — низкий), probability (High/Med/Low), mitigation steps, owner (рекомендация).

9) Definition of Done (конкретно для README.md)
    - README содержит все разделы из пункта (2).
    - Все эндпоинты перечислены и имеют пример запроса/ответа.
    - Все сущности и репозитории описаны.
    - Есть список рисков и план mitigations.
    - Есть list of commands, как поднять сервис и запустить тесты.
    - Acceptance: продуктовая команда или тимлид подтверждает покрытие знаний — checklist all checked.

10) Output
- Генерируй README.md в Markdown (чёткая структура, оглавление).
- Дополнительно: если найдено OpenAPI/Swagger — включи ссылку/краткий экспорт (YAML/JSON) в appendix.
- Если не хватает данных — в разделе Assumptions & Unknowns явно укажи, что необходимо получить (например: credentials, external API specs).

11) Доп. инструкции для исполнителя/агента
- Уровни детализации: сначала summary (1–3 предложения) для каждого компонента, затем детальный блок (до 200–400 слов).
- Указывай точные пути файлов и строки примеров (где возможно).
- Не пытайся генерировать реальные секреты/ключи/пароли.
- Помни: не писать исполняемый код — только примеры запросов/ответов и текстовые диаграммы.

12) Если уместно, предложи альтернативные архитектурные варианты (2–3) с trade-offs (cost, complexity, effort, risk).

ПРИМЕЧАНИЕ: Если хочешь — предоставь доступ к репозиторию или загрузи архив, и я (или агент) сгенерирую README.md по этому промпту и сохраню в корне.
