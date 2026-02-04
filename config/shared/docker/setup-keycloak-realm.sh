#!/bin/bash
set -e

echo "════════════════════════════════════════════════════════════════════"
echo "Keycloak Realm Setup Script"
echo "════════════════════════════════════════════════════════════════════"

# Warte bis Keycloak bereit ist
echo "Warte auf Keycloak..."
until docker exec keycloak /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 \
  --realm master \
  --user admin \
  --password admin 2>/dev/null; do
  echo "  Keycloak noch nicht bereit, warte 5 Sekunden..."
  sleep 5
done

echo "✅ Keycloak ist bereit"
echo ""

# Prüfe ob Realm existiert
echo "Prüfe ob Realm 'jeeeraaah-realm' existiert..."
if docker exec keycloak /opt/keycloak/bin/kcadm.sh get realms/jeeeraaah-realm 2>/dev/null; then
  echo "⚠️  Realm 'jeeeraaah-realm' existiert bereits"
  echo "   Lösche und erstelle neu..."
  docker exec keycloak /opt/keycloak/bin/kcadm.sh delete realms/jeeeraaah-realm
fi

# Erstelle Realm
echo "Erstelle Realm 'jeeeraaah-realm'..."
docker exec keycloak /opt/keycloak/bin/kcadm.sh create realms \
  -s realm=jeeeraaah-realm \
  -s enabled=true

echo "✅ Realm erstellt"
echo ""

# Erstelle Client
echo "Erstelle Client 'jeeeraaah-frontend'..."
docker exec keycloak /opt/keycloak/bin/kcadm.sh create clients \
  -r jeeeraaah-realm \
  -s clientId=jeeeraaah-frontend \
  -s enabled=true \
  -s publicClient=true \
  -s directAccessGrantsEnabled=true \
  -s 'redirectUris=["http://localhost:*"]' \
  -s 'webOrigins=["*"]'

echo "✅ Client erstellt"
echo ""

# Hole Client UUID für Protocol Mapper
echo "Hole Client UUID..."
CLIENT_UUID=$(docker exec keycloak /opt/keycloak/bin/kcadm.sh get clients \
  -r jeeeraaah-realm \
  -q clientId=jeeeraaah-frontend \
  --fields id \
  --format csv \
  --noquotes | tail -1)

echo "  Client UUID: $CLIENT_UUID"
echo ""

# Erstelle Audience Mapper
echo "Erstelle Audience Mapper..."
docker exec keycloak /opt/keycloak/bin/kcadm.sh create clients/$CLIENT_UUID/protocol-mappers/models \
  -r jeeeraaah-realm \
  -s name=audience-mapper \
  -s protocol=openid-connect \
  -s protocolMapper=oidc-audience-mapper \
  -s 'config."included.custom.audience"=jeeeraaah-backend' \
  -s 'config."access.token.claim"=true' \
  -s 'config."id.token.claim"=false'

echo "✅ Audience Mapper erstellt"
echo ""

# Erstelle Testuser
echo "Erstelle Testuser 'jeeeraaah'..."
docker exec keycloak /opt/keycloak/bin/kcadm.sh create users \
  -r jeeeraaah-realm \
  -s username=jeeeraaah \
  -s enabled=true \
  -s email=jeeeraaah@example.com \
  -s emailVerified=true

# Setze Passwort
USER_ID=$(docker exec keycloak /opt/keycloak/bin/kcadm.sh get users \
  -r jeeeraaah-realm \
  -q username=jeeeraaah \
  --fields id \
  --format csv \
  --noquotes | tail -1)

docker exec keycloak /opt/keycloak/bin/kcadm.sh set-password \
  -r jeeeraaah-realm \
  --username jeeeraaah \
  --new-password jeeeraaah

echo "✅ Testuser erstellt mit Passwort"
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "✅ Keycloak Setup abgeschlossen"
echo "════════════════════════════════════════════════════════════════════"
echo ""
echo "Realm: jeeeraaah-realm"
echo "Client: jeeeraaah-frontend"
echo "  - Audience: jeeeraaah-backend"
echo "Testuser: jeeeraaah / jeeeraaah"
echo ""
echo "Admin Console: http://localhost:8080/admin"
echo "  Admin User: admin / admin"
echo "════════════════════════════════════════════════════════════════════"
