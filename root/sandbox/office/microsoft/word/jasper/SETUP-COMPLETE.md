# ✅ JasperReports Setup Complete!

## Was wurde erstellt?

### 📁 Dateien

```
root/sandbox/office/microsoft/word/jasper/
├── pom.xml                              ✅ Maven-Konfiguration mit allen Dependencies
├── README.md                            ✅ Vollständige Dokumentation
├── QUICKSTART.md                        ✅ 5-Minuten Schnellstart
├── JASPERSOFT-STUDIO-SETUP.md           ✅ Detaillierte Jaspersoft Studio Anleitung
└── src/main/
    ├── java/de/ruu/sandbox/office/microsoft/word/jasper/
    │   ├── InvoiceData.java             ✅ Datenmodell für Rechnungen
    │   └── InvoiceGenerator.java        ✅ Generator (Main-Klasse)
    └── resources/templates/
        └── invoice_template.jrxml       ✅ JasperReports Template mit Zwischensummen
```

### 🎯 Features

✅ **Mehrseitige Rechnungen** - Automatische Seitenumbrüche  
✅ **Zwischensummen** - Pro Seite automatisch berechnet  
✅ **Überträge** - "Übertrag von Seite X: €Y"  
✅ **DOCX Export** - Microsoft Word Format  
✅ **PDF Export** - PDF Format  
✅ **Maven Integration** - Vollständig in Build integriert  
✅ **Visual Designer Support** - Jaspersoft Studio kompatibel  

---

## 🚀 Nächste Schritte

### Schritt 1: Testen Sie das Setup

```bash
# In IntelliJ Terminal oder WSL
cd /home/r-uu/develop/github/main/root/sandbox/office/microsoft/word/jasper

# Kompilieren und Beispiel generieren
mvn clean compile exec:java
```

**Erwartetes Ergebnis**:
```
[INFO] BUILD SUCCESS
Rechnung erstellt: target/rechnung_beispiel.docx
Rechnung erstellt: target/rechnung_beispiel.pdf
```

**Dateien öffnen**:
- `target/rechnung_beispiel.docx` (50 Positionen, mehrere Seiten, Zwischensummen)
- `target/rechnung_beispiel.pdf`

### Schritt 2: Jaspersoft Studio installieren (Optional aber empfohlen)

Für visuelles Template-Design mit Eclipse und Jaspersoft Studio siehe:

📘 **[ECLIPSE-JASPERSOFT-INSTALLATION.md](ECLIPSE-JASPERSOFT-INSTALLATION.md)** ← **Detaillierte Schritt-für-Schritt-Anleitung**

Diese Anleitung zeigt:
- ✅ Eclipse Installation in Windows 11
- ✅ Jaspersoft Studio Plugin hinzufügen
- ✅ WSL-Integration konfigurieren
- ✅ Optimaler Workflow Eclipse ↔ IntelliJ
- ✅ Template-Design, Variablen, Zwischensummen visuell erstellen

**Quick Overview**:
1. **Eclipse IDE for Java Developers** installieren
2. **Jaspersoft Studio Plugin** via Eclipse Marketplace
3. **WSL-Projekt** importieren: `\\wsl.localhost\Ubuntu\home\r-uu\develop\github\main\root\sandbox\office\microsoft\word\jasper`
4. **Template öffnen**: `src/main/resources/templates/invoice_template.jrxml`
5. **Visuell bearbeiten**: Logo hinzufügen, Farben ändern, Layout anpassen
6. **Speichern** → In IntelliJ: `mvn compile exec:java` → Preview

### Schritt 3: Eigene Rechnung erstellen

Siehe **[QUICKSTART.md](QUICKSTART.md)** für Beispiel-Code.

---

## 📚 Dokumentation

| Datei | Beschreibung |
|-------|--------------|
| **[QUICKSTART.md](QUICKSTART.md)** | 5-Minuten Schnellstart |
| **[README.md](README.md)** | Vollständige API-Dokumentation |
| **[ECLIPSE-JASPERSOFT-INSTALLATION.md](ECLIPSE-JASPERSOFT-INSTALLATION.md)** | **Schritt-für-Schritt Eclipse & Jaspersoft Setup** |
| **[JASPERSOFT-STUDIO-SETUP.md](JASPERSOFT-STUDIO-SETUP.md)** | Template-Design Konzepte & Workflow |

---

## 🎓 Warum JasperReports für Rechnungen?

### vs. docx4j (manuelle Word-Manipulation)

