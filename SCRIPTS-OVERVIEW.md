# Projekt Skripte und Aliase - Übersicht

**Letzte Aktualisierung:** 2026-01-30

---

## 📂 Verzeichnisstruktur

```
config/shared/
├── docker/              → Docker-spezifische Skripte
│   ├── startup-and-setup.sh      ✅ Haupt-Startup-Skript
│   ├── check-status.sh           ✅ Status aller Container
│   ├── start-all-and-wait.sh     ✅ Start + Wait bis healthy
│   ├── complete-setup.sh         ✅ Komplett-Setup mit Realm
│   ├── reset-docker-environment.sh ✅ Kompletter Reset
│   └── healthcheck/              → Health Check Skripte für Container
├── scripts/            → Allgemeine Build/Setup-Skripte
│   ├── build-all.sh              ✅ Gesamtprojekt bauen
│   ├── test-docker-autostart.sh  ✅ Docker-Tests
│   └── test-multi-db.sh          ✅ Multi-DB-Tests
└── wsl/               → WSL-spezifische Konfiguration
    ├── aliases.sh                ✅ Alle Projekt-Aliase
    └── wsl-startup.sh            ✅ WSL-Autostart-Konfiguration
```

---

## 🚀 Hauptskripte (Aktiv verwendet)

### Docker Environment Management

#### `startup-and-setup.sh` ⭐ EMPFOHLEN
**Alias:** `ruu-docker-startup`

**Beschreibung:** Kompletter automatischer Startup aller Docker Services
- Startet alle Container (PostgreSQL, Keycloak, JasperReports)
- Wartet bis alle Container `healthy` sind
- Erstellt automatisch Keycloak Realm (falls nicht vorhanden)
- Führt Health Checks durch

**Verwendung:**
```bash
ruu-docker-startup
```

#### `check-status.sh`
**Alias:** `ruu-docker-status`

**Beschreibung:** Zeigt detaillierten Status aller Container
- Container-Status (running/stopped/healthy)
- PostgreSQL Datenbank-Erreichbarkeit
- Keycloak Realm Status

**Verwendung:**
```bash
ruu-docker-status
```

#### `start-all-and-wait.sh`
**Alias:** `ruu-docker-start-all`

**Beschreibung:** Startet Container und wartet bis alle `healthy` sind

**Verwendung:**
```bash
ruu-docker-start-all
```

### Build & Test

#### `build-all.sh`
**Alias:** `ruu-build` / `ruu-build-all`

**Beschreibung:** Baut das gesamte Projekt in korrekter Reihenfolge
1. BOM (Bill of Materials)
2. Root Projekt (alle Libs + Apps)

**Verwendung:**
```bash
ruu-build               # Standard-Build
ruu-build --skip-tests  # Ohne Tests
ruu-build --clean       # Mit mvn clean
```

#### `test-docker-autostart.sh`
**Alias:** `ruu-test`

**Beschreibung:** Testet Docker Auto-Start Funktionalität

**Verwendung:**
```bash
ruu-test               # Nur Docker-Tests
ruu-test --with-build  # Mit Maven Build
```

#### `test-multi-db.sh`
**Alias:** `ruu-test-multidb`

**Beschreibung:** Testet alle PostgreSQL Datenbanken

**Verwendung:**
```bash
ruu-test-multidb
```

---

## 🔧 Aliase (Wichtigste)

### Navigation
```bash
ruu-home      # → cd ~/develop/github/main
ruu-root      # → cd ~/develop/github/main/root
ruu-docker    # → cd ~/develop/github/main/config/shared/docker
ruu-config    # → cd ~/develop/github/main/config
```

### Docker - Schnellstart
```bash
ruu-docker-startup    # ⭐ Komplett-Start (empfohlen!)
ruu-docker-status     # Status aller Container
ruu-docker-ps         # Container-Übersicht
ruu-docker-restart    # Alle Container neu starten
ruu-docker-reset      # Kompletter Reset (Container + Volumes)
```

