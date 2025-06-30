# CI/CD Anomaly Detection & AI Analysis System

A comprehensive **DevSecOps monitoring solution** that provides real-time anomaly detection for Jenkins CI/CD pipelines using advanced AI analysis. This system monitors build logs, security scans, dependency data, and system metrics to detect security vulnerabilities, performance issues, and operational anomalies.

## 🏗️ System Architecture

```
┌─────────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐ ┌─────────────────────┐
│   Jenkins (External)│    │  PostgreSQL (Ext.)  │    │  Google Gemini AI   │ | RabbitMQ (External) |
│   Port: 8080        │    │  Port: 5432         │    │  (External API)     │ | Port : 5672         |
└─────────────────────┘    └─────────────────────┘    └─────────────────────┘ └─────────────────────┘
           │                           │                           │                    |
           │                           │                           │                    |
           ▼                           ▼                           ▼                    ▼
┌────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                  CI  Anomaly   Detector App                                        |
│                                  Port: 8282  (Docker Container)                                    |
└────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

## 🚀 Core Features

### 🔍 **AI-Powered Analysis**
- **Google Gemini 2.5 Flash Lite Preview** integration for intelligent log analysis
- **Context-aware conversation memory** for Jenkins jobs with PostgreSQL persistence
- **Historical comparison** with previous builds using chat memory
- **Risk scoring** (0-100) based on anomaly severity with structured JSON responses

### 🛡️ **Comprehensive Monitoring**
- **14 log types per build**: Build logs, security scans, dependency data, code changes, system metrics
- **Real-time processing** via RabbitMQ message queue
- **Multi-tool SAST integration**: Semgrep, Bearer, Trivy, Horusec
- **Secret detection** across build logs and Jenkinsfiles

### 📊 **Anomaly Detection**
- Security regressions and vulnerabilities
- Secrets leakage and credential exposure
- Build instability and agent crashes
- Dependency issues and version conflicts
- Performance degradation patterns

### 💾 **Data Management**
- **PostgreSQL** with JSONB support for flexible log storage
- **Conversation-based memory** for contextual AI analysis
- **Build correlation** across different log types
- **Temporal filtering** with timestamp validation

## 🛠️ Technology Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Framework** | Spring Boot 3.3.0 | Application framework |
| **Language** | Java 21 | Core development |
| **AI Integration** | Spring AI + Google Gemini 2.5 Flash Lite | Anomaly detection |
| **Database** | PostgreSQL 12+ with JSONB | Data persistence & chat memory |
| **Message Queue** | RabbitMQ (External) | Real-time log processing |
| **Containerization** | Docker (Single Container) | Deployment |
| **Build Tool** | Gradle 8.x | Build automation |

## 📋 Prerequisites

### Required External Services

1. **PostgreSQL Database**
   - Version: 12+ with JSONB support
   - Database: `postgres` (default) or `ci_anomaly_detector`
   - **Default connection**: `127.0.0.1:5432/postgres`
   - User with full permissions

2. **RabbitMQ Message Broker**
   - Version: 3.8+
   - Management plugin enabled (optional)
   - Queue: `Jenkins` (configurable)
   - **Default connection**: `130.193.49.138:5672`
   - User with appropriate permissions

3. **Jenkins Server**
   - Version: 2.400+
   - Must send logs to RabbitMQ queue
   - Produces 14 log types per build for comprehensive analysis

4. **Google Gemini AI API**
   - Valid API key required
   - Model: `gemini-2.5-flash-lite-preview-06-17`
   - API endpoint: `https://generativelanguage.googleapis.com`

### Required Software

- **Docker Engine** 20.10+
- **4GB+ available RAM**
- **10GB+ available disk space**
- **Network access to external services**

## ⚙️ Configuration

### Environment Variables

