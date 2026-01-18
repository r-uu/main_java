#!/bin/bash
################################################################################
# Docker Services Auto-Start Script
# Startet PostgreSQL und Keycloak Container für jeeeraaah Entwicklung
################################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"
ENV_FILE="$SCRIPT_DIR/.env"

# Farben für Ausgabe
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# Prüfe ob Docker läuft
if ! docker info >/dev/null 2>&1; then
    log_error "Docker daemon ist nicht erreichbar!"
    log_error "Bitte stelle sicher, dass Docker läuft: sudo systemctl start docker"
    exit 1
fi

log_info "Docker daemon läuft"

# Wechsel zum Docker Compose Verzeichnis
cd "$SCRIPT_DIR"

# Lade .env falls vorhanden
if [ -f "$ENV_FILE" ]; then
    log_info "Lade Umgebungsvariablen aus .env"
    set -a
    source "$ENV_FILE"
    set +a
fi

# Lade .env.local falls vorhanden (überschreibt .env)
if [ -f "$SCRIPT_DIR/.env.local" ]; then
    log_info "Lade lokale Umgebungsvariablen aus .env.local"
    set -a
    source "$SCRIPT_DIR/.env.local"
    set +a
fi

# Starte die Services
log_info "Starte PostgreSQL und Keycloak..."
docker compose -f "$COMPOSE_FILE" up -d

# Warte auf gesunde Container
log_info "Warte auf Container-Initialisierung..."
sleep 10

# Prüfe Container-Status
POSTGRES_STATUS=$(docker inspect -f '{{.State.Status}}' ruu-postgres 2>/dev/null || echo "not found")
KEYCLOAK_STATUS=$(docker inspect -f '{{.State.Status}}' keycloak-service 2>/dev/null || echo "not found")

if [ "$POSTGRES_STATUS" = "running" ]; then
    log_info "PostgreSQL läuft (Container: ruu-postgres, Port: ${POSTGRES_PORT:-5432})"
else
    log_warn "PostgreSQL Status: $POSTGRES_STATUS"
fi

if [ "$KEYCLOAK_STATUS" = "running" ]; then
    log_info "Keycloak läuft (Container: keycloak-service, Port: ${KEYCLOAK_PORT:-8080})"
else
    log_warn "Keycloak Status: $KEYCLOAK_STATUS"
fi

log_info "Docker Services gestartet!"
log_info ""
log_info "Zugriff:"
log_info "  - PostgreSQL: localhost:${POSTGRES_PORT:-5432}"
log_info "  - Keycloak Admin: http://localhost:${KEYCLOAK_PORT:-8080}/admin"
log_info ""
log_info "Logs anzeigen: docker compose -f $COMPOSE_FILE logs -f"
log_info "Services stoppen: docker compose -f $COMPOSE_FILE down"

exit 0

