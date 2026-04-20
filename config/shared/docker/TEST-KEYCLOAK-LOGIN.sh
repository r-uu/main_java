#!/bin/bash
# =============================================================================
# Test Keycloak Login mit test/test Credentials
# =============================================================================

echo "==================================================================="
echo "🔐 Keycloak Login Test - test/test"
echo "==================================================================="
echo ""

# Keycloak Health Check
echo "1️⃣  Prüfe Keycloak Health Status..."
KEYCLOAK_HEALTH=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/health/ready 2>/dev/null || echo "000")

if [ "$KEYCLOAK_HEALTH" = "200" ]; then
    echo "✅ Keycloak ist bereit (HTTP $KEYCLOAK_HEALTH)"
else
    echo "❌ Keycloak ist nicht bereit (HTTP $KEYCLOAK_HEALTH)"
    echo "   Starte Keycloak mit: docker compose up -d keycloak"
    exit 1
fi
echo ""

# Test Login
echo "2️⃣  Teste Login mit test/test..."
echo "-------------------------------------------------------------------"

RESPONSE=$(curl -s -X POST 'http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=test' \
  -d 'password=test' \
  -d 'grant_type=password' \
  -d 'client_id=jeeeraaah-frontend' 2>&1)

# Prüfe ob Token erhalten
if echo "$RESPONSE" | grep -q "access_token"; then
    echo "✅ Login erfolgreich!"
    echo ""
    echo "Access Token (erste 50 Zeichen):"
    echo "$RESPONSE" | jq -r '.access_token' 2>/dev/null | head -c 50
    echo "..."
    echo ""
    echo "Token Typ:"
    echo "$RESPONSE" | jq -r '.token_type' 2>/dev/null
    echo ""
    echo "Expires In:"
    echo "$RESPONSE" | jq -r '.expires_in' 2>/dev/null
    echo " Sekunden"
elif echo "$RESPONSE" | grep -q "invalid_grant"; then
    echo "❌ Login fehlgeschlagen: Ungültige Credentials"
    echo ""
    echo "Mögliche Ursachen:"
    echo "  1. User 'test' existiert nicht im jeeeraaah-realm"
    echo "  2. Passwort ist falsch"
    echo "  3. Realm 'jeeeraaah-realm' existiert nicht"
    echo ""
    echo "Lösung: Führe Realm Setup aus"
    echo "  cd ~/develop/github/java/main/root/lib/keycloak.admin"
    echo "  mvn exec:java -Dexec.mainClass=\"de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup\""
    exit 1
elif echo "$RESPONSE" | grep -q "error"; then
    echo "❌ Login fehlgeschlagen"
    echo ""
    echo "Server Antwort:"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
    exit 1
else
    echo "❌ Unerwartete Antwort vom Server"
    echo ""
    echo "Antwort:"
    echo "$RESPONSE"
    exit 1
fi

echo ""
echo "==================================================================="
echo "✅ Keycloak Login Test erfolgreich abgeschlossen!"
echo "==================================================================="
echo ""
echo "Credentials funktionieren:"
echo "  Username: test"
echo "  Password: test"
echo "  Realm:    jeeeraaah-realm"
echo "  Client:   jeeeraaah-frontend"
echo ""
echo "Diese Credentials können nun in DashAppRunner verwendet werden!"
echo "==================================================================="
