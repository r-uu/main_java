# Kritische Bewertung: "Entwicklung modularer Software mit Java"

## Datum der Bewertung
3. Februar 2026

---

## Executive Summary

Der Artikel bietet eine **solide und gut strukturierte Einführung** in die Modularisierung von Java-Anwendungen. Er schlägt erfolgreich die Brücke zwischen klassischen Monolithen, Microservices und dem modernen Modulith-Ansatz. Die **didaktische Aufbereitung** mit Visualisierungen ist hervorragend. Allerdings fehlen **praktische Code-Beispiele**, eine kritische Auseinandersetzung mit **Implementierungsherausforderungen** und eine Diskussion von **Alternativen zu JPMS**.

**Gesamtbewertung: 7/10** - Guter Überblicksartikel, dem es an praktischer Tiefe mangelt.

---

## Stärken

### 1. **Klare Problemanalyse** ✅
- Die Darstellung von Komplexität in Softwaresystemen ist präzise und nachvollziehbar
- Der historische Kontext (Java ≤ 8 vs. Java ≥ 9) wird gut herausgearbeitet
- Die Erosion von Systemarchitekturen wird anschaulich erklärt

### 2. **Exzellente Visualisierung** ✅
- Durchgehend hochwertige Diagramme (SVG/PNG)
- Konsistente visuelle Sprache (beige Kugeln für Module/Komponenten)
- Die 3D-Darstellungen sind eingängig und modern

### 3. **Strukturierter Aufbau** ✅
- Logischer Fluss von Problem → Lösungsansätze → Fazit
- Gute Verwendung von Expandable Sections (`<details>`)
- Klare Abgrenzung zwischen Monolith, Microservices und Modulith

### 4. **Praxisrelevanz** ✅
- Der Modulith-Ansatz ist hochaktuell und wird in der Industrie diskutiert
- Die Kombination Modulith + Microservices entspricht realen Architekturen
- Die Tabelle "Wann was verwenden?" ist praktisch wertvoll

---

## Schwächen

### 1. **Mangel an praktischen Code-Beispielen** ❌
**Problem:**
- Das einzige Code-Beispiel (`module-info.java`) ist sehr rudimentär
- Es fehlen Beispiele für typische Modularisierungs-Szenarien
- Keine Demonstration, wie man ein Legacy-System zu einem Modulith migriert

**Konkrete Verbesserung:**
```java
// FEHLT: Wie sieht ein realistisches Modul mit Schnittstelle aus?
module de.example.order.api {
    exports de.example.order.api;          // Public API
    exports de.example.order.api.dto;      // Data Transfer Objects
    
    // HIDDEN: de.example.order.internal.* ist nicht exportiert!
}

// FEHLT: Wie kommunizieren Module untereinander?
module de.example.shipping {
    requires de.example.order.api;  // Zugriff nur auf exportierte Packages!
    // NICHT möglich: import de.example.order.internal.*
}
```

### 2. **Fehlende Diskussion von Implementierungsherausforderungen** ❌

**Was fehlt:**
- **Split Packages:** Was passiert, wenn zwei Module dasselbe Package exportieren?
- **Reflection:** Wie geht JPMS mit Reflection um (`opens` vs. `exports`)?
- **Legacy Dependencies:** Viele Libraries sind noch nicht modularisiert (automatic modules)
- **Build-Tool-Integration:** Maven vs. Gradle, wie konfiguriert man die?
- **Testing:** Wie testet man Module? Braucht man `--add-opens` für Tests?

**Beispiel für reale Herausforderung:**
```java
// Im Artikel steht:
module de.ruu.app.jeeeraaah.frontend.api.client.ws.rs {
    opens de.ruu.app.jeeeraaah.frontend.api.client.ws.rs.dto;
    // WARUM "opens"? WARUM nicht nur "exports"?
    // → Für JSON-Serialisierung (Jackson) via Reflection!
}
```

### 3. **Keine Diskussion von Alternativen** ❌

**Was fehlt:**
- **OSGi:** Wird als etablierter Standard für Modularisierung nicht erwähnt
- **Jigsaw vs. OSGi:** Keine Gegenüberstellung
- **ArchUnit:** Wird kurz erwähnt, aber nicht als valide Alternative diskutiert
- **Package-by-Feature:** Modularisierung ohne JPMS

### 4. **Oberflächliche Behandlung von Microservices** ⚠️

**Problem:**
- Die Kritik an Microservices ist stark vereinfacht
- Nur Infrastruktur-Komplexität wird erwähnt
- Weitere Nachteile fehlen:
  - Distributed Transactions (Saga Pattern)
  - Network Latency & Timeouts
  - Service Discovery & Load Balancing
  - Eventual Consistency
  - Debugging & Monitoring Overhead

