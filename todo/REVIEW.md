## ⚠️ Замечания и потенциальные проблемы


### 9. Документация

#### Проблема: Неполная документация по развертыванию
- Нет инструкций по Docker Compose
- Отсутствует описание переменных окружения

---

## 💡 Рекомендации по улучшению


### 8. Документация


#### Рекомендация: Создать Docker Compose
```yaml
# docker-compose.yml
version: '3.8'
services:
  users-api:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DATABASE_URL=jdbc:postgresql://postgres:5432/twitter
    depends_on:
      - postgres
      
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: twitter
      POSTGRES_USER: user
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
```