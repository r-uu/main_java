# CLAUDE.md — JEEERAAAH Project

> Automatisch von Claude geladene Kontextdatei. Für tiefere Details zum jeeeraaah-App-Modul: [`root/app/jeeeraaah/CLAUDE.md`](root/app/jeeeraaah/CLAUDE.md).

---

## Projektzweck

JEEERAAAH ist eine Jakarta EE 10 Enterprise-Aufgabenverwaltung und dient als Referenzimplementierung für modulares Java mit JPMS. Basis für zwei Publikationen:

- **"JPMS in Action – jeeeraaah"** ✅ Vollständig
- **"Modular Software in Java"**   ✅ Vollständig

---

## Tech Stack

| Komponente      | Technologie            | Version   |
|-----------------|------------------------|-----------|
| JDK             | GraalVM                | 25        |
| App Server      | OpenLiberty            | 25.0.0.12 |
| Jakarta EE      | Jakarta EE             | 10.0      |
| MicroProfile    | MicroProfile           | 6.1       |
| Frontend        | JavaFX                 | 25        |
| Datenbank       | PostgreSQL             | 16        |
| IAM             | Keycloak               | (Docker)  |
| Reports         | JasperReports          | (Docker)  |
| Build           | Maven                  | 3.9+      |
| Modulsystem     | JPMS                   | —         |
| ORM             | Hibernate (via Liberty)| —         |
| Bean Mapping    | MapStruct              | —         |

---

## Maven-Modulstruktur

```
(Workspace-Root)
├── bom/                   Bill of Materials (zentrale Dependency-Verwaltung)
└── root/
    ├── lib/               Wiederverwendbare Bibliotheken (19 Module)
    └── app/
        └── jeeeraaah/     Hauptapplikation (→ root/app/jeeeraaah/CLAUDE.md)
```

Build-Einstiegspunkt: `root/pom.xml`

---

## Quick Start

### Voraussetzungen
Java 25 (GraalVM), Maven 3.9+, Docker

### 1. Aliases laden (einmalig pro Shell-Session)
```bash
source config/shared/wsl/aliases.sh
```

### 2. Docker-Umgebung starten
```bash
ruu-docker-startup    # startet postgres-jeeeraaah, postgres-keycloak, keycloak, jasperreports
```

### 3. Projekt bauen
```bash
ruu-mvn-install-fast  # mvn clean install -DskipTests (schnell)
ruu-mvn-build-all     # mvn clean install (mit Tests)
```

### 4. Backend starten
```bash
ruu-ol-start          # cd .../backend/api/ws_rs && mvn liberty:dev
# oder VS Code Task: "🚀 Liberty: Start (Dev Mode)"
```

### 5. Frontend starten
```bash
# Via VS Code Tasks:
# "🚀 Frontend: Start MainApp"   → MainApp
# "🎨 Frontend: Start DashApp"   → DashApp
# "🎩 Frontend: Start GanttApp"  → GanttApp
# Oder IntelliJ: Run Configuration "DashAppRunner" / "GanttAppRunner"
```

---

## Dev-Credentials (nur lokal, nicht im Git)

| Service              | Benutzer       | Passwort      |
|----------------------|----------------|---------------|
| Frontend-Login       | `testuser`     | `testpassword`|
| Keycloak Admin       | `admin`        | `admin`       |
| PostgreSQL jeeeraaah | `jeeeraaah`    | `jeeeraaah`   |
| PostgreSQL keycloak  | `keycloak`     | `keycloak`    |
| PostgreSQL lib_test  | `lib_test`     | `lib_test`    |

Echte Credentials: `config/shared/docker/.env` (nicht in Git)  
Template: `config/shared/docker/.env.template`

---

## Service-URLs

| Service               | URL                                                                            |
|-----------------------|--------------------------------------------------------------------------------|
| Backend API           | http://localhost:9080/jeee-raaah/                                              |
| OpenAPI UI            | http://localhost:9080/openapi/ui                                               |
| Keycloak Admin        | http://localhost:8080/admin                                                    |
| Keycloak Token        | http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token     |
| JasperReports Health  | http://localhost:8090/health                                                   |
| Backend Health        | http://localhost:9080/health                                                   |

Keycloak Realm: `jeeeraaah-realm` | Client-ID: `jeeeraaah-frontend`

---

## Wichtige Aliases

