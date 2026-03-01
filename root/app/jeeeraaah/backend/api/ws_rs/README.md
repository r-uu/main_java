# JEEERAAAH Backend API (OpenLiberty + JTA/JPA)

Dieses Modul stellt die JEEERAAAH Backend API bereit, deployt als WAR auf OpenLiberty. Die App nutzt JTA/JPA mit einer PostgreSQL-Datenquelle.

- Artifact/Name: `r-uu.app.jeeeraaah.backend.api.ws.rs`
- Context-Root: `/jeeeraaah` (siehe `server.xml`)
- JNDI DataSource: `jdbc/datasource_postgresql`
- Persistence Provider: Hibernate (via Liberty `persistenceContainer-3.1`)

## Voraussetzungen
- Docker Desktop (lokale PostgreSQL-Instanz im Container)
- Java 25 (GraalVM)
- Maven
- WSL2 mit konfigurierten Aliases

## Datenbank (lokal via Docker)
Standardwerte (aus `config/shared/docker/.env`):
- Host: `localhost`
- Port: `5432`
- DB: `jeeeraaah`
- Benutzer: `jeeeraaah`
- Passwort: `jeeeraaah`

DB starten:
```bash
ruu-docker-up
```

## Starten im Dev-Mode (Liberty)
```bash
cd ~/develop/github/main/root/app/jeeeraaah/backend/api/ws_rs
mvn liberty:dev
```

Oder mit Alias:
```bash
ruu-liberty-dev
```

Beenden: `Strg+C` im Dev-Mode-Terminal oder (Git Bash) `lib-stop-jeee`.

## Aufruf der API
Die Anwendung ist unter dem Context-Root `/jeee-raaah` verfügbar, z. B.:
```
http://localhost:9080/jeee-raaah/
```
Konkrete Ressourcen/Endpoints hängen von den implementierten JAX-RS-Ressourcen ab (siehe Quellcode des Moduls). Falls eine OpenAPI/Swagger-UI konfiguriert ist, ist sie i. d. R. ebenfalls unter dem Context-Root erreichbar.

## Konfiguration (Auszug)
- `src/main/liberty/config/server.xml`
  - `featureManager`: u. a. `microProfile-6.1`, `jdbc-4.3`, `persistenceContainer-3.1`
  - `dataSource id="datasource_postgresql"` mit `jndiName="jdbc/datasource_postgresql"`
  - `<jpa defaultPersistenceProvider="org.eclipse.persistence.jpa.PersistenceProvider" ... />`
  - `<webApplication contextRoot="/jeee-raaah" ... />`
- Persistenz-Unit(s): unter `src/main/resources/META-INF/persistence.xml`

## Troubleshooting
- DB-Fehler/Verbindungsprobleme: Läuft der Container? Port 5432 frei? Firewall prüfen.
- Schema/Tables fehlen: Beim ersten Start wird `test` angelegt (Init-Skript); je nach PU/DDL-Settings erzeugt JPA die Tabellen automatisch.
- Rechte/Ownership: Rolle `r_uu` muss existieren (siehe Init-Skript). Bei Problemen `pg ensure` (Git Bash) ausführen.
- Context-Root: Endpoints liegen unter `/jeee-raaah/...`.

## Nützliche Aliases (Git Bash)
```bash
lib-dev-jeee     # Liberty dev mode für das Backend starten
lib-stop-jeee    # Liberty dev mode stoppen
pg-psql          # psql verbunden mit der lokalen DB
pg-backup        # Dump erstellen
pg-restore-last  # letzten Dump zurückspielen
```

## OpenAPI-Dokumentation
Während der Dev-Mode läuft, ist die API-Spezifikation erreichbar:
- JSON: `http://localhost:9080/jeee-raaah/openapi`
- YAML: `http://localhost:9080/jeee-raaah/openapi?format=YAML`

Export (Windows CMD): Skript `scripts/export-openapi.cmd` legt Kopien unter `docs/openapi.json` und `docs/openapi.yaml` ab.
