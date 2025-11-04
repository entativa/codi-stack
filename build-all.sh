#!/bin/bash
set -e

echo "======================================"
echo "CodiBase Stack - Build All Components"
echo "======================================"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

# Build Vibecoda
echo -e "${BLUE}Building Vibecoda...${NC}"
cd vibecoda
npm install
npm run build
echo -e "${GREEN}✓ Vibecoda built${NC}"
cd ..

# Build CodiBase
echo -e "${BLUE}Building CodiBase...${NC}"
cd codibase
./gradlew clean build -x test
echo -e "${GREEN}✓ CodiBase built${NC}"
cd ..

# Build PilotCodi
echo -e "${BLUE}Building PilotCodi...${NC}"
cd pilotcodi
cargo build --release
echo -e "${GREEN}✓ PilotCodi built${NC}"
cd ..

# Build Telemetry Service
echo -e "${BLUE}Building Telemetry Service...${NC}"
cd telemetry-service
docker build -t codibase-telemetry:latest .
echo -e "${GREEN}✓ Telemetry Service built${NC}"
cd ..

echo ""
echo -e "${GREEN}======================================"
echo "✓ All components built successfully!"
echo "======================================${NC}"
