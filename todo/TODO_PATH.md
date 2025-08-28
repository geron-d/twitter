# Base stack
- БД Postgres
- REST

## 0
- отредактировать user-service по видео
- как лучше его сделать на последнем spring boot с лучшими практиками


## 1.1 Создание user-service
- Создание пользователя
- Редактирование данных пользователя
- Получение данных по пользователю

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