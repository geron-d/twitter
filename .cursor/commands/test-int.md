# 🎯 Цель

Сформировать полный список интеграционных тестовых сценариев для метода сервиса (Java 24, Spring Boot 3).

Сценарии должны покрывать:
- бизнес-логику
- интеграции
- отказоустойчивость

---

# 📥 Входные данные (обязательно)

- Сигнатура метода
- Класс
- Зависимости:
    - БД
    - HTTP сервисы
    - брокеры (Kafka/Rabbit)
- Побочные эффекты:
    - запись в БД
    - публикация событий
- Ограничения:
    - transactional
    - idempotency

Если данных не хватает:
→ укажи UNKNOWN и предположения

---

# 🧠 Шаг 1 — Определи границы интеграционного теста

- Что поднимается через Testcontainers
- Что мокируется через WireMock
- Что не входит в тест

---

# 🧠 Шаг 2 — Анализ метода

Определи:

- основной happy flow
- альтернативные ветки
- точки отказа
- внешние зависимости

---

# 🧠 Шаг 3 — Генерация сценариев

Раздели на 3 группы:

## 1. Happy Path
## 2. Boundary Cases
## 3. Exceptional Cases

---

# 📋 Формат сценария (обязателен)

Для каждого сценария:

- Name
- Type (happy / boundary / exceptional)
- Goal

- Preconditions
- Input

- External conditions:
    - DB state
    - WireMock responses
    - контейнеры

- Expected result
- Side effects:
    - DB changes
    - events
    - external calls

- Assertions:
    - response
    - состояние системы

---

# ⚠️ Обязательные проверки

Для каждой интеграции:

- timeout
- retry
- failure response
- invalid response

---

# 🔁 Дополнительно

Проверь:

- идемпотентность
- повторные вызовы
- конкурентные вызовы (если применимо)

---

# 🧪 Технологии

- JUnit 5
- Spring Boot Test
- Testcontainers
- WireMock

- Использовать AAA
- Все тесты в одном @Nested классе

---

# 📦 Output

Сгенерируй файл:

todo/TODO.md

Структура:

# Integration Tests

## Method: <name>

## Summary

## Test Scenarios

### Happy Path
...

### Boundary Cases
...

### Exceptional Cases
...

## Test Implementation Plan

- Поднятие контейнеров
- Конфигурация Spring context
- Настройка WireMock
- Подготовка данных
- Структура тестов
- Очистка состояния

---

# ✅ Definition of Done

Сценарии считаются достаточными, если:

- покрыты все ветки логики
- есть минимум 1 failure сценарий на зависимость
- проверяются side effects
- нет выдуманных данных