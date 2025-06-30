# =============================================================================
# CI ANOMALY DETECTOR - PRODUCTION DOCKERFILE
# =============================================================================
# Multi-stage Docker build for the CI/CD Anomaly Detection & AI Analysis System
# Optimized for production deployment with security and performance considerations
# =============================================================================

# =============================================================================
# BUILD STAGE - Gradle Build Environment
# =============================================================================
FROM gradle:jdk21 AS builder

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradle/ gradle/
COPY gradlew gradlew.bat build.gradle settings.gradle ./
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew clean build -x test --no-daemon

# =============================================================================
# RUNTIME STAGE - Production Environment
# =============================================================================
FROM openjdk:21-jdk-slim

# Metadata labels
LABEL maintainer="Khasan Abdurakhmanov <ya.hasan2001@yandex.ru>"
LABEL description="CI/CD Anomaly Detection & AI Analysis System"
LABEL version="1.0.0"
LABEL vendor="Inno"

# Create application user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    dumb-init \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# Set working directory
WORKDIR /app

# Create necessary directories
RUN mkdir -p /app/logs /app/templates \
    && chown -R appuser:appuser /app

# Copy built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Copy templates and configuration files
COPY --from=builder /app/src/main/resources/templates/ /app/templates/

# Copy the startup script and make it executable
COPY --chown=appuser:appuser docker-start.sh /app/docker-start.sh
RUN chmod +x /app/docker-start.sh

# Set proper permissions
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8282


# Environment variables with defaults
ENV JAVA_OPTS="-XX:InitialRAMPercentage=50 -XX:MaxRAMPercentage=75 -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs -Djava.security.egd=file:/dev/./urandom"
ENV SERVER_PORT=8282

# Use the startup script as entrypoint with Java command as arguments
ENTRYPOINT ["/app/docker-start.sh"]

# Start the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
