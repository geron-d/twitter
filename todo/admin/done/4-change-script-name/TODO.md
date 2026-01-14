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
- [x] (P1) [2025-01-27 15:30] #3: Переименование GenerateUsersAndTweetsRequestDto → BaseScriptRequestDto — Переименовать Request DTO и обновить JavaDoc, @Schema аннотации.
  acceptance: "Файл переименован, JavaDoc обновлен, @Schema аннотации обновлены, все ссылки на старое название заменены"
  note: "Создан новый файл BaseScriptRequestDto.java с обновленным JavaDoc и @Schema аннотациями. Обновлены все ссылки в 9 файлах (контроллеры, сервисы, валидаторы, тесты, README). Старый файл удален. Проверено: все ссылки заменены, ошибок компиляции нет."
- [x] (P1) [2025-01-27 15:35] #4: Переименование GenerateUsersAndTweetsResponseDto → BaseScriptResponseDto — Переименовать Response DTO и обновить JavaDoc, @Schema аннотации.
  acceptance: "Файл переименован, JavaDoc обновлен, @Schema аннотации обновлены, все ссылки на старое название заменены"
  note: "Создан новый файл BaseScriptResponseDto.java с обновленным JavaDoc и @Schema аннотациями. Обновлены все ссылки в 7 файлах (контроллеры, сервисы, тесты, README). Старый файл удален. Проверено: все ссылки заменены, ошибок компиляции нет."

#### Валидаторы
- [x] (P1) [2025-01-27 16:00] #5: Переименование GenerateUsersAndTweetsValidator → BaseScriptValidator — Переименовать интерфейс валидатора и обновить JavaDoc, ссылки на DTO.
  acceptance: "Файл переименован, JavaDoc обновлен, ссылки на BaseScriptRequestDto обновлены"
  note: "Создан новый файл BaseScriptValidator.java с обновленным JavaDoc. Обновлены все ссылки в 4 файлах (реализация валидатора, сервис, тесты сервиса). Старый файл удален. Проверено: все ссылки заменены, ошибок компиляции нет."
- [x] (P1) [2025-01-27 16:05] #6: Переименование GenerateUsersAndTweetsValidatorImpl → BaseScriptValidatorImpl — Переименовать реализацию валидатора и обновить JavaDoc, импорты.
  acceptance: "Файл переименован, JavaDoc обновлен, импорты обновлены, ссылки на BaseScriptValidator и BaseScriptRequestDto обновлены"
  note: "Создан новый файл BaseScriptValidatorImpl.java с обновленным JavaDoc. Обновлены все ссылки в 1 файле (тесты валидатора). Старый файл удален. Проверено: все ссылки заменены, ошибок компиляции нет."

#### Сервисы
- [x] (P1) [2025-01-27 16:10] #7: Переименование GenerateUsersAndTweetsService → BaseScriptService — Переименовать интерфейс сервиса и обновить JavaDoc, ссылки на DTO.
  acceptance: "Файл переименован, JavaDoc обновлен, ссылки на BaseScriptRequestDto и BaseScriptResponseDto обновлены"
  note: "Создан новый файл BaseScriptService.java с обновленным JavaDoc. Обновлены все ссылки в 3 файлах (реализация сервиса, контроллер). Старый файл удален. Проверено: все ссылки заменены, ошибок компиляции нет."
- [x] (P1) [2025-01-27 16:15] #8: Переименование GenerateUsersAndTweetsServiceImpl → BaseScriptServiceImpl — Переименовать реализацию сервиса и обновить JavaDoc, импорты, ссылки на валидатор.
  acceptance: "Файл переименован, JavaDoc обновлен, импорты обновлены, ссылки на BaseScriptService, BaseScriptValidator, BaseScriptRequestDto, BaseScriptResponseDto обновлены"
  note: "Файл переименован в BaseScriptServiceImpl.java с обновленным JavaDoc. Обновлены все ссылки в 1 файле (тесты сервиса). Проверено: все ссылки заменены, ошибок компиляции нет."

#### Контроллер и API интерфейс
- [x] (P1) [2025-01-27 16:20] #9: Обновление AdminScriptApi — Переименовать метод generateUsersAndTweets → baseScript, обновить OpenAPI аннотации, примеры, JavaDoc.
  acceptance: "Метод переименован, OpenAPI аннотации (@Operation, @ApiResponses) обновлены, примеры в @ExampleObject обновлены, JavaDoc обновлен, ссылки на BaseScriptRequestDto и BaseScriptResponseDto обновлены"
  note: "Метод переименован в baseScript в интерфейсе AdminScriptApi и контроллере AdminScriptController. Обновлены JavaDoc и @Operation аннотации. Примеры в @ExampleObject остались актуальными. Проверено: все ссылки обновлены, ошибок компиляции нет."
