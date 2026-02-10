# Authentication Credentials Übersicht

**Single Point of Truth**: Alle Credentials sind in `testing.properties` definiert

## Keycloak Realm: jeeeraaah-realm

### Test-Benutzer (für automatisches Login im Testing Mode)

| Property | Wert | Verwendung |
|----------|------|------------|
| `testing.username` | `test` | Test-Benutzer Username |
| `testing.password` | `test` | Test-Benutzer Passwort |
| `testing` | `true` | Aktiviert automatisches Login |

### Keycloak Admin (Master Realm)

| Property | Wert | Verwendung |
|----------|------|------------|
| `keycloak.admin.username` | `admin` | Keycloak Admin Console |
| `keycloak.admin.password` | `admin` | Keycloak Admin API |

**⚠️ WICHTIG**: 
- Der Admin-Benutzer (`admin`) existiert **nur im Master Realm**
- Der Test-Benutzer (`test`) existiert **nur im jeeeraaah-realm**
- Für automatisches Login in der App wird **immer `test/test`** verwendet

## Realm Setup

Der Keycloak Realm wird automatisch erstellt mit:

```bash
cd ~/develop/github/main/root/lib/keycloak.admin
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```

Dies erstellt:
- ✅ Realm: `jeeeraaah-realm`
- ✅ Client: `jeeeraaah-frontend` (Public Client, Direct Access Grants)
- ✅ Test User: `test / test` (mit allen Rollen)
- ✅ Rollen: `task-*`, `taskgroup-*` (read, create, update, delete)

## Test-Login via cURL

```bash
curl -X POST 'http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=test' \
  -d 'password=test' \
  -d 'grant_type=password' \
  -d 'client_id=jeeeraaah-frontend'
```

## Automatisches Login in DashAppRunner

Wenn `testing=true` in `testing.properties`, wird beim Start von `DashAppRunner` automatisch:
1. Test-Credentials aus Config gelesen (`testing.username`, `testing.password`)
2. Login bei Keycloak durchgeführt
3. JWT Token abgerufen und in `KeycloakAuthService` gespeichert
4. Alle REST API Calls verwenden automatisch den Token

Bei Fehler beim automatischen Login:
- ✅ Keycloak läuft: `docker ps | grep keycloak`
- ✅ Realm existiert: `ruu-keycloak-setup`
- ✅ Credentials korrekt: `testing.properties` prüfen

## Historie

- **2026-01-26**: Credentials von `admin/admin` auf `test/test` geändert
  - `admin/admin` funktioniert nur für Master Realm
  - `test/test` ist der korrekte User im `jeeeraaah-realm`
