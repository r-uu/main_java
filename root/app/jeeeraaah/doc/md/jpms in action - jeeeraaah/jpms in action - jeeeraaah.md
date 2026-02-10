# JPMS in Aktion - jeeeraaah

JPMS (Java Platform Module System) ist eine Technologie zur Modularisierung von Java Anwendungen. Es wurde 2017 mit der Java Version 9 veröffentlicht. Während es für das JDK meist als großer Erfolg gewertet wird, kämpft es um Akzeptanz in der Java User Community.

Modularisierung ist ein entscheidender Faktor für die Entwicklung von gut wartbaren, gut verständlichen und gut erweiterbaren, großen Softwaresystemen (siehe Artikel [modular software in java](../modular-software-in-java/modular-software-in-java.md)).

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