**Fehlende Nuancierung:**
> "Es zeigte sich aber, dass mit wachsender Zahl von Microservices die Komplexität an anderen Stellen, z. B. bei der benötigten Infrastruktur und deren Management, enorm steigt."

**Besser wäre:**
> "Microservices verschieben Komplexität von der Codebasis zur Infrastruktur: 
> - Netzwerk-Latenz und Fehlerbehandlung (Circuit Breaker, Retry)
> - Konsistenz über Service-Grenzen (Saga Pattern statt ACID)
> - Deployment-Orchestrierung (Kubernetes, Service Mesh)
> - Monitoring & Distributed Tracing (OpenTelemetry)"

### 5. **Fehlende Metriken und Bewertungskriterien** ❌

**Was fehlt:**
- Wie misst man "gute Modularisierung"? (Coupling, Cohesion)
- Welche Tools gibt es zur Analyse? (JDepend, Structure101)
- Wie validiert man Module? (ArchUnit-Beispiele fehlen)

### 6. **Inkonsistente Terminologie** ⚠️

**Beispiele:**
- "Teilsysteme" vs. "Module" vs. "Bausteine" - wird synonym verwendet, aber nicht klar definiert
- "Deployment Unit" - wird nur im Kontext Monolith erwähnt, aber Module sind auch deployment units

### 7. **Fehlende Quellenangaben** ❌

**Problem:**
- Keine Literaturverweise
- Keine Links zu offiziellen JPMS-Dokumentationen
- Begriff "Modulith" wird erwähnt, aber nicht referenziert (Spring Modulith? Andere?)

---

## Inhaltliche Kritikpunkte

### A) **JPMS-Darstellung ist zu optimistisch**

**Zitat aus dem Artikel:**
> "Mit JPMS können Entwickler Module definieren, die ausschließlich über eine selbst festgelegte Schnittstelle genutzt werden können und gleichzeitig den Zugriff auf interne Teile des Moduls unterbinden."

**Kritik:**
Das ist **theoretisch richtig**, aber in der Praxis gibt es **Escape Hatches**:
```bash
# Reflection kann Modul-Grenzen brechen!
java --add-opens java.base/java.lang=ALL-UNNAMED ...

# Oder per Code:
module.addExports("internal.package", targetModule);
```

**Verbesserungsvorschlag:**
> "JPMS bietet **starke, aber nicht absolute** Kapselung. Reflection-basierte Frameworks (Hibernate, Spring) benötigen oft `opens`-Direktiven, die die Kapselung gezielt aufweichen. Zur Laufzeit können JVM-Flags (`--add-opens`) Modul-Grenzen durchbrechen."

### B) **Package-Sichtbarkeit-Problem ist übertrieben**

**Artikel behauptet (Abb. 6):**
> "Gibt es in einem `package` eine Klasse, die in einem anderen `package` verwendet werden soll, muss die Klasse `public` gemacht werden. Damit ist sie aber öffentlich für ALLE Systemteile."

**Gegenargument:**
- Das ist **nur ein Problem, wenn man keine klare Package-Struktur** hat!
- Mit konventionellen Ansätzen (`.api` vs. `.internal` Packages) lässt sich das Problem mildern:
  ```
  de.example.order.api          → Öffentliche API
  de.example.order.internal     → Versteckte Implementierung
  ```
- **ArchUnit kann das erzwingen** ohne JPMS!

### C) **Modulith-Definition ist vage**

**Artikel sagt:**
> "Dieser Beitrag zeigt, dass ein modular aufgebauter Monolith genau dies realisiert. Dieses Konzept taucht seit einiger Zeit in der Literatur unter dem Kunstbegriff 'Modulith' auf."

