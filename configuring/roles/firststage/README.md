# FirstStage Ansible Role

This Ansible role deploys the **FirstStage Secret Detection Application** to a Kubernetes cluster, providing comprehensive secret scanning capabilities with AI-powered analysis integration. It serves as the core secret detection engine in the multi-stage DevSecOps platform.

## 🎯 **Overview**

The `firststage` role is a critical component of the DevSecOps platform that:

- **Builds & Deploys**: Creates containerized FirstStage application with embedded TLS certificates
- **Secret Detection**: Implements comprehensive secret scanning with 6,400+ regex patterns
- **Database Integration**: Deploys dedicated PostgreSQL instance for scan results and audit trails
- **Service Integration**: Connects to LiftOffStage services (RabbitMQ, Jenkins) for CI/CD pipeline
- **Docker API Security**: Establishes secure TLS communication with SAST node Docker daemon
- **Kubernetes Native**: Full Kubernetes deployment with persistent storage and service discovery

## 🏗️ **Architecture & Integration**

### **Role in DevSecOps Pipeline**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   LiftOffStage  │    │   FirstStage    │    │   SecondStage   │
│ (External Svcs) │ -> │ (Secret Scan)   │ -> │ (AI Analysis)   │
│                 │    │                 │    │                 │
│ • Jenkins       │    │ • Spring Boot   │    │ • API Service   │
│ • RabbitMQ      │    │ • PostgreSQL    │    │ • AI Integration│
│ • PostgreSQL CI │    │ • Docker API    │    │ • Chat Memory   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### **Core Components**
- **🔍 Secret Detection Engine**: Spring Boot 3.3.0 application with Java 21
- **🐘 PostgreSQL Database**: Dedicated instance for scan results and metadata
- **🔐 TLS Security**: Secure Docker API communication with certificate-based authentication
- **📡 Message Integration**: RabbitMQ connectivity for CI/CD pipeline communication
- **🏗️ Jenkins Integration**: Direct API access for build monitoring and analysis
- **🌐 Kubernetes Services**: Native service discovery and networking

## 📋 **Prerequisites**

### **Infrastructure Requirements**
- **Kubernetes Cluster**: Version 1.20+ with persistent volume support
- **Node Resources**: Minimum 4 CPU cores, 8GB RAM on target worker node
- **Storage**: Local storage class with minimum 15GB available space
- **Network**: Internal cluster networking and external SAST node access

### **Dependencies**
- **FirstStage-Prep Role**: Must be executed first to copy application files and generate certificates
- **LiftOffStage Services**: RabbitMQ and Jenkins must be deployed and accessible
- **SAST Node**: Docker daemon with TLS enabled for secure API access
- **BuildKit**: For Docker image building capabilities

### **Application Files** (Provided by firststage-prep)
- **Application Archive**: Complete FirstStage application in `/opt/firststage/app-files/`
- **TLS Certificates**: Client certificates for Docker API authentication
- **Build Context**: Dockerfile and all required application resources

## ⚙️ **Configuration Variables**

All variables are defined in `defaults/main.yml` and can be overridden via Ansible Vault, group_vars, or playbook variables.

### **🌐 Application Configuration**

| Variable | Default | Description |
|----------|---------|-------------|
| `ns_firststage` | `firststage` | Kubernetes namespace for FirstStage deployment |
| `firststage_image_repo` | `docker.io/khasanabdurakhmanov/innopolis` | Docker registry repository |
| `firststage_image_name` | `firststage` | Docker image name for the application |
| `firststage_image_tag` | `latest` | Docker image tag |
| `buildkit_install_dir` | `/usr/local/bin` | BuildKit installation directory |
| `firststage_target_node` | `worker` | Target Kubernetes worker node |

### **💾 Resource Allocation**

#### **FirstStage Application Resources**
| Variable | Default | Description |
|----------|---------|-------------|
| `firststage_memory_request` | `2Gi` | Guaranteed memory allocation |
| `firststage_memory_limit` | `3Gi` | Maximum memory limit |
| `firststage_cpu_request` | `1000m` | Guaranteed CPU allocation (1 core) |
| `firststage_cpu_limit` | `2000m` | Maximum CPU limit (2 cores) |

