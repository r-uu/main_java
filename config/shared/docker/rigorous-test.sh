#!/bin/bash
set -e

echo "════════════════════════════════════════════════════════════════"
echo "🧪 RIGOROUS DOCKER ENVIRONMENT TEST & FIX"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counters
TESTS_PASSED=0
TESTS_FAILED=0

test_result() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ PASS${NC}: $1"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC}: $1"
        ((TESTS_FAILED++))
        return 1
    fi
}

echo "📋 Step 1: Clean Docker Environment"
echo "────────────────────────────────────────────────────────────────"

# Stop all containers
docker compose down -v 2>/dev/null || true
docker rm -f postgres keycloak jasperreports postgres-jeeeraaah postgres-keycloak postgres-lib-test keycloak-jeeeraaah 2>/dev/null || true
docker volume prune -f 2>/dev/null || true

echo -e "${GREEN}✓${NC} Docker environment cleaned"
echo ""

echo "📋 Step 2: Start PostgreSQL"
echo "────────────────────────────────────────────────────────────────"

docker compose up -d postgres
sleep 5

# Wait for healthy
for i in {1..30}; do
    if docker inspect postgres --format='{{.State.Health.Status}}' 2>/dev/null | grep -q "healthy"; then
        echo -e "${GREEN}✓${NC} PostgreSQL is healthy"
        break
    fi
    echo "Waiting for PostgreSQL to be healthy... ($i/30)"
    sleep 2
done

echo ""

echo "📋 Step 3: Verify Databases"
echo "────────────────────────────────────────────────────────────────"

# Check databases exist
docker exec postgres psql -U postgres -c "\l" | grep -q "jeeeraaah"
test_result "Database 'jeeeraaah' exists"

docker exec postgres psql -U postgres -c "\l" | grep -q "lib_test"
test_result "Database 'lib_test' exists"

docker exec postgres psql -U postgres -c "\l" | grep -q "keycloak"
test_result "Database 'keycloak' exists"

echo ""

echo "📋 Step 4: Verify Users & Authentication"
echo "────────────────────────────────────────────────────────────────"

# Test jeeeraaah user
docker exec postgres psql -U jeeeraaah -d jeeeraaah -c "SELECT 1" >/dev/null 2>&1
test_result "User 'jeeeraaah' can authenticate"

# Test lib_test user
docker exec postgres psql -U lib_test -d lib_test -c "SELECT 1" >/dev/null 2>&1
test_result "User 'lib_test' can authenticate"

# Test keycloak user
docker exec postgres psql -U keycloak -d keycloak -c "SELECT 1" >/dev/null 2>&1
test_result "User 'keycloak' can authenticate"

echo ""

echo "📋 Step 5: Verify User Permissions"
echo "────────────────────────────────────────────────────────────────"

# Test CREATE TABLE permission
docker exec postgres psql -U lib_test -d lib_test -c "CREATE TABLE IF NOT EXISTS test_table (id INT); DROP TABLE test_table;" >/dev/null 2>&1
test_result "User 'lib_test' can CREATE and DROP tables"

docker exec postgres psql -U jeeeraaah -d jeeeraaah -c "CREATE TABLE IF NOT EXISTS test_table (id INT); DROP TABLE test_table;" >/dev/null 2>&1
test_result "User 'jeeeraaah' can CREATE and DROP tables"

echo ""

echo "📋 Step 6: Start Keycloak"
echo "────────────────────────────────────────────────────────────────"

docker compose up -d keycloak
sleep 10

# Wait for healthy
for i in {1..60}; do
    if docker inspect keycloak --format='{{.State.Health.Status}}' 2>/dev/null | grep -q "healthy"; then
        echo -e "${GREEN}✓${NC} Keycloak is healthy"
        break
    fi
    echo "Waiting for Keycloak to be healthy... ($i/60)"
    sleep 2
done

# Check if Keycloak is running
docker ps | grep -q "keycloak"
test_result "Keycloak container is running"

echo ""

echo "📋 Step 7: Setup Keycloak Realm"
echo "────────────────────────────────────────────────────────────────"

cd ~/develop/github/java/main/root/lib/keycloak.admin
mvn -q exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup" >/dev/null 2>&1
test_result "Keycloak Realm 'jeeeraaah-realm' created"

# Verify realm exists
curl -s http://localhost:8080/realms/jeeeraaah-realm/.well-known/openid-configuration >/dev/null 2>&1
test_result "Keycloak Realm is accessible via HTTP"

echo ""

echo "📋 Step 8: Start JasperReports"
echo "────────────────────────────────────────────────────────────────"

cd ~/develop/github/java/main/config/shared/docker
docker compose up -d jasperreports
sleep 5

# Wait for healthy
for i in {1..20}; do
    if docker inspect jasperreports --format='{{.State.Health.Status}}' 2>/dev/null | grep -q "healthy"; then
        echo -e "${GREEN}✓${NC} JasperReports is healthy"
        break
    fi
    echo "Waiting for JasperReports to be healthy... ($i/20)"
    sleep 2
done

docker ps | grep -q "jasperreports"
test_result "JasperReports container is running"

echo ""

echo "📋 Step 9: Verify Container Names"
echo "────────────────────────────────────────────────────────────────"

# Check for old container names (should not exist)
! docker ps -a | grep -q "postgres-jeeeraaah"
test_result "No old 'postgres-jeeeraaah' container exists"

! docker ps -a | grep -q "postgres-keycloak"
test_result "No old 'postgres-keycloak' container exists"

! docker ps -a | grep -q "keycloak-jeeeraaah"
test_result "No old 'keycloak-jeeeraaah' container exists"

# Check for correct names
docker ps | grep -q "^[a-z0-9]* *postgres "
test_result "Container 'postgres' exists with correct name"

docker ps | grep -q "^[a-z0-9]* *keycloak "
test_result "Container 'keycloak' exists with correct name"

echo ""

echo "📋 Step 10: Run Java Tests"
echo "────────────────────────────────────────────────────────────────"

cd ~/develop/github/java/main/root

# Test JPA PostgreSQL Demo
mvn -q test -pl lib/jpa/se.hibernate.postgres.demo >/dev/null 2>&1
test_result "JPA PostgreSQL tests pass"

# Test Config Health Check
mvn -q test -pl lib/util/config/mp -Dtest=ConfigHealthCheckTest >/dev/null 2>&1
test_result "Config Health Check tests pass"

echo ""

echo "📋 Step 11: Final Container Status"
echo "────────────────────────────────────────────────────────────────"

docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "📊 TEST RESULTS"
echo "════════════════════════════════════════════════════════════════"
echo -e "${GREEN}✅ PASSED${NC}: $TESTS_PASSED"
echo -e "${RED}❌ FAILED${NC}: $TESTS_FAILED"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL TESTS PASSED!${NC}"
    echo ""
    echo "✅ Docker environment is ready"
    echo "✅ All databases are accessible"
    echo "✅ All users can authenticate"
    echo "✅ Keycloak realm is configured"
    echo "✅ Java tests pass"
    exit 0
else
    echo -e "${RED}⚠️  SOME TESTS FAILED${NC}"
    echo ""
    echo "Check the output above for details."
    echo "You may need to:"
    echo "  - Check Docker logs: docker logs postgres"
    echo "  - Check init scripts: ls -la initdb/"
    echo "  - Verify .env file: cat .env"
    exit 1
fi
