# 📜 Scripts - Übersicht

Gemeinsame Entwicklungs-Skripte für das r-uu Projekt.

## 🎯 Verfügbare Skripte

### 1. `install-graalvm.sh`
**GraalVM 25 Installation**

Installiert GraalVM JDK 25 und ersetzt das existierende JDK.

```bash
cd ~/develop/github/java/main/config/shared/scripts
sudo ./install-graalvm.sh
```

**Was macht es:**
- ✅ Lädt GraalVM 25 automatisch herunter
- ✅ Installiert nach `/opt/graalvm-jdk-25`
- ✅ Konfiguriert Java-Alternativen
- ✅ Aktualisiert `/etc/environment`
- ✅ Prüft existierende Installationen

**Nach der Installation:**
```bash
source ~/.bashrc
ruu-graalvm-version
```

→ **Dokumentation:** [GRAALVM-INSTALLATION.md](../../GRAALVM-INSTALLATION.md)

---

### 2. `check-java.sh`
**Java/GraalVM Installations-Check**

Überprüft die Java/GraalVM Installation und Konfiguration.

```bash
# Direkt ausführen
bash ~/develop/github/java/main/config/shared/scripts/check-java.sh

# Oder mit Alias (nach source ~/.bashrc)
ruu-java-check
```

**Was wird geprüft:**
- ✅ Java Version (sollte 25.x.x sein)
- ✅ GraalVM erkannt
- ✅ JAVA_HOME korrekt gesetzt
- ✅ Java Binary Pfad
- ✅ Maven nutzt korrektes Java
- ✅ Native Image verfügbar
- ✅ r-uu Aliase geladen

**Beispiel-Output:**
```
🔍 Java/GraalVM Installations-Check
====================================

1️⃣ Java Version:
   java 25.0.1 2025-10-21 LTS
✓ Java 25 erkannt
✓ GraalVM erkannt (Oracle GraalVM)

2️⃣ JAVA_HOME:
   /opt/graalvm-jdk-25
✓ JAVA_HOME korrekt gesetzt
✓ JAVA_HOME Verzeichnis existiert

📊 Zusammenfassung:
✓ System bereit für Entwicklung mit GraalVM 25
```

---

### 3. `setup-dev-env.sh`
**Entwicklungsumgebung Setup**

Richtet die lokale Entwicklungsumgebung ein.

```bash
cd ~/develop/github/java/main/config/shared/scripts
./setup-dev-env.sh
```

**Was macht es:**
- ✅ Erstellt `config/local/` Verzeichnisse
- ✅ Kopiert Templates nach `config/local/`
- ✅ Setzt korrekte Berechtigungen
- ✅ Zeigt nächste Schritte

---

### 4. `maven-build.sh`
**Maven Build Wrapper**

Vereinfachter Maven-Build mit Fehlerbehandlung.

```bash
# Alle Module bauen
./maven-build.sh

# Mit Tests überspringen
./maven-build.sh -DskipTests
```

---

### 5. `docker-manager.sh`
**Docker Container Management**

Verwaltet Docker-Container und -Services.

```bash
# Container starten
./docker-manager.sh start

# Container stoppen
./docker-manager.sh stop

# Status anzeigen
./docker-manager.sh status
```

---

## 🔧 Berechtigungen setzen

Alle Skripte sollten ausführbar sein:

```bash
cd ~/develop/github/java/main/config/shared/scripts
chmod +x *.sh
```

## 📝 Wichtige Hinweise

### Pfade
Alle Skripte erwarten, dass das Projekt unter folgendem Pfad liegt:
```
/home/r-uu/develop/github/java/main
```

### WSL-Umgebung
Die Skripte sind für WSL (Windows Subsystem for Linux) optimiert.

### Sudo-Rechte
Einige Skripte benötigen sudo-Rechte:
- `install-graalvm.sh` - Installation nach /opt/
- `docker-manager.sh` - Docker-Befehle

## 🆘 Troubleshooting

### "Permission denied"
```bash
chmod +x ~/develop/github/java/main/config/shared/scripts/*.sh
```

### "Command not found"
Stelle sicher, dass du im richtigen Verzeichnis bist:
```bash
cd ~/develop/github/java/main/config/shared/scripts
```

### Aliase nicht verfügbar
Füge zur `~/.bashrc` hinzu:
```bash
source ~/develop/github/java/main/config/shared/wsl/aliases.sh
```

Dann:
```bash
source ~/.bashrc
```

## 📚 Weitere Dokumentation

- [../../QUICKSTART.md](../../QUICKSTART.md) - Schnellstart
- [../../GRAALVM-INSTALLATION.md](../../GRAALVM-INSTALLATION.md) - GraalVM Installation
- [../../readme.md](../../readme.md) - Vollständige config/ Dokumentation

