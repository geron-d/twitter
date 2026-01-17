# Changelog

## 2026-01-15 22:28 — step #1 done — Добавлена зависимость Liquibase в build.gradle admin-script-api — автор: assistant

## 2026-01-15 23:03 — step #2 done — Настроена конфигурация Liquibase в application.yml и application-docker.yml — автор: assistant

## 2026-01-15 23:04 — step #3 done — Создан master changelog файл (db.changelog-master.xml) — автор: assistant

## 2026-01-15 23:05 — step #4 done — Создан changelog для таблицы users (001-create-users-table.xml) — автор: assistant

## 2026-01-15 23:07 — step #5 done — Создан changelog для таблицы tweets (002-create-tweets-table.xml) — автор: assistant

## 2026-01-15 23:09 — step #6 done — Создан changelog для таблицы follows (003-create-follows-table.xml) — автор: assistant

## 2026-01-15 23:10 — step #7 done — Создан changelog для таблицы tweet_likes (004-create-tweet-likes-table.xml) — автор: assistant

## 2026-01-15 23:11 — step #8 done — Создан changelog для таблицы tweet_retweets (005-create-tweet-retweets-table.xml) — автор: assistant

## 2026-01-15 23:12 — step #9 done — Проверена структура миграций, добавлены инструкции по тестированию — автор: assistant

## 2026-01-27 12:00 — step #10 done — Обновлен docker-compose.yml: удалено монтирование SQL скриптов, миграции теперь управляются через Liquibase — автор: assistant

## 2026-01-27 12:05 — step #11 done — Обновлен README: добавлен раздел о Liquibase миграциях с описанием структуры, порядка выполнения и инструкциями по добавлению новых миграций — автор: assistant