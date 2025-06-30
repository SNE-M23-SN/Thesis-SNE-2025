#!/bin/bash



# Domain-Specific VPN Routing Script

# Routes specific domains through VPN interface only



set -e



# Configuration

DOMAIN="generativelanguage.googleapis.com"

RT_TABLE="vpnroute"

RT_TABLE_ID="100"

NAMESPACE="vpnspace"  # Use with your namespace setup

LOG_FILE="/var/log/domain-vpn-routing.log"



# Colors for output

RED='\033[0;31m'

GREEN='\033[0;32m'

YELLOW='\033[1;33m'

BLUE='\033[0;34m'

NC='\033[0m'



# Logging function

log() {

    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"

}



print_status() {

    echo -e "${GREEN}[INFO]${NC} $1"

    log "INFO: $1"

}



print_warning() {

    echo -e "${YELLOW}[WARNING]${NC} $1"

    log "WARNING: $1"

}



print_error() {

    echo -e "${RED}[ERROR]${NC} $1"

    log "ERROR: $1"

}



print_step() {

    echo -e "${BLUE}[STEP]${NC} $1"

    log "STEP: $1"

}



# Check if running as root

check_root() {

    if [[ $EUID -ne 0 ]]; then

        print_error "This script must be run as root (use sudo)"

        exit 1

    fi

}



# Function to detect VPN interface and gateway

detect_vpn_interface() {

    local vpn_if=""

    local vpn_gw=""

    

    # Method 1: Check for common VPN interfaces

    for iface in tun0 tun1 wg0 ppp0; do

        if ip link show "$iface" &>/dev/null && ip link show "$iface" | grep -q "UP"; then

            vpn_if="$iface"

            vpn_gw=$(ip route | grep "^default.*$vpn_if" | awk '{print $3}' | head -n1)

            print_status "Detected VPN interface: $vpn_if, gateway: ${vpn_gw:-none}"

            echo "direct:$vpn_if:$vpn_gw"

            return 0

        fi

    done

    

    # Method 2: Check in namespace if using namespace setup

    if [[ -z "$vpn_if" ]] && ip netns list | grep -q "$NAMESPACE"; then

        print_status "Checking namespace: $NAMESPACE"

        

        # Get the default route info from namespace

        local ns_route=$(ip netns exec "$NAMESPACE" ip route show default 2>/dev/null | head -n1)

        if [[ -n "$ns_route" ]]; then

            vpn_gw=$(echo "$ns_route" | awk '/^default/ {print $3}')

            vpn_if=$(echo "$ns_route" | awk '/^default/ {print $5}')

            

            if [[ -n "$vpn_if" ]]; then

                print_status "Detected VPN in namespace: $NAMESPACE, interface: $vpn_if, gateway: ${vpn_gw:-none}"

                echo "namespace:$vpn_if:$vpn_gw"

                return 0

            fi

        fi

    fi

    

    # Method 3: Look for interface with default route that's not main interface

    if [[ -z "$vpn_if" ]]; then

        local main_if=$(ip route show default | awk '{print $5}' | head -n1)

        local all_default_routes=$(ip route show default | awk '{print $5}')

        

        for iface in $all_default_routes; do

            if [[ "$iface" != "$main_if" ]]; then

                vpn_if="$iface"

                vpn_gw=$(ip route show default | grep "dev $vpn_if" | awk '{print $3}' | head -n1)

                break

            fi

        done

    fi

    

    if [[ -n "$vpn_if" ]]; then

        print_status "Detected VPN interface: $vpn_if, gateway: ${vpn_gw:-none}"

        echo "direct:$vpn_if:$vpn_gw"

    else

        print_error "No VPN interface detected. Make sure your VPN is connected."

        exit 1

    fi

}



# Function to resolve domain IPs with retries

resolve_domain_ips() {

    local domain="$1"

    local retries=3

    local ips=""

    

    print_step "Resolving IP addresses for $domain..."

    

    for ((i=1; i<=retries; i++)); do

        ips=$(dig +short "$domain" +time=5 +tries=2 2>/dev/null | grep -Eo '([0-9]{1,3}\.){3}[0-9]{1,3}' | sort -u)

        

        if [[ -n "$ips" ]]; then

            print_status "Found IP addresses:"

            echo "$ips" | while read -r ip; do

                echo "  - $ip"

            done

            echo "$ips"

            return 0

        fi

        

        print_warning "Attempt $i failed, retrying..."

        sleep 2

    done

    

    print_error "Failed to resolve $domain after $retries attempts"

    exit 1

}



# Function to setup routing table

