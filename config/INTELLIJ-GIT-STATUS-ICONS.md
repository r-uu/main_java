# 📌 IntelliJ Git Status Icons - Erklärung

## ❓ Was bedeutet das ? Icon?

Das **Fragezeichen (?)** in IntelliJ's Project Explorer bedeutet:

**Die Datei ist NICHT unter Git-Versionskontrolle (untracked file)**

---

## 🎨 Git Status Icons in IntelliJ

| Icon | Farbe | Bedeutung | Git Status |
|------|-------|-----------|------------|
| (kein Icon) | Grau/Normal | Versioniert, unverändert | `(clean)` |
| 📝 | Blau | Geändert | `M` (modified) |
| ➕ | Grün | Hinzugefügt (staged) | `A` (added) |
| ❓ | Braun/Orange | Nicht versioniert | `?` (untracked) |
| 🔴 | Rot | Konflikt oder gelöscht | `D` (deleted) / Konflikt |
| 📋 | Grau durchgestrichen | Ignoriert (.gitignore) | (ignored) |

---

## 🔍 URSACHEN

### 1. Neue Datei (häufigste Ursache)
Die Datei wurde gerade erstellt und noch nicht zu Git hinzugefügt.

**Lösung:**
```bash
git add <datei>
# oder in IntelliJ: Rechtsklick → Git → Add
```

### 2. IntelliJ Cache nicht synchron
Git kennt die Datei bereits, aber IntelliJ's Cache ist veraltet.

**Lösung:**
```bash
# In IntelliJ
File → Invalidate Caches... → Invalidate and Restart

# Oder Git-Status manuell prüfen
git status --short
```

### 3. Datei sollte ignoriert werden
Die Datei ist generiert oder enthält lokale Konfiguration.

**Lösung:**
```bash
# Zur .gitignore hinzufügen
echo "pfad/zur/datei" >> .gitignore

# Oder in IntelliJ: Rechtsklick → Git → Add to .gitignore
```

### 4. Datei mit falschen Pfaden
Manchmal entstehen Dateien mit Windows-Pfaden in WSL.

**Lösung:**
```bash
# Prüfe auf seltsame Dateien
git status --short | grep "?"

# Lösche fehlerhafte Dateien
git clean -fd  # Vorsicht! Löscht alle untracked files
```

---

## ✅ LÖSUNGEN

### Datei versionieren (wenn sie ins Repo soll):

**In IntelliJ:**
1. Rechtsklick auf Datei mit `?`
2. **Git → Add** wählen
3. Datei wird grün (staged)
4. Committen: `Git → Commit...`

**Im Terminal:**
```bash
cd /home/r-uu/develop/github/main
git add config/KEYCLOAK-HEALTH-CHECK-FIX.md
git commit -m "Dokumentation hinzugefügt"
git push
```

### Datei ignorieren (wenn sie NICHT ins Repo soll):

**In IntelliJ:**
1. Rechtsklick auf Datei
2. **Git → Add to .gitignore** wählen
3. Wähle `.gitignore` (Projekt-root)

**Im Terminal:**
```bash
# Einzelne Datei
echo "pfad/zur/datei.pdf" >> .gitignore

# Pattern für mehrere Dateien
echo "**/*.pdf" >> .gitignore
echo "**/output/" >> .gitignore
```

### IntelliJ Cache neu laden:

1. **File → Invalidate Caches...**
2. Wähle: **Invalidate and Restart**
3. Warte auf Neustart und Indexierung

---

## 🔧 FÜR DEIN PROJEKT

### Bereits behoben:

✅ `.gitignore` wurde aktualisiert für:
```gitignore
# JasperReports - Generierte Output-Dateien
**/jasperreports/*.pdf
**/jasperreports/*.docx
**/jasperreports/output/
**/jasperreports/server/output/
```

### Was das bedeutet:

- ✅ Alle `.pdf` und `.docx` Dateien in `jasperreports/` werden ignoriert
- ✅ Die Ordner `output/` und `server/output/` werden ignoriert
- ✅ Diese Dateien erscheinen nicht mehr mit `?` in IntelliJ

### Nach dem Update:

1. **IntelliJ neustarten** oder Cache invalidieren
2. Die generierten Dateien sollten nun grau durchgestrichen sein (ignoriert)
3. Neue generierte Dateien werden automatisch ignoriert

---

## 🚀 PRAKTISCHE TIPPS

### Alle untracked Files anzeigen:

```bash
cd /home/r-uu/develop/github/main
git status --short | grep "^??"
```

### Alle untracked Files löschen (VORSICHT!):

```bash
# Dry-run (zeigt nur, was gelöscht würde)
git clean -n

# Tatsächlich löschen
git clean -fd
```

### Git-Status mit IntelliJ synchronisieren:

```bash
# Im Terminal
git status

# Dann in IntelliJ
Git → Refresh File Status (Cmd/Ctrl + Alt + A)
```

### Häufige Patterns für .gitignore:

```gitignore
# Alle PDFs ignorieren
*.pdf

# PDFs nur in bestimmtem Ordner
output/*.pdf

# Rekursiv alle PDFs
**/*.pdf

# Gesamten Ordner
output/
target/

# Alle außer einer Datei
*.pdf
!wichtig.pdf
```

---

## ⚙️ INTELLIJ GIT-EINSTELLUNGEN

### Git-Status automatisch aktualisieren:

1. **Settings → Version Control → Git**
2. ✅ **Update files on checkout**
3. ✅ **Check Git working tree on changes**

### Commit-Dialog anpassen:

1. **Settings → Version Control → Commit**
2. ✅ **Use non-modal commit interface**
3. ✅ **Check for unstaged files**

### Ignored Files anzeigen:

1. **Settings → Version Control → File Status Colors**
2. Passe Farben an (optional)

---

## 📋 HÄUFIGE SZENARIEN

### Szenario 1: Neue Markdown-Doku mit ?

```bash
# Datei ist neu und soll ins Repo
git add config/NEUE-DOKU.md
git commit -m "Neue Dokumentation"
```

### Szenario 2: Generierte PDF mit ?

```bash
# Datei ist generiert und soll NICHT ins Repo
echo "**/*.pdf" >> .gitignore
git clean -fd  # Entfernt alle untracked PDFs
```

### Szenario 3: Viele ? Icons nach Pull

```bash
# Wahrscheinlich Cache-Problem
# In IntelliJ: File → Invalidate Caches → Restart
```

### Szenario 4: Datei hat ? obwohl sie committed wurde

```bash
# Cache-Problem, prüfe Git-Status
git status --short config/datei.md

# Wenn Git "clean" sagt, Cache invalidieren
# File → Invalidate Caches → Restart
```

---

## ✅ CHECKLISTE

Wenn eine Datei ein `?` Icon hat:

- [ ] Prüfe Git-Status: `git status --short pfad/zur/datei`
- [ ] Ist es eine neue Datei? → `git add` oder ignorieren
- [ ] Ist es eine generierte Datei? → Zur `.gitignore` hinzufügen
- [ ] Ist der Pfad korrekt? → Keine Windows-Pfade in WSL
- [ ] Cache-Problem? → IntelliJ Cache invalidieren
- [ ] Soll die Datei ignoriert werden? → Pattern in `.gitignore`

---

## 📚 SIEHE AUCH

- `.gitignore` - Projektweite Ignore-Regeln
- `ruu-status` - Alias für `git status`
- `ruu-help-git` - Git-Aliase anzeigen

---

**Das ? Icon bedeutet: Datei ist nicht versioniert (untracked)**

**Lösung: Entweder `git add` oder zur `.gitignore` hinzufügen!** ✅

