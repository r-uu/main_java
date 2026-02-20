# Schnellstart - GanttApp Fix

## Zwei Probleme, eine Lösung

### 1. Backend POM Problem
```
[ERROR] 'dependencies.dependency.version' for r-uu:r-uu.app.jeeeraaah.common.api.mapping:jar is missing
```

**Fix:**
```bash
cd ~/develop/github/main
chmod +x fix-backend-pom.sh
./fix-backend-pom.sh
```

### 2. Keycloak Login Problem
```
ERROR Keycloak authentication failed with status 401: Invalid user credentials
```

**Fix:**
```bash
cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn clean install
```

## Komplette Lösung (alle Schritte)

```bash
# 1. Backend POM Fix
cd ~/develop/github/main
chmod +x fix-backend-pom.sh
./fix-backend-pom.sh

# 2. Frontend neu bauen (für Keycloak Fix)
cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn clean install

# 3. GanttApp starten
mvn exec:java -Dexec.mainClass="de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.GanttAppRunner"
```

## Erwartetes Ergebnis

```
✅ Configuration properties validated successfully
=== Testing mode enabled - attempting automatic login ===
  Test credentials found: username=test
  ✅ Automatic login successful
```

## Details

Siehe:
- `KEYCLOAK-LOGIN-FIX.md` - Ausführliche Anleitung für Login-Problem
- `fix-backend-pom.sh` - Automatisches Fix-Skript für Backend POM

## Schnelltest

Nach dem Fix sollte folgendes funktionieren:

```bash
# Test 1: Backend Build
cd ~/develop/github/main/root/app/jeeeraaah/backend/persistence/jpa
mvn clean compile
# Erwartung: BUILD SUCCESS ohne Warnung

# Test 2: Keycloak Login
curl -X POST 'http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=test' \
  -d 'password=test' \
  -d 'grant_type=password' \
  -d 'client_id=jeeeraaah-frontend'
# Erwartung: Access Token erhalten

# Test 3: GanttApp Start
cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx
mvn exec:java -Dexec.mainClass="de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.GanttAppRunner"
# Erwartung: App startet ohne Fehler
```

