# ✅ JasperReports Maven-Hierarchie wiederhergestellt

**Datum:** 2026-01-18  
**Status:** ✅ **ABGESCHLOSSEN**

---

## 🎯 FINALE STRUKTUR

### root/lib - Wiederverwendbare Libraries:
```
root/lib/
└── office/                           # r-uu.lib.office
    └── word/                         # r-uu.lib.office.word
        └── jasperreports/            # r-uu.lib.jasperreports
            ├── pom.xml
            ├── client/               # r-uu.lib.jasperreports.client
            └── server/               # r-uu.lib.jasperreports.server
```

### root/sandbox - Examples und Models:
```
root/sandbox/office/microsoft/word/
└── jasperreports/                    # r-uu.sandbox.jasperreports
    ├── pom.xml
    ├── model/                        # r-uu.sandbox.jasperreports.model
    ├── example/                      # r-uu.sandbox.jasperreports.example
    ├── templates/                    # *.jrxml files
    ├── Dockerfile
    └── docker-compose.yml
```

---

## 📦 MAVEN ARTEFAKTE

### lib (Wiederverwendbare Komponenten):
```xml
<!-- Parent-Module -->
r-uu.lib.office
r-uu.lib.office.word
r-uu.lib.jasperreports

<!-- Implementierungen -->
r-uu.lib.jasperreports.client         → HTTP-Client für Report-Service
r-uu.lib.jasperreports.server         → REST-API Server (Docker)
```

### sandbox (Examples und Models):
```xml
<!-- Parent -->
r-uu.sandbox.jasperreports

<!-- Implementierungen -->
r-uu.sandbox.jasperreports.model      → InvoiceData, InvoiceItem, etc.
r-uu.sandbox.jasperreports.example    → Beispiel-Nutzung
```

---

## 🔗 DEPENDENCIES

### client → model:
```xml
<dependency>
    <groupId>r-uu</groupId>
    <artifactId>r-uu.sandbox.jasperreports.model</artifactId>
    <version>${project.version}</version>
</dependency>
```

### server → model:
```xml
<dependency>
    <groupId>r-uu</groupId>
    <artifactId>r-uu.sandbox.jasperreports.model</artifactId>
    <version>${project.version}</version>
</dependency>
```

### example → client + model:
```xml
<!-- Client from lib -->
<dependency>
    <groupId>r-uu</groupId>
    <artifactId>r-uu.lib.jasperreports.client</artifactId>
    <version>${project.version}</version>
</dependency>

<!-- Model from sandbox -->
<dependency>
    <groupId>r-uu</groupId>
    <artifactId>r-uu.sandbox.jasperreports.model</artifactId>
    <version>${project.version}</version>
</dependency>
```

---

## ✅ BUILD VERIFICATION

```
[INFO] r-uu.lib.office .................................... SUCCESS
[INFO] r-uu.lib.office.word ............................... SUCCESS
[INFO] r-uu.lib.jasperreports ............................. SUCCESS
[INFO] r-uu.lib.jasperreports.client ...................... SUCCESS
[INFO] r-uu.lib.jasperreports.server ...................... SUCCESS
[INFO] r-uu.sandbox.jasperreports ......................... SUCCESS
[INFO] r-uu.sandbox.jasperreports.model ................... SUCCESS
[INFO] r-uu.sandbox.jasperreports.example ................. SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

---

## 🎯 DESIGN-RATIONALE

### Warum diese Aufteilung?

**lib/office/word/jasperreports:**
- ✅ **client** = Generischer HTTP-Client, wiederverwendbar
- ✅ **server** = Generischer REST-API Server, wiederverwendbar
- ✅ Keine projekt-spezifischen Models
- ✅ Kann in verschiedenen Projekten genutzt werden

**sandbox/office/microsoft/word/jasperreports:**
- ✅ **model** = Projekt-spezifische Datenmodelle (InvoiceData, etc.)
- ✅ **example** = Beispiel-Implementierung
- ✅ **templates** = JRXML-Vorlagen
- ✅ Experimentell, projekt-spezifisch

---

## 🔄 DEPENDENCIES FLOW

```
example (sandbox)
    ↓ uses
