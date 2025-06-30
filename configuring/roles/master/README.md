# Master Ansible Role

This Ansible role configures a Kubernetes master (control plane) node, initializing the cluster and setting up essential components for the DevSecOps platform infrastructure.

## 🎯 **Overview**

The `master` role is the foundational infrastructure component that:

- **Kubernetes Control Plane**: Initializes and configures the Kubernetes master node
- **Cluster Initialization**: Sets up the cluster with proper networking and runtime configuration
- **CNI Installation**: Deploys Calico Container Network Interface for pod networking
- **Helm Installation**: Installs Helm package manager for Kubernetes applications
- **Join Token Management**: Generates and distributes worker node join tokens
- **Admin Configuration**: Sets up kubectl access for cluster administration

## 🏗️ **Architecture & Integration**

### **Role in DevSecOps Infrastructure**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Common      │    │   Containerd    │    │     Master      │
│ (System Prep)   │ -> │ (Runtime)       │ -> │ (Control Plane) │
│                 │    │                 │    │                 │
│ • APT Management│    │ • Container     │    │ • kubeadm init  │
│ • Dependencies  │    │ • CRI Config    │    │ • CNI Setup     │
│ • System Setup  │    │ • Security      │    │ • Helm Install  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                       ↓
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Worker      │    │   Kubernetes    │    │   Applications  │
│ (Join Cluster)  │ <- │ (Base Config)   │ <- │ (DevSecOps)     │
│                 │    │                 │    │                 │
│ • Join Token    │    │ • kubelet       │    │ • LiftOffStage  │
│ • Node Config   │    │ • Package Hold  │    │ • FirstStage    │
│ • Cluster Join  │    │ • System Config │    │ • SecondStage   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📋 **Prerequisites**

### **Infrastructure Requirements**
- **Operating System**: Ubuntu 20.04+ with root/sudo access
- **Hardware**: Minimum 2 CPU cores, 4GB RAM, 20GB disk space
- **Network**: Static IP address and proper DNS resolution
- **Dependencies**: Common and Containerd roles must be executed first

### **Required Variables**
- `master_hostname`: Hostname for the master node (e.g., "k8smaster")

### **System Requirements**
- **Container Runtime**: containerd properly configured and running
- **Network**: Pod network CIDR (10.244.0.0/16) available
- **Firewall**: Kubernetes control plane ports accessible

## ⚙️ **Configuration Variables**

### **🌐 Node Configuration**

| Variable | Default | Description |
|----------|---------|-------------|
| `master_hostname` | Required | Hostname for the Kubernetes master node |

### **🔧 Kubernetes Configuration**

| Setting | Value | Description |
|---------|-------|-------------|
| Pod Network CIDR | `10.244.0.0/16` | Network range for pod communication |
| CRI Socket | `unix:///var/run/containerd/containerd.sock` | Container runtime interface |
| CNI Provider | Calico | Container Network Interface |
| Helm Version | `v3.11.0` | Kubernetes package manager |

## 🚀 **Deployment Process**

### **Phase 1: Node Preparation**
```yaml
# Hostname and system configuration
1. Sets hostname for master node identification
2. Checks for existing cluster initialization
3. Resets kubeadm if previous initialization failed
4. Verifies containerd runtime is operational
```

### **Phase 2: Cluster Initialization**
```yaml
# Kubernetes control plane setup
1. Initializes cluster with kubeadm
2. Configures pod network CIDR (10.244.0.0/16)
3. Specifies containerd as container runtime
4. Creates cluster certificates and configuration
```

### **Phase 3: Admin Configuration**
```yaml
# kubectl access setup
1. Creates .kube directory for ubuntu user
2. Copies admin.conf to user's kube config
3. Sets proper ownership and permissions
4. Enables kubectl access for cluster management
```

### **Phase 4: Network Configuration**
```yaml
# Container Network Interface setup
1. Installs Calico CNI from official manifests
2. Configures pod-to-pod networking
3. Enables network policies and security
4. Establishes cluster networking foundation
```

### **Phase 5: Package Management**
```yaml
# Helm installation for application deployment
1. Downloads Helm v3.11.0 binary
2. Extracts to /usr/local/bin with proper permissions
3. Enables Kubernetes package management
4. Prepares for application chart deployments
```

