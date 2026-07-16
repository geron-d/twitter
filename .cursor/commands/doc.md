# 🎯 Цель

Сгенерировать production-grade README.md для Java 24 + Spring Boot 3 сервиса.

README должен позволять:
- понять систему за 10 минут
- развернуть сервис без кода
- диагностировать основные проблемы

---

# 🧠 Execution Pipeline

## Stage 1 — Discovery
Изучи:
- src/main/java
- src/main/resources
- pom.xml / build.gradle
- Dockerfile
- k8s manifests
- tests
- OpenAPI (если есть)

Если OpenAPI нет:
- анализируй @RestController, @Service, @Repository

---

## Stage 2 — Analysis

Определи:
- bounded contexts
- ключевые бизнес-флоу
- зависимости
- точки отказа

---

## Stage 3 — Risk & Failure Analysis

Для каждого компонента:

- Why does it exist? (5 Whys — кратко)
- Failure modes (FMEA)
- Что если dependency недоступна?
- Blast radius
- Recovery strategy (runbook thinking)

---

## Stage 4 — Documentation Generation

Сгенерируй README.md со структурой:

### 1. Introduction
- Назначение
- Контекст
- Владельцы

### 2. Architecture
- Компоненты
- Диаграмма (текстовая)

### 3. Components (обязательно)

Для каждого:

- Name (FQN + путь)
- Type
- Responsibility

- Public API:
  signature:
    - purpose
    - inputs
    - outputs
    - side effects
    - exceptions
    - transactional / caching

- Dependencies

- Failure modes
- Recovery

---

### 4. API

Для каждого endpoint:

- Endpoint
- Auth
- Params
- Request
- Response (с примерами)
- Errors (минимум 2 сценария)
- Idempotency / caching
- Performance (p95)
- Retry / timeout behaviour

---

### 5. Integrations

- External systems
- Contracts
- SLA assumptions
- Retry/backoff

---

### 6. Security

- Auth/AuthZ
- Secrets handling
- Threat model (кратко)

---

### 7. Observability

- Метрики
- Логи
- Трейсинг
- Алерты

---

### 8. Deployment

- Docker
- k8s
- ENV variables
- CI/CD

---

### 9. Usage Examples

- cURL
- Request/response

---

### 10. Assumptions & Unknowns

- Чётко:
  UNKNOWN
  HOW TO VERIFY

---

### 11. Appendix

- OpenAPI (если есть)

---

## Stage 5 — Validation (ОБЯЗАТЕЛЬНО)

Проверь:

- Можно ли вызвать API по README?
- Понятно ли как дебажить?
- Нет ли выдуманных данных?
- Есть ли recovery steps?

---

# ⚠️ Ограничения

- НЕ писать код
- НЕ генерировать секреты
- НЕ придумывать отсутствующие данные

---

# 🧩 Output

Только README.md в Markdown