#### **PostgreSQL Database Resources**
| Variable | Default | Description |
|----------|---------|-------------|
| `postgres_memory_request` | `1Gi` | Guaranteed memory allocation |
| `postgres_memory_limit` | `2Gi` | Maximum memory limit |
| `postgres_cpu_request` | `500m` | Guaranteed CPU allocation (0.5 cores) |
| `postgres_cpu_limit` | `1000m` | Maximum CPU limit (1 core) |

### **🔗 Service Integration**

#### **RabbitMQ Configuration (LiftOffStage)**
| Variable | Default | Description |
|----------|---------|-------------|
| `rabbitmq_hostname` | `rabbitmq.liftoffstage.svc.cluster.local` | Internal cluster service address |
| `rabbitmq_port` | `5672` | AMQP protocol port |
| `rabbitmq_username` | `liftoffstage` | RabbitMQ authentication username |
| `rabbitmq_password` | `liftoffstage123` | RabbitMQ authentication password |

#### **Jenkins Configuration (LiftOffStage)**
| Variable | Default | Description |
|----------|---------|-------------|
| `jenkins_url` | `http://jenkins-liftoff.liftoffstage.svc.cluster.local:8080` | Internal cluster service URL |
| `jenkins_username` | `admin` | Jenkins API username |
| `jenkins_password` | `admin123` | Jenkins API password |

#### **PostgreSQL Database Configuration**
| Variable | Default | Description |
|----------|---------|-------------|
| `postgres_user` | `postgres` | Database superuser account |
| `postgres_password` | `postgres` | Database password (⚠️ Change in production) |
| `postgres_db` | `postgres` | Primary database name |

### **🔐 Docker API Security**

| Variable | Default | Description |
|----------|---------|-------------|
| `sast_target_node` | `sast` | SAST node hostname for Docker API access |
| `docker_host` | `tcp://{{ hostvars[sast_target_node]['ansible_host'] }}:2376` | Dynamic Docker daemon TLS endpoint |

### **🔄 Alternative External Access**
```yaml
# For external service access instead of internal cluster services:
# rabbitmq_hostname: "{{ hostvars['worker'].ansible_host }}"
# rabbitmq_port: "32011"  # NodePort for AMQP
# jenkins_url: "http://{{ hostvars['worker'].ansible_host }}:32013"
```

## 🚀 **Deployment Process**

The FirstStage role executes a comprehensive 8-phase deployment process:

### **Phase 1: Docker Image Building**
```yaml
# Custom image creation with embedded certificates
1. Validates application files exist (from firststage-prep)
2. Uses BuildKit for advanced Docker image building
3. Builds from /opt/firststage/app-files/ context
4. Embeds TLS client certificates in application
5. Tags as firststage:latest for local use
```

### **Phase 2: Kubernetes Namespace Setup**
```yaml
# Isolated environment creation
1. Creates dedicated 'firststage' namespace
2. Applies proper labels and metadata
3. Ensures namespace isolation and security
```

### **Phase 3: Configuration Management**
```yaml
# Application configuration and secrets
1. Creates ConfigMap with environment variables
2. Generates Kubernetes Secret for sensitive data
3. Configures RabbitMQ and Jenkins connection details
4. Sets up PostgreSQL database credentials
5. Configures Docker API endpoint for SAST communication
```

### **Phase 4: Persistent Storage Setup**
```yaml
# Database storage preparation
1. Creates PersistentVolume (10Gi) on target node
2. Configures local storage at /opt/firststage-postgres
3. Creates PersistentVolumeClaim (5Gi) for PostgreSQL
4. Ensures proper storage class and access modes
```

### **Phase 5: PostgreSQL Database Deployment**
```yaml
# Dedicated database instance
1. Deploys PostgreSQL 13 with persistent storage
2. Configures resource limits and requests
3. Sets up health checks and readiness probes
4. Applies node affinity to target worker node
5. Creates internal service for database access
```

### **Phase 6: FirstStage Application Deployment**
```yaml
# Main application deployment
1. Deploys Spring Boot application container
2. Configures resource allocation (2-3Gi memory, 1-2 CPU cores)
3. Mounts configuration and secrets
4. Sets up comprehensive health checks
5. Applies node affinity for consistent placement
6. Configures imagePullPolicy: Never (local image)
```

### **Phase 7: Service Exposure**
```yaml
# Network access configuration
1. Creates Kubernetes Service for internal access
2. Exposes application on NodePort 32015
3. Configures service discovery within cluster
4. Enables external access for monitoring and testing
```

