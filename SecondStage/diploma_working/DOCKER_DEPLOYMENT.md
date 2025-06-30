# CI Anomaly Detector - Docker Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying the CI/CD Anomaly Detection & AI Analysis System using Docker. The system is designed as a **single containerized application** that connects to external services (PostgreSQL, RabbitMQ, and Google Gemini AI) for comprehensive Jenkins CI/CD pipeline monitoring and anomaly detection.

## Architecture

```
    ┌─────────────────────┐    ┌─────────────────────┐ ┌─────────────────────┐
    │  PostgreSQL (Ext.)  │    │  Google Gemini AI   │ | RabbitMQ (External) |
    │  Port: 5432         │    │  (External API)     │ | Port : 5672         |
    └─────────────────────┘    └─────────────────────┘ └─────────────────────┘
           │                           │                          |
           │                           │                          │                    
           ▼                           ▼                          ▼                    
┌────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                  CI  Anomaly   Detector App                                        |
│                                  Port: 8282  (Docker Container)                                    |
│                                                                                                    |
│  Features:                                                                                         |
│  • Processes 14 log types per Jenkins build                                                        |
│  • AI-powered anomaly detection with Google Gemini 2.5 Flash Preview                               |
│  • Real-time RabbitMQ message processing                                                           |
│  • PostgreSQL with JSONB for flexible log storage                                                  |
│  • Conversation-based memory for contextual AI analysis                                            |
│  • Basic application monitoring (Note: Actuator endpoints require additional dependency)           |
└────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

## Prerequisites

### Required External Services

1. **PostgreSQL Database**
   - Version: 12+ with JSONB support
   - Database: `ci_anomaly_detector` (or `postgres` for default)
   - User with full permissions
   - **Default connection**: `127.0.0.1:5432/postgres`

2. **RabbitMQ Message Broker**
   - Version: 3.8+
   - Management plugin enabled (optional)
   - Queue: `Jenkins` (configurable)
   - **Default connection**: `130.193.49.138:5672` (development) / `rabbitmq:5672` (Docker)
   - User with appropriate permissions

4. **Google Gemini AI API**
   - Valid API key required
   - Model: `gemini-2.5-flash-preview-05-20` (Docker default) / `gemini-2.5-flash-lite-preview-06-17` (application default)
   - API endpoint: `https://generativelanguage.googleapis.com`

### Required Software

- Docker Engine 20.10+
- 4GB+ available RAM
- 10GB+ available disk space
- Network access to external services

## Quick Start

### 1. Clone and Prepare

```bash
# Clone the repository
git clone <repository-url>
cd diploma_working

```

### 2. Configure Environment

Edit `.env.production` file with your configuration values:

```bash
# Required: Google Gemini AI API Key (SECURITY WARNING: Never use default/hardcoded keys in production)
AI_API_KEY=your_google_gemini_api_key_here

# Required: PostgreSQL Connection
DB_URL=jdbc:postgresql://your-postgres-host:5432/ci_anomaly_detector
DB_USERNAME=postgres
DB_PASSWORD=your_secure_database_password_here

# Required: RabbitMQ Connection (External)
RABBITMQ_HOST=your-rabbitmq-host
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_PORT=5672

# Optional: Server Configuration
SERVER_PORT=8282
LOG_LEVEL_APP=INFO

# Optional: AI Configuration (Note: Docker default differs from application default)
AI_MODEL=gemini-2.5-flash-preview-05-20
AI_TEMPERATURE=0
AI_RESPONSE_FORMAT=JSON_OBJECT
```

### 3. Deploy

```bash
# Build and start the application
docker-compose --env-file .env.production up -d

# Check service status
docker-compose ps

# View application logs
docker-compose logs -f ci-anomaly-detector

# Check application health
curl http://localhost:8282/actuator/health
```

## Configuration

### Environment Variables

