#!/bin/bash

# Docker Compose Build Wrapper mit Proxy-Konfiguration
# Verwendet klassischen Docker Builder statt BuildKit für bessere Proxy-Kompatibilität

set -e

# Proxy-Einstellungen exportieren
export HTTP_PROXY="http://gkd-re%5Clinuxupdateuser:Eet9atoo@172.16.28.3:8080"
export HTTPS_PROXY="http://gkd-re%5Clinuxupdateuser:Eet9atoo@172.16.28.3:8080"
export NO_PROXY="localhost,127.0.0.1,*.local,172.16.0.0/12,10.0.0.0/8"

# Klassischen Builder verwenden (kein BuildKit)
export DOCKER_BUILDKIT=0

echo "🔧 Building with Docker Compose (Proxy-enabled, BuildKit disabled)..."
echo "   HTTP_PROXY: $HTTP_PROXY"
echo ""

# Führe docker compose build mit allen übergebenen Argumenten aus
docker compose build "$@"

echo ""
echo "✅ Build completed successfully!"
