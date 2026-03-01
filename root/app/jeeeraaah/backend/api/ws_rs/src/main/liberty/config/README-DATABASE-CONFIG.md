# Liberty Server Konfiguration - Datenbank-Verbindung

## Problem
Liberty kann keine Verbindung zur PostgreSQL-Datenbank aufbauen, wenn die IP-Adresse hardcodiert ist.

## Lösung
Die Datenbank-Host-Konfiguration befindet sich in der Datei `server.env` im Liberty-Konfigurationsverzeichnis.

### Speicherort
```
app/jeeeraaah/backend/api/ws_rs/src/main/liberty/config/server.env
```

### Konfiguration anpassen

Die wichtigste Einstellung ist `datasource_server_host`:

```bash
# Standard für WSL2-Entwicklung (PostgreSQL läuft in WSL2)
datasource_server_host=localhost
```

### Verschiedene Szenarien

#### 1. PostgreSQL in WSL2, Liberty in WSL2 (Empfohlen für Entwicklung)
```bash
datasource_server_host=localhost
datasource_server_port=5432
```

#### 2. PostgreSQL in Docker Container
```bash
# Wenn Docker Port-Mapping verwendet wird: -p 5432:5432
datasource_server_host=localhost
datasource_server_port=5432
```

#### 3. PostgreSQL auf anderem WSL2-System/IP
```bash
# IP-Adresse mit: ip addr show eth0
datasource_server_host=172.26.187.214
datasource_server_port=5432
```

#### 4. PostgreSQL auf Windows Host (Zugriff von WSL2)
```bash
# Windows IP mit: ipconfig (in PowerShell)
datasource_server_host=<Windows-IP-Adresse>
datasource_server_port=5432
```

#### 5. Beide in Docker mit Docker Compose
```bash
# Service-Name aus docker-compose.yml verwenden
datasource_server_host=postgres-container-name
datasource_server_port=5432
```

### IP-Adresse herausfinden

**WSL2 IP-Adresse:**
```bash
ip addr show eth0
```

**Windows Host IP (von PowerShell):**
```powershell
ipconfig
```

**PostgreSQL Container IP:**
```bash
docker inspect <container-name> | grep IPAddress
```

### Nach Änderungen

Nach Änderungen an `server.env` muss Liberty neu gestartet werden:

```bash
# Liberty Server neu starten
mvn liberty:stop
mvn liberty:run
```

### Template-Datei

Eine Template-Datei mit allen Szenarien ist verfügbar:
```
server.env.template
```

Diese kann als Vorlage für verschiedene Umgebungen verwendet werden.

## Weitere Konfigurationsoptionen

Alle verfügbaren Parameter in `server.env`:

- `default_http_port` - HTTP Port (Standard: 9080)
- `default_https_port` - HTTPS Port (Standard: 9443)
- `default_host_name` - Netzwerk-Interface Binding (Standard: *)
- `datasource_server_host` - PostgreSQL Hostname/IP
- `datasource_server_port` - PostgreSQL Port
- `datasource_database` - Datenbankname
- `datasource_database_username` - Datenbank-Benutzername
- `datasource_database_password` - Datenbank-Passwort

## Sicherheitshinweis

**Wichtig:** Die `server.env` Datei sollte NICHT in die Versionskontrolle aufgenommen werden, da sie sensible Zugangsdaten enthält!

Fügen Sie folgende Zeile zur `.gitignore` hinzu:
```
**/server.env
```

Verwenden Sie stattdessen die `server.env.template` Datei als Vorlage und dokumentieren Sie die erforderlichen Konfigurationsschritte.