| Category | Variable | Description | Default | Required |
|----------|----------|-------------|---------|----------|
| **Server** | `SERVER_PORT` | Application port | `8282` | No |
| **AI** | `AI_API_KEY` | Google Gemini API key | - | **Yes** |
| **AI** | `AI_MODEL` | AI model name | `gemini-2.5-flash-preview-05-20` (Docker) / `gemini-2.5-flash-lite-preview-06-17` (App) | No |
| **AI** | `AI_TEMPERATURE` | AI response randomness | `0` (Docker) / `0.3` (App) | No |
| **AI** | `AI_RESPONSE_FORMAT` | Response format | `JSON_OBJECT` | No |
| **Database** | `DB_URL` | PostgreSQL connection URL | `jdbc:postgresql://127.0.0.1:5432/postgres` | **Yes** |
| **Database** | `DB_USERNAME` | Database username | `postgres` | **Yes** |
| **Database** | `DB_PASSWORD` | Database password | `postgres` | **Yes** |
| **Database** | `DB_DDL_AUTO` | Hibernate DDL mode | `update` | No |
| **RabbitMQ** | `RABBITMQ_HOST` | RabbitMQ hostname | `rabbitmq` (Docker) / `130.193.49.138` (App) | **Yes** |
| **RabbitMQ** | `RABBITMQ_USERNAME` | RabbitMQ username | `guest` | **Yes** |
| **RabbitMQ** | `RABBITMQ_PASSWORD` | RabbitMQ password | `guest` | **Yes** |
| **RabbitMQ** | `RABBITMQ_PORT` | RabbitMQ port | `5672` | No |
| **Queue** | `QUEUE_NAME` | Main queue name | `Jenkins` | No |
| **Queue** | `QUEUE_DLQ_NAME` | Dead letter queue | `Jenkins.dlq` | No |
| **Queue** | `QUEUE_TTL` | Message TTL (ms) | `150000` | No |
| **Logging** | `LOG_LEVEL_APP` | Application log level | `DEBUG` | No |
| **Logging** | `LOG_LEVEL_ROOT` | Root log level | `INFO` | No |
| **Memory** | `AI_MEMORY_MAX_MESSAGES` | Max messages per conversation | `100` | No |

### Volume Mounts

- `./logs:/app/logs` - Application logs (optional)
- `./templates:/app/templates:ro` - Custom AI templates (optional)

### Log Processing Pipeline

The system processes **14 distinct log types** per Jenkins build:

1. **build_log_data** - Initial build execution logs
2. **secret_detection** - Build log secret scanning
3. **dependency_data** - Dependency analysis & vulnerabilities
4. **secret_detection** - Jenkinsfile secret scanning
5. **code_changes** - Git commit & change analysis
6. **additional_info_agent** - Jenkins agent system metrics
7. **additional_info_controller** - System health metrics
8. **additional_info_controller** - JVM performance data
9. **sast_scanning** - Semgrep SAST results
10. **sast_scanning** - Bearer security scanning
11. **sast_scanning** - Trivy vulnerability scanning
12. **build_log_data** - Final build execution logs
13. **secret_detection** - Final build log secrets
14. **sast_scanning** - Horusec security scanning (if SCM available)

**AI Analysis Trigger**: The system triggers AI analysis when 2 BuildLogData logs are received and the final SecretDetection with source="build_log" is processed.

## Service Management

### Start Services

```bash
# Start the application
docker-compose --env-file .env.production up -d

# Start with specific service name
docker-compose --env-file .env.production up -d ci-anomaly-detector

# Start with build (rebuild if needed)
docker-compose --env-file .env.production  up -d --build

# Start in foreground (see logs directly)
docker-compose --env-file .env.production  up
```

### Stop Services

```bash
# Stop the application
docker-compose --env-file .env.production  down

# Stop and remove volumes
docker-compose --env-file .env.production  down -v

# Stop and remove everything including images
docker-compose --env-file .env.production  down --rmi all
```

### Restart Services