- [x] (P1) [2025-01-27 16:25] #10: Обновление AdminScriptController — Переименовать метод и эндпоинт /generate-users-and-tweets → /base-script, обновить поле сервиса, JavaDoc.
  acceptance: "Метод переименован в baseScript, @PostMapping обновлен на /base-script, поле сервиса обновлено на BaseScriptService, JavaDoc обновлен, ссылки на BaseScriptRequestDto и BaseScriptResponseDto обновлены"
  note: "Эндпоинт обновлен с /generate-users-and-tweets на /base-script. Поле сервиса переименовано с generateUsersAndTweetsService на baseScriptService. Метод уже был переименован в baseScript на предыдущем шаге. Проверено: все ссылки обновлены, ошибок компиляции нет."

### Переименование тестовых классов

#### Интеграционные тесты
- [x] (P1) [2026-01-13 15:14] #11: Переименование GenerateUsersAndTweetsControllerTest → BaseScriptControllerTest — Переименовать тестовый класс контроллера и обновить все ссылки на эндпоинт и DTO.
  acceptance: "Файл переименован, все ссылки на /generate-users-and-tweets заменены на /base-script, все ссылки на GenerateUsersAndTweetsRequestDto и GenerateUsersAndTweetsResponseDto заменены на BaseScriptRequestDto и BaseScriptResponseDto, названия тестовых методов обновлены"
  note: "Создан новый файл BaseScriptControllerTest.java. Класс переименован в BaseScriptControllerTest, вложенный класс GenerateUsersAndTweetsTests переименован в BaseScriptTests, все 15 ссылок на эндпоинт /generate-users-and-tweets заменены на /base-script, все тестовые методы generateUsersAndTweets_* переименованы в baseScript_*. Старый файл удален. Проверено: все ссылки обновлены, ошибок компиляции нет. Примечание: ссылка на GenerateUsersAndTweetsTestStubBuilder оставлена (будет обновлена в шаге #14), ссылки в README.md оставлены (будут обновлены в шаге #15)."

#### Unit тесты
- [x] (P1) [2026-01-13 15:22] #12: Переименование GenerateUsersAndTweetsServiceImplTest → BaseScriptServiceImplTest — Переименовать тестовый класс сервиса и обновить все ссылки на DTO и валидатор.
  acceptance: "Файл переименован, все ссылки на GenerateUsersAndTweetsRequestDto, GenerateUsersAndTweetsResponseDto, GenerateUsersAndTweetsValidator, GenerateUsersAndTweetsServiceImpl заменены на новые названия"
  note: "Создан новый файл BaseScriptServiceImplTest.java. Класс переименован в BaseScriptServiceImplTest. Проверено: все ссылки на DTO уже были обновлены на BaseScriptRequestDto и BaseScriptResponseDto в предыдущих шагах, ссылка на валидатор уже была обновлена на BaseScriptValidator, ссылка на сервис уже была обновлена на BaseScriptServiceImpl. Старый файл удален. Проверено: все ссылки обновлены, ошибок компиляции нет. Примечание: ссылки в README.md оставлены (будут обновлены в шаге #15)."
- [x] (P1) [2026-01-13 15:23] #13: Переименование GenerateUsersAndTweetsValidatorImplTest → BaseScriptValidatorImplTest — Переименовать тестовый класс валидатора и обновить все ссылки на DTO и валидатор.
  acceptance: "Файл переименован, все ссылки на GenerateUsersAndTweetsRequestDto, GenerateUsersAndTweetsValidator, GenerateUsersAndTweetsValidatorImpl заменены на новые названия"
  note: "Создан новый файл BaseScriptValidatorImplTest.java. Класс переименован в BaseScriptValidatorImplTest. Проверено: все ссылки на DTO уже были обновлены на BaseScriptRequestDto в предыдущих шагах, ссылка на валидатор уже была обновлена на BaseScriptValidatorImpl. Старый файл удален. Проверено: все ссылки обновлены, ошибок компиляции нет. Примечание: ссылки в README.md оставлены (будут обновлены в шаге #15)."

