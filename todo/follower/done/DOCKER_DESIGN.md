# Follower API Service - Docker Configuration Design

## Дата создания
2025-01-27

## Обзор
Документ содержит проектирование Docker конфигурации для микросервиса follower-api, включая структуру Dockerfile, application-docker.yml и конфигурацию в docker-compose.yml.

---

## 1. Dockerfile Design

### 1.1 Общая структура
Dockerfile будет использовать multi-stage build паттерн, аналогичный другим сервисам проекта (users-api, tweet-api, admin-script-api).

### 1.2 Stage 1: Build Stage
- **Base Image:** `gradle:jdk24`
- **Purpose:** Сборка приложения с использованием Gradle
- **Steps:**
  1. Установка рабочей директории `/app`
  2. Копирование Gradle файлов для кэширования (gradle/, gradlew, gradlew.bat, build.gradle, settings.gradle, gradle.properties)
  3. Копирование всей структуры проекта
  4. Установка прав на выполнение для gradlew
  5. Загрузка зависимостей (`./gradlew dependencies --no-daemon`)
  6. Сборка приложения (`./gradlew :services:follower-api:build -x test --no-daemon --parallel --build-cache`)

### 1.3 Stage 2: Runtime Stage
- **Base Image:** `eclipse-temurin:24-jre`
- **Purpose:** Запуск приложения в production окружении
- **Steps:**
  1. Установка curl для healthcheck (`apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*`)
  2. Создание non-root пользователя (`groupadd -r appuser && useradd -r -g appuser appuser`)
  3. Установка рабочей директории `/app`
  4. Копирование JAR файла из build stage (`COPY --from=build /app/services/follower-api/build/libs/*.jar app.jar`)
  5. Создание директории для логов (`mkdir -p /app/logs && chown -R appuser:appuser /app`)
  6. Переключение на non-root пользователя (`USER appuser`)
  7. Открытие порта 8084 (`EXPOSE 8084`)
  8. Настройка JVM опций (`ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"`)
  9. Настройка healthcheck (`HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 CMD curl -f http://localhost:8084/actuator/health || exit 1`)
  10. Запуск приложения (`ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]`)

### 1.4 Ключевые особенности
- Multi-stage build для уменьшения размера финального образа
- Использование Java 24 (JDK для сборки, JRE для runtime)
- Non-root пользователь для безопасности
- Оптимизированные JVM опции для контейнера
- Healthcheck на `/actuator/health` с интервалом 30 секунд
- Порт 8084 (согласно требованиям)

---

## 2. application-docker.yml Design

### 2.1 Общая структура
Файл будет содержать конфигурацию для Docker окружения, аналогичную другим сервисам проекта.

### 2.2 Конфигурация Spring Profile
- **Profile:** `docker`
- **Purpose:** Активация конфигурации для Docker окружения

### 2.3 Конфигурация интеграции с users-api
- **Property:** `app.users-api.base-url`
- **Value:** `http://users-api:8081`
- **Purpose:** URL для Feign клиента, использующий имя сервиса Docker вместо localhost
- **Note:** Имя сервиса `users-api` соответствует имени сервиса в docker-compose.yml

### 2.4 Конфигурация базы данных
- **Property:** `spring.datasource.url`
- **Value:** `jdbc:postgresql://postgres:5432/twitter`
- **Purpose:** URL подключения к PostgreSQL, использующий имя сервиса Docker `postgres`
- **Note:** Имя сервиса `postgres` соответствует имени сервиса в docker-compose.yml

### 2.5 Структура файла
```yaml
# Follower API specific configuration for Docker environment
app:
  users-api:
    base-url: http://users-api:8081
```

**Note:** Конфигурация базы данных будет передаваться через environment variables в docker-compose.yml, а не через application-docker.yml, что соответствует паттерну других сервисов.

---

## 3. docker-compose.yml Configuration Design

### 3.1 Общая структура
Конфигурация будет добавлена в существующий файл `docker-compose.yml` в корне проекта, следуя паттерну других сервисов.

### 3.2 Сервис follower-api
- **Service Name:** `follower-api`
- **Container Name:** `twitter-follower-api`
- **Build Context:** `.` (корень проекта)
- **Dockerfile Path:** `services/follower-api/Dockerfile`

### 3.3 Порты
- **Host Port:** `8084`
- **Container Port:** `8084`
- **Mapping:** `8084:8084`

### 3.4 Environment Variables
- `SPRING_PROFILES_ACTIVE=docker` - Активация Docker профиля
- `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/twitter` - URL базы данных через имя сервиса Docker
- `SPRING_DATASOURCE_USERNAME=user` - Имя пользователя БД
- `SPRING_DATASOURCE_PASSWORD=password` - Пароль БД
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver` - Драйвер БД
- `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` - Режим валидации схемы БД
- `SPRING_JPA_SHOW_SQL=false` - Отключение вывода SQL в логи
- `LOGGING_LEVEL_COM_TWITTER=DEBUG` - Уровень логирования для пакета com.twitter
- `LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG` - Уровень логирования для SQL запросов Hibernate
- `USERS_API_URL=http://users-api:8081` - URL users-api для Feign клиента (опционально, если используется через environment variable)

