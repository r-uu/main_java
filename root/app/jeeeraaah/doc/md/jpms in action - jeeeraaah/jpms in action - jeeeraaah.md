# JPMS in Aktion - jeeeraaah

JPMS (Java Platform Module System) ist eine Technologie zur Modularisierung von Java Anwendungen. Es wurde 2017 mit der Java Version 9 veröffentlicht.

Für das JDK selbst wird JPMS meist als großer Erfolg gewertet, da es seit dem nicht mehr als ein einziger riesiger Monolith (rt.jar) ausgeliefert werden muss, der schon aufgrund seiner Größe nicht mehr zum sich immer weiter verbreitenden Architekturmodell Microservices passte.

In der Java User Community hingegen kämpft JPMS aus verschiedenen Gründen wie (noch) nicht modularer legacy code, Probleme mit reflection, ... weiter um Akzeptanz.

Modularisierung ist aber ein entscheidender Faktor für die Entwicklung von gut wartbaren, gut verständlichen und gut erweiterbaren, großen Softwaresystemen (siehe Artikel [modular software in java](../modular-software-in-java/modular-software-in-java.md)).

Das Projekt jeeeraaah wurde als "proof of concept" (POC) für die Möglichkeit der Verwendung von JPMS in Enterprise Java Systemen gestartet. Ziel ist, anhand einer überschaubaren, aber nicht trivialen Anwendung zu überprüfen, ob und wie Modularisierung großer Java Applikationen mit JPMS eine valide Alternative zu anderen Architekturansätzen wie z. B. Microservices ist.

Gleichzeitig soll kritisch geprüft werden, ob die Vorteile von Modularisierung mit JPMS die Nachteile überwiegen, z. B. die Komplexität der Modularisierung selbst, die Komplexität der Build- und Deployment-Prozesse, ... .

Fachlich geht es im Projekt jeeeraaah im Kern um die Verwaltung von Aufgaben (Tasks) und die Planung von Arbeitsabläufen. Dazu sollen zusammengehörige Tasks in Gruppen (TaskGroups) organisiert werden. **Abb. 10** zeigt das zentrale Objektmodell:

<p align="center">
  <img src="jeeeraaah-uml-taskgroup-task.drawio.svg" alt="TaskGroup - Task" width="350"/>
  <br/>
  <em>Abb. 1: UML - TaskGroup-Task</em>
</p>

Die Idee ist, Aufgaben in Teilaufgaben zu gliedern (Tasks und SubTasks) und für alle Aufgaben Abläufe (Predecessor- und Successor-Tasks) planen zu können.

<p align="center">
  <img src="jeeeraaah-uml-task-objects.png" alt="Task-Objects" width="350"/>
  <br/>
  <em>Abb. 2: Task-Objekte</em>
</p>

In der Anwendung sieht das dann im dashboard etwa so aus:

<p align="center">
  <img src="jeeeraaah-dashboard.png" alt="Task-Objects" width="350"/>
  <br/>
  <em>Abb. 3: jeeeraaah dashboard</em>
</p>

Eine Gantt-Diagramm-Darstellung zeigt eine andere Sicht auf Aufgaben und die geplanten Abläufe:

<p align="center">
  <img src="jeeeraaah-gantt.png" alt="Task-Objects" width="350"/>
  <br/>
  <em>Abb. 4: jeeeraaah Gantt Diagramm</em>
</p>

## Der Technologiestack

Ein Ziel des POCs ist, die Versionen der eingesetzten Technologien dauerhaft auf einem möglichst modernen Stand zu halten. Updates aller Technologien gehören daher zur Tagesordnung.

Jeeeraaah ist eine client-server Java Anwendung, deren Bestandteile (bis auf eine Ausnahme, dazu später mehr) mit Java 25 entwickelt wurden. Dabei kommen aktuell folgende Technologien zum Einsatz:

Das backend ist eine Jakarta EE 10 / Microprofile 6.1 Anwendung. Als Application Server wird Open Liberty verwendet. Im frontend kommt JavaFX 25 zum Einsatz.

Frontend und Backend sind weitestgehend mit JPMS modularisiert. Die Kommunikation zwischen ihnen erfolgt über REST APIs, die mit Jakarta-RS implementiert wurden. Die (De-) Serialisierung der Daten erfolgt mit Jackson, was einen komfortablen und gleichzeitig effizienten Umgang auch mit zirkulären Datenstrukturen (siehe Task / TaskGroup Objektmodell) erlaubt. Die build Prozesse für beide Anwendungen werden mit Apache Maven realisiert.