```bash
# Restart the application
docker-compose --env-file .env.production  restart

# Restart with rebuild
docker-compose --env-file .env.production  down && docker-compose --env-file .env.production  up -d --build
```

## Monitoring

### Important Note on Monitoring Endpoints

**⚠️ ACTUATOR ENDPOINTS LIMITATION**: The current build configuration does not include the Spring Boot Actuator dependency (`spring-boot-starter-actuator`). The monitoring endpoints documented below will **NOT be available** unless this dependency is added to `build.gradle`.

### Health Checks (Requires Actuator Dependency)

```bash
# Application health (NOT AVAILABLE without actuator dependency)
curl http://localhost:8282/actuator/health

# Database connectivity (NOT AVAILABLE without actuator dependency)
curl http://localhost:8282/actuator/health/db

# Detailed health information (NOT AVAILABLE without actuator dependency)
curl http://localhost:8282/actuator/health | jq '.'
```

### Logs

```bash
# Application logs (real-time)
docker-compose logs -f ci-anomaly-detector

# Application logs (last 100 lines)
docker-compose logs --tail=100 ci-anomaly-detector

# Filter logs by level
docker-compose logs ci-anomaly-detector | grep ERROR

# Export logs to file
docker-compose logs ci-anomaly-detector > app-logs.txt
```

### Metrics & Endpoints (Requires Actuator Dependency)

**⚠️ IMPORTANT**: These endpoints are **NOT AVAILABLE** in the current build configuration. To enable them, add the following dependency to `build.gradle`:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

Potential monitoring endpoints (if actuator dependency is added):

| Endpoint | Description | Example |
|----------|-------------|---------|
| `/actuator/health` | Overall application health | `curl http://localhost:8282/actuator/health` |
| `/actuator/health/db` | Database connectivity | `curl http://localhost:8282/actuator/health/db` |
| `/actuator/metrics` | Application metrics | `curl http://localhost:8282/actuator/metrics` |
| `/actuator/info` | Application information | `curl http://localhost:8282/actuator/info` |
| `/actuator/env` | Environment variables | `curl http://localhost:8282/actuator/env` |

### Container Monitoring

```bash
# Check container status
docker-compose ps

# Check container resource usage
docker stats $(docker-compose ps -q)

# Check container logs for errors
docker-compose logs ci-anomaly-detector | grep -i error

# Access container shell
docker-compose exec ci-anomaly-detector /bin/bash
```

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   ```bash
   # Check database connectivity from container (NOT AVAILABLE without actuator dependency)
   # docker-compose exec ci-anomaly-detector curl -f http://localhost:8282/actuator/health/db

   # Check database connection manually
   docker-compose exec ci-anomaly-detector nc -zv your-postgres-host 5432

   # Check application logs for database errors
   docker-compose logs ci-anomaly-detector | grep -i "database\|postgres\|connection"
   ```

2. **RabbitMQ Connection Issues**
   ```bash
   # Check RabbitMQ connectivity from container
   docker-compose exec ci-anomaly-detector nc -zv your-rabbitmq-host 5672

   # Check RabbitMQ logs in application
   docker-compose logs ci-anomaly-detector | grep -i "rabbitmq\|amqp"

   # Verify queue configuration
   docker-compose logs ci-anomaly-detector | grep "Jenkins"
   ```

3. **AI API Issues**
   ```bash
   # Check AI service logs
   docker-compose logs ci-anomaly-detector | grep -i "ai\|gemini\|openai"

   # Test AI API connectivity
   docker-compose exec ci-anomaly-detector curl -I https://generativelanguage.googleapis.com

   # Check API key configuration (WARNING: This may expose sensitive information)
   docker-compose logs ci-anomaly-detector | grep "API_KEY"
   ```

4. **Application Startup Issues**
   ```bash
   # Check application startup logs
   docker-compose logs ci-anomaly-detector | head -50

   # Check for port conflicts
   netstat -tulpn | grep 8282

   # Check container resource usage
   docker stats $(docker-compose ps -q ci-anomaly-detector)
   ```

