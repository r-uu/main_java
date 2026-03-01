#!/bin/bash
set -e

echo "════════════════════════════════════════════════════════════════════"
echo "🧪 Docker Autostart - Kompletter Test"
echo "════════════════════════════════════════════════════════════════════"
echo ""

PROJECT_ROOT="/home/r-uu/develop/github/main"
DOCKER_DIR="$PROJECT_ROOT/config/shared/docker"

FAILED=0

# ═══════════════════════════════════════════════════════════════════
# Test 1: Docker Daemon
# ═══════════════════════════════════════════════════════════════════
echo "📌 Test 1: Docker Daemon Status"
if docker info >/dev/null 2>&1; then
    echo "  ✅ Docker Daemon läuft"
else
    echo "  ❌ Docker Daemon läuft NICHT"
    echo "  → Starte Docker Daemon..."
    sudo service docker start
    sleep 5
    if docker info >/dev/null 2>&1; then
        echo "  ✅ Docker Daemon erfolgreich gestartet"
    else
        echo "  ❌ Docker Daemon konnte nicht gestartet werden"
        FAILED=1
    fi
fi
echo ""

# ═══════════════════════════════════════════════════════════════════
# Test 2: Container laufen
# ═══════════════════════════════════════════════════════════════════
echo "📌 Test 2: Container Status"
EXPECTED_CONTAINERS=("postgres-jeeeraaah" "postgres-keycloak" "keycloak" "jasperreports")

for container in "${EXPECTED_CONTAINERS[@]}"; do
    if docker ps --format '{{.Names}}' | grep -q "^${container}$"; then
        STATUS=$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "no-healthcheck")
        if [ "$STATUS" = "healthy" ] || [ "$STATUS" = "no-healthcheck" ]; then
            echo "  ✅ $container läuft"
        else
            echo "  ⚠️  $container läuft, aber Status: $STATUS"
        fi
    else
        echo "  ❌ $container läuft NICHT"
        FAILED=1
    fi
done
echo ""

# ═══════════════════════════════════════════════════════════════════
# Test 3: PostgreSQL Verbindung
# ═══════════════════════════════════════════════════════════════════
echo "📌 Test 3: PostgreSQL Verbindung"
if docker exec postgres-jeeeraaah pg_isready -U r_uu -d jeeeraaah >/dev/null 2>&1; then
    echo "  ✅ PostgreSQL ist bereit"
else
    echo "  ❌ PostgreSQL antwortet nicht"
    FAILED=1
fi
echo ""

# ═══════════════════════════════════════════════════════════════════
# Test 4: Datenbanken existieren
# ═══════════════════════════════════════════════════════════════════
echo "📌 Test 4: Datenbanken"

# jeeeraaah
if docker exec postgres-jeeeraaah psql -U r_uu -d postgres -lqt 2>/dev/null | cut -d \| -f 1 | grep -qw jeeeraaah; then
    echo "  ✅ jeeeraaah Datenbank existiert"
else
    echo "  ❌ jeeeraaah Datenbank fehlt"
    FAILED=1
fi

# lib_test
if docker exec postgres-jeeeraaah psql -U r_uu -d postgres -lqt 2>/dev/null | cut -d \| -f 1 | grep -qw lib_test; then
    echo "  ✅ lib_test Datenbank existiert"
else
    echo "  ⚠️  lib_test Datenbank fehlt - warte auf Healthcheck (30s)..."
    sleep 30
    if docker exec postgres-jeeeraaah psql -U r_uu -d postgres -lqt 2>/dev/null | cut -d \| -f 1 | grep -qw lib_test; then
        echo "  ✅ lib_test wurde automatisch erstellt!"
    else
        echo "  ❌ lib_test wurde NICHT automatisch erstellt"
        FAILED=1
    fi
fi
echo ""

# ═══════════════════════════════════════════════════════════════════
# Test 5: Keycloak erreichbar
# ═══════════════════════════════════════════════════════════════════
echo "📌 Test 5: Keycloak"
if curl -s http://localhost:8080/health/ready | grep -q '"status":"UP"'; then
    echo "  ✅ Keycloak ist bereit"
else
    echo "  ⚠️  Keycloak antwortet nicht oder ist nicht ready"
    echo "     (Dies ist OK wenn Container gerade erst gestartet wurde)"
fi
echo ""

# ═══════════════════════════════════════════════════════════════════
# Test 6: JasperReports Service
# ═══════════════════════════════════════════════════════════════════
echo "📌 Test 6: JasperReports"
if curl -s http://localhost:8090/health >/dev/null 2>&1; then
    echo "  ✅ JasperReports Service ist erreichbar"
else
    echo "  ⚠️  JasperReports Service antwortet nicht"
    echo "     (Dies ist OK wenn Container gerade erst gestartet wurde)"
fi
echo ""

# ═══════════════════════════════════════════════════════════════════
# Test 7: Restart Policy
# ═══════════════════════════════════════════════════════════════════
echo "📌 Test 7: Restart Policy"
for container in "${EXPECTED_CONTAINERS[@]}"; do
    RESTART_POLICY=$(docker inspect --format='{{.HostConfig.RestartPolicy.Name}}' "$container" 2>/dev/null || echo "unknown")
    if [ "$RESTART_POLICY" = "always" ]; then
        echo "  ✅ $container: restart=always"
    else
        echo "  ❌ $container: restart=$RESTART_POLICY (erwartet: always)"
        FAILED=1
    fi
done
echo ""

# ═══════════════════════════════════════════════════════════════════
# Test 8: Maven Build (optional - nur wenn gewünscht)
# ═══════════════════════════════════════════════════════════════════
if [ "$1" = "--with-build" ]; then
    echo "📌 Test 8: Maven Build"
    echo "  → Führe Build aus (kann einige Minuten dauern)..."
    cd "$PROJECT_ROOT/root"
    if mvn clean install -q 2>&1 | tail -1 | grep -q "BUILD SUCCESS"; then
        echo "  ✅ Maven Build erfolgreich"
    else
        echo "  ❌ Maven Build fehlgeschlagen"
        FAILED=1
    fi
    echo ""
fi

# ═══════════════════════════════════════════════════════════════════
# Zusammenfassung
# ═══════════════════════════════════════════════════════════════════
echo "════════════════════════════════════════════════════════════════════"
if [ $FAILED -eq 0 ]; then
    echo "✅ ALLE TESTS BESTANDEN!"
    echo "════════════════════════════════════════════════════════════════════"
    echo ""
    echo "🎯 Docker-Umgebung ist vollständig funktionsfähig!"
    echo ""
    echo "Nächste Schritte:"
    echo "  → Projekt bauen: cd $PROJECT_ROOT/root && mvn clean install"
    echo "  → Backend starten: cd $PROJECT_ROOT/root/app/jeeeraaah/backend/api/ws_rs && mvn liberty:dev"
    echo "  → Frontend starten: IntelliJ → Run 'DashAppRunner'"
    echo ""
    exit 0
else
    echo "❌ EINIGE TESTS SIND FEHLGESCHLAGEN"
    echo "════════════════════════════════════════════════════════════════════"
    echo ""
    echo "Bitte prüfe die Fehler oben und führe aus:"
    echo "  → ruu-startup"
    echo ""
    echo "Dann Test erneut ausführen:"
    echo "  → bash $0"
    echo ""
    exit 1
fi
