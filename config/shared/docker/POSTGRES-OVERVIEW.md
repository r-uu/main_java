# PostgreSQL Container Übersicht

## Zusammenfassung

Das Projekt nutzt **2 separate PostgreSQL Container** mit **3 Datenbanken**:

| Container | Host-Port | Datenbanken | Verwendung |
|-----------|-----------|-------------|------------|
| `postgres-jeeeraaah` | `localhost:5432` | `jeeeraaah`, `lib_test` | Application + Tests |
| `postgres-keycloak` | `localhost:5433` | `keycloak` | Identity Management |

## Verbindungs-Details

### 1. Von Host-Maschine (z.B. DBeaver, psql)

```bash
# Application Datenbank
Host: localhost
Port: 5432
Database: jeeeraaah
User: jeeeraaah
Password: jeeeraaah

# Test Datenbank (selber Container wie jeeeraaah!)
Host: localhost
Port: 5432
Database: lib_test
User: lib_test
Password: lib_test

# Keycloak Datenbank (separater Container)
Host: localhost
Port: 5433
Database: keycloak
User: keycloak
Password: keycloak
```

### 2. Von Docker Containern (z.B. Liberty Server, Keycloak)

```bash
# Application Datenbank
Host: postgres-jeeeraaah
Port: 5432
Database: jeeeraaah

# Test Datenbank
Host: postgres-jeeeraaah
Port: 5432
Database: lib_test

# Keycloak Datenbank
Host: postgres-keycloak
Port: 5432  # Intern immer 5432!
Database: keycloak
```

## IP-Adressen (WICHTIG!)

❌ **Verwende KEINE festen IP-Adressen wie `172.26.187.214`**

Docker IPs sind dynamisch und ändern sich bei jedem Neustart!

✅ **Verwende stattdessen:**
- Von Host: `localhost` mit Port-Mapping (`5432` oder `5433`)
- Von Containern: Container-Namen (`postgres-jeeeraaah`, `postgres-keycloak`)

## Credentials (Single Source of Truth)

Alle Credentials werden in `.env` definiert:

```bash
# postgres-jeeeraaah Container
POSTGRES_JEEERAAAH_DATABASE=jeeeraaah
POSTGRES_JEEERAAAH_USER=jeeeraaah
POSTGRES_JEEERAAAH_PASSWORD=jeeeraaah

POSTGRES_LIB_TEST_DATABASE=lib_test
POSTGRES_LIB_TEST_USER=lib_test
POSTGRES_LIB_TEST_PASSWORD=lib_test

# postgres-keycloak Container
POSTGRES_KEYCLOAK_DATABASE=keycloak
POSTGRES_KEYCLOAK_USER=keycloak
POSTGRES_KEYCLOAK_PASSWORD=keycloak
```

Diese Werte werden von:
- Docker Compose (Container-Erstellung)
- MicroProfile Config (Java Application)
- Init-Skripten (Datenbank-Setup)

automatisch verwendet.

## Initialisierung

### postgres-jeeeraaah

Init-Skript: `initdb/01-init-databases.sh`

1. Konfiguriert `jeeeraaah` DB (Extensions, Permissions)
2. Erstellt `lib_test` DB + User
3. Setzt Schema-Permissions

### postgres-keycloak

Init-Skript: `initdb/keycloak/01-init.sql`

1. Setzt Basis-Permissions
2. Keycloak erstellt sein Schema beim ersten Start

## Container neu initialisieren

```bash
# Alle Container + Volumes löschen
cd ~/develop/github/main/config/shared/docker
docker compose down -v

# Neu starten (triggert Init-Skripte)
docker compose up -d

# Logs prüfen
docker logs postgres-jeeeraaah | grep -E "(✅|ERROR)"
docker logs postgres-keycloak | grep -E "(✅|ERROR)"
```

## MicroProfile Config Properties

Die Java-Application nutzt diese Properties (werden automatisch aus `.env` generiert):

```properties
# Main Application DB (postgres-jeeeraaah:5432/jeeeraaah)
db.jeeeraaah.host=localhost
db.jeeeraaah.port=5432
db.jeeeraaah.name=jeeeraaah
db.jeeeraaah.username=jeeeraaah
db.jeeeraaah.password=jeeeraaah

# Test DB (postgres-jeeeraaah:5432/lib_test)
db.lib_test.host=localhost
db.lib_test.port=5432
db.lib_test.name=lib_test
db.lib_test.username=lib_test
db.lib_test.password=lib_test

# Keycloak DB (postgres-keycloak:5433/keycloak)
# Nur für Keycloak selbst, nicht für Java App
```

## DBeaver Konfiguration

### Connection: jeeeraaah (Application DB)
- Host: `localhost`
- Port: `5432`
- Database: `jeeeraaah`
- Username: `jeeeraaah`
- Password: `jeeeraaah`

### Connection: lib_test (Test DB)
- Host: `localhost`
- Port: `5432`
- Database: `lib_test`
- Username: `lib_test`
- Password: `lib_test`

### Connection: keycloak (Identity DB)
- Host: `localhost`
- Port: `5433` ← **Beachte: anderer Port!**
- Database: `keycloak`
- Username: `keycloak`
- Password: `keycloak`

## Häufige Probleme

### "password authentication failed"

**Ursache:** Alte Daten im Volume, neue Credentials in `.env`

**Lösung:**
```bash
docker compose down -v
docker compose up -d
```

### "database does not exist"

**Ursache:** Init-Skripte wurden nicht ausgeführt (Volume existierte bereits)

**Lösung:**
```bash
docker compose down -v
docker compose up -d
```

### "Connection refused" auf Port 5432/5433

**Ursache:** Container läuft nicht

**Lösung:**
```bash
docker ps  # Prüfe Status
docker compose up -d  # Falls gestoppt
docker logs postgres-jeeeraaah  # Prüfe Fehler
```

### Tests schlagen fehl: "lib_test not found"

**Ursache:** `lib_test` wurde nicht initialisiert

**Lösung:**
```bash
# Prüfe ob Datenbank existiert
docker exec postgres-jeeeraaah psql -U jeeeraaah -l | grep lib_test

# Falls nicht: Container neu initialisieren
docker compose down -v
docker compose up -d
```
