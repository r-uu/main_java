# Git WSL + IntelliJ Kompatibilitäts-Fix
## Problem
Wenn IntelliJ IDEA aus Windows auf ein WSL-Repository zugreift und Git-Operationen durchführt (push, pull, etc.), kann dieser Fehler auftreten:
```
/mnt/c/Users/r-uu/AppData/Local/JetBrains/IntelliJIdea2025.3/tmp/intellij-git-askpass-wsl-Ubuntu.sh: 3: 
/mnt/c/Users/r-uu/AppData/Local/Programs/IntelliJ IDEA/jbr/bin/java.exe: Exec format error
unable to read askpass response from '/mnt/c/Users/r-uu/AppData/Local/JetBrains/IntelliJIdea2025.3/tmp/intellij-git-askpass-wsl-Ubuntu.sh'
could not read Username for 'https://github.com': No such device or address
```
### Ursache
IntelliJ setzt `GIT_ASKPASS` auf ein Windows-Skript, das versucht, eine Windows `.exe` Datei (java.exe) aus WSL heraus auszuführen. Dies führt zu einem "Exec format error", weil WSL keine Windows-Executables direkt ausführen kann (in diesem Kontext).
## Lösung
### Automatische Lösung (EMPFOHLEN)
```bash
# Aliase neu laden (enthält bereits die Fix-Variablen)
source ~/.bashrc
# Oder Git-Fix-Skript ausführen
ruu-git-fix
# Oder manuell:
bash /home/r-uu/develop/github/main/config/shared/scripts/fix-git-wsl-intellij.sh
```
### Was das Fix-Skript macht
1. **Deaktiviert IntelliJs askpass helper**
   ```bash
   git config --local core.askpass ""
   git config --local core.askPass ""
   ```
2. **Konfiguriert GitHub CLI als credential helper** (falls verfügbar)
   ```bash
   git config --local --add credential.helper "cache --timeout=3600"
   git config --local --add credential.helper '!/usr/bin/gh auth git-credential'
   ```
3. **Setzt Umgebungsvariablen** (in `config/shared/wsl/aliases.sh`)
   ```bash
   export GIT_ASKPASS=""
   export SSH_ASKPASS=""
   ```
### Manuelle Lösung
Falls das Skript nicht funktioniert:
```bash
cd /home/r-uu/develop/github/main
# Schritt 1: askpass deaktivieren
git config --local core.askpass ""
git config --local core.askPass ""
# Schritt 2: credential helper setzen (Wähle eine Option)
# Option A: Mit GitHub CLI (gh) - EMPFOHLEN
git config --local credential.helper ""
git config --local --add credential.helper "cache --timeout=3600"
git config --local --add credential.helper '!/usr/bin/gh auth git-credential'
# Option B: Nur cache (ohne gh)
git config --local credential.helper "cache --timeout=3600"
# Schritt 3: Umgebungsvariablen setzen (für aktuelle Session)
export GIT_ASKPASS=""
export SSH_ASKPASS=""
```
## GitHub CLI (gh) Setup
Falls noch nicht installiert:
```bash
# Installation
sudo apt install gh
# Authentifizierung
gh auth login
# Status prüfen
gh auth status
```
## Dauerhaft aktivieren
Die Umgebungsvariablen sind bereits in `config/shared/wsl/aliases.sh` enthalten und werden automatisch geladen, wenn die Datei in `~/.bashrc` gesourced wird.
Falls nicht, füge zu deiner `~/.bashrc` hinzu:
```bash
# Am Ende von ~/.bashrc
source /home/r-uu/develop/github/main/config/shared/wsl/aliases.sh
```
## Testen
```bash
# Git-Status abrufen
cd /home/r-uu/develop/github/main
git status
# Fetch testen (sollte keine Fehler zeigen)
git fetch --dry-run
# Push/Pull testen
git pull
git push
```
## IntelliJ Cache
Nach dem Fix kann es sein, dass IntelliJ noch alte Dateien im Cache zeigt.
**Lösung:**
1. In IntelliJ: `File → Invalidate Caches...`
2. Alle Optionen auswählen
3. `Invalidate and Restart` klicken
**Oder:**
- `VCS → Git → Refresh Git Status` (Strg+Alt+Y)
- Im Commit Tool Window: Rechtsklick → `Refresh`
## Konfiguration prüfen
```bash
# Git-Konfiguration anzeigen
cd /home/r-uu/develop/github/main
git config --local --list | grep -E "(askpass|credential)"
# Sollte zeigen:
# core.askpass=
# core.askPass=
# credential.helper=cache --timeout=3600
# credential.helper=!/usr/bin/gh auth git-credential
```
## Weitere Informationen
- **Problem-Tracker**: Diese Lösung behebt das WSL+IntelliJ Interoperabilitätsproblem
- **Langfristige Lösung**: GitHub CLI (gh) als credential helper verwenden
- **Fallback**: Git credential cache (1 Stunde Timeout)
---
**Erstellt**: 2026-01-11  
**Status**: ✅ Implementiert und getestet
