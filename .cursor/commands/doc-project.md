Цель: подготовить / обновить верхнеуровневую документацию по монорепозиторию (Java 24 + Spring Boot 3) и сохранить в корне репозитория файл PROJECT_OVERVIEW.md. Опционально — сгенерировать todo/TODO.md с планом задач.

ВАЖНО — думай очень сильно и многослойно.
При каждом разделе задавай себе "почему?", "что если?", "какие скрытые зависимости?", "кто владелец/ответственный?". Применяй техники: 5 Whys, FMEA (failure modes), threat modelling, runbook-thinking. Для каждой значимой находки указывай вероятность/влияние/mitigation.

Входные данные (если ты агент):
- Репозиторий (root). Смотри src/, modules/, services/, libs/, docs/, .github/workflows, pom.xml / build.gradle, Dockerfile, docker-compose.yml, k8s/helm/, README-*, *.md.
- Существующая документация по сервисам/библиотекам — агрегируй ссылки/пути.
- Конфиги (src/main/resources, application*.yml/.properties), secrets (в vault — если есть ссылки), OpenAPI/Swagger (если есть).

Если репозиторий недоступен — верни список необходимых файлов/прав доступа.

Требуемый результат:
- PROJECT_OVERVIEW.md в корне репозитория, содержащий нижеследующие секции (см. формат ниже).
- В секции "Assumptions & Unknowns" — четко перечислить, что не удалось обнаружить.
- Опционально: todo/TODO.md: чек-лист с пунктами (Done / In Progress / To Do) и приоритетами (P1/P2/P3).

Формат и содержание PROJECT_OVERVIEW.md (строго):
1. Заголовок + краткое summary (1–3 предложения): назначение проекта и ключевой контекст.
2. Quick facts: owners, contacts, main tech stack, repo-layout pointer.
3. Цели и бизнес-задачи (2–6 пунктов).
4. Архитектура (текстовое описание + простая псевдографика или PlantUML-опционально):
    - ключевые сервисы/модули/библиотеки,
    - коммуникации (REST/gRPC/Kafka/etc.),
    - критические внешние интеграции.
5. Структура монорепозитория:
    - корневые папки и назначение (services/, libs/, modules/, docs/, infra/, ci/),
    - где искать доки по сервисам,
    - примеры путей (например: services/payment-service/README.md).
6. Сборка и запуск:
    - команды для сборки (mvn/gradle) и ключевые флаги,
    - локальный запуск (docker-compose / scripts / тестовые наборы),
    - настройки окружения (переменные, примеры .env или application.yml).
7. Деплой и окружения:
    - target envs (dev/stage/prod),
    - описание CI/CD пайплайна (файлы .github/workflows или Jenkinsfile, helm charts),
    - основные шаги деплоя и rollback.
8. Стандарты и соглашения:
    - кодстайл, ветки (git flow / trunk), правила коммитов, PR review, semantic versioning, миграции БД.
9. Observability & Ops:
    - где смотреть логи, метрики, трассировки, alerting,
    - основные SLO/SLA, RTO/RPO ориентиры.
10. Security & Secrets:
- auth/authz подходы, где хранятся секреты, базовые угрозы и mitigations.
11. Dependencies & Third-parties:
- критические внешние сервисы, версии, SLA/контакты.
12. Key risks & mitigations (FMEA-style):
- краткий список рисков с severity (P1/P2/P3), probability (High/Med/Low), mitigation, owner.
13. Onboarding (для новичков):
- быстрый путь: ключевые команды, минимальный стек, "how to run locally in 10–15 min".
14. Links & Further reading:
- ссылки на per-service docs, ADRs, Confluence, swagger, runbooks.
15. Assumptions & Unknowns:
- что не удалось подтвердить, список вопросов для команды.
16. Appendix:
- полезные команды, примеры команд build/run/test, псевдо-PlantUML/ASCII diagram, список важных файлов.

Стиль: кратко, структурировано, маркированные списки, таблицы для quick facts. Максимум практики — чтобы новый инженер мог быстро понять проект и начать работу.

Definition of Done:
- PROJECT_OVERVIEW.md содержит все секции 1–16;
- Все сервисы/модули, для которых есть отдельная документация, перечислены с ссылками;
- Есть секция рисков и onboarding;
- Assumptions & Unknowns явные;
- Если в репозитории есть OpenAPI — ссылка в Appendix;
- Если чего-то нет — отмечено как P2/P1 task в todo/TODO.md.

Выходные файлы:
- /PROJECT_OVERVIEW.md (обязателен)
- /todo/TODO.md (опционально — если найдены задачи/пробелы)

Если данных недостаточно — формируй список конкретных вопросов (owner, точные файлы, доступы) и помести их в Assumptions & Unknowns.
