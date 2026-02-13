# JPMS Opens-Anweisungen - Best Practices
**Datum:** 2026-02-13  
**Status:** Analyse der aktuellen Implementierung
---
## ✅ Aktuelle Implementierung ist optimal!
Die `opens`-Anweisung in `common.api.domain/module-info.java` ist bereits **best practice**:
```java
opens de.ruu.app.jeeeraaah.common.api.domain to lombok, com.fasterxml.jackson.databind;
```
---
## Warum ist das optimal?
### 1. Gezielt (✅ RICHTIG)
```java
// ✅ Nur spezifische Module haben Reflection-Zugriff
opens package.name to module1, module2;
// ❌ VERMEIDEN: Alle Module hätten Zugriff
opens package.name;
```
### 2. Minimal (✅ RICHTIG)
Nur die **wirklich benötigten** Frameworks:
- `lombok` - für @AllArgsConstructor, @Getter, etc.
- `com.fasterxml.jackson.databind` - für @JsonProperty
### 3. Notwendig (✅ RICHTIG)
Beide Frameworks **brauchen** Reflection für:
- Lombok: Compile-time Code-Generation (Annotation Processing)
- Jackson: Runtime JSON Serialisierung/Deserialisierung
---
## Könnte man es noch gezielter machen?
### ❌ Class-Level Opens (NICHT MÖGLICH)
```java
// Würde man gerne machen, aber JPMS unterstützt das NICHT:
opens de.ruu.app.jeeeraaah.common.api.domain.InterTaskRelationData to lombok;
```
**Fazit:** JPMS unterstützt nur **Package-Level** `opens`, nicht Class-Level!
### ✅ Multiple Packages (falls vorhanden)
```java
// Falls mehrere Packages verschiedene Frameworks brauchen:
opens de.ruu.app.common.domain to lombok;
opens de.ruu.app.common.dto to com.fasterxml.jackson.databind;
```
**In unserem Fall:** Nur 1 Package → nicht relevant
---
## JPMS Opens-Varianten im Vergleich
| Variante | Syntax | Zugriff | Empfohlen? |
|----------|--------|---------|------------|
| **Vollständig offen** | `opens package;` | Alle Module | ❌ Nur wenn nötig |
| **Gezielt** | `opens package to module1, module2;` | Nur diese Module | ✅ **BEST PRACTICE** |
| **Gar nicht** | (keine opens) | Kein Reflection | ✅ Wenn möglich |
---
## Wann braucht man `opens`?
### Reflection-basierte Frameworks
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
4. **Annotation Processing** (Lombok, MapStruct zur Runtime)
   ```java
   opens de.ruu.app.domain to lombok;
   ```
5. **Testing** (JUnit, Mockito)
   ```java
   opens de.ruu.app.internal to org.junit.platform.commons;
   ```
---
## Empfehlungen für das Projekt
### ✅ Aktuell gut umgesetzt:
1. `common.api.domain` - Opens zu Lombok + Jackson ✅
2. Gezielte `to`-Klauseln verwendet ✅
3. Dokumentation mit Kommentar ✅
### 🔍 Zu prüfen:
Andere Module durchgehen und Pattern anwenden:
```bash
# Alle module-info.java finden
find root -name "module-info.java" -type f
# Opens-Anweisungen prüfen
grep -r "opens" root/*/module-info.java
```
### 📋 Checkliste für jedes Modul:
- [ ] Braucht das Package wirklich `opens`?
- [ ] Kann es `exports` statt `opens` sein?
- [ ] Ist die `to`-Klausel minimal (nur nötige Module)?
- [ ] Ist der Grund dokumentiert (Kommentar)?
---
## Pattern-Vorlage für module-info.java
```java
/**
 * [Modul-Beschreibung]
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
## Zusammenfassung
### ✅ **Aktuelle Lösung BEIBEHALTEN**
Die `opens`-Anweisung ist bereits optimal:
- Gezielt auf notwendige Frameworks
- Minimal (nur 2 Module)
- Gut dokumentiert
### 💡 **Zusätzlicher Aufwand lohnt sich NICHT**
- Class-Level opens gibt es nicht in JPMS
- Package-Level ist bereits granular genug
- Performance-Unterschied wäre vernachlässigbar
### 📚 **Best Practice erfüllt**
Projekt folgt Oracle's JPMS-Empfehlungen:
1. `exports` für öffentliche API
2. `opens ... to` für Reflection (minimal)
3. Dokumentation der Gründe
---
**Fazit:** Aktuelle Implementierung ist **production-ready** und entspricht **Best Practices**! 🎉
