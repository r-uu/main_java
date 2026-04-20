#!/bin/bash
# Dokumentations-Konsolidierung für r-uu Projekt
# Identifiziert obsolete/duplizierte Dokumentation

set -e

RUU_HOME="/home/r-uu/develop/github/java/main"
CONFIG="$RUU_HOME/config"
ARCHIVE="$CONFIG/archive"

echo "════════════════════════════════════════════════════════════"
echo "Dokumentations-Konsolidierung"
echo "════════════════════════════════════════════════════════════"
echo ""

# Zu archivierende Dokumente (bereits gelöste Probleme)
OBSOLETE_DOCS=(
    "$CONFIG/SESSION-EXPIRED-CRUD-FIX.md"
    "$CONFIG/SESSION-RETRY-REFACTORING.md"
    "$CONFIG/SESSION-EXPIRY-FIX.md"
    "$CONFIG/DEADLOCK-FIX-SUMMARY.md"
    "$CONFIG/COMPILE-FIX-EXCEPTIONDIALOG.md"
    "$CONFIG/POSTGRES-AUTO-INIT.md"
    "$CONFIG/CONSOLIDATION-COMPLETE.md"
    "$CONFIG/shared/docker/MIGRATION-GUIDE.md"
    "$CONFIG/shared/docker/KEYCLOAK-HEALTHCHECK-FIX.md"
    "$CONFIG/shared/docker/CONTAINER-RENAME-SUCCESS.md"
    "$CONFIG/shared/docker/AUTOSTART-SETUP-FERTIG.md"
    "$CONFIG/shared/docker/DOCKER-RESET-GUIDE.md"
)

# Duplizierte/redundante Dokumente
DUPLICATE_DOCS=(
    "$CONFIG/INDEX.md"  # Ersetzt durch DOCUMENTATION-INDEX.md
    "$CONFIG/DOCS-INDEX.md"  # Dupliziert
)

echo "📋 Zu archivierende Dokumente:"
echo ""

for doc in "${OBSOLETE_DOCS[@]}" "${DUPLICATE_DOCS[@]}"; do
    if [ -f "$doc" ]; then
        echo "  ➜ $(basename "$doc")"
    fi
done

echo ""
echo "════════════════════════════════════════════════════════════"
echo "Aktion wählen:"
echo "  [1] Dokumente ins Archiv verschieben"
echo "  [2] Dokumente löschen (⚠️  unwiderruflich!)"
echo "  [3] Nur anzeigen (nichts ändern)"
echo "  [4] Abbrechen"
echo "════════════════════════════════════════════════════════════"
read -p "Auswahl [1-4]: " choice

case $choice in
    1)
        echo ""
        echo "📦 Verschiebe Dokumente ins Archiv..."
        mkdir -p "$ARCHIVE"
        for doc in "${OBSOLETE_DOCS[@]}" "${DUPLICATE_DOCS[@]}"; do
            if [ -f "$doc" ]; then
                mv "$doc" "$ARCHIVE/"
                echo "  ✓ $(basename "$doc") → archive/"
            fi
        done
        echo ""
        echo "✅ Fertig! Dokumente im Archiv: $ARCHIVE"
        ;;
    2)
        echo ""
        read -p "⚠️  Wirklich löschen? [yes/no]: " confirm
        if [ "$confirm" = "yes" ]; then
            echo ""
            echo "🗑️  Lösche Dokumente..."
            for doc in "${OBSOLETE_DOCS[@]}" "${DUPLICATE_DOCS[@]}"; do
                if [ -f "$doc" ]; then
                    rm "$doc"
                    echo "  ✓ $(basename "$doc") gelöscht"
                fi
            done
            echo ""
            echo "✅ Fertig!"
        else
            echo "Abgebrochen."
        fi
        ;;
    3)
        echo ""
        echo "📄 Existierende obsolete Dokumente:"
        for doc in "${OBSOLETE_DOCS[@]}" "${DUPLICATE_DOCS[@]}"; do
            if [ -f "$doc" ]; then
                echo "  • $doc"
            fi
        done
        ;;
    4)
        echo "Abgebrochen."
        exit 0
        ;;
    *)
        echo "Ungültige Auswahl."
        exit 1
        ;;
esac

echo ""
echo "════════════════════════════════════════════════════════════"
echo "💡 Nächste Schritte:"
echo "  1. Neue Hauptdokumentation ansehen: config/DOCUMENTATION-INDEX.md"
echo "  2. Session-Handling Doku: config/SESSION-HANDLING-README.md"
echo "  3. Archiv bereinigen (falls nötig): rm -rf config/archive/*"
echo "════════════════════════════════════════════════════════════"
