# jeeeraaah - A Task Management System built on JPMS

Enterprise Task-Management-System mit JavaFX Frontend und Open Liberty Backend.

## Übersicht

jeeeraaah ist eine JPMS-basierte Modulith-Anwendung für Task- und TaskGroup-Verwaltung mit:
- Keycloak-Authentifizierung
- Rollen-basierter Autorisierung
- REST API (JAX-RS)
- JPA/Hibernate Persistence
- JavaFX Desktop UI

## Modulstruktur

```
jeeeraaah/
├── backend/                    # Server-Komponenten
│   ├── api/ws_rs/              # REST API (Open Liberty)
│   ├── persistence/            # JPA Entities & Repositories
│   └── common/                 # Backend-gemeinsame Klassen
├── frontend/                   # Client-Komponenten
│   ├── api.client/ws_rs/       # REST Client
│   ├── ui/fx/                  # JavaFX UI
│   └── common/                 # Frontend-gemeinsame Klassen
├── common/api/                 # API Domain Models (geteilt)
└── common/mapping/             # MapStruct Mappings

```

## Starten

### Backend (Open Liberty)
```bash
mvn liberty:dev -pl app/jeeeraaah/backend/api/ws_rs
```

**Zugriff:** http://localhost:9080/jeeeraaah

### Frontend (JavaFX DashApp)
```bash
mvn exec:java -pl app/jeeeraaah/frontend/ui/fx \
  -Dexec.mainClass=de.ruu.app.jeeeraaah.frontend.ui.fx.dash.DashAppRunner
```

## Voraussetzungen

- PostgreSQL (via Docker Compose)
- Keycloak (via Docker Compose)

Siehe [Infrastruktur-Dokumentation](../../docs/infrastruktur/README.md)

## Dokumentation

- [Keycloak Integration](../../docs/infrastruktur/keycloak/README.md)
- [Runner Apps Guide](../../docs/entwicklung/RUNNER-APPS-GUIDE.md)
- [JPMS Architektur](../../docs/entwicklung/JPMS-ARCHITECTURE.md)
