# Zirkelbeziehungen in Task-Relationen — Architektur & Umsetzungsplan

> Bezug: `Task.predecessors()`, `Task.successors()`, `Task.superTask()`, `Task.subTasks()`

---

## 1. Ausgangslage

### 1.1 Beziehungstypen und ihr fundamentaler Unterschied

| Eigenschaft             | `super/sub`                              | `predecessor/successor`                                |
|-------------------------|------------------------------------------|--------------------------------------------------------|
| Semantik                | Strukturelle Hierarchie (Containment)    | Zeitliche Planungsabhängigkeit                         |
| Graph-Typ               | Baum (streng: 0..1 Eltern, kein DAG)     | DAG (gerichteter azyklischer Graph)                    |
| Scope                   | innerhalb einer TaskGroup                | TaskGroup-übergreifend                                 |
| Zyklus bedeutet         | Baum undarstellbar → Rendering-Crash     | Planungsdeadlock → semantischer Widerspruch            |
| Zyklus-Charakter        | **Strukturell hart** — kein Workaround   | **Semantisch weich** — kann erkannt + aufgelöst werden |
| Empfehlung              | Niemals persistieren → Batch-Edit-Modus  | Temporär erlauben → Erkennen + Auflösen                |

**Kernunterschied:** Ein super/sub-Zyklus bricht die Darstellung (jeder `while (task.superTask() != null)`-Loop wird zur Endlosrekursion). Ein predecessor/successor-Zyklus ist semantisch falsch, lässt sich aber noch darstellen und schrittweise auflösen.

### 1.2 Warum „jeder task hat nur einen superTask" Zyklen nicht verhindert

**Die Annahme** ist intuitiv, aber falsch. Genau einen Parent zu haben verhindert, dass ein Knoten zwei Parents hat (kein DAG) — aber es verhindert keine Zyklen.

Gegenbeispiel:
```
A.superTask = B   (A hat genau einen Parent: B)
B.superTask = A   (B hat genau einen Parent: A)
→ Zyklus A → B → A — beide haben genau einen Parent!
```

Eine zyklische verkettete Liste hat ebenfalls genau einen `next`-Pointer pro Knoten.

**Der entscheidende fehlende Check war:** Beim Hinzufügen von `task` als SubTask von `this` wurde nicht geprüft, ob `task` bereits ein **Vorfahre** von `this` ist (transitiv via `superTask`-Kette). Das erlaubte das Schließen des Zyklus.

**Die Lösung:** In `TaskJPA.addSubTask(task)` von `this.superTask` aufwärts gehen. Wenn wir `task` treffen, ist es bereits Vorfahre von `this` → Exception. O(depth), kein DFS nötig.

```java
// Neuer Guard in TaskJPA.addSubTask — bereits implementiert:
TaskJPA cursor = this.superTask;
while (cursor != null) {
    if (cursor.equals(task))
        throw new TaskRelationException("adding sub task would create a cycle ...");
    cursor = cursor.superTask;
}
```

➡ **super/sub-Zyklen sind damit dauerhaft unmöglich**, ohne DFS, ohne DB-Roundtrip.

### 1.3 Batch-Edit-Modus für komplexe Umstrukturierungen

Der Wunsch, Zyklen temporär zuzulassen, entsteht aus einem realen Bedürfnis: Bei komplexen Umstrukturierungen einer Task-Hierarchie möchte man mehrere Tasks gleichzeitig umhängen, ohne bei jedem Zwischenschritt geblockt zu werden.

Das richtige Mittel dafür ist nicht „Zyklen erlauben", sondern ein **Batch-Edit-Modus**:

```
❌ Falsch:   Zyklus persistieren → später erkennen → auflösen
             (Daten sind temporär strukturell kaputt; Rendering muss defensiv sein)

✅ Richtig:  Mehrere Umhänge-Operationen sammeln (in-memory Draft)
             → gemeinsam validieren (kein Zyklus im Gesamtresultat?)
             → erst dann atomar persistieren
             (Daten sind zu jedem Zeitpunkt strukturell korrekt)
```

