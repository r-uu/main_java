# Docker Health Check & Auto-Fix System

## Übersicht

Das Health Check & Auto-Fix System prüft die Docker-Umgebung beim Anwendungsstart und behebt automatisch häufige Probleme.

## Komponenten

### 1. Health Checks (`de.ruu.lib.docker.health.check`)

Prüfungen der Docker-Umgebung:

- **DockerDaemonHealthCheck** - Docker Daemon läuft?
- **PostgresDatabaseHealthCheck** - Datenbanken erreichbar?
- **KeycloakServerHealthCheck** - Keycloak Container läuft?
- **KeycloakRealmHealthCheck** - Keycloak Realm existiert?
- **JasperReportsHealthCheck** - JasperReports Service läuft?

### 2. Auto-Fix Strategies (`de.ruu.lib.docker.health.fix`)

Automatische Behebungen:

- **DockerContainerStartStrategy** - Startet gestoppte Container
- **KeycloakRealmSetupStrategy** - Erstellt fehlenden Keycloak Realm

### 3. Orchestrierung

- **HealthCheckRunner** - Führt alle Health Checks aus
- **AutoFixRunner** - Orchestriert Auto-Fix basierend auf Failures

## Verwendung

### Einfaches Beispiel

```java
// Health Checks konfigurieren
HealthCheckRunner healthChecker = new HealthCheckRunner();
healthChecker.registerCheck(new DockerDaemonHealthCheck());
healthChecker.registerCheck(new PostgresDatabaseHealthCheck("jeeeraaah", "postgres-jeeeraaah", 5432));
healthChecker.registerCheck(new KeycloakServerHealthCheck());
healthChecker.registerCheck(new KeycloakRealmHealthCheck("jeeeraaah-realm"));

// Auto-Fix konfigurieren
AutoFixRunner autoFix = new AutoFixRunner(healthChecker);
autoFix.registerStrategy(new DockerContainerStartStrategy());
autoFix.registerStrategy(new KeycloakRealmSetupStrategy());

// Ausführen (mit Auto-Fix)
if (!autoFix.runWithAutoFix()) {
    // Fehlerbehandlung
    System.err.println("Docker environment setup failed!");
    System.exit(1);
}
```

### Workflow

1. **Health Checks ausführen**
   - Alle registrierten Checks werden ausgeführt
   - Failures werden gesammelt

2. **Auto-Fix bei Failures**
   - Für jeden Failure wird passende Strategy gesucht
   - Strategy versucht Problem zu beheben
   - Wartet konfigurierte Zeit (default: 5s)

3. **Re-Check**
   - Health Checks werden erneut ausgeführt
   - Bei Erfolg: Anwendung startet normal
   - Bei Misserfolg: Fehler wird gemeldet

## Eigene Auto-Fix Strategy erstellen

```java
public class CustomFixStrategy implements AutoFixStrategy {
    
    @Override
    public boolean canHandle(String serviceName) {
        return "My Service".equals(serviceName);
    }
    
    @Override
    public boolean fix(String serviceName) {
        try {
            // Fix-Logik hier
            return true; // Erfolg
        } catch (Exception e) {
            return false; // Misserfolg
        }
    }
    
    @Override
    public String getDescription() {
        return "Fixes my custom service";
    }
}
```

Dann registrieren:

```java
autoFix.registerStrategy(new CustomFixStrategy());
```

## Konfiguration

### DockerContainerStartStrategy

```java
// Default: ~/develop/github/main/config/shared/docker, 30s Wartezeit
new DockerContainerStartStrategy()

// Custom:
new DockerContainerStartStrategy("/custom/docker/dir", 60)
```

### KeycloakRealmSetupStrategy

```java
// Default: ~/develop/github/main/root/lib/keycloak_admin
new KeycloakRealmSetupStrategy()

// Custom:
new KeycloakRealmSetupStrategy("/custom/keycloak/project")
```

### AutoFixRunner

```java
// Default: 5s Wartezeit vor Re-Check
new AutoFixRunner(healthChecker)

// Custom:
new AutoFixRunner(healthChecker, 10)
```

