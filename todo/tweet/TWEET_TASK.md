Задача:
Есть проект twitter
Есть сервис где уже реализована часть функционала
Архитектура сервиса tweet-api todo\tweet\TWEET_API_ARCHITECTURE.md
План todo\tweet\TWEET_API_COMMON_2.md
Нужно составить план реализации запроса: Получить ленту новостей пользователя (с пагинацией)
GET    /api/v1/tweets/timeline/{userId}
Для реализации подписок был реализован сервис follower-api