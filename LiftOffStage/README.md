# External Services Platform - LiftOffStage

## 📋 Overview

LiftOffStage is the **external services platform** that provides essential infrastructure components for the RabbitMQ Integration Platform. This platform deploys three critical services that support the main application's secret scanning, CI/CD automation, and chat memory functionality.

### Core Purpose
- **Service Isolation**: Separates external dependencies from the main application
- **Scalability**: Allows independent scaling and deployment of infrastructure services
- **Multi-Stage Support**: Provides specialized services for both FirstStage (secret scanning) and SecondStage (chat memory) applications
- **Production Ready**: Enterprise-grade services with persistent storage and automated configuration

## 🏗️ Service Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                             LiftOffStage Platform                       │
│                                                                         │
│  ┌──────────────────────┐  ┌───────────────────┐  ┌───────────────────┐ │
│  │       Jenkins        │  │    RabbitMQ       │  │    PostgreSQL CI  │ │
│  │       (CI/CD)        │  │   (Messaging)     │  │    (Chat Memory)  │ │
│  │                      │  │                   │  │                   │ │
│  │ • Build Automation   │  │ • Message Queue   │  │ • Chat Storage    │ │
│  │ • Pipeline Management│  │ • Task Processing │  │ • Anomaly Track   │ │
│  │ • Auto Setup         │  │ • Management UI   │  │ • Job Monitoring  │ │
│  │                      │  │                   │  │ • Scheduled Jobs  │ │
│  │    Port: 8080        │  │   Port: 5672      │  │   Port: 5432      │ │
│  │    Port: 50000       │  │   Port: 15672     │  │                   │ │
│  └──────────────────────┘  └───────────────────┘  └───────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
```

## 📁 Directory Structure

```
LiftOffStage/
├── docker-compose.yml           # Main orchestration file for all services
├── .env                         # Environment configuration
├── README.md                    # This documentation
├── jenkins/                     # Jenkins CI/CD configuration
│   └── init.groovy.d/
│       └── init.groovy         # Automated Jenkins setup script
└── postgres_ci/                # Custom PostgreSQL for chat memory
    ├── Dockerfile              # Custom PostgreSQL with pg_cron
    └── init.sql                # Database schema and functions
```

## 🎯 Service Details

### 1. Jenkins CI/CD Server
**Purpose**: Automated build and deployment pipeline management

**Key Features**:
- **JDK 21 Support**: Latest Java development kit for modern applications
- **Automated Setup**: Pre-configured admin user (admin/admin123)
- **Agent Support**: Ready for distributed builds
- **Persistent Storage**: Jenkins home directory preserved across restarts
- **Custom Initialization**: Groovy scripts for automated configuration

**Access**:
- **Web UI**: http://localhost:8080
- **Credentials**: admin/admin123
- **Agent Port**: 50000

### 2. RabbitMQ Message Broker
**Purpose**: Asynchronous message processing and task queuing

**Key Features**:
- **Management UI**: Web-based monitoring and administration
- **Alpine Linux**: Lightweight, secure base image
- **Default Configuration**: Ready-to-use with guest/guest credentials
- **AMQP Protocol**: Standard message queuing protocol support
- **High Performance**: Optimized for message throughput

**Access**:
- **Management UI**: http://localhost:15672
- **Credentials**: guest/guest
- **AMQP Port**: 5672

### 3. PostgreSQL CI (Chat Memory)
**Purpose**: Specialized database for SecondStage application chat memory and analytics

**Key Features**:
- **PostgreSQL 17.5**: Latest stable version with advanced features
- **pg_cron Extension**: Automated scheduled task execution
- **Chat Message Storage**: Optimized schema for conversation data
- **Anomaly Detection**: Automated security anomaly tracking
- **Job Monitoring**: Build status and active job tracking
- **Performance Optimized**: Multiple indexes for efficient querying

**Database Schema**:
- `chat_messages`: Core conversation storage
- `job_counts`: Precomputed job statistics
- `active_build_counts`: Real-time build monitoring
- `security_anomaly_counts`: Security issue tracking
- `recent_job_builds`: Latest build information
- `build_anomaly_summary`: Aggregated anomaly data

**Access**:
- **Database**: localhost:5432
- **Credentials**: postgres/postgres
- **Database Name**: postgres

## 🚀 Deployment Instructions

### Prerequisites
- Docker and Docker Compose installed
- Minimum 4GB RAM for all services
- Ports 5432, 5672, 8080, 15672 available
- Network connectivity for external access

### Quick Start
```bash
# Navigate to LiftOffStage directory
cd LiftOffStage

