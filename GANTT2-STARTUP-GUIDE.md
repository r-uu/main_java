# Gantt2AppRunner - Startanleitung

**Datum:** 2026-02-09  
**Status:** ✅ BEREIT ZUM START

## Schnellstart

### Option 1: IntelliJ Run Configuration

1. **Öffne:** IntelliJ IDEA
2. **Navigiere zu:** `Gantt2AppRunner.java`
3. **Rechtsklick → Run 'Gantt2AppRunner.main()'**

**Datei-Pfad:**
```
root/app/jeeeraaah/frontend/ui/fx/src/main/java/de/ruu/app/jeeeraaah/frontend/ui/fx/task/gantt2/Gantt2AppRunner.java
```

### Option 2: Maven Exec Plugin

```bash
cd /home/r-uu/develop/github/main/root

mvn exec:java -Dexec.mainClass="de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt2.Gantt2AppRunner" \
  -pl app/jeeeraaah/frontend/ui/fx
```

### Option 3: Java Command (nach mvn package)

```bash
cd /home/r-uu/develop/github/main/root

# 1. Package erstellen
mvn clean package -DskipTests

# 2. Ausführen
java --module-path target/modules \
     --module de.ruu.app.jeeeraaah.frontend.ui.fx/de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt2.Gantt2AppRunner
```

## Voraussetzungen

### ✅ Docker Services müssen laufen

```bash
# Status prüfen
docker ps

# Erwartete Container:
# - postgres (Port 5432)
# - keycloak (Port 8180)
# - jasperserver (Port 8080)
```

**Falls nicht gestartet:**
```bash
cd /home/r-uu/develop/github/main/config/shared/docker
docker-compose up -d
```

### ✅ Liberty Server (optional - nur für Backend-API Tests)

**Wenn Backend-API benötigt wird:**
```bash
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/backend/api/ws.rs
mvn liberty:dev
```

## Was passiert beim Start?

### 1. CDI Container Start (Weld SE)
```
INFO  org.jboss.weld.Bootstrap - WELD-ENV-000001: Weld SE container starting
INFO  de.ruu.lib.cdi.common.CDIExtension - starting bean discovery
INFO  de.ruu.lib.cdi.common.CDIExtension - finished the scanning process
```

### 2. Docker Health Check
```
INFO  de.ruu.lib.docker.health.HealthCheckRunner - 🏥 Docker Environment Health Check
INFO  de.ruu.lib.docker.health.check.DockerDaemonHealthCheck - ✅ Docker daemon is running
INFO  de.ruu.lib.docker.health.check.PostgresDatabaseHealthCheck - ✅ Database 'jeeeraaah' is accessible
INFO  de.ruu.lib.docker.health.check.KeycloakServerHealthCheck - ✅ Keycloak server is running
INFO  de.ruu.lib.docker.health.check.KeycloakRealmHealthCheck - ✅ Keycloak realm 'jeeeraaah-realm' exists
INFO  de.ruu.lib.docker.health.HealthCheckRunner - ✅ ALL SERVICES HEALTHY - Ready to start!
```

### 3. Authentication Dialog
- **Username:** `admin`
- **Password:** `admin`
- **Realm:** `jeeeraaah-realm`

### 4. Gantt2 App Window
- Task Group Selector (Dropdown)
- Filter Controls (Start/End Date, Quarter Presets)
- Gantt Table mit Timeline

## Erwartetes Verhalten

### Task Group Auswahl
1. **Dropdown öffnen:** Task Groups werden geladen
2. **"project jeeeraaah" auswählen:** Tasks werden vom Backend geladen
3. **Konvertierung:** TaskGroupBean → TaskGroupWithTasks (mit TaskFlat)

### Filter
**Standard:** Q1 2025 (01.01.2025 - 31.03.2025)

**Sichtbare Tasks:**
- ✅ Feature Set 1 (+ alle Sub-Tasks)
- ✅ Feature Set 2 (+ alle Sub-Tasks)
- ✅ Feature Set 3 (+ alle Sub-Tasks)

### Gantt Chart Spalten

**Fixierte Spalten (immer sichtbar):**
1. **Expand/Collapse** - Checkbox mit +/-
2. **Task Name** - Eingerückt nach Level

**Scrollbare Spalten:**
- **Tag-Spalten:** 1, 2, 3, 4, ... 31 (Januar)
- **Tag-Spalten:** 1, 2, 3, 4, ... 28 (Februar)
- **Tag-Spalten:** 1, 2, 3, 4, ... 31 (März)

### Hierarchie

**Expand/Collapse:**
```
[+] Feature Set 1                    ████████████████████
[-] Feature Set 2                    ████████████████████
    [+] Feature 2.1 - design         ████████
    [-] Feature 2.2 - implement      ████████
        Task 2.2.1                   ████
        Task 2.2.2                   ████
[+] Feature Set 3                    ████████████████████
```

**Level-Indentation:**
- Level 0: Kein Indent (Feature Sets)
- Level 1: 1x Indent (Features)
- Level 2: 2x Indent (Tasks)
- Level 3: 3x Indent (Subtasks)
- ...

