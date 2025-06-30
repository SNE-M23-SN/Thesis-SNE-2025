# SecondStage Ansible Role

This Ansible role deploys the **SecondStage AI-Powered Monitoring & Analysis Platform** to a Kubernetes cluster, providing advanced CI/CD monitoring, anomaly detection, and AI-driven analysis capabilities. It serves as the intelligent analysis layer in the multi-stage DevSecOps platform.

## 🎯 **Overview**

The `secondstage` role is an advanced component of the DevSecOps platform that:

- **Dual Application Architecture**: Deploys API (Jenkins monitoring) and Diploma (AI analysis) applications
- **AI-Powered Analysis**: Integrates Google Gemini AI for intelligent anomaly detection and conversation memory
- **VPN Security**: Establishes secure VPN tunnels for external AI API access with domain-specific routing
- **Service Integration**: Seamlessly connects with LiftOffStage services (PostgreSQL, RabbitMQ, Jenkins)
- **Advanced Monitoring**: Provides comprehensive CI/CD pipeline monitoring and analytics
- **Kubernetes Native**: Full containerized deployment with persistent configurations and health monitoring

## 🏗️ **Architecture & Integration**

### **Role in DevSecOps Pipeline**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   LiftOffStage  │    │   FirstStage    │    │   SecondStage   │
│ (External Svcs) │ -> │ (Secret Scan)   │ -> │ (AI Analysis)   │
│                 │    │                 │    │                 │
│ • Jenkins       │    │ • Spring Boot   │    │ • API Service   │
│ • RabbitMQ      │    │ • PostgreSQL    │    │ • AI Integration│
│ • PostgreSQL CI │    │ • Docker API    │    │ • VPN Security  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### **Dual Application Architecture**
```
┌─────────────────────────────────────────────────────────────┐
│                    SecondStage Platform                     │
├─────────────────────────────┬───────────────────────────────┤
│        API Application      │      Diploma Application     │
│         (Port 8383)         │         (Port 8282)          │
├─────────────────────────────┼───────────────────────────────┤
│ • Jenkins Monitoring        │ • AI Anomaly Detection       │
│ • Build Analytics           │ • Google Gemini Integration  │
│ • Dashboard API             │ • Conversation Memory        │
│ • PostgreSQL Integration    │ • RabbitMQ Processing        │
│ • NodePort: 32016           │ • VPN-Secured AI Access      │
│                             │ • NodePort: 32017            │
└─────────────────────────────┴───────────────────────────────┘
```

## 📋 **Prerequisites**

### **Infrastructure Requirements**
- **Kubernetes Cluster**: Version 1.20+ with persistent volume support
- **Node Resources**: Minimum 6 CPU cores, 12GB RAM on target worker node
- **Network**: VPN capability and external internet access for AI services
- **Storage**: Local storage for application files and VPN configurations

### **Dependencies**
- **LiftOffStage Services**: PostgreSQL CI, RabbitMQ, and Jenkins must be deployed and accessible
- **BuildKit**: For Docker image building capabilities
- **OpenVPN**: For secure AI API access tunneling
- **Network Tools**: iproute2, iptables-persistent, dnsutils for VPN configuration

### **Required Credentials**
- **Google Gemini AI API Key**: Essential for Diploma application AI functionality
- **Service Credentials**: Access to LiftOffStage PostgreSQL, RabbitMQ, and Jenkins
- **VPN Configurations**: OpenVPN configuration files for secure external access

### **Application Files** (SecondStage.zip)
- **API Application**: Complete Spring Boot application for Jenkins monitoring
- **Diploma Application**: AI-powered analysis application with conversation memory
- **VPN Scripts**: Automated VPN setup and domain routing scripts
- **Configuration Files**: 30+ worldwide VPN server configurations

## ⚙️ **Configuration Variables**

All variables are defined in `defaults/main.yml` and can be overridden via Ansible Vault, group_vars, or playbook variables.

### **🌐 Core Configuration**

