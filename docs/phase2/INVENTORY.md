# Mapper Inventur - Phase 2 Vorbereitung

**Datum**: 2026-02-14  
**Status**: ✅ VOLLSTÄNDIG  
**Zweck**: Basis für Phase 2 Neustart

---

## 📊 Übersicht

### Gesamt
- **20 Mapper-Interfaces** gefunden
- **3 Module** (aktuell)
- **5 verschiedene Mapping-Typen** identifiziert

---

## 🗂️ Modul 1: `common.api.mapping.bean.dto`

**Pfad**: `root/app/jeeeraaah/common/api/mapping.bean.dto/`  
**Verantwortung**: Bean ↔ DTO, Bean ↔ Lazy, Flat → Bean (GEMISCHT!)

### Mapper (10 Stück)

#### Bean ↔ DTO (2)
1. **Map_TaskGroup_Bean_DTO**
   - Pfad: `src/main/java/.../bean/dto/Map_TaskGroup_Bean_DTO.java`
   - Richtung: TaskGroupBean → TaskGroupDTO
   - Status: ✅ Korrekt platziert

2. **Map_TaskGroup_DTO_Bean**
   - Pfad: `src/main/java/.../dto/bean/Map_TaskGroup_DTO_Bean.java`
   - Richtung: TaskGroupDTO → TaskGroupBean
   - Status: ✅ Korrekt platziert

#### Bean ↔ Lazy (4)
3. **Map_TaskGroup_Bean_Lazy**
   - Pfad: `src/main/java/.../bean/lazy/Map_TaskGroup_Bean_Lazy.java`
   - Richtung: TaskGroupBean → TaskGroupLazy
   - Status: ⚠️ Sollte nach `mapping.bean.lazy` (Phase 2)

4. **Map_Task_Bean_Lazy**
   - Pfad: `src/main/java/.../bean/lazy/Map_Task_Bean_Lazy.java`
   - Richtung: TaskBean → TaskLazy
   - Status: ⚠️ Sollte nach `mapping.bean.lazy` (Phase 2)

5. **Map_TaskGroup_Lazy_Bean**
   - Pfad: `src/main/java/.../lazy/bean/Map_TaskGroup_Lazy_Bean.java`
   - Richtung: TaskGroupLazy → TaskGroupBean
   - Status: ⚠️ Sollte nach `mapping.bean.lazy` (Phase 2)

6. **Map_Task_Lazy_Bean**
   - Pfad: `src/main/java/.../lazy/bean/Map_Task_Lazy_Bean.java`
   - Richtung: TaskLazy → TaskBean
   - Status: ⚠️ Sollte nach `mapping.bean.lazy` (Phase 2)

#### Flat → Bean (1)
7. **Map_TaskGroup_Flat_Bean**
   - Pfad: `src/main/java/.../flat/bean/Map_TaskGroup_Flat_Bean.java`
   - Richtung: TaskGroupFlat → TaskGroupBean
   - Status: ⚠️ Sollte nach `mapping.flat.bean` (Phase 2)

### Probleme
- ❌ **Mixed Responsibility**: 3 verschiedene Mapping-Typen in 1 Modul
- ❌ **Naming Inkonsistent**: Modul heißt `.bean.dto` aber enthält auch Lazy + Flat
- ⚠️ 5 Mapper müssen verschoben werden (Phase 2)

---

## 🗂️ Modul 2: `backend.common.mapping.jpa.dto`

**Pfad**: `root/app/jeeeraaah/backend/common/mapping_jpa_dto/`  
**Verantwortung**: JPA ↔ DTO, JPA ↔ Lazy (GEMISCHT!)

### Mapper (10 Stück)

#### JPA ↔ DTO (4)
1. **Map_TaskGroup_JPA_DTO**
   - Pfad: `src/main/java/.../jpa/dto/Map_TaskGroup_JPA_DTO.java`
   - Richtung: TaskGroupJPA → TaskGroupDTO
   - Status: ✅ Korrekt platziert

2. **Map_Task_JPA_DTO**
   - Pfad: `src/main/java/.../jpa/dto/Map_Task_JPA_DTO.java`
   - Richtung: TaskJPA → TaskDTO
   - Status: ✅ Korrekt platziert

3. **Map_TaskGroup_DTO_JPA**
   - Pfad: `src/main/java/.../dto/jpa/Map_TaskGroup_DTO_JPA.java`
   - Richtung: TaskGroupDTO → TaskGroupJPA
   - Status: ✅ Korrekt platziert