### **Phase 8: Deployment Validation**
```yaml
# Health and readiness verification
1. Waits for PostgreSQL to be ready
2. Validates FirstStage application startup
3. Confirms service endpoints are accessible
4. Verifies resource allocation and node placement
```

## 📖 **Usage Examples**

### **Basic Production Deployment**
```yaml
---
- name: Deploy FirstStage Secret Detection Platform
  hosts: master
  become: yes
  roles:
    - role: firststage
  vars:
    # Application configuration
    ns_firststage: "firststage-prod"
    firststage_target_node: "firststage-worker"

    # High-performance resource allocation
    firststage_memory_limit: "8Gi"
    firststage_cpu_limit: "4000m"
    postgres_memory_limit: "4Gi"
    postgres_cpu_limit: "2000m"

    # Production service integration
    rabbitmq_username: "firststage_prod"
    rabbitmq_password: "{{ vault_rabbitmq_password }}"
    jenkins_username: "firststage_api"
    jenkins_password: "{{ vault_jenkins_password }}"
    postgres_password: "{{ vault_postgres_password }}"
```

### **Development Environment**
```yaml
---
- name: Deploy FirstStage for Development
  hosts: master
  become: yes
  roles:
    - role: firststage
  vars:
    # Development configuration
    ns_firststage: "firststage-dev"
    firststage_image_tag: "dev"

    # Reduced resources for development
    firststage_memory_request: "1Gi"
    firststage_memory_limit: "2Gi"
    firststage_cpu_request: "500m"
    firststage_cpu_limit: "1000m"

    postgres_memory_request: "512Mi"
    postgres_memory_limit: "1Gi"
```

### **External Service Access Configuration**
```yaml
---
- name: Deploy FirstStage with External Services
  hosts: master
  become: yes
  roles:
    - role: firststage
  vars:
    # External RabbitMQ access via NodePort
    rabbitmq_hostname: "{{ hostvars['liftoffstage'].ansible_host }}"
    rabbitmq_port: "32011"  # NodePort

    # External Jenkins access via NodePort
    jenkins_url: "http://{{ hostvars['liftoffstage'].ansible_host }}:32013"

    # Custom Docker API endpoint
    sast_target_node: "sast-production"
```

### **Ansible Vault Integration**
```yaml
---
- name: Deploy FirstStage with Encrypted Secrets
  hosts: master
  become: yes
  vars_files:
    - "{{ inventory_dir }}/vault/secrets.yml"
  roles:
    - role: firststage
  vars:
    # Use vault-encrypted credentials
    rabbitmq_username: "{{ vault_firststage_rabbitmq_user }}"
    rabbitmq_password: "{{ vault_firststage_rabbitmq_pass }}"
    jenkins_username: "{{ vault_firststage_jenkins_user }}"
    jenkins_password: "{{ vault_firststage_jenkins_pass }}"
    postgres_password: "{{ vault_firststage_postgres_pass }}"

    # Custom image repository
    firststage_image_repo: "{{ vault_docker_registry }}/firststage"
```

## 🌐 **Service Access & Endpoints**

After successful deployment, the FirstStage application provides multiple access points:

### **🔍 FirstStage Secret Detection API**
```yaml
# External Access (NodePort)
URL: http://<worker-node-ip>:32015
Health Check: http://<worker-node-ip>:32015/actuator/health
API Documentation: http://<worker-node-ip>:32015/swagger-ui.html

# Internal Cluster Access
Host: firststage-app.firststage.svc.cluster.local
Port: 8080
Health Endpoint: /actuator/health
Metrics Endpoint: /actuator/metrics
```

### **🐘 PostgreSQL Database**
```yaml
# Internal Cluster Access (from other pods)
Host: firststage-postgres.firststage.svc.cluster.local
Port: 5432
Database: postgres
Username: postgres
Connection String: postgresql://postgres:<password>@firststage-postgres:5432/postgres
```

### **📊 Application Features**
- **Secret Detection**: Comprehensive scanning with 6,400+ regex patterns
- **REST API**: Full RESTful API for integration and monitoring
- **Health Monitoring**: Spring Boot Actuator endpoints
- **Database Integration**: Persistent storage for scan results
- **Message Queue**: RabbitMQ integration for CI/CD pipeline
- **Jenkins Integration**: Direct API access for build monitoring

## 🐳 **Container Image Details**

