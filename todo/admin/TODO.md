# TODO: Подключение Liquibase к admin-script-api

## Meta
- project: twitter-microservices
- updated: 2025-01-27
- standards:
  - STANDART_CODE.md
  - STANDART_PROJECT.md
  - STANDART_DOCKER.md

## Задача

Подключить Liquibase к сервису admin-script-api и перенести SQL скрипты из папки `sql/` в формат Liquibase changelog для управления миграциями базы данных.

## Анализ задачи

### Текущее состояние
- В проекте есть SQL скрипты в папке `sql/`:
  - `script_1_users.sql` - создание таблицы users
  - `script_2_tweets.sql` - создание таблицы tweets
  - `script_3_follows.sql` - создание таблицы follows
  - `script_4_tweet_likes.sql` - создание таблицы tweet_likes
  - `script_5_tweet_retweets.sql` - создание таблицы tweet_retweets
- SQL скрипты монтируются в PostgreSQL через docker-compose.yml (`./sql:/docker-entrypoint-initdb.d`)
- admin-script-api использует JPA с `ddl-auto: validate`
- В проекте нет примеров использования Liquibase

### Цель
- Подключить Liquibase к admin-script-api
- Перенести SQL скрипты в формат Liquibase changelog
- Обеспечить автоматическое выполнение миграций при старте приложения
- Сохранить все ограничения, индексы и foreign keys из оригинальных SQL

### Затронутые стандарты
- **STANDART_CODE.md**: Структура проекта, использование Java 24 features
- **STANDART_PROJECT.md**: Конфигурация Spring Boot
- **STANDART_DOCKER.md**: Конфигурация docker-compose.yml (возможно, потребуется обновление)

## Структура миграций Liquibase

Миграции будут организованы следующим образом:
```
services/admin-script-api/src/main/resources/
└── db/
    └── changelog/
        ├── db.changelog-master.xml          # Master changelog
        └── changes/
            ├── 001-create-users-table.xml
            ├── 002-create-tweets-table.xml
            ├── 003-create-follows-table.xml
            ├── 004-create-tweet-likes-table.xml
            └── 005-create-tweet-retweets-table.xml
```

## Tasks

### Реализация инфраструктуры
- [x] (P1) [2026-01-15 22:28] #1: Добавить зависимость Liquibase в build.gradle admin-script-api — выполнено (добавлена зависимость org.liquibase:liquibase-core в services/admin-script-api/build.gradle)
  acceptance: "Зависимость org.liquibase:liquibase-core добавлена, проект собирается без ошибок"
- [ ] (P1) #2: Настроить Liquibase в application.yml и application-docker.yml
  acceptance: "Конфигурация Liquibase добавлена (change-log, enabled, drop-first), spring.jpa.hibernate.ddl-auto установлен в none, приложение запускается с Liquibase"
- [ ] (P1) #3: Создать master changelog файл (db.changelog-master.xml)
  acceptance: "Master changelog создан в services/admin-script-api/src/main/resources/db/changelog/, все изменения включены в правильном порядке (001-005)"

### Создание миграций
- [ ] (P1) #4: Создать changelog для таблицы users (001-create-users-table.xml)
  acceptance: "Changelog создан, все ограничения сохранены (PRIMARY KEY, UNIQUE на login и email, NOT NULL, DEFAULT для created_at), формат соответствует стандартам Liquibase"
- [ ] (P1) #5: Создать changelog для таблицы tweets (002-create-tweets-table.xml)
  acceptance: "Changelog создан, все ограничения сохранены (PRIMARY KEY, DEFAULT gen_random_uuid(), FOREIGN KEY на user_id, CHECK constraints для content, DEFAULT значения)"
- [ ] (P1) #6: Создать changelog для таблицы follows (003-create-follows-table.xml)
  acceptance: "Changelog создан, все ограничения сохранены (PRIMARY KEY, FOREIGN KEY на follower_id и following_id, UNIQUE constraint, CHECK constraint для предотвращения self-follow)"