| Variable | Default | Description |
|----------|---------|-------------|
| `buildkit_install_dir` | `/usr/local/bin` | BuildKit installation directory |
| `ns_secondstage` | `secondstage` | Kubernetes namespace for SecondStage deployment |
| `secondstage_target_node` | `worker` | Target Kubernetes worker node |
| `secondstage_image_repo` | `docker.io/khasanabdurakhmanov/innopolis` | Docker registry repository |

### **🔍 API Application Configuration (Jenkins Monitoring)**

#### **Application Settings**
| Variable | Default | Description |
|----------|---------|-------------|
| `api_app_port` | `8383` | Internal application port |
| `api_nodeport` | `32016` | External NodePort for access |
| `secondstage_api_image_name` | `secondstage` | Docker image name |
| `secondstage_api_image_tag` | `api` | Docker image tag |

#### **Jenkins Integration**
| Variable | Default | Description |
|----------|---------|-------------|
| `jenkins_url` | `http://jenkins-liftoff.liftoffstage.svc.cluster.local:8080` | Internal Jenkins service URL |
| `api_jenkins_username` | `admin` | Jenkins API username |
| `api_jenkins_password` | `admin123` | Jenkins API password |

#### **Database Configuration**
| Variable | Default | Description |
|----------|---------|-------------|
| `api_database_url` | `jdbc:postgresql://postgres-ci.liftoffstage.svc.cluster.local:5432/postgres` | PostgreSQL connection URL |
| `api_database_username` | `postgres` | Database username |
| `api_database_password` | `postgres` | Database password |

#### **Application Settings**
| Variable | Default | Description |
|----------|---------|-------------|
| `api_cors_origins` | `*` | CORS allowed origins |
| `api_log_level` | `DEBUG` | Application logging level |

#### **Resource Allocation**
| Variable | Default | Description |
|----------|---------|-------------|
| `api_memory_request` | `2Gi` | Guaranteed memory allocation |
| `api_memory_limit` | `4Gi` | Maximum memory limit |
| `api_cpu_request` | `1000m` | Guaranteed CPU allocation (1 core) |
| `api_cpu_limit` | `3000m` | Maximum CPU limit (3 cores) |

### **🤖 Diploma Application Configuration (AI Analysis)**

#### **Application Settings**
| Variable | Default | Description |
|----------|---------|-------------|
| `diploma_app_port` | `8282` | Internal application port |
| `diploma_nodeport` | `32017` | External NodePort for access |
| `secondstage_app_image_name` | `secondstage` | Docker image name |
| `secondstage_app_image_tag` | `app` | Docker image tag |

#### **AI Configuration** ⚠️ **REQUIRED**
| Variable | Default | Description |
|----------|---------|-------------|
| `diploma_ai_api_key` | `""` | **REQUIRED**: Google Gemini AI API key |
| `ai_model` | `gemini-2.5-pro` | AI model for analysis |
| `ai_temperature` | `0.3` | AI response creativity (0.0-1.0) |
| `ai_response_format` | `JSON_OBJECT` | AI response format |
| `ai_base_url` | `https://generativelanguage.googleapis.com` | Google Gemini API base URL |

#### **Database Configuration**
| Variable | Default | Description |
|----------|---------|-------------|
| `diploma_database_url` | `jdbc:postgresql://postgres-ci.liftoffstage.svc.cluster.local:5432/postgres` | PostgreSQL connection URL |
| `diploma_database_username` | `postgres` | Database username |
| `diploma_database_password` | `postgres` | Database password |

#### **RabbitMQ Configuration**
| Variable | Default | Description |
|----------|---------|-------------|
| `rabbitmq_host` | `rabbitmq.liftoffstage.svc.cluster.local` | RabbitMQ hostname |
| `rabbitmq_port` | `5672` | RabbitMQ AMQP port |
| `diploma_rabbitmq_username` | `liftoffstage` | RabbitMQ username |
| `diploma_rabbitmq_password` | `liftoffstage123` | RabbitMQ password |
| `queue_name` | `Jenkins` | Primary queue name |

