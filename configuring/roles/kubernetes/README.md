# Kubernetes Ansible Role

This Ansible role installs and configures Kubernetes packages (kubelet, kubeadm, kubectl) on both master and worker nodes, providing the foundation for Kubernetes cluster deployment in the DevSecOps platform.

## 🎯 **Overview**

The `kubernetes` role is a foundational infrastructure component that:

- **Package Installation**: Installs specific versions of Kubernetes components
- **System Configuration**: Configures system requirements for Kubernetes
- **Repository Management**: Sets up official Kubernetes package repositories
- **Version Control**: Holds packages at specific versions to prevent unwanted updates
- **Service Management**: Configures and starts kubelet service
- **Security Setup**: Implements secure package installation with GPG verification

## 🏗️ **Architecture & Integration**

### **Role in DevSecOps Infrastructure**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Common      │    │   Containerd    │    │   Kubernetes    │
│ (System Prep)   │ -> │ (Runtime)       │ -> │ (K8s Packages)  │
│                 │    │                 │    │                 │
│ • APT Management│    │ • Container     │    │ • kubelet       │
│ • Dependencies  │    │ • CRI Config    │    │ • kubeadm       │
│ • System Setup  │    │ • Security      │    │ • kubectl       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                       ↓
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Master      │    │     Worker      │    │   Applications  │
│ (Control Plane) │    │ (Compute Node)  │    │ (DevSecOps)     │
│                 │    │                 │    │                 │
│ • Cluster Init  │    │ • Node Join     │    │ • Platform Apps │
│ • CNI Setup     │    │ • Workload Host │    │ • Services      │
│ • Admin Config  │    │ • Pod Runtime   │    │ • Monitoring    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### **Kubernetes Components Architecture**
```
┌─────────────────────────────────────────────────────────────┐
│                  Kubernetes Installation                   │
├─────────────────────────────┬───────────────────────────────┤
│       System Layer          │      Kubernetes Layer        │
│                             │                               │
├─────────────────────────────┼───────────────────────────────┤
│ • Swap Disabled             │ • kubelet (Node Agent)        │
│ • System Dependencies       │ • kubeadm (Cluster Tool)      │
│ • Kernel Parameters         │ • kubectl (CLI Tool)          │
│ • Repository Setup          │ • Package Version Hold       │
│ • GPG Key Verification      │ • Service Configuration       │
└─────────────────────────────┴───────────────────────────────┘
```

## 📋 **Prerequisites**

### **Infrastructure Requirements**
- **Operating System**: Ubuntu 20.04+ with root/sudo access
- **Hardware**: Minimum 2 CPU cores, 2GB RAM, 10GB disk space
- **Network**: Internet connectivity for package downloads
- **Dependencies**: Common and Containerd roles must be executed first

### **System Requirements**
- **Swap**: Must be disabled for kubelet operation
- **Container Runtime**: containerd properly configured
- **Network**: Proper network configuration for cluster communication

## ⚙️ **Configuration Variables**

### **🔧 Kubernetes Package Configuration**

| Component | Version | Description |
|-----------|---------|-------------|
| `kubelet` | `1.31.1-1.1` | Kubernetes node agent |
| `kubeadm` | `1.31.1-1.1` | Kubernetes cluster management tool |
| `kubectl` | `1.31.1-1.1` | Kubernetes command-line interface |

### **📦 Repository Configuration**

| Setting | Value | Description |
|---------|-------|-------------|
| Repository URL | `https://pkgs.k8s.io/core:/stable:/v1.31/deb/` | Official Kubernetes repository |
| GPG Key URL | `https://pkgs.k8s.io/core:/stable:/v1.31/deb/Release.key` | Package signing key |
| Keyring Location | `/etc/apt/keyrings/kubernetes-apt-keyring.gpg` | Secure key storage |

### **🔒 Security Configuration**

