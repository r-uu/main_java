# 🔄 Docker-Reset mit Keycloak-Realm Backup

**Anleitung für kompletten Docker-Reset unter Beibehaltung des Keycloak-Realms**

---

## 🎯 ÜBERSICHT

Dieses Skript führt einen kompletten Reset aller Docker-Container durch:
- ✅ Stoppt alle Container
- ✅ Löscht alle Volumes (Datenbanken werden geleert!)
- ✅ **Sichert Keycloak-Realm vor dem Löschen**
- ✅ Startet Container neu
- ✅ Anleitung zum Wiederherstellen des Realms

---

## ⚠️ WICHTIG

**Was wird gelöscht:**
- Alle PostgreSQL-Datenbanken (jeeeraaah, keycloak)
- Alle Container
- Alle Volumes

**Was bleibt erhalten:**
- Keycloak-Realm (als Backup in `config/local/keycloak-backup/`)
- Docker-Images
- Konfigurationsdateien

---

## 🚀 VERWENDUNG

### Automatischer Reset mit Backup:
```bash
cd /home/r-uu/develop/github/main/config/shared/docker
./docker-reset-with-keycloak-backup.sh
```

### Manueller Reset:

#### 1. Keycloak-Realm exportieren (optional)
```bash
# Falls Keycloak läuft und Realm vorhanden
docker exec ruu-keycloak /opt/keycloak/bin/kc.sh export \
    --dir /tmp/keycloak-export \
    --realm jeeeraaah \
    --users realm_file

# Backup lokal speichern
docker cp ruu-keycloak:/tmp/keycloak-export ./keycloak-backup
```

#### 2. Container stoppen und löschen
```bash
cd /home/r-uu/develop/github/main/config/shared/docker
docker compose down
```

#### 3. Volumes löschen
```bash
# Alle Daten werden gelöscht!
docker volume rm postgres-jeeeraaah-data postgres-jeeeraaah-backups
docker volume rm postgres-keycloak-data postgres-keycloak-backups
```

#### 4. Container neu starten
```bash
docker compose up -d
```

#### 5. Keycloak-Realm wiederherstellen
```bash
# Warte bis Keycloak bereit ist
docker exec ruu-keycloak curl -sf http://localhost:8080/health/ready

# Kopiere Backup in Container
docker cp ./keycloak-backup ruu-keycloak:/tmp/keycloak-import

# Import via Admin Console:
# http://localhost:8080/admin → Realm → Create Realm → Import
```

---

## 📋 AKTUELLE CONTAINER

Nach dem Reset sollten folgende Container laufen:

| Name | Image | Port | Zweck |
|------|-------|------|-------|
| `postgres-jeeeraaah` | postgres:16-alpine | 5432 | JEEERAaH DB |
| `postgres-keycloak` | postgres:16-alpine | 5433 | Keycloak DB |
| **`ruu-keycloak`** | keycloak:latest | 8080 | **Ein Keycloak (nicht zwei!)** |
| `jasperreports-service` | custom | 8090 | JasperReports |

**Wichtig:** Es gibt nur **einen** Keycloak-Container: `ruu-keycloak`

---

## ✅ VERIFIKATION

Nach dem Reset prüfen:

```bash
# Container-Status
docker compose ps

# Erwartete Ausgabe:
# NAME                   STATUS         PORTS
# postgres-jeeeraaah     Up (healthy)   0.0.0.0:5432->5432/tcp
# postgres-keycloak      Up (healthy)   0.0.0.0:5433->5432/tcp
# ruu-keycloak           Up (healthy)   0.0.0.0:8080->8080/tcp
# jasperreports-service  Up (healthy)   0.0.0.0:8090->8090/tcp

# Health-Checks
docker exec postgres-jeeeraaah pg_isready
docker exec postgres-keycloak pg_isready
curl -f http://localhost:8080/health/ready
curl -f http://localhost:8090/health
```

---

## 🔧 KEYCLOAK-REALM WIEDERHERSTELLEN

### Option A: Admin Console (empfohlen)
1. Öffne: http://localhost:8080/admin
2. Login: `admin` / `admin`
3. Klicke: **Realm** (Dropdown oben links)
4. Klicke: **Create Realm**
5. Tab: **Import**
6. Wähle: Datei aus Backup
7. Klicke: **Create**

### Option B: CLI
```bash
docker exec ruu-keycloak /opt/keycloak/bin/kc.sh import \
    --dir /tmp/keycloak-import \
    --override true
```

---

## 📝 ALTE CONTAINER-NAMEN (VERALTET)

Falls in Dokumentation erwähnt - **IGNORIEREN:**
- ❌ `keycloak-jeeeraaah` - Gibt es nicht mehr
- ❌ `ruu-postgres` - Durch zwei Container ersetzt
- ✅ `ruu-keycloak` - **Aktueller Name**

**Nur ein Keycloak-Container:** `ruu-keycloak`

---

## 🗑️ ALTE VOLUMES ENTFERNEN

Falls alte Volumes von früheren Setups existieren:

```bash
# Liste alte Volumes
docker volume ls | grep -E "ruu-postgres|keycloak-jeeeraaah"

# Entfernen
docker volume rm ruu-postgres-data ruu-postgres-backups 2>/dev/null || true
```

---

## 📚 SIEHE AUCH

- `docker-compose.yml` - Aktuelle Container-Konfiguration
- `SEPARATE-POSTGRES-CONTAINER.md` - PostgreSQL-Setup
- `CREDENTIALS-CLEANUP-COMPLETE.md` - Credential-Verwaltung

---

✅ **Ein Keycloak-Container, sauberer Reset, Realm-Backup inklusive!**

