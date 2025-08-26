
```
twitter/
├── build.gradle (корневой)
├── settings.gradle
├── gradle.properties
├── shared/
│   ├── common-lib/
│   │   ├── build.gradle
│   │   └── src/main/java/com/twitter/common/
│   │       ├── exception/
│   │       ├── dto/
│   │       └── util/
│   └── database/
│       ├── build.gradle
│       └── src/main/java/com/twitter/database/
├── services/
│   ├── users-api/ (переименованный users-service)
│   ├── tweets-api/
│   ├── follow-api/
│   └── timeline-api/
├── gateway/
│   └── api-gateway/
├── scripts/
│   ├── build-all.sh
│   ├── start-all.sh
│   └── docker/
├── docs/
│   ├── architecture/
│   ├── api/
│   └── deployment/
└── compose/
    └── postgre.yaml
```



Запустить сразу все сервисы из монорепозитория Gradle — это задача, которая требует создания кастомной задачи или использования плагина, так как по умолчанию Gradle не имеет встроенной команды для параллельного запуска нескольких Spring Boot приложений.

Вот несколько подходов, как это можно сделать:

1. Самый простой способ: скрипт для терминала

Это самый быстрый и легко настраиваемый способ. Вы можете создать простой скрипт (например, start-all.sh для Linux/macOS или start-all.bat для Windows), который будет запускать каждый сервис в отдельном процессе.

start-all.sh (для Linux/macOS)
Bash

#!/bin/bash

echo "Starting all services..."

# Запуск users-api в фоновом режиме
echo "Starting users-api..."
./gradlew :services:users-api:bootRun &

# Добавьте другие сервисы здесь
# echo "Starting another-service..."
# ./gradlew :services:another-service:bootRun &

echo "All services are being started in the background. Check logs for status."
echo "To stop them, use 'killall java' or find their process IDs."

Как использовать:

    Создайте файл start-all.sh в корне проекта.

    Сделайте его исполняемым: chmod +x start-all.sh.

    Запустите его: ./start-all.sh.

Недостатки:

    Управление процессами (остановка, просмотр логов) может быть неудобным. Вам придется вручную искать и останавливать процессы.

    Запуск происходит в фоновом режиме, что может усложнить отладку.

2. С помощью Docker Compose

Этот способ является стандартом для развертывания нескольких сервисов. Вы упаковываете каждый сервис в Docker-образ и управляете их жизненным циклом (запуск, остановка, взаимодействие) с помощью docker-compose.yml файла.

Шаги:

    Создайте Dockerfile для каждого сервиса.
    Например, services/users-api/Dockerfile:
    Dockerfile

FROM openjdk:21-jdk-slim
COPY build/libs/users-api-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

Соберите образы.
Вам нужно будет собрать ваш проект с помощью ./gradlew clean build и затем собрать Docker-образы.

Создайте docker-compose.yml в корне проекта.
YAML

version: '3.8'
services:
  users-api:
    build:
      context: ./services/users-api
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      # Здесь можно настроить переменные окружения, например, для подключения к БД
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/twitter
    depends_on:
      - db
    networks:
      - my-network

  # Добавьте другие сервисы здесь
  # another-service:
  #   ...

  db:
    image: postgres:14.1-alpine
    environment:
      - POSTGRES_USER=twitter
      - POSTGRES_PASSWORD=password
      - POSTGRES_DB=twitter
    ports:
      - "5432:5432"
    networks:
      - my-network

networks:
  my-network:
    driver: bridge

Запустите все сервисы:
Bash

    docker-compose up --build

Преимущества:

    Изоляция: каждый сервис работает в своем контейнере.

    Простота управления: docker-compose up для запуска, docker-compose down для остановки.

    Воспроизводимость: окружение одинаково для всех разработчиков.

    Управление зависимостями: depends_on гарантирует правильный порядок запуска.

Недостатки:

    Требует установки Docker.

    Сборка Docker-образов может быть дольше, чем обычный запуск через Gradle.

### Шаг 7: Docker и развертывание
- **Dockerfile** для каждого сервиса
- **docker-compose.yml** для локальной разработки
- **Скрипты** для сборки и запуска всех сервисов

### Шаг 9: Документация и скрипты
- **README.md** с описанием структуры
- **Скрипты** для быстрой разработки
- **Документация** по архитектуре