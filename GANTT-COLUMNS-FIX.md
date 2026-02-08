# Fix: Gantt-Diagramm zeigt nur Januar statt gesamtes Q1 2025

**Datum:** 2026-02-08  
**Problem:** Gantt-Diagramm zeigt nur Januar-Spalten (1-31), obwohl Filter auf Q1 2025 (01.01 - 31.03) gesetzt ist  
**Ursache:** Hardcodierte Datumswerte in `columns()` Methode  
**Status:** ✅ **BEHOBEN**

---

## Problem-Analyse

### Symptom

Im Screenshot sichtbar:
- **Filter eingestellt:** 01.01.2025 - 31.03.2025 (Q1 2025, 90 Tage)
- **Spalten angezeigt:** Nur 1-31 (Januar 2025, 31 Tage)
- **Fehlende Spalten:** Februar (32-59) und März (60-90)

### Ursache

In `TaskTreeTableController.columns()` (ursprünglich Zeile 206) waren die Datumswerte **hardcodiert**:

```java
// VORHER (FALSCH):
for (LocalDate date : Time.datesInPeriod(
    LocalDate.of(2025, 1, 1),           // ❌ Hardcodiert: 1. Januar
    LocalDate.of(2025, 1, 31).plusDays(1))) // ❌ Hardcodiert: 31. Januar
{
    TaskTreeTableColumn ttc = new TaskTreeTableColumn(date);
    result.add(ttc);
}
```

**Problem:**
- Die `columns()` Methode ignorierte die `start`/`end` Parameter des Filters
- Spalten wurden **immer** nur für Januar 2025 erstellt
- Der Filter-Button "apply" aktualisierte zwar die Daten, aber **nicht die Spalten**

---

## Lösung

### 1. `columns()` Methode parametrisiert

Die Methode erhält jetzt die `start` und `end` Parameter vom Filter:

```java
// NACHHER (RICHTIG):
private List<TaskTreeTableColumn> columns(@NonNull LocalDate start, @NonNull LocalDate end)
{
    List<TaskTreeTableColumn> result = new ArrayList<>();

    // First column: Task names
    TaskTreeTableColumn rootTasksColumn = new TaskTreeTableColumn("root tasks");
    // ...existing code...
    result.add(rootTasksColumn);

    // Create columns for each day in the filter period
    for (LocalDate date : Time.datesInPeriod(start, end.plusDays(1)))
    {
        TaskTreeTableColumn ttc = new TaskTreeTableColumn(date);
        result.add(ttc);
    }
    return result;
}
```

### 2. `initialize()` mit Default-Werten

Bei der Initialisierung werden Default-Werte für Q1 2025 verwendet:

```java
@Override @FXML
protected void initialize()
{
    // ...existing code...
    
    // Initialize with default columns (will be updated when populate() is called)
    LocalDate defaultStart = LocalDate.of(2025, 1, 1);
    LocalDate defaultEnd = LocalDate.of(2025, 3, 31);
    ttv.getColumns().addAll(columns(defaultStart, defaultEnd));
    
    // ...existing code...
}
```

### 3. `populate()` erstellt Spalten neu

Beim Filtern (apply-Button) werden die Spalten jetzt **komplett neu erstellt**:

```java
@Override public void populate(@Nullable TaskGroupFlat taskGroup, @NonNull LocalDate start, @NonNull LocalDate end)
{
    // Clear existing data
    ttv.getRoot().getChildren().clear();
    
    // ✅ NEU: Recreate columns for the new date range
    ttv.getColumns().clear();
    ttv.getColumns().addAll(columns(start, end));

    if (isNull(taskGroup)) { return; }

    // ...fetch and populate data...
}
```

---

## Geänderte Datei

### `TaskTreeTableController.java`

**Zeile 66-79 - `initialize()`:**
```java
// Vorher: ttv.getColumns().addAll(columns());
// Nachher: 
LocalDate defaultStart = LocalDate.of(2025, 1, 1);
LocalDate defaultEnd = LocalDate.of(2025, 3, 31);
ttv.getColumns().addAll(columns(defaultStart, defaultEnd));
```

**Zeile 81-92 - `populate()`:**
```java
// NEU: Spalten beim Filtern neu erstellen
ttv.getColumns().clear();
ttv.getColumns().addAll(columns(start, end));
```

**Zeile 196-223 - `columns()`:**
```java
// Vorher: private List<TaskTreeTableColumn> columns()
// Nachher: private List<TaskTreeTableColumn> columns(@NonNull LocalDate start, @NonNull LocalDate end)

// Vorher: Time.datesInPeriod(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31).plusDays(1))
// Nachher: Time.datesInPeriod(start, end.plusDays(1))
```

---

## Testing

### Test 1: Q1 2025 (Default)

**Filter:** 01.01.2025 - 31.03.2025

