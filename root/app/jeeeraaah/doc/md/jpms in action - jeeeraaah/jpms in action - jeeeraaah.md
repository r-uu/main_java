# JPMS in Aktion - jeeeraaah

JPMS (Java Platform Module System) ist eine Technologie zur Modularisierung von Java Anwendungen. Es wurde 2017 mit der Java Version 9 veröffentlicht. Für das JDK selbst wird JPMS meist als großer Erfolg gewertet, da es seit dem nicht mehr als ein einziger riesiger Monolith (rt.jar) ausgeliefert werden muss, der schon aufgrund seiner Größe nicht mehr zum sich immer weiter verbreitenden Architekturmodell Microservices passte. In der Java User Community hingegen kämpft JPMS aus verschiedenen Gründen ((noch) nicht modularer legacy code, Probleme mit reflection, ...) weiter um Akzeptanz.

Modularisierung ist aber ein entscheidender Faktor für die Entwicklung von gut wartbaren, gut verständlichen und gut erweiterbaren, großen Softwaresystemen (siehe Artikel [modular software in java](../modular-software-in-java/modular-software-in-java.md)).

Das Projekt jeeeraaah wurde als "proof of concept" (POC) für die Verwendung von JPMS in Enterprise Java Systemen gestartet. Ziel ist, anhand einer überschaubaren, aber nicht trivialen Anwendung zu überprüfen, ob und wie Modularisierung großer Java Applikationen mit JPMS eine valide Alternative zu anderen Architekturansätzen wie z. B. Microservices ist.

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

Beide Anwendungen sind weitestgehend mit JPMS modularisiert. Die Kommunikation zwischen frontend und backend erfolgt über REST APIs, die mit JAX-RS implementiert wurden. Die build Prozesse für beide Anwendungen werden mit Maven realisiert.

Für das Identity and Access Management (IAM) wird Keycloak verwendet, das über OpenID Connect (OIDC) mit dem backend kommuniziert. Das frontend kommuniziert direkt mit Keycloak, um die Authentifizierung und Autorisierung der Benutzer zu gewährleisten.

Im Backend wird die Persistenz mit JPA (hibernate) und postgres realisiert. Postgres und Keycloak laufen in Docker Containern, die über docker-compose orchestriert werden. Der Postgres Container enthält im POC die Datenbank für sowohl für die Anwendung als auch für das IAM mit Keycloak.

## Architektur

Das backend ist in zwei Hauptmodule aufgeteilt: api und persistence. Das api Modul enthält die REST API Schnittstellen, die mit Jakarta-RS implementiert wurden. Im persistence Modul befindet sich die Datenzugriffsschicht, die mit JPA (hibernate) implementiert wurde.

Das frontend ist ebenfalls in zwei Module aufgeteilt: ui und api.client. Das ui Modul enthält die JavaFX Komponenten, die für die Darstellung der Benutzeroberfläche verantwortlich sind. Das api.client Modul enthält die Logik für die Kommunikation mit dem backend über REST APIs.

Das Bindeglied zwischen frontend und backend ist das common Modul, das Objekte und Objekt-Mappings enthält, die von beiden Seiten verwendet werden.

### Modul common

Das common.api.domain Modul enthält zentrale Schnittstellen und Basisklassen des Domänenmodells. Dieses Modul bildet das Fundament für das Jeeeraaah Task-Management-System und definiert:

- Zentrale Domain-Entitäten und deren Verträge,
- Lazy-Loading-Varianten zur Performanceoptimierung
- Flache Repräsentationen für vereinfachten Datentransfer
- Konfigurationen für Beziehungen zwischen Tasks

Das Modul ist so konzipiert, dass es transitiv sowohl vom Frontend als auch vom Backend benötigt wird, um ein konsistentes Domänenmodell über alle Anwendungsschichten hinweg zu gewährleisten.

Der Aufbau des Moduls spiegelt die Struktur des gesamten Projekts wider:

- das Submodul common.api.domain enthält vor allem die zentralen Interfaces des Domänenmodells, die von beiden Seiten (frontend und backend) verwendet werden. Um die Verwendung der Interfaces auf beiden Seiten möglichst konsistent halten zu können, sind sie generisch, was eine sehr starke Typisierung in den implementierenden Klassen ermöglicht.
- das Submodul common.api.ws.rs enthält die DTO Klassen, mit deren Hilfe frontend und backend kommunizieren. Die DTO Klassen implementieren die generischen Interfaces aus common.api.dommain.
- das Submodul common.api.bean enthält (Java-)Bean-Implementierungen der Interfaces aus common.api.domain. Genaugenommen sind die Implementierungen keine Java-Beans, da sie fluent accessors anstelle der Java-Beans üblichen get-/set-accessors verwenden. Die Bean-Implementierungen aus diesem Modul sind für die Realisierung von Geschäftslogik im Projekt vorgesehen.

### Modul backend


