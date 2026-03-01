# Docker Environment - Rigorous Testing & Troubleshooting

## 🚀 Quick Start - Automated Testing

```bash
cd ~/develop/github/main/config/shared/docker
./rigorous-test.sh
```

Das Script führt **alle** Tests automatisch durch:
- ✅ Bereinigt alte Container und Volumes
- ✅ Startet PostgreSQL mit allen 3 Datenbanken (jeeeraaah, lib_test, keycloak)
- ✅ Verifiziert User-Authentifizierung
- ✅ Testet Berechtigungen (CREATE/DROP TABLE)
- ✅ Startet und konfiguriert Keycloak
- ✅ Startet JasperReports
- ✅ Führt Java-Tests aus
- ✅ Zeigt Zusammenfassung mit PASS/FAIL

## 📊 Expected Test Results

```
════════════════════════════════════════════════════════════════
📊 TEST RESULTS
════════════════════════════════════════════════════════════════
✅ PASSED: 17
❌ FAILED: 0

🎉 ALL TESTS PASSED!

✅ Docker environment is ready
✅ All databases are accessible
✅ All users can authenticate
✅ Keycloak realm is configured
✅ Java tests pass
```

## 🔧 Manual Testing Commands

### Test 1: PostgreSQL Database Existence

```bash
docker exec postgres psql -U postgres -c "\l" | grep -E "(jeeeraaah|lib_test|keycloak)"
```

**Expected Output:**
```
 jeeeraaah  | jeeeraaah  | UTF8     | ...
 keycloak   | keycloak   | UTF8     | ...
 lib_test   | lib_test   | UTF8     | ...
```

### Test 2: User Authentication

```bash
# Test jeeeraaah user
docker exec postgres psql -U jeeeraaah -d jeeeraaah -c "SELECT current_user, current_database();"

# Test lib_test user  
docker exec postgres psql -U lib_test -d lib_test -c "SELECT current_user, current_database();"

# Test keycloak user
docker exec postgres psql -U keycloak -d keycloak -c "SELECT current_user, current_database();"
```

**Expected Output (example for lib_test):**
```
 current_user | current_database 
--------------+------------------
 lib_test     | lib_test
```

### Test 3: User Permissions (CREATE/DROP)

```bash
docker exec postgres psql -U lib_test -d lib_test -c "
  CREATE TABLE test_permissions (id INT);
  INSERT INTO test_permissions VALUES (1);
  SELECT * FROM test_permissions;
  DROP TABLE test_permissions;
"
```

**Expected Output:**
```
CREATE TABLE
INSERT 0 1
 id 
----
  1
DROP TABLE
```

### Test 4: Container Health Status

```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

**Expected Output:**
```
NAMES          STATUS
postgres       Up X minutes (healthy)
keycloak       Up X minutes (healthy)
jasperreports  Up X minutes (healthy)
```

### Test 5: Keycloak Realm

```bash
curl -s http://localhost:8080/realms/jeeeraaah-realm/.well-known/openid-configuration | jq .issuer
```

**Expected Output:**
```json
"http://localhost:8080/realms/jeeeraaah-realm"
```

### Test 6: Java JPA Tests

```bash
cd ~/develop/github/main/root
mvn test -pl lib/jpa/se_hibernate_postgres_demo
```

**Expected Output:**
```
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Test 7: Configuration Health Check

```bash
cd ~/develop/github/main/root
mvn test -pl lib/util/config/mp -Dtest=ConfigHealthCheckTest
```

**Expected Output:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## ❌ Common Problems & Solutions

### Problem 1: "password authentication failed for user 'lib_test'"

**Ursache:** Init-Scripts wurden nicht ausgeführt oder User existiert nicht.

**Lösung:**
```bash
# Kompletter Reset
cd ~/develop/github/main/config/shared/docker
docker compose down -v
docker compose up -d postgres

# Warten und verifizieren
sleep 30
docker exec postgres psql -U lib_test -d lib_test -c "SELECT 1;"
```

### Problem 2: Container mit alten Namen existieren noch

**Symptom:**
```
CONTAINER ID   IMAGE              COMMAND   CREATED   STATUS    NAMES
xxx            postgres:16        ...       ...       ...       postgres-jeeeraaah  ❌
```

**Lösung:**
```bash
# Alte Container entfernen
docker rm -f postgres-jeeeraaah postgres-keycloak keycloak-jeeeraaah

# Richtige Container starten
docker compose up -d
```

**Korrekte Namen:**
- ✅ `postgres` (NICHT postgres-jeeeraaah)
- ✅ `keycloak` (NICHT keycloak-jeeeraaah)  
- ✅ `jasperreports`

### Problem 3: Keycloak ist "unhealthy"

**Ursache:** Keycloak-Datenbank nicht initialisiert oder falsche Credentials.

**Lösung:**
```bash
# Check Keycloak Logs
docker logs keycloak 2>&1 | grep -i "error\|fatal"

# Verify keycloak database
docker exec postgres psql -U keycloak -d keycloak -c "\dt"

# Restart Keycloak
docker compose restart keycloak
sleep 60
docker ps | grep keycloak
```

