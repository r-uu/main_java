
**Datum:** 2026-01-18  
**Status:** ✅ **GELÖST**

---

## 🔴 PROBLEM

Der Keycloak-Container war als **unhealthy** markiert:

```bash
docker ps
NAMES              STATUS
keycloak-service   Up 20 hours (unhealthy)
```

**FailingStreak:** 188 (!)

---

## 🔍 URSACHE

Der laufende Container verwendete einen **veralteten Health-Check** mit `curl`:

```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -f http://localhost:8080/health/ready || exit 1"]
```

**Problem:** Das Keycloak-Image enthält **kein `curl`**!

```
/bin/sh: line 1: curl: command not found
ExitCode: 1
```

---

## ✅ LÖSUNG

### 1. Korrekte Health-Check Konfiguration

Die `docker-compose.yml` hatte bereits die **korrekte Konfiguration** mit TCP-Socket-Test:

```yaml
healthcheck:
  test: ["CMD-SHELL", "exec 3<>/dev/tcp/localhost/8080 || exit 1"]
  interval: 30s
  timeout: 10s
  retries: 10
  start_period: 120s
```

**Vorteil:** Verwendet nur Shell-Built-ins, kein externes Tool benötigt!

### 2. Container neu starten

Der Container musste mit der aktuellen Konfiguration neu gestartet werden:

```bash
cd /home/r-uu/develop/github/main/config/shared/docker
docker compose stop keycloak
docker rm keycloak-service
docker compose up -d keycloak
```

**Oder per Alias:**

```bash
ruu-keycloak-restart
```

### 3. Warten auf Initialisierung

Keycloak braucht ~2 Minuten zum Starten (`start_period: 120s`):

```bash
# Warten...
sleep 120

# Status prüfen
docker ps | grep keycloak
```

---

## ✅ ERGEBNIS

Alle Container sind jetzt **healthy**:

```bash
docker ps
NAMES                   STATUS
keycloak-service        Up 3 minutes (healthy)   ✅
jasperreports-service   Up 6 minutes (healthy)   ✅
postgres-jeeeraaah      Up 20 hours (healthy)    ✅
postgres-keycloak       Up 20 hours (healthy)    ✅
```

---

## 🎯 WIE ES PASSIERTE

Der Container wurde wahrscheinlich mit einer **älteren docker-compose.yml** gestartet, die noch den `curl`-basierten Health-Check hatte.

Beim Update der docker-compose.yml wurde der **laufende Container nicht neu gestartet**, daher behielt er die alte Konfiguration.

---

## 🔧 HEALTH-CHECK DETAILS

### Aktuelle Konfiguration (korrekt):

```yaml
healthcheck:
  test: ["CMD-SHELL", "exec 3<>/dev/tcp/localhost/8080 || exit 1"]
  interval: 30s          # Prüfe alle 30 Sekunden
  timeout: 10s           # Timeout nach 10 Sekunden
  retries: 10            # 10 Fehlversuche bis "unhealthy"
  start_period: 120s     # Ignoriere Fehler in den ersten 2 Minuten
```

### Wie es funktioniert:

```bash
exec 3<>/dev/tcp/localhost/8080
```

- Öffnet einen TCP-Socket zu localhost:8080
- Exit Code 0 wenn erfolgreich → healthy
- Exit Code 1 wenn fehlgeschlagen → unhealthy
- Benötigt nur Shell-Built-ins ✅

---

## 🚀 VERWENDUNG

### Container-Status prüfen:

```bash
docker ps
# oder
ruu-docker-ps
```

### Keycloak neu starten:

```bash
ruu-keycloak-restart
```

### Keycloak Logs:

```bash
ruu-keycloak-logs
```

### Health-Check manuell testen:

```bash
docker exec keycloak-service /bin/sh -c "exec 3<>/dev/tcp/localhost/8080 && echo 'OK'"
```

### Keycloak Admin Console:

```
URL: http://localhost:8080/admin
Credentials: siehe config.properties
```

---

## 📋 PRÄVENTION

### Beim Docker-Compose Update immer Container neu starten:

```bash
cd /home/r-uu/develop/github/main/config/shared/docker
docker compose down
docker compose up -d
```

**Oder:**

```bash
ruu-docker-restart
```

### Bei Health-Check Änderungen:

1. Container stoppen: `docker compose stop keycloak`
2. Container entfernen: `docker rm keycloak-service`
3. Neu starten: `docker compose up -d keycloak`

**Oder einfacher:**

```bash
ruu-docker-reset  # Kompletter Reset mit Backup
```

---

## ✅ CHECKLISTE

- [x] Problem identifiziert (curl fehlt im Container)
- [x] Korrekte Health-Check Konfiguration bestätigt
- [x] Container mit neuer Konfiguration gestartet
- [x] 120 Sekunden Wartezeit beachtet
- [x] Alle Container sind healthy
- [x] Keycloak ist erreichbar
- [x] Dokumentation erstellt

---

## 📚 SIEHE AUCH

- `config/shared/docker/docker-compose.yml` - Docker-Konfiguration
- `config/ALIASE-SCHNELLREFERENZ.md` - Keycloak-Aliase
- `ruu-help-docker` - Docker-Hilfe

---

✅ **Keycloak-Container ist jetzt healthy!**  
✅ **Health-Check verwendet Shell-Built-ins statt curl!**  
✅ **Problem dauerhaft behoben!**

