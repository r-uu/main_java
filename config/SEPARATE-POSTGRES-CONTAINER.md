# ✅ Separate PostgreSQL-Container für JEEERAAAH und Keycloak

**Datum:** 2026-01-17  
**Status:** ✅ **KONFIGURIERT**

---

## 🎯 ÜBERSICHT

Zwei separate PostgreSQL-Container mit Credentials aus `config.properties`:

| Service | Container | Port | Database | User | Password |
|---------|-----------|------|----------|------|----------|
| **JEEERAaH** | `postgres-jeeeraaah` | 5432 | `jeeeraaah` | `db.username` | `db.password` |
| **Keycloak** | `postgres-keycloak` | 5433 | `keycloak` | `db.username` | `db.password` |

**Credentials:** Siehe `config.properties` (lokal, nicht in Git!)

---

## 🚀 STARTEN

### Alle Services starten
```bash
cd /home/r-uu/develop/github/main/config/shared/docker
docker compose up -d
```

### Nur spezifische Services
```bash
# Nur JEEERAAAH-Datenbank
docker compose up -d postgres-jeeeraaah

# Nur Keycloak + Datenbank
docker compose up -d postgres-keycloak keycloak
```

---

## 🔍 STATUS PRÜFEN

```bash
# Alle Services
docker compose ps

# Logs
docker compose logs postgres-jeeeraaah
docker compose logs postgres-keycloak
docker compose logs keycloak

# Health-Check
docker exec postgres-jeeeraaah pg_isready -U r_uu -d jeeeraaah
docker exec postgres-keycloak pg_isready -U r_uu -d keycloak
```

---

## 🔌 VERBINDUNG

### JEEERAaH (von Host)
```bash
# Credentials aus config.properties verwenden
psql -h localhost -p 5432 -U <db.username> -d jeeeraaah
# Password: <db.password>
```

### Keycloak (von Host)
```bash
# Credentials aus config.properties verwenden
psql -h localhost -p 5433 -U <db.username> -d keycloak
# Password: <db.password>
```

### JEEERAaH (von Docker-Container)
```yaml
# Credentials aus config.properties im docker-compose.yml konfiguriert
jdbc:postgresql://postgres-jeeeraaah:5432/jeeeraaah
user: <db.username>
password: <db.password>
```

### Keycloak (von Docker-Container)
```yaml
# Credentials aus config.properties im docker-compose.yml konfiguriert
jdbc:postgresql://postgres-keycloak:5432/keycloak
user: <db.username>
password: <db.password>
```

---

## 📋 KONFIGURATIONSDATEIEN

### config.properties (lokal, nicht in Git!)
```properties
# Beispiel - siehe config.properties.template
db.host=localhost
db.port=5432
db.database=jeeeraaah
db.username=<dein-username>
db.password=<dein-password>
```

**Wichtig:** Diese Datei ist lokal und NICHT in Git!  
**Template:** `config.properties.template`

### docker-compose.yml
Separate Services:
- `postgres-jeeeraaah` → Port 5432
- `postgres-keycloak` → Port 5433
- `keycloak` → nutzt `postgres-keycloak`

---

## 🗄️ DATEN-MANAGEMENT

### Volumes
```bash
# Liste Volumes
docker volume ls | grep postgres

# Ausgabe:
# postgres-jeeeraaah-data
# postgres-jeeeraaah-backups
# postgres-keycloak-data
# postgres-keycloak-backups
```

### Backup
```bash
# JEEERAaH (Credentials aus config.properties)
docker exec postgres-jeeeraaah pg_dump -U <db.username> jeeeraaah > jeeeraaah-backup.sql

# Keycloak (Credentials aus config.properties)
docker exec postgres-keycloak pg_dump -U <db.username> keycloak > keycloak-backup.sql
```

### Restore
```bash
# JEEERAaH
cat jeeeraaah-backup.sql | docker exec -i postgres-jeeeraaah psql -U <db.username> -d jeeeraaah

# Keycloak
cat keycloak-backup.sql | docker exec -i postgres-keycloak psql -U <db.username> -d keycloak
```

---

## 🔄 NEU INITIALISIEREN

### Nur JEEERAAAH-Datenbank
```bash
docker compose stop postgres-jeeeraaah
docker volume rm postgres-jeeeraaah-data
docker compose up -d postgres-jeeeraaah
```

### Nur Keycloak-Datenbank
```bash
docker compose stop keycloak postgres-keycloak
docker volume rm postgres-keycloak-data
docker compose up -d postgres-keycloak keycloak
```