### Problem 4: Init-Scripts werden nicht ausgeführt

**Symptom:** Datenbanken existieren nicht, obwohl Container läuft.

**Debugging:**
```bash
# Prüfe ob Init-Scripts vorhanden sind
ls -la ~/develop/github/main/config/shared/docker/initdb/

# Erwartete Dateien:
# 01-init-jeeeraaah.sql
# 02-init-lib_test.sql
# 03-init-keycloak.sql

# Prüfe Container-Logs für Init-Meldungen
docker logs postgres 2>&1 | grep -i "executing.*init"
```

**Lösung:**
```bash
# Volumes komplett löschen und neu initialisieren
docker compose down -v
docker volume prune -f
docker compose up -d postgres
```

### Problem 5: "database lib_test does not exist"

**Ursache:** Init-Script `02-init-lib_test.sql` wurde nicht ausgeführt.

**Verifizierung:**
```bash
# Check ob Datei existiert
cat ~/develop/github/main/config/shared/docker/initdb/02-init-lib_test.sql

# Check ob ausgeführt wurde
docker exec postgres psql -U postgres -c "\l" | grep lib_test
```

**Manuelle Reparatur (nur wenn automatisch nicht funktioniert):**
```bash
docker exec postgres psql -U postgres <<EOF
CREATE DATABASE lib_test;
CREATE USER lib_test WITH PASSWORD 'lib_test';
GRANT ALL PRIVILEGES ON DATABASE lib_test TO lib_test;
\c lib_test
GRANT ALL ON SCHEMA public TO lib_test;
EOF
```

## 📝 Configuration Single Point of Truth

Alle Credentials sind in **`testing.properties`** definiert (nicht in Git versioniert):

```properties
# PostgreSQL - jeeeraaah
db.jeeeraaah.host=localhost
db.jeeeraaah.port=5432
db.jeeeraaah.name=jeeeraaah
db.jeeeraaah.username=<siehe testing.properties>
db.jeeeraaah.password=<siehe testing.properties>

# PostgreSQL - lib_test  
db.lib_test.host=localhost
db.lib_test.port=5432
db.lib_test.name=lib_test
db.lib_test.username=<siehe testing.properties>
db.lib_test.password=<siehe testing.properties>

# PostgreSQL - keycloak
db.keycloak.host=localhost
db.keycloak.port=5432
db.keycloak.name=keycloak
db.keycloak.username=<siehe testing.properties>
db.keycloak.password=<siehe testing.properties>

# Keycloak
keycloak.server.url=http://localhost:8080
keycloak.realm=jeeeraaah-realm
keycloak.test.user=<siehe testing.properties>
keycloak.test.password=<siehe testing.properties>
keycloak.admin.username=<siehe testing.properties>
keycloak.admin.password=<siehe testing.properties>
```

**Hinweis:** Die tatsächlichen Credentials befinden sich in `testing.properties` (im Projekt-Root), 
die NICHT in Git versioniert wird. Siehe `testing.properties.template` für die benötigten Properties.

Diese Properties werden automatisch von **MicroProfile Config** geladen!

## 🔍 Debugging Tips

### 1. Container Logs ansehen

```bash
# PostgreSQL
docker logs postgres

# Keycloak  
docker logs keycloak

# JasperReports
docker logs jasperreports
```

### 2. In Container einloggen

```bash
# PostgreSQL Shell
docker exec -it postgres psql -U postgres

# Bash Shell
docker exec -it postgres bash
```

### 3. Health Check manuell ausführen

```bash
# PostgreSQL
docker exec postgres pg_isready -U postgres

# Keycloak
curl -f http://localhost:8080/health/ready
```

### 4. Netzwerk prüfen

```bash
# Zeige Docker-Netzwerk
docker network inspect ruu-network

# Zeige Container-IPs
docker inspect -f '{{.Name}} - {{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $(docker ps -q)
```

## ✅ Success Criteria

Alle folgenden Punkte müssen erfüllt sein:

1. ✅ **3 PostgreSQL-Datenbanken** existieren: jeeeraaah, lib_test, keycloak
2. ✅ **3 PostgreSQL-User** können sich authentifizieren
3. ✅ **Alle User** können Tabellen erstellen und löschen
4. ✅ **3 Container** laufen und sind healthy: postgres, keycloak, jasperreports
5. ✅ **Keine alten Container** mit Namen wie `postgres-jeeeraaah` existieren
6. ✅ **Keycloak Realm** `jeeeraaah-realm` ist erreichbar
7. ✅ **Java JPA Tests** laufen erfolgreich durch
8. ✅ **Config Health Check** ist grün

## 📊 Final Verification Command

```bash
cd ~/develop/github/main/config/shared/docker
./rigorous-test.sh && echo "🎉 ENVIRONMENT IS READY!"
```

Wenn alle Tests bestehen, ist die Umgebung **produktionsbereit**!
