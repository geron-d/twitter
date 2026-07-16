## 🧠 Задача

Сгенерируй **короткий и информативный commit message** на основе `@Commit (Diff of Working State)`.

---

## 📏 Требования

- Использовать формат **Conventional Commits**:
    - `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `perf`, `ci`, `build`, `revert`
- Формат:
    - type(scope): summary 
    - или (если scope не нужен):
    - type: summary

- Использовать **повелительное наклонение**:
- ✅ `add`, `fix`, `update`
- ❌ `added`, `fixed`, `updates`

- Ограничения:
- длина ≤ **50 символов**
- **без точки** в конце
- **только одна строка**
- без лишних слов (`this`, `some`, `various`)

---

## 🔍 Анализ diff (УЛУЧШЕНИЕ)

Перед генерацией:

1. Определи:
- основной тип изменений:
    - новая функциональность → `feat`
    - исправление бага → `fix`
    - рефакторинг → `refactor`
    - тесты → `test`
    - документация → `docs`
    - инфраструктура → `chore` / `ci` / `build`
    - оптимизация → `perf`

2. Выдели:
- **основную суть изменения (1 действие)**
- затронутую область (scope)

3. Если изменений несколько:
- выбрать **самое важное**
- игнорировать второстепенные детали

---

## 🎯 Правила хорошего summary (НОВОЕ)

- максимум **одно действие**
- конкретика > общие слова:
- ❌ `update code`
- ✅ `add user validation`
- избегать:
- `improve`, `change`, `update` без контекста
- использовать доменные термины:
- `user`, `order`, `auth`, `api`, `dto`

---

## 🏷 Scope (НОВОЕ)

Добавлять scope, если:

- затронут конкретный модуль:
- `user`, `auth`, `order`
- или слой:
- `api`, `service`, `repository`, `dto`

Примеры:
```
feat(user): add email validation
fix(auth): handle expired token
refactor(api): simplify controller logic
```


---

## ⚠️ Edge cases (НОВОЕ)

- Если diff пустой →  
```
chore: empty commit
```
- Если только форматирование →  
```
style: format code
```
- Если только переименование →  
```
refactor: rename classes for clarity
```
- Если много несвязанных изменений →  
```
chore: minor updates
```

---

## 📊 Проверка перед выводом

Перед выводом убедись:

- ≤ 50 символов
- нет точки в конце
- корректный `type`
- повелительное наклонение
- нет мусорных слов

---

## 📤 Формат ответа

Вывести **только одну строку**:
```type(scope): summary```
или:
```type: summary```

---

## 💡 Примеры
```
feat(user): add email validation
fix(auth): handle null token
refactor(service): extract validation logic
test(api): add user controller tests
docs: update README
chore: update dependencies
```