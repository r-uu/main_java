# JEEERAAAH Projekt - Quick Start Guide

> Letzte Aktualisierung: 2026-01-30

## 📋 Übersicht

Dieses Projekt ist eine Jakarta EE 10 Enterprise-Anwendung mit:
- **Backend**: OpenLiberty 25.x mit JAX-RS, JPA (Hibernate), CDI
- **Frontend**: JavaFX 24 mit JPMS-Modulen
- **Security**: Keycloak für Authentication/Authorization
- **Database**: PostgreSQL 16
- **Reports**: JasperReports Server

## 🚀 Schnellstart

### 1. Voraussetzungen

```bash
# Java Version prüfen
java -version  # Sollte GraalVM 25 anzeigen

# Docker prüfen
docker --version
docker compose version
```

### 2. Projekt-Setup

```bash
# Aliase laden (automatisch in ~/.bashrc)
source ~/develop/github/main/config/shared/wsl/aliases.sh

# Projekt bauen
ruu-build

# Docker-Umgebung starten
ruu-docker-up

# Keycloak Realm initialisieren (nur beim ersten Mal)
ruu-keycloak-setup
```

### 3. Backend starten

```bash
# Liberty Dev Mode (Hot Reload)
ruu-liberty-dev

# Oder manuell:
cd ~/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs
mvn liberty:dev
```

**Endpoints:**
- API: http://localhost:9080/jeeeraaah
- OpenAPI: http://localhost:9080/openapi/ui
- Health: http://localhost:9080/health

### 4. Frontend starten

In IntelliJ IDEA:
- Run Configuration: `DashAppRunner (JPMS)`
- Oder aus Terminal: `mvn exec:exec -f root/app/jeeeraaah/frontend/ui/fx/pom.xml`

## 🐳 Docker Services

| Service | Container | Port | Credentials |
|---------|-----------|------|-------------|
| PostgreSQL (App) | `postgres-jeeeraaah` | 5432 | `jeeeraaah:jeeeraaah` |
| PostgreSQL (Tests) | `postgres-lib-test` | 5434 | `lib_test:lib_test` |
| PostgreSQL (Keycloak) | `postgres-keycloak` | 5433 | `keycloak:keycloak` |
| Keycloak | `keycloak` | 8080 | `admin:admin` |
| JasperReports | `jasperreports` | 8090 | - |

**Aliases:**
```bash
ruu-docker-up          # Alle Container starten
ruu-docker-down        # Alle Container stoppen
ruu-docker-ps          # Container-Status anzeigen
ruu-docker-logs        # Logs anzeigen
ruu-docker-reset       # Kompletter Reset (VORSICHT!)
```

## 🔑 Keycloak

**Admin Console:** http://localhost:8080/admin
- Username: `admin`
- Password: `admin`

**Test-User:**
- Username: `jeeeraaah`
- Password: `jeeeraaah`

**Setup:**
```bash
# Realm erstellen (falls noch nicht vorhanden)
ruu-keycloak-setup

# Status prüfen
ruu-keycloak-check
```

## 📊 Datenbanken

Alle Credentials sind in `config/shared/docker/.env` definiert.

**Verbindung testen:**
```bash
# JEEERAaH DB
psql -h localhost -p 5432 -U jeeeraaah -d jeeeraaah

# Test DB
psql -h localhost -p 5434 -U lib_test -d lib_test

# Keycloak DB
psql -h localhost -p 5433 -U keycloak -d keycloak
```

## 🔧 Konfiguration

### MicroProfile Config

Zentrale Konfiguration in: `~/develop/github/main/testing.properties`

**Wichtige Properties:**
```properties
# Database (JEEERAaH)
db.jeeeraaah.host=localhost
db.jeeeraaah.port=5432
db.jeeeraaah.name=jeeeraaah
db.jeeeraaah.username=<siehe testing.properties>
db.jeeeraaah.password=<siehe testing.properties>

# Keycloak
keycloak.server.url=http://localhost:8080
keycloak.realm=jeeeraaah-realm
keycloak.client.id=jeeeraaah-frontend
keycloak.test.user=<siehe testing.properties>
keycloak.test.password=<siehe testing.properties>

# Backend Service
service.backend.url=http://localhost:9080/jeeeraaah
```

**Hinweis:** Die tatsächlichen Credentials befinden sich in `testing.properties` im Projekt-Root 
(nicht in Git versioniert). Siehe `testing.properties.template` für die Struktur.

### Testing Mode

Wenn `testing=true` in `testing.properties`:
- Automatischer Login mit Test-User
- Keine manuelle Authentifizierung erforderlich

## 🏗️ Projekt-Struktur

```
main/
├── bom/                    # Bill of Materials (Dependency Management)
├── root/
│   ├── app/
│   │   └── jeeeraaah/
│   │       ├── backend/    # Liberty Backend (JAX-RS, JPA)
│   │       └── frontend/   # JavaFX Frontend (JPMS)
│   ├── lib/                # Wiederverwendbare Bibliotheken
│   └── sandbox/            # Experimente & Prototypen
└── config/
    ├── shared/             # Gemeinsame Konfiguration (versioniert)
    │   ├── docker/         # Docker Compose & Init-Scripts
    │   ├── scripts/        # Build- & Setup-Scripte
    │   └── wsl/            # Bash-Aliase
    └── local/              # Lokale Konfiguration (nicht versioniert)
```

## 🧪 Testing

```bash
# Alle Tests
ruu-test

# Nur Unit Tests
ruu-test-unit

# Nur Integration Tests  
ruu-test-integration

# Schnell (Tests überspringen)
ruu-install-fast
```

## 🛠️ Häufige Probleme

### "Keycloak container is unhealthy"

```bash
ruu-docker-reset
ruu-keycloak-setup
```

### "Database does not exist"

```bash
ruu-docker-down
ruu-docker-up
```

### "Module not found" beim Frontend-Start

IntelliJ Run Configuration prüfen:
- VM Options sollten `--add-modules` Parameter enthalten
- Module Path korrekt gesetzt

### Liberty startet nicht (Port belegt)

```bash
# Laufende Prozesse prüfen
lsof -i :9080

# Liberty stoppen
ruu-liberty-stop
```

## 📚 Weitere Dokumentation

- **Backend API**: `root/app/jeeeraaah/backend/api/ws_rs/README.md`
- **Docker Setup**: `config/shared/docker/README.md`
- **Keycloak**: `root/lib/keycloak_admin/README.md`
- **Health Checks**: `root/lib/docker_health/README.md`

## 🎯 Nächste Schritte

1. **Entwicklung**:
   - Backend: `ruu-liberty-dev`
   - Frontend: IntelliJ Run Configuration `DashAppRunner (JPMS)`

2. **Testen**: Backend-API unter http://localhost:9080/openapi/ui

3. **Debuggen**: 
   - Backend: Liberty Debug Port 7777
   - Frontend: IntelliJ Debug Mode

---

**Hilfe anzeigen:**
```bash
ruu-help        # Zeigt alle verfügbaren Aliase
ruu-docs        # Listet Dokumentations-Dateien
```
