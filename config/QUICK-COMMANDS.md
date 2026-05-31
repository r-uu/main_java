# 📋 TÄGLICHE BEFEHLE - QUICK REFERENCE

**Letzte Aktualisierung:** 2026-01-22

---

## 🚀 PROJEKT STARTEN

### Morgens (einmalig)

```bash
# Docker Services starten + Datenbanken sicherstellen
ruu-startup
```

**Das war's!** 
- Docker Container laufen
- **ALLE** Datenbanken sind automatisch vorhanden (jeeeraaah, lib_test, keycloak)
- Kein manuelles Setup mehr nötig

### Datenbanken manuell prüfen/reparieren

```bash
# Alle Datenbanken sicherstellen (idempotent, kann jederzeit ausgeführt werden)
ruu-postgres-setup
```

---

## 🔨 ENTWICKLUNG

### Projekt bauen

```bash
# Komplettes Projekt
build-all

# Oder klassisch
cd ~/develop/github/java/main/root
mvn clean install
```

### Backend starten

**In IntelliJ:**
- Run Configuration: **"backend"** oder **"liberty:dev"**
- Port: `9080`

**Prüfen:**
```bash
curl http://localhost:9080/health
```

### Frontend starten

**In IntelliJ:**
- Run Configuration: **"HierarchiesAppRunner"**

**Test-Login:**
- Username: `r_uu`
- Password: `r_uu_password`

---

## 🐳 DOCKER

### Container-Status

```bash
ruu-docker-ps

# Oder
docker ps
```

### Container neu starten

```bash
ruu-docker-restart
```

### Logs anschauen

```bash
# Keycloak
docker logs keycloak -f

# PostgreSQL (jeeeraaah)
docker logs postgres-jeeeraaah -f

# PostgreSQL (keycloak)
docker logs postgres-keycloak -f

# JasperReports
docker logs jasperreports -f
```

---

## 🐘 POSTGRESQL

### Datenbanken sicherstellen

```bash
# Alle benötigten Datenbanken automatisch erstellen/prüfen
# (jeeeraaah, lib_test, keycloak)
ruu-postgres-setup
```

**Hinweis:** Dies passiert automatisch bei `ruu-startup`!

### In Datenbank verbinden

```bash
# jeeeraaah DB
docker exec -it postgres-jeeeraaah psql -U r_uu -d jeeeraaah

# lib_test DB
docker exec -it postgres-jeeeraaah psql -U r_uu -d lib_test

# Beenden mit: \q
```

### Datenbanken auflisten

```bash
docker exec -it postgres-jeeeraaah psql -U r_uu -d postgres -c "\l"
```

---

## 🔐 KEYCLOAK

### Realm einrichten

```bash
ruu-keycloak-setup
```

### Admin Console

```
http://localhost:8080/admin
```

**Credentials:**
- Username: `admin`
- Password: `admin`

### Token abrufen

```bash
curl -X POST http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password' \
  -d 'client_id=jeeeraaah-frontend' \
  -d 'username=r_uu' \
  -d 'password=r_uu_password'
```

---

## 📊 JASPERREPORTS

### Service Status

```bash
curl http://localhost:8090/health
```

### Report generieren

```bash
curl -X POST http://localhost:8090/api/report/generate \
  -H "Content-Type: application/json" \
  -d '{
    "template": "invoice.jasper",
    "format": "pdf",
    "data": {
      "invoiceNumber": "2026-001",
      "invoiceDate": "2026-01-22",
      "customerName": "Test GmbH",
      "total": 1190.0
    }
  }' --output rechnung.pdf
```

---

## 🔍 TROUBLESHOOTING

### Build-Fehler

```bash
# Maven Cache leeren
cd ~/develop/github/java/main/root
mvn clean

# Komplett neu bauen
build-all
```

### Docker-Probleme

```bash
# Container neu starten
ruu-docker-restart

# Oder komplett neu
docker compose -f ~/develop/github/java/main/config/shared/docker/docker-compose.yml down
ruu-startup
```

### PostgreSQL-Probleme

```bash
# Datenbanken neu einrichten
ruu-postgres-setup
```

### Keycloak-Probleme

```bash
# Realm neu einrichten
ruu-keycloak-setup
```

---

## 🆘 HILFE

### Aliase anzeigen

```bash
ruu-help
```

### Dokumentation

```bash
# Zentrale Dokumentation
cat ~/develop/github/java/main/config/PROJEKT-DOKUMENTATION.md

# Hauptdokumentation
cat ~/develop/github/java/main/README.md

# Troubleshooting
cat ~/develop/github/java/main/config/TROUBLESHOOTING.md
```

---

## 📝 GIT

### Status

```bash
git status
```

### Commit & Push

```bash
git add .
git commit -m "Beschreibung"
git push
```

**Hinweis:** Git Push funktioniert jetzt automatisch via SSH (konfiguriert).

---

## 🎯 TYPISCHER ARBEITSTAG

```bash
# 1. Morgens
ruu-startup              # Docker starten

# 2. Code ändern...

# 3. Testen
build-all

# 4. Backend starten (IntelliJ)

# 5. Frontend starten (IntelliJ)

# 6. Entwickeln...

# 7. Abends
# → Nichts! Container laufen weiter
```

---

## 📚 WEITERE DOKUMENTATION

- **[README.md](../README.md)** - Hauptdokumentation
- **[config/PROJEKT-DOKUMENTATION.md](PROJEKT-DOKUMENTATION.md)** - Zentrale Referenz
- **[config/TROUBLESHOOTING.md](TROUBLESHOOTING.md)** - Problemlösungen

---

**🎉 Viel Erfolg!**
