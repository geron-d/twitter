# Multi-stage build for Twitter Follower API
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
RUN ./gradlew :services:follower-api:build -x test --no-daemon --parallel --build-cache

# Stage 2: Runtime stage
FROM eclipse-temurin:24-jre

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/services/follower-api/build/libs/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8084

# Set JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
