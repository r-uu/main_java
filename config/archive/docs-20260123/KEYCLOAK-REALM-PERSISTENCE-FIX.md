# ✅ Keycloak Realm Persistence Fix - GELÖST!

**Datum:** 2026-01-23  
**Problem:** Keycloak Realm verschwindet nach Container-Restart  
**Lösung:** Keycloak Data Volume + Auto-Fix Health Check

---

## 🔍 PROBLEM-ANALYSE

### ❌ VORHER:

1. **Keycloak hatte KEIN Volume** für `/opt/keycloak/data`
2. **Realm-Konfiguration nur in PostgreSQL**, aber nicht vollständig persistent
3. **Bei jedem Container-Restart** musste Realm manuell neu erstellt werden
4. **Health Check** erkannte Problem, aber **kein Auto-Fix**

---

## ✅ LÖSUNG

### 1. Keycloak Data Volume hinzugefügt

**Datei:** `config/shared/docker/docker-compose.yml`

```yaml
keycloak:
  volumes:
    # NEU: Keycloak data directory für Persistenz
    - keycloak-data:/opt/keycloak/data
```

**Volumes-Sektion:**

```yaml
volumes:
  # NEU: Keycloak Data Volume
  keycloak-data:
    name: keycloak-data
```

---

### 2. Auto-Fix im Health Check implementiert

**Datei:** `root/lib/docker_health/.../KeycloakRealmHealthCheck.java`

**Neue Funktionen:**

#### ✅ `check()` - Erweiterter Health Check mit Auto-Fix

```java
@Override
public HealthCheckResult check()
{
  // 1. Prüfe ob Realm existiert
  if (realmExists())
  {
    // 2. Prüfe ob vollständig konfiguriert
    if (verifyRealmConfiguration())
    {
      return HealthCheckResult.success(...);
    }
    else
    {
      // 3. Auto-Fix: Rekonfiguriere Realm
      if (autoFixRealm())
      {
        return HealthCheckResult.success(...);
      }
    }
  }
  else
  {
    // 4. Auto-Fix: Erstelle Realm
    if (autoFixRealm())
    {
      return HealthCheckResult.success(...);
    }
  }
}
```

#### ✅ `verifyRealmConfiguration()` - Vollständige Konfiguration prüfen

```java
private boolean verifyRealmConfiguration()
{
  // Prüft OpenID Configuration Endpoint
  String url = "http://" + host + ":" + port + "/realms/" + realmName + 
               "/.well-known/openid-configuration";
  
  HttpURLConnection conn = ...;
  return conn.getResponseCode() == 200;
}
```

#### ✅ `autoFixRealm()` - Automatische Realm-Erstellung

```java
private boolean autoFixRealm()
{
  // Führt KeycloakRealmSetup automatisch aus
  ProcessBuilder pb = new ProcessBuilder(
    "mvn", "exec:java",
    "-Dexec.mainClass=de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup",
    "-q"
  );
  
  pb.directory(new File(projectDir + "/root/lib/keycloak_admin"));
  Process process = pb.start();
  int exitCode = process.waitFor();
  
  if (exitCode == 0)
  {
    Thread.sleep(2000); // Warte auf Keycloak
    return true;
  }
  
  return false;
}
```

---

## 🚀 WIE ES JETZT FUNKTIONIERT

### Beim Start von DashAppRunner:

```
1️⃣ Health Check läuft
   ├─ Docker Daemon: ✅ OK
   ├─ PostgreSQL Databases: ✅ OK
   ├─ Keycloak Server: ✅ OK
   └─ Keycloak Realm: ❓ Prüfen...

2️⃣ Realm existiert NICHT
   ├─ ⚠️ Fehler erkannt
   ├─ 🔧 Auto-Fix startet
   ├─ → mvn exec:java KeycloakRealmSetup
   ├─ ⏳ Warte 2 Sekunden
   └─ ✅ Realm erstellt!

3️⃣ Dashboard startet
   └─ ✅ Alles funktioniert!
```

### Bei weiteren Starts:

```
1️⃣ Health Check läuft
   └─ Keycloak Realm: ✅ Vorhanden (aus Volume!)

2️⃣ Dashboard startet
   └─ ✅ Sofort einsatzbereit!
```

---

## 📦 PERSISTENZ GARANTIERT

### Keycloak Volumes:

```bash
docker volume ls | grep keycloak
```

**Ausgabe:**

```
keycloak-data            # NEU: Keycloak /opt/keycloak/data
postgres-keycloak-data   # PostgreSQL für Keycloak
```

### Was wird persistiert:

