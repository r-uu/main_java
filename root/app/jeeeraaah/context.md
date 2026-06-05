# App-Kontext: jeeeraaah Application

> Diese Datei beschreibt die Architektur der Hauptapplikation im Java-Projekt.
> Allgemeiner Projekt-Kontext unter: `../../../context.md`

---

## Applikations-Architektur (3-Tier Multi-Module)

```
jeeeraaah/
├── common/                  Gemeinsame API-Verträge (DTOs, Domain-Interfaces)
├── backend/                 OpenLiberty Server-Side
│   ├── persistence/         JPA-Entities, Repositories
│   └── api/ws_rs/           REST API Endpoint (WAR-Modul)
└── frontend/                JavaFX Desktop-Applikation
```

## Datenmodell & Zyklus-Schutz (Cycle-Guard)

- **Beziehungen:** TaskGroup (1) ── (*) Task.
- **Task-Hierarchie:** Super-/Sub-Tasks.
- **Task-Abhängigkeiten:** Predecessors/Successors.
- **Cycle-Guard:** Transitive DFS-basierte Prüfung in allen Schichten (Entity, DTO, Bean) verhindert zirkuläre Abhängigkeiten.

## Backend-Details

- **Context Root:** `/jeee-raaah` (Wichtig für API-Aufrufe!)
- **Port:** 9080
- **JNDI:** `jdbc/datasource_postgresql`

## Frontend-Details (JavaFX)

- **Einstieg:** `MainAppRunner` (Haupt-App), `HierarchiesAppRunner` (Dashboard).
- **Technik:** JavaFX 25, MapStruct für DTO/Bean Mapping.

## Publikationen (in `doc/md/`)

- "JPMS in Action" (⚠️ Unvollständig bei Keycloak)
- "Modular Software in Java" (✅ Vollständig)