### **Custom Built Image**
```yaml
Image Name: firststage:latest
Build Context: /opt/firststage/app-files/
Base Image: openjdk:21-jdk-slim
Pull Policy: Never (locally built)
Registry: Local node registry
```

### **Embedded Components**
- **Spring Boot 3.3.0**: Modern Java framework with native compilation support
- **TLS Certificates**: Client certificates for secure Docker API communication
- **Secret Patterns**: 6,400+ regex patterns for comprehensive secret detection
- **Application JAR**: Complete application with all dependencies
- **Configuration**: Environment-specific configuration files

### **Security Features**
- **Non-root User**: Runs as dedicated application user
- **TLS Communication**: Encrypted Docker API access
- **Secret Management**: Kubernetes secrets for sensitive data
- **Resource Limits**: CPU and memory constraints for security

## 💾 **Storage Architecture**

### **Persistent Volume Configuration**
```yaml
# PostgreSQL Data Storage
PersistentVolume:
  Name: firststage-postgres-pv
  Size: 10Gi
  Path: /opt/firststage-postgres
  Access Mode: ReadWriteOnce
  Storage Class: local-storage

PersistentVolumeClaim:
  Name: firststage-postgres-pvc
  Size: 5Gi
  Access Mode: ReadWriteOnce
```

### **Data Persistence**
- **Database Files**: PostgreSQL data directory with full ACID compliance
- **Scan Results**: Persistent storage of secret detection results
- **Audit Trails**: Complete audit logging of all scanning activities
- **Configuration**: Application configuration and runtime state

### **Backup Considerations**
- **Database Backups**: Regular PostgreSQL dumps recommended
- **Volume Snapshots**: Kubernetes volume snapshot support
- **Data Retention**: Configurable retention policies for scan results

## 🚨 **Troubleshooting Guide**

### **Common Issues & Solutions**

#### **🔴 Pod Startup Issues**
```bash
# Check pod status and events
kubectl get pods -n firststage
kubectl describe pod -n firststage <pod-name>

# View application logs
kubectl logs -n firststage deployment/firststage-app --tail=100
kubectl logs -n firststage deployment/firststage-postgres --tail=100

# Check resource constraints
kubectl top pods -n firststage
kubectl describe nodes
```

#### **🔴 Database Connection Issues**
```bash
# Test PostgreSQL connectivity
kubectl exec -it -n firststage deployment/firststage-postgres -- psql -U postgres -d postgres

# Check database service
kubectl get svc -n firststage firststage-postgres
kubectl describe svc -n firststage firststage-postgres

# Verify database credentials
kubectl get secret -n firststage firststage-secrets -o yaml
```

#### **🔴 External Service Connectivity**
```bash
# Test RabbitMQ connectivity from pod
kubectl exec -it -n firststage deployment/firststage-app -- curl -v telnet://rabbitmq.liftoffstage.svc.cluster.local:5672

# Test Jenkins connectivity
kubectl exec -it -n firststage deployment/firststage-app -- curl -v http://jenkins-liftoff.liftoffstage.svc.cluster.local:8080

# Check service discovery
kubectl get svc -n liftoffstage
nslookup rabbitmq.liftoffstage.svc.cluster.local
```

#### **🔴 Docker Image Build Issues**
```bash
# Check BuildKit installation
ls -la /usr/local/bin/buildctl-daemonless.sh

# Verify application files exist
ls -la /opt/firststage/app-files/
ls -la /opt/firststage/app-files/Dockerfile

# Check build logs
kubectl logs -n firststage deployment/firststage-app --previous
```

#### **🔴 Storage Issues**
```bash
# Check persistent volumes
kubectl get pv firststage-postgres-pv
kubectl get pvc -n firststage firststage-postgres-pvc

# Verify storage directory
ls -la /opt/firststage-postgres/
df -h /opt/firststage-postgres/

# Check storage class
kubectl get storageclass local-storage
```

### **🔧 Diagnostic Commands**
```bash
# Complete cluster status
kubectl get all -n firststage

# Resource usage monitoring
kubectl top pods -n firststage
kubectl top nodes

# Network connectivity testing
kubectl exec -it -n firststage deployment/firststage-app -- netstat -tlnp
kubectl exec -it -n firststage deployment/firststage-app -- ss -tlnp

# Application health checks
curl http://<worker-node-ip>:32015/actuator/health
curl http://<worker-node-ip>:32015/actuator/info
```

## 🔐 **Security Architecture**