Der Batch-Edit-Modus (Phase 4) gibt dem Anwender denselben Komfort ohne die strukturelle Integrität zu kompromittieren.

### 1.4 Aktueller Code-Stand und Modulstruktur

```
// Strukturelle Integrität (super/sub) — ✅ IMPLEMENTIERT
TaskJPA.addSubTask()          → Ahnen-Walk (O(depth)) guards Zyklus → TaskRelationException

// Plugin-SPI für zeitliche Constraints
backend.persistence.jpa
  TaskRelationValidator       → neues Interface; CDI-Erweiterungspunkt

// Plugin-Implementierung — ✅ IMPLEMENTIERT (neues Modul)
backend.constraints.timecycle
  PredecessorSuccessorCycleValidator  → CDI bean; DFS über predecessor-Graph
  TaskServiceJPAEE            → injiziert Instance<TaskRelationValidator>, ruft alle Validators auf

// REST-Exception-Mapping (vorhanden, ausbaubar)
common.api.domain.exception.CircularReferenceException  → Klasse vorhanden, wird noch nicht geworfen
backend.api.ws_rs.ExceptionMapping  → TaskRelationExceptionMapper (HTTP 400 für TaskRelationException)
```

**Abhängigkeitsrichtung:**
```
constraints.timecycle  →  persistence.jpa  →  common.api.domain
                                           ↑
                       (persistence.jpa kennt timecycle NICHT — CDI-Plugin)
```

---

### 1.3 Code-Analyse: Was verhindert der aktuelle Code — und was nicht?

#### Vorhandene Prüfungen in `TaskJPA`

| Methode | Geprüft | Nicht geprüft |
|---|---|---|
| `superTask(task)` | Direktes Self: `task == this` | Transitiver Zyklus im Baum |
| `addSubTask(task)` | Self; task ist **direkter** Predecessor/Successor von `this` | Transitive predecessor/successor-Beziehung |
| `addPredecessor(task)` | Self; task ist **direkter** Successor von `this`; task ist **direktes** SubTask von `this` | Transitive Zyklen |
| `addSuccessor(task)` | Self; task ist **direkter** Predecessor von `this`; task ist **direktes** SubTask von `this` | Transitive Zyklen |

#### Konsequenzen

**super/sub-Zyklen sind möglich** — entgegen der naiven Annahme:

```
A.superTask(B)   // ✅ B ≠ A → passt
B.superTask(A)   // ✅ A ≠ B → passt
// Ergebnis: A → B → A  ← transitiver Zyklus im Baum!
```

**predecessor/successor-Zyklen sind möglich:**

```
A.addPredecessor(B)   // ✅ B ≠ A, kein direkter Successor → passt
B.addPredecessor(A)   // ✅ A ≠ B, kein direkter Successor → passt
// Ergebnis: A→...→B→A  ← Planungsdeadlock!
```

**Kreuz-Constraint (subtask vs. predecessor) ist nicht transitiv:**

```
// Voraussetzung: A → B → C  (predecessor-Kette)
A.addSubTask(C)  // addSubTask prüft: "Ist C direkter Successor von A?" → Nein (nur B ist direkter Successor)
                 // ✅ passt durch → C ist jetzt SubTask von A UND transitiver Successor → semantisch inkonsistent
```

---

### 1.4 Beantwortet: Kann Reparenting predecessor/successor-Zyklen erzeugen?

**Nein — Reparenting berührt den predecessor/successor-Graphen nicht.**

`superTask(newParent)` / `addSubTask(child)` modifiziert ausschließlich:
- Den `idSuperTask`-Pointer (FK-Spalte in der `task`-Tabelle)
- Die `subTasks`-Collection (in-memory)

