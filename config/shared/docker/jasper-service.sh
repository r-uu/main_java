#!/bin/bash
# JasperReports Service Management

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$SCRIPT_DIR"
COMPOSE_FILE="docker-compose.jasper.yml"

# Farben
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo_info() {
    echo -e "${GREEN}✓${NC} $1"
}

echo_warn() {
    echo -e "${YELLOW}⚠${NC} $1"
}

echo_error() {
    echo -e "${RED}✗${NC} $1"
}

# Commands
case "${1:-help}" in
    start)
        echo_info "Starte JasperReports Service..."
        cd "$DOCKER_DIR"
        docker-compose -f "$COMPOSE_FILE" up -d --build
        echo_info "Service läuft auf http://localhost:8090"
        ;;

    stop)
        echo_info "Stoppe JasperReports Service..."
        cd "$DOCKER_DIR"
        docker-compose -f "$COMPOSE_FILE" down
        echo_info "Service gestoppt"
        ;;

    restart)
        echo_info "Starte JasperReports Service neu..."
        cd "$DOCKER_DIR"
        docker-compose -f "$COMPOSE_FILE" restart
        echo_info "Service neu gestartet"
        ;;

    rebuild)
        echo_info "Rebuilde JasperReports Service..."
        cd "$DOCKER_DIR"
        docker-compose -f "$COMPOSE_FILE" down
        docker-compose -f "$COMPOSE_FILE" build --no-cache
        docker-compose -f "$COMPOSE_FILE" up -d
        echo_info "Service neu gebaut und gestartet"
        ;;

    logs)
        docker logs jasperreports-service -f
        ;;

    status)
        if docker ps | grep -q jasperreports-service; then
            echo_info "Service läuft"
            docker ps --filter name=jasperreports-service --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
        else
            echo_warn "Service läuft nicht"
        fi
        ;;

    shell)
        docker exec -it jasperreports-service sh
        ;;

    test)
        echo_info "Teste JasperReports Service..."

        # Health Check
        if curl -s http://localhost:8090/health > /dev/null; then
            echo_info "Health Check: OK"
        else
            echo_error "Health Check: FAILED"
            exit 1
        fi

        # Templates auflisten
        echo_info "Verfügbare Templates:"
        curl -s http://localhost:8090/api/templates | jq .
        ;;

    generate)
        if [ -z "$2" ] || [ -z "$3" ]; then
            echo_error "Usage: $0 generate <template.jrxml> <format: pdf|docx>"
            exit 1
        fi

        TEMPLATE="$2"
        FORMAT="${3:-pdf}"

        echo_info "Generiere Report..."
        curl -X POST http://localhost:8090/api/report/generate \
            -H "Content-Type: application/json" \
            -d "{
                \"template\": \"$TEMPLATE\",
                \"format\": \"$FORMAT\",
                \"parameters\": {
                    \"generatedAt\": \"$(date '+%Y-%m-%d %H:%M:%S')\"
                }
            }" | jq .
        ;;

    clean)
        echo_warn "Lösche alle generierten Reports..."
        rm -rf "$DOCKER_DIR/jasperreports/output/"*.pdf
        rm -rf "$DOCKER_DIR/jasperreports/output/"*.docx
        echo_info "Output-Verzeichnis bereinigt"
        ;;

    help|*)
        cat << 'EOF'
JasperReports Service Management

Usage: ./jasper-service.sh <command>

Commands:
    start       - Startet den Service
    stop        - Stoppt den Service
    restart     - Startet den Service neu
    rebuild     - Baut den Service neu (nach Code-Änderungen)
    logs        - Zeigt Live-Logs
    status      - Zeigt Service-Status
    shell       - Öffnet Shell im Container
    test        - Testet den Service
    generate    - Generiert einen Report
                  Usage: generate <template.jrxml> <pdf|docx>
    clean       - Löscht generierte Reports
    help        - Zeigt diese Hilfe

Beispiele:
    ./jasper-service.sh start
    ./jasper-service.sh generate invoice.jrxml pdf
    ./jasper-service.sh logs
    ./jasper-service.sh test

Service URL: http://localhost:8090
EOF
        ;;
esac

