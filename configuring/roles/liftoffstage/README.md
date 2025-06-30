# LiftOffStage Ansible Role

This Ansible role deploys the complete **LiftOffStage Infrastructure Services Platform** to a Kubernetes cluster, providing essential external services for the DevSecOps pipeline including PostgreSQL CI database, RabbitMQ message broker, and Jenkins CI/CD server.

## 🎯 **Overview**

The `liftoffstage` role is a critical component of the multi-stage DevSecOps platform that:

- **Converts Docker Compose to Kubernetes**: Transforms the original Docker Compose services into production-ready Kubernetes deployments
- **Provides External Services**: Supplies foundational infrastructure services for FirstStage, SecondStage, and ThirdStage applications
- **Ensures High Availability**: Implements persistent storage, health checks, and resource management
- **Enables Service Discovery**: Creates internal cluster networking for seamless inter-service communication

## 🏗️ **Architecture Components**

### 🐘 **PostgreSQL CI Database**
- **Custom Image**: PostgreSQL 17.5 with pg_cron extension pre-installed
- **Purpose**: Chat memory storage, build metadata, and audit trails
- **Features**:
  - Custom Docker image built from `postgres_ci/Dockerfile`
  - Automated initialization with `init.sql` schema
  - Persistent storage with local storage class
  - High-performance resource allocation (2Gi-4Gi memory)
  - Comprehensive health checks with `pg_isready`
  - NodePort external access (30432)

### 🐰 **RabbitMQ Message Broker**
- **Image**: `rabbitmq:management-alpine`
- **Purpose**: Message queuing for Jenkins build logs and inter-service communication
- **Features**:
  - Management UI for monitoring and administration
  - Persistent message storage for durability
  - Dual NodePort access (AMQP: 32011, Management: 32012)
  - Configurable authentication and authorization
  - Resource-optimized deployment (1Gi-2Gi memory)
  - Health monitoring with RabbitMQ diagnostics

### 🏗️ **Jenkins CI/CD Server**
- **Image**: `jenkins/jenkins:jdk21`
- **Purpose**: Continuous integration and deployment automation
- **Features**:
  - JDK 21 for modern Java application builds
  - Custom initialization scripts via `init.groovy.d`
  - Automated admin user creation and security setup
  - Plugin installation automation
  - Persistent storage for jobs, configurations, and build artifacts (20Gi)
  - High-performance resource allocation (4Gi-8Gi memory)
  - Dual NodePort access (Web: 32013, Agent: 32014)
  - Advanced Java tuning with G1GC

### 🎯 **Enterprise Features**
- **Kubernetes Native**: Full Kubernetes resource management
- **Node Affinity**: Targeted deployment to specific worker nodes
- **Persistent Volumes**: Local storage with retention policies
- **Security Context**: Proper user permissions and security constraints
- **Resource Management**: CPU/memory requests and limits
- **Health Monitoring**: Comprehensive liveness and readiness probes
- **Service Discovery**: Internal cluster DNS and service mesh
- **Configuration Management**: ConfigMaps and service information tracking

## 📋 **Prerequisites**

### **Infrastructure Requirements**
- **Kubernetes Cluster**: Version 1.20+ with local storage support
- **Node Resources**: Minimum 8 CPU cores, 16GB RAM on target worker node
- **Storage**: Local storage class configured and available
- **Network**: NodePort range 30000-32767 accessible

### **Ansible Requirements**
- **Ansible Core**: Version 2.9+ (recommended 2.12+)
- **Collections**:
  - `community.kubernetes` (1.2.0+)
  - `kubernetes.core` (2.3.0+)
  - `community.docker` (2.0.0+)
- **Python Dependencies**:
  - `kubernetes` library (12.0.0+)
  - `openshift` library (0.12.0+)
  - `pyyaml` library (5.4.0+)

### **Build Tools**
- **BuildKit**: For custom PostgreSQL CI image building
- **Docker Registry Access**: For pushing custom images
- **Registry Credentials**: Configured in `/root/.docker/config.json`

## ⚙️ **Configuration Variables**

All variables are defined in `defaults/main.yml` and can be overridden via Ansible Vault, group_vars, or playbook variables.