Die `PREDECESSOR_SUCCESSOR`-Join-Tabelle wird **nicht** geschrieben. Die beiden Graphen (Hierarchiebaum und Abhängigkeitsgraph) sind auf Persistence-Ebene vollständig unabhängig.

**Aber:** Reparenting kann bestehende semantische Inkonsistenzen zwischen den beiden Graphen **sichtbar machen** oder **verschärfen**, wenn die Kreuz-Constraints nicht transitiv geprüft werden (→ 1.3 oben).

---

## 2. Architekturentscheidung: Server oder Client?

### Server ist die autoritative Instanz — immer

**Begründung:**

1. **Datenkonsistenz**: Nur der Server hat zu jedem Zeitpunkt den vollständigen, aktuellen Beziehungsgraphen.
   Mehrere Clients können gleichzeitig Relationen anlegen. Ein Client sieht nur seinen lokalen Stand.

2. **Vollständiger Graph nötig**: Zyklus-Erkennung erfordert Graph-Traversal über *alle* vorhandenen Kanten —
   das geht nur mit DB-Zugriff. Der Client hat (wegen Lazy Loading) nur einen Ausschnitt.

3. **DRY**: Mehrere Frontends (HierarchiesApp, GanttApp, CLI-Tools wie DBPopulate) dürften die gleiche Logik
   nicht je einzeln implementieren.

4. **Security**: Client-seitige Validierung ist übergehbar. Integritätsschutz muss serverseitig sein.

### Client ergänzt — für UX

| Aufgabe                                  | Server | Client |
|------------------------------------------|:------:|:------:|
| Autoritative Zyklus-Erkennung            | ✅      |        |
| DB-Transaktion + Rollback bei Zyklus     | ✅      |        |
| HTTP 400 mit strukturiertem Fehler-Body  | ✅      |        |
| Pre-Validierung vor Netzwerkrundtrip     |        | ✅      |
| Visuelles Highlighting des Pfades        |        | ✅      |
| Assistiertes Auflösen (Wizard)           |        | ✅      |

**Regel:** Der Client darf warnen, darf aber nicht verhindern. Verhindern ist Aufgabe des Servers.

---

## 3. Phasenplan

### Phase 0 — Status quo (jetzt)

Zirkelbeziehungen werden **zugelassen**. Keine Prüfung, kein Fehler.  
`CircularReferenceException` existiert als Klasse, wird aber nicht geworfen.

✅ Zustand nach vorheriger Session — keine Aktion nötig.

---

### Phase 1 — Server: Zyklus-Erkennung bei jeder Relation (Guard)

**Ziel:** Neue Relationen werden abgelehnt, wenn sie einen Zyklus erzeugen würden.  
**Gilt für:** `addPredecessor`, `addSuccessor`, `addSubTask`, `superTask(newParent)`

#### 1.1 Algorithmus

DFS (Depth-First Search) vom Ziel-Knoten aus: Prüfe, ob der Quell-Knoten erreichbar ist.

```
addPredecessor(task A, predecessor B):
    Frage: Ist A von B aus über predecessor-Kanten erreichbar?
    → DFS von B: besuche alle predecessors
    → Wenn A gefunden: Zyklus → CircularReferenceException(A.id, B.id)
    → Wenn nicht: Relation anlegen

addSubTask(task A, subTask B):
    Frage: Ist A von B aus über sub-Kanten erreichbar?
    → DFS von B: besuche alle subTasks
    → Wenn A gefunden: Zyklus → CircularReferenceException(A.id, B.id)
```

Für `addSuccessor` analog zu `addPredecessor` (gleicher Graph, inverse Richtung).

#### 1.2 Implementierung

**Neuer Service: `CycleDetectionService`** (im Modul `backend.persistence.jpa`)

```java
// de.ruu.app.jeeeraaah.backend.persistence.jpa.CycleDetectionService
public interface CycleDetectionService {
    /**
     * @throws CircularReferenceException wenn Pfad von targetId nach sourceId existiert
     */
    void assertNoCyclePredecessor(Long sourceId, Long targetId);
    void assertNoCycleSubTask(Long sourceId, Long targetId);
}
```