Für das Identity and Access Management (IAM) wird Keycloak verwendet. Das frontend kommuniziert direkt mit Keycloak, um die Authentifizierung der Benutzer durchführen zu lassen. Das Open Liberty backend ist so konfiguriert, dass es die von keycloak ausgestellten token akzeptiert und die Autorisierung für alle eingehenden Requests durchführen kann.

Die persistente Datenhaltung im backend wird mit einer postgres Datenbank realisiert. Sie wird genau wie keycloak in einem von docker-compose orchestrierten Container betrieben. In diesem POC liegen die jeeeraaah- zusammen mit den keycloak-Daten in ein und derselben Datenbank, sie sind aber jeweils explizit einem eigenen Schema zugeordnet. Die jeeeraaah Zugriffe auf die Datenbank sind durchgängig mit JPA (hibernate) umgesetzt.

## Die Modulstruktur

```
jeeeraaah/
├── backend/                    # Server-Komponenten
│   ├── api/ws.rs/              # REST API Server (Open Liberty)
│   ├── persistence/            # JPA Entities & Repositories
│   └── common/                 # gemeinsame Backend-Klassen, Mappings DTO <-> JPA
├── frontend/                   # Client-Komponenten
│   ├── api.client/ws.rs/       # REST API Client
│   ├── ui/fx/                  # JavaFX UI
│   └── common/                 # gemeinsame Frontend-Klassen, Mappings DTO <-> Bean <-> JavaFXBean
└── common/api/                 # API Domain Model Types (geteilt)
```

Bis auf das maven Modul r-uu.app.jeeeraaah.backend.api sind alle Module mit JPMS modularisiert. Das Modul r-uu.app.jeeeraaah.backend.api ist nicht modularisiert, da es die REST API Schnittstellen enthält, die mit Jakarta-RS implementiert wurden. 

xxx folgender Abschnitt muss noch überarbeitet werden, da er so nicht mehr ganz korrekt ist, siehe weiter unten xxx

Jakarta-RS verwendet reflection, um die REST API Schnittstellen zu implementieren, was mit JPMS nicht kompatibel ist. Daher wurde dieses Modul bewusst nicht modularisiert, um die Verwendung von Jakarta-RS zu ermöglichen. Alle anderen Module sind modularisiert, um die Vorteile von Modularisierung mit JPMS nutzen zu können, z. B. die Möglichkeit, die Abhängigkeiten zwischen den Modulen explizit zu machen.

## Architektur

Das backend ist in zwei Hauptmodule aufgeteilt: api und persistence. Das api Modul enthält die REST API Schnittstellen, die mit Jakarta-RS implementiert wurden. Im persistence Modul befindet sich die Datenzugriffsschicht, die mit JPA (hibernate) implementiert wurde.

Das frontend ist ebenfalls in zwei Module aufgeteilt: ui und api.client. Das ui Modul enthält die JavaFX Komponenten, die für die Darstellung der Benutzeroberfläche verantwortlich sind. Das api.client Modul enthält die Logik für die Kommunikation mit dem backend über REST APIs.

Das Bindeglied zwischen frontend und backend ist das common Modul, das Objekte und Objekt-Mappings enthält, die von beiden Seiten verwendet werden.

### Modul common

Das common.api.domain Modul enthält zentrale Schnittstellen und Basisklassen des Domänenmodells. Dieses Modul bildet das Fundament für das Jeeeraaah Task-Management-System und definiert:

- Zentrale Domain-Entitäten und deren Verträge
- **Lazy-Loading-Varianten** zur Performanceoptimierung (`domain.lazy` Package)
- **Flache Repräsentationen** für vereinfachten Datentransfer (`domain.flat` Package)
- Konfigurationen für Beziehungen zwischen Tasks

Das Modul ist so konzipiert, dass es transitiv sowohl vom Frontend als auch vom Backend benötigt wird, um ein konsistentes Domänenmodell über alle Anwendungsschichten hinweg zu gewährleisten.

Der Aufbau des Moduls spiegelt die Struktur des gesamten Projekts wider:

- das Submodul **common.api.domain** enthält vor allem die zentralen Interfaces des Domänenmodells, die von beiden Seiten (frontend und backend) verwendet werden. Um die Verwendung der Interfaces auf beiden Seiten möglichst konsistent halten zu können, sind sie generisch, was eine sehr starke Typisierung in den implementierenden Klassen ermöglicht.

- das Submodul **common.api.domain.flat** enthält flache (flat) Repräsentationen von Domain-Objekten, die nur Kern-Felder ohne teure Beziehungen enthalten. Diese sind für Performance-optimierte Szenarien gedacht, z.B. beim Aufbau von Hierarchien im Gantt-Diagramm.

