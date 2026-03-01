# Docker Environment - JEEERAaH Project

## 🎯 Übersicht

Dieses Verzeichnis enthält die Docker Compose Konfiguration für die Entwicklungsumgebung des JEEERAaH-Projekts.

**Container:**
- **postgres** - PostgreSQL 16 mit drei Datenbanken (jeeeraaah, lib_test, keycloak)
- **keycloak** - Identity & Access Management
- **jasperreports** - Report Generation Service

## 🚀 Quick Start

```bash
# Komplette Umgebung neu aufsetzen
cd ~/develop/github/main/config/shared/docker
./full-reset.sh

# Oder manuell:
docker compose down -v           # Alles stoppen und Volumes löschen
docker compose up -d             # Alle Services starten
docker compose ps                # Status prüfen
docker compose logs -f           # Logs verfolgen
```

## 📦 Container Details

### PostgreSQL (Port 5432)

**Ein Container, drei Datenbanken:**

| Database | User | Password | Verwendung |
|----------|------|----------|------------|
| jeeeraaah | jeeeraaah | jeeeraaah | Hauptanwendung |
| lib_test | lib_test | lib_test | Integrationstests |
| keycloak | keycloak | keycloak | Keycloak Persistence |

**Superuser:** postgres:postgres (nur für Container-Management)

**Init-Scripts:**
- `initdb/01-init-jeeeraaah.sql` - Erstellt jeeeraaah DB + User
- `initdb/02-init-lib_test.sql` - Erstellt lib_test DB + User
- `initdb/03-init-keycloak.sql` - Erstellt keycloak DB + User

**Verbindung testen:**
```bash
# Als Superuser
docker exec -it postgres psql -U postgres

# Als jeeeraaah User
docker exec -it postgres psql -U jeeeraaah -d jeeeraaah

# Als lib_test User
docker exec -it postgres psql -U lib_test -d lib_test
```

### Keycloak (Port 8080)

**Admin Console:** http://localhost:8080/admin
- **Username:** admin
- **Password:** admin

**Realm:** jeeeraaah-realm
- **Test User:** jeeeraaah:jeeeraaah

**Healthcheck:**
- Script: `healthcheck/keycloak-healthcheck.sh`
- Prüft: `/health/ready` Endpoint
- Start Period: 60s (Keycloak braucht Zeit zum Starten)

**Realm Setup:**
```bash
cd ~/develop/github/main/root/lib/keycloak_admin
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```

### JasperReports (Port 8090)

**Service URL:** http://localhost:8090
- **Health Endpoint:** http://localhost:8090/health

## 🔧 Konfiguration

### Environment Variables (.env)

Alle Credentials und Konfigurationen sind in `.env` definiert:

```dotenv
# PostgreSQL
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_JEEERAAAH_DB=jeeeraaah
POSTGRES_JEEERAAAH_USER=jeeeraaah
POSTGRES_JEEERAAAH_PASSWORD=jeeeraaah
# ... (siehe .env für vollständige Liste)

# Keycloak
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PASSWORD=admin
KEYCLOAK_TEST_USERNAME=jeeeraaah
KEYCLOAK_TEST_PASSWORD=jeeeraaah
```

**WICHTIG:** Niemals `.env` in Git committen! Verwende `.env.template` als Vorlage.

### Volumes

**Persistente Daten:**
```yaml
volumes:
  postgres-data              # PostgreSQL Datenbanken
  postgres-backups           # PostgreSQL Backups
  keycloak-data              # Keycloak Konfiguration/Realms
  jasperreports-output       # JasperReports Output
```

**Volumes löschen:**
```bash
docker compose down -v  # ACHTUNG: Löscht alle Daten!
```

## 🏥 Health Checks

### Manuell prüfen:

```bash
# Alle Container
docker compose ps

# PostgreSQL
docker exec postgres pg_isready -U postgres

# Keycloak
curl http://localhost:8080/health/ready

# JasperReports
curl http://localhost:8090/health
```

### Automatisch (Java):

