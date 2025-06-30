#!/bin/bash

# VPN Network Namespace Setup Script
# This script creates an isolated network namespace for VPN connections

set -e  # Exit on any error

# Configuration variables
NAMESPACE_NAME="vpnspace"
VETH_HOST="veth0"
VETH_NS="veth1"
HOST_IP="10.200.1.1"
NS_IP="10.200.1.2"
SUBNET="10.200.1.0/24"
VPN_CONFIG_PATH="/etc/openvpn/Canada, Quebec S2.ovpn" # Update this path

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Function to check if running as root
check_root() {
    if [[ $EUID -ne 0 ]]; then
        print_error "This script must be run as root (use sudo)"
        exit 1
    fi
}

# Function to get main network interface
get_main_interface() {
    local main_if=$(ip route | grep default | awk '{print $5}' | head -n1)
    if [[ -z "$main_if" ]]; then
        print_error "Could not determine main network interface"
        exit 1
    fi
    echo "$main_if"
}

# Function to cleanup existing setup
cleanup_existing() {
    print_step "Cleaning up existing setup..."
    
    # Remove existing namespace
    if ip netns list | grep -q "$NAMESPACE_NAME"; then
        print_status "Removing existing namespace: $NAMESPACE_NAME"
        ip netns del "$NAMESPACE_NAME" 2>/dev/null || true
    fi
    
    # Remove existing veth pair
    if ip link show "$VETH_HOST" &>/dev/null; then
        print_status "Removing existing veth pair"
        ip link del "$VETH_HOST" 2>/dev/null || true
    fi
    
    # Remove existing iptables rules
    print_status "Cleaning up iptables rules..."
    iptables -t nat -D POSTROUTING -s "$SUBNET" -o "$MAIN_IF" -j MASQUERADE 2>/dev/null || true
    iptables -D FORWARD -i "$VETH_HOST" -o "$MAIN_IF" -j ACCEPT 2>/dev/null || true
    iptables -D FORWARD -i "$MAIN_IF" -o "$VETH_HOST" -m state --state RELATED,ESTABLISHED -j ACCEPT 2>/dev/null || true
}

# Function to create namespace and veth pair
create_namespace() {
    print_step "Creating network namespace and veth pair..."
    
    # Create namespace
    ip netns add "$NAMESPACE_NAME"
    print_status "Created namespace: $NAMESPACE_NAME"
    
    # Create veth pair
    ip link add "$VETH_HOST" type veth peer name "$VETH_NS"
    print_status "Created veth pair: $VETH_HOST <-> $VETH_NS"
    
    # Move one end to namespace
    ip link set "$VETH_NS" netns "$NAMESPACE_NAME"
    print_status "Moved $VETH_NS to namespace"
}

# Function to configure IP addresses
configure_ips() {
    print_step "Configuring IP addresses..."
    
    # Configure host side
    ip addr add "$HOST_IP/24" dev "$VETH_HOST"
    ip link set "$VETH_HOST" up
    print_status "Configured $VETH_HOST with IP $HOST_IP"
    
    # Configure namespace side
    ip netns exec "$NAMESPACE_NAME" ip addr add "$NS_IP/24" dev "$VETH_NS"
    ip netns exec "$NAMESPACE_NAME" ip link set "$VETH_NS" up
    ip netns exec "$NAMESPACE_NAME" ip link set lo up
    print_status "Configured $VETH_NS with IP $NS_IP"
}

# Function to setup routing
setup_routing() {
    print_step "Setting up routing..."
    
    # Add default route in namespace
    ip netns exec "$NAMESPACE_NAME" ip route add default via "$HOST_IP"
    print_status "Added default route in namespace"
    
    # Enable IP forwarding
    sysctl -w net.ipv4.ip_forward=1
    print_status "Enabled IP forwarding"
}