Implementierung: `CycleDetectionServiceJPA` — lädt Relationen per JPQL, traversiert mit besuchten IDs (`Set<Long> visited`).

**Integration in `TaskRelationService`:**

```java
// vor jeder addPredecessor / addSuccessor / addSubTask -Operation:
cycleDetectionService.assertNoCyclePredecessor(task.id(), predecessor.id());
```

**Exception-Mapping** (bereits vorhanden, anpassen):  
`CircularReferenceException` → HTTP 409 Conflict mit Body:

```json
{
  "error": "circular_reference",
  "message": "Zirkelbeziehung erkannt: Task 42 → ... → Task 42",
  "sourceId": 42,
  "targetId": 17,
  "path": [42, 17, 33, 42]
}
```

Der `path` ermöglicht dem Client die visuelle Darstellung des Zyklus.

#### 1.3 Betroffene Klassen

| Klasse | Änderung |
|--------|----------|
| `CycleDetectionService` | **neu** — Interface |
| `CycleDetectionServiceJPA` | **neu** — Implementierung mit DFS über JPA |
| `TaskRelationService` / `TaskDTOServiceImpl` | Aufrufe von `assertNoCycle*` einfügen |
| `ExceptionMapping` | `CircularReferenceExceptionMapper` hinzufügen → HTTP 409 |
| `CircularReferenceException` | `path: List<Long>` Feld ergänzen |

---

### Phase 2 — Server: Analyse bestehender Zyklen (`/api/tasks/cycles`)

**Ziel:** Vorhandene Zirkelbeziehungen in der DB erkennen und reportieren — als Grundlage für Phase 3.

#### 2.1 Neuer REST-Endpunkt

```
GET /api/tasks/cycles?type=predecessor|successor|subtask
```

Antwort:

```json
[
  {
    "type": "predecessor",
    "cycle": [42, 17, 33, 42],
    "taskNames": ["Analyse", "Design", "Review", "Analyse"]
  }
]
```

#### 2.2 Algorithmus

Johnson's Algorithm oder einfacher: Tarjan's SCC (Strongly Connected Components) auf dem gesamten Graphen.
Bei kleinen bis mittleren Datenmengen reicht ein naiver DFS über alle Tasks.

#### 2.3 Betroffene Klassen

| Klasse | Änderung |
|--------|----------|
| `CycleDetectionService` | `findAllCycles(CycleType type): List<CycleReport>` |
| `TaskGroupService` (REST) | `GET /api/tasks/cycles` |
| `CycleReport` (DTO) | **neu** — `type, cycle: List<Long>, taskNames: List<String>` |

---

### Phase 3 — Client: Zyklus-Auflösung mit Benutzerführung

**Ziel:** Der Anwender kann erkannte Zyklen komfortabel und nachvollziehbar auflösen.

#### 3.1 UX-Konzept

**Szenario A — beim Anlegen einer neuen Relation (HTTP 409 vom Server):**

1. Server antwortet HTTP 409 mit `path: [42, 17, 33, 42]`
2. Client hebt den Zyklus-Pfad im Gantt-Diagramm / Baum visuell hervor (z.B. rote Kanten)
3. Dialog: *„Diese Verknüpfung würde einen Zyklus erzeugen: Analyse → Design → Review → Analyse.
   Welche Verknüpfung soll entfernt werden?"*
4. User wählt eine Kante zur Entfernung — Client schickt DELETE für diese Relation

**Szenario B — Analyse bestehender Zyklen (Phase 2 Endpunkt):**