5. **Message Processing Issues**
   ```bash
   # Check message listener logs
   docker-compose logs ci-anomaly-detector | grep "LogMessageListener"

   # Check for message deserialization errors
   docker-compose logs ci-anomaly-detector | grep -i "deserialize\|json"

   # Monitor queue processing
   docker-compose logs ci-anomaly-detector | grep "processLogMessage"
   ```

### Debug Mode

Enable debug logging:

```bash
# Set debug environment variables
echo "LOG_LEVEL_APP=DEBUG" >> .env
echo "LOG_LEVEL_AI=DEBUG" >> .env

# Restart with debug logging
docker-compose down && docker-compose up -d

# View debug logs
docker-compose logs -f ci-anomaly-detector
```

### Performance Debugging

```bash
# Check memory usage
docker-compose exec ci-anomaly-detector free -h

# Check disk usage
docker-compose exec ci-anomaly-detector df -h

# Check Java process
docker-compose exec ci-anomaly-detector ps aux | grep java

# Check JVM metrics (NOT AVAILABLE without actuator dependency)
# curl http://localhost:8282/actuator/metrics/jvm.memory.used
```

## Security

### Production Security Checklist

- [ ] **CRITICAL**: Remove hardcoded API key from application.properties (currently contains: AIzaSyDJwezpLJRES6IXytJsDYGrobgW30sHqAQ)
- [ ] Change default database credentials
- [ ] Change default RabbitMQ credentials
- [ ] Use secure PostgreSQL connection (SSL)
- [ ] Rotate Google Gemini API keys regularly
- [ ] Enable firewall rules for port 8282
- [ ] Use secrets management system (HashiCorp Vault, AWS Secrets Manager)
- [ ] Enable audit logging
- [ ] Regular security updates for base images
- [ ] Implement API rate limiting
- [ ] Use non-root user in container (already configured)
- [ ] Add Spring Boot Actuator dependency for proper monitoring

### Network Security

```bash
# Create custom Docker network (if needed)
docker network create ci-anomaly-network

# Run with custom network
docker-compose --env-file .env.production up -d
```

### Environment Security

```bash
# Secure .env file permissions
chmod 600 .env

# Use Docker secrets for sensitive data (production)
echo "your_api_key" | docker secret create gemini_api_key -
echo "your_db_password" | docker secret create db_password -
```

## Backup & Recovery

### Database Backup

```bash
# Backup PostgreSQL (external service)
pg_dump -h your-postgres-host -U postgres ci_anomaly_detector > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore database
psql -h your-postgres-host -U postgres ci_anomaly_detector < backup_20241215_120000.sql
```

### Application Data Backup

```bash
# Backup application logs
docker-compose exec ci-anomaly-detector tar -czf /app/logs/backup_$(date +%Y%m%d).tar.gz /app/logs/

# Copy backup from container
docker cp $(docker-compose ps -q ci-anomaly-detector):/app/logs/backup_20241215.tar.gz ./backups/
```

### Configuration Backup

```bash
# Backup configuration files
cp .env.production .env.backup.$(date +%Y%m%d)
cp docker-compose.yml docker-compose.yml.backup.$(date +%Y%m%d)
```

## Performance Tuning

### Resource Limits

The Dockerfile already includes optimized JVM settings. You can customize them by adding to your `.env.production` file:

```bash
# JVM Memory Settings (Note: These match the Dockerfile defaults)
JAVA_OPTS=-XX:InitialRAMPercentage=50 -XX:MaxRAMPercentage=75 -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs -Djava.security.egd=file:/dev/./urandom

# Custom JVM options for high-volume processing (Override Dockerfile defaults)
JAVA_OPTS=-Xmx4g -Xms2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication
```

### Docker Resource Limits

Add resource limits to `docker-compose.yml`:

```yaml
services:
  ci-anomaly-detector:
    deploy:
      resources:
        limits:
          memory: 4G
          cpus: '2.0'
        reservations:
          memory: 2G
          cpus: '1.0'
```

