#!/bin/bash
# =============================================================================
# Automated Docker Environment Startup and Setup
# =============================================================================
# This script:
# 1. Starts all Docker containers
# 2. Waits for all containers to become healthy
# 3. Sets up Keycloak realm automatically
# 4. Verifies the complete setup
# =============================================================================

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "════════════════════════════════════════════════════════════"
echo "🚀 Starting Docker Environment"
echo "════════════════════════════════════════════════════════════"
echo ""

# 0. Clean up old containers with wrong names
echo "🧹 Cleaning up old containers (if any)..."
OLD_CONTAINERS="keycloak-jeeeraaah ruu-keycloak ruu-postgres jasperreports-service"
for container in $OLD_CONTAINERS; do
    docker stop "$container" 2>/dev/null || true
    docker rm "$container" 2>/dev/null || true
done
echo "   ✅ Cleanup complete"
echo ""

# 1. Start containers
echo "📦 Starting Docker containers..."
docker compose up -d

echo ""
echo "⏳ Waiting for containers to become healthy..."
echo ""

# 2. Wait for each container to become healthy
wait_for_healthy() {
    local container=$1
    local max_wait=120  # Maximum 2 minutes
    local elapsed=0

    echo -n "   Waiting for $container..."

    while [ $elapsed -lt $max_wait ]; do
        status=$(docker inspect --format='{{.State.Health.Status}}' $container 2>/dev/null || echo "not found")

        if [ "$status" = "healthy" ]; then
            echo " ✅ healthy (${elapsed}s)"
            return 0
        fi

        echo -n "."
        sleep 2
        elapsed=$((elapsed + 2))
    done

    echo " ❌ timeout after ${elapsed}s"
    return 1
}

# Wait for each container
wait_for_healthy "postgres-keycloak" || exit 1
wait_for_healthy "postgres-jeeeraaah" || exit 1
wait_for_healthy "jasperreports" || exit 1
wait_for_healthy "keycloak" || exit 1

echo ""
echo "✅ All containers are healthy!"
echo ""

# 3. Setup Keycloak Realm
echo "════════════════════════════════════════════════════════════"
echo "🔐 Setting up Keycloak Realm"
echo "════════════════════════════════════════════════════════════"
echo ""

cd ~/develop/github/main/root/lib/keycloak.admin
mvn -q exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Keycloak realm setup completed successfully!"
else
    echo ""
    echo "❌ Keycloak realm setup failed!"
    exit 1
fi

# 4. Stop Liberty if running
echo ""
echo "════════════════════════════════════════════════════════════"
echo "🛑 Checking Liberty Server"
echo "════════════════════════════════════════════════════════════"
echo ""

LIBERTY_DIR=~/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs

if [ -d "$LIBERTY_DIR/target/liberty/wlp/usr/servers/defaultServer" ]; then
    echo "   Stopping Liberty if running..."
    cd "$LIBERTY_DIR"
    mvn -q liberty:stop 2>/dev/null || true
    echo "   ✅ Liberty stopped (if it was running)"
else
    echo "   ℹ️  Liberty not yet built"
fi

# 5. Final status check
echo ""
echo "════════════════════════════════════════════════════════════"
echo "🔍 Final Status Check"
echo "════════════════════════════════════════════════════════════"
echo ""

cd "$SCRIPT_DIR"
./check-status.sh

echo ""
echo "════════════════════════════════════════════════════════════"
echo "✅ Environment is ready!"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "Next steps:"
echo "  1. Start Liberty: cd ~/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs && mvn liberty:dev"
echo "  2. Start DashAppRunner in IntelliJ"
echo ""
