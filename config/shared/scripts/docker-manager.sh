#!/bin/bash
# Docker Management Skript für r-uu Projekt
# Verwaltung von PostgreSQL und Keycloak Services

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
DOCKER_DIR="$PROJECT_ROOT/config/shared/docker"

# Farben
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Hilfsfunktion: Status
status_ok() {
    echo -e "${GREEN}✓${NC} $1"
}

status_warn() {
    echo -e "${YELLOW}⚠${NC} $1"
}

status_error() {
    echo -e "${RED}✗${NC} $1"
}

status_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Header
show_header() {
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}🐳 r-uu Docker Management${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════════${NC}"
    echo ""
}

# Zeige Status
show_status() {
    echo -e "${YELLOW}Status:${NC}"
    echo ""
    docker ps --filter "name=ruu-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" 2>/dev/null || status_warn "Keine Container laufen"
    echo ""
}

# Service starten
start_service() {
    local service=$1
    cd "$DOCKER_DIR"
    echo -e "${YELLOW}Starte $service...${NC}"
    docker compose up -d "$service"
    if [ $? -eq 0 ]; then
        status_ok "$service gestartet"
    else
        status_error "$service konnte nicht gestartet werden"
        exit 1
    fi
}

# Service stoppen
stop_service() {
    local service=$1
    local container="ruu-$service"
    echo -e "${YELLOW}Stoppe $service...${NC}"
    docker container stop "$container" 2>/dev/null
    if [ $? -eq 0 ]; then
        status_ok "$service gestoppt"
    else
        status_warn "$service war nicht gestartet"
    fi
}

# Rebuild Service
rebuild_service() {
    local service=$1
    echo -e "${YELLOW}Rebuilding $service (Daten werden gelöscht!)...${NC}"
    read -p "Wirklich fortfahren? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        status_info "Abgebrochen"
        exit 0
    fi

    stop_service "$service"
    docker volume rm "ruu-${service}-data" 2>/dev/null && status_ok "Volume gelöscht" || status_warn "Kein Volume gefunden"
    start_service "$service"
    status_ok "$service neu aufgesetzt"
}

# Logs anzeigen
show_logs() {
    local service=$1
    local container="ruu-$service"
    echo -e "${YELLOW}Logs von $service:${NC}"
    echo -e "${BLUE}───────────────────────────────────────────────────────────────────${NC}"
    docker logs -f "$container"
}

# Shell öffnen
open_shell() {
    local service=$1
    case $service in
        postgres)
            docker exec -it ruu-postgres psql -U "${POSTGRES_USER:-ruu}" -d "${POSTGRES_DB:-ruu_dev}"
            ;;
        *)
            docker exec -it "ruu-$service" /bin/bash
            ;;
    esac
}

# Hilfe
show_help() {
    cat <<EOF
Verwendung: ./docker-manager.sh [COMMAND] [SERVICE]

COMMANDS:
  start [SERVICE]    - Startet einen oder alle Services
  stop [SERVICE]     - Stoppt einen oder alle Services
  restart [SERVICE]  - Neustart eines oder aller Services
  status             - Zeigt Status aller Services
  logs [SERVICE]     - Zeigt Logs eines Services
  shell [SERVICE]    - Öffnet Shell/CLI für Service
  rebuild [SERVICE]  - Rebuild Service (LÖSCHT DATEN!)
  prune              - Bereinigt Docker System
  help               - Zeigt diese Hilfe

SERVICES:
  postgres           - PostgreSQL Datenbank
  keycloak           - Keycloak Auth Server
  all                - Alle Services (Standard)

BEISPIELE:
  ./docker-manager.sh start                  # Alle Services starten
  ./docker-manager.sh start postgres         # Nur PostgreSQL starten
  ./docker-manager.sh logs postgres          # PostgreSQL Logs anzeigen
  ./docker-manager.sh shell postgres         # PostgreSQL Shell öffnen
  ./docker-manager.sh rebuild postgres       # PostgreSQL neu aufsetzen
  ./docker-manager.sh status                 # Status anzeigen

ALIASE:
  ruu-docker-up                              # Alle Services starten
  ruu-postgres-start                         # PostgreSQL starten
  ruu-postgres-logs                          # PostgreSQL Logs
  ruu-postgres-shell                         # PostgreSQL Shell
  ruu-keycloak-start                         # Keycloak starten

  Siehe: ruu-help-docker

EOF
}

# Hauptlogik
main() {
    show_header

    if [ $# -eq 0 ]; then
        show_help
        exit 0
    fi

    COMMAND=$1
    SERVICE=${2:-all}

    case $COMMAND in
        start)
            if [ "$SERVICE" = "all" ]; then
                cd "$DOCKER_DIR" && docker compose up -d
                status_ok "Alle Services gestartet"
                show_status
            else
                start_service "$SERVICE"
            fi
            ;;

        stop)
            if [ "$SERVICE" = "all" ]; then
                cd "$DOCKER_DIR" && docker compose down
                status_ok "Alle Services gestoppt"
            else
                stop_service "$SERVICE"
            fi
            ;;

        restart)
            if [ "$SERVICE" = "all" ]; then
                cd "$DOCKER_DIR" && docker compose restart
                status_ok "Alle Services neugestartet"
                show_status
            else
                stop_service "$SERVICE"
                sleep 2
                start_service "$SERVICE"
            fi
            ;;

        status)
            show_status
            ;;

        logs)
            if [ "$SERVICE" = "all" ]; then
                cd "$DOCKER_DIR" && docker compose logs -f
            else
                show_logs "$SERVICE"
            fi
            ;;

        shell)
            if [ "$SERVICE" = "all" ]; then
                status_error "Bitte Service angeben (postgres, keycloak)"
                exit 1
            fi
            open_shell "$SERVICE"
            ;;

        rebuild)
            if [ "$SERVICE" = "all" ]; then
                status_warn "Rebuild all nicht unterstützt - nutze 'docker compose down -v'"
                exit 1
            fi
            rebuild_service "$SERVICE"
            ;;

        prune)
            echo -e "${RED}WARNUNG: Alle ungenutzten Docker-Ressourcen werden gelöscht!${NC}"
            read -p "Wirklich fortfahren? (y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                sudo docker system prune --all --volumes --force
                sudo docker volume prune --all --force
                status_ok "Docker System bereinigt"
            else
                status_info "Abgebrochen"
            fi
            ;;

        help|--help|-h)
            show_help
            ;;

        *)
            status_error "Unbekannter Befehl: $COMMAND"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

main "$@"

