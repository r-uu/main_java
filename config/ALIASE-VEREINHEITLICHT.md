# ✅ Aliase Vereinheitlicht und Aufgeräumt

**Datum:** 2026-01-18  
**Status:** ✅ **ABGESCHLOSSEN**

---

## 🎯 ÄNDERUNGEN

Alle Aliase wurden vereinheitlicht und mit konsequentem **`ruu-`** Präfix versehen.

---

## 📝 VORHER / NACHHER

### ❌ Entfernt (inkonsistent):

```bash
# Alte kurze Aliase ohne ruu- Präfix
alias cdruu='cd $RUU_HOME'
alias cdbom='cd $RUU_BOM'
alias cdroot='cd $RUU_ROOT'
alias cdlib='cd $RUU_ROOT/lib'
alias cdapp='cd $RUU_ROOT/app'

# Inkonsistente Build-Funktion
build-all() { ... }
alias ruu-build='build-all'
alias ruu-build-all='build-all'

# Veraltete/komplexe Aliase
alias ruu-docker-restart-clean='bash $RUU_DOCKER/restart-with-fixes.sh'
alias ruu-docker-cleanup='docker stop jasperreports-service ruu-keycloak ruu-postgres 2>/dev/null; ...'
alias ruu-docker-system-prune='sudo docker system prune --all --volumes --force && ...'
alias ruu-postgres-service-* # Lokale PostgreSQL (nicht mehr benötigt)
alias ruu-postgres-docker-* # Veraltete Container-Namen
alias ruu-postgres-setup # Veraltetes Skript

# Jasper ohne ruu- Präfix
alias jasper='bash $RUU_JASPER/jasper-service.sh'
alias jasper-start='...'
alias jasper-stop='...'
# etc.

# Nicht mehr benötigte Tools
alias ruu-native-image='native-image'
alias ruu-gu='gu'
alias ruu-java-check='bash $RUU_CONFIG/shared/scripts/check-java.sh'
```

### ✅ Neu (konsistent):

```bash
# Alle Aliase mit ruu- Präfix
alias ruu-home='cd $RUU_HOME'
alias ruu-bom='cd $RUU_BOM'
alias ruu-root='cd $RUU_ROOT'
alias ruu-lib='cd $RUU_ROOT/lib'
alias ruu-app='cd $RUU_ROOT/app'
alias ruu-config='cd $RUU_CONFIG'
alias ruu-docker='cd $RUU_DOCKER'
alias ruu-jasper='cd $RUU_JASPER'  # NEU

# Konsistente Build-Funktion
ruu-build-all() { ... }  # Funktion mit ruu- Präfix
alias ruu-build='ruu-build-all'

# Vereinfachte Docker-Aliase
alias ruu-docker-cleanup='docker container prune -f && docker volume prune -f && ...'
alias ruu-docker-reset='bash $RUU_DOCKER/reset-all-containers.sh'
alias ruu-docker-daemon-restart='sudo service docker restart'  # NEU

# PostgreSQL - nur Docker (bereinigt)
alias ruu-postgres-start='cd $RUU_DOCKER && docker compose up -d postgres-jeeeraaah'
alias ruu-postgres-stop='docker container stop postgres-jeeeraaah'
alias ruu-postgres-restart='ruu-postgres-stop && ruu-postgres-start'
alias ruu-postgres-logs='docker logs -f postgres-jeeeraaah'
alias ruu-postgres-shell='docker exec -it postgres-jeeeraaah psql -U r_uu -d jeeeraaah'
alias ruu-postgres-shell-admin='docker exec -it postgres-jeeeraaah psql -U postgres'

# JasperReports - mit ruu- Präfix
alias ruu-jasper-start='cd $RUU_DOCKER && docker compose up -d jasperreports'
alias ruu-jasper-stop='docker container stop jasperreports-service'
alias ruu-jasper-restart='ruu-jasper-stop && ruu-jasper-start'
alias ruu-jasper-logs='docker logs -f jasperreports-service'
alias ruu-jasper-shell='docker exec -it jasperreports-service sh'
alias ruu-jasper-rebuild='cd $RUU_DOCKER && docker compose build jasperreports && ...'
alias ruu-jasper-test='curl http://localhost:8090/health'

# Neue Versions-Übersicht
alias ruu-versions='echo "=== Tool Versionen ===" && ruu-java-version && ...'

# Erweiterte Hilfe
alias ruu-help-git='echo "=== Git Aliase ===" && ruu-help | grep git'
alias ruu-aliases-edit='${EDITOR:-nano} $RUU_HOME/config/shared/wsl/aliases.sh'
```

---

## 🗂️ KATEGORIEN (Neu Organisiert)

### 1. Navigation (8 Aliase)
```bash
ruu-home, ruu-bom, ruu-root, ruu-lib, ruu-app, 
ruu-config, ruu-docker, ruu-jasper
```

### 2. Maven Build (9 Aliase)
```bash
ruu-build, ruu-build-all, ruu-clean, ruu-install, 
ruu-install-fast, ruu-test, ruu-verify,
ruu-bom-install, ruu-root-install, ruu-lib-install, ruu-app-install
```

### 3. Docker - Daemon (4 Aliase)
```bash
ruu-docker-daemon-start, ruu-docker-daemon-stop,
ruu-docker-daemon-status, ruu-docker-daemon-restart
```

### 4. Docker - Services (7 Aliase)
```bash
ruu-docker-up, ruu-docker-down, ruu-docker-restart,
ruu-docker-logs, ruu-docker-ps, ruu-docker-cleanup, ruu-docker-reset
```