### **TLS Certificate Management**
The FirstStage role integrates with the firststage-prep role for comprehensive TLS security:

#### **Certificate Integration**
```yaml
# Certificates provided by firststage-prep role:
Client Certificates (Embedded in Application):
  - /opt/firststage/app-files/src/main/resources/cert/ca.pem
  - /opt/firststage/app-files/src/main/resources/cert/cert.pem
  - /opt/firststage/app-files/src/main/resources/cert/key.pem

Server Certificates (For SAST Node):
  - {{ playbook_dir }}/server_certificates/ca.pem
  - {{ playbook_dir }}/server_certificates/server-cert.pem
  - {{ playbook_dir }}/server_certificates/server-key.pem
```

#### **Docker API Security**
- **Mutual TLS**: Client and server certificate authentication
- **Encrypted Communication**: All Docker API calls encrypted with TLS 1.3
- **Certificate Validation**: Automatic certificate validation and trust chain verification
- **Secure Key Storage**: Private keys with restrictive file permissions

### **Kubernetes Security**
```yaml
# Security features implemented:
Secrets Management:
  - Kubernetes Secrets for sensitive data
  - Base64 encoded credentials
  - Automatic secret mounting

Network Security:
  - Internal service discovery
  - Namespace isolation
  - Service-to-service communication

Resource Security:
  - CPU and memory limits
  - Non-root container execution
  - Read-only root filesystem where possible
```

### **Application Security**
- **Spring Security**: Comprehensive security framework integration
- **Input Validation**: Robust input sanitization and validation
- **SQL Injection Protection**: Parameterized queries and ORM security
- **Cross-Site Scripting (XSS) Protection**: Output encoding and CSP headers
- **Authentication & Authorization**: Role-based access control

## 🔗 **Integration with DevSecOps Pipeline**

### **Role Dependencies**
```yaml
# Execution order in main playbook:
1. common                    # Basic system setup
2. containerd               # Container runtime
3. kubernetes               # Kubernetes cluster
4. worker                   # Worker node configuration
5. liftoffstage            # External services (Jenkins, RabbitMQ)
6. firststage-prep         # Application preparation and certificates
7. firststage              # THIS ROLE - Main application deployment
8. secondstage             # AI analysis integration (optional)
```

### **Service Communication**
```yaml
# FirstStage integrates with:
RabbitMQ (LiftOffStage):
  - Receives scan requests from CI/CD pipeline
  - Publishes scan results and notifications
  - Queue-based asynchronous processing

Jenkins (LiftOffStage):
  - Monitors build status and triggers
  - Retrieves build artifacts for scanning
  - Reports scan results back to builds

SAST Node (Docker API):
  - Secure container analysis
  - Dynamic code scanning
  - Runtime security assessment

SecondStage (Optional):
  - AI-powered analysis of scan results
  - Advanced threat detection
  - Intelligent false positive reduction
```

## 📊 **Performance & Monitoring**

### **Resource Monitoring**
```bash
# Application performance metrics
curl http://<worker-node-ip>:32015/actuator/metrics
curl http://<worker-node-ip>:32015/actuator/prometheus

# Database performance
kubectl exec -it -n firststage deployment/firststage-postgres -- pg_stat_activity

# System resource usage
kubectl top pods -n firststage
kubectl describe nodes
```

### **Scaling Considerations**
- **Horizontal Scaling**: Multiple FirstStage replicas for high throughput
- **Database Scaling**: PostgreSQL read replicas for improved performance
- **Resource Allocation**: Dynamic resource adjustment based on workload
- **Load Balancing**: Kubernetes service load balancing for multiple replicas

## 📄 **License & Support**

### **License Information**
- **License**: MIT License
- **Author**: Khasan Abdurakhmanov
- **Organization**: Innopolis University DevSecOps Platform
- **Version**: 2.0.0
- **Last Updated**: 2025

### **Support Resources**
- **Documentation**: Comprehensive README with examples and troubleshooting
- **Community Support**: DevSecOps Platform community forums
- **Issue Tracking**: GitHub Issues for bug reports and feature requests
- **Professional Support**: Enterprise support available through Innopolis University

---

**🎉 Thank you for using the FirstStage Secret Detection Platform!**

This role provides enterprise-grade secret detection capabilities with comprehensive security, monitoring, and integration features. For advanced configurations and enterprise support, please contact the DevSecOps Platform team.
