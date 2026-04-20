# 🎯 Цель

Выполнить production-grade code review проекта (Java 24 + Spring Boot 3)
и сохранить отчёт в todo/REVIEW.md.

---

# 🧠 Роли

Ты действуешь как:

1. Architect
2. Senior Developer
3. SRE

---

# 🔄 Execution Pipeline

## Stage 1 — Discovery
Изучи:
- root, services/, libs/, modules/
- src/, configs
- build.gradle / pom.xml
- Docker / k8s / CI
- OpenAPI

Если нет доступа:
→ верни список REQUIRED DATA

---

## Stage 2 — Architecture Mapping

Определи:

- модули и границы
- зависимости
- data flow

Сгенерируй текстовую диаграмму

---

## Stage 3 — Risk Analysis

Для ключевых компонентов:

- Failure modes (FMEA)
- Threat model (кратко)
- Production scenarios:
    - DB down
    - latency spike
    - partial failure

---

## Stage 4 — Code Review

Области:

- Architecture
- Java 24
- Spring Boot 3
- Style
- Security
- Testing
- Performance
- Observability
- Docs
- Developer Experience

---

## Stage 5 — Findings (обязательный формат)

Для каждого замечания:

- Title
- Severity (P1/P2/P3)
- Business Impact (High/Medium/Low)

- Location (file + line/class)

- Description
- Evidence
- Impact

- Reproduction

- Recommendation

- Suggested owner

- Confidence (High/Medium/Low)

---

## Stage 6 — Prioritization

Сформируй:

- Top 5 P1 issues
- Quick wins (low effort / high impact)

---

## Stage 7 — Output

Файл: todo/REVIEW.md

---

# 📄 Структура отчёта

# Code Review Report

## Overall Score (1–5)

## Risk Summary (Top issues)

## Architecture Overview
- Diagram (text)
- Data flow

## ✅ Strengths

## ⚠️ Findings (по категориям)

## 💡 Recommendations
- Short-term
- Long-term

## 🔥 Incident Scenarios
- как система ломается
- как восстанавливается

## 🧪 Developer Experience

## 📊 Scores by Area

## 📌 Appendix
- команды
- проверенные файлы
- checklist

## ❓ Assumptions & Unknowns

---

# ⚠️ Ограничения

- НЕ придумывать отсутствующие данные
- Указывать UNKNOWN при недостатке информации

---

# ✅ Definition of Done

- REVIEW.md создан
- Все модули перечислены
- Есть P1 issues с remediation
- Есть команды воспроизведения
- Есть risk analysis
- Нет галлюцинаций