### **🌐 Global Infrastructure**

| Variable | Default | Description |
|----------|---------|-------------|
| `buildkit_install_dir` | `/usr/local/bin` | BuildKit installation directory for image building |
| `ns_liftoffstage` | `liftoffstage` | Kubernetes namespace for all LiftOffStage services |
| `liftoffstage_target_node` | `worker` | Target Kubernetes worker node for service deployment |

### **🐘 PostgreSQL CI Database**

#### **Database Configuration**
| Variable | Default | Description |
|----------|---------|-------------|
| `postgres_ci_db` | `postgres` | Primary database name |
| `postgres_ci_user` | `postgres` | Database superuser account |
| `postgres_ci_password` | `postgres` | Database password (⚠️ Change in production) |

#### **Docker Image Configuration**
| Variable | Default | Description |
|----------|---------|-------------|
| `postgres_ci_image_repo` | `docker.io/khasanabdurakhmanov/innopolis` | Docker registry repository |
| `postgres_ci_image_name` | `postgres-ci` | Custom image name |
| `postgres_ci_image_tag` | `17.5-cron` | Image tag with pg_cron extension |

#### **Storage & Network**
| Variable | Default | Description |
|----------|---------|-------------|
| `postgres_ci_storage_size` | `10Gi` | Persistent volume size |
| `postgres_ci_host_path` | `/opt/liftoffstage/postgres-ci` | Host path for data persistence |
| `postgres_ci_nodeport` | `30432` | External NodePort for database access |

#### **Resource Allocation (High-Performance)**
| Variable | Default | Description |
|----------|---------|-------------|
| `postgres_ci_memory_request` | `2Gi` | Guaranteed memory allocation |
| `postgres_ci_cpu_request` | `1000m` | Guaranteed CPU allocation (1 core) |
| `postgres_ci_memory_limit` | `4Gi` | Maximum memory limit |
| `postgres_ci_cpu_limit` | `2000m` | Maximum CPU limit (2 cores) |

### **🐰 RabbitMQ Message Broker**

#### **Authentication & Storage**
| Variable | Default | Description |
|----------|---------|-------------|
| `rabbitmq_default_user` | `liftoffstage` | RabbitMQ admin username |
| `rabbitmq_default_pass` | `liftoffstage123` | RabbitMQ admin password (⚠️ Change in production) |
| `rabbitmq_storage_size` | `5Gi` | Persistent volume size for message storage |
| `rabbitmq_host_path` | `/opt/liftoffstage/rabbitmq` | Host path for data persistence |

#### **Network Configuration**
| Variable | Default | Description |
|----------|---------|-------------|
| `rabbitmq_amqp_nodeport` | `32011` | NodePort for AMQP protocol (5672) |
| `rabbitmq_management_nodeport` | `32012` | NodePort for management UI (15672) |

#### **Resource Allocation (Optimized)**
| Variable | Default | Description |
|----------|---------|-------------|
| `rabbitmq_memory_request` | `1Gi` | Guaranteed memory allocation |
| `rabbitmq_cpu_request` | `500m` | Guaranteed CPU allocation (0.5 cores) |
| `rabbitmq_memory_limit` | `2Gi` | Maximum memory limit |
| `rabbitmq_cpu_limit` | `1000m` | Maximum CPU limit (1 core) |

### **🏗️ Jenkins CI/CD Server**

#### **Authentication & Configuration**
| Variable | Default | Description |
|----------|---------|-------------|
| `jenkins_liftoff_admin_user` | `admin` | Jenkins admin username |
| `jenkins_liftoff_admin_password` | `admin123` | Jenkins admin password (⚠️ Change in production) |
| `jenkins_liftoff_url` | `http://{{ hostvars[...] }}:32013` | Dynamic Jenkins URL based on worker node IP |

#### **Java & Performance Tuning**
| Variable | Default | Description |
|----------|---------|-------------|
| `jenkins_liftoff_java_opts` | `-Djenkins.install.runSetupWizard=false -Xms512m -Xmx1536m -XX:+UseG1GC -XX:MaxGCPauseMillis=200` | Optimized JVM settings with G1GC |

