#!/bin/bash
set -e

RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m'

echo -e "${GREEN}🔍 Validating OpenAPI contracts with Spectral...${NC}"

# Ejecuta Spectral directamente, exit code se captura automáticamente
npx @stoplight/spectral-cli lint contracts/*.yaml
EXIT_CODE=$?

# Bloquear Maven si hay errores
if [ $EXIT_CODE -ne 0 ]; then
    echo -e "${RED}❌ Contract validation failed. Tests will NOT run.${NC}"
    exit $EXIT_CODE
fi

echo -e "${GREEN}✅ Contracts valid. Running Java API tests...${NC}"
mvn test
