# Docker Proxy Setup - Finale Konfiguration

## ✅ Problem gelöst

**Ursprüngliches Problem:**
- Docker konnte keine Images von Docker Hub laden
- Fehler: `dial tcp: i/o timeout`

**Ursache:**
- Docker lief hinter einem Unternehmens-Proxy
- Proxy-Server: `172.16.28.3:8080`
- Hostname `proxy.gkd-re.local` war nicht DNS-auflösbar

## ✅ Implementierte Lösung

### 1. Systemd Proxy-Konfiguration
**Datei:** `/etc/systemd/system/docker.service.d/http-proxy.conf`

```ini
[Service]
Environment="HTTP_PROXY=http://gkd-re%5Clinuxupdateuser:Eet9atoo@172.16.28.3:8080"
Environment="HTTPS_PROXY=http://gkd-re%5Clinuxupdateuser:Eet9atoo@172.16.28.3:8080"
Environment="NO_PROXY=localhost,127.0.0.1,*.local,172.16.0.0/12,10.0.0.0/8"
```

### 2. Docker Client Proxy
**Datei:** `~/.docker/config.json`

```json
{
  "proxies": {
    "default": {
      "httpProxy": "http://gkd-re%5Clinuxupdateuser:Eet9atoo@172.16.28.3:8080",
      "httpsProxy": "http://gkd-re%5Clinuxupdateuser:Eet9atoo@172.16.28.3:8080",
      "noProxy": "localhost,127.0.0.1,*.local,172.16.0.0/12,10.0.0.0/8"
    }
  }
}
```

### 3. Docker Compose Umgebungsvariablen
**Datei:** `config/shared/docker/.env`

```env
HTTP_PROXY=http://gkd-re%5Clinuxupdateuser:Eet9atoo@172.16.28.3:8080
HTTPS_PROXY=http://gkd-re%5Clinuxupdateuser:Eet9atoo@172.16.28.3:8080
NO_PROXY=localhost,127.0.0.1,*.local,172.16.0.0/12,10.0.0.0/8
```

### 4. Dockerfile Anpassungen

**Problem:** Alpine APK konnte Pakete nicht laden wegen SSL-Zertifikatsprobleme mit Proxy

**Lösung:** HTTP statt HTTPS für Alpine Repositories

```dockerfile
# Workaround für Corporate Proxy SSL-Zertifikatsprobleme
RUN echo "http://dl-cdn.alpinelinux.org/alpine/v3.22/main" > /etc/apk/repositories && \
    echo "http://dl-cdn.alpinelinux.org/alpine/v3.22/community" >> /etc/apk/repositories
```

### 5. Build-Wrapper-Skript
**Datei:** `docker-compose-build.sh`

Automatisiert Docker Compose Builds mit:
- Proxy-Umgebungsvariablen
- Deaktiviertem BuildKit (`DOCKER_BUILDKIT=0`)

## ✅ Tests durchgeführt

### Test 1: Basis-Image-Pull
```bash
docker pull hello-world
# ✅ Erfolgreich
```

### Test 2: JasperReports Base Image
```bash
docker pull eclipse-temurin:17-jre-alpine
# ✅ Erfolgreich
```

### Test 3: JasperReports Container Build
```bash
./docker-compose-build.sh jasperreports
# 🔄 Läuft aktuell...
```

## Wichtige Erkenntnisse

### DNS-Problem
- **Hostname:** `proxy.gkd-re.local` → ❌ Nicht auflösbar
- **IP-Adresse:** `172.16.28.3` → ✅ Erreichbar
- **Lösung:** Direkte IP-Adresse in allen Konfigurationen verwenden

### SSL-Zertifikatsproblem
- **Problem:** Corporate Proxy modifiziert SSL-Zertifikate
- **Betroffene:** Alpine APK Package Manager
- **Lösung:** HTTP statt HTTPS für Alpine Repositories

### BuildKit-Kompatibilität
- **Problem:** BuildKit hat manchmal Probleme mit Proxies
- **Lösung:** Klassischen Builder verwenden (`DOCKER_BUILDKIT=0`)

## Nächste Schritte

1. ✅ Docker Proxy konfiguriert
2. ✅ Base Images erfolgreich geladen
3. 🔄 JasperReports Container wird gebaut
4. ⏳ Nach Build: Container starten mit `docker compose up -d jasperreports`
5. ⏳ Health Check: `curl http://localhost:8090/health`

## Dateien

### Konfigurationsdateien
- `/etc/systemd/system/docker.service.d/http-proxy.conf`
- `~/.docker/config.json`
- `/etc/docker/daemon.json`
- `config/shared/docker/.env`

### Skripte
- `config/shared/docker/configure-docker-proxy.sh` - Automatisches Setup
- `config/shared/docker/docker-compose-build.sh` - Build-Wrapper
- `config/shared/docker/proxy-quick-ref.sh` - Quick Reference

### Dokumentation
- `config/shared/docker/PROXY-CONFIGURATION.md` - Detaillierte Anleitung
- `config/shared/docker/PROXY-SETUP-SUMMARY.md` - Zusammenfassung
- `config/shared/docker/PROXY-FINAL-CONFIG.md` - Diese Datei

## Support

Bei Problemen:

1. **Proxy-Konfiguration prüfen:**
   ```bash
   sudo systemctl show --property=Environment docker | grep PROXY
   ```

2. **Docker neu starten:**
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl restart docker
   ```

3. **Build-Logs prüfen:**
   ```bash
   tail -f /tmp/jasperreports-build2.log
   ```

---

**Status:** ✅ Proxy-Konfiguration abgeschlossen  
**Build-Status:** 🔄 In Arbeit  
**Datum:** 4. Februar 2026
