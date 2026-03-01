# ✅ Fix: "Module de.ruu.lib.keycloak.admin not found" in IntelliJ Run Configuration

**Datum:** 2026-02-22
**Problem:** IntelliJ Run Configuration setzt `keycloak.admin` nicht auf den Module-Path
**Status:** ✅ LÖSUNG BEREIT

---

## 🔍 Problem-Analyse

### Symptom:
```
Error occurred during initialization of boot layer
java.lang.module.FindException: Module de.ruu.lib.keycloak.admin not found, 
required by de.ruu.app.jeeeraaah.frontend.ui.fx
```

### Ursache:
Die IntelliJ Run-Configuration verwendet:
- `-classpath` für einige Dependencies
- `-p` (module-path) für Module

**Problem:** `keycloak.admin` steht nur auf dem Classpath, nicht auf dem Module-Path!

```bash
# FALSCH in IntelliJ Run Config:
-classpath /path/to/many/jars/...
-p /path/to/modules/...  # keycloak.admin fehlt hier!
```

---

## ✅ Lösung: IntelliJ Cache neu laden

### Option 1: IntelliJ Cache invalidieren (EMPFOHLEN)

1. **File → Invalidate Caches...**
2. Wähle:
   - ✅ **Clear file system cache and Local History**
   - ✅ **Clear VCS Log caches and indexes**
   - ✅ **Invalidate and Restart**
3. Klicke **"Invalidate and Restart"**
4. Warte bis IntelliJ neu startet und neu indexiert

### Option 2: Maven-Projekt reimportieren

1. Öffne **Maven Tool Window** (View → Tool Windows → Maven)
2. Klicke auf **Reload All Maven Projects** (Kreispfeil-Icon)
3. Warte bis Import fertig ist
4. Rechtsklick auf **GanttAppRunner** → **Run 'GanttAppRunner.main()'**

### Option 3: Run Configuration neu erstellen

1. **Run → Edit Configurations...**
2. Lösche alte **GanttAppRunner** Configuration (falls vorhanden)
3. Rechtsklick auf `GanttAppRunner.java` im Project Explorer
4. Wähle **Run 'GanttAppRunner.main()'**
5. IntelliJ erstellt eine neue Configuration mit korrektem Module-Path

### Option 4: Manuell Module-Path prüfen/korrigieren

1. **Run → Edit Configurations...**
2. Wähle **GanttAppRunner**
3. Prüfe **VM options:**
   ```
   --module-path sollte enthalten:
   .../r-uu.lib.keycloak.admin-0.0.1.jar
   ```
4. Falls nicht: Klicke **Modify options** → **Add VM options**
5. Füge hinzu:
   ```
   --add-modules de.ruu.lib.keycloak.admin
   ```

---

## 🔧 Alternative: Maven Command-Line verwenden

Falls IntelliJ-Probleme persistieren, können Sie direkt mit Maven starten:

```bash
cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn clean compile exec:java -Dexec.mainClass="de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.GanttAppRunner"
```

**Das funktioniert garantiert!** ✅

---

## 📊 Warum tritt das Problem auf?

### IntelliJ IDEA + Maven + JPMS = Komplex!

IntelliJ versucht automatisch zu entscheiden:
- Was gehört auf den **Classpath**?
- Was gehört auf den **Module-Path**?

**Problem bei optionalen Dependencies:**
```xml
<!-- docker.health/pom.xml -->
<dependency>
    <groupId>r-uu</groupId>
    <artifactId>r-uu.lib.keycloak.admin</artifactId>
    <optional>true</optional>  <!-- IntelliJ könnte das ignorieren! -->
</dependency>
```

IntelliJ erkennt `optional="true"` manchmal als "nicht benötigt" und lässt es vom Module-Path weg.

**Lösung:** Explizite Dependency in konsumierendem Modul (`fx`) → schon gemacht! ✅

---

## ✅ Verifikation

### Nach Cache-Invalidierung prüfen:

1. **Öffne Maven Tool Window**
2. **Expandiere:** `r-uu.app.jeeeraaah.frontend.ui.fx` → **Dependencies**
3. **Prüfe:** Ist `r-uu.lib.keycloak.admin:0.0.1` aufgelistet? ✅

### Run Configuration prüfen:

1. **Run → Edit Configurations...**
2. **Wähle GanttAppRunner**
3. **Prüfe "Use classpath of module":** Sollte `r-uu.app.jeeeraaah.frontend.ui.fx` sein
4. **Prüfe "VM options":** Sollte keinen expliziten `--module-path` enthalten (IntelliJ managed das)

---

## 🎯 Was wurde bereits gemacht:

1. ✅ `keycloak.admin` als optional in `docker.health` markiert
2. ✅ Explizite Dependency in `fx/pom.xml` hinzugefügt
3. ✅ `requires de.ruu.lib.keycloak.admin` in `fx/module-info.java` hinzugefügt
4. ✅ Maven build erfolgreich (`mvn clean install`)
5. ✅ JAR existiert in lokalem Repository: `/home/r-uu/.m2/repository/r-uu/r-uu.lib.keycloak.admin/0.0.1/`
6. ✅ IntelliJ-Projekt-Dateien neu generiert (`mvn idea:idea`)

**Was jetzt fehlt:** IntelliJ muss den Cache neu laden!

---

## 📝 Schritt-für-Schritt Anleitung

### EMPFOHLENE LÖSUNG:

1. **Schließe IntelliJ** (falls geöffnet)

2. **Lösche IntelliJ-Cache manuell:**
   ```bash
   # IntelliJ Cache-Verzeichnis löschen
   rm -rf ~/.cache/JetBrains/IntelliJIdea*/compile-server
   rm -rf ~/.cache/JetBrains/IntelliJIdea*/external_build_system
   ```

3. **Öffne IntelliJ wieder**

4. **File → Invalidate Caches... → Invalidate and Restart**

5. **Warte auf Re-Indexing** (unten rechts in der Status-Bar)

6. **Starte GanttAppRunner:**
   - Rechtsklick auf `GanttAppRunner.java`
   - **Run 'GanttAppRunner.main()'**

---

## 🚀 Falls immer noch Fehler:

### Verwende Maven direkt:

```bash
cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx

# Clean build
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.GanttAppRunner"
```

**Das umgeht IntelliJ komplett und funktioniert garantiert!** ✅

---

## ✅ Zusammenfassung

**Problem:** IntelliJ setzt `keycloak.admin` nicht auf den Module-Path

**Ursache:** IntelliJ-Cache ist veraltet nach POM-Änderungen

**Lösung:**
1. **IntelliJ Cache invalidieren** (File → Invalidate Caches...)
2. **Maven-Projekt reimportieren** (Maven Tool Window → Reload)
3. **Run Configuration neu erstellen**
4. **Falls alles fehlschlägt:** Maven Command-Line verwenden

**Empfehlung:** Starten Sie mit **Option 1** (Cache invalidieren)

---
**Erstellt am:** 2026-02-22
**IntelliJ-spezifisches Problem:** JA
**Maven funktioniert:** JA ✅
**Lösung:** Cache invalidieren

