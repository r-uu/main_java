# JasperReports Docker Service

Isolierte JasperReports-Umgebung in Docker - unabhängig von deiner GraalVM 25 JPMS-Umgebung.

## 🎯 Konzept

```
┌─────────────────────────────────────────┐
│ Deine Haupt-Umgebung                    │
│ • GraalVM 25 + JPMS                     │
│ • Docx4j für Word-Dokumente             │
│                                         │
│   ┌───────────────────────────────┐    │
│   │ Docker Container              │    │
│   │ • JasperReports 7.0.1         │    │
│   │ • Java 17                     │    │
│   │ • REST API + CLI              │    │
│   │ • Kein JPMS                   │    │
│   └───────────────────────────────┘    │
│            ↓ REST/CLI ↑                 │
└─────────────────────────────────────────┘
```

## 🚀 Schnellstart

### 1. Service starten

```bash
# In Docker-Verzeichnis
cd config/shared/docker

# Container bauen und starten
docker-compose -f docker-compose.jasper.yml up -d --build

# Status prüfen
docker ps | grep jasperreports
```

### 2. Template platzieren

```bash
# Dein JasperReports Template (.jrxml)
cp mein_template.jrxml config/shared/docker/jasperreports/templates/
```

### 3. Report generieren

**Option A: REST API**
```bash
curl -X POST http://localhost:8090/api/report/generate \
  -H "Content-Type: application/json" \
  -d '{
    "template": "invoice.jrxml",
    "format": "pdf",
    "parameters": {
      "customerName": "Max Mustermann",
      "invoiceNumber": "INV-001"
    }
  }'
```

**Option B: CLI (direkt im Container)**
```bash
docker exec jasperreports-service java -jar target/jasperreports-service.jar cli \
  /app/templates/invoice.jrxml \
  /app/data.json \
  /app/output/invoice.pdf
```

**Option C: Von deiner Java-Anwendung**
```java
// Simple HTTP Client
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8090/api/report/generate"))
    .header("Content-Type", "application/json")
    .POST(HttpRequest.BodyPublishers.ofString("""
        {
          "template": "invoice.jrxml",
          "format": "pdf",
          "parameters": {
            "customerName": "Max Mustermann"
          }
        }
        """))
    .build();
    
HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
// Ergebnis verarbeiten
```

## 📁 Verzeichnisstruktur

```
config/shared/docker/jasperreports/
├── Dockerfile                 # Container-Definition
├── pom.xml                    # Maven Dependencies
├── src/                       # Java Source Code
│   └── main/java/de/ruu/jasper/
│       └── ReportService.java # REST API + CLI
├── templates/                 # Deine .jrxml Templates
│   └── invoice.jrxml
└── output/                    # Generierte Reports
    └── xxxxxxxx-xxxx-xxxx.pdf
```

## 🔧 Verwendung

### Templates verwalten

```bash
# Template hinzufügen
cp my_report.jrxml config/shared/docker/jasperreports/templates/

# Templates auflisten
curl http://localhost:8090/api/templates

# Container neu starten (nach Template-Änderung)
docker-compose -f docker-compose.jasper.yml restart
```

### Reports abholen

```bash
# Generierte Reports liegen in:
ls -la config/shared/docker/jasperreports/output/

# Report kopieren
cp config/shared/docker/jasperreports/output/xxx.pdf ~/Downloads/
```

### Logs prüfen

```bash
# Live-Logs
docker-compose -f docker-compose.jasper.yml logs -f jasperreports

# Letzte 100 Zeilen
docker logs jasperreports-service --tail 100
```

## 🌐 REST API Endpunkte

### POST /api/report/generate
Report generieren

**Request:**
```json
{
  "template": "invoice.jrxml",
  "format": "pdf",  // oder "docx"
  "parameters": {
    "customerName": "Max Mustermann",
    "invoiceNumber": "INV-001",
    "total": 1234.56
  }
}
```

**Response:**
```json
{
  "success": true,
  "outputFile": "/app/output/xxxxxxxx-xxxx-xxxx.pdf",
  "message": "Report erfolgreich generiert"
}
```

### GET /api/templates
Liste verfügbare Templates

**Response:**
```json
{
  "templates": [
    "invoice.jrxml",
    "report.jrxml"
  ]
}
```

