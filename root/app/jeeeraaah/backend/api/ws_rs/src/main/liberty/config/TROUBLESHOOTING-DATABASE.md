# PostgreSQL Verbindung testen

## Voraussetzungen prüfen

### 1. PostgreSQL läuft in WSL2
```bash
# PostgreSQL Status prüfen
sudo systemctl status postgresql

# Falls nicht gestartet:
sudo systemctl start postgresql
```

### 2. PostgreSQL lauscht auf localhost
```bash
# PostgreSQL Konfiguration prüfen
sudo grep "^listen_addresses" /etc/postgresql/*/main/postgresql.conf

# Sollte sein:
# listen_addresses = 'localhost' oder '*'
```

### 3. Datenbank und Benutzer existieren
```bash
# Als postgres Benutzer anmelden
sudo -u postgres psql

# In psql:
\l                          # Liste alle Datenbanken
\du                         # Liste alle Benutzer

# Prüfen ob Datenbank 'jeeeraaah' existiert
# Prüfen ob Benutzer 'r_uu' existiert
```

### 4. Verbindung manuell testen
```bash
# Von WSL2 aus testen
psql -h localhost -p 5432 -U r_uu -d jeeeraaah

# Passwort eingeben: r_uu_password
# Bei Erfolg sollten Sie eine psql-Eingabeaufforderung sehen
```

## Fehlerbehebung

### Fehler: "Connection refused"
```bash
# PostgreSQL läuft nicht
sudo systemctl start postgresql

# Oder Port ist falsch
sudo netstat -tlnp | grep postgres
```

### Fehler: "FATAL: password authentication failed"
```bash
# pg_hba.conf prüfen
sudo cat /etc/postgresql/*/main/pg_hba.conf | grep -v "^#"

# Für localhost sollte stehen:
# host    all             all             127.0.0.1/32            md5
# host    all             all             ::1/128                 md5

# Falls geändert, PostgreSQL neu starten:
sudo systemctl restart postgresql
```

### Fehler: "FATAL: database does not exist"
```bash
# Datenbank erstellen
sudo -u postgres createdb -O r_uu jeeeraaah
```

### Fehler: "FATAL: role does not exist"
```bash
# Benutzer erstellen
sudo -u postgres psql -c "CREATE USER r_uu WITH PASSWORD 'r_uu_password';"
sudo -u postgres psql -c "ALTER USER r_uu CREATEDB;"
```

## Liberty mit neuer Konfiguration testen

### 1. Liberty neu starten
```bash
cd /home/r-uu/develop/github/space-02/r-uu/app/jeeeraaah/backend/api/ws_rs

# Liberty stoppen (falls läuft)
mvn liberty:stop

# Liberty neu starten
mvn liberty:run
```

### 2. Logs prüfen
```bash
# In einem anderen Terminal
tail -f target/liberty/wlp/usr/servers/defaultServer/logs/messages.log

# Auf Fehler achten wie:
# - "Connection refused"
# - "password authentication failed"
# - "database does not exist"
```

### 3. Datenbankverbindung im Log verifizieren
Bei erfolgreicher Verbindung sollte im Log stehen:
```
[AUDIT   ] CWWJP0009I: JPA persistence unit XXXX has been bound to java:
[AUDIT   ] CWWKZ0001I: Application jeeeraaah started
```

Ohne Fehler bezüglich der Datenbankverbindung.

## Alternative: Docker PostgreSQL

Falls Sie PostgreSQL in Docker verwenden:

```bash
# PostgreSQL Container starten
docker run --name postgres-jeeeraaah \
  -e POSTGRES_USER=r_uu \
  -e POSTGRES_PASSWORD=r_uu_password \
  -e POSTGRES_DB=jeeeraaah \
  -p 5432:5432 \
  -d postgres:16

# In server.env:
datasource_server_host=localhost  # Port-Mapping macht localhost möglich
datasource_server_port=5432
```

