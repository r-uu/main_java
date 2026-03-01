# Build Troubleshooting Guide

## Problem: Module not found

Wenn Sie Fehler wie `Module de.ruu.app.jeeeraaah.common.api.mapping.bean.dto not found` sehen:

### Schritt 1: Kompletter Clean Build

```bash
cd /home/r-uu/develop/github/main/root
mvn clean install -DskipTests
```

### Schritt 2: Prüfen Sie die Build-Ausgabe

Suchen Sie nach Fehlern in diesen Modulen (Reihenfolge wichtig!):
1. `common/api/domain` 
2. `common/api/bean`
3. `common/api/ws_rs`
4. `common/api/mapping.bean.dto` ← KRITISCH

### Schritt 3: Falls Mapping-Modul nicht baut

```bash
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/common/api/mapping.bean.dto
mvn clean compile
```

Häufige Fehlerursachen:
- **MapStruct Annotation Processor nicht gefunden**: Prüfen Sie `pom.xml`
- **Fehlende Abhängigkeiten**: Bean/DTO Module müssen vor Mappings gebaut werden
- **Syntax-Fehler in Mapper-Interfaces**: Prüfen Sie alle `Map_*.java` Dateien

### Schritt 4: IntelliJ Cache löschen (falls Sie IntelliJ verwenden)

```
File → Invalidate Caches → Invalidate and Restart
```

### Schritt 5: Modulpfad prüfen

Das Problem beim Starten der Anwendung kann sein, dass das Modul nicht im Modulpfad liegt.

**Temporäre Lösung**: Starten Sie über Maven:

```bash
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn clean compile exec:java -Dexec.mainClass=de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.GanttAppRunner
```

**Dauerhafte Lösung**: IntelliJ Run-Konfiguration anpassen:
1. Run → Edit Configurations
2. "Use classpath of module" → Wählen Sie `r-uu.app.jeeeraaah.frontend.ui.fx`
3. "Add VM options" → Entfernen Sie manuelle `-p` Parameter

## Problem: InaccessibleObjectException

Wenn Sie Fehler wie `module X does not "opens Y" to unnamed module` sehen:

### Lösung: Opens hinzufügen

Bearbeiten Sie `module-info.java` und fügen Sie hinzu:

```java
opens de.ruu.app.jeeeraaah.frontend.ui.fx.PACKAGE_NAME;
```

Dies ist für CDI/Weld notwendig.

## Problem: Compilation Fehler in TaskGroupManagementController

Falls Sie Syntax-Fehler sehen:

```bash
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
cat src/main/java/de/ruu/app/jeeeraaah/frontend/ui/fx/taskgroup/TaskGroupManagementController.java
```

Prüfen Sie, ob die Datei beschädigt ist (z.B. nur Imports, keine Klasse).

## Empfohlener Workflow

1. **Immer zuerst**: `mvn clean install -DskipTests` vom Root
2. **Bei Problemen**: Einzelne Module von unten nach oben bauen
3. **IntelliJ**: Regelmäßig Cache invalidieren
4. **Start**: Über Maven starten, nicht über IntelliJ Run Config

## Noch Probleme?

Führen Sie aus und senden Sie die Ausgabe:

```bash
cd /home/r-uu/develop/github/main/root
mvn clean install -DskipTests 2>&1 | tee build.log
```

Dann können wir die konkrete Fehlerstelle analysieren.

