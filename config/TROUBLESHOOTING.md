# 🔧 TROUBLESHOOTING - Problemlösungen

**Häufige Probleme und Lösungen**

---

## 🐳 Docker-Probleme

### Container startet nicht

```bash
# 1. Logs prüfen
ruu-docker-logs

# 2. Spezifischen Container prüfen
docker logs <container-name>

# 3. Neustart versuchen
ruu-docker-restart

# 4. Hard Reset
ruu-docker-reset
```

### Container "unhealthy"

```bash
# Health-Check manuell testen
docker exec <container> <health-check-command>

# Beispiele:
docker exec keycloak curl -f http://localhost:8080/health/ready
docker exec jasperreports wget -q -O- http://localhost:8090/health
docker exec postgres-jeeeraaah pg_isready -U r_uu
```

### Port bereits belegt

```bash
# Prüfen welcher Prozess Port nutzt
sudo netstat -tulpn | grep <port>

# Prozess beenden oder Port ändern in docker-compose.yml
```

---

## ☕ Java-Probleme

### "JAVA_HOME not defined correctly"

```bash
# Prüfen
echo $JAVA_HOME
java --version

# Fix
export JAVA_HOME="/opt/graalvm-jdk-25"
export PATH="$JAVA_HOME/bin:$PATH"

# Permanent in ~/.bashrc
echo 'export JAVA_HOME="/opt/graalvm-jdk-25"' >> ~/.bashrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

### Falsche Java-Version

```bash
# Aktuelle Version
ruu-java-version

# GraalVM aktivieren
source config/shared/wsl/aliases.sh
ruu-java-version
# Sollte GraalVM 25 zeigen
```

---

## 🔨 Maven-Probleme

### Build-Fehler

```bash
# Clean Build
ruu-clean
ruu-build

# BOM neu bauen
ruu-bom-install

# Maven Cache löschen
rm -rf ~/.m2/repository/r-uu
ruu-build
```

### "Could not resolve dependencies"

```bash
# Dependency-Updates prüfen
mvn versions:display-dependency-updates

# BOM installieren
cd bom
mvn clean install
cd ..
ruu-build
```

### "release version 25 not supported"

**Ursache:** Tool nutzt Java < 25

**Lösung:** Nur im Projekt relevant - Docker-Container nutzen Java 17

---

## 🔐 Keycloak-Probleme

### "Realm does not exist"

```bash
# Realm-Setup ausführen
ruu-keycloak-setup

# Status prüfen
curl http://localhost:8080/realms/jeeeraaah-realm
# Sollte JSON zurückgeben
```

### "Invalid client or credentials"

```bash
# Client-Konfiguration prüfen in Admin Console
# http://localhost:8080/admin

# Direct Access Grants aktiviert?
# Clients → jeeeraaah-frontend → Settings → Direct Access Grants: ON
```

### "Session expired"

```bash
# Token-Lifetime in Keycloak erhöhen
# Admin Console → Realm Settings → Tokens
# - Access Token Lifespan: 5 minutes → 15 minutes
# - SSO Session Idle: 30 minutes → 60 minutes
```

---

## 🗄️ Datenbank-Probleme

### "port out of range:-1"

```bash
# config.properties prüfen
cat config.properties

# Sollte enthalten:
# db.port=5432
# db.host=localhost
```

### "database does not exist"

```bash
# Datenbank erstellen
ruu-postgres-shell-admin

# In psql:
CREATE DATABASE lib_test OWNER r_uu;
\q
```

### "password authentication failed"

```bash
# Credentials in config.properties prüfen
# db.user=r_uu
# db.password=r_uu_password

# Docker-Container neu starten
ruu-docker-reset
```

---

## 💻 IntelliJ-Probleme

### "Could not make working directory" (Windows-Pfad)

**Ursache:** IntelliJ verwendet Windows-Pfade in WSL

**Lösung:**
- Run Configuration → Don't delegate to Maven
- Working Directory: `$MODULE_WORKING_DIR$`

### "Module not found"

**Ursache:** JPMS module-path Problem

**Lösung:**
```bash
# Maven reimport
# IntelliJ → Maven → Reload All Maven Projects

# Cache invalidieren
# File → Invalidate Caches → Invalidate and Restart
```

### "Unresolved plugin"

**Nach Maven reimport:**
- Settings → Build → Compiler → Build project automatically: ON
- File → Invalidate Caches → Invalidate and Restart

---

## 📄 JasperReports-Probleme

### Container baut nicht

```bash
# Cache löschen
docker compose build --no-cache jasperreports

# Logs während Build
docker compose build jasperreports 2>&1 | tail -50
```

### "Unable to load report"

```bash
# Template existiert?
ls -la root/sandbox/office/microsoft/word/jasperreports/templates/

# Container neu starten
ruu-jasper-rebuild
```

---

## 🌐 Git-Probleme (WSL + IntelliJ)

### "Exec format error"

**Ursache:** IntelliJ versucht Windows .exe aus WSL auszuführen

**Lösung:** In `aliases.sh` (bereits enthalten):
```bash
export GIT_ASKPASS=""
export SSH_ASKPASS=""
```

### Git Push schlägt fehl

```bash
# SSH verwenden statt HTTPS
git remote set-url origin git@github.com:user/repo.git

# Oder Git Credential Helper
git config --global credential.helper store
```

---

## 🚀 Backend-Probleme

### Liberty startet nicht

```bash
# Logs prüfen
cd root/app/jeeeraaah/backend/api/ws_rs
mvn liberty:dev

# server.xml prüfen
cat src/main/liberty/config/server.xml

# server.env prüfen
cat src/main/liberty/config/server.env
```

### "Recursive expression expansion"

**Ursache:** Zirkul

äre Property-Referenzen

**Lösung:** `config.properties` prüfen
```properties
# FALSCH:
keycloak.realm=${keycloak.realm}

# RICHTIG:
keycloak.realm=jeeeraaah-realm
```

---

## 🖥️ Frontend-Probleme

### "Module xyz not found"

**Lösung:**
```bash
# Projekt neu bauen
ruu-build

# IntelliJ Maven Reload
# Maven → Reload All Maven Projects
```

### JavaFX-Fehler

```bash
# DISPLAY Variable setzen (WSL)
export DISPLAY=:0

# Oder X-Server installieren (VcXsrv, X410)
```

---

## 🔄 Kompletter Reset

### Alles zurücksetzen (behält Code)

```bash
# 1. Docker komplett reset
ruu-docker-reset

# 2. Maven Cache löschen
rm -rf ~/.m2/repository/r-uu

# 3. Projekt neu bauen
ruu-clean
ruu-build

# 4. Keycloak neu konfigurieren
ruu-keycloak-setup

# 5. Testen
ruu-docker-ps
ruu-versions
```

---

## 📞 Weitere Hilfe

```bash
# Alle Aliase anzeigen
ruu-help

# Tool-Versionen prüfen
ruu-versions

# Logs prüfen
ruu-docker-logs
ruu-keycloak-logs
ruu-jasper-logs
```

---

**Dokumentation:**
- [DOCKER-SETUP.md](DOCKER-SETUP.md)
- [KEYCLOAK-SETUP.md](KEYCLOAK-SETUP.md)
- [JASPERREPORTS-SETUP.md](JASPERREPORTS-SETUP.md)
- [START-HERE.md](../START-HERE.md)
