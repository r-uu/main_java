# 🔐 Credentials Übersicht
**Single Point of Truth für alle Development-Credentials**
## 🔧 Verwendung
### PostgreSQL - Schema: jeeeraaah
```bash
# Shell-Zugriff
docker exec -it postgres psql -U jeeeraaah -d jeeeraaah
# JDBC URL
jdbc:postgresql://localhost:5432/jeeeraaah?user=jeeeraaah&password=jeeeraaah
```
### PostgreSQL - Schema: lib_test
```bash
# Shell-Zugriff
docker exec -it postgres psql -U lib_test -d lib_test
# JDBC URL
jdbc:postgresql://localhost:5432/lib_test?user=lib_test&password=lib_test
```
### PostgreSQL - Schema: keycloak
```bash
# Shell-Zugriff
docker exec -it postgres psql -U keycloak -d keycloak
# JDBC URL
jdbc:postgresql://localhost:5432/keycloak?user=keycloak&password=keycloak
```
### Keycloak Admin Console
```bash
# URL öffnen
open http://localhost:8080/admin
# Login (Master Realm)
Username: admin
Password: admin
```
### Keycloak Test User (jeeeraaah-realm)
```bash
# Test-Login via cURL
curl -X POST 'http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=test' \
  -d 'password=test' \
  -d 'grant_type=password' \
  -d 'client_id=jeeeraaah-frontend'
# Für automatisches Login in der App
Username: test
Password: test
```
---
## 📁 Konfiguration
### `.env` (lokal, NICHT in Git!)
```bash
# PostgreSQL Container
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
# Schema: jeeeraaah
POSTGRES_JEEERAAAH_DB=jeeeraaah
POSTGRES_JEEERAAAH_USER=jeeeraaah
POSTGRES_JEEERAAAH_PASSWORD=jeeeraaah
# Schema: lib_test
POSTGRES_LIB_TEST_DB=lib_test
POSTGRES_LIB_TEST_USER=lib_test
POSTGRES_LIB_TEST_PASSWORD=lib_test
# Schema: keycloak
POSTGRES_KEYCLOAK_DB=keycloak
POSTGRES_KEYCLOAK_USER=keycloak
POSTGRES_KEYCLOAK_PASSWORD=keycloak
# Keycloak Admin
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PASSWORD=admin
# Keycloak Test User
KEYCLOAK_TEST_USERNAME=test
KEYCLOAK_TEST_PASSWORD=test
```
### `testing.properties`
```properties
# Testing Mode
testing.enabled=true
testing=true
testing.username=test
testing.password=test
# Keycloak Test User
keycloak.test.user=test
keycloak.test.password=test
```
---
## 🔒 Sicherheit
### ✅ OK für Dev:
- Schema-basierte User (jeeeraaah/jeeeraaah, lib_test/lib_test, keycloak/keycloak)
- Einheitlicher Test User (test/test)
- Admin Zugang (admin/admin)
- Einfaches Debugging
- Keine Produktionsdaten
### ❌ NIEMALS in Produktion:
- Schwache Passwörter
- `.env` in Git
- Hardcoded Credentials
---
## 🚀 Neustart
```bash
# Alles löschen und neu starten
cd ~/develop/github/main/config/shared/docker
docker compose down -v
docker compose up -d
# Warte auf Postgres Health Status
sleep 10
# Keycloak und JasperReports starten
docker compose up -d keycloak jasperreports
# Warte auf Keycloak Health Status
sleep 30
# Keycloak Realm Setup (erstellt test/test User)
cd ~/develop/github/main/root/lib/keycloak_admin
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```
---
**Merke:** 
- PostgreSQL: Schema-Name = Username = Password
- Keycloak Admin: admin / admin
- Keycloak Test User: test / test 🎯
