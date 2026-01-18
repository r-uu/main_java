# JasperReports & DocX4J - Dokumentgenerierung

**Professionelle PDF- und DOCX-Generierung für Rechnungen**

---

## 🚀 QUICK START

### JasperReports (empfohlen für komplexe Reports):
```bash
# Server starten
docker compose -f ~/develop/github/main/config/shared/docker/docker-compose.yml up -d jasperreports

# Rechnung generieren
curl -X POST http://localhost:8090/api/report/generate \
  -H "Content-Type: application/json" \
  -d '{
    "template": "invoice.jasper",
    "format": "pdf",
    "data": {
      "invoiceNumber": "2026-001",
      "invoiceDate": "2026-01-17",
      "customerName": "Firma GmbH",
      "customerAddress": "Straße 1, 12345 Stadt",
      "subtotal": 1000.0,
      "tax": 190.0,
      "total": 1190.0,
      "dueDate": "2026-02-28",
      "notes": "Zahlung 14 Tage",
      "items": [
        {"description": "Produkt", "quantity": 1, "unitPrice": 1000.0, "totalPrice": 1000.0}
      ]
    }
  }'
```

### DocX4J (einfach für Standard-Dokumente):
```java
InvoiceGenerator generator = new InvoiceGenerator();
InvoiceData data = new InvoiceData();
// ... Daten befüllen ...
generator.generateInvoice(data, "rechnung.docx");
```

---

## 📋 MODULE

### JasperReports (Docker-basiert)
- **server** - REST-API für Report-Generierung (Java 17, Docker)
- **client** - Java-Client für API-Zugriff (JPMS)
- **model** - Gemeinsame Datenmodelle (JPMS)
- **example** - Verwendungsbeispiele

### DocX4J (Library-basiert)
- **docx4j** - Direkte DOCX-Generierung (JPMS)

---

## 🎯 WANN WELCHES TOOL?

| Feature | JasperReports | DocX4J |
|---------|---------------|---------|
| **Automatische Seitenumbrüche** | ✅ Ja | ❌ Manuell |
| **Zwischensummen/Überträge** | ✅ Automatisch | ❌ Manuell |
| **Template-Design** | ✅ JasperSoft Studio | ❌ Code |
| **PDF-Export** | ✅ Ja | ❌ Nein (nur DOCX) |
| **DOCX-Export** | ✅ Ja | ✅ Ja |
| **Setup-Komplexität** | ⚠️ Docker nötig | ✅ Einfach |
| **JPMS-Unterstützung** | ✅ Client/Model | ✅ Vollständig |

**Empfehlung:**
- **JasperReports** für: Komplexe Rechnungen über mehrere Seiten, automatische Berechnungen
- **DocX4J** für: Einfache Dokumente, schnelle Prototypen

---

## 📚 DOKUMENTATION

### JasperReports:
- `jasperreports/QUICK-REFERENCE.md` - Schnellreferenz
- `jasperreports/FINALE-ZUSAMMENFASSUNG.md` - Vollständige Anleitung
- `jasperreports/DATEN-PROBLEM-BEHOBEN.md` - Parameter-Referenz

### DocX4J:
- `docx4j/README.md` - Englische Dokumentation
- `docx4j/README_DE.md` - Deutsche Dokumentation

---

## 🔧 SETUP

### JasperReports:
1. Template in JasperSoft Studio erstellen (`.jrxml`)
2. Kompilieren → `.jasper`
3. In `jasperreports/templates/` ablegen
4. Docker-Container starten
5. API aufrufen

**Wichtig:** Kompilierte `.jasper` Templates verwenden (nicht `.jrxml`)!

### DocX4J:
1. Dependency einbinden
2. Code schreiben
3. Ausführen

---

## 🏗️ ARCHITEKTUR

### JasperReports (Client-Server):
```
[Java App] → [Client] → HTTP → [Server (Docker)] → [JasperReports Engine] → PDF/DOCX
                                    ↓
                              [Templates (.jasper)]
```

### DocX4J (Library):
```
[Java App] → [DocX4J Library] → DOCX
```

---

## 📦 DEPENDENCIES

Alle Dependencies werden zentral im **BOM** (`bom/pom.xml`) verwaltet:

```xml
<!-- JasperReports -->
<dependency>
    <groupId>net.sf.jasperreports</groupId>
    <artifactId>jasperreports</artifactId>
    <!-- Version vom BOM -->
</dependency>

<!-- DocX4J -->
<dependency>
    <groupId>org.docx4j</groupId>
    <artifactId>docx4j-JAXB-ReferenceImpl</artifactId>
    <!-- Version vom BOM -->
</dependency>
```

---

## ✅ STATUS

- ✅ JasperReports funktioniert vollständig (PDF & DOCX)
- ✅ DocX4J funktioniert vollständig (DOCX)
- ✅ JPMS-konform
- ✅ Docker-Setup produktionsreif
- ✅ Dependencies im BOM konsolidiert

---

## 📖 WEITERE INFORMATIONEN

Siehe Unterverzeichnisse für detaillierte Dokumentation:
- `jasperreports/` - JasperReports-Modul
- `docx4j/` - DocX4J-Modul

