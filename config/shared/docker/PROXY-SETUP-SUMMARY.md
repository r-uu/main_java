# Docker Proxy Setup - Zusammenfassung

## Was wurde konfiguriert?

### ✅ 1. Docker Daemon Proxy (systemd)
**Datei:** `/etc/systemd/system/docker.service.d/http-proxy.conf`

Ermöglicht dem Docker Daemon, Images von Docker Hub zu laden.

### ✅ 2. Docker Client Proxy
**Datei:** `~/.docker/config.json`

Konfiguriert BuildKit für Proxy-Nutzung (teilweise problematisch).

### ✅ 3. DNS-Konfiguration
**Datei:** `/etc/docker/daemon.json`

Verwendet Google DNS (8.8.8.8) für bessere Name-Auflösung.

### ✅ 4. Docker Compose Umgebungsvariablen
**Datei:** `config/shared/docker/.env`

Stellt Proxy-Variablen für docker-compose.yml bereit.

### ✅ 5. Dockerfile Proxy-Support
**Datei:** `root/lib/office/word/jasperreports/Dockerfile`

Akzeptiert HTTP_PROXY/HTTPS_PROXY als Build-Args.

### ✅ 6. Build-Wrapper-Skript
**Datei:** `config/shared/docker/docker-compose-build.sh`

Automatisiert Builds mit korrekten Proxy-Einstellungen.

## Verwendung

### Images pullen
```bash
docker pull postgres:16-alpine
# → Funktioniert ✅
```

### Docker Compose bauen (empfohlen)
```bash
cd config/shared/docker
./docker-compose-build.sh jasperreports
```

### Manueller Build
```bash
cd config/shared/docker
DOCKER_BUILDKIT=0 docker compose build jasperreports
```

## Bekannte Probleme

### Problem: BuildKit Timeout
**Symptom:**
```
dial tcp: i/o timeout
```

**Lösung:**
```bash
export DOCKER_BUILDKIT=0  # Klassischen Builder verwenden
```

### Problem: Base Image kann nicht geladen werden
**Symptom:**
```
failed to fetch anonymous token
```

**Lösung:**
1. Docker Daemon neu starten:
   ```bash
   sudo systemctl restart docker
   ```

2. Proxy-Konfiguration prüfen:
   ```bash
   sudo systemctl show --property=Environment docker | grep PROXY
   ```

## Nächste Schritte

1. **Warten auf Build-Abschluss:**
   ```bash
   docker compose build jasperreports
   ```

2. **Container starten:**
   ```bash
   docker compose up -d jasperreports
   ```

3. **Logs prüfen:**
   ```bash
   docker compose logs -f jasperreports
   ```

4. **Health Check:**
   ```bash
   curl http://localhost:8090/health
   ```

## Proxy-Credentials

⚠️ **WICHTIG:** Diese Credentials sind in folgenden Dateien gespeichert:
- `/etc/systemd/system/docker.service.d/http-proxy.conf`
- `~/.docker/config.json`
- `config/shared/docker/.env`

**Bei Passwort-Änderung:** Alle Dateien aktualisieren und Docker neu starten!

## Verifizierung

### Test 1: Docker Daemon Proxy
```bash
sudo systemctl show --property=Environment docker | grep PROXY
```

**Erwartetes Ergebnis:**
```
Environment=HTTP_PROXY=http://gkd-re%5Clinuxupdateuser:Eet9atoo@proxy.gkd-re.local:8080
```

### Test 2: Image Pull
```bash
docker pull hello-world
```

**Erwartetes Ergebnis:**
```
latest: Pulling from library/hello-world
...
Status: Downloaded newer image
```

### Test 3: Docker Compose Build
```bash
./docker-compose-build.sh jasperreports
```

**Erwartetes Ergebnis:**
- Maven lädt Dependencies über Proxy
- JasperReports Image wird gebaut
- Keine Timeout-Fehler

---

**Status:** ✅ Proxy-Konfiguration abgeschlossen  
**Datum:** 4. Februar 2026  
**Nächster Schritt:** Build-Prozess überwachen
