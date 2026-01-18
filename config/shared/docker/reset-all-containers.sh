#!/bin/bash
################################################################################
# Docker Container Reset mit Keycloak Realm Backup
# Setzt alle Container zurück, erhält aber den Keycloak Realm
################################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_DIR="$SCRIPT_DIR/keycloak-backup"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Farben für Ausgabe
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

echo "════════════════════════════════════════════════════════════════"
echo "  Docker Container Reset mit Keycloak Realm Backup"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Wechsel zum Docker Verzeichnis
cd "$SCRIPT_DIR"

# ═══════════════════════════════════════════════════════════════════
# Schritt 1: Keycloak Realm exportieren
# ═══════════════════════════════════════════════════════════════════
log_step "1. Sichere Keycloak Realm..."

if docker ps --format '{{.Names}}' | grep -q "^keycloak-service$"; then
    log_info "Keycloak Container läuft, exportiere Realm..."

    mkdir -p "$BACKUP_DIR"

    # Exportiere alle Realms
    docker exec keycloak-service /opt/keycloak/bin/kc.sh export \
        --dir /tmp/keycloak-export \
        --users realm_file 2>/dev/null || log_warn "Export fehlgeschlagen (Container nicht bereit?)"

    # Kopiere Export-Dateien
    docker cp keycloak-service:/tmp/keycloak-export "$BACKUP_DIR/export_$TIMESTAMP" 2>/dev/null && \
        log_info "Realm gesichert in: $BACKUP_DIR/export_$TIMESTAMP" || \
        log_warn "Konnte Realm nicht sichern (evtl. kein Realm konfiguriert)"
else
    log_warn "Keycloak Container läuft nicht, kein Backup möglich"
    log_warn "Wenn ein Realm existiert, starte zuerst die Container!"
fi

echo ""

# ═══════════════════════════════════════════════════════════════════
# Schritt 2: Container stoppen
# ═══════════════════════════════════════════════════════════════════
log_step "2. Stoppe alle Container..."

docker compose down
log_info "Container gestoppt"

echo ""

# ═══════════════════════════════════════════════════════════════════
# Schritt 3: Volumes löschen
# ═══════════════════════════════════════════════════════════════════
log_step "3. Lösche Volumes..."

# Bestätige Löschung
read -p "$(echo -e ${YELLOW}WARNUNG:${NC} Alle Daten gehen verloren! Fortfahren? [y/N] )" -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log_error "Abgebrochen durch Benutzer"
    exit 1
fi

# Lösche alle Volumes
docker volume rm postgres-jeeeraaah-data 2>/dev/null && log_info "✓ postgres-jeeeraaah-data gelöscht" || log_warn "postgres-jeeeraaah-data nicht gefunden"
docker volume rm postgres-jeeeraaah-backups 2>/dev/null && log_info "✓ postgres-jeeeraaah-backups gelöscht" || log_warn "postgres-jeeeraaah-backups nicht gefunden"
docker volume rm postgres-keycloak-data 2>/dev/null && log_info "✓ postgres-keycloak-data gelöscht" || log_warn "postgres-keycloak-data nicht gefunden"
docker volume rm postgres-keycloak-backups 2>/dev/null && log_info "✓ postgres-keycloak-backups gelöscht" || log_warn "postgres-keycloak-backups nicht gefunden"

echo ""

# ═══════════════════════════════════════════════════════════════════
# Schritt 4: Container neu starten
# ═══════════════════════════════════════════════════════════════════
log_step "4. Starte Container neu..."

docker compose up -d
log_info "Container gestartet"

echo ""

# ═══════════════════════════════════════════════════════════════════
# Schritt 5: Warte auf Keycloak
# ═══════════════════════════════════════════════════════════════════
log_step "5. Warte auf Keycloak-Initialisierung..."

log_info "Warte 30 Sekunden auf Container-Start..."
sleep 30

# Prüfe Keycloak Health
MAX_RETRIES=30
RETRY=0
while [ $RETRY -lt $MAX_RETRIES ]; do
    if curl -sf http://localhost:8080/health/ready >/dev/null 2>&1; then
        log_info "Keycloak ist bereit!"
        break
    fi
    RETRY=$((RETRY + 1))
    echo -n "."
    sleep 5
done
echo ""

if [ $RETRY -eq $MAX_RETRIES ]; then
    log_warn "Keycloak noch nicht bereit, aber fahre fort..."
fi

echo ""

# ═══════════════════════════════════════════════════════════════════
# Schritt 6: Realm importieren (falls Backup existiert)
# ═══════════════════════════════════════════════════════════════════
log_step "6. Stelle Keycloak Realm wieder her..."

if [ -d "$BACKUP_DIR/export_$TIMESTAMP" ] && [ "$(ls -A $BACKUP_DIR/export_$TIMESTAMP)" ]; then
    log_info "Importiere Realm aus Backup..."

    # Kopiere Backup in Container
    docker cp "$BACKUP_DIR/export_$TIMESTAMP" keycloak-service:/tmp/keycloak-import

    # Importiere Realm
    docker exec keycloak-service /opt/keycloak/bin/kc.sh import \
        --dir /tmp/keycloak-import \
        --override true && \
        log_info "✓ Realm erfolgreich importiert!" || \
        log_error "Import fehlgeschlagen - prüfe Logs: docker logs keycloak-service"
else
    log_warn "Kein Backup gefunden, überspringe Import"
    log_info "Keycloak startet mit frischer Konfiguration"
fi

echo ""

# ═══════════════════════════════════════════════════════════════════
# Schritt 7: Status anzeigen
# ═══════════════════════════════════════════════════════════════════
log_step "7. Container Status:"
echo ""

docker compose ps

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  ✅ Reset abgeschlossen!"
echo "════════════════════════════════════════════════════════════════"
echo ""
log_info "Zugriff auf die Services:"
echo "  - PostgreSQL (JEEERAaH): localhost:5432"
echo "  - PostgreSQL (Keycloak): localhost:5433"
echo "  - Keycloak Admin:        http://localhost:8080/admin"
echo ""
log_info "Standard Credentials:"
echo "  - PostgreSQL User:       r_uu / r_uu_password"
echo "  - Keycloak Admin:        admin / admin"
echo ""

if [ -d "$BACKUP_DIR/export_$TIMESTAMP" ]; then
    log_info "Backup gespeichert in: $BACKUP_DIR/export_$TIMESTAMP"
fi

echo ""
log_info "Logs anzeigen: docker compose logs -f"
log_info "Container neu starten: docker compose restart"
echo ""

