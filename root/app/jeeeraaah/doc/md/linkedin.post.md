- original link to github
  - https://github.com/r-uu/main/blob/main/root/app/jeeeraaah/doc/md/jpms%20in%20action%20-%20jeeeraaah/jpms%20in%20action%20-%20jeeeraaah.md
- bitly link to github
	- https://bit.ly/4lxB7KD
- linkedin beitrag
🔧 JPMS in Action — was wirklich passiert, wenn man das Java Module System konsequent einsetzt

Ich arbeite seit einiger Zeit am Projekt jeeeraaah — einem Enterprise Java POC auf Basis von Jakarta EE 10, JavaFX 25 und Java 25 (GraalVM). Das besondere daran: (fast) die gesamte Anwendung ist konsequent mit dem Java Platform Module System (JPMS) modularisiert.

JPMS kämpft in der Java-Community um Akzeptanz. Zu Recht gibt es Vorbehalte: Legacy-Code, Split-Package-Probleme, Reflection-Komplexität. In der Praxis zeigt sich aber: wer JPMS ernsthaft einsetzt, bekommt etwas zurück, das andere Architekturansätze so nicht liefern können — vom Compiler erzwungene Architektur.

📊 Konkrete Zahlen aus dem Projekt (Stand Feb. 2026):

10 JPMS-Module, 24 exportierte Packages (reduziert von 46)
149 public Typen — davon 80 (53,7%) vollständig durch JPMS gekapselt
27 qualifizierte opens-Direktiven — nur für spezifische Frameworks
0 Split-Package-Konflikte über alle Module hinweg

Was mich am meisten überzeugt hat: Architekturverstöße werden nicht durch Code-Reviews oder ArchUnit-Tests verhindert, sondern zur Compile-Zeit. Das Frontend kann JPA-Entities strukturell nicht importieren. Nicht weil eine Konvention es verbietet — sondern weil der Compiler es schlicht ablehnt.

Ist JPMS für jedes Projekt das Richtige? Nein. Aber für ein langlebiges, modular durchdachtes System ist es ein mächtiges Werkzeug — wenn man bereit ist, die Lernkurve zu nehmen.

Mehr dazu im verlinkten Kommentar 👇

#Java #JPMS #JakartaEE #Modularisierung #OpenLiberty #JavaFX #SoftwareArchitektur #CleanArchitecture
- linkedin kommentar
Der vollständige Artikel "JPMS in Action — jeeeraaah" mit allen Details zur Modulstruktur, den konkreten module-info.java-Beispielen, der Dual-Export-Strategie und den Kapselungsmetriken ist hier auf GitHub zu finden:

👉 https://bit.ly/4lxB7KD

Das gesamte Projekt jeeeraaah liegt hier:

👉 https://bit.ly/4b9ESSS