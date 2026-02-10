# Credentials - Complete Reference
**Single Point of Truth for all project credentials**
**Last Update:** 2026-02-09
---
## 🔐 Credential Management
### Development Environment
**All credentials** are defined in: `config/shared/docker/.env`
**Format:** `KEY=value` (no quotes!)
**Version Control:**
- ❌ `.env` - Contains real credentials (in `.gitignore`)
- ✅ `.env.template` - Template with placeholders (in Git)
---
## 📋 Quick Reference
### Application Login
| Service | Username | Password | URL |
|---------|----------|----------|-----|
| **Frontend Apps** | `testuser` | `testpassword` | JavaFX Desktop Apps |
| **Keycloak Admin** | `admin` | `admin` | http://localhost:8080/admin |
### Database Connections
| Database | User | Password | Host | Port |
|----------|------|----------|------|------|
| **jeeeraaah** | `jeeeraaah` | `jeeeraaah` | localhost | 5432 |
| **lib_test** | `lib_test` | `lib_test` | localhost | 5434 |
| **keycloak** | `keycloak` | `keycloak` | localhost | 5433 |
---
## 🗄️ PostgreSQL Databases
### Application Database
```bash
# Main application database
POSTGRES_JEEERAAAH_HOST=localhost
POSTGRES_JEEERAAAH_PORT=5432
POSTGRES_JEEERAAAH_DATABASE=jeeeraaah
POSTGRES_JEEERAAAH_USER=jeeeraaah
POSTGRES_JEEERAAAH_PASSWORD=jeeeraaah
```
**JDBC URL:**
```
jdbc:postgresql://localhost:5432/jeeeraaah
```
**Used by:**
- Backend (OpenLiberty)
- Integration tests
### Library Test Database
```bash
# For library integration tests
POSTGRES_LIB_TEST_HOST=localhost
POSTGRES_LIB_TEST_PORT=5434
POSTGRES_LIB_TEST_DATABASE=lib_test
POSTGRES_LIB_TEST_USER=lib_test
POSTGRES_LIB_TEST_PASSWORD=lib_test
```
**JDBC URL:**
```
jdbc:postgresql://localhost:5434/lib_test
```
**Used by:**
- `root/lib/*/src/test/java` tests
- JPA/Hibernate tests
### Keycloak Database
```bash
# Keycloak persistence
POSTGRES_KEYCLOAK_HOST=localhost
POSTGRES_KEYCLOAK_PORT=5433
POSTGRES_KEYCLOAK_DATABASE=keycloak
POSTGRES_KEYCLOAK_USER=keycloak
POSTGRES_KEYCLOAK_PASSWORD=keycloak
```
**JDBC URL:**
```
jdbc:postgresql://localhost:5433/keycloak
```
**Used by:**
- Keycloak container
- Stores realms, users, roles
⚠️ **Important:** Without this database, Keycloak is not persistent!
---
## 🔑 Keycloak Configuration
### Admin Console
```bash
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PASSWORD=admin
```
**Access:**
- URL: http://localhost:8080/admin
- Realm: Master (for admin)
**Used for:**
- Manual administration
- Automatic realm setup via Admin API
### Application Realm
```bash
KEYCLOAK_REALM=jeeeraaah-realm
```
**Automatically created with:**
- Client: `jeeeraaah-frontend`
- Test user: `testuser`
- Roles: `task-*`, `taskgroup-*`
### Test User
```bash
KEYCLOAK_TEST_USER=testuser
KEYCLOAK_TEST_PASSWORD=testpassword
```
**Used by:**
- All JavaFX applications (DashApp, GanttApp)
- REST API testing
- Automated login in test mode
⚠️ **Important:**
- Admin user (`admin/admin`) works **only in Master Realm**
- Test user (`testuser/testpassword`) works **only in jeeeraaah-realm**
---
## 🚀 How Credentials Are Used
### Frontend Applications
When starting DashApp or GanttApp:
1. Reads credentials from `testing.properties`:
   ```properties
   testing.username=testuser
   testing.password=testpassword
   testing=true
   ```
2. Authenticates with Keycloak
3. Receives JWT token
4. Uses token for all REST API calls
### Backend API
1. Validates JWT tokens from frontend
2. Connects to PostgreSQL using credentials from `.env`
3. Uses MicroProfile Config for credential injection
### Keycloak Setup
Run once to create realm and test user:
```bash
cd ~/develop/github/main/root/lib/keycloak.admin
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```
Creates:
- ✅ Realm: `jeeeraaah-realm`
- ✅ Client: `jeeeraaah-frontend` (public, direct access grants enabled)
- ✅ User: `testuser/testpassword`
- ✅ Roles: `task-read`, `task-create`, `task-update`, `task-delete`
- ✅ Roles: `taskgroup-read`, `taskgroup-create`, `taskgroup-update`, `taskgroup-delete`
---
## 🧪 Testing Credentials
### Manual Token Request
```bash
curl -X POST 'http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=testuser' \
  -d 'password=testpassword' \
  -d 'grant_type=password' \
  -d 'client_id=jeeeraaah-frontend'
```
**Expected:** JSON response with `access_token`
### Database Connection Test
```bash
# Test jeeeraaah database
psql -h localhost -p 5432 -U jeeeraaah -d jeeeraaah
# Test lib_test database
psql -h localhost -p 5434 -U lib_test -d lib_test
# Password for both: same as username
```
---
## 🔧 Troubleshooting
### Authentication fails?
**Check Keycloak is running:**
```bash
docker ps | grep keycloak
```
**Verify realm exists:**
```bash
# Run realm setup
ruu-keycloak-setup
```
**Check credentials:**
- File: `config/shared/docker/.env`
- Format: `KEY=value` (no spaces, no quotes)
### Database connection fails?
**Check PostgreSQL is running:**
```bash
docker ps | grep postgres
```
**Verify credentials match:**
```bash
cat config/shared/docker/.env | grep POSTGRES
```
### Automatic login not working?
**Check testing.properties:**
```bash
cat testing.properties
# Should contain:
# testing.username=testuser
# testing.password=testpassword
# testing=true
```
---
## 🔒 Production Considerations
⚠️ **These are DEVELOPMENT credentials only!**
For production:
1. Use strong, unique passwords
2. Store credentials in secure vault (e.g., HashiCorp Vault)
3. Use environment variables, not files
4. Enable HTTPS/TLS everywhere
5. Rotate credentials regularly
6. Use different credentials per environment
---
## 📚 Related Documentation
- [AUTHENTICATION-CREDENTIALS.md](AUTHENTICATION-CREDENTIALS.md) - Original (archived after merge)
- [CREDENTIALS-OVERVIEW.md](CREDENTIALS-OVERVIEW.md) - Original (archived after merge)
- [KEYCLOAK-ADMIN-CONSOLE.md](KEYCLOAK-ADMIN-CONSOLE.md) - Keycloak administration
- [JWT-TROUBLESHOOTING.md](JWT-TROUBLESHOOTING.md) - JWT/token issues
---
**Last updated:** 2026-02-09  
**Status:** Complete credential reference