- das Submodul **common.api.domain.lazy** enthält Lazy-Loading-Varianten, die IDs anstelle von vollständigen Objekten verwenden. Dies ermöglicht verzögertes Laden von Beziehungen und reduziert die Netzwerk- und Speicherlast.

- das Submodul **common.api.ws.rs** enthält die DTO Klassen, mit deren Hilfe frontend und backend kommunizieren. Die DTO Klassen implementieren die generischen Interfaces aus common.api.domain.

- das Submodul **common.api.bean** enthält (Java-)Bean-Implementierungen der Interfaces aus common.api.domain. Genaugenommen sind die Implementierungen keine Java-Beans, da sie fluent accessors anstelle der Java-Beans üblichen get-/set-accessors verwenden. Die Bean-Implementierungen aus diesem Modul sind für die Realisierung von Geschäftslogik im Projekt vorgesehen.

Ergänzend zu den Submodulen enthält das common Modul noch das Submodul **common.api.mapping**, in dem die Mappings zwischen Java-Beans und DTOs definiert werden. Die Mappings werden aktuell mit MapStruct implementiert.

---

<details><summary>Hinweis 1: möglicher Verzicht auf DTOs</summary>
Es ist durchaus denkbar, dass die Bean-Implementierungen aus common.api.bean auch für die Realisierung von DTOs verwendet werden könnten. In diesem Fall könnte das common.api.ws.rs Submodul entfallen. Aktuell ist es aber so, dass die DTOs und die Bean-Implementierungen getrennt sind, um eine klare Trennung zwischen den beiden Schichten zu gewährleisten.
</details>

---

<details><summary>Hinweis 2: möglicher Verzicht auf MapStruct</summary>
Die MapStruct Mappings implementieren die Umwandlung aktuell quasi "manuell", d. h. die typischen MapStruct Features wie automatisches Mapping von gleichnamigen Feldern oder die Verwendung von Mapping-Methoden für die Umwandlung von komplexeren Objekten werden nicht bzw. nur sehr eingeschränkt genutzt. Das hat sich im Laufe der Zeit in diese Richtung entwickelt.

Im Nachhinein wäre ein Verzicht auf MapStruct und die Implementierung der Mappings von Hand wahrscheinlich die bessere Wahl gewesen, da die Verwendung von MapStruct hier mehr Komplexität z. B. im Build-Prozess mit sich bringt und die typischen Vorteile von MapStruct durch automatisierte Code-Generierung für die Umwandlung nicht zum Tragen kommt. Die aktuelle Implementierung funktioniert allerdings, ist gut getestet und es ist durchaus denkbar, dass durch zukünftige Erweiterung des Objektmodells die typischen Vorteile von Mapstruct zum Tragen kommen. 
</details>

---

### Modul backend

Das backend besteht aus zwei Hauptmodulen: api und persistence. Das api Modul enthält die REST API Schnittstellen, die mit Jakarta-RS implementiert wurden. Im persistence Modul befindet sich die mit JPA (hibernate) implementiert Datenzugriffsschicht. Auch hier gibt es ein common Modul, das die Mappings zwischen JPA-Entity-Typen und Jakarta-RS-DTOs definiert.

### Modul frontend

Das frontend ist ebenfalls in zwei Module aufgeteilt: ui und api.client. Das ui Modul enthält die JavaFX Komponenten, die für die Darstellung der Benutzeroberfläche verantwortlich sind. Das api.client Modul enthält die Logik für die Kommunikation mit dem backend über REST APIs. Auch hier gibt es ein common Modul, das die Mappings zwischen JavaFX-Objekten und Jakarta-RS-DTOs definiert.

## Identity and Access Management mit Keycloak

Der jeeeraaah keycloak server läuft in einem Docker Container, der über docker-compose orchestriert wird. Damit das Identity and Access Management (IAM) mit Keycloak funktioniert, müssen folgende Schritte durchgeführt werden:

### Konfiguration von Keycloak

Die keycloak service Konfiguration erfolgt in docker-compose.yml. Dort wird der keycloak server mit den notwendigen Umgebungsvariablen konfiguriert, um die initiale Einrichtung von Realm, Client und User zu ermöglichen.

Das openliberty backend ist so konfiguriert, dass es bei eingehenden requests mit keycloak über OpenID Connect (OIDC) kommuniziert, um die Authentifizierung und Autorisierung der Benutzer zu gewährleisten. Das frontend kommuniziert direkt mit Keycloak, um die Authentifizierung und Autorisierung der Benutzer zu gewährleisten.

The Server Side