#### **Application Settings**
| Variable | Default | Description |
|----------|---------|-------------|
| `diploma_log_level` | `DEBUG` | Application logging level |

#### **Resource Allocation**
| Variable | Default | Description |
|----------|---------|-------------|
| `diploma_memory_request` | `4Gi` | Guaranteed memory allocation |
| `diploma_memory_limit` | `6Gi` | Maximum memory limit |
| `diploma_cpu_request` | `2000m` | Guaranteed CPU allocation (2 cores) |
| `diploma_cpu_limit` | `4000m` | Maximum CPU limit (4 cores) |

### **🔐 VPN Configuration**

| Variable | Default | Description |
|----------|---------|-------------|
| `vpn_namespace` | `vpnspace` | VPN network namespace |
| `vpn_config_file` | `Canada, Quebec S2.ovpn` | Default VPN server configuration |
| `domain_to_route` | `generativelanguage.googleapis.com` | Domain to route through VPN |

### **🔗 External Service Integration**

| Variable | Default | Description |
|----------|---------|-------------|
| `postgres_ci_nodeport` | `30432` | PostgreSQL CI external port |
| `rabbitmq_amqp_nodeport` | `32011` | RabbitMQ AMQP external port |

## 🚀 **Deployment Process**

The SecondStage role executes a comprehensive 11-phase deployment process:

### **Phase 1: System Preparation**
```yaml
# Package installation and system setup
1. Handles APT lock management safely
2. Installs OpenVPN, networking tools, and utilities
3. Installs Python prerequisites for Kubernetes management
4. Prepares system for VPN and container operations
```

### **Phase 2: Namespace & File Management**
```yaml
# Kubernetes environment and application files
1. Creates dedicated 'secondstage' namespace
2. Creates application directory on target node
3. Copies SecondStage.zip to target node
4. Extracts application files with proper permissions
5. Validates all required files are present
```

### **Phase 3: Docker Image Building**
```yaml
# Custom image creation for both applications
1. Creates Docker registry pull secret
2. Builds API application image (secondstage:api)
3. Builds Diploma application image (secondstage:app)
4. Validates successful image creation
5. Displays build summaries with timestamps
```

### **Phase 4: Configuration Management**
```yaml
# Application configuration and secrets
1. Creates ConfigMap with environment variables
2. Generates Kubernetes Secret for sensitive data
3. Configures AI API keys and service credentials
4. Sets up database and message queue connections
```

### **Phase 5: Diploma Application Deployment**
```yaml
# AI-powered analysis application
1. Deploys Diploma application with AI integration
2. Configures resource limits (4-6Gi memory, 2-4 CPU cores)
3. Sets up health checks and readiness probes
4. Applies node affinity to target worker node
5. Creates internal service for cluster communication
6. Waits for deployment readiness (10 minutes timeout)
```

### **Phase 6: API Application Deployment**
```yaml
# Jenkins monitoring application
1. Deploys API application for Jenkins monitoring
2. Configures resource limits (2-4Gi memory, 1-3 CPU cores)
3. Sets up comprehensive health checks
4. Applies node affinity for consistent placement
5. Creates NodePort service for external access
```

### **Phase 7: VPN Infrastructure Setup**
```yaml
# Secure AI API access preparation
1. Copies VPN configuration files to /etc/openvpn/
2. Installs VPN management scripts to /usr/local/bin/
3. Sets proper permissions for VPN operations
4. Prepares 30+ worldwide VPN server configurations
```

### **Phase 8: VPN Network Configuration**
```yaml
# Network namespace and routing setup
1. Creates isolated VPN namespace (vpnspace)
2. Configures domain-specific routing for AI APIs
3. Sets up iptables rules for secure tunneling
4. Prepares network infrastructure for VPN connection
```

