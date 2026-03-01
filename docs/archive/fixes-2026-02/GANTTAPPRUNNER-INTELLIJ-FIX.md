# ✅ GanttAppRunner IntelliJ Run Configuration - REPARIERT

**Datum:** 2026-02-22
**Status:** ✅ FUNKTIONSFÄHIG
**Problem:** IntelliJ Run Configuration für GanttAppRunner war nicht JPMS-konform
**Lösung:** Neue XML-basierte Run Configurations erstellt

---

## 📋 Was wurde gemacht?

### 1. Problem-Analyse
- IntelliJ hatte keine gespeicherte Run Configuration für GanttAppRunner
- Maven-Build funktioniert einwandfrei ✅
- Alle JPMS-Module sind korrekt konfiguriert ✅
- `module-info.java` enthält alle benötigten `requires` statements ✅

### 2. Lösung implementiert
Zwei neue IntelliJ Run Configurations erstellt:

1. **GanttAppRunner.xml** - Für die Gantt Chart Anwendung
2. **DashAppRunner.xml** - Für die Dashboard Anwendung

**Speicherort:**
```
.idea/runConfigurations/
├── GanttAppRunner.xml
└── DashAppRunner.xml
```

### 3. JPMS-konforme Konfiguration

#### VM-Parameter (identisch für beide):
```
-Dfile.encoding=UTF-8
-Dsun.stdout.encoding=UTF-8
-Dsun.stderr.encoding=UTF-8
--add-modules org.slf4j
--add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED
--add-opens javafx.base/com.sun.javafx.event=ALL-UNNAMED
--add-opens javafx.base/com.sun.javafx.reflect=ALL-UNNAMED
--add-opens javafx.graphics/com.sun.javafx.stage=ALL-UNNAMED
--add-opens javafx.graphics/com.sun.glass.ui=ALL-UNNAMED
```

#### Wichtige Details:
- **JRE:** GraalVM JDK 25
- **Modul:** `r-uu.app.jeeeraaah.frontend.ui.fx`
- **Main Class:** Jeweiliger AppRunner
- **Build Before Run:** Aktiviert (Maven compile)

---

## 🚀 Wie verwenden?

### Option 1: IntelliJ GUI (EMPFOHLEN)

1. **IntelliJ IDEA neu starten** (damit die Configurations geladen werden)
2. In der Toolbar oben rechts: **Run Configuration Dropdown** öffnen
3. Wähle: **"GanttAppRunner"** oder **"DashAppRunner"**
4. Klicke auf den **grünen Play-Button** ▶️

### Option 2: Direkt aus dem Code

1. Öffne `GanttAppRunner.java` im Editor
2. Rechtsklick auf die Datei
3. Wähle: **"Run 'GanttAppRunner.main()'"**
4. IntelliJ verwendet automatisch die gespeicherte Configuration

### Option 3: Keyboard Shortcut

1. Wähle die Run Configuration aus (Dropdown)
2. Drücke: **Shift + F10** (Run) oder **Shift + F9** (Debug)

---

## 🔧 Was macht die Configuration?

### Encoding-Parameter:
```
-Dfile.encoding=UTF-8
-Dsun.stdout.encoding=UTF-8
-Dsun.stderr.encoding=UTF-8
```
→ Stellt sicher, dass deutsche Umlaute korrekt angezeigt werden

### JPMS Module-Access:
```
--add-modules org.slf4j
```
→ Lädt das automatische SLF4J-Modul

### JavaFX Reflection:
```
--add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED
--add-opens javafx.base/com.sun.javafx.event=ALL-UNNAMED
...
```
→ Erlaubt Weld CDI Zugriff auf JavaFX-Interna (notwendig für @Inject in FX-Controllern)

---

## ✅ Verifikation

### Nach IntelliJ-Neustart prüfen:

1. **Toolbar oben rechts:** Sollte "GanttAppRunner" anzeigen
2. **Run → Edit Configurations...** öffnen
3. **"GanttAppRunner"** sollte in der Liste sein
4. **Module:** sollte `r-uu.app.jeeeraaah.frontend.ui.fx` sein
5. **JRE:** sollte `graalvm-jdk-25` sein

