# ThirdStage Ansible Role

This Ansible role deploys the **ThirdStage DevSecOps AI Dashboard Frontend** to a Kubernetes cluster, providing a comprehensive web-based interface for real-time monitoring, analysis, and visualization of the entire DevSecOps platform. It serves as the user-facing component that aggregates data from all platform stages.

## 🎯 **Overview**

The `thirdstage` role is the frontend visualization layer of the DevSecOps platform that:

- **React Dashboard**: Modern React 19.1.0 + TypeScript frontend with real-time data visualization
- **AI Integration**: Displays AI-powered insights and anomaly detection from SecondStage
- **Multi-Stage Monitoring**: Aggregates data from LiftOffStage, FirstStage, and SecondStage services
- **Real-Time Updates**: 30-second auto-refresh with live build monitoring and security insights
- **Production-Ready**: Optimized builds with comprehensive health checks and resource management
- **Kubernetes Native**: Full containerized deployment with persistent configurations

## 🏗️ **Architecture & Integration**

### **Role in DevSecOps Pipeline**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   LiftOffStage  │    │   FirstStage    │    │   SecondStage   │    │   ThirdStage    │
│ (External Svcs) │ -> │ (Secret Scan)   │ -> │ (AI Analysis)   │ -> │ (Dashboard UI)  │
│                 │    │                 │    │                 │    │                 │
│ • Jenkins       │    │ • Spring Boot   │    │ • API Service   │    │ • React Frontend│
│ • RabbitMQ      │    │ • PostgreSQL    │    │ • AI Integration│    │ • Data Viz      │
│ • PostgreSQL CI │    │ • Docker API    │    │ • VPN Security  │    │ • User Interface│
└─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
```

### **Dashboard Features**
```
┌─────────────────────────────────────────────────────────────┐
│                    ThirdStage Dashboard                     │
├─────────────────────────────┬───────────────────────────────┤
│     Real-Time Monitoring    │      AI-Powered Insights      │
│                             │                               │
│ • Jenkins Build Status      │ • Security Anomaly Detection │
│ • Build Success/Failure     │ • AI Analysis Results        │
│ • Pipeline Analytics        │ • Risk Scoring & Trends      │
│ • 30-Second Auto-Refresh    │ • Conversation Memory        │
├─────────────────────────────┼───────────────────────────────┤
│    Interactive Features     │     Responsive Design         │
│                             │                               │
│ • Dark/Light Theme Toggle   │ • Mobile-Responsive Layout   │
│ • Interactive Charts        │ • Progressive Web App        │
│ • Build Log Analysis        │ • Cross-Browser Support      │
│ • Trend Visualization       │ • Accessibility Features     │
└─────────────────────────────┴───────────────────────────────┘
```

## 📋 **Prerequisites**

### **Infrastructure Requirements**
- **Kubernetes Cluster**: Version 1.20+ with service discovery
- **Node Resources**: Minimum 1 CPU core, 2GB RAM on target worker node
- **Network**: Internal cluster networking for SecondStage API connectivity
- **Storage**: Minimal storage requirements for application files

### **Dependencies**
- **SecondStage API**: Must be deployed and accessible for dashboard data
- **Node.js 20**: Automatically installed on target node during deployment
- **BuildKit**: For Docker image building capabilities
- **Docker Registry**: Access to push built images

### **Required Services**
- **SecondStage API**: Provides dashboard data and AI insights
- **LiftOffStage Services**: Jenkins, RabbitMQ, PostgreSQL for comprehensive monitoring
- **FirstStage (Optional)**: Secret detection results for security dashboard

### **Application Files** (ThirdStage.zip)
- **React Application**: Complete frontend application with TypeScript
- **Vite Configuration**: Modern build system with hot module replacement
- **Dashboard Components**: Pre-built UI components for monitoring and visualization
- **Styling & Assets**: Comprehensive styling with dark/light theme support

## ⚙️ **Configuration Variables**

All variables are defined in `defaults/main.yml` and can be overridden via Ansible Vault, group_vars, or playbook variables.

### **🌐 Core Configuration**

| Variable | Default | Description |
|----------|---------|-------------|
| `ns_thirdstage` | `thirdstage` | Kubernetes namespace for ThirdStage deployment |
| `buildkit_install_dir` | `/usr/local/bin` | BuildKit installation directory |
| `node_env` | `production` | Node.js environment (production/development) |

### **🔗 API Integration**

| Variable | Default | Description |
|----------|---------|-------------|
| `secondstage_target_node` | `secondstage` | SecondStage target node for API connectivity |
| `api_base_url` | `http://{{ hostvars[secondstage_target_node].ansible_host }}:32016/api/dashboard` | SecondStage API endpoint URL |