# Function to configure iptables
configure_iptables() {
    print_step "Configuring iptables rules..."
    
    # Add NAT rule
    iptables -t nat -A POSTROUTING -s "$SUBNET" -o "$MAIN_IF" -j MASQUERADE
    print_status "Added NAT rule for $SUBNET"
    
    # Add FORWARD rules
    iptables -A FORWARD -i "$VETH_HOST" -o "$MAIN_IF" -j ACCEPT
    iptables -A FORWARD -i "$MAIN_IF" -o "$VETH_HOST" -m state --state RELATED,ESTABLISHED -j ACCEPT
    print_status "Added FORWARD rules"
}

# Function to setup DNS
setup_dns() {
    print_step "Setting up DNS for namespace..."
    
    # Create netns directory if it doesn't exist
    mkdir -p "/etc/netns/$NAMESPACE_NAME"
    
    # Copy resolv.conf to namespace
    cp /etc/resolv.conf "/etc/netns/$NAMESPACE_NAME/"
    
    # Also create a backup DNS config with public DNS servers
    cat > "/etc/netns/$NAMESPACE_NAME/resolv.conf" << EOF
nameserver 8.8.8.8
nameserver 8.8.4.4
nameserver 1.1.1.1
EOF
    
    print_status "Configured DNS for namespace"
}

# Function to test connectivity
test_connectivity() {
    print_step "Testing connectivity..."
    
    # Test namespace to host
    print_status "Testing namespace to host connectivity..."
    if ip netns exec "$NAMESPACE_NAME" ping -c 2 "$HOST_IP" &>/dev/null; then
        print_status "✓ Namespace to host: OK"
    else
        print_error "✗ Namespace to host: FAILED"
        return 1
    fi
    
    # Test external connectivity
    print_status "Testing external connectivity..."
    if ip netns exec "$NAMESPACE_NAME" ping -c 2 8.8.8.8 &>/dev/null; then
        print_status "✓ External connectivity: OK"
    else
        print_error "✗ External connectivity: FAILED"
        return 1
    fi
    
    # Test DNS resolution
    print_status "Testing DNS resolution..."
    if ip netns exec "$NAMESPACE_NAME" nslookup google.com &>/dev/null; then
        print_status "✓ DNS resolution: OK"
    else
        print_warning "✗ DNS resolution: FAILED - trying to fix..."
        # Try alternative DNS setup
        cat > "/etc/netns/$NAMESPACE_NAME/resolv.conf" << EOF
nameserver 8.8.8.8
nameserver 8.8.4.4
EOF
        if ip netns exec "$NAMESPACE_NAME" nslookup google.com &>/dev/null; then
            print_status "✓ DNS resolution: FIXED"
        else
            print_warning "✗ DNS resolution: Still failed (VPN might work anyway)"
        fi
    fi
}

# Function to show usage information
show_usage() {
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  setup     Set up the VPN namespace (default)"
    echo "  cleanup   Remove the VPN namespace and rules"
    echo "  status    Show current status"
    echo "  start-vpn Start VPN in namespace"
    echo "  shell     Open shell in namespace"
    echo "  test      Test connectivity"
    echo ""
    echo "Options:"
    echo "  -c, --config PATH    Path to VPN config file (default: $VPN_CONFIG_PATH)"
    echo "  -n, --namespace NAME Namespace name (default: $NAMESPACE_NAME)"
    echo "  -h, --help           Show this help"
}

# Function to show current status
show_status() {
    print_step "Checking VPN namespace status..."
    
    # Check if namespace exists
    if ip netns list | grep -q "$NAMESPACE_NAME"; then
        print_status "✓ Namespace '$NAMESPACE_NAME' exists"
        
        # Show namespace interfaces
        echo "Namespace interfaces:"
        ip netns exec "$NAMESPACE_NAME" ip addr show | grep -E "^\d+:|inet "
        
        # Show namespace routes
        echo "Namespace routes:"
        ip netns exec "$NAMESPACE_NAME" ip route
        
        # Check iptables rules
        echo "Relevant iptables rules:"
        iptables -t nat -L POSTROUTING -n | grep "$SUBNET" || echo "No NAT rules found"
        iptables -L FORWARD -n | grep "$VETH_HOST" || echo "No FORWARD rules found"
    else
        print_warning "✗ Namespace '$NAMESPACE_NAME' does not exist"
    fi
}

