# Docker Container Startup - Anleitung

## 📦 Container-Übersicht

Das Projekt verwendet folgende Docker Container:

1. **postgres** (Port 5432) - PostgreSQL Datenbank
   - Schema: `jeeeraaah` 
   - Schema: `lib_test`
   - Schema: `keycloak`

2. **keycloak** (Port 8080) - Identity & Access Management
   - Admin Console: http://localhost:8080/admin

3. **jasperreports** (Port 8090) - Report Generation Service
   - Health Endpoint: http://localhost:8090/health

## 🚀 Schnellstart

### Alle Container starten

```bash
# Empfohlen: Kompletter Startup mit Wartezeit und Setup
ruu-docker-startup

# Oder einzeln:
ruu-docker-up
```

### Einzelne Container starten

```bash
# Nur Keycloak starten
ruu-keycloak-start

# Nur JasperReports starten
ruu-jasper-start

# Nur PostgreSQL starten (falls gestoppt)
ruu-postgres-start
```

### Beide (Keycloak + JasperReports) gleichzeitig starten

```bash
# Option 1: Mit Skript
cd ~/develop/github/main/config/shared/docker
./start-keycloak-jasper.sh

# Option 2: Mit docker compose
cd ~/develop/github/main/config/shared/docker
docker compose up -d keycloak jasperreports

# Option 3: Mit Aliasen
ruu-keycloak-start && ruu-jasper-start
```

## 🔍 Status prüfen

```bash
# Alle Container anzeigen
ruu-docker-ps

# Oder detailliert:
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Oder mit docker compose:
cd ~/develop/github/main/config/shared/docker
docker compose ps
```

### Erwarteter Output (alle Container healthy)

```
NAMES           STATUS                  PORTS
keycloak        Up 2 minutes (healthy)  0.0.0.0:8080->8080/tcp
jasperreports   Up 2 minutes (healthy)  0.0.0.0:8090->8090/tcp
postgres        Up 5 minutes (healthy)  0.0.0.0:5432->5432/tcp
```

## 🏥 Health Checks

### Manuell prüfen

```bash
# PostgreSQL
docker exec postgres pg_isready -U postgres

# Keycloak
curl -f http://localhost:8080/health/ready || echo "Not ready yet"

# JasperReports
curl -f http://localhost:8090/health || echo "Not ready yet"
```

### Mit Skript

```bash
ruu-docker-status
```

## 🔄 Container neu starten

```bash
# Alle Container
ruu-docker-restart

# Einzeln
ruu-keycloak-restart
ruu-jasper-restart
ruu-postgres-restart
```

## 📋 Logs anzeigen

```bash
# Alle Container (Follow-Modus)
ruu-docker-logs

# Einzeln
ruu-keycloak-logs
ruu-jasper-logs
ruu-postgres-logs
```

## 🛑 Container stoppen

```bash
# Alle Container
ruu-docker-down

# Einzeln
ruu-keycloak-stop
ruu-jasper-stop
ruu-postgres-stop
```

## 🗑️ Container & Volumes löschen

```bash
# Nur Container löschen
docker compose down

# Container + Volumes löschen (ACHTUNG: Datenverlust!)
docker compose down -v

# Komplett-Reset mit Neuaufbau
ruu-docker-reset
```

## ⚠️ Troubleshooting

### Container startet nicht

```bash
# Logs prüfen
docker logs <container-name>

# Beispiel:
docker logs keycloak
docker logs jasperreports
```

### Docker Daemon läuft nicht

```bash
# Docker starten
sudo service docker start

# Status prüfen
sudo service docker status
```

### Container ist "unhealthy"

```bash
# 1. Logs prüfen
docker logs <container-name>

# 2. Container neu starten
docker compose restart <container-name>

# 3. Falls das nicht hilft: Komplett neu erstellen
docker compose down
docker compose up -d
```

### Keycloak Realm fehlt

```bash
# Realm automatisch erstellen
ruu-keycloak-setup

# Oder manuell:
cd ~/develop/github/main/root/lib/keycloak_admin
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```

## 📝 Wichtige Hinweise

1. **Credentials**: Alle Passwörter sind in `testing.properties` (Projekt-Root)
   - Diese Datei ist **nicht** in Git versioniert
   - Template: `testing.properties.template`

2. **Startup-Reihenfolge**: 
   - Postgres muss zuerst starten (dependency in docker-compose.yml)
   - Keycloak wartet auf Postgres (depends_on)
   - JasperReports startet unabhängig

3. **Health Checks**: 
   - Warte auf "healthy" Status bevor du die Services verwendest
   - Prüfen mit: `docker ps` oder `ruu-docker-ps`

4. **Ports**:
   - 5432: PostgreSQL
   - 8080: Keycloak
   - 8090: JasperReports
   - 9080: Liberty Server (Backend)

## 🎯 Siehe auch

- `config/shared/docker/POSTGRES-OVERVIEW.md` - PostgreSQL Details
- `QUICKSTART.md` - Projekt Schnellstart
- `CREDENTIALS-CLEANUP-SUMMARY.md` - Credentials Management
