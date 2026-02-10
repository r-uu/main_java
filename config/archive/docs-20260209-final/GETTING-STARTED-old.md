# JEEERAAAH - Getting Started

**Enterprise Task Management System mit JavaFX, JAX-RS, JPA & Keycloak**

## 📋 Schnellstart

### 1. Voraussetzungen

- **WSL2/Ubuntu** (für Windows-Nutzer)
- **Docker** & Docker Compose
- **Java 25** (GraalVM empfohlen)
- **Maven 3.9+**
- **Git**

### 2. Projekt klonen

```bash
cd ~
mkdir -p develop/github
cd develop/github
git clone <repository-url> main
cd main
```

### 3. Aliase einrichten

```bash
# Aliase zur .bashrc hinzufügen
cat config/shared/wsl/aliases.sh >> ~/.bashrc

# Shell neu laden
source ~/.bashrc
```

### 4. Docker-Umgebung starten

```bash
# Komplette Docker-Umgebung aufsetzen (PostgreSQL, Keycloak, JasperReports)
ruu-docker-startup

# Status prüfen
ruu-docker-ps
ruu-docker-status
```

Das Skript:
- Startet alle Container
- Wartet bis alle Containers healthy sind
- Erstellt automatisch Keycloak Realm
- Validiert die komplette Umgebung

### 5. Projekt bauen

```bash
# Im root-Verzeichnis
cd ~/develop/github/main/root
mvn clean install
```

### 6. Backend starten (Liberty Server)

```bash
# In neuem Terminal
cd ~/develop/github/main/root/app/jeeeraaah/backend/api/ws.rs
mvn liberty:dev
```

Server läuft auf: `http://localhost:9080`

### 7. Frontend starten (JavaFX Dashboard)

**Option A: IntelliJ IDEA**
- Öffne `DashAppRunner.java`
- Run Configuration: `.run/DashAppRunner.run.xml` (im Projekt enthalten)
- Start mit Debug-Modus möglich

**Option B: Maven**
```bash
cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn javafx:run
```

**Option C: Alias**
```bash
ruu-dash-start
```

## 🔐 Standard-Credentials

### PostgreSQL

| Datenbank   | User       | Passwort   | Port | Container           |
|-------------|------------|------------|------|---------------------|
| `jeeeraaah` | `jeeeraaah`| `jeeeraaah`| 5432 | postgres-jeeeraaah  |
| `lib_test`  | `lib_test` | `lib_test` | 5434 | postgres-lib-test   |
| `keycloak`  | `keycloak` | `keycloak` | 5433 | postgres-keycloak   |

### Keycloak

- **Admin Console:** http://localhost:8080/admin
- **Admin User:** `admin` / `admin`
- **Realm:** `jeeeraaah-realm`
- **Test User:** `test` / `test`

### Backend API

- **Base URL:** http://localhost:9080
- **OpenAPI:** http://localhost:9080/openapi/ui/

## 🛠️ Nützliche Aliase

### Docker

```bash
ruu-docker-startup    # Komplette Umgebung starten (mit Keycloak Realm Setup)
ruu-docker-restart    # Container neustarten
ruu-docker-reset      # Alles löschen und neu aufsetzen
ruu-docker-ps         # Container-Status
ruu-docker-status     # Detaillierter Status-Check
ruu-docker-logs       # Alle Logs anzeigen
```

### Keycloak

```bash
ruu-keycloak-start    # Keycloak starten
ruu-keycloak-restart  # Keycloak neu starten
ruu-keycloak-setup    # Realm manuell erstellen
ruu-keycloak-logs     # Logs anzeigen
```

### PostgreSQL

```bash
ruu-postgres-start    # PostgreSQL starten
ruu-postgres-shell    # PostgreSQL Shell öffnen
ruu-postgres-logs     # Logs anzeigen
```

### Build & Testing

```bash
ruu-build             # Komplettes Projekt bauen
ruu-build-root        # Nur root bauen
ruu-test              # Tests ausführen
ruu-clean             # Maven clean
```

### Entwicklung

```bash
ruu-dash-start        # Dashboard starten
ruu-liberty-start     # Liberty Server starten
ruu-help              # Alle Aliase anzeigen
```

