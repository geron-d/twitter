# 🎯 Цель

Сформировать полный список unit-тестовых сценариев для метода сервиса (Java 24, Spring Boot 3).

---

# 📥 Входные данные (обязательно)

- Сигнатура метода
- Класс
- Зависимости
- Контракт метода (ожидаемое поведение)

Если данных недостаточно:
→ укажи UNKNOWN / ASSUMPTION

---

# 🧠 Определи границы unit-теста

- тестируется только один класс
- все зависимости — mock (Mockito)
- без Spring context / DB / HTTP

---

# 🧠 Анализ метода

Определи:

- happy flow
- альтернативные ветки
- ошибки
- взаимодействия с зависимостями

---

# 📋 Генерация сценариев

Раздели на группы:

## 1. Happy Path
## 2. Boundary Cases
## 3. Errors & Exceptions
## 4. Interaction Tests

---

# 📌 Формат сценария (обязателен)

Для каждого сценария:

- Name
- Type (happy / boundary / error / interaction)
- Goal

- Input
- Mock setup

- Expected result
- Assertions

---

# 🔁 Обязательные проверки

Покрыть:

- null значения
- пустые коллекции
- крайние значения
- исключения зависимостей
- пустые ответы

---

# 🤝 Работа с mock-объектами

Проверить:

- dependency returns success
- dependency throws exception
- dependency returns empty/null

- verify:
    - called
    - NOT called
    - called N times

---

# ⚠️ Ограничения

- НЕ писать код тестов
- НЕ использовать Spring context
- НЕ придумывать поведение

---

# 📦 Output

Сгенерировать файл:

todo/TEST.md

Структура:

# Unit Tests

## Method: <name>

## Summary

## Scenarios

### Happy Path
...

### Boundary Cases
...

### Errors & Exceptions
...

### Interaction Tests
...

---

# ✅ Definition of Done

- покрыты все ветки логики
- покрыты все взаимодействия
- есть negative сценарии
- нет выдуманных данных