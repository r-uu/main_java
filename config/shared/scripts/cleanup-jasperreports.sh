#!/bin/bash
# Cleanup-Skript für generierte JasperReports Testdateien
# Löscht alle PDF/DOCX Dateien und fehlerhafte Windows-Pfade

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
JASPER_DIR="$PROJECT_ROOT/root/sandbox/office/microsoft/word/jasperreports"

echo "=== JasperReports Cleanup ==="
echo "Projekt-Root: $PROJECT_ROOT"
echo "JasperReports: $JASPER_DIR"
echo ""

# Zähle Dateien vor dem Löschen
PDF_COUNT=$(find "$JASPER_DIR" -name "*.pdf" 2>/dev/null | wc -l)
DOCX_COUNT=$(find "$JASPER_DIR" -name "*.docx" 2>/dev/null | wc -l)
WSL_COUNT=$(find "$PROJECT_ROOT" -name "*wsl*" -type f 2>/dev/null | wc -l)

echo "Gefundene Dateien:"
echo "  - PDF:  $PDF_COUNT"
echo "  - DOCX: $DOCX_COUNT"
echo "  - WSL-Pfad-Fehler: $WSL_COUNT"
echo ""

if [ "$PDF_COUNT" -eq 0 ] && [ "$DOCX_COUNT" -eq 0 ] && [ "$WSL_COUNT" -eq 0 ]; then
    echo "✓ Keine Dateien zum Löschen gefunden"
    exit 0
fi

# Bestätigung (optional, mit -f überspringen)
if [ "$1" != "-f" ]; then
    read -p "Alle generierte Dateien löschen? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Abgebrochen."
        exit 0
    fi
fi

# 1. PDF/DOCX im Hauptverzeichnis löschen
echo "1. Lösche PDF/DOCX im Hauptverzeichnis..."
cd "$JASPER_DIR"
rm -f *.pdf *.docx 2>/dev/null || true
echo "   ✓ Hauptverzeichnis bereinigt"

# 2. server/output/ Verzeichnis löschen
echo "2. Lösche server/output/ Verzeichnis..."
if [ -d "$JASPER_DIR/server/output" ]; then
    rm -rf "$JASPER_DIR/server/output"
    echo "   ✓ server/output/ gelöscht"
else
    echo "   ℹ server/output/ existiert nicht"
fi

# 3. Fehlerhafte Windows-Pfade löschen
echo "3. Lösche Dateien mit fehlerhaften Windows-Pfaden..."
find "$PROJECT_ROOT" -name "*wsl.localhost*" -type f -delete 2>/dev/null || true
find "$PROJECT_ROOT" -name "*wsl*" -path "*/config/shared/docker/*" -type f -delete 2>/dev/null || true
echo "   ✓ Windows-Pfad-Fehler bereinigt"

# 4. Git clean (nur untracked files in jasperreports/)
echo "4. Git-Cleanup für jasperreports/..."
cd "$JASPER_DIR"
git clean -fd . 2>/dev/null || echo "   ℹ Git-Cleanup übersprungen (kein Git-Repo oder keine untracked files)"

echo ""
echo "✅ Cleanup abgeschlossen!"
echo ""
echo "Verbleibende Dateien:"
find "$JASPER_DIR" -type f \( -name "*.pdf" -o -name "*.docx" \) 2>/dev/null || echo "  (keine)"

