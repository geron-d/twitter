# Стратегия централизации через dependencyManagement

## Анализ текущего состояния

### Существующая структура
В корневом build.gradle уже есть блок `subprojects` с `dependencyManagement`:
```gradle
subprojects {
    apply plugin: 'io.spring.dependency-management'

    dependencyManagement {
        imports {
            mavenBom "org.springframework.boot:spring-boot-dependencies:3.5.5"
            mavenBom "org.testcontainers:testcontainers-bom:1.21.3"
        }
    }
}
```

### Проблема
Подпроекты используют хардкодные версии для зависимостей, которые не управляются Spring Boot BOM:
- Swagger Annotations
- SpringDoc OpenAPI
- Lombok
- MapStruct
- PostgreSQL Driver

## Рекомендуемое решение

### Расширение dependencyManagement
Добавить версии для внешних зависимостей в существующий блок `dependencyManagement`:

```gradle
subprojects {
    apply plugin: 'io.spring.dependency-management'

    dependencyManagement {
        imports {
            mavenBom "org.springframework.boot:spring-boot-dependencies:3.5.5"
            mavenBom "org.testcontainers:testcontainers-bom:1.21.3"
        }
        
        dependencies {
            // OpenAPI/Swagger
            dependency "io.swagger.core.v3:swagger-annotations:2.2.38"
            dependency "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13"
            
            // Code Generation
            dependency "org.projectlombok:lombok:1.18.38"
            dependency "org.mapstruct:mapstruct:1.6.3"
            dependency "org.mapstruct:mapstruct-processor:1.6.3"
            dependency "org.projectlombok:lombok-mapstruct-binding:0.2.0"
            
            // Database
            dependency "org.postgresql:postgresql:42.7.7"
        }
    }
}
```

### Обновление подпроектов
Удалить все хардкодные версии из подпроектов:

**users-api/build.gradle:**
```gradle
dependencies {
    // Зависимости на shared модули
    implementation project(':shared:common-lib')
    implementation project(':shared:database')

    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    
    // OpenAPI/Swagger документация
    implementation 'io.swagger.core.v3:swagger-annotations'
    implementation('org.springdoc:springdoc-openapi-starter-webmvc-ui') {
        exclude group: "io.swagger.core.v3", module: "swagger-annotations"
    }
    
    // Трейсинг и мониторинг
    implementation 'io.micrometer:micrometer-tracing-bridge-otel'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    implementation 'org.mapstruct:mapstruct'
    annotationProcessor 'org.mapstruct:mapstruct-processor'
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding'

    runtimeOnly 'org.postgresql:postgresql'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.junit.jupiter:junit-jupiter-api'
    testImplementation 'org.junit.jupiter:junit-jupiter-engine'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:postgresql'
}
```

**common-lib/build.gradle:**
```gradle
dependencies {
    api 'org.springframework.boot:spring-boot-starter'
    api 'org.springframework.boot:spring-boot-starter-aop'
    api 'org.springframework.boot:spring-boot-starter-validation'
    api 'org.springframework.boot:spring-boot-starter-web'

    implementation 'io.swagger.core.v3:swagger-annotations'
    implementation('org.springdoc:springdoc-openapi-starter-webmvc-ui') {
        exclude group: "io.swagger.core.v3", module: "swagger-annotations"
    }

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    implementation 'org.mapstruct:mapstruct'
    annotationProcessor 'org.mapstruct:mapstruct-processor'
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

## Преимущества подхода через dependencyManagement

1. **Консистентность**: Все версии управляются централизованно
2. **Совместимость**: Работает с существующей Spring Boot инфраструктурой
3. **Простота**: Не требует ext блоков и переменных
4. **Стандартность**: Использует стандартный Gradle механизм управления зависимостями
5. **Гибкость**: Легко переопределить версии при необходимости

## Риски и митигация

### Риск: Конфликт с Spring Boot BOM
**Митигация**: Использование совместимых версий, тестирование сборки

### Риск: Нарушение exclude блоков
**Митигация**: Сохранение всех exclude блоков при удалении версий

### Риск: Проблемы с annotation processors
**Митигация**: Проверка корректности работы MapStruct и Lombok

## Следующие шаги

1. Расширить dependencyManagement в корневом build.gradle
2. Удалить хардкодные версии из users-api/build.gradle
3. Удалить хардкодные версии из common-lib/build.gradle
4. Провести валидацию сборки
5. Обновить документацию