| Category | Variable | Description | Default | Required |
|----------|----------|-------------|---------|----------|
| **Server** | `SERVER_PORT` | Application port | `8282` | No |
| **AI** | `AI_API_KEY` | Google Gemini API key | - | **Yes** |
| **AI** | `AI_MODEL` | AI model name | `gemini-2.5-flash-lite-preview-06-17` | No |
| **AI** | `AI_TEMPERATURE` | AI response randomness | `0.3` | No |
| **AI** | `AI_RESPONSE_FORMAT` | Response format | `JSON_OBJECT` | No |
| **AI** | `AI_BASE_URL` | AI API base URL | `https://generativelanguage.googleapis.com` | No |
| **Database** | `DB_URL` | PostgreSQL connection URL | `jdbc:postgresql://127.0.0.1:5432/postgres` | **Yes** |
| **Database** | `DB_USERNAME` | Database username | `postgres` | **Yes** |
| **Database** | `DB_PASSWORD` | Database password | `postgres` | **Yes** |
| **Database** | `DB_DDL_AUTO` | Hibernate DDL mode | `update` | No |
| **Database** | `DB_SHOW_SQL` | Show SQL queries | `false` | No |
| **RabbitMQ** | `RABBITMQ_HOST` | RabbitMQ hostname | `130.193.49.138` | **Yes** |
| **RabbitMQ** | `RABBITMQ_USERNAME` | RabbitMQ username | `guest` | **Yes** |
| **RabbitMQ** | `RABBITMQ_PASSWORD` | RabbitMQ password | `guest` | **Yes** |
| **RabbitMQ** | `RABBITMQ_PORT` | RabbitMQ port | `5672` | No |
| **Queue** | `QUEUE_NAME` | Main queue name | `Jenkins` | No |
| **Queue** | `QUEUE_DLQ_NAME` | Dead letter queue | `Jenkins.dlq` | No |
| **Queue** | `QUEUE_TTL` | Message TTL (ms) | `150000` | No |
| **Logging** | `LOG_LEVEL_APP` | Application log level | `DEBUG` | No |
| **Logging** | `LOG_LEVEL_ROOT` | Root log level | `INFO` | No |
| **Logging** | `LOG_LEVEL_AI` | AI log level | `DEBUG` | No |
| **Memory** | `AI_MEMORY_MAX_MESSAGES` | Max messages per conversation | `100` | No |
| **Memory** | `AI_SYSTEM_PROMPT_TEMPLATE` | System prompt template path | `classpath:templates/system.st` | No |

### Sample Environment Configuration

Create a `.env` file in the project root:

```bash
# Required: Google Gemini AI API Key
AI_API_KEY=your_google_gemini_api_key_here

# Required: PostgreSQL Connection
DB_URL=jdbc:postgresql://your-postgres-host:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=your_secure_database_password_here

# Required: RabbitMQ Connection (External)
RABBITMQ_HOST=your-rabbitmq-host
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_PORT=5672

# Optional: Server Configuration
SERVER_PORT=8282
LOG_LEVEL_APP=DEBUG
LOG_LEVEL_ROOT=INFO

# Optional: AI Configuration
AI_MODEL=gemini-2.5-flash-lite-preview-06-17
AI_TEMPERATURE=0.3
AI_RESPONSE_FORMAT=JSON_OBJECT

# Optional: Memory Configuration
AI_MEMORY_MAX_MESSAGES=100
QUEUE_TTL=150000
```

## 🐳 Docker Deployment

### Quick Start

1. **Clone and prepare**:
```bash
git clone <repository-url>
cd diploma_working
```

2. **Configure environment**:
```bash
# Create environment file
touch .env
# Edit .env with your configuration (see sample above)
```

3. **Deploy with Docker**:
```bash
# Build and start the application
docker-compose --env-file .env up -d

# Check service status
docker-compose ps

# View application logs
docker-compose logs -f ci-anomaly-detector

# Check application health
curl http://localhost:8282/actuator/health
```

### Service Management

```bash
# Start the application
docker-compose up -d

# Start with build (rebuild if needed)
docker-compose up -d --build

# Stop the application
docker-compose down

# Stop and remove volumes
docker-compose down -v

# View logs
docker-compose logs -f ci-anomaly-detector

# Restart with rebuild
docker-compose down && docker-compose up -d --build
```

## 🔧 Local Development

### Prerequisites
- Java 21
- PostgreSQL 12+
- RabbitMQ
- Google Gemini API key

### Setup Steps

1. **Clone repository**:
```bash
git clone <repository-url>
cd diploma_working
```

2. **Configure database**:
```sql
CREATE DATABASE ci_anomaly_detector;
CREATE USER postgres WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE ci_anomaly_detector TO postgres;
-- Or use existing postgres database with default user
```

3. **Setup RabbitMQ**:
```bash
# Install and start RabbitMQ (example for Ubuntu)
sudo apt-get install rabbitmq-server
sudo systemctl start rabbitmq-server

# Use default guest user or create custom user
sudo rabbitmqctl add_user your_user your_password
sudo rabbitmqctl set_permissions your_user ".*" ".*" ".*"
```

