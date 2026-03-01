# Mapping-Struktur Analyse & Empfehlung
## 🔍 Aktuelle Struktur (IST-Zustand)
### Vorhandene Mappings:
```
1. common.api.mapping
   - Bean ↔ DTO       (bidirektional) ✅
   - Bean ↔ Lazy      (bidirektional) ✅
   - Flat → Bean      (unidirektional)
2. frontend.common.mapping
   - Bean ↔ FXBean    (bidirektional) ✅
   - Bean → FlatBean  (unidirektional)
3. backend.common.mapping
   - JPA ↔ DTO        (bidirektional) ✅
   - JPA ↔ Lazy       (bidirektional) ✅
```
### Problem-Analyse:
❌ **Inkonsistente Namensgebung**
- `common.api.mapping` vs. `frontend.common.mapping` vs. `backend.common.mapping`
❌ **Unklare Verantwortlichkeiten**
- Wo gehört Bean ↔ DTO? (aktuell: common.api.mapping)
- Wo gehört JPA ↔ DTO? (aktuell: backend.common.mapping)
- Warum nicht zusammen?
❌ **Redundante Package-Namen**
- `bean.fxbean` und `fxbean.bean` - verwirrend!
---
## 💡 Empfehlung: Namensbasierte Module
### Option A: **Technologie-basiert** (EMPFOHLEN ✅)
Jedes Mapping bekommt ein eigenes Modul basierend auf Source→Target:
```
common/
├── api/
│   ├── mapping/
│   │   ├── bean.dto/          ← Bean ↔ DTO
│   │   ├── bean.lazy/         ← Bean ↔ Lazy  
│   │   ├── dto.bean/          ← DTO → Bean (optional wenn bean.dto bidirektional)
│   │   └── flat.bean/         ← Flat → Bean
frontend/
├── common/
│   ├── mapping/
│   │   ├── bean.fxbean/       ← Bean ↔ FXBean
│   │   └── bean.flat/         ← Bean → Flat
backend/
├── common/
│   ├── mapping/
│   │   ├── jpa.dto/           ← JPA ↔ DTO
│   │   └── jpa.lazy/          ← JPA ↔ Lazy
```
**Vorteile:**
- ✅ **Klare Verantwortlichkeiten** - jedes Modul hat genau 1 Zweck
- ✅ **Selbsterklärend** - Name sagt, was gemappt wird
- ✅ **Feingranular** - einzelne Mappings können unabhängig versioniert werden
- ✅ **JPMS-freundlich** - kleinere Module, gezielte Dependencies
**Module-Struktur:**
```
module de.ruu.app.jeeeraaah.common.api.mapping.bean.dto {
    exports de.ruu.app.jeeeraaah.common.api.mapping.bean.dto;
    requires de.ruu.app.jeeeraaah.common.api.bean;
    requires de.ruu.app.jeeeraaah.common.api.ws.rs;
    requires de.ruu.lib.mapstruct;
}
```
---
### Option B: **Layer-basiert** (Konsolidiert)
Weniger Module, mehr Packages:
```
common/
├── api/
│   └── mapping/                ← ALLE API-Mappings in 1 Modul
│       ├── bean/
│       │   ├── dto/            ← Bean → DTO
│       │   ├── lazy/           ← Bean → Lazy
│       │   └── flat/           ← Bean → Flat
│       └── dto/
│           └── bean/           ← DTO → Bean
frontend/
├── common/
│   └── mapping/                ← ALLE Frontend-Mappings in 1 Modul
│       ├── bean/
│       │   ├── fxbean/         ← Bean → FXBean
│       │   └── flat/           ← Bean → Flat
│       └── fxbean/
│           └── bean/           ← FXBean → Bean
backend/
├── common/
│   └── mapping/                ← ALLE Backend-Mappings in 1 Modul
│       ├── jpa/
��       │   ├── dto/            ← JPA → DTO
│       │   └── lazy/           ← JPA → Lazy
│       ├── dto/
│       │   └── jpa/            ← DTO → JPA
│       └── lazy/
│           └── jpa/            ← Lazy → JPA
```
**Vorteile:**
- ✅ **Einfacher** - weniger Module
- ✅ **Weniger Overhead** - weniger pom.xml, module-info.java
- ✅ **Aktuelle Struktur** - ähnlich wie jetzt
**Nachteile:**
- ❌ **Grobgranular** - große Module mit vielen Verantwortlichkeiten
- ❌ **Mehr Dependencies** - jedes Modul braucht alle beteiligten Typen
---
## 🎯 Meine Empfehlung: **Hybrid-Ansatz**
**Konsolidiere verwandte Mappings, aber trenne unterschiedliche Layer:**
```
common/
├── api/
│   ├── mapping.bean.dto/       ← Bean ↔ DTO (bidirektional)
│   ├── mapping.bean.lazy/      ← Bean ↔ Lazy (bidirektional)
│   └── mapping.flat.bean/      ← Flat → Bean (unidirektional)
frontend/
├── common/
│   └── mapping.bean.fxbean/    ← Bean ↔ FXBean (bidirektional)
backend/
├── common/
│   ├── mapping.jpa.dto/        ← JPA ↔ DTO (bidirektional)
│   └── mapping.jpa.lazy/       ← JPA ↔ Lazy (bidirektional)
```
### Begründung:
1. **Bidirektionale Mappings in 1 Modul**
   - Bean ↔ DTO beide Richtungen zusammen
   - Verhindert Duplikation