**API Configuration Notes:**
- **Internal Cluster Access**: Uses dynamic IP resolution to SecondStage node
- **External Access Alternative**: Can be overridden for external API endpoints
- **Auto-Discovery**: Automatically resolves SecondStage API location

### **🐳 Docker Image Configuration**

| Variable | Default | Description |
|----------|---------|-------------|
| `thirdstage_image_name` | `thirdstage` | Docker image name |
| `thirdstage_image_tag` | `app` | Docker image tag |
| `thirdstage_image_repo` | `docker.io/khasanabdurakhmanov/innopolis` | Docker registry repository |
| `thirdstage_target_node` | `worker` | Target Kubernetes worker node |

### **🌐 Service Configuration**

| Variable | Default | Description |
|----------|---------|-------------|
| `nodeport` | `32018` | External NodePort for dashboard access |

**Service Notes:**
- **NodePort Only**: Simplified service configuration for consistent external access
- **Single Port**: Dashboard runs on port 3000 internally, exposed via NodePort
- **HMR Support**: Hot Module Replacement port (24678) available for development

### **💾 Resource Configuration**

| Variable | Default | Description |
|----------|---------|-------------|
| `cpu_request` | `200m` | Guaranteed CPU allocation (0.2 cores) |
| `cpu_limit` | `512m` | Maximum CPU limit (0.5 cores) |
| `memory_request` | `512Mi` | Guaranteed memory allocation |
| `memory_limit` | `1Gi` | Maximum memory limit |

### **🏥 Health Check Configuration**

| Variable | Default | Description |
|----------|---------|-------------|
| `readiness_initial_delay` | `30` | Readiness probe initial delay (seconds) |
| `readiness_period` | `10` | Readiness probe check interval (seconds) |
| `liveness_initial_delay` | `60` | Liveness probe initial delay (seconds) |
| `liveness_period` | `30` | Liveness probe check interval (seconds) |

### **📁 Application Paths** (vars/main.yml)

| Variable | Default | Description |
|----------|---------|-------------|
| `thirdstage_app_path` | `/opt/thirdstage/app-files` | Application files directory |
| `thirdstage_build_context` | `/opt/thirdstage/app-files/dashboard-frontend` | Docker build context path |

## 🚀 **Deployment Process**

The ThirdStage role executes a comprehensive 8-phase deployment process:

### **Phase 1: System Preparation**
```yaml
# Package management and system setup
1. Handles APT lock management safely
2. Installs Python prerequisites for Kubernetes management
3. Installs curl, wget, gnupg, and software-properties-common
4. Prepares system for Node.js and Docker operations
```

### **Phase 2: Kubernetes Environment Setup**
```yaml
# Namespace and registry preparation
1. Creates dedicated 'thirdstage' namespace
2. Reads Docker configuration from target machine
3. Creates image pull secret for private registry access
4. Prepares Kubernetes environment for deployment
```

### **Phase 3: Application File Management**
```yaml
# File copying and extraction
1. Creates application directory on target node
2. Copies ThirdStage.zip to target node
3. Extracts dashboard frontend files with proper permissions
4. Cleans up temporary files
5. Verifies all required files are present (Dockerfile, package.json, src/, public/)
```

### **Phase 4: Node.js Runtime Setup**
```yaml
# Node.js 20 installation and verification
1. Adds NodeSource GPG key for package verification
2. Adds NodeSource repository for Node.js 20
3. Installs Node.js 20 with npm package manager
4. Verifies Node.js installation and displays version
```

### **Phase 5: Docker Image Building**
```yaml
# Custom React application image creation
1. Uses BuildKit for advanced Docker image building
2. Builds from dashboard-frontend context
3. Creates optimized production build with Vite
4. Tags and pushes image to registry
5. Validates successful build and extracts image digest
```

### **Phase 6: Kubernetes Configuration**
```yaml
# Application configuration and secrets
1. Creates ConfigMap with environment variables
2. Configures NODE_ENV and VITE_API_BASE_URL
3. Sets up dashboard application configuration
4. Prepares environment for React application
```

