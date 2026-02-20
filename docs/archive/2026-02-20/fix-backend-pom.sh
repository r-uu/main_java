#!/bin/bash
# ==============================================================================
# Fix für das backend.persistence.jpa POM Problem
# ==============================================================================
# Problem: Version für r-uu.app.jeeeraaah.common.api.mapping fehlt
#
# Fehler:
#   [WARNING] The POM for r-uu:r-uu.app.jeeeraaah.backend.persistence.jpa:jar:0.0.1 is invalid
#   [ERROR] 'dependencies.dependency.version' for r-uu:r-uu.app.jeeeraaah.common.api.mapping:jar is missing
#
# Nutzung:
#   cd ~/develop/github/main
#   chmod +x fix-backend-pom.sh
#   ./fix-backend-pom.sh
#
# Was macht das Skript:
#   1. Installiert das BOM neu (enthält alle Versions-Definitionen)
#   2. Installiert das mapping Modul neu
#   3. Testet den backend.persistence.jpa Build
# ==============================================================================

set -e  # Beende bei Fehler

echo "==============================================="
echo "BOM und Mapping Modul Installation"
echo "==============================================="
echo ""

# 1. BOM neu installieren
echo "1. Installiere BOM..."
cd ~/develop/github/main/bom
mvn clean install
if [ $? -eq 0 ]; then
    echo "✅ BOM erfolgreich installiert"
else
    echo "❌ BOM Installation fehlgeschlagen"
    exit 1
fi
echo ""

# 2. Mapping Modul neu installieren
echo "2. Installiere mapping Modul..."
cd ~/develop/github/main/root/app/jeeeraaah/common/api/mapping
mvn clean install
if [ $? -eq 0 ]; then
    echo "✅ Mapping Modul erfolgreich installiert"
else
    echo "❌ Mapping Modul Installation fehlgeschlagen"
    exit 1
fi
echo ""

# 3. Backend persistence.jpa Modul testen
echo "3. Teste backend.persistence.jpa Build..."
cd ~/develop/github/main/root/app/jeeeraaah/backend/persistence/jpa
mvn clean compile
if [ $? -eq 0 ]; then
    echo "✅ Backend persistence.jpa Build erfolgreich"
else
    echo "❌ Backend persistence.jpa Build fehlgeschlagen"
    exit 1
fi
echo ""

echo "==============================================="
echo "✅ Alle Module erfolgreich gebaut!"
echo "==============================================="
echo ""
echo "Das Problem sollte nun behoben sein."
echo ""
echo "Nächster Schritt: Frontend UI FX neu bauen für Keycloak Login Fix"
echo "  cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx"
echo "  mvn clean install"


