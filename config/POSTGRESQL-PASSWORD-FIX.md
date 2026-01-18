# 🔧 PostgreSQL Passwort-Problem behoben

**Fehler:** `password authentication failed for user "<username>"`

---

## ✅ LÖSUNG

PostgreSQL-Benutzer mit Passwort aus `config.properties` erstellen/aktualisieren:

```bash
# 1. PostgreSQL-Container starten
cd /home/r-uu/develop/github/main/config/shared/docker
docker compose up -d postgres-jeeeraaah

# 2. Credentials aus config.properties verwenden
# Ersetze <USERNAME> und <PASSWORD> mit Werten aus config.properties
docker exec postgres-jeeeraaah psql -U postgres -c "
DROP USER IF EXISTS <USERNAME>;
CREATE USER <USERNAME> WITH PASSWORD '<PASSWORD>';
ALTER USER <USERNAME> CREATEDB;
"

# 3. Datenbank erstellen
docker exec postgres-jeeeraaah psql -U postgres -c "
DROP DATABASE IF EXISTS jeeeraaah;
CREATE DATABASE jeeeraaah OWNER <USERNAME>;
GRANT ALL PRIVILEGES ON DATABASE jeeeraaah TO <USERNAME>;
"

# 4. Schema-Rechte setzen
docker exec postgres-jeeeraaah psql -U postgres -d jeeeraaah -c "
GRANT ALL ON SCHEMA public TO <USERNAME>;
"
```

**Wichtig:** Verwende die Werte aus `config.properties` (nicht in Git!)

---

## 🧪 TESTEN

```bash
# Test 1: Verbindung testen (Credentials aus config.properties)
docker exec postgres-jeeeraaah psql -U <USERNAME> -d jeeeraaah -c "SELECT current_user, current_database();"

# Test 2: Maven-Tests
cd /home/r-uu/develop/github/main/root
mvn test -pl lib/jdbc/postgres
```

**Erwartete Ausgabe:**
```
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

---

## 📋 CREDENTIALS

**config.properties (lokal, nicht in Git!):**
```properties
db.host=localhost
db.port=5432
db.database=jeeeraaah
db.username=<dein-username>
db.password=<dein-password>
```

**Wichtig:** Diese Werte müssen mit PostgreSQL übereinstimmen!  
**Template:** `config.properties.template`

---

## 🔄 ALIAS FÜR SCHNELLE WIEDERHERSTELLUNG

**Hinweis:** Verwende Credentials aus `config.properties`

**In ~/.bashrc oder config/shared/wsl/aliases.sh:**
```bash
# Beispiel - ANPASSEN mit Werten aus config.properties!
alias db-setup='docker exec postgres-jeeeraaah psql -U postgres -c "
DROP USER IF EXISTS <USERNAME>; 
CREATE USER <USERNAME> WITH PASSWORD '\''<PASSWORD>'\''; 
ALTER USER <USERNAME> CREATEDB;
DROP DATABASE IF EXISTS jeeeraaah;
CREATE DATABASE jeeeraaah OWNER <USERNAME>;
GRANT ALL PRIVILEGES ON DATABASE jeeeraaah TO <USERNAME>;" && 
docker exec postgres-jeeeraaah psql -U postgres -d jeeeraaah -c "GRANT ALL ON SCHEMA public TO <USERNAME>;" && 
echo "✅ Database setup complete!"'
```

**Wichtig:** Ersetze `<USERNAME>` und `<PASSWORD>` mit Werten aus `config.properties`!

**Nutzung:**
```bash
db-setup
```

---

✅ **Problem behoben - Tests sollten jetzt durchlaufen!**

