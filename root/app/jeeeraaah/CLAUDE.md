# CLAUDE.md — jeeeraaah Application

> Für allgemeinen Projekt-Kontext (Build, Docker, Credentials, Aliases): [`../../CLAUDE.md`](../../CLAUDE.md)

---

## Applikations-Architektur

3-schichtige Maven-Multi-Modul-Applikation:

```
jeeeraaah/
├── common/                  Gemeinsame API-Verträge
│   └── api/
│       ├── bean/            Data Transfer Objects (DTOs)
│       ├── domain/          Domain-Interfaces (TaskService, TaskGroup, Task)
│       ├── mapping/         Mapping-Interfaces
│       └── ws_rs/           JAX-RS-Vertragsinterfaces
│
├── backend/                 OpenLiberty Server-Side
│   ├── common/
│   │   └── mapping_jpa_dto/ JPA-Entity ↔ DTO Mapping (MapStruct)
│   ├── persistence/
│   │   └── jpa/             JPA-Entities, Repositories, persistence.xml
│   └── api/
│       ├── open-liberty/
│       │   └── shared/      Geteilte Liberty-Konfiguration
│       └── ws_rs/           WAR-Modul — REST API Endpoint (deployed auf Liberty)
│
└── frontend/                JavaFX Desktop-Applikation
    ├── common/
    │   └── mapping_bean_fxbean/  DTO ↔ JavaFX Bean Mapping (MapStruct)
    ├── api_client/
    │   └── ws_rs/           Jersey REST-Client (ruft Backend auf)
    └── ui/
        ├── fx/              Haupt-Frontend-Modul (alle AppRunner)
        ├── fx_model/        JavaFX Observable Models
        └── fx_executable/   Executable JAR
```

---

## Datenmodell

```
TaskGroup (1) ──── (*) Task
```

- **TaskGroup**: Fasst Tasks zusammen. Ein Task gehört immer zu genau einer TaskGroup.
- **Task**: Zentrales Geschäftsobjekt mit folgenden Beziehungen:
  - **Super-/Sub-Tasks**: Aufgaben-Hierarchie (Eltern/Kinder)
  - **Predecessors/Successors**: Abhängigkeitsreihenfolge (Vorgänger/Nachfolger)

Paket-Basis: `de.ruu.app.jeeeraaah.*`

---

## Backend

| Eigenschaft        | Wert                                     |
|--------------------|------------------------------------------|
| Context Root       | `/jeee-raaah`                            |
| Port               | `9080`                                   |
| Liberty Features   | `jakartaee-10.0`, `microProfile-6.1`, `mpJwt-2.1`, `appSecurity-5.0` |
| JNDI DataSource    | `jdbc/datasource_postgresql`             |
| Persistence Provider | Hibernate (via `persistenceContainer-3.1`) |
| Konfiguration      | `backend/api/ws_rs/src/main/liberty/config/server.xml` |
| MicroProfile Config| `backend/api/ws_rs/src/main/resources/META-INF/microprofile-config.properties` |

### Backend starten
```bash
cd root/app/jeeeraaah/backend/api/ws_rs
mvn liberty:dev    # Dev-Mode (Hot-Reload, Strg+C zum Beenden)
# oder:
ruu-liberty-dev
```

### WAR-Modul & JPMS
Das WAR-Modul (`backend/api/ws_rs`) hat **bewusst keine `module-info.java`** — WAR-Dateien laufen im Liberty-Classpath, JPMS greift hier nicht. VS Code zeigt hier falsche Fehlermeldungen; der Maven-Build ist maßgeblich.

---

## Frontend

Alle lauffähigen Apps liegen in `frontend/ui/fx/src/main/java/de/ruu/app/jeeeraaah/frontend/ui/fx/`:

