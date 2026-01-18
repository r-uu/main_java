#!/bin/bash

# Keycloak und PostgreSQL Container neu starten mit fixes

set -e

echo "🔧 Starte Docker-Services mit Keycloak-Fix neu..."
echo ""

cd /home/r-uu/develop/github/main/config/shared/docker

echo "1️⃣  Stoppe Container..."
docker compose down

echo ""
echo "2️⃣  Entferne alte Container (falls vorhanden)..."
# Gemeinsame Container
docker stop jasperreports-service 2>/dev/null || true
docker rm jasperreports-service 2>/dev/null || true
docker stop ruu-keycloak 2>/dev/null || true
docker rm ruu-keycloak 2>/dev/null || true
docker stop ruu-postgres 2>/dev/null || true
docker rm ruu-postgres 2>/dev/null || true

# Alte jeeeraaah-Container (können Ports blockieren!)
docker ps -a --filter "name=jeeeraaah" --format "{{.Names}}" | while read name; do
    docker stop "$name" 2>/dev/null || true
    docker rm "$name" 2>/dev/null || true
    echo "   Entfernt: $name"
done

echo "   (Alte Container bereinigt)"

echo ""
echo "3️⃣  Lösche alte Volumes (für sauberen Start)..."
docker volume rm ruu-postgres-data 2>/dev/null || echo "   (Volume existiert nicht)"

echo ""
echo "4️⃣  Starte Container neu..."
docker compose up -d

echo ""
echo "5️⃣  Warte auf PostgreSQL (10 Sekunden)..."
sleep 10

echo ""
echo "6️⃣  Erstelle Keycloak-Datenbank (falls nicht existiert)..."
docker exec ruu-postgres createdb -U ruu keycloak 2>/dev/null && echo "   ✅ Datenbank erstellt" || echo "   (Datenbank existiert bereits)"
docker exec ruu-postgres psql -U ruu -d keycloak -c "GRANT ALL ON SCHEMA public TO ruu;" 2>/dev/null || true

echo ""
echo "7️⃣  Warte auf Container-Start (weitere 20 Sekunden)..."
sleep 20

echo ""
echo "8️⃣  Container-Status:"
docker compose ps

echo ""
echo "9️⃣  Healthcheck-Status:"
docker ps --format "table {{.Names}}\t{{.Status}}"

echo ""
echo "✅ Fertig!"
echo ""
echo "🔍 Keycloak Admin Console: http://localhost:8080"
echo "   Username: admin"
echo "   Password: admin"
echo ""
echo "📋 Logs anschauen:"
echo "   docker compose logs -f keycloak"
echo ""