#### **Storage & Network**
| Variable | Default | Description |
|----------|---------|-------------|
| `jenkins_liftoff_storage_size` | `20Gi` | Persistent volume size for jobs and artifacts |
| `jenkins_liftoff_host_path` | `/opt/liftoffstage/jenkins` | Host path for data persistence |
| `jenkins_liftoff_web_nodeport` | `32013` | NodePort for web UI access |
| `jenkins_liftoff_agent_nodeport` | `32014` | NodePort for build agent connections |

#### **Resource Allocation (High-Performance)**
| Variable | Default | Description |
|----------|---------|-------------|
| `jenkins_liftoff_memory_request` | `4Gi` | Guaranteed memory allocation |
| `jenkins_liftoff_cpu_request` | `2000m` | Guaranteed CPU allocation (2 cores) |
| `jenkins_liftoff_memory_limit` | `8Gi` | Maximum memory limit |
| `jenkins_liftoff_cpu_limit` | `4000m` | Maximum CPU limit (4 cores) |

## 🔗 **Dependencies & Integration**

### **Role Dependencies**
This role requires the following infrastructure roles to be executed first:

| Role | Purpose | Required |
|------|---------|----------|
| `common` | Basic system setup, package management, security hardening | ✅ **Required** |
| `containerd` | Container runtime installation and configuration | ✅ **Required** |
| `buildkit` | BuildKit for custom image building capabilities | ✅ **Required** |
| `kubernetes` | Kubernetes cluster components and networking | ✅ **Required** |
| `worker` | Worker node configuration (if deploying to worker node) | ✅ **Required** |

### **Collection Dependencies**
```yaml
collections:
  - community.kubernetes  # Kubernetes resource management
  - kubernetes.core       # Core Kubernetes modules
  - community.docker      # Docker operations
```

## 📖 **Usage Examples**

### **Basic Production Deployment**
```yaml
---
- name: Deploy LiftOffStage Infrastructure Services
  hosts: master
  become: yes
  roles:
    - role: liftoffstage
  vars:
    # Global configuration
    ns_liftoffstage: "liftoffstage-prod"
    liftoffstage_target_node: "liftoffstage"  # Dedicated node

    # PostgreSQL CI - Production Settings
    postgres_ci_db: "liftoffstage_prod"
    postgres_ci_user: "liftoffstage_admin"
    postgres_ci_password: "{{ vault_postgres_password }}"
    postgres_ci_storage_size: "50Gi"
    postgres_ci_memory_limit: "8Gi"
    postgres_ci_cpu_limit: "4000m"

    # RabbitMQ - Production Settings
    rabbitmq_default_user: "liftoffstage_admin"
    rabbitmq_default_pass: "{{ vault_rabbitmq_password }}"
    rabbitmq_storage_size: "20Gi"
    rabbitmq_memory_limit: "4Gi"

    # Jenkins - High-Performance Settings
    jenkins_liftoff_admin_user: "admin"
    jenkins_liftoff_admin_password: "{{ vault_jenkins_password }}"
    jenkins_liftoff_storage_size: "100Gi"
    jenkins_liftoff_memory_limit: "16Gi"
    jenkins_liftoff_cpu_limit: "8000m"
    jenkins_liftoff_java_opts: "-Djenkins.install.runSetupWizard=false -Xms2g -Xmx8g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
```

### **Development Environment**
```yaml
---
- name: Deploy LiftOffStage Development Environment
  hosts: master
  become: yes
  roles:
    - role: liftoffstage
  vars:
    # Development configuration with lower resources
    ns_liftoffstage: "liftoffstage-dev"

    # Reduced resource allocation for development
    postgres_ci_memory_request: "512Mi"
    postgres_ci_memory_limit: "1Gi"
    postgres_ci_cpu_request: "250m"
    postgres_ci_cpu_limit: "500m"

    rabbitmq_memory_request: "256Mi"
    rabbitmq_memory_limit: "512Mi"

    jenkins_liftoff_memory_request: "1Gi"
    jenkins_liftoff_memory_limit: "2Gi"
    jenkins_liftoff_java_opts: "-Djenkins.install.runSetupWizard=false -Xmx1024m"
```