| Feature | Implementation | Description |
|---------|----------------|-------------|
| GPG Verification | Enabled | Package authenticity verification |
| Secure Keyrings | `/etc/apt/keyrings/` | Secure key storage directory |
| Package Holds | All packages | Prevents unwanted version updates |

## 🚀 **Deployment Process**

The Kubernetes role executes a comprehensive 8-phase deployment process:

### **Phase 1: System Preparation**
```yaml
# Swap and system configuration
1. Removes swap entries from /etc/fstab
2. Disables active swap partitions
3. Ensures system meets Kubernetes requirements
4. Prepares system for kubelet operation
```

### **Phase 2: Dependencies Installation**
```yaml
# Required system packages
1. Installs apt-transport-https for secure repositories
2. Installs ca-certificates for SSL verification
3. Installs curl for file downloads
4. Installs gnupg-agent for GPG operations
5. Installs software-properties-common for repository management
```

### **Phase 3: Security Setup**
```yaml
# GPG key and repository security
1. Creates secure keyrings directory (/etc/apt/keyrings)
2. Downloads Kubernetes GPG signing key
3. Imports and secures GPG key
4. Sets proper permissions for security
```

### **Phase 4: Repository Configuration**
```yaml
# Kubernetes package repository
1. Adds official Kubernetes repository
2. Configures GPG signature verification
3. Updates package cache with new repository
4. Prepares for Kubernetes package installation
```

### **Phase 5: Package Management**
```yaml
# APT lock and process management
1. Checks for running apt/dpkg processes
2. Waits for package managers to complete
3. Removes stale locks if processes timeout
4. Ensures clean package management state
```

### **Phase 6: Kubernetes Installation**
```yaml
# Kubernetes components installation
1. Installs kubelet 1.31.1-1.1
2. Installs kubeadm 1.31.1-1.1
3. Installs kubectl 1.31.1-1.1
4. Allows version downgrade if necessary
```

### **Phase 7: Version Control**
```yaml
# Package version management
1. Holds kubelet at current version
2. Holds kubeadm at current version
3. Holds kubectl at current version
4. Prevents automatic updates
```

### **Phase 8: Service Configuration**
```yaml
# kubelet service management
1. Enables kubelet service for automatic startup
2. Starts kubelet service
3. Reloads systemd daemon
4. Ensures service is operational
```

## 📖 **Usage Examples**

### **Basic Kubernetes Installation**
```yaml
---
- name: Install Kubernetes Components
  hosts: all
  become: yes
  roles:
    - role: common
    - role: containerd
    - role: kubernetes
```

### **Master Node Preparation**
```yaml
---
- name: Prepare Master Node
  hosts: master
  become: yes
  roles:
    - role: common
    - role: containerd
    - role: kubernetes
    - role: master
```

### **Worker Node Preparation**
```yaml
---
- name: Prepare Worker Nodes
  hosts: workers
  become: yes
  roles:
    - role: common
    - role: containerd
    - role: kubernetes
    - role: worker
```

### **Complete Cluster Setup**
```yaml
---
- name: Deploy Complete Kubernetes Cluster
  hosts: all
  become: yes
  roles:
    - role: common
    - role: containerd
    - role: kubernetes

- name: Initialize Master
  hosts: master
  become: yes
  roles:
    - role: master

- name: Join Workers
  hosts: workers
  become: yes
  roles:
    - role: worker
```

## 🔗 **Integration with DevSecOps Pipeline**

### **Role Dependencies**
```yaml
# Execution order in main playbook:
1. common                    # System preparation and APT management
2. containerd               # Container runtime configuration
3. kubernetes               # THIS ROLE - Kubernetes packages installation
4. master                   # Control plane initialization
5. worker                   # Worker nodes join cluster
6. liftoffstage            # External services deployment
7. firststage-prep         # Application preparation
8. firststage              # Secret detection application
9. secondstage             # AI monitoring and analysis
10. thirdstage             # Dashboard frontend
11. sast                   # Security analysis platform
```

