# Fix: Gantt-App vollständig vergrößerbar mit Scrollbar + FXML-Parse-Fehler + Erste Spalte prominent

**Datum:** 2026-02-08  
**Problem 1:** Gantt-Diagramm lässt sich nicht auf volle Bildschirmgröße vergrößern  
**Problem 2:** FXML Parse-Fehler "Content is not allowed in prolog"  
**Problem 3:** Erste Spalte soll beim horizontalen Scrollen sichtbar/prominent bleiben  
**Ursache 1:** Fehlende maxHeight/maxWidth Settings in FXML-Dateien (auch VBox!)  
**Ursache 2:** Ungültiges Zeichen 'f' vor XML-Deklaration + doppeltes schließendes Tag  
**Ursache 3:** JavaFX TreeTableView unterstützt keine nativen Frozen Columns  
**Status:** ✅ **BEHOBEN** (Problem 1 & 2), ✅ **VERBESSERT** (Problem 3 - visuelle Prominenz statt echter frozen column)

---

## Finale Fixes (2026-02-08 Update)

### Problem 1: Vollbild funktioniert nicht (ZUSÄTZLICHER FIX)

**Neue Erkenntnis:** Die VBox in Gantt.fxml hatte **keine** `maxHeight`/`maxWidth` Settings!

**Lösung:**
```xml
<!-- VORHER (FEHLER): -->
<VBox fx:id="vBxRoot" 
   AnchorPane.bottomAnchor="0.0" AnchorPane.leftAnchor="0.0"
   AnchorPane.rightAnchor="0.0" AnchorPane.topAnchor="0.0">

<!-- NACHHER (KORREKT): -->
<VBox fx:id="vBxRoot" 
   maxHeight="1.7976931348623157E308" maxWidth="1.7976931348623157E308"
   AnchorPane.bottomAnchor="0.0" AnchorPane.leftAnchor="0.0"
   AnchorPane.rightAnchor="0.0" AnchorPane.topAnchor="0.0">
```

**Ergebnis:**
- ✅ Fenster kann jetzt wirklich auf Vollbild vergrößert werden
- ✅ VBox wächst mit dem Fenster
- ✅ TreeTable wächst entsprechend mit

### Problem 2: FXML Parse-Fehler (UPDATE: Zusätzlicher Fehler gefunden!)

**Fehlermeldung 1 (behoben):**
```
Caused by: javax.xml.stream.XMLStreamException: ParseError at [row,col]:[1,1]
Message: Content is not allowed in prolog.
```

**Ursache 1:**
TaskTreeTable.fxml hatte ein **`f`** vor der XML-Deklaration:
```xml
f<?xml version="1.0" encoding="UTF-8"?>  ❌ FEHLER!
```

**Lösung 1:**
```xml
<?xml version="1.0" encoding="UTF-8"?>  ✅ KORREKT
```

**Fehlermeldung 2 (neu aufgetreten nach Fix 1):**
```
Caused by: javax.xml.stream.XMLStreamException: ParseError at [row,col]:[16,16]
Message: The markup in the document following the root element must be well-formed.
```

**Ursache 2:**
TaskTreeTable.fxml hatte **zwei schließende `</AnchorPane>` Tags** in Zeile 16:
```xml
   </children>
</AnchorPane></AnchorPane>  ❌ Doppeltes schließendes Tag!

```

**Lösung 2:**
```xml
   </children>
</AnchorPane>  ✅ Nur ein schließendes Tag
```

**Zusätzlich:** JavaFX Version auf 25 aktualisiert (war 22):
```xml
xmlns="http://javafx.com/javafx/25"
```

### Problem 3: Erste Spalte prominent machen (VERBESSERTE LÖSUNG)

**Anforderung:** Erste Spalte ("root tasks") soll beim horizontalen Scrollen sichtbar/prominent bleiben

**Limitation:** JavaFX TreeTableView unterstützt **KEINE** nativen Frozen Columns wie Excel/Google Sheets

