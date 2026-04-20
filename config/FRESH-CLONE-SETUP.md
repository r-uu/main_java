# Fresh Clone Setup - Zusammenfassung

## Was wurde gemacht

### 1. Altes Projekt gesichert
```bash
cd /home/r-uu/develop/github
mv main main.backup.20260111_235114
```

Das alte Projekt ist noch da, falls du etwas brauchst!

### 2. Frisch vom Remote geklont
```bash
git clone https://github.com/r-uu/main.git
```

✅ **2583 Objekte erfolgreich heruntergeladen**
✅ **1245 Dateien aktualisiert**

### 3. Git für WSL+IntelliJ konfiguriert
```bash
cd main
bash config/shared/scripts/fix-git-wsl-intellij.sh
```

✅ Git askpass deaktiviert
✅ GitHub CLI als credential helper konfiguriert
✅ Keine "Exec format error" mehr!

## Aktueller Status

### ✅ Was funktioniert
- Git pull/push im Terminal
- Frische Kopie vom Remote
- Alle Dateien sind auf dem neuesten Stand
- Git-Konfiguration ist korrekt

### ⚠️ Bekannte Änderungen
Die Shell-Scripts hatten Windows-Zeilenenden (CRLF) und wurden auf Unix (LF) konvertiert:
```
modified:   config/shared/scripts/*.sh (12 Dateien)
```

**Optionen:**
1. Änderungen zurücksetzen: `git restore config/shared/scripts/*.sh`
2. Committen: `git add -A && git commit -m "fix: Zeilenenden für Shell-Scripts"`

## Nächste Schritte

### Option 1: IntelliJ neu öffnen (EMPFOHLEN)

**Schritt 1:** Projekt in IntelliJ schließen
- File → Close Project

**Schritt 2:** Git-Pfad in IntelliJ konfigurieren
- File → Settings (Ctrl+Alt+S)
- Version Control → Git
- Path to Git executable: `\\wsl.localhost\Ubuntu\usr\bin\git`
- SSH executable: `Native`
- Test klicken → OK

**Schritt 3:** Projekt wieder öffnen
- Open → `\\wsl.localhost\Ubuntu\home\r-uu\develop\github\main`

**Schritt 4:** Test Update Project
- Ctrl+T oder VCS → Update Project
- Sollte jetzt ohne "Exec format error" funktionieren!

### Option 2: Terminal verwenden (SCHNELL)

Einfach das IntelliJ Terminal verwenden (Alt+F12):
```bash
# Aliase laden (falls nicht automatisch)
source ~/.bashrc

# Git-Operationen
ruu-pull
ruu-push
ruu-status
```

Funktioniert sofort, keine weitere Konfiguration nötig!

### Option 3: SSH verwenden (DAUERHAFT)

Für die beste Erfahrung, wechsle zu SSH:
```bash
cd /home/r-uu/develop/github/java/main

# Remote auf SSH umstellen
git remote set-url origin git@github.com:r-uu/main.git

# Test
ssh -T git@github.com

# Ab jetzt: keine Credential-Probleme mehr!
```

## Verzeichnis-Struktur

```
/home/r-uu/develop/github/
├── main/                           ← NEU, frisch geklont ✅
├── main.backup.20260111_235114/    ← Backup vom alten Stand
├── space-02/                       ← Original, unverändert
├── backup.r-uu.bom/                ← Altes Backup
└── backup.r-uu.root/               ← Altes Backup
```

## Quick Commands

```bash
# Zum neuen Projekt wechseln
cd /home/r-uu/develop/github/java/main

# Git Status
git status

# Aliase laden
source ~/.bashrc

# Git Pull testen
ruu-pull

# Alte Backups aufräumen (VORSICHT!)
# rm -rf /home/r-uu/develop/github/java/main.backup.*
# rm -rf /home/r-uu/develop/github/backup.r-uu.*
```

## Troubleshooting

### Problem: "Exec format error" tritt immer noch auf
**Lösung:** IntelliJ Git-Pfad noch nicht konfiguriert (siehe Option 1 oben)

### Problem: Aliase funktionieren nicht
**Lösung:** 
```bash
source ~/.bashrc
# oder
source /home/r-uu/develop/github/java/main/config/shared/wsl/aliases.sh
```

### Problem: Ich brauche Dateien aus dem alten Projekt
**Lösung:**
```bash
# Kopiere aus dem Backup
cp /home/r-uu/develop/github/java/main.backup.20260111_235114/some-file.txt \
   /home/r-uu/develop/github/java/main/
```

### Problem: Scripts haben falsche Zeilenenden
**Lösung:**
```bash
cd /home/r-uu/develop/github/java/main
bash setup-fresh-clone.sh
```

## IntelliJ IDEA spezifisch

### Projekt-Pfad in IntelliJ
```
\\wsl.localhost\Ubuntu\home\r-uu\develop\github\main
```

### Git Executable in IntelliJ
```
\\wsl.localhost\Ubuntu\usr\bin\git
```

### Terminal in IntelliJ
- Automatisch WSL verwenden
- Alle ruu-* Aliase verfügbar
- Git-Operationen funktionieren einwandfrei

## Erfolgs-Test

```bash
# Im Terminal (Alt+F12 in IntelliJ)
cd /home/r-uu/develop/github/java/main

# Test 1: Git funktioniert
git status
# Erwartung: "Your branch is up to date"

# Test 2: Aliase funktionieren
ruu-help
# Erwartung: Liste aller Aliase

# Test 3: Pull funktioniert
ruu-pull
# Erwartung: "Already up to date" (keine Fehler!)
```

Wenn alle 3 Tests erfolgreich sind: **✅ Alles funktioniert!**

## Zusammenfassung

✅ **Projekt frisch geklont** - Sauberer Zustand
✅ **Git konfiguriert** - WSL+IntelliJ kompatibel
✅ **Backup erstellt** - Alte Version ist sicher
✅ **Dokumentiert** - Alle Schritte nachvollziehbar

**Empfehlung:** Nutze das Terminal in IntelliJ (Alt+F12) für Git-Operationen, bis du IntelliJ's Git-Pfad konfiguriert hast.

---

**Erstellt:** 2026-01-11 23:51
**Repository:** https://github.com/r-uu/main.git
**Lokaler Pfad:** /home/r-uu/develop/github/java/main

