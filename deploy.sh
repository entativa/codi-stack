#!/bin/bash
set -e

echo "======================================"
echo "CodiBase Stack - Deploy All Services"
echo "======================================"

# Build all components first
./build-all.sh

# Deploy with docker-compose
echo "Starting services with docker-compose..."
docker-compose up -d

echo ""
echo "✓ All services deployed!"
echo ""
echo "Service URLs:"
echo "  - Telemetry API: http://localhost:8080"
echo "  - CodiBase: http://localhost:6610"
echo "  - PilotCodi: http://localhost:8081"
echo ""
echo "Check status: docker-compose ps"
echo "View logs: docker-compose logs -f"
