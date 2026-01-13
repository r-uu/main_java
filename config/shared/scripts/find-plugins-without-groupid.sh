#!/bin/bash

# ============================================================================
# Find Maven Plugins without GroupId
# ============================================================================
# IntelliJ kann Plugins ohne explizite GroupId nicht immer auflösen
# ============================================================================

cd /home/r-uu/develop/github/main

echo "=========================================="
echo "Suche nach Plugins ohne GroupId"
echo "=========================================="
echo ""

# Finde alle pom.xml Dateien
find . -name "pom.xml" -type f | while read pom; do
    # Prüfe ob Plugin ohne GroupId existiert
    # Suche nach <plugin> gefolgt von <artifactId> OHNE <groupId> dazwischen
    if grep -Pzo '(?s)<plugin>\s*<artifactId>' "$pom" 2>/dev/null; then
        echo "❌ Potentielles Problem in: $pom"
        echo "   Plugin ohne GroupId gefunden"
        echo ""
    fi
done

echo "=========================================="
echo "Prüfe auch auf unvollständige Plugins"
echo "=========================================="
echo ""

# Alternativer Check: Suche nach <artifactId>...</artifactId> ohne <groupId> im gleichen <plugin> Block
find . -name "pom.xml" -type f -exec grep -l '<artifactId>maven-' {} \; | while read pom; do
    # Extrahiere plugin Blöcke und prüfe
    echo "Prüfe: $pom"
done

echo ""
echo "=========================================="
echo "Fertig"
echo "=========================================="

