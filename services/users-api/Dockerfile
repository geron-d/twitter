# Multi-stage build for Twitter Users API
# Stage 1: Build stage with Gradle
FROM gradle:jdk24 AS build

# Set working directory
WORKDIR /app

# Copy gradle files first for better caching
COPY gradle/ gradle/
COPY gradlew gradlew.bat ./
COPY build.gradle settings.gradle ./
COPY gradle.properties ./

# Copy the entire project structure
COPY . .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies first (this layer will be cached)
RUN ./gradlew dependencies --no-daemon

# Build the application with optimized flags
RUN ./gradlew :services:users-api:build -x test --no-daemon --parallel --build-cache

# Stage 2: Runtime stage
FROM eclipse-temurin:24-jre

# Create app user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/services/users-api/build/libs/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Set JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]