| Feature | JasperReports | docx4j |
|---------|---------------|--------|
| Mehrseitige Rechnungen | ✅ Automatisch | ⚠️ Manuell berechnen |
| Zwischensummen | ✅ Variables | ❌ Komplex |
| Überträge | ✅ Built-in | ❌ Sehr komplex |
| Visual Designer | ✅ Jaspersoft Studio | ❌ Nur XML |
| Learning Curve | 🟢 Mittel | 🔴 Hoch |
| Word-Template | ⚠️ Eigenes Format | ✅ Natives DOCX |
| PDF Export | ✅ Nativ | ⚠️ Zusätzliche Library |

**Fazit**: Für Rechnungen mit Zwischensummen ist JasperReports **deutlich weniger komplex** als manuelle DOCX-Manipulation.

### Beispiel: Zwischensummen

**JasperReports** (Deklarativ):
```xml
<variable name="PAGE_TOTAL" class="BigDecimal" resetType="Page" calculation="Sum">
    <variableExpression><![CDATA[$F{total}]]></variableExpression>
</variable>
```

**docx4j** (Imperativ):
```java
// Manuelle Berechnung:
BigDecimal pageTotal = BigDecimal.ZERO;
int itemsOnPage = 0;
for (InvoiceItem item : items) {
    pageTotal = pageTotal.add(item.getTotal());
    itemsOnPage++;
    
    // Seitenumbruch manuell berechnen
    if (itemsOnPage >= MAX_ITEMS_PER_PAGE) {
        // Zwischensumme manuell ins Dokument einfügen
        // Neue Seite erstellen
        // Übertrag auf nächste Seite manuell berechnen
        // ...
    }
}
```

---

## 🛠️ IntelliJ Integration

### Maven Run Configuration

1. **IntelliJ → Run → Edit Configurations**
2. **+ → Maven**
3. **Name**: `Generate Invoice`
4. **Command line**: `clean compile exec:java`
5. **Working directory**: `.../root/sandbox/office/microsoft/word/jasper`
6. **OK**

**Jetzt**: `Run → Generate Invoice` → Rechnung wird erstellt

### File Watcher (Auto-Compile bei Template-Änderung)

Optional: Template-Änderungen automatisch kompilieren

1. **Settings → Tools → File Watchers**
2. **+ → Custom**
   - **File type**: Files
   - **Scope**: `*.jrxml`
   - **Program**: `mvn`
   - **Arguments**: `compile exec:java`

---

## 🎨 Template Customization Beispiele

### Logo hinzufügen

In Jaspersoft Studio:
1. **Palette → Image** → In Title-Band ziehen
2. **Properties → Image Expression**: `"logo.png"`
3. Logo-Datei in `src/main/resources/` ablegen

### Firmenadresse

1. **Title-Band** → Static Text bearbeiten
2. Text ändern zu Ihrer Adresse
3. Speichern

### Mehrwertsteuer-Aufschlüsselung

1. **Outline → Groups → Create Group**
2. **Group by**: `vatRate`
3. **Group Footer**: MwSt-Summe pro Satz

### QR-Code für SEPA-Überweisung

```xml
<componentElement>
    <jr:QRCode>
        <jr:codeExpression><![CDATA[
            "BCD\n002\n1\nSCT\n" + 
            "BICXXXXX\n" +
            "Empfänger\n" +
            "DE89370400440532013000\n" +
            "EUR" + $P{TOTAL_AMOUNT}
        ]]></jr:codeExpression>
    </jr:QRCode>
</componentElement>
```

---

## 🐛 Troubleshooting

### Template nicht gefunden

```bash
# Prüfen
ls -la src/main/resources/templates/invoice_template.jrxml

# Falls nicht vorhanden
mvn clean compile
```

### Dependencies fehlen

```bash
cd /home/r-uu/develop/github/main
mvn clean install -pl bom  # BOM neu bauen
cd root/sandbox/office/microsoft/word/jasper
mvn clean compile
```

### DOCX fehlerhaft

**Ursache**: Überlappende Elemente im Template  
**Lösung**: In Jaspersoft Studio → Design-Ansicht → Elemente prüfen

---

## 📞 Support

- **JasperReports Community**: https://community.jaspersoft.com/
- **Dokumentation**: https://jasperreports.sourceforge.net/
- **Video Tutorials**: https://www.youtube.com/c/JaspersoftStudio

---

## ✨ Zusammenfassung

Sie haben jetzt:

1. ✅ **Funktionierenden Invoice Generator** mit JasperReports
2. ✅ **Template mit Zwischensummen** und Überträgen
3. ✅ **DOCX & PDF Export** Funktionalität
4. ✅ **Maven Integration** für IntelliJ
5. ✅ **Dokumentation** für Visual Designer (Jaspersoft Studio)

**Nächster Schritt**: Führen Sie `mvn clean compile exec:java` aus und schauen Sie sich die generierte Rechnung an!

---

**Viel Erfolg mit Ihrem Rechnungs-Generator! 🎉📄💼**