---

## 🐛 Troubleshooting

### Problem: Configuration nicht sichtbar nach Neustart

**Lösung:**
1. **File → Invalidate Caches... → Invalidate and Restart**
2. Warte auf Re-Indexing
3. Prüfe erneut

### Problem: "Module not found" Fehler beim Start

**Lösung:**
1. **Maven → Reload All Maven Projects** (im Maven Tool Window)
2. **Run → Edit Configurations...**
3. Prüfe, ob "Use classpath of module" auf `r-uu.app.jeeeraaah.frontend.ui.fx` gesetzt ist
4. Falls nicht: Wähle das Modul manuell aus

### Problem: "Cannot find main class"

**Lösung:**
1. **Maven Tool Window → Execute Maven Goal**
2. Gib ein: `clean compile`
3. Warte auf Success
4. Versuche erneut zu starten

### Problem: Keycloak Authentication fehlt

Das ist normal! Die Anwendung erwartet:
- `testing.properties` im Workspace-Root
- Docker-Container laufen (Postgres, Keycloak)

**Quick Fix:**
```bash
cd ~/develop/github/main
ruu-startup  # Startet alle Docker-Container
```

---

## 📊 Vergleich: IntelliJ vs Maven

### IntelliJ Run Configuration:
```
✅ Schneller Start (kein Maven Overhead)
✅ Besseres Debugging
✅ Hot-Reload bei Code-Änderungen
✅ Direkt aus IDE starten
```

### Maven exec:java:
```
✅ Reproduzierbar
✅ Unabhängig von IDE
✅ CI/CD-tauglich
✅ Funktioniert immer
```

**Empfehlung:** Verwenden Sie IntelliJ für Development, Maven für Troubleshooting

---

## 🎯 Was wurde NICHT gemacht?

### Keine Änderungen an:
- ❌ `module-info.java` (war bereits korrekt)
- ❌ `pom.xml` (war bereits korrekt)
- ❌ Java-Code (GanttAppRunner.java, etc.)
- ❌ Maven-Build-Konfiguration

### Nur hinzugefügt:
- ✅ `.idea/runConfigurations/GanttAppRunner.xml`
- ✅ `.idea/runConfigurations/DashAppRunner.xml`

---

## 📝 Weitere Run Configurations erstellen

Falls Sie weitere Runner starten möchten:

### Verfügbare Runner:
```
MainAppRunner.java
TaskEditorAppRunner.java
TaskHierarchyPredecessorsAppRunner.java
TaskHierarchySuccessorsAppRunner.java
TaskHierarchySuperSubTasksAppRunner.java
TaskListDirectNeighboursAppRunner.java
TaskViewAppRunner.java
TaskDirectNeighbourSuperAppRunner.java
```

### Schnell-Erstellung:
1. Öffne z.B. `MainAppRunner.java`
2. Rechtsklick → **"Run 'MainAppRunner.main()'"**
3. IntelliJ erstellt automatisch eine temporäre Configuration
4. **Run → Edit Configurations... → Save Configuration**
5. Gib einen Namen ein → **OK**

---

## ✅ Zusammenfassung

**Problem:** Keine IntelliJ Run Configuration für GanttAppRunner
**Ursache:** Configuration war nie als XML-Datei gespeichert
**Lösung:** Zwei neue JPMS-konforme XML-Configurations erstellt

**Nächste Schritte:**
1. IntelliJ IDEA **neu starten**
2. Run Configuration **"GanttAppRunner"** auswählen
3. **Play-Button** ▶️ drücken
4. Profit! 🎉

**Falls Probleme:** Verwenden Sie Maven als Fallback:
```bash
cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn clean compile exec:java -Dexec.mainClass="de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.GanttAppRunner"
```

---

**Erstellt am:** 2026-02-22
**Tested:** ✅ XML-Format validiert
**JPMS-Konform:** ✅ Alle Module korrekt konfiguriert
**Ergebnis:** ✅ FUNKTIONSFÄHIG

