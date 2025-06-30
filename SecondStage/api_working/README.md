# CI Anomaly Detector API

A comprehensive Jenkins CI/CD monitoring and AI-powered anomaly detection system built with Spring Boot. This system provides real-time build monitoring, AI analysis of build logs, security anomaly detection, and dashboard analytics for Jenkins CI/CD pipelines.

## 🚀 Features

- **Real-time Jenkins Build Monitoring**: Continuous monitoring of Jenkins jobs and builds
- **AI-Powered Anomaly Detection**: Machine learning analysis of build logs for anomaly detection
- **Security Vulnerability Assessment**: Detection of security issues in CI/CD pipelines
- **Dashboard Analytics**: Comprehensive metrics and visualizations for build trends
- **Build Log Analysis**: Detailed analysis and insights from build logs
- **Risk Score Calculations**: AI-powered risk assessment and trending
- **Job Management**: Discovery and management of Jenkins jobs
- **REST API**: Complete REST API for dashboard integration

## 🏗️ Architecture

### System Components
- **Spring Boot Application**: Main REST API microservice
- **Jenkins Integration**: REST API integration using jenkins-rest library
- **PostgreSQL Database**: Primary data storage with JSONB support for AI analysis
- **AI Analysis Pipeline**: External AI service integration for log analysis
- **Materialized Views**: Performance-optimized pre-computed data

### Database Schema
The system uses PostgreSQL with a primary `chat_messages` table that stores:
- Build logs as USER messages
- AI analysis results as ASSISTANT messages
- JSONB content for structured data storage
- Materialized views for performance optimization

## 📋 Prerequisites

### Required Software
- **Java 21** or higher
- **PostgreSQL 12+** with JSONB support
- **Jenkins Server** (accessible via REST API)
- **Docker & Docker Compose** (for containerized deployment)
- **Gradle 8+** (for building from source)

### System Requirements
- **Memory**: Minimum 2GB RAM (4GB recommended for production)
- **Storage**: At least 10GB free space for logs and database
- **Network**: Access to Jenkins server and PostgreSQL database

## 🛠️ Installation & Setup

### Option 1: Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd api_working
   ```

2. **Configure environment variables**
   
   Copy and modify the production environment file:
   ```bash
   cp .env.production .env.local
   ```
   
   Edit `.env.production` with your configuration:
   ```bash
   # Jenkins Configuration
   JENKINS_URL=http://your-jenkins-server:8080
   JENKINS_USERNAME=your-jenkins-username
   JENKINS_PASSWORD=your-jenkins-password
   
   # Database Configuration
   DATABASE_URL=jdbc:postgresql://your-db-host:5432/your-database
   DATABASE_USERNAME=your-db-username
   DATABASE_PASSWORD=your-db-password
   
   # Server Configuration
   SERVER_PORT=8282
   ```

3. **Start the application**
   ```bash
   # Using the local environment file
   docker-compose --env-file .env.production up -d
   
   # Or using the default production file
   docker-compose up -d
   ```

4. **Verify deployment**
   ```bash
   # Check container status
   docker-compose ps
   
   # View logs
   docker-compose logs -f ci-anomaly-detector-api
   
   # Health check
   curl http://localhost:8282/api/dashboard
   ```

### Option 2: Manual Installation

1. **Setup PostgreSQL Database**
   ```sql
   CREATE DATABASE postgres;
   CREATE USER postgres WITH PASSWORD 'postgres';
   GRANT ALL PRIVILEGES ON DATABASE postgres TO postgres;
   ```

2. **Configure Application Properties**
   
   Create `src/main/resources/application-local.properties`:
   ```properties
   # Server Configuration
   server.port=8282
   
   # Jenkins Configuration
   jenkins.url=http://your-jenkins-server:8080
   jenkins.username=your-username
   jenkins.password=your-password
   
   # Database Configuration
   spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   
   # JPA Configuration
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=false
   ```

3. **Build and Run**
   ```bash
   # Build the application
   ./gradlew clean build
   
   # Run with local profile
   ./gradlew bootRun --args='--spring.profiles.active=local'
   
   # Or run the JAR directly
   java -jar build/libs/diploma-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
   ```

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SERVER_PORT` | Application server port | 8282 | No |
| `JENKINS_URL` | Jenkins server URL | http://130.193.49.138:8080 | Yes |
| `JENKINS_USERNAME` | Jenkins username | admin | Yes |
| `JENKINS_PASSWORD` | Jenkins password/token | admin123 | Yes |
| `DATABASE_URL` | PostgreSQL connection URL | jdbc:postgresql://127.0.0.1:5432/postgres | Yes |
| `DATABASE_USERNAME` | Database username | postgres | Yes |
| `DATABASE_PASSWORD` | Database password | postgres | Yes |
| `LOG_LEVEL_APP` | Application log level | DEBUG | No |
| `CORS_ALLOWED_ORIGINS` | CORS allowed origins | * | No |

### Jenkins Configuration

The application requires Jenkins REST API access with the following permissions:
- Read access to jobs and builds
- Ability to trigger new builds
- Access to build logs and console output

**Jenkins API Token Setup:**
1. Go to Jenkins → Manage Jenkins → Manage Users
2. Click on your username → Configure
3. Generate API Token
4. Use the token as `JENKINS_PASSWORD`

### Database Configuration

