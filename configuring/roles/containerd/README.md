# Containerd Ansible Role

This Ansible role installs and configures containerd as the container runtime for Kubernetes, providing secure and efficient container management for the DevSecOps platform infrastructure.

## 🎯 **Overview**

The `containerd` role is a critical infrastructure component that:

- **Container Runtime**: Installs and configures containerd for Kubernetes
- **Kernel Configuration**: Sets up required kernel modules and parameters
- **CRI Integration**: Configures Container Runtime Interface for Kubernetes
- **Security Hardening**: Implements security best practices for container runtime
- **Tool Installation**: Installs and configures crictl for container management
- **System Optimization**: Optimizes system for container workloads

## 🏗️ **Architecture & Integration**

### **Role in DevSecOps Infrastructure**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Common      │    │   Containerd    │    │   Kubernetes    │
│ (System Prep)   │ -> │ (Runtime)       │ -> │ (K8s Packages)  │
│                 │    │                 │    │                 │
│ • APT Control   │    │ • Container     │    │ • kubelet       │
│ • Lock Mgmt     │    │ • CRI Config    │    │ • kubeadm       │
│ • Dependencies  │    │ • Security      │    │ • kubectl       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                       ↓
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Master      │    │     Worker      │    │   Applications  │
│ (Control Plane) │    │ (Compute Node)  │    │ (DevSecOps)     │
│                 │    │                 │    │                 │
│ • Cluster Init  │    │ • Node Join     │    │ • Container     │
│ • CNI Setup     │    │ • Workload Host │    │ • Workloads     │
│ • Admin Config  │    │ • Pod Runtime   │    │ • Services      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### **Containerd Architecture**
```
┌─────────────────────────────────────────────────────────────┐
│                    Containerd Runtime                      │
├─────────────────────────────┬───────────────────────────────┤
│       Kernel Layer          │      Runtime Layer            │
│                             │                               │
├─────────────────────────────┼───────────────────────────────┤
│ • overlay module            │ • containerd.io               │
│ • br_netfilter module       │ • CRI configuration           │
│ • IP forwarding enabled     │ • SystemdCgroup enabled       │
│ • Bridge netfilter enabled  │ • crictl tool                 │
│ • Network parameters        │ • Socket communication        │
└─────────────────────────────┴───────────────────────────────┘
```

## 📋 **Prerequisites**

### **Infrastructure Requirements**
- **Operating System**: Ubuntu 20.04+ with root/sudo access
- **Hardware**: Minimum 2 CPU cores, 2GB RAM, 10GB disk space
- **Network**: Internet connectivity for package downloads
- **Dependencies**: Common role must be executed first

### **System Requirements**
- **Kernel Support**: Modern Linux kernel with container support
- **Network Configuration**: Proper network setup for container networking
- **Storage**: Sufficient storage for container images and data

## ⚙️ **Configuration Variables**

### **🔧 Kernel Modules**

| Module | Purpose | Description |
|--------|---------|-------------|
| `overlay` | Filesystem | OverlayFS for container layers |
| `br_netfilter` | Networking | Bridge netfilter for container networking |

### **🌐 Network Parameters**

| Parameter | Value | Description |
|-----------|-------|-------------|
| `net.bridge.bridge-nf-call-ip6tables` | `1` | Enable IPv6 bridge netfilter |
| `net.bridge.bridge-nf-call-iptables` | `1` | Enable IPv4 bridge netfilter |
| `net.ipv4.ip_forward` | `1` | Enable IP forwarding |

### **📦 Dependencies**

| Package | Purpose | Description |
|---------|---------|-------------|
| `apt-transport-https` | Secure repositories | HTTPS transport for APT |
| `ca-certificates` | SSL verification | Certificate authority certificates |
| `curl` | File downloads | Command-line HTTP client |
| `gnupg-agent` | GPG operations | GNU Privacy Guard agent |
| `software-properties-common` | Repository management | Tools for managing repositories |
| `socat` | Port forwarding | Required for Kubernetes port forwarding |

