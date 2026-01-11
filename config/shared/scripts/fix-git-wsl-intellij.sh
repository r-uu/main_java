#!/bin/bash
# Git Konfiguration für WSL + IntelliJ
# Behebt das "Exec format error" Problem wenn IntelliJ versucht Windows .exe aus WSL auszuführen
set -e
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
echo "🔧 Konfiguriere Git für WSL + IntelliJ Kompatibilität..."
echo "📁 Repository: $REPO_ROOT"
cd "$REPO_ROOT"
# Deaktiviere IntelliJ's askpass helper (verhindert "Exec format error")
git config --local core.askpass ""
git config --local core.askPass ""
# Prüfe ob gh (GitHub CLI) verfügbar ist
if command -v gh &> /dev/null; then
    echo "✓ GitHub CLI (gh) gefunden"
    # Prüfe ob gh authentifiziert ist
    if gh auth status &> /dev/null; then
        echo "✓ GitHub CLI ist authentifiziert"
        # Nutze gh als credential helper
        git config --local credential.helper ""
        git config --local --add credential.helper "cache --timeout=3600"
        git config --local --add credential.helper '!/usr/bin/gh auth git-credential'
        echo "✓ GitHub CLI als credential helper konfiguriert"
    else
        echo "⚠️  GitHub CLI ist nicht authentifiziert"
        echo "   Führe aus: gh auth login"
        # Fallback zu cache
        git config --local credential.helper "cache --timeout=3600"
        echo "✓ Fallback: cache credential helper (1 Stunde)"
    fi
else
    echo "⚠️  GitHub CLI (gh) nicht gefunden"
    echo "   Installation: sudo apt install gh"
    # Fallback zu cache
    git config --local credential.helper "cache --timeout=3600"
    echo "✓ Fallback: cache credential helper (1 Stunde)"
fi
# Deaktiviere askpass Umgebungsvariablen für diese Shell
export GIT_ASKPASS=""
export SSH_ASKPASS=""
echo ""
echo "✅ Git-Konfiguration abgeschlossen!"
echo ""
echo "Aktuelle Konfiguration:"
git config --local --list | grep -E "(askpass|credential)" || echo "  (keine askpass/credential Einträge)"
echo ""
echo "💡 Tipp: Um das dauerhaft zu machen, füge diese Zeilen zu deiner ~/.bashrc hinzu:"
echo "   export GIT_ASKPASS=\"\""
echo "   export SSH_ASKPASS=\"\""