### Application Performance Settings

Add to your `.env.production` file:

```bash
# Database Connection Pool
DB_HIKARI_MAXIMUM_POOL_SIZE=20
DB_HIKARI_MINIMUM_IDLE=5

# AI Memory Management
AI_MEMORY_MAX_MESSAGES=100

# Queue Processing
QUEUE_TTL=150000

# Logging Performance
LOG_LEVEL_ROOT=WARN
LOG_LEVEL_APP=INFO
```

### Monitoring Performance

```bash
# Monitor container resources
docker stats $(docker-compose ps -q ci-anomaly-detector)

# Check JVM memory usage (NOT AVAILABLE without actuator dependency)
# curl http://localhost:8282/actuator/metrics/jvm.memory.used

# Check database connection pool (NOT AVAILABLE without actuator dependency)
# curl http://localhost:8282/actuator/metrics/hikaricp.connections.active

# Monitor message processing rate (Available via application logs)
docker-compose logs ci-anomaly-detector | grep "processLogMessage" | tail -20
```

## Integration with Jenkins

### Jenkins Configuration

Your Jenkins instance should be configured to send logs to the RabbitMQ queue. The system expects:

1. **14 log types per build** in the following order:
   - build_log_data (initial)
   - secret_detection (build log)
   - dependency_data
   - secret_detection (Jenkinsfile)
   - code_changes
   - additional_info_agent
   - additional_info_controller (system health)
   - additional_info_controller (JVM info)
   - sast_scanning (Semgrep)
   - sast_scanning (Bearer)
   - sast_scanning (Trivy)
   - build_log_data (final)
   - secret_detection (final build log)
   - sast_scanning (Horusec, if SCM available)

2. **Message Format**: Each message should be JSON with:
   ```json
   {
     "type": "build_log_data",
     "job_name": "your-jenkins-job",
     "build_number": 123,
     "timestamp": "2024-01-15T10:30:00Z",
     "data": { /* log-specific data */ }
   }
   ```

3. **Queue Configuration**: Messages sent to queue `Jenkins` (configurable via `QUEUE_NAME`)

## Support

For issues and support:
- **Check logs**: `docker-compose logs ci-anomaly-detector`
- **Review health endpoints**: Not available (requires actuator dependency)
- **Monitor metrics**: Not available (requires actuator dependency)
- **Alternative monitoring**: Use container logs and Docker stats
- **Contact**: **Khasan Abdurakhmanov** <ya.hasan2001@yandex.ru>

## Additional Resources

- **Project Repository**: Contains source code and documentation
- **Spring Boot Actuator**: Built-in monitoring and management endpoints (requires additional dependency)
- **Google Gemini AI**: AI model documentation and API reference
- **Docker Documentation**: Container deployment and management guides

## Configuration Compliance Notes

This deployment guide has been updated to reflect the **actual implementation** as of the current codebase:

### Key Implementation Details:
- **AI Model**: Docker uses `gemini-2.5-flash-preview-05-20`, application defaults to `gemini-2.5-flash-lite-preview-06-17`
- **AI Temperature**: Docker uses `0`, application defaults to `0.3`
- **RabbitMQ Host**: Docker uses `rabbitmq`, application defaults to `130.193.49.138`
- **Actuator Endpoints**: **NOT AVAILABLE** - requires adding `spring-boot-starter-actuator` dependency
- **Security Warning**: Hardcoded API key present in `application.properties` - **MUST BE REMOVED** for production

### To Enable Full Monitoring:
Add to `build.gradle`:
```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

### Security Fix Required:
Remove hardcoded API key from `src/main/resources/application.properties`:
```properties
# Change this line:
spring.ai.openai.api-key=${AI_API_KEY:AIzaSyDJwezpLJRES6IXytJsDYGrobgW30sHqAQ}
# To this:
spring.ai.openai.api-key=${AI_API_KEY}
```