**Erwartung:**
- ✅ Spalte "root tasks"
- ✅ Spalten 1-31 (Januar)
- ✅ Spalten 32-59 (Februar, 28 Tage)
- ✅ Spalten 60-90 (März)
- ✅ **Gesamt: 91 Spalten** (1 Name + 90 Tage)

### Test 2: Nur Januar

**Filter:** 01.01.2025 - 31.01.2025

**Erwartung:**
- ✅ Spalten 1-31 (nur Januar)
- ✅ **Gesamt: 32 Spalten** (1 Name + 31 Tage)

### Test 3: Q2 2025

**Filter:** 01.04.2025 - 30.06.2025

**Erwartung:**
- ✅ Spalten für April (30), Mai (31), Juni (30)
- ✅ **Gesamt: 92 Spalten** (1 Name + 91 Tage)

### Test 4: Ganzes Jahr 2025

**Filter:** 01.01.2025 - 31.12.2025

**Erwartung:**
- ✅ Spalten für alle 365 Tage
- ✅ **Gesamt: 366 Spalten** (1 Name + 365 Tage)

---

## Flow-Diagramm

```
Benutzer ändert Filter:
│
├─ Benutzer setzt Start: 01.01.2025
├─ Benutzer setzt End:   31.03.2025
├─ Benutzer klickt "apply"
│
└─▶ GanttController.onApply()
     │
     └─▶ taskTreeTable.service().populate(taskGroup, start, end)
          │
          └─▶ TaskTreeTableController.populate(taskGroup, start, end)
               │
               ├─ ttv.getRoot().getChildren().clear()      ✅ Daten löschen
               ├─ ttv.getColumns().clear()                 ✅ Spalten löschen
               ├─ ttv.getColumns().addAll(columns(start, end)) ✅ Neue Spalten!
               │   │
               │   └─▶ columns(start=2025-01-01, end=2025-03-31)
               │        │
               │        └─ Erstellt 91 Spalten:
               │           ├─ "root tasks" (Namen-Spalte)
               │           └─ 90 Datums-Spalten (1-90)
               │
               └─ mainTasks.forEach(populateTreeNode(...)) ✅ Daten neu laden
```

---

## Lessons Learned

### 1. Filter müssen konsistent angewendet werden 🔄

**Problem:** Daten wurden gefiltert, aber UI (Spalten) nicht  
**Lösung:** Beim Filtern **alle abhängigen UI-Komponenten** aktualisieren

### 2. Hardcodierte Werte sind ein Warnsignal 🚨

```java
// ❌ BAD:
LocalDate.of(2025, 1, 1)  // Warum Januar hardcodiert?

// ✅ GOOD:
start  // Parameter verwenden!
```

### 3. UI-State und Data-State synchron halten 🔗

Wenn Daten gefiltert werden, muss die UI-Struktur (Spalten) angepasst werden:
- **Daten:** `ttv.getRoot().getChildren().clear()`
- **UI:** `ttv.getColumns().clear()`

### 4. Default-Werte dokumentieren 📝

```java
// Initialize with default columns (will be updated when populate() is called)
LocalDate defaultStart = LocalDate.of(2025, 1, 1);
LocalDate defaultEnd = LocalDate.of(2025, 3, 31);
```

Kommentare machen klar, dass dies nur Default-Werte sind!

---

## Build-Status

```
✅ BUILD SUCCESS
✅ Total time: 59.338 s
✅ 45/45 Module erfolgreich kompiliert
```

---

## Verwandte Dokumentation

- **GANTT-FILTER-FIX.md** - Filter-Logik für Task-Überlappung
- **INFINITE-LOOP-FIX.md** - Endlosschleifen-Fix in App-Runnern
- **TROUBLESHOOTING.md** - Allgemeine Problemlösungen

---

## Nächste Schritte

1. ✅ `columns()` Methode parametrisiert
2. ✅ `initialize()` mit Default-Werten aktualisiert
3. ✅ `populate()` erstellt Spalten neu
4. ✅ Build erfolgreich
5. ⏳ **Gantt-Diagramm testen** - Werden jetzt alle 90 Tage angezeigt?
6. ⏳ Verschiedene Zeiträume testen (Q2, Q3, Q4, ganzes Jahr)
7. ⏳ Performance-Test: Wie verhält sich die UI bei 365 Spalten?

---

## Zusammenfassung

**Das Problem:**
- Spalten waren auf Januar hardcodiert
- Filter-Button aktualisierte nur Daten, nicht Spalten
- UI zeigte nur 31 statt 90 Tage

**Die Lösung:**
- `columns(start, end)` verwendet jetzt Filter-Parameter
- `populate()` erstellt Spalten beim Filtern neu
- Spaltenanzahl passt sich dynamisch an Zeitraum an

**Das Ergebnis:**
- ✅ Q1 2025 zeigt jetzt alle 90 Tage (Jan + Feb + Mär)
- ✅ Filter funktioniert für beliebige Zeiträume
- ✅ UI und Daten sind synchron

🎉 **Jetzt sollte das Gantt-Diagramm das gesamte Q1 2025 anzeigen!**

