#!/bin/bash

# Define the path to ip_addresses.txt file
IP_FILE="ip_addresses.txt"

# Check if the file exists
if [ -f "$IP_FILE" ]; then
    # Read IP addresses from ip_addresses.txt
    while IFS='=' read -r key value; do
        # Export each variable to ~/.bashrc
        echo "export $key=$value" >> ~/.bashrc
    done < "$IP_FILE"

    echo "IP addresses exported to ~/.bashrc"
else
    echo "Error: ip_addresses.txt not found."
fi

# Source ~/.bashrc to apply changes immediately
source ~/.bashrc

