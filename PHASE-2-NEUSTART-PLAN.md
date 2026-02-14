# Phase 2 Neustart - Verbesserter Plan

## Status
- ✅ Zurückgerollt zu Commit `00ee37d` (Phase 1 - erfolgreich)
- ✅ Alle Phase 2 Änderungen verworfen
- ✅ Sauberer Ausgangspunkt für Neustart

## Probleme beim ersten Versuch

### 1. **Fehlende Vollständigkeitsprüfung**
- Phase 2 wurde unvollständig durchgeführt (nur 3 von 7 Modulen erstellt)
- Keine strukturierte Checkliste
- Zu viele parallele Änderungen ohne Zwischenvalidierung

### 2. **Mangelhafte Testbarkeit**
- Kein Build-Test nach jedem Schritt
- Fehler kumulierten sich
- Schwer zu identifizieren, wo genau das Problem entstand

### 3. **TemporaryMappingHelper Anti-Pattern**
- Stub-Implementierungen statt echte Mapper
- Verschleiert eigentliche Probleme
- Macht Code schwer wartbar

### 4. **Fehlende Abhängigkeits-Analyse**
- Mapper-Abhängigkeiten nicht vollständig erfasst
- Module in falscher Reihenfolge erstellt
- Zyklische Abhängigkeiten nicht erkannt

## Verbesserter Plan

### **Vorbereitung (Tag 1)**

#### A. Vollständige Inventur
```bash
# 1. Welche Mapper existieren aktuell?
find root/app/jeeeraaah -name "Map_*.java" | sort

# 2. Welche Dependencies haben sie?
grep -r "import.*Map_" root/app/jeeeraaah --include="*.java"

# 3. Welche Module verwenden welche Mapper?
grep -r "Mappings\." root/app/jeeeraaah --include="*.java"
```

#### B. Abhängigkeits-Graph erstellen
Dokumentiere in `PHASE-2-DEPENDENCIES.md`:
- Welcher Mapper hängt von welchem ab
- Welche Module verwenden welche Mapper
- Kritische Pfade identifizieren

#### C. Build-Validierung
```bash
# Vor jedem Schritt: Build-Test
cd root && mvn clean compile -DskipTests -q -e

# Erwartung: BUILD SUCCESS
```

### **Phase 2 Durchführung (Tag 2-3)**

#### **Schritt 1: Module erstellen (ohne Code-Verschiebung)**
**Reihenfolge (Bottom-up Dependency):**

1. **mapping.flat.bean** (keine Abhängigkeiten außer Bean/ws.rs)
2. **mapping.bean.lazy** (hängt von Lazy ab)
3. **mapping.jpa.lazy** (hängt von JPA+Lazy ab)

**Pro Modul:**
```bash
# a) Verzeichnis + POMs erstellen
mkdir -p root/app/jeeeraaah/common/api/mapping.MODULE
# b) module-info.java erstellen
# c) beans.xml erstellen (falls CDI)
# d) In parent POM registrieren
# e) In BOM registrieren
# f) BUILD TEST
cd root && mvn clean compile -DskipTests

# ✅ Muss: BUILD SUCCESS
```

#### **Schritt 2: Mapper verschieben (einzeln!)**
**Pro Mapper:**
```bash
# a) git mv SOURCE TARGET
# b) Package in Datei anpassen
# c) Import in abhängigen Dateien anpassen
# d) BUILD TEST
cd root && mvn clean compile -DskipTests

# ✅ Muss: BUILD SUCCESS
```

#### **Schritt 3: Facade-Aufrufe ersetzen**
**Pro Datei:**
```bash
# a) Mappings.toX() → Map_X_Y_Z.INSTANCE.map()
# b) Imports aktualisieren
# c) BUILD TEST
cd root && mvn clean compile -DskipTests

# ✅ Muss: BUILD SUCCESS
```

### **Validierung (kontinuierlich)**

#### Nach JEDEM Schritt:
```bash
# 1. Build muss erfolgreich sein
mvn clean compile -DskipTests -q

# 2. Keine neuen Fehler in IDE
# → IntelliJ "Problems" Tab prüfen

# 3. Git Commit (reversible Checkpoints)
git add -A
git commit -m "Step X: [Beschreibung] - BUILD ✅"
```

#### Nach JEDEM Modul:
```bash
# 1. Full Build mit Tests
mvn clean install

# 2. Manuelle Funktionstest
# → DashAppRunner starten
# → GanttAppRunner starten
```

### **Sicherheitsnetz**

#### Automatische Rollback-Punkte
```bash
# Vor jedem Major Step: Tag erstellen
git tag -a phase2-pre-step-X -m "Checkpoint vor Schritt X"

# Bei Fehler: Rollback zum letzten Tag
git reset --hard phase2-pre-step-X
```

#### Test-First Approach
**VOR jeder Änderung:**
1. Test schreiben, der fehlschlägt
2. Änderung implementieren
3. Test läuft grün
4. Build erfolgreich
5. Commit

## Neue Architektur-Entscheidungen

### 1. **Keine TemporaryHelper**
- Nur echte MapStruct-Mapper
- Wenn Mapper fehlt → Modul fehlt → Modul erstellen
- Keine Shortcuts

### 2. **Granulare Commits**
- 1 Commit = 1 logische Änderung
- Jeder Commit muss buildbar sein
- Aussagekräftige Commit-Messages

### 3. **Dokumentation First**
- `PHASE-2-PROGRESS.md` mit Checkbox-Liste
- Nach jedem Schritt: Checkbox abhaken
- Transparent: Jeder sieht Fortschritt

## Geschätzter Zeitaufwand

### Vorbereitung: 2-3 Stunden
- Inventur
- Abhängigkeits-Analyse
- Plan reviewen

### Durchführung: 6-8 Stunden
- Modul-Erstellung: 2h
- Mapper-Verschiebung: 3h
- Facade-Ersetzung: 2h
- Validierung: 1h

### Gesamt: 8-11 Stunden
**Verteilt über 2-3 Tage** (3-4h pro Tag)

## Erfolgs-Kriterien

### Technisch
- ✅ `mvn clean install` erfolgreich
- ✅ Alle Apps starten (Dash, Gantt)
- ✅ Keine IDE-Fehler
- ✅ Alle Tests grün

### Architektur
- ✅ Jedes Modul hat genau 1 Verantwortung
- ✅ Klare Namenskonvention (SOURCE.TARGET)
- ✅ Keine zyklischen Abhängigkeiten
- ✅ Kein TemporaryMappingHelper

### Wartbarkeit
- ✅ Dokumentation aktuell
- ✅ Jeder Mapper hat eigenes Modul
- ✅ Klare Verzeichnisstruktur

## Start-Kommando

```bash
# 1. Status prüfen
cd /home/r-uu/develop/github/main
git status
git log -1 --oneline

# 2. Sollte zeigen:
# HEAD is now at 00ee37d Phase 1: Rename mapping modules...

# 3. Phase 2 Vorbereitung starten
mkdir -p docs/phase2
touch docs/phase2/INVENTORY.md
touch docs/phase2/DEPENDENCIES.md
touch docs/phase2/PROGRESS.md
```

## Notfall-Kontakte

- **Bei unlösbaren Problemen**: STOPPEN, dokumentieren, Frage stellen
- **Bei Build-Fehlern**: Commit-Message mit ERROR-Tag
- **Bei Unsicherheit**: Checkpoint (Tag) erstellen, Pause machen

---

**Erstellt**: 2026-02-14 21:50  
**Status**: BEREIT FÜR NEUSTART  
**Nächster Schritt**: Vorbereitung Tag 1

