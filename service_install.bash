#!/bin/bash

services=("api-gateway" "config-server" "discovery-server" "product-service")

for service in "${services[@]}"
do
    echo "Building $service..."
    cd "$service"
    
    mvn clean install
    
    cd ..
done

echo "All services built successfully."