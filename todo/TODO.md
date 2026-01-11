# TODO: Добавление лайков и ретвитов в административный скрипт

## Meta
- project: twitter-microservices
- updated: 2025-01-27 18:00
- changelog: todo/CHANGELOG.md
- standards:
  - STANDART_CODE.md
  - STANDART_PROJECT.md
  - STANDART_TEST.md
  - STANDART_JAVADOC.md
  - STANDART_README.md

## Tasks

### Анализ и проектирование
- [x] (P1) [2025-01-27] #1: Анализ зависимостей DTO — Проверить доступность LikeTweetRequestDto и RetweetRequestDto из tweet-api в admin-script-api. Определить стратегию: использовать напрямую, создать копии или переместить в common-lib.
  acceptance: "Определена стратегия работы с DTO для лайков и ретвитов, проверена доступность через зависимости"
  note: "Проанализирована архитектура проекта. DTO находятся в tweet-api (com.twitter.dto.request), но admin-script-api не имеет зависимости от tweet-api. Определена стратегия: переместить LikeTweetRequestDto и RetweetRequestDto в common-lib (com.twitter.common.dto.request) для соответствия текущей архитектуре, где другие DTO (CreateTweetRequestDto, DeleteTweetRequestDto) уже находятся в common-lib. Это потребует обновления импортов в tweet-api."
- [x] (P1) [2025-01-27] #2: Проектирование API интеграции — Определить структуру Feign клиентов для лайков и ретвитов, методы Gateway, логику выбора твитов и пользователей.
  acceptance: "Спроектирована структура интеграции с tweet-api для лайков и ретвитов, определена логика выбора твитов и пользователей"
  note: "Спроектирована полная структура интеграции: методы likeTweet и retweetTweet для TweetsApiClient и TweetsGateway, логика выбора 6 разных твитов и пользователей (исключая автора), обработка ошибок (409 для self-like/self-retweet/дубликатов не прерывает выполнение), кэширование TweetResponseDto для получения автора твита. Создан документ design-api-integration.md с детальным проектированием."

### Реализация инфраструктуры
- [x] (P1) [2025-01-27] #3: Добавление методов в TweetsApiClient — Добавить методы likeTweet и retweetTweet в TweetsApiClient с JavaDoc.
  acceptance: "Методы likeTweet и retweetTweet добавлены в TweetsApiClient с полной JavaDoc документацией, соответствуют эндпоинтам tweet-api"
  note: "Добавлены методы likeTweet() и retweetTweet() в TweetsApiClient с полной JavaDoc документацией. Использованы полные имена пакетов для DTO (com.twitter.dto.request.* и com.twitter.dto.response.*), так как DTO еще не перемещены в common-lib. После перемещения DTO (из шага #1) необходимо обновить импорты. Методы соответствуют эндпоинтам tweet-api: POST /api/v1/tweets/{tweetId}/like и POST /api/v1/tweets/{tweetId}/retweet."
- [x] (P1) [2025-01-27] #4: Расширение TweetsGateway — Добавить методы likeTweet и retweetTweet в TweetsGateway с обработкой ошибок и JavaDoc.
  acceptance: "Методы likeTweet и retweetTweet добавлены в TweetsGateway с обработкой ошибок, логированием и полной JavaDoc документацией"
  note: "Добавлены методы likeTweet() и retweetTweet() в TweetsGateway с валидацией входных параметров (null-проверки), логированием успеха/ошибок, обработкой исключений (пробрасывание как RuntimeException). Добавлена полная JavaDoc документация с описанием функциональности, параметров, возвращаемых значений и примечанием о необходимости graceful обработки HTTP 409 ошибок в сервисном слое. Методы следуют существующему паттерну в TweetsGateway."

### Реализация бизнес-логики
- [x] (P1) [2025-01-27] #5: Обновление ScriptStatisticsDto — Добавить поля totalLikesCreated и totalRetweetsCreated, обновить JavaDoc и @Schema аннотации.
  acceptance: "ScriptStatisticsDto обновлен с новыми полями totalLikesCreated и totalRetweetsCreated, обновлены JavaDoc и @Schema аннотации"
  note: "Добавлены поля totalLikesCreated и totalRetweetsCreated в ScriptStatisticsDto. Обновлен JavaDoc в описании record с новыми параметрами. Обновлена @Schema аннотация с примерами новых полей. Добавлены @Schema аннотации для новых полей с описаниями и примерами. Обновлен конструктор в GenerateUsersAndTweetsServiceImpl (временно используется 0 для новых полей, будут реализованы в шаге #6)."
- [x] (P1) [2025-01-27] #6: Реализация логики лайков и ретвитов в GenerateUsersAndTweetsServiceImpl — Реализовать шаги 6-11: логика выбора твитов и пользователей, создание лайков и ретвитов, обработка ошибок, обновление статистики.
  acceptance: "Реализованы шаги 6-11 в GenerateUsersAndTweetsServiceImpl: выбор разных твитов для каждой операции, выбор пользователей (исключая автора твита), создание лайков (половина, треть, 1 пользователь), создание ретвитов (половина, треть, 1 пользователь), обработка ошибок (self-like, self-retweet, дубликаты), обновление статистики"
  note: "Реализованы шаги 6-11: добавлено кэширование TweetResponseDto при создании твитов (шаг 2), реализована логика выбора разных твитов для каждой операции (6 разных твитов), выбор пользователей исключая автора твита, создание лайков (шаги 6-8: половина, треть, 1 пользователь), создание ретвитов (шаги 9-11: половина, треть, 1 пользователь), graceful обработка ошибок (логирование и добавление в errors, выполнение продолжается), обновление статистики (totalLikesCreated и totalRetweetsCreated). Обновлен JavaDoc с описанием новых шагов 6-11."