### 3.5 Зависимости (depends_on)
- `postgres` с условием `service_healthy` - Ожидание готовности PostgreSQL
- `users-api` с условием `service_healthy` - Ожидание готовности users-api (необходимо для валидации существования пользователей)

### 3.6 Network
- **Network:** `twitter-network`
- **Purpose:** Связь с другими сервисами (postgres, users-api)

### 3.7 Volumes
- `./logs:/app/logs` - Монтирование директории логов для доступа с хоста

### 3.8 Restart Policy
- `restart: unless-stopped` - Автоматический перезапуск при сбое

### 3.9 Healthcheck
- **Test:** `["CMD", "curl", "-f", "http://localhost:8084/actuator/health"]`
- **Interval:** `30s` - Интервал проверки
- **Timeout:** `10s` - Таймаут проверки
- **Retries:** `3` - Количество попыток перед пометкой как unhealthy
- **Start Period:** `60s` - Период ожидания перед началом проверок (время на запуск приложения)

### 3.10 Полная структура конфигурации
```yaml
  # Follower API Service
  follower-api:
    build:
      context: .
      dockerfile: services/follower-api/Dockerfile
    container_name: twitter-follower-api
    ports:
      - "8084:8084"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/twitter
      SPRING_DATASOURCE_USERNAME: user
      SPRING_DATASOURCE_PASSWORD: password
      SPRING_DATASOURCE_DRIVER_CLASS_NAME: org.postgresql.Driver
      SPRING_JPA_HIBERNATE_DDL_AUTO: validate
      SPRING_JPA_SHOW_SQL: false
      LOGGING_LEVEL_COM_TWITTER: DEBUG
      LOGGING_LEVEL_ORG_HIBERNATE_SQL: DEBUG
    depends_on:
      postgres:
        condition: service_healthy
      users-api:
        condition: service_healthy
    networks:
      - twitter-network
    volumes:
      - ./logs:/app/logs
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8084/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
```

---

## 4. Порядок развертывания

### 4.1 Зависимости
1. **postgres** - Должен быть запущен первым и быть healthy
2. **users-api** - Должен быть запущен и быть healthy перед follower-api
3. **follower-api** - Запускается после postgres и users-api

### 4.2 Проверка развертывания
После развертывания можно проверить работу сервиса:
```bash
# Проверка healthcheck
curl http://localhost:8084/actuator/health

# Проверка Swagger документации
curl http://localhost:8084/swagger-ui.html
```

---

## 5. Соответствие стандартам проекта

### 5.1 Паттерны
- ✅ Multi-stage build (как в других сервисах)
- ✅ Использование Java 24 (JDK для сборки, JRE для runtime)
- ✅ Non-root пользователь для безопасности
- ✅ Healthcheck на `/actuator/health`
- ✅ Использование имен сервисов Docker для межсервисной коммуникации
- ✅ Конфигурация через environment variables в docker-compose.yml
- ✅ Использование профиля `docker` для Docker окружения

### 5.2 Безопасность
- ✅ Non-root пользователь в контейнере
- ✅ Минимальный runtime образ (JRE вместо JDK)
- ✅ Отдельная сеть для изоляции сервисов

### 5.3 Производительность
- ✅ Оптимизированные JVM опции для контейнера
- ✅ Кэширование слоев Docker (копирование Gradle файлов первым)
- ✅ Параллельная сборка Gradle

---

## 6. Предположения

1. Сервис будет работать на порту 8084 (согласно требованиям)
2. База данных PostgreSQL уже настроена и доступна через имя сервиса `postgres`
3. Сервис users-api доступен через имя сервиса `users-api` на порту 8081
4. Все сервисы находятся в одной Docker сети `twitter-network`
5. Директория `./logs` существует в корне проекта для монтирования логов
6. SQL скрипт для создания таблицы `follows` будет выполнен при инициализации PostgreSQL через volume `./sql:/docker-entrypoint-initdb.d`

---

## 7. Риски и ограничения

### 7.1 Риски
1. **Зависимость от users-api:** Если users-api недоступен, follower-api не сможет валидировать существование пользователей. Решение: healthcheck и depends_on с условием service_healthy.
2. **Зависимость от PostgreSQL:** Если PostgreSQL недоступен, сервис не запустится. Решение: healthcheck и depends_on с условием service_healthy.
3. **Порты:** Порт 8084 должен быть свободен на хосте. Решение: проверка перед запуском.

### 7.2 Ограничения
- Сервис требует минимум 512MB RAM (Xms512m)
- Сервис требует максимум 1024MB RAM (Xmx1024m)
- Healthcheck требует установки curl в контейнере

---

## 8. Следующие шаги

После завершения проектирования Docker конфигурации:
1. Создание Dockerfile в `services/follower-api/Dockerfile`
2. Создание application-docker.yml в `services/follower-api/src/main/resources/application-docker.yml`
3. Обновление docker-compose.yml с добавлением сервиса follower-api
4. Тестирование сборки образа: `docker build -f services/follower-api/Dockerfile -t twitter-follower-api .`
5. Тестирование развертывания через docker-compose: `docker-compose up -d follower-api`
6. Проверка healthcheck: `curl http://localhost:8084/actuator/health`