### Timeline Balken

**Farbe:** Blau (`#1E90FF`)
**Position:** Basierend auf start/end Datum
**Breite:** Anzahl Tage zwischen start und end

**Beispiel:**
```
Task: "Feature 1.1 - analyse"
Start: 01.01.2025
End:   15.01.2025
→ Balken von Tag 1 bis Tag 15 (Januar-Spalten)
```

## Troubleshooting

### Problem: "Authentication failed"

**Ursache:** Keycloak nicht erreichbar

**Lösung:**
```bash
# 1. Keycloak Status prüfen
docker ps | grep keycloak

# 2. Falls nicht gestartet
cd /home/r-uu/develop/github/main/config/shared/docker
docker-compose up -d keycloak

# 3. Warten bis ready (ca. 30 Sekunden)
docker logs -f keycloak
# Warten auf: "Admin console listening on..."
```

### Problem: "No task groups available"

**Ursache:** Backend-API nicht erreichbar oder DB leer

**Lösung 1: Backend-API starten**
```bash
cd /home/r-uu/develop/github/main/root/app/jeeeraaah/backend/api/ws.rs
mvn liberty:dev
```

**Lösung 2: Testdaten in DB prüfen**
```bash
# PostgreSQL prüfen
docker exec -it postgres psql -U jeeeraaah -d jeeeraaah

# SQL Query
SELECT id, name FROM task_group;
SELECT id, name, super_task_id FROM task WHERE task_group_id = 1;
```

### Problem: "Nur Januar angezeigt, nicht Q1 2025"

**Ursache:** Filter-Logik für Spalten-Generierung

**Prüfen:**
1. Filter-Werte korrekt? (Start: 01.01.2025, End: 31.03.2025)
2. Apply-Button geklickt?
3. Console-Logs für Fehler prüfen

**Expected Log:**
```
INFO GanttTableController - Generating columns for period: 2025-01-01 to 2025-03-31
DEBUG GanttTableController - Column count: 90 (31 Jan + 28 Feb + 31 Mar)
```

### Problem: "Feature Sets 2 und 3 fehlen"

**Ursache:** Hierarchie-Navigation oder Datenbank

**Prüfen:**
1. **DB Query:**
   ```sql
   SELECT id, name, super_task_id FROM task 
   WHERE task_group_id = 1 
   ORDER BY super_task_id NULLS FIRST, id;
   ```

2. **Console Logs:**
   ```
   DEBUG TaskGroupWithTasks - Main tasks: 3 (Feature Set 1, 2, 3)
   DEBUG GanttTableController - Building row for task: Feature Set 2
   DEBUG GanttTableController - Sub-tasks of Feature Set 2: 4
   ```

3. **Filter Dates:**
   - Prüfen ob Tasks außerhalb Q1 2025 liegen
   - Tasks ohne start/end werden NICHT gefiltert (sollten immer sichtbar sein)

## Logs & Debugging

### Log Levels

**application.properties:**
```properties
# Root level
logging.level.root=INFO

# Gantt2 Package
logging.level.de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt2=DEBUG

# Health Checks
logging.level.de.ruu.lib.docker.health=DEBUG

# CDI
logging.level.org.jboss.weld=INFO
```

### Wichtige Log-Ausgaben

**Start:**
```
INFO  FXCAppRunner - Starting Gantt2App...
INFO  BaseAuthenticatedApp - Performing Docker environment health check...
INFO  HealthCheckRunner - ✅ ALL SERVICES HEALTHY
INFO  BaseAuthenticatedApp - Setup authentication...
```

**Task Group Load:**
```
DEBUG Gantt2Controller - Selected task group: project jeeeraaah (ID: 1)
DEBUG Gantt2Controller - Loading tasks from backend...
DEBUG Gantt2Controller - Received TaskGroupBean with 45 tasks
DEBUG Gantt2Controller - Converting to TaskGroupWithTasks...
DEBUG TaskGroupWithTasks - Created with 45 TaskFlat objects
DEBUG TaskGroupWithTasks - Main tasks: 3
```

**Hierarchie Build:**
```
DEBUG GanttTableController - Building rows recursively...
DEBUG GanttTableController - Row: Feature Set 1 (level 0)
DEBUG GanttTableController -   Row: Feature 1.1 (level 1)
DEBUG GanttTableController -     Row: Task 1.1.1 (level 2)
DEBUG GanttTableController - Total rows: 45
```

## Zusammenfassung

✅ **Gantt2AppRunner ist bereit zum Start!**

**Checkliste:**
- [x] BUILD SUCCESS
- [x] JPMS Module Exports konfiguriert
- [x] TaskFlat & TaskGroupWithTasks implementiert
- [x] Gantt2 Package vollständig
- [x] Docker Services laufen
- [x] Testdaten in DB vorhanden

**Nächster Schritt:** 
```bash
# In IntelliJ:
Right-click on Gantt2AppRunner.java → Run
```

🚀 **Viel Erfolg beim Testen!**