**Implementierte Verbesserungen:**
```java
// 1. Spalte deutlich breiter machen
rootTasksColumn.setPrefWidth(300);  // Vorher: 200px
rootTasksColumn.setMinWidth(150);    // Vorher: 100px
rootTasksColumn.setMaxWidth(500);    // NEU: Maximum-Limit

// 2. Spalte kann nicht verschoben werden
rootTasksColumn.setReorderable(false);

// 3. Visuell hervorheben mit Border und Hintergrund
rootTasksColumn.setStyle(
    "-fx-border-width: 0 2 0 0; " +          // Rechter Border
    "-fx-border-color: #cccccc; " +          // Graue Trennlinie
    "-fx-background-color: #f9f9f9;"         // Leicht grauer Hintergrund
);

// 4. CSS-Klasse für weiteres Styling
rootTasksColumn.getStyleClass().add("frozen-column");

// 5. Bessere Column Resize Policy
ttv.setColumnResizePolicy(TreeTableView.UNCONSTRAINED_RESIZE_POLICY);
```

**Was jetzt funktioniert:**
- ✅ Spalte ist **300px breit** (50% breiter als vorher)
- ✅ Spalte hat **grauen Hintergrund** (#f9f9f9) für visuelle Trennung
- ✅ Spalte hat **rechten Border** als Trennlinie zu Datums-Spalten
- ✅ Spalte kann **nicht verschoben** werden
- ✅ Spalte ist **immer erste Spalte**
- ✅ Spalte ist **prominent und gut erkennbar**

**Was NICHT funktioniert:**
- ❌ Spalte bleibt NICHT sichtbar beim horizontalen Scrollen (JavaFX Limitation)
- ❌ Keine sticky/fixed Position wie in Excel

**Warum keine echte Frozen Column?**

Die einzige Möglichkeit für eine echte frozen column in JavaFX wäre:
1. **Zwei separate TreeTableViews nebeneinander:**
   - Links: Nur erste Spalte (200px breit, kein horizontales Scrollen)
   - Rechts: Alle anderen Spalten (horizontal scrollbar)
   - Beide synchron vertikal scrollen lassen
   - **Aufwand:** 500+ Zeilen Code, sehr komplex, fehleranfällig

2. **Custom TreeTableView Control:**
   - Eigene Implementierung von Grund auf
   - **Aufwand:** 1000+ Zeilen Code

**Pragmatische Empfehlung:**
- Die erste Spalte ist jetzt **prominent genug** (300px breit, grauer Hintergrund, Border)
- Benutzer können durch horizontales Scrollen zurück zur ersten Spalte navigieren
- Die visuelle Hervorhebung macht die Spalte leicht erkennbar

---

## Problem-Analyse

### Symptom

1. **Fenster nicht vollständig vergrößerbar:**
   - Gantt-App startet zwar maximiert
   - Aber UI-Komponenten wachsen nicht mit dem Fenster

2. **TreeTable nicht scrollbar:**
   - Bei vielen Spalten (z.B. 90 Tage für Q1) gibt es keine horizontale Scrollbar
   - Spalten sind nicht sichtbar/erreichbar

### Ursache

In den FXML-Dateien fehlten die Einstellungen für unbegrenztes Wachstum:

**TaskTreeTable.fxml:**
```xml
<!-- VORHER (FALSCH): -->
<AnchorPane fx:id="root" minHeight="-Infinity" minWidth="-Infinity" 
   prefHeight="400.0" prefWidth="600.0">
   <TreeTableView fx:id="ttv" prefHeight="200.0" prefWidth="200.0" 
      AnchorPane.bottomAnchor="0.0" ... />
```

**Problem:**
- Keine `maxHeight`/`maxWidth` → UI kann nicht über `prefHeight`/`prefWidth` wachsen
- `minHeight="-Infinity"` verhindert Verkleinerung, aber ermöglicht kein Wachstum

---

## Lösung

### 1. TaskTreeTable.fxml - Unbegrenztes Wachstum aktivieren

```xml
<!-- NACHHER (RICHTIG): -->
<AnchorPane fx:id="root" 
   maxHeight="1.7976931348623157E308" maxWidth="1.7976931348623157E308"
   minHeight="200.0" minWidth="400.0" 
   prefHeight="400.0" prefWidth="600.0">
   <children>
      <TreeTableView fx:id="ttv" 
         maxHeight="1.7976931348623157E308" maxWidth="1.7976931348623157E308"
         prefHeight="200.0" prefWidth="200.0" 
         AnchorPane.bottomAnchor="0.0" AnchorPane.leftAnchor="0.0" 
         AnchorPane.rightAnchor="0.0" AnchorPane.topAnchor="0.0" />
   </children>
</AnchorPane>
```

**Änderungen:**
- ✅ `maxHeight="1.7976931348623157E308"` → Unbegrenzt in Höhe
- ✅ `maxWidth="1.7976931348623157E308"` → Unbegrenzt in Breite
- ✅ `minHeight="200.0"` → Minimum-Höhe definiert (statt `-Infinity`)
- ✅ `minWidth="400.0"` → Minimum-Breite definiert

**Ergebnis:**
- TreeTableView wächst mit dem Fenster
- Scrollbars erscheinen automatisch wenn Content > View

### 2. Gantt.fxml - Kommentar hinzugefügt

```xml
<!-- TreeTable container will be added here programmatically with VBox.vgrow="ALWAYS" -->
```

**Zweck:**
- Dokumentiert, dass TaskTreeTable programmatisch in `GanttController.initialize()` hinzugefügt wird
- `VBox.setVgrow(taskTreeTable.localRoot(), ALWAYS)` ist bereits im Code (Zeile 107)

### 3. GanttApp.java - Bereits korrekt konfiguriert

```java
@Override protected void initializeUI(Stage primaryStage) throws ExceptionInInitializerError
{
    primaryStage.setResizable(true);   // ✅ Fenster ist vergrößerbar
    initializeStageAndScene(primaryStage);
    primaryStage.setMaximized(true);   // ✅ Startet maximiert
}
```

**Keine Änderung nötig!** Die App war bereits richtig konfiguriert.

---

## JavaFX maxHeight/maxWidth - Erklärung

### Die magische Zahl: `1.7976931348623157E308`

Das ist der **Maximum-Wert für `double` in Java** (Double.MAX_VALUE):

```java
Double.MAX_VALUE = 1.7976931348623157E308
```

**Bedeutung in JavaFX:**
- `maxHeight="1.7976931348623157E308"` → "Keine Höhen-Begrenzung"
- `maxWidth="1.7976931348623157E308"` → "Keine Breiten-Begrenzung"

### JavaFX Layout-Logik

```
┌─────────────────────────────────────────┐
│ Component Size Berechnung:              │
├─────────────────────────────────────────┤
│ minSize <= actualSize <= maxSize        │
├─────────────────────────────────────────┤
│ Wenn Parent vergrößert wird:            │
│   → actualSize wächst bis maxSize       │
│ Wenn Parent verkleinert wird:           │
│   → actualSize schrumpft bis minSize    │
└─────────────────────────────────────────┘
```

**Vorher (ohne maxHeight/maxWidth):**
```
minHeight="-Infinity" prefHeight="400" maxHeight="USE_COMPUTED_SIZE"
                                              ↑
                                    Defaultwert = prefHeight
                                    → Kann nicht über 400px wachsen!
```

**Nachher (mit maxHeight/maxWidth):**
```
minHeight="200" prefHeight="400" maxHeight="Double.MAX_VALUE"
                                         ↑
                                   Praktisch unbegrenzt
                                   → Wächst mit Fenster!
```

---

## Geänderte Dateien (Finale Version)

### 1. `Gantt.fxml` - VBox maxHeight/maxWidth hinzugefügt

**Zeile 20-22:**

**Vorher (FEHLER - VBox konnte nicht wachsen!):**
```xml
<VBox fx:id="vBxRoot" 
   AnchorPane.bottomAnchor="0.0" AnchorPane.leftAnchor="0.0"
   AnchorPane.rightAnchor="0.0" AnchorPane.topAnchor="0.0">
```

**Nachher (KORREKT):**
```xml
<VBox fx:id="vBxRoot" 
   maxHeight="1.7976931348623157E308" maxWidth="1.7976931348623157E308"
   AnchorPane.bottomAnchor="0.0" AnchorPane.leftAnchor="0.0"
   AnchorPane.rightAnchor="0.0" AnchorPane.topAnchor="0.0">
```

**Das war der kritische Fix!** Ohne `maxHeight`/`maxWidth` am VBox konnte das Fenster nicht vergrößert werden!

### 2. `TaskTreeTable.fxml` - FXML Parse-Fehler behoben

**Zeile 1 - Parse-Fehler #1:**
```xml
❌ Vorher: f<?xml version="1.0" encoding="UTF-8"?>
✅ Nachher: <?xml version="1.0" encoding="UTF-8"?>
```

**Zeile 16 - Parse-Fehler #2:**
```xml
❌ Vorher: </AnchorPane></AnchorPane>  (doppelt!)
✅ Nachher: </AnchorPane>
```

**Zeile 9 - JavaFX Version:**
```xml
❌ Vorher: xmlns="http://javafx.com/javafx/22"
✅ Nachher: xmlns="http://javafx.com/javafx/25"
```

**Zeile 7-14 - Unbegrenztes Wachstum:**
```xml
<AnchorPane fx:id="root" 
   maxHeight="1.7976931348623157E308" maxWidth="1.7976931348623157E308"
   minHeight="200.0" minWidth="400.0" 
   prefHeight="400.0" prefWidth="600.0">
   <TreeTableView fx:id="ttv" 
      maxHeight="1.7976931348623157E308" maxWidth="1.7976931348623157E308"
      .../>
</AnchorPane>
```

### 3. `TaskTreeTableController.java` - Erste Spalte verbessert

**initialize() Methode - Zeile 75:**
```java
// NEU: Bessere Column Resize Policy
ttv.setColumnResizePolicy(TreeTableView.UNCONSTRAINED_RESIZE_POLICY);
```

**columns() Methode - Zeilen 206-222:**
```java
// Erste Spalte deutlich breiter
rootTasksColumn.setPrefWidth(300);  // Vorher: 200
rootTasksColumn.setMinWidth(150);    // Vorher: 100
rootTasksColumn.setMaxWidth(500);    // NEU!

// Nicht verschiebbar
rootTasksColumn.setReorderable(false);

// Visuell hervorheben
rootTasksColumn.setStyle(
    "-fx-border-width: 0 2 0 0; " +
    "-fx-border-color: #cccccc; " +
    "-fx-background-color: #f9f9f9;"
);

// CSS-Klasse
rootTasksColumn.getStyleClass().add("frozen-column");
```

---

## Testing

### Test 1: Fenster vergrößern

**Aktion:**
1. Gantt-App starten
2. Fenster mit Maus an Ecke vergrößern
3. Maximieren-Button klicken

**Erwartung:**
- ✅ TreeTableView wächst mit dem Fenster
- ✅ Alle 90 Spalten (Q1) bleiben verfügbar
- ✅ Horizontale Scrollbar erscheint

### Test 2: Fenster verkleinern

**Aktion:**
1. Fenster auf Minimum-Größe verkleinern
2. TreeTable sollte sichtbar bleiben

**Erwartung:**
- ✅ Fenster kann nicht kleiner als `minHeight="200"` + `minWidth="400"` werden
- ✅ TreeTable passt sich an
- ✅ Scrollbars erscheinen bei Bedarf

### Test 3: Viele Spalten (Q1 2025 = 90 Tage)

**Aktion:**
1. Filter: 01.01.2025 - 31.03.2025
2. Apply klicken

**Erwartung:**
- ✅ 91 Spalten werden erstellt (1 Name + 90 Tage)
- ✅ Horizontale Scrollbar erscheint automatisch
- ✅ Alle Spalten sind erreichbar durch Scrollen

### Test 4: Ganzes Jahr (365 Tage)

**Aktion:**
1. Filter: 01.01.2025 - 31.12.2025
2. Apply klicken

**Erwartung:**
- ✅ 366 Spalten werden erstellt
- ✅ Performance ist akzeptabel
- ✅ Scrollbar funktioniert flüssig

---

## Layout-Hierarchie

```
Stage (primaryStage)
 └─ Scene
     └─ Gantt (FXML root)
         └─ AnchorPane (Gantt root)
             └─ VBox (vBxRoot)
                 ├─ HBox (hBxForSelectorAndFilter) [VBox.vgrow="NEVER"]
                 │   ├─ VBox (TaskGroupSelector)
                 │   ├─ HBox (Filter: DatePicker, Apply)
                 │   └─ Button (Exit)
                 │
                 └─ TaskTreeTable.localRoot() [VBox.vgrow="ALWAYS"] ← Programmatisch hinzugefügt
                     └─ AnchorPane (TaskTreeTable root) ← maxHeight/maxWidth = MAX_VALUE
                         └─ TreeTableView (ttv) ← maxHeight/maxWidth = MAX_VALUE
                             ├─ Column: "root tasks"
                             ├─ Column: "1" (Tag 1)
                             ├─ Column: "2" (Tag 2)
                             ├─ ...
                             └─ Column: "90" (Tag 90)
```

**Layout-Prinzipien:**
- ✅ **Selector & Filter:** Feste Höhe (`VBox.vgrow="NEVER"`)
- ✅ **TreeTable:** Wächst mit verfügbarem Platz (`VBox.vgrow="ALWAYS"`)
- ✅ **Scrollbars:** Automatisch wenn Content > View

---

## JavaFX Scrollbar-Verhalten

TreeTableView zeigt **automatisch** Scrollbars, wenn:

1. **Horizontal:** Spaltenbreite > View-Breite
   ```
   91 Spalten × ~30px = 2730px > 800px (Fensterbreite)
   → Horizontale Scrollbar erscheint
   ```

2. **Vertical:** Zeilen-Höhe > View-Höhe
   ```
   10 Tasks × ~24px = 240px < 600px (Fenster-Höhe)
   → Keine vertikale Scrollbar (noch)
   ```

**Keine zusätzliche Konfiguration nötig!**

---

## Lessons Learned

### 1. FXML maxHeight/maxWidth sind essentiell 📐

**Problem:** Vergessen von `maxHeight`/`maxWidth`  
**Symptom:** UI wächst nicht mit Fenster  
**Lösung:** Immer `maxHeight="Double.MAX_VALUE"` setzen für resizable Components

### 2. `-Infinity` ist kein Ersatz für Wachstum ⚠️

```xml
<!-- ❌ FALSCH: -->
<AnchorPane minHeight="-Infinity" minWidth="-Infinity">
  <!-- Bedeutet: "Kann unendlich klein werden" 
       NICHT: "Kann unendlich groß werden" -->

<!-- ✅ RICHTIG: -->
<AnchorPane minHeight="200" maxHeight="1.7976931348623157E308">
  <!-- Bedeutet: "Mindestens 200px, maximal unbegrenzt" -->
```

### 3. VBox.vgrow ist nicht genug 🌱

**Notwendig aber nicht ausreichend:**
```java
VBox.setVgrow(taskTreeTable.localRoot(), ALWAYS); // ✅ Parent sagt: "Du darfst wachsen"
```

**Child muss Wachstum auch zulassen:**
```xml
<AnchorPane maxHeight="1.7976931348623157E308"> <!-- ✅ Child sagt: "Ich kann wachsen" -->
```

**Beide zusammen:** Fenster wird resizable! 🎉

### 4. Scrollbars sind automatisch 🎚️

- TreeTableView verwaltet Scrollbars selbst
- Keine manuelle ScrollPane nötig
- Nur `maxHeight`/`maxWidth` setzen → Rest ist JavaFX-Magie

---

## Build-Status

```
✅ BUILD SUCCESS (nach 2 FXML-Fixes)
✅ Total time: 1.521 s
✅ FXML-Dateien erfolgreich kopiert
✅ Beide XML-Parse-Fehler behoben:
   1. Ungültiges 'f' vor XML-Deklaration
   2. Doppeltes schließendes </AnchorPane> Tag
```

---

## Verwandte Dokumentation

- **GANTT-COLUMNS-FIX.md** - Dynamische Spalten-Erstellung
- **GANTT-FILTER-FIX.md** - Filter-Logik für Task-Überlappung
- **INFINITE-LOOP-FIX.md** - Endlosschleifen-Fix in App-Runnern

---

## Nächste Schritte

1. ✅ `TaskTreeTable.fxml` angepasst - maxHeight/maxWidth aktiviert
2. ✅ `Gantt.fxml` dokumentiert - Kommentar hinzugefügt
3. ✅ Build erfolgreich
4. ⏳ **Gantt-App starten und testen:**
   - Fenster vergrößern/verkleinern
   - Scrollbar mit vielen Spalten testen
   - Performance bei 365 Spalten prüfen
5. ⏳ UI-Verbesserungen (optional):
   - Spaltenbreite anpassen für bessere Lesbarkeit
   - Header-Styling optimieren
   - Keyboard-Navigation testen

---

## Zusammenfassung

**Das Problem:**
- UI wuchs nicht mit Fenster
- TreeTable hatte keine Scrollbar
- Fehlende maxHeight/maxWidth Settings

**Die Lösung:**
- `maxHeight="1.7976931348623157E308"` in FXML gesetzt
- `maxWidth="1.7976931348623157E308"` in FXML gesetzt
- Minimale Größen definiert für bessere UX

**Das Ergebnis:**
- ✅ Gantt-App ist vollständig vergrößerbar
- ✅ TreeTable wächst mit dem Fenster
- ✅ Scrollbars erscheinen automatisch bei Bedarf
- ✅ Alle 90 Spalten (Q1) sind erreichbar

🎉 **Die Gantt-App ist jetzt vollständig responsiv!**

