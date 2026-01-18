# 🔄 Migration auf separate PostgreSQL-Container

**Quick-Guide für die Umstellung**

---

## ✅ WAS WURDE GEÄNDERT

**VORHER:**
- Ein Container `ruu-postgres` für alles
- Port 5432
- User/DB variabel (aus Env-Variablen)

**NACHHER:**
- `postgres-jeeeraaah` für JEEERAaH (Port 5432)
- `postgres-keycloak` für Keycloak (Port 5433)
- Credentials aus `config.properties` (lokal, nicht in Git!)

---

## 🚀 UMSTELLUNG

### 1. Alte Container stoppen & entfernen
```bash
cd /home/r-uu/develop/github/main/config/shared/docker

# Alle Container stoppen
docker compose down

# Optional: Alte Volumes löschen (wenn Daten nicht mehr benötigt)
docker volume rm ruu-postgres-data 2>/dev/null || true
```

### 2. Neue Container starten
```bash
# Alle Services neu starten
docker compose up -d

# Logs verfolgen
docker compose logs -f
```

### 3. Prüfen
```bash
# Status
docker compose ps

# Sollte zeigen:
# postgres-jeeeraaah  running (healthy)  0.0.0.0:5432->5432/tcp
# postgres-keycloak   running (healthy)  0.0.0.0:5433->5432/tcp
# ruu-keycloak        running (healthy)  0.0.0.0:8080->8080/tcp
# jasperreports-service running (healthy) 0.0.0.0:8090->8090/tcp
```

### 4. Verbindung testen
```bash
# JEEERAaH-DB (Credentials aus config.properties)
docker exec postgres-jeeeraaah psql -U <USERNAME> -d jeeeraaah -c "SELECT version();"

# Keycloak-DB (Credentials aus config.properties)
docker exec postgres-keycloak psql -U <USERNAME> -d keycloak -c "SELECT version();"
```

**Wichtig:** Ersetze `<USERNAME>` mit Wert aus `config.properties`!

---

## 🔧 KONFIGURATION ANGEPASST

### config.properties
```properties
# GEÄNDERT: Database-Name
db.database=jeeeraaah  # war: lib_test
```

### docker-compose.yml
- ✅ Zwei separate PostgreSQL-Services
- ✅ Separate Volumes
- ✅ Keycloak nutzt `postgres-keycloak`
- ✅ Init-Skripte pro Datenbank

---

## ✅ VORTEILE

1. **Isolation:** JEEERAaH und Keycloak getrennt
2. **Klarheit:** Keine gemischten Daten
3. **Flexibilität:** Unabhängiges Backup/Restore
4. **Skalierung:** Container einzeln skalierbar

---

## 📚 WEITERE INFO

Siehe: `SEPARATE-POSTGRES-CONTAINER.md`

---

✅ **Migration abgeschlossen!**

