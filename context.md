# Projektkontext: jeeeraaah (Java/Jakarta EE Backend & JavaFX Frontend)

> **Hinweis für KI-Agenten:** Diese Datei ist die primäre Kontextquelle für dieses Projekt.
> Sie soll **zu Beginn jeder Sitzung gelesen** und **nach relevanten Änderungen aktualisiert** werden.
> Datum der letzten Aktualisierung bitte stets anpassen.

---

## Metadaten

| Feld | Wert |
| :--- | :--- |
| **Projektname** | jeeeraaah |
| **Typ** | Enterprise-Aufgabenverwaltung (Backend & JavaFX Desktop) |
| **Sprache** | Java (JDK 25 GraalVM) |
| **Frameworks** | Jakarta EE 10, MicroProfile 6.1, JavaFX 25 |
| **Build-System** | Maven 3.9+ |
| **Infrastruktur** | Docker (PostgreSQL, Keycloak, JasperReports) |
| **Laufzeitumgebung** | Open Liberty 25.0.0.12 (WSL Ubuntu) |
| **Letzte Aktualisierung** | 2026-06-01 (Umstellung auf context.md / Gemini Fokus) |

---

## Projektzweck

JEEERAAAH ist eine Jakarta EE 10 Enterprise-Aufgabenverwaltung und dient als Referenzimplementierung für modulares Java mit JPMS. Basis für zwei Publikationen:
- **"JPMS in Action – jeeeraaah"** (Vollständig)
- **"Modular Software in Java"** (Vollständig)

---

## Tech Stack

| Komponente | Technologie | Version |
| :--- | :--- | :--- |
| JDK | GraalVM | 25 |
| App Server | OpenLiberty | 25.0.0.12 |
| Frontend | JavaFX | 25 |
| Datenbank | PostgreSQL | 16 |
| IAM | Keycloak | Docker |
| Reports | JasperReports | Docker |

---

## Maven-Modulstruktur

```
(Workspace-Root)
├── bom/                   Bill of Materials (zentrale Dependency-Verwaltung)
└── root/
    ├── lib/               Wiederverwendbare Bibliotheken (19 Module)
    └── app/
        └── jeeeraaah/     Hauptapplikation (→ context.md in diesem Unterordner)
```

Build-Einstiegspunkt: `root/pom.xml`

---

## Infrastruktur & Startup

Das Backend-Ökosystem lebt in **WSL Ubuntu**.

### Docker-Umgebung
Die Container werden über Aliase oder die `ruu-docker-startup` Funktion verwaltet.
- `postgres-jeeeraaah` (Port 5432)
- `postgres-keycloak`
- `keycloak` (Port 8080)
- `jasperreports` (Port 8090)

### Wichtige Aliase (WSL)
- `ruu-docker-startup`: Startet alle Container.
- `ruu-ol-start`: Startet das Backend im Dev-Mode (`mvn liberty:dev`).
- `ruu-mvn-install-fast`: Schneller Build ohne Tests.

### Run-Konfigurationen (IntelliJ)
Diese Konfigurationen rufen die WSL-Aliase direkt aus der IDE auf (Shell Script Executor):
- **infrastructure – docker startup**: Startet die Docker-Umgebung.
- **backend – liberty:dev**: Startet das Backend (Hot-Reload).

---

## Dev-Credentials

| Service | Benutzer | Passwort |
| :--- | :--- | :--- |
| Frontend-Login | `testuser` | `testpassword` |
| Keycloak Admin | `admin` | `admin` |
| PostgreSQL jeeeraaah | `jeeeraaah` | `jeeeraaah` |

---

## Service-URLs

| Service | URL |
| :--- | :--- |
| Backend API | http://localhost:9080/jeee-raaah/ |
| OpenAPI UI | http://localhost:9080/openapi/ui |
| Keycloak Token | http://localhost:8080/realms/jeeeraaah-realm/protocol/openid-connect/token |

---

## Offene Prioritäten (Stand 2026-06-01)

1. **Vollständigkeit:** Keycloak-Kapitel in "JPMS in Action" abschließen.
2. **Synchronität:** API-Pfade mit dem neuen Kotlin CMP-Frontend abgleichen.
3. **CI/CD:** GitHub Actions aufsetzen.
4. **Tests:** 3 Integration-Tests aktivieren (benötigen laufendes Backend).

---

## Verwandte Projekte

| Projekt | Pfad |
| :--- | :--- |
| **Kotlin CMP Frontend** | `C:\Users\r-uu\develop\github\kotlin\cmp\main\app\jeeeraaah` |