### **Phase 7: Dashboard Application Deployment**
```yaml
# React frontend deployment
1. Deploys dashboard application with custom image
2. Configures resource limits and health checks
3. Sets up node affinity to target worker node
4. Creates comprehensive readiness and liveness probes
5. Applies proper container security and networking
```

### **Phase 8: Service Exposure & Validation**
```yaml
# Network access and health verification
1. Creates NodePort service for external access
2. Exposes dashboard on port 3000 (NodePort 32018)
3. Includes HMR port for development support
4. Waits for deployment readiness (5 minutes timeout)
5. Validates external accessibility and health
6. Creates deployment summary ConfigMap
```

## 📖 **Usage Examples**

### **Basic Production Deployment**
```yaml
---
- name: Deploy ThirdStage DevSecOps Dashboard
  hosts: master
  become: yes
  roles:
    - role: thirdstage
  vars:
    # Core configuration
    ns_thirdstage: "thirdstage-prod"
    thirdstage_target_node: "dashboard-worker"

    # Production resource allocation
    cpu_limit: "1000m"
    memory_limit: "2Gi"

    # Custom API endpoint (if SecondStage on different cluster)
    api_base_url: "https://secondstage-api.company.com/api/dashboard"
```

### **Development Environment**
```yaml
---
- name: Deploy ThirdStage for Development
  hosts: master
  become: yes
  roles:
    - role: thirdstage
  vars:
    # Development configuration
    ns_thirdstage: "thirdstage-dev"
    node_env: "development"

    # Reduced resources for development
    cpu_request: "100m"
    cpu_limit: "500m"
    memory_request: "256Mi"
    memory_limit: "512Mi"

    # Development API endpoint
    api_base_url: "http://{{ hostvars['dev-worker'].ansible_host }}:32016/api/dashboard"
    nodeport: 32019  # Different port for dev environment
```

### **High-Performance Configuration**
```yaml
---
- name: Deploy ThirdStage for High-Traffic Environment
  hosts: master
  become: yes
  roles:
    - role: thirdstage
  vars:
    # High-performance configuration
    ns_thirdstage: "thirdstage-prod"

    # Enhanced resource allocation
    cpu_request: "500m"
    cpu_limit: "2000m"
    memory_request: "1Gi"
    memory_limit: "4Gi"

    # Optimized health checks
    readiness_initial_delay: 15
    readiness_period: 5
    liveness_initial_delay: 30
    liveness_period: 15

    # Custom image repository
    thirdstage_image_repo: "{{ vault_docker_registry }}/thirdstage"
```

### **Ansible Vault Integration**
```yaml
---
- name: Deploy ThirdStage with Encrypted Configuration
  hosts: master
  become: yes
  vars_files:
    - "{{ inventory_dir }}/vault/secrets.yml"
  roles:
    - role: thirdstage
  vars:
    # Use vault-encrypted configurations
    ns_thirdstage: "{{ vault_thirdstage_namespace }}"
    api_base_url: "{{ vault_thirdstage_api_url }}"
    thirdstage_image_repo: "{{ vault_docker_registry }}/thirdstage"
    nodeport: "{{ vault_thirdstage_nodeport }}"

    # Custom target node
    thirdstage_target_node: "{{ vault_thirdstage_node }}"
```

## 🌐 **Service Access & Endpoints**

After successful deployment, the ThirdStage dashboard provides comprehensive access points:

### **🖥️ ThirdStage DevSecOps Dashboard**
```yaml
# External Access (NodePort)
URL: http://<worker-node-ip>:32018
Health Check: http://<worker-node-ip>:32018/
Dashboard Features: Real-time monitoring, AI insights, build analytics

# Internal Cluster Access
Host: thirdstage-service.thirdstage.svc.cluster.local
Port: 3000
Health Endpoint: /
Development HMR: 24678 (development only)
```

### **📊 Dashboard Features**
- **Real-Time Monitoring**: 30-second auto-refresh with live build status
- **AI-Powered Insights**: Security anomaly detection and intelligent analysis
- **Interactive Charts**: Build trends, success rates, and performance metrics
- **Build Log Analysis**: Detailed log analysis with risk scoring
- **Theme Support**: Dark/Light theme toggle with user preference persistence
- **Mobile-Responsive**: Progressive web app design for all devices

### **🔗 API Backend Integration**