### **Phase 9: Application Readiness Validation**
```yaml
# Health and readiness verification
1. Waits for API application to be ready (5 minutes timeout)
2. Waits for Diploma application to be ready (10 minutes timeout)
3. Validates service endpoints are accessible
4. Confirms resource allocation and node placement
```

### **Phase 10: VPN Activation & Connectivity**
```yaml
# Secure tunnel establishment
1. Starts VPN connection with selected configuration
2. Tests connectivity to external AI services
3. Validates LiftOffStage service accessibility
4. Performs API endpoint health checks
```

### **Phase 11: Status Reporting & Documentation**
```yaml
# Deployment summary and information
1. Displays comprehensive deployment status
2. Shows application URLs and access points
3. Reports VPN status and configuration
4. Creates deployment ConfigMap with service information
5. Provides troubleshooting and management guidance
```

## 🔐 **VPN Security Architecture**

### **Advanced VPN Features**
```yaml
# Secure AI API Access
Network Isolation:
  - Dedicated network namespace (vpnspace)
  - Isolated routing tables
  - Secure tunnel encryption

Domain-Specific Routing:
  - Routes only AI API traffic through VPN
  - Preserves local cluster communication
  - Automatic failover and reconnection

Multi-Server Support:
  - 30+ worldwide VPN server locations
  - Configurable server selection
  - Load balancing and redundancy
```

### **VPN Management Scripts**
```bash
# Automated VPN operations
/usr/local/bin/vpn-setup.sh:
  - setup: Creates VPN namespace and configuration
  - start-vpn: Establishes VPN connection
  - status: Checks VPN connection status
  - cleanup: Removes VPN configuration

/usr/local/bin/domain-route.sh:
  - setup: Configures domain-specific routing
  - status: Checks routing configuration
  - cleanup: Removes routing rules
```

### **VPN Configuration Management**
```yaml
# Configuration files and locations
VPN Configs: /etc/openvpn/
  - Canada, Quebec S2.ovpn (default)
  - Multiple worldwide locations
  - Standard OpenVPN format

Management Scripts: /usr/local/bin/
  - vpn-setup.sh (VPN operations)
  - domain-route.sh (routing management)
  - Executable permissions set automatically
```

## 📖 **Usage Examples**

### **Production Deployment with AI Integration**
```yaml
---
- name: Deploy SecondStage AI Monitoring Platform
  hosts: master
  become: yes
  roles:
    - role: secondstage
  vars:
    # Core configuration
    ns_secondstage: "secondstage-prod"
    secondstage_target_node: "ai-worker"

    # AI configuration (REQUIRED)
    diploma_ai_api_key: "{{ vault_gemini_api_key }}"
    ai_model: "gemini-2.5-pro"
    ai_temperature: "0.2"  # More deterministic responses

    # High-performance resource allocation
    api_memory_limit: "8Gi"
    api_cpu_limit: "4000m"
    diploma_memory_limit: "12Gi"
    diploma_cpu_limit: "6000m"

    # VPN configuration for secure AI access
    vpn_config_file: "US, New York S1.ovpn"
    domain_to_route: "generativelanguage.googleapis.com"
```

### **Development Environment**
```yaml
---
- name: Deploy SecondStage for Development
  hosts: master
  become: yes
  roles:
    - role: secondstage
  vars:
    # Development configuration
    ns_secondstage: "secondstage-dev"

    # Reduced resources for development
    api_memory_request: "1Gi"
    api_memory_limit: "2Gi"
    api_cpu_request: "500m"
    api_cpu_limit: "1000m"

    diploma_memory_request: "2Gi"
    diploma_memory_limit: "4Gi"
    diploma_cpu_request: "1000m"
    diploma_cpu_limit: "2000m"

    # Development AI settings
    diploma_ai_api_key: "{{ vault_dev_gemini_key }}"
    ai_temperature: "0.5"  # More creative responses for testing
    api_log_level: "DEBUG"
    diploma_log_level: "DEBUG"
```

