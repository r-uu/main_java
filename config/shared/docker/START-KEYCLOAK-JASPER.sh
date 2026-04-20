#!/bin/bash
# ============================================================================
# Keycloak und JasperReports Container starten
# Verwendung: Kopiere und führe die Befehle in deinem Terminal aus
# ============================================================================

echo "==================================================================="
echo "Docker Container Startup - Keycloak und JasperReports"
echo "==================================================================="
echo ""

# Schritt 1: Zum Docker-Verzeichnis wechseln
echo "Schritt 1: Wechsle zum Docker-Verzeichnis..."
cd ~/develop/github/java/main/config/shared/docker || {
    echo "❌ Fehler: Verzeichnis nicht gefunden!"
    exit 1
}
echo "✓ Verzeichnis: $(pwd)"
echo ""

# Schritt 2: Docker Daemon Status prüfen
echo "Schritt 2: Prüfe Docker Daemon Status..."
if sudo service docker status | grep -q "running"; then
    echo "✓ Docker Daemon läuft bereits"
else
    echo "⚠ Docker Daemon läuft nicht, starte..."
    sudo service docker start
    sleep 3
    echo "✓ Docker Daemon gestartet"
fi
echo ""

# Schritt 3: Keycloak starten
echo "Schritt 3: Starte Keycloak Container..."
docker compose up -d keycloak
sleep 2
echo "✓ Keycloak gestartet"
echo ""

# Schritt 4: JasperReports starten
echo "Schritt 4: Starte JasperReports Container..."
docker compose up -d jasperreports
sleep 2
echo "✓ JasperReports gestartet"
echo ""

# Schritt 5: Warte kurz auf Initialisierung
echo "Schritt 5: Warte auf Container-Initialisierung (10 Sekunden)..."
sleep 10
echo "✓ Wartezeit abgeschlossen"
echo ""

# Schritt 6: Container Status anzeigen
echo "Schritt 6: Container Status:"
echo "-------------------------------------------------------------------"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(NAMES|postgres|keycloak|jasper)"
echo ""

# Schritt 7: Warte auf Keycloak Health Status
echo "Schritt 7: Warte auf Keycloak Health Status..."
echo "⏳ Keycloak muss 'healthy' sein, bevor Realm erstellt werden kann..."
WAIT_TIME=0
MAX_WAIT=120
while [ $WAIT_TIME -lt $MAX_WAIT ]; do
    KEYCLOAK_STATUS=$(docker inspect --format='{{.State.Health.Status}}' keycloak 2>/dev/null || echo "unknown")
    if [ "$KEYCLOAK_STATUS" = "healthy" ]; then
        echo "✓ Keycloak ist healthy nach ${WAIT_TIME} Sekunden"
        break
    fi
    echo "  Status: $KEYCLOAK_STATUS (${WAIT_TIME}s von ${MAX_WAIT}s)"
    sleep 5
    WAIT_TIME=$((WAIT_TIME + 5))
done

if [ "$KEYCLOAK_STATUS" != "healthy" ]; then
    echo "⚠️  Warnung: Keycloak ist nach ${MAX_WAIT} Sekunden immer noch nicht healthy!"
    echo "   Realm-Setup wird trotzdem versucht..."
fi
echo ""

# Schritt 8: Keycloak Realm erstellen
echo "Schritt 8: Prüfe und erstelle Keycloak Realm 'jeeeraaah-realm'..."
echo "-------------------------------------------------------------------"
cd ~/develop/github/java/main/root/lib/keycloak.admin || {
    echo "❌ Fehler: Verzeichnis keycloak.admin nicht gefunden!"
    exit 1
}

echo "Führe KeycloakRealmSetup aus..."
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup" -q

if [ $? -eq 0 ]; then
    echo "✓ Keycloak Realm Setup erfolgreich abgeschlossen"
else
    echo "⚠️  Warnung: Keycloak Realm Setup hatte Probleme (siehe Ausgabe oben)"
fi
echo ""

# Zurück zum Docker-Verzeichnis
cd ~/develop/github/java/main/config/shared/docker
echo ""

# Schritt 9: Health Status Details
echo "Schritt 9: Detaillierter Health Status:"
echo "-------------------------------------------------------------------"
docker ps --format "table {{.Names}}\t{{.Status}}"
echo ""

# Schritt 10: Erwarteter Zustand
echo "==================================================================="
echo "✅ Container wurden gestartet und Keycloak Realm wurde konfiguriert!"
echo "==================================================================="
echo ""
echo "Erwarteter Zustand:"
echo "  • postgres:        healthy"
echo "  • keycloak:        healthy (mit Realm 'jeeeraaah-realm')"
echo "  • jasperreports:   healthy"
echo ""
echo "Keycloak Setup:"
echo "  • Realm:   jeeeraaah-realm"
echo "  • Client:  jeeeraaah-frontend"
echo "  • User:    test / test (mit allen benötigten Rollen)"
echo ""
echo "Prüfe Status mit:"
echo "  docker ps"
echo ""
echo "Oder verwende Aliase:"
echo "  ruu-docker-ps      # Container Status"
echo "  ruu-docker-status  # Detaillierter Health Check"
echo ""
echo "Health Endpoints:"
echo "  Keycloak:      http://localhost:8080/health/ready"
echo "  JasperReports: http://localhost:8090/health"
echo ""
echo "Keycloak Admin Console:"
echo "  URL:      http://localhost:8080/admin"
echo "  User:     admin"
echo "  Password: admin"
echo ""
echo "Falls Container 'unhealthy' bleiben:"
echo "  docker logs keycloak       # Keycloak Logs"
echo "  docker logs jasperreports  # JasperReports Logs"
echo "==================================================================="
