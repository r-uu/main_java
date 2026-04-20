# Single Point of Truth - Credential Management

## Übersicht

Alle Credentials und Konfigurationswerte werden zentral in **einer einzigen Datei** verwaltet:

```
config/shared/docker/.env
```

Diese Datei ist **NICHT** unter Git-Kontrolle (`.gitignore`) und enthält alle sensitiven Daten.

## Template-Datei

Die Template-Datei unter Git-Kontrolle:

```
config/shared/docker/.env.template
```

Kopiere diese Datei zu `.env` und passe die Werte an:

```bash
cp config/shared/docker/.env.template config/shared/docker/.env
```

## Credential-Struktur

### PostgreSQL Admin (System-Admin für alle Postgres-Instanzen)
```properties
postgres_admin_user=postgres_admin
postgres_admin_password=<your-password>
postgres_admin_database=postgres
```

### PostgreSQL Jeeeraaah (Application Database)
```properties
postgres_jeeeraaah_host=localhost
postgres_jeeeraaah_port=5432
postgres_jeeeraaah_user=<your-username>
postgres_jeeeraaah_password=<your-password>
postgres_jeeeraaah_database_jeeeraaah=jeeeraaah
postgres_jeeeraaah_database_lib_test=lib_test
```

### PostgreSQL Keycloak (Keycloak Database)
```properties
postgres_keycloak_host=localhost
postgres_keycloak_port=5433
postgres_keycloak_user=<your-username>
postgres_keycloak_password=<your-password>
postgres_keycloak_database=keycloak
```

### Keycloak Admin
```properties
keycloak_admin_user=admin
keycloak_admin_password=admin
```

### Test Credentials
```properties
test_user=<your-test-user>
test_password=<your-test-password>
```

## Wie werden die Werte verteilt?

### 1. Docker Compose
`docker-compose.yml` lädt `.env` automatisch:

```yaml
environment:
  POSTGRES_USER: ${postgres_jeeeraaah_user}
  POSTGRES_PASSWORD: ${postgres_jeeeraaah_password}
```

### 2. Maven Resource Filtering
`bom/pom.xml` konfiguriert Resource Filtering:

```xml
<build>
  <filters>
    <filter>${project.basedir}/../config/shared/docker/.env</filter>
  </filters>
  <resources>
    <resource>
      <directory>src/main/resources</directory>
      <filtering>true</filtering>
    </resource>
  </resources>
</build>
```

Dann in `persistence.xml`:
```xml
<property name="jakarta.persistence.jdbc.user" value="${postgres_jeeeraaah_user}"/>
<property name="jakarta.persistence.jdbc.password" value="${postgres_jeeeraaah_password}"/>
```

### 3. MicroProfile Config
In `microprofile-config.properties`:
```properties
# Values are replaced during Maven build via resource filtering
database.username=${postgres_jeeeraaah_user}
database.password=${postgres_jeeeraaah_password}
```

### 4. Java Code (über MicroProfile Config)
```java
@Inject
@ConfigProperty(name = "database.username")
String dbUsername;
```

## Vorteile

✅ **Single Point of Truth**: Nur eine Datei zu pflegen  
✅ **Keine Duplikation**: Kein Copy-Paste von Credentials  
✅ **Git-sicher**: `.env` ist in `.gitignore`  
✅ **Template**: `.env.template` zeigt die Struktur  
✅ **Automatisch**: Maven ersetzt Platzhalter beim Build  

## Wichtige Regeln

1. **NIE** echte Credentials in `.env.template` eintragen
2. **NIE** echte Credentials committen
3. **IMMER** `.env` aus `.env.template` erstellen
4. **IMMER** nach Änderungen an `.env` das Projekt neu bauen:
   ```bash
   cd ~/develop/github/java/main/root
   mvn clean install
   ```

## Debugging

### Überprüfen, ob Werte ersetzt wurden:
```bash
# In target-Verzeichnis schauen:
cat target/classes/META-INF/microprofile-config.properties
cat target/classes/META-INF/persistence.xml
```

### Häufige Probleme:

**Problem**: Platzhalter wie `${postgres_jeeeraaah_user}` bleiben stehen  
**Lösung**: 
1. Prüfe, ob `.env` existiert
2. Führe `mvn clean install` aus
3. Prüfe Filter-Konfiguration in `bom/pom.xml`

**Problem**: Tests schlagen fehl mit "password authentication failed"  
**Lösung**:
1. Prüfe Credentials in `.env`
2. Führe `mvn clean install` aus
3. Starte Docker-Container neu: `ruu-docker-restart`

## Dateien-Übersicht

```
project/
├── config/
│   └── shared/
│       └── docker/
│           ├── .env               ← Single Point of Truth (NICHT in Git)
│           ├── .env.template      ← Template (IN Git)
│           └── docker-compose.yml ← Nutzt .env
├── bom/
│   └── pom.xml                    ← Konfiguriert Resource Filtering
└── */
    └── src/main/resources/
        ├── META-INF/
        │   ├── persistence.xml    ← ${...} wird ersetzt
        │   └── microprofile-config.properties ← ${...} wird ersetzt
        └── ...
```

## Siehe auch

- `config/shared/docker/.env.template` - Template-Datei
- `config/shared/docker/docker-compose.yml` - Docker-Konfiguration
- `bom/pom.xml` - Maven Resource Filtering Konfiguration
- `config.properties.template` - Legacy-Konfiguration (wird noch migriert)