### **Custom Service Integration**
```yaml
---
- name: Deploy SecondStage with Custom Services
  hosts: master
  become: yes
  roles:
    - role: secondstage
  vars:
    # Custom external service endpoints
    jenkins_url: "http://custom-jenkins.company.com:8080"
    api_database_url: "jdbc:postgresql://custom-db.company.com:5432/cicd"
    diploma_database_url: "jdbc:postgresql://custom-db.company.com:5432/analysis"
    rabbitmq_host: "custom-rabbitmq.company.com"

    # Custom credentials
    api_jenkins_username: "{{ vault_jenkins_user }}"
    api_jenkins_password: "{{ vault_jenkins_pass }}"
    api_database_username: "{{ vault_db_user }}"
    api_database_password: "{{ vault_db_pass }}"
```

### **Ansible Vault Integration**
```yaml
---
- name: Deploy SecondStage with Encrypted Secrets
  hosts: master
  become: yes
  vars_files:
    - "{{ inventory_dir }}/vault/secrets.yml"
  roles:
    - role: secondstage
  vars:
    # Use vault-encrypted credentials
    diploma_ai_api_key: "{{ vault_secondstage_ai_key }}"
    api_jenkins_username: "{{ vault_secondstage_jenkins_user }}"
    api_jenkins_password: "{{ vault_secondstage_jenkins_pass }}"
    api_database_password: "{{ vault_secondstage_db_pass }}"
    diploma_database_password: "{{ vault_secondstage_db_pass }}"
    diploma_rabbitmq_username: "{{ vault_secondstage_rabbitmq_user }}"
    diploma_rabbitmq_password: "{{ vault_secondstage_rabbitmq_pass }}"

    # Custom image repository
    secondstage_image_repo: "{{ vault_docker_registry }}/secondstage"
```

## 🌐 **Service Access & Endpoints**

After successful deployment, the SecondStage platform provides multiple access points:

### **🔍 API Application (Jenkins Monitoring)**
```yaml
# External Access (NodePort)
URL: http://<worker-node-ip>:32016/api/dashboard
Health Check: http://<worker-node-ip>:32016/actuator/health
API Documentation: http://<worker-node-ip>:32016/swagger-ui.html

# Internal Cluster Access
Host: secondstage-api.secondstage.svc.cluster.local
Port: 8383
Dashboard Endpoint: /api/dashboard
Jobs Endpoint: /api/dashboard/jobs
Metrics Endpoint: /actuator/metrics
```

### **🤖 Diploma Application (AI Analysis)**
```yaml
# External Access (NodePort)
URL: http://<worker-node-ip>:32017/actuator/health
AI Analysis API: http://<worker-node-ip>:32017/api/analyze
Conversation API: http://<worker-node-ip>:32017/api/conversation

# Internal Cluster Access
Host: secondstage-diploma.secondstage.svc.cluster.local
Port: 8282
Health Endpoint: /actuator/health
AI Endpoint: /api/analyze
Memory Endpoint: /api/conversation/memory
```

### **📊 Application Features**
- **Jenkins Monitoring**: Real-time build status, analytics, and anomaly detection
- **AI Analysis**: Google Gemini-powered intelligent analysis with conversation memory
- **Health Monitoring**: Spring Boot Actuator endpoints for both applications
- **Database Integration**: Persistent storage for analysis results and conversation history
- **Message Queue**: RabbitMQ integration for asynchronous processing
- **VPN Security**: Secure AI API access through encrypted tunnels

## 🔗 **External Service Integration**

### **🐘 PostgreSQL CI (LiftOffStage)**
```yaml
# Database connectivity for both applications
Internal Access:
  Host: postgres-ci.liftoffstage.svc.cluster.local
  Port: 5432
  Database: postgres

External Access:
  Host: <worker-node-ip>
  Port: 30432
  Connection: Direct TCP access

Usage:
  - API Application: Build data, analytics, monitoring results
  - Diploma Application: AI analysis results, conversation memory
  - Persistent Storage: All application data and state
```