### **Multi-Environment with Ansible Vault**
```yaml
---
- name: Deploy LiftOffStage with Vault Secrets
  hosts: master
  become: yes
  vars_files:
    - "{{ inventory_dir }}/vault/secrets.yml"
  roles:
    - role: liftoffstage
  vars:
    # Use vault-encrypted variables
    postgres_ci_password: "{{ vault_postgres_ci_password }}"
    rabbitmq_default_pass: "{{ vault_rabbitmq_password }}"
    jenkins_liftoff_admin_password: "{{ vault_jenkins_admin_password }}"

    # Custom image repository
    postgres_ci_image_repo: "{{ vault_docker_registry }}/liftoffstage"
```

## 🚀 **Deployment Process**

### **Pre-Deployment Checklist**

1. **✅ Infrastructure Readiness**
   ```bash
   # Verify Kubernetes cluster status
   kubectl cluster-info
   kubectl get nodes

   # Check local storage class availability
   kubectl get storageclass

   # Verify target node resources
   kubectl describe node <liftoffstage_target_node>
   ```

2. **✅ Node Preparation**
   ```bash
   # Ensure target node has sufficient resources
   # Minimum: 8 CPU cores, 16GB RAM, 100GB storage

   # Verify BuildKit installation
   ls -la /usr/local/bin/buildctl-daemonless.sh

   # Check Docker registry credentials
   cat /root/.docker/config.json
   ```

3. **✅ Network Configuration**
   ```bash
   # Verify NodePort range availability (30432, 32011-32014)
   netstat -tuln | grep -E "(30432|32011|32012|32013|32014)"
   ```

### **Deployment Execution**

1. **Run the Playbook**
   ```bash
   ansible-playbook -i inventory/production main.yml --tags liftoffstage
   ```

2. **Monitor Deployment Progress**
   ```bash
   # Watch namespace creation and pod startup
   kubectl get all -n liftoffstage -w

   # Check persistent volumes
   kubectl get pv,pvc -n liftoffstage

   # Monitor resource usage
   kubectl top pods -n liftoffstage
   ```

3. **Verify Service Health**
   ```bash
   # Check all deployments are ready
   kubectl get deployments -n liftoffstage

   # Verify service endpoints
   kubectl get services -n liftoffstage

   # Check pod logs for any issues
   kubectl logs -n liftoffstage -l app=postgres-ci
   kubectl logs -n liftoffstage -l app=rabbitmq
   kubectl logs -n liftoffstage -l app=jenkins-liftoff
   ```

## 🌐 **Service Access & Endpoints**

After successful deployment, services are accessible via the following endpoints:

### **🐘 PostgreSQL CI Database**
```yaml
# Internal Cluster Access (from other pods)
Host: postgres-ci.liftoffstage.svc.cluster.local
Port: 5432
Database: postgres  # or as configured
Username: postgres  # or as configured
Password: <configured_password>

# External Access (from outside cluster)
Host: <worker-node-ip>
Port: 30432
Connection String: postgresql://postgres:<password>@<worker-node-ip>:30432/postgres
```

### **🐰 RabbitMQ Message Broker**
```yaml
# AMQP Protocol Access
Host: <worker-node-ip>
Port: 32011
URL: amqp://liftoffstage:<password>@<worker-node-ip>:32011/

# Management UI Access
URL: http://<worker-node-ip>:32012
Username: liftoffstage  # or as configured
Password: liftoffstage123  # or as configured

# Internal Cluster Access
Host: rabbitmq.liftoffstage.svc.cluster.local
AMQP Port: 5672
Management Port: 15672
```

### **🏗️ Jenkins CI/CD Server**
```yaml
# Web UI Access
URL: http://<worker-node-ip>:32013
Username: admin  # or as configured
Password: admin123  # or as configured

# Agent Connection
Host: <worker-node-ip>
Port: 32014
JNLP URL: http://<worker-node-ip>:32013/computer/<agent-name>/slave-agent.jnlp

# Internal Cluster Access
Host: jenkins-liftoff.liftoffstage.svc.cluster.local
Web Port: 8080
Agent Port: 50000
```