### **Provides for Subsequent Roles**
- **✅ kubelet**: Node agent for container management
- **✅ kubeadm**: Cluster initialization and management
- **✅ kubectl**: Command-line interface for cluster operations
- **✅ Version Stability**: Held packages prevent unwanted updates
- **✅ Service Foundation**: Running kubelet ready for cluster operations

## 🚨 **Troubleshooting Guide**

### **Common Issues & Solutions**

#### **🔴 Package Installation Failures**
```bash
# Check repository configuration
cat /etc/apt/sources.list.d/kubernetes.list
apt-cache policy kubelet kubeadm kubectl

# Verify GPG key
ls -la /etc/apt/keyrings/kubernetes-apt-keyring.gpg
apt-key list

# Manual package installation
sudo apt update
sudo apt install -y kubelet=1.31.1-1.1 kubeadm=1.31.1-1.1 kubectl=1.31.1-1.1
```

#### **🔴 APT Lock Issues**
```bash
# Check for running processes
ps aux | grep -E "(apt|dpkg)"
pgrep -f "apt|dpkg|unattended-upgrade"

# Remove locks manually
sudo rm -f /var/lib/dpkg/lock*
sudo rm -f /var/cache/apt/archives/lock
sudo dpkg --configure -a
```

#### **🔴 Swap Issues**
```bash
# Check swap status
swapon --show
free -h

# Disable swap permanently
sudo swapoff -a
sudo sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab
```

#### **🔴 kubelet Service Issues**
```bash
# Check kubelet status
systemctl status kubelet
journalctl -u kubelet -f

# Restart kubelet
sudo systemctl restart kubelet
sudo systemctl enable kubelet
```

### **🔧 Diagnostic Commands**
```bash
# Package verification
dpkg -l | grep kube
apt-mark showhold

# Service status
systemctl status kubelet
systemctl is-enabled kubelet

# System requirements
swapon --show
free -h
df -h

# Network configuration
ip addr show
ip route show
```

### **🔄 Recovery Procedures**
```bash
# Reset package holds
sudo apt-mark unhold kubelet kubeadm kubectl

# Reinstall packages
sudo apt remove -y kubelet kubeadm kubectl
sudo apt autoremove -y
sudo apt install -y kubelet=1.31.1-1.1 kubeadm=1.31.1-1.1 kubectl=1.31.1-1.1

# Restore holds
sudo apt-mark hold kubelet kubeadm kubectl

# Restart services
sudo systemctl daemon-reload
sudo systemctl restart kubelet
```

## 🔒 **Security Features**

### **Package Security**
- **GPG Verification**: All packages verified with official Kubernetes GPG key
- **Secure Repository**: Official Kubernetes repository with HTTPS
- **Version Control**: Specific versions held to prevent unwanted updates
- **Secure Keyrings**: GPG keys stored in secure system directory

### **System Security**
- **Minimal Dependencies**: Only required packages installed
- **Proper Permissions**: Secure file and directory permissions
- **Service Isolation**: kubelet runs with appropriate system privileges

## 📊 **Performance & Monitoring**

### **Package Metrics**
```bash
# Package information
dpkg -l | grep kube
apt list --installed | grep kube

# Service performance
systemctl status kubelet
journalctl -u kubelet --since "1 hour ago"

# System resources
free -h
df -h /var/lib/kubelet
```

### **Version Management**
- **Consistent Versions**: All components at same version (1.31.1-1.1)
- **Update Control**: Package holds prevent automatic updates
- **Compatibility**: Versions tested for DevSecOps platform compatibility

## 📄 **License & Support**

### **License Information**
- **License**: MIT License
- **Author**: Khasan Abdurakhmanov
- **Organization**: Innopolis University DevSecOps Platform
- **Version**: 1.0.0
- **Last Updated**: 2025

---

**🎉 Thank you for using the Kubernetes Infrastructure Role!**

This role provides the essential Kubernetes packages and configuration for the DevSecOps platform infrastructure. For advanced configurations and enterprise support, please contact the DevSecOps Platform team.
