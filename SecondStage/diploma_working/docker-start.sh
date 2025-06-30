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

    # Check RabbitMQ connectivity
    if [ ! -z "$RABBITMQ_HOST" ] && [ ! -z "$RABBITMQ_PORT" ]; then
        print_status "Checking RabbitMQ connectivity: $RABBITMQ_HOST:$RABBITMQ_PORT"
        if timeout 10 bash -c "</dev/tcp/$RABBITMQ_HOST/$RABBITMQ_PORT"; then
            print_status "✓ RabbitMQ is reachable"
        else
            print_warning "⚠ RabbitMQ is not reachable at $RABBITMQ_HOST:$RABBITMQ_PORT"
            print_warning "  Application will start but message queue integration may fail"
        fi
    fi

    # Check RabbitMQ Management Interface (optional)
    if [ ! -z "$RABBITMQ_HOST" ] && [ ! -z "$RABBITMQ_MANAGEMENT_PORT" ]; then
        print_status "Checking RabbitMQ Management Interface: $RABBITMQ_HOST:$RABBITMQ_MANAGEMENT_PORT"
        if timeout 5 bash -c "</dev/tcp/$RABBITMQ_HOST/$RABBITMQ_MANAGEMENT_PORT"; then
            print_status "✓ RabbitMQ Management Interface is reachable"
        else
            print_warning "⚠ RabbitMQ Management Interface is not reachable"
        fi
    fi

    # Check PostgreSQL connectivity
    if [ ! -z "$DB_URL" ]; then
        DB_HOST=$(echo $DB_URL | sed 's|jdbc:postgresql://||' | cut -d':' -f1)
        DB_PORT=$(echo $DB_URL | sed 's|jdbc:postgresql://||' | cut -d':' -f2 | cut -d'/' -f1)

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
        "DB_URL"
        "DB_USERNAME"
        "DB_PASSWORD"
        "RABBITMQ_HOST"
        "RABBITMQ_USERNAME"
        "RABBITMQ_PASSWORD"
        "RABBITMQ_PORT"
        "AI_API_KEY"
    )

    for var in "${REQUIRED_VARS[@]}"; do
        if [ -z "${!var}" ]; then
            print_error "Required environment variable $var is not set"
            exit 1
        fi
    done

    # Check for sensitive values that shouldn't be defaults
    if [ "$AI_API_KEY" = "your_google_gemini_api_key_here" ]; then
        print_error "AI_API_KEY is set to default placeholder value. Please set a valid API key."
        exit 1
    fi

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
    if [ ! -z "$DB_URL" ]; then
        DB_HOST=$(echo $DB_URL | sed 's|jdbc:postgresql://||' | cut -d':' -f1)
        DB_PORT=$(echo $DB_URL | sed 's|jdbc:postgresql://||' | cut -d':' -f2 | cut -d'/' -f1)

        print_status "Waiting for PostgreSQL at $DB_HOST:$DB_PORT..."
        timeout 60 bash -c 'until printf "" 2>>/dev/null >>/dev/tcp/$0/$1; do sleep 1; done' $DB_HOST $DB_PORT
        print_status "✓ PostgreSQL is ready"
    fi

    # Wait for RabbitMQ with retry
    if [ ! -z "$RABBITMQ_HOST" ] && [ ! -z "$RABBITMQ_PORT" ]; then
        print_status "Waiting for RabbitMQ at $RABBITMQ_HOST:$RABBITMQ_PORT..."
        if ! timeout 30 bash -c 'until printf "" 2>>/dev/null >>/dev/tcp/$0/$1; do sleep 2; done' $RABBITMQ_HOST $RABBITMQ_PORT; then
            print_warning "⚠ RabbitMQ is not ready after 30 seconds, continuing anyway"
        else
            print_status "✓ RabbitMQ is ready"
        fi
    fi
}

# Function to display configuration summary
display_config() {
    print_status "Configuration Summary:"
    echo "  Environment: ${ENVIRONMENT:-docker}"
    echo "  Server Port: ${SERVER_PORT:-8282}"
    echo "  RabbitMQ Host: ${RABBITMQ_HOST:-not set}"
    echo "  RabbitMQ Port: ${RABBITMQ_PORT:-not set}"
    echo "  Queue Name: ${QUEUE_NAME:-not set}"
    echo "  Database: ${DB_URL:-not set}"
    echo "  AI Model: ${AI_MODEL:-not set}"
    echo "  Log Level: ${LOG_LEVEL_APP:-DEBUG}"
    echo "  Java Options: ${JAVA_OPTS:-default}"
}

# Function to test RabbitMQ connection (optional advanced check)
test_rabbitmq_connection() {
    if command -v curl >/dev/null 2>&1; then
        if [ ! -z "$RABBITMQ_HOST" ] && [ ! -z "$RABBITMQ_MANAGEMENT_PORT" ]; then
            print_status "Testing RabbitMQ Management API..."
            RABBITMQ_MGMT_URL="http://${RABBITMQ_HOST}:${RABBITMQ_MANAGEMENT_PORT}/api/overview"
            
            if curl -f -s -u "${RABBITMQ_USERNAME}:${RABBITMQ_PASSWORD}" "$RABBITMQ_MGMT_URL" >/dev/null 2>&1; then
                print_status "✓ RabbitMQ Management API is accessible"
            else
                print_warning "⚠ RabbitMQ Management API is not accessible (this is optional)"
            fi
        fi
    fi
}

# Main execution
main() {
    print_header

    # Validate environment
    validate_environment

    # Create directories
    # create_directories (commented out as in original)

    # Check external services
    check_external_services

    # Wait for services
    wait_for_services

    # Test RabbitMQ connection (optional)
    test_rabbitmq_connection

    # Display configuration
    display_config

    print_status "Starting CI Anomaly Detector API..."

    # Execute the main command
    exec "$@"
}

# Run main function with all arguments
main "$@"
