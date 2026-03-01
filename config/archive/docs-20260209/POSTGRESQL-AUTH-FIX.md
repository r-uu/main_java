 bedeutet # PostgreSQL Authentication Fix - lib_test User

## Problem
```
[ERROR] FATAL: password authentication failed for user "lib_test"
```

## Ursache
Die PostgreSQL-Datenbank wurde nicht korrekt initialisiert oder die Init-Scripts wurden nicht ausgeführt.

## Lösung

### Schritt 1: Docker-Umgebung komplett neu aufsetzen

```bash
cd ~/develop/github/main/config/shared/docker

# Alle Container und Volumes entfernen
docker compose down -v

# Alte Container mit veralteten Namen entfernen
docker rm -f postgres-jeeeraaah postgres-lib-test postgres-keycloak keycloak-jeeeraaah 2>/dev/null || true

# PostgreSQL starten (Init-Scripts werden automatisch ausgeführt)
docker compose up -d postgres

# Warten bis PostgreSQL healthy ist (30 Sekunden)
sleep 30
```

### Schritt 2: Datenbanken verifizieren

```bash
# Alle Datenbanken anzeigen
docker exec postgres psql -U postgres -c "\l"

# Sollte zeigen:
# - jeeeraaah
# - lib_test
# - keycloak
```

### Schritt 3: User verifizieren

```bash
# lib_test User testen
docker exec postgres psql -U lib_test -d lib_test -c "SELECT current_user;"

# Sollte zeigen: lib_test
```

### Schritt 4: Tests ausführen

```bash
cd ~/develop/github/main/root
mvn clean test -pl lib/jpa/se_hibernate_postgres_demo
```

## Was die Init-Scripts tun

Die Datei `config/shared/docker/initdb/02-init-lib_test.sql` wird **automatisch** beim ersten Start des PostgreSQL-Containers ausgeführt:

```sql
-- Erstellt Datenbank
CREATE DATABASE lib_test;

-- Erstellt User mit Password
CREATE USER lib_test WITH PASSWORD 'lib_test';

-- Gibt Berechtigungen
GRANT ALL PRIVILEGES ON DATABASE lib_test TO lib_test;
```

## Wichtig!

- Init-Scripts werden **NUR beim ersten Start** ausgeführt
- Wenn der Container mit `-v` entfernt wird, werden die Volumes gelöscht
- Beim nächsten Start werden die Scripts wieder ausgeführt
- **Niemals** manuell Datenbanken in einem bereits laufenden Container erstellen!

## Debugging

Wenn es weiterhin nicht funktioniert:

```bash
# Container-Logs prüfen
docker logs postgres

# Prüfen ob Init-Scripts ausgeführt wurden
docker logs postgres 2>&1 | grep -i "init"

# In den Container einloggen und manuell prüfen
docker exec -it postgres bash
psql -U postgres
\l  -- Liste alle Datenbanken
\du -- Liste alle User
```

## Häufige Fehler

### Fehler 1: Init-Scripts wurden nicht ausgeführt
**Symptom:** Datenbank existiert nicht  
**Lösung:** `docker compose down -v && docker compose up -d postgres`

### Fehler 2: Alter Container läuft noch
**Symptom:** Container startet mit alten Daten  
**Lösung:** `docker ps -a | grep postgres` und alte Container mit `docker rm -f <id>` entfernen

### Fehler 3: Volume-Rechte
**Symptom:** Permission denied beim Schreiben  
**Lösung:** Volumes komplett entfernen mit `docker volume rm $(docker volume ls -q | grep postgres)`

## Automatisierung

Das `full-reset.sh` Script macht alles automatisch:

```bash
cd ~/develop/github/main/config/shared/docker
chmod +x full-reset.sh
./full-reset.sh
```

Es führt aus:
1. Container stoppen und Volumes entfernen
2. PostgreSQL starten und auf healthy warten
3. Datenbanken verifizieren
4. Keycloak starten und Realm erstellen

## Credentials - Single Point of Truth

Alle Credentials sind in `testing.properties` definiert (nicht in Git versioniert):

```properties
db.lib_test.host=localhost
db.lib_test.port=5432
db.lib_test.name=lib_test
db.lib_test.username=<siehe testing.properties>
db.lib_test.password=<siehe testing.properties>
db.lib_test.url=jdbc:postgresql://localhost:5432/lib_test
```

Diese Properties werden von MicroProfile Config automatisch geladen!

## Status: ✅ RESOLVED

Nach Ausführung von `docker compose down -v && docker compose up -d` sollten die Tests erfolgreich durchlaufen.
