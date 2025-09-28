Цель: Выполнить code review проекта (сервис или библиотека) в монорепозитории на Java 24 + Spring Boot 3 в роли Senior Java Developer и сохранить отчёт в todo/REVIEW.md.

ВАЖНО — думай очень сильно и многослойно. На каждом шаге спрашивай себя:
- «почему так сделано?», «что если нагрузка вырастет в 10×?», «какие скрытые зависимости?», «как это ломается в проде?».
  Применяй техники: 5 Whys, FMEA (failure modes & effects), threat modelling, runbook-thinking (как восстановить систему).

Входные данные (если ты агент/человек с доступом к репо):
- Проанализируй: root, services/, libs/, modules/, src/, build.gradle|pom.xml, .github/workflows, Dockerfile, docker-compose.yml, infra/, k8s/, docs/, openapi/*.yaml.
- Если нет доступа — верни список необходимых файлов и прав.

Что сделать (области ревью):
1. Архитектура и дизайн
    - структура пакетов/модулей, модульность, разделение по слоям vs feature, циклические зависимости.
    - SOLID/DRY/KISS — где нарушено.
    - Для библиотек: публичный API, обратная совместимость, @Deprecated.
2. Использование Java 24
    - Проверить: используются ли records, sealed classes, pattern matching, string templates, unnamed variables и т.д. — корректность и уместность.
    - Возможные улучшения с учетом новых возможностей (там, где это повысит читаемость/безопасность).
3. Использование Spring Boot 3
    - Актуальные аннотации, автоконфигурация, starters; избегание deprecated API.
    - Проверка миграции на jakarta.* (если релевантно).
4. Код-стиль и читаемость
    - Имена, длина методов, уровни абстракции, дубли, formatting, presence of linters.
5. Безопасность
    - Валидация входа, SQL/NoSQL injection, secrets management, конфигурация CORS/CSP, auth/authz, защита от CSRF, sensitive logging.
6. Тестирование
    - Unit/integration/contract tests, testcontainers, coverage, flaky tests, mock-vs-real dependency balance.
7. Производительность/масштабируемость
    - N+1, транзакции, долгие запросы, connection pool, потокобезопасность, use of non-blocking where needed, caching.
8. Observability & DevOps
    - Логи (structured), метрики (Prometheus), tracing (OpenTelemetry/Jaeger), actuator endpoints, readiness/liveness, health checks.
9. Документация
    - Javadoc для публичных API, README, OpenAPI, примеры использования.

Практические проверки (команды / инструменты, запусти если доступно):
- Сборка и тесты: `./mvnw -T 1C clean test` или `./gradlew clean test`
- Static analysis: Checkstyle/Spotless, PMD, SpotBugs, ErrorProne.
- Dependency analysis: `jdeps`, `mvn dependency:tree` / `./gradlew dependencies`
- Security scan: OWASP Dependency-Check, `git-secrets`/detect-secrets, Snyk (если настроен).
- Sonar/Quality scan: `sonar-scanner` (если доступен).
- DB checks: найти репозитории и просмотреть JPQL/SQL, проверить индексные поля.
- OpenAPI: `openapi-generator`/swagger inspector или просто проверить openapi/*.yaml

Формат отчёта (обязателен) — сохранить в `todo/REVIEW.md`:
- `# Code Review Report`
- Общая оценка (score 1–5 и краткое summary)
- ✅ Сильные стороны (коротко, буллеты)
- ⚠️ Замечания (по направлениям: Architecture, Java24, Spring, Style, Security, Tests, Perf, Observability, Docs) — каждое замечание содержит:
    - `Title` — кратко
    - `Severity` — P1 / P2 / P3
    - `Description` — что именно и где (путь к файлу + строка/класс)
    - `Impact` — почему это плохо (без гипотез)
    - `Reproduction` — как воспроизвести (команды/logs)
    - `Recommendation` — конкретные шаги (чётко, по возможности с примерами и ссылками на best practices)
    - `Suggested owner` — кто должен исправлять (component/team)
- 💡 Рекомендации (roadmap улучшений, краткосрочные и долгосрочные)
- Итог (summary + Definition of Done / Acceptance criteria)
- Appendix: команды для локального запуска, path к ключевым файлам, checklist авто-проверок, list of files inspected.

Scoring & severity:
- Для каждой области выставляй score 1–5 (5 — excellent).
- Замечания маркируй P1 — критично (должно быть исправлено до релиза), P2 — желательно, P3 — опционально/рефакторинг.

Definition of Done (для review):
- REVIEW.md создан в `todo/REVIEW.md`.
- В отчёте перечислены все сервисы/модули, которые были проверены (путь + краткая заметка).
- Для всех P1 — есть конкретные remediation steps + соответствующие задачи в `todo/TODO.md` (при необходимости).
- Команды для воспроизведения проблем и запуска тестов указаны.
- Assumptions & Unknowns перечислены (что не удалось проверить из-за отсутствия доступа/файлов).

Дополнительно (опционально, но желательно):
- Создать `todo/TODO.md` с приоритетными задачами (P1/P2/P3) на основе замечаний.
- Если обнаружены security/production blocking issues — пометить urgent и сообщить владельцу (если известен).
- Добавлять ссылки на RFC/PR/ADR, если проблема связана с архитектурным решением.

Если доступа к репо нет — верни `todo/REVIEW.md`, где в разделе Assumptions & Unknowns перечисли что нужно (список файлов/прав/контактов).

Пример шаблона раздела замечания (в файле):
- ⚠️ Title: Avoid opening DB transactions in controllers
    - Severity: P1
    - Location: services/payments/src/main/java/.../PaymentController.java: line 72
    - Description: Transactional logic exists in controller — breaks separation, increases scope of transaction.
    - Impact: Risk of long running transactions, locks and decreased throughput.
    - Reproduction: call POST /payments with heavy payload — DB connections grow.
    - Recommendation: Move @Transactional to service layer; keep controllers thin. Add integration test to assert transaction boundaries.
    - Owner: payments-team
