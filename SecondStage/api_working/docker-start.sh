#!/bin/bash

# =============================================================================
# CI ANOMALY DETECTOR - DOCKER STARTUP SCRIPT
# =============================================================================

set -e

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

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE} CI ANOMALY DETECTOR STARTUP${NC}"
    echo -e "${BLUE}================================${NC}"
}

# Function to check if external services are available
check_external_services() {
    print_status "Checking external services..."

    # Check Jenkins connectivity
    if [ ! -z "$JENKINS_URL" ]; then
        JENKINS_HOST=$(echo $JENKINS_URL | sed 's|http[s]*://||' | cut -d':' -f1)
        JENKINS_PORT=$(echo $JENKINS_URL | sed 's|http[s]*://||' | cut -d':' -f2 | cut -d'/' -f1)

        if [ "$JENKINS_PORT" = "$JENKINS_HOST" ]; then
            JENKINS_PORT=80
            if [[ $JENKINS_URL == https* ]]; then
                JENKINS_PORT=443
            fi
        fi

        print_status "Checking Jenkins connectivity: $JENKINS_HOST:$JENKINS_PORT"
        if timeout 10 bash -c "</dev/tcp/$JENKINS_HOST/$JENKINS_PORT"; then
            print_status "✓ Jenkins is reachable"
        else
            print_warning "⚠ Jenkins is not reachable at $JENKINS_URL"
            print_warning "  Application will start but Jenkins integration may fail"
        fi
    fi

    # Check PostgreSQL connectivity
    if [ ! -z "$DATABASE_URL" ]; then
        DB_HOST=$(echo $DATABASE_URL | sed 's|jdbc:postgresql://||' | cut -d':' -f1)
        DB_PORT=$(echo $DATABASE_URL | sed 's|jdbc:postgresql://||' | cut -d':' -f2 | cut -d'/' -f1)

        print_status "Checking PostgreSQL connectivity: $DB_HOST:$DB_PORT"
        if timeout 10 bash -c "</dev/tcp/$DB_HOST/$DB_PORT"; then
            print_status "✓ PostgreSQL is reachable"
        else
            print_error "✗ PostgreSQL is not reachable"
            print_error "  Application startup will likely fail"
            exit 1
        fi
    fi
}

# Function to validate environment variables
validate_environment() {
    print_status "Validating environment variables..."

    # Required variables
    REQUIRED_VARS=(
        "DATABASE_URL"
        "DATABASE_USERNAME"
        "DATABASE_PASSWORD"
        "JENKINS_URL"
        "JENKINS_USERNAME"
        "JENKINS_PASSWORD"
    )

    for var in "${REQUIRED_VARS[@]}"; do
        if [ -z "${!var}" ]; then
            print_error "Required environment variable $var is not set"
            exit 1
        fi
    done

    print_status "✓ All required environment variables are set"
}

# Function to create necessary directories
create_directories() {
    print_status "Creating necessary directories..."

    mkdir -p /app/logs /app/config 

    # Set proper permissions
    chmod 755 /app/logs /app/config 

    print_status "✓ Directories created successfully"
}

# Function to wait for external services
wait_for_services() {
    print_status "Waiting for external services to be ready..."

    # Wait for PostgreSQL
    if [ ! -z "$DATABASE_URL" ]; then
        DB_HOST=$(echo $DATABASE_URL | sed 's|jdbc:postgresql://||' | cut -d':' -f1)
        DB_PORT=$(echo $DATABASE_URL | sed 's|jdbc:postgresql://||' | cut -d':' -f2 | cut -d'/' -f1)

        print_status "Waiting for PostgreSQL at $DB_HOST:$DB_PORT..."
        timeout 60 bash -c 'until printf "" 2>>/dev/null >>/dev/tcp/$0/$1; do sleep 1; done' $DB_HOST $DB_PORT
        print_status "✓ PostgreSQL is ready"
    fi

    # Wait for Jenkins with retry
    if [ ! -z "$JENKINS_URL" ]; then
        JENKINS_HOST=$(echo $JENKINS_URL | sed 's|http[s]*://||' | cut -d':' -f1)
        JENKINS_PORT=$(echo $JENKINS_URL | sed 's|http[s]*://||' | cut -d':' -f2 | cut -d'/' -f1)

        if [ "$JENKINS_PORT" = "$JENKINS_HOST" ]; then
            JENKINS_PORT=80
            if [[ $JENKINS_URL == https* ]]; then
                JENKINS_PORT=443
            fi
        fi

        print_status "Waiting for Jenkins at $JENKINS_HOST:$JENKINS_PORT..."
        if ! timeout 30 bash -c 'until printf "" 2>>/dev/null >>/dev/tcp/$0/$1; do sleep 2; done' $JENKINS_HOST $JENKINS_PORT; then
            print_warning "⚠ Jenkins is not ready after 30 seconds, continuing anyway"
        else
            print_status "✓ Jenkins is ready"
        fi
    fi
}

# Function to display configuration summary
display_config() {
    print_status "Configuration Summary:"
    echo "  Environment: ${ENVIRONMENT:-docker}"
    echo "  Server Port: ${SERVER_PORT:-8282}"
    echo "  Jenkins URL: ${JENKINS_URL:-not set}"
    echo "  Database: ${DATABASE_URL:-not set}"
    echo "  Log Level: ${LOG_LEVEL_APP:-DEBUG}"
    echo "  CORS Origins: ${CORS_ALLOWED_ORIGINS:-*}"
    echo "  Java Options: ${JAVA_OPTS:-default}"
}

# Main execution
main() {
    print_header

    # Validate environment
    validate_environment

    # Create directories
#    create_directories

    # Check external services
    check_external_services

    # Wait for services
    wait_for_services

    # Display configuration
    display_config

    print_status "Starting CI Anomaly Detector API..."

    # Execute the main command
    exec "$@"
}

# Run main function with all arguments
main "$@"