### Docker - Einzelne Services
```bash
# PostgreSQL
ruu-postgres-start
ruu-postgres-shell    # SQL Shell für jeeeraaah DB

# Keycloak
ruu-keycloak-start
ruu-keycloak-setup    # Realm erstellen
ruu-keycloak-admin    # Admin Console URL anzeigen

# JasperReports
ruu-jasper-start
ruu-jasper-test
```

### Maven Build
```bash
ruu-build             # ⭐ Gesamtprojekt bauen (empfohlen!)
ruu-install           # cd root && mvn clean install
ruu-install-fast      # Mit -DskipTests
ruu-test              # mvn test
```

### Hilfe
```bash
ruu-help              # Alle Aliase anzeigen
ruu-docs              # Dokumentation anzeigen
ruu-aliases-reload    # Aliase neu laden
```

---

## 📋 Deprecated Skripte (Nicht mehr verwenden!)

Diese Skripte existieren noch, sollten aber nicht mehr verwendet werden:

| Skript | Ersetzt durch | Grund |
|--------|---------------|-------|
| `startup-complete.sh` | `startup-and-setup.sh` | Modernere Health Checks |
| `clean-environment.sh` | `reset-docker-environment.sh` | Konsolidierung |
| `complete-reset.sh` | `reset-docker-environment.sh` | Umbenennung |

**Hinweis:** Diese Skripte werden in Zukunft entfernt!

---

## 🔄 Typische Workflows

### Täglicher Start (Cold Start nach Rechner-Neustart)

```bash
# 1. Terminal öffnen (Aliase werden automatisch geladen via .bashrc)

# 2. Docker Environment starten
ruu-docker-startup
# → Wartet ~2-3 Minuten bis alle Container healthy

# 3. Backend starten
cd ~/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs
mvn liberty:dev

# 4. Frontend starten (in IntelliJ)
# Run Configuration: DashAppRunner (JPMS)
```

### Nur Docker neu starten (Backend läuft schon)

```bash
ruu-docker-restart
```

### Nach Code-Änderungen: Neu bauen

```bash
ruu-build --skip-tests  # Schneller Build ohne Tests
```

### Bei Docker-Problemen: Kompletter Reset

```bash
ruu-docker-reset        # Löscht alle Container + Volumes
ruu-docker-startup      # Erstellt alles neu
```

---

## 🛠️ Erweiterte Nutzung

### Custom Build-Optionen

```bash
# Nur BOM
ruu-bom-install

# Nur Root (ohne BOM)
ruu-root-install

# Einzelnes Modul
cd ~/develop/github/main/root/lib/fx/comp
mvn clean install
```

### Logs live verfolgen

```bash
# Alle Container
ruu-docker-logs

# Einzelner Container
ruu-postgres-logs
ruu-keycloak-logs
ruu-jasper-logs
```

### PostgreSQL Datenbank-Reparatur

```bash
# lib_test Datenbank sicherstellen (für Tests)
ruu-postgres-ensure-lib-test

# lib_test komplett neu erstellen
ruu-postgres-reset-lib-test
```

---

## 📚 Weiterführende Dokumentation

| Datei | Beschreibung |
|-------|-------------|
| [STARTUP-QUICK-GUIDE.md](STARTUP-QUICK-GUIDE.md) | Schnellstart in 3 Minuten |
| [DOCUMENTATION-INDEX.md](DOCUMENTATION-INDEX.md) | Alle Dokumentationen |
| [config/TROUBLESHOOTING.md](config/TROUBLESHOOTING.md) | Problemlösungen |
| [config/shared/docker/README.md](config/shared/docker/README.md) | Docker Details |
| [config/shared/docker/initdb/README.md](config/shared/docker/initdb/README.md) | PostgreSQL Init-Skripte |

---

## ✅ Best Practices

1. ✅ **Nutze Aliase** statt direkte Skript-Aufrufe
2. ✅ **`ruu-docker-startup`** beim täglichen Start verwenden
3. ✅ **`ruu-build`** für komplette Builds
4. ✅ **`ruu-help`** zeigt alle verfügbaren Aliase
5. ❌ **Keine deprecated Skripte** verwenden

---

**Fragen?** Siehe [TROUBLESHOOTING.md](config/TROUBLESHOOTING.md)