setup_routing_table() {

    print_step "Setting up routing table..."

    

    # Add routing table if missing

    if ! grep -q "^$RT_TABLE_ID $RT_TABLE" /etc/iproute2/rt_tables; then

        echo "$RT_TABLE_ID $RT_TABLE" >> /etc/iproute2/rt_tables

        print_status "Added routing table: $RT_TABLE (ID: $RT_TABLE_ID)"

    else

        print_status "Routing table $RT_TABLE already exists"

    fi

}



# Function to cleanup existing rules and routes

cleanup_existing() {

    print_step "Cleaning up existing rules and routes..."

    

    # Remove existing rules for this table

    while ip rule list | grep -q "lookup $RT_TABLE"; do

        local rule_line=$(ip rule list | grep "lookup $RT_TABLE" | head -n1)

        local priority=$(echo "$rule_line" | cut -d: -f1)

        if [[ -n "$priority" ]]; then

            ip rule del priority "$priority" 2>/dev/null || break

        else

            break

        fi

    done

    

    # Flush routing table

    ip route flush table "$RT_TABLE" 2>/dev/null || true

    

    print_status "Cleanup completed"

}



# Function to add routes via VPN

add_vpn_routes() {

    local vpn_info="$1"

    local ips="$2"

    

    local vpn_type=$(echo "$vpn_info" | cut -d: -f1)

    local vpn_if=$(echo "$vpn_info" | cut -d: -f2)

    local vpn_gw=$(echo "$vpn_info" | cut -d: -f3)

    

    print_step "Adding routes via VPN interface $vpn_if..."

    

    # Debug output

    print_status "VPN Type: $vpn_type"

    print_status "VPN Interface: $vpn_if"

    print_status "VPN Gateway: ${vpn_gw:-none}"

    

    # Add default route to VPN table

    if [[ "$vpn_type" == "namespace" ]]; then

        print_status "Setting up namespace routing..."

        # For namespace setup, we need a different approach

        if [[ -n "$vpn_gw" && "$vpn_gw" != "none" ]]; then

            ip route add default via "$vpn_gw" dev "$vpn_if" table "$RT_TABLE" 2>/dev/null || {

                print_warning "Could not add default route via gateway, trying without gateway"

                ip route add default dev "$vpn_if" table "$RT_TABLE" 2>/dev/null || true

            }

        else

            ip route add default dev "$vpn_if" table "$RT_TABLE" 2>/dev/null || true

        fi

    else

        # For direct VPN connection

        print_status "Setting up direct VPN routing..."

        if [[ -n "$vpn_gw" && "$vpn_gw" != "none" ]]; then

            ip route add default via "$vpn_gw" dev "$vpn_if" table "$RT_TABLE" 2>/dev/null || {

                print_warning "Could not add default route via gateway, trying without gateway"

                ip route add default dev "$vpn_if" table "$RT_TABLE" 2>/dev/null || true

            }

        else

            ip route add default dev "$vpn_if" table "$RT_TABLE" 2>/dev/null || true

        fi

    fi

    

    # Add specific routes for domain IPs

    local rule_priority=100

    local ip_array=()

    while IFS= read -r ip; do

        [[ -n "$ip" ]] && ip_array+=("$ip")

    done <<< "$ips"

    

    for ip in "${ip_array[@]}"; do
     print_status "Adding route for $ip"

     # Add rule to use our custom table for this IP
     ip rule add to "$ip" lookup "$RT_TABLE" priority "$rule_priority" 2>/dev/null || {
         print_warning "Rule for $ip already exists or failed to add"
     }

     # Instead of trying to reach tun0 (not accessible in host), route via vpnspace gateway
     ip route add "$ip" via 10.200.1.2 dev veth0 table "$RT_TABLE" 2>/dev/null || {
        print_warning "Could not add route for $ip via veth0"
     }

     rule_priority=$((rule_priority + 1))
    done

    

    print_status "Routes added successfully"

}



# Function to test routing

