# Jaspersoft Studio Setup für IntelliJ Integration

## Übersicht

Diese Anleitung zeigt, wie Sie Jaspersoft Studio (Eclipse-basiert) optimal mit IntelliJ IDEA verwenden, um JasperReports-Templates visuell zu designen und in Ihrem IntelliJ-Projekt zu nutzen.

## Vorteile dieser Lösung

✅ **Visuelles Design**: WYSIWYG-Editor für Report-Templates  
✅ **Automatische Zwischensummen**: Konfigurierbar über GUI  
✅ **Seitenüberträge**: Variables & Expressions visuell erstellen  
✅ **IntelliJ bleibt primäre IDE**: Jaspersoft nur für Template-Design  
✅ **Maven Integration**: Kompilierung läuft über Maven in IntelliJ  

---

## 1. Installation von Jaspersoft Studio

### Download

- **URL**: https://community.jaspersoft.com/community-download
- **Version**: Jaspersoft Studio 7.0.0 oder neuer (Community Edition - kostenlos)
- **Platform**: Windows 64-bit

### Installation

```bash
# 1. Download: Jaspersoft Studio-<version>-windows-x86_64.exe
# 2. Installieren nach: C:\Program Files\Jaspersoft\

# Alternative: Portable Version (ohne Installation)
# Download: TIB_js-studiocomm_<version>_windows_x86_64.zip
# Entpacken nach: C:\Tools\JaspersoftStudio\
```

### Erster Start

1. Jaspersoft Studio starten
2. **Workspace** wählen: `C:\Users\<YourUser>\JaspersoftWorkspace` (NICHT in Ihrem Git-Repository!)
3. Welcome-Screen schließen

---

## 2. Projekt in Jaspersoft Studio einrichten

### Option A: Externes Projekt referenzieren (Empfohlen)

Diese Methode vermeidet Konflikte mit IntelliJ und Git.

1. **File → New → Project**
2. **General → Project** auswählen
3. **Project Name**: `jasper-invoice-templates`
4. **Finish**

5. **Projektstruktur erstellen**:
   - Rechtsklick auf Projekt → **New → Folder**
   - Name: `templates`

6. **Existierende .jrxml Dateien verlinken**:
   - Rechtsklick auf `templates` Folder → **Import**
   - **General → File System**
   - **From directory**: `\\wsl.localhost\Ubuntu\home\r-uu\develop\github\main\root\sandbox\office\microsoft\word\jasper\src\main\resources\templates`
   - **☑ Create links in workspace** (WICHTIG!)
   - **Finish**

### Option B: Direktes Arbeiten im WSL-Projekt (Fortgeschritten)

1. **File → Switch Workspace → Other**
2. Pfad: `\\wsl.localhost\Ubuntu\home\r-uu\develop\github\main\root\sandbox\office\microsoft\word\jasper`
3. **⚠ Achtung**: Eclipse-Metadaten (`.project`, `.settings/`) in `.gitignore` aufnehmen!

---

## 3. JRXML Template öffnen und bearbeiten

### Template öffnen

1. In Jaspersoft Studio: **File → Open File**
2. Navigieren zu: `\\wsl.localhost\Ubuntu\home\r-uu\develop\github\main\root\sandbox\office\microsoft\word\jasper\src\main\resources\templates\invoice_template.jrxml`
3. Template wird im **Design-Tab** geöffnet

### Design-Ansicht

Sie sehen nun:
- **Repository Explorer** (links): Felder, Parameter, Variablen
- **Design Canvas** (Mitte): Visueller Editor
- **Palette** (rechts): Elemente (Text Field, Static Text, Line, etc.)
- **Properties** (unten): Element-Eigenschaften

---

## 4. Wichtige Konzepte für Rechnungen

### 4.1 Bands (Bereiche)

| Band | Verwendung | Häufigkeit |
|------|------------|------------|
| **Title** | Rechnungskopf mit Logo, Adresse | 1x pro Report |
| **Page Header** | Überschrift auf jeder Seite | 1x pro Seite |
| **Column Header** | Spaltenüberschriften (Beschreibung, Menge, Preis) | 1x pro Seite |
| **Detail** | Rechnungspositionen (Loop) | Pro Item |
| **Page Footer** | Zwischensumme, Seitenzahl | 1x pro Seite |
| **Summary** | Gesamtsumme, Zahlungsbedingungen | 1x am Ende |