4. **Set environment variables**:
```bash
export AI_API_KEY=your_google_gemini_api_key
export DB_URL=jdbc:postgresql://localhost:5432/postgres
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export RABBITMQ_HOST=localhost
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=guest
```

5. **Build and run**:
```bash
# Build the application
./gradlew clean build

# Run the application
./gradlew bootRun

# Or run the JAR
java -jar build/libs/diploma-0.0.1-SNAPSHOT.jar
```

## 📊 Monitoring & Health Checks

### Health Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /actuator/health` | Overall application health |
| `GET /actuator/health/db` | Database connectivity |
| `GET /actuator/metrics` | Application metrics |
| `GET /actuator/info` | Application information |
| `GET /actuator/env` | Environment variables |

### Monitoring Commands

```bash
# Application health
curl http://localhost:8282/actuator/health

# Database connectivity
curl http://localhost:8282/actuator/health/db

# RabbitMQ management (external)
open http://your-rabbitmq-host:15672
```

## 📝 Log Processing Pipeline

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

### AI Analysis Trigger

The system triggers AI analysis when:
- **2 BuildLogData logs** are received (initial + final)
- **Final SecretDetection** with source="build_log" is processed
- All **13+ logs** are available for comprehensive analysis

**AI Response Format**: The system uses structured JSON responses with:
- Risk scoring (0-100) based on anomaly severity
- Detailed anomaly descriptions with locations and recommendations
- Security trends analysis across historical builds
- Actionable mitigation strategies

## 🔒 Security Considerations

### Production Security Checklist

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

### Network Security

```bash
# Create custom Docker network (if needed)
docker network create ci-anomaly-network

# Secure .env file permissions
chmod 600 .env
```

## 🚨 Troubleshooting

### Common Issues

1. **Database Connection Failed**
```bash
# Check database connectivity from container
docker-compose exec ci-anomaly-detector curl -f http://localhost:8282/actuator/health/db

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

# Check API key configuration
docker-compose logs ci-anomaly-detector | grep "API_KEY"
```

4. **Message Processing Issues**
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

## 📚 API Documentation

The application provides Spring Boot Actuator endpoints for monitoring:

- **Health Check**: `GET /actuator/health`
- **Database Health**: `GET /actuator/health/db`
- **Metrics**: `GET /actuator/metrics`
- **Info**: `GET /actuator/info`
- **Environment**: `GET /actuator/env`

### Available Metrics

Key metrics available via `/actuator/metrics`:
- `jvm.memory.used` - JVM memory usage
- `hikaricp.connections.active` - Database connection pool
- `system.cpu.usage` - CPU utilization
- `process.uptime` - Application uptime

## 🔄 Backup & Recovery

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
cp .env .env.backup.$(date +%Y%m%d)
cp docker-compose.yml docker-compose.yml.backup.$(date +%Y%m%d)
```

## 🎯 Performance Tuning

### Resource Limits

The Dockerfile already includes optimized JVM settings. You can customize them by adding to your `.env` file:

```bash
# JVM Memory Settings (already optimized in Dockerfile)
JAVA_OPTS=-XX:InitialRAMPercentage=50 -XX:MaxRAMPercentage=75 -XX:+UseG1GC -XX:MaxGCPauseMillis=200

# Custom JVM options for high-volume processing
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

### Performance Monitoring

```bash
# Monitor container resources
docker stats $(docker-compose ps -q ci-anomaly-detector)

# Check JVM memory usage
curl http://localhost:8282/actuator/metrics/jvm.memory.used

# Check database connection pool
curl http://localhost:8282/actuator/metrics/hikaricp.connections.active

# Monitor message processing rate
docker-compose logs ci-anomaly-detector | grep "processLogMessage" | tail -20
```

## 👥 Support

For issues and support:
- **Check logs**: `docker-compose logs ci-anomaly-detector`
- **Review health endpoints**: `curl http://localhost:8282/actuator/health`
- **Monitor metrics**: `curl http://localhost:8282/actuator/metrics`
- **Contact**: **Khasan Abdurakhmanov** <ya.hasan2001@yandex.ru>

## 🔗 Integration with Jenkins

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

## 📄 License

This project is part of a diploma thesis for CI/CD anomaly detection and AI analysis.

---

**Author**: Khasan Abdurakhmanov  
**Version**: 1.0.0  
**Last Updated**: 2025