### **🐰 RabbitMQ (LiftOffStage)**
```yaml
# Message queue for asynchronous processing
Internal Access:
  Host: rabbitmq.liftoffstage.svc.cluster.local
  Port: 5672 (AMQP)
  Management: 15672

External Access:
  AMQP: <worker-node-ip>:32011
  Management: <worker-node-ip>:32012
  Web UI: http://<worker-node-ip>:32012

Usage:
  - Diploma Application: Queue processing for AI analysis
  - Message Types: Build events, analysis requests, results
  - Queue Configuration: Jenkins queue with DLQ support
```

### **🏗️ Jenkins (LiftOffStage)**
```yaml
# CI/CD monitoring and integration
Internal Access:
  Host: jenkins-liftoff.liftoffstage.svc.cluster.local
  Port: 8080
  API: /api/json

External Access:
  URL: http://<worker-node-ip>:32013
  API: http://<worker-node-ip>:32013/api/json
  Web UI: http://<worker-node-ip>:32013

Usage:
  - API Application: Build monitoring, status tracking
  - Integration: REST API for build data retrieval
  - Analytics: Build trends, failure analysis, performance metrics
```

### **🤖 Google Gemini AI**
```yaml
# AI service integration via secure VPN
API Endpoint:
  Base URL: https://generativelanguage.googleapis.com
  Model: gemini-2.5-pro
  Authentication: API key (vault-encrypted)

VPN Routing:
  Domain: generativelanguage.googleapis.com
  Tunnel: Secure OpenVPN connection
  Namespace: vpnspace (isolated)

Usage:
  - Diploma Application: Intelligent analysis, conversation memory
  - Features: Anomaly detection, pattern recognition, natural language processing
  - Security: All traffic routed through encrypted VPN tunnel
```

## 💾 **Resource Architecture**

### **🔍 API Application Resource Profile**
```yaml
# Jenkins monitoring application
Resource Allocation:
  Memory Request: 2Gi (guaranteed)
  Memory Limit: 4Gi (maximum)
  CPU Request: 1000m (1 core guaranteed)
  CPU Limit: 3000m (3 cores maximum)

Storage:
  Type: External PostgreSQL
  Data: Build analytics, monitoring results
  Persistence: Database-backed (no local storage)

Network:
  Internal Port: 8383
  External Port: 32016 (NodePort)
  Health Checks: /actuator/health, /actuator/info
```

### **🤖 Diploma Application Resource Profile**
```yaml
# AI-powered analysis application
Resource Allocation:
  Memory Request: 4Gi (guaranteed)
  Memory Limit: 6Gi (maximum)
  CPU Request: 2000m (2 cores guaranteed)
  CPU Limit: 4000m (4 cores maximum)

Storage:
  Type: External PostgreSQL + RabbitMQ
  Data: AI analysis results, conversation memory
  Persistence: Database and queue-backed

Network:
  Internal Port: 8282
  External Port: 32017 (NodePort)
  Health Checks: /actuator/health, /api/status

AI Integration:
  Provider: Google Gemini AI
  Model: gemini-2.5-pro
  Security: VPN-tunneled access
```

### **📊 Performance Considerations**
- **Horizontal Scaling**: Both applications support multiple replicas
- **Resource Monitoring**: Kubernetes metrics and Spring Boot Actuator
- **Load Balancing**: Kubernetes service load balancing
- **Auto-scaling**: HPA support for dynamic resource adjustment

## 🔒 **Security Architecture**

### **🔐 Secrets Management**
```yaml
# Kubernetes-native secret storage
API Application Secrets:
  - Jenkins credentials (username/password)
  - Database credentials (username/password)
  - Service authentication tokens

Diploma Application Secrets:
  - Google Gemini AI API key
  - Database credentials (username/password)
  - RabbitMQ credentials (username/password)

Security Features:
  - Base64 encoded storage
  - Kubernetes RBAC protection
  - Namespace isolation
  - Automatic secret mounting
```

