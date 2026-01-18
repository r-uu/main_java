# IntelliJ IDEA WSL Setup für GraalVM 25

## Problem
IntelliJ IDEA läuft unter Windows und kann die WSL-Umgebungsvariablen (insbesondere `JAVA_HOME`) nicht direkt lesen.

**Fehlermeldung:**
```
The JAVA_HOME environment variable is not defined correctly,
this environment variable is needed to run this program.
```

## ✅ Lösung

### Schritt 1: IntelliJ JDK Konfiguration

1. **Öffne IntelliJ IDEA**
2. **File → Project Structure** (Strg+Alt+Shift+S)
3. **Platform Settings → SDKs**
4. **Klicke auf "+"** → Add JDK
5. **Navigiere zu:** `\\wsl.localhost\Ubuntu\opt\graalvm-jdk-25`
6. **Wähle dieses Verzeichnis** als JDK Home
7. **Klicke OK**

### Schritt 2: Project JDK setzen

1. **Noch in Project Structure**
2. **Project Settings → Project**
3. **Project SDK:** Wähle das gerade hinzugefügte GraalVM 25 JDK
4. **Project language level:** 25 (Preview)
5. **Klicke Apply und OK**

### Schritt 3: Maven JDK Konfiguration

1. **File → Settings** (Strg+Alt+S)
2. **Build, Execution, Deployment → Build Tools → Maven**
3. **Maven home path:** `\\wsl.localhost\Ubuntu\opt\maven\maven`
4. **User settings file:** `\\wsl.localhost\Ubuntu\home\r-uu\.m2\settings.xml` (falls vorhanden)
5. **Local repository:** `\\wsl.localhost\Ubuntu\home\r-uu\.m2\repository`

### Schritt 4: Maven Runner Konfiguration

1. **Noch in Settings → Maven → Runner**
2. **JRE:** Wähle das GraalVM 25 JDK (sollte jetzt verfügbar sein)
3. **VM Options:** 
   
   **Leer lassen** oder optional:
   ```
   --enable-native-access=org.fusesource.jansi
   ```
   
   **⚠️ NICHT verwenden:** `--enable-native-access=ALL-UNNAMED`
   
   **Grund:** Widerspricht der strikten JPMS-Strategie des Projekts:
   - Versteckt JPMS-Warnings (Projekt-Philosophie: Warnings sichtbar machen)
   - Schwächt JPMS-Sicherheitsmechanismen
   - Die verbleibenden Warnings sind Maven-intern und unkritisch
   
   Siehe: `config/JPMS-NATIVE-ACCESS-ANALYSE.md`

4. **Klicke Apply und OK**

### Schritt 5: IntelliJ neu starten

1. **File → Invalidate Caches...**
2. **Invalidate and Restart**

## 🧪 Testen

Nach dem Neustart:

1. **Öffne das Maven-Tool-Fenster** (rechts in IntelliJ)
2. **Root Projekt** → Lifecycle → **clean**
3. **Root Projekt** → Lifecycle → **install**

**Erwartete Ausgabe:**
```
[INFO] BUILD SUCCESS
```

## 📋 Verification Checklist

- [ ] GraalVM 25 JDK in IntelliJ SDKs vorhanden
- [ ] Project SDK ist GraalVM 25
- [ ] Maven home zeigt auf WSL Maven Installation
- [ ] Maven Runner nutzt GraalVM 25 JRE
- [ ] `mvn clean install` läuft erfolgreich durch

## 🐚 Alternative: Terminal in IntelliJ

Falls die obige Konfiguration nicht funktioniert, kannst du das **integrierte WSL-Terminal** in IntelliJ verwenden:

1. **View → Tool Windows → Terminal**
2. **Terminal-Settings** (Zahnrad-Symbol)
3. **Shell path:** `wsl.exe`
4. **Im Terminal ausführen:**
   ```bash
   cd ~/develop/github/main/root
   mvn clean install
   ```

## 🔍 Debugging

### JAVA_HOME in WSL prüfen
```bash
echo $JAVA_HOME
# Sollte ausgeben: /opt/graalvm-jdk-25

java -version
# Sollte ausgeben: java version "25.0.1"... Oracle GraalVM

mvn --version
# Sollte ausgeben: Java version: 25.0.1... /opt/graalvm-jdk-25
```

### IntelliJ Log prüfen
**Help → Show Log in Explorer**

Suche nach Fehlern bezüglich:
- `JAVA_HOME`
- Maven
- JDK

## 📚 Weitere Informationen

- [GRAALVM-INSTALLATION.md](GRAALVM-INSTALLATION.md) - GraalVM Installation in WSL
- [QUICKSTART.md](QUICKSTART.md) - Projekt Schnellstart
- [config/shared/scripts/readme.md](shared/scripts/readme.md) - Verfügbare Skripte

## 💡 Hinweise

- IntelliJ speichert JDK-Pfade in `.idea/misc.xml`
- Maven-Einstellungen werden in `.idea/workspace.xml` gespeichert
- Diese Dateien sind im Git-Repository ignoriert (`.gitignore`)
- Jeder Entwickler muss die Konfiguration einmal durchführen

---

**Erstellt:** 2026-01-11  
**Zuletzt getestet mit:** IntelliJ IDEA 2025.3.1.1, GraalVM 25.0.1, Maven 3.9.9