## 📁 Projekt-Struktur

```
main/
├── root/                          # Maven Multi-Module Projekt
│   ├── app/jeeeraaah/            # Hauptanwendung
│   │   ├── backend/api/ws.rs/    # JAX-RS Backend (Liberty)
│   │   ├── frontend/ui/fx/       # JavaFX Frontend
│   │   └── common/               # Shared Code
│   ├── lib/                      # Wiederverwendbare Bibliotheken
│   │   ├── fx/comp/              # JavaFX Components
│   │   ├── jpa/                  # JPA Utilities
│   │   ├── keycloak.admin/       # Keycloak Admin Client
│   │   ├── mp.config/            # MicroProfile Config
│   │   └── ...
│   └── pom.xml                   # Root POM
├── config/                       # Konfiguration & Dokumentation
│   ├── shared/
│   │   ├── docker/               # Docker Compose Setup
│   │   │   ├── .env.template     # Template für Credentials
│   │   │   ├── docker-compose.yml
│   │   │   └── initdb/           # PostgreSQL Init-Skripte
│   │   ├── scripts/              # Helper-Skripte
│   │   └── wsl/aliases.sh        # Bash Aliase
│   └── *.md                      # Dokumentation
├── bom/                          # Bill of Materials (Dependency Management)
└── testing.properties            # Global Test Configuration
```

## 🔧 Troubleshooting

### Docker Container starten nicht

```bash
# Status prüfen
docker ps -a

# Logs prüfen
docker compose logs

# Komplett neu aufsetzen
ruu-docker-reset
```

### Keycloak Realm fehlt

```bash
# Realm manuell erstellen
ruu-keycloak-setup

# Oder kompletten Reset
ruu-docker-reset
```

### Datenbank-Verbindungsprobleme

```bash
# PostgreSQL Container prüfen
docker logs postgres-jeeeraaah
docker logs postgres-lib-test

# Shell öffnen und manuell testen
docker exec -it postgres-jeeeraaah psql -U jeeeraaah -d jeeeraaah
```

### Liberty Server startet nicht

```bash
# Prüfe ob Port 9080 belegt ist
netstat -tulpn | grep 9080

# Stoppe alten Prozess falls nötig
mvn liberty:stop

# Neu starten
mvn liberty:dev
```

### Frontend startet nicht (IntelliJ)

1. Prüfe dass `.run/DashAppRunner.run.xml` existiert
2. Build das Projekt: `mvn clean install`
3. Refreshe IntelliJ: `File → Invalidate Caches → Restart`
4. Stelle sicher dass `JAVA_HOME` auf Java 25 zeigt

### Compilation Errors

```bash
# Kompletter Clean Build
cd ~/develop/github/main/root
mvn clean install -DskipTests

# Falls JPMS Probleme:
# - Prüfe module-info.java Dateien
# - Siehe: JPMS-INTELLIJ-QUICKSTART.md
```

## 📚 Weitere Dokumentation

- **[STARTUP-QUICK-GUIDE.md](STARTUP-QUICK-GUIDE.md)** - Detaillierte Startup-Anleitung
- **[JPMS-INTELLIJ-QUICKSTART.md](JPMS-INTELLIJ-QUICKSTART.md)** - JPMS & IntelliJ Setup
- **[config/README.md](config/README.md)** - Konfiguration im Detail
- **[config/DOCKER-ENV-SETUP.md](config/DOCKER-ENV-SETUP.md)** - Docker Environment Setup
- **[config/SINGLE-POINT-OF-TRUTH.md](config/SINGLE-POINT-OF-TRUTH.md)** - Configuration Strategy

## 🎯 Nächste Schritte

1. ✅ Getting Started durchgearbeitet
2. 📖 Lies [PROJECT-STATUS.md](PROJECT-STATUS.md) für Projekt-Übersicht
3. 🔍 Schau dir die API-Dokumentation an: http://localhost:9080/openapi/ui/
4. 🧪 Führe Tests aus: `ruu-test`
5. 💻 Starte Entwicklung!

## 🆘 Support

- **Dokumentation:** `~/develop/github/main/config/`
- **Alle Aliase anzeigen:** `ruu-help`
- **Docker Status:** `ruu-docker-status`
