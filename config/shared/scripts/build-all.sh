#!/bin/bash

# ============================================================================
# Build Fix - Führe kompletten Build vom Root aus
# ============================================================================

set -e  # Exit on error

echo "=========================================="
echo "Maven Build - Korrekte Reihenfolge"
echo "=========================================="
echo ""

# Zum Hauptverzeichnis wechseln
cd /home/r-uu/develop/github/java/main

echo "Aktuelles Verzeichnis: $(pwd)"
echo ""

# Prüfe und starte PostgreSQL
echo "Prüfe PostgreSQL..."
if command -v psql &> /dev/null; then
    if ! sudo service postgresql status > /dev/null 2>&1; then
        echo "PostgreSQL läuft nicht. Starte Service..."
        sudo service postgresql start
        sleep 2

        if sudo service postgresql status > /dev/null 2>&1; then
            echo "✅ PostgreSQL gestartet"
        else
            echo "⚠️  PostgreSQL konnte nicht gestartet werden!"
            echo "   Tests werden möglicherweise übersprungen."
            echo "   Für PostgreSQL Setup: sudo ./config/shared/scripts/setup-postgresql.sh"
        fi
    else
        echo "✅ PostgreSQL läuft bereits"
    fi
else
    echo "⚠️  PostgreSQL ist nicht installiert!"
    echo "   Tests werden übersprungen."
    echo "   Für Installation: sudo ./config/shared/scripts/setup-postgresql.sh"
fi
echo ""

# Prüfe ob config.properties existiert
if [ ! -f "config.properties" ]; then
    echo "⚠️  WARNUNG: config.properties nicht gefunden!"
    echo "   Erstelle aus Template..."
    cp config.properties.template config.properties
    echo "   ✅ Erstellt. Bitte Werte prüfen/anpassen!"
    echo ""
fi

echo "1. BOM bauen..."
mvn -f bom/pom.xml clean install
BOM_STATUS=$?

if [ $BOM_STATUS -ne 0 ]; then
    echo "❌ BOM Build fehlgeschlagen!"
    exit 1
fi

echo ""
echo "2. Root bauen..."
mvn -f root/pom.xml clean install
ROOT_STATUS=$?

if [ $ROOT_STATUS -ne 0 ]; then
    echo "❌ Root Build fehlgeschlagen!"
    exit 1
fi

echo ""
echo "=========================================="
echo "✅ BUILD ERFOLGREICH!"
echo "=========================================="
echo ""

# Prüfe gefilterte Dateien
echo "3. Prüfe Maven Resource Filtering..."
FILTERED_FILE="root/lib/jpa/se.hibernate.postgres.demo/target/test-classes/META-INF/microprofile-config.properties"

if [ -f "$FILTERED_FILE" ]; then
    echo "Gefilterte Datei: $FILTERED_FILE"
    echo ""
    if grep -q '${db.host}' "$FILTERED_FILE"; then
        echo "⚠️  WARNUNG: \${db.host} wurde NICHT ersetzt!"
        echo "   Maven Resource Filtering funktioniert nicht korrekt!"
    else
        echo "✅ Properties wurden korrekt ersetzt:"
        grep "database.host=" "$FILTERED_FILE" || echo "  (database.host nicht gefunden)"
        grep "database.port=" "$FILTERED_FILE" || echo "  (database.port nicht gefunden)"
    fi
else
    echo "⚠️  Gefilterte Datei nicht gefunden (Modul nicht gebaut?)"
fi

echo ""
echo "=========================================="
echo "Bereit für Tests!"
echo "=========================================="

