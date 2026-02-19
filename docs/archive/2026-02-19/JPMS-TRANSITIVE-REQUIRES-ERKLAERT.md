# JPMS: Transitive Dependencies erklärt
## Was bedeutet "transitively required"?
### Kurz gesagt:
**"Transitively required"** bedeutet, dass ein Modul nicht nur für sich selbst eine Abhängigkeit deklariert, sondern diese Abhängigkeit auch an alle Module weitergibt, die es selbst verwenden.
---
## Visuelles Beispiel aus Ihrem Projekt
### Situation mit `requires transitive`:
```
┌─────────────────────────────────────────────────────────┐
│ common.api.domain                                       │
│ - TaskEntity, TaskGroupEntity (Core Domain Interfaces) │
└─────────────────────────────────────────────────────────┘
                    ↑                    ↑
                    │                    │
      requires transitive    requires transitive
                    │                    │
┌───────────────────┴─────┐    ┌────────┴────────────────┐
│ common.api.ws.rs        │    │ frontend.ui.fx          │
│ - TaskDTO               │    │ - TaskController        │
│ (nutzt TaskEntity)      │    │ (nutzt TaskEntity)      │
└─────────────────────────┘    └─────────────────────────┘
                    ↑                    ↑
                    │                    │
              requires             requires
                    │                    │
┌───────────────────┴─────┐    ┌────────┴────────────────┐
│ backend.api.ws.rs       │    │ frontend.ui.fx.gantt    │
│ - TaskService           │    │ - GanttController       │
│                         │    │                         │
│ ✅ Hat AUTOMATISCH      │    │ ✅ Hat AUTOMATISCH      │
│    Zugriff auf          │    │    Zugriff auf          │
│    TaskEntity!          │    │    TaskEntity!          │
└─────────────────────────┘    └─────────────────────────┘
```
---
## Code-Beispiel
### Mit `requires transitive`:
```java
// ═══════════════════════════════════════════════════════
// common.api.domain/module-info.java
// ═══════════════════════════════════════════════════════
module de.ruu.app.jeeeraaah.common.api.domain {
    exports de.ruu.app.jeeeraaah.common.api.domain;
}
// ═══════════════════════════════════════════════════════
// common.api.ws.rs/module-info.java
// ═══════════════════════════════════════════════════════
module de.ruu.app.jeeeraaah.common.api.ws.rs {
    exports de.ruu.app.jeeeraaah.common.api.ws.rs;
    // ⚠️ WICHTIG: "transitive" macht den Unterschied!
    requires transitive de.ruu.app.jeeeraaah.common.api.domain;
}
// ═══════════════════════════════════════════════════════
// backend.api.ws.rs/module-info.java
// ═══════════════════════════════════════════════════════
module de.ruu.app.jeeeraaah.backend.api.ws.rs {
    exports de.ruu.app.jeeeraaah.backend.api.ws.rs;
    requires de.ruu.app.jeeeraaah.common.api.ws.rs;
    // ✅ Muss NICHT explizit common.api.domain requiren!
    // Bekommt es automatisch durch "requires transitive"
}
// ═══════════════════════════════════════════════════════
// In backend.api.ws.rs/TaskService.java
// ═══════════════════════════════════════════════════════
package de.ruu.app.jeeeraaah.backend.api.ws.rs;
// ✅ FUNKTIONIERT - obwohl TaskEntity aus common.api.domain kommt!
import de.ruu.app.jeeeraaah.common.api.domain.TaskEntity;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
public class TaskService {
    public TaskDTO convert(TaskEntity entity) {
        // ✅ Kann beide Typen nutzen!
    }
}
```
---
## OHNE `requires transitive` würde es SO aussehen:
```java
// ═══════════════════════════════════════════════════════
// common.api.ws.rs/module-info.java (OHNE transitive)
// ═══════════════════════════════════════════════════════
module de.ruu.app.jeeeraaah.common.api.ws.rs {
    exports de.ruu.app.jeeeraaah.common.api.ws.rs;
    // ❌ NUR "requires" - NICHT transitiv!
    requires de.ruu.app.jeeeraaah.common.api.domain;
}
// ═══════════════════════════════════════════════════════
// backend.api.ws.rs/module-info.java (OHNE transitive)
// ═══════════════════════════════════════════════════════
module de.ruu.app.jeeeraaah.backend.api.ws.rs {
    exports de.ruu.app.jeeeraaah.backend.api.ws.rs;
    requires de.ruu.app.jeeeraaah.common.api.ws.rs;
    // ❌ MUSS jetzt EXPLIZIT common.api.domain requiren!
    requires de.ruu.app.jeeeraaah.common.api.domain;
}
// In backend.api.ws.rs/TaskService.java
// ═══════════════════════════════════════════════════════
package de.ruu.app.jeeeraaah.backend.api.ws.rs;
// ❌ KOMPILIERT NICHT ohne "requires common.api.domain" im module-info!
import de.ruu.app.jeeeraaah.common.api.domain.TaskEntity;
```
---
## Wann verwendet man `requires transitive`?
### ✅ JA - Verwende `requires transitive` wenn:
1. **API-Typen weitergegeben werden**
   ```java
   // TaskDTO ENTHÄLT TaskEntity in seiner Signatur
   public class TaskDTO {
       public TaskEntity getEntity() { ... }
   }
   // → Jeder der TaskDTO nutzt, braucht auch TaskEntity!
   ```
