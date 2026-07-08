сделать инфру как в https://www.youtube.com/watch?v=V4oFJ3LbW9s

настроить энверсы для таблиц
    - конфиги 3.05
    - сущности для ревизии 3.40

сдлать логирование через logback

узнать что такое в V0__init_schema.sql

поправить создание таблиц с учетом 
id         UUID PRIMARY KEY                     DEFAULT uuid_generate_v4(),
created    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),

утильный клас для получения текущего времени DateTimeUtil   3.35

исключения в стиле PersonException 3.36

сущности в стиле 3.37 3.42

soft delete 3.56

разобраться с очередностью сервисов, потому что скрипты в admin-tool

мапперы 3.57

настроить сущности через open-api в отдельной ветке

dockerfile 4.20

docker compose service 4.25

makefile 4.27 4.39

tempo 4.36 6.44

nexus 4.38

prometheus 4.41

loki 4.42

тестирование 6.46

настроить в grafana 
    - dashbord для отслеживания ошибок и исключений
    - alertmanager
    - сохранить в json для восстановления

разобраться как смотреть метрики в prometheus