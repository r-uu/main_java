#!/bin/bash

# Docker Proxy Konfiguration für GKD-RE Netzwerk
# Dieses Skript konfiguriert Docker, um den Unternehmens-Proxy zu nutzen

set -e

echo "🔧 Konfiguriere Docker für Proxy-Nutzung..."

# Proxy-Details (aus aktuellen Umgebungsvariablen übernommen)
# WICHTIG: Hostname proxy.gkd-re.local kann nicht aufgelöst werden, daher IP verwenden
PROXY_HOST="172.16.28.3"
PROXY_PORT="8080"
PROXY_USER="gkd-re%%5Clinuxupdateuser"
PROXY_PASS="Eet9atoo"
PROXY_URL="http://${PROXY_USER}:${PROXY_PASS}@${PROXY_HOST}:${PROXY_PORT}"

# Erstelle systemd Override-Verzeichnis
echo "📁 Erstelle Docker-Service-Override-Verzeichnis..."
sudo mkdir -p /etc/systemd/system/docker.service.d

# Erstelle Proxy-Konfiguration für systemd
echo "📝 Schreibe systemd Proxy-Konfiguration..."
sudo tee /etc/systemd/system/docker.service.d/http-proxy.conf > /dev/null << EOF
[Service]
Environment="HTTP_PROXY=${PROXY_URL}"
Environment="HTTPS_PROXY=${PROXY_URL}"
Environment="NO_PROXY=localhost,127.0.0.1,172.16.0.0/12,10.0.0.0/8"
EOF

# Erstelle daemon.json DNS-Konfiguration (Proxy wird über systemd gesetzt)
echo "📝 Schreibe Docker Daemon DNS-Konfiguration..."
sudo tee /etc/docker/daemon.json > /dev/null << EOF
{
  "dns": ["8.8.8.8", "8.8.4.4"]
}
EOF

echo "✅ Proxy-Konfiguration geschrieben:"
echo ""
echo "systemd Override:"
sudo cat /etc/systemd/system/docker.service.d/http-proxy.conf
echo ""
echo "Docker Daemon:"
sudo cat /etc/docker/daemon.json

# Reload systemd und restart Docker
echo ""
echo "🔄 Lade systemd-Konfiguration neu..."
sudo systemctl daemon-reload

echo "🔄 Starte Docker-Dienst neu..."
sudo systemctl restart docker

echo "⏳ Warte 5 Sekunden auf Docker-Start..."
sleep 5

# Prüfe Docker-Status
echo "🔍 Überprüfe Docker-Status..."
if sudo systemctl is-active --quiet docker; then
    echo "✅ Docker läuft!"
else
    echo "❌ Docker läuft nicht!"
    exit 1
fi


echo ""
echo "✅ Proxy-Konfiguration abgeschlossen!"
echo ""
echo "Du kannst jetzt die Docker-Images herunterladen mit:"
echo "  docker pull postgres:16-alpine"
echo "  docker pull quay.io/keycloak/keycloak:latest"

