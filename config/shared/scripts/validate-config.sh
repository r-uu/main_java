#!/bin/bash
# ============================================================================
# Configuration Validation Script
# ============================================================================
# Checks if .env file is properly configured and environment variables work
# ============================================================================

set -e

# Navigate to docker directory where .env file is located
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DOCKER_DIR="$SCRIPT_DIR/../docker"

cd "$DOCKER_DIR"

echo "════════════════════════════════════════════════════════════"
echo "🔍 Configuration Validation"
echo "════════════════════════════════════════════════════════════"
echo "📁 Working directory: $(pwd)"
echo ""

# Check .env file exists
echo "📄 Checking .env file..."
if [ ! -f .env ]; then
    echo "❌ ERROR: .env file not found!"
    echo "   Create it from template: cp .env.template .env"
    exit 1
fi
echo "✅ .env file exists"
echo ""

# Check .env has correct line endings (LF not CRLF)
echo "📝 Checking line endings..."
if file .env | grep -q CRLF; then
    echo "❌ ERROR: .env has CRLF line endings!"
    echo "   Convert to LF: dos2unix .env"
    exit 1
fi
echo "✅ Line endings are correct (LF)"
echo ""

# Load .env and check required variables
echo "🔧 Loading environment variables from .env..."
export $(cat .env | grep -v '^#' | xargs)

# Required variables
REQUIRED_VARS=(
    "POSTGRES_JEEERAAAH_HOST"
    "POSTGRES_JEEERAAAH_PORT"
    "POSTGRES_JEEERAAAH_DATABASE"
    "POSTGRES_JEEERAAAH_USER"
    "POSTGRES_JEEERAAAH_PASSWORD"
    "POSTGRES_LIB_TEST_DATABASE"
    "POSTGRES_LIB_TEST_USER"
    "POSTGRES_LIB_TEST_PASSWORD"
    "POSTGRES_KEYCLOAK_HOST"
    "POSTGRES_KEYCLOAK_PORT"
    "POSTGRES_KEYCLOAK_DATABASE"
    "POSTGRES_KEYCLOAK_USER"
    "POSTGRES_KEYCLOAK_PASSWORD"
    "KEYCLOAK_ADMIN_USER"
    "KEYCLOAK_ADMIN_PASSWORD"
    "KEYCLOAK_REALM"
)

MISSING=0
for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        echo "❌ Missing: $var"
        MISSING=1
    else
        echo "✅ $var = ${!var}"
    fi
done

echo ""
if [ $MISSING -eq 1 ]; then
    echo "════════════════════════════════════════════════════════════"
    echo "❌ Validation FAILED - missing required variables"
    echo "════════════════════════════════════════════════════════════"
    exit 1
fi

# Check for placeholder passwords
echo "🔐 Checking for placeholder passwords..."
PLACEHOLDERS=0
if echo "$POSTGRES_JEEERAAAH_PASSWORD" | grep -q "CHANGE_ME"; then
    echo "⚠️  POSTGRES_JEEERAAAH_PASSWORD contains CHANGE_ME placeholder"
    PLACEHOLDERS=1
fi
if echo "$POSTGRES_LIB_TEST_PASSWORD" | grep -q "CHANGE_ME"; then
    echo "⚠️  POSTGRES_LIB_TEST_PASSWORD contains CHANGE_ME placeholder"
    PLACEHOLDERS=1
fi
if echo "$POSTGRES_KEYCLOAK_PASSWORD" | grep -q "CHANGE_ME"; then
    echo "⚠️  POSTGRES_KEYCLOAK_PASSWORD contains CHANGE_ME placeholder"
    PLACEHOLDERS=1
fi
if echo "$KEYCLOAK_ADMIN_PASSWORD" | grep -q "CHANGE_ME"; then
    echo "⚠️  KEYCLOAK_ADMIN_PASSWORD contains CHANGE_ME placeholder"
    PLACEHOLDERS=1
fi

if [ $PLACEHOLDERS -eq 1 ]; then
    echo ""
    echo "⚠️  WARNING: Some passwords still contain CHANGE_ME placeholders"
    echo "   This is OK for development, but change them for production!"
fi

echo ""
echo "════════════════════════════════════════════════════════════"
echo "✅ Configuration validation PASSED"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "💡 Next steps:"
echo "   1. Start Docker: cd ~/develop/github/java/main/config/shared/docker && docker compose up -d"
echo "   2. Check status: ./check-status.sh"
echo "   3. Build project: cd ~/develop/github/java/main && mvn clean install"
echo ""