2. **Basis-Interfaces/Abstrakte Klassen**
   ```java
   // Service ERWEITERT BaseService
   public class TaskService extends BaseService { ... }
   // → Jeder der TaskService nutzt, sieht auch BaseService!
   ```
3. **Gemeinsame Domain-Typen**
   ```java
   // Alle Module brauchen die Domain-Typen
   // → Ein zentrales Modul macht sie transitiv verfügbar
   ```
### ❌ NEIN - Normale `requires` wenn:
1. **Implementierungs-Details**
   ```java
   // Verwendet intern Jackson, aber API zeigt das nicht
   requires com.fasterxml.jackson.databind;  // NICHT transitive
   ```
2. **Utility-Dependencies**
   ```java
   // Nutzt intern Logging
   requires org.slf4j;  // NICHT transitive
   ```
---
## In Ihrem Projekt
### Module die `common.api.domain` transitiv machen:
1. **`common.api.ws.rs`**
   ```java
   requires transitive de.ruu.app.jeeeraaah.common.api.domain;
   ```
   **Grund:** TaskDTO und TaskGroupDTO enthalten/referenzieren TaskEntity
2. **`frontend.ui.fx`**
   ```java
   requires transitive de.ruu.app.jeeeraaah.common.api.domain;
   ```
   **Grund:** Controller arbeiten direkt mit Domain-Typen
### Resultat:
Alle Module die `common.api.ws.rs` oder `frontend.ui.fx` verwenden,
bekommen **automatisch** Zugriff auf `common.api.domain` - ohne es
explizit zu requiren!
---
## Vorteile von `requires transitive`
1. ✅ **Weniger Redundanz** - Nicht jedes Modul muss common.api.domain requiren
2. ✅ **Klarere Abhängigkeiten** - API-Module dokumentieren ihre transitiven Abhängigkeiten
3. ✅ **Einfacheres Refactoring** - Änderungen an transitiven Dependencies zentral
4. ✅ **Konsistente Versionen** - Alle Module nutzen dieselbe Version von common.api.domain
---
## Zusammenfassung
**"Transitively required"** = Ein Modul wird durch `requires transitive` 
nicht nur für sich selbst, sondern auch für alle abhängigen Module verfügbar gemacht.
**In Ihrem Projekt:** `common.api.domain` wird von mehreren Modulen transitiv 
required, sodass alle Module die diese nutzen, automatisch Zugriff auf die 
Domain-Typen (TaskEntity, TaskGroupEntity, etc.) haben.
**Analogie:** Wie beim "Erben" von Dependencies - wer das Kind (API-Modul) nutzt,
bekommt auch die Gene (Domain-Typen) der Eltern!
