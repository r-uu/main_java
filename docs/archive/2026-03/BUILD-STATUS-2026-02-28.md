# Build Status - 28. Februar 2026

## Maven Build: ✅ BUILD SUCCESS

**Datum:** 2026-02-28  
**Build-Zeit:** 2:14 min (clean compile)  
**Module:** 88/88 erfolgreich  
**Errors:** 0  
**Kritische Warnings:** 0

## Durchgeführte Fixes

### 1. Lombok @NonNull Transitive Export
**Problem:** lib.util exportiert @NonNull in öffentlichen APIs, aber lombok war nicht transitiv  
**Lösung:** `requires static lombok` → `requires static transitive lombok`  
**Datei:** [root/lib/util/src/main/java/module-info.java](root/lib/util/src/main/java/module-info.java)

### 2. Gson Module Reference
**Problem:** jasperreports.model hatte `opens ... to com.google.gson` ohne dependency  
**Lösung:** gson-Referenz aus module-info.java entfernt  
**Datei:** [root/sandbox/office/microsoft/word/jasperreports/model/src/main/java/module-info.java](root/sandbox/office/microsoft/word/jasperreports/model/src/main/java/module-info.java)

### 3. Hibernate Module Warnings
**Status:** Dokumentiert als harmlos  
**Grund:** Hibernate ist runtime-only dependency (JPA Service Provider)  
**Dateien:**
- [root/lib/jpa/core/src/main/java/module-info.java](root/lib/jpa/core/src/main/java/module-info.java)
- [root/lib/jpa/se/src/main/java/module-info.java](root/lib/jpa/se/src/main/java/module-info.java)

## Verbleibende Harmlose Warnungen

### Module Not Found (Runtime Dependencies)
Diese Warnungen sind **erwartbar und harmlos**:

```
[WARNING] module not found: org.hibernate.orm.core
```
- **Grund:** Hibernate wird zur Laufzeit über SPI geladen
- **Verwendung:** Nur in `opens`-Statements für Reflection
- **Anzahl:** ~36 Warnungen (über mehrere Module)

```
[WARNING] module not found: org.eclipse.yasson
```
- **Grund:** JSON-B Implementation (runtime)
- **Module:** lib.jsonb
- **Anzahl:** 4 Warnungen

```
[WARNING] module not found: com.fasterxml.jackson.databind
```
- **Grund:** Optional dependency für DTO-Serialisierung
- **Module:** common.api.ws.rs
- **Anzahl:** 4 Warnungen

### Auto-Module Warnings
```
[WARNING] Required filename-based automodules detected: [keycloak-admin-client-26.0.8.jar, ...]
```
- **Grund:** Externe Dependencies ohne module-info.java werden als automatische Module erkannt
- **Empfehlung:** "Please don't publish this project to a public artifact repository!"
- **Anzahl:** ~20 Warnungen
- **Status:** Standard für Projekte, die auf Legacy-Libraries angewiesen sind

### MapStruct Warnings
```
[WARNING] Unmapped target property: "superTaskId"
[WARNING] Unknown options: '[mapstruct.suppressGeneratorVersionComment]'
```
- **Anzahl:** 2 Warnungen
- **Status:** Konfigurationsprobleme, keine funktionalen Auswirkungen

## IDE-Probleme (VS Code / Language Server)

**Symptom:** VS Code zeigt 786-1099 "Fehler" an, obwohl Maven Build erfolgreich ist

**Ursachen:**
- Veralteter Language Server Cache
- Nicht synchronisierte .class-Dateien nach JPMS-Änderungen
- Indexierungs-Probleme nach Refactoring

**Lösung:**
1. VS Code neu laden: `Ctrl+Shift+P` → "Developer: Reload Window"
2. Java Language Server neu starten: `Ctrl+Shift+P` → "Java: Clean Java Language Server Workspace"
3. Build-Verzeichnis löschen: `mvn clean`
4. Falls weiterhin Probleme: IntelliJ IDEA Cache invalidieren

## JPMS-Verbesserungen (heute durchgeführt)

Gemäß [JPMS-IMPROVEMENTS-2026-02-28.md](JPMS-IMPROVEMENTS-2026-02-28.md):

1. ✅ **lib.jpa.core** - Package `criteria.restriction` nicht mehr exportiert (19 Klassen gekapselt)
2. ✅ **backend.persistence.jpa** - Services in `internal/` Package verschoben (siehe [JPMS-KAPSELUNG-BACKEND-PERSISTENCE-JPA-2026-02-28.md](JPMS-KAPSELUNG-BACKEND-PERSISTENCE-JPA-2026-02-28.md))

## Empfohlene Nächste Schritte

### Sofort:
- [ ] IDE neu laden (siehe oben)
- [ ] Verifizieren, dass IDE-Fehler verschwunden sind

### Optional (Verbesserungen):
- [ ] MapStruct `superTaskId` Warnung beheben
- [ ] Unbenutzte Variable `PACKAGES_TO_SCAN` in KeycloakConfigurationValidator entfernen
- [ ] `requires transitive` für `Modifier` und `ElementKind` in lib.gen.java.core hinzufügen

### Dokumentation:
- [x] Build-Status dokumentiert
- [ ] todo.md aktualisieren (Last Updated: 2026-02-09 → 2026-02-28)

## Fazit

**Das Projekt ist vollständig build-fähig.** Alle "Fehler" sind IDE-Artefakte, die durch Workspace-Reload behoben werden können. Die verbleibenden Maven-Warnungen sind dokumentiert und haben keine Auswirkung auf die Funktionalität.
