# IntelliJ Application Run Configuration für DashAppRunner

## Problem

Beim Start von `DashAppRunner` als **Application** (nicht Maven) aus IntelliJ fehlen bestimmte Module,
die auf dem Classpath liegen, aber nicht im Module-Path sind.

Fehler wie:
```
Error occurred during initialization of boot layer
java.lang.module.FindException: Module org.slf4j not found, required by de.ruu.app.jeeeraaah.frontend.ui.fx
```

## Lösung: VM Options in Run Configuration

### 1. Run Configuration öffnen

- Run → Edit Configurations...
- Wähle die **DashAppRunner** Configuration aus (oder erstelle eine neue Application Configuration)

### 2. VM Options hinzufügen

Füge folgende VM Options ein:

```
--add-modules jakarta.annotation,jakarta.inject,org.slf4j
--add-reads de.ruu.app.jeeeraaah.frontend.ui.fx=ALL-UNNAMED
-Dglass.gtk.uiScale=1.5
```

**Erklärung:**

- `--add-modules` : Fügt explizit Module hinzu, die automatisch verfügbar sein sollen
  - `jakarta.annotation` - Jakarta Annotations API (@Inject, etc.)
  - `jakarta.inject` - Jakarta Dependency Injection
  - `org.slf4j` - SLF4J Logging API

- `--add-reads` : Erlaubt dem Frontend-Modul, vom unnamed module (Classpath) zu lesen
  - Dies ist notwendig für CDI/Weld, das auf dem Classpath läuft

- `-Dglass.gtk.uiScale=1.5` : Optional, für HiDPI-Displays

### 3. Main Class

```
de.ruu.app.jeeeraaah.frontend.ui.fx.dash.DashAppRunner
```

### 4. Module Path vs Class Path

**Wichtig:** IntelliJ sollte automatisch die Module erkennen. Falls Probleme auftreten:

- **Modify options** → **Use classpath of module** → Wähle `r-uu.app.jeeeraaah.frontend.ui.fx`
- **Modify options** → **Add dependencies with "provided" scope to classpath** → Aktivieren

## Vollständige Run Configuration Einstellungen

```
Name: DashAppRunner
Main class: de.ruu.app.jeeeraaah.frontend.ui.fx.dash.DashAppRunner
VM options: --add-modules jakarta.annotation,jakarta.inject,org.slf4j --add-reads de.ruu.app.jeeeraaah.frontend.ui.fx=ALL-UNNAMED -Dglass.gtk.uiScale=1.5
Use classpath of module: r-uu.app.jeeeraaah.frontend.ui.fx
```

## Warum funktionierte es gestern?

Möglicherweise wurden VM Options versehentlich entfernt oder die Run Configuration wurde zurückgesetzt.
IntelliJ speichert Run Configurations normalerweise in `.idea/runConfigurations/*.xml`.

## Alternative: Maven Exec Plugin

Falls die Application Configuration weiterhin Probleme macht, kann DashAppRunner auch über Maven gestartet werden:

```bash
cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn exec:java -Dexec.mainClass="de.ruu.app.jeeeraaah.frontend.ui.fx.dash.DashAppRunner"
```

Dies funktioniert, weil die pom.xml bereits die korrekten VM-Argumente im `maven-exec-plugin` konfiguriert hat.

## Debugging

Falls weitere Module fehlen, erweitere die `--add-modules` Liste entsprechend der Fehlermeldung.

Beispiel für weitere häufig benötigte Module:
```
--add-modules jakarta.annotation,jakarta.inject,org.slf4j,jakarta.cdi,jakarta.enterprise.cdi.api
```

## Fazit

Die **Application Configuration** in IntelliJ ist die bevorzugte Methode zum Debuggen.
Sie erfordert nur einmalig die korrekten VM Options in der Run Configuration.