#### **SecondStage API Connectivity**
```yaml
# Default Internal Connection
API Endpoint: http://<secondstage-node-ip>:32016/api/dashboard
Data Sources: Jenkins builds, AI analysis, anomaly detection
Update Frequency: Real-time with 30-second refresh cycles

# Alternative External Connection
API Endpoint: Custom configurable via api_base_url variable
Authentication: None (internal cluster communication)
Data Format: JSON REST API responses
```

#### **Data Integration Flow**
```yaml
# Information aggregation pipeline:
Jenkins Builds → SecondStage API → ThirdStage Dashboard
    ↓
AI Analysis Results → Real-time Visualization
    ↓
Security Insights → Interactive Charts & Alerts
    ↓
User Interface → Responsive Dashboard Display
```

## 🐳 **Container Architecture**

### **React Application Container**
```yaml
# Production-optimized container
Base Image: node:20-alpine
Build System: Vite (modern, fast builds)
Runtime: Node.js 20 with npm
Port: 3000 (internal), 32018 (external NodePort)
Environment: Production-optimized with minification

# Container Features:
- Multi-stage Docker build for size optimization
- Production-ready Vite configuration
- Optimized asset serving and caching
- Health check endpoints for Kubernetes
```

### **Build Optimization**
```yaml
# Production build features:
Asset Optimization:
  - JavaScript minification and tree-shaking
  - CSS optimization and purging
  - Image optimization and compression
  - Bundle splitting for efficient loading

Performance Features:
  - Code splitting for faster initial loads
  - Lazy loading for non-critical components
  - Service worker for offline capability
  - Progressive web app features
```

## 🔧 **Production Optimizations**

### **Performance Enhancements**
- **Eliminated File Watching**: Removed CHOKIDAR_USEPOLLING to reduce CPU overhead by 50-60%
- **Optimized Builds**: Vite-powered builds with advanced optimization and minification
- **Resource Management**: Configurable CPU/memory limits for production workloads
- **Health Monitoring**: Comprehensive readiness and liveness probes with configurable intervals

### **Deployment Optimizations**
- **NodePort Service**: Simplified service configuration for consistent external access
- **Single Port Exposure**: Dashboard on port 3000, HMR on 24678 (development only)
- **Node Affinity**: Ensures consistent placement on target worker node
- **Image Pull Secrets**: Secure access to private Docker registries

### **Application Optimizations**
- **Environment-Specific Builds**: Production vs development configurations
- **API Integration**: Dynamic SecondStage API discovery and connectivity
- **Error Handling**: Comprehensive error boundaries and fallback UI
- **Accessibility**: WCAG-compliant design with keyboard navigation support

## 🚨 **Troubleshooting Guide**

### **Common Issues & Solutions**

#### **🔴 Application Build Issues**
```bash
# Check Node.js installation
kubectl exec -it -n thirdstage deployment/thirdstage-dashboard -- node --version

# Verify application files
ls -la /opt/thirdstage/app-files/dashboard-frontend/
ls -la /opt/thirdstage/app-files/dashboard-frontend/package.json

# Check Docker build logs
kubectl logs -n thirdstage deployment/thirdstage-dashboard --previous
```

#### **🔴 API Connectivity Issues**
```bash
# Test SecondStage API connectivity
curl -v http://<secondstage-node-ip>:32016/api/dashboard

# Check API configuration in ConfigMap
kubectl get configmap -n thirdstage thirdstage-config -o yaml

# Test from within dashboard pod
kubectl exec -it -n thirdstage deployment/thirdstage-dashboard -- curl -v $VITE_API_BASE_URL
```

#### **🔴 Service Access Issues**
```bash
# Check service status
kubectl get svc -n thirdstage thirdstage-service
kubectl describe svc -n thirdstage thirdstage-service

# Test external access
curl -v http://<worker-node-ip>:32018/
telnet <worker-node-ip> 32018

# Check NodePort availability
netstat -tlnp | grep 32018
```

#### **🔴 Resource and Performance Issues**
```bash
# Monitor resource usage
kubectl top pods -n thirdstage
kubectl describe pod -n thirdstage <pod-name>

# Check resource limits
kubectl describe deployment -n thirdstage thirdstage-dashboard

# View application performance
curl http://<worker-node-ip>:32018/ -w "@curl-format.txt"
```