| AppRunner                                  | Beschreibung                          |
|--------------------------------------------|---------------------------------------|
| `MainAppRunner`                            | Haupt-Einstiegspunkt                  |
| `DashAppRunner` *(ehemaliger Hauptfokus)*  | Task-Management-Dashboard             |
| `GanttAppRunner`                           | Gantt-Diagramm                        |
| `TaskEditorAppRunner`                      | Task bearbeiten                       |
| `TaskViewAppRunner`                        | Task anzeigen                         |
| `TaskHierarchyPredecessorsAppRunner`       | Vorgänger-Hierarchie                  |
| `TaskHierarchySuccessorsAppRunner`         | Nachfolger-Hierarchie                 |
| `TaskHierarchySuperSubTasksAppRunner`      | Super-/Sub-Task-Hierarchie            |
| `TaskListDirectNeighboursAppRunner`        | Liste direkter Nachbarn               |
| `ConfiguratorAppRunner` (2x)               | Konfigurator für Hierarchie-Relationen|

### Frontend starten
```bash
cd root/app/jeeeraaah/frontend/ui/fx
mvn exec:java           # DashApp
mvn exec:java@gantt     # GanttApp
mvn exec:java@main      # MainApp
# oder VS Code Tasks:
# "🚀 Frontend: Start MainApp"
# "🎨 Frontend: Start DashApp"
# "🎩 Frontend: Start GanttApp"
```

Login: `testuser` / `testpassword`

---

## JPMS-Konventionen

Alle Module außer dem WAR-Backend haben `module-info.java`.

Paket-Muster: `de.ruu.app.jeeeraaah.<layer>.<sublayer>`

Modul-Benennung entspricht den Artifact-IDs, z. B.:
- `de.ruu.app.jeeeraaah.common.api.domain`
- `de.ruu.app.jeeeraaah.backend.persistence.jpa`
- `de.ruu.app.jeeeraaah.frontend.ui.fx`

**Exportregeln:**
- Nur API-Pakete werden exportiert (`exports`)
- Interne Pakete (`internal/`) werden **nicht** exportiert
- Für MapStruct: Qualified Exports an `org.mapstruct` Prozessor-Modul

Facade-Pattern (in diesem Projekt verwendet):
```java
module mapping.module {
    exports mapping.module;                            // Facade
    exports mapping.module.jpa.dto to org.mapstruct;  // Nur für MapStruct
    // mapping.module.internal bleibt versteckt
}
```

Details und Troubleshooting: [`JPMS-REFERENCE.md`](../../JPMS-REFERENCE.md)

---

## Publikationen

In `root/app/jeeeraaah/doc/md/`:

| Dokument                          | Status                                           |
|-----------------------------------|--------------------------------------------------|
| `jpms in action - jeeeraaah/`     | ⚠️ **UNVOLLSTÄNDIG** — Keycloak-Kapitel endet abrupt bei "The Server Side" |
| `modular-software-in-java/`       | ✅ Vollständig                                   |

**Kritische offene Aufgabe:** Keycloak-Kapitel in "JPMS in Action" vervollständigen.

---

## Lib-Module (Übersicht)

In `root/lib/` — wiederverwendbare Bibliotheken, die auch außerhalb von jeeeraaah verwendbar sind:

| Modul              | Beschreibung                          |
|--------------------|---------------------------------------|
| `keycloak_admin`   | Keycloak Admin & Setup                |
| `jpa/`             | JPA-Kern, MapStruct-Integration, Hibernate/PostgreSQL |
| `fx/`              | JavaFX Komponenten-Framework          |
| `office/word/jasperreports/` | JasperReports Client & Server |
| `docker_health`    | Docker Health-Check-Utilities         |
| `util`             | Allgemeine Hilfsmethoden              |
| `mp_config`        | MicroProfile Config Utilities         |
| `cdi`              | CDI-Erweiterungen                     |
| `jackson`          | JSON-Verarbeitung                     |
| `jdbc`             | JDBC-Utilities                        |
| `jsonb`            | JSON Binding                          |
| `junit`            | Test-Utilities                        |
| `mapstruct`        | Bean Mapping SPI                      |
| `ws_rs`            | JAX-RS Utilities                      |
| `archunit`         | Architektur-Tests                     |
| `postgres_util_ui` | PostgreSQL UI-Utilities               |

Paket-Basis Libs: `de.ruu.lib.*`
