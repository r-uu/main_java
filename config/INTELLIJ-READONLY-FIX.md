# ✅ IntelliJ Read-Only Dateien Problem Gelöst

**Datum:** 2026-01-18  
**Status:** ✅ **GELÖST**

---

## 🔴 PROBLEM

IntelliJ zeigt **"Clear read-only status"** Dialog beim Löschen von Testdateien an.

**Symptome:**
- Testdateien (PDFs, DOCX) können in IntelliJ nicht gelöscht werden
- Dialog: "Clear read-only status"
- Dateien erscheinen als schreibgeschützt

---

## 🔍 URSACHE

Das Problem tritt auf bei **WSL + Windows + IntelliJ** Kombination:

1. **Windows-Attribute über WSL**
   - Dateien von Windows/IntelliJ erstellt
   - Read-Only Attribute wird über WSL nicht korrekt interpretiert
   - IntelliJ kann Attribute nicht ändern

2. **Fehlerhafte Pfade**
   - Dateien mit Windows-Pfaden im Namen: `\\wsl.localhost\Ubuntu\...`
   - Entstehen durch falsche Pfad-Auflösung
   - Können nicht normal gelöscht werden

3. **Generierte Dateien**
   - PDF/DOCX Testausgaben von JasperReports
   - Sollten eigentlich in `.gitignore` sein
   - Häufen sich bei Tests an

---

## ✅ LÖSUNG

### Schnelle Lösung (Manuelle Kommandos):

```bash
# 1. PDF/DOCX im Hauptverzeichnis löschen
cd /home/r-uu/develop/github/main/root/lib/office/word/jasperreports
rm -f *.pdf *.docx

# 2. Output-Verzeichnis löschen
rm -rf server/output/

# 3. Fehlerhafte Windows-Pfade löschen
cd /home/r-uu/develop/github/main
find . -name "*wsl.localhost*" -delete
```

### Empfohlene Lösung (Cleanup-Skript):

```bash
# Alias verwenden (mit Bestätigung)
ruu-jasper-cleanup

# Oder direkt (ohne Bestätigung)
ruu-jasper-cleanup -f

# Oder manuell
bash /home/r-uu/develop/github/main/config/shared/scripts/cleanup-jasperreports.sh
```

---

## 🛠️ WAS WURDE BEHOBEN

### 1. Alle Testdateien gelöscht:
```bash
✓ PDF/DOCX im jasperreports/ Hauptverzeichnis gelöscht
✓ server/output/ Verzeichnis gelöscht
✓ Dateien mit fehlerhaften Windows-Pfaden gelöscht
```

### 2. Cleanup-Skript erstellt:
- **Pfad:** `config/shared/scripts/cleanup-jasperreports.sh`
- **Funktion:** Löscht alle generierten Testdateien
- **Features:**
  - Zeigt Anzahl der zu löschenden Dateien
  - Fragt um Bestätigung (außer mit `-f` Flag)
  - Löscht PDF/DOCX
  - Löscht server/output/
  - Entfernt fehlerhafte Windows-Pfade
  - Git cleanup für untracked files

### 3. Alias hinzugefügt:
```bash
alias ruu-jasper-cleanup='bash $RUU_CONFIG/shared/scripts/cleanup-jasperreports.sh'
```

### 4. .gitignore bereits aktualisiert (vorher):
```gitignore
# JasperReports - Generierte Output-Dateien
**/jasperreports/*.pdf
**/jasperreports/*.docx
**/jasperreports/output/
**/jasperreports/server/output/
```

---

## 🚀 VERWENDUNG

### Nach Tests aufräumen:

```bash
# Mit Bestätigung
ruu-jasper-cleanup

# Ohne Bestätigung (force)
bash /home/r-uu/develop/github/main/config/shared/scripts/cleanup-jasperreports.sh -f
```

### Prüfen, ob Dateien übrig sind:

```bash
cd /home/r-uu/develop/github/main/root/sandbox/office/microsoft/word/jasperreports
ls -la *.pdf *.docx 2>/dev/null || echo "Keine PDF/DOCX Dateien"
```

### Git-Status prüfen:

```bash
ruu-status
# oder
git status --short | grep jasperreports
```

---

## 🔧 WARUM PASSIERT DAS?

### Problem mit WSL + Windows + IntelliJ:

