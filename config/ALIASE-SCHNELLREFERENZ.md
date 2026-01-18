
# 🚀 r-uu Aliase - Schnellreferenz

**Alle Aliase mit `ruu-` Präfix | Autocomplete: `ruu-<TAB>`**

---

## 📍 Navigation

```bash
ruu-home         # → /home/r-uu/develop/github/main
ruu-bom          # → main/bom
ruu-root         # → main/root
ruu-lib          # → main/root/lib
ruu-app          # → main/root/app
ruu-config       # → main/config
ruu-docker       # → main/config/shared/docker
ruu-jasper       # → main/root/sandbox/office/microsoft/word/jasperreports
```

---

## 🔨 Maven Build

```bash
ruu-build               # Kompletter Build (empfohlen!)
ruu-build-all           # Alias für ruu-build
ruu-install             # mvn clean install
ruu-install-fast        # mvn clean install -DskipTests
ruu-clean               # mvn clean
ruu-test                # mvn test
ruu-verify              # mvn verify

# Einzelne Module
ruu-bom-install         # BOM bauen
ruu-root-install        # Root bauen
ruu-lib-install         # Alle Libraries
ruu-app-install         # Alle Apps
```

---

## 🐳 Docker

### Daemon
```bash
ruu-docker-daemon-start     # Docker starten
ruu-docker-daemon-stop      # Docker stoppen
ruu-docker-daemon-restart   # Docker neu starten
ruu-docker-daemon-status    # Docker Status
```

### Services (alle)
```bash
ruu-docker-up          # Alle Container starten
ruu-docker-down        # Alle Container stoppen
ruu-docker-restart     # Alle Container neu starten
ruu-docker-logs        # Alle Logs anzeigen
ruu-docker-ps          # Container-Status (formatiert)
ruu-docker-cleanup     # Ungenutzte Container/Volumes löschen
ruu-docker-reset       # Kompletter Reset mit Backup
```

### PostgreSQL
```bash
ruu-postgres-start          # PostgreSQL starten
ruu-postgres-stop           # PostgreSQL stoppen
ruu-postgres-restart        # PostgreSQL neu starten
ruu-postgres-logs           # PostgreSQL Logs
ruu-postgres-shell          # psql als r_uu in jeeeraaah DB
ruu-postgres-shell-admin    # psql als postgres (admin)
```

### Keycloak
```bash
ruu-keycloak-start     # Keycloak starten
ruu-keycloak-stop      # Keycloak stoppen
ruu-keycloak-restart   # Keycloak neu starten
ruu-keycloak-logs      # Keycloak Logs
ruu-keycloak-admin     # Zeigt Admin-URL
```

### JasperReports
```bash
ruu-jasper-start       # JasperReports starten
ruu-jasper-stop        # JasperReports stoppen
ruu-jasper-restart     # JasperReports neu starten
ruu-jasper-logs        # JasperReports Logs
ruu-jasper-shell       # Shell im Container
ruu-jasper-rebuild     # Image neu bauen & starten
ruu-jasper-test        # Health-Check
```

---

## 📦 Git

```bash
ruu-status         # git status
ruu-pull           # git pull
ruu-push           # git push
ruu-log            # git log (graph, letzten 20)
ruu-diff           # git diff
ruu-branches       # git branch -a
ruu-git-fix        # WSL/IntelliJ Git-Probleme beheben
```

---

## 🔧 System & Monitoring

```bash
ruu-ports          # Offene Ports anzeigen
ruu-disk           # Disk-Usage
ruu-tree           # Projekt-Struktur (3 Ebenen)
ruu-tree-full      # Komplette Struktur
```

---

## ℹ️ Versionen & Info

```bash
ruu-java-version       # Java-Version
ruu-maven-version      # Maven-Version
ruu-docker-version     # Docker-Version
ruu-graalvm-version    # GraalVM-Version + Path
ruu-versions           # Alle Versionen auf einmal
```

---

## 🔄 Shell & Aliase

```bash
ruu-shell-reset        # Shell neu laden (clear + exec)
ruu-aliases-reload     # Aliase neu laden (source ~/.bashrc)
ruu-aliases-edit       # Aliase bearbeiten
```

---

## ❓ Hilfe

```bash
ruu-help               # Alle Aliase anzeigen
ruu-help-docker        # Nur Docker-Aliase
ruu-help-maven         # Nur Maven-Aliase
ruu-help-nav           # Nur Navigation-Aliase
ruu-help-git           # Nur Git-Aliase
ruu-docs               # Dokumentation anzeigen
ruu-quickstart         # Quickstart-Guide
```

---

## 💡 Tipps

### Autocomplete nutzen:
```bash
ruu-<TAB><TAB>     # Zeigt alle ruu-Aliase
ruu-docker-<TAB>   # Zeigt alle Docker-Aliase
```

### Schnellster Build:
```bash
ruu-build          # Nutzt build-all.sh Skript
```

### Container-Status prüfen:
```bash
ruu-docker-ps      # Schön formatierte Tabelle
```

### Logs live verfolgen:
```bash
ruu-postgres-logs  # PostgreSQL
ruu-keycloak-logs  # Keycloak
ruu-jasper-logs    # JasperReports
ruu-docker-logs    # Alle zusammen
```

### In Container Shell springen:
```bash
ruu-postgres-shell       # PostgreSQL (als r_uu)
ruu-postgres-shell-admin # PostgreSQL (als admin)
ruu-jasper-shell         # JasperReports
```

---

## 🎯 Häufige Workflows

### Kompletter Build:
```bash
ruu-home
ruu-build
```

### Container neu starten:
```bash
ruu-docker-restart
```

### Nur PostgreSQL neu starten:
```bash
ruu-postgres-restart
```

### Logs checken:
```bash
ruu-docker-ps        # Status
ruu-postgres-logs    # PostgreSQL Details
```

### Projekt aufräumen:
```bash
ruu-clean
ruu-docker-cleanup
```

---

## 📚 Siehe auch

- `ruu-help` - Vollständige Aliase-Liste
- `config/ALIASE-VEREINHEITLICHT.md` - Detaillierte Dokumentation
- `config/shared/wsl/aliases.sh` - Die Aliase-Datei

---

**Alle Aliase beginnen mit `ruu-` - einfach zu merken und zu finden!** ✅

