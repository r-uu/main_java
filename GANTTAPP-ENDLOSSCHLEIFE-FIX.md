# Fix: Endlosschleife in GanttAppRunner und anderen Task-Hierarchie-Controllern

**Datum:** 2026-02-08  
**Problem:** Endlosschleife beim Start von GanttApp  
**Ursache:** Zirkuläre Referenzen in Task-Hierarchien  
**Status:** ✅ Behoben

## Problem

Der `GanttAppRunner` begibt sich nach dem Start in eine Endlosschleife. Das Problem trat auf, wenn zirkuläre Referenzen in den Task-Beziehungen existieren:

```
Task A → SubTask B → SubTask A → SubTask B → ... (Endlosschleife)
```

### Betroffene Controller

1. **TaskTreeTableController** (Gantt-Ansicht)
   - `populateTreeNode()` - Rekursive Methode für Baumerstellung
   - `populateRecursively()` - Rekursive Population von SubTasks

2. **TaskSelectorController** (Task-Auswahl)
   - `populateTreeNode()` - Baumerstellung
   - `collectTaskWithSubTasks()` - Sammeln aller Tasks mit SubTasks

### Ursache

Die rekursiven Methoden hatten **keine Überprüfung** auf bereits verarbeitete Tasks:

```java
// VORHER - FEHLER: Keine Überprüfung auf zirkuläre Referenzen
private TreeItem<TaskBean> populateTreeNode(TaskBean task, LocalDate start, LocalDate end)
{
    TreeItem<TaskBean> result = new TreeItem<>(task);
    if (task.subTasks().isPresent())
    {
        // ❌ Wenn SubTask wieder auf Parent verweist → Endlosschleife!
        task.subTasks().get().forEach(subTask -> 
            result.getChildren().add(populateTreeNode(subTask, start, end)));
    }
    return result;
}
```

## Lösung

Einführung eines **Tracking-Mechanismus** mit `Set<Long> processedTaskIds`, um bereits verarbeitete Tasks zu erkennen:

```java
// NACHHER - KORREKT: Mit Zyklus-Erkennung
private TreeItem<TaskBean> populateTreeNode(TaskBean task, LocalDate start, LocalDate end)
{
    return populateTreeNode(task, start, end, new HashSet<>());
}

private TreeItem<TaskBean> populateTreeNode(TaskBean task, LocalDate start, LocalDate end, 
                                             Set<Long> processedTaskIds)
{
    // ✅ Überprüfung auf zirkuläre Referenz
    if (task.id() != null && processedTaskIds.contains(task.id()))
    {
        log.warn("Circular reference detected: Task {} (ID: {}) already in hierarchy - skipping",
                task.name(), task.id());
        return null;  // ✅ Abbruch statt Endlosschleife
    }

    // ✅ Task als verarbeitet markieren
    if (task.id() != null)
    {
        processedTaskIds.add(task.id());
    }

    TreeItem<TaskBean> result = new TreeItem<>(task);
    if (task.subTasks().isPresent())
    {
        task.subTasks().get().forEach(subTask -> {
            TreeItem<TaskBean> childNode = populateTreeNode(subTask, start, end, processedTaskIds);
            if (childNode != null)  // ✅ null-Check
            {
                result.getChildren().add(childNode);
            }
        });
    }
    return result;
}
```

## Geänderte Dateien

### 1. TaskTreeTableController.java
**Pfad:** `root/app/jeeeraaah/frontend/ui/fx/.../task/gantt/TaskTreeTableController.java`

**Geänderte Methoden:**
- `populateTreeNode(TaskBean, LocalDate, LocalDate)` → Delegiert an Überladung mit `processedTaskIds`
- `populateTreeNode(TaskBean, LocalDate, LocalDate, Set<Long>)` → NEU: Mit Zyklus-Schutz
- `populateRecursively(TreeItem, LocalDate, LocalDate)` → Delegiert an Überladung mit `processedTaskIds`
- `populateRecursively(TreeItem, LocalDate, LocalDate, Set<Long>)` → NEU: Mit Zyklus-Schutz

### 2. TaskSelectorController.java
**Pfad:** `root/app/jeeeraaah/frontend/ui/fx/.../task/selector/TaskSelectorController.java`

**Geänderte Methoden:**
- `populateTreeNode(TreeItem, TaskBean)` → Delegiert an Überladung mit `processedTaskIds`
- `populateTreeNode(TreeItem, TaskBean, Set<Long>)` → NEU: Mit Zyklus-Schutz
- `populateTreeNode(TreeItem, Set<TaskBean>)` → Delegiert an Überladung mit `processedTaskIds`
- `populateTreeNode(TreeItem, Set<TaskBean>, Set<Long>)` → NEU: Mit Zyklus-Schutz
- `collectTaskWithSubTasks(TaskBean, List)` → Delegiert an Überladung mit `processedTaskIds`
- `collectTaskWithSubTasks(TaskBean, List, Set<Long>)` → NEU: Mit Zyklus-Schutz

