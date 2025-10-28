# Base stack
- БД Postgres
- REST

## 1.1 Создание user-service

## 2. Создание tweet-service
- Публикация твита (280 символов max)

```java
public class Tweet {
    private UUID id;
    private UUID userId;
    private String content;
    private LocalDateTime createdAt;
}
```

## 3. follow-service
- подписка на пользователя
- отписка

```java
public class Follow {
    private UUID followerId;
    private UUID followeeId;
    private LocalDateTime createdAt;
}
```

## 4. timeline-service
генерация ленты пользователя из бд по подпискам

## 5. api-gateway


## Future
- добавить логирование изменений сущностей с использоватем JPA (Hibernate)
- правильный подход для работы с ролями и статусами пользователя
- узнать что такое openTelemetry и подключить его (собирать все логи в 1 точку opentelemetry, zipkin, elk)
- твиты в монго
- твиты в касандра
- Neo4j для подписки в follow-service
- лайки в follow-service (отдельная таблица)
- spring-security в api-gateway
- spring-gateway в api-gateway
- ретвиты (отдельная таблица)
- redis с ttl для популярных твитов
- Kafka для асинхронной доставки твитов подписчикам (push timeline). (TimelineService получает событие → обновляет timeline подписчиков в Redis (push model))
- Shard DB по userId для твитов
- Поиск твитов → ElasticSearch.
- Rate limiting → предотвратить спам. (api-gateway)
- (профили, регистрации, аутентификация) в users-api
- Redis для профилей пользователей
- сервис уведомлений (с кафкой)
- Используем Hadoop HDFS или S3 для хранения архивов твитов, которые не нужны в реальном времени.
- Используем S3 для хранения архивов твитов, которые не нужны в реальном времени.
- Мониторинг и логирование: Используем Prometheus/Grafana для метрик и ELK Stack (Elasticsearch, Logstash, Kibana) для агрегации логов.
- Поэкспериминтировать с настройкой spring.jpa.open0in-view (https://www.youtube.com/watch?v=gBLyiBOc_Cg&t=2225s)
- Добавить кастомные метрики
- метрики производительности операций
- Внедрить rate limiting
- Усилить валидацию паролей
- Добавить кэширование
- Оптимизировать запросы (Добавить индексы в БД)
- Настроить алерты
- Добавить контрактные тесты
- N+1 проблема в фильтрации
- Добавить фильтрацию по дате создания пользователя
- Добавить аудит для пользователей
- проверить как   tracing:  sampling:  probability: 1.0 влияет на производительность
- сделать так чтобы в url не было id пользователя (для секурити)
- проработать ускорение билда докеробразов сервисов