# FirstStage-Prep Ansible Role

This Ansible role prepares the **FirstStage Secret Detection Application** for deployment by copying application files and generating TLS certificates for secure Docker API access. It serves as the essential preparation phase before the main FirstStage deployment.

## 🎯 **Overview**

The `firststage-prep` role is a critical preparation component of the multi-stage DevSecOps platform that:

- **Copies Application Files**: Transfers FirstStage application files to the target worker node
- **Generates TLS Certificates**: Creates secure certificates for Docker API communication with SAST node
- **Validates File Structure**: Ensures all required files and directories are properly deployed
- **Downloads Certificates**: Fetches server certificates to the Ansible control node for distribution
- **Prepares Environment**: Sets up the foundation for FirstStage secret detection deployment

## 🏗️ **Architecture & Purpose**

### **Role in DevSecOps Pipeline**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   LiftOffStage  │    │ FirstStage-Prep │    │   FirstStage    │
│ (External Svcs) │ -> │ (Preparation)   │ -> │ (Secret Scan)   │
│                 │    │                 │    │                 │
│ • Jenkins       │    │ • File Copy     │    │ • Spring Boot   │
│ • RabbitMQ      │    │ • TLS Certs     │    │ • PostgreSQL    │
│ • PostgreSQL CI │    │ • Validation    │    │ • Docker API    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### **Key Responsibilities**
- **📦 Application Deployment**: Copies FirstStage.zip and extracts to `/opt/firststage/app-files/`
- **🔐 Certificate Generation**: Creates TLS certificates for secure Docker daemon communication
- **✅ File Validation**: Verifies all critical files and directories are present
- **📥 Certificate Distribution**: Downloads server certificates for SAST node configuration
- **🛡️ Security Setup**: Establishes secure communication channels for secret detection

## 📋 **Prerequisites**

### **Infrastructure Requirements**
- **Target Worker Node**: Ubuntu 20.04+ with sufficient storage (minimum 10GB free)
- **SAST Node**: Accessible for Docker API communication
- **Network Connectivity**: Worker node can reach SAST node for certificate validation
- **File Permissions**: Ansible user has sudo privileges on target nodes

### **Application Files**
- **FirstStage.zip**: Complete application archive in `files/` directory
- **Required Contents**:
  - `Dockerfile` - Container build instructions
  - `rabbitmq-app.jar` - Spring Boot application
  - `generate_docker_certs.sh` - Certificate generation script
  - `src/main/resources/` - Application resources and configuration
  - `docker-compose.yml` - Development configuration reference

### **Dependencies**
- **OpenSSL**: For TLS certificate generation
- **Unzip**: For application file extraction
- **Proper DNS/IP Resolution**: Between worker and SAST nodes

## ⚙️ **Configuration Variables**

All variables are defined in `defaults/main.yml` and can be overridden via Ansible Vault, group_vars, or playbook variables.

### **🌐 Target Node Configuration**

| Variable | Default | Description |
|----------|---------|-------------|
| `firststage_target_node` | `worker` | Target Kubernetes worker node for FirstStage deployment |
| `sast_target_node` | `sast` | SAST node hostname for Docker API communication |

### **👤 File Permissions**

| Variable | Default | Description |
|----------|---------|-------------|
| `file_owner` | `ubuntu` | Owner for copied application files |
| `file_group` | `ubuntu` | Group for copied application files |

### **🔐 Certificate Configuration**

| Variable | Default | Description |
|----------|---------|-------------|
| `docker_server_hostname` | `docker-host` | Hostname for TLS certificate generation |
| `docker_server_ip` | `{{ hostvars[sast_target_node]['ansible_host'] }}` | Dynamic IP address of SAST node for certificate |
| `force_cert_regeneration` | `false` | Force regeneration of existing certificates |

## 🚀 **Deployment Process**

### **Phase 1: APT Lock Management**
```yaml
# Safe package management preparation
- Waits for existing apt locks to be released
- Checks for dpkg inconsistencies
- Fixes any package management issues
```