### **🔒 Containerd Configuration**

| Setting | Value | Description |
|---------|-------|-------------|
| SystemdCgroup | `true` | Use systemd for cgroup management |
| Sandbox Image | `registry.k8s.io/pause:3.9` | Kubernetes pause container |
| Disabled Plugins | `[]` | No plugins disabled |
| CRI Socket | `unix:///var/run/containerd/containerd.sock` | Container runtime socket |

## 🚀 **Deployment Process**

The Containerd role executes a comprehensive 10-phase deployment process:

### **Phase 1: Kernel Module Configuration**
```yaml
# Kernel modules for container support
1. Creates containerd.conf in /etc/modules-load.d/
2. Configures overlay filesystem module
3. Configures br_netfilter networking module
4. Ensures modules load at boot time
```

### **Phase 2: Kernel Module Loading**
```yaml
# Load required kernel modules
1. Loads overlay module for container layers
2. Loads br_netfilter module for networking
3. Verifies successful module loading
4. Prepares kernel for container operations
```

### **Phase 3: Network Parameter Configuration**
```yaml
# Kubernetes networking requirements
1. Enables bridge netfilter for IPv6
2. Enables bridge netfilter for IPv4
3. Enables IP forwarding
4. Applies sysctl parameters immediately
```

### **Phase 4: Dependency Installation**
```yaml
# System dependencies for containerd
1. Installs APT transport packages
2. Installs certificate management tools
3. Installs networking utilities (socat)
4. Prepares system for Docker repository
```

### **Phase 5: Repository Setup**
```yaml
# Docker repository for containerd
1. Adds Docker official GPG key
2. Adds Docker repository to APT sources
3. Updates package cache
4. Prepares for containerd installation
```

### **Phase 6: Process Management**
```yaml
# Package manager conflict resolution
1. Checks for active package managers
2. Waits for package operations to complete
3. Handles stuck package managers
4. Ensures clean installation environment
```

### **Phase 7: Containerd Installation**
```yaml
# Containerd package installation
1. Installs containerd.io package
2. Creates containerd configuration directory
3. Generates default containerd configuration
4. Prepares for custom configuration
```

### **Phase 8: Configuration Optimization**
```yaml
# Containerd CRI configuration
1. Enables SystemdCgroup for proper cgroup management
2. Configures Kubernetes pause container image
3. Enables all plugins (removes disabled_plugins)
4. Optimizes for Kubernetes integration
```

### **Phase 9: Service Management**
```yaml
# Containerd service configuration
1. Enables containerd service for automatic startup
2. Starts containerd service
3. Reloads systemd daemon
4. Waits for containerd socket availability
```

### **Phase 10: Tool Installation**
```yaml
# Container management tools
1. Verifies crictl availability
2. Installs crictl if not present
3. Configures crictl for containerd
4. Enables container debugging and management
```

## 📖 **Usage Examples**

### **Basic Containerd Installation**
```yaml
---
- name: Install Containerd Runtime
  hosts: all
  become: yes
  roles:
    - role: common
    - role: containerd
```

### **Kubernetes Infrastructure Setup**
```yaml
---
- name: Setup Kubernetes Infrastructure
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

### **Complete Cluster Deployment**
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
2. containerd               # THIS ROLE - Container runtime configuration
3. kubernetes               # Kubernetes packages installation
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
- **✅ Container Runtime**: Functional containerd for Kubernetes
- **✅ CRI Interface**: Container Runtime Interface for kubelet
- **✅ Network Support**: Kernel networking for pod communication
- **✅ Management Tools**: crictl for container debugging
- **✅ Security Foundation**: Secure container runtime configuration

### **Supports DevSecOps Applications**
- **Container Workloads**: All platform applications run as containers
- **Security Isolation**: Container-based security boundaries
- **Resource Management**: CPU and memory allocation for containers
- **Image Management**: Container image storage and lifecycle

## 🚨 **Troubleshooting Guide**

### **Common Issues & Solutions**

#### **🔴 Containerd Service Issues**
```bash
# Check containerd status
systemctl status containerd
journalctl -u containerd -f

