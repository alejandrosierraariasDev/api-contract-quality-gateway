# 🔐 API Contract Quality Gateway

[![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0.3-green?logo=openapis&logoColor=white)](https://www.openapis.org/)
[![Spectral](https://img.shields.io/badge/Spectral-Linting-blue?logo=stoplight&logoColor=white)](https://stoplight.io/open-source/spectral/)
[![Java](https://img.shields.io/badge/Java-17-orange?logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-red?logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![JUnit5](https://img.shields.io/badge/JUnit5-5.10.2-green?logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![RestAssured](https://img.shields.io/badge/RestAssured-5.4.0-blue?logo=rest&logoColor=white)](https://rest-assured.io/)
[![Allure](https://img.shields.io/badge/Allure-2.27.0-purple?logo=allure&logoColor=white)](https://docs.qameta.io/allure/)

## 📋 Overview

**API Contract Quality Gateway** validates OpenAPI contracts so that API specifications are consistent and of good quality before any functional test runs.

The project implements a **blocking validation flow**:
1. ✅ OpenAPI contracts are validated against a specific Spectral ruleset
2. 🚫 If validation fails, functional tests **do NOT run**
3. ✔️ If validation passes, functional tests run automatically

---

## 🎯 Main goal

Make sure every OpenAPI contract meets the **quality rules defined in the Spectral ruleset**, ensuring:
- Well-documented, consistent contracts
- Operations with unique identifiers
- Standard error responses defined
- Explicit schemas in every response
- Reliable traceability and client generation

---

## 📐 Spectral validation rules

The project uses a custom ruleset (`spectral/ruleset.yaml`) that extends the standard OpenAPI rules. The main rules are:

### 🔴 Error rules (blocking)

| Rule | Description | Impact |
|-------|-------------|--------|
| **operation-id-required** | Every HTTP operation must define a unique, explicit `operationId` | Required for traceability and client generation |
| **responses-must-be-defined** | Every operation must define at least one `responses` block | Prevents incomplete contracts |
| **success-response-must-have-schema** | Successful (2xx) responses must define an explicit `schema` | Prevents ambiguous, unstable contracts |
| **no-200-without-body** | HTTP 200 without `content` is not allowed; use 204 when there is no body | Clear specifications |
| **error-responses-must-be-defined** | Every operation must define error responses (4xx/5xx) | Consistent error handling |

### ⚠️ Warning rules (non-blocking)

| Rule | Description |
|-------|-------------|
| **operation-summary-required** | Every operation must have a clear `summary` |
| **operation-description-required** | Every operation must have a detailed `description` |

---

## 📁 Project structure

```
api-contract-quality-gateway/
├── contracts/                      # OpenAPI contracts to validate
│   ├── meetTheRules.yaml          # ✅ Contract that PASSES validation
│   └── noMeetTheRules.yaml        # ❌ Contract that FAILS validation
├── spectral/
│   └── ruleset.yaml               # Custom Spectral rules
├── .spectral.yaml                 # Spectral configuration
├── src/test/java/                 # Functional tests (JUnit5 + RestAssured)
├── scripts/
│   └── validateAndExecute.sh       # Validation and execution script
├── pom.xml                         # Maven configuration
├── .env                            # Environment variables (local)
└── README.md                       # This file
```

---

## 🔍 Example contracts

### ✅ meetTheRules.yaml (VALID)

Meets every Spectral rule:
- ✓ Operation with `operationId: getUserById`
- ✓ `summary` and `description` defined
- ✓ Responses (200, 400, 404, 500) with explicit schemas
- ✓ Error schemas defined in `components`

```yaml
paths:
  /users/{id}:
    get:
      operationId: getUserById
      summary: Obtener usuario por id
      description: Devuelve el detalle de un usuario existente...
      responses:
        "200":
          description: Usuario encontrado
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/User"
        "400":
          description: Petición inválida
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/Error"
```

### ❌ noMeetTheRules.yaml (INVALID)

Breaks several Spectral rules:
- ✗ Missing `operationId`
- ✗ Missing `summary` and `description`
- ✗ 200 response without `schema`
- ✗ Missing error responses (400, 500)

```yaml
paths:
  /users/{id}:
    get:
      responses:
        "200":
          description: Usuario encontrado
          content:
            application/json:
              # ❌ schema MISSING here
```

---

## 🚀 Script validateAndExecute.sh

The `scripts/validateAndExecute.sh` script orchestrates the whole flow:

```bash
#!/bin/bash
set -e

# 1️⃣ Run Spectral to validate the contracts
npx @stoplight/spectral-cli lint contracts/*.yaml
EXIT_CODE=$?

# 2️⃣ If there are errors, BLOCK the run
if [ $EXIT_CODE -ne 0 ]; then
    echo "❌ Contract validation failed. Tests will NOT run."
    exit $EXIT_CODE
fi

# 3️⃣ If everything is valid, run the functional tests
echo "✅ Contracts valid. Running Java API tests..."
mvn test
```

### Execution flow

```
┌─────────────────────────────────────┐
│  validateAndExecute.sh              │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│  Spectral Lint (contracts/*.yaml)   │
│  - Validates against ruleset.yaml   │
│  - Checks every rule                │
└────────────┬────────────────────────┘
             │
      ┌──────┴──────┐
      │             │
      ▼             ▼
   ❌ ERROR      ✅ VALID 
   (EXIT 1)     (EXIT 0)
      │             │
      ▼             ▼
   STOP         ┌─────────────────────────┐
                │  mvn test               │
                │  - JUnit5 Tests         │
                │  - RestAssured Requests │
                │  - Allure Reports       │
                └─────────────────────────┘
```

---

## 📋 How to run the project

### 1️⃣ **Install dependencies**

```bash
# Install Maven dependencies
mvn clean install

# Install the Spectral CLI (if not installed)
npm install -g @stoplight/spectral-cli
```

### 2️⃣ **Configure the environment**

```bash

# Edit .env with your values at the project root
# BASE_URL=https://your-api.com
```

### 3️⃣ **Validate contracts**

```bash
# Validate contracts manually
npx @stoplight/spectral-cli lint contracts/*.yaml

# Validate a specific contract
npx @stoplight/spectral-cli lint contracts/meetTheRules.yaml
```

### 4️⃣ **Full run (validation + tests)**

```bash
# Run the full script
bash scripts/validateAndExecute.sh

# Or run Maven directly (without prior validation)
mvn test
```

### 5️⃣ **Generate Allure reports**

```bash
# Reports are generated automatically in target/allure-report
# Open in a browser
open target/allure-report/index.html
```

---

## 🛠️ Requirements and setup

### Quick install

```bash
# 1. Install Maven dependencies
mvn clean install

# 2. Install the Spectral CLI
npm install -g @stoplight/spectral-cli

# 3. Configure environment variables
cp .env.example .env

# 4. Run validation + tests
bash scripts/validateAndExecute.sh
```

---



## 🐛 Troubleshooting

### Error: "Could not find ./.env"
```bash
# Fix: copy the template
cp .env.example .env
```

### Error: "spectral: command not found"
```bash
# Fix: install Spectral globally
npm install -g @stoplight/spectral-cli
```

### Error: "Maven not found"
```bash
# Fix: install Maven or add it to PATH
# On macOS with Homebrew:
brew install maven
```

### Tests fail but validation passed
```bash
# Check that BASE_URL in .env is correct
cat .env

# Run with detailed logs
mvn test -X
```

---

## 📚 Further reading

- [OpenAPI 3.0.3 Spec](https://spec.openapis.org/oas/v3.0.3)
- [Spectral Documentation](https://docs.stoplight.io/docs/spectral/)
- [RestAssured Guide](https://rest-assured.io/)
- [Allure Reports](https://docs.qameta.io/allure/)
- [JUnit5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

---
