# Lombok + MapStruct Integration (Deutsch)

Dieses Modul verwendet das `lombok-mapstruct-binding`, damit MapStruct die von Lombok erzeugten fluent Getter/Setter erkennt.

Ziel
- MapStruct soll die vorhandenen fluent-Accessors (z. B. `name()` / `name(String)`) erkennen, sodass zusätzliche JavaBean-Style-Methoden (`getName()`, `setName(...)`) nicht mehr notwendig sind.

Konfiguration
- Die zentrale Maven-Konfiguration befindet sich bereits in der Root-POM und stellt `lombok` und `lombok-mapstruct-binding` als Annotation-Processor-Pfade zur Verfügung.
- Damit die Bindings greifen, ist keine weitere Code-Änderung bei MapStruct-Mappern notwendig — MapStruct generiert dann Implementierungen, die Lomboks fluent-Methoden verwenden.

Empfehlung für schrittweises Vorgehen
1. Sicherstellen, dass das Projekt mit dem aktuellen Root-POM gebaut wird (Annotation-Processor-Pfade aktiv).
2. Tests ausführen (z. B. `mvn -pl app/jeeeraaah/backend.common.mapping test`).
3. Falls Tests grün sind, können schrittweise die JavaBean-Style-Accessors entfernt werden (z. B. `getDescription()`/`setDescription(...)`).

Hinweis
- `@AfterMapping`-Methoden bleiben sinnvoll für komplexe Fälle (Relationen, Cycle-Tracking, spezielle Logik). Wenn viele einfache Felder per `@AfterMapping` gesetzt werden müssen, deutet das auf ein Accessor-Erkennungsproblem hin — hier löst das Binding es eleganter.

Weiteres
- Wenn ihr statt des Bindings lieber eine eigene `AccessorNamingStrategy` implementieren möchtet, kann ich ein Beispiel bereitstellen.
