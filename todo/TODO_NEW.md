- протестировать создание
- заимпортить ошибки в TweetService
- сделать ошибки по аналогии с UserValidatorImpl
- сделать в users-api existsUser и использовать в tweet
- open-api для контроллера


- подумать над тем чтобы вынести изменения updated_At твита из бд в код (может быть аудитинг) (вроде как реализовано, проверить псоле реализации метода update и удалить тригер)
- удалить лишние методы из TweetRepository
- удалить лишние методы из TweetMapper

Есть проект twitter
Архитектура сервиса tweet-api todo\tweet\TWEET_API_ARCHITECTURE.md
Старый план todo\tweet\TWEET_API_COMMON.md
Новый план todo\tweet\TWEET_API_COMMON_2.md
Нужно составить новый план реализации запроса для управления твитами 
POST   /api/v1/tweets                    # Создать твит