### **📋 Service Information ConfigMap**
```bash
# View deployment summary and connection details
kubectl get configmap liftoffstage-services-info -n liftoffstage -o yaml

# Quick access to service URLs
kubectl get configmap liftoffstage-services-info -n liftoffstage -o jsonpath='{.data.services\.txt}'
```

## 🏥 **Health Monitoring & Diagnostics**

The role implements comprehensive health monitoring for all services with multiple probe types:

### **🐘 PostgreSQL CI Health Checks**
```yaml
# Liveness Probe (Container Health)
Command: pg_isready -U postgres -d postgres -h 127.0.0.1
Initial Delay: 60 seconds
Period: 10 seconds
Timeout: 5 seconds
Failure Threshold: 3

# Readiness Probe (Service Availability)
Command: pg_isready -U postgres -d postgres -h 127.0.0.1
Initial Delay: 30 seconds
Period: 5 seconds
Timeout: 3 seconds
Failure Threshold: 3
```

### **🐰 RabbitMQ Health Checks**
```yaml
# Liveness Probe (Process Health)
Command: rabbitmq-diagnostics -q ping
Initial Delay: 60 seconds
Period: 60 seconds
Timeout: 15 seconds
Failure Threshold: 3

# Readiness Probe (Service Readiness)
Command: rabbitmq-diagnostics -q check_running
Initial Delay: 20 seconds
Period: 60 seconds
Timeout: 10 seconds
Failure Threshold: 3
```

### **🏗️ Jenkins Health Checks**
```yaml
# Startup Probe (Initial Boot)
HTTP GET: /login (port 8080)
Initial Delay: 60 seconds
Period: 10 seconds
Timeout: 5 seconds
Failure Threshold: 30

# Liveness Probe (Application Health)
HTTP GET: /login (port 8080)
Initial Delay: 0 seconds
Period: 30 seconds
Timeout: 10 seconds
Failure Threshold: 3

# Readiness Probe (Traffic Readiness)
HTTP GET: /login (port 8080)
Initial Delay: 0 seconds
Period: 10 seconds
Timeout: 5 seconds
Failure Threshold: 3
```

### **📊 Monitoring Commands**
```bash
# Check overall service health
kubectl get pods -n liftoffstage -o wide

# View detailed health status
kubectl describe pods -n liftoffstage

# Monitor resource usage
kubectl top pods -n liftoffstage

# Check service endpoints
kubectl get endpoints -n liftoffstage

# View recent events
kubectl get events -n liftoffstage --sort-by='.lastTimestamp'
```

## 🔒 **Security & Best Practices**

### **Security Features Implemented**
- **🔐 Namespace Isolation**: Complete service isolation within dedicated namespace
- **👤 Security Contexts**: Proper user permissions and non-root execution
- **📦 Resource Limits**: CPU/memory constraints prevent resource exhaustion
- **🛡️ Network Policies**: Service-to-service communication controls
- **🔑 Secret Management**: Sensitive data handled via Kubernetes secrets
- **📋 RBAC Integration**: Role-based access control for service accounts

### **Security Configuration Details**
```yaml
# Jenkins Security Context
securityContext:
  fsGroup: 1000
  runAsUser: 1000
  runAsGroup: 1000
  allowPrivilegeEscalation: false
  readOnlyRootFilesystem: false

# PostgreSQL Security
- Custom image with security patches
- Non-root user execution
- Encrypted connections support

# RabbitMQ Security
- Management UI with authentication
- AMQP protocol security
- Message persistence encryption
```

### **Production Security Recommendations**
1. **🔐 Change Default Passwords**: Update all default credentials
2. **🔑 Use Ansible Vault**: Encrypt sensitive variables
3. **🌐 Network Segmentation**: Implement network policies
4. **📜 TLS Certificates**: Enable SSL/TLS for all services
5. **🔍 Audit Logging**: Enable comprehensive audit trails
6. **🛡️ Image Scanning**: Scan container images for vulnerabilities

## 🔄 **Docker Compose to Kubernetes Migration**

This role provides a complete migration path from Docker Compose to Kubernetes:

### **Migration Mapping**
| **Docker Compose Service** | **Kubernetes Resources** | **Migration Notes** |
|----------------------------|---------------------------|---------------------|
| **`jenkins`** | Deployment + Service + PV + PVC + InitContainer | • Custom init scripts via hostPath mount<br>• JDK 21 with optimized JVM settings<br>• Persistent storage for jobs and plugins<br>• Security context for proper permissions |
| **`rabbitmq`** | Deployment + Service + PV + PVC | • Management UI exposed via NodePort<br>• Persistent message storage<br>• Health checks with RabbitMQ diagnostics<br>• Resource optimization for message throughput |
| **`postgres_ci`** | Deployment + Service + PV + PVC + Custom Image | • Custom PostgreSQL image with pg_cron<br>• BuildKit-based image building<br>• Automated schema initialization<br>• High-performance resource allocation |

### **Enhanced Kubernetes Features**
- **📈 Scalability**: Horizontal pod autoscaling support
- **🔄 Rolling Updates**: Zero-downtime deployments
- **🏥 Health Monitoring**: Advanced probe configurations
- **📊 Resource Management**: Requests and limits for optimal scheduling
- **🌐 Service Discovery**: Internal DNS and service mesh integration
- **💾 Persistent Storage**: Kubernetes-native volume management

## ⚙️ **Advanced Customization**

### **🎛️ Performance Tuning**

#### **PostgreSQL Optimization**
```yaml
# High-Performance Database Configuration
postgres_ci_memory_request: "8Gi"
postgres_ci_memory_limit: "16Gi"
postgres_ci_cpu_request: "4000m"
postgres_ci_cpu_limit: "8000m"
postgres_ci_storage_size: "500Gi"

# Custom PostgreSQL configuration via init scripts
# Add custom postgresql.conf settings in postgres_ci/init.sql
```

#### **RabbitMQ Scaling**
```yaml
# Message Broker Optimization
rabbitmq_memory_request: "2Gi"
rabbitmq_memory_limit: "8Gi"
rabbitmq_cpu_request: "1000m"
rabbitmq_cpu_limit: "4000m"
rabbitmq_storage_size: "100Gi"

# Enable clustering for high availability
# Configure additional RabbitMQ nodes
```

#### **Jenkins High-Performance**
```yaml
# CI/CD Server Optimization
jenkins_liftoff_memory_request: "8Gi"
jenkins_liftoff_memory_limit: "32Gi"
jenkins_liftoff_cpu_request: "4000m"
jenkins_liftoff_cpu_limit: "16000m"
jenkins_liftoff_storage_size: "1Ti"

# Advanced JVM tuning
jenkins_liftoff_java_opts: >-
  -Djenkins.install.runSetupWizard=false
  -Xms4g -Xmx16g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=100
  -XX:+UnlockExperimentalVMOptions
  -XX:+UseCGroupMemoryLimitForHeap
  -XX:+UseStringDeduplication
  -Djava.awt.headless=true
  -Dfile.encoding=UTF-8
```

### **🌐 Network Customization**
```yaml
# Custom NodePort Configuration
postgres_ci_nodeport: 31432        # Custom PostgreSQL port
rabbitmq_amqp_nodeport: 31011      # Custom AMQP port
rabbitmq_management_nodeport: 31012 # Custom management port
jenkins_liftoff_web_nodeport: 31013 # Custom Jenkins web port
jenkins_liftoff_agent_nodeport: 31014 # Custom agent port

# Load Balancer Integration (if available)
# Configure LoadBalancer service type instead of NodePort
```

### **💾 Storage Customization**
```yaml
# Custom Storage Classes
postgres_ci_storage_class: "fast-ssd"
rabbitmq_storage_class: "standard"
jenkins_liftoff_storage_class: "fast-ssd"

# Custom Host Paths
postgres_ci_host_path: "/data/liftoffstage/postgres"
rabbitmq_host_path: "/data/liftoffstage/rabbitmq"
jenkins_liftoff_host_path: "/data/liftoffstage/jenkins"
```

## 📊 **Monitoring & Maintenance**

### **📋 Service Information Management**
The role automatically creates a comprehensive service information ConfigMap:

```yaml
# ConfigMap: liftoffstage-services-info
apiVersion: v1
kind: ConfigMap
metadata:
  name: liftoffstage-services-info
  namespace: liftoffstage
data:
  services.txt: |
    LiftOffStage Services Information
    ================================

    PostgreSQL CI:
    - External URL: <worker-ip>:30432
    - Internal URL: postgres-ci:5432
    - Database: postgres
    - User: postgres

    RabbitMQ:
    - AMQP Port: <worker-ip>:32011
    - Management UI: http://<worker-ip>:32012
    - User: liftoffstage

    Jenkins:
    - Web UI: http://<worker-ip>:32013
    - Agent Port: <worker-ip>:32014
    - Admin User: admin

    Deployment Details:
    - Namespace: liftoffstage
    - Target Node: <worker-node>
    - Deployed: <timestamp>
```

### **🔧 Maintenance Commands**
```bash
# Service Health Check
kubectl get all -n liftoffstage

# Resource Usage Monitoring
kubectl top pods -n liftoffstage
kubectl top nodes

# Log Analysis
kubectl logs -n liftoffstage -l app=postgres-ci --tail=100
kubectl logs -n liftoffstage -l app=rabbitmq --tail=100
kubectl logs -n liftoffstage -l app=jenkins-liftoff --tail=100

# Backup Operations
kubectl exec -n liftoffstage postgres-ci-<pod-id> -- pg_dump -U postgres postgres > backup.sql

# Service Restart
kubectl rollout restart deployment/postgres-ci -n liftoffstage
kubectl rollout restart deployment/rabbitmq -n liftoffstage
kubectl rollout restart deployment/jenkins-liftoff -n liftoffstage
```

### **📈 Performance Monitoring**
```bash
# Resource Utilization
kubectl describe nodes | grep -A 5 "Allocated resources"

# Service Metrics
kubectl get --raw /metrics | grep liftoffstage

# Storage Usage
kubectl get pv,pvc -n liftoffstage
df -h /opt/liftoffstage/
```

## 🚨 **Troubleshooting Guide**

### **Common Issues & Solutions**

#### **🐘 PostgreSQL Issues**
```bash
# Database Connection Issues
kubectl exec -n liftoffstage postgres-ci-<pod> -- pg_isready -U postgres

# Check Database Logs
kubectl logs -n liftoffstage postgres-ci-<pod> --tail=50

# Verify Persistent Volume
kubectl describe pv postgres-ci-pv
```

#### **🐰 RabbitMQ Issues**
```bash
# Check RabbitMQ Status
kubectl exec -n liftoffstage rabbitmq-<pod> -- rabbitmq-diagnostics status

# Management UI Access Issues
kubectl port-forward -n liftoffstage svc/rabbitmq 15672:15672

# Queue Management
kubectl exec -n liftoffstage rabbitmq-<pod> -- rabbitmqctl list_queues
```

#### **🏗️ Jenkins Issues**
```bash
# Jenkins Startup Issues
kubectl logs -n liftoffstage jenkins-liftoff-<pod> --tail=100

# Plugin Installation Problems
kubectl exec -n liftoffstage jenkins-liftoff-<pod> -- ls -la /var/jenkins_home/plugins/

# Permission Issues
kubectl exec -n liftoffstage jenkins-liftoff-<pod> -- ls -la /var/jenkins_home/
```

## 📄 **License & Support**

### **License**
This role is licensed under the **MIT License**. See the LICENSE file for details.

### **Author Information**
- **Author**: Khasan Abdurakhmanov
- **Organization**: DevSecOps Platform Team
- **Contact**: Infrastructure Services Division
- **Version**: 2.0.0
- **Last Updated**: 2025

### **Support & Contributions**
- **Documentation**: Comprehensive README with examples
- **Issue Tracking**: GitHub Issues for bug reports and feature requests
- **Contributions**: Pull requests welcome for improvements
- **Community**: DevSecOps Platform community support

### **Version History**
- **v2.0.0**: Complete Kubernetes migration with enhanced features
- **v1.5.0**: Docker Compose to Kubernetes conversion
- **v1.0.0**: Initial Docker Compose implementation

---

**🎉 Thank you for using the LiftOffStage Infrastructure Services Platform!**

For additional support and advanced configurations, please refer to the DevSecOps Platform documentation or contact the infrastructure team.