### **🔧 Diagnostic Commands**
```bash
# Complete cluster status
kubectl get all -n thirdstage

# Pod logs and events
kubectl logs -n thirdstage deployment/thirdstage-dashboard --tail=100
kubectl describe pod -n thirdstage <pod-name>

# Network connectivity testing
kubectl exec -it -n thirdstage deployment/thirdstage-dashboard -- netstat -tlnp
kubectl exec -it -n thirdstage deployment/thirdstage-dashboard -- ss -tlnp

# Application health checks
curl http://<worker-node-ip>:32018/
curl -I http://<worker-node-ip>:32018/
```

### **🔄 Common Fixes**
```bash
# Restart deployment
kubectl rollout restart deployment/thirdstage-dashboard -n thirdstage

# Force pod recreation
kubectl delete pod -n thirdstage -l app=thirdstage-dashboard

# Update ConfigMap and restart
kubectl patch configmap thirdstage-config -n thirdstage --patch '{"data":{"VITE_API_BASE_URL":"new-url"}}'
kubectl rollout restart deployment/thirdstage-dashboard -n thirdstage
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
8. secondstage             # AI monitoring and analysis
9. thirdstage              # THIS ROLE - Dashboard frontend
```

### **Service Communication Flow**
```yaml
# ThirdStage integrates with:
SecondStage API:
  - Real-time dashboard data
  - Jenkins build monitoring
  - AI analysis results
  - Security insights and anomaly detection

LiftOffStage Services (via SecondStage):
  - Jenkins build status and analytics
  - RabbitMQ message processing metrics
  - PostgreSQL CI data and trends

FirstStage Results (via SecondStage):
  - Secret detection scan results
  - Security vulnerability reports
  - Risk scoring and trend analysis
```

### **Data Visualization Pipeline**
```yaml
# Information flow to dashboard:
Raw Data Sources → SecondStage API → ThirdStage Dashboard
    ↓
Jenkins Builds → Build Analytics → Interactive Charts
RabbitMQ Metrics → Message Processing → Real-time Graphs
Security Scans → Risk Analysis → Security Dashboard
AI Analysis → Insights & Trends → Intelligent Alerts
```

## 🔒 **Security Architecture**

### **Frontend Security**
```yaml
# Client-side security features:
Content Security Policy:
  - Strict CSP headers for XSS protection
  - Trusted source validation
  - Inline script restrictions

Data Protection:
  - No sensitive data stored in frontend
  - API tokens handled securely
  - Environment variables properly scoped

Network Security:
  - HTTPS recommended for production
  - Secure API communication
  - CORS configuration for API access
```

### **Kubernetes Security**
```yaml
# Container and cluster security:
Pod Security:
  - Non-root container execution
  - Read-only root filesystem where possible
  - Resource limits and requests

Network Security:
  - Namespace isolation
  - Service-to-service communication
  - Internal DNS resolution

Configuration Security:
  - ConfigMap for non-sensitive data
  - Kubernetes Secrets for sensitive information
  - RBAC for service account permissions
```

## 📊 **Performance & Monitoring**

### **Application Metrics**
```bash
# Dashboard performance monitoring
curl http://<worker-node-ip>:32018/ -w "Total time: %{time_total}s\n"

# Resource usage monitoring
kubectl top pods -n thirdstage --containers
kubectl describe nodes

# Network performance
kubectl exec -it -n thirdstage deployment/thirdstage-dashboard -- netstat -i
```

### **Scaling Considerations**
- **Horizontal Scaling**: Multiple dashboard replicas for high availability
- **Resource Scaling**: Dynamic CPU and memory adjustment based on usage
- **CDN Integration**: Static asset delivery optimization
- **Load Balancing**: Kubernetes service load balancing for multiple replicas

## 📄 **License & Support**

### **License Information**
- **License**: MIT License
- **Author**: Khasan Abdurakhmanov
- **Organization**: Innopolis University DevSecOps Platform
- **Version**: 3.0.0
- **Last Updated**: 2025

### **Support Resources**
- **Documentation**: Comprehensive README with examples and troubleshooting
- **Community Support**: DevSecOps Platform community forums
- **Issue Tracking**: GitHub Issues for bug reports and feature requests
- **Professional Support**: Enterprise support available through Innopolis University

---

**🎉 Thank you for using the ThirdStage DevSecOps Dashboard!**

This role provides enterprise-grade dashboard visualization with comprehensive monitoring, real-time analytics, and seamless integration with the complete DevSecOps platform. For advanced configurations and enterprise support, please contact the DevSecOps Platform team.