- [ ] (P1) #7: Создать changelog для таблицы tweet_likes (004-create-tweet-likes-table.xml)
  acceptance: "Changelog создан, все ограничения сохранены (PRIMARY KEY, DEFAULT gen_random_uuid(), FOREIGN KEY на tweet_id и user_id, UNIQUE constraint)"
- [ ] (P1) #8: Создать changelog для таблицы tweet_retweets (005-create-tweet-retweets-table.xml)
  acceptance: "Changelog создан, все ограничения сохранены (PRIMARY KEY, DEFAULT gen_random_uuid(), FOREIGN KEY на tweet_id и user_id, UNIQUE constraint, NULLABLE для comment)"

### Тестирование и финализация
- [ ] (P1) #9: Протестировать выполнение миграций при старте приложения
  acceptance: "Все миграции выполняются успешно, все таблицы созданы с правильными ограничениями, foreign keys, unique constraints и check constraints работают корректно"
- [ ] (P2) #10: Обновить docker-compose.yml (опционально - рассмотреть удаление монтирования sql скриптов)
  acceptance: "docker-compose.yml обновлен (или оставлен без изменений с обоснованием решения)"
- [ ] (P2) #11: Обновить README (опционально)
  acceptance: "README обновлен с информацией о Liquibase миграциях, описана структура миграций, указано как добавлять новые миграции (если файл существует)"

## Технические детали

### Формат Liquibase changelog

Каждый changelog файл должен следовать структуре:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.24.xsd">
    
    <changeSet id="001-create-users-table" author="geron">
        <createTable tableName="users">
            <!-- columns -->
        </createTable>
    </changeSet>
</databaseChangeLog>
```

### Порядок выполнения миграций

Миграции должны выполняться в следующем порядке:
1. users (базовая таблица, нет зависимостей)
2. tweets (зависит от users)
3. follows (зависит от users)
4. tweet_likes (зависит от users и tweets)
5. tweet_retweets (зависит от users и tweets)

### Обработка PostgreSQL-специфичных функций

- `gen_random_uuid()` - использовать в DEFAULT для UUID полей
- `CURRENT_TIMESTAMP` - использовать в DEFAULT для timestamp полей

## Риски и зависимости

### Риски
1. **Конфликт с существующими SQL скриптами**: Если база данных уже инициализирована через SQL скрипты, Liquibase может попытаться создать таблицы заново
   - **Решение**: Использовать `spring.liquibase.drop-first: false` и проверить существование таблиц
2. **Порядок выполнения**: Неправильный порядок changelog файлов может привести к ошибкам foreign key
   - **Решение**: Использовать префиксы 001-005 для обеспечения правильного порядка
3. **PostgreSQL-специфичный синтаксис**: Некоторые функции могут не работать в Liquibase XML
   - **Решение**: Использовать `<sql>` теги для сложных случаев

### Зависимости
- Spring Boot должен автоматически подтянуть версию Liquibase через BOM
- PostgreSQL драйвер уже подключен
- Нет зависимостей от других сервисов для миграций

## Критерии успеха

1. ✅ Liquibase подключен к admin-script-api
2. ✅ Все SQL скрипты перенесены в формат Liquibase changelog
3. ✅ Все ограничения, индексы и foreign keys сохранены
4. ✅ Миграции выполняются автоматически при старте приложения
5. ✅ Все таблицы создаются в правильном порядке
6. ✅ Проект собирается и запускается без ошибок
7. ✅ Тесты проходят (если есть интеграционные тесты с БД)

## Assumptions

- Предполагается, что база данных может быть уже инициализирована через SQL скрипты, поэтому используется `drop-first: false`
- Предполагается, что все сервисы используют одну и ту же базу данных PostgreSQL
- Предполагается, что Liquibase будет управлять схемой только для admin-script-api (или для всей базы, если это централизованный подход)

## Notes

- SQL скрипты в папке `sql/` можно оставить для совместимости или удалить после успешной миграции на Liquibase
- В будущем можно рассмотреть централизованное управление миграциями для всех сервисов через один сервис или отдельный модуль
- Ссылки на стандарты:
  - [Code Standards](../standards/STANDART_CODE.md)
  - [Project Standards](../standards/STANDART_PROJECT.md)
  - [Docker Standards](../standards/STANDART_DOCKER.md)