### **Phase 2: Application File Deployment**
```yaml
# File copying and extraction
1. Creates /opt/firststage/app-files/ directory
2. Copies FirstStage.zip to target node
3. Extracts application files with proper permissions
4. Validates all critical files are present
```

### **Phase 3: TLS Certificate Generation**
```yaml
# Secure Docker API communication setup
1. Sets certificate generation parameters
2. Makes generate_docker_certs.sh executable
3. Checks for existing certificates
4. Generates new certificates if needed
5. Validates certificate creation
```

### **Phase 4: Certificate Distribution**
```yaml
# Downloads certificates for SAST configuration
1. Fetches server certificates to control node
2. Downloads CA certificate
3. Validates successful download
4. Prepares certificates for SAST deployment
```

## 📖 **Usage Examples**

### **Basic Deployment**
```yaml
---
- name: Prepare FirstStage Application
  hosts: master
  become: yes
  roles:
    - role: firststage-prep
```

### **Production Configuration**
```yaml
---
- name: Prepare FirstStage for Production
  hosts: master
  become: yes
  roles:
    - role: firststage-prep
  vars:
    # Target specific nodes
    firststage_target_node: "firststage-worker"
    sast_target_node: "sast-production"
    
    # Custom permissions
    file_owner: "firststage"
    file_group: "firststage"
    
    # Certificate configuration
    docker_server_hostname: "sast-docker-api"
    force_cert_regeneration: true
```

### **Development Environment**
```yaml
---
- name: Prepare FirstStage for Development
  hosts: master
  become: yes
  roles:
    - role: firststage-prep
  vars:
    # Use same node for both services in dev
    firststage_target_node: "dev-worker"
    sast_target_node: "dev-worker"
    
    # Force certificate regeneration for testing
    force_cert_regeneration: true
```

### **Ansible Vault Integration**
```yaml
---
- name: Prepare FirstStage with Vault Secrets
  hosts: master
  become: yes
  vars_files:
    - "{{ inventory_dir }}/vault/secrets.yml"
  roles:
    - role: firststage-prep
  vars:
    # Use vault-encrypted node configurations
    firststage_target_node: "{{ vault_firststage_node }}"
    sast_target_node: "{{ vault_sast_node }}"
    docker_server_hostname: "{{ vault_docker_hostname }}"
```

## 📁 **File Structure & Validation**

### **Application Files Deployed**
```bash
/opt/firststage/app-files/
├── Dockerfile                    # Container build instructions
├── docker-compose.yml           # Development reference
├── rabbitmq-app.jar             # Spring Boot application
├── generate_docker_certs.sh     # Certificate generation script
├── README.md                    # Application documentation
└── src/
    └── main/
        └── resources/
            ├── cert/             # Client certificates (generated)
            │   ├── ca.pem
            │   ├── cert.pem
            │   └── key.pem
            └── secret-patterns-db/  # Secret detection patterns
```

### **Generated Certificates**
```bash
/opt/firststage/app-files/
├── ca.pem                       # Certificate Authority
├── server_cert/                 # Server certificates
│   ├── server-cert.pem         # Server certificate
│   └── server-key.pem          # Server private key
└── src/main/resources/cert/     # Client certificates
    ├── ca.pem                  # CA certificate
    ├── cert.pem                # Client certificate
    └── key.pem                 # Client private key
```

### **Downloaded to Control Node**
```bash
{{ playbook_dir }}/server_certificates/
├── ca.pem                       # Certificate Authority
├── server-cert.pem             # Server certificate
└── server-key.pem              # Server private key
```

## 🔧 **Certificate Generation Process**

### **TLS Certificate Details**
The role generates a complete PKI infrastructure for secure Docker API communication:

#### **Certificate Authority (CA)**
- **Purpose**: Root certificate for trust chain
- **Location**: `/opt/firststage/app-files/ca.pem`
- **Usage**: Validates both client and server certificates

