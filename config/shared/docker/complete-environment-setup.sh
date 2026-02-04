#!/bin/bash
set -e

echo "════════════════════════════════════════════════════════════════════"
echo "Complete JEEERAAAH Environment Setup"
echo "════════════════════════════════════════════════════════════════════"
echo ""

# Change to docker directory
cd "$(dirname "$0")"

# Step 1: Clean everything
echo "Step 1: Cleaning existing environment..."
echo "  Stopping containers..."
docker-compose down 2>/dev/null || true

echo "  Removing volumes..."
docker volume rm postgres-jeeeraaah-data postgres-keycloak-data keycloak-data 2>/dev/null || true

echo "✅ Environment cleaned"
echo ""

# Step 2: Start PostgreSQL containers
echo "Step 2: Starting PostgreSQL containers..."
docker-compose up -d postgres-jeeeraaah postgres-keycloak

echo "  Waiting for PostgreSQL to be ready..."
sleep 15

# Test connections
echo "  Testing postgres-jeeeraaah connection..."
docker exec postgres-jeeeraaah psql -U jeeeraaah -d jeeeraaah -c "SELECT 'jeeeraaah DB ready' as status;" || {
  echo "❌ postgres-jeeeraaah not ready"
  exit 1
}

echo "  Testing lib_test connection..."
docker exec postgres-jeeeraaah psql -U lib_test -d lib_test -c "SELECT 'lib_test DB ready' as status;" || {
  echo "❌ lib_test DB not ready"
  exit 1
}

echo "✅ PostgreSQL containers ready"
echo ""

# Step 3: Start Keycloak
echo "Step 3: Starting Keycloak..."
docker-compose up -d keycloak

echo "  Waiting for Keycloak to be ready (this takes ~60 seconds)..."
sleep 60

# Test Keycloak
until curl -sf http://localhost:8080/health/ready > /dev/null 2>&1; do
  echo "  Keycloak not ready yet, waiting 10 more seconds..."
  sleep 10
done

echo "✅ Keycloak ready"
echo ""

# Step 4: Configure Keycloak Realm
echo "Step 4: Configuring Keycloak Realm..."
./setup-keycloak-realm.sh

echo ""
echo "════════════════════════════════════════════════════════════════════"
echo "✅ Complete Environment Setup Finished"
echo "════════════════════════════════════════════════════════════════════"
echo ""
echo "Running Services:"
echo "  - PostgreSQL (jeeeraaah): localhost:5432"
echo "  - PostgreSQL (keycloak):  localhost:5433"
echo "  - Keycloak:              localhost:8080"
echo ""
echo "Database Credentials:"
echo "  jeeeraaah DB: jeeeraaah / jeeeraaah"
echo "  lib_test DB:  lib_test / lib_test"
echo "  keycloak DB:  keycloak / keycloak"
echo ""
echo "Keycloak:"
echo "  Realm:     jeeeraaah-realm"
echo "  Client:    jeeeraaah-frontend"
echo "  Test User: jeeeraaah / jeeeraaah"
echo "  Admin:     admin / admin"
echo ""
echo "Next Steps:"
echo "  1. Start Liberty backend: mvn -pl root/app/jeeeraaah/backend/api/ws.rs liberty:run"
echo "  2. Start frontend: Run DashAppRunner from IntelliJ"
echo "════════════════════════════════════════════════════════════════════"
