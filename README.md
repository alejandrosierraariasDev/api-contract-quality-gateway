# 🔐 API Contract Quality Gateway

[![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0.3-green?logo=openapis&logoColor=white)](https://www.openapis.org/)
[![Spectral](https://img.shields.io/badge/Spectral-Linting-blue?logo=stoplight&logoColor=white)](https://stoplight.io/open-source/spectral/)
[![Java](https://img.shields.io/badge/Java-17-orange?logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-red?logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![JUnit5](https://img.shields.io/badge/JUnit5-5.10.2-green?logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![RestAssured](https://img.shields.io/badge/RestAssured-5.4.0-blue?logo=rest&logoColor=white)](https://rest-assured.io/)
[![Allure](https://img.shields.io/badge/Allure-2.27.0-purple?logo=allure&logoColor=white)](https://docs.qameta.io/allure/)

## 📋 Descripción

**API Contract Quality Gateway** es una solución de validación de contratos OpenAPI que garantiza la calidad y consistencia de las especificaciones API antes de ejecutar pruebas funcionales.

El proyecto implementa un **flujo de validación bloqueante** donde:
1. ✅ Se validan los contratos OpenAPI contra reglas Spectral específicas
2. 🚫 Si la validación falla, las pruebas funcionales **NO se ejecutan**
3. ✔️ Si la validación pasa, se ejecutan las pruebas funcionales automáticamente

---

## 🎯 Misión Principal

Validar que todos los contratos OpenAPI cumplan con las **normas de calidad definidas en el ruleset de Spectral**, asegurando:
- Contratos bien documentados y consistentes
- Operaciones con identificadores únicos
- Respuestas de error estándar definidas
- Schemas explícitos en todas las respuestas
- Trazabilidad y generación de clientes confiable

---

## 📐 Reglas de Validación Spectral

El proyecto utiliza un ruleset personalizado (`spectral/ruleset.yaml`) que extiende las reglas estándar de OpenAPI. Las reglas principales son:

### 🔴 Reglas de Error (Bloqueantes)

| Regla | Descripción | Impacto |
|-------|-------------|--------|
| **operation-id-required** | Todas las operaciones HTTP deben definir un `operationId` único y explícito | Obligatorio para trazabilidad y generación de clientes |
| **responses-must-be-defined** | Todas las operaciones deben definir al menos un bloque `responses` | Evita contratos incompletos |
| **success-response-must-have-schema** | Las respuestas exitosas (2xx) deben definir un `schema` explícito | Evita contratos ambiguos e inestables |
| **no-200-without-body** | No se permite HTTP 200 sin `content`. Usar 204 si no hay body | Claridad en la especificación |
| **error-responses-must-be-defined** | Todas las operaciones deben definir respuestas de error (4xx/5xx) | Manejo consistente de errores |

### ⚠️ Reglas de Advertencia (No Bloqueantes)

| Regla | Descripción |
|-------|-------------|
| **operation-summary-required** | Cada operación debe tener un `summary` claro |
| **operation-description-required** | Cada operación debe tener una `description` detallada |

---

## 📁 Estructura del Proyecto

```
api-contract-quality-gateway/
├── contracts/                      # Contratos OpenAPI para validación
│   ├── meetTheRules.yaml          # ✅ Contrato que PASA validación
│   └── noMeetTheRules.yaml        # ❌ Contrato que FALLA validación
├── spectral/
│   └── ruleset.yaml               # Reglas personalizadas de Spectral
├── .spectral.yaml                 # Configuración de Spectral
├── src/test/java/                 # Pruebas funcionales (JUnit5 + RestAssured)
├── scripts/
│   └── validateAndExecute.sh       # Script de validación y ejecución
├── pom.xml                         # Configuración Maven
├── .env                            # Variables de entorno (local)
└── README.md                       # Este archivo
```

---

## 🔍 Contratos de Ejemplo

### ✅ meetTheRules.yaml (VÁLIDO)

Cumple con todas las reglas de Spectral:
- ✓ Operación con `operationId: getUserById`
- ✓ `summary` y `description` definidos
- ✓ Respuestas (200, 400, 404, 500) con schemas explícitos
- ✓ Schemas de error definidos en `components`

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

### ❌ noMeetTheRules.yaml (INVÁLIDO)

Viola varias reglas de Spectral:
- ✗ Falta `operationId`
- ✗ Falta `summary` y `description`
- ✗ Respuesta 200 sin `schema`
- ✗ Faltan respuestas de error (400, 500)

```yaml
paths:
  /users/{id}:
    get:
      responses:
        "200":
          description: Usuario encontrado
          content:
            application/json:
              # ❌ FALTA schema aquí
```

---

## 🚀 Script validateAndExecute.sh

El script `scripts/validateAndExecute.sh` orquesta el flujo completo:

```bash
#!/bin/bash
set -e

# 1️⃣ Ejecuta Spectral para validar contratos
npx @stoplight/spectral-cli lint contracts/*.yaml
EXIT_CODE=$?

# 2️⃣ Si hay errores, BLOQUEA la ejecución
if [ $EXIT_CODE -ne 0 ]; then
    echo "❌ Contract validation failed. Tests will NOT run."
    exit $EXIT_CODE
fi

# 3️⃣ Si todo es válido, ejecuta pruebas funcionales
echo "✅ Contracts valid. Running Java API tests..."
mvn test
```

### Flujo de Ejecución

```
┌─────────────────────────────────────┐
│  validateAndExecute.sh              │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│  Spectral Lint (contracts/*.yaml)   │
│  - Valida contra ruleset.yaml       │
│  - Verifica todas las reglas        │
└────────────┬────────────────────────┘
             │
      ┌──────┴──────┐
      │             │
      ▼             ▼
   ❌ ERROR      ✅ VÁLIDO
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

## 📋 Pasos de Ejecución del Proyecto

### 1️⃣ **Instalación de Dependencias**

```bash
# Instalar dependencias Maven
mvn clean install

# Instalar Spectral CLI (si no está instalado)
npm install -g @stoplight/spectral-cli
```

### 2️⃣ **Configuración del Entorno**

```bash

# Editar .env con tus valores en la raiz del proyecto
# BASE_URL=https://tu-api.com
```

### 3️⃣ **Validación de Contratos**

```bash
# Validar contratos manualmente
npx @stoplight/spectral-cli lint contracts/*.yaml

# Validar un contrato específico
npx @stoplight/spectral-cli lint contracts/meetTheRules.yaml
```

### 4️⃣ **Ejecución Completa (Validación + Tests)**

```bash
# Ejecutar el script completo
bash scripts/validateAndExecute.sh

# O ejecutar Maven directamente (sin validación previa)
mvn test
```

### 5️⃣ **Generar Reportes Allure**

```bash
# Los reportes se generan automáticamente en target/allure-report
# Abrir en navegador
open target/allure-report/index.html
```

---

## 🛠️ Necesidades y Setup

### Instalación Rápida

```bash
# 1. Instalar dependencias Maven
mvn clean install

# 2. Instalar Spectral CLI
npm install -g @stoplight/spectral-cli

# 3. Configurar variables de entorno
cp .env.example .env

# 4. Ejecutar validación + tests
bash scripts/validateAndExecute.sh
```

---



## 🐛 Troubleshooting

### Error: "Could not find ./.env"
```bash
# Solución: Copiar plantilla
cp .env.example .env
```

### Error: "spectral: command not found"
```bash
# Solución: Instalar Spectral globalmente
npm install -g @stoplight/spectral-cli
```

### Error: "Maven not found"
```bash
# Solución: Instalar Maven o agregar a PATH
# En macOS con Homebrew:
brew install maven
```

### Los tests fallan pero la validación pasó
```bash
# Verificar que BASE_URL en .env es correcto
cat .env

# Ejecutar con logs detallados
mvn test -X
```

---

## 📚 Documentación Adicional

- [OpenAPI 3.0.3 Spec](https://spec.openapis.org/oas/v3.0.3)
- [Spectral Documentation](https://docs.stoplight.io/docs/spectral/)
- [RestAssured Guide](https://rest-assured.io/)
- [Allure Reports](https://docs.qameta.io/allure/)
- [JUnit5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

---