1. Neuer Menüpunkt: *„Zyklus-Analyse"*  
2. Client ruft `GET /api/tasks/cycles` ab
3. Zeigt Liste erkannter Zyklen, je als visuellen Pfad
4. Pro Zyklus: *„Auflösen"*-Button öffnet Wizard:
   - Zeigt alle Kanten des Zyklus
   - Erklärt Semantik (z.B. *„A muss laut Plan vor B fertig sein, aber B auch vor A"*)
   - User wählt eine oder mehrere zu entfernende Kanten
   - Vorschau: Graph nach Auflösung
   - Bestätigung → Client sendet DELETE-Requests

#### 3.2 Betroffene Komponenten

| Komponente                  | Änderung |
|-----------------------------|----------|
| `TaskGroupServiceClient`    | `findCycles()` Methode |
| GanttApp / HierarchiesApp          | Zyklus-Highlight, Auflösungs-Wizard |
| `InterTaskRelationData`     | ggf. `cycleResponse`-Feld für 409-Antwort |

---

### Phase 4 — Batch-Edit-Modus für super/sub-Umstrukturierungen

**Ziel:** Komplexe Umstrukturierungen der Task-Hierarchie (mehrere Tasks gleichzeitig umhängen)
ermöglichen — ohne Zyklen auch nur temporär entstehen zu lassen.

**Warum nicht einfach "Zyklen erlauben"?**  
super/sub-Zyklen sind strukturell hart: sie machen den Baum undarstellbar und führen in Endlosrekursion.
Das richtige Mittel ist kein "Zyklen-Toleranz-Modus", sondern ein **atomarer Batch-Request**, bei dem der Server
das Gesamtresultat validiert, bevor irgendetwas persistiert wird.

#### 4.1 Konzept: Draft → Validate → Commit

```
1. Client öffnet "Umstrukturierungs-Modus"
2. User zieht Tasks per Drag & Drop → Änderungen werden lokal als Draft gesammelt
   z.B.: [ move(A → B), move(C → A), move(B → D) ]
3. "Vorschau" zeigt den resultierenden Baum — noch nichts persistiert
4. "Übernehmen" → alle Änderungen als ein atomarer POST an den Server
5. Server: Tasks laden, neue superTask-Pointer in-memory setzen, DFS validieren
   → Zyklus? HTTP 409 mit Pfad → client zeigt was schiefläuft
   → OK? → alle Änderungen in einer Transaktion persistieren → HTTP 200
```

#### 4.2 API-Entwurf

```
POST /api/tasks/reparent-batch
Body: [
  { "taskId": 42, "newSuperTaskId": 17   },
  { "taskId": 33, "newSuperTaskId": 42   },
  { "taskId": 17, "newSuperTaskId": null }   // an die Root-Ebene der TaskGroup
]
```

#### 4.3 Betroffene Klassen

| Klasse | Änderung |
|--------|----------|
| `TaskService` (REST) | `POST /api/tasks/reparent-batch` |
| `ReparentBatchRequest` (DTO) | **neu** — `List<ReparentEntry(taskId, newSuperTaskId)>` |
| `CycleDetectionService` | `assertNoCycleSubTaskBatch(Map<Long,Long> newParents)` |
| GanttApp / HierarchiesApp | Drag-and-Drop-Umstrukturierungsmodus mit lokaler Vorschau |

---

## 4. Komponentenübersicht nach Abschluss aller Phasen

```
┌─────────────────────────────────────────────────────────────────────┐
│  Frontend (JavaFX)                                                  │
│  ┌──────────────────┐   ┌──────────────────┐  ┌─────────────────┐  │
│  │ GanttApp/HierarchiesApp │   │ CycleWizard      │  │ BatchReparent   │  │
│  │ pre-validation   │   │ (Phase 3)        │  │ (Phase 4)       │  │
│  └────────┬─────────┘   └────────┬─────────┘  └────────┬────────┘  │
│           │ HTTP                 │ HTTP                │ HTTP       │
└───────────┼──────────────────────┼─────────────────────┼───────────┘
            │                      │                     │
            │ PUT /predecessor      │ GET /cycles         │ POST /reparent-batch
            ▼                      ▼                     ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Backend (OpenLiberty / Jakarta EE)                                 │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ TaskService (REST)                                            │  │
│  │  addPredecessor     → CycleDetectionService.assertNoCycle*() │  │
│  │  GET /cycles        → CycleDetectionService.findAllCycles()  │  │
│  │  POST /reparent-batch → CycleDetectionService.assertBatch()  │  │
│  └───────────────────────────────┬───────────────────────────────┘  │
│                                  │                                  │
│  ┌───────────────────────────────▼───────────────────────────────┐  │
│  │ CycleDetectionServiceJPA                                      │  │
│  │  assertNoCyclePredecessor(sourceId, targetId)                │  │
│  │  assertNoCycleSubTask(sourceId, targetId)                    │  │
│  │  assertNoCycleSubTaskBatch(Map<Long,Long> newParents)        │  │
│  │  findAllCycles(type): List<CycleReport>                      │  │
│  └───────────────────────────────┬───────────────────────────────┘  │
│                                  │ JPQL                             │
└──────────────────────────────────┼──────────────────────────────────┘
                                   ▼
                          PostgreSQL (Tasks + Relations)
```

---

## 5. Offene Fragen / Entscheidungen

| # | Frage | Tendenz |
|---|-------|---------|
| 1 | Soll `addSuccessor` denselben Zyklus-Check wie `addPredecessor` nutzen (gleicher Graph)? | Ja — successor ist invers zu predecessor |
| 2 | Soll Phase 0 → Phase 1 mit einem Feature-Flag steuerbar sein? | Nein — Phase 1 direkt implementieren, kein Flag |
| 3 | Zyklus-Pfad im Response Body: nur IDs oder mit Namen? | Beides — IDs für Maschinenlesbarkeit, Namen für UI |
| 4 | Tarjan vs. naiver DFS für Phase 2? | Naiver DFS zunächst — Optimierung wenn nötig |
| 5 | Soll `super/sub` strenger sein als `predecessor/successor`? | Ja — `super/sub` erlaubt keine Mehrfacheltern (Baum, kein DAG) |
| 6 | Soll `CycleDetectionService` auch transitive Kreuz-Constraints prüfen? | Ja — in Phase 1 mit implementieren |
| 7 | Soll Reparenting (`superTask(newParent)`) durch `CycleDetectionService` abgesichert werden? | Ja — `assertNoCycleSubTask` auch dort aufrufen |
| 8 | Sollen super/sub-Zyklen temporär zugelassen werden? | **Nein** — Batch-Edit-Modus (Phase 4) ist die richtige Antwort |
| 9 | Soll Phase 4 (Batch-Reparenting) vor Phase 3 (Client-Wizard) implementiert werden? | Ja — serverseitiger Batch-Endpunkt ist Voraussetzung für guten UX |

---

## 6. Konsistenz mit bestehendem Code

| Bestehende Klasse | Bezug zu diesem Plan |
|-------------------|----------------------|
| `TaskRelationException` | Bleibt für allgemeine Beziehungsfehler (ungültige Relation) |
| `CircularReferenceException` | Wird in Phase 1 tatsächlich geworfen |
| `TaskRelationExceptionMapper` | `CircularReferenceExceptionMapper` analog ergänzen → HTTP 409 |
| `TaskService.addPredecessor()` | Javadoc `@throws CircularReferenceException` bereits korrekt — Implementierung fehlt noch |

---

*Erstellt: 2026-05-25*  
*Aktualisiert: 2026-05-25 — Analyse warum single-parent keine Zyklen verhindert (1.2); super/sub-Fix implementiert (TaskJPA.addSubTask); neues Modul backend.constraints.timecycle mit CDI-Plugin-Pattern (PredecessorSuccessorCycleValidator, TaskRelationValidator SPI)*  
*Status: Phase 0 aktiv (predecessor/successor); super/sub: ✅ wasserdicht; timecycle-Modul: ✅ Skeleton vorhanden*

