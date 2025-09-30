# TODO - Анализ и удаление неиспользуемого LastAdminDeactivationException

## Meta
- project: twitter-microservices
- updated: 2025-01-27
- changelog: todo/CHANGELOG.md
- task: Анализ и безопасное удаление неиспользуемого исключения LastAdminDeactivationException

## Tasks

### Analysis Phase
- [x] (P1) [2025-01-27 15:45] #1: Поиск всех импортов LastAdminDeactivationException — выполнено
  acceptance: "Найти все файлы, импортирующие класс"
  tools: grep, codebase_search
  risks: Могут быть скрытые импорты через wildcard
  note: "КРИТИЧНО: GlobalExceptionHandler использует класс БЕЗ импорта - код не компилируется!"

- [x] (P1) [2025-01-27 19:15] #2: Поиск всех упоминаний класса в коде — выполнено
  acceptance: "Найти все места использования класса (new, instanceof, catch)"
  tools: grep, semantic search
  risks: Использование через рефлексию или строковые ссылки
  note: "КРИТИЧНО: Класс НЕ ИСПОЛЬЗУЕТСЯ в реальном коде! Только примеры в JavaDoc и GlobalExceptionHandler без импорта"

- [x] (P1) [2025-01-27 19:30] #3: Анализ тестовых файлов — выполнено
  acceptance: "Проверить использование в unit/integration тестах"
  tools: grep по test директориям
  risks: Тесты могут использовать исключение для проверки поведения
  note: "КРИТИЧНО: LastAdminDeactivationException НЕ ИСПОЛЬЗУЕТСЯ в тестах! Функциональность реализована через BusinessRuleValidationException.lastAdminDeactivation()"

- [x] (P1) [2025-01-27 19:45] #4: Проверка документации и JavaDoc — выполнено
  acceptance: "Найти ссылки в документации, README, JavaDoc"
  tools: grep по .md файлам, анализ @see тегов
  risks: Документация может ссылаться на класс
  note: "КРИТИЧНО: Найдены устаревшие ссылки в README.md, JAVADOC_STANDARDS.md, JavaDoc! Все ссылки неактуальны и указывают на несуществующий функционал"

- [x] (P2) [2025-01-27 20:00] #5: Анализ архитектурной важности — выполнено
  acceptance: "Оценить роль исключения в общей архитектуре"
  analysis: "Понять, является ли это частью planned functionality"
  note: "КРИТИЧНО: LastAdminDeactivationException архитектурно устарел! Нарушает принципы Consistency и Extensibility. Дублирует BusinessRuleValidationException. Создает проблемы компиляции. РЕКОМЕНДУЕТСЯ НЕМЕДЛЕННОЕ УДАЛЕНИЕ"

### Design Phase
- [x] (P2) [2025-01-27 20:15] #6: Оценка потенциальных сценариев использования — выполнено
  acceptance: "Определить, может ли класс понадобиться в будущем"
  considerations: "Анализ бизнес-логики управления администраторами"
  note: "КРИТИЧНО: НЕТ потенциальных сценариев! Все будущие потребности легко покрываются BusinessRuleValidationException. Удаление БЕЗОПАСНО и НЕОБХОДИМО"

- [x] (P2) [2025-01-27 20:30] #7: Определение стратегии удаления — выполнено
  acceptance: "Выбрать безопасный подход к удалению"
  options: "Немедленное удаление vs помещение в deprecated"
  note: "РЕШЕНИЕ: Немедленное удаление! НЕТ рисков, НЕТ зависимостей, решает проблемы компиляции. План: backup → удаление класса → обновление GlobalExceptionHandler → обновление документации → проверка"

### Implementation Phase
- [ ] (P3) #8: Создание backup/checkpoint
  acceptance: "Сохранить текущее состояние перед изменениями"
  tools: git commit, backup файла

- [ ] (P3) #9: Удаление класса LastAdminDeactivationException
  acceptance: "Удалить файл и все связанные ссылки"
  file: "shared/common-lib/src/main/java/com/twitter/common/exception/LastAdminDeactivationException.java"

- [ ] (P3) #10: Обновление зависимостей и импортов
  acceptance: "Удалить все импорты класса из других файлов"
  verification: "Проверить отсутствие broken imports"

### Testing Phase
- [ ] (P1) #11: Проверка компиляции проекта
  acceptance: "Убедиться, что проект компилируется без ошибок"
  tools: gradle build
  risks: Могут появиться compilation errors

- [ ] (P1) #12: Запуск всех тестов
  acceptance: "Все тесты должны проходить успешно"
  tools: gradle test
  risks: Тесты могут сломаться из-за удаления класса

- [ ] (P2) #13: Проверка интеграционных тестов
  acceptance: "Проверить работу сервисов после изменений"
  tools: docker-compose up, manual testing

### Verification Phase
- [ ] (P1) #14: Финальная проверка проекта
  acceptance: "Убедиться в отсутствии ошибок и предупреждений"
  tools: gradle build --warning-mode all

- [ ] (P2) #15: Обновление документации
  acceptance: "Удалить ссылки на класс из документации"
  files: "README.md, JavaDoc, архитектурные диаграммы"

- [ ] (P3) #16: Создание changelog записи
  acceptance: "Зафиксировать изменения в CHANGELOG.md"
  content: "Описание удаленного класса и причины"

## Assumptions
- Класс LastAdminDeactivationException действительно не используется в коде
- Удаление не нарушит существующую функциональность
- Нет планов по использованию этого класса в ближайшем будущем
- Проект использует Java 24 и Spring Boot
- Все зависимости управляются через Gradle

## Risks & Mitigation
- **Риск**: Скрытые зависимости через рефлексию
  - **Митигация**: Тщательный поиск по всему коду, включая строковые ссылки

- **Риск**: Нарушение API контрактов
  - **Митигация**: Проверка всех публичных API и интерфейсов

- **Риск**: Поломка тестов
  - **Митигация**: Запуск всех тестов после удаления

- **Риск**: Будущая необходимость в классе
  - **Митигация**: Создание backup и возможность быстрого восстановления

## Success Criteria
- [ ] Класс полностью удален из кода
- [ ] Проект компилируется без ошибок
- [ ] Все тесты проходят успешно
- [ ] Документация обновлена
- [ ] Изменения зафиксированы в git

## Tools & Technologies
- **Поиск**: grep, codebase_search, semantic search
- **Сборка**: Gradle build system
- **Тестирование**: JUnit, Spring Boot Test
- **Контейнеризация**: Docker, docker-compose
- **Версионирование**: Git

## Notes
- Класс LastAdminDeactivationException находится в shared/common-lib
- Исключение предназначено для предотвращения деактивации последнего администратора
- HTTP статус: 409 Conflict
- Наследуется от ResponseStatusException
- Имеет подробную JavaDoc документацию

## Alternative Approaches
1. **Deprecation**: Пометить класс как @Deprecated вместо удаления
2. **Conditional compilation**: Использовать условную компиляцию
3. **Gradual removal**: Удалять поэтапно с предупреждениями

## Next Steps After Completion
- Мониторинг системы на предмет отсутствия ошибок
- Обновление архитектурной документации
- Проведение code review изменений
- Планирование аналогичной очистки других неиспользуемых классов