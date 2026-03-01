# JPMS Reference Guide - Complete Documentation

**Last Updated:** 2026-03-01  
**Purpose:** Comprehensive guide for JPMS usage in this project

---

## Table of Contents

1. [Package-Hiding Strategy](#1-package-hiding-strategy)
2. [Opens Directives - Best Practices](#2-opens-directives---best-practices)
3. [IntelliJ Run Configuration](#3-intellij-run-configuration)
4. [Encapsulation Improvements](#4-encapsulation-improvements)
5. [Run Configuration Details](#5-run-configuration-details)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. Package-Hiding Strategy

### 🎯 Core Principle

**JPMS enables:**
- A `public` type in a **non-exported** package is **completely hidden**
- Only exported packages are visible from outside
- Qualified exports grant access only to specific modules

### Example:
```java
module my.module {
    // ✅ Public API
    exports com.example.api;
    
    // ❌ NOT exported = completely hidden
    // com.example.internal remains private, even if classes are public!
    
    // ✅ Only for specific modules
    exports com.example.impl to framework.module;
}
```

### 📦 Recommended Package Structure

#### Pattern 1: API + Internal Pattern
```
my.module/
├── api/                    ← Exported (public API)
│   ├── MyService.java
│   └── MyDTO.java
├── internal/               ← NOT exported (implementation)
│   ├── MyServiceImpl.java
│   └── MyHelper.java
└── spi/                    ← Qualified export (for frameworks)
    └── MyExtension.java
```

**module-info.java:**
```java
module my.module {
    exports my.module.api;                    // Public API
    exports my.module.spi to framework;       // Only for framework
    // my.module.internal remains hidden!
}
```

#### Pattern 2: Facade Pattern (Used in this project!)
```
mapping.module/
├── Mappings.java           ← Exported (facade)
├── jpa.dto/               ← Qualified export (MapStruct)
│   └── MapperImpl.java
└── dto.jpa/               ← Qualified export (MapStruct)
    └── MapperImpl.java
```

**module-info.java:**
```java
module mapping.module {
    exports mapping.module;                           // Facade
    exports mapping.module.jpa.dto to org.mapstruct;  // Only MapStruct
    exports mapping.module.dto.jpa to org.mapstruct;  // Only MapStruct
}
```

### ✅ Best Practices in This Project

#### **backend.common.mapping.jpa.dto** ⭐ Best Practice!
```java
module de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto {
    // ✅ Only facade exported
    exports de.ruu.app.jeeeraaah.backend.common.mapping;
    
    // ✅ Mappers only for MapStruct
    exports de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto 
        to org.mapstruct;
    
    // ✅ Minimal reflection
    opens de.ruu.app.jeeeraaah.backend.common.mapping 
        to weld.core.impl, weld.spi;
}
```

**Advantages:**
- Clients use only `Mappings` (the facade)
- Mapper implementations are hidden
- MapStruct can generate, but nobody else sees the classes

---

## 2. Opens Directives - Best Practices

### ✅ Current Implementation is Optimal!

The `opens` directives in project modules follow best practices:
```java
opens de.ruu.app.jeeeraaah.common.api.domain to lombok, com.fasterxml.jackson.databind;
```

### Why is this optimal?

#### 1. Targeted (✅ CORRECT)
```java
// ✅ Only specific modules have reflection access
opens package.name to module1, module2;

// ❌ AVOID: All modules would have access
opens package.name;
```

#### 2. Minimal (✅ CORRECT)
Only the **really needed** frameworks:
- `lombok` - for @AllArgsConstructor, @Getter, etc.
- `com.fasterxml.jackson.databind` - for @JsonProperty

#### 3. Necessary (✅ CORRECT)
Both frameworks **need** reflection for:
- Lombok: Compile-time code generation (annotation processing)
- Jackson: Runtime JSON serialization/deserialization

### JPMS Opens Variants Comparison

| Variant | Syntax | Access | Recommended? |
|---------|--------|---------|------------|
| **Fully open** | `opens package;` | All modules | ❌ Only when necessary |
| **Targeted** | `opens package to module1, module2;` | Only these modules | ✅ **BEST PRACTICE** |
| **Not at all** | (no opens) | No reflection | ✅ If possible |

### When do you need `opens`?

#### Reflection-based Frameworks

1. **Dependency Injection** (CDI, Spring, Guice)
   ```java
   opens de.ruu.app.services to weld.core.impl, org.jboss.weld.se.core;
   ```

2. **JSON Mapping** (Jackson, Jsonb)
   ```java
   opens de.ruu.app.dto to com.fasterxml.jackson.databind;
   ```

3. **ORM** (Hibernate, EclipseLink)
   ```java
   opens de.ruu.app.entities to org.hibernate.orm.core;
   ```

4. **Annotation Processing** (Lombok, MapStruct at runtime)
   ```java
   opens de.ruu.app.domain to lombok;
   ```

5. **Testing** (JUnit, Mockito)
   ```java
   opens de.ruu.app.internal to org.junit.platform.commons;
   ```

### Pattern Template for module-info.java

```java
/**
 * [Module description]
 * 
 * @since [version]
 */
module de.ruu.[module.name]
{
    // Public API
    exports de.ruu.[module.name];
    exports de.ruu.[module.name].api;
    
    // Dependencies
    requires transitive [api.module];
    requires [impl.module];
    requires static [optional.module];
    
    // Reflection access (minimal, targeted)
    // - Framework X: Reason Y
    // - Framework Z: Reason W
    opens de.ruu.[module.name].internal to [framework.x], [framework.z];
}
```

---

## 3. IntelliJ Run Configuration

### Problem Solved ✅

IntelliJ Run Configurations now use **JPMS Module Path** instead of classpath.

### What was changed?

#### 1. `.mvn/jvm.config` extended
```
root/app/jeeeraaah/frontend/ui/fx/.mvn/jvm.config
```
This file contains all JVM options and is **automatically** read by Maven and IntelliJ.

#### 2. ConfigHealthCheck corrected
Missing property names were added:
- `keycloak.test.user`
- `keycloak.test.password`

### How to use it in IntelliJ?

#### Option A: Automatic (Recommended)
1. **Right-click** on `DashAppRunner.java`
2. **Run 'DashAppRunner.main()'**
3. ✅ **Done!** IntelliJ automatically uses JPMS configuration

#### Option B: Manual
1. **Run → Edit Configurations...**
2. **+ → Application**
3. **Name:** DashAppRunner
4. **Main class:** `de.ruu.app.jeeeraaah.frontend.ui.fx.dash.DashAppRunner`
5. **Use classpath of module:** `de.ruu.app.jeeeraaah.frontend.ui.fx`
6. **Build and run:** `<Default> (Module Path)` ← **Important!**
7. **VM options:** Leave empty (read from `.mvn/jvm.config`)
8. **OK**

### What's the difference?

#### ❌ Before (wrong)
```
-cp <long list of JARs>
--add-modules jakarta.annotation,jakarta.inject
```
- Uses **Classpath** instead of Module Path
- Not JPMS-compliant
- Hard to maintain

#### ✅ Now (correct)
```
--module-path <module path>
--module de.ruu.app.jeeeraaah.frontend.ui.fx/de.ruu.app.jeeeraaah.frontend.ui.fx.dash.DashAppRunner
```
- Uses **Module Path**
- Fully JPMS-compliant
- All options in `.mvn/jvm.config`

### Advantages

1. **Single Point of Truth:** All JVM options in **one** file
2. **Maintainable:** Changes only in one place
3. **Team-consistent:** Works the same for all developers
4. **JPMS-compliant:** Uses Java Module System correctly

---

## 4. Encapsulation Improvements

### Project-wide Statistics (as of 28 Feb 2026)

#### Before improvements:
```
Total modules analyzed: 50
Total public types: 479
Hidden public types: 20
Encapsulation ratio: 4.0%
```

#### After improvements:
```
Total modules analyzed: 50
Total public types: 481 (+2 new interfaces)
Hidden public types: 43 (+23)
Encapsulation ratio: 8.9% (doubled!)
```

### Key Improvements

#### 1. lib.jpa.core - Removed criteria.restriction export ✅

**Problem:**
- Package `de.ruu.lib.jpa.core.criteria.restriction` was exported
- Contains 19 implementation classes (BetweenExpression, Conjunction, etc.)
- Only used internally (by AbstractRepository in same module)

**Solution:**
- Removed export from module-info.java
- Package is now fully encapsulated
- Only criteria facade (Criteria, Criterion, Order, Projection) remains accessible

**Result:**
- **19 implementation classes** now protected from external access
- Smaller public API surface

#### 2. backend.persistence.jpa - Hidden Implementation Classes ✅

**New package structure:**
```
backend.persistence.jpa/
├── de.ruu.app.jeeeraaah.backend.persistence.jpa/        [EXPORTED]
│   ├── TaskJPA.java                                      (Entity)
│   ├── TaskGroupJPA.java                                 (Entity)
│   ├── TaskCreationService.java                          (Interface - NEW)
│   └── TaskLazyMapper.java                               (Interface - NEW)
│
├── de.ruu.app.jeeeraaah.backend.persistence.jpa.ee/      [NOT EXPORTED]
│   ├── TaskServiceJPAEE.java                             (CDI Bean)
│   ├── TaskGroupServiceJPAEE.java                        (CDI Bean)
│   ├── TaskRepositoryJPAEE.java                          (CDI Bean)
│   └── TaskGroupRepositoryJPAEE.java                     (CDI Bean)
│
└── de.ruu.app.jeeeraaah.backend.persistence.jpa.internal/ [NOT EXPORTED - NEW]
    ├── TaskServiceJPA.java                               (Abstract Service)
    ├── TaskGroupServiceJPA.java                          (Abstract Service)
    ├── TaskRepositoryJPA.java                            (Abstract Repository)
    └── TaskGroupRepositoryJPA.java                       (Abstract Repository)
```

**module-info.java:**
```java
module de.ruu.app.jeeeraaah.backend.persistence.jpa {
    // Only entities and interfaces exported
    exports de.ruu.app.jeeeraaah.backend.persistence.jpa;
    
    // CDI access via 'opens' (NO export!)
    opens de.ruu.app.jeeeraaah.backend.persistence.jpa.ee to weld.se.shaded;
    opens de.ruu.app.jeeeraaah.backend.persistence.jpa.internal to weld.se.shaded;
}
```

**Hidden classes (before 4, now 8):**
- TaskServiceJPAEE, TaskGroupServiceJPAEE *(ee)*
- TaskRepositoryJPAEE, TaskGroupRepositoryJPAEE *(ee)*
- **TaskServiceJPA, TaskGroupServiceJPA** *(internal - NEW hidden)*
- **TaskRepositoryJPA, TaskGroupRepositoryJPA** *(internal - NEW hidden)*

**Advantages:**
- REST controllers work against interfaces, not concrete classes
- Implementation can be replaced without API changes
- Compile-time safety: Attempts to import `internal.*` result in compiler errors
- CDI injection still works via `opens`

### Modules with Hidden Implementation Classes

| Module | Hidden Classes | Packages |
|--------|----------------|----------|
| lib.jpa.core | 19 | criteria.restriction |
| backend.persistence.jpa | 8 | ee, internal |
| lib.jpa.core.mapstruct.demo.bidirectional | 6 | tree |
| backend.common.mapping | 3 | lazy.jpa |
| sandbox.office.microsoft.word.docx4j | 3 | (root) |
| frontend.api.client.ws.rs | 1 | example |
| lib.fx.demo | 1 | bean.tableview |
| lib.jsonb | 1 | recursion |
| lib.jasperreports.example | 1 | (root) |

---

## 5. Run Configuration Details

### Important Principles for JPMS

1. **No `provided` scope for module dependencies**: If a module is declared with `requires` in module-info.java, the corresponding Maven dependency must have `compile` scope.

2. **No --add-modules when possible**: Better to explicitly declare modules as dependencies.

3. **Module Path over Classpath**: JPMS applications should consistently use the Module Path.

4. **IntelliJ Module Setting**: Make sure "Use classpath of module" is disabled for JPMS configurations.

### POM Corrections Made

The `provided` scope was removed from `jakarta.annotation-api` in the following modules:
- `r-uu.app.jeeeraaah.frontend.api.client.ws.rs`
- `r-uu.app.jeeeraaah.common.api.domain`
- `r-uu.app.jeeeraaah.common.api.bean`
- `r-uu.app.jeeeraaah.frontend.ui.fx.model`
- `r-uu.lib.util`
- `r-uu.lib.mapstruct`
- `r-uu.lib.junit`

Now it's a normal compile dependency:
```xml
<dependency>
    <groupId>jakarta.annotation</groupId>
    <artifactId>jakarta.annotation-api</artifactId>
</dependency>
```

### JPMS-compliant Run Configurations

Two new IntelliJ run configurations were created in the `.run` folder:

#### DashAppRunner (JPMS).run.xml
- Starts the main GUI application
- Consistently uses the Module Path
- No more --add-modules parameters needed
- VM parameters: `-Dglass.gtk.uiScale=1.5`

#### DBClean (JPMS).run.xml
- Starts the DB-Clean tool
- Consistently uses the Module Path
- No more --add-modules parameters needed

---

## 6. Troubleshooting

### "Module X not found"
➜ Add module in `.mvn/jvm.config` under `--add-modules`

**Example:**
```
error: java.lang.module.FindException: Module jakarta.annotation not found
```

**Solution:**
Check POM - dependency should be `compile` scope, not `provided`.

### "Unable to make field accessible"
➜ Check `--add-opens` in `.mvn/jvm.config`

**Example:**
```
java.lang.reflect.InaccessibleObjectException: Unable to make field private final java.lang.String accessible
```

**Solution:**
Add targeted opens in module-info.java:
```java
opens de.ruu.app.package to framework.module;
```

### "Restricted method called"
➜ Check `--enable-native-access` in `.mvn/jvm.config`

### Compile error: "package X is not visible"

**Example:**
```
error: package de.ruu.app.jeeeraaah.backend.persistence.jpa.internal is not visible
  (package de.ruu.app.jeeeraaah.backend.persistence.jpa.internal is declared in module
   de.ruu.app.jeeeraaah.backend.persistence.jpa, which does not export it)
```

**This is GOOD!** This is JPMS working as intended - preventing access to non-exported packages.

**Solution:** Use the public API/interfaces instead of internal implementation classes.

### IntelliJ doesn't recognize modules

**Solution:**
1. **File → Invalidate Caches... → Invalidate and Restart**
2. **Maven → Reload All Maven Projects**
3. Ensure IntelliJ is using correct JDK (File → Project Structure → Project SDK)

---

## Summary & Best Practices

### ✅ Key Takeaways

1. **exports vs opens**
   - `exports`: Compile-time API visibility
   - `opens`: Runtime reflection access
   - Frameworks need `opens`, not `exports`!

2. **Package organization**
   - Public API → exported packages
   - Implementation → internal packages (not exported)
   - Framework SPI → qualified exports

3. **Avoid test-driven exports**
   - Don't export types just because tests need them
   - Adjust tests instead!

4. **Continuous improvement**
   - Encapsulation is not a one-time goal
   - Regular analysis finds improvement opportunities
   - This project: 4.0% → 8.9% encapsulation ratio

5. **Pragmatism before dogma**
   - backend.api.ws.rs remains classpath-based (Jakarta EE requirement)
   - Use JPMS where it provides value

### 📊 Project Statistics

| Metric | Value | Benefit |
|--------|-------|---------|
| Modules with JPMS | 50 | Clear structuring |
| Exported packages total | 125 | Minimal API surface |
| **Encapsulation ratio** | **8.9%** | Implementation details protected |
| Hidden public types | 43 | |
| Qualified `opens` | 27 directives | Minimal reflection attack surface |
| Split-package conflicts | 0 | Clean package structure |

---

**For publication documentation, see:**
- [JPMS in Action - jeeeraaah](root/app/jeeeraaah/doc/md/jpms in action - jeeeraaah/jpms in action - jeeeraaah.md)
- [Modular Software in Java](root/app/jeeeraaah/doc/md/modular-software-in-java/modular-software-in-java.md)

**Analysis tools:**
- `count-jpms-encapsulation.py` - Analyzes JPMS encapsulation metrics

---

*Last updated: 2026-03-01*