## Pattern: Cycle Detection in Recursive Tree Processing

### Allgemeines Pattern

```java
// Public API: Keine Tracking-Parameter
public TreeItem<T> buildTree(T rootNode)
{
    return buildTree(rootNode, new HashSet<>());
}

// Private Implementation: Mit Cycle-Tracking
private TreeItem<T> buildTree(T node, Set<Long> visitedIds)
{
    // 1. Check if already processed
    if (node.getId() != null && visitedIds.contains(node.getId()))
    {
        log.warn("Cycle detected: {} already processed", node.getId());
        return null;
    }

    // 2. Mark as processed
    if (node.getId() != null)
    {
        visitedIds.add(node.getId());
    }

    // 3. Process node
    TreeItem<T> treeItem = new TreeItem<>(node);

    // 4. Recursively process children (passing visitedIds)
    for (T child : node.getChildren())
    {
        TreeItem<T> childItem = buildTree(child, visitedIds);
        if (childItem != null)  // null = cycle detected
        {
            treeItem.getChildren().add(childItem);
        }
    }

    return treeItem;
}
```

### Vorteile dieser Lösung

✅ **Keine Endlosschleifen** - Zirkuläre Referenzen werden erkannt und abgebrochen  
✅ **Logging** - Warnungen zeigen, wo Zyklen auftreten  
✅ **Graceful Degradation** - Anwendung stürzt nicht ab, zeigt nur unvollständige Hierarchie  
✅ **Performance** - O(1) Lookup in HashSet für ID-Prüfung  
✅ **Abwärtskompatibilität** - Public API bleibt unverändert  

## Weitere betroffene Controller (noch nicht gefixt)

Die folgenden Controller haben ähnliche rekursive Methoden und sollten ebenfalls geprüft werden:

1. **TaskHierarchySuperSubTasksController**
   - `populateTreeNode(TreeItem, TaskBean)`
   - `populateTreeNode(TreeItem, Set<TaskBean>)`

2. **TaskHierarchySuccessorsController**
   - `populateTreeNode(TreeItem, TaskBean)`
   - `populateTreeNode(TreeItem, Set<TaskBean>)`

3. **TaskHierarchyPredecessorsController**
   - `populateTreeNode(TreeItem, TaskBean)`
   - `populateTreeNode(TreeItem, Set<TaskBean>)`

**Empfehlung:** Diese Controller sollten nach dem gleichen Pattern refaktoriert werden.

## Testing

### Testfälle

1. **Normale Hierarchie** (kein Zyklus)
   ```
   TaskGroup A
     → Task 1
       → SubTask 1.1
       → SubTask 1.2
     → Task 2
   ```
   **Erwartung:** Vollständige Darstellung ✅

2. **Zirkuläre Referenz** (direkter Zyklus)
   ```
   Task A → SubTask B → SubTask A (Zyklus!)
   ```
   **Erwartung:** 
   - Task A wird angezeigt
   - SubTask B wird angezeigt
   - Zweites Task A wird übersprungen (Warnung im Log)

3. **Komplexer Zyklus** (indirekter Zyklus)
   ```
   Task A → SubTask B → SubTask C → SubTask A (Zyklus!)
   ```
   **Erwartung:** 
   - Task A, B, C werden einmal angezeigt
   - Zweites Task A wird übersprungen (Warnung im Log)

### Log-Ausgaben

Bei Zyklus-Erkennung wird geloggt:

```
WARN  TaskTreeTableController - Circular reference detected: Task Task-Name (ID: 123) already in hierarchy - skipping to prevent infinite loop
```

## Lessons Learned

1. **Rekursive Baum-Verarbeitung braucht Zyklus-Schutz**
   - Nie davon ausgehen, dass Datenmodell azyklisch ist
   - Immer defensive Programmierung verwenden

2. **Template für rekursive Methoden**
   - Public API ohne Tracking-Parameter
   - Private Implementierung mit `Set<Long> visitedIds`
   - Überprüfung vor Rekursion
   - Null-Checks nach Rekursion

3. **Logging ist essentiell**
   - Warnungen helfen bei Fehlersuche
   - Zeigen, wo Dateninkonsistenzen vorliegen

4. **Graceful Degradation**
   - Lieber unvollständige Anzeige als Absturz
   - User kann weiterarbeiten
   - Problem kann später im Datenmodell behoben werden

## Build-Status

✅ **BUILD SUCCESS** - Alle Änderungen kompilieren fehlerfrei  
✅ GanttApp startet ohne Endlosschleife  
✅ TaskSelectorController funktioniert korrekt  

## Nächste Schritte

1. ✅ TaskTreeTableController gefixt
2. ✅ TaskSelectorController gefixt
3. ⏳ TaskHierarchy-Controller prüfen und ggf. fixen
4. ⏳ Unit-Tests für Zyklus-Erkennung schreiben
5. ⏳ Datenmodell-Validierung einbauen (Verhindern von Zyklen beim Speichern)

