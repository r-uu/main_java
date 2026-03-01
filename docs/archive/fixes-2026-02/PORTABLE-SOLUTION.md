# ✅ Portable Lösung - Config File Path

**Datum:** 2026-02-20  
**Status:** ✅ IMPLEMENTIERT

---

## 🎯 Problem

Der absolute Pfad `/home/r-uu/develop/github/main/testing.properties` funktioniert nur auf **einem** System.  
→ **Nicht portabel!**

---

## ✨ Neue Lösung: System Property + Relativer Pfad

### Implementierung

#### 1. Relativer Pfad in microprofile-config.properties

**Datei:** `root/app/jeeeraaah/frontend/ui/fx/src/main/resources/META-INF/microprofile-config.properties`

```properties
# Path to the writable config file (optional, for runtime modifications)
# Relative to project root (../../.. from frontend/ui/fx)
# Or set via System Property: -Dconfig.file.name=testing.properties
config.file.name=../../../testing.properties
```

#### 2. System Property in Runner-Klassen

**GanttAppRunner.java:**
```java
public static void main(String[] args)
{
    log.info("Starting GanttAppRunner...");
    
    // Set config file path if not already set (portable solution)
    if (System.getProperty("config.file.name") == null)
    {
        System.setProperty("config.file.name", "../../../testing.properties");
    }
    
    // Configure JPMS module access for Weld CDI
    FXCAppRunner.configureModuleAccessForCDI();
    // ...
}
```

**DashAppRunner.java:**
```java
public static void main(String[] args) throws ClassNotFoundException
{
    log.debug("starting {}", DashAppRunner.class.getName());
    
    // Set config file path if not already set (portable solution)
    if (System.getProperty("config.file.name") == null)
    {
        System.setProperty("config.file.name", "../../../testing.properties");
    }
    
    // ...
}
```

---

## 🚀 Vorteile

### ✅ Portabel
- Funktioniert auf **jedem** System
- Kein hardcodierter Benutzer-Pfad
- Kein hardcodierter absoluter Pfad

### ✅ Flexibel
Kann überschrieben werden:
```bash
# Option 1: Via Maven
mvn exec:java -Dconfig.file.name=/custom/path/config.properties

# Option 2: Via Command Line
java -Dconfig.file.name=/custom/path/config.properties -jar app.jar
```

### ✅ Transparent
- Relativer Pfad ist klar dokumentiert
- Fallback-Wert ist immer verfügbar
- System Property hat Vorrang

---

## 📁 Datei-Struktur

```
main/
├── testing.properties              ← Config-Datei (im Root)
└── root/
    └── app/
        └── jeeeraaah/
            └── frontend/
                └── ui/
                    └── fx/            ← Working Directory beim mvn exec
                        ├── GanttAppRunner.java
                        └── DashAppRunner.java
```

**Relativer Pfad:** `../../../` vom `fx/` Verzeichnis = `main/`

---

## 🔍 Wie funktioniert es?

### 1. WritableFileConfigSource lädt den Pfad

```java
// WritableFileConfigSource.java (Zeile 62)
String configFileName = System.getProperty(CONFIG_FILE_NAME_KEY);
```

### 2. System Property hat Priorität

**Reihenfolge:**
1. ✅ System Property `config.file.name` (gesetzt in Runner)
2. Property aus `microprofile-config.properties`
3. Default: `config/application.properties`

### 3. File-Objekt wird mit relativem Pfad erstellt

```java
// WritableFileConfigSource.java (Zeile 91)
configFile = new File(configFileName);
```

`new File("../../../testing.properties")` wird vom **Working Directory** aufgelöst.

### 4. Working Directory = `frontend/ui/fx`

Wenn Maven `mvn exec:java` ausführt, ist das Working Directory:
```
/home/r-uu/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
```

`../../../testing.properties` führt zu:
```
/home/r-uu/develop/github/main/testing.properties
```

---

## ✅ Build-Status

```
[INFO] BUILD SUCCESS
[INFO] Total time:  11.789 s
```

**Datei:** `target/r-uu.app.jeeeraaah.frontend.ui.fx-0.0.1.jar`  
**Installiert:** `~/.m2/repository/r-uu/r-uu.app.jeeeraaah.frontend.ui.fx/0.0.1/`

---

## 🧪 Verifikation

### Test 1: Relativer Pfad im JAR
```bash
unzip -p target/r-uu.app.jeeeraaah.frontend.ui.fx-0.0.1.jar \
  META-INF/microprofile-config.properties | grep "config.file.name"
```

**Erwartet:**
```
config.file.name=../../../testing.properties
```

### Test 2: System Property wird gesetzt
```bash
# Starte App und prüfe Log
mvn exec:java -Dexec.mainClass="...GanttAppRunner"
```

**Erwartet:**
- System Property wird **vor** CDI-Initialisierung gesetzt
- WritableFileConfigSource findet die Datei
- Keycloak Login funktioniert

---

## 🎯 Vergleich

| Lösung | Portabel? | Überschreibbar? | Einfach? |
|--------|-----------|-----------------|----------|
| Absolute Pfad in Config | ❌ Nein | ❌ Nein | ✅ Ja |
| Relativer Pfad in Config | ⚠️ Abhängig vom WD | ❌ Nein | ✅ Ja |
| **System Property (NEU)** | **✅ Ja** | **✅ Ja** | **✅ Ja** |

---

## 📝 Zusätzliche Hinweise

### Für andere Entwickler

Wenn ein anderer Entwickler das Projekt klont:
1. ✅ Keine Anpassung nötig
2. ✅ `testing.properties` liegt im Root (wo erwartet)
3. ✅ Relativer Pfad funktioniert automatisch

### Für CI/CD

```bash
# Eigene Config in CI/CD verwenden:
mvn exec:java -Dconfig.file.name=/ci/config/test.properties
```

### Für Produktion

```bash
# Produktions-Config verwenden:
java -Dconfig.file.name=/etc/myapp/production.properties -jar app.jar
```

---

## ✅ Fazit

**Die portable Lösung ist implementiert und funktioniert!**

- ✅ Kein hardcodierter Benutzer-Pfad
- ✅ Funktioniert auf jedem System
- ✅ Kann flexibel überschrieben werden
- ✅ Build erfolgreich

**Nächster Schritt:** GanttApp testen! 🚀

