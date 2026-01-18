# ✅ Docker Container Reset Durchgeführt

**Datum:** 2026-01-17  
**Status:** ✅ **ABGESCHLOSSEN**

---

## 🎯 WAS WURDE GEMACHT

Kompletter Reset aller Docker-Container mit Keycloak-Realm-Backup durchgeführt.

---

## 🔧 DURCHGEFÜHRTE SCHRITTE

### 1. ✅ Keycloak Realm gesichert
- Realm-Export vor Reset durchgeführt
- Backup-Verzeichnis: `config/shared/docker/keycloak-backup/`
- Falls kein Realm vorhanden war, wird mit frischer Konfiguration gestartet

### 2. ✅ Alle Container gestoppt
```bash
docker compose down
```
Gestoppte Container:
- `postgres-jeeeraaah` - JEEERAaH PostgreSQL Datenbank
- `postgres-keycloak` - Keycloak PostgreSQL Datenbank  
- `keycloak-service` - Keycloak Identity & Access Management
- `jasperreports-service` - JasperReports Service

### 3. ✅ Alle Volumes gelöscht
```bash
docker volume rm postgres-jeeeraaah-data
docker volume rm postgres-jeeeraaah-backups
docker volume rm postgres-keycloak-data
docker volume rm postgres-keycloak-backups
```

### 4. ✅ Container neu gestartet
```bash
docker compose up -d
```

### 5. ✅ Keycloak Realm wiederhergestellt
- Import des gesicherten Realms (falls vorhanden)
- Oder: Frische Keycloak-Konfiguration

---

## 📊 AKTUELLER STATUS

### Laufende Container:

| Container | Status | Ports |
|-----------|--------|-------|
| `keycloak-service` | ✅ healthy | 8080 |
| `postgres-jeeeraaah` | ✅ healthy | 5432 |
| `postgres-keycloak` | ✅ healthy | 5433 |
| `jasperreports-service` | ✅ healthy | 8090 |

---

## 🔌 ZUGRIFF AUF SERVICES

### PostgreSQL (JEEERAaH Application)
- **Host:** `localhost`
- **Port:** `5432`
- **Datenbank:** `jeeeraaah`
- **User:** Siehe `config.properties`
- **Passwort:** Siehe `config.properties`

**JDBC URL:**
```
jdbc:postgresql://localhost:5432/jeeeraaah
```

### PostgreSQL (Keycloak)
- **Host:** `localhost`
- **Port:** `5433`
- **Datenbank:** `keycloak`
- **User:** Siehe `config.properties`
- **Passwort:** Siehe `config.properties`

**JDBC URL:**
```
jdbc:postgresql://localhost:5433/keycloak
```

### Keycloak Admin Console
- **URL:** http://localhost:8080/admin
- **Benutzer:** Siehe `config.properties`
- **Passwort:** Siehe `config.properties`

⚠️ **Credentials aus `config.properties`** (lokal, nicht in Git!)

---

## 📝 KEYCLOAK CONTAINER-NAMEN BEREINIGT

### ✅ Korrekte Container-Namen (aktuell):

| Service | Container-Name | Beschreibung |
|---------|---------------|--------------|
| Keycloak | `keycloak-service` | Identity & Access Management |
| PostgreSQL (JEEERAaH) | `postgres-jeeeraaah` | App-Datenbank |
| PostgreSQL (Keycloak) | `postgres-keycloak` | Keycloak-Datenbank |
| JasperReports | `jasperreports-service` | Report Service |

### ❌ VERALTETE Namen (nicht mehr verwenden):

- ~~`keycloak-jeeeraaah`~~ → **JETZT:** `keycloak-service`
- ~~`ruu-keycloak`~~ → **JETZT:** `keycloak-service`

**Hinweis:** In einigen Dokumenten wird noch auf alte Container-Namen verwiesen. Diese sind veraltet!

---

## 🛠️ NÜTZLICHE BEFEHLE

### Container verwalten:
```bash
# Status prüfen
docker compose ps
docker ps

# Logs anzeigen
docker compose logs -f
docker logs -f keycloak-service
docker logs -f postgres-jeeeraaah

# Container neu starten
docker compose restart
docker compose restart keycloak-service

# Container stoppen
docker compose down

# Container mit Volumes löschen
docker compose down -v
```

### PostgreSQL Zugriff:
```bash
# JEEERAaH Datenbank
docker exec -it postgres-jeeeraaah psql -U r_uu -d jeeeraaah

# Keycloak Datenbank
docker exec -it postgres-keycloak psql -U r_uu -d keycloak
```

### Keycloak Management:
```bash
# Keycloak Admin Console URL
echo "http://localhost:8080/admin"

# Keycloak Logs
docker logs -f keycloak-service

# Keycloak neu starten (z.B. nach Änderungen)
docker restart keycloak-service
```

---

## 🔄 RESET WIEDERHOLEN

Falls erneut ein Reset benötigt wird:

```bash
cd /home/r-uu/develop/github/main/config/shared/docker
./reset-all-containers.sh
```

Das Skript:
1. ✅ Sichert automatisch den Keycloak Realm
2. ✅ Stoppt alle Container
3. ✅ Löscht alle Volumes (nach Bestätigung!)
4. ✅ Startet Container neu
5. ✅ Stellt Keycloak Realm wieder her

---

## 📚 DOKUMENTATION

### Haupt-Docker-Konfiguration:
- `config/shared/docker/docker-compose.yml` - Alle Services
- `config/shared/docker/reset-all-containers.sh` - Reset-Skript
- `config/shared/docker/start-docker-services.sh` - Autostart-Skript

### Init-Skripte:
- `config/shared/docker/initdb/jeeeraaah/` - JEEERAaH DB Init
- `config/shared/docker/initdb/keycloak/` - Keycloak DB Init

---

## ✅ CHECKLISTE

- [x] Keycloak Realm gesichert (oder neu)
- [x] Alle Container gestoppt
- [x] Alle Volumes gelöscht
- [x] Container neu gestartet
- [x] Alle Container sind healthy
- [x] Keycloak Admin Console erreichbar
- [x] PostgreSQL Datenbanken erreichbar
- [x] Nur ein Keycloak-Container (`keycloak-service`)
- [x] Veraltete Container-Namen dokumentiert
- [x] Reset-Skript erstellt

---

## 🎯 NÄCHSTE SCHRITTE

### Keycloak konfigurieren:
1. Öffne Admin Console: http://localhost:8080/admin
2. Login: `admin` / `admin`
3. Falls Realm wiederhergestellt wurde: ✅ Fertig!
4. Falls neu: Realm konfigurieren
   - Neuen Realm erstellen
   - Clients einrichten
   - User/Rollen konfigurieren

### JEEERAaH App verbinden:
- JDBC URL: `jdbc:postgresql://localhost:5432/jeeeraaah`
- Credentials aus `config.properties`

---

✅ **Docker Container Reset erfolgreich abgeschlossen!**  
✅ **Nur ein Keycloak-Container aktiv: `keycloak-service`**  
✅ **Alle Services sind healthy und erreichbar!**

