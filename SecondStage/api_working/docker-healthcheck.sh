#!/bin/bash

# =============================================================================
# CI ANOMALY DETECTOR - DOCKER HEALTH CHECK SCRIPT
# =============================================================================

set -e

# Configuration
HEALTH_ENDPOINT="http://localhost:${SERVER_PORT:-8282}/actuator/health"
TIMEOUT=10
MAX_RETRIES=3

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() {
    echo -e "${GREEN}[HEALTH CHECK]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[HEALTH CHECK]${NC} $1"
}

print_error() {
    echo -e "${RED}[HEALTH CHECK]${NC} $1"
}

# Function to check application health
check_application_health() {
    local retry_count=0
    
    while [ $retry_count -lt $MAX_RETRIES ]; do
        # Check if the application is responding
        if curl -f -s --max-time $TIMEOUT "$HEALTH_ENDPOINT" > /dev/null 2>&1; then
            # Get detailed health information
            HEALTH_RESPONSE=$(curl -s --max-time $TIMEOUT "$HEALTH_ENDPOINT" 2>/dev/null)
            
            if [ $? -eq 0 ]; then
                # Parse health status
                STATUS=$(echo "$HEALTH_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
                
                if [ "$STATUS" = "UP" ]; then
                    print_success "Application is healthy (status: UP)"
                    return 0
                else
                    print_warning "Application status: $STATUS"
                    print_warning "Health response: $HEALTH_RESPONSE"
                fi
            fi
        fi
        
        retry_count=$((retry_count + 1))
        if [ $retry_count -lt $MAX_RETRIES ]; then
            print_warning "Health check failed (attempt $retry_count/$MAX_RETRIES), retrying in 2 seconds..."
            sleep 2
        fi
    done
    
    print_error "Application health check failed after $MAX_RETRIES attempts"
    return 1
}

# Function to check database connectivity
check_database_connectivity() {
    # This is a basic check - the application health endpoint should include database status
    local db_check_endpoint="http://localhost:${SERVER_PORT:-8282}/actuator/health/db"
    
    if curl -f -s --max-time $TIMEOUT "$db_check_endpoint" > /dev/null 2>&1; then
        DB_RESPONSE=$(curl -s --max-time $TIMEOUT "$db_check_endpoint" 2>/dev/null)
        DB_STATUS=$(echo "$DB_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
        
        if [ "$DB_STATUS" = "UP" ]; then
            print_success "Database connectivity is healthy"
            return 0
        else
            print_warning "Database status: $DB_STATUS"
        fi
    fi
    
    print_warning "Database connectivity check inconclusive"
    return 0  # Don't fail the health check for database issues
}

# Function to check Jenkins connectivity
check_jenkins_connectivity() {
    # Check if Jenkins integration is working
    local jenkins_endpoint="http://localhost:${SERVER_PORT:-8282}/api/dashboard/jobs"
    
    if curl -f -s --max-time $TIMEOUT "$jenkins_endpoint" > /dev/null 2>&1; then
        print_success "Jenkins integration is responding"
        return 0
    else
        print_warning "Jenkins integration check failed (this may be expected if Jenkins is not available)"
        return 0  # Don't fail the health check for Jenkins issues
    fi
}

# Function to check memory usage
check_memory_usage() {
    if command -v free >/dev/null 2>&1; then
        MEMORY_INFO=$(free -m)
        MEMORY_USAGE=$(echo "$MEMORY_INFO" | awk 'NR==2{printf "%.1f%%", $3*100/$2}')
        print_success "Memory usage: $MEMORY_USAGE"
    fi
}

# Function to check disk space
check_disk_space() {
    if command -v df >/dev/null 2>&1; then
        DISK_USAGE=$(df -h /app | awk 'NR==2{print $5}')
        print_success "Disk usage: $DISK_USAGE"
        
        # Warning if disk usage is high
        DISK_PERCENT=$(echo $DISK_USAGE | sed 's/%//')
        if [ "$DISK_PERCENT" -gt 90 ]; then
            print_warning "High disk usage detected: $DISK_USAGE"
        fi
    fi
}

# Main health check function
main() {
    echo "==================================="
    echo "CI Anomaly Detector Health Check"
    echo "==================================="
    echo "Endpoint: $HEALTH_ENDPOINT"
    echo "Timeout: ${TIMEOUT}s"
    echo "Max Retries: $MAX_RETRIES"
    echo "==================================="
    
    # Primary health check
    if ! check_application_health; then
        exit 1
    fi
    
    # Additional checks (non-critical)
    check_database_connectivity
    check_jenkins_connectivity
    check_memory_usage
    check_disk_space
    
    print_success "All health checks completed successfully"
    exit 0
}

# Run main function
main "$@"