### **🌐 Network Security**
```yaml
# Multi-layer network protection
VPN Security:
  - Dedicated network namespace (vpnspace)
  - Encrypted OpenVPN tunnels
  - Domain-specific routing
  - Traffic isolation

Kubernetes Security:
  - Namespace isolation (secondstage)
  - Service-to-service communication
  - Internal DNS resolution
  - Network policies support

Application Security:
  - Spring Security framework
  - Input validation and sanitization
  - CORS configuration
  - Health check endpoints
```

### **🔑 Access Control**
```yaml
# Authentication and authorization
Kubernetes RBAC:
  - Service account isolation
  - Role-based permissions
  - Resource access control
  - Namespace boundaries

Application Security:
  - API authentication
  - Database connection security
  - Message queue authentication
  - External service credentials
```

## 🚨 **Troubleshooting Guide**

### **Common Issues & Solutions**

#### **🔴 Application Startup Issues**
```bash
# Check pod status and events
kubectl get pods -n secondstage
kubectl describe pod -n secondstage <pod-name>

# View application logs
kubectl logs -n secondstage deployment/secondstage-api --tail=100
kubectl logs -n secondstage deployment/secondstage-diploma --tail=100

# Check resource constraints
kubectl top pods -n secondstage
kubectl describe nodes
```

#### **🔴 AI API Connection Issues**
```bash
# Test VPN status
sudo /usr/local/bin/vpn-setup.sh status

# Check domain routing
sudo /usr/local/bin/domain-route.sh status

# Test AI API connectivity through VPN
sudo ip netns exec vpnspace curl -v https://generativelanguage.googleapis.com

# Verify API key configuration
kubectl get secret -n secondstage secondstage-secrets -o yaml
```

#### **🔴 External Service Connectivity**
```bash
# Test PostgreSQL connectivity
kubectl exec -it -n secondstage deployment/secondstage-api -- curl -v telnet://postgres-ci.liftoffstage.svc.cluster.local:5432

# Test RabbitMQ connectivity
kubectl exec -it -n secondstage deployment/secondstage-diploma -- curl -v telnet://rabbitmq.liftoffstage.svc.cluster.local:5672

# Test Jenkins connectivity
kubectl exec -it -n secondstage deployment/secondstage-api -- curl -v http://jenkins-liftoff.liftoffstage.svc.cluster.local:8080

# Check service discovery
kubectl get svc -n liftoffstage
nslookup postgres-ci.liftoffstage.svc.cluster.local
```

#### **🔴 VPN Configuration Issues**
```bash
# Check VPN configuration files
ls -la /etc/openvpn/
cat /etc/openvpn/"Canada, Quebec S2.ovpn"

# Verify VPN scripts
ls -la /usr/local/bin/vpn-setup.sh
ls -la /usr/local/bin/domain-route.sh

# Test VPN namespace
sudo ip netns list
sudo ip netns exec vpnspace ip addr show
```

#### **🔴 Resource and Performance Issues**
```bash
# Monitor resource usage
kubectl top pods -n secondstage
kubectl top nodes

# Check application performance
curl http://<worker-node-ip>:32016/actuator/metrics
curl http://<worker-node-ip>:32017/actuator/metrics

# Database performance
kubectl exec -it -n liftoffstage deployment/postgres-ci -- psql -U postgres -c "SELECT * FROM pg_stat_activity;"
```

### **🔧 VPN Management Operations**

#### **Manual VPN Control**
```bash
# Complete VPN status check
sudo /usr/local/bin/vpn-setup.sh status

# Start VPN connection manually
sudo /usr/local/bin/vpn-setup.sh start-vpn -c "/etc/openvpn/Canada, Quebec S2.ovpn"

# Stop VPN connection
sudo /usr/local/bin/vpn-setup.sh cleanup

# Restart VPN with different server
sudo /usr/local/bin/vpn-setup.sh start-vpn -c "/etc/openvpn/US, New York S1.ovpn"
```

