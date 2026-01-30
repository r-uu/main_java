# PostgreSQL Init-Skripte

Dieses Verzeichnis enthält Initialisierungs-Skripte für die PostgreSQL Container.

## Container-Übersicht

Das Projekt verwendet **drei separate PostgreSQL Container**:

### 1. postgres-jeeeraaah (`localhost:5432`)
- **Datenbank:** `jeeeraaah`
- **Container-Name:** `postgres-jeeeraaah`
- **Init-Skripte:** `initdb/jeeeraaah/`
- **Credentials aus .env:** `POSTGRES_JEEERAAAH_*`
- **Zweck:** Hauptdatenbank für die JEEERAaH Anwendung

### 2. postgres-lib-test (`localhost:5434`)
- **Datenbank:** `lib_test`
- **Container-Name:** `postgres-lib-test`
- **Init-Skripte:** `initdb/lib_test/`
- **Credentials aus .env:** `POSTGRES_LIB_TEST_*`
- **Zweck:** Dedizierte Test-Datenbank für Integrationstests

### 3. postgres-keycloak (`localhost:5433`)
- **Datenbank:** `keycloak`
- **Container-Name:** `postgres-keycloak`
- **Init-Skripte:** `initdb/keycloak/`
- **Credentials aus .env:** `POSTGRES_KEYCLOAK_*`
- **Zweck:** Keycloak Identity Management Persistierung

## Verzeichnisstruktur

```
initdb/
├── jeeeraaah/
│   └── 01-init.sql          → Initialisierung für jeeeraaah DB
├── lib_test/
│   └── 01-init-database.sql → Initialisierung für lib_test DB
├── keycloak/
│   └── 01-init.sql          → Initialisierung für keycloak DB
└── README.md                → Diese Datei
```

**Wichtig:** 
- Jeder Container mountet nur sein eigenes Unterverzeichnis
- `postgres-jeeeraaah` → `./initdb/jeeeraaah:/docker-entrypoint-initdb.d:ro`
- `postgres-lib-test` → `./initdb/lib_test:/docker-entrypoint-initdb.d:ro`
- `postgres-keycloak` → `./initdb/keycloak:/docker-entrypoint-initdb.d:ro`

## Verwendung

### Automatische Initialisierung

Alle Init-Skripte werden automatisch beim **ersten Start** des jeweiligen Containers ausgeführt.

**Reihenfolge:** Skripte werden **alphabetisch** ausgeführt (daher Präfixe `01-`, `02-`, etc.)

### Manueller Neustart

Um die Datenbanken neu zu initialisieren:

```bash
# Alle Container und Volumes löschen
cd ~/develop/github/main/config/shared/docker
docker compose down -v

# Volumes explizit löschen (falls nötig)
docker volume rm postgres-jeeeraaah-data postgres-keycloak-data

# Container neu starten (triggert Init-Skripte)
docker compose up -d
```

### Aliase nutzen (falls konfiguriert)

```bash
ruu-docker-reset    # Löscht alle Container + Volumes
ruu-docker-start    # Startet alle Container neu
```

## Init-Skript Details

### jeeeraaah/01-init.sql (postgres-jeeeraaah)

Wird ausgeführt im `postgres-jeeeraaah` Container und:
- Konfiguriert die `jeeeraaah` Datenbank (bereits von Docker über `POSTGRES_DB` erstellt)
- Setzt Schema-Berechtigungen
- Aktiviert Extensions: `uuid-ossp`, `pg_trgm`

### lib_test/01-init-database.sql (postgres-lib-test)

Wird ausgeführt im `postgres-lib-test` Container und:
- Konfiguriert die `lib_test` Datenbank (bereits von Docker über `POSTGRES_DB` erstellt)
- Setzt Schema-Berechtigungen
- Konfiguriert Default-Privileges für zukünftige Tabellen/Sequences

### keycloak/01-init.sql (postgres-keycloak)

Wird ausgeführt im `postgres-keycloak` Container und:
- Setzt Basis-Berechtigungen für `keycloak` User
- Keycloak erstellt sein eigenes Schema beim ersten Start

## Verbindungs-Details

### Von der Host-Maschine (außerhalb Docker)

```bash
# jeeeraaah Datenbank
psql -h localhost -p 5432 -U jeeeraaah -d jeeeraaah

# lib_test Datenbank (eigener Container!)
psql -h localhost -p 5434 -U lib_test -d lib_test

# keycloak Datenbank (eigener Container)
psql -h localhost -p 5433 -U keycloak -d keycloak
```

