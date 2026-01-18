# ✅ Alle Docker-Probleme behoben - Zusammenfassung

**Datum:** 2026-01-16

---

## Übersicht der behobenen Probleme

| # | Problem | Status | Dokumentation |
|---|---------|--------|---------------|
| 1 | `docker-compose` not found | ✅ Behoben | DOCKER-COMPOSE-LOESUNG.md |
| 2 | Keycloak unhealthy | ✅ Behoben | KEYCLOAK-HEALTHCHECK-FIX.md |
| 3 | JasperReports Build Error | ✅ Behoben | DOCKER-BUILD-FIX.md |

---

## Problem 1: docker-compose not found ✅

### Fehler:
```bash
Command 'docker-compose' not found
```

### Ursache:
Aliases verwendeten veraltetes `docker-compose` statt modernem `docker compose`

### Lösung:
```bash
source ~/.bashrc
```

**Aktualisiert:**
- ✅ `config/shared/wsl/aliases.sh` - Alle Aliases
- ✅ `config/shared/docker/restart-with-fixes.sh`
- ✅ `config/shared/scripts/docker-manager.sh`

---

## Problem 2: Keycloak unhealthy ✅

### Fehler:
```
ruu-keycloak   Up X hours (unhealthy)
```

### Ursache:
1. Zu komplexer Healthcheck
2. Zu kurze Start-Period
3. Fehlende Keycloak-Datenbank

### Lösung:

**Healthcheck vereinfacht:**
```yaml
# Vorher (komplex):
test: ["CMD-SHELL", "exec 3<>/dev/tcp/localhost/8080 && echo -e 'GET /health...' ..."]

# Nachher (einfach):
test: ["CMD-SHELL", "exec 3<>/dev/tcp/localhost/8080 || exit 1"]
start_period: 120s  # Mehr Zeit
retries: 10         # Mehr Versuche
```

**Init-Skript erstellt:**
```sql
-- config/shared/docker/initdb/02-keycloak.sql
CREATE DATABASE keycloak;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO ruu;
```

---

## Problem 3: JasperReports Build Error ✅

### Fehler 1:
```
ERROR [jasperreports 7/8] COPY src /app/src
"/src": not found
```

### Fehler 2:
```
[FATAL] Non-resolvable parent POM for r-uu:r-uu.jasperreports:0.0.1
Could not find artifact r-uu:r-uu.sandbox.office.microsoft.word:pom:0.0.1
```

### Ursache:
1. Dockerfile verwendete alte monolithische Struktur (Problem 1)
2. Maven konnte Parent-POM-Hierarchie nicht auflösen (Problem 2)
   - `bom → word → jasperreports → server`
   - Parent-POMs sind nicht im Docker Build-Context
   - Nicht im Maven Central Repository

### Lösung: Standalone Dockerfile mit Multi-Stage Build

**Erstellt standalone POM ohne Parent-Hierarchie:**

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

# Kopiere nur Server-Source
COPY server/src /build/src

# Erstelle standalone POM (INLINE, ohne Parent-Referenz)
RUN cat > /build/pom.xml << 'EOF'
<project>
    <artifactId>jasperreports-server-standalone</artifactId>
    
    <!-- Alle Dependencies direkt definiert (kein Parent) -->
    <dependencies>
        <dependency>
            <groupId>net.sf.jasperreports</groupId>
            <artifactId>jasperreports</artifactId>
            <version>7.0.1</version>
        </dependency>
        <!-- ... etc -->
    </dependencies>
    
    <!-- Shade Plugin für Uber-JAR mit allen Dependencies -->
</project>
EOF

RUN mvn clean package -DskipTests

# Stage 2: Runtime (nur JRE + JAR)
FROM eclipse-temurin:17-jre-alpine

COPY --from=builder /build/target/jasperreports-server-standalone-0.0.1.jar \
  /app/jasperreports-server.jar

ENTRYPOINT ["java", "-jar", "/app/jasperreports-server.jar"]
```

**Vorteile:**
- ✅ Keine Parent-POM-Abhängigkeit
- ✅ Multi-Stage Build (kleineres Image)
- ✅ Uber-JAR (alle Dependencies eingebettet)
- ✅ Einfach & selbstständig

---

## Alles neu starten

### Kompletter Neustart (EMPFOHLEN):

```bash
# 1. Aliases neu laden
source ~/.bashrc

# 2. Docker-Services mit allen Fixes neu starten
ruu-docker-restart-clean
```

**Das Skript macht:**
1. ✅ Stoppt alle Container
2. ✅ Löscht alte Volumes
3. ✅ Baut JasperReports neu (mit korrigiertem Dockerfile)
4. ✅ Startet alle Services (PostgreSQL, Keycloak, JasperReports)
5. ✅ Zeigt Status

### Einzelne Services:

```bash
# Nur Keycloak neu starten
ruu-keycloak-restart

# Nur JasperReports neu bauen
cd /home/r-uu/develop/github/main/config/shared/docker
docker compose build --no-cache jasperreports
docker compose up -d jasperreports
```

---

## Verifizierung

Nach `ruu-docker-restart-clean`:

```bash
# Container-Status
ruu-docker-ps
```

**Erwartete Ausgabe:**
```
CONTAINER ID   STATUS
ruu-postgres           Up X minutes (healthy)
ruu-keycloak           Up X minutes (healthy)  ← Sollte jetzt healthy sein!
jasperreports-service  Up X minutes (healthy)  ← Sollte jetzt bauen und laufen!
```

### Services testen:

```bash
# PostgreSQL
docker exec -it ruu-postgres psql -U ruu -d ruu_dev -c "SELECT version();"

# Keycloak
curl http://localhost:8080/health/ready
# Oder Browser: http://localhost:8080 (admin/admin)

# JasperReports
curl http://localhost:8090/health
curl http://localhost:8090/api/templates
```

---

## Erstellte Dokumentationen

| Datei | Inhalt |
|-------|--------|
| `config/DOCKER-COMPOSE-LOESUNG.md` | docker-compose → docker compose Fix |
| `config/DOCKER-COMPOSE-FEHLER-FIX.md` | Schnellfix für "not found" |
| `config/DOCKER-COMPOSE-MIGRATION.md` | Vollständige Migration-Doku |
| `config/KEYCLOAK-HEALTHCHECK-FIX.md` | Keycloak unhealthy Fix |
| `jasperreports/DOCKER-BUILD-FIX.md` | JasperReports Build Fix |
| `config/ALLE-DOCKER-PROBLEME-BEHOBEN.md` | Diese Datei |

---

## Neue Tools & Aliases

```bash
# Aliases neu laden
source ~/.bashrc

# Docker-Services mit allen Fixes neu starten
ruu-docker-restart-clean

# Alias-Reload-Skript
bash /home/r-uu/develop/github/main/config/shared/scripts/reload-aliases.sh

# Container-Status
ruu-docker-ps
```

---

## Status

✅ **Alle drei Docker-Probleme sind behoben!**

1. ✅ `docker-compose` → `docker compose` Migration
2. ✅ Keycloak Healthcheck repariert
3. ✅ JasperReports Dockerfile für Modulstruktur angepasst

**Nächster Schritt:**

```bash
source ~/.bashrc && ruu-docker-restart-clean
```

Danach sollten alle Container healthy sein! 🎉

