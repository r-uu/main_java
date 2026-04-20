# Keycloak Admin Console - Anleitung

## 🔐 Anmeldung in der Keycloak Admin Console

### Schritt 1: Keycloak Container prüfen

```bash
docker ps | grep keycloak
```

Der Container muss `healthy` sein!

### Schritt 2: Admin Console URL öffnen

Öffne im Browser:
```
http://localhost:8080/admin
```

Oder für den Master Realm direkt:
```
http://localhost:8080/admin/master/console/
```

### Schritt 3: Anmeldung

**Username**: `admin`  
**Password**: `admin`

> 💡 Diese Credentials kommen aus der `.env` Datei:
> ```
> kc_admin_username=admin
> kc_admin_password=admin
> ```

### Schritt 4: Nach dem Login

Nach erfolgreicher Anmeldung siehst du:
- Links: Realm-Auswahl (master, jeeeraaah-realm, etc.)
- Hauptbereich: Dashboard mit Statistiken
- Oben rechts: Dein Admin-User

## 📋 Häufige Aufgaben

### Realm wechseln

1. Klicke oben links auf den aktuellen Realm-Namen (z.B. "master")
2. Wähle den gewünschten Realm aus (z.B. "jeeeraaah-realm")

### User anzeigen

1. Wähle den gewünschten Realm
2. Klicke im Menü links auf "Users"
3. Klicke auf "View all users" oder suche nach einem User

### Roles anzeigen

1. Wähle den gewünschten Realm
2. Klicke im Menü links auf "Roles"
3. Wähle zwischen "Realm roles" oder "Client roles"

### Clients anzeigen

1. Wähle den gewünschten Realm
2. Klicke im Menü links auf "Clients"
3. Suche nach dem gewünschten Client (z.B. "jeeeraaah-frontend")

## 🚨 Troubleshooting

### Login schlägt fehl

**Problem**: "Invalid username or password"

**Lösung**:
1. Prüfe die Keycloak Container Logs:
   ```bash
   docker logs keycloak | tail -50
   ```

2. Prüfe die .env Datei:
   ```bash
   cat ~/develop/github/java/main/config/shared/docker/.env | grep kc_admin
   ```

3. Falls Credentials nicht stimmen, Container neu erstellen:
   ```bash
   cd ~/develop/github/java/main/config/shared/docker
   docker compose down keycloak
   docker volume rm docker_keycloak-data
   docker compose up -d keycloak
   ```

### Admin Console lädt nicht

**Problem**: Browser zeigt "Connection refused" oder "ERR_CONNECTION_REFUSED"

**Lösung**:
1. Prüfe, ob Keycloak läuft:
   ```bash
   docker ps | grep keycloak
   ```

2. Prüfe, ob Port 8080 offen ist:
   ```bash
   curl http://localhost:8080/health
   ```

3. Warte bis Container healthy ist:
   ```bash
   # Dieser Befehl zeigt den Health-Status
   docker ps --format "table {{.Names}}\t{{.Status}}"
   ```

## 📚 Weiterführende Links

- [Keycloak Admin Console Guide](https://www.keycloak.org/docs/latest/server_admin/#admin-console)
- [Managing Users](https://www.keycloak.org/docs/latest/server_admin/#assembly-managing-users_server_administration_guide)
- [Managing Realms](https://www.keycloak.org/docs/latest/server_admin/#configuring-realms)
