# Credentials - Zentrale Übersicht

**Single Point of Truth für alle Credentials im Projekt**

**Letzte Aktualisierung:** 2026-01-30

---

## 🔐 Credential-Verwaltung

### Entwicklungsumgebung (Development)

Alle Credentials werden in **einer zentralen Datei** verwaltet:

**Datei:** `config/shared/docker/.env`

**Format:** `SCHLÜSSEL=wert` (keine Anführungszeichen!)

**Versionierung:** 
- ❌ `.env` ist in `.gitignore` (enthält echte Credentials)
- ✅ `.env.template` ist in Git (enthält Platzhalter)

---

## 📋 Credential-Übersicht

### PostgreSQL - JEEERAAAH Application

```bash
# Container: postgres-jeeeraaah (Port 5432)
POSTGRES_JEEERAAAH_HOST=localhost
POSTGRES_JEEERAAAH_PORT=5432
POSTGRES_JEEERAAAH_DATABASE=jeeeraaah
POSTGRES_JEEERAAAH_USER=jeeeraaah
POSTGRES_JEEERAAAH_PASSWORD=jeeeraaah
```

**Verwendung:**
- Backend: OpenLiberty `server.xml` liest via MicroProfile Config
- Tests: Hibernate `persistence.xml` liest via MicroProfile Config
- Docker: `docker-compose.yml` mountet `.env`

### PostgreSQL - lib_test (Integrationstests)

```bash
# Container: postgres-lib-test (Port 5434)
POSTGRES_LIB_TEST_HOST=localhost
POSTGRES_LIB_TEST_PORT=5434
POSTGRES_LIB_TEST_DATABASE=lib_test
POSTGRES_LIB_TEST_USER=lib_test
POSTGRES_LIB_TEST_PASSWORD=lib_test
```

**Verwendung:**
- Library-Tests: `root/lib/*/src/test/java`
- JPA-Tests: Hibernate Konfiguration via MicroProfile Config

### PostgreSQL - Keycloak Persistence

```bash
# Container: postgres-keycloak (Port 5433)
POSTGRES_KEYCLOAK_HOST=localhost
POSTGRES_KEYCLOAK_PORT=5433
POSTGRES_KEYCLOAK_DATABASE=keycloak
POSTGRES_KEYCLOAK_USER=keycloak
POSTGRES_KEYCLOAK_PASSWORD=keycloak
```

**Verwendung:**
- Keycloak Container: Speichert Realms, Users, etc.
- **Wichtig:** Ohne diese DB ist Keycloak nicht persistent!

### Keycloak Admin Console

```bash
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PASSWORD=admin
```

**Admin Console URL:** http://localhost:8080/admin

**Verwendung:**
- Manuelle Admin-Aufgaben (Browser)
- Automatisches Realm-Setup (Java Keycloak Admin Client)

### Keycloak Realm

```bash
KEYCLOAK_REALM=jeeeraaah-realm
```

**Verwendung:**
- Backend: JWT Token Validation
- Frontend: Token Requests
- Setup: Realm Creation via Keycloak Admin API

### Keycloak Test User (Entwicklung)

```bash
KEYCLOAK_TEST_USER=testuser
KEYCLOAK_TEST_PASSWORD=test
```

**Verwendung:**
- Automatischer Login in Test-Modus (`testing=true`)
- Frontend: Überspringe Login-Dialog

---

## 🔄 Wie Credentials verwendet werden

### 1. Docker Compose

**Datei:** `config/shared/docker/docker-compose.yml`

```yaml
services:
  postgres-jeeeraaah:
    environment:
      POSTGRES_DB: ${POSTGRES_JEEERAAAH_DATABASE}
      POSTGRES_USER: ${POSTGRES_JEEERAAAH_USER}
      POSTGRES_PASSWORD: ${POSTGRES_JEEERAAAH_PASSWORD}
```

**Mechanismus:**
- Docker Compose liest `.env` automatisch
- Variablen werden via `${VAR}` referenziert

### 2. MicroProfile Config (Java)

**Datei:** `testing.properties` (Projekt-Root)

```properties
# PostgreSQL JEEERAaH
db.jeeeraaah.host=${POSTGRES_JEEERAAAH_HOST}
db.jeeeraaah.port=${POSTGRES_JEEERAAAH_PORT}
db.jeeeraaah.name=${POSTGRES_JEEERAAAH_DATABASE}
db.jeeeraaah.username=${POSTGRES_JEEERAAAH_USER}
db.jeeeraaah.password=${POSTGRES_JEEERAAAH_PASSWORD}

# Keycloak
keycloak.server.url=http://localhost:8080
keycloak.realm=${KEYCLOAK_REALM}
keycloak.admin.user=${KEYCLOAK_ADMIN_USER}
keycloak.admin.password=${KEYCLOAK_ADMIN_PASSWORD}
```

**Mechanismus:**
- SmallRye Config liest `testing.properties`
- Environment Variables haben Vorrang (`POSTGRES_JEEERAAAH_HOST` überschreibt Property)
- `@ConfigProperty` Injection in Java-Code

### 3. OpenLiberty server.xml

**Datei:** `root/app/jeeeraaah/backend/api/ws.rs/src/main/liberty/config/server.xml`

```xml
<dataSource id="jeeeraaahDS">
    <jdbcDriver libraryRef="postgresLib"/>
    <properties.postgresql
        serverName="${db.jeeeraaah.host}"
        portNumber="${db.jeeeraaah.port}"
        databaseName="${db.jeeeraaah.name}"
        user="${db.jeeeraaah.username}"
        password="${db.jeeeraaah.password}"/>
</dataSource>
```

