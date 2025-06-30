#!/bin/bash

# Script to generate TLS certificates for Docker remote connections
# Creates server_cert and client_cert folders and organizes certificates accordingly
# Externalizes server hostname and IP address as variables

# Exit on any error
set -e

# Define default values for externalized variables
# SERVER_HOSTNAME: Replace with your actual server hostname (e.g., docker.example.com)
# SERVER_IP: Replace with your server's IP address (e.g., 192.168.1.10)
SERVER_HOSTNAME=${SERVER_HOSTNAME:-"your-server-hostname"}
SERVER_IP=${SERVER_IP:-"158.160.166.41"}

# Define directories for server and client certificates
SERVER_DIR="server_cert"
CLIENT_DIR="client_cert"
RESOURCES_DIR="src/main/resources/cert"

# Create directories if they don't exist
echo "Creating directories for certificates..."
mkdir -p "$SERVER_DIR" "$CLIENT_DIR"

# Change to a temporary working directory (current directory by default)
WORK_DIR=$(pwd)
cd "$WORK_DIR"

# Step 1: Generate CA certificates (stored in the current directory)
echo "Generating CA private key and certificate..."
openssl genrsa -out ca-key.pem 4096
openssl req -new -x509 -days 365 -key ca-key.pem -out ca.pem -subj "/CN=CA" -nodes

# Step 2: Generate Server certificates (stored in server_cert folder)
echo "Generating server certificates in $SERVER_DIR for $SERVER_HOSTNAME ($SERVER_IP)..."
cd "$SERVER_DIR"

# Generate server private key
openssl genrsa -out server-key.pem 4096

# Generate server certificate signing request (CSR)
# Uses the externalized SERVER_HOSTNAME variable
openssl req -subj "/CN=$SERVER_HOSTNAME" -new -key server-key.pem -out server.csr -nodes

# Create extfile with subjectAltName using externalized SERVER_HOSTNAME and SERVER_IP
echo "subjectAltName = DNS:$SERVER_HOSTNAME,IP:$SERVER_IP" > extfile.cnf

# Sign the server certificate with the CA
openssl x509 -req -days 365 -in server.csr -CA "$WORK_DIR/ca.pem" -CAkey "$WORK_DIR/ca-key.pem" -CAcreateserial -out server-cert.pem -extfile extfile.cnf

# Step 3: Generate Client certificates (stored in client_cert folder)
echo "Generating client certificates in $CLIENT_DIR..."
cd "$WORK_DIR/$CLIENT_DIR"

# Generate client private key
openssl genrsa -out client-key.pem 4096

# Generate client certificate signing request (CSR)
openssl req -subj "/CN=client" -new -key client-key.pem -out client.csr -nodes

# Create extfile for client with extendedKeyUsage
echo "extendedKeyUsage = clientAuth" > extfile-client.cnf

# Sign the client certificate with the CA
openssl x509 -req -days 365 -in client.csr -CA "$WORK_DIR/ca.pem" -CAkey "$WORK_DIR/ca-key.pem" -CAcreateserial -out client-cert.pem -extfile extfile-client.cnf

# Step 4: Set appropriate permissions for all generated files
echo "Setting file permissions..."
cd "$WORK_DIR"
chmod 0400 ca-key.pem "$SERVER_DIR/server-key.pem" "$CLIENT_DIR/client-key.pem"
chmod 0444 ca.pem "$SERVER_DIR/server-cert.pem" "$CLIENT_DIR/client-cert.pem"

# Step 5: Clean up temporary CSR files (optional)
echo "Cleaning up temporary files..."
rm -f "$SERVER_DIR/server.csr" "$CLIENT_DIR/client.csr" "$SERVER_DIR/extfile.cnf" "$CLIENT_DIR/extfile-client.cnf"

# Step 6: Copy client certificates into src/main/resources/cert for Java application
echo "Copying client certificates into $RESOURCES_DIR for Java application..."
# Create the resources directory if it doesn't exist
mkdir -p "$RESOURCES_DIR"
# Copy the files with error checking
cp "./ca.pem" "$RESOURCES_DIR/ca.pem" || { echo "Failed to copy ca.pem"; exit 1; }
cp "$CLIENT_DIR/client-cert.pem" "$RESOURCES_DIR/cert.pem" || { echo "Failed to copy client-cert.pem"; exit 1; }
cp "$CLIENT_DIR/client-key.pem" "$RESOURCES_DIR/key.pem" || { echo "Failed to copy client-key.pem"; exit 1; }

echo "Certificate generation complete!"
echo "CA files are in: $WORK_DIR"
echo "Server certificates are in: $WORK_DIR/$SERVER_DIR (for $SERVER_HOSTNAME, $SERVER_IP)"
echo "Client certificates are in: $WORK_DIR/$CLIENT_DIR"
echo "Client certificates copied to: $WORK_DIR/$RESOURCES_DIR"