test_routing() {

    local ips="$1"

    

    print_step "Testing routing..."

    

    # Convert to array to avoid subshell issues

    local ip_array=()

    while IFS= read -r ip; do

        [[ -n "$ip" ]] && ip_array+=("$ip")

    done <<< "$ips"

    

    # Test first IP

    if [[ ${#ip_array[@]} -gt 0 ]]; then

        local test_ip="${ip_array[0]}"

        print_status "Testing route to $test_ip..."

        

        # Show which interface will be used

        local route_info=$(ip route get "$test_ip" 2>/dev/null || echo "No route found")

        print_status "Route: $route_info"

        

        # Test connectivity

        if timeout 10 ping -c 1 -W 5 "$test_ip" &>/dev/null; then

            print_status "✓ Connectivity test passed"

        else

            print_warning "✗ Connectivity test failed (might be normal if server doesn't respond to ping)"

        fi

    else

        print_error "No IPs to test"

    fi

}



# Function to show debug information

show_debug() {

    print_step "Debug information:"

    

    echo "Domain: $DOMAIN"

    echo "Routing table: $RT_TABLE"

    echo "Namespace: $NAMESPACE"

    echo ""

    

    # Test DNS resolution

    print_step "Testing DNS resolution..."

    local ips=$(dig +short "$DOMAIN" 2>/dev/null | grep -Eo '([0-9]{1,3}\.){3}[0-9]{1,3}')

    if [[ -n "$ips" ]]; then

        echo "Resolved IPs:"

        echo "$ips" | while read -r ip; do

            echo "  - $ip"

        done

    else

        echo "Failed to resolve $DOMAIN"

    fi

    echo ""

    

    # Show network interfaces

    print_step "Network interfaces:"

    ip link show | grep -E '^[0-9]+:|UP'

    echo ""

    

    # Show routing tables

    print_step "Current routing tables:"

    echo "Main table:"

    ip route show table main | head -5

    echo ""

    echo "Custom table ($RT_TABLE):"

    ip route show table "$RT_TABLE" 2>/dev/null || echo "  (empty or doesn't exist)"

    echo ""

    

    # Show IP rules

    print_step "IP rules:"

    ip rule list

    echo ""

    

    # Check namespace

    if ip netns list | grep -q "$NAMESPACE"; then

        print_step "Namespace $NAMESPACE exists:"

        echo "Routes in namespace:"

        ip netns exec "$NAMESPACE" ip route show 2>/dev/null || echo "  (failed to access)"

    else

        echo "Namespace $NAMESPACE does not exist"

    fi

}

show_status() {

    print_step "Current routing status for $DOMAIN:"

    

    # Show domain IPs

    local ips=$(resolve_domain_ips "$DOMAIN")

    

    # Show routing table

    echo "Routing table $RT_TABLE:"

    ip route show table "$RT_TABLE" 2>/dev/null || echo "  (empty)"

    echo ""

    

    # Show rules

    echo "IP rules for $RT_TABLE:"

    ip rule list | grep "$RT_TABLE" || echo "  (none)"

    echo ""

    

    # Test each IP - fix subshell issue

    echo "Routes for domain IPs:"

    local ip_array=()

    while IFS= read -r ip; do

        [[ -n "$ip" ]] && ip_array+=("$ip")

    done <<< "$ips"

    

    for ip in "${ip_array[@]}"; do

        local route=$(ip route get "$ip" 2>/dev/null | head -n1)

        echo "  $ip: $route"

    done

}



# Function to show usage

show_usage() {

    echo "Usage: $0 [COMMAND] [OPTIONS]"

    echo ""

    echo "Commands:"

    echo "  setup     Set up domain routing through VPN (default)"

    echo "  cleanup   Remove domain routing rules"

    echo "  status    Show current routing status"

    echo "  test      Test routing for domain"

    echo "  debug     Show debug information"

    echo ""

    echo "Options:"

    echo "  -d, --domain DOMAIN    Domain to route (default: $DOMAIN)"

    echo "  -t, --table TABLE      Routing table name (default: $RT_TABLE)"

    echo "  -n, --namespace NS     VPN namespace name (default: $NAMESPACE)"

    echo "  -h, --help             Show this help"

}



# Main function

main() {

    local command="setup"

    

    # Parse arguments

    while [[ $# -gt 0 ]]; do

        case $1 in

            setup|cleanup|status|test|debug)

                command="$1"

                shift

                ;;

            -d|--domain)

                DOMAIN="$2"

                shift 2

                ;;

            -t|--table)

                RT_TABLE="$2"

                shift 2

                ;;

            -n|--namespace)

                NAMESPACE="$2"

                shift 2

                ;;

            -h|--help)

                show_usage

                exit 0

                ;;

            debug)

            show_debug

            ;;

        *)

                print_error "Unknown option: $1"

                show_usage

                exit 1

                ;;

        esac

    done

    

    # Execute command

    case $command in

        setup)

            check_root

            local vpn_info=$(detect_vpn_interface)

            local ips=$(resolve_domain_ips "$DOMAIN")

            setup_routing_table

            cleanup_existing

            add_vpn_routes "$vpn_info" "$ips"

            test_routing "$ips"

            print_status "Domain routing setup completed!"

            print_status "Domain $DOMAIN will now use VPN for all connections"

            ;;

        cleanup)

            check_root

            cleanup_existing

            print_status "Domain routing rules removed"

            ;;

        status)

            show_status

            ;;

        test)

            print_step "Running connectivity test for $DOMAIN"

            local ips=$(resolve_domain_ips "$DOMAIN")

            if [[ -n "$ips" ]]; then

                test_routing "$ips"

            else

                print_error "Could not resolve domain $DOMAIN"

                exit 1

            fi

            ;;

        *)

            print_error "Unknown command: $command"

            show_usage

            exit 1

            ;;

    esac

}



# Run main function

main "$@"