**Mechanismus:**
- Liberty liest MicroProfile Config (`testing.properties`)
- `${db.*}` Variablen werden ersetzt

### 4. Keycloak Realm Setup (Java)

**Datei:** `root/lib/keycloak.admin/src/main/java/.../KeycloakRealmSetup.java`

```java
@Inject
@ConfigProperty(name = "keycloak.admin.user")
String adminUser;

@Inject
@ConfigProperty(name = "keycloak.admin.password")
String adminPassword;
```

**Mechanismus:**
- CDI + MicroProfile Config Injection
- Liest aus `testing.properties`

---

## 🛡️ Sicherheit

### Entwicklung (lokale Workstation)

✅ **Erlaubt:** Einfache Passwörter wie `admin`, `jeeeraaah`, `test`

**Grund:** 
- Keine Netzwerk-Exposition (nur localhost)
- Docker Container laufen nur lokal
- Schnellerer Setup

### Produktion (Server/Cloud)

❌ **NIEMALS:** Einfache Passwörter verwenden!

**Empfehlung:**
1. Separates `.env` File auf Server
2. Starke, generierte Passwörter
3. Secrets Management (z.B. Docker Secrets, Vault)
4. Regelmäßiger Passwort-Rotation

**Beispiel `.env` für Produktion:**
```bash
POSTGRES_JEEERAAAH_PASSWORD=$(openssl rand -base64 32)
KEYCLOAK_ADMIN_PASSWORD=$(openssl rand -base64 32)
```

---

## 📝 Credential-Änderungen

### Schritt 1: `.env` aktualisieren

```bash
cd ~/develop/github/main/config/shared/docker
nano .env
```

**Beispiel:** PostgreSQL Passwort ändern
```bash
POSTGRES_JEEERAAAH_PASSWORD=neues-passwort
```

### Schritt 2: Docker Container neu erstellen

```bash
# Alle Container stoppen und löschen
ruu-docker-down

# Volumes löschen (wichtig für neue Passwörter!)
docker volume rm postgres-jeeeraaah-data

# Container neu starten (liest neue .env)
ruu-docker-startup
```

**Wichtig:** PostgreSQL speichert Passwörter im Volume! Daher muss Volume gelöscht werden.

### Schritt 3: Keycloak Realm neu erstellen

```bash
# Falls Keycloak Admin-Passwort geändert wurde
ruu-keycloak-setup
```

---

## 🧪 Testing Credentials

Für automatisierte Tests wird ein spezieller Test-User verwendet:

**Property:** `testing=true` (in `testing.properties`)

**Effekt:**
- Frontend: Kein Login-Dialog, automatischer Login mit Test-User
- Backend: Akzeptiert Test-Token

**Test-User Credentials:**
```bash
KEYCLOAK_TEST_USER=testuser
KEYCLOAK_TEST_PASSWORD=test
```

**Keycloak Setup:** Test-User wird automatisch erstellt bei `ruu-keycloak-setup`

---

## 🔗 Credential-Fluss (Übersicht)

```
.env
 ├─> Docker Compose (Container Environment)
 │    ├─> postgres-jeeeraaah
 │    ├─> postgres-lib-test
 │    ├─> postgres-keycloak
 │    └─> keycloak
 │
 └─> testing.properties (via Env Vars)
      ├─> OpenLiberty server.xml (Backend)
      ├─> Hibernate persistence.xml (Tests)
      ├─> Keycloak Admin Client (Setup)
      └─> Frontend Config (Auto-Login)
```

**Wichtig:** Alle Wege führen zu `.env`! (Single Point of Truth)

---

## 🚨 Troubleshooting

### Problem: "password authentication failed"

**Ursache:** Credentials stimmen nicht überein

**Lösung:**
```bash
# 1. Prüfe .env
cat ~/develop/github/main/config/shared/docker/.env

# 2. Prüfe ob Container alte Credentials cached
docker inspect postgres-jeeeraaah | grep -A5 POSTGRES

# 3. Container + Volume löschen, neu starten
ruu-docker-reset
ruu-docker-startup
```

### Problem: "Keycloak Admin Login fehlgeschlagen"

**Ursache:** Keycloak Admin Credentials falsch

**Lösung:**
```bash
# 1. Prüfe .env
grep KEYCLOAK_ADMIN ~/develop/github/main/config/shared/docker/.env

# 2. Keycloak Container + Volume neu erstellen
docker compose down
docker volume rm keycloak-data
ruu-docker-startup
```

### Problem: "Realm nicht gefunden"

**Ursache:** Realm nicht erstellt oder Keycloak-DB gelöscht

**Lösung:**
```bash
# Realm neu erstellen
ruu-keycloak-setup

# Oder: Komplett-Reset
ruu-docker-reset
ruu-docker-startup  # Erstellt Realm automatisch
```

---

## 📚 Siehe auch

| Dokument | Beschreibung |
|----------|-------------|
| [SINGLE-POINT-OF-TRUTH.md](SINGLE-POINT-OF-TRUTH.md) | Konfigurationsverwaltung |
| [KEYCLOAK-ADMIN-CONSOLE.md](KEYCLOAK-ADMIN-CONSOLE.md) | Keycloak Admin Aufgaben |
| [TROUBLESHOOTING.md](TROUBLESHOOTING.md) | Allgemeine Problemlösungen |
| [shared/docker/.env.template](shared/docker/.env.template) | Template für .env |

---

**Bei Problemen:** Siehe [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