Das Projekt enthält `lib.docker.health` mit automatischen Health Checks beim Application-Start.

## 🛠️ Häufige Befehle

```bash
# Status anzeigen
docker compose ps
docker compose logs -f                    # Alle Logs
docker compose logs -f postgres           # Nur PostgreSQL
docker compose logs -f keycloak           # Nur Keycloak

# Einzelne Services steuern
docker compose up -d postgres             # Nur PostgreSQL starten
docker compose restart keycloak           # Keycloak neu starten
docker compose stop jasperreports         # JasperReports stoppen

# Container betreten
docker exec -it postgres bash             # PostgreSQL Container
docker exec -it keycloak bash             # Keycloak Container

# Aufräumen
docker compose down                       # Container stoppen
docker compose down -v                    # Container + Volumes löschen
docker system prune -a --volumes          # ALLES löschen (VORSICHT!)
```

## 🐛 Troubleshooting

### PostgreSQL startet nicht

**Problem:** Container startet, wird aber nicht healthy

**Lösung:**
```bash
docker logs postgres
docker exec postgres pg_isready -U postgres
```

**Häufige Ursachen:**
- Alter Container läuft noch: `docker ps -a | grep postgres`
- Port 5432 belegt: `lsof -i :5432`
- Init-Scripts fehlerhaft: Prüfe `initdb/*.sql`

### Keycloak wird nicht healthy

**Problem:** Container startet, aber Health Check schlägt fehl

**Lösung:**
```bash
docker logs keycloak --tail 100
curl http://localhost:8080/health/ready
```

**Häufige Ursachen:**
- PostgreSQL nicht erreichbar (Keycloak braucht DB)
- Keycloak braucht 60-90s zum Starten (normal!)
- Falsche DB Credentials in `.env`

### Realm fehlt

**Problem:** Keycloak läuft, aber `jeeeraaah-realm` existiert nicht

**Lösung:**
```bash
cd ~/develop/github/main/root/lib/keycloak_admin
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```

### Port-Konflikte

**Problem:** "Port already in use"

**Lösung:**
```bash
# Welcher Prozess verwendet den Port?
lsof -i :5432   # PostgreSQL
lsof -i :8080   # Keycloak
lsof -i :8090   # JasperReports

# Prozess beenden
kill <PID>
```

### Alles kaputt - Komplett-Reset

```bash
cd ~/develop/github/main/config/shared/docker

# Variante 1: Reset-Script (empfohlen)
./full-reset.sh

# Variante 2: Manuell
docker compose down -v
docker volume prune -f
docker compose up -d
cd ~/develop/github/main/root/lib/keycloak_admin
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```

## 📚 Weitere Dokumentation

- **Keycloak Realm Setup:** `~/develop/github/main/root/lib/keycloak_admin/README.md`
- **Docker Health Checks:** `~/develop/github/main/root/lib/docker_health/README.md`
- **Project Quickstart:** `~/develop/github/main/QUICKSTART.md`
- **Konsolidierung:** `~/develop/github/main/KONSOLIDIERUNG-2026-01-30.md`

## 🔐 Sicherheitshinweise

**Entwicklungsumgebung:**
- Alle Passwörter sind **NICHT sicher** (z.B. admin:admin)
- HTTP statt HTTPS (Keycloak)
- Keine Firewall-Regeln

**Für Produktion:**
- Starke Passwörter verwenden
- HTTPS aktivieren (Keycloak KC_HTTPS_*)
- Firewall konfigurieren
- Secrets extern verwalten (z.B. Vault)
- `.env` niemals committen

## 📝 Änderungshistorie

- **2026-01-30:** Konsolidierung - Ein PostgreSQL Container mit drei Datenbanken
- **2026-01-30:** Keycloak Container-Name standardisiert (keycloak statt keycloak-jeeeraaah)
- **2026-01-30:** Init-Scripts vereinfacht (3 SQL-Dateien statt Verzeichnisstruktur)
- **2026-01-30:** Full-Reset Script hinzugefügt
