Цель: Выполнить качественное code review проекта (сервис или библиотека) в монорепозитории на Java 24 + Spring Boot 3 и сохранить отчёт в `todo/REVIEW.md`.

ВАЖНО — ДУМАЙ ОЧЕНЬ СИЛЬНО И ГЛУБОКО.  
На каждом шаге спрашивай себя: «почему так сделано?», «что если нагрузка вырастет в 10×?», «какие скрытые зависимости?», «какие сценарии отказа?», «кто владелец/ответственный?».  
Используй техники: **5 Whys**, **FMEA (Failure Modes & Effects Analysis)**, **threat modelling**, **runbook-thinking** (как восстановить систему в проде).

Входные данные (если ты агент с доступом к репозиторию):
- Репо root; ищи: `services/`, `libs/`, `modules/`, `src/`, `pom.xml`/`build.gradle`, `.github/workflows`, `Dockerfile`, `docker-compose.yml`, `infra/`, `k8s/`, `docs/`, `openapi/*.yaml`.
- Существующая per-service документация — агрегируй ссылки/пути.
- Конфиги: `application*.yml`, profiles, secrets configuration.

Если доступа нет — верни в `Assumptions & Unknowns` список необходимых файлов/прав/контактов.

---

## Что проверить (области ревью — обязательно покрыть все)
1. **Архитектура проекта**
    - Структура монорепозитория (разделение на сервисы/библиотеки, shared modules).
    - Управление версиями и зависимостями (BOM, dependency management).
    - Module boundaries, cyclic dependencies, packaging strategy.

2. **Взаимодействие сервисов/библиотек**
    - Консистентность API (contracts / OpenAPI).
    - Reuse of shared libs vs. duplication.
    - Clear SRP и границы ответственности.

3. **Использование возможностей Java 24**
    - Проверить целесообразное применение: `record`, `sealed` classes, pattern matching, string templates, unnamed variables и др.
    - Предложения там, где использование новых фич повысит ясность/безопасность.

4. **Использование Spring Boot 3**
    - Автоконфигурация, starters, современный стиль конфигов (Properties+@ConfigurationProperties), отсутствие deprecated API.
    - Правильная работа с `jakarta.*` (если релевантно).

5. **Качество кода**
    - Кодстайл / форматирование / единообразие между модулями.
    - Слишком длинные методы, дубли, глубокие вложенности.
    - Публичные API библиотеки: семантика, javadoc, backward compatibility.

6. **Безопасность**
    - Секреты: где хранятся, нет ли захардкоженных ключей.
    - Валидация входных данных, защита от injections, auth/authz, CORS, CSRF, sensitive logging.
    - Threat modelling: OWASP-ish проверки.

7. **Тестирование**
    - Unit / Integration / Contract tests coverage и консистентность подхода во всех сервисах.
    - Use of Testcontainers, flaky tests, isolation of external dependencies.

8. **Производительность и масштабируемость**
    - N+1, транзакции и scope (@Transactional usage), connection pool sizing, caching strategies.
    - Асинхронность / non-blocking там, где требуется.

9. **Observability & DevOps**
    - Логи (structured, MDC), metrics (Prometheus endpoints), tracing (OpenTelemetry/Jaeger).
    - Health/readiness, CI/CD pipelines, static analysis in CI, release/versioning strategy.
    - Dockerfile best practices, multi-stage builds, small images.

10. **Документация и onboarding**
    - README, JavaDoc, OpenAPI, ADRs, runbooks, quick start для локальной разработки.

---

## Практические команды / grep / инструменты (запустить если есть доступ)
- Build & tests:
    - `./mvnw -T 1C clean test` или `./gradlew clean test`
- Dependency & module analysis:
    - `mvn dependency:tree` / `./gradlew dependencies`
    - `jdeps` для модулей
- Static analysis:
    - Checkstyle/Spotless, PMD, SpotBugs, ErrorProne