# Start all services
docker-compose up -d

# Verify all services are running
docker-compose ps

# Check service logs
docker-compose logs -f
```

### Individual Service Deployment
```bash
# Start only Jenkins
docker-compose up -d jenkins

# Start only RabbitMQ
docker-compose up -d rabbitmq

# Start only PostgreSQL CI
docker-compose up -d postgres_ci
```

### Service Initialization Wait Times
- **RabbitMQ**: ~30 seconds (ready when management UI accessible)
- **Jenkins**: ~60-90 seconds (includes plugin installation and setup)
- **PostgreSQL CI**: ~45 seconds (includes extension setup and schema creation)

## ⚙️ Configuration

### Environment Variables (.env)
```bash
# Jenkins Configuration
JENKINS_WEB_PORT=8080              # Web UI port
JENKINS_AGENT_PORT=50000           # Agent communication port
JENKINS_JAVA_OPTS=-Djenkins.install.runSetupWizard=false

# RabbitMQ Configuration
RABBITMQ_AMQP_PORT=5672           # AMQP protocol port
RABBITMQ_MANAGEMENT_PORT=15672     # Management UI port
RABBITMQ_DEFAULT_USER=guest        # Default username
RABBITMQ_DEFAULT_PASS=guest        # Default password

# PostgreSQL CI Configuration
POSTGRES_CI_PORT=5432             # Database port
POSTGRES_CI_DB=postgres           # Database name
POSTGRES_CI_USER=postgres         # Database user
POSTGRES_CI_PASSWORD=postgres     # Database password
```

### Customization Options

**Jenkins Customization**:
- Modify `jenkins/init.groovy.d/init.groovy` for custom setup
- Add additional Groovy scripts for plugin installation
- Mount custom Jenkins configuration files

**PostgreSQL CI Customization**:
- Modify `postgres_ci/init.sql` for custom schema
- Add additional initialization scripts
- Configure pg_cron schedules

**RabbitMQ Customization**:
- Add custom RabbitMQ configuration files
- Configure additional users and permissions
- Set up custom exchanges and queues

## 🔧 Service Management

### Starting Services
```bash
# Start all services
docker-compose up -d

# Start with logs visible
docker-compose up

# Start specific service
docker-compose up -d jenkins
```

### Stopping Services
```bash
# Stop all services
docker-compose down

# Stop specific service
docker-compose stop jenkins

# Stop and remove volumes (WARNING: Data loss)
docker-compose down -v
```

### Monitoring and Logs
```bash
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f jenkins
docker-compose logs -f rabbitmq
docker-compose logs -f postgres_ci

# Check service status
docker-compose ps

# View resource usage
docker stats
```

## 🔍 Service Health Checks

### Automated Health Verification
```bash
# Check all services
docker-compose ps

# Test service endpoints
curl -f http://localhost:8080 || echo "Jenkins not ready"
curl -f http://localhost:15672 || echo "RabbitMQ not ready"
pg_isready -h localhost -p 5432 || echo "PostgreSQL not ready"
```

### Manual Service Testing
```bash
# Jenkins - Check if web UI is accessible
curl -I http://localhost:8080/login

# RabbitMQ - Check management interface
curl -I http://localhost:15672

# PostgreSQL CI - Test database connection
docker-compose exec postgres_ci psql -U postgres -d postgres -c "SELECT 1;"

# PostgreSQL CI - Verify pg_cron extension
docker-compose exec postgres_ci psql -U postgres -d postgres -c "SELECT * FROM pg_extension WHERE extname='pg_cron';"
```

## 🛠️ Troubleshooting

### Common Issues

**1. Port Conflicts**
```bash
# Check what's using the ports
netstat -tulpn | grep :8080
netstat -tulpn | grep :5672
netstat -tulpn | grep :5432