1. **IntelliJ läuft unter Windows**
2. **Projekt liegt in WSL-Filesystem** (`\\wsl.localhost\Ubuntu\...`)
3. **Dateien werden von IntelliJ/Java erstellt**
4. **Windows setzt Read-Only Attribute** (manchmal)
5. **WSL interpretiert Attribute anders**
6. **IntelliJ kann Attribute nicht ändern**

### Lösung: Terminal verwenden

**Im Terminal (WSL) funktioniert `rm` immer:**
```bash
rm -f datei.pdf  # Ignoriert Read-Only
rm -rf ordner/   # Löscht rekursiv
```

**In IntelliJ gibt es Probleme:**
- Dialog: "Clear read-only status"
- Löschen schlägt fehl
- Attribute können nicht geändert werden

---

## 📋 PRÄVENTION

### 1. Generierte Dateien automatisch ignorieren

Die `.gitignore` ist bereits konfiguriert:
```gitignore
**/jasperreports/*.pdf
**/jasperreports/*.docx
**/jasperreports/output/
```

→ Neue generierte Dateien werden automatisch ignoriert

### 2. Regelmäßig aufräumen

```bash
# Nach jedem Test-Durchlauf
ruu-jasper-cleanup -f

# Oder als Post-Build Hook
```

### 3. Output in Docker Container generieren

Der JasperReports Service im Docker-Container schreibt nach:
```
server/output/  # Wird automatisch ignoriert
```

→ Einfach zu löschen: `rm -rf server/output/`

---

## 🎯 BEST PRACTICES

### DO ✅

```bash
# Cleanup über Terminal/Skript
ruu-jasper-cleanup

# Git clean für untracked files
git clean -fd root/lib/office/word/jasperreports/

# Generierte Dateien in .gitignore
echo "**/output/*.pdf" >> .gitignore
```

### DON'T ❌

```bash
# Nicht in IntelliJ manuell löschen (WSL-Probleme)
# Nicht generierte Dateien ins Git committen
# Nicht fehlerhafte Pfade ignorieren
```

---

## 🆘 TROUBLESHOOTING

### Problem: Datei lässt sich nicht löschen

```bash
# Im Terminal (WSL)
rm -f pfad/zur/datei

# Wenn das nicht funktioniert (Permission denied)
sudo rm -f pfad/zur/datei

# Komplettes Verzeichnis
rm -rf pfad/zum/verzeichnis/
```

### Problem: Fehlerhafte Pfade mit Backslashes

```bash
# Suche nach fehlerhaften Dateien
find . -name "*wsl*" -o -name "*localhost*"

# Lösche sie
find . -name "*wsl*" -delete
```

### Problem: IntelliJ Cache zeigt gelöschte Dateien

```bash
# IntelliJ Cache invalidieren
# File → Invalidate Caches → Invalidate and Restart

# Oder nur Git-Status neu laden
# Git → Refresh File Status
```

---

## ✅ CHECKLISTE

Nach JasperReports Tests:

- [ ] `ruu-jasper-cleanup` ausführen
- [ ] Git-Status prüfen: `ruu-status`
- [ ] IntelliJ refreshen (falls nötig)
- [ ] Keine PDF/DOCX im Projekt-Verzeichnis
- [ ] server/output/ gelöscht
- [ ] Keine Dateien mit `?` Icon in IntelliJ

---

## 📚 SIEHE AUCH

- `config/shared/scripts/cleanup-jasperreports.sh` - Cleanup-Skript
- `config/INTELLIJ-GIT-STATUS-ICONS.md` - Git-Status Erklärung
- `.gitignore` - Ignore-Regeln für generierte Dateien
- `ruu-help-git` - Git-Aliase

---

## 📝 ZUSAMMENFASSUNG

**Problem:** IntelliJ kann Read-Only Testdateien in WSL nicht löschen

**Ursache:** Windows-Attribute über WSL + fehlerhafte Pfade

**Lösung:** 
1. ✅ Cleanup-Skript erstellt: `cleanup-jasperreports.sh`
2. ✅ Alias hinzugefügt: `ruu-jasper-cleanup`
3. ✅ Alle Testdateien gelöscht
4. ✅ .gitignore aktualisiert (bereits vorher)

**Verwendung:** `ruu-jasper-cleanup` nach Tests ausführen

---

✅ **Problem gelöst!**  
✅ **Cleanup-Skript verfügbar!**  
✅ **Generierte Dateien werden ignoriert!**

