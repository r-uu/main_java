#!/bin/bash
# ==============================================================================
# Kompletter Fix für GanttApp
# ==============================================================================
# Behebt beide Probleme:
#   1. Backend POM Problem (fehlende Version für mapping Modul)
#   2. Keycloak Login Problem (veraltete Config in frontend)
#
# Nutzung:
#   cd ~/develop/github/main
#   chmod +x fix-ganttapp-complete.sh
#   ./fix-ganttapp-complete.sh
# ==============================================================================

set -e  # Beende bei Fehler

echo "================================================================"
echo "GanttApp Complete Fix - Alle Probleme beheben"
echo "================================================================"
echo ""

# ============================================================================
# Teil 1: Backend POM Fix
# ============================================================================
echo "─────────────────────────────────────────────────────────────────"
echo "Teil 1/2: Backend POM Problem beheben"
echo "─────────────────────────────────────────────────────────────────"
echo ""

# 1.1 BOM neu installieren
echo "1.1 Installiere BOM..."
cd ~/develop/github/main/bom
mvn clean install -q
if [ $? -eq 0 ]; then
    echo "  ✅ BOM erfolgreich installiert"
else
    echo "  ❌ BOM Installation fehlgeschlagen"
    exit 1
fi

# 1.2 Mapping Modul neu installieren
echo "1.2 Installiere mapping Modul..."
cd ~/develop/github/main/root/app/jeeeraaah/common/api/mapping
mvn clean install -q
if [ $? -eq 0 ]; then
    echo "  ✅ Mapping Modul erfolgreich installiert"
else
    echo "  ❌ Mapping Modul Installation fehlgeschlagen"
    exit 1
fi

# 1.3 Backend persistence.jpa testen
echo "1.3 Teste backend.persistence.jpa Build..."
cd ~/develop/github/main/root/app/jeeeraaah/backend/persistence/jpa
mvn clean compile -q
if [ $? -eq 0 ]; then
    echo "  ✅ Backend persistence.jpa Build erfolgreich"
else
    echo "  ❌ Backend persistence.jpa Build fehlgeschlagen"
    exit 1
fi

echo ""
echo "✅ Teil 1/2 abgeschlossen: Backend POM Problem behoben"
echo ""

# ============================================================================
# Teil 2: Frontend UI FX neu bauen (für Keycloak Login Fix)
# ============================================================================
echo "─────────────────────────────────────────────────────────────────"
echo "Teil 2/2: Frontend UI FX neu bauen (Keycloak Login Fix)"
echo "─────────────────────────────────────────────────────────────────"
echo ""

cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn clean install -q
if [ $? -eq 0 ]; then
    echo "  ✅ Frontend UI FX erfolgreich gebaut"
else
    echo "  ❌ Frontend UI FX Build fehlgeschlagen"
    exit 1
fi

echo ""
echo "✅ Teil 2/2 abgeschlossen: Frontend UI FX neu gebaut"
echo ""

# ============================================================================
# Zusammenfassung
# ============================================================================
echo "================================================================"
echo "✅ ALLE FIXES ERFOLGREICH ABGESCHLOSSEN!"
echo "================================================================"
echo ""
echo "GanttApp kann jetzt gestartet werden:"
echo ""
echo "  cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx"
echo "  mvn exec:java -Dexec.mainClass=\"de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.GanttAppRunner\""
echo ""
echo "Erwartetes Ergebnis:"
echo "  ✅ Configuration properties validated successfully"
echo "  ✅ Automatic login successful"
echo "  ✅ App startet ohne Fehler"
echo ""

