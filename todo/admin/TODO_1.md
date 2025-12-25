# TODO: Добавление логики создания follow-отношений в Admin Script

## Meta
- project: twitter-microservices
- updated: 2025-01-27
- changelog: todo/admin/done/CHANGELOG.md
- standards:
  - STANDART_CODE.md
  - STANDART_PROJECT.md
  - STANDART_TEST.md
  - STANDART_JAVADOC.md
  - STANDART_README.md

## Tasks

### Анализ и проектирование
- [ ] (P1) #1: Анализ требований и проектирование логики создания follow-отношений
  acceptance: "Понять вход/выход, логику выбора центрального пользователя, вычисления половины, обработку ошибок, определить затронутые стандарты"
- [ ] (P1) #2: Проектирование интеграции с follower-api
  acceptance: "Определить структуру Feign Client, Gateway, использование DTO из follower-api или создание shared DTO"

### Реализация кода
- [ ] (P1) #3: Создание FollowApiClient (Feign Client) для интеграции с follower-api
  acceptance: "FollowApiClient создан с методом createFollow, использует @FeignClient, настроен URL из properties, имеет JavaDoc"
- [ ] (P1) #4: Создание FollowGateway для абстракции над Feign Client
  acceptance: "FollowGateway создан с методом createFollow, использует паттерн Gateway, обработка ошибок, логирование, имеет JavaDoc"
- [ ] (P1) #5: Обновление GenerateUsersAndTweetsResponseDto: добавление поля createdFollows
  acceptance: "Добавлено поле List<UUID> createdFollows, обновлены JavaDoc и @Schema аннотации"
- [ ] (P1) #6: Обновление ScriptStatisticsDto: добавление поля totalFollowsCreated
  acceptance: "Добавлено поле int totalFollowsCreated, обновлены JavaDoc"
- [ ] (P1) #7: Обновление GenerateUsersAndTweetsServiceImpl: добавление логики создания follow-отношений
  acceptance: "Добавлен новый шаг Step 1.5 после создания пользователей: выбор центрального пользователя, вычисление половины, создание follow-отношений, обработка ошибок, сбор статистики"
- [ ] (P1) #8: Обновление application.yml: добавление настройки app.follower-api.base-url
  acceptance: "Добавлена настройка app.follower-api.base-url для URL follower-api"

### Документация кода (JavaDoc)
- [ ] (P1) #9: JavaDoc для новых классов: FollowApiClient, FollowGateway
  acceptance: "Все новые классы имеют JavaDoc с @author geron, @version 1.0, @param для всех параметров, @return для возвращаемых значений, @throws для исключений"
- [ ] (P1) #10: JavaDoc для обновленных классов: GenerateUsersAndTweetsServiceImpl, DTO
  acceptance: "Обновлен JavaDoc в GenerateUsersAndTweetsServiceImpl для нового шага, обновлен JavaDoc в DTO для новых полей"

### Тестирование
- [ ] (P1) #11: Unit тесты для FollowGateway
  acceptance: "Создан FollowGatewayTest с тестами: успешное создание follow-отношения, обработка ошибок, null request, используется @ExtendWith(MockitoExtension.class), AssertJ, паттерн AAA"
- [ ] (P1) #12: Unit тесты для обновленного GenerateUsersAndTweetsServiceImpl (follow-отношения)
  acceptance: "Обновлен GenerateUsersAndTweetsServiceImplTest с тестами: успешное создание follow-отношений, с одним пользователем, с двумя пользователями, с тремя пользователями, обработка ошибок, проверка статистики, используется @Nested для группировки"

### Swagger/OpenAPI документация
- [ ] (P3) #13: Swagger/OpenAPI документация
  acceptance: "Не требуется, так как не добавляются новые эндпоинты в контроллере"

### Обновление README
- [ ] (P2) #14: Обновление README.md: описание новой функциональности и примеры
  acceptance: "Обновлен раздел 'Бизнес-логика' с описанием шага создания follow-отношений, обновлен раздел 'Примеры использования' с примером ответа с createdFollows, обновлен раздел 'Интеграция' с информацией об интеграции с follower-api"

### Postman коллекции
- [ ] (P3) #15: Postman коллекции
  acceptance: "Не требуется, так как не добавляются новые эндпоинты"

### Проверка соответствия стандартам
- [ ] (P1) #16: Проверка соответствия всем стандартам проекта
  acceptance: "Проверено соответствие STANDART_CODE.md, STANDART_PROJECT.md, STANDART_TEST.md, STANDART_JAVADOC.md, STANDART_README.md, код соответствует требованиям"

## Assumptions
- DTO для follow-отношений (`FollowRequestDto`, `FollowResponseDto`) можно использовать напрямую из follower-api. Если это недопустимо, нужно будет создать shared DTO в common-lib.
- При вычислении половины используется целочисленное деление (округление вниз). Например: 3 пользователя → halfCount = 1, 4 пользователя → halfCount = 1, 5 пользователей → halfCount = 2.
- Follow-отношения создаются после создания всех пользователей, но до создания твитов.
- Если создание follow-отношения не удалось, ошибка логируется и добавляется в список errors, но выполнение скрипта продолжается.
- Первый созданный пользователь выбирается как "центральный" (тот, который фолловит и которого фолловят).

## Risks
- Зависимость от follower-api: Если follower-api недоступен, создание follow-отношений будет падать. Риск смягчается graceful error handling.
- Производительность: При большом количестве пользователей может быть много HTTP-запросов к follower-api. Риск смягчается тем, что запросы выполняются последовательно и с обработкой ошибок.
- Консистентность данных: Если часть follow-отношений создалась, а часть нет, данные могут быть в несогласованном состоянии. Риск смягчается логированием и статистикой.
- DTO зависимости: Если DTO из follower-api нельзя использовать напрямую, потребуется рефакторинг для создания shared DTO в common-lib.

## Metrics & Success Criteria
- Функциональность:
  - ✅ После создания пользователей создаются follow-отношения согласно логике
  - ✅ Центральный пользователь фолловит половину остальных
  - ✅ Половина остальных фолловят центрального пользователя
  - ✅ Ошибки обрабатываются gracefully
- Качество кода:
  - ✅ Все тесты проходят
  - ✅ Покрытие кода > 80% для новых методов
  - ✅ Код соответствует стандартам проекта
- Документация:
  - ✅ JavaDoc для всех новых классов и методов
  - ✅ README обновлен с описанием новой функциональности
  - ✅ Примеры использования обновлены

## Notes
- Ссылки на стандарты:
  - [STANDART_CODE.md](../../standards/STANDART_CODE.md)
  - [STANDART_PROJECT.md](../../standards/STANDART_PROJECT.md)
  - [STANDART_TEST.md](../../standards/STANDART_TEST.md)
  - [STANDART_JAVADOC.md](../../standards/STANDART_JAVADOC.md)
  - [STANDART_README.md](../../standards/STANDART_README.md)
- Ссылки на существующий код:
  - [GenerateUsersAndTweetsServiceImpl.java](../../services/admin-script-api/src/main/java/com/twitter/service/GenerateUsersAndTweetsServiceImpl.java)
  - [UsersGateway.java](../../services/admin-script-api/src/main/java/com/twitter/gateway/UsersGateway.java)
  - [FollowController.java](../../services/follower-api/src/main/java/com/twitter/controller/FollowController.java)
  - [FollowRequestDto.java](../../services/follower-api/src/main/java/com/twitter/dto/request/FollowRequestDto.java)