# Restart containerd
sudo systemctl restart containerd
sudo systemctl enable containerd

# Verify socket
ls -la /var/run/containerd/containerd.sock
```

#### **🔴 Kernel Module Issues**
```bash
# Check loaded modules
lsmod | grep overlay
lsmod | grep br_netfilter

# Load modules manually
sudo modprobe overlay
sudo modprobe br_netfilter

# Verify module configuration
cat /etc/modules-load.d/containerd.conf
```

#### **🔴 Network Parameter Issues**
```bash
# Check network parameters
sysctl net.bridge.bridge-nf-call-iptables
sysctl net.bridge.bridge-nf-call-ip6tables
sysctl net.ipv4.ip_forward

# Apply parameters manually
sudo sysctl -w net.bridge.bridge-nf-call-iptables=1
sudo sysctl -w net.bridge.bridge-nf-call-ip6tables=1
sudo sysctl -w net.ipv4.ip_forward=1
```

#### **🔴 CRI Configuration Issues**
```bash
# Check containerd configuration
cat /etc/containerd/config.toml | grep SystemdCgroup
cat /etc/containerd/config.toml | grep sandbox_image

# Test CRI functionality
crictl --runtime-endpoint unix:///var/run/containerd/containerd.sock version
crictl ps
```

### **🔧 Diagnostic Commands**
```bash
# Service status
systemctl status containerd
systemctl is-enabled containerd

# Socket verification
ls -la /var/run/containerd/
ss -l | grep containerd

# Configuration verification
cat /etc/containerd/config.toml
cat /etc/crictl.yaml

# Runtime testing
crictl version
crictl info
```

### **🔄 Recovery Procedures**
```bash
# Reset containerd configuration
sudo systemctl stop containerd
sudo rm -f /etc/containerd/config.toml
containerd config default | sudo tee /etc/containerd/config.toml

# Fix SystemdCgroup setting
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/' /etc/containerd/config.toml

# Restart services
sudo systemctl daemon-reload
sudo systemctl restart containerd
sudo systemctl status containerd
```

## 🔒 **Security Features**

### **Container Security**
- **SystemdCgroup**: Proper cgroup management for security
- **Namespace Isolation**: Container namespace isolation
- **Resource Limits**: CPU and memory resource controls
- **Image Security**: Secure container image handling

### **Network Security**
- **Bridge Netfilter**: Secure container networking
- **IP Forwarding Control**: Controlled network routing
- **Socket Security**: Secure containerd socket communication

### **System Security**
- **Kernel Module Control**: Secure kernel module loading
- **Service Isolation**: Isolated containerd service
- **Configuration Security**: Secure configuration file permissions

## 📊 **Performance & Monitoring**

### **Runtime Metrics**
```bash
# Containerd performance
crictl stats
crictl ps -a

# System resources
free -h
df -h /var/lib/containerd

# Network performance
ip addr show
ss -tuln | grep containerd
```

### **Performance Optimization**
- **SystemdCgroup**: Efficient cgroup management
- **Overlay Filesystem**: Optimized container layer storage
- **Resource Management**: Proper CPU and memory allocation
- **Network Optimization**: Efficient container networking

## 📄 **License & Support**

### **License Information**
- **License**: MIT License
- **Author**: Khasan Abdurakhmanov
- **Organization**: Innopolis University DevSecOps Platform
- **Version**: 1.0.0
- **Last Updated**: 2025

---

**🎉 Thank you for using the Containerd Runtime Role!**

This role provides the essential container runtime foundation for the DevSecOps platform Kubernetes infrastructure. For advanced configurations and enterprise support, please contact the DevSecOps Platform team.