### 4.2 Variablen für Zwischensummen

Im bestehenden Template sind bereits konfiguriert:

**`PAGE_TOTAL`**: Summe der aktuellen Seite
```xml
<variable name="PAGE_TOTAL" class="java.math.BigDecimal" resetType="Page" calculation="Sum">
    <variableExpression><![CDATA[$F{total}]]></variableExpression>
</variable>
```

**`RUNNING_TOTAL`**: Laufende Gesamtsumme
```xml
<variable name="RUNNING_TOTAL" class="java.math.BigDecimal" calculation="Sum">
    <variableExpression><![CDATA[$F{total}]]></variableExpression>
</variable>
```

**`PREVIOUS_PAGE_TOTAL`**: Übertrag von vorheriger Seite
```xml
<variable name="PREVIOUS_PAGE_TOTAL" class="java.math.BigDecimal">
    <variableExpression><![CDATA[$V{RUNNING_TOTAL}.subtract($V{PAGE_TOTAL})]]></variableExpression>
</variable>
```

### 4.3 Neue Variable visuell erstellen

1. **Outline** (links) → Rechtsklick auf **Variables** → **Create Variable**
2. **Properties**:
   - **Name**: `MY_VARIABLE`
   - **Class**: `java.math.BigDecimal`
   - **Calculation**: `Sum` (für Summen)
   - **Reset Type**: `Page` (bei Seitenwechsel zurücksetzen) oder `None`
   - **Variable Expression**: `$F{fieldName}` (Feld zum Summieren)

---

## 5. Workflow: Template in Jaspersoft bearbeiten → In IntelliJ verwenden

### Schritt 1: Template in Jaspersoft Studio anpassen

1. **Öffnen**: `invoice_template.jrxml`
2. **Anpassungen vornehmen**:
   - Logo hinzufügen: **Palette → Image** → In Title-Band ziehen
   - Schriftarten ändern: Element markieren → **Properties → Font**
   - Neue Spalte hinzufügen: **TextField** in Column Header + Detail ziehen
3. **Speichern**: `Ctrl+S`

### Schritt 2: In IntelliJ testen

```bash
# In WSL Terminal oder IntelliJ Terminal
cd /home/r-uu/develop/github/main/root/sandbox/office/microsoft/word/jasper

# Kompilieren und ausführen
mvn clean compile exec:java -Dexec.mainClass="de.ruu.sandbox.office.microsoft.word.jasper.InvoiceGenerator"
```

**Erwartetes Ergebnis**:
```
Rechnung erstellt: target/rechnung_beispiel.docx
Rechnung erstellt: target/rechnung_beispiel.pdf
```

### Schritt 3: Ergebnis prüfen

- **DOCX öffnen**: `\\wsl.localhost\Ubuntu\home\r-uu\develop\github\main\root\sandbox\office\microsoft\word\jasper\target\rechnung_beispiel.docx`
- **PDF öffnen**: `\\wsl.localhost\Ubuntu\home\r-uu\develop\github\main\root\sandbox\office\microsoft\word\jasper\target\rechnung_beispiel.pdf`

---

## 6. Fortgeschrittene Features

### 6.1 Zwischensummen mit "Print When Expression"

**Anwendungsfall**: Zwischensumme nur auf Seite 2+ anzeigen

1. **Text Field** in Page Footer markieren
2. **Properties → Print When Expression**:
```java
$V{PAGE_NUMBER} > 1
```

### 6.2 Überträge anzeigen

**Im Page Header (ab Seite 2)**:

1. **Static Text**: "Übertrag von Seite"
2. **Text Field**: `$V{PAGE_NUMBER} - 1`
3. **Text Field**: `$V{PREVIOUS_PAGE_TOTAL}` (Pattern: `#,##0.00 €`)

### 6.3 Conditional Formatting

**Beispiel**: Negative Beträge rot anzeigen

1. Text Field markieren → **Properties → Forecolor**
2. **Expression**: Klick auf **fx**-Button
```java
$F{total}.compareTo(BigDecimal.ZERO) < 0 ? java.awt.Color.RED : java.awt.Color.BLACK
```

### 6.4 Subreports

**Für komplexe Rechnungen mit mehreren Sections**:

1. **File → New → Jasper Report**
2. Name: `invoice_subreport.jrxml`
3. In Hauptreport: **Palette → Subreport** → In Band ziehen
4. **Subreport Expression**: `"invoice_subreport.jasper"`

---

## 7. IntelliJ Integration optimieren

### 7.1 Maven Exec Plugin konfigurieren

In `jasper/pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>exec-maven-plugin</artifactId>
            <version>3.1.0</version>
            <executions>
                <execution>
                    <id>generate-invoice</id>
                    <goals>
                        <goal>java</goal>
                    </goals>
                    <configuration>
                        <mainClass>de.ruu.sandbox.office.microsoft.word.jasper.InvoiceGenerator</mainClass>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**Ausführen in IntelliJ**:
- **Run → Edit Configurations**
- **+ → Maven**
- **Command line**: `exec:java@generate-invoice`

### 7.2 Live Reload mit File Watcher (Optional)

1. **IntelliJ → Settings → Tools → File Watchers**
2. **+ → Custom**
   - **File type**: `Files`
   - **Scope**: `*.jrxml`
   - **Program**: `mvn`
   - **Arguments**: `exec:java@generate-invoice`

**Effekt**: Bei jedem Speichern von `.jrxml` wird automatisch neu kompiliert.

---

## 8. Best Practices

### ✅ DO

- **Jaspersoft nur für Template-Design verwenden**
- **IntelliJ für Java-Code und Builds**
- **Templates in `src/main/resources/templates/` ablegen**
- **Versionskontrolle**: `.jrxml` in Git committen
- **Preview in Jaspersoft**: Mit Beispieldaten testen

### ❌ DON'T

- **NICHT**: `.project`, `.classpath`, `.settings/` in Git committen
- **NICHT**: Komplexe Java-Logik in Template-Expressions
- **NICHT**: Templates direkt in Jaspersoft kompilieren (Maven macht das)

---

## 9. Troubleshooting

### Problem: "Template nicht gefunden"

**Symptom**: `IllegalStateException: Template nicht gefunden: /templates/invoice_template.jrxml`

**Lösung**:
```bash
# Prüfen, ob Datei existiert
ls -la root/sandbox/office/microsoft/word/jasper/src/main/resources/templates/

# Maven neu bauen
mvn clean compile
```

### Problem: "Variablen nicht korrekt berechnet"

**Lösung**: In Jaspersoft Studio → **Preview** → **Java Preview with Sample Data**
- Ermöglicht Debugging mit Beispieldaten

### Problem: "DOCX Export fehlerhaft"

**Häufige Ursachen**:
- Überlappende Elemente → In Design-Ansicht prüfen
- Fehlende Fonts → `jasperreports-fonts` dependency prüfen

---

## 10. Nächste Schritte

Nach dem Setup können Sie:

1. **Eigenes Logo hinzufügen**: Image-Element in Title-Band
2. **Firmenadresse**: Static Text anpassen
3. **Mehrwertsteuer-Aufschlüsselung**: Group-Band für MwSt-Sätze
4. **Zahlungsbedingungen**: Text in Summary-Band
5. **Barcode/QR-Code**: Barcode-Komponente aus Palette

---

## 11. Referenzen

- **Jaspersoft Community**: https://community.jaspersoft.com/
- **JasperReports Dokumentation**: https://jasperreports.sourceforge.net/
- **Video Tutorials**: https://www.youtube.com/c/JaspersoftStudio
- **Ultimate Guide**: https://jaspersoft.github.io/jasperreports/

---

## 12. Schnelleinstieg Cheatsheet

### Template erstellen
```bash
File → New → Jasper Report → Blank A4
```

### Element hinzufügen
```
Palette → [Element] → Auf Canvas ziehen
```

### Variable erstellen
```
Outline → Variables → Rechtsklick → Create Variable
```

### Daten binden
```
Element markieren → Properties → Expression → fx-Button
Syntax: $F{fieldName}, $P{parameterName}, $V{variableName}
```

### Preview
```
Preview-Tab → Java Preview with Parameter Prompts
```

### Export nach IntelliJ
```
File → Save → Zurück zu IntelliJ → mvn compile
```

---

**Viel Erfolg mit Ihrem Rechnungs-Generator! 🎉**