#### **Server Certificates**
- **Purpose**: Authenticates SAST Docker daemon
- **Files**: `server-cert.pem`, `server-key.pem`
- **Subject**: Uses `docker_server_hostname` and `docker_server_ip`
- **Usage**: Enables TLS on Docker daemon port 2376

#### **Client Certificates**
- **Purpose**: Authenticates FirstStage application to Docker daemon
- **Files**: `cert.pem`, `key.pem` (+ `ca.pem`)
- **Location**: `src/main/resources/cert/`
- **Usage**: Embedded in Spring Boot application for secure API calls

### **Certificate Generation Script**
```bash
# The generate_docker_certs.sh script creates:
export SERVER_HOSTNAME="docker-host"     # From docker_server_hostname
export SERVER_IP="192.168.1.100"        # From docker_server_ip
./generate_docker_certs.sh

# Generates complete PKI:
# 1. CA private key and certificate
# 2. Server private key and certificate
# 3. Client private key and certificate
# 4. Proper file permissions and locations
```

## 🏥 **Validation & Health Checks**

### **File Validation Process**
The role performs comprehensive validation to ensure deployment success:

#### **Critical Files Verification**
```yaml
# Validates presence of essential application files:
- Dockerfile                     # Container build capability
- docker-compose.yml            # Configuration reference
- rabbitmq-app.jar              # Application binary
- generate_docker_certs.sh      # Certificate generation capability
- README.md                     # Documentation
```

#### **Directory Structure Verification**
```yaml
# Validates proper directory structure:
- src/                          # Source code directory
- src/main/                     # Main application directory
- src/main/resources/           # Application resources
- src/main/resources/cert/      # Certificate storage
- src/main/resources/secret-patterns-db/  # Secret detection patterns
```

#### **Certificate Validation**
```yaml
# Validates generated certificates:
- ca.pem                        # Certificate Authority
- server_cert/server-cert.pem   # Server certificate
- server_cert/server-key.pem    # Server private key
- src/main/resources/cert/ca.pem     # Client CA
- src/main/resources/cert/cert.pem   # Client certificate
- src/main/resources/cert/key.pem    # Client private key
```

### **Error Handling**
- **APT Lock Management**: Safely handles package management conflicts
- **File Copy Verification**: Ensures successful file transfer
- **Certificate Generation**: Validates certificate creation success
- **Download Verification**: Confirms certificates are available on control node

## 🔗 **Integration with Other Roles**

### **Role Dependencies**
```yaml
# Execution order in main playbook:
1. common              # Basic system setup
2. containerd          # Container runtime
3. kubernetes          # Kubernetes cluster
4. worker              # Worker node configuration
5. liftoffstage        # External services (Jenkins, RabbitMQ)
6. firststage-prep     # THIS ROLE - Application preparation
7. firststage          # Main FirstStage deployment
```

### **Provides for FirstStage Role**
- **✅ Application Files**: Ready-to-build application in `/opt/firststage/app-files/`
- **✅ TLS Certificates**: Complete PKI for secure Docker API communication
- **✅ Validated Environment**: Confirmed file structure and permissions
- **✅ Server Certificates**: Available on control node for SAST configuration

### **Requires from Previous Roles**
- **Target Nodes**: Worker and SAST nodes must be accessible
- **Basic System**: Package management and file system ready
- **Network Connectivity**: Nodes can communicate for certificate validation

## 🚨 **Troubleshooting Guide**

### **Common Issues & Solutions**

#### **🔴 File Copy Failures**
```bash
# Issue: Permission denied or disk space
# Check target node storage and permissions
df -h /opt/
ls -la /opt/firststage/

# Solution: Ensure sufficient space and proper sudo access
sudo mkdir -p /opt/firststage/app-files
sudo chown ubuntu:ubuntu /opt/firststage/app-files
```

