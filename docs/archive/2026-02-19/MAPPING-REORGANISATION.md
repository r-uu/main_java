# Mapping-Module Reorganisation
## Zusammenfassung
Die Mapping-Module wurden von der Root-Ebene in ihre logischen Unterverzeichnisse verschoben:
- `frontend.common.mapping` → `frontend/common/mapping`
- `backend.common.mapping` → `backend/common/mapping`
## Gründe
1. **Bessere Struktur**: Die Module liegen jetzt in ihrer logischen Hierarchie
2. **Konsistenz**: Gleiche Strukturierung wie andere Module (z.B. `common/api`)
3. **Klarheit**: Sofort erkennbar, dass Mappings zu frontend/backend gehören
## Vorher
```
jeeeraaah/
├── backend/
│   ├── api/
│   └── persistence/
├── backend.common.mapping/     # ❌ Auf Root-Ebene
├── frontend/
│   ├── api.client/
│   └── ui/
├── frontend.common.mapping/    # ❌ Auf Root-Ebene
└── common/
```
## Nachher
```
jeeeraaah/
├── backend/
│   ├── common/
│   │   └── mapping/           # ✅ In backend/common/
│   ├── api/
│   └── persistence/
├── frontend/
│   ├── common/
│   │   └── mapping/           # ✅ In frontend/common/
│   ├── api.client/
│   └── ui/
└── common/
    └── api/
        └── mapping/
```
## Geänderte Dateien
### POM-Strukturen
- `jeeeraaah/pom.xml`: Module-Referenzen entfernt
- `backend/pom.xml`: `common`-Modul hinzugefügt
- `frontend/pom.xml`: `common`-Modul hinzugefügt
- `backend/common/pom.xml`: Neu erstellt
- `frontend/common/pom.xml`: Neu erstellt
- `backend/common/mapping/pom.xml`: Parent aktualisiert
- `frontend/common/mapping/pom.xml`: Parent aktualisiert
## Module-Übersicht
### 1. common.api.mapping
- **Pfad**: `common/api/mapping/`
- **Zweck**: API-neutrale Mappings
- **Mappings**: Bean ↔ DTO, Bean ↔ Lazy, Flat → Bean
### 2. frontend.common.mapping
- **Zweck**: Frontend-spezifische JavaFX-Mappings
- **Mappings**: Bean ↔ FXBean, Bean → FlatBean
### 3. backend.common.mapping
- **Zweck**: Backend-spezifische JPA-Mappings
- **Mappings**: JPA ↔ DTO, JPA ↔ Lazy
## Artefakt-IDs
Die Artefakt-IDs bleiben **unverändert**:
- `r-uu.app.jeeeraaah.frontend.common.mapping`
- `r-uu.app.jeeeraaah.backend.common.mapping`
Dadurch bleiben alle Abhängigkeiten funktionsfähig.
## Build-Status
✅ Maven Build erfolgreich
✅ Alle Module kompilieren ohne Fehler
✅ Git-Historie bleibt erhalten (git mv)
## Datum
2026-02-13
