# CLAUDE.md — JEEERAAAH Project

> Automatisch von Claude geladene Kontextdatei. Für tiefere Details zum jeeeraaah-App-Modul: [`root/app/jeeeraaah/CLAUDE.md`](root/app/jeeeraaah/CLAUDE.md).

---

## Projektzweck

JEEERAAAH ist eine Jakarta EE 10 Enterprise-Aufgabenverwaltung und dient als Referenzimplementierung für modulares Java mit JPMS. Basis für zwei Publikationen:

- **"JPMS in Action – jeeeraaah"** ⚠️ Unvollständig — Keycloak-Kapitel endet abrupt
- **"Modular Software in Java"** ✅ Vollständig

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
ruu-install-fast      # mvn clean install -DskipTests (schnell)
ruu-build-all         # mvn clean install (mit Tests)
```

### 4. Backend starten
```bash
ruu-liberty-dev       # cd .../backend/api/ws_rs && mvn liberty:dev
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
ruu-build-all            # Vollständiger Projekt-Build
ruu-install-fast         # Build ohne Tests
ruu-docker-startup       # Alle Container starten (inkl. Setup)
ruu-docker-up/down       # docker compose up/down
ruu-docker-restart       # Container neu starten
ruu-docker-status        # Container-Status prüfen
ruu-docker-reset         # Docker-Umgebung komplett zurücksetzen
ruu-liberty-dev          # Backend im Dev-Mode starten
ruu-liberty-run          # Backend im Prod-Mode starten
ruu-liberty-stop         # Backend stoppen
ruu-keycloak-setup       # Keycloak Realm/User konfigurieren
ruu-postgres-shell       # psql in jeeeraaah-DB
ruu-postgres-shell-admin # psql als postgres-Admin
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