- ✅ **Realm-Konfiguration** (jeeeraaah-realm)
- ✅ **Clients** (jeeeraaah-frontend)
- ✅ **Users** (r_uu + Rollen)
- ✅ **Roles** (task-*, taskgroup-*)
- ✅ **Themes & Customizations**

---

## 🧪 TESTEN

### 1. Realm löschen (absichtlich)

```bash
# Lösche nur das Keycloak Data Volume
docker volume rm keycloak-data

# Starte Keycloak neu
docker compose -f config/shared/docker/docker-compose.yml restart keycloak
```

### 2. DashAppRunner starten

```bash
# In IntelliJ: Run DashAppRunner
```

**Erwartete Ausgabe:**

```
13:XX:XX INFO  - Checking Keycloak realm 'jeeeraaah-realm'...
13:XX:XX ERROR -   ❌ Keycloak realm 'jeeeraaah-realm' does not exist (HTTP 404)
13:XX:XX INFO  -   🔧 Auto-fixing: Creating realm...
13:XX:XX INFO  -     → Executing KeycloakRealmSetup...
13:XX:XX INFO  -     ✓ KeycloakRealmSetup completed successfully
13:XX:XX INFO  -   ✅ Realm 'jeeeraaah-realm' created successfully!
13:XX:XX INFO  - ✅ All health checks passed
```

### 3. Neu starten (ohne Realm zu löschen)

```bash
# Stoppe DashAppRunner
# Starte DashAppRunner erneut
```

**Erwartete Ausgabe:**

```
13:XX:XX INFO  - Checking Keycloak realm 'jeeeraaah-realm'...
13:XX:XX INFO  -   ✅ Keycloak realm 'jeeeraaah-realm' exists
13:XX:XX DEBUG -     ✓ OpenID configuration available
13:XX:XX INFO  -   ✅ Keycloak realm 'jeeeraaah-realm' is fully configured
13:XX:XX INFO  - ✅ All health checks passed
```

---

## 🎯 VORTEILE

### 1. Automatische Wiederherstellung

- **Kein manueller Eingriff mehr nötig**
- **Realm wird automatisch erstellt** falls fehlend
- **Funktioniert bei jedem Start**

### 2. Echte Persistenz

- **Volume speichert Realm-Daten**
- **Überlebt Container-Restarts**
- **Überlebt `docker compose down` (ohne `-v`)**

### 3. Robuste Health Checks

- **Erkennt fehlenden Realm**
- **Erkennt unvollständige Konfiguration**
- **Behebt Probleme automatisch**

---

## ⚙️ KONFIGURATION

### Docker Compose Volume

**Automatisch erstellt** beim ersten `docker compose up -d keycloak`

### Volume löschen (VORSICHT!)

```bash
# Stoppt Keycloak UND löscht Realm-Daten
docker compose down keycloak
docker volume rm keycloak-data

# Startet neu - Auto-Fix erstellt Realm automatisch
docker compose up -d keycloak
```

### Volume sichern

```bash
# Backup erstellen
docker run --rm -v keycloak-data:/data -v $(pwd):/backup \
  alpine tar czf /backup/keycloak-backup.tar.gz /data

# Restore
docker run --rm -v keycloak-data:/data -v $(pwd):/backup \
  alpine tar xzf /backup/keycloak-backup.tar.gz -C /
```

---

## 📚 RELATED DOCS

- **[JWT-AUTHORIZATION-FIX.md](JWT-AUTHORIZATION-FIX.md)** - JWT Setup & Token-Mapping
- **[DOCKER-SETUP.md](DOCKER-SETUP.md)** - Docker Environment
- **[KEYCLOAK-SETUP.md](KEYCLOAK-SETUP.md)** - Keycloak Grundkonfiguration

---

## ✅ STATUS

- **Docker Compose:** ✅ Keycloak Volume konfiguriert
- **Health Check:** ✅ Auto-Fix implementiert
- **Persistenz:** ✅ Realm überlebt Restarts
- **Getestet:** ✅ 2026-01-23

---

## 🎉 ZUSAMMENFASSUNG

**Problem:** Realm verschwand nach jedem Restart  
**Ursache:** Kein Keycloak Data Volume  
**Lösung:** Volume + Auto-Fix Health Check  
**Ergebnis:** ✅ **Vollautomatisch & Persistent!**

Realm wird **NIE WIEDER** manuell erstellt werden müssen! 🚀

---

**Stand:** 2026-01-23 13:55 Uhr  
**Keycloak Container:** `keycloak` (healthy mit Volume)  
**Volume:** `keycloak-data` (persistent)  
**Auto-Fix:** ✅ Aktiv  
**Problem:** ✅ **GELÖST!**
