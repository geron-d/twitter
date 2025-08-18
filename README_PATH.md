# Base stack
- БД Postgres
- REST

## 1. Создание user-service
- Создание пользователя
- Редактирование данных пользователя
- Получение данных по пользователю

```java
public class User {
    private Long id;
    private String username;
    private String firstName;
    private String secondName;
    private String email;
    private String passwordHash;
    private Set<Long> followingIds; // id пользователей, на которых подписан
}
```

## 2. Создание tweet-service
- Публикация твита (280 символов max)

```java
public class Tweet {
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}
```

## 3. follow-service
- подписка на пользователя
- отписка

```java
public class Follow {
    private Long followerId;
    private Long followeeId;
    private LocalDateTime createdAt;
}
```

## Future
- твиты в монго
- твиты в касандра
- Neo4j для подписки в follow-service