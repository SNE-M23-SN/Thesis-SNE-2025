# Common Ansible Role

This Ansible role provides essential system preparation and APT package management for all nodes in the DevSecOps platform, ensuring a clean and stable foundation for subsequent role deployments.

## 🎯 **Overview**

The `common` role is the foundational system preparation component that:

- **APT Management**: Disables and controls automatic package updates
- **Process Control**: Manages conflicting package management processes
- **Lock Management**: Handles APT locks and prevents installation conflicts
- **System Stability**: Ensures predictable package management behavior
- **Dependency Installation**: Installs essential system dependencies
- **Emergency Recovery**: Provides robust error handling and recovery mechanisms

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
│ • Cluster Init  │    │ • Node Join     │    │ • Platform Apps │
│ • CNI Setup     │    │ • Workload Host │    │ • Services      │
│ • Admin Config  │    │ • Pod Runtime   │    │ • Monitoring    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### **System Management Architecture**
```
┌─────────────────────────────────────────────────────────────┐
│                    Common System Preparation               │
├─────────────────────────────┬───────────────────────────────┤
│     APT Management          │      Process Control          │
│                             │                               │
├─────────────────────────────┼───────────────────────────────┤
│ • Disable unattended-upgrades │ • Kill hanging processes   │
│ • Stop automatic updates    │ • Remove stale locks         │
│ • Mask systemd services     │ • Emergency recovery         │
│ • Configure APT policies    │ • Retry mechanisms           │
│ • Control update timers     │ • Process monitoring         │
└─────────────────────────────┴───────────────────────────────┘
```

## 📋 **Prerequisites**

### **Infrastructure Requirements**
- **Operating System**: Ubuntu 20.04+ with root/sudo access
- **Network**: Internet connectivity for package downloads
- **Permissions**: sudo/root access for system configuration
- **Storage**: Sufficient disk space for package operations

### **System Requirements**
- **APT System**: Functional APT package management
- **Systemd**: systemd service management
- **Network**: Stable network connection for package repositories

## ⚙️ **Configuration Variables**

### **🔧 APT Configuration**

| Setting | Value | Description |
|---------|-------|-------------|
| Update Package Lists | `0` | Disables automatic package list updates |
| Unattended Upgrade | `0` | Disables automatic package upgrades |
| Autoclean Interval | `0` | Disables automatic package cleanup |
| Download Upgradeable | `0` | Disables automatic package downloads |

### **📦 Essential Dependencies**

| Package | Purpose | Description |
|---------|---------|-------------|
| `apt-transport-https` | Secure repositories | HTTPS transport for APT |
| `ca-certificates` | SSL verification | Certificate authority certificates |
| `curl` | File downloads | Command-line HTTP client |
| `gnupg-agent` | GPG operations | GNU Privacy Guard agent |
| `software-properties-common` | Repository management | Tools for managing repositories |
| `unzip` | Archive extraction | ZIP file extraction utility |

### **🔒 Security Services Disabled**

| Service | Action | Description |
|---------|--------|-------------|
| `unattended-upgrades.service` | Stopped, Disabled, Masked | Automatic package updates |
| `apt-daily.timer` | Stopped, Disabled, Masked | Daily APT operations |
| `apt-daily-upgrade.timer` | Stopped, Disabled, Masked | Daily upgrade operations |
| `apt-daily.service` | Stopped, Disabled, Masked | APT daily service |
| `apt-daily-upgrade.service` | Stopped, Disabled, Masked | APT upgrade service |

## 🚀 **Deployment Process**

The Common role executes a comprehensive 10-phase system preparation process:

### **Phase 1: Service Termination**
```yaml
# Stop automatic update services
1. Stops unattended-upgrades service
2. Disables service from automatic startup
3. Masks service to prevent activation
4. Ensures no automatic updates interfere
```

### **Phase 2: Process Cleanup**
```yaml
# Kill running update processes
1. Terminates unattended-upgrade processes
2. Kills apt-daily processes
3. Stops apt-daily-upgrade processes
4. Clears any hanging package operations
```

### **Phase 3: Configuration Management**
```yaml
# APT configuration files
1. Updates 20auto-upgrades configuration
2. Disables all automatic update policies
3. Modifies 50unattended-upgrades settings
4. Creates backup of original configurations
```

### **Phase 4: Timer Management**
```yaml
# Systemd timer control
1. Stops all APT-related timers
2. Disables timers from automatic activation
3. Masks timers to prevent restart
4. Ensures complete timer deactivation
```

### **Phase 5: Service Override**
```yaml
# Systemd service override
1. Creates override directory
2. Implements service override configuration
3. Redirects service execution to /bin/true
4. Creates disable marker file
```

### **Phase 6: Lock Cleanup**
```yaml
# APT lock removal
1. Removes dpkg locks
2. Clears APT list locks
3. Removes archive locks
4. Ensures clean lock state
```

### **Phase 7: System Reload**
```yaml
# Systemd and system refresh
1. Reloads systemd daemon
2. Waits for system stabilization
3. Verifies service states
4. Confirms configuration changes
```

### **Phase 8: Process Management**
```yaml
# Advanced process control
1. Monitors running APT processes
2. Implements timeout-based waiting
3. Provides emergency process termination
4. Handles stale lock recovery
```

### **Phase 9: Emergency Recovery**
```yaml
# Robust error handling
1. Emergency process killing
2. Force lock removal
3. DPKG reconfiguration
4. System state recovery
```