#### Тестовые утилиты
- [x] (P1) [2026-01-13 15:27] #14: Переименование GenerateUsersAndTweetsTestStubBuilder → BaseScriptTestStubBuilder — Переименовать тестовый утилитный класс и обновить JavaDoc, ссылки в тестах.
  acceptance: "Файл переименован, JavaDoc обновлен, все ссылки в тестах обновлены"
  note: "Создан новый файл BaseScriptTestStubBuilder.java. Класс переименован в BaseScriptTestStubBuilder. Обновлен JavaDoc: ссылка на GenerateUsersAndTweetsControllerTest заменена на BaseScriptControllerTest. Обновлены все ссылки в BaseScriptControllerTest.java: импорт, поле и инициализация. Старый файл удален. Проверено: все ссылки обновлены, ошибок компиляции нет."

### Обновление документации

#### README
- [x] (P2) [2026-01-13 15:29] #15: Обновление README.md — Обновить структуру пакетов, таблицу эндпоинтов, разделы REST API, бизнес-логика, валидация, примеры curl.
  acceptance: "Раздел 'Структура пакетов' обновлен с новыми названиями классов, таблица эндпоинтов обновлена (/generate-users-and-tweets → /base-script), раздел 'REST API' обновлен, раздел 'Бизнес-логика' обновлен с новыми названиями сервисов, раздел 'Слой валидации' обновлен, раздел 'Валидация по операциям' обновлен, примеры curl команд обновлены"
  note: "Обновлен README.md: структура пакетов (GenerateUsersAndTweetsService → BaseScriptService, GenerateUsersAndTweetsServiceImpl → BaseScriptServiceImpl, GenerateUsersAndTweetsValidator → BaseScriptValidator, GenerateUsersAndTweetsValidatorImpl → BaseScriptValidatorImpl), таблица эндпоинтов (/generate-users-and-tweets → /base-script), раздел REST API (POST /api/v1/admin-scripts/generate-users-and-tweets → POST /api/v1/admin-scripts/base-script), раздел Бизнес-логика (GenerateUsersAndTweetsService → BaseScriptService, GenerateUsersAndTweetsValidator → BaseScriptValidator), раздел Слой валидации (GenerateUsersAndTweetsValidator → BaseScriptValidator, POST /generate-users-and-tweets → POST /base-script), примеры curl команд (все 3 примера обновлены), раздел Тестирование (GenerateUsersAndTweetsValidatorImplTest → BaseScriptValidatorImplTest, GenerateUsersAndTweetsServiceImplTest → BaseScriptServiceImplTest, GenerateUsersAndTweetsControllerTest → BaseScriptControllerTest). Проверено: все ссылки обновлены, ошибок не обнаружено."

#### Postman
- [x] (P2) [2026-01-13 15:31] #16: Обновление Postman коллекции — Обновить название запроса, URL эндпоинта во всех примерах, описание.
  acceptance: "Название запроса обновлено с 'generate users and tweets' на 'base script', URL /generate-users-and-tweets заменен на /base-script во всех примерах ответов, описание запроса обновлено"
  note: "Обновлена Postman коллекция twitter-admin-script-api.postman_collection.json: название запроса изменено с 'generate users and tweets' на 'base script', URL эндпоинта /generate-users-and-tweets заменен на /base-script в основном запросе и во всех 8 примерах ответов (originalRequest.url). Описание запроса оставлено без изменений, так как оно описывает функциональность, а не название эндпоинта. Проверено: все ссылки обновлены, ошибок не обнаружено."

### Финальная проверка
- [x] (P1) [2026-01-13 15:34] #17: Проверка всех файлов — Проверить все файлы на наличие старых названий (GenerateUsersAndTweets, generateUsersAndTweets, generate-users-and-tweets).
  acceptance: "Выполнен поиск всех вхождений старых названий в сервисе admin-script-api, все ссылки обновлены, проект компилируется без ошибок, все тесты проходят"
  note: "Выполнена проверка всех файлов в сервисе admin-script-api на наличие старых названий (GenerateUsersAndTweets, generateUsersAndTweets, generate-users-and-tweets). Результаты: в services/admin-script-api - упоминаний старых названий не найдено, в postman/admin-script-api - упоминаний старых названий не найдено, в todo/ - упоминания найдены только в исторических записях TODO.md и CHANGELOG.md (это нормально). Все ссылки в коде обновлены. Примечание: упоминания в todo/ - это исторические записи о выполненных шагах, их обновление не требуется."

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