### GET /health
Health Check

**Response:** `OK`

## 🔄 Integration in deine Anwendung

### Beispiel: Java Service

```java
package de.ruu.app.reporting;

import java.net.http.*;
import java.net.URI;
import java.util.Map;
import com.google.gson.Gson;

public class JasperReportsClient {
    
    private static final String JASPER_URL = "http://localhost:8090/api/report/generate";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    
    public String generateInvoice(String customerName, double total) throws Exception {
        var request = Map.of(
            "template", "invoice.jrxml",
            "format", "pdf",
            "parameters", Map.of(
                "customerName", customerName,
                "total", total
            )
        );
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(JASPER_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
            .build();
            
        HttpResponse<String> response = client.send(httpRequest, 
            HttpResponse.BodyHandlers.ofString());
            
        var result = gson.fromJson(response.body(), Map.class);
        return (String) result.get("outputFile");
    }
}
```

## 🛠️ Entwicklung

### Container neu bauen (nach Code-Änderungen)

```bash
docker-compose -f docker-compose.jasper.yml up -d --build
```

### Im Container debuggen

```bash
# Shell im Container
docker exec -it jasperreports-service sh

# Java Version prüfen
docker exec jasperreports-service java -version

# Maven Dependencies prüfen
docker exec jasperreports-service mvn dependency:tree
```

## 🎨 Beispiel-Template

Erstelle `templates/invoice.jrxml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports"
              name="invoice" pageWidth="595" pageHeight="842">
    
    <parameter name="customerName" class="java.lang.String"/>
    <parameter name="total" class="java.lang.Double"/>
    
    <title>
        <band height="100">
            <staticText>
                <reportElement x="0" y="0" width="200" height="30"/>
                <text><![CDATA[RECHNUNG]]></text>
            </staticText>
        </band>
    </title>
    
    <detail>
        <band height="50">
            <textField>
                <reportElement x="0" y="0" width="200" height="20"/>
                <textFieldExpression><![CDATA["Kunde: " + $P{customerName}]]></textFieldExpression>
            </textField>
            <textField>
                <reportElement x="0" y="25" width="200" height="20"/>
                <textFieldExpression><![CDATA["Betrag: " + $P{total} + " EUR"]]></textFieldExpression>
            </textField>
        </band>
    </detail>
</jasperReport>
```

## 🔒 Sicherheit

- Container läuft ohne Root-Rechte
- Nur Port 8090 exponiert
- Templates read-only gemountet
- Output-Verzeichnis isoliert

## 📊 Performance

- Container startet in ~10 Sekunden
- Report-Generierung: ~100-500ms (abhängig von Komplexität)
- Speicherverbrauch: ~512 MB (konfigurierbar)

## 🆘 Troubleshooting

**Container startet nicht:**
```bash
docker-compose -f docker-compose.jasper.yml logs jasperreports
```

**Port 8090 bereits belegt:**
Ändere in `docker-compose.jasper.yml`: `"8091:8090"`

**Template nicht gefunden:**
```bash
# Prüfe ob Template im richtigen Verzeichnis ist
ls config/shared/docker/jasperreports/templates/

# Prüfe Container-Mount
docker exec jasperreports-service ls /app/templates
```

## 🔧 Aliase (optional)

Füge zu `~/.bashrc` oder `config/shared/wsl/aliases.sh` hinzu:

```bash
alias jasper-start='cd $RUU_DOCKER && docker-compose -f docker-compose.jasper.yml up -d'
alias jasper-stop='cd $RUU_DOCKER && docker-compose -f docker-compose.jasper.yml down'
alias jasper-logs='docker logs jasperreports-service -f'
alias jasper-shell='docker exec -it jasperreports-service sh'
```

## ✅ Vorteile dieser Lösung

1. ✅ **Isoliert** - Keine Konflikte mit GraalVM 25 / JPMS
2. ✅ **Stabil** - Java 17 LTS, bewährte JasperReports-Version
3. ✅ **Flexibel** - REST API oder CLI
4. ✅ **Portabel** - Läuft überall wo Docker läuft
5. ✅ **Wartbar** - Alle Dependencies im Container
6. ✅ **Skalierbar** - Mehrere Instanzen möglich

---

**Erstellt:** 2026-01-15  
**Maintainer:** r-uu

