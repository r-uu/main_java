#!/bin/bash
set -e

echo "════════════════════════════════════════════════════════════════════"
echo "🧪 Multi-Database Setup - Verifikation"
echo "════════════════════════════════════════════════════════════════════"
echo ""

FAILED=0

# ═══════════════════════════════════════════════════════════════════
# Test 1: Container läuft
# ═══════════════════════════════════════════════════════════════════
echo "📌 Test 1: PostgreSQL Container"
if docker ps --format '{{.Names}}' | grep -q "^postgres-jeeeraaah$"; then
    echo "  ✅ postgres-jeeeraaah läuft"
else
    echo "  ❌ postgres-jeeeraaah läuft NICHT"
    FAILED=1
    exit 1
fi
echo ""

# ═══════════════════════════════════════════════════════════════════
# Test 2: Entrypoint-Wrapper ist konfiguriert
# ═══════════════════════════════════════════════════════════════════
echo "📌 Test 2: Entrypoint-Wrapper Konfiguration"
ENTRYPOINT=$(docker inspect --format='{{json .Config.Entrypoint}}' postgres-jeeeraaah 2>/dev/null)
if echo "$ENTRYPOINT" | grep -q "postgres-entrypoint-wrapper.sh"; then
    echo "  ✅ Entrypoint-Wrapper ist konfiguriert"
else
    echo "  ❌ Entrypoint-Wrapper ist NICHT konfiguriert"
    echo "     Erwartet: postgres-entrypoint-wrapper.sh"
    echo "     Gefunden: $ENTRYPOINT"
    FAILED=1
fi
echo ""

# ═══════════════════════════════════════════════════════════════════
# Test 3: Beide Datenbanken existieren
# ═══════════════════════════════════════════════════════════════════
echo "📌 Test 3: Datenbanken"

# jeeeraaah
if docker exec postgres-jeeeraaah psql -U r_uu -d postgres -lqt 2>/dev/null | cut -d \| -f 1 | grep -qw jeeeraaah; then
    echo "  ✅ jeeeraaah existiert"
else
    echo "  ❌ jeeeraaah fehlt"
    FAILED=1
fi

# lib_test
if docker exec postgres-jeeeraaah psql -U r_uu -d postgres -lqt 2>/dev/null | cut -d \| -f 1 | grep -qw lib_test; then
    echo "  ✅ lib_test existiert"
else
    echo "  ❌ lib_test fehlt"
    FAILED=1
fi
echo ""

# ═══════════════════════════════════════════════════════════════════
# Test 4: Beide Datenbanken haben richtigen Owner
# ═══════════════════════════════════════════════════════════════════
echo "📌 Test 4: Datenbank-Eigentümer"

for db in jeeeraaah lib_test; do
    OWNER=$(docker exec postgres-jeeeraaah psql -U r_uu -d postgres -tc "SELECT pg_catalog.pg_get_userbyid(d.datdba) FROM pg_catalog.pg_database d WHERE d.datname = '$db'" 2>/dev/null | tr -d '[:space:]')
    if [ "$OWNER" = "r_uu" ]; then
        echo "  ✅ $db Owner: r_uu"
    else
        echo "  ❌ $db Owner: $OWNER (erwartet: r_uu)"
        FAILED=1
    fi
done
echo ""

# ═══════════════════════════════════════════════════════════════════
# Test 5: Extensions installiert
# ═══════════════════════════════════════════════════════════════════
echo "📌 Test 5: PostgreSQL Extensions"

for db in jeeeraaah lib_test; do
    # Prüfe uuid-ossp
    if docker exec postgres-jeeeraaah psql -U r_uu -d "$db" -tc "SELECT 1 FROM pg_extension WHERE extname = 'uuid-ossp'" 2>/dev/null | grep -q 1; then
        echo "  ✅ $db: uuid-ossp installiert"
    else
        echo "  ⚠️  $db: uuid-ossp fehlt (optional)"
    fi
done
echo ""

# ═══════════════════════════════════════════════════════════════════
# Test 6: Entrypoint-Wrapper Logs
# ═══════════════════════════════════════════════════════════════════
echo "📌 Test 6: Entrypoint-Wrapper Logs"
if docker logs postgres-jeeeraaah 2>&1 | grep -q "PostgreSQL Entrypoint Wrapper gestartet"; then
    echo "  ✅ Entrypoint-Wrapper wurde ausgeführt"

    if docker logs postgres-jeeeraaah 2>&1 | grep -q "Alle Datenbanken bereit"; then
        echo "  ✅ Alle Datenbanken wurden erfolgreich erstellt"
    else
        echo "  ⚠️  'Alle Datenbanken bereit' nicht in Logs gefunden"
    fi
else
    echo "  ❌ Entrypoint-Wrapper wurde NICHT ausgeführt"
    FAILED=1
fi
echo ""

# ═══════════════════════════════════════════════════════════════════
# Test 7: Idempotenz-Test (kann mehrfach ausgeführt werden)
# ═══════════════════════════════════════════════════════════════════
echo "📌 Test 7: Idempotenz (Container-Neustart)"
echo "  → Teste ob Wrapper mehrfach ausgeführt werden kann..."
echo "  → Starte Container neu..."
docker restart postgres-jeeeraaah >/dev/null 2>&1

sleep 10

# Prüfe ob beide DBs noch da sind
if docker exec postgres-jeeeraaah psql -U r_uu -d postgres -lqt 2>/dev/null | cut -d \| -f 1 | grep -qw jeeeraaah && \
   docker exec postgres-jeeeraaah psql -U r_uu -d postgres -lqt 2>/dev/null | cut -d \| -f 1 | grep -qw lib_test; then
    echo "  ✅ Beide Datenbanken nach Neustart noch da (Idempotent!)"
else
    echo "  ❌ Datenbanken fehlen nach Neustart"
    FAILED=1
fi
echo ""

# ═══════════════════════════════════════════════════════════════════
# Zusammenfassung
# ═══════════════════════════════════════════════════════════════════
echo "════════════════════════════════════════════════════════════════════"
if [ $FAILED -eq 0 ]; then
    echo "✅ ALLE TESTS BESTANDEN!"
    echo "════════════════════════════════════════════════════════════════════"
    echo ""
    echo "🎯 Multi-Database Setup funktioniert perfekt!"
    echo ""
    echo "Beide Datenbanken werden einheitlich via Entrypoint-Wrapper erstellt:"
    echo "  ✅ jeeeraaah - Main application database"
    echo "  ✅ lib_test - Library test database"
    echo ""
    echo "Einheitlich • Elegant • Verständlich • Best Practice ✨"
    echo ""
    exit 0
else
    echo "❌ EINIGE TESTS SIND FEHLGESCHLAGEN"
    echo "════════════════════════════════════════════════════════════════════"
    echo ""
    echo "Bitte Container neu starten:"
    echo "  cd /home/r-uu/develop/github/java/main/config/shared/docker"
    echo "  docker compose down"
    echo "  docker compose up -d"
    echo ""
    echo "Dann Test erneut ausführen:"
    echo "  bash $0"
    echo ""
    exit 1
fi