# Solution: Update .env file with different ports
```

**2. Jenkins Setup Issues**
```bash
# Get initial admin password (if setup wizard appears)
docker-compose exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# Check Jenkins logs for errors
docker-compose logs jenkins | grep -i error
```

**3. PostgreSQL CI Connection Issues**
```bash
# Check if PostgreSQL is accepting connections
docker-compose exec postgres_ci pg_isready -U postgres

# Verify database exists
docker-compose exec postgres_ci psql -U postgres -l

# Check pg_cron extension
docker-compose exec postgres_ci psql -U postgres -d postgres -c "\dx"
```

**4. RabbitMQ Management Issues**
```bash
# Check RabbitMQ status
docker-compose exec rabbitmq rabbitmqctl status

# List users
docker-compose exec rabbitmq rabbitmqctl list_users

# Check if management plugin is enabled
docker-compose exec rabbitmq rabbitmq-plugins list
```

### Service Recovery
```bash
# Restart specific service
docker-compose restart jenkins

# Rebuild service (if configuration changed)
docker-compose build postgres_ci
docker-compose up -d postgres_ci

# Reset service data (WARNING: Data loss)
docker-compose down
docker volume rm liftoffstage_jenkins-home
docker volume rm liftoffstage_postgres_ci_data
docker-compose up -d
```

## 📊 Service Information

| Service | URL | Credentials | Purpose |
|---------|-----|-------------|---------|
| **Jenkins** | http://localhost:8080 | admin/admin123 | CI/CD automation |
| **RabbitMQ Management** | http://localhost:15672 | guest/guest | Message queue monitoring |
| **PostgreSQL CI** | localhost:5432 | postgres/postgres | Chat memory database |

## 🔄 Maintenance Operations

### Data Backup
```bash
# Backup Jenkins configuration
docker-compose exec jenkins tar -czf /tmp/jenkins-backup.tar.gz /var/jenkins_home
docker cp $(docker-compose ps -q jenkins):/tmp/jenkins-backup.tar.gz ./jenkins-backup.tar.gz

# Backup PostgreSQL CI database
docker-compose exec postgres_ci pg_dump -U postgres postgres > postgres_ci_backup.sql

# Backup RabbitMQ configuration
docker-compose exec rabbitmq rabbitmqctl export_definitions /tmp/rabbitmq-definitions.json
docker cp $(docker-compose ps -q rabbitmq):/tmp/rabbitmq-definitions.json ./rabbitmq-definitions.json
```

### Updates and Upgrades
```bash
# Update service images
docker-compose pull

# Rebuild custom PostgreSQL CI
docker-compose build postgres_ci

# Restart with new images
docker-compose down
docker-compose up -d
```

### Performance Monitoring
```bash
# Monitor resource usage
docker stats $(docker-compose ps -q)

# Check disk usage
docker system df

# Monitor PostgreSQL CI performance
docker-compose exec postgres_ci psql -U postgres -d postgres -c "SELECT * FROM pg_stat_activity;"
```

## 🎯 Integration with Main Application

This LiftOffStage platform is designed to work with the main RabbitMQ Integration Platform:

1. **Main Application** connects to RabbitMQ for message processing
2. **Main Application** triggers Jenkins builds for CI/CD automation  
3. **SecondStage Applications** use PostgreSQL CI for chat memory storage
4. **All services** can be deployed on separate machines for scalability

For main application configuration, update the main application's `.env` file with the appropriate service endpoints.

## 📝 Next Steps

1. **Deploy Services**: Follow the deployment instructions above
2. **Verify Connectivity**: Test all service endpoints
3. **Configure Main App**: Update main application to connect to these services
4. **Monitor Performance**: Set up monitoring and alerting
5. **Backup Strategy**: Implement regular backup procedures

This platform provides a robust foundation for enterprise-grade secret scanning and chat memory functionality with proper service isolation and scalability.