### **Phase 10: Dependency Installation**
```yaml
# Essential package installation
1. Updates APT cache with retry mechanism
2. Installs core system dependencies
3. Verifies successful installation
4. Prepares system for subsequent roles
```

## 📖 **Usage Examples**

### **Basic System Preparation**
```yaml
---
- name: Prepare All Nodes
  hosts: all
  become: yes
  roles:
    - role: common
```

### **Infrastructure Deployment**
```yaml
---
- name: Deploy DevSecOps Infrastructure
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

### **Complete Platform Deployment**
```yaml
---
- name: Deploy Complete DevSecOps Platform
  hosts: all
  become: yes
  roles:
    - role: common

- name: Setup Infrastructure
  hosts: all
  become: yes
  roles:
    - role: containerd
    - role: kubernetes

- name: Initialize Cluster
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
1. common                    # THIS ROLE - System preparation
2. containerd               # Container runtime configuration
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

### **Provides for All Subsequent Roles**
- **✅ Clean APT State**: No conflicting package operations
- **✅ Stable System**: Predictable package management behavior
- **✅ Essential Dependencies**: Core packages for all operations
- **✅ Process Control**: Managed system processes
- **✅ Error Recovery**: Robust error handling mechanisms

## 🚨 **Troubleshooting Guide**

### **Common Issues & Solutions**

#### **🔴 APT Lock Issues**
```bash
# Check for running processes
ps aux | grep -E "(apt|dpkg|unattended)"
pgrep -f "apt|dpkg|unattended-upgrade"

# Manual lock removal
sudo rm -f /var/lib/dpkg/lock*
sudo rm -f /var/cache/apt/archives/lock
sudo rm -f /var/lib/apt/lists/lock

# Reconfigure dpkg
sudo dpkg --configure -a
```

#### **🔴 Service Control Issues**
```bash
# Check service status
systemctl status unattended-upgrades
systemctl is-active apt-daily.timer

# Manual service control
sudo systemctl stop unattended-upgrades
sudo systemctl disable unattended-upgrades
sudo systemctl mask unattended-upgrades
```

#### **🔴 Package Installation Failures**
```bash
# Check APT configuration
cat /etc/apt/apt.conf.d/20auto-upgrades
cat /etc/apt/apt.conf.d/50unattended-upgrades

# Manual package installation
sudo apt update
sudo apt install -y apt-transport-https ca-certificates curl
```

#### **🔴 Process Hanging Issues**
```bash
# Kill hanging processes
sudo pkill -f "apt-get|aptitude|dpkg|unattended-upgrade"

# Check process status
ps aux | grep -E "(apt|dpkg)" | grep -v grep

# Force process termination
sudo pkill -9 -f "unattended-upgrade"
```

### **🔧 Diagnostic Commands**
```bash
# System status
systemctl status unattended-upgrades
systemctl list-timers | grep apt

# Process monitoring
ps aux | grep -E "(apt|dpkg)"
pgrep -f "apt|dpkg|unattended"

# Lock status
ls -la /var/lib/dpkg/lock*
ls -la /var/cache/apt/archives/lock

# Configuration verification
cat /etc/apt/apt.conf.d/20auto-upgrades
systemctl is-masked unattended-upgrades
```

### **🔄 Recovery Procedures**
```bash
# Complete system reset
sudo systemctl stop unattended-upgrades apt-daily.timer apt-daily-upgrade.timer
sudo pkill -f "apt|dpkg|unattended"
sudo rm -f /var/lib/dpkg/lock*
sudo dpkg --configure -a
sudo apt update

# Service reconfiguration
sudo systemctl daemon-reload
sudo systemctl mask unattended-upgrades
sudo systemctl disable apt-daily.timer apt-daily-upgrade.timer
```

## 🔒 **Security Features**

### **System Security**
- **Controlled Updates**: Prevents unwanted system changes
- **Process Isolation**: Manages conflicting system processes
- **Configuration Backup**: Preserves original system configurations
- **Emergency Recovery**: Robust error handling and recovery

### **Package Security**
- **Dependency Control**: Installs only essential packages
- **Version Stability**: Prevents automatic package updates
- **Repository Security**: Uses official package repositories
- **GPG Verification**: Maintains package authenticity

## 📊 **Performance & Monitoring**

### **System Metrics**
```bash
# Service status
systemctl status unattended-upgrades
systemctl list-timers --all

# Process monitoring
ps aux | grep -E "(apt|dpkg)" | wc -l
pgrep -f "apt|dpkg|unattended" | wc -l

# Lock monitoring
ls -la /var/lib/dpkg/lock* 2>/dev/null | wc -l
```

### **Performance Benefits**
- **Reduced CPU Usage**: No background update processes
- **Predictable Behavior**: Controlled package management
- **Faster Deployments**: No conflicting package operations
- **System Stability**: Consistent system state

## 📄 **License & Support**

### **License Information**
- **License**: MIT License
- **Author**: Khasan Abdurakhmanov
- **Organization**: Innopolis University DevSecOps Platform
- **Version**: 1.0.0
- **Last Updated**: 2025

---

**🎉 Thank you for using the Common System Preparation Role!**

This role provides the essential foundation for all DevSecOps platform deployments by ensuring clean and stable system preparation. For advanced configurations and enterprise support, please contact the DevSecOps Platform team.
