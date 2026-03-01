# PostgreSQL Initialization Scripts
Dieses Verzeichnis enthält SQL-Skripte für die Initialisierung der PostgreSQL-Datenbanken.
Diese Skripte werden automatisch beim **ersten Start** des PostgreSQL-Containers ausgeführt.
## Übersicht
**Wichtig:** Alle Datenbanken laufen jetzt in einem **einzigen PostgreSQL-Container** namens `postgres` auf Port 5432.
Dies vereinfacht:
- Ressourcennutzung (ein Container statt drei)
- Port-Management (immer 5432)
- Backup/Restore-Verfahren
- Credential-Verwaltung
## Dateistruktur
```
initdb/
├── 01-init-jeeeraaah.sql    # Initialisierung für jeeeraaah Anwendungsdatenbank
├── 02-init-lib_test.sql     # Initialisierung für lib_test Integrationstests
├── 03-init-keycloak.sql     # Initialisierung für keycloak Persistenz
└── README.md                # Diese Datei
```
## Ausführungsreihenfolge
Docker führt alle `.sql`-Dateien in `/docker-entrypoint-initdb.d/` in **alphabetischer Reihenfolge** aus:
1. `01-init-jeeeraaah.sql` → Erstellt Datenbank `jeeeraaah` mit Benutzer `jeeeraaah`
2. `02-init-lib_test.sql` → Erstellt Datenbank `lib_test` mit Benutzer `lib_test`
3. `03-init-keycloak.sql` → Erstellt Datenbank `keycloak` mit Benutzer `keycloak`
## Datenbank-Konfiguration
### Container: postgres
- **Image:** postgres:16-alpine
- **Port:** 5432
- **Superuser:** postgres / postgres (aus .env: `POSTGRES_USER` / `POSTGRES_PASSWORD`)
### Schema: jeeeraaah (Anwendung)
- **Datenbank:** `jeeeraaah`
- **Benutzer:** `jeeeraaah`
- **Passwort:** `jeeeraaah`
- **Zweck:** Hauptdatenbank der Anwendung
- **JDBC URL:** `jdbc:postgresql://localhost:5432/jeeeraaah`
### Schema: lib_test (Tests)
- **Datenbank:** `lib_test`
- **Benutzer:** `lib_test`
- **Passwort:** `lib_test`
- **Zweck:** Integrations- & Unit-Tests
- **JDBC URL:** `jdbc:postgresql://localhost:5432/lib_test`
### Schema: keycloak (Identity Management)
- **Datenbank:** `keycloak`
- **Benutzer:** `keycloak`
- **Passwort:** `keycloak`
- **Zweck:** Keycloak-Persistenzschicht
- **JDBC URL:** `jdbc:postgresql://localhost:5432/keycloak`
## Credentials-Quelle
Alle Credentials sind zentral in `../.env` definiert:
```env
# PostgreSQL Container
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
# Schema: jeeeraaah
POSTGRES_JEEERAAAH_DB=jeeeraaah
POSTGRES_JEEERAAAH_USER=jeeeraaah
POSTGRES_JEEERAAAH_PASSWORD=jeeeraaah
# Schema: lib_test
POSTGRES_LIB_TEST_DB=lib_test
POSTGRES_LIB_TEST_USER=lib_test
POSTGRES_LIB_TEST_PASSWORD=lib_test
# Schema: keycloak
POSTGRES_KEYCLOAK_DB=keycloak
POSTGRES_KEYCLOAK_USER=keycloak
POSTGRES_KEYCLOAK_PASSWORD=keycloak
```
**Konvention:** Benutzer/Passwort entspricht immer dem Datenbanknamen (zur Vereinfachung).
## Neu-Initialisierung
Um die Init-Skripte erneut auszuführen (WARNUNG: löscht alle Daten):
```bash
# Kompletter Reset
cd ~/develop/github/main/config/shared/docker
docker compose down -v
docker compose up -d
# Warten bis Container healthy sind
sleep 30
# Keycloak-Realm neu erstellen
cd ~/develop/github/main/root/lib/keycloak_admin
mvn exec:java -Dexec.mainClass="de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup"
```
Oder verwende den Alias:
```bash
ruu-docker-reset
```
## Troubleshooting
### Problem: "FATAL: database does not exist"
**Ursache:** Container wurde ohne Ausführung der Init-Skripte gestartet (Volume existierte bereits)
**Lösung:**
```bash
docker compose down -v  # Volumes entfernen!
docker compose up -d
```
### Problem: "password authentication failed"
**Ursache:** Credentials-Mismatch zwischen .env und Anwendungs-Konfiguration
**Lösung:**
1. Prüfen ob `.env` korrekte Werte hat
2. Prüfen ob `testing.properties` mit `.env` übereinstimmt
3. Neu bauen: `docker compose down -v && docker compose up -d`
### Problem: Mehrere Datenbanken benötigt
**Antwort:** Kein Problem! Alle drei Datenbanken sind in einem Container:
- Verbinde mit `jeeeraaah` auf Port 5432
- Verbinde mit `lib_test` auf Port 5432
- Verbinde mit `keycloak` auf Port 5432
Verschiedene Datenbanken, gleicher Port!