4. **Map_Task_DTO_JPA**
   - Pfad: `src/main/java/.../dto/jpa/Map_Task_DTO_JPA.java`
   - Richtung: TaskDTO → TaskJPA
   - Status: ✅ Korrekt platziert

#### JPA ↔ Lazy (4)
5. **Map_TaskGroup_JPA_Lazy**
   - Pfad: `src/main/java/.../jpa/lazy/Map_TaskGroup_JPA_Lazy.java`
   - Richtung: TaskGroupJPA → TaskGroupLazy
   - Status: ⚠️ Sollte nach `mapping.jpa.lazy` (Phase 2)

6. **Map_Task_JPA_Lazy**
   - Pfad: `src/main/java/.../jpa/lazy/Map_Task_JPA_Lazy.java`
   - Richtung: TaskJPA → TaskLazy
   - Status: ⚠️ Sollte nach `mapping.jpa.lazy` (Phase 2)

7. **Map_TaskGroup_Lazy_JPA**
   - Pfad: `src/main/java/.../lazy/jpa/Map_TaskGroup_Lazy_JPA.java`
   - Richtung: TaskGroupLazy → TaskGroupJPA
   - Status: ⚠️ Sollte nach `mapping.jpa.lazy` (Phase 2)

8. **Map_Task_Lazy_JPA**
   - Pfad: `src/main/java/.../lazy/jpa/Map_Task_Lazy_JPA.java`
   - Richtung: TaskLazy → TaskJPA
   - Status: ⚠️ Sollte nach `mapping.jpa.lazy` (Phase 2)

### Probleme
- ❌ **Mixed Responsibility**: JPA↔DTO und JPA↔Lazy in 1 Modul
- ❌ **Naming Inkonsistent**: Modul heißt `.jpa.dto` aber enthält auch Lazy
- ⚠️ 4 Mapper müssen verschoben werden (Phase 2)

---

## 🗂️ Modul 3: `frontend.common.mapping.bean.fxbean`

**Pfad**: `root/app/jeeeraaah/frontend/common/mapping_bean_fxbean/`  
**Verantwortung**: Bean ↔ FXBean, Bean → FlatBean (FAST KORREKT)

### Mapper (5 Stück)

#### Bean ↔ FXBean (4)
1. **Map_TaskGroup_Bean_FXBean**
   - Pfad: `src/main/java/.../bean/fxbean/Map_TaskGroup_Bean_FXBean.java`
   - Richtung: TaskGroupBean → TaskGroupFXBean
   - Status: ✅ Korrekt platziert

2. **Map_Task_Bean_FXBean**
   - Pfad: `src/main/java/.../bean/fxbean/Map_Task_Bean_FXBean.java`
   - Richtung: TaskBean → TaskFXBean
   - Status: ✅ Korrekt platziert

3. **Map_TaskGroup_FXBean_Bean**
   - Pfad: `src/main/java/.../fxbean/bean/Map_TaskGroup_FXBean_Bean.java`
   - Richtung: TaskGroupFXBean → TaskGroupBean
   - Status: ✅ Korrekt platziert

4. **Map_Task_FXBean_Bean**
   - Pfad: `src/main/java/.../fxbean/bean/Map_Task_FXBean_Bean.java`
   - Richtung: TaskFXBean → TaskBean
   - Status: ✅ Korrekt platziert

#### Bean → FlatBean (1)
5. **Map_TaskGroup_Bean_FlatBean**
   - Pfad: `src/main/java/.../bean/flatbean/Map_TaskGroup_Bean_FlatBean.java`
   - Richtung: TaskGroupBean → TaskGroupFlatBean
   - Status: ⚠️ Diskussionswürdig (UI-spezifisch, könnte bleiben)

### Probleme
- ⚠️ **Leicht gemischte Responsibility**: FlatBean-Mapper könnte separiert werden
- ✅ **Naming konsistent**: Modul-Name passt zu Hauptaufgabe

---

## 📋 Zusammenfassung nach Mapping-Typ

### 1. Bean ↔ DTO (2 Mapper)
- Map_TaskGroup_Bean_DTO ✅
- Map_TaskGroup_DTO_Bean ✅
- **Modul**: `common.api.mapping.bean.dto` ✅