### **Phase 6: Worker Node Integration**
```yaml
# Join token generation and distribution
1. Generates kubeadm join command with token
2. Displays join command for verification
3. Saves join command to local file
4. Prepares for worker node cluster joining
```

### **Phase 7: Cluster Verification**
```yaml
# Final validation and status
1. Retrieves cluster node information
2. Displays cluster membership status
3. Validates successful initialization
4. Confirms cluster readiness
```

## 📖 **Usage Examples**

### **Basic Master Deployment**
```yaml
---
- name: Initialize Kubernetes Master Node
  hosts: master
  become: yes
  roles:
    - role: common
    - role: containerd
    - role: kubernetes
    - role: master
  vars:
    master_hostname: "k8smaster"
```

### **Production Master Configuration**
```yaml
---
- name: Deploy Production Kubernetes Master
  hosts: master
  become: yes
  roles:
    - role: common
    - role: containerd
    - role: kubernetes
    - role: master
  vars:
    master_hostname: "prod-k8s-master-01"
```

### **Development Environment**
```yaml
---
- name: Setup Development Kubernetes Master
  hosts: master
  become: yes
  roles:
    - role: common
    - role: containerd
    - role: kubernetes
    - role: master
  vars:
    master_hostname: "dev-k8s-master"
```

## 🔗 **Integration with DevSecOps Pipeline**

### **Role Dependencies**
```yaml
# Execution order in main playbook:
1. common                    # System preparation and APT management
2. containerd               # Container runtime configuration
3. kubernetes               # Kubernetes packages and base configuration
4. master                   # THIS ROLE - Control plane initialization
5. worker                   # Worker nodes join the cluster
6. liftoffstage            # External services deployment
7. firststage-prep         # Application preparation
8. firststage              # Secret detection application
9. secondstage             # AI monitoring and analysis
10. thirdstage             # Dashboard frontend
11. sast                   # Security analysis platform
```

### **Provides for Subsequent Roles**
- **✅ Kubernetes Cluster**: Functional control plane for application deployment
- **✅ CNI Networking**: Pod-to-pod communication infrastructure
- **✅ Helm Package Manager**: Application deployment capabilities
- **✅ Join Token**: Worker node cluster integration
- **✅ kubectl Access**: Cluster administration and management

## 🚨 **Troubleshooting Guide**

### **Common Issues & Solutions**

#### **🔴 Cluster Initialization Failures**
```bash
# Check containerd status
systemctl status containerd
crictl --runtime-endpoint unix:///var/run/containerd/containerd.sock version

# Reset and retry initialization
sudo kubeadm reset --force
sudo systemctl restart containerd
sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --cri-socket=unix:///var/run/containerd/containerd.sock
```

#### **🔴 CNI Installation Issues**
```bash
# Check pod status
kubectl get pods -n kube-system

# Reinstall Calico CNI
kubectl delete -f https://docs.projectcalico.org/manifests/calico.yaml
kubectl apply -f https://docs.projectcalico.org/manifests/calico.yaml
```

#### **🔴 kubectl Access Problems**
```bash
# Fix kubectl configuration
sudo cp /etc/kubernetes/admin.conf /home/ubuntu/.kube/config
sudo chown ubuntu:ubuntu /home/ubuntu/.kube/config
```

### **🔧 Diagnostic Commands**
```bash
# Cluster status
kubectl get nodes
kubectl get pods -A
kubectl cluster-info

# Component health
kubectl get componentstatuses
systemctl status kubelet

# Network verification
kubectl get pods -n kube-system | grep calico
```

## 📄 **License & Support**

### **License Information**
- **License**: MIT License
- **Author**: Khasan Abdurakhmanov
- **Organization**: Innopolis University DevSecOps Platform
- **Version**: 1.0.0
- **Last Updated**: 2025

---

**🎉 Thank you for using the Master Infrastructure Role!**

This role provides the foundational Kubernetes control plane for the complete DevSecOps platform. For advanced configurations and enterprise support, please contact the DevSecOps Platform team.