client (lib) ←─────┐
    ↓ uses         │
model (sandbox) ←──┤
    ↑ uses         │
server (lib) ──────┘
```

**Alle Komponenten nutzen das gleiche Model aus sandbox!**

---

## 📝 GEÄNDERTE DATEIEN

### Neu erstellt:
1. `root/lib/office/pom.xml`
2. `root/lib/office/word/pom.xml`
3. `root/sandbox/office/microsoft/word/jasperreports/pom.xml`

### Aktualisiert:
1. `root/lib/pom.xml` - Modul `office` statt `office/word/jasperreports`
2. `root/lib/office/word/jasperreports/pom.xml` - Parent zu `r-uu.lib.office.word`
3. `root/lib/office/word/jasperreports/client/pom.xml` - Dependency zu sandbox-model
4. `root/lib/office/word/jasperreports/server/pom.xml` - Dependency zu sandbox-model
5. `root/sandbox/office/microsoft/word/pom.xml` - Modul `jasperreports` hinzugefügt
6. `root/sandbox/office/microsoft/word/jasperreports/model/pom.xml` - Parent zu sandbox
7. `root/sandbox/office/microsoft/word/jasperreports/example/pom.xml` - Parent und Dependencies
8. `config/shared/wsl/aliases.sh` - `RUU_JASPER` Pfad
9. `config/shared/scripts/cleanup-jasperreports.sh` - `JASPER_DIR` Pfad
10. `config/shared/docker/docker-compose.yml` - Dockerfile und Volume Pfade

### Verschoben:
- `model/` von lib nach sandbox
- `example/` von lib nach sandbox
- `templates/` von lib nach sandbox

---

## 🚀 VERWENDUNG

### Navigation:
```bash
ruu-jasper    # → root/sandbox/office/microsoft/word/jasperreports
```

### Build:
```bash
# Alles bauen
cd root
mvn clean install

# Nur lib
cd root/lib
mvn clean install

# Nur sandbox
cd root/sandbox
mvn clean install
```

### Dependencies nutzen:

**In einem anderen Projekt den Client verwenden:**
```xml
<dependency>
    <groupId>r-uu</groupId>
    <artifactId>r-uu.lib.jasperreports.client</artifactId>
    <version>0.0.1</version>
</dependency>
```

**Eigene Models definieren:**
```java
// Eigene Invoice-Klasse, nicht die aus sandbox
public class MyInvoice {
    // ...
}
```

---

## 📂 DATEI-STANDORTE

### Code (lib):
- Client: `root/lib/office/word/jasperreports/client/src/`
- Server: `root/lib/office/word/jasperreports/server/src/`

### Code (sandbox):
- Model: `root/sandbox/office/microsoft/word/jasperreports/model/src/`
- Example: `root/sandbox/office/microsoft/word/jasperreports/example/src/`

### Resources:
- Templates: `root/sandbox/office/microsoft/word/jasperreports/templates/`
- Output: `root/sandbox/office/microsoft/word/jasperreports/server/output/`

### Docker:
- Dockerfile: `root/sandbox/office/microsoft/word/jasperreports/Dockerfile`
- docker-compose: `config/shared/docker/docker-compose.yml`

---

## 🎓 BEST PRACTICES

### ✅ DO:
- Wiederverwendbare Komponenten in `lib/`
- Projekt-spezifische Models in `sandbox/`
- Examples und Tests in `sandbox/`
- Klare Maven-Hierarchie mit Parent-Modulen

### ❌ DON'T:
- Projekt-spezifische Klassen in `lib/`
- Wiederverwendbare Libraries in `sandbox/`
- Flache Struktur ohne Parent-Module
- Zirkuläre Dependencies

---

## 📚 SIEHE AUCH

- `root/lib/office/word/jasperreports/` - Client & Server
- `root/sandbox/office/microsoft/word/jasperreports/` - Model, Example, Templates
- `config/ALIASE-SCHNELLREFERENZ.md` - Aliase

---

✅ **Maven-Hierarchie erfolgreich wiederhergestellt!**  
✅ **Saubere Trennung zwischen lib und sandbox!**  
✅ **BUILD SUCCESS!**  
✅ **Alle Pfade aktualisiert!**