### 2. Bean ↔ Lazy (4 Mapper)
- Map_TaskGroup_Bean_Lazy ⚠️
- Map_Task_Bean_Lazy ⚠️
- Map_TaskGroup_Lazy_Bean ⚠️
- Map_Task_Lazy_Bean ⚠️
- **Aktuelles Modul**: `common.api.mapping.bean.dto` ❌
- **Ziel-Modul**: `common.api.mapping.bean.lazy` (NEU in Phase 2)

### 3. Flat → Bean (1 Mapper)
- Map_TaskGroup_Flat_Bean ⚠️
- **Aktuelles Modul**: `common.api.mapping.bean.dto` ❌
- **Ziel-Modul**: `common.api.mapping.flat.bean` (NEU in Phase 2)

### 4. JPA ↔ DTO (4 Mapper)
- Map_TaskGroup_JPA_DTO ✅
- Map_Task_JPA_DTO ✅
- Map_TaskGroup_DTO_JPA ✅
- Map_Task_DTO_JPA ✅
- **Modul**: `backend.common.mapping.jpa.dto` ✅

### 5. JPA ↔ Lazy (4 Mapper)
- Map_TaskGroup_JPA_Lazy ⚠️
- Map_Task_JPA_Lazy ⚠️
- Map_TaskGroup_Lazy_JPA ⚠️
- Map_Task_Lazy_JPA ⚠️
- **Aktuelles Modul**: `backend.common.mapping.jpa.dto` ❌
- **Ziel-Modul**: `backend.common.mapping.jpa.lazy` (NEU in Phase 2)

### 6. Bean ↔ FXBean (4 Mapper)
- Map_TaskGroup_Bean_FXBean ✅
- Map_Task_Bean_FXBean ✅
- Map_TaskGroup_FXBean_Bean ✅
- Map_Task_FXBean_Bean ✅
- **Modul**: `frontend.common.mapping.bean.fxbean` ✅

### 7. Bean → FlatBean (1 Mapper)
- Map_TaskGroup_Bean_FlatBean ⚠️
- **Modul**: `frontend.common.mapping.bean.fxbean` (diskussionswürdig)

---

## 🎯 Phase 2 Aufgaben (aus Inventur abgeleitet)

### Neue Module erstellen (3 Stück)

1. **`common.api.mapping.bean.lazy`**
   - Empfängt: 4 Mapper aus `mapping.bean.dto`
   - Verantwortung: Bean ↔ Lazy

2. **`common.api.mapping.flat.bean`**
   - Empfängt: 1 Mapper aus `mapping.bean.dto`
   - Verantwortung: Flat → Bean

3. **`backend.common.mapping.jpa.lazy`**
   - Empfängt: 4 Mapper aus `mapping.jpa.dto`
   - Verantwortung: JPA ↔ Lazy

### Mapper verschieben (9 Stück)

#### Nach `common.api.mapping.bean.lazy` (4)
- Map_TaskGroup_Bean_Lazy
- Map_Task_Bean_Lazy
- Map_TaskGroup_Lazy_Bean
- Map_Task_Lazy_Bean

#### Nach `common.api.mapping.flat.bean` (1)
- Map_TaskGroup_Flat_Bean

#### Nach `backend.common.mapping.jpa.lazy` (4)
- Map_TaskGroup_JPA_Lazy
- Map_Task_JPA_Lazy
- Map_TaskGroup_Lazy_JPA
- Map_Task_Lazy_JPA

---

## ✅ Erfolgs-Kriterien nach Phase 2

### Module (6 Gesamt)
- ✅ `common.api.mapping.bean.dto` (2 Mapper: Bean↔DTO)
- ✅ `common.api.mapping.bean.lazy` (4 Mapper: Bean↔Lazy)
- ✅ `common.api.mapping.flat.bean` (1 Mapper: Flat→Bean)
- ✅ `backend.common.mapping.jpa.dto` (4 Mapper: JPA↔DTO)
- ✅ `backend.common.mapping.jpa.lazy` (4 Mapper: JPA↔Lazy)
- ✅ `frontend.common.mapping.bean.fxbean` (5 Mapper: Bean↔FXBean + FlatBean)

### Jedes Modul
- ✅ Klare Single Responsibility
- ✅ Name = SOURCE.TARGET Pattern
- ✅ Nur Mapper eines Typs
- ✅ Saubere Abhängigkeiten

---

**Status**: ✅ INVENTUR ABGESCHLOSSEN  
**Nächster Schritt**: Dependency-Graph erstellen  
**Zeitaufwand**: ~30 Minuten