- [ ] (P1) #7: Обновление JavaDoc для GenerateUsersAndTweetsServiceImpl — Обновить JavaDoc с описанием новых шагов 6-11.
  acceptance: "JavaDoc для GenerateUsersAndTweetsServiceImpl обновлен с подробным описанием шагов 6-11 (создание лайков и ретвитов)"

### Документация
- [ ] (P2) #8: Обновление README.md — Обновить README: описание новых шагов скрипта, новых методов Gateway, примеры использования.
  acceptance: "README.md обновлен с описанием новых шагов 6-11 скрипта, новых методов TweetsGateway (likeTweet, retweetTweet), примеры использования"

### Тестирование
- [ ] (P1) #9: Unit тесты для TweetsGateway — Написать unit тесты для новых методов TweetsGateway (мокирование Feign клиента, обработка ошибок).
  acceptance: "Unit тесты для методов likeTweet и retweetTweet в TweetsGateway написаны, покрывают успешные сценарии и обработку ошибок, используют мокирование Feign клиента"
- [ ] (P1) #10: Unit тесты для GenerateUsersAndTweetsServiceImpl — Написать unit тесты для логики выбора твитов и пользователей, обработки ошибок в GenerateUsersAndTweetsServiceImpl.
  acceptance: "Unit тесты для логики выбора твитов и пользователей, обработки ошибок (self-like, self-retweet, дубликаты) в GenerateUsersAndTweetsServiceImpl написаны, покрывают основные сценарии и edge cases"
- [ ] (P2) #11: Integration тесты — Написать integration тесты для полного цикла скрипта с лайками и ретвитами, включая edge cases.
  acceptance: "Integration тесты для полного цикла скрипта с лайками и ретвитами написаны, покрывают успешные сценарии и edge cases (мало пользователей, мало твитов)"

## Assumptions
- DTO для лайков и ретвитов (LikeTweetRequestDto, RetweetRequestDto) будут перемещены в common-lib (com.twitter.common.dto.request) для соответствия текущей архитектуре проекта, где другие DTO уже находятся в common-lib. Это потребует обновления импортов в tweet-api.
- Эндпоинты лайков и ретвитов в tweet-api работают корректно и возвращают соответствующие статус-коды (201 для успеха, 409 для self-like/self-retweet/дубликатов)
- Ошибки self-like и self-retweet обрабатываются gracefully (логируются и добавляются в errors, но не прерывают выполнение скрипта)
- Для каждой операции выбираются разные твиты из созданных (6 разных твитов для 6 операций)
- Для каждой операции выбираются разные пользователи (исключая автора твита) для избежания пересечений

## Risks
- **Технические риски:**
  - Доступность DTO: Если DTO из tweet-api недоступны, нужно будет создать копии или переместить в common-lib
  - Обработка ошибок: Self-like и self-retweet будут возвращать 409, нужно корректно обрабатывать эти ошибки
  - Производительность: Множественные HTTP вызовы могут замедлить выполнение скрипта (6 операций × N пользователей)
  - Edge cases: Мало пользователей или мало твитов может привести к невозможности выполнения всех операций
  
- **Зависимости:**
  - Доступность tweet-api во время выполнения скрипта
  - Корректная работа эндпоинтов лайков и ретвитов в tweet-api
  - Наличие достаточного количества созданных пользователей и твитов для выполнения всех операций

## Metrics & Success Criteria
- Все 6 операций (3 лайка + 3 ретвита) выполняются корректно
- Ошибки (self-like, self-retweet, дубликаты) обрабатываются gracefully (логируются и добавляются в errors, но не прерывают выполнение)
- Статистика корректно отражает количество созданных лайков и ретвитов
- Тесты покрывают основные сценарии и edge cases (покрытие > 80%)
- Документация обновлена (README, JavaDoc)
- Код соответствует стандартам проекта (STANDART_CODE.md, STANDART_PROJECT.md, STANDART_JAVADOC.md)

## Notes
- Лайки и ретвиты находятся в tweet-api (не отдельный сервис), поэтому используются Feign клиенты для вызова эндпоинтов
- Эндпоинты: POST /api/v1/tweets/{tweetId}/like и POST /api/v1/tweets/{tweetId}/retweet
- Для каждой операции выбираются разные твиты (6 разных твитов для 6 операций)
- Для каждой операции выбираются разные пользователи (исключая автора твита) для избежания пересечений и ошибок self-like/self-retweet
- Используется Collections.shuffle() для случайного выбора твитов и пользователей
- Обработка ошибок: self-like, self-retweet, дубликаты логируются и добавляются в errors, но выполнение продолжается
- Статистика обновляется: totalLikesCreated и totalRetweetsCreated добавляются в ScriptStatisticsDto