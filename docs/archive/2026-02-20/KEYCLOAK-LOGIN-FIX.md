# Keycloak Login Fix - README

## Problem
Die GanttApp zeigt beim Start folgende Fehlermeldung:
```
ERROR Keycloak authentication failed with status 401: {"error":"invalid_grant","error_description":"Invalid user credentials"}
```

## Ursache
Der Test-User `test/test` wurde zwar in Keycloak angelegt, aber die Anwendung verwendet möglicherweise eine alte, gecachte Version der Konfiguration.

## Lösung

### 1. Keycloak Realm Setup ausführen (bereits erledigt ✅)
Der Test-User wurde erfolgreich angelegt mit:
```bash
cd ~/develop/github/main/root/lib/keycloak.admin
mvn -q exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```

Ergebnis:
- ✅ User 'test' exists (ID: 1175fedd-053c-423b-8d0b-e359f714868e)
- ✅ Password set for user 'test'
- ✅ User 'test' updated (Required Actions deleted)
- ✅ All roles assigned

### 2. Login manuell testen (bereits erfolgreich getestet ✅)
```bash
curl -X POST 'http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=test' \
  -d 'password=test' \
  -d 'grant_type=password' \
  -d 'client_id=jeeeraaah-frontend'
```

Ergebnis: ✅ Access Token erfolgreich erhalten

### 3. Frontend UI FX Modul neu bauen
```bash
cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn clean install
```

### 4. GanttApp starten
```bash
cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn exec:java -Dexec.mainClass="de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.GanttAppRunner"
```

## Konfigurationsdateien

Die folgenden Dateien enthalten die korrekten Test-Credentials:

### 1. microprofile-config.properties (im frontend.ui.fx Modul)
**Pfad:** `root/app/jeeeraaah/frontend/ui/fx/src/main/resources/META-INF/microprofile-config.properties`

```properties
# Keycloak Test User (for automatic login in testing mode)
# IMPORTANT: These credentials must match the user created in the jeeeraaah-realm
# User/Password: test/test
keycloak.test.user=test
keycloak.test.password=test
```

### 2. testing.properties (im Root-Verzeichnis)
**Pfad:** `testing.properties`

```properties
# Keycloak Test User (for automatic login in testing mode)
# IMPORTANT: These credentials must match the user created in the jeeeraaah-realm
# User/Password: test/test
keycloak.test.user=test
keycloak.test.password=test
```

## Verifikation

Nach dem Neustart sollte die App folgende Meldungen zeigen:
```
✅ Configuration properties validated successfully
=== Testing mode enabled - attempting automatic login ===
  Test credentials found: username=test
  ✅ Automatic login successful
```

## Troubleshooting

Falls der Login weiterhin fehlschlägt:

1. **Prüfe Keycloak-Container:**
   ```bash
   docker ps | grep keycloak
   ```

2. **Prüfe Keycloak-Logs:**
   ```bash
   docker logs keycloak
   ```

3. **Realm-Setup erneut ausführen:**
   ```bash
   cd ~/develop/github/main/root/lib/keycloak.admin
   mvn -q exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
   ```

4. **Maven-Cache leeren:**
   ```bash
   cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
   mvn clean
   rm -rf target
   mvn install
   ```

5. **Prüfe, ob Properties geladen werden:**
   Füge temporär diese Zeile in `BaseAuthenticatedApp.performTestingModeLogin()` ein:
   ```java
   System.out.println("DEBUG: testUsername=" + testUsername + ", testPassword=" + testPassword);
   ```

## Backend POM Problem Fix (ZUSÄTZLICH)

Falls beim Build folgende Warnung erscheint:
```
[WARNING] The POM for r-uu:r-uu.app.jeeeraaah.backend.persistence.jpa:jar:0.0.1 is invalid
[ERROR] 'dependencies.dependency.version' for r-uu:r-uu.app.jeeeraaah.common.api.mapping:jar is missing
```

**Lösung - Ausführbares Skript:**
```bash
cd ~/develop/github/main
chmod +x fix-backend-pom.sh
./fix-backend-pom.sh
```

**Oder manuell:**
```bash
# 1. BOM neu installieren
cd ~/develop/github/main/bom
mvn clean install

# 2. Mapping Modul neu installieren
cd ~/develop/github/main/root/app/jeeeraaah/common/api/mapping
mvn clean install

# 3. Backend persistence.jpa testen
cd ~/develop/github/main/root/app/jeeeraaah/backend/persistence/jpa
mvn clean compile
```

## Zusammenfassung

✅ **Bereits erledigt:**
- Keycloak Realm Setup ausgeführt
- Test-User `test/test` angelegt und konfiguriert
- Login manuell via curl erfolgreich getestet
- Konfigurationsdateien aktualisiert
- Fix-Skript für Backend POM Problem erstellt (`fix-backend-pom.sh`)

⏳ **Noch zu tun:**
1. Backend POM Problem beheben (falls Warnung erscheint): `./fix-backend-pom.sh`
2. Frontend UI FX Modul neu bauen: `mvn clean install`
3. GanttApp starten und testen

**Erwartetes Ergebnis:** Die App startet ohne Login-Fehler! 🎉