#### **Domain Routing Management**
```bash
# Check domain routing status
sudo /usr/local/bin/domain-route.sh status

# Setup domain routing manually
sudo /usr/local/bin/domain-route.sh setup -d "generativelanguage.googleapis.com"

# Remove domain routing
sudo /usr/local/bin/domain-route.sh cleanup
```

#### **VPN Connectivity Testing**
```bash
# Test external IP through VPN
sudo ip netns exec vpnspace curl ifconfig.me

# Test DNS resolution in VPN namespace
sudo ip netns exec vpnspace nslookup generativelanguage.googleapis.com

# Test AI API endpoint
sudo ip netns exec vpnspace curl -v https://generativelanguage.googleapis.com/v1/models
```

### **📊 Diagnostic Commands**
```bash
# Complete cluster status
kubectl get all -n secondstage

# Resource usage monitoring
kubectl top pods -n secondstage
kubectl top nodes

# Network connectivity testing
kubectl exec -it -n secondstage deployment/secondstage-api -- netstat -tlnp
kubectl exec -it -n secondstage deployment/secondstage-diploma -- ss -tlnp

# Application health checks
curl http://<worker-node-ip>:32016/actuator/health
curl http://<worker-node-ip>:32017/actuator/health
curl http://<worker-node-ip>:32016/api/dashboard/jobs
```

## 🔗 **Integration with DevSecOps Pipeline**

### **Role Dependencies**
```yaml
# Execution order in main playbook:
1. common                    # Basic system setup
2. containerd               # Container runtime
3. kubernetes               # Kubernetes cluster
4. worker                   # Worker node configuration
5. liftoffstage            # External services (PostgreSQL, RabbitMQ, Jenkins)
6. firststage-prep         # Application preparation (optional)
7. firststage              # Secret detection (optional)
8. secondstage             # THIS ROLE - AI monitoring and analysis
```

### **Service Communication Flow**
```yaml
# SecondStage integrates with:
LiftOffStage Services:
  PostgreSQL CI → Database for both applications
  RabbitMQ → Message queue for Diploma application
  Jenkins → CI/CD monitoring for API application

FirstStage (Optional):
  Secret Detection → Analysis input for Diploma application
  Scan Results → AI-powered analysis and insights

External AI Services:
  Google Gemini AI → Intelligent analysis and conversation memory
  VPN-Secured Access → Encrypted tunnel for AI API calls
```

### **Data Flow Architecture**
```yaml
# Information processing pipeline:
Jenkins Builds → API Application → Analytics Dashboard
    ↓
RabbitMQ Queue → Diploma Application → AI Analysis
    ↓
Google Gemini AI ← VPN Tunnel ← Diploma Application
    ↓
Analysis Results → PostgreSQL → Persistent Storage
    ↓
Dashboard Display ← API Application ← Web Interface
```

## 📊 **Performance & Monitoring**

### **Application Metrics**
```bash
# API application performance
curl http://<worker-node-ip>:32016/actuator/metrics/jvm.memory.used
curl http://<worker-node-ip>:32016/actuator/metrics/http.server.requests

# Diploma application performance
curl http://<worker-node-ip>:32017/actuator/metrics/jvm.memory.used
curl http://<worker-node-ip>:32017/actuator/metrics/ai.requests.total

# System resource monitoring
kubectl top pods -n secondstage --containers
kubectl describe nodes
```

### **Scaling Considerations**
- **Horizontal Scaling**: Multiple replicas for high availability
- **Resource Scaling**: Dynamic CPU and memory adjustment
- **AI API Limits**: Google Gemini API rate limiting considerations
- **Database Scaling**: PostgreSQL connection pooling and optimization

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

**🎉 Thank you for using the SecondStage AI Monitoring & Analysis Platform!**

This role provides enterprise-grade AI-powered monitoring and analysis capabilities with comprehensive security, VPN integration, and seamless service connectivity. For advanced configurations and enterprise support, please contact the DevSecOps Platform team.
