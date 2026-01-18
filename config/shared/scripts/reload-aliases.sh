#!/bin/bash

# Reload WSL Aliases

echo "🔄 Lade r-uu Aliase neu..."
echo ""

# Bashrc neu laden
source ~/.bashrc

echo "✅ Aliase erfolgreich neu geladen!"
echo ""
echo "Teste Aliases:"
echo ""

# Docker Version
echo "📦 Docker & Compose Version:"
docker --version
docker compose version
echo ""

# Verfügbare Aliases anzeigen
echo "🔧 Verfügbare Docker-Aliases:"
alias | grep ruu-docker | head -10
echo ""

echo "✅ Fertig! Du kannst jetzt verwenden:"
echo "   ruu-docker-restart"
echo "   ruu-docker-restart-clean"
echo "   ruu-docker-up"
echo "   ruu-docker-down"
echo ""

