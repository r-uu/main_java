#!/bin/bash
# =============================================================================
# Cleanup Deprecated Files
# =============================================================================
# Entfernt veraltete Dateien und archiviert gelöste Probleme
#
# Verwendung:
#   bash config/shared/scripts/cleanup-deprecated.sh
#   ruu-cleanup (falls Alias konfiguriert)
# =============================================================================

set -e

PROJECT_ROOT="/home/r-uu/develop/github/java/main"
ARCHIVE_DIR="$PROJECT_ROOT/config/archive/docs-20260130"

echo "════════════════════════════════════════════════════════════"
echo "🧹 Cleanup veralteter Dateien"
echo "════════════════════════════════════════════════════════════"
echo ""

# Prüfe ob wir im richtigen Verzeichnis sind
if [ ! -f "$PROJECT_ROOT/README.md" ]; then
    echo "❌ Fehler: Projekt-Root nicht gefunden!"
    echo "   Erwartet: $PROJECT_ROOT"
    exit 1
fi

cd "$PROJECT_ROOT"

# Archive-Verzeichnis erstellen
echo "📦 Erstelle Archive-Verzeichnis..."
mkdir -p "$ARCHIVE_DIR"
echo "   ✅ $ARCHIVE_DIR"
echo ""

# =============================================================================
# 1. Gelöste Probleme archivieren
# =============================================================================
echo "📦 Archiviere gelöste Probleme..."

ARCHIVE_FILES=(
    "config/DASHAPPRUNNER-FIX-APPLIED.md"
    "config/JAKARTA-MODULE-FIX.md"
    "config/POSTGRESQL-DB-PROBLEM-GELÖST.md"
    "config/LIBERTY-RESTART-SUCCESS.md"
    "config/shared/docker/CONTAINER-PROBLEM-SOLVED.md"
)

for file in "${ARCHIVE_FILES[@]}"; do
    if [ -f "$file" ]; then
        mv "$file" "$ARCHIVE_DIR/" && echo "   ✅ Archiviert: $file"
    else
        echo "   ⏭️  Nicht gefunden: $file"
    fi
done
echo ""

# =============================================================================
# 2. Veraltete Skripte entfernen
# =============================================================================
echo "🗑️  Entferne veraltete Skripte..."

DEPRECATED_SCRIPTS=(
    "config/shared/scripts/startup-complete.sh"
    "config/shared/docker/clean-environment.sh"
    "config/shared/docker/reset-all-containers.sh"
    "config/shared/docker/complete-reset.sh"
)

for script in "${DEPRECATED_SCRIPTS[@]}"; do
    if [ -f "$script" ]; then
        rm "$script" && echo "   ✅ Gelöscht: $script"
    else
        echo "   ⏭️  Nicht gefunden: $script"
    fi
done
echo ""

# =============================================================================
# 3. Doppelte/veraltete Dokumentation entfernen
# =============================================================================
echo "📝 Entferne doppelte Dokumentation..."

DEPRECATED_DOCS=(
    "CLEANUP-COMPLETE.md"
    "CONFIG-STRATEGY.md"
    "SCHNELL-ANLEITUNG-DOCKER.md"
    "config/DOCKER-CREDENTIALS-OVERVIEW.md"
    "config/DOCKER-CREDENTIALS-STATUS.md"
    "config/DOCKER-ENV-SETUP.md"
    "config/KEYCLOAK-RESET-ANLEITUNG.md"
    "config/SINGLE-SOURCE-OF-TRUTH-STATUS.md"
    "config/INTELLIJ-JPMS-RUN-CONFIG.md"
    "config/MIGRATION-CHECKLIST.md"
)

for doc in "${DEPRECATED_DOCS[@]}"; do
    if [ -f "$doc" ]; then
        rm "$doc" && echo "   ✅ Gelöscht: $doc"
    else
        echo "   ⏭️  Nicht gefunden: $doc"
    fi
done
echo ""

# =============================================================================
# 4. Doppelte Frontend Startup-Doku entfernen
# =============================================================================
echo "📝 Entferne doppelte Frontend-Dokumentation..."

FRONTEND_DEPRECATED=(
    "root/app/jeeeraaah/frontend/ui/fx/INTELLIJ-START-FIX.md"
    "root/app/jeeeraaah/frontend/ui/fx/START-DASHAPPRUNNER.md"
    "root/app/jeeeraaah/frontend/ui/fx/README-STARTUP.md"
)

for doc in "${FRONTEND_DEPRECATED[@]}"; do
    if [ -f "$doc" ]; then
        rm "$doc" && echo "   ✅ Gelöscht: $doc"
    else
        echo "   ⏭️  Nicht gefunden: $doc"
    fi
done
echo ""

# =============================================================================
# Zusammenfassung
# =============================================================================
echo "════════════════════════════════════════════════════════════"
echo "✅ Cleanup abgeschlossen!"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "📦 Archivierte Dateien: $ARCHIVE_DIR"
echo ""
echo "📋 Nächste Schritte:"
echo "   1. Prüfe gelöschte Dateien: git status"
echo "   2. Commit Änderungen: git add . && git commit -m 'docs: Cleanup veralteter Dateien'"
echo "   3. Prüfe DOCUMENTATION-INDEX.md auf kaputte Links"
echo ""