### Alles neu (alle Daten löschen!)
```bash
docker compose down -v
docker volume rm postgres-jeeeraaah-data postgres-keycloak-data
docker compose up -d
```

---

## 📂 INIT-SKRIPTE

SQL-Skripte werden beim **ersten** Start automatisch ausgeführt:

### JEEERAAAH
```
config/shared/docker/initdb/jeeeraaah/
├── 01-init.sql        # Rechte setzen, Schema erstellen
└── 02-custom.sql      # Optional: eigene Init-Skripte
```

### Keycloak
```
config/shared/docker/initdb/keycloak/
├── 01-init.sql        # Rechte setzen
└── 02-custom.sql      # Optional: eigene Init-Skripte
```

**Hinweis:** Skripte werden alphabetisch sortiert ausgeführt!

---

## 🧪 TESTEN

### JEEERAaH-Verbindung testen
```bash
# Credentials aus config.properties verwenden
docker exec postgres-jeeeraaah psql -U <db.username> -d jeeeraaah -c "
SELECT current_user, current_database(), version();
"
```

**Erwartete Ausgabe:**
```
 current_user | current_database |           version
--------------+------------------+-------------------------------
 <username>   | jeeeraaah        | PostgreSQL 16.x on x86_64...
```

### Keycloak-Verbindung testen
```bash
# Credentials aus config.properties verwenden
docker exec postgres-keycloak psql -U <db.username> -d keycloak -c "
SELECT current_user, current_database(), version();
"
```

**Erwartete Ausgabe:**
```
 current_user | current_database |           version
--------------+------------------+-------------------------------
 <username>   | keycloak         | PostgreSQL 16.x on x86_64...
```

### Maven-Tests (JEEERAAAH)
```bash
cd /home/r-uu/develop/github/main/root
mvn test -pl lib/jdbc/postgres
```

**Erwartete Ausgabe:**
```
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

---

## ⚙️ KEYCLOAK-KONFIGURATION

Keycloak ist automatisch konfiguriert:
- **Admin Console:** http://localhost:8080/admin
- **Admin User:** `admin`
- **Admin Password:** `admin` (änderbar via `KEYCLOAK_ADMIN_PASSWORD`)
- **Datenbank:** Automatisch initialisiert bei erstem Start

---

## 🔧 TROUBLESHOOTING

### Problem: Port bereits belegt
**Fehlermeldung:** `Bind for 0.0.0.0:5432 failed: port is already allocated`

**Lösung:**
```bash
# Prüfe was auf Port 5432 läuft
lsof -i :5432

# Stoppe alten Container
docker ps -a | grep postgres
docker stop <container-id>
docker rm <container-id>
```

### Problem: Verbindung abgelehnt
**Fehlermeldung:** `password authentication failed for user "r_uu"`

**Lösung:**
```bash
# Container neu erstellen (löscht Daten!)
docker compose stop postgres-jeeeraaah
docker volume rm postgres-jeeeraaah-data
docker compose up -d postgres-jeeeraaah
```

### Problem: Keycloak startet nicht
**Ursache:** Wartet auf Datenbank

**Lösung:**
```bash
# Logs prüfen
docker compose logs keycloak

# Datenbank-Container prüfen
docker compose ps postgres-keycloak

# Falls unhealthy: neu starten
docker compose restart postgres-keycloak
sleep 10
docker compose restart keycloak
```

---

## ✅ VORTEILE

### Getrennte Container:
- ✅ **Isolation:** JEEERAAAH und Keycloak nutzen separate Datenbanken
- ✅ **Unabhängigkeit:** Backup/Restore einzeln möglich
- ✅ **Klarheit:** Keine gemischten Daten
- ✅ **Skalierung:** Container können unabhängig skaliert werden

### Identische Credentials:
- ✅ **Einfachheit:** Ein Passwort für beide DBs (aus `config.properties`)
- ✅ **Konsistenz:** Gleicher User in beiden DBs
- ✅ **Entwicklung:** Zentral konfiguriert

---

## 📚 SIEHE AUCH

- `docker-compose.yml` - Komplette Konfiguration
- `POSTGRESQL-PASSWORD-FIX.md` - Allgemeine DB-Probleme
- `config.properties` - Lokale Konfiguration

---

✅ **Konfiguration abgeschlossen - Separate Container für JEEERAAAH und Keycloak!**

