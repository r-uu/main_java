# Git Push Problem behoben

## Problem
IntelliJ versuchte, ein Windows-Java-Executable (`java.exe`) aus WSL heraus für Git-Authentifizierung aufzurufen, was fehlschlug.

**Fehlermeldung:**
```
/mnt/c/Users/r-uu/AppData/Local/JetBrains/IntelliJIdea2025.3/tmp/intellij-git-askpass-wsl-Ubuntu.sh: 3: 
/mnt/c/Users/r-uu/AppData/Local/Programs/IntelliJ IDEA/jbr/bin/java.exe: Exec format error
error: unable to read askpass response
fatal: could not read Username for 'https://github.com': No such device or address
```

## Ursache
IntelliJ generiert automatisch askpass-Skripte in:
- `/mnt/c/Users/r-uu/AppData/Local/JetBrains/IntelliJIdea2025.3/tmp/`

Diese Skripte versuchen, Windows `java.exe` aus WSL aufzurufen, was nicht funktioniert.

## Lösung

### 1. Ersetzen der IntelliJ askpass-Skripte
Die fehlerhaften Skripte wurden durch funktionierende Versionen ersetzt, die `gh auth git-credential` verwenden:

```bash
# Automatischer Fix
./config/shared/scripts/fix-intellij-git-push.sh
```

### 2. Git-Konfiguration bereinigt
```bash
# Entfernte alle problematischen askpass Einträge
git config --global --unset-all core.askpass

# GitHub CLI als Credential Helper
git config --global credential.https://github.com.helper '!gh auth git-credential'
```

### 3. Credentials gespeichert
GitHub Token wurde in `~/.git-credentials` gespeichert für Fallback.

## Aktuelle Konfiguration

```
credential.helper=store
credential.https://github.com.helper=!gh auth git-credential
```

## Status

✅ IntelliJ askpass-Skripte ersetzt (verwenden jetzt gh CLI)
✅ Git-Konfiguration bereinigt
✅ Git Push funktioniert wieder in IntelliJ
✅ Keine Passwort-Abfrage mehr
✅ HTTPS Remote-URL (wie vorher)

## Bei erneutem Auftreten

Falls IntelliJ die Skripte neu generiert, führe aus:
```bash
./config/shared/scripts/fix-intellij-git-push.sh
```

Oder füge zu `~/.bashrc` hinzu für automatischen Fix bei jedem Terminal-Start:
```bash
# Auto-fix IntelliJ Git Push beim Start
if [ -f ~/develop/github/main/config/shared/scripts/fix-intellij-git-push.sh ]; then
    ~/develop/github/main/config/shared/scripts/fix-intellij-git-push.sh > /dev/null 2>&1
fi
```

