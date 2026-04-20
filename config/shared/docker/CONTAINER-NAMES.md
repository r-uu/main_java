# Docker Container Namen - Definitiv

**Stand: 2026-01-25**

## ✅ Korrekte Container-Namen

Die Container-Namen sind in `docker-compose.yml` eindeutig definiert:

| Service           | Container Name         | Port(s)      | Zweck                                    |
|-------------------|------------------------|--------------|------------------------------------------|
| postgres-jeeeraaah| `postgres-jeeeraaah`   | 5432         | PostgreSQL für JEEERAaH + lib_test      |
| postgres-keycloak | `postgres-keycloak`    | 5433         | PostgreSQL für Keycloak                  |
| keycloak          | `keycloak`             | 8080         | Keycloak Identity Management             |
| jasperreports     | `jasperreports`        | 8090         | JasperReports Service                    |

## ❌ Falsche/Alte Namen

Diese Namen existieren NICHT MEHR und dürfen nirgends verwendet werden:

- ❌ `keycloak-jeeeraaah` (alter Name, jetzt: `keycloak`)
- ❌ `ruu-keycloak` (alter Name, jetzt: `keycloak`)
- ❌ `ruu-postgres` (alter Name, jetzt: `postgres-jeeeraaah` oder `postgres-keycloak`)
- ❌ `jasperreports-service` (alter Name, jetzt: `jasperreports`)

## 🔍 Überprüfung

```bash
# Zeige alle Container
docker ps

# Sollte genau diese Container zeigen:
# - keycloak
# - postgres-jeeeraaah
# - postgres-keycloak
# - jasperreports
```

## 🛠️ Bei Problemen

Wenn alte Container-Namen auftauchen:

1. **Stoppe alle Container:**
   ```bash
   cd ~/develop/github/java/main/config/shared/docker
   docker compose down
   ```

2. **Entferne alte Container:**
   ```bash
   docker stop keycloak-jeeeraaah ruu-keycloak jasperreports-service 2>/dev/null || true
   docker rm keycloak-jeeeraaah ruu-keycloak jasperreports-service 2>/dev/null || true
   ```

3. **Starte neu:**
   ```bash
   docker compose up -d
   ```

4. **Prüfe:**
   ```bash
   ./check-status.sh
   ```

## 📝 Dokumentation aktualisieren

Falls in Skripten, Java-Code oder Dokumentation alte Namen gefunden werden:

1. **In Skripten (*.sh):** Ersetze durch korrekte Namen
2. **In Java-Code:** Health-Checks verwenden bereits korrekte Namen
3. **In Dokumentation (*.md):** Aktualisiere auf korrekte Namen

## 🎯 Wichtig

Die Container-Namen sind der **Single Point of Truth** in `docker-compose.yml`.
Alle anderen Konfigurationen müssen zu dieser Datei passen!
