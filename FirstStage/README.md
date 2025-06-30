# RabbitMQ Integration Platform - Main Application

## 📋 Project Overview

This is the **main application component** of a Java-based microservices integration platform designed for **secret scanning and CI/CD automation**. This application serves as the core processing engine that connects to external RabbitMQ and Jenkins services.

### Core Functionality
- **Secret Detection**: Comprehensive scanning using 6,400+ regex patterns for identifying sensitive data (API keys, tokens, credentials)
- **External Service Integration**: Connects to external RabbitMQ and Jenkins instances for message processing and CI/CD automation
- **Container Orchestration**: Remote Docker host management with TLS-secured API communication
- **Data Persistence**: PostgreSQL storage for scan results, configurations, and audit trails
- **Message Processing**: Processes messages from external RabbitMQ queues for scalable operations

### Application Components
- **Main Application**: Java 21 Spring Boot application with secret detection engine
- **Database**: PostgreSQL for persistent data storage
- **Security Layer**: TLS certificate management for secure Docker API access
- **External Integrations**: Connects to separately deployed RabbitMQ and Jenkins services

## 🏗️ Application Architecture

```
┌─────────────────────────────────────┐
│           Main Application          │
│                                     │
│  ┌───────────────────────────────┐  │
│  │     Java Spring Boot App      │  │
│  │    (Secret Detection)         │  │
│  │                               │  │
│  │  • Secret Pattern Matching   │  │
│  │  • External Service Client   │  │
│  │  • Docker API Integration    │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │        PostgreSQL             │  │
│  │      (Port: 5432)             │  │
│  │                               │  │
│  │  • Scan Results Storage      │  │
│  │  • Configuration Data        │  │
│  │  • Audit Trails              │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
              │
              │ External Connections
              ▼
    ┌─────────────────┐    ┌─────────────────┐
    │   RabbitMQ      │    │    Jenkins      │
    │ (External)      │    │  (External)     │
    │ Port: 5672      │    │  Port: 8080     │
    └─────────────────┘    └─────────────────┘
```

## 🚀 Deployment Guide

### Prerequisites

**System Requirements**:
- Docker and Docker Compose installed
- Minimum 2GB RAM, 1 CPU core
- Port 5432 available for PostgreSQL
- Network access to external RabbitMQ and Jenkins services

**External Services Required**:
- RabbitMQ server accessible on port 5672
- Jenkins server accessible on port 8080
- Network connectivity between this application and external services

### Deployment Steps

**Step 1: Configure Environment Variables**
```bash
# Update .env file with your external service endpoints
# See Configuration Guide section below for details
```

**Step 2: Generate TLS Certificates (if needed)**
```bash
# Set your Docker host details
export SERVER_HOSTNAME=your-docker-host
export SERVER_IP=your-docker-ip

# Generate certificates for Docker API access
chmod +x generate_docker_certs.sh
./generate_docker_certs.sh
```

**Step 3: Deploy Application**
```bash
# Start the main application and PostgreSQL
docker-compose up -d

# Verify services are running
docker-compose ps

# Check application logs
docker-compose logs -f app
```

**Step 4: Verify External Connectivity**
```bash
# Test connections to external services
docker-compose logs app | grep -i "rabbitmq"
docker-compose logs app | grep -i "jenkins"
```

### Development vs Production Considerations

**Development Environment**:
- Deploy with external services on localhost
- Enable debug logging: `JAVA_OPTS=-Ddebug=true`
- Use default credentials for testing
- Disable TLS for Docker API

**Production Environment**:
- Connect to production RabbitMQ and Jenkins instances
- Change default PostgreSQL passwords in .env file
- Enable TLS certificates for Docker API
- Implement proper backup strategies
- Set up monitoring and alerting
- Use secure network connections to external services

## ⚙️ Configuration Guide

### Environment Variables Configuration

#### Main Application (.env)
```bash
# External Service Connections (Update IPs for multi-machine deployment)
RABBITMQ_HOSTNAME=localhost          # Change to RabbitMQ machine IP
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_PORT=5672

JENKINS_URL=http://localhost:8080    # Change to Jenkins machine IP
JENKINS_USERNAME=admin
JENKINS_PASSWORD=admin123

# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
POSTGRES_PORT=5432
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=postgres

# Docker API Configuration
DOCKER_HOST=tcp://51.250.37.180:2376
```



### Network Connectivity Setup

**For Single Machine**:
- All services communicate via Docker's internal networking
- No additional network configuration required

**For External Service Connectivity**:
1. **Update IP Addresses**: Modify .env file with actual external service IPs
2. **Firewall Configuration**: Ensure the following ports are accessible:
   - Application → Jenkins: Port 8080
   - Application → RabbitMQ: Ports 5672, 15672
3. **Network Testing**:
   ```bash
   # Test connectivity to external services
   telnet <jenkins-ip> 8080
   telnet <rabbitmq-ip> 5672
   curl http://<rabbitmq-ip>:15672
   ```

### Security Configuration

#### TLS Certificates for Docker API
```bash
# Set environment variables
export SERVER_HOSTNAME=your-docker-host
export SERVER_IP=your-docker-ip

# Generate certificates
chmod +x generate_docker_certs.sh
./generate_docker_certs.sh

# Certificates will be placed in:
# - server_cert/ (for Docker daemon)
# - client_cert/ (for client authentication)
# - src/main/resources/cert/ (for Java application)
```

