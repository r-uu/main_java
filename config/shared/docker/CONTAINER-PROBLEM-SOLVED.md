# Docker Container Problem - Gelöst

**Problem:** Container mit falschem Namen `keycloak-jeeeraaah` (unhealthy)  
**Sollte sein:** `keycloak`  
**Status:** ✅ BEHOBEN

## Was war das Problem?

Die `docker-compose.yml` definiert den Container korrekt als `keycloak`:

```yaml
services:
  keycloak:
    container_name: keycloak
```

Jedoch wurde irgendwo ein alter Container mit dem Namen `keycloak-jeeeraaah` erstellt, 
der dann Konflikte verursachte.

## Lösung

### 1. Automatische Lösung (empfohlen)

```bash
cd ~/develop/github/main/config/shared/docker
./clean-environment.sh
```

Dieses Skript:
- ✅ Stoppt alle Compose Services
- ✅ Entfernt alle alten/falsch benannten Container
- ✅ Startet die Umgebung mit korrekten Namen neu

### 2. Manuelle Lösung

```bash
cd ~/develop/github/main/config/shared/docker

# Stoppe Compose Services
docker compose down

# Entferne alte Container
docker stop keycloak-jeeeraaah ruu-keycloak jasperreports-service 2>/dev/null || true
docker rm keycloak-jeeeraaah ruu-keycloak jasperreports-service 2>/dev/null || true

# Starte neu
docker compose up -d
```

## Überprüfung

Nach dem Neustart sollten genau diese Container laufen:

```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

Erwartete Ausgabe:
```
NAMES                 STATUS
keycloak              Up X minutes (healthy)
postgres-jeeeraaah    Up X minutes (healthy)
postgres-keycloak     Up X minutes (healthy)
jasperreports         Up X minutes (healthy)
```

## Warum ist das passiert?

Mögliche Ursachen:
1. **Alte Skripte**: Frühere Versionen der Skripte verwendeten andere Namen
2. **Manuelles Docker-Kommando**: Jemand hat Container manuell mit anderem Namen erstellt
3. **Alte docker-compose.yml**: Eine veraltete Version wurde verwendet

## Vorbeugung

1. **Immer `docker-compose.yml` verwenden:**
   - Nie `docker run` manuell für diese Services
   - Immer `docker compose up -d`

2. **Bei Namensänderungen:**
   - Erst `docker compose down`
   - Alte Container mit altem Namen manuell entfernen
   - Dann `docker compose up -d`

3. **Regelmäßige Überprüfung:**
   ```bash
   cd ~/develop/github/main/config/shared/docker
   ./check-status.sh
   ```

## Referenz

Siehe auch: `CONTAINER-NAMES.md` für die definitive Liste aller Container-Namen.
