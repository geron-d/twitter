# TODO: Переименование эндпоинта и сервиса в admin-script-api

## Meta
- project: twitter-microservices
- updated: 2025-01-27
- changelog: todo/admin/done/1_base/CHANGELOG.md
- standards:
  - STANDART_CODE.md
  - STANDART_PROJECT.md
  - STANDART_TEST.md
  - STANDART_JAVADOC.md
  - STANDART_SWAGGER.md
  - STANDART_README.md
  - STANDART_POSTMAN.md

## Tasks

### Анализ и проектирование
- [x] (P1) #1: Анализ требований — Анализ текущего состояния и целевого состояния переименования.
  acceptance: "Определены все компоненты для переименования: эндпоинт, метод контроллера, сервисы, валидаторы, DTO, тесты, документация"
- [x] (P1) #2: Проектирование переименования — Определение порядка переименования компонентов с учетом зависимостей.
  acceptance: "Определен порядок переименования: DTO → валидаторы → сервисы → контроллер → тесты → документация"

### Переименование основных компонентов

#### DTO
- [ ] (P1) #3: Переименование GenerateUsersAndTweetsRequestDto → BaseScriptRequestDto — Переименовать Request DTO и обновить JavaDoc, @Schema аннотации.
  acceptance: "Файл переименован, JavaDoc обновлен, @Schema аннотации обновлены, все ссылки на старое название заменены"
- [ ] (P1) #4: Переименование GenerateUsersAndTweetsResponseDto → BaseScriptResponseDto — Переименовать Response DTO и обновить JavaDoc, @Schema аннотации.
  acceptance: "Файл переименован, JavaDoc обновлен, @Schema аннотации обновлены, все ссылки на старое название заменены"

#### Валидаторы
- [ ] (P1) #5: Переименование GenerateUsersAndTweetsValidator → BaseScriptValidator — Переименовать интерфейс валидатора и обновить JavaDoc, ссылки на DTO.
  acceptance: "Файл переименован, JavaDoc обновлен, ссылки на BaseScriptRequestDto обновлены"
- [ ] (P1) #6: Переименование GenerateUsersAndTweetsValidatorImpl → BaseScriptValidatorImpl — Переименовать реализацию валидатора и обновить JavaDoc, импорты.
  acceptance: "Файл переименован, JavaDoc обновлен, импорты обновлены, ссылки на BaseScriptValidator и BaseScriptRequestDto обновлены"

#### Сервисы
- [ ] (P1) #7: Переименование GenerateUsersAndTweetsService → BaseScriptService — Переименовать интерфейс сервиса и обновить JavaDoc, ссылки на DTO.
  acceptance: "Файл переименован, JavaDoc обновлен, ссылки на BaseScriptRequestDto и BaseScriptResponseDto обновлены"
- [ ] (P1) #8: Переименование GenerateUsersAndTweetsServiceImpl → BaseScriptServiceImpl — Переименовать реализацию сервиса и обновить JavaDoc, импорты, ссылки на валидатор.
  acceptance: "Файл переименован, JavaDoc обновлен, импорты обновлены, ссылки на BaseScriptService, BaseScriptValidator, BaseScriptRequestDto, BaseScriptResponseDto обновлены"

#### Контроллер и API интерфейс
- [ ] (P1) #9: Обновление AdminScriptApi — Переименовать метод generateUsersAndTweets → baseScript, обновить OpenAPI аннотации, примеры, JavaDoc.
  acceptance: "Метод переименован, OpenAPI аннотации (@Operation, @ApiResponses) обновлены, примеры в @ExampleObject обновлены, JavaDoc обновлен, ссылки на BaseScriptRequestDto и BaseScriptResponseDto обновлены"
- [ ] (P1) #10: Обновление AdminScriptController — Переименовать метод и эндпоинт /generate-users-and-tweets → /base-script, обновить поле сервиса, JavaDoc.
  acceptance: "Метод переименован в baseScript, @PostMapping обновлен на /base-script, поле сервиса обновлено на BaseScriptService, JavaDoc обновлен, ссылки на BaseScriptRequestDto и BaseScriptResponseDto обновлены"

### Переименование тестовых классов