## Best Practices

### 1. Health Checks vor Auto-Fix

Definiere Health Checks so spezifisch wie möglich:

```java
// ✅ GUT: Spezifisch
healthChecker.registerCheck(new KeycloakRealmHealthCheck("jeeeraaah-realm"));

// ❌ SCHLECHT: Zu generisch
healthChecker.registerCheck(new GenericHealthCheck());
```

### 2. Strategy Reihenfolge

Registriere Strategies in logischer Reihenfolge:

```java
// ✅ GUT: Container zuerst, dann Realm
autoFix.registerStrategy(new DockerContainerStartStrategy());
autoFix.registerStrategy(new KeycloakRealmSetupStrategy());

// ❌ SCHLECHT: Realm vor Container
autoFix.registerStrategy(new KeycloakRealmSetupStrategy());
autoFix.registerStrategy(new DockerContainerStartStrategy());
```

### 3. Logging

Alle Komponenten loggen auf verschiedenen Levels:

- **INFO**: Normale Operationen
- **WARN**: Warnings (z.B. keine Strategy gefunden)
- **ERROR**: Fehler
- **DEBUG**: Detaillierte Informationen

Aktiviere DEBUG-Logging bei Problemen:

```xml
<!-- log4j2.xml -->
<Logger name="de.ruu.lib.docker.health" level="DEBUG"/>
```

## Fehlerbehandlung

### Szenario 1: Container gestoppt

```
Health Check: ❌ Keycloak Container is not running
Auto-Fix: Starts container with docker-compose
Re-Check: ✅ All checks passed
Result: SUCCESS
```

### Szenario 2: Realm fehlt

```
Health Check: ❌ Keycloak realm does not exist
Auto-Fix: Runs KeycloakRealmSetup
Re-Check: ✅ All checks passed
Result: SUCCESS
```

### Szenario 3: Keine Strategy verfügbar

```
Health Check: ❌ Unknown Service failed
Auto-Fix: No strategy available
Result: FAILURE (manual intervention needed)
```

## Testing

### Unit Test Beispiel

```java
@Test
void testAutoFix() {
    // Mock Health Check Runner
    HealthCheckRunner healthChecker = mock(HealthCheckRunner.class);
    when(healthChecker.runAll())
        .thenReturn(false)  // Erste Prüfung: Fehler
        .thenReturn(true);  // Nach Fix: Erfolg
    
    when(healthChecker.getFailures()).thenReturn(
        List.of(new HealthCheckResult("Test Service", false, null, null))
    );
    
    // Mock Strategy
    AutoFixStrategy strategy = mock(AutoFixStrategy.class);
    when(strategy.canHandle("Test Service")).thenReturn(true);
    when(strategy.fix("Test Service")).thenReturn(true);
    
    // Test
    AutoFixRunner autoFix = new AutoFixRunner(healthChecker, 0);
    autoFix.registerStrategy(strategy);
    
    assertTrue(autoFix.runWithAutoFix());
    verify(strategy).fix("Test Service");
}
```

## Monitoring

Log-Output bei erfolgreichem Auto-Fix:

```
INFO  - 🏥 Docker Environment Health Check
INFO  - Checking Keycloak server...
ERROR - ❌ Keycloak container is not running
WARN  - ⚠️ Health check failures detected - attempting auto-fix...
INFO  - Attempting to fix: Keycloak Container
INFO  - Using strategy: Starts stopped Docker containers using docker-compose
INFO  - Starting Docker container: keycloak
INFO  - ✅ Container keycloak started successfully
INFO  - Waiting 30s for container to become healthy...
INFO  - Waiting 5s before re-checking health...
INFO  - Re-running health checks after auto-fix...
INFO  - ✅ Auto-fix successful - all health checks passed!
```

## Siehe auch

- [DOCKER-HEALTH-CHECK.md](DOCKER-HEALTH-CHECK.md) - Health Check Details
- [KEYCLOAK-SETUP-VOLLAUTOMATISCH.md](KEYCLOAK-SETUP-VOLLAUTOMATISCH.md) - Keycloak Setup