2. **Klare Namenskonvention**
   - `mapping.SOURCE.TARGET`
   - Sofort erkennbar was gemappt wird
3. **Technologie-Trennung**
   - Frontend (FXBean) getrennt von Backend (JPA)
   - API-Layer (Bean/DTO) neutral in common
4. **Praktikabel**
   - Nicht zu viele Module (6 statt 12+)
   - Nicht zu wenig (nicht alles in 1 Modul)
---
## 📋 Package-Struktur innerhalb der Module
Für **bidirektionale** Mappings:
```
mapping.bean.dto/
├── src/main/java/
│   └── de/ruu/app/jeeeraaah/common/api/mapping/bean/dto/
│       ├── Mappers.java                    ← Facade
│       ├── Map_Task_Bean_DTO.java          ← Bean → DTO
│       ├── Map_Task_DTO_Bean.java          ← DTO → Bean
│       ├── Map_TaskGroup_Bean_DTO.java
│       └── Map_TaskGroup_DTO_Bean.java
└── module-info.java
```
**Facade-Klasse** für einfache Nutzung:
```java
public interface Mappers {
    // Bean → DTO
    static TaskDTO toDTO(TaskBean bean, ReferenceCycleTracking ctx) {
        return Map_Task_Bean_DTO.INSTANCE.map(bean, ctx);
    }
    // DTO → Bean
    static TaskBean toBean(TaskDTO dto, ReferenceCycleTracking ctx) {
        return Map_Task_DTO_Bean.INSTANCE.map(dto, ctx);
    }
}
```
---
## 🔄 Migration-Plan
### Phase 1: Umbenennen (Low-Risk)
Einfach Module umbenennen ohne Struktur zu ändern:
```bash
# Aktuell:
common/api/mapping/                    → mapping.bean.dto/
frontend/common/mapping/               → mapping.bean.fxbean/
backend/common/mapping/                → mapping.jpa.dto/
```
### Phase 2: Aufteilen (Medium-Risk)
Große Module in kleinere aufteilen:
```bash
common/api/mapping/                    
  → mapping.bean.dto/                  # Bean ↔ DTO
  → mapping.bean.lazy/                 # Bean ↔ Lazy
  → mapping.flat.bean/                 # Flat → Bean
```
### Phase 3: Konsolidieren (High-Value)
Package-Struktur vereinfachen:
```bash
# Vorher:
frontend/common/mapping/bean/fxbean/
frontend/common/mapping/fxbean/bean/
# Nachher:
frontend/common/mapping_bean_fxbean/
  ├── BeanToFXBeanMapper.java
  └── FXBeanToBeanMapper.java
```
---
## 📊 Vergleichstabelle
| Aspekt | Aktuell | Option A (Viele Module) | Option B (Wenige Module) | **Hybrid (Empfohlen)** |
|--------|---------|------------------------|-------------------------|----------------------|
| Anzahl Module | 3 | 12+ | 3 | **6** |
| Klarheit | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | **⭐⭐⭐⭐** |
| Wartbarkeit | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | **⭐⭐⭐⭐** |
| Overhead | Niedrig | Hoch | Niedrig | **Mittel** |
| JPMS-Fit | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ | **⭐⭐⭐⭐** |
| Refactoring-Aufwand | - | Hoch | Niedrig | **Mittel** |
---
## ✅ Konkrete Empfehlung
### Start: **Quick Win - Module umbenennen**
```
1. common.api.mapping           → common.api.mapping.bean.dto
2. frontend.common.mapping      → frontend.common.mapping.bean.fxbean  
3. backend.common.mapping       → backend.common.mapping.jpa.dto
```
**Aufwand:** 30 Minuten  
**Nutzen:** Sofort klarere Namen
### Mittelfristig: **Aufteilen wenn nötig**
Wenn `common.api.mapping.bean.dto` zu groß wird:
- Lazy-Mappings in eigenes Modul auslagern
- Flat-Mappings in eigenes Modul auslagern
**Aufwand:** 2-3 Stunden  
**Nutzen:** Feinere Granularität, bessere Wartbarkeit
---
## 🎯 Finale Empfehlung
**HYBRID-ANSATZ mit klarer Namenskonvention:**
```
✅ mapping.SOURCE.TARGET Schema
✅ Bidirektionale Mappings in 1 Modul
✅ 6 fokussierte Module statt 3 große oder 12+ kleine
✅ Pragmatisch und wartbar
```
**Soll ich diese Reorganisation durchführen?**
