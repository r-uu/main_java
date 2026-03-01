#!/bin/bash
#
# Automatisches Setup der kompletten Docker-Umgebung
#
# Dieses Skript:
# 1. Stoppt alle Container
# 2. Löscht alle Volumes (Fresh Start)
# 3. Startet alle Container neu
# 4. Wartet bis Keycloak healthy ist
# 5. Erstellt automatisch den Keycloak Realm
#

set -e  # Exit on error

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

echo "════════════════════════════════════════════════════════════"
echo "🔄 Docker Environment - Complete Reset & Setup"
echo "════════════════════════════════════════════════════════════"
echo ""

# Step 1: Stop all containers
echo "📦 Step 1/6: Stopping all containers..."
docker compose down -v 2>&1 || true
echo "✅ Containers stopped"
echo ""

# Step 2: Clean volumes
echo "🗑️  Step 2/6: Cleaning volumes..."
docker volume prune -f 2>&1 || true
echo "✅ Volumes cleaned"
echo ""

# Step 3: Start containers
echo "🚀 Step 3/6: Starting containers..."
docker compose up -d
echo "✅ Containers started"
echo ""

# Step 4: Wait for PostgreSQL databases
echo "⏳ Step 4/6: Waiting for PostgreSQL databases (30s)..."
sleep 30
echo "✅ PostgreSQL should be ready"
echo ""

# Step 5: Wait for Keycloak
echo "⏳ Step 5/6: Waiting for Keycloak to become healthy (max 120s)..."
SECONDS=0
MAX_WAIT=120

while [ $SECONDS -lt $MAX_WAIT ]; do
    STATUS=$(docker inspect --format='{{.State.Health.Status}}' keycloak 2>/dev/null || echo "not-found")

    if [ "$STATUS" = "healthy" ]; then
        echo "✅ Keycloak is healthy (took ${SECONDS}s)"
        break
    fi

    if [ $((SECONDS % 10)) -eq 0 ]; then
        echo "   ⏳ Keycloak status: $STATUS (${SECONDS}s elapsed)"
    fi

    sleep 2
done

if [ "$STATUS" != "healthy" ]; then
    echo "❌ ERROR: Keycloak did not become healthy within ${MAX_WAIT} seconds"
    echo ""
    echo "Keycloak logs:"
    docker logs keycloak --tail 50
    exit 1
fi

echo ""

# Step 6: Setup Keycloak Realm
echo "🔐 Step 6/6: Creating Keycloak realm 'jeeeraaah-realm'..."
cd ~/develop/github/main/root/lib/keycloak.admin
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup" -q

if [ $? -eq 0 ]; then
    echo "✅ Keycloak realm created successfully"
else
    echo "❌ Failed to create Keycloak realm"
    exit 1
fi

echo ""
echo "════════════════════════════════════════════════════════════"
echo "✅ Setup Complete!"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "📊 Container Status:"
docker ps --format "table {{.Names}}\t{{.Status}}"
echo ""
echo "🔗 Services Ready:"
echo "   • PostgreSQL (jeeeraaah): localhost:5432"
echo "   • PostgreSQL (keycloak):  localhost:5433"
echo "   • Keycloak:               http://localhost:8080"
echo "   • Keycloak Admin:         http://localhost:8080/admin (admin/admin)"
echo "   • JasperReports:          http://localhost:8090"
echo ""
echo "▶️  Next Steps:"
echo "   1. Start Liberty Server:"
echo "      cd ~/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs"
echo "      mvn liberty:dev"
echo ""
echo "   2. Start DashAppRunner in IntelliJ"
echo ""
