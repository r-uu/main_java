#!/bin/bash
# ==============================================================================
# GanttApp Verifikations-Skript
# ==============================================================================
# Testet, ob alle Fixes funktionieren
# ==============================================================================

set -e

echo "================================================================"
echo "GanttApp Verifikation"
echo "================================================================"
echo ""

# Test 1: Prüfe ob Properties im JAR sind
echo "Test 1: Prüfe Properties im kompilierten JAR..."
JAR_FILE="/home/r-uu/develop/github/main/root/app/jeeeraaah/frontend/ui/fx/target/r-uu.app.jeeeraaah.frontend.ui.fx-0.0.1.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "  ❌ JAR nicht gefunden: $JAR_FILE"
    echo "  Führe zuerst 'mvn clean install' aus!"
    exit 1
fi

# Prüfe config.file.name
CONFIG_LINE=$(unzip -p "$JAR_FILE" META-INF/microprofile-config.properties | grep "config.file.name=" | head -1)
if [[ "$CONFIG_LINE" == *"/home/r-uu/develop/github/main/testing.properties"* ]]; then
    echo "  ✅ config.file.name verwendet absoluten Pfad"
else
    echo "  ❌ config.file.name nicht korrekt: $CONFIG_LINE"
    exit 1
fi

# Prüfe keycloak.test.user
KEYCLOAK_USER=$(unzip -p "$JAR_FILE" META-INF/microprofile-config.properties | grep "keycloak.test.user=" | head -1)
if [[ "$KEYCLOAK_USER" == "keycloak.test.user=test" ]]; then
    echo "  ✅ keycloak.test.user=test gefunden"
else
    echo "  ❌ keycloak.test.user nicht gefunden"
    exit 1
fi

# Prüfe keycloak.test.password
KEYCLOAK_PASS=$(unzip -p "$JAR_FILE" META-INF/microprofile-config.properties | grep "keycloak.test.password=" | head -1)
if [[ "$KEYCLOAK_PASS" == "keycloak.test.password=test" ]]; then
    echo "  ✅ keycloak.test.password=test gefunden"
else
    echo "  ❌ keycloak.test.password nicht gefunden"
    exit 1
fi

echo ""

# Test 2: Prüfe testing.properties Datei
echo "Test 2: Prüfe testing.properties Datei..."
TESTING_PROPS="/home/r-uu/develop/github/main/testing.properties"

if [ ! -f "$TESTING_PROPS" ]; then
    echo "  ❌ testing.properties nicht gefunden: $TESTING_PROPS"
    exit 1
fi

if grep -q "keycloak.test.user=test" "$TESTING_PROPS"; then
    echo "  ✅ testing.properties enthält keycloak.test.user=test"
else
    echo "  ❌ testing.properties fehlt keycloak.test.user"
    exit 1
fi

if grep -q "keycloak.test.password=test" "$TESTING_PROPS"; then
    echo "  ✅ testing.properties enthält keycloak.test.password=test"
else
    echo "  ❌ testing.properties fehlt keycloak.test.password"
    exit 1
fi

echo ""

# Test 3: Prüfe Keycloak Erreichbarkeit
echo "Test 3: Prüfe Keycloak Server..."
KEYCLOAK_URL="http://localhost:8080"

if curl -s -o /dev/null -w "%{http_code}" "$KEYCLOAK_URL" | grep -q "200\|302\|303"; then
    echo "  ✅ Keycloak Server ist erreichbar"
else
    echo "  ⚠️  Keycloak Server nicht erreichbar"
    echo "     Bitte Docker Container starten: cd ~/develop/github/main/config/shared/docker && docker compose up -d"
fi

echo ""

# Test 4: Prüfe Keycloak Login
echo "Test 4: Teste Keycloak Login mit test/test..."
LOGIN_RESPONSE=$(curl -s -X POST 'http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=test' \
  -d 'password=test' \
  -d 'grant_type=password' \
  -d 'client_id=jeeeraaah-frontend' 2>&1)

if [[ "$LOGIN_RESPONSE" == *"access_token"* ]]; then
    echo "  ✅ Keycloak Login erfolgreich (Access Token erhalten)"
else
    echo "  ❌ Keycloak Login fehlgeschlagen"
    echo "  Response: $LOGIN_RESPONSE"
    echo ""
    echo "  Führe Keycloak Realm Setup aus:"
    echo "    cd ~/develop/github/main/root/lib/keycloak.admin"
    echo "    mvn -q exec:java -Dexec.mainClass=\"de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup\""
    exit 1
fi

echo ""
echo "================================================================"
echo "✅ ALLE VERIFIKATIONS-TESTS BESTANDEN!"
echo "================================================================"
echo ""
echo "GanttApp kann jetzt gestartet werden:"
echo ""
echo "  cd ~/develop/github/main/root/app/jeeeraaah/frontend/ui/fx"
echo "  mvn exec:java -Dexec.mainClass=\"de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.GanttAppRunner\""
echo ""
echo "Erwartetes Ergebnis:"
echo "  ✅ Configuration properties validated successfully"
echo "  ✅ Automatic login successful"
echo ""

