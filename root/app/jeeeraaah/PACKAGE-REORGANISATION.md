# Package Reorganisation: Flat/Lazy Sub-Packages

**Datum:** 2026-02-20  
**Status:** ✅ IMPLEMENTIERT

---

## 🎯 Ziel

Flat- und Lazy-Typen in `common/api/domain` als Sub-Packages organisieren für bessere Struktur und Übersichtlichkeit.

---

## ✅ Durchgeführte Änderungen

### 1. Neue Package-Struktur erstellt

```
common/api/domain/
├── (root package)          # Haupt-Domain-Interfaces
│   ├── Task.java
│   ├── TaskGroup.java
│   ├── TaskEntity.java
│   ├── TaskGroupEntity.java
│   ├── TaskService.java
│   └── TaskGroupService.java
├── flat/                   # Flache Repräsentationen (Performance-optimiert)
│   ├── TaskFlat.java
│   └── TaskGroupFlat.java
└── lazy/                   # Lazy-Loading-Varianten (mit IDs)
    ├── TaskLazy.java
    └── TaskGroupLazy.java
```

### 2. Dateien verschoben

**Vorher:**
```
de.ruu.app.jeeeraaah.common.api.domain.TaskFlat
de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat
de.ruu.app.jeeeraaah.common.api.domain.TaskLazy
de.ruu.app.jeeeraaah.common.api.domain.TaskGroupLazy
```

**Nachher:**
```
de.ruu.app.jeeeraaah.common.api.domain.flat.TaskFlat
de.ruu.app.jeeeraaah.common.api.domain.flat.TaskGroupFlat
de.ruu.app.jeeeraaah.common.api.domain.lazy.TaskLazy
de.ruu.app.jeeeraaah.common.api.domain.lazy.TaskGroupLazy
```

### 3. Module-Info aktualisiert

**`module-info.java`:**
```java
module de.ruu.app.jeeeraaah.common.api.domain
{
	exports de.ruu.app.jeeeraaah.common.api.domain;
	exports de.ruu.app.jeeeraaah.common.api.domain.flat;    // NEU
	exports de.ruu.app.jeeeraaah.common.api.domain.lazy;    // NEU

	// ... requires ...

	opens de.ruu.app.jeeeraaah.common.api.domain      to lombok, com.fasterxml.jackson.databind;
	opens de.ruu.app.jeeeraaah.common.api.domain.flat to lombok, com.fasterxml.jackson.databind;  // NEU
	opens de.ruu.app.jeeeraaah.common.api.domain.lazy to lombok, com.fasterxml.jackson.databind;  // NEU
}
```

### 4. Alle Imports automatisiert aktualisiert

**Geänderte Imports in allen Modulen:**
```java
// Alt:
import de.ruu.app.jeeeraaah.common.api.domain.TaskFlat;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat;
import de.ruu.app.jeeeraaah.common.api.domain.TaskLazy;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupLazy;

// Neu:
import de.ruu.app.jeeeraaah.common.api.domain.flat.TaskFlat;
import de.ruu.app.jeeeraaah.common.api.domain.flat.TaskGroupFlat;
import de.ruu.app.jeeeraaah.common.api.domain.lazy.TaskLazy;
import de.ruu.app.jeeeraaah.common.api.domain.lazy.TaskGroupLazy;
```

**Betroffene Module:**
- ✅ `common/api/domain` (TaskGroupService.java)
- ✅ `common/api/ws.rs` (TaskDTOLazy, TaskGroupDTOLazy, TaskDTOFlat, TaskGroupDTOFlat)
- ✅ `common/api/mapping` (alle Mapper-Interfaces)
- ✅ `backend/common/mapping.*` (alle JPA-Mapper)
- ✅ `frontend/ui/fx` (alle UI-Controller)

### 5. Dokumentation aktualisiert

**`jpms in action - jeeeraaah.md`:**
- ✅ Package-Struktur dokumentiert
- ✅ Zweck von flat/lazy erklärt
- ✅ Performance-Optimierungen beschrieben

---

## 📦 Zweck der Sub-Packages

### `domain.flat` - Flache Repräsentationen

**Zweck:**
- Performance-Optimierung
- Nur Kern-Felder ohne teure Beziehungen
- Ideal für Listen-Anzeigen, Auswahl-Dialoge

**Beispiel TaskFlat:**
```java
public interface TaskFlat extends Entity<Long>
{
	@NonNull String         name();
	Optional<String>        description();
	Optional<LocalDate>     start();
	Optional<LocalDate>     end();
	Optional<Long>          superTaskId(); // Nur ID, nicht das ganze Objekt!
}
```

### `domain.lazy` - Lazy-Loading-Varianten

**Zweck:**
- Verzögertes Laden von Beziehungen
- IDs anstelle von vollständigen Objekten
- Reduzierte Netzwerk- und Speicherlast

**Beispiel TaskLazy:**
```java
public interface TaskLazy extends TaskFlat
{
	@NonNull  Long taskGroupId();      // Nur ID, nicht das ganze TaskGroup-Objekt
	@Nullable Long superTaskId();      // Nur ID
	
	@NonNull Set<Long> subTaskIds();       // Nur IDs der Subtasks
	@NonNull Set<Long> predecessorIds();   // Nur IDs
	@NonNull Set<Long> successorIds();     // Nur IDs
}
```

---

## 🎯 Vorteile der neuen Struktur

| Aspekt | Vorher | Nachher |
|--------|--------|---------|
| **Übersichtlichkeit** | ⚠️ Alle Typen im Root | ✅ Logische Gruppierung |
| **Verständlichkeit** | ⚠️ Zweck nicht klar | ✅ Zweck durch Package ersichtlich |
| **Wartbarkeit** | ⚠️ Schwer zu finden | ✅ Einfach zu lokalisieren |
| **Konsistenz** | ⚠️ Gemischt | ✅ Klare Trennung |

---

## 🧪 Build-Status

**Befehl:**
```bash
cd ~/develop/github/main/root
mvn clean install -DskipTests
```

**Erwartetes Ergebnis:**
```
[INFO] BUILD SUCCESS
```

---

## 📚 Siehe auch

- **`jpms in action - jeeeraaah.md`** - Vollständige Projektdokumentation
- **`PORTABLE-SOLUTION.md`** - Portable Config-Lösung
- **`START-HERE.md`** - Quick Start Guide

---

**Erstellt:** 2026-02-20  
**Status:** ✅ IMPLEMENTIERT  
**Build:** In Arbeit...

