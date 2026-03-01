#!/bin/bash
# =============================================================================
# Complete Docker Environment Reset Script
# =============================================================================
# Stops all containers, removes volumes, rebuilds everything from scratch
# Use this when the environment is corrupted or after major changes
# =============================================================================

set -e  # Exit on any error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEYCLOAK_ADMIN_DIR="$HOME/develop/github/main/root/lib/keycloak.admin"

echo "════════════════════════════════════════════════════════════════"
echo "🔄 Complete Docker Environment Reset"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Step 1: Stop all containers
echo "1️⃣  Stopping all containers..."
cd "$SCRIPT_DIR"
docker compose down -v 2>/dev/null || true
echo ""

# Step 2: Remove any dangling containers with old names
echo "2️⃣  Removing old containers (if any)..."
docker rm -f postgres-jeeeraaah postgres-lib-test postgres-keycloak keycloak-jeeeraaah 2>/dev/null || true
echo ""

# Step 3: Verify .env file
echo "3️⃣  Verifying .env file..."
if [ ! -f "$SCRIPT_DIR/.env" ]; then
    echo "❌ ERROR: .env file not found!"
    echo "   Please create .env from .env.template"
    exit 1
fi
echo "✅ .env file exists"
echo ""

# Step 4: Start PostgreSQL first
echo "4️⃣  Starting PostgreSQL container..."
docker compose up -d postgres
echo ""

# Step 5: Wait for PostgreSQL to be healthy
echo "5️⃣  Waiting for PostgreSQL to be ready..."
TIMEOUT=60
ELAPSED=0
while [ $ELAPSED -lt $TIMEOUT ]; do
    if docker inspect postgres --format='{{.State.Health.Status}}' 2>/dev/null | grep -q "healthy"; then
        echo "✅ PostgreSQL is healthy"
        break
    fi
    echo "   Waiting... (${ELAPSED}s elapsed)"
    sleep 5
    ELAPSED=$((ELAPSED + 5))
done

if [ $ELAPSED -ge $TIMEOUT ]; then
    echo "❌ PostgreSQL did not become healthy within ${TIMEOUT}s"
    echo ""
    echo "PostgreSQL logs:"
    docker logs postgres --tail 50
    exit 1
fi
echo ""

# Step 6: Verify databases were created
echo "6️⃣  Verifying databases..."
for db in jeeeraaah lib_test keycloak; do
    if docker exec postgres psql -U postgres -lqt | cut -d \| -f 1 | grep -qw "$db"; then
        echo "   ✅ Database '$db' exists"
    else
        echo "   ❌ Database '$db' NOT found!"
        exit 1
    fi
done
echo ""

# Step 7: Start Keycloak
echo "7️⃣  Starting Keycloak..."
docker compose up -d keycloak
echo ""

# Step 8: Wait for Keycloak to be healthy
echo "8️⃣  Waiting for Keycloak to be ready (may take 60s)..."
TIMEOUT=120
ELAPSED=0
while [ $ELAPSED -lt $TIMEOUT ]; do
    STATUS=$(docker inspect keycloak --format='{{.State.Health.Status}}' 2>/dev/null || echo "unknown")
    if [ "$STATUS" = "healthy" ]; then
        echo "✅ Keycloak is healthy"
        break
    fi
    echo "   Keycloak status: $STATUS (${ELAPSED}s elapsed)"
    sleep 5
    ELAPSED=$((ELAPSED + 5))
done

if [ $ELAPSED -ge $TIMEOUT ]; then
    echo "❌ Keycloak did not become healthy within ${TIMEOUT}s"
    echo ""
    echo "Keycloak logs:"
    docker logs keycloak --tail 100
    exit 1
fi
echo ""

# Step 9: Setup Keycloak Realm
echo "9️⃣  Setting up Keycloak realm..."
if [ -d "$KEYCLOAK_ADMIN_DIR" ]; then
    cd "$KEYCLOAK_ADMIN_DIR"
    mvn -q exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
    echo "✅ Keycloak realm setup complete"
else
    echo "⚠️  WARNING: Keycloak admin directory not found: $KEYCLOAK_ADMIN_DIR"
    echo "   Please run realm setup manually"
fi
echo ""

# Step 10: Start remaining services
echo "🔟 Starting remaining services..."
cd "$SCRIPT_DIR"
docker compose up -d
echo ""

# Step 11: Final status check
echo "1️⃣1️⃣ Final status check..."
sleep 5
docker compose ps
echo ""

echo "════════════════════════════════════════════════════════════════"
echo "✅ Docker Environment Reset Complete!"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "🔍 Next steps:"
echo "   1. Check container status: docker compose ps"
echo "   2. View logs: docker compose logs -f"
echo "   3. Build project: cd ~/develop/github/main/root && mvn clean install"
echo "   4. Start Liberty: cd ~/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs && mvn liberty:dev"
echo "   5. Start Frontend: Run DashAppRunner in IntelliJ"
echo ""