- Security scan:
    - `./mvnw org.owasp:dependency-check-maven:check` или OWASP Dependency-Check
    - `git-secrets` / detect-secrets (grep for `password=`, `AKIA`, `-----BEGIN PRIVATE KEY-----`)
- Search for anti-patterns (use ripgrep/grep):
    - `rg "@Transactional" --hidden` (and check controllers)
    - `rg "System\.out|printStackTrace" --hidden`
    - `rg "TODO|FIXME" --hidden`
    - `rg "new Thread\(|ExecutorService" --hidden`
    - `rg "password|secret|apikey|access_key" --hidden`
- OpenAPI check:
    - find `openapi.yaml` / `swagger.yaml` and validate with `swagger-cli validate`
- DB/ORM heuristics:
    - Search for `.fetch` mappings, look for default EAGER relationships
    - `rg "findAll\\(|getAll\\("` for potentially heavy queries
- Observability:
    - `rg "micrometer|prometheus|opentelemetry|jaeger|actuator" --hidden`

---

## Как структурировать замечания
Для каждого замечания указывать:
- `Title`
- `Severity` — P1 (critical), P2 (important), P3 (low)
- `Location` — путь к файлу + класс + (если возможно) строка
- `Description`
- `Impact` — почему это плохо в проде (конкретно)
- `Reproduction` — как воспроизвести / команды
- `Recommendation` — конкретные шаги (пример patch / code-snippets / config)
- `Suggested owner` — кто должен исправлять

---

## Scoring / Rubric
Для каждого направления выставляй score 1–5 и давай краткий justification:
- 5 — excellent, modern best practices, tests, docs
- 4 — good, minor improvements
- 3 — acceptable, several improvements needed
- 2 — worrying, needs plan
- 1 — critical blockers

---

## FMEA / Threat modelling (обязательный блок)
- Выдели 5–10 наиболее вероятных отказных сценариев (DB outage, message broker lag/drop, broken migration, secret leak).
- Для каждого: описание, probability (High/Med/Low), impact (High/Med/Low), mitigation (steps, owner).

---

## Definition of Done (DoD) для review
- `todo/REVIEW.md` создан и содержит:
    - Заголовок `# Project Code Review Report`
    - Общая оценка и score по направлениям
    - ✅ Сильные стороны
    - ⚠️ Замечания (по направлениям) с полными шаблонами
    - 💡 Рекомендации (short/medium/long term)
    - Итог + Acceptance criteria
    - Appendix: команды для запуска, список файлов/модулей, Assumptions & Unknowns
- Все P1 проблемы содержат корректные remediation steps.
- Если P1-issues блокируют production — добавить urgent note и рекомендованного контакта.
- При отсутствии доступа — `Assumptions & Unknowns` с конкретным списком требуемых файлов/прав.

---

## Output (формат и путь)
- Сохранить Markdown отчет в `todo/REVIEW.md`.
- (Опционально) Сгенерировать `todo/TODO.md` с задачами по P1/P2/P3 на основе найденных замечаний.

---

## Если уместно — примеры полезных рекомендаций (для вставки в отчет)
- «Перенести `@Transactional` из контроллера в сервисный слой; добавить integration-test, имитирующий rollback при исключении.»
- «Использовать `record` для DTO-ов, где класс — просто контейнер полей; это уменьшит boilerplate и улучшит equals/hashCode.»
- «Добавить `@ConfigurationProperties` для конфигов и валидацию через `@Validated` — избавит от magic strings.»
- «Заменить blocking JDBC calls на R2DBC только при явной необходимости async; в ином случае оптимизировать connection pool и queries.»
- «Добавить structured logging (JSON) и MDC-context для requestId; интегрировать с tracing.»

---

## Assumptions & Unknowns (при отсутствии данных)
- Список файлов/путей/доступов, которые нужны для полного ревью (например: `infra/helm`, credentials to internal registry, access to Confluence/ADRs).