#### **🔴 Certificate Generation Failures**
```bash
# Issue: OpenSSL not available or script permissions
# Check OpenSSL installation and script permissions
which openssl
ls -la /opt/firststage/app-files/generate_docker_certs.sh

# Solution: Install OpenSSL and fix permissions
sudo apt update && sudo apt install openssl -y
chmod +x /opt/firststage/app-files/generate_docker_certs.sh
```

#### **🔴 Network Connectivity Issues**
```bash
# Issue: Cannot reach SAST node for IP resolution
# Test connectivity to SAST node
ping {{ sast_target_node }}
nslookup {{ sast_target_node }}

# Solution: Verify DNS resolution and network routing
# Check /etc/hosts or DNS configuration
```

#### **🔴 APT Lock Issues**
```bash
# Issue: Package management locks preventing execution
# Check for running package operations
ps aux | grep -E "(apt|dpkg|unattended-upgrade)"

# Solution: Wait for operations to complete or kill if stuck
sudo killall apt apt-get dpkg unattended-upgrade
sudo dpkg --configure -a
```

### **Verification Commands**
```bash
# Verify application files are properly deployed
ls -la /opt/firststage/app-files/
find /opt/firststage/app-files/ -name "*.jar" -o -name "Dockerfile"

# Verify certificates were generated
ls -la /opt/firststage/app-files/src/main/resources/cert/
ls -la /opt/firststage/app-files/server_cert/

# Verify certificates on control node
ls -la {{ playbook_dir }}/server_certificates/

# Test certificate validity
openssl x509 -in /opt/firststage/app-files/server_cert/server-cert.pem -text -noout
```

## 📊 **Performance & Resource Usage**

### **Resource Requirements**
- **Disk Space**: ~500MB for application files and certificates
- **Memory**: Minimal during execution (~50MB peak)
- **Network**: ~100MB transfer for FirstStage.zip
- **CPU**: Low usage except during certificate generation

### **Execution Time**
- **File Copy**: 30-60 seconds (depends on network speed)
- **Certificate Generation**: 10-30 seconds
- **Validation**: 5-10 seconds
- **Total Runtime**: 1-2 minutes typical

### **Optimization Tips**
- **Pre-stage Files**: Keep FirstStage.zip on target nodes to reduce transfer time
- **Certificate Caching**: Use `force_cert_regeneration: false` to reuse existing certificates
- **Parallel Execution**: Role can run on multiple nodes simultaneously

## 🔒 **Security Considerations**

### **Certificate Security**
- **Private Keys**: Properly secured with 600 permissions
- **CA Certificate**: Shared between client and server for trust
- **Certificate Rotation**: Use `force_cert_regeneration: true` for regular rotation
- **Secure Storage**: Server certificates downloaded to control node for distribution

### **File Permissions**
- **Application Files**: Owned by specified user (default: ubuntu)
- **Executable Scripts**: Proper execute permissions on certificate generation script
- **Directory Security**: Restricted access to certificate directories

### **Network Security**
- **TLS Communication**: All Docker API communication encrypted
- **Certificate Validation**: Mutual TLS authentication between client and server
- **IP Restrictions**: Certificates bound to specific IP addresses

## 📄 **License & Support**

### **License**
This role is licensed under the **MIT License**. See the LICENSE file for details.

### **Author Information**
- **Author**: Khasan Abdurakhmanov
- **Organization**: Innopolis University
- **Contact**: DevSecOps Platform Team
- **Version**: 1.0.0
- **Last Updated**: 2025

### **Support & Contributions**
- **Documentation**: Comprehensive README with examples and troubleshooting
- **Issue Tracking**: GitHub Issues for bug reports and feature requests
- **Contributions**: Pull requests welcome for improvements
- **Community**: DevSecOps Platform community support

---

**🎉 Thank you for using the FirstStage-Prep role!**

This role provides the essential foundation for secure FirstStage secret detection deployment. For additional support and advanced configurations, please refer to the DevSecOps Platform documentation or contact the infrastructure team.
