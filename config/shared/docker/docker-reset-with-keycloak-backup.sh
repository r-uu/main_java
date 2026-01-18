#!/bin/bash
# Docker Reset mit Keycloak-Realm Backup
# Datum: 2026-01-17

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_DIR="$SCRIPT_DIR/../local/keycloak-backup"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "═══════════════════════════════════════════════════════════"
echo "Docker Reset mit Keycloak-Realm Backup"
echo "═══════════════════════════════════════════════════════════"
echo ""

# 1. Backup-Verzeichnis erstellen
echo "1. Erstelle Backup-Verzeichnis..."
mkdir -p "$BACKUP_DIR"

# 2. Keycloak-Realm exportieren (falls Container läuft)
echo "2. Prüfe Keycloak-Container..."
if docker ps -a --format '{{.Names}}' | grep -q "^ruu-keycloak$"; then
    echo "   Keycloak-Container gefunden: ruu-keycloak"

    if docker ps --format '{{.Names}}' | grep -q "^ruu-keycloak$"; then
        echo "   Container läuft - exportiere Realm..."

        # Export Realm (JEEERAaH Realm)
        docker exec ruu-keycloak /opt/keycloak/bin/kc.sh export \
            --dir /tmp/keycloak-export \
            --realm jeeeraaah \
            --users realm_file 2>/dev/null || echo "   Realm 'jeeeraaah' nicht gefunden oder Export fehlgeschlagen"

        # Kopiere Export
        docker cp ruu-keycloak:/tmp/keycloak-export "$BACKUP_DIR/export_$TIMESTAMP" 2>/dev/null || echo "   Kein Export zum Kopieren"

        echo "   ✅ Realm-Backup erstellt (falls vorhanden): $BACKUP_DIR/export_$TIMESTAMP"
    else
        echo "   Container gestoppt - kein Export möglich"
    fi
else
    echo "   Kein Keycloak-Container gefunden"
fi

# 3. Alte Container stoppen und entfernen
echo ""
echo "3. Stoppe und entferne alte Container..."
cd "$SCRIPT_DIR"

# Stoppe alle Compose-Services
docker compose down 2>/dev/null || echo "   Keine Compose-Services aktiv"

# Entferne alte Container (falls manuell erstellt)
OLD_CONTAINERS="postgres-jeeeraaah keycloak-jeeeraaah ruu-postgres"
for container in $OLD_CONTAINERS; do
    if docker ps -a --format '{{.Names}}' | grep -q "^$container$"; then
        echo "   Entferne alten Container: $container"
        docker stop "$container" 2>/dev/null || true
        docker rm "$container" 2>/dev/null || true
    fi
done

echo "   ✅ Alte Container entfernt"

# 4. Volumes löschen (ACHTUNG: Alle Daten!)
echo ""
echo "4. Lösche Volumes..."
echo "   WARNUNG: Alle Daten in Datenbanken gehen verloren!"
read -p "   Fortfahren? (ja/nein): " confirm

if [ "$confirm" != "ja" ]; then
    echo "   Abgebrochen!"
    exit 1
fi

# Liste alte Volumes
OLD_VOLUMES="ruu-postgres-data ruu-postgres-backups postgres-data postgres-backups"
for volume in $OLD_VOLUMES; do
    if docker volume ls --format '{{.Name}}' | grep -q "^$volume$"; then
        echo "   Entferne altes Volume: $volume"
        docker volume rm "$volume" 2>/dev/null || true
    fi
done

# Lösche aktuelle Volumes
docker volume rm postgres-jeeeraaah-data postgres-jeeeraaah-backups 2>/dev/null || echo "   Volumes postgres-jeeeraaah nicht gefunden"
docker volume rm postgres-keycloak-data postgres-keycloak-backups 2>/dev/null || echo "   Volumes postgres-keycloak nicht gefunden"

echo "   ✅ Volumes gelöscht"

# 5. Neue Container starten
echo ""
echo "5. Starte neue Container..."
docker compose up -d

echo ""
echo "   Warte auf Container-Start..."
sleep 15

# 6. Prüfe Container-Status
echo ""
echo "6. Container-Status:"
docker compose ps

# 7. Keycloak-Realm wiederherstellen (falls Backup vorhanden)
echo ""
echo "7. Prüfe Realm-Wiederherstellung..."

if [ -d "$BACKUP_DIR/export_$TIMESTAMP" ] && [ "$(ls -A "$BACKUP_DIR/export_$TIMESTAMP" 2>/dev/null)" ]; then
    echo "   Backup gefunden - warte bis Keycloak bereit ist..."

    # Warte bis Keycloak vollständig gestartet ist
    for i in {1..30}; do
        if docker exec ruu-keycloak curl -sf http://localhost:8080/health/ready > /dev/null 2>&1; then
            echo "   Keycloak ist bereit"
            break
        fi
        echo "   Warte auf Keycloak... ($i/30)"
        sleep 5
    done

    # Kopiere Backup in Container
    docker cp "$BACKUP_DIR/export_$TIMESTAMP" ruu-keycloak:/tmp/keycloak-import

    # Import (manuell via Admin Console oder kc.sh)
    echo "   ⚠️  Realm-Import muss manuell durchgeführt werden:"
    echo "      1. Öffne: http://localhost:8080/admin"
    echo "      2. Login: admin / admin"
    echo "      3. Realm → Create Realm → Import → Wähle Datei aus /tmp/keycloak-import"
    echo ""
    echo "   Oder via CLI:"
    echo "      docker exec ruu-keycloak /opt/keycloak/bin/kc.sh import --dir /tmp/keycloak-import"
else
    echo "   Kein Backup zum Wiederherstellen gefunden"
    echo "   ℹ️  Erstelle neuen Realm manuell:"
    echo "      http://localhost:8080/admin"
fi

# 8. Zusammenfassung
echo ""
echo "═══════════════════════════════════════════════════════════"
echo "✅ Docker-Reset abgeschlossen!"
echo "═══════════════════════════════════════════════════════════"
echo ""
echo "Services:"
echo "  - PostgreSQL JEEERAaH:  localhost:5432 (DB: jeeeraaah)"
echo "  - PostgreSQL Keycloak:  localhost:5433 (DB: keycloak)"
echo "  - Keycloak:            http://localhost:8080"
echo "  - JasperReports:       http://localhost:8090"
echo ""
echo "Keycloak Admin:"
echo "  URL:      http://localhost:8080/admin"
echo "  User:     admin"
echo "  Password: admin"
echo ""
echo "Backup-Location: $BACKUP_DIR"
echo "═══════════════════════════════════════════════════════════"

