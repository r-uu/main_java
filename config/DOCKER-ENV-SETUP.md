# Docker Environment Setup - Komplettanleitung

## Überblick

Alle Docker-Credentials werden zentral in `.env` verwaltet.

**WICHTIG:** Die Variable**nnamen** und Variable**nwerte** sind bewusst identisch!
Dies erleichtert das Debugging und macht Konfigurationsprobleme sofort sichtbar.

## Credentials

### PostgreSQL Admin (superuser)
- **Username:** `postgres_admin_username`
- **Password:** `postgres_admin_password`
- **Database:** `postgres`

### PostgreSQL JEEERAaH (Application DB)
- **Container:** `postgres-jeeeraaah`
- **Port:** `5432`
- **Username:** `postgres_jeeeraaah_username`
- **Password:** `postgres_jeeeraaah_password`
- **Databases:**
  - `jeeeraaah` (Haupt-Anwendung)
  - `lib_test` (automatisch erstellt via Init-Skript)

### PostgreSQL Keycloak (Identity DB)
- **Container:** `postgres-keycloak`
- **Port:** `5433`
- **Username:** `postgres_keycloak_username`
- **Password:** `postgres_keycloak_password`
- **Database:** `keycloak`

### Keycloak Admin
- **Container:** `keycloak`
- **Port:** `8080`
- **Admin Console:** http://localhost:8080/admin
- **Username:** `keycloak_admin_username`
- **Password:** `keycloak_admin_password`

### Test User (JEEERAaH Application)
- **Username:** `test_username`
- **Password:** `test_password`

## Kompletter Reset & Setup

### 1. Alle Container und Volumes löschen

```bash
cd ~/develop/github/main/config/shared/docker
docker compose down -v
```

### 2. Container neu starten

```bash
docker compose up -d
```

Warten bis alle Container healthy sind (~60 Sekunden):

```bash
docker ps
```

### 3. Keycloak Realm erstellen

```bash
cd ~/develop/github/main/root/lib/keycloak.admin

# Environment-Variablen aus .env laden
source ~/develop/github/main/config/shared/docker/.env

# Realm Setup ausführen
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```

**Alternative mit expliziten System Properties:**

```bash
mvn exec:java \
  -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup" \
  -Dkeycloak.admin.user=keycloak_admin_username \
  -Dkeycloak.admin.password=keycloak_admin_password \
  -Dkeycloak.test.user=test_username \
  -Dkeycloak.test.password=test_password
```

### 4. Verify Setup

#### Keycloak Admin Console
1. Öffne: http://localhost:8080/admin
2. Login mit:
   - Username: `keycloak_admin_username`
   - Password: `keycloak_admin_password`

3. Prüfe Realm "jeeeraaah-realm":
   - Client: "jeeeraaah-frontend"
   - User: "test_username"

#### PostgreSQL Databases

```bash
# JEEERAaH DB
docker exec -it postgres-jeeeraaah psql -U postgres_jeeeraaah_username -d jeeeraaah -c '\l'

# lib_test DB
docker exec -it postgres-jeeeraaah psql -U postgres_jeeeraaah_username -d lib_test -c '\l'

# Keycloak DB
docker exec -it postgres-keycloak psql -U postgres_keycloak_username -d keycloak -c '\l'
```

## Wichtige Hinweise

### Environment-Variablen

Die Java-Anwendungen lesen Credentials aus:
1. **System Properties** (höchste Priorität)
2. **Environment Variables**
3. **Fallback-Defaults** (gleich dem Variablennamen)

### Docker Compose Environment

Docker Compose lädt `.env` automatisch und substituiert `${variable_name}` Platzhalter.

### Health Checks

Die Docker Health Checks verwenden fest kodierte Variablennamen als Fallbacks:
- `PostgresDatabaseHealthCheck`: Verwendet `postgres_jeeeraaah_username` / `postgres_keycloak_username`
- `KeycloakRealmHealthCheck`: Verwendet `keycloak_admin_username`

### Init Scripts

PostgreSQL Init-Skripte in `initdb/` verwenden `$POSTGRES_USER` und `$POSTGRES_DB` 
aus den Environment-Variablen des postgres-jeeeraaah Containers.

## Troubleshooting

### Keycloak 401 Unauthorized

**Problem:** KeycloakRealmSetup schlägt fehl mit 401

**Lösung:** Environment-Variablen explizit setzen vor mvn exec:

```bash
source ~/develop/github/main/config/shared/docker/.env
mvn exec:java -Dexec.mainClass="..."
```

### Database "lib_test" existiert nicht

**Problem:** Tests schlagen fehl weil lib_test nicht existiert

**Lösung:**  
1. postgres-jeeeraaah Container neu starten (Init-Skript läuft)
2. Oder manuell erstellen:

```bash
docker exec -i postgres-jeeeraaah psql -U postgres_jeeeraaah_username -d postgres -c \
  "CREATE DATABASE lib_test OWNER postgres_jeeeraaah_username;"
```

### Keycloak Realm fehlt

**Problem:** Application schlägt fehl wegen fehlendem Realm

**Lösung:** KeycloakRealmSetup erneut ausführen (siehe oben)

## Aliases

Füge zu `~/.bashrc` hinzu:

```bash
alias ruu-docker-reset='cd ~/develop/github/main/config/shared/docker && docker compose down -v && docker compose up -d'
alias ruu-keycloak-setup='cd ~/develop/github/main/root/lib/keycloak.admin && source ~/develop/github/main/config/shared/docker/.env && mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"'
```

Dann reload:

```bash
source ~/.bashrc
```

Verwendung:

```bash
ruu-docker-reset    # Reset komplette Docker-Umgebung
ruu-keycloak-setup  # Keycloak Realm erstellen
```