```bash
# Build (ruu-mvn-*)
ruu-mvn-build-all        # Vollständiger Projekt-Build
ruu-mvn-install-fast     # Build ohne Tests

# Docker Stack (ruu-docker-*)
ruu-docker-startup       # Alle Container starten (inkl. Setup)
ruu-docker-up/down       # docker compose up/down
ruu-docker-restart       # Container neu starten
ruu-docker-status        # Container-Status prüfen
ruu-docker-reset         # Docker-Umgebung komplett zurücksetzen

# Open Liberty Backend (ruu-ol-*)
ruu-ol-start             # Backend im Dev-Mode starten (liberty:dev)
ruu-ol-run               # Backend im Prod-Mode starten
ruu-ol-stop              # Backend stoppen

# Keycloak (ruu-kc-*)
ruu-kc-setup             # Keycloak Realm/User konfigurieren
ruu-kc-reset             # Container + Realm komplett zurücksetzen

# PostgreSQL (ruu-pg-*)
ruu-pg-shell             # psql in jeeeraaah-DB
ruu-pg-shell-admin       # psql als postgres-Admin

# Greenbone (ruu-gb-*)
ruu-gb-up/down           # Greenbone Vulnerability Scanner starten/stoppen
ruu-gb-status            # Greenbone Container-Status

# Navigation (ruu-cd-*)
ruu-cd-home              # Wechsel zu $RUU_MAIN
ruu-cd-root              # Wechsel zu root/

# Git (ruu-git-*)
ruu-git-status / pull / push / log

# Hilfe
ruu-help                 # Alle Aliase auflisten
ruu-groups               # Übersicht aller Alias-Gruppen
```

---

## Troubleshooting

| Problem                         | Lösung                                              |
|---------------------------------|-----------------------------------------------------|
| Docker startet nicht            | `ruu-docker-reset`                                  |
| Port 9080 belegt                | `lsof -i :9080` → `kill -9 <PID>`                  |
| Auth schlägt fehl / Realm fehlt | `ruu-keycloak-setup`                               |
| Build-Cache-Problem             | `rm -rf ~/.m2/repository/r-uu && ruu-install-fast` |
| VS Code zeigt rote Fehler       | Erwartetes Verhalten — JPMS+WAR-Modul hat classpath-Konflikte in VS Code. Maven-Build ist maßgeblich. |
| "database does not exist"       | `ruu-postgres-shell-admin` → DB manuell anlegen    |
| "Realm does not exist"          | `ruu-keycloak-setup`                               |
| Vollständiger Reset             | `ruu-docker-reset && rm -rf ~/.m2/repository/r-uu && ruu-keycloak-setup` |

---

## Shell-Konfiguration & Maschinenübergreifende Synchronisation

### Architektur
- **Aliase:** ausschließlich in `config/shared/wsl/aliases.sh` (versioniert) — niemals Aliase direkt in `.bashrc` definieren
- **`.bashrc` selbst:** lebt als versionierte Datei in `config/shared/wsl/.bashrc`; `~/.bashrc` ist ein Symlink darauf
- Git selbst ist das Backup — keine separate Backup-Datei nötig

### Einmalig einrichten (pro Maschine)
```bash
ln -sf ~/develop/github/main/config/shared/wsl/.bashrc ~/.bashrc
```
Danach synchronisiert `git pull/push` `.bashrc` automatisch zwischen allen Maschinen.

### Regel für Claude: .bashrc-Änderungen
`~/.bashrc` ist ein Symlink → Änderungen direkt in `config/shared/wsl/.bashrc` vornehmen. Kein manuelles Kopieren nötig.

### Bekannte Einschränkung
Installer (sdkman, nvm, conda) die `~/.bashrc` **ersetzen** statt ergänzen, löschen den Symlink. Danach einfach neu anlegen: `ln -sf ~/develop/github/main/config/shared/wsl/.bashrc ~/.bashrc`

---

## Offene Prioritäten (Stand 2026-03-01)

1. **KRITISCH:** Keycloak-Kapitel in "JPMS in Action" vervollständigen  
   → `root/app/jeeeraaah/doc/md/jpms in action - jeeeraaah/jpms in action - jeeeraaah.md`
2. Compiler-Warnungen in `DashController.java` beheben
3. Startup-Guides konsolidieren (`GETTING-STARTED.md` + `config/QUICK-COMMANDS.md`)
4. Credentials-Dokumentation zusammenführen (3 Dateien → 1)
5. Unit-Tests für Task-Hierarchie-Randfälle ergänzen
6. CI/CD-Pipeline aufsetzen (GitHub Actions)
7. ArchUnit-Tests für Layer-Grenzen implementieren

---

## Schlüssel-Dokumentation

| Datei                              | Inhalt                                      |
|------------------------------------|---------------------------------------------|
| `README.md`                        | Projektübersicht & Ultra-Quick-Start        |
| `GETTING-STARTED.md`               | Vollständiger Einrichtungsleitfaden         |
| `JPMS-REFERENCE.md`                | JPMS-Patterns und Konventionen              |
| `IAM-KEYCLOAK-LIBERTY-GUIDE.md`    | Keycloak/Security-Setup & JWT              |
| `API-DOCUMENTATION.md`             | REST API & Authentifizierung               |
| `VSCODE-TASKS-GUIDE.md`            | VS Code Tasks Referenz                     |
| `BUILD-TROUBLESHOOTING.md`         | Build-Fehler-Lösungen                      |
| `config/CREDENTIALS.md`            | Credentials-Referenz                       |
| `config/QUICK-COMMANDS.md`         | Tägliche Befehls-Cheat-Sheet               |
| `config/TROUBLESHOOTING.md`        | Allgemeine Problemlösung                   |
| `todo.md`                          | Aktuelle Aufgabenliste                     |
| `root/app/jeeeraaah/CLAUDE.md`     | App-spezifischer Kontext (Architektur etc.)|