### Von anderen Docker Containern (im selben Netzwerk)

```bash
# jeeeraaah Datenbank
psql -h postgres-jeeeraaah -p 5432 -U jeeeraaah -d jeeeraaah

# lib_test Datenbank
psql -h postgres-lib-test -p 5432 -U lib_test -d lib_test

# keycloak Datenbank
psql -h postgres-keycloak -p 5432 -U keycloak -d keycloak
```

**Beachte:** Intern nutzen alle Container Port `5432`, aber von außen sind es `5432`, `5433` und `5434`.

## IP-Adressen

- **Von Host:** Nutze `localhost` (wird zu Container-IP gemappt)
- **Von WSL/Container:** Nutze Container-Namen (`postgres-jeeeraaah`, `postgres-keycloak`)
- **Interne IPs** (wie `172.26.187.214`) sind dynamisch und sollten nicht hardcodiert werden

## Eigene Skripte hinzufügen

### Für postgres-jeeeraaah

Erstelle `initdb/jeeeraaah/02-my-script.sh`:

```bash
#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE TABLE my_table (id SERIAL PRIMARY KEY);
    \echo '✅ My table created'
EOSQL
```

### Für postgres-lib-test

Erstelle `initdb/lib_test/02-my-test-script.sql`:

```sql
-- Additional test database setup
CREATE EXTENSION IF NOT EXISTS pgcrypto;
SELECT 'Test database extended successfully' AS status;
```

### Für postgres-keycloak

Erstelle `initdb/keycloak/02-my-keycloak-script.sql`:

```sql
-- Additional Keycloak setup
GRANT SELECT ON ALL TABLES IN SCHEMA public TO keycloak;
```

## Umgebungsvariablen

Verfügbar in Init-Skripten:

**postgres-jeeeraaah:**
- `$POSTGRES_DB` = jeeeraaah
- `$POSTGRES_USER` = jeeeraaah
- `$POSTGRES_PASSWORD` = jeeeraaah

**postgres-lib-test:**
- `$POSTGRES_DB` = lib_test
- `$POSTGRES_USER` = lib_test
- `$POSTGRES_PASSWORD` = lib_test

**postgres-keycloak:**
- `$POSTGRES_DB` = keycloak
- `$POSTGRES_USER` = keycloak
- `$POSTGRES_PASSWORD` = keycloak

## Logs prüfen

```bash
# postgres-jeeeraaah Logs
docker logs postgres-jeeeraaah 2>&1 | grep -E "(✅|ERROR)"

# postgres-lib-test Logs
docker logs postgres-lib-test 2>&1 | grep -E "(✅|ERROR)"

# postgres-keycloak Logs
docker logs postgres-keycloak 2>&1 | grep -E "(✅|ERROR)"
```

## Best Practices

1. ✅ Verwende `set -e` in Bash-Skripten (stoppt bei Fehler)
2. ✅ Verwende `ON_ERROR_STOP=1` für psql
3. ✅ Verwende `IF NOT EXISTS` für CREATE-Statements (falls idempotent nötig)
4. ✅ Logge Fortschritt mit `\echo '✅ ...'`
5. ✅ Benenne Skripte mit Präfixen: `01-`, `02-`, etc.
6. ❌ Keine Secrets in Skripten hardcoden (nutze Env-Vars)
7. ❌ Keine produktiven Daten in Git committen

## Troubleshooting

### Skript wird nicht ausgeführt

Init-Skripte werden nur beim **ersten Start** ausgeführt (wenn Volume leer ist).

**Lösung:** Volume löschen und neu starten:
```bash
docker compose down -v
docker compose up -d
```

### Berechtigungsfehler

Stelle sicher, dass Skripte ausführbar sind:
```bash
chmod +x config/shared/docker/initdb/*.sh
```

### Falscher Container nutzt Skript

- Skripte in `initdb/jeeeraaah/` → `postgres-jeeeraaah`
- Skripte in `initdb/lib_test/` → `postgres-lib-test`
- Skripte in `initdb/keycloak/` → `postgres-keycloak`

### Datenbank existiert nicht

Prüfe:
1. Wurde der Container neu gestartet NACH Volume-Löschung?
2. Sind die Skripte fehlerfrei? (siehe Logs)
3. Stimmen die Env-Variablen in `.env`?