**Problem:**
- **Welche Literatur?** (Keine Quellen!)
- **Spring Modulith** (https://spring.io/projects/spring-modulith) wird nicht erwähnt
- **Unterschied Modulith vs. JPMS-Monolith** bleibt unklar

### D) **Unrealistische Vergleichstabelle**

**Tabelle im Artikel:**
| Aspekt | Monolith | Microservices | Modulith |
|--------|----------|---------------|----------|
| Modularisierung | ❌ Schwach | ✅ Stark | ✅ Stark |

**Kritik:**
- Ein **Monolith mit JPMS ist ein Modulith!**
- Die Tabelle suggeriert, dass Monolithen **grundsätzlich** schlecht modularisiert sind
- Tatsächlich: Ein gut strukturierter Monolith (ohne JPMS) kann besser sein als ein schlecht strukturierter Modulith

**Verbesserung:**
| Aspekt | Legacy Monolith | JPMS Modulith | Microservices |
|--------|-----------------|---------------|---------------|
| Modularisierung | ❌ Konvention | ✅ Erzwungen | ✅ Erzwungen (Prozess) |

---

## Fehlende Themen

### 1. **Migration von Legacy zu Modulith**
- Wie migriert man ein bestehendes System?
- Bottom-up vs. Top-down Ansatz?
- Umgang mit Circular Dependencies?

### 2. **Performance-Aspekte**
- Hat JPMS einen Runtime-Overhead?
- Sind kleinere JAR-Dateien ein Vorteil (jlink)?

### 3. **Tooling & IDE-Support**
- IntelliJ IDEA vs. Eclipse: Wie gut ist die JPMS-Unterstützung?
- Debugging über Modul-Grenzen?

### 4. **Team-Organisation**
- Wie organisiert man Teams um Module?
- Code Ownership für Module?

---

## Empfohlene Verbesserungen

### **Sofort umsetzbar:**

1. **Code-Beispiele hinzufügen:**
   ```java
   // Beispiel: Order-Modul mit klarer API
   module de.example.order {
       exports de.example.order.api;
       
       requires de.example.common;
       requires java.sql;
       requires jakarta.persistence;
   }
   ```

2. **Practical Pitfalls Section:**
   ```markdown
   ## Häufige Stolpersteine bei JPMS
   
   ### 1. Split Packages
   Problem: Zwei Module exportieren `com.example.util`
   Lösung: Umbenennen oder Refactoring
   
   ### 2. Automatic Modules
   Problem: Legacy JARs ohne module-info.java
   Lösung: `requires lib.name;` (Name aus MANIFEST.MF)
   ```

3. **Literaturverzeichnis:**
   ```markdown
   ## Quellen
   - [JSR 376: Java Platform Module System](https://openjdk.org/jeps/261)
   - [Spring Modulith](https://spring.io/projects/spring-modulith)
   - Newman, Sam: "Building Microservices" (2021)
   ```

### **Mittelfristig:**

4. **Hands-on Tutorial:** Migration eines Mini-Projekts zu JPMS
5. **Video/Screencast:** Debugging von Modul-Problemen
6. **Vergleich mit OSGi:** Warum JPMS? Warum nicht OSGi?

### **Langfristig:**

7. **Case Study:** Reales Projekt (Open Source), das JPMS nutzt
8. **Performance-Benchmarks:** Modulith vs. Microservices (Latenz, Throughput)

---

## Stilistische Anmerkungen

### **Positiv:**
- ✅ Konsistente Verwendung von Bildern und Metaphern
- ✅ Leserfreundliche Formatierung (Markdown)
- ✅ Professioneller Ton

### **Negativ:**
- ❌ Manche Sätze sind zu lang (z. B. Microservices-Absatz)
- ❌ Wiederholungen: "big ball of mud" wird sehr oft wiederholt
- ❌ Einige Anglizismen sind unnötig (z. B. "Deployment" statt "Auslieferung")

---

## Zielgruppen-Analyse

### **Für wen ist der Artikel geeignet?**

**✅ Gut für:**
- **Softwarearchitekten:** Guter Überblick über Modularisierungsoptionen
- **Technische Entscheider:** Hilft bei Architekturentscheidungen
- **Studenten:** Einführung in moderne Java-Architektur

**❌ Weniger gut für:**
- **Entwickler:** Zu wenig praktische Anleitung
- **DevOps-Ingenieure:** Deployment-Aspekte fehlen
- **Experten:** Zu oberflächlich, nichts Neues

---

## Fazit und Handlungsempfehlungen

### **Gesamtbewertung: 7/10**

**Aufschlüsselung:**
- Didaktik & Struktur: 9/10 ✅
- Visualisierung: 10/10 ✅
- Inhaltliche Tiefe: 5/10 ❌
- Praxisrelevanz: 6/10 ⚠️
- Vollständigkeit: 6/10 ⚠️

### **Empfehlung:**

**Für Leser:**
- Als **Einstieg** sehr gut geeignet
- Danach unbedingt **offizielle JPMS-Dokumentation** lesen
- **Hands-on Erfahrung** mit einem kleinen Projekt sammeln

**Für Autoren:**
- Artikel um **praktischen Teil** erweitern (Tutorial)
- **Pitfalls & Best Practices** ergänzen
- **Quellenverzeichnis** hinzufügen
- **Peer Review** durch JPMS-Experten einholen

---

## Abschließende Bemerkung

Der Artikel ist ein **guter Ausgangspunkt**, aber kein vollständiger Guide. Er weckt Interesse am Thema Modularisierung, lässt den Leser aber mit vielen offenen Fragen zurück. 

**Metapher:** Der Artikel ist wie eine **Landkarte** – er zeigt die Landschaft, aber nicht, wie man durch schwieriges Gelände navigiert. Für die tatsächliche Reise braucht man noch einen **Kompass und Wanderführer**.

---

**Erstellt am:** 3. Februar 2026  
**Reviewt von:** GitHub Copilot  
**Nächste Review:** Bei signifikanten Änderungen am Artikel
