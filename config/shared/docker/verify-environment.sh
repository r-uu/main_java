#!/bin/bash
# Environment Verification Script

echo "════════════════════════════════════════════════════════════════════"
echo "Docker Environment Status"
echo "════════════════════════════════════════════════════════════════════"
echo ""

echo "=== Running Containers ==="
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | head -10
echo ""

echo "=== PostgreSQL Databases ==="
docker exec postgres psql -U postgres -lqt | cut -d \| -f 1 | grep -E 'jeeeraaah|lib_test|keycloak' | sed 's/^/  ✓ /'
echo ""

echo "=== Database Connection Tests ==="
docker exec postgres psql -U jeeeraaah -d jeeeraaah -c "SELECT 'jeeeraaah' as db, 'OK' as status;" -t 2>&1 | grep -q OK && echo "  ✓ jeeeraaah DB: Connected" || echo "  ✗ jeeeraaah DB: Failed"
docker exec postgres psql -U lib_test -d lib_test -c "SELECT 'lib_test' as db, 'OK' as status;" -t 2>&1 | grep -q OK && echo "  ✓ lib_test DB: Connected" || echo "  ✗ lib_test DB: Failed"
docker exec postgres psql -U keycloak -d keycloak -c "SELECT 'keycloak' as db, 'OK' as status;" -t 2>&1 | grep -q OK && echo "  ✓ keycloak DB: Connected" || echo "  ✗ keycloak DB: Failed"
echo ""

echo "=== Keycloak Status ==="
if curl -sf http://localhost:8080/health/ready > /dev/null 2>&1; then
    echo "  ✓ Keycloak: Healthy"
else
    echo "  ✗ Keycloak: Not Ready"
fi

if curl -sf http://localhost:8080/realms/jeeeraaah-realm > /dev/null 2>&1; then
    echo "  ✓ Realm jeeeraaah-realm: Configured"
else
    echo "  ✗ Realm jeeeraaah-realm: Not Found"
fi
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "Environment Summary"
echo "════════════════════════════════════════════════════════════════════"
echo ""
echo "Services:"
echo "  • PostgreSQL:  http://localhost:5432"
echo "  • Keycloak:    http://localhost:8080"
echo ""
echo "Credentials:"
echo "  • jeeeraaah DB:  jeeeraaah / jeeeraaah"
echo "  • lib_test DB:   lib_test / lib_test"
echo "  • keycloak DB:   keycloak / keycloak"
echo "  • Keycloak Admin: admin / admin"
echo "  • Test User:     jeeeraaah / jeeeraaah"
echo ""
echo "Next Steps:"
echo "  1. Build project: cd ~/develop/github/main/root && mvn clean install"
echo "  2. Start backend: cd ~/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs && mvn liberty:dev"
echo "  3. Start frontend: Run DashAppRunner in IntelliJ"
echo "════════════════════════════════════════════════════════════════════"