#### Security Best Practices
1. **Change Default Passwords**: Update all default credentials in .env files
2. **Use Strong Passwords**: Generate complex passwords for production
3. **Network Isolation**: Use VPNs or private networks for multi-machine deployments
4. **Regular Updates**: Keep Docker images updated
5. **Access Control**: Limit access to management interfaces (Jenkins, RabbitMQ)

## 🔧 Operational Instructions

### Starting Services

**Complete Startup**:
```bash
# Start main application and PostgreSQL
docker-compose up -d

# Check service status
docker-compose ps
```

**Individual Service Startup**:
```bash
# Start PostgreSQL first
docker-compose up -d postgres

# Start main application
docker-compose up -d app
```

### Stopping Services

**Graceful Shutdown**:
```bash
# Stop main application and PostgreSQL
docker-compose down
```

**Force Stop**:
```bash
# Force stop all containers
docker-compose kill
```

### Service Health Checks

**Application Health Verification**:
```bash
# Check container status
docker-compose ps

# Verify PostgreSQL is ready
pg_isready -h localhost -p 5432 || echo "PostgreSQL not ready"

# Check application logs for startup completion
docker-compose logs app | grep -i "started"
```

**External Service Connectivity**:
```bash
# Test external service connections (update IPs as needed)
curl -f http://your-jenkins-ip:8080 || echo "Jenkins not accessible"
curl -f http://your-rabbitmq-ip:15672 || echo "RabbitMQ not accessible"
telnet your-rabbitmq-ip 5672 || echo "RabbitMQ AMQP not accessible"
```

### Monitoring and Logging

**Real-time Log Monitoring**:
```bash
# Follow all logs
docker-compose logs -f

# Follow specific service logs
docker-compose logs -f app
docker-compose logs -f postgres
```

**Log Analysis**:
```bash
# Search for errors
docker-compose logs app | grep -i error
docker-compose logs app | grep -i exception

# Check startup logs
docker-compose logs app | head -50

# Export logs to file
docker-compose logs app > app-logs.txt
```

**Resource Monitoring**:
```bash
# Monitor resource usage
docker stats

# Check disk usage
docker system df

# Monitor specific container
docker stats $(docker-compose ps -q app)
```

### Troubleshooting

**Common Issues and Solutions**:

1. **External Service Connection Failures**:
   ```bash
   # Verify environment variables
   docker-compose exec app env | grep RABBITMQ
   docker-compose exec app env | grep JENKINS

   # Check application logs for connection errors
   docker-compose logs app | grep -i "connection"
   docker-compose logs app | grep -i "refused"
   ```

2. **Database Connection Issues**:
   ```bash
   # Check PostgreSQL status
   docker-compose logs postgres

   # Test database connection
   docker-compose exec postgres psql -U postgres -d postgres -c "SELECT 1;"

   # Check if PostgreSQL is accepting connections
   docker-compose exec app pg_isready -h postgres -p 5432
   ```

3. **Application Startup Issues**:
   ```bash
   # Check application logs for errors
   docker-compose logs app | grep -i error
   docker-compose logs app | grep -i exception

   # Verify JAR file exists
   docker-compose exec app ls -la /app/rabbitmq-app.jar
   ```

**Debug Mode**:
```bash
# Enable debug logging for main application
# Add to .env file:
JAVA_OPTS=-Ddebug=true -Dlogging.level.root=DEBUG

# Restart application
docker-compose restart app
```

## 📊 Service Information

| Service | URL | Default Credentials | Purpose |
|---------|-----|-------------------|---------|
| **PostgreSQL** | localhost:5432 | postgres/postgres | Application database |
| **Main Application** | localhost | - | Secret scanning engine |

### External Service Requirements

| Service | Expected URL | Purpose |
|---------|-------------|---------|
| **RabbitMQ** | your-rabbitmq-ip:5672 | Message processing |
| **Jenkins** | your-jenkins-ip:8080 | CI/CD automation |

## 🔄 Maintenance Operations

**Application Updates**:
```bash
# Update application JAR
docker-compose stop app
# Replace rabbitmq-app.jar with new version
docker-compose build app
docker-compose up -d app
```

**Data Backup**:
```bash
# Backup PostgreSQL database
docker-compose exec postgres pg_dump -U postgres postgres > backup.sql

# Backup application configuration
cp .env .env.backup
```

**System Cleanup**:
```bash
# Remove unused containers and images
docker system prune -f

# Remove unused volumes (WARNING: This will delete data)
docker volume prune -f
```

## 📁 Project Structure

```
project-root/
├── src/                         # Resources and configuration
│   └── main/resources/
│       ├── cert/               # TLS certificates (generated)
│       └── secret-patterns-db/ # Secret detection patterns
├── docker-compose.yml          # Main application services
├── .env                        # Environment configuration
├── Dockerfile                  # Application container definition
├── rabbitmq-app.jar           # Pre-built application JAR
├── generate_docker_certs.sh   # TLS certificate generator
└── README.md                   # This documentation
```

This application provides a robust foundation for automated secret detection with external service integration. Follow the deployment steps carefully and refer to the troubleshooting section for any issues.

**Note**: External RabbitMQ and Jenkins services must be deployed separately and configured in the .env file for proper connectivity.