### 5. Docker - PostgreSQL (6 Aliase)
```bash
ruu-postgres-start, ruu-postgres-stop, ruu-postgres-restart,
ruu-postgres-logs, ruu-postgres-shell, ruu-postgres-shell-admin
```

### 6. Docker - Keycloak (5 Aliase)
```bash
ruu-keycloak-start, ruu-keycloak-stop, ruu-keycloak-restart,
ruu-keycloak-logs, ruu-keycloak-admin
```

### 7. Docker - JasperReports (7 Aliase)
```bash
ruu-jasper-start, ruu-jasper-stop, ruu-jasper-restart,
ruu-jasper-logs, ruu-jasper-shell, ruu-jasper-rebuild, ruu-jasper-test
```

### 8. Git (7 Aliase)
```bash
ruu-status, ruu-pull, ruu-push, ruu-log,
ruu-diff, ruu-branches, ruu-git-fix
```

### 9. System & Monitoring (4 Aliase)
```bash
ruu-ports, ruu-disk, ruu-tree, ruu-tree-full
```

### 10. Versionen (5 Aliase)
```bash
ruu-java-version, ruu-maven-version, ruu-docker-version,
ruu-graalvm-version, ruu-versions
```

### 11. Shell (3 Aliase)
```bash
ruu-shell-reset, ruu-aliases-reload, ruu-aliases-edit
```

### 12. Hilfe (8 Aliase)
```bash
ruu-help, ruu-help-docker, ruu-help-maven, ruu-help-nav,
ruu-help-git, ruu-docs, ruu-quickstart
```

**GESAMT:** ~63 Aliase (alle mit `ruu-` Präfix!)

---

## ✅ VORTEILE

1. **Konsistenz:** Alle Projekt-Aliase beginnen mit `ruu-`
2. **Autocomplete:** Einfach `ruu-<TAB>` drücken für alle Aliase
3. **Klarheit:** Sofort erkennbar, dass es zum r-uu Projekt gehört
4. **Keine Kollisionen:** Kein Konflikt mit System-Befehlen
5. **Einfache Wartung:** Logische Gruppierung nach Funktion
6. **Bessere Hilfe:** Kategorisierte Help-Befehle

---

## 🚀 VERWENDUNG

### Aliase neu laden:
```bash
source ~/.bashrc
# oder
ruu-aliases-reload
```

### Alle Aliase anzeigen:
```bash
ruu-help
```

### Kategorisierte Hilfe:
```bash
ruu-help-docker    # Zeigt nur Docker-Aliase
ruu-help-maven     # Zeigt nur Maven-Aliase
ruu-help-nav       # Zeigt nur Navigation-Aliase
ruu-help-git       # Zeigt nur Git-Aliase
```

### Aliase bearbeiten:
```bash
ruu-aliases-edit
```

---

## 📋 MIGRATION

### Alt → Neu:

| Alt | Neu |
|-----|-----|
| `cdruu` | `ruu-home` |
| `cdbom` | `ruu-bom` |
| `cdroot` | `ruu-root` |
| `cdlib` | `ruu-lib` |
| `cdapp` | `ruu-app` |
| `build-all` | `ruu-build` oder `ruu-build-all` |
| `jasper-start` | `ruu-jasper-start` |
| `jasper-stop` | `ruu-jasper-stop` |
| `jasper-logs` | `ruu-jasper-logs` |
| - | `ruu-jasper` (Navigation) |

---

## 🧹 AUFGERÄUMT

### Entfernte Aliase (nicht mehr benötigt):

- ❌ `ruu-postgres-service-*` - Lokale PostgreSQL (nur Docker jetzt)
- ❌ `ruu-postgres-docker-setup` - Manuelle DB-Einrichtung
- ❌ `ruu-postgres-docker-shell*` - Ersetzt durch `ruu-postgres-shell*`
- ❌ `ruu-postgres-rebuild` - Nicht mehr benötigt
- ❌ `ruu-docker-system-prune` - Zu aggressiv, durch cleanup ersetzt
- ❌ `ruu-docker-restart-clean` - Veraltetes Skript
- ❌ `ruu-native-image`, `ruu-gu` - Generische Tools
- ❌ `ruu-java-check` - Veraltetes Skript
- ❌ `ruu-postgres-setup` - Veraltetes Skript
- ❌ Alle `jasper-*` ohne `ruu-` Präfix

### Vereinfachte Aliase:

- ✅ `ruu-docker-cleanup` - Jetzt nur noch container/volume prune
- ✅ `ruu-docker-reset` - Nutzt dediziertes Reset-Skript
- ✅ PostgreSQL-Aliase - Nur Docker, keine lokale Installation mehr

---

## ✅ CHECKLISTE

- [x] Alle Aliase haben `ruu-` Präfix
- [x] Logische Gruppierung nach Funktion
- [x] Konsistente Benennung (z.B. `*-start`, `*-stop`, `*-restart`)
- [x] Veraltete Aliase entfernt
- [x] Duplikate entfernt
- [x] Hilfe-Funktionen kategorisiert
- [x] Export-Variablen am Anfang
- [x] Git-Kompatibilität am Ende
- [x] Kommentare für alle Sektionen

---

## 📚 SIEHE AUCH

- `config/shared/wsl/aliases.sh` - Die Aliase-Datei
- `~/.bashrc` - Sollte die Datei sourcen
- `ruu-help` - Zeigt alle verfügbaren Aliase

---

✅ **Aliase erfolgreich vereinheitlicht!**  
✅ **Alle Projekt-Aliase mit `ruu-` Präfix!**  
✅ **Konsistente und logische Struktur!**