#### Интеграционные тесты
- [ ] (P1) #11: Переименование GenerateUsersAndTweetsControllerTest → BaseScriptControllerTest — Переименовать тестовый класс контроллера и обновить все ссылки на эндпоинт и DTO.
  acceptance: "Файл переименован, все ссылки на /generate-users-and-tweets заменены на /base-script, все ссылки на GenerateUsersAndTweetsRequestDto и GenerateUsersAndTweetsResponseDto заменены на BaseScriptRequestDto и BaseScriptResponseDto, названия тестовых методов обновлены"

#### Unit тесты
- [ ] (P1) #12: Переименование GenerateUsersAndTweetsServiceImplTest → BaseScriptServiceImplTest — Переименовать тестовый класс сервиса и обновить все ссылки на DTO и валидатор.
  acceptance: "Файл переименован, все ссылки на GenerateUsersAndTweetsRequestDto, GenerateUsersAndTweetsResponseDto, GenerateUsersAndTweetsValidator, GenerateUsersAndTweetsServiceImpl заменены на новые названия"
- [ ] (P1) #13: Переименование GenerateUsersAndTweetsValidatorImplTest → BaseScriptValidatorImplTest — Переименовать тестовый класс валидатора и обновить все ссылки на DTO и валидатор.
  acceptance: "Файл переименован, все ссылки на GenerateUsersAndTweetsRequestDto, GenerateUsersAndTweetsValidator, GenerateUsersAndTweetsValidatorImpl заменены на новые названия"

#### Тестовые утилиты
- [ ] (P1) #14: Переименование GenerateUsersAndTweetsTestStubBuilder → BaseScriptTestStubBuilder — Переименовать тестовый утилитный класс и обновить JavaDoc, ссылки в тестах.
  acceptance: "Файл переименован, JavaDoc обновлен, все ссылки в тестах обновлены"

### Обновление документации

#### README
- [ ] (P2) #15: Обновление README.md — Обновить структуру пакетов, таблицу эндпоинтов, разделы REST API, бизнес-логика, валидация, примеры curl.
  acceptance: "Раздел 'Структура пакетов' обновлен с новыми названиями классов, таблица эндпоинтов обновлена (/generate-users-and-tweets → /base-script), раздел 'REST API' обновлен, раздел 'Бизнес-логика' обновлен с новыми названиями сервисов, раздел 'Слой валидации' обновлен, раздел 'Валидация по операциям' обновлен, примеры curl команд обновлены"

#### Postman
- [ ] (P2) #16: Обновление Postman коллекции — Обновить название запроса, URL эндпоинта во всех примерах, описание.
  acceptance: "Название запроса обновлено с 'generate users and tweets' на 'base script', URL /generate-users-and-tweets заменен на /base-script во всех примерах ответов, описание запроса обновлено"

### Финальная проверка
- [ ] (P1) #17: Проверка всех файлов — Проверить все файлы на наличие старых названий (GenerateUsersAndTweets, generateUsersAndTweets, generate-users-and-tweets).
  acceptance: "Выполнен поиск всех вхождений старых названий в сервисе admin-script-api, все ссылки обновлены, проект компилируется без ошибок, все тесты проходят"

## Assumptions
- Метод сервиса executeScript остается без изменений (переименовывается только интерфейс и реализация)
- DTO переименовываются для консистентности (подтверждено пользователем)
- Все тесты должны продолжать работать после переименования
- Обратная совместимость не требуется (breaking change)

## Risks
- **Пропущенные ссылки**: Возможны пропущенные ссылки в комментариях или строковых литералах
- **Компиляция**: После переименования файлов нужно убедиться, что проект компилируется
- **Тесты**: Все тесты должны пройти после переименования
- **Обратная совместимость**: Старый эндпоинт больше не будет работать (breaking change)

## Metrics & Success Criteria
- ✅ Все классы переименованы
- ✅ Все импорты обновлены
- ✅ Все тесты проходят
- ✅ Проект компилируется без ошибок
- ✅ README обновлен
- ✅ Postman коллекция обновлена
- ✅ Swagger документация обновлена
- ✅ Нет ссылок на старые названия в коде

## Notes
- Переименование выполняется для обеспечения консистентности кодовой базы
- Стандарты проекта должны быть соблюдены при переименовании
- Все JavaDoc должны быть обновлены с новыми названиями
- OpenAPI аннотации должны быть обновлены с новыми названиями и примерами