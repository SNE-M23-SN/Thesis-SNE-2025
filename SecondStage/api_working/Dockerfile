# =============================================================================
# UPDATED DOCKERFILE - INTEGRATED WITH STARTUP SCRIPT
# =============================================================================
FROM gradle:jdk21 AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./gradlew
COPY src ./src
RUN chmod +x ./gradlew && ./gradlew --no-daemon clean build -x test

# =============================================================================
# STAGE 2: RUNTIME STAGE
# =============================================================================
FROM openjdk:21-jdk-slim
LABEL maintainer="CI Anomaly Detector Team"
LABEL version="1.0.0"
LABEL description="CI Anomaly Detector API - Jenkins monitoring with AI-powered anomaly detection"

RUN apt-get update && apt-get install -y \
    curl \
    wget \
    dumb-init \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# Copy the startup script and make it executable
COPY --chown=appuser:appuser docker-start.sh /app/docker-start.sh
RUN chmod +x /app/docker-start.sh

# Copy application files
COPY --from=builder /app/build/libs/*.jar app.jar
COPY --chown=appuser:appuser src/main/resources/application*.properties /app/config/

# Set permissions (directories created by docker-start.sh)
RUN chown -R appuser:appuser /app

USER appuser


ENV JAVA_OPTS="-XX:InitialRAMPercentage=50 -XX:MaxRAMPercentage=75 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

# Use the startup script as entrypoint with Java command as arguments
ENTRYPOINT ["/app/docker-start.sh"]
# Start the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
