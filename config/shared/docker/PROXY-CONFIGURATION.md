# Docker Proxy-Konfiguration für GKD-RE Netzwerk

## Übersicht

Diese Dokumentation beschreibt, wie Docker für den Zugriff auf Docker Hub über den Unternehmens-Proxy konfiguriert wurde.

## Konfigurationsdateien

### 1. `/etc/systemd/system/docker.service.d/http-proxy.conf`
**Zweck:** Proxy-Konfiguration für den Docker-Daemon (Pull/Push von Images)

```ini
[Service]
Environment="HTTP_PROXY=http://gkd-re%5Clinuxupdateuser:Eet9atoo@172.16.28.3:8080"
Environment="HTTPS_PROXY=http://gkd-re%5Clinuxupdateuser:Eet9atoo@172.16.28.3:8080"
Environment="NO_PROXY=localhost,127.0.0.1,*.local,172.16.0.0/12,10.0.0.0/8"
```

### 2. `~/.docker/config.json`
**Zweck:** Proxy-Konfiguration für Docker Client (BuildKit)

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

### 3. `/etc/docker/daemon.json`
**Zweck:** DNS-Konfiguration für Docker-Daemon

```json
{
  "dns": ["8.8.8.8", "8.8.4.4"]
}
```

### 4. `config/shared/docker/.env`
**Zweck:** Proxy-Variablen für Docker Compose Builds

```env
HTTP_PROXY=http://gkd-re%5Clinuxupdateuser:Eet9atoo@172.16.28.3:8080
HTTPS_PROXY=http://gkd-re%5Clinuxupdateuser:Eet9atoo@172.16.28.3:8080
NO_PROXY=localhost,127.0.0.1,*.local,172.16.0.0/12,10.0.0.0/8
```

## Verwendung

### Images pullen
```bash
docker pull postgres:16-alpine
docker pull keycloak/keycloak:latest
```

### Docker Compose Build (empfohlen)
```bash
# Mit Wrapper-Skript (deaktiviert BuildKit automatisch)
./docker-compose-build.sh jasperreports

# Oder manuell
DOCKER_BUILDKIT=0 docker compose build jasperreports
```

### Docker Compose Build (mit BuildKit)
⚠️ **Problem:** BuildKit hat manchmal Probleme mit Proxies

**Workaround:** Deaktiviere BuildKit:
```bash
export DOCKER_BUILDKIT=0
docker compose build
```

## Troubleshooting

### Problem: "dial tcp: i/o timeout" beim Build

**Ursache:** BuildKit verwendet den Proxy nicht korrekt

**Lösung:**
```bash
# 1. BuildKit deaktivieren
export DOCKER_BUILDKIT=0

# 2. Mit klassischem Builder bauen
docker compose build jasperreports
```

### Problem: "failed to fetch anonymous token"

**Ursache:** Docker Daemon erkennt Proxy nicht

**Lösung:**
```bash
# 1. Proxy-Konfiguration prüfen
sudo cat /etc/systemd/system/docker.service.d/http-proxy.conf

# 2. Docker neu starten
sudo systemctl daemon-reload
sudo systemctl restart docker

# 3. Verifizieren
sudo systemctl show --property=Environment docker | grep PROXY
```

### Problem: Maven kann Dependencies nicht laden (im Container-Build)

**Ursache:** Proxy-Variablen werden nicht an Maven weitergegeben

**Lösung:** Bereits in Dockerfile implementiert via ARG/ENV

## Proxy-Details

- **Host:** `172.16.28.3` (IP-Adresse, da `proxy.gkd-re.local` nicht auflösbar ist)
- **Port:** `8080`
- **User:** `gkd-re\linuxupdateuser` (URL-encoded: `gkd-re%5Clinuxupdateuser`)
- **Password:** `Eet9atoo`

**Hinweis:** Ursprünglich war der Hostname `proxy.gkd-re.local` konfiguriert, aber dieser kann vom Docker-Daemon nicht aufgelöst werden. Daher wird die direkte IP-Adresse verwendet.

## Automatisches Setup

Das Skript `configure-docker-proxy.sh` automatisiert die Konfiguration:

```bash
./configure-docker-proxy.sh
```

**Was macht das Skript:**
1. Erstellt `/etc/systemd/system/docker.service.d/http-proxy.conf`
2. Konfiguriert `/etc/docker/daemon.json`
3. Führt `systemctl daemon-reload` aus
4. Startet Docker neu

## Verifizierung

### 1. Prüfe Docker Daemon Proxy-Einstellungen
```bash
sudo systemctl show --property=Environment docker | grep PROXY
```

**Erwartete Ausgabe:**
```
Environment=HTTP_PROXY=http://gkd-re%5Clinuxupdateuser:Eet9atoo@172.16.28.3:8080 HTTPS_PROXY=...
```

### 2. Teste Image Pull
```bash
docker pull hello-world
```

### 3. Teste Docker Compose Build
```bash
cd config/shared/docker
./docker-compose-build.sh jasperreports
```

## Wichtige Hinweise

1. **URL-Encoding:** Der Backslash im Benutzernamen muss als `%5C` kodiert werden
2. **BuildKit:** Manchmal problematisch mit Proxies → klassischen Builder verwenden
3. **NO_PROXY:** Lokale Services (`localhost`, `127.0.0.1`) vom Proxy ausschließen
4. **Maven:** Proxy-Variablen werden automatisch von Maven erkannt (über ENV)

## Weiterführende Dokumentation

- [Docker Proxy Configuration](https://docs.docker.com/config/daemon/systemd/#httphttps-proxy)
- [Docker BuildKit Proxy](https://docs.docker.com/build/building/variables/#http_proxy)
- [Maven Proxy Settings](https://maven.apache.org/guides/mini/guide-proxies.html)

---

**Letzte Aktualisierung:** 4. Februar 2026  
**Verantwortlich:** r-uu
