# twitter

## 1. Уточнение требований (Requirements Gathering)

### Functional requirements
- Пользователи могут **регистрироваться и логиниться**.
- Пользователи могут **постить твиты** (ограничение 280 символов).
- Пользователи могут **подписываться на других пользователей**.
- Пользователи видят **ленты новостей** (timeline) — твиты от подписанных пользователей.
- Возможность лайков и ретвитов (опционально на базовом уровне).

### Non-functional requirements
- Масштабируемость: миллионы пользователей, миллиарды твитов.
- Высокая доступность.
- Низкая задержка при просмотре timeline.
- Eventual consistency допустима для лайков и репостов.

## 2. Высокоуровневая архитектура

1. **API Gateway** / Load Balancer – распределяет запросы на сервисы.
2. **User Service** – регистрация, логин, профиль.
3. **Tweet Service** – создание твитов, хранение твитов.
4. **Follow Service** – управление подписками.
5. **Timeline Service** – генерация ленты пользователя (pull vs push модель).
6. **Database**:
   - Relational DB для пользователей и подписок (PostgreSQL/MySQL)
   - NoSQL DB для твитов (Cassandra, MongoDB) – для высокой скорости записи.
7. **Cache** – Redis/Memcached для ускорения timeline и популярных твитов.
8. **Message Queue** – Kafka для асинхронной доставки твитов подписчикам (push timeline).

## 3. Модели данных

```java
public class User {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private Set<Long> followingIds; // id пользователей, на которых подписан
}

public class Tweet {
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}

public class Follow {
    private Long followerId;
    private Long followeeId;
    private LocalDateTime createdAt;
}
```

## 4. Основная логика

### 4.1 Создание твита
- Пользователь постит твит → TweetService сохраняет в DB → отправляет событие в Kafka.
- TimelineService получает событие → обновляет timeline подписчиков в Redis (push model).

Пример сервиса:
```java
@Service
public class TweetService {
    private final TweetRepository tweetRepository;
    private final KafkaTemplate<String, Tweet> kafkaTemplate;

    public Tweet postTweet(Long userId, String content) {
        Tweet tweet = new Tweet();
        tweet.setUserId(userId);
        tweet.setContent(content);
        tweet.setCreatedAt(LocalDateTime.now());
        tweet = tweetRepository.save(tweet);

        kafkaTemplate.send("tweets", tweet); // для асинхронного пуша в timeline
        return tweet;
    }
}
```

### 4.2 Получение timeline
- **Pull модель**: собираем твиты подписок в момент запроса.
- **Push модель**: timeline заранее кэшируется (Redis) при каждом твите.

Простой пример pull-модели
```java
public List<Tweet> getTimeline(Long userId, int limit) {
    Set<Long> followees = followService.getFollowees(userId);
    return tweetRepository.findTopByUserIdInOrderByCreatedAtDesc(followees, limit);
}
```

## 5. Масштабирование
- **Shard DB по userId**.
- **Tweet storage** – NoSQL (Cassandra/MongoDB) для горизонтальной масштабируемости.
- **Timeline cache** – Redis с TTL.
- **Message queue** – Kafka для асинхронного обновления timeline.

## 6. Опциональные улучшения
- Поиск твитов → ElasticSearch.
- Лайки и ретвиты → отдельная таблица или хранение в Redis.
- Rate limiting → предотвратить спам.

## Схема архитектуры с сервисами и потоками данных
![Схема архитектуры](/pictures/architecture_1.png)