# Function to start VPN
start_vpn() {
    if [[ ! -f "$VPN_CONFIG_PATH" ]]; then
        print_error "VPN config file not found: $VPN_CONFIG_PATH"
        print_status "Please update the VPN_CONFIG_PATH variable or use -c option"
        exit 1
    fi
    
    print_step "Starting VPN in namespace..."
    print_status "VPN config: $VPN_CONFIG_PATH"
    print_status "Use Ctrl+C to stop the VPN"
    
    # Start OpenVPN in namespace
    ip netns exec "$NAMESPACE_NAME" openvpn --config "$VPN_CONFIG_PATH" --daemon

    # Wait until tun0 exists in the namespace

    echo "[INFO] Waiting for tun0 interface to appear..."

    for i in {1..10}; do

       if sudo ip netns exec "$NAMESPACE_NAME" ip link show tun0 &> /dev/null; then
 
           echo "[INFO] tun0 is now available"

           break

       fi

       sleep 1

    done

    # Enable IP forwarding & NAT inside namespace after VPN is up
    sudo ip netns exec "$NAMESPACE_NAME" sysctl -w net.ipv4.ip_forward=1
    sudo ip netns exec "$NAMESPACE_NAME" iptables -t nat -C POSTROUTING -o tun0 -j MASQUERADE 2>/dev/null || \
       sudo ip netns exec "$NAMESPACE_NAME" iptables -t nat -A POSTROUTING -o tun0 -j MASQUERADE

    # Optional: Verify interface is up
    sudo ip netns exec "$NAMESPACE_NAME" ip addr show tun0

}

# Function to open shell in namespace
open_shell() {
    print_step "Opening shell in namespace..."
    print_status "You are now in the VPN namespace. Type 'exit' to return."
    ip netns exec "$NAMESPACE_NAME" bash
}

# Main execution
main() {
    # Get main interface
    MAIN_IF=$(get_main_interface)
    print_status "Main network interface: $MAIN_IF"
    
    # Parse command line arguments
    COMMAND="setup"
    while [[ $# -gt 0 ]]; do
        case $1 in
            setup|cleanup|status|start-vpn|shell|test)
                COMMAND="$1"
                shift
                ;;
            -c|--config)
                VPN_CONFIG_PATH="$2"
                shift 2
                ;;
            -n|--namespace)
                NAMESPACE_NAME="$2"
                shift 2
                ;;
            -h|--help)
                show_usage
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    # Execute command
    case $COMMAND in
        setup)
            check_root
            cleanup_existing
            create_namespace
            configure_ips
            setup_routing
            configure_iptables
            setup_dns
            test_connectivity
            print_status "VPN namespace setup completed successfully!"
            echo ""
            print_status "Usage examples:"
            echo "  Start VPN: sudo $0 start-vpn"
            echo "  Open shell in namespace: sudo $0 shell"
            echo "  Run command in namespace: sudo ip netns exec $NAMESPACE_NAME <command>"
            echo "  Check your IP: sudo ip netns exec $NAMESPACE_NAME curl ifconfig.me"
            ;;
        cleanup)
            check_root
            cleanup_existing
            print_status "VPN namespace cleanup completed!"
            ;;
        status)
            show_status
            ;;
        start-vpn)
            check_root
            start_vpn
            ;;
        shell)
            check_root
            open_shell
            ;;
        test)
            test_connectivity
            ;;
        *)
            print_error "Unknown command: $COMMAND"
            show_usage
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"