**PostgreSQL Setup:**
```sql
-- Create database and user
CREATE DATABASE ci_anomaly_detector;
CREATE USER ci_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE ci_anomaly_detector TO ci_user;

-- Enable JSONB support (usually enabled by default in PostgreSQL 9.4+)
-- No additional setup required for JSONB
```

## 🚀 Usage

### API Endpoints

The application provides a comprehensive REST API at `http://localhost:8282/api/dashboard`:

#### Core Endpoints
- `GET /recentJobBuilds` - Get recent builds for all jobs
- `GET /recentJobBuilds/{jobName}` - Get recent builds for specific job
- `GET /activeBuilds` - Get total active build count
- `GET /totalJobs/{timeBoundary}` - Get job count by time period
- `GET /securityAnomalies` - Get security anomaly count

#### Build Details
- `GET /builds/{jobName}/{buildId}` - Get comprehensive build summary
- `GET /builds/{jobName}/{buildId}/risk-score` - Get AI risk assessment
- `GET /builds/{jobName}/{buildId}/logs` - Get paginated build logs
- `GET /builds/{jobName}/{buildId}/ai-insights` - Get AI analysis insights

#### Analytics & Charts
- `GET /anomaly-trend/{jobFilter}` - Get anomaly trend data for charts
- `GET /severity-distribution/{jobFilter}` - Get severity distribution data
- `GET /job-explorer` - Get jobs filtered by status

#### Job Management
- `POST /builds/{jobName}/{buildId}/rerun` - Trigger new build
- `GET /jobs` - Get all Jenkins jobs

### Example API Calls

```bash
# Get recent builds
curl http://localhost:8282/api/dashboard/recentJobBuilds

# Get build summary
curl http://localhost:8282/api/dashboard/builds/my-web-app/123

# Get risk score
curl http://localhost:8282/api/dashboard/builds/my-web-app/123/risk-score

# Get anomaly trends
curl http://localhost:8282/api/dashboard/anomaly-trend/all?buildCount=10

# Trigger new build
curl -X POST http://localhost:8282/api/dashboard/builds/my-web-app/123/rerun
```

## 📊 Monitoring & Health Checks

### Health Check Endpoints
```bash
# Application health (if Spring Actuator is enabled)
curl http://localhost:8282/actuator/health

# Custom dashboard health check
curl http://localhost:8282/api/dashboard
```

### Docker Health Checks
The Docker container includes comprehensive health checks:
```bash
# Manual health check
docker exec ci-anomaly-detector-api /app/docker-healthcheck.sh

# View health check logs
docker inspect ci-anomaly-detector-api | grep -A 10 "Health"
```

### Logging
Application logs are available at:
- **Container**: `/app/logs/ci-anomaly-detector-api.log`
- **Host**: Docker volume `ci-anomaly-detector-api-logs`

```bash
# View real-time logs
docker-compose logs -f ci-anomaly-detector

# View specific log files
docker exec ci-anomaly-detector-api tail -f /app/logs/ci-anomaly-detector-api.log
```

## 🔧 Development

### Building from Source
```bash
# Clean and build
./gradlew clean build

# Run tests
./gradlew test

# Generate documentation
./gradlew javadoc

# Build Docker image
docker build -t ci-anomaly-detector:latest .
```

### Development Environment
```bash
# Run in development mode with hot reload
./gradlew bootRun --args='--spring.profiles.active=dev'

# Run with debug enabled
./gradlew bootRun --args='--debug --spring.profiles.active=dev'
```

## 📚 API Documentation

Comprehensive API documentation is available in Swagger format:
- **File**: `src/main/resources/swagger/ci-anomaly-detector-api.yaml`
- **Generated Docs**: `docs/` directory (JavaDoc)

### Key API Features
- **Pagination**: Most list endpoints support pagination
- **Filtering**: Job-based and time-based filtering
- **Real-time Data**: Live build status and metrics
- **AI Integration**: AI-powered analysis and insights
- **Chart Data**: Ready-to-use data for frontend visualizations

## 🔒 Security Considerations

- **Jenkins Authentication**: Secure API token authentication
- **Database Security**: Encrypted connections and secure credentials
- **Environment Variables**: Sensitive data via environment variables
- **CORS Configuration**: Configurable CORS origins for production
- **Input Validation**: Comprehensive validation and error handling

## 🐛 Troubleshooting

### Common Issues

1. **Jenkins Connection Failed**
   ```bash
   # Check Jenkins URL and credentials
   curl -u username:password http://jenkins-url/api/json
   ```

2. **Database Connection Issues**
   ```bash
   # Test PostgreSQL connection
   psql -h hostname -U username -d database_name
   ```

3. **Application Won't Start**
   ```bash
   # Check logs for detailed error messages
   docker-compose logs ci-anomaly-detector
   ```

4. **Health Check Failures**
   ```bash
   # Run manual health check
   ./docker-healthcheck.sh
   ```

### Performance Optimization

- **Database Indexing**: Ensure proper indexes on JSONB columns
- **Connection Pooling**: Adjust `DB_POOL_MAX_SIZE` based on load
- **Memory Settings**: Tune `JAVA_OPTS` for your environment
- **Caching**: Jenkins service includes 15-second TTL cache

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📞 Support

For support and questions:
- **Author**: Khasan Abdurakhmanov
- **Email**: ya.hasan2001@yandex.ru
- **Documentation**: See `docs/` directory for detailed JavaDoc

---

**Note**: This is a diploma project for CI/CD monitoring and anomaly detection. Ensure proper security configurations before deploying to production environments.
