#!/bin/bash
# JasperReports Build Status Checker

echo "═══════════════════════════════════════════════════════════════"
echo "  JasperReports Build Status"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Check if build is running
if ps aux | grep -q "[d]ocker compose build jasperreports"; then
    echo "🔄 Build läuft aktuell..."
    echo ""
    echo "📊 Build-Fortschritt:"
    tail -10 /tmp/jasperreports-build2.log
    echo ""
    echo "💡 Zum Verfolgen des Builds:"
    echo "   tail -f /tmp/jasperreports-build2.log"
else
    echo "✅ Build abgeschlossen (oder nicht gestartet)"
    echo ""

    # Check if image was created
    if docker images | grep -q "jasperreports"; then
        echo "✅ JasperReports Image erfolgreich gebaut!"
        docker images | grep -E "REPOSITORY|jasperreports"
        echo ""
        echo "🚀 Nächster Schritt: Container starten"
        echo "   cd /home/r-uu/develop/github/main/config/shared/docker"
        echo "   docker compose up -d jasperreports"
    else
        echo "❌ JasperReports Image nicht gefunden"
        echo ""
        echo "📋 Letzten 20 Zeilen des Build-Logs:"
        tail -20 /tmp/jasperreports-build2.log
    fi
fi

echo ""
echo "═══════════════════════════════════════════════